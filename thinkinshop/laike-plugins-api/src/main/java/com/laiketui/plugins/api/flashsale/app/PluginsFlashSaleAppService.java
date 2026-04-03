package com.laiketui.plugins.api.flashsale.app;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;

import java.util.Map;

public interface PluginsFlashSaleAppService
{

    //

    /**
     * 店铺限时折扣商品列表
     *
     * @param vo
     * @param mchId
     * @param key
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> appFlashSaleIndex(MainVo vo, Integer mchId, String key) throws LaiKeAPIException;

    /**
     * 获取活动说明
     *
     * @param vo
     * @param mchId
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> getFlashsaleRule(MainVo vo, Integer mchId) throws LaiKeAPIException;


    /**
     * 确认订单界面获取加购商品列表
     *
     * @param vo
     * @param mchId
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> getAddProList(MainVo vo, Integer mchId) throws LaiKeAPIException;
}
