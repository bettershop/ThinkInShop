package com.laiketui.core.db;

import com.laiketui.core.exception.LaiKeAPIException;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.common.Mapper;
import tk.mybatis.mapper.common.MySqlMapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @description:
 * @author: wx
 * @date: Created in 2019/10/30 12:02
 * @version: 1.0
 * @modified By:
 */
@Component
public interface BaseMapper<T> extends Mapper<T>, MySqlMapper<T>
{

    /**
     * 动态sql
     *
     * @param map -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/20 10:56
     */
    List<Map<String, Object>> selectDynamic(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 动态sql-统计
     *
     * @param map -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/20 10:56
     */
    int countDynamic(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 动态统计 - key=需统计的字段 必须
     *
     * @param map -
     * @return BigDecimal
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/7/5 10:43
     */
    BigDecimal sumDynamic(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 批量插入
     *
     * @param map -
     * @return int
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/12/24 16:34
     */
    int saveList(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 动态删除
     *
     * @param map -
     * @return int
     * @author Trick
     * @date 2023/3/21 15:33
     */
    default int delDynamic(Map<String, Object> map)
    {
        return 0;
    }

    @Select("SELECT LAST_INSERT_ID()")
    int getLastInsertId();

}
