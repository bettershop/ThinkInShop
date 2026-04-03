package com.laiketui.apps.mch.controller;


import com.laiketui.apps.api.mch.AppsMchExpressService;
import com.laiketui.core.domain.Result;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.core.lktconst.gwconst.LaiKeGWConst;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.config.AddExpressSubtableVo;
import com.laiketui.domain.vo.config.GetExpressSubtableListVo;
import com.laiketui.domain.vo.order.GetExpressDeliveryListVo;
import com.laiketui.root.annotation.HttpApiMethod;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

/**
 * 物流管理
 */
@Api(tags = "物流管理")
@RestController
@RequestMapping("/app/mch/express/")
public class AppsMchExpressController
{

    @Autowired
    private AppsMchExpressService appsMchExpressService;

    @ApiOperation("添加修改快递公司子表")
    @PostMapping("/add_logistics")
    @HttpApiMethod(apiKey = "mch.App.Mch.add_logistics")
    public Result add_logistics(AddExpressSubtableVo vo)
    {
        try
        {
            appsMchExpressService.addAndUpdateExpressSubtable(vo);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("获取快递公司子表列表")
    @PostMapping("/logistics_list")
    @HttpApiMethod(apiKey = "mch.App.Mch.logistics_list")
    public Result logistics_list(GetExpressSubtableListVo vo)
    {
        try
        {
            return Result.success(appsMchExpressService.getExpressSubtableList(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("获取快递公司子表详情")
    @PostMapping("/edit_logistics_page")
    @HttpApiMethod(apiKey = "mch.App.Mch.edit_logistics_page")
    public Result edit_logistics_page(MainVo vo, Integer id)
    {
        try
        {
            return Result.success(appsMchExpressService.getExpressSubtableById(vo, id));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("删除快递公司子表详情")
    @PostMapping("/del_logistics")
    @HttpApiMethod(apiKey = "mch.App.Mch.del_logistics")
    public Result del_logistics(MainVo vo, Integer id)
    {
        try
        {
            appsMchExpressService.delExpressSubtableById(vo, id);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("获取所有物流公司信息")
    @PostMapping("/get_logistics")
    @HttpApiMethod(apiKey = "mch.App.Mch.get_logistics")
    public Result get_logistics(MainVo vo, Integer id)
    {
        try
        {
            return Result.success(appsMchExpressService.getExpressInfo(vo, id));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("获取发货物流公司信息")
    @PostMapping("/GetLogistics")
    @HttpApiMethod(apiKey = "mch.App.Mch.GetLogistics")
    public Result GetLogistics(MainVo vo, String sNo)
    {
        try
        {
            return Result.success(appsMchExpressService.GetLogistics(vo, sNo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("获取订单发货列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "sNo", value = "订单号", dataType = "String", paramType = "form")
    })
    @PostMapping("/ShippingRecords")
    @HttpApiMethod(apiKey = "mch.App.Mch.ShippingRecords")
    public Result ShippingRecords(GetExpressDeliveryListVo vo)
    {
        try
        {
            return Result.success(appsMchExpressService.ShippingRecords(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("查看发货记录商品")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "发货记录id", dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "name", value = "商品名称", dataType = "string", paramType = "form")
    })
    @PostMapping("/expressGetPro")
    @HttpApiMethod(apiKey = "mch.App.Mch.getPro")
    public Result expressGetPro(MainVo vo, Integer id, String name)
    {
        try
        {
            return Result.success(appsMchExpressService.expressGetPro(vo, id, name));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("取消电子面单发货")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "发货记录id", dataType = "int", paramType = "form")
    })
    @PostMapping("/CancelElectronicWaybill")
    @HttpApiMethod(apiKey = "mch.App.Mch.CancelElectronicWaybill")
    public Result CancelElectronicWaybill(MainVo vo, Integer id)
    {
        try
        {
            String code = LaiKeGWConst.GW_SUCCESS;
            String msg = appsMchExpressService.CancelElectronicWaybill(vo, id);
            if (!Objects.equals(msg,"操作成功"))
            {
                code = "0000000";
            }
            return Result.success(code,msg);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("电子面单发货")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "express_id", value = "快递公司", required = true, dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "orderList_id", value = "订单明细id集", required = true, dataType = "string", paramType = "form")
    })
    @PostMapping("/FaceSheetSend")
    @HttpApiMethod(apiKey = "mch.App.Mch.FaceSheetSend")
    public Result FaceSheetSend(MainVo vo, Integer express_id, String orderList_id)
    {
        try
        {
            appsMchExpressService.FaceSheetSend(vo, express_id, orderList_id);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }
}
