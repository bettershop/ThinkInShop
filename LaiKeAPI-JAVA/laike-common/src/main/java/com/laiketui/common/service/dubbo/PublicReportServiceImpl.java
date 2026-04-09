package com.laiketui.common.service.dubbo;

import com.laiketui.common.api.PublicReportService;
import com.laiketui.common.mapper.CustomerModelMapper;
import com.laiketui.common.mapper.OrderModelMapper;
import com.laiketui.common.mapper.ProductListModelMapper;
import com.laiketui.common.mapper.ReturnOrderModelMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.core.utils.tool.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class PublicReportServiceImpl implements PublicReportService
{

    @Autowired
    private CustomerModelMapper customerModelMapper;
    @Autowired
    private OrderModelMapper    orderModelMapper;

    @Autowired
    private ReturnOrderModelMapper returnOrderModelMapper;

    @Autowired
    private ProductListModelMapper productListModelMapper;


    public List<List> getGoodsNumList(int storeid)
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
                List<List> temp = new ArrayList<>();
                if (j < 2)
                {
                    List<Date>   days       = DateUtil.createDays(dateList.get(j));
                    List<String> daystr     = new ArrayList<>();
                    List<Long>   saledNums  = new ArrayList<>();
                    List<Long>   returnNums = new ArrayList<>();
                    List<Long>   upperNums  = new ArrayList<>();
                    for (Date day : days)
                    {
                        String paramday = DateUtil.dateFormate(day, GloabConst.TimePattern.YMD);
                        daystr.add(paramday);
                        saledNums.add(getSaledGoodsNum(storeid, i, paramday, storeMchId));
                        returnNums.add(getReturnGoodsNum(storeid, i, paramday, storeMchId, j));
                        upperNums.add(getUpperedGoodsNum(storeid, i, paramday, storeMchId, j));

                    }
                    temp.add(daystr);
                    temp.add(saledNums);
                    temp.add(returnNums);
                    temp.add(upperNums);

                }
                else
                {
                    List<Date>   months     = DateUtil.createMonths(dateList.get(j));
                    List<String> daystr     = new ArrayList<>();
                    List<Long>   saledNums  = new ArrayList<>();
                    List<Long>   returnNums = new ArrayList<>();
                    List<Long>   upperNums  = new ArrayList<>();
                    for (Date month : months)
                    {
                        String paramday = DateUtil.dateFormate(month, GloabConst.TimePattern.YM);
                        daystr.add(paramday);
                        saledNums.add(getSaledGoodsNum(storeid, i, paramday, storeMchId));
                        returnNums.add(getReturnGoodsNum(storeid, i, paramday, storeMchId, j));
                        upperNums.add(getUpperedGoodsNum(storeid, i, paramday, storeMchId, j));
                    }
                    temp.add(daystr);
                    temp.add(saledNums);
                    temp.add(returnNums);
                    temp.add(upperNums);
                }
                dataList.add(temp);
            }

            resultList.add(dataList);
        }

        return resultList;
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
     * 统计上架商品数量
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
