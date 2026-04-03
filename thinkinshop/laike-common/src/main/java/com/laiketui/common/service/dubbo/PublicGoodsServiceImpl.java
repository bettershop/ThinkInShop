package com.laiketui.common.service.dubbo;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import com.laiketui.common.api.*;
import com.laiketui.common.api.admin.PublicAdminService;
import com.laiketui.common.mapper.*;
import com.laiketui.common.utils.HttpApiUtils;
import com.laiketui.common.utils.tool.ImgUploadUtils;
import com.laiketui.common.utils.tool.data.DataCheckTool;
import com.laiketui.common.utils.tool.data.GoodsDataUtils;
import com.laiketui.core.common.LKTSnowflakeIdWorker;
import com.laiketui.core.common.SplitUtils;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.exception.LaiKeApiWarnException;
import com.laiketui.core.lktconst.DictionaryConst;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.core.utils.okhttp.HttpUtils;
import com.laiketui.core.utils.tool.*;
import com.laiketui.domain.Page;
import com.laiketui.domain.config.*;
import com.laiketui.domain.dictionary.DictionaryListModel;
import com.laiketui.domain.distribution.FreightModel;
import com.laiketui.domain.living.LivingProductModel;
import com.laiketui.domain.log.AdminRecordModel;
import com.laiketui.domain.mch.DistributionGradeModel;
import com.laiketui.domain.mch.MchConfigModel;
import com.laiketui.domain.mch.MchStoreModel;
import com.laiketui.domain.message.MessageLoggingModal;
import com.laiketui.domain.presell.PreSellGoodsModel;
import com.laiketui.domain.product.*;
import com.laiketui.domain.supplier.SupplierProClassModel;
import com.laiketui.domain.user.User;
import com.laiketui.domain.user.UserAddress;
import com.laiketui.domain.user.UserFootprintModel;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.PageModel;
import com.laiketui.domain.vo.dic.DicVo;
import com.laiketui.domain.vo.freight.DefaultFreightVO;
import com.laiketui.domain.vo.goods.AddStockVo;
import com.laiketui.domain.vo.mch.UploadMerchandiseVo;
import com.laiketui.root.common.BuilderIDTool;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.net.URLDecoder;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 关于商品的公共类
 *
 * @author Trick
 * @date 2020/11/11 14:56
 */
@Service
public class PublicGoodsServiceImpl implements PublicGoodsService
{

    private final Logger logger = LoggerFactory.getLogger(PublicGoodsServiceImpl.class);

    @Autowired
    private CustomerModelMapper customerModelMapper;

    @Autowired
    private ProLabelModelMapper proLabelModelMapper;

    @Autowired
    private LangModelMapper langModelMapper;

    @Autowired
    private CountryModelMapper countryModelMapper;

    @Autowired
    private PreSellGoodsMapper preSellGoodsMapper;

    @Autowired
    private MchConfigModelMapper mchConfigModelMapper;

    @Autowired
    private MchStoreModelMapper mchStoreModelMapper;

    @Autowired
    private MchPromiseModelMapper mchPromiseModelMapper;

    @Autowired
    private PromiseShModelMapper promiseShModelMapper;

    @Autowired
    private PublicMchService publicMchService;

    @Autowired
    private LivingProductModelMapper livingProductModelMapper;

    @Autowired
    private DraftsModelMapper draftsModelMapper;

    @Override
    public Map<String, Object> addPage(int storeId, String adminName, int mchId, int type) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            //运费集
            List<Map<String, Object>> freightModelList = new ArrayList<>();
            //产品类型集
            List<Map<String, Object>> spTypeList = new ArrayList<>();
            //产品展示位
            List<Map<String, Object>> showAdrList = new ArrayList<>();
            //单位
            List<String> unitList = new ArrayList<>();
            //插件集
            Map<String, Object> pluginArrMap = new HashMap<>(16);

            LKTSnowflakeIdWorker snowflakeIdWorker = new LKTSnowflakeIdWorker();
            //获取运费
            FreightModel freightModel = new FreightModel();
            freightModel.setStore_id(storeId);
            freightModel.setMch_id(mchId);
            List<FreightModel> freightModels = freightModelMapper.select(freightModel);
            for (FreightModel freight : freightModels)
            {
                Map<String, Object> map = new HashMap<>(16);
                map.put("id", freight.getId());
                map.put("name", freight.getName());
                map.put("is_default", freight.getIs_default());
                freightModelList.add(map);
            }
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("status", DictionaryListModel.Status.TAKE_EFFECT);
            //获取产品类型
            ProLabelModel proLabelModel = new ProLabelModel();
            proLabelModel.setStore_id(storeId);
            List<ProLabelModel> proLabelModelList = proLabelModelMapper.select(proLabelModel);
            for (ProLabelModel proLabel : proLabelModelList)
            {
                Map<String, Object> goodsTypeMap = new HashMap<>(3);
                goodsTypeMap.put("name", proLabel.getName());
                goodsTypeMap.put("value", proLabel.getId());
                goodsTypeMap.put("status", false);
                spTypeList.add(goodsTypeMap);
            }
            //获取商品展示位
            parmaMap.put("name", "商品展示位置");
            List<Map<String, Object>> showAdrListTemp = dictionaryListModelMapper.getDictionaryDynamic(parmaMap);
            for (Map<String, Object> map : showAdrListTemp)
            {
                Map<String, Object> goodsShowAdrMap = new HashMap<>(3);
                goodsShowAdrMap.put("name", map.get("text"));
                goodsShowAdrMap.put("value", map.get("value"));
                goodsShowAdrMap.put("status", false);
                showAdrList.add(goodsShowAdrMap);
            }

            //获取单位
            parmaMap.put("name", "单位");
            List<Map<String, Object>> unitMap = dictionaryListModelMapper.getDictionaryDynamic(parmaMap);
            for (Map<String, Object> map : unitMap)
            {
                unitList.add(String.valueOf(map.get("text")));
            }
            //如果是平台则获取插件
            List<Map<String, Object>> pluginArr = new ArrayList<>();
            if (type == GloabConst.LktConfig.LKT_CONFIG_TYPE_PT)
            {
                //获取插件集...暂时不做
                //$Plugin_arr = $Plugin->product_plugin($db, $store_id, 'product', '');
            }
            else
            {
                //获取所有商品支持的活动
                pluginArr = publiceService.getGoodsActive(storeId);
            }
            pluginArrMap.put("active", pluginArr);
            //店铺门店信息
            MchStoreModel mchStoreModel = new MchStoreModel();
            mchStoreModel.setStore_id(storeId);
            mchStoreModel.setMch_id(mchId);
            List<MchStoreModel> mchStoreModels = mchStoreModelMapper.select(mchStoreModel);
            if (mchStoreModels != null && mchStoreModels.size() > 0)
            {
                resultMap.put("mchStoreModels", mchStoreModels);
            }
            resultMap.put("product_number", snowflakeIdWorker.nextId());
            resultMap.put("plugin_list", pluginArrMap);
            resultMap.put("freight_list", freightModelList);
            resultMap.put("s_type", spTypeList);
            resultMap.put("show_adr", showAdrList);
            resultMap.put("unit", unitList);
        }
        catch (ClassCastException c)
        {
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSLXBZQ, "参数类型不正确", "addPage");
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("添加商品 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "addPage");
        }

        return resultMap;
    }

    @Override
    public Map<String, Object> addPage(MainVo vo, String adminName, int mchId, int type) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            //运费集
            List<Map<String, Object>> freightModelList = new ArrayList<>();
            //产品类型集
            List<Map<String, Object>> spTypeList = new ArrayList<>();
            //产品展示位
            List<Map<String, Object>> showAdrList = new ArrayList<>();
            //单位
            List<String> unitList = new ArrayList<>();
            //插件集
            Map<String, Object> pluginArrMap = new HashMap<>(16);

            LKTSnowflakeIdWorker snowflakeIdWorker = new LKTSnowflakeIdWorker();

            int storeId = vo.getStoreId();

            String langCode = vo.getLang_code();
            if (StringUtils.isEmpty(langCode))
            {
                langCode = GloabConst.Lang.CN;
            }
            //获取运费
            FreightModel freightModel = new FreightModel();
            freightModel.setLang_code(langCode);
            freightModel.setStore_id(storeId);
            freightModel.setMch_id(mchId);
            List<FreightModel> freightModels = freightModelMapper.select(freightModel);
            for (FreightModel freight : freightModels)
            {
                Map<String, Object> map = new HashMap<>(16);
                map.put("id", freight.getId());
                map.put("name", freight.getName());
                map.put("is_default", freight.getIs_default());
                freightModelList.add(map);
            }
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("status", DictionaryListModel.Status.TAKE_EFFECT);
            //获取产品类型
            ProLabelModel proLabelModel = new ProLabelModel();
            proLabelModel.setLang_code(langCode);
            proLabelModel.setStore_id(storeId);
            List<ProLabelModel> proLabelModelList = proLabelModelMapper.select(proLabelModel);
            for (ProLabelModel proLabel : proLabelModelList)
            {
                Map<String, Object> goodsTypeMap = new HashMap<>(3);
                goodsTypeMap.put("name", proLabel.getName());
                goodsTypeMap.put("value", proLabel.getId());
                goodsTypeMap.put("status", false);
                spTypeList.add(goodsTypeMap);
            }
            //获取商品展示位
            parmaMap.put("name", "商品展示位置");
            parmaMap.put("lang_code", langCode);
            List<Map<String, Object>> showAdrListTemp = dictionaryListModelMapper.getDictionaryDynamic(parmaMap);
            for (Map<String, Object> map : showAdrListTemp)
            {
                Map<String, Object> goodsShowAdrMap = new HashMap<>(3);
                goodsShowAdrMap.put("name", map.get("text"));
                goodsShowAdrMap.put("value", map.get("value"));
                goodsShowAdrMap.put("status", false);
                showAdrList.add(goodsShowAdrMap);
            }

            //获取单位
            parmaMap.put("name", "单位");
            List<Map<String, Object>> unitMap = dictionaryListModelMapper.getDictionaryDynamic(parmaMap);
            for (Map<String, Object> map : unitMap)
            {
                unitList.add(String.valueOf(map.get("text")));
            }
            //如果是平台则获取插件
            List<Map<String, Object>> pluginArr = new ArrayList<>();
            if (type == GloabConst.LktConfig.LKT_CONFIG_TYPE_PT)
            {
                //获取插件集...暂时不做
                //$Plugin_arr = $Plugin->product_plugin($db, $store_id, 'product', '');
            }
            else
            {
                //获取所有商品支持的活动
                pluginArr = publiceService.getGoodsActive(storeId);
            }
            pluginArrMap.put("active", pluginArr);
            //店铺门店信息
            MchStoreModel mchStoreModel = new MchStoreModel();
            mchStoreModel.setStore_id(storeId);
            mchStoreModel.setMch_id(mchId);
            List<MchStoreModel> mchStoreModels = mchStoreModelMapper.select(mchStoreModel);
            if (mchStoreModels != null && mchStoreModels.size() > 0)
            {
                resultMap.put("mchStoreModels", mchStoreModels);
            }
            resultMap.put("product_number", snowflakeIdWorker.nextId());
            resultMap.put("plugin_list", pluginArrMap);
            resultMap.put("freight_list", freightModelList);
            resultMap.put("s_type", spTypeList);
            resultMap.put("show_adr", showAdrList);
            resultMap.put("unit", unitList);
        }
        catch (ClassCastException c)
        {
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSLXBZQ, "参数类型不正确", "addPage");
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("添加商品 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "addPage");
        }

        return resultMap;
    }

    @Override
    public Map<String, Object> livingEditPage(int storeId, String adminName, int mchId, int goodsId, int type, Integer roomId) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            //运费信息 全部
            List<Map<String, Object>> freightModelList;
            //商品运费信息
            Map<String, Object> goodsFreightMap = new HashMap<>(16);
            //商品图片信息(封面图集合)
            List<String> productShowImgList = new ArrayList<>();
            //商品品牌信息 全部
            List<Map<String, Object>> productBrandList = null;

            //商品分类
            List<ProductClassModel> productClassList = new ArrayList<>();
            //插件集信息 Plugin_arr
            Map<String, Object> pluginArr = new HashMap<>(16);

            //如果是平台，则查询商品信息无视店铺
//            if (type != GloabConst.LktConfig.LKT_CONFIG_TYPE_PT) {
//                //否则加店铺id
//                productListModel.setMch_id(mchId);
//            }
            ProductListModel productListModel = productListModelMapper.selectByPrimaryKey(goodsId);
            if (productListModel != null)
            {
                //获取图片信息
                String imgUrl       = productListModel.getImgurl();
                String converMapUrl = productListModel.getCover_map();
                imgUrl = publiceService.getImgPath(imgUrl, storeId);
                productListModel.setImgurl(imgUrl);
                converMapUrl = publiceService.getImgPath(converMapUrl, storeId);
                productListModel.setCover_map(converMapUrl);
            }
            else
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SPBCZ, "商品不存在");
            }
            //获取店铺运费信息
            FreightModel freightModel = new FreightModel();
            freightModel.setStore_id(storeId);
            freightModel.setMch_id(mchId);
            freightModelList = freightModelMapper.getFreightInfo(freightModel);
            for (Map<String, Object> map : freightModelList)
            {
                boolean selected = false;
                String  fid      = map.get("id").toString();
                if (fid.equals(productListModel.getFreight()))
                {
                    selected = true;
                }
                map.put("selected", selected);
            }
            //获取商品封面图
            boolean         flag            = false;
            ProductImgModel productImgModel = new ProductImgModel();
            productImgModel.setProduct_id(goodsId);
            List<Map<String, Object>> productImgModelList = productImgModelMapper.getProductImgInfoByPid(productImgModel);
            if (GloabConst.LktConfig.LKT_CONFIG_TYPE_PT == type || GloabConst.LktConfig.LKT_CONFIG_TYPE_PC == type)
            {
                flag = true;
            }
            if (productImgModelList != null)
            {
                for (int i = 0; i < productImgModelList.size(); i++)
                {
                    Map<String, Object> map        = productImgModelList.get(i);
                    String              productUrl = map.get("product_url").toString();
                    productUrl = publiceService.getImgPath(productUrl, storeId);
                    productShowImgList.add(productUrl);
                    if (flag)
                    {
                        int isCenter = productUrl.equals(productImgModel.getProduct_url()) ? 1 : 0;
                        map.put("is_center", isCenter);
                    }
                    else
                    {
                        map.clear();
                        map.put(i + "", productUrl);
                    }
                }
            }
            //商品分类+品牌处理
            String   res     = productListModel.getProduct_class();
            //商品所属分类
            int classId    = -1;
            int classIdTop = -1;
            if (StringUtils.isNotEmpty(res))
            {
                String[] resList = res.split(SplitUtils.HG);
                //商品所属分类顶级
                classIdTop = Integer.parseInt(resList[1]);
                int classNum = resList.length - 1;
                //商品所属分类
                classId = Integer.parseInt(resList[classNum]);
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
            }

            //获取当前品牌名称
            String          brandName       = null;
            BrandClassModel brandClassModel = new BrandClassModel();
            if (productListModel.getBrand_id() != null)
            {
                brandClassModel.setBrand_id(productListModel.getBrand_id());
                brandClassModel = brandClassModelMapper.selectOne(brandClassModel);
                if (brandClassModel != null)
                {
                    brandName = brandClassModel.getBrand_name();
                }
            }

            //获取顶级牌信息
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", storeId);
            parmaMap.put("status", BrandClassModel.Status.OPEN);

            if (StringUtils.isNotEmpty(brandName) && classIdTop != -1)
            {
                parmaMap.put("categories", classIdTop);
                productBrandList = brandClassModelMapper.getBrandClassDynamic(parmaMap);
            }

            //商品主图
            Map<String, Object> defaultImgMap = new HashMap<>(16);
            defaultImgMap.put("product_url", productListModel.getImgurl());
            if (flag)
            {
                //品牌默认值
                Map<String, Object> defaultBrandMap = new HashMap<>(16);
                defaultBrandMap.put("brand_id", 0);
                defaultBrandMap.put("brand_name", "请选择品牌");
                if (productBrandList == null)
                {
                    productBrandList = new ArrayList<>();
                    productBrandList.add(defaultBrandMap);
                }
                defaultImgMap.put("is_center", 1);
            }
            //获取当前商品运费信息
            if (!productListModel.getCommodity_type().equals(ProductListModel.COMMODITY_TYPE.virtual))
            {
                String       fname        = "";
                FreightModel goodsFreight = new FreightModel();
                goodsFreight.setId(Integer.parseInt(productListModel.getFreight()));
                goodsFreight = freightModelMapper.selectOne(goodsFreight);
                if (goodsFreight != null)
                {
                    fname = goodsFreight.getName();
                }
                goodsFreightMap.put("id", Integer.valueOf(productListModel.getFreight()));
                goodsFreightMap.put("name", fname);
            }
            //把商品主图放到图片列表中的第一个
            if (productImgModelList == null)
            {
                productImgModelList = new ArrayList<>();
                productImgModelList.add(defaultImgMap);
            }
            else
            {
                if (productImgModelList.size() < 1)
                {
                    productImgModelList.add(defaultImgMap);
                }
                else
                {
                    productImgModelList.set(0, defaultImgMap);
                }
            }

            //规格值集
            List<Map<String, Object>> strArrList      = new ArrayList<>();
            List<Map<String, Object>> checkedAttrList = new ArrayList<>();
            List<Map<String, Object>> attrGroupList   = new ArrayList<>();

            ConfiGureModel confiGureModel = new ConfiGureModel();
            confiGureModel.setPid(goodsId);
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
                                    int    keyId       = attributeKey.lastIndexOf(SplitUtils.HG) + 1;
                                    int    valueId     = attribyteValue.indexOf(SplitUtils.UNDERLIEN) + 1;
                                    String keyIdTemp   = attributeKey.substring(keyId);
                                    String valueIdTemp = attribyteValue.substring(0, valueId);

                                    int                 skuValueId = attribyteValue.lastIndexOf(SplitUtils.UNDERLIEN) + 1;
                                    String              skuId      = attribyteValue.substring(skuValueId);
                                    Map<String, Object> skuById    = skuModelMapper.getSkuById(skuId);
//                                    Map<String, Object> dataMap = new HashMap<>(16);
//                                    dataMap.put("id0", keyIdTemp);
//                                    dataMap.put("id1", valueIdTemp);

                                    //获取名称
                                    attributeKey = attributeKey.substring(0, attributeKey.indexOf("_LKT"));
                                    attribyteValue = attribyteValue.substring(0, attribyteValue.indexOf("_LKT"));

                                    if (attributeKey.equals("默认") && attribyteValue.equals("默认"))
                                    {
                                        continue;
                                    }

                                    strArrList.add(skuById);
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
                        //获取销量
                        LivingProductModel livingProductModel = new LivingProductModel();
                        livingProductModel.setConfig_id(confiGure.getId());
                        if (StringUtils.isNotEmpty(roomId))
                        {
                            livingProductModel.setLiving_id(roomId);
                        }
                        livingProductModel.setRecycle(0);
                        List<LivingProductModel> list       = livingProductModelMapper.select(livingProductModel);
                        int                      xl_num     = 0;
                        int                      num        = 0;
                        BigDecimal               live_price = new BigDecimal(0);
                        if (list.size() > 0)
                        {
                            for (LivingProductModel productModel : list)
                            {
                                xl_num += productModel.getXl_num();
                                num += productModel.getNum();

                                live_price = productModel.getLive_price();
                            }
                        }
                        Map<String, Object> checkedAttrListMap = new HashMap<>(16);
                        checkedAttrListMap.put("attr_list", attrLists);
                        checkedAttrListMap.put("cbj", confiGure.getCostprice());
                        checkedAttrListMap.put("yj", confiGure.getYprice());
                        checkedAttrListMap.put("sj", confiGure.getPrice());
                        if (productListModel.getCommodity_type() == ProductListModel.COMMODITY_TYPE.virtual)
                        {
                            checkedAttrListMap.put("kucun", confiGure.getWrite_off_num());
                        }
                        else
                        {
                            checkedAttrListMap.put("kucun", confiGure.getNum());
                        }
                        checkedAttrListMap.put("unit", confiGure.getUnit());
                        checkedAttrListMap.put("bar_code", confiGure.getBar_code());
                        checkedAttrListMap.put("name", confiGure.getName());
                        checkedAttrListMap.put("img", publiceService.getImgPath(confiGure.getImg(), storeId));
                        checkedAttrListMap.put("cid", confiGure.getId());
                        checkedAttrListMap.put("commission", confiGure.getCommission());
                        checkedAttrListMap.put("xl_num", xl_num);
                        checkedAttrListMap.put("num", num);
                        checkedAttrListMap.put("sy_num", num - xl_num);
                        if (list.size() > 0)
                        {
                            checkedAttrListMap.put("live_price", live_price);
                        }
                        else
                        {
                            checkedAttrListMap.put("live_price", confiGure.getLive_price());
                        }
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
            //展示位置
            List<Map<String, Object>> showAdrList;
            String                    showAdr  = productListModel.getShow_adr();
            List<String>              showAdrs = new ArrayList<>();
            if (!StringUtils.isEmpty(showAdr))
            {
                showAdr = StringUtils.trim(showAdr, SplitUtils.DH);
                showAdrs = Arrays.asList(showAdr.split(SplitUtils.DH));
            }

            //获取商品展示位
            parmaMap.clear();
            parmaMap.put("name", "商品展示位置");
            parmaMap.put("status", DictionaryListModel.Status.TAKE_EFFECT);
            showAdrList = dictionaryListModelMapper.getDictionaryDynamic(parmaMap);
            for (Map<String, Object> map : showAdrList)
            {
                boolean status = false;
                String  value  = map.get("value").toString();
                String  text   = map.get("text").toString();
                map.clear();
                map.put("name", text);
                map.put("value", value);
                if (showAdrs.contains(value))
                {
                    status = true;
                }
                map.put("status", status);
            }
            //获取商品类型
            ProLabelModel proLabelModel = new ProLabelModel();
            proLabelModel.setStore_id(storeId);
            List<ProLabelModel> proLabelModelList = proLabelModelMapper.select(proLabelModel);
            for (ProLabelModel proLabel : proLabelModelList)
            {
                Map<String, Object> spTypeMap = new HashMap<>(16);
                boolean             status    = false;
                spTypeMap.put("name", proLabel.getName());
                spTypeMap.put("value", proLabel.getId());
                if (arr.contains(proLabel.getId().toString()))
                {
                    status = true;
                }
                spTypeMap.put("status", status);
                sTypeList.add(spTypeMap);
            }
            //查询商品其它信息
            Map<String, Object> distributorsMap = new HashMap<>(16);
            if (GloabConst.LktConfig.LKT_CONFIG_TYPE_PT == type)
            {
                //获取插件 暂时不做...
//                $Plugin_arr = $Plugin->product_plugin($db, $store_id, 'product', $active, $distributor_id);
            }
            else
            {
                //获取商品支持的活动类型
                List<Map<String, Object>> activeMainList = publiceService.getGoodsActive(storeId, Integer.parseInt(productListModel.getActive()));
                pluginArr.put("active", activeMainList);
                //获取商品分销等级
                String  dengjiName = "会员专区商品绑定等级";
                Integer fenxiaoId  = productListModel.getDistributor_id();
                if (fenxiaoId != null && fenxiaoId != 0)
                {
                    DistributionGradeModel distributionGradeModel = new DistributionGradeModel();
                    distributionGradeModel.setStore_id(storeId);
                    distributionGradeModel.setIs_ordinary(DictionaryConst.WhetherMaven.WHETHER_NO);
                    distributionGradeModel.setId(fenxiaoId);
                    distributionGradeModel = distributionGradeModelMapper.selectOne(distributionGradeModel);
                    if (distributionGradeModel != null)
                    {
                        Map<String, Object> setsMap = SerializePhpUtils.getDistributionGradeBySets(distributionGradeModel.getSets());
                        dengjiName = setsMap.get("s_dengjiname").toString();
                    }
                }
                distributorsMap.put("id", fenxiaoId == null ? 0 : fenxiaoId);
                distributorsMap.put("name", dengjiName);
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
                if (GloabConst.LktConfig.LKT_CONFIG_TYPE_PC == type)
                {
                    boolean             checked = false;
                    Map<String, Object> dataMap = new HashMap<>(16);
                    dataMap.put("name", unit);
                    if (initialMap != null)
                    {
                        if (initialMap.get("unit").equals(unit))
                        {
                            checked = true;
                        }
                    }
                    dataMap.put("checked", checked);
                    unitMapList.add(dataMap);
                }
                else
                {
                    units.add(unit);
                }
            }
            //预售商品信息
            PreSellGoodsModel preSellGoodsModel = new PreSellGoodsModel();
            preSellGoodsModel.setProduct_id(goodsId);
            preSellGoodsModel = preSellGoodsMapper.selectOne(preSellGoodsModel);
            if (!Objects.isNull(preSellGoodsModel))
            {
                Map<String, Object> sellGoodMap = new HashMap<>();
                sellGoodMap.put("sell_type", preSellGoodsModel.getSell_type());
                if (preSellGoodsModel.getDeposit() != null)
                {
                    sellGoodMap.put("deposit", preSellGoodsModel.getDeposit());
                }
                if (preSellGoodsModel.getPay_type() != null)
                {
                    sellGoodMap.put("payType", preSellGoodsModel.getPay_type());
                }
                if (preSellGoodsModel.getDeposit_start_time() != null)
                {
                    sellGoodMap.put("depositStartTime", DateUtil.dateFormate(preSellGoodsModel.getDeposit_start_time(), GloabConst.TimePattern.YMDHMS));
                }
                if (preSellGoodsModel.getDeposit_end_time() != null)
                {
                    sellGoodMap.put("depositEndTime", DateUtil.dateFormate(preSellGoodsModel.getDeposit_end_time(), GloabConst.TimePattern.YMDHMS));
                }
                if (preSellGoodsModel.getBalance_pay_time() != null)
                {
                    sellGoodMap.put("balancePayTime", DateUtil.dateFormate(preSellGoodsModel.getBalance_pay_time(), GloabConst.TimePattern.YMD));
                }
                if (preSellGoodsModel.getSell_num() != null)
                {
                    sellGoodMap.put("sellNum", preSellGoodsModel.getSell_num());
                }
                if (preSellGoodsModel.getSurplus_num() != null)
                {
                    sellGoodMap.put("surplusNum", preSellGoodsModel.getSurplus_num());
                }
                if (preSellGoodsModel.getEnd_day() != null)
                {
                    sellGoodMap.put("endDay", preSellGoodsModel.getEnd_day());
                }
                sellGoodMap.put("deliveryTime", preSellGoodsModel.getDelivery_time());
                resultMap.put("sellGoodInfo", sellGoodMap);
            }
            resultMap.put("mch_id", mchId);
            resultMap.put("list", productListModel);
            if (StringUtils.isNotEmpty(productListModel.getVideo()))
            {
                resultMap.put("video", publiceService.getImgPath(productListModel.getVideo(), storeId));
            }
            if (StringUtils.isNotEmpty(productListModel.getProVideo()))
            {
                resultMap.put("proVideo", publiceService.getImgPath(productListModel.getProVideo(), storeId));
            }
            if (productListModel.getCommodity_type().equals(ProductListModel.COMMODITY_TYPE.virtual))
            {
                resultMap.put("writeOffSettings", productListModel.getWrite_off_settings());
                if (!productListModel.getWrite_off_mch_ids().equals("0"))
                {
                    String[]            split             = productListModel.getWrite_off_mch_ids().split(SplitUtils.DH);
                    List<String>        ids               = Arrays.asList(split);
                    List<MchStoreModel> mchStoreModelList = mchStoreModelMapper.selectByIds(ids);
                    String              names             = mchStoreModelList.stream().map(x -> x.getName()).collect(Collectors.joining(SplitUtils.DH));
                    resultMap.put("writeOffMchName", names);
                }
                else
                {
                    resultMap.put("writeOffMchIds", productListModel.getWrite_off_mch_ids());
                    resultMap.put("writeOffMchName", "全部");
                }
                resultMap.put("isAppointment", productListModel.getIs_appointment());
            }
            resultMap.put("commodityType", productListModel.getCommodity_type());
            resultMap.put("product_title", productListModel.getProduct_title());
            resultMap.put("subtitle", productListModel.getSubtitle());
            resultMap.put("keyword", productListModel.getKeyword());
            resultMap.put("weight", productListModel.getWeight());
            resultMap.put("product_number", productListModel.getProduct_number());
            resultMap.put("class_id", classId == -1 ? null : classId);
            resultMap.put("ctypes", productClassList);
            resultMap.put("brand_class", productBrandList);
            resultMap.put("brand_id", productListModel.getBrand_id());
            resultMap.put("imgurls", productShowImgList);
            resultMap.put("initial", initialMap);
            resultMap.put("status", productListModel.getStatus());
            if (units.size() > 0)
            {
                resultMap.put("unit", units);
            }
            else
            {
                resultMap.put("unit", unitMapList);
            }
            resultMap.put("attr_group_list", attrGroupList);
            resultMap.put("checked_attr_list", checkedAttrList);
            resultMap.put("min_inventory", productListModel.getMin_inventory());
            resultMap.put("inventoryWarning", productListModel.getMin_inventory());
            resultMap.put("freight_list", freightModelList);
            resultMap.put("sp_type", sTypeList);
            resultMap.put("active", productListModel.getActive());
            resultMap.put("Plugin_arr", pluginArr);
            resultMap.put("show_adr", showAdrList);
            resultMap.put("content", productListModel.getContent());
            resultMap.put("brand_name", brandName);
            resultMap.put("brand_class_list1", brandClassModel);
            resultMap.put("freight_list1", goodsFreightMap);
            resultMap.put("distributors", null);
            resultMap.put("distributors1", distributorsMap);
            resultMap.put("status", productListModel.getStatus());
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
    public Map<String, Object> editPage(int storeId, String adminName, int mchId, int goodsId, int type) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            //运费信息 全部
            List<Map<String, Object>> freightModelList;
            //商品运费信息
            Map<String, Object> goodsFreightMap = new HashMap<>(16);
            //商品图片信息(封面图集合)
            List<String> productShowImgList = new ArrayList<>();
            //商品品牌信息 全部
            List<Map<String, Object>> productBrandList = null;

            //商品分类
            List<ProductClassModel> productClassList = new ArrayList<>();
            //插件集信息 Plugin_arr
            Map<String, Object> pluginArr = new HashMap<>(16);

            //如果是平台，则查询商品信息无视店铺
