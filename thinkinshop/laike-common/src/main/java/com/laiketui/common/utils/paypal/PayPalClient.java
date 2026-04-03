package com.laiketui.common.utils.paypal;


import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.paypal.core.PayPalEnvironment;
import com.paypal.core.PayPalHttpClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
public class PayPalClient
{

    public PayPalHttpClient client(String mode, String clientId, String clientSecret)
    {
        log.info("mode={}, clientId={}, clientSecret={}", mode, clientId, clientSecret);
        PayPalEnvironment environment = mode.equals("live") ? new PayPalEnvironment.Live(clientId, clientSecret) : new PayPalEnvironment.Sandbox(clientId, clientSecret);
        return new PayPalHttpClient(environment);
    }

    /**
     * 格式化输出JSON对象
     * @param jo JSON对象
     * @param pre 前缀（缩进）
     * @return 格式化后的字符串
     */
    public String prettyPrint(JSONObject jo, String pre)
    {
        StringBuilder pretty = new StringBuilder();

        for (String key : jo.keySet())
        {
            pretty.append(String.format("%s%s: ", pre, StringUtils.capitalize(key)));
            Object value = jo.get(key);

            if (value instanceof JSONObject)
            {
                pretty.append(prettyPrint((JSONObject) value, pre + "\t"));
            }
            else if (value instanceof JSONArray)
            {
                JSONArray array = (JSONArray) value;
                for (int i = 0; i < array.size(); i++)
                {
                    Object item = array.get(i);
                    if (item instanceof JSONObject)
                    {
                        pretty.append(String.format("\n%s\t%d:\n", pre, i + 1));
                        pretty.append(prettyPrint((JSONObject) item, pre + "\t\t"));
                    }
                    else
                    {
                        pretty.append(String.format("\n%s\t%d: %s\n", pre, i + 1, item.toString()));
                    }
                }
            }
            else
            {
                pretty.append(String.format("%s\n", value.toString()));
            }
        }

        return pretty.toString();
    }
}


