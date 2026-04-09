package com.laiketui.admins.admin.services.plugin;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.laiketui.admins.api.admin.plugin.AdminSignService;
import com.laiketui.common.mapper.SignConfigModelMapper;
import com.laiketui.common.mapper.SignRecordModelMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.DictionaryConst;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.core.utils.tool.DataUtils;
import com.laiketui.core.utils.tool.DateUtil;
import com.laiketui.core.utils.tool.SerializePhpUtils;
import com.laiketui.core.utils.tool.StringUtils;
import com.laiketui.domain.config.SignConfigModel;
import com.laiketui.domain.log.SignRecordModel;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.plugin.sign.AddSignConfigVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 后台签到管理
 *
 * @author Trick
 * @date 2021/5/11 10:44
 */
@Service
public class AdminSignServiceImpl implements AdminSignService
{
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private SignRecordModelMapper signRecordModelMapper;

    @Autowired
    private SignConfigModelMapper signConfigModelMapper;

    @Override
    public Map<String, Object> index(MainVo vo, String userName, Integer userType) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("type", 0);
            parmaMap.put("userName", userName);
            parmaMap.put("store_id", vo.getStoreId());
            parmaMap.put("source", userType);
            parmaMap.put("sign_time1_sort", DataUtils.Sort.DESC.toString());
            parmaMap.put("pageStart", vo.getPageNo());
            parmaMap.put("pageEnd", vo.getPageNo());
            List<Map<String, Object>> sginList = signRecordModelMapper.selectUserDynamic(parmaMap);
            int                       total    = signRecordModelMapper.countUserDynamic(parmaMap);

