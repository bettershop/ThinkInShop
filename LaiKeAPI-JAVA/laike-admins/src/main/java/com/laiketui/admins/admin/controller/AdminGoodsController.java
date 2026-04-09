package com.laiketui.admins.admin.controller;

import com.laiketui.admins.api.admin.AdminBrandService;
import com.laiketui.admins.api.admin.AdminFreightService;
import com.laiketui.admins.api.admin.AdminGoodsDubboService;
import com.laiketui.admins.api.admin.AdminStockService;
import com.laiketui.common.api.PubliceService;
import com.laiketui.core.annotation.ParamsMapping;
import com.laiketui.core.domain.Result;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.goods.*;
import com.laiketui.domain.vo.mch.AddFreihtVo;
import com.laiketui.domain.vo.mch.ApplyShopVo;
import com.laiketui.domain.vo.mch.UploadMerchandiseVo;
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

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * 后台-商品管理
 *
 * @author Trick
 * @date 2020/12/28 17:26
 */
@Api(tags = "后台-商品管理")
@RestController
@RequestMapping("/admin/goods")
public class AdminGoodsController
{

    @Autowired
    private AdminGoodsDubboService adminGoodsDubboService;

    @Autowired
    private AdminBrandService adminBrandService;

    @Autowired
    private AdminFreightService adminFreightService;

    @Autowired
    private AdminStockService adminStockService;

    @Autowired
    private PubliceService publiceService;

