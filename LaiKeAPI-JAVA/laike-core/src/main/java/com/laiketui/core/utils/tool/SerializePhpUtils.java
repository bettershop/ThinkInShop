package com.laiketui.core.utils.tool;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.lktconst.GloabConst;
import org.phprpc.util.AssocArray;
import org.phprpc.util.PHPSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * php序列化.反序列化
 *
 * @author Trick
 * @date 2020/10/13 11:22
 */
public class SerializePhpUtils
{
    private static final Logger logg = LoggerFactory.getLogger(SerializePhpUtils.class);

    public static void main(String[] args) throws InvocationTargetException, IllegalAccessException
    {
//        String line = "a:1:{i:0;a:5:{s:3:\"one\";i:1;s:7:\"freight\";s:1:\"0\";s:8:\"Tfreight\";s:2:\"20\";s:4:\"name\";s:372:\"北京市,天津市,河北省,山西省,内蒙古自治区,辽宁省,吉林省,黑龙江省,上海市,江苏省,浙江省,安徽省,福建省,江西省,山东省,河南省,湖北省,湖南省,广东省,广西省,海南省,重庆市,四川省,贵州省,云南省,西藏自治区,陕西省,甘肃省,青海省,宁夏自治区,新疆省,台湾省,香港特别行政区,澳门\";s:3:\"two\";i:1;}}";
//        Map<String, Object> freightList = JSON.parseObject(line, new TypeReference<Map<String, Object>>() {
//        });
//        System.out.println(line);
//        List<Map<String, Object>> map = JSON.parseObject(line, new TypeReference<List<Map<String, Object>>>() {
//        });
//        System.out.println(SerializePhpUtils.getUnSerializeByFreight("a:10:{s:6:\"zhekou\";s:1:\"1\";s:14:\"different_type\";s:1:\"0\";s:12:\"sibling_type\";r:3;s:7:\"sibling\";r:2;s:15:\"indirect_m_type\";r:3;s:12:\"s_dengjiname\";s:6:\"初级\";s:9:\"different\";r:2;s:8:\"direct_m\";r:2;s:10:\"indirect_m\";r:2;s:13:\"direct_m_type\";r:3;}"));
//        System.out.println(SerializePhpUtils.getUnserializeObj(line, Map.class));
//        Map<String, Object> intialMap = new HashMap<>(16);
//        intialMap.put("yj", "2");
//        intialMap.put("unit", "包");
//        intialMap.put("sj", "10");
//        intialMap.put("kucun", "100");
//        intialMap.put("cbj", "1");
//        intialMap.put("stockWarn", "10");
//        intialMap.put("msrp", "10");
//        String s = SerializePhpUtils.JavaSerializeByPhp(intialMap);
//        Map unserializeObj = SerializePhpUtils.getUnserializeObj("a:2:{s:15:\"尺码_LKT_3195\";s:10:\"L_LKT_3196\";s:15:\"颜色_LKT_2953\";s:15:\"红色_LKT_2956\";}");
//        System.out.println(unserializeObj);
//        String context = "a:3:{i:0;a:4:{s:9:\"gradeName\";s:6:\"青铜\";s:6:\"jtfybl\";i:1;s:4:\"fxzk\";i:10;s:6:\"ztfybl\";i:1;}i:1;a:4:{s:9:\"gradeName\";s:6:\"黄金\";s:6:\"jtfybl\";i:1;s:4:\"fxzk\";i:1;s:6:\"ztfybl\";i:10;}i:2;a:4:{s:9:\"gradeName\";s:6:\"钻石\";s:6:\"jtfybl\";i:1;s:4:\"fxzk\";i:1;s:6:\"ztfybl\";i:1;}}";
//        System.out.println(SerializePhpUtils.getUnserializeArray(context,Map.class));
        doWOR();
    }

