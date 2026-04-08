package com.laiketui.apps.mch.services;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.laiketui.apps.api.mch.SupplierMchGoodsService;
import com.laiketui.common.api.PublicFreightService;
import com.laiketui.common.api.PublicGoodsService;
import com.laiketui.common.api.PublicStockService;
import com.laiketui.common.api.PubliceService;
import com.laiketui.common.api.admin.PublicAdminService;
import com.laiketui.common.mapper.*;
import com.laiketui.common.utils.EasyPoiExcelUtil;
import com.laiketui.common.utils.tool.ImgUploadUtils;
import com.laiketui.common.utils.tool.cache.RedisDataTool;
import com.laiketui.common.utils.tool.data.DataCheckTool;
import com.laiketui.core.cache.RedisUtil;
import com.laiketui.core.common.SplitUtils;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.DictionaryConst;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.core.utils.tool.DataUtils;
import com.laiketui.core.utils.tool.DateUtil;
import com.laiketui.core.utils.tool.SerializePhpUtils;
import com.laiketui.core.utils.tool.StringUtils;
import com.laiketui.domain.config.ConfiGureModel;
import com.laiketui.domain.config.JumpPathModel;
import com.laiketui.domain.config.SkuModel;
import com.laiketui.domain.dictionary.DictionaryListModel;
import com.laiketui.domain.distribution.FreightModel;
import com.laiketui.domain.log.AdminRecordModel;
import com.laiketui.domain.product.*;
import com.laiketui.domain.supplier.SupplierConfigModel;
import com.laiketui.domain.user.User;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.Tool.ExcelParamVo;
import com.laiketui.domain.vo.admin.DefaultViewVo;
import com.laiketui.domain.vo.goods.AddStockVo;
import com.laiketui.domain.vo.mch.UploadMerchandiseVo;
import com.laiketui.domain.vo.supplier.GoodsQueryVo;
import com.laiketui.root.common.BuilderIDTool;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.util.*;

/**
 * @Author: sunH_
 * @Date: Create in 17:17 2022/9/21
 */
@Service
public class SupplierMchGoodsServiceImpl implements SupplierMchGoodsService
{

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private SupplierProModelMapper supplierProModelMapper;

    @Autowired
    private PubliceService publiceService;

    @Autowired
    private ProductClassModelMapper productClassModelMapper;

    @Autowired
    private BrandClassModelMapper brandClassModelMapper;

    @Autowired
    private SupplierConfigureModelMapper supplierConfigureModelMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private SupplierModelMapper supplierModelMapper;

    @Autowired
    private SkuModelMapper skuModelMapper;

    @Autowired
    private PublicGoodsService publicGoodsService;

    @Autowired
    private StockModelMapper stockModelMapper;

    @Autowired
    private PublicAdminService publicAdminService;

    @Autowired
    private ProductImgModelMapper productImgModelMapper;

    @Autowired
    private PublicStockService publicStockService;

    @Autowired
    private FreightModelMapper freightModelMapper;

    @Autowired
    private ProductListModelMapper productListModelMapper;

    @Autowired
    private ConfiGureModelMapper confiGureModelMapper;


    @Autowired
    private DictionaryListModelMapper dictionaryListModelMapper;

    @Autowired
    private SupplierProClassModelMapper supplierProClassModelMapper;

    @Autowired
    private SupplierBrandModelMapper supplierBrandModelMapper;

    @Autowired
    private JumpPathModelMapper jumpPathModelMapper;

    @Autowired
    private PublicFreightService publicFreightService;

    @Autowired
    private MchModelMapper mchModelMapper;

    @Autowired
    private SupplierConfigModelMapper supplierConfigModelMapper;

    @Override
    public Map<String, Object> proList(GoodsQueryVo vo, HttpServletResponse response) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User                user     = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            Map<String, Object> paramMap = new HashMap<>(16);
            paramMap.put("storeId", vo.getStoreId());
            paramMap.put("add_date_sort", DataUtils.Sort.DESC.toString());
            paramMap.put("sort_sort", DataUtils.Sort.DESC.toString());
            paramMap.put("pageStart", vo.getPageNo());
            paramMap.put("pageEnd", vo.getPageSize());
            paramMap.put("mchId", user.getMchId());
            if (!Objects.isNull(vo.getGoodId()))
            {
                paramMap.put("goodsId", vo.getGoodId());
            }
            if (StringUtils.isNotEmpty(vo.getCondition()))
            {
                paramMap.put("condition", vo.getCondition());
            }
            if (StringUtils.isNotEmpty(vo.getProductTitle()))
            {
                paramMap.put("productTitle", vo.getProductTitle());
            }
            if (!Objects.isNull(vo.getCid()))
            {
                paramMap.put("product_class", vo.getCid());
            }
            if (!Objects.isNull(vo.getBrandId()))
            {
                paramMap.put("brand_id", vo.getBrandId());
            }
            if (!Objects.isNull(vo.getStatus()))
            {
                paramMap.put("status", vo.getStatus());
            }
            if (!Objects.isNull(vo.getMchStatus()))
            {
                paramMap.put("mch_status", vo.getMchStatus());
            }
            if (StringUtils.isNotEmpty(vo.getMchStatusList()))
            {
                List<String> mchStatusList = Arrays.asList(vo.getMchStatusList().split(SplitUtils.DH));
                paramMap.put("mchStatusList", mchStatusList);
            }
            if (StringUtils.isNotEmpty(vo.getSupplierName()))
            {
                paramMap.put("supplierName", vo.getSupplierName());
            }
            if (StringUtils.isNotEmpty(vo.getSupplierKey()))
            {
                paramMap.put("supplierKey", vo.getSupplierKey());
            }
            if (StringUtils.isNotEmpty(vo.getStartTime()))
            {
                paramMap.put("startTime", vo.getStartTime());
            }
            if (StringUtils.isNotEmpty(vo.getEndTime()))
            {
                paramMap.put("endTime", vo.getEndTime());
            }
            List<Map<String, Object>> list  = new ArrayList<>();
            int                       count = supplierProModelMapper.countCondition(paramMap);
            if (count > 0)
            {
                list = supplierProModelMapper.selectCondition(paramMap);
                for (Map<String, Object> map : list)
                {
                    int                 goodsId          = MapUtils.getInteger(map, "id");
                    int                 topSupplierProId = MapUtils.getInteger(map, "supplier_superior");
                    String              classIds         = MapUtils.getString(map, "product_class");
                    int                 brandId          = MapUtils.getInteger(map, "brand_id");
                    String              initialStr       = MapUtils.getString(map, "initial");
                    Map<String, String> initialMap       = DataUtils.cast(SerializePhpUtils.getUnserializeObj(initialStr, Map.class));
                    if (initialMap == null)
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SJCW, "数据错误");
                    }
                    BigDecimal price        = new BigDecimal(initialMap.get("sj"));
                    String     unit         = initialMap.get("unit");
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
                    }
                    //查询库存信息
                    ConfiGureModel confiGureModel = new ConfiGureModel();
                    Integer        goodStockNum   = confiGureModelMapper.countConfigGureNum(topSupplierProId);
                    map.put("num", goodStockNum);
                    //获取价格
                    confiGureModel = confiGureModelMapper.getProductMinPriceAndMaxYprice(goodsId);
                    if (confiGureModel != null)
                    {
                        unit = confiGureModel.getUnit();
                        price = confiGureModel.getPrice();
                    }
                    ProductListModel productListModel = productListModelMapper.selectByPrimaryKey(topSupplierProId);
                    String initial = productListModel.getInitial();
                    Map<String, String> stringMap = DataUtils.cast(SerializePhpUtils.getUnserializeObj(initial, Map.class));

                    //获取商品主图
                    map.put("imgurl", publiceService.getImgPath(MapUtils.getString(map, "imgurl"), vo.getStoreId()));
                    map.put("unit", unit);
                    map.put("supplyPrice", stringMap.get("yj"));
                    map.put("price", price);
                    map.put("add_date", DateUtil.dateFormate(MapUtils.getString(map, "add_date"), GloabConst.TimePattern.YMDHMS));
                    if (MapUtils.getInteger(map, "volume") < 0)
                    {
                        map.put("volume", 0);
                    }

                    map.put("volume", (MapUtils.getInteger(map, "volume") + (MapUtils.getInteger(map, "real_volume"))));
                    String upperShelfTime = MapUtils.getString(map, "upper_shelf_time");
                    if (StringUtils.isNotEmpty(upperShelfTime))
                    {
                        map.put("upper_shelf_time", DateUtil.dateFormate(MapUtils.getString(map, "upper_shelf_time"), GloabConst.TimePattern.YMDHMS));
                    }
                    Integer status = MapUtils.getInteger(map, "status");
                    if (status.equals(DictionaryConst.GoodsStatus.NOT_GROUNDING))
                    {
                        map.put("statusDesc", "待上架");
                    }
                    else if (status.equals(DictionaryConst.GoodsStatus.NEW_GROUNDING))
                    {
                        map.put("statusDesc", "已上架");
                    }
                    else if (status.equals(DictionaryConst.GoodsStatus.OFFLINE_GROUNDING))
                    {
                        map.put("statusDesc", "已下架");
                    }
                }
            }
            if (vo.getExportType().equals(1))
            {
                exportGoodsData(list, response);
                return null;
            }
            resultMap.put("total", count);
            resultMap.put("list", list);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("商品列表 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "logout");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> consignmentPro(DefaultViewVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            if (user.getMchId() == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DPBCZ, "店铺不存在", "index");
            }
            int mchId = user.getMchId();
            //获取商品信息
            Map<String, Object> paramMap = new HashMap<>(16);
            //加入分页后报错
            paramMap.put("page", new Integer(vo.getPageNo()));
            paramMap.put("pageSize", new Integer(vo.getPageSize()));
