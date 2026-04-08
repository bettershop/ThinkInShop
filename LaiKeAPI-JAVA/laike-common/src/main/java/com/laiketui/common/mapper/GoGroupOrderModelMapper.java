package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.plugin.group.GoGroupOrderModel;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 拼团订单记录表
 *
 * @author Trick
 * @date 2021/2/20 14:04
 */
public interface GoGroupOrderModelMapper extends BaseMapper<GoGroupOrderModel>
{

    /**
     * 查询拼团活动已经购买多了多少
     *
     * @param activityId -
     * @return int
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/2/20 14:06
     */
    @Select("select sum(num) as volume from lkt_go_group_order where activity_no = #{activityId}")
    Integer groupSumOrderByNum(int activityId) throws LaiKeAPIException;


    /**
     * 获取拼团订单列表
     *
     * @param map -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/3/24 9:43
     */
    List<Map<String, Object>> getGrouporder(Map<String, Object> map) throws LaiKeAPIException;


    /**
     * 统计
     *
     * @param map -
     * @return Integer
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/3/25 10:17
     */
    Integer countGrouporder(Map<String, Object> map) throws LaiKeAPIException;


    /**
     * 更具拼团编号获取成功拼团的用户信息
     *
     * @param storeId -
     * @param ptcode  -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/3/24 17:38
     */
    @Select("SELECT i.user_id, u.headimgurl FROM lkt_go_group_order i LEFT JOIN lkt_user u ON i.user_id = u.user_id " +
            "WHERE u.store_id = #{storeId} AND i.ptcode = #{ptcode} AND i.status NOT IN (0, 6, 7) ORDER BY i.id ASC")
    List<Map<String, Object>> getMemberGrouporder(int storeId, String ptcode) throws LaiKeAPIException;


    /**
     * 当前用户成功拼团数量
     *
     * @param storeId -
     * @param userId  -
     * @param ptCode  -
     * @return Integer
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/3/26 18:44
     */
    @Select("select count(id) as count from lkt_go_group_order where ptcode = #{ptCode} and user_id = #{userId} " +
            "and status NOT IN (0,6,7,8) and ptcode != '' and store_id = #{storeId}")
    Integer countGroupPayOrderNum(int storeId, String userId, String ptCode) throws LaiKeAPIException;


    /**
     * 统计拼团商品购买数量
     *
     * @param storeId -
     * @param goodsId -
     * @return Integer
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/3/30 17:18
     */
    @Select("select count(*) as sum " +
            "                   from lkt_go_group_order_details as d " +
            "                   left join lkt_go_group_order as o on d.r_sNo=o.sNo " +
            "                   where d.p_id=#{goodsId} and o.otype='pt' and d.r_status!=0 and d.r_status!=6 and d.store_id=#{storeId}")
    Integer countGroupGoodsPayNum(int storeId, int goodsId) throws LaiKeAPIException;


    /**
     * 修改订单状态
     *
     * @param storeId -
     * @param orderno -
     * @param status  -
     * @return Integer
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/4/1 11:19
     */
    @Update("update lkt_go_group_order set `status`=#{status} where sno=#{orderno} and store_id=#{storeId}")
    Integer updateStatus(int storeId, String orderno, int status) throws LaiKeAPIException;


    /**
     * 修改订单拼团状态
     *
     * @param storeId -
     * @param ptcode  -
     * @param status  -
     * @return Integer
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/4/2 18:28
     */
    @Update("update lkt_go_group_order set ptstatus=#{status} where ptcode=#{ptcode} and store_id=#{storeId}")
    Integer updatePtStatus(int storeId, String ptcode, int status) throws LaiKeAPIException;


    /**
     * 关闭失效订单
     *
     * @param storeId -
     * @param endTime -
     * @return Integer
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/4/6 15:26
     */
    @Update("update lkt_go_group_order set status=7 where status = 0 and store_id=#{storeId} and add_time<#{endTime}")
    Integer closeOrder(int storeId, Date endTime) throws LaiKeAPIException;


    /**
     * 获取失效订单
     *
     * @param storeId -
     * @param endTime -
     * @return Integer
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/4/6 15:55
     */
    @Update("select * from lkt_go_group_order where status = 0 and store_id=#{storeId} and add_time<#{endTime}")
    List<GoGroupOrderModel> selectInvalidOrder(int storeId, Date endTime) throws LaiKeAPIException;

    /**
     * 获取拼团明细
     *
     * @param ptcode -
     * @return List
     * @author Trick
     * @date 2021/5/8 18:12
     */
    @Select("SELECT u.user_name,gp.starttime,gp.endtime,d.p_name,c.img,d.p_id as goods_id,d.sid as attr_id,o.* from lkt_order as o " +
            " LEFT JOIN lkt_user as u on o.user_id = u.user_id LEFT JOIN lkt_order_details as d on o.sNo = d.r_sNo " +
            " LEFT JOIN lkt_configure as c on d.sid = c.id LEFT JOIN lkt_group_open as g on o.ptcode = g.ptcode " +
            " LEFT JOIN lkt_group_product as gp on gp.product_id = d.p_id and gp.activity_no = g.activity_no " +
            " where d.r_status != 6 AND g.ptcode = #{ptcode} group by o.id ORDER BY add_time desc")
    List<Map<String, Object>> groupDetailList(String ptcode);
}