package com.laiketui.admins.admin.services;

import com.laiketui.admins.api.admin.AdminBrandService;
import com.laiketui.common.api.PublicGoodsService;
import com.laiketui.common.api.PubliceService;
import com.laiketui.common.mapper.*;
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
import com.laiketui.core.utils.tool.DateUtil;
import com.laiketui.core.utils.tool.StringUtils;
import com.laiketui.domain.dictionary.CountryModel;
import com.laiketui.domain.log.AdminRecordModel;
import com.laiketui.domain.mch.AdminModel;
import com.laiketui.domain.product.BrandClassModel;
import com.laiketui.domain.product.ProductClassModel;
import com.laiketui.domain.supplier.SupplierBrandModel;
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
 * 品牌
 *
 * @author Trick
 * @date 2020/12/30 16:58
 */
@Service
public class AdminBrandServiceImpl implements AdminBrandService
{
    private final Logger logger = LoggerFactory.getLogger(AdminBrandServiceImpl.class);

    @Autowired
    private PubliceService publicService;

    @Override
    public Map<String, Object> getBrandInfo(BrandClassVo vo, HttpServletResponse response) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", vo.getStoreId());
            parmaMap.put("brand_name", vo.getBrandName());
            String langCode = vo.getLang_code();
            if (StringUtils.isNotEmpty(langCode))
            {
                parmaMap.put("lang_code", langCode);
            }

            //系统的整体默认或者用户右上角所选的语种，这种场景只适用于第一次进入功能界面的查询
//            String language = vo.getLanguage();
//            if(StringUtils.isEmpty(langCode) && StringUtils.isNotEmpty(language))
//            {
//                logger.info("默认语种:{}",language);
//                parmaMap.put("lang_code", language);
//            }
            parmaMap.put("country_num", vo.getCountry_num());
            parmaMap.put("brand_id", vo.getBrandId());
            parmaMap.put("pageStart", vo.getPageNo());
            parmaMap.put("pageEnd", vo.getPageSize());
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
                String[] cidList = StringUtils.trim(MapUtils.getString(brandClass, "categories", ""), SplitUtils.DH).split(SplitUtils.DH);
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
                brandClass.put("lang_name", publiceService.getLangName(MapUtils.getString(brandClass, "lang_code")));
                brandClass.put("country_name", publiceService.getCountryName(MapUtils.getInteger(brandClass, "country_num")));
                brandClass.put("categoriesName", StringUtils.stringImplode(classNames, SplitUtils.DH));
                brandClass.put("brand_time", DateUtil.dateFormate(MapUtils.getString(brandClass, "brand_time"), GloabConst.TimePattern.YMDHMS));
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

