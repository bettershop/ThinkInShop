package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.mch.MchClassModel;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

public interface MchClassModelMapper extends BaseMapper<MchClassModel>
{


    @Select("<script>" +
            "SELECT COUNT(1) FROM lkt_mch_class WHERE store_id = #{storeId} AND name = #{name} AND recycle = 0" +
            " <if test='id != null '> " + " AND id != #{id} " + " </if> " +
            "</script>")
    int countByName(int storeId, String name, Integer id) throws LaiKeAPIException;

    List<Map<String, Object>> selectCondition(Map<String, Object> map) throws LaiKeAPIException;

    int countCondition(Map<String, Object> map) throws LaiKeAPIException;

    @Select("SELECT IFNULL(MAX(sort),0) FROM lkt_mch_class WHERE store_id = #{storeId}")
    int selectMaxSort(int storeId) throws LaiKeAPIException;

    /**
     * 动态sql
     *
     * @param map -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/20 10:56
     */
    @Override
    List<Map<String, Object>> selectDynamic(Map<String, Object> map) throws LaiKeAPIException;
}