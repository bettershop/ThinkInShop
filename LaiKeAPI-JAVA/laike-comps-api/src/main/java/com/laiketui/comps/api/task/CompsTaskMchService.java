package com.laiketui.comps.api.task;

import com.laiketui.core.exception.LaiKeAPIException;

/**
 * 店铺相关任务接口
 *
 * @author Trick
 * @date 2020/12/14 9:34
 */
public interface CompsTaskMchService
{

    /**
     * 提取码定时任务
     * 【php mch.up_extraction_code】
     *
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/14 9:35
     */
    void extractionCodeTask() throws LaiKeAPIException;


    /**
     * 修改买家能否提醒发货按钮
     * 【php test.remind_deliver】
     *
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/15 14:21
     */
    void remindDeliver() throws LaiKeAPIException;


    /**
     * 自动好评
     * 【php test.auto_good_comment】
     *
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/16 10:52
     */
    void autoGoodComment() throws LaiKeAPIException;


    /**
     * 商家订单定时结算
     * 【php test.order_settlement】
     *
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/16 13:56
     */
    void orderSettlement() throws LaiKeAPIException;

    /**
     * 清除虚拟商品需要预约待支付订单
     *
     * @throws LaiKeAPIException
     */
    void cleanVIWrite() throws LaiKeAPIException;
}
