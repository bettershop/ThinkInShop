package com.laiketui.admins.admin.controller;

import com.laiketui.admins.api.admin.AdminOverviewService;
import com.laiketui.core.domain.Result;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.saas.OverviewVo;
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
 * 功能导览
 *
 * @author Trick
 * @date 2021/1/26 11:30
 */
@Api(tags = "后台-功能导览")
@RestController
@RequestMapping("/admin/overview")
public class AdminOverviewController
{

    @Autowired
    private AdminOverviewService adminOverviewService;

    @ApiOperation("功能导览首页")
    @PostMapping("/index")
    @HttpApiMethod(apiKey = "admin.overview.index")
    public Result getMchInfo(MainVo vo)
    {
        try
        {
            return Result.success(adminOverviewService.index(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("编辑商城导览列表")
    @PostMapping("/functionList")
    @HttpApiMethod(apiKey = "admin.overview.functionList")
    public Result getMchInfo(OverviewVo vo)
    {
        try
        {
            return Result.success(adminOverviewService.functionList(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("【功能导览顺序重新排序】")
    @PostMapping("/rsort")
    public Result rsort(MainVo vo)
    {
        try
        {
            adminOverviewService.rsort(vo);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("菜单置顶")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "菜单id", required = true, dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "sid", value = "子菜单id 子菜单置顶", dataType = "int", paramType = "form")
    })
    @PostMapping("/sortTop")
    @HttpApiMethod(apiKey = "admin.overview.sortTop")
    public Result sortTop(MainVo vo, int id, Integer sid)
    {
        try
        {
            adminOverviewService.sortTop(vo, id, sid);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("菜单上下移动")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "菜单id", dataType = "int", required = true, paramType = "form"),
            @ApiImplicitParam(name = "id2", value = "菜单id2", dataType = "int", readOnly = true, paramType = "form")
    })
    @PostMapping("/move")
    @HttpApiMethod(apiKey = "admin.overview.move")
    public Result sortTop(MainVo vo, int id, int id2)
    {
        try
        {
            adminOverviewService.move(vo, id, id2);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("是否显示开关")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "菜单id", dataType = "int", required = true, paramType = "form")
    })
    @PostMapping("/isDisplaySwitch")
    @HttpApiMethod(apiKey = "admin.overview.isDisplaySwitch")
    public Result sortTop(MainVo vo, int id)
    {
        try
        {
            adminOverviewService.isDisplaySwitch(vo, id);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }
}