//
            paramMap.put("storeId", vo.getStoreId());
            paramMap.put("date_sort", DataUtils.Sort.DESC.toString());
            paramMap.put("mchId", mchId);
            if (StringUtils.isNotEmpty(vo.getCondition()))
            {
                paramMap.put("condition", vo.getCondition());
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
    public Map<String, Object> getGoodsInfoById(MainVo vo, int goodId) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            //商品运费信息
            Map<String, Object> goodsFreightMap = new HashMap<>(16);
            //商品图片信息(封面图集合)
            List<String> productShowImgList = new ArrayList<>();
            //商品品牌信息 全部
            List<Map<String, Object>> productBrandList;
            //商品分类
            List<ProductClassModel> productClassList = new ArrayList<>();
            //插件集信息 Plugin_arr
            Map<String, Object> pluginArr        = new HashMap<>(16);
            ProductListModel    productListModel = new ProductListModel();
            productListModel.setStore_id(vo.getStoreId());
            productListModel.setId(goodId);
            productListModel = productListModelMapper.selectOne(productListModel);
            if (productListModel != null)
            {
                //获取图片信息
                String imgUrl       = productListModel.getImgurl();
                String converMapUrl = productListModel.getCover_map();
                imgUrl = publiceService.getImgPath(imgUrl, vo.getStoreId());
                productListModel.setImgurl(imgUrl);
                converMapUrl = publiceService.getImgPath(converMapUrl, vo.getStoreId());
                productListModel.setCover_map(converMapUrl);
            }
            else
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SPBCZ, "商品不存在");
            }
            //获取商品封面图
            ProductImgModel productImgModel = new ProductImgModel();
            productImgModel.setProduct_id(goodId);
            List<Map<String, Object>> productImgModelList = productImgModelMapper.getProductImgInfoByPid(productImgModel);
            if (productImgModelList != null)
            {
                for (int i = 0; i < productImgModelList.size(); i++)
                {
                    Map<String, Object> map        = productImgModelList.get(i);
                    String              productUrl = map.get("product_url").toString();
                    productUrl = publiceService.getImgPath(productUrl, vo.getStoreId());
                    productShowImgList.add(productUrl);
                }
            }
            //商品分类+品牌处理
            String   res     = productListModel.getProduct_class();
            String[] resList = res.split(SplitUtils.HG);
            //商品所属分类顶级
            int classIdTop = Integer.parseInt(resList[1]);
            int classNum   = resList.length - 1;
            //商品所属分类
            int classId = Integer.parseInt(resList[classNum]);
            for (String id : resList)
            {
                if (StringUtils.isEmpty(id))
                {
                    continue;
                }
                ProductClassModel productClass = new ProductClassModel();
                productClass.setCid(Integer.parseInt(id));
                productClass = productClassModelMapper.selectOne(productClass);
                productClassList.add(productClass);
            }
            //获取当前品牌名称
            String          brandName       = null;
            BrandClassModel brandClassModel = new BrandClassModel();
            brandClassModel.setBrand_id(productListModel.getBrand_id());
            brandClassModel = brandClassModelMapper.selectOne(brandClassModel);
            if (brandClassModel != null)
            {
                brandName = brandClassModel.getBrand_name();
            }
            //获取顶级牌信息
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", vo.getStoreId());
            parmaMap.put("status", BrandClassModel.Status.OPEN);
            parmaMap.put("categories", classIdTop);
            productBrandList = brandClassModelMapper.getBrandClassDynamic(parmaMap);
            //获取当前商品运费信息
            String       fname        = "";
            FreightModel goodsFreight = new FreightModel();
            goodsFreight.setId(Integer.parseInt(productListModel.getFreight()));
            goodsFreight = freightModelMapper.selectOne(goodsFreight);
            if (goodsFreight != null)
            {
                fname = goodsFreight.getName();
            }
            goodsFreightMap.put("id", productListModel.getFreight());
            goodsFreightMap.put("name", fname);

            //规格值集
            List<Map<String, Object>> strArrList      = new ArrayList<>();
            List<Map<String, Object>> checkedAttrList = new ArrayList<>();
            List<Map<String, Object>> attrGroupList   = new ArrayList<>();

            ConfiGureModel confiGureModel = new ConfiGureModel();
            confiGureModel.setPid(goodId);
            confiGureModel.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS.toString());
            List<ConfiGureModel> confiGureModelList = confiGureModelMapper.select(confiGureModel);
            if (confiGureModelList != null && confiGureModelList.size() > 0)
            {
                //规格处理
                String attribute = confiGureModelList.get(0).getAttribute();
                if (!StringUtils.isEmpty(attribute))
                {
                    Map<String, Object> attributeMap = DataUtils.cast(SerializePhpUtils.getUnserializeObj(attribute, Map.class));
                    if (attributeMap != null)
                    {
                        for (String key : attributeMap.keySet())
                        {
                            Map<String, Object> dataMap        = new HashMap<>(16);
                            String              attribyteKey   = key;
                            String              attribyteValue = attributeMap.get(key).toString();
                            int                 index          = attribyteKey.indexOf("_LKT_");
                            if (index > 0)
                            {
                                //属性名称
                                attribyteKey = attribyteKey.substring(0, attribyteKey.indexOf("_LKT_"));
                                attribyteValue = attribyteValue.substring(0, attribyteValue.indexOf("_LKT"));
                            }
                            dataMap.put("attr_group_name", attribyteKey);
                            //默认规格不展示不回显
                            if (attribyteKey.equals("默认") && attribyteValue.equals("默认"))
                            {
                                continue;
                            }
                            attrGroupList.add(dataMap);
                        }
                    }
                    //属性名称集合,去重
                    List<String> tempList = new ArrayList<>();
                    for (ConfiGureModel confiGure : confiGureModelList)
                    {
                        //当前属性信息
                        List<Map<String, Object>> attrLists       = new ArrayList<>();
                        String                    attributeStr    = confiGure.getAttribute();
                        Map<String, Object>       attributeStrMap = DataUtils.cast(SerializePhpUtils.getUnserializeObj(attributeStr, Map.class));
                        if (attributeStrMap != null)
                        {
                            for (String key : attributeStrMap.keySet())
                            {
                                String attributeKey   = key;
                                String attribyteValue = attributeStrMap.get(key).toString();
                                int    index          = attributeKey.indexOf("_LKT_");
                                if (index > 0)
                                {
                                    //获取id (尺码_LKT_8) 左边为名称 最后一个是id
                                    int                 keyId       = attributeKey.lastIndexOf(SplitUtils.HG) + 1;
                                    int                 valueId     = attribyteValue.indexOf(SplitUtils.UNDERLIEN) + 1;
                                    String              keyIdTemp   = attributeKey.substring(keyId);
                                    String              valueIdTemp = attribyteValue.substring(0, valueId);
                                    Map<String, Object> dataMap     = new HashMap<>(16);
                                    dataMap.put("id0", keyIdTemp);
                                    dataMap.put("id1", valueIdTemp);
                                    strArrList.add(dataMap);
                                    //获取名称
                                    attributeKey = attributeKey.substring(0, attributeKey.indexOf("_LKT"));
                                    attribyteValue = attribyteValue.substring(0, attribyteValue.indexOf("_LKT"));
                                }
                                //默认规格不展示不回显
                                if (attributeKey.equals("默认") && attribyteValue.equals("默认"))
                                {
                                    continue;
                                }
                                for (Map<String, Object> map : attrGroupList)
                                {
                                    //规格集 (attr_name:value,status:false)
                                    List<Map<String, Object>> attrList = new ArrayList<>();
                                    if (map.containsKey("attr_list"))
                                    {
                                        attrList = DataUtils.cast(map.get("attr_list"));
                                    }
                                    if (attributeKey.equals(map.get("attr_group_name").toString()))
                                    {
                                        //判断当前规格值是否存在数组中，不存在则添加
                                        if (!tempList.contains(attribyteValue))
                                        {
                                            Map<String, Object> attrListMap = new HashMap<>(16);
                                            boolean             isdsj       = false;
                                            attrListMap.put("attr_name", attribyteValue);
                                            //商品是否待上架
                                            if (DictionaryConst.GoodsStatus.NOT_GROUNDING.toString().equals(productListModel.getStatus()))
                                            {
                                                isdsj = true;
                                            }
                                            attrListMap.put("status", isdsj);
                                            attrList.add(attrListMap);
                                            map.put("attr_list", attrList);
                                            tempList.add(attribyteValue);
                                        }
                                    }
                                }
                                Map<String, Object> attrListsMap = new HashMap<>(16);
                                attrListsMap.put("attr_id", "");
                                attrListsMap.put("attr_group_name", attributeKey);
                                attrListsMap.put("attr_name", attribyteValue);
                                attrLists.add(attrListsMap);
                            }
                        }
                        //默认规格不返回
                        if (attrLists.size() == 0)
                        {
                            continue;
                        }
                        Map<String, Object> checkedAttrListMap = new HashMap<>(16);
                        checkedAttrListMap.put("attr_list", attrLists);
                        checkedAttrListMap.put("cbj", confiGure.getCostprice());
                        checkedAttrListMap.put("yj", confiGure.getYprice());
                        checkedAttrListMap.put("sj", confiGure.getPrice());
                        checkedAttrListMap.put("kucun", confiGure.getNum());
                        checkedAttrListMap.put("msrp", confiGure.getMsrp());
                        checkedAttrListMap.put("unit", confiGure.getUnit());
                        checkedAttrListMap.put("bar_code", confiGure.getBar_code());
                        checkedAttrListMap.put("img", publiceService.getImgPath(confiGure.getImg(), vo.getStoreId()));
                        checkedAttrListMap.put("cid", confiGure.getId());
                        checkedAttrList.add(checkedAttrListMap);
                    }
                }
            }

            //产品属性处理
            List<Map<String, Object>> sTypeList = new ArrayList<>();
            String                    sType     = productListModel.getS_type();
            List<String>              arr       = new ArrayList<>();
            if (StringUtils.isNotEmpty(sType))
            {
                arr = Arrays.asList(sType.split(SplitUtils.DH));
            }

            //获取商品初始值
            String              initialStr = productListModel.getInitial();
            Map<String, String> initialMap = DataUtils.cast(SerializePhpUtils.getUnserializeObj(initialStr, Map.class));
            if (initialMap != null)
            {
                if (!initialMap.containsKey("stockWarn"))
                {
                    initialMap.put("stockWarn", productListModel.getMin_inventory().toString());
                }
            }
            //单位处理
            List<String>              units       = new ArrayList<>(16);
            List<Map<String, Object>> unitMapList = new ArrayList<>();
            parmaMap.clear();
            parmaMap.put("name", "单位");
            parmaMap.put("status", DictionaryListModel.Status.TAKE_EFFECT);
            List<Map<String, Object>> unitList = dictionaryListModelMapper.getDictionaryDynamic(parmaMap);
            for (Map<String, Object> map : unitList)
            {
                String unit = map.get("text").toString();

                units.add(unit);

            }
            resultMap.put("list", productListModel);
            resultMap.put("video", publiceService.getImgPath(productListModel.getVideo(), vo.getStoreId()));
            resultMap.put("proVideo", publiceService.getImgPath(productListModel.getProVideo(), vo.getStoreId()));
            resultMap.put("product_title", productListModel.getProduct_title());
            resultMap.put("subtitle", productListModel.getSubtitle());
            resultMap.put("keyword", productListModel.getKeyword());
            resultMap.put("weight", productListModel.getWeight());
            resultMap.put("product_number", productListModel.getProduct_number());
            resultMap.put("class_id", classId);
            resultMap.put("ctypes", productClassList);
            resultMap.put("brand_class", productBrandList);
            resultMap.put("brand_id", productListModel.getBrand_id());
            resultMap.put("imgurls", productShowImgList);
            resultMap.put("initial", initialMap);
            resultMap.put("status", productListModel.getStatus());
            resultMap.put("unit", units);
            resultMap.put("attr_group_list", attrGroupList);
            resultMap.put("checked_attr_list", checkedAttrList);
            resultMap.put("min_inventory", productListModel.getMin_inventory());
            resultMap.put("sp_type", sTypeList);
            resultMap.put("active", productListModel.getActive());
            resultMap.put("Plugin_arr", pluginArr);
            resultMap.put("content", productListModel.getContent());
            resultMap.put("brand_name", brandName);
            resultMap.put("brand_class_list1", brandClassModel);
            resultMap.put("freight_list1", goodsFreightMap);
            resultMap.put("distributors", null);
            resultMap.put("richList", productListModel.getRichList());
            resultMap.put("strArr", strArrList);
            resultMap.put("cover_map", productListModel.getCover_map());
        }
        catch (ClassCastException c)
        {
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SJCW, "数据错误");
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取商品明细数据 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "editPage");
        }

        return resultMap;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean upperAndLowerShelves(MainVo vo, String goodsIds, Integer status) throws LaiKeAPIException
    {
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            if (user != null)
            {
                publicGoodsService.upperAndLowerShelves(vo.getStoreId(), goodsIds, user.getMchId(), status);
                String[] pidList = goodsIds.split(",");
                for (String goodsId : pidList)
                {
                    if (status == 0)
                    {
                        publiceService.addAdminRecord(vo.getStoreId(), "上架了供应商商品ID：" + goodsId, AdminRecordModel.Type.UPDATE, vo.getAccessId());
                    }
                    else
                    {
                        publiceService.addAdminRecord(vo.getStoreId(), "下架了供应商商品ID：" + goodsId, AdminRecordModel.Type.UPDATE, vo.getAccessId());
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
    @Transactional(rollbackFor = Exception.class)
    public void delGoodsById(MainVo vo, String goodsIds) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User     user    = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            String[] pidList = goodsIds.split(SplitUtils.DH);
            for (String goodsId : pidList)
            {
                if (!publicGoodsService.delGoodsById(vo.getStoreId(), Integer.parseInt(goodsId), null))
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SPSCSB, "商品删除失败");
                }
                publiceService.addAdminRecord(vo.getStoreId(), "删除了供应商商品ID：" + goodsId, AdminRecordModel.Type.DEL, vo.getAccessId());
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
    @Transactional(rollbackFor = Exception.class)
    public void editSort(MainVo vo, Integer id, Integer sort) throws LaiKeAPIException
    {
        try
        {
            User             user           = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
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
            productListUpdate.setSort(sort);
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
    @Transactional(rollbackFor = Exception.class)
    public void addSupplierPro(MainVo vo, String goodsIds) throws LaiKeAPIException
    {
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            if (StringUtils.isEmpty(goodsIds))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误");
            }
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
                productListModel.setId(null);
                productListModel.setProduct_number(BuilderIDTool.getSnowflakeId() + "");
                productListModel.setMch_id(user.getMchId());
                productListModel.setPublisher(user.getUser_name());
                productListModel.setVolume(0);
                productListModel.setSearch_num(0);
                productListModel.setAdd_date(new Date());
                productListModel.setSort(productListModelMapper.getGoodsMaxSortByMch(vo.getStoreId(), user.getId()));
                productListModel.setStatus(DictionaryConst.GoodsStatus.NOT_GROUNDING.toString());
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
                    String     content    = user.getZhanghao() + "增加商品总库存:" + attrSave.getNum();
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
                publiceService.addAdminRecord(vo.getStoreId(), "添加了供应商商品ID：" + goodsId, AdminRecordModel.Type.ADD, vo.getAccessId());
                //添加跳转路径
                publicAdminService.addJumpPath(vo, productListModel.getId() + "", productListModel.getProduct_title(), JumpPathModel.JumpType.JUMP_TYPE0_GOODS, JumpPathModel.JumpType.JUMP_TYPE_APP,
                        GloabConst.LaikeTuiUrl.JumpPath.GOODS_APP, new String[]{productListModel.getId() + ""}, null, null);
                publicAdminService.addJumpPath(vo, productListModel.getId() + "", productListModel.getProduct_title(), JumpPathModel.JumpType.JUMP_TYPE0_GOODS, JumpPathModel.JumpType.JUMP_TYPE_PC,
                        GloabConst.LaikeTuiUrl.JumpPath.GOODS_PC, new String[]{productListModel.getId() + ""}, null, null);
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
    public Map<String, Object> addGoods(UploadMerchandiseVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        //保存规格属性集
        List<ConfiGureModel> saveConfigGureList = new ArrayList<>();
        //是否为编辑商品
        boolean isEdit = false;
        User    user   = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
        try
        {
            if (vo.getActive() == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QXZZCHDLX, "请选择支持活动类型");
            }
            ProductListModel productOld = null;
            if (vo.getpId() != null)
            {
                isEdit = true;
                productOld = productListModelMapper.selectByPrimaryKey(vo.getpId());
            }

            ProductListModel productListModel = new ProductListModel();
            //判断商品名称是否重复
            if (productOld == null || !productOld.getProduct_title().equals(vo.getProductTitle()))
            {
                productListModel.setStore_id(vo.getStoreId());
                productListModel.setProduct_title(vo.getProductTitle());
                productListModel.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS.toString());
                productListModel.setMch_id(user.getMchId());
                if (productListModelMapper.selectCount(productListModel) > 0)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_NYCZGSPQWZFTJ, "您有存在该商品,请勿重复添加");
                }
            }
            productListModel = new ProductListModel();
            if (isEdit)
            {
                productListModel.setId(vo.getpId());
            }
            productListModel.setStore_id(vo.getStoreId());
            productListModel.setProduct_title(vo.getProductTitle());
            productListModel.setSubtitle(vo.getSubtitle());
            productListModel.setScan(vo.getScan());
            if (StringUtils.isEmpty(vo.getScan()))
            {
                productListModel.setScan("");
            }
            productListModel.setProduct_number(BuilderIDTool.getGuid());
            productListModel.setProduct_class(vo.getProductClassId());
            productListModel.setBrand_id(StringUtils.stringParseInt(vo.getBrandId()));
            productListModel.setKeyword(vo.getKeyword());
            productListModel.setWeight(vo.getWeight());
            productListModel.setImgurl(vo.getShowImg());
            productListModel.setContent(vo.getContent());
            productListModel.setRichList(StringUtils.isNotEmpty(vo.getRichList()) ? vo.getRichList() : "");
            productListModel.setMin_inventory(vo.getStockWarn());
            //序号
            productListModel.setSort(vo.getSort());
            //处理商品类型
            List<Object> typeList = new ArrayList<>();
            if (StringUtils.isNotEmpty(vo.getsType()))
            {
                typeList = new ArrayList<>(Arrays.asList(vo.getsType().split(SplitUtils.DH)));
            }
            productListModel.setS_type(StringUtils.stringImplode(typeList, SplitUtils.DH, true));

            productListModel.setShow_adr(vo.getDisplayPosition());
            productListModel.setDistributor_id(vo.getDistributorId());
            productListModel.setFreight(vo.getFreightId() + "");
            productListModel.setActive(vo.getActive() + "");
            productListModel.setMch_id(0);
            productListModel.setInitial(vo.getInitial());
            productListModel.setWeight_unit(vo.getUnit());
            productListModel.setVolume(vo.getVolume());
            if (StringUtils.isEmpty(vo.getCoverMap()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QSCFMT, "请上传封面图");
            }
            productListModel.setCover_map(ImgUploadUtils.getUrlImgByName(vo.getCoverMap(), true));
            //是否为分销商品
            int isDistribution = 0;
            // 活动类型处理+展示位置数据处理
            List<String> showAdrs = new ArrayList<>();
            if (StringUtils.isNotEmpty(productListModel.getShow_adr()))
            {
                showAdrs = new ArrayList<>(Arrays.asList(productListModel.getShow_adr().split(SplitUtils.DH)));
            }
            //展示位
            showAdrs.add(DictionaryConst.GoodsShowAdr.GOODSSHOWADR_DEFAULT.toString());
            if (productListModel.getActive().equals(DictionaryConst.GoodsActive.GOODSACTIVE_POSITIVE_PRICE.toString()))
            {
                String showAdrStr = productListModel.getShow_adr();
                if (!StringUtils.isEmpty(showAdrStr))
                {
                    showAdrs = Arrays.asList(showAdrStr.split(SplitUtils.DH));
                }
            }
            else if (productListModel.getActive().equals(DictionaryConst.GoodsActive.GOODSACTIVE_VIP_DISCOUNT.toString()))
            {
                isDistribution = 1;
            }
            //非自选
            productListModel.setIs_zixuan(String.valueOf(DictionaryConst.WhetherMaven.WHETHER_NO));
            //商品展示位置数据处理
            List<Object> adrList = new ArrayList<>(showAdrs);
            productListModel.setShow_adr(StringUtils.stringImplode(adrList, SplitUtils.DH, true));

            productListModel.setIs_distribution(isDistribution);
//            productListModel.setPublisher(supplierCache.getAccount_number());
            productListModel.setMch_status(String.valueOf(DictionaryConst.GoodsMchExameStatus.EXAME_SUBMITTED));
            if (productOld != null)
            {
                productListModel.setMch_status(productOld.getMch_status());
                if (DictionaryConst.GoodsStatus.VIOLATION.toString().equals(productOld.getStatus()))
                {
                    productListModel.setStatus(DictionaryConst.GoodsStatus.NEW_GROUNDING.toString());
                    productListModel.setMch_status(String.valueOf(DictionaryConst.GoodsMchExameStatus.EXAME_SUBMITTED));
                }
                if (DictionaryConst.GoodsMchExameStatus.EXAME_NOT_PASS_STATUS.toString().equals(productOld.getMch_status()))
                {
                    productListModel.setMch_status(String.valueOf(DictionaryConst.GoodsMchExameStatus.EXAME_SUBMITTED));
                }
            }
            productListModel.setPro_param(vo.getProParam());
            productListModel.setPro_introduce(vo.getProIntroduce());
            //判断是否校验供应商商品的成本价
            if (Objects.nonNull(vo.getUnitType()))
            {
                productListModel.setUnitType(vo.getUnitType());
            }
            //校验商品数据
            productListModel = DataCheckTool.checkGoodsDataFormate(productListModel);

            //递归找上级
            String classIdStr = strOption(vo.getStoreId(), Integer.parseInt(productListModel.getProduct_class()), "");
            if (StringUtils.isEmpty(classIdStr))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SPLBBCZ, "商品类别id不存在");
            }
            productListModel.setProduct_class(classIdStr);
            BrandClassModel brandClassModel = new BrandClassModel();
            brandClassModel.setBrand_id(productListModel.getBrand_id());
            brandClassModel = brandClassModelMapper.selectOne(brandClassModel);
            if (brandClassModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_PPBCZ, "品牌不存在");
            }
            String classTop = classIdStr.split(SplitUtils.HG)[1];
            if (!brandClassModel.getCategories().contains(String.format(",%s,", classTop)))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SPLBBZGPPX, "商品类别不在该品牌下");
            }

            //商品展示图处理
            List<String> goodsImgUrlList = new ArrayList<>();
            String       goodsImgUrl     = productListModel.getImgurl();
            goodsImgUrlList = Arrays.asList(goodsImgUrl.split(SplitUtils.DH));

            //处理属性 [{"cbj":"1","yj":"12","sj":"11","kucun":"111","attr_list":[{"attr_id":"","attr_name":"蓝色","attr_group_name":"颜色"}]}]
            String attrStr = vo.getAttrArr();
            if (StringUtils.isEmpty(attrStr))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QTXSX, "请填写属性", "addProduct");
            }
            //编码处理
            attrStr = URLDecoder.decode(attrStr, GloabConst.Chartset.UTF_8);
            //当前无需回收的属性 id
            List<Integer> notRecycleAttrIds = new ArrayList<>();

            logger.debug("新上传的商品规格参数 :{}", attrStr);
            List<Map<String, Object>> attrAllList = JSON.parseObject(attrStr, new TypeReference<List<Map<String, Object>>>()
            {
            });
            int stockNum = 0;
            //当前所有属性的图片
            Map<String, Object> attrValueImageMap = new HashMap<>(16);
            for (Map<String, Object> map : attrAllList)
            {
                if (map.get("cbj") == null || map.get("sj") == null || map.get("yj") == null || map.get("img") == null)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERRCODE_QWSSX, "请完善属性");
                }
                //成本价
                BigDecimal cbj    = new BigDecimal(map.get("cbj").toString());
                BigDecimal price  = new BigDecimal(map.get("sj").toString());
                BigDecimal yprice = new BigDecimal(map.get("yj").toString());
                if (cbj.compareTo(price) > 0)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERRCODE_SJBXDYGHJ, "售价必须大于供货价");
                }
                //条形码
                String attrBraCode = MapUtils.getString(map, "bar_code");
                //当前规格库存
                if (!map.containsKey("kucun"))
                {
                    logger.debug("保存商品的时候库存为空>>>{}", JSON.toJSONString(map));
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_KCBNWK, "库存不能为空");
                }
                Integer currentNum = MapUtils.getInteger(map, "kucun");
                if (currentNum == null || currentNum == 0)
                {
                    logger.debug("保存商品的时候库存为空>>>{}", JSON.toJSONString(map));
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_KCBNWK, "库存不能为空");
                }
                stockNum += currentNum;
                //[{"attr_group_name":"颜色","attr_list":[{"attr_name":"蓝色","status":true},{"attr_name":"天蓝"}]},
                List<Map<String, Object>> attrList = DataUtils.cast(map.get("attr_list"));
                if (attrList == null)
                {
                    attrList = new ArrayList<>();
                }
                //用于保存属性的集合 (颜色分类_LKT_144=花色_LKT_145...)
                Map<String, Object> attrbiuteStrMap = new HashMap<>(16);
                ConfiGureModel      saveConfigure   = new ConfiGureModel();
                for (Map<String, Object> attrMap : attrList)
                {
                    //当前规格key和value
                    String attributeGroupName = attrMap.get("attr_group_name").toString();
                    String attributeName      = attrMap.get("attr_name").toString();
                    logger.debug("正在处理规格：{}{}", attributeGroupName, attributeName);
                    //记录规格
                    int      attrNameId;
                    SkuModel saveSkuModel = new SkuModel();
                    saveSkuModel.setStore_id(vo.getStoreId());
                    saveSkuModel.setAdmin_name(user.getUser_name());
                    saveSkuModel.setType(DictionaryConst.SkuType.SKUTYPE_NAME);
                    saveSkuModel.setStatus(SkuModel.SKU_STATUS_INVALID);

                    String langCode = vo.getLang_code();
                    if (StringUtils.isNotEmpty(langCode))
                    {
                        saveSkuModel.setLang_code(langCode);
                    }

                    Integer countryNum = vo.getCountry_num();
                    if (countryNum != null)
                    {
                        saveSkuModel.setCountry_num(countryNum);
                    }

                    //店铺发布、编辑商品时,设置的新属性,保存在SKU中,只要是商品未审核通过,则应不生效,只有审核通过或者平台手动开启方可生效
                    if (productListModel.getMch_status().equals(DictionaryConst.GoodsMchExameStatus.EXAME_PASS_STATUS.toString()))
                    {
                        saveSkuModel.setStatus(SkuModel.SKU_STATUS_TAKE_EFFECT);
                    }
                    saveSkuModel.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS);
                    //根据属性名称获取属性id
                    int attrGourpNameId = getAttributeId(vo.getStoreId(), DictionaryConst.SkuType.SKUTYPE_NAME, attributeGroupName, 0);
                    if (attrGourpNameId > 0)
                    {
                        // 当属性名称ID不为0，SKU表里有数据
                        attrNameId = getAttributeId(vo.getStoreId(), DictionaryConst.SkuType.SKUTYPE_VALUE, attributeName, attrGourpNameId);
                        if (attrNameId == 0)
                        {
                            //没找到则添加属性
                            String code = publicGoodsService.getSkuCode(vo.getStoreId(), null, attrGourpNameId);
                            saveSkuModel.setCode(code);
                            saveSkuModel.setType(DictionaryConst.SkuType.SKUTYPE_VALUE);
                            saveSkuModel.setName(attributeName);
                            saveSkuModel.setAdd_date(new Date());
                            saveSkuModel.setSid(attrGourpNameId);
                            skuModelMapper.insertSelective(saveSkuModel);
                            attrNameId = saveSkuModel.getId();
                        }
                    }
                    else
                    {
                        //全部都没有则全部添加
                        String code = publicGoodsService.getSkuCode(vo.getStoreId(), attributeGroupName, null);
                        saveSkuModel.setCode(code);
                        saveSkuModel.setName(attributeGroupName);
                        saveSkuModel.setType(DictionaryConst.SkuType.SKUTYPE_NAME);
                        saveSkuModel.setSid(0);
                        saveSkuModel.setAdd_date(new Date());
                        skuModelMapper.insertSelective(saveSkuModel);
                        attrGourpNameId = saveSkuModel.getId();
                        code = publicGoodsService.getSkuCode(vo.getStoreId(), null, saveSkuModel.getId());
                        saveSkuModel.setId(null);
                        saveSkuModel.setSid(attrGourpNameId);
                        saveSkuModel.setCode(code);
                        saveSkuModel.setName(attributeName);
                        saveSkuModel.setType(DictionaryConst.SkuType.SKUTYPE_VALUE);
                        saveSkuModel.setAdd_date(new Date());
                        skuModelMapper.insertSelective(saveSkuModel);
                        attrNameId = saveSkuModel.getId();
                    }

                    //拼接属性 规格+str+id
                    attributeGroupName += "_LKT_" + attrGourpNameId;
                    attributeName += "_LKT_" + attrNameId;
                    attrMap.put("attr_group_name", attributeGroupName);
                    attrMap.put("attr_name", attributeName);
                    //属性序列化处理
                    attrbiuteStrMap.put(attributeGroupName, attributeName);
                }
                saveConfigure.setCostprice(cbj);
                saveConfigure.setPrice(price);
                saveConfigure.setYprice(yprice);
                saveConfigure.setNum(currentNum);
                saveConfigure.setTotal_num(currentNum);
                saveConfigure.setBar_code(attrBraCode);

                //序列化
                saveConfigure.setAttribute(SerializePhpUtils.JavaSerializeByPhp(attrbiuteStrMap));
                //当前属性图片
                attrValueImageMap.put(saveConfigure.getAttribute(), ImgUploadUtils.getUrlImgByName(map.get("img") + "", true));
                //如果是修改
                if (isEdit)
                {
                    //获取之前商品属性信息 看当前属性是否是之前的属性,如果是之前的属性则做修改 否则做添加
                    ConfiGureModel configureOld = new ConfiGureModel();
                    configureOld.setPid(productListModel.getId());
                    configureOld.setAttribute(saveConfigure.getAttribute());
                    configureOld.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS.toString());
                    configureOld = confiGureModelMapper.selectOne(configureOld);
                    if (configureOld != null)
                    {
                        //标记无需回收的规格
                        notRecycleAttrIds.add(configureOld.getId());
                        //修改当前属性
                        ConfiGureModel configureNew = new ConfiGureModel();
                        configureNew.setId(configureOld.getId());
                        //规格图片处理
                        String currentImage = attrValueImageMap.get(configureOld.getAttribute()) + "";
                        if (!StringUtils.isEmpty(currentImage))
                        {
                            configureNew.setImg(currentImage);
                        }
                        configureNew.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS.toString());
                        configureNew.setCostprice(saveConfigure.getCostprice());
                        configureNew.setYprice(saveConfigure.getYprice());
                        configureNew.setPrice(saveConfigure.getPrice());
                        //总库存只增不减 库存没有发生变化则不动库存
                        configureNew.setNum(configureOld.getNum());
                        configureNew.setTotal_num(configureOld.getTotal_num());
                        if (!configureOld.getNum().equals(saveConfigure.getNum()))
                        {
                            configureNew.setNum(saveConfigure.getNum());
                            //计算增加了多少库存
                            int totalNumOld = saveConfigure.getTotal_num() - saveConfigure.getNum();
                            //总库存只增不减
                            if (totalNumOld > 0)
                            {
                                configureNew.setTotal_num(configureOld.getTotal_num() + totalNumOld);
                            }
                        }
                        int count = confiGureModelMapper.updateByPrimaryKeySelective(configureNew);
                        if (count < 1)
                        {
                            logger.info("修改商品规格信息失败 参数:" + JSON.toJSONString(productListModel));
                            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SXBCSB, "属性保存失败", "addProduct");
                        }
                        String content;

                        StockModel saveStockModel = new StockModel();
                        saveStockModel.setTotal_num(saveConfigure.getTotal_num());
                        saveStockModel.setFlowing_num(configureOld.getTotal_num());
                        //记录库存
                        if (!configureOld.getNum().equals(saveConfigure.getNum()))
                        {
                            //清空之前的库存记录重新记录
                            /*StockModel stockDel = new StockModel();
                            stockDel.setStore_id(vo.getStoreId());
                            stockDel.setProduct_id(productListModel.getId());
                            stockDel.setAttribute_id(confiGureOld.getId());
                            stockModelMapper.delete(stockDel);*/
                            String text = "增加商品规格库存";
                            int    num  = saveConfigure.getNum() - configureOld.getNum();
                            if (num < 0)
                            {
                                text = "减少商品规格库存";
                            }
                            //添加库存信息
                            content = user.getZhanghao() + text + Math.abs(num);
                            saveStockModel.setId(null);
                            saveStockModel.setStore_id(vo.getStoreId());
                            saveStockModel.setProduct_id(productListModel.getId());
                            saveStockModel.setAttribute_id(configureNew.getId());
                            saveStockModel.setTotal_num(configureNew.getTotal_num());
                            if (num < 0)
                            {
                                saveStockModel.setType(StockModel.StockType.AGREEMENTTYPE_WAREHOUSING_OUT);
                            }
                            else
                            {
                                saveStockModel.setType(StockModel.StockType.STOCKTYPE_WAREHOUSING);
                            }
                            saveStockModel.setContent(content);
                            saveStockModel.setFlowing_num(Math.abs(num));
                            saveStockModel.setAdd_date(new Date());
                            count = stockModelMapper.insertSelective(saveStockModel);
                            if (count < 1)
                            {
                                logger.info("库存记录失败 参数:" + JSON.toJSONString(saveStockModel));
                                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_KCJLSB, "库存记录失败", "addProduct");
                            }
                        }
                        //如果库存有变化则清空之前的库存记录
                        // 当属性库存低于等于预警值
                        if (productListModel.getMin_inventory() >= saveConfigure.getNum())
                        {
                            content = "预警";
                            saveStockModel.setId(null);
                            saveStockModel.setType(StockModel.StockType.AGREEMENTTYPE_WAREHOUSING_WARNING);
                            saveStockModel.setContent(content);
                            saveStockModel.setUser_id(user.getUser_id());
                            saveStockModel.setAdd_date(new Date());
                            count = stockModelMapper.insertSelective(saveStockModel);
                            if (count < 1)
                            {
                                logger.info("库存记录失败 参数:" + JSON.toJSONString(saveStockModel));
                                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_KCYJJLSB, "库存预警记录失败", "addProduct");
                            }
                        }
                        //做了修改无需做保存,下一个
                        continue;
                    }
                }
                //需要保存的新规格
                saveConfigGureList.add(saveConfigure);
            }
            //第一张为商品主图
            productListModel.setImgurl(ImgUploadUtils.getUrlImgByName(goodsImgUrlList.get(0), true));
            productListModel.setNum(stockNum);

            //保存商品
            int count;
            if (isEdit)
            {
                productListModel.setMch_id(null);
                count = productListModelMapper.updateByPrimaryKeySelective(productListModel);
                //同步店铺供应商商品信息
//                mchPro(vo.getStoreId(), supplierCache.getId(), productListModel, saveConfigGureList, attrValueImageMap);
                publiceService.addAdminRecord(vo.getStoreId(), "修改了供应商商品ID：" + productOld.getId() + " 的信息", AdminRecordModel.Type.UPDATE, vo.getAccessId());
            }
            else
            {
                productListModel.setLabel("");
                productListModel.setAdd_date(new Date());
                productListModel.setMch_id(user.getMchId());
                productListModel.setSupplier_superior(0);
                //供应商设置是否需要审核
                SupplierConfigModel config = supplierConfigModelMapper.getConfig(vo.getStoreId());
                if (Objects.isNull(config))
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_PZBCZ, "请先设置供应商配置信息");
                }
                if (config.getIs_examine().equals(DictionaryConst.WhetherMaven.WHETHER_NO))
                {
                    productListModel.setMch_status(String.valueOf(DictionaryConst.GoodsMchExameStatus.EXAME_PASS_STATUS));
                }
                //获取最新序号
                int sort = productListModelMapper.getGoodsMaxSort(vo.getStoreId());
                productListModel.setSort(sort);
                count = productListModelMapper.insertSelective(productListModel);

                publiceService.addAdminRecord(vo.getStoreId(), "添加了供应商商品ID：" + productOld.getId(), AdminRecordModel.Type.ADD, vo.getAccessId());
                //添加跳转路径
                publicAdminService.addJumpPath(vo, productListModel.getId() + "", productListModel.getProduct_title(), JumpPathModel.JumpType.JUMP_TYPE0_GOODS, JumpPathModel.JumpType.JUMP_TYPE_APP,
                        GloabConst.LaikeTuiUrl.JumpPath.GOODS_APP, new String[]{productListModel.getId() + ""}, null, null);
                publicAdminService.addJumpPath(vo, productListModel.getId() + "", productListModel.getProduct_title(), JumpPathModel.JumpType.JUMP_TYPE0_GOODS, JumpPathModel.JumpType.JUMP_TYPE_PC,
                        GloabConst.LaikeTuiUrl.JumpPath.GOODS_PC, new String[]{productListModel.getId() + ""}, null, null);
            }
            if (count < 1)
            {
                logger.info("商品保存失败 参数:" + JSON.toJSONString(productListModel));
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SPBCSB, "商品保存失败", "addProduct");
            }
            //轮播图处理
            if (isEdit)
            {
                //修改之前图片,把之前图片删除
                ProductImgModel delProductImgModel = new ProductImgModel();
                delProductImgModel.setProduct_id(productListModel.getId());
                count = productImgModelMapper.delete(delProductImgModel);
                logger.info("产品之前展示图删除失败 商品id:{} 删除状态:{}", productListModel.getId(), count > 0);
            }
            //添加轮播图
            for (String img : goodsImgUrlList)
            {
                ProductImgModel saveImg = new ProductImgModel();
                saveImg.setProduct_url(ImgUploadUtils.getUrlImgByName(img, true));
                saveImg.setProduct_id(productListModel.getId());
                saveImg.setSeller_id(String.valueOf(user.getMchId()));
                saveImg.setAdd_date(new Date());
                count = productImgModelMapper.insertSelective(saveImg);
                if (count < 1)
                {
                    logger.info("产品展示图上传失败 参数:" + JSON.toJSONString(saveImg));
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CPZSTSCSB, "产品展示图上传失败", "addProduct");
                }
            }
            //规格回收处理
            if (isEdit)
            {
                //回收除了当前的规格其它属性
                count = confiGureModelMapper.delAppointConfiGureInfo(productListModel.getId(), notRecycleAttrIds);
                if (count < 1)
                {
                    logger.debug("未回收规格,可能是在原来的基础上做了新增/修改;规格Id集:{}", notRecycleAttrIds);
                }
            }

            //添加库存、规格信息
            for (ConfiGureModel configureModel : saveConfigGureList)
            {
                //规格图片处理
                String currentImage = ImgUploadUtils.getUrlImgByName(attrValueImageMap.get(configureModel.getAttribute()) + "", false);
                //验证属性图片是否上传
                if (StringUtils.isEmpty(currentImage))
                {
                    //没有图片则查看当前属性是否存在过，如果存在过则取之前图片
                    ConfiGureModel configOld = new ConfiGureModel();
                    configOld.setPid(productListModel.getId());
                    configOld.setAttribute(configureModel.getAttribute());
                    configOld = confiGureModelMapper.selectOne(configOld);
                    if (configOld == null)
                    {
                        logger.debug("属性id【{}】没有上传图片", configureModel.getId());
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QSCSXTP, "请上传属性图片");
                    }
                    else
                    {
                        //属性老图
                        currentImage = configOld.getImg();
                        logger.debug("商品id{},属性字符串{},未上传新属性图片,延续用之前的图片", productListModel.getId(), configureModel.getAttribute());
                    }
                }
                configureModel.setImg(currentImage);

                //库存数量
                int attrNum = configureModel.getNum();
                configureModel.setNum(0);
                configureModel.setTotal_num(0);
                configureModel.setPid(productListModel.getId());
                configureModel.setMin_inventory(productListModel.getMin_inventory());
                configureModel.setBar_code(productListModel.getScan());
                configureModel.setColor("");
                configureModel.setCtime(new Date());
                configureModel.setUnit(productListModel.getWeight_unit());
                count = confiGureModelMapper.insertSelective(configureModel);
                if (count < 1)
                {
                    logger.info("库存记录添加失败 参数:" + JSON.toJSONString(configureModel));
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_KCJLTJSB, "库存记录添加失败", "addProduct");
                }

                //添加库存信息
                AddStockVo addStockVo = new AddStockVo();
                addStockVo.setStoreId(vo.getStoreId());
                addStockVo.setId(configureModel.getId());
                addStockVo.setPid(configureModel.getPid());
                addStockVo.setUpStockTotal(true);
                addStockVo.setAddNum(attrNum);
                publicStockService.addGoodsStock(addStockVo, String.valueOf(user.getMchId()));
            }
            //同步商品总库存
            productClassModelMapper.synchroStock(productListModel.getId());


        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("添加商品 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "addGoods");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> getGoodClass(MainVo vo, Integer classId, Integer brandId) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            Map<String, Object> resultClassMap = publicGoodsService.getClassifiedBrands(vo.getStoreId(), classId, brandId);
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
    public Map<String, Object> queryDropDown(MainVo vo, Integer classId) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            //获取下拉框数据
            resultMap = publicGoodsService.dropDownProClassAndBrand(vo.getStoreId(), classId);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取下拉框数据 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "queryDropDown");
        }
        return resultMap;
    }


    private void exportGoodsData(List<Map<String, Object>> goodsList, HttpServletResponse response) throws LaiKeAPIException
    {
        try
        {
            //表头
            String[] headerList = new String[]{"商品编号", "商品图片", "商品标题", "商品分类", "品牌", "售价", "供货价", "库存", "商品状态", "销量", "所属供应商", "排序", "上架时间"};
            //对应字段
            String[]     kayList = new String[]{"id", "imgurl", "product_title", "class_name", "brand_name", "price", "supplyPrice", "num", "statusDesc", "volume", "supplier_name", "sort", "upper_shelf_time"};
            ExcelParamVo vo      = new ExcelParamVo();
            vo.setTitle("供应商商品列表");
            vo.setHeaderList(headerList);
            vo.setValueList(kayList);
            vo.setList(goodsList);
            vo.setResponse(response);
            EasyPoiExcelUtil.excelExport(vo);
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

    public String strOption(int storeId, int cid, String res) throws LaiKeAPIException
    {
        try
        {
            logger.info("cid={} res={} 递归找上级", cid, res);
            //获取商品类别
            ProductClassModel productClassModel = new ProductClassModel();
            productClassModel.setCid(cid);
            productClassModel.setStore_id(storeId);
            productClassModel.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS);
            productClassModel = productClassModelMapper.selectOne(productClassModel);
            if (productClassModel != null)
            {
                int sid = productClassModel.getSid();
                res = cid + SplitUtils.HG + res;
                if (sid == 0)
                {
                    res = SplitUtils.HG + res;
                }
                else
                {
                    res = this.strOption(storeId, sid, res);
                }
            }
            return res;
        }
        catch (ClassCastException c)
        {
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSLXBZQ, "参数类型不正确", "strOption");
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

    /**
     * 获取属性id
     *
     * @param storeId -
     * @param type    -
     * @param name    -
     * @param sid     -
     * @return int
     * @throws LaiKeAPIException -
     */
    private int getAttributeId(int storeId, int type, String name, int sid) throws LaiKeAPIException
    {
        try
        {
            //参数列表
            Map<String, Object> parmaMap = new HashMap<>();
            List<Integer>       storeIds = new ArrayList<>();
            storeIds.add(0);
            storeIds.add(storeId);
            parmaMap.put("storeIds", storeIds);
            parmaMap.put("type", type);
            parmaMap.put("name", name);
            parmaMap.put("sid", sid);
            List<Map<String, Object>> attributeValueList = skuModelMapper.getAttributeDynamic(parmaMap);
            for (Map<String, Object> map : attributeValueList)
            {
                return Integer.parseInt(map.get("id").toString());
            }
        }
        catch (ClassCastException c)
        {
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSLXBZQ, "参数类型不正确", "getAttributeId");
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取属性id 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getAttributeId");
        }
        return 0;
    }

}
