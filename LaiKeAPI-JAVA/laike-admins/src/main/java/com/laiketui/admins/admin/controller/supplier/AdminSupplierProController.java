package com.laiketui.admins.admin.controller.supplier;

import com.laiketui.admins.api.admin.supplier.AdminSupplierProService;
import com.laiketui.core.domain.Result;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.supplier.GoodsQueryVo;
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
 * @Author: sunH_
 * @Date: Create in 17:00 2022/9/19
 */
@Api(tags = "后台-供应商商品管理")
@RestController
@RequestMapping("/admin/supplierPro")
public class AdminSupplierProController
{

    @Autowired
    private AdminSupplierProService adminSupplierProService;

    @ApiOperation("查询商品池")
    @PostMapping("/proList")
    @HttpApiMethod(apiKey = "supplier.Admin.SupplierPro.ProList")
    public Result proList(GoodsQueryVo vo, HttpServletResponse response)
    {
        try
        {
            return Result.success(adminSupplierProService.proList(vo, response));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("审核商品")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "供应商商品id", dataType = "Integer", paramType = "form"),
            @ApiImplicitParam(name = "status", value = "审核状态：1.待审核，2.审核通过，3.审核不通过，4.暂不审核", dataType = "Integer", paramType = "form"),
            @ApiImplicitParam(name = "remark", value = "备注", dataType = "String", paramType = "form"),
    })
    @PostMapping("/examine")
    @HttpApiMethod(apiKey = "supplier.Admin.SupplierPro.Examine")
    public Result examine(MainVo vo, Integer id, Integer status, String remark)
    {
        try
        {
            adminSupplierProService.examine(vo, id, status, remark);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("删除商品")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "供应商商品id", dataType = "Integer", paramType = "form"),
    })
    @PostMapping("/del")
    @HttpApiMethod(apiKey = "supplier.Admin.SupplierPro.Del")
    public Result del(MainVo vo, Integer id)
    {
        try
        {
            adminSupplierProService.del(vo, id);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("违规下架")
    @PostMapping("/violation")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "供应商商品id", dataType = "Integer", paramType = "form"),
            @ApiImplicitParam(name = "remark", value = "备注", dataType = "String", paramType = "form"),
            @ApiImplicitParam(name = "img", value = "图片", dataType = "String", paramType = "form"),
    })
    @HttpApiMethod(apiKey = "supplier.Admin.SupplierPro.Violation")
    public Result getList(MainVo vo, Integer id, String remark, String img)
    {
        try
        {
            adminSupplierProService.violation(vo, id, remark, img);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("修改排序值")
    @PostMapping("/sortUpdate")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "供应商商品id", dataType = "Integer", paramType = "form"),
            @ApiImplicitParam(name = "sort", value = "排序值", dataType = "Integer", paramType = "form"),
    })
    @HttpApiMethod(apiKey = "supplier.Admin.SupplierPro.SortUpdate")
    public Result sortUpdate(MainVo vo, Integer id, Integer sort)
    {
        try
        {
            adminSupplierProService.sortUpdate(vo, id, sort);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }
}
