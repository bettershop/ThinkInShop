package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.mch.IntegralGoodsModel;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;


/**
 * 积分商城商品表 sql映射
 *
 * @author Trick
 * @date 2020/10/12 11:19
 */
public interface IntegralGoodsModelMapper extends BaseMapper<IntegralGoodsModel>
{

    @Override
    List<Map<String, Object>> selectDynamic(Map<String, Object> map) throws LaiKeAPIException;

    @Override
    int countDynamic(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 获取积分商品信息
     *
     * @param map -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/10/12 11:21
     */
    List<Map<String, Object>> getGoodsInfo(Map<String, Object> map);

    int countGoodsInfo(Map<String, Object> map);

    /**
     * 获取积分商品信息
     *
     * @param map -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/10/12 11:21
     */
    List<Map<String, Object>> getGoodsInfo1(Map<String, Object> map) throws LaiKeAPIException;

    int countGoodsInfo1(Map<String, Object> map);

    /**
     * 获取积分商品明细
     *
     * @param id - 商品id
     * @return Map
     * @author Trick
     * @date 2021/4/14 10:53
     */
    @Select("select g.id, g.is_delete,g.integral,g.money,g.goods_id,g.attr_id,a.real_volume, g.num,a.content,a.mch_id,a.product_title,a.volume,a.status,a.imgurl," +
            "a.freight,a.initial,a.weight from lkt_integral_goods as g " +
            "left join lkt_product_list as a on g.goods_id=a.id where g.goods_id = #{id} and g.attr_id = #{attrId} and g.is_delete = 0")
    Map<String, Object> getGoodsDetailInfo(int id, int attrId);

    /**
     * 获取积分商品明细
     *
     * @param id - 积分商品id
     * @return Map
     * @author Trick
     * @date 2021/4/14 10:53
     */
    @Select("select g.is_delete,g.integral,g.money,g.goods_id,g.attr_id,a.content,a.mch_id,a.product_title,a.volume,a.real_volume,a.status,a.imgurl," +
            "a.freight,a.initial,a.weight from lkt_integral_goods as g " +
            "left join lkt_product_list as a on g.goods_id=a.id where g.id = #{id}")
    Map<String, Object> getGoodsDetailInfoById(int id);

    /**
     * 获取商城最大列序号
     *
     * @param storeId -
     * @return int
     * @author Trick
     * @date 2021/5/12 14:17
     */
    @Select("SELECT IFNULL(MAX(sort), 0) FROM lkt_integral_goods WHERE store_id=#{storeId}")
    int getMaxSort(int storeId);


    @Select("select g.integral,g.money,c.img from lkt_integral_goods as g left join lkt_configure as c on g.attr_id = c.id where g.id=#{gid} ")
    Map<String, Object> getGoodsInfo2(String gid);


    //获取商品与库存量
    @Select("SELECT IFNULL(SUM(a.num), 0) AS total_num FROM lkt_integral_goods a WHERE a.goods_id = #{goodsId} AND a.is_delete = 0 AND a.attr_id = #{attrId}")
    int getGoodsStockNum(int goodsId, int attrId);

    //增减库存
    @Update("update lkt_integral_goods a set a.num=a.num + #{stockNum} where a.id=#{integralId} and a.num>=0")
    int addStockNum(int integralId, int stockNum);

    //增减库存
    @Update("update lkt_integral_goods a set a.num=a.num + #{stockNum},a.max_num = a.max_num + #{stockNum} where a.id=#{integralId} and a.num>=0")
    int addStockNum1(int integralId, int stockNum);

    //释放库存
    @Update("update lkt_integral_goods a set a.num=a.num+(select if(num is null,0,sum(num)) num from lkt_order_details b where b.r_sno=#{orderNo} and b.p_id=#{id}) where a.id=#{id}")
    int releaseStockNum(String orderNo, int id);

    /**
     * 获取多规格的最低价格商品
     *
     * @return
     */
    @Select("SELECT \n" +
            "    goods_id, \n" +
            "    MIN(id) AS id, \n" +
            "    MIN(money) AS min_money \n" +
            "FROM \n" +
            "    `lkt_integral_goods` \n" +
            "WHERE \n" +
            "    is_delete = 0 \n" +
            "    AND store_id = #{storeId} \n" +
            "GROUP BY \n" +
            "    goods_id;")
    List<Map<String, Object>> selectMinGoods(Integer storeId);

    /**
     * 获取多规格的所有规格商品数量和
     *
     * @return
     */
    @Select("SELECT sum(num)  FROM `lkt_integral_goods` WHERE is_delete = 0 and store_id = #{storeId} and goods_id = #{goodsId}")
    Integer selectAllGoodsNum(Integer storeId, Integer goodsId);

    /**
     * 获取积分商品兑换记录
     */
    List<Map<String, Object>> getRecordsByGoodsId(Map<String, Object> map);

    /**
     * 获取积分商品兑换记录
     */
    Integer getRecordsByGoodsIdcount(Map<String, Object> map);

    /**
     * 根据商品id 规格id判断商品是否存在
     *
     * @param storeId
     * @param goodsId
     * @return
     */
    @Select("SELECT count(0)  FROM `lkt_integral_goods` WHERE is_delete = 0 and store_id = #{storeId} and goods_id = #{goodsId} and attr_id = #{attrId}")
    Integer selectGoodsNumByGoodsIdAndAttrId(Integer storeId, Integer goodsId, Integer attrId);

    /**
     * 判断当前商品所有规格是否全部下架
     *
     * @param storeId
     * @param goodsId
     * @return
     */
    @Select("SELECT count(0)  FROM `lkt_integral_goods` WHERE is_delete = 0 and store_id = #{storeId} and goods_id = #{goodsId} and status = 2")
    Integer selectGoodsNumByGoodsId(Integer storeId, Integer goodsId);

    /**
     * 根据 规格id商品获取积分商品剩余库存
     *
     * @param storeId
     * @param attrId
     * @return
     */
    @Select("SELECT num  FROM `lkt_integral_goods` WHERE is_delete = 0 and store_id = #{storeId} and attr_id = #{attrId}")
    Integer selectAttrNumByAttrId(Integer storeId, Integer attrId);

    @Update("update lkt_integral_goods set num = num + #{num} where attr_id = #{cid} and goods_id = #{goodsId} and store_id = #{storeId}")
    int addGoodsStockNum(int num,Integer cid,Integer goodsId,Integer storeId);
}