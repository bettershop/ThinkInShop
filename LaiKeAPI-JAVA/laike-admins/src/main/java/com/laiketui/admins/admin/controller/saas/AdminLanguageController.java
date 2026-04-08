package com.laiketui.admins.admin.controller.saas;

import com.laiketui.admins.api.admin.saas.AdminLanguageService;
import com.laiketui.core.domain.Result;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.domain.LangModel;
import com.laiketui.domain.vo.saas.LanguageVo;
import com.laiketui.root.annotation.HttpApiMethod;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@Api(tags = "多商户后台-语种管理")
@RestController
@RequestMapping("/saas/lang")
public class AdminLanguageController
{
    @Autowired
    private AdminLanguageService adminLanguageService;


    @ApiOperation("语种列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "主键id", dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "name", value = "语种名称", dataType = "string", paramType = "form"),
    })
    @PostMapping("/index")
    @HttpApiMethod(apiKey = "admin.lang.index")
    public Result index(LanguageVo vo)
    {
        try
        {
            return Result.success(adminLanguageService.index(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("添加/编辑语种标签")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "主键id", dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "name", value = "商品名称", required = true, dataType = "string", paramType = "form"),
    })
    @PostMapping("/addOrEditLang")
    @HttpApiMethod(apiKey = "admin.lang.addOrEditLang")
    public Result addOrEditLang(LanguageVo vo)
    {
        try
        {
            LangModel langModel = new LangModel();
            BeanUtils.copyProperties(vo, langModel);
            langModel.setRecycle(1);
            langModel.setOp_time(new Date());
            adminLanguageService.addLanguage(langModel);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("删除商品标签")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "主键id", required = true, dataType = "int", paramType = "form")
    })
    @PostMapping("/delLang")
    @HttpApiMethod(apiKey = "admin.lang.delLang")
    public Result delGoodsLabel(Integer id)
    {
        try
        {
            adminLanguageService.delLanguage(id);

            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }
}
