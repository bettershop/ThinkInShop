package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.group.GroupOpenRecordModel;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 开团详情
 *
 * @author Trick
 * @date 2023/3/21 19:27
 */
public interface GroupOpenRecordModelMapper extends BaseMapper<GroupOpenRecordModel>
{
    @Override
    List<Map<String, Object>> selectDynamic(Map<String, Object> map) throws LaiKeAPIException;

    @Override
    int countDynamic(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 获取用户参团信息
     *
     * @param map -
     * @return List
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2023-04-04 14:35:24
     */
    List<Map<String, Object>> getUserJoinGroupInfo(Map<String, Object> map);

    int countUserJoinGroupInfo(Map<String, Object> map);

    /**
     * 获取我的拼团订单号
     * @param map
     * @return
     */
    List<String> getSnoByGroupOpen(Map<String,Object> map);

    /**
     * 获取团队参与的所有订单
     */
    List<Map<String, Object>> getUserJoinGroupOrderInfo(Map<String, Object> map);

    int countUserJoinGroupOrderInfo(Map<String, Object> map);

    /**
     * 获取参与拼团活动的所有人
     */
    @Select("select `order`.id orderId,a.id,u.user_id,u.user_name,u.headimgurl from lkt_group_open_record a inner join lkt_order `order` on `order`.sno=a.sno inner join lkt_user u on u.user_id=a.user_id where a.open_id=#{openId} and a.goods_id=#{goodsId} and a.recycle=0 order by add_date ")
    List<Map<String, Object>> getUserJoin(String openId, int goodsId);

    @Select("select u.id,u.user_id,u.user_name,u.headimgurl from lkt_user u where user_id=#{userId} and store_id=#{storeId}")
    Map<String, Object> getUserInfoById(int storeId, String userId);

    //获取用户是否参与当前团
    @Select("select count(1) from lkt_group_open_record a inner join lkt_group_open b on a.open_id=b.id and b.status=0 where a.user_id=#{userId} and b.id=#{openId} and a.recycle=0 ")
    int getUserJoinNumByOpenId(String userId, String openId);

    //获取用户参团数量
    @Select("select count(1) from lkt_group_open_record a inner join lkt_group_open b on a.open_id=b.id and b.status=0 " +
            " where a.user_id=#{userId}  and a.recycle=0 and b.activity_id=#{acId} ")
    int getUserJoinNum(String userId, String acId);


    //获取用户某活动下某商品参团是否超过一次-返回null则为没有参与过,否则返回次数
    @Select("select count(1) from lkt_group_open_record a inner join lkt_group_open b on a.open_id=b.id " +
            " where a.user_id=#{userId} and a.recycle=0 and b.activity_id=#{acId} and b.goods_id=#{goodsId}" +
            " group by b.activity_id,a.user_id having count(a.goods_id)>0 ")
    Integer getUserJoinNumByAcGoodsId(String acId, String userId, Integer goodsId);

    //获取团队参与人数
    @Select("select count(distinct user_id) from lkt_group_open_record where open_id=#{openId} and recycle=0 ")
    int getTeamJoinNum(String openId);

    //获取拼团所需人数
    @Select("select x.team_num from lkt_group_open x inner join lkt_group_open_record y on x.id=y.open_id where y.sno=#{sNo}")
    int getTeamNumBysNO(String sNo);

    //获取团长开团佣金
    @Select("SELECT\n" +
            "\to.team_commission \n" +
            "FROM\n" +
            "\tlkt_group_open_record r\n" +
            "\tLEFT JOIN lkt_group_open o ON r.open_id = o.id \n" +
            "WHERE\n" +
            "\tr.sno = #{sNo} \n" +
            "\tAND r.user_id != o.user_id \n" +
            "\tAND r.recycle = 0")
    BigDecimal selectOpenCommission(String sNo);


    /**
     * 获取参与拼团活动
     */
    @Select("select `order`.id orderId, `order`.sno sNo, `order`.store_id storeId, `order`.user_id userId from lkt_group_open_record a inner join lkt_order `order` on `order`.sno=a.sno " +
            " where a.open_id=#{openId} and a.recycle=0 order by add_date")
    List<Map<String, Object>> getAllOpenRecordOrder(String openId);

    int countGroup(Map<String, Object> parmaMap);

}