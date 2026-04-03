package com.laiketui.apps.mch.controller;

import com.laiketui.apps.api.mch.AppsMchBaseService;
import com.laiketui.common.api.PubliceService;
import com.laiketui.common.utils.tool.ImgUploadUtils;
import com.laiketui.core.annotation.ParamsMapping;
import com.laiketui.core.domain.Result;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.DictionaryConst;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.main.RefundVo;
import com.laiketui.domain.vo.mch.*;
import com.laiketui.domain.vo.order.OrderSettlementVo;
import com.laiketui.domain.vo.user.AddBankVo;
import com.laiketui.root.annotation.HttpApiMethod;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 店铺控制类
 *
 * @author Trick
 * @date 2020/9/25 14:10
 */
@Api(tags = "店铺接口")
@RestController
@RequestMapping("/app/mch/base")
public class AppsMchBaseControllor
{

    @Autowired
    AppsMchBaseService appsMchBaseService;

    @Autowired
    PubliceService publiceService;

    @ApiOperation("申请开店")
    @PostMapping(value = "/apply")
    @HttpApiMethod(urlMapping = "mch.App.Mch.Apply")
    public Result apply(ApplyShopVo vo)
    {
        try
        {
            appsMchBaseService.applyShop(vo);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("我的店铺")
    @PostMapping("/index")
    @HttpApiMethod(urlMapping = "mch.App.Mch.Index")
    public Result index(MainVo vo)
    {
        try
        {
            return Result.success(appsMchBaseService.index(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("获取店铺银行卡列表")
    @PostMapping("/bankList")
    @HttpApiMethod(urlMapping = "mch.App.MchBank.BankList")
    public Result bankList(MainVo vo)
    {
        try
        {
            return Result.success(appsMchBaseService.bankList(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("添加店铺银行卡")
    @PostMapping("/addBank")
    @HttpApiMethod(urlMapping = "mch.App.MchBank.AddBank")
    public Result addBank(AddBankVo vo)
    {
        try
        {
            appsMchBaseService.addBank(vo);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("编辑店铺银行卡")
    @PostMapping("/editBank")
    @HttpApiMethod(urlMapping = {"mch.App.MchBank.EditBank"})
    public Result updateBank(AddBankVo vo)
    {
        try
        {
            appsMchBaseService.updateBank(vo);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("根据id获取店铺银行卡信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "店铺id", required = true, dataType = "int", paramType = "form")
    })
    @PostMapping("/mchBankInfo")
    @HttpApiMethod(urlMapping = {"app.MchBank.mchBankInfo", "mch.App.MchBank.BankPage"})
    public Result mchBankInfo(MainVo vo, int id)
    {
        try
        {
            return Result.success(appsMchBaseService.mchBankInfo(vo, id));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("验证店铺名称")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "name", value = "店铺名称", required = true, dataType = "string", paramType = "form")
    })
    @PostMapping("/verify_store_name")
    @HttpApiMethod(urlMapping = "mch.App.Mch.Verify_store_name")
    public Result verifyStoreName(MainVo vo, String name)
    {
        try
        {
            return Result.success(appsMchBaseService.verifyStoreName(vo, name));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("入住协议")
    @PostMapping("/agreement")
    @HttpApiMethod(urlMapping = "mch.App.Mch.Agreement")
    public Result agreement(MainVo vo)
    {
        try
        {
            return Result.success(publiceService.getAgreement(vo.getStoreId(), DictionaryConst.AgreementType.AGREEMENTTYPE_MCH));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("继续开通店铺-获取信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "shopId", value = "商户id", required = true, dataType = "int", paramType = "form")
    })
    @PostMapping("/continue_apply")
    @HttpApiMethod(urlMapping = "mch.App.Mch.Continue_apply")
    public Result continueApply(MainVo vo, @NotNull @RequestParam("shop_id") int shopId)
    {
        try
        {
            return Result.success(appsMchBaseService.continueApply(vo, shopId));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("添加(自选)商品页面")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "shopId", value = "商户id", required = true, dataType = "int", paramType = "form")
    })
    @PostMapping("/add_goods_page")
    @HttpApiMethod(urlMapping = "mch.App.Mch.add_goods_page")
    public Result addGoodsPage(MainVo vo, @ParamsMapping("shop_id") Integer shopId)
    {
        try
        {
            return Result.success(appsMchBaseService.addGoodsPage(vo, shopId));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("添加(自选)商品页面-加载更多")
    @PostMapping("/add_goods_page_load")
    @HttpApiMethod(urlMapping = {"app.mch.add_goods_page_load", "app.mch.goods_query", "mch.App.Mch.add_goods_page_load", "mch.App.Mch.goods_query"})
    public Result addGoodsPageLoad(AddGoodsPageLoadVo vo)
    {
        try
        {
            return Result.success(appsMchBaseService.addGoodsPageLoad(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("添加自选商品")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "shopId", value = "商户id", required = true, dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "pro_id", value = "产品id集", required = true, dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "label_id", value = "产品id", required = false, dataType = "int", paramType = "form"),
    })
    @PostMapping("/add_goods")
    @HttpApiMethod(urlMapping = {"mch.App.Mch.add_goods"})
    public Result addGoods(MainVo vo, @ParamsMapping("shop_id") int shopId, @ParamsMapping("pro_id") String proId, @ParamsMapping("label_id") String labbelId,
                           @ParamsMapping("freight_id") Integer freightId)
    {
        try
        {
            appsMchBaseService.addGoods(vo, shopId, proId, labbelId, freightId);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("获取自选商品信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "shopId", value = "店铺id", required = true, dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "goodsId", value = "商品id", required = true, dataType = "int", paramType = "form")
    })
    @PostMapping("/getGoodsInfoById")
    @HttpApiMethod(urlMapping = {"app.mch.see"})
    public Result getGoodsInfoById(MainVo vo, @ParamsMapping("shop_id") int shopId, @ParamsMapping("p_id") int goodsId)
    {
        try
        {
            return Result.success(appsMchBaseService.getZxGoodsInfoById(vo, shopId, goodsId));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("上传商品页面渲染")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "shopId", value = "商户id", required = true, dataType = "int", paramType = "form")
    })
    @PostMapping("/upload_merchandise_page")
    @HttpApiMethod(urlMapping = "mch.App.Mch.Upload_merchandise_page")
    public Result uploadMerchandisePage(MainVo vo, @RequestParam("shop_id") int shopId)
    {
        try
        {
            Map<String, Object> result = appsMchBaseService.uploadMerchandisePage(vo, shopId);
            return Result.success(result);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("获取商品类别")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "class_str", value = "商品类型id", dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "brand_str", value = "品牌id", dataType = "int", paramType = "form"),
    })
    @PostMapping("/get_class")
    @HttpApiMethod(urlMapping = "mch.App.Mch.Get_class")
    public Result getClass(MainVo vo, @ParamsMapping("class_str") Integer classId, @ParamsMapping("brand_str") Integer brandId)
    {
        try
        {
            Map<String, Object> result = appsMchBaseService.getClass(vo, classId, brandId);
            return Result.success(result);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("选择类别")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "class_str", value = "商品类型id", dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "brand_str", value = "品牌id", dataType = "int", paramType = "form")
    })
    @PostMapping("/choice_class")
    @HttpApiMethod(urlMapping = "mch.App.Mch.Choice_class")
    public Result choiceClass(MainVo vo, @ParamsMapping({"cid", "get_class"}) Integer classId, @ParamsMapping("brand_str") Integer brandId)
    {
        try
        {
            Map<String, Object> result = appsMchBaseService.choiceClass(vo, classId, brandId);
            return Result.success(result);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("获取属性名")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "attribute_str", value = "属性id,多个用','", dataType = "string", paramType = "form")
    })
    @PostMapping("/get_attribute_name")
    @HttpApiMethod(urlMapping = "mch.App.Mch.Get_attribute_name")
    public Result getAttributeName(MainVo vo, @ParamsMapping("attribute_str") String attributes)
    {
        try
        {
            return Result.success(appsMchBaseService.getAttributeName(vo, attributes));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("获取属性值")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "attribute_str", value = "属性名称", required = true, dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "attr_arr", value = "属性一对多,json", dataType = "string", paramType = "form")
    })
    @PostMapping("/get_attribute_value")
    @HttpApiMethod(urlMapping = "mch.App.Mch.Get_attribute_value")
    public Result getAttributeValue(MainVo vo, @ParamsMapping("attribute_str") String attributes, @ParamsMapping("attr_arr") String attrIds)
    {
        try
        {
            return Result.success(appsMchBaseService.getAttributeValue(vo, attributes, attrIds));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("我的商品")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "type", value = "商户审核状态", required = true, dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "shopId", value = "商户id", required = true, dataType = "int", paramType = "form")
    })
    @PostMapping("/my_merchandise")
    @HttpApiMethod(urlMapping = {"mch.App.Mch.My_merchandise", "mch.App.Mch.My_merchandise_load"})
    public Result myMerchandise(MainVo vo, String type, @ParamsMapping("shop_id") Integer shopId, Integer status, Integer commodity_type)
    {
        try
        {
            return Result.success(appsMchBaseService.myMerchandise(vo, shopId, type, status, commodity_type));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("编辑商品(加载数据)")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "shopId", value = "商户id", required = true, dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "p_id", value = "产品id", dataType = "int", paramType = "form")
    })
    @PostMapping("/shopModify")
    @HttpApiMethod(urlMapping = "mch.App.Mch.Modify")
    public Result modify(MainVo vo, @ParamsMapping("shop_id") int shopId, @ParamsMapping("p_id") int pid)
    {
        try
        {
            return Result.success(appsMchBaseService.modify(vo, shopId, pid));
        }
        catch (LaiKeAPIException e)
        {
            e.printStackTrace();
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("上传商品(保存)")
    @PostMapping("/upload_merchandise")
    @HttpApiMethod(urlMapping = "mch.App.Mch.Upload_merchandise")
    public Result uploadMerchandise(UploadMerchandiseVo vo)
    {
        try
        {
            appsMchBaseService.saveGoods(vo);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("上传图片")
    @PostMapping("/uploadImgs")
    @HttpApiMethod(urlMapping = "mch.App.Mch.UploadImgs")
    public Result uploadImgs(MainVo vo, @ParamsMapping("shop_id") Integer mchId, MultipartFile image)
    {
        try
        {
            List<MultipartFile> files = new ArrayList<>();
            files.add(image);
            //上传图片
            List<String> imgUrls = publiceService.uploadImage(files, GloabConst.UploadConfigConst.IMG_UPLOAD_OSS, vo.getStoreType(), vo.getStoreId(), mchId);
            return Result.success(ImgUploadUtils.getUrlPure(imgUrls.get(0), true));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("修改商品库存页面数据")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "shopId", value = "商户id", required = true, dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "p_id", value = "产品id", dataType = "int", paramType = "form")
    })
    @PostMapping("/up_stock_page")
    @HttpApiMethod(urlMapping = "mch.App.Mch.Up_stock_page")
    public Result upStockPage(MainVo vo, @ParamsMapping("shop_id") int shopId, @ParamsMapping("p_id") int pid)
    {
        try
        {
            return Result.success(appsMchBaseService.upStockPage(vo, shopId, pid));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("修改库存")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "shopId", value = "商户id", required = true, dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "attributeInfo", value = "属性信息集 json", dataType = "string", paramType = "form")
    })
    @PostMapping("/up_stock")
    @HttpApiMethod(urlMapping = "mch.App.Mch.Up_stock")
    public Result upStock(MainVo vo, @ParamsMapping("shop_id") int shopId, String attributeInfo)
    {
        try
        {
            appsMchBaseService.upStock(vo, shopId, attributeInfo);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("提交审核/撤销审核")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "shopId", value = "商户id", required = true, dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "p_id", value = "商品id", required = true, dataType = "int", paramType = "form")
    })
    @PostMapping("/submit_audit")
    @HttpApiMethod(urlMapping = "mch.App.Mch.Submit_audit")
    public Result submitAudit(MainVo vo, @ParamsMapping("shop_id") int shopId, @ParamsMapping("p_id") int pId)
    {
        try
        {
            return Result.success(appsMchBaseService.submitAudit(vo, shopId, pId));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("删除商品")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "shopId", value = "商户id", required = true, dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "p_id", value = "商品id", dataType = "int", paramType = "form")
    })
    @PostMapping("/del_my_merchandise")
    @HttpApiMethod(urlMapping = "mch.App.Mch.Del_my_merchandise")
    public Result delMyMerchandise(MainVo vo, @ParamsMapping("shop_id") int shopId, @ParamsMapping("p_id") int pId)
    {
        try
        {
            boolean result = appsMchBaseService.delMyMerchandise(vo, shopId, pId);
            return Result.success(result);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("上/下架商品")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "shopId", value = "商户id", required = true, dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "p_id", value = "商品id 多个用','", dataType = "string", paramType = "form")
    })
    @PostMapping("/my_merchandise_status")
    @HttpApiMethod(urlMapping = "mch.App.Mch.My_merchandise_status")
    public Result myMerchandiseStatus(MainVo vo, @ParamsMapping("shop_id") int shopId, @ParamsMapping("p_id") String pId)
    {
        try
        {
            return Result.success(appsMchBaseService.myMerchandiseStatus(vo, shopId, pId));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("编辑商品")
    @PostMapping("/re_edit")
    @HttpApiMethod(urlMapping = "mch.App.Mch.Re_edit")
    public Result reEdit(UploadMerchandiseVo vo)
    {
        try
        {
            return Result.success(appsMchBaseService.reEdit(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("虚拟商品添加时查询核销门店")
    @PostMapping("/getWriteStore")
    @HttpApiMethod(urlMapping = "mch.App.Mch.getWriteStore")
    public Result reEdit(Integer mchId, @ParamsMapping("store_id") Integer store_id)
    {
        try
        {
            return Result.success(appsMchBaseService.getWriteStore(mchId, store_id));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("虚拟商品判断是否有预约")
    @PostMapping("/getWrite")
    @HttpApiMethod(urlMapping = "mch.App.Mch.getWrite")
    public Result getWrite(Integer dId)
    {
        try
        {
            return Result.success(appsMchBaseService.getWrite(dId));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("店铺主页")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "shopId", value = "商户id", required = true, dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "shopListId", value = "店铺id", dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "longitude", value = "经度", required = true, dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "latitude", value = "维度", required = true, dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "lang_code", value = "语种", required = true, dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "type", value = "类别 1=推荐,2=全部商品,3=商品分类", required = true, dataType = "int", paramType = "form")
    })
    @PostMapping("/store_homepage")
    @HttpApiMethod(urlMapping = "mch.App.Mch.Store_homepage")
    public Result storeHomepage(MainVo vo, @ParamsMapping("shop_id") int shopId, @ParamsMapping("shop_list_id") Integer shopListId,
                                String longitude, String latitude, int type, String lang_code)
    {
        try
        {
            return Result.success(appsMchBaseService.storeHomepage(vo, shopId, shopListId, longitude, latitude, type, lang_code));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @PostMapping("/getMchDiyHomePage")
    @HttpApiMethod(apiKey = "mch.App.Mch.diy_home_page")
    @ApiOperation("获取店铺diy数据")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "shopId", value = "商户id", required = true, dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "longitude", value = "经度", required = true, dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "latitude", value = "维度", required = true, dataType = "string", paramType = "form")
    })
    public Result getMchDiyHomePage(MainVo vo, @ParamsMapping("shop_id") int shopId, String longitude, String latitude) {
        try {
            return Result.success(appsMchBaseService.getMchDiyHomePage(vo, shopId, longitude, latitude));
        } catch (LaiKeAPIException e) {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("添加浏览记录")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "shopId", value = "商户id", required = true, dataType = "int", paramType = "form")
    })
    @PostMapping("/browse_record")
    @HttpApiMethod(urlMapping = "mch.App.Mch.browse_record")
    public Result browseRecord(MainVo vo, @ParamsMapping("shop_id") int shopId)
    {
        try
        {
            appsMchBaseService.browseRecord(vo, shopId);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("店铺主页-加载更多")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "shopId", value = "商户id", required = true, dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "type", value = "类别 1=推荐,2=全部商品,3=商品分类", required = true, dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "page", value = "分页", required = true, dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "lang_code", value = "语种", required = true, dataType = "int", paramType = "form")
    })
    @PostMapping("/store_homepage_load")
    @HttpApiMethod(urlMapping = "mch.App.Mch.Store_homepage_load")
    public Result storeHomepage(MainVo vo, @ParamsMapping("shop_id") int shopId, int type, String lang_code)
    {
        try
        {
            return Result.success(appsMchBaseService.storeHomepageLoad(vo, shopId, type, lang_code));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("店铺点击收藏按钮")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "shopId", value = "商户id", required = true, dataType = "int", paramType = "form")
    })
    @PostMapping("/collection_shop")
    @HttpApiMethod(urlMapping = "mch.App.Mch.Collection_shop")
    public Result collectionShop(MainVo vo, @ParamsMapping("shop_id") int shopId)
    {
        try
        {
            return Result.success(appsMchBaseService.collectionShop(vo, shopId));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("设置店铺页面数据")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "shopId", value = "商户id", required = true, dataType = "int", paramType = "form")
    })
    @PostMapping("/into_set_shop")
    @HttpApiMethod(urlMapping = "mch.App.Mch.Into_set_shop")
    public Result intoSetShop(MainVo vo, @ParamsMapping("shop_id") int shopId)
    {
        try
        {
            return Result.success(appsMchBaseService.intoSetShop(vo, shopId));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("设置店铺")
    @PostMapping("/set_shop")
    @HttpApiMethod(urlMapping = "mch.App.Mch.Set_shop")
    public Result setShop(SetShopVo vo, MultipartFile image)
    {
        try
        {
            if (image != null)
            {
                vo.setFile(image);
            }
            return Result.success(appsMchBaseService.setShop(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("注销店铺")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "shopId", value = "商户id", required = true, dataType = "int", paramType = "form")
    })
    @PostMapping("/cancellation_shop")
    @HttpApiMethod(urlMapping = "mch.App.Mch.Cancellation_shop")
    public Result cancellationShop(MainVo vo, @RequestParam("shop_id") int shopId)
    {
        try
        {
            appsMchBaseService.cancellationShop(vo, shopId);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("我的顾客")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "shopId", value = "商户id", required = true, dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "page", value = "分页", required = true, dataType = "int", paramType = "form")
    })
    @PostMapping("/shop_customer")
    @HttpApiMethod(urlMapping = "mch.App.Mch.Shop_customer")
    public Result shopCustomer(MainVo vo, @ParamsMapping("shop_id") int shopId)
    {
        try
        {
            return Result.success(appsMchBaseService.shopCustomer(vo, shopId));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("我的粉丝")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "shopId", value = "商户id", required = true, dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "page", value = "分页", required = true, dataType = "int", paramType = "form")
    })
    @PostMapping("/shopFans")
    @HttpApiMethod(urlMapping = "mch.App.Mch.ShopFans")
    public Result shopFans(MainVo vo, @ParamsMapping("shop_id") int shopId)
    {
        try
        {
            return Result.success(appsMchBaseService.shopFans(vo, shopId));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("移除粉丝")
    @PostMapping("/removeFans")
    @HttpApiMethod(urlMapping = "mch.App.Mch.RemoveFans")
    public Result removeFans(MainVo vo, Integer cid)
    {
        try
        {
            appsMchBaseService.removeFans(vo, cid);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("我的订单")
    @PostMapping("/my_order")
    @HttpApiMethod(urlMapping = {"mch.App.Mch.My_order", "app.mch.my_order_load"})
    public Result myOrder(MchOrderIndexVo vo)
    {
        try
        {
            return Result.success(appsMchBaseService.myOrder(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("发货列表显示")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "订单号", required = true, dataType = "string", paramType = "form")
    })
    @PostMapping("/deliver_show")
    @HttpApiMethod(urlMapping = "mch.App.Mch.Deliver_show")
    public Result deliverShow(MainVo vo, String id)
    {
        try
        {
            return Result.success(appsMchBaseService.deliverShow(vo, id));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("发货点击发货按钮-弹出填写发货信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "shopId", value = "店铺id", required = true, dataType = "int", paramType = "form")
    })
    @PostMapping("/into_send")
    @HttpApiMethod(urlMapping = "mch.App.Mch.Into_send")
    public Result intoSend(MainVo vo, @ParamsMapping("shop_id") int shopId)
    {
        try
        {
            return Result.success(appsMchBaseService.intoSend(vo, shopId));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("发货")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "shopId", value = "店铺id", required = true, dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "sNo", value = "订单号", required = true, dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "expressId", value = "快递公司id", required = true, dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "courierNum", value = "快递单号", required = true, dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "orderListId", value = "订单明细id集", required = true, dataType = "string", paramType = "form")
    })
    @PostMapping("/send")
    @HttpApiMethod(urlMapping = "mch.App.Mch.Send")
    public Result send(MainVo vo, @ParamsMapping("shop_id") int shopId, String sNo, @ParamsMapping("express_id") Integer expressId,
                       @ParamsMapping("courier_num") String courierNum, @ParamsMapping("orderList_id") String orderListId)
    {
        try
        {
            appsMchBaseService.send(vo, shopId, sNo, expressId, courierNum, orderListId);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("商家自提发货")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "shopId", value = "店铺id", required = true, dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "sNo", value = "订单号", required = true, dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "expressId", value = "快递公司id", required = true, dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "courierNum", value = "快递单号", required = true, dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "orderListId", value = "订单明细id集", required = true, dataType = "string", paramType = "form")
    })
    @PostMapping("/selfSend")
    @HttpApiMethod(urlMapping = "mch.App.Mch.selfSend")
    public Result selfSend(MainVo vo, @ParamsMapping("shop_id") int shopId, String sNo
            , String phone, String courier_name)
    {
        try
        {
            appsMchBaseService.selfSend(vo, shopId, sNo, phone, courier_name);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("新发货提交，后台端发货统一接口")
    @PostMapping("/deliverySave")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "exId", value = "快递公司", required = true, dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "exNo", value = "快递单号", required = true, dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "orderDetailIds", value = "订单明细id集", required = true, dataType = "string", paramType = "form"),
    })
    @HttpApiMethod(apiKey = "mch.App.Mch.UnifiedShipment")
    public Result deliverySave(MainVo vo, String list)
    {
        try
        {
            appsMchBaseService.deliverySave(vo, list);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("关闭订单")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "shopId", value = "店铺id", required = true, dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "sNo", value = "订单号", required = true, dataType = "string", paramType = "form")
    })
    @PostMapping("/closing_order")
    @HttpApiMethod(urlMapping = "mch.App.Mch.Closing_order")
    public Result closingOrder(MainVo vo, @ParamsMapping("shop_id") int shopId, String sNo)
    {
        try
        {
            return Result.success(appsMchBaseService.closingOrder(vo, shopId, sNo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("修改订单")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "shopId", value = "店铺id", required = true, dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "sNo", value = "订单号", required = true, dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "orderDetail", value = "json对象", required = true, dataType = "string", paramType = "form")
    })
    @PostMapping("/upOrder")
    @HttpApiMethod(urlMapping = "mch.App.Mch.Up_order")
    public Result orderDetails(MainVo vo, @ParamsMapping("shop_id") int shopId, String sNo, String orderDetail)
    {
        try
        {
            return Result.success(appsMchBaseService.upOrder(vo, shopId, sNo, orderDetail));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("订单详情")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "shopId", value = "店铺id", required = true, dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "sNo", value = "订单号", required = true, dataType = "string", paramType = "form")
    })
    @PostMapping("/order_details")
    @HttpApiMethod(urlMapping = "mch.App.Mch.Order_details")
    public Result orderDetails(MainVo vo, @ParamsMapping("shop_id") int shopId, String sNo)
    {
        try
        {
            return Result.success(appsMchBaseService.orderDetails(vo, shopId, sNo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("订单详情")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "shopId", value = "店铺id", required = true, dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "sNo", value = "订单号", required = true, dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "id", value = "售后id", required = true, dataType = "string", paramType = "form")
    })
    @PostMapping("/Returndetail")
    @HttpApiMethod(urlMapping = "mch.App.Mch.Returndetail")
    public Result returnOrderDetails(MainVo vo, @ParamsMapping("shop_id") int shopId, String sNo, int id)
    {
        try
        {
            return Result.success(appsMchBaseService.returnOrderDetails(vo, shopId, sNo, id));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("我的提现页面")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "shopId", value = "店铺id", required = true, dataType = "int", paramType = "form")
    })
    @PostMapping("/my_wallet")
    @HttpApiMethod(urlMapping = "mch.App.Mch.My_wallet")
    public Result myWallet(MainVo vo, @ParamsMapping("shop_id") int shopId)
    {
        try
        {
            return Result.success(appsMchBaseService.myWallet(vo, shopId));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("账户收入/支出")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "shopId", value = "店铺id", required = true, dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "type", value = "1=收入 2=支出", required = true, dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "keyWord", value = "关键字", required = true, dataType = "string", paramType = "form"),
    })
    @PostMapping("/wallet_details")
    @HttpApiMethod(urlMapping = {"app.mch.account_details", "app.mch.account_details_load"})
    public Result walletDetails(MainVo vo, @ParamsMapping("shop_id") int shopId, int type, String keyWord)
    {
        try
        {
            return Result.success(appsMchBaseService.walletDetails(vo, shopId, type, keyWord));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("账户明细")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "shopId", value = "店铺id", required = true, dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "type", value = "1=售后明细 2=提现明细", required = true, dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "tabIndex", value = "0：审核中 1：审核通过 2：拒绝", required = true, dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "startDay", value = "开始时间", required = false, dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "endDay", value = "结束时间", required = false, dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "orderNo", value = "订单号", required = false, dataType = "string", paramType = "form")
    })
    @PostMapping("/account_details")
    @HttpApiMethod(urlMapping = {"mch.App.Mch.Detail"})
    public Result accountDetails(MainVo vo, @ParamsMapping("shop_id") int shopId, int type, int tabIndex, String startDay, String endDay, String orderNo)
    {
        try
        {
            return Result.success(appsMchBaseService.accountDetails(vo, shopId, type, tabIndex, startDay, endDay, orderNo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("提现明细")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "shopId", value = "店铺id", required = true, dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "id", value = "提现id", required = true, dataType = "int", paramType = "form")
    })
    @PostMapping("/Withdrawal_details")
    @HttpApiMethod(urlMapping = {"mch.App.Mch.Withdrawal_details"})
    public Result withdrawalDetails(MainVo vo, @ParamsMapping("shop_id") int shopId, int id)
    {
        try
        {
            return Result.success(appsMchBaseService.withdrawalDetails(vo, shopId, id));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("删除提现明细")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "shopId", value = "店铺id", required = true, dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "id", value = "提现id", required = true, dataType = "int", paramType = "form")
    })
    @PostMapping("/delWithdrawalDetails")
    @HttpApiMethod(urlMapping = {"mch.App.Mch.del_Withdrawal_details"})
    public Result delWithdrawalDetails(MainVo vo, @ParamsMapping("shop_id") int shopId, int id)
    {
        try
        {
            return Result.success(appsMchBaseService.delWithdrawalDetails(vo, shopId, id));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("添加我的门店")
    @PostMapping("/add_store")
    @HttpApiMethod(urlMapping = {"mch.App.Mch.Add_store"})
    public Result addStore(AddStoreVo vo)
    {
        try
        {
            return Result.success(appsMchBaseService.addStore(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("编辑我的门店页面数据")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "shopId", value = "店铺id", required = true, dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "id", value = "门店id", required = true, dataType = "int", paramType = "form")
    })
    @PostMapping("/edit_store_page")
    @HttpApiMethod(urlMapping = "mch.App.Mch.Edit_store_page")
    public Result editStorePage(MainVo vo, @ParamsMapping("shop_id") Integer shopId, int id)
    {
        try
        {
            return Result.success(appsMchBaseService.editStorePage(vo, shopId, id));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("编辑我的门店")
    @PostMapping("/edit_store")
    @HttpApiMethod(urlMapping = "mch.App.Mch.Edit_store")
    public Result editStore(AddStoreVo vo)
    {
        try
        {
            return Result.success(appsMchBaseService.editStore(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("删除我的门店")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "shopId", value = "店铺id", required = true, dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "id", value = "门店id集", required = true, dataType = "int", paramType = "form")
    })
    @PostMapping("/del_store")
    @HttpApiMethod(urlMapping = "mch.App.Mch.Del_store")
    public Result delStore(MainVo vo, @ParamsMapping("shop_id") int shopId, String id)
    {
        try
        {
            return Result.success(appsMchBaseService.delStore(vo, shopId, id));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("查看我的门店")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "shopId", value = "店铺id", required = true, dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "proId", value = "虚拟商品id", required = true, dataType = "int", paramType = "form")
    })
    @PostMapping("/see_my_store")
    @HttpApiMethod(urlMapping = {"mch.App.Mch.See_my_store"})
    public Result seeMyStore(MainVo vo, @ParamsMapping("shop_id") Integer shopId, @ParamsMapping("pro_id") Integer proId)
    {
        try
        {
            return Result.success(appsMchBaseService.seeMyStore(vo, shopId, proId));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("查看我的门店预约时间段")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "mchStoreId", value = "门店id", required = true, dataType = "int", paramType = "form")
    })
    @PostMapping("/getAppointmenTime")
    @HttpApiMethod(urlMapping = {"mch.App.Mch.getAppointmenTime"})
    public Result getAppointmenTime(MainVo vo, Integer mchStoreId, Integer w_id)
    {
        try
        {
            return Result.success(appsMchBaseService.getAppointmenTime(vo, mchStoreId, w_id));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("编辑我的门店预约时间段")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "mchStoreId", value = "门店id", required = true, dataType = "int", paramType = "form")
    })
    @PostMapping("/editAppointmenTime")
    @HttpApiMethod(urlMapping = {"mch.App.Mch.editAppointmenTime"})
    public Result getAppointmenTime(EditAppointVo vo, Integer w_id)
    {
        try
        {
            return Result.success(appsMchBaseService.editAppointmentTime(vo, w_id));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("增加我的门店预约时间段")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "mchStoreId", value = "门店id", required = true, dataType = "int", paramType = "form")
    })
    @PostMapping("/addAppointmenTime")
    @HttpApiMethod(urlMapping = {"mch.App.Mch.addAppointmenTime"})
    public Result getAppointmenTime(AddAppointVo vo, @ParamsMapping("store_id") Integer store_id)
    {
        try
        {
            return Result.success(appsMchBaseService.addAppointmentTime(vo, store_id));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("删除我的门店预约时间段")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "mchStoreId", value = "门店id", required = true, dataType = "int", paramType = "form")
    })
    @PostMapping("/deleteAppointmenTime")
    @HttpApiMethod(urlMapping = {"mch.App.Mch.deleteAppointmenTime"})
    public Result getAppointmenTime(EditAppointVo vo)
    {
        try
        {
            return Result.success(appsMchBaseService.deleteAppointmentTime(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("验证码扫码订单信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "shopId", value = "店铺id", required = true, dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "orderId", value = "订单id", dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "extraction_code", value = "提货码", required = true, dataType = "string", paramType = "form")
    })
    @PostMapping("/orderInfoForCode")
    @HttpApiMethod(urlMapping = {"mch.App.Mch.OrderInfoForCode"})
    public Result orderInfoForCode(MainVo vo, @ParamsMapping("shop_id") int shopId, @ParamsMapping("order_id") Integer orderId, @ParamsMapping("extraction_code") String extractionCode, @ParamsMapping("write_shop_id") Integer writeShopId)
    {
        try
        {
            return Result.success(appsMchBaseService.orderInfoForCode(vo, shopId, orderId, extractionCode, writeShopId));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("验证码提货")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "shopId", value = "店铺id", required = true, dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "orderId", value = "订单id", dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "extraction_code", value = "提货码", required = true, dataType = "string", paramType = "form")
    })
    @PostMapping("/verification_extraction_code")
    @HttpApiMethod(urlMapping = {"app.mch.verification_extraction_code", "mch.App.Mch.Sweep_extraction_code"})
    public Result verificationExtractionCode(MainVo vo, @ParamsMapping("shop_id") int shopId, @ParamsMapping("order_id") Integer orderId,
                                             @ParamsMapping("extraction_code") String extractionCode, @ParamsMapping("write_shop_id") Integer mch_store_id, @ParamsMapping("p_id") Integer pid)
    {
        try
        {
            return Result.success(appsMchBaseService.verificationExtractionCode(vo, shopId, orderId, extractionCode, mch_store_id, pid));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("无预约虚拟商品选择门店")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "goodId", value = "商品id", required = true, dataType = "int")
    })
    @PostMapping("/get_write_shop")
    @HttpApiMethod(urlMapping = "mch.App.Mch.get_write_shop")
    public Result verificationExtractionCode(@ParamsMapping("s_no") String sNo)
    {
        try
        {
            return Result.success(appsMchBaseService.get_write_shop(sNo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("显示省份")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "sel_city", value = "已选中省份", dataType = "string", paramType = "form")
    })
    @PostMapping("get_sheng")
    @HttpApiMethod(urlMapping = "mch.App.Mch.Get_sheng")
    public Result getSheng(String sel_city,Integer level,Integer pid) throws LaiKeAPIException
    {
        try
        {
            if (Objects.isNull(level))
            {
                level = DictionaryConst.Position.LEVEL_2;
            }
            if (Objects.isNull(pid))
            {
                pid = 0;
            }
            return Result.success(publiceService.getJoinCityCounty(level, pid, sel_city));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("运费列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "shopId", value = "店铺id", required = true, dataType = "int", paramType = "form")
    })
    @PostMapping("/freight_list")
    @HttpApiMethod(urlMapping = "mch.App.Mch.Freight_list")
    public Result freightList(MainVo vo, @ParamsMapping("shop_id") Integer shopId)
    {
        try
        {
            return Result.success(appsMchBaseService.freightList(vo, shopId));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("添加运费")
    @PostMapping("/freight_add")
    @HttpApiMethod(urlMapping = "mch.App.Mch.Freight_add")
    public Result freightAdd(AddFreihtVo vo)
    {
        try
        {
            appsMchBaseService.addFreight(vo);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("编辑运费")
    @PostMapping("/freight_modify")
    @HttpApiMethod(urlMapping = "mch.App.Mch.Freight_modify")
    public Result freightModify(AddFreihtVo vo)
    {
        try
        {
            appsMchBaseService.addFreight(vo);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("修改默认运费")
    @PostMapping("/set_default")
    @HttpApiMethod(urlMapping = "mch.App.Mch.Set_default")
    public Result setDefault(AddFreihtVo vo)
    {
        try
        {
            appsMchBaseService.setDefault(vo);
            return Result.success(true);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("删除运费")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "ids", value = "运费id集", required = true, dataType = "string", paramType = "form")
    })
    @PostMapping("/freight_del")
    @HttpApiMethod(urlMapping = "mch.App.Mch.Freight_del")
    public Result freightDel(MainVo vo, @ParamsMapping("id") String id, @ParamsMapping("shop_id") int shopId)
    {
        try
        {
            appsMchBaseService.freightDel(vo, id, shopId);
            return Result.success(true);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("编辑运费页面数据")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "shopId", value = "店铺id", required = true, dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "id", value = "运费id", required = true, dataType = "int", paramType = "form")
    })
    @PostMapping("/freight_modify_show")
    @HttpApiMethod(urlMapping = "mch.App.Mch.Freight_modify_show")
    public Result freightModifyShow(MainVo vo, @ParamsMapping("shop_id") int shopId, int id)
    {
        try
        {
            Map<String, Object> resultMap = appsMchBaseService.freightModifyShow(vo, shopId, id);
            return Result.success(resultMap);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("售后审核通过/拒绝")
    @PostMapping("/examine")
    @HttpApiMethod(urlMapping = "mch.App.Mch.Examine")
    public Result examine(RefundVo vo)
    {
        try
        {
            appsMchBaseService.examine(vo);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("银行卡解绑【店铺】")
    @PostMapping("/delBank")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "bankId", value = "银行卡id", required = true, dataType = "int", paramType = "form")
    })
    @HttpApiMethod(urlMapping = {"mch.App.MchBank.DelBank"})
    public Result delBank(MainVo vo, @ParamsMapping("id") int bankId)
    {
        try
        {
            appsMchBaseService.delBank(vo, bankId);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("结算列表及详情")
    @PostMapping("/getSettlementOrderList")
    @HttpApiMethod(urlMapping = "mch.App.Mch.GetSettlementOrderList")
    public Result getSettlementOrderList(OrderSettlementVo vo)
    {
        try
        {
            Map<String, Object> resultMap = appsMchBaseService.getSettlementOrderList(vo);
            return Result.success(resultMap);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("添加门店账户")
    @PostMapping("/addMchStoreAccount")
    @HttpApiMethod(urlMapping = {"app.mch.addMchStoreAccount"})
    public Result addMchStoreAccount(MchStoreAccountVo vo)
    {
        try
        {
            appsMchBaseService.addMchStoreAccount(vo);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("删除门店账户")
    @PostMapping("/delMchStoreAccount")
    @HttpApiMethod(urlMapping = {"app.mch.delMchStoreAccount"})
    public Result delMchStoreAccount(MainVo vo, Integer id)
    {
        try
        {
            appsMchBaseService.delMchStoreAccount(vo, id);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("门店账户列表")
    @PostMapping("/mchStoreAccountList")
    @HttpApiMethod(urlMapping = "app.mch.mchStoreAccountList")
    public Result mchStoreAccountList(MchStoreAccountVo vo)
    {
        try
        {
            Map<String, Object> resultMap = appsMchBaseService.mchStoreAccountList(vo);
            return Result.success(resultMap);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("上传收款二维码")
    @PostMapping("/collectionCode")
    @HttpApiMethod(urlMapping = {"app.mch.collectionCode"})
    public Result collectionCode(MainVo vo, Integer mchId, String code)
    {
        try
        {
            appsMchBaseService.collectionCode(vo, mchId, code);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("获取门店管理员列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "mch_store_id", value = "门店id", required = true, dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "account_number", value = "管理员账号", required = true, dataType = "int", paramType = "form"),
    })
    @PostMapping("/AdminList")
    @HttpApiMethod(apiKey = "mch.App.Mch.StoreAdminList")
    public Result getAdminList(MainVo vo, Integer mch_store_id)
    {
        try
        {
            return Result.success(appsMchBaseService.StoreAdminList(vo, mch_store_id));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("添加修改管理员")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "mch_store_id", value = "门店id", required = true, dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "account_number", value = "管理员账号", required = true, dataType = "int", paramType = "form"),
    })
    @PostMapping("/AddAdmin")
    @HttpApiMethod(apiKey = "mch.App.Mch.AddStoreAdmin")
    public Result addAdmin(MainVo vo, Integer mch_store_id, String account_number, String password, Integer id)
    {
        try
        {
            appsMchBaseService.addAdmin(vo, mch_store_id, account_number, password, id);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("删除管理员")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "mch_store_id", value = "门店id", required = true, dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "id", value = "管理员id", required = true, dataType = "int", paramType = "form"),
    })
    @PostMapping("/DelAdmin")
    @HttpApiMethod(apiKey = "mch.App.Mch.DelStoreAdmin")
    public Result delAdmin(MainVo vo, Integer mch_store_id, Integer id)
    {
        try
        {
            appsMchBaseService.delAdmin(vo, mch_store_id, id);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("极速退款")
    @PostMapping("/quickRefund")
    @HttpApiMethod(apiKey = "mch.App.Mch.CancellationOfOrder")
    public Result quickRefund(RefundVo vo)
    {
        try
        {
            Map<String,Object> res = appsMchBaseService.quickRefund(vo);
            return Result.success(res);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

}
