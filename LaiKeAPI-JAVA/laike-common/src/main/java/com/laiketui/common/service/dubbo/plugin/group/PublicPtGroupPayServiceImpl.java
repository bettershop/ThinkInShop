package com.laiketui.common.service.dubbo.plugin.group;

import com.laiketui.common.api.PublicOrderService;
import com.laiketui.common.api.plugin.PubliceGroupService;
import com.laiketui.common.api.plugin.group.PublicGroupPayService;
import com.laiketui.common.api.plugin.payment.PaymentService;
import com.laiketui.common.mapper.*;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.DictionaryConst;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.core.utils.tool.DateUtil;
import com.laiketui.domain.group.GroupProductModel;
import com.laiketui.domain.plugin.group.GoGroupOrderModel;
import com.laiketui.domain.plugin.group.PtGoGroupOrderModel;
import com.laiketui.domain.vo.pay.PayCallBackVo;
import com.laiketui.domain.vo.pay.PayVo;
import com.laiketui.root.common.BuilderIDTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 拼团支付
 *
 * @author Trick
 * @date 2021/4/1 9:55
 */
@Service("ptGroupPayService")
public class PublicPtGroupPayServiceImpl implements PaymentService, PublicGroupPayService
{
    private final Logger logger = LoggerFactory.getLogger(PublicPtGroupPayServiceImpl.class);

    @Autowired
    private PtGoGroupOrderModelMapper goGroupOrderModelMapper;

    @Autowired
    private PtGoGroupOrderDetailsModelMapper goGroupOrderDetailsModelMapper;

    @Autowired
    private PtGroupProductModelMapper groupProductModelMapper;

    @Autowired
    private PtGoGroupOpenModelMapper groupOpenModelMapper;

    @Autowired
    private GroupConfigModelMapper groupConfigModelMapper;

    @Autowired
    private PublicOrderService publicOrderService;

    @Autowired
    private PubliceGroupService publiceGroupService;

    @Autowired
    private OrderModelMapper orderModelMapper;

    @Autowired
    private OrderDetailsModelMapper orderDetailsModelMapper;

