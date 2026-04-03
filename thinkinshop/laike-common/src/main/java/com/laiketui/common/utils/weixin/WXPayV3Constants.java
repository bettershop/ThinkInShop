package com.laiketui.common.utils.weixin;

/**
 * @Author: sunH_
 * @Date: Create in 19:42 2023/9/7
 */
public class WXPayV3Constants
{
    //根路径
    public static final String DOMAIN_API = "https://api.mch.weixin.qq.com/v3";

    //app下单
    public static final String PAY_TRANSACTIONS_APP = "/pay/transactions/app";

    //小程序下单
    public static final String PAY_TRANSACTIONS_JSAPI = "/pay/transactions/jsapi";

    //手机h5下单
    public static final String PAY_TRANSACTIONS_H5 = "/pay/transactions/h5";

    public static final String PAY_TRANSACTIONS_PC = "/pay/transactions/native";

    //微信支付回调
    public static final String WECHAT_PAY_NOTIFY_URL = "http://java.houjiemeishi.com/payment/v2/weChatNotify";

    //申请退款
    public static final String REFUND_DOMESTIC_REFUNDS = "/refund/domestic/refunds";

    //微信退款回调
    public static final String WECHAT_REFUNDS_NOTIFY_URL = "https://java.houjiemeishi.com/wx_notify";

    //关闭订单
    public static final String PAY_TRANSACTIONS_OUT_TRADE_NO = "/pay/transactions/out-trade-no/{}/close";

    //请求分账API
    public static final String REQUEST_FOR_LEDGER_ALLOCATION = "https://api.mch.weixin.qq.com/v3/profitsharing/orders";

    //请求分账回退API
    public static final String REQUEST_FOR_DISTRIBUTION_AND_REFUND = "https://api.mch.weixin.qq.com/v3/profitsharing/return-orders";

    //查询剩余待分金额API
    public static final String QUERY_THE_REMAINING_AMOUNT_TO_BE_DIVIDED = "https://api.mch.weixin.qq.com/v3/profitsharing/transactions/{transaction_id}/amounts";
}