            //获取签到配配置信息
            SignConfigModel signConfigModel = new SignConfigModel();
            signConfigModel.setStore_id(vo.getStoreId());
            signConfigModel = signConfigModelMapper.selectOne(signConfigModel);
            if (signConfigModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QPZQD, "请配置签到");
            }
            Date startDate = DateUtil.dateFormateToDate(signConfigModel.getStarttime(), GloabConst.TimePattern.YMD);
            Date endDate   = DateUtil.dateFormateToDate(signConfigModel.getEndtime(), GloabConst.TimePattern.YMD);
            if (startDate == null || endDate == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误");
            }
            //获取今天、昨天开始时间
            Date teDayStartDate = DateUtil.dateFormateToDate(new Date(), GloabConst.TimePattern.YMD);
            //连续签到天数
            int continueSginNum = 0;
            //活动天数
            int activityDay = DateUtil.getBetweenDate(endDate.getTime() / 1000, startDate.getTime() / 1000);

            for (Map<String, Object> map : sginList)
            {
                String userId = map.get("user_id").toString();
                if (map.containsKey("user_name"))
                {
                    map.put("user_name", map.get("user_name"));
                }
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
                    int count = signRecordModelMapper.countSiginNum(vo.getStoreId(), userId, yesDayDate, teDayStartDate);
                    if (count > 0)
                    {
                        continueSginNum++;
                        continue;
                    }
                    break;
                }
                map.put("num", continueSginNum);
                //获取签到积分
                int sumScore = signRecordModelMapper.sumSiginScore(vo.getStoreId(), userId);
                map.put("score", sumScore);
            }
            resultMap.put("list", sginList);
            resultMap.put("total", total);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("签到列表 异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "addGroupConfig");
        }
        return resultMap;
    }


    @Override
    public Map<String, Object> signDetail(MainVo vo, String userId, String startDate, String endDate) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("type", 0);
            parmaMap.put("user_id", userId);
            parmaMap.put("sign_time_start", startDate);
            parmaMap.put("sign_time_end1", endDate);
            parmaMap.put("store_id", vo.getStoreId());
            parmaMap.put("sign_time_sort", DataUtils.Sort.DESC.toString());
            parmaMap.put("pageStart", vo.getPageNo());
            parmaMap.put("pageEnd", vo.getPageNo());
            List<Map<String, Object>> sginList = signRecordModelMapper.selectUserDynamic1(parmaMap);
            for (Map<String, Object> map : sginList)
            {
                String time = map.get("sign_time").toString();
                time = DateUtil.dateFormate(time, GloabConst.TimePattern.YMDHMS);
                map.put("sign_time", time);
            }
            resultMap.put("list", sginList);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("签到明细 异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "signDetail");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> delSign(MainVo vo, String userId) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            SignRecordModel signRecordModel = new SignRecordModel();
            signRecordModel.setStore_id(vo.getStoreId());
            signRecordModel.setUser_id(userId);
            signRecordModel.setType(DictionaryConst.IntegralType.SIGN);
            int row = signRecordModelMapper.delete(signRecordModel);
            if (row < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CZSB, "操作失败");
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("删除签到记录 异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "delSign");
        }
        return resultMap;
    }


    @Override
    public Map<String, Object> signConfigInfo(MainVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            SignConfigModel signConfigModel = new SignConfigModel();
            signConfigModel.setStore_id(vo.getStoreId());
            signConfigModel = signConfigModelMapper.selectOne(signConfigModel);
            if (signConfigModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CZSB, "操作失败");
            }
            List<Map<Integer, Integer>> scoreMap = SerializePhpUtils.getSignConfigByContinuity(signConfigModel.getContinuity());

            resultMap.put("is_status", signConfigModel.getIs_status());
            resultMap.put("score_num", signConfigModel.getScore_num());
            resultMap.put("starttime", signConfigModel.getStarttime());
            resultMap.put("endtime", signConfigModel.getEndtime());
            resultMap.put("is_remind", signConfigModel.getIs_remind());
            resultMap.put("score", signConfigModel.getScore());
            resultMap.put("continuity", signConfigModel.getContinuity());
            resultMap.put("detail", signConfigModel.getDetail());
            resultMap.put("is_many_time", signConfigModel.getIs_many_time());
            resultMap.put("sginParma", scoreMap);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("获取签到设置信息 异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "signConfigInfo");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> addSignConfig(AddSignConfigVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            int row = 0;
            if (StringUtils.isEmpty(vo.getStarttime()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QXZQDYXKSSJ, "请选择签到有效开始时间");
            }
            if (StringUtils.isEmpty(vo.getEndtime()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QXZQDYXJSSJ, "请选择签到有效结束时间");
            }
            else if (!DateUtil.dateCompare(vo.getEndtime(), vo.getStarttime()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QDYXSJCW, "签到有效时间错误");
            }
            //间隔时间,默认不间隔
            int resetTime = DateUtil.dateConversion(24, DateUtil.TimeType.TIME);
            //签到次数最低限制/间隔小时限制/间隔分钟限制/领取积分限制
            Integer minimumNum = 0;
            //签到次数最高限制
            Integer highestNum = 6;
            if (vo.getIsManyTime() == DictionaryConst.WhetherMaven.WHETHER_OK)
            {
                if (StringUtils.isEmpty(vo.getScoreNum()))
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QTXMTQDYXCS, "请填写每天签到有效次数");
                }
                if (!StringUtils.isInteger(vo.getScoreNum() + ""))
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_MTQDYXCSQTXZS, "每天签到有效次数请填写整数");
                }
                if (vo.getScoreNum() <= minimumNum)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_MTQDYXCSQTXDYDZS, "每天签到有效次数请填写大于0的整数");
                }
                else if (vo.getScoreNum() > highestNum)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_MTKQDCSZDBNCGC, "每天可签到次数最多不能超过6次");
                }
                if (vo.getResetH() == null || vo.getResetH().equals(minimumNum))
                {
                    if (vo.getResetI() == null || vo.getResetI().equals(minimumNum))
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_JGXSBNWK, "间隔小时不能为空");
                    }
                }
                //间隔时间转换成秒
                int resetTime1 = DateUtil.dateConversion(vo.getResetH(), DateUtil.TimeType.TIME) + DateUtil.dateConversion(vo.getResetI(), DateUtil.TimeType.MINUTE);
                if (resetTime1 >= resetTime)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_JGSJSZCCXZQZXSZ, "间隔时间设置超出限制，请重新设置");
                }
                resetTime = resetTime1;
            }
            if (vo.getScore() == null || vo.getScore().equals(minimumNum))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_LQJFBNWK, "领取积分不能为空");
            }
            if (!StringUtils.isInteger(vo.getScore() + ""))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_LQJFQTXZS, "领取积分请填写整数");
            }
            if (vo.getScore() <= 0)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_LQJFQTXDYDZS, "领取积分请填写大于0的整数");
            }
            //连续签到处理 {1:10},{2:20}
            Map<Integer, Integer> signParma = JSON.parseObject(vo.getScoreJson(), new TypeReference<Map<Integer, Integer>>()
            {
            });
            //[{3=10}, {30=30}]
            List<Map<Integer, Integer>> secoreList = new ArrayList<>();
            Integer                     keyTemp    = null;
            Integer                     secoreTemp = null;
            for (Integer key : signParma.keySet())
            {
                Map<Integer, Integer> secoreMap = new HashMap<>(16);
                Integer               secore    = signParma.get(key);
                if (key == 0 || secore == 0)
                {
                    continue;
                }
                if (secore <= 1)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_LXQDCSBNXYDY, "连续签到次数不能小于等于1");
                }
                if (keyTemp != null)
                {
                    if (key <= keyTemp - 1)
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSMYYCDZ, "次数没有依次递增");
                    }
                    if (secore <= secoreTemp)
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_JLJFMYYCDZ, "奖励积分没有依次递增");
                    }
                }
                keyTemp = key;
                secoreTemp = secore;
                secoreMap.put(keyTemp, secoreTemp);
                secoreList.add(secoreMap);
            }
            SignConfigModel signConfigSave = new SignConfigModel();
            signConfigSave.setIs_status(vo.getIsStatus());
            signConfigSave.setScore_num(vo.getScoreNum());
            signConfigSave.setStarttime(vo.getStarttime());
            signConfigSave.setEndtime(vo.getEndtime());
            signConfigSave.setIs_remind(vo.getIsRemind());
            signConfigSave.setReset(resetTime + "");
            signConfigSave.setScore(vo.getScore());
            signConfigSave.setContinuity(SerializePhpUtils.JavaSerializeByPhp(secoreList));
            signConfigSave.setDetail(vo.getDetail());
            signConfigSave.setModify_date(new Date());
            signConfigSave.setIs_many_time(vo.getIsManyTime());

            SignConfigModel signConfigOld = new SignConfigModel();
            signConfigOld.setStore_id(vo.getStoreId());
            signConfigOld = signConfigModelMapper.selectOne(signConfigOld);
            if (signConfigOld != null)
            {
                signConfigSave.setId(signConfigOld.getId());
                row = signConfigModelMapper.updateByPrimaryKeySelective(signConfigSave);
            }
            else
            {
                row = signConfigModelMapper.insertSelective(signConfigSave);
            }

            if (row < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CZSB, "操作失败");
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("添加/编辑签到配置 异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "addSignConfig");
        }
        return resultMap;
    }
}

