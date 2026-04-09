package com.laiketui.admins.admin.controller.saas;

import com.laiketui.admins.api.admin.notie.AdminPublicNoticeService;
import com.laiketui.common.api.PublicDictionaryService;
import com.laiketui.core.domain.Result;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.systems.AddNoticeVo;
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
 * 多商户控制台-公告管理
 *
 * @author Trick
 * @date 2021/2/1 10:25
 */
@Api(tags = "多商户后台-公告管理")
@RestController
@RequestMapping("/saas/sysNotice")
public class AdminSysNoticeManageController
{

    @Autowired
    private AdminPublicNoticeService adminPublicNoticeService;

    @ApiOperation("获取系统公告信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "公告id", dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "isStore", value = "获取商城公告", dataType = "int", paramType = "form"),
    })
    @PostMapping("/getSysNoticeInfo")
    @HttpApiMethod(apiKey = "saas.sysNotice.getSysNoticeInfo")
    public Result getSysNoticeInfo(MainVo vo, Integer id, Integer isStore)
    {
        try
        {
            return Result.success(adminPublicNoticeService.getPublicNoticeInfo(vo, id, isStore));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("添加/编辑系统公告")
    @PostMapping("/addSysNoticeInfo")
    @HttpApiMethod(apiKey = "saas.sysNotice.addSysNoticeInfo")
    public Result addSysNoticeInfo(AddNoticeVo vo)
    {
        try
        {
            return Result.success(adminPublicNoticeService.addSysNoticeInfo(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("删除系统公告")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "公告id", required = true, dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "storeId", value = "商城id", required = true, dataType = "int", paramType = "form")
    })
    @PostMapping("/delSysNoticeInfo")
    @HttpApiMethod(apiKey = "saas.sysNotice.delSysNoticeInfo")
    public Result delSysNoticeInfo(MainVo vo, int storeId, int id)
    {
        try
        {
            return Result.success(adminPublicNoticeService.delSysNoticeInfo(vo.getAccessId(), storeId, id));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @Autowired
    private PublicDictionaryService publicDictionaryService;

    @ApiOperation("获取公告类型")
    @PostMapping("/getNoticeTypeInfo")
    @HttpApiMethod(apiKey = "saas.sysNotice.getNoticeTypeInfo")
    public Result getNoticeTypeInfo()
    {
        try
        {
            return Result.success(publicDictionaryService.getDictionaryById("公告类型", null));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("标记系统公告-已读")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "ids", value = "公告id集", dataType = "string", paramType = "form")
    })
    @PostMapping("/readSysNotice")
    @HttpApiMethod(apiKey = "saas.sysNotice.readSysNotice")
    public Result readSysNotice(MainVo vo, String ids)
    {
        try
        {
            adminPublicNoticeService.readSysNotice(vo, ids);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

}
