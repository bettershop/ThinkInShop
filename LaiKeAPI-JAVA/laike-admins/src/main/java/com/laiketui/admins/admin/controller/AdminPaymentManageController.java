package com.laiketui.admins.admin.controller;

import com.laiketui.admins.api.admin.AdminPaymentManageService;
import com.laiketui.core.domain.Result;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.files.UploadFileVo;
import com.laiketui.root.annotation.HttpApiMethod;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 支付管理
 *
 * @author Trick
 * @date 2021/7/15 15:12
 */

@Api(tags = "后台-支付管理")
@RestController
@RequestMapping("/admin/payment")
public class AdminPaymentManageController
{

    @Autowired
    private AdminPaymentManageService adminPaymentManageService;

    @ApiOperation("支付类型列表")
    @PostMapping("/index")
    @HttpApiMethod(apiKey = "admin.payment.index")
    public Result index(MainVo vo, Integer id)
    {
        try
        {
            return Result.success(adminPaymentManageService.index(vo, id));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("编辑支付类型")
    @PostMapping("/setPayment")
    @HttpApiMethod(apiKey = "admin.payment.setPayment")
    public Result setPayment(MainVo vo, Integer id, String Logo, String remark)
    {
        try
        {
            adminPaymentManageService.setPayment(vo, id, Logo, remark);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("编辑支付类型图标上传")
    @PostMapping("/setPaymentLoge")
    @HttpApiMethod(apiKey = "admin.payment.setPaymentLoge")
    public Result setPaymentLoge(MainVo vo, List<MultipartFile> image)
    {
        try
        {
            return Result.success(adminPaymentManageService.setPaymentLoge(vo, image));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("获取支付配置参数")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "支付类型列表id", required = true, dataType = "int", paramType = "form"),
    })
    @PostMapping("/paymentParmaInfo")
    @HttpApiMethod(apiKey = "admin.payment.paymentParmaInfo")
    public Result paymentParmaInfo(MainVo vo, int id)
    {
        try
        {
            return Result.success(adminPaymentManageService.paymentParmaInfo(vo, id));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("设置支付配置参数")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "支付类型列表id", dataType = "int", required = true, paramType = "form"),
            @ApiImplicitParam(name = "status", value = "是否启用 1=启用 0=未启用", dataType = "int", required = true, paramType = "form"),
            @ApiImplicitParam(name = "json", value = "支付配置参数json", dataType = "string", required = true, paramType = "form")
    })
    @PostMapping("/setPaymentParma")
    @HttpApiMethod(apiKey = "admin.payment.setPaymentParma")
    public Result setPaymentParma(MainVo vo, String json, int id, Integer status)
    {
        try
        {
            adminPaymentManageService.setPaymentParma(vo, json, id, status);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("开关")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "支付类型列表id", required = true, dataType = "int", paramType = "form"),
    })
    @PostMapping("/setPaymentSwitch")
    @HttpApiMethod(apiKey = "admin.payment.setPaymentSwitch")
    public Result setPaymentSwitch(MainVo vo, int id)
    {
        try
        {
            adminPaymentManageService.setPaymentSwitch(vo, id);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("设置默认支付方式")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "支付类型列表id", required = true, dataType = "int", paramType = "form"),
    })
    @PostMapping("/settingDefaultPaytype")
    @HttpApiMethod(apiKey = "admin.payment.settingDefaultPaytype")
    public Result settingDefaultPaytype(MainVo vo, int id)
    {
        try
        {
            adminPaymentManageService.settingDefaultPaytype(vo, id);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("微信p12文件上传")
    @PostMapping("/uploadCertP12")
    @HttpApiMethod(apiKey = "admin.payment.uploadCertP12")
    public Result uploadFiles(@RequestParam("file") MultipartFile[] file, UploadFileVo vo)
    {
        try
        {
            vo.setImage(file);
            return Result.success(adminPaymentManageService.uploadCertP12(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

}
