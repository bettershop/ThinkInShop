package com.laiketui.admins.mch.controller;

import com.laiketui.admins.api.mch.MchUserLoginService;
import com.laiketui.core.annotation.ParamsMapping;
import com.laiketui.core.domain.Result;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.user.ForgetPasswordVo;
import com.laiketui.domain.vo.user.LoginVo;
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
 * 用户模块
 *
 * @author Trick
 * @date 2021/5/26 9:35
 */
@Api(tags = "用户模块")
@RestController
@RequestMapping({"/admin/mch/user/"})
public class MchUserController
{

    @Autowired
    private MchUserLoginService mchUserLoginService;

    @ApiOperation("用户账号密码登陆")
    @PostMapping("/loginUser")
    @HttpApiMethod(apiKey = "mch.Mch.User.LoginUser")
    public Result loginUser(LoginVo vo)
    {
        try
        {
            return Result.success(mchUserLoginService.login(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("获取图形验证码")
    @PostMapping("/getCode")
    @HttpApiMethod(urlMapping = {"mch.Mch.User.getCode"})
    public Result getCode(MainVo vo)
    {
        try
        {
            return Result.success(mchUserLoginService.getCode(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("发送短信")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "phone", value = "手机号", required = true, dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "storeId", value = "商户id", required = true, dataType = "int", paramType = "form")
    })
    @PostMapping("/sendSms")
    @HttpApiMethod(apiKey = "mch.Mch.User.SendSms")
    public Result sendSms(String phone, @ParamsMapping("store_id") int storeId)
    {
        try
        {
            return Result.success(mchUserLoginService.sendSms(phone, storeId));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("修改密码发送短信")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "phone", value = "手机号", required = true, dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "storeId", value = "商户id", required = true, dataType = "int", paramType = "form")
    })
    @PostMapping("/sendSmsForgetPwd")
    @HttpApiMethod(apiKey = "mch.Mch.User.SendSmsForgetPwd")
    public Result sendSmsForgetPwd(MainVo vo, String phone)
    {
        try
        {
            return Result.success(mchUserLoginService.sendSmsForgetPwd(vo, phone));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("用户短信登陆")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "phone", value = "手机号", required = true, dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "pcode", value = "验证码", required = true, dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "imgCode", value = "图形验证码", required = true, dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "imgCodeToken", value = "图形验证码token", required = true, dataType = "string", paramType = "form")
    })
    @PostMapping("/loginBySms")
    @HttpApiMethod(apiKey = "mch.Mch.User.LoginBySms")
    public Result loginSmsUser(MainVo vo, String phone, @ParamsMapping("keyCode") String pcode, String imgCode, String imgCodeToken)
    {
        try
        {
            return Result.success(mchUserLoginService.loginBySms(vo, phone, pcode, imgCode, imgCodeToken));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("退出登陆")
    @PostMapping("/quit")
    @HttpApiMethod(apiKey = "mch.Mch.User.quit")
    public Result quit(MainVo vo)
    {
        try
        {
            return Result.success(mchUserLoginService.loginOut(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("修改密码")
    @PostMapping("/forgetPwd")
    @HttpApiMethod(apiKey = "mch.Mch.User.ForgetPwd")
    public Result forgetPwd(ForgetPasswordVo vo)
    {
        try
        {
            return Result.success(mchUserLoginService.forgetPwd(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("商城查看店铺端所需token")
    @PostMapping("/storeLookMch")
    @HttpApiMethod(apiKey = "mch.Admin.Mch.StoreLookMch")
    public Result storeLookMch(MainVo vo, Integer mchId)
    {
        try
        {
            return Result.success(mchUserLoginService.storeLookMch(vo, mchId));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("标记公告以读")
    @PostMapping("/markToRead")
    @HttpApiMethod(apiKey = "mch.Admin.Mch.markToRead")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "tell_id", value = "公告id", dataType = "int", paramType = "form")
    })
    public Result markToRead(MainVo vo, Integer tell_id)
    {
        try
        {
            mchUserLoginService.markToRead(vo, tell_id);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("获取平台维护公告")
    @PostMapping("getUserTell")
    @HttpApiMethod(urlMapping = "mch.Admin.Mch.getUserTell")
    public Result getUserTell(MainVo vo)
    {
        try
        {
            return Result.success(mchUserLoginService.getUserTell(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }
}