    public static void doWOR()
    {
        String context = "a:3:{i:0;a:4:{s:9:\"gradeName\";s:6:\"青铜\";s:6:\"jtfybl\";i:1;s:4:\"fxzk\";i:10;s:6:\"ztfybl\";i:1;}i:1;a:4:{s:9:\"gradeName\";s:6:\"黄金\";s:6:\"jtfybl\";i:1;s:4:\"fxzk\";i:1;s:6:\"ztfybl\";i:10;}i:2;a:4:{s:9:\"gradeName\";s:6:\"钻石\";s:6:\"jtfybl\";i:1;s:4:\"fxzk\";i:1;s:6:\"ztfybl\";i:1;}}";
        System.out.println(getUnserializeToList(context));

    }


    /**
     * 反php序列化数组
     * lkt_sign_config.continuity
     * <p>
     * [{"3":"10"},{"30":"30"}]
     *
     * @param content -a:2:{i:0;a:1:{i:3;s:2:"10";}i:1;a:1:{i:30;s:2:"30";}}
     * @return List -
     */
    public static List<Map<Integer, Integer>> getSignConfigByContinuity(String content) throws LaiKeAPIException
    {
        PHPSerializer               p          = new PHPSerializer();
        List<Map<Integer, Integer>> resultList = new ArrayList<>();
        try
        {
            if (StringUtils.isEmpty(content))
            {
                return null;
            }
            AssocArray       array1      = (AssocArray) p.unserialize(content.getBytes(GloabConst.Chartset.UTF_8));
            List<AssocArray> dataMapList = DataUtils.cast(array1.toArrayList());
            if (dataMapList == null)
            {
                return null;
            }
            for (AssocArray array : dataMapList)
            {
                Map<Integer, Integer> resultMap = new HashMap<>(16);
                Map<Integer, byte[]>  map       = DataUtils.cast(array.toHashMap());
                if (map == null)
                {
                    return null;
                }
                for (Integer key : map.keySet())
                {
                    resultMap.put(key, Integer.parseInt(new String(map.get(key))));
                }
                resultList.add(resultMap);
            }
            return resultList;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logg.error("反序列化object: " + content + " 失败！！！");
        }
        return null;
    }

    /**
     * 反序列化php对象
     * lkt_group_product.group_level
     * lkt_group_open.group_level
     *
     * @param content -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/16 18:31
     */
    public static Map<Integer, Object> getGroupByGroupLevel(String content) throws LaiKeAPIException
    {
        Map<Integer, Object> resultMap = new HashMap<>(16);
        try
        {
            if (StringUtils.isEmpty(content))
            {
                return resultMap;
            }
            PHPSerializer        p     = new PHPSerializer();
            AssocArray           array = (AssocArray) p.unserialize(content.getBytes(GloabConst.Chartset.UTF_8));
            Map<Integer, Object> map   = array.toHashMap();

            for (Integer key : map.keySet())
            {
                resultMap.put(key, new String((byte[]) map.get(key)));
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logg.error("反序列化object: " + content + " 失败！！！");
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.BUSY_NETWORK, "网络异常", "getGroupByGroupLevel");
        }
        return resultMap;
    }

    /**
     * 反序列化php对象
     * lkt_distribution_config.sets
     *
     * @param content -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/16 18:31
     */
    public static Map<String, Object> getDistributionConfigBySets(String content) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            if (StringUtils.isEmpty(content))
            {
                return resultMap;
            }
            PHPSerializer       p   = new PHPSerializer();
            Map<String, Object> map = DataUtils.cast(p.unserialize(content.getBytes(GloabConst.Chartset.UTF_8), Map.class));

            List<Integer> oneList  = new ArrayList<>();
            List<Integer> teamList = new ArrayList<>();

