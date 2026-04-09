package com.laiketui.admins.mch.plugin.order;

import com.laiketui.admins.api.mch.plugin.order.MchIntegralOrderService;
import com.laiketui.admins.api.mch.plugin.order.MchRefundService;
import com.laiketui.core.domain.Result;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.DictionaryConst;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.order.AdminOrderListVo;
import com.laiketui.domain.vo.order.OrderSettlementVo;
import com.laiketui.domain.vo.order.RefundQueryVo;
import com.laiketui.root.annotation.HttpApiMethod;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

/**
 * 积分订单管理
 *
 * @author Trick
 * @date 2021/11/2 15:15
 */
@Api(tags = "积分订单管理")
@RestController
@RequestMapping("/pc/integral/order")
public class MchIntegralOrderController
{

    @Autowired
    @Qualifier("pcIntegralOrderServiceImpl")
    private MchIntegralOrderService integralOrderService;

    @ApiOperation("订单列表")
    @PostMapping("/index")
    @HttpApiMethod(apiKey = "pc.integral.order.index")
    public Result index(AdminOrderListVo vo, HttpServletResponse response)
    {
        try
        {
            vo.setSelfLifting(DictionaryConst.GoodsActive.GOODSACTIVE_INTEGRAL);
            return Result.success(integralOrderService.index(vo, response));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("关闭订单")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "订单id", dataType = "int", paramType = "form")
    })
    @PostMapping("/closeOrder")
    @HttpApiMethod(apiKey = "pc.integral.order.closeOrder")
    public Result closeOrder(MainVo vo, Integer id)
    {
        try
        {
            integralOrderService.closeOrder(vo, id);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("删除订单")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "订单id", dataType = "int", paramType = "form")
    })
    @PostMapping("/delOrder")
    @HttpApiMethod(apiKey = "pc.integral.order.delOrder")
    public Result delOrder(MainVo vo, String id)
    {
        try
        {
            integralOrderService.delOrder(vo, id);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @Autowired
    private MchRefundService refundService;

    @ApiOperation("获取积分售后列表")
    @PostMapping("/getRefundList")
    @HttpApiMethod(apiKey = "pc.integral.order.getRefundList")
    public Result getRefundList(RefundQueryVo vo, HttpServletResponse response)
    {
        try
        {
            vo.setOrderType(DictionaryConst.OrdersType.ORDERS_HEADER_IN);
            return Result.success(refundService.getRefundList(vo, response));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("订单结算列表")
    @PostMapping("/orderSettlement")
    @HttpApiMethod(apiKey = "pc.integral.order.orderSettlement")
    public Result orderSettlement(OrderSettlementVo vo, HttpServletResponse response)
    {
        try
        {
            return Result.success(integralOrderService.orderSettlement(vo, response));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

}
