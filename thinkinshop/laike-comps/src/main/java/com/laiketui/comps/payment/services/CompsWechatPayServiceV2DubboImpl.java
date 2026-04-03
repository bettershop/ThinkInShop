package com.laiketui.comps.payment.services;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import com.google.common.collect.Maps;
import com.laiketui.common.api.PublicOrderService;
import com.laiketui.common.api.PubliceService;
import com.laiketui.common.mapper.*;
import com.laiketui.common.utils.HttpApiUtils;
import com.laiketui.common.utils.tool.cache.RedisDataTool;
import com.laiketui.common.utils.weixin.AppletUtil;
import com.laiketui.comps.api.payment.CompsWechatPayService;
import com.laiketui.core.cache.RedisUtil;
import com.laiketui.core.config.wechatpay.WechatConfigInfo;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.DictionaryConst;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.core.utils.tool.DataUtils;
import com.laiketui.core.utils.tool.DateUtil;
import com.laiketui.core.utils.tool.IpUtil;
import com.laiketui.core.utils.tool.WxOpenIdUtils;
import com.laiketui.domain.config.ConfigModel;
import com.laiketui.domain.message.TemplateData;
import com.laiketui.domain.order.NoticeModel;
import com.laiketui.domain.order.OrderDataModel;
import com.laiketui.domain.order.OrderDetailsModel;
import com.laiketui.domain.order.OrderModel;
import com.laiketui.domain.presell.PreSellGoodsModel;
import com.laiketui.domain.presell.PreSellRecordModel;
import com.laiketui.domain.user.User;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.OrderVo;
import com.laiketui.domain.vo.pay.PaymentVo;
import github.wxpay.sdk.WXPay;
import github.wxpay.sdk.WXPayConstants;
import github.wxpay.sdk.WXPayUtil;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.*;

import static com.laiketui.core.lktconst.DictionaryConst.OrderPayType.*;
import static com.laiketui.core.lktconst.DictionaryConst.OrdersType.*;
import static com.laiketui.core.lktconst.ErrorCode.BizErrorCode.API_OPERATION_FAILED;
import static com.laiketui.core.lktconst.ErrorCode.BizErrorCode.ERROR_CODE_YFKQECFZF;

/**
 * 微信支付版本 v2 实现
 *
 * @author wangxian
 */
@Service("compsWechatPayServiceV2DubboImpl")
@RefreshScope
public class CompsWechatPayServiceV2DubboImpl extends CompsPayServiceAdapter implements CompsWechatPayService
{

    private final Logger logger = LoggerFactory.getLogger(CompsWechatPayServiceV2DubboImpl.class);

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private PaymentConfigModelMapper paymentConfigModelMapper;

    @Autowired
    private OrderModelMapper orderModelMapper;

    @Autowired
    private OrderDetailsModelMapper orderDetailsModelMapper;

    @Autowired
    private OrderDataModelMapper orderDataModelMapper;

    @Autowired
    private PublicOrderService publicOrderService;

    @Autowired
    private ConfigModelMapper configModelMapper;

    @Autowired
    private NoticeModelMapper noticeModelMapper;

    @Autowired
    private PreSellRecordModelMapper preSellRecordModelMapper;

    @Autowired
    private PreSellGoodsMapper preSellGoodsMapper;

    @Autowired
    private UserBaseMapper userBaseMapper;

    @Value("${node.wx-certp12-path}")
    private String path;

    @Autowired
    private PubliceService publiceService;

    @Autowired
    private HttpApiUtils httpApiUtils;

