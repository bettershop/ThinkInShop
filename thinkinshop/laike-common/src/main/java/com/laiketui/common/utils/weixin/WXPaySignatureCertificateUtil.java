package com.laiketui.common.utils.weixin;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.laiketui.core.config.wechatpay.WechatConfigInfo;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.utils.tool.StringUtils;
import com.wechat.pay.contrib.apache.httpclient.WechatPayHttpClientBuilder;
import com.wechat.pay.contrib.apache.httpclient.auth.AutoUpdateCertificatesVerifier;
import com.wechat.pay.contrib.apache.httpclient.auth.PrivateKeySigner;
import com.wechat.pay.contrib.apache.httpclient.auth.WechatPay2Credentials;
import com.wechat.pay.contrib.apache.httpclient.auth.WechatPay2Validator;
import com.wechat.pay.contrib.apache.httpclient.util.PemUtil;
import github.wxpay.sdk.WXPayConstants;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;

import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @Author: sunH_
 * @Date: Create in 19:37 2023/9/7
 */
public class WXPaySignatureCertificateUtil
{

    public static Logger logger = LoggerFactory.getLogger(WXPaySignatureCertificateUtil.class);
    /**
     * 证书验证
     * 自动更新的签名验证器
     *
     * @param privateKey  商户API V3私钥
     * @param mchId       服务商商户id
     * @param mchSerialNo 平台证书序列号
     * @return
     * @throws IOException
     */
    public static CloseableHttpClient checkSign(String privateKey, String mchId, String mchSerialNo) throws IOException
    {
        try
        {
            //验签
            CloseableHttpClient httpClient         = null;
            PrivateKey          merchantPrivateKey = PemUtil.loadPrivateKey(new ByteArrayInputStream(privateKey.getBytes("utf-8")));
            httpClient = WechatPayHttpClientBuilder.create()
                    .withMerchant(mchId, mchSerialNo, merchantPrivateKey)
                    .withValidator(new WechatPay2Validator(WXPaySignatureCertificateUtil.getVerifier(mchSerialNo,privateKey,null,null)))
                    .build();
            return httpClient;
        }catch (Exception ex)
        {

        }
        return null;
    }


    /**
     * 证书验证
     * 自动更新的签名验证器
     *
     * @param privateKey  商户API V3私钥
     * @param mchId       服务商商户id
     * @param mchSerialNo 平台证书序列号
     * @return
     * @throws IOException
     */
    public static CloseableHttpClient checkSign(String privateKey, String mchId, String mchSerialNo,String apiKey) throws IOException
    {
        try
        {
            //验签
            CloseableHttpClient httpClient         = null;
            PrivateKey          merchantPrivateKey = PemUtil.loadPrivateKey(new ByteArrayInputStream(privateKey.getBytes("utf-8")));
            httpClient = WechatPayHttpClientBuilder.create()
                    .withMerchant(mchId, mchSerialNo, merchantPrivateKey)
                    .withValidator(new WechatPay2Validator(WXPaySignatureCertificateUtil.getVerifier(mchSerialNo,privateKey,apiKey,mchId)))
                    .build();
            return httpClient;
        }catch (Exception ex)
        {
            logger.error("验签失败：{}",ex.getMessage());
        }
        return null;
    }


    /**
     * 保存微信平台证书
     */
    private static final ConcurrentHashMap<String, AutoUpdateCertificatesVerifier> verifierMap = new ConcurrentHashMap<>();

