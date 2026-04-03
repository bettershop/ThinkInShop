package com.laiketui.admins.admin.services.supplier;

import com.laiketui.admins.api.admin.supplier.AdminSupplierProService;
import com.laiketui.common.api.PubliceService;
import com.laiketui.common.mapper.*;
import com.laiketui.common.utils.EasyPoiExcelUtil;
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
import com.laiketui.domain.config.SkuModel;
import com.laiketui.domain.log.AdminRecordModel;
import com.laiketui.domain.message.MessageLoggingModal;
import com.laiketui.domain.product.BrandClassModel;
import com.laiketui.domain.product.ProductClassModel;
import com.laiketui.domain.product.ProductListModel;
import com.laiketui.domain.supplier.SupplierConfigModel;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.Tool.ExcelParamVo;
import com.laiketui.domain.vo.supplier.GoodsQueryVo;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.*;

/**
 * 商品池管理
 *
 * @Author: sunH_
 * @Date: Create in 16:27 2022/9/15
 */
@Service
public class AdminSupplierProServiceImpl implements AdminSupplierProService
{

    private final Logger logger = LoggerFactory.getLogger(AdminSupplierProServiceImpl.class);

    @Autowired
    private SupplierProModelMapper supplierProModelMapper;

    @Autowired
    private SupplierModelMapper supplierModelMapper;

    @Autowired
    private PubliceService publiceService;

    @Autowired
    private ProductClassModelMapper productClassModelMapper;

    @Autowired
    private BrandClassModelMapper brandClassModelMapper;

    @Autowired
    private ConfiGureModelMapper confiGureModelMapper;

    @Autowired
    private SupplierConfigureModelMapper supplierConfigureModelMapper;

    @Autowired
    private CustomerModelMapper customerModelMapper;

    @Autowired
    private ProductListModelMapper productListModelMapper;

    @Autowired
    private ProductImgModelMapper productImgModelMapper;

    @Autowired
    private SupplierConfigModelMapper supplierConfigModelMapper;

    @Autowired
    private SkuModelMapper skuModelMapper;

    @Override
    public Map<String, Object> proList(GoodsQueryVo vo, HttpServletResponse response) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            Map<String, Object> paramMap = new HashMap<>(16);
            paramMap.put("storeId", vo.getStoreId());
            paramMap.put("add_date_sort", DataUtils.Sort.DESC.toString());
            paramMap.put("pageStart", vo.getPageNo());
            paramMap.put("pageEnd", vo.getPageSize());
            if (!Objects.isNull(vo.getGoodId()))
            {
                paramMap.put("goodsId", vo.getGoodId());
            }
            if (StringUtils.isNotEmpty(vo.getCondition()))
            {
                paramMap.put("condition", vo.getCondition());
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
            if (StringUtils.isNotEmpty(vo.getStatusList()))
            {
                List<String> statusList = Arrays.asList(vo.getStatusList().split(SplitUtils.DH));
                paramMap.put("statusList", statusList);
            }
            if (!Objects.isNull(vo.getMchStatus()))
            {
                paramMap.put("mch_status", vo.getMchStatus());
                if (vo.getMchStatus().equals(DictionaryConst.GoodsMchExameStatus.EXAME_PASS_STATUS))
                {
                    paramMap.put("examine_time_sort", DataUtils.Sort.DESC.toString());
                }
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
            if (StringUtils.isNotEmpty(vo.getStartTime()))
            {
                if (vo.getStatus() != null && vo.getStatus().equals(DictionaryConst.GoodsStatus.UNABLE_TO_PAY))
                {
                    paramMap.put("outageStartTime", vo.getStartTime());
                }
                else
                {
                    paramMap.put("startTime", vo.getStartTime());
                }
            }
            if (StringUtils.isNotEmpty(vo.getEndTime()))
            {
                if (vo.getStatus() != null && vo.getStatus().equals(DictionaryConst.GoodsStatus.UNABLE_TO_PAY))
                {
                    paramMap.put("outageEndTime", vo.getEndTime());
                }
                else
                {
                    paramMap.put("endTime", vo.getEndTime());
                }
            }
            List<Map<String, Object>> list  = new ArrayList<>();
            int                       count = supplierProModelMapper.countCondition(paramMap);
            if (count > 0)
            {
                list = supplierProModelMapper.selectCondition(paramMap);
                for (Map<String, Object> map : list)
                {
                    int                 goodsId     = MapUtils.getInteger(map, "id");
                    String              classIds    = MapUtils.getString(map, "product_class");
                    int                 brandId     = 0;
                    if( map.get("brand_id") == null )
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SJCW, "数据错误");
                    }
                    String              initialStr  = MapUtils.getString(map, "initial");
                    String              examineTime = MapUtils.getString(map, "examine_time");
                    Map<String, String> initialMap  = DataUtils.cast(SerializePhpUtils.getUnserializeObj(initialStr, Map.class));
                    if (initialMap == null)
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SJCW, "数据错误");
                    }
                    BigDecimal presentPrice = new BigDecimal(initialMap.get("sj"));
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
                        //查询库存信息
                        ConfiGureModel confiGureModel = new ConfiGureModel();
                        Integer        goodStockNum   = confiGureModelMapper.countConfigGureNum(goodsId);
                        map.put("num", goodStockNum);

