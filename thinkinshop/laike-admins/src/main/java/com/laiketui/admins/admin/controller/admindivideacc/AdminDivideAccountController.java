package com.laiketui.admins.admin.controller.admindivideacc;

import com.laiketui.admins.api.admin.admindivideacc.AdminDivideAccountsService;
import com.laiketui.core.domain.Result;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.divideAccount.DivideAccountVo;
import com.laiketui.root.annotation.HttpApiMethod;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

/**
 * @Author: sunH_
 * @Date: Create in 15:35 2023/9/14
 */
@Api(tags = "分账管理")
@RestController
@RequestMapping("/admin/divideAccount")
public class AdminDivideAccountController
{
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private AdminDivideAccountsService adminDivideAccountsService;

    @ApiOperation("获取店铺分账信息")
    @PostMapping("/divideAccountInfo")
    @HttpApiMethod(urlMapping = "admin.divideAccount.divideAccountInfo")
    public Result divideAccountInfo(MainVo vo, Integer mchId)
    {
        try
        {
            return Result.success(adminDivideAccountsService.divideAccountInfo(vo, mchId));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("保存店铺分账信息")
    @PostMapping("/saveDivideAccount")
    @HttpApiMethod(urlMapping = "admin.divideAccount.saveDivideAccount")
    public Result saveDivideAccount(DivideAccountVo vo)
    {
        try
        {
            adminDivideAccountsService.saveDivideAccount(vo);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("申请分账账单")
    @PostMapping("/applyBilling")
    @HttpApiMethod(urlMapping = "admin.divideAccount.applyBilling")
    public Result applyBilling(MainVo vo, String date)
    {
        try
        {
            return Result.success(adminDivideAccountsService.applyBilling(vo, date));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("下载分账账单")
    @PostMapping("/downloadBilling")
    @HttpApiMethod(urlMapping = "admin.divideAccount.downloadBilling")
    public Result downloadBilling(MainVo vo, String url, HttpServletResponse httpServletResponse)
    {
        try
        {

            adminDivideAccountsService.downloadBilling(vo, url, httpServletResponse);
            return Result.exportFile();
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("分账记录")
    @PostMapping("/divideRecord")
    @HttpApiMethod(urlMapping = "admin.divideAccount.divideRecord")
    public Result divideRecord(MainVo vo, Integer mchId, String condition, String startDate, String endDate, HttpServletResponse response)
    {
        try
        {
            return Result.success(adminDivideAccountsService.divideRecord(vo, mchId, condition, startDate, endDate, response));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }
}
