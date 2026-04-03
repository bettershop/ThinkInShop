package com.laiketui.apps.app.controller.substration;

import com.laiketui.apps.api.app.services.substration.AppsCstrSubstrationService;
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

/**
 * 满减
 *
 * @author Trick
 * @date 2021/4/16 14:23
 */
@Api(tags = "插件-满减")
@RestController
@RequestMapping("/app/substration")
public class AppsCstrSubstrationController
{

    @Autowired
    private AppsCstrSubstrationService appsCstrSubstrationService;

    @ApiOperation("满减商品列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "满减活动id", dataType = "int", paramType = "form")
    })
    @PostMapping("/index")
    @HttpApiMethod(urlMapping = "app.subtraction.index")
    public Result index(MainVo vo, int id)
    {
        try
        {
            return Result.success(appsCstrSubstrationService.getSubstrationGoodsList(vo, id));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }
}