            if (map != null)
            {
                if (map.containsKey("one"))
                {
                    AssocArray oneObj = (AssocArray) map.get("one");
                    if (oneObj != null)
                    {
                        oneList = DataUtils.cast(oneObj.toArrayList());
                    }
                    AssocArray teamObj = (AssocArray) map.get("team");
                    teamList = DataUtils.cast(teamObj.toArrayList());
                }

                for (String key : map.keySet())
                {
                    if (map.get(key) instanceof byte[])
                    {
                        map.put(key, new String((byte[]) map.get(key)));
                    }
                }
                resultMap.putAll(map);
            }
            resultMap.put("one", oneList);
            resultMap.put("team", teamList);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logg.error("反序列化lkt_distribution_config.sets: {} 失败！！！", content);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.BUSY_NETWORK, "网络异常", "getDistributionConfigBySets");
        }
        return resultMap;
    }

    /**
     * 序列化 lkt_freight.freight
     * 结构:
     * Map<integer,map<string,object>>
     * or
     * List<Map<String, Object>>
     *
     * @param content - a:1:{s:1:"0";a:2:{s:3:"one";s:2:"10";s:4:"name";s:39:"北京市,内蒙古自治区,上海市,";}}
     * @return Map - {one=5, name=北京市,天津市,江西省,河南省,湖北省,湖南省,四川省,青海省...}{...}...
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/2 9:37
     */
    public static Map<Integer, LinkedHashMap<String, Object>> getUnSerializeByFreight(String content) throws LaiKeAPIException
    {
        try
        {
            Map<Integer, LinkedHashMap<String, Object>> resultDataMap = new HashMap<>(16);

            PHPSerializer        p       = new PHPSerializer();
            Object               obj     = p.unserialize(content.getBytes(StandardCharsets.UTF_8), Map.class);
            Map<Integer, Object> dataMap = (LinkedHashMap<Integer, Object>) obj;

            for (Integer key : dataMap.keySet())
            {
                AssocArray                    assocArray = (AssocArray) dataMap.get(key);
                LinkedHashMap<String, Object> resultMap  = assocArray.toLinkedHashMap();

                resultMap.replaceAll((k, v) ->
                {
                    if (!StringUtils.isInteger(resultMap.get(k) + ""))
                    {
                        return new String((byte[]) resultMap.get(k));
                    }
                    return resultMap.get(k);
                });

                String json = JSON.toJSONString(resultMap);
                LinkedHashMap<String, Object> map = JSON.parseObject(json, new TypeReference<LinkedHashMap<String, Object>>()
                {
                });
                for (String k : map.keySet())
                {
                    if ("name".equals(k))
                    {
                        String name = map.get("name").toString().replace("&nbsp;", "");
                        name = StringUtils.trim(name, ",");
                        map.put("name", name);
                    }
                }
                resultDataMap.put(key, map);
            }

            return resultDataMap;
        }
        catch (Exception e)
        {
            logg.error("反序列化 lkt_freight.freight 异常: ", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.BUSY_NETWORK, "网络异常", "getPhpSerializeToHashMap");
        }
    }


    /**
     * 反序列化php对象
     * lkt_distribution_grade.sets
     *
     * @param content -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/16 18:31
     */
    public static Map<String, Object> getDistributionGradeBySets(String content) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            PHPSerializer       p   = new PHPSerializer();
            Map<String, Object> map = DataUtils.cast(p.unserialize(content.getBytes(GloabConst.Chartset.UTF_8), Map.class));

            AssocArray levelmoneyObj = (AssocArray) map.get("levelmoney");
            if (levelmoneyObj != null)
            {
                Map<Integer, Object> levelmoneyMap = levelmoneyObj.toLinkedHashMap();
                if (levelmoneyMap != null && !levelmoneyMap.isEmpty())
                {
                    levelmoneyMap.replaceAll((k, v) -> new String((byte[]) levelmoneyMap.get(k)));
                }
                resultMap.put("levelmoney", levelmoneyObj);
            }
            AssocArray levelObj = (AssocArray) map.get("levelobj");
            if (levelObj != null)
            {
                Map<String, Object> levelObjMap = levelObj.toLinkedHashMap();
                if (levelObjMap != null && !levelObjMap.isEmpty())
                {
                    levelObjMap.replaceAll((k, v) -> new String((byte[]) levelObjMap.get(k)));
                }
                resultMap.put("levelobj", levelObj);
            }
            resultMap.put("s_dengjiname", new String((byte[]) map.get("s_dengjiname")));
            //折扣
            BigDecimal zhekou = new BigDecimal("0");
            if (!StringUtils.isEmpty(map.get("zhekou").toString()))
            {
                zhekou = new BigDecimal(new String((byte[]) map.get("zhekou")));
            }
            BigDecimal priceType = new BigDecimal("0");
            if (!StringUtils.isEmpty(map.get("price_type")))
            {
                priceType = new BigDecimal(map.get("price_type").toString());
            }
            //级差金额
            BigDecimal different     = new BigDecimal("0");
            BigDecimal differentType = new BigDecimal("0");
            if (!StringUtils.isEmpty(map.get("different")))
            {
                different = new BigDecimal(new String((byte[]) map.get("different")));
                if (map.containsKey("different_type"))
                {
                    Object obj = map.get("different_type");
                    if (obj instanceof Integer)
                    {
                        differentType = new BigDecimal(map.get("different_type").toString());
                    }
                    else
                    {
                        differentType = new BigDecimal(new String((byte[]) map.get("different_type")));
                    }
                }
            }
            //平级金额
            BigDecimal sibling     = new BigDecimal("0");
            BigDecimal siblingType = new BigDecimal("0");
            if (!StringUtils.isEmpty(map.get("sibling")))
            {
                sibling = new BigDecimal(new String((byte[]) map.get("sibling")));
                if (map.containsKey("sibling_type"))
                {
                    Object obj = map.get("sibling_type");
                    if (obj instanceof Integer)
                    {
                        siblingType = new BigDecimal(map.get("sibling_type").toString());
                    }
                    else
                    {
                        siblingType = new BigDecimal(new String((byte[]) map.get("sibling_type")));
                    }
                }
            }
            //直推分销奖金额
            BigDecimal directM = new BigDecimal("0");
            //数值类型 1=元 other = 百分比
            BigDecimal directmType = new BigDecimal("0");
            if (map.containsKey("direct_m"))
            {
                directM = new BigDecimal(new String((byte[]) map.get("direct_m")));
                if (map.containsKey("direct_m_type"))
                {
                    Object obj = map.get("direct_m_type");
                    if (obj instanceof Integer)
                    {
                        directmType = new BigDecimal(map.get("direct_m_type").toString());
                    }
                    else
                    {
                        directmType = new BigDecimal(new String((byte[]) map.get("direct_m_type")));
                    }
                }
            }
            //间推分销奖金额
            BigDecimal indirectM = new BigDecimal("0");
            ;
            BigDecimal indirectmType = new BigDecimal("0");
            if (map.containsKey("indirect_m"))
            {
                indirectM = new BigDecimal(new String((byte[]) map.get("indirect_m")));
                if (map.containsKey("indirect_m_type"))
                {
                    Object obj = map.get("indirect_m_type");
                    if (obj instanceof Integer)
                    {
                        indirectmType = new BigDecimal(map.get("indirect_m_type").toString());
                    }
                    else
                    {
                        indirectmType = new BigDecimal(new String((byte[]) map.get("indirect_m_type")));
                    }
                }
            }


            resultMap.put("zhekou", zhekou);
            resultMap.put("price_type", priceType);

            resultMap.put("different", different);
            resultMap.put("different_type", differentType);

            resultMap.put("sibling", sibling);
            resultMap.put("sibling_type", siblingType);

            resultMap.put("direct_m", directM);
            resultMap.put("direct_m_type", directmType);

            resultMap.put("indirect_m", indirectM);
            resultMap.put("indirectMType", indirectmType);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logg.error("反序列化object: " + content + " 失败！！！");
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.BUSY_NETWORK, "网络异常", "getDistributionGradeBySets");
        }
        return resultMap;
    }

    /**
     * 反php序列化对象
     * 【此方法没有转换流】
     *
     * @param content -
     * @return T
     * @throws LaiKeAPIException -
     */
    @Deprecated
    public static Map getUnserializeObj(String content) throws LaiKeAPIException
    {
        PHPSerializer p = new PHPSerializer();
        try
        {
            if (StringUtils.isEmpty(content))
            {
                return null;
            }
            Map map = (Map) p.unserialize(content.getBytes(GloabConst.Chartset.UTF_8), Map.class);
            return map;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logg.error("反序列化object: " + content + " 失败！！！");
        }
        return null;
    }

    /**
     * 反php序列化对象
     *
     * @param content -
     * @return T
     * @throws LaiKeAPIException -
     */
    public static <T> T getUnserializeObj(String content, Class<T> cls) throws LaiKeAPIException
    {
        PHPSerializer p = new PHPSerializer();
        try
        {
            if (StringUtils.isEmpty(content))
            {
                return null;
            }
            Map map = (Map) p.unserialize(content.getBytes(StandardCharsets.UTF_8), cls);
            if (map != null && !map.isEmpty())
            {
                map.replaceAll((k, v) ->
                {
                    if (StringUtils.isInteger(map.get(k) + ""))
                    {
                        return map.get(k);
                    }
                    Object val = map.get(k);
                    if (val != null)
                    {
                        return new String((byte[]) val, StandardCharsets.UTF_8);
                    }
                    return val;
                });
            }
            String json = JSON.toJSONString(map);
            return JSON.parseObject(json, cls);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logg.error("反序列化object: " + content + " 失败！！！");
        }
        return null;
    }

    public static <T> T getUnserializeByBasic(String content, Class<T> cls) throws LaiKeAPIException
    {
        PHPSerializer p = new PHPSerializer();
        try
        {
            if (StringUtils.isEmpty(content))
            {
                return null;
            }
            Object obj = p.unserialize(content.getBytes(GloabConst.Chartset.UTF_8), cls);
            return JSON.parseObject(JSON.toJSONString(obj), cls);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logg.error("反序列化object: " + content + " 失败！！！");
        }
        return null;
    }

    /**
     * 反php序列化数组
     *
     * @param content -
     * @return List -
     */
    public static <T> T getUnserializeArray1(String content, Class<T> cls) throws LaiKeAPIException
    {
        PHPSerializer p    = new PHPSerializer();
        String        json = "";
        try
        {
            if (StringUtils.isEmpty(content))
            {
                return null;
            }
            AssocArray array1 = (AssocArray) p.unserialize(content.getBytes(StandardCharsets.UTF_8));
            json = JSON.toJSONString(array1.toArrayList());
            return JSON.parseObject(json, cls);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logg.error("反序列化object: " + content + " 失败！！！");
        }
        return null;
    }


    /**
     * 反php序列化数组
     *
     * @param content -
     * @return List -
     */
    public static <T> T getUnserializeArray(String content, Class<T> cls) throws LaiKeAPIException
    {
        PHPSerializer p    = new PHPSerializer();
        String        json = "";
        try
        {
            if (StringUtils.isEmpty(content))
            {
                return null;
            }
            AssocArray array1 = (AssocArray) p.unserialize(content.getBytes(GloabConst.Chartset.UTF_8));


            AssocArray          array2 = (AssocArray) array1.toArrayList().get(0);
            Map<String, Object> map    = array2.toHashMap();
            if (map != null && !map.isEmpty())
            {
                map.replaceAll((k, v) -> new String((byte[]) map.get(k)));
            }
            json = JSON.toJSONString(map);
            return JSON.parseObject(json, cls);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logg.error("反序列化object: " + content + " 失败！！！");
        }
        return null;
    }

    /**
     * 反php序列化数组
     *
     * @param content -
     * @return List -
     */
    public static List<Map<String, Object>> getUnserializeToList(String content) throws LaiKeAPIException
    {
        PHPSerializer             serializer = new PHPSerializer();
        List<Map<String, Object>> resultList = null;
        try
        {
            // 将 PHP 序列化字符串转换为字节数组
            byte[] serializedBytes = content.getBytes();
            // 进行反序列化操作
            Object deserializedObject = serializer.unserialize(serializedBytes);
            if (deserializedObject instanceof AssocArray)
            {
                resultList = new ArrayList<>();
                List deserializedList = ((AssocArray) deserializedObject).toArrayList();
                for (Object item : deserializedList)
                {
                    if (item instanceof AssocArray)
                    {
                        AssocArray          array  = (AssocArray) item;
                        Map<String, Object> map    = array.toHashMap();
                        Set<String>         keySet = map.keySet();
                        for (String key : keySet)
                        {
                            if (map.get(key) instanceof byte[])
                            {
                                map.put(key, new String((byte[]) map.get(key)));
                            }
                        }
                        resultList.add(map);
                    }
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logg.error("反序列化object: " + content + " 失败！！！");
        }
        return resultList;
    }


    /**
     * java bean 序列化成php格式
     *
     * @param obj -
     * @return java.lang.String
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/11 17:56
     */
    public static String JavaSerializeByPhp(Object obj) throws LaiKeAPIException
    {
        try
        {
            PHPSerializer p         = new PHPSerializer();
            Object        resultObj = p.serialize(obj);
            return new String((byte[]) resultObj);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logg.error("序列化object: " + JSON.toJSONString(obj) + " 失败！！！");
        }
        return null;
    }

    /**
     * 获取反序列化字符串
     *
     * @param content
     * @return
     * @throws LaiKeAPIException
     */
    public static String getUnserializeString(String content)
    {
        PHPSerializer p = new PHPSerializer();
        try
        {
            if (StringUtils.isEmpty(content))
            {
                return null;
            }
            Object array1 = p.unserialize(content.getBytes(GloabConst.Chartset.UTF_8));
            return new String((byte[]) array1);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logg.error("反序列化object: " + content + " 失败！！！");
        }
        return null;
    }

    /**
     * 是否是序列化字符串
     *
     * @param content
     * @return
     */
    public static boolean isSerialized(String content)
    {
        if (org.apache.commons.lang3.StringUtils.isNotEmpty(content))
        {
            String data = StringUtils.trim(content, " ");
            if ("N;".equals(data))
            {
                return true;
            }
            String  reg     = "^([adObis]):";
            Pattern pattern = Pattern.compile(reg);
            Matcher matcher = pattern.matcher(data);
            if (!matcher.find())
            {
                return false;
            }
            String g1 = matcher.group(1);
            switch (g1)
            {
                case "a":
                case "O":
                case "s":
                    reg = "^[badions]:[0-9]+:.*[;}]";
                    pattern = Pattern.compile(reg);
                    matcher = pattern.matcher(data);
                    if (matcher.find())
                    {
                        return true;
                    }
                    break;
                case "b":
                case "i":
                case "d":
                    reg = "^[badions]:[0-9.E-]+;";
                    pattern = Pattern.compile(reg);
                    matcher = pattern.matcher(data);
                    if (matcher.find())
                    {
                        return true;
                    }
                    break;
                default:
                    break;
            }
            return false;
        }
        return false;
    }

    /**
     * 反php序列化对象
     * lkt_auction_product.attribute
     *
     * @param content - a:1:{i:1270;a:1:{i:16625;s:5:"16625";}}
     * @return T  - {1270={16625=16625}}
     * @throws LaiKeAPIException -
     */
    public static Map<Integer, Map<Integer, Object>> getAuctionProductAttribute(String content) throws LaiKeAPIException
    {
        PHPSerializer                      p         = new PHPSerializer();
        Map<Integer, Map<Integer, Object>> resultMap = new HashMap<>(16);
        try
        {
            if (StringUtils.isEmpty(content))
            {
                return null;
            }
            Map<Integer, AssocArray> dataMap = DataUtils.cast(p.unserialize(content.getBytes(GloabConst.Chartset.UTF_8), Map.class));
            if (dataMap != null && !dataMap.isEmpty())
            {
                dataMap.replaceAll((k, v) ->
                {
                    Map<Integer, Object> map = new HashMap<>(16);
                    if (v != null)
                    {
                        map = DataUtils.cast(v.toHashMap());
                        if (map != null && !map.isEmpty())
                        {
                            map.replaceAll((kk, vv) -> new String((byte[]) vv));
                        }
                    }
                    resultMap.put(k, map);
                    return v;
                });
            }
            return resultMap;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logg.error("反序列化object: " + content + " 失败！！！");
        }
        return null;
    }

}
