package com.laiketui.admins.admin.controller.order;


import com.laiketui.admins.admin.services.order.AdminConfigServiceImpl;
import com.laiketui.core.domain.Result;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.order.ConfigVo;
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
 * 订单设置
 *
 * @author Trick
 */
@Api(tags = "后台-订单设置")
@RestController
@RequestMapping("/admin/orderSet")
public class AdminOrderSetController
{

    @Autowired
    private AdminConfigServiceImpl adminConfigService;

    @ApiOperation("获取订单设置")
    @PostMapping("/index")
    @HttpApiMethod(apiKey = "admin.orderSet.index")
    public Result index(MainVo vo)
    {
        try
        {
            return Result.success(adminConfigService.configShow(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("订单设置")
    @PostMapping("/saveConfig")
    @HttpApiMethod(apiKey = "admin.orderSet.saveConfig")
    public Result saveConfig(ConfigVo vo)
    {
        try
        {
            adminConfigService.saveConfig(vo);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("店铺获取订单设置")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "mchId", value = "店铺id", required = true, dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "isType", value = "类型", dataType = "int", paramType = "form"),
    })
    @PostMapping("/mchIndex")
    @HttpApiMethod(urlMapping = {"mch.Mch.Order.MchIndex", "mch.App.Mch.MchIndex"})
    public Result mchIndex(MainVo vo, int mchId, int isType)
    {
        try
        {
            return Result.success(adminConfigService.mchConfigShow(vo, mchId, isType));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("店铺订单设置")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "mchId", value = "店铺id", required = true, dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "isType", value = "类型", dataType = "int", paramType = "form"),
    })
    @PostMapping("/mchSaveConfig")
    @HttpApiMethod(urlMapping = {"mch.Mch.Order.MchSaveConfig", "mch.App.Mch.MchSaveConfig"})
    public Result mchSaveConfig(ConfigVo vo, int mchId, int isType)
    {
        try
        {
            adminConfigService.mchSaveConfig(vo, mchId, isType);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

}
