package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.order.ExpressDeliveryModel;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

public interface ExpressDeliveryModelMapper extends BaseMapper<ExpressDeliveryModel>
{
    @Override
    List<Map<String, Object>> selectDynamic(Map<String, Object> map) throws LaiKeAPIException;

    @Override
    int countDynamic(Map<String, Object> map) throws LaiKeAPIException;


    List<Map<String, Object>> getGoodsByExpressDeliveryId(Map<String, Object> map) throws LaiKeAPIException;


    int countGoodsByExpressDeliveryId(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 判断该订单是否有电子面单发货
     *
     * @param storeId
     * @param sNo
     * @return
     * @throws LaiKeAPIException
     */
    @Select("select count(*) from lkt_express_delivery where store_id = #{storeId} and sNo = #{sNo} and subtable_id != 0")
    int countBysNo(Integer storeId, String sNo) throws LaiKeAPIException;

    @Update("update lkt_express_delivery set is_status = #{status} where id = #{id}")
    void updateStatusById(int status,Integer id);
}
