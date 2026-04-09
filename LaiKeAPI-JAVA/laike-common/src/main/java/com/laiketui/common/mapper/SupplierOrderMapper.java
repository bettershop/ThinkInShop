package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.domain.order.OrderModel;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;


/**
 * 供应商订单 sql
 *
 * @author sunH_
 * @date 2023/02/06 15:09
 */
public interface SupplierOrderMapper extends BaseMapper<OrderModel>
{

    //获取能拆单的订单集sql
    String SUPPLIER_SPLIT_ORDER_LIST_SQL = "SELECT p.gongyingshang supplierId,a.sno,b.id detailId,b.p_id pid,b.sid,b.p_price,b.num,b.freight,b.after_discount FROM lkt_order a INNER JOIN lkt_order_details b ON a.sno = b.r_sno inner join lkt_product_list p ON b.p_id = p.id ";

    /**
     * 获取能拆单的供应商订单集-未经过拆单的
     *
     * @param storeId -
     * @param orderNo -
     * @return List
     */
    @Select(SUPPLIER_SPLIT_ORDER_LIST_SQL + " WHERE a.store_id=#{storeId} and a.sno = #{orderNo} AND ISNULL(p.gongyingshang) = 0 AND LENGTH(TRIM(p.gongyingshang)) > 0")
    List<Map<String, Object>> getSupplierSplitOrderList(int storeId, String orderNo);

    /**
     * 获取能拆单的供应商订单集-经过拆单后的
     *
     * @param storeId -
     * @param pSno    - 父级id
     * @return List
     */
    @Select(SUPPLIER_SPLIT_ORDER_LIST_SQL + " WHERE a.store_id=#{storeId} and a.p_sno = #{pSno} AND ISNULL(p.gongyingshang) = 0 AND LENGTH(TRIM(p.gongyingshang)) > 0")
    List<Map<String, Object>> getSupplierSplitOrderByFatherNoList(int storeId, String pSno);

    /**
     * 优惠券拆分订单获取订单详情
     *
     * @param storeId
     * @param sNo
     * @param mchId
     * @return
     */
    @Select("select a.id,a.p_id,c.price p_price,a.num,a.freight,b.product_class,a.r_sNo,a.after_discount from lkt_order_details as a " +
            " left join lkt_product_list as b on a.p_id = b.id left join lkt_configure c on c.id=a.sid where a.store_id = #{storeId} and a.r_sNo = #{sNo} and b.mch_id = #{mchId} AND ISNULL(b.gongyingshang) = 0 AND LENGTH(TRIM(b.gongyingshang)) > 0 ")
    List<Map<String, Object>> getOrderDetails(int storeId, String sNo, int mchId);
}