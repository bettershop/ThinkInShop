package com.laiketui.common.service.dubbo.plugin.seconds;

import com.laiketui.common.api.PubliceService;
import com.laiketui.common.api.plugin.seconds.PublicSecondsService;
import com.laiketui.common.mapper.CustomerModelMapper;
import com.laiketui.common.mapper.SecondsActivityModelMapper;
import com.laiketui.common.mapper.SecondsConfigModelMapper;
import com.laiketui.common.mapper.SecondsProModelMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.utils.tool.DateUtil;
import com.laiketui.domain.seckill.SecondsActivityModel;
import com.laiketui.domain.seckill.SecondsConfigModel;
import com.laiketui.domain.seckill.SecondsProModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 公共秒杀
 *
 * @author Trick
 * @date 2021/4/9 16:24
 */
@Service
public class PublicSecondsServiceImpl implements PublicSecondsService
{
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public Map<String, Object> settlement(int storeId, List<Map<String, Object>> goodsInfo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("秒杀结算 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "settlement");
        }
        return resultMap;
    }

    @Override
    public int getSecondsStatus(int storeId, int secProId, int stockNum, Date startDate, Date endDate, boolean isUpdate) throws LaiKeAPIException
    {
        int status = SecondsProModel.SecondsStatus.SECKILL_STATUS_NOT_START;
        try
        {
            SecondsActivityModel secondsActivityUpdate = new SecondsActivityModel();
            secondsActivityUpdate.setId(secProId);
            if (stockNum < 1)
            {
                status = SecondsProModel.SecondsStatus.SECKILL_STATUS_TOOT_ALL;
            }
            else if (DateUtil.dateCompare(new Date(), endDate))
            {
                status = SecondsProModel.SecondsStatus.SECKILL_STATUS_END;
            }
            else if (DateUtil.dateCompare(new Date(), startDate) && DateUtil.dateCompare(endDate, new Date()))
            {
                status = SecondsProModel.SecondsStatus.SECKILL_STATUS_START;
            }
            else
            {
                Integer mchId = secondsActivityModelMapper.getSecMchId(secProId);
                if (mchId == null)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SJYC, "数据异常");
                }
                //预告
                SecondsConfigModel secondsConfigModel = new SecondsConfigModel();
                secondsConfigModel.setStore_id(storeId);
                //587 此处只有平台可以设置秒杀预告时间，此处不加店铺筛选
                //secondsConfigModel.setMch_id(mchId);
                secondsConfigModel = secondsConfigModelMapper.selectOne(secondsConfigModel);
                if (secondsConfigModel != null)
                {
                    //是否打开预告
                    if (secondsConfigModel.getIs_herald() == 1)
                    {
                        //获取秒杀预警时间
                        Date heraldDate = DateUtil.getAddDateBySecond(new Date(), secondsConfigModel.getHeraldTime());
                        if (DateUtil.dateCompare(startDate, new Date()) && DateUtil.dateCompare(heraldDate, startDate))
                        {
                            status = SecondsProModel.SecondsStatus.SECKILL_STATUS_HERALD;
                        }
                    }
                }
            }
            secondsActivityUpdate.setStatus(status);
            if (isUpdate)
            {
                secondsActivityModelMapper.updateByPrimaryKeySelective(secondsActivityUpdate);
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取秒杀状态 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "settlement");
        }
        return status;
    }

    @Override
    public String getSecondsStatusName(int secProId, Date startDate, Date endDate) throws LaiKeAPIException
    {
        String statusName;
        try
        {
            SecondsActivityModel secondsActivityUpdate = new SecondsActivityModel();
            secondsActivityUpdate.setId(secProId);
            if (DateUtil.dateCompare(new Date(), endDate))
            {
                statusName = "已结束";
                secondsActivityUpdate.setStatus(SecondsProModel.SecondsStatus.SECKILL_STATUS_END);
            }
            else if (DateUtil.dateCompare(new Date(), startDate))
            {
                statusName = "进行中";
                secondsActivityUpdate.setStatus(SecondsProModel.SecondsStatus.SECKILL_STATUS_START);
            }
            else
            {
                statusName = "未开始";
                secondsActivityUpdate.setStatus(SecondsProModel.SecondsStatus.SECKILL_STATUS_NOT_START);
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取秒杀状态 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "settlement");
        }
        return statusName;
    }

    @Override
    public int getSecondsStatus(int storeId, Integer mchId, int secProId, int stockNum, Date startDate, Date endDate, boolean isUpdate) throws LaiKeAPIException
    {
        int status = SecondsProModel.SecondsStatus.SECKILL_STATUS_NOT_START;
        try
        {
            //获取商城自营店id
            Integer              zyMchId               = customerModelMapper.getStoreMchId(storeId);
            SecondsActivityModel secondsActivityUpdate = new SecondsActivityModel();
            secondsActivityUpdate.setId(secProId);
            if (stockNum < 1)
            {
                status = SecondsProModel.SecondsStatus.SECKILL_STATUS_TOOT_ALL;
            }
            else if (DateUtil.dateCompare(new Date(), endDate))
            {
                status = SecondsProModel.SecondsStatus.SECKILL_STATUS_END;
            }
            else if (DateUtil.dateCompare(new Date(), startDate) && DateUtil.dateCompare(endDate, new Date()))
            {
                status = SecondsProModel.SecondsStatus.SECKILL_STATUS_START;
            }
            else
            {
                //预告
                SecondsConfigModel secondsConfigModel = new SecondsConfigModel();
                secondsConfigModel.setStore_id(storeId);
                secondsConfigModel.setMch_id(zyMchId);
                secondsConfigModel = secondsConfigModelMapper.selectOne(secondsConfigModel);
                if (secondsConfigModel != null)
                {
                    //是否打开预告
                    if (secondsConfigModel.getIs_herald() == 1)
                    {
                        //获取秒杀预警时间
                        Date heraldDate = DateUtil.getAddDateBySecond(new Date(), secondsConfigModel.getHeraldTime());
                        if (DateUtil.dateCompare(startDate, new Date()) && DateUtil.dateCompare(heraldDate, startDate))
                        {
                            status = SecondsProModel.SecondsStatus.SECKILL_STATUS_HERALD;
                        }
                    }
                }
            }
            secondsActivityUpdate.setStatus(status);
            if (isUpdate)
            {
                secondsActivityModelMapper.updateByPrimaryKeySelective(secondsActivityUpdate);
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取秒杀状态 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "settlement");
        }
        return status;
    }

    @Override
    public int getSecondsStatus(int storeId, Integer mchId, int secProId, int stockNum, Date startDate, Date endDate) throws LaiKeAPIException
    {
        try
        {
            return getSecondsStatus(storeId, mchId, secProId, stockNum, startDate, endDate, false);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取秒杀状态 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "settlement");
        }
    }

    @Autowired
    private SecondsProModelMapper secondsProModelMapper;

    @Autowired
    private SecondsActivityModelMapper secondsActivityModelMapper;

    @Autowired
    private SecondsConfigModelMapper secondsConfigModelMapper;

    @Autowired
    private PubliceService publiceService;

    @Autowired
    private CustomerModelMapper customerModelMapper;
}

