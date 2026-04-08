package com.laiketui.plugins.diy.controller;

import com.laiketui.core.domain.Result;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.diy.DiyProjectVo;
import com.laiketui.plugins.api.diy.PluginsDiyProjectAdminService;
import com.laiketui.root.annotation.HttpApiMethod;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "diy项目操作接口")
@RestController
@RequestMapping("/plugin/admin/diyProject")
public class PluginsDiyProjectAdminController
{

    @Autowired
    private PluginsDiyProjectAdminService pluginsDiyProjectAdminService;

    @ApiOperation("获取diy套装列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "id", dataType = "int", paramType = "form")
    })
    @PostMapping("/getDiyProjectList")
    @HttpApiMethod(apiKey = "plugin.template.AdminDiy.getDiyProjectList")
    public Result getDiyProjectList(MainVo vo, Integer id) throws LaiKeAPIException
    {
        try
        {
            return Result.success(pluginsDiyProjectAdminService.getDiyProjectList(vo, id));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("获取diy套装")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "id", dataType = "int", paramType = "form")
    })
    @PostMapping("/getDiyProjectById")
    @HttpApiMethod(apiKey = "plugin.template.adminDiy.getDiyProjectById")
    public Result getDiyProjectById(MainVo vo, Integer id) throws LaiKeAPIException
    {
        try
        {
            return Result.success(pluginsDiyProjectAdminService.getDiyProjectById(vo, id));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("添加/编辑diy套装")
    @PostMapping("/addOrUpdateDiyProject")
    @HttpApiMethod(apiKey = "plugin.template.AdminDiy.addOrUpdateDiyProject")
    public Result addOrUpdateDiyProject(DiyProjectVo vo) throws LaiKeAPIException
    {
        try
        {
            pluginsDiyProjectAdminService.addOrUpdateDiyProject(vo);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("设置diy套装使用状态")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "id", dataType = "int", paramType = "form")
    })
    @PostMapping("/diyProjectStatus")
    @HttpApiMethod(apiKey = "plugin.template.AdminDiy.diyProjectStatus")
    public Result diyProjectStatus(MainVo vo, Integer id) throws LaiKeAPIException
    {
        try
        {
            pluginsDiyProjectAdminService.diyProjectStatus(vo, id);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("删除diy项目")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "id", dataType = "int", paramType = "form")
    })
    @PostMapping("/delDiyProject")
    @HttpApiMethod(apiKey = "plugin.template.AdminDiy.delDiyProject")
    public Result delDiy(MainVo vo, Integer id) throws LaiKeAPIException
    {
        try
        {
            pluginsDiyProjectAdminService.delDiyProject(vo, id);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }
}
