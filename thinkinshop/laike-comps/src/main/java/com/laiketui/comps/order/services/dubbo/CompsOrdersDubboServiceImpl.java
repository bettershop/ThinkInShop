package com.laiketui.comps.order.services.dubbo;


import com.laiketui.common.api.PublicOrderService;
import com.laiketui.common.api.pay.PublicPaymentService;
import com.laiketui.common.mapper.*;
import com.laiketui.common.utils.tool.data.OrderDataUtils;
import com.laiketui.comps.api.order.CompsOrderTaskService;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.DictionaryConst;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.core.utils.tool.DateUtil;
import com.laiketui.core.utils.tool.StringUtils;
import com.laiketui.domain.living.LivingCommissionModel;
import com.laiketui.domain.log.MchAccountLogModel;
import com.laiketui.domain.mch.MchModel;
import com.laiketui.domain.order.OrderDetailsModel;
import com.laiketui.domain.order.OrderModel;
import com.laiketui.domain.supplier.SupplierAccountLogModel;
import com.laiketui.domain.supplier.SupplierModel;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

import static com.laiketui.core.lktconst.DictionaryConst.OrderPayType.ORDERPAYTYPE_MINI_WECHAT;


/**
 * 订单dubbo服务
 *
 * @author Trick
 * @date 2023/4/21 14:43
 */
@Service
@RequiredArgsConstructor(onConstructor_ = {@Lazy})
public class CompsOrdersDubboServiceImpl implements CompsOrderTaskService
{
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final PublicOrderService publicOrderService;

    private final OrderModelMapper orderModelMapper;

    private final OrderDetailsModelMapper orderDetailsModelMapper;

    private final ReturnOrderModelMapper returnOrderModelMapper;

    private final MchModelMapper mchModelMapper;

    private final SupplierOrderFrightModelMapper supplierOrderFrightModelMapper;

    private final SupplierModelMapper supplierModelMapper;

    private final SupplierAccountLogModelMapper supplierAccountLogModelMapper;

    private final MchAccountLogModelMapper mchAccountLogModelMapper;

    private final LivingCommissionModelMapper livingCommissionModelMapper;

    private SignRecordModelMapper signRecordModelMapper;

    private UserBaseMapper userBaseMapper;

    @Autowired
    @Qualifier("publicWechatServiceImpl")
    private PublicPaymentService publicPaymentService;

