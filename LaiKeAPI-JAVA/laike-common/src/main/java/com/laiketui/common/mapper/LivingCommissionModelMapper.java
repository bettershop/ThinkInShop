package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.living.LivingCommissionModel;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @author zhuqingyu
 * @create 2024/6/28
 */
public interface LivingCommissionModelMapper extends BaseMapper<LivingCommissionModel>
{
    List<Map<String, Object>> queryLivingCommission(Map<String, Object> map) throws LaiKeAPIException;

    Integer countLivingCommission(Map<String, Object> map) throws LaiKeAPIException;

    //    @Select("select sum(commission) from lkt_order_details where store_id=#{store_id} and anchor_id = #{user_id} and r_status in (1,2,5)")
    @Select("select sum(commission) from lkt_living_commission where store_id=#{store_id} and user_id = #{user_id} and status in (101) and recycle=0")
    BigDecimal queryExpectCommission(Integer store_id, String user_id) throws LaiKeAPIException;


    @Select("select sum(commission) from lkt_living_commission where store_id=#{store_id} and user_id = #{user_id} and status in (100) and recycle=0")
    BigDecimal queryWithdrawalCommission(Integer store_id, String user_id) throws LaiKeAPIException;

    @Select("select sum(money) from lkt_record where store_id=#{store_id} and user_id = #{user_id} and type =42")
    BigDecimal queryAccumulateCommission(Integer store_id, String user_id) throws LaiKeAPIException;

    @Select("select IFNULL(SUM(commission),0) from lkt_living_commission where store_id=#{store_id} and living_id = #{living_id} and recycle=0")
    BigDecimal queryAllCommission(Integer store_id, String living_id) throws LaiKeAPIException;
}
