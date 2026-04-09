package com.laiketui.apps.app.controller.group;


import com.laiketui.apps.api.app.group.AppsCstrGroupBuyService;
import com.laiketui.core.annotation.ParamsMapping;
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
 * 插件-平台拼团
 *
 * @author Trick
 * @date 2021/2/20 11:12
 */
@Api(tags = "插件-平台拼团")
@RestController
@RequestMapping("/app/pthdGroupBuy")
public class AppsCstrPtGroupBuyController
{

    @Autowired
    private AppsCstrGroupBuyService appsCstrGroupBuyService;

    @ApiOperation("拼团首页列表数据")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "navType", value = "查询类型 1=查询活动结束+进行中的活动", dataType = "int", paramType = "form")
    })
    @PostMapping("/platformGrouphome")
    @HttpApiMethod(urlMapping = "app.pthd_groupbuy.grouphome")
    public Result platformGrouphome(MainVo vo, Integer navType)
    {
        try
        {
            return Result.success(appsCstrGroupBuyService.grouphome(vo, navType));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("获取拼团商品详情(平台)")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "goodsId", value = "商品id", dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "activityId", value = "商品活动id", dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "platformActivityId", value = "平团活动id", dataType = "int", paramType = "form")
    })
    @PostMapping("/pthd_getgoodsdetail")
    @HttpApiMethod(urlMapping = "app.pthd_groupbuy.getgoodsdetail")
    public Result pthdGoodsdetail(MainVo vo, @ParamsMapping("pro_id") int goodsId, @ParamsMapping("activity_no") int activityId,
                                  @ParamsMapping("platform_activities_id") int platformActivityId)
    {
        try
        {
            return Result.success(appsCstrGroupBuyService.getgoodsdetail(vo, goodsId, activityId, platformActivityId));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("拼团订单的订单拆分")
    @PostMapping("/goGroupLeaveSettlement")
    @HttpApiMethod(urlMapping = "app.pthd_groupbuy.goGroupLeaveSettlement")
    public Result grouporderDetail(MainVo vo)
    {
        try
        {
            //拼团订单无需拆分
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("拼团订单明细")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "order_id", value = "订单id", required = true, dataType = "int", paramType = "form")
    })
    @PostMapping("/orderDetails")
    @HttpApiMethod(urlMapping = "app.pthd_groupbuy.order_details")
    public Result orderDetails(MainVo vo, @ParamsMapping("order_id") int orderId)
    {
        try
        {
            return Result.success(appsCstrGroupBuyService.orderDetails(vo, orderId));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("拼团明细")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "orderno", value = "订单号", required = true, dataType = "int", paramType = "form")
    })
    @PostMapping("/groupPthdDetail")
    @HttpApiMethod(urlMapping = "app.pthd_groupbuy.group_other_detail")
    public Result groupPthdDetail(MainVo vo, @ParamsMapping({"oid"}) String orderno)
    {
        try
        {
            return Result.success(appsCstrGroupBuyService.grouporderDetail(vo, orderno));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("查询拼团订单")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "orderType", value = "notpay=未付款 grouping=未发货 success=待收货 status=待收货", dataType = "int", paramType = "form")
    })
    @PostMapping("/groupOrder")
    @HttpApiMethod(urlMapping = "app.pthd_groupbuy.grouporder")
    public Result grouporder(MainVo vo, @ParamsMapping("order_type") String orderType)
    {
        try
        {
            return Result.success(appsCstrGroupBuyService.grouporder(vo, orderType));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

}
