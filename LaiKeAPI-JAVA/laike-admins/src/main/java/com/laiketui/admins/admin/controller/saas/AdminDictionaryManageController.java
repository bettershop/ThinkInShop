package com.laiketui.admins.admin.controller.saas;

import com.laiketui.admins.api.admin.saas.AdminDataDictionaryManageService;
import com.laiketui.common.api.PublicDictionaryService;
import com.laiketui.core.annotation.ParamsMapping;
import com.laiketui.core.domain.Result;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.core.utils.tool.DataUtils;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.systems.AddDictionaryDetailVo;
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


/**
 * 多商户后台-数据字典管理
 *
 * @author Trick
 * @date 2021/2/1 13:57
 */
@Api(tags = "多商户后台-数据字典管理")
@RestController
@RequestMapping("/saas/dic")
public class AdminDictionaryManageController
{

    @Autowired
    private PublicDictionaryService publicDictionaryService;

    @ApiOperation("获取字典目录列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "字典id", dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "dicNo", value = "数据编码", dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "key", value = "数据名称", dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "value", value = "属性值", dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "status", value = "是否生效 0不生效 1生效", dataType = "int", paramType = "form")
    })
    @PostMapping("/getDictionaryInfo")
    @HttpApiMethod(apiKey = "saas.dic.getDictionaryInfo")
    public Result getDictionaryInfo(MainVo vo, Integer id, String dicNo, String key, @ParamsMapping("text") String value, Integer status)
    {
        try
        {
            return Result.success(publicDictionaryService.getDictionaryInfo(vo, id, dicNo, key, value, status));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("获取字典目录下拉")
    @PostMapping("/getDictionaryCatalogList")
    @HttpApiMethod(apiKey = "saas.dic.getDictionaryCatalogList")
    public Result getDictionaryCatalogList()
    {
        try
        {
            return Result.success(publicDictionaryService.getDictionaryCatalogList());
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("获取目录编码")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "字典id", required = true, dataType = "int", paramType = "form")
    })
    @PostMapping("/getDictionaryCode")
    @HttpApiMethod(apiKey = "saas.dic.getDictionaryCode")
    public Result getDictionaryCode(int id)
    {
        try
        {
            return Result.success(publicDictionaryService.getDictionaryCode(id));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("添加/编辑数据字典目录")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "字典id", dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "name", value = "字典目录名称", dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "isOpen", value = "是否生效", dataType = "int", paramType = "form"),
    })
    @PostMapping("/addDictionaryInfo")
    @HttpApiMethod(apiKey = "saas.dic.addDictionaryInfo")
    public Result addDictionaryInfo(MainVo vo, Integer id, String name, int isOpen)
    {
        try
        {
            return Result.success(publicDictionaryService.addDictionaryInfo(id, name, isOpen, vo.getAccessId()));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("添加/修改字典表明细")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", dataType = "string", paramType = "form")
    })
    @PostMapping("/addDictionaryDetailInfo")
    @HttpApiMethod(apiKey = "saas.dic.addDictionaryDetailInfo")
    public Result addDictionaryDetailInfo(AddDictionaryDetailVo vo)
    {
        try
        {
            publicDictionaryService.addDictionaryDetailInfo(vo);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("字典表明细开关")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "字典明细id", dataType = "int", required = true, paramType = "form")
    })
    @PostMapping("/switchDictionaryDetail")
    @HttpApiMethod(apiKey = "saas.dic.switchDictionaryDetail")
    public Result switchDictionaryDetail(MainVo vo, int id)
    {
        try
        {
            return Result.success(publicDictionaryService.switchDictionaryDetail(vo, id));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("字典表开关")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "字典目录id", dataType = "int", required = true, paramType = "form")
    })
    @PostMapping("/switchDictionary")
    @HttpApiMethod(apiKey = "saas.dic.switchDictionary")
    public Result switchDictionary(MainVo vo, int id)
    {
        try
        {
            return Result.success(publicDictionaryService.switchDictionary(vo, id));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("获取字典名称列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "字典目录id", dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "name", value = "名称id", dataType = "string", paramType = "form")
    })
    @PostMapping("/getDictionaryCatalogInfo")
    @HttpApiMethod(apiKey = "saas.dic.getDictionaryCatalogInfo")
    public Result getDictionaryCatalogInfo(MainVo vo, Integer id, String name)
    {
        try
        {
            return Result.success(publicDictionaryService.getDictionaryCatalogInfo(vo, id, name));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("删除字典目录")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "idList", value = "字典目录id集", allowMultiple = true, dataType = "int", paramType = "form")
    })
    @PostMapping("/delDictionary")
    @HttpApiMethod(apiKey = "saas.dic.delDictionary")
    public Result delDictionary(MainVo vo, Integer[] idList)
    {
        try
        {
            return Result.success(publicDictionaryService.delDictionary(vo, DataUtils.convertToList(idList)));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("删除字典明细")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "idList", value = "字典明细id集", allowMultiple = true, dataType = "int", paramType = "form")
    })
    @PostMapping("/delDictionaryDetailInfo")
    @HttpApiMethod(apiKey = "saas.dic.delDictionaryDetailInfo")
    public Result delDictionaryDetailInfo(MainVo vo, Integer[] idList)
    {
        try
        {
            return Result.success(publicDictionaryService.delDictionaryDetailInfo(vo, DataUtils.convertToList(idList)));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @Autowired
    private AdminDataDictionaryManageService dictionaryManageService;


    @ApiOperation("获取商品属性信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "id", dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "sid", value = "上级id", dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "dataCode", value = "属性代码", dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "dataName", value = "属性名称", dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "key", value = "属性名称(用于精确查询)", dataType = "string", paramType = "form")
    })
    @PostMapping("/getSkuInfo")
    @HttpApiMethod(apiKey = "saas.dic.getSkuInfo")
    public Result getSkuInfo(MainVo vo, Integer id, Integer sid, String dataCode, String dataName, String key, HttpServletResponse response)
    {
        try
        {
            return Result.success(dictionaryManageService.getSkuInfo(vo, id, sid, dataCode, dataName, key, response));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("获取属性名称下拉")
    @PostMapping("/getSkuAttributeList")
    @HttpApiMethod(apiKey = "saas.dic.getSkuAttributeList")
    public Result getSkuAttributeList(MainVo vo)
    {
        try
        {
            return Result.success(dictionaryManageService.getSkuAttributeList(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("获取商品属性/属性值列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "keyword", value = "关键字", dataType = "string", paramType = "form")
    })
    @PostMapping("/getSkuList")
    @HttpApiMethod(apiKey = "saas.dic.getSkuList")
    public Result getSkuList(MainVo vo, String keyword, String strArr)
    {
        try
        {
            return Result.success(dictionaryManageService.getSkuList(vo, keyword, strArr));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("商品属性生效开关")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "id", required = true, dataType = "int", paramType = "form")
    })
    @PostMapping("/setSkuSwitch")
    @HttpApiMethod(apiKey = "saas.dic.setSkuSwitch")
    public Result setSkuSwitch(MainVo vo, int id)
    {
        try
        {
            return Result.success(dictionaryManageService.setSkuSwitch(vo.getAccessId(), id));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("添加/修改【商品属性名称】")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "id", dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "skuName", value = "属性名称", required = true, dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "isOpen", value = "是否生效", required = true, dataType = "int", paramType = "form"),
    })
    @PostMapping("/addSkuName")
    @HttpApiMethod(apiKey = "saas.dic.addSkuName")
    public Result addSkuName(MainVo vo, Integer id, String skuName, int isOpen)
    {
        try
        {
            return Result.success(dictionaryManageService.addSkuName(id, skuName, isOpen, vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("添加/修改【商品属性】")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "sid", value = "属性名id", required = true, dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "attributeList", value = "属性值集", required = true, dataType = "string", allowMultiple = true, paramType = "form"),
            @ApiImplicitParam(name = "type", value = "0=添加 1=修改", required = true, dataType = "int", paramType = "form")
    })
    @PostMapping("/addSku")
    @HttpApiMethod(apiKey = "saas.dic.addSku")
    public Result addSku(MainVo vo, int sid, String[] attributeList, int type)
    {
        try
        {
            dictionaryManageService.addSku(sid, DataUtils.convertToList(attributeList), vo, type);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("批量删除商品属性")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "idList", value = "属性值集", required = true, dataType = "int", allowMultiple = true, paramType = "form")
    })
    @PostMapping("/delSku")
    @HttpApiMethod(apiKey = "saas.dic.delSku")
    public Result delSku(MainVo vo, Integer[] idList)
    {
        try
        {
            dictionaryManageService.delSku(vo.getAccessId(), DataUtils.convertToList(idList));
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

}
