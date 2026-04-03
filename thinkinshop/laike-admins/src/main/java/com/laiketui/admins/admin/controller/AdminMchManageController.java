package com.laiketui.admins.admin.controller;

import com.laiketui.admins.api.admin.plugin.AdminMchManageService;
import com.laiketui.core.domain.Result;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.domain.user.WithdrawModel;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.main.AddStoreConfigVo;
import com.laiketui.domain.vo.mch.AddIMchVo;
import com.laiketui.domain.vo.mch.AddMchVo;
import com.laiketui.domain.vo.user.WithdrawalVo;
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
import java.util.Map;

/**
 * 店铺管理
 *
 * @author Trick
 * @date 2021/1/26 11:30
 */
@Api(tags = "后台-店铺管理")
@RestController
@RequestMapping("/admin/mch")
public class AdminMchManageController
{

    @Autowired
    private AdminMchManageService adminMchManageService;

    @ApiOperation("获取店铺信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "店铺id", dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "isOpen", value = "是否营业：0.未营业 1.营业中 2.打烊", dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "promiseStatus", value = "保证金状态 1已交 0未交", dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "name", value = "店铺名称/用户Id", dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "cid", value = "店铺分类id", dataType = "int", paramType = "form")
    })
    @PostMapping("/getMchInfo")
    @HttpApiMethod(apiKey = "mch.Admin.Mch.GetMchInfo")
    public Result getMchInfo(MainVo vo, Integer id, Integer isOpen, String name, Integer promiseStatus, Integer cid)
    {
        try
        {
            return Result.success(adminMchManageService.getMchInfo(vo, id, isOpen, name, promiseStatus, cid));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("新增店铺信息")
    @PostMapping("/addMchInfo")
    @HttpApiMethod(apiKey = "mch.Admin.Mch.AddMchInfo")
    public Result addMchInfo(AddIMchVo vo)
    {
        try
        {
            return Result.success(adminMchManageService.addMchInfo(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("新增店铺信息,获取未创建店铺用户信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "queryUser", value = "查询所用的:用户id/用户名称/用户电话号码", dataType = "String", paramType = "form")
    })
    @PostMapping("/addMchGetUserInfo")
    @HttpApiMethod(apiKey = "admin.mch.addMchGetUserInfo")
    public Result addMchGetUserInfo(MainVo vo, String queryUser)
    {
        try
        {
            return Result.success(adminMchManageService.addMchGetUserInfo(vo, queryUser));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("修改商户信息")
    @PostMapping("/modifyMchInfo")
    @HttpApiMethod(apiKey = "mch.Admin.Mch.ModifyMchInfo")
    public Result modifyMchInfo(AddMchVo vo)
    {
        try
        {
            return Result.success(adminMchManageService.modifyMchInfo(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("删除店铺")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "mchId", value = "商户id", required = true, dataType = "int", paramType = "form")
    })
    @PostMapping("/delMchInfo")
    @HttpApiMethod(apiKey = "mch.Admin.Mch.DelMchInfo")
    public Result delMchInfo(MainVo vo, int mchId)
    {
        try
        {
            return Result.success(adminMchManageService.delMchInfo(vo, mchId));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("获取审核信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "id", dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "reviewStatus", value = "审核状态：0.待审核 1.审核通过 2.审核不通过", dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "name", value = "商户名称/用户id", dataType = "string", paramType = "form")
    })
    @PostMapping("/getMchExamineInfo")
    @HttpApiMethod(apiKey = "mch.Admin.Mch.GetMchExamineInfo")
    public Result getMchExamineInfo(MainVo vo, Integer id, Integer reviewStatus, String name, HttpServletResponse response)
    {
        try
        {
            Map<String, Object> ret = adminMchManageService.getMchExamineInfo(vo, id, reviewStatus, name, response);
            return ret == null ? Result.exportFile() : Result.success(ret);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("审核通过/拒绝")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "mchId", value = "商户id", dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "reviewStatus", value = "审核状态： 1.审核通过 2.审核不通过", dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "text", value = "拒绝原因", dataType = "string", paramType = "form"),
    })
    @PostMapping("/examineMch")
    @HttpApiMethod(apiKey = "mch.Admin.Mch.ExamineMch")
    public Result examineMch(MainVo vo, Integer mchId, Integer reviewStatus, String text)
    {
        try
        {
            return Result.success(adminMchManageService.examineMch(vo, mchId, reviewStatus, text));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("添加/修改店铺分类")
    @PostMapping("/addMchClass")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "店铺分类id", dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "name", value = "店铺分类名称", dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "img", value = "图片", dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "sort", value = "排序值", dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "isDisplay", value = "是否显示 0.否 1.是", dataType = "int", paramType = "form"),
    })
    @HttpApiMethod(apiKey = "mch.Admin.Mch.AddMchClass")
    public Result addMchClass(MainVo vo, Integer id, String name, String img, Integer sort, Integer isDisplay)
    {
        try
        {
            adminMchManageService.addMchClass(vo, id, name, img, sort, isDisplay);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("查询店铺分类列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "店铺分类id", dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "name", value = "店铺分类名称", dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "isDisPlay", value = "是否显示 0.否 1.是", dataType = "int", paramType = "form"),
    })
    @PostMapping("/mchClassList")
    @HttpApiMethod(apiKey = "mch.Admin.Mch.MchClassList")
    public Result mchClassList(MainVo vo, Integer id, String name, Integer isDisPlay)
    {
        try
        {
            return Result.success(adminMchManageService.mchClassList(vo, id, name, isDisPlay));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("是否显示")
    @PostMapping("/isDisplay")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "店铺分类id", dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "isDisplay", value = "是否显示 0.否 1.是", dataType = "string", paramType = "form"),
    })
    @HttpApiMethod(apiKey = "mch.Admin.Mch.IsDisplay")
    public Result isDisplay(MainVo vo, Integer id, Integer isDisplay)
    {
        try
        {
            adminMchManageService.isDisplay(vo, id, isDisplay);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("删除店铺分类")
    @PostMapping("/delMchClass")
    @HttpApiMethod(apiKey = "mch.Admin.Mch.DelMchClass")
    public Result delMchClass(MainVo vo, Integer id)
    {
        try
        {
            adminMchManageService.delMchClass(vo, id);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("获取商品审核列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "mchName", value = "商户名称", dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "goodsName", value = "商品名称", dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "goodsId", value = "商品id", dataType = "int", paramType = "form"),
    })
    @PostMapping("/getGoodsExamineInfo")
    @HttpApiMethod(apiKey = "mch.Admin.Mch.GetGoodsExamineInfo")
    public Result getGoodsExamineInfo(MainVo vo, String mchName, String goodsName, Integer goodsId)
    {
        try
        {
            return Result.success(adminMchManageService.getGoodsExamineInfo(vo, mchName, goodsName, goodsId));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("获取商品详情信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "goodsId", value = "商品id", required = true, dataType = "int", paramType = "form")
    })
    @PostMapping("/getGoodsDetailInfo")
    @HttpApiMethod(apiKey = "mch.Admin.Mch.GetGoodsDetailInfo")
    public Result getGoodsDetailInfo(MainVo vo, int goodsId)
    {
        try
        {
            return Result.success(adminMchManageService.getGoodsDetailInfo(vo, goodsId));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("商品审核")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "goodsId", value = "商品id", required = true, dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "status", value = "审核状态 0=拒绝 1=通过", required = true, dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "text", value = "拒绝理由", dataType = "string", paramType = "form")
    })
    @PostMapping("/goodsExamine")
    @HttpApiMethod(apiKey = "mch.Admin.Mch.GoodsExamine")
    public Result goodsExamine(MainVo vo, int goodsId, int status, String text)
    {
        try
        {
            return Result.success(adminMchManageService.goodsExamine(vo, goodsId, status, text));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("获取店铺提现审核列表")
    @PostMapping("/getWithdrawalExamineInfo")
    @HttpApiMethod(apiKey = "mch.Admin.Mch.GetWithdrawalExamineInfo")
    public Result getWithdrawalExamineInfo(WithdrawalVo vo, HttpServletResponse response)
    {
        try
        {
            Map<String, Object> result = adminMchManageService.getWithdrawalInfo(vo, WithdrawModel.EXAME_WAIT_STATUS, response);
            return result == null ? Result.exportFile() : Result.success(result);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("店铺提现审核")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "提现id", required = true, dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "stauts", required = true, value = "状态 1：审核通过 2：拒绝", dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "text", value = "拒绝原因", dataType = "string", paramType = "form"),
    })
    @PostMapping("/withdrawalExamine")
    @HttpApiMethod(apiKey = "mch.Admin.Mch.WithdrawalExamine")
    public Result withdrawalExamine(MainVo vo, int id, int stauts, String text)
    {
        try
        {
            return Result.success(adminMchManageService.withdrawalExamineMch(vo, id, stauts, text));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("获取提现记录列表")
    @PostMapping("/getWithdrawalInfo")
    @HttpApiMethod(apiKey = "mch.Admin.Mch.GetWithdrawalInfo")
    public Result getWithdrawalInfo(WithdrawalVo vo, HttpServletResponse response)
    {
        try
        {
            Map<String, Object> ret = adminMchManageService.getWithdrawalInfo(vo, null, response);
            return ret == null ? Result.exportFile() : Result.success(ret);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("获取商城配置信息")
    @PostMapping("/getStoreConfigInfo")
    @HttpApiMethod(apiKey = "mch.Admin.Mch.GetStoreConfigInfo")
    public Result getStoreConfigInfo(MainVo vo)
    {
        try
        {
            return Result.success(adminMchManageService.getStoreConfigInfo(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("添加/修改 商城配置")
    @PostMapping("/setStoreConfigInfo")
    @HttpApiMethod(apiKey = "mch.Admin.Mch.SetStoreConfigInfo")
    public Result setStoreConfigInfo(AddStoreConfigVo vo)
    {
        try
        {
            return Result.success(adminMchManageService.setStoreConfigInfo(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("上传店铺位置")
    @PostMapping("/location")
    @HttpApiMethod(apiKey = "admin.mch.location")
    public Result location(MainVo vo)
    {
        try
        {
            adminMchManageService.location(vo);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

}
