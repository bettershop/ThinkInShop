package com.laiketui.comps.order.controlller;

import com.laiketui.comps.api.order.CompsOrderTaskService;
import com.laiketui.core.domain.Result;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.root.annotation.HttpApiMethod;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 订单远程调用
 * gp
 */
@Api(tags = "订单远程调用")
@RestController
@RequestMapping("/comps/order/feign")
public class CompsOrderFeignController
{
    @Autowired
    private CompsOrderTaskService compsOrderTaskService;

    @ApiOperation("订单定时结算")
    @RequestMapping("/orderSettlement")
    @HttpApiMethod(apiKey = "laike.order.task.http.orderSettlement")
    public Result orderSettlement(int storeId, int mchId, String orderType)
    {
        try
        {
            return Result.success(compsOrderTaskService.orderSettlement(storeId, mchId, orderType));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }
}
