package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.order.ReturnOrderModel;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 售后 sql
 *
 * @author Trick
 * @date 2020/11/4 14:18
 */
public interface ReturnOrderModelMapper extends BaseMapper<ReturnOrderModel>
{

    @Override
    List<Map<String, Object>> selectDynamic(Map<String, Object> map) throws LaiKeAPIException;

    @Override
    int countDynamic(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 获取订单售后最新信息
     *
     * @param storeId  -
     * @param sno      -
     * @param detailId -
     * @return ReturnOrderModel
     * @author Trick
     * @date 2022/1/17 20:00
     */
    @Select("select * from lkt_return_order where store_id=#{storeId} and sno=#{sno} and p_id=#{detailId} order by audit_time desc limit 1")
    ReturnOrderModel getReturnOrderInfo(int storeId, int detailId, String sno);

    @Select("select * from lkt_return_order a inner join lkt_order b on b.id=#{orderId} and b.sno=a.sno where a.store_id=#{storeId} order by audit_time desc limit 1 ")
    ReturnOrderModel getReturnOrderInfoByOrderId(int storeId, int orderId);

    /**
     * 获取售后表信息 动态sql
     *
     * @param map -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/4 14:19
     */
    List<ReturnOrderModel> getReturnOrderListDynamic(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 统计售后表信息 动态sql
     *
     * @param map -
     * @return int
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/2/25 15:45
     */
    int countReturnOrderListDynamic(Map<String, Object> map) throws LaiKeAPIException;


    /**
     * 统计售后订单动态sql
     *
     * @param map -
     * @return int
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/9 17:58
     */
    int countRturnOrderNumDynamic(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 统计售后未结束订单
     * @return
     * @throws LaiKeAPIException
     */
    int countReturnNotFinishedCount(Map<String,Object> map);

    /**
     * 通过退款信息获取商品信息 动态Sql
     *
     * @param map -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/25 17:24
     */
    List<Map<String, Object>> getReturnOrderByGoodsInfo(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 通过退款信息获取商品信息 统计
     *
     * @param map -
     * @return int
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/5 17:14
     */
    int getReturnOrderByGoodsCount(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 获取售后订单信息
     *
     * @param map -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/2 14:29
     */
    List<Map<String, Object>> getReturnOrderJoinOrderListDynamic(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 获取急速售后售后订单信息
     *
     * @param map -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/2 14:29
     */
    List<Map<String, Object>> getReturnOrderQuickRefund(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * @param map
     * @return
     */
    int getUnFinishShouHouOrder(Map<String, Object> map);

    /**
     * 获取售后订单信息(个人中心订单售后列表)
     *
     * @param map -
     * @return List
     * @throws LaiKeAPIException -
     */
    List<Map<String, Object>> getReturnOrderList(Map<String, Object> map);

    /**
     * 获取售后订单信息(个人中心订单售后列表)
     *
     * @param int -
     * @return List
     * @throws LaiKeAPIException -
     */
    int getReturnOrderListCount(Map<String, Object> map);

    /**
     * 更新
     *
     * @param storeId
     * @param id
     * @return
     */
    @Update("update lkt_return_order set r_type = #{rType} where store_id = #{storeId} and id = #{id} ")
    int updateReturnOrder(int storeId, int id, int rType);


    /**
     * 获取售后信息
     *
     * @param storeId  -
     * @param id       - 售后订单id - 可选
     * @param detailId - 订单详情id - 可选
     * @return Map
     * @author Trick
     * @date 2021/1/6 11:16
     */
    @Select("<script>" +
            "select a.*,CAST(a.r_type AS char) rtype,b.p_name,attr.pid goodsId,attr.yprice,b.p_id superId,b.supplier_settlement,a.r_write_off_num,a.re_time,attr.img,b.write_off_num,b.num,b.p_price,b.p_integral,b.deliver_time,b.size,b.freight,b.after_discount,b.score_deduction" +
            ",b.p_name,b.size,mch.id mchId,mch.name mchName,mch.logo,mch.head_img,goods.write_off_settings,goods.gongyingshang as gys,goods.commodity_type,c.currency_symbol,c.currency_code,c.exchange_rate,c.sheng,c.shi,c.xian,c.address,c.name,c.mobile,u.mobile as user_mobile," +
            "CASE WHEN (a.r_type = 1 || a.r_type = 3)\n" +
            "        AND a.re_type = 1 THEN\n" +
            "        '退款中'\n" +
            "        when a.r_type = 4 || a.r_type = 9 || a.r_type = 15 then '退款成功'\n" +
            "        when (a.r_type = 2 || a.r_type = 5 || a.r_type = 8) and a.re_type != 3 then '退款失败'\n" +
            "        when (a.r_type = 1 || a.r_type = 3 || a.r_type = 11) and a.re_type = 3 then '换货中'\n" +
            "        when a.r_type = 12 then '换货成功'\n" +
            "        when (a.r_type = 5 || a.r_type = 10) and a.re_type = 3 then '换货失败'\n" +
            "        ELSE '审核中'\n" +
            "        END prompt from lkt_return_order as a " +
            " RIGHT JOIN lkt_configure attr ON attr.id = a.sid " +
            " left join lkt_order_details as b on a.p_id = b.id " +
            " left join lkt_order as c on b.r_sNo = c.sNo " +
            " LEFT JOIN lkt_product_list goods ON goods.id=attr.pid " +
            " LEFT JOIN lkt_mch mch ON mch.id=goods.mch_id" +
            " LEFT JOIN lkt_user u ON u.user_id = c.user_id" +
            " where a.store_id = #{storeId} " +
            " <if test='id != null '>and a.id = #{id} </if> " +
            " <if test='detailId != null '>and a.p_id = #{detailId} </if> " +
            " order by re_time desc limit 1 " +
            "</script>")
    Map<String, Object> getReturnOrderMap(int storeId, Integer id, Integer detailId);


    /**
     * 获取用户售后订单信息
     *
     * @param storeId -
     * @param userId  -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/7 14:33
     */
    @Select("select sNo from lkt_return_record where store_id = #{storeId} and user_id = #{userId} and r_type not in (1,4,9,11)")
    List<String> getReturnOrdernoList(int storeId, int userId) throws LaiKeAPIException;

    /**
     * 获取订单状态
     *
     * @param storeId
     * @param orderId
     * @return
     * @throws LaiKeAPIException
     */
    @Select(" select ifnull(count(1),0) from lkt_return_order where store_id = #{storeId} and p_id = #{orderId} and r_type in (0,1,3,11) ")
    int getOrderStatus(int storeId, int orderId) throws LaiKeAPIException;


    /**
     * 获取售后订单详情
     *
     * @param storeId -
     * @param id      -
     * @return List
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/3/11 16:36
     */
    @Select("select a.*,b.p_name,d.img,b.p_price,b.num,b.size from lkt_return_order as a left join lkt_order_details as b on a.p_id = b.id " +
            "left join lkt_product_list as c on b.p_id = c.id left join lkt_configure as d on b.sid = d.id" +
            " where a.store_id = #{storeId} and a.id = #{id} order by re_time desc")
    List<Map<String, Object>> selectReturnOrderInfo(int storeId, int id) throws LaiKeAPIException;


    /**
     * 获取用户回寄信息
     *
     * @param oid      - 订单详情id
     * @param returnId - 售后id
     * @param storeId  -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/3/11 16:54
     */
    @Select("SELECT * FROM lkt_return_goods WHERE oid =  #{oid} and store_id = #{storeId} and re_id=#{returnId} order by add_data desc ")
    List<Map<String, Object>> selectReturnGoodsInfo(int oid, Integer returnId, int storeId) throws LaiKeAPIException;


    /**
     * 订单是否在售后且未结束
     *
     * @param stroeId -
     * @param orderno -
     * @return Integer
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/3/24 10:26
     */
    @Select("select count(1) from lkt_return_order a ,lkt_order_details b where a.sno=b.r_sno and a.store_id = #{stroeId}  and a.sNo = #{orderno} and a.r_type not in  (2,8,4,5,9,10,12,13,15) and b.r_status != 7")
    Integer orderReturnIsNotEnd(int stroeId, String orderno) throws LaiKeAPIException;


    //订单明细是否在售后且未结束
    @Select("select count(1) from lkt_return_order a,lkt_order_details b where a.p_id=b.id and a.store_id = #{storeId} and a.sNo = #{orderNo} and a.p_id=#{detailId} and a.r_type not in (2,8,4,9,10,12,13,15) and b.r_status != 7")
    Integer orderDetailReturnIsNotEnd(int storeId, String orderNo, Integer detailId);

    /**
     * 订单是否在售后或售后完成
     *
     * @param stroeId -
     * @param orderno -
     * @return Integer
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/3/24 10:26
     */
    @Select("select count(1) from lkt_return_order a ,lkt_order_details b where a.sno=b.r_sno and a.store_id = #{stroeId}  and a.sNo = #{orderno} and a.r_type  in  (0,1,3,4,5,9,11,12,13,15)")
    Integer orderReturnIsNotEndOrEnd(int stroeId, String orderno) throws LaiKeAPIException;


    /**
     * 根据订单明细获取退款是否成功
     *
     * @param storeId  -
     * @param orderNo  -
     * @param detailId -
     * @return Integer
     * @author Trick
     * @date 2022/1/17 19:50
     */
    @Select("select count(1) from lkt_return_order a,lkt_order_details b where a.p_id=b.id and a.store_id = #{storeId} and a.sNo = #{orderNo} and a.p_id=#{detailId} and a.r_type in (4,9,13,15) and b.r_status = 7")
    Integer orderReturnSuccessNum(int storeId, String orderNo, Integer detailId);

    /**
     * 店铺带退款的订单数
     *
     * @param storeId -
     * @param mchId   -
     * @return Integer
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/5/28 10:39
     */
    @Select("<script>" +
            "select count(1) from lkt_return_order a left join lkt_product_list b on a.pid = b.id " +
            " where a.store_id = #{storeId} " +
            " <if test='mchId != null '> " +
            "  and b.mch_id = #{mchId} " +
            " </if> " +
            "<if test='mchId != null '> and b.mch_id = #{mchId} </if> " +
            " </script> ")
    Integer countOrderReturnWait(int storeId, Integer mchId) throws LaiKeAPIException;

    /**
     * 供应商带退款的订单数
     *
     * @param storeId  -
     * @param supplier -
     * @return Integer
     * @throws LaiKeAPIException -
     * @author sunH
     * @date 2022/10/8 10:39
     */
    @Select("<script>" +
            "select count(1) from lkt_return_order a left join lkt_product_list b on a.pid = b.id " +
            " where a.store_id = #{storeId} " +
            "<if test='supplier != null '> and b.gongyingshang = #{supplier} and is_agree = 1 </if> " +
            "and a.r_type in(0,1,3,11) " +
            " </script> ")
    Integer countOrderReturnForSupplier(int storeId, Integer supplier) throws LaiKeAPIException;


    //根据状态统计售后数量
    @Select("select count(a.id) from lkt_return_order a inner join lkt_order o on o.sNo=a.sNo left join lkt_product_list b on a.pid = b.id " +
            " where a.store_id = #{storeId} and a.r_type in(0,1,3,11) and a.r_type = #{rType} and o.otype=#{orderType}")
    Integer countOrderReturnWaitByStoreStatus(int storeId, int rType, String orderType) throws LaiKeAPIException;


    /**
     * 获取未退款、未失效、订单完成的订单信息
     *
     * @param storeId -
     * @param invalid -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/5/25 9:56
     */
    @Select("select a.sNo from lkt_order as a inner join lkt_order_details d on d.r_sno=a.sno left join lkt_return_order as b on a.sNo = b.sNo " +
            " where a.store_id = #{storeId} and a.otype = 'FX' and a.status = 5  and a.recycle=0 and a.commission_type = 0 and b.sNo is null and d.arrive_time <=#{invalid} " +
            " union all " +
            "select a.sNo from lkt_order as a inner join lkt_order_details d on d.r_sno=a.sno left join lkt_return_order as b on a.sNo = b.sNo where a.store_id = #{storeId} and a.otype = 'FX' and a.status = 5  and a.recycle=0 " +
            "and a.commission_type = 0 and b.r_type not in(0,1,3) and b.re_type !=3 and d.arrive_time <=#{invalid} ")
    List<Map<String, Object>> selectReturnNotMoney(int storeId, Date invalid) throws LaiKeAPIException;

    List<Map<String, Object>> selectRturnOrderNumDynamic1(Map<String, Object> map);

    int countRturnOrderNumDynamic1(Map<String, Object> map);

    @Select("select ifnull(sum(real_money),0) as total from lkt_return_order where re_type in (1,2) and r_type in (4,9) and sNo = #{orderno}")
    BigDecimal getReturnAmtByOrder(String orderno);

    @Select("select ifnull(sum(real_money),0) as total from lkt_return_order where re_type in (1,2) and r_type in (4,9,13,15) and sNo = #{orderno}")
    BigDecimal getReturnAmtByOrderForSettlement(String orderno);


    //获取最新售后信息 - 订单
    @Select("select * from lkt_return_order where sNo=#{orderNo} order by re_time desc limit 1")
    ReturnOrderModel getReturnNewInfoBySno(String orderNo);

    //获取最新售后信息 - 详情
    @Select("select * from lkt_return_order where p_id=#{detailId} order by re_time desc  limit 1")
    ReturnOrderModel getReturnNewInfoByDetailId(int detailId);

    @Select("select   ifnull(sum(real_money),0) from  " +
            "lkt_return_order a " +
            "left join lkt_order b on a.sNo=b.sNo " +
            "left join lkt_product_list AS p ON p.id = a.pid " +
            "where  a.store_id=#{storeid} " +
            "and  a.r_type in(4,9,13,15) " +
            "and   DATE_FORMAT(a.re_time,'%Y-%m-%d')=#{date} " +
            "and b.otype='GM' " +
            "AND ( p.gongyingshang IS NULL OR p.gongyingshang = '' ) ")
    BigDecimal sumByDate(int storeid, String date);

    @Select("select   ifnull(sum(real_money),0) from  " +
            "lkt_return_order a " +
            "left join lkt_order b on a.sNo=b.sNo " +
            "left join lkt_product_list AS p ON p.id = a.pid " +
            "where  a.store_id=#{storeid} " +
            "and  a.r_type in(4,9,13,15) " +
            "and  DATE_FORMAT(a.re_time,'%Y-%m')=#{date} " +
            "and b.otype='GM'" +
            "AND ( p.gongyingshang IS NULL OR p.gongyingshang = '' ) ")
    BigDecimal sumByModel(int storeid, String date);

    @Select("select   ifnull(sum(real_money),0) from " +
            "lkt_return_order a " +
            "left join lkt_order b on a.sNo=b.sNo  " +
            "left join lkt_product_list AS p ON p.id = a.pid " +
            "where  a.store_id=#{storeid} " +
            "and  a.r_type in(4,9,13,15) " +
            "and b.otype='GM'" +
            "AND ( p.gongyingshang IS NULL OR p.gongyingshang = '' ) ")
    BigDecimal sumAll(int storeid);

    @Select("select count(1) from   lkt_return_order where  store_id=#{storeid} and  DATE_FORMAT(re_time,\"%Y-%m-%d\")=#{date} group  by DATE_FORMAT(re_time,\"%Y-%m-%d\") ")
    Integer countBydays(int storeid, String date);

    @Select("select count(1) from   lkt_return_order where  store_id=#{storeid} and  DATE_FORMAT(re_time,\"%Y-%m\")=#{date} group  by DATE_FORMAT(re_time,\"%Y-%m\") ")
    Integer countByMonth(int storeid, String date);

    /**
     * 获取退款商品的数量
     *
     * @param param
     * @return
     */
    Long getReturnGoodsNum(Map<String, Object> param);

    /**
     * 查询待处理订单
     *
     * @param storeid
     * @param startDate
     * @param endDate
     * @return
     */
    Integer getOrderNumByPeriod(int storeid, String startDate, String endDate);

    /**
     * 查询订单换货成功次数
     *
     * @param storeId
     * @param detailId
     * @param sno
     * @return
     */
    @Select("select count(0) from lkt_return_order where store_id=#{storeId} and sno=#{sno} and p_id=#{detailId} and re_type = 3 and r_type = 12")
    Integer getOrderReturnGoodsNum(int storeId, int detailId, String sno);

    /**
     * 查询订单是否退货退款成功
     *
     * @param storeId
     * @param sno
     * @return
     */
    @Select("select count(0) from lkt_return_order where store_id=#{storeId} and sno=#{sno}  and re_type = 1 and r_type = 4")
    Integer getOrderGoodsRefundSuccessful(int storeId, String sno);

    /**
     * 根据订单详情id查询退货订单
     *
     * @param p_id
     * @return
     */
    @Select("select * from lkt_return_order where p_id=#{p_id}")
    List<ReturnOrderModel> selectByOrderDetailId(Integer p_id);

    /**
     * 获取订单申请售后未结束订单
     * @param storeId
     * @param sNo
     * @return
     */
    int getOrderReturnIsNotEnd(int storeId, String sNo);

    /**
     * 获取同意退款的数据
     * @return
     */
    List<ReturnOrderModel> getFinishReturnList(String sNo);

    @Update("update lkt_return_order set pay_pal_return_id = #{payPalReturnId} where id = #{refundId}")
    void setPayPalRefundId(int refundId, String payPalReturnId);

    @Update("update lkt_return_order set ali_out_request_no = #{ali_out_request_no} where id = #{refundId}")
    void setALiOutRequestNo(int refundId, String ali_out_request_no);

}
