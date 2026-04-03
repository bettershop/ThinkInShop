package com.laiketui.admins.mch.services;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.laiketui.admins.api.mch.MchOrderService;
import com.laiketui.common.api.*;
import com.laiketui.common.mapper.*;
import com.laiketui.common.utils.EasyPoiExcelUtil;
import com.laiketui.common.utils.tool.ImgUploadUtils;
import com.laiketui.common.utils.tool.cache.RedisDataTool;
import com.laiketui.core.cache.RedisUtil;
import com.laiketui.core.common.SplitUtils;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.DictionaryConst;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.core.utils.tool.DataUtils;
import com.laiketui.core.utils.tool.DateUtil;
import com.laiketui.core.utils.tool.StringUtils;
import com.laiketui.domain.config.ExpressModel;
import com.laiketui.domain.config.ExpressSubtableModel;
import com.laiketui.domain.config.IntegralConfigModel;
import com.laiketui.domain.config.PrintSetupModel;
import com.laiketui.domain.file.FileDeliveryModel;
import com.laiketui.domain.log.AdminRecordModel;
import com.laiketui.domain.log.SignRecordModel;
import com.laiketui.domain.mch.AdminModel;
import com.laiketui.domain.mch.MchModel;
import com.laiketui.domain.order.ExpressDeliveryModel;
import com.laiketui.domain.order.OrderDetailsModel;
import com.laiketui.domain.order.OrderModel;
import com.laiketui.domain.order.ReturnOrderModel;
import com.laiketui.domain.product.CommentsImgModel;
import com.laiketui.domain.product.CommentsModel;
import com.laiketui.domain.product.ProductClassModel;
import com.laiketui.domain.product.ReplyCommentsModel;
import com.laiketui.domain.user.User;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.Tool.ExcelAnalysisVo;
import com.laiketui.domain.vo.Tool.ExcelParamVo;
import com.laiketui.domain.vo.main.RefundVo;
import com.laiketui.domain.vo.mch.FrontDeliveryVo;
import com.laiketui.domain.vo.order.*;
import com.laiketui.domain.vo.pc.MchPcReturnOrderVo;
import com.laiketui.domain.vo.query.GetCommentsDetailInfoVo;
import com.laiketui.root.common.BuilderIDTool;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.util.*;

/**
 * 订单管理
 *
 * @author Trick
 * @date 2021/6/2 17:28
 */
@Service
public class MchOrderServiceImpl implements MchOrderService
{

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private PublicOrderService publicOrderService;

    @Autowired
    private ReturnOrderModelMapper returnOrderModelMapper;

    @Autowired
    private OrderDetailsModelMapper orderDetailsModelMapper;

    @Autowired
    private ProductClassModelMapper productClassModelMapper;

    @Autowired
    private PubliceService publiceService;

    @Autowired
    private OrderModelMapper orderModelMapper;

    @Autowired
    private PublicRefundService publicRefundService;

    @Autowired
    private FileDeliveryModelMapper fileDeliveryModelMapper;

    @Autowired
    private ExpressModelMapper expressModelMapper;

    @Autowired
    private PublicStockService publicStockService;

    @Autowired
    private ProductListModelMapper productListModelMapper;

    @Autowired
    private PublicMchService publicMchService;

    @Autowired
    DataSourceTransactionManager dataSourceTransactionManager;
    @Autowired
    TransactionDefinition        transactionDefinition;
    @Autowired
    private PlatformTransactionManager platformTransactionManager;

    @Autowired
    private MchModelMapper mchModelMapper;

    @Autowired
    private CommentsModelMapper commentsModelMapper;

    @Autowired
    private CommentsImgModelMapper   commentsImgModelMapper;
    @Autowired
    private ReplyCommentsModelMapper replyCommentsModelMapper;

    @Autowired
    private CustomerModelMapper customerModelMapper;

    @Autowired
    private PrintSetupModelMapper printSetupModelMapper;

    @Autowired
    private ExpressSubtableModelMapper expressSubtableModelMapper;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Map<String, Object> index(AdminOrderListVo vo, HttpServletResponse response) throws LaiKeAPIException
    {
        Map<String, Object> resultMap;
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);
            vo.setMchId(user.getMchId());
            if (vo.getSelfLifting() == null)
            {
                vo.setSelfLifting(SelfLiftingType.ALL);
            }
            vo.setOperator(String.valueOf(GloabConst.LktConfig.LKT_CONFIG_TYPE_PC));
            resultMap = publicOrderService.pcMchOrderIndex(vo);
            if (vo.getExportType().equals(1))
            {
                exportOrderData(DataUtils.cast(resultMap.get("list")), response);
                return null;
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("我的订单 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "index");
        }
        return resultMap;
    }

