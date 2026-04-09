package com.laiketui.common.utils.txun;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.laiketui.common.mapper.BbsVideoModelMapper;
import com.laiketui.core.cache.RedisUtil;
import com.laiketui.domain.plugin.bbs.BbsConfigModel;
import com.laiketui.domain.plugin.bbs.BbsVideoModel;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @Author: liuao
 * @Date: 2025-10-10-10:12
 * @Description: 腾讯云点播工具类
 */
@Component
public class KCPlayerUtils {

    private static final String HMAC_ALGORITHM = "HmacSHA1"; //签名算法
    private final static Charset UTF8 = StandardCharsets.UTF_8;
    private static final String PROCESS_MEDIA_URL = "https://vod.tencentcloudapi.com/";
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final String DEFAULT_CHARACTERS =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int BASE_CHARSET_SIZE = DEFAULT_CHARACTERS.length();

    private static final String CURRENT_DAY_PUSH_COUNT = "currentDayPushCount_";

    private static final int MAX_COUNT = 10000;


    @Autowired
    private BbsVideoModelMapper bbsVideoModelMapper;

    @Autowired
    private RedisUtil redisUtil;

    /**
     * 生成签名
     *
     * @param secretId          密钥id
     * @param secretKey         米娅
     * @param currentTime       当前时间
     * @param signValidDuration 签名有效期（单位）：秒
     * @return
     * @throws Exception
     */
    public String mediaSign(String secretId, String secretKey, long currentTime, int signValidDuration, String appid) throws Exception
    {
        String strSign = "";
        String contextStr = "";
        Integer random = random();
        // 生成原始参数字符串
        long endTime = (currentTime + signValidDuration);
        contextStr += "secretId=" + java.net.URLEncoder.encode(secretId, "utf8");
        contextStr += "&currentTimeStamp=" + currentTime;
        contextStr += "&expireTime=" + endTime;
        contextStr += "&random=" + random;
        contextStr += "&vodSubAppId=" + appid;
        try
        {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(UTF8), mac.getAlgorithm());
            mac.init(secretKeySpec);
            byte[] hash = mac.doFinal(contextStr.getBytes());
            byte[] sigBuf = byteMerger(hash, contextStr.getBytes("utf8"));
            strSign = base64Encode(sigBuf);
            strSign = strSign.replace(" ", "").replace("\n", "").replace("\r", "");
        }
        catch (Exception e)
        {
            logger.error("生成签名失败：{}", e.getMessage());
            throw e;
        }
        return strSign;
    }


    public Boolean ProcessMedia(BbsConfigModel bbsConfigModel, String filed_id)
    {

        logger.info("视频id：{}", filed_id);
        logger.info("视频配置信息：{}", JSON.toJSONString(bbsConfigModel));

        try (CloseableHttpClient httpClient = HttpClients.createDefault())
        {
            String timestamp = String.valueOf(System.currentTimeMillis() / 1000);

            String secret_key = bbsConfigModel.getSecret_key();
            String secret_id = bbsConfigModel.getSecret_id();


            ObjectMapper objectMapper = new ObjectMapper();
            ObjectNode rootNode = objectMapper.createObjectNode();
            HttpPost httpPost = new HttpPost(PROCESS_MEDIA_URL);
            httpPost.addHeader("X-TC-Action", "ProcessMedia");
            httpPost.addHeader("X-TC-Version", "2018-07-17");
            httpPost.addHeader("X-TC-Timestamp", timestamp);
            httpPost.addHeader("X-TC-Region", bbsConfigModel.getRegion());


            if (Objects.nonNull(bbsConfigModel.getDefinition_template_id()) || Objects.nonNull(bbsConfigModel.getSample_template_id()))
            {
                ArrayNode transcodeTaskSetNode = objectMapper.createArrayNode();
                ArrayNode sampleSnapshotTaskSetNode = objectMapper.createArrayNode();

                if (Objects.nonNull(bbsConfigModel.getDefinition_template_id()))
                {
                    ObjectNode definition = objectMapper.createObjectNode();
                    definition.put("Definition", bbsConfigModel.getDefinition_template_id());
                    transcodeTaskSetNode.add(definition);
                }

                if (Objects.nonNull(bbsConfigModel.getSample_template_id()))
                {
                    ObjectNode definition = objectMapper.createObjectNode();
                    definition.put("Definition", bbsConfigModel.getSample_template_id());
                    sampleSnapshotTaskSetNode.add(definition);
                }

                ObjectNode mediaProcessTask = rootNode.putObject("MediaProcessTask");

                //转码任务
                if (!transcodeTaskSetNode.isEmpty())
                {
                    mediaProcessTask.set("TranscodeTaskSet", transcodeTaskSetNode);
                }

                //视频采样截图任务
                if (!sampleSnapshotTaskSetNode.isEmpty())
                {
                    mediaProcessTask.set("SampleSnapshotTaskSet", sampleSnapshotTaskSetNode);
                }
            }

            //视频id
            rootNode.put("FileId", filed_id);

            String bodyJson = rootNode.toString();

            String sign = authSign(secret_key, secret_id, bodyJson, timestamp,"ProcessMedia");
            httpPost.addHeader("Authorization", sign);

            logger.info("Authorization:{}", sign);

            logger.info("媒体处理请求参数：{}", bodyJson);
            logger.info("媒体处理请求头：{}", Arrays.toString(httpPost.getAllHeaders()));


            StringEntity entity = new StringEntity(bodyJson, StandardCharsets.UTF_8);
            entity.setContentType("application/json; charset=utf-8");
            httpPost.setEntity(entity);

            CloseableHttpResponse response = httpClient.execute(httpPost);
            String result = EntityUtils.toString(response.getEntity(), "UTF-8");

            //更新响应数据
            bbsVideoModelMapper.setMsg(filed_id, result);

            logger.info("媒体处理返回结果：{}", JSON.toJSONString(result));
            int code = response.getStatusLine().getStatusCode();
            logger.info("媒体处理返回状态：{}", code);
            if (code == 200)
            {
                return true;
            }
        }
        catch (Exception e)
        {
            logger.error("媒体处理失败：{}", e.getMessage());
        }
        return false;
    }

    /**
     * 视频预热
     * URL 的域名必须已在云点播中注册
     * 单次请求最多指定20个 URL
     * 默认预热配额为每天10000个 URL
     * @param bbsConfigModel
     * @param urlList
     */
    public void pushUrl(BbsConfigModel bbsConfigModel,List<String> urlList)
    {

        logger.info("视频预热开始");

        if (Objects.isNull(bbsConfigModel))
        {
            logger.error("配置为空，中止预热");
            return;
        }
        String key = CURRENT_DAY_PUSH_COUNT + bbsConfigModel.getStore_id();
        if (redisUtil.hasKey(key))
        {
           int count = (int) redisUtil.get(key);
           if (count > MAX_COUNT)
           {
               logger.error("超过当天最大预热次数，中止预热");
             return;
           }
        }

        try(CloseableHttpClient httpClient = HttpClients.createDefault())
        {
            String timestamp = String.valueOf(System.currentTimeMillis() / 1000);

            ObjectMapper objectMapper = new ObjectMapper();
            ObjectNode rootNode = objectMapper.createObjectNode();
            HttpPost httpPost = new HttpPost(PROCESS_MEDIA_URL);
            httpPost.addHeader("X-TC-Action", "PushUrlCache");
            httpPost.addHeader("X-TC-Version", "2018-07-17");
            httpPost.addHeader("X-TC-Timestamp", timestamp);
            httpPost.addHeader("X-TC-Region", bbsConfigModel.getRegion());

            ArrayNode urlArrayNode = objectMapper.valueToTree(urlList);
            rootNode.set("Urls", urlArrayNode);

            String bodyJson = rootNode.toString();

            String sign = authSign(bbsConfigModel.getSecret_key(), bbsConfigModel.getSecret_id(), bodyJson, timestamp,"PushUrlCache");
            httpPost.addHeader("Authorization", sign);

            StringEntity entity = new StringEntity(bodyJson, StandardCharsets.UTF_8);
            entity.setContentType("application/json; charset=utf-8");
            httpPost.setEntity(entity);

            CloseableHttpResponse response = httpClient.execute(httpPost);
            String result = EntityUtils.toString(response.getEntity(), "UTF-8");

            logger.info("result:{}", result);

            JSONObject jsonObject = JSON.parseObject(result);
            JSONObject res = jsonObject.getJSONObject("Response");

            JSONObject error = res.getJSONObject("Error");
            if (Objects.nonNull(error))
            {
                String code = error.getString("Code");
                String message = error.getString("Message");
                switch (code)
                {
                    case "LimitExceeded":
                        logger.error("超过配额限制{}",message);
                        break;
                    case "InvalidParameterValue":
                        logger.error("参数取值错误：{}",message);
                        break;
                    case "UnauthorizedOperation":
                        logger.error("未授权操作：{}",message);
                        break;
                }
                return;
            }

            //一天最多10000个url
            if (redisUtil.hasKey(key))
            {
               int count = (int) redisUtil.get(key);
               logger.info("已用配额：{}",count);
               if (count < MAX_COUNT)
               {
                   //递增+1
                   redisUtil.set(key, count + urlList.size());
               }
            }
            else
            {
                long endOfDa = getEndOfDa();
                redisUtil.set(key, urlList.size(),endOfDa);
            }
        }
        catch (Exception e)
        {
            logger.error("视频预热失败：{}", e.getMessage());
        }
    }


    private String authSign(String secret_key, String secret_id, String body, String timestamp,String action)
    {

        String service = "vod";
        String host = "vod.tencentcloudapi.com";
        String algorithm = "TC3-HMAC-SHA256";


        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        // 注意时区，否则容易出错
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        String date = sdf.format(new Date(Long.parseLong(timestamp + "000")));

        String httpRequestMethod = "POST";
        String canonicalUri = "/";
        String canonicalQueryString = "";
        String canonicalHeaders = "content-type:application/json; charset=utf-8\n"
                + "host:" + host + "\n" + "x-tc-action:" + action.toLowerCase() + "\n";
        String signedHeaders = "content-type;host;x-tc-action";

        try
        {
            //拼接规范请求串
            String hashedRequestPayload = sha256Hex(body);
            String canonicalRequest = httpRequestMethod + "\n" + canonicalUri + "\n" + canonicalQueryString + "\n"
                    + canonicalHeaders + "\n" + signedHeaders + "\n" + hashedRequestPayload;


            logger.info("CanonicalRequest:{}", canonicalRequest);

            //拼接待签名字符串
            String credentialScope = date + "/" + service + "/" + "tc3_request";
            String hashedCanonicalRequest = sha256Hex(canonicalRequest);
            String stringToSign = algorithm + "\n" + timestamp + "\n" + credentialScope + "\n" + hashedCanonicalRequest;

            logger.info("Date:{}", date);
            logger.info("stringToSign:{}", stringToSign);

            //计算签名
            byte[] secretDate = hmac256(("TC3" + secret_key).getBytes(UTF8), date);
            byte[] secretService = hmac256(secretDate, service);
            byte[] secretSigning = hmac256(secretService, "tc3_request");
            String signature = DatatypeConverter.printHexBinary(hmac256(secretSigning, stringToSign)).toLowerCase();

            logger.info("signature:{}", signature);

            //拼接 Authorization
            return algorithm + " " + "Credential=" + secret_id + "/" + credentialScope + ", "
                    + "SignedHeaders=" + signedHeaders + ", " + "Signature=" + signature;
        }
        catch (Exception e)
        {
            logger.error("签名生成失败：{}", e.getMessage());
        }
        return null;
    }


    /**
     * 防盗链生成
     */
    public String generateSecurityUrl(BbsConfigModel bbsConfigModel, String media_url)
    {
        try
        {
            if (Objects.isNull(bbsConfigModel))
            {
                logger.error("云点播配置为空，无法生成防盗链");
                return "";
            }

            //过期时间戳 十六进制小写形式表示
            String expireTime = convertHoursToUnixTimestamp(bbsConfigModel.getExpire_time());

            //除去文件名的那部分路径
            URL url = new URL(media_url);
            String path = url.getPath();
            String dirPath = path.substring(0, path.lastIndexOf('/'));
            if (!path.endsWith("/"))
            {
                dirPath += "/";
            }
            //随机数
            String us = generateSingleString(10);
            //计算签名
            String sign = generateMD5Signature(bbsConfigModel.getSecurity_key(), dirPath, expireTime, us);

            //如果开启了cdn，需要将域名替换
            if (bbsConfigModel.getIs_cdn() == 1)
            {
                media_url = media_url.replaceFirst("https://[^/]+", "https://" + bbsConfigModel.getCdn_url());
            }
            media_url = media_url + "?t=" + expireTime + "&us=" + us + "&sign=" + sign;
        }
        catch (Exception e)
        {
            logger.error("生成防盗播放地址失败：{}", e.getMessage());
        }
        return media_url;
    }

    public static void main(String[] args) throws Exception {
       /*   String originalUrl = "https://1308415585.vod-qcloud.com/3a0eb9cfvodtransgzp1308415585/675e90125145403702313571811/v.f100220.m3u8";
          String newDomain = "newdomain.com";  // 新域名不带协议部分

        // 使用replaceFirst方法，去掉原有的域名并替换为新的域名
        String newUrl = originalUrl.replaceFirst("https://[^/]+", "https://" + newDomain);

        System.out.println(newUrl);*/

         // 创建 URL 对象
       /* URL url = new URL(originalUrl);

        // 获取路径部分（不包含协议、域名和文件名）
        String path = url.getPath();

        // 去掉文件名，保留目录路径
        String dirPath = path.substring(0, path.lastIndexOf('/'));
        if (!path.endsWith("/")) {
            dirPath += "/";
        }*/

      /*  Map<String,Object> map = new HashMap<>();

        map.put("appId",1308415585);
        map.put("fileId","5145403702879328247");



        Map<String,Object> contentInfo = new HashMap<>();
        //播放类型：转码后
        contentInfo.put("audioVideoType","Transcode");
        //转码模板id
        contentInfo.put("transcodeDefinition",100220);
        map.put("contentInfo",contentInfo);
        map.put("currentTimeStamp",System.currentTimeMillis() / 1000);

        String jsonString = JSON.toJSONString(map);

        System.out.println("payload:" + jsonString);

        String token = generateToken("2dDTkFaEbmeohg3pZAGw", jsonString);

        System.out.println(token);
        */
    }


    /**
     * 通过fil_id 播放需要传的播放器签名
     * @param bbsConfigModel
     * @param fileId
     * @return
     */
    public String generatePlaySign(BbsConfigModel bbsConfigModel,String fileId,String playKey)
    {
        try
        {
            String appid = bbsConfigModel.getAppid();
            //当前时间戳
            long currentTimeStamp = System.currentTimeMillis() / 1000;
            //签名过期时间
            long expireTimeStamp = getTimestampByHour(bbsConfigModel.getExpire_time());

            Map<String,Object> map = new HashMap<>();
            map.put("appId",appid);
            map.put("fileId",fileId);
            Map<String,Object> contentInfo = new HashMap<>();
            //播放类型：转码后
            contentInfo.put("audioVideoType","Transcode");
            //转码模板id
            contentInfo.put("transcodeDefinition",bbsConfigModel.getDefinition_template_id());
            map.put("contentInfo",contentInfo);
            map.put("currentTimeStamp",currentTimeStamp);
            map.put("expireTimeStamp",expireTimeStamp);
            String playLoad = JSON.toJSONString(map);

           return generateToken(playKey,playLoad);
        }
        catch (Exception e)
        {
            logger.error("生成播放器签名错误：{}",e.getMessage());
        }
        return null;
    }

    public void getVideoUrl(BbsConfigModel bbsConfigModel, Map<String, Object> map, String videoId)
    {


        BbsVideoModel bbsVideoModel = new BbsVideoModel();
        bbsVideoModel.setFile_id(videoId);
        bbsVideoModel = bbsVideoModelMapper.selectOne(bbsVideoModel);
        if (Objects.nonNull(bbsVideoModel) && Objects.equals(bbsVideoModel.getStatus(),2))
        {
            map.put("cover_img",bbsVideoModel.getCover_img());
        }
        map.put("license_url",bbsConfigModel.getLicense_url());
        String url = "";
        if (redisUtil.hasKey(videoId))
        {
            url = (String) redisUtil.get(videoId);
        }
        else
        {
            int time = bbsConfigModel.getExpire_time() * 60 * 60;
            url = this.generateSecurityUrl(bbsConfigModel,bbsVideoModel.getUrl());
            redisUtil.set(videoId,url,time);
        }
        map.put("video_url", url);
    }


    private String generateMD5Signature(String key, String dirPath, String expireTimeHex, String us) throws NoSuchAlgorithmException
    {
        // 拼接需要加密的字符串
        String data = key + dirPath + expireTimeHex + us;

        // 获取 MD5 MessageDigest 实例
        MessageDigest md = MessageDigest.getInstance("MD5");

        // 计算哈希值
        byte[] hashBytes = md.digest(data.getBytes());

        // 将字节数组转换为十六进制字符串
        StringBuilder hexString = new StringBuilder();
        for (byte b : hashBytes)
        {
            // 将每个字节转换为两位的十六进制数
            String hex = Integer.toHexString(0xFF & b);
            if (hex.length() == 1)
            {
                hexString.append('0'); // 补充零以确保每个字节是两位
            }
            hexString.append(hex);
        }
        return hexString.toString(); // 返回十六进制表示的小写签名
    }


    /**
     * WT 使用的是 Base64Url 编码，而不是标准的 Base64 编码
     * @param input
     * @return
     */
    private String base64UrlEncode(byte[] input)
    {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(input);
    }

    // 使用 HMACSHA256 算法生成签名
    private byte[] hmac256(String data, String key) throws Exception
    {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(secretKeySpec);
        return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
    }

    private byte[] hmac256(byte[] key, String msg) throws Exception
    {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, mac.getAlgorithm());
        mac.init(secretKeySpec);
        return mac.doFinal(msg.getBytes(UTF8));
    }

    private String sha256Hex(String s) throws Exception
    {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] d = md.digest(s.getBytes(UTF8));
        return DatatypeConverter.printHexBinary(d).toLowerCase();
    }

    // 生成 JWT Token
    private String generateToken(String PlayKey, String payload) throws Exception
    {
        // 1. 创建 Header
        String header = "{\"alg\": \"HS256\", \"typ\": \"JWT\"}";

        // 3. Base64Url 编码 Header 和 Payload
        String base64UrlHeader = base64UrlEncode(header.getBytes(StandardCharsets.UTF_8));
        String base64UrlPayload = base64UrlEncode(payload.getBytes(StandardCharsets.UTF_8));

        // 4. 计算签名
        String dataToSign = base64UrlHeader + "." + base64UrlPayload;
        byte[] signature = hmac256(dataToSign, PlayKey);

        // 5. Base64Url 编码签名
        String base64UrlSignature = base64UrlEncode(signature);

        // 6. 拼接最终的 JWT Token
        return base64UrlHeader + "." + base64UrlPayload + "." + base64UrlSignature;
    }



    private String convertHoursToUnixTimestamp(int hoursToAdd)
    {
        long unixTimestamp = getTimestampByHour(hoursToAdd);
        return Long.toHexString(unixTimestamp);
    }


    public long getTimestampByHour(int hour)
    {
        // 获取当前时间
        Calendar calendar = Calendar.getInstance();

        // 将输入的小时数添加到当前时间
        calendar.add(Calendar.HOUR, hour);

        // 获取新的时间
        java.util.Date futureDate = calendar.getTime();

        // 将时间转换为Unix时间戳（秒）
        return futureDate.getTime() / 1000L;
    }

    private byte[] byteMerger(byte[] byte1, byte[] byte2)
    {
        byte[] byte3 = new byte[byte1.length + byte2.length];
        System.arraycopy(byte1, 0, byte3, 0, byte1.length);
        System.arraycopy(byte2, 0, byte3, byte1.length, byte2.length);
        return byte3;
    }

    private String base64Encode(byte[] buffer)
    {
        return Base64.getEncoder().encodeToString(buffer);
    }


    /**
     * 生成单个随机字符串（线程安全）
     */
    public static String generateSingleString(int length)
    {
        StringBuilder sb = new StringBuilder(length);
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int i = 0; i < length; i++)
        {
            sb.append(DEFAULT_CHARACTERS.charAt(random.nextInt(BASE_CHARSET_SIZE)));
        }
        return sb.toString();
    }

        /**
         * 生成随机数
         * @return
         */
    private Integer random()
    {
        return new Random().nextInt(Integer.MAX_VALUE);
    }

    /**
     * 获取当前最大时间的秒数
     * @return
     */
    private long getEndOfDa()
    {
        // 获取今天的日期
        LocalDate today = LocalDate.now();
        // 设置时间为当天的最晚时间 23:59:59.999
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);
        // 转换为ZonedDateTime，并计算秒数
        ZonedDateTime zonedDateTime = endOfDay.atZone(ZoneId.systemDefault());
        // 获取Unix时间戳（秒）
        return zonedDateTime.toEpochSecond();
    }
}
