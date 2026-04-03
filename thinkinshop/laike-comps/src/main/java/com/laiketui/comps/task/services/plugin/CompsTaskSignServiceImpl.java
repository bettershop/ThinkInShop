package com.laiketui.comps.task.services.plugin;

import com.laiketui.common.mapper.SignConfigModelMapper;
import com.laiketui.comps.api.task.plugin.CompsTaskSignService;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.core.utils.tool.DateUtil;
import com.laiketui.domain.config.SignConfigModel;
import com.xxl.job.core.context.XxlJobHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * 签到插件异步任务
 *
 * @author Trick
 * @date 2021/4/8 11:20
 */
@Service
//@HandlerOrderType(type = DictionaryConst.OrdersType.ORDERS_HEADER_IN + DictionaryConst.TaskType.TASK)
public class CompsTaskSignServiceImpl implements CompsTaskSignService
{
    @Autowired
    private SignConfigModelMapper signConfigModelMapper;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void task(int storeId) throws LaiKeAPIException
    {
        SignConfigModel signConfigUpdate = null;
        SignConfigModel signConfigModel  = null;
        try
        {
            //获取签到结束时间
            signConfigModel = new SignConfigModel();
            signConfigModel.setStore_id(storeId);
            signConfigModel = signConfigModelMapper.selectOne(signConfigModel);
            if (signConfigModel != null)
            {
                //判断活动是否结束,结束则关闭活动
                if (DateUtil.dateCompare(DateUtil.dateFormateToDate(signConfigModel.getEndtime(), GloabConst.TimePattern.YMDHMS), new Date()))
                {
                    signConfigUpdate = new SignConfigModel();
                    signConfigUpdate.setId(signConfigModel.getId());
                    signConfigModel.setIs_status(0);
                    int row = signConfigModelMapper.updateByPrimaryKeySelective(signConfigModel);
                    XxlJobHelper.handleFail(String.format("%s 商城的签到活动已经结束 结束结果:%s", storeId, row > 0));
                }
                else
                {
                    XxlJobHelper.handleFail("活动未过期");
                }
            }
        }
        catch (Exception e)
        {
            XxlJobHelper.log(e);
            XxlJobHelper.handleFail("签到任务 异常: " + e.getMessage());
        }
        finally
        {
            signConfigUpdate = null;
            signConfigModel = null;
        }
    }
}

