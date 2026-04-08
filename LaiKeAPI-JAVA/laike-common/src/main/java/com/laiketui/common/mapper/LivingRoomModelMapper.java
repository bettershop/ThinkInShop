package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.living.LivingRoomModel;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @author zhuqingyu
 * @create 2024/5/29
 */
public interface LivingRoomModelMapper extends BaseMapper<LivingRoomModel>
{
    /**
     * 直播间管理-查询场次
     *
     * @param map -
     * @return
     * @throws LaiKeAPIException
     */
    List<Map<String, Object>> selectDynamic(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 直播间管理-查询场次-计数
     *
     * @param map -
     * @return
     * @throws LaiKeAPIException
     */
    int countDynamic(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 统计直播间商品数量
     *
     * @param storeId
     * @param roomId
     * @return
     * @throws LaiKeAPIException
     */
    @Select(" select count(1) from (select count(1) as sl from lkt_living_product a left join lkt_configure c on c.id = a.config_id where a.store_id = #{storeId} and ifnull(c.commission,'')!='' and a.living_id = #{roomId} and a.recycle=0 group by a.pro_id) z;")
    int liveRoomProCount(Integer storeId, String roomId) throws LaiKeAPIException;

    /**
     * 同进订单信息
     *
     * @param storeId
     * @param roomId
     * @return
     * @throws LaiKeAPIException
     */
    @Select("select sum(commission) as commission,count(1) as order_num,sum(num) as xl_num,sum(after_discount) as order_money from lkt_order_details where store_id = #{storeId} and living_room_id = #{roomId} and recycle=0 and r_status!=7")
    Map<String, Object> liveRoomOrderNumCount(Integer storeId, String roomId) throws LaiKeAPIException;

    /**
     * 查询订单信息
     *
     * @param map
     * @return
     * @throws LaiKeAPIException
     */
    List<Map<String, Object>> selectOrderDynamic(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 查询订单信息-计数
     *
     * @param map
     * @return
     * @throws LaiKeAPIException
     */
    int countOrderDynamic(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 查询关注列表
     *
     * @param map
     * @return
     * @throws LaiKeAPIException
     */
    List<Map<String, Object>> selectFollowDynamic(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 查询关注列表-计数
     *
     * @param map
     * @return
     * @throws LaiKeAPIException
     */
    int countFollowDynamic(Map<String, Object> map) throws LaiKeAPIException;

    List<Map<String, Object>> queryGovernIndex(Map<String, Object> map) throws LaiKeAPIException;

    @Select("select count(1) from lkt_living_follow_list where anchor_id = #{anchor} and recycle=0;")
    int queryFsNumByAnchor(String anchor) throws LaiKeAPIException;

    List<Map<String, Object>> queryFans(Map<String, Object> map) throws LaiKeAPIException;

    int countFans(Map<String, Object> map) throws LaiKeAPIException;


    List<Map<String, Object>> queryFollow(Map<String, Object> map) throws LaiKeAPIException;

    int countFollow(Map<String, Object> map) throws LaiKeAPIException;

    @Select("select count(1) from lkt_living_room where user_id=#{anchor_id} and living_status=1")
    int isLivingByAnchorId(String anchor_id);


    @Select("select count(1) from lkt_living_follow_list where user_id=#{userId} and anchor_id=#{anchor_id}")
    int isFollow(String userId, String anchor_id) throws LaiKeAPIException;

    /**
     * 根据直播间id查询订单数据
     *
     * @param map
     * @return
     * @throws LaiKeAPIException
     */
    List<Map<String, Object>> getOrderDetailDynamic(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 根据直播间ID获取订单信息
     *
     * @param roomId
     * @param storeId
     * @return
     * @throws LaiKeAPIException
     */
    @Select("select count(1) as orderNum,sum(a.z_price) as orderRevenue from lkt_order a left join lkt_order_details d on d.r_sNo=a.sNo where a.store_id = #{storeId} and d.living_room_id=#{roomId} and a.status in (#{rstatus})")
    Map<String, Object> countOrderNum(String roomId, int storeId, String rstatus) throws LaiKeAPIException;

    /**
     * 根据直播间ID获取所有订单信息
     *
     * @param roomId
     * @param storeId
     * @return
     * @throws LaiKeAPIException
     */
    @Select("select count(1) as orderNum,sum(a.z_price) as orderRevenue from lkt_order a left join lkt_order_details d on d.r_sNo=a.sNo where a.store_id = #{storeId} and d.living_room_id=#{roomId} and a.status in (1,2,5,0,7)")
    Map<String, Object> countAllOrderNum(String roomId, int storeId) throws LaiKeAPIException;

    /**
     * 查询主播
     *
     * @param map
     * @return
     * @throws LaiKeAPIException
     */
    List<Map<String, Object>> queryRecommend(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 查询主播-计数
     *
     * @param map
     * @return
     * @throws LaiKeAPIException
     */
    int countRecommend(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 获取主播的佣金
     *
     * @param user_id
     * @param store_id
     * @return
     */
    @Select("select sum(ifnull(d.commission,0)) from lkt_order o left join lkt_order_details d on o.sNo=d.r_sNo  where o.store_id=#{store_id} and o.otype='ZB' and d.anchor_id=#{user_id} and o.status in (0,1,2,5)")
    BigDecimal queryCommissionByUserId(String user_id, Integer store_id);

    /**
     * 获取主播的销量
     *
     * @param user_id
     * @param store_id
     * @return
     */
    @Select("select sum(ifnull(d.num,0)) as xl from lkt_order o left join lkt_order_details d on o.sNo=d.r_sNo  where o.store_id=#{store_id} and o.otype='ZB' and d.anchor_id=#{user_id} and o.status in (0,1,2,5)")
    Map<String, Object> queryXlNumByUserId(String user_id, Integer store_id);

    /**
     * 获取正在直播的主播
     *
     * @param store_id
     * @return
     */
    List<Map<String, Object>> queryLiving(@Param("store_id") Integer store_id, @Param("pageSize") int pageSize) throws LaiKeAPIException;

    /**
     * 获取记录
     *
     * @param map
     * @return
     * @throws LaiKeAPIException
     */
    List<Map<String, Object>> queryRecord(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 记录计数
     *
     * @param map
     * @return
     * @throws LaiKeAPIException
     */
    int countRecord(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 移动端-主播中心查询主播数据
     *
     * @param user_id
     * @return
     */
    @Select("select * from lkt_living_room where user_id=#{user_id} and recycle=0 and living_status in (1,2) and store_id=#{store_id}")
    List<Map<String, Object>> queryLivingByUserId(String user_id, Integer store_id);

    /**
     * 获取最大的直播间排序
     */
    @Select("select * from lkt_living_room where store_id=#{store_id} order by sort desc limit 1")
    Map<String, Object> maxSort(Integer store_id);

    /**
     * 判断开播时除了传进来的ID是否还有别的在开播的直播
     *
     * @param userId
     * @param roomId
     * @return
     */
    @Select("select count(1) from lkt_living_room where user_id = #{userId} and living_status=1 and id!=#{roomId}")
    int countStatus(String userId, Integer roomId);
}
