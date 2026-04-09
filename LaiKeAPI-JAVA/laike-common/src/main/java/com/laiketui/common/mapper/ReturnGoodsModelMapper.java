package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.mch.ReturnGoodsModel;

import java.util.List;
import java.util.Map;

/**
 * 退货商品
 *
 * @author Trick
 * @date 2021/12/20 14:02
 */
public interface ReturnGoodsModelMapper extends BaseMapper<ReturnGoodsModel>
{

    @Override
    List<Map<String, Object>> selectDynamic(Map<String, Object> map) throws LaiKeAPIException;

    @Override
    int countDynamic(Map<String, Object> map) throws LaiKeAPIException;
}