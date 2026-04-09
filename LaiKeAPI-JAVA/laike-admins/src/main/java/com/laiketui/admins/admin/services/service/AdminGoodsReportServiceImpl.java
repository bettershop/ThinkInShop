package com.laiketui.admins.admin.services.service;

import com.laiketui.admins.api.admin.report.AdminGoodsReportService;
import com.laiketui.common.api.PubliceService;
import com.laiketui.common.mapper.*;
import com.laiketui.common.utils.tool.data.GoodsDataUtils;
import com.laiketui.core.common.SplitUtils;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.DictionaryConst;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.core.utils.tool.DateUtil;
import com.laiketui.core.utils.tool.StringUtils;
import com.laiketui.domain.product.ProductClassModel;
import com.laiketui.domain.vo.MainVo;
import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class AdminGoodsReportServiceImpl implements AdminGoodsReportService
{
    @Autowired
    private MchModelMapper mchModelMapper;

    @Autowired
    private ReturnOrderModelMapper  returnOrderModelMapper;
    @Autowired
    private OrderModelMapper        orderModelMapper;
    @Autowired
    private StockModelMapper        stockModelMapper;
    @Autowired
    private OrderDetailsModelMapper orderDetailsModelMapper;
    @Autowired
    private ProductListModelMapper  productListModelMapper;
    @Autowired
    private ConfiGureModelMapper    confiGureModelMapper;
    @Autowired
    private BrandClassModelMapper   brandClassModelMapper;

    @Autowired
    private ProductClassModelMapper productClassModelMapper;
    @Autowired
    private CustomerModelMapper     customerModelMapper;
    @Autowired
    PubliceService publiceService;

    /**
     * 商品报表数据量的统计
     *
     * @param storeid
     * @return
     * @throws LaiKeAPIException
     */
    @Override
    public Map<String, Object> getCountList(int storeid) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>();
        try
        {
            //对接商品数
            Integer             userAmount = mchModelMapper.countUserAmount(storeid);
            Integer             mchAmount  = mchModelMapper.countAll(storeid);
            Map<String, Object> classMap   = new HashMap<>();
            classMap.put("store_id", storeid);
            classMap.put("recycle", 0);
            Integer productClassAmount = productClassModelMapper.countDynamic(classMap);
            Integer brandAmount        = brandClassModelMapper.countAll(storeid);

            resultMap.put("userAmount", userAmount);
            resultMap.put("mchAmount", mchAmount);
            resultMap.put("productClassAmount", productClassAmount);
            resultMap.put("brandAmount", brandAmount);

            return resultMap;
        }
        catch (Exception e)
        {
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_, "网络异常！", "getCountList");
        }

    }

    /**
     * 商品报表库存及预警的统计
     *
     * @param vo
     * @param type
     * @return
     * @throws LaiKeAPIException
     */
    @Override
    public Map<String, Object> getStockRecord(MainVo vo, String type) throws LaiKeAPIException
    {
        try
        {
            Date                currentDate = new Date();
            Map<String, Object> resultMap   = new HashMap<>();
            String              dateStr     = "";
            switch (type)
            {
                case "week":
                    dateStr = DateUtil.dateFormate(DateUtil.getAddDate(currentDate, -7), GloabConst.TimePattern.YMD);
                    break;
                case "month":
                    dateStr = DateUtil.dateFormate(DateUtil.getAddDateByMonth(currentDate, -1), GloabConst.TimePattern.YMD);
                    break;
                case "year":
                    dateStr = DateUtil.dateFormate(DateUtil.getAddDateByYear(currentDate, -1), GloabConst.TimePattern.YMD);
                    break;
            }
            resultMap.put("total", confiGureModelMapper.countWarnedStock(vo.getStoreId(), dateStr));
            Map<String, Object> param = new HashMap<>();
            param.put("storeId", vo.getStoreId());
            param.put("date", dateStr);

            param.put("pageStart", vo.getPageNo());
            param.put("pageEnd", vo.getPageSize());
            List<Map<String, Object>> warnedGoodsInfo = confiGureModelMapper.getWarnedGoodsInfo(param);
            int                       sort            = 0;
            for (Map<String, Object> data : warnedGoodsInfo)
            {
                String img       = MapUtils.getString(data, "img");
                String attribute = GoodsDataUtils.getProductSkuValue(MapUtils.getString(data, "attribute"));
                String warnDate  = MapUtils.getString(data, "warnDate");

                data.put("img", publiceService.getImgPath(img, vo.getStoreId()));
                data.put("sort", sort++);
                data.put("config", attribute);
                data.put("warnDate", DateUtil.dateFormate(warnDate, GloabConst.TimePattern.YMDHMS));
            }
            resultMap.put("data", warnedGoodsInfo);
            return resultMap;
        }
        catch (Exception e)
        {
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_, "网络异常！", "getStockRecord");
        }

    }


    public List<List> getGoodsSalesInfo(int storeid) throws LaiKeAPIException
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

            List<List> resultList = new ArrayList<>();
            for (int i = 0; i < dateList.size(); i++)
            {
                List<List> dataList = new ArrayList<>();
                if (i < 2)
                {
                    List<Date>    days       = DateUtil.createDays(dateList.get(i));
                    List<String>  daystr     = new ArrayList<>();
                    List<Integer> mchnums    = new ArrayList<>();
                    List<Integer> notmchnums = new ArrayList<>();
                    for (Date day : days)
                    {
                        String paramday = DateUtil.dateFormate(day, GloabConst.TimePattern.YMD);
                        daystr.add(paramday);
                        Integer storeMchId       = customerModelMapper.getStoreMchId(storeid);
                        Integer goodsMchSales    = orderDetailsModelMapper.getGoodsSales(storeid, storeMchId, paramday, Boolean.TRUE, "day");
                        Integer goodsNotMchSales = orderDetailsModelMapper.getGoodsSales(storeid, storeMchId, paramday, Boolean.FALSE, "day");
                        mchnums.add(goodsMchSales);
                        notmchnums.add(goodsNotMchSales);
                    }
                    dataList.add(daystr);
                    dataList.add(mchnums);
                    dataList.add(notmchnums);

                }
                else
                {
                    List<Date>    months     = DateUtil.createMonths(dateList.get(i));
                    List<String>  daystr     = new ArrayList<>();
                    List<Integer> mchnums    = new ArrayList<>();
                    List<Integer> notmchnums = new ArrayList<>();
                    for (Date month : months)
                    {
                        String paramday = DateUtil.dateFormate(month, GloabConst.TimePattern.YM);
                        daystr.add(paramday);
                        Integer storeMchId       = customerModelMapper.getStoreMchId(storeid);
                        Integer goodsMchSales    = orderDetailsModelMapper.getGoodsSales(storeid, storeMchId, paramday, Boolean.TRUE, "month");
                        Integer goodsNotMchSales = orderDetailsModelMapper.getGoodsSales(storeid, storeMchId, paramday, Boolean.FALSE, "month");
                        mchnums.add(goodsMchSales);
                        notmchnums.add(goodsNotMchSales);
                    }
                    dataList.add(daystr);
                    dataList.add(mchnums);
                    dataList.add(notmchnums);
                }
                resultList.add(dataList);
            }
            return resultList;
        }
        catch (Exception e)
        {
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_, "网络异常", "getGoodsSalesInfo");
        }
    }

    public List<Long> getGoodsStockInfo(int storeid, String type) throws LaiKeAPIException
    {
        try
        {
            Date   currentDate = new Date();
            String dateStr     = "";
            switch (type)
            {
                case "week":
                    dateStr = DateUtil.dateFormate(DateUtil.getAddDate(currentDate, -7), GloabConst.TimePattern.YMD);
                    break;
                case "month":
                    dateStr = DateUtil.dateFormate(DateUtil.getAddDateByMonth(currentDate, -1), GloabConst.TimePattern.YMD);
                    break;
                case "year":
                    dateStr = DateUtil.dateFormate(DateUtil.getAddDateByYear(currentDate, -1), GloabConst.TimePattern.YMD);
                    break;
            }
            List<Long> tempList        = new ArrayList<>();
            Long       inStockAmount   = stockModelMapper.countWithParam(storeid, dateStr, "0");
            Long       outStockAmount  = stockModelMapper.countWithParam(storeid, dateStr, "1");
            Long       warnStockAmount = stockModelMapper.countWarnAmount(storeid, dateStr);

            tempList.add(inStockAmount);
            tempList.add(outStockAmount);
            tempList.add(warnStockAmount);

            return tempList;
        }
        catch (Exception e)
        {
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_, "网络异常！", "getGoodsStockInfo");
        }

    }

    public List<List> getGoodsSalesInfoByStatus(int storeid) throws LaiKeAPIException
    {
        try
        {
            List<List> resultList = new ArrayList<>();
            //获取自营店id
            Integer storeMchId = customerModelMapper.getStoreMchId(storeid);

            List<Map<String, Object>> onsalseList = new ArrayList<>();
            //自营店在售商品
            Map<String, Object> onSalsedMchGoosNum = productListModelMapper.getGoodsAmount1(storeMchId, storeid, DictionaryConst.GoodsStatus.NEW_GROUNDING, Boolean.TRUE);
            //非自营店在售商品
            Map<String, Object> onSalsedNotMchGoosNum = productListModelMapper.getGoodsAmount1(storeMchId, storeid, DictionaryConst.GoodsStatus.NEW_GROUNDING, Boolean.FALSE);
            //供应商在售商品
            Map<String, Object> onSalsedSupplierGoosNum = productListModelMapper.getSupplierGoodsAmount(storeid, DictionaryConst.GoodsStatus.NEW_GROUNDING, null);
            onsalseList.add(onSalsedMchGoosNum);
            onsalseList.add(onSalsedNotMchGoosNum);
            onsalseList.add(onSalsedSupplierGoosNum);

            resultList.add(onsalseList);


            List<Map<String, Object>> undersalseList = new ArrayList<>();
            //已下架
            Map<String, Object> underSalsedMchGoosNum      = productListModelMapper.getGoodsAmount1(storeMchId, storeid, DictionaryConst.GoodsStatus.OFFLINE_GROUNDING, Boolean.TRUE);
            Map<String, Object> underSalsedNotMchGoosNum   = productListModelMapper.getGoodsAmount1(storeMchId, storeid, DictionaryConst.GoodsStatus.OFFLINE_GROUNDING, Boolean.FALSE);
            Map<String, Object> underSalsedSupplierGoosNum = productListModelMapper.getSupplierGoodsAmount(storeid, DictionaryConst.GoodsStatus.VIOLATION, null);
            undersalseList.add(underSalsedMchGoosNum);
            undersalseList.add(underSalsedNotMchGoosNum);
            undersalseList.add(underSalsedSupplierGoosNum);

            resultList.add(undersalseList);

            List<Map<String, Object>> allsalseList = new ArrayList<>();
            //全部
            Map<String, Object> allMchGoodsNum      = productListModelMapper.getGoodsAmount1(storeMchId, storeid, null, Boolean.TRUE);
            Map<String, Object> allNotMchGoodsNum   = productListModelMapper.getGoodsAmount1(storeMchId, storeid, null, Boolean.FALSE);
            List<String>        statusList          = Arrays.asList("1", "2", "3");
            Map<String, Object> allSupplierGoodsNum = productListModelMapper.getSupplierGoodsAmount(storeid, null, statusList);
            allsalseList.add(allMchGoodsNum);
            allsalseList.add(allNotMchGoodsNum);
            allsalseList.add(allSupplierGoodsNum);

            resultList.add(allsalseList);

            return resultList;
        }
        catch (Exception e)
        {
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_, "网络异常", "getGoodsSalesInfoByStatus");
        }
    }

    public List<List> getSkuNumber(int storeid) throws LaiKeAPIException
    {
        try
        {
            List<List> resultList = new ArrayList<>();
            Integer    storeMchId = customerModelMapper.getStoreMchId(storeid);

            //查询自营店所有的分类的sku数量
            List<Map<String, Object>> mchSkuList = productListModelMapper.goodsSkuList(storeid, storeMchId, Boolean.TRUE);
            //非自营店
            List<Map<String, Object>> SkuList = productListModelMapper.goodsSkuList(storeid, storeMchId, Boolean.FALSE);
            //供应商
            List<Map<String, Object>> supplierSkulist = productListModelMapper.supplierGoodsSkuList(storeid);
            //自营店
            Map<String, Integer> allClassAndNumMap = new HashMap<>();
            List<String>         mch_classList     = new ArrayList<>();
            List<Integer>        mch_numList       = new ArrayList<>();
            List<List>           mchData           = new ArrayList<>();
            for (Map<String, Object> data : mchSkuList)
            {
                //商品分类：-111-111-
                String catagery = MapUtils.getString(data, "class");

                if (StringUtils.isNotEmpty(catagery))
                {
                    //分类对应的数量
                    Integer  num         = MapUtils.getInteger(data, "num");
                    String[] categoryStr = catagery.split(SplitUtils.HG);
                    //获取第一个分类，即一级分类
                    String classId = categoryStr.length > 1 ? categoryStr[1] : categoryStr[0];
                    //根据一级分类统计数量
                if (!allClassAndNumMap.containsKey(classId))
                {
                    allClassAndNumMap.put(classId, num);
                }
                else
                {
                    allClassAndNumMap.put(classId, MapUtils.getInteger(allClassAndNumMap, classId) + num);
                }
                    //allClassAndNumMap.merge(classId, num, Integer::sum);
                }
            }

            for (String key : allClassAndNumMap.keySet())
            {
                ProductClassModel productClassModel = productClassModelMapper.selectByPrimaryKey(key);
                if (Objects.nonNull(productClassModel))
                {
                    //一级分类名称
                    mch_classList.add(productClassModel.getPname());
                    //一级分类对应商品数量
                    mch_numList.add(allClassAndNumMap.get(key));
                }
            }
            mchData.add(mch_classList);
            mchData.add(mch_numList);

            //非自营店
            allClassAndNumMap = new HashMap<>();
            List<String>  classList = new ArrayList<>();
            List<Integer> numList   = new ArrayList<>();
            List<List>    otherData = new ArrayList<>();
            for (Map<String, Object> data : SkuList)
            {
                String   catagery    = MapUtils.getString(data, "class");
                Integer  num         = MapUtils.getInteger(data, "num");
                String[] categoryStr = catagery.split(SplitUtils.HG);
                String   classId     = categoryStr.length > 1 ? categoryStr[1] : categoryStr[0];
                if (!allClassAndNumMap.containsKey(classId))
                {
                    allClassAndNumMap.put(classId, num);
                }
                else
                {
                    allClassAndNumMap.put(classId, MapUtils.getInteger(allClassAndNumMap, classId) + num);
                }
            }

            for (String key : allClassAndNumMap.keySet())
            {
                numList.add(allClassAndNumMap.get(key));
                ProductClassModel productClassModel = productClassModelMapper.selectByPrimaryKey(key);
                if (Objects.nonNull(productClassModel))
                {
                    classList.add(productClassModel.getPname());
                }
            }
            otherData.add(classList);
            otherData.add(numList);

            //供应商
            allClassAndNumMap = new HashMap<>();
            List<String>  supplier_classList = new ArrayList<>();
            List<Integer> supplier_numList   = new ArrayList<>();
            List<List>    supplierData       = new ArrayList<>();
            for (Map<String, Object> data : supplierSkulist)
            {
                String   catagery    = MapUtils.getString(data, "class");
                Integer  num         = MapUtils.getInteger(data, "num");
                String[] categoryStr = catagery.split(SplitUtils.HG);
                String   classId     = categoryStr.length > 1 ? categoryStr[1] : categoryStr[0];;
                if (!allClassAndNumMap.containsKey(classId))
                {
                    allClassAndNumMap.put(classId, num);
                }
                else
                {
                    allClassAndNumMap.put(classId, MapUtils.getInteger(allClassAndNumMap, classId) + num);
                }
            }

            for (String key : allClassAndNumMap.keySet())
            {
                supplier_numList.add(allClassAndNumMap.get(key));
                ProductClassModel productClassModel = productClassModelMapper.selectByPrimaryKey(key);
                if (Objects.nonNull(productClassModel))
                {
                    supplier_classList.add(productClassModel.getPname());
                }
            }
            supplierData.add(supplier_classList);
            supplierData.add(supplier_numList);


            resultList.add(mchData);
            resultList.add(otherData);
            resultList.add(supplierData);

            return resultList;
        }
        catch (Exception e)
        {
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_, "网络异常", "getGoodsSalesInfoByStatus");
        }
    }

    /**
     * 商品数量报表统计
     *
     * @param storeid
     * @return
     */
    public List<List> getGoodsNumList(int storeid) throws LaiKeAPIException
    {
        try
        {
            Date       currentDate = DateUtil.dateFormateToDate(new Date(), GloabConst.TimePattern.YMD);
            List<Date> dateList    = new ArrayList<>();
            dateList.add(DateUtil.getAddDate(currentDate, -7));
            dateList.add(DateUtil.getAddDateByMonth(currentDate, -1));
            dateList.add(DateUtil.getAddDateByYear(currentDate, -1));
            Integer    storeMchId = customerModelMapper.getStoreMchId(storeid);
            List<List> resultList = new ArrayList<>();
            for (int j = 0; j < dateList.size(); j++)
            {
                List<List> dataList = new ArrayList<>();
                for (int i = 0; i < 3; i++)
                {
                    List<List>   temp           = new ArrayList<>();
                    List<Long>   saledNums      = new ArrayList<>();
                    List<Long>   returnNums     = new ArrayList<>();
                    List<Long>   upperNums      = new ArrayList<>();
                    List<String> daystr         = new ArrayList<>();
                    List<Long>   undershelfNums = new ArrayList<>();
                    if (j < 2)
                    {
                        List<Date> days = DateUtil.createDays(dateList.get(j));
                        for (Date day : days)
                        {
                            String paramday = DateUtil.dateFormate(day, GloabConst.TimePattern.YMD);
                            daystr.add(paramday);
                            saledNums.add(getSaledGoodsNum(storeid, i, paramday, storeMchId));
                            returnNums.add(getReturnGoodsNum(storeid, i, paramday, storeMchId, j));
                            upperNums.add(getUpperedGoodsNum(storeid, i, paramday, storeMchId, j));
                            undershelfNums.add(getUnderShelfGoodsNum(storeid, i, paramday, storeMchId, j));
                        }
                    }
                    else
                    {
                        List<Date> months = DateUtil.createMonths(dateList.get(j));
                        for (Date month : months)
                        {
                            String paramday = DateUtil.dateFormate(month, GloabConst.TimePattern.YM);
                            daystr.add(paramday);
                            saledNums.add(getSaledGoodsNum(storeid, i, paramday, storeMchId));
                            returnNums.add(getReturnGoodsNum(storeid, i, paramday, storeMchId, j));
                            upperNums.add(getUpperedGoodsNum(storeid, i, paramday, storeMchId, j));
                            undershelfNums.add(getUnderShelfGoodsNum(storeid, i, paramday, storeMchId, j));
                        }
                    }
                    temp.add(daystr);
                    temp.add(saledNums);
                    temp.add(returnNums);
                    temp.add(upperNums);
                    temp.add(undershelfNums);
                    dataList.add(temp);
                }
                resultList.add(dataList);
            }

            return resultList;
        }
        catch (Exception e)
        {
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_, "网络异常", "getGoodsNumList");
        }
    }

    /**
     * 商品指标---已售商品数量
     *
     * @param storeid
     * @param index
     * @param paramday
     * @param mchId
     * @return
     * @throws LaiKeAPIException
     */
    private Long getSaledGoodsNum(int storeid, int index, String paramday, int mchId) throws LaiKeAPIException
    {
        try
        {
            Map<String, Object> param = new HashMap<>();
            param.put("storeid", storeid);
            param.put("index", index);
            param.put("mchId", mchId);
            param.put("date", paramday);
            return orderModelMapper.getSaledGoodsNum(param);
        }
        catch (Exception e)
        {
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_, "网络异常！", "getSaledGoodsNum");
        }
    }

    /**
     * 获取退款商品的数量
     *
     * @param storeid
     * @param type
     * @param paramday
     * @param mchId
     * @return
     * @throws LaiKeAPIException
     */
    private Long getReturnGoodsNum(int storeid, int type, String paramday, int mchId, int regix) throws LaiKeAPIException
    {
        try
        {
            Map<String, Object> param = new HashMap<>();
            param.put("storeid", storeid);
            param.put("type", type);
            param.put("mchId", mchId);
            param.put("date", paramday);
            param.put("regix", regix);
            return returnOrderModelMapper.getReturnGoodsNum(param);
        }
        catch (Exception e)
        {
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_, "网络异常！", "getSaledGoodsNum");
        }
    }

    /**
     * 统计新增商品数量
     *
     * @param storeid
     * @param index
     * @param paramday
     * @param mchId
     * @return
     * @throws LaiKeAPIException
     */
    private Long getUpperedGoodsNum(int storeid, int index, String paramday, int mchId, int regix) throws LaiKeAPIException
    {
        try
        {
            Map<String, Object> param = new HashMap<>();
            param.put("storeid", storeid);
            param.put("type", index);
            param.put("mchId", mchId);
            param.put("date", paramday);
            param.put("regix", regix);
            return productListModelMapper.getUpperGoodsNum(param);
        }
        catch (Exception e)
        {
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_, "网络异常！", "getSaledGoodsNum");
        }
    }

    /**
     * 待上架商品统计
     *
     * @param storeid
     * @param index
     * @param paramday
     * @param mchId
     * @param regix
     * @return
     * @throws LaiKeAPIException
     */
    private Long getUnderShelfGoodsNum(int storeid, int index, String paramday, int mchId, int regix) throws LaiKeAPIException
    {
        try
        {
            Map<String, Object> param = new HashMap<>();
            param.put("storeid", storeid);
            param.put("index", index);
            param.put("mchId", mchId);
            param.put("date", paramday);
            return productListModelMapper.getUpperGoodsNum(param);
        }
        catch (Exception e)
        {
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_, "网络异常！", "getSaledGoodsNum");
        }
    }
}
