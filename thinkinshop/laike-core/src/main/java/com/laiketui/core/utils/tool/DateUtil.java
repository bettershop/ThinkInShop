package com.laiketui.core.utils.tool;

import com.laiketui.core.common.SplitUtils;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.lktconst.GloabConst;
import org.apache.commons.lang3.time.FastDateFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 日期帮助类
 *
 * @author Trick
 * @date 2020/10/14 14:41
 */
public class DateUtil
{

    private static final Logger logger = LoggerFactory.getLogger(DateUtil.class);


    /**
     * 是否为日期类型
     *
     * @param str -
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/3/11 11:01
     */
    public static boolean isDate(Object str) throws LaiKeAPIException
    {
        if (StringUtils.isEmpty(str))
        {
            return false;
        }
        try
        {
            FastDateFormat fdf = FastDateFormat.getInstance(GloabConst.TimePattern.YMD);
            System.out.println(fdf.format(fdf.parse(String.valueOf(str))));
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
        return true;
    }


    /**
     * 时间戳转换成日期格式字符串
     *
     * @param seconds 精确到秒的时间戳
     * @param format  格式 yyyy-MM-dd HH:mm:ss
     * @return static
     */
    public static String timeStamp2Date(String seconds, String format)
    {
        if (StringUtils.isEmpty(seconds))
        {
            return "";
        }
        if (format == null || format.isEmpty())
        {
            format = GloabConst.TimePattern.YMDHMS;
        }
        FastDateFormat fdf = FastDateFormat.getInstance(format);
        return fdf.format(new Date(Long.parseLong(seconds + "000")));
    }

    /**
     * 日期格式字符串转换成时间戳
     *
     * @param dateStr 字符串日期
     * @param format  如：yyyy-MM-dd HH:mm:ss
     * @return String
     */
    public static String date2TimeStamp(String dateStr, String format)
    {
        try
        {
            FastDateFormat fdf = FastDateFormat.getInstance(format);
            return String.valueOf(fdf.parse(dateStr).getTime() / 1000);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return "";
    }


    /**
     * 日期比较
     * date>parseDate
     * (!date>parseDate) 相当于 <=的效果
     *
     * @param date      - 日期
     * @param parseDate - 被比较的日期
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/7 14:12
     */
    public static boolean dateCompare(Date date, Date parseDate) throws LaiKeAPIException
    {
        try
        {
            return date.after(parseDate);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("日期比较 异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.BUSY_NETWORK, "日期比较失败", "dateCompare");
        }
    }

    public static void main(String[] args)
    {
        String s     = "2023-05-22 00:00:00";
        String s1    = "2023-05-23 00:00:00";
        Date   date  = dateFormateToDate(s, GloabConst.TimePattern.YMD);
        Date   date1 = dateFormateToDate(s1, GloabConst.TimePattern.YMD);
        System.out.println(DateUtil.dateCompare(date, date1));
    }

    /**
     * 日期比较
     * date>parseDate
     * (!date>parseDate) 相当于 <=的效果
     *
     * @param dateStr      - 日期
     * @param parseDateStr - 被比较的日期
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/7 14:12
     */
    public static boolean dateCompare(String dateStr, String parseDateStr) throws LaiKeAPIException
    {
        try
        {
            FastDateFormat date = FastDateFormat.getInstance(GloabConst.TimePattern.YMDHMS);
            try
            {
                return date.parse(dateStr).after(date.parse(parseDateStr));
            }
            catch (ParseException parseException)
            {
                date = FastDateFormat.getInstance(GloabConst.TimePattern.YMD);
                return date.parse(dateStr).after(date.parse(parseDateStr));
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("日期比较 异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.BUSY_NETWORK, "日期比较失败", "dateCompare");
        }
    }

    /**
     * 日期比较
     * date>parseDate
     * (!date>parseDate) 相当于 <=的效果，与上面同名的方法相比增加了等于的情况
     *
     * @param dateStr      - 日期
     * @param parseDateStr - 被比较的日期
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/7 14:12
     */
    public static boolean dateCompare2(String dateStr, String parseDateStr) throws LaiKeAPIException
    {
        try
        {
            FastDateFormat date = FastDateFormat.getInstance(GloabConst.TimePattern.YMDHMS);
            try
            {
                return date.parse(dateStr).after(date.parse(parseDateStr)) || date.parse(dateStr).equals(date.parse(parseDateStr));
            }
            catch (ParseException parseException)
            {
                date = FastDateFormat.getInstance(GloabConst.TimePattern.YMD);
                return date.parse(dateStr).after(date.parse(parseDateStr)) || date.parse(dateStr).equals(date.parse(parseDateStr));
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("日期比较 异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.BUSY_NETWORK, "日期比较失败", "dateCompare");
        }
    }

    /**
     * 日期比较
     * date>parseDate
     * (!date>parseDate) 相当于 <=的效果
     *
     * @param dateStr      - 日期
     * @param parseDateStr - 被比较的日期
     * @param dateFormate  - 字符格式
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/7 14:12
     */
    public static boolean dateCompare(String dateStr, String parseDateStr, String dateFormate) throws LaiKeAPIException
    {
        try
        {
            FastDateFormat date = FastDateFormat.getInstance(dateFormate);
            return date.parse(dateStr).after(date.parse(parseDateStr));
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("日期比较 异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.BUSY_NETWORK, "日期比较失败", "dateCompare");
        }
    }

    /**
     * 字符串时间格式化
     *
     * @param dateStr -
     * @param format  -
     * @return String
     * @author Trick
     * @date 2020/11/25 14:01
     */
    public static String dateFormate(String dateStr, String format)
    {
        try
        {
            if (StringUtils.isNotEmpty(dateStr))
            {
                Date date = dateFormateToDate(dateStr, GloabConst.TimePattern.YMDHMS);
                return dateFormate(date, format);
            }
            return dateStr;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 字符串时间格式化
     *
     * @param dateStr    -
     * @param format     - 最终返回的格式
     * @param dateFormat - 转换中途的格式
     * @return String
     * @author Trick
     * @date 2020/11/25 14:01
     */
    public static String dateFormate(String dateStr, String format, String dateFormat)
    {
        try
        {
            FastDateFormat fdf = FastDateFormat.getInstance(format);
            return fdf.format(dateFormateToDate(dateStr, dateFormat));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 时间时间格式化
     *
     * @param date   -
     * @param format -
     * @return java.lang.String
     * @author Trick
     * @date 2020/12/4 16:57
     */
    public static String dateFormate(Date date, String format)
    {
        try
        {
            if (date != null)
            {
                FastDateFormat fdf = FastDateFormat.getInstance(format);
                return fdf.format(date);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 字符串时间格式化
     *
     * @param dateStr -
     * @param format  -
     * @return Date
     * @author Trick
     * @date 2020/11/25 14:04
     */
    public static Date dateFormateToDate(String dateStr, String format)
    {
        try
        {
            if (!StringUtils.isEmpty(dateStr))
            {
                FastDateFormat fdf = FastDateFormat.getInstance(format);
                //Sharding-Jdbc 日期格式特殊处理
                if (format.equals(GloabConst.TimePattern.YMDHMS))
                {
                    if (dateStr.contains("T"))
                    {
                        dateStr = dateStr.replace("T", " ");
                    }
                    int lastMh  = dateStr.lastIndexOf(":");
                    int firstMh = dateStr.indexOf(":");
                    if (lastMh == firstMh)
                    {
                        dateStr = dateStr.concat(":00");
                    }
                }
                return fdf.parse(dateStr);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 日期格式化
     *
     * @param date   -
     * @param format -
     * @return Date
     * @author Trick
     * @date 2020/11/25 14:04
     */
    public static Date dateFormateToDate(Date date, String format)
    {
        try
        {
            if (date != null)
            {
                FastDateFormat fdf = FastDateFormat.getInstance(format);
                return dateFormateToDate(fdf.format(date), format);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 获得当天最小时间
     * 传昨天的日期则是昨天的,以此类推
     *
     * @param date -
     * @return String
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/10/30 16:09
     */
    public static String getStartOfDay(Date date) throws LaiKeAPIException
    {
        try
        {
            LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(date.getTime()),
                    ZoneId.systemDefault());
            LocalDateTime startOfDay = localDateTime.with(LocalTime.MIN);
            Date          startDate  = Date.from(startOfDay.atZone(ZoneId.systemDefault()).toInstant());
            return FastDateFormat.getInstance(GloabConst.TimePattern.YMDHMS).format(startDate);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("获得当天最小时间异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.BUSY_NETWORK, "获得当天最小时间失败", "getStartOfDay");
        }
    }

    /**
     * 获得当天最大时间
     * 传昨天的日期则是昨天的,以此类推
     *
     * @param date -
     * @return String
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/10/30 16:09
     */
    public static String getEndOfDay(Date date) throws LaiKeAPIException
    {
        try
        {
            LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(date.getTime()),
                    ZoneId.systemDefault());
            LocalDateTime endOfDay = localDateTime.with(LocalTime.MAX);
            Date          endDate  = Date.from(endOfDay.atZone(ZoneId.systemDefault()).toInstant());
            return FastDateFormat.getInstance(GloabConst.TimePattern.YMDHMS).format(endDate);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("获得当天最小时间异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.BUSY_NETWORK, "获得当天最大时间出错", "getEndOfDay");
        }
    }


    /**
     * 增减日期
     *
     * @param addDay -
     * @return Date
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/24 15:38
     */
    public static Date getAddDate(int addDay) throws LaiKeAPIException
    {
        return getAddDate(new Date(), addDay);
    }

    /**
     * 增减日期
     *
     * @param addDay -
     * @return Date
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/24 15:38
     */
    public static Date getAddDate(Date time, int addDay) throws LaiKeAPIException
    {
        try
        {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(time);
            calendar.add(Calendar.DAY_OF_MONTH, addDay);
            return calendar.getTime();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("增加日期异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.BUSY_NETWORK, "增减日期失败", "getAddDate");
        }
    }

    public static Date getAddDateByMonth(Date time, int addMonth) throws LaiKeAPIException
    {
        try
        {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(time);
            calendar.add(Calendar.MONTH, addMonth);
            return calendar.getTime();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("增加日期异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.BUSY_NETWORK, "增减日期失败", "getAddDate");
        }
    }


    public static Date getAddDateByYear(Date time, int addYear) throws LaiKeAPIException
    {
        try
        {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(time);
            calendar.add(Calendar.YEAR, addYear);
            return calendar.getTime();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("增加日期异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.BUSY_NETWORK, "增减日期失败", "getAddDate");
        }
    }

    /**
     * 日期 增/减秒数
     *
     * @param time      - 需要增加/减少的时间
     * @param addSecond - 秒
     * @return Date
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/24 15:38
     */
    public static Date getAddDateBySecond(Date time, int addSecond) throws LaiKeAPIException
    {
        try
        {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(time);
            calendar.add(Calendar.SECOND, addSecond);
            return calendar.getTime();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("增加日期秒数异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.BUSY_NETWORK, "增加日期秒数失败", "getAddDateBySecond");
        }
    }

    /**
     * 获得当月第一天
     *
     * @param date -
     * @return String
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/10/30 16:09
     */
    public static String getMonthFirstDay(Date date) throws LaiKeAPIException
    {
        try
        {
            Calendar cale = Calendar.getInstance();
            cale.setTime(date);
            cale.set(Calendar.DAY_OF_MONTH, cale.getActualMinimum(Calendar.DAY_OF_MONTH));
            String firstMonth = FastDateFormat.getInstance(GloabConst.TimePattern.YMD).format(cale.getTime());
            String endDay     = "00:00:00";
            return firstMonth + " " + endDay;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("获得当月最后一天异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.BUSY_NETWORK, "获得当月最后一天出错", "getFirstMonthDay");
        }
    }

    /**
     * 获得当月最后一天
     *
     * @param date -
     * @return String
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/10/30 16:09
     */
    public static String getMonthLastDay(Date date) throws LaiKeAPIException
    {
        try
        {
            Calendar cale = Calendar.getInstance();
            cale.setTime(date);
            cale.set(Calendar.DAY_OF_MONTH, cale.getActualMaximum(Calendar.DAY_OF_MONTH));
            String firstMonth = FastDateFormat.getInstance(GloabConst.TimePattern.YMD).format(cale.getTime());
            String endDay     = "23:59:59";
            return firstMonth + " " + endDay;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("获得当月最后一天异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.BUSY_NETWORK, "获得当月最后一天出错", "getFirstMonthDay");
        }
    }

    /**
     * 日期按月增加
     *
     * @param date -
     * @return Date
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/10/30 16:09
     */
    public static Date getAddMonth(Date date, int monthNum) throws LaiKeAPIException
    {
        try
        {
            Calendar cale = Calendar.getInstance();
            cale.setTime(date);
            cale.add(Calendar.MONTH, monthNum);
            return cale.getTime();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("日期按月增加 异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.BUSY_NETWORK, "日期处理失败", "getAddMonth");
        }
    }

    /**
     * 获取两个时间戳之间相差的几天
     *
     * @param time1 - 年月日 单位秒
     * @param time2 - 年月日 单位秒
     * @return int - 只算整数，多余的舍去
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/4 11:54
     */
    public static int getBetweenDate(long time1, long time2) throws LaiKeAPIException
    {
        try
        {
            return getBetweenDate(time1, time2, false);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("获取两个时间戳之间相差的几天异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.BUSY_NETWORK, "网络异常", "getBetweenDate");
        }
    }


    /**
     * 获取两个时间戳之间相差的几天
     *
     * @param time1 - 年月日 单位秒
     * @param time2 - 年月日 单位秒
     * @param isOut - 是否不足一天按一天计算
     * @return int
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/4 11:54
     */
    public static int getBetweenDate(long time1, long time2, boolean isOut) throws LaiKeAPIException
    {
        try
        {
            long resultDay = (time1 - time2) / 86400;
            if (isOut)
            {
                return Math.abs(new BigDecimal(resultDay + "").setScale(0, BigDecimal.ROUND_UP).intValue());
            }
            else
            {
                return Math.abs(new BigDecimal(resultDay + "").intValue());
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("获取两个时间戳之间相差的几天异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.BUSY_NETWORK, "网络异常", "getBetweenDate");
        }
    }


    /**
     * 获取 年月周日 开始、结束时间
     *
     * @param type    - 1=今天 2= 本周 3=本月 4=本年
     * @param isStart - 是否获取开始时间,否则获取最后的时间
     * @return String
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/2/18 10:41
     */
    public static String getSpanDate(int type, boolean isStart) throws LaiKeAPIException
    {
        return getSpanDate(type, new Date(), isStart);
    }

    /**
     * 获取 年月周日 开始、结束时间
     *
     * @param type    - 1=今天 2= 本周 3=本月 4=本年
     * @param date    - 日期
     * @param isStart - 是否获取开始时间,否则获取最后的时间
     * @return String
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2022-06-08 16:35:24
     */
    public static String getSpanDate(int type, Date date, boolean isStart) throws LaiKeAPIException
    {
        try
        {
            Date     resultDate = DateUtil.dateFormateToDate(date, GloabConst.TimePattern.YMDHMS);
            Calendar cal        = Calendar.getInstance();
            cal.setTime(resultDate);
            switch (type)
            {
                case 1:
                    break;
                case 2:
                    if (isStart)
                    {
                        cal.add(Calendar.WEEK_OF_MONTH, 0);
                        cal.set(Calendar.DAY_OF_WEEK, 2);
                    }
                    else
                    {
                        cal.set(Calendar.DAY_OF_WEEK, cal.getActualMaximum(Calendar.DAY_OF_WEEK));
                        cal.add(Calendar.DAY_OF_WEEK, 1);
                    }
                    resultDate = cal.getTime();
                    break;
                case 3:
                    if (isStart)
                    {
                        cal.add(Calendar.MONTH, 0);
                        cal.set(Calendar.DAY_OF_MONTH, 1);
                    }
                    else
                    {
                        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
                    }
                    resultDate = cal.getTime();
                    break;
                case 4:
                    if (isStart)
                    {
                        return new SimpleDateFormat("yyyy").format(resultDate) + "-01-01";
                    }
                    else
                    {
                        cal.set(Calendar.MONTH, cal.getActualMaximum(Calendar.MONTH));
                        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
                        resultDate = cal.getTime();
                    }
                    break;
                default:
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.PARAMATER_ERROR, "参数错误");
            }
            if (isStart)
            {
                return getStartOfDay(resultDate);
            }
            else
            {
                return getEndOfDay(resultDate);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("获取时间跨度 异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.BUSY_NETWORK, "网络异常", "getSpanDate");
        }
    }


    /**
     * 时间单位枚举
     */
    public enum TimeType
    {
        /**
         * 年 月 日 时 分 秒
         */
        YEAR, MONTH, DAY, TIME, MINUTE, SECOND;
    }

    /**
     * 将秒转换成任意类型
     *
     * @param seconds - 秒
     * @return BigDecimal
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/10/20 11:54
     */
    public static BigDecimal dateConversionType(long seconds, TimeType type) throws LaiKeAPIException
    {
        try
        {
            BigDecimal value;
            BigDecimal secondsTime = new BigDecimal(seconds);

            switch (type)
            {
                case DAY:
                    value = secondsTime.divide(new BigDecimal("24").multiply(new BigDecimal("3600")), 2, BigDecimal.ROUND_HALF_UP);
                    break;
                case TIME:
                    value = secondsTime.divide(new BigDecimal("3600"), 2, BigDecimal.ROUND_HALF_UP);
                    break;
                case MINUTE:
                    value = secondsTime.divide(new BigDecimal("60"), 2, BigDecimal.ROUND_HALF_UP);
                    break;
                case SECOND:
                    return secondsTime;
                default:
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.PARAMATER_ERROR, String.format("暂不支持【%s】的转换", type));
            }
            return value;
        }
        catch (Exception e)
        {
            logger.error("时间换算 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.BUSY_NETWORK, "网络异常", "dateConversion");
        }
    }

    public static BigDecimal dateConversionType(int seconds, TimeType type) throws LaiKeAPIException
    {
        return dateConversionType(Long.parseLong(seconds + ""), type);
    }

    /**
     * 时间差换算
     *
     * @param time - 两个时间差的时间戳/单位秒
     * @param type -
     * @return long
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/2/22 17:18
     */
    public static long dateConversion(long time, TimeType type) throws LaiKeAPIException
    {
        try
        {
            long value;

            switch (type)
            {
                case DAY:
                    value = time / (24 * 3600);
                    break;
                case TIME:
                    value = time % (24 * 3600) / 3600;
                    break;
                case MINUTE:
                    value = time % 3600 / 60;
                    break;
                case SECOND:
                    value = time % 60;
                    break;
                default:
                    return 0;
            }
            return value;
        }
        catch (Exception e)
        {
            logger.error("时间换算 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.BUSY_NETWORK, "网络异常", "dateConversion");
        }
    }

    /**
     * 时间差换算
     *
     * @param time - 两个时间差的时间戳/单位秒
     * @param type -
     * @return long
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/2/22 17:18
     */
    public static long dateConversionForAution(long time, TimeType type) throws LaiKeAPIException
    {
        try
        {
            return dateConversion(new BigDecimal(time), type).longValue();
        }
        catch (Exception e)
        {
            logger.error("时间换算 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.BUSY_NETWORK, "网络异常", "dateConversion");
        }
    }


    public static BigDecimal dateConversion(BigDecimal time, TimeType type) throws LaiKeAPIException
    {
        try
        {
            BigDecimal value;

            switch (type)
            {
                case DAY:
                    value = time.multiply(new BigDecimal("24")).multiply(new BigDecimal("60")).multiply(new BigDecimal("60"));
                    break;
                case TIME:
                    value = time.multiply(new BigDecimal("60")).multiply(new BigDecimal("60"));
                    break;
                case MINUTE:
                    value = time.multiply(new BigDecimal("60"));
                    break;
                case SECOND:
                    value = time;
                    break;
                default:
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.PARAMATER_ERROR, String.format("暂不支持【%s】的转换", type));
            }
            return value;
        }
        catch (Exception e)
        {
            logger.error("时间换算 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.BUSY_NETWORK, "网络异常", "dateConversion");
        }
    }

    /**
     * 时间换算-秒
     *
     * @param num  -
     * @param type -
     * @return int -秒
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/4/6 15:15
     */
    public static int dateConversion(int num, TimeType type) throws LaiKeAPIException
    {
        try
        {
            int value;

            switch (type)
            {
                case DAY:
                    value = num * 24 * 60 * 60;
                    break;
                case TIME:
                    value = num * 60 * 60;
                    break;
                case MINUTE:
                    value = num * 60;
                    break;
                default:
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.PARAMATER_ERROR, String.format("暂不支持【%s】的转换", type));
            }
            return value;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("时间换算 异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.BUSY_NETWORK, "网络异常", "dateConversion");
        }
    }

    /**
     * 计算两个时间差多少 /单位
     *
     * @param time           - 精确到秒的时间戳
     * @param differenceTime - 差数
     * @param type           -
     * @return long - 返回相差秒
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/4/2 12:32
     */
    public static long dateConversion(long time, long differenceTime, TimeType type) throws LaiKeAPIException
    {
        try
        {
            long value;

            long s    = time - differenceTime;
            long day  = s / (24 * 60 * 60);
            long hour = (s / (60 * 60) - day * 24);
            long min  = ((s / (60)) - day * 24 * 60 - hour * 60);

            switch (type)
            {
                case DAY:
                    value = day;
                    break;
                case TIME:
                    value = hour;
                    break;
                case MINUTE:
                    value = min;
                    break;
                case SECOND:
                    value = s;
                    break;
                default:
                    return 0;
            }
            return value;
        }
        catch (Exception e)
        {
            logger.error("时间换算-计算两个时间差 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.BUSY_NETWORK, "网络异常", "dateConversion");
        }
    }

    /**
     * 计算两个时间相差的月份，不足一月按照一月计算
     *
     * @param gradeEndList - 拆分过后的开始时间 年月日
     * @param nowDateList  - 拆分过后的结束时间 年月日
     * @return int
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/3/16 11:50
     */
    public static int calculationMonthNum(String[] gradeEndList, String[] nowDateList) throws LaiKeAPIException
    {
        int num;
        try
        {
            //根据要升级的等级计算费用
            if (Integer.parseInt(nowDateList[1]) < Integer.parseInt(gradeEndList[1]))
            {
                //当前月份小于过期月份
                num = Math.abs(Integer.parseInt(nowDateList[0]) - Integer.parseInt(gradeEndList[0])) * 12 +
                        Math.abs(Integer.parseInt(nowDateList[1]) - Integer.parseInt(gradeEndList[1]));
            }
            else
            {
                num = Math.abs(Integer.parseInt(nowDateList[0]) - Integer.parseInt(gradeEndList[0])) * 12 -
                        Math.abs(Integer.parseInt(nowDateList[1]) - Integer.parseInt(gradeEndList[1]));
            }
            if (num == 0)
            {
                //升级本月,至少一个月
                num = 1;
            }
            else
            {
                //不足一月按一月计算
                int day = Integer.parseInt(gradeEndList[2]) - Integer.parseInt(nowDateList[2]);
                if (day > 0)
                {
                    num += 1;
                }
            }
            return num;
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("计算两个时间相差的月份，不足一月按照一月计算 异常 " + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.BUSY_NETWORK, "网络异常", "calculationMonthNum");
        }
    }

    /**
     * 取得当前时间戳（精确到秒）
     *
     * @return String
     */
    public static String timeStamp()
    {
        long time = System.currentTimeMillis();
        return String.valueOf(time / 1000);
    }

    /**
     * 取得当前时间戳（精确到秒）
     *
     * @return long
     */
    public static long getTime()
    {
        long time = System.currentTimeMillis();
        return time / 1000;
    }

    /**
     * 判断时间是否在时间段内 *
     *
     * @param date         当前时间 yyyy-MM-dd HH:mm:ss
     * @param strDateBegin 开始时间 00:00:00
     * @param strDateEnd   结束时间 00:05:00
     * @return
     */
    public static boolean isInDate(Date date, String strDateBegin, String strDateEnd)
    {
        SimpleDateFormat sdf     = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String           strDate = sdf.format(date);
        // 截取当前时间时分秒
        int strDateH = Integer.parseInt(strDate.substring(11, 13));
        int strDateM = Integer.parseInt(strDate.substring(14, 16));
        int strDateS = Integer.parseInt(strDate.substring(17, 19));
        // 截取开始时间时分秒
        int strDateBeginH = Integer.parseInt(strDateBegin.substring(0, 2));
        int strDateBeginM = Integer.parseInt(strDateBegin.substring(3, 5));
        int strDateBeginS = Integer.parseInt(strDateBegin.substring(6, 8));
        // 截取结束时间时分秒
        int strDateEndH = Integer.parseInt(strDateEnd.substring(0, 2));
        int strDateEndM = Integer.parseInt(strDateEnd.substring(3, 5));
        int strDateEndS = Integer.parseInt(strDateEnd.substring(6, 8));
        if ((strDateH >= strDateBeginH && strDateH <= strDateEndH))
        {
            // 当前时间小时数在开始时间和结束时间小时数之间
            if (strDateH > strDateBeginH && strDateH < strDateEndH)
            {
                return true;
                // 当前时间小时数等于开始时间小时数，分钟数在开始和结束之间
            }
            else if (strDateH == strDateBeginH && strDateM >= strDateBeginM && strDateM <= strDateEndM)
            {
                return true;
                // 当前时间小时数等于开始时间小时数，分钟数等于开始时间分钟数，秒数在开始和结束之间
            }
            else if (strDateH == strDateBeginH && strDateM == strDateBeginM && strDateS >= strDateBeginS && strDateS <= strDateEndS)
            {
                return true;
            }
            // 当前时间小时数大等于开始时间小时数，等于结束时间小时数，分钟数小等于结束时间分钟数
            else if (strDateH >= strDateBeginH && strDateH == strDateEndH && strDateM <= strDateEndM)
            {
                return true;
                // 当前时间小时数大等于开始时间小时数，等于结束时间小时数，分钟数等于结束时间分钟数，秒数小等于结束时间秒数
            }
            else if (strDateH >= strDateBeginH && strDateH == strDateEndH && strDateM == strDateEndM && strDateS <= strDateEndS)
            {
                return true;
            }
            else
            {
                return false;
            }
        }
        else
        {
            return false;
        }
    }

    /**
     * 思路：将有交集的情况列出,若不符合有交集的情况,则无交集
     * 有交集的两种情况
     * 1.第一个时间段的开始时间在第二个时间段的开始时间和结束时间当中
     * 2.第二个时间段的开始时间在第一个时间段的开始时间和结束时间当中
     * 判断两个时间段是否有交集
     *
     * @param leftStartDate  第一个时间段的开始时间
     * @param leftEndDate    第一个时间段的结束时间
     * @param rightStartDate 第二个时间段的开始时间
     * @param rightEndDate   第二个时间段的结束时间
     * @return 若有交集, 返回true, 否则返回false
     */
    public static boolean hasOverlap(Date leftStartDate, Date leftEndDate, Date rightStartDate, Date rightEndDate)
    {

        return ((leftStartDate.getTime() >= rightStartDate.getTime())
                && leftStartDate.getTime() < rightEndDate.getTime())
                ||
                ((leftStartDate.getTime() > rightStartDate.getTime())
                        && leftStartDate.getTime() <= rightEndDate.getTime())
                ||
                ((rightStartDate.getTime() >= leftStartDate.getTime())
                        && rightStartDate.getTime() < leftEndDate.getTime())
                ||
                ((rightStartDate.getTime() > leftStartDate.getTime())
                        && rightStartDate.getTime() <= leftEndDate.getTime());

    }

    /**
     * 获取过去任意天内的日期数组
     *
     * @param intervals intervals天内
     * @return 日期数组
     * @author sunH
     * @date 2022/10/09 10:12
     */
    public static ArrayList<String> sevenDateList(int intervals)
    {
        ArrayList<String> pastDaysList = new ArrayList<>();
        for (int i = intervals - 1; i >= 0; i--)
        {
            pastDaysList.add(getPastDate(i));
        }
        return pastDaysList;
    }

    /**
     * 获取过去第几天的日期
     *
     * @param past
     * @return
     * @author sunH
     * @date 2022/10/09 10:12
     */
    public static String getPastDate(int past)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR) - past);
        Date             today  = calendar.getTime();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        return format.format(today);
    }

    /**
     * 获取过去几月的日期数组
     *
     * @param intervals intervals天内
     * @return 日期数组
     * @author sunH
     * @date 2022/10/09 10:12
     */
    public static ArrayList<String> monthDateList(int intervals)
    {
        ArrayList<String> pastDaysList = new ArrayList<>();
        for (int i = intervals - 1; i >= 0; i--)
        {
            pastDaysList.add(getLast12Months(i));
        }
        return pastDaysList;
    }

    /**
     * 获取过去几月
     *
     * @param i
     * @return
     * @author sunH
     * @date 2022/10/09 10:12
     */
    public static String getLast12Months(int i)
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
        Calendar         c   = Calendar.getInstance();
        c.setTime(new Date());
        c.add(Calendar.MONTH, -i);
        Date m = c.getTime();
        return sdf.format(m);
    }

    /**
     * 获取区间内所有日期
     *
     * @param start
     * @param end
     * @return
     * @author sunH
     * @date 2023/01/03 10:12
     */
    public static List<String> getIntervalDate(String start, String end)
    {
        SimpleDateFormat format      = new SimpleDateFormat("yyyy-MM-dd");
        List<String>     betweenList = new ArrayList<>();
        try
        {
            Calendar startDay = Calendar.getInstance();
            startDay.setTime(format.parse(start));
            startDay.add(Calendar.DATE, -1);
            while (true)
            {
                startDay.add(Calendar.DATE, 1);
                Date   newDate = startDay.getTime();
                String newend  = format.format(newDate);
                betweenList.add(newend);
                if (end.equals(newend))
                {
                    break;
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        System.out.println(betweenList);
        return betweenList;
    }

    /**
     * 当前是星期几 java中一周第一天为星期天，所以1代表星期日，2代表星期一，以此类推，7代表星期6
     *
     * @param date -
     * @author Trick
     * @date 2023/2/10 14:44
     */
    public static int getWeek(Date date)
    {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        // 当前是星期几 java中一周第一天为星期天，所以1代表星期日，2代表星期一，以此类推，7代表星期6
        int[] weekSort = new int[]{7, 1, 2, 3, 4, 5, 6};
        int   week     = c.get(Calendar.DAY_OF_WEEK);
        return weekSort[week - 1];
    }

    /**
     * 获取输入时间到当前时间之间的时间 YYYY-MM-DD
     *
     * @param date
     * @return
     */
    public static List<Date> createDays(Date date)
    {
        List<Date> datas       = new ArrayList<>();
        Date       currentDate = dateFormateToDate(new Date(), GloabConst.TimePattern.YMD);
        while (date.before(currentDate))
        {
            date = getAddDate(date, 1);
            datas.add(dateFormateToDate(date, GloabConst.TimePattern.YMD));
        }

        return datas;
    }

    /**
     * 剩余秒 格式化成 年-月-日 时:分:秒
     *
     * @param midTime    - 剩余秒
     * @param dateFormat - 日期格式
     * @return StringBuilder
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022/8/17 10:49
     */
    public static String formatSurplusSeconds(long midTime, String dateFormat) throws LaiKeAPIException
    {
        String resultStr = "";
        try
        {
            StringBuilder sb = new StringBuilder();
            long          dd = midTime / 60 / 60 / 24 % 60;
            long          hh = midTime / 60 / 60 % 60;
            long          mm = midTime / 60 % 60;
            long          ss = midTime % 60;

            switch (dateFormat)
            {
                case GloabConst.TimePattern.HMS:
                    hh = midTime / 60 / 60;
                    sb.append(hh).append(SplitUtils.MH).append(mm).append(SplitUtils.MH).append(ss);
                    break;
                default:
                    sb.append(dd).append("天").append(hh).append("小时").append(mm).append("分钟").append(ss).append("秒");
                    break;
            }
            resultStr = sb.toString();
        }
        catch (Exception e)
        {
            logger.error("剩余秒 格式化 异常 ", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.BUSY_NETWORK, "网络异常", "formatSurplusSeconds");
        }
        return resultStr;
    }

    /**
     * 剩余秒 格式化成 年-月-日 时:分:秒
     *
     * @param midTime - 剩余秒
     * @return StringBuilder
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022/8/17 10:49
     */
    public static String formatDate(String midTime) throws LaiKeAPIException
    {
        String resultStr = "";
        try
        {
            // 定义输入和输出日期的格式
            DateTimeFormatter inputFormatter  = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日", Locale.CHINA);

            // 解析日期字符串为 LocalDate 对象
            LocalDate date = LocalDate.parse(midTime, inputFormatter);

            // 格式化 LocalDate 对象为所需的输出格式
            resultStr = date.format(outputFormatter);

        }
        catch (Exception e)
        {
            logger.error("剩余秒 格式化 异常 ", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.BUSY_NETWORK, "网络异常", "formatSurplusSeconds");
        }
        return resultStr;
    }

    /**
     * 获取输入时间到当前时间之间的时间 YYYY-MM
     *
     * @param date
     * @return
     */
    public static List<Date> createMonths(Date date)
    {
        List<Date> datas       = new ArrayList<>();
        Date       currentDate = dateFormateToDate(new Date(), GloabConst.TimePattern.YM);
        while (date.before(currentDate))
        {
            date = getAddMonth(date, 1);
            datas.add(dateFormateToDate(date, GloabConst.TimePattern.YM));
        }

        return datas;
    }

    //上周一
    public static Date geLastWeekMonday(Date date)
    {
        Calendar cal = Calendar.getInstance();
        cal.setTime(getThisWeekMonday(date));
        cal.add(Calendar.DATE, -7);
        return cal.getTime();
    }

    //本周一
    public static Date getThisWeekMonday(Date date)
    {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        // 获得当前日期是一个星期的第几天
        int dayWeek = cal.get(Calendar.DAY_OF_WEEK);
        if (1 == dayWeek)
        {
            cal.add(Calendar.DAY_OF_MONTH, -1);
        }
        // 设置一个星期的第一天，按中国的习惯一个星期的第一天是星期一
        cal.setFirstDayOfWeek(Calendar.MONDAY);
        // 获得当前日期是一个星期的第几天
        int day = cal.get(Calendar.DAY_OF_WEEK);
        // 根据日历的规则，给当前日期减去星期几与一个星期第一天的差值
        cal.add(Calendar.DATE, cal.getFirstDayOfWeek() - day);
        return cal.getTime();
    }

    //下周一
    public static Date getNextWeekMonday(Date date)
    {
        Calendar cal = Calendar.getInstance();
        cal.setTime(getThisWeekMonday(date));
        cal.add(Calendar.DATE, 7);
        return cal.getTime();
    }

    /**
     * 时间转换秒
     *
     * @param time
     * @return
     */
    public static Integer convToSecond(Integer time, Integer type)
    {
        int second = 0;
        if (time == null)
        {
            return GloabConst.LktConfig.LOGIN_EXISTENCE_TIME;
        }
        switch (type)
        {
            case 1:
                second = time * 60;
                break;
            case 2:
                second = time * 3600;
                break;
            //case 3、4
        }
        return second;
    }
}