    @Override

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> orderSettlement(int storeId, int mchId, String orderType) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        StringBuilder       loggerStr = new StringBuilder();
        out:
        try
        {
            if (StringUtils.isEmpty(orderType))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.PARAMATER_ERROR, "订单类型不能为空");
            }
            //准备结算的订单集合
            List<String> settlementOrders = new ArrayList<>();
            //还有商品在售后状态的订单
            Set<String> returnOrderList = new HashSet<>();
            loggerStr.append(String.format("==== 开始处理店铺id:%s ====\n", mchId));

            Map<String, Object> configMap = publicOrderService.getOrderConfig(storeId, mchId, orderType.toUpperCase());
            if (configMap == null)
            {
                loggerStr.append(String.format("【%s】插件-未开启/未配置 \n", OrderDataUtils.getOrderType(orderType)));
                break out;
            }
            if (configMap.containsKey("isSettlement") && !MapUtils.getBooleanValue(configMap, "isSettlement"))
            {
                loggerStr.append(String.format("【%s】插件-不进行结算 \n", OrderDataUtils.getOrderType(orderType)));
                break out;
            }
            //售后日期 单位【秒】
            int orderAfterSec = MapUtils.getInteger(configMap, "orderAfter");

            //已处理过的订单
            List<String>        divideOrderList = new ArrayList<>();
            Map<String, Object> parmaMap        = new HashMap<>(16);
            parmaMap.put("mch_id", mchId);
            parmaMap.put("store_id", storeId);
            parmaMap.put("orderType", orderType);
            parmaMap.put("recycle", DictionaryConst.ProductRecycle.NOT_STATUS);
            parmaMap.put("settlement_type", OrderDetailsModel.SETTLEMENT_TYPE_UNSETTLED);
            //parmaMap.put("r_status", DictionaryConst.OrdersStatus.ORDERS_R_STATUS_COMPLETE);
            List<Map<String, Object>> orderInfoList = orderModelMapper.getOrderInfoLeftDetailDynamic(parmaMap);
            for (Map<String, Object> map : orderInfoList)
            {
                String orderno = map.get("sNo").toString();
                String otype   = map.get("otype").toString();
                //如果是直播的订单，需要将佣金表的状态改为已发放
                if (DictionaryConst.OrdersType.ORDERS_HEADER_ZB.equals(otype))
                {
                    LivingCommissionModel livingCommissionModel = new LivingCommissionModel();
                    livingCommissionModel.setRecycle(0);
                    livingCommissionModel.setS_no(orderno);
                    livingCommissionModel = livingCommissionModelMapper.selectOne(livingCommissionModel);
                    livingCommissionModel.setStatus(LivingCommissionModel.COMMISSION_STATUS_SETTLED);
                    livingCommissionModelMapper.updateByPrimaryKeySelective(livingCommissionModel);
                    loggerStr.append(String.format("修改佣金信息失败 \n", orderno));
                }
                //订单总金额
                BigDecimal orderAmt = new BigDecimal(map.get("z_price").toString());
                //明细id
                int detailId = Integer.parseInt(map.get("detailId").toString());
                //明细id
                int pid = Integer.parseInt(map.get("p_id").toString());
                //收货时间
                Date orderDate = DateUtil.dateFormateToDate(MapUtils.getString(map, "arrive_time"), GloabConst.TimePattern.YMDHMS);

                Integer selfLifting = MapUtils.getInteger(map, "self_lifting");
                //秒杀自提订单，在结算的时候，赋值结算时间
                if (orderType.equals(DictionaryConst.OrdersType.ORDERS_HEADER_MS) && selfLifting == 1) {
                    orderDate = new Date();
                }
                if (orderDate == null)
                {
                    loggerStr.append(String.format("==== 订单号:%s 数据有问题,没有收货时间-不进行结算! ====\n", orderno));
                    continue;
                }
                //售后终止日期 收货时间+售后时间
                Date arriveDate = DateUtil.getAddDateBySecond(orderDate, orderAfterSec);
                loggerStr.append(String.format("订单%s 开始进行结算 收货日期%s 最终售后日期:%s \n", orderno, DateUtil.dateFormate(orderDate, GloabConst.TimePattern.YMDHMS), DateUtil.dateFormate(arriveDate, GloabConst.TimePattern.YMDHMS)));
                if (!DateUtil.dateCompare(new Date(), arriveDate))
                {
                    returnOrderList.add(orderno);
                    settlementOrders.remove(orderno);
                    loggerStr.append("未过售后期,等待下次结算\n");
                    continue;
                }

                //优惠后的金额
                BigDecimal orderDetailAmt = new BigDecimal(map.get("after_discount").toString());
                //积分商品特殊处理，积分商品优惠后的金额为商品所需积分
                if (orderType.equals(DictionaryConst.OrdersType.ORDERS_HEADER_IN))
                {
                    //使用积分商品价格为设计支付价格
                    orderDetailAmt = new BigDecimal(map.get("p_price").toString());
                }
                //运费
                BigDecimal freight = new BigDecimal(map.get("freight").toString());
                //结算金额
                BigDecimal money = orderDetailAmt.add(freight);

                loggerStr.append(String.format("店铺id%s,订单结算日为: %s 秒", mchId, orderAfterSec));
                loggerStr.append(String.format("正在结算订单: %s\n 订单金额:%s,优惠后的金额:%s,运费:%s,最后结算金额%s \n", orderno, orderAmt, orderDetailAmt, freight, money));
                if (money.doubleValue() >= 0)
                {
                    //订单是否在售后中,如果在售后中则不进行结算
                    if (returnOrderList.contains(orderno))
                    {
                        settlementOrders.remove(orderno);
                        loggerStr.append(String.format("订单%s 还在售后中,不予结算,等待下次结算\n", orderno));
                        continue;
                    }
                    int count = returnOrderModelMapper.orderDetailReturnIsNotEnd(storeId, orderno, detailId);
                    if (count > 0)
                    {
                        returnOrderList.add(orderno);
                        settlementOrders.remove(orderno);
                        loggerStr.append(String.format("订单%s-->明细id%s 还在售后中,此次不予结算\n", orderno, detailId));
                        continue;
                    }
                    //店铺金额结算
                    count = mchModelMapper.settlementMch(storeId, mchId, money);
                    //标记明细结算
                    if (count > 0)
                    {
                        //标记订单已结算
                        OrderDetailsModel orderDetailsModel = new OrderDetailsModel();
                        orderDetailsModel.setId(detailId);
                        orderDetailsModel.setSettlement_type(OrderDetailsModel.SETTLEMENT_TYPE_SETTLED);
                        count = orderDetailsModelMapper.updateByPrimaryKeySelective(orderDetailsModel);
                        //虚拟订单直接结算
                        if (DictionaryConst.OrdersType.ORDERS_HEADER_VI.equals(otype) && !returnOrderList.contains(orderno))
                        {
                            orderModelMapper.updateOrderSettlementByOrderNo(storeId, orderno, new Date());
                        }
                        loggerStr.append(String.format("标记已经结算 执行结果: %s\n", count > 0));
                        if (!settlementOrders.contains(orderno))
                        {
                            settlementOrders.add(orderno);
                        }
                        loggerStr.append(String.format("订单结算成功 明细id: %s\n", count > 0));
                    }
                    else
                    {
                        settlementOrders.remove(orderno);
                        loggerStr.append(String.format("订单明细结算失败 明细id: %s\n", detailId));
                    }
                }
                //供应商订单结算逻辑
                String supplierId = MapUtils.getString(map, "gongyingshang");
                //供应商订单没有插件订单只有普通订单
                if (DictionaryConst.OrdersType.ORDERS_HEADER_GM.equals(orderType) && StringUtils.isNotEmpty(supplierId))
                {
                    loggerStr.append(String.format("正在处理id为%s 供应商的订单 订单号:%s -->商品id%s\n", supplierId, orderno, detailId));
                    BigDecimal totalAmount = BigDecimal.ZERO;
                    //订单运费结算
                    BigDecimal totalFright = supplierOrderFrightModelMapper.getOrderTotal(storeId, orderno, pid);
                    if (totalFright.compareTo(BigDecimal.ZERO) > 0)
                    {
                        //增加供应商余额
                        supplierModelMapper.addPrice(Integer.parseInt(supplierId), totalFright);
                        SupplierModel supplierModel = supplierModelMapper.selectByPrimaryKey(supplierId);
                        //生成余额明细
                        supplierAccountLogModelMapper.saveAccountLog(storeId, Integer.parseInt(supplierId), totalFright, supplierModel.getSurplus_balance(), SupplierAccountLogModel.ENTRY, SupplierAccountLogModel.ORDERFRIGHT, new Date(), orderno, "订单运费");
                        //标记该订单当前商品运费已结算
                        supplierOrderFrightModelMapper.updateIsSettlement(storeId, orderno, pid);
                        totalAmount = totalAmount.add(totalFright);
                    }
                    //订单商品供货价结算(供应商订单详情下单时保存了当时的总供货价)
                    BigDecimal supplierSettlement = new BigDecimal(MapUtils.getString(map, "supplier_settlement"));
                    supplierModelMapper.addPrice(Integer.parseInt(supplierId), supplierSettlement);
                    SupplierModel supplierModel = supplierModelMapper.selectByPrimaryKey(supplierId);
                    //添加供应商账户余额记录
                    supplierAccountLogModelMapper.saveAccountLog(storeId, Integer.parseInt(supplierId), supplierSettlement, supplierModel.getSurplus_balance(), SupplierAccountLogModel.ENTRY, SupplierAccountLogModel.ORDER, new Date(), orderno, "订单总供货价");
                    //供货价/运费由店铺结算给供应商 更新店铺余额
                    totalAmount = totalAmount.add(supplierSettlement);
                    parmaMap.put("t_money", totalAmount);
                    parmaMap.put("store_id", storeId);
                    parmaMap.put("mch_id", mchId);
                    int count = mchModelMapper.withdrawal(parmaMap);
                    if (count > 0)
                    {
                        loggerStr.append(String.format("%s供应商 订单号:%s-->明细id %s订单结算成功 \n", supplierId, orderno, detailId));
                    }
                    //记录店铺收支信息
                    MchModel           mchModel           = mchModelMapper.selectByPrimaryKey(mchId);
                    MchAccountLogModel mchAccountLogModel = new MchAccountLogModel();
                    mchAccountLogModel.setStore_id(storeId);
                    mchAccountLogModel.setMch_id(String.valueOf(mchId));
                    mchAccountLogModel.setRemake(orderno);
                    mchAccountLogModel.setPrice(totalAmount);
                    mchAccountLogModel.setStatus(DictionaryConst.MchAccountLogStatus.MCHACCOUNTLOG_STATUS_EXPENDITURE);
                    mchAccountLogModel.setAccount_money(mchModel.getAccount_money());
                    mchAccountLogModel.setType(DictionaryConst.MchAccountLogType.MCHACCOUNTLOG_TYPE_SUPPLIER);
                    mchAccountLogModel.setAddtime(new Date());
                    mchAccountLogModelMapper.insertSelective(mchAccountLogModel);
                }
                //分账处理
//                if (!divideOrderList.contains(orderno)){
//                    OrderModel orderModel = new OrderModel();
//                    orderModel.setsNo(orderno);
//                    orderModel = orderModelMapper.selectOne(orderModel);
//                    if (orderModel.getDividend_status()==1 && orderModel.getPay().equals(ORDERPAYTYPE_MINI_WECHAT) && StringUtils.isEmpty(supplierId)) {
//                        //请求微信分账
//                        Map<String, Object> divideAccountMap = publicWechatServiceImpl.divideAccount(orderModel.getReal_sno(), orderModel.getTransaction_id(), loggerStr);
//                        String resultJsonString = MapUtils.getString(divideAccountMap, "resultJsonString");
//                        JSONObject jsonObject = JSONObject.parseObject(resultJsonString);
//                        if (jsonObject.containsKey("code")) {
//                            loggerStr.append(String.format("订单%s微信请求分账失败 失败原因%s", orderno, jsonObject.get("message").toString()));
//                        } else {
//                            loggerStr.append(String.format("订单%s微信请求分账成功 处理结果：%s", orderno, JSONObject.toJSONString(divideAccountMap)));
//                            divideOrderList.add(orderno);
//                        }
//                    }
//                }

                if (!divideOrderList.contains(orderno))
                {

                    OrderModel orderModel = new OrderModel();
                    orderModel.setsNo(orderno);
                    orderModel = orderModelMapper.selectOne(orderModel);
                    if (orderModel.getDividend_status() == 1 && orderModel.getPay().equals(ORDERPAYTYPE_MINI_WECHAT)
                            && orderModel.getStatus() == OrderModel.ORDER_FINISH && StringUtils.isEmpty(supplierId)
                    )
                    {
                        //请求微信分账
                        Map<String, Object> divideAccountMap = publicPaymentService.newDivideAccount(orderModel.getReal_sno(), orderModel.getTransaction_id(), loggerStr);
                        String              resultJsonString = MapUtils.getString(divideAccountMap, "resultJsonString");
                        String              divideRequest    = MapUtils.getString(divideAccountMap, "result");
                        if (resultJsonString.equals("SUCCESS"))
                        {
                            loggerStr.append(String.format("订单%s微信请求分账成功", orderno));
                            divideOrderList.add(orderno);
                        }
                        else
                        {
                            loggerStr.append(String.format("订单%s微信请求分账失败 失败原因%s", orderno, divideRequest));
                        }
                    }
                }
            }


            //标记订单结算
            int successNum = 0;
            int failNum    = 0;
            for (String orderNo : settlementOrders)
            {
                if (returnOrderList.contains(orderNo))
                {
                    loggerStr.append(String.format("订单再售后中,此次不予结算 订单号: %s", orderNo));
                    continue;
                }
                int row = orderModelMapper.updateOrderSettlementByOrderNo(storeId, orderNo, new Date());
                if (row > 0)
                {
                    successNum++;
                }
                else
                {
                    failNum++;
                }
                loggerStr.append(String.format("订单结算状态%s 订单号: %s", row > 0, orderNo));
                //禅道50113会员生日特权积分
//                SignRecordModel signRecordModel = new SignRecordModel();
//                signRecordModel.setStore_id(storeId);
//                signRecordModel.setType(SignRecordModel.ScoreType.MEMBER_BIRTHDAY);
//                signRecordModel.setsNo(orderNo);
//                signRecordModel = signRecordModelMapper.selectOne(signRecordModel);
//                if (signRecordModel != null){
//                    User user = userBaseMapper.selectByUserId(storeId, signRecordModel.getUser_id());
//                    user.setLock_score(user.getLock_score() - signRecordModel.getSign_score());
//                    user.setScore(user.getScore() + signRecordModel.getSign_score());
//                    userBaseMapper.updateByPrimaryKeySelective(user);
//                }
            }
            loggerStr.append(String.format(">>>> 结算成功,总共%s个,成功结算%s个,失败%s个,%s个再售后期的订单,等下次结算 <<<<", settlementOrders.size(), successNum, failNum, returnOrderList.size()));
        }
        catch (Exception e)
        {
            logger.error("订单结算任务异常 ", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常");
        }
        resultMap.put("logger", loggerStr.toString());
        return resultMap;
    }
}
