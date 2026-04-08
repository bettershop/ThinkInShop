package com.laiketui.apps.common.controller;

import com.laiketui.common.api.PubliceService;
import com.laiketui.core.domain.Result;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.saas.CurrencyStoreVo;
import com.laiketui.root.annotation.HttpApiMethod;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;


@Api(tags = "用户端-公共接口")
@RestController
@RequestMapping("/app/common")
public class AppCommonController
{

    @Autowired
    private PubliceService publiceService;

    @ApiOperation("语种列表")
    @PostMapping("/getLangs")
    @HttpApiMethod(apiKey = "app.common.getLangs")
    public Result getLangs(MainVo vo)
    {
        try
        {
            //只获取平台创建商城时候给商城配置的语种 在 lkt_customer 表的 store_langs 存的是 lkt_lang 表的语种id 逗号分隔开的
            return Result.success(publiceService.getAppLangs(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("获取国家列表")
    @PostMapping("/getCountry")
    @HttpApiMethod(apiKey = "app.common.getCountry")
    public Result getCountry(MainVo vo)
    {
        try
        {
            return Result.success(publiceService.getCountryList(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("币种列表")
    @PostMapping("/getCurrencys")
    @HttpApiMethod(apiKey = "app.common.getCurrencys")
    public Result getCurrencys(CurrencyStoreVo vo)
    {
        try
        {
            return Result.success(publiceService.getAppCurencys(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("商城默认货币")
    @PostMapping("/getStoreDefaultCurrency")
    @HttpApiMethod(apiKey = "app.common.getStoreDefaultCurrency")
    public Result getStoreDefaultCurrency(CurrencyStoreVo vo)
    {
        try
        {
            return Result.success(publiceService.getStoreDefaultCurrency(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("商城默认语言")
    @PostMapping("/getStoreDefaultLang")
    @HttpApiMethod(apiKey = "app.common.getStoreDefaultLang")
    public Result getStoreDefaultLang(MainVo vo)
    {
        try
        {
            return Result.success(publiceService.getStoreDefaultLang(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("商城默认语言和货币")
    @PostMapping("/getStoreDefaultI18n")
    @HttpApiMethod(apiKey = "app.common.getStoreDefaultI18n")
    public Result getStoreDefaultI18n(MainVo vo)
    {
        try
        {
            //商城默认语言
            Map<String, Object> storeDefaultLangInfo = publiceService.getStoreDefaultLang(vo);

            //商城默认货币
            CurrencyStoreVo currencyStoreVo = new CurrencyStoreVo();
            currencyStoreVo.setStoreId(vo.getStoreId());
            Map<String, Object> storeDefaultCurrencyInfo = publiceService.getStoreDefaultCurrency(currencyStoreVo);

            Map<String, Object> allIn = new HashMap<String, Object>();
            allIn.put("storeDefaultLanguage", storeDefaultLangInfo.get("storeDefaultLanguage"));
            allIn.put("storeId", vo.getStoreId());
            allIn.put("storeDefaultCurrency", storeDefaultCurrencyInfo);

            return Result.success(allIn);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }



    @ApiOperation("用户更改货币api")
    @PostMapping("userChangeCurrency")
    @HttpApiMethod(urlMapping = "app.common.userChangeCurrency")
    public Result userChangeCurrency(CurrencyStoreVo vo)
    {
        try
        {
            publiceService.changeCurrency(vo);
            return Result.success();
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("获取用户货币的最新信息")
    @PostMapping("fetchUserCurrencyInfo")
    @HttpApiMethod(urlMapping = "app.common.fetchUserCurrencyInfo")
    public Result fetchUserCurrencyInfo(CurrencyStoreVo vo)
    {
        try
        {
            return Result.success(publiceService.fetchUserCurrencyInfo(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

}
