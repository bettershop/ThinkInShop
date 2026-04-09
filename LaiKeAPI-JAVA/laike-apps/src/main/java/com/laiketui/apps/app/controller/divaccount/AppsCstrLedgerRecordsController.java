package com.laiketui.apps.app.controller.divaccount;

import com.laiketui.apps.api.app.divaccount.AppsCstrLedgerRecordsService;
import com.laiketui.core.domain.Result;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.root.annotation.HttpApiMethod;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zhuqingyu
 * @create 2024/3/13
 */
@Api(tags = "手机端分账记录")
@RestController
@RequestMapping("/app/ledgerRecords")
public class AppsCstrLedgerRecordsController
{

    @Autowired
    private AppsCstrLedgerRecordsService appsCstrLedgerRecordsService;


    @ApiOperation("手机端分账记录")
    @PostMapping("/queryLedgerRecord")
    @HttpApiMethod(urlMapping = "mch.App.LedgerRecords.queryLedgerRecord")
    public Result divideRecord(MainVo vo, Integer mchId, String condition, String startDate, String endDate)
    {
        try
        {
            return Result.success(appsCstrLedgerRecordsService.queryLedgerRecord(vo, mchId, condition, startDate, endDate));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }
}
