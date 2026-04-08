package com.laiketui.admins.mch.services;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import com.laiketui.admins.api.mch.MchGoodsService;
import com.laiketui.common.api.*;
import com.laiketui.common.api.admin.PublicAdminService;
import com.laiketui.common.mapper.*;
import com.laiketui.common.utils.EasyPoiExcelUtil;
import com.laiketui.common.utils.HttpApiUtils;
import com.laiketui.common.utils.tool.cache.RedisDataTool;
import com.laiketui.common.utils.tool.data.GoodsDataUtils;
import com.laiketui.core.cache.RedisUtil;
import com.laiketui.core.common.SplitUtils;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.exception.LaiKeApiWarnException;
import com.laiketui.core.lktconst.DictionaryConst;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.core.utils.tool.*;
import com.laiketui.domain.config.ConfiGureModel;
import com.laiketui.domain.config.JumpPathModel;
import com.laiketui.domain.config.PluginsModel;
import com.laiketui.domain.dictionary.CountryModel;
import com.laiketui.domain.distribution.FreightModel;
import com.laiketui.domain.log.AdminRecordModel;
import com.laiketui.domain.mch.MchConfigModel;
import com.laiketui.domain.mch.PromiseShModel;
import com.laiketui.domain.message.MessageLoggingModal;
import com.laiketui.domain.product.*;
import com.laiketui.domain.user.User;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.Tool.ExcelParamVo;
import com.laiketui.domain.vo.admin.DefaultViewVo;
import com.laiketui.domain.vo.goods.AddStockVo;
import com.laiketui.domain.vo.goods.BrandClassVo;
import com.laiketui.domain.vo.goods.GoodsClassVo;
import com.laiketui.domain.vo.goods.StockInfoVo;
import com.laiketui.domain.vo.mch.UploadMerchandiseVo;
import com.laiketui.domain.vo.mch.pc.DelBrandVo;
import com.laiketui.root.common.BuilderIDTool;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.*;

/**
 * 商品管理
 *
 * @author Trick
 * @date 2021/5/31 15:56
 */
@Service
public class MchGoodsServiceImpl implements MchGoodsService
{
    private final Logger logger = LoggerFactory.getLogger(MchGoodsServiceImpl.class);

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private PublicGoodsService publicGoodsService;

    @Autowired
    private PublicStockService publicStockService;

    @Autowired
    private PublicAdminService publicAdminService;

    @Autowired
    private CountryModelMapper countryModelMapper;


    @Autowired
    private MchConfigModelMapper mchConfigModelMapper;
    @Autowired
    private SkuModelMapper       skuModelMapper;

    @Autowired
    private ConfiGureModelMapper confiGureModelMapper;

    @Autowired
    private ProductListModelMapper productListModelMapper;

    @Autowired
    private PublicMchService publicMchService;

    @Autowired
    private PublicGoodsClassService publicGoodsClassService;

    @Autowired
    private PublicBrandClassService publicBrandClassService;

    @Autowired
    private PubliceService publiceService;

    @Autowired
    private CustomerModelMapper customerModelMapper;

    @Autowired
    private ProLabelModelMapper proLabelModelMapper;

    @Autowired
    private FreightModelMapper freightModelMapper;

    @Autowired
    private ProductImgModelMapper productImgModelMapper;

    @Autowired
    private StockModelMapper stockModelMapper;

    @Autowired
    private MessageLoggingModalMapper messageLoggingModalMapper;

    @Autowired
    private ProductClassModelMapper productClassModelMapper;

    @Autowired
    private BrandClassModelMapper brandClassModelMapper;

    @Autowired
    private PluginsModelMapper pluginsModelMapper;

    @Autowired
    private HttpApiUtils httpApiUtils;

    @Autowired
    private PromiseShModelMapper promiseShModelMapper;

    private void isZxSwitch(int storeId, int mchId) throws LaiKeAPIException
    {
        try
        {
            //是否设置自选商品
            Integer        storeMchId     = customerModelMapper.getStoreMchId(storeId);
            MchConfigModel mchConfigModel = publicMchService.getMchConfig(storeId, storeMchId);
            if (mchConfigModel != null)
            {
                boolean  isZxSwitch = false;
                String[] settTypes  = mchConfigModel.getCommodity_setup().split(SplitUtils.DH);
                for (String type : settTypes)
                {
                    if ("2".equals(type))
                    {
                        isZxSwitch = true;
                        break;
                    }
                }
                if (!isZxSwitch)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SCWKZXSP, "商城未开自选商品", "index");
                }
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("自选开关判断 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "isZxSwitch");
        }
    }

    @Override
    public Map<String, Object> index(DefaultViewVo vo, HttpServletResponse response) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);
            if (user.getMchId() == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DPBCZ, "店铺不存在", "index");
            }
            int mchId = user.getMchId();
            Integer zyMchId = customerModelMapper.getStoreMchId(vo.getStoreId());
            //获取商品信息
            Map<String, Object> map = new HashMap<>(16);
            map.put("page", vo.getPageNo());
            map.put("pagesize", vo.getPageSize());
            if (vo.getCommodityType() == null)
            {
                vo.setCommodityType(0);
            }
            //默认实物商品
            int mchStatus = 1;
            if (vo.getCommodityType().equals(CommodityType.OPTIONAL))
            {
                //除了通过的
                mchStatus = 2;
            }
            else if (vo.getCommodityType().equals(CommodityType.TO_BE_REVIEWED))
            {
                //待审核
                mchStatus = 5;
            }
            else if (vo.getCommodityType().equals(CommodityType.AUDIT_FAILED))
            {
                //审核失败
                mchStatus = 3;
            }
            else if (vo.getCommodityType().equals(CommodityType.STORE_OPTIONAL))
            {
                //是否设置自选商品
                this.isZxSwitch(vo.getStoreId(), user.getMchId());
                //获取当前自己店铺已经选过的自选商品
                map.put("myMchId", customerModelMapper.getStoreMchId(vo.getStoreId()));
                map.put("isZx", true);
            }
            else if (vo.getCommodityType().equals(CommodityType.VIRTUAL_GOODS))
            {
                if (!zyMchId.equals(user.getMchId()))
                {
                    map.put("myMchId", mchId);
                    map.put("notZx", true);
                    map.put("status", DictionaryConst.GoodsStatus.NEW_GROUNDING);
                    //获取自营店店铺id
                    mchId = zyMchId;
                }
                else
                {
                    map.put("isZy",zyMchId);
                }
            }
            else
            {
                if (!zyMchId.equals(user.getMchId()))
                {
                    //默认列表,剔除已自选的商品
                    map.put("myMchId", customerModelMapper.getStoreMchId(vo.getStoreId()));
                    map.put("notZx", true);
                }
            }
            map.put("mch_status", mchStatus);
