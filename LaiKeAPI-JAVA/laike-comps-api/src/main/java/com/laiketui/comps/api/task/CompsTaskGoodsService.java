package com.laiketui.comps.api.task;

import com.laiketui.core.exception.LaiKeAPIException;

/**
 * 商品任务
 *
 * @author Trick
 * @date 2020/12/15 16:11
 */
public interface CompsTaskGoodsService
{


    /**
     * 刷新商品状态
     * 【php test.product_status】
     *
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/15 16:12
     */
    void goodsStatus() throws LaiKeAPIException;

    /**
     * 预售商品定时任务
     *
     * @throws LaiKeAPIException -
     * @author sunH_
     * @date 2022/01/19 15:10
     */
    void sellGoods() throws LaiKeAPIException;

}
