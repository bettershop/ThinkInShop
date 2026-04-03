package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.domain.mch.son.MchAdminRecordModel;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

public interface MchAdminRecordModelMapper extends BaseMapper<MchAdminRecordModel>
{

    /**
     * 获取日, 周, 月 核销订单数
     *
     * @param store_id
     * @param administrators_id
     * @param time
     * @return
     */
    @Select("SELECT " +
            "count( id ) AS total " +
            "FROM " +
            "lkt_mch_admin_record " +
            "WHERE " +
            "store_id = #{store_id} " +
            "AND administrators_id = #{administrators_id} " +
            "AND add_date >= #{time} ")
    Integer getToDayOrderNum(Integer store_id, Integer administrators_id, String time);

    /**
     * 获取当天每小时核销订单数
     *
     * @param store_id
     * @param administrators_id
     * @param time
     * @return
     */
    @Select("SELECT " +
            "count( id ) AS total " +
            "FROM " +
            "lkt_mch_admin_record " +
            "WHERE " +
            "store_id = #{store_id} " +
            "AND administrators_id = #{administrators_id} " +
            "and DATE_FORMAT( add_date, '%Y-%m-%d %H' ) = #{time} ")
    Integer getHourOrderNum(Integer store_id, Integer administrators_id, String time);


    /**
     * 获取每天时核销订单数
     *
     * @param store_id
     * @param administrators_id
     * @param time
     * @return
     */
    @Select("SELECT " +
            "count( id ) AS total, " +
            "DATE_FORMAT(add_date,\"%Y-%m-%d\") as day " +
            "FROM " +
            "lkt_mch_admin_record " +
            "WHERE " +
            "store_id = #{store_id} " +
            "AND administrators_id = #{administrators_id} " +
            "and DATE_FORMAT( add_date, '%Y-%m-%d' ) > #{time} " +
            "GROUP BY day ")
    List<Map<String, Object>> getDayOrderNum(Integer store_id, Integer administrators_id, String time);

}
