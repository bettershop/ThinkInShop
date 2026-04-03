package com.laiketui.common.utils.redisGEO;

/**
 * Created by sunH on 2020/2/7.
 */
public class GeoUtils
{

    public static boolean isAvailablePointInRedis(Double longitude, Double latitude)
    {
        if (longitude == null || latitude == null)
        {
            return false;
        }

        return (longitude >= -180 && longitude <= 180) && (latitude >= -85.05112878 && latitude <= 85.05112878);
    }


}
