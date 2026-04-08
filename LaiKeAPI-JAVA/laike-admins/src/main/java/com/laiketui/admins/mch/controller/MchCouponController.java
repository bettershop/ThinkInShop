package com.laiketui.admins.mch.controller;

import com.laiketui.admins.api.mch.MchCouponService;
import com.laiketui.core.domain.Result;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.coupon.AddCouponActivityVo;
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
 * @date 2021/6/9 15:30
 */
@Api(tags = "优惠卷管理")
@RestController
@RequestMapping("/admin/mch/coupon/")
public class MchCouponController
{

    @Autowired
    private MchCouponService mchCouponService;

    @ApiOperation("获取优惠卷列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "name", value = "优惠卷名称", dataType = "String", paramType = "form"),
            @ApiImplicitParam(name = "activityType", value = "优惠卷类型", dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "receiveType", value = "领取方式", dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "isOverdue", value = "是否过期", dataType = "int", paramType = "form"),
    })
    @PostMapping("/index")
    @HttpApiMethod(apiKey = "plugin.coupon.Mchcoupon.Index")
    public Result index(CouponParmaVo vo, HttpServletResponse response)
    {
        try
        {
            vo.setResponse(response);
            return Result.success(mchCouponService.index(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("添加优惠卷")
    @PostMapping("/add")
    @HttpApiMethod(apiKey = "plugin.coupon.Mchcoupon.Add")
    public Result add(AddCouponActivityVo vo)
    {
        try
        {
            mchCouponService.add(vo);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("添加优惠卷页面")
    @PostMapping("/addPage")
    @HttpApiMethod(apiKey = "plugin.coupon.Mchcoupon.AddPage")
    public Result addPage(MainVo vo)
    {
        try
        {
            return Result.success(mchCouponService.addPage(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("编辑优惠卷")
    @PostMapping("/modify")
    @HttpApiMethod(apiKey = "admin.mch.coupon.modify")
    public Result modify(AddCouponActivityVo vo)
    {
        try
        {
            mchCouponService.modify(vo);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("编辑优惠卷页面")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "优惠卷id", required = true, dataType = "int", paramType = "form")
    })
    @PostMapping("/modify_page")
    @HttpApiMethod(apiKey = "plugin.coupon.Mchcoupon.ModifyPage")
    public Result modifyPage(CouponParmaVo vo)
    {
        try
        {
            return Result.success(mchCouponService.modifyPage(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("查看优惠卷")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "优惠卷id", required = true, dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "mchId", value = "店铺id", dataType = "int", paramType = "form"),
    })
    @PostMapping("/see_coupon")
    @HttpApiMethod(apiKey = "admin.mch.coupon.see_coupon")
    public Result seeCoupon(CouponParmaVo vo)
    {
        try
        {
            return Result.success(mchCouponService.seeCoupon(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("批量删除")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "ids", value = "提货券活动id集", required = true, dataType = "string", paramType = "form")
    })
    @PostMapping("/batchDel")
    @HttpApiMethod(apiKey = "plugin.coupon.Mchcoupon.BatchDel")
    public Result batchDel(MainVo vo, String ids)
    {
        try
        {
            mchCouponService.batchDel(vo, ids);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("删除优惠卷")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "优惠卷id", required = true, dataType = "int", paramType = "form")
    })
    @PostMapping("/del")
    @HttpApiMethod(apiKey = "plugin.coupon.Mchcoupon.DelCoupon")
    public Result del(MainVo vo, int id)
    {
        try
        {
            mchCouponService.del(vo, id);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("分类")
    @PostMapping("/fenlei")
    @HttpApiMethod(apiKey = "plugin.coupon.Mchcoupon.Fenlei")
    public Result fenlei(MainVo vo)
    {
        try
        {
            return Result.success(mchCouponService.fenlei(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("商品")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "name", value = "商品名称", dataType = "string", paramType = "form")
    })
    @PostMapping("/mch_product")
    @HttpApiMethod(apiKey = "admin.mch.coupon.product")
    public Result mchProduct(MainVo vo, String name)
    {
        try
        {
            return Result.success(mchCouponService.mchProduct(vo, name));
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
    @HttpApiMethod(apiKey = "plugin.coupon.Mchcoupon.GetAssignGoods")
    public Result getSpecifiedGoodsInfo(GoodsConfigureVo vo, String id)
    {
        try
        {
            Map<String, Object> result = mchCouponService.getSpecifiedGoodsInfo(vo, id);
            return Result.success(result);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("用户信息")
    @PostMapping("/getGiveUserInfo")
    @HttpApiMethod(apiKey = "plugin.coupon.Mchcoupon.GetGiveUserInfo")
    public Result getGiveUserInfo(CouponUserVo vo)
    {
        try
        {
            return Result.success(mchCouponService.getGiveUserInfo(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("赠卷")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userIds", value = "会员id集", dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "hid", value = "优惠卷id", dataType = "int", paramType = "form")
    })
    @PostMapping("/receiveUserCoupon")
    @HttpApiMethod(apiKey = "plugin.coupon.Mchcoupon.ReceiveUserCoupon")
    public Result receiveUserCoupon(MainVo vo, String userIds, int hid)
    {
        try
        {
            mchCouponService.receiveUserCoupon(vo, userIds, hid);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("获取优惠卷领取记录")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "status", value = "优惠卷状态 0=未使用 1=使用中 2=已使用 3=已过期", dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "hid", value = "优惠卷id", dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "type", value = "优惠券类型", dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "keyWord", value = "订单号/会员名称", dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "exportType", value = "导出 1=导出数据", dataType = "Integer", paramType = "form"),
    })
    @PostMapping("/seeCouponLogger")
    @HttpApiMethod(apiKey = "plugin.coupon.Mchcoupon.SeeCouponLogger")
    public Result seeCouponLogger(MainVo vo, int hid, Integer status, Integer type, String keyWord, HttpServletResponse response)
    {
        try
        {
            return Result.success(mchCouponService.seeCouponLogger(vo, hid, status, type, keyWord, response));
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
    @HttpApiMethod(apiKey = "plugin.coupon.Mchcoupon.SeeGiveCouponLogger")
    public Result seeGiveCouponLogger(MainVo vo, int hid, Integer state, Integer type, String keyWord, HttpServletResponse response)
    {
        try
        {
            return Result.success(mchCouponService.seeGiveCouponLogger(vo, hid, state, type, keyWord, response));
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
    @HttpApiMethod(apiKey = "plugin.coupon.Mchcoupon.ActivityisDisplay")
    public Result activityisDisplay(MainVo vo, int hid)
    {
        try
        {
            return Result.success(mchCouponService.activityisDisplay(vo, hid));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("开启店铺主页领卷入口-开关")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "mchId", value = "店铺id", required = true, dataType = "int", paramType = "form")
    })
    @PostMapping("/isOpenCoupon")
    @HttpApiMethod(apiKey = "plugin.coupon.Mchcoupon.IsOpenCoupon")
    public Result isOpenCoupon(MainVo vo, Integer mchId)
    {
        try
        {
            return Result.success(mchCouponService.isOpenCoupon(vo, mchId));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }
}
