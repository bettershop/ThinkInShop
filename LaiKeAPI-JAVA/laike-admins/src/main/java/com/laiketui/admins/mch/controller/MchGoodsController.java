package com.laiketui.admins.mch.controller;

import com.laiketui.admins.api.mch.MchGoodsService;
import com.laiketui.common.api.PubliceService;
import com.laiketui.core.domain.Result;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.admin.DefaultViewVo;
import com.laiketui.domain.vo.goods.AddStockVo;
import com.laiketui.domain.vo.goods.BrandClassVo;
import com.laiketui.domain.vo.goods.GoodsClassVo;
import com.laiketui.domain.vo.goods.StockInfoVo;
import com.laiketui.domain.vo.mch.UploadMerchandiseVo;
import com.laiketui.domain.vo.mch.pc.DelBrandVo;
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
import java.util.Map;

/**
 * 商品管理
 *
 * @author Trick
 * @date 2021/5/31 15:55
 */
@Api(tags = "商品管理")
@RestController
@RequestMapping("/admin/mch/goods/")
public class MchGoodsController
{

    @Autowired
    private MchGoodsService mchGoodsService;

    @ApiOperation("商品列表")
    @PostMapping("/index")
    @HttpApiMethod(apiKey = "mch.Mch.Goods.Index")
    public Result index(DefaultViewVo vo, HttpServletResponse response)
    {
        try
        {
            return Result.success(mchGoodsService.index(vo, response));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("代售可选商品列表")
    @PostMapping("/consignmentPro")
    @HttpApiMethod(apiKey = "admin.mch.goods.consignmentPro")
    public Result consignmentPro(DefaultViewVo vo)
    {
        try
        {
            return Result.success(mchGoodsService.consignmentPro(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("获取店铺商品配置")
    @PostMapping("/getCommoditySetup")
    @HttpApiMethod(apiKey = "mch.Mch.Goods.GetCommoditySetup")
    public Result getCommoditySetup(MainVo vo)
    {
        try
        {
            return Result.success(mchGoodsService.getCommoditySetup(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @Autowired
    private PubliceService publiceService;

    @ApiOperation("商品支持的活动类型")
    @PostMapping("/getGoodsActive")
    @HttpApiMethod(apiKey = "mch.Mch.Goods.GetGoodsActive")
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

    @ApiOperation("商品标签列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "主键id", dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "name", value = "商品名称", dataType = "string", paramType = "form"),
    })
    @PostMapping("/getGoodsLabel")
    @HttpApiMethod(apiKey = "mch.Mch.Goods.GetGoodsLabel")
    public Result getGoodsLabel(MainVo vo, String name, Integer id)
    {
        try
        {
            return Result.success(mchGoodsService.getGoodsLabel(vo, name, id));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("获取类别信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "classId", value = "类型id", dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "brandId", value = "品牌id", dataType = "int", paramType = "form"),
    })
    @PostMapping("/getClass")
    @HttpApiMethod(apiKey = "mch.Mch.Goods.GetClass")
    public Result getClass(MainVo vo, Integer classId, Integer brandId)
    {
        try
        {
            return Result.success(mchGoodsService.getClass(vo, classId, brandId));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("获取商品属性(规格)名")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "attributesName", value = "规格名称", dataType = "string", paramType = "form")
    })
    @PostMapping("/getAttributeName")
    @HttpApiMethod(apiKey = "admin.mch.goods.getAttributeName")
    public Result getAttributeName(MainVo vo, String attributesName)
    {
        try
        {
            Map<String, Object> result = mchGoodsService.getAttributeName(vo, attributesName);
            return Result.success(result);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("获取商品属性(规格)值")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "attributesIds", value = "规格id,多个用','", required = true, dataType = "string", paramType = "form")
    })
    @PostMapping("/getAttributeValue")
    @HttpApiMethod(apiKey = "admin.mch.goods.getAttributeValue")
    public Result getAttributeValue(MainVo vo, String attributesIds)
    {
        try
        {
            Map<String, Object> result = mchGoodsService.getAttributeValue(vo, attributesIds);
            return Result.success(result);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("获取商品规格列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "goodsId", value = "商品id", required = true, dataType = "int", paramType = "form")
    })
    @PostMapping("/getAttrByGoodsId")
    @HttpApiMethod(apiKey = "mch.Mch.Goods.GetAttrByGoodsId")
    public Result getAttrByGoodsId(MainVo vo, Integer goodsId)
    {
        try
        {
            return Result.success(mchGoodsService.getAttrByGoodsId(vo, goodsId));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("获取(发布商品)商品页面数据")
    @PostMapping("/getAddPage")
    @HttpApiMethod(apiKey = "admin.mch.goods.getAddPage")
    public Result getAddPage(MainVo vo)
    {
        try
        {
            return Result.success(mchGoodsService.getAddPage(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("上传商品")
    @PostMapping("/addGoods")
    @HttpApiMethod(apiKey = "mch.Mch.Goods.AddGoods")
    public Result addGoods(UploadMerchandiseVo vo)
    {
        try
        {
            return Result.success(mchGoodsService.addGoods(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("获取分类信息")
    @PostMapping("/getClassInfo")
    @HttpApiMethod(apiKey = "mch.Mch.goods.getClassInfo")
    public Result getClassInfo(GoodsClassVo vo, HttpServletResponse response)
    {
        try
        {
            return Result.success(mchGoodsService.getClassInfo(vo, response));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("添加/编辑类别")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "类别id(修改)", dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "className", value = "类别名称", dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "ename", value = "类别英文副标题", dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "level", value = "需要添加的类别等级", dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "fatherId", value = "上级id", dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "img", value = "类别图标", dataType = "string", paramType = "form")
    })
    @PostMapping("/addClass")
    @HttpApiMethod(apiKey = "mch.Mch.goods.addClass")
    public Result addClass(MainVo vo, Integer id, String className, String ename, String img, Integer level, Integer fatherId, @RequestParam(value = "type",required = false) Integer type)
    {
        try
        {
            mchGoodsService.addClass(vo, id, className, ename, img, level, fatherId,type);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("查询分类审核列表")
    @PostMapping("/classAuditList")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "condition", value = "多条件", dataType = "String", paramType = "form"),
            @ApiImplicitParam(name = "status", value = "审核状态 0.待审核 1.审核通过 2.不通过", dataType = "Integer", paramType = "form"),
            @ApiImplicitParam(name = "startTime", value = "开始时间", dataType = "String", paramType = "form"),
            @ApiImplicitParam(name = "endTime", value = "结束时间", dataType = "String", paramType = "form"),
            @ApiImplicitParam(name = "level", value = "级别", dataType = "Integer", paramType = "form")
    })
    @HttpApiMethod(apiKey = "mch.Mch.ProClass.auditList")
    public Result auditList(MainVo vo, String condition, Integer status, String startTime, String endTime,Integer level)
    {
        try
        {
            return Result.success(mchGoodsService.classAuditList(vo, condition, status, startTime, endTime,level));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("获取品牌信息")
    @PostMapping("/getBrandInfo")
    @HttpApiMethod(apiKey = "mch.Mch.goods.getBrandInfo")
    public Result getBrandInfo(BrandClassVo vo, HttpServletResponse response)
    {
        try
        {
            return Result.success(mchGoodsService.getBrandInfo(vo, response));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("保存/编辑品牌信息")
    @PostMapping("/addBrand")
    @HttpApiMethod(apiKey = "mch.Mch.goods.addBrand")
    public Result addBrand(BrandClassVo vo)
    {
        try
        {
            return Result.success(mchGoodsService.addBrand(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("查询审核列表")
    @PostMapping("/brandAuditList")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "condition", value = "多条件", dataType = "String", paramType = "form"),
            @ApiImplicitParam(name = "status", value = "审核状态 0.待审核 1.审核通过 2.不通过", dataType = "Integer", paramType = "form"),
            @ApiImplicitParam(name = "startTime", value = "开始时间", dataType = "String", paramType = "form"),
            @ApiImplicitParam(name = "endTime", value = "结束时间", dataType = "String", paramType = "form"),
    })
    @HttpApiMethod(apiKey = "mch.Mch.Brand.auditList")
    public Result auditList(MainVo vo, Integer id, String condition, Integer status, String startTime, String endTime)
    {
        try
        {
            return Result.success(mchGoodsService.brandAuditList(vo, id, condition, status, startTime, endTime));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("获取国家列表")
    @PostMapping("/getCountry")
    @HttpApiMethod(apiKey = "mch.Mch.goods.getCountry")
    public Result getCountry(MainVo vo)
    {
        try
        {
            return Result.success(mchGoodsService.getCountry(vo));
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
    @HttpApiMethod(apiKey = "mch.Mch.goods.getClassLevelTopAllInfo")
    public Result getClassLevelTopAllInfo(MainVo vo, int classId)
    {
        try
        {
            return Result.success(mchGoodsService.getClassLevelTopAllInfo(vo, classId));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("上架自选商品")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "goodsIds", value = "自选商品id集合','", required = true, dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "yunFeiId", value = "运费模板id','", required = true, dataType = "int", paramType = "form"),
    })
    @PostMapping("/addZxGoods")
    @HttpApiMethod(apiKey = "mch.Mch.Goods.AddZxGoods")
    public Result addZxGoods(MainVo vo, String goodsIds, Integer yunFeiId)
    {
        try
        {
            mchGoodsService.addZxGoods(vo, goodsIds, yunFeiId);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("编辑商品序号")
    @PostMapping("/editSort")
    @HttpApiMethod(apiKey = "mch.Mch.Goods.editSort")
    public Result editSort(MainVo vo, Integer id, Integer sort)
    {
        try
        {
            mchGoodsService.editSort(vo, id, sort);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("根据id获取商品信息(编辑商品)")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "goodsId", value = "商品id", required = true, dataType = "int", paramType = "form"),
    })
    @PostMapping("/getGoodsInfoById")
    @HttpApiMethod(apiKey = "mch.Mch.Goods.GetGoodsInfoById")
    public Result getGoodsInfoById(MainVo vo, int goodsId)
    {
        try
        {
            return Result.success(mchGoodsService.getGoodsInfoById(vo, goodsId, false));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("上下架商品")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "goodsIds", value = "商品id集", dataType = "string", paramType = "form")
    })
    @PostMapping("/upperAndLowerShelves")
    @HttpApiMethod(apiKey = "mch.Mch.Goods.UpperAndLowerShelves")
    public Result upperAndLowerShelves(MainVo vo, String goodsIds, Integer status)
    {
        try
        {
            boolean result = mchGoodsService.upperAndLowerShelves(vo, goodsIds, status);
            return Result.success(result);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("添加库存页面数据")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "goodsIds", value = "商品id集", dataType = "int", required = true, paramType = "form")
    })
    @PostMapping("/stockPage")
    @HttpApiMethod(apiKey = "admin.mch.goods.stockPage")
    public Result stockPage(MainVo vo, int goodsIds)
    {
        try
        {
            return Result.success(mchGoodsService.stockPage(vo, goodsIds));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("增加库存")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "stock", value = "[{id:1,addNum:1,pid:123},{id:2,addNum:1,pid:123}...] json数据", dataType = "int", required = true, paramType = "form")
    })
    @PostMapping("/addStock")
    @HttpApiMethod(apiKey = "mch.Mch.Goods.AddStock")
    public Result addStock(MainVo vo, String stock)
    {
        try
        {
            return Result.success(mchGoodsService.addStock(vo, stock));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("删除商品")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "goodsIds", value = "商品id 集", dataType = "string", required = true, paramType = "form")
    })
    @PostMapping("/delGoodsById")
    @HttpApiMethod(apiKey = "mch.Mch.Goods.DelGoodsById")
    public Result delGoodsById(MainVo vo, String goodsIds)
    {
        try
        {
            mchGoodsService.delGoodsById(vo, goodsIds);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("获取商品审核列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "classId", value = "商品类别id", dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "brandId", value = "商品品牌id", dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "goodsName", value = "商品名称", dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "examineStatus", value = "1=审核中 3=审核未通过  4=待审核", dataType = "int", paramType = "form"),
    })
    @PostMapping("/getGoodsExamineInfo")
    @HttpApiMethod(apiKey = "mch.Mch.Goods.GetGoodsExamineInfo")
    public Result getGoodsExamineInfo(MainVo vo, Integer classId, Integer brandId, Integer examineStatus, String goodsName, Integer goodsId)
    {
        try
        {
            return Result.success(mchGoodsService.getGoodsExamineInfo(vo, classId, brandId, examineStatus, goodsName, goodsId));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("提交审核/撤销审核")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pIds", value = "商品id 集", required = true, dataType = "string", paramType = "form")
    })
    @PostMapping("/submitAudit")
    @HttpApiMethod(apiKey = "mch.Mch.Goods.SubmitAudit")
    public Result submitAudit(MainVo vo, String pIds)
    {
        try
        {
            return Result.success(mchGoodsService.submitAudit(vo, pIds));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("上传图片")
    @PostMapping("/uploadImgs")
    @HttpApiMethod(apiKey = "admin.mch.goods.uploadImgs")
    public Result uploadImgs(MainVo vo, MultipartFile image)
    {
        try
        {
            return Result.success(mchGoodsService.uploadImgs(vo, image));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("PC端删除品牌")
    @PostMapping("/delBrand")
    @HttpApiMethod(apiKey = "mch.Mch.Goods.DelBrand")
    public Result delBrand(DelBrandVo vo)
    {
        try
        {
            mchGoodsService.delBrand(vo);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("删除当前类别")
    @ApiImplicitParam(name = "classId", value = "类别id", required = true, dataType = "int", paramType = "form")
    @PostMapping("/delClass")
    @HttpApiMethod(apiKey = "mch.Mch.goods.DelClass")
    public Result delClass(MainVo vo, int classId)
    {
        try
        {
            mchGoodsService.delClass(vo, classId);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "attrId", value = "属性id", dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "pid", value = "商品id", dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "type", value = "类型 0.入库 1.出库 2.预警 3.入库+出库", dataType = "int", paramType = "form")
    })
    @PostMapping("/getStockDetailInfo")
    @HttpApiMethod(apiKey = "mch.Mch.Goods.getStockDetailInfo")
    public Result getStockDetailInfo(StockInfoVo vo, Integer attrId, Integer pid, Integer type, HttpServletResponse response)
    {
        try
        {
            Map<String, Object> ret = mchGoodsService.getStockDetailInfo(vo, attrId, pid, type, response);
            return ret == null ? Result.exportFile() : Result.success(ret);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("添加库存")
    @PostMapping("/addInventory")
    @HttpApiMethod(apiKey = "mch.Mch.Goods.AddInventory")
    public Result addInventory(AddStockVo vo)
    {
        try
        {
            mchGoodsService.addInventory(vo);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("语种列表")
    @PostMapping("/getLangs")
    @HttpApiMethod(apiKey = "admin.mch.Goods.getLangs")
    public Result getLangs(MainVo vo)
    {
        try
        {
            return Result.success(publiceService.getMchLangs(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

}
