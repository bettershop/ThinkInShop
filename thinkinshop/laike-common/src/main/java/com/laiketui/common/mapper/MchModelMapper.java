package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.mch.MchModel;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;


/**
 * 店铺 sql
 *
 * @author Trick
 * @date 2020/10/15 15:04
 */
public interface MchModelMapper extends BaseMapper<MchModel>
{


    /**
     * 获取搜索最多的店铺信息
     *
     * @param map -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/10/15 15:05
     */
    @Select("select * from lkt_mch where store_id = #{store_id} and review_status = 1 order by collection_num desc limit #{page},#{pageSize}")
    List<MchModel> getHotMch(Map<String, Object> map) throws LaiKeAPIException;


    /**
     * 模糊查询店铺信息
     *
     * @param storeId   -
     * @param mchName   -
     * @param pageStart -
     * @param pageEnd   -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/10/16 10:45
     */
    @Select("<script> " +
            "select * from lkt_mch where store_id = #{storeId} and recovery = 0 and review_status = 1 and is_lock = 0 " +
            "<if test=\"mchName != null and mchName!='' \"  > and name like concat('%',#{mchName},'%')</if>" +
            " order by collection_num desc " +
            " limit #{pageStart},#{pageEnd} " +
            "</script>")
    List<MchModel> getLikeMchByName(int storeId, String mchName, Integer pageStart, Integer pageEnd) throws LaiKeAPIException;


    /**
     * 获取店铺信息动态sql
     *
     * @param map -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/10/20 16:34
     */
    List<MchModel> getMchDynamic(Map<String, Object> map) throws LaiKeAPIException;


    /**
     * 店铺收藏数-1
     *
     * @param mchModel -
     * @return int
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/10/22 16:37
     */
    @Update("update lkt_mch set collection_num = collection_num - 1 where store_id = #{store_id} and id = #{id}")
    int cancelCollection(MchModel mchModel) throws LaiKeAPIException;


    /**
     * 店铺收藏数+1
     *
     * @param mchModel -
     * @return int
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/23 17:56
     */
    @Update("update lkt_mch set collection_num = collection_num + 1 where store_id = #{store_id} and id = #{id}")
    int addCollection(MchModel mchModel) throws LaiKeAPIException;

    /**
     * 修改店铺账户金额
     *
     * @param mchId -
     * @param money -
     * @author Trick
     * @date 2021/9/24 18:59
     */
    @Update("update lkt_mch set account_money = account_money + #{money} " +
            " where id = #{mchId} ")
    int mchSettlement(int mchId, BigDecimal money);

    /**
     * 店铺结算金额-直接结算到可提现金额
     * 场景: 1.已经发货但是未完成的订单，退运费的情况
     * 只有确认收货才会把金额计算到店铺余额中（account_money）
     * 2.扣除用户保证金
     *
     * @param storeId -
     * @param mchId   -
     * @param money   -
     * @return int
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2022-08-09 15:03:03
     */
    @Update("update lkt_mch set cashable_money = cashable_money + #{money} where store_id = #{storeId} and id = #{mchId}")
    int settlementMchCash(int storeId, int mchId, BigDecimal money) throws LaiKeAPIException;

    /**
     * 买家收货,增加余额、积分
     *
     * @param mchId    -
     * @param money    - 收入金额
     * @param integral - 收入积分
     * @return int
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/3 16:55
     */
    @Update("update lkt_mch set account_money = account_money + #{money},integral_money = integral_money + #{integral} " +
            " where id = #{mchId} ")
    int clientConfirmReceipt(int mchId, BigDecimal money, BigDecimal integral) throws LaiKeAPIException;

    /**
     * 增加店铺可提现账户金额
     *
     * @param mchId -
     * @param money - 收入金额
     * @return int
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/3 16:55
     */
    @Update("update lkt_mch set cashable_money = cashable_money + #{money}  where id = #{mchId} ")
    int refuseWithdraw(int mchId, BigDecimal money) throws LaiKeAPIException;

