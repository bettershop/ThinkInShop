package com.laiketui.admins.admin.controller.report;


import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.laiketui.admins.api.admin.report.AdminOrderReportService;
import com.laiketui.common.api.PublicOrderService;
import com.laiketui.core.domain.Result;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.root.annotation.HttpApiMethod;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Api(tags = "后台报表-订单数据")
@RestController
@RequestMapping("/admin/report/order")
public class AdminOrderReportController
{

    @Autowired
    private AdminOrderReportService adminOrderReportService;

    @Autowired
    private PublicOrderService publicOrderService;

    @ApiOperation("订单统计数据")
    @PostMapping("/orderReport")
    @HttpApiMethod(apiKey = "admin.report.orderIndex")
    public Result index(MainVo vo)
    {
        Map<String, Object> resultMap = new HashMap<>();
        try
        {
            List<JSONArray> parseData = new ArrayList<>();
            for (int i = 0; i < 5; i++)
            {
                Map<String, Object> dataMap = publicOrderService.getOrderDataByStatus(vo.getStoreId(), i);
                JSONArray           temp    = JSON.parseArray(MapUtils.getString(dataMap, "data"));
                parseData.add(temp);
            }
            //订单报表顶部
            resultMap.put("orderStatus", parseData);

            //默认值
            JSONObject data2 = new JSONObject();
            data2.put("add_rate", "0%");
            data2.put("all_num", 0);
            data2.put("flag", "up");
            data2.put("today_num", 0);
            data2.put("yesterday_num", 0);
            JSONArray data2List = new JSONArray();
            data2List.add(data2);
            data2List.add(data2);
            data2List.add(data2);
            //实时订单数 + 订单金额 + 退款金额
            List<Map<String, Object>> orderAmount = publicOrderService.getReportData(vo.getStoreId());
            resultMap.put("orderAmount", orderAmount == null ? data2List : orderAmount);

            //无数据库默认值
            JSONObject         empty3Data = new JSONObject();
            ArrayList<Integer> data3      = new ArrayList<>();
            data3.add(0);
            data3.add(0);
            data3.add(0);
            empty3Data.put("week", data3);
            empty3Data.put("month", data3);
            empty3Data.put("year", data3);

            //订单总数统计
            Object orderData = adminOrderReportService.getTotalAmount(vo);
            if (orderData != null)
            {
                resultMap.put("totalAmount", orderData);
            }
            else
            {
                resultMap.put("totalAmount", empty3Data);
            }
            //付款 + 退款订单统计
            Object refundOrderData = adminOrderReportService.getRefundData(vo);
            if (refundOrderData != null)
            {
                resultMap.put("refundOrderData", refundOrderData);
            }
            else
            {
                resultMap.put("refundOrderData", empty3Data);
            }
            return Result.success(resultMap);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
        catch (Exception e)
        {
            return Result.error(ErrorCode.BizErrorCode.ERROR_CODE_, "网络异常！");
        }


    }


}
