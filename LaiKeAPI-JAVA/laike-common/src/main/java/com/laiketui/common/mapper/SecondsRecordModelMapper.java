package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.domain.plugin.seckill.SecondsRecordModel;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

/**
 * @author Trick
 */
public interface SecondsRecordModelMapper extends BaseMapper<SecondsRecordModel>
{

    /**
     * 逻辑删除秒杀记录信息
     *
     * @param secId -
     * @param day   -
     * @return int
     * @author Trick
     * @date 2021/5/6 11:45
     */
    @Update("update  lkt_seconds_record set is_delete = 1 where sec_id = #{secId} and LEFT(add_time,10) = #{day}")
    int updateRecord(int secId, String day);


    /**
     * 获取秒杀记录列表
     *
     * @param map -
     * @return List
     * @author Trick
     * @date 2021/5/7 17:17
     */
    List<Map<String, Object>> selectRecordList(Map<String, Object> map);

    int countRecordList(Map<String, Object> map);

    /**
     * 查询这一天秒杀了多少件
     *
     * @param secId -
     * @param day   -
     * @return int
     * @author Trick
     * @date 2021/5/8 10:10
     */
    @Select("select ifnull(sum(num),0) as soldNum from lkt_seconds_record where sec_id = #{secId}  and is_delete = 0 and left(add_time,10) = #{day}")
    int secSaleNum(int secId, String day);

    /**
     * 查询这之前秒杀了多少件
     *
     * @param secId -
     * @param day   -
     * @return int
     * @author Trick
     * @date 2021/5/8 10:10
     */
    @Select("select ifnull(sum(num),0) as soldNum from lkt_seconds_record where sec_id = #{secId}  and is_delete = 0 and left(add_time,10) < #{day}")
    int secBeforeSaleNum(int secId, String day);


    //释放库存
    @Update("update lkt_seconds_activity a,lkt_seconds_pro pro,lkt_seconds_record b set a.num=a.num+b.num,pro.num=pro.num+b.num  " +
            " where a.id=b.activity_id and a.id=pro.activity_id and b.attr_id=pro.attr_id " +
            " and b.sno=#{orderNo} ")
    int releaseStockNum(String orderNo);
}