    /**
     * 提现扣减金额
     *
     * @param map -
     * @return int
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/3 16:55
     */
    @Update("update lkt_mch set cashable_money = cashable_money - #{t_money} " +
            " where store_id = #{store_id} and id = #{mch_id} ")
    int withdrawal(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 扣减可提现金额
     *
     * @return int
     * @throws LaiKeAPIException -
     * @author gp
     * @date 2023-08-10
     */
    @Update("update lkt_mch set cashable_money = cashable_money - #{money} " +
            " where store_id = #{storeId} and id = #{mchId} ")
    int withdrawalMoney(int storeId, int mchId, BigDecimal money) throws LaiKeAPIException;

    /**
     * 店铺结算金额-账户余额结算到可提现金额
     *
     * @param storeId -
     * @param mchId   -
     * @param money   -
     * @return int
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/16 15:15
     */
    @Update("update lkt_mch set account_money=account_money-#{money}, cashable_money = cashable_money + #{money} where store_id = #{storeId} and id = #{mchId}")
    int settlementMch(int storeId, int mchId, BigDecimal money) throws LaiKeAPIException;

    /**
     * 已收货 退货结算时 店铺余额需要减掉用户申请退款的金额
     * 店铺账户余额
     *
     * @param storeId -  商城id
     * @param mchId   -  店铺id
     * @param money   - 用户实际退款金额
     * @return int
     * @throws LaiKeAPIException -
     * @author vvx
     * @date 2024/12/25
     */
    @Update("update lkt_mch set account_money=account_money-#{money}  where store_id = #{storeId} and id = #{mchId}")
    int settlementMchAccountMoneyAfterReceipt(int storeId, int mchId, BigDecimal money) throws LaiKeAPIException;

    /**
     * 待收货的情况退款 结算时 店铺余额需要减掉用户申请退款的金额
     * 店铺账户余额
     *
     * @param storeId -   商城id
     * @param mchId   -   店铺id
     * @param money   -  详情退款后的剩余金额
     * @return int
     * @throws LaiKeAPIException -
     * @author vvx
     * @date 2024/12/25
     */
    @Update("update lkt_mch set account_money=account_money+#{money}  where store_id = #{storeId} and id = #{mchId}")
    int settlementMchAccountMoneyBeReceipt(int storeId, int mchId, BigDecimal money) throws LaiKeAPIException;


    /**
     * 店铺结算金额-直接结算到可提现金额
     * 场景: 1.已经发货但是未完成的订单，退运费的情况
     * 只有确认收货才会把金额计算到店铺余额中（account_money）
     *
     * @param storeId -
     * @param mchId   -
     * @param money   -
     * @return int
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2022-08-18 19:18:16
     */
    @Update("update lkt_mch set cashable_money = cashable_money + #{money} where store_id = #{storeId} and id = #{mchId}")
    int settlementCashableMch(int storeId, int mchId, BigDecimal money) throws LaiKeAPIException;

    /**
     * 获取店铺信息
     *
     * @param mchId
     * @param storeId
     * @return
     * @throws LaiKeAPIException
     */
    @Select("select * from lkt_mch where store_id = #{storeId} and id = #{mchId}  ")
    MchModel getMchInfo(String mchId, int storeId) throws LaiKeAPIException;

    /**
     * 获取店铺信息 通过产品id
     *
     * @param pid
     * @return
     * @throws LaiKeAPIException
     */
    @Select(" SELECT m.id,m.name,m.logo  from lkt_product_list as l right join lkt_configure attr on attr.pid=l.id  LEFT JOIN lkt_mch as m on l.mch_id = m.id where attr.id=#{attrId} ")
    MchModel getMchInfoByPid(int attrId) throws LaiKeAPIException;


    /**
     * 用户是否是店主
     *
     * @param storeId -
     * @param userId  -
     * @return int
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/7 15:07
     */
    @Select("select count(1) from lkt_mch where user_id = #{userId} and store_id=#{storeId} and review_status = 1 and recovery=0")
    int countMchIsByUser(int storeId, String userId) throws LaiKeAPIException;

    /**
     * 店铺是否是用户的
     *
     * @param storeId -
     * @param userId  -
     * @param mchId   -
     * @return int
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/7 15:07
     */
    @Select("select count(1) from lkt_mch where id=#{mchId} and user_id = #{userId} and store_id=#{storeId} and review_status = 1 and recovery=0")
    int countUserIsByMch(int storeId, String userId, int mchId) throws LaiKeAPIException;

    /**
     * 平台对接商家数量
     *
     * @param storeId -
     * @return int
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/12 18:05
     */
    @Select("select COUNT(1) from lkt_mch as m left join lkt_user as u on m.user_id = u.user_id and u.store_id = m.store_id where m.store_id = #{storeId} and m.review_status = 1 and m.recovery = 0")
    int countMchUserNum(int storeId) throws LaiKeAPIException;


    /**
     * 获取店铺信息
     *
     * @param map -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/26 11:43
     */
    List<Map<String, Object>> getMchUserInfo(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 获取店铺信息-统计
     *
     * @param map -
     * @return int
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/26 11:43
     */
    int countMchUserInfo(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 验证店铺名称是否存在
     *
     * @param storeId -
     * @param mchName -
     * @return int
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/3/1 14:56
     */
    @Select("select count(1) from lkt_mch where store_id = #{storeId} and name = #{mchName} and review_status!=2 and recovery = 0")
    Integer verifyMchName(int storeId, String mchName) throws LaiKeAPIException;

    /**
     * 获取上传所有店铺id
     *
     * @param storeId -
     * @return List
     * @author Trick
     * @date 2021/6/17 17:00
     */
    @Select("select id from lkt_mch where recovery=0 and store_id = #{storeId} and review_status = 1")
    List<Integer> getStoreMchIdList(int storeId);

    /**
     * 统计店铺未完成的订单
     * (订单已完成但是未结算也算未完成)
     *
     * @param storeId -
     * @param userId  -
     * @return int
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021-10-29 11:51:49
     */
    @Select("select count(1) FROM lkt_order as a ,lkt_mch as b " +
            " WHERE a.mch_id =concat(',', b.id ,',') " +
            " and b.user_id = #{userId} " +
            " and b.store_id=#{storeId} " +
            " and a.store_id = b.store_id " +
            " and a.recycle = 0 " +
            " and (a.status in (0,1,2,3) or (a.status = 5 and a.settlement_status = 0) )")
    int countMchUnfinishedOrder(int storeId, String userId) throws LaiKeAPIException;

    /**
     * 根据商城id获取所有店铺id集 【超过n天未登录的】
     *
     * @return List
     */
    @Select("select a.id from lkt_mch a,lkt_user b where a.store_id=#{storeId} and a.recovery=0 " +
            " and a.user_id=b.user_id " +
            " and b.last_time < date_add(CURRENT_TIMESTAMP,interval #{expireDate}*(-30) day ) ")
    List<Integer> getMchIdListByStoreId(Integer storeId, Integer expireDate);

    /**
     * 根据商城id获取所有店铺id集 【申请时间超过n天未审核的】
     *
     * @return List
     */
    @Select("SELECT id FROM lkt_mch WHERE store_id = #{storeId} AND recovery = 0 AND review_status = 0 AND add_time <= #{expireDate}")
    List<Integer> getAutoExamineMchId(Integer storeId, Date expireDate);

    /**
     * 根据商城id获取所有店铺id集 【最后登录时间超过n个月未登录的】
     *
     * @return List
     */
    @Select("SELECT id FROM lkt_mch WHERE store_id = #{storeId} AND recovery = 0 AND is_lock = 0 AND review_status = 1 AND last_login_time is not null AND last_login_time <= #{logOffDate}")
    List<Integer> getAutoLogOffMchId(Integer storeId, String logOffDate);

    /**
     * 查询供应商关联店铺
     *
     * @param storeId
     * @param supplierId
     * @param mchName
     * @param startTime
     * @param endTime
     * @param pageStart
     * @param pageEnd
     * @return
     * @throws LaiKeAPIException
     */
   /* @Select("<script> " +
            "select z.id,z.name,z.add_date,z.upNum,z.lowerNum,z.orderNum,z.settlementPrice from ( SELECT b.id,b.`name`,a.add_date," +
            "(SELECT COUNT(1) FROM lkt_product_list as p WHERE p.gongyingshang = #{supplierId} AND p.mch_id = b.id AND p.`status` = 2 AND p.`recycle` = 0) upNum," +
            "(SELECT COUNT(1) FROM lkt_product_list as p WHERE p.gongyingshang = #{supplierId} AND p.mch_id = b.id AND p.`status` = 3 AND p.`recycle` = 0) lowerNum," +
            "(SELECT COUNT( DISTINCT o.sNo) FROM lkt_order o RIGHT JOIN lkt_order_details d ON o.sNo = d.r_sNo LEFT JOIN lkt_product_list p ON d.p_id = p.id WHERE p.gongyingshang = #{supplierId} AND p.mch_id = b.id AND o.is_lssued = 1) orderNum," +
            "IFNULL((SELECT SUM(d.supplier_settlement) FROM lkt_order o RIGHT JOIN lkt_order_details d ON o.sNo = d.r_sNo LEFT JOIN lkt_product_list p ON d.p_id = p.id WHERE p.gongyingshang = #{supplierId} AND p.mch_id = b.id AND o.settlement_status = 1 AND o.is_lssued = 1),0) settlementPrice " +
            "FROM lkt_product_list a LEFT JOIN lkt_mch b ON a.mch_id = b.id " +
            "WHERE a.store_id = #{storeId} AND a.gongyingshang = #{supplierId} AND a.recycle = 0 AND a.mch_id != 0 " +
            "<if test=\"mchName != null and mchName!='' \"  > and b.name like concat('%',#{mchName},'%') </if>" +
            "<if test=\"startTime != null and startTime!='' \"  > and a.add_date <![CDATA[  >=  ]]> #{startTime} </if>" +
            "<if test=\"endTime != null and endTime!='' \"  > and a.add_date <![CDATA[  <=  ]]> #{endTime} </if>" +
            "order by a.add_date DESC " +
            "limit #{pageStart},#{pageEnd} ) z " +
            "GROUP BY id " +
            "</script>")*/
    List<Map<String, Object>> getRelationMch(@Param("storeId") int storeId, @Param("supplierId") String supplierId, @Param("mchName") String mchName, @Param("startTime") String startTime, @Param("endTime") String endTime, @Param("pageStart") Integer pageStart, @Param("pageEnd") Integer pageEnd) throws LaiKeAPIException;

    @Select("<script> " +
            "SELECT count(distinct b.id) from lkt_product_list a RIGHT JOIN lkt_mch b ON a.mch_id = b.id WHERE a.store_id = #{storeId} AND a.gongyingshang = #{supplierId} AND a.recycle = 0 And a.mch_id != 0" +
            "<if test=\"mchName != null and mchName!='' \"  > and b.name like concat('%',#{mchName},'%') </if>" +
            "<if test=\"startTime != null and startTime!='' \"  > and a.add_date <![CDATA[  >=  ]]> #{startTime}</if>" +
            "<if test=\"endTime != null and endTime!='' \"  > and a.add_date <![CDATA[  <=  ]]> #{endTime}</if>" +
            "</script>")
    int countRelationMch(int storeId, String supplierId, String mchName, String startTime, String endTime) throws LaiKeAPIException;

    /**
     * 对接商品数
     *
     * @param storeid
     * @return
     */
    @Select("select   count(1) from  lkt_product_list where  recycle = 0 and mch_status = 2 and  store_id=#{storeid}")
    Integer countUserAmount(int storeid);

    @Select("select   count(1) from  lkt_mch  where store_id=#{storeid} and review_status = 1 and recovery = 0")
    Integer countAll(int storeid);

//    @Select("select   count(1) from  lkt_mch where  store_id=#{storeid} and add_time>=#{date} ")
//    Integer getWeekAmount(int  storeid,String date);

//    @Select("select   count(1) from  lkt_mch where  store_id=#{storeid} and add_time>=#{startDate} and  add_time<=#{endDate}")
//    Integer getBetweenWeekAmount(int  storeid,String startDate,String endDate);

    //新增店铺
    @Select("select  count(1) from lkt_mch where store_id=#{storeid} and add_time>=#{date} and add_time<=#{today} and review_status = 1 and recovery = 0")
    Integer getWeekAmount(int storeid, String date, String today);

    @Select("select   count(1) from  lkt_mch where  store_id=#{storeid} and add_time>=#{startDate} and  add_time<#{endDate} and review_status = 1")
    Integer getBetweenWeekAmount(int storeid, String startDate, String endDate);

    /**
     * 新增店铺余额
     *
     * @param storeId
     * @param mchId
     * @param mchAddAccountMoney
     * @return
     */
    @Update("update lkt_mch set account_money = account_money + #{money}  where id = #{mchId} and store_id=#{storeId}")
    public Integer updateByMchIdAddAccountMoney(int storeId, int mchId, BigDecimal mchAddAccountMoney);

    @Select("<script> " +
            "select count(*) from lkt_mch where store_id = #{storeId} and review_status = 1 AND recovery = 0 " +
            "<if test=\"mchName != null and mchName!='' \"  > and name like concat('%',#{mchName},'%')</if>" +
            "</script>")
    int countLikeMchByName(int storeId, String mchName);

    /**
     * 根据查询统计模糊查询店铺信息
     *
     * @param storeId
     * @param mchName
     * @param pageStart
     * @param pageEnd
     * @param userId
     * @return
     * @throws LaiKeAPIException
     */
   /* @Select("<script> " +
            " select m.*,count(distinct p.id)as pro_num ,count(distinct c.id) as is_collect,count(distinct luc.id) as collectionNum  from lkt_mch m " +
            " left join lkt_product_list p on m.id = p.mch_id and p.status = 2 and p.mch_status = 2 " +
            " left join lkt_user_collection c on c.mch_id = m.id and c.type = 1 and c.user_id = #{userId} " +
            " left join lkt_user_collection luc on luc.mch_id = m.id and luc.type = 1 " +
            " where m.store_id = #{storeId} and m.review_status = 1 and m.recovery = 0 " +
            " <if test=\"mchName != null and mchName!='' \"  > and m.name like concat('%',#{mchName},'%')</if> " +
            " GROUP BY m.id " +
            " order by m.id desc " +
            " limit #{pageStart},#{pageEnd} " +
            "</script>")*/
    List<Map<String, Object>> getListByParams(@Param("storeId") int storeId, @Param("mchName") String mchName, @Param("pageStart") Integer pageStart, @Param("pageEnd") Integer pageEnd, @Param("userId") String userId) throws LaiKeAPIException;


    /**
     * 获取店铺交易数据-金额
     *
     * @param map
     * @return
     * @throws LaiKeAPIException
     */
    BigDecimal getMchOrderRecordByDateMoney(Map<String, Object> map) throws LaiKeAPIException;


    /**
     * 获取店铺交易数据-数量
     *
     * @param map
     * @return
     * @throws LaiKeAPIException
     */
    BigDecimal getMchOrderRecordByDateNumber(Map<String, Object> map) throws LaiKeAPIException;


    /**
     * 获取店铺购买力数据
     *
     * @param map
     * @return
     * @throws LaiKeAPIException
     */
    List<Map<String, Object>> getMchBuyPower(Map<String, Object> map) throws LaiKeAPIException;


    /**
     * 获取商品信息
     *
     * @param map
     * @return
     * @throws LaiKeAPIException
     */
    List<Map<String, Object>> getProList(Map<String, Object> map) throws LaiKeAPIException;

    @Select("select id from lkt_mch_class where store_id = #{storeId} and is_display = 1 and recycle = 0 limit 1" )
    Integer getOneMchCIdByStoreId(int storeId);

}