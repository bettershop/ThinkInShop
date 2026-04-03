package com.laiketui.comps.task.controller;

import com.laiketui.common.api.order.PublicTaskService;
import com.laiketui.common.process.HandlerOrderTaskContext;
import com.laiketui.comps.api.task.*;
import com.laiketui.comps.api.task.plugin.CompsTaskFlashsaleService;
import com.laiketui.comps.api.task.plugin.CompsTaskSignService;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.DictionaryConst;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 异步任务api
 *
 * @author Trick
 * @date 2020/12/11 11:17
 */
@Component
public class CompsTaskController
{

    @Autowired
    CompsTaskCouponService compsTaskCouponService;

    @Autowired
    CompsTaskMchService compsTaskMchService;

    @Autowired
    CompsTaskOrderService compsTaskOrderService;

    @Autowired
    CompsTaskService compsTaskService;

    @Autowired
    CompsTaskGoodsService compsTaskGoodsService;

    @Autowired
    HandlerOrderTaskContext handlerOrderTaskContext;

    @Autowired
    private CompsTaskUserService compsTaskUserService;

    @Autowired
    private CompsTaskApiCheckService compsTaskApiCheckService;

    @Autowired
    private CompsTaskFlashsaleService compsTaskFlashsaleService;

    @Autowired
    private CompsTaskWxService compsTaskWxService;

    @XxlJob(value = "youtask")
    public void youtask()
    {
        //定时缓存数据
        System.out.println("hello xxl");
    }

    @XxlJob(value = "storeTask")
    public void storeTask()
    {
        //定时缓存 有效的商城id
        compsTaskService.taskStoreAll();
    }


    @XxlJob(value = "videoTask")
    public void videoTask() throws LaiKeAPIException
    {
        //定时缓存 有效的商城id
        compsTaskService.videoTask();
    }

    @XxlJob(value = "couponTask")
    public void couponTask()
    {
        //优惠卷定时任务
        compsTaskCouponService.couponTimingTask();
    }

    @XxlJob(value = "autoPushCoupon")
    public void autoPushCoupon()
    {
        //优惠卷定自动发放任务
        compsTaskCouponService.autoPushCoupon();
    }

    @XxlJob(value = "extractionCodeTask")
    public void extractionCodeTask()
    {
        //提取码定时任务
        compsTaskMchService.extractionCodeTask();
    }


    @XxlJob(value = "orderFailureTask")
    public void orderFailure()
    {
        //删除失效订单定时任务
        compsTaskOrderService.orderFailure();
    }

    @XxlJob(value = "thirdPayOrderReturn")
    public void thirdPayOrderReturn()
    {
        //第三方支付订单退款
        compsTaskOrderService.thirdPayOrderReturn();
    }

    @XxlJob(value = "cancellationShop")
    public void cancellationShop()
    {
        //定时清理店铺
        compsTaskService.cancellationShop();
    }

    @XxlJob(value = "ptOrderTask")
    public void dotask()
    {
        //拼团订单定时任务
        compsTaskOrderService.ptOrderTask();
    }

    @XxlJob(value = "AdditionUserData")
    public void AdditionUserData()
    {
        //定时缓存平台新增用户信息（用户报表）
        compsTaskUserService.AdditionUserData();
    }

    @XxlJob(value = "codeImgClear")
    public void codeImgClear()
    {
        //定时删除5分钟没有使用的验证码图片
        //目前无法删除了没有写到lkt_files_recoder
        //codeImageTaskService.codeImgClear();
    }

    /**
     * 订单报表定时缓存信息
     */
    @XxlJob(value = "orderReportTask")
    public void saveOrderReport()
    {
        compsTaskOrderService.saveReportData();
    }

    @XxlJob(value = "taskMS")
    public void taskMs()
    {
        //秒杀定时任务
        PublicTaskService publicTaskService = handlerOrderTaskContext.getOrderService(DictionaryConst.OrdersType.ORDERS_HEADER_MS + DictionaryConst.TaskType.TASK);
        publicTaskService.execute();
    }

    @XxlJob(value = "taskMSStock")
    public void taskMsStock()
    {
        //秒杀回滚库存
        PublicTaskService publicTaskService = handlerOrderTaskContext.getOrderService(DictionaryConst.OrdersType.ORDERS_HEADER_MS + DictionaryConst.TaskType.TASK);
        publicTaskService.stock();
    }

    @XxlJob(value = "taskMSCache")
    public void taskMSCache()
    {
        //缓存秒杀活动信息 -->未使用
        PublicTaskService publicTaskService = handlerOrderTaskContext.getOrderService(DictionaryConst.OrdersType.ORDERS_HEADER_MS + DictionaryConst.TaskType.TASK);
        publicTaskService.Cache();
    }

    @XxlJob(value = "taskJP")
    public void taskJp()
    {
        XxlJobHelper.log("【竞拍任务】开始执行 - 任务ID: " + XxlJobHelper.getJobId());

        try
        {
            PublicTaskService publicTaskService = handlerOrderTaskContext.getOrderService(DictionaryConst.OrdersType.ORDERS_HEADER_JP + DictionaryConst.TaskType.TASK);
            publicTaskService.execute();

            XxlJobHelper.log("【竞拍任务】执行完成");

        } catch (Exception e)
        {
            XxlJobHelper.log("【竞拍任务】异常: " + e.getMessage());
            throw e;
        }
    }

