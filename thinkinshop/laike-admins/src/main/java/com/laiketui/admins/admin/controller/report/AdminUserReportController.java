package com.laiketui.admins.admin.controller.report;

import com.laiketui.admins.api.admin.report.AdminUserReportService;
import com.laiketui.core.domain.Result;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.root.annotation.HttpApiMethod;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;


@Api(tags = "后台报表-用户数据")
@RestController
@RequestMapping("/admin/report/user")
public class AdminUserReportController
{
    @Autowired
    private AdminUserReportService adminUserReportService;


    @ApiOperation("用户统计数据")
    @PostMapping("/userReport")
    @HttpApiMethod(apiKey = "admin.report.userIndex")
    public Result index(MainVo vo)
    {
        Map<String, Object> resultMap = new HashMap<>();
        try
        {
            //活跃用户
            resultMap.put("activeuserData", adminUserReportService.getUserData(vo));
            //各用户端用户统计
            resultMap.put("userCount", adminUserReportService.getUserAmount(vo));
            //新增用户统计
//            resultMap.put("additiondata", userReportService.getAdditionUserData(vo));
            resultMap.put("additiondata", adminUserReportService.additionData(vo).get("additiondata"));
            //会员统计
            resultMap.put("Membership", adminUserReportService.getMembershipStatistics(vo));
            //用户消费排行
            resultMap.put("moneyTop", adminUserReportService.getMoneyTop(vo));
            return Result.success(resultMap);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
        catch (Exception e)
        {
            return Result.error(ErrorCode.BizErrorCode.DATA_ERROR, "网络异常！");
        }

    }

}