                        //获取价格
                        confiGureModel = confiGureModelMapper.getProductMinPriceAndMaxYprice(goodsId);
                        if (confiGureModel != null)
                        {
                            unit = confiGureModel.getUnit();
                            presentPrice = confiGureModel.getYprice();
                        }

                        //获取商品主图
                        map.put("imgurl", publiceService.getImgPath(MapUtils.getString(map, "imgurl"), vo.getStoreId()));
                        map.put("unit", unit);
                        map.put("price", presentPrice);
                        map.put("add_date", DateUtil.dateFormate(MapUtils.getString(map, "add_date"), GloabConst.TimePattern.YMDHMS));
                        if (StringUtils.isNotEmpty(examineTime))
                        {
                            map.put("examine_time", DateUtil.dateFormate(examineTime, GloabConst.TimePattern.YMDHMS));
                        }
                        String outageTime = MapUtils.getString(map, "outage_time");
                        if (StringUtils.isNotEmpty(outageTime))
                        {
                            map.put("outage_time", DateUtil.dateFormate(outageTime, GloabConst.TimePattern.YMDHMS));
                        }
                        String upper_shelf_time = MapUtils.getString(map, "upper_shelf_time");
                        if (StringUtils.isNotEmpty(upper_shelf_time))
                        {
                            map.put("upper_shelf_time", DateUtil.dateFormate(MapUtils.getString(map, "upper_shelf_time"), GloabConst.TimePattern.YMDHMS));
                        }
                        String violation_time = MapUtils.getString(map, "violation_time");
                        if (StringUtils.isNotEmpty(violation_time))
                        {
                            map.put("violation_time", DateUtil.dateFormate(MapUtils.getString(map, "violation_time"), GloabConst.TimePattern.YMDHMS));
                        }
                        if (MapUtils.getInteger(map, "volume") < 0)
                        {
                            map.put("volume", 0);
                        }
                        map.put("volume", (MapUtils.getInteger(map, "volume") + (MapUtils.getInteger(map, "real_volume"))));
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
            //是否需要审核
            int                 isExamine = 1;
            SupplierConfigModel config    = supplierConfigModelMapper.getConfig(vo.getStoreId());
            if (!Objects.isNull(config))
            {
                isExamine = config.getIs_examine();
            }
            resultMap.put("isExamine", isExamine);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("供应商商品池列表 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "proList");
        }
        return resultMap;
    }

    @Autowired
    private MessageLoggingModalMapper messageLoggingModalMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void examine(MainVo vo, Integer id, Integer status, String remark) throws LaiKeAPIException
    {
        try
        {
            String           text             = "";
            String           envet            = "";
            ProductListModel productListModel = productListModelMapper.selectByPrimaryKey(id);
            if (Objects.isNull(productListModel))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_MBBCZ, "商品池商品信息不存在", "examine");
            }
            productListModel.setMch_status(status.toString());
            //商品sku信息
            Set<Integer>   attrIdList     = new HashSet<>();
            ConfiGureModel confiGureModel = new ConfiGureModel();
            confiGureModel.setPid(productListModel.getId());
            confiGureModel.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS.toString());
            List<ConfiGureModel> confiGureModelList = confiGureModelMapper.select(confiGureModel);
            for (ConfiGureModel confiGure : confiGureModelList)
            {
                String              attributeStr = confiGure.getAttribute();
                Map<String, Object> attributeMap = DataUtils.cast(SerializePhpUtils.getUnserializeObj(attributeStr, Map.class));
                if (attributeMap != null)
                {
                    //a:2:{s:12:"颜色_LKT_1";s:14:"白色_LKT_124";s:12:"尺码_LKT_8";s:7:"M_LKT_9";}
                    for (String key : attributeMap.keySet())
                    {
                        String attribyteKeyId   = key;
                        String attribyteValueId = attributeMap.get(key) + "";
                        int    indexKey         = attribyteKeyId.lastIndexOf("_") + 1;
                        int    indexValue       = attribyteValueId.lastIndexOf("_") + 1;
                        if (indexKey > 0)
                        {
                            //属性id
                            attribyteKeyId = attribyteKeyId.substring(indexKey);
                            attrIdList.add(Integer.parseInt(attribyteKeyId));
                        }
                        if (indexValue > 0)
                        {
                            //属性值id
                            attribyteValueId = attribyteValueId.substring(indexValue);
                            attrIdList.add(Integer.parseInt(attribyteValueId));
                        }
                    }
                }
            }
            if (status.equals(DictionaryConst.UserMchApply.ADOPT))
            {
                productListModel.setStatus(DictionaryConst.GoodsStatus.NEW_GROUNDING.toString());
                productListModel.setSupplier_status(DictionaryConst.supplierGoodsStatus.NEW_GROUNDING.toString());
                productListModel.setExamine_time(new Date());
                text = "商品" + productListModel.getProduct_title() + "审核通过";
                //获取未生效的属性
                Map<String, Object> parmaMap = new HashMap<>(16);
                parmaMap.put("attrIdList", attrIdList);
                parmaMap.put("status", SkuModel.SKU_STATUS_INVALID);
                List<Map<String, Object>> notTakeEffectSkuList = skuModelMapper.getAttributeDynamic(parmaMap);
                //属性生效
                for (Map<String, Object> map : notTakeEffectSkuList)
                {
                    Integer  attrId   = Integer.parseInt(String.valueOf(map.get("id")));
                    SkuModel skuModel = new SkuModel();
                    skuModel.setId(attrId);
                    skuModel.setStatus(SkuModel.SKU_STATUS_TAKE_EFFECT);
                    skuModel.setIs_examine(DictionaryConst.WhetherMaven.WHETHER_OK);
                    skuModelMapper.updateByPrimaryKeySelective(skuModel);
                }
                envet = "通过了供应商ID: " + productListModel.getGongyingshang() + "，商品ID：" + id + " 的审核";
            }
            else
            {
                if (StringUtils.isEmpty(remark))
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "拒绝原因不得为空", "examine");
                }
                productListModel.setRefuse_reasons(remark);
                text = "商品" + productListModel.getProduct_title() + "审核不通过";
                //删除未审核通过的sku
                Map<String, Object> parmaMap = new HashMap<>(16);
                parmaMap.put("attrIdList", attrIdList);
                parmaMap.put("is_examine", DictionaryConst.WhetherMaven.WHETHER_NO);
                List<Map<String, Object>> notTakeEffectSkuList = skuModelMapper.getAttributeDynamic(parmaMap);
                //属性生效
                for (Map<String, Object> map : notTakeEffectSkuList)
                {
                    Integer  attrId   = Integer.parseInt(String.valueOf(map.get("id")));
                    SkuModel skuModel = new SkuModel();
                    skuModel.setId(attrId);
                    skuModelMapper.deleteByPrimaryKey(skuModel);
                }
                envet = "拒绝了供应商ID: " + productListModel.getGongyingshang() + "，商品ID：" + id + " 的审核";
            }
            productListModelMapper.updateByPrimaryKeySelective(productListModel);
            MessageLoggingModal messageLoggingSave = new MessageLoggingModal();
            messageLoggingSave.setStore_id(vo.getStoreId());
            messageLoggingSave.setType(MessageLoggingModal.Type.TYPE_GOODS_GOODS_EXAMINE);
            messageLoggingSave.setParameter(productListModel.getId() + "");
            messageLoggingSave.setContent(text);
            messageLoggingSave.setAdd_date(new Date());
            messageLoggingSave.setSupplier_id(Integer.valueOf(productListModel.getGongyingshang()));
            messageLoggingModalMapper.insertSelective(messageLoggingSave);

            publiceService.addAdminRecord(vo.getStoreId(), envet, AdminRecordModel.Type.PASS_OR_REFUSE, vo.getAccessId());
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
    @Transactional(rollbackFor = Exception.class)
    public void del(MainVo vo, Integer id) throws LaiKeAPIException
    {
        try
        {
            ProductListModel productListModel = productListModelMapper.selectByPrimaryKey(id);
            if (Objects.isNull(productListModel))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SPBCZ, "商品不存在", "operation");
            }
            productListModel.setRecycle(DictionaryConst.ProductRecycle.RECOVERY.toString());
            ProductListModel productList = new ProductListModel();
            productList.setStore_id(vo.getStoreId());
            productList.setSupplier_superior(id);
            productList.setGongyingshang(String.valueOf(productListModel.getGongyingshang()));
            productList.setRecycle(String.valueOf(DictionaryConst.ProductRecycle.NOT_STATUS));
            List<ProductListModel> select = productListModelMapper.select(productList);
            if (select.size() > 0)
            {
                for (ProductListModel productOld : select)
                {
                    productOld.setRecycle(DictionaryConst.ProductRecycle.RECOVERY.toString());
                    productListModelMapper.updateByPrimaryKeySelective(productList);
                }
            }
            productListModelMapper.updateByPrimaryKeySelective(productListModel);

            publiceService.addAdminRecord(vo.getStoreId(), "删除了违规下架商品ID：" + id + " 的数据", AdminRecordModel.Type.DEL, vo.getAccessId());
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("删除供应商商品 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "del");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void violation(MainVo vo, Integer id, String remark, String img) throws LaiKeAPIException
    {
        try
        {
            ProductListModel productListModel = productListModelMapper.selectByPrimaryKey(id);
            if (Objects.isNull(productListModel))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_MBBCZ, "商品池商品信息不存在", "examine");
            }
            if (StringUtils.isEmpty(remark))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "违规原因不得为空", "violation");
            }
            productListModel.setStatus(DictionaryConst.GoodsStatus.VIOLATION.toString());
            productListModel.setLower_remark(remark);
            productListModel.setViolation_time(new Date());
            productListModel.setLower_image(img);
            productListModel.setSupplier_status(DictionaryConst.supplierGoodsStatus.UNABLE_TO_PAY.toString());
            productListModelMapper.updateByPrimaryKeySelective(productListModel);
            //通知后台消息
            MessageLoggingModal messageLoggingSave = new MessageLoggingModal();
            messageLoggingSave.setStore_id(vo.getStoreId());
            messageLoggingSave.setType(MessageLoggingModal.Type.TYPE_SUPPLIER_GOODS_OFF_THE_SHELF);
            messageLoggingSave.setParameter(String.valueOf(productListModel.getId()));
            messageLoggingSave.setContent(String.format("%s商品已被平台违规下架", productListModel.getProduct_title()));
            messageLoggingSave.setAdd_date(new Date());
            messageLoggingSave.setSupplier_id(Integer.valueOf(productListModel.getGongyingshang()));
            messageLoggingModalMapper.insertSelective(messageLoggingSave);
            ProductListModel paramModel = new ProductListModel();
            paramModel.setStore_id(vo.getStoreId());
            paramModel.setGongyingshang(productListModel.getGongyingshang());
            paramModel.setSupplier_superior(id);
            paramModel.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS.toString());
            List<ProductListModel> modelList = productListModelMapper.select(paramModel);
            for (ProductListModel model : modelList)
            {
                //通知后台消息
                messageLoggingSave = new MessageLoggingModal();
                messageLoggingSave.setStore_id(vo.getStoreId());
                messageLoggingSave.setType(MessageLoggingModal.Type.TYPE_GOODS_DEL);
                messageLoggingSave.setParameter(String.valueOf(model.getId()));
                messageLoggingSave.setContent(String.format("%s供应商商品已被平台违规下架,店铺选择的该商品将同步删除", model.getProduct_title()));
                messageLoggingSave.setAdd_date(new Date());
                messageLoggingSave.setMch_id(model.getMch_id());
                messageLoggingModalMapper.insertSelective(messageLoggingSave);
            }
            //删除其他店铺供应商商品
            productListModelMapper.violationStatus(id, productListModel.getGongyingshang());
            publiceService.addAdminRecord(vo.getStoreId(), "将商品ID：" + id + " 进行了违规下架", AdminRecordModel.Type.UPDATE, vo.getAccessId());
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("违规下架供应商商品异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "violation");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sortUpdate(MainVo vo, Integer id, Integer sort) throws LaiKeAPIException
    {
        try
        {
            ProductListModel productListModel = productListModelMapper.selectByPrimaryKey(id);
            if (Objects.isNull(productListModel))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_MBBCZ, "商品池商品信息不存在", "examine");
            }
            productListModel.setSort(sort);
            productListModelMapper.updateByPrimaryKeySelective(productListModel);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("修改排序值异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "violation");
        }
    }

    private void exportGoodsData(List<Map<String, Object>> goodsList, HttpServletResponse response) throws LaiKeAPIException
    {
        try
        {
            //表头
            String[] headerList = new String[]{"商品编号", "商品图片", "商品标题", "商品分类", "品牌", "供货价", "库存", "所属供应商", "销量", "发布时间", "排序"};
            //对应字段
            String[]     kayList = new String[]{"id", "imgurl", "product_title", "class_name", "brand_name", "price", "num", "supplier_name", "volume", "examine_time", "sort"};
            ExcelParamVo vo      = new ExcelParamVo();
            vo.setTitle("商品池列表");
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

    private ProductListModel synchronization(ProductListModel supplierProModel) throws LaiKeAPIException
    {
        ProductListModel productListModel = new ProductListModel();
        try
        {
            productListModel.setStore_id(supplierProModel.getStore_id());
            productListModel.setProduct_number(supplierProModel.getProduct_number());
            productListModel.setProduct_title(supplierProModel.getProduct_title());
            productListModel.setSubtitle(supplierProModel.getSubtitle());
            productListModel.setLabel(supplierProModel.getLabel());
            productListModel.setScan(supplierProModel.getScan());
            productListModel.setProduct_class(supplierProModel.getProduct_class());
            productListModel.setImgurl(supplierProModel.getImgurl());
            productListModel.setContent(supplierProModel.getContent());
            productListModel.setRichList(supplierProModel.getRichList());
            productListModel.setSort(supplierProModel.getSort());
            productListModel.setAdd_date(new Date());
            productListModel.setUpper_shelf_time(supplierProModel.getUpper_shelf_time());
            productListModel.setVolume(supplierProModel.getVolume());
            productListModel.setInitial(supplierProModel.getInitial());
            productListModel.setS_type(supplierProModel.getS_type());
            productListModel.setNum(supplierProModel.getNum());
            productListModel.setMin_inventory(supplierProModel.getMin_inventory());
            productListModel.setStatus(supplierProModel.getStatus().toString());
            productListModel.setBrand_id(supplierProModel.getBrand_id());
            productListModel.setIs_distribution(supplierProModel.getIs_distribution());
            productListModel.setIs_default_ratio(supplierProModel.getIs_default_ratio());
            productListModel.setKeyword(supplierProModel.getKeyword());
            productListModel.setWeight(supplierProModel.getWeight());
            productListModel.setWeight_unit(supplierProModel.getWeight_unit());
            productListModel.setDistributor_id(supplierProModel.getDistributor_id());
            productListModel.setFreight(supplierProModel.getFreight());
            productListModel.setIs_zhekou(supplierProModel.getIs_zhekou());
            productListModel.setSeparate_distribution(supplierProModel.getSeparate_distribution());
            productListModel.setRecycle(supplierProModel.getRecycle().toString());
            productListModel.setGongyingshang(supplierProModel.getGongyingshang());
            productListModel.setIs_hexiao(supplierProModel.getIs_hexiao().toString());
            productListModel.setHxaddress(supplierProModel.getHxaddress());
            productListModel.setActive(supplierProModel.getActive());
            productListModel.setMch_id(supplierProModel.getMch_id());
            productListModel.setMch_status(supplierProModel.getMch_status().toString());
            productListModel.setRefuse_reasons(supplierProModel.getRefuse_reasons());
            productListModel.setSearch_num(supplierProModel.getSearch_num());
            productListModel.setShow_adr(supplierProModel.getShow_adr());
            productListModel.setPublisher(supplierProModel.getPublisher());
            productListModel.setIs_zixuan(supplierProModel.getIs_zixuan().toString());
            productListModel.setSource(supplierProModel.getSource().toString());
            productListModel.setComment_num(supplierProModel.getComment_num());
            productListModel.setCover_map(supplierProModel.getCover_map());
            productListModel.setSupplier_superior(supplierProModel.getId());
            Integer mchId = customerModelMapper.getStoreMchId(supplierProModel.getStore_id());
            if (mchId == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QXCJZYD, "请先创建自营店", "add");
            }
            productListModel.setMch_id(mchId);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("添加商品数据 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "add");
        }
        return productListModel;
    }
}
