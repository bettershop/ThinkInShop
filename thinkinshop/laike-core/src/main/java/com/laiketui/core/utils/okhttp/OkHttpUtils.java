package com.laiketui.core.utils.okhttp;

import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
public class OkHttpUtils
{

    @Autowired
    @Qualifier("apiClient")
    private OkHttpClient apiClient;

    public Object dispatch(String methodType, String url, Map<String, String[]> params) throws IOException
    {
        if ("GET".equalsIgnoreCase(methodType))
        {
            return get(url, params);
        }
        return post(url, params);
    }

    private String get(String url, Map<String, String[]> params) throws IOException
    {
        HttpUrl.Builder builder = HttpUrl.parse(url).newBuilder();
        params.forEach((k, v) ->
        {
            for (String val : v)
            {
                builder.addQueryParameter(k, val);
            }
        });

        Request request = new Request.Builder()
                .url(builder.build())
                .get()
                .build();

        try (Response response = apiClient.newCall(request).execute())
        {
            if (!response.isSuccessful())
            {
                throw new IOException("GET failed, code=" + response.code());
            }
            return response.body().string();
        }
    }

    private String post(String url, Map<String, String[]> params) throws IOException
    {
        FormBody.Builder body = new FormBody.Builder();
        params.forEach((k, v) ->
        {
            for (String val : v)
            {
                body.add(k, val);
            }
        });

        Request request = new Request.Builder()
                .url(url)
                .post(body.build())
                .build();

        try (Response response = apiClient.newCall(request).execute())
        {
            if (!response.isSuccessful())
            {
                throw new IOException("POST failed, code=" + response.code());
            }
            return response.body().string();
        }
    }

}
