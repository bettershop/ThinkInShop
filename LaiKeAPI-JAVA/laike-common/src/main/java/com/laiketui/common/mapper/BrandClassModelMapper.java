package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.product.BrandClassModel;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;


/**
 * 商品品牌 sql
 *
 * @author Trick
 * @date 2020/10/16 14:44
 */
public interface BrandClassModelMapper extends BaseMapper<BrandClassModel>
{


    /**
     * 获取品牌动态
     *
     * @param map -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/12 14:44
     */
    List<Map<String, Object>> getBrandClassDynamic(Map<String, Object> map) throws LaiKeAPIException;


    /**
     * 获取品牌信息
     *
     * @param map -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/30 17:54
     */
    List<Map<String, Object>> getBrandClassInfo(Map<String, Object> map) throws LaiKeAPIException;

    int countBrandClassInfo(Map<String, Object> map);

    /**
     * 获取品牌最新序号
     *
     * @param storeId -
     * @return int
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/31 9:49
     */
    @Select("SELECT IFNULL(MAX(sort) + 1,0) FROM lkt_brand_class WHERE store_id = #{storeId}")
    int getBrandClassMaxSort(int storeId) throws LaiKeAPIException;

    /**
     * 获取商品品牌集
     *
     * @param map -
     * @return List
     * @author Trick
     * @date 2022/2/11 15:14
     */
    List<Map<String, Object>> getGoodsBrandList(Map<String, Object> map);


    //品牌是否可以删除 只看审核通过和待审核的是否绑定分类;被拒绝的需要重新编辑,可以不管;暂不审核的提交审核的审核需要校验
    @Select("select count(distinct b.id) from lkt_brand_class a,lkt_product_list b where a.store_id=#{storeId} and a.store_id=b.store_id and b.brand_id=a.brand_id and a.brand_id=#{brandId} " +
            "and b.recycle=0 and mch_status in(1,2)")
    int brandIsDel(int storeId, int brandId);

    //根据品牌id查询商品id 只看审核通过和待审核的是否绑定分类;被拒绝的需要重新编辑,可以不管;暂不审核的提交审核的审核需要校验
    @Select("select brand_id from lkt_brand_class where store_id=#{storeId} and recycle = #{recycle} and is_default = 1")
    String getBrand(int storeId, int recycle);

    //根据品牌id查询商品id 只看审核通过和待审核的是否绑定分类;被拒绝的需要重新编辑,可以不管;暂不审核的提交审核的审核需要校验
    @Select("select distinct b.id from lkt_brand_class a,lkt_product_list b where a.store_id=#{storeId} and a.store_id=b.store_id and b.brand_id=a.brand_id and a.brand_id=#{brandId} " +
            "and b.recycle=0")
    List<Map<String, Object>> getGoodsByBrand(int storeId, int brandId);

    //解除品牌与某个分类的关系-【调用此方法必须立即调用 removeClassBrandClean】
    @Update("update lkt_brand_class a set a.categories=REPLACE(categories,CONCAT(#{classId},','), '') where categories like CONCAT('%,',#{classId},',%') ")
    int removeClassBrand(Integer classId);

    //解除品牌与某个分类的关系-【调用此方法无需再调用 removeClassBrandClean】
    @Update("UPDATE lkt_brand_class a " +
            "SET a.categories = REPLACE( " +
            "  REPLACE( " +
            "    REPLACE(categories, CONCAT(',', #{classId}, ','), ','), " +
            "    CONCAT(',', #{classId}), ''), " +
            "  CONCAT(#{classId}, ','), '') " +
            "WHERE categories LIKE CONCAT('%,', #{classId}, ',%') " +
            "   OR categories LIKE CONCAT(#{classId}, ',%') " +
            "   OR categories LIKE CONCAT('%,', #{classId})")
    int unbindClassBrand(Integer classId);


    //清理垃圾数据
    @Update("update lkt_brand_class a set a.categories='' where a.categories = ',' ")
    int removeClassBrandClean();

    //品牌分类是空的则绑定到默认分类下面
    @Update("update lkt_brand_class a set a.categories=CONCAT(',',(select cid from lkt_product_class x where x.store_id=a.store_id and x.notset=1 and x.recycle=0),',') where a.categories='' and a.store_id=#{storeId} ")
    int bindDefaultClass(int storeId);

    //查询默认品牌id
    @Select("select a.brand_id from lkt_brand_class a where a.store_id=#{storeId} and a.is_default = #{storeId} limit 1")
    Integer getBrandIdDefault(int storeId);

    @Select("select count(1) from   lkt_brand_class where  store_id=#{storeid} and  recycle=0 ")
    Integer countAll(Integer storeid);

    /**
     * 获取品牌
     *
     * @param map -
     * @return List
     * @throws LaiKeAPIException -
     */
    List<Map<String, Object>> getBrandDynamic(Map<String, Object> map) throws LaiKeAPIException;

    @Select("SELECT LAST_INSERT_ID()")
    int getLastInsertId();
}