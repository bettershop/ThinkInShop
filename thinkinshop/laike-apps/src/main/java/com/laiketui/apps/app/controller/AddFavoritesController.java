package com.laiketui.apps.app.controller;

import com.laiketui.apps.api.app.services.AppsCstrAddFavoritesService;
import com.laiketui.core.annotation.ParamsMapping;
import com.laiketui.core.domain.Result;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.DictionaryConst;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.root.annotation.HttpApiMethod;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 收藏 api
 *
 * @author Trick
 * @date 2020/10/22 15:16
 */
@Api(tags = "商品收藏")
@RestController
@Validated
@RequestMapping("/app/favorites")
public class AddFavoritesController
{

    @Autowired
    AppsCstrAddFavoritesService appsCstrAddFavoritesService;

    @ApiOperation("点击收藏商品api")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "store_id", value = "商城id", required = true, dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "language", value = "语言", required = true, dataType = "String", paramType = "form"),
            @ApiImplicitParam(name = "access_id", value = "授权id", required = true, dataType = "String", paramType = "form"),
            @ApiImplicitParam(name = "pro_id", value = "商品id", required = true, dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "type", value = "收藏类型", required = true, dataType = "int", paramType = "form")
    })
    @PostMapping("/favoritesIndex")
    @HttpApiMethod(urlMapping = "app.addFavorites.index")
    public Result index(MainVo vo,
                        @ParamsMapping("pro_id") int pid, Integer type) throws LaiKeAPIException
    {
        try
        {
            if (type == null)
            {
                type = DictionaryConst.UserCollectionType.COLLECTIONTYPE1;
            }
            return Result.success(appsCstrAddFavoritesService.index(vo, pid, type));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("收藏列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "type", value = "类型 1=商品收藏 2=店铺收藏", required = true, dataType = "int", paramType = "form")
    })
    @PostMapping("/collection")
    @HttpApiMethod(urlMapping = "app.addFavorites.collection")
    public Result collection(MainVo vo, int type) throws LaiKeAPIException
    {
        try
        {
            return Result.success(appsCstrAddFavoritesService.collection(vo, type));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("取消收藏")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "type", value = "类型 1=普通收藏 2=积分商城", required = true, dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "collection", value = "收藏id,多个用’,‘分隔", required = true, dataType = "string", paramType = "form")
    })
    @PostMapping("/removeFavorites")
    @HttpApiMethod(urlMapping = "app.addFavorites.removeFavorites")
    public Result removeFavorites(MainVo vo, String collection, Integer type) throws LaiKeAPIException
    {
        try
        {
            if (type == null)
            {
                type = DictionaryConst.UserCollectionType.COLLECTIONTYPE1;
            }
            appsCstrAddFavoritesService.removeFavorites(vo, collection, type);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("找相似")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pro_id", value = "商品id", required = true, dataType = "int", paramType = "form")
    })
    @PostMapping("/similar")
    @HttpApiMethod(urlMapping = "app.addFavorites.similar")
    public Result similar(MainVo vo, @ParamsMapping("pro_id") int goodsId) throws LaiKeAPIException
    {
        try
        {
            return Result.success(appsCstrAddFavoritesService.similar(vo, goodsId));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

}
