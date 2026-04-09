package com.laiketui.common.utils.tool;

import com.laiketui.core.cache.RedisUtil;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.lktconst.GloabConst;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

/**
 * 敏感词处理
 *
 * @author Trick
 * @date 2020/10/28 12:07
 */
@Component
public class SensitiveWordTool
{

    private static final Logger logger = LoggerFactory.getLogger(SensitiveWordTool.class);

    @Autowired
    RedisUtil redisUtil;

    private static final String REPLCE_STR = "*";

    private static final String FILE_PATH = "text/CensorWords.txt";


    /**
     * 初始化敏感词库
     *
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/10/28 13:48
     */
    @PostConstruct
    public void filterInfoAfter() throws LaiKeAPIException
    {
        InputStream       inputStream       = null;
        InputStreamReader inputStreamReader = null;
        BufferedReader    bufferedReader    = null;
        List<String>      arrayList         = new ArrayList<>();
        try
        {
            logger.info("正在初始化【敏感词汇】库");
            Object wordsObj = redisUtil.get(GloabConst.RedisHeaderKey.LKT_SENSITIVE_WORDS_PUBLIC);
            if (wordsObj == null)
            {
                logger.info("正在生成【敏感词汇】库");
                inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(FILE_PATH);
                inputStreamReader = new InputStreamReader(inputStream);
                bufferedReader = new BufferedReader(inputStreamReader);
                for (String txt = null; (txt = bufferedReader.readLine()) != null; )
                {
                    if (!arrayList.contains(txt))
                    {
                        arrayList = Arrays.asList(txt.split(","));
                    }
                }
                StringBuilder replaceAll = new StringBuilder(arrayList.size());
                for (int x = 0; x < arrayList.size(); x++)
                {
                    replaceAll.append(REPLCE_STR);
                }
                redisUtil.set(GloabConst.RedisHeaderKey.LKT_SENSITIVE_WORDS_PUBLIC, arrayList, -1);
                redisUtil.set(GloabConst.RedisHeaderKey.LKT_SENSITIVE_WORDS_SENSITIVE, replaceAll, -1);
                logger.info("已生成【敏感词汇】库");
            }
            else
            {
                logger.info("已存在【敏感词汇】库");
            }
        }
        catch (Exception e)
        {
            logger.error("初始化【敏感词汇】库异常 " + e.getMessage());
            e.printStackTrace();
        }
        finally
        {
            try
            {
                if (null != inputStreamReader)
                {
                    inputStreamReader.close();
                }
                if (null != inputStream)
                {
                    inputStream.close();
                }
                if (null != bufferedReader)
                {
                    bufferedReader.close();
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    /**
     * 将敏感字转换为*符号
     *
     * @param str -
     * @return str
     * @author Trick
     * @date 2020/10/28 14:06
     */
    @SuppressWarnings("unchecked")
    public String filterInfo(String str) throws LaiKeAPIException
    {
        try
        {
            Object badwordObj   = redisUtil.get(GloabConst.RedisHeaderKey.LKT_SENSITIVE_WORDS_PUBLIC);
            Object sensitiveObj = redisUtil.get(GloabConst.RedisHeaderKey.LKT_SENSITIVE_WORDS_SENSITIVE);
            if (badwordObj != null)
            {
                List<String>              arrayList  = (List<String>) badwordObj;
                String                    replaceAll = (String) sensitiveObj;
                HashMap<Integer, Integer> hash       = new HashMap<>(arrayList.size());
                //非法字符集合
//                Set<String> sensitiveWordSet = new HashSet<>();
//                List<String> sensitiveWordList = new ArrayList<>();
                StringBuilder buffer = new StringBuilder(str);
                String        temp;
                for (String s : arrayList)
                {
                    temp = s;
                    int findIndexSize = 0;
                    for (int start = -1; (start = buffer.indexOf(temp, findIndexSize)) > -1; )
                    {
                        findIndexSize = start + temp.length();
                        Integer mapStart = hash.get(start);
                        if (mapStart == null || findIndexSize > mapStart)
                        {
                            hash.put(start, findIndexSize);
                        }
                    }
                }

                Collection<Integer> values = hash.keySet();
                for (Integer startIndex : values)
                {
                    Integer endIndex = hash.get(startIndex);
                    //获取非法字符集
                    //String sensitive = buffer.substring(startIndex, endIndex);
                    //if (!sensitive.contains("*")) {
                    //sensitiveWordSet.add(sensitive);
                    //sensitiveWordList.add(sensitive);}
                    buffer.replace(startIndex, endIndex, replaceAll.substring(0, endIndex - startIndex));
                }
                hash.clear();
                return buffer.toString();
            }
            else
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.DATA_NOT_EXIST, "词库不存在", "filterInfo");
            }
        }
        catch (Exception e)
        {
            logger.error("数据脱敏失败", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.PARAMATER_ERROR, "数据脱敏失败", "filterInfo");
        }
    }


    /**
     * 断是否有敏感词汇判
     *
     * @param str -
     * @return boolean
     * @author Trick
     * @date 2020/10/28 14:10
     */
    public boolean checkSenstiveWord(String str)
    {
        SensitiveWordTool swt = new SensitiveWordTool();
        str = swt.filterInfo(str);
        return str.contains(SensitiveWordTool.REPLCE_STR);
    }


}
