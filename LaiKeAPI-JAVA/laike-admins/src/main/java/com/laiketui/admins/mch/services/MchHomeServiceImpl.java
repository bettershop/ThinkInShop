package com.laiketui.admins.mch.services;

import com.laiketui.admins.api.mch.MchHomeService;
import com.laiketui.common.mapper.*;
import com.laiketui.common.utils.tool.cache.RedisDataTool;
import com.laiketui.core.cache.RedisUtil;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.DictionaryConst;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.core.utils.tool.DataUtils;
import com.laiketui.core.utils.tool.DateUtil;
import com.laiketui.core.utils.tool.StringUtils;
import com.laiketui.domain.config.AdminCgModel;
import com.laiketui.domain.config.ConfigModel;
import com.laiketui.domain.mch.MchBrowseModel;
import com.laiketui.domain.mch.MchModel;
import com.laiketui.domain.product.ProductClassModel;
import com.laiketui.domain.user.User;
import com.laiketui.domain.vo.MainVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

/**
 * 首页
 *
 * @author Trick
 * @date 2021/5/28 9:58
 */
@Service
public class MchHomeServiceImpl implements MchHomeService
{
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private OrderModelMapper orderModelMapper;

    @Autowired
    private ReturnOrderModelMapper returnOrderModelMapper;

    @Autowired
    private MchBrowseModelMapper mchBrowseModelMapper;

    @Autowired
    private MchModelMapper mchModelMapper;

    @Autowired
    private UserBaseMapper userBaseMapper;

    @Autowired
    private ConfigModelMapper configModelMapper;

    @Override
    public Map<String, Object> index(MainVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);
            //获取当月第一天和最后一天
            String startDate = DateUtil.getStartOfDay(new Date());
            String endDate   = DateUtil.getEndOfDay(new Date());

            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", vo.getStoreId());
            parmaMap.put("mch_id", user.getMchId());

            //今天订单数
            Map<String, Object> parmaTodayOrderMap = new HashMap<>(16);
            parmaTodayOrderMap.putAll(parmaMap);
            parmaTodayOrderMap.put("toDayOrder", DateUtil.dateFormate(new Date(), GloabConst.TimePattern.YMD));
            int todayOrderNum = orderModelMapper.countDynamic(parmaTodayOrderMap);
            //待付款订单数
            Map<String, Object> parmaNoPayOrderMap = new HashMap<>(16);
            parmaNoPayOrderMap.putAll(parmaMap);
            parmaNoPayOrderMap.put("status", DictionaryConst.OrdersStatus.ORDERS_R_STATUS_UNPAID);
            int noPayOrderNum = orderModelMapper.countDynamic(parmaNoPayOrderMap);
            //代发货订单数
            Map<String, Object> parmaNoSendOrderMap = new HashMap<>(16);
            parmaNoSendOrderMap.putAll(parmaMap);
            parmaNoSendOrderMap.put("toDayOrder", DictionaryConst.OrdersStatus.ORDERS_R_STATUS_CONSIGNMENT);
            int noSendOrderNum = orderModelMapper.countDynamic(parmaNoSendOrderMap);
            //代发货订单数
            int orderReturnWaitNum = returnOrderModelMapper.countOrderReturnWait(vo.getStoreId(), user.getMchId());
            //订单统计相关数据
            int[] countOrderList = {todayOrderNum, noPayOrderNum, noSendOrderNum, orderReturnWaitNum};
            //统计店铺总销售额
            BigDecimal countMchOrderSale = orderModelMapper.countMchOrderSale(vo.getStoreId(), user.getMchId());
            //统计店铺总访问量
            MchBrowseModel mchBrowseModel = new MchBrowseModel();
            mchBrowseModel.setStore_id(vo.getStoreId());
            mchBrowseModel.setMch_id(user.getMchId() + "");
            int countMchBrowseNum = mchBrowseModelMapper.countMchBrowseNum(mchBrowseModel);
            //统计店铺总支付订单量
            int countMchOrderNum = orderModelMapper.countMchOrderNum(vo.getStoreId(), user.getMchId());

