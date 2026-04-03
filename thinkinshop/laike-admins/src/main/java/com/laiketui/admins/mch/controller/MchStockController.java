package com.laiketui.admins.mch.controller;

import com.laiketui.admins.api.mch.MchStockService;
import com.laiketui.core.domain.Result;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.product.StockModel;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.goods.AddStockVo;
import com.laiketui.root.annotation.HttpApiMethod;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 库存管理
 *
 * @author Trick
 * @date 2021/6/2 11:38
 */
@Api(tags = "库存管理")
@RestController
@RequestMapping("/admin/mch/stock/")
public class MchStockController
{

    @Autowired
    private MchStockService mchStockService;

    @ApiOperation("商品库存列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "goodsName", value = "商品名称", dataType = "string", paramType = "form")
    })
    @PostMapping("/index")
    @HttpApiMethod(apiKey = "admin.mch.stock.index")
    public Result index(MainVo vo, String goodsName)
    {
        try
        {
            return Result.success(mchStockService.index(vo, goodsName));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("添加库存")
    @PostMapping("/addStock")
    @HttpApiMethod(apiKey = "admin.mch.stock.addStock")
    public Result addStock(AddStockVo vo)
    {
        try
        {
            return Result.success(mchStockService.addStock(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("获取库存信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "规格id", dataType = "int", required = true, paramType = "form")
    })
    @PostMapping("/getStockInfoById")
    @HttpApiMethod(apiKey = "admin.mch.stock.getStockInfoById")
    public Result getStockInfoById(MainVo vo, int id)
    {
        try
        {
            return Result.success(mchStockService.getStockInfoById(vo, id));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("获取库存详情")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "规格id", dataType = "int", required = true, paramType = "form"),
            @ApiImplicitParam(name = "pid", value = "商品id", dataType = "int", required = true, paramType = "form"),
    })
    @PostMapping("/getStockDetail")
    @HttpApiMethod(apiKey = "admin.mch.stock.getStockDetail")
    public Result getStockDetail(MainVo vo, int id, int pid)
    {
        try
        {
            return Result.success(mchStockService.getStockDetail(vo, id, pid));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("获取库存 入库信息")
    @PostMapping("/getStockEnterInfo")
    @HttpApiMethod(apiKey = "admin.mch.stock.getStockEnterInfo")
    public Result getStockEnterInfo(MainVo vo, String goodsName, String startDate, String endDate)
    {
        try
        {
            return Result.success(mchStockService.getStockInOutInfo(vo, goodsName, startDate, endDate, StockModel.StockType.STOCKTYPE_WAREHOUSING));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("获取库存 出库信息")
    @PostMapping("/getStockShipmentInfo")
    @HttpApiMethod(apiKey = "admin.mch.stock.getStockShipmentInfo")
    public Result getStockShipmentInfo(MainVo vo, String goodsName, String startDate, String endDate)
    {
        try
        {
            return Result.success(mchStockService.getStockInOutInfo(vo, goodsName, startDate, endDate, StockModel.StockType.AGREEMENTTYPE_WAREHOUSING_OUT));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("获取库存【预警】信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "goodsId", value = "商品id", dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "goodsName", value = "商品名称", dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "startDate", value = "开始时间", dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "endDate", value = "结束时间", dataType = "string", paramType = "form"),
    })
    @PostMapping("/getStockWarningInfo")
    @HttpApiMethod(apiKey = "admin.mch.stock.getStockWarningInfo")
    public Result getStockWarningInfo(MainVo vo, Integer goodsId, String goodsName, String startDate, String endDate)
    {
        try
        {
            return Result.success(mchStockService.getStockWarningInfo(vo, goodsId, goodsName, startDate, endDate));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

}
