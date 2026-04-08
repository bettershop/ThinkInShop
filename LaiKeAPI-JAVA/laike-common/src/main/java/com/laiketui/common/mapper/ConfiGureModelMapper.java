package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.config.ConfiGureModel;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 商品属性表
 *
 * @author Trick
 * @date 2020/10/10 19:29
 */
public interface ConfiGureModelMapper extends BaseMapper<ConfiGureModel>
{

    @Override
    List<Map<String, Object>> selectDynamic(Map<String, Object> map) throws LaiKeAPIException;

    @Override
    int countDynamic(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 获取某产品库存数量
     *
     * @param pid -
     * @return BigDecimal
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/10/13 14:09
     */
    @Select("select ifnull(sum(num),0) as num from lkt_configure where pid = #{pid} and recycle = 0")
    Integer countConfigGureNum(int pid) throws LaiKeAPIException;

    /**
     * 获取某产品库存数量
     *
     * @param pid -
     * @return BigDecimal
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/10/13 14:09
     */
    @Select("update  lkt_configure set min_inventory =  #{min_inventory}  where pid = #{pid} and recycle = 0")
    Integer setConfigGureWarnNum(int min_inventory,int pid) throws LaiKeAPIException;

    /**
     * 获取当前规格库存
     *
     * @param attrId -
     * @return Integer
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/3/31 18:35
     */
    @Select("select ifnull(sum(num),0) as num from lkt_configure where id = #{attrId}")
    Integer sumConfigGureNum(int attrId) throws LaiKeAPIException;

    /**
     * 获取某产品最低价格
     *
     * @param pid -
     * @return BigDecimal
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/10/13 14:09
     */
    @Select("SELECT ifnull(min(price),0) FROM lkt_configure where pid = #{pid}  and recycle=0")
    BigDecimal getProductMinPrice(int pid) throws LaiKeAPIException;

    @Select("select pid from lkt_configure where id=#{attrId} and recycle=0")
    Integer getGoodsId(Integer attrId);

    @Select("select price from lkt_configure where id=#{attrId} and recycle=0")
    BigDecimal getGoodsPirce(Integer attrId);


    /**
     * 获取商品最小出售价格、最大原价格
     *
     * @param pid -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/9 12:08
     */
    @Select("SELECT ifnull(min(costprice),0) costprice, ifnull(min(price),0) price,ifnull(min(live_price),0) live_price,ifnull(max(yprice),0) yprice,ANY_VALUE(img) AS img,ANY_VALUE(unit) AS unit FROM lkt_configure WHERE pid = #{pid} and recycle=0")
    ConfiGureModel getProductMinPriceAndMaxYprice(int pid) throws LaiKeAPIException;

    /**
     * 获取直播商品最小出售价格、最大原价格
     *
     * @param pid -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/9 12:08
     */
    @Select("SELECT ifnull(min(costprice),0) costprice, ifnull(min(live_price),0) price,ifnull(min(live_price),0) live_price, ifnull(max(yprice),0) yprice,ANY_VALUE(img) AS img,ANY_VALUE(unit) FROM lkt_configure WHERE pid = #{pid} and recycle=0 and ifnull(commission,'')!='' ")
    ConfiGureModel getLiveProductMinPriceAndMaxPrice(int pid) throws LaiKeAPIException;

    @Select("SELECT ifnull(min(a.live_price),0) live_price\n" +
            "FROM lkt_living_product a\n" +
            "left join lkt_configure b on b.id  = a.config_id\n" +
            "WHERE a.living_id=#{roomId} and pro_id =#{pid}  and a.recycle=0 and ifnull(commission,'')!='';")
    BigDecimal getLivePrice(int pid, String roomId) throws LaiKeAPIException;

    /**
     * 获取商品最大出售价格
     *
     * @param pid -
     * @return Map
     * @throws LaiKeAPIException -
     * @author gp
     * @date 2023-09-05
     */
    @Select("SELECT ifnull(min(costprice),0) costprice, ifnull(max(price),0) price, ifnull(max(price),0) yprice,ANY_VALUE(img) AS img,ANY_VALUE(unit) FROM lkt_configure WHERE pid = #{pid} and recycle=0")
    ConfiGureModel getProductMaxPriceAndMaxPrice(int pid) throws LaiKeAPIException;

    /**
     * 获取活动中商品最低价格
     *
     * @param configModel -
     * @return BigDecimal
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/10/13 14:09
     */
    @Select("select min(c.price) price from lkt_configure as c " +
            " left join lkt_product_list as p on c.pid=p.id" +
            " where c.pid=#{pid} and c.recycle=0 limit 1")
    BigDecimal getActivityGoodsLowPrice(ConfiGureModel configModel) throws LaiKeAPIException;

    /**
     * 获取活动中商品最低价格 + 规格
     *
     * @param configModel -
     * @return BigDecimal
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/10/13 14:09
     */
    @Select("select min(c.price) from lkt_configure as c " +
            " left join lkt_product_list as p on c.pid=p.id" +
            " where c.pid=#{pid} " +
            " and c.id=#{id}")
    BigDecimal getActivityGoodsLowPriceByAttrId(ConfiGureModel configModel) throws LaiKeAPIException;


    /**
     * 增/减商品规格库存
     *
     * @param confiGureModel -
     * @return int
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/19 15:40
     */
    @Update("update lkt_configure set total_num = total_num + #{total_num},num = num + #{num} where pid = #{pid} and id = #{id}")
    int addGoodsAttrStockNum(ConfiGureModel confiGureModel) throws LaiKeAPIException;

    /**
     * 预售商品-增/减商品规格库存
     *
     * @param confiGureModel
     * @return
     * @throws LaiKeAPIException
     */
    @Update("update lkt_configure set total_num = total_num + #{total_num},num = num + #{num} where pid = #{pid}")
    int addGoodsPreSellAttrStockNum(ConfiGureModel confiGureModel) throws LaiKeAPIException;


    /**
     * 增加商品规格库存
     *
     * @param num -
     * @param id  -
     * @return int
     * @throws LaiKeAPIException -
     * @update 2022-06-23 14:52:03 总库存只增不减
     * @author Trick
     * @date 2020/12/14 16:40
     */
    @Update("update lkt_configure set num = num + #{num} where id = #{id}")
    int addGoodsAttrStockNumByPid(int num, int id) throws LaiKeAPIException;


    /**
     * 删除某商品规格信息
     *
     * @param pid -
     * @return pid
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/20 17:44
     */
    @Update("update lkt_configure set recycle = 1 where pid=#{pid}")
    int delConfiGureInfo(int pid) throws LaiKeAPIException;


    /**
     * 回收除了当前的属性
     *
     * @param pid     -
     * @param pidList -
     * @return int
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/3/22 16:48
     */
    @Update("<script>" +
            "update lkt_configure set recycle = 1 where " +
            " pid=#{pid} " +
            " <if test='pidList != null '> " +
            "   <foreach collection=\"pidList\" item=\"id\" separator=\",\" open=\"and id not in(\" close=\")\"> " +
            "        #{id,jdbcType=INTEGER}" +
            "   </foreach> " +
            "</if> " +
            "</script>")
    int delAppointConfiGureInfo(int pid, List<Integer> pidList) throws LaiKeAPIException;


    /**
     * 获取回收了的属性id
     *
     * @param pid     -
     * @param pidList -
     * @return int
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/3/22 16:48
     */
    @Select("<script>" +
            "select id from lkt_configure where " +
            " pid=#{pid} " +
            " <if test='pidList != null '> " +
            "   <foreach collection=\"pidList\" item=\"id\" separator=\",\" open=\"and id not in(\" close=\")\"> " +
            "        #{id,jdbcType=INTEGER}" +
            "   </foreach> " +
            "</if> " +
            "</script>")
    List<Integer> getAppointConfiGureId(int pid, List<Integer> pidList) throws LaiKeAPIException;

    /**
     * 增减库存
     *
     * @param num -
     * @param cid -
     * @return int
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/19 16:47
     */
    @Update("update lkt_configure set num = num - #{num} where id = #{cid}")
    int reduceGoodsStockNum(int num, int cid) throws LaiKeAPIException;

    /**
     * 增减商品所有规格库存
     *
     * @param num -
     * @param cid -
     * @return int
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/19 16:47
     */
    @Update("update lkt_configure set num = num - #{num} where pid = #{pid}")
    int reduceProAllAttrNum(int num, int pid) throws LaiKeAPIException;

    /**
     * 更新赠品库存
     *
     * @param give_id
     * @return
     */
    @Update("update lkt_configure set num = num - 1 where pid = #{give_id} order by id limit 1")
    int reduceGiveGoodsStockNum(int give_id);


    /**
     * 获取商品库存信息动态 sql
     *
     * @param map -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/7 15:27
     */
    List<Map<String, Object>> getProductListLeftJoinMchDynamic(Map<String, Object> map) throws LaiKeAPIException;

    int countProductListLeftJoinMchDynamic(Map<String, Object> map);


    /**
     * 获取商品库存信息
     *
     * @param storeId -
     * @param goodsId -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/23 16:30
     */
    Map<String, Object> getGoodsConfigureList(@Param("storeId") int storeId, @Param("goodsId") int goodsId) throws LaiKeAPIException;


    /**
     * 获取商品库存和属性库存信息
     *
     * @param attrId -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/4 15:13
     */
    @Select("SELECT b.min_inventory, a.num,a.pid,a.total_num,b.product_title,a.price,a.img,b.mch_id FROM lkt_configure a, lkt_product_list b " +
            "WHERE a.id = #{attrId} AND b.id = a.pid AND b.id = a.pid")
    Map<String, Object> getGoodsStockInfo(int attrId) throws LaiKeAPIException;


    /**
     * 获取拼团商品规格信息
     *
     * @param goodsId    -
     * @param activityNo -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/4/2 15:10
     */
    @Select("select b.* from lkt_group_product a,lkt_configure b where a.attr_id=b.id and b.pid=#{goodsId} and activity_no =#{activityNo}")
    List<ConfiGureModel> selectGroupGureListByPid(int goodsId, int activityNo) throws LaiKeAPIException;

    /**
     * 获取库存记录相关信息
     *
     * @param map -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/6/2 15:42
     */
    List<Map<String, Object>> selectStockInfo(Map<String, Object> map) throws LaiKeAPIException;

    int countStockInfo(Map<String, Object> map) throws LaiKeAPIException;

    List<Map<String, Object>> getWarnedGoodsInfo(Map<String, Object> param);

    @Select("SELECT DISTINCT " +
            "COUNT( a.id) " +
            "FROM " +
            "lkt_configure a " +
            "LEFT JOIN lkt_product_list b ON a.pid = b.id " +
            "INNER JOIN lkt_stock s ON a.id = s.attribute_id and a.pid = product_id " +
            "WHERE " +
            "b.store_id = #{storeid} " +
            "AND s.type = 2 " +
            "AND s.add_date >= #{date} " +
            "AND a.recycle = 0 " +
            "AND b.recycle = 0")
    Long countWarnedStock(int storeid, String date);

    /**
     * 获取默认规格价格
     *
     * @param pid -
     * @return BigDecimal
     * @author Trick
     * @date 2023/4/10 11:51
     */
    @Select("SELECT price FROM lkt_configure where pid = #{pid} and recycle=0 order by ctime limit 1")
    BigDecimal getProductDefaultPrice(int pid) throws LaiKeAPIException;

    /**
     * 获取默认规格id
     *
     * @param pid -
     * @return BigDecimal
     * @author Trick
     * @date 2023/4/10 11:51
     */
    @Select("SELECT id FROM lkt_configure where pid = #{pid} and recycle=0 order by ctime limit 1")
    int getProductDefaultId(int pid);

//    @Update("update lkt_configure set commission=#{commission},update_time=now() where id = #{id}")
//    int updateCommissionById(Integer id,double commission);

    /**
     * 修改佣金和价格信息
     *
     * @param id
     * @param commission
     * @param live_price
     * @return
     */
    @Update("update lkt_configure set commission=#{commission},update_time=now(),live_price=#{live_price} where id = #{id}")
    int updateCommissionById(Integer id, String commission, BigDecimal live_price) throws LaiKeAPIException;

    /**
     * 取消佣金设置
     *
     * @param pid
     * @return
     */
    @Update("update lkt_configure set commission='',live_price=0 where pid = #{pid}")
    int updateCommissionByPId(Integer pid) throws LaiKeAPIException;

    @Select("select * from lkt_configure where pid= #{pid} and recycle=0 and ifnull(commission,'')!='' ")
    List<ConfiGureModel> getLiveConfiGureModelList(Integer pid) throws LaiKeAPIException;

    //获取默认规格的价格
    @Select("select ifnull(price,0) from lkt_configure where pid=#{goodsId} and recycle=0 order by ctime,id limit 1")
    BigDecimal getAttrDefaultMoney(Integer goodsId);

}