//            if (type != GloabConst.LktConfig.LKT_CONFIG_TYPE_PT) {
//                //否则加店铺id
//                productListModel.setMch_id(mchId);
//            }
            ProductListModel productListModel = productListModelMapper.selectByPrimaryKey(goodsId);
            if (productListModel != null)
            {
                //获取图片信息
                String imgUrl       = productListModel.getImgurl();
                String converMapUrl = productListModel.getCover_map();
                imgUrl = publiceService.getImgPath(imgUrl, storeId);
                productListModel.setImgurl(imgUrl);
                converMapUrl = publiceService.getImgPath(converMapUrl, storeId);
                productListModel.setCover_map(converMapUrl);
            }
            else
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SPBCZ, "商品不存在");
            }
            //获取店铺运费信息
            FreightModel freightModel = new FreightModel();
            freightModel.setStore_id(storeId);
            freightModel.setMch_id(mchId);
            freightModelList = freightModelMapper.getFreightInfo(freightModel);
            for (Map<String, Object> map : freightModelList)
            {
                boolean selected = false;
                String  fid      = map.get("id").toString();
                if (fid.equals(productListModel.getFreight()))
                {
                    selected = true;
                }
                map.put("selected", selected);
            }
            //获取商品封面图
            boolean         flag            = false;
            ProductImgModel productImgModel = new ProductImgModel();
            productImgModel.setProduct_id(goodsId);
            List<Map<String, Object>> productImgModelList = productImgModelMapper.getProductImgInfoByPid(productImgModel);

            if (GloabConst.LktConfig.LKT_CONFIG_TYPE_PT == type || GloabConst.LktConfig.LKT_CONFIG_TYPE_PC == type)
            {
                flag = true;
            }
            if (productImgModelList != null)
            {
                for (int i = 0; i < productImgModelList.size(); i++)
                {
                    Map<String, Object> map        = productImgModelList.get(i);
                    String              productUrl = map.get("product_url").toString();
                    productUrl = publiceService.getImgPath(productUrl, storeId);
                    productShowImgList.add(productUrl);
                    if (flag)
                    {
                        int isCenter = productUrl.equals(productImgModel.getProduct_url()) ? 1 : 0;
                        map.put("is_center", isCenter);
                    }
                    else
                    {
                        map.clear();
                        map.put(i + "", productUrl);
                    }
                }
            }
            //商品分类+品牌处理
            String   res     = productListModel.getProduct_class();
            //商品所属分类
            int classId    = -1;
            int classIdTop = -1;
            if (StringUtils.isNotEmpty(res))
            {
                String[] resList = res.split(SplitUtils.HG);
                //商品所属分类顶级
                classIdTop = Integer.parseInt(resList[1]);
                int classNum = resList.length - 1;
                //商品所属分类
                classId = Integer.parseInt(resList[classNum]);
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
            }

            //获取当前品牌名称
            String          brandName       = null;
            BrandClassModel brandClassModel = new BrandClassModel();
            if (productListModel.getBrand_id() != null)
            {
                brandClassModel.setBrand_id(productListModel.getBrand_id());
                brandClassModel = brandClassModelMapper.selectOne(brandClassModel);
                if (brandClassModel != null)
                {
                    brandName = brandClassModel.getBrand_name();
                }
            }

            //获取顶级牌信息
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", storeId);
            parmaMap.put("status", BrandClassModel.Status.OPEN);

            if (StringUtils.isNotEmpty(brandName) && classIdTop != -1)
            {
                parmaMap.put("categories", classIdTop);
                productBrandList = brandClassModelMapper.getBrandClassDynamic(parmaMap);
            }

            //商品主图
            Map<String, Object> defaultImgMap = new HashMap<>(16);
            defaultImgMap.put("product_url", productListModel.getImgurl());
            if (flag)
            {
                defaultImgMap.put("is_center", 1);
            }
            //获取当前商品运费信息
            if (!productListModel.getCommodity_type().equals(ProductListModel.COMMODITY_TYPE.virtual))
            {
                String       fname        = "";
                FreightModel goodsFreight = new FreightModel();
                goodsFreight.setId(Integer.parseInt(productListModel.getFreight()));
                goodsFreight = freightModelMapper.selectOne(goodsFreight);
                if (goodsFreight != null)
                {
                    fname = goodsFreight.getName();
                }
                goodsFreightMap.put("id", Integer.valueOf(productListModel.getFreight()));
                goodsFreightMap.put("name", fname);
            }
            //把商品主图放到图片列表中的第一个
            if (productImgModelList == null)
            {
                productImgModelList = new ArrayList<>();
                productImgModelList.add(defaultImgMap);
            }
            else
            {
                if (productImgModelList.size() < 1)
                {
                    productImgModelList.add(defaultImgMap);
                }
                else
                {
                    productImgModelList.set(0, defaultImgMap);
                }
            }

            //规格值集
            List<Map<String, Object>> strArrList      = new ArrayList<>();
            List<Map<String, Object>> checkedAttrList = new ArrayList<>();
            List<Map<String, Object>> attrGroupList   = new ArrayList<>();

            ConfiGureModel confiGureModel = new ConfiGureModel();
            confiGureModel.setPid(goodsId);
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
                                    int    keyId       = attributeKey.lastIndexOf(SplitUtils.HG) + 1;
                                    int    valueId     = attribyteValue.indexOf(SplitUtils.UNDERLIEN) + 1;
                                    String keyIdTemp   = attributeKey.substring(keyId);
                                    String valueIdTemp = attribyteValue.substring(0, valueId);

                                    int                 skuValueId = attribyteValue.lastIndexOf(SplitUtils.UNDERLIEN) + 1;
                                    String              skuId      = attribyteValue.substring(skuValueId);
                                    Map<String, Object> skuById    = skuModelMapper.getSkuById(skuId);
//                                    Map<String, Object> dataMap = new HashMap<>(16);
//                                    dataMap.put("id0", keyIdTemp);
//                                    dataMap.put("id1", valueIdTemp);

                                    //获取名称
                                    attributeKey = attributeKey.substring(0, attributeKey.indexOf("_LKT"));
                                    attribyteValue = attribyteValue.substring(0, attribyteValue.indexOf("_LKT"));

                                    if (attributeKey.equals("默认") && attribyteValue.equals("默认"))
                                    {
                                        continue;
                                    }

                                    strArrList.add(skuById);
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
                        //获取销量
                        LivingProductModel livingProductModel = new LivingProductModel();
                        livingProductModel.setConfig_id(confiGure.getId());
                        livingProductModel.setRecycle(0);
                        List<LivingProductModel> list   = livingProductModelMapper.select(livingProductModel);
                        int                      xl_num = 0;
                        int                      num    = 0;
                        if (list.size() > 0)
                        {
                            for (LivingProductModel productModel : list)
                            {
                                xl_num += productModel.getXl_num();
                                num += productModel.getNum();
                            }
                        }
                        Map<String, Object> checkedAttrListMap = new HashMap<>(16);
                        checkedAttrListMap.put("attr_list", attrLists);
                        checkedAttrListMap.put("cbj", confiGure.getCostprice());
                        checkedAttrListMap.put("yj", confiGure.getYprice());
                        checkedAttrListMap.put("sj", confiGure.getPrice());
                        if (productListModel.getCommodity_type() == ProductListModel.COMMODITY_TYPE.virtual)
                        {
                            checkedAttrListMap.put("kucun", confiGure.getWrite_off_num());
                        }
                        else
                        {
                            checkedAttrListMap.put("kucun", confiGure.getNum());
                        }
                        checkedAttrListMap.put("unit", confiGure.getUnit());
                        checkedAttrListMap.put("bar_code", confiGure.getBar_code());
                        checkedAttrListMap.put("name", confiGure.getName());
                        checkedAttrListMap.put("img", publiceService.getImgPath(confiGure.getImg(), storeId));
                        checkedAttrListMap.put("cid", confiGure.getId());
                        checkedAttrListMap.put("commission", confiGure.getCommission());
                        checkedAttrListMap.put("xl_num", xl_num);
                        checkedAttrListMap.put("num", num);
                        checkedAttrListMap.put("sy_num", num - xl_num);
                        checkedAttrListMap.put("live_price", confiGure.getLive_price());
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
            //展示位置
            List<Map<String, Object>> showAdrList;
            String                    showAdr  = productListModel.getShow_adr();
            List<String>              showAdrs = new ArrayList<>();
            if (!StringUtils.isEmpty(showAdr))
            {
                showAdr = StringUtils.trim(showAdr, SplitUtils.DH);
                showAdrs = Arrays.asList(showAdr.split(SplitUtils.DH));
            }

            //获取商品展示位
            parmaMap.clear();
            parmaMap.put("name", "商品展示位置");
            parmaMap.put("status", DictionaryListModel.Status.TAKE_EFFECT);
            showAdrList = dictionaryListModelMapper.getDictionaryDynamic(parmaMap);
            for (Map<String, Object> map : showAdrList)
            {
                boolean status = false;
                String  value  = map.get("value").toString();
                String  text   = map.get("text").toString();
                map.clear();
                map.put("name", text);
                map.put("value", value);
                if (showAdrs.contains(value))
                {
                    status = true;
                }
                map.put("status", status);
            }
            //获取商品类型
            ProLabelModel proLabelModel = new ProLabelModel();
            proLabelModel.setStore_id(storeId);
            List<ProLabelModel> proLabelModelList = proLabelModelMapper.select(proLabelModel);
            for (ProLabelModel proLabel : proLabelModelList)
            {
                Map<String, Object> spTypeMap = new HashMap<>(16);
                boolean             status    = false;
                spTypeMap.put("name", proLabel.getName());
                spTypeMap.put("value", proLabel.getId());
                if (arr.contains(proLabel.getId().toString()))
                {
                    status = true;
                }
                spTypeMap.put("status", status);
                sTypeList.add(spTypeMap);
            }
            //查询商品其它信息
            Map<String, Object> distributorsMap = new HashMap<>(16);
            if (GloabConst.LktConfig.LKT_CONFIG_TYPE_PT == type)
            {
                //获取插件 暂时不做...
//                $Plugin_arr = $Plugin->product_plugin($db, $store_id, 'product', $active, $distributor_id);
            }
            else
            {
                //获取商品支持的活动类型
                List<Map<String, Object>> activeMainList = publiceService.getGoodsActive(storeId, Integer.parseInt(productListModel.getActive()));
                pluginArr.put("active", activeMainList);
                //获取商品分销等级
                String  dengjiName = "会员专区商品绑定等级";
                Integer fenxiaoId  = productListModel.getDistributor_id();
                if (fenxiaoId != null && fenxiaoId != 0)
                {
                    DistributionGradeModel distributionGradeModel = new DistributionGradeModel();
                    distributionGradeModel.setStore_id(storeId);
                    distributionGradeModel.setIs_ordinary(DictionaryConst.WhetherMaven.WHETHER_NO);
                    distributionGradeModel.setId(fenxiaoId);
                    distributionGradeModel = distributionGradeModelMapper.selectOne(distributionGradeModel);
                    if (distributionGradeModel != null)
                    {
                        Map<String, Object> setsMap = SerializePhpUtils.getDistributionGradeBySets(distributionGradeModel.getSets());
                        dengjiName = setsMap.get("s_dengjiname").toString();
                    }
                }
                distributorsMap.put("id", fenxiaoId == null ? 0 : fenxiaoId);
                distributorsMap.put("name", dengjiName);
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
            //TODO 单位
//            parmaMap.put("lang_code",)
            List<Map<String, Object>> unitList = dictionaryListModelMapper.getDictionaryDynamic(parmaMap);
            for (Map<String, Object> map : unitList)
            {
                String unit = map.get("text").toString();
                if (GloabConst.LktConfig.LKT_CONFIG_TYPE_PC == type)
                {
                    boolean             checked = false;
                    Map<String, Object> dataMap = new HashMap<>(16);
                    dataMap.put("name", unit);
                    if (initialMap != null)
                    {
                        if (initialMap.get("unit").equals(unit))
                        {
                            checked = true;
                        }
                    }
                    dataMap.put("checked", checked);
                    unitMapList.add(dataMap);
                }
                else
                {
                    units.add(unit);
                }
            }
            //预售商品信息
            PreSellGoodsModel preSellGoodsModel = new PreSellGoodsModel();
            preSellGoodsModel.setProduct_id(goodsId);
            preSellGoodsModel = preSellGoodsMapper.selectOne(preSellGoodsModel);
            if (!Objects.isNull(preSellGoodsModel))
            {
                Map<String, Object> sellGoodMap = new HashMap<>();
                sellGoodMap.put("sell_type", preSellGoodsModel.getSell_type());
                if (preSellGoodsModel.getDeposit() != null)
                {
                    sellGoodMap.put("deposit", preSellGoodsModel.getDeposit());
                }
                if (preSellGoodsModel.getPay_type() != null)
                {
                    sellGoodMap.put("payType", preSellGoodsModel.getPay_type());
                }
                if (preSellGoodsModel.getDeposit_start_time() != null)
                {
                    sellGoodMap.put("depositStartTime", DateUtil.dateFormate(preSellGoodsModel.getDeposit_start_time(), GloabConst.TimePattern.YMDHMS));
                }
                if (preSellGoodsModel.getDeposit_end_time() != null)
                {
                    sellGoodMap.put("depositEndTime", DateUtil.dateFormate(preSellGoodsModel.getDeposit_end_time(), GloabConst.TimePattern.YMDHMS));
                }
                if (preSellGoodsModel.getBalance_pay_time() != null)
                {
                    sellGoodMap.put("balancePayTime", DateUtil.dateFormate(preSellGoodsModel.getBalance_pay_time(), GloabConst.TimePattern.YMD));
                }
                if (preSellGoodsModel.getSell_num() != null)
                {
                    sellGoodMap.put("sellNum", preSellGoodsModel.getSell_num());
                }
                if (preSellGoodsModel.getSurplus_num() != null)
                {
                    sellGoodMap.put("surplusNum", preSellGoodsModel.getSurplus_num());
                }
                if (preSellGoodsModel.getEnd_day() != null)
                {
                    sellGoodMap.put("endDay", preSellGoodsModel.getEnd_day());
                }
                sellGoodMap.put("deliveryTime", preSellGoodsModel.getDelivery_time());
                resultMap.put("sellGoodInfo", sellGoodMap);
            }
            resultMap.put("mch_id", mchId);
            resultMap.put("list", productListModel);
            if (StringUtils.isNotEmpty(productListModel.getVideo()))
            {
                resultMap.put("video", publiceService.getImgPath(productListModel.getVideo(), storeId));
            }
            if (StringUtils.isNotEmpty(productListModel.getProVideo()))
            {
                resultMap.put("proVideo", publiceService.getImgPath(productListModel.getProVideo(), storeId));
            }
            if (productListModel.getCommodity_type().equals(ProductListModel.COMMODITY_TYPE.virtual))
            {
                resultMap.put("writeOffSettings", productListModel.getWrite_off_settings());
                if (!productListModel.getWrite_off_mch_ids().equals("0"))
                {
                    String[]            split             = productListModel.getWrite_off_mch_ids().split(SplitUtils.DH);
                    List<String>        ids               = Arrays.asList(split);
                    List<MchStoreModel> mchStoreModelList = mchStoreModelMapper.selectByIds(ids);
                    String              names             = mchStoreModelList.stream().map(x -> x.getName()).collect(Collectors.joining(SplitUtils.DH));
                    resultMap.put("writeOffMchName", names);
                }
                else
                {
                    resultMap.put("writeOffMchIds", productListModel.getWrite_off_mch_ids());
                    resultMap.put("writeOffMchName", "全部");
                }
                resultMap.put("isAppointment", productListModel.getIs_appointment());
            }
            resultMap.put("commodityType", productListModel.getCommodity_type());
            resultMap.put("product_title", productListModel.getProduct_title());
            resultMap.put("subtitle", productListModel.getSubtitle());
            resultMap.put("keyword", productListModel.getKeyword());
            resultMap.put("weight", productListModel.getWeight());
            resultMap.put("product_number", productListModel.getProduct_number());
            resultMap.put("class_id", classId == -1 ? null : classId);
            resultMap.put("ctypes", productClassList);
            resultMap.put("brand_class", productBrandList);
            resultMap.put("brand_id", productListModel.getBrand_id());
            resultMap.put("imgurls", productShowImgList);
            resultMap.put("initial", initialMap);
            resultMap.put("status", productListModel.getStatus());
            resultMap.put("country_num", productListModel.getCountry_num());
            if (StringUtils.isNotEmpty(productListModel.getCountry_num())) {
                String countryName = publiceService.getCountryName(productListModel.getCountry_num());
                resultMap.put("country_name", countryName);
            }
            resultMap.put("lang_code", productListModel.getLang_code());
            if (StringUtils.isNotEmpty(productListModel.getLang_code())) {
                String langName = publiceService.getLangName(productListModel.getLang_code());
                resultMap.put("lang_name", langName);
            }

            if (productListModel.getLang_pid() != null)
            {
                resultMap.put("lang_pid", productListModel.getLang_pid());
            }

            if (units.size() > 0)
            {
                resultMap.put("unit", units);
            }
            else
            {
                resultMap.put("unit", unitMapList);
            }
            resultMap.put("attr_group_list", attrGroupList);
            resultMap.put("checked_attr_list", checkedAttrList);
            resultMap.put("min_inventory", productListModel.getMin_inventory());
            resultMap.put("inventoryWarning", productListModel.getMin_inventory());
            resultMap.put("freight_list", freightModelList);
            resultMap.put("sp_type", sTypeList);
            resultMap.put("active", productListModel.getActive());
            resultMap.put("Plugin_arr", pluginArr);
            resultMap.put("show_adr", showAdrList);
            resultMap.put("content", productListModel.getContent());
            resultMap.put("brand_name", brandName);
            resultMap.put("brand_class_list1", brandClassModel);
            resultMap.put("freight_list1", goodsFreightMap);
            resultMap.put("distributors", null);
            resultMap.put("distributors1", distributorsMap);
            resultMap.put("status", productListModel.getStatus());
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
    public Map<String, Object> editPage(MainVo vo, String adminName, int mchId, int goodsId, int type) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        int                 storeId   = vo.getStoreId();
        try
        {
            //运费信息 全部
            List<Map<String, Object>> freightModelList;
            //商品运费信息
            Map<String, Object> goodsFreightMap = new HashMap<>(16);
            //商品图片信息(封面图集合)
            List<String> productShowImgList = new ArrayList<>();
            //商品品牌信息 全部
            List<Map<String, Object>> productBrandList = null;

            //商品分类
            List<ProductClassModel> productClassList = new ArrayList<>();
            //插件集信息 Plugin_arr
            Map<String, Object> pluginArr = new HashMap<>(16);

            //如果是平台，则查询商品信息无视店铺
//            if (type != GloabConst.LktConfig.LKT_CONFIG_TYPE_PT) {
//                //否则加店铺id
//                productListModel.setMch_id(mchId);
//            }
            ProductListModel productListModel = productListModelMapper.selectByPrimaryKey(goodsId);
            if (productListModel != null)
            {
                //获取图片信息
                String imgUrl       = productListModel.getImgurl();
                String converMapUrl = productListModel.getCover_map();
                imgUrl = publiceService.getImgPath(imgUrl, storeId);
                productListModel.setImgurl(imgUrl);
                converMapUrl = publiceService.getImgPath(converMapUrl, storeId);
                productListModel.setCover_map(converMapUrl);
            }
            else
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SPBCZ, "商品不存在");
            }
            //不会为空
            String langCode = vo.getLang_code();
            //获取店铺运费信息
            FreightModel freightModel = new FreightModel();
            freightModel.setStore_id(storeId);
            freightModel.setMch_id(mchId);
            freightModel.setLang_code(langCode);
            freightModelList = freightModelMapper.getFreightInfo(freightModel);
            for (Map<String, Object> map : freightModelList)
            {
                boolean selected = false;
                String  fid      = map.get("id").toString();
                if (fid.equals(productListModel.getFreight()))
                {
                    selected = true;
                }
                map.put("selected", selected);
            }
            //获取商品封面图
            boolean         flag            = false;
            ProductImgModel productImgModel = new ProductImgModel();
            productImgModel.setProduct_id(goodsId);
            List<Map<String, Object>> productImgModelList = productImgModelMapper.getProductImgInfoByPid(productImgModel);
            if (GloabConst.LktConfig.LKT_CONFIG_TYPE_PT == type || GloabConst.LktConfig.LKT_CONFIG_TYPE_PC == type)
            {
                flag = true;
            }
            if (productImgModelList != null)
            {
                for (int i = 0; i < productImgModelList.size(); i++)
                {
                    Map<String, Object> map        = productImgModelList.get(i);
                    String              productUrl = map.get("product_url").toString();
                    productUrl = publiceService.getImgPath(productUrl, storeId);
                    productShowImgList.add(productUrl);
                    if (flag)
                    {
                        int isCenter = productUrl.equals(productImgModel.getProduct_url()) ? 1 : 0;
                        map.put("is_center", isCenter);
                    }
                    else
                    {
                        map.clear();
                        map.put(i + "", productUrl);
                    }
                }
            }

            //商品分类+品牌处理
            String   res     = productListModel.getProduct_class();
            //商品所属分类
            int classId    = -1;
            int classIdTop = -1;
            if (StringUtils.isNotEmpty(res))
            {
                String[] resList = res.split(SplitUtils.HG);
                //商品所属分类顶级
                classIdTop = Integer.parseInt(resList[1]);
                int classNum = resList.length - 1;
                //商品所属分类
                classId = Integer.parseInt(resList[classNum]);
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
            }

            //获取当前品牌名称
            String          brandName       = null;
            BrandClassModel brandClassModel = new BrandClassModel();
            if (productListModel.getBrand_id() != null)
            {
                brandClassModel.setBrand_id(productListModel.getBrand_id());
                brandClassModel = brandClassModelMapper.selectOne(brandClassModel);
                if (brandClassModel != null)
                {
                    brandName = brandClassModel.getBrand_name();
                }
            }

            //获取顶级牌信息
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", storeId);
            parmaMap.put("status", BrandClassModel.Status.OPEN);
            parmaMap.put("lang_code", langCode);
            if (StringUtils.isNotEmpty(brandName) && classIdTop != -1)
            {
                parmaMap.put("categories", classIdTop);
                productBrandList = brandClassModelMapper.getBrandClassDynamic(parmaMap);
            }

            //商品主图
            Map<String, Object> defaultImgMap = new HashMap<>(16);
            defaultImgMap.put("product_url", productListModel.getImgurl());
            if (flag)
            {
                //品牌默认值
                Map<String, Object> defaultBrandMap = new HashMap<>(16);
                defaultBrandMap.put("brand_id", 0);
                defaultBrandMap.put("brand_name", "请选择品牌");
                if (productBrandList == null)
                {
                    productBrandList = new ArrayList<>();
                    productBrandList.add(defaultBrandMap);
                }
                defaultImgMap.put("is_center", 1);
            }
            //获取当前商品运费信息
            if (!productListModel.getCommodity_type().equals(ProductListModel.COMMODITY_TYPE.virtual))
            {
                String       fname        = "";
                FreightModel goodsFreight = new FreightModel();
                if (StringUtils.isNotEmpty(productListModel.getFreight()))
                {
                    goodsFreight.setId(Integer.parseInt(productListModel.getFreight()));
                    goodsFreight = freightModelMapper.selectOne(goodsFreight);
                    if (goodsFreight != null)
                    {
                        fname = goodsFreight.getName();
                    }
                    goodsFreightMap.put("id", Integer.valueOf(productListModel.getFreight()));
                    goodsFreightMap.put("name", fname);
                }
            }
            //把商品主图放到图片列表中的第一个
            if (productImgModelList == null)
            {
                productImgModelList = new ArrayList<>();
                productImgModelList.add(defaultImgMap);
            }
            else
            {
                if (productImgModelList.size() < 1)
                {
                    productImgModelList.add(defaultImgMap);
                }
                else
                {
                    productImgModelList.set(0, defaultImgMap);
                }
            }

            //规格值集
            List<Map<String, Object>> strArrList      = new ArrayList<>();
            List<Map<String, Object>> checkedAttrList = new ArrayList<>();
            List<Map<String, Object>> attrGroupList   = new ArrayList<>();

            ConfiGureModel confiGureModel = new ConfiGureModel();
            confiGureModel.setPid(goodsId);
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
                                    int    keyId       = attributeKey.lastIndexOf(SplitUtils.HG) + 1;
                                    int    valueId     = attribyteValue.indexOf(SplitUtils.UNDERLIEN) + 1;
                                    String keyIdTemp   = attributeKey.substring(keyId);
                                    String valueIdTemp = attribyteValue.substring(0, valueId);

                                    int                 skuValueId = attribyteValue.lastIndexOf(SplitUtils.UNDERLIEN) + 1;
                                    String              skuId      = attribyteValue.substring(skuValueId);
                                    Map<String, Object> skuById    = skuModelMapper.getSkuById(skuId);
