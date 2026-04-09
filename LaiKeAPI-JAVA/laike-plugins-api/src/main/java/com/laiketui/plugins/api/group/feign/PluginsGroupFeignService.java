package com.laiketui.plugins.api.group.feign;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;

import java.util.Map;

/**
 * 拼团远程调用
 * gp
 */
public interface PluginsGroupFeignService
{
    /**
     * 商品否是加入拼团活动商品
     *
     * @param vo
     * @param goodsIds
     * @return
     */
    public Map<String, Object> getExistenceOfGoods(MainVo vo, String goodsIds, Integer MchId) throws LaiKeAPIException;
}
