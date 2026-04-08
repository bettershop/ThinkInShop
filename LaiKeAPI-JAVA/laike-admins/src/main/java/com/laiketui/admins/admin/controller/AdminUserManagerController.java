package com.laiketui.admins.admin.controller;

import com.laiketui.admins.api.admin.users.AdminRechargeServive;
import com.laiketui.admins.api.admin.users.AdminUserGradeService;
import com.laiketui.admins.api.admin.users.AdminUserManagerService;
import com.laiketui.admins.api.admin.users.AdminWithdrawalManageService;
import com.laiketui.common.api.cascade.PublicCascadeService;
import com.laiketui.core.domain.Result;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.SaveAddressVo;
import com.laiketui.domain.vo.user.*;
import com.laiketui.root.annotation.HttpApiMethod;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 会员管理
 *
 * @author Trick
 * @date 2021/1/7 10:58
 */
@Api(tags = "后台-用户管理")
@RestController
@RequestMapping("/admin/user")
public class AdminUserManagerController
{

    @Autowired
    private AdminUserManagerService adminUserManagerService;

    @Autowired
    private PublicCascadeService publicCascadeService;

    @ApiOperation("获取会员等级下拉")
    @PostMapping("/goodsStatus")
    @HttpApiMethod(apiKey = "admin.user.goodsStatus")
    public Result goodsStatus(MainVo vo)
    {
        try
        {
            return Result.success(publicCascadeService.getGradeList(vo.getStoreId()));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("获取来源下拉")
    @PostMapping("/getSourceList")
    @HttpApiMethod(apiKey = "admin.user.getSourceList")
    public Result getSourceList(MainVo vo)
    {
        try
        {
            return Result.success(publicCascadeService.getSourceList(vo.getStoreId()));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("获取会员列表")
    @PostMapping("/getUserInfo")
    @HttpApiMethod(apiKey = "admin.user.getUserInfo")
    public Result getUserInfo(UserVo vo, HttpServletResponse response)
    {
        try
        {
            Map<String, Object> ret = adminUserManagerService.getUserInfo(vo, response);
            return ret == null ? Result.exportFile() : Result.success(ret);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("修改会员信息")
    @PostMapping("/updateUserById")
    @HttpApiMethod(apiKey = "admin.user.updateUserById")
    public Result updateUserById(UpdateUserVo vo)
    {
        try
        {
            return Result.success(adminUserManagerService.updateUserById(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("获取会员配置升级方式")
    @PostMapping("/getUserGradeType")
    @HttpApiMethod(apiKey = "admin.user.getUserGradeType")
    public Result getUserGradeType(MainVo vo)
    {
        try
        {
            return Result.success(adminUserManagerService.getUserGradeType(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("会员充值余额")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "用户主键id", dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "money", value = "充值金额", dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "type", value = "1=余额 2=消费金额 3=积分", dataType = "int", paramType = "form", defaultValue = "1"),
            @ApiImplicitParam(name = "remake", value = "备注", dataType = "String", paramType = "form")
    })
    @PostMapping("/userRechargeMoney")
    @HttpApiMethod(apiKey = "admin.user.userRechargeMoney")
    public Result userRechargeMoney(MainVo vo, int id, BigDecimal money, Integer type, String remake)
    {
        try
        {
            return Result.success(adminUserManagerService.userRechargeMoney(vo, id, money, type, remake));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("添加用户")
    @PostMapping("/saveUser")
    @HttpApiMethod(apiKey = "admin.user.saveUser")
    public Result saveUser(AddUserVo vo)
    {
        try
        {
            adminUserManagerService.saveUser(vo);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("批量上传用户")
    @PostMapping("/uploadAddUser")
    @HttpApiMethod(apiKey = "admin.user.uploadAddUser")
    public Result uploadAddUser(MainVo vo, List<MultipartFile> image)
    {
        try
        {
            Boolean flag = adminUserManagerService.uploadAddUser(vo, image);
            if (!flag)
            {
                return Result.error(ErrorCode.BizErrorCode.ERROR_CODE_DRSB,"导入失败");
            }
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("删除上传记录")
    @PostMapping("/delUploadRecord")
    @HttpApiMethod(apiKey = "admin.user.delUploadRecord")
    public Result delUploadRecord(MainVo vo, String id)
    {
        try
        {
            adminUserManagerService.delUploadRecord(vo, id);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("批量上传记录列表")
    @PostMapping("/uploadRecordList")
    @HttpApiMethod(apiKey = "admin.user.uploadRecordList")
    public Result uploadRecordList(MainVo vo, String key, Integer status, String startDate, String endDate)
    {
        try
        {
            return Result.success(adminUserManagerService.uploadRecordList(vo, key, status, startDate, endDate));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("删除用户")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "用户主键id", required = true, dataType = "int", paramType = "form")
    })
    @PostMapping("/delUserById")
    @HttpApiMethod(apiKey = "admin.user.delUserById")
    public Result delUserById(MainVo vo, int id)
    {
        try
        {
            return Result.success(adminUserManagerService.delUserById(vo, id));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("修改用户等级")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userId", value = "用户id", required = true, dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "grade", value = "等级", required = true, dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "gradeType", value = "开通类型 1包月2包年3包季", required = true, dataType = "int", paramType = "form")
    })
    @PostMapping("/updateUserGradeById")
    @HttpApiMethod(apiKey = "admin.user.updateUserGradeById")
    public Result updateUserGradeById(MainVo vo, String userId, int grade, Integer gradeType)
    {
        try
        {
            UpdateUserVo userVo = new UpdateUserVo();
            userVo.setStoreId(vo.getStoreId());
            userVo.setUserId(userId);
            userVo.setGrade(grade);
            userVo.setGradeType(gradeType);
            return Result.success(adminUserManagerService.updateUserById(userVo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @Autowired
    private AdminUserGradeService userGradeService;

    @ApiOperation("获取等级列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "gid", value = "等级id", dataType = "int", paramType = "form")
    })
    @PostMapping("/getUserGradeInfo")
    @HttpApiMethod(apiKey = "admin.user.getUserGradeInfo")
    public Result getUserGradeInfo(MainVo vo, Integer gid)
    {
        try
        {
            return Result.success(userGradeService.getUserGradeInfo(vo, gid));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("获取赠送商品下拉数据")
    @PostMapping("/getGiveGoodsList")
    @HttpApiMethod(apiKey = "admin.user.getGiveGoodsList")
    public Result getGiveGoodsList(MainVo vo)
    {
        try
        {
            return Result.success(userGradeService.getGiveGoodsList(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("添加等级")
    @PostMapping("/addUserGrade")
    @HttpApiMethod(apiKey = "admin.user.addUserGrade")
    public Result addUserGrade(AddUserGradeVo vo)
    {
        try
        {
            userGradeService.addUserGrade(vo);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("删除等级")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "等级id", dataType = "int", paramType = "form")
    })
    @PostMapping("/delUserGrade")
    @HttpApiMethod(apiKey = "admin.user.delUserGrade")
    public Result delUserGrade(MainVo vo, int id)
    {
        try
        {
            return Result.success(userGradeService.delUserGrade(vo, id));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("获取会员配置信息")
    @PostMapping("/getUserConfigInfo")
    @HttpApiMethod(apiKey = "admin.user.getUserConfigInfo")
    public Result getUserConfigInfo(MainVo vo)
    {
        try
        {
            return Result.success(userGradeService.getUserConfigInfo(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("删除/修改配置")
    @PostMapping("/addUserRule")
    @HttpApiMethod(apiKey = "admin.user.addUserRule")
    public Result addUserRule(AddUserRuleVo vo, HttpServletRequest request)
    {
        try
        {
            userGradeService.addUserRule(vo);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @Autowired
    private AdminWithdrawalManageService withdrawalManageService;

    @ApiOperation("获取提现列表")
    @PostMapping("/getWithdrawalInfo")
    @HttpApiMethod(apiKey = "admin.user.getWithdrawalInfo")
    public Result getWithdrawalInfo(WithdrawalVo vo, HttpServletResponse response)
    {
        try
        {
            Map<String, Object> ret = withdrawalManageService.getWithdrawalInfo(vo, response);
            return ret == null ? Result.exportFile() : Result.success(ret);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("获取提现记录列表")
    @PostMapping("/getWithdrawalRecord")
    @HttpApiMethod(apiKey = "admin.user.getWithdrawalRecord")
    public Result getWithdrawalRecord(WithdrawalVo vo, HttpServletResponse response)
    {
        try
        {
            Map<String, Object> ret = withdrawalManageService.getWithdrawalRecord(vo, response);
            return ret == null ? Result.exportFile() : Result.success(ret);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("提现审核(用户钱包)")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "提现id", required = true, dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "status", required = true, value = "状态 0：审核中 1：审核通过 2：拒绝", dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "refuse", value = "拒绝原因", dataType = "string", paramType = "form"),
    })
    @PostMapping("/withdrawalExamine")
    @HttpApiMethod(apiKey = "admin.user.withdrawalExamine")
    public Result withdrawalExamine(MainVo vo, int id, int status, String refuse)
    {
        try
        {
            withdrawalManageService.withdrawalExamine(vo, id, status, refuse);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("获取钱包设置信息")
    @PostMapping("/getWalletInfo")
    @HttpApiMethod(apiKey = "admin.user.getWalletInfo")
    public Result getWalletInfo(MainVo vo)
    {
        try
        {
            return Result.success(withdrawalManageService.getWalletInfo(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("设置钱包设置信息")
    @PostMapping("/setWalletInfo")
    @HttpApiMethod(apiKey = "admin.user.setWalletInfo")
    public Result setWalletInfo(WalletVo vo)
    {
        try
        {
            return Result.success(withdrawalManageService.setWalletInfo(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @Autowired
    private AdminRechargeServive rechargeServive;

    @ApiOperation("获取用户充值记录")
    @PostMapping("/getRechargeInfo")
    @HttpApiMethod(apiKey = "admin.user.getRechargeInfo")
    public Result getRechargeInfo(RechargeVo vo)
    {
        try
        {
            return Result.success(rechargeServive.getRechargeInfo(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("获取用户充值列表")
    @PostMapping("/getupInfo")
    @HttpApiMethod(apiKey = "admin.user.getupInfo")
    public Result getupInfo(RechargeVo vo, HttpServletResponse response)
    {
        try
        {
            Map<String, Object> ret = rechargeServive.getupInfo(vo, response);
            return ret == null ? Result.exportFile() : Result.success(ret);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("获取用户资金记录")
    @PostMapping("/getUserMoneyInfo")
    @HttpApiMethod(apiKey = "admin.user.getUserMoneyInfo")
    public Result getUserMoneyInfo(UserMoneyVo vo, HttpServletResponse response)
    {
        try
        {
            Map<String, Object> ret = rechargeServive.getUserMoneyInfo(vo, response);
            return ret == null ? Result.exportFile() : Result.success(ret);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("获取用户积分记录")
    @PostMapping("/getUserIntegralInfo")
    @HttpApiMethod(apiKey = "admin.user.getUserIntegralInfo")
    public Result getUserIntegralInfo(UserMoneyVo vo, HttpServletResponse response)
    {
        try
        {
            Map<String, Object> ret = rechargeServive.getUserIntegralInfo(vo, response);
            return ret == null ? Result.exportFile() : Result.success(ret);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("获取用户资金详情记录")
    @PostMapping("/getUserMoneyInfo_see")
    @HttpApiMethod(apiKey = "admin.user.getUserMoneyInfo_see")
    public Result getUserMoneyInfo_see(UserMoneyVo vo, HttpServletResponse response)
    {
        try
        {
            Map<String, Object> ret = rechargeServive.getUserMoneyInfo_see(vo, response);
            return ret == null ? Result.exportFile() : Result.success(ret);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("获取用户积分详情记录")
    @PostMapping("/getUserIntegralInfo_see")
    @HttpApiMethod(apiKey = "admin.user.getUserIntegralInfo_see")
    public Result getUserIntegralInfo_see(UserMoneyVo vo, HttpServletResponse response)
    {
        try
        {
            Map<String, Object> ret = rechargeServive.getUserIntegralInfo_see(vo, response);
            return ret == null ? Result.exportFile() : Result.success(ret);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("添加地址")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userId", value = "用户userid", required = true, dataType = "string", paramType = "form"),
    })
    @PostMapping("saveAddress")
    @HttpApiMethod(apiKey = "admin.user.saveAddress")
    public Result saveAddress(SaveAddressVo vo, String userId) throws LaiKeAPIException
    {
        try
        {
            return Result.success(rechargeServive.saveAddress(vo, userId));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("获取区号")
    @PostMapping("/getItuList")
    @HttpApiMethod(apiKey = "admin.user.getItuList")
    public Result getItuList(String keyword){
        try
        {
            List<Map<String,Object>> res = rechargeServive.getItuList(keyword);
            return Result.success(res);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }
    @ApiOperation("生成账号")
    @PostMapping("/generateAccount")
    @HttpApiMethod(apiKey = "admin.user.generateAccount")
    public Result generateAccount()
    {
        try
        {
            String res = rechargeServive.generateAccount();
            return Result.success(res);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }
}
