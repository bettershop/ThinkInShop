package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.plugin.seckill.PtSecondsProModel;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

/**
 * 平台 秒杀活动
 *
 * @author Trick
 */
public interface PtSecondsProModelMapper extends BaseMapper<PtSecondsProModel>
{
    @Override
    List<Map<String, Object>> selectDynamic(Map<String, Object> map) throws LaiKeAPIException;

    @Override
    int countDynamic(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 订单确认页面获取商品信息（结算）
     *
     * @param storeId -
     * @param needNum -
     * @param secId   -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/4/9 16:49
     */
    @Select("select m.product_title,m.volume,a.seconds_price price,c.unit,c.attribute,c.img,c.yprice,m.freight,a.free_freight" +
            " ,m.product_class,m.brand_id,m.weight,m.mch_id,m.is_distribution  " +
            " from lkt_pt_seconds_pro a inner join lkt_product_list AS m on a.pro_id=m.id LEFT JOIN lkt_configure AS c ON c.id=a.attr_id " +
            " where m.store_id = #{storeId} and a.num >= #{needNum} and m.status = 2 and a.id=#{secId} ")
    Map<String, Object> settlementProductsInfo(int storeId, int needNum, int secId) throws LaiKeAPIException;

    /**
     * 获取店铺自己的拼团商品id
     *
     * @param storeId -
     * @param mchId   -
     * @param formId  -
     * @return List
     * @author Trick
     * @date 2021/4/25 17:56
     */
    @Select("select attr_id from lkt_pt_seconds_pro where store_id = #{storeId} and mch_id = #{mchId} and platform_activities_id = #{formId} " +
            " and audit_status in (1,2)")
    List<Integer> selectMchGoodsId(int storeId, int mchId, int formId);


    /**
     * 获取秒杀商品列表
     *
     * @param map -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/4/8 15:38
     */
    List<Map<String, Object>> secGoodsList(Map<String, Object> map) throws LaiKeAPIException;

    int countSecGoodsList(Map<String, Object> map);


    List<Map<String, Object>> secGoodsListUser(Map<String, Object> map) throws LaiKeAPIException;

    int countSecGoodsListUser(Map<String, Object> map);


    /**
     * 增减库存
     *
     * @param secId -
     * @param num   -
     * @return int
     * @author Trick
     * @date 2021/4/12 15:09
     */
    @Update("update lkt_pt_seconds_pro set num = num + #{num} where id=#{secId} and is_delete  = 0")
    int addStockNum(int secId, int num);


}