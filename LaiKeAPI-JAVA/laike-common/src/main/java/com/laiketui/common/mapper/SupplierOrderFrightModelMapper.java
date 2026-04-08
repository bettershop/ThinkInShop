package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.supplier.SupplierOrderFrightModel;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;

public interface SupplierOrderFrightModelMapper extends BaseMapper<SupplierOrderFrightModel>
{

    /**
     * 未被结算的供应商订单总运费
     *
     * @param storeId
     * @param sNo
     * @return
     * @throws LaiKeAPIException
     */
    @Select("SELECT IFNULL(SUM(freight),0) FROM lkt_supplier_order_fright WHERE store_id = #{storeId} AND sNo = #{sNo} AND detail_id = #{goodId} AND is_settlement = 0")
    BigDecimal getOrderTotal(int storeId, String sNo, Integer goodId) throws LaiKeAPIException;

    @Update("UPDATE lkt_supplier_order_fright SET is_settlement = 1 WHERE store_id = #{storeId} AND sNo = #{sNo} AND detail_id = #{goodId}")
    int updateIsSettlement(int storeId, String sNo, Integer goodId) throws LaiKeAPIException;

}