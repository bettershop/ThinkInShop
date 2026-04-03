package com.laiketui.comps.payment.util;

import com.google.common.collect.Maps;
import com.laiketui.core.utils.tool.DataUtils;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.springframework.util.Base64Utils;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.*;

public final class CompsWechatPayUtils
{

    /**
     * 微信证书
     */
    public final static String                       WECHAT_CERTS_URL = "https://api.mch.weixin.qq.com/v3/certificates";
    /**
     * 服务器证书
     */
    public static       Map<String, X509Certificate> certsMaps        = Maps.newConcurrentMap();

    /**
     * 加密
     *
     * @param params
     * @param signatureStr
     * @return
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     * @throws SignatureException
     */
    public static String sign(Map<String, Object> params, String signatureStr) throws IOException, NoSuchAlgorithmException, InvalidKeyException, SignatureException
    {
        String     keyContent = DataUtils.getStringVal(params, "key");
        PrivateKey privateKey = getPrivateKey(keyContent);
        Signature  sign       = Signature.getInstance("SHA256withRSA");
        sign.initSign(privateKey);
        sign.update(signatureStr.getBytes(StandardCharsets.UTF_8));
        return Base64Utils.encodeToString(sign.sign());
    }

    /**
     * 获取证书。
     *
     * @param fis 证书文件流
     * @return X509证书
     */
    private static X509Certificate getCertificate(InputStream fis) throws IOException
    {
        BufferedInputStream bis = new BufferedInputStream(fis);
        try
        {
            CertificateFactory cf   = CertificateFactory.getInstance("X509");
            X509Certificate    cert = (X509Certificate) cf.generateCertificate(bis);
            cert.checkValidity();
            return cert;
        }
        catch (CertificateExpiredException e)
        {
            throw new RuntimeException("证书已过期", e);
        }
        catch (CertificateNotYetValidException e)
        {
            throw new RuntimeException("证书尚未生效", e);
        }
        catch (CertificateException e)
        {
            throw new RuntimeException("无效的证书文件", e);
        }
        finally
        {
            bis.close();
        }
    }

    /**
     * 获取私钥。
     *
     * @param content 私钥文件路径  (required)
     * @return 私钥对象
     */
    public static PrivateKey getPrivateKey(String content) throws IOException
    {
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
     * 解密微信支付服务器返回的结果
     *
     * @param associatedData
     * @param nonce
     * @param ciphertext
     * @return
     */
    public static String decryptResponseBody(String apiv3key, String associatedData, String nonce, String ciphertext)
    {
        try
        {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            // apiv3key 使用
            SecretKeySpec    key  = new SecretKeySpec(apiv3key.getBytes(StandardCharsets.UTF_8), "AES");
            GCMParameterSpec spec = new GCMParameterSpec(128, nonce.getBytes(StandardCharsets.UTF_8));
            cipher.init(Cipher.DECRYPT_MODE, key, spec);
            cipher.updateAAD(associatedData.getBytes(StandardCharsets.UTF_8));
            byte[] bytes;
            try
            {
                bytes = cipher.doFinal(Base64Utils.decodeFromString(ciphertext));
            }
            catch (GeneralSecurityException e)
            {
                throw new IllegalArgumentException(e);
            }
            return new String(bytes, StandardCharsets.UTF_8);
        }
        catch (NoSuchAlgorithmException | NoSuchPaddingException e)
        {
            throw new IllegalStateException(e);
        }
        catch (InvalidKeyException | InvalidAlgorithmParameterException e)
        {
            throw new IllegalArgumentException(e);
        }
    }


    /**
     * 获取 nonce str
     *
     * @return
     */
    public static String generateNonceStr()
    {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 获取响应的xml 字符串
     *
     * @param request
     * @return
     */
    public static String getRequestBody(HttpServletRequest request) throws Exception
    {
        try
        {
            BufferedReader br = new BufferedReader(new InputStreamReader(request.getInputStream(), "UTF-8"));
            String         line;
            StringBuilder  sb = new StringBuilder();
            while ((line = br.readLine()) != null)
            {
                sb.append(line);
            }
            return sb.toString();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * 解析通知xml
     *
     * @param xml
     * @return
     */
    public static Map parseXmlToMap(String xml)
    {
        TreeMap treeMap = Maps.newTreeMap();
        try
        {
            StringReader read = new StringReader(xml);
            // 创建新的输入源SAX 解析器将使用 InputSource 对象来确定如何读取 XML 输入
            org.xml.sax.InputSource source = new org.xml.sax.InputSource(read);
            // 创建一个新的SAXBuilder
            SAXBuilder sb = new SAXBuilder();
            // 通过输入源构造一个Document
            Document doc = sb.build(source);
            // 指向根节点
            Element       root = doc.getRootElement();
            List<Element> es   = root.getChildren();
            if (es != null && es.size() != 0)
            {
                for (Element element : es)
                {
                    treeMap.put(element.getName(), element.getValue());
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return treeMap;
    }


}
