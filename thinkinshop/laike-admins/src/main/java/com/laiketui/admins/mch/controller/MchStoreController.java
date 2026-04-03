package com.laiketui.admins.mch.controller;

import com.laiketui.admins.api.mch.MchStoreService;
import com.laiketui.core.annotation.ParamsMapping;
import com.laiketui.core.domain.Result;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.mch.AddStoreVo;
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
 * 门店管理
 *
 * @author Trick
 * @date 2021/5/27 16:24
 */
@Api(tags = "门店管理")
@RestController
@RequestMapping({"/admin/mch/store/"})
public class MchStoreController
{

    @Autowired
    private MchStoreService mchStoreService;

    @ApiOperation("门店列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "mchStoreId", value = "门店id", dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "mchName", value = "门店名称", dataType = "string", paramType = "form"),
    })
    @PostMapping("/index")
    @HttpApiMethod(apiKey = "mch.Mch.Store.Index")
    public Result index(MainVo vo, Integer mchStoreId, String mchName)
    {
        try
        {
            return Result.success(mchStoreService.index(vo, mchStoreId, mchName));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("添加/编辑我的门店")
    @PostMapping("/addStore")
    @HttpApiMethod(apiKey = "mch.Mch.Store.AddStore")
    public Result addStore(AddStoreVo vo)
    {
        try
        {
            mchStoreService.addStore(vo);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("设置默认门店")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "mchStoreId", value = "门店id", required = true, dataType = "int", paramType = "form"),
    })
    @PostMapping("/setDefaultStore")
    @HttpApiMethod(apiKey = "mch.Mch.Store.SetDefaultStore")
    public Result setDefaultStore(MainVo vo, Integer mchStoreId)
    {
        try
        {
            mchStoreService.setDefaultStore(vo, mchStoreId);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
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
    @PostMapping("/editStorePage")
    @HttpApiMethod(apiKey = "admin.mch.store.editStorePage")
    public Result editStorePage(MainVo vo, @ParamsMapping("shop_id") int shopId, int id)
    {
        try
        {
            return Result.success(mchStoreService.editStorePage(vo, shopId, id));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("删除我的门店")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "门店id集", required = true, dataType = "int", paramType = "form")
    })
    @PostMapping("/delStore")
    @HttpApiMethod(apiKey = "mch.Mch.Store.DelStore")
    public Result delStore(MainVo vo, String id)
    {
        try
        {
            mchStoreService.delStore(vo, id);
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
    @HttpApiMethod(apiKey = "mch.Mch.Store.AdminList")
    public Result getAdminList(MainVo vo, Integer mch_store_id, String account_number)
    {
        try
        {
            return Result.success(mchStoreService.getAdminList(vo, mch_store_id, account_number));
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
    @HttpApiMethod(apiKey = "mch.Mch.Store.AddAdmin")
    public Result addAdmin(MainVo vo, Integer mch_store_id, String account_number, String password, Integer id)
    {
        try
        {
            mchStoreService.addAdmin(vo, mch_store_id, account_number, password, id);
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
    @HttpApiMethod(apiKey = "mch.Mch.Store.DelAdmin")
    public Result delAdmin(MainVo vo, Integer mch_store_id, Integer id)
    {
        try
        {
            mchStoreService.delAdmin(vo, mch_store_id, id);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


}
