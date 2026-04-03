package com.laiketui.apps.mch.controller.supplier;

import com.laiketui.apps.api.mch.AppsMchBaseService;
import com.laiketui.apps.api.mch.SupplierMchOrderService;
import com.laiketui.core.annotation.ParamsMapping;
import com.laiketui.core.domain.Result;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.main.RefundVo;
import com.laiketui.domain.vo.mch.MchOrderIndexVo;
import com.laiketui.domain.vo.order.AdminOrderDetailVo;
import com.laiketui.domain.vo.order.AdminOrderVo;
import com.laiketui.domain.vo.order.OrderSettlementVo;
import com.laiketui.domain.vo.pc.MchPcReturnOrderVo;
import com.laiketui.root.annotation.HttpApiMethod;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

/**
 * @Author: sunH_
 * @Date: Create in 18:15 2023/2/14
 */

@Api(tags = "供应商订单-h5店铺")
@RestController
@RequestMapping("/app/supplier/app/mch/orders")
public class SupplierMchOrderController
{

    @Autowired
    private SupplierMchOrderService supplierMchOrderService;

    @ApiOperation("订单列表")
    @PostMapping("/orderIndex")
    @HttpApiMethod(apiKey = "supplier.AppMch.Orders.orderIndex")
    public Result orderIndex(MchOrderIndexVo vo, HttpServletResponse response)
    {
        try
        {
            return Result.success(supplierMchOrderService.orderIndex(vo, response));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("订单详情")
    @PostMapping("/orderDetailsInfo")
    @HttpApiMethod(apiKey = "supplier.AppMch.Orders.orderDetailsInfo")
    public Result orderDetailsInfo(AdminOrderDetailVo orderVo)
    {
        try
        {
            return Result.success(supplierMchOrderService.orderDetailsInfo(orderVo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("关闭订单")
    @PostMapping("/close")
    @HttpApiMethod(apiKey = "supplier.AppMch.Orders.close")
    public Result close(AdminOrderVo orderVo)
    {
        try
        {
            supplierMchOrderService.close(orderVo);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("获取订单物流信息")
    @PostMapping("/kuaidishow")
    @HttpApiMethod(apiKey = "supplier.AppMch.Orders.kuaidishow")
    public Result kuaidishow(MainVo vo, String orderno)
    {
        try
        {
            return Result.success(supplierMchOrderService.kuaidishow(vo, orderno));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("删除订单")
    @PostMapping("/del")
    @HttpApiMethod(apiKey = "supplier.AppMch.orders.del")
    public Result del(MainVo vo, String oid)
    {
        try
        {
            supplierMchOrderService.del(vo, oid);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("退款售后列表")
    @PostMapping("/returnList")
    @HttpApiMethod(apiKey = "supplier.AppMch.Orders.returnList")
    public Result returnList(MchPcReturnOrderVo vo, HttpServletResponse response)
    {
        try
        {
            return Result.success(supplierMchOrderService.returnList(vo, response));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @Autowired
    AppsMchBaseService appsMchBaseService;

    @ApiOperation("售后订单详情")
    @PostMapping("/refundPageById")
    @HttpApiMethod(apiKey = "supplier.AppMch.Orders.refundPageById")
    public Result refundPageById(MainVo vo, @ParamsMapping("shop_id") int shop_id, Integer id, String sNo)
    {
        try
        {
            return Result.success(appsMchBaseService.returnOrderDetails(vo, shop_id, sNo, id));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("售后审核")
    @PostMapping("/examine")
    @HttpApiMethod(apiKey = "supplier.AppMch.Orders.examine")
    public Result examine(RefundVo vo)
    {
        try
        {
            supplierMchOrderService.examine(vo);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("订单结算列表")
    @PostMapping("/settlementIndex")
    @HttpApiMethod(apiKey = "supplier.AppMch.Orders.settlementIndex")
    public Result settlementIndex(OrderSettlementVo vo, HttpServletResponse response)
    {
        try
        {
            return Result.success(supplierMchOrderService.settlementIndex(vo, response));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("查看订单明细")
    @PostMapping("/settlementDetail")
    @HttpApiMethod(apiKey = "supplier.AppMch.Orders.settlementDetail")
    public Result settlementDetail(MainVo vo, String sNo)
    {
        try
        {
            return Result.success(supplierMchOrderService.settlementDetail(vo, sNo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("删除结算订单")
    @PostMapping("/settlementDel")
    @HttpApiMethod(apiKey = "supplier.AppMch.Orders.settlementDel")
    public Result settlementDel(MainVo vo, int id)
    {
        try
        {
            supplierMchOrderService.settlementDel(vo, id);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("一键代发")
    @PostMapping("/oneClickDistribution")
    @HttpApiMethod(apiKey = "supplier.AppMch.Orders.oneClickDistribution")
    public Result oneClickDistribution(MainVo vo, String orders)
    {
        try
        {
            supplierMchOrderService.oneClickDistribution(vo, orders);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("进入供应商管理界面-订单数量")
    @PostMapping("/ordersNum")
    @HttpApiMethod(apiKey = "supplier.AppMch.Orders.num")
    public Result ordersNum(MainVo vo)
    {
        try
        {
            return Result.success(supplierMchOrderService.ordersNum(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }
}
