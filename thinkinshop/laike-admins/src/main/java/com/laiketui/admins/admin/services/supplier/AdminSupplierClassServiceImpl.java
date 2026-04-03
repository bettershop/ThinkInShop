package com.laiketui.admins.admin.services.supplier;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import com.laiketui.admins.api.admin.supplier.AdminSupplierClassService;
import com.laiketui.common.api.PublicGoodsService;
import com.laiketui.common.api.PubliceService;
import com.laiketui.common.mapper.BrandClassModelMapper;
import com.laiketui.common.mapper.MessageLoggingModalMapper;
import com.laiketui.common.mapper.ProductClassModelMapper;
import com.laiketui.common.mapper.SupplierProClassModelMapper;
import com.laiketui.common.utils.tool.cache.RedisDataTool;
import com.laiketui.core.cache.RedisUtil;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.exception.LaiKeApiWarnException;
import com.laiketui.core.lktconst.DictionaryConst;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.core.utils.tool.DataUtils;
import com.laiketui.core.utils.tool.DateUtil;
import com.laiketui.core.utils.tool.StringUtils;
import com.laiketui.domain.log.AdminRecordModel;
import com.laiketui.domain.mch.AdminModel;
import com.laiketui.domain.message.MessageLoggingModal;
import com.laiketui.domain.product.ProductClassModel;
import com.laiketui.domain.vo.MainVo;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 供应商商品分类管理
 *
 * @Author: sunH_
 * @Date: Create in 16:27 2022/9/15
 */
@Service
public class AdminSupplierClassServiceImpl implements AdminSupplierClassService
{

    private final Logger logger = LoggerFactory.getLogger(AdminSupplierClassServiceImpl.class);

    @Autowired
    private SupplierProClassModelMapper supplierProClassModelMapper;

    @Autowired
    private ProductClassModelMapper productClassModelMapper;

    @Autowired
    private PubliceService publiceService;

    @Autowired
    private BrandClassModelMapper brandClassModelMapper;

    @Autowired
    private PublicGoodsService publicGoodsService;

    @Autowired
    private RedisUtil redisUtil;

    /*@Override
    public Map<String, Object> auditList(MainVo vo, String condition, Integer status, String startTime, String endTime) throws LaiKeAPIException {
        Map<String, Object> resultMap = new HashMap<>(16);
        try {
            Map<String, Object> paramMap = new HashMap<>(16);
            paramMap.put("storeId", vo.getStoreId());
            paramMap.put("add_date_sort", DataUtils.Sort.DESC.toString());
            paramMap.put("page", vo.getPageNo());
            paramMap.put("pageSize", vo.getPageSize());
            if (StringUtils.isNotEmpty(condition)){
                paramMap.put("condition", condition);
            }
            if (!Objects.isNull(status)){
                paramMap.put("status", status);
            }
            if (StringUtils.isNotEmpty(startTime)){
                paramMap.put("startTime", startTime);
            }
            if (StringUtils.isNotEmpty(endTime)){
                paramMap.put("endTime", endTime);
            }
            int i = supplierProClassModelMapper.countCondition(paramMap);
            List<Map<String, Object>> list = new ArrayList<>();
            if (i > 0){
                list = supplierProClassModelMapper.selectCondition(paramMap);
                for (Map<String, Object> map : list){
                    map.put("add_date", DateUtil.dateFormate(MapUtils.getString(map, "add_date"), GloabConst.TimePattern.YMDHMS));
                    String examine = MapUtils.getString(map, "examine");
                    String examineDesc = "";
                    if (examine.equals(DictionaryConst.ExameStatus.EXAME_WAIT_STATUS)){
                        examineDesc = "待审核";
                    }else if (examine.equals(DictionaryConst.ExameStatus.EXAME_PASS_STATUS)){
                        examineDesc = "审核通过";
                    }else {
                        examineDesc = "审核不通过";
                    }
                    map.put("examineDesc", examineDesc);
                    map.put("img", publiceService.getImgPath(MapUtils.getString(map, "img"), vo.getStoreId()));
                    map.put("bg", publiceService.getImgPath(MapUtils.getString(map, "bg"), vo.getStoreId()));
                    ProductClassModel productClassModel = productClassModelMapper.selectByPrimaryKey(MapUtils.getInteger(map, "sid"));
                    if (!Objects.isNull(productClassModel)){
                        map.put("spname", productClassModel.getPname());
                    }
                    int level = MapUtils.getIntValue(map, "level") + 1;
                    String levelFormat = "%s级分类";
                    switch (level) {
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
                }
            }
            resultMap.put("total", i);
            resultMap.put("list", list);
        } catch (LaiKeAPIException l) {
            throw l;
        } catch (Exception e) {
            logger.error("审核供应商商品 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "examine");
        }

        return resultMap;
    }*/

