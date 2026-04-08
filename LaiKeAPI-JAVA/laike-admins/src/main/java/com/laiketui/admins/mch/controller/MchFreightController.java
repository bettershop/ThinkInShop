package com.laiketui.admins.mch.controller;

import com.laiketui.admins.api.mch.MchFreightService;
import com.laiketui.core.domain.Result;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.mch.AddFreihtVo;
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
 * 运费管理
 *
 * @author Trick
 * @date 2021/6/8 10:18
 */
@Api(tags = "运费管理")
@RestController
@RequestMapping("/admin/mch/freight/")
public class MchFreightController
{

    @Autowired
    private MchFreightService mchFreightService;

    @ApiOperation("获取运费列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "fid", value = "运费id", dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "status", value = "使用状态 0=未使用 1=使用中", dataType = "int", paramType = "form"),
    })
    @PostMapping("/index")
    @HttpApiMethod(apiKey = "mch.Mch.Freight.Index")
    public Result index(MainVo vo, Integer fid, Integer status)
    {
        try
        {
            return Result.success(mchFreightService.index(vo, fid, status));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("添加/编辑运费")
    @PostMapping("/addFreight")
    @HttpApiMethod(apiKey = "admin.mch.freight.addFreight")
    public Result addFreight(AddFreihtVo vo)
    {
        try
        {
            mchFreightService.addFreight(vo);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("删除运费")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "ids", value = "运费id集", dataType = "string", paramType = "form"),
    })
    @PostMapping("/freightDel")
    @HttpApiMethod(apiKey = "app.mch.freightDel")
    public Result freightDel(MainVo vo, String ids)
    {
        try
        {
            mchFreightService.freightDel(vo, ids);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("修改默认运费")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "运费id", dataType = "int", paramType = "form"),
    })
    @PostMapping("/setDefault")
    @HttpApiMethod(apiKey = "app.mch.setDefault")
    public Result setDefault(MainVo vo, int id)
    {
        try
        {
            mchFreightService.setDefault(vo, id);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

}