    /**
     * 功能描述:获取平台证书，自动更新
     * 注意：这个方法内置了平台证书的获取和返回值解密
     */
    public static AutoUpdateCertificatesVerifier getVerifier(String mchSerialNo,String key_pem,String apiKey,String mch_id)
    {
        AutoUpdateCertificatesVerifier verifier = null;
        if (verifierMap.isEmpty() || !verifierMap.containsKey(mchSerialNo))
        {
            verifierMap.clear();
            try
            {
                //传入证书
                PrivateKey privateKey = getPrivateKey(key_pem);
                //刷新
                PrivateKeySigner      signer      = new PrivateKeySigner(mchSerialNo, privateKey);
                WechatPay2Credentials credentials = new WechatPay2Credentials(mch_id, signer);
                verifier = new AutoUpdateCertificatesVerifier(credentials
                        , apiKey.getBytes(StandardCharsets.UTF_8));
                verifierMap.put(verifier.getValidCertificate().getSerialNumber() + "", verifier);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        else
        {
            verifier = verifierMap.get(mchSerialNo);
        }
        return verifier;
    }


    /**
     * 功能描述:获取平台证书，自动更新
     * 注意：这个方法内置了平台证书的获取和返回值解密
     */
    public static AutoUpdateCertificatesVerifier getVerifier(String mchSerialNo)
    {
        logger.info("verifierMap.size{}",verifierMap.size());
        AutoUpdateCertificatesVerifier verifier = null;
        if (verifierMap.isEmpty() || !verifierMap.containsKey(mchSerialNo))
        {
            verifierMap.clear();
            try
            {
                //传入证书
                PrivateKey privateKey = getPrivateKey();
                //刷新
                PrivateKeySigner signer = new PrivateKeySigner(mchSerialNo, privateKey);
                WechatPay2Credentials credentials = new WechatPay2Credentials(WxV3PayConfig.Mch_ID, signer);
                verifier = new AutoUpdateCertificatesVerifier(credentials
                        , WxV3PayConfig.apiV3Key.getBytes(StandardCharsets.UTF_8));
                verifierMap.put(verifier.getValidCertificate().getSerialNumber()+"", verifier);
            }
            catch (Exception e)
            {
                logger.error("获取平台证书失败：{}",e.getMessage());
                e.printStackTrace();
            }
        } else
        {
            verifier = verifierMap.get(mchSerialNo);
        }
        return verifier;
    }


    /**
     * 生成带签名支付信息
     *
     * @param appId     服务商应用id
     * @param timestamp 时间戳
     * @param nonceStr  随机数
     * @param prepayId  预付单
     * @return 支付信息
     * @throws Exception
     */
    public static String appPaySign(String appId, String timestamp, String nonceStr, String prepayId,String keyPem) throws Exception
    {
        //上传私钥
        PrivateKey privateKey = getPrivateKey(keyPem);
        String signatureStr = Stream.of(appId, timestamp, nonceStr, "prepay_id=" + prepayId)
                .collect(Collectors.joining("\n", "", "\n"));
        Signature sign = Signature.getInstance("SHA256withRSA");
        sign.initSign(privateKey);
        sign.update(signatureStr.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(sign.sign());
    }

    /**
     * 生成带签名支付信息
     *
     * @param timestamp 时间戳
     * @param nonceStr  随机数
     * @param prepayId  预付单
     * @return 支付信息
     * @throws Exception
     */
    public static String jsApiPaySign(String timestamp, String nonceStr, String prepayId) throws Exception
    {
        //上传私钥
        PrivateKey privateKey = getPrivateKey();
        String signatureStr = Stream.of(WxV3PayConfig.APP_ID, timestamp, nonceStr, "prepay_id=" + prepayId)
                .collect(Collectors.joining("\n", "", "\n"));
        Signature sign = Signature.getInstance("SHA256withRSA");
        sign.initSign(privateKey);
        sign.update(signatureStr.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(sign.sign());
    }


    /**
     * 获取私钥。
     * 证书路径 本地使用如： D:\\微信平台证书工具\\7.9\\apiclient_key.pem
     * 证书路径 线上使用如： /usr/apiclient_key.pem
     * String filename 私钥文件路径  (required)
     *
     * @return 私钥对象
     */
    public static PrivateKey getPrivateKey() throws IOException
    {
        String content = new String(WxV3PayConfig.privateKey.getBytes("utf-8"), "utf-8");
        try
        {
            String privateKey = content.replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s+", "");

            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePrivate(
                    new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKey)));
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new RuntimeException("当前Java环境不支持RSA", e);
        }
        catch (InvalidKeySpecException e)
        {
            throw new RuntimeException("无效的密钥格式");
        }
    }


    /**
     * 获取私钥。
     * 证书路径 本地使用如： D:\\微信平台证书工具\\7.9\\apiclient_key.pem
     * 证书路径 线上使用如： /usr/apiclient_key.pem
     * String filename 私钥文件路径  (required)
     *
     * @return 私钥对象
     */
    public static PrivateKey getPrivateKey(String key_pem) throws IOException
    {
        String content = new String(key_pem.getBytes("utf-8"), "utf-8");
        try
        {
            String privateKey = content.replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s+", "");

            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePrivate(
                    new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKey)));
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new RuntimeException("当前Java环境不支持RSA", e);
        }
        catch (InvalidKeySpecException e)
        {
            throw new RuntimeException("无效的密钥格式");
        }
    }


