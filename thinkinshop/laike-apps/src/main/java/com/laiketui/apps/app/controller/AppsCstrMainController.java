package com.laiketui.apps.app.controller;

import com.laiketui.core.domain.Result;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotNull;


@RestController
@Validated
@RequestMapping("/app/test")
public class AppsCstrMainController
{

    @ApiOperation("测试")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "storeId", value = "商城id", required = true, dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "language", value = "语言", required = true, dataType = "String", paramType = "form"),
            @ApiImplicitParam(name = "accessId", value = "授权id", required = true, dataType = "String", paramType = "form")
    })
    @PostMapping("main")
    @ResponseBody
    public Result index(int storeId, @NotNull String language, @NotNull String accessId) throws LaiKeAPIException
    {
        try
        {
            return Result.success(null);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("测试")
    @PostMapping("main2")
    @ResponseBody
    public Result index1(MainVo vo) throws LaiKeAPIException
    {
        try
        {
            return Result.success(vo);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

}