            //总支付订单数量 数据图表(月表)
            List<Map<String, String>> getOrderNumByDayList = orderModelMapper.getOrderNumByDay(vo.getStoreId(), user.getMchId(), startDate, endDate);
            //获取转换率
            BigDecimal conversion        = new BigDecimal("0");
            int        countOrderUserNum = orderModelMapper.countOrderUserNum(vo.getStoreId(), user.getMchId());
            //如果没有访问量，但是有人买,代表转换率是100
            if (countMchBrowseNum == 0 && countOrderUserNum > 0)
            {
                conversion = new BigDecimal("100");
            }
            else
            {
                BigDecimal userNum   = new BigDecimal(countOrderUserNum + "");
                BigDecimal browseNum = new BigDecimal(countMchBrowseNum + "");
                if (browseNum.compareTo(BigDecimal.ZERO) > 0)
                {
                    conversion = userNum.divide(browseNum, 6, BigDecimal.ROUND_FLOOR).multiply(new BigDecimal("100")).setScale(2, BigDecimal.ROUND_HALF_UP);
                }
            }
            //日均销售额
            BigDecimal averageSale = new BigDecimal("0");
            String     orderDate   = orderModelMapper.getOldOrderTime(user.getMchId());
            if (!StringUtils.isEmpty(orderDate))
            {
                Date date = DateUtil.dateFormateToDate(orderDate, GloabConst.TimePattern.YMDHMS);
                if (date != null)
                {
                    long time    = date.getTime() / 1000;
                    long entTime = DateUtil.getTime();
                    //计算两个日期之间间隔多少天
                    int day = DateUtil.getBetweenDate(entTime, time);
                    averageSale = countMchOrderSale.divide(BigDecimal.valueOf(day), 2, BigDecimal.ROUND_HALF_UP);
                }
            }
            //日均访问量
            int      browseNum = 0;
            MchModel mchModel  = new MchModel();
            mchModel.setStore_id(vo.getStoreId());
            mchModel.setId(user.getMchId());
            mchModel = mchModelMapper.selectOne(mchModel);
            if (!StringUtils.isEmpty(mchModel))
            {
                Date date = DateUtil.dateFormateToDate(mchModel.getAdd_time(), GloabConst.TimePattern.YMDHMS);
                if (date != null)
                {
                    long time    = date.getTime() / 1000;
                    long entTime = DateUtil.getTime();
                    //计算两个日期之间间隔多少天
                    int        day            = DateUtil.getBetweenDate(entTime, time, true);
                    BigDecimal browseCountNum = countMchOrderSale.divide(BigDecimal.valueOf(day), 2, BigDecimal.ROUND_HALF_UP);
                    browseNum = browseCountNum.intValue();
                }
            }
            //获今天天开始、结束日期
            String toDayStartDate = DateUtil.getStartOfDay(new Date());
            //获取昨天开始、结束日期
            String yesterDayStartDate = DateUtil.getStartOfDay(DateUtil.getAddDate(new Date(), -1));
            String yesterDayEndDate   = DateUtil.getEndOfDay(DateUtil.getAddDate(new Date(), -1));
            //获取本周第一天
            Date weekDate    = DateUtil.dateFormateToDate(DateUtil.getSpanDate(2, true), GloabConst.TimePattern.YMD);
            Date weekEndDate = DateUtil.dateFormateToDate(DateUtil.getSpanDate(2, false), GloabConst.TimePattern.YMD);
            //计算上周开始、结束日期
            Date lastWeekEndDate   = DateUtil.getAddDate(weekDate, -1);
            Date lastWeekStartDate = DateUtil.getAddDate(weekDate, -7);

