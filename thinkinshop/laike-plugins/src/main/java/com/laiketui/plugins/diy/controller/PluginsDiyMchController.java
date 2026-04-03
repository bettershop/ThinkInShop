package com.laiketui.plugins.diy.controller;

import com.laiketui.core.domain.Result;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.diy.DiyVo;
import com.laiketui.plugins.api.diy.PluginsDiyMchService;
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
 * @Author: liuao
 * @Date: 2025-06-18-13:54
 * @Description:
 */
@Api(tags = "diy店铺后台管理")
@RestController
@RequestMapping("/plugin/mchDiy")
public class PluginsDiyMchController {


    @Autowired
    private PluginsDiyMchService pluginsDiyMchService;

    @ApiOperation("获取diy模板列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "id", dataType = "int", paramType = "form")
    })
    @PostMapping("/getDiyList")
    @HttpApiMethod(apiKey = "plugin.template.mchAdminDiy.GetDiyList")
    public Result getDiyList(MainVo vo, Integer id) throws LaiKeAPIException
    {
        try
        {
            return Result.success(pluginsDiyMchService.getDiyList(vo, id));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("添加/编辑diy模板")
    @PostMapping("/addOrUpdateDiy")
    @HttpApiMethod(apiKey = "plugin.template.mchAdminDiy.AddOrUpdateDiy")
    public Result addOrUpdateDiy(DiyVo vo) throws LaiKeAPIException
    {
        try
        {
            return Result.success(pluginsDiyMchService.addOrUpdateDiy(vo));
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
    @HttpApiMethod(apiKey = "plugin.template.mchAdminDiy.DiyStatus")
    public Result diyStatus(MainVo vo, Integer id) throws LaiKeAPIException
    {
        try
        {
            pluginsDiyMchService.diyStatus(vo, id);
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
    @HttpApiMethod(apiKey = "plugin.template.mchAdminDiy.DelDiy")
    public Result delDiy(MainVo vo, Integer id) throws LaiKeAPIException
    {
        try
        {
            pluginsDiyMchService.delDiy(vo, id);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("diy后台首页")
    @PostMapping("/index")
    @HttpApiMethod(apiKey = "plugin.template.mchAdminDiy.Index")
    public Result index(MainVo vo) throws LaiKeAPIException {
        try {
            return Result.success(pluginsDiyMchService.index(vo));
        } catch (LaiKeAPIException e) {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("轮播图路径分类")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "type", value = "跳转类型 1.分类 2.商品 3.店铺 默认自定义", dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "language", value = "国际化语种", dataType = "String", paramType = "form")
    })
    @PostMapping("/bannerPathList")
    @HttpApiMethod(apiKey = "plugin.template.mchAdminDiy.BannerPathList")
    public Result bannerPathList(MainVo vo, Integer type, String language) throws LaiKeAPIException {
        try {
            return Result.success(pluginsDiyMchService.bannerPathList(vo, type, language));
        } catch (LaiKeAPIException e) {
            return Result.error(e.getCode(), e.getMessage());
        }
    }
}
