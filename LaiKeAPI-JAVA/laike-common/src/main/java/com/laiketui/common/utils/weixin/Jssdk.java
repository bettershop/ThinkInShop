package com.laiketui.common.utils.weixin;

import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.laiketui.core.cache.RedisUtil;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.core.utils.okhttp.HttpUtils;
import com.laiketui.core.utils.tool.DataUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 微信小程序授权
 *
 * @author Trick
 * @date 2020/10/9 14:58
 */
@Component
public class Jssdk
{

    private final static Logger logger = LoggerFactory.getLogger(Jssdk.class);

    @Autowired
    private RedisUtil redisutil;

    /**
     * 获取微信 ticket
     *
     * @param url -
     * @return String
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/10/9 15:18
     */
    public Map<String, String> getTicket(String url, String appid, String secret) throws LaiKeAPIException
    {
        Map<String, String> ret;
        String              ticket = "";
        try
        {
            Object cacheObj = redisutil.get(GloabConst.RedisHeaderKey.WEIXIN_TICKET);
            if (cacheObj != null)
            {
                ticket = cacheObj + "";
            }
            else
            {
                //重新获取ticket
                String token        = getAccessToken(appid, secret);
                String getTicketUrl = String.format(GloabConst.WeiXinUrl.TICKET_JSAPI_GET_URL, token);
                ticket = HttpUtils.get(getTicketUrl);
                if (ticket != null)
                {
                    Map<String, Object> resultMap = JSON.parseObject(ticket, new TypeReference<Map<String, Object>>()
                    {
                    });
                    String errcode = "errcode";
                    String ok      = "0";
                    String code    = resultMap.containsKey(errcode) + "";
                    if (!StringUtils.isEmpty(code) && ok.equals(code))
                    {
                        String successKey = "ticket";
                        ticket = resultMap.get(successKey) + "";
                        //ticket保存到redis
                        redisutil.set(GloabConst.RedisHeaderKey.WEIXIN_TICKET, ticket, GloabConst.WeiXinUrl.ACCESSTOKEN_EXPIRES);
                    }
                    else
                    {
                        logger.error("token获取失败" + resultMap.get("errmsg"));
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.GET_ACCESSTOKEN_FAIL, "ticket获取失败 errmsg=" + resultMap.get("errmsg"), "getTicket");
                    }
                }
                else
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.BUSY_NETWORK, "网络异常", "getTicket");
                }
            }

            //ticket签名
            ret = sign(ticket, url);
            System.out.println(JSON.toJSONString(ret));
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("ticket获取失败" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.GET_TICKET_FAIL, "ticket获取失败", "getTicket");
        }

        return ret;
    }


    /**
     * 获取token
     *
     * @param appid  -
     * @param secret -
     * @return String
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/10/9 16:06
     */
    public String getAccessToken(String appid, String secret) throws LaiKeAPIException
    {
        String accessToken;
        try
        {
            String key = GloabConst.RedisHeaderKey.WEIXIN_ACCESS_TOKEN + appid;
            Object cacheObj = redisutil.get(key);
            long   time     = redisutil.getExpire(key);
            logger.info(" 微信token还有 {} 秒失效", (time == 0 ? "无限" : time + ""));
            //在5分钟后失效的token 都重新获取 避免短时间失效的token影响业务使用,微信规定的是5分钟内新老token都有效，那么只需要修改更新token的间隔时间
            //小于等于5分钟即可
            long    fourMins       = 4 * 60;
            boolean tokenIsExpired = (time > 0 && time <= fourMins) || time < 0;
            if (cacheObj == null || tokenIsExpired)
            {
                //重新获取token
                accessToken = getWeixinToken(appid, secret);
            }
            else
            {
                //失效剩余时间大于4分钟的返回给业务操作使用
                accessToken = cacheObj + "";
            }

        }
        catch (LaiKeAPIException e)
        {
            logger.error("token获取失败", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.GET_ACCESSTOKEN_FAIL, "token获取失败", "getAccessToken");
        }
        catch (Exception e)
        {
            logger.error("token获取异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.GET_ACCESSTOKEN_FAIL, "token获取失败", "getAccessToken");
        }
        return accessToken;
    }

    /**
     * 获取微信接口访问 accessToken
     *
     * @param appid
     * @param secret
     * @return
     * @throws LaiKeAPIException
     */
    private String getWeixinToken(String appid, String secret) throws LaiKeAPIException
    {
        String accessToken = null;
        //重新获取token
        String url = GloabConst.WeiXinUrl.ACCESSTOKEN_GET_URL;
        logger.info("微信Token请求地址:{}", url);
        String jsonInput = String.format(
                "{\"grant_type\":\"client_credential\",\"appid\":\"%s\",\"secret\":\"%s\",\"force_refresh\":false}",
                appid, secret);
        String res = HttpUtils.post(url,jsonInput);
        Map<String, Object> resultMap = JSON.parseObject(res, new TypeReference<Map<String, Object>>()
        {
        });
        logger.info("返回Token信息：{}", JSON.toJSONString(resultMap));
        String errcode = "errcode";
        if (!resultMap.containsKey(errcode))
        {
            String successKey = "access_token";
            accessToken = resultMap.get(successKey) + "";
            int expireTime = 7200;
            if (resultMap.get("expires_in") != null)
            {
                expireTime = DataUtils.getIntegerVal(resultMap, "expires_in");
            }
            //token保存到redis
            logger.info("本次获取的Access_token:{},有效期：{} 秒", accessToken, expireTime);
            redisutil.set(GloabConst.RedisHeaderKey.WEIXIN_ACCESS_TOKEN + appid, accessToken, expireTime);
        }
        else
        {
            logger.error("token获取失败" + resultMap.get("errmsg"));
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.GET_ACCESSTOKEN_FAIL, "token获取失败 errmsg=" + resultMap.get("errmsg"), "getWeixinToken");
        }
        return accessToken;
    }

    public Map<String, String> sign(String jsapiTicket, String url)
    {
        Map<String, String> ret       = new HashMap<String, String>(16);
        String              nonceStr  = create_nonce_str();
        String              timestamp = create_timestamp();
        String              string1;
        String              signature = "";

        //注意这里参数名必须全部小写，且必须有序
        string1 = "jsapi_ticket=" + jsapiTicket +
                "&noncestr=" + nonceStr +
                "&timestamp=" + timestamp +
                "&url=" + url;
        System.out.println(string1);

        try
        {
            MessageDigest crypt = MessageDigest.getInstance("SHA-1");
            crypt.reset();
            crypt.update(string1.getBytes(StandardCharsets.UTF_8));
            signature = byteToHex(crypt.digest());
        }
        catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }

        ret.put("url", url);
        ret.put("jsapi_ticket", jsapiTicket);
        ret.put("nonceStr", nonceStr);
        ret.put("timestamp", timestamp);
        ret.put("signature", signature);

        return ret;
    }


    /**
     * 数组转换16进制数据
     *
     * @param hash -
     * @return String
     * @author Trick
     * @date 2020/11/28 11:16
     */
    private String byteToHex(final byte[] hash)
    {
        Formatter formatter = new Formatter();
        for (byte b : hash)
        {
            formatter.format("%02x", b);
        }
        String result = formatter.toString();
        formatter.close();
        return result;
    }

    private static String create_nonce_str()
    {
        return UUID.randomUUID().toString();
    }

    private static String create_timestamp()
    {
        return Long.toString(System.currentTimeMillis() / 1000);
    }
}