            //销售额周同比
            BigDecimal weekSale = BigDecimal.ZERO;
            //本周销售额
            BigDecimal thisWeekSale = orderModelMapper.sumSaleByDate(vo.getStoreId(), user.getMchId(), weekDate, null);
            //上周销售额
            BigDecimal oldWeekSale = orderModelMapper.sumSaleByDate(vo.getStoreId(), user.getMchId(), lastWeekStartDate, lastWeekEndDate);
            if (oldWeekSale.compareTo(BigDecimal.ZERO) == 0 && thisWeekSale.compareTo(BigDecimal.ZERO) > 0)
            {
                weekSale = new BigDecimal("100");
            }
            else if (thisWeekSale.compareTo(BigDecimal.ZERO) > 0 && oldWeekSale.compareTo(BigDecimal.ZERO) > 0)
            {
                weekSale = thisWeekSale.subtract(oldWeekSale).divide(oldWeekSale.multiply(new BigDecimal("100")), 2, BigDecimal.ROUND_HALF_UP);
            }
            //日销售占比
            BigDecimal daySale = BigDecimal.ZERO;
            //今天销售额
            BigDecimal toDaySale = orderModelMapper.sumSaleByDate(vo.getStoreId(), user.getMchId(), DateUtil.dateFormateToDate(toDayStartDate, GloabConst.TimePattern.YMDHMS), null);
            //昨天销售额
            BigDecimal yesterDaySale = orderModelMapper.sumSaleByDate(vo.getStoreId(), user.getMchId(), DateUtil.dateFormateToDate(yesterDayStartDate, GloabConst.TimePattern.YMDHMS), DateUtil.dateFormateToDate(yesterDayEndDate, GloabConst.TimePattern.YMDHMS));
            if (yesterDaySale.compareTo(BigDecimal.ZERO) == 0 && toDaySale.compareTo(BigDecimal.ZERO) > 0)
            {
                daySale = new BigDecimal("100");
            }
            else if (yesterDaySale.compareTo(BigDecimal.ZERO) > 0 && toDaySale.compareTo(BigDecimal.ZERO) > 0)
            {
                daySale = toDaySale.subtract(yesterDaySale).divide(yesterDaySale.multiply(new BigDecimal("100")), 2, BigDecimal.ROUND_HALF_UP);
            }
            //访客日同比
            BigDecimal dayBrowse = BigDecimal.ZERO;
            //今天销访客
            BigDecimal toDayBrowse = mchBrowseModelMapper.sumBrowseByDate(vo.getStoreId(), user.getMchId(), DateUtil.dateFormateToDate(toDayStartDate, GloabConst.TimePattern.YMDHMS), null);
            //昨天销访客
            BigDecimal yesterDayBrowse = mchBrowseModelMapper.sumBrowseByDate(vo.getStoreId(), user.getMchId(), DateUtil.dateFormateToDate(yesterDayStartDate, GloabConst.TimePattern.YMDHMS), DateUtil.dateFormateToDate(yesterDayEndDate, GloabConst.TimePattern.YMDHMS));
            if (yesterDayBrowse.compareTo(BigDecimal.ZERO) == 0 && toDayBrowse.compareTo(BigDecimal.ZERO) > 0)
            {
                dayBrowse = new BigDecimal("100");
            }
            else if (yesterDaySale.compareTo(BigDecimal.ZERO) > 0 && toDayBrowse.compareTo(BigDecimal.ZERO) > 0)
            {
                dayBrowse = toDayBrowse.subtract(yesterDayBrowse).divide(yesterDayBrowse.multiply(new BigDecimal("100")), 2, BigDecimal.ROUND_HALF_UP);
            }
            //访客周同比
            BigDecimal weekBrowse = BigDecimal.ZERO;
            //本周访客
            BigDecimal thisWeekBrowse = mchBrowseModelMapper.sumBrowseByDate(vo.getStoreId(), user.getMchId(), weekDate, null);
            //上周访客
            BigDecimal oldWeekBrowse = mchBrowseModelMapper.sumBrowseByDate(vo.getStoreId(), user.getMchId(), lastWeekStartDate, lastWeekEndDate);
            if (oldWeekBrowse.compareTo(BigDecimal.ZERO) == 0 && thisWeekBrowse.compareTo(BigDecimal.ZERO) > 0)
            {
                weekBrowse = new BigDecimal("100");
            }
            else if (thisWeekBrowse.compareTo(BigDecimal.ZERO) > 0 && oldWeekBrowse.compareTo(BigDecimal.ZERO) > 0)
            {
                weekBrowse = thisWeekBrowse.subtract(oldWeekBrowse).divide(oldWeekBrowse.multiply(new BigDecimal("100")), 2, BigDecimal.ROUND_HALF_UP);
            }

            //订单数量同比
            BigDecimal dayOrder = BigDecimal.ZERO;
            //今天销访客
            BigDecimal toDayOrder = orderModelMapper.sumSaleNumByDate(vo.getStoreId(), user.getMchId(), DateUtil.dateFormateToDate(toDayStartDate, GloabConst.TimePattern.YMDHMS), null);
            //昨天销访客
            BigDecimal yesterDayOrder = orderModelMapper.sumSaleNumByDate(vo.getStoreId(), user.getMchId(), DateUtil.dateFormateToDate(yesterDayStartDate, GloabConst.TimePattern.YMDHMS), DateUtil.dateFormateToDate(yesterDayEndDate, GloabConst.TimePattern.YMDHMS));
            if (yesterDayOrder.compareTo(BigDecimal.ZERO) == 0 && toDayOrder.compareTo(BigDecimal.ZERO) > 0)
            {
                dayOrder = new BigDecimal("100");
            }
            else if (yesterDayOrder.compareTo(BigDecimal.ZERO) > 0 && toDayOrder.compareTo(BigDecimal.ZERO) > 0)
            {
                dayOrder = toDayOrder.subtract(yesterDayOrder).divide(yesterDayOrder.multiply(new BigDecimal("100")), 2, BigDecimal.ROUND_HALF_UP);
            }

