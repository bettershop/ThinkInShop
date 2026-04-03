package com.laiketui.admins.admin.controller.plugin;

import com.laiketui.admins.api.admin.plugin.AdminDiyTemplateService;
import com.laiketui.core.domain.Result;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.admin.diy.AddDiyActivityVo;
import com.laiketui.domain.vo.admin.diy.SaveDiyUiVo;
import com.laiketui.domain.vo.plugin.BannerSaveVo;
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
 * diy模板管理
 *
 * @author Trick
 * @date 2021/6/30 9:38
 */
@Api(tags = "diy模板管理")
@RestController
@RequestMapping("/admin/diy")
public class AdminDiyTemplateController
{

    @Autowired
    private AdminDiyTemplateService adminDiyTemplateService;

    @ApiOperation("diy后台首页")
    @PostMapping("/index")
    @HttpApiMethod(apiKey = "plugin.template.AdminDiy.Index")
    public Result index(MainVo vo) throws LaiKeAPIException
    {
        try
        {
            return Result.success(adminDiyTemplateService.index(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("获取轮播图列表")
    @PostMapping("/bannerIndex")
    @HttpApiMethod(apiKey = "plugin.template.AdminDiy.BannerIndex")
    public Result bannerIndex(MainVo vo) throws LaiKeAPIException
    {
        try
        {
            return Result.success(adminDiyTemplateService.bannerIndex(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("轮播图路径分类")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "type", value = "跳转类型 1.分类 2.商品 3.店铺 默认自定义", dataType = "int", paramType = "form")
    })
    @PostMapping("/bannerPathList")
    @HttpApiMethod(apiKey = "plugin.template.AdminDiy.BannerPathList")
    public Result bannerPathList(MainVo vo, Integer type) throws LaiKeAPIException
    {
        try
        {
            return Result.success(adminDiyTemplateService.bannerPathList(vo, type));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("保存轮播图")
    @PostMapping("/bannerSave")
    @HttpApiMethod(apiKey = "plugin.template.AdminDiy.BannerSave")
    public Result bannerSave(BannerSaveVo vo) throws LaiKeAPIException
    {
        try
        {
            adminDiyTemplateService.bannerSave(vo);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("轮播图置顶")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "id", required = true, dataType = "int", paramType = "form")
    })
    @PostMapping("/bannerMoveTop")
    @HttpApiMethod(apiKey = "plugin.template.AdminDiy.BannerMoveTop")
    public Result bannerMoveTop(MainVo vo, int id) throws LaiKeAPIException
    {
        try
        {
            adminDiyTemplateService.bannerMoveTop(vo, id);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("轮播图上移下移")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "id", required = true, dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "id1", value = "id", required = true, dataType = "int", paramType = "form"),
    })
    @PostMapping("/bannerRemove")
    @HttpApiMethod(apiKey = "plugin.template.AdminDiy.BannerRemove")
    public Result bannerRemove(MainVo vo, int id, int id1) throws LaiKeAPIException
    {
        try
        {
            adminDiyTemplateService.bannerRemove(vo, id, id1);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("轮播图删除")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "id", required = true, dataType = "int", paramType = "form")
    })
    @PostMapping("/bannerDel")
    @HttpApiMethod(apiKey = "plugin.template.AdminDiy.BannerDel")
    public Result bannerDel(MainVo vo, int id) throws LaiKeAPIException
    {
        try
        {
            adminDiyTemplateService.bannerDel(vo, id);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("ui导航栏列表")
    @PostMapping("/uiIndex")
    @HttpApiMethod(apiKey = "plugin.template.AdminDiy.UiIndex")
    public Result uiIndex(MainVo vo) throws LaiKeAPIException
    {
        try
        {
            return Result.success(adminDiyTemplateService.uiIndex(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("ui导航栏列表明细")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "id", required = true, dataType = "int", paramType = "form")
    })
    @PostMapping("/uiIndexDetail")
    @HttpApiMethod(apiKey = "admin.diy.uiIndexDetail")
    public Result uiIndexDetail(MainVo vo, int id) throws LaiKeAPIException
    {
        try
        {
            return Result.success(adminDiyTemplateService.uiIndexDetail(vo, id));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("添加/编辑 ui导航栏")
    @PostMapping("/uiSave")
    @HttpApiMethod(apiKey = "plugin.template.AdminDiy.UiSave")
    public Result uiSave(SaveDiyUiVo vo) throws LaiKeAPIException
    {
        try
        {
            adminDiyTemplateService.uiSave(vo);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("ui导航栏置顶")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "id", required = true, dataType = "int", paramType = "form")
    })
    @PostMapping("/uiTop")
    @HttpApiMethod(apiKey = "plugin.template.AdminDiy.UiTop")
    public Result uiTop(MainVo vo, int id) throws LaiKeAPIException
    {
        try
        {
            adminDiyTemplateService.uiTop(vo, id);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("ui导航栏 上下移动")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "id", required = true, dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "id1", value = "id1", required = true, dataType = "int", paramType = "form"),
    })
    @PostMapping("/uiMove")
    @HttpApiMethod(apiKey = "plugin.template.AdminDiy.UiMove")
    public Result uiMove(MainVo vo, int id, int id1) throws LaiKeAPIException
    {
        try
        {
            adminDiyTemplateService.uiMove(vo, id, id1);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("删除 ui导航栏")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "id", required = true, dataType = "int", paramType = "form")
    })
    @PostMapping("/uiDel")
    @HttpApiMethod(apiKey = "plugin.template.AdminDiy.UiDel")
    public Result uiDel(MainVo vo, int id) throws LaiKeAPIException
    {
        try
        {
            adminDiyTemplateService.uiDel(vo, id);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("ui导航栏是否显示开关")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "id", required = true, dataType = "int", paramType = "form")
    })
    @PostMapping("/uiIsShowSwitch")
    @HttpApiMethod(apiKey = "plugin.template.AdminDiy.UiIsShowSwitch")
    public Result uiIsShowSwitch(MainVo vo, int id) throws LaiKeAPIException
    {
        try
        {
            adminDiyTemplateService.uiIsShowSwitch(vo, id);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("ui导航栏是否需要登录开关")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "id", required = true, dataType = "int", paramType = "form")
    })
    @PostMapping("/uiIsLoginSwitch")
    @HttpApiMethod(apiKey = "plugin.template.AdminDiy.UiIsLoginSwitch")
    public Result uiIsLoginSwitch(MainVo vo, int id) throws LaiKeAPIException
    {
        try
        {
            adminDiyTemplateService.uiIsLoginSwitch(vo, id);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("分类管理列表")
    @PostMapping("/classIndex")
    @HttpApiMethod(apiKey = "plugin.template.AdminDiy.ClassIndex")
    public Result classIndex(MainVo vo) throws LaiKeAPIException
    {
        try
        {
            return Result.success(adminDiyTemplateService.classIndex(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation(value = "分类序号重新排列")
    @PostMapping("/reSort")
    public Result reSort(MainVo vo) throws LaiKeAPIException
    {
        try
        {
            adminDiyTemplateService.reSort(vo);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("类别置顶")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "id", required = true, dataType = "int", paramType = "form")
    })
    @PostMapping("/classTop")
    @HttpApiMethod(apiKey = "plugin.template.AdminDiy.ClassTop")
    public Result classIndex(MainVo vo, int id) throws LaiKeAPIException
    {
        try
        {
            adminDiyTemplateService.classTop(vo, id);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("类别上下移动")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "id", required = true, dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "id1", value = "id1", required = true, dataType = "int", paramType = "form")
    })
    @PostMapping("/classMove")
    @HttpApiMethod(apiKey = "plugin.template.AdminDiy.ClassMove")
    public Result classMove(MainVo vo, int id, int id1) throws LaiKeAPIException
    {
        try
        {
            adminDiyTemplateService.classMove(vo, id, id1);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("类别是否显示开关")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "id", required = true, dataType = "int", paramType = "form")
    })
    @PostMapping("/classSwitch")
    @HttpApiMethod(apiKey = "plugin.template.AdminDiy.ClassSwitch")
    public Result classSwitch(MainVo vo, int id) throws LaiKeAPIException
    {
        try
        {
            adminDiyTemplateService.classSwitch(vo, id);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("活动管理列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "id", required = true, dataType = "int", paramType = "form")
    })
    @PostMapping("/activityList")
    @HttpApiMethod(apiKey = "plugin.template.AdminDiy.ActivityList")
    public Result activityList(MainVo vo, Integer id) throws LaiKeAPIException
    {
        try
        {
            return Result.success(adminDiyTemplateService.activityList(vo, id));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("获取插件类型")
    @PostMapping("/getPluginTypeList")
    @HttpApiMethod(apiKey = "plugin.template.AdminDiy.GetPluginTypeList")
    public Result getPluginTypeList(MainVo vo) throws LaiKeAPIException
    {
        try
        {
            return Result.success(adminDiyTemplateService.getPluginTypeList(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("获取商品列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "goodsName", value = "商品名称", dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "classId", value = "类别id", dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "brandId", value = "品牌id", dataType = "int", paramType = "form")
    })
    @PostMapping("/getGoodsList")
    @HttpApiMethod(apiKey = "plugin.template.AdminDiy.GetGoodsList")
    public Result getGoodsList(MainVo vo, String goodsName, Integer classId, Integer brandId) throws LaiKeAPIException
    {
        try
        {
            return Result.success(adminDiyTemplateService.getGoodsList(vo, goodsName, classId, brandId));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("保存、编辑营销活动")
    @PostMapping("/activitySave")
    @HttpApiMethod(apiKey = "plugin.template.AdminDiy.ActivitySave")
    public Result activitySave(AddDiyActivityVo vo) throws LaiKeAPIException
    {
        try
        {
            adminDiyTemplateService.activitySave(vo);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("活动管理上下移动")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "moveId", value = "活动id", dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "moveId2", value = "活动id2", dataType = "int", paramType = "form"),
    })
    @PostMapping("/activityMove")
    @HttpApiMethod(apiKey = "plugin.template.AdminDiy.ActivityMove")
    public Result activityMove(MainVo vo, int moveId, int moveId2) throws LaiKeAPIException
    {
        try
        {
            adminDiyTemplateService.activityMove(vo, moveId, moveId2);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("活动显示开关")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "id", required = true, dataType = "int", paramType = "form")
    })
    @PostMapping("/activitySwitch")
    @HttpApiMethod(apiKey = "plugin.template.AdminDiy.ActivitySwitch")
    public Result activitySwitch(MainVo vo, int id) throws LaiKeAPIException
    {
        try
        {
            adminDiyTemplateService.activitySwitch(vo, id);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("删除活动")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "id", required = true, dataType = "int", paramType = "form")
    })
    @PostMapping("/activityDel")
    @HttpApiMethod(apiKey = "plugin.template.AdminDiy.ActivityDel")
    public Result activityDel(MainVo vo, int id) throws LaiKeAPIException
    {
        try
        {
            adminDiyTemplateService.activityDel(vo, id);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("获取活动商品列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "活动id", required = true, dataType = "int", paramType = "form")
    })
    @PostMapping("/getActGoodsList")
    @HttpApiMethod(apiKey = "plugin.template.AdminDiy.GetActGoodsList")
    public Result getActGoodsList(MainVo vo, int id) throws LaiKeAPIException
    {
        try
        {
            return Result.success(adminDiyTemplateService.getActGoodsList(vo, id));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("活动商品上下移动")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "活动id", required = true, dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "goodsId", value = "商品id", required = true, dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "goodsId1", value = "商品id1", required = true, dataType = "int", paramType = "form")
    })
    @PostMapping("/actGoodsMove")
    @HttpApiMethod(apiKey = "plugin.template.AdminDiy.ActGoodsMove")
    public Result actGoodsMove(MainVo vo, int id, int goodsId, int goodsId1) throws LaiKeAPIException
    {
        try
        {
            adminDiyTemplateService.actGoodsMove(vo, id, goodsId, goodsId1);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("活动商品置顶")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "活动id", required = true, dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "goodsId", value = "活动商品id", required = true, dataType = "int", paramType = "form"),
    })
    @PostMapping("/actGoodsTop")
    @HttpApiMethod(apiKey = "plugin.template.AdminDiy.ActGoodsTop")
    public Result actGoodsTop(MainVo vo, int id, int goodsId) throws LaiKeAPIException
    {
        try
        {
            adminDiyTemplateService.actGoodsMove(vo, id, goodsId, 0);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("活动商品是否显示开关")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "活动商品id", required = true, dataType = "int", paramType = "form"),
    })
    @PostMapping("/actGoodsSwitch")
    @HttpApiMethod(apiKey = "plugin.template.AdminDiy.ActGoodsSwitch")
    public Result actGoodsSwitch(MainVo vo, int id) throws LaiKeAPIException
    {
        try
        {
            adminDiyTemplateService.actGoodsSwitch(vo, id);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("是否开启diy插件")
    @PostMapping("/isDiyPlugin")
    @HttpApiMethod(apiKey = "admin.diy.isDiyPlugin")
    public Result isDiyPlugin(MainVo vo) throws LaiKeAPIException
    {
        try
        {
            return Result.success(adminDiyTemplateService.isDiyPlugin(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("获取diy模板首页列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "id", dataType = "int", paramType = "form")
    })
    @PostMapping("/getTemplateDiyList")
    @HttpApiMethod(apiKey = "admin.diy.getTemplateDiyList")
    public Result addTemplate(MainVo vo, Integer id) throws LaiKeAPIException
    {
        try
        {
            return Result.success(adminDiyTemplateService.getTemplateDiyList(vo, id));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("添加/编辑diy模板")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "id", dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "jsonValue", value = "json值", dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "title", value = "diy名称", dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "cover", value = "base64格式图片", dataType = "string", paramType = "form")
    })
    @PostMapping("/addTemplate")
    @HttpApiMethod(apiKey = "admin.diy.addTemplate")
    public Result addTemplate(MainVo vo, String jsonValue, String title, String cover, Integer id) throws LaiKeAPIException
    {
        try
        {
            adminDiyTemplateService.addTemplate(vo, jsonValue, title, cover, id);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("设置diy模板")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "id 传当前默认模板id表示默认主模板", dataType = "int", paramType = "form")
    })
    @PostMapping("/setDiy")
    @HttpApiMethod(apiKey = "admin.diy.setDiy")
    public Result setDiy(MainVo vo, Integer id) throws LaiKeAPIException
    {
        try
        {
            adminDiyTemplateService.setDiy(vo, id);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("删除diy")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "id", dataType = "int", paramType = "form")
    })
    @PostMapping("/delDiy")
    @HttpApiMethod(apiKey = "admin.diy.delDiy")
    public Result delDiy(MainVo vo, Integer id) throws LaiKeAPIException
    {
        try
        {
            adminDiyTemplateService.delDiy(vo, id);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }
}
