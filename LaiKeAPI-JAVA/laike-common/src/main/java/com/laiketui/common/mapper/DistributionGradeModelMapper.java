package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.mch.DistributionGradeModel;
import com.laiketui.domain.user.User;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;


/**
 * 分销等级 sql
 *
 * @author Trick
 * @date 2020/10/29 16:38
 */
public interface DistributionGradeModelMapper extends BaseMapper<DistributionGradeModel>
{

    /**
     * 获取用户分佣等级
     *
     * @param user -
     * @return DistributionGradeModel
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/10/29 17:04
     */
    @Select("select b.* from lkt_user_distribution as a left join lkt_distribution_grade as b on a.level=b.id where a.user_id=#{user_id} and a.store_id=#{store_id}")
    DistributionGradeModel getUserDistribution(User user) throws LaiKeAPIException;


    /**
     * 获取商城分销top1 分销人信息
     *
     * @param storeId -
     * @return DistributionGradeModel
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/3/23 14:25
     */
//    @Select("select sets from lkt_distribution_grade where store_id = #{storeId} order by id desc limit 0,1")
    @Select("select * from lkt_distribution_grade where store_id = #{storeId} order by id desc limit 0,1")
    DistributionGradeModel getUserTop1Distribution(int storeId) throws LaiKeAPIException;

    /**
     * 获取商城分销top1 分销人id
     *
     * @param storeId -
     * @return DistributionGradeModel
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/3/23 14:25
     */
    @Select("select id from lkt_distribution_grade where store_id = #{storeId} order by id desc limit 0,1")
    Integer getUserTop1DistributionId(int storeId) throws LaiKeAPIException;

    /**
     * 动态查询-查询
     *
     * @param map -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/2/4 17:01
     */
    @Override
    List<Map<String, Object>> selectDynamic(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 动态查询-统计
     *
     * @param map -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/2/4 17:01
     */
    @Override
    int countDynamic(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 根据订单获取分销商品信息
     *
     * @param storeId -
     * @param orderNo -
     * @return Map
     * @author Trick
     * @date 2022/1/25 16:28
     */
    @Select("select c.id,c.distribution_rule,c.p_id,c.s_id,c.uplevel from lkt_order a,lkt_order_details b,lkt_distribution_goods c where a.store_id = #{storeId} " +
            " and a.sno=#{orderNo} and a.sno = b.r_sno  and b.sid = c.s_id  and c.recycle = 0  ")
    Map<String, Object> getGoodsInfoByOrder(int storeId, String orderNo);

}
