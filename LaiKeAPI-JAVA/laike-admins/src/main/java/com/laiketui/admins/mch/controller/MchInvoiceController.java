package com.laiketui.admins.mch.controller;

import com.laiketui.admins.api.mch.MchInvoiceService;
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
 * @author sunH
 * @date 2022/07/29 10:25
 */
@Api(tags = "后台-发票信息管理")
@RestController
@RequestMapping("/admin/mch/invoice")
public class MchInvoiceController
{

    @Autowired
    private MchInvoiceService mchInvoiceService;

    @ApiOperation("查询发票列表")
    @PostMapping("/getList")
    @HttpApiMethod(apiKey = "mch.Mch.Invoice.GetList")
    public Result getList(AdminInvoiceVo vo, HttpServletResponse response)
    {
        try
        {
            return Result.success(mchInvoiceService.getList(vo, response));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("查看发票详情")
    @PostMapping("/getInfo")
    @HttpApiMethod(apiKey = "mch.Mch.Invoice.GetInfo")
    public Result getInfo(MainVo vo, Integer id)
    {
        try
        {
            return Result.success(mchInvoiceService.getInfo(vo, id));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("上传发票文件")
    @PostMapping("/uploadInvoice")
    @HttpApiMethod(apiKey = "mch.Mch.Invoice.UploadInvoice")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "发票id", dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "filePath", value = "文件路径", dataType = "string", paramType = "form")
    })
    public Result uploadInvoice(MainVo vo, Integer id, String filePath)
    {
        try
        {
            mchInvoiceService.uploadInvoice(vo, id, filePath);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("删除发票信息")
    @PostMapping("/delInvoice")
    @HttpApiMethod(apiKey = "mch.Mch.Invoice.DelInvoice")
    public Result delInvoice(MainVo vo, String ids)
    {
        try
        {
            mchInvoiceService.delInvoice(vo, ids);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }
}
