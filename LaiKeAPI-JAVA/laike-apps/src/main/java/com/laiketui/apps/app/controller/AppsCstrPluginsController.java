package com.laiketui.apps.app.controller;

import com.laiketui.apps.api.app.AppsCstrPluginsService;
import com.laiketui.core.domain.Result;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.root.annotation.HttpApiMethod;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 插件信息API
 *
 * @author sunH_
 * @date 2020/12/17 11:23
 */
@Api(tags = "插件信息API")
@RestController
@RequestMapping("/app/plugins")
@Validated
public class AppsCstrPluginsController
{

    @Autowired
    private AppsCstrPluginsService pluginsService;

    @ApiOperation("首页是否div接口")
    @PostMapping("/getPluginInfo")
    @HttpApiMethod(urlMapping = "app.plugins.getPluginInfo")
    public Result getPluginInfo(MainVo vo, String pluginCode) throws LaiKeAPIException
    {
        try
        {
            return Result.success(pluginsService.getPluginInfo(vo, pluginCode));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("获取所有插件开关状态")
    @PostMapping("/getPluginAll")
    @HttpApiMethod(urlMapping = "app.plugins.getPluginAll")
    public Result getPluginInfo(MainVo vo) throws LaiKeAPIException
    {
        try
        {
            return Result.success(pluginsService.getPluginAll(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }
}
