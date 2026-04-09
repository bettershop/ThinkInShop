package com.laiketui.apps.app.controller;

import com.laiketui.apps.api.app.AppsCstrLiveBroadcastService;
import com.laiketui.core.domain.Result;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.root.annotation.HttpApiMethod;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 直播
 *
 * @author Trick
 * @date 2020/12/16 16:37
 */
@RestController
@RequestMapping("/app/live")
public class AppsCstrLiveBroadcastController
{

    @Autowired
    private AppsCstrLiveBroadcastService appsCstrLiveBroadcastService;

    @ApiOperation("直播列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "roomId", value = "房间id", dataType = "string", paramType = "form")
    })
    @PostMapping("/getLiveList")
    @HttpApiMethod(urlMapping = {"app.liveBroadcast.getLiveList"})
    public Result getLiveList(MainVo vo, String roomId)
    {
        try
        {
            return Result.success(appsCstrLiveBroadcastService.getLiveBroadcastList(vo, roomId));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

}
