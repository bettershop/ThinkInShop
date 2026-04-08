package com.laiketui.admins.admin.controller.saas;


import com.laiketui.admins.api.admin.saas.AdminCurrencyService;
import com.laiketui.core.domain.Result;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.domain.CurrencyModel;
import com.laiketui.domain.vo.saas.CurrencyVo;
import com.laiketui.root.annotation.HttpApiMethod;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "多商户后台-货币管理")
@RestController
@RequestMapping("/saas/currency")
public class AdminCurrencyController
{

    @Autowired
    private AdminCurrencyService adminCurrencyService;


    @ApiOperation("货币列表")
    @PostMapping("/currencyList")
    @HttpApiMethod(apiKey = "admin.currency.currencyList")
    public Result countryList(CurrencyVo vo)
    {
        try
        {
            return Result.success(adminCurrencyService.index(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("添加/编辑货币")
    @PostMapping("/saveOrEditCurrency")
    @HttpApiMethod(apiKey = "admin.currency.saveOrEditCurrency")
    public Result saveOrEditCurrency(CurrencyVo vo)
    {
        try
        {
            CurrencyModel currencyModel = new CurrencyModel();
            BeanUtils.copyProperties(vo, currencyModel);
            adminCurrencyService.addCurrency(currencyModel);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("删除货币基本信息")
    @PostMapping("/delCurrency")
    @HttpApiMethod(apiKey = "admin.currency.delCurrency")
    public Result delCurrency(CurrencyVo vo)
    {
        try
        {
            return Result.success(adminCurrencyService.delCurrency(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

}