package com.laiketui.admins.admin.controller;

import com.laiketui.admins.api.admin.plugin.AdminCouponManageService;
import com.laiketui.core.domain.Result;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.coupon.AddCouponActivityVo;
import com.laiketui.domain.vo.coupon.AddCouponConfigVo;
import com.laiketui.domain.vo.coupon.CouponParmaVo;
import com.laiketui.domain.vo.coupon.CouponUserVo;
import com.laiketui.domain.vo.goods.GoodsConfigureVo;
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
 * 优惠卷管理
 *
 * @author Trick
 * @date 2021/1/22 14:35
 */
@Api(tags = "后台-优惠卷管理")
@RestController
@RequestMapping("/admin/coupon")
public class AdminCouponManageController
{

    @Autowired
    private AdminCouponManageService adminCouponManageService;

    @ApiOperation("获取商城优惠卷配置信息")
    @PostMapping("/getCouponConfigInfo")
    @HttpApiMethod(apiKey = "plugin.coupon.Admincoupon.GetCouponConfigInfo")
    public Result getCouponConfigInfo(MainVo vo)
    {
        try
        {
            return Result.success(adminCouponManageService.getCouponConfigInfo(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("获取所有优惠券类型")
    @PostMapping("/getCouponTypeList")
    @HttpApiMethod(apiKey = "plugin.coupon.Admincoupon.GetCouponTypeList")
    public Result getCouponTypeList(MainVo vo)
    {
        try
        {
            return Result.success(adminCouponManageService.getCouponTypeList(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("添加/编辑优惠卷配置")
    @PostMapping("/addCouponConfig")
    @HttpApiMethod(apiKey = "plugin.coupon.Admincoupon.AddCouponConfig")
    public Result addCouponConfig(AddCouponConfigVo vo)
    {
        try
        {
            return Result.success(adminCouponManageService.addCouponConfig(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("获取优惠卷信息")
    @PostMapping("/getCouponCardInfo")
    @HttpApiMethod(apiKey = "plugin.coupon.Admincoupon.GetCouponCardInfo")
    public Result getCouponCardInfo(CouponParmaVo vo, HttpServletResponse response)
    {
        try
        {
            vo.setResponse(response);
            return Result.success(adminCouponManageService.getCouponCardInfo(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("获取符合赠卷的会员列表")
    @PostMapping("/getGiveUserInfo")
    @HttpApiMethod(apiKey = "plugin.coupon.Admincoupon.GetGiveUserInfo")
    public Result getGiveUserInfo(CouponUserVo vo)
    {
        try
        {
            return Result.success(adminCouponManageService.getGiveUserInfo(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("赠券")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userIds", value = "用户id集", required = true, dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "hid", value = "活动id", required = true, dataType = "int", paramType = "form")
    })
    @PostMapping("/receiveUserCoupon")
    @HttpApiMethod(apiKey = "plugin.coupon.Admincoupon.ReceiveUserCoupon")
    public Result receiveUserCoupon(MainVo vo, String userIds, int hid)
    {
        try
        {
            return Result.success(adminCouponManageService.receiveUserCoupon(vo, userIds, hid));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("活动是否显示-开关")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "hid", value = "活动id", required = true, dataType = "int", paramType = "form")
    })
    @PostMapping("/activityisDisplay")
    @HttpApiMethod(apiKey = "plugin.coupon.Admincoupon.ActivityisDisplay")
    public Result activityisDisplay(MainVo vo, int hid)
    {
        try
        {
            return Result.success(adminCouponManageService.activityisDisplay(vo, hid));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("批量删除优惠卷活动")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "ids", value = "提货券活动id集", required = true, dataType = "string", paramType = "form")
    })
    @PostMapping("/batchDel")
    @HttpApiMethod(apiKey = "plugin.coupon.Admincoupon.BatchDel")
    public Result batchDel(MainVo vo, String ids)
    {
        try
        {
            adminCouponManageService.batchDel(vo, ids);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("删除优惠卷活动")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "hid", value = "活动id", required = true, dataType = "int", paramType = "form")
    })
    @PostMapping("/delCoupon")
    @HttpApiMethod(apiKey = "plugin.coupon.Admincoupon.DelCoupon")
    public Result delCoupon(MainVo vo, int hid)
    {
        try
        {
            return Result.success(adminCouponManageService.delCoupon(vo, hid));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("优惠卷活动领取记录")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "hid", value = "活动id", required = true, dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "status", value = "优惠卷状态 0:未使用 1:使用中 2:已使用 3:已过期", dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "type", value = "优惠券类型", dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "keyWord", value = "订单号/会员名称", dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "exportType", value = "导出 1=导出数据", dataType = "Integer", paramType = "form"),
    })
    @PostMapping("/seeCouponLogger")
    @HttpApiMethod(apiKey = "plugin.coupon.Admincoupon.SeeCouponLogger")
    public Result seeCouponLogger(MainVo vo, int hid, Integer status, Integer type, String keyWord, HttpServletResponse response)
    {
        try
        {
            return Result.success(adminCouponManageService.seeCouponLogger(vo, hid, status, type, keyWord, response));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("优惠卷赠送记录")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "hid", value = "活动id", required = true, dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "state", value = "类型 0:未使用 1:使用中 2:已使用 3:已过期", dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "type", value = "类型", dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "keyWord", value = "订单号/会员名称", dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "exportType", value = "导出 1=导出数据", dataType = "int", paramType = "form"),
    })
    @PostMapping("/seeGiveCouponLogger")
    @HttpApiMethod(apiKey = "plugin.coupon.Admincoupon.SeeGiveCouponLogger")
    public Result seeGiveCouponLogger(MainVo vo, int hid, Integer state, Integer type, String keyWord, HttpServletResponse response)
    {
        try
        {
            return Result.success(adminCouponManageService.seeGiveCouponLogger(vo, hid, state, type, keyWord, response));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("获取指定商品列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "goodsName", value = "商品名称", dataType = "string", paramType = "form"),
    })
    @PostMapping("/getAssignGoods")
    @HttpApiMethod(apiKey = "plugin.coupon.Admincoupon.GetAssignGoods")
    public Result getAssignGoods(MainVo vo, String goodsName)
    {
        try
        {
            return Result.success(adminCouponManageService.getAssignGoods(vo, goodsName));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("获取指定商品")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "商品id", dataType = "string", paramType = "form"),
    })
    @PostMapping("/getSpecifiedGoodsInfo")
    @HttpApiMethod(apiKey = "plugin.coupon.Admincoupon.GetSpecifiedGoodsInfo")
    public Result getSpecifiedGoodsInfo(GoodsConfigureVo vo, String id)
    {
        try
        {
            Map<String, Object> result = adminCouponManageService.getSpecifiedGoodsInfo(vo, id);
            return Result.success(result);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("获取指定分类列表")
    @PostMapping("/getAssignGoodsClass")
    @HttpApiMethod(apiKey = "plugin.coupon.Admincoupon.GetAssignGoodsClass")
    public Result getAssignGoodsClass(MainVo vo)
    {
        try
        {
            return Result.success(adminCouponManageService.getAssignGoodsClass(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("添加/修改优惠卷活动")
    @PostMapping("/addCoupon")
    @HttpApiMethod(apiKey = "plugin.coupon.Admincoupon.AddCoupon")
    public Result addCoupon(AddCouponActivityVo vo)
    {
        try
        {
            return Result.success(adminCouponManageService.addCoupon(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

}
