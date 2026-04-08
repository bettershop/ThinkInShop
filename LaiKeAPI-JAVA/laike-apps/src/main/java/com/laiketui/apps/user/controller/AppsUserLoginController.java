package com.laiketui.apps.user.controller;

import com.laiketui.apps.api.user.AppsUserLoginService;
import com.laiketui.common.api.PubliceService;
import com.laiketui.core.annotation.ParamsMapping;
import com.laiketui.core.domain.Result;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.DictionaryConst;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.SaveAuthorizeUserInfoVo;
import com.laiketui.domain.vo.user.AlipayUserVo;
import com.laiketui.domain.vo.user.AppletsVo;
import com.laiketui.domain.vo.user.WxAuthPhoneVo;
import com.laiketui.root.annotation.HttpApiMethod;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotNull;
import java.util.Map;

/**
 * 登陆
 *
 * @author Trick
 * @date 2020/10/19 9:12
 */
@Api(tags = "登录")
@RestController
@Validated
@RequestMapping("/app/user/login")
public class AppsUserLoginController
{

    @Autowired
    AppsUserLoginService appsUserLoginService;

    @Autowired
    PubliceService publiceService;

    @ApiOperation("获取token")
    @PostMapping("/getToken")
    @HttpApiMethod(urlMapping = "app.login.token")
    public Result getToken(MainVo vo)
    {
        try
        {
            return Result.success(appsUserLoginService.token(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("判断是否要注册")
    @PostMapping("is_register")
    public Result isRegister(MainVo vo)
    {
        try
        {
            Map<String, Object> result = appsUserLoginService.isRegister(vo);
            return Result.success(result);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("授权未过期")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "openid", value = "微信openid", required = true, dataType = "int", paramType = "form")
    })
    @PostMapping("login_access")
    public Result loginAccess(MainVo vo, @NotNull String openid)
    {
        try
        {
            Map<String, Object> result = appsUserLoginService.loginAccess(vo, openid);
            return Result.success(result);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("用户授权时,存储用户信息（已弃用）")
    @PostMapping("user")
    @Deprecated
    public Result saveAuthorizeUserInfo(SaveAuthorizeUserInfoVo vo)
    {
        try
        {
            Map<String, Object> result = appsUserLoginService.saveAuthorizeUserInfo(vo);
            return Result.success(result);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    //h5
    @ApiOperation("进入登陆页面")
    @PostMapping("loginIndex")
    @HttpApiMethod(urlMapping = "app.login.index")
    public Result loginIndex(MainVo vo)
    {
        try
        {
            Map<String, Object> result = appsUserLoginService.loginIndex(vo);
            return Result.success(result);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("注册协议")
    @PostMapping("register_agreement")
    @HttpApiMethod(urlMapping = "app.login.register_agreement")
    public Result getRegisterAgreement(MainVo vo)
    {
        try
        {
            return Result.success(publiceService.getAgreement(vo.getStoreId(), DictionaryConst.AgreementType.AGREEMENTTYPE_REGISTER));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("隐私协议")
    @PostMapping("privacy_agreement")
    @HttpApiMethod(urlMapping = "app.login.privacy_agreement")
    public Result getPrivacyAgreement(MainVo vo)
    {
        try
        {
            return Result.success(publiceService.getAgreement(vo.getStoreId(), DictionaryConst.AgreementType.AGREEMENTTYPE_PRIVACY));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("忘记密码")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "type", value = " 类型 0.手机号 1.邮箱", required = true, dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "zhanghao", value = "账号", required = false, dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "cpc", value = "区号", required = false, dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "country_num", value = "国家代码", required = false, dataType = "Integer", paramType = "form")

    })
    @PostMapping("forget_zhanghao")
    @HttpApiMethod(apiKey = "app.Login.forget_zhanghao")
    public Result forgetZhanghao(MainVo vo, String zhanghao,Integer type,String cpc,Integer country_num)
    {
        try
        {
            return Result.success(appsUserLoginService.forgetZhanghao(vo, zhanghao,type,cpc,country_num));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("忘记密码,验证验证码是否正确,并修改密码")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "tel", value = "手机号", required = false, dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "keyCode", value = "验证码", required = false, dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "type", value = "类型 0.手机号 1.邮箱", required = false, dataType = "Integer", paramType = "form"),
            @ApiImplicitParam(name = "e_mail", value = "邮箱", required = false, dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "password", value = "密码", required = false, dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "cpc", value = "区号", required = false, dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "country_num", value = "国家代码", required = false, dataType = "string", paramType = "form")
    })
    @PostMapping("forget_code")
    @HttpApiMethod(apiKey = "app.Login.forget_code")
    public Result forgetCode(MainVo vo,String tel,String keyCode,Integer type,String e_mail,String password,String cpc,Integer country_num)
    {
        try
        {
            appsUserLoginService.validatePcodeAndUpdatePassword(vo, tel,keyCode,type,e_mail,password,cpc,country_num, GloabConst.VcodeCategory.UPDATE_PWD_CODE);
            return Result.success();
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("忘记密码,重置密码")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "phone", value = "手机号", required = true, dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "keyCode", value = "验证码", required = true, dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "password", value = "密码", required = true, dataType = "string", paramType = "form")
    })
    @PostMapping("forgotpassword")
    @HttpApiMethod(urlMapping = "app.login.forgotpassword")
    public Result forgotPassword(MainVo vo, String phone, String keyCode, String password)
    {
        try
        {
            return Result.success(appsUserLoginService.forgotpassword(vo, phone, keyCode, password));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("退出登陆")
    @PostMapping("quit")
    @HttpApiMethod(urlMapping = "app.login.quit")
    public Result quit(MainVo vo)
    {
        try
        {
            return Result.success(appsUserLoginService.quit(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("修改用户推荐人")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "phone", value = "手机号", required = true, dataType = "string", paramType = "form")
    })
    @PostMapping("chang_pid")
    @HttpApiMethod(urlMapping = "app.login.chang_pid")
    public Result changPid(MainVo vo, @ParamsMapping("pid") String pid)
    {
        try
        {
            appsUserLoginService.changPid(vo, pid);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("小程序授权登录/注册参数")
    @PostMapping("/appletsParam")
    @HttpApiMethod(urlMapping = {"app.login.appletsParam"})
    public Result appletsParam(AppletsVo vo)
    {
        try
        {
            return Result.success(appsUserLoginService.appletsParam(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("小程序授权登录/注册")
    @PostMapping("/applets")
    @HttpApiMethod(urlMapping = {"app.login.user"})
    public Result applets(AppletsVo vo)
    {
        try
        {
            return Result.success(appsUserLoginService.applets(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("【微信公众号】授权登录/注册")
    @PostMapping("/appletsWx")
    @HttpApiMethod(urlMapping = {"app.login.appletsWx"})
    public Result appletsWx(AppletsVo vo)
    {
        try
        {
            return Result.success(appsUserLoginService.appletsWx(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("小程序授权获取用户手机")
    @PostMapping("/getWxPhone")
    @HttpApiMethod(urlMapping = {"app.login.getWxInfo"})
    public Result getWxPhone(WxAuthPhoneVo vo)
    {
        try
        {
            return Result.success(appsUserLoginService.getWxPhone(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("小程序验证用户Token ")
    @PostMapping("/tokenAccess")
    @HttpApiMethod(urlMapping = {"app.login.login_access"})
    public Result tokenAccess(MainVo vo, @ParamsMapping("wx_id") String openid)
    {
        try
        {
            Map<String, Object> result = appsUserLoginService.tokenAccess(vo, openid);
            return Result.success(result);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("获取微信appid")
    @PostMapping("/getWxAppId")
    @HttpApiMethod(urlMapping = {"app.login.getWxAppId"})
    public Result getWxAppId(MainVo vo)
    {
        try
        {
            return Result.success(appsUserLoginService.getWxAppId(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("新增、修改阿里用户")
    @PostMapping("/aliUser")
    @HttpApiMethod(urlMapping = {"app.login.updateAliUser"})
    public Result synuser(AlipayUserVo vo)
    {
        try
        {
            return Result.success(appsUserLoginService.aliUser(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("阿里用户登陆")
    @PostMapping("/aliUserLogin")
    @HttpApiMethod(urlMapping = {"app.login.mpaliUserLogin"})
    public Result aliUserLogin(MainVo vo, String alimp_auth_code)
    {
        try
        {

            return Result.success(appsUserLoginService.aliUserLogin(vo, alimp_auth_code, true));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("阿里用户登陆")
    @PostMapping("/aliUserLoginApp")
    @HttpApiMethod(urlMapping = {"app.login.aliUserLoginApp"})
    public Result aliUserLoginApp(MainVo vo, String authCode)
    {
        try
        {
            return Result.success(appsUserLoginService.aliUserLogin(vo, authCode, false));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("阿里用户登陆-app(非小程序场景)")
    @PostMapping("/aliUserLoginByApp")
    @HttpApiMethod(urlMapping = {"app.login.aliUserLoginByApp"})
    public Result aliUserLoginByApp(MainVo vo)
    {
        try
        {
            return Result.success(appsUserLoginService.aliUserLoginByApp(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("阿里用户登陆-web(非小程序场景)")
    @PostMapping("/aliUserLoginByWeb")
    @HttpApiMethod(urlMapping = {"app.login.aliUserLoginByWeb"})
    public Result aliUserLoginByWeb(MainVo vo)
    {
        try
        {
            return Result.success(appsUserLoginService.aliUserLoginByWeb(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("【微信公众号,APP】 用户绑定微信")
    @PostMapping("/bindWechat")
    @HttpApiMethod(urlMapping = {"app.user.bindWechat"})
    public Result bindWechat(AppletsVo vo)
    {
        try
        {
            appsUserLoginService.bindWechat(vo);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }
}