//                                    Map<String, Object> dataMap = new HashMap<>(16);
//                                    dataMap.put("id0", keyIdTemp);
//                                    dataMap.put("id1", valueIdTemp);

                                    //获取名称
                                    attributeKey = attributeKey.substring(0, attributeKey.indexOf("_LKT"));
                                    attribyteValue = attribyteValue.substring(0, attribyteValue.indexOf("_LKT"));

                                    if (attributeKey.equals("默认") && attribyteValue.equals("默认"))
                                    {
                                        continue;
                                    }

                                    strArrList.add(skuById);
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
                        //获取销量
                        LivingProductModel livingProductModel = new LivingProductModel();
                        livingProductModel.setConfig_id(confiGure.getId());
                        livingProductModel.setRecycle(0);
                        List<LivingProductModel> list   = livingProductModelMapper.select(livingProductModel);
                        int                      xl_num = 0;
                        int                      num    = 0;
                        if (list.size() > 0)
                        {
                            for (LivingProductModel productModel : list)
                            {
                                xl_num += productModel.getXl_num();
                                num += productModel.getNum();
                            }
                        }
                        BigDecimal cbj = BigDecimal.ZERO;
                        BigDecimal yj = BigDecimal.ZERO;
                        Map<String, Object> checkedAttrListMap = new HashMap<>(16);
                        //供应商商品上级规格id
                        if (Objects.nonNull(confiGure.getSupplier_superior())) {
                            ConfiGureModel suppLierConfiGureModel = confiGureModelMapper.selectByPrimaryKey(confiGure.getSupplier_superior());
                            cbj = suppLierConfiGureModel.getYprice();
                            yj = suppLierConfiGureModel.getPrice();
                        } else {
                            cbj = confiGure.getCostprice();
                            yj = confiGure.getYprice();
                        }

                        checkedAttrListMap.put("attr_list", attrLists);
                        checkedAttrListMap.put("cbj", cbj);
                        checkedAttrListMap.put("yj", yj);
                        checkedAttrListMap.put("sj", confiGure.getPrice());
                        if (productListModel.getCommodity_type() == ProductListModel.COMMODITY_TYPE.virtual)
                        {
                            checkedAttrListMap.put("kucun", confiGure.getWrite_off_num());
                        }
                        else
                        {
                            checkedAttrListMap.put("kucun", confiGure.getNum());
                        }
                        checkedAttrListMap.put("unit", confiGure.getUnit());
                        checkedAttrListMap.put("bar_code", confiGure.getBar_code());
                        checkedAttrListMap.put("name", confiGure.getName());
                        checkedAttrListMap.put("img", publiceService.getImgPath(confiGure.getImg(), storeId));
                        checkedAttrListMap.put("cid", confiGure.getId());
                        checkedAttrListMap.put("commission", confiGure.getCommission());
                        checkedAttrListMap.put("xl_num", xl_num);
                        checkedAttrListMap.put("num", num);
                        checkedAttrListMap.put("sy_num", num - xl_num);
                        checkedAttrListMap.put("live_price", confiGure.getLive_price());
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
            //展示位置
            List<Map<String, Object>> showAdrList;
            String                    showAdr  = productListModel.getShow_adr();
            List<String>              showAdrs = new ArrayList<>();
            if (!StringUtils.isEmpty(showAdr))
            {
                showAdr = StringUtils.trim(showAdr, SplitUtils.DH);
                showAdrs = Arrays.asList(showAdr.split(SplitUtils.DH));
            }

            //获取商品展示位
            parmaMap.clear();
            parmaMap.put("name", "商品展示位置");
            parmaMap.put("status", DictionaryListModel.Status.TAKE_EFFECT);
            parmaMap.put("lang_code", langCode);
            showAdrList = dictionaryListModelMapper.getDictionaryDynamic(parmaMap);
            for (Map<String, Object> map : showAdrList)
            {
                boolean status = false;
                String  value  = map.get("value").toString();
                String  text   = map.get("text").toString();
                map.clear();
                map.put("name", text);
                map.put("value", value);
                if (showAdrs.contains(value))
                {
                    status = true;
                }
                map.put("status", status);
            }
            //获取商品类型
            ProLabelModel proLabelModel = new ProLabelModel();
            proLabelModel.setStore_id(storeId);
            proLabelModel.setLang_code(langCode);
            List<ProLabelModel> proLabelModelList = proLabelModelMapper.select(proLabelModel);
            for (ProLabelModel proLabel : proLabelModelList)
            {
                Map<String, Object> spTypeMap = new HashMap<>(16);
                boolean             status    = false;
                spTypeMap.put("name", proLabel.getName());
                spTypeMap.put("value", proLabel.getId());
                if (arr.contains(proLabel.getId().toString()))
                {
                    status = true;
                }
                spTypeMap.put("status", status);
                sTypeList.add(spTypeMap);
            }
            //查询商品其它信息
            Map<String, Object> distributorsMap = new HashMap<>(16);
            if (GloabConst.LktConfig.LKT_CONFIG_TYPE_PT == type)
            {
                //获取插件 暂时不做...
//                $Plugin_arr = $Plugin->product_plugin($db, $store_id, 'product', $active, $distributor_id);
            }
            else
            {
                //获取商品支持的活动类型
                List<Map<String, Object>> activeMainList = publiceService.getGoodsActive(storeId, Integer.parseInt(productListModel.getActive()));
                pluginArr.put("active", activeMainList);
                //获取商品分销等级
                String  dengjiName = "会员专区商品绑定等级";
                Integer fenxiaoId  = productListModel.getDistributor_id();
                if (fenxiaoId != null && fenxiaoId != 0)
                {
                    DistributionGradeModel distributionGradeModel = new DistributionGradeModel();
                    distributionGradeModel.setStore_id(storeId);
                    distributionGradeModel.setIs_ordinary(DictionaryConst.WhetherMaven.WHETHER_NO);
                    distributionGradeModel.setId(fenxiaoId);
                    distributionGradeModel = distributionGradeModelMapper.selectOne(distributionGradeModel);
                    if (distributionGradeModel != null)
                    {
                        Map<String, Object> setsMap = SerializePhpUtils.getDistributionGradeBySets(distributionGradeModel.getSets());
                        dengjiName = setsMap.get("s_dengjiname").toString();
                    }
                }
                distributorsMap.put("id", fenxiaoId == null ? 0 : fenxiaoId);
                distributorsMap.put("name", dengjiName);
            }
            //获取商品初始值
            String initialStr = productListModel.getInitial();
            String initial = null;

            if (Objects.nonNull(productListModel.getSupplier_superior())) {
                Integer superior = productListModel.getSupplier_superior();
                initial = productListModelMapper.selectByPrimaryKey(superior).getInitial();
            }
            Map<String, String> initialMap = DataUtils.cast(SerializePhpUtils.getUnserializeObj(initialStr, Map.class));
            if (initialMap != null)
            {
                if (StringUtils.isNotEmpty(initial)) {
                    Map<String, String> stringMap = DataUtils.cast(SerializePhpUtils.getUnserializeObj(initial, Map.class));
                    initialMap.put("msrp", stringMap.get("sj"));
                    initialMap.put("yj", stringMap.get("yj"));
                }
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
            parmaMap.put("lang_code", langCode);
            parmaMap.put("status", DictionaryListModel.Status.TAKE_EFFECT);
            List<Map<String, Object>> unitList = dictionaryListModelMapper.getDictionaryDynamic(parmaMap);
            for (Map<String, Object> map : unitList)
            {
                String unit = map.get("text").toString();
                if (GloabConst.LktConfig.LKT_CONFIG_TYPE_PC == type)
                {
                    boolean             checked = false;
                    Map<String, Object> dataMap = new HashMap<>(16);
                    dataMap.put("name", unit);
                    if (initialMap != null)
                    {
                        if (initialMap.get("unit").equals(unit))
                        {
                            checked = true;
                        }
                    }
                    dataMap.put("checked", checked);
                    unitMapList.add(dataMap);
                }
                else
                {
                    units.add(unit);
                }
            }
            //预售商品信息
            PreSellGoodsModel preSellGoodsModel = new PreSellGoodsModel();
            preSellGoodsModel.setProduct_id(goodsId);
            preSellGoodsModel = preSellGoodsMapper.selectOne(preSellGoodsModel);
            if (!Objects.isNull(preSellGoodsModel))
            {
                Map<String, Object> sellGoodMap = new HashMap<>();
                sellGoodMap.put("sell_type", preSellGoodsModel.getSell_type());
                if (preSellGoodsModel.getDeposit() != null)
                {
                    sellGoodMap.put("deposit", preSellGoodsModel.getDeposit());
                }
                if (preSellGoodsModel.getPay_type() != null)
                {
                    sellGoodMap.put("payType", preSellGoodsModel.getPay_type());
                }
                if (preSellGoodsModel.getDeposit_start_time() != null)
                {
                    sellGoodMap.put("depositStartTime", DateUtil.dateFormate(preSellGoodsModel.getDeposit_start_time(), GloabConst.TimePattern.YMDHMS));
                }
                if (preSellGoodsModel.getDeposit_end_time() != null)
                {
                    sellGoodMap.put("depositEndTime", DateUtil.dateFormate(preSellGoodsModel.getDeposit_end_time(), GloabConst.TimePattern.YMDHMS));
                }
                if (preSellGoodsModel.getBalance_pay_time() != null)
                {
                    sellGoodMap.put("balancePayTime", DateUtil.dateFormate(preSellGoodsModel.getBalance_pay_time(), GloabConst.TimePattern.YMD));
                }
                if (preSellGoodsModel.getSell_num() != null)
                {
                    sellGoodMap.put("sellNum", preSellGoodsModel.getSell_num());
                }
                if (preSellGoodsModel.getSurplus_num() != null)
                {
                    sellGoodMap.put("surplusNum", preSellGoodsModel.getSurplus_num());
                }
                if (preSellGoodsModel.getEnd_day() != null)
                {
                    sellGoodMap.put("endDay", preSellGoodsModel.getEnd_day());
                }
                sellGoodMap.put("deliveryTime", preSellGoodsModel.getDelivery_time());
                resultMap.put("sellGoodInfo", sellGoodMap);
            }
            resultMap.put("mch_id", mchId);
            resultMap.put("list", productListModel);
            if (StringUtils.isNotEmpty(productListModel.getVideo()))
            {
                resultMap.put("video", publiceService.getImgPath(productListModel.getVideo(), storeId));
            }
            if (StringUtils.isNotEmpty(productListModel.getProVideo()))
            {
                resultMap.put("proVideo", publiceService.getImgPath(productListModel.getProVideo(), storeId));
            }
            if (productListModel.getCommodity_type().equals(ProductListModel.COMMODITY_TYPE.virtual))
            {
                resultMap.put("writeOffSettings", productListModel.getWrite_off_settings());
                if (!productListModel.getWrite_off_mch_ids().equals("0"))
                {
                    String[]            split             = productListModel.getWrite_off_mch_ids().split(SplitUtils.DH);
                    List<String>        ids               = Arrays.asList(split);
                    List<MchStoreModel> mchStoreModelList = mchStoreModelMapper.selectByIds(ids);
                    String              names             = mchStoreModelList.stream().map(x -> x.getName()).collect(Collectors.joining(SplitUtils.DH));
                    resultMap.put("writeOffMchName", names);
                }
                else
                {
                    resultMap.put("writeOffMchIds", productListModel.getWrite_off_mch_ids());
                    resultMap.put("writeOffMchName", "全部");
                }
                resultMap.put("isAppointment", productListModel.getIs_appointment());
            }
            resultMap.put("commodityType", productListModel.getCommodity_type());
            resultMap.put("product_title", productListModel.getProduct_title());
            resultMap.put("subtitle", productListModel.getSubtitle());
            resultMap.put("keyword", productListModel.getKeyword());
            resultMap.put("weight", productListModel.getWeight());
            resultMap.put("product_number", productListModel.getProduct_number());
            resultMap.put("class_id", classId == -1 ? null : classId);
            resultMap.put("ctypes", productClassList);
            resultMap.put("brand_class", productBrandList);
            resultMap.put("brand_id", productListModel.getBrand_id());
            resultMap.put("imgurls", productShowImgList);
            resultMap.put("initial", initialMap);
            resultMap.put("status", productListModel.getStatus());
            resultMap.put("country_num", productListModel.getCountry_num());
            resultMap.put("lang_code", productListModel.getLang_code());

            if (productListModel.getLang_pid() != null)
            {
                resultMap.put("lang_pid", productListModel.getLang_pid());
            }

            if (units.size() > 0)
            {
                resultMap.put("unit", units);
            }
            else
            {
                resultMap.put("unit", unitMapList);
            }
            resultMap.put("attr_group_list", attrGroupList);
            resultMap.put("checked_attr_list", checkedAttrList);
            resultMap.put("min_inventory", productListModel.getMin_inventory());
            resultMap.put("inventoryWarning", productListModel.getMin_inventory());
            resultMap.put("freight_list", freightModelList);
            resultMap.put("sp_type", sTypeList);
            resultMap.put("active", productListModel.getActive());
            resultMap.put("Plugin_arr", pluginArr);
            resultMap.put("show_adr", showAdrList);
            resultMap.put("content", productListModel.getContent());
            resultMap.put("brand_name", brandName);
            resultMap.put("brand_class_list1", brandClassModel);
            resultMap.put("freight_list1", goodsFreightMap);
            resultMap.put("distributors", null);
            resultMap.put("distributors1", distributorsMap);
            resultMap.put("status", productListModel.getStatus());
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
    public Map<String, Object> productList(int storeId, String adminName, int mchId, int type, Map<String, Object> map) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            //结果集
            List<Map<String, Object>> list = new ArrayList<>();

            int    pageNo   = Integer.parseInt(map.get("page").toString());
            int    pageSize = Integer.parseInt(map.get("pagesize").toString());
            Page   page     = Page.newBuilder(pageNo, pageSize, null);
            String pageTo   = "";
            if (map.containsKey("pageto"))
            {
                pageTo = map.get("pageto").toString();
            }

            //分类 当前级别id+上级id : 当前级别名称
            Map<String, Object> productClassMap = new HashMap<>(16);
            //品牌
            Map<Integer, Object> brandClassMap = new HashMap<>(16);

            //分类集
            List<Map<String, Object>> productClassList = new ArrayList<>();
            //品牌集
            List<Map<String, Object>> brandList = new ArrayList<>();

            String langCode = MapUtils.getString(map, "lang_code");


            //获取分类下拉
            ProductClassModel productClassModel = new ProductClassModel();
            productClassModel.setStore_id(storeId);
            productClassModel.setSid(0);
            if (StringUtils.isNotEmpty(langCode))
            {
                productClassModel.setLang_code(langCode);
            }

            //获取所有分类 数据结构 -3-31-191-
            List<ProductClassModel> productClassModelAll = productClassModelMapper.getProductClassLevel(productClassModel);
            for (ProductClassModel productClassAll : productClassModelAll)
            {
                String coneCid = SplitUtils.HG + productClassAll.getCid() + SplitUtils.HG;
                productClassMap.put(coneCid, productClassAll.getPname());
                //获取第一级
                productClassModel.setSid(productClassAll.getCid());
                List<ProductClassModel> productClassModeLeve1 = productClassModelMapper.getProductClassLevel(productClassModel);
                for (ProductClassModel productClassLeve1 : productClassModeLeve1)
                {
                    coneCid += productClassAll.getCid() + SplitUtils.HG;
                    productClassMap.put(coneCid, productClassLeve1.getPname());
                    //获取第二级
                    productClassModel.setSid(productClassLeve1.getCid());
                    List<ProductClassModel> productClassModeLeve2 = productClassModelMapper.getProductClassLevel(productClassModel);
                    for (ProductClassModel productClass2 : productClassModeLeve2)
                    {
                        coneCid += productClass2.getCid() + SplitUtils.HG;
                        productClassMap.put(coneCid, productClass2.getPname());
                    }
                }
            }
            //获取品牌下拉
            BrandClassModel brandClassModel = new BrandClassModel();
            brandClassModel.setStore_id(storeId);
            brandClassModel.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS);
            if (StringUtils.isNotEmpty(langCode))
            {
                brandClassModel.setLang_code(langCode);
            }
            List<BrandClassModel> brandClassModelList = brandClassModelMapper.select(brandClassModel);
            for (BrandClassModel brandClass : brandClassModelList)
            {
                brandClassMap.put(brandClass.getBrand_id(), brandClass.getBrand_name());
            }

            //sql参数列表 导出全部
            Map<String, Object> parmaMap1 = new HashMap<>(16);
            //sql参数列表
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", storeId);
            if (map.containsKey("commodity_type"))
            {
                parmaMap.put("commodity_type", map.get("commodity_type"));
            }

            if (type == GloabConst.LktConfig.LKT_CONFIG_TYPE_PT)
            {
                //1、先按照状态排序，优先显示待上架商品，待上架商品则按照发布时间排序；2、已上架和已下架商品则按照上架时间排序
                //禅道 40028 40292 del
                //parmaMap.put("diy_sort", DataUtils.Sort.DESC.toString());
                parmaMap.put("goods_asc_sort", DataUtils.Sort.ASC.toString());
                if (map.containsKey("mchStatus"))
                {
                    //商品类型 虚拟/实物
                    if (map.containsKey("commodity_type"))
                    {
                        parmaMap.put("commodity_type", map.get("commodity_type"));
                    }
                    int mchStatus = Integer.parseInt(map.get("mchStatus").toString());
                    if (mchStatus != 0)
                    {
                        parmaMap.put("mch_status", mchStatus);
                    }
                }
            }
            else
            {
                parmaMap.put("mch_id", mchId);
                if (type == GloabConst.LktConfig.LKT_CONFIG_TYPE_PC)
                {
                    parmaMap.put("mch_diy_sort", DataUtils.Sort.DESC.toString());
                    //todo-meger-plugin-comm
                    // parmaMap.put("diy_sort", DataUtils.Sort.DESC.toString());
                    //禅道 56652 暂时不显示虚拟商品
                    parmaMap.put("commodity_type", ProductListModel.COMMODITY_TYPE.InKind);
                }
                else
                {
                    parmaMap.put("sort_sort", DataUtils.Sort.DESC.toString());
                }
                String mchStatusTemp = map.get("mch_status") + "";
                if (!StringUtils.isEmpty(mchStatusTemp))
                {
                    //todo-meger-plugin-comm
                    //parmaMap.put("mch_id", mchId);
                    //商品类型 虚拟/实物
                    if (map.containsKey("commodity_type"))
                    {
                        parmaMap.put("commodity_type", map.get("commodity_type"));
                    }
                    int mchStatus = Integer.parseInt(mchStatusTemp);
                    if (mchStatus == 1)
                    {
                        //商品列表只显示已审核通过的商品
                        parmaMap.put("mch_status", DictionaryConst.GoodsMchExameStatus.EXAME_PASS_STATUS);
                    }
                    else if (mchStatus == 2)
                    {
                        //mch_status != 2
                        parmaMap.put("mch_status", -2);
                    }
                    else if (mchStatus == 3)
                    {
                        parmaMap.put("mch_status", DictionaryConst.GoodsMchExameStatus.EXAME_NOT_PASS_STATUS);
                    }
                    else if (mchStatus == 4)
                    {
                        parmaMap.put("mch_status", DictionaryConst.GoodsMchExameStatus.EXAME_STOP_STATUS);
                    }
                    else if (mchStatus == 5)
                    {
                        //待审核的
                        parmaMap.put("mch_status", DictionaryConst.GoodsMchExameStatus.EXAME_WAIT_STATUS);
                    }
                }
                List<Integer> mchStatusList = (List<Integer>) map.get("mchStatusList");
                if (mchStatusList != null && mchStatusList.size() > 0)
                {
                    parmaMap.put("mchStatusList", mchStatusList);
                }
                //自选商品流程
                if (map.containsKey("isZx"))
                {
                    boolean isZx = MapUtils.getBooleanValue(map, "isZx");
                    if (isZx)
                    {
                        int myMchId = MapUtils.getInteger(map, "myMchId");
                        parmaMap.put("is_zixuan", 1);
//                        parmaMap.put("isSupplier", "isSupplier");
                        parmaMap.put("checkZx_mchId", myMchId);
                    }
                }
            }
            //剔除自选商品 add by trick 2021-12-17 11:35:41
            if (map.containsKey("notZx") && MapUtils.getBooleanValue(map, "notZx"))
            {
                int myMchId = MapUtils.getInteger(map, "myMchId");
                parmaMap.put("not_checkZx_mchId", myMchId);
            }
            parmaMap1.putAll(parmaMap);

            //是否需要查询商品分类
            if (map.containsKey("product_class") && map.get("classnotset") == null)
            {
                Map<String, Object> brandMap = new HashMap<>(16);
                brandMap.put("brand_id", "0");
                brandMap.put("brand_name", "请选择品牌");

                int classId = Integer.parseInt(map.get("product_class").toString());
                if (classId > 0)
                {
                    //商品分类递归找上级
                    String   classIdListStr = strOption(storeId, classId, "");
                    String[] classIds       = StringUtils.trim(classIdListStr, SplitUtils.HG).split(SplitUtils.HG);
                    //查询子分类要带出上级,查上级不能带出下级数据
                    parmaMap.put("product_class", classIds[classIds.length - 1]);
                    //商品分类顶级
                    int classIdTop = Integer.parseInt(classIds[0]);
                    for (String cid : classIds)
                    {
                        int               id      = Integer.parseInt(cid);
                        ProductClassModel product = new ProductClassModel();
                        product.setStore_id(storeId);
                        product.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS);
                        product.setCid(id);
                        if (StringUtils.isNotEmpty(langCode))
                        {
                            product.setLang_code(langCode);
                        }
                        product = productClassModelMapper.selectOne(product);
                        if (product != null)
                        {
                            productClassList.add(brandMap);
                        }
                    }
                    //获取顶级类别品牌
                    Map<String, Object> parmaBrandMap = new HashMap<>(16);
                    parmaBrandMap.put("store_id", storeId);
                    parmaBrandMap.put("categories", classIdTop);
                    parmaBrandMap.put("status", BrandClassModel.Status.OPEN);

                    if (StringUtils.isNotEmpty(langCode))
                    {
                        parmaBrandMap.put("lang_code", langCode);
                    }
                    brandList = brandClassModelMapper.getBrandClassDynamic(parmaBrandMap);
                }
                brandList.add(0, brandMap);
            }
            else if (MapUtils.getInteger(map, "classnotset", 0) == 1)
            {
                parmaMap.put("classnotset", 1);
            }
            //商品Id
            if (map.containsKey("goodsId"))
            {
                Integer goodsId = MapUtils.getInteger(map, "goodsId");
                parmaMap.put("goodsId", goodsId);
            }
            //是否需要查询品牌id
            if (map.containsKey("brand_id") && map.get("brandnotset") == null)
            {
                int brandIdTemp = Integer.parseInt(map.get("brand_id").toString());
                if (brandIdTemp != 0)
                {
                    parmaMap.put("brand_id", brandIdTemp);
                }
            }
            else if (MapUtils.getInteger(map, "brandnotset", 0) == 1)
            {
                parmaMap.put("brandnotset", 1);
            }
            //是否需要查询商品上架状态
            if (map.containsKey("status"))
            {
                int statusTemp = Integer.parseInt(map.get("status").toString());
                if (statusTemp != 0)
                {
                    parmaMap.put("goodsStatus", statusTemp);
                }
                else
                {
                    //app.mch.my_merchandise接口需要
                    parmaMap.put("mch_status", DictionaryConst.GoodsMchExameStatus.EXAME_WAIT_STATUS);
                }
            }
            else if (map.containsKey("statusList"))
            {
                List<Integer> statusList = DataUtils.cast(map.get("statusList"));
                if (statusList != null)
                {
                    parmaMap.put("statusList", statusList);
                }
            }
            //是否需要查询商品互动状态
            if (map.containsKey("active"))
            {
                int activeTemp = Integer.parseInt(map.get("active").toString());
                if (activeTemp != 0)
                {
                    parmaMap.put("active", activeTemp);
                }
            }
            //是否需要查询商品标题
            if (map.containsKey("product_title"))
            {
                String productTitle = map.get("product_title") + "";
                if (!StringUtils.isEmpty(productTitle))
                {
                    if (productTitle.indexOf(" ") > 0)
                    {
                        //多个标题搜索
                        List<String> productTitleList = Arrays.asList(productTitle.split(" "));
                        parmaMap.put("productTitleList", productTitleList);
                    }
                    else
                    {
                        parmaMap.put("product_title", productTitle);
                    }
                }

            }
            //是否需要查询店铺名称
            if (map.containsKey("mch_name"))
            {
                String mchName = map.get("mch_name") + "";
                if (StringUtils.isNotEmpty(mchName))
                {
                    parmaMap.put("mch_name", mchName);
                }
            }
            if (map.containsKey("mchNameOrGoodsName"))
            {
                String key = map.get("mchNameOrGoodsName") + "";
                if (StringUtils.isNotEmpty(key))
                {
                    parmaMap.put("mchNameOrGoodsName", StringUtils.getKey(key));
                }
            }
            //是否需要查询展示位置
            if (map.containsKey("show_adr"))
            {
                String showAdr = map.get("show_adr") + "";
                if (StringUtils.isNotEmpty(showAdr))
                {
                    parmaMap.put("show_adr", showAdr);
                }
            }
            //是否需要查询商品标签
            if (map.containsKey("goodsTga"))
            {
                String showAdr = map.get("goodsTga") + "";
                if (StringUtils.isNotEmpty(showAdr))
                {
                    parmaMap.put("s_type", showAdr);
                }
            }
            //是否需要查询商品标签
            if (map.containsKey("lang_code"))
            {
                String langCodeStr = map.get("lang_code") + "";
                if (StringUtils.isNotEmpty(langCodeStr))
                {
                    parmaMap.put("lang_code", langCodeStr);
                }
            }
            //显示已售罄商品按钮处理
            ProductConfigModel productConfigModel = new ProductConfigModel();
            productConfigModel.setStore_id(storeId);
            productConfigModel = productConfigModelMapper.selectOne(productConfigModel);
            if (productConfigModel != null && type != GloabConst.LktConfig.LKT_CONFIG_TYPE_PT)
            {
                parmaMap.put("is_display_sell_put", productConfigModel.getIs_display_sell_put());
            }

            boolean isAll = false;
            if ("whole".equals(pageTo))
            {
                //导出全部
                isAll = true;
            }
            else
            {
                //导出查询无需分页
                if (!"inquiry".equals(pageTo))
                {
                    //导出本页 This_page
                    parmaMap.put("page", page.getPageNo());
                    parmaMap.put("pageSize", page.getPageSize());
                }
            }

            if (map.containsKey("IsItDescendingOrder"))
            {

                parmaMap1.put("IsItDescendingOrder", map.get("IsItDescendingOrder"));
                parmaMap1.remove("sort_sort");
                parmaMap1.remove("diy_sort");
                parmaMap1.remove("mch_diy_sort");
                parmaMap1.remove("goods_asc_sort");

                parmaMap.put("IsItDescendingOrder", map.get("IsItDescendingOrder"));
                parmaMap.remove("sort_sort");
                parmaMap.remove("diy_sort");
                parmaMap.remove("mch_diy_sort");
                parmaMap.remove("goods_asc_sort");

            }
            if (map.containsKey("isZy"))
            {
                parmaMap.put("isZy",MapUtils.getInteger(map,"isZy"));
            }

            if (StringUtils.isNotEmpty(langCode))
            {
                parmaMap.put("lang_code", langCode);
            }

            //根据条件统计
            int                       total = productListModelMapper.getProductListLeftJoinMchCountDynamic(parmaMap);
            List<Map<String, Object>> productList;
            if (isAll)
            {
                //导出查询
                productList = productListModelMapper.getProductListLeftJoinMchDynamic(parmaMap1);
            }
            else
            {

                productList = productListModelMapper.getProductListLeftJoinMchDynamic(parmaMap);
            }
            //缓存商品标签
            Map<String, String> proLabelModelMap = new HashMap<>(16);
            //缓存商品标签颜色
            Map<String, String> proLabelColorMap = new HashMap<>(16);
            ProLabelModel       proLabelModel    = new ProLabelModel();
            proLabelModel.setStore_id(storeId);

            if (StringUtils.isNotEmpty(langCode))
            {
                proLabelModel.setLang_code(langCode);
            }

            List<ProLabelModel> proLabelModelList = proLabelModelMapper.select(proLabelModel);
            for (ProLabelModel label : proLabelModelList)
            {
                proLabelModelMap.put(label.getId().toString(), label.getName());
                proLabelColorMap.put(label.getId().toString(), label.getColor());
            }

            if (productList != null)
            {
                for (int i = 0; i < productList.size(); i++)
                {
                    Map<String, Object> goodMap    = productList.get(i);
                    int                 goodsId    = Integer.parseInt(goodMap.get("id").toString());
//

                    Object brandIdObj = goodMap.get("brand_id");
                    int    brandId    = 0;

                    if (brandIdObj != null)
                    {
                        brandId = Integer.parseInt(brandIdObj.toString());
                    }


                    //获取国家名称 可以放redis缓存处理
                    int country = Integer.parseInt(goodMap.get("country_num").toString());
                    goodMap.put("country_name", publiceService.getCountryName(country));

                    //获取语种名称 可以放redis缓存处理
                    String langCode1 = goodMap.get("lang_code").toString();
                    goodMap.put("lang_name", publiceService.getLangName(langCode1));

                    String              initialStr = goodMap.get("initial").toString();
                    Map<String, String> initialMap = DataUtils.cast(SerializePhpUtils.getUnserializeObj(initialStr, Map.class));
                    if (initialMap == null)
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SJCW, "数据错误");
                    }
                    BigDecimal presentPrice = new BigDecimal(initialMap.get("sj"));
                    String     unit         = initialMap.get("unit");

                    //是否有未完成的订单
                    Integer rew = chaxun(goodsId, storeId);
                    goodMap.put("rew", 0);
                    if (rew != null)
                    {
                        if (rew == 2)
                        {
                            goodMap.put("rew", 2);
                        }
                        else if (rew == 3)
                        {
                            goodMap.put("rew", 3);
                        }
                    }
                    //获取上一条数据的id
                    Integer supperGoodsId = null;
                    boolean supperStatus  = false;
                    if (i == 0)
                    {
                        //第一页没有上一条
                        if (page.getPageNo() > 1)
                        {
                            //获取上一页最后一条数据
                            Map<String, Object> parmaTempMap = new HashMap<>(16);
                            parmaTempMap.putAll(parmaMap);
                            parmaTempMap.put("page", page.getPageNo() - 1);
                            parmaTempMap.put("pageSize", 1);
                            //获取商品信息
                            List<Map<String, Object>> goodsFirstList = productListModelMapper.getProductListLeftJoinMchDynamic(parmaTempMap);
                            Map<String, Object>       goodsFirstMap  = goodsFirstList.get(0);
                            supperGoodsId = Integer.parseInt(goodsFirstMap.get("id").toString());
                            supperStatus = true;
                        }
                    }
                    else
                    {
                        //上一条数据id
                        supperGoodsId = Integer.parseInt(productList.get(i - 1).get("id").toString());
                        supperStatus = true;
                    }
                    goodMap.put("upper_status", supperStatus);
                    //获取下一条数据
                    Integer underneathGoodsId = null;
                    if (i == productList.size() - 1)
                    {
                        Map<String, Object> parmaTempMap = new HashMap<>(16);
                        parmaTempMap.putAll(parmaMap);
                        parmaTempMap.put("pageSize", 1);
                        //当为最后一条数据
                        if (pageNo == 1)
                        {
                            parmaTempMap.put("page", page.getPageNo());
                        }
                        else
                        {
                            int start = page.getPageNo() + page.getPageSize();
                            parmaTempMap.put("page", start);
                        }
                        //获取商品信息
                        List<Map<String, Object>> goodsFirstList = productListModelMapper.getProductListLeftJoinMchDynamic(parmaTempMap);
                        if (goodsFirstList.size() > 0)
                        {
                            Map<String, Object> goodsFirstMap = goodsFirstList.get(0);
                            underneathGoodsId = Integer.parseInt(goodsFirstMap.get("id").toString());
                        }
                    }
                    else
                    {
                        underneathGoodsId = Integer.parseInt(productList.get(i + 1).get("id").toString());
                    }
                    goodMap.put("upper_id", supperGoodsId);
                    goodMap.put("underneath_id", underneathGoodsId);
                    String                  stype         = goodMap.get("s_type") + "";
                    List<String>            labelNameList = new ArrayList<>();
                    List<Object>            s_type_list   = new ArrayList<>();
                    HashMap<String, Object> typeMap;
                    if (StringUtils.isNotEmpty(stype))
                    {
                        stype = StringUtils.trim(stype, SplitUtils.DH);
                        String[] stypeList = stype.split(SplitUtils.DH);
                        for (String labelId : stypeList)
                        {
                            typeMap = new HashMap<>();
                            if (proLabelModelMap.containsKey(labelId))
                            {
                                labelNameList.add(proLabelModelMap.get(labelId));
                                typeMap.put("name", proLabelModelMap.get(labelId));
                                typeMap.put("color", proLabelColorMap.get(labelId));
                                typeMap.put("id", labelId);
                            }
                            s_type_list.add(typeMap);
                        }
                        goodMap.put("s_type", stypeList);
                        goodMap.put("s_type_list", s_type_list);
                    }
                    goodMap.put("labelList", labelNameList);
                    //展示位置处理
                    String       showAdr         = goodMap.get("show_adr") + "";
                    List<String> showAdrNameList = new ArrayList<>();
                    if (StringUtils.isNotEmpty(showAdr))
                    {
                        showAdr = StringUtils.trim(showAdr, SplitUtils.DH);
                        String[] showAdrList = showAdr.split(SplitUtils.DH);
                        goodMap.put("showAdrList", showAdrList);
                        DicVo dicVo = new DicVo();
                        dicVo.setName("商品展示位置");
                        dicVo.setShowChild(true);
                        dicVo.setShowChild(true);
                        for (String showAdrId : showAdrList)
                        {
                            dicVo.setValue(showAdrId);
                            Map<String, Object>       showAdrMap = publicDictionaryService.getDictionaryByName(dicVo);
                            List<DictionaryListModel> showList   = DataUtils.cast(showAdrMap.get("value"));
                            if (showList != null)
                            {
                                for (DictionaryListModel dic : showList)
                                {
                                    showAdrNameList.add(dic.getValue());
                                }
                            }
                        }
                    }
                    if (showAdrNameList.size() < 1)
                    {
                        showAdrNameList.add("全部商品");
                    }
                    goodMap.put("showAdrNameList", showAdrNameList);
                    //总平台的商品分类名称
                    String commodityClassification = "";
                    //总平台的商品品牌名称
                    String commodityBrand = "";
                    //店主分类
                    String shopkeepersClassification = "";

                    Object productClassObj = goodMap.get("product_class");
                    String classIds        = null;

                    if (productClassObj != null)
                    {
                        classIds = productClassObj.toString();
                    }

                    if (StringUtils.isNotEmpty(classIds))
                    {
                        //-394-395-396- 转换成数组需要 -1
                        String[]          classIdList = StringUtils.trim(classIds, SplitUtils.HG).split(SplitUtils.HG);
                        int               classId     = Integer.parseInt(classIdList[classIdList.length - 1]);
                        ProductClassModel p           = new ProductClassModel();
                        p.setStore_id(storeId);
                        p.setCid(classId);
                        p = productClassModelMapper.selectOne(p);
                        if (p != null)
                        {
                            commodityClassification = p.getPname();
                        }

                        if (brandId != 0)
                        {
                            BrandClassModel b = new BrandClassModel();
                            b.setStore_id(storeId);
                            b.setBrand_id(brandId);
                            b = brandClassModelMapper.selectOne(b);
                            commodityBrand = b.getBrand_name();
                        }
                        else
                        {
                            commodityBrand = "";
                        }
                        goodMap.put("brand_name", commodityBrand);
                        goodMap.put("pname", commodityClassification);

                    }

                    //查询库存信息
                    ConfiGureModel confiGureModel = new ConfiGureModel();
                    Integer        goodStockNum   = confiGureModelMapper.countConfigGureNum(goodsId);
                    goodMap.put("num", goodStockNum);
                    //商品图片
                    String cgImg = "";
                    //获取价格
                    confiGureModel = confiGureModelMapper.getProductMinPriceAndMaxYprice(goodsId);
                    if (confiGureModel != null)
                    {
                        unit = confiGureModel.getUnit();
                        presentPrice = confiGureModel.getPrice();
                    }

                    //获取商品主图
                    goodMap.put("imgurl", publiceService.getImgPath(productImgModelMapper.getProductImg(goodsId), storeId));
                    goodMap.put("unit", unit);
                    goodMap.put("price", presentPrice);
                    String statusName = "";
                    int    status     = Integer.parseInt(goodMap.get("status").toString());
                    if (DictionaryConst.GoodsStatus.NOT_GROUNDING == status)
                    {
                        statusName = "待上架";
                    }
                    else if (DictionaryConst.GoodsStatus.NEW_GROUNDING == status)
                    {
                        statusName = "上架";
                    }
                    else if (DictionaryConst.GoodsStatus.OFFLINE_GROUNDING == status)
                    {
                        statusName = "下架";
                    }
                    goodMap.put("status_name", statusName);
                    goodMap.put("upper_shelf_time", DateUtil.dateFormate(MapUtils.getString(goodMap, "upper_shelf_time"), GloabConst.TimePattern.YMDHMS));
                    goodMap.put("add_date", DateUtil.dateFormate(MapUtils.getString(goodMap, "add_date"), GloabConst.TimePattern.YMDHMS));
                    Integer volume     = MapUtils.getInteger(goodMap, "volume");
                    Integer realVolume = MapUtils.getInteger(goodMap, "real_volume");
                    Integer svolume    = MapUtils.getInteger(goodMap, "svolume");
                    goodMap.put("volume", svolume);
                    list.add(goodMap);
                }
            }

