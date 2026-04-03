package com.laiketui.common.service.dubbo;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.laiketui.common.api.PublicGoodsClassService;
import com.laiketui.common.api.PublicGoodsService;
import com.laiketui.common.api.PubliceService;
import com.laiketui.common.api.admin.PublicAdminService;
import com.laiketui.common.mapper.BrandClassModelMapper;
import com.laiketui.common.mapper.CustomerModelMapper;
import com.laiketui.common.mapper.ProductClassModelMapper;
import com.laiketui.common.mapper.ProductListModelMapper;
import com.laiketui.common.utils.EasyPoiExcelUtil;
import com.laiketui.common.utils.tool.ImgUploadUtils;
import com.laiketui.common.utils.tool.cache.RedisDataTool;
import com.laiketui.core.cache.RedisUtil;
import com.laiketui.core.common.SplitUtils;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.exception.LaiKeApiWarnException;
import com.laiketui.core.lktconst.DictionaryConst;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.core.utils.tool.DataUtils;
import com.laiketui.core.utils.tool.DateUtil;
import com.laiketui.core.utils.tool.StringUtils;
import com.laiketui.domain.config.JumpPathModel;
import com.laiketui.domain.log.AdminRecordModel;
import com.laiketui.domain.mch.AdminModel;
import com.laiketui.domain.product.BrandClassModel;
import com.laiketui.domain.product.ProductClassModel;
import com.laiketui.domain.user.User;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.PageModel;
import com.laiketui.domain.vo.goods.GoodsClassVo;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * 关于运费
 *
 * @author Trick
 * @date 2020/11/13 9:11
 */
@Service
public class PublicGoodsClassServiceImpl implements PublicGoodsClassService
{

    private final Logger              logger = LoggerFactory.getLogger(PublicGoodsClassServiceImpl.class);
    @Autowired
    private       CustomerModelMapper customerModelMapper;

    @Autowired
    private ProductClassModelMapper productClassModelMapper;

    @Autowired
    private BrandClassModelMapper brandClassModelMapper;

    @Autowired
    private ProductListModelMapper productListModelMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private PubliceService publiceService;

    @Autowired
    private PublicGoodsService publicGoodsService;

    @Autowired
    private PublicAdminService publicAdminService;


