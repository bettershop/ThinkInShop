package com.laiketui.admins.admin.controller;

import com.laiketui.admins.api.admin.AdminUserService;
import com.laiketui.core.domain.Result;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.admin.UpdateAdminVo;
import com.laiketui.domain.vo.user.AdminColorVo;
import com.laiketui.domain.vo.user.AdminLoginVo;
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
 * 商城后台登录
 *
 * @author Trick
 * @date 2021/1/26 11:30
 */
@Api(tags = "后台-商城后台登录")
@RestController
@RequestMapping("/admin/saas/user")
public class AdminUserController
{

    @Autowired
    private AdminUserService adminUserService;

    @ApiOperation("后台登录")
    @PostMapping("/login")
    @HttpApiMethod(apiKey = "admin.saas.user.login")
    public Result login(AdminLoginVo vo)
    {
        try
        {
            return Result.success(adminUserService.login(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("获取图形验证码")
    @PostMapping("/getCode")
    @HttpApiMethod(urlMapping = {"admin.saas.user.getCode"})
    public Result getCode(MainVo vo)
    {
        try
        {
            return Result.success(adminUserService.getCode(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("退出登录")
    @PostMapping("/logout")
    @HttpApiMethod(apiKey = "admin.saas.user.logout")
    public Result logout(MainVo vo)
    {
        try
        {
            adminUserService.logout(vo);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("赋予系统管理员商城")
    @PostMapping("/setUserAdmin")
    @HttpApiMethod(apiKey = "admin.saas.user.setUserAdmin")
    public Result setUserAdmin(MainVo vo)
    {
        try
        {
            return Result.success(adminUserService.setUserAdmin(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("更改语言api")
    @PostMapping("/select_language")
    @HttpApiMethod(apiKey = "admin.saas.user.select_language")
    public Result selectLanguage(MainVo vo)
    {
        try
        {
            adminUserService.updateLanguageByUser(vo);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("修改基本信息")
    @PostMapping("/updateAdminInfo")
    @HttpApiMethod(apiKey = "admin.saas.user.updateAdminInfo")
    public Result updateAdminInfo(UpdateAdminVo vo)
    {
        try
        {
            adminUserService.updateAdminInfo(vo);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("标记公告以读")
    @PostMapping("/markToRead")
    @HttpApiMethod(apiKey = "admin.saas.user.markToRead")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "tell_id", value = "公告id", dataType = "int", paramType = "form")
    })
    public Result markToRead(MainVo vo, Integer tell_id)
    {
        try
        {
            adminUserService.markToRead(vo, tell_id);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("获取平台维护公告")
    @PostMapping("getUserTell")
    @HttpApiMethod(urlMapping = "admin.saas.user.getUserTell")
    public Result getUserTell(MainVo vo)
    {
        try
        {
            return Result.success(adminUserService.getUserTell(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("修改管理员商城样式颜色")
    @PostMapping("/updateAdminColor")
    @HttpApiMethod(apiKey = "admin.saas.user.updateColor")
    public Result updateAdminColor(AdminColorVo vo)
    {
        try
        {
            adminUserService.updateAdminColor(vo);
            return Result.success("颜色修改成功");
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }
}