//            if(map.containsKey("IsItDescendingOrder")){
//                resultMap.put("list", sort(list,map.get("IsItDescendingOrder").toString()));
//            }else{
            resultMap.put("list", list);
//            }
            resultMap.put("total", total);
        }
        catch (ClassCastException c)
        {
            c.printStackTrace();
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSLXBZQ, "参数类型不正确", "productList");
        }
        catch (LaiKeAPIException l)
        {
            logger.error("自定义异常 :", l);
            throw l;
        }
        catch (Exception e)
        {
            logger.error("商品列表 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "productList");
        }

        return resultMap;
    }

    public List<Map<String, Object>> sort(List<Map<String, Object>> list, String desc)
    {
        List<Map<String, Object>> sortedDescList = list;
        if (StringUtils.isNotEmpty(desc))
        {
            if ("desc".equalsIgnoreCase(desc))
            {
                sortedDescList = list.stream()
                        .sorted((map11, map22) -> ((Integer) map22.get("volume")) - ((Integer) map11.get("volume")))
                        .collect(Collectors.toList());
            }
            else
            {
                sortedDescList = list.stream()
                        .sorted((map11, map22) -> ((Integer) map11.get("volume")) - ((Integer) map22.get("volume")))
                        .collect(Collectors.toList());
            }
        }
        return sortedDescList;
    }


    @Override
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

    @Override
    public void getClassLevelAllInfo(int storeId, int cid, Map<Integer, List<ProductClassModel>> resultMap) throws LaiKeAPIException
    {
        try
        {
            List<ProductClassModel> resultList = new ArrayList<>();
            //获取当前类别
            ProductClassModel productClassModel = new ProductClassModel();
            productClassModel.setCid(cid);
            productClassModel.setStore_id(storeId);
            productClassModel.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS);
            productClassModel = productClassModelMapper.selectOne(productClassModel);
            if (productClassModel != null)
            {
                resultList.add(productClassModel);
                resultMap.put(productClassModel.getLevel(), resultList);
                //获取上级类别
                int sid = productClassModel.getSid();
                //0代表最顶级
                if (sid != 0)
                {
                    this.getClassLevelAllInfo(storeId, sid, resultMap);
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

    @Autowired
    private SupplierProClassModelMapper supplierProClassModelMapper;

    @Override
    public void getSupplierClassLevelAllInfo(int storeId, int cid, Map<Integer, List<Map<String, Object>>> resultMap, int i) throws LaiKeAPIException
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
                        this.getSupplierClassLevelAllInfo(storeId, sid, resultMap, i + 1);
                    }
                }
            }
            else
            {
                SupplierProClassModel productClassModel = new SupplierProClassModel();
                productClassModel.setCid(cid);
                productClassModel.setStore_id(storeId);
                productClassModel.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS);
                productClassModel = supplierProClassModelMapper.selectOne(productClassModel);
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
                        this.getSupplierClassLevelAllInfo(storeId, sid, resultMap, i + 1);
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

    @Override
    public List<Map<String, Object>> getClassLevelLowAll(int storeId, int sid) throws LaiKeAPIException
    {
        try
        {
            List<Map<String, Object>> resultList = new ArrayList<>();
            //找下级
            ProductClassModel productClassModel = new ProductClassModel();
            productClassModel.setStore_id(storeId);
            productClassModel.setSid(sid);
            productClassModel.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS);
            productClassModel.setExamine(1);
            List<ProductClassModel> productClassModelList = productClassModelMapper.select(productClassModel);
            for (ProductClassModel productClass : productClassModelList)
            {
                Map<String, Object> classTopMap = new HashMap<>(16);
                classTopMap.put("cid", productClass.getCid());
                classTopMap.put("sid", productClass.getSid());
                classTopMap.put("level", productClass.getLevel());
                classTopMap.put("cname", productClass.getPname());
                //递归获取下级
                classTopMap.put("child", getClassLevelLowAll(storeId, productClass.getCid()));
                resultList.add(classTopMap);
            }
            return resultList;
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("递归商品类别下级 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getClassLevelLowAll");
        }
    }


    @Override
    public List<Map<String, Object>> mchAllFenlei(int storeId, Integer mchId) throws LaiKeAPIException
    {
        List<Map<String, Object>> resultList = new ArrayList<>();
        try
        {
            //店铺拥有的所有顶级类别id
            Set<Integer> goodsClassIdList = new HashSet<>();
            //获取店铺所有商品信息
            ProductListModel productListModel = new ProductListModel();
            productListModel.setStore_id(storeId);
            productListModel.setMch_id(mchId);
            productListModel.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS.toString());
            productListModel.setMch_status(DictionaryConst.GoodsMchExameStatus.EXAME_PASS_STATUS.toString());
            productListModel.setActive(DictionaryConst.GoodsActive.GOODSACTIVE_POSITIVE_PRICE.toString());
            List<ProductListModel> productListModelList = productListModelMapper.select(productListModel);
            for (ProductListModel productList : productListModelList)
            {
                //商品类别 -3-31-191-
                String[] classIds = StringUtils.trim(productList.getProduct_class(), SplitUtils.HG).split(SplitUtils.HG);
                //获取顶级目录
                goodsClassIdList.add(Integer.parseInt(classIds[0]));
            }
            for (Integer Cid : goodsClassIdList)
            {
                Map<String, Object> classTopMap = new HashMap<>(16);
                //获取平台商品类别当前类别
                ProductClassModel productClassModel = new ProductClassModel();
                productClassModel.setStore_id(storeId);
                productClassModel.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS);
                productClassModel.setCid(Cid);
                productClassModel.setLevel(0);
                ProductClassModel goodsClassLevel = productClassModelMapper.selectOne(productClassModel);
                classTopMap.put("cid", goodsClassLevel.getCid());
                classTopMap.put("sid", goodsClassLevel.getSid());
                classTopMap.put("level", goodsClassLevel.getLevel());
                classTopMap.put("cname", goodsClassLevel.getPname());
                //递归获取下级
                classTopMap.put("child", getClassLevelLowAll(storeId, Cid));
                resultList.add(classTopMap);
            }
            return resultList;
        }
        catch (LaiKeAPIException e)
        {
            e.printStackTrace();
        }
        catch (Exception e)
        {
            logger.error("获取店铺下的所有商品分类 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getClassLevelLowAll");
        }

        return null;
    }

    @Override
    public List<Map<String, Object>> getClassLevelLowAll(int storeId, int sid, List<Integer> classIds) throws LaiKeAPIException
    {
        try
        {
            List<Map<String, Object>> resultList = new ArrayList<>();
            //找下级
            ProductClassModel productClassModel = new ProductClassModel();
            productClassModel.setStore_id(storeId);
            productClassModel.setSid(sid);
            productClassModel.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS);
            List<ProductClassModel> productClassModelList = productClassModelMapper.select(productClassModel);
            for (ProductClassModel productClass : productClassModelList)
            {
                if (!classIds.contains(productClass.getCid()))
                {
                    continue;
                }
                Map<String, Object> classTopMap = new HashMap<>(16);
                classTopMap.put("cid", productClass.getCid());
                classTopMap.put("sid", productClass.getSid());
                classTopMap.put("level", productClass.getLevel());
                classTopMap.put("cname", productClass.getPname());
                //递归获取下级
                classTopMap.put("child", getClassLevelLowAll(storeId, productClass.getCid()));
                resultList.add(classTopMap);
            }
            return resultList;
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("递归商品类别下级 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getClassLevelLowAll");
        }
    }

    @Override
    public Integer getClassTop(int storeId, int cid) throws LaiKeAPIException
    {
        try
        {
            //获取当前类别
            ProductClassModel productClassModel = new ProductClassModel();
            productClassModel.setCid(cid);
            productClassModel.setStore_id(storeId);
            productClassModel.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS);
            productClassModel = productClassModelMapper.selectOne(productClassModel);
            if (productClassModel != null)
            {
                //获取上级类别
                int sid = productClassModel.getSid();
                //0代表最顶级
                if (sid != 0)
                {
                    //继续获取上级
                    return this.getClassTop(storeId, sid);
                }
                return productClassModel.getCid();
            }
            return 0;
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

    @Override
    public Integer chaxun(int id, int storeId) throws LaiKeAPIException
    {
        try
        {
            //获取当前商品信息
            ProductListModel productListModel = new ProductListModel();
            productListModel.setStore_id(storeId);
            productListModel.setActive("all");
            productListModel.setMch_status(DictionaryConst.GoodsMchExameStatus.EXAME_PASS_STATUS.toString());
            productListModel.setId(id);
            productListModel = productListModelMapper.selectOne(productListModel);
            if (productListModel != null)
            {
                //判断当前商品是否上架
                if (productListModel.getStatus().equals(DictionaryConst.GoodsStatus.NEW_GROUNDING.toString()))
                {
                    Map<String, Object> parmaMap = new HashMap<>();
                    parmaMap.put("store_id", storeId);
                    parmaMap.put("mch_status", DictionaryConst.GoodsStatus.NEW_GROUNDING);
                    List<Integer> rstatusList = new ArrayList<>();
                    rstatusList.add(DictionaryConst.OrdersStatus.ORDERS_R_STATUS_UNPAID);
                    rstatusList.add(DictionaryConst.OrdersStatus.ORDERS_R_STATUS_CONSIGNMENT);
                    rstatusList.add(DictionaryConst.OrdersStatus.ORDERS_R_STATUS_DISPATCHED);
                    parmaMap.put("rstatusList", rstatusList);
                    List<Map<String, Object>> resultGoodsList = productListModelMapper.getProductListJoinOrderDetailsDynamic(parmaMap);
                    if (resultGoodsList != null)
                    {
                        return 3;
                    }
                }
                return 1;
            }
        }
        catch (ClassCastException c)
        {
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSLXBZQ, "参数类型不正确", "chaxun");
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("chaxun 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "chaxun");
        }
        return null;
    }


    /**
     * 获取商品明细
     *
     * @param dom -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/7/6 10:58
     */
    private String getGoodsContext(Document dom) throws LaiKeAPIException
    {
        StringBuilder content = new StringBuilder();
        try
        {
            String              pattern   = "(?s)apiImgInfo(.*?):(.*?),";
            Pattern             r         = Pattern.compile(pattern);
            Matcher             m         = r.matcher(dom.getElementsByTag("script").html());
            String              imgUrlApi = "https://gd1.alicdn.com/imgextra/%s";
            Map<String, Object> imgUrlMap = new HashMap<>(16);
            while (m.find())
            {
                String              imgUrlApiJson = "{" + StringUtils.trim(m.group()).replace(SplitUtils.DH, "}");
                Map<String, String> imgUrlApiMap  = new HashMap<>(16);
                imgUrlApiMap = JSON.parseObject(imgUrlApiJson, new TypeReference<Map<String, String>>()
                {
                });
                String goodsImgUrlApi = GloabConst.HttpProtocol.HTTPS + ":" + imgUrlApiMap.get("apiImgInfo");
                String resultJson     = HttpUtils.get(goodsImgUrlApi);
                resultJson = resultJson.replace("$callback(", "");
                resultJson = resultJson.substring(0, resultJson.length() - 1);
                imgUrlMap = JSON.parseObject(resultJson, new TypeReference<Map<String, Object>>()
                {
                });
            }
            content.append("<p>");
            for (String key : imgUrlMap.keySet())
            {
                if (key.length() > 10)
                {
                    content.append("<img src=\"").append(String.format(imgUrlApi, key)).append("\">");
                }
            }
            content.append("</p>");
        }
        catch (Exception e)
        {
            logger.error(">>>>>>>抓取商品详情 【异常失败】<<<<<<<", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SPGGKCBCSB, "商品规格库存保存失败", "getGoodsContext");
        }
        return content.toString();
    }

    @Override
    public Map<String, Object> getClassifiedBrands(int storeId, Integer classId, Integer brandId) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        //产品类别集
        List<Map<String, Object>> productClassList = new ArrayList<>();
        //品牌集
        List<Map<String, Object>> brandList;
        try
        {
            Integer classTopId = null;

            ProductClassModel productClassModel = new ProductClassModel();
            productClassModel.setStore_id(storeId);
            if (classId == null || classId == 0)
            {
                //获取产品顶级类别信息
                productClassModel.setSid(0);
                productClassModel.setExamine(1);
                List<ProductClassModel> productClassModelList = productClassModelMapper.getProductClassLevel(productClassModel);
                for (ProductClassModel productClass : productClassModelList)
                {
                    Map<String, Object> productClassMap = new HashMap<>(16);
                    productClassMap.put("cid", productClass.getCid());
                    productClassMap.put("pname", productClass.getPname());
                    productClassMap.put("level", productClass.getLevel());
                    productClassMap.put("status", false);
                    productClassList.add(productClassMap);
                }
                if (productClassModelList.size() > 0)
                {
                    classTopId = productClassModelList.get(0).getCid();
                }
            }
            else
            {
                //获取产品子类别信息
                productClassModel.setCid(classId);
                ProductClassModel productClassModelFirstLeve = productClassModelMapper.selectOne(productClassModel);
                //查询下级,没有下级则查询同级
                productClassModel = new ProductClassModel();
                productClassModel.setStore_id(storeId);
                productClassModel.setSid(classId);
                List<ProductClassModel> productClassLowLeves = productClassModelMapper.getProductClassLevel(productClassModel);
                if (productClassLowLeves != null)
                {
                    //递归获取顶级
                    classTopId = getClassTop(storeId, classId);
                    //获取所有下级类别信息
                    for (ProductClassModel productClass : productClassLowLeves)
                    {
                        Map<String, Object> productClassMap = new HashMap<>(16);
                        productClassMap.put("cid", productClass.getCid());
                        productClassMap.put("pname", productClass.getPname());
                        productClassMap.put("level", productClass.getLevel());
                        productClassMap.put("status", false);
                        productClassList.add(productClassMap);
                    }
                }
                else
                {
                    //查询同级
                    productClassModel = new ProductClassModel();
                    productClassModel.setStore_id(storeId);
                    productClassModel.setSid(productClassModelFirstLeve.getSid());
                    List<ProductClassModel> productClassLeves = productClassModelMapper.getProductClassLevel(productClassModel);
                    for (ProductClassModel productClass : productClassLeves)
                    {
                        Map<String, Object> productClassMap = new HashMap<>(16);
                        //是否选中标识
                        boolean flag = productClass.getCid().equals(classId);
                        productClassMap.put("cid", productClass.getCid());
                        productClassMap.put("pname", productClass.getPname());
                        productClassMap.put("level", productClass.getLevel());
                        productClassMap.put("status", flag);
                        productClassList.add(productClassMap);
                    }
                }
            }

            //获取品牌信息
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", storeId);
            parmaMap.put("categories", classTopId);
            parmaMap.put("status", BrandClassModel.Status.OPEN);
            parmaMap.put("examine",1);
            parmaMap.put("recycle",DictionaryConst.ProductRecycle.NOT_STATUS);
            brandList = brandClassModelMapper.getBrandClassDynamic(parmaMap);

            List<Object> tempList = new ArrayList<>();
            if (productClassList.size() > 0)
            {
                tempList.add(productClassList);
            }
            resultMap.put("class_list", tempList);
            resultMap.put("brand_list", brandList);
        }
        catch (ClassCastException c)
        {
            logger.error("获取分类 失败", c);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSLXBZQ, "参数类型不正确", "getClassifiedBrands");
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取分类 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SJYC, "数据异常", "getClassifiedBrands");
        }

        return resultMap;
    }

    @Override
    public Map<String, Object> getClassifiedBrands(MainVo vo, Integer classId, Integer brandId) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        //产品类别集
        List<Map<String, Object>> productClassList = new ArrayList<>();
        //品牌集
        List<Map<String, Object>> brandList;
        int                       storeId  = vo.getStoreId();
        String                    langCode = vo.getLang_code();
        try
        {
            Integer           classTopId        = null;
            ProductClassModel productClassModel = new ProductClassModel();
            productClassModel.setStore_id(storeId);
            if (classId == null || classId == 0)
            {
                //获取产品顶级类别信息
                productClassModel.setSid(0);
                productClassModel.setExamine(1);
                if (StringUtils.isNotEmpty(langCode))
                {
                    productClassModel.setLang_code(langCode);
                }

                //系统的整体默认或者用户右上角所选的语种，这种场景只适用于第一次进入功能界面的查询
//                String language = vo.getLanguage();
//                if(StringUtils.isEmpty(langCode) && StringUtils.isNotEmpty(language))
//                {
//                    logger.info("默认语种:{}",language);
//                    productClassModel.setLang_code(language);
//                }
                productClassModel.setExamine(Integer.valueOf(DictionaryConst.ExameStatus.EXAME_PASS_STATUS));
                List<ProductClassModel> productClassModelList = productClassModelMapper.getProductClassLevel(productClassModel);
                for (ProductClassModel productClass : productClassModelList)
                {
                    Map<String, Object> productClassMap = new HashMap<>(16);
                    productClassMap.put("cid", productClass.getCid());
                    productClassMap.put("pname", productClass.getPname());
                    productClassMap.put("level", productClass.getLevel());
                    productClassMap.put("status", false);
                    productClassMap.put("lang_code", productClass.getLang_code());
                    productClassMap.put("lang_name", publiceService.getLangName(productClass.getLang_code()));
                    productClassMap.put("country_name", publiceService.getCountryName(productClass.getCountry_num()));
                    productClassMap.put("country_num", productClass.getCountry_num());
                    productClassMap.put("notset", productClass.getNotset());
                    productClassList.add(productClassMap);
                }
                if (productClassModelList.size() > 0)
                {
                    classTopId = productClassModelList.get(0).getCid();
                }
            }
            else
            {
                //获取产品子类别信息
                productClassModel.setCid(classId);
                ProductClassModel productClassModelFirstLeve = productClassModelMapper.selectOne(productClassModel);
                //查询下级,没有下级则查询同级
                productClassModel = new ProductClassModel();
                productClassModel.setStore_id(storeId);
                productClassModel.setSid(classId);
                if (StringUtils.isNotEmpty(langCode))
                {
                    productClassModel.setLang_code(langCode);
                }
                productClassModel.setExamine(Integer.valueOf(DictionaryConst.ExameStatus.EXAME_PASS_STATUS));
                List<ProductClassModel> productClassLowLeves = productClassModelMapper.getProductClassLevel(productClassModel);
                if (productClassLowLeves != null)
                {
                    //递归获取顶级
                    classTopId = getClassTop(storeId, classId);
                    //获取所有下级类别信息
                    for (ProductClassModel productClass : productClassLowLeves)
                    {
                        Map<String, Object> productClassMap = new HashMap<>(16);
                        productClassMap.put("cid", productClass.getCid());
                        productClassMap.put("pname", productClass.getPname());
                        productClassMap.put("level", productClass.getLevel());
                        productClassMap.put("status", false);
                        productClassMap.put("lang_code", productClass.getLang_code());
                        productClassMap.put("lang_name", publiceService.getLangName(productClass.getLang_code()));
                        productClassMap.put("country_name", publiceService.getCountryName(productClass.getCountry_num()));
                        productClassMap.put("country_num", productClass.getLang_code());
                        productClassMap.put("notset", productClass.getNotset());
                        productClassList.add(productClassMap);
                    }
                }
                else
                {
                    //查询同级
                    productClassModel = new ProductClassModel();
                    productClassModel.setStore_id(storeId);
                    productClassModel.setSid(productClassModelFirstLeve.getSid());
                    productClassModel.setExamine(Integer.valueOf(DictionaryConst.ExameStatus.EXAME_PASS_STATUS));
                    List<ProductClassModel> productClassLeves = productClassModelMapper.getProductClassLevel(productClassModel);
                    for (ProductClassModel productClass : productClassLeves)
                    {
                        Map<String, Object> productClassMap = new HashMap<>(16);
                        //是否选中标识
                        boolean flag = productClass.getCid().equals(classId);
                        productClassMap.put("cid", productClass.getCid());
                        productClassMap.put("pname", productClass.getPname());
                        productClassMap.put("level", productClass.getLevel());
                        productClassMap.put("status", flag);
                        productClassMap.put("notset", productClass.getNotset());
                        productClassList.add(productClassMap);
                    }
                }
            }

            //获取品牌信息
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", storeId);
            parmaMap.put("categories", classTopId);
            parmaMap.put("status", BrandClassModel.Status.OPEN);
            parmaMap.put("examine", Integer.valueOf(DictionaryConst.ExameStatus.EXAME_PASS_STATUS));
            if (StringUtils.isNotEmpty(langCode))
            {
                parmaMap.put("lang_code", langCode);
            }

            brandList = brandClassModelMapper.getBrandClassDynamic(parmaMap);

            List<Object> tempList = new ArrayList<>();
            if (productClassList.size() > 0)
            {
                tempList.add(productClassList);
            }
            resultMap.put("class_list", tempList);
            resultMap.put("brand_list", brandList);
        }
        catch (ClassCastException c)
        {
            logger.error("获取分类 失败", c);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSLXBZQ, "参数类型不正确", "getClassifiedBrands");
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取分类 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SJYC, "数据异常", "getClassifiedBrands");
        }

        return resultMap;
    }

    @Override
    public List<Map<String, Object>> attribute1(int storeId, PageModel pageModel, List<String> attributeList,Integer sid) throws LaiKeAPIException
    {
        //属性集
        List<Map<String, Object>> attributes = new ArrayList<>();
        try
        {
            Map<String, Object> parmaMap = new HashMap<>(16);
            List<Integer>       storeIds = new ArrayList<>();
            storeIds.add(0);
            storeIds.add(storeId);
            parmaMap.put("storeIds", storeIds);
            parmaMap.put("status", SkuModel.SKU_STATUS_TAKE_EFFECT);
            parmaMap.put("type", SkuModel.SKU_TYPE_ATTRIBUTE_NAME);
            parmaMap.put("is_examine", DictionaryConst.WhetherMaven.WHETHER_OK);
            parmaMap.put("add_date_sort", DataUtils.Sort.DESC.toString());
            if (pageModel != null)
            {
                parmaMap.put("pageStart", pageModel.getPageNo());
                parmaMap.put("pageEnd", pageModel.getPageSize());
            }
            if (Objects.nonNull(sid))
            {
                parmaMap.put("addPt",sid);
            }
            List<Map<String, Object>> attributeAllList = skuModelMapper.getAttributeDynamic(parmaMap);

            for (Map<String, Object> map : attributeAllList)
            {
                boolean             status      = false;
                Map<String, Object> attrbuteMap = new HashMap<>(16);
                attrbuteMap.put("id", map.get("id").toString());
                String name = map.get("name").toString();
                attrbuteMap.put("text", name);
                if (attributeList != null && attributeList.size() > 0)
                {
                    if (attributeList.contains(name))
                    {
                        status = true;
                    }
                }
                attrbuteMap.put("status", status);

                attributes.add(attrbuteMap);
            }

        }
        catch (ClassCastException c)
        {
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSLXBZQ, "参数类型不正确", "attribute1");
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取属性名称 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "attribute1");
        }

        return attributes;
    }

    @Override
    public List<Map<String, Object>> attribute1(MainVo vo, PageModel pageModel, List<String> attributeList, Integer sid) throws LaiKeAPIException
    {
        int storeId = vo.getStoreId();
        //属性集
        List<Map<String, Object>> attributes = new ArrayList<>();
        try
        {
            Map<String, Object> parmaMap = new HashMap<>(16);
            List<Integer>       storeIds = new ArrayList<>();
            storeIds.add(0);
            storeIds.add(storeId);
            parmaMap.put("storeIds", storeIds);
            parmaMap.put("status", SkuModel.SKU_STATUS_TAKE_EFFECT);
            parmaMap.put("type", SkuModel.SKU_TYPE_ATTRIBUTE_NAME);
            parmaMap.put("is_examine", DictionaryConst.WhetherMaven.WHETHER_OK);
            parmaMap.put("add_date_sort", DataUtils.Sort.DESC.toString());
            if (pageModel != null)
            {
                parmaMap.put("pageStart", pageModel.getPageNo());
                parmaMap.put("pageEnd", pageModel.getPageSize());
            }
            if (Objects.nonNull(sid))
            {
                parmaMap.put("addPt", sid);
            }
            String langCode = vo.getLang_code();
            if (StringUtils.isEmpty(langCode))
            {
                langCode = GloabConst.Lang.CN;
            }

            parmaMap.put("lang_code", langCode);

            List<Map<String, Object>> attributeAllList = skuModelMapper.getAttributeDynamic(parmaMap);

            for (Map<String, Object> map : attributeAllList)
            {
                boolean             status      = false;
                Map<String, Object> attrbuteMap = new HashMap<>(16);
                attrbuteMap.put("id", map.get("id").toString());
                String name = map.get("name").toString();
                attrbuteMap.put("text", name);
                if (attributeList != null && attributeList.size() > 0)
                {
                    if (attributeList.contains(name))
                    {
                        status = true;
                    }
                }
                attrbuteMap.put("status", status);

                attributes.add(attrbuteMap);
            }

        }
        catch (ClassCastException c)
        {
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSLXBZQ, "参数类型不正确", "attribute1");
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取属性名称 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "attribute1");
        }

        return attributes;
    }

    @Override
    public Map<String, Object> attributeName1(int storeId, String attributeName, Map<String, Object> attrValue,Integer sid) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            Map<String, Object> parmaMap = new HashMap<>(16);
            List<Integer>       storeIds = new ArrayList<>();
            storeIds.add(0);
            storeIds.add(storeId);
            parmaMap.put("storeIds", storeIds);
            parmaMap.put("name", attributeName);
            parmaMap.put("type", SkuModel.SKU_TYPE_ATTRIBUTE_NAME);
            parmaMap.put("status", SkuModel.SKU_STATUS_TAKE_EFFECT);
            if (Objects.nonNull(sid))
            {
                parmaMap.put("addSid",sid);
            }
            List<Map<String, Object>> attributeAllList = skuModelMapper.getAttributeDynamic(parmaMap);

            if (attributeAllList != null && attributeAllList.size() > 0)
            {

                //获取当前属性被选中的属性值
                List<Map<String, Object>> attrCheckedList = new ArrayList<>();
                if (attrValue != null && attrValue.containsKey("attr_list"))
                {
                    attrCheckedList = DataUtils.cast(attrValue.get("attr_list"));
                }
                for (Map<String, Object> map : attributeAllList)
                {
                    //属性Id
                    sid = Integer.parseInt(map.get("id").toString());
                    //属性名称
                    String mainName = map.get("name").toString();
                    //获取该类型下的属性值
                    parmaMap.clear();
                    parmaMap.put("type", SkuModel.SKU_TYPE_ATTRIBUTE_VALUE);
                    parmaMap.put("status", SkuModel.SKU_STATUS_TAKE_EFFECT);
                    parmaMap.put("sid", sid);
                    parmaMap.put("is_examine", DictionaryConst.WhetherMaven.WHETHER_OK);
                    List<Map<String, Object>> attributeValueList = skuModelMapper.getAttributeDynamic(parmaMap);
                    //处理属性值
                    for (Map<String, Object> valueMap : attributeValueList)
                    {
                        boolean valueStatus = false;
                        String  name        = valueMap.get("name").toString();
                        //判断是否被选中
                        if (attrCheckedList != null)
                        {
                            for (Map<String, Object> value : attrCheckedList)
                            {
                                String checkName = DataUtils.cast(value.get("attr_name"));
                                if (!StringUtils.isEmpty(checkName))
                                {
                                    if (name.equals(checkName))
                                    {
                                        valueStatus = true;
                                    }
                                }
                            }
                        }
                        valueMap.put("value", name);
                        valueMap.put("status", valueStatus);
                        valueMap.remove("name");
                    }
                    resultMap.put(mainName, attributeValueList);
                }
            }
            else
            {
                resultMap.put(attributeName, new ArrayList<>());
            }
        }
        catch (ClassCastException c)
        {
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSLXBZQ, "参数类型不正确", "attribute1");
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取属性值 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "attribute1");
        }

        return resultMap;
    }

    @Override
    //@Transactional(rollbackFor = Exception.class)
    public boolean addProduct(UploadMerchandiseVo vo, String adminName, int mchId, int type) throws LaiKeAPIException
    {
        //保存规格属性集
        List<ConfiGureModel> saveConfigGureList = new ArrayList<>();
        //是否为编辑商品
        boolean isEdit = false;
        try
        {
            if (vo.getActive() == null)
            {
                vo.setActive(1);
            }

            ProductListModel productListOld = null;
            if (vo.getpId() != null)
            {
                isEdit = true;
                productListOld = productListModelMapper.selectByPrimaryKey(vo.getpId());
            }

            ProductListModel productListModel = new ProductListModel();
            //判断商品名称是否重复
            //禅道44701 要求不判断重复
//            if (productListOld == null || !productListOld.getProduct_title().equals(vo.getProductTitle())) {
//                productListModel.setStore_id(vo.getStoreId());
//                productListModel.setProduct_title(vo.getProductTitle());
//                productListModel.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS.toString());
//                productListModel.setMch_id(mchId);
//                if (productListModelMapper.selectCount(productListModel) > 0) {
//                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_NYCZGSPQWZFTJ, "您有存在该商品,请勿重复添加");
//                }
//            }
            if (StringUtils.isEmpty(vo.getContent()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SJCW, "商品详情不能为空");
            }
            productListModel = new ProductListModel();
            if (isEdit)
            {
                productListModel.setId(vo.getpId());
            }
            //虚拟商品
            //是否是虚拟商品并且线下核销
            Boolean isVirtual = false;
            if (vo.getCommodityType().equals(ProductListModel.COMMODITY_TYPE.virtual))
            {
                if (StringUtils.isNotEmpty(vo.getCommodityType()))
                {
                    productListModel.setCommodity_type(vo.getCommodityType());
                    if (StringUtils.isNotEmpty(vo.getWriteOffSettings()))
                    {
                        Integer writeOffSettings = vo.getWriteOffSettings();
                        productListModel.setWrite_off_settings(writeOffSettings);
                        if (writeOffSettings.equals(ProductListModel.WRITE_OFF_SETTINGS.offline))
                        {
                            isVirtual = true;
                            productListModel.setIs_appointment(vo.getIsAppointment());
                            productListModel.setWrite_off_mch_ids(vo.getWriteOffMchIds());
                        }
                    }
                    else
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SJCW, "核销设置不能为空");
                    }
                }
                if (vo.getWriteOffSettings() == 1)
                {
                    Map<String, Object> params = new HashMap<String, Object>();
                    params.put("store_id", vo.getStoreId());
                    params.put("mch_id", mchId);
                    List<Map<String, Object>> maps = mchStoreModelMapper.selectDynamic(params);
                    if (CollectionUtils.isEmpty(maps))
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QXCJMD,"请先创建门店");
                    }
                }
            }
            productListModel.setLang_code(vo.getLang_code());
            productListModel.setCountry_num(vo.getCountry_num());
            if (vo.getLang_pid() != null)
            {
                productListModel.setLang_pid(vo.getLang_pid());
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
            //店铺序号
            productListModel.setMch_sort(vo.getMchSort());
            //处理商品类型
            List<Object> typeList = new ArrayList<>();
            if (StringUtils.isNotEmpty(vo.getsType()))
            {
                typeList = new ArrayList<>(Arrays.asList(vo.getsType().split(SplitUtils.DH)));
            }
            productListModel.setS_type(StringUtils.stringImplode(typeList, SplitUtils.DH, true));

            productListModel.setShow_adr(vo.getDisplayPosition());
            productListModel.setDistributor_id(vo.getDistributorId());
            if (vo.getCommodityType().equals(ProductListModel.COMMODITY_TYPE.InKind))
            {
                productListModel.setFreight(vo.getFreightId() + "");
            }
            productListModel.setActive(vo.getActive() + "");
            productListModel.setMch_id(mchId);
            productListModel.setMch_status(vo.getMchStatus() + "");
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
            //消息类型 1.订单(待发货) 2.订单(售后) 3.订单(提醒发货) 4.订单(订单关闭) 5.订单(新订单) 6.订单(收货) 7.商品(审核) 8.商品(下架) 9.商品(补货) 10.商品(新商品上架) 11.商品(分类) 12.商品(品牌)
            Integer       loggerGoodsType = null;
            StringBuilder loggerGoodsText = new StringBuilder(SplitUtils.DH);
            if (vo.getMchStatus() == null)
            {
                // 默认暂不审核
                vo.setMchStatus(DictionaryConst.GoodsMchExameStatus.EXAME_STOP_STATUS);
            }
            //非平台则需要审核
            if (GloabConst.LktConfig.LKT_CONFIG_TYPE_PT == type)
            {
                //平台发布商品直接审核通过
                loggerGoodsType = 10;
                loggerGoodsText.append("新商品上架");
                vo.setMchStatus(DictionaryConst.GoodsMchExameStatus.EXAME_PASS_STATUS);
            }
            else if (GloabConst.LktConfig.LKT_CONFIG_TYPE_MCH == type)
            {
                //判断是否是自营店铺,自营店铺直接跳过审核
                if (customerModelMapper.getStoreMchId(vo.getStoreId()).equals(mchId))
                {
                    logger.debug("店铺id{}为自营店铺,发布商品自动跳过审核", mchId);
                    loggerGoodsType = 10;
                    vo.setMchStatus(DictionaryConst.GoodsMchExameStatus.EXAME_PASS_STATUS);
                }
            }
            else if (GloabConst.LktConfig.LKT_CONFIG_TYPE_PC == type)
            {
                vo.setMchStatus(DictionaryConst.GoodsMchExameStatus.EXAME_WAIT_STATUS);
                loggerGoodsType = 7;
                loggerGoodsText.append("审核");
                //判断是否是自营店铺,自营店铺直接跳过审核
                if (customerModelMapper.getStoreMchId(vo.getStoreId()).equals(mchId))
                {
                    logger.debug("[pc管理后台]店铺id{}为自营店铺,发布商品自动跳过审核", mchId);
                    vo.setMchStatus(DictionaryConst.GoodsMchExameStatus.EXAME_PASS_STATUS);
                }
            }
            productListModel.setMch_status(vo.getMchStatus().toString());
            //如果是非自营店则自选字段为0
            Integer mchMainId = customerModelMapper.getStoreMchId(vo.getStoreId());
            productListModel.setIs_zixuan(DictionaryConst.WhetherMaven.WHETHER_NO + "");
            if (mchMainId.equals(mchId))
            {
                //自选
                productListModel.setIs_zixuan(DictionaryConst.WhetherMaven.WHETHER_OK + "");
            }
            //商品展示位置数据处理
            List<Object> adrList = new ArrayList<>(showAdrs);
            productListModel.setShow_adr(StringUtils.stringImplode(adrList, SplitUtils.DH, true));

            productListModel.setIs_distribution(isDistribution);
            productListModel.setPublisher(adminName);
            //视频文件
            if (vo.getVideo() != null && !vo.getVideo().isEmpty())
            {
                productListModel.setVideo(ImgUploadUtils.getUrlImgByName(vo.getVideo(), true));
            }
            else
            {
                productListModel.setVideo("");
            }
            if (vo.getProVideo() != null && !vo.getProVideo().isEmpty())
            {
                productListModel.setProVideo(ImgUploadUtils.getUrlImgByName(vo.getProVideo(), true));
            }
            else
            {
                productListModel.setProVideo("");
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
            //条形码
            String attrBraCode = null;
            //入库编码
            String attrName = null;
            logger.debug("新上传的商品规格参数 :{}", attrStr);
            List<Map<String, Object>> attrAllList = JSON.parseObject(attrStr, new TypeReference<List<Map<String, Object>>>()
            {
            });
            int stockNum = 0;
            //当前所有属性的图片
            Map<String, Object> attrValueImageMap = new HashMap<>(16);
            int                 index             = 0;
            for (Map<String, Object> map : attrAllList)
            {
                index++;
                //成本价
                if (StringUtils.isEmpty(map.get("sj")) || StringUtils.isEmpty(map.get("cbj")) || StringUtils.isEmpty(map.get("yj")) || StringUtils.isEmpty(map.get("img")))
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERRCODE_QWSSX, "请完善属性");
                }
                BigDecimal cbj    = new BigDecimal(map.get("cbj").toString());
                BigDecimal price  = new BigDecimal(map.get("sj").toString());
                BigDecimal yprice = new BigDecimal(map.get("yj").toString());
                String     unit   = map.get("unit").toString();
                if (cbj.compareTo(price) >= 0)
                {
                    throw new LaiKeAPIException(ErrorCode.SysErrorCode.ALL_CODE, String.format("第%s个规格成本价需低于售价", index));
                }

                //条形码
                attrBraCode = MapUtils.getString(map, "bar_code");
                Integer commodityType = vo.getCommodityType();
                //可核销次数则根据属性中的库存来
                Integer Write_off_num = MapUtils.getInteger(map, "kucun");
                attrName = MapUtils.getString(map, "name");
                //当前规格库存
                if (!map.containsKey("kucun") && !ProductListModel.COMMODITY_TYPE.virtual.equals(commodityType))
                {
                    logger.debug("保存商品的时候库存为空>>>{}", JSON.toJSONString(map));
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_KCBNWK, "属性库存不能为空");
                }
                Integer currentNum = MapUtils.getInteger(map, "kucun");
                if ((currentNum == null || currentNum == 0) && !ProductListModel.COMMODITY_TYPE.virtual.equals(commodityType))
                {
                    logger.debug("保存商品的时候库存为空>>>{}", JSON.toJSONString(map));
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_KCBNWK, "属性库存不能为空");
                }
                //isVirtual是否虚拟商品且线下核销，是线下核销则为ture
                if ((currentNum == null || currentNum == 0) && ProductListModel.COMMODITY_TYPE.virtual.equals(commodityType) && isVirtual)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_SCHXCSBNWK, "属性核销次数不能为空");
                }
                else if (ProductListModel.COMMODITY_TYPE.virtual.equals(commodityType))
                {
                    //当商品为虚拟商品时默认给99999999个库存，可核销次数则根据属性中的库存来
                    currentNum = 9999;
                }

                if (!Objects.isNull(vo.getSellType()))
                {
                    if (vo.getSellType() == 1)
                    {
                        currentNum = 9999;
                    }
                    else if (vo.getSellType() == 2)
                    {
                        currentNum = vo.getSellNum();
                    }
                }

                stockNum += currentNum;

                //[{"attr_group_name":"颜色","attr_list":[{"attr_name":"蓝色","status":true},{"attr_name":"天蓝"}]},
                // {"attr_group_name":"尺码","attr_list":[{"attr_name":"M","status":true}]}]
                List<Map<String, Object>> attrList = DataUtils.cast(map.get("attr_list"));
                if (attrList == null)
                {
                    attrList = new ArrayList<>();
                }
                //用于保存属性的集合 (颜色分类_LKT_144=花色_LKT_145...)
                Map<String, Object> attrbiuteStrMap    = new HashMap<>(16);
                ConfiGureModel      saveConfiGureModel = new ConfiGureModel();
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
                    saveSkuModel.setAdmin_name(adminName);
                    saveSkuModel.setType(SkuModel.SKU_TYPE_ATTRIBUTE_NAME);
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
                    //店铺发布、编辑商品时,设置的新属性,保存在SKU中,只要是商品未审核通过,则应不生效,只有审核通过或者平台手动开启方可生效;
                    if (productListModel.getMch_status().equals(DictionaryConst.GoodsMchExameStatus.EXAME_PASS_STATUS.toString()))
                    {
                        saveSkuModel.setStatus(SkuModel.SKU_STATUS_TAKE_EFFECT);
                    }
                    saveSkuModel.setRecycle(0);
                    if (mchMainId.equals(mchId))
                    {
                        //自选
                        saveSkuModel.setIs_examine(DictionaryConst.WhetherMaven.WHETHER_OK);
                    }
                    //根据属性名称获取属性id
                    int attrGourpNameId = getAttributeId(vo.getStoreId(), SkuModel.SKU_TYPE_ATTRIBUTE_NAME, attributeGroupName, 0);
                    if (attrGourpNameId > 0)
                    {
                        // 当属性名称ID不为0，SKU表里有数据
                        attrNameId = getAttributeId(vo.getStoreId(), SkuModel.SKU_TYPE_ATTRIBUTE_VALUE, attributeName, attrGourpNameId);
                        if (attrNameId == 0)
                        {
                            //没找到则添加属性
                            String code = this.getSkuCode(vo.getStoreId(), null, attrGourpNameId);
                            saveSkuModel.setCode(code);
                            saveSkuModel.setType(SkuModel.SKU_TYPE_ATTRIBUTE_VALUE);
                            saveSkuModel.setName(attributeName);
                            saveSkuModel.setAdd_date(new Date());
                            saveSkuModel.setSid(attrGourpNameId);
                            skuModelMapper.insertSelective(saveSkuModel);
                            attrNameId = saveSkuModel.getId();
                        }
                        else
                        {
                            //如果是表里有数据则强制把属性生效
                            SkuModel saveSkuUpdate = new SkuModel();
                            saveSkuUpdate.setId(attrNameId);
                            saveSkuUpdate.setStatus(SkuModel.SKU_STATUS_TAKE_EFFECT);
                            skuModelMapper.updateByPrimaryKeySelective(saveSkuUpdate);
                        }
                    }
                    else
                    {
                        //全部都没有则全部添加
                        String code = this.getSkuCode(vo.getStoreId(), attributeGroupName, null);
                        saveSkuModel.setCode(code);
                        saveSkuModel.setName(attributeGroupName);
                        saveSkuModel.setType(SkuModel.SKU_TYPE_ATTRIBUTE_NAME);
                        saveSkuModel.setSid(0);
                        saveSkuModel.setAdd_date(new Date());
                        skuModelMapper.insertSelective(saveSkuModel);
                        attrGourpNameId = saveSkuModel.getId();
                        code = this.getSkuCode(vo.getStoreId(), null, saveSkuModel.getId());
                        saveSkuModel.setId(null);
                        saveSkuModel.setSid(attrGourpNameId);
                        saveSkuModel.setCode(code);
                        saveSkuModel.setName(attributeName);
                        saveSkuModel.setType(SkuModel.SKU_TYPE_ATTRIBUTE_VALUE);
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
                saveConfiGureModel.setCostprice(cbj);
                saveConfiGureModel.setPrice(price);
                saveConfiGureModel.setYprice(yprice);
                saveConfiGureModel.setNum(currentNum);
                saveConfiGureModel.setTotal_num(currentNum);
                saveConfiGureModel.setBar_code(attrBraCode);
                saveConfiGureModel.setName(attrName);
                saveConfiGureModel.setUnit(unit);
                saveConfiGureModel.setWrite_off_num(Write_off_num);

                //序列化
                saveConfiGureModel.setAttribute(SerializePhpUtils.JavaSerializeByPhp(attrbiuteStrMap));
                //当前属性图片
                attrValueImageMap.put(saveConfiGureModel.getAttribute(), ImgUploadUtils.getUrlImgByName(map.get("img") + "", true));

                //如果是修改
                if (isEdit)
                {
                    //获取之前商品属性信息 看当前属性是否是之前的属性,如果是之前的属性则做修改 否则做添加
                    ConfiGureModel confiGureOld = new ConfiGureModel();
                    confiGureOld.setPid(productListModel.getId());
                    confiGureOld.setAttribute(saveConfiGureModel.getAttribute());
                    confiGureOld.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS.toString());
                    confiGureOld = confiGureModelMapper.selectOne(confiGureOld);
                    if (confiGureOld != null)
                    {
                        //标记无需回收的规格
                        notRecycleAttrIds.add(confiGureOld.getId());
                        //修改当前属性
                        ConfiGureModel confiGureNew = new ConfiGureModel();
                        confiGureNew.setId(confiGureOld.getId());
                        //规格图片处理
                        String currentImage = attrValueImageMap.get(confiGureOld.getAttribute()) + "";
                        if (!StringUtils.isEmpty(currentImage))
                        {
                            confiGureNew.setImg(currentImage);
                        }
                        confiGureNew.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS.toString());
                        confiGureNew.setCostprice(saveConfiGureModel.getCostprice());
                        confiGureNew.setYprice(saveConfiGureModel.getYprice());
                        confiGureNew.setPrice(saveConfiGureModel.getPrice());
                        confiGureNew.setBar_code(saveConfiGureModel.getBar_code());
                        //总库存只增不减 库存没有发生变化则不动库存
                        confiGureNew.setNum(confiGureOld.getNum());
                        confiGureNew.setTotal_num(confiGureOld.getTotal_num());
                        if (!confiGureOld.getNum().equals(saveConfiGureModel.getNum()))
                        {
                            confiGureNew.setNum(saveConfiGureModel.getNum());
                            //计算增加了多少库存
                            int totalNumOld = saveConfiGureModel.getTotal_num() - confiGureOld.getNum();
                            //总库存只增不减
                            if (totalNumOld > 0)
                            {
                                confiGureNew.setTotal_num(confiGureOld.getTotal_num() + totalNumOld);
                            }
                        }
                        confiGureNew.setUnit(saveConfiGureModel.getUnit());
                        int count = confiGureModelMapper.updateByPrimaryKeySelective(confiGureNew);
                        if (count < 1)
                        {
                            logger.info("修改商品规格信息失败 参数:" + JSON.toJSONString(productListModel));
                            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SXBCSB, "属性保存失败", "addProduct");
                        }
                        String content;

                        StockModel saveStockModel = new StockModel();
                        saveStockModel.setTotal_num(saveConfiGureModel.getTotal_num());
                        saveStockModel.setFlowing_num(confiGureOld.getTotal_num());
                        //记录库存
                        if (!confiGureOld.getNum().equals(saveConfiGureModel.getNum()))
                        {
                            //清空之前的库存记录重新记录
                            /*StockModel stockDel = new StockModel();
                            stockDel.setStore_id(vo.getStoreId());
                            stockDel.setProduct_id(productListModel.getId());
                            stockDel.setAttribute_id(confiGureOld.getId());
                            stockModelMapper.delete(stockDel);*/
                            String text = "增加商品规格库存";
                            int    num  = saveConfiGureModel.getNum() - confiGureOld.getNum();
                            if (num < 0)
                            {
                                text = "减少商品规格库存";
                            }
                            //添加库存信息
                            content = adminName + text + Math.abs(num);
                            saveStockModel.setId(null);
                            saveStockModel.setStore_id(vo.getStoreId());
                            saveStockModel.setProduct_id(productListModel.getId());
                            saveStockModel.setAttribute_id(confiGureNew.getId());
                            saveStockModel.setTotal_num(confiGureNew.getTotal_num());
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
                        if (productListModel.getMin_inventory() >= saveConfiGureModel.getNum())
                        {
                            content = "预警";
                            saveStockModel.setId(null);
                            saveStockModel.setType(StockModel.StockType.AGREEMENTTYPE_WAREHOUSING_WARNING);
                            saveStockModel.setContent(content);
                            saveStockModel.setUser_id(adminName);
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
                saveConfigGureList.add(saveConfiGureModel);
            }
            //第一张为商品主图
            productListModel.setImgurl(ImgUploadUtils.getUrlImgByName(goodsImgUrlList.get(0), true));
            productListModel.setNum(stockNum);
            productListModel.setAdd_date(new Date());
            //保存商品
            int count;
            if (isEdit)
            {
                productListModel.setMch_id(null);
                productListModel.setIs_zixuan(productListOld.getIs_zixuan());

                count = productListModelMapper.updateByPrimaryKeySelective(productListModel);
                //操作日志
                if (vo.getStoreType() == 8 || vo.getStoreType() == 7)
                {
                    publiceService.addAdminRecord(vo.getStoreId(), "修改了商品ID：" + productListModel.getId() + "的信息", AdminRecordModel.Type.UPDATE, vo.getAccessId());
                }

            }
            else
            {
                productListModel.setLabel("");

                //获取最新序号
                if (productListModel.getSort() == null)
                {
                    int sort = productListModelMapper.getGoodsMaxSort(vo.getStoreId());
                    productListModel.setSort(sort);
                }
                if (productListModel.getMch_sort() == null)
                {
                    int mchSort = productListModelMapper.getGoodsMaxMchSort(vo.getStoreId());
                    productListModel.setMch_sort(mchSort);
                }
                count = productListModelMapper.insertSelective(productListModel);
                //删除草稿箱
                if (count > 0)
                {
                    if (Objects.nonNull(vo.getDraftsId()))
                    {
                        draftsModelMapper.deleteByPrimaryKey(vo.getDraftsId());
                    }
                };
                /*if (productListModel.getMch_status().equals(DictionaryConst.GoodsMchExameStatus.EXAME_WAIT_STATUS.toString())){
                    //管理后台商品审核消息通知
                    MessageLoggingModal messageLoggingSave = new MessageLoggingModal();
                    messageLoggingSave.setStore_id(vo.getStoreId());
                    messageLoggingSave.setMch_id(productListModel.getMch_id());
                    messageLoggingSave.setType(MessageLoggingModal.Type.TYPE_GOODS_GOODS_EXAMINE);
                    messageLoggingSave.setParameter(productListModel.getId() + "");
                    messageLoggingSave.setContent("商品id为:" + productListModel.getId() + "商品名称为: " + productListModel.getProduct_title() +  "的商品需要管理员审核");
                    messageLoggingSave.setAdd_date(new Date());
                    messageLoggingSave.setTo_url(GoodsDataUtils.MCH_GOODS_URL);
                    messageLoggingModalMapper.insertSelective(messageLoggingSave);
                }*/
                //判断是否开启上传商品
                isUploadSwitch:
                //操作日志
                if (type == GloabConst.LktConfig.LKT_CONFIG_TYPE_PC)
                {
                    MchConfigModel mchConfigModel = new MchConfigModel();
                    mchConfigModel.setStore_id(vo.getStoreId());
                    mchConfigModel.setMch_id(mchMainId);
                    mchConfigModel = mchConfigModelMapper.selectOne(mchConfigModel);
                    if (mchConfigModel != null)
                    {
                        String[] settTypes = mchConfigModel.getCommodity_setup().split(SplitUtils.DH);
                        for (String isUploadSwitch : settTypes)
                        {
                            if ("1".equals(isUploadSwitch))
                            {
                                break isUploadSwitch;
                            }
                        }
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QSCFMT, "上传");
                    }
                }
                if (vo.getStoreType() == 8 || vo.getStoreType() == 7)
                {
                    publiceService.addAdminRecord(vo.getStoreId(), "添加了商品ID：" + productListModel.getId(), AdminRecordModel.Type.ADD, vo.getAccessId());
                }
            }
            //添加跳转路径
            publicAdminService.addJumpPath(vo, productListModel.getId() + "", productListModel.getProduct_title(), JumpPathModel.JumpType.JUMP_TYPE0_GOODS, JumpPathModel.JumpType.JUMP_TYPE_APP,
                    GloabConst.LaikeTuiUrl.JumpPath.GOODS_APP, new String[]{productListModel.getId() + ""}, mchId, StringUtils.isEmpty(productListModel.getLang_code()) ? "zh_CN" : productListModel.getLang_code());
            publicAdminService.addJumpPath(vo, productListModel.getId() + "", productListModel.getProduct_title(), JumpPathModel.JumpType.JUMP_TYPE0_GOODS, JumpPathModel.JumpType.JUMP_TYPE_PC,
                    GloabConst.LaikeTuiUrl.JumpPath.GOODS_PC, new String[]{productListModel.getId() + ""}, mchId, StringUtils.isEmpty(productListModel.getLang_code()) ? "zh_CN" : productListModel.getLang_code());
            if (count < 1)
            {
                logger.info("商品保存失败 参数:" + JSON.toJSONString(productListModel));
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SPBCSB, "商品保存失败", "addProduct");
            }
            productListModel = productListModelMapper.selectByPrimaryKey(productListModel.getId());
            //发布或者修改商品都需要平台审核
            if (productListModel.getMch_status().equals(DictionaryConst.GoodsMchExameStatus.EXAME_WAIT_STATUS.toString()))
            {
                //管理后台商品审核消息通知
                MessageLoggingModal messageLoggingSave = new MessageLoggingModal();
                messageLoggingSave.setStore_id(vo.getStoreId());
                messageLoggingSave.setMch_id(productListModel.getMch_id());
                messageLoggingSave.setType(MessageLoggingModal.Type.TYPE_GOODS_GOODS_EXAMINE);
                messageLoggingSave.setParameter(productListModel.getId() + "");
                messageLoggingSave.setContent("商品id为:" + productListModel.getId() + "商品名称为: " + productListModel.getProduct_title() + "的商品需要管理员审核");
                messageLoggingSave.setAdd_date(new Date());
                messageLoggingSave.setTo_url(GoodsDataUtils.MCH_GOODS_URL);
                messageLoggingModalMapper.insertSelective(messageLoggingSave);
            }
            //发布商品记录，暂时不需要
            /*if (loggerGoodsType != null) {
                MessageLoggingModal messageLoggingSave = new MessageLoggingModal();
                messageLoggingSave.setStore_id(vo.getStoreId());
                messageLoggingSave.setMch_id(productListModel.getMch_id());
                messageLoggingSave.setType(loggerGoodsType);
                messageLoggingSave.setParameter(productListModel.getId() + "");
                messageLoggingSave.setContent(productListModel.getProduct_title() + loggerGoodsText);
                messageLoggingSave.setAdd_date(new Date());
                messageLoggingModalMapper.insertSelective(messageLoggingSave);
            }*/
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
                saveImg.setSeller_id(adminName);
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
                else
                {
                    //禅道 50980
                    //秒杀插件预扣库存 同样进行回收规格的库存回收
                    Map<String, Object> resultMapOrder = null;
                    PluginsModel        secondsConfig  = pluginsModelMapper.getPluginInfo(DictionaryConst.Plugin.SECONDS, vo.getStoreId());
                    if (secondsConfig != null)
                    {
                        HashMap<String, Object> map = new HashMap<>();
                        map.put("attrIds", JSON.toJSONString(confiGureModelMapper.getAppointConfiGureId(productListModel.getId(), notRecycleAttrIds)));
                        Map<String, Object> paramApiMap = JSON.parseObject(JSON.toJSONString(map));
                        try
                        {
                            httpApiUtils.executeHttpApi("sec.http.deleteByAttrIds", paramApiMap, MediaType.MULTIPART_FORM_DATA_VALUE);
                        }
                        catch (Exception e)
                        {
                            logger.error("删除规格,秒杀商品远程调用异常:", e);
                        }
                    }
                    //积分插件 todo


                    //限时折扣
                }

            }

            //添加库存、规格信息
            for (ConfiGureModel confiGureModel : saveConfigGureList)
            {
                //规格图片处理
                String currentImage = ImgUploadUtils.getUrlImgByName(attrValueImageMap.get(confiGureModel.getAttribute()) + "", false);
                //验证属性图片是否上传
                if (StringUtils.isEmpty(currentImage))
                {
                    //没有图片则查看当前属性是否存在过，如果存在过则取之前图片
                    ConfiGureModel confiGureOld = new ConfiGureModel();
                    confiGureOld.setPid(productListModel.getId());
                    confiGureOld.setAttribute(confiGureModel.getAttribute());
                    confiGureOld = confiGureModelMapper.selectOne(confiGureOld);
                    if (confiGureOld == null)
                    {
                        logger.debug("属性id【{}】没有上传图片", confiGureModel.getId());
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QSCSXTP, "请上传属性图片");
                    }
                    else
                    {
                        //属性老图
                        currentImage = confiGureOld.getImg();
                        logger.debug("商品id{},属性字符串{},未上传新属性图片,延续用之前的图片", productListModel.getId(), confiGureModel.getAttribute());
                    }
                }
                confiGureModel.setImg(currentImage);

                //库存数量
                int attrNum = confiGureModel.getNum();
                confiGureModel.setNum(0);
                confiGureModel.setTotal_num(0);
                confiGureModel.setPid(productListModel.getId());
                confiGureModel.setMin_inventory(productListModel.getMin_inventory());
                confiGureModel.setBar_code(confiGureModel.getBar_code());
                confiGureModel.setName(attrName);
                confiGureModel.setColor("");
                confiGureModel.setCtime(new Date());
                confiGureModel.setUnit(productListModel.getWeight_unit());
                count = confiGureModelMapper.insertSelective(confiGureModel);
                if (count < 1)
                {
                    logger.info("库存记录添加失败 参数:" + JSON.toJSONString(confiGureModel));
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_KCJLTJSB, "库存记录添加失败", "addProduct");
                }

                //添加库存信息
                AddStockVo addStockVo = new AddStockVo();
                addStockVo.setStoreId(vo.getStoreId());
                addStockVo.setId(confiGureModel.getId());
                addStockVo.setPid(confiGureModel.getPid());
                addStockVo.setUpStockTotal(true);
                addStockVo.setAddNum(attrNum);
                publicStockService.addGoodsStock(addStockVo, adminName);
            }
            //同步商品总库存
            productClassModelMapper.synchroStock(productListModel.getId());
            vo.setpId(productListModel.getId());
            return true;
        }
        catch (ClassCastException c)
        {
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSLXBZQ, "参数类型不正确", "addProduct");
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("保存商品 异常:", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SPSCSB_001, "商品上传失败", "addProduct");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addProduct1(UploadMerchandiseVo vo, String adminName, int mchId, int type) throws LaiKeAPIException
    {
        //保存规格属性集
        List<ConfiGureModel> saveConfigGureList = new ArrayList<>();
        //是否为编辑商品
        boolean isEdit = false;
        try
        {
            if (vo.getActive() == null)
            {
                vo.setActive(1);
            }

            ProductListModel productListOld = null;
            if (vo.getpId() != null)
            {
                isEdit = true;
                productListOld = productListModelMapper.selectByPrimaryKey(vo.getpId());
            }

            ProductListModel productListModel = new ProductListModel();
            //判断商品名称是否重复
            //禅道44701 要求不判断重复
//            if (productListOld == null || !productListOld.getProduct_title().equals(vo.getProductTitle())) {
//                productListModel.setStore_id(vo.getStoreId());
//                productListModel.setProduct_title(vo.getProductTitle());
//                productListModel.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS.toString());
//                productListModel.setMch_id(mchId);
//                if (productListModelMapper.selectCount(productListModel) > 0) {
//                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_NYCZGSPQWZFTJ, "您有存在该商品,请勿重复添加");
//                }
//            }
            if (StringUtils.isEmpty(vo.getContent()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SJCW, "商品详情不能为空");
            }
            productListModel = new ProductListModel();
            if (isEdit)
            {
                productListModel.setId(vo.getpId());
            }
            //虚拟商品
            //是否是虚拟商品并且线下核销
            Boolean isVirtual = false;
            if (StringUtils.isNotEmpty(vo.getCommodityType()) && vo.getCommodityType().equals(ProductListModel.COMMODITY_TYPE.virtual))
            {
                productListModel.setCommodity_type(vo.getCommodityType());
                if (StringUtils.isNotEmpty(vo.getWriteOffSettings()))
                {
                    Integer writeOffSettings = vo.getWriteOffSettings();
                    productListModel.setWrite_off_settings(writeOffSettings);
                    if (writeOffSettings.equals(ProductListModel.WRITE_OFF_SETTINGS.offline))
                    {
                        isVirtual = true;
                        productListModel.setIs_appointment(vo.getIsAppointment());
                        productListModel.setWrite_off_mch_ids(vo.getWriteOffMchIds());
                    }
                }
                else
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SJCW, "核销设置不能为空");
                }
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
            //店铺序号
            productListModel.setMch_sort(vo.getMchSort());
            //处理商品类型
            List<Object> typeList = new ArrayList<>();
            if (StringUtils.isNotEmpty(vo.getsType()))
            {
                typeList = new ArrayList<>(Arrays.asList(vo.getsType().split(SplitUtils.DH)));
            }
            productListModel.setS_type(StringUtils.stringImplode(typeList, SplitUtils.DH, true));

            productListModel.setShow_adr(vo.getDisplayPosition());
            productListModel.setDistributor_id(vo.getDistributorId());
            if (vo.getCommodityType().equals(ProductListModel.COMMODITY_TYPE.InKind))
            {
                productListModel.setFreight(vo.getFreightId() + "");
            }
            productListModel.setActive(vo.getActive() + "");
            productListModel.setMch_id(mchId);
            productListModel.setMch_status(vo.getMchStatus() + "");
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
            //消息类型 1.订单(待发货) 2.订单(售后) 3.订单(提醒发货) 4.订单(订单关闭) 5.订单(新订单) 6.订单(收货) 7.商品(审核) 8.商品(下架) 9.商品(补货) 10.商品(新商品上架) 11.商品(分类) 12.商品(品牌)
            Integer       loggerGoodsType = null;
            StringBuilder loggerGoodsText = new StringBuilder(SplitUtils.DH);
            if (vo.getMchStatus() == null)
            {
                // 默认暂不审核
                vo.setMchStatus(DictionaryConst.GoodsMchExameStatus.EXAME_STOP_STATUS);
            }
            //非平台则需要审核
            if (GloabConst.LktConfig.LKT_CONFIG_TYPE_PT == type)
            {
                //平台发布商品直接审核通过
                loggerGoodsType = 10;
                loggerGoodsText.append("新商品上架");
                vo.setMchStatus(DictionaryConst.GoodsMchExameStatus.EXAME_PASS_STATUS);
            }
            else if (GloabConst.LktConfig.LKT_CONFIG_TYPE_MCH == type)
            {
                //判断是否是自营店铺,自营店铺直接跳过审核
                if (customerModelMapper.getStoreMchId(vo.getStoreId()).equals(mchId))
                {
                    logger.debug("店铺id{}为自营店铺,发布商品自动跳过审核", mchId);
                    loggerGoodsType = 10;
                    vo.setMchStatus(DictionaryConst.GoodsMchExameStatus.EXAME_PASS_STATUS);
                }
            }
            else if (GloabConst.LktConfig.LKT_CONFIG_TYPE_PC == type)
            {
                vo.setMchStatus(DictionaryConst.GoodsMchExameStatus.EXAME_WAIT_STATUS);
                loggerGoodsType = 7;
                loggerGoodsText.append("审核");
                //判断是否是自营店铺,自营店铺直接跳过审核
                if (customerModelMapper.getStoreMchId(vo.getStoreId()).equals(mchId))
                {
                    logger.debug("[pc管理后台]店铺id{}为自营店铺,发布商品自动跳过审核", mchId);
                    vo.setMchStatus(DictionaryConst.GoodsMchExameStatus.EXAME_PASS_STATUS);
                }
            }
            productListModel.setMch_status(vo.getMchStatus().toString());
            //如果是非自营店则自选字段为0
            Integer mchMainId = customerModelMapper.getStoreMchId(vo.getStoreId());
            productListModel.setIs_zixuan(DictionaryConst.WhetherMaven.WHETHER_NO + "");
            if (mchMainId.equals(mchId))
            {
                //自选
                productListModel.setIs_zixuan(DictionaryConst.WhetherMaven.WHETHER_OK + "");
            }
            //商品展示位置数据处理
            List<Object> adrList = new ArrayList<>(showAdrs);
            productListModel.setShow_adr(StringUtils.stringImplode(adrList, SplitUtils.DH, true));

            productListModel.setIs_distribution(isDistribution);
            productListModel.setPublisher(adminName);
            //视频文件
            if (vo.getVideo() != null && !vo.getVideo().isEmpty())
            {
                productListModel.setVideo(ImgUploadUtils.getUrlImgByName(vo.getVideo(), true));
            }
            else
            {
                productListModel.setVideo("");
            }
            if (vo.getProVideo() != null && !vo.getProVideo().isEmpty())
            {
                productListModel.setProVideo(ImgUploadUtils.getUrlImgByName(vo.getProVideo(), true));
            }
            else
            {
                productListModel.setProVideo("");
            }
            //校验商品数据
            productListModel = DataCheckTool.checkGoodsDataFormate(productListModel);

            //直接上架
            productListModel.setStatus("2");

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
            //条形码
            String attrBraCode = null;
            //入库编码
            String attrName = null;
            logger.debug("新上传的商品规格参数 :{}", attrStr);
            List<Map<String, Object>> attrAllList = JSON.parseObject(attrStr, new TypeReference<List<Map<String, Object>>>()
            {
            });
            int stockNum = 0;
            //当前所有属性的图片
            Map<String, Object> attrValueImageMap = new HashMap<>(16);
            int                 index             = 0;
            for (Map<String, Object> map : attrAllList)
            {
                index++;
                //成本价
                if (StringUtils.isEmpty(map.get("sj")) || StringUtils.isEmpty(map.get("cbj")) || StringUtils.isEmpty(map.get("yj")) || StringUtils.isEmpty(map.get("img")))
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERRCODE_QWSSX, "请完善属性");
                }
                BigDecimal cbj    = new BigDecimal(map.get("cbj").toString());
                BigDecimal price  = new BigDecimal(map.get("sj").toString());
                BigDecimal yprice = new BigDecimal(map.get("yj").toString());
                String     unit   = map.get("unit").toString();
                if (cbj.compareTo(price) >= 0)
                {
                    throw new LaiKeAPIException(ErrorCode.SysErrorCode.ALL_CODE, String.format("第%s个规格成本价需低于售价", index));
                }

                //条形码
                attrBraCode = MapUtils.getString(map, "bar_code");
                Integer commodityType = vo.getCommodityType();
                //可核销次数则根据属性中的库存来
                Integer Write_off_num = MapUtils.getInteger(map, "kucun");
                attrName = MapUtils.getString(map, "name");
                //当前规格库存
                if (!map.containsKey("kucun") && !ProductListModel.COMMODITY_TYPE.virtual.equals(commodityType))
                {
                    logger.debug("保存商品的时候库存为空>>>{}", JSON.toJSONString(map));
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_KCBNWK, "属性库存不能为空");
                }
                Integer currentNum = MapUtils.getInteger(map, "kucun");
                if ((currentNum == null || currentNum == 0) && !ProductListModel.COMMODITY_TYPE.virtual.equals(commodityType))
                {
                    logger.debug("保存商品的时候库存为空>>>{}", JSON.toJSONString(map));
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_KCBNWK, "属性库存不能为空");
                }
                //isVirtual是否虚拟商品且线下核销，是线下核销则为ture
                if ((currentNum == null || currentNum == 0) && ProductListModel.COMMODITY_TYPE.virtual.equals(commodityType) && isVirtual)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_SCHXCSBNWK, "属性核销次数不能为空");
                }
                else if (ProductListModel.COMMODITY_TYPE.virtual.equals(commodityType))
                {
                    //当商品为虚拟商品时默认给99999999个库存，可核销次数则根据属性中的库存来
                    currentNum = 99999999;
                }

                if (!Objects.isNull(vo.getSellType()))
                {
                    if (vo.getSellType() == 1)
                    {
                        currentNum = 99999999;
                    }
                    else if (vo.getSellType() == 2)
                    {
                        currentNum = vo.getSellNum();
                    }
                }

                stockNum += currentNum;

                //[{"attr_group_name":"颜色","attr_list":[{"attr_name":"蓝色","status":true},{"attr_name":"天蓝"}]},
                // {"attr_group_name":"尺码","attr_list":[{"attr_name":"M","status":true}]}]
                List<Map<String, Object>> attrList = DataUtils.cast(map.get("attr_list"));
                if (attrList == null)
                {
                    attrList = new ArrayList<>();
                }
                //用于保存属性的集合 (颜色分类_LKT_144=花色_LKT_145...)
                Map<String, Object> attrbiuteStrMap    = new HashMap<>(16);
                ConfiGureModel      saveConfiGureModel = new ConfiGureModel();
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
                    saveSkuModel.setAdmin_name(adminName);
                    saveSkuModel.setType(SkuModel.SKU_TYPE_ATTRIBUTE_NAME);
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

                    //店铺发布、编辑商品时,设置的新属性,保存在SKU中,只要是商品未审核通过,则应不生效,只有审核通过或者平台手动开启方可生效;
                    if (productListModel.getMch_status().equals(DictionaryConst.GoodsMchExameStatus.EXAME_PASS_STATUS.toString()))
                    {
                        saveSkuModel.setStatus(SkuModel.SKU_STATUS_TAKE_EFFECT);
                    }
                    saveSkuModel.setRecycle(0);
                    if (mchMainId.equals(mchId))
                    {
                        //自选
                        saveSkuModel.setIs_examine(DictionaryConst.WhetherMaven.WHETHER_OK);
                    }
                    //根据属性名称获取属性id
                    int attrGourpNameId = getAttributeId(vo.getStoreId(), SkuModel.SKU_TYPE_ATTRIBUTE_NAME, attributeGroupName, 0);
                    if (attrGourpNameId > 0)
                    {
                        // 当属性名称ID不为0，SKU表里有数据
                        attrNameId = getAttributeId(vo.getStoreId(), SkuModel.SKU_TYPE_ATTRIBUTE_VALUE, attributeName, attrGourpNameId);
                        if (attrNameId == 0)
                        {
                            //没找到则添加属性
                            String code = this.getSkuCode(vo.getStoreId(), null, attrGourpNameId);
                            saveSkuModel.setCode(code);
                            saveSkuModel.setType(SkuModel.SKU_TYPE_ATTRIBUTE_VALUE);
                            saveSkuModel.setName(attributeName);
                            saveSkuModel.setAdd_date(new Date());
                            saveSkuModel.setSid(attrGourpNameId);
                            skuModelMapper.insertSelective(saveSkuModel);
                            attrNameId = saveSkuModel.getId();
                        }
                        else
                        {
                            //如果是表里有数据则强制把属性生效
                            SkuModel saveSkuUpdate = new SkuModel();
                            saveSkuUpdate.setId(attrNameId);
                            saveSkuUpdate.setStatus(SkuModel.SKU_STATUS_TAKE_EFFECT);
                            skuModelMapper.updateByPrimaryKeySelective(saveSkuUpdate);
                        }
                    }
                    else
                    {
                        //全部都没有则全部添加
                        String code = this.getSkuCode(vo.getStoreId(), attributeGroupName, null);
                        saveSkuModel.setCode(code);
                        saveSkuModel.setName(attributeGroupName);
                        saveSkuModel.setType(SkuModel.SKU_TYPE_ATTRIBUTE_NAME);
                        saveSkuModel.setSid(0);
                        saveSkuModel.setAdd_date(new Date());
                        skuModelMapper.insertSelective(saveSkuModel);
                        attrGourpNameId = saveSkuModel.getId();
                        code = this.getSkuCode(vo.getStoreId(), null, saveSkuModel.getId());
                        saveSkuModel.setId(null);
                        saveSkuModel.setSid(attrGourpNameId);
                        saveSkuModel.setCode(code);
                        saveSkuModel.setName(attributeName);
                        saveSkuModel.setType(SkuModel.SKU_TYPE_ATTRIBUTE_VALUE);
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
                saveConfiGureModel.setCostprice(cbj);
                saveConfiGureModel.setPrice(price);
                saveConfiGureModel.setYprice(yprice);
                saveConfiGureModel.setNum(currentNum);
                saveConfiGureModel.setTotal_num(currentNum);
                saveConfiGureModel.setBar_code(attrBraCode);
                saveConfiGureModel.setName(attrName);
                saveConfiGureModel.setUnit(unit);
                saveConfiGureModel.setWrite_off_num(Write_off_num);

                //序列化
                saveConfiGureModel.setAttribute(SerializePhpUtils.JavaSerializeByPhp(attrbiuteStrMap));
                //当前属性图片
                attrValueImageMap.put(saveConfiGureModel.getAttribute(), ImgUploadUtils.getUrlImgByName(map.get("img") + "", true));

                //如果是修改
                if (isEdit)
                {
                    //获取之前商品属性信息 看当前属性是否是之前的属性,如果是之前的属性则做修改 否则做添加
                    ConfiGureModel confiGureOld = new ConfiGureModel();
                    confiGureOld.setPid(productListModel.getId());
                    confiGureOld.setAttribute(saveConfiGureModel.getAttribute());
                    confiGureOld.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS.toString());
                    confiGureOld = confiGureModelMapper.selectOne(confiGureOld);
                    if (confiGureOld != null)
                    {
                        //标记无需回收的规格
                        notRecycleAttrIds.add(confiGureOld.getId());
                        //修改当前属性
                        ConfiGureModel confiGureNew = new ConfiGureModel();
                        confiGureNew.setId(confiGureOld.getId());
                        //规格图片处理
                        String currentImage = attrValueImageMap.get(confiGureOld.getAttribute()) + "";
                        if (!StringUtils.isEmpty(currentImage))
                        {
                            confiGureNew.setImg(currentImage);
                        }
                        confiGureNew.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS.toString());
                        confiGureNew.setCostprice(saveConfiGureModel.getCostprice());
                        confiGureNew.setYprice(saveConfiGureModel.getYprice());
                        confiGureNew.setPrice(saveConfiGureModel.getPrice());
                        confiGureNew.setBar_code(saveConfiGureModel.getBar_code());
                        //总库存只增不减 库存没有发生变化则不动库存
                        confiGureNew.setNum(confiGureOld.getNum());
                        confiGureNew.setTotal_num(confiGureOld.getTotal_num());
                        if (!confiGureOld.getNum().equals(saveConfiGureModel.getNum()))
                        {
                            confiGureNew.setNum(saveConfiGureModel.getNum());
                            //计算增加了多少库存
                            int totalNumOld = saveConfiGureModel.getTotal_num() - confiGureOld.getNum();
                            //总库存只增不减
                            if (totalNumOld > 0)
                            {
                                confiGureNew.setTotal_num(confiGureOld.getTotal_num() + totalNumOld);
                            }
                        }
                        confiGureNew.setUnit(saveConfiGureModel.getUnit());
                        int count = confiGureModelMapper.updateByPrimaryKeySelective(confiGureNew);
                        if (count < 1)
                        {
                            logger.info("修改商品规格信息失败 参数:" + JSON.toJSONString(productListModel));
                            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SXBCSB, "属性保存失败", "addProduct");
                        }
                        String content;

                        StockModel saveStockModel = new StockModel();
                        saveStockModel.setTotal_num(saveConfiGureModel.getTotal_num());
                        saveStockModel.setFlowing_num(confiGureOld.getTotal_num());
                        //记录库存
                        if (!confiGureOld.getNum().equals(saveConfiGureModel.getNum()))
                        {
                            //清空之前的库存记录重新记录
                            /*StockModel stockDel = new StockModel();
                            stockDel.setStore_id(vo.getStoreId());
                            stockDel.setProduct_id(productListModel.getId());
                            stockDel.setAttribute_id(confiGureOld.getId());
                            stockModelMapper.delete(stockDel);*/
                            String text = "增加商品规格库存";
                            int    num  = saveConfiGureModel.getNum() - confiGureOld.getNum();
                            if (num < 0)
                            {
                                text = "减少商品规格库存";
                            }
                            //添加库存信息
                            content = adminName + text + Math.abs(num);
                            saveStockModel.setId(null);
                            saveStockModel.setStore_id(vo.getStoreId());
                            saveStockModel.setProduct_id(productListModel.getId());
                            saveStockModel.setAttribute_id(confiGureNew.getId());
                            saveStockModel.setTotal_num(confiGureNew.getTotal_num());
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
                        if (productListModel.getMin_inventory() >= saveConfiGureModel.getNum())
                        {
                            content = "预警";
                            saveStockModel.setId(null);
                            saveStockModel.setType(StockModel.StockType.AGREEMENTTYPE_WAREHOUSING_WARNING);
                            saveStockModel.setContent(content);
                            saveStockModel.setUser_id(adminName);
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
                saveConfigGureList.add(saveConfiGureModel);
            }
            //第一张为商品主图
            productListModel.setImgurl(ImgUploadUtils.getUrlImgByName(goodsImgUrlList.get(0), true));
            productListModel.setNum(stockNum);
            productListModel.setAdd_date(new Date());
            //保存商品
            int count;
            if (isEdit)
            {
                productListModel.setMch_id(null);
                productListModel.setIs_zixuan(productListOld.getIs_zixuan());
                count = productListModelMapper.updateByPrimaryKeySelective(productListModel);
                //操作日志
                if (vo.getStoreType() == 8 || vo.getStoreType() == 7)
                {
                    publiceService.addAdminRecord(vo.getStoreId(), "修改了商品ID：" + productListModel.getId() + "的信息", AdminRecordModel.Type.UPDATE, vo.getAccessId());
                }
            }
            else
            {
                productListModel.setLabel("");
                //获取最新序号
                if (productListModel.getSort() == null)
                {
                    int sort = productListModelMapper.getGoodsMaxSort(vo.getStoreId());
                    productListModel.setSort(sort);
                }
                if (productListModel.getMch_sort() == null)
                {
                    int mchSort = productListModelMapper.getGoodsMaxMchSort(vo.getStoreId());
                    productListModel.setMch_sort(mchSort);
                }
                count = productListModelMapper.insertSelective(productListModel);
                /*if (productListModel.getMch_status().equals(DictionaryConst.GoodsMchExameStatus.EXAME_WAIT_STATUS.toString())){
                    //管理后台商品审核消息通知
                    MessageLoggingModal messageLoggingSave = new MessageLoggingModal();
                    messageLoggingSave.setStore_id(vo.getStoreId());
                    messageLoggingSave.setMch_id(productListModel.getMch_id());
                    messageLoggingSave.setType(MessageLoggingModal.Type.TYPE_GOODS_GOODS_EXAMINE);
                    messageLoggingSave.setParameter(productListModel.getId() + "");
                    messageLoggingSave.setContent("商品id为:" + productListModel.getId() + "商品名称为: " + productListModel.getProduct_title() +  "的商品需要管理员审核");
                    messageLoggingSave.setAdd_date(new Date());
                    messageLoggingSave.setTo_url(GoodsDataUtils.MCH_GOODS_URL);
                    messageLoggingModalMapper.insertSelective(messageLoggingSave);
                }*/
                //判断是否开启上传商品
                isUploadSwitch:
                //操作日志
                if (type == GloabConst.LktConfig.LKT_CONFIG_TYPE_PC)
                {
                    MchConfigModel mchConfigModel = new MchConfigModel();
                    mchConfigModel.setStore_id(vo.getStoreId());
                    mchConfigModel.setMch_id(mchMainId);
                    mchConfigModel = mchConfigModelMapper.selectOne(mchConfigModel);
                    if (mchConfigModel != null)
                    {
                        String[] settTypes = mchConfigModel.getCommodity_setup().split(SplitUtils.DH);
                        for (String isUploadSwitch : settTypes)
                        {
                            if ("1".equals(isUploadSwitch))
                            {
                                break isUploadSwitch;
                            }
                        }
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QSCFMT, "上传");
                    }
                }
                if (vo.getStoreType() == 8 || vo.getStoreType() == 7)
                {
                    publiceService.addAdminRecord(vo.getStoreId(), "添加了商品ID：" + productListModel.getId(), AdminRecordModel.Type.ADD, vo.getAccessId());
                }
            }

            if (count < 1)
            {
                logger.info("商品保存失败 参数:" + JSON.toJSONString(productListModel));
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SPBCSB, "商品保存失败", "addProduct");
            }
            productListModel = productListModelMapper.selectByPrimaryKey(productListModel.getId());
            //添加跳转路径
            publicAdminService.addJumpPath(vo, productListModel.getId() + "", productListModel.getProduct_title(), JumpPathModel.JumpType.JUMP_TYPE0_GOODS, JumpPathModel.JumpType.JUMP_TYPE_APP,
                    GloabConst.LaikeTuiUrl.JumpPath.GOODS_APP, new String[]{productListModel.getId() + ""}, mchId, productListModel.getLang_code());
            publicAdminService.addJumpPath(vo, productListModel.getId() + "", productListModel.getProduct_title(), JumpPathModel.JumpType.JUMP_TYPE0_GOODS, JumpPathModel.JumpType.JUMP_TYPE_PC,
                    GloabConst.LaikeTuiUrl.JumpPath.GOODS_PC, new String[]{productListModel.getId() + ""}, mchId, productListModel.getLang_code());

            //发布或者修改商品都需要平台审核
            if (productListModel.getMch_status().equals(DictionaryConst.GoodsMchExameStatus.EXAME_WAIT_STATUS.toString()))
            {
                //管理后台商品审核消息通知
                MessageLoggingModal messageLoggingSave = new MessageLoggingModal();
                messageLoggingSave.setStore_id(vo.getStoreId());
                messageLoggingSave.setMch_id(productListModel.getMch_id());
                messageLoggingSave.setType(MessageLoggingModal.Type.TYPE_GOODS_GOODS_EXAMINE);
                messageLoggingSave.setParameter(productListModel.getId() + "");
                messageLoggingSave.setContent("商品id为:" + productListModel.getId() + "商品名称为: " + productListModel.getProduct_title() + "的商品需要管理员审核");
                messageLoggingSave.setAdd_date(new Date());
                messageLoggingSave.setTo_url(GoodsDataUtils.MCH_GOODS_URL);
                messageLoggingModalMapper.insertSelective(messageLoggingSave);
            }
            //发布商品记录，暂时不需要
            /*if (loggerGoodsType != null) {
                MessageLoggingModal messageLoggingSave = new MessageLoggingModal();
                messageLoggingSave.setStore_id(vo.getStoreId());
                messageLoggingSave.setMch_id(productListModel.getMch_id());
                messageLoggingSave.setType(loggerGoodsType);
                messageLoggingSave.setParameter(productListModel.getId() + "");
                messageLoggingSave.setContent(productListModel.getProduct_title() + loggerGoodsText);
                messageLoggingSave.setAdd_date(new Date());
                messageLoggingModalMapper.insertSelective(messageLoggingSave);
            }*/
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
                saveImg.setSeller_id(adminName);
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
                else
                {
                    //禅道 50980
                    //秒杀插件预扣库存 同样进行回收规格的库存回收
                    Map<String, Object> resultMapOrder = null;
                    PluginsModel        secondsConfig  = pluginsModelMapper.getPluginInfo(DictionaryConst.Plugin.SECONDS, vo.getStoreId());
                    if (secondsConfig != null)
                    {
                        HashMap<String, Object> map = new HashMap<>();
                        map.put("attrIds", JSON.toJSONString(confiGureModelMapper.getAppointConfiGureId(productListModel.getId(), notRecycleAttrIds)));
                        Map<String, Object> paramApiMap = JSON.parseObject(JSON.toJSONString(map));
                        try
                        {
                            httpApiUtils.executeHttpApi("sec.http.deleteByAttrIds", paramApiMap, MediaType.MULTIPART_FORM_DATA_VALUE);
                        }
                        catch (Exception e)
                        {
                            logger.error("删除规格,秒杀商品远程调用异常:", e);
                        }
                    }
                    //积分插件 todo


                    //限时折扣
                }

            }

            //添加库存、规格信息
            for (ConfiGureModel confiGureModel : saveConfigGureList)
            {
                //规格图片处理
                String currentImage = ImgUploadUtils.getUrlImgByName(attrValueImageMap.get(confiGureModel.getAttribute()) + "", false);
                //验证属性图片是否上传
                if (StringUtils.isEmpty(currentImage))
                {
                    //没有图片则查看当前属性是否存在过，如果存在过则取之前图片
                    ConfiGureModel confiGureOld = new ConfiGureModel();
                    confiGureOld.setPid(productListModel.getId());
                    confiGureOld.setAttribute(confiGureModel.getAttribute());
                    confiGureOld = confiGureModelMapper.selectOne(confiGureOld);
                    if (confiGureOld == null)
                    {
                        logger.debug("属性id【{}】没有上传图片", confiGureModel.getId());
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QSCSXTP, "请上传属性图片");
                    }
                    else
                    {
                        //属性老图
                        currentImage = confiGureOld.getImg();
                        logger.debug("商品id{},属性字符串{},未上传新属性图片,延续用之前的图片", productListModel.getId(), confiGureModel.getAttribute());
                    }
                }
                confiGureModel.setImg(currentImage);

                //库存数量
                int attrNum = confiGureModel.getNum();
                confiGureModel.setNum(0);
                confiGureModel.setTotal_num(0);
                confiGureModel.setPid(productListModel.getId());
                confiGureModel.setMin_inventory(productListModel.getMin_inventory());
                confiGureModel.setBar_code(confiGureModel.getBar_code());
                confiGureModel.setName(attrName);
                confiGureModel.setColor("");
                confiGureModel.setCtime(new Date());
                confiGureModel.setUnit(productListModel.getWeight_unit());
                count = confiGureModelMapper.insertSelective(confiGureModel);
                if (count < 1)
                {
                    logger.info("库存记录添加失败 参数:" + JSON.toJSONString(confiGureModel));
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_KCJLTJSB, "库存记录添加失败", "addProduct");
                }

                //添加库存信息
                AddStockVo addStockVo = new AddStockVo();
                addStockVo.setStoreId(vo.getStoreId());
                addStockVo.setId(confiGureModel.getId());
                addStockVo.setPid(confiGureModel.getPid());
                addStockVo.setUpStockTotal(true);
                addStockVo.setAddNum(attrNum);
                publicStockService.addGoodsStock(addStockVo, adminName);
            }
            //同步商品总库存
            productClassModelMapper.synchroStock(productListModel.getId());
            vo.setpId(productListModel.getId());
            return true;
        }
        catch (ClassCastException c)
        {
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSLXBZQ, "参数类型不正确", "addProduct");
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("保存商品 异常:", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SPSCSB_001, "商品上传失败", "addProduct");
        }
    }

    @Override
    public String getSkuCode(int storeId, String skuName, Integer sid) throws LaiKeAPIException
    {
        String code = "LKT_%s_%s";
        try
        {
            //最高循环次数-不限制次数可能出现死循环
            final int maxNum = 50;
            if (sid == null || sid == 0)
            {
                //生成父类属性code规则
                String skuNamePY = PinyinUtils.getPinYinHeadChar(skuName).toUpperCase();
                String codeTemp  = skuNamePY;
                int    count;
                int    index     = 0;
                do
                {
                    codeTemp = String.format(code, codeTemp, storeId);
                    SkuModel sku = new SkuModel();
                    sku.setSid(0);
                    sku.setCode(codeTemp);
                    count = skuModelMapper.selectCount(sku);
                    if (count > 0)
                    {
                        //父类重名+1
                        index++;
                        codeTemp = skuNamePY + index;
                    }
                    if (index > maxNum)
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.OPERATION_FAILED, "商品规格名称添加失败");
                    }
                }
                while (count > 0);
                code = codeTemp;
            }
            else
            {
                int index = 0;
                //获取属性上级code LKT_YS_0
                SkuModel sku = new SkuModel();
                sku.setId(sid);
                sku.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS);
                sku = skuModelMapper.selectOne(sku);
                if (sku == null)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SJBCZ, "上级不存在");
                }
                String thisCode = sku.getCode();
                //获取之前最新子级规则然后+1
                String code1 = skuModelMapper.getAttributeByCode(sid);
                String str   = "_";
                if (!StringUtils.isEmpty(code1))
                {
                    //属性子级拼接规则 LKT_name_storeId_子级名称_001(++)...
                    String codeTemp = code1.substring(code1.lastIndexOf("_"));
                    if (codeTemp.indexOf("R") > 0)
                    {
                        str += "R";
                    }
                    index = Integer.parseInt(code1.substring(code1.lastIndexOf(str) + str.length()));
                }
                //生成子属性code规则
                code = thisCode + str + String.format("%03d", index + 1);
            }
            //判断code是否重复重复则拼接数字
            SkuModel skuCount = new SkuModel();
            skuCount.setStore_id(storeId);
            skuCount.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS);
            int    count;
            int    num      = 0;
            String codeTemp = code;
            do
            {
                skuCount.setCode(codeTemp);
                count = skuModelMapper.selectCount(skuCount);
                //已存在的sku则继续拼接,直到名称不相同
                logger.debug("已存在的sku则继续拼接,直到名称不相同 {}", codeTemp);
                num++;
                //重名规则'R'
                codeTemp = code + String.format("_R%s", num);
                if (num > maxNum)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.OPERATION_FAILED, "商品规格名称添加失败");
                }
            }
            while (count > 0);

            if (num > 1)
            {
                code = codeTemp;
            }
        }
        catch (LaiKeAPIException e)
        {
            logger.error("获取属性代码 异常", e);
            throw e;
        }
        catch (Exception e)
        {
            logger.error("获取属性代码 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getSkuCode");
        }
        return code;
    }

    @Override
    public boolean delGoodsById(int storeId, int goodsId, Integer mchId) throws LaiKeAPIException
    {
        try
        {
            ProductListModel productListModel = productListModelMapper.selectByPrimaryKey(goodsId);
            if (productListModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SPBCZ, "商品不存在");
            }
            if (mchId != null)
            {
                if (!productListModel.getMch_id().equals(mchId))
                {
                    logger.info("商品回收失败 商品属于店铺{},传入的店铺id{}", productListModel.getMch_id(), mchId);
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_FFRQ, "非法入侵");
                }
            }
            //审核中的商品不能删除
            if (String.valueOf(DictionaryConst.GoodsMchExameStatus.EXAME_WAIT_STATUS).equals(productListModel.getMch_status()))
            {
                logger.debug("审核中的商品不能删除");
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SHZDSPBKSC, "审核中的商品不能删除");
            }

            ProductListModel delGoods = new ProductListModel();
            delGoods.setId(goodsId);
            delGoods.setRecycle(DictionaryConst.ProductRecycle.RECOVERY.toString());
            //回收商品
            int count = productListModelMapper.updateByPrimaryKeySelective(delGoods);
            if (count < 1)
            {
                logger.info("商品回收失败 参数:{}", JSON.toJSONString(delGoods));
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SPSCSB, "商品删除失败", "cancellationShop");
            }
            //回收商品属性
            count = confiGureModelMapper.delConfiGureInfo(goodsId);
            if (count < 1)
            {
                logger.info("商品属性回收失败,可能属性已经回收 商品id:{}", goodsId);
            }
            //删除商品图片列表
            ProductImgModel productImgModel = new ProductImgModel();
            productImgModel.setProduct_id(goodsId);
            count = productImgModelMapper.delete(productImgModel);
            if (count < 1)
            {
                logger.info("商品图片删除数量:{} 参数:{}", count, JSON.toJSONString(productImgModel));
            }
            //删除商品购物车数据
