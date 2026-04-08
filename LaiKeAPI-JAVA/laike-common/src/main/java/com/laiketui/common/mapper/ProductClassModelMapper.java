package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.product.ProductClassModel;
import com.laiketui.domain.vo.MainVo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;


/**
 * 商品分类
 *
 * @author Trick
 * @date 2020/10/10 17:44
 */
public interface ProductClassModelMapper extends BaseMapper<ProductClassModel>
{


    @Override
    List<Map<String, Object>> selectDynamic(Map<String, Object> map) throws LaiKeAPIException;

    @Override
    int countDynamic(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 查询商品并分类显示返回JSON至小程序(一级分类)
     *
     * @param storeId-
     * @return List<ProductClassModel>
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/10/10 17:47
     */
    List<ProductClassModel> getProduct(@Param("storeId") int storeId, @Param("lang_code") String langCode) throws LaiKeAPIException;

    List<Map<String, Object>> getProductPc(@Param("vo") MainVo vo,@Param("mchId") Integer mchId) throws LaiKeAPIException;

    @Select("select cid,pname,english_name from lkt_product_class where store_id = #{storeId} and is_display=1 and recycle = 0 and examine = 1 and sid=#{sid} order by sort desc limit 0,10")
    List<Map<String, Object>> getProductBySid(int storeId, int sid) throws LaiKeAPIException;

    /**
     * 获取类目信息
     *
     * @param productClassModel -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/10/19 15:39
     */
    List<ProductClassModel> getProductClassLevel(ProductClassModel productClassModel) throws LaiKeAPIException;

    /**
     * 获取一级类目信息
     *
     * @param productClassModel -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/10/19 15:39
     */
    @Select("SELECT GROUP_CONCAT( cid SEPARATOR ',' ) classId FROM lkt_product_class where level = 0 and recycle = 0 and store_id = #{store_id}")
    String getProductClassLevelTop(int store_id) throws LaiKeAPIException;

    /**
     * 匹配商品类目名称
     *
     * @param map -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/10/15 14:41
     */
    @Select("select * from lkt_product_class where store_id = #{store_id} and recycle = 0 and pname LIKE CONCAT('%', #{pname},'%') order by sort desc")
    List<ProductClassModel> getGoodsClass(Map<String, Object> map) throws LaiKeAPIException;


    /**
     * 获取当前店铺所有的类别
     *
     * @param mchId -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/23 14:51
     */
    @Select("select distinct b.cid,b.pname from lkt_product_list a,lkt_product_class b where a.mch_id=#{mchId} " +
            " and substring_index(substring_index(a.product_class, '-', 2), '-', -1) =  b.cid and a.is_presell = 0 " +
            " and a.recycle=0 and b.recycle=0 and b.is_display = 1 ")
    List<ProductClassModel> getGoodsClassByMch(int mchId) throws LaiKeAPIException;

    @Select("select distinct b.cid,b.pname from lkt_product_list a,lkt_product_class b where a.mch_id=#{mchId} " +
            " and substring_index(substring_index(a.product_class, '-', 2), '-', -1) =  b.cid and a.is_presell = 0 " +
            " and a.recycle=0 and b.recycle=0 and b.is_display = 1 and b.lang_code = #{langCode} ")
    List<ProductClassModel> getGoodsClassByMch1(int mchId, String langCode) throws LaiKeAPIException;

    //获取商品类别名称
    @Select(" select b.pname from lkt_product_list a,lkt_product_class b where substring_index(substring_index(a.product_class, '-', 2), '-', -1)=b.cid " +
            " and a.id=#{goodsId} ")
    String getGoodsClassName(int goodsId);

    //获取商品类别名称 --最后一个分类
    @Select(" select b.pname from lkt_product_list a,lkt_product_class b where substring_index(substring_index(a.product_class, '-', -2), '-', 1)=b.cid " +
            " and a.id=#{goodsId} ")
    String getGoodsClassNameFinally(int goodsId);

    /**
     * 获取最新序号
     *
     * @param storeId -
     * @return int
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/30 15:46
     */
    @Select("select IFNULL(MAX(sort) + 1,0) from lkt_product_class where store_id=#{storeId}")
    int getGoodsClassMaxSort(int storeId) throws LaiKeAPIException;

    /**
     * 单表动态统计
     *
     * @param productClassModel -
     * @return int
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/30 15:38
     */
    int getGoodsClassCount(ProductClassModel productClassModel) throws LaiKeAPIException;


    @Update("update lkt_product_class a,lkt_product_class b set a.sort=b.sort,b.sort = a.sort where a.cid=#{id} and b.cid=#{id1} ")
    int move(int id, int id1);

    @Update("update lkt_product_class set recycle=1 where sid=#{cid} and recycle = 0")
    int delClassBySid(int cid);

    /**
     * 获取商品类目集
     *
     * @param map -
     * @return List
     * @author Trick
     * @date 2022/2/11 15:14
     */
    List<Map<String, Object>> getGoodsClassList(Map<String, Object> map);


    //分类是否可以删除 只看审核通过和待审核的是否绑定分类;被拒绝的需要重新编辑,可以不管;暂不审核的提交审核的审核需要校验
    @Select("select count(distinct b.id) from lkt_product_class a,lkt_product_list b where a.store_id=#{storeId} and a.store_id=b.store_id and b.product_class like concat('%-',a.cid,'-%') and a.cid=#{cid} " +
            "and b.recycle=0 and mch_status in(1,2)")
    int classIsDel(int storeId, int cid);

    //根据分类id查询商品id
    @Select("select distinct b.id from lkt_product_class a,lkt_product_list b where a.store_id=#{storeId} and a.store_id=b.store_id and b.product_class like concat('%-',a.cid,'-%') and a.cid=#{cid} " +
            "and b.recycle=0")
    List<Map<String, Object>> getGoodsByClass(int storeId, int cid);

    //查询默认分类id
    @Select("select distinct a.cid from lkt_product_class a where a.store_id=#{storeId} and a.is_default = 1")
    String getClassIdIsDefault(int storeId);

    //分类是否绑定了品牌
    @Select("select count(1) from lkt_brand_class where store_id=#{storeId} and categories like CONCAT(',',#{cid},',') and recycle=0")
    int classIsBindBrand(int storeId, int cid);

    //分类是否绑定了品牌的品牌id
    @Select("select distinct brand_id from lkt_brand_class where store_id=#{storeId} and categories like CONCAT(',',#{cid},',') and recycle=0")
    List<Map<String, Object>> getclassIsBindBrand(int storeId, int cid);

    //同步商品总库存
    @Select("update lkt_product_list a,(SELECT IFNULL(sum(b.num) ,0) num,a.id FROM lkt_product_list AS a INNER JOIN lkt_configure AS b " +
            "where a.id=#{goodsId} and a.id=b.pid and b.recycle=0 group by a.id) x set a.num =x.num where a.id=#{goodsId} and a.id=x.id")
    Integer synchroStock(int goodsId);

    @Select("SELECT LAST_INSERT_ID()")
    int getLastInsertId();

    int count(Map<String, Object> map);

}