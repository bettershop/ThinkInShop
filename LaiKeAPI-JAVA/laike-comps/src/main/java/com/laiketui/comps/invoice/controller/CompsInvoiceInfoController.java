package com.laiketui.comps.invoice.controller;


import com.laiketui.comps.api.invoice.CompsInvoiceInfoService;
import com.laiketui.core.domain.Result;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.invoice.InvoiceInfoVo;
import com.laiketui.root.annotation.HttpApiMethod;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * H5发票信息管理
 *
 * @author sunH
 * @date 2022/07/28 14:10
 */
@Api(tags = "H5发票信息管理")
@RestController
@RequestMapping("/comps/invoiceInfo")
public class CompsInvoiceInfoController
{

    @Autowired
    private CompsInvoiceInfoService compsInvoiceInfoService;

    @ApiOperation("查询可以开票的订单")
    @PostMapping(value = "/getToInvoiced")
    @HttpApiMethod(urlMapping = "app.invoiceInfo.getToInvoiced")
    public Result getToInvoiced(MainVo vo)
    {
        try
        {
            return Result.success(compsInvoiceInfoService.getToInvoiced(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("查询用户发票列表")
    @PostMapping("/getInvoiceInfo")
    @HttpApiMethod(urlMapping = "app.invoiceInfo.getInvoiceInfo")
    public Result getInvoiceInfo(MainVo vo, Integer status)
    {
        try
        {
            return Result.success(compsInvoiceInfoService.getInvoiceInfo(vo, status));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("查询用户发票详情")
    @PostMapping("/getDetails")
    @HttpApiMethod(urlMapping = "app.invoiceInfo.getDetails")
    public Result getDetails(MainVo vo, Integer id)
    {
        try
        {
            return Result.success(compsInvoiceInfoService.getDetails(vo, id));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("申请开票")
    @PostMapping("/applyInvoicing")
    @HttpApiMethod(urlMapping = "app.invoiceInfo.applyInvoicing")
    public Result applyInvoicing(InvoiceInfoVo vo)
    {
        try
        {
            compsInvoiceInfoService.applyInvoicing(vo);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("撤销")
    @PostMapping(value = "/revoke")
    @HttpApiMethod(urlMapping = "app.invoiceInfo.revoke")
    public Result revoke(MainVo vo, Integer id)
    {
        try
        {
            compsInvoiceInfoService.revoke(vo, id);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }
}
