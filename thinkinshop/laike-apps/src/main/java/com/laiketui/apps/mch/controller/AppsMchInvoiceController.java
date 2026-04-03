package com.laiketui.apps.mch.controller;


import com.laiketui.apps.api.mch.AppsMchInvoiceService;
import com.laiketui.core.domain.Result;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.invoice.AdminInvoiceVo;
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
 * 发票信息管理
 *
 * @author 朱庆宇
 * @date 2024/04/25 14:25
 */
@Api(tags = "店铺移动端-发票信息管理")
@RestController
@RequestMapping("/app/mch/invoice")
public class AppsMchInvoiceController
{

    @Autowired
    private AppsMchInvoiceService appsMchInvoiceService;

    @ApiOperation("查询发票列表")
    @PostMapping("/getList")
    @HttpApiMethod(apiKey = "mch.App.Invoice.GetList")
    public Result getList(AdminInvoiceVo vo, HttpServletResponse response)
    {
        try
        {
            return Result.success(appsMchInvoiceService.getList(vo, response));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("查看发票详情")
    @PostMapping("/getInfo")
    @HttpApiMethod(apiKey = "mch.App.Invoice.GetInfo")
    public Result getInfo(MainVo vo, Integer id)
    {
        try
        {
            return Result.success(appsMchInvoiceService.getInfo(vo, id));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("上传发票文件")
    @PostMapping("/uploadInvoice")
    @HttpApiMethod(apiKey = "mch.App.Invoice.UploadInvoice")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "发票id", dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "filePath", value = "文件路径", dataType = "string", paramType = "form")
    })
    public Result uploadInvoice(MainVo vo, Integer id, String filePath)
    {
        try
        {
            appsMchInvoiceService.uploadInvoice(vo, id, filePath);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("删除发票信息")
    @PostMapping("/delInvoice")
    @HttpApiMethod(apiKey = "mch.App.Invoice.DelInvoice")
    public Result delInvoice(MainVo vo, String id)
    {
        try
        {
            appsMchInvoiceService.delInvoice(vo, id);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }
}
