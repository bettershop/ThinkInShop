package com.laiketui.apps.mch.controller;

import com.laiketui.apps.api.mch.AppsMchPromiseService;
import com.laiketui.core.domain.Result;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.domain.vo.MainVo;
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
 * 保证金
 *
 * @author Trick
 * @date 2021/10/26 10:40
 */
@Api(tags = "保证金")
@RestController
@RequestMapping("/app/mch/promise")
public class AppsMchPromiseController
{

    @Autowired
    private AppsMchPromiseService appsMchPromiseService;

    @ApiOperation("缴纳保证金页面")
    @PostMapping("/index")
    @HttpApiMethod(apiKey = "mch.App.Promise.Index")
    public Result index(MainVo vo) throws LaiKeAPIException
    {
        try
        {
            return Result.success(appsMchPromiseService.index(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("是否缴纳保证金")
    @PostMapping("/isPromisePay")
    @HttpApiMethod(apiKey = "mch.App.Promise.IsPromisePay")
    public Result isPromisePay(MainVo vo) throws LaiKeAPIException
    {
        try
        {
            return Result.success(appsMchPromiseService.isPromisePay(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("获取支付方式列表")
    @PostMapping("/getPaymentList")
    @HttpApiMethod(apiKey = "mch.promise.getPaymentList")
    public Result payment(MainVo vo) throws LaiKeAPIException
    {
        try
        {
            return Result.success(appsMchPromiseService.getPaymentList(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("支付保证金/第三方下单")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "payType", value = "支付方式 wallet_pay=钱包支付...", dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "pwd", value = "支付密码", dataType = "string", paramType = "form")
    })
    @PostMapping("/payment")
    @HttpApiMethod(apiKey = "mch.App.Promise.Payment")
    public Result payment(MainVo vo, String payType, String pwd) throws LaiKeAPIException
    {
        try
        {
            return Result.success(appsMchPromiseService.payment(vo, payType, pwd));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("保证金管理页面")
    @PostMapping("/promiseManage")
    @HttpApiMethod(apiKey = "mch.App.Promise.PromiseManage")
    public Result promiseManage(MainVo vo) throws LaiKeAPIException
    {
        try
        {
            return Result.success(appsMchPromiseService.promiseManage(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("保证金记录页面")
    @PostMapping("/promiseList")
    @HttpApiMethod(apiKey = "mch.App.Promise.PromiseList")
    public Result promiseList(MainVo vo) throws LaiKeAPIException
    {
        try
        {
            return Result.success(appsMchPromiseService.promiseList(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("退还保证金")
    @PostMapping("/returnPromisePrice")
    @HttpApiMethod(apiKey = "mch.promise.returnPromisePrice")
    public Result returnPromisePrice(MainVo vo) throws LaiKeAPIException
    {
        try
        {
            appsMchPromiseService.returnPromisePrice(vo);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("保证金审核")
    @PostMapping("/insertPromisePrice")
    @HttpApiMethod(apiKey = "mch.App.Promise.InsertPromisePrice")
    public Result insertPromisePrice(MainVo vo) throws LaiKeAPIException
    {
        try
        {
            appsMchPromiseService.insertPromisePrice(vo);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("保证金审核列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "title", value = "用户名称/id", dataType = "string", paramType = "form"),
    })
    @PostMapping("/selectPromisePrice")
    @HttpApiMethod(apiKey = "mch.Admin.Mch.SelectPromisePrice")
    public Result selectPromisePrice(MainVo vo, String title) throws LaiKeAPIException
    {
        try
        {
            return Result.success(appsMchPromiseService.selectPromisePrice(vo, title));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("保证金审核通过或拒绝")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "保证金审核id", dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "isPass", value = "是否通过", dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "refusedWhy", value = "拒绝原因", dataType = "string", paramType = "form"),
    })
    @PostMapping("/passOrRefused")
    @HttpApiMethod(apiKey = "mch.Admin.Mch.PassOrRefused")
    public Result passOrRefused(MainVo vo, int id, int isPass, String refusedWhy) throws LaiKeAPIException
    {
        try
        {
            appsMchPromiseService.passOrRefused(vo, id, isPass, refusedWhy);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("保证金审核删除")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "保证金审核id", dataType = "int", paramType = "form"),
    })
    @PostMapping("/deletePromisePrice")
    @HttpApiMethod(apiKey = "mch.Admin.Mch.DeletePromisePrice")
    public Result deletePromisePrice(MainVo vo, int id) throws LaiKeAPIException
    {
        try
        {
            appsMchPromiseService.deletePromisePrice(vo, id);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

}
