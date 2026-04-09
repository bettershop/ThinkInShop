package com.laiketui.admins.mch.plugin;

import com.laiketui.admins.api.mch.plugin.MchDiyTemplateService;
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
@RequestMapping("/pc/diy")
public class MchDiyTemplateController
{

    @Autowired
    private MchDiyTemplateService diytemplateService;

    @ApiOperation("diy后台首页")
    @PostMapping("/index")
    @HttpApiMethod(apiKey = "pc.diy.index")
    public Result index(MainVo vo) throws LaiKeAPIException
    {
        try
        {
            return Result.success(diytemplateService.index(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("获取轮播图列表")
    @PostMapping("/bannerIndex")
    @HttpApiMethod(apiKey = "pc.diy.bannerIndex")
    public Result bannerIndex(MainVo vo) throws LaiKeAPIException
    {
        try
        {
            return Result.success(diytemplateService.bannerIndex(vo));
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
    @HttpApiMethod(apiKey = "pc.diy.bannerPathList")
    public Result bannerPathList(MainVo vo, Integer type) throws LaiKeAPIException
    {
        try
        {
            return Result.success(diytemplateService.bannerPathList(vo, type));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("保存轮播图")
    @PostMapping("/bannerSave")
    @HttpApiMethod(apiKey = "pc.diy.bannerSave")
    public Result bannerSave(BannerSaveVo vo) throws LaiKeAPIException
    {
        try
        {
            diytemplateService.bannerSave(vo);
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
    @HttpApiMethod(apiKey = "pc.diy.bannerMoveTop")
    public Result bannerMoveTop(MainVo vo, int id) throws LaiKeAPIException
    {
        try
        {
            diytemplateService.bannerMoveTop(vo, id);
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
    @HttpApiMethod(apiKey = "pc.diy.bannerRemove")
    public Result bannerRemove(MainVo vo, int id, int id1) throws LaiKeAPIException
    {
        try
        {
            diytemplateService.bannerRemove(vo, id, id1);
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
    @HttpApiMethod(apiKey = "pc.diy.bannerDel")
    public Result bannerDel(MainVo vo, int id) throws LaiKeAPIException
    {
        try
        {
            diytemplateService.bannerDel(vo, id);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("ui导航栏列表")
    @PostMapping("/uiIndex")
    @HttpApiMethod(apiKey = "pc.diy.uiIndex")
    public Result uiIndex(MainVo vo) throws LaiKeAPIException
    {
        try
        {
            return Result.success(diytemplateService.uiIndex(vo));
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
    @HttpApiMethod(apiKey = "pc.diy.uiIndexDetail")
    public Result uiIndexDetail(MainVo vo, int id) throws LaiKeAPIException
    {
        try
        {
            return Result.success(diytemplateService.uiIndexDetail(vo, id));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("添加/编辑 ui导航栏")
    @PostMapping("/uiSave")
    @HttpApiMethod(apiKey = "pc.diy.uiSave")
    public Result uiSave(SaveDiyUiVo vo) throws LaiKeAPIException
    {
        try
        {
            diytemplateService.uiSave(vo);
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
    @HttpApiMethod(apiKey = "pc.diy.uiTop")
    public Result uiTop(MainVo vo, int id) throws LaiKeAPIException
    {
        try
        {
            diytemplateService.uiTop(vo, id);
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
    @HttpApiMethod(apiKey = "pc.diy.uiMove")
    public Result uiMove(MainVo vo, int id, int id1) throws LaiKeAPIException
    {
        try
        {
            diytemplateService.uiMove(vo, id, id1);
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
    @HttpApiMethod(apiKey = "pc.diy.uiDel")
    public Result uiDel(MainVo vo, int id) throws LaiKeAPIException
    {
        try
        {
            diytemplateService.uiDel(vo, id);
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
    @HttpApiMethod(apiKey = "pc.diy.uiIsShowSwitch")
    public Result uiIsShowSwitch(MainVo vo, int id) throws LaiKeAPIException
    {
        try
        {
            diytemplateService.uiIsShowSwitch(vo, id);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("分类管理列表")
    @PostMapping("/classIndex")
    @HttpApiMethod(apiKey = "pc.diy.classIndex")
    public Result classIndex(MainVo vo) throws LaiKeAPIException
    {
        try
        {
            return Result.success(diytemplateService.classIndex(vo));
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
            diytemplateService.reSort(vo);
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
    @HttpApiMethod(apiKey = "pc.diy.classTop")
    public Result classIndex(MainVo vo, int id) throws LaiKeAPIException
    {
        try
        {
            diytemplateService.classTop(vo, id);
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
    @HttpApiMethod(apiKey = "pc.diy.classMove")
    public Result classMove(MainVo vo, int id, int id1) throws LaiKeAPIException
    {
        try
        {
            diytemplateService.classMove(vo, id, id1);
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
    @HttpApiMethod(apiKey = "pc.diy.classSwitch")
    public Result classSwitch(MainVo vo, int id) throws LaiKeAPIException
    {
        try
        {
            diytemplateService.classSwitch(vo, id);
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
    @HttpApiMethod(apiKey = "pc.diy.activityList")
    public Result activityList(MainVo vo, Integer id) throws LaiKeAPIException
    {
        try
        {
            return Result.success(diytemplateService.activityList(vo, id));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("获取插件类型")
    @PostMapping("/getPluginTypeList")
    @HttpApiMethod(apiKey = "pc.diy.getPluginTypeList")
    public Result getPluginTypeList(MainVo vo) throws LaiKeAPIException
    {
        try
        {
            return Result.success(diytemplateService.getPluginTypeList(vo));
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
    @HttpApiMethod(apiKey = "pc.diy.getGoodsList")
    public Result getGoodsList(MainVo vo, String goodsName, Integer classId, Integer brandId) throws LaiKeAPIException
    {
        try
        {
            return Result.success(diytemplateService.getGoodsList(vo, goodsName, classId, brandId));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("保存、编辑营销活动")
    @PostMapping("/activitySave")
    @HttpApiMethod(apiKey = "pc.diy.activitySave")
    public Result activitySave(AddDiyActivityVo vo) throws LaiKeAPIException
    {
        try
        {
            diytemplateService.activitySave(vo);
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
    @HttpApiMethod(apiKey = "pc.diy.activityMove")
    public Result activityMove(MainVo vo, int moveId, int moveId2) throws LaiKeAPIException
    {
        try
        {
            diytemplateService.activityMove(vo, moveId, moveId2);
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
    @HttpApiMethod(apiKey = "pc.diy.activitySwitch")
    public Result activitySwitch(MainVo vo, int id) throws LaiKeAPIException
    {
        try
        {
            diytemplateService.activitySwitch(vo, id);
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
    @HttpApiMethod(apiKey = "pc.diy.activityDel")
    public Result activityDel(MainVo vo, int id) throws LaiKeAPIException
    {
        try
        {
            diytemplateService.activityDel(vo, id);
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
    @HttpApiMethod(apiKey = "pc.diy.getActGoodsList")
    public Result getActGoodsList(MainVo vo, int id) throws LaiKeAPIException
    {
        try
        {
            return Result.success(diytemplateService.getActGoodsList(vo, id));
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
    @HttpApiMethod(apiKey = "pc.diy.actGoodsMove")
    public Result actGoodsMove(MainVo vo, int id, int goodsId, int goodsId1) throws LaiKeAPIException
    {
        try
        {
            diytemplateService.actGoodsMove(vo, id, goodsId, goodsId1);
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
    @HttpApiMethod(apiKey = "pc.diy.actGoodsTop")
    public Result actGoodsTop(MainVo vo, int id, int goodsId) throws LaiKeAPIException
    {
        try
        {
            diytemplateService.actGoodsMove(vo, id, goodsId, 0);
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
    @HttpApiMethod(apiKey = "pc.diy.actGoodsSwitch")
    public Result actGoodsSwitch(MainVo vo, int id) throws LaiKeAPIException
    {
        try
        {
            diytemplateService.actGoodsSwitch(vo, id);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("是否开启diy插件")
    @PostMapping("/isDiyPlugin")
    @HttpApiMethod(apiKey = "pc.diy.isDiyPlugin")
    public Result isDiyPlugin(MainVo vo) throws LaiKeAPIException
    {
        try
        {
            return Result.success(diytemplateService.isDiyPlugin(vo));
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
    @HttpApiMethod(apiKey = "pc.diy.getTemplateDiyList")
    public Result addTemplate(MainVo vo, Integer id) throws LaiKeAPIException
    {
        try
        {
            return Result.success(diytemplateService.getTemplateDiyList(vo, id));
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
    @HttpApiMethod(apiKey = "pc.diy.addTemplate")
    public Result addTemplate(MainVo vo, String jsonValue, String title, String cover, Integer id) throws LaiKeAPIException
    {
        try
        {
            diytemplateService.addTemplate(vo, jsonValue, title, cover, id);
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
    @HttpApiMethod(apiKey = "pc.diy.setDiy")
    public Result setDiy(MainVo vo, Integer id) throws LaiKeAPIException
    {
        try
        {
            diytemplateService.setDiy(vo, id);
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
    @HttpApiMethod(apiKey = "pc.diy.delDiy")
    public Result delDiy(MainVo vo, Integer id) throws LaiKeAPIException
    {
        try
        {
            diytemplateService.delDiy(vo, id);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }
}
