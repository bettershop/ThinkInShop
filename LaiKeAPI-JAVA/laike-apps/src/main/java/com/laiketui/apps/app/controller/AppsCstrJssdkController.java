package com.laiketui.apps.app.controller;

import com.laiketui.apps.api.app.AppsCstrJssdkService;
import com.laiketui.core.annotation.ParamsMapping;
import com.laiketui.core.domain.Result;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.root.annotation.HttpApiMethod;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotNull;

/**
 * h5授权信息接口
 *
 * @author Trick
 * @date 2020/10/9 14:47
 */
@RestController
@Validated
@RequestMapping("/app/jssdk")
public class AppsCstrJssdkController
{

    @Autowired
    AppsCstrJssdkService appsCstrJssdkService;

    @ApiOperation("获取h5权限信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "storeId", value = "商城id", required = true, dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "url", value = "小程序url", required = true, dataType = "int", paramType = "form"),
    })
    @PostMapping("/getData")
    @HttpApiMethod(urlMapping = "app.jssdk.getData")
    public Result getData(@NotNull @ParamsMapping("store_id") int storeId, @NotNull String url)
    {
        try
        {
            return Result.success(appsCstrJssdkService.getData(storeId, url));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }

    }

}