//            if (vo.getCommodityType() != null && vo.getCommodityType() >= 0 && vo.getCommodityType() < CommodityType.VIRTUAL_GOODS) {
//                map.put("commodity_type", vo.getCommodityType());
//            }
            if (vo.getStatus() != null)
            {
                map.put("status", vo.getStatus());
            }
            if (vo.getBrandId() != null)
            {
                map.put("brand_id", vo.getBrandId());
            }
            if (vo.getCid() != null)
            {
                map.put("product_class", vo.getCid());
            }
            if (!StringUtils.isEmpty(vo.getProductTitle()))
            {
                map.put("product_title", vo.getProductTitle());
            }
            if (!StringUtils.isEmpty(vo.getProductId()))
            {
                map.put("goodsId", vo.getProductId());
            }

            //销量升序降序
            if (StringUtils.isNotEmpty(vo.getIsItDescendingOrder()))
            {
                map.put("IsItDescendingOrder", vo.getIsItDescendingOrder());
            }

            if (vo.getClassnotset() == 1)
            {
                map.put("classnotset", 1);
            }

            if (vo.getBrandnotset() == 1)
            {
                map.put("brandnotset", 1);
            }

            map.put("lang_code", vo.getLang_code());
            map.put("country_num", vo.getCountry_num());

            resultMap = publicGoodsService.productList(vo.getStoreId(), user.getZhanghao(), mchId, GloabConst.LktConfig.LKT_CONFIG_TYPE_PC, map);
            List<Map<String, Object>> goodsList = DataUtils.cast(resultMap.get("list"));
            if (vo.getExportType().equals(1))
            {
                exportGoodsData(goodsList, response);
                return null;
            }
            if (goodsList != null)
            {
                for (Map<String, Object> goodsMap : goodsList)
                {
                    String              initial      = MapUtils.getString(goodsMap, "initial");
                    Map<String, Object> goodsInfoMap = DataUtils.cast(SerializePhpUtils.getUnserializeObj(initial, Map.class));
                    if (goodsInfoMap != null)
                    {
                        goodsMap.put("sj", new BigDecimal(MapUtils.getString(goodsInfoMap, "sj")));
                        goodsMap.put("cbj", new BigDecimal(MapUtils.getString(goodsInfoMap, "cbj")));
                    }

                    goodsMap.put("lang_name", publiceService.getLangName(MapUtils.getString(goodsMap, "lang_code")));
                    goodsMap.put("country_name", publiceService.getCountryName(MapUtils.getIntValue(goodsMap, "country_num")));

                }
            }
            //是否缴纳保证金
            boolean isPayment = publicMchService.judgeMchPromise(vo, user.getUser_id());
            //是否存在保证金审核
            boolean        examineStatus = false;
            PromiseShModel oldPromise    = new PromiseShModel();
            oldPromise.setMch_id(user.getMchId());
            oldPromise.setIs_pass(3);
            int i = promiseShModelMapper.selectCount(oldPromise);
            if (i > 0)
            {
                examineStatus = false;
            }
            else
            {
                examineStatus = true;
            }
            resultMap.put("isPromiseExamine", examineStatus);
            resultMap.put("is_Payment", isPayment);

        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("商品列表-pc店铺", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "index");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> consignmentPro(DefaultViewVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);
            if (user.getMchId() == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DPBCZ, "店铺不存在", "index");
            }
            int mchId = user.getMchId();
            //获取商品信息
            Map<String, Object> paramMap = new HashMap<>(16);
            paramMap.put("page", vo.getPageNo());
            paramMap.put("pageSize", vo.getPageSize());
            paramMap.put("storeId", vo.getStoreId());
            paramMap.put("date_sort", DataUtils.Sort.DESC.toString());
            if (vo.getCommodityType().equals(CommodityType.STORE_OPTIONAL))
            {
                paramMap.put("mch_id", mchId);
            }
            else if (vo.getCommodityType().equals(CommodityType.VIRTUAL_GOODS))
            {
                paramMap.put("mchId", mchId);
            }
            if (vo.getStatus() != null)
            {
                paramMap.put("status", vo.getStatus());
            }
            if (vo.getBrandId() != null)
            {
                paramMap.put("brand_id", vo.getBrandId());
            }
            if (vo.getCid() != null)
            {
                paramMap.put("product_class", vo.getCid());
            }
            if (!StringUtils.isEmpty(vo.getProductTitle()))
            {
                paramMap.put("product_title", vo.getProductTitle());
            }
            List<Map<String, Object>> mapList = new ArrayList<>();
            int                       i       = productListModelMapper.countConsignmentPro(paramMap);
            if (i > 0)
            {
                mapList = productListModelMapper.getConsignmentPro(paramMap);
                mapList.stream().forEach(map ->
                {
                    int                 goodsId    = Integer.parseInt(map.get("id").toString());
                    String              classIds   = map.get("product_class").toString();
                    int                 brandId    = Integer.parseInt(map.get("brand_id").toString());
                    String              initialStr = map.get("initial").toString();
                    Map<String, String> initialMap = DataUtils.cast(SerializePhpUtils.getUnserializeObj(initialStr, Map.class));
                    if (initialMap == null)
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SJCW, "数据错误");
                    }
                    //成本价
                    BigDecimal costPrice = BigDecimal.ZERO;
                    //售价
                    BigDecimal price = BigDecimal.ZERO;
                    //供货价
                    BigDecimal supplyPrice = BigDecimal.ZERO;
                    //单位
                    String unit = initialMap.get("unit");
                    //总平台的商品分类名称
                    String commodityClassification = "";
                    //总平台的商品品牌名称
                    String commodityBrand = "";
                    //店主分类
                    String shopkeepersClassification = "";
                    if (StringUtils.isNotEmpty(classIds))
                    {
                        //-394-395-396- 转换成数组需要 -1
                        String[]          classIdList = StringUtils.trim(classIds, SplitUtils.HG).split(SplitUtils.HG);
                        int               classId     = Integer.parseInt(classIdList[classIdList.length - 1]);
                        ProductClassModel p           = new ProductClassModel();
                        p.setStore_id(vo.getStoreId());
                        p.setCid(classId);
                        p = productClassModelMapper.selectOne(p);
                        if (p != null)
                        {
                            commodityClassification = p.getPname();
                        }
                        BrandClassModel b = new BrandClassModel();
                        b.setStore_id(vo.getStoreId());
                        b.setBrand_id(brandId);
                        b = brandClassModelMapper.selectOne(b);
                        if (b != null)
                        {
                            commodityBrand = b.getBrand_name();
                        }
                        map.put("class_name", commodityClassification);
                        map.put("brand_name", commodityBrand);
                        //查询库存信息
                        ConfiGureModel confiGureModel = new ConfiGureModel();
                        Integer        goodStockNum   = confiGureModelMapper.countConfigGureNum(goodsId);
                        map.put("num", goodStockNum);

                        //获取价格
                        confiGureModel = confiGureModelMapper.getProductMinPriceAndMaxYprice(goodsId);
                        if (confiGureModel != null)
                        {
                            unit = confiGureModel.getUnit();
                            costPrice = confiGureModel.getCostprice();
                            price = confiGureModel.getPrice();
                            supplyPrice = confiGureModel.getYprice();
                        }
                        map.put("costPrice", costPrice);
                        map.put("price", price);
                        map.put("supplyPrice", supplyPrice);
                        //获取商品主图
                        map.put("imgurl", publiceService.getImgPath(MapUtils.getString(map, "imgurl"), vo.getStoreId()));
                        map.put("unit", unit);

                        map.put("add_date", DateUtil.dateFormate(MapUtils.getString(map, "add_date"), GloabConst.TimePattern.YMDHMS));
                        if (MapUtils.getInteger(map, "volume") < 0)
                        {
                            map.put("volume", 0);
                        }
                    }
                });
            }
            resultMap.put("total", i);
            resultMap.put("list", mapList);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("代售可选列表-pc店铺", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "consignmentPro");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> getClassInfo(GoodsClassVo vo, HttpServletResponse response) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            return publicGoodsClassService.getClassInfo(vo, response);
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
    }

    @Transactional(rollbackFor = Exception.class, noRollbackFor = LaiKeApiWarnException.class)
    @Override
    public void addClass(MainVo vo, Integer classId, String className, String ename, String img, int level, int fatherId, Integer type) throws LaiKeAPIException
    {
        try
        {
            publicGoodsClassService.addClass(vo, classId, className, ename, img, level, fatherId, type);
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

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean classSortTop(MainVo vo, Integer classId) throws LaiKeAPIException
    {
        try
        {
            User              user             = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);
            ProductClassModel updateClassModel = new ProductClassModel();
            updateClassModel.setCid(classId);
            //获取最新序号
            int maxSort = productClassModelMapper.getGoodsClassMaxSort(vo.getStoreId());
            updateClassModel.setSort(maxSort);

            //置顶
            int count = productClassModelMapper.updateByPrimaryKeySelective(updateClassModel);
            //操作日志
            publiceService.addAdminRecord(vo.getStoreId(), "置顶了分类ID：" + classId, AdminRecordModel.Type.UPDATE, vo.getAccessId());
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
            logger.error("类别置顶 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "classSortTop");
        }
        return false;
    }

    @Override
    public Map<String, Object> getBrandInfo(BrandClassVo vo, HttpServletResponse response) throws LaiKeAPIException
    {
        try
        {
            return publicBrandClassService.getBrandInfo(vo, response);
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
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean addBrand(BrandClassVo vo) throws LaiKeAPIException
    {
        try
        {
            publicBrandClassService.addBrand(vo);
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
        return true;
    }

    @Override
    public Map<String, Object> classAuditList(MainVo vo, String condition, Integer status, String startTime, String endTime,Integer level) throws LaiKeAPIException
    {

        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);
            if (user.getMchId() == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DPBCZ, "店铺不存在", "index");
            }
            return publicGoodsClassService.getExamineClassInfo(vo, condition, status, startTime, endTime, user.getMchId(),level);
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
    public Map<String, Object> brandAuditList(MainVo vo, Integer id, String condition, Integer status, String startTime, String endTime) throws LaiKeAPIException
    {
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);
            if (user.getMchId() == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DPBCZ, "店铺不存在", "index");
            }
            return publicBrandClassService.getExamineBrandInfo(vo, id, condition, status, startTime, endTime, user.getMchId());
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
            Map<Integer, List<ProductClassModel>> resultDataMap = new HashMap<>(16);
            if (productClassModel != null)
            {
                //图片处理
                String imgUrl = publiceService.getImgPath(productClassModel.getImg(), vo.getStoreId());
                productClassModel.setImg(imgUrl);
                //递归找上级
                publicGoodsService.getClassLevelAllInfo(vo.getStoreId(), classId, resultDataMap);
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
            e.printStackTrace();
            logger.error("获取当前类别所有上级 异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getClassLevelAllInfo");
        }
        return resultMap;
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delBrand(DelBrandVo vo)
    {
        Integer brandId = vo.getBrandId();
        //获取店铺登录信息
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);
            if (Objects.isNull(user))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WZDYHXX, "未获取到用户信息！");
            }
            BrandClassModel brand = brandClassModelMapper.selectByPrimaryKey(brandId);
            if (Objects.isNull(brand))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_PPBCZ, "该品牌不存在");
            }
            if (!Objects.equals(user.getStore_id(), brand.getStore_id()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CZSB, "不得操作其他商家的品牌");
            }
            //查看品牌是否绑定了其他商品威
            if (brandClassModelMapper.brandIsDel(user.getStore_id(), brandId) > 0)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_GPPBDLSPWFSC, "该品牌绑定了商品,无法删除");
            }
            BrandClassModel brandClassOld = new BrandClassModel();
            brandClassOld.setBrand_id(brandId);
            brandClassOld.setRecycle(DictionaryConst.ProductRecycle.RECOVERY);
            brandClassModelMapper.updateByPrimaryKeySelective(brandClassOld);
        }
        catch (Exception e)
        {
            logger.error("品牌删除异常");
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delClass(MainVo vo, int classId)
    {
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);
            if (Objects.isNull(user))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WZDYHXX, "未获取到用户信息！");
            }
            ProductClassModel productClassModel = productClassModelMapper.selectByPrimaryKey(classId);
            if (Objects.isNull(productClassModel))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.DATA_NOT_EXIST, "数据不存在");
            }
            if (!Objects.equals(user.getStore_id(), productClassModel.getStore_id()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CZSB, "不得操作其他商家的分类");
            }
            if (!Objects.equals(productClassModel.getExamine(), 2))
            {
                throw new LaiKeAPIException("只有审核未通过分类才可删除");
            }
            productClassModel.setRecycle(DictionaryConst.ProductRecycle.RECOVERY);
            int count = productClassModelMapper.updateByPrimaryKeySelective(productClassModel);
            if (count < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SCSB, "删除失败", "delClass");
            }
        }
        catch (LaiKeAPIException e)
        {
            logger.error("待审核分类删除异常: {}", e.getMessage(), e);
            throw e;
        }
        catch (Exception e)
        {
            logger.error("待审核分类删除异常");
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常");
        }
    }

    @Override
    public Map<String, Object> getStockDetailInfo(StockInfoVo vo, Integer attrId, Integer pid, Integer type, HttpServletResponse response)
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User                user     = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);
            Integer             mchId    = user.getMchId();
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", vo.getStoreId());
            parmaMap.put("product_id", pid);
            parmaMap.put("mch_id", mchId);
            parmaMap.put("attribute_id", attrId);
            if (type != null)
            {
                if (type == 3)
                {
                    List<Integer> types = new ArrayList<>();
                    types.add(0);
                    types.add(1);
                    parmaMap.put("stockTypeList", types);
                }
                else if (type == 2)
                {
                    parmaMap.put("stockType", type);
                    parmaMap.put("group_attr_id", "group_attr_id");
                }
                else
                {
                    parmaMap.put("stockType", type);
                }
            }
            if (!StringUtils.isEmpty(vo.getMchName()))
            {
                parmaMap.put("mch_name", vo.getMchName());
            }
            if (!StringUtils.isEmpty(vo.getProductTitle()))
            {
                parmaMap.put("product_title_like", vo.getProductTitle());
            }
            if (!StringUtils.isEmpty(vo.getStartDate()))
            {
                parmaMap.put("startDate", vo.getStartDate());
            }
            if (!StringUtils.isEmpty(vo.getEndDate()))
            {
                parmaMap.put("endDate", vo.getEndDate());
            }
            //库存大于预警库存不显示
            parmaMap.put("ltNum","ltNum");
            //预售商品不显示
            parmaMap.put("is_presell","is_presell");
            parmaMap.put("lang_code", vo.getLang_code());
            int total = stockModelMapper.goodsStockInfoCountDynamic(parmaMap);
            parmaMap.put("pageStart", vo.getPageNo());
            parmaMap.put("pageEnd", vo.getPageSize());

            List<Map<String, Object>> list = stockModelMapper.getGoodsStorckInfoDynamic(parmaMap);

            //排序
            String sortCriteria = vo.getSortCriteria();
            String sortOrder    = vo.getSort();

            if (Objects.nonNull(sortCriteria) && Objects.nonNull(sortOrder))
            {
                Comparator<Map<String, Object>> comparator = getMapComparator(sortCriteria, sortOrder);
                list.sort(comparator);
            }
            data(vo.getStoreId(), list);

            if (vo.getExportType() == 1 && type != null)
            {
                //0.入库 1.出库 2.预警
                if (type == 0)
                {
                    exportStockAddData(list, response);
                }
                else if (type == 1)
                {
                    exportStockOutData(list, response);
                }
                else if (type == 2)
                {
                    exportStockWarningData(list, response);
                }
                return null;
            }


            resultMap.put("list", list);
            resultMap.put("total", total);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取商品详细库存信息 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getStockDetailInfo");
        }
        return resultMap;
    }

    /**
     * 排序
     * @param sortCriteria
     * @param sortOrder
     * @return
     */
    private Comparator<Map<String, Object>> getMapComparator(String sortCriteria, String sortOrder)
    {
        Comparator<Map<String, Object>> comparator = null;
        if (sortCriteria.equals("num"))
        {
            comparator = Comparator.comparing(o -> MapUtils.getIntValue(o, "num"));
        }
        else
        {
            comparator = Comparator.comparing(o -> MapUtils.getString(o, "upper_shelf_time"),Comparator.nullsLast(Comparator.naturalOrder()));
        }
        if (sortOrder.equals("desc"))
        {
            comparator = comparator.reversed();
        }
        return comparator;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addInventory(AddStockVo vo)
    {
        //[{"id":7800,"addNum":1,"pid":6033}]
        try
        {
            AddStockVo addStockVo = new AddStockVo();
            addStockVo.setId(vo.getId());
            addStockVo.setAddNum(vo.getAddNum());
            addStockVo.setPid(vo.getPid());
            List<AddStockVo> addStockVoList = Collections.singletonList(addStockVo);
            String           stock          = JSONObject.toJSONString(addStockVoList);
            addGoodsStock(vo.getAccessId(), stock, vo.getStoreId());
        }
        catch (Exception e)
        {
            logger.error("添加库存 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "addInventory");
        }


    }

    private void exportStockAddData(List<Map<String, Object>> list, HttpServletResponse response) throws LaiKeAPIException
    {
        try
        {
            //表头
            String[] headerList = new String[]{"商品名称", "商品售价", "商品规格", "商品状态", "商品总库存", "入库数量", "入库时间", "店铺"};
            //对应字段
            String[]     kayList = new String[]{"product_title", "price", "specifications", "statusName", "total_num", "flowing_num", "add_date", "name"};
            ExcelParamVo vo      = new ExcelParamVo();
            vo.setTitle("库存入库列表");
            vo.setHeaderList(headerList);
            vo.setValueList(kayList);
            vo.setList(list);
            vo.setResponse(response);
            vo.setNeedNo(true);
            EasyPoiExcelUtil.excelExport(vo);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("导出商品库存数据 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "exportStockAddData");
        }
    }

    //导出库存出库列表
    private void exportStockOutData(List<Map<String, Object>> list, HttpServletResponse response) throws LaiKeAPIException
    {
        try
        {
            //表头
            String[] headerList = new String[]{"商品名称", "商品售价", "商品规格", "商品状态", "剩余库存", "出库数量", "店铺", "出库时间"};
            //对应字段
            String[]     kayList = new String[]{"product_title", "price", "specifications", "statusName", "total_num", "flowing_num", "name", "add_date"};
            ExcelParamVo vo      = new ExcelParamVo();
            vo.setTitle("库存出库列表");
            vo.setHeaderList(headerList);
            vo.setValueList(kayList);
            vo.setList(list);
            vo.setResponse(response);
            vo.setNeedNo(true);
            EasyPoiExcelUtil.excelExport(vo);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("导出库存出库列表 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "exportStockOutData");
        }
    }

    /**
     * 数据处理
     *
     * @param storeId               -
     * @param outExcelStockInfoList -
     * @author Trick
     * @date 2021/1/4 13:08
     */
    private void data(int storeId, List<Map<String, Object>> outExcelStockInfoList)
    {
        for (Map<String, Object> map : outExcelStockInfoList)
        {
            //图片处理
            String imgUrl = map.get("imgurl").toString();
            imgUrl = publiceService.getImgPath(imgUrl, storeId);
            map.put("imgurl", imgUrl);
            //属性处理
            String attribute = map.get("attribute") + "";
            attribute = DataSerializeHelp.getAttributeStr(attribute);
            map.put("specifications", attribute);

            int status = Integer.parseInt(map.get("status").toString());
            map.put("statusName", GoodsDataUtils.getGoodsStatusStr(status));

            map.put("add_date", DateUtil.dateFormate(MapUtils.getString(map, "add_date"), GloabConst.TimePattern.YMDHMS));
            map.put("upper_shelf_time", DateUtil.dateFormate(MapUtils.getString(map, "upper_shelf_time"), GloabConst.TimePattern.YMDHMS));
        }
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

    private void exportGoodsData(List<Map<String, Object>> goodsList, HttpServletResponse response) throws LaiKeAPIException
    {
        try
        {
            //表头
            String[] headerList = new String[]{"商品编号", "商品标题", "商品分类", "库存", "商品状态", "销量", "发布时间", "商品品牌", "价格", "所属店铺", "上架时间"};
            //对应字段
            String[] kayList = new String[]{"id", "product_title", "pname", "num", "status_name", "volume", "add_date", "brand_name", "price", "name", "upper_shelf_time"};
            EasyPoiExcelUtil.excelExport("商品列表", headerList, kayList, goodsList, response);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("导出商品数据 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "exportGoodsData");
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

    @Override
    public Map<String, Object> getCommoditySetup(MainVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);
            //获取平台自营店店铺配置
            MchConfigModel mchConfigModel = new MchConfigModel();
            mchConfigModel.setStore_id(user.getStore_id());
            mchConfigModel.setMch_id(customerModelMapper.getStoreMchId(vo.getStoreId()));
            mchConfigModel = mchConfigModelMapper.selectOne(mchConfigModel);
            //商品设置 1.上传商品 2.自选商品
            List<String> goodsSetList = new ArrayList<>();
            if (mchConfigModel != null)
            {
                if (StringUtils.isNotEmpty(mchConfigModel.getCommodity_setup()))
                {
                    goodsSetList = DataUtils.convertToList(mchConfigModel.getCommodity_setup().split(","));
                }
            }

            //获取商品设置
            resultMap.put("commodity_setup", goodsSetList);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取店铺商品配置 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getCommoditySetup");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> getClass(MainVo vo, Integer classId, Integer brandId) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            Map<String, Object> resultClassMap = publicGoodsService.getClassifiedBrands(vo, classId, brandId);
            resultMap.put("list", resultClassMap);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取商品类别 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getClass");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> getGoodsLabel(MainVo vo, String name, Integer id) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", vo.getStoreId());
            parmaMap.put("id", id);
            parmaMap.put("name", name);
            parmaMap.put("add_time_sort", DataUtils.Sort.DESC.toString());
            parmaMap.put("pageStart", vo.getPageNo());
            parmaMap.put("pageEnd", vo.getPageSize());
            int                       total = proLabelModelMapper.countDynamic(parmaMap);
            List<Map<String, Object>> list  = proLabelModelMapper.selectDynamic(parmaMap);

            resultMap.put("list", list);
            resultMap.put("total", total);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取商品列表 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "index");
        }
        return resultMap;
    }


    @Override
    public Map<String, Object> getAttributeName(MainVo vo, String attributes) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", vo.getStoreId());
            parmaMap.put("name", attributes);
            parmaMap.put("type", DictionaryConst.SkuType.SKUTYPE_NAME);
            parmaMap.put("name_sort", DataUtils.Sort.DESC.toString());
            parmaMap.put("pageStart", vo.getPageNo());
            parmaMap.put("pageEnd", vo.getPageSize());

            resultMap.put("list", skuModelMapper.getAttributeDynamicAll(parmaMap));
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取属性名称 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getAttributeName");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> getAttributeValue(MainVo vo, String attributes) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            if (StringUtils.isNotEmpty(attributes))
            {
                String[] attributeIds = attributes.split(",");
                if (attributeIds.length > 0)
                {
                    Map<String, Object> parmaMap = new HashMap<>(16);
                    parmaMap.put("store_id", vo.getStoreId());
                    parmaMap.put("sidList", attributeIds);
                    parmaMap.put("type", DictionaryConst.SkuType.SKUTYPE_VALUE);
                    parmaMap.put("name_sort", DataUtils.Sort.DESC.toString());
                    parmaMap.put("pageStart", vo.getPageNo());
                    parmaMap.put("pageEnd", vo.getPageSize());

                    resultMap.put("list", skuModelMapper.getAttributeDynamicAll(parmaMap));
                    return resultMap;
                }
            }
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误");
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取属性值 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getAttributeValue");
        }
    }

    @Override
    public Map<String, Object> getAttrByGoodsId(MainVo vo, Integer goodsId) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            if (goodsId == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误");
            }
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", vo.getStoreId());
            parmaMap.put("goodsId", goodsId);
            parmaMap.put("add_date_sort", DataUtils.Sort.DESC.toString());
            /*parmaMap.put("pageStart", vo.getPageNo());
            parmaMap.put("pageEnd", vo.getPageSize());*/

            List<Map<String, Object>> attrList = confiGureModelMapper.selectDynamic(parmaMap);
            for (Map<String, Object> map : attrList)
            {
                //处理规格属性
                String attrStr = MapUtils.getString(map, "attribute");
                map.put("attr", GoodsDataUtils.getProductSku(attrStr));
            }

            resultMap.put("list", attrList);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取商品规格列表 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getAttributeValue");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> getAddPage(MainVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);
            if (user != null)
            {
                resultMap = publicGoodsService.addPage(vo.getStoreId(), user.getZhanghao(), user.getMchId(), GloabConst.LktConfig.LKT_CONFIG_TYPE_PC);
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("加载添加商品页面 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getAddPage");
        }
        return resultMap;
    }


    @Transactional(rollbackFor = Exception.class)
    @Override
    public Map<String, Object> addGoods(UploadMerchandiseVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);
            if (user != null)
            {
                publicGoodsService.addProduct(vo, user.getZhanghao(), user.getMchId(), GloabConst.LktConfig.LKT_CONFIG_TYPE_PC);
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("添加商品 异常:", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "addGoods");
        }
        return resultMap;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addZxGoods(MainVo vo, String goodsIds, Integer yunFeiId) throws LaiKeAPIException
    {
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);
            if (StringUtils.isEmpty(goodsIds))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误");
            }
            //2023/01/29讨论需求更改：自选商品改为代售商品 所有商品不需要重新选择模板id
            FreightModel freightModel = new FreightModel();
            freightModel.setId(yunFeiId);
            if (freightModelMapper.selectCount(freightModel) < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YFMBBCZ, "运费模板不存在");
            }
            //获取自营店
