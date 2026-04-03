package com.laiketui.plugins.coupon.controller;

import com.laiketui.core.annotation.ParamsMapping;
import com.laiketui.core.domain.Result;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.coupon.AddCouponActivityVo;
import com.laiketui.domain.vo.coupon.CouponParmaVo;
import com.laiketui.plugins.api.coupon.PluginCouponService;
import com.laiketui.root.annotation.HttpApiMethod;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 优惠卷pai
 *
 * @author Trick
 * @date 2020/12/7 9:46
 */
@Api(tags = "优惠卷api")
@RestController
@RequestMapping("/plugin/coupon")
public class PluginCouponController
{

    @Autowired
    private PluginCouponService pluginCouponService;

    @ApiOperation("我的优惠卷")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "type", value = "优惠卷类型 0=去兑换/去使用 1=已使用 2=已过期", required = true, dataType = "int", paramType = "form")
    })
    @PostMapping("/mycoupon")
    @HttpApiMethod(urlMapping = "plugin.coupon.Appcoupon.MyCoupon", apiKey = "admin.coupon.mycoupon")
    public Result mycoupon(MainVo vo, Integer type)
    {
        try
        {
            return Result.success(pluginCouponService.mycoupon(vo, type));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("获取小程序优惠卷")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "type", value = "类型 1=非店铺 other 默认店铺", required = true, dataType = "int", paramType = "form")
    })
    @PostMapping("/index")
    @HttpApiMethod(urlMapping = "plugin.coupon.Appcoupon.Index")
    public Result index(MainVo vo, Integer type, String mchName)
    {
        try
        {
            return Result.success(pluginCouponService.index(vo, type, mchName));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("点击领取")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "活动id", required = true, dataType = "int", paramType = "form")
    })
    @PostMapping("/receive")
    @HttpApiMethod(urlMapping = "plugin.coupon.Appcoupon.Receive")
    public Result receive(MainVo vo, int id)
    {
        try
        {
            return Result.success(pluginCouponService.receive(vo, id));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("获取商品可用优惠券活动")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pro_id", value = "商品id", required = true, dataType = "int", paramType = "form")
    })
    @PostMapping("/pro_coupon")
    @HttpApiMethod(urlMapping = "plugin.coupon.MallCoupon.proCoupon")
    public Result proCoupon(MainVo vo, @RequestParam("goodsId") int goodsId)
    {
        try
        {
            Map<String, Object> resultMap = pluginCouponService.proCoupon(vo, goodsId);
            return Result.success(resultMap);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("店铺主页获取优惠券活动")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "shop_id", value = "店铺id", required = true, dataType = "int", paramType = "form")
    })
    @PostMapping("/mch_coupon")
    @HttpApiMethod(urlMapping = "plugin.coupon.Appcoupon.MchCoupon")
    public Result mchCoupon(MainVo vo, @ParamsMapping("shop_id") int mchId)
    {
        try
        {
            return Result.success(pluginCouponService.mchCoupon(vo, mchId));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("优惠券列表")
    @PostMapping("/mch_index")
    @HttpApiMethod(urlMapping = "plugin.coupon.AppMchcoupon.MchIndex")
    public Result mchIndex(CouponParmaVo vo)
    {
        try
        {
            return Result.success(pluginCouponService.mchIndex(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("添加优惠卷页面")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "mch_id", value = "店铺id", required = true, dataType = "int", paramType = "form")
    })
    @PostMapping("/add_page")
    @HttpApiMethod(urlMapping = "plugin.coupon.AppMchcoupon.AddPage")
    public Result addPage(MainVo vo, @ParamsMapping("mch_id") int mchId)
    {
        try
        {
            return Result.success(pluginCouponService.addPage(vo, mchId));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("添加优惠卷【店铺】")
    @PostMapping("/add")
    @HttpApiMethod(urlMapping = "plugin.coupon.AppMchcoupon.Add")
    public Result add(AddCouponActivityVo vo)
    {
        try
        {
            return Result.success(pluginCouponService.add(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("编辑优惠卷")
    @PostMapping("/modify")
    @HttpApiMethod(urlMapping = "plugin.coupon.AppMchcoupon.Modify")
    public Result modify(AddCouponActivityVo vo)
    {
        try
        {
            pluginCouponService.modify(vo);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("编辑优惠卷页面")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "mch_id", value = "店铺id", required = true, dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "id", value = "优惠卷id", required = true, dataType = "int", paramType = "form")
    })
    @PostMapping("/modify_page")
    @HttpApiMethod(urlMapping = "plugin.coupon.AppMchcoupon.ModifyPage")
    public Result modifyPage(MainVo vo, @ParamsMapping("mch_id") int mchId, int id)
    {
        try
        {
            return Result.success(pluginCouponService.modifyPage(vo, mchId, id));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("查看优惠卷")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "优惠卷id", required = true, dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "mch_id", value = "店铺id", required = true, dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "sNo", value = "订单号", dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "name", value = "会员名称", dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "status", value = "优惠卷状态", dataType = "int", paramType = "form")
    })
    @PostMapping("/see_coupon")
    @HttpApiMethod(urlMapping = "plugin.coupon.AppMchcoupon.SeeCoupon")
    public Result seeCoupon(MainVo vo, int id, @ParamsMapping("mch_id") int mchId, Integer status, String sNo, String name)
    {
        try
        {
            return Result.success(pluginCouponService.seeCoupon(vo, id, mchId, status, sNo, name));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("删除优惠卷")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "优惠卷id", required = true, dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "mch_id", value = "店铺id", required = true, dataType = "int", paramType = "form")
    })
    @PostMapping("/del")
    @HttpApiMethod(urlMapping = "plugin.coupon.AppMchcoupon.Del")
    public Result del(MainVo vo, int id, @ParamsMapping("mch_id") int mchId)
    {
        try
        {
            return Result.success(pluginCouponService.del(vo, id, mchId));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("删除我的优惠卷")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "ids", value = "提货券活动id集", required = true, dataType = "string", paramType = "form")
    })
    @PostMapping("/batchDel")
    @HttpApiMethod(apiKey = "plugin.coupon.Appcoupon.BatchDel")
    public Result batchDel(MainVo vo, String ids)
    {
        try
        {
            pluginCouponService.batchDel(vo, ids);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("分类")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "mch_id", value = "店铺id", required = true, dataType = "int", paramType = "form")
    })
    @PostMapping("/fenlei")
    @HttpApiMethod(urlMapping = "plugin.coupon.AppMchcoupon.Fenlei")
    public Result fenlei(MainVo vo, @ParamsMapping("mch_id") int mchId)
    {
        try
        {
            return Result.success(pluginCouponService.fenlei(vo, mchId));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("商品")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "mch_id", value = "店铺id", required = true, dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "name", value = "商品名称", dataType = "string", paramType = "form")
    })
    @PostMapping("/mch_product")
    @HttpApiMethod(urlMapping = "plugin.coupon.AppMchcoupon.Product")
    public Result mchProduct(MainVo vo, @ParamsMapping("mch_id") int mchId, String name)
    {
        try
        {
            return Result.success(pluginCouponService.mchProduct(vo, mchId, name));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("店铺优惠券使用记录")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "优惠券活动id", required = true, dataType = "int", paramType = "form"),
    })
    @PostMapping("/mchUseRecord")
    @HttpApiMethod(urlMapping = "plugin.coupon.AppMchcoupon.MchUseRecord", apiKey = "admin.coupon.mchUseRecord")
    public Result mchUseRecord(MainVo vo, Integer id)
    {
        try
        {
            return Result.success(pluginCouponService.mchUseRecord(vo, id));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

}
