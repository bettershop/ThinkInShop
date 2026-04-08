package com.laiketui.apps.app.controller;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.laiketui.apps.api.app.services.AppsCstrCodeService;
import com.laiketui.common.utils.HttpApiUtils;
import com.laiketui.core.annotation.ParamsMapping;
import com.laiketui.core.domain.Result;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.utils.tool.StringUtils;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.goods.ShareVo;
import com.laiketui.root.annotation.HttpApiMethod;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 二维码
 *
 * @author Trick
 * @date 2020/12/17 12:02
 */
@RestController
@RequestMapping("/app/code")
public class AppsCstrCodeController
{

    @Autowired
    private AppsCstrCodeService appsCstrCodeService;

    @Autowired
    private HttpApiUtils httpApiUtils;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @ApiOperation("生成小程序二维码")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "scene", value = "参数", required = true, dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "path", value = "扫码进入的小程序页面路径(路径需要正式版的)", required = true, dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "id", value = "id4", required = true, dataType = "int", paramType = "form")
    })
    @PostMapping("/getShareQrcode")
    public Result getShareQrcode(MainVo vo, String scene, String path, String id)
    {
        try
        {
            return Result.success(appsCstrCodeService.getShareQrcode(vo, scene, 430, path, id));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("获取商品二维码所需要的信息")
    @PostMapping("/rqCodeInfo")
    @HttpApiMethod(urlMapping = "app.getCode.rqCodeInfo")
    public Result rqCodeInfo(ShareVo vo, HttpServletRequest request)
    {
        try
        {
            return Result.success(appsCstrCodeService.rqCodeInfo(vo, request.getContextPath()));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("商品分享二维码")
    @PostMapping("/share")
    @HttpApiMethod(urlMapping = "app.getcode.share")
    public Result shareShop(ShareVo vo, HttpServletRequest request)
    {
        try
        {
            return Result.success(appsCstrCodeService.goodsShare(vo, request.getContextPath()));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("店铺二维码")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "shopId", value = "店铺id", required = true, dataType = "int", paramType = "form"),
    })
    @PostMapping("/shareShop")
    @HttpApiMethod(urlMapping = "app.getcode.share_shop")
    public Result shareShop(MainVo vo, @ParamsMapping("shop_id") int id, HttpServletRequest request)
    {
        try
        {
            return Result.success(appsCstrCodeService.shareShop(vo, id, request.getContextPath()));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("获取小程序二维码参数")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "key", value = "参数密钥", required = true, dataType = "string", paramType = "form"),
    })
    @PostMapping("/getCodeParameter")
    @HttpApiMethod(urlMapping = "app.getcode.getCodeParameter")
    public Result getCodeParameter(MainVo vo, String key)
    {
        try
        {
            return Result.success(appsCstrCodeService.getCodeParameter(vo, key));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("二维码分享-api调用方式-通用接口")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "主键id", required = true, dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "parameter", value = "json参数 {apiUrl:'api',path:'xx/xx/xx'}", required = true, dataType = "string", paramType = "form"),
    })
    @PostMapping("/shareQrCode")
    @HttpApiMethod(urlMapping = "app.code.shareQrCode")
    public Result shareShop(MainVo vo, String id, String parameter)
    {
        try
        {
            if (StringUtils.isEmpty(parameter) || StringUtils.isEmpty(id))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误");
            }
            Map<String, Object> dataMap = JSON.parseObject(parameter, new TypeReference<Map<String, Object>>()
            {
            });
            Map<String, Object> paramApiMap = JSON.parseObject(JSON.toJSONString(vo));
            paramApiMap.put("id", id);
            paramApiMap.put("path", MapUtils.getString(dataMap, "path"));
            logger.info("远程调用参数：{}", paramApiMap);
            return Result.success(httpApiUtils.executeHttpApi(MapUtils.getString(dataMap, "apiUrl"), paramApiMap, MediaType.MULTIPART_FORM_DATA_VALUE));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }
}
