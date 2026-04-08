package com.laiketui.plugins.api.presell;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.plugin.presell.PreSellGoodsVo;

import java.util.Map;

/**
 * 预售商品
 *
 * @author sunH_
 * @date 2022/01/04 10:58
 */
public interface PluginsPsAppGoodsService
{

    /**
     * 获取预售商品
     *
     * @param vo
     * @return
     * @throws LaiKeAPIException
     * @author sunH_
     */
    Map<String, Object> getPreGoods(PreSellGoodsVo vo) throws LaiKeAPIException;

}
