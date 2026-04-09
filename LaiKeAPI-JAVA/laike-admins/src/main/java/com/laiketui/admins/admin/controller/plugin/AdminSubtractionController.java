package com.laiketui.admins.admin.controller.plugin;

import com.laiketui.admins.api.admin.plugin.AdminSubtractionService;
import com.laiketui.core.domain.Result;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.plugin.subtraction.AddSubConfigVo;
import com.laiketui.domain.vo.plugin.subtraction.AddSubtractionVo;
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
 * 满减
 *
 * @author Trick
 * @date 2021/5/12 17:11
 */
@Api(tags = "后台满减管理接口")
@RestController
@RequestMapping("/admin/subtraction")
public class AdminSubtractionController
{

    @Autowired
    private AdminSubtractionService adminSubtractionService;

    @ApiOperation("满减商品列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "title", value = "活动标题", dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "status", value = "活动状态", dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "id", value = "满减id", dataType = "int", paramType = "form")
    })
    @PostMapping("/index")
    @HttpApiMethod(urlMapping = "app.admin.subtraction.index")
    public Result index(MainVo vo, String title, Integer status, Integer id) throws LaiKeAPIException
    {
        try
        {
            return Result.success(adminSubtractionService.index(vo, title, status, id));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("添加/编辑满减")
    @PostMapping("/addSubtraction")
    @HttpApiMethod(urlMapping = "app.admin.subtraction.addSubtraction")
    public Result addSubtraction(AddSubtractionVo vo) throws LaiKeAPIException
    {
        try
        {
            return Result.success(adminSubtractionService.addSubtraction(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("开始/结束活动")
    @PostMapping("/subtractionSwitch")
    @HttpApiMethod(urlMapping = "app.admin.subtraction.subtractionSwitch")
    public Result subtractionSwitch(MainVo vo, int id) throws LaiKeAPIException
    {
        try
        {
            return Result.success(adminSubtractionService.subtractionSwitch(vo, id));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("删除活动")
    @PostMapping("/del")
    @HttpApiMethod(urlMapping = "app.admin.subtraction.del")
    public Result del(MainVo vo, int id) throws LaiKeAPIException
    {
        try
        {
            return Result.success(adminSubtractionService.del(vo, id));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("获取满减配置信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "满减Id", dataType = "int", paramType = "form")
    })
    @PostMapping("/getonfigInfo")
    @HttpApiMethod(urlMapping = "app.admin.subtraction.getonfigInfo")
    public Result getonfigInfo(MainVo vo, int id) throws LaiKeAPIException
    {
        try
        {
            return Result.success(adminSubtractionService.getonfigInfo(vo, id));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("获取满赠商品列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "classId", value = "商品类型id", dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "brandId", value = "品牌id", dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "title", value = "商品名称", dataType = "int", paramType = "form")
    })
    @PostMapping("/getSubGoods")
    @HttpApiMethod(urlMapping = "app.admin.subtraction.getSubGoods")
    public Result getSubGoods(MainVo vo, Integer classId, Integer brandId, String title) throws LaiKeAPIException
    {
        try
        {
            return Result.success(adminSubtractionService.getSubGoods(vo, classId, brandId, title));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("添加/编辑满减设置信息")
    @PostMapping("/addSubConfig")
    @HttpApiMethod(urlMapping = "app.admin.subtraction.addSubConfig")
    public Result addSubConfig(AddSubConfigVo vo) throws LaiKeAPIException
    {
        try
        {
            return Result.success(adminSubtractionService.addSubConfig(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

}
