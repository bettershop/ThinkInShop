package com.laiketui.apps.mch.services;

import com.google.common.collect.Maps;
import com.laiketui.apps.api.mch.SupplierMchOrderService;
import com.laiketui.common.api.PublicMchService;
import com.laiketui.common.api.PublicOrderService;
import com.laiketui.common.api.PublicRefundService;
import com.laiketui.common.api.PubliceService;
import com.laiketui.common.mapper.*;
import com.laiketui.common.utils.EasyPoiExcelUtil;
import com.laiketui.common.utils.tool.cache.RedisDataTool;
import com.laiketui.core.cache.RedisUtil;
import com.laiketui.core.common.SplitUtils;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.DictionaryConst;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.utils.tool.DataUtils;
import com.laiketui.core.utils.tool.StringUtils;
import com.laiketui.domain.config.ConfiGureModel;
import com.laiketui.domain.log.AdminRecordModel;
import com.laiketui.domain.message.MessageLoggingModal;
import com.laiketui.domain.order.OrderDetailsModel;
import com.laiketui.domain.order.OrderModel;
import com.laiketui.domain.order.ReturnOrderModel;
import com.laiketui.domain.product.ProductListModel;
import com.laiketui.domain.product.StockModel;
import com.laiketui.domain.user.User;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.Tool.ExcelParamVo;
import com.laiketui.domain.vo.main.RefundVo;
import com.laiketui.domain.vo.mch.MchOrderDetailVo;
import com.laiketui.domain.vo.mch.MchOrderIndexVo;
import com.laiketui.domain.vo.order.AdminOrderDetailVo;
import com.laiketui.domain.vo.order.AdminOrderListVo;
import com.laiketui.domain.vo.order.AdminOrderVo;
import com.laiketui.domain.vo.order.OrderSettlementVo;
import com.laiketui.domain.vo.pc.MchPcReturnOrderVo;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.*;

import static com.laiketui.core.lktconst.DictionaryConst.OrdersStatus.ORDERS_R_STATUS_CLOSE;
import static com.laiketui.domain.order.OrderModel.ORDER_CLOSE;

/**
 * @Author: sunH_
 * @Date: Create in 16:23 2023/2/14
 */
@Service
public class SupplierMchOrderServiceImpl implements SupplierMchOrderService
{

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private PublicOrderService publicOrderService;


    @Autowired
    private ReturnOrderModelMapper returnOrderModelMapper;

    @Autowired
    private PublicMchService publicMchService;

    @Autowired
    private OrderDetailsModelMapper orderDetailsModelMapper;

    @Autowired
    private PubliceService publiceService;

    @Autowired
    private OrderModelMapper orderModelMapper;

    @Autowired
    private PublicRefundService publicRefundService;

    @Autowired
    private ProductListModelMapper productListModelMapper;

    @Autowired
    private ConfiGureModelMapper confiGureModelMapper;
    @Autowired
    private StockModelMapper     stockModelMapper;

