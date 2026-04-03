package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.seckill.SecondsProModel;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;


/**
 * 秒杀配置表
 *
 * @author Trick
 * @date 2020/10/14 11:58
 */
public interface SecondsProModelMapper extends BaseMapper<SecondsProModel>
{


    @Update("update lkt_seconds_pro set num = num + #{num} where attr_id = #{cid}")
    int addGoodsStockNum(int num, Integer cid);

    /**
     * 获取当前进行中的秒杀商品
     *
     * @param map -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/10/14 12:00
     */
    List<Map<String, Object>> getCurrentSeconds(Map<String, String> map) throws LaiKeAPIException;


    /**
     * 获取秒杀商品列表
     * 【请使用 getSecGoodsList】
     *
     * @param map -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/4/8 15:38
     */
    @Deprecated
    List<Map<String, Object>> secGoodsList(Map<String, Object> map) throws LaiKeAPIException;

    @Deprecated
    int countSecGoodsList(Map<String, Object> map) throws LaiKeAPIException;


    /**
     * 订单确认页面获取商品信息（结算）
     *
     * @param storeId -
     * @param needNum -
     * @param secId   -
     * @param attrId  - 规格id 新秒杀是商品,未到规格
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/4/9 16:49
     */
    @Select("select m.product_title,m.volume,a.seconds_price price,a.price_type,c.price attrPrice,c.unit,c.attribute,c.img,c.yprice,m.freight" +
            " ,m.product_class,m.brand_id,m.weight,m.mch_id,m.is_distribution  " +
            " from lkt_seconds_activity a inner join lkt_product_list AS m on a.goodsId=m.id LEFT JOIN lkt_configure AS c ON c.id=#{attrId} " +
            " where m.store_id = #{storeId} and a.num >= #{needNum} and m.status = 2 and a.id=#{secId} ")
    Map<String, Object> settlementProductsInfo(int storeId, int needNum, int secId, int attrId) throws LaiKeAPIException;


    /**
     * 获取秒杀后台活动列表
     *
     * @param storeId -
     * @return List
     * @author Trick
     * @date 2021/5/6 10:06
     */
    @Select("SELECT sp.*,pl.product_title,c.price,m.name " +
            "FROM `lkt_seconds_pro` as sp " +
            "LEFT JOIN lkt_product_list as pl on pl.id = sp.pro_id " +
            "LEFT JOIN lkt_configure as c on c.id = sp.attr_id " +
            "LEFT JOIN lkt_mch as m on m.id = pl.mch_id " +
            "WHERE sp.store_id = #{storeId} and sp.is_delete = 0")
    List<Map<String, Object>> getAdminSecondsList(int storeId);


    /**
     * 增减库存
     *
     * @param secId -
     * @param num   -
     * @return int
     * @author Trick
     * @date 2021/4/12 15:09
     */
    @Update("update lkt_seconds_pro set num = num + #{num},max_num=max_num+#{num} where id=#{secId} and is_delete  = 0")
    int addStockNum(int secId, int num);

    //置空库存
    @Update("update lkt_seconds_pro set num = 0 where activity_id=#{acId} and is_delete  = 0")
    int resettingStockByAcId(int acId);

    int countSecondsGoodsListByLabelId(Map<String, Object> map) throws LaiKeAPIException;


    //获取用户秒杀购买数量
    /*@Select(" select if(b.num is null,0,sum(b.num)) from lkt_seconds_record a,lkt_order b where a.sno=b.sno " +
            " and a.activity_id=#{activityId} and a.user_id=#{userId} and b.recycle = 0 " +
            " and b.status !=7 ")*/
    int getUserSecNum(@Param("activityId") int activityId, @Param("userId") String userId);

    /**
     * 商品规格删除，秒杀插件预扣库存同样进行回收规格的库存回收
     *
     * @param attrIds
     * @return
     */
    @Update("<script>" +
            "update lkt_seconds_pro p, lkt_seconds_activity a set p.is_delete = 1,a.num = a.num - p.num, a.max_num = a.max_num - p.max_num where " +
            "p.activity_id = a.id " +
            " <if test='attrIds != null '> " +
            "   <foreach collection=\"attrIds\" item=\"id\" separator=\",\" open=\"and p.attr_id in(\" close=\")\"> " +
            "        #{id,jdbcType=INTEGER}" +
            "   </foreach> " +
            "</if> " +
            "</script>")
    int deleteByAttrId(@Param("attrIds") List<Integer> attrIds);
}