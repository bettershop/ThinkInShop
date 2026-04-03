package com.laiketui.apps.app.controller;

import com.laiketui.apps.api.app.services.AppsCstrCartService;
import com.laiketui.core.annotation.ParamsMapping;
import com.laiketui.core.domain.Result;
import com.laiketui.core.exception.LaiKeAPIException;
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

import javax.validation.constraints.NotNull;

/**
 * 购物车api
 *
 * @author Trick
 * @date 2020/10/20 9:49
 */
@Api(tags = "购物车api")
@RestController
@RequestMapping("/app/shopcart")
public class AppsCstrCartController
{

    @Autowired
    AppsCstrCartService appsCstrCartService;

    @ApiOperation("进入购物车页面api")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "store_id", value = "商城id", required = true, dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "language", value = "语言", required = true, dataType = "String", paramType = "form"),
            @ApiImplicitParam(name = "access_id", value = "授权id", required = true, dataType = "String", paramType = "form"),
            @ApiImplicitParam(name = "page", value = "分页", required = true, dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "commodity_type", value = "商品类型 0.实物商品 1.虚拟商品", required = true, dataType = "int", paramType = "form")
    })
    @PostMapping("cartIndex")
    @HttpApiMethod(urlMapping = "app.cart.index",apiKey = "app.cart.indexonly")
    public Result index(MainVo vo, Integer commodity_type) throws LaiKeAPIException
    {
        try
        {
            return Result.success(appsCstrCartService.index(vo, commodity_type));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("购物车编辑api")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "goods", value = "购物车[{\"num\":2,\"cart_id\":3297},...]", required = true, dataType = "String", paramType = "form")
    })
    @PostMapping("upCart")
    @HttpApiMethod(urlMapping = "app.cart.up_cart")
    public Result upCart(MainVo vo, String goods) throws LaiKeAPIException
    {
        try
        {
            if (appsCstrCartService.upCart(vo, goods))
            {
                return Result.success(true);
            }
            else
            {
                return Result.error(false);
            }
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("删除购物商品api")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "cartId", value = "购物车id", required = true, dataType = "string", paramType = "form")
    })
    @PostMapping("delcart")
    @HttpApiMethod(urlMapping = "app.cart.delcart")
    public Result delcart(MainVo vo, @ParamsMapping("cart_id") String cartId) throws LaiKeAPIException
    {
        try
        {
            if (appsCstrCartService.delcart(vo, cartId))
            {
                return Result.success(true);
            }
            else
            {
                return Result.error(false);
            }
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("清空购物车api")
    @PostMapping("delAllCart")
    public Result delAllCart(MainVo vo) throws LaiKeAPIException
    {
        try
        {
            boolean resultJson = appsCstrCartService.delAllCart(vo);
            if (resultJson)
            {
                return Result.success(true);
            }
            else
            {
                return Result.error(false);
            }
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("修改购物车商品属性api")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "cart_id", value = "购物车id", required = true, dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "attribute_id", value = "属性id", required = true, dataType = "int", paramType = "form")
    })
    @PostMapping("modify_attribute")
    @HttpApiMethod(urlMapping = "app.cart.modify_attribute")
    public Result modifyAttribute(MainVo vo, @ParamsMapping("cart_id") Integer cartId, @ParamsMapping("attribute_id") Integer attributeId) throws LaiKeAPIException
    {
        try
        {
            boolean resultJson = appsCstrCartService.modifyAttribute(vo, cartId, attributeId);
            if (resultJson)
            {
                return Result.success(true);
            }
            else
            {
                return Result.error(false);
            }
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("弹出购物车商品属性api")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "cart_id", value = "购物车id", required = true, dataType = "int", paramType = "form")
    })
    @PostMapping("dj_attribute")
    @HttpApiMethod(urlMapping = "app.cart.dj_attribute")
    public Result djAttribute(MainVo vo, @NotNull @ParamsMapping("cart_id") int cartId) throws LaiKeAPIException
    {
        try
        {
            return Result.success(appsCstrCartService.djAttribute(vo, cartId));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

}
