package com.laiketui.apps.app.controller;

import com.laiketui.apps.api.app.AppsCstrSearchService;
import com.laiketui.core.domain.Result;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.app.SearchCatVo;
import com.laiketui.domain.vo.app.SearchGoodsVo;
import com.laiketui.domain.vo.goods.GoodsSearchVo;
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

import java.util.Map;

/**
 * 搜索Api
 *
 * @author Trick
 * @date 2020/10/15 12:36
 */
@Api(tags = "搜索Api")
@RestController
@Validated
@RequestMapping("/app/search")
public class AppsCstrSearchController
{

    @Autowired
    AppsCstrSearchService appsCstrSearchService;


    @ApiOperation("搜索界面api")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "storeId", value = "商城id", required = true, dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "language", value = "语言", required = true, dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "accessId", value = "授权id", required = true, dataType = "String", paramType = "form")
    })
    @PostMapping("searchIndex")
    @HttpApiMethod(urlMapping = "app.search.index")
    public Result index(SearchCatVo vo) throws LaiKeAPIException
    {
        try
        {
            Map<String, Object> resultJson = appsCstrSearchService.index(vo);
            return Result.success(resultJson);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("搜索详情")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "shopId", value = "店铺id", required = true, dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "language", value = "语言", required = true, dataType = "String", paramType = "form"),
            @ApiImplicitParam(name = "accessId", value = "授权id", required = false, dataType = "String", paramType = "form"),
            @ApiImplicitParam(name = "cid", value = "分类id", required = true, dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "page", value = "分页", required = true, dataType = "int", paramType = "form"),
    })
    @PostMapping("listdetail")
    @HttpApiMethod(urlMapping = "app.search.listdetail")
    public Result searchDetail(SearchGoodsVo vo)
    {
        try
        {
            Map<String, Object> resultJson = appsCstrSearchService.searchDetail(vo);
            return Result.success(resultJson);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("热门搜搜api")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "type", value = "分类id", required = true, dataType = "int", paramType = "form")
    })
    @PostMapping("hot_search")
    @HttpApiMethod(urlMapping = "app.search.hot_search")
    public Result hotSearch(MainVo vo, Integer type)
    {
        try
        {
            Map<String, Object> resultJson = appsCstrSearchService.hotSearch(vo, type);
            return Result.success(resultJson);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("普通搜搜api")
    @PostMapping("search")
    @HttpApiMethod(urlMapping = "app.search.search")
    public Result search(GoodsSearchVo vo) throws LaiKeAPIException
    {
        try
        {
            return Result.success(appsCstrSearchService.search(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("搜搜联想api")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "keyword", value = "关键字", required = true, dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "type", value = "分类id", required = true, dataType = "int", paramType = "form")
    })
    @PostMapping("input_search")
    @HttpApiMethod(urlMapping = "app.search.input_search")
    public Result inputSearch(MainVo vo, Integer type, String keyword) throws LaiKeAPIException
    {
        try
        {
            return Result.success(appsCstrSearchService.inputSearch(vo, type, keyword));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


}
