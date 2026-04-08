package com.laiketui.admins.admin.controller.saas;


import com.laiketui.admins.api.admin.saas.AdminCountryServcie;
import com.laiketui.core.domain.Result;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.domain.dictionary.CountryModel;
import com.laiketui.domain.vo.saas.CountryVo;
import com.laiketui.root.annotation.HttpApiMethod;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "多商户后台-国家管理")
@RestController
@RequestMapping("/saas/country")
public class AdminCountryController
{

    @Autowired
    private AdminCountryServcie adminCountryServcie;


    @ApiOperation("国家列表")
    @PostMapping("/countryList")
    @HttpApiMethod(apiKey = "admin.country.countryList")
    public Result countryList(CountryVo vo)
    {
        try
        {
            return Result.success(adminCountryServcie.countryList(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("添加/编辑国家")
    @PostMapping("/saveOrEditCountry")
    @HttpApiMethod(apiKey = "admin.country.saveOrEditCountry")
    public Result saveOrEditCountry(CountryVo vo)
    {
        try
        {
            CountryModel countryModel = new CountryModel();
            BeanUtils.copyProperties(vo, countryModel);
            adminCountryServcie.saveOrEditCountry(countryModel);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("删除国家基本信息")
    @PostMapping("/deleteCountry")
    @HttpApiMethod(apiKey = "admin.country.deleteCountry")
    public Result deleteCountry(CountryVo vo)
    {
        try
        {
            adminCountryServcie.deleteCountry(vo);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

}
