package com.laiketui.apps.mch.controller;

import com.laiketui.apps.api.mch.AppsMchNoticeService;
import com.laiketui.core.domain.Result;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.domain.vo.MainVo;
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
 * h5店铺消息通知
 * gp
 * 2023-08-31
 */
@Api(tags = "pc店铺-公告管理")
@RestController
@RequestMapping("/app/mch/notice")
public class AppsMchNoticeController
{

    @Autowired
    private AppsMchNoticeService appsMchNoticeService;

    @ApiOperation("店铺消息通知")
    @PostMapping("/noticeList")
    @HttpApiMethod(apiKey = "mch.App.Notice.NoticeList")
    public Result noticeList(MainVo vo)
    {
        try
        {
            return Result.success(appsMchNoticeService.noticeList(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("店铺消息通知详情")
    @PostMapping("/noticeDetails")
    @HttpApiMethod(apiKey = "mch.App.Notice.NoticeDetails")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "消息id", dataType = "int", paramType = "form"),
    })
    public Result noticeDetails(MainVo vo, Integer id)
    {
        try
        {
            return Result.success(appsMchNoticeService.noticeRead(vo, id));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("店铺消息通知获取未读消息条数")
    @PostMapping("/messageNum")
    @HttpApiMethod(apiKey = "mch.notice.messageNum")
    public Result messageNum(MainVo vo)
    {
        try
        {
            return Result.success(appsMchNoticeService.messageNum(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("标记全部以读")
    @PostMapping("/allMessage")
    @HttpApiMethod(apiKey = "mch.App.Notice.AllMessage")
    public Result allMessage(MainVo vo)
    {
        try
        {
            appsMchNoticeService.allMessage(vo);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("删除消息通知")
    @PostMapping("/delMessage")
    @HttpApiMethod(apiKey = "mch.App.Notice.DelMessage")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "ids", value = "消息id", dataType = "String", paramType = "form"),
    })
    public Result delMessage(MainVo vo, String ids)
    {
        try
        {
            appsMchNoticeService.delMessage(vo, ids);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

}