    //导出订单列表
    private void exportOrderData(List<Map<String, Object>> list, HttpServletResponse response) throws LaiKeAPIException
    {
        try
        {
            //表头
            String[] headerList = new String[]{"订单编号", "订单明细id", "创单时间", "产品名称", "规格", "件数", "价格", "订单总计", "数量", "下单类型", "订单状态", "订单类型", "用户ID", "联系人"
                    , "电话", "地址", "支付方式", "运输方式", "物流单号", "运费"};
            //对应字段
            String[] kayList = new String[]{"orderno", "detailId", "createDate", "goodsName", "attrStr", "needNum", "goodsPrice", "orderPrice", "goodsNum", "operationTypeName", "status", "otype", "userId", "userName"
                    , "mobile", "addressInfo", "payName", "selfLiftingName", "courier_num", "freight"};
            ExcelParamVo vo = new ExcelParamVo();
            vo.setTitle("订单列表");
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
            logger.error("导出订单列表数据 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "exportOrderData");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Map<String, Object> returnList(MchPcReturnOrderVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);
            //售后数据
            Map<String, Object> returnParmaMap = new HashMap<>(16);
            returnParmaMap.put("store_id", vo.getStoreId());
            returnParmaMap.put("mch_id", user.getMchId());
            //禅道54756 【JAVA开发环境】订单（pc店铺）：插件的售后订单不应该在普通订单的售后列表显示
            returnParmaMap.put("otype", DictionaryConst.OrdersType.ORDERS_HEADER_GM);
            if (!StringUtils.isEmpty(vo.getOrderno()))
            {
                returnParmaMap.put("ordernoLike", vo.getOrderno());
            }
            if (vo.getOrderStauts() != null)
            {
                List<Integer> rTypeList = new ArrayList<>();
                if (vo.getOrderStauts().equals(OrderStatusEnum.TO_BE_REVIEWED))
                {
                    returnParmaMap.put("r_type", DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_EXAMEWAIT_STATUS);
                }
                else if (vo.getOrderStauts().equals(OrderStatusEnum.IN_REFUND))
                {
                    rTypeList.add(DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_AGREE_REBACK);
                    rTypeList.add(DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_USER_DELIVERED);
                    rTypeList.add(DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_QUICK_SUPPLIER_AGRESS);
                    returnParmaMap.put("rTypeList", rTypeList);
                    returnParmaMap.put("re_type", DictionaryConst.ReturnRecordReType.RETURNORDERTYPE_REFUSE_AMT);
                }
                else if (vo.getOrderStauts().equals(OrderStatusEnum.REFUND_SUCCESS))
                {
                    rTypeList.add(DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_RECEIVED_REBAKGOODS);
                    rTypeList.add(DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_AGREE_REBACK_AMT);
                    rTypeList.add(DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_REFUSE_ARTIFICIAL_OK);

                    returnParmaMap.put("rTypeList", rTypeList);
                }
                else if (vo.getOrderStauts().equals(OrderStatusEnum.REFUND_FAILED))
                {
                    rTypeList.add(DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_REFUSE_REBACKGOODS_AMT);
                    rTypeList.add(DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_REFUSE_REBACKGOODS);
                    rTypeList.add(DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_REFUSE_AMT);
                    returnParmaMap.put("rTypeList", rTypeList);
                    returnParmaMap.put("not_re_type", DictionaryConst.ReturnRecordReType.RETURNORDERSTATUS_GOODS_REBACK);
                }
                else if (vo.getOrderStauts().equals(OrderStatusEnum.IN_EXCHANGE))
                {
                    rTypeList.add(DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_AGREE_REBACK);
                    rTypeList.add(DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_USER_DELIVERED);
                    rTypeList.add(DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_AGREE_REBACK_GOODS);
                    returnParmaMap.put("rTypeList", rTypeList);
                    returnParmaMap.put("re_type", DictionaryConst.ReturnRecordReType.RETURNORDERSTATUS_GOODS_REBACK);
                }
                else if (vo.getOrderStauts().equals(OrderStatusEnum.EXCHANGE_SUCCESS))
                {
                    returnParmaMap.put("r_type", DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_AFTER_SALE_END);
                }
                else if (vo.getOrderStauts().equals(OrderStatusEnum.EXCHANGE_FAILED))
                {
                    rTypeList.add(DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_REFUSE_REBACKGOODS);
                    rTypeList.add(DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_REFUSE_AFTER_SALE);
                    returnParmaMap.put("rTypeList", rTypeList);
                    returnParmaMap.put("re_type", DictionaryConst.ReturnRecordReType.RETURNORDERSTATUS_GOODS_REBACK);
                }
            }
            if (StringUtils.isNotEmpty(vo.getStartDate()))
            {
                returnParmaMap.put("startDate", vo.getStartDate());
            }
            if (StringUtils.isNotEmpty(vo.getEndDate()))
            {
                returnParmaMap.put("endDate", vo.getEndDate());
            }

            returnParmaMap.put("re_time_sort", DataUtils.Sort.DESC.toString());
            returnParmaMap.put("r_type_sort", DataUtils.Sort.ASC.toString());
            returnParmaMap.put("pageStart", vo.getPageNo());
            returnParmaMap.put("pageEnd", vo.getPageSize());
            int                       total              = returnOrderModelMapper.countRturnOrderNumDynamic1(returnParmaMap);
            List<Map<String, Object>> returnOrderMapList = returnOrderModelMapper.selectRturnOrderNumDynamic1(returnParmaMap);
            for (Map<String, Object> map : returnOrderMapList)
            {
                String  returnType;
                Integer reType   = MapUtils.getInteger(map, "re_type");
                Integer refundId = MapUtils.getIntValue(map, "id");
                Double  actual_total = MapUtils.getDouble(map, "actual_total");
            /*    if (Objects.nonNull(actual_total))
                {
                    BigDecimal actualTotal = BigDecimal.valueOf(actual_total);
                    BigDecimal re_money = BigDecimal.valueOf(MapUtils.getDouble(map, "re_money"));
                    if (actualTotal.compareTo(BigDecimal.ZERO) > 0)
                    {
                        re_money = re_money.subtract(actualTotal);
                        map.put("re_money",re_money);
                    }
                }*/
                switch (reType)
                {
                    case 2:
                        returnType = "仅退款";
                        break;
                    case 1:
                        returnType = "退货退款";
                        break;
                    default:
                        returnType = "换货";
                        break;
                }
                map.put("returnTypeName", returnType);
                //region 是否可以审核
                boolean isExamine;
                map.put("isExamine", isExamine = publicRefundService.isExamine(vo.getStoreId(), refundId));
                //endregion

                //region 是否显示人工受理按钮 商品回寄之后被拒绝
                boolean isManExamine = false;
                if (!isExamine)
                {
                    isManExamine = publicRefundService.isMainExamine(vo.getStoreId(), refundId);
                }
                map.put("isManExamine", isManExamine);
                //endregion

                //实退金额
                String     realAmtName;
                BigDecimal realAmt = new BigDecimal(MapUtils.getString(map, "real_money"));
                if (realAmt.compareTo(BigDecimal.ZERO) == 0 && !DictionaryConst.ReturnRecordReType.RETURNORDERSTATUS_GOODS_REBACK.equals(reType))
                {
                    realAmtName = "未退款";
                }
                else if (DictionaryConst.ReturnRecordReType.RETURNORDERSTATUS_GOODS_REBACK.equals(reType))
                {
                    realAmtName = "-";
                }
                else
                {
                    realAmtName = realAmt.toString();
                }
                map.put("prompt", publicRefundService.getRefundStatus(vo.getStoreId(), MapUtils.getInteger(map, "id")));
                map.put("realAmtName", realAmtName);
            }
            if (vo.getExportType() == 1)
            {
                for (Map<String, Object> map : returnOrderMapList)
                {
                    if (map.containsKey("re_time"))
                    {
                        map.put("re_time", DateUtil.dateFormate(DataUtils.getStringVal(map, "re_time"), GloabConst.TimePattern.YMDHMS));
                    }
                }
                exportReturnOrderData(returnOrderMapList, vo.getResponse());
                return null;
            }

            resultMap.put("list", returnOrderMapList);
            resultMap.put("total", total);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("退款列表 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "returnList");
        }
        return resultMap;
    }

    @Override
    public List<Map<String, Object>> orderPrint(AdminOrderVo orderVo) throws LaiKeAPIException
    {
        try
        {
            User user = RedisDataTool.getRedisUserCache(orderVo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);
            //获取管理后台订单打印配置
            PrintSetupModel printSetupModel = new PrintSetupModel();
            printSetupModel.setStoreId(orderVo.getStoreId());
            printSetupModel.setMchId(user.getMchId());
            printSetupModel = printSetupModelMapper.selectOne(printSetupModel);
            HashMap<String, Object> printSetupMap = new HashMap<>();
            if (printSetupModel != null)
            {
                printSetupMap.put("printName", printSetupModel.getPrintName());
                printSetupMap.put("printUrl", printSetupModel.getPrintUrl());
                printSetupMap.put("printSheng", printSetupModel.getSheng());
                printSetupMap.put("printShi", printSetupModel.getShi());
                printSetupMap.put("printxian", printSetupModel.getXian());
                printSetupMap.put("printAddress", printSetupModel.getAddress());
                printSetupMap.put("printPhone", printSetupModel.getPhone());
            }
            MchModel mchModel = mchModelMapper.selectByPrimaryKey(user.getMchId());
            //订单号
            String                    sNos             = orderVo.getsNo();
            String[]                  orderNos         = sNos.split(SplitUtils.DH);
            List<Map<String, Object>> orderDetailsInfo = new ArrayList<>();
            for (String sNo : orderNos)
            {
                AdminOrderDetailVo adminOrderDetailVo = new AdminOrderDetailVo();
                adminOrderDetailVo.setStoreId(orderVo.getStoreId());
                adminOrderDetailVo.setId(sNo);
                adminOrderDetailVo.setOperationName(mchModel.getName());
                Map<String, Object> tmpOrderDetails = publicOrderService.orderPcDetails(adminOrderDetailVo);
                tmpOrderDetails.putAll(printSetupMap);
                Map<String, Object> tmpMap = Maps.newHashMap();
                tmpMap.put("list", tmpOrderDetails);
                orderDetailsInfo.add(tmpMap);
            }
            return orderDetailsInfo;
        }
        catch (LaiKeAPIException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            logger.error("打印订单失败 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "orderPrint");
        }
    }

    @Override
    public Map<String, Object> orderCount(MainVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User                user     = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", vo.getStoreId());
            parmaMap.put("mchId", user.getMchId());
            //parmaMap.put("status", DictionaryConst.OrdersStatus.ORDERS_R_STATUS_CONSIGNMENT);
            parmaMap.put("mchRecycle", OrderModel.SHOW);
            parmaMap.put("recycle", OrderModel.RECYCLE_SHOW + "");
            //统计代发货的订单数量
            Map<String, Object> parmaOrderMap = new HashMap<>(16);
            parmaOrderMap.putAll(parmaMap);
            parmaOrderMap.put("orderType", DictionaryConst.OrdersType.ORDERS_HEADER_GM);
            int orderNum = orderModelMapper.countAdminOrderList(parmaOrderMap);
            //统计待发货 实物 订单
            Map<String, Object> parmaMap1 = new HashMap<>(16);
            parmaMap1.putAll(parmaMap);
            parmaMap1.put("orderType", DictionaryConst.OrdersType.ORDERS_HEADER_GM);
            parmaMap1.put("self_lifting", 0);
            int shiWuNum = orderModelMapper.countAdminOrderList(parmaMap1);
            //统计 活动 订单
            Map<String, Object> parmaMap2 = new HashMap<>(16);
            parmaMap2.putAll(parmaMap);
            List<String> list = new ArrayList<>();
            list.add(DictionaryConst.OrdersType.ORDERS_HEADER_GM);
            list.add(DictionaryConst.OrdersType.ORDERS_HEADER_VI);
            parmaMap2.put("self_lifting ", 0);
            parmaMap2.put("orderTypeList_not", list);
            int activityNum = orderModelMapper.countAdminOrderList(parmaMap2);

            //统计 退货列表 订单
            int returnNum = returnOrderModelMapper.countOrderReturnWaitByStoreStatus(vo.getStoreId(), DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_EXAMEWAIT_STATUS, DictionaryConst.OrdersType.ORDERS_HEADER_GM);

            //统计秒杀订单
            OrderModel orderSecCount = new OrderModel();
            orderSecCount.setStore_id(vo.getStoreId());
            orderSecCount.setOtype(DictionaryConst.OrdersType.ORDERS_HEADER_MS);
            orderSecCount.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS);
            orderSecCount.setStatus(DictionaryConst.OrdersStatus.ORDERS_R_STATUS_CONSIGNMENT);
            int secOrderNum = orderModelMapper.selectCount(orderSecCount);
            //统计积分订单
            orderSecCount.setOtype(DictionaryConst.OrdersType.ORDERS_HEADER_IN);
            int inOrderNum = orderModelMapper.selectCount(orderSecCount);

            resultMap.put("orderNum", orderNum);
            resultMap.put("shiWuNum", shiWuNum);
            resultMap.put("activityNum", activityNum);
            resultMap.put("returnNum", returnNum);
            resultMap.put("secOrderNum", secOrderNum);
            resultMap.put("inOrderNum", inOrderNum);

        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("订单统计 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "orderCount");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> deliverList(MainVo vo, String orderNo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User                      user      = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);
            List<Map<String, Object>> goodsList = new ArrayList<>();

            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", vo.getStoreId());
            parmaMap.put("orderno", orderNo);
            parmaMap.put("r_status", DictionaryConst.OrdersStatus.ORDERS_R_STATUS_CONSIGNMENT);
            parmaMap.put("deliverNum", "deliverNum");
            parmaMap.put("pageStart", vo.getPageNo());
            parmaMap.put("pageEnd", vo.getPageSize());
            //获取订单信息
            List<Map<String, Object>> orderInfoList = orderDetailsModelMapper.selectOrderInfoListDynamce(parmaMap);
            int                       total         = orderDetailsModelMapper.countOrderInfoListDynamce(parmaMap);
            for (Map<String, Object> map : orderInfoList)
            {
                String imgUrl = map.get("imgurl") + "";
                imgUrl = publiceService.getImgPath(imgUrl, vo.getStoreId());
                map.put("imgurl", imgUrl);
                //商品类别id集
                String   goodsClassIdStr  = map.get("product_class").toString();
                String[] goodsClassIdList = StringUtils.trim(goodsClassIdStr, "-").split("-");
                //获取当前商品最小类别
                String            minClassId        = goodsClassIdList[goodsClassIdList.length - 1];
                ProductClassModel productClassModel = new ProductClassModel();
                productClassModel.setCid(StringUtils.stringParseInt(minClassId));
                productClassModel = productClassModelMapper.selectOne(productClassModel);
                if (productClassModel != null)
                {
                    map.put("class_name", productClassModel.getPname());
                }
                //原来显示成本价 现在显示售价
                map.put("costprice", map.get("price"));
                Integer          id               = MapUtils.getInteger(map, "id");
                ReturnOrderModel returnOrderModel = new ReturnOrderModel();
                returnOrderModel.setP_id(id);
                returnOrderModel.setR_type(0);
                returnOrderModel = returnOrderModelMapper.selectOne(returnOrderModel);
                if (Objects.isNull(returnOrderModel))
                {
                    goodsList.add(map);
                }
                //可发货数量
                map.put("deliverNum", MapUtils.getIntValue(map, "num") - MapUtils.getIntValue(map, "deliver_num"));
                resultMap.put("self_lifting", DataUtils.getStringVal(orderInfoList.get(0), "self_lifting"));
            }
            resultMap.put("logistics_type", publicExpressService.getMchHaveExpressSubtableByMchId(vo, user.getMchId()));
            resultMap.put("goods", goodsList);
            resultMap.put("total", total);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("订单发货列表 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "deliverList");
        }
        return resultMap;
    }

    @Override
//    @Transactional(rollbackFor = Exception.class)
    public boolean batchDelivery(MainVo vo, List<MultipartFile> image) throws LaiKeAPIException
    {
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);

            ExcelAnalysisVo excelAnalysisVo = new ExcelAnalysisVo();
            excelAnalysisVo.setFile(image);
            List<String> titleNames = new ArrayList<>();
            titleNames.add("订单号");
            titleNames.add("订单明细id");
            titleNames.add("物流公司名称");
            titleNames.add("物流单号");
            excelAnalysisVo.setTitleName(titleNames);
            List<String> valueNames = new ArrayList<>();
            valueNames.add("orderNo");
            valueNames.add("detailId");
            valueNames.add("wuName");
            valueNames.add("wuNo");
            excelAnalysisVo.setValueKey(valueNames);
            Map<String, Object>       resultMap     = EasyPoiExcelUtil.analysisExcel(excelAnalysisVo);
            List<Map<String, Object>> excelDataList = DataUtils.cast(resultMap.get("list"));
            if (excelDataList != null)
            {
                //设置回滚点
//                Object savePoint = TransactionAspectSupport.currentTransactionStatus().createSavepoint();
                String                    fileName  = image.get(0).getOriginalFilename();
                StringBuilder             errorText = new StringBuilder();
                OrderDetailsModel         orderDetailsModel;
                List<Map<String, Object>> detailIdList;
                HashMap<String, Object>   detailMap;
                for (Map<String, Object> map : excelDataList)
                {
                    int x = MapUtils.getIntValue(map, "x");
                    if (StringUtils.isNotEmpty(MapUtils.getString(map, "errorText")))
                    {
                        errorText.append(MapUtils.getString(map, "errorText"));
                    }
                    if (map.containsKey("orderNo"))
                    {
                        //商品发货信息
                        String  orderNo  = MapUtils.getString(map, "orderNo");
                        String  detailId = MapUtils.getString(map, "detailId");
                        String  wuName   = MapUtils.getString(map, "wuName");
                        Integer wuId     = 0;
                        //订单信息
                        OrderModel orderModel = new OrderModel();
                        orderModel.setStore_id(vo.getStoreId());
                        orderModel.setsNo(orderNo);
                        orderModel = orderModelMapper.selectOne(orderModel);
                        String wuNo = MapUtils.getString(map, "wuNo");
                        //发货
                        try
                        {
                            if (orderModel == null)
                            {
                                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DDBCZ, "订单不存在");
                            }
                            if (orderModel.getSelf_lifting() == 2)
                            {
                                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_PSDDBKSYPLFH,"配送订单不可使用批量发货");
                            }

                            if (orderModel.getStatus() == DictionaryConst.OrdersStatus.ORDERS_R_STATUS_UNPAID)
                            {
                                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DDYFHBNJXXG, "订单未付款,不能修改");
                            }
                            if (orderModel.getStatus() == DictionaryConst.OrdersStatus.ORDERS_R_STATUS_DISPATCHED)
                            {
                                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DDYFHBNJXXG, "订单已发货,不能修改");
                            }
                            String mchId = org.apache.commons.lang3.StringUtils.strip(orderModel.getMch_id(), SplitUtils.DH);
                            if (!mchId.equals(user.getMchId().toString()))
                            {
                                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERRCODE_ZNFHZJDPDSP, "只能发货自己店铺的商品");
                            }
                            ExpressModel expressModel = new ExpressModel();
                            expressModel.setKuaidi_name(wuName);
                            expressModel.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS);
                            expressModel = expressModelMapper.selectOne(expressModel);
                            if (expressModel != null)
                            {
                                wuId = expressModel.getId();
                            }
                            else
                            {
                                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QXZKDGS, "快递公司不存在");
                            }
                            if (StringUtils.isEmpty(detailId))
                            {
                                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DDMXBCZ,"订单明细id不存在");
                            }
                            orderDetailsModel = orderDetailsModelMapper.selectByPrimaryKey(detailId);
                            if (orderDetailsModel == null)
                            {
                                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DDBCZ, "订单不存在");
                            }
                            if (orderDetailsModel.getR_status().equals(DictionaryConst.OrdersStatus.ORDERS_R_STATUS_DISPATCHED))
                            {
                                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DDYFHBNJXXG, "订单已发货,不能修改");
                            }
                            detailIdList = new ArrayList<>();
                            detailMap = new HashMap<>();
                            detailMap.put("detailId", orderDetailsModel.getId());
                            int DeliverNum = 0;
                            if (orderDetailsModel.getDeliverNum() != null)
                            {
                                DeliverNum = orderDetailsModel.getDeliverNum();
                            }
                            detailMap.put("num", orderDetailsModel.getNum() - DeliverNum);
                            detailIdList.add(detailMap);
                            //判断快递公司是否配置了电子面单 -配置了就进行电子面单发货
                            //获取店铺的对应物流电子面单配置
                            ExpressSubtableModel expressSubtableModel = new ExpressSubtableModel();
                            expressSubtableModel.setStoreId(vo.getStoreId());
                            expressSubtableModel.setMchId(Integer.parseInt(mchId));
                            expressSubtableModel.setExpressId(wuId);
                            expressSubtableModel.setRecovery(DictionaryConst.WhetherMaven.WHETHER_NO);
                            expressSubtableModel = expressSubtableModelMapper.selectOne(expressSubtableModel);
                            if (expressSubtableModel != null && !org.apache.commons.lang3.StringUtils.isEmpty(expressSubtableModel.getPartnerId()))
                            {
                                FaceSheetSend(vo, wuId, JSON.toJSONString(detailIdList));
                            }
                            else
                            {
                                deliver(vo, orderNo, wuId, wuNo, JSON.toJSONString(detailIdList));
                            }
                        }
                        catch (LaiKeAPIException l)
                        {
                            logger.error("批量发货失败 异常", l);
                            //回滚
//                            TransactionAspectSupport.currentTransactionStatus().rollbackToSavepoint(savePoint);
                            errorText.append(String.format("第%s行 %s", x, l.getMessage())).append(SplitUtils.DH);
                        }
                    }
                }

                //记录
                FileDeliveryModel fileDeliveryModel = new FileDeliveryModel();
                fileDeliveryModel.setId(BuilderIDTool.getSnowflakeId() + "");
                fileDeliveryModel.setName(fileName);
                fileDeliveryModel.setAdd_date(new Date());
                fileDeliveryModel.setType(1);
                int status = 1;
                if (StringUtils.isNotEmpty(errorText))
                {
                    status = 0;
                    fileDeliveryModel.setText(errorText.deleteCharAt(errorText.length() - 1).toString());
                }
                fileDeliveryModel.setStatus(status);
                fileDeliveryModel.setMch_id(user.getMchId());
                fileDeliveryModelMapper.insertSelective(fileDeliveryModel);
                return status == 1;
            }
            publiceService.addAdminRecord(vo.getStoreId(), "进行了订单批量发货操作", AdminRecordModel.Type.UPDATE, vo.getAccessId());
            return false;
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("订单发货列表 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "batchDelivery");
        }
    }


    @Override
    public Map<String, Object> deliveryList(MainVo vo, String fileName, Integer status, String startDate, String endDate, HttpServletResponse response) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User                user     = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);
            Map<String, Object> paramMap = new HashMap<>(16);
            paramMap.put("mchId", user.getMchId());
            paramMap.put("name", fileName);
            paramMap.put("status", status);
            paramMap.put("startDate", startDate);
            paramMap.put("endDate", endDate);
            paramMap.put("add_date_sort", DataUtils.Sort.DESC.toString());
            paramMap.put("pageStart", vo.getPageNo());
            paramMap.put("pageEnd", vo.getPageSize());
            paramMap.put("type", "1");
            int                       total      = fileDeliveryModelMapper.countDynamic(paramMap);
            List<Map<String, Object>> resultList = new ArrayList<>();
            if (total > 0)
            {
                resultList = fileDeliveryModelMapper.selectDynamic(paramMap);
            }
            resultList.stream().forEach(stringObjectMap ->
            {
                if ((int) stringObjectMap.get("status") == 0)
                {
                    stringObjectMap.put("statusName", "发货失败");
                }
                else
                {
                    stringObjectMap.put("statusName", "发货成功");
                }
            });
            if (vo.getExportType() == 1)
            {
                exportBulkDeliveryData(resultList, response);
                return null;
            }
            resultMap.put("total", total);
            resultMap.put("list", resultList);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("发货列表 异常 ", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "bannerList");
        }
        return resultMap;
    }

    //导出批量发货记录
    private void exportBulkDeliveryData(List<Map<String, Object>> list, HttpServletResponse response) throws LaiKeAPIException
    {
        try
        {
            //表头
            String[] headerList = new String[]{"文件ID", "文件名称", "文件状态", "批量发货时间", "失败原因"};
            //对应字段
            String[]     kayList = new String[]{"id", "name", "statusName", "add_date", "text"};
            ExcelParamVo vo      = new ExcelParamVo();
            vo.setTitle("批量发货记录表");
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
            logger.error("导出订单列表数据 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "exportOrderData");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delDelivery(MainVo vo, String id) throws LaiKeAPIException
    {
        try
        {
            User              user              = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);
            FileDeliveryModel fileDeliveryModel = fileDeliveryModelMapper.selectByPrimaryKey(id);
            if (fileDeliveryModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_JLBCZ, "记录不存在");
            }
            int row = fileDeliveryModelMapper.deleteByPrimaryKey(id);
            if (row < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SCSB, "删除失败");
            }
            publiceService.addAdminRecord(vo.getStoreId(), user.getUser_id(), "删除ID为" + id + "发货记录", AdminRecordModel.Type.DEL, AdminRecordModel.Source.PC_SHOP, user.getMchId());
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("删除发货记录 异常 ", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "bannerList");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Map<String, Object> deliver(MainVo vo, String sNo, Integer expressId, String courierNum, String orderListId) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);
            //装载参数
            FrontDeliveryVo sendVo = new FrontDeliveryVo();
            sendVo.setStoreId(vo.getStoreId());
            sendVo.setStoreType(vo.getStoreType());
            sendVo.setUserId(user.getUser_id());
            sendVo.setWxid(user.getWx_id());
            sendVo.setExpressId(expressId);
            sendVo.setsNo(sNo);
            sendVo.setOrderDetailsId(orderListId);
            sendVo.setCourierNum(courierNum);
            //发货
            publicOrderService.frontDelivery(sendVo);

            OrderModel orderModel = new OrderModel();
            orderModel.setStore_id(vo.getStoreId());
            orderModel.setsNo(sNo);
            orderModel = orderModelMapper.selectOne(orderModel);
            //操作日志
            publiceService.addAdminRecord(vo.getStoreId(), "将订单ID：" + orderModel.getsNo() + " 进行了发货", AdminRecordModel.Type.UPDATE, vo.getAccessId());
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("订单发货 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "deliver");
        }
        return resultMap;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Map<String, Object> selfSend(MainVo vo, String phone, String courier_name, String sNo, String orderListId) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            //验证店铺
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);
            if (Strings.isNullOrEmpty(courier_name))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERRCODE_PSYXMBNWK, "配送员姓名不能为空");
            }
            else if (Strings.isNullOrEmpty(phone))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SJHBNWK, "手机不能为空");
            }
          /*  else if (!MobileUtils.isMobile(phone))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SJHGSBZQ, "手机格式不正确");
            }*/
            //装载参数
            FrontDeliveryVo sendVo = new FrontDeliveryVo();
            sendVo.setStoreId(vo.getStoreId());
            sendVo.setStoreType(vo.getStoreType());
            sendVo.setUserId(user.getUser_id());
            sendVo.setWxid(user.getWx_id());
            sendVo.setsNo(sNo);
            sendVo.setPhone(phone);
            sendVo.setCourier_name(courier_name);
            //发货
            publicOrderService.selfSend(sendVo);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("发货 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "send");
        }
        return resultMap;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deliverySave(MainVo vo, String orderDetailIds) throws LaiKeAPIException
    {
        try
        {
            ShipDataVo shippingData = JSON.parseObject(orderDetailIds, ShipDataVo.class);

            //1普通发货 2电子面单 3商家配送
            if (shippingData.getType() == 2)
            {
                FaceSheetSend(vo, shippingData.getExpressId(), JSON.toJSONString(shippingData.getOrderList()));
            }
            else if (shippingData.getType() == 3)
            {
                selfSend(vo, shippingData.getPsyInfo().getTel(), shippingData.getPsyInfo().getName(), orderDetailsModelMapper.selectByPrimaryKey(shippingData.getOrderList().get(0).getDetailId()).getR_sNo(), "");
            }
            else
            {
                // 请选择快递公司
                if (shippingData.getExpressId() == null)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QXZKDGS, "请选择快递公司");
                }
                if (shippingData.getCourierNumber() == null)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QSRKDDH, "请输入快递单号");
                }
                int count = orderDetailsModelMapper.getDeliverNumByExIdAndExNo(shippingData.getExpressId().toString(), shippingData.getCourierNumber());
                if (count > 0)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_KDDHYCZ, "快递单号已存在");
                }
                deliver(vo, orderDetailsModelMapper.selectByPrimaryKey(shippingData.getOrderList().get(0).getDetailId()).getR_sNo(), shippingData.getExpressId(), shippingData.getCourierNumber(), JSON.toJSONString(shippingData.getOrderList()));
            }


        }
        catch (LaiKeAPIException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            logger.error("订单发货 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "orderPrint");
        }

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> quickRefund(RefundVo vo)
    {
       return publicRefundService.adminQuickRefund(vo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void offlineReview(MainVo vo,String sNo, String reasonForRejection, Integer reviewStatus) throws LaiKeAPIException
    {
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);

            OrderModel orderModel = new OrderModel();
            orderModel.setsNo(sNo);
            orderModel = orderModelMapper.selectOne(orderModel);
            if (Objects.isNull(orderModel))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DDBCZ, "订单不存在");
            }
            if (!Objects.equals(orderModel.getPay(),"offline_payment"))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "该订单不属于线下支付订单");
            }
            if (StringUtils.isNotEmpty(reasonForRejection))
            {
                orderModel.setReason_for_rejection(reasonForRejection);
            }
            orderModel.setReview_status(reviewStatus);

            //审核通过，调用订单回调，走原有订单逻辑
            if (reviewStatus == 2)
            {
                publicOrderService.payBackUpOrder(orderModel);
            }

            orderModelMapper.updateByPrimaryKey(orderModel);

            //操作日志
            publiceService.addAdminRecord(vo.getStoreId(), "将订单：" + orderModel.getsNo() + " 进行中凭证审核", AdminRecordModel.Type.UPDATE, vo.getAccessId());
        }
        catch (LaiKeAPIException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            logger.error("凭证审核 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "offlineReview");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Map<String, Object> orderDetails(MainVo vo, String sNo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);
            resultMap = publicOrderService.storeOrderDetails(vo.getStoreId(), sNo);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取订单明细 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "orderDetails");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> editeOrderInfo(OrderModifyVo orderVo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User user = RedisDataTool.getRedisUserCache(orderVo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);
            // 订单id sno
            String             sNo                = orderVo.getsNo();
            AdminOrderDetailVo adminOrderDetailVo = new AdminOrderDetailVo();
            adminOrderDetailVo.setId(sNo);
            adminOrderDetailVo.setsNo(sNo);
            adminOrderDetailVo.setOrderType("modify");
            adminOrderDetailVo.setType(orderVo.getType());
            adminOrderDetailVo.setStoreId(orderVo.getStoreId());
            adminOrderDetailVo.setMchId(user.getMchId());
            Map<String, Object> adminOrderDetailsMap = publicOrderService.orderPcDetails(adminOrderDetailVo);
            resultMap.putAll(adminOrderDetailsMap);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取订单明细 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "editeOrderInfo");
        }
        return resultMap;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveEditOrder(EditOrderVo orderVo) throws LaiKeAPIException
    {
        try
        {
            User user = RedisDataTool.getRedisUserCache(orderVo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);
            if (StringUtils.isEmpty(orderVo.getOrderNo()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DDBJSB, "订单编辑失败");
            }
            publicOrderService.modifyOrder(orderVo);
            //操作日志
//            publiceService.addAdminRecord(orderVo.getStoreId(), user.getZhanghao(), "编辑" + orderVo.getOrderNo() + "订单信息", AdminRecordModel.Type.UPDATE, AdminRecordModel.Source.PC_SHOP, user.getMchId());
            publiceService.addAdminRecord(orderVo.getStoreId(), "修改了订单ID：" + orderVo.getOrderNo() + " 的信息", AdminRecordModel.Type.UPDATE, orderVo.getAccessId());
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("订单编辑失败", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DDBJSB, "订单编辑失败", "saveEditOrder");
        }
    }

    @Override
    public Map<String, Object> delOrder(MainVo vo, List<String> orderNos) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User user    = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);
            int  storeId = vo.getStoreId();
            for (String orderNo : orderNos)
            {
                // 根据订单id,查询订单列表(订单号)
                OrderModel order = new OrderModel();
                order.setStore_id(storeId);
                order.setsNo(orderNo);
                order = orderModelMapper.selectOne(order);
                if (order == null)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DDBCZ, "订单不存在");
                }
                //店铺删除订单
                int row1 = orderDetailsModelMapper.mchDelOrderDetails(storeId, orderNo);
                int row2 = orderModelMapper.mchDelOrder(storeId, orderNo);
                if (row1 >= 0 && row2 >= 0)
                {
                    //判断用户、商家、平台是否都已经删除订单
                    publicOrderService.allDelOrder(storeId, orderNo);
                }
                else
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SCSB, "删除失败", "delOrder");
                }
