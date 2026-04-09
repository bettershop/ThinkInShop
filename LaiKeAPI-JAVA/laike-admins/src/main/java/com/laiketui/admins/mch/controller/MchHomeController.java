package com.laiketui.admins.mch.controller;

import com.laiketui.admins.api.mch.MchHomeService;
import com.laiketui.common.api.PublicDictionaryService;
import com.laiketui.common.api.PubliceService;
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
 * 首页
 *
 * @author Trick
 * @date 2021/5/28 9:56
 */
@Api(tags = "首页")
@RestController
@RequestMapping("/admin/mch/home/")
public class MchHomeController
{

    @Autowired
    private MchHomeService mchHomeService;

    @Autowired
    private PublicDictionaryService publicDictionaryService;
    @Autowired
    private PubliceService publiceService;

    @ApiOperation("info")
    @PostMapping("/info")
    @HttpApiMethod(apiKey = "mch.Mch.Home.Info")
    public Result info(MainVo vo)
    {
        try
        {
            return Result.success(mchHomeService.info(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("获取商城列表")
    @PostMapping("/storeList")
    @HttpApiMethod(apiKey = "admin.mch.home.storeList")
    public Result storeList(MainVo vo)
    {
        try
        {
            return Result.success(mchHomeService.storeList(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("店铺信息")
    @PostMapping("/index")
    @HttpApiMethod(apiKey = "admin.mch.home.index")
    public Result index(MainVo vo)
    {
        try
        {
            return Result.success(mchHomeService.index(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("获取字典目录列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "字典id", dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "dicNo", value = "数据编码", dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "key", value = "数据名称", dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "value", value = "属性值", dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "status", value = "是否生效 0不生效 1生效", dataType = "int", paramType = "form")
    })
    @PostMapping("/getDictionaryInfo")
    @HttpApiMethod(urlMapping = {"mch.Mch.Home.GetDictionaryInfo", "admin.mch.home.getDictionaryInfo"})
    public Result getDictionaryInfo(MainVo vo, Integer id, String dicNo, String key, String value, Integer status)
    {
        try
        {
            return Result.success(publicDictionaryService.getDictionaryInfo(vo, id, dicNo, key, value, status));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("获取字典目录下拉")
    @PostMapping("/getDictionaryCatalogList")
    @HttpApiMethod(apiKey = "admin.mch.home.getDictionaryCatalogList")
    public Result getDictionaryCatalogList()
    {
        try
        {
            return Result.success(publicDictionaryService.getDictionaryCatalogList());
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("省市级联")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "level", value = "默认全部 2=省 3=市 4=区", dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "sid", value = "上级id", dataType = "int", paramType = "form"),
    })
    @PostMapping("/getRegion")
    @HttpApiMethod(apiKey = "mch.Mch.Home.GetRegion")
    public Result getRegion(MainVo vo, Integer level, Integer sid)
    {
        try
        {
            return Result.success(mchHomeService.getRegion(vo, level, sid));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("店铺后台设置国际化语言")
    @PostMapping("/select_language")
    @HttpApiMethod(apiKey = "admin.mch.home.select_language")
    public Result selectLanguage(MainVo vo)
    {
        try
        {
            mchHomeService.selectLanguage(vo);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("获取前端基础信息配置")
    @PostMapping("/getMchBasicConfiguration")
    @HttpApiMethod(apiKey = "admin.mch.home.GetBasicConfiguration")
    public Result getFrontMsgAndLoginConfig(MainVo vo)
    {
        try
        {
            return Result.success(publiceService.getFrontConfig(vo, 1));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

}
