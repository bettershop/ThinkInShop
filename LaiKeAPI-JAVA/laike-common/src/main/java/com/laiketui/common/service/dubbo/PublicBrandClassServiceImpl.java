package com.laiketui.common.service.dubbo;

import com.laiketui.common.api.PublicBrandClassService;
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
import com.laiketui.domain.log.AdminRecordModel;
import com.laiketui.domain.product.BrandClassModel;
import com.laiketui.domain.product.ProductClassModel;
import com.laiketui.domain.user.User;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.Tool.ExcelParamVo;
import com.laiketui.domain.vo.goods.BrandClassVo;
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
public class PublicBrandClassServiceImpl implements PublicBrandClassService
{

    private final Logger              logger = LoggerFactory.getLogger(PublicBrandClassServiceImpl.class);
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
    public Map<String, Object> getBrandInfo(BrandClassVo vo, HttpServletResponse response) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", vo.getStoreId());
            parmaMap.put("brand_name", vo.getBrandName());
            parmaMap.put("brand_id", vo.getBrandId());
            parmaMap.put("pageStart", vo.getPageNo());
            parmaMap.put("pageEnd", vo.getPageSize());
            parmaMap.put("lang_code", vo.getLang_code());
            parmaMap.put("examine", 1);
            List<Map<String, Object>> productClassModelList = brandClassModelMapper.getBrandClassInfo(parmaMap);
            int                       total                 = brandClassModelMapper.countBrandClassInfo(parmaMap);

