package com.laiketui.admins.admin.controller;

import com.laiketui.admins.api.admin.AdminReportService;
import com.laiketui.core.domain.Result;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.admin.MchReportVo;
import com.laiketui.root.annotation.HttpApiMethod;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 报表管理
 *
 * @author Trick
 * @date 2021/7/5 10:35
 */
@Api(tags = "报表管理")
@RestController
@RequestMapping("/admin/turnover")
public class AdminReportController
{

    @Autowired
    private AdminReportService adminReportService;

    @ApiOperation("商户营业报表")
    @PostMapping("/mchTurnoverReport")
    @HttpApiMethod(apiKey = "admin.turnover.mchTurnoverReport")
    public Result index(MchReportVo vo)
    {
        try
        {
            return Result.success(adminReportService.mchTurnoverReport(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("商户新增用户报表")
    @PostMapping("/mchTurnoverNewUserReport")
    @HttpApiMethod(apiKey = "admin.turnover.mchTurnoverNewUserReport")
    public Result mchTurnoverNewUserReport(MchReportVo vo)
    {
        try
        {
            return Result.success(adminReportService.mchTurnoverNewUserReport(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("商户订单报表")
    @PostMapping("/mchTurnoverOrderReport")
    @HttpApiMethod(apiKey = "admin.turnover.mchTurnoverOrderReport")
    public Result mchTurnoverOrderReport(MchReportVo vo)
    {
        try
        {
            return Result.success(adminReportService.mchTurnoverOrderReport(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

}
