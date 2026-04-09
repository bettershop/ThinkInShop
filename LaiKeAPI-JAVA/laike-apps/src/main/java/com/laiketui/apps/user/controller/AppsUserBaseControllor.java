package com.laiketui.apps.user.controller;

import com.laiketui.apps.api.user.AppsUserBaseService;
import com.laiketui.apps.user.consts.AppsUserConst;
import com.laiketui.common.utils.tool.cache.RedisDataTool;
import com.laiketui.core.annotation.ParamsMapping;
import com.laiketui.core.cache.RedisUtil;
import com.laiketui.core.domain.Result;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.UserLoginVo;
import com.laiketui.domain.vo.Withdrawals1Vo;
import com.laiketui.domain.vo.user.AddBankVo;
import com.laiketui.domain.vo.user.SycnUserVo;
import com.laiketui.domain.vo.user.UserRegisterVo;
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
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 用户控制类
 *
 * @author Trick
 * @date 2020/9/23 10:12
 */
@Api(tags = "用户接口")
@RestController
@RequestMapping("/app/user/base")
@Validated
public class AppsUserBaseControllor
{

    @Autowired
    AppsUserBaseService appsUserBaseService;

    @Autowired
    RedisUtil redisUtil;

    @ApiOperation("用户注册")
    @PostMapping("/registerUser")
    @HttpApiMethod(urlMapping = {"app.Login.user_register", "app.Login.register"})
    public Result registerUser(UserRegisterVo vo)
    {
        try
        {
            return Result.success(appsUserBaseService.insertUser(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    //h5
    @ApiOperation("用户邮箱密码登陆")
    @PostMapping("/loginUser")
    @HttpApiMethod(urlMapping = "app.login.login")
    public Result loginUser(UserLoginVo vo)
    {
        try
        {
            return Result.success(appsUserBaseService.login(vo));
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
            @ApiImplicitParam(name = "pid", value = "推荐人", required = true, dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "clientid", value = "推送客户id", required = true, dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "cpc", value = "区号", required = true, dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "country_num", value = "国家代码", required = true, dataType = "Integer", paramType = "form"),
            @ApiImplicitParam(name = "type", value = "登录类型 1：手机号 2：邮箱", required = true, dataType = "Integer", paramType = "form")
    })
    @PostMapping("/loginBySms")
    @HttpApiMethod(urlMapping = {"app.register.login"}, version = "1.0.0", login = false, module = AppsUserConst.MODULE_NAME)
    public Result loginSmsUser(MainVo vo, String phone, @ParamsMapping("keyCode") String pcode, String pid, String clientid,String cpc,Integer country_num,Integer type)
    {
        try
        {
            return Result.success(appsUserBaseService.loginSms(vo, phone, pcode, pid, clientid,cpc,country_num,type));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("发送短信")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "phone", value = "手机号", required = true, dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "smsType", value = "短信类型(1.登陆 2.注册 3.修改手机号 4.修改密码)", required = true, dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "storeId", value = "商户id", required = true, dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "cpc", value = "区号", required = true, dataType = "string", paramType = "form")
    })
    @PostMapping("/sendSMS")
    @HttpApiMethod(urlMapping = {"app.user.secret_key", "app.index.index1", "app.index1.index1"}, version = "1.0.0", login = false, module = AppsUserConst.MODULE_NAME)
    public Result sendSms(@ParamsMapping({"tel", "mobile"}) String phone, @ParamsMapping("message_type1") Integer smsType, @ParamsMapping("store_id") int storeId,String cpc)
    {
        try
        {
            if (smsType == null)
            {
                smsType = GloabConst.VcodeCategory.CURRENCY_CODE;
            }
            if (appsUserBaseService.sendSms(phone, smsType, storeId,cpc))
            {
                return Result.success("SUCCESS");
            }
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
        return Result.error("FIAL");
    }

    @ApiOperation("修改用户信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "access_id", value = "授权id", required = true, dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "nickname", value = "用户名称", required = false, dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "image", value = "图片 file", required = false, dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "birthday", value = "用户出生日期", required = false, dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "sex", value = "性别 1=女 2=男", required = false, dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "store_type", value = "来源", required = true, dataType = "byte", paramType = "form"),
            @ApiImplicitParam(name = "e_mail", value = "邮箱", required = true, dataType = "String", paramType = "form"),
            @ApiImplicitParam(name = "keyCode", value = "验证码", required = false, dataType = "String", paramType = "form")
    })
    @PostMapping("/setUser")
    @HttpApiMethod(urlMapping = {"app.user.set_user"}, login = true, module = AppsUserConst.MODULE_NAME)
    public Result updateUserInfo(MainVo vo, Integer sex, String birthday, @ParamsMapping("Nickname") String nickname, String e_mail,MultipartFile image,String keyCode)
    {
        try
        {
            if (appsUserBaseService.updateUser(vo, sex, birthday, nickname, image,e_mail,keyCode))
            {
                return Result.success("SUCCESS");
            }
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
        return Result.error("FIAL");
    }

    @ApiOperation("图片上传 java")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "access_id", value = "授权令牌", required = true, dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "uploadType", value = "上传类型 1=本地,2=阿里云oss", required = true, dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "files", value = "图片集合 file", required = false, dataType = "string", paramType = "form")
    })
    @PostMapping("/uploadImages")
    @HttpApiMethod(urlMapping = {"app.user.uploadImages"}, login = false, module = AppsUserConst.MODULE_NAME)
    public Result uploadImage(MainVo vo, String uploadType, @NotNull MultipartFile[] files)
    {
        try
        {
            if (files != null && files.length > 0)
            {
                return Result.success(appsUserBaseService.uploadImage(vo.getStoreId(), vo.getStoreType(), uploadType, files));
            }
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
        return Result.error("FIAL");
    }

    @ApiOperation("修改密码")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "password", value = "旧密码", required = true, dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "newPwd", value = "新密码", required = true, dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "confirm", value = "确认密码", required = true, dataType = "string", paramType = "form")
    })
    @PostMapping("/updatepassword")
    @HttpApiMethod(urlMapping = {"app.user.updatepassword", "app.index.updatepassword"}, login = false, module = AppsUserConst.MODULE_NAME)
    public Result updatePassword(MainVo vo, @NotNull String password, @NotNull String newPwd, @NotNull String confirm)
    {
        try
        {
            return Result.success(appsUserBaseService.updatePassword(vo, password, newPwd, confirm));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("设置支付密码")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "password", value = "密码", required = true, dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "keyCode", value = "验证码", required = true, dataType = "string", paramType = "form")
    })
    @PostMapping("/set_payment_password")
    @HttpApiMethod(urlMapping = "app.user.set_payment_password")
    public Result setPayPassword(MainVo vo, String password, String keyCode)
    {
        try
        {
            appsUserBaseService.setPaymentPassword(vo, password, keyCode);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("设置")
    @PostMapping("/set")
    @HttpApiMethod(urlMapping = "app.user.set")
    public Result set(MainVo vo)
    {
        try
        {
            Map<String, Object> result = appsUserBaseService.set(vo);
            return Result.success(result);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("验证支付密码")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "password", value = "密码", required = true, dataType = "string", paramType = "form")
    })
    @PostMapping("/paymentPassword")
    @HttpApiMethod(urlMapping = "app.user.payment_password")
    public Result paymentPassword(MainVo vo, @NotNull String password)
    {
        try
        {
            return Result.success(appsUserBaseService.paymentPassword(vo, password));
        }
        catch (LaiKeAPIException e)
        {
            Map<String, Object> errorMap = new HashMap<>(16);
            errorMap.put("enterless", true);
            return Result.error(e.getCode(), e.getMessage(), errorMap);
        }
    }

    @ApiOperation("我的钱包主页")
    @PostMapping("/details")
    @HttpApiMethod(urlMapping = "app.user.details")
    public Result getUserWallet(MainVo vo)
    {
        try
        {
            return Result.success(appsUserBaseService.getUserWallet(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("我的钱包加载更多")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", value = "分页", required = false, dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "type", value = "1=充值明细 2=提醒明细", required = false, dataType = "int", paramType = "form")
    })
    @PostMapping("/wallet_detailed")
    @HttpApiMethod(urlMapping = "app.user.wallet_detailed")
    public Result walletDetailed(MainVo vo, Integer type)
    {
        try
        {
            return Result.success(appsUserBaseService.getUserWalletDetail(vo, type));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("我的钱包记录详情")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "记录详情id", required = false, dataType = "string", paramType = "form")
    })
    @PostMapping("/getRecordDetails")
    @HttpApiMethod(urlMapping = "app.user.getRecordDetails")
    public Result getRecordDetails(MainVo vo, Integer id)
    {
        try
        {
            return Result.success(appsUserBaseService.getRecordDetails(vo, id));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("修改支付密码")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "x_password", value = "密码", required = true, dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "phoneNum", value = "手机号", required = true, dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "keyCode", value = "验证码", required = true, dataType = "string", paramType = "form")
    })
    @PostMapping("/modify_payment_password")
    @HttpApiMethod(urlMapping = "app.user.modify_payment_password")
    public Result updatePayPassword(MainVo vo, @ParamsMapping("x_password") String password,
                                    String phoneNum, String keyCode)
    {
        try
        {
            return Result.success(appsUserBaseService.updatePayPassword(vo, password, phoneNum, keyCode));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("设置登陆密码")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "password", value = "密码", required = true, dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "phone", value = "手机号", required = true, dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "keyCode", value = "验证码", required = true, dataType = "string", paramType = "form")
    })
    @PostMapping("/set_password")
    @HttpApiMethod(urlMapping = "app.user.set_password")
    public Result setPassword(MainVo vo, String password, String phone, String keyCode)
    {
        try
        {
            return Result.success(appsUserBaseService.setPassword(vo, password, phone, keyCode));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("修改手机号")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "password", value = "密码", required = true, dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "x_phone", value = "新手机号", required = true, dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "keyCode", value = "验证码", required = true, dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "cpc", value = "区号", required = true, dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "country_num", value = "国家代码", required = true, dataType = "int", paramType = "form")
    })
    @PostMapping("/update_phone")
    @HttpApiMethod(urlMapping = "app.user.update_phone")
    public Result updatePhone(MainVo vo, @ParamsMapping("x_phone") String phoneNew, String keyCode,String cpc,Integer country_num)
    {
        try
        {
            return Result.success(appsUserBaseService.updatePhone(vo, phoneNew, keyCode,cpc,country_num));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("关于我们")
    @PostMapping("/about_us")
    @HttpApiMethod(urlMapping = "app.user.about_us")
    public Result aboutUs(MainVo vo)
    {
        try
        {
            return Result.success(appsUserBaseService.aboutUs(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("进入提现页面")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "shop_id", value = "店铺id", required = true, dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "pluginType", value = "插件提现", required = true, dataType = "int", paramType = "form")
    })
    @PostMapping("/into_wallet1")
    @HttpApiMethod(urlMapping = "app.user.into_withdrawals")
    public Result intoWallet1(MainVo vo, @ParamsMapping("shop_id") Integer shopId, String pluginType)
    {
        try
        {
            return Result.success(appsUserBaseService.intoWallet1(vo, shopId, pluginType));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("申请提现")
    @PostMapping("/withdrawals")
    @HttpApiMethod(urlMapping = {"app.user.withdrawals1", "app.user.withdrawals"})
    public Result withdrawals1(Withdrawals1Vo vo)
    {
        try
        {
            appsUserBaseService.withdrawals1(vo);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("验证银行卡与银行名称是否匹配")
    @PostMapping("/Verification")
    @HttpApiMethod(urlMapping = "app.user.Verification")
    public Result verificationBank(Withdrawals1Vo vo, @ParamsMapping("Bank_name") String bankName, @ParamsMapping("Bank_card_number") String bankCardNumber)
    {
        try
        {
            return Result.success(appsUserBaseService.verificationBank(vo, bankName, bankCardNumber));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("获取我的银行卡列表")
    @PostMapping("/bankList")
    @HttpApiMethod(urlMapping = "app.user.bank_list")
    public Result bankList(MainVo vo)
    {
        try
        {
            return Result.success(appsUserBaseService.bankList(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("获取银行详情信息")
    @PostMapping("/getBankDetail")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "bankId", value = "银行卡id", required = true, dataType = "int", paramType = "form")
    })
    @HttpApiMethod(urlMapping = "app.user.getBankDetail")
    public Result getBankDetail(MainVo vo, int bankId)
    {
        try
        {
            return Result.success(appsUserBaseService.getBankDetail(vo, bankId));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("添加用户银行卡")
    @PostMapping("/addBank")
    @HttpApiMethod(urlMapping = "app.user.add_bank")
    public Result addBank(AddBankVo vo)
    {
        try
        {
            return Result.success(appsUserBaseService.addBank(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("设置默认银行卡")
    @PostMapping("/setDefaultBank")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "bankId", value = "银行卡id", required = true, dataType = "int", paramType = "form")
    })
    @HttpApiMethod(urlMapping = "app.user.setDefaultBank")
    public Result setDefaultBank(MainVo vo, int bankId)
    {
        try
        {
            appsUserBaseService.setDefaultBank(vo, bankId);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("银行卡解绑")
    @PostMapping("/delBank")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "bankId", value = "银行卡id", required = true, dataType = "int", paramType = "form")
    })
    @HttpApiMethod(urlMapping = {"app.user.del_bank"})
    public Result delBank(MainVo vo, @ParamsMapping("id") int bankId)
    {
        try
        {
            appsUserBaseService.delBank(vo, bankId);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("请求我的数据")
    @PostMapping("/userIndex")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "mobile", value = "手机号", required = false, dataType = "String", paramType = "form")
    })
    @HttpApiMethod(urlMapping = "app.user.index")
    public Result index(MainVo vo, String mobile)
    {
        try
        {
            return Result.success(appsUserBaseService.index(vo, mobile));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("小程序用户绑定手机号")
    @PostMapping("/bindPhone")
    @HttpApiMethod(urlMapping = {"app.user.bind_phone"})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "x_phone", value = "手机号", required = false, dataType = "String", paramType = "form")
    })
    public Result bindPhone(MainVo vo, @ParamsMapping("x_phone") String phone)
    {
        try
        {
            appsUserBaseService.bindPhone(vo, phone);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("微信小程序用户合并")
    @PostMapping("/synchronizeAccount")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "mobile", value = "手机号", required = true, dataType = "String", paramType = "form"),
            @ApiImplicitParam(name = "source", value = "来源 1.微信小程序 2.支付宝小程序", required = true, dataType = "String", paramType = "form")
    })
    @HttpApiMethod(urlMapping = {"app.user.synchronizeAccount"})
    public Result synchronizeAccount(MainVo vo, String mobile, Integer source)
    {
        try
        {
            appsUserBaseService.synchronizeAccount(vo, mobile, source);
            return Result.success(RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("第三方用户信息同步")
    @PostMapping("/synuser")
    @HttpApiMethod(urlMapping = {"app.user.synuser"})
    public Result synuser(SycnUserVo sycnUserVo)
    {
        try
        {
            return Result.success(appsUserBaseService.synuser(sycnUserVo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("用户解除与微信的绑定")
    @PostMapping("/wxUnbind")
    @HttpApiMethod(urlMapping = {"app.user.wxUnbind"})
    public Result wxUnbind(MainVo vo)
    {
        try
        {
            appsUserBaseService.wxUnbind(vo);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("用户绑定微信 小程序")
    @PostMapping("/wxBind")
    @HttpApiMethod(urlMapping = {"app.user.wxBind"})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "openId", value = "openId", required = true, dataType = "String", paramType = "form"),
            @ApiImplicitParam(name = "wxName", value = "微信名称", required = true, dataType = "String", paramType = "form")
    })
    public Result wxBind(MainVo vo, String openId, String wxName)
    {
        try
        {
            appsUserBaseService.wxBind(vo, openId, wxName);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("请求我的-推荐产品数据")
    @PostMapping("/myRecommendation")
    @HttpApiMethod(urlMapping = "app.user.myRecommendation")
    public Result myRecommendation(MainVo vo)
    {
        try
        {
            return Result.success(appsUserBaseService.getMyRecommendation(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("用户是否已经去完善过资料，修改是否使用的默认用户配置   1是 2否  默认否状态")
    @PostMapping("/isDefaultValue")
    @HttpApiMethod(apiKey = "app.User.isDefaultValue")
    public Result isDefaultValue(MainVo vo)
    {
        try
        {
            appsUserBaseService.isDefaultValue(vo);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("/发送邮件验证码")
    @PostMapping("/sendEmail")
    @ApiImplicitParam(name = "邮箱",value = "email",dataType = "String",paramType = "form",required = true)
    @HttpApiMethod(apiKey = "app.User.send_email_verification_code")
    public Result sendEmail(String email,MainVo vo)
    {
        try
        {
            appsUserBaseService.sendEmail(email,vo);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("获取区号")
    @PostMapping("/getItuList")
    @HttpApiMethod(apiKey = "app.user.getItuList")
    public Result getItuList(String keyword){
        try
        {
            List<Map<String,Object>> res = appsUserBaseService.getItuList(keyword);
            return Result.success(res);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("获取用户当前选择的货币最新信息")
    @PostMapping("/getUserCurrnetCurrencyInfo")
    @HttpApiMethod(apiKey = "app.user.getUserCurrnetCurrencyInfo")
    public Result getUserCurrnetCurrencyInfo(MainVo vo)
    {
        try
        {
            return Result.success(appsUserBaseService.getUserCurrnetCurrencyInfo(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("获取系统浏览器icon和商城名称")
    @PostMapping("/getSystemIconAndName")
    @HttpApiMethod(apiKey = "app.user.getSystemIconAndName")
    public Result getSystemIconAndName(MainVo vo)
    {
        try
        {
            return Result.success(appsUserBaseService.getSystemIconAndName(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }
}
