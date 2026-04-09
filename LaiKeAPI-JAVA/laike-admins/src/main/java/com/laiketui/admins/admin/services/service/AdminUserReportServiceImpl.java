package com.laiketui.admins.admin.services.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.laiketui.admins.api.admin.report.AdminUserReportService;
import com.laiketui.common.api.PublicUserService;
import com.laiketui.common.mapper.*;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.core.utils.tool.DateUtil;
import com.laiketui.domain.report.OrderReportModel;
import com.laiketui.domain.user.User;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.plugin.member.MemberOrderVo;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdminUserReportServiceImpl implements AdminUserReportService
{
    private final Logger     logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private       UserMapper userMapper;

    @Autowired
    private OrderModelMapper orderModelMapper;

    @Autowired
    private UserBaseMapper userBaseMapper;

    @Autowired
    private OrderDataModelMapper orderDataModelMapper;

    /**
     * 用户报表----
     *
     * @param vo
     * @return
     */
    @Override
    public Map<String, Object> getUserData(MainVo vo) throws LaiKeAPIException
    {
        try
        {
            //当前日时间
            Date         currentDate = DateUtil.dateFormateToDate(new Date(), GloabConst.TimePattern.YMD);
            List<String> dateList    = new ArrayList<>();
            //当天
            dateList.add(DateUtil.dateFormate(currentDate, GloabConst.TimePattern.YMD));
            //前一天
            dateList.add(DateUtil.dateFormate(DateUtil.getAddDate(currentDate, -1), GloabConst.TimePattern.YMD));
            //前一周
            dateList.add(DateUtil.dateFormate(DateUtil.getAddDate(currentDate, -7), GloabConst.TimePattern.YMD));
            //前一月
            dateList.add(DateUtil.dateFormate(DateUtil.getAddDateByMonth(currentDate, -1), GloabConst.TimePattern.YMD));
            Map<String, Object> detailMap = new HashMap<>();
            for (int i = 0; i < dateList.size(); i++)
            {
                String                    timeStr;
                List<Map<String, Object>> activeCount;
                List<List>                dataList = new ArrayList<>();
                Map<String, Integer>      collect;
                List<String>              hour     = new ArrayList<>();
                List<Integer>             num      = new ArrayList<>();
                switch (i)
                {
                    case 0:
                        //map：{hour:dd-00, num：Integer}
                        activeCount = userBaseMapper.getActiveCountByHour(vo.getStoreId(), dateList.get(i));
                        //map：{hour：num}
                        collect = activeCount.stream().collect(Collectors.toMap(key -> MapUtils.getString(key, "hour"), value -> MapUtils.getInteger(value, "num")));
//                      hour = activeCount.stream().map(e -> MapUtils.getString(e, "hour")).collect(Collectors.toList());
//                      num = activeCount.stream().map(e -> MapUtils.getInteger(e, "num")).collect(Collectors.toList());
                        //组装每天24小时数据
                        for (int j = 0; j < 24; j++)
                        {
                            if (j < 10)
                            {
                                timeStr = String.format("0" + "%s:00", j);
                            }
                            else
                            {
                                timeStr = String.format("%s:00", j);
                            }
                            //时间
                            hour.add(timeStr);
                            //用户数量
                            num.add(collect.getOrDefault(timeStr, 0));
                        }
                        dataList.add(hour);
                        dataList.add(num);
                        //今天
                        detailMap.put("today", dataList);
                        break;
                    case 1:
                        activeCount = userBaseMapper.getActiveCountByHour(vo.getStoreId(), dateList.get(i));
                        //map：{hour：num}
                        collect = activeCount.stream().collect(Collectors.toMap(key -> MapUtils.getString(key, "hour"), value -> MapUtils.getInteger(value, "num")));
//                        hour = activeCount.stream().map(e -> MapUtils.getString(e, "hour")).collect(Collectors.toList());
//                        num = activeCount.stream().map(e -> MapUtils.getInteger(e, "num")).collect(Collectors.toList());
                        //组装每天24小时数据
                        for (int j = 0; j < 24; j++)
                        {
                            if (j < 10)
                            {
                                timeStr = String.format("0" + "%s:00", j);
                            }
                            else
                            {
                                timeStr = String.format("%s:00", j);
                            }
                            //时间
                            hour.add(timeStr);
                            //用户数量
                            num.add(collect.getOrDefault(timeStr, 0));
                        }
                        dataList.add(hour);
                        dataList.add(num);
                        detailMap.put("yesterday", dataList);
                        break;
                    case 2:
                        String weekTime = dateList.get(i);
                        activeCount = userBaseMapper.getActiveCountByDay(vo.getStoreId(), weekTime);
                        collect = activeCount.stream().collect(Collectors.toMap(key -> MapUtils.getString(key, "day"), value -> MapUtils.getInteger(value, "num")));
                        for (int j = 0; j < 7; j++)
                        {
                            weekTime = DateUtil.dateFormate(DateUtil.getAddDate(DateUtil.dateFormateToDate(weekTime, GloabConst.TimePattern.YMD), 1), GloabConst.TimePattern.YMD);
                            hour.add(weekTime);
                            num.add(collect.getOrDefault(weekTime, 0));
                        }
                        dataList.add(hour);
                        dataList.add(num);
                        detailMap.put("thisweek", dataList);
                        break;
                    case 3:
                        activeCount = userBaseMapper.getActiveCountByDay(vo.getStoreId(), dateList.get(i));
                        hour = activeCount.stream().map(e -> MapUtils.getString(e, "day")).collect(Collectors.toList());
                        num = activeCount.stream().map(e -> MapUtils.getInteger(e, "num")).collect(Collectors.toList());
                        dataList.add(hour);
                        dataList.add(num);
                        detailMap.put("thismonth", dataList);
                        break;
                }
            }
            return detailMap;
        }
        catch (Exception e)
        {
            logger.error("用户信息报表，活跃用户异常：" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_, "网络异常", "getUserData");
        }

    }

    /**
     * 用户统计---用户来源统计
     *
     * @param vo
     * @return
     */
    @Override
    public List<Map<String, Object>> getUserAmount(MainVo vo) throws LaiKeAPIException
    {
        try
        {
            List<Map<String, Object>> returnList = userMapper.countBySource(vo.getStoreId());

            //数据-用户报表，英文适配问题
            if (vo.getLanguage().equals("en_US"))
            {
                for (Map<String, Object> map : returnList)
                {
                    String name = MapUtils.getString(map, "name");
                    switch (name)
                    {
                        case "H5端":
                            map.put("name", "H5");
                            break;
                        case "小程序端":
                            map.put("name", "Mini Program");
                            break;
                        case "App":
                            map.put("name", "App");
                            break;
                        case "PC端":
                            map.put("name", "PC");
                            break;
                        default:
                            map.put("name", "Other");
                            break;
                    }
                }
            }

            return returnList;
        }
        catch (Exception e)
        {
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_, "查询失败！", "getUserAmount");
        }
    }

    /**
     * 用户报表--新增用户统计
     *
     * @param vo
     * @return
     */
    @Override
    public Map<String, Object> getAdditionUserData(MainVo vo) throws LaiKeAPIException
    {
        try
        {
            Date       currentDate = DateUtil.dateFormateToDate(new Date(), GloabConst.TimePattern.YMD);
            List<Date> dateList    = new ArrayList<>();
            //减一周
            dateList.add(DateUtil.getAddDate(currentDate, -7));
            //减一月
            dateList.add(DateUtil.getAddDateByMonth(currentDate, -1));
            //减一年
            dateList.add(DateUtil.getAddDateByYear(currentDate, -1));
            Map<String, Object> detailMap = new HashMap<>();

            for (int i = 0; i < dateList.size(); i++)
            {
                List<List> dataList = new ArrayList<>();
                //获取输入时间到当前时间之间的时间
                List<Date> days = DateUtil.createDays(dateList.get(i));
                //全部日期
                List<String> daysStr = new ArrayList<>();
                //对于平台的新增用户
                List<Integer> app_num = new ArrayList<>();
                List<Integer> h5_num  = new ArrayList<>();
                List<Integer> pc_num  = new ArrayList<>();
                List<Integer> xcx_num = new ArrayList<>();
                for (Date day : days)
                {
                    String dayStr = DateUtil.dateFormate(day, GloabConst.TimePattern.YMD);
                    daysStr.add(dayStr);
                    int[] sources = {2, 1, 7, 6};
                    for (int source : sources)
                    {
                        //查询用户数量
                        Integer additionUserByDay = userMapper.getAdditionUserByDay(vo.getStoreId(), dayStr, source);
                        switch (source)
                        {
                            case 2:
                                app_num.add(additionUserByDay == null ? 0 : additionUserByDay);
                                break;
                            case 7:
                                h5_num.add(additionUserByDay == null ? 0 : additionUserByDay);
                                break;
                            case 6:
                                pc_num.add(additionUserByDay == null ? 0 : additionUserByDay);
                                break;
                            case 1:
                                xcx_num.add(additionUserByDay == null ? 0 : additionUserByDay);
                                break;
                        }
                    }
                }
                dataList.add(daysStr);
                dataList.add(app_num);
                dataList.add(xcx_num);
                dataList.add(h5_num);
                dataList.add(pc_num);

                switch (i)
                {
                    case 0:
                        detailMap.put("week", dataList);
                        break;
                    case 1:
                        detailMap.put("month", dataList);
                        break;
                    case 2:
                        detailMap.put("year", dataList);
                        break;
                }
            }
            return detailMap;
        }
        catch (Exception e)
        {
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_, "网络异常", "getAdditionUserData");
        }
    }

    /**
     * 用户报表 -- 会员统计
     *
     * @param vo
     * @return
     */
    public HashMap<String, Object> getMembershipStatistics(MainVo vo) throws LaiKeAPIException
    {
        HashMap<String, Object> map = new HashMap<>();
        try
        {
            Date       currentDate = DateUtil.dateFormateToDate(new Date(), GloabConst.TimePattern.YMD);
            List<Date> dateList    = new ArrayList<>();
            //按周
            dateList.add(DateUtil.getAddDate(currentDate, -7));
            //按月
            dateList.add(DateUtil.getAddDateByMonth(currentDate, -1));
            //按年
            dateList.add(DateUtil.getAddDateByYear(currentDate, -1));
            for (int i = 0; i < dateList.size(); i++)
            {
                //日期
                ArrayList<Object> date = new ArrayList<>();
                //开通会员数
                ArrayList<Object> memberNumList = new ArrayList<>();
                //会员过期数
                ArrayList<Object> expiredMemberNumList = new ArrayList<>();
                //会员续费数
                ArrayList<Object> renewalMemberList = new ArrayList<>();
                if (i < 2)
                {
                    List<Date> days = DateUtil.createDays(dateList.get(i));
                    for (Date day : days)
                    {
                        ArrayList<String> userIdList = new ArrayList<>();
                        String            paramday   = DateUtil.dateFormate(day, GloabConst.TimePattern.YMD);
                        //开通会员数
                        int memberNum = userBaseMapper.getMembershipStatisticsByDay(vo.getStoreId(), paramday);
                        //会员过期数
                        int expiredMemberNum = userBaseMapper.getExpiredMemberByDay(vo.getStoreId(), paramday);
                        //会员续费数
                        List<Map<String, Object>> renewalMemberNByDay = orderDataModelMapper.getRenewalMemberNByDay(paramday);
                        if (renewalMemberNByDay != null)
                            for (Map<String, Object> objectMap : renewalMemberNByDay)
                            {
                                MemberOrderVo memberOrderVo = JSONObject.parseObject((String) objectMap.get("data"), MemberOrderVo.class);
                                if (memberOrderVo.getStoreId().equals(vo.getStoreId()) && !userIdList.contains(memberOrderVo.getUserId()))
                                {
                                    userIdList.add(memberOrderVo.getUserId());
                                }
                            }
                        date.add(paramday);
                        memberNumList.add(memberNum);
                        expiredMemberNumList.add(expiredMemberNum);
                        renewalMemberList.add(userIdList.size());
                    }
                }
                else
                {
                    List<Date> days = DateUtil.createMonths(dateList.get(i));
                    for (Date day : days)
                    {
                        ArrayList<String> userIdList = new ArrayList<>();
                        String            paramday   = DateUtil.dateFormate(day, GloabConst.TimePattern.YM);
                        //开通会员数
                        int memberNum = userBaseMapper.getMembershipStatisticsByMonth(vo.getStoreId(), paramday);
                        //会员过期数
                        int expiredMemberNum = userBaseMapper.getExpiredMemberByMonth(vo.getStoreId(), paramday);
                        //会员续费数
                        List<Map<String, Object>> renewalMemberNByMonth = orderDataModelMapper.getRenewalMemberNByMonth(paramday);
                        if (renewalMemberNByMonth != null)
                            for (Map<String, Object> objectMap : renewalMemberNByMonth)
                            {
                                MemberOrderVo memberOrderVo = JSONObject.parseObject((String) objectMap.get("data"), MemberOrderVo.class);
                                if (memberOrderVo.getStoreId().equals(vo.getStoreId()) && !userIdList.contains(memberOrderVo.getUserId()))
                                {
                                    userIdList.add(memberOrderVo.getUserId());
                                }
                            }
                        date.add(paramday);
                        memberNumList.add(memberNum);
                        expiredMemberNumList.add(expiredMemberNum);
                        renewalMemberList.add(userIdList.size());
                    }
                }
                ArrayList<Object> allList = new ArrayList<>();
                allList.add(date);
                allList.add(memberNumList);
                allList.add(expiredMemberNumList);
                allList.add(renewalMemberList);
                switch (i)
                {
                    case 0:
                        map.put("week", allList);
                        break;
                    case 1:
                        map.put("month", allList);
                        break;
                    case 2:
                        map.put("year", allList);
                        break;
                }
            }
        }
        catch (Exception e)
        {
            logger.error("用户报表会员部分异常 :" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_, "网络异常", "getAdditionUserData");
        }
        return map;
    }

    /**
     * 用户报表--用户消费排行
     *
     * @param vo
     * @return
     */
    public Map<String, Object> getMoneyTop(MainVo vo) throws LaiKeAPIException
    {
        Map<String, Object> detailMap = new HashMap<>(16);
        try
        {
            Date       currentDate = DateUtil.dateFormateToDate(new Date(), GloabConst.TimePattern.YMD);
            List<Date> dateList    = new ArrayList<>();
            dateList.add(DateUtil.getAddDate(currentDate, -6));
            dateList.add(DateUtil.getAddDateByMonth(currentDate, -1));
            dateList.add(DateUtil.getAddDateByYear(currentDate, -1));
            for (int i = 0; i < dateList.size(); i++)
            {
                List<List> dataList = new ArrayList<>();

                List<Map<String, Object>> userCount = orderModelMapper.countByUser(vo.getStoreId(), dateList.get(i));
                List<String> name = userCount.stream().map(e ->
                {
                    String userId = MapUtils.getString(e, "name");
                    User   user   = userBaseMapper.selectByUserId(vo.getStoreId(), userId);
                    String userName;
                    if (user == null)
                    {
                        userName = "来客推";
                    }
                    else
                    {
                        userName = user.getUser_name();
                    }
                    e.put("name", userName);
                    return userName;
                }).collect(Collectors.toList());
                List<Integer> amount = userCount.stream().map(e -> MapUtils.getInteger(e, "amount")).collect(Collectors.toList());
                dataList.add(name);
                dataList.add(amount);
                switch (i)
                {
                    case 0:
                        detailMap.put("week", dataList);
                        detailMap.put("weekdata", userCount);
                        break;
                    case 1:
                        detailMap.put("month", dataList);
                        detailMap.put("monthdata", userCount);
                        break;
                    case 2:
                        detailMap.put("year", dataList);
                        detailMap.put("yeardata", userCount);
                        break;
                }
            }
            List<List>                dataList     = new ArrayList<>();
            List<Map<String, Object>> userCountAll = orderModelMapper.countByUser1(vo.getStoreId());
            List<String> name = userCountAll.stream().map(e ->
            {
                String userId = MapUtils.getString(e, "name");
                User   user   = userBaseMapper.selectByUserId(vo.getStoreId(), userId);
                String userName;
                if (user == null)
                {
                    userName = "来客推";
                }
                else
                {
                    userName = user.getUser_name();
                }
                e.put("name", userName);
                return userName;
            }).collect(Collectors.toList());
            List<Integer> amount = userCountAll.stream().map(e -> MapUtils.getInteger(e, "amount")).collect(Collectors.toList());
            dataList.add(name);
            dataList.add(amount);
            detailMap.put("all", dataList);
            detailMap.put("alldata", userCountAll);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("用户报表--用户消费排行 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "aMchOrderIndex");
        }
        return detailMap;
    }


    @Override
    public Map<String, Object> additionData(MainVo vo) throws LaiKeAPIException
    {
        Map<String, Object> detailMap = new HashMap<>(16);
        try
        {
            Example          example  = new Example(OrderReportModel.class);
            Example.Criteria criteria = example.createCriteria();
            criteria.andEqualTo("type", 5);
            criteria.andEqualTo("storeId", vo.getStoreId());
            OrderReportModel orderReportModel = reportModelMapper.selectOneByExample(example);
            if (orderReportModel != null)
            {
                detailMap.put("additiondata",
                        JSON.parseObject(orderReportModel.getData()));
            }
            else
            {
                //添加日期数据
                Map<String, Object> additionUserData = publicUserService.getAdditionUserData(vo.getStoreId());
                OrderReportModel    model            = new OrderReportModel();
                model.setType(5);
                model.setNum(0);
                model.setStoreId(vo.getStoreId());
                model.setData(JSON.toJSONString(additionUserData));
                int insert = orderReportModelMapper.insert(model);
                detailMap.put("additiondata", additionUserData);
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("新增用户报表 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "aMchOrderIndex");
        }
        return detailMap;
    }

    @Autowired
    private OrderReportModelMapper reportModelMapper;

    @Autowired
    private PublicUserService publicUserService;

    @Autowired
    private OrderReportModelMapper orderReportModelMapper;
}
