package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.virtual.WriteRecordModel;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

public interface WriteRecordModelMapper extends BaseMapper<WriteRecordModel>
{

    @Override
    List<Map<String, Object>> selectDynamic(Map<String, Object> map) throws LaiKeAPIException;

    @Override
    int countDynamic(Map<String, Object> map) throws LaiKeAPIException;

    @Select("select r.*,s.name as store_name from lkt_order_details r left join lkt_mch_store s on r.mch_store_write_id=s.id where r.r_sNo=#{s_no} ")
    List<Map<String, Object>> selectBySno(Map<String, Object> parmaMap);

    /**
     * 根据商品详情id查询
     *
     * @param id
     * @return
     */
    @Select("select r.write_code,r.status,r.write_time,r.p_id,s.name AS name,r.s_no, o.currency_symbol , o.exchange_rate  from lkt_write_record r  LEFT JOIN lkt_order_details d ON d.id = r.p_id LEFT JOIN lkt_order o on o.sNo = d.r_sNo LEFT JOIN lkt_mch_store s ON r.write_store_id = s.id WHERE r.p_id = #{id} ")
    List<Map<String, Object>> selectByOrderDetailId(Integer id);
}
