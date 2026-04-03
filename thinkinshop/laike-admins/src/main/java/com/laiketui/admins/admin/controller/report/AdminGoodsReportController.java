package com.laiketui.admins.admin.controller.report;

import com.laiketui.admins.api.admin.report.AdminGoodsReportService;
import com.laiketui.common.api.PublicGoodsService;
import com.laiketui.common.mapper.OrderReportModelMapper;
import com.laiketui.core.domain.Result;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.root.annotation.HttpApiMethod;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Api(tags = "后台报表-商品数据")
@RestController
@RequestMapping("/admin/report/goods")
public class AdminGoodsReportController
{

    @Autowired
    private AdminGoodsReportService adminGoodsReportService;

    @Autowired
    private PublicGoodsService publicGoodsService;

    @Autowired
    private OrderReportModelMapper orderReportModelMapper;

    @ApiOperation("商品统计数据")
    @PostMapping("/goodsReport")
    @HttpApiMethod(apiKey = "admin.report.goodsIndex")
    public Result index(MainVo vo)
    {
        Map<String, Object> resultMap = new HashMap<>();
        try
        {
            //商品相关指标统计
            resultMap.put("countList", adminGoodsReportService.getCountList(vo.getStoreId()));
            //商品状态对应数量
            resultMap.put("goodsSalesInfoWithStatus", adminGoodsReportService.getGoodsSalesInfoByStatus(vo.getStoreId()));
            //商品销量汇总
            resultMap.put("goodsSalesInfo", adminGoodsReportService.getGoodsSalesInfo(vo.getStoreId()));
            //商品规格对应商品数量
            resultMap.put("skuInfo", adminGoodsReportService.getSkuNumber(vo.getStoreId()));
            //商品数量
            resultMap.put("goodsNumInfo", adminGoodsReportService.getGoodsNumList(vo.getStoreId()));

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


    @ApiOperation("库存统计数据")
    @PostMapping("/stockRecord")
    @HttpApiMethod(apiKey = "admin.report.stockRecord")
    public Result index(MainVo vo, String type)
    {
        Map<String, Object> resultMap = new HashMap<>();
        try
        {
            //商品数量--入库，出库，预警
            resultMap.put("stockInfo", adminGoodsReportService.getGoodsStockInfo(vo.getStoreId(), type));

            resultMap.put("stockRecord", adminGoodsReportService.getStockRecord(vo, type));
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
