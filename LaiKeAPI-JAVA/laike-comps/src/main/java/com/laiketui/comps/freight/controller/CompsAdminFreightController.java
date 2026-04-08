package com.laiketui.comps.freight.controller;

import com.laiketui.comps.api.freight.CompsAdminFreightService;
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
 * 后台-运费管理
 *
 * @author Trick
 * @date 2020/12/28 17:26
 */
@Api(tags = "后台-运费管理")
@RestController
@RequestMapping("/comps/admin/AdminFreight")
public class CompsAdminFreightController
{

    @Autowired
    private CompsAdminFreightService compsAdminFreightService;

    @ApiOperation("获取运费列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "fid", value = "运费id", dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "mchId", value = "店铺id", dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "status", value = "使用状态 0=未使用 1=使用中", dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "type", value = "计算方式 0:件 1:重量 2:默认", dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "name", value = "模板名称", dataType = "int", paramType = "form"),
    })
    @PostMapping("/AdminGetFreightInfo")
    @HttpApiMethod(apiKey = "admin.AdminFreight.AdminGetFreightInfo")
    public Result AdminGetFreightInfo(MainVo vo, Integer mchId, Integer fid, Integer status, Integer type, String name)
    {
        try
        {
            return Result.success(compsAdminFreightService.AdminGetFreightInfo(vo, mchId, fid, status, type, name));
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
    @PostMapping("/AdminFreightSetDefault")
    @HttpApiMethod(apiKey = "admin.AdminFreight.AdminFreightSetDefault")
    public Result AdminFreightSetDefault(MainVo vo, int id)
    {
        try
        {
            compsAdminFreightService.AdminFreightSetDefault(vo, id);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("添加/修改运费")
    @PostMapping("/addAdminFreight")
    @HttpApiMethod(apiKey = "admin.AdminFreight.addAdminFreight")
    public Result addFreight(AddFreihtVo vo)
    {
        try
        {
            return Result.success(compsAdminFreightService.addAdminFreight(vo));
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
    @PostMapping("/delFreight")
    @HttpApiMethod(apiKey = "admin.AdminFreight.delAdminFreight")
    public Result delAdminFreight(MainVo vo, @ParamsMapping("idList") String freightIds)
    {
        try
        {
            compsAdminFreightService.delAdminFreight(vo, freightIds);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("城市信息")
    @PostMapping("/cityInfo")
    @HttpApiMethod(urlMapping = {"mch.Mch.Freight.CityInfo", "admin.AdminFreight.cityInfo"})
    public Result cityInfo(MainVo vo)
    {
        try
        {
            return Result.success(compsAdminFreightService.cityInfo(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("关联商品")
    @PostMapping("/relatedProducts")
    @HttpApiMethod(urlMapping = {"admin.Freight.RelatedProducts"})
    public Result relatedProducts(AddFreihtVo vo)
    {
        try
        {
            return Result.success(compsAdminFreightService.relatedProducts(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


}
