package com.laiketui.admins.admin.controller.supplier;

import com.laiketui.admins.api.admin.supplier.AdminSupplierClassService;
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
@Api(tags = "后台-供应商商品分类管理")
@RestController
@RequestMapping("/admin/supplierClass")
public class AdminSupplierClassController
{

    @Autowired
    private AdminSupplierClassService adminSupplierClassService;

    @ApiOperation("查询审核列表")
    @PostMapping("/auditList")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "condition", value = "多条件", dataType = "String", paramType = "form"),
            @ApiImplicitParam(name = "status", value = "审核状态 0.待审核 1.审核通过 2.不通过", dataType = "Integer", paramType = "form"),
            @ApiImplicitParam(name = "startTime", value = "开始时间", dataType = "String", paramType = "form"),
            @ApiImplicitParam(name = "endTime", value = "结束时间", dataType = "String", paramType = "form"),
    })
    @HttpApiMethod(apiKey = "admin.supplierClass.auditList")
    public Result auditList(MainVo vo, String condition, Integer status, String startTime, String endTime)
    {
        try
        {
            return Result.success(adminSupplierClassService.auditList(vo, condition, status, startTime, endTime));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("审核分类")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "供应商商品分类id", dataType = "Integer", paramType = "form"),
            @ApiImplicitParam(name = "status", value = "审核状态 0.待审核 1.审核通过 2.不通过", dataType = "Integer", paramType = "form"),
            @ApiImplicitParam(name = "remark", value = "备注", dataType = "String", paramType = "form"),
    })
    @PostMapping("/examine")
    @HttpApiMethod(apiKey = "admin.supplierClass.examine")
    public Result examine(MainVo vo, Integer id, Integer status, String remark)
    {
        try
        {
            adminSupplierClassService.examine(vo, id, status, remark);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("找当前类别所有上级")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "classId", value = "类别id", dataType = "int", paramType = "form")
    })
    @PostMapping("/getClassLevelTopAllInfo")
    @HttpApiMethod(urlMapping = {"admin.supplier.goods.getClassLevelTopAllInfo"})
    public Result getClassLevelTopAllInfo(MainVo vo, int classId)
    {
        try
        {
            return Result.success(adminSupplierClassService.getClassLevelTopAllInfo(vo, classId));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }
}
