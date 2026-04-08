package com.laiketui.comps.api.task;

import com.laiketui.core.exception.LaiKeAPIException;

import java.util.List;

/**
 * 公共定时任务接口
 *
 * @author Trick
 * @date 2020/12/14 15:30
 */
public interface CompsTaskService
{

    /**
     * 缓存
     *
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/14 15:31
     */
    void taskStoreAll() throws LaiKeAPIException;


    /**
     * 获取缓存的storeId
     *
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/15 9:21
     */
    List<Integer> getStoreIdAll() throws LaiKeAPIException;


    /**
     * 清空消息
     * 【php test.message_day】
     *
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/15 16:57
     */
    void clearMessageDay() throws LaiKeAPIException;

    /**
     * 定时重置密码重试次数
     * 【php test.login_num_resetting】
     *
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/16 13:41
     */
    void resettingPwdNum() throws LaiKeAPIException;

    /**
     * 定时处理签到
     *
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/10/12 14:43
     */
    void signTask() throws LaiKeAPIException;


    /**
     * 定时结算积分
     *
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/11/10 19:12
     */
    void settlementIntegral() throws LaiKeAPIException;

    /**
     * 定时清理未审核店铺
     *
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022/1/7 11:20
     */
    void cancellationShop() throws LaiKeAPIException;

    /**
     * 会员过期处理
     *
     * @throws LaiKeAPIException
     */
    void memberExpirationProcessing() throws LaiKeAPIException;

    /**
     * 店铺自动审核成功
     *
     * @throws LaiKeAPIException
     */
    void mchAutoExamine() throws LaiKeAPIException;

    /**
     * 店铺自动注销
     *
     * @throws LaiKeAPIException
     */
    void mchAutoLogOff() throws LaiKeAPIException;

    /**
     * 处理商城过期
     *
     * @throws LaiKeAPIException
     */
    void storeExpiration() throws LaiKeAPIException;

    /**
     * 分销系统收益统计报表
     */
    void distributionIncome();

    /**
     * 统计店铺信息
     */
    void statisticsMch();

    /**
     * 过期积分提醒
     */
    void noticeInvalidIntegral();

    /**
     * url
     *
     * @throws LaiKeAPIException
     */
    void httpGetJob() throws LaiKeAPIException;

    void httpPostJob() throws LaiKeAPIException;

    /**
     * 腾讯云防盗链视频过期时间延长
     */
    void videoTask() throws LaiKeAPIException;
}
