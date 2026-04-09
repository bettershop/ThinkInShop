package com.laiketui.core.utils.tool;

import okhttp3.ConnectionPool;
import okhttp3.ConnectionSpec;
import okhttp3.OkHttpClient;
import okhttp3.internal.Util;
import okhttp3.internal.platform.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * 帮助类：okhttp公共方法
 */
public class OKHttpsHelper
{

    private final static Logger logger = LoggerFactory.getLogger(OKHttpsHelper.class);

    /**
     * 设置SSLFactory
     *
     * @param builder
     * @throws KeyManagementException
     * @throws NoSuchAlgorithmException
     */
    public static OkHttpClient upgradeOkHttpClient()
    {
        try
        {
            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            //获取jdk版本
            String jdkversion = System.getProperty("java.specification.version");
//            logger.info("JDK版本：" + jdkversion);
            //其他jdk版本不支持这个方法 builder.sslSocketFactory

            if ("1.8".equalsIgnoreCase(jdkversion))
            {
//                logger.info("=>" + jdkversion);
                SSLSocketFactory sslSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
                X509TrustManager trustManager     = Platform.get().platformTrustManager();
                builder.sslSocketFactory(sslSocketFactory, trustManager);
                builder.hostnameVerifier(new HostnameVerifier()
                {
                    @Override
                    public boolean verify(String hostname, SSLSession session)
                    {
                        return true;
                    }
                });
            }

            builder.retryOnConnectionFailure(false);
            builder.connectionPool(pool());
            builder.connectionSpecs(Util.immutableListOf(ConnectionSpec.MODERN_TLS, ConnectionSpec.COMPATIBLE_TLS, ConnectionSpec.CLEARTEXT));
            builder.connectTimeout(30, TimeUnit.SECONDS);
            builder.readTimeout(30, TimeUnit.SECONDS);
            builder.writeTimeout(30, TimeUnit.SECONDS);
            return builder.build();
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    private static ConnectionPool pool()
    {
        return new ConnectionPool(2000, 50, TimeUnit.MINUTES);
    }


    /**
     * 兼容okhttp3 3.4.1版本
     *
     * @return
     */
    public static OkHttpClient builderHttpClientTwo()
    {
        return new OkHttpClient.Builder()
                .retryOnConnectionFailure(false)
                .connectionPool(pool())
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    public static OkHttpClient builderHttpClient()
    {
        ConnectionSpec spec = new ConnectionSpec.Builder(ConnectionSpec.COMPATIBLE_TLS).build();
        return new OkHttpClient.Builder()
                .retryOnConnectionFailure(false)
                .connectionPool(pool())
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .connectionSpecs(Collections.singletonList(spec))
                .build();
    }

    public static void main(String[] args)
    {
        System.out.println(System.getProperty("java.specification.version"));
    }

}
