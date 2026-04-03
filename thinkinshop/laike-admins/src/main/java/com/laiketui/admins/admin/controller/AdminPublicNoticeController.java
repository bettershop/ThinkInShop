package com.laiketui.admins.admin.controller;

import com.laiketui.admins.api.admin.notie.AdminPublicNoticeService;
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

import java.util.Map;

/**
 * 公告管理
 *
 * @author Trick
 * @date 2021/1/19 15:43
 */
@Api(tags = "后台-公告管理")
@RestController
@RequestMapping("/admin/notice")
public class AdminPublicNoticeController
{

    @Autowired
    private AdminPublicNoticeService adminPublicNoticeService;

    @ApiOperation("获取售后列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "公告id", dataType = "int", paramType = "form")
    })
    @PostMapping("/getPublicNoticeInfo")
    @HttpApiMethod(apiKey = "admin.notice.getPublicNoticeInfo")
    public Result getPublicNoticeInfo(MainVo vo, Integer id)
    {
        try
        {
            Map<String, Object> result = adminPublicNoticeService.getPublicNoticeInfo(vo, id, null);
            return Result.success(result);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("商城消息通知")
    @PostMapping("/noticeList")
    @HttpApiMethod(apiKey = "admin.notice.noticeList")
    public Result noticeList(MainVo vo)
    {
        try
        {
            return Result.success(adminPublicNoticeService.noticeList(vo));
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
    @HttpApiMethod(apiKey = "admin.notice.noticeRead")
    public Result noticeRead(MainVo vo, Integer id, String types)
    {
        try
        {
            adminPublicNoticeService.noticeRead(vo, id, types);
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
    @HttpApiMethod(apiKey = "admin.notice.noticePopup")
    public Result noticePopup(MainVo vo, String ids)
    {
        try
        {
            adminPublicNoticeService.noticePopup(vo, ids);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

}
