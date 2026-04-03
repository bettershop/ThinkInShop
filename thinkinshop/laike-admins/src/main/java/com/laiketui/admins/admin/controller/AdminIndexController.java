package com.laiketui.admins.admin.controller;

import com.laiketui.admins.api.admin.AdminIndexDubboService;
import com.laiketui.core.domain.Result;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.admin.DefaultViewVo;
import com.laiketui.root.annotation.HttpApiMethod;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * 首页
 *
 * @author Trick
 * @date 2020/12/28 14:04
 */
@Api(tags = "首页")
@RestController
@RequestMapping("/admin/goods")
public class AdminIndexController
{

    @Autowired
    private AdminIndexDubboService adminIndexDubboService;


    @ApiOperation("商城首页")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "mchId", value = "店铺id", dataType = "int", paramType = "form")
    })
    @PostMapping("/home")
    @HttpApiMethod(apiKey = "admin.home.index")
    public Result home(MainVo vo, Integer mchId)
    {
        try
        {
            return Result.success(adminIndexDubboService.home(vo, mchId));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("后台商品列表")
    @PostMapping("/index")
    @HttpApiMethod(apiKey = "admin.goods.index")
    public Result index(DefaultViewVo vo, HttpServletResponse response)
    {
        try
        {
            vo.setMchName(vo.getProductTitle());
            Map<String, Object> ret = adminIndexDubboService.index(vo, response);
            return ret == null ? Result.exportFile() : Result.success(ret);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("商品状态")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "ids", value = "商品id集", required = true, dataType = "string", paramType = "form")
    })
    @PostMapping("/goodsStatus")
    @HttpApiMethod(apiKey = "admin.goods.goodsStatus")
    public Result goodsStatus(MainVo vo, String ids)
    {
        try
        {
            return Result.success(adminIndexDubboService.goodsStatus(vo, ids));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("是否显示下架商品开关")
    @PostMapping("/isOpen")
    @HttpApiMethod(apiKey = "admin.goods.isOpen")
    public Result isOpen(MainVo vo)
    {
        try
        {
            return Result.success(adminIndexDubboService.isopen(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("是否展示已售罄的商品")
    @PostMapping("/displaySellOut")
    @HttpApiMethod(apiKey = "admin.goods.displaySellOut")
    public Result displaySellOut(MainVo vo)
    {
        try
        {
            adminIndexDubboService.displaySellOut(vo);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

}
