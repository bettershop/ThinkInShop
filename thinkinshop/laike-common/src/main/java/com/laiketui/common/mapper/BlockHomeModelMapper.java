package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.dictionary.BlockHomeModel;

import java.util.List;
import java.util.Map;

/**
 * 楼层管理
 *
 * @author Trick
 * @date 2023/2/15 10:52
 */
public interface BlockHomeModelMapper extends BaseMapper<BlockHomeModel>
{

    @Override
    List<Map<String, Object>> selectDynamic(Map<String, Object> map) throws LaiKeAPIException;

    @Override
    int countDynamic(Map<String, Object> map) throws LaiKeAPIException;


    List<Map<String, Object>> selectGoodsInfo(Map<String, Object> map) throws LaiKeAPIException;

    int countGoodsInfo(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 获取商品是否重复添加到楼层了
     *
     * @param map
     * @return
     * @throws LaiKeAPIException
     */
    int getRepeatCount(Map<String, Object> map) throws LaiKeAPIException;


    /**
     * 获取所有的供应商或者店铺
     */

    List<Map<String, Object>> getAllMch(Map<String, Object> map) throws LaiKeAPIException;


    List<Map<String, Object>> getAllSupplier(Map<String, Object> map) throws LaiKeAPIException;
}