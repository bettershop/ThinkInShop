package com.laiketui.admins.mch.controller;

import com.laiketui.admins.api.mch.MchSettlemntService;
import com.laiketui.core.domain.Result;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.DictionaryConst;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.order.OrderSettlementVo;
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

/**
 * 订单结算
 *
 * @author Trick
 * @date 2021/6/4 16:43
 */
@Api(tags = "订单结算")
@RestController
@RequestMapping("/admin/mch/settlement/")
public class MchSettlementController
{

    @Autowired
    private MchSettlemntService mchSettlemntService;

    @ApiOperation("订单结算列表")
    @PostMapping("/index")
    @HttpApiMethod(apiKey = "mch.Mch.Settlement.Index")
    public Result index(OrderSettlementVo vo, HttpServletResponse response)
    {
        try
        {
            vo.setOrderType(DictionaryConst.OrdersType.ORDERS_HEADER_GM);
            return Result.success(mchSettlemntService.index(vo, response));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("订单详情")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "orderNo", value = "订单号", dataType = "string", paramType = "form")
    })
    @PostMapping("/detail")
    @HttpApiMethod(apiKey = "mch.Mch.Settlement.Detail")
    public Result detail(MainVo vo, String orderNo)
    {
        try
        {
            return Result.success(mchSettlemntService.orderDetail(vo, orderNo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("删除结算订单")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "订单id", dataType = "int", paramType = "form")
    })
    @PostMapping("/del")
    @HttpApiMethod(apiKey = "mch.Mch.Settlement.Del")
    public Result del(MainVo vo, int id)
    {
        try
        {
            mchSettlemntService.del(vo, id);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

}
