package com.laiketui.common.mapper;

import com.laiketui.core.exception.LaiKeAPIException;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * @author zhuqingyu
 * @create 2024/6/18
 * <p>
 * 直播数据
 */

public interface LivingDataModelMapper
{
    /**
     * 统计上架商品
     *
     * @param mch_id
     * @return
     */
    @Select("select count(1) from (select count(1) from lkt_product_list a left join lkt_configure b on a.id=b.pid WHERE ifnull(b.commission,'')!='' and a.mch_id=#{mch_id} group by a.id) p")
    int countPro(int mch_id);

    /**
     * 查询订单信息
     *
     * @param map
     * @return
     */
    Map<String, Object> selectOrderData(Map<String, Object> map);

    /**
     * 获取订单表数据
     *
     * @param map -
     * @return List
     * @throws LaiKeAPIException -
     */
    List<Map<String, Object>> getOrdersNumDynamic(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 查询退款信息
     *
     * @param map
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> selectReturnOrder(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 移动店铺端-直播数据-根据日期统计上架商品数
     *
     * @param map
     * @return
     * @throws LaiKeAPIException
     */
    int countProNumByData(Map<String, Object> map) throws LaiKeAPIException;
}
