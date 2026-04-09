package com.laiketui.admins.admin.controller;

import com.laiketui.admins.api.admin.AdminBackUpService;
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
 * @author zhuqingyu
 * @create 2024/8/19
 */
@Api(tags = "后台-数据备份")
@RestController
@RequestMapping("/admin/BackUp")
public class AdminBackUpController
{
    @Autowired
    private AdminBackUpService adminBackUpService;


    @ApiOperation("查询配置")
    @PostMapping("queryConfig")
    @HttpApiMethod(apiKey = "admin.BackUp.queryConfig")
    public Result queryConfig(MainVo vo)
    {
        try
        {
            return Result.success(adminBackUpService.queryConfig(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("添加配置")
    @PostMapping("addConfig")
    @HttpApiMethod(apiKey = "admin.BackUp.addConfig")
    public Result addConfig(MainVo vo, Integer is_open, String execute_cycle, String url, Integer query_data)
    {
        try
        {
            adminBackUpService.addConfig(vo, is_open, execute_cycle, url, query_data);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("数据备份记录")
    @PostMapping("backUpRecord")
    @HttpApiMethod(apiKey = "admin.BackUp.backUpRecord")
    public Result BackUpRecord(MainVo vo)
    {
        try
        {
            return Result.success(adminBackUpService.backUpRecord(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("删除备份记录")
    @PostMapping("delBackUpRecord")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "主id", dataType = "integer", paramType = "form"),
    })
    @HttpApiMethod(apiKey = "admin.BackUp.delBackUpRecord")
    public Result delBackUpRecord(MainVo vo, Integer id)
    {
        try
        {
            adminBackUpService.delBackUpRecord(vo, id);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("还原")
    @PostMapping("reduction")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "主id", dataType = "integer", paramType = "form"),
    })
    @HttpApiMethod(apiKey = "admin.BackUp.reduction")
    public Result reduction(MainVo vo, Integer id)
    {
        try
        {
            adminBackUpService.reduction(vo, id);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("立即备份")
    @PostMapping("immediately")
    @HttpApiMethod(apiKey = "admin.BackUp.immediately")
    public Result immediately(MainVo vo)
    {
        try
        {
            adminBackUpService.immediately(vo);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("取消任务")
    @PostMapping("cancelTask")
    @HttpApiMethod(apiKey = "admin.BackUp.cancelTask")
    public Result cancelTask(MainVo vo, Integer id)
    {
        try
        {
            adminBackUpService.delBackUpRecord(vo, id);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


}
