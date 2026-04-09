package com.laiketui.plugins.api.seckill;

import com.laiketui.core.exception.LaiKeAPIException;

/**
 * 秒杀远程调用
 * gp
 * 2023-10-07
 */
public interface PluginsSecFeignService
{
    /**
     * 缓存秒杀信息
     * gp
     * 2023-10-07
     */
    void cache() throws LaiKeAPIException;

    /**
     * 秒杀标签修改,更新对应秒杀缓存信息
     *
     * @param labelId gp
     *                2023-10-07
     */
    void cacheByLabelId(String labelId) throws LaiKeAPIException;

    /**
     * 秒杀活动修改,更新对应秒杀缓存信息
     *
     * @param activityId
     * @throws LaiKeAPIException gp
     *                           2023-10-07
     */
    void cacheByActivityId(Integer activityId) throws LaiKeAPIException;

    /**
     * 商品规格删除，秒杀插件预扣库存同样进行回收规格的库存回收
     *
     * @param attrIds 商品规格id
     */
    void deleteByAttrIds(String attrIds);
}