    @Override
    public Map<String, Object> auditList(MainVo vo, String condition, Integer status, String startTime, String endTime) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            Map<String, Object> paramMap = new HashMap<>(16);
            paramMap.put("store_id", vo.getStoreId());
            paramMap.put("add_date_sort", DataUtils.Sort.DESC.toString());
            paramMap.put("recycle", "0");
            paramMap.put("operator", "pt");
            paramMap.put("pageStart", vo.getPageNo());
            paramMap.put("pageEnd", vo.getPageSize());
            if (StringUtils.isNotEmpty(condition))
            {
                paramMap.put("condition", condition);
            }
            if (!Objects.isNull(status))
            {
                paramMap.put("status", status);
            }
            if (StringUtils.isNotEmpty(vo.getLang_code()))
            {
                paramMap.put("lang_code", vo.getLang_code());
            }
            if (StringUtils.isNotEmpty(startTime))
            {
                paramMap.put("startTime", startTime);
            }
            if (StringUtils.isNotEmpty(endTime))
            {
                paramMap.put("endTime", endTime);
            }
            int i = productClassModelMapper.countDynamic(paramMap);
            List<Map<String, Object>> list = new ArrayList<>();
            if (i > 0)
            {
                list = productClassModelMapper.selectDynamic(paramMap);
                for (Map<String, Object> map : list)
                {
                    map.put("add_date", DateUtil.dateFormate(MapUtils.getString(map, "add_date"), GloabConst.TimePattern.YMDHMS));
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
                    map.put("img", publiceService.getImgPath(MapUtils.getString(map, "img"), vo.getStoreId()));
                    map.put("bg", publiceService.getImgPath(MapUtils.getString(map, "bg"), vo.getStoreId()));
                    map.put("lang_name", publiceService.getLangName(MapUtils.getString(map, "lang_code")));
                    map.put("country_name", publiceService.getCountryName(MapUtils.getInteger(map, "country_num")));
                    ProductClassModel productClassModel = productClassModelMapper.selectByPrimaryKey(MapUtils.getInteger(map, "sid"));
                    if (!Objects.isNull(productClassModel))
                    {
                        map.put("spname", productClassModel.getPname());
                    }
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
                }
            }
            resultMap.put("total", i);
            resultMap.put("list", list);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("审核供应商商品 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "examine");
        }

