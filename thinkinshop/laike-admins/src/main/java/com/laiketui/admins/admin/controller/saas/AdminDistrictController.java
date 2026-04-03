package com.laiketui.admins.admin.controller.saas;

import com.laiketui.admins.api.admin.saas.AdminDistrictService;
import com.laiketui.core.domain.Result;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.domain.dictionary.CountryModel;
import com.laiketui.domain.vo.saas.DistrictVo;
import com.laiketui.root.annotation.HttpApiMethod;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "多商户后台-地区管理")
@RestController
@RequestMapping("/saas/district")
public class AdminDistrictController
{
    @Autowired
    private AdminDistrictService adminDistrictService;

    @ApiOperation("国家列表")
    @PostMapping("/districtList")
    @HttpApiMethod(apiKey = "admin.district.districtList")
    public Result districtList(DistrictVo vo)
    {
        try
        {
            return Result.success(adminDistrictService.districtList(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("添加/编辑地区")
    @PostMapping("/saveOrEditCountry")
    @HttpApiMethod(apiKey = "admin.district.saveOrEditCountry")
    public Result saveOrEditCountry(DistrictVo vo)
    {
        try
        {
            CountryModel countryModel = new CountryModel();
            BeanUtils.copyProperties(vo, countryModel);
            adminDistrictService.saveOrEditDistrict(vo);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("删除地区基本信息")
    @PostMapping("/deleteDistrict")
    @HttpApiMethod(apiKey = "admin.district.deleteDistrict")
    public Result deleteDistrict(DistrictVo vo)
    {
        try
        {
            adminDistrictService.deleteDistrict(vo);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("获取上级地区信息")
    @PostMapping("/allDistrict")
    @HttpApiMethod(apiKey = "admin.district.allDistrict")
    public Result allDistrict()
    {
        try
        {
            return Result.success(adminDistrictService.allDistrict());
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

}
