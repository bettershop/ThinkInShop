package com.laiketui.comps.task.services.plugin;

import com.laiketui.common.annotation.HandlerOrderType;
import com.laiketui.common.api.order.PublicTaskService;
import com.laiketui.common.mapper.*;
import com.laiketui.comps.api.task.CompsTaskService;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.DictionaryConst;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.core.utils.tool.DateUtil;
import com.laiketui.domain.distribution.DistributionRecordModel;
import com.laiketui.domain.order.OrderConfigModal;
import com.xxl.job.core.context.XxlJobHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 分销任务
 *
 * @author Trick
 * @date 2021/5/25 9:32
 */
@Service("disTask")
@HandlerOrderType(type = DictionaryConst.OrdersType.ORDERS_HEADER_FX + DictionaryConst.TaskType.TASK)
public class CompsTaskDistributionServiceImpl implements PublicTaskService
{
    @Autowired
    private CompsTaskService taskServer;

    @Autowired
    private OrderConfigModalMapper orderConfigModalMapper;

    @Autowired
    private ReturnOrderModelMapper returnOrderModelMapper;

    @Autowired
    private UserDistributionModelMapper userDistributionModelMapper;

    @Autowired
    private DistributionRecordModelMapper distributionRecordModelMapper;

    @Autowired
    private OrderModelMapper orderModelMapper;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void execute() throws LaiKeAPIException
    {
        List<Integer>                 storeIdList                 = null;
        OrderConfigModal              orderConfigModal            = null;
        List<Map<String, Object>>     orderList                   = null;
        DistributionRecordModel       distributionRecordModel     = null;
        List<DistributionRecordModel> distributionRecordModelList = null;
        try
        {
            int num = 0;
            storeIdList = taskServer.getStoreIdAll();
            XxlJobHelper.log("====================== 分销异步任务开始执行 ======================");
            for (int storeId : storeIdList)
            {
                //订单售后天数
                int saveDay = 7;
                //获取商城订单配置信息
                orderConfigModal = new OrderConfigModal();
                orderConfigModal.setStore_id(storeId);
                orderConfigModal = orderConfigModalMapper.selectOne(orderConfigModal);
                if (orderConfigModal != null && orderConfigModal.getDays() != null)
                {
                    saveDay = orderConfigModal.getOrder_after();
                }
                //订单售后日期
                Date invalidDate = DateUtil.getAddDate(new Date(), -saveDay);
                XxlJobHelper.log(">>>>>>>>>>>商城id【{}】 订单承诺天数{},失效时间{} <<<<<<<<", storeId, saveDay, DateUtil.dateFormate(invalidDate, GloabConst.TimePattern.YMDHMS));
                orderList = returnOrderModelMapper.selectReturnNotMoney(storeId, invalidDate);
                for (Map<String, Object> map : orderList)
                {
                    String orderno = map.get("sNo").toString();
                    XxlJobHelper.log("----------- 订单号{},开始发放佣金 -----------", orderno);
                    //获取订单已发布的佣金
                    distributionRecordModel = new DistributionRecordModel();
                    distributionRecordModel.setStore_id(storeId);
                    distributionRecordModel.setsNo(orderno);
                    distributionRecordModel.setType(1);
                    distributionRecordModel.setStatus(1);
                    distributionRecordModelList = distributionRecordModelMapper.select(distributionRecordModel);
                    for (DistributionRecordModel distributionRecord : distributionRecordModelList)
                    {
                        XxlJobHelper.log("----------- 发放一笔{}元,给用户:{} ----------", distributionRecord.getMoney(), distributionRecord.getUser_id());
                        //发放佣金
                        int row = userDistributionModelMapper.grantTxCommission(storeId, distributionRecord.getUser_id(), distributionRecord.getMoney());
                        if (row < 1)
                        {
                            XxlJobHelper.log("佣金发放失败!!!");
                            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YJFFSB, "佣金发放失败!!!");
                        }
                    }
                    //标记订单已结算
                    int row = orderModelMapper.disSettlement(storeId, orderno);
                    if (row < 1)
                    {
                        XxlJobHelper.log("佣金结算失败!!!");
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YJJSSB, "佣金结算失败!!!");
                    }
                    num++;
                    orderno = null;
                    XxlJobHelper.log("----------- 订单结算完成! -----------");
                }
            }
            XxlJobHelper.log("====================== 分销异步任务执行完毕,共结算了【{}】个订单 ======================", num);
        }
        catch (Exception e)
        {
            XxlJobHelper.log(e);
            XxlJobHelper.handleFail("分销任务 异常: " + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, e.getMessage());
        }
        finally
        {
            storeIdList = null;
            orderConfigModal = null;
            orderList = null;
            distributionRecordModel = null;
            distributionRecordModelList = null;
        }
    }
}

