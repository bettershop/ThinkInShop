package com.laiketui.admins.admin.services.service;

import com.laiketui.admins.api.admin.report.AdminStoreIndexReportService;
import com.laiketui.common.mapper.*;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.core.utils.tool.DateUtil;
import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdminStoreIndexReportImpl implements AdminStoreIndexReportService
{
    @Autowired
    private ProductListModelMapper productListModelMapper;

    @Autowired
    private OrderModelMapper orderModelMapper;

    @Autowired
    private MchModelMapper mchModelMapper;

    @Autowired
    private SupplierModelMapper supplierModelMapper;

    @Autowired
    private WithdrawModelMapper withdrawModelMapper;

    @Autowired
    private StockModelMapper stockModelMapper;

    /**
     * 首页报表的数据展示---全量
     *
     * @param storeid
     * @return
     */
    @Override
    public Map<String, Object> getReportData(Integer storeid)
    {

        try
        {
            Map<String, Object> resultMap = new HashMap<>(16);

            Map<String, Object> volumeMap   = new HashMap<>();
            Map<String, Object> mchMap      = new HashMap<>();
            Map<String, Object> supplierMap = new HashMap<>();
            //查询商品数量
            resultMap.put("goodsData", getGoodsNum(storeid));
            //当前时间yyyy-mm-dd
            Date currentDate = DateUtil.dateFormateToDate(new Date(), GloabConst.TimePattern.YMD);
            //当前日
            String today = DateUtil.dateFormate(currentDate, GloabConst.TimePattern.YMD);
            //上一个月
            String month    = DateUtil.dateFormate(DateUtil.getAddDateByMonth(currentDate, -1), GloabConst.TimePattern.YMD);
            String monthday = DateUtil.dateFormate(currentDate, GloabConst.TimePattern.YM);
            //减一星期
            String week = DateUtil.dateFormate(DateUtil.getAddDate(currentDate, -6), GloabConst.TimePattern.YMD);
            //减一年
            String year = DateUtil.dateFormate(DateUtil.getAddDateByYear(currentDate, -1), GloabConst.TimePattern.YMD);
            //减两星期
            String lastWeek = DateUtil.dateFormate(DateUtil.getAddDate(currentDate, -13), GloabConst.TimePattern.YMD);
            //上一个月当前时间减一个星期
            String lastMonthWeek = DateUtil.dateFormate(DateUtil.getAddDate(DateUtil.dateFormateToDate(month, GloabConst.TimePattern.YMD), -6), GloabConst.TimePattern.YMD);
            //减一天
            String yesterday = DateUtil.dateFormate(DateUtil.getAddDate(currentDate, -1), GloabConst.TimePattern.YMD);
            //本周一
            String Monday = DateUtil.dateFormate(getThisWeekMonday(currentDate), GloabConst.TimePattern.YMD);
            //上周一
            String LastMonday = DateUtil.dateFormate(geLastWeekMonday(currentDate), GloabConst.TimePattern.YMD);
            //今日零点至现在的所有订单交易总额
            BigDecimal currentVolume = orderModelMapper.getCurrentVolume(storeid, today);
            //昨日交易
            BigDecimal yesterdayVolume = orderModelMapper.getCurrentVolume(storeid, yesterday);
            //月交易
            BigDecimal monthtVolume = orderModelMapper.getCurrentVolumeByMonth(storeid, monthday);
            //总交易
            BigDecimal allVolume = orderModelMapper.sumAllMoeny(storeid);
            ;
            NumberFormat dF = NumberFormat.getInstance();
            dF.setMaximumFractionDigits(2);
            volumeMap.put("currentVolume", currentVolume == null ? BigDecimal.ZERO : currentVolume);
            volumeMap.put("yesterdayVolume", yesterdayVolume == null ? BigDecimal.ZERO : yesterdayVolume);
            volumeMap.put("monthtVolume", monthtVolume == null ? BigDecimal.ZERO : monthtVolume);
            volumeMap.put("allVolume", allVolume == null ? BigDecimal.ZERO : allVolume);
            BigDecimal subtract = currentVolume.subtract(yesterdayVolume);
            volumeMap.put("flag", subtract.compareTo(BigDecimal.ZERO) >= 0 ? "up" : "down");
            if (subtract.compareTo(BigDecimal.ZERO) == 0)
            {
                volumeMap.put("rate", "0%");
            }
            else
            {
                volumeMap.put("rate", yesterdayVolume.compareTo(BigDecimal.ZERO) == 0 ? "100%" : dF.format(Math.abs(subtract.floatValue()) / yesterdayVolume.floatValue() * 100) + "%");
            }
            //交易总额
            resultMap.put("volumeData", volumeMap);

            Integer weekAmount          = mchModelMapper.getWeekAmount(storeid, Monday, today);
            Integer betweenHbWeekAmount = mchModelMapper.getBetweenWeekAmount(storeid, LastMonday, Monday);
            Integer betweenTbWeekAmount = mchModelMapper.getBetweenWeekAmount(storeid, lastMonthWeek, month);
            Integer substract           = weekAmount - betweenHbWeekAmount;
            Integer weeksubstract       = weekAmount - betweenTbWeekAmount;
            mchMap.put("total", weekAmount);
            if (substract == 0)
            {
                mchMap.put("hbrate", "0%");
            }
            else
            {
                mchMap.put("hbrate", betweenHbWeekAmount == 0 ? "100%" : dF.format(Math.abs(substract.floatValue()) / betweenHbWeekAmount.floatValue() * 100) + "%");

            }
            if (weeksubstract == 0)
            {
                mchMap.put("tbrate", "0%");
            }
            else
            {
                mchMap.put("tbrate", betweenTbWeekAmount == 0 ? "100%" : dF.format(Math.abs(weeksubstract.floatValue()) / betweenTbWeekAmount.floatValue() * 100) + "%");

            }
            mchMap.put("tbflag", weeksubstract >= 0 ? "up" : "down");
            mchMap.put("hbflag", substract >= 0 ? "up" : "down");
            //开通店铺汇总
            resultMap.put("mchData", mchMap);

            Integer supplier_weekAmount          = supplierModelMapper.getWeekAmount(storeid, Monday, today);
            Integer supplier_betweenHbWeekAmount = supplierModelMapper.getBetweenWeekAmount(storeid, LastMonday, Monday);
            Integer supplier_betweenTbWeekAmount = supplierModelMapper.getBetweenWeekAmount(storeid, lastMonthWeek, month);
            Integer supplier_substract           = supplier_weekAmount - supplier_betweenHbWeekAmount;
            Integer supplier_weeksubstract       = supplier_weekAmount - supplier_betweenTbWeekAmount;
            if (supplier_substract == 0)
            {
                supplierMap.put("hbrate", "0%");
            }
            else
            {
                supplierMap.put("hbrate", supplier_betweenHbWeekAmount == 0 ? "100%" : dF.format((float) Math.abs(supplier_substract) / supplier_betweenHbWeekAmount.floatValue() * 100) + "%");

            }
            if (supplier_weeksubstract == 0)
            {
                supplierMap.put("tbrate", "0%");
            }
            else
            {
                supplierMap.put("tbrate", supplier_betweenTbWeekAmount == 0 ? "100%" : dF.format((float) Math.abs(supplier_weeksubstract) / supplier_betweenTbWeekAmount.floatValue() * 100) + "%");
            }
            supplierMap.put("tbflag", supplier_weeksubstract >= 0 ? "up" : "down");
            supplierMap.put("hbflag", supplier_substract >= 0 ? "up" : "down");
            supplierMap.put("total", supplier_weekAmount);
            //入驻供应商
            resultMap.put("supplierCountData", supplierMap);

            Map<String, Object>       topGoodsData = new HashMap<>();
            List<Map<String, Object>> topGoods     = productListModelMapper.getTopGoods(storeid);
            Integer                   total        = productListModelMapper.getAllVolume(storeid);
            topGoodsData.put("total", total);
            topGoodsData.put("topGoods", topGoods);
            //商品销量排行
            resultMap.put("topGoodsData", topGoodsData);

            List<Map<String, Object>> weekMoney = orderModelMapper.getChargeMoney(storeid, week);
            List<String>              name      = weekMoney.stream().map(e -> MapUtils.getString(e, "name")).collect(Collectors.toList());
            List<Double>              amount    = weekMoney.stream().map(e -> MapUtils.getDouble(e, "amount")).collect(Collectors.toList());
            List<List>                weekData  = new ArrayList<>();
            Map<String, Object>       chargeMap = new HashMap<>();
            weekData.add(name);
            weekData.add(amount);
            chargeMap.put("weekData", weekData);
            chargeMap.put("weekDataList", weekMoney);
            List<List>                monthData   = new ArrayList<>();
            List<Map<String, Object>> montheMoney = orderModelMapper.getChargeMoney(storeid, month);
            List<String>              name1       = montheMoney.stream().map(e -> MapUtils.getString(e, "name")).collect(Collectors.toList());
            List<Double>              amount1     = montheMoney.stream().map(e -> MapUtils.getDouble(e, "amount")).collect(Collectors.toList());
            monthData.add(name1);
            monthData.add(amount1);
            chargeMap.put("monthData", monthData);
            chargeMap.put("monthDataList", montheMoney);
            List<List>                yearData  = new ArrayList<>();
            List<Map<String, Object>> yearMoney = orderModelMapper.getChargeMoney(storeid, year);
            List<String>              name2     = yearMoney.stream().map(e -> MapUtils.getString(e, "name")).collect(Collectors.toList());
            List<Double>              amount2   = yearMoney.stream().map(e -> MapUtils.getDouble(e, "amount")).collect(Collectors.toList());
            yearData.add(name2);
            yearData.add(amount2);
            chargeMap.put("yearData", yearData);
            chargeMap.put("yearDataList", yearMoney);
            List<List>                allData  = new ArrayList<>();
            List<Map<String, Object>> allMoney = orderModelMapper.getChargeMoney(storeid, null);
            List<String>              name3    = allMoney.stream().map(e -> MapUtils.getString(e, "name")).collect(Collectors.toList());
            List<Double>              amount3  = allMoney.stream().map(e -> MapUtils.getDouble(e, "amount")).collect(Collectors.toList());
            allData.add(name3);
            allData.add(amount3);
            chargeMap.put("allData", allData);
            chargeMap.put("allDataList", allMoney);
            //店铺结算金额统计
            resultMap.put("chargeMoneyData", chargeMap);


            List<List> weekSupplierData  = new ArrayList<>();
            List<List> monthSupplierData = new ArrayList<>();
            List<List> yearSupplierData  = new ArrayList<>();
            List<List> allSupplierData   = new ArrayList<>();

            List<Map<String, Object>> weekSupplier  = productListModelMapper.getTopSupplier(storeid, week);
            List<Map<String, Object>> monthSupplier = productListModelMapper.getTopSupplier(storeid, month);
            List<Map<String, Object>> yearSupplier  = productListModelMapper.getTopSupplier(storeid, year);
            List<Map<String, Object>> allSupplier   = productListModelMapper.getTopSupplier(storeid, null);

            List<String>  supplierName = weekSupplier.stream().map(e -> MapUtils.getString(e, "supplier_name")).collect(Collectors.toList());
            List<Integer> num          = weekSupplier.stream().map(e -> MapUtils.getInteger(e, "num")).collect(Collectors.toList());
            weekSupplierData.add(supplierName);
            weekSupplierData.add(num);


            List<Integer> num1          = monthSupplier.stream().map(e -> MapUtils.getInteger(e, "num")).collect(Collectors.toList());
            List<String>  supplierName1 = monthSupplier.stream().map(e -> MapUtils.getString(e, "supplier_name")).collect(Collectors.toList());
            monthSupplierData.add(supplierName1);
            monthSupplierData.add(num1);

            List<Integer> num2          = yearSupplier.stream().map(e -> MapUtils.getInteger(e, "num")).collect(Collectors.toList());
            List<String>  supplierName2 = yearSupplier.stream().map(e -> MapUtils.getString(e, "supplier_name")).collect(Collectors.toList());
            yearSupplierData.add(supplierName2);
            yearSupplierData.add(num2);

            List<Integer> num3          = allSupplier.stream().map(e -> MapUtils.getInteger(e, "num")).collect(Collectors.toList());
            List<String>  supplierName3 = allSupplier.stream().map(e -> MapUtils.getString(e, "supplier_name")).collect(Collectors.toList());
            allSupplierData.add(supplierName3);
            allSupplierData.add(num3);

            Map<String, Object> supplierDataMap = new HashMap<>();
            supplierDataMap.put("weekSupplierData", weekSupplierData);
            supplierDataMap.put("weekSupplierDataList", weekSupplier);
            supplierDataMap.put("monthSupplierData", monthSupplierData);
            supplierDataMap.put("monthSupplierDataList", monthSupplier);
            supplierDataMap.put("yearSupplierData", yearSupplierData);
            supplierDataMap.put("yearSupplierDataList", yearSupplier);
            supplierDataMap.put("allSupplierData", allSupplierData);
            supplierDataMap.put("allSupplierDataList", allSupplier);
            //供应商排行
            resultMap.put("supplierData", supplierDataMap);
            //提现手续费统计
            resultMap.put("moneyInfo", getMoneyInfo(storeid));
            return resultMap;
        }
        catch (Exception e)
        {
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_, "网络异常");
        }
    }

    /**
     * 查询商品总数
     *
     * @param storeid
     * @return Map<String, Integer>
     */
    public Map<String, Integer> getGoodsNum(Integer storeid)
    {
        Map<String, Integer> goodsMap = new HashMap<>();
        //在售商品
        Integer onSaledAmount = productListModelMapper.getOnSaledAmount(null, storeid);
        //下架商品
        Integer underSaledAmount = productListModelMapper.getUnderSaledAmount(null, storeid);
        //商品总数
        Integer allGoodsAmount = productListModelMapper.getallGoodsNum(null, storeid);
        //预警商品
        HashMap<String, Object> parmaMap = new HashMap<>();
        parmaMap.put("store_id", storeid);
        parmaMap.put("stockType", 2);
        parmaMap.put("group_attr_id", "group_attr_id");
        Integer warnenAnmun = stockModelMapper.goodsStockInfoCountDynamic(parmaMap);//productListModelMapper.getWarnedAmount(storeid);
        goodsMap.put("onSaledAmount", onSaledAmount);
        goodsMap.put("underSaledAmount", underSaledAmount);
        goodsMap.put("allGoodsAmount", allGoodsAmount);
        goodsMap.put("warnenAnmun", warnenAnmun);
        return goodsMap;
    }

    /**
     * 提现手续费统计
     *
     * @param storeid
     * @return
     */
    public List<List> getMoneyInfo(int storeid)
    {
        try
        {
            Date       currentDate = DateUtil.dateFormateToDate(new Date(), GloabConst.TimePattern.YMD);
            List<Date> dateList    = new ArrayList<>();
            dateList.add(DateUtil.getAddDate(currentDate, -7));
            dateList.add(DateUtil.getAddDateByMonth(currentDate, -1));
            dateList.add(DateUtil.getAddDateByYear(currentDate, -1));
            List<List> resultList = new ArrayList<>();
            for (int i = 0; i < dateList.size(); i++)
            {
                List<String>     daystr    = new ArrayList<>();
                List<List>       dataList  = new ArrayList<>();
                List<BigDecimal> moneyList = new ArrayList<>();
                if (i < 2)
                {
                    List<Date> days = DateUtil.createDays(dateList.get(i));
                    for (Date day : days)
                    {
                        String paramday = DateUtil.dateFormate(day, GloabConst.TimePattern.YMD);
                        daystr.add(paramday);
                        BigDecimal money = withdrawModelMapper.getTotallMoney(storeid, paramday, i);
                        moneyList.add(money);
                    }
                    dataList.add(daystr);
                    dataList.add(moneyList);

                }
                else
                {
                    List<Date> months = DateUtil.createMonths(dateList.get(i));

                    for (Date month : months)
                    {
                        String paramday = DateUtil.dateFormate(month, GloabConst.TimePattern.YM);
                        daystr.add(paramday);
                        BigDecimal money = withdrawModelMapper.getTotallMoney(storeid, paramday, i);
                        moneyList.add(money);
                    }
                    dataList.add(daystr);
                    dataList.add(moneyList);
                }
                resultList.add(dataList);
            }

            return resultList;
        }
        catch (Exception e)
        {

            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_, "网络异常！", "getMoneyInfo");
        }

    }

    //上周一
    public static Date geLastWeekMonday(Date date)
    {
        Calendar cal = Calendar.getInstance();
        cal.setTime(getThisWeekMonday(date));
        cal.add(Calendar.DATE, -7);
        return cal.getTime();
    }

    //本周一
    public static Date getThisWeekMonday(Date date)
    {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        // 获得当前日期是一个星期的第几天
        int dayWeek = cal.get(Calendar.DAY_OF_WEEK);
        if (1 == dayWeek)
        {
            cal.add(Calendar.DAY_OF_MONTH, -1);
        }
        // 设置一个星期的第一天，按中国的习惯一个星期的第一天是星期一
        cal.setFirstDayOfWeek(Calendar.MONDAY);
        // 获得当前日期是一个星期的第几天
        int day = cal.get(Calendar.DAY_OF_WEEK);
        // 根据日历的规则，给当前日期减去星期几与一个星期第一天的差值
        cal.add(Calendar.DATE, cal.getFirstDayOfWeek() - day);
        return cal.getTime();
    }

    //下周一
    public static Date getNextWeekMonday(Date date)
    {
        Calendar cal = Calendar.getInstance();
        cal.setTime(getThisWeekMonday(date));
        cal.add(Calendar.DATE, 7);
        return cal.getTime();
    }

    public static void main(String[] args)
    {
        //当前时间yyyy-mm-dd
        Date currentDate = DateUtil.dateFormateToDate(new Date(), GloabConst.TimePattern.YMD);
        //当前日
        String today = DateUtil.dateFormate(currentDate, GloabConst.TimePattern.YMD);
        //上一个月
        String month    = DateUtil.dateFormate(DateUtil.getAddDateByMonth(currentDate, -1), GloabConst.TimePattern.YMD);
        String monthday = DateUtil.dateFormate(currentDate, GloabConst.TimePattern.YM);        //减一星期
        String week     = DateUtil.dateFormate(DateUtil.getAddDate(currentDate, -7), GloabConst.TimePattern.YMD);
        //减一年
        String year = DateUtil.dateFormate(DateUtil.getAddDateByYear(currentDate, -1), GloabConst.TimePattern.YMD);
        //减两星期
        String lastWeek = DateUtil.dateFormate(DateUtil.getAddDate(currentDate, -14), GloabConst.TimePattern.YMD);
        //上一个月当前时间减一个星期
        String lastMonthWeek = DateUtil.dateFormate(DateUtil.getAddDate(DateUtil.dateFormateToDate(month, GloabConst.TimePattern.YMD), -7), GloabConst.TimePattern.YMD);
        //减一天
        String yesterday = DateUtil.dateFormate(DateUtil.getAddDate(currentDate, -1), GloabConst.TimePattern.YMD);
        //本周一
        String Monday = DateUtil.dateFormate(getThisWeekMonday(currentDate), GloabConst.TimePattern.YMD);
        //上周一
        String LastMonday = DateUtil.dateFormate(geLastWeekMonday(currentDate), GloabConst.TimePattern.YMD);

        System.out.println(yesterday);
        System.out.println(monthday);
    }


}
