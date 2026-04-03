package com.laiketui.core.utils.okhttp;

import com.laiketui.core.utils.tool.OKHttpsHelper;
import okhttp3.*;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * 请求外部api
 *
 * @author Trick
 * @date 2020/11/28 11:29
 */
public class HttpUtils
{

    static
    {
        System.setProperty("jdk.tls.client.protocols", "TLSv1");
    }

    private static final Logger logger = LoggerFactory.getLogger(OkHttpUtils.class);

    public static String get(String url)
    {
        String responseBody = "";
        Request request = new Request.Builder()
                .url(url)
                .build();
        Response response = null;
        try
        {
            OkHttpClient okHttpClient = upgradeOkHttpClient();
            response = okHttpClient.newCall(request).execute();
            if (response.isSuccessful())
            {
                return response.body().string();
            }
        }
        catch (Exception e)
        {
            logger.error("root get 错误 >> ex = {}", ExceptionUtils.getStackTrace(e));
        }
        finally
        {
            if (response != null)
            {
                response.close();
            }
        }
        return responseBody;
    }

    public static String post(String url)
    {
        return post(url, "{}");
    }

    public static String post(String url, Map<String, String[]> queries)
    {
        String           responseBody = "";
        FormBody.Builder builder      = new FormBody.Builder();
        //添加参数
        if (queries != null && queries.keySet().size() > 0)
        {
            for (String key : queries.keySet())
            {
                String[] vals = queries.get(key);
                if (vals != null && vals.length > 0)
                {
                    for (String val : vals)
                    {
                        builder.add(key, val);
                    }
                }
            }
        }

        System.out.println(builder.toString());

        Request request = new Request.Builder()
                .url(url)
                .post(builder.build())
                .build();
        Response response = null;
        try
        {
            OkHttpClient okHttpClient = upgradeOkHttpClient();
            response = okHttpClient.newCall(request).execute();
            if (response.isSuccessful())
            {
                return response.body().string();
            }
        }
        catch (Exception e)
        {
            logger.error("root post 错误 >> ex = {}", ExceptionUtils.getStackTrace(e));
        }
        finally
        {
            if (response != null)
            {
                response.close();
            }
        }
        return responseBody;
    }

    public static String post(String url, String json)
    {
        String      responseBody = "";
        MediaType   mediaType    = MediaType.parse("application/json;charset=utf-8");
        RequestBody requestBody  = RequestBody.create(mediaType, String.valueOf(json));

        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
        Response response = null;
        try
        {
            OkHttpClient okHttpClient = upgradeOkHttpClient();
            response = okHttpClient.newCall(request).execute();
            if (response.isSuccessful())
            {
                return response.body().string();
            }
        }
        catch (Exception e)
        {
            logger.error("okhttp3 post json 错误 >> ex = {}", ExceptionUtils.getStackTrace(e));
        }
        finally
        {
            if (response != null)
            {
                response.close();
            }
        }
        return responseBody;
    }

    public static String post(HttpPost httpPost)
    {
        String result = "";
        try
        {
            CloseableHttpClient httpClient = HttpClients.createDefault();

            CloseableHttpResponse response = httpClient.execute(httpPost);
            result = EntityUtils.toString(response.getEntity(), "UTF-8");
            logger.info("result:{}", result);
        }
        catch (Exception e)
        {
            logger.error("post请求调用失败：{}", e.getMessage());
        }
        return result;
    }

    public static InputStream getFile(String url, String domain)
    {
        Request request = new Request.Builder()
                .url(url)
                .addHeader("referer", domain)
                .build();
        Response response = null;
        try
        {
            OkHttpClient okHttpClient = upgradeOkHttpClient();
            response = okHttpClient.newCall(request).execute();
            if (response.isSuccessful())
            {
                if (response.body() == null)
                {
                    return null;
                }
                return cloneInputStream(response.body().byteStream());
            }
        }
        catch (Exception e)
        {
            logger.error("okhttp3 getFile downloadFile 错误 >> ex = {}", ExceptionUtils.getStackTrace(e));
        }
        finally
        {
            if (response != null)
            {
                response.close();
            }
        }
        return null;
    }

    public static InputStream getFile(String url)
    {
        Request request = new Request.Builder()
                .url(url)
                .build();
        Response response = null;
        try
        {
            OkHttpClient okHttpClient = upgradeOkHttpClient();
            response = okHttpClient.newCall(request).execute();
            if (response.isSuccessful())
            {
                if (response.body() == null)
                {
                    return null;
                }
                return cloneInputStream(response.body().byteStream());
            }
        }
        catch (Exception e)
        {
            logger.error("okhttp3 getFile downloadFile 错误 >> ex = {}", ExceptionUtils.getStackTrace(e));
        }
        finally
        {
            if (response != null)
            {
                response.close();
            }
        }
        return null;
    }

    public static InputStream postFile(String url, String json)
    {
        MediaType   mediaType   = MediaType.parse("application/json;charset=utf-8");
        RequestBody requestBody = RequestBody.create(mediaType, String.valueOf(json));

        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
        Response response = null;
        try
        {
            OkHttpClient okHttpClient = upgradeOkHttpClient();
            response = okHttpClient.newCall(request).execute();
            if (response.isSuccessful())
            {
                if (response.body() == null)
                {
                    return null;
                }
                return cloneInputStream(response.body().byteStream());
            }
        }
        catch (Exception e)
        {
            logger.error("获取外链文件出错-post ", e);
        }
        finally
        {
            if (response != null)
            {
                response.close();
            }
        }
        return null;
    }

    /**
     * 复制流
     *
     * @param input -
     * @return InputStream
     * @author Trick
     * @date 2022/4/20 18:29
     */
    private static InputStream cloneInputStream(InputStream input)
    {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream())
        {
            byte[] buffer = new byte[1024];
            int    len;
            while ((len = input.read(buffer)) > -1)
            {
                baos.write(buffer, 0, len);
            }
            baos.flush();
            return new ByteArrayInputStream(baos.toByteArray());
        }
        catch (IOException e)
        {
            logger.error("复制流出错", e);
            return null;
        }
    }

    /**
     * 竞拍二维码兼容问题(2023/08/29)
     *
     * @param url
     * @param domain
     * @return
     */
    public static InputStream getCodeFile(String url, String domain)
    {
        Request request = new Request.Builder()
                .url(url)
                .addHeader("referer", domain)
                .build();
        Response response = null;
        try
        {
            OkHttpClient okHttpClient = builderHttpClientTwo();
            response = okHttpClient.newCall(request).execute();
            if (response.isSuccessful())
            {
                if (response.body() == null)
                {
                    return null;
                }
                return cloneInputStream(response.body().byteStream());
            }
        }
        catch (Exception e)
        {
            logger.error("okhttp3 getFile downloadFile 错误 >> ex = {}", ExceptionUtils.getStackTrace(e));
        }
        finally
        {
            if (response != null)
            {
                response.close();
            }
        }
        return null;
    }

    /**
     * okHttp 3.7.1 4.8.11 版本兼容配置
     *
     * @return
     */
    public static OkHttpClient upgradeOkHttpClient()
    {
        try
        {
            return OKHttpsHelper.upgradeOkHttpClient();
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    private static OkHttpClient builderHttpClient()
    {
        try
        {
            return OKHttpsHelper.builderHttpClient();
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * 兼容okhttp3 3.4.1版本
     *
     * @return
     */
    private static OkHttpClient builderHttpClientTwo()
    {
        try
        {
            return OKHttpsHelper.builderHttpClientTwo();
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }


}
