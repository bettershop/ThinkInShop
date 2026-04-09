package com.laiketui.comps.invoice.controller;


import com.laiketui.comps.api.invoice.CompsInvoiceHeaderService;
import com.laiketui.core.domain.Result;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.invoice.InvoiceHeaderVo;
import com.laiketui.root.annotation.HttpApiMethod;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 发票抬头
 *
 * @author sunH
 * @date 2022/07/28 14:10
 */
@Api(tags = "发票抬头管理")
@RestController
@RequestMapping("/comps/invoiceHeader")
public class CompsInvoiceHeaderController
{

    @Autowired
    private CompsInvoiceHeaderService compsInvoiceHeaderService;

    @ApiOperation("查询我的发票抬头")
    @PostMapping(value = "/getList")
    @HttpApiMethod(urlMapping = "app.invoiceHeader.getList")
    public Result getList(MainVo vo, Integer id)
    {
        try
        {
            return Result.success(compsInvoiceHeaderService.getList(vo, id));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("添加/修改发票抬头信息")
    @PostMapping("/addOrUpdate")
    @HttpApiMethod(urlMapping = "app.invoiceHeader.addOrUpdate")
    public Result addOrUpdate(InvoiceHeaderVo vo)
    {
        try
        {
            compsInvoiceHeaderService.addOrUpdate(vo);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("删除发票抬头信息")
    @PostMapping("/del")
    @HttpApiMethod(urlMapping = "app.invoiceHeader.del")
    public Result del(MainVo vo, String ids)
    {
        try
        {
            compsInvoiceHeaderService.del(vo, ids);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("获得默认发票抬头信息")
    @PostMapping(value = "/getDefault")
    @HttpApiMethod(urlMapping = "app.invoiceHeader.getDefault")
    public Result getDefault(MainVo vo)
    {
        try
        {
            return Result.success(compsInvoiceHeaderService.getDefault(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }
}
