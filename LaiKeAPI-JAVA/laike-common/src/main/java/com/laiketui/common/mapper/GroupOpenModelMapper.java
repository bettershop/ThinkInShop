package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.group.GroupOpenModel;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.Date;
import java.util.List;
import java.util.Map;


/**
 * 开团表
 *
 * @author Trick
 * @date 2023/3/20 16:18
 */
public interface GroupOpenModelMapper extends BaseMapper<GroupOpenModel>
{


    @Override
    List<Map<String, Object>> selectDynamic(Map<String, Object> map) throws LaiKeAPIException;

    @Override
    int countDynamic(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 获取用户平团信息
     *
     * @param storeId -
     * @param orderno -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/18 15:32
     */
    @Select("SELECT o.id orderId,o.z_price,goo.uid,goo.ptcode,goo.ptgoods_id,goo.endtime,goo.addtime,goo.ptstatus,goo.ptnumber,goo.groupman,goo.platform_activities_id," +
            "goo.activity_no,goo.group_data,goo.group_level,o.z_price,o.offset_balance " +
            "FROM lkt_go_group_order o LEFT JOIN lkt_group_open goo ON o.ptcode = goo.ptcode " +
            " WHERE o.sNo = #{orderno} AND o.store_id = #{storeId}")
    Map<String, Object> getGroupOpen(int storeId, String orderno) throws LaiKeAPIException;


    /**
     * 开团详情
     *
     * @param map -
     * @return List
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2023/3/21 18:52
     */
    List<Map<String, Object>> groupDetailInfo(Map<String, Object> map) throws LaiKeAPIException;

    int countDetailInfo(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * goodscode
     *
     * @param goodsId    -
     * @param activityNo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/3/26 14:53
     */
    @Select("select g.g_status,p.status,g.is_show,g.group_level,g.sales_volume    from lkt_group_product as g " +
            "                    right join lkt_product_list as p on g.product_id=p.id " +
            "where p.id=#{goodsId} and g.is_delete = 0 and   g.activity_no=#{activityNo} limit 1")
    Map<String, Object> getGoodscode(int goodsId, int activityNo) throws LaiKeAPIException;

    /**
     * 获取拼团订单详情信息
     *
     * @param storeId -
     * @param orderId - 拼团订单id
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/3/24 16:26
     */
    @Select("SELECT k.ptgoods_id, k.ptnumber, k.addtime AS cantime, k.endtime, k.ptstatus , k.groupman, p.NAME, p.num, p.sNo, p.sheng , p.shi, p.xian, p.address, " +
            " p.mobile, p.STATUS , p.id, d.p_name, d.p_price, d.sid " +
            "FROM lkt_group_open k RIGHT JOIN lkt_go_group_order p ON k.ptcode = p.ptcode LEFT JOIN lkt_go_group_order_details d ON p.sNo = d.r_sNo " +
            " WHERE p.store_id = #{storeId} AND p.id = #{orderId}")
    Map<String, Object> getGroupOpenInfoByOid(int storeId, int orderId) throws LaiKeAPIException;


    /**
     * 获取用户开团信息
     *
     * @param map -
     * @return List
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2023/4/4 11:02
     */
    List<Map<String, Object>> getUserOpenGroupInfo(Map<String, Object> map) throws LaiKeAPIException;

    int countUserOpenGroupInfo(Map<String, Object> map) throws LaiKeAPIException;

    //获取某团参团人数
    @Select("select count(a.id) from lkt_group_activity a inner join lkt_group_open b on a.id=b.activity_id inner join lkt_group_open_record c on c.open_id=b.id " +
            " where b.status=0 and b.id=#{openId} and b.goods_id=#{goodsId}")
    int getGoodsBuyNum(int goodsId, String openId);

    //获取某活动拼团成功人数
    @Select("select count(a.id) from lkt_group_activity a inner join lkt_group_open b on a.id=b.activity_id inner join lkt_group_open_record c on c.open_id=b.id " +
            " where b.status=1 and a.id=#{acId} and b.goods_id=#{goodsId}")
    int getGoodsBuyNumByAcId(int goodsId, String acId);

    /**
     * 获取其它拼团信息
     *
     * @param storeId -
     * @param goodsId -
     * @param sysDate - 系统时间
     * @param userId  -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/3/24 16:48
     */
    @Select("select g.ptcode,g.ptnumber,g.groupman,g.endtime,cast(u.user_name as char) user_name,u.headimgurl,o.sNo from lkt_group_open as g left join lkt_user as u on g.uid=u.user_id " +
            "left join lkt_go_group_order as o on g.ptcode=o.ptcode where g.store_id = #{storeId} and g.ptgoods_id=#{goodsId} and g.ptstatus=1 and  u.store_id = #{storeId}  " +
            "and EXISTS(select * from lkt_go_group_order as o where o.ptcode=g.ptcode and o.status!=6) and g.uid!=#{userId} and o.pid='kaituan' " +
            "and g.endtime>#{sysDate}")
    List<Map<String, Object>> getOterGroupInfo(int storeId, int goodsId, String userId, Date sysDate) throws LaiKeAPIException;


    /**
     * 增加拼团数量
     *
     * @param storeId -
     * @param ptcode  -
     * @return LaiKeAPIException
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/4/1 16:41
     */
    @Update("update lkt_group_open set ptnumber=ptnumber+1 where store_id = #{storeId} and ptcode=#{ptcode}")
    int addPtNum(int storeId, String ptcode) throws LaiKeAPIException;

    /**
     * 根据结束时间获取开团信息
     *
     * @param storeId -
     * @param sysDate -
     * @return List
     * @author Trick
     * @date 2021/4/6 17:06
     */
    @Select("select * from lkt_group_open where store_id = #{storeId} and endtime < #{sysDate} and ptstatus=1")
    List<GroupOpenModel> selectGroupOpenByEndTime(int storeId, Date sysDate);


    /**
     * 查询拼团所有人数
     *
     * @param storeId -
     * @return List
     * @author Trick
     * @date 2021/5/8 16:55
     */
    @Select("SELECT distinct groupman FROM lkt_group_open WHERE store_id = #{storeId} and groupman > 0")
    List<Integer> selectGroupOpenByAll(int storeId);

    //某活动总参与人数
    @Select("select count(c.id) from lkt_group_activity a inner join lkt_group_open b on a.id=b.activity_id inner join lkt_group_open_record c on c.open_id=b.id " +
            " where a.id=#{acId}")
    int getActivityNumBuyAcId(String acId);

    //获取用户开团数量
    @Select("select count(1) from lkt_group_open where recycle=0 and user_id=#{userId} and activity_id=#{acId} and status=0")
    int getUserOpenNum(String userId, String acId);

    /**
     * 修改开团结束时间
     *
     * @param storeId   -
     * @param endtime   -
     * @param ptgoodsId -
     * @return int
     * @author Trick
     * @date 2021/5/10 16:30
     */
    @Update("update lkt_group_open set endtime = #{endtime}  where store_id = #{storeId} and (ptstatus =0 or ptstatus =1)")
    int endGroup(int storeId, String endtime, int ptgoodsId);

    //设置开团状态
    @Update("update lkt_group_open set status=#{status} where recycle=0 and id=#{openId} and recycle=0")
    int setGroupOpenStatus(String openId, int status);


    //获取最新团队状态
    @Select("select status from lkt_group_open where id=#{openId}")
    int getNewStatusById(String openId);


    //获取结算相关信息-需要等到所有团员过了受后期才能结算佣金,只要没有全部退款都可以结算佣金
    @Select("select a.id,u.user_id,u.id uid,orders.store_id,a.team_commission,a.status,a.sno,a.activity_id, ac.mch_id from lkt_group_open a inner join lkt_group_open_record b on a.id=b.open_id " +
            " inner join lkt_user u on u.user_id=a.user_id inner join lkt_order orders on orders.sno=b.sno and orders.settlement_status=1" +
            " left join lkt_group_activity ac on ac.id = a.activity_id" +
            " where a.status=1 and a.id=#{openId}  and a.is_settlement=0 and a.recycle=0" +
            " group by a.id")
    Map<String, Object> getOrderSettlement(String openId);

    //获取售后订单所属团队信息
    @Select("select c.id openId,c.user_id,c.team_num,a.sno,c.sno openSno,o.store_id from lkt_return_order a inner join lkt_order o on o.sno=a.sno inner join lkt_order_details b on a.sno=b.r_sno inner join lkt_group_open c on c.id=b.p_id and c.status=1 " +
            " where a.id=#{refundId} and o.otype='PT' ")
    Map<String, Object> getOrderRefundInfo(int refundId);

    //获取售后退款通过的数量
    @Select("select count(o.id) from lkt_order_details a inner join lkt_order o on o.sno=a.r_sno where o.otype='PT' and a.p_id=#{openId} and o.sno!=#{notOrderNo} and o.status=7")
    int countRefundNum(String openId, String notOrderNo);

    //拼团失败结算金额置空
    @Update("update lkt_group_activity a left join lkt_group_open b on a.id=b.activity_id and b.recycle=0 set b.status=2,b.team_commission=0 " +
            " where a.store_id=#{storeId} and b.end_date <=#{sysDate} and a.status=0 and b.status!=1 and a.recycle=0  ")
    int invalidGroupOpen(Integer storeId, String acId, Date sysDate);

    //团员全部退款，结算金额置空
    @Update("update  lkt_group_open set team_commission=0 where sno = #{openSno}")
    int setCommissionByOpenSno(String openSno);

    //获取拼团订单拼团状态
    @Select("select x.id, x.status, x.update_date  from lkt_group_open x inner join lkt_group_open_record y on x.id=y.open_id where y.sno=#{sNo}")
    Map<String, Object> getGroupOrderStatus(Integer storeId, String sNo);
}