/*                if (row1 >= 0 && row2 >= 0) {
                    Map<String, Object> parmaMap = new HashMap<>(16);
                    parmaMap.put("store_id", vo.getStoreId());
                    parmaMap.put("orderno", sNo);
                    parmaMap.put("recycle", DictionaryConst.ProductRecycle.NOT_STATUS);
                    //回滚库存
                    List<Map<String, Object>> orderDetailsModelList = orderDetailsModelMapper.getOrderDetailByGoodsInfo(parmaMap);
                    for (Map<String, Object> detail : orderDetailsModelList) {
                        Integer status = MapUtils.getInteger(detail, "r_status");
                        int id = MapUtils.getIntValue(detail, "id");
                        int attrId = MapUtils.getIntValue(detail, "sid");
                        int goodsId = MapUtils.getIntValue(detail, "goodsId");
                        int num = MapUtils.getIntValue(detail, "num");
                        if (status.equals(DictionaryConst.OrdersStatus.ORDERS_R_STATUS_COMPLETE) || status.equals(DictionaryConst.OrdersStatus.ORDERS_R_STATUS_CLOSE)) {
                            logger.debug("订单明细id{} 状态{} 删除订单不回滚库存", id, status);
                            continue;
                        }
                        //回滚商品库存
                        AddStockVo addStockVo = new AddStockVo();
                        addStockVo.setStoreId(vo.getStoreId());
                        addStockVo.setId(attrId);
                        addStockVo.setPid(goodsId);
                        addStockVo.setAddNum(num);
                        addStockVo.setText("后台删除订单,返还" + num);
                        publicStockService.addGoodsStock(addStockVo, "admin");
                        //扣减商品销量
                        productListModelMapper.updateProductListVolume(-num, vo.getStoreId(), goodsId);
                    }

                }*/
                //操作日志
                publiceService.addAdminRecord(storeId, "删除了订单ID：" + orderNo + " 的信息", AdminRecordModel.Type.DEL, vo.getAccessId());
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("删除订单 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "delOrder");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> searchExpress(MainVo vo, String express) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            Map<String, Object> paramMap = new HashMap<>(16);
            paramMap.put("is_open", DictionaryConst.WhetherMaven.WHETHER_OK);
            paramMap.put("sort_sort", DataUtils.Sort.DESC.toString());
            if (org.apache.commons.lang3.StringUtils.isNotEmpty(express))
            {
                paramMap.put("kuaidi_name", express);
            }

            int                       total            = expressModelMapper.countDynamic(paramMap);
            List<Map<String, Object>> expressModelList = new ArrayList<>();
            if (total > 0)
            {
                expressModelList = expressModelMapper.selectDynamic(paramMap);
            }
            resultMap.put("total", total);
            resultMap.put("list", expressModelList);
            return resultMap;
        }
        catch (LaiKeAPIException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            logger.error("获取快递信息失败：", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "searchExpress");
        }
    }

    @Override
    public Map<String, Object> kuaidishow(MainVo vo, String orderno) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);
            resultMap = publicOrderService.getLogistics(orderno);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取订单物流信息 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "kuaidishow");
        }
        return resultMap;
    }

    @Autowired
    private SignRecordModelMapper signRecordModelMapper;

    @Autowired
    private PublicMemberService publicMemberService;


    @Override
    public Map<String, Object> getGoodsInfoByExtractionCode(MainVo vo, Integer orderId, String extractionCode) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User                user     = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", vo.getStoreId());
            parmaMap.put("mch_id", user.getMchId());
            if (orderId == null)
            {
                if (extractionCode.contains(SplitUtils.DH))
                {
                    parmaMap.put("extraction_code1", extractionCode);
                }
                else
                {
                    parmaMap.put("extraction_code", extractionCode);
                }
            }
            parmaMap.put("id", orderId);
            parmaMap.put("supplierPro", "supplierPro");
            List<Map<String, Object>> orderInfo    = orderModelMapper.getOrdersNumDynamic(parmaMap);
            HashMap<String, Object>   orderInfoMap = new HashMap<>();
            if (orderId == null && (orderInfo == null || orderInfo.size() < 1))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YZMCW, "验证码错误");
            }
            else
            {
                Map<String, Object> map = orderInfo.get(0);
                orderInfoMap.put("sNo", MapUtils.getString(map, "sNo"));
                orderInfoMap.put("name", MapUtils.getString(map, "name"));
                orderInfoMap.put("mobile", MapUtils.getString(map, "mobile"));
                orderInfoMap.put("num", MapUtils.getString(map, "num"));
                orderInfoMap.put("old_total", MapUtils.getString(map, "old_total"));
                //订单状态
                int status = Integer.parseInt(map.get("status").toString());
                if (status == DictionaryConst.OrdersStatus.ORDERS_R_STATUS_DISPATCHED)
                {
                    //取货代码 E9ds5B,1601349348,1601351148
                    String keyCode = MapUtils.getString(map, "extraction_code");
                    if (StringUtils.isEmpty(keyCode))
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QHMBCZ, "取货码不存在");
                    }
                    String[] codeStr = keyCode.split(",");
                    if (codeStr.length < 3 || StringUtils.isEmpty(codeStr[0]) || !StringUtils.isInteger(codeStr[2]))
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YZMWX, "验证码无效");
                    }
                    //验证码
                    String code = codeStr[0];
                    //失效时间
                    long endTime = Long.parseLong(codeStr[2]);
                    //系统时间
                    long sysTime = Long.parseLong(DateUtil.timeStamp());
                    if (sysTime > endTime)
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YZMYSX, "验证码已失效");
                    }
                    if (!code.equals(extractionCode))
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YZMCW, "验证码错误");
                    }
                }
            }

            resultMap.put("orderInfo", orderInfoMap);
            parmaMap.clear();
            parmaMap.put("store_id", vo.getStoreId());
            parmaMap.put("user_id", MapUtils.getString(orderInfo.get(0), "user_id"));
            parmaMap.put("id", orderId);
            List<Map<String, Object>> orderCodesInfo = orderModelMapper.seeExtractionCode(parmaMap);
            List<Map<String, Object>> products       = new ArrayList<>();
            for (int i = 0; i < orderCodesInfo.size(); i++)
            {
                Map<String, Object> productMap = orderCodesInfo.get(i);
                // 商品ID
                int proId = MapUtils.getInteger(productMap, "pid");
                // 商品规格id
                int                 productSid = MapUtils.getInteger(productMap, "sid");
                Map<String, Object> params     = new HashMap<>();
                params.put("store_id", vo.getStoreId());
                params.put("sid", productSid);
                params.put("p_id", proId);
                List<Map<String, Object>> productInfoList = productListModelMapper.getGoodsTitleAndImg(params);
                if (CollectionUtils.isEmpty(productInfoList))
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YWYC, "业务异常", "seeExtractionCode");
                }
                Map<String, Object> productInfoMap = productInfoList.get(0);
                // 商品名称
                String product_title = DataUtils.getStringVal(productInfoMap, "product_title");
                // 拼图片路径
                String              img           = publiceService.getImgPath(DataUtils.getStringVal(productInfoMap, "img"), vo.getStoreId());
                Map<String, Object> productRetMap = new HashMap<>();
                productRetMap.put("p_id", proId);
                productRetMap.put("product_title", product_title);
                productRetMap.put("p_price", DataUtils.getBigDecimalVal(productMap, "p_price", BigDecimal.ZERO));
                productRetMap.put("num", MapUtils.getInteger(productMap, "num"));
                productRetMap.put("sid", MapUtils.getInteger(productMap, "sid"));
                productRetMap.put("size", DataUtils.getStringVal(productMap, "size"));
                productRetMap.put("img", img);
                products.add(productRetMap);
            }
            resultMap.put("por_list", products);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取订单物流信息 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "kuaidishow");
        }
        return resultMap;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Map<String, Object> verificationExtractionCode(MainVo vo, Integer orderId, String extractionCode) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User                user     = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", vo.getStoreId());
            parmaMap.put("mch_id", user.getMchId());
            if (orderId == null)
            {
                if (extractionCode.contains(SplitUtils.DH))
                {
                    parmaMap.put("extraction_code1", extractionCode);
                }
                else
                {
                    parmaMap.put("extraction_code", extractionCode);
                }
            }
            parmaMap.put("id", orderId);
            parmaMap.put("supplierPro", "supplierPro");
            List<Map<String, Object>> orderInfo = orderModelMapper.getOrdersNumDynamic(parmaMap);
            if (orderId == null && (orderInfo == null || orderInfo.size() < 1))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YZMCW, "验证码错误");
            }
            if (orderInfo != null && orderInfo.size() > 0)
            {
                Map<String, Object> map = orderInfo.get(0);
                //订单号
                String orderno = map.get("sNo").toString();
                //订单状态
                int status = Integer.parseInt(map.get("status").toString());
                //订单金额
                BigDecimal orderAmt = new BigDecimal(map.get("z_price").toString());
                //积分
                BigDecimal allow = new BigDecimal("0");
                if (map.containsKey("allow"))
                {
                    allow = new BigDecimal(map.get("allow").toString());
                }
                if (status == DictionaryConst.OrdersStatus.ORDERS_R_STATUS_DISPATCHED)
                {
                    //取货代码 E9ds5B,1601349348,1601351148
                    String keyCode = MapUtils.getString(map, "extraction_code");
                    if (StringUtils.isEmpty(keyCode))
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QHMBCZ, "取货码不存在");
                    }
                    String[] codeStr = keyCode.split(",");
                    if (codeStr.length < 3 || StringUtils.isEmpty(codeStr[0]) || !StringUtils.isInteger(codeStr[2]))
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YZMWX, "验证码无效");
                    }
                    //验证码
                    String code = codeStr[0];
                    //失效时间
                    long endTime = Long.parseLong(codeStr[2]);
                    //系统时间
                    long sysTime = Long.parseLong(DateUtil.timeStamp());
                    if (sysTime > endTime)
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YZMYSX, "验证码已失效");
                    }
                    if (!code.equals(extractionCode))
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YZMCW, "验证码错误");
                    }
                    //标记订单已完成
                    parmaMap.clear();
                    parmaMap.put("orderno", orderno);
                    parmaMap.put("status", DictionaryConst.OrdersStatus.ORDERS_R_STATUS_COMPLETE);
                    //店主核销
                    parmaMap.put("VerifiedBy_type", 1);
                    int count = orderModelMapper.updateByOrdernoDynamic(parmaMap);
                    if (count < 1)
                    {
                        logger.info("订单标记已完成失败 参数:" + JSON.toJSONString(parmaMap));
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常");
                    }
                    //标记明细状态
                    parmaMap.clear();
                    parmaMap.put("orderno", orderno);
                    parmaMap.put("arrive_time", new Date());
                    parmaMap.put("r_status", DictionaryConst.OrdersStatus.ORDERS_R_STATUS_COMPLETE);
                    count = orderDetailsModelMapper.updateByOrdernoDynamic(parmaMap);
                    if (count < 1)
                    {
                        logger.info("订单标记已完成失败 参数:" + JSON.toJSONString(parmaMap));
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常");
                    }
                    //卖家收入
                    publicMchService.clientConfirmReceipt(vo.getStoreId(), user.getMchId(), orderno, orderAmt, allow);
                    //确认收货 普通商品赠送积分(只有普通订单可以自提，if判断可以不用)
                    if (DictionaryConst.OrdersType.ORDERS_HEADER_GM.equals(MapUtils.getString(map, "otype")) ||
                            DictionaryConst.OrdersType.ORDERS_HEADER_VI.equals(MapUtils.getString(map, "otype")))
                    {
                        //如果订单之前已经获得了一笔积分则不再获取
                        SignRecordModel signRecordCount = new SignRecordModel();
                        signRecordCount.setStore_id(vo.getStoreId());
                        signRecordCount.setUser_id(MapUtils.getString(map, "user_id"));
                        signRecordCount.setsNo(orderno);
                        if (signRecordModelMapper.selectCount(signRecordCount) == 0)
                        {
                            publicMemberService.memberSettlement(vo.getStoreId(), MapUtils.getString(map, "user_id"),
                                    orderno, new BigDecimal(MapUtils.getString(map, "z_price", "0")), IntegralConfigModel.GiveStatus.RECEIVING);
                        }
                        else
                        {
                            logger.debug("订单{}已经获得过一笔【会员购物积分】积分,此次不获嘚积分.", orderno);
                        }
                    }
                    resultMap.put("sNo", orderno);
                    resultMap.put("p_price", orderAmt);

                    publiceService.addAdminRecord(vo.getStoreId(), "核销了订单ID：" + orderno, AdminRecordModel.Type.UPDATE, vo.getAccessId());
                }
                else
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YZMWX, "验证码无效");
                }
            }
            else
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DDBCZ, "订单不存在");
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("验证提货码 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "verificationExtractionCode");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> refundPageById(MainVo vo, Integer id, String orderNo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);
            if (id == null)
            {
                if (StringUtils.isEmpty(orderNo))
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DDBCZ, "订单不存在");
                }
                else
                {
                    ReturnOrderModel returnOrderOld = returnOrderModelMapper.getReturnNewInfoBySno(orderNo);
                    if (returnOrderOld == null)
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SHXXBCZ, "售后信息不存在");
                    }
                    id = returnOrderOld.getId();
                }
            }
            resultMap = publicRefundService.refundPageById(vo.getStoreId(), id);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("售后订单详情 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "refundPageById");
        }
        return resultMap;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void examine(RefundVo vo) throws LaiKeAPIException
    {
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);
            vo.setMchId(user.getMchId());
            ReturnOrderModel returnOrderModel = returnOrderModelMapper.selectByPrimaryKey(vo.getId());
            if (!publicRefundService.refund(vo))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SHSB, "售后失败");
            }
            if (vo.getType().equals(DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_AGREE_REBACK)
                    || vo.getType().equals(DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_RECEIVED_REBAKGOODS)
                    || vo.getType().equals(DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_AGREE_REBACK_AMT)
                    || vo.getType().equals(DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_AGREE_REBACK_GOODS))
            {
                publiceService.addAdminRecord(vo.getStoreId(), "通过了订单ID：" + returnOrderModel.getsNo() + " 的售后申请", AdminRecordModel.Type.PASS_OR_REFUSE, vo.getAccessId());
            }
            else
            {
                publiceService.addAdminRecord(vo.getStoreId(), "拒绝了订单ID：" + returnOrderModel.getsNo() + " 的售后申请", AdminRecordModel.Type.PASS_OR_REFUSE, vo.getAccessId());
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("售后审核 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "examine");
        }
    }

    @Override
    public Map<String, Object> getCommentsInfo(CommentsInfoVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);
            if (vo.getCid() != null && vo.getCid() > 0)
            {
                return getDetail(vo, vo.getCid());
            }
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", vo.getStoreId());
            parmaMap.put("status", 1);
            parmaMap.put("type", vo.getType());
            parmaMap.put("mchId", user.getMchId());
            if (!StringUtils.isEmpty(vo.getStartDate()))
            {
                parmaMap.put("startDate", vo.getStartDate());
                if (!StringUtils.isEmpty(vo.getEndDate()))
                {
                    parmaMap.put("endDate", vo.getEndDate());
                }
            }
            //订单类型
            if (StringUtils.isEmpty(vo.getOrderType()))
            {
                List<String> orderTypeList = new ArrayList<>();
                orderTypeList.add(DictionaryConst.OrdersType.ORDERS_HEADER_GM);
                parmaMap.put("orderTypeList", orderTypeList);
            }
            else
            {
                parmaMap.put("orderType", vo.getOrderType());
            }
            if (StringUtils.isNotEmpty(vo.getMchName()))
            {
                parmaMap.put("mchName", vo.getMchName());
            }
            parmaMap.put("keyword", vo.getOrderno());
            int count = commentsModelMapper.countCommentsOrderDynamic(parmaMap);
            parmaMap.put("pageStart", vo.getPageNo());
            parmaMap.put("pageEnd", vo.getPageSize());
            List<Map<String, Object>> commentsList = commentsModelMapper.getCommentsOrderDynamic(parmaMap);
            for (Map<String, Object> map : commentsList)
            {
                int          commentId    = MapUtils.getIntValue(map, "id");
                Integer      mchId        = MapUtils.getInteger(map, "mch_id");
                List<String> imgUrls      = commentsImgModelMapper.getCommentsImages(commentId);
                List<String> resultImgUrl = new ArrayList<>();
                for (String img : imgUrls)
                {
                    resultImgUrl.add(publiceService.getImgPath(img, vo.getStoreId()));
                }
                map.put("replyText", replyCommentsModelMapper.getMchReplyInfo(commentId, mchId));
                map.put("commentImgList", resultImgUrl);
                //自己才能回复自己的店铺的评论
                map.put("isMain", mchId.equals(user.getMchId()));
            }

            resultMap.put("total", count);
            resultMap.put("list", commentsList);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取评论列表 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常");
        }
        return resultMap;
    }


    @Override
    public Map<String, Object> getCommentsDetailInfoById(CommentsDetailInfoVo vo, int cid) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User                user     = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", vo.getStoreId());
            parmaMap.put("commentId", cid);
            if (StringUtils.isNotEmpty(vo.getKey()))
            {
                parmaMap.put("key", vo.getKey());
            }
            parmaMap.put("startDate", vo.getStartDate());
            parmaMap.put("endDate", vo.getEndDate());
            parmaMap.put("sid_null", "sid_null");
            parmaMap.put("type", ReplyCommentsModel.Type.USER);
            parmaMap.put("add_time_sort", DataUtils.Sort.DESC.toString());
            parmaMap.put("pageStart", vo.getPageNo());
            parmaMap.put("pageEnd", vo.getPageSize());

            resultMap.putAll(publiceService.getCommentsDetailInfoById(parmaMap));
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取评论详细信息 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常");
        }
        return resultMap;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCommentsDetailInfoById(UpdateCommentsInfoVo vo) throws LaiKeAPIException
    {
        try
        {
            if (vo.getCommentText().trim().isEmpty())
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "请输入修改评论内容");
            }
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);
            //获取评论信息
            CommentsModel commentsModel = new CommentsModel();
            commentsModel.setStore_id(vo.getStoreId());
            commentsModel.setId(vo.getCid());
            commentsModel = commentsModelMapper.selectOne(commentsModel);
            if (commentsModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_PLBCZ, "评论不存在");
            }
            //删除之前评论图片
            CommentsImgModel commentsImgModel = new CommentsImgModel();
            commentsImgModel.setComments_id(commentsModel.getId());
            commentsImgModelMapper.delete(commentsImgModel);
            //保存修改后的图片
            int count;
            if (StringUtils.isNotEmpty(vo.getCommentImgUrls()))
            {
                for (String img : vo.getCommentImgUrls())
                {
                    img = ImgUploadUtils.getUrlImgByName(img, true);
                    CommentsImgModel saveCommentsImgModel = new CommentsImgModel();
                    saveCommentsImgModel.setComments_id(commentsModel.getId());
                    saveCommentsImgModel.setType(0);
                    saveCommentsImgModel.setComments_url(img);
                    saveCommentsImgModel.setAdd_time(new Date());
                    count = commentsImgModelMapper.insertSelective(saveCommentsImgModel);
                    if (count < 1)
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_XGSB, "修改失败");
                    }
                }
            }
            //追评图
            if (StringUtils.isNotEmpty(vo.getReviewImgList()))
            {
                for (String img : vo.getReviewImgList())
                {
                    img = ImgUploadUtils.getUrlImgByName(img, true);
                    CommentsImgModel saveCommentsImgModel = new CommentsImgModel();
                    saveCommentsImgModel.setComments_id(commentsModel.getId());
                    saveCommentsImgModel.setType(1);
                    saveCommentsImgModel.setComments_url(img);
                    saveCommentsImgModel.setAdd_time(new Date());
                    count = commentsImgModelMapper.insertSelective(saveCommentsImgModel);
                    if (count < 1)
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_XGSB, "修改失败");
                    }
                }
            }

            //修改评论
            CommentsModel updateComment = new CommentsModel();
            updateComment.setId(commentsModel.getId());
            updateComment.setContent(vo.getCommentText());
            if (!StringUtils.isEmpty(vo.getReview()))
            {
                updateComment.setReview_time(new Date());
                updateComment.setReview(vo.getReview());
            }
            updateComment.setCommentType(String.valueOf(vo.getCommentType()));

            count = commentsModelMapper.updateByPrimaryKeySelective(updateComment);
            if (count < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_XGSB, "修改失败");
            }
            OrderDetailsModel orderDetailsModel = orderDetailsModelMapper.selectByPrimaryKey(commentsModel.getOrder_detail_id());
            OrderModel        orderModel        = new OrderModel();
            orderModel.setsNo(orderDetailsModel.getR_sNo());
            orderModel = orderModelMapper.selectOne(orderModel);

            publiceService.addAdminRecord(vo.getStoreId(), "修改了订单ID：" + orderModel.getsNo() + " 的评论信息", AdminRecordModel.Type.UPDATE, vo.getAccessId());
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("修改评论 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常");
        }
    }


    @Override
    public Map<String, Object> getCommentReplyList(GetCommentsDetailInfoVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new Hashtable<>(16);
        try
        {
            Map<String, Object> parmaMap = new Hashtable<>(16);
            parmaMap.put("store_id", vo.getStoreId());
            parmaMap.put("commentId", vo.getCid());
            parmaMap.put("type", ReplyCommentsModel.Type.USER);
            parmaMap.put("sid_null", "sid_null");
            parmaMap.put("add_time_sort", DataUtils.Sort.DESC.toString());
            if (StringUtils.isNotEmpty(vo.getKey()))
            {
                parmaMap.put("key", vo.getKey());
            }
            if (vo.getStartDate() != null)
            {
                parmaMap.put("startDate", vo.getStartDate());
            }
            if (vo.getEndDate() != null)
            {
                parmaMap.put("endDate", vo.getEndDate());
            }
            //明细不需要展示店铺的回复
            parmaMap.put("notMch", "notMch");

            parmaMap.put("pageStart", vo.getPageNo());
            parmaMap.put("pageEnd", vo.getPageSize());

            int                       total = replyCommentsModelMapper.countDynamic(parmaMap);
            List<Map<String, Object>> list  = new ArrayList<>();
            if (total > 0)
            {
                list = replyCommentsModelMapper.selectDynamic(parmaMap);
                for (Map<String, Object> map : list)
                {
                    map.put("addTime", DateUtil.dateFormate(MapUtils.getString(map, "add_time"), GloabConst.TimePattern.YMD));
                }
            }

            resultMap.put("total", total);
            resultMap.put("list", list);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取评论详情回复列表 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getCommentReplyList");
        }
        return resultMap;
    }

    @Override
    public void delCommentReply(MainVo vo, int id) throws LaiKeAPIException
    {
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);
            publiceService.delCommentsDetailInfoById(vo, id);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取评论详情回复列表 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "delCommentReply");
        }
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean replyComments(MainVo vo, int commentId, String commentText) throws LaiKeAPIException
    {
        try
        {
            if (commentText.trim().isEmpty())
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "请输入回复内容");
            }
            User               user               = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);
            ReplyCommentsModel replyCommentsModel = new ReplyCommentsModel();
            replyCommentsModel.setStore_id(vo.getStoreId());
            replyCommentsModel.setCid(String.valueOf(commentId));
            replyCommentsModel.setType(ReplyCommentsModel.Type.MCH);
            replyCommentsModel.setUid(user.getUser_id());
            int count = replyCommentsModelMapper.selectCount(replyCommentsModel);
            if (count > 0)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YJHFGL, "已经回复过了");
            }

            replyCommentsModel.setUid(user.getUser_id());
            replyCommentsModel.setContent(commentText);
            replyCommentsModel.setAdd_time(new Date());
            count = replyCommentsModelMapper.insertSelective(replyCommentsModel);

            CommentsModel     commentsModel     = commentsModelMapper.selectByPrimaryKey(commentId);
            OrderDetailsModel orderDetailsModel = orderDetailsModelMapper.selectByPrimaryKey(commentsModel.getOrder_detail_id());
            OrderModel        orderModel        = new OrderModel();
            orderModel.setsNo(orderDetailsModel.getR_sNo());
            orderModel = orderModelMapper.selectOne(orderModel);

            publiceService.addAdminRecord(vo.getStoreId(), "回复了订单ID：" + orderModel.getsNo() + " 的评论信息", AdminRecordModel.Type.UPDATE, vo.getAccessId());
            if (count < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_HFSB, "回复失败");
            }
            return true;
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error(e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean delComments(MainVo vo, int commentId) throws LaiKeAPIException
    {
        try
        {
            User          user          = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);
            CommentsModel commentsModel = commentsModelMapper.selectByPrimaryKey(commentId);
            if (commentsModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_PLBCZ, "评论不存在");
            }
            //删除评论
            int count = commentsModelMapper.deleteByPrimaryKey(commentId);
            if (count < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SCSB, "删除失败");
            }
            //删除评论图片
            CommentsImgModel commentsImgModel = new CommentsImgModel();
            commentsImgModel.setComments_id(commentId);
            commentsImgModelMapper.delete(commentsImgModel);
            //删除回复
            ReplyCommentsModel replyCommentsModel = new ReplyCommentsModel();
            replyCommentsModel.setCid(String.valueOf(commentId));
            replyCommentsModelMapper.delete(replyCommentsModel);

            //获取订单ID
            OrderDetailsModel orderDetailsModel = orderDetailsModelMapper.selectByPrimaryKey(commentsModel.getOrder_detail_id());
            OrderModel        orderModel        = new OrderModel();
            orderModel.setsNo(orderDetailsModel.getR_sNo());
            orderModel = orderModelMapper.selectOne(orderModel);

            publiceService.addAdminRecord(vo.getStoreId(), "删除了订单ID：" + orderModel.getsNo() + " 的评论信息", AdminRecordModel.Type.DEL, vo.getAccessId());
            return true;
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("删除评论", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常");
        }
    }

    @Override
    public Map<String, Object> ShippingRecords(GetExpressDeliveryListVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);
            vo.setMch_id(user.getMchId());
            resultMap = publicExpressService.getExpressDeliveryList(vo);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("验证提货码 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "获取订单发货列表", "ShippingRecords");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> expressGetPro(MainVo vo, Integer id, String name) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);
            resultMap = publicExpressService.getGoodsByExpressDeliveryId(vo, id, name);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("查看发货记录商品 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "expressGetPro");
        }
        return resultMap;
    }


    @Override
    public String CancelElectronicWaybill(MainVo vo, Integer id) throws LaiKeAPIException
    {
        String msg;
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);
            msg = publicOrderService.cancelElectronicSheetDelivery(vo, id);
            ExpressDeliveryModel expressDeliveryModel = expressDeliveryModelMapper.selectByPrimaryKey(id);
            publiceService.addAdminRecord(vo.getStoreId(), "取消了订单ID：" + expressDeliveryModel.getsNo() + " 的面单打印", AdminRecordModel.Type.DEL_ORDER, vo.getAccessId());
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("查看发货记录商品 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "expressGetPro");
        }
        return msg;
    }

    @Override
    public void FaceSheetSend(MainVo vo, Integer exId, String orderDetailIds) throws LaiKeAPIException
    {
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);
            publicOrderService.electronicSheetDelivery(vo, exId, orderDetailIds);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("查看发货记录商品 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "expressGetPro");
        }
    }

    /**
     * 导出售后列表
     *
     * @param orderList -
     * @param response  -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/7/29 10:22
     */
    private void exportReturnOrderData(List<Map<String, Object>> orderList, HttpServletResponse response) throws LaiKeAPIException
    {
        try
        {
            //表头
            String[] headerList = new String[]{"用户id", "商品名称", "商品价格", "数量", "订单号", "实退金额", "申请时间", "类型", "状态"};
            //对应字段
            String[]     kayList = new String[]{"user_id", "p_name", "p_price", "num", "sNo", "real_money", "re_time", "returnTypeName", "returnName"};
            ExcelParamVo vo      = new ExcelParamVo();
            vo.setTitle("售后列表");
            vo.setHeaderList(headerList);
            vo.setValueList(kayList);
            vo.setList(orderList);
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
            logger.error("导出售后列表 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "exportOrderData");
        }
    }

    private Map<String, Object> getDetail(MainVo vo, int cid)
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", vo.getStoreId());
            parmaMap.put("commentId", cid);
            //类型(0=全部,1=好评,2=中评,3=差评,4=有图)
            parmaMap.put("type", 0);
            List<Map<String, Object>> commentsMap = publiceService.getGoodsCommentList(parmaMap);

            resultMap.put("list", commentsMap);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取评论详情 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常");
        }
        return resultMap;
    }


    @Autowired
    private MchDistributionRecordModelMapper mchDistributionRecordModelMapper;

    @Autowired
    private PaymentConfigModelMapper paymentConfigModelMapper;

    @Override
    public Map<String, Object> divideRecord(MainVo vo, Integer mchId, String condition, String startDate, String endDate, HttpServletResponse response) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            //查询服务商id
            //查询服务商id
            String mini_wechat = paymentConfigModelMapper.getPaymentConfigInfo(vo.getStoreId(), "mini_wechat");
            if (StringUtils.isEmpty(mini_wechat))
            {
                resultMap.put("total", 0);
                List<Map<String, Object>> list = new ArrayList<>();
                resultMap.put("list", list);
                if (vo.getExportType().equals(1))
                {
                    exportRecordReportData(list, response);
                    return null;
                }
                return resultMap;
            }
            String     paymentJson  = URLDecoder.decode(mini_wechat, GloabConst.Chartset.UTF_8);
            JSONObject payJson      = JSONObject.parseObject(paymentJson);
            String     serviceMchId = payJson.getString("mch_id");
            logger.info("mchID:{}", mchId);
            if (mchId != null)
            {
                User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);
            }
            else
            {
                AdminModel user = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            }
            Map<String, Object> paramMap = new HashMap<>(16);
            paramMap.put("storeId", vo.getStoreId());
            paramMap.put("add_date_sort", DataUtils.Sort.ASC.toString());
            paramMap.put("pageStart", vo.getPageNo());
            paramMap.put("pageEnd", vo.getPageSize());
            if (!Objects.isNull(mchId))
            {
                paramMap.put("mchId", mchId);
                paramMap.put("serviceMchId", serviceMchId);
            }
            if (StringUtils.isNotEmpty(condition))
            {
                paramMap.put("condition", condition);
            }
            if (StringUtils.isNotEmpty(startDate))
            {
                paramMap.put("startDate", startDate);
            }
            if (StringUtils.isNotEmpty(endDate))
            {
                paramMap.put("endDate", endDate);
            }
            List<Map<String, Object>> list = new ArrayList<>();
            int                       i    = mchDistributionRecordModelMapper.countDynamic(paramMap);
            if (i > 0)
            {
                list = mchDistributionRecordModelMapper.selectDynamic(paramMap);
                for (Map<String, Object> map : list)
                {
                    map.put("add_date", DateUtil.dateFormate(MapUtils.getString(map, "add_date"), GloabConst.TimePattern.YMDHMS));
                    String     orderNo          = MapUtils.getString(map, "order_no");
                    BigDecimal returnAmtByOrder = returnOrderModelMapper.getReturnAmtByOrder(orderNo);
                    map.put("refund_price", returnAmtByOrder);
                }
            }
            resultMap.put("total", i);
            resultMap.put("list", list);
            if (vo.getExportType().equals(1))
            {
                exportRecordReportData(list, response);
                return null;
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("分账记录 异常 ", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "divideRecord");
        }
        return resultMap;
    }

    private void exportRecordReportData(List<Map<String, Object>> list, HttpServletResponse response) throws LaiKeAPIException
    {
        try
        {
            //表头
            String[] headerList = new String[]{"订单编号", "订单金额", "退单金额", "分账对象", "分账金额", "运费", "店铺名称", "处理时间"};
            //对应字段
            String[]     kayList = new String[]{"order_no", "z_price", "refund_price", "account", "amount", "z_freight", "name", "add_date"};
            ExcelParamVo vo      = new ExcelParamVo();
            vo.setTitle("分账记录报表列表");
            vo.setHeaderList(headerList);
            vo.setValueList(kayList);
            vo.setList(list);
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

    @Autowired
    private PublicExpressService publicExpressService;

    @Autowired
    private ExpressDeliveryModelMapper expressDeliveryModelMapper;
}
