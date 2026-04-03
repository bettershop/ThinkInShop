package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.order.OrderDataModel;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

public interface OrderDataModelMapper extends BaseMapper<OrderDataModel>
{

    @Update("update lkt_order_data set status=#{status} where id=#{id}")
    Integer updateStatus(int id, int status) throws LaiKeAPIException;


    List<Map<String, Object>> getVipOrder(Map<String, Object> map) throws LaiKeAPIException;


    Integer countVipOrder(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 获取按天会员续费数量
     *
     * @param date
     * @return
     * @throws LaiKeAPIException
     */
    @Select("select a.data from lkt_order_data a where order_type = 'DJ' and  DATE_FORMAT( addtime, '%Y-%m-%d' )  = #{date} and status = 1")
    List<Map<String, Object>> getRenewalMemberNByDay(String date) throws LaiKeAPIException;

    @Select("select a.data from lkt_order_data a where order_type = 'DJ' and DATE_FORMAT( addtime, '%Y-%m' ) = #{date} and status = 1")
    List<Map<String, Object>> getRenewalMemberNByMonth(String date) throws LaiKeAPIException;

    /**
     * 根据订单号tradeNo查询
     *
     * @param tradeNo
     * @return
     * @throws LaiKeAPIException
     */
    @Select("select * from lkt_order_data where trade_no = #{tradeNo}")
    OrderDataModel getOrderDataByTradeNo(String tradeNo) throws LaiKeAPIException;


    @Update("UPDATE lkt_order_data " +
            "SET data = #{data}, order_type = #{order_type} " +
            "WHERE trade_no = #{trade_no}")
    Integer updataByTradeNo(OrderDataModel orderDataSave) throws LaiKeAPIException;
}
