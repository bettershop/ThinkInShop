package com.laiketui.apps.user.controller;

import com.laiketui.apps.api.user.AppsUserRechargeService;
import com.laiketui.core.domain.Result;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.utils.tool.DataUtils;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.goods.ProductShareVo;
import com.laiketui.root.annotation.HttpApiMethod;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;


/**
 * 会员制
 *
 * @author Trick
 * @date 2020/12/21 17:34
 */
@Api(tags = "会员制")
@RestController
@RequestMapping("/app/user/recharge")
public class AppsUserRechargeController
{

    @Autowired
    private AppsUserRechargeService appsUserRechargeService;

    @ApiOperation("请求我的详细数据")
    @PostMapping("/index")
    @HttpApiMethod(urlMapping = "app.recharge.index")
    public Result index(MainVo vo)
    {
        try
        {
            return Result.success(appsUserRechargeService.index(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("会员等级列表展示")
    @PostMapping("/grade")
    @HttpApiMethod(urlMapping = "app.recharge.grade")
    public Result grade(MainVo vo)
    {
        try
        {
            return Result.success(appsUserRechargeService.grade(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

//    @ApiOperation("vip开通、充值、续费")
//    @PostMapping("/gradeOrder")
//    @HttpApiMethod(urlMapping = "app.recharge.grade_order")
//    public Result gradeOrder(GradeOrderVo vo) {
//        try {
//            return Result.success(rechargeService.gradeOrder(vo));
//        } catch (LaiKeAPIException e) {
//            return Result.error(e.getCode(), e.getMessage());
//        }
//    }

    @ApiOperation("会员升价渲染接口")
    @PostMapping("/upgrade")
    @HttpApiMethod(urlMapping = "app.recharge.upgrade")
    public Result upgrade(MainVo vo)
    {
        try
        {
            return Result.success(appsUserRechargeService.upgrade(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("会员等级中心接口")
    @PostMapping("/grade_center")
    @HttpApiMethod(urlMapping = "app.recharge.grade_center")
    public Result gradeCenter(MainVo vo)
    {
        try
        {
            return Result.success(appsUserRechargeService.gradeCenter(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("加载更多商品")
    @PostMapping("/getMore")
    @HttpApiMethod(urlMapping = "app.recharge.get_more")
    public Result getMore(MainVo vo)
    {
        try
        {
            return Result.success(appsUserRechargeService.getMore(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("关闭自动续费提示")
    @PostMapping("/close")
    @HttpApiMethod(urlMapping = "app.recharge.close")
    public Result closeAutomaticRenewal(MainVo vo)
    {
        try
        {
            return Result.success(appsUserRechargeService.closeAutomaticRenewal(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("会员制分享")
    @PostMapping("/share")
    @HttpApiMethod(urlMapping = "app.recharge.share")
    public Result share(MainVo vo, Integer type, HttpServletRequest request)
    {
        try
        {
            return Result.success(appsUserRechargeService.share(vo, type, DataUtils.getServcerUrl(request)));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("制作商品分享带参数二维码")
    @PostMapping("/product_share")
    @HttpApiMethod(urlMapping = {"app.getcode.index", "app.getcode.product_share"})
    public Result productShare(ProductShareVo vo, HttpServletRequest request)
    {
        try
        {
            return Result.success(appsUserRechargeService.productShare(vo, DataUtils.getServcerUrl(request)));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }
}