    @ApiOperation("获取类别信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "classId", value = "类型id", dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "brandId", value = "品牌id", dataType = "int", paramType = "form"),
    })
    @PostMapping("/getClass")
    @HttpApiMethod(apiKey = "admin.goods.getClass")
    public Result getClass(MainVo vo, Integer classId, Integer brandId)
    {
        try
        {
            Map<String, Object> result = adminGoodsDubboService.getClass(vo, classId, brandId);
            return Result.success(result);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("选择类别")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "classId", value = "商品类型id", dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "brandId", value = "品牌id", dataType = "int", paramType = "form")
    })
    @PostMapping("/choiceClass")
    @HttpApiMethod(apiKey = "admin.goods.choiceClass")
    public Result choiceClass(MainVo vo, Integer classId, Integer brandId)
    {
        try
        {
            return Result.success(adminGoodsDubboService.choiceClass(vo, classId, brandId));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("获取商品列表(规格)")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "attribute_str", value = "属性id,多个用','", dataType = "string", paramType = "form")
    })
    @PostMapping("/getGoodsConfigureList")
    @HttpApiMethod(apiKey = "admin.goods.getGoodsConfigureList")
    public Result getGoodsConfigureList(GoodsConfigureVo vo)
    {
        try
        {
            return Result.success(adminGoodsDubboService.getGoodsConfigureList(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("获取商品属性(规格)名")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "attribute_str", value = "属性id,多个用','", dataType = "string", paramType = "form")
    })
    @PostMapping("/get_attribute_name")
    @HttpApiMethod(apiKey = "admin.goods.get_attribute_name")
    public Result get_attribute_name(MainVo vo, @RequestParam("attribute_str") String attributes)
    {
        try
        {
            Map<String, Object> result = adminGoodsDubboService.getAttributeName(vo, attributes);
            return Result.success(result);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("获取商品属性(规格)值")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "attribute_str", value = "属性id,多个用','", required = true, dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "attr_arr", value = "属性id", dataType = "int", paramType = "form")
    })
    @PostMapping("/getAttributeValue")
    @HttpApiMethod(apiKey = "admin.goods.getAttributeValue")
    public Result getAttributeValue(MainVo vo, @RequestParam("attribute_str") String attributes, @RequestParam("attr_arr") Integer attrId)
    {
        try
        {
            Map<String, Object> result = adminGoodsDubboService.getAttributeValue(vo, attributes, attrId);
            return Result.success(result);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("添加自营店铺")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "logo", value = "logo URL", required = true, dataType = "string", paramType = "form"),
    })
    @PostMapping("/addMch")
    @HttpApiMethod(apiKey = "admin.goods.addMch")
    public Result addMch(ApplyShopVo vo, String logo)
    {
        try
        {
            return Result.success(adminGoodsDubboService.addMch(vo, logo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("获取添加商品页面数据")
    @PostMapping("/getAddPage")
    @HttpApiMethod(apiKey = "admin.goods.getAddPage")
    public Result getAddPage(MainVo vo)
    {
        try
        {
            return Result.success(adminGoodsDubboService.getAddPage(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("根据id获取商品信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "goodsId", value = "商品id", required = true, dataType = "int", paramType = "form")
    })
    @PostMapping("/getGoodsInfoById")
    @HttpApiMethod(apiKey = "admin.goods.getGoodsInfoById")
    public Result getGoodsInfoById(MainVo vo, int goodsId)
    {
        try
        {
            return Result.success(adminGoodsDubboService.getGoodsInfoById(vo, goodsId));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("添加虚拟商品获取自营店门店列表")
    @PostMapping("/getMchStore")
    @HttpApiMethod(apiKey = "admin.addGoods.getMchStore")
    public Result getMchStore(MainVo vo, Integer mchId)
    {
        try
        {
            return Result.success(publiceService.getMchStore(vo, mchId));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("添加商品")
    @PostMapping("/addGoods")
    @HttpApiMethod(apiKey = "admin.goods.addGoods")
    public Result addGoods(UploadMerchandiseVo vo)
    {
        try
        {
            return Result.success(adminGoodsDubboService.addGoods(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("批量上传商品")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "image", value = "文件集", required = true, dataType = "file", paramType = "form"),
            @ApiImplicitParam(name = "productClassId", value = "分类id", required = true, dataType = "file", paramType = "form"),
            @ApiImplicitParam(name = "brandId", value = "品牌id", required = true, dataType = "file", paramType = "form"),
            @ApiImplicitParam(name = "freightId", value = "运费id", required = true, dataType = "file", paramType = "form"),
    })
    @PostMapping("/uploadAddGoods")
    @HttpApiMethod(apiKey = "admin.goods.uploadAddGoods")
    public Result uploadAddGoods(MainVo vo, List<MultipartFile> image, String productClassId, String brandId, String freightId)
    {
        try
        {
            Boolean flag = adminGoodsDubboService.uploadAddGoods(vo, image, productClassId, brandId, freightId);
            if (!flag)
            {
                return Result.error(ErrorCode.BizErrorCode.ERROR_CODE_DRSB,"导入失败");
            }
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("批量上传记录列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "key", value = "文件名称", dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "status", value = "1=成功 0=失败", dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "startDate", value = "开始时间", dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "endDate", value = "结束时间", dataType = "string", paramType = "form"),
    })
    @PostMapping("/uploadRecordList")
    @HttpApiMethod(apiKey = "admin.goods.uploadRecordList")
    public Result uploadRecordList(MainVo vo, String key, Integer status, String startDate, String endDate)
    {
        try
        {
            return Result.success(adminGoodsDubboService.uploadRecordList(vo, key, status, startDate, endDate));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("删除发货记录")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "id", dataType = "string", paramType = "form")
    })
    @PostMapping("/delUploadRecord")
    @HttpApiMethod(apiKey = "admin.goods.delUploadRecord")
    public Result delUploadRecord(MainVo vo, String id)
    {
        try
        {
            adminGoodsDubboService.delUploadRecord(vo, id);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("修改序号")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "商品id", required = true, dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "sort", value = "序号", required = true, dataType = "int", paramType = "form"),
    })
    @PostMapping("/editSort")
    @HttpApiMethod(apiKey = "admin.goods.editSort")
    public Result editSort(MainVo vo, Integer id, Integer sort)
    {
        try
        {
            adminGoodsDubboService.editSort(vo, id, sort);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("批量删除商品")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "goodsId", value = "商品id 集", dataType = "string", required = true, paramType = "form")
    })
    @PostMapping("/delGoodsById")
    @HttpApiMethod(apiKey = "admin.goods.delGoodsById")
    public Result delGoodsById(MainVo vo, String goodsId)
    {
        try
        {
            boolean result = adminGoodsDubboService.delGoodsById(vo, goodsId);
            return Result.success(result);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("上下架商品")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "goodsIds", value = "商品id集", dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "status", value = "1=上架 0=下架", dataType = "int", paramType = "form"),
    })
    @PostMapping("/upperAndLowerShelves")
    @HttpApiMethod(urlMapping = {"admin.goods.upperAndLowerShelves", "admin.goods.BatchLoadingAndUnloading"})
    public Result upperAndLowerShelves(MainVo vo, String goodsIds, Integer status)
    {
        try
        {
            adminGoodsDubboService.upperAndLowerShelves(vo, goodsIds, status);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("批量选择位置")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "goodsIds", value = "商品id集", dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "status", value = "显示位置 首页 分类 购物车 我的", dataType = "int", paramType = "form"),
    })
    @PostMapping("/batchSelectionOfLocations")
    @HttpApiMethod(urlMapping = {"admin.goods.BatchSelectionOfLocations"})
    public Result batchSelectionOfLocations(MainVo vo, String goodsIds, String status)
    {
        try
        {
            adminGoodsDubboService.batchSelectionOfLocations(vo, goodsIds, status);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("批量预警")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "goodsIds", value = "商品id集", dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "min_inventory", value = "预警值", dataType = "int", paramType = "form"),
    })
    @PostMapping("/BatchWarning")
    @HttpApiMethod(urlMapping = {"admin.goods.BatchWarning"})
    public Result BatchWarning(MainVo vo, String goodsIds, Integer min_inventory)
    {
        try
        {
            adminGoodsDubboService.batchWarning(vo, goodsIds, min_inventory);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("获取店铺运费")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "goodsIds", value = "商品id集", dataType = "string", paramType = "form")
    })
    @PostMapping("/batchObtainShippingFees")
    @HttpApiMethod(urlMapping = {"admin.goods.BatchObtainShippingFees"})
    public Result BatchSetShippingFees(MainVo vo, String goodsIds)
    {
        try
        {
            return Result.success(adminGoodsDubboService.batchObtainShippingFees(vo, goodsIds));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("批量设置运费")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "goodsIds", value = "商品id集", dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "fid", value = "运费ID", dataType = "int", paramType = "form"),
    })
    @PostMapping("/BatchSetShippingFees")
    @HttpApiMethod(urlMapping = {"admin.goods.BatchSetShippingFees"})
    public Result BatchSetShippingFees(MainVo vo, String goodsIds, Integer fid)
    {
        try
        {
            adminGoodsDubboService.batchSetShippingFees(vo, goodsIds, fid);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("商品置顶")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "goodsId", value = "商品id", dataType = "int", paramType = "form")
    })
    @PostMapping("/goodsByTop")
    @HttpApiMethod(apiKey = "admin.goods.goodsByTop")
    public Result goodsByTop(MainVo vo, int goodsId)
    {
        try
        {
            boolean result = adminGoodsDubboService.goodsByTop(vo, goodsId);
            return Result.success(result);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("上下移动商品位置")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "goodsId", value = "商品id", dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "moveGoodsId", value = "上一个/下一个商品的id", dataType = "int", paramType = "form")
    })
    @PostMapping("/goodsMovePosition")
    @HttpApiMethod(apiKey = "admin.goods.goodsMovePosition")
    public Result goodsMovePosition(MainVo vo, int goodsId, int moveGoodsId)
    {
        try
        {
            boolean result = adminGoodsDubboService.goodsMovePosition(vo, goodsId, moveGoodsId);
            return Result.success(result);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("获取分类信息")
    @PostMapping("/getClassInfo")
    @HttpApiMethod(apiKey = "admin.goods.getClassInfo")
    public Result getClassInfo(GoodsClassVo vo, HttpServletResponse response)
    {
        try
        {
            return Result.success(adminGoodsDubboService.getClassInfo(vo, response));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("找当前类别所有上级")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "classId", value = "类别id", dataType = "int", paramType = "form")
    })
    @PostMapping("/getClassLevelTopAllInfo")
    @HttpApiMethod(apiKey = "admin.goods.getClassLevelTopAllInfo")
    public Result getClassLevelTopAllInfo(MainVo vo, int classId)
    {
        try
        {
            return Result.success(adminGoodsDubboService.getClassLevelTopAllInfo(vo, classId));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("删除当前类别")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "classId", value = "类别id", dataType = "int", paramType = "form")
    })
    @PostMapping("/delClass")
    @HttpApiMethod(apiKey = "admin.goods.delClass")
    public Result delClass(MainVo vo, int classId)
    {
        try
        {
            adminGoodsDubboService.delClass(vo, classId);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("添加/编辑类别")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "classId", value = "类别id(修改)", dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "className", value = "类别名称", dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "ename", value = "类别英文副标题", dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "level", value = "需要添加的类别等级", dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "fatherId", value = "上级id", dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "img", value = "类别图标", dataType = "string", paramType = "form")
    })
    @PostMapping("/addClass")
    @HttpApiMethod(apiKey = "admin.goods.addClass")
    public Result addClass(MainVo vo, Integer classId, String className, String ename, String img, int level, int fatherId)
    {
        try
        {
            adminGoodsDubboService.addClass(vo, classId, className, ename, img, level, fatherId);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("类别置顶")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "classId", value = "类别id", dataType = "int", paramType = "form")
    })
    @PostMapping("/classSortTop")
    @HttpApiMethod(apiKey = "admin.goods.classSortTop")
    public Result classSortTop(MainVo vo, Integer classId)
    {
        try
        {
            return Result.success(adminGoodsDubboService.classSortTop(vo, classId));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("获取国家列表")
    @PostMapping("/getCountry")
    @HttpApiMethod(apiKey = "admin.goods.getCountry")
    public Result getCountry(MainVo vo)
    {
        try
        {
            return Result.success(publiceService.getCountryList(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("语种列表")
    @PostMapping("/getLangs")
    @HttpApiMethod(apiKey = "admin.goods.getLangs")
    public Result getLangs(MainVo vo)
    {
        try
        {
            //只获取平台创建商城时候给商城配置的语种 在 lkt_customer 表的 store_langs 存的是 lkt_lang 表的语种id 逗号分隔开的
            return Result.success(publiceService.getAdminLangs(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("获取品牌信息")
    @PostMapping("/getBrandInfo")
    @HttpApiMethod(apiKey = "admin.goods.getBrandInfo")
    public Result getBrandInfo(BrandClassVo vo, HttpServletResponse response)
    {
        try
        {
            return Result.success(adminBrandService.getBrandInfo(vo, response));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("保存/编辑品牌信息")
    @PostMapping("/addBrand")
    @HttpApiMethod(apiKey = "admin.goods.addBrand")
    public Result addBrand(BrandClassVo vo)
    {
        try
        {
            return Result.success(adminBrandService.addBrand(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("删除品牌")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "brandId", value = "品牌id", dataType = "int", paramType = "form")
    })
    @PostMapping("/delBrand")
    @HttpApiMethod(apiKey = "admin.goods.delBrand")
    public Result delBrand(MainVo vo, int brandId)
    {
        try
        {
            return Result.success(adminBrandService.delBrand(vo, brandId));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("品牌置顶")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "brandId", value = "品牌id", dataType = "int", paramType = "form")
    })
    @PostMapping("/brandByTop")
    @HttpApiMethod(apiKey = "admin.goods.brandByTop")
    public Result brandByTop(MainVo vo, int brandId)
    {
        try
        {
            return Result.success(adminBrandService.brandByTop(vo, brandId));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("获取运费列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "fid", value = "运费id", dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "mchId", value = "店铺id", dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "status", value = "使用状态 0=未使用 1=使用中", dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "name", value = "模板名称", dataType = "int", paramType = "form"),
    })
    @PostMapping("/getFreightInfo")
    @HttpApiMethod(apiKey = "admin.goods.getFreightInfo")
    public Result getFreightInfo(MainVo vo, Integer mchId, Integer fid, Integer status, String name)
    {
        try
        {
            return Result.success(adminFreightService.getFreightInfo(vo, mchId, fid, status, name));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("获取地区列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "level", value = "默认全部 2=省 3=市 4=区", dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "sid", value = "上级id", dataType = "int", paramType = "form"),
    })
    @PostMapping("/getRegion")
    @HttpApiMethod(apiKey = "admin.goods.getRegion")
    public Result getRegion(MainVo vo, Integer level, Integer sid)
    {
        try
        {
            return Result.success(adminFreightService.getRegion(vo, level, sid));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("运费设置默认开关")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "运费id", dataType = "int", paramType = "form")
    })
    @PostMapping("/freightSetDefault")
    @HttpApiMethod(apiKey = "admin.goods.freightSetDefault")
    public Result freightSetDefault(MainVo vo, int id)
    {
        try
        {
            adminFreightService.freightSetDefault(vo, id);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("添加/修改运费")
    @PostMapping("/addFreight")
    @HttpApiMethod(apiKey = "admin.goods.addFreight")
    public Result addFreight(AddFreihtVo vo)
    {
        try
        {
            return Result.success(adminFreightService.addFreight(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("删除运费")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "freightIds", value = "运费模板id,支持多个", dataType = "int", paramType = "form")
    })
    @PostMapping("/delFreight")
    @HttpApiMethod(apiKey = "admin.goods.delFreight")
    public Result delFreight(MainVo vo, @ParamsMapping("idList") String freightIds)
    {
        try
        {
            adminFreightService.delFreight(vo, freightIds);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("库存列表")
    @PostMapping("/getStockInfo")
    @HttpApiMethod(apiKey = "admin.goods.getStockInfo")
    public Result getStockInfo(StockInfoVo vo, HttpServletResponse response)
    {
        try
        {
            Map<String, Object> ret = adminStockService.getStockInfo(vo, response);
            return ret == null ? Result.exportFile() : Result.success(ret);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("获取商品库存详细信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "attrId", value = "属性id", dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "pid", value = "商品id", dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "type", value = "类型 0.入库 1.出库 2.预警 3.入库+出库", dataType = "int", paramType = "form")
    })
    @PostMapping("/getStockDetailInfo")
    @HttpApiMethod(apiKey = "admin.goods.getStockDetailInfo")
    public Result getStockDetailInfo(StockInfoVo vo, Integer attrId, Integer pid, Integer type, HttpServletResponse response)
    {
        try
        {
            Map<String, Object> ret = adminStockService.getStockDetailInfo(vo, attrId, pid, type, response);
            return ret == null ? Result.exportFile() : Result.success(ret);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("添加库存")
    @PostMapping("/addStock")
    @HttpApiMethod(apiKey = "admin.goods.addStock")
    public Result getStockDetailInfo(AddStockVo vo)
    {
        try
        {
            return Result.success(adminStockService.addStock(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("批量添加库存")
    @PostMapping("/batchAddStock")
    @HttpApiMethod(apiKey = "admin.Goods.BatchAddStock")
    public Result batchAddStock(AddStockVo vo)
    {
        try
        {
            adminStockService.batchAddStock(vo);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("商品支持的活动类型")
    @PostMapping("/getGoodsActive")
    @HttpApiMethod(apiKey = "admin.goods.getGoodsActive")
    public Result getGoodsActive(MainVo vo)
    {
        try
        {
            return Result.success(publiceService.getGoodsActive(vo.getStoreId()));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


}
