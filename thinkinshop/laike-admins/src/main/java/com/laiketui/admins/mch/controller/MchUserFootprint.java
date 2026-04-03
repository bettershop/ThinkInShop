package com.laiketui.admins.mch.controller;

import com.laiketui.admins.api.mch.MchUserFootprintService;
import com.laiketui.core.annotation.ParamsMapping;
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
 * 客户管理
 *
 * @author Trick
 * @date 2021/5/26 17:17
 */
@Api(tags = "客户管理")
@RestController
@RequestMapping({"/admin/mch/client/"})
public class MchUserFootprint
{

    @Autowired
    private MchUserFootprintService mchUserFootprintService;

    @ApiOperation("客户管理列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "phone", value = "手机号", dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "startDate", value = "开始时间", dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "endDate", value = "结束时间", dataType = "string", paramType = "form"),
    })
    @PostMapping("/clientList")
    @HttpApiMethod(apiKey = "admin.mch.client.clientList")
    public Result clientList(MainVo vo, @ParamsMapping("mobile") String phone, @ParamsMapping("startdate") String startDate, @ParamsMapping("enddate") String endDate)
    {
        try
        {
            return Result.success(mchUserFootprintService.index(vo, phone, startDate, endDate));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("今日足迹")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userId", value = "用户id", dataType = "string", paramType = "form")
    })
    @PostMapping("/userPug")
    @HttpApiMethod(apiKey = "admin.mch.client.userPug")
    public Result userPug(MainVo vo, String userId)
    {
        try
        {
            return Result.success(mchUserFootprintService.userPug(vo, userId));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("删除足迹")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userId", value = "用户id", dataType = "string", paramType = "form")
    })
    @PostMapping("/del")
    @HttpApiMethod(apiKey = "admin.mch.client.del")
    public Result del(MainVo vo, String userId)
    {
        try
        {
            return Result.success(mchUserFootprintService.del(vo, userId));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

}
