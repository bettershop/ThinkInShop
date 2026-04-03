package com.laiketui.admins.admin.controller.supplier;

import com.laiketui.admins.api.admin.supplier.AdminSupplierService;
import com.laiketui.core.domain.Result;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.order.AdminOrderDetailVo;
import com.laiketui.domain.vo.order.AdminOrderListVo;
import com.laiketui.domain.vo.order.OrderSettlementVo;
import com.laiketui.domain.vo.supplier.AddSupplierVo;
import com.laiketui.domain.vo.supplier.GetSupplierVo;
import com.laiketui.domain.vo.supplier.SupplierConfigVo;
import com.laiketui.domain.vo.supplier.SupplierWithdrawVo;
import com.laiketui.root.annotation.HttpApiMethod;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

/**
 * @Author: sunH_
 * @Date: Create in 17:00 2022/9/19
 */
@Api(tags = "后台-供应商管理")
@RestController
@RequestMapping("/admin/supplier")
public class AdminSupplierController
{

    @Autowired
    private AdminSupplierService adminSupplierService;

    @ApiOperation("添加修改供应商信息")
    @PostMapping("/addOrUpdate")
    @HttpApiMethod(apiKey = "supplier.Admin.Supplier.AddOrUpdate")
    public Result addOrUpdate(AddSupplierVo vo, BindingResult bindingResult)
    {
        try
        {
            adminSupplierService.addOrUpdate(vo);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("删除供应商")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "供应商id", dataType = "Integer", paramType = "form"),
    })
    @PostMapping("/del")
    @HttpApiMethod(apiKey = "supplier.Admin.Supplier.Del")
    public Result del(MainVo vo, Integer id)
    {
        try
        {
            adminSupplierService.del(vo, id);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("锁定解锁")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "供应商id", dataType = "Integer", paramType = "form"),
    })
    @PostMapping("/lock")
    @HttpApiMethod(apiKey = "supplier.Admin.Supplier.Lock")
    public Result lock(MainVo vo, Integer id)
    {
        try
        {
            adminSupplierService.lock(vo, id);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("查询供应商")
    @PostMapping("/getList")
    @HttpApiMethod(apiKey = "supplier.Admin.Supplier.GetList")
    public Result getList(GetSupplierVo vo, HttpServletResponse response)
    {
        try
        {
            return Result.success(adminSupplierService.getList(vo, response));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("订单列表")
    @PostMapping("/orderIndex")
    @HttpApiMethod(apiKey = "supplier.Admin.SupplierOrders.OrderIndex")
    public Result orderIndex(AdminOrderListVo adminOrderVo, HttpServletResponse response)
    {
        try
        {
            return Result.success(adminSupplierService.orderIndex(adminOrderVo, response));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("订单详情")
    @PostMapping("/orderDetailsInfo")
    @HttpApiMethod(apiKey = "supplier.Admin.SupplierOrders.OrderDetailsInfo")
    public Result orderDetailsInfo(AdminOrderDetailVo orderVo)
    {
        try
        {
            return Result.success(adminSupplierService.orderDetailsInfo(orderVo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("订单结算列表")
    @PostMapping("/settlement")
    @HttpApiMethod(apiKey = "supplier.Admin.SupplierOrders.SettlementList")
    public Result settlementList(OrderSettlementVo vo, HttpServletResponse response)
    {
        try
        {
            return Result.success(adminSupplierService.settlementList(vo, response));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("提现记录")
    @PostMapping("/withdraw")
    @HttpApiMethod(apiKey = "supplier.Admin.Supplier.Withdraw")
    public Result withdraw(SupplierWithdrawVo vo, HttpServletResponse response)
    {
        try
        {
            return Result.success(adminSupplierService.withdraw(vo, response));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("审核提现记录")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "审核记录id", dataType = "Integer", paramType = "form"),
            @ApiImplicitParam(name = "status", value = "状态 0：审核中 1：审核通过 2：拒绝", dataType = "Integer", paramType = "form"),
            @ApiImplicitParam(name = "voucher", value = "凭证", dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "remark", value = "拒绝备注", dataType = "string", paramType = "form")
    })
    @PostMapping("/examineWithdraw")
    @HttpApiMethod(apiKey = "supplier.Admin.Supplier.ExamineWithdraw")
    public Result examineWithdraw(MainVo vo, Integer id, Integer status, String voucher, String remark)
    {
        try
        {
            adminSupplierService.examineWithdraw(vo, id, status, voucher, remark);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("查询供应商配置信息")
    @PostMapping("/getConfig")
    @HttpApiMethod(apiKey = "supplier.Admin.Supplier.GetConfig")
    public Result getConfig(MainVo vo)
    {
        try
        {
            return Result.success(adminSupplierService.getConfig(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("更新供应商配置信息")
    @PostMapping("/addUpdateConfig")
    @HttpApiMethod(apiKey = "supplier.Admin.Supplier.AddUpdateConfig")
    public Result addUpdateConfig(SupplierConfigVo vo)
    {
        try
        {
            adminSupplierService.addUpdateConfig(vo);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("查询供应商经营收益")
    @PostMapping("/income")
    @HttpApiMethod(apiKey = "supplier.Admin.Supplier.Income")
    public Result income(MainVo vo, String startTime, String endTime)
    {
        try
        {
            return Result.success(adminSupplierService.income(vo, startTime, endTime));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("设置供应商商品是否需要审核")
    @PostMapping("/isExamine")
    @HttpApiMethod(apiKey = "supplier.Admin.Supplier.IsExamine")
    public Result isExamine(MainVo vo)
    {
        try
        {
            adminSupplierService.isExamine(vo);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }
}
