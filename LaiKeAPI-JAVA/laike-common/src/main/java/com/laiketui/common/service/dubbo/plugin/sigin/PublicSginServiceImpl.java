package com.laiketui.common.service.dubbo.plugin.sigin;

import com.alibaba.fastjson2.JSON;
import com.laiketui.common.api.plugin.sgin.PublicSginService;
import com.laiketui.common.mapper.SignConfigModelMapper;
import com.laiketui.common.mapper.SignRecordModelMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.DictionaryConst;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.core.utils.tool.DataUtils;
import com.laiketui.core.utils.tool.DateUtil;
import com.laiketui.core.utils.tool.StringUtils;
import com.laiketui.domain.config.SignConfigModel;
import com.laiketui.domain.log.SignRecordModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 公共签到
 *
 * @author Trick
 * @date 2021/4/7 14:16
 */
@Service
public class PublicSginServiceImpl implements PublicSginService
{
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private SignRecordModelMapper signRecordModelMapper;

    @Autowired
    private SignConfigModelMapper signConfigModelMapper;

    @Override
    public Map<String, Object> index(int storeId, String userId, int year, int month) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        //签到状态 0 签过了  1 未签
        int signStatus = 0;
        //连续签到天数
        int continueSginNum = 0;
        //限制签到次数
        int sginNum = 0;
        //当天签到次数
        int currentSignNum = 0;
        //是否允许重复签到
        int isAginSgin = 0;
        //当月签到日期集合
        List<String> sginTimeList = new ArrayList<>();
        //签到详情
        String sginText = "";
        try
        {
            //获取今天、昨天开始时间
            Date   teDayStartDate    = DateUtil.dateFormateToDate(new Date(), GloabConst.TimePattern.YMD);
            Date   yesteDayDate      = DateUtil.getAddDate(teDayStartDate, -1);
            String yesteDayStartDate = DateUtil.getStartOfDay(yesteDayDate);

            //获取签到插件配置信息
            SignConfigModel signConfigModel = new SignConfigModel();
            signConfigModel.setStore_id(storeId);
            signConfigModel = signConfigModelMapper.selectOne(signConfigModel);
            if (signConfigModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QDPZXXBCZ, "签到配置信息不存在");
            }
            //签到插件是否开启
            if (signConfigModel.getIs_status() == 0)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QDCJWQY, "签到插件未启用");
            }
            isAginSgin = signConfigModel.getIs_many_time();
            sginNum = signConfigModel.getScore_num();
            sginText = signConfigModel.getDetail();
            //活动开始时间、结束时间
            Date startDate = DateUtil.dateFormateToDate(signConfigModel.getStarttime(), GloabConst.TimePattern.YMD);
            Date endDate   = DateUtil.dateFormateToDate(signConfigModel.getEndtime(), GloabConst.TimePattern.YMD);

            //判断昨天是否已经签到过
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("type", 0);
            parmaMap.put("user_id", userId);
            parmaMap.put("store_id", storeId);
            parmaMap.put("sign_time_end", teDayStartDate);
            parmaMap.put("sign_time_start", yesteDayStartDate);
            int count = signRecordModelMapper.countDynamic(parmaMap);
            //只有昨天签到过,才算连续签到
            if (count > 0)
            {
                //计算签到活动天数
                long startTime   = Objects.requireNonNull(startDate).getTime() / 1000;
                long endTime     = Objects.requireNonNull(endDate).getTime() / 1000;
                int  activityDay = DateUtil.getBetweenDate(startTime, endTime);
                for (int i = 1; i <= activityDay; i++)
                {
                    //上一天的开始时间
                    Date yesDayDate = DateUtil.getAddDate(teDayStartDate, -i);
                    if (!DateUtil.dateCompare(startDate, yesDayDate) || !DateUtil.dateCompare(yesDayDate, endDate))
                    {
                        logger.debug("签到活动未开始/已结束 签到活动开始时间{},结束时间{}", startDate, endDate);
                        break;
                    }
                    //继续查询上一天是否签到过
                    count = signRecordModelMapper.countSiginNum(storeId, userId, yesDayDate, teDayStartDate);
                    if (count > 0)
                    {
                        continueSginNum++;
                        continue;
                    }
                    break;
                }
            }
            //判断活动是否结束
            if (!DateUtil.dateCompare(startDate, new Date()) && !DateUtil.dateCompare(new Date(), endDate))
            {
                //今天是否已签到
                parmaMap.put("sign_time_end", null);
                parmaMap.put("sign_time_start", teDayStartDate);
                parmaMap.put("sign_time_sort", DataUtils.Sort.DESC.toString());
                List<Map<String, Object>> sginList = signRecordModelMapper.selectDynamic(parmaMap);
                if (sginList != null && sginList.size() > 0)
                {
                    //签到次数加今天的
                    continueSginNum++;
                    //今天签到次数
                    count = currentSignNum = sginList.size();
                    //已签到,是否允许重复签到
                    if (isAginSgin == 1)
                    {
                        //是否小于重复签到次数
                        if (count < sginNum)
                        {
                            //获取最近签到时间
                            Date signDate = DateUtil.dateFormateToDate(sginList.get(0).get("sign_time").toString(), GloabConst.TimePattern.YMDHMS);
                            if (signDate == null)
                            {
                                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SJCW, "数据错误");
                            }
                            long time = DateUtil.dateConversion(DateUtil.getTime(), signDate.getTime() / 1000, DateUtil.TimeType.SECOND);
                            if (time > Integer.parseInt(signConfigModel.getReset()))
                            {
                                //可以继续重复签到
                                signStatus = 1;
                            }
                        }
                    }
                }
                else
                {
                    signStatus = 1;
                }
            }
            else
            {
                logger.debug("签到活动已结束,关闭活动");
                SignConfigModel signConfigUpdate = new SignConfigModel();
                signConfigUpdate.setId(signConfigModel.getId());
                signConfigUpdate.setIs_status(0);
                count = signConfigModelMapper.updateByPrimaryKeySelective(signConfigUpdate);
                logger.debug("签到活动已结束 插件关闭状态:{}", count > 0);
            }
            //获取当月开始时间/结束时间
            String monthStart = DateUtil.dateFormate(String.format("%s-%s-01 00:00:00", year, month), GloabConst.TimePattern.YMDHMS);
            String monthEnd   = DateUtil.getMonthLastDay(DateUtil.dateFormateToDate(monthStart, GloabConst.TimePattern.YMDHMS));
            //查询当月份签到的记录
            parmaMap.remove("sign_time_end");
            parmaMap.put("sign_time_end1", monthEnd);
            parmaMap.put("sign_time_start", teDayStartDate);
            parmaMap.put("sign_time_sort", DataUtils.Sort.DESC.toString());
            List<Map<String, Object>> sginList = signRecordModelMapper.selectDynamic(parmaMap);
            for (Map<String, Object> map : sginList)
            {
                String time = map.get("sign_time").toString();
                time = DateUtil.dateFormate(time, GloabConst.TimePattern.YMD4);
                sginTimeList.add(time);
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("签到数据加载 异常:" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QDSJJZSB, "签到数据加载失败", "sigin");
        }
        resultMap.put("is_many_time", isAginSgin);
        resultMap.put("sign_status", signStatus);
        resultMap.put("sign_time", sginTimeList);
        resultMap.put("num", continueSginNum);
        resultMap.put("currentSignNum", currentSignNum);
        resultMap.put("score_num", sginNum);
        resultMap.put("details", sginText);
        return resultMap;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteSign(int storeId, List<String> ids) throws LaiKeAPIException
    {
        try
        {
            if (StringUtils.isEmpty(ids))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.PARAMATER_ERROR, "参数错误");
            }
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", storeId);
            parmaMap.put("ids", ids);
            int count = signRecordModelMapper.countDynamic(parmaMap);
            if (count < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.PARAMATER_ERROR, "参数错误");
            }
            ids.forEach(id ->
            {
                int acId = Integer.parseInt(id);
                //删除过期积分
                SignRecordModel signRecordModel = new SignRecordModel();
                signRecordModel.setId(acId);
                signRecordModel.setRecovery(DictionaryConst.ProductRecycle.RECOVERY);
                int row = signRecordModelMapper.updateByPrimaryKeySelective(signRecordModel);
                if (row < 1)
                {
                    logger.info("删除过期积分 参数:{}", JSON.toJSONString(signRecordModel));
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLGZ, "网络故障", "delMchCoupon");
                }
            });
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("批量删除店铺活动 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "delBatchMchCoupon");
        }
        return true;
    }

}

