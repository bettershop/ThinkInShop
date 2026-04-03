package com.laiketui.admins.mch.plugin;

import com.laiketui.admins.api.mch.plugin.MchSignService;
import com.laiketui.core.domain.Result;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.plugin.sign.AddSignConfigVo;
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
 * 后台签到管理接口
 *
 * @author Trick
 * @date 2021/5/11 10:40
 */
@Api(tags = "后台签到管理接口")
@RestController
@RequestMapping("/pc/sign")
public class MchSignController
{

    @Autowired
    private MchSignService signService;


    @ApiOperation("秒杀活动列表数据")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userName", value = "用户昵称", dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "userType", value = "用户来源", dataType = "int", paramType = "form")
    })
    @PostMapping("/index")
    @HttpApiMethod(urlMapping = "pc.sign.index")
    public Result index(MainVo vo, String userName, Integer userType) throws LaiKeAPIException
    {
        try
        {
            return Result.success(signService.index(vo, userName, userType));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("签到明细")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userId", value = "用户昵称", dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "startDate", value = "开始时间", dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "endDate", value = "结束时间", dataType = "string", paramType = "form")
    })
    @PostMapping("/signDetail")
    @HttpApiMethod(urlMapping = "pc.sign.signDetail")
    public Result signDetail(MainVo vo, String userId, String startDate, String endDate) throws LaiKeAPIException
    {
        try
        {
            return Result.success(signService.signDetail(vo, userId, startDate, endDate));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("删除签到记录")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userId", value = "用户昵称", dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "startDate", value = "开始时间", dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "endDate", value = "结束时间", dataType = "string", paramType = "form")
    })
    @PostMapping("/delSign")
    @HttpApiMethod(urlMapping = "pc.sign.delSign")
    public Result delSign(MainVo vo, String userId) throws LaiKeAPIException
    {
        try
        {
            return Result.success(signService.delSign(vo, userId));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("获取签到设置信息")
    @PostMapping("/signConfigInfo")
    @HttpApiMethod(urlMapping = "pc.sign.signConfigInfo")
    public Result signConfigInfo(MainVo vo) throws LaiKeAPIException
    {
        try
        {
            return Result.success(signService.signConfigInfo(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("添加/编辑签到配置")
    @PostMapping("/addSignConfig")
    @HttpApiMethod(urlMapping = "pc.sign.addSignConfig")
    public Result addSignConfig(AddSignConfigVo vo) throws LaiKeAPIException
    {
        try
        {
            return Result.success(signService.addSignConfig(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

}
