package com.laiketui.admins.admin.controller;

import com.laiketui.admins.api.admin.AdminExpressService;
import com.laiketui.core.domain.Result;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.config.AddExpressSubtableVo;
import com.laiketui.domain.vo.config.GetExpressSubtableListVo;
import com.laiketui.domain.vo.goods.ExpressSaveVo;
import com.laiketui.root.annotation.HttpApiMethod;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 物流公司管理
 *
 * @author Trick
 * @date 2021/7/6 16:47
 */
@Api(tags = "后台-物流公司管理")
@RestController
@RequestMapping("/admin/express")
public class AdminExpressController
{

    @Autowired
    private AdminExpressService adminExpressService;

    @ApiOperation("物流公司列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "id", dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "keyWord", value = "关键字搜索", dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "sortType", value = "0=降序 默认升序", dataType = "int", paramType = "form")
    })
    @PostMapping("/index")
    @HttpApiMethod(apiKey = "admin.express.index")
    public Result index(MainVo vo, Integer id, String keyWord, Integer sortType)
    {
        try
        {
            return Result.success(adminExpressService.index(vo, id, keyWord, sortType));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("物流开关")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "id", dataType = "int", paramType = "form")
    })
    @PostMapping("/expressSwitch")
    @HttpApiMethod(apiKey = "admin.express.expressSwitch")
    public Result expressSwitch(MainVo vo, Integer id)
    {
        try
        {
            adminExpressService.expressSwitch(vo, id);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("添加/编辑物流")
    @PostMapping("/expressSave")
    @HttpApiMethod(apiKey = "admin.express.expressSave")
    public Result expressSave(ExpressSaveVo vo, BindingResult bindingResult)
    {
        try
        {
            adminExpressService.expressSave(vo);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("批量删除物流公司")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "ids", value = "id集", dataType = "string", paramType = "form")
    })
    @PostMapping("/expressDel")
    @HttpApiMethod(apiKey = "admin.express.expressDel")
    public Result expressDel(MainVo vo, String ids)
    {
        try
        {
            adminExpressService.expressDel(vo, ids);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("添加修改快递公司子表")
    @PostMapping("/add_logistics")
    @HttpApiMethod(apiKey = "admin.express.add_logistics")
    public Result add_logistics(AddExpressSubtableVo vo)
    {
        try
        {
            adminExpressService.addAndUpdateExpressSubtable(vo);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("获取快递公司子表列表")
    @PostMapping("/logistics_list")
    @HttpApiMethod(apiKey = "admin.express.logistics_list")
    public Result logistics_list(GetExpressSubtableListVo vo)
    {
        try
        {
            return Result.success(adminExpressService.getExpressSubtableList(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("获取快递公司子表详情")
    @PostMapping("/edit_logistics_page")
    @HttpApiMethod(apiKey = "admin.express.edit_logistics_page")
    public Result edit_logistics_page(MainVo vo, Integer id)
    {
        try
        {
            return Result.success(adminExpressService.getExpressSubtableById(vo, id));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("删除快递公司子表详情")
    @PostMapping("/del_logistics")
    @HttpApiMethod(apiKey = "admin.express.del_logistics")
    public Result del_logistics(MainVo vo, Integer id)
    {
        try
        {
            adminExpressService.delExpressSubtableById(vo, id);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("获取所有物流公司信息")
    @PostMapping("/get_logistics")
    @HttpApiMethod(apiKey = "admin.express.get_logistics")
    public Result get_logistics(MainVo vo)
    {
        try
        {
            return Result.success(adminExpressService.getExpressInfo(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("获取发货物流公司信息")
    @PostMapping("/GetLogistics")
    @HttpApiMethod(apiKey = "admin.order.GetLogistics")
    public Result GetLogistics(MainVo vo, String sNo)
    {
        try
        {
            return Result.success(adminExpressService.GetLogistics(vo, sNo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

}
