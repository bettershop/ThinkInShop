package com.laiketui.admins.admin.controller.saas;


import com.laiketui.admins.api.admin.saas.AdminCurrencyStoreService;
import com.laiketui.core.domain.Result;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.domain.CurrencyStoreModel;
import com.laiketui.domain.vo.saas.CurrencyStoreVo;
import com.laiketui.root.annotation.HttpApiMethod;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "多商户后台-商城货币管理")
@RestController
@RequestMapping("/saas/currencyStore")
public class AdminCurrencyStoreController
{

    @Autowired
    private AdminCurrencyStoreService adminCurrencyStoreService;

    @ApiOperation("商城货币列表")
    @PostMapping("/currencyStoreList")
    @HttpApiMethod(apiKey = "admin.currencyStore.currencyStoreList")
    public Result countryList(CurrencyStoreVo vo)
    {
        try
        {
            return Result.success(adminCurrencyStoreService.index(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("添加/编辑商城货币")
    @PostMapping("/saveOrEditCurrencyStore")
    @HttpApiMethod(apiKey = "admin.currencyStore.saveOrEditCurrencyStore")
    public Result saveOrEditCurrencyStore(CurrencyStoreVo vo)
    {
        try
        {
            CurrencyStoreModel currencyStoreModel = new CurrencyStoreModel();
            BeanUtils.copyProperties(vo, currencyStoreModel);
            currencyStoreModel.setStore_id(vo.getStoreId());
            adminCurrencyStoreService.addCurrencyStore(currencyStoreModel);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("设置商城默认基础货币")
    @PostMapping("/setDefaultCurrency")
    @HttpApiMethod(apiKey = "admin.currencyStore.setDefaultCurrency")
    public Result setDefaultCurrency(CurrencyStoreVo vo)
    {
        try
        {
            CurrencyStoreModel currencyStoreModel = new CurrencyStoreModel();
            BeanUtils.copyProperties(vo, currencyStoreModel);
            currencyStoreModel.setStore_id(vo.getStoreId());
            return Result.success(adminCurrencyStoreService.setDefaultCurrency(currencyStoreModel,true));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("获取商城默认基础货币")
    @PostMapping("/getDefaultCurrency")
    @HttpApiMethod(apiKey = "admin.currencyStore.getDefaultCurrency")
    public Result getDefaultCurrency(CurrencyStoreVo vo)
    {
        try
        {
            return Result.success(adminCurrencyStoreService.getStoreDefaultCurrency(vo.getStoreId()));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("删除商城货币基本信息")
    @PostMapping("/delCurrencyStore")
    @HttpApiMethod(apiKey = "admin.currencyStore.delCurrencyStore")
    public Result delCurrencyStore(CurrencyStoreVo vo)
    {
        try
        {
            adminCurrencyStoreService.delCurrencyStore(vo);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

}