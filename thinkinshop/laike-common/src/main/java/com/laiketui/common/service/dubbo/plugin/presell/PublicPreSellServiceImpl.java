package com.laiketui.common.service.dubbo.plugin.presell;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.laiketui.common.api.PublicMemberService;
import com.laiketui.common.api.PubliceService;
import com.laiketui.common.api.pay.PublicPaymentService;
import com.laiketui.common.api.plugin.PublicPreSellService;
import com.laiketui.common.mapper.CustomerModelMapper;
import com.laiketui.common.mapper.OrderModelMapper;
import com.laiketui.common.mapper.PreSellRecordModelMapper;
import com.laiketui.common.mapper.SystemMessageModelMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.DictionaryConst;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.domain.home.SystemMessageModel;
import com.laiketui.domain.mch.CustomerModel;
import com.laiketui.domain.order.OrderModel;
import com.laiketui.domain.presell.PreSellRecordModel;
import com.laiketui.domain.vo.main.RefundVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @Author: sunH_
 * @Date: Create in 15:04 2023/8/24
 */
@Service
public class PublicPreSellServiceImpl implements PublicPreSellService
{

    private final Logger logger = LoggerFactory.getLogger(PublicPreSellServiceImpl.class);

    @Autowired
    private PublicMemberService publicMemberService;

    @Autowired
    @Qualifier("publicAlipayServiceImpl")
    private PublicPaymentService publicAlipayServiceImpl;

    @Autowired
    @Qualifier("publicWechatServiceImpl")
    private PublicPaymentService publicWechatServiceImpl;

    @Autowired
    protected PreSellRecordModelMapper preSellRecordModelMapper;

    @Autowired
    private CustomerModelMapper customerModelMapper;

    @Autowired
    private OrderModelMapper orderModelMapper;

    @Autowired
    private PubliceService publiceService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean returnAmt(RefundVo vo, String orderNo, boolean isSendNotice, Map<String, String> smsParmaMap) throws LaiKeAPIException
    {
        boolean isExamine = false;
        try
        {
            String mchPhone = "";
            //获取电商平台管理员信息
            CustomerModel customerModel = new CustomerModel();
            customerModel.setId(vo.getStoreId());
            customerModel.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS);
            customerModel = customerModelMapper.selectOne(customerModel);
            if (customerModel != null)
            {
                mchPhone = customerModel.getMobile();
            }
            //订单信息
            OrderModel orderModel = new OrderModel();
            orderModel.setsNo(orderNo);
            orderModel.setStore_id(vo.getStoreId());
            orderModel = orderModelMapper.selectOne(orderModel);
            if (Objects.isNull(orderModel))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_MBBCZ, "订单信息不存在", "returnAmt");
            }
            //判断是否为预售商品，修改预售订单表的数据
            PreSellRecordModel preSellRecordModel = new PreSellRecordModel();
            preSellRecordModel.setsNo(orderNo);
            preSellRecordModel.setIs_pay(DictionaryConst.WhetherMaven.WHETHER_OK);
            List<PreSellRecordModel> select = preSellRecordModelMapper.select(preSellRecordModel);
            if (!Objects.isNull(select) && select.size() > 0)
            {
                for (PreSellRecordModel model : select)
                {
                    //退款流程
                    switch (model.getPay())
                    {
                        case DictionaryConst.OrderPayType.ORDERPAYTYPE_WALLET_PAY:
                            //钱包支付全额退款
                            publicMemberService.returnUserMoney(vo.getStoreId(), model.getUser_id(), vo.getPrice(), orderNo);
                            isExamine = true;
                            break;
                        case DictionaryConst.OrderPayType.ORDERPAYTYPE_ALIPAY:
                        case DictionaryConst.OrderPayType.ORDERPAYTYPE_ALIPAY_TMP:
                        case DictionaryConst.OrderPayType.ORDERPAYTYPE_PC_ALIPAY:
                        case DictionaryConst.OrderPayType.ORDERPAYTYPE_ALIPAY_MINIPAY:
                        case DictionaryConst.OrderPayType.ORDERPAYTYPE_ALIPAY_WAP:
                            //发起退款
                            Map<String, String> map = publicAlipayServiceImpl.refundOrder(vo.getStoreId(), orderModel.getId(), model.getPay(), model.getReal_sno(), vo.getPrice());
                            String code = map.get("code");
                            if (DictionaryConst.AliApiCode.ALIPAY_ACQ_SELLER_BALANCE_NOT_ENOUGH.equals(code))
                            {
                                //短信通知商家
                                if (isSendNotice)
                                {
                                    publiceService.sendSms(vo.getStoreId(), mchPhone, GloabConst.VcodeCategory.TYPE_NOTICE, vo.getType(), smsParmaMap);
                                }
                                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TKSB, "退款失败", "refund");
                            }
                            isExamine = true;
                            break;
                        case DictionaryConst.OrderPayType.ORDERPAYTYPE_APP_WECHAT:
                        case DictionaryConst.OrderPayType.ORDERPAYTYPE_MINI_WECHAT:
                        case DictionaryConst.OrderPayType.ORDERPAYTYPE_PC_WECHAT:
                        case DictionaryConst.OrderPayType.ORDERPAYTYPE_H5_WECHAT:
                        case DictionaryConst.OrderPayType.ORDERPAYTYPE_JSAPI_WECHAT:
                            //微信退款
                            logger.info("退款参数-{}-,", JSON.toJSONString(vo));
                            logger.info("退款参数orderId-{},", orderModel.getId());
                            logger.info("退款参数payType-{}-,", model.getPay());
                            logger.info("退款参数realOrderno-{},", model.getReal_sno());
                            map = publicWechatServiceImpl.refundOrder(vo.getStoreId(), orderModel.getId(), model.getPay(), model.getReal_sno(), vo.getPrice());
//                            Map<String, Object> dividendRefundMap = publicWechatServiceImpl.dividendRefund(orderno);
//                            logger.info("#########微信分账回退######## {}", JSONObject.toJSONString(dividendRefundMap));
                            logger.info("#########退款返回##### {}", JSONObject.toJSONString(map));
                            isExamine = true;
                            break;
                        default:
                            isExamine = false;
                            break;
                    }
                    model.setIs_refund(1);
                    model.setPrice(BigDecimal.ZERO);
                    preSellRecordModelMapper.updateByPrimaryKeySelective(model);
                }
            }
            //h5 pc商城消息通知
            SystemMessageModel systemMessageSave = new SystemMessageModel();
            systemMessageSave.setType(SystemMessageModel.ReadType.UNREAD);
            systemMessageSave.setSenderid("admin");
            systemMessageSave.setStore_id(orderModel.getStore_id());
            systemMessageSave.setRecipientid(orderModel.getUser_id());
            systemMessageSave.setTitle("系统消息");
            systemMessageSave.setContent("您的预售定金已退回！");
            systemMessageSave.setTime(new Date());
            systemMessageModelMapper.insertSelective(systemMessageSave);
        }
        catch (Exception e)
        {
            logger.error("预售订单退款 异常" + e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "returnAmt");
        }
        return isExamine;
    }

    @Autowired
    private SystemMessageModelMapper systemMessageModelMapper;
}
