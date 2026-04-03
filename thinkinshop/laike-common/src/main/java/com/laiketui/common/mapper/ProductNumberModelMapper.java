package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.product.ProductNumberModel;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;


/**
 * 商品编号
 *
 * @author Trick
 * @date 2020/11/18 20:30
 */
public interface ProductNumberModelMapper extends BaseMapper<ProductNumberModel>
{


    /**
     * 修改商品编号状态
     *
     * @param productNumberModel -
     * @return int
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/18 20:38
     */
    @Update("update lkt_product_number set status = #{status} where store_id = #{store_id} and mch_id = #{mch_id} and operation = #{operation} " +
            " and product_number = #{product_number} ")
    int updateGoodsNumberByStatus(ProductNumberModel productNumberModel) throws LaiKeAPIException;


    /**
     * 获取商品编号最后一条
     *
     * @param productNumberModel -
     * @return ProductNumberModel
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/18 20:45
     */
    @Select("select product_number from lkt_product_number where store_id = #{store_id} and mch_id = #{mch_id} and operation = #{operation} and status = 1 " +
            "order by addtime desc limit 1")
    ProductNumberModel getGoodsNumberLow(ProductNumberModel productNumberModel) throws LaiKeAPIException;

}