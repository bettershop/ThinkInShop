package com.laiketui.admins.admin.controller.supplier;

import com.laiketui.admins.api.admin.supplier.AdminSupplierBrandService;
import com.laiketui.core.domain.Result;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.root.annotation.HttpApiMethod;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author: sunH_
 * @Date: Create in 17:00 2022/9/19
 */
@Api(tags = "后台-供应商商品品牌管理")
@RestController
@RequestMapping("/admin/supplierBrand")
public class AdminSupplierBrandController
{

    @Autowired
    private AdminSupplierBrandService adminSupplierBrandService;

    @ApiOperation("查询审核列表")
    @PostMapping("/auditList")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "condition", value = "多条件", dataType = "String", paramType = "form"),
            @ApiImplicitParam(name = "status", value = "审核状态 0.待审核 1.审核通过 2.不通过", dataType = "Integer", paramType = "form"),
            @ApiImplicitParam(name = "startTime", value = "开始时间", dataType = "String", paramType = "form"),
            @ApiImplicitParam(name = "endTime", value = "结束时间", dataType = "String", paramType = "form"),
    })
    @HttpApiMethod(apiKey = "admin.supplierBrand.auditList")
    public Result auditList(MainVo vo, Integer id, String condition, Integer status, String startTime, String endTime)
    {
        try
        {
            return Result.success(adminSupplierBrandService.auditList(vo, id, condition, status, startTime, endTime));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("审核品牌")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "供应商商品品牌id", dataType = "Integer", paramType = "form"),
            @ApiImplicitParam(name = "status", value = "审核状态 0.待审核 1.审核通过 2.不通过", dataType = "Integer", paramType = "form"),
            @ApiImplicitParam(name = "remark", value = "备注", dataType = "String", paramType = "form"),
    })
    @PostMapping("/examine")
    @HttpApiMethod(apiKey = "admin.supplierBrand.examine")
    public Result examine(MainVo vo, Integer id, Integer status, String remark)
    {
        try
        {
            adminSupplierBrandService.examine(vo, id, status, remark);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

}