    private static final int WAIT_PAY_TYPE = 1;
    private static final int SEND_TYPE     = 2;
    private static final int RETURN_TYPE   = 3;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> orderIndex(MchOrderIndexVo vo, HttpServletResponse response) throws LaiKeAPIException
    {
        Map<String, Object> resultMap;
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            //验证店铺
            publicMchService.verificationMchExis(vo.getStoreId(), user.getUser_id(), vo.getShopId());
            vo.setUserId(user.getUser_id());
            vo.setIsSupplier("isSupplierPro");
            vo.setIsSupplierPro("isSupplierPro");
            resultMap = publicOrderService.aMchOrderIndex(vo);
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

    @Override
    public Map<String, Object> orderDetailsInfo(AdminOrderDetailVo orderVo)
    {
        try
        {
            User user = RedisDataTool.getRedisUserCache(orderVo.getAccessId(), redisUtil, true);
            //验证店铺
            publicMchService.verificationMchExis(orderVo.getStoreId(), user.getUser_id(), user.getMchId());
            MchOrderDetailVo mchOrderDetailVo = new MchOrderDetailVo();
            mchOrderDetailVo.setStoreId(orderVo.getStoreId());
            mchOrderDetailVo.setShopId(orderVo.getMchId());
            mchOrderDetailVo.setsNo(orderVo.getsNo());
            return publicOrderService.mchOrderDetails(mchOrderDetailVo);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("订单详情异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DDBJSB, "订单编辑失败", "orderDetailsInfo");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void close(AdminOrderVo orderVo) throws LaiKeAPIException
    {
        try
        {
            User              user              = RedisDataTool.getRedisUserCache(orderVo.getAccessId(), redisUtil, true);
            int               storeId           = orderVo.getStoreId();
            String            sNo               = orderVo.getOid();
            OrderDetailsModel orderDetailsModel = new OrderDetailsModel();
            orderDetailsModel.setR_status(ORDERS_R_STATUS_CLOSE);
            Map params = Maps.newHashMap();
            params.put("status", ORDER_CLOSE);
            params.put("storeId", storeId);
            params.put("orderno", sNo);
            int row = orderModelMapper.updateOrderInfo(params);
            if (row < 1)
            {
                logger.info(sNo + "修改订单状态失败:");
            }
            int row1 = orderDetailsModelMapper.updateOrderDetailsStatus(storeId, sNo, ORDER_CLOSE);
            logger.info(sNo + "订单关闭:");
            if (row1 < 1)
            {
                logger.info("sNo 修改订单状态失败！");
            }
            orderDetailsModel.setStore_id(storeId);
            orderDetailsModel.setR_sNo(sNo);
            List<OrderDetailsModel> orderDetailsModels = orderDetailsModelMapper.select(orderDetailsModel);
            for (OrderDetailsModel orderDetailsInfo : orderDetailsModels)
            {
                int            pid            = orderDetailsInfo.getP_id();
                int            goodsNum       = orderDetailsInfo.getNum();
                String         attributeId    = orderDetailsInfo.getSid();
                ConfiGureModel confiGureModel = new ConfiGureModel();
                confiGureModel.setId(Integer.valueOf(attributeId));
                confiGureModel.setPid(pid);
                confiGureModel = confiGureModelMapper.selectOne(confiGureModel);
                int totalNum = confiGureModel.getNum();
                row = productListModelMapper.addGoodsStockNum(pid, goodsNum);
                if (row < 1)
                {
                    logger.info("修改商品库存失败！");
                }
                confiGureModel.setNum(goodsNum);
                confiGureModel.setTotal_num(0);
                row = confiGureModelMapper.addGoodsAttrStockNum(confiGureModel);
                if (row < 1)
                {
                    logger.info("修改商品属性库存失败！");
                }
                String     content    = " 管理员关闭订单【" + orderDetailsInfo.getR_sNo() + "】，返还库存" + goodsNum;
                StockModel stockModel = new StockModel();
                stockModel.setStore_id(storeId);
                stockModel.setProduct_id(pid);
                stockModel.setAttribute_id(Integer.valueOf(attributeId));
                stockModel.setTotal_num(totalNum);
                stockModel.setFlowing_num(goodsNum);
                stockModel.setType(0);
                stockModel.setUser_id(orderDetailsInfo.getUser_id());
                stockModel.setAdd_date(new Date());
                stockModel.setContent(content);
                stockModelMapper.insert(stockModel);
            }

            publiceService.addAdminRecord(orderVo.getStoreId(), "关闭了订单ID：" + orderVo.getOid() + " 的信息", AdminRecordModel.Type.UPDATE, orderVo.getAccessId());
        }
        catch (LaiKeAPIException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("关闭订单失败：{}", e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "close");
        }
    }

    @Override
    public Map<String, Object> kuaidishow(MainVo vo, String orderNo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            resultMap = publicOrderService.getLogistics(orderNo);
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void del(MainVo vo, String oid) throws LaiKeAPIException
    {
        try
        {
            User     user      = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            int      storeId   = vo.getStoreId();
            String[] orderList = oid.split(SplitUtils.DH);
            for (String orderNo : orderList)
            {
                int row = orderModelMapper.delOrderById(orderNo);
                if (row < 1)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SCSB, "删除失败");
                }
                publiceService.addAdminRecord(vo.getStoreId(), "删除了订单ID：" + orderNo + " 的信息", AdminRecordModel.Type.DEL, vo.getAccessId());
            }
        }
        catch (LaiKeAPIException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("删除订单失败：{}", e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "del");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Map<String, Object> returnList(MchPcReturnOrderVo vo, HttpServletResponse response) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            //售后数据
            Map<String, Object> returnParmaMap = new HashMap<>(16);
            returnParmaMap.put("store_id", vo.getStoreId());
            returnParmaMap.put("mch_id", user.getMchId());
            returnParmaMap.put("supplierReturn", "supplierReturn");
            if (!StringUtils.isEmpty(vo.getOrderno()))
            {
                returnParmaMap.put("ordernoLike", vo.getOrderno());
            }
            if (!Objects.isNull(vo.getReType()))
            {
                returnParmaMap.put("re_type", vo.getReType());
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
                    rTypeList.add(DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_AGREE_REBACK_GOODS);
                    returnParmaMap.put("rTypeList", rTypeList);
                    returnParmaMap.put("re_type", DictionaryConst.ReturnRecordReType.RETURNORDERTYPE_REFUSE_AMT);
                }
                else if (vo.getOrderStauts().equals(OrderStatusEnum.REFUND_SUCCESS))
                {
                    rTypeList.add(DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_RECEIVED_REBAKGOODS);
                    rTypeList.add(DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_AGREE_REBACK_AMT);
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
                map.put("returnType", returnType);
                //商品属性id
                Integer sid = MapUtils.getInteger(map, "sid");
                //商品数量
                Integer        num            = MapUtils.getInteger(map, "num");
                ConfiGureModel confiGureModel = confiGureModelMapper.selectByPrimaryKey(sid);
                //商品供货价
                map.put("supplyPrice", confiGureModel.getYprice().multiply(new BigDecimal(num)));
                //region 是否可以审核
                boolean isExamine;
                isExamine = publicRefundService.isExamine(vo.getStoreId(), refundId);
                if (Objects.nonNull(vo.getSource()))
                {
                    ReturnOrderModel returnOrderOld = returnOrderModelMapper.selectByPrimaryKey(refundId);
                    if (returnOrderOld == null)
                    {
                        isExamine = false;
                    }
                    else if (DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_AGREE_REBACK.equals(returnOrderOld.getR_type())
                            || (returnOrderOld.getR_type() >= DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_AGREE_REBACK && returnOrderOld.getR_type() < DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_QUICK_SUPPLIER_AGRESS))
                    {
                        {
                            isExamine = false;
                        }
                    }
                    else if (returnOrderOld.getR_type().equals(DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_QUICK_SUPPLIER_AGRESS))
                    {
                        isExamine = true;
                    }
                }
                map.put("isExamine", isExamine);

                //region 是否显示人工受理按钮 商品回寄之后被拒绝
                boolean isManExamine = false;
                if (!isExamine)
                {
                    isManExamine = publicRefundService.isMainExamine(vo.getStoreId(), refundId);
                }
                map.put("isManExamine", isManExamine);
                map.put("goodsImgUrl", publiceService.getImgPath(MapUtils.getString(map, "goodsImgUrl"), vo.getStoreId()));
            }
            if (vo.getExportType() == 1)
            {
                exportReturnOrderData(returnOrderMapList, response);
                return null;
            }

            //获取待付款/待发货/退款数
            MchOrderIndexVo mchOrderIndexVo = new MchOrderIndexVo();
            mchOrderIndexVo.setUserId(user.getUser_id());
            mchOrderIndexVo.setIsSupplier("isSupplierPro");
            mchOrderIndexVo.setIsSupplierPro("isSupplierPro");
            mchOrderIndexVo.setStoreId(vo.getStoreId());
            mchOrderIndexVo.setShopId(vo.getShopId());
            Map<String, Object> waitPayParamMap = setParam(mchOrderIndexVo, WAIT_PAY_TYPE);
            int                 waitPayNum      = getCont(waitPayParamMap, 1);
            Map<String, Object> sendParamMap    = setParam(mchOrderIndexVo, SEND_TYPE);
            int                 sendNum         = getCont(sendParamMap, 1);
            Map<String, Object> returnParamMap  = setParam(mchOrderIndexVo, RETURN_TYPE);
            int                 returnNum       = getCont(returnParamMap, 2);
            resultMap.put("payment_num", waitPayNum);
            resultMap.put("send_num", sendNum);
            resultMap.put("return_num", returnNum);
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
    public Map<String, Object> ordersNum(MainVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User             user             = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            AdminOrderListVo adminOrderListVo = new AdminOrderListVo();
            adminOrderListVo.setMchId(user.getMchId());
            adminOrderListVo.setStoreId(vo.getStoreId());
            adminOrderListVo.setStatus(DictionaryConst.OrdersStatus.ORDERS_R_STATUS_CONSIGNMENT);
            //直播订单数
            adminOrderListVo.setSelfLifting(13);
            Map<String, Object> map      = publicOrderService.pcMchOrderIndex(adminOrderListVo);
            Integer             orderNum = MapUtils.getInteger(map, "total");
            resultMap.put("orderNum", orderNum);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取拼团活动列表 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "index");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> refundPageById(MainVo vo, Integer id, String orderNo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
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
            //678  退货退款，用户寄回商品给供应商，供应商端同意退款后，在pc店铺这边应该是没有拒绝操作的 ，此处对于供应商商品单独处理
            ReturnOrderModel returnOrderModel = returnOrderModelMapper.selectByPrimaryKey(id);
            if (DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_QUICK_SUPPLIER_AGRESS.equals(returnOrderModel.getR_type()))
            {
                resultMap.put("refuseButton", false);
            }
            else
            {
                resultMap.put("refuseButton", true);
            }
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
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            vo.setMchId(user.getMchId());
            if (!publicRefundService.refund(vo))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SHSB, "售后失败");
            }
            ReturnOrderModel returnOrderModel = returnOrderModelMapper.selectByPrimaryKey(vo.getId());
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
    public Map<String, Object> settlementIndex(OrderSettlementVo vo, HttpServletResponse response) throws LaiKeAPIException
    {
        Map<String, Object> resultMap;
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            vo.setMchId(user.getMchId());
            vo.setSupplierOrder("supplierOrder");
            resultMap = publicOrderService.getSettlementOrderList(vo);
            if (vo.getExportType() == 1)
            {
                exportOrderSettlementData(DataUtils.cast(resultMap.get("list")), response);
                return null;
            }
            return resultMap;
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("订单结算列表 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "index");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Map<String, Object> settlementDetail(MainVo vo, String sNo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User               user               = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            AdminOrderDetailVo adminOrderDetailVo = new AdminOrderDetailVo();
            adminOrderDetailVo.setStoreId(vo.getStoreId());
            adminOrderDetailVo.setId(sNo);
            adminOrderDetailVo.setOrderType("see");
            resultMap = publicOrderService.orderPcDetails(adminOrderDetailVo);
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
    @Transactional(rollbackFor = Exception.class)
    public void settlementDel(MainVo vo, int id) throws LaiKeAPIException
    {
        try
        {
            int        row;
            User       user       = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            OrderModel orderModel = orderModelMapper.selectByPrimaryKey(id);
            if (orderModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DDBCZ, "订单不存在");
            }
            OrderModel orderUpdate = new OrderModel();
            orderUpdate.setId(id);
            orderUpdate.setRecycle(DictionaryConst.ProductRecycle.RECOVERY);
            row = orderModelMapper.updateByPrimaryKeySelective(orderUpdate);
            if (row < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SCSB, "删除失败");
            }
            row = orderDetailsModelMapper.delOrderDetails1(vo.getStoreId(), DictionaryConst.ProductRecycle.RECOVERY, orderModel.getsNo());
            if (row < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SCSB, "删除失败");
            }
            publiceService.addAdminRecord(vo.getStoreId(), user.getUser_id(), "删除ID为" + id + "结算订单", AdminRecordModel.Type.DEL_ORDER, AdminRecordModel.Source.PC_SHOP, user.getMchId());
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("删除结算订单 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "del");
        }
    }

    @Autowired
    private MessageLoggingModalMapper messageLoggingModalMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void oneClickDistribution(MainVo vo, String orders) throws LaiKeAPIException
    {
        try
        {
            User       user       = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            OrderModel orderModel = new OrderModel();
            orderModel.setStore_id(vo.getStoreId());
            orderModel.setsNo(orders);
            orderModel.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS);
            orderModel = orderModelMapper.selectOne(orderModel);
            if (orderModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DDBCZ, "订单不存在");
            }
            orderModel.setIs_lssued(DictionaryConst.WhetherMaven.WHETHER_OK);
            orderModelMapper.updateByPrimaryKeySelective(orderModel);
            OrderDetailsModel orderDetailsModel = new OrderDetailsModel();
            orderDetailsModel.setStore_id(orderModel.getStore_id());
            orderDetailsModel.setR_sNo(orderModel.getsNo());
            orderDetailsModel = orderDetailsModelMapper.select(orderDetailsModel).get(0);
            ProductListModel productListModel = productListModelMapper.selectByPrimaryKey(orderDetailsModel.getP_id());
            //通知后台消息
            MessageLoggingModal messageLoggingSave = new MessageLoggingModal();
            messageLoggingSave.setStore_id(vo.getStoreId());
            messageLoggingSave.setType(MessageLoggingModal.Type.TYPE_ORDER_WAIT_SEND);
            messageLoggingSave.setParameter(String.valueOf(orderModel.getId()));
            messageLoggingSave.setContent(String.format("您来新订单了，订单为%s，请及时处理！", orderModel.getsNo()));
            messageLoggingSave.setAdd_date(new Date());
            messageLoggingSave.setSupplier_id(Integer.valueOf(productListModel.getGongyingshang()));
            messageLoggingModalMapper.insertSelective(messageLoggingSave);

            publiceService.addAdminRecord(vo.getStoreId(), "将订单ID：" + orderModel.getsNo() + " 进行了一键待发操作", AdminRecordModel.Type.UPDATE, vo.getAccessId());
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("一键代发 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "oneClickDistribution");
        }
    }

    //导出订单结算列表
    private void exportOrderSettlementData(List<Map<String, Object>> list, HttpServletResponse response) throws LaiKeAPIException
    {
        try
        {
            //表头
            String[] headerList = new String[]{"结算单号", "订单编号", "结算金额", "订单金额", "运费", "店铺优惠", "平台优惠", "退单金额", "结算状态", "结算时间", "订单生成时间"};
            //对应字段
            String[] kayList = new String[]{"id", "sNo", "settlementPrice", "z_price", "z_freight", "mch_discount", "preferential_amount", "return_money", "status_name", "arrive_time", "add_time"};
            //对应字段
            ExcelParamVo vo = new ExcelParamVo();
            vo.setTitle("订单结算列表");
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
            logger.error("导出订单列表数据 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "exportOrderData");
        }

    }

    //导出订单列表
    private void exportOrderData(List<Map<String, Object>> list, HttpServletResponse response) throws LaiKeAPIException
    {
        try
        {
            //表头
            String[] headerList = new String[]{"订单编号", "订单明细id", "创单时间", "产品名称", "规格", "数量", "小计", "订单总计", "数量", "订单状态", "订单类型", "用户ID", "联系人"
                    , "电话", "地址", "支付方式", "物流单号", "运费"};
            //对应字段
            String[] kayList = new String[]{"orderno", "detailId", "createDate", "goodsName", "attrStr", "needNum", "goodsPrice", "orderPrice", "goodsNum", "status", "otype", "userId", "userName"
                    , "mobile", "addressInfo", "payName", "courier_num", "freight"};
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

    /**
     * 导出售后列表
     *
     * @param orderList -
     * @param response  -
     * @throws LaiKeAPIException-
     */
    private void exportReturnOrderData(List<Map<String, Object>> orderList, HttpServletResponse response) throws LaiKeAPIException
    {
        try
        {
            //表头
            String[] headerList = new String[]{"用户id", "商品名称", "商品价格", "供货价", "数量", "订单号", "所属供应商", "实退金额", "申请时间", "类型", "状态"};
            //对应字段
            String[]     kayList = new String[]{"user_id", "p_name", "p_price", "supplyPrice", "num", "sNo", "supplier_name", "real_money", "re_time", "returnType", "returnName"};
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

    /**
     * 查询参数赋值
     *
     * @param vo
     * @param type 1：待付款 2：待发货 3：退款/售后
     * @return
     */
    private Map<String, Object> setParam(MchOrderIndexVo vo, Integer type)
    {
        Map<String, Object> paramMap   = new HashMap<>();
        List<String>        orderTypes = new ArrayList<>();
        List<String>        rTypeList  = new ArrayList<>();
        List<String>        statusList = new ArrayList<>();
        paramMap.put("store_id", vo.getStoreId());
        paramMap.put("mch_id", vo.getShopId());
        if (StringUtils.isNotEmpty(vo.getIsSupplierPro()))
        {
            paramMap.put("supplierPro", "supplierPro");
        }
        if (StringUtils.isNotEmpty(vo.getOrderHeadrType()))
        {
            paramMap.put("orderType", vo.getOrderHeadrType());
        }
        if (StringUtils.isNotEmpty(vo.getOrderHeadrType()))
        {
            paramMap.put("orderStauts", vo.getOrderStauts());
        }
        if (StringUtils.isNotEmpty(vo.getIsSupplier()))
        {
            paramMap.put("isSupplierPro", "isSupplierPro");
        }
        paramMap.put("recycle1", DictionaryConst.ProductRecycle.NOT_STATUS);

        switch (type)
        {
            case WAIT_PAY_TYPE:
                statusList.add(String.valueOf(DictionaryConst.OrdersStatus.ORDERS_R_STATUS_UNPAID));
                break;
            case SEND_TYPE:
                paramMap.put("statusType", 3);
                break;
            case RETURN_TYPE:
                rTypeList.add(DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_EXAMEWAIT_STATUS.toString());
                rTypeList.add(DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_AGREE_REBACK.toString());
                rTypeList.add(DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_USER_DELIVERED.toString());
                paramMap.put("rTypeList", rTypeList);
                break;
        }
        paramMap.put("statusList", statusList);

        if (StringUtils.isEmpty(vo.getOrderHeadrType()))
        {
            orderTypes.add(DictionaryConst.OrdersType.ORDERS_HEADER_GM);
            paramMap.put("orderType", DictionaryConst.OrdersType.ORDERS_HEADER_GM);
        }
        else
        {
            orderTypes.add(vo.getOrderHeadrType());
            paramMap.put("orderType", (vo.getOrderHeadrType()));
        }
        paramMap.put("orderTypes", orderTypes);
        if (StringUtils.isNotEmpty(vo.getKeyword()))
        {
            paramMap.put("likeOrderno", vo.getKeyword());
        }
        return paramMap;
    }

    /**
     * 获取待支付数
     *
     * @param paramMap
     * @return
     */
    private int getCont(Map<String, Object> paramMap, Integer type)
    {
        int count = 0;
        switch (type)
        {
            case 1:
                count = orderModelMapper.countOrdersNumDynamic(paramMap);
                break;
            case 2:
                count = returnOrderModelMapper.countRturnOrderNumDynamic(paramMap);
                break;
        }
        return count;
    }
}
