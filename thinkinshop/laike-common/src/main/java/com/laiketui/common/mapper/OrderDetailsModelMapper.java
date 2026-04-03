package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.order.OrderDetailsModel;
import com.laiketui.domain.product.ProductListModel;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 订单明细 sql
 *
 * @author Trick
 * @date 2020/10/27 16:28
 */
public interface OrderDetailsModelMapper extends BaseMapper<OrderDetailsModel>
{


    /**
     * 通过订单明细获取商品信息 动态Sql
     *
     * @param map -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/10/27 16:31
     */
    List<Map<String, Object>> getOrderDetailByGoodsInfo(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 更新订单详情订单号
     *
     * @param storeId
     * @param newOrderNo
     * @param orderDetailsId
     * @return
     */
    @Update("update lkt_order_details set r_sNo = #{newOrderNo} where store_id = #{storeId} and id = #{orderDetailsId} ")
    int updateOrderDetailsParentOrderNo(int storeId, String newOrderNo, int orderDetailsId);

    /**
     * 更新订单详情订单号
     *
     * @param storeId
     * @param newOrderNo
     * @param sNo
     * @return
     */
    @Update("update lkt_order_details set r_sNo = #{newOrderNo} where store_id = #{storeId} and r_sNo = #{sNo} ")
    int updateOrderDetailsBysNo(int storeId, String newOrderNo, String sNo);

    /**
     * 更新订单详情订单状态
     *
     * @param storeId
     * @param orderNo
     * @param status
     * @return
     */
    @Update("update lkt_order_details set r_status = #{status} where store_id = #{storeId} and r_sNo = #{orderNo} ")
    int updateOrderDetailsStatus(int storeId, String orderNo, int status);

    /**
     * 更新订单详情订单状态和结算状态
     *
     * @param storeId
     * @param orderNo
     * @param status
     * @return
     */
    @Update("update lkt_order_details set r_status = #{status}, settlement_type = #{settlement_status} where store_id = #{storeId} and r_sNo = #{orderNo} ")
    int updateOrderDetailsStatusAndSettlementStatus(int storeId, String orderNo, int status, Integer settlement_status);


    /**
     * 根据订单号修改明细表信息 动态sql
     *
     * @param map -
     * @return int
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/26 17:31
     */
    int updateByOrdernoDynamic(Map<String, Object> map) throws LaiKeAPIException;


    /**
     * 动态统计
     *
     * @param map -
     * @return int
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/27 14:34
     */
    int countOrderDetailDynamic(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 获取订单总数
     *
     * @param storeId
     * @param sNo
     * @return
     */
    @Select("select sum(num) as sum from lkt_order_details where store_id = #{storeId} and r_sNo = #{sNo}")
    int getOrderDetailNum(int storeId, String sNo);

    /**
     * 删除订单详情
     *
     * @param storeId
     * @param sNo
     * @return
     */
    @Update("update lkt_order_details set recycle = 1 where store_id = #{storeId} and r_sNo = #{sNo} ")
    int delOrderDetails(int storeId, String sNo);

    @Update("update lkt_order_details set recycle = #{delType} where store_id = #{storeId} and r_sNo = #{sNo} ")
    int delOrderDetails1(int storeId, int delType, String sNo);

    /**
     * 用户删除订单详情
     *
     * @param storeId
     * @param sNo
     * @return
     */
    @Update("update lkt_order_details set user_recycle = 2 where store_id = #{storeId} and r_sNo = #{sNo} ")
    int userDelOrderDetails(int storeId, String sNo);

    /**
     * 商户删除订单详情
     *
     * @param storeId
     * @param sNo
     * @return
     */
    @Update("update lkt_order_details set mch_recycle = 2 where store_id = #{storeId} and r_sNo = #{sNo} ")
    int mchDelOrderDetails(int storeId, String sNo);

    /**
     * 商城删除订单详情
     *
     * @param storeId
     * @param sNo
     * @return
     */
    @Update("update lkt_order_details set store_recycle = 2 where store_id = #{storeId} and r_sNo = #{sNo} ")
    int storeDelOrderDetails(int storeId, String sNo);

    //修改订单明细金额
    @Update("update lkt_order_details set after_discount = after_discount - #{amt} where id=#{id}")
    int updateOrderPrice(int id, BigDecimal amt);

    /**
     * 判断订单是否全在售后且未结束
     *
     * @param storeId
     * @param sNo
     * @return
     */
    @Select("select count(1) from lkt_order_details where store_id = #{storeId} and r_sNo = #{sNo} and r_status != 7 ")
    int getOrderDetailAllUnfinishSh(int storeId, String sNo);


    /**
     * 获取未关闭的订单明细
     *
     * @param storeId -
     * @param sNo     -
     * @return List
     * @author Trick
     * @date 2021/4/9 11:13
     */
    @Select("select * from lkt_order_details where store_id = #{storeId} and r_sNo = #{sNo} and r_status != 7 ")
    List<Map<String, Object>> getOrderDetailNotClose(int storeId, String sNo);


    /**
     * 获取满足自动收货的订单信息
     *
     * @param storeId    -
     * @param oType      - 订单类型
     * @param autoSecond -
     * @param sysDate    -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/15 9:50
     */
    @Select(" select a.id,a.r_sNo,a.deliver_time,a.user_id,a.store_id,a.p_id,attr.pid goodsId,a.p_price,a.num,b.user_id,b.otype,b.z_price,b.allow  " +
            " from lkt_order_details a RIGHT JOIN lkt_order b on a.r_sno = b.sno and b.recycle=0 and b.`status`=2 and b.otype=#{oType} RIGHT JOIN lkt_configure attr on attr.id=a.sid " +
            " where a.store_id = #{storeId} and b.mch_id = concat(',',#{mchId},',') and a.r_status = '2' and date_add(a.deliver_time, interval #{autoSecond} second) < #{sysDate} ")
    List<Map<String, Object>> getReceivingGoodsInfo(int storeId, Integer mchId, String oType, int autoSecond, Date sysDate) throws LaiKeAPIException;

    /**
     * 售后寄回商品 确定收货
     *
     * @param storeId
     * @param orderId
     * @return
     */
    @Update(" update lkt_order_details set r_type = 12,r_status = 5  where store_id = #{storeId} and id = #{orderId} ")
    int updateOkOrderDetails(int storeId, int orderId);


    /**
     * 获取待自动评价信息
     *
     * @param storeId -
     * @param day     -
     * @param sysDate -
     * @param mchId   -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/16 11:15
     */
    @Select(" SELECT a.id,a.r_sNo,a.user_id,a.p_id,attr.pid goodsId,a.sid  " +
            " FROM lkt_order_details a RIGHT JOIN lkt_order `order` on `order`.sno=a.r_sno and `order`.recycle=0 " +
            " RIGHT JOIN lkt_configure attr on attr.id=a.sid " +
            " and a.recycle=0 AND not EXISTS (select x.id from lkt_comments x where x.oid = `order`.sno and a.id=x.order_detail_id ) " +
            " WHERE a.r_status = 5" +
            " and order.otype = #{oType} " +
            " and order.mch_id = concat(',',#{mchId},',') " +
            " AND a.store_id = #{storeId} AND date_add(a.arrive_time, INTERVAL #{day} second) <= #{sysDate}")
    List<Map<String, Object>> getReceivingCommentsInfo(int storeId, String oType, Integer mchId, int day, Date sysDate) throws LaiKeAPIException;


    @Update("update lkt_order_details set exchange_num =  #{exnum} + 1 where store_id = #{stroeId} and user_id = #{userId}  and id = #{id} ")
    int confirmReceiptOrderDetaisl(int exnum, int stroeId, String userId, int id);

    /**
     * 获取状态为 1和 2的订单详记录数量
     *
     * @param orderNo
     * @return
     */
    @Select(" select count(1) from lkt_order_details where r_status in (${status}) and r_sNo= #{orderNo} ")
    int getOrderDetailsNum(String status, String orderNo);


    @Select("select l.currency_code,l.exchange_rate,l.currency_symbol,l.p_sNo,d.id,l.source,l.mch_id,l.remarks,l.pay_time,l.id as oid,l.spz_price,l.z_freight,u.user_name,l.sNo,l.name,\n" +
            "l.mobile,l.sheng,l.shi,l.z_price,l.xian,l.status,l.address,l.pay,l.trade_no,l.coupon_id,l.reduce_price,l.coupon_price,\n" +
            "l.allow,l.drawid,l.mch_id,l.otype,l.grade_rate,l.grade_fan,l.preferential_amount, l.self_lifting, d.user_id,c.pid p_id,d.p_name,d.p_price,d.num,d.unit,d.add_time,\n" +
            "d.deliver_time,d.p_integral,d.score_deduction,d.after_discount,d.store_self_delivery,d.arrive_time,d.p_id as openId,d.platform_coupon_price,d.store_coupon_price,d.mch_id as d_mch_id,d.r_status,d.content,d.express_id,d.courier_num,d.sid,d.size,d.freight,d.supplier_settlement,e.kuaidi_name,d.write_off_num,d.after_write_off_num,d.mch_store_write_id,d.write_time," +
            " c.total_num ,c.img ,c.num stockNum, c.price goodsPrice, c.id attrId," +
            "l.subtraction_id,d.after_discount, l.comm_discount,ifnull(anchor.user_name,'') as anchor_name,ifnull(anchor.headimgurl,'') as anchor_url " +
            "from lkt_order_details as d\n" +
            "left join lkt_order as l on l.sNo=d.r_sNo\n" +
            "left join lkt_user as u on u.user_id=l.user_id and u.store_id=#{storeId} \n" +
            "left join lkt_express as e on d.express_id=e.id\n" +
            "left join lkt_configure as c on c.id = d.sid\n" +
            "left join lkt_user as anchor on anchor.user_id = d.anchor_id\n" +
            "where l.store_id = #{storeId} and l.sNo=#{sNo} ")
    List<Map<String, Object>> getAdminOrderDetailsInfo(String sNo, int storeId);


    /**
     * 获取店售出的商品类Id
     *
     * @param mchId -
     * @return List
     * @author Trick
     * @date 2021/5/31 14:58
     */
    @Select("select distinct substring_index(substring_index(b.product_class, '-', 2), '-', -1) from lkt_order_details as a left join lkt_product_list as b on a.p_id = b.id left join lkt_order as c on a.r_sNo = c.sNo " +
            " where a.r_status in(1,2,5) and c.sNo is not null and b.mch_id = #{mchId} ")
    List<Integer> getSaleProductClassList(int mchId);

    /**
     * 获取用户以往订单商品类别
     *
     * @param storeId -
     * @param userId  -
     * @return List
     * @author Trick
     * @date 2021/6/23 14:20
     */
   /* @Select("select distinct substring_index(substring_index(b.product_class, '-', 2), '-', -1)  " +
            "from lkt_order_details as a right join lkt_configure c on a.sid = c.id " +
            "right join lkt_product_list as b on c.pid=b.id  where b.store_id = #{storeId} and a.user_id = #{userId} order by b.add_date desc ")*/
    List<Integer> getGoodsByUser(@Param("storeId") int storeId, @Param("userId") String userId);

    /**
     * 根据商品类别统计金额
     *
     * @param mchId   -
     * @param classId -
     * @return Map
     * @author Trick
     * @date 2021/5/31 15:04
     */
    @Select("select SUM(a.after_discount) as price,SUM(a.freight) as freight from lkt_order_details as a left join lkt_product_list as b on a.p_id = b.id left join lkt_order as c on a.r_sNo = c.sNo\n" +
            " where a.r_status in(1,2,5) and b.mch_id = #{mchId} and c.sNo is not null and b.product_class like concat('%-',#{classId},'-%')  ")
    Map<String, BigDecimal> getSaleProductClassAmt(int mchId, int classId);

    @Select("select  d.num,c.costprice,c.price from lkt_order_details as d left join lkt_configure as c on d.sid = c.id " +
            " where d.r_sNo = #{orderno} and d.store_id = #{storeId}")
    List<Map<String, Object>> getbMchOrderIndexAmt(String orderno, int storeId);

    @Select("select lpl.imgurl,lpl.product_title,lpl.product_number,lod.p_price,lod.unit,lod.num,lod.size,lod.p_id,lod.courier_num,lod.express_id,lod.freight,lpl.brand_id ,lm.name as mchname,lod.r_status " +
            "from lkt_order_details as lod  " +
            "left join lkt_product_list as lpl on lpl.id=lod.p_id  " +
            "LEFT JOIN lkt_mch as lm on lm.id = lpl.mch_id  " +
            "where r_sNo=#{orderno}")
    List<Map<String, Object>> getbMchOrderIndexDetail(String orderno);


    List<Map<String, Object>> selectOrderInfoListDynamce(Map<String, Object> map);

    int countOrderInfoListDynamce(Map<String, Object> map);

    @Select("select d.id,u.user_name,d.user_id,d.p_id,d.p_name,d.p_price,d.num,d.unit,d.add_time,d.deliver_time,d.arrive_time," +
            "d.r_status,d.content,d.express_id,d.p_integral,d.score_deduction," +
            " d.courier_num,d.sid,d.size,d.freight,e.kuaidi_name, c.total_num ,c.img,d.after_discount,c.commission " +
            " from lkt_order_details as d left join lkt_order as l on l.sNo=d.r_sNo left " +
            " join lkt_user as u on u.user_id=l.user_id and u.store_id=#{storeId} " +
            " left join lkt_express as e on d.express_id=e.id " +
            " left join lkt_configure as c on c.id = d.sid " +
            " where l.store_id = #{storeId} and l.sNo=#{orderno}")
    List<Map<String, Object>> selectStoreOrderDetails(int storeId, String orderno);


    /**
     * 根据商城ID、订单号、店铺优惠券ID，查询不是这个订单详情的数据
     *
     * @param storeId
     * @param sNo
     * @param oneCouponId
     * @param orderId
     * @return
     */
    @Select("select id,r_status,after_discount from lkt_order_details where store_id =  #{storeId} and r_sNo = #{sNo} and coupon_id like concat('%',#{oneCouponId},'%')  and id != #{orderId} ")
    List<Map<String, Object>> getOrderDetailsUseTheCoupon(int storeId, String sNo, String oneCouponId, int orderId);

    //关闭订单
    @Update("update lkt_order_details set r_status=7 where r_sno=#{orderNo}")
    int closeOrder(String orderNo);

    /**
     * 自营店商品销量
     *
     * @param storeId
     * @param mchId
     * @param date
     * @param flag
     * @param type
     * @return
     */
    Integer getGoodsSales(int storeId, int mchId, String date, Boolean flag, String type);

    /**
     * 根据订单号获取全部订单详情id
     *
     * @param orderNo
     * @return
     */
    @Select("select id FROM lkt_order_details WHERE r_sNo = #{orderNo} ")
    List<Integer> getOrderDetailsIdByNO(String orderNo);

    /**
     * 更新子订单运费为0
     *
     * @param orderNo
     * @return
     */
    @Update("update lkt_order_details set freight = 0 where  r_sNo = #{orderNo}")
    int updateFreightZEROByRsNo(String orderNo);

    /**
     * 查询同订单同商品不同规格并且未关闭和未结算的详单信息
     *
     * @param storeId
     * @param orderNo
     * @param goodId
     * @param detailId
     * @return
     */
    @Select("select * FROM lkt_order_details WHERE store_id = #{storeId} and r_sNo = #{orderNo} and p_id = #{goodId} and id != #{detailId} and r_status != 7 and settlement_type = 0 limit 1")
    OrderDetailsModel getSameProOrderDetail(int storeId, String orderNo, Integer goodId, Integer detailId);

    /**
     * 根据订单号商品id获取未关闭的订单明细
     *
     * @param storeId -
     * @param sNo     -
     * @return List
     */
    @Select("select * from lkt_order_details where store_id = #{storeId} and r_sNo = #{sNo} and p_id = #{goodId} and r_status != 7 and settlement_type = 0 ")
    List<OrderDetailsModel> getSameProDetailNotCloseAndSettlement(int storeId, String sNo, Integer goodId);


    @Select("SELECT COUNT(DISTINCT r_sNo) FROM lkt_order_details WHERE store_id = #{storeId} and r_status != 0 and p_id = #{proId}")
    int payPeople(int storeId, Integer proId);

    /**
     * 根据订单号未关闭的订单明细
     *
     * @param storeId -
     * @param sNo     -
     * @return List
     */
    @Select("select * from lkt_order_details where store_id = #{storeId} and r_sNo = #{sNo} and r_status != 7 and settlement_type = 0 and after_discount = 0")
    List<OrderDetailsModel> getNotCloseDetailsBysNo(int storeId, String sNo);

    /**
     * 根据订单id判断该详单是否未待发货状态
     *
     * @param storeId   -
     * @param detailsId -
     * @return List
     */
    @Select("select count(0) from lkt_order_details where store_id = #{storeId} and id = #{detailsId} and r_status != 1 ")
    Integer getDetailsNumById(int storeId, Integer detailsId);

    /**
     * 统计当前订单下全部发货数量
     *
     * @param storeId
     * @param sNo
     * @return
     */
    @Select("select ifNull(sum(deliver_num), 0) from lkt_order_details where store_id = #{storeId} and r_sNo = #{sNo} ")
    Integer getDeliverNumBySNo(int storeId, String sNo);

    /**
     * 发货是否有重复订单号
     *
     * @param exId 快带id
     * @param exNo 快递单号
     * @return
     */
    @Select("select count(*) from lkt_order_details where courier_num like concat('%',#{exNo},'%') and express_id like concat('%',#{exId},'%')")
    Integer getDeliverNumByExIdAndExNo(String exId, String exNo);


    /**
     * 根据订单号或者父定单号查询信息
     *
     * @param map
     */
    List<Map<String, Object>> getOrderDetailBypSnoOrsNo(Map<String, Object> map);

    /**
     * 虚拟商品查询待核销次数
     *
     * @param paramsToOrderDetail
     * @return
     */
    @Select("select * from lkt_order_details where p_id = #{p_id} and r_sNo = #{r_sNo}")
    List<OrderDetailsModel> selectByPidAndSno(Map<String, Object> paramsToOrderDetail);

    /**
     * 根据订单号获取全部订单中商品的信息
     *
     * @param orderNo
     * @return
     */
    @Select("select p.* FROM lkt_order_details o left join lkt_product_list p on o.p_id=p.id  WHERE r_sNo = #{orderNo} ")
    List<ProductListModel> getProductInfoByNO(String orderNo);

    /**
     * 查询用户是否有未支付的预约订单
     *
     * @param param
     * @return
     */
    @Select("select o.* FROM lkt_order_details o left join lkt_product_list p on o.p_id=p.id WHERE o.user_id = #{user_id} and o.r_status=#{r_status} and o.write_time is not null and p.is_appointment = 2")
    List<OrderDetailsModel> selectOrder(HashMap<String, Object> param);

    @Update("update lkt_order_details set write_off_num = 0 where  r_sNo = #{orderNo} ")
    int updateOrderDetailsWriteNum(String sNo, int i);

    int reBack(String sNo, List<String> sNos);

    @Select("select * from lkt_order_details where r_sNo = #{sNo}")
    OrderDetailsModel selectBySno(String sNo);

    /**
     * 待发货订单
     *
     * @param storeId
     * @param mchId
     * @return
     * @throws LaiKeAPIException
     */
    @Select("select ifnull(count(1),0) from lkt_order where store_id=#{storeId} and mch_id=CONCAT(',',#{mchId},',') and recycle=0 and status=1")
    Integer getDfhOrderNum(Integer storeId, Integer mchId) throws LaiKeAPIException;

    /**
     * 待付款订单
     *
     * @param storeId
     * @param mchId
     * @return
     * @throws LaiKeAPIException
     */
    @Select("select ifnull(count(1),0) from lkt_order where store_id=#{storeId} and mch_id=CONCAT(',',#{mchId},',') and recycle=0 and status=0")
    Integer getDfkOrderNum(Integer storeId, Integer mchId) throws LaiKeAPIException;

    /**
     * 待收货数量
     *
     * @param storeId
     * @param mchId
     * @return
     * @throws LaiKeAPIException
     */
    @Select("select ifnull(count(1),0) from lkt_order where store_id=#{storeId} and mch_id=CONCAT(',',#{mchId},',') and recycle=0 and status=2")
    Integer getShOrderNum(Integer storeId, Integer mchId) throws LaiKeAPIException;

    /**
     * 申请开票订单数量
     *
     * @param storeId
     * @param mchId
     * @return
     * @throws LaiKeAPIException
     */
//    @Select("select count(1) from lkt_return_order r left join  lkt_order d on d.sNo = r.sNo where d.store_id=#{storeId} and d.mch_id=CONCAT(',',#{mchId},',') and d.recycle=0")
//    Integer getTkOrderNum(Integer storeId,Integer mchId) throws LaiKeAPIException;
    @Select("select ifnull(count(1),0) from lkt_invoice_info d where d.store_id=#{storeId} and d.mch_id=#{mchId} and d.recovery=0 and d.invoice_status=1")
    Integer getTkOrderNum(Integer storeId, Integer mchId) throws LaiKeAPIException;

    /**
     * 退款-待审核数量
     *
     * @param storeId
     * @param mchId
     * @return
     * @throws LaiKeAPIException
     */
    @Select("select ifnull(count(1),0) from lkt_return_order r left join  lkt_order d on d.sNo = r.sNo where d.store_id=#{storeId} and d.mch_id=CONCAT(',',#{mchId},',') and d.recycle=0 and r.r_type in (0,3,16) and r.re_type in (1,2)")
    Integer getDshOrderNum(Integer storeId, Integer mchId) throws LaiKeAPIException;

    /**
     * 待结算订单
     *
     * @param storeId
     * @param mchId
     * @return
     * @throws LaiKeAPIException
     */
    @Select("select ifnull(count(1),0) from lkt_order where store_id=#{storeId} and mch_id=CONCAT(',',#{mchId},',') and recycle=0 and settlement_status=0 and status=5")
    Integer getDjsOrderNum(Integer storeId, Integer mchId) throws LaiKeAPIException;

    /**
     * 待结算金额
     *
     * @param storeId
     * @param mchId
     * @return
     * @throws LaiKeAPIException
     */
    BigDecimal getDjsJeOrder(Integer storeId, Integer mchId) throws LaiKeAPIException;

    /**
     * 退款金额
     *
     * @param storeId
     * @param mchId
     * @return
     * @throws LaiKeAPIException
     */
    @Select("select ifnull(sum(r.real_money),0) from lkt_return_order r left join  lkt_order d on d.sNo = r.sNo where d.store_id=#{storeId} and d.mch_id=CONCAT(',',#{mchId},',') and d.recycle=0 and r.r_type in (4,9,13,15)")
    BigDecimal getTkJe(Integer storeId, Integer mchId) throws LaiKeAPIException;

    /**
     * 已提现金额
     *
     * @param storeId
     * @param mchId
     * @return
     * @throws LaiKeAPIException
     */
    @Select("select ifnull(sum(w.money),0) from lkt_withdraw as w  left join lkt_mch as m on m.user_id = w.user_id where m.store_id=#{storeId} and m.id=#{mchId} and is_mch=1  and w.status=1")
    BigDecimal getTxJe(Integer storeId, Integer mchId) throws LaiKeAPIException;


    /**
     * 总客单
     *
     * @param storeId
     * @param mchId
     * @return
     * @throws LaiKeAPIException
     */
//    @Select("select count(1) from lkt_order where store_id=#{storeId} and mch_id=CONCAT(',',#{mchId},',') and recycle=0")
    @Select("select ifnull(count(1),0) from (\n" +
            "select a.user_id from (\n" +
            "select user_id from lkt_user_collection where store_id=#{storeId} and mch_id=#{mchId} group by user_id\n" +
            "union all\n" +
            "select user_id from lkt_mch_browse where store_id = #{storeId} and mch_id=#{mchId} group by user_id\n" +
            "union all\n" +
            "select user_id from lkt_order where store_id=#{storeId} and mch_id = CONCAT(',',#{mchId},',') group by user_id ) a\n" +
            "group by a.user_id ) b;")
    Integer getZkdOrderNum(Integer storeId, Integer mchId) throws LaiKeAPIException;

    /**
     * 获取所有下单的客户
     *
     * @param storeId
     * @param mchId
     * @return
     * @throws LaiKeAPIException
     */
    @Select("select user_id from lkt_order where store_id=#{storeId} and mch_id=CONCAT(',',#{mchId},',') and recycle=0 group by user_id")
    List<Map<String, Object>> getAllUserByMchId(Integer storeId, Integer mchId) throws LaiKeAPIException;

    /**
     * 获取客户下单的所有金额
     *
     * @param storeId
     * @param mchId
     * @param userId
     * @return
     * @throws LaiKeAPIException
     */
    @Select("select ifnull(sum(z_price),0) from lkt_order where store_id=#{storeId} and mch_id=CONCAT(',',#{mchId},',') and recycle=0 and user_id=#{userId} and status in (1,2,5)")
    BigDecimal getAllUserMoneyByUserId(Integer storeId, Integer mchId, String userId) throws LaiKeAPIException;

    @Select("select date_format(add_time,'%Y-%m-%d') as add_time from lkt_order where store_id=#{storeId} and mch_id=CONCAT(',',#{mchId},',') and recycle=0 group by date_format(add_time,'%Y-%m-%d')")
    List<Map<String, Object>> getAllDateByMchId(Integer storeId, Integer mchId) throws LaiKeAPIException;


    @Select("select ifnull(count(1),0) as num  from lkt_order where store_id=#{storeId} and mch_id=CONCAT(',',#{mchId},',') and recycle=0 and date_format(add_time,'%Y-%m-%d')=#{date}")
    Integer getNumByMchId(Integer storeId, Integer mchId, String date) throws LaiKeAPIException;

    @Select("select ifnull(sum(z_price),0) as money from lkt_order where store_id=#{storeId} and mch_id=CONCAT(',',#{mchId},',') and recycle=0 and date_format(add_time,'%Y-%m-%d')=#{date}")
    BigDecimal getMoneyByMchId(Integer storeId, Integer mchId, String date) throws LaiKeAPIException;

    @Select("SELECT d.p_id,d.p_name,d.size,d.num,d.is_addp,d.sid,d.store_id,d.p_price,p.imgurl,d.freight as imgUrl,mch.id as mch_id,mch.name as mchName,mch.logo" +
            " FROM lkt_order_details as d LEFT JOIN lkt_product_list p ON p.id = d.p_id " +
            "LEFT JOIN lkt_mch mch ON mch.id=p.mch_id where d.r_sNo = #{orderNo} and d.is_addp = 1")
    List<OrderDetailsModel> getAddGoodsList(String orderNo);

    /**
     * 获取订单详情
     * @param sNo
     * @param pId
     * @param storeId
     * @return
     */
    Map<String, Object> getDetailInfoById(@Param("sNo") String sNo, @Param("pId") Integer pId, @Param("storeId") Integer storeId);

    @Select("select id,logistics from lkt_order_details where courier_num like concat ('%',#{number},'%') ")
    List<OrderDetailsModel> likeCourierNum(String number);
}