    @Override
    public Map<String, Object> wechatAppPay(Map params) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>();
        try
        {

        }
        catch (Exception e)
        {
            logger.error("微信支付失败", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.API_OPERATION_FAILED, "微信支付失败", "wechatJsapiPay");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> weChatWapPay(Map params) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>();
        try
        {

        }
        catch (Exception e)
        {
            logger.error("微信支付失败", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.API_OPERATION_FAILED, "微信支付失败", "wechatJsapiPay");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> wechatPcQrPay(Map params) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>();
        try
        {

        }
        catch (Exception e)
        {
            logger.error("微信支付失败", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.API_OPERATION_FAILED, "微信支付失败", "wechatJsapiPay");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> wechatMiniPay(Map params) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>();
        try
        {

        }
        catch (Exception e)
        {
            logger.error("微信支付失败", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.API_OPERATION_FAILED, "微信支付失败", "wechatJsapiPay");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> wechatJsapiPay(Map params) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>();
        try
        {

        }
        catch (Exception e)
        {
            logger.error("微信支付失败", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.API_OPERATION_FAILED, "微信支付失败", "wechatJsapiPay");
        }
        return resultMap;
    }

    /**
     * 此方法在这里做分发
     *
     * @param params
     * @return
     * @throws LaiKeAPIException
     */

    @Transactional
    @Override
    public Map<String, Object> pay(Map params) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = Maps.newHashMap();
        try
        {
            logger.info("微信v2支付");
            PaymentVo paymentVo = (PaymentVo) params.get("paymentVo");
            User      user      = null;
            if (GloabConst.StoreType.STORE_TYPE_PC_MALL == paymentVo.getStoreType())
            {
                user = RedisDataTool.getRedisUserCache(paymentVo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_PC_SHOP_TOKEN, true);
            }
            else
            {
                user = RedisDataTool.getRedisUserCache(paymentVo.getAccessId(), redisUtil, true);
            }
            logger.info("传入信息：{}", params);
            logger.info("传入信息PaymentVo：{}", JSON.toJSONString(paymentVo));
            // 参数
            String     orderList  = paymentVo.getOrder_list();
            JSONObject jsonObject = JSONObject.parseObject(orderList);
            // 订单sNo
            String sNo;
            if (!StringUtils.isEmpty(paymentVo.getsNo()))
            {
                sNo = paymentVo.getsNo();
            }
            else
            {
                sNo = jsonObject.getString("sNo");
            }

            if (StringUtils.isEmpty(sNo))
            {
                throw new LaiKeAPIException(API_OPERATION_FAILED, "支付失败", "pay");
            }
            //获取订单信息
            OrderModel       orderModel = publicOrderService.getOrderInfo(sNo, paymentVo, user.getUser_id());
            WechatConfigInfo config     = getWechatConfigInfo(paymentVo);
            WXPay            wxpay      = new WXPay(config);
            //查看微信订单信息（返回结果示例）
            /*<xml>
            <return_code><![CDATA[SUCCESS]]></return_code>
            <return_msg><![CDATA[OK]]></return_msg>
            <result_code><![CDATA[SUCCESS]]></result_code>
            <mch_id><![CDATA[1516978921]]></mch_id>
            <appid><![CDATA[wx441f3b338ac30350]]></appid>
            <device_info><![CDATA[]]></device_info>
            <trade_state><![CDATA[NOTPAY]]></trade_state>
            <total_fee>1</total_fee>
            <out_trade_no><![CDATA[PS230911142035506610]]></out_trade_no>
            <trade_state_desc><![CDATA[订单未支付]]></trade_state_desc>
            <nonce_str><![CDATA[VuiOJC5YH94H861V]]></nonce_str>
            <sign><![CDATA[E572BB4FCB140458A2720E1F5317621B]]></sign>
            </xml>*/
            Map<String, String> queryMap = Maps.newHashMap();
            queryMap.put("appid", config.getAppID());
            queryMap.put("mch_id", config.getMchID());
            queryMap.put("out_trade_no", orderModel.getReal_sno());
            queryMap.put("nonce_str", String.valueOf(System.currentTimeMillis()));
            String querySign = WXPayUtil.generateSignature(queryMap, config.getKey(), WXPayConstants.SignType.MD5);
            queryMap.put("sign", querySign);
            Map<String, String> map = wxpay.orderQuery(queryMap);
            //查看订单支付前的状态
            logger.info("支付前订单号[" + orderModel.getsNo() + "],支付订单号[" + orderModel.getReal_sno() + "] 微信返回的状态结果：", JSON.toJSONString(map));
            if ("SUCCESS".equals(MapUtils.getString(map, "result_code")) && !"NOTPAY".equals(MapUtils.getString(map, "trade_state")))
            {
                //没有支付的情况下更换支付订单号，重新拉支付
                String otype     = orderModel.getOtype();
                String orderType = ORDERS_HEADER_GM;
                switch (otype)
                {
                    //对应的插件订单类型不能错 错了回调那边会有问题
                    case ORDERS_HEADER_JP:
                        orderType = ORDERS_HEADER_JP;
                        break;
                    case ORDERS_HEADER_FS:
                        orderType = ORDERS_HEADER_FS;
                        break;
                    case ORDERS_HEADER_FX:
                        orderType = ORDERS_HEADER_FX;
                        break;
                    case ORDERS_HEADER_IN:
                        orderType = ORDERS_HEADER_IN;
                        break;
                    case ORDERS_HEADER_MS:
                        orderType = ORDERS_HEADER_MS;
                        break;
                    case ORDERS_HEADER_VI:
                        orderType = ORDERS_HEADER_VI;
                        break;
                    case ORDERS_HEADER_PS:
                        orderType = ORDERS_HEADER_PS;
                        break;
                    case ORDERS_HEADER_TH:
                        orderType = ORDERS_HEADER_TH;
                        break;
                    case ORDERS_HEADER_PT:
                        orderType = ORDERS_HEADER_PT;
                        break;
                    case ORDERS_HEADER_AC:
                        orderType = ORDERS_HEADER_AC;
                        break;
                    case ORDERS_HEADER_ZB:
                        orderType = ORDERS_HEADER_ZB;
                        break;
                    default:
                        orderType = ORDERS_HEADER_GM;
                        break;
                }
                orderModel.setReal_sno(publicOrderService.createOrderNo(orderType));
                orderModelMapper.updateByPrimaryKeySelective(orderModel);
            }
            else if ("SUCCESS".equals(MapUtils.getString(map, "result_code")) && "SUCCESS".equals(MapUtils.getString(map, "trade_state")))
            {
                //重复支付问题
                throw new LaiKeAPIException(ERROR_CODE_YFKQECFZF, "请勿重复支付!", "pay");
            }
            Map<String, String> data = Maps.newHashMap();
            data.put("body", paymentVo.getTitle());
            data.put("out_trade_no", orderModel.getReal_sno());
            data.put("device_info", "");
            data.put("fee_type", "CNY");
            data.put("total_fee", String.valueOf(orderModel.getZ_price().multiply(new BigDecimal(100)).intValue()));
            //获取ip地址
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            data.put("spbill_create_ip", IpUtil.getIpAddr(request));
            data.put("notify_url", config.getNotifyUrl());
            //支付类型
            String payClass  = paymentVo.getType();
            String appId     = config.getAppID();
            String appSecret = config.getAppSecreKey();
            String code      = paymentVo.getCode();
            switch (payClass)
            {
                case ORDERPAYTYPE_MINI_WECHAT:
                    data.put("trade_type", "JSAPI");
                    if (StringUtils.isNotEmpty(paymentVo.getOpenid()))
                    {
                        data.put("openid", paymentVo.getOpenid());
                    }
                    else
                    {
                        data.put("openid", WxOpenIdUtils.getMiniOpenid(appId, appSecret, code));
                    }
                    break;
                case ORDERPAYTYPE_JSAPI_WECHAT:
                    data.put("trade_type", "JSAPI");
                    data.put("openid", WxOpenIdUtils.getGzhOpenid(appId, appSecret, code));
                    break;
                case ORDERPAYTYPE_PC_WECHAT:
                    data.put("trade_type", "NATIVE");
                    break;
                case ORDERPAYTYPE_H5_WECHAT:
//                    data.put("body", "开通会员");
                    data.put("device_info", "WEB");
                    Map h5_info = Maps.newConcurrentMap();
                    h5_info.put("type", "Wap");
                    h5_info.put("wap_url", "https://java.houjiemeishi.com");
                    h5_info.put("wap_name", "微信H5手机支付");
                    Map sceneInfoMap = Maps.newConcurrentMap();
                    sceneInfoMap.put("h5_info", JSONObject.toJSONString(h5_info));
                    data.put("scene_info", JSONObject.toJSONString(sceneInfoMap));
                    data.put("trade_type", "MWEB");
//                    data.put("goods_name", "开通会员");
                    break;
                default:
                    data.put("trade_type", "APP");
                    break;
            }
            logger.info("微信支付请求参数{}", data);

            //是否分账 微信公众号支付
            /* if (getISAcounts(orderModel.getStore_id()) == GloabConst.DivAccountStatus.YES)
            {
                data.put("profit_sharing", "Y");
            }*/
            Map<String, String> payResult = wxpay.unifiedOrder(data);
            logger.info("微信支付 类型：{},结果结果:{}", payClass, JSONObject.toJSONString(payResult));
            String resultCode = MapUtils.getString(payResult, "result_code");
            String returnCode = MapUtils.getString(payResult, "return_code");
            if ("SUCCESS".equalsIgnoreCase(resultCode) && "SUCCESS".equalsIgnoreCase(returnCode))
            {
                Map<String, String> payData = new HashMap<>();
                String              time    = String.valueOf((System.currentTimeMillis() / 1000));
                switch (payClass)
                {
                    case ORDERPAYTYPE_APP_WECHAT:
                        payData.put("appid", MapUtils.getString(payResult, "appid"));
                        payData.put("partnerid", MapUtils.getString(payResult, "mch_id"));
                        payData.put("prepayid", MapUtils.getString(payResult, "prepay_id"));
                        payData.put("package", "Sign=WXPay");
                        payData.put("noncestr", MapUtils.getString(payResult, "nonce_str"));
                        payData.put("timestamp", time);
                        String sign1 = WXPayUtil.generateSignature(payData, config.getKey(), WXPayConstants.SignType.MD5);
                        payData = new HashMap<>();
                        payData.put("sign", sign1);
                        payData.put("appid", config.getAppID());
                        payData.put("timestamp", String.valueOf(WXPayUtil.getCurrentTimestamp()));
                        payData.put("noncestr", MapUtils.getString(payResult, "nonce_str"));
                        payData.put("partnerid", config.getMchID());
                        payData.put("prepayid", MapUtils.getString(payResult, "prepay_id"));
                        payData.put("package", "Sign=WXPay");

                        break;
                    case ORDERPAYTYPE_PC_WECHAT:
                    case ORDERPAYTYPE_MINI_WECHAT:
                    case ORDERPAYTYPE_JSAPI_WECHAT:
                        Map<String, String> tmp = new HashMap<>();
                        tmp.put("appId", MapUtils.getString(payResult, "appid"));
                        tmp.put("nonceStr", MapUtils.getString(payResult, "nonce_str"));
                        tmp.put("signType", "MD5");
                        tmp.put("package", "prepay_id=" + MapUtils.getString(payResult, "prepay_id"));
                        tmp.put("timeStamp", time);
                        String sign = WXPayUtil.generateSignature(tmp, config.getKey(), WXPayConstants.SignType.MD5);
                        //禅道 56681 微信小程序支付timestamp错误
                        payData.put("timeStamp", time);
                        payData.put("state", String.valueOf(1));
                        payData.put("appid", MapUtils.getString(payResult, "appid"));
                        payData.put("nonceStr", MapUtils.getString(payResult, "nonce_str"));
                        payData.put("signType", "MD5");
                        payData.put("package", "prepay_id=" + MapUtils.getString(payResult, "prepay_id"));
                        payData.put("paySign", sign);
                        payData.put("out_trade_no", orderModel.getReal_sno());
                        String codeUrl = MapUtils.getString(payResult, "code_url");
                        if (StringUtils.isNotEmpty(codeUrl))
                        {
                            payData.put("code_url", codeUrl);
                        }
                        break;
                    case ORDERPAYTYPE_H5_WECHAT:
                        StringBuilder sb = new StringBuilder(MapUtils.getString(payResult, "mweb_url"));
                        logger.error("微信mweb_url支付跳转链接-----------{}", sb);
                        String h5Url = getH5Url(orderModel.getStore_id()) + "pages/pay/payResult?sNo=" + orderModel.getReal_sno() + "&payment_money=" + orderModel.getZ_price() + "&isH5=true";
                        String gbk = URLEncoder.encode(h5Url, "GBK");
                        logger.error("url编码后-------------{}", sb + "&redirect_url=" + gbk);
                        payData.put("url", sb + "&redirect_url=" + gbk);
//                        payData.put("url", sb.toString());
                        payData.put("pay_type", payClass);
                        payData.put("prepayid", MapUtils.getString(payResult, "prepay_id"));
                        break;
                    default:
                        break;
                }
                resultMap.put("data", payData);
                resultMap.put("code", 200);
                resultMap.put("message", "支付成功");
                logger.error("============================================微信唤醒支付成功");

                //缓存是否分账
                /*if (getISAcounts(orderModel.getStore_id()) == GloabConst.DivAccountStatus.YES && payClass.equals(ORDERPAYTYPE_MINI_WECHAT))
                {
                    redisUtil.set(GloabConst.RedisHeaderKey.ORDER_IS_FZ_KEY + orderModel.getsNo(), "Y");
                }*/
            }
            else
            {
                resultMap.put("code", 500);
                resultMap.put("message", "支付失败");
            }

        }
        catch (LaiKeAPIException l)
        {
            logger.error("微信支付失败", l);
            throw l;
        }
        catch (Exception e)
        {
            logger.error("微信支付 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.API_OPERATION_FAILED, "微信支付失败", "wechatJsapiPay");
        }
        return resultMap;
    }

    /**
     * 获取H5地址
     *
     * @param storeId
     * @return
     */
    public String getH5Url(int storeId)
    {
        String h5Url = "";
        try
        {
            ConfigModel configModel = new ConfigModel();
            configModel.setStore_id(storeId);
            configModel = configModelMapper.selectOne(configModel);
            h5Url = configModel.getH5_domain();
        }
        catch (LaiKeAPIException e)
        {
            throw e;
        }
        return h5Url;
    }

    /**
     * 获取是否分账的开关 1 分账 0 不分账
     *
     * @param storeId
     * @return
     */
    public int getISAcounts(int storeId)
    {
        Integer isAccounts = 0;
        try
        {
            ConfigModel configModel = new ConfigModel();
            configModel.setStore_id(storeId);
            configModel = configModelMapper.selectOne(configModel);
            if (configModel != null)
            {
                isAccounts = Objects.nonNull(configModel.getIsAccounts()) ? configModel.getIsAccounts() : isAccounts;
            }
        }
        catch (Exception e)
        {
            logger.error(e.getMessage());
            return 0;
        }
        return isAccounts;
    }


    /**
     * 退款 场景：刷卡支付、公共号支付、扫码支付、APP支付、h5（可能不支持，待测）
     *
     * @param params
     * @return
     * @throws LaiKeAPIException
     */
    @Override
    public Map<String, Object> refund(Map params) throws LaiKeAPIException
    {
        Map resultMap = new HashMap<>(16);
        try
        {
            PaymentVo        paymentVo = (PaymentVo) params.get("paymentVo");
            WechatConfigInfo config    = getWechatConfigInfo(paymentVo);
            WXPay            wxpay     = new WXPay(config);
            resultMap = wxpay.refund(params);
        }
        catch (Exception e)
        {
            logger.error("微信退款失败", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.API_OPERATION_FAILED, "微信退款失败", "refund");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> refund(int storeId, Integer oid, BigDecimal refundAmt, boolean isTempOrder) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            logger.info(">>微信退款开始>>");
            PaymentVo        paymentVo = new PaymentVo();
            WechatConfigInfo config    = getWechatConfigInfo(paymentVo);
            WXPay            wxpay     = new WXPay(config);
            //获取商户支付配置信息
            Map<String, String> paramsMap = new HashMap<>(16);
            paramsMap.put("appid", config.getAppID());
            paramsMap.put("mch_id", config.getMchID());
            //商户订单号
            paramsMap.put("out_trade_no", paymentVo.getsNo());
            //商户退款单号
            paramsMap.put("out_refund_no", paymentVo.getsNo().concat(String.valueOf(System.currentTimeMillis())));
            //订单金额
            OrderModel orderModel;
            if (!isTempOrder)
            {
                orderModel = orderModelMapper.selectByPrimaryKey(oid);
            }
            else
            {
                orderModel = new OrderModel();
                //获取临时订单
                OrderDataModel orderDataModel = orderDataModelMapper.selectByPrimaryKey(oid);
                Map<String, Object> dataMap = JSON.parseObject(orderDataModel.getData(), new TypeReference<Map<String, Object>>()
                {
                });
                if (dataMap == null)
                {
                    logger.error(">>微信退款失败 临时订单id:{} 未找到>>", oid);
                    resultMap.put("code", GloabConst.ManaValue.MANA_VALUE_FAIL);
                    return resultMap;
                }
                orderModel.setsNo(orderDataModel.getTrade_no());
                orderModel.setZ_price(new BigDecimal(MapUtils.getString(dataMap, "paymentAmt")));
                orderModel.setUser_id(MapUtils.getString(dataMap, "user_id"));
            }
            //微信金额单位分处理
            BigDecimal yb = new BigDecimal("100");
            paramsMap.put("total_fee", String.valueOf(orderModel.getZ_price().multiply(yb).intValue()));
            //退款金额
            paramsMap.put("refund_fee", String.valueOf(refundAmt.multiply(yb).intValue()));
            Map<String, String> resultMap1 = wxpay.refund(paramsMap);
            logger.info("#########退款信息#########start####");
            logger.info("退款信息，{}", JSONObject.toJSONString(resultMap1));
            logger.info("#########退款信息#########end######");
            //微信请求退款失败
            if (!"SUCCESS".equals(MapUtils.getString(resultMap1, "result_code")) || !"SUCCESS".equals(MapUtils.getString(resultMap1, "return_code")))
            {
                logger.info("退款错误信息，{}", MapUtils.getString(resultMap1, "err_code_des"));
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CZSB, MapUtils.getString(resultMap1, "err_code_des"), "refundOrder");
            }
            resultMap.put("code", GloabConst.ManaValue.MANA_VALUE_SUCCESS);
            //发送模板消息
            NoticeModel noticeModel = new NoticeModel();
            noticeModel.setStore_id(paymentVo.getStoreId());
            noticeModel = noticeModelMapper.selectOne(noticeModel);
            if (Objects.isNull(noticeModel))
            {
                logger.debug("该商城id{}暂无微信推送模板", paymentVo.getStoreId());
            }
            //当前用户信息
            User userEntity = new User();
            userEntity.setUser_id(orderModel.getUser_id());
            User user = userBaseMapper.selectOne(userEntity);
            //发送通知
            if (noticeModel != null)
            {
//                String response = AppletUtil.sendMessage(publiceService.getWeiXinToken(storeId), user.getWx_id(), noticeModel.getPay_success());
//                logger.error("===================微信消息推送返回值：{}", response);
            }
        }
        catch (Exception e)
        {
            logger.error("微信退款失败", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.API_OPERATION_FAILED, "微信退款失败", "refund");
        }
        return resultMap;
    }

    @Transactional
    @Override
    public Map<String, Object> payBack(Map params) throws LaiKeAPIException
    {
        Map resultMap = Maps.newHashMap();
        try
        {
            logger.info("v2支付回调");
            int storeId = 0;
            //微信推送标题
            String title = "";
            logger.info("payBack-start-params-{}", JSON.toJSONString(params));
            String sNo = null;
            storeId = MapUtils.getInteger(params, "storeId");
            String    notifyData = MapUtils.getString(params, "notifyData");
            PaymentVo paymentVo  = (PaymentVo) params.get("paymentVo");
            int       orderId    = paymentVo.getOrder_id();
            logger.info("paymentVo-{}", JSON.toJSONString(paymentVo));
            WechatConfigInfo    config    = getWechatConfigInfo(paymentVo);
            WXPay               wxpay     = new WXPay(config);
            Map<String, String> notifyMap = WXPayUtil.xmlToMap(notifyData);
            logger.info("回调notifyMap-{}", JSON.toJSONString(notifyMap));
            if (wxpay.isPayResultNotifySignatureValid(notifyMap))
            {
                logger.info("回调notifyMap-{}", JSON.toJSONString(notifyMap));
                // 注意特殊情况：订单已经退款，但收到了支付结果成功的通知，不应把商户侧订单状态从退款改成支付成功
                String payNo = DataUtils.getStringVal(params, "payNo");
                logger.error("payNo:" + payNo);
                String     transaction_id = MapUtils.getString(notifyMap, "transaction_id");
                BigDecimal total          = DataUtils.getBigDecimalVal(params, "total");
                if (StringUtils.isEmpty(payNo) || total.compareTo(BigDecimal.ZERO) <= 0)
                {
                    logger.error("普通订单回调失败信息 订单：{},支付金额：{}", payNo, total);
                    resultMap.put("code", ErrorCode.BizErrorCode.API_OPERATION_FAILED);
                    resultMap.put("message", "error");
                    return resultMap;
                }
                //根据调起支付所用订单号获取主订单号(未支付先拆单，子订单调起支付所用订单号和总订单相同)
                sNo = orderModelMapper.getOrderByRealSno(payNo);
                logger.info("根据吊起支付订单号获取主订单号:{}", sNo);
                //根据调起支付所用订单号获取订单号，先拆单后支付
                if (StringUtils.isEmpty(sNo))
                {
                    sNo = orderModelMapper.getOrdersNoByRealSno(payNo);
                    logger.info("先拆单后支付:{}", sNo);
                }
                //钱包充值sNo = null
                if (sNo == null)
                {
                    sNo = payNo;
                }
                OrderModel orderModel = new OrderModel();
                orderModel.setsNo(sNo);
                orderModel = orderModelMapper.selectOne(orderModel);
                logger.info("微信版本2回调订单处理");
                if (payNo.contains(DictionaryConst.OrdersType.ORDERS_HEADER_CZ))
                {
                    OrderDataModel orderDataModel = new OrderDataModel();
                    orderDataModel.setTrade_no(payNo);
                    orderDataModel = orderDataModelMapper.selectOne(orderDataModel);
                    /**更新订单*/
                    resultMap = publicOrderService.payBackUpOrderMember(orderDataModel);
                    title = "充值";
                }
                else if (payNo.contains(DictionaryConst.OrdersType.ORDERS_HEADER_DJ))
                {
                    OrderDataModel orderDataModel = new OrderDataModel();
                    orderDataModel.setTrade_no(payNo);
                    orderDataModel = orderDataModelMapper.selectOne(orderDataModel);
                    /**更新订单*/
                    resultMap = publicOrderService.payBackForMember(orderDataModel);
                    title = "办理会员";
                }
                else if (payNo.contains(DictionaryConst.OrdersType.ORDERS_HEADER_PS))
                {
                    //预售订单信息
                    PreSellRecordModel preSellRecordModel = new PreSellRecordModel();
                    preSellRecordModel.setReal_sno(payNo);
                    preSellRecordModel = preSellRecordModelMapper.selectOne(preSellRecordModel);
                    //主订单信息
                    OrderModel preSellOrder = new OrderModel();
                    preSellOrder.setsNo(preSellRecordModel.getsNo());
                    preSellOrder = orderModelMapper.selectOne(preSellOrder);
                    //商品信息
                    PreSellGoodsModel preSellGoodsModel = new PreSellGoodsModel();
                    preSellGoodsModel.setProduct_id(preSellRecordModel.getProduct_id());
                    preSellGoodsModel = preSellGoodsMapper.selectOne(preSellGoodsModel);
                    //只有支付定金时需要用到
                    paymentVo.setPayment_money(total.divide(new BigDecimal(100)).toString());
                    if (preSellRecordModel.getPay_type() == null)
                    {
                        paymentVo.setPayTarget(3);
                    }
                    else if (preSellRecordModel.getPay_type().equals(PreSellRecordModel.BALANCE))
                    {
                        paymentVo.setPayTarget(2);
                    }
                    else
                    {
                        preSellOrder = JSON.parseObject(preSellRecordModel.getOrder_info(), OrderModel.class);
                        paymentVo.setPayTarget(1);
                    }
                    resultMap = publicOrderService.payBackForPreSell(paymentVo, preSellOrder);
                    title = preSellGoodsModel.getProduct_title();
                }
                else if (payNo.contains(DictionaryConst.OrdersType.ORDERS_HEADER_MCH_PROMISE))
                {
                    OrderDataModel orderDataModel = new OrderDataModel();
                    orderDataModel.setTrade_no(payNo);
                    orderDataModel = orderDataModelMapper.selectOne(orderDataModel);
                    resultMap = publicOrderService.payBackUpOrderMchPromise(orderDataModel);
                    title = "店铺保证金";
                }
                else if (payNo.contains(DictionaryConst.OrdersType.ORDERS_HEADER_JB))
                {
                    OrderDataModel orderDataModel = new OrderDataModel();
                    orderDataModel.setTrade_no(payNo);
                    orderDataModel = orderDataModelMapper.selectOne(orderDataModel);
                    resultMap = publicOrderService.payBackUpOrderAuctionPromise(orderDataModel);
                    title = "竞拍保证金";
                }
                else
                {
                    //是否有内部api参数,如果有则调用内部实现 add by trick 2022-08-09 17:32:34
                    Map<String, Object> paramMap = new HashMap<>(1);
                    try
                    {
                        paramMap = JSON.parseObject(paymentVo.getParameter(), new TypeReference<Map<String, Object>>()
                        {
                        });
                    }
                    catch (JSONException ignored)
                    {
                        logger.error("微信回调parameter参数错误:{}", paymentVo.getParameter());
                    }
                    String laikeApiUrl = MapUtils.getString(paramMap, "laikeApi");
                    if (StringUtils.isNotEmpty(laikeApiUrl))
                    {
                        logger.debug("订单{}支付成功回调 执行接口:{}", paymentVo.getsNo(), laikeApiUrl);
                        //region 是否有内部api参数,如果有则调用内部实现(用于支付后下单场景) add by trick 2023-04-24 11:02:06
                        //临时订单转变插件订单-内部接口不要暴露到外网
                        if (paymentVo.getsNo().contains(DictionaryConst.OrdersType.ORDERS_HEADER_PT))
                        {
                            //获取临时表订单数据
                            OrderDataModel orderDataModel = new OrderDataModel();
                            orderDataModel.setPay_type(paymentVo.getPayType());
                            orderDataModel.setTrade_no(paymentVo.getsNo());
                            orderDataModel = orderDataModelMapper.selectOne(orderDataModel);
                            if (orderDataModel == null)
                            {
                                throw new LaiKeAPIException(ErrorCode.BizErrorCode.API_OPERATION_FAILED, "微信支付失败", "wechatJsapiPay");
                            }
                            //下单
                            OrderVo orderVo = new OrderVo();
                            orderVo.setStoreId(storeId);
                            orderVo.setUserId(paymentVo.getUserId());
                            orderVo.setStoreType(paymentVo.getStoreType());
                            orderVo.setProductsInfo(orderDataModel.getData());
                            //支付订单
                            orderVo.setRealSno(paymentVo.getsNo());
                            orderVo.setPayType(paymentVo.getPayType());
                            Map<String, Object> paramApiMap = JSON.parseObject(JSON.toJSONString(orderVo));

                            Map<String, Object> resultMapOrder = httpApiUtils.executeHttpApi("plugin.group.app.payment", paramApiMap, MediaType.MULTIPART_FORM_DATA_VALUE);
                            logger.info("plugin.group.app.payment远程调用返回值: " + JSON.toJSONString(resultMapOrder));
                            paymentVo.setsNo(MapUtils.getString(resultMapOrder, "sNo"));

                            //标记临时订单已支付
                            OrderDataModel orderDataUpdate = new OrderDataModel();
                            orderDataUpdate.setId(orderDataModel.getId());
                            orderDataUpdate.setStatus(DictionaryConst.WhetherMaven.WHETHER_OK);
                            orderDataModelMapper.updateByPrimaryKeySelective(orderDataUpdate);
                            logger.error("临时开团订单已更新");
                        }
                        //endregion

                        logger.error("正在支付订单{} 执行接口:{}", payNo, laikeApiUrl);
                        Map<String, Object> paramMap1 = new HashMap<>();
                        logger.error("paymentVo值:{}", JSON.toJSONString(paymentVo));
                        paramMap1.put("vo", JSON.toJSONString(paymentVo));
                        logger.error("paramJson值:{}", paramMap1);
                        resultMap = httpApiUtils.executeHttpApi(laikeApiUrl, paramMap1, MediaType.MULTIPART_FORM_DATA_VALUE);
                        logger.error("支付回调成功 返回数据{}", JSON.toJSONString(resultMap));
                    }
                    else
                    {
                        if (orderModel != null)
                        {
                            /**更新订单*/
                            //分账代码
                            String transactionId = MapUtils.getString(params, "transactionId");
                            orderModel.setTransaction_id(transactionId);
                            Object fzFlag = redisUtil.get(GloabConst.RedisHeaderKey.ORDER_IS_FZ_KEY + sNo);
                            if (fzFlag != null && StringUtils.isNotEmpty(fzFlag.toString()))
                            {
                                if ("Y".equals(redisUtil.get(GloabConst.RedisHeaderKey.ORDER_IS_FZ_KEY + sNo).toString()))
                                {
                                    logger.info("获取redis缓存数据 返回数据{}", redisUtil.get(GloabConst.RedisHeaderKey.ORDER_IS_FZ_KEY + sNo).toString());
                                    orderModel.setDividend_status(1);
                                }
                            }
                            else
                            {
                                orderModel.setDividend_status(0);
                            }
                            resultMap = publicOrderService.payBackUpOrder(orderModel);
                            storeId = orderModel.getStore_id();
                            OrderDetailsModel orderDetailsModel = new OrderDetailsModel();
                            orderDetailsModel.setStore_id(storeId);
                            orderDetailsModel.setR_sNo(orderModel.getsNo());
                            List<OrderDetailsModel> select = orderDetailsModelMapper.select(orderDetailsModel);
                            if (select.size() > 0)
                            {
                                OrderDetailsModel orderDetail = select.get(0);
                                title = orderDetail.getP_name();
                                if (select.size() > 1)
                                {
                                    title = title + "等商品";
                                }
                            }
                        }
                    }
                }
                //发送模板消息
                NoticeModel noticeModel = new NoticeModel();
                noticeModel.setStore_id(paymentVo.getStoreId());
                noticeModel = noticeModelMapper.selectOne(noticeModel);
                if (Objects.isNull(noticeModel))
                {
                    logger.debug("该商城id{}暂无微信推送模板", paymentVo.getStoreId());
                }
                //发起人openid
                String openId = MapUtils.getString(params, "openId");
                if (StringUtils.isEmpty(openId))
                {
                    openId = MapUtils.getString(params, "openid");
                }
                logger.error("openId: {}", openId);
                //发送通知
                if (noticeModel != null && StringUtils.isNotEmpty(openId))
                {
                    //获取token
                    String accessToken = publiceService.getWeiXinToken(storeId);
                    logger.error("accessToken: {}", accessToken);
                    Map<String, Object> map = new HashMap<>(16);
                    map.put("character_string1", new TemplateData(payNo));
                    map.put("thing3", new TemplateData(title));
                    map.put("amount4", new TemplateData(String.valueOf(total.divide(new BigDecimal(100)))));
                    map.put("date2", new TemplateData(DateUtil.dateFormate(new Date(), GloabConst.TimePattern.YMDHMS)));
                    logger.error("=================微信消息推送data请求值：{}", JSON.toJSONString(map));
                    String response = AppletUtil.sendMessage(accessToken, openId, noticeModel.getPay_success(), map);
                    logger.error("=================微信消息推送返回值：{}", response);
                }

            }
            else
            {
                // 签名错误，如果数据里没有sign字段，也认为是签名错误
                logger.error("微信支付支付回调失败11");
                throw new LaiKeAPIException(API_OPERATION_FAILED, "微信回调失败", "payBack");
            };
        }
        catch (Exception e)
        {
            logger.error("微信回调失败", e);
            throw new LaiKeAPIException(API_OPERATION_FAILED, "微信回调失败", "payBack");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> getPayConfig(Map params) throws LaiKeAPIException
    {
        Map resultMap = new HashMap<>(16);
        try
        {
            PaymentVo paymentVo   = (PaymentVo) params.get("paymentVo");
            String    className   = paymentVo.getType();
            int       storeId     = paymentVo.getStoreId();
            String    paymentJson = paymentConfigModelMapper.getPaymentConfigInfo(storeId, className);
            paymentJson = paymentJson.replaceAll("%2B", "\\+");
            if (StringUtils.isEmpty(paymentJson))
            {
                logger.error("获取支付信息");
                throw new LaiKeAPIException(API_OPERATION_FAILED, "获取支付信息", "getPayConfig");
            }
            logger.info(className + "支付信息：" + paymentJson);
            JSONObject payJson = JSONObject.parseObject(paymentJson);
            resultMap.put("data", payJson);
        }
        catch (Exception e)
        {
            logger.error("获取支付信息", e);
            throw new LaiKeAPIException(API_OPERATION_FAILED, "获取支付信息", "getPayConfig");
        }
        return resultMap;
    }


    @Override
    public boolean MerchantTransfersToChange(String json)
    {
        return false;
    }

    @Override
    public void QueryBatchTransferOrder(MainVo vo)
    {

    }

    /**
     * 获取微信支付配置
     *
     * @param paymentVo
     * @return
     * @throws Exception
     */
    private WechatConfigInfo getWechatConfigInfo(PaymentVo paymentVo) throws Exception
    {
        //微信app支付 默认app支付
        String className   = paymentVo.getPayType();
        int    storeId     = paymentVo.getStoreId();
        String paymentJson = paymentConfigModelMapper.getPaymentConfigInfo(storeId, className);
        paymentJson = URLDecoder.decode(paymentJson, GloabConst.Chartset.UTF_8);
        logger.info(className + "支付配置信息：" + paymentJson);
        JSONObject payJson = JSONObject.parseObject(paymentJson);
        String     appID   = payJson.getString("appid");
        logger.info("appID:{}", appID);
        String mchID = payJson.getString("mch_id");
        logger.info("mchID:{}", mchID);
        String key = payJson.getString("mch_key");
        logger.info("key:{}", key);
        String certPath = payJson.getString("cert_p12");
//        String certPath = "C:/Users/Administrator/Desktop/WeChatPay/lkt_cert/apiclient_cert.p12";
        logger.info("certPath:{}", certPath);
        String notifyUrl = payJson.getString("notify_url");
        logger.info("notifyUrl:{}", notifyUrl);
        String appSecreKey = payJson.getString("appsecret");
        String serial_no =  payJson.getString("serial_no");
        logger.info("serial_no:{}", serial_no);
        String APIv3_key =  payJson.getString("APIv3_key");
        String key_pem =  payJson.getString("key_pem");
        logger.info("key_pem:{}", key_pem);

        logger.info("appSecreKey:{}", appSecreKey);
        return new WechatConfigInfo(appID, mchID, key, certPath, notifyUrl, appSecreKey, StringUtils.isEmpty(serial_no) ? "" :serial_no,StringUtils.isEmpty(APIv3_key) ? "" : APIv3_key,key_pem);
    }

}
