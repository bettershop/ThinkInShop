package com.laiketui.comps.payment.common.v3Dto;

import com.alibaba.fastjson2.JSONObject;
import com.laiketui.common.mapper.PaymentConfigModelMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.core.utils.tool.StringUtils;
import com.wechat.pay.java.core.RSAAutoCertificateConfig;
import com.wechat.pay.java.core.exception.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * v3商家转账到零钱商户号构造实例
 */
@Component
public class CompsMerchantConfig
{

    private final Logger                   logger = LoggerFactory.getLogger(CompsMerchantConfig.class);
    @Autowired
    private       PaymentConfigModelMapper paymentConfigModelMapper;

    //商户号构造实例集合  《key-storeId,value-config》
    public static volatile ConcurrentHashMap<Integer, RSAAutoCertificateConfig> configMap = new ConcurrentHashMap<>();

    //商户号构造实例集合数据  《key-storeId,value-configDate》
    public static volatile ConcurrentHashMap<Integer, Map<String, String>> configDateMap = new ConcurrentHashMap<>();

    public RSAAutoCertificateConfig getRSAConfig(Integer storeId) throws LaiKeAPIException
    {
        try
        {
            String paymentJson = paymentConfigModelMapper.getPaymentConfigInfo(storeId, "wechat_v3_withdraw");
            if (StringUtils.isEmpty(paymentJson))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WXYRTXWPZ, "V3微信余额提现未配置", "MerchantTransfersToChange");
            }
            paymentJson = URLDecoder.decode(paymentJson, GloabConst.Chartset.UTF_8);
            logger.info("v3商家转账到零钱配置信息：" + paymentJson);
            JSONObject payJson = JSONObject.parseObject(paymentJson);
            /**
             *微信商户号
             */
            String mchid = payJson.getString("mch_id");

            /**
             *商户私钥serial_no
             */
            String privateKey = payJson.getString("key_pem");

            /**
             *证书序列号
             */
            String serialNumber = payJson.getString("serial_no");

            /**
             *V3微信支付密钥
             */
            String apiV3Key = payJson.getString("mch_key");
            //获取
            Map<String, String> configDate = configDateMap.get(storeId);
            //获取
            RSAAutoCertificateConfig config = configMap.get(storeId);
            try
            {
                if (config == null)
                {
                    configDate = new HashMap<>();
                    configDate.put("mchid", mchid);
                    configDate.put("privateKey", privateKey);
                    configDate.put("serialNumber", serialNumber);
                    configDate.put("apiV3Key", apiV3Key);
                    // 同一个商户号构造多个实例，会抛出IllegalStateException异常
                    synchronized (CompsMerchantConfig.class)
                    {
                        config =
                                new RSAAutoCertificateConfig.Builder()
                                        .merchantId(mchid)
                                        .privateKey(privateKey)
                                        .merchantSerialNumber(serialNumber)
                                        .apiV3Key(apiV3Key)
                                        .build();
                        configMap.put(storeId, config);
                        configDateMap.put(storeId, configDate);
                    }
                }
                else
                {
                    //更新商户信息
                    String odlMchid        = configDate.get("mchid");
                    String odlPrivateKey   = configDate.get("privateKey");
                    String odlSerialNumber = configDate.get("serialNumber");
                    String odlApiV3Key     = configDate.get("mchid");
                    if (!odlMchid.equals(mchid) || !odlPrivateKey.equals(privateKey)
                            || !odlSerialNumber.equals(serialNumber) || !odlApiV3Key.equals(apiV3Key))
                    {
                        configDate = new HashMap<>();
                        configDate.put("mchid", mchid);
                        configDate.put("privateKey", privateKey);
                        configDate.put("serialNumber", serialNumber);
                        configDate.put("apiV3Key", apiV3Key);
                        synchronized (CompsMerchantConfig.class)
                        {
                            config =
                                    new RSAAutoCertificateConfig.Builder()
                                            .merchantId(mchid)
                                            .privateKey(privateKey)
                                            .merchantSerialNumber(serialNumber)
                                            .apiV3Key(apiV3Key)
                                            .build();
                            configMap.put(storeId, config);
                            configDateMap.put(storeId, configDate);
                        }
                    }
                }
            }
            catch (ServiceException e)
            {
                // http请求成功，但是wx接口失败，这里需要根据实际需求处理错误码
                logger.error("商家转账到零钱错误!");
                logger.error("错误信息:" + e.getErrorCode() + "," + e.getErrorMessage());
                throw new LaiKeAPIException(e.getErrorCode(), e.getErrorMessage());
            }
            catch (LaiKeAPIException l)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "MerchantTransfersToChange");
            }
            return config;
        }
        catch (LaiKeAPIException l)
        {
            logger.error("商家转账到零钱异常", l);
            throw l;
        }
        catch (Exception e)
        {
            logger.error("商家转账到零钱异常 ", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "MerchantTransfersToChange");
        }

    }
}
