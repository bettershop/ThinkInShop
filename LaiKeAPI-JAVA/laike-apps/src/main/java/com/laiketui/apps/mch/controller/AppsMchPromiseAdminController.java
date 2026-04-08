package com.laiketui.apps.mch.controller;

import com.laiketui.apps.api.mch.AppsMchPromiseAdminService;
import com.laiketui.core.domain.Result;
import com.laiketui.core.exception.LaiKeAPIException;
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
 * 店铺保证金
 *
 * @author Trick
 * @date 2021/10/25 14:12
 */
@Api(tags = "店铺保证金管理")
@RestController
@RequestMapping("/app/mch/promiseadmin")
public class AppsMchPromiseAdminController
{

    @Autowired
    private AppsMchPromiseAdminService appsMchPromiseAdminService;


    @ApiOperation("保证金列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "keyName", value = "关键字", dataType = "string", paramType = "form"),
    })
    @PostMapping("/index")
    @HttpApiMethod(apiKey = "mch.Admin.Mch.Index")
    public Result index(MainVo vo, String keyName) throws LaiKeAPIException
    {
        try
        {
            return Result.success(appsMchPromiseAdminService.index(vo, keyName));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("保证金审核列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "mchName", value = "店铺名称", dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "status", value = "申请状态", dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "startTime", value = "开始时间", dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "endTime", value = "结束时间", dataType = "string", paramType = "form"),
    })
    @PostMapping("/PromisePriceIndex")
    @HttpApiMethod(apiKey = "admin.mch.PromisePriceIndex")
    public Result PromisePriceIndex(MainVo vo, String mchName, String status, String startTime, String endTime) throws LaiKeAPIException
    {
        try
        {
            return Result.success(appsMchPromiseAdminService.PromisePriceIndex(vo, mchName, status, startTime, endTime));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }
}
