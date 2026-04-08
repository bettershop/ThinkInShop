package com.laiketui.apps.app.controller.distribution;

import com.laiketui.apps.api.app.services.AppsCstrAppDistributionService;
import com.laiketui.core.annotation.ParamsMapping;
import com.laiketui.core.domain.Result;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.Withdrawals1Vo;
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
 * 分销
 *
 * @author Trick
 * @date 2021/2/9 9:26
 */
@Api(tags = "我的-分销中心")
@RestController
@RequestMapping("/app/distribution")
public class AppsCstrAppDistributionController
{

    @Autowired
    private AppsCstrAppDistributionService appDistributionService;

    @ApiOperation("获取我的分销信息")
    @PostMapping("/index")
    @HttpApiMethod(urlMapping = "plugin.distribution.AppDistribution.index")
    public Result index(MainVo vo)
    {
        try
        {
            return Result.success(appDistributionService.index(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("获取我的团队")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userId", value = "用户id", dataType = "string", paramType = "form")
    })
    @PostMapping("/mygroup")
    @HttpApiMethod(urlMapping = "plugin.distribution.AppDistribution.myGroup")
    public Result mygroup(MainVo vo, @ParamsMapping("uid") String userId)
    {
        try
        {
            return Result.success(appDistributionService.mygroup(vo, userId));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("获取佣金明细")
    @PostMapping("/commList")
    @HttpApiMethod(urlMapping = "plugin.distribution.AppDistribution.commList")
    public Result commList(MainVo vo)
    {
        try
        {
            return Result.success(appDistributionService.commList(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("获取提现明细列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "status", value = "状态 0：审核中 1：审核通过 2：拒绝", required = true, dataType = "int", paramType = "form")
    })
    @PostMapping("/cashList")
    @HttpApiMethod(urlMapping = "plugin.distribution.AppDistribution.cashList")
    public Result cashList(MainVo vo, @ParamsMapping("status") Integer status)
    {
        try
        {
            return Result.success(appDistributionService.cashList(vo, status, null));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("获取提现详情")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "提现id", required = true, dataType = "int", paramType = "form")
    })
    @PostMapping("/cashDetail")
    @HttpApiMethod(urlMapping = "plugin.distribution.AppDistribution.cashDetail")
    public Result cashDetail(MainVo vo, Integer id)
    {
        try
        {
            return Result.success(appDistributionService.cashDetail(vo, id));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("获取提现页面数据")
    @PostMapping("/intoWithdrawals")
    @HttpApiMethod(urlMapping = "plugin.distribution.AppDistribution.intoWithdrawals")
    public Result intoWithdrawals(MainVo vo)
    {
        try
        {
            return Result.success(appDistributionService.intoWithdrawals(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

//    @ApiOperation("提现申请")
//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "pcode", value = "验证码", required = true, dataType = "string", paramType = "form"),
//            @ApiImplicitParam(name = "amt", value = "提现金额", required = true, dataType = "string", paramType = "form"),
//            @ApiImplicitParam(name = "bankId", value = "银行卡id", required = true, dataType = "int", paramType = "form")
//    })
//    @PostMapping("/withdrawalsApply")
//    @HttpApiMethod(urlMapping = "plugin.distribution.AppDistribution.withdrawals")
//    public Result withdrawalsApply(MainVo vo, @ParamsMapping("code") String pcode, @ParamsMapping("amoney") String amt, @ParamsMapping("bank_id") int bankId)
//    {
//        try
//        {
//            return Result.success(appDistributionService.withdrawalsApply(vo, pcode, amt, bankId));
//        }
//        catch (LaiKeAPIException e)
//        {
//            return Result.error(e.getCode(), e.getMessage());
//        }
//    }

    @ApiOperation("提现申请")
    @PostMapping("/withdrawalsApply")
    @HttpApiMethod(urlMapping = "plugin.distribution.AppDistribution.withdrawals")
    public Result withdrawalsApply(Withdrawals1Vo vo)
    {
        try
        {
            return Result.success(appDistributionService.withdrawalsApply(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("获取佣金排行榜")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "id", dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "type", value = "排行类型 1今日2本周3本月4本年", required = true, dataType = "int", paramType = "form")
    })
    @PostMapping("/getRanking")
    @HttpApiMethod(urlMapping = "plugin.distribution.AppDistribution.getRanking")
    public Result getRanking(MainVo vo, Integer id, int type)
    {
        try
        {
            return Result.success(appDistributionService.getRanking(vo, id, type));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("推广订单")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "search", value = "用户名称/商品名称", dataType = "string", paramType = "form")
    })
    @PostMapping("/promoteOrder")
    @HttpApiMethod(urlMapping = "plugin.distribution.AppDistribution.promoteOrder")
    public Result promoteOrder(MainVo vo, String search, String userId)
    {
        try
        {
            return Result.success(appDistributionService.promoteOrder(vo, search, userId));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("获取分销礼包商品")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "productTitle", value = "商品名称", dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "distributorId", value = "分销等级id", dataType = "int", paramType = "form"),
    })
    @PostMapping("/getStart")
    @HttpApiMethod(urlMapping = "plugin.distribution.AppDistribution.getStart")
    public Result getStart(MainVo vo, @ParamsMapping("product_title") String productTitle, @ParamsMapping("distributor_id") Integer distributorId)
    {
        try
        {
            return Result.success(appDistributionService.getStart(vo, productTitle, distributorId));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("获取商品列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "productTitle", value = "商品名称", dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "sortKey", value = "排序字段", dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "queue", value = "1降序2升序", dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "cid", value = "商品类别id", dataType = "int", paramType = "form")
    })
    @PostMapping("/getGoodsList")
    @HttpApiMethod(urlMapping = "plugin.distribution.AppDistribution.getGoodsInfo")
    public Result getGoodsList(MainVo vo, @ParamsMapping("product_title") String productTitle, @ParamsMapping("sort_key") String sortKey, int queue, Integer cid)
    {
        try
        {
            return Result.success(appDistributionService.getGoodsList(vo, productTitle, sortKey, queue, cid));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("获取分销商品所有类别")
    @PostMapping("/getclass")
    @HttpApiMethod(urlMapping = "plugin.distribution.AppDistribution.getClass")
    public Result getclass(MainVo vo)
    {
        try
        {
            return Result.success(appDistributionService.getClass(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

}