        return resultMap;
    }

    @Autowired
    private MessageLoggingModalMapper messageLoggingModalMapper;

    @Override
    @Transactional(rollbackFor = Exception.class, noRollbackFor = LaiKeApiWarnException.class)
    public void examine(MainVo vo, Integer id, Integer status, String remark) throws LaiKeAPIException
    {
        try
        {
            AdminModel adminModel = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            String     text       = "";
            String     event      = "";

            ProductClassModel productClassModel = productClassModelMapper.selectByPrimaryKey(id);
            if (productClassModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_MBBCZ, "商品分类信息不存在", "examine");
            }
            productClassModel.setExamine(status);
            if (status.toString().equals(DictionaryConst.ExameStatus.EXAME_NOT_PASS_STATUS))
            {
                if (StringUtils.isEmpty(remark))
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "备注不能为空", "examine");
                }
                productClassModel.setRemark(remark);
                text = "商品分类" + productClassModel.getPname() + "审核拒绝";
                event = "拒接了分类ID：" + productClassModel.getCid() + "，并且名称为：" + productClassModel.getPname() + "的审核";
            }
            else if (status.toString().equals(DictionaryConst.ExameStatus.EXAME_PASS_STATUS))
            {
                if (productClassModel.getSid() > 0)
                {
                    ProductClassModel sid = productClassModelMapper.selectByPrimaryKey(productClassModel.getSid());
                    if (sid == null || !sid.getExamine().toString().equals(DictionaryConst.ExameStatus.EXAME_PASS_STATUS))
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QXTGGFLDSJFL, "请先通过该分类的上级分类", "examine");
                    }
                }
                //判断类别名称是否存在
                ProductClassModel productClassName = new ProductClassModel();
                productClassName.setStore_id(vo.getStoreId());
                productClassName.setLevel(productClassModel.getLevel());
                productClassName.setPname(productClassModel.getPname());
                productClassName.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS);
                productClassName.setExamine(Integer.valueOf(DictionaryConst.ExameStatus.EXAME_PASS_STATUS));
                int i = productClassModelMapper.selectCount(productClassName);
                if (i > 0)
                {
                    productClassName.setRemark("分类名称重复，请勿重复添加");
                    productClassName.setCid(id);
                    productClassName.setExamine(Integer.valueOf(DictionaryConst.ExameStatus.EXAME_NOT_PASS_STATUS));
                    productClassModelMapper.updateByPrimaryKeySelective(productClassName);
                    //消息通知
                    MessageLoggingModal messageLoggingSave = new MessageLoggingModal();
                    messageLoggingSave.setStore_id(vo.getStoreId());
                    messageLoggingSave.setType(MessageLoggingModal.Type.TYPE_GOODS_BRAND);
                    messageLoggingSave.setParameter(productClassName.getCid() + "");
                    messageLoggingSave.setContent("分类名称已存在");
                    messageLoggingSave.setAdd_date(new Date());
                    if (StringUtils.isNotEmpty(productClassName.getMch_id()))
                    {
                        messageLoggingSave.setMch_id(productClassName.getMch_id());
                    }
                    else
                    {
                        messageLoggingSave.setSupplier_id(productClassName.getSupplier_id());
                    }
                    messageLoggingModalMapper.insertSelective(messageLoggingSave);
                    throw new LaiKeApiWarnException(ErrorCode.BizErrorCode.ERROR_CODE_XTJCGFLMCYCZ, "系统检测该分类名称已存在，已自动拒绝", "examine");
                }

                //以下这段的意思就是给所有的分类都绑上其他品牌  现在没有其他品牌了 所以下面这段干掉 先注释 后删除 以下代码
                //添加默认品牌
