package com.laiketui.apps.mch.controller.supplier;

import com.laiketui.apps.api.mch.SupplierMchGoodsService;
import com.laiketui.core.annotation.ParamsMapping;
import com.laiketui.core.domain.Result;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.admin.DefaultViewVo;
import com.laiketui.domain.vo.mch.UploadMerchandiseVo;
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
 * @Date: Create in 15:04 2022/9/26
 */
@Api(tags = "供应商品-h5店铺")
@RestController
@RequestMapping("/app/supplier/app/mch/goods")
public class SupplierMchGoodsController
{

    @Autowired
    private SupplierMchGoodsService adminMchGoodsService;

    @ApiOperation("查询商品池列表")
    @PostMapping("/proList")
    @HttpApiMethod(apiKey = "supplier.AppMch.Goods.proList")
    public Result proList(GoodsQueryVo vo, HttpServletResponse response)
    {
        try
        {
            return Result.success(adminMchGoodsService.proList(vo, response));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("根据id获取商品信息")
    @PostMapping("/getGoodsInfoById")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "goodId", value = "商品id", dataType = "int", paramType = "form"),
    })
    @HttpApiMethod(apiKey = "supplier.AppMch.Goods.getGoodsInfoById")
    public Result getGoodsInfoById(MainVo vo, int goodId)
    {
        try
        {
            return Result.success(adminMchGoodsService.getGoodsInfoById(vo, goodId));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("删除商品")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "goodsIds", value = "商品id 集", dataType = "string", required = true, paramType = "form")
    })
    @PostMapping("/delGoodsById")
    @HttpApiMethod(apiKey = "supplier.AppMch.Goods.delGoodsById")
    public Result delGoodsById(MainVo vo, String goodsIds)
    {
        try
        {
            adminMchGoodsService.delGoodsById(vo, goodsIds);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("上下架商品")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "goodsIds", value = "商品id集", dataType = "string", paramType = "form")
    })
    @PostMapping("/upperAndLowerShelves")
    @HttpApiMethod(apiKey = "supplier.AppMch.Goods.upperAndLowerShelves")
    public Result upperAndLowerShelves(MainVo vo, String goodsIds, Integer status)
    {
        try
        {
            boolean result = adminMchGoodsService.upperAndLowerShelves(vo, goodsIds, status);
            return Result.success(result);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("代售可选商品列表")
    @PostMapping("/consignmentPro")
    @HttpApiMethod(apiKey = "supplier.AppMch.Goods.consignmentPro")
    public Result consignmentPro(DefaultViewVo vo)
    {
        try
        {
            return Result.success(adminMchGoodsService.consignmentPro(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("修改序号")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "商品id", required = true, dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "sort", value = "序号", required = true, dataType = "int", paramType = "form"),
    })
    @PostMapping("/editSort")
    @HttpApiMethod(apiKey = "supplier.AppMch.Goods.editSort")
    public Result editSort(MainVo vo, Integer id, Integer sort)
    {
        try
        {
            adminMchGoodsService.editSort(vo, id, sort);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("店铺添加供应商商品")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "goodsIds", value = "自选商品id集合','", required = true, dataType = "string", paramType = "form"),
    })
    @PostMapping("/addSupplierPro")
    @HttpApiMethod(apiKey = "supplier.AppMch.Goods.addSupplierPro")
    public Result addSupplierPro(MainVo vo, String goodsIds)
    {
        try
        {
            adminMchGoodsService.addSupplierPro(vo, goodsIds);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("编辑商品")
    @PostMapping("/addGoods")
    @HttpApiMethod(apiKey = "supplier.AppMch.Goods.addGoods")
    public Result addGoods(UploadMerchandiseVo vo)
    {
        try
        {
            return Result.success(adminMchGoodsService.addGoods(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("获取类别信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "classId", value = "类型id", dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "brandId", value = "品牌id", dataType = "int", paramType = "form"),
    })
    @PostMapping("/getClass")
    @HttpApiMethod(apiKey = "supplier.app.mch.GetClass")
    public Result getClass(MainVo vo, Integer classId, Integer brandId)
    {
        try
        {
            return Result.success(adminMchGoodsService.getGoodClass(vo, classId, brandId));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("获取商品的分类和品牌下拉框")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "classId", value = "分类Id", dataType = "Integer", paramType = "form")
    })
    @PostMapping("/queryDropDown")
    @HttpApiMethod(urlMapping = "supplier.AppMch.Goods.queryDropDown")
    public Result queryDropDown(MainVo vo, @ParamsMapping("classId") int classId)
    {
        try
        {
            return Result.success(adminMchGoodsService.queryDropDown(vo, classId));
        }
        catch (LaiKeAPIException e)
        {
            e.printStackTrace();
            return Result.error(e.getCode(), e.getMessage());
        }
    }
}
