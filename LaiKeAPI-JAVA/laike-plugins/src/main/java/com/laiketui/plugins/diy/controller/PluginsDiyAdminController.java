package com.laiketui.plugins.diy.controller;

import com.laiketui.core.domain.Result;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.diy.DiyVo;
import com.laiketui.plugins.api.diy.PluginsDiyAdminService;
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
 * @Author: sunH_
 * @Date: Create in 15:47 2022/4/28
 */
@Api(tags = "diy模板管理")
@RestController
@RequestMapping("/plugin/adminDiy")
public class PluginsDiyAdminController
{

    @Autowired
    private PluginsDiyAdminService pluginsDiyAdminService;


    @ApiOperation("获取diy模板列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "id", dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "theme_type", value = "主题类型 1:系统 2:自定义", dataType = "Integer", paramType = "form"),
            @ApiImplicitParam(name = "theme_type_code", value = "主题类型code", dataType = "String", paramType = "form")
    })
    @PostMapping("/getDiyList")
    @HttpApiMethod(apiKey = "plugin.template.AdminDiy.GetDiyList")
    public Result getDiyList(MainVo vo, Integer id,Integer theme_type,String theme_type_code) throws LaiKeAPIException
    {
        try
        {
            return Result.success(pluginsDiyAdminService.getDiyList(vo, id,theme_type,theme_type_code));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("获取插件状态")
    @PostMapping("/getPluginStatus")
    @HttpApiMethod(apiKey = "plugin.template.Admin.getPluginStatus")
    public Result getPluginStatus(MainVo vo) throws LaiKeAPIException
    {
        try
        {
            return Result.success(pluginsDiyAdminService.getPluginStatus(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("添加/编辑diy模板")
    @PostMapping("/addOrUpdateDiy")
    @HttpApiMethod(apiKey = "plugin.template.AdminDiy.AddOrUpdateDiy")
    public Result addOrUpdateDiy(DiyVo vo) throws LaiKeAPIException
    {
        try
        {
            return Result.success(pluginsDiyAdminService.addOrUpdateDiy(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("获取当前主题绑定的页面列表")
    @PostMapping("/getBindPageList")
    @HttpApiMethod(apiKey = "plugin.template.AdminDiy.getBindPageList")
    public Result getBindPageList(MainVo vo, Integer id) throws LaiKeAPIException
    {
        try
        {
            return Result.success(pluginsDiyAdminService.getBindPageList(vo, id));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("设置diy模板使用状态")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "id", dataType = "int", paramType = "form")
    })
    @PostMapping("/diyStatus")
    @HttpApiMethod(apiKey = "plugin.template.AdminDiy.DiyStatus")
    public Result diyStatus(MainVo vo, Integer id) throws LaiKeAPIException
    {
        try
        {
            pluginsDiyAdminService.diyStatus(vo, id);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("删除diy")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "id", dataType = "int", paramType = "form")
    })
    @PostMapping("/delDiy")
    @HttpApiMethod(apiKey = "plugin.template.AdminDiy.DelDiy")
    public Result delDiy(MainVo vo, Integer id) throws LaiKeAPIException
    {
        try
        {
            pluginsDiyAdminService.delDiy(vo, id);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }
}