            //中间柱形图数据
            //今日销售额
            List<Map<String, BigDecimal>> toDaySaleList = new ArrayList<>();
            //今日访问数
            List<Map<String, Integer>> toDayBrowseList = new ArrayList<>();
            //获取今天每小时销售额
            Map<String, BigDecimal>       toDaySaleMap = new HashMap<>(16);
            List<Map<String, BigDecimal>> hourSaleList = orderModelMapper.getSaleNumByHour(vo.getStoreId(), user.getMchId(), new Date());
            for (Map<String, BigDecimal> hourMap : hourSaleList)
            {
                for (String key : hourMap.keySet())
                {
                    toDaySaleMap.put(key, hourMap.get(key));
                }
            }
            //获取今天每小时访问数
            Map<String, Integer>      toDayBrowseMap = new HashMap<>(16);
            List<Map<String, Object>> hourBrowseList = mchBrowseModelMapper.getBrowseNumByHour(vo.getStoreId(), user.getMchId(), new Date());
            for (Map<String, Object> hourMap : hourBrowseList)
            {
                for (String key : hourMap.keySet())
                {
                    toDayBrowseMap.put(key, StringUtils.stringParseInt(hourMap.get(key)));
                }
            }
            //组装24小时数据
            for (int i = 0; i < 24; i++)
            {
                Map<String, Integer>    browseMap = new HashMap<>(16);
                Map<String, BigDecimal> saleMap   = new HashMap<>(16);
                String                  timeStr   = String.format("%s时", i);
                browseMap.put(timeStr, toDayBrowseMap.getOrDefault(timeStr, 0));
                saleMap.put(timeStr, toDaySaleMap.getOrDefault(timeStr, BigDecimal.ZERO));
                toDayBrowseList.add(browseMap);
                toDaySaleList.add(saleMap);
            }

            //月销售额/访问+周销售额/访问
            List<Map<String, BigDecimal>> toDayMonthSaleList   = new ArrayList<>();
            List<Map<String, Integer>>    toDayMonthBrowseList = new ArrayList<>();
            List<Map<String, BigDecimal>> toDayWeekSaleList    = new ArrayList<>();
            List<Map<String, Integer>>    toDayWeekBrowseList  = new ArrayList<>();

            //获本月每天销售额
            Map<String, BigDecimal>       toDayMonthSaleMap = new HashMap<>(16);
            List<Map<String, BigDecimal>> monthSaleList     = orderModelMapper.getSaleNumByMonth(vo.getStoreId(), user.getMchId(), weekDate, weekEndDate);
            for (Map<String, BigDecimal> monthMap : monthSaleList)
            {
                for (String key : monthMap.keySet())
                {
                    toDayMonthSaleMap.put(key, monthMap.get(key));
                }
            }
            //获本月每天访问量
            Map<String, Integer>      toDayMonthBrowseMap = new HashMap<>(16);
            List<Map<String, Object>> monthBrowseList     = mchBrowseModelMapper.getBrowseNumByMonth(vo.getStoreId(), user.getMchId(), weekDate, weekEndDate);
            for (Map<String, Object> monthMap : monthBrowseList)
            {
                for (String key : monthMap.keySet())
                {
                    toDayMonthBrowseMap.put(key, StringUtils.stringParseInt(monthMap.get(key)));
                }
            }
            //获取当月天数
            int monthDayNum = StringUtils.stringParseInt(DateUtil.dateFormate(DateUtil.getMonthLastDay(new Date()), GloabConst.TimePattern.D1, GloabConst.TimePattern.YMDHMS));
            //获取本周 day
            int weekStart = StringUtils.stringParseInt(DateUtil.dateFormate(weekDate, GloabConst.TimePattern.D1));
            int weekEnd   = StringUtils.stringParseInt(DateUtil.dateFormate(weekEndDate, GloabConst.TimePattern.D1));
            //获取本月 month 字符串
            String monthStr = DateUtil.dateFormate(weekEndDate, GloabConst.TimePattern.MM);

