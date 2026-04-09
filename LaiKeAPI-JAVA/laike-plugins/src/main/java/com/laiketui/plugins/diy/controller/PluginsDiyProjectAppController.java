package com.laiketui.plugins.diy.controller;

import com.laiketui.core.domain.Result;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
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

@Api(tags = "diy移动端操作接口")
@RestController
@RequestMapping("/plugin/app/diyProject")
public class PluginsDiyProjectAppController
{

    @Autowired
    private PluginsDiyProjectAdminService pluginsDiyProjectAdminService;

    @ApiOperation("获取diy套装列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "id", dataType = "int", paramType = "form")
    })
    @PostMapping("/getDiyProjectById")
    @HttpApiMethod(apiKey = "plugin.template.appDiy.getDiyProjectById")
    public Result getDiyProjectList(MainVo vo, Integer id) throws LaiKeAPIException
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

}