    /**
     * @param key_pem
     * @return
     * @throws IOException
     */
    /**
     * 加载公钥 (X.509 格式)
     * @param pem 公钥 PEM 内容，带 -----BEGIN PUBLIC KEY-----
     * @return PublicKey
     */
    public static PublicKey getPublicKey(String pem)
    {
        try
        {
            logger.info("微信支付公钥：{}",pem);
            String content = pem.replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s+", "");

            byte[] keyBytes = Base64.getDecoder().decode(content.getBytes(StandardCharsets.UTF_8));
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePublic(keySpec);
        }
        catch (Exception e)
        {
            logger.error("加载公钥失败，错误日志信息：{}",e.getMessage());
            throw new RuntimeException("加载公钥失败", e);
        }
    }



        /**
         * 功能描述: 验证签名
         * 注意：使用平台证书验签
         * Wechatpay-Signature 微信返签名
         * Wechatpay-Serial 平台证书
         */
    public static boolean verifySign(HttpServletRequest request, String body,String privatePem,String mchId,String apIv3Key)
    {
        boolean verify = false;
        try
        {

            String wechatPaySignature = request.getHeader("Wechatpay-Signature");
            String wechatPayTimestamp = request.getHeader("Wechatpay-Timestamp");
            String wechatPayNonce     = request.getHeader("Wechatpay-Nonce");
            String wechatPaySerial    = request.getHeader("Wechatpay-Serial");

            logger.info("wechatPaySerial::{}", wechatPaySerial);
            logger.info("wechatPaySignature::{}", wechatPaySignature);
            logger.info("wechatPayTimestamp::{}", wechatPayTimestamp);
            logger.info("wechatPayNonce::{}", wechatPayNonce);

            //组装签名串(格式按时间戳》随机串》消息体排列)
            String signStr = Stream.of(wechatPayTimestamp, wechatPayNonce, body)
                    .collect(Collectors.joining("\n", "", "\n"));
            //获取平台证书
            AutoUpdateCertificatesVerifier verifier = getVerifier(wechatPaySerial,privatePem,apIv3Key,mchId);

            if (verifier != null)
            {
                Signature signature = Signature.getInstance("SHA256withRSA");
                signature.initVerify(verifier.getValidCertificate());
                signature.update(signStr.getBytes(StandardCharsets.UTF_8));
                verify = signature.verify(Base64.getDecoder().decode(wechatPaySignature));
            }
            else
            {
                logger.error("获取平台证书为空");
            }
        }
        catch (InvalidKeyException | SignatureException | NoSuchAlgorithmException e)
        {
            logger.error("微信平台证书验签失败，错误信息：：{}",e.getMessage());
            throw new RuntimeException("微信平台证书验签失败");
        }
        return verify;
    }


    /**
     * 使用微信支付公钥验签
     * @param request
     * @param body
     * @param pubPem 微信支付公钥pem
     * @return
     */
    public static boolean verifySign(HttpServletRequest request, String body,String pubPem)
    {
        boolean verify = false;
        try
        {
            String wechatPaySignature = request.getHeader("Wechatpay-Signature");
            String wechatPayTimestamp = request.getHeader("Wechatpay-Timestamp");
            String wechatPayNonce     = request.getHeader("Wechatpay-Nonce");
            //微信支付公钥id
            String wechatPaySerial    = request.getHeader("Wechatpay-Serial");
            logger.info("wechatPaySerial::{}", wechatPaySerial);
            logger.info("wechatPaySignature::{}", wechatPaySignature);
            logger.info("wechatPayTimestamp::{}", wechatPayTimestamp);
            logger.info("wechatPayNonce::{}", wechatPayNonce);

            //组装签名串(格式按时间戳》随机串》消息体排列)
            String signStr = Stream.of(wechatPayTimestamp, wechatPayNonce, body)
                    .collect(Collectors.joining("\n", "", "\n"));

            PublicKey publicKey = getPublicKey(pubPem);

            if (publicKey != null)
            {
                logger.info("publicKey::{}", publicKey);
                Signature signature = Signature.getInstance("SHA256withRSA");
                signature.initVerify(publicKey);
                signature.update(signStr.getBytes(StandardCharsets.UTF_8));
                verify = signature.verify(Base64.getDecoder().decode(wechatPaySignature));
            }
            else
            {
                logger.error("公钥为空");
            }
        }
        catch (InvalidKeyException | SignatureException | NoSuchAlgorithmException e)
        {
            logger.error("微信支付公钥验签失败，错误信息：：{}",e.getMessage());
            throw new RuntimeException("微信支付公钥验签失败");
        }
        return verify;
    }


