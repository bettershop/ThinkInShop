package com.laiketui.common.service.dubbo;

import com.alibaba.fastjson2.JSON;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePrecreateRequest;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alipay.easysdk.factory.Factory;
import com.alipay.easysdk.kernel.Config;
import com.alipay.easysdk.kernel.util.ResponseChecker;
import com.alipay.easysdk.payment.common.models.AlipayTradeRefundResponse;
import com.google.common.collect.Maps;
import com.laiketui.common.api.pay.PublicPaymentService;
import com.laiketui.common.mapper.*;
import com.laiketui.core.config.alipay.AlipayConfigInfo;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.domain.mch.MchPromiseModel;
import com.laiketui.domain.mch.PromiseShModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * 支付宝支付
 *
 * @author Trick
 * @date 2020/12/3 15:09
 */
@Service("publicAlipayServiceImpl")
public class PublicAlipayServiceImpl implements PublicPaymentService
{
    private final Logger                logger = LoggerFactory.getLogger(PublicAlipayServiceImpl.class);
    @Autowired
    private       MchPromiseModelMapper mchPromiseModelMapper;

    @Autowired
    private MchModelMapper mchModelMapper;

    @Autowired
    private PromiseShModelMapper promiseShModelMapper;


    @Override
    public Map<String, String> refundOrder(int storeId, String sNo, Integer id, String className, String tradeNo, BigDecimal refundAmt, boolean isTempOrder, BigDecimal orderPrice,Integer refundId) throws LaiKeAPIException
    {
        return null;
    }

    //y标记
    @Override
    public Map<String, Object> refund(Map params) throws LaiKeAPIException
    {
        return null;
    }

    @Override
    public String qrCodeOrder(String appid, String privateKey, String publicKey, String orderno, BigDecimal orderAmt) throws LaiKeAPIException
    {
        try
        {
            AlipayClient alipayClient = new DefaultAlipayClient(GloabConst.AlibabaApiUrl.ALIPAY_GET_URL, appid, privateKey, "json", GloabConst.Chartset.UTF_8, publicKey, "RSA2");
            //二维码
            AlipayTradePrecreateRequest alipayRequest = new AlipayTradePrecreateRequest();
            // 支付成功后回跳地址
            alipayRequest.setReturnUrl(RETURNURL);
            // 支付后的异步通知地址
            alipayRequest.setNotifyUrl(NOTIFYURL);
            Map<String, String> requestParmaMap = new TreeMap<>();
            requestParmaMap.put("out_trade_no", orderno);
            requestParmaMap.put("product_code", "FAST_INSTANT_TRADE_PAY");
            requestParmaMap.put("total_amount", orderAmt.toString());
            requestParmaMap.put("subject", "二维码支付");
            alipayRequest.setBizContent(JSON.toJSONString(requestParmaMap));

            //发送请求
            AlipayTradePrecreateResponse response = alipayClient.execute(alipayRequest);
            if ("10000".equals(response.getCode()) && response.getQrCode() != null)
            {
                return response.getQrCode();
            }
            else
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_XDSB, "下单失败", "qrCodeOrder");
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("阿里退款 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "qrCodeOrder");
        }
    }

