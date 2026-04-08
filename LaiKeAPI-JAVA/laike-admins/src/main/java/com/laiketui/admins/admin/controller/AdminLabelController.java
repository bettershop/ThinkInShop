package com.laiketui.admins.admin.controller;

import com.laiketui.admins.api.admin.AdminLabelService;
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
 * 商品标签管理
 *
 * @author Trick
 * @date 2021/6/25 18:04
 */
@Api(tags = "后台-商品标签")
@RestController
@RequestMapping("/admin/label")
public class AdminLabelController
{

    @Autowired
    private AdminLabelService adminLabelService;


    @ApiOperation("商品标签列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "主键id", dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "name", value = "商品名称", dataType = "string", paramType = "form"),
    })
    @PostMapping("/index")
    @HttpApiMethod(apiKey = "admin.label.index")
    public Result index(MainVo vo, String name, Integer id)
    {
        try
        {
            return Result.success(adminLabelService.index(vo, name, id));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("添加/编辑商品标签")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "主键id", dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "name", value = "商品名称", required = true, dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "color", value = "颜色 1.红色 2.橘色 3.黄色 4.绿色 5.蓝色", required = true, dataType = "int", paramType = "form"),
    })
    @PostMapping("/addGoodsLabel")
    @HttpApiMethod(apiKey = "admin.label.addGoodsLabel")
    public Result addGoodsLabel(MainVo vo, String name, Integer id, String color)
    {
        try
        {
            adminLabelService.addGoodsLabel(vo, name, id, color);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("删除商品标签")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "主键id", required = true, dataType = "int", paramType = "form")
    })
    @PostMapping("/delGoodsLabel")
    @HttpApiMethod(apiKey = "admin.label.delGoodsLabel")
    public Result delGoodsLabel(MainVo vo, int id)
    {
        try
        {
            adminLabelService.delGoodsLabel(vo, id);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }
}
