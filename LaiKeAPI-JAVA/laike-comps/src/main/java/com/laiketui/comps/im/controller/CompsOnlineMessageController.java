package com.laiketui.comps.im.controller;

import com.laiketui.common.api.PublicOrderService;
import com.laiketui.comps.api.im.CompsOnlineMessageService;
import com.laiketui.core.domain.Result;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.OrderVo;
import com.laiketui.domain.vo.onlinemessage.AddOnlineMessageVo;
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

@Api(tags = "H5在线消息api")
@RestController
@RequestMapping("/comps/onlineMessage")
public class CompsOnlineMessageController
{
    @Autowired
    private CompsOnlineMessageService compsOnlineMessageService;

    @Autowired
    private PublicOrderService orderService;

    @ApiOperation("查询用户与店铺在线消息")
    @PostMapping("/getMessageList")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userId", value = "用户id", dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "mchId", value = "店铺id", dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "type", value = "查询类型", dataType = "int", paramType = "form"),
    })
    @HttpApiMethod(urlMapping = {"app.msg.getMessageList", "mall.Msg.getMessageList", "mch.Mch.Msg.getMessageList"})
    public Result getMessageList(MainVo vo, String userId, String mchId, Integer type)
    {
        try
        {
            return Result.success(compsOnlineMessageService.getMessageList(vo, userId, mchId, type));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("添加在线消息")
    @PostMapping("/addMessage")
    @HttpApiMethod(urlMapping = {"app.msg.addMessage", "mall.Msg.addMessage", "mch.Mch.Msg.addMessage"})
    public Result addMessage(AddOnlineMessageVo addOnlineMessageVo)
    {
        try
        {
            compsOnlineMessageService.addMessage(addOnlineMessageVo);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("查询店铺会话的用户列表")
    @PostMapping("/mch_userList")
    @HttpApiMethod(urlMapping = {"app.msg.mch_userList", "mch.Mch.Msg.mch_userList"})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "mchId", value = "店铺id", dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "userName", value = "用户名称", dataType = "int", paramType = "form")
    })
    public Result mchUserList(MainVo vo, String mchId, String userName)
    {
        try
        {
            return Result.success(compsOnlineMessageService.mchUserList(vo, mchId, userName));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("查询用户会话的店铺列表")
    @PostMapping("/user_mchList")
    @HttpApiMethod(urlMapping = {"app.msg.user_mchList", "mall.Msg.user_mchList"})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userId", value = "用户id", dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "mchId", value = "店铺id", dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "mchName", value = "店铺名称", dataType = "int", paramType = "form"),
    })
    public Result userMchList(MainVo vo, String userId, String mchId, String mchName)
    {
        try
        {
            return Result.success(compsOnlineMessageService.userMchList(vo, userId, mchId, mchName));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("客服页面获取订单列表")
    @PostMapping("/customerOrderIndex")
    @HttpApiMethod(urlMapping = "app.customer.orderIndex")
    public Result customerOrderIndex(OrderVo vo){
        try
        {
            Map<String, Object> resultMap = orderService.customerOrderIndex(vo);
            return Result.success(resultMap);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }
}
