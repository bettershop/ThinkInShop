package com.laiketui.core.utils.tool;

import com.alibaba.fastjson2.JSONObject;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.core.utils.okhttp.HttpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * 腾讯定位服务
 *
 * @author Trick
 * @date 2020/10/10 10:54
 */
public class TengxunMapUtil
{
    private static final Logger logger = LoggerFactory.getLogger(TengxunMapUtil.class);
    private static final String API_URL = "https://apis.map.qq.com/ws/geocoder/v1/";

    public static void main(String[] args)
    {
        //getlatAndLng("ID3BZ-ROX6J-4GYFS-XEEXK-3J4FT-KNF3F", "湖南省长沙市芙蓉区高龄小区");
    }
    /**
     * 腾讯地图通过定位反查详细地址
     *
     * @param key       密钥
     * @param latitude  维度
     * @param longitude 经度
     * @return String - 返回json https://lbs.qq.com/service/webService/webServiceGuide/webServiceGcoder
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/10/10 11:15
     */
    public static String getAddress(String key, String latitude, String longitude) throws LaiKeAPIException
    {
        String success  = "0";
        String location = latitude + "," + longitude;
        String url      = GloabConst.TengXunUrl.TX_GET_ADDRESS;
        url = String.format(url, location, key);
        String json = HttpUtils.get(url);

        if (json.length() > 0)
        {
            JSONObject obj        = JSONObject.parseObject(json);
            String     returnCode = obj.get("status").toString();
            if (success.equals(returnCode))
            {
                System.out.println(obj);
                return obj.getJSONObject("result").toString();
            }
            else
            {
                String msg = obj.get("message").toString();
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.TENGXUN_API_ERROR, msg, "getAddress");
            }
        }
        else
        {
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.READ_NOT_DATA, "未获取取到数据", "getAddress");
        }

    }

    /**
     * 获取地址经纬度
     *
     * @param key     -
     * @param address -
     * @return java.util.Map<java.lang.String, java.lang.String>
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/10 14:20
     */
    public static Map<String, String> getlatAndLng(String key, String address) throws LaiKeAPIException
    {
        Map<String, String> resultMap = new HashMap<>(16);
        try
        {
            String success = "0";
            String url     = GloabConst.TengXunUrl.TX_GET_LONGITUDE_LATITUDE;
            url = String.format(url, address, key);
            String json = HttpUtils.get(url);
            logger.error("腾讯地图获取位置经纬度结果{}", json);
            if (json.length() > 0)
            {
                JSONObject obj        = JSONObject.parseObject(json);
                String     returnCode = obj.get("status").toString();
                if (success.equals(returnCode))
                {
                    logger.debug(obj.toString());
                    resultMap.put("lng", obj.getJSONObject("result").getJSONObject("location").get("lng").toString());
                    resultMap.put("lat", obj.getJSONObject("result").getJSONObject("location").get("lat").toString());
                    return resultMap;
                }
                else
                {
                    String msg = obj.get("message").toString();
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.TENGXUN_API_ERROR, msg, "getlatAndLongitude");
                }
            }
            else
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.READ_NOT_DATA, "未获取取到数据", "getlatAndLongitude");
            }
        }
        catch (Exception e)
        {
            logger.error("获取地址经纬度 异常 ", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.BUSY_NETWORK, "网络异常", "getlatAndLongitude");
        }
    }


    /**
     * 根据经纬度获取距离
     *
     * @param key  - api密钥
     * @param to   - 起点坐标 格式[维度,精度]
     * @param from - 终点坐标 格式[维度,精度]
     * @return String
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/10/12 13:53
     */
    public static String getStoreDstance(String key, String to, String from) throws LaiKeAPIException
    {
        String success = "0";
        String url     = GloabConst.TengXunUrl.TX_GET_DISTANCEMATRIX;
        try
        {
            url = String.format(url, from, to, key);
            String json = HttpUtils.get(url);

            if (json.length() > 0)
            {
                JSONObject obj        = JSONObject.parseObject(json);
                String     returnCode = obj.get("status").toString();
                if (success.equals(returnCode))
                {
                    System.out.println(obj);
                    //获取距离
                    JSONObject resultObj = obj.getJSONObject("result");

                    return resultObj.toString();
                }
                else
                {
                    String msg = obj.get("message").toString();
                    logger.info("获取附近店铺失败 原因:" + msg);
//                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.TENGXUN_API_ERROR, msg, "getStore");
                }
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取附近店铺 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.BUSY_NETWORK, "获取距离信息失败", "getStore");
        }

        return null;
    }

}