    public static String buildAuthorization(String mchId,
                                            String serialNo,
                                            String method,
                                            String url,
                                            String bodyJson,
                                            String timestamp,
                                            String nonceStr,
                                            String keyPem) throws Exception {
        if (bodyJson == null) bodyJson = "";

        if (StringUtils.isEmpty(serialNo))
        {
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_HQZSSB,"获取证书失败");
        }

        // 待签字符串（最后必须有换行）
        String message = method.toUpperCase() + "\n"
                + url + "\n"
                + timestamp + "\n"
                + nonceStr + "\n"
                + bodyJson + "\n";

        logger.info("签名串明文:{}",message);

        // RSA-SHA256 签名并 Base64
        Signature sign = Signature.getInstance("SHA256withRSA");
        PrivateKey privateKey = getPrivateKey(keyPem);
        sign.initSign(privateKey);
        sign.update(message.getBytes(StandardCharsets.UTF_8));
        String signature = Base64.getEncoder().encodeToString(sign.sign());

        logger.info("签名:{}",signature);

        // 组装 Token
        String token = String.format(
                "mchid=\"%s\",nonce_str=\"%s\",signature=\"%s\",timestamp=\"%s\",serial_no=\"%s\"",
                mchId, nonceStr, signature, timestamp, serialNo
        );

        // 完整 Authorization 值
        return "WECHATPAY2-SHA256-RSA2048 " + token;
    }

    public static void wxV3Post(ByteArrayOutputStream bos, ObjectMapper objectMapper, ObjectNode rootNode, HttpPost httpPost, String authorization) throws IOException
    {
        httpPost.addHeader("Accept", "application/json");
        httpPost.addHeader("Content-Type", "application/json; charset=UTF-8");
        httpPost.addHeader("Authorization", authorization);
        objectMapper.writeValue(bos, rootNode);
        String bodyJson = rootNode.toString();

        objectMapper.writeValue(bos, rootNode);
        StringEntity entity = new StringEntity(bodyJson, StandardCharsets.UTF_8);
        entity.setContentType("application/json");
        httpPost.setEntity(entity);
    }

    /**
     * 微信v3退款
     * @param out_refund_no 商户退款单号
     * @return
     * @throws Exception
     */
    public static void wxV3Refund(String out_refund_no,String tradeNo, Integer total_fee, Integer refund_fee, WechatConfigInfo payConfig) throws Exception
    {
        String url = "https://" + WXPayConstants.DOMAIN_API + WXPayConstants.V3_REFUND_URL_SUFFIX;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode rootNode = objectMapper.createObjectNode();

        //商户退款单号
        rootNode.put("out_refund_no", out_refund_no);
        //商户订单号
        rootNode.put("out_trade_no", tradeNo);

        logger.info("out_refund_no:{}",out_refund_no);

        rootNode.putObject("amount")
                .put("refund", refund_fee)
                .put("total", total_fee)
                .put("currency", "CNY");

        //商户号
        String mchId = payConfig.getMchID();
        //序列证书号
        String serial_no =  payConfig.getSerial_no();
        //私钥
        String keyPem = payConfig.getKey_pem();
        //时间戳
        String timestamp = String.valueOf((System.currentTimeMillis() / 1000));
        //随机字符串 32位
        String nonceStr = RandomStringUtils.randomAlphanumeric(32);
        HttpPost httpPost = new HttpPost(url);
        //签名认证token
        String authorization = WXPaySignatureCertificateUtil.buildAuthorization(mchId, serial_no, httpPost.getMethod(), httpPost.getURI().getPath(), rootNode.toString(), timestamp, nonceStr, keyPem);
        WXPaySignatureCertificateUtil.wxV3Post(bos, objectMapper, rootNode, httpPost, authorization);

        try(CloseableHttpClient httpClient = HttpClients.createDefault())
        {
            CloseableHttpResponse response = httpClient.execute(httpPost);

            int statusCode = response.getStatusLine().getStatusCode();
            logger.info("statusCode:{}", statusCode);
            String result = EntityUtils.toString(response.getEntity(), "UTF-8");
            logger.info("result:{}", result);
            if (statusCode != 200)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CZYC, "退款失败", "wxV3Refund");
            }
        }
        catch (Exception e)
        {
            logger.error("退款失败",e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CZYC, "退款失败", "wxV3Refund");
        }
    }


    /**
     * 判断是否是xml格式
     * @param str
     * @return
     */
    public static boolean isXmlValid(String str)
    {
        try
        {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setExpandEntityReferences(false);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true); // 防 XXE
            DocumentBuilder builder = factory.newDocumentBuilder();
            builder.parse(new InputSource(new StringReader(str)));
            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }
}
