package com.laiketui.admins.admin.controller.plugin;

import com.laiketui.admins.api.admin.plugin.AdminPluginManageService;
import com.laiketui.core.domain.Result;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.DictionaryConst;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.config.AddPluginConfigVo;
import com.laiketui.domain.vo.config.AddPluginOrderConfigVo;
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
 * 插件管理
 *
 * @author Trick
 * @date 2023/3/17 10:14
 */
@Api(tags = "插件管理")
@RestController
@RequestMapping("/admin/plugin/manage")
public class AdminPluginManageController
{

    @Autowired
    private AdminPluginManageService adminPluginManageService;

    @ApiOperation("获取插件列表")
    @PostMapping("/index")
    @HttpApiMethod(urlMapping = "admin.plugin.manage.index")
    public Result index(MainVo vo) throws LaiKeAPIException
    {
        try
        {
            return Result.success(adminPluginManageService.index(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("保存插件排序")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pluginId", value = "插件ID", dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "pluginSort", value = "排序值", dataType = "int", paramType = "form"),
    })
    @PostMapping("/saveSort")
    @HttpApiMethod(urlMapping = "admin.plugin.manage.saveSort")
    public Result saveSort(MainVo vo, int pluginId, int pluginSort) throws LaiKeAPIException
    {
        try
        {
            adminPluginManageService.saveSort(vo, pluginId, pluginSort);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("插件开关")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "插件id", dataType = "int", paramType = "form"),
    })
    @PostMapping("/pluginSwitch")
    @HttpApiMethod(urlMapping = "admin.plugin.manage.pluginSwitch")
    public Result pluginSwitch(MainVo vo, int id) throws LaiKeAPIException
    {
        try
        {
            adminPluginManageService.pluginSwitch(vo, id);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("插件配置")
    @PostMapping("/addPlugin")
    @HttpApiMethod(urlMapping = "admin.plugin.manage.addPlugin")
    public Result addPlugin(AddPluginConfigVo vo) throws LaiKeAPIException
    {
        try
        {
            adminPluginManageService.addPlugin(vo);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("添加/编辑 插件订单设置")
    @PostMapping("/addPluginConfig")
    @HttpApiMethod(urlMapping = "admin.plugin.manage.addPluginConfig")
    public Result addPluginConfig(AddPluginOrderConfigVo vo) throws LaiKeAPIException
    {
        try
        {
            adminPluginManageService.addPluginConfig(vo);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("获取插件订单设置")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pluginCode", value = "插件code", dataType = "string", paramType = "form"),
    })
    @PostMapping("/getOrderConfig")
    @HttpApiMethod(urlMapping = "admin.plugin.manage.getOrderConfig")
    public Result getOrderConfig(MainVo vo, String pluginCode) throws LaiKeAPIException
    {
        try
        {
            String oType = "";
            switch (pluginCode)
            {
                case DictionaryConst.Plugin.GOGROUP:
                    oType = DictionaryConst.OrdersType.ORDERS_HEADER_PT;
                    break;
                case DictionaryConst.Plugin.DISTRIBUTION:
                    oType = DictionaryConst.OrdersType.ORDERS_HEADER_FX;
                    break;
                case DictionaryConst.Plugin.SECONDS:
                    oType = DictionaryConst.OrdersType.ORDERS_HEADER_MS;
                    break;
                case DictionaryConst.Plugin.AUCTION:
                    oType = DictionaryConst.OrdersType.ORDERS_HEADER_JP;
                    break;
                case DictionaryConst.Plugin.INTEGRAL:
                    oType = DictionaryConst.OrdersType.ORDERS_HEADER_IN;
                default:
                    break;
            }
            return Result.success(adminPluginManageService.getOrderConfig(vo, oType));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


}