    @Override
    public Map<String, Object> getClassInfo(GoodsClassVo vo, HttpServletResponse response) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);
            if (user.getMchId() == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DPBCZ, "店铺不存在", "index");
            }
            ProductClassModel productClassModel = new ProductClassModel();
            productClassModel.setStore_id(vo.getStoreId());
            productClassModel.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS);
            if (!StringUtils.isEmpty(vo.getClassName()))
            {
                productClassModel.setPname(vo.getClassName());
                if (vo.getLevel() == null)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误", "getClassInfo");
                }
                productClassModel.setLevel(vo.getLevel());
            }
            else
            {
                //1=查询下级,2=查询上级,3=根据类别Id查询 默认查询一级
                if (vo.getType() == null || vo.getType().equals(ClassType.FIRST_STAGE))
                {
                    //查询一级
                    productClassModel.setSid(0);
                }
                else if (vo.getType().equals(ClassType.SUBORDINATE))
                {
                    //查询下级
                    if (vo.getClassId() == null)
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误", "getClassInfo");
                    }
                    productClassModel.setSid(vo.getClassId());
                }
                else if (vo.getType().equals(ClassType.SUPERIOR))
                {
                    //查询上级
                    if (vo.getFatherId() == null)
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误", "getClassInfo");
                    }
                    productClassModel.setSid(vo.getFatherId());
                }
                else if (vo.getType().equals(ClassType.ID))
                {
                    //查询分类id
                    if (vo.getClassId() == null)
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误", "getClassInfo");
                    }
                    productClassModel.setCid(vo.getClassId());
                }
            }
            PageModel pageModel = new PageModel(vo.getPageNo(), vo.getPageSize());
            productClassModel.setPageModel(pageModel);

            String langCode = vo.getLang_code();
            if (StringUtils.isNotEmpty(langCode))
            {
                productClassModel.setLang_code(langCode);
            }

            //只展示审核通过的
            productClassModel.setExamine(Integer.valueOf(DictionaryConst.ExameStatus.EXAME_PASS_STATUS));
            int                     total                 = productClassModelMapper.getGoodsClassCount(productClassModel);
            List<ProductClassModel> productClassModelList = productClassModelMapper.getProductClassLevel(productClassModel);
            List<Map<String, Object>> productClassModelMap = JSON.parseObject(JSON.toJSONString(productClassModelList), new TypeReference<List<Map<String, Object>>>()
            {
            });
            //图片处理
            for (Map<String, Object> map : productClassModelMap)
            {
                int    level       = MapUtils.getIntValue(map, "level") + 1;
                String levelFormat = "%s级分类";
                switch (level)
                {
                    case 1:
                        levelFormat = String.format(levelFormat, "一");
                        break;
                    case 2:
                        levelFormat = String.format(levelFormat, "二");
                        break;
                    case 3:
                        levelFormat = String.format(levelFormat, "三");
                        break;
                    default:
                        levelFormat = String.format(levelFormat, level);
                        break;
                }
                map.put("levelFormat", levelFormat);
                String imgUrl = publiceService.getImgPath(MapUtils.getString(map, "img"), vo.getStoreId());
                map.put("img", imgUrl);
                Date addDate = new Date(MapUtils.getLongValue(map, "add_date"));
                map.put("add_date", DateUtil.dateFormate(addDate, GloabConst.TimePattern.YMDHMS));

                String langCodeDB = MapUtils.getString(map, "lang_code");
                map.put("lang_name", publiceService.getLangName(langCodeDB));
                map.put("lang_code", langCodeDB);


            }

            resultMap.put("classInfo", productClassModelMap);
            resultMap.put("total", total);
            if (vo.getExportType().equals(1))
            {
                if (productClassModelMap.size() < 1)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_LBBCZ, "当前没有分类信息", "getClassInfo");
                }
            }
            if (vo.getExportType().equals(1))
            {
                exportGoodsClassData(productClassModelMap, response);
                return null;
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取类别信息 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getClassInfo");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> getExamineClassInfo(MainVo vo, String condition, Integer status, String startTime, String endTime, Integer mch_id,Integer level) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            HashMap<String, Object> params = new HashMap<>();
            //展示没有审核通过的
            List<String> statusList = new ArrayList<>();
            statusList.add(DictionaryConst.ExameStatus.EXAME_WAIT_STATUS);
            statusList.add(DictionaryConst.ExameStatus.EXAME_NOT_PASS_STATUS);
            params.put("statusList", statusList);
            params.put("add_date_sort", "desc");
            params.put("mch_id", mch_id);
            params.put("recycle", 0);
            params.put("pageStart", vo.getPageNo());
            params.put("pageEnd", vo.getPageSize());

            if (StringUtils.isNotEmpty(vo.getLang_code()))
            {
                params.put("lang_code", vo.getLang_code());
            }
            if (!Objects.isNull(status))
            {
                params.put("status", status);
            }
            if (Objects.nonNull(level))
            {
                params.put("level",level);
            }
            if (StringUtils.isNotEmpty(startTime))
            {
                params.put("startTime", startTime);
            }
            if (StringUtils.isNotEmpty(endTime))
            {
                params.put("endTime", endTime);
            }
            if (StringUtils.isNotEmpty(condition))
            {
                params.put("className", condition);
            }

            int                       total                = productClassModelMapper.countDynamic(params);
            List<Map<String, Object>> productClassModelMap = productClassModelMapper.selectDynamic(params);

            //图片处理
            for (Map<String, Object> map : productClassModelMap)
            {
                int proudctLevel = MapUtils.getIntValue(map, "level") + 1;
                String levelFormat = "%s级分类";
                switch (proudctLevel)
                {
                    case 1:
                        levelFormat = String.format(levelFormat, "一");
                        break;
                    case 2:
                        levelFormat = String.format(levelFormat, "二");
                        break;
                    case 3:
                        levelFormat = String.format(levelFormat, "三");
                        break;
                    default:
                        levelFormat = String.format(levelFormat, proudctLevel);
                        break;
                }
                String examine     = MapUtils.getString(map, "examine");
                String examineDesc = "";
                if (examine.equals(DictionaryConst.ExameStatus.EXAME_WAIT_STATUS))
                {
                    examineDesc = "待审核";
                }
                else if (examine.equals(DictionaryConst.ExameStatus.EXAME_PASS_STATUS))
                {
                    examineDesc = "审核通过";
                }
                else
                {
                    examineDesc = "审核不通过";
                }
                map.put("examineDesc", examineDesc);
                ProductClassModel productClassModel = productClassModelMapper.selectByPrimaryKey(MapUtils.getInteger(map, "sid"));
                if (!Objects.isNull(productClassModel))
                {
                    map.put("spname", productClassModel.getPname());
                }
                map.put("levelFormat", levelFormat);
                String imgUrl = publiceService.getImgPath(MapUtils.getString(map, "img"), vo.getStoreId());
                map.put("img", imgUrl);
                //Date addDate = new Date(MapUtils.getLongValue(map, "add_date"));
                map.put("add_date", DateUtil.dateFormate(DataUtils.getStringVal(map, "add_date"), GloabConst.TimePattern.YMDHMS));

                map.put("lang_name", publiceService.getLangName(MapUtils.getString(map, "lang_code")));
                map.put("country_name", publiceService.getCountryName(MapUtils.getInteger(map, "country_num")));

            }

            resultMap.put("classInfo", productClassModelMap);
            resultMap.put("total", total);
            /*if (vo.getExportType().equals(1)) {
                if (productClassModelMap.size() < 1) {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_LBBCZ, "当前没有分类信息", "getClassInfo");
                }
            }
            if (vo.getExportType().equals(1)) {
                exportGoodsClassData(productClassModelMap, response);
                return null;
            }*/
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取类别信息 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getClassInfo");
        }
        return resultMap;
    }

    @Transactional(rollbackFor = Exception.class, noRollbackFor = LaiKeApiWarnException.class)
    @Override
    public void addClass(MainVo vo, Integer classId, String className, String ename, String img, int level, int fatherId,Integer type) throws LaiKeAPIException
    {
        try
        {
            int        storeType  = vo.getStoreType();
            AdminModel adminModel = null;
            User       user       = null;
            switch (storeType)
            {
                case GloabConst.StoreType.STORE_TYPE_PC_ADMIN:
                    adminModel = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
                    break;
                case GloabConst.StoreType.STORE_TYPE_PC_MCH:
                    user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);
                    break;
            }

            //店铺id
            int mchid = -1;
            if (adminModel != null)
            {
                mchid = adminModel.getShop_id();
            }
            else
            {
                mchid = user.getMchId();
            }

            if (mchid == -1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误", "addclass");
            }

            //获取自营店id
            Integer storeMchId = customerModelMapper.getStoreMchId(vo.getStoreId());
            boolean is_zy      = false;
            if (mchid == storeMchId)
            {
                is_zy = true;
            }
            ProductClassModel productClassOld = null;
            //是否是编辑
            boolean isUpdate = false;
            if (classId != null)
            {
                isUpdate = true;
                productClassOld = productClassModelMapper.selectByPrimaryKey(classId);
                productClassOld.setCid(classId);
                productClassOld.setStore_id(vo.getStoreId());
                productClassOld.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS);
                productClassOld = productClassModelMapper.selectOne(productClassOld);
                if (productClassOld == null)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_LBBCZ, "类别不存在", "addClass");
                }
            }

            ProductClassModel saveClass = new ProductClassModel();
            saveClass.setStore_id(vo.getStoreId());
            saveClass.setEnglish_name(ename);
            saveClass.setPname(className);
            saveClass.setImg(ImgUploadUtils.getUrlImgByName(img, true));
            saveClass.setMch_id(mchid);
            //如果不是自营店则需要等待审核，自营店无需审核
            if (!is_zy)
            {
                saveClass.setExamine(Integer.valueOf(DictionaryConst.ExameStatus.EXAME_WAIT_STATUS));
            }
            else
            {
                saveClass.setExamine(Integer.valueOf(DictionaryConst.ExameStatus.EXAME_PASS_STATUS));
            }
            if (StringUtils.isEmpty(className))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_LMBNWK, "类名不能为空");
            }
            if (className.length() > 15)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "分类长度限制15个字符");
            }

            int count;
            if (productClassOld == null || !className.equals(productClassOld.getPname()))
            {
                Map<String,Object> paramMap = new HashMap<String,Object>();
                paramMap.put("storeId",vo.getStoreId());
                paramMap.put("pname",className);
                //判断类别名称是否存在
                //只再当前类目中筛选分类 35353
                if (fatherId > 0)
                {
                    paramMap.put("sid",fatherId);
                }
                paramMap.put("level",level);
                paramMap.put("recycle",DictionaryConst.ProductRecycle.NOT_STATUS);
                List<Integer> examineList = new ArrayList<Integer>();
                examineList.add(Integer.valueOf(DictionaryConst.ExameStatus.EXAME_PASS_STATUS));
                examineList.add(Integer.valueOf(DictionaryConst.ExameStatus.EXAME_WAIT_STATUS));
                paramMap.put("examineList",examineList);
                count = productClassModelMapper.count(paramMap);
                if (count > 0)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_LMYCZ, "类名已存在", "addClass");
                }
            }
            if (level > 0 || fatherId > 0)
            {
                ProductClassModel productClassModel = new ProductClassModel();
                productClassModel.setStore_id(vo.getStoreId());
                productClassModel.setCid(fatherId);
                productClassModel.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS);
                productClassModel = productClassModelMapper.selectOne(productClassModel);
                //判断上级是否存在
                if (productClassModel == null)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SJBCZ_001, "上级不存在", "addClass");
                }
                //当前级别只能是上级的id
                int levelTemp = level - 1;
                if (productClassModel.getLevel() != levelTemp)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SJHDQDJBF, "上级和当前等级不符!");
                }
                saveClass.setIs_display(DictionaryConst.WhetherMaven.WHETHER_OK);
            }
            else
            {
                level = 0;
                fatherId = 0;
            }
            saveClass.setLevel(level);
            saveClass.setSid(fatherId);
            int maxSort;
            //如果是修改了当前级别的等级则重新获取最大序号
            if (productClassOld == null || productClassOld.getLevel() != level)
            {
                maxSort = productClassModelMapper.getGoodsClassMaxSort(vo.getStoreId());
                //获取最新序号
                saveClass.setSort(maxSort);
            }
            if (!isUpdate)
            {
                saveClass.setLang_code(vo.getLang_code());
                saveClass.setCountry_num(vo.getCountry_num());
                saveClass.setAdd_date(new Date());
                count = productClassModelMapper.insertSelective(saveClass);

                //只有自营店的能直接修改 非自营店的在分类审核通过的时候加进去
                //修改所有的一级分类到未设置品牌的所属分类
                //查询所有的一级分类
                if (is_zy)
                {
                    String          productClassLevelTop = productClassModelMapper.getProductClassLevelTop(vo.getStoreId());
                    BrandClassModel brandClassModel      = new BrandClassModel();
                    brandClassModel.setStore_id(vo.getStoreId());
                    brandClassModel.setLang_code(vo.getLang_code());
                    brandClassModel.setNotset(1);
                    //如果有多条则删掉多余的 这记录是新增语种的时候在初始化脚本里面加的 init.sql
                    brandClassModel = brandClassModelMapper.selectOne(brandClassModel);
                    BrandClassModel brandClassModelNew = new BrandClassModel();
                    brandClassModelNew.setCategories(SplitUtils.DH + productClassLevelTop + SplitUtils.DH);
                    brandClassModelNew.setBrand_id(brandClassModel.getBrand_id());
                    brandClassModelNew.setNotset(1);
                    brandClassModelMapper.updateByPrimaryKeySelective(brandClassModelNew);
                }

                //操作日志
                publiceService.addAdminRecord(vo.getStoreId(), "添加了分类ID：" + saveClass.getCid(), AdminRecordModel.Type.ADD, vo.getAccessId());
                //添加跳转路径只添加一级
                if (level == 0)
                {
                    publicAdminService.addJumpPath(vo, saveClass.getCid() + "", className, JumpPathModel.JumpType.JUMP_TYPE0_GOODS_CLASS, JumpPathModel.JumpType.JUMP_TYPE_APP,
                            GloabConst.LaikeTuiUrl.JumpPath.GOODS_CLASS_APP, new String[]{saveClass.getCid() + ""}, mchid, saveClass.getLang_code());
                    publicAdminService.addJumpPath(vo, saveClass.getCid() + "", className, JumpPathModel.JumpType.JUMP_TYPE0_GOODS_CLASS, JumpPathModel.JumpType.JUMP_TYPE_PC,
                            GloabConst.LaikeTuiUrl.JumpPath.GOODS_CLASS_PC, new String[]{saveClass.getCid() + ""}, mchid, saveClass.getLang_code());
                }
            }
            else
            {
                saveClass.setCid(classId);

                //解决非自营商城编辑待审核分类状态变更问题
                if (!is_zy && Objects.nonNull(type))
                {
                    saveClass.setExamine(productClassOld.getExamine());
                    //2321 审核未通过的情况下，编辑之后改为待审核
                    if (Objects.equals(productClassOld.getExamine(), Integer.valueOf(DictionaryConst.ExameStatus.EXAME_NOT_PASS_STATUS)))
                    {
                        saveClass.setExamine(Integer.valueOf(DictionaryConst.ExameStatus.EXAME_WAIT_STATUS));
                    }
                }
                count = productClassModelMapper.updateByPrimaryKeySelective(saveClass);
                //操作日志
                publiceService.addAdminRecord(vo.getStoreId(), "修改了分类ID：" + saveClass.getCid() + "的信息", AdminRecordModel.Type.UPDATE, vo.getAccessId());
                //当前下面的所有类别降级/升级处理
                if (productClassOld.getLevel().equals(saveClass.getLevel()))
                {
                    this.classUpLevel(classId, productClassOld.getLevel(), level);
                }
            }

            if (count < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CZSB, "操作失败");
            }
        }
        catch (NullPointerException ne)
        {
            ne.printStackTrace();
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSBZQ, "参数不正确", "addClass");
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("添加当前类别 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "addClass");
        }
    }

    /**
     * 当前下面的所有类别降级/升级处理
     *
     * @param cid      -
     * @param levelOld - 之前的等级
     * @param level    - 升级/降级后的等级
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2022/2/22 18:09
     */
    private void classUpLevel(int cid, int levelOld, int level) throws LaiKeAPIException
    {
        try
        {
            //级差
            int levelDif = levelOld - level;
            if (levelDif == 0)
            {
                return;
            }
            logger.error("级别升级/降级 当前级别【{}】", level);
            ProductClassModel productClassOld = new ProductClassModel();
            productClassOld.setSid(cid);
            productClassOld.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS);
            List<ProductClassModel> productClassModelList = productClassModelMapper.select(productClassOld);
            for (ProductClassModel productClass : productClassModelList)
            {
                //降级/升级处理
                ProductClassModel productClassUpdate = new ProductClassModel();
                productClassUpdate.setCid(productClass.getCid());
                productClassUpdate.setLevel(level + 1);
                //如果超过5级则删除
                if (productClassUpdate.getLevel() > 4)
                {
                    productClassUpdate.setRecycle(DictionaryConst.ProductRecycle.RECOVERY);
                    //把下级也全部删除
                    productClassModelMapper.delClassBySid(productClass.getCid());
                }
                productClassModelMapper.updateByPrimaryKeySelective(productClassUpdate);
                this.classUpLevel(productClass.getCid(), productClass.getLevel(), productClassUpdate.getLevel());
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("级别升级/降级 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "classUpLevel");
        }
    }

    private void exportGoodsClassData(List<Map<String, Object>> goodsList, HttpServletResponse response) throws LaiKeAPIException
    {
        try
        {
            //表头
            String[] headerList = new String[]{"分类ID", "分类名称", "分类级别", "添加时间"};
            //对应字段
            String[] kayList = new String[]{"cid", "pname", "levelFormat", "add_date"};
            EasyPoiExcelUtil.excelExport("商品分类", headerList, kayList, goodsList, response);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("导出商品分类数据 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "exportGoodsClassData");
        }
    }
}

