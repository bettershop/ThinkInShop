package com.laiketui.admins.mch.controller;

import com.laiketui.admins.api.mch.MchNoticeService;
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
 * pc店铺消息通知
 * gp
 * 2023-07-11
 */
@Api(tags = "pc店铺-公告管理")
@RestController
@RequestMapping("/admin/mch/notice")
public class MchNoticeController
{

    @Autowired
    private MchNoticeService mchNoticeService;

    @ApiOperation("店铺消息通知")
    @PostMapping("/noticeList")
    @HttpApiMethod(apiKey = "mch.Mch.Notice.NoticeList")
    public Result noticeList(MainVo vo)
    {
        try
        {
            return Result.success(mchNoticeService.noticeList(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("标记通知已读状态")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "消息id", dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "types", value = "消息类型,一键已读,多个类型用,隔开", dataType = "string", paramType = "form"),
    })
    @PostMapping("/noticeRead")
    @HttpApiMethod(apiKey = "mch.Mch.Notice.NoticeRead")
    public Result noticeRead(MainVo vo, Integer id, String types)
    {
        try
        {
            mchNoticeService.noticeRead(vo, id, types);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("标记已弹窗")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "ids", value = "消息id,一键已读,多个类型用,隔开", dataType = "string", paramType = "form"),
    })
    @PostMapping("/noticePopup")
    @HttpApiMethod(apiKey = "admin.mch.notice.noticePopup")
    public Result noticePopup(MainVo vo, String ids)
    {
        try
        {
            mchNoticeService.noticePopup(vo, ids);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

}
