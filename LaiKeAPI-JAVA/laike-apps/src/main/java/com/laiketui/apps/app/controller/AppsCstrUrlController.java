package com.laiketui.apps.app.controller;

import com.laiketui.apps.api.app.AppsCstrUrlService;
import com.laiketui.core.annotation.ParamsMapping;
import com.laiketui.core.domain.Result;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.root.annotation.HttpApiMethod;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 首页
 *
 * @author Trick
 * @date 2020/10/9 13:58
 */
@RestController
@Validated
@RequestMapping("/app/kefu")
public class AppsCstrUrlController
{

    @Autowired
    AppsCstrUrlService appsCstrUrlService;

    @ApiOperation("获取首页授权url")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "storeId", value = "商城id", required = true, dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "language", value = "语言", required = true, dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "access_id", value = "授权id", required = true, dataType = "String", paramType = "form"),
            @ApiImplicitParam(name = "get", value = "参数", required = true, dataType = "String", paramType = "form")
    })
    @PostMapping("/getUrl")
    @HttpApiMethod(urlMapping = "app.url.geturl")
    public Result getUrl(@ParamsMapping("store_id") int storeId, String language,
                         @ParamsMapping("access_id") String accessId, String get)
    {
        try
        {
            return Result.success(appsCstrUrlService.getUrl(storeId, language, accessId, get));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error("FAIL");
        }
    }


    @ApiOperation("获取在线客服url")
    @PostMapping("/kefu_url")
    @HttpApiMethod(urlMapping = "app.url.kefu_url")
    public Result getServerUrl(MainVo vo)
    {
        try
        {
            return Result.success(appsCstrUrlService.getServerUrl(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error("FAIL");
        }
    }

}
