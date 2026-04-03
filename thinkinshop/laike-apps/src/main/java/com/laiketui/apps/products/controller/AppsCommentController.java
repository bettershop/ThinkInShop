package com.laiketui.apps.products.controller;

import com.laiketui.apps.api.products.AppsCommentService;
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
 * 商品评论
 *
 * @author Trick
 * @date 2023/3/6 11:01
 */
@Api(tags = "移动端-商品评论")
@RestController
@RequestMapping("/app/product/comment")
public class AppsCommentController
{

    @Autowired
    private AppsCommentService appsCommentService;

    @ApiOperation("获取商品评论列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "storeId", value = "商城id", required = true, dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "language", value = "语言", required = true, dataType = "String", paramType = "form"),
            @ApiImplicitParam(name = "accessId", value = "授权id", required = true, dataType = "String", paramType = "form"),
            @ApiImplicitParam(name = "pid", value = "商品id", required = true, dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "type", value = "类型(0=全部,1=好评,2=中评,3=差评,4=有图)", required = true, dataType = "int", paramType = "form"),
    })
    @PostMapping("getComment")
    @HttpApiMethod(urlMapping = "app.product.comment.getComment")
    public Result getComment(MainVo vo, int pid, int type)
    {
        try
        {
            return Result.success(appsCommentService.getComment(vo, pid, type));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("获取商品评论详情")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "commentId", value = "评论id", required = true, dataType = "int", paramType = "form"),
    })
    @PostMapping("getCommentDetail")
    @HttpApiMethod(urlMapping = {"app.product.comment.getCommentDetail", "mall.Product.getCommentDetail"})
    public Result getCommentDetail(MainVo vo, int commentId)
    {
        try
        {
            return Result.success(appsCommentService.getCommentDetail(vo, commentId));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("获取商品详情回复列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "commentId", value = "评论id", required = true, dataType = "int", paramType = "form"),
    })
    @PostMapping("getCommentReplyList")
    @HttpApiMethod(urlMapping = {"app.product.comment.getCommentReplyList", "mall.Product.getCommentReplyList"})
    public Result getCommentReplyList(MainVo vo, int commentId)
    {
        try
        {
            return Result.success(appsCommentService.getCommentReplyList(vo, commentId));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("发表回复")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "commentId", value = "评论id", required = true, dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "sid", value = "上级id", required = false, dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "text", value = "发表内容", required = false, dataType = "string", paramType = "form"),
    })
    @PostMapping("sendComment")
    @HttpApiMethod(urlMapping = {"app.product.comment.sendComment", "mall.Product.sendComment"})
    public Result sendComment(MainVo vo, int commentId, Integer sid, String text)
    {
        try
        {
            appsCommentService.sendComment(vo, commentId, sid, text);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

}
