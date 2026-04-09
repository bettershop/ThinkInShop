package com.laiketui.plugins.api.auction.app;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;

import java.util.Map;

/**
 * 竞拍-平台
 *
 * @author Trick
 * @date 2022/7/22 15:45
 */
public interface PluginAuctionAppPtService
{

    /**
     * 竞拍详情-店铺主页数据
     *
     * @param vo        -
     * @param specialId - 专场id
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2022/7/12 15:29
     */
    Map<String, Object> specialDetail(MainVo vo, String specialId) throws LaiKeAPIException;

    /**
     * 获取专场下所有商品信息
     *
     * @param vo        -
     * @param specialId -
     * @param sortType  - 价格排序类型 1=降序 2=升序
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022/7/22 15:48
     */
    Map<String, Object> specialGoodsList(MainVo vo, String specialId, int sortType) throws LaiKeAPIException;

}
