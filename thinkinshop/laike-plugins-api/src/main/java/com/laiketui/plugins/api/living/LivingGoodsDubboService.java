package com.laiketui.plugins.api.living;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;

import java.util.Map;

/**
 * @author zhuqingyu
 * @create 2024/6/3
 */

public interface LivingGoodsDubboService
{


    /**
     * 根据id获取商品信息
     *
     * @param vo
     * @param goodsId
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> getGoodsInfoById(MainVo vo, int goodsId) throws LaiKeAPIException;

    /**
     * 查询商品池
     *
     * @param map
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> queryGoodsPool(Map<String, Object> map) throws LaiKeAPIException;
}
