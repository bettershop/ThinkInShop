package com.laiketui.admins.mch.plugin;

import com.laiketui.admins.api.mch.plugin.MchGroupService;
import com.laiketui.core.domain.Result;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.plugin.group.AddGroup;
import com.laiketui.domain.vo.plugin.group.AddGroupConfigVo;
import com.laiketui.domain.vo.plugin.group.QueryOpenGroupVo;
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
 * 拼团后台
 *
 * @author Trick
 * @date 2021/5/8 11:45
 */
@Api(tags = "拼团管理后台")
@RestController
@RequestMapping("/pc/group")
public class MchGroupController
{

    @Autowired
    private MchGroupService groupService;

    @ApiOperation("秒杀活动列表数据")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "goodsName", value = "拼团编号", dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "status", value = "拼团状态", dataType = "int", paramType = "form")
    })
    @PostMapping("/index")
    @HttpApiMethod(urlMapping = "pc.group.index")
    public Result index(MainVo vo, String goodsName, Integer status) throws LaiKeAPIException
    {
        try
        {
            return Result.success(groupService.index(vo, goodsName, status));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("开团记录")
    @PostMapping("/openGroupList")
    @HttpApiMethod(urlMapping = {"pc.group.openGroupList"})
    public Result openGroupList(QueryOpenGroupVo vo) throws LaiKeAPIException
    {
        try
        {
            return Result.success(groupService.openGroupList(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("参团记录")
    @PostMapping("/canGroupList")
    @HttpApiMethod(urlMapping = {"pc.group.canGroupList"})
    public Result canGroupList(QueryOpenGroupVo vo) throws LaiKeAPIException
    {
        try
        {
            return Result.success(groupService.canGroupList(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("拼团明细")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "ptcode", value = "拼团编号", dataType = "string", paramType = "form")
    })
    @PostMapping("/groupDetailList")
    @HttpApiMethod(urlMapping = {"pc.group.groupDetailList"})
    public Result groupDetailList(MainVo vo, String ptcode) throws LaiKeAPIException
    {
        try
        {
            return Result.success(groupService.groupDetailList(vo, ptcode));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("修改拼团活动页面数据")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "goodsId", value = "商品id", dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "activityNo", value = "活动id", dataType = "int", paramType = "form")
    })
    @PostMapping("/getGroupById")
    @HttpApiMethod(urlMapping = {"pc.group.getGroupById"})
    public Result getGroupById(MainVo vo, Integer goodsId, Integer activityNo) throws LaiKeAPIException
    {
        try
        {
            return Result.success(groupService.getGroupById(vo, goodsId, activityNo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("添加/修改拼团活动")
    @PostMapping("/addGroup")
    @HttpApiMethod(urlMapping = {"pc.group.addGroup"})
    public Result addGroup(AddGroup vo) throws LaiKeAPIException
    {
        try
        {
            return Result.success(groupService.addGroup(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("复制拼团活动")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "activityNo", value = "活动id", dataType = "int", paramType = "form")
    })
    @PostMapping("/copyGroup")
    @HttpApiMethod(urlMapping = {"pc.group.copyGroup"})
    public Result copyGroup(MainVo vo, int activityNo) throws LaiKeAPIException
    {
        try
        {
            return Result.success(groupService.copyGroup(vo, activityNo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("拼团活动开始/结束")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "activityNo", value = "活动id", dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "type", value = "2=开始 3=结束", dataType = "int", paramType = "form")
    })
    @PostMapping("/switchGroup")
    @HttpApiMethod(urlMapping = {"pc.group.switchGroup"})
    public Result switchGroup(MainVo vo, int activityNo, int type) throws LaiKeAPIException
    {
        try
        {
            return Result.success(groupService.switchGroup(vo, activityNo, type));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("拼团活动是否显示开始/结束")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "activityNo", value = "活动id", dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "type", value = "2=开始 3=结束", dataType = "int", paramType = "form")
    })
    @PostMapping("/switchGroupShow")
    @HttpApiMethod(urlMapping = {"pc.group.switchGroupShow"})
    public Result switchGroupShow(MainVo vo, int activityNo, int type) throws LaiKeAPIException
    {
        try
        {
            return Result.success(groupService.switchGroupShow(vo, activityNo, type));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("删除拼团活动")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "activityNos", value = "活动id集", dataType = "string", paramType = "form")
    })
    @PostMapping("/delGroup")
    @HttpApiMethod(urlMapping = {"pc.group.delGroup"})
    public Result delGroup(MainVo vo, String activityNos) throws LaiKeAPIException
    {
        try
        {
            return Result.success(groupService.delGroup(vo, activityNos));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("获取拼团配置信息")
    @PostMapping("/getGroupConfig")
    @HttpApiMethod(urlMapping = {"pc.group.getGroupConfig"})
    public Result getGroupConfig(MainVo vo) throws LaiKeAPIException
    {
        try
        {
            return Result.success(groupService.getGroupConfig(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("添加/编辑配置信息")
    @PostMapping("/addGroupConfig")
    @HttpApiMethod(urlMapping = {"pc.group.addGroupConfig"})
    public Result addGroupConfig(AddGroupConfigVo vo) throws LaiKeAPIException
    {
        try
        {
            return Result.success(groupService.addGroupConfig(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

}