//                String          productClassLevelTop = productClassModelMapper.getProductClassLevelTop(vo.getStoreId());
//                BrandClassModel brandClassModel      = new BrandClassModel();
//                brandClassModel.setStore_id(vo.getStoreId());
//                brandClassModel.setIs_default(DictionaryConst.DefaultMaven.DEFAULT_OK);
//                brandClassModel = brandClassModelMapper.selectOne(brandClassModel);
//                if (brandClassModel == null)
//                {
//                    publicGoodsService.builderDefaultClassBrand(vo.getStoreId());
//                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLFM, "请稍后再试");
//                }
//                BrandClassModel brandClassModelNew = new BrandClassModel();
//                brandClassModelNew.setCategories(SplitUtils.DH + productClassLevelTop + SplitUtils.DH);
//                brandClassModelNew.setBrand_id(brandClassModel.getBrand_id());
//                brandClassModelMapper.updateByPrimaryKeySelective(brandClassModelNew);

                text = "商品分类" + productClassModel.getPname() + "审核通过";
                event = "通过了分类ID：" + productClassModel.getCid() + "，并且名称为：" + productClassModel.getPname() + "的审核";

            }
            productClassModelMapper.updateByPrimaryKeySelective(productClassModel);
            //添加操作日志
            publiceService.addAdminRecord(vo.getStoreId(), event, AdminRecordModel.Type.PASS_OR_REFUSE, vo.getAccessId());
            //消息通知u
            MessageLoggingModal messageLoggingSave = new MessageLoggingModal();
            messageLoggingSave.setStore_id(vo.getStoreId());
            messageLoggingSave.setType(MessageLoggingModal.Type.TYPE_GOODS_CLASS);
            messageLoggingSave.setParameter(productClassModel.getCid() + "");
            messageLoggingSave.setContent(text);
            messageLoggingSave.setAdd_date(new Date());
            if (StringUtils.isNotEmpty(productClassModel.getMch_id()))
            {
                messageLoggingSave.setMch_id(productClassModel.getMch_id());
            }
            else
            {
                messageLoggingSave.setSupplier_id(productClassModel.getSupplier_id());
            }
            messageLoggingModalMapper.insertSelective(messageLoggingSave);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("审核供应商商品 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "examine");
        }
    }

    @Override
    public Map<String, Object> getClassLevelTopAllInfo(MainVo vo, int classId) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            //获取当前类别信息
            ProductClassModel productClassModel = new ProductClassModel();
            productClassModel.setStore_id(vo.getStoreId());
            productClassModel.setCid(classId);
            productClassModel.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS);
            productClassModel = productClassModelMapper.selectOne(productClassModel);
            Map<Integer, List<Map<String, Object>>> resultDataMap = new HashMap<>(16);
            if (productClassModel != null)
            {
                //图片处理
                String imgUrl = publiceService.getImgPath(productClassModel.getImg(), vo.getStoreId());
                productClassModel.setImg(imgUrl);
                //递归找上级
                getClassLevelAllInfo(vo.getStoreId(), classId, resultDataMap, 0);
            }
            resultMap.put("classInfo", productClassModel);
            resultMap.put("levelInfoList", resultDataMap);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取当前类别所有上级 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getClassLevelAllInfo");
        }
        return resultMap;
    }

    /**
     * 递归找上级分类
     *
     * @param storeId
     * @param cid
     * @param resultMap
     * @throws LaiKeAPIException
     */
    public void getClassLevelAllInfo(int storeId, int cid, Map<Integer, List<Map<String, Object>>> resultMap, int i) throws LaiKeAPIException
    {
        try
        {
            List<Map<String, Object>> resultList = new ArrayList<>();
            //获取当前类别
            if (i != 0)
            {
                ProductClassModel productClassModel = new ProductClassModel();
                productClassModel.setCid(cid);
                productClassModel.setStore_id(storeId);
                productClassModel.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS);
                productClassModel = productClassModelMapper.selectOne(productClassModel);
                if (productClassModel != null)
                {
                    Map<String, Object> map = JSON.parseObject(JSONObject.toJSONString(productClassModel), new TypeReference<Map<String, Object>>()
                    {
                    });
                    resultList.add(map);
                    resultMap.put(productClassModel.getLevel(), resultList);
                    //获取上级类别
                    int sid = productClassModel.getSid();
                    //0代表最顶级
                    if (sid != 0)
                    {
                        this.getClassLevelAllInfo(storeId, sid, resultMap, i + 1);
                    }
                }
            }
            else
            {
                ProductClassModel productClassModel = new ProductClassModel();
                productClassModel.setCid(cid);
                productClassModel.setStore_id(storeId);
                productClassModel.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS);
                productClassModel = productClassModelMapper.selectOne(productClassModel);
                if (productClassModel != null)
                {
                    Map<String, Object> map = JSON.parseObject(JSONObject.toJSONString(productClassModel), new TypeReference<Map<String, Object>>()
                    {
                    });
                    resultList.add(map);
                    resultMap.put(productClassModel.getLevel(), resultList);
                    //获取上级类别
                    int sid = productClassModel.getSid();
                    //0代表最顶级
                    if (sid != 0)
                    {
                        this.getClassLevelAllInfo(storeId, sid, resultMap, i + 1);
                    }
                }
            }

        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("递归商品类别 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "strOption");
        }
    }
}
