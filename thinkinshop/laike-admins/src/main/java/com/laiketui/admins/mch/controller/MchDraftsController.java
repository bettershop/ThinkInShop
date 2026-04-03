package com.laiketui.admins.mch.controller;

import com.laiketui.common.api.DraftsModelService;
import com.laiketui.core.domain.Result;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.root.annotation.HttpApiMethod;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @author 草稿箱管理
 * @version 1.0
 * @description: liuao
 * @date 2025/1/16 15:42
 */
@Api(tags = "草稿箱管理")
@RestController
@RequestMapping("/admin/mch/drafts")
public class MchDraftsController
{
    @Autowired
    private DraftsModelService draftsModelService;

    @ApiOperation("/新增草稿箱")
    @PostMapping("/add")
    @HttpApiMethod(apiKey = "mch.Mch.Drafts.add")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "text",value = "草稿数据 json",dataType = "String",required = true),
            @ApiImplicitParam(name = "id",value = "id",dataType = "Integer",required = false)
    })
    public Result add(MainVo vo,String text, Integer id){
        try
        {
            draftsModelService.add(vo,3,text,id);
            return Result.success();
        } catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("/草稿箱列表")
    @PostMapping("/list")
    @HttpApiMethod(apiKey = "mch.Mch.Drafts.get_list")
    public Result list(MainVo vo){
        try
        {
            Map<String, Object> list = draftsModelService.list(vo, 3);
            return Result.success(list);
        }catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }

    }


    @ApiOperation("/草稿箱删除")
    @PostMapping("/del")
    @HttpApiMethod(apiKey = "mch.Mch.Drafts.del")
    @ApiImplicitParam(name = "id",value = "id",dataType = "String",required = true)
    public Result del(String id){
        try
        {
            draftsModelService.del(id);
            return Result.success();
        }catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("/获取草稿箱内容")
    @PostMapping("/getTextById")
    @HttpApiMethod(apiKey = "mch.Mch.Drafts.edit_page")
    @ApiImplicitParam(name = "id",value = "id",dataType = "Integer",required = true)
    public Result del(Integer id){
        try
        {
            Map<String, Object> map = draftsModelService.getTextById(id);
            return Result.success(map);
        }catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }
}
