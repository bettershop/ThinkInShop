package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.mch.MchPromiseModel;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

/**
 * 店铺保证金记录表
 *
 * @author Trick
 * @date 2021/10/25 16:48
 */
public interface MchPromiseModelMapper extends BaseMapper<MchPromiseModel>
{

    @Override
    List<Map<String, Object>> selectDynamic(Map<String, Object> map) throws LaiKeAPIException;

    @Override
    int countDynamic(Map<String, Object> map) throws LaiKeAPIException;

    @Update("UPDATE lkt_mch_promise SET paypal_id = #{paypal_id} WHERE orderNo = #{sNo}")
    int updatePaypalIdBySno(String sNo, String paypal_id);

}