    @XxlJob(value = "taskPT")
    public void taskPt()
    {
        //拼团定时任务 10分钟
        PublicTaskService publicTaskService = handlerOrderTaskContext.getOrderService(DictionaryConst.OrdersType.ORDERS_HEADER_PT + DictionaryConst.TaskType.TASK);
        publicTaskService.execute();
    }

    @XxlJob(value = "signTask")
    public void signTask()
    {
        //签到异步任务
        compsTaskService.signTask();
    }


    @XxlJob(value = "taskFX")
    public void taskFx()
    {
        //分销定时任务
        PublicTaskService publicTaskService = handlerOrderTaskContext.getOrderService(DictionaryConst.OrdersType.ORDERS_HEADER_FX + DictionaryConst.TaskType.TASK);
        publicTaskService.execute();
    }

    @XxlJob(value = "receivingGoods")
    public void receivingGoods()
    {
        //自动定时自动收货
        compsTaskOrderService.receivingGoods();
    }


    @XxlJob(value = "remindDeliver")
    public void remindDeliver()
    {
        //重置提醒发货
        compsTaskMchService.remindDeliver();
    }

    @XxlJob(value = "noticeInvalidIntegral")
    public void noticeInvalidIntegral()
    {
        //过期积分提醒
        compsTaskService.noticeInvalidIntegral();
    }


    @XxlJob(value = "goodsStatus")
    public void goodsStatus()
    {
        //刷新商品状态以及库存预警
        compsTaskGoodsService.goodsStatus();
    }

    @XxlJob(value = "sellGoods")
    public void sellGoods()
    {
        //预售商品及订单定时任务
        compsTaskGoodsService.sellGoods();
    }


    @XxlJob(value = "clearMessageDay")
    public void clearMessageDay()
    {
        //定时清空消息
        compsTaskService.clearMessageDay();
    }


    @XxlJob(value = "autoGoodComment")
    public void autoGoodComment()
    {
        //定时自动好评
        compsTaskMchService.autoGoodComment();
    }

    @XxlJob(value = "resettingPwdNum")
    public void resettingPwdNum()
    {
        //定时重置登录重试次数
        compsTaskService.resettingPwdNum();
    }


    @XxlJob(value = "orderSettlement")
    public void orderSettlement()
    {
        //商家定订单时orderSettlement
        compsTaskMchService.orderSettlement();
    }

    @XxlJob(value = "cleanVIWrite")
    public void cleanVIWrite()
    {
        //虚拟商品清空预约次数
        compsTaskMchService.cleanVIWrite();
    }


    @XxlJob(value = "settlementIntegral")
    public void settlementIntegral()
    {
        //定时结算积分
        compsTaskService.settlementIntegral();
    }

    @XxlJob(value = "memberExpirationProcessing")
    public void memberExpirationProcessing()
    {
        //会员过期处理
        compsTaskService.memberExpirationProcessing();
    }

    @XxlJob(value = "mchAutoExamine")
    public void mchAutoExamine()
    {
        //店铺自动审核成功
        compsTaskService.mchAutoExamine();
    }

    @XxlJob(value = "mchAutoLogOff")
    public void mchAutoLogOff()
    {
        //店铺自动注销
        compsTaskService.mchAutoLogOff();
    }

    @XxlJob(value = "distributionSettlement")
    public void distributionSettlement()
    {
        //分销订单确认收货佣金结算特殊处理(订单参数格式:FX23082415490904540,FX230904090924611042,...)
        compsTaskOrderService.distributionSettlement();
    }

    @XxlJob(value = "storeExpiration")
    public void storeExpiration()
    {
        //处理商城过期
        compsTaskService.storeExpiration();
    }


    @XxlJob(value = "checkApis")
    public void checkApis()
    {
        //监测apis是否可用 10分钟检查一次
        compsTaskApiCheckService.checkApis();
        //TODO 服务重启
    }

    @XxlJob(value = "flashsaleActivity")
    public void flashsaleActivity()
    {
        //限时折扣活动定时任务
        compsTaskFlashsaleService.activityTask();
    }

    @XxlJob(value = "V3QueryBatchTransferOrder")
    public void V3QueryBatchTransferOrder()
    {
        //商家转账到零钱查询批次转账单结果
        compsTaskWxService.V3QueryBatchTransferOrder();
    }

    @XxlJob(value = "distributionIncome")
    public void distributionIncome()
    {
        //分销系统收益统计报表
        compsTaskService.distributionIncome();
    }

    @XxlJob(value = "synchronizationWxDelivery")
    public void synchronizationWxDelivery()
    {
        //同步微信发货信息录入的物流公司信息
        compsTaskWxService.synchronizationWxDelivery();
    }

    @XxlJob(value = "synchronizationWxOrderStatus")
    public void synchronizationWxOrderStatus()
    {
        //同步微信发货订单状态
        compsTaskWxService.synchronizationWxOrderStatus();
    }

    @XxlJob(value = "statisticsMch")
    public void statisticsMch()
    {
        //定时汇总店铺数据
        compsTaskService.statisticsMch();
    }

    @XxlJob(value = "httpGetJob")
    public void httpGetJob()
    {
        /*http类型的定时任务*/
        compsTaskService.httpGetJob();
    }

    @XxlJob(value = "httpPostJob")
    public void httpPostJob()
    {
        /*http类型的定时任务*/
        compsTaskService.httpPostJob();
    }


    public static void main(String[] args) throws InterruptedException
    {

    }
}
