package com.laiketui.admins.mch.controller;

import com.laiketui.admins.api.mch.MchExpressService;
import com.laiketui.core.domain.Result;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.config.AddExpressSubtableVo;
import com.laiketui.domain.vo.config.GetExpressSubtableListVo;
import com.laiketui.root.annotation.HttpApiMethod;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 物流管理
 */
@Api(tags = "物流管理")
@RestController
@RequestMapping("/admin/mch/express/")
public class MchExpressController
{

    @Autowired
    private MchExpressService mchExpressService;

    @ApiOperation("添加修改快递公司子表")
    @PostMapping("/add_logistics")
    @HttpApiMethod(apiKey = "mch.Mch.Express.add_logistics")
    public Result add_logistics(AddExpressSubtableVo vo)
    {
        try
        {
            mchExpressService.addAndUpdateExpressSubtable(vo);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("获取快递公司子表列表")
    @PostMapping("/logistics_list")
    @HttpApiMethod(apiKey = "mch.Mch.Express.logistics_list")
    public Result logistics_list(GetExpressSubtableListVo vo)
    {
        try
        {
            return Result.success(mchExpressService.getExpressSubtableList(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("获取快递公司子表详情")
    @PostMapping("/edit_logistics_page")
    @HttpApiMethod(apiKey = "mch.Mch.Express.edit_logistics_page")
    public Result edit_logistics_page(MainVo vo, Integer id)
    {
        try
        {
            return Result.success(mchExpressService.getExpressSubtableById(vo, id));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("删除快递公司子表详情")
    @PostMapping("/del_logistics")
    @HttpApiMethod(apiKey = "mch.Mch.Express.del_logistics")
    public Result del_logistics(MainVo vo, Integer id)
    {
        try
        {
            mchExpressService.delExpressSubtableById(vo, id);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("获取所有物流公司信息")
    @PostMapping("/get_logistics")
    @HttpApiMethod(apiKey = "mch.Mch.Express.get_logistics")
    public Result get_logistics(MainVo vo)
    {
        try
        {
            return Result.success(mchExpressService.getExpressInfo(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("获取发货物流公司信息")
    @PostMapping("/GetLogistics")
    @HttpApiMethod(apiKey = "mch.Mch.Order.GetLogistics")
    public Result GetLogistics(MainVo vo, String sNo)
    {
        try
        {
            return Result.success(mchExpressService.GetLogistics(vo, sNo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

}
