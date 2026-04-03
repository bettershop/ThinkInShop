package com.laiketui.core.config.wechatpay;


import github.wxpay.sdk.IWXPayDomain;
import github.wxpay.sdk.WXPayConfig;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

/**
 * @author wangxian
 */
public class WechatConfigInfo extends WXPayConfig
{

    private byte[] certData;
    /**
     * 微信公众号appid
     */
    private String appID;
    /**
     * 公众号密钥
     */
    private String appSecreKey;
    /**
     * 微信商户号
     */
    private String mchID;
    /**
     * 密钥
     */
    private String key;
    /**
     * 证书路径
     */
    private String certPath;
    /**
     * 链接超时
     */
    private int    connTimeout   = 80000;
    /**
     * 读取超时
     */
    private int    readTimeoutMs = 100000;

    /**
     * 序列证书号
     */
    private String serial_no;

    /**
     * 私钥
     */
    private String key_pem;

    /**
     * v3 key
     */
    private String APIv3_key;

    /**
     * 支付回调通知地址
     */
    private String notifyUrl;

    public String getAppSecreKey()
    {
        return appSecreKey;
    }

    public void setAppSecreKey(String appSecreKey)
    {
        this.appSecreKey = appSecreKey;
    }

    public String getNotifyUrl()
    {
        return notifyUrl;
    }

    public void setNotifyUrl(String notifyUrl)
    {
        this.notifyUrl = notifyUrl;
    }

    public WechatConfigInfo(String appID, String mchID, String key, String certPath, int connTimeout, int readTimeoutMs) throws Exception
    {

        setAppID(appID);
        setCertPath(certPath);
        setConnTimeout(connTimeout);
        setReadTimeoutMs(readTimeoutMs);
        setKey(key);
        setMchID(mchID);

        File        file       = new File(getCertPath());
        try (InputStream certStream = new FileInputStream(file)) {
            this.certData = new byte[(int) file.length()];
            certStream.read(this.certData);
        }
    }


    public WechatConfigInfo(String appID, String mchID, String key) throws Exception
    {
        setAppID(appID);
        setKey(key);
        setMchID(mchID);
    }


    public WechatConfigInfo(String appID, String mchID, String key, String certPath, String notifyUrl, String appSecreKey,String serial_no,String APIv3_key,String key_pem) throws Exception
    {
        setAppID(appID);
        setCertPath(certPath);
        setConnTimeout(connTimeout);
        setReadTimeoutMs(readTimeoutMs);
        setKey(key);
        setMchID(mchID);
        setNotifyUrl(notifyUrl);
        setAppSecreKey(appSecreKey);
        setSerial_no(serial_no);
        setAPIv3_key(APIv3_key);
        setKey_pem(key_pem);

        File        file       = new File(getCertPath());
        try (InputStream certStream = new FileInputStream(file)) {
            this.certData = new byte[(int) file.length()];
            certStream.read(this.certData);
        }
    }

    public void setConnTimeout(int connTimeout)
    {
        this.connTimeout = connTimeout;
    }

    public void setReadTimeoutMs(int readTimeoutMs)
    {
        this.readTimeoutMs = readTimeoutMs;
    }


    @Override
    public String getAppID()
    {
        return this.appID;
    }

    @Override
    public String getMchID()
    {
        return this.mchID;
    }

    @Override
    public String getKey()
    {
        return this.key;
    }

    @Override
    public InputStream getCertStream()
    {
        return new ByteArrayInputStream(this.certData);
    }

    @Override
    public int getHttpConnectTimeoutMs()
    {
        return 8000;
    }

    @Override
    public int getHttpReadTimeoutMs()
    {
        return 10000;
    }

    @Override
    protected IWXPayDomain getWXPayDomain()
    {
        return null;
    }

    public void setKey(String key)
    {
        this.key = key;
    }

    public String getKey_pem() {
        return key_pem;
    }

    public void setKey_pem(String key_pem) {
        this.key_pem = key_pem;
    }

    public void setCertPath(String certPath)
    {
        this.certPath = certPath;
    }

    public String getCertPath()
    {
        return certPath;
    }

    public void setAppID(String appID)
    {
        this.appID = appID;
    }

    public String getAPIv3_key() {
        return APIv3_key;
    }

    public void setAPIv3_key(String APIv3_key) {
        this.APIv3_key = APIv3_key;
    }

    public String getSerial_no() {
        return serial_no;
    }

    public void setSerial_no(String serial_no) {
        this.serial_no = serial_no;
    }

    public void setMchID(String mchID)
    {
        this.mchID = mchID;
    }

}