//            CartModel cartModel = new CartModel();
//            cartModel.setStore_id(storeId);
//            cartModel.setGoods_id(goodsId);
//            count = cartModelMapper.delete(cartModel);
//            if (count < 1) {
//                logger.info("购物车删除数量:{} 参数:{}", count, JSON.toJSONString(cartModel));
//            }
            //删除足迹
            UserFootprintModel userFootprintModel = new UserFootprintModel();
            userFootprintModel.setStore_id(storeId);
            userFootprintModel.setP_id(goodsId);
            count = userFootprintModelMapper.delete(userFootprintModel);
            if (count < 1)
            {
                logger.info("足迹删除数量:{} 参数:{}", count, JSON.toJSONString(userFootprintModel));
            }
            //删除收藏
//            count = userCollectionModelMapper.delGoodsOrMchCollection(storeId, goodsId, mchId);
//            if (count < 1) {
//                logger.info("收藏删除数量:{} 参数:{}", count, JSON.toJSONString(cartModel));
//            }
            //删除楼层管理中的商品  ->禅道 47878
            ProductMappingModel productMappingModel = new ProductMappingModel();
            productMappingModel.setProduct_id(goodsId);
            productMappingModelMapper.delete(productMappingModel);
            return true;
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("删除商品 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "delGoodsById");
        }
    }

    @Override
    public void upperAndLowerShelves(int storeId, String goodsIds, Integer mchId, Integer status) throws LaiKeAPIException
    {
        try
        {
            if (StringUtils.isEmpty(goodsIds))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误");
            }
            String[] goodsIdList = goodsIds.split(SplitUtils.DH);
            for (String goodsId : goodsIdList)
            {
                //获取商品信息
                ProductListModel productListModel = new ProductListModel();
                productListModel.setId(Integer.parseInt(goodsId));
                productListModel = productListModelMapper.selectByPrimaryKey(goodsId);
                if (productListModel == null)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SPBCZ, "商品不存在", "upperAndLowerShelves");
                }
                ProductListModel updateGoods = new ProductListModel();
                //虚拟商品特殊处理
                if (productListModel.getCommodity_type() == ProductListModel.COMMODITY_TYPE.virtual)
                {
                    updateGoods.setCommodity_type(ProductListModel.COMMODITY_TYPE.virtual);
                }
                updateGoods.setId(productListModel.getId());
                if (status == 0)
                {
                    //下架 删除轮播图
                    delBanner(storeId, goodsIds, "productId");
                    updateGoods.setStatus(DictionaryConst.GoodsStatus.OFFLINE_GROUNDING.toString());
                    //上架时间置空
                    productListModelMapper.setUpperTimeNull(Integer.parseInt(goodsId));
                }
                else
                {
                    if (DictionaryConst.GoodsStatus.VIOLATION.toString().equals(productListModel.getStatus()))
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_GSPYWGXJSJSB, "该商品已违规下架，上架失败", "upperAndLowerShelves");
                    }
                    else if (DictionaryConst.supplierGoodsStatus.UNABLE_TO_PAY.toString().equals(productListModel.getSupplier_status()))
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_GSPYDGSJSB, "该商品已断供，上架失败", "upperAndLowerShelves");
                    }

                    //商品上架 验证商品数据完整性
                    try
                    {
                        productListModel.setInitial(GloabConst.ManaValue.MANA_VALUE_PICK);
                        DataCheckTool.checkGoodsDataFormate(productListModel);
                        //验证商品是否有属性,没有属性则不能上架
                        ConfiGureModel confiGureModel = new ConfiGureModel();
                        confiGureModel.setPid(updateGoods.getId());
                        confiGureModel.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS.toString());
                        int count = confiGureModelMapper.selectCount(confiGureModel);
                        if (count < 1)
                        {
                            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SPMYSX, "商品没有属性");
                        }
                    }
                    catch (LaiKeAPIException l)
                    {
                        logger.info("商品{}上架失败 原因:{}", updateGoods.getId(), l.getMessage());
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QXQWSSPXX, "请先去完善商品信息", "upperAndLowerShelves");
                    }
                    if (productListModel.getNum() < 0)
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SJSBKCBZ, "上架失败:库存不足", "upperAndLowerShelves");
                    }
                    //标记商品上架
                    updateGoods.setStatus(status + "");
                    //上架时间
                    updateGoods.setUpper_shelf_time(new Date());
                }
                int count = productListModelMapper.updateByPrimaryKeySelective(updateGoods);
                if (count < 1)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CZSB, "操作失败", "upperAndLowerShelves");
                }
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
    }

    @Override
    public boolean delBanner(int storeId, String goodsIds, String str) throws LaiKeAPIException
    {
        try
        {
            //获取跳转url参数中包含的轮播图
            List<BannerModel> bannerModelList = bannerModelMapper.getBannersByUrl(storeId, str, goodsIds);
            for (BannerModel bannerModel : bannerModelList)
            {
                int count = bannerModelMapper.clearBannerById(storeId, bannerModel.getId());
                logger.info("删除轮播图id{} 执行结果:", count > 0);
            }
            return true;
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("删除轮播图 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "delBanner");
        }
    }

    @Override
    public boolean editProduct(UploadMerchandiseVo vo, String adminName, int mchId, int type) throws LaiKeAPIException
    {
        try
        {
            if (vo.getpId() != null && vo.getpId() > 0)
            {
                return addProduct(vo, adminName, mchId, type);
            }
            else
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSBWZ, "参数不完整", "editProduct");
            }
        }
        catch (ClassCastException c)
        {
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSLXBZQ, "参数类型不正确", "editProduct");
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("重新编辑商品 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "editProduct");
        }
    }

    @Override
    public List<Integer> getProductSettings(int storeId) throws LaiKeAPIException
    {
        List<Integer> goodsStautsList = new ArrayList<>();
        try
        {
            ProductConfigModel productConfigModel = new ProductConfigModel();
            productConfigModel.setStore_id(storeId);
            productConfigModel = productConfigModelMapper.selectOne(productConfigModel);
            if (productConfigModel != null)
            {
                goodsStautsList.add(DictionaryConst.ProductStatus.PRODUCTSTATUS_END_STATUS);
                if (productConfigModel.getIs_open().equals(ProductConfigModel.DISPLAY_BEAN_GOODS))
                {
                    goodsStautsList.add(DictionaryConst.ProductStatus.UNSOLD_STATUS);
                }
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取商品设置 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getProductSettings");
        }
        return goodsStautsList;
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
     * @author Trick
     * @date 2020/11/17 19:18
     */
    private int getAttributeId(int storeId, int type, String name, int sid) throws LaiKeAPIException
    {
        try
        {
            //参数列表
            Map<String, Object> parmaMap = new HashMap<>();
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void builderDefaultClassBrand(int storeId) throws LaiKeApiWarnException
    {

    }

    @Override
    public List<Map<String, String>> getGoodsLabelList(int storeId, List<String> labelId) throws LaiKeAPIException
    {
        List<Map<String, String>> resultTypeMapList = new ArrayList<>();
        try
        {
            if (labelId != null)
            {
                //标签缓存
                Map<String, Map<String, String>> goodsLabelMap = new HashMap<>(16);
                for (String type : labelId)
                {
                    if (StringUtils.isEmpty(type))
                    {
                        continue;
                    }
                    if (goodsLabelMap.containsKey(type))
                    {
                        resultTypeMapList.add(goodsLabelMap.get(type));
                        continue;
                    }
                    //获取标签
                    Map<String, String> labMap        = new HashMap<>(2);
                    ProLabelModel       proLabelModel = new ProLabelModel();
                    proLabelModel.setStore_id(storeId);
                    proLabelModel.setId(Integer.parseInt(type));
                    proLabelModel = proLabelModelMapper.selectOne(proLabelModel);
                    if (proLabelModel != null)
                    {
                        labMap.put("id", type);
                        labMap.put("name", proLabelModel.getName());
                        labMap.put("color", proLabelModel.getColor() + "");
                        goodsLabelMap.put(type, labMap);
                        resultTypeMapList.add(goodsLabelMap.get(type));
                    }
                }
            }
            return resultTypeMapList;
        }
        catch (LaiKeApiWarnException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取商品标签集合 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getGoodsLabelList");
        }
    }


    @Override
    public BigDecimal getGoodsFreight(User loginUser, int goodsId) throws LaiKeAPIException
    {
        BigDecimal freight = BigDecimal.ZERO;
        try
        {
            if (loginUser == null)
            {
                return freight;
            }
            UserAddress userAddress = new UserAddress();
            userAddress.setUid(loginUser.getUser_id());
            userAddress.setIs_default(1);
            userAddress.setStore_id(loginUser.getStore_id());
            userAddress = userAddressMapper.selectOne(userAddress);
            if (userAddress != null)
            {
                Map<String, Object> parmaMap = new HashMap<>(16);
                parmaMap.put("p_id", goodsId);
                List<Map<String, Object>> freightResList = productListModelMapper.getProductListJoinFreightDynamic(parmaMap);
                for (Map<String, Object> freightMap : freightResList)
                {
                    //商品详情运费就直接取默认运费规则
                    String defaultFreight = MapUtils.getString(freightMap, "default_freight", "");
                    if (StringUtils.isNotEmpty(defaultFreight))
                    {
                        DefaultFreightVO defaultFreightVO = JSON.parseObject(defaultFreight, DefaultFreightVO.class);
                        freight = new BigDecimal(defaultFreightVO.getNum2());
                    }
                }
            }
        }
        catch (LaiKeApiWarnException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取商品运费 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getGoodsFreight");
        }
        return freight;
    }

    @Override
    public Map<String, Object> dropDownProClassAndBrand(Integer storeId, Integer classId) throws LaiKeAPIException
    {
        Map<String, Object> result = new HashMap<>(16);
        //产品类别集
        List<Map<String, Object>> productClassList = new ArrayList<>();
        //品牌集
        List<Map<String, Object>> brandList;
        try
        {
            Integer classTopId = null;

            ProductClassModel productClassModel = new ProductClassModel();
            productClassModel.setStore_id(storeId);
            if (classId == null || classId == 0)
            {
                //获取产品顶级类别信息
                productClassModel.setSid(0);
                List<ProductClassModel> productClassModelList = productClassModelMapper.getProductClassLevel(productClassModel);
                for (ProductClassModel productClass : productClassModelList)
                {
                    Map<String, Object> productClassMap = new HashMap<>(16);
                    productClassMap.put("id", productClass.getCid());
                    productClassMap.put("name", productClass.getPname());
                    productClassList.add(productClassMap);
                }
                if (productClassModelList.size() > 0)
                {
                    classTopId = productClassModelList.get(0).getCid();
                }
            }
            else
            {
                //获取产品子类别信息
                productClassModel.setCid(classId);
                ProductClassModel productClassModelFirstLeve = productClassModelMapper.selectOne(productClassModel);
                //查询下级,没有下级则查询同级
                productClassModel = new ProductClassModel();
                productClassModel.setStore_id(storeId);
                productClassModel.setSid(classId);
                List<ProductClassModel> productClassLowLeves = productClassModelMapper.getProductClassLevel(productClassModel);
                if (productClassLowLeves != null)
                {
                    //递归获取顶级
                    classTopId = getClassTop(storeId, classId);
                    //获取所有下级类别信息
                    for (ProductClassModel productClass : productClassLowLeves)
                    {
                        Map<String, Object> productClassMap = new HashMap<>(16);
                        productClassMap.put("id", productClass.getCid());
                        productClassMap.put("name", productClass.getPname());
                        productClassList.add(productClassMap);
                    }
                }
                else
                {
                    //查询同级
                    productClassModel = new ProductClassModel();
                    productClassModel.setStore_id(storeId);
                    productClassModel.setSid(productClassModelFirstLeve.getSid());
                    List<ProductClassModel> productClassLeves = productClassModelMapper.getProductClassLevel(productClassModel);
                    for (ProductClassModel productClass : productClassLeves)
                    {
                        Map<String, Object> productClassMap = new HashMap<>(16);
                        //是否选中标识
                        boolean flag = productClass.getCid().equals(classId);
                        productClassMap.put("id", productClass.getCid());
                        productClassMap.put("name", productClass.getPname());
                        productClassList.add(productClassMap);
                    }
                }
            }

            //获取品牌信息
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", storeId);
            parmaMap.put("categories", classTopId);
            parmaMap.put("status", BrandClassModel.Status.OPEN);
            brandList = brandClassModelMapper.getBrandDynamic(parmaMap);

//            List<Object> tempList = new ArrayList<>();
//            if (productClassList.size() > 0) {
//                tempList.add(productClassList);
//            }
            result.put("class_list", productClassList);
            result.put("brand_list", brandList);
        }
        catch (LaiKeApiWarnException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取分类下拉框 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "dropDownProClass");
        }
        return result;
    }

    @Override
    public void batchSelectionOfLocations(MainVo vo, String goodsIds, String status)
    {
        try
        {
            if (StringUtils.isEmpty(goodsIds))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误");
            }
            String[] goodsIdList = goodsIds.split(SplitUtils.DH);
            for (String goodsId : goodsIdList)
            {
                //获取商品信息
                ProductListModel productListModel = new ProductListModel();
                productListModel.setId(Integer.parseInt(goodsId));
                productListModel = productListModelMapper.selectByPrimaryKey(goodsId);
                if (productListModel == null)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SPBCZ, "商品不存在", "batchSelectionOfLocations");
                }
                ProductListModel updateGoods = new ProductListModel();
                updateGoods.setCommodity_type(productListModel.getCommodity_type());
                updateGoods.setId(productListModel.getId());
                updateGoods.setShow_adr(SplitUtils.DH + status + SplitUtils.DH);
                int count = productListModelMapper.updateByPrimaryKeySelective(updateGoods);
                if (count < 1)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CZSB, "操作失败", "batchSelectionOfLocations");
                }
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("设置显示位置异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "batchSelectionOfLocations");
        }
    }

    @Override
    public void batchWarning(MainVo vo, String goodsIds, Integer minInventory)
    {
        try
        {
            if (StringUtils.isEmpty(goodsIds) || Objects.isNull(minInventory))
            {
                logger.error("goodsIds:{},minInventory:{}", goodsIds, minInventory);
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误");
            }
            if (minInventory.intValue() <= 0)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YJKCBUNWL, "预警值不能为0或小于0");
            }
            String[] goodsIdList = goodsIds.split(SplitUtils.DH);
            for (String goodsId : goodsIdList)
            {
                Integer pid = Integer.parseInt(goodsId);
                //获取商品信息
                ProductListModel productListModel = new ProductListModel();
                productListModel.setId(pid);
                productListModel = productListModelMapper.selectByPrimaryKey(goodsId);
                if (productListModel == null)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SPBCZ, "商品不存在", "batchWarning");
                }
                ProductListModel updateGoods = new ProductListModel();
                updateGoods.setId(productListModel.getId());
                updateGoods.setMin_inventory(minInventory);

                String              initialStr = productListModel.getInitial();
                Map<String, String> initialMap = DataUtils.cast(SerializePhpUtils.getUnserializeObj(initialStr, Map.class));
                if (initialMap != null)
                {
                    if (initialMap.containsKey("stockWarn"))
                    {
                        initialMap.put("stockWarn", minInventory + "");
                    }
                    updateGoods.setInitial(SerializePhpUtils.JavaSerializeByPhp(initialMap));
                }
                int count = productListModelMapper.updateByPrimaryKeySelective(updateGoods);
                if (count < 1)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CZSB, "操作失败", "batchWarning");
                }
                confiGureModelMapper.setConfigGureWarnNum(minInventory, pid);
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("设置预警异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "upperAndLowerShelves");
        }
    }

    @Override
    public void batchSetShippingFees(MainVo vo, String goodsIds, Integer fid)
    {
        try
        {
            if (StringUtils.isEmpty(goodsIds))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误");
            }
            String[] goodsIdList = goodsIds.split(SplitUtils.DH);
            for (String goodsId : goodsIdList)
            {
                //获取商品信息
                ProductListModel productListModel = new ProductListModel();
                productListModel.setId(Integer.parseInt(goodsId));
                productListModel = productListModelMapper.selectByPrimaryKey(goodsId);
                if (productListModel == null)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SPBCZ, "商品不存在", "batchSetShippingFees");
                }
                ProductListModel updateGoods = new ProductListModel();
                updateGoods.setId(productListModel.getId());
                updateGoods.setFreight(fid + "");
                int count = productListModelMapper.updateByPrimaryKeySelective(updateGoods);
                if (count < 1)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CZSB, "操作失败", "batchSetShippingFees");
                }
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("设置运费异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "batchSetShippingFees");
        }
    }

    @Autowired
    private ProductListModelMapper productListModelMapper;

    @Autowired
    private ProductClassModelMapper productClassModelMapper;
    @Autowired
    private OrderDetailsModelMapper orderDetailsModelMapper;
    @Autowired
    private FreightModelMapper      freightModelMapper;

    @Autowired
    private UserAddressMapper userAddressMapper;

    @Autowired
    private DictionaryListModelMapper dictionaryListModelMapper;

    @Autowired
    private BrandClassModelMapper brandClassModelMapper;

    @Autowired
    private SkuModelMapper skuModelMapper;

    @Autowired
    private ProductListModelExtendMapper productListModelExtendMapper;

    @Autowired
    private ConfiGureModelMapper confiGureModelMapper;

    @Autowired
    private PubliceService publiceService;

    @Autowired
    private ProductImgModelMapper productImgModelMapper;

    @Autowired
    private DistributionGradeModelMapper distributionGradeModelMapper;

    @Autowired
    private StockModelMapper stockModelMapper;

    @Autowired
    private CartModelMapper cartModelMapper;

    @Autowired
    private UserFootprintModelMapper userFootprintModelMapper;

    @Autowired
    private UserCollectionModelMapper userCollectionModelMapper;

    @Autowired
    private BannerModelMapper bannerModelMapper;

    @Autowired
    private PublicStockService       publicStockService;
    @Autowired
    private AdminModelMapper         adminModelMapper;
    @Autowired
    private ProductConfigModelMapper productConfigModelMapper;

    @Autowired
    private PublicAdminService publicAdminService;

    @Autowired
    private PublicDictionaryService publicDictionaryService;

    @Autowired
    private MessageLoggingModalMapper messageLoggingModalMapper;

    @Autowired
    private ProductMappingModelMapper productMappingModelMapper;

    @Autowired
    private PluginsModelMapper pluginsModelMapper;

    @Autowired
    private HttpApiUtils httpApiUtils;

}

