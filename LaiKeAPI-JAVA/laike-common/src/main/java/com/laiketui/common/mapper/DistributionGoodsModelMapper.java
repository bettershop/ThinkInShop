package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.distribution.DistributionGoodsModel;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;


/**
 * 分销商品表
 *
 * @author Trick
 * @date 2021/2/7 17:19
 */
public interface DistributionGoodsModelMapper extends BaseMapper<DistributionGoodsModel>
{

    /**
     * 获取分销商品
     *
     * @param map -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/20 10:56
     */
    List<Map<String, Object>> selectGoodsInfo(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 获取分销商品-统计
     *
     * @param map -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/20 10:56
     */
    int countGoodsInfo(Map<String, Object> map) throws LaiKeAPIException;


    /***
     * 订单下单时 获取分销商品分销配置信息
     * @param map
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> getDistributionGoodInfos(Map<String, Object> map) throws LaiKeAPIException;


    /**
     * 非分佣商品
     *
     * @param map -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/2/8 9:44
     */
    List<Map<String, Object>> selectNotDistributionGoodsInfo(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 非分佣商品-统计
     *
     * @param map -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/2/8 9:44
     */
    int countNotDistributionGoodsInfo(Map<String, Object> map) throws LaiKeAPIException;


    /**
     * 批量删除分销商品
     *
     * @param ids -
     * @return int
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/2/8 11:05
     */
    int batchDelById(List<Integer> ids) throws LaiKeAPIException;


    /**
     * 获取分销商品类别-只获取一级分类
     *
     * @param storeId -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/2/19 9:22
     */
    @Select("select distinct substring_index(substring_index(a.product_class, '-', 2), '-', -1) from lkt_product_list as a right join lkt_distribution_goods as b on a.store_id = b.store_id and a.id = b.p_id " +
            " left join lkt_configure as c on b.s_id = c.id " +
            " left join lkt_product_class as cls on cls.cid = substring_index(substring_index(a.product_class, '-', 2), '-', -1) " +
            " where a.store_id = #{storeId} and a.status = 2 and c.recycle = 0 and b.recycle = 0 and b.uplevel = 0 and cls.level=0 " +
            " order by b.add_time desc")
    List<Integer> selectDistributionGoodsClassInfo(int storeId) throws LaiKeAPIException;


    /**
     * 获取订单信息
     *
     * @param storeId -
     * @param orderno -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/5/21 17:06
     */
    @Select("select a.p_price,a.num,c.uplevel,a.after_discount + a.freight as after_discount from lkt_order_details as a left join lkt_product_list as b on b.id = a.p_id " +
            " left join lkt_distribution_goods as c on a.p_id = c.p_id and a.sid = c.s_id and c.recycle=0 where a.store_id = #{storeId} and a.r_sNo = #{orderno} limit 1")
    List<Map<String, Object>> selectOrderInfo(int storeId, String orderno) throws LaiKeAPIException;

    /**
     * 同一商品的不同规格需要在一个商品中进行展示
     *
     * @return List
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022/9/7 15:01
     */
    @Select("select s_id from lkt_distribution_goods where p_id=#{goodsId} and recycle=0")
    List<Integer> selectDistributionGoodsAtrrIds(Integer goodsId) throws LaiKeAPIException;
}