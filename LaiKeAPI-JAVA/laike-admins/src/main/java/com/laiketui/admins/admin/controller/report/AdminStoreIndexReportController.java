package com.laiketui.admins.admin.controller.report;


import com.laiketui.admins.api.admin.report.AdminStoreIndexReportService;
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

@Api(tags = "后台报表-商城首页数据")
@RestController
@RequestMapping("/admin/report/storeIndex")
public class AdminStoreIndexReportController
{


    @Autowired
    private AdminStoreIndexReportService adminStoreIndexReportService;

    @ApiOperation("商城首页统计数据")
    @PostMapping("/demo")
    @HttpApiMethod(apiKey = "admin.report.storeIndex")
    public Result index(MainVo vo)
    {
        try
        {
            Map<String, Object> resultMap  = new HashMap<>();
            Map<String, Object> reportData = adminStoreIndexReportService.getReportData(vo.getStoreId());
            resultMap.put("reportData", reportData);
            return Result.success(resultMap);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
        catch (Exception e)
        {
            return Result.error(ErrorCode.BizErrorCode.ERROR_CODE_, "网络异常！");
        }
    }


}