            //组装当月每天数据
            for (int i = 1; i < monthDayNum; i++)
            {
                Map<String, Integer>    browseMap     = new HashMap<>(16);
                Map<String, Integer>    browseWeekMap = new HashMap<>(16);
                Map<String, BigDecimal> saleMap       = new HashMap<>(16);
                Map<String, BigDecimal> saleWeekMap   = new HashMap<>(16);
                String                  timeStr       = monthStr + "/" + i;
                if (i < 10)
                {
                    timeStr = String.format("%s/0%s", monthStr, i);
                }
                //判断是否是本周
                if (i >= weekStart && i <= weekEnd)
                {
                    browseWeekMap.put(timeStr, toDayMonthBrowseMap.getOrDefault(timeStr, 0));
                    saleWeekMap.put(timeStr, toDayMonthSaleMap.getOrDefault(timeStr, BigDecimal.ZERO));
                    toDayWeekSaleList.add(saleWeekMap);
                    toDayWeekBrowseList.add(browseWeekMap);
                }
                //本月
                browseMap.put(timeStr, toDayMonthBrowseMap.getOrDefault(timeStr, 0));
                saleMap.put(timeStr, toDayMonthSaleMap.getOrDefault(timeStr, BigDecimal.ZERO));
                toDayMonthBrowseList.add(browseMap);
                toDayMonthSaleList.add(saleMap);
            }

            //店铺已提现金额
            parmaMap.clear();
            parmaMap.put("store_id", vo.getStoreId());
            parmaMap.put("status", 2);
            parmaMap.put("type", 3);
            BigDecimal mchWithdraw = mchAccountLogModelMapper.sumMchAccountLogDynamic(parmaMap);
            //店铺退款金额
            parmaMap.put("type", 2);
            BigDecimal mchReturnAmt = mchAccountLogModelMapper.sumMchAccountLogDynamic(parmaMap);
            //获取7日趋势图
            Date oldStartDate = DateUtil.getAddDate(new Date(), -6);
            //余额、提现、退款 7日趋势数据
            List<Map<String, BigDecimal>> mchAmtList      = new ArrayList<>();
            List<Map<String, BigDecimal>> withdrawAmtList = new ArrayList<>();
            List<Map<String, BigDecimal>> returnAmtList   = new ArrayList<>();
            for (int i = 0; i < 7; i++)
            {
                Date                    currentDate = DateUtil.getAddDate(oldStartDate, i);
                String                  currentStr  = DateUtil.dateFormate(currentDate, GloabConst.TimePattern.YMD);
                Map<String, BigDecimal> mchAmtMap   = mchAccountLogModelMapper.getMchMoneyByDate(vo.getStoreId(), user.getMchId(), currentDate);
                if (mchAmtMap == null)
                {
                    mchAmtMap = new HashMap<>(1);
                    mchAmtMap.put(currentStr, BigDecimal.ZERO);
                }
                mchAmtList.add(mchAmtMap);
                Map<String, BigDecimal> withdrawAmtMap = mchAccountLogModelMapper.getMchWithdrawByDate(vo.getStoreId(), user.getMchId(), currentDate);
                Map<String, BigDecimal> returnAmtMap   = mchAccountLogModelMapper.getMchReturnMoneyByDate(vo.getStoreId(), user.getMchId(), currentDate);
                withdrawAmtList.add(withdrawAmtMap);
                returnAmtList.add(returnAmtMap);
            }
            //获取销售占比
            List<Map<String, Object>> resultGoodsClassMoneys = new ArrayList<>();
            List<Integer>             goodsClassIdList       = orderDetailsModelMapper.getSaleProductClassList(user.getMchId());
            for (Integer classId : goodsClassIdList)
            {
                //获取类别名称
                ProductClassModel       productClassModel = productClassModelMapper.selectByPrimaryKey(classId);
                Map<String, BigDecimal> classAmtMap       = orderDetailsModelMapper.getSaleProductClassAmt(user.getMchId(), classId);
                BigDecimal              afterDiscount     = classAmtMap.get("price");
                BigDecimal              freight           = classAmtMap.get("freight");
                BigDecimal              zprice            = afterDiscount.add(freight);
                Map<String, Object>     map               = new HashMap<>(16);
                map.put("count", zprice);
                map.put("item", productClassModel.getPname());
                map.put("percent", zprice.divide(countMchOrderSale, 4, BigDecimal.ROUND_HALF_UP));
                resultGoodsClassMoneys.add(map);
            }
            DataUtils.mapSort(resultGoodsClassMoneys, "percent", DataUtils.Sort.DESC);