    @Autowired
    private PtGoGroupOrderModelMapper ptGoGroupOrderModelMapper;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Map<String, Object> payment(PayVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            Map<String, Object> parmaMap = new HashMap<>(16);
            int                 count;
            //获取订单信息
            parmaMap.put("sNo", vo.getOrderno());
            parmaMap.put("user_id", vo.getUserId());
            parmaMap.put("store_id", vo.getStoreId());
            parmaMap.put("status", GoGroupOrderModel.OrderStatus.NOTPAY);
            List<Map<String, Object>> orderInfo = ptGoGroupOrderModelMapper.getGrouporder(parmaMap);
            if (orderInfo == null || orderInfo.size() < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DDBCZ, "订单不存在");
            }
            for (Map<String, Object> order : orderInfo)
            {
                //订单id
                int orderId = Integer.parseInt(order.get("id").toString());
                //订单模式
                String otype = order.get("pid").toString();
                //支付金额,抵扣金额  支付这里把抵扣金额加进订单金额里,以后就不需再计算金额了
                BigDecimal payAmt = new BigDecimal(order.get("z_price").toString()), offsetBalance = new BigDecimal(order.get("offset_balance").toString());
                payAmt = payAmt.add(offsetBalance);
                //拼团编号
                String ptcode = order.get("ptcode") + "";
                if (GoGroupOrderModel.OrderPid.KAITUAN.toLowerCase().equals(otype))
                {
                    //生成拼团编号 15位
                    ptcode = DictionaryConst.OrdersType.ORDERS_HEADER_KT + BuilderIDTool.getNext(BuilderIDTool.Type.NUMBER, 13);
                    groupOpenPay(vo.getStoreId(), vo.getUserId(), ptcode, order);
                }
                else
                {
                    //校验数据
                    if (payValidata(vo))
                    {
                        groupCanOpenPay(vo.getStoreId(), ptcode);
                    }
                    else
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_ZFSB, "支付失败");
                    }
                }
                //支付成功,修改订单信息
                PtGoGroupOrderModel goGroupOrderUpdate = new PtGoGroupOrderModel();
                goGroupOrderUpdate.setId(orderId);
                goGroupOrderUpdate.setPtcode(ptcode);
                goGroupOrderUpdate.setZ_price(payAmt);
                goGroupOrderUpdate.setPay(vo.getPayType());
                goGroupOrderUpdate.setOffset_balance(payAmt);
                goGroupOrderUpdate.setTrade_no(vo.getTradeNo());
                goGroupOrderUpdate.setStatus(GoGroupOrderModel.OrderStatus.PT_ING);
                goGroupOrderUpdate.setPtstatus(GoGroupOrderModel.Ptstatus.PT_ING);
                goGroupOrderUpdate.setPay_time(new Date());
                count = ptGoGroupOrderModelMapper.updateByPrimaryKeySelective(goGroupOrderUpdate);
                if (count < 1)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_ZFSB, "支付失败");
                }
                count = goGroupOrderDetailsModelMapper.updateStatus(vo.getStoreId(), vo.getOrderno(), GoGroupOrderModel.OrderStatus.DELIVER);
                if (count < 1)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_ZFSB, "支付失败");
                }
                //返回支付金额
                resultMap.put("payAmt", payAmt);
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("拼团订单支付校验 异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "payment");
        }
        return resultMap;
    }

    /**
     * 开团支付流程
     *
     * @param storeId -
     * @param userId  -
     * @param ptCode  -
     * @param order   -
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/4/1 16:03
     */
    private void groupOpenPay(int storeId, String userId, String ptCode, Map<String, Object> order) throws LaiKeAPIException
    {

    }

    /**
     * 参团支付流程
     *
     * @param storeId -
     * @param ptCode  -
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/4/1 16:03
     */
    private void groupCanOpenPay(int storeId, String ptCode) throws LaiKeAPIException
    {

    }

    @Override
    public boolean payValidata(PayVo vo) throws LaiKeAPIException
    {
        try
        {
            int                 count;
            Map<String, Object> parmaMap = new HashMap<>(16);
            //获取订单信息
            parmaMap.put("sNo", vo.getOrderno());
            parmaMap.put("user_id", vo.getUserId());
            parmaMap.put("store_id", vo.getStoreId());
            parmaMap.put("status", GoGroupOrderModel.OrderStatus.NOTPAY);
            List<Map<String, Object>> orderInfo = goGroupOrderModelMapper.getGrouporder(parmaMap);
            if (orderInfo == null || orderInfo.size() < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DDBCZ, "订单不存在");
            }
            int activityId = 0;
            //拼团只会有一个商品,只会循环一次
            for (Map<String, Object> order : orderInfo)
            {
                //订单id
                int orderId = Integer.parseInt(order.get("id").toString());
                //活动id
                activityId = Integer.parseInt(order.get("activity_no").toString());
                //活动状态
                int ptStatus = Integer.parseInt(order.get("g_status").toString());
                //活动过期时间
                Date endDate = DateUtil.dateFormateToDate(order.get("endtime").toString(), GloabConst.TimePattern.YMDHMS);

                //判断活动是否在拼团中
                if (GroupProductModel.GROUP_GOODS_STATUS_UNDER_WAY == ptStatus)
                {
                    //判断活动是否过期
                    if (DateUtil.dateCompare(endDate, new Date()))
                    {
                        //校验通过
                        PtGoGroupOrderModel goGroupOrderModel = new PtGoGroupOrderModel();
                        goGroupOrderModel.setId(orderId);
                        goGroupOrderModel.setPay(vo.getPayType());
                        String orderno = publicOrderService.createOrderNo(DictionaryConst.OrdersType.PTHD_ORDER_PP);
                        goGroupOrderModel.setReal_sno(orderno);
                        goGroupOrderModel.setRemark(vo.getRemark());
                        count = goGroupOrderModelMapper.updateByPrimaryKeySelective(goGroupOrderModel);
                        if (count < 1)
                        {
                            logger.debug("拼团订单{} 修改支付信息失败", vo.getOrderno());
                            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常");
                        }
                        return true;
                    }
                    else
                    {
                        logger.debug("拼团订单{}正在支付前校验,校验失败:活动已过期", vo.getOrderno());
                    }
                }
                else
                {
                    logger.debug("拼团订单{}正在支付前校验,校验失败:订单状态不在待支付状态", vo.getOrderno());
                }
            }
            //关闭订单
            goGroupOrderModelMapper.updateStatus(vo.getStoreId(), vo.getOrderno(), GoGroupOrderModel.OrderStatus.ORDER_CLOSE);
            count = goGroupOrderDetailsModelMapper.updateStatus(vo.getStoreId(), vo.getOrderno(), GoGroupOrderModel.OrderStatus.ORDER_CLOSE);
            if (count < 1)
            {
                logger.debug("拼团订单{} 订单关闭失败", vo.getOrderno());
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常");
            }
            //回滚库存
            publiceGroupService.reBackGoodsNum(vo.getStoreId(), vo.getOrderno(), activityId, "拼团订单关闭");
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("拼团订单支付校验 异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "payValidata");
        }
        return false;
    }

    @Override
    public Map<String, Object> callBack(PayCallBackVo vo) throws LaiKeAPIException
    {
        return null;
    }

}

