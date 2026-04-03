package com.laiketui.apps.products.controller;

import com.laiketui.apps.api.products.AppsProductService;
import com.laiketui.core.annotation.ParamsMapping;
import com.laiketui.core.domain.Result;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.ProductIndexVo;
import com.laiketui.domain.vo.cart.AddCartVo;
import com.laiketui.root.annotation.HttpApiMethod;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;
import java.util.Map;

/**
 * 商品
 *
 * @author Trick
 * @date 2020/10/26 8:35
 */
@Api(tags = "商品通用api")
@RestController
@Validated
@RequestMapping("/app/product")
public class AppsProductController
{

    @Autowired
    AppsProductService appsProductService;

    @ApiOperation("添加购物车api")
    @PostMapping("addCart")
    @HttpApiMethod(urlMapping = "app.product.add_cart")
    public Result addCart(AddCartVo vo)
    {
        try
        {
            return Result.success(appsProductService.addCart(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    /*@ApiOperation("评价详情显示")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "storeId", value = "商城id", required = true, dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "language", value = "语言", required = true, dataType = "String", paramType = "form"),
            @ApiImplicitParam(name = "accessId", value = "授权id", required = true, dataType = "String", paramType = "form"),
            @ApiImplicitParam(name = "pid", value = "商品id", required = true, dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "type", value = "类型(0=全部,1=好评,2=中评,3=差评,4=有图)", required = true, dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "bargain", value = "是否是交易", required = false, dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "page", value = "分页", required = true, dataType = "int", paramType = "form")
    })
    @PostMapping("getcomment")
    @HttpApiMethod(urlMapping = "app.product.getcomment")
    public Result getcomment(@ParamsMapping("store_id") int storeId, String language, @ParamsMapping("access_id") String accessId,
                             int pid, int type, String bargain, int page) {
        try {
            return Result.success(productService.getcomment(storeId, language, accessId, pid, bargain, type, page));
        } catch (LaiKeAPIException e) {
            return Result.error(e.getCode(), e.getMessage());
        }
    }*/

    @ApiOperation("立即购买api")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "store_id", value = "商城id", required = true, dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "language", value = "语言", required = true, dataType = "String", paramType = "form"),
            @ApiImplicitParam(name = "access_id", value = "授权id", required = true, dataType = "String", paramType = "form"),
            @ApiImplicitParam(name = "product", value = "商品集", required = true, dataType = "String", paramType = "form"),
            @ApiImplicitParam(name = "orderType", value = "商品类型", required = true, dataType = "String", paramType = "form")
    })
    @PostMapping("immediately_cart")
    @HttpApiMethod(urlMapping = "app.product.immediately_cart")
    public Result immediatelyCart(@ParamsMapping({"store_id", "scid"}) int storeId, String language,
                                  @ParamsMapping("access_id") String accessId, String product, String orderType)
    {
        try
        {
            boolean result = appsProductService.immediatelyCart(storeId, language, accessId, product, orderType);
            if (result)
            {
                return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
            }
            else
            {
                return Result.success(GloabConst.ManaValue.MANA_VALUE_FAIL);
            }
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("获取订单商品数据")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "store_id", value = "商城id", required = true, dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "language", value = "语言", required = true, dataType = "String", paramType = "form"),
            @ApiImplicitParam(name = "access_id", value = "授权id", required = true, dataType = "String", paramType = "form"),
            @ApiImplicitParam(name = "order_details_id", value = "订单详情id", required = true, dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "type", value = "类型 0：立即评价 1：追加评价", required = true, dataType = "int", paramType = "form")

    })
    @PostMapping("comment")
    @HttpApiMethod(urlMapping = "app.product.comment")
    public Result getOrderDetailInfo(@RequestParam("store_id") int storeId, @NotNull String language, @RequestParam("access_id") @NotNull String accessId,
                                     @RequestParam("order_details_id") @NotNull int orderDetailsId,Integer type)
    {
        try
        {
            Map<String, Object> resultJson = appsProductService.getOrderDetailInfo(storeId, language, accessId, orderDetailsId,type);

            return Result.success(resultJson);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("base64图片上传")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "base64_image_content", value = "base64", required = true, dataType = "String", paramType = "form"),
            @ApiImplicitParam(name = "path", value = "根路径", required = true, dataType = "String", paramType = "form")
    })
    @PostMapping("base64_image_content")
    public Result base64ImageContent(@RequestParam("base64_image_content") String base64Str, @NotNull String path)
    {
        try
        {
            boolean resultJson = appsProductService.base64ImageContent(base64Str, path);

            return Result.success(resultJson);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("添加/追加评论")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "anonymous", value = "是否匿名", required = true, dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "start", value = "星级", dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "comment", value = "评论内容", required = true, dataType = "String", paramType = "form"),
            @ApiImplicitParam(name = "order_details_id", value = "订单详情id", required = true, dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "imgIndex", value = "上传图片当前图片下标", dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "imgNum", value = "需要上传的数量", dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "image", value = "图片", dataType = "file", paramType = "form")
    })
    @PostMapping("add_comment")
    @HttpApiMethod(urlMapping = {"app.product.add_comment", "app.product.t_comment"})
    public Result addComment(MainVo vo,
                             @NotNull int anonymous, @ParamsMapping("order_details_id") @NotNull int orderDetailsId,
                             Integer start, @NotNull String comment, @ParamsMapping("upload_num") Integer imgIndex,
                             @ParamsMapping("upload_z_num") Integer imgNum, MultipartFile image)
    {
        try
        {
            return Result.success(appsProductService.addComment(vo, anonymous, orderDetailsId, start, comment, imgIndex, imgNum, image));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("多商品添加/追加评论")
    @PostMapping("addBatch_comment")
    @HttpApiMethod(urlMapping = "app.product.addBatch_comment")
    public Result addBatchComment(MainVo vo,String commentList)
    {
        try
        {
            return Result.success(appsProductService.addBatchComment(vo,commentList));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("评论上传图片")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "cid", value = "评论id", required = true, dataType = "int", paramType = "form")
    })
    @PostMapping("img_comment")
    @HttpApiMethod(urlMapping = "app.product.img_comment")
    public Result imgComment(MainVo vo, int cid, MultipartFile image, @ParamsMapping("upload_num") int imgIndex, @ParamsMapping("upload_z_num") int imgNum, Integer type)
    {
        try
        {
            return Result.success(appsProductService.imgComment(vo, cid, image, imgIndex, imgNum, type));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("获取产品详情")
    @PostMapping("productIndex")
    @HttpApiMethod(urlMapping = {"app.product.index"})
    public Result index(ProductIndexVo vo)
    {
        try
        {
            return Result.success(appsProductService.index(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("获取商品评价数据")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "store_id", value = "商城id", required = true, dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "language", value = "语言", required = true, dataType = "String", paramType = "form"),
            @ApiImplicitParam(name = "access_id", value = "授权id", required = true, dataType = "String", paramType = "form"),
            @ApiImplicitParam(name = "orderNo", value = "订单编号", required = true, dataType = "String", paramType = "form"),
            @ApiImplicitParam(name = "type", value = "类型 0：立即评价 1：追加评价", required = true, dataType = "Integer", paramType = "form"),
            @ApiImplicitParam(name = "order_details_id", value = "订单详情id", required = true, dataType = "Integer", paramType = "form")
    })
    @PostMapping("getDetailCommList")
    @HttpApiMethod(urlMapping = "app.product.commentList")
    public Result getDetailCommList(@RequestParam("store_id") Integer storeId, @NotNull String language, @RequestParam("access_id") @NotNull String accessId,
                                     @RequestParam("orderNo") @NotNull String orderNo,Integer type,Integer order_details_id)
    {
        try
        {
            Map<String, Object> resultJson = appsProductService.getDetailCommList(storeId, language, accessId, orderNo,type,order_details_id);
            return Result.success(resultJson);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }
}