            //数据处理
            for (Map<String, Object> brandClass : productClassModelList)
            {
                //图片处理
                String pidUrl = MapUtils.getString(brandClass, "brand_pic");
                pidUrl = publiceService.getImgPath(pidUrl, vo.getStoreId());
                brandClass.put("brand_pic", pidUrl);
                //获取分类
                String[]     cidList    = StringUtils.trim(MapUtils.getString(brandClass, "categories"), SplitUtils.DH).split(SplitUtils.DH);
                List<String> classNames = new ArrayList<>();
                for (String cid : cidList)
                {
                    if (StringUtils.isEmpty(cid))
                    {
                        continue;
                    }
                    ProductClassModel productClassModel = new ProductClassModel();
                    productClassModel.setCid(Integer.parseInt(cid));
                    productClassModel = productClassModelMapper.selectOne(productClassModel);
                    if (productClassModel != null)
                    {
                        classNames.add(productClassModel.getPname());
                    }
                }
                brandClass.put("categories", classNames);
                brandClass.put("categoriesName", StringUtils.stringImplode(classNames, SplitUtils.DH));
                brandClass.put("brand_time", DateUtil.dateFormate(MapUtils.getString(brandClass, "brand_time"), GloabConst.TimePattern.YMDHMS));

                brandClass.put("lang_name", publiceService.getLangName(MapUtils.getString(brandClass, "lang_code")));
                brandClass.put("country_name", publiceService.getCountryName(MapUtils.getInteger(brandClass, "country_num")));

            }
            if (vo.getExportType().equals(1))
            {
                exportBrandData(productClassModelList, response);
                return null;
            }
            resultMap.put("brandInfoList", productClassModelList);
            resultMap.put("total", total);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取品牌信息 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getBrandInfo");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> getExamineBrandInfo(MainVo vo, Integer id, String condition, Integer status, String startTime, String endTime, Integer mch_id) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            Map<String, Object> paramMap = new HashMap<>(16);
            paramMap.put("storeId", vo.getStoreId());
            paramMap.put("add_date_sort", DataUtils.Sort.DESC.toString());
            paramMap.put("pageStart", vo.getPageNo());
            paramMap.put("pageEnd", vo.getPageSize());
            if (!Objects.isNull(id))
            {
                paramMap.put("id", id);
            }
            if (StringUtils.isNotEmpty(condition))
            {
                paramMap.put("brand_name", condition);
            }
            if (StringUtils.isNotEmpty(mch_id))
            {
                paramMap.put("mch_id", mch_id);
            }
            //展示没有审核通过的
            List<String> statusList = new ArrayList<>();
            statusList.add(DictionaryConst.ExameStatus.EXAME_WAIT_STATUS);
            statusList.add(DictionaryConst.ExameStatus.EXAME_NOT_PASS_STATUS);
            paramMap.put("brandExamineList", statusList);
            if (!Objects.isNull(status))
            {
                paramMap.put("status", status);
                paramMap.put("examine",status);
            }
            if (StringUtils.isNotEmpty(startTime))
            {
                paramMap.put("startTime", startTime);
            }
            if (StringUtils.isNotEmpty(endTime))
            {
                paramMap.put("endTime", endTime);
            }
            paramMap.put("lang_code", vo.getLang_code());
            int                       i    = brandClassModelMapper.countBrandClassInfo(paramMap);
            List<Map<String, Object>> list = new ArrayList<>();
            if (i > 0)
            {
                list = brandClassModelMapper.getBrandClassInfo(paramMap);
                for (Map<String, Object> map : list)
                {
                    map.put("brand_time", DateUtil.dateFormate(MapUtils.getString(map, "brand_time"), GloabConst.TimePattern.YMDHMS));
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
                    map.put("brand_pic", publiceService.getImgPath(MapUtils.getString(map, "brand_pic"), vo.getStoreId()));
                    map.put("brand_image", publiceService.getImgPath(MapUtils.getString(map, "brand_image"), vo.getStoreId()));
                    //获取分类
                    String[]     cidList    = StringUtils.trim(MapUtils.getString(map, "categories"), ",").split(",");
                    List<String> classNames = new ArrayList<>();
                    for (String cid : cidList)
                    {
                        ProductClassModel productClassModel = new ProductClassModel();
                        productClassModel.setCid(Integer.parseInt(cid));
                        productClassModel = productClassModelMapper.selectOne(productClassModel);
                        if (productClassModel != null)
                        {
                            classNames.add(productClassModel.getPname());
                        }
                    }
                    map.put("categories", classNames);
                    map.put("categoriesName", StringUtils.stringImplode(classNames, SplitUtils.DH));

                    map.put("lang_name", publiceService.getLangName(MapUtils.getString(map, "lang_code")));
                    map.put("country_name", publiceService.getCountryName(MapUtils.getInteger(map, "country_num")));


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

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean addBrand(BrandClassVo vo) throws LaiKeAPIException
    {
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);
            //获取自营店id
            Integer storeMchId = customerModelMapper.getStoreMchId(vo.getStoreId());
            boolean is_zy      = false;
            if (user.getMchId().equals(storeMchId))
            {
                is_zy = true;
            }

            BrandClassModel brandClassOld = new BrandClassModel();
            //是否是修改
            boolean isUpdate = false;
            if (vo.getBrandId() != null)
            {
                isUpdate = true;
                brandClassOld.setStore_id(vo.getStoreId());
                brandClassOld.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS);
                brandClassOld.setBrand_id(vo.getBrandId());
                brandClassOld = brandClassModelMapper.selectOne(brandClassOld);
                if (brandClassOld == null)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_PPBCZ, "品牌不存在", "addBrand");
                }
            }
            BrandClassModel saveBrand = new BrandClassModel();
            saveBrand.setStore_id(vo.getStoreId());
            saveBrand.setBrand_name(vo.getBrandName());
            saveBrand.setLang_code(vo.getLang_code());
            saveBrand.setBrand_pic(ImgUploadUtils.getUrlImgByName(vo.getBrandLogo(), true));
            saveBrand.setCategories(vo.getBrandClass());
            saveBrand.setRemarks(vo.getRemarks());
            saveBrand.setCountry_num(vo.getCountry_num());
            saveBrand.setMch_id(user.getMchId());
            //如果不是自营店则需要等待审核，自营店无需审核
            if (!is_zy)
            {
                saveBrand.setExamine(Integer.valueOf(DictionaryConst.ExameStatus.EXAME_WAIT_STATUS));
            }
            else
            {
                saveBrand.setExamine(Integer.valueOf(DictionaryConst.ExameStatus.EXAME_PASS_STATUS));
            }

            if (StringUtils.isEmpty(vo.getBrandClass()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_PPLBBNWK, "品牌类别不能为空", "addBrand");
            }
            if (!isUpdate || !brandClassOld.getBrand_name().equals(vo.getBrandName().trim()))
            {
                if (StringUtils.isEmpty(saveBrand.getBrand_name()))
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_PPMCBNWK, "品牌名称不能为空", "addBrand");
                }
                //判断品牌名称是否存在
                BrandClassModel brandClassModel = new BrandClassModel();
                brandClassModel.setStore_id(vo.getStoreId());
                brandClassModel.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS);
                brandClassModel.setBrand_name(saveBrand.getBrand_name());
                brandClassModel.setLang_code(vo.getLang_code());
                int count = brandClassModelMapper.selectCount(brandClassModel);
                if (count > 0)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_PPMCYCZ, "品牌名称已存在", "addBrand");
                }

            }
            else if (brandClassOld.getBrand_name().equals(vo.getBrandName().trim()))
            {
                saveBrand.setBrand_name(null);
            }
            saveBrand.setCategories("," + vo.getBrandClass() + ",");

            if (isUpdate)
            {
                if (StringUtils.equals(brandClassOld.getBrand_pic(), vo.getBrandLogo()))
                {
                    saveBrand.setBrand_pic(null);
                }
                if (StringUtils.equals(brandClassOld.getBrand_pic(), vo.getBrandLogo()))
                {
                    saveBrand.setCategories(null);
                }
                if (StringUtils.equals(brandClassOld.getRemarks(), vo.getRemarks()))
                {
                    saveBrand.setRemarks(null);
                }
            }

            int count;

            saveBrand.setLang_code(vo.getLang_code());
            saveBrand.setCountry_num(vo.getCountry_num());

            if (isUpdate)
            {
                saveBrand.setBrand_id(vo.getBrandId());
                saveBrand.setStore_id(brandClassOld.getStore_id());
                count = brandClassModelMapper.updateByPrimaryKeySelective(saveBrand);
                //操作日志
                publiceService.addAdminRecord(vo.getStoreId(), "修改了品牌ID：" + vo.getBrandId() + "的信息", AdminRecordModel.Type.UPDATE, vo.getAccessId());

                //查询系统自动生成且无法删除的品牌id
                String brand = brandClassModelMapper.getBrand(vo.getStoreId(), DictionaryConst.ProductRecycle.NOT_STATUS);
                if (StringUtils.isEmpty(brand))
                {
                    publicGoodsService.builderDefaultClassBrand(vo.getStoreId());
                    throw new LaiKeApiWarnException(ErrorCode.BizErrorCode.ERROR_CODE_QSHZS, "请稍后再试");
                }

//                //根据品牌id和商城id 查询绑定了此品牌的商品id
//                List<Map<String, Object>> goodIds = brandClassModelMapper.getGoodsByBrand(vo.getStoreId(), vo.getBrandId());
//                //循环修改商品的品牌id
//                for (Map goods : goodIds)
//                {
//                    ProductListModel productListModel = new ProductListModel();
//                    productListModel.setBrand_id(Integer.parseInt(brand));
//                    productListModel.setId(MapUtils.getInteger(goods, "id"));
//                    productListModelMapper.updateByPrimaryKeySelective(productListModel);
//                    //判断商品的一级分类是否在默认品牌归属分类下
//                    productListModel = productListModelMapper.selectByPrimaryKey(productListModel.getId());
//                    String          topClassId   = StringUtils.trim(productListModel.getProduct_class(), SplitUtils.HG).split(SplitUtils.HG)[0];
//                    BrandClassModel defaultBrand = brandClassModelMapper.selectByPrimaryKey(brand);
//                    if (!defaultBrand.getCategories().contains(SplitUtils.DH + topClassId + SplitUtils.DH))
//                    {
//                        defaultBrand.setCategories(defaultBrand.getCategories() + topClassId + SplitUtils.DH);
//                        brandClassModelMapper.updateByPrimaryKeySelective(defaultBrand);
//                    }
//                }
            }
            else
            {
                saveBrand.setBrand_time(new Date());
                //获取最新序号
                int maxSort = brandClassModelMapper.getBrandClassMaxSort(vo.getStoreId());
                saveBrand.setSort(maxSort);
                count = brandClassModelMapper.insertSelective(saveBrand);
                //操作日志
                publiceService.addAdminRecord(vo.getStoreId(), "添加了品牌ID：" + saveBrand.getBrand_id(), AdminRecordModel.Type.ADD, vo.getAccessId());
            }
            if (count > 0)
            {
                return true;
            }
        }
        catch (NullPointerException ne)
        {
            ne.printStackTrace();
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSBZQ, "参数不正确", "addBrand");
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("添加品牌信息 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "addBrand");
        }
        return false;
    }

    //导出品牌
    private void exportBrandData(List<Map<String, Object>> list, HttpServletResponse response) throws LaiKeAPIException
    {
        try
        {
            //表头
            String[] headerList = new String[]{"ID", "品牌名称", "所属分类", "添加时间"};
            //对应字段
            String[]     kayList = new String[]{"brand_id", "brand_name", "categoriesName", "brand_time"};
            ExcelParamVo vo      = new ExcelParamVo();
            vo.setTitle("品牌列表");
            vo.setHeaderList(headerList);
            vo.setValueList(kayList);
            vo.setList(list);
            vo.setResponse(response);
            vo.setNeedNo(false);
            EasyPoiExcelUtil.excelExport(vo);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("导出[品牌]数据 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "exportBrandData");
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

