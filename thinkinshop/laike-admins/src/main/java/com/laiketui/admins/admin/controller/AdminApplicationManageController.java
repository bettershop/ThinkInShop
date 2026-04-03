package com.laiketui.admins.admin.controller;

import com.laiketui.admins.api.admin.AdminApplicationManageService;
import com.laiketui.core.domain.Result;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.app.AppConfigInfoVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * app管理
 *
 * @author Trick
 * @date 2021/1/22 9:37
 */
@Api(tags = "后台-app管理")
@RestController
@RequestMapping("/admin/app")
public class AdminApplicationManageController
{

    @Autowired
    private AdminApplicationManageService adminApplicationManageService;

    @ApiOperation("获取app版本配置信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "storeId", value = "商出id", dataType = "int", paramType = "form")
    })
    @PostMapping("/getVersionConfigInfo")
    public Result getVersionConfigInfo(int storeId)
    {
        try
        {
            return Result.success(adminApplicationManageService.getVersionConfigInfo(storeId));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("添加/编辑版本配置信息")
    @PostMapping("/addVersionConfigInfo")
    public Result addVersionConfigInfo(AppConfigInfoVo vo)
    {
        try
        {
            return Result.success(adminApplicationManageService.addVersionConfigInfo(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

}
