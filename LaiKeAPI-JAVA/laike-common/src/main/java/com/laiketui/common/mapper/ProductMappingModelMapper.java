package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.product.ProductMappingModel;

import java.util.Map;

/**
 * 商品映射表
 *
 * @author Trick
 * @date 2023/2/15 10:56
 */
public interface ProductMappingModelMapper extends BaseMapper<ProductMappingModel>
{

    /**
     * 批量删除商品
     *
     * @param map -
     * @return int
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2023-02-15 17:14:49
     */
    int batchDelById(Map<String, Object> map) throws LaiKeAPIException;


}