package com.laiketui.common.api.plugin;

import com.laiketui.core.exception.LaiKeAPIException;

import java.util.List;
import java.util.Map;

/**
 * 公共积分商城
 *
 * @author Trick
 * @date 2021/2/23 10:15
 */
public interface PubliceIntegralService
{


    /**
     * 获取积分商城商品信息
     *
     * @param storeId   -
     * @param pageStart -
     * @param pageEnd   -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/2/23 10:15
     */
    List<Map<String, Object>> getIntegralGoodsInfo(int storeId, int pageStart, int pageEnd) throws LaiKeAPIException;
}
