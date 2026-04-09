package com.laiketui.apps.user.controller;

import com.laiketui.apps.api.user.AppsUserAddressService;
import com.laiketui.core.annotation.ParamsMapping;
import com.laiketui.core.domain.Result;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.DictionaryConst;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.SaveAddressVo;
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

/**
 * 我的地址
 *
 * @author Trick
 * @date 2020/11/4 17:01
 */
@Api(tags = "地址")
@RestController
@Validated
@RequestMapping("/app/user/address")
public class AppsUserAddressController
{

    @Autowired
    AppsUserAddressService appsUserAddressService;

    @ApiOperation("我的地址页面")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "product", value = "地址id", required = true, dataType = "string", paramType = "form")
    })
    @PostMapping("addressIndex")
    @HttpApiMethod(urlMapping = "app.address.index")
    public Result index(MainVo vo, String product) throws LaiKeAPIException
    {
        try
        {
            return Result.success(appsUserAddressService.index(vo, product));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("添加地址")
    @PostMapping("saveAddress")
    @HttpApiMethod(urlMapping = "app.address.SaveAddress")
    public Result saveAddress(SaveAddressVo vo) throws LaiKeAPIException
    {
        try
        {
            appsUserAddressService.saveAddress(vo);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("根据id获取地址信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "addr_id", value = "地址id", required = true, dataType = "int", paramType = "form")
    })
    @PostMapping("up_addsindex")
    @HttpApiMethod(urlMapping = "app.address.up_addsindex")
    public Result getAddressById(MainVo vo, @ParamsMapping("addr_id") int addrId) throws LaiKeAPIException
    {
        try
        {
            return Result.success(appsUserAddressService.getAddressById(vo, addrId));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("修改地址")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "addr_id", value = "地址id", required = true, dataType = "int", paramType = "form")
    })
    @PostMapping("up_adds")
    @HttpApiMethod(urlMapping = "app.address.up_adds")
    public Result updateAddress(SaveAddressVo vo, @ParamsMapping("addr_id") int addsId) throws LaiKeAPIException
    {
        try
        {
            return Result.success(appsUserAddressService.updateAddress(vo, addsId));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("设置默认地址")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "addr_id", value = "地址id", required = true, dataType = "int", paramType = "form")
    })
    @PostMapping("set_default")
    @HttpApiMethod(urlMapping = "app.address.set_default")
    public Result setDefaultAddress(MainVo vo, @ParamsMapping("addr_id") int addsId) throws LaiKeAPIException
    {
        try
        {
            return Result.success(appsUserAddressService.setDefaultAddress(vo, addsId));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("显示省份")
    @PostMapping("AddressManagement")
    @HttpApiMethod(urlMapping = "app.address.AddressManagement")
    public Result addressManagement(MainVo vo) throws LaiKeAPIException
    {
        try
        {
            return Result.success(appsUserAddressService.getJoinCityCounty(vo, DictionaryConst.Position.LEVEL_2, 0));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("根据省查询市")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "GroupID", value = "级联id", required = true, dataType = "int", paramType = "form")
    })
    @PostMapping("getCityArr")
    @HttpApiMethod(urlMapping = "app.address.getCityArr")
    public Result getCityArr(MainVo vo, @ParamsMapping("GroupID") int groupId) throws LaiKeAPIException
    {
        try
        {
            return Result.success(appsUserAddressService.getJoinCityCounty(vo, DictionaryConst.Position.LEVEL_3, groupId));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("根据市查询县")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "GroupID", value = "级联id", required = true, dataType = "int", paramType = "form")
    })
    @PostMapping("getCountyInfo")
    @HttpApiMethod(urlMapping = "app.address.getCountyInfo")
    public Result getCountyInfo(MainVo vo, @ParamsMapping("GroupID") int groupId) throws LaiKeAPIException
    {
        try
        {
            return Result.success(appsUserAddressService.getJoinCityCounty(vo, DictionaryConst.Position.LEVEL_4, groupId));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("删除地址")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "addr_id", value = "级联id", required = true, dataType = "int", paramType = "form")
    })
    @PostMapping("del_adds")
    @HttpApiMethod(urlMapping = "app.address.del_adds")
    public Result delAdds(MainVo vo, @ParamsMapping("addr_id") int addrId) throws LaiKeAPIException
    {
        try
        {
            appsUserAddressService.delAdds(vo, addrId);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


}
