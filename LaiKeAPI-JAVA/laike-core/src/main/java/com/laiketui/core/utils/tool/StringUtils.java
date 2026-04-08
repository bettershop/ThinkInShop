package com.laiketui.core.utils.tool;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.ErrorCode;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Trick
 * @date 2020/11/17 16:24
 */
public class StringUtils
{

    private static final Pattern EMPTY_ALL = Pattern.compile("\\s*|\t|\r|\n");

    /**
     * 去掉字符串所有空格以及换行
     *
     * @param str -
     * @return String
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/19 15:37
     */
    public static String trim(String str) throws LaiKeAPIException
    {
        try
        {
            if (!isEmpty(str))
            {
                Matcher m = EMPTY_ALL.matcher(str);
                str = m.replaceAll("");
            }
        }
        catch (Exception e)
        {
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.DATA_ILLEGAL_FAIL, "数据格式不正确", "trim");
        }

        return str;
    }

    /**
     * 用于处理%和_查出数据库中所有数据的问题
     *
     * @return String
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/19 15:37
     */
    public static String getKey(String key)
    {
        if (null != key && (key.contains("_") || key.contains("%")))
        {
            char[]        chars  = key.toCharArray();
            StringBuilder result = new StringBuilder();
            for (char ch : chars)
            {
                if (ch == '_' || ch == '%')
                {
                    result.append("/");
                }
                result.append(ch);
            }
            return result.toString();
        }
        return key;
    }


    /**
     * 转换成字符串
     *
     * @param obj -
     * @return String
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/9 11:41
     */
    public static String toString(Object obj) throws LaiKeAPIException
    {
        String str = "";
        try
        {
            if (obj != null)
            {
                str = obj + "";
                if (isEmpty(str))
                {
                    return "";
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return str;
    }


    /**
     * 循环限制
     */
    private final static int OUT_NUM = 50;

    /**
     * 获取指定字符串之间的数据
     *
     * @param str         -
     * @param startTarget -
     * @param endT        -
     * @param separate    - 分隔符,可选
     * @return Map - key: 剔除后的值 value: 剔出来的值
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2020/12/22 13:32
     */
    public static Map<String, String> getSplit(String str, String startTarget, String endT, String separate) throws LaiKeAPIException
    {
        Map<String, String> resultMap = new HashMap<>(16);
        try
        {
            int           indexX = str.indexOf(startTarget);
            int           indexY = str.indexOf(endT);
            StringBuilder value  = new StringBuilder("");
            //数量限制
            int i = 0;
            while (indexX >= 0 && indexY >= 0)
            {
                String temp = str.substring(indexX, indexY + 1);
                value.append(temp);
                //更新字符串
                str = str.replace(temp, "");
                indexX = str.indexOf(startTarget);
                indexY = str.indexOf(endT);
                if (!isEmpty(separate) && indexX >= 0)
                {
                    value.append(separate);
                }
                if (i > OUT_NUM)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.DATA_ILLEGAL_FAIL, "数量超过限制,建议使用正则表达式", "getSplit");
                }
                i++;
            }
            resultMap.put(str, value.toString());
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.DATA_ILLEGAL_FAIL, "数据获取失败", "getSplit");
        }
        return resultMap;
    }


    /**
     * 字符串转数字
     * 转换失败返回 null
     * 不是数字默认为 0
     *
     * @param obj -
     * @return Integer
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/18 9:06
     */
    public static Integer stringParseInt(Object obj) throws LaiKeAPIException
    {
        Integer value = 0;
        try
        {
            String str = obj + "";
            if (isInteger(str))
            {
                value = Integer.parseInt(str);
            }
        }
        catch (NumberFormatException ignored)
        {
            value = null;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            value = null;
        }
        return value;
    }


    /**
     * 字符串是否为空
     *
     * @param obj -
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/19 12:02
     */
    public static boolean isEmpty(Object obj) throws LaiKeAPIException
    {
        try
        {
            if (obj == null)
            {
                return true;
            }
            String  str  = obj + "";
            boolean flag = org.apache.commons.lang3.StringUtils.isBlank(str);
            if (!flag)
            {
                if ("null".equals(str.trim()))
                {
                    flag = true;
                }
            }
            return flag;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.DATA_ILLEGAL_FAIL, "数据格式不正确", "isEmpty");
        }
    }

    public static boolean isNotEmpty(Object obj) throws LaiKeAPIException
    {
        return !isEmpty(obj);
    }

    /**
     * 判断数字是否为空
     *
     * @param str -
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/19 12:02
     */
    public static boolean isEmpty(Integer num) throws LaiKeAPIException
    {
        if (num == null || num < 1)
        {
            return true;
        }
        return false;
    }


    /**
     * 判断字符串值是否相等
     *
     * @param str  -
     * @param str2 -
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/31 11:03
     */
    public static boolean equals(String str, String str2) throws LaiKeAPIException
    {
        try
        {
            return Objects.equals(str, str2);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.DATA_ILLEGAL_FAIL, "数据格式不正确", "isEmpty");
        }
    }

    /**
     * 拼接数组成字符串
     * 出错返回 null
     *
     * @param strs    - 数组
     * @param str     - 拼接方式 例如 ','、'-'
     * @param isLimit - 首尾是否拼接
     * @return String
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/18 17:46
     */
    public static String stringImplode(List<Object> strs, String str, boolean isLimit) throws LaiKeAPIException
    {
        StringBuilder value = new StringBuilder("");
        try
        {
            if (strs != null && strs.size() > 0)
            {
                if (strs.size() > 1)
                {
                    for (int i = 0; i < strs.size(); i++)
                    {
                        String s = strs.get(i).toString();
                        value.append(s);
                        if (i < strs.size() - 1)
                        {
                            value.append(str);
                        }
                    }
                }
                else
                {
                    String temp = strs.get(0) + "";
                    if (StringUtils.isEmpty(temp))
                    {
                        return "";
                    }
                    if (isLimit)
                    {
                        return str + strs.get(0) + str;
                    }
                    return strs.get(0).toString();
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
        if (isLimit)
        {
            value.insert(0, str);
            value.append(str);
        }
        return value.toString();
    }

    public static String stringImplode(List<String> strs, String str) throws LaiKeAPIException
    {
        List<Object> list = new ArrayList<>(strs);
        return stringImplode(list, str, false);
    }

    /**
     * 判断字符串是否为整数
     *
     * @param str -
     * @return boolean
     * @author Trick
     * @date 2020/11/18 9:13
     */
    public static boolean isInteger(String str)
    {
        if (str == null || "".equals(str))
        {
            return false;
        }
        return Pattern.matches("^[-\\+]?[\\d]*$", str);
    }


    /**
     * 占位替换
     *
     * @param str      -
     * @param startStr - 占位符头部
     * @param endStr   - 占位符尾部
     * @return String
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/15 18:02
     */
    public static String replace(String str, String startStr, String endStr, Object... parmas) throws LaiKeAPIException
    {
        try
        {
            if (isEmpty(startStr))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.PARAMATER_ERROR, "占位符参数不正确");
            }
            else if (isEmpty(endStr))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.PARAMATER_ERROR, "占位符参数不正确");
            }
            startStr = makeQueryStringAllRegExp(startStr);
            endStr = makeQueryStringAllRegExp(endStr);
            String  pattern = startStr + "(.*?)" + endStr;
            Pattern r       = Pattern.compile(pattern);
            Matcher m       = r.matcher(str);
            int     num     = 0;
            while (m.find())
            {
                num++;
            }
            if (parmas.length != num)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.PARAMATER_ERROR, "参数数量不正确,期望数量:" + num + "实际数量:" + parmas.length);
            }
            str = str.replaceAll(pattern, "%s");
            str = String.format(str, parmas);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.DATA_ILLEGAL_FAIL, "数据格式不正确", "replace");
        }

        return str;
    }

    /**
     * 占位集合
     *
     * @param str      -
     * @param startStr - 占位符头部
     * @param endStr   - 占位符尾部
     * @return String
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/18 11:44
     */
    public static List<String> replace(String str, String startStr, String endStr) throws LaiKeAPIException
    {
        List<String> parmaList = new ArrayList<>();
        try
        {
            if (isEmpty(startStr))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.PARAMATER_ERROR, "占位符参数不正确");
            }
            else if (isEmpty(endStr))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.PARAMATER_ERROR, "占位符参数不正确");
            }
            startStr = makeQueryStringAllRegExp(startStr);
            endStr = makeQueryStringAllRegExp(endStr);
            String  pattern = startStr + "(.*?)" + endStr;
            Pattern r       = Pattern.compile(pattern);
            Matcher m       = r.matcher(str);
            while (m.find())
            {
                parmaList.add(m.group());
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.DATA_ILLEGAL_FAIL, "数据格式不正确", "replace");
        }

        return parmaList;
    }


    /**
     * 替换特殊字符串
     *
     * @param str    - 原字符串
     * @param newStr - 需要替换成指定的字符串
     * @return String
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/18 14:15
     */
    public static String replaceSpecialStr(String str, String newStr) throws LaiKeAPIException
    {
        try
        {
            str = str.replaceAll("[^0-9a-zA-Z\u4e00-\u9fa5.，,。？“”]+", newStr);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.DATA_ILLEGAL_FAIL, "数据格式不正确", "replaceSpecialStr");
        }

        return str;
    }

    /**
     * 身份证号脱敏处理
     *
     * @param idNumber - 身份证号
     * @return String
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/3/5 9:55
     */
    public static String desensitizedIdNumber(String idNumber) throws LaiKeAPIException
    {
        try
        {
            if (!isEmpty(idNumber))
            {
                if (idNumber.length() == 15)
                {
                    idNumber = idNumber.replaceAll("(\\w{8})\\w*(\\w{4})", "$1******$2");
                }
                if (idNumber.length() == 18)
                {
                    idNumber = idNumber.replaceAll("(\\w{8})\\w*(\\w{4})", "$1*********$2");
                }
            }
            return idNumber;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.DATA_ILLEGAL_FAIL, "数据格式不正确", "desensitizedIdNumber");
        }
    }

    /**
     * 手机号脱敏处理
     *
     * @param phoneNumber - 手机号
     * @return String
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/3/5 9:55
     */
    public static String desensitizedPhoneNumber(String phoneNumber) throws LaiKeAPIException
    {
        try
        {
            if (!isEmpty(phoneNumber))
            {
                phoneNumber = phoneNumber.replaceAll("(\\w{3})\\w*(\\w{4})", "$1****$2");
            }
            return phoneNumber;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.DATA_ILLEGAL_FAIL, "数据格式不正确", "desensitizedPhoneNumber");
        }
    }

    /**
     * 密钥/证书等敏感字段脱敏处理
     *
     * @param secret -
     * @return String
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2026/03/06 11:00
     */
    public static String desensitizedSecret(String secret) throws LaiKeAPIException
    {
        try
        {
            if (isEmpty(secret))
            {
                return secret;
            }
            int len = secret.length();
            if (len <= 4)
            {
                return "****";
            }
            int prefix = Math.min(4, len / 2);
            int suffix = Math.min(4, len - prefix);
            if (prefix + suffix >= len)
            {
                prefix = 1;
                suffix = 1;
            }
            return secret.substring(0, prefix) + "****" + secret.substring(len - suffix);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.DATA_ILLEGAL_FAIL, "数据格式不正确", "desensitizedSecret");
        }
    }

    /**
     * 转义正则特殊字符 （$()*+.[]?\^{}
     *
     * @param str -
     * @return String
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/18 10:52
     */
    public static String makeQueryStringAllRegExp(String str) throws LaiKeAPIException
    {
        if (org.apache.commons.lang3.StringUtils.isBlank(str))
        {
            return str;
        }
        return str.replace("\\", "\\\\").replace("*", "\\*")
                .replace("+", "\\+").replace("|", "\\|")
                .replace("{", "\\{").replace("}", "\\}")
                .replace("(", "\\(").replace(")", "\\)")
                .replace("^", "\\^").replace("$", "\\$")
                .replace("[", "\\[").replace("]", "\\]")
                .replace("?", "\\?").replace(",", "\\,")
                .replace(".", "\\.").replace("&", "\\&");
    }

    /**
     * @param str
     * @param splitter
     * @return
     */
    public static String trim(String str, String splitter)
    {
        String regex = "^" + splitter + "*|" + splitter + "*$";
        return str.replaceAll(regex, "");
    }

    /**
     * @param str
     * @param splitter
     * @return
     */
    public static String ltrim(String str, String splitter)
    {
        String regex = "^" + splitter + "*";
        return str.replaceAll(regex, "");
    }

    /**
     * 删掉左边的空格
     *
     * @param str
     * @param splitter
     * @return
     */
    public static String rtrim(String str, String splitter)
    {
        String regex = splitter + "*$";
        return str.replaceAll(regex, "");
    }

}
