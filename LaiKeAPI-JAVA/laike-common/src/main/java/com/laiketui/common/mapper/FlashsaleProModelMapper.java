package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.flashsale.FlashsaleProModel;

import java.util.List;
import java.util.Map;

public interface FlashsaleProModelMapper extends BaseMapper<FlashsaleProModel>
{
    /**
     * 获取限时折扣商品
     *
     * @param map
     * @return
     * @throws LaiKeAPIException
     */
    List<Map<String, Object>> getGoodsInfo(Map<String, Object> map) throws LaiKeAPIException;

    int countGoodsInfo(Map<String, Object> map) throws LaiKeAPIException;
}
