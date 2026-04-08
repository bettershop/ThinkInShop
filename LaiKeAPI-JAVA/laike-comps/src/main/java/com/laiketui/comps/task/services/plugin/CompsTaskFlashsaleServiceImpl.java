package com.laiketui.comps.task.services.plugin;

import com.laiketui.common.mapper.FlashsaleActivityModelMapper;
import com.laiketui.common.mapper.FlashsaleLabelModelMapper;
import com.laiketui.comps.api.task.CompsTaskService;
import com.laiketui.comps.api.task.plugin.CompsTaskFlashsaleService;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.core.utils.tool.DateUtil;
import com.xxl.job.core.context.XxlJobHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * 限时折扣任务
 *
 * @author Trick
 * @date 2021/5/25 9:32
 */
@Service("FlashsaleTask")
public class CompsTaskFlashsaleServiceImpl implements CompsTaskFlashsaleService
{

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private FlashsaleActivityModelMapper flashsaleActivityModelMapper;

    @Autowired
    private FlashsaleLabelModelMapper flashsaleLabelModelMapper;

    @Autowired
    private CompsTaskService taskServer;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void activityTask() throws LaiKeAPIException
    {
        List<Integer> storeIdList = null;
        try
        {
            XxlJobHelper.log("限时折扣活动定时任务 开始执行!");
            String nowTime = DateUtil.dateFormate(new Date(), GloabConst.TimePattern.YMDHMS);
            flashsaleActivityModelMapper.updateActivityStatus(nowTime);
            flashsaleActivityModelMapper.updateActivityStatus2(nowTime);
            flashsaleLabelModelMapper.updateActivityStatus(nowTime);
            flashsaleLabelModelMapper.updateActivityStatus2(nowTime);
        }
        catch (Exception e)
        {
            logger.error("限时折扣活动定时任务 ", e);
            XxlJobHelper.log("限时折扣活动定时任务: ", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC,e.getMessage());
        }
        finally
        {
            storeIdList = null;
        }
    }
}
