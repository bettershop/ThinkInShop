package com.laiketui.plugins.diy.controller;

import com.laiketui.core.domain.Result;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.diy.DiyPageVo;
import com.laiketui.plugins.api.diy.PluginsDiyPageAdminService;
import com.laiketui.root.annotation.HttpApiMethod;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "diy单页面操作接口")
@RestController
@RequestMapping("/plugin/admin/diyPage")
public class PluginsDiyPageAdminController
{

    @Autowired
    private PluginsDiyPageAdminService pluginsDiyPageAdminService;

    @ApiOperation("获取diy项目单页列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "name", value = "页面名称", dataType = "String", paramType = "form"),
            @ApiImplicitParam(name = "status", value = "状态 0:未使用 1：使用", dataType = "int", paramType = "form")
    })
    @PostMapping("/getDiyPageList")
    @HttpApiMethod(apiKey = "plugin.template.AdminDiy.getDiyPageList")
    public Result getDiyPageList(MainVo vo,String name,Integer status) throws LaiKeAPIException
    {
        try
        {
            return Result.success(pluginsDiyPageAdminService.getDiyPageList(vo,name,status));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("添加/编辑diy单页")
    @PostMapping("/addOrUpdateDiyPage")
    @HttpApiMethod(apiKey = "plugin.template.AdminDiy.addOrUpdateDiyPage")
    public Result addOrUpdateDiyPage(DiyPageVo vo) throws LaiKeAPIException
    {
        try
        {
            return Result.success(pluginsDiyPageAdminService.addOrUpdateDiyPage(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("查看关联主题")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "id", dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "name", value = "页面名称", dataType = "String", paramType = "form")
    })
    @PostMapping("/getDiyPageBindList")
    @HttpApiMethod(apiKey = "plugin.template.AdminDiy.getDiyPageBindList")
    public Result getDiyPageBindList(MainVo vo, Integer id,String name) throws LaiKeAPIException
    {
        try
        {
            return Result.success(pluginsDiyPageAdminService.getDiyPageBindList(vo, id,name));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("删除页面绑定关系")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "diy_id", value = "主题id", dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "diy_page_id", value = "页面id", dataType = "int", paramType = "form")
    })
    @PostMapping("/delBindDiyPage")
    @HttpApiMethod(apiKey = "plugin.template.AdminDiy.delBindDiyPage")
    public Result delBindDiyPage(MainVo vo, Integer diy_id,Integer diy_page_id) throws LaiKeAPIException
    {
        try
        {
            pluginsDiyPageAdminService.delBindDiyPage(vo, diy_id,diy_page_id);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("获取页面json")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "diy_id", value = "主题id", dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "link", value = "页面链接", dataType = "String", paramType = "form")
    })
    @PostMapping("/getPageJson")
    @HttpApiMethod(apiKey = "plugin.template.AdminDiy.getPageJson")
    public Result getPageJson(MainVo vo,Integer diy_id,String link) throws LaiKeAPIException
    {
        try
        {
            return Result.success(pluginsDiyPageAdminService.getPageJson(vo,diy_id,link));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("删除页面")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "id", dataType = "int", paramType = "form")
    })
    @PostMapping("/delDiyPage")
    @HttpApiMethod(apiKey = "plugin.template.AdminDiy.delDiyPage")
    public Result delDiy(MainVo vo, Integer id) throws LaiKeAPIException
    {
        try
        {
            pluginsDiyPageAdminService.delDiyPage(vo, id);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }



    @ApiOperation("获取diy单页")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "id", dataType = "int", paramType = "form")
    })
    @PostMapping("/getDiyPageById")
    @HttpApiMethod(apiKey = "plugin.template.adminDiy.getDiyPageById")
    public Result getDiyPageById(MainVo vo, Integer id) throws LaiKeAPIException
    {
        try
        {
            return Result.success(pluginsDiyPageAdminService.getDiyPageById(vo, id));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

}
