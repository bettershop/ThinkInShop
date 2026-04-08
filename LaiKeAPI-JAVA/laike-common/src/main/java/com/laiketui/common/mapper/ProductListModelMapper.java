package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.product.ProductListModel;
import com.laiketui.domain.vo.PageModel;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;


/**
 * 商品表
 *
 * @author Trick
 * @date 2020/10/10 18:02
 */
public interface ProductListModelMapper extends BaseMapper<ProductListModel>
{

    @Override
    List<Map<String, Object>> selectDynamic(Map<String, Object> map) throws LaiKeAPIException;

    @Override
    int countDynamic(Map<String, Object> map) throws LaiKeAPIException;

    @Override
    BigDecimal sumDynamic(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 查询商品并分类显示返回JSON至小程序
     *
     * @param map -
     * @return List<ProductListModel>
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/10/10 18:04
     */
    List<Map<String, Object>> getProductList(Map<String, Object> map) throws LaiKeAPIException;

    int countProductList(Map<String, Object> map);

    /**
     * 获取被搜索最多的商品
     *
     * @param map -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/10/15 14:41
     */
    List<ProductListModel> getHotGoods(Map<String, Object> map) throws LaiKeAPIException;


    /**
     * 增加商品总库存
     *
     * @param pid -
     * @param num -
     * @return int
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/19 16:47
     */
    @Update("update lkt_product_list set num = num + #{num} where id = #{pid}")
    int addGoodsStockNum(@Param("pid") int pid, @Param("num") int num) throws LaiKeAPIException;

    /**
     * 根据商品获取商品信息
     *
     * @param map -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/10/30 16:30
     */
    @Select("select a.product_title,c.img,c.price,a.id from lkt_product_list AS a " +
            " LEFT JOIN lkt_configure AS c ON a.id = c.pid" +
            " where store_id = #{store_id} and a.id = #{pid}")
    List<Map<String, Object>> getGoodsJoinConfigureById(Map<String, Object> map) throws LaiKeAPIException;

    @Select("SELECT pl.product_title , c.* " +
            "    from lkt_product_list as pl " +
            "    LEFT JOIN lkt_configure as c on c.pid = pl.id " +
            "    where 1 and pl.store_id = #{storeId} and pid = #{storeId} and c.id = #{attrId}")
    Map<String, Object> getGoodsJoinConfigureByAttrid(int storeId, int attrId) throws LaiKeAPIException;

    /**
     * 获取商品标题和图片
     *
     * @param map
     * @return
     * @throws LaiKeAPIException
     */
    @Select(" select a.product_title,a.commodity_type,c.img,a.id,a.write_off_mch_ids from lkt_product_list AS a " +
            " LEFT JOIN lkt_configure AS c ON a.id = c.pid where a.store_id = #{store_id} and a.id = #{p_id} AND c.id= #{sid} ")
    List<Map<String, Object>> getGoodsTitleAndImg(Map<String, Object> map) throws LaiKeAPIException;


    /**
     * 获取被匹配的商品 动态sql
     *
     * @param map -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/10/16 9:24
     */
    List<Map<String, Object>> getProductListDynamic(Map<String, Object> map) throws LaiKeAPIException;

    int countProductListDynamic(Map<String, Object> map) throws LaiKeAPIException;


    /**
     * 获取商品信息 -
     * left join lkt_mch
     *
     * @param map -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/13 14:04
     */
    List<Map<String, Object>> getProductListLeftJoinMchDynamic(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 获取商品信息统计
     * left join lkt_mch
     *
     * @param map -
     * @return int
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/13 14:04
     */
    int getProductListLeftJoinMchCountDynamic(Map<String, Object> map) throws LaiKeAPIException;


    /**
     * 获取商品信息
     * left join lkt_order_details
     *
     * @param map -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/13 16:53
     */
    List<Map<String, Object>> getProductListJoinOrderDetailsDynamic(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 搜索量+1
     *
     * @param productListModel -
     * @return int
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/10/16 9:28
     */
    @Update("update lkt_product_list set search_num = search_num+1 where id = #{id}")
    int updateAddSearchNum(ProductListModel productListModel) throws LaiKeAPIException;

    //上架时间置空
    @Update("update lkt_product_list set upper_shelf_time = null where id = #{goodsId}")
    int setUpperTimeNull(int goodsId) throws LaiKeAPIException;

    /**
     * 获取商品最低价格和最高价格
     *
     * @param productListModel -
     * @return BigDecimal
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/10/30 12:39
     */
    @Select("select min(b.price) minPrice,max(b.price) maxPrice,min(b.yprice) minYprice,max(b.yprice) maxYprice, min(b.costprice) AS costprice from lkt_product_list a " +
            "left join lkt_configure b ON a.id = b.pid " +
            "where  a.store_id = #{store_id} " +
            "AND a.id = #{id} " +
            "AND b.recycle = 0")
    Map<String, BigDecimal> getGoodsPriceMinAndMax(ProductListModel productListModel) throws LaiKeAPIException;


    @Select("select min(p.live_price) minPrice,max(p.live_price) maxPrice,min(b.yprice) minYprice,max(b.yprice) maxYprice, min(b.costprice) AS costprice from lkt_product_list a " +
            "left join lkt_configure b ON a.id = b.pid " +
            "left join lkt_living_product p on p.pro_id = a.id " +
            "where a.store_id = #{store_id} " +
            "AND a.id = #{id} and p.living_id=#{roomId} " +
            "AND b.recycle = 0 and ifnull(b.commission,'') != '' ")
    Map<String, BigDecimal> getLiveGoodsPriceMinAndMax(Integer store_id, Integer id, Integer roomId) throws LaiKeAPIException;

    /**
     * 评论数量+1
     *
     * @param goodsId -
     * @return int
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/10/28 14:57
     */
    @Update("update lkt_product_list set comment_num = comment_num+1 where id = #{goodsId}")
    int updateAddCommentNum(int goodsId) throws LaiKeAPIException;


    /**
     * 统计店铺总销量
     *
     * @param map -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/10/16 11:29
     */
    int countProductListVolume(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 统计店铺 销售总数、在售总数
     *
     * @param map -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/10/16 11:29
     */
    Map<String, Object> countProductListByTotal(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 获取商品信息
     * LEFT JOIN lkt_configure
     *
     * @param map -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/10/29 10:27
     */
    List<Map<String, Object>> getProductListJoinConfigureDynamic(Map<String, Object> map) throws LaiKeAPIException;

    int countProductListJoinConfigureDynamic(Map<String, Object> map) throws LaiKeAPIException;


    /**
     * 获取运费信息
     *
     * @param map -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/10/29 15:41
     */
    List<Map<String, Object>> getProductListJoinFreightDynamic(Map<String, Object> map) throws LaiKeAPIException;


    /**
     * 获取商品单表动态sql
     *
     * @param map -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/10/30 11:43
     */
    List<Map<String, Object>> getGoodsInfoDynamic(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 获取商品多表动态sql
     *
     * @param map -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/10/30 11:43
     */
    List<Map<String, Object>> getSpecifiedGoodsInfoDynamic(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 获取商品多表动态sql
     *
     * @param map -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/10/30 11:43
     */
    int countSpecifiedGoodsInfoDynamic(Map<String, Object> map) throws LaiKeAPIException;


    /**
     * 订单确认页面获取商品信息
     *
     * @param map -
     * @return List
     * @throws LaiKeAPIException -
     * @author wangxian
     * @date 2020/11/11 10:17
     */
    @Select("select m.product_title,m.receiving_form,m.volume,c.price,c.unit,c.attribute,c.img,c.yprice,m.freight,m.product_class,m.brand_id,m.weight,m.mch_id,m.is_distribution,m.gongyingshang,m.is_appointment,m.write_off_settings " +
            "from lkt_product_list AS m LEFT JOIN lkt_configure AS c ON m.id = c.pid  " +
            "where m.store_id = #{store_id} and c.num > 0 and c.num >= #{num} and m.status = 2 and m.id = #{pid} and c.id = #{cid}")
    Map<String, Object> settlementProductsInfo(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 直播商品订单确认页面获取商品信息
     *
     * @param map
     * @return
     * @throws LaiKeAPIException
     */
    @Select("select m.product_title,m.receiving_form,m.volume,a.live_price as price,c.unit,c.attribute,c.img,c.yprice,m.freight,m.product_class,m.brand_id,m.weight,m.mch_id,m.is_distribution,m.gongyingshang,m.is_appointment,m.write_off_settings " +
            "from lkt_living_product a left join lkt_product_list AS m on m.id=a.pro_id LEFT JOIN lkt_configure AS c ON c.id = a.config_id " +
            "where m.store_id = #{store_id} and a.recycle=0 and a.num > 0 and (a.num-a.xl_num) >= #{num} and m.id = #{pid} and c.id = #{cid} and a.living_id=#{roomId}")
    Map<String, Object> settlementLiveProductsInfo(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 减少商品总库存
     *
     * @param pid -
     * @param num -
     * @return int
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/19 16:47
     */
    @Update("update lkt_product_list set num = num - #{num} where id = #{pid}")
    int reduceGoodsStockNum(int pid, int num) throws LaiKeAPIException;


    /**
     * 更新商品销量
     * plugin - update lkt_product_list set volume = volume + #{num} where store_id = #{storeId} and id = #{pid}
     *
     * @param num     -
     * @param storeId -
     * @param pid     -
     * @return int
     * @throws LaiKeAPIException -
     */
    @Update("update lkt_product_list set real_volume = real_volume + #{num} where store_id = #{storeId} and id = #{pid}")
    int updateProductListVolume(@Param("num") int num, @Param("storeId") int storeId, @Param("pid") int pid) throws LaiKeAPIException;

    /**
     * 获取状态为上架的商品属性库存
     *
     * @param storeId
     * @param pid
     * @param sid
     * @return
     */
    @Select("select ifnull(b.num,0) from lkt_product_list as a " +
            "left join lkt_configure as b on a.id = b.pid where a.store_id = #{storeId} " +
            "and a.status = 2 and a.mch_status = 2 and b.pid = #{pid} and b.id = #{sid} " +
            "and a.recycle = 0")
    Integer getProductNum(int storeId, int pid, int sid);


    /**
     * 获取商城商品最新顺序号
     *
     * @param storeId -
     * @return int
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/19 13:50
     */
    @Select("select  COALESCE(MAX(sort),0) + 1 as sort from lkt_product_list where store_id = #{storeId} ")
    int getGoodsMaxSort(int storeId) throws LaiKeAPIException;

    /**
     * 获取商品最新店铺顺序号
     *
     * @param storeId -
     * @return int
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/19 13:50
     */
    @Select("select COALESCE(MAX(mch_sort),0) + 1  as mch_sort from lkt_product_list where store_id = #{storeId} ")
    int getGoodsMaxMchSort(int storeId) throws LaiKeAPIException;


    /**
     * 获取商城【店铺】商品最新顺序号
     *
     * @param storeId -
     * @param mchId   -
     * @return int
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/19 13:50
     */
    @Select("select COALESCE(MAX(sort),0) + 1 as sort from lkt_product_list where store_id = #{storeId} and mch_id=#{mchId} ")
    int getGoodsMaxSortByMch(@Param("storeId") int storeId, @Param("mchId") int mchId) throws LaiKeAPIException;

    /**
     * 查询商品序号
     *
     * @param storeId -
     * @param id      -
     * @return int
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/29 18:07
     */
    @Select("select sort from lkt_product_list where store_id = #{storeId} and id = #{id}")
    int getGoodsSort(int storeId, int id) throws LaiKeAPIException;


    /**
     * 获取特惠商品
     *
     * @param storeId   -
     * @param pageModel -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/24 10:27
     */
    /*@Select("select a.id,a.product_title,a.subtitle,a.imgurl,a.volume,min(c.price) as price,c.yprice from lkt_product_list AS a RIGHT JOIN lkt_configure AS c ON a.id = c.pid " +
            "where a.store_id = #{storeId} and a.status = 2 and a.mch_status = 2 and a.recycle = 0 and a.active = 6  " +
            "group by c.pid  order by a.sort desc,a.add_date DESC limit #{pageModel.pageNo},#{pageModel.pageSize}")*/
    List<Map<String, Object>> getGoodsTeHuiInfo(@Param("storeId") int storeId, @Param("pageModel") PageModel pageModel) throws LaiKeAPIException;


    /**
     * 获取赠送商品
     *
     * @param storeId -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/8 17:29
     */
    @Select(" select b.id,b.product_title from lkt_configure as a left join lkt_product_list as b on a.pid = b.id " +
            " left join lkt_mch as c on b.mch_id = c.id  where b.store_id = #{storeId} and b.recycle=0 " +
            " and c.store_id = b.store_id and b.active = 1 and b.status = 2  and a.num > 0  group by b.id ")
    List<Map<String, Object>> getGiveGoodsList(int storeId) throws LaiKeAPIException;


    /**
     * 获取销售前10的商品信息
     *
     * @param storeId -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/12 18:08
     */
    @Select("select id,product_title,volume from lkt_product_list where store_id = #{storeId} AND recycle = 0 AND mch_status = 2 AND is_presell = 0 order by volume desc limit 10")
    List<Map<String, Object>> countGoodsTop10(int storeId) throws LaiKeAPIException;


    /**
     * 获取商品详细信息
     *
     * @param storeId -
     * @param goodsId -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/26 17:51
     */
    @Select("SELECT c.pname AS className, b.name AS freightName, d.brand_name AS brandName, a.* FROM lkt_product_list a " +
            "LEFT JOIN lkt_freight b ON b.id = a.freight " +
            " LEFT JOIN lkt_product_class c ON c.cid = substring_index(substring_index(a.product_class, '-', 2), '-', -1) " +
            " LEFT JOIN lkt_brand_class d ON d.brand_id = a.brand_id " +
            "where a.store_id=#{storeId} " +
            "and a.id=#{goodsId}")
    List<Map<String, Object>> selectGoodsDetailInfo(int storeId, int goodsId) throws LaiKeAPIException;


    /**
     * 获取拼团商品详细信息
     *
     * @param storeId -
     * @param goodsId -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/3/30 18:53
     */
    @Select("SELECT b.name ,b.freight ,b.default_freight,a.mch_id,b.supplier_id FROM lkt_product_list a " +
            "LEFT JOIN lkt_freight b ON b.id = a.freight " +
            "where a.store_id=#{storeId} " +
            "and a.id=#{goodsId}")
    Map<String, Object> selectGoodsFreightInfo(int storeId, int goodsId) throws LaiKeAPIException;

    //是否是自选商品
    @Select("SELECT count(1) FROM lkt_product_list a INNER JOIN lkt_customer b ON b.id = #{storeId} INNER JOIN lkt_admin c ON c.id = b.admin_id AND a.mch_id = c.shop_id WHERE a.store_id = #{storeId} and a.id=#{goodsId} AND a.recycle = 0 AND a.mch_status = 2 ")
    int isZxGoods(int storeId, int goodsId) throws LaiKeAPIException;

    //商品是否可以购买
    @Select("select count(1) from lkt_product_list a where a.id=#{goodsId} and a.status=#{status} and a.recycle=0")
    int isBuyGoods(@Param("goodsId") int goodsId, @Param("status") int status);

    //相似商品pc商城
//    @Select("SELECT a.id,a.product_title,a.imgurl,a.subtitle,a.status,a.volume,a.cover_map,c.price,c.yprice " +
//            "FROM lkt_product_list AS a RIGHT JOIN lkt_configure AS c ON a.id = c.pid " +
//            "WHERE a.store_id = #{storeId} AND a.id != #{pId} AND a.is_presell = 0 AND a.`status` = 2 " +
//            "AND a.product_class LIKE CONCAT('%-',#{cid},'-%') and a.mch_status = 2 and a.recycle = 0 and a.mch_id != 0  " +
//            "ORDER BY c.price asc ,c.yprice asc LIMIT 0,1")
//    List<Map<String, Object>> similarityPro(int storeId, String cid, Integer pId) throws LaiKeAPIException;

    @Select("SELECT a.id,a.product_title,a.imgurl,a.subtitle,a.status,a.volume,a.cover_map,c.price,c.yprice " +
            "FROM lkt_product_list AS a RIGHT JOIN lkt_configure AS c ON a.id = c.pid " +
            "WHERE a.store_id = #{storeId} AND a.id != #{pId} AND a.is_presell = 0 AND a.`status` = 2 " +
            "AND a.product_class LIKE CONCAT('%-',#{cid},'-%') and a.lang_code =  #{langCode} and a.mch_status = 2 and a.recycle = 0 and a.mch_id != 0  " +
            "ORDER BY c.price asc ,c.yprice asc LIMIT 0,1")
    List<Map<String, Object>> similarityPro(int storeId, String cid, Integer pId, String langCode) throws LaiKeAPIException;

    //是否属于插件商品
    @Select("select count(1) from lkt_product_list where id=#{goodsId} and is_presell=0")
    int isPluginGoods(int goodsId);

    //商品是否存在
    @Select("select count(1) from lkt_product_list a,lkt_configure b where b.id=#{attrId} and a.id=b.pid and a.id=b.pid and a.recycle=0 and a.recycle=b.recycle")
    int isGoodsExits(int attrId);

    //根据之前的分类,修改商品成新的分类、品牌
    @Update("update lkt_product_list a set a.product_class=concat('-',#{newClassId},'-'),a.brand_id=#{newBrandClassId} where product_class like concat('%-',#{oldClassId},'-%')")
    int updateGoodsClass(String oldClassId, String newClassId, Integer newBrandClassId);

    //断供同步供应商商品状态
    @Update("update lkt_product_list set `status` = #{status},`supplier_status` = #{supplierGoodsStatus},outage_time = NOW() WHERE gongyingshang = #{supplierId} AND supplier_superior = #{proId}")
    int operationStatus(Integer proId, String supplierId, Integer status, Integer supplierGoodsStatus) throws LaiKeAPIException;

    //违规下架同步供应商商品状态
    @Update("update lkt_product_list set recycle = 1 WHERE gongyingshang = #{supplierId} AND supplier_superior = #{proId}")
    int violationStatus(Integer proId, String supplierId) throws LaiKeAPIException;

    //统计代售可选商品
    int countConsignmentPro(Map<String, Object> map) throws LaiKeAPIException;

    //获取代售可选商品
    List<Map<String, Object>> getConsignmentPro(Map<String, Object> map) throws LaiKeAPIException;

    //删除所有当前指定供应商商品
    @Update("update lkt_product_list set recycle = 1 WHERE store_id = #{storeId} AND gongyingshang = #{supplierId}")
    int delAllSupplierPro(int storeId, String supplierId) throws LaiKeAPIException;

    //统计在售商品数量
    Integer getOnSaledAmount(Integer storeMchId, Integer storeid);

    //统计商品数量---根据店铺、状态
    Map<String, Object> getGoodsAmount1(Integer storeMchId, Integer storeid, Integer status, Boolean flag);

    Integer getGoodsAmount(Integer storeMchId, Integer storeid, Integer status, Boolean flag);

    //统计下架商品数量
    Integer getUnderSaledAmount(Integer storeMchId, Integer storeid);

    //统计所有商品数量
    Integer getallGoodsNum(Integer storeMchId, Integer storeid);

    //预警商品数量
    @Select("select  count(1) as num  from  lkt_product_list  where  num<=min_inventory")
    Integer getWarnedAmount(Integer storeid);

    //统计销售量前10商品
    @Select("select product_title name, volume + real_volume value from  lkt_product_list  " +
            "where  store_id = #{storeid} and recycle = 0 order by volume + real_volume desc limit 0,10")
    List<Map<String, Object>> getTopGoods(int storeid);

    //销量统计
    @Select("select   ifnull(sum(volume + real_volume),0) from   lkt_product_list  where  store_id=#{storeid} and  recycle=0")
    Integer getAllVolume(Integer storeid);

    /**
     * 供应商交易金额
     *
     * @param storeid
     * @param date
     * @return
     */
    List<Map<String, Object>> getTopSupplier(int storeid, String date);


    Map<String, Object> getSupplierGoodsAmount(Integer storeid, Integer status, List<String> statusList);

    //下架店铺所有仅自提的商品
    @Update("update lkt_product_list set status = 3 WHERE store_id = #{storeId} AND mch_id = #{mchId} AND receiving_form = '2'")
    int undercarriage(int storeId, Integer mchId) throws LaiKeAPIException;

    //更新店铺所有支持两种配送方式的商品为仅配送
    @Update("update lkt_product_list set receiving_form = '1' WHERE store_id = #{storeId} AND mch_id = #{mchId} AND receiving_form = '1,2'")
    int onlyShipping(int storeId, Integer mchId) throws LaiKeAPIException;

    //获取最新的上新商品时间
    @Select("select add_date from lkt_product_list a where store_id=#{storeId} and status=#{status} and a.mch_id=#{mchId} order by add_date desc limit 1")
    Date isUploadNewDate(int storeId, int mchId, int status) throws LaiKeAPIException;

    /**
     * 商品类型对应的商品数量
     *
     * @param storeid
     * @param mchId
     * @param flag
     * @return
     */
    List<Map<String, Object>> goodsSkuList(int storeid, Integer mchId, Boolean flag);

    /**
     * 经销商
     *
     * @param storeid
     * @return
     */
    List<Map<String, Object>> supplierGoodsSkuList(int storeid);

    /***
     * 统计新增商品数量
     * @param param
     * @return
     */
    Long getUpperGoodsNum(Map<String, Object> param);

    Long getUnderShelfGoodsNum(Map<String, Object> param);

    /**
     * 更具商品id查询对应店铺名字
     *
     * @param pId
     * @return
     */
    @Select("SELECT b.name FROM lkt_product_list a LEFT JOIN lkt_mch b on a.mch_id = b.id WHERE a.id = #{pId}")
    String getMctNameByPid(int pId);

    /**
     * 查询标签绑定的商品
     *
     * @param labelId
     * @return
     */
    @Select("SELECT id FROM lkt_product_list WHERE s_type LIKE CONCAT('%,',#{labelId},',%') ")
    List<Integer> getProByLabel(int labelId);

    /**
     * 查询
     *
     * @param storeId
     * @param classId
     * @return
     */
    @Select("SELECT id FROM lkt_product_list WHERE s_type LIKE CONCAT('%,',#{labelId},',%') ")
    int getClassPro(int storeId, int classId);

    /**
     * 获取分销端为你推荐商品列表，普通商品 + 分销商品
     *
     * @param map -
     * @return List<Map < String, Object>>
     * @throws LaiKeAPIException -
     */
    List<Map<String, Object>> getProductAndDistributionProductList(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 获取分销端为你推荐商品列表，普通商品 + 分销商品
     *
     * @param map -
     * @return List<Map < String, Object>>
     * @throws LaiKeAPIException -
     */
    int countProductAndDistributionProductList(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 查询商品
     *
     * @param id
     * @return
     */
    @Select("SELECT * FROM lkt_product_list WHERE id = #{id}")
    ProductListModel selectProduct(int id);

    /**
     * 查询商品
     *
     * @param id
     * @return
     */
    @Select("SELECT * FROM lkt_product_list WHERE id = #{id} and store_id= #{storeId}")
    ProductListModel selectProductByIdAndStoreId(int id, int storeId);

    /**
     * 根据购物车id查询商品信息
     *
     * @param id
     * @return
     */
    @Select("select p.* from lkt_product_list p left join lkt_cart c on p.id=c.Goods_id where c.id = #{id}")
    ProductListModel selectByCartId(Integer id);

    /**
     * 通过id集合查询出实体集合
     *
     * @param pIds
     * @return
     */
    List<ProductListModel> selectByIds(@Param("pIds") List<Integer> pIds);

    /**
     * 删除商品表中商品对应的可核销门店
     *
     * @param map
     * @return
     */
    List<ProductListModel> selectForStore(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 获取商品上下架数量
     *
     * @param status
     * @return
     */
    @Select("select ifnull(count(1),0) as num from lkt_product_list where store_id=#{storeId} and mch_id=#{mchId} and status = #{status} and recycle=0 and mch_status in (2,4)")
    Integer getNumByMchIdAndStatus(Integer storeId, Integer mchId, Integer status) throws LaiKeAPIException;

    /**
     * 获取商品待审核数量
     *
     * @return
     */
    @Select("select ifnull(count(1),0) as num from lkt_product_list where store_id=#{storeId} and mch_id=#{mchId} and mch_status =1 and recycle=0")
    Integer getDshNumByMchId(Integer storeId, Integer mchId) throws LaiKeAPIException;

    /**
     * 获取商品的分类数量
     *
     * @param mchId
     * @param storeId
     * @return
     */
    @Select("select ifnull(count(1),0) from (select ifnull(count(1),0) as num from lkt_product_list where store_id=#{storeId} and mch_id=#{mchId} and recycle=0 group by product_class) a")
    Integer getClassNumByMchId(Integer storeId, Integer mchId) throws LaiKeAPIException;

    /**
     * 获取品牌数量
     *
     * @param mchId
     * @param storeId
     * @return
     */
    @Select("select ifnull(count(1),0) from (select ifnull(count(1),0) as num from lkt_product_list a left join lkt_brand_class b on a.brand_id = b.brand_id where a.store_id=#{storeId} and a.mch_id=#{mchId} and a.recycle=0 and b.recycle=0 group by a.brand_id) a")
    Integer getBrandNumByMchId(Integer storeId, Integer mchId);

    /**
     * 获取销售商品sku
     *
     * @param mchId
     * @param storeId
     * @return
     */
    //@Select("select ifnull(count(1),0) as num from lkt_configure c left join lkt_product_list a on a.id=c.pid where a.store_id=#{storeId} and a.mch_id=#{mchId} and a.mch_status =2 and a.recycle=0 and a.status=2")
    Integer getSaleSkuNumByMchId(@Param("storeId") Integer  storeId, @Param("mchId") Integer mchId) throws LaiKeAPIException;

    /**
     * 获取商品sku数量
     *
     * @param mchId
     * @param storeId
     * @return
     */
    Integer getSkuNumByMchId(Integer storeId, Integer mchId) throws LaiKeAPIException;

    /**
     * 统计库存预警数
     *
     * @param storeId
     * @param mchId
     * @return
     * @throws LaiKeAPIException
     */
    @Select("select ifnull(count(1),0) as num from lkt_product_list where store_id=#{storeId} and mch_id=#{mchId} and recycle=0 and num<=min_inventory")
    Integer getKcbzNumByMchId(Integer storeId, Integer mchId) throws LaiKeAPIException;

    /**
     * 统计秒杀商品数量
     * @param parmaMap
     * @return
     */
    int getSeckillCount(Map<String, Object> parmaMap);

    /**
     * 获取秒杀商品信息
     * @param parmaMap
     * @return
     */
    List<Map<String, Object>> getSeckillCountList(Map<String, Object> parmaMap);

    /**
     * 修改商品运费模板
     * @param id
     * @param freight
     */
    @Update("update lkt_product_list set freight = #{freight} where id = #{id}")
    void updateFreight(Integer id, String freight);


    /**
     * 品牌删除 下架商品、品牌字段置空
     *
     * @param id
     */
    @Update("UPDATE lkt_product_list SET brand_id = NULL , `status` = 3 WHERE id = #{id}")
    void updateBrandIdNullById(Integer id);

    /**
     * 分类被删除 下架商品、商品分类和品牌字段置空
     *
     * @param id
     */
    @Update("UPDATE lkt_product_list SET product_class = NULL , brand_id = NULL , `status` = 3 WHERE id = #{id}")
    void updateClassIdNullById(Integer id);


    /**
     * 获取商品详情列表
     * @param proIdList
     * @return
     */
    List<Map<String, Object>> details(@Param("proIdList") List<String> proIdList);

}
