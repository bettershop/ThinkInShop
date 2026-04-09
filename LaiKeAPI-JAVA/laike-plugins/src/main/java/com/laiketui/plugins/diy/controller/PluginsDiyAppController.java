package com.laiketui.plugins.diy.controller;

import com.laiketui.core.domain.Result;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.plugins.api.diy.PluginsDiyAppService;
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
 * diy移动端接口
 *
 * @author Trick
 * @date 2022/5/9 9:37
 */
@Api(tags = "diy移动端接口")
@RestController
@RequestMapping("/plugin/diy")
public class PluginsDiyAppController
{


    @Autowired
    private PluginsDiyAppService pluginsDiyAppService;

    @ApiOperation("diy首页接口")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "id", dataType = "int", paramType = "form")
    })
    @PostMapping("/index")
    @HttpApiMethod(urlMapping = {"plugin.diy.index", "app.diy.index"})
    public Result index(MainVo vo) throws LaiKeAPIException
    {
        try
        {
            return Result.success(pluginsDiyAppService.index(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("根据页面id获取页面数据")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "id", dataType = "int", paramType = "form")
    })
    @PostMapping("/getPageInfoById")
    @HttpApiMethod(apiKey = "plugin.diy.getPageInfoById")
    public Result getPageInfoById(Integer id)
    {
        try
        {
            return Result.success(pluginsDiyAppService.getPageInfoById(id));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }
}