//            int mchId = customerModelMapper.getStoreMchId(vo.getStoreId());
//            this.isZxSwitch(vo.getStoreId(), user.getMchId());
            String[] goodsIdList = goodsIds.split(SplitUtils.DH);
            for (String goodsId : goodsIdList)
            {
                int goodsIdOld = Integer.parseInt(goodsId);
                //获取商品信息
                ProductListModel productListModel = productListModelMapper.selectByPrimaryKey(goodsId);
                if (productListModel == null)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SPBCZ, "商品不存在");
                }
                //复制商品到当前店铺-重置业务数据
                productListModel.setZixuan_id(productListModel.getId());
                productListModel.setId(null);
                productListModel.setProduct_number(BuilderIDTool.getSnowflakeId() + "");
                productListModel.setMch_id(user.getMchId());
                productListModel.setPublisher(user.getUser_name());
                productListModel.setVolume(0);
                productListModel.setReal_volume(0);
                productListModel.setSearch_num(0);
                productListModel.setAdd_date(new Date());
                productListModel.setUpper_shelf_time(new Date());
                productListModel.setSort(productListModelMapper.getGoodsMaxSortByMch(vo.getStoreId(), user.getId()));
                productListModel.setFreight(String.valueOf(yunFeiId));
                if (StringUtils.isNotEmpty(productListModel.getGongyingshang()))
                {
                    productListModel.setSupplier_superior(Integer.valueOf(goodsId));
                }
                int row = productListModelMapper.insertSelective(productListModel);
                if (row < 1)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SPTJSB, "商品添加失败");
                }
                //查询自选商品轮播图
                ProductImgModel productImgModel = new ProductImgModel();
                productImgModel.setProduct_id(goodsIdOld);
                List<ProductImgModel> productImgModelList = productImgModelMapper.select(productImgModel);
                for (ProductImgModel productImg : productImgModelList)
                {
                    //添加新得商品轮播图
                    productImg.setId(null);
                    productImg.setAdd_date(new Date());
                    productImg.setProduct_id(productListModel.getId());
                    row = productImgModelMapper.insertSelective(productImg);
                    if (row < 1)
                    {
                        logger.info("商品轮播图添加失败 参数:" + JSON.toJSONString(productImg));
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误");
                    }
                }
                //复制所有规格到当前店铺
                ConfiGureModel confiGureModel = new ConfiGureModel();
                confiGureModel.setPid(Integer.parseInt(goodsId));
                confiGureModel.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS.toString());
                List<ConfiGureModel> attrModelList = confiGureModelMapper.select(confiGureModel);
                if (attrModelList.size() == 0)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SPSJBWZ, "商品数据不完整");
                }
                for (ConfiGureModel attrSave : attrModelList)
                {
                    if (StringUtils.isNotEmpty(productListModel.getGongyingshang()))
                    {
                        attrSave.setSupplier_superior(attrSave.getId());
                    }
                    attrSave.setId(null);
                    attrSave.setAttribute_str("");
                    attrSave.setPid(productListModel.getId());
                    attrSave.setCtime(new Date());
                    row = confiGureModelMapper.insertSelective(attrSave);
                    if (row < 1)
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_GGTJSB, "规格添加失败");
                    }
                    //添加库存记录信息
                    String     content    = user.getUser_id() + "增加商品总库存:" + attrSave.getNum();
                    StockModel stockModel = new StockModel();
                    stockModel.setStore_id(vo.getStoreId());
                    stockModel.setProduct_id(productListModel.getId());
                    stockModel.setAttribute_id(attrSave.getId());
                    stockModel.setTotal_num(attrSave.getNum());
                    stockModel.setFlowing_num(attrSave.getNum());
                    stockModel.setType(StockModel.StockType.STOCKTYPE_WAREHOUSING);
                    stockModel.setContent(content);
                    stockModel.setAdd_date(new Date());
                    row = stockModelMapper.insertSelective(stockModel);
                    if (row < 1)
                    {
                        logger.info("库存入库记录失败 参数:" + JSON.toJSONString(stockModel));
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误");
                    }
                    //库存是否达到阀值
                    if (productListModel.getMin_inventory() >= attrSave.getNum())
                    {
                        //记录一条库存预警信息
                        content = "预警";
                        stockModel.setId(null);
                        stockModel.setType(StockModel.StockType.AGREEMENTTYPE_WAREHOUSING_WARNING);
                        stockModel.setContent(content);
                        stockModel.setAdd_date(new Date());
                        row = stockModelMapper.insertSelective(stockModel);
                        if (row < 1)
                        {
                            logger.info("库存预警记录失败 参数:" + JSON.toJSONString(stockModel));
                            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误");
                        }
                    }

                }
                publiceService.addAdminRecord(vo.getStoreId(), "添加了商品ID：" + goodsId + " 为代售商品", AdminRecordModel.Type.ADD, vo.getAccessId());
                //添加跳转路径
                publicAdminService.addJumpPath(vo, productListModel.getId() + "", productListModel.getProduct_title(), JumpPathModel.JumpType.JUMP_TYPE0_GOODS, JumpPathModel.JumpType.JUMP_TYPE_APP,
                        GloabConst.LaikeTuiUrl.JumpPath.GOODS_APP, new String[]{productListModel.getId() + ""}, user.getMchId(), productListModel.getLang_code());
                publicAdminService.addJumpPath(vo, productListModel.getId() + "", productListModel.getProduct_title(), JumpPathModel.JumpType.JUMP_TYPE0_GOODS, JumpPathModel.JumpType.JUMP_TYPE_PC,
                        GloabConst.LaikeTuiUrl.JumpPath.GOODS_PC, new String[]{productListModel.getId() + ""}, user.getMchId(), productListModel.getLang_code());
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("添加自选商品 异常:", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "addZxGoods");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void editSort(MainVo vo, Integer id, Integer sort) throws LaiKeAPIException
    {
        try
        {
            User             user           = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);
            ProductListModel productListOld = productListModelMapper.selectByPrimaryKey(id);
            if (productListOld == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SPBCZ, "商品不存在");
            }
            if (sort == null)
            {
                sort = 0;
            }
            ProductListModel productListUpdate = new ProductListModel();
            productListUpdate.setId(id);
            productListUpdate.setMch_sort(sort);
            int row = productListModelMapper.updateByPrimaryKeySelective(productListUpdate);
            if (row < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CZSB, "操作失败");
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("编辑商品序号 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "addGoods");
        }
    }


    @Override
    public Map<String, Object> getGoodsInfoById(MainVo vo, int goodsId, boolean isZx) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User user  = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);
            int  mchId = user.getMchId();
            if (isZx)
            {
                mchId = customerModelMapper.getStoreMchId(vo.getStoreId());
            }
            resultMap = publicGoodsService.editPage(vo.getStoreId(), user.getZhanghao(), mchId, goodsId, GloabConst.LktConfig.LKT_CONFIG_TYPE_PC);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取商品信息 异常:", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getGoodsInfoById");
        }
        return resultMap;
    }


    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean upperAndLowerShelves(MainVo vo, String goodsIds, Integer status) throws LaiKeAPIException
    {
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);
            if (user != null)
            {
                publicGoodsService.upperAndLowerShelves(vo.getStoreId(), goodsIds, user.getMchId(), status);
                String[] pidList = goodsIds.split(",");
                for (String goodsId : pidList)
                {
                    if (status == 0)
                    {
                        publiceService.addAdminRecord(vo.getStoreId(), "将商品ID：" + goodsId + " 进行了下架操作", AdminRecordModel.Type.UPDATE, vo.getAccessId());
                    }
                    else
                    {
                        publiceService.addAdminRecord(vo.getStoreId(), "将商品ID：" + goodsId + " 进行了上架操作", AdminRecordModel.Type.UPDATE, vo.getAccessId());
                    }
                }
                return true;
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("上下架商品 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "upperAndLowerShelves");
        }
        return false;
    }

    @Override
    public Map<String, Object> stockPage(MainVo vo, int goodsIds) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);
            //规格信息
            List<Map<String, List<Object>>> attrList      = new ArrayList<>();
            Map<String, List<Object>>       resultAttrMap = new HashMap<>(16);
            //规格明细
            List<List<Map<String, Object>>>        attrDetailList      = new ArrayList<>();
            Map<String, List<Map<String, Object>>> resultAttrDetailMap = new HashMap<>(16);

            //获取商品规格信息
            ConfiGureModel confiGureModel = new ConfiGureModel();
            confiGureModel.setPid(goodsIds);
            List<ConfiGureModel> configModelList = confiGureModelMapper.select(confiGureModel);
            for (ConfiGureModel confiGure : configModelList)
            {
                //a:1:{s:14:"颜色_LKT_880";s:14:"蓝色_LKT_881";}
                Map<String, Object> attrMap = DataUtils.cast(SerializePhpUtils.getUnserializeObj(confiGure.getAttribute(), Map.class));
                if (attrMap != null)
                {
                    for (String key : attrMap.keySet())
                    {
                        String value = attrMap.get(key).toString();
                        if (key.indexOf("_LKT") > 0)
                        {
                            key = key.substring(0, key.indexOf("_LKT"));
                        }
                        if (value.indexOf("_LKT") > 0)
                        {
                            value = value.substring(0, value.indexOf("_LKT"));
                        }
                        List<Map<String, Object>> valueDetailList = new ArrayList<>();
                        List<Object>              valueList       = new ArrayList<>();
                        if (resultAttrDetailMap.containsKey(key))
                        {
                            valueDetailList = resultAttrDetailMap.get(key);
                        }
                        if (resultAttrMap.containsKey(key))
                        {
                            valueList = resultAttrMap.get(key);
                        }
                        Map<String, Object> valueMap = new HashMap<>(16);
                        valueMap.put("name", key);
                        valueMap.put("value", value);
                        valueMap.put("addStockNum", 0);
                        valueMap.put("cid", confiGure.getId());
                        valueMap.put("stock", confiGure.getNum());
                        valueMap.put("stockWarn", confiGure.getMin_inventory());
                        valueDetailList.add(valueMap);
                        resultAttrDetailMap.put(key, valueDetailList);

                        valueList.add(value);
                        resultAttrMap.put(key, valueList);

                    }
                }
            }
            //组装数据
            for (String key : resultAttrMap.keySet())
            {
                Map<String, List<Object>> listMap = new HashMap<>(16);
                List<Object>              temp    = new ArrayList<>();
                temp.add(key);
                listMap.put("attrName", temp);
                listMap.put("attrValue", resultAttrMap.get(key));
                attrList.add(listMap);
            }
            for (String key : resultAttrDetailMap.keySet())
            {
                attrDetailList.add(resultAttrDetailMap.get(key));
            }

            resultMap.put("attr", attrList);
            resultMap.put("attrList", attrDetailList);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("添加库存页面数据 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "stockPage");
        }
        return resultMap;
    }


    @Transactional(rollbackFor = Exception.class)
    @Override
    public Map<String, Object> addStock(MainVo vo, String stock) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        addGoodsStock(vo.getAccessId(), stock, vo.getStoreId());
        return resultMap;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delGoodsById(MainVo vo, String goodsIds) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);
            //拼团商品不可删除
            PluginsModel groupConfig = pluginsModelMapper.getPluginInfo(DictionaryConst.Plugin.GOGROUP, vo.getStoreId());
            if (groupConfig != null && groupConfig.getStatus().equals(1))
            {
                Map<String, Object>     resultMapOrder = null;
                HashMap<String, Object> map            = new HashMap<>();
                map.put("vo", vo);
                map.put("goodsIds", goodsIds);
                map.put("mchId", user.getMchId());
                Map<String, Object> paramApiMap = JSON.parseObject(JSON.toJSONString(map));
                try
                {
                    resultMapOrder = httpApiUtils.executeHttpApi("plugin.group.http.getExistenceOfGoods", paramApiMap, MediaType.MULTIPART_FORM_DATA_VALUE);
                }
                catch (Exception e)
                {
                    logger.error("删除商品,拼团商品远程调用异常:", e);
                }
                if (resultMapOrder != null && !(Boolean) resultMapOrder.get("whether"))
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SPSCSB, "商品删除失败,商品加入拼团活动，对应拼团活动未结束");
                }
            }
            String[] pidList = goodsIds.split(SplitUtils.DH);
            for (String goodsId : pidList)
            {
                if (!publicGoodsService.delGoodsById(vo.getStoreId(), Integer.parseInt(goodsId), null))
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SPSCSB, "商品删除失败");
                }
                publiceService.addAdminRecord(vo.getStoreId(), "删除了商品ID：" + goodsId, AdminRecordModel.Type.DEL, vo.getAccessId());
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("根据商品id删除商品 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "delGoodsById");
        }
    }

    @Override
    public Map<String, Object> getGoodsExamineInfo(MainVo vo, Integer classId, Integer brandId, Integer examinStatus, String goodsName, Integer goodsId) throws LaiKeAPIException
    {
        Map<String, Object> resultMap;
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);

            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("page", vo.getPageNo());
            parmaMap.put("pagesize", vo.getPageSize());
            parmaMap.put("pageto", "");
            if (examinStatus == null)
            {
                examinStatus = 2;
            }
            parmaMap.put("mch_status", examinStatus);
            if (!StringUtils.isEmpty(goodsName))
            {
                parmaMap.put("product_title", goodsName);
            }
            if (!StringUtils.isEmpty(classId))
            {
                parmaMap.put("product_class", classId);
            }
            if (!StringUtils.isEmpty(brandId))
            {
                parmaMap.put("brand_id", brandId);
            }
            if (!StringUtils.isEmpty(goodsId))
            {
                parmaMap.put("goodsId", goodsId);
            }
            parmaMap.put("lang_code", vo.getLang_code());
            resultMap = publicGoodsService.productList(vo.getStoreId(), user.getZhanghao(), user.getMchId(), GloabConst.LktConfig.LKT_CONFIG_TYPE_PC, parmaMap);

        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取商品审核列表 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getGoodsExamineInfo");
        }
        return resultMap;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Map<String, Object> submitAudit(MainVo vo, String pIds) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);
            //校验店铺信息
            publicMchService.verificationMchExis(vo.getStoreId(), user.getUser_id(), user.getMchId());
            if (StringUtils.isEmpty(pIds))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QXZSP, "请选择商品");
            }
            String[] goodsIds = pIds.split(SplitUtils.DH);
            for (String pid : goodsIds)
            {
                //获取商品信息
                ProductListModel productListModel = new ProductListModel();
                productListModel.setId(Integer.parseInt(pid));
                productListModel.setStore_id(vo.getStoreId());
                productListModel.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS.toString());
                productListModel = productListModelMapper.selectOne(productListModel);
                if (productListModel == null)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SPBCZ, "商品不存在");
                }

                ProductListModel updateGoods = new ProductListModel();
                updateGoods.setId(productListModel.getId());
                if (DictionaryConst.GoodsMchExameStatus.EXAME_STOP_STATUS.toString().equals(productListModel.getMch_status()))
                {
                    //重新提交审核
                    updateGoods.setMch_status(DictionaryConst.GoodsMchExameStatus.EXAME_WAIT_STATUS.toString());

                    //通知后台消息
                    MessageLoggingModal messageLoggingSave = new MessageLoggingModal();
                    messageLoggingSave.setStore_id(vo.getStoreId());
                    messageLoggingSave.setMch_id(productListModel.getMch_id());
                    messageLoggingSave.setType(MessageLoggingModal.Type.TYPE_GOODS_GOODS_EXAMINE);
                    messageLoggingSave.setParameter(productListModel.getId() + "");
                    messageLoggingSave.setContent("商品id为:" + productListModel.getId() + "商品名称为: " + productListModel.getProduct_title() + "的商品需要管理员审核");
                    messageLoggingSave.setAdd_date(new Date());
                    messageLoggingSave.setTo_url(GoodsDataUtils.MCH_GOODS_URL);
                    messageLoggingModalMapper.insertSelective(messageLoggingSave);

                    publiceService.addAdminRecord(vo.getStoreId(), "提交了商品ID：" + pid + " 的商品审核", AdminRecordModel.Type.UPDATE, vo.getAccessId());
                }
                else if (DictionaryConst.GoodsMchExameStatus.EXAME_WAIT_STATUS.toString().equals(productListModel.getMch_status()))
                {
                    //撤销审核
                    updateGoods.setMch_status(DictionaryConst.GoodsMchExameStatus.EXAME_STOP_STATUS.toString());
                    publiceService.addAdminRecord(vo.getStoreId(), "撤销了商品ID：" + pid + " 的商品审核", AdminRecordModel.Type.UPDATE, vo.getAccessId());
                }
                else
                {
                    logger.info("该商品商品状态为：" + productListModel.getMch_status() + "无需修改");
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_ZTWBH, "审核完成,无法撤销审核");
                }
                int count = productListModelMapper.updateByPrimaryKeySelective(updateGoods);
                if (count < 1)
                {
                    logger.info("商品审核 提交/撤销失败  参数:" + JSON.toJSONString(updateGoods));
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "submitAudit");
                }
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("提交/撤销商品审核 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "submitAudit");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> uploadImgs(MainVo vo, MultipartFile image) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            if (image == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TPBNWK, "图片不能为空");
            }
            User                user  = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);
            List<MultipartFile> files = new ArrayList<>();
            files.add(image);
            List<String> imgUrls = publiceService.uploadImage(files, GloabConst.UploadConfigConst.IMG_UPLOAD_OSS, vo.getStoreType(), vo.getStoreId(), user.getMchId());
            resultMap.put("url", imgUrls);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("上传图片 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "uploadImgs");
        }
        return resultMap;
    }

    //导出库存预警列表
    private void exportStockWarningData(List<Map<String, Object>> list, HttpServletResponse response) throws LaiKeAPIException
    {
        try
        {
            //表头
            String[] headerList = new String[]{"商品名称", "商品售价", "商品规格", "商品状态", "商品总库存", "剩余库存", "店铺", "上架时间"};
            //对应字段
            String[]     kayList = new String[]{"product_title", "price", "specifications", "statusName", "total_num", "num", "name", "upper_shelf_time"};
            ExcelParamVo vo      = new ExcelParamVo();
            vo.setTitle("库存预警列表");
            vo.setHeaderList(headerList);
            vo.setValueList(kayList);
            vo.setList(list);
            vo.setResponse(response);
            vo.setNeedNo(true);
            EasyPoiExcelUtil.excelExport(vo);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("导出商品库存数据 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "exportStockData");
        }
    }

    /**
     * 添加库存公共方法
     *
     * @param accessId
     * @param stock
     * @param storeId
     */
    private void addGoodsStock(String accessId, String stock, Integer storeId)
    {
        try
        {
            User user = RedisDataTool.getRedisUserCache(accessId, redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);
            if (StringUtils.isEmpty(stock))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误");
            }
            List<AddStockVo> stockMapList;
            try
            {
                stockMapList = JSON.parseObject(stock, new TypeReference<List<AddStockVo>>()
                {
                });
            }
            catch (Exception e)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SJGSCW, "数据格式错误");
            }
            for (AddStockVo stockVo : stockMapList)
            {
                stockVo.setStoreId(storeId);
                publicStockService.addGoodsStock(stockVo, user.getZhanghao());

                publiceService.addAdminRecord(storeId, "添加了商品ID：" + stockVo.getPid() + ", " + stockVo.getAddNum() + " 个库存", AdminRecordModel.Type.UPDATE, accessId);
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("添加库存 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "addStock");
        }
    }
}