            //日均销售额
            resultMap.put("average_sale", averageSale.toString());
            //店铺余额
            resultMap.put("accout_in", mchModel.getAccount_money());
            //店铺余额趋势
            resultMap.put("account_in_arr", mchAmtList);
            //店铺提现金额
            resultMap.put("account_withdraw", mchWithdraw);
            //店铺提现金额 趋势
            resultMap.put("account_withdraw_arr", withdrawAmtList);
            //店铺退款金额
            resultMap.put("account_return", mchReturnAmt);
            //店铺退款7日变化趋势
            resultMap.put("account_return_arr", returnAmtList);
            //日均访问量
            resultMap.put("browse_num", browseNum);
            //统计店铺总访问量
            resultMap.put("coustomer_num_z", countMchBrowseNum);
            //转换率
            resultMap.put("conversion", conversion.toString() + "%");
            //订单统计相关数据
            resultMap.put("list_sno", countOrderList);
            //销售占比
            resultMap.put("sales_class", resultGoodsClassMoneys);
            //销售额周同比
            resultMap.put("yoy_week", weekSale);
            //日销售占比
            resultMap.put("yoy_day", daySale);
            //访客日同比
            resultMap.put("yoy_salesw", weekBrowse);
            //访客周同比
            resultMap.put("yoy_salesn", dayBrowse);
            //订单数量同比
            resultMap.put("yoy_order", dayOrder);
            //今日销售额占比图数据
            resultMap.put("z_sales_day", toDaySaleList);
            //今日访问数占比图数据
            resultMap.put("z_coustomer_day", toDayBrowseList);
            //本月销售额占比图数据
            resultMap.put("z_sales_month", toDayMonthSaleList);
            //本月访问数占比图数据
            resultMap.put("z_coustomer_month", toDayMonthBrowseList);
            //本周销售额占比图数据
            resultMap.put("z_sales_week", toDayWeekSaleList);
            //本周访问数占比图数据
            resultMap.put("z_coustomer_week", toDayWeekBrowseList);
            //统计店铺总销售额
            resultMap.put("z_total", countMchOrderSale);
            //统计店铺总支付订单量
            resultMap.put("z_num", countMchOrderNum);
            //总支付订单数量 数据图表(月表)
            resultMap.put("z_num_arr", getOrderNumByDayList);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("店铺首页 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "index");
        }
        return resultMap;
    }


    @Override
    public Map<String, Object> info(MainVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            ConfigModel configModel = new ConfigModel();
            configModel.setStore_id(vo.getStoreId());
            configModel = configModelMapper.selectOne(configModel);
            if (Objects.nonNull(configModel))
            {
                configModel.setLogo(configModel.getLogon_logo());
                resultMap.put("config", configModel);
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取店铺页面信息 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "info");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> storeList(MainVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            List<Map<String, Object>> list = customerModelMapper.storeList(new Date());
            resultMap.put("list", list);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取所有商城 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "storeList");
        }
        return resultMap;
    }

    @Override
    public List<AdminCgModel> getRegion(MainVo vo, Integer level, Integer sid) throws LaiKeAPIException
    {
        try
        {
            AdminCgModel adminCgModel = new AdminCgModel();
            adminCgModel.setDistrictLevel(level);
            if (sid != null)
            {
                adminCgModel.setDistrictPid(sid);
            }
            return adminCgModelMapper.select(adminCgModel);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取地区信息 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getRegion");
        }
    }

    @Override
    public void selectLanguage(MainVo vo) throws LaiKeAPIException
    {
        try
        {
            User user    = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);
            User updUser = new User();
            if (StringUtils.isEmpty(vo.getLanguage()))
            {
                updUser.setLang(GloabConst.Lang.CN);
            }
            else
            {
                updUser.setLang(vo.getLanguage());
            }
            // 更新
            updUser.setId(user.getId());
            updUser.setStore_id(vo.getStoreId());
            userBaseMapper.updateByPrimaryKeySelective(updUser);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("设置店铺后台管理-系统语言 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "selectLanguage");
        }

    }


    @Autowired
    private AdminCgModelMapper adminCgModelMapper;

    @Autowired
    private MchAccountLogModelMapper mchAccountLogModelMapper;

    @Autowired
    private CustomerModelMapper customerModelMapper;

    @Autowired
    private OrderDetailsModelMapper orderDetailsModelMapper;

    @Autowired
    private ProductClassModelMapper productClassModelMapper;

    @Autowired
    private SystemConfigurationModelMapper systemConfigurationModelMapper;
}

