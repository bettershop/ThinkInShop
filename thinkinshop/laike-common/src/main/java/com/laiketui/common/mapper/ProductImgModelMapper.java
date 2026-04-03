package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.product.ProductImgModel;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 商品图片 sql
 *
 * @author Trick
 * @date 2020/11/16 11:17
 */
public interface ProductImgModelMapper extends BaseMapper<ProductImgModel>
{


    /**
     * 获取商品图片信息
     *
     * @param productImgModel -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/16 11:18
     */
    @Select("select * from lkt_product_img where product_id = #{product_id} order by id asc")
    List<Map<String, Object>> getProductImgInfoByPid(ProductImgModel productImgModel) throws LaiKeAPIException;

    @Select("select product_url from lkt_product_img where product_id = #{goodsId} order by id asc")
    List<String> getProductImgListById(int goodsId) throws LaiKeAPIException;

    //获取商品展示图 第一张
    @Select("select product_url from lkt_product_img where product_id = #{goodsId} order by id asc limit 1")
    String getProductImg(int goodsId) throws LaiKeAPIException;

    //根据商品获取商品轮播图
    @Select("select product_url from lkt_product_img where product_id=#{goodsId} order by id asc")
    List<String> getBannerImgByGoodsId(Integer goodsId);

}