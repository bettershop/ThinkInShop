package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.supplier.SupplierModel;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface SupplierModelMapper extends BaseMapper<SupplierModel>
{

    @Select("<script>" +
            "SELECT COUNT(1) FROM lkt_supplier WHERE store_id = #{storeId} AND supplier_name = #{supplierName} AND recovery = 0" +
            " <if test='id != null '> " + " AND id != #{id} " + " </if> " +
            "</script>")
    int countBySupplierName(Integer storeId, String supplierName, Integer id) throws LaiKeAPIException;


    @Select("<script>" +
            "SELECT COUNT(1) FROM lkt_supplier WHERE store_id = #{storeId} AND account_number = #{accountNumber} AND recovery = 0" +
            " <if test='id != null '> " + " AND id != #{id} " + " </if> " +
            "</script>")
    int countByAccountNumber(Integer storeId, String accountNumber, Integer id) throws LaiKeAPIException;

    List<Map<String, Object>> conditionQuery(Map<String, Object> map) throws LaiKeAPIException;

    int countCondition(Map<String, Object> map) throws LaiKeAPIException;

    //供应商商品总数d
    @Select("SELECT COUNT(1) FROM lkt_product_list WHERE store_id = #{storeId} AND gongyingshang = #{supplierId} AND mch_id = 0 AND recycle = 0")
    int proTotal(Integer storeId, Integer supplierId) throws LaiKeAPIException;

    //供应商在售商品
    //@Select("")
    int salePro(Integer storeId, Integer supplierId,Integer status) throws LaiKeAPIException;

    //供应商断供商品
    @Select("SELECT COUNT(1) FROM lkt_product_list WHERE store_id = #{storeId} AND gongyingshang = #{supplierId} AND `status` = 5 AND mch_id = 0 AND recycle = 0")
    int noSalePro(Integer storeId, Integer supplierId) throws LaiKeAPIException;

    //供应商违规下架商品
    @Select("SELECT COUNT(1) FROM lkt_product_list WHERE store_id = #{storeId} AND gongyingshang = #{supplierId} AND `status` = 4 AND mch_id = 0 AND recycle = 0")
    int violationPro(Integer storeId, Integer supplierId) throws LaiKeAPIException;

    //供应商待审核商品
    @Select("SELECT COUNT(1) FROM lkt_product_list WHERE store_id = #{storeId} AND gongyingshang = #{supplierId} AND mch_status = 1 AND mch_id = 0 AND recycle = 0")
    int examinePro(Integer storeId, Integer supplierId) throws LaiKeAPIException;

    //供应商关联店铺
    @Select("SELECT count(distinct b.id) from lkt_product_list a LEFT JOIN lkt_mch b ON a.mch_id = b.id WHERE a.store_id = #{storeId} AND a.gongyingshang = #{supplierId} AND a.recycle = 0 AND a.mch_id != 0")
    int mchNum(Integer storeId, Integer supplierId) throws LaiKeAPIException;

    //供应商需补货商品
    @Select("SELECT COUNT(1) FROM lkt_product_list WHERE store_id = #{storeId} AND gongyingshang = #{supplierId} AND num = 0 AND mch_id = 0 AND recycle = 0")
    int replenishmentPro(Integer storeId, Integer supplierId) throws LaiKeAPIException;

    //供应商待结算金额
    //@Select("SELECT IFNULL(SUM(d.supplier_settlement + d.freight), 0) totalAmount FROM lkt_order a RIGHT JOIN lkt_order_details d ON a.sNo = d.r_sNo LEFT JOIN lkt_product_list as p on p.id = d.p_id WHERE a.store_id = #{storeId} AND a.`status` = 5 AND a.settlement_status = 0 AND p.gongyingshang = #{supplierId} AND a.recycle = 0 AND a.is_lssued = 1")
    BigDecimal settlementPrice(Integer storeId, String supplierId) throws LaiKeAPIException;

    @Select("SELECT DISTINCT p.id,a.sNo,a.z_price,d.supplier_settlement,d.id as d_id FROM lkt_order a RIGHT JOIN lkt_order_details d ON a.sNo = d.r_sNo LEFT JOIN lkt_product_list as p on p.id = d.p_id " +
            "WHERE a.store_id = #{storeId} AND a.`status` in(5,7)  AND a.settlement_status = 1 AND p.gongyingshang = #{supplierId} AND a.recycle = 0 AND a.is_lssued = 1 " +
            "AND a.arrive_time >= #{startTime} " +
            "AND a.arrive_time <= #{endTime}")
    List<Map<String, Object>> settlementData(Integer storeId, String supplierId, String startTime, String endTime) throws LaiKeAPIException;

    /**
     * 余额增减
     *
     * @param uid
     * @param price
     * @return
     * @throws LaiKeAPIException
     */
    @Update("update lkt_supplier set surplus_balance = surplus_balance - #{price} where id = #{uid}")
    int subtractPrice(int uid, BigDecimal price) throws LaiKeAPIException;

    @Update("update lkt_supplier set surplus_balance = surplus_balance + #{price} where id = #{uid}")
    int addPrice(int uid, BigDecimal price) throws LaiKeAPIException;

    //查询指定日期供应商结算的金额
    @Select("SELECT DISTINCT IFNULL(SUM(b.supplier_settlement) + IFNULL(SUM(e.freight),0),0) FROM lkt_order a RIGHT JOIN lkt_order_details b ON a.sNo = b.r_sNo RIGHT JOIN lkt_product_list c ON b.p_id = c.id LEFT JOIN lkt_supplier_order_fright e ON b.id = e.detail_id WHERE a.store_id = #{storeId} and c.gongyingshang is not null and c.gongyingshang != '' AND a.settlement_status = 1 AND a.arrive_time LIKE concat('%',#{date},'%') AND a.is_lssued = 1")
    BigDecimal settlementDay(int storeId, String date) throws LaiKeAPIException;

    //供应商累计结算金额排行榜
    @Select("SELECT d.id ,d.supplier_name,IFNULL(SUM(b.supplier_settlement) + IFNULL(SUM(e.freight),0),0) settlement_price FROM lkt_order a RIGHT JOIN lkt_order_details b ON a.sNo = b.r_sNo RIGHT JOIN lkt_product_list c ON b.p_id = c.id LEFT JOIN lkt_supplier d ON c.gongyingshang = d.id LEFT JOIN lkt_supplier_order_fright e ON b.id = e.detail_id WHERE a.store_id = #{storeId} AND a.settlement_status = 1 AND c.gongyingshang IS NOT NULL AND c.gongyingshang != '' AND a.is_lssued = 1 GROUP BY d.id ORDER BY settlement_price desc LIMIT 10")
    List<Map<String, Object>> cumulative(int storeId) throws LaiKeAPIException;

    //供应商本周结算金额排行榜
    @Select("SELECT d.id ,d.supplier_name,IFNULL(SUM(b.supplier_settlement) + IFNULL(SUM(e.freight),0),0) settlement_price FROM lkt_order a RIGHT JOIN lkt_order_details b ON a.sNo = b.r_sNo RIGHT JOIN lkt_product_list c ON b.p_id = c.id LEFT JOIN lkt_supplier d ON c.gongyingshang = d.id LEFT JOIN lkt_supplier_order_fright e ON b.id = e.detail_id WHERE a.store_id = #{storeId} AND a.settlement_status = 1 AND c.gongyingshang IS NOT NULL AND c.gongyingshang != '' AND yearweek(date_format(a.arrive_time,'%Y-%m-%d')) = yearweek(now()) AND a.is_lssued = 1 GROUP BY d.id ORDER BY settlement_price desc LIMIT 10")
    List<Map<String, Object>> thisWeek(int storeId) throws LaiKeAPIException;

    //    @Select("select  count(1) from  lkt_supplier  where   add_date>=#{date} and  store_id=#{storeid}")
//    Integer getWeekAmount(int storeid,String date);
//    @Select("select  count(1) from  lkt_supplier  where   add_date>=#{startDate} and  add_date<=#{endDate} and  store_id=#{storeid}")
//    Integer  getBetweenWeekAmount(int  storeid,String  startDate,String endDate);
    //供应商周入驻数量
    @Select("select  count(1) from  lkt_supplier  where   add_date>=#{date} and DATE_FORMAT( add_date, '%Y-%m-%d' ) <= #{today} and  store_id=#{storeid}")
    Integer getWeekAmount(int storeid, String date, String today);

    //供应商驻数量
    @Select("select  count(1) from  lkt_supplier  where   add_date>=#{startDate} and  add_date<#{endDate} and  store_id=#{storeid}")
    Integer getBetweenWeekAmount(int storeid, String startDate, String endDate);

    /**
     * 根据供应商ID更新商城样式颜色
     *
     * @param id     供应商ID
     * @param color 颜色值
     * @return 受影响的行数
     */
    @Update("UPDATE lkt_supplier " +
            "SET color = #{color} " +
            "WHERE id = #{id}")
    int updateColorById(@Param("id") Integer id, @Param("color") String color);
}
