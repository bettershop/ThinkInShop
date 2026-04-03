package com.laiketui.comps.payment.services;

import com.laiketui.comps.api.payment.CompsPayService;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.GloabConst;
import org.springframework.stereotype.Service;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.cert.*;
import java.util.Map;

/**
 * 支付适配类
 *
 * @author wangxian
 */
@Service
public abstract class CompsPayServiceAdapter implements CompsPayService
{

    @Override
    public Map<String, Object> pay(Map params) throws LaiKeAPIException
    {
        return null;
    }

    @Override
    public Map<String, Object> refund(Map params) throws LaiKeAPIException
    {
        return null;
    }

    @Override
    public Map<String, Object> payBack(Map params) throws LaiKeAPIException
    {
        return null;
    }

    @Override
    public Map<String, Object> getPayConfig(Map params) throws LaiKeAPIException
    {
        return null;
    }

    /**
     * 获取证书。
     *
     * @param certContent 证书文件路径  (required)
     * @return X509证书
     */
    public static X509Certificate getCertificate(String certContent) throws IOException
    {
        BufferedInputStream bis = null;
        try
        {
            bis = new BufferedInputStream(new ByteArrayInputStream(certContent.getBytes(GloabConst.Chartset.UTF_8)));
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
            if (bis != null)
            {
                bis.close();
            }
        }
    }

    /**
     * 获取证书序列号
     *
     * @param certContent cert_pem 证书内容
     * @return
     * @throws IOException
     */
    public static String getSerialNo(String certContent) throws IOException
    {
        X509Certificate certificate = getCertificate(certContent);
        return certificate.getSerialNumber().toString(16).toUpperCase();
    }

}