package com.laiketui.admins.admin.controller;

import com.laiketui.admins.api.admin.AdminPcManageService;
import com.laiketui.core.domain.Result;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.config.AddPcConfigVo;
import com.laiketui.domain.vo.pc.AddBannerInfoVo;
import com.laiketui.domain.vo.pc.AddBottomInfoVo;
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
 * pc管理
 *
 * @author Trick
 * @date 2021/1/22 10:38
 */
@Api(tags = "后台-pc管理")
@RestController
@RequestMapping("/admin/pc")
public class AdminPcManageController
{
    @Autowired
    private AdminPcManageService adminPcManageService;

    @ApiOperation("获取pc轮播图列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "轮播图id", dataType = "int", paramType = "form")
    })
    @PostMapping("/getBannerInfo")
    @HttpApiMethod(apiKey = "admin.pc.getBannerInfo")
    public Result getBannerInfo(MainVo vo, Integer id)
    {
        try
        {
            return Result.success(adminPcManageService.getBannerInfo(vo, id));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("添加/编辑轮播图信息")
    @PostMapping("/addBannerInfo")
    @HttpApiMethod(apiKey = "admin.pc.addBannerInfo")
    public Result addBannerInfo(AddBannerInfoVo vo)
    {
        try
        {
            return Result.success(adminPcManageService.addBannerInfo(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("删除轮播图")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "轮播图id", dataType = "int", paramType = "form")
    })
    @PostMapping("/delBannerById")
    @HttpApiMethod(apiKey = "admin.pc.delBannerById")
    public Result delBannerById(int id)
    {
        try
        {
            return Result.success(adminPcManageService.delBannerById(id));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("轮播图置顶")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "轮播图id", dataType = "int", paramType = "form")
    })
    @PostMapping("/topBannerById")
    @HttpApiMethod(apiKey = "admin.pc.topBannerById")
    public Result topBannerById(int id)
    {
        try
        {
            adminPcManageService.topBannerById(id);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("获取PC商城底部栏图片配置")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "轮播图id", dataType = "int", paramType = "form")
    })
    @PostMapping("/getBottomInfo")
    @HttpApiMethod(apiKey = "admin.pc.getBottomInfo")
    public Result getBottomInfo(MainVo vo, Integer id)
    {
        try
        {
            return Result.success(adminPcManageService.getBottomInfo(vo, id));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("添加/编辑PC商城底部栏图片配置")
    @PostMapping("/addBottomInfo")
    @HttpApiMethod(apiKey = "admin.pc.addBottomInfo")
    public Result addBottomInfo(AddBottomInfoVo vo)
    {
        try
        {
            return Result.success(adminPcManageService.addBottomInfo(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("删除PC商城底部栏图片配置")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "轮播图id", dataType = "int", paramType = "form")
    })
    @PostMapping("/delBottomById")
    @HttpApiMethod(apiKey = "admin.pc.delBottomById")
    public Result delBottomById(MainVo vo, int id)
    {
        try
        {
            return Result.success(adminPcManageService.delBottomById(vo, id));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("获取PC商城显示配置")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "轮播图id", dataType = "int", paramType = "form")
    })
    @PostMapping("/getConfig")
    @HttpApiMethod(apiKey = "admin.pc.getConfig")
    public Result getConfig(MainVo vo)
    {
        try
        {
            return Result.success(adminPcManageService.getConfig(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("添加/编辑PC商城显示配置")
    @PostMapping("/addConfig")
    @HttpApiMethod(apiKey = "admin.pc.addConfig")
    public Result addConfig(MainVo vo, AddPcConfigVo config)
    {
        try
        {
            adminPcManageService.addConfig(vo, config);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }
}
