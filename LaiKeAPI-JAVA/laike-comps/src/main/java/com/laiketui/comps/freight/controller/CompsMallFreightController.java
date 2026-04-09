package com.laiketui.comps.freight.controller;

import com.laiketui.comps.api.freight.CompsMallFreightService;
import com.laiketui.core.annotation.ParamsMapping;
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
 * 后台商城-运费管理
 *
 * @author Trick
 * @date 2020/12/28 17:26
 */
@Api(tags = "后台商城-运费管理")
@RestController
@RequestMapping("/comps/admin/MallFreight")
public class CompsMallFreightController
{

    @Autowired
    private CompsMallFreightService compsMallFreightService;

    @ApiOperation("获取运费列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "fid", value = "运费id", dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "mchId", value = "店铺id", dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "status", value = "使用状态 0=未使用 1=使用中", dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "type", value = "计算方式 0:件 1:重量 2:默认", dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "name", value = "模板名称", dataType = "int", paramType = "form"),
    })
    @PostMapping("/getMallFreightInfo")
    @HttpApiMethod(apiKey = "admin.MallFreight.getMallFreightInfo")
    public Result getMallFreightInfo(MainVo vo, Integer mchId, Integer fid, Integer status, Integer type, String name)
    {
        try
        {
            return Result.success(compsMallFreightService.getMallFreightInfo(vo, mchId, fid, status, type, name));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("添加/修改运费")
    @PostMapping("/addMallFreight")
    @HttpApiMethod(apiKey = "admin.MallFreight.addMallFreight")
    public Result addMallFreight(AddFreihtVo vo)
    {
        try
        {
            return Result.success(compsMallFreightService.addMallFreight(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("运费设置默认开关")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "运费id", dataType = "int", paramType = "form")
    })
    @PostMapping("/MallFreightSetDefault")
    @HttpApiMethod(apiKey = "admin.MallFreight.MallFreightSetDefault")
    public Result MallFreightSetDefault(MainVo vo, int id)
    {
        try
        {
            compsMallFreightService.MallFreightSetDefault(vo, id);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("删除运费")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "freightIds", value = "运费模板id,支持多个", dataType = "int", paramType = "form")
    })
    @PostMapping("/delMallFreight")
    @HttpApiMethod(apiKey = "admin.MallFreight.delMallFreight")
    public Result delMallFreight(MainVo vo, @ParamsMapping("idList") String freightIds)
    {
        try
        {
            compsMallFreightService.delMallFreight(vo, freightIds);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }
}