    @Override
    public Map<String, String> refundOrder(int storeId, Integer id, String className, String treadeNo, BigDecimal refundAmt) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            //获取商户支付配置信息
            String configStr = paymentConfigModelMapper.getPaymentConfigInfo(storeId, className);
            Config config    = AlipayConfigInfo.getOptions(configStr);
            if (config == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_ZFBZFCC, "支付宝支付出错", "pay");
            }
            Factory.setOptions(config);
            AlipayTradeRefundResponse response = Factory.Payment.Common().refund(treadeNo, refundAmt.toString());
            logger.info("支付宝退款返回参数{}", JSON.toJSONString(response.toMap()));
            if (ResponseChecker.success(response))
            {
                resultMap = response.toMap();
                //{"msg":"Success","code":"10000","buyer_user_id":"2088002677650555","out_trade_no":"GM210826145001750628","http_body":"{\"alipay_trade_refund_response\":{\"code\":\"10000\",\"msg\":\"Success\",\"buyer_logon_id\":\"sin***@163.com\",\"buyer_user_id\":\"2088002677650555\",\"fund_change\":\"Y\",\"gmt_refund_pay\":\"2021-08-26 15:11:30\",\"out_trade_no\":\"GM210826145001750628\",\"refund_fee\":\"0.01\",\"send_back_fee\":\"0.00\",\"trade_no\":\"2021082622001450551409619245\"},\"sign\":\"\"}
                logger.info("支付宝退款调用成功{}", JSON.toJSONString(resultMap));
            }
            else
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_ZFBTKCC, "支付宝退款出错", "refund");
            }
            Map<String, String> ret = Maps.newHashMap();
            ret.put("code", "200");
            return ret;
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("阿里退款 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "returnAlipay");
        }
    }

    @Override
    public Map<String, String> refundOrder(int storeId, Integer id, String className, String treadeNo, BigDecimal refundAmt, BigDecimal orderPrice) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        Map<String, String> ret = Maps.newHashMap();
        try
        {
            //退款请求号 (标识一次退款请求)
            String concat = treadeNo.concat(String.valueOf(System.currentTimeMillis()));
            logger.info("支付宝退款请求号：{}", concat);
            //获取商户支付配置信息
            String configStr = paymentConfigModelMapper.getPaymentConfigInfo(storeId, className);
            Config config    = AlipayConfigInfo.getOptions(configStr);
            if (config == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_ZFBZFCC, "支付宝支付出错", "pay");
            }
            Factory.setOptions(config);
            AlipayTradeRefundResponse response = Factory.Payment.Common().optional("out_request_no", concat).refund(treadeNo, refundAmt.toString());
            if (ResponseChecker.success(response))
            {
                resultMap = response.toMap();
                //{"msg":"Success","code":"10000","buyer_user_id":"2088002677650555","out_trade_no":"GM210826145001750628","http_body":"{\"alipay_trade_refund_response\":{\"code\":\"10000\",\"msg\":\"Success\",\"buyer_logon_id\":\"sin***@163.com\",\"buyer_user_id\":\"2088002677650555\",\"fund_change\":\"Y\",\"gmt_refund_pay\":\"2021-08-26 15:11:30\",\"out_trade_no\":\"GM210826145001750628\",\"refund_fee\":\"0.01\",\"send_back_fee\":\"0.00\",\"trade_no\":\"2021082622001450551409619245\"},\"sign\":\"\"}
                logger.info("支付宝退款调用成功{}", JSON.toJSONString(resultMap));
                ret.put("ali_out_request_no",concat);
            }
            else
            {
                resultMap = response.toMap();
                logger.info("支付宝退款调用失败{}", JSON.toJSONString(resultMap));
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_ZFBTKCC, "支付宝退款出错", "refund");
            }
            ret.put("code", "200");
            return ret;
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("阿里退款 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "returnAlipay");
        }
    }

    @Override
    public Map<String, String> refundOrder(int storeId, PromiseShModel promiseShModel, String className, String treadeNo, BigDecimal refundAmt) throws LaiKeAPIException
    {
        try
        {
            return refundOrder(storeId, promiseShModel, className, treadeNo, refundAmt);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("微信退款 异常:", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "returnAlipay");
        }
    }

    @Override
    public Map<String, String> refundOrder(int storeId, int id, int isPass, String refusedWhy, String className, String treadeNo, BigDecimal refundAmt) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            //获取商户支付配置信息
            String configStr = paymentConfigModelMapper.getPaymentConfigInfo(storeId, className);
            Config config    = AlipayConfigInfo.getOptions(configStr);
            if (config == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_ZFBZFCC, "支付宝支付出错", "pay");
            }
            Factory.setOptions(config);
            AlipayTradeRefundResponse response = Factory.Payment.Common().refund(treadeNo, refundAmt.toString());
            if (ResponseChecker.success(response))
            {
                resultMap = response.toMap();
                //{"msg":"Success","code":"10000","buyer_user_id":"2088002677650555","out_trade_no":"GM210826145001750628","http_body":"{\"alipay_trade_refund_response\":{\"code\":\"10000\",\"msg\":\"Success\",\"buyer_logon_id\":\"sin***@163.com\",\"buyer_user_id\":\"2088002677650555\",\"fund_change\":\"Y\",\"gmt_refund_pay\":\"2021-08-26 15:11:30\",\"out_trade_no\":\"GM210826145001750628\",\"refund_fee\":\"0.01\",\"send_back_fee\":\"0.00\",\"trade_no\":\"2021082622001450551409619245\"},\"sign\":\"\"}
                logger.info("支付宝退款调用成功{}", JSON.toJSONString(resultMap));
                PromiseShModel promiseShModel = new PromiseShModel();
                promiseShModel.setStatus(isPass);
                promiseShModel.setIs_pass(isPass);
                promiseShModel.setId(id);
                promiseShModelMapper.updateByPrimaryKeySelective(promiseShModel);
                PromiseShModel promiseShTKModel = promiseShModelMapper.selectByPrimaryKey(id);
                //根据审核表获取保证金表
                MchPromiseModel mchPromiseOld = new MchPromiseModel();
                mchPromiseOld.setStatus(MchPromiseModel.PromiseConstant.STATUS_PAY);
                mchPromiseOld.setMch_id(promiseShTKModel.getMch_id());
                mchPromiseOld = mchPromiseModelMapper.selectOne(mchPromiseOld);
                //修改保证金表
                MchPromiseModel mchPromiseUpdate = new MchPromiseModel();
                mchPromiseUpdate.setStatus(MchPromiseModel.PromiseConstant.STATUS_RETURN_PAY);
                mchPromiseUpdate.setIs_return_pay(1);
                mchPromiseUpdate.setId(mchPromiseOld.getId());
                mchPromiseUpdate.setUpdate_date(new Date());
                int row = mchPromiseModelMapper.updateByPrimaryKeySelective(mchPromiseUpdate);
                if (row < 1)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TKSB, "退款失败");
                }
            }
            else
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_ZFBTKCC, "支付宝退款出错", "refund");
            }
            Map<String, String> ret = Maps.newHashMap();
            ret.put("code", "200");
            return ret;
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("阿里退款 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "returnAlipay");
        }
    }

    public Map<String, Object> newDivideAccount(String orderNo, String transactionId, StringBuilder loggerStr) throws LaiKeAPIException
    {
        return null;
    }

    @Autowired
    private PaymentConfigModelMapper paymentConfigModelMapper;

    @Autowired
    private PaymentModelMapper paymentModelMapper;
}

