package com.laiketui.admins.mch.controller;

import com.laiketui.admins.api.mch.MchService;
import com.laiketui.common.api.PubliceService;
import com.laiketui.core.domain.Result;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.mch.AddMchVo;
import com.laiketui.domain.vo.mch.MchPrintSetupVo;
import com.laiketui.domain.vo.pc.AddBannerInfoVo;
import com.laiketui.domain.vo.saas.CurrencyStoreVo;
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
 * 店铺设置
 *
 * @author Trick
 * @date 2021/5/27 11:36
 */
@Api(tags = "店铺设置")
@RestController
@RequestMapping({"/admin/mch/set/"})
public class MchController
{

    @Autowired
    private MchService mchService;

    @Autowired
    private PubliceService publiceService;

    @ApiOperation("币种列表")
    @PostMapping("/getCurrencys")
    @HttpApiMethod(apiKey = "mch.Mch.Set.getCurrencys")
    public Result getCurrencys(CurrencyStoreVo vo)
    {
        try
        {
            return Result.success(publiceService.getPcMchCurencys(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("用户更改货币api")
    @PostMapping("userChangeCurrency")
    @HttpApiMethod(urlMapping = "mch.Mch.Set.userChangeCurrency")
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

    @ApiOperation("店铺信息")
    @PostMapping("/index")
    @HttpApiMethod(apiKey = "mch.Mch.Set.Index")
    public Result index(MainVo vo)
    {
        try
        {
            return Result.success(mchService.index(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("编辑店铺")
    @PostMapping("/edit")
    @HttpApiMethod(apiKey = "mch.Mch.Set.Edit")
    public Result edit(AddMchVo vo)
    {
        try
        {
            return Result.success(mchService.edit(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("修改密码")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pwd", value = "新密码", dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "pwdOld", value = "原密码", dataType = "string", paramType = "form"),
    })
    @PostMapping("/setPassword")
    @HttpApiMethod(apiKey = "mch.Mch.Set.SetPassword")
    public Result setPassword(MainVo vo, String pwd, String pwdOld)
    {
        try
        {
            return Result.success(mchService.setPassword(vo, pwd, pwdOld));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("获取轮播图列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "id", dataType = "int", paramType = "form"),
    })
    @PostMapping("/bannerList")
    @HttpApiMethod(apiKey = "mch.Mch.Set.BannerList")
    public Result bannerList(MainVo vo, Integer id)
    {
        try
        {
            return Result.success(mchService.bannerList(vo, id));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("轮播图路径分类")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "type", value = "跳转类型 1.分类 2.商品 3.店铺 默认自定义", dataType = "int", paramType = "form")
    })
    @PostMapping("/bannerPathList")
    @HttpApiMethod(apiKey = "mch.Mch.Set.BannerPathList")
    public Result bannerPathList(MainVo vo, Integer type) throws LaiKeAPIException
    {
        try
        {
            return Result.success(mchService.bannerPathList(vo, type));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("添加/编辑轮播图信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "type", value = "默认移动端轮播图  4=pc端轮播图", dataType = "int", paramType = "form")
    })
    @PostMapping("/addBannerInfo")
    @HttpApiMethod(apiKey = "mch.Mch.Set.AddBannerInfo")
    public Result addBannerInfo(AddBannerInfoVo vo, Integer type)
    {
        try
        {
            mchService.addBannerInfo(vo, type);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("修改轮播图序号")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "轮播图id", dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "sort", value = "序号", dataType = "int", paramType = "form"),
    })
    @PostMapping("/setBannerSort")
    @HttpApiMethod(apiKey = "mch.Mch.Set.SetBannerSort")
    public Result setBannerSort(MainVo vo, Integer id, Integer sort)
    {
        try
        {
            mchService.setBannerSort(vo, id, sort);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("删除轮播图")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "轮播图id", dataType = "int", paramType = "form")
    })
    @PostMapping("/delBannerById")
    @HttpApiMethod(apiKey = "mch.Mch.Set.DelBannerById")
    public Result delBannerById(MainVo vo, int id)
    {
        try
        {
            mchService.delBannerById(vo, id);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("轮播图置顶")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "轮播图id", dataType = "int", paramType = "form")
    })
    @PostMapping("/topBannerById")
    @HttpApiMethod(apiKey = "admin.mch.set.topBannerById")
    public Result topBannerById(int id)
    {
        try
        {
            mchService.topBannerById(id);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("注销店铺")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "轮播图id", dataType = "int", paramType = "form")
    })
    @PostMapping("/delMchInfo")
    @HttpApiMethod(apiKey = "mch.Mch.Set.DelMchInfo")
    public Result delMchInfo(MainVo vo)
    {
        try
        {
            return Result.success(mchService.delMchInfo(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("获取店铺设置")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "轮播图id", dataType = "int", paramType = "form")
    })
    @PostMapping("/getMchConfig")
    @HttpApiMethod(apiKey = "admin.mch.set.getMchConfig")
    public Result getMchConfig(MainVo vo)
    {
        try
        {
            return Result.success(mchService.getMchConfig(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("查询店铺分类列表")
    @PostMapping("/mchClassList")
    @HttpApiMethod(apiKey = "mch.Mch.Set.MchClassList")
    public Result mchClassList(MainVo vo)
    {
        try
        {
            return Result.success(mchService.mchClassList(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("获取订单打印配置")
    @PostMapping("/getMchPrintSetup")
    @HttpApiMethod(apiKey = "mch.Mch.Set.GetMchPrintSetup")
    public Result getMchPrintSetup(MainVo vo)
    {
        try
        {
            return Result.success(mchService.getMchPrintSetup(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("上传订单打印配置")
    @PostMapping("/setMchPrintSetup")
    @HttpApiMethod(apiKey = "mch.Mch.Set.SetMchPrintSetup")
    public Result getMchPrintSetup(MchPrintSetupVo vo)
    {
        try
        {
            mchService.setMchPrintSetup(vo);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("pc店铺端-首页")
    @PostMapping("/mchIndex")
    @HttpApiMethod(apiKey = "mch.Mch.Index.index")
    public Result mchIndex(MainVo vo, String startTime, String endTime)
    {
        try
        {
            return Result.success(mchService.mchIndex(vo, startTime, endTime));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("统计汇总")
    @PostMapping("/statisticsMch")
    @HttpApiMethod(apiKey = "admin.mch.statisticsMch")
    public Result statisticsMch(MainVo vo)
    {
        try
        {
            mchService.statisticsMch(vo);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

}