    @Override
    public List<CountryModel> getCountry(MainVo vo) throws LaiKeAPIException
    {
        List<CountryModel> countryModelList;
        try
        {
            CountryModel countryModel = new CountryModel();
            countryModel.setIs_show(1);
            countryModelList = countryModelMapper.select(countryModel);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("获取国家列表 异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getCountry");
        }
        return countryModelList;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean addBrand(BrandClassVo vo) throws LaiKeAPIException
    {
        try
        {
            AdminModel      adminModel    = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
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
            saveBrand.setBrand_pic(ImgUploadUtils.getUrlImgByName(vo.getBrandLogo(), true));
            saveBrand.setCategories(vo.getBrandClass());
            saveBrand.setRemarks(vo.getRemarks());
            saveBrand.setLang_code(vo.getLang_code());
            saveBrand.setCountry_num(vo.getCountry_num());
            //平台添加的无需审核
            saveBrand.setExamine(Integer.valueOf(DictionaryConst.ExameStatus.EXAME_PASS_STATUS));
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
                brandClassModel.setExamine(Integer.valueOf(DictionaryConst.ExameStatus.EXAME_PASS_STATUS));
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
            if (isUpdate)
            {
                saveBrand.setBrand_id(vo.getBrandId());
                saveBrand.setStore_id(brandClassOld.getStore_id());
                count = brandClassModelMapper.updateByPrimaryKeySelective(saveBrand);
                //操作日志
                publiceService.addAdminRecord(vo.getStoreId(), "修改了品牌ID：" + vo.getBrandId() + "的信息", AdminRecordModel.Type.UPDATE, vo.getAccessId());

                //查询系统自动生成且无法删除的品牌id
//                String brand = brandClassModelMapper.getBrand(vo.getStoreId(), DictionaryConst.ProductRecycle.NOT_STATUS);
//                if (StringUtils.isEmpty(brand))
//                {
//                    publicGoodsService.builderDefaultClassBrand(vo.getStoreId());
//                    throw new LaiKeApiWarnException(ErrorCode.BizErrorCode.ERROR_CODE_QSHZS, "请稍后再试");
//                }

                //根据品牌id和商城id 查询绑定了此品牌的商品id
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

    @Transactional(rollbackFor = Exception.class)
    @Override
    public int addBrand1(BrandClassVo vo) throws LaiKeAPIException
    {

        int bid = 0;//品牌id
        try
        {
            AdminModel      adminModel    = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
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
            saveBrand.setBrand_pic(ImgUploadUtils.getUrlImgByName(vo.getBrandLogo(), true));
            saveBrand.setCategories(vo.getBrandClass());
            saveBrand.setRemarks(vo.getRemarks());
            saveBrand.setCountry_num(vo.getCountry_num());
            //平台添加的无需审核
            saveBrand.setExamine(Integer.valueOf(DictionaryConst.ExameStatus.EXAME_PASS_STATUS));
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
                int count = brandClassModelMapper.selectCount(brandClassModel);
                if (count > 0)
                {
                    brandClassModel = brandClassModelMapper.selectOne(brandClassModel);
                    bid = brandClassModel.getBrand_id();
                    if (!brandClassModel.getCategories().contains(String.format(",%s,", vo.getBrandClass())))
                    {
                        brandClassModel.setCategories(brandClassModel.getCategories() + "," + vo.getBrandClass() + ",");
                        brandClassModelMapper.updateByPrimaryKeySelective(brandClassModel);
                    }
                    return bid;
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

            if (isUpdate)
            {
                saveBrand.setBrand_id(vo.getBrandId());
                saveBrand.setStore_id(brandClassOld.getStore_id());
                count = brandClassModelMapper.updateByPrimaryKeySelective(saveBrand);
                //操作日志
                publiceService.addAdminRecord(vo.getStoreId(), "修改了品牌ID：" + vo.getBrandId() + "的信息", AdminRecordModel.Type.UPDATE, vo.getAccessId());

//                //查询系统自动生成且无法删除的品牌id
//                String brand = brandClassModelMapper.getBrand(vo.getStoreId(), DictionaryConst.ProductRecycle.NOT_STATUS);
//                if (StringUtils.isEmpty(brand))
//                {
//                    publicGoodsService.builderDefaultClassBrand(vo.getStoreId());
//                    throw new LaiKeApiWarnException(ErrorCode.BizErrorCode.ERROR_CODE_QSHZS, "请稍后再试");
//                }

                //根据品牌id和商城id 查询绑定了此品牌的商品id
//                List<Map<String, Object>> goodIds = brandClassModelMapper.getGoodsByBrand(vo.getStoreId(), vo.getBrandId());
                //循环修改商品的品牌id
//                for (Map goods : goodIds)
//                {

//                    ProductListModel productListModel = new ProductListModel();
//                    productListModel.setBrand_id(vo.getBrandId());
//                    productListModel.setId(MapUtils.getInteger(goods, "id"));
//                    productListModelMapper.updateByPrimaryKeySelective(productListModel);

                    //判断商品的一级分类是否在默认品牌归属分类下
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
                bid = brandClassModelMapper.getLastInsertId();
                //操作日志
                publiceService.addAdminRecord(vo.getStoreId(), "添加了品牌ID：" + saveBrand.getBrand_id(), AdminRecordModel.Type.ADD, vo.getAccessId());
            }
            return bid;
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
    }

    @Override
    @Transactional(rollbackFor = Exception.class, noRollbackFor = LaiKeApiWarnException.class)
    public boolean delBrand(MainVo vo, int brandId) throws LaiKeAPIException
    {
        try
        {
            AdminModel      adminModel    = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            BrandClassModel brandClassOld = brandClassModelMapper.selectByPrimaryKey(brandId);
            if (brandClassOld == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SJBCZ, "数据不存在");
            }
            if (brandClassOld.getSort() == 0)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SJBCZ, "默认品牌无法删除");
            }
            /*if (StringUtils.isNotEmpty(brandClassOld.getCategories())) {
                //判断该品牌是否绑定了分类,绑定的分类不能删除
                String[] classIds = brandClassOld.getCategories().split(SplitUtils.DH);
                Map<String, Object> parmaMap = new HashMap<>(16);
                parmaMap.put("store_id", vo.getStoreId());
                parmaMap.put("recycle", DictionaryConst.ProductRecycle.NOT_STATUS);
                parmaMap.put("classIdList", classIds);
                int total = productClassModelMapper.countDynamic(parmaMap);
                if (total > 0) {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_GPPBDLFLWFSC, "该品牌绑定了分类,无法删除");
                }
            }*/
            //java版本优化，绑定商品的品牌需要可以进行删除
            //判断该品牌是否绑定了商品
//            if (brandClassModelMapper.brandIsDel(vo.getStoreId(), brandId) > 0) {
//                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_GPPBDLSPWFSC, "该品牌绑定了商品,无法删除");
//            }
            //查询系统自动生成且无法删除的品牌id
//            String brand = brandClassModelMapper.getBrand(vo.getStoreId(), DictionaryConst.ProductRecycle.NOT_STATUS);
//            if (StringUtils.isEmpty(brand))
//            {
//                publicGoodsService.builderDefaultClassBrand(vo.getStoreId());
//                throw new LaiKeApiWarnException(ErrorCode.BizErrorCode.ERROR_CODE_QSHZS, "请稍后再试");
//            }
            //根据品牌id和商城id 查询绑定了此品牌的商品id
            List<Map<String, Object>> goodIds = brandClassModelMapper.getGoodsByBrand(vo.getStoreId(), brandId);
            //循环修改商品的品牌id
            for (Map goods : goodIds)
            {
                //置空
                //下架
                productListModelMapper.updateBrandIdNullById(MapUtils.getInteger(goods, "id"));
                //判断商品的一级分类是否在默认品牌归属分类下
//                productListModel = productListModelMapper.selectByPrimaryKey(productListModel.getId());
//                String          topClassId   = StringUtils.trim(productListModel.getProduct_class(), SplitUtils.HG).split(SplitUtils.HG)[0];
//                BrandClassModel defaultBrand = brandClassModelMapper.selectByPrimaryKey(brand);
//                if (!defaultBrand.getCategories().contains(SplitUtils.DH + topClassId + SplitUtils.DH))
//                {
//                    defaultBrand.setCategories(defaultBrand.getCategories() + topClassId + SplitUtils.DH);
//                    brandClassModelMapper.updateByPrimaryKeySelective(defaultBrand);
//                }
            }

            BrandClassModel brandClassModel = new BrandClassModel();
            brandClassModel.setStore_id(vo.getStoreId());
            brandClassModel.setBrand_id(brandId);
            brandClassModel.setRecycle(DictionaryConst.ProductRecycle.RECOVERY);
            int count = brandClassModelMapper.updateByPrimaryKeySelective(brandClassModel);

            //同时删除供应商品牌
            SupplierBrandModel supplierBrandModel = new SupplierBrandModel();
            supplierBrandModel.setStore_id(vo.getStoreId());
            supplierBrandModel.setBrand_name(brandClassOld.getBrand_name());
            supplierBrandModel.setRecycle(DictionaryConst.WhetherMaven.WHETHER_NO);
            supplierBrandModel.setExamine(1);
            supplierBrandModel = supplierBrandModelMapper.selectOne(supplierBrandModel);
            if (supplierBrandModel != null)
            {
                supplierBrandModel.setRecycle(DictionaryConst.WhetherMaven.WHETHER_OK);
                supplierBrandModelMapper.updateByPrimaryKeySelective(supplierBrandModel);
            }
            //操作日志
            publiceService.addAdminRecord(vo.getStoreId(), "删除了品牌ID：" + brandId + "的信息", AdminRecordModel.Type.DEL, vo.getAccessId());
            if (count > 0)
            {
                return true;
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取国家列表 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getCountry");
        }
        return false;
    }

    @Override
    public boolean brandByTop(MainVo vo, int brandId) throws LaiKeAPIException
    {
        try
        {
            AdminModel      adminModel      = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            BrandClassModel brandClassModel = new BrandClassModel();
            brandClassModel.setStore_id(vo.getStoreId());
            brandClassModel.setBrand_id(brandId);
            //获取最新序号
            int maxSort = brandClassModelMapper.getBrandClassMaxSort(vo.getStoreId());
            brandClassModel.setSort(maxSort);
            int count = brandClassModelMapper.updateByPrimaryKeySelective(brandClassModel);
            publiceService.addAdminRecord(vo.getStoreId(), adminModel.getName(), "置顶了品牌ID：" + brandId, AdminRecordModel.Type.UPDATE, AdminRecordModel.Source.PC_PLATFORM);
            if (count > 0)
            {
                return true;
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("品牌置顶 异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "brandByTop");
        }
        return false;
    }


    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private ProductClassModelMapper productClassModelMapper;

    @Autowired
    private BrandClassModelMapper brandClassModelMapper;

    @Autowired
    private PubliceService publiceService;

    @Autowired
    private CountryModelMapper countryModelMapper;

    @Autowired
    private ProductListModelMapper productListModelMapper;

    @Autowired
    private PublicGoodsService publicGoodsService;

    @Autowired
    private SupplierBrandModelMapper supplierBrandModelMapper;
}

