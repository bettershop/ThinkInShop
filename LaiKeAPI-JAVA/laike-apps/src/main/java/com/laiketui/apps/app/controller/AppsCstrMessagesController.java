package com.laiketui.apps.app.controller;

import com.laiketui.apps.api.app.AppsCstrMessagesService;
import com.laiketui.core.annotation.ParamsMapping;
import com.laiketui.core.domain.Result;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.order.NoticeModel;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.root.annotation.HttpApiMethod;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotNull;
import java.util.Map;

/**
 * 我的消息
 *
 * @author Trick
 * @date 2020/10/16 16:43
 */
@Api(tags = "我的消息Api")
@RestController
@Validated
@RequestMapping("/app/msg")
public class AppsCstrMessagesController
{

    @Autowired
    AppsCstrMessagesService appsCstrMessagesService;

    @ApiOperation("消息列表api")
    @PostMapping("msgIndex")
    @HttpApiMethod(urlMapping = "app.message.index")
    public Result index(MainVo vo)
    {
        try
        {
            Map<String, Object> resultMap = appsCstrMessagesService.index(vo);
            return Result.success(resultMap);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("消息后续加载api")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", value = "页数", required = true, dataType = "int", paramType = "form")
    })
    @PostMapping("more")
    @HttpApiMethod(urlMapping = "app.message.more")
    public Result more(MainVo vo, @ParamsMapping("page") int page)
    {
        try
        {
            return Result.success(appsCstrMessagesService.more(vo, page));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("一键标记全读api")
    @PostMapping("msgAll")
    @HttpApiMethod(urlMapping = "app.message.all")
    public Result all(MainVo vo)
    {
        try
        {
            return Result.success(appsCstrMessagesService.all(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("获取微信模板api")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "storeId", value = "商城id", required = true, dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "accessId", value = "授权id", required = true, dataType = "String", paramType = "form"),
            @ApiImplicitParam(name = "language", value = "语言", required = true, dataType = "String", paramType = "form")
    })
    @PostMapping("getWxTemplates")
    public Result getWxTemplates(@NotNull int storeId, @NotNull String language, @NotNull String accessId)
    {
        try
        {
            NoticeModel noticeModel = appsCstrMessagesService.getWxTemplates(storeId, language, accessId);
            return Result.success(noticeModel);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("消息详情")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "消息id", required = true, dataType = "int", paramType = "form")
    })
    @PostMapping("msgOneindex")
    @HttpApiMethod(urlMapping = "app.message.oneindex")
    public Result oneindex(MainVo vo, Integer id)
    {
        try
        {
            return Result.success(appsCstrMessagesService.oneindex(vo, id));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("删除消息api")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "消息id集合','隔开", required = true, dataType = "String", paramType = "form")
    })
    @PostMapping("msgDel")
    @HttpApiMethod(urlMapping = "app.message.del")
    public Result del(MainVo vo, String id)
    {
        try
        {
            return Result.success(appsCstrMessagesService.del(vo, id));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("小程序获取微信订阅消息模板")
    @PostMapping("getMessageIds")
    @HttpApiMethod(urlMapping = "app.message.getWXTemplates")
    public Result getMessageIds(MainVo vo)
    {
        try
        {
            return Result.success(appsCstrMessagesService.getMessageIds(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("是否客服有未读消息")
    @PostMapping("messageNotReade")
    @HttpApiMethod(urlMapping = "app.message.messageNotReade")
    public Result messageNotReade(MainVo vo)
    {
        try
        {
            return Result.success(appsCstrMessagesService.messageNotReade(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }
}
