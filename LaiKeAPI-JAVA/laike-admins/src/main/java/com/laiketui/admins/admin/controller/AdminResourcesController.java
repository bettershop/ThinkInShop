package com.laiketui.admins.admin.controller;

import com.laiketui.admins.api.admin.AdminResourcesService;
import com.laiketui.core.annotation.ParamsMapping;
import com.laiketui.core.domain.Result;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.files.UploadFileVo;
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

/**
 * 资源管理
 *
 * @author Trick
 * @date 2021/7/21 16:38
 */
@Api(tags = "后台-资源管理")
@RestController
@RequestMapping("/admin/resources")
public class AdminResourcesController
{

    @Autowired
    private AdminResourcesService adminResourcesService;


    @ApiOperation("获取资源列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "imageName", value = "图片名称", dataType = "String", paramType = "form"),
            @ApiImplicitParam(name = "startTime", value = "开始时间", dataType = "String", paramType = "form"),
            @ApiImplicitParam(name = "endTime", value = "结束时间", dataType = "String", paramType = "form")
    })
    @PostMapping("/index")
    @HttpApiMethod(apiKey = "admin.resources.index")
    public Result index(MainVo vo, String imageName, String startTime, String endTime, String groupId, Integer type)
    {
        try
        {
            return Result.success(adminResourcesService.index(vo, imageName, startTime, endTime, groupId, type));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("文件上传")
    @PostMapping("/uploadFiles")
    @HttpApiMethod(apiKey = "admin.resources.uploadFiles")
    public Result uploadFiles(@RequestParam("file") MultipartFile[] image,UploadFileVo vo)
    {
        try
        {
            vo.setImage(image);
            return Result.success(adminResourcesService.uploadImage(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("删除文件")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "ids", value = "图片id集 用','分割", dataType = "string", paramType = "form")
    })
    @PostMapping("/delFile")
    @HttpApiMethod(apiKey = "admin.resources.delFile")
    public Result delFile(MainVo vo, @ParamsMapping("id") String ids)
    {
        try
        {
            adminResourcesService.delFile(vo, ids);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("添加/修改分类")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "imgIds", value = "图片id集", dataType = "string", paramType = "form"),
    })
    @RequestMapping("/addGroup")
    @HttpApiMethod(apiKey = "admin.resources.addGroup")
    public Result addGroup(MainVo vo, String catalogueName, Integer id, Integer type)
    {
        try
        {
            adminResourcesService.addGroup(vo, catalogueName, id, type);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("分类列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "imgIds", value = "图片id集", dataType = "string", paramType = "form"),
    })
    @RequestMapping("/groupList")
    @HttpApiMethod(apiKey = "admin.resources.groupList")
    public Result groupList(MainVo vo, Integer type)
    {
        try
        {
            return Result.success(adminResourcesService.groupList(vo, type));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("删除分类")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "目录id", dataType = "int", paramType = "form")
    })
    @PostMapping("/delCatalogue")
    @HttpApiMethod(apiKey = "admin.resources.delCatalogue")
    public Result delCatalogue(MainVo vo, int id)
    {
        try
        {
            adminResourcesService.delCatalogue(vo, id);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("修改分类展示状态")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "目录id", dataType = "int", paramType = "form")
    })
    @PostMapping("/updateCatalogueShow")
    @HttpApiMethod(apiKey = "admin.resources.updateCatalogueShow")
    public Result updateCatalogueShow(MainVo vo, String id, Integer type)
    {
        try
        {
            adminResourcesService.updateCatalogueShow(vo, id, type);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("批量修改分类")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "目录id", dataType = "int", paramType = "form")
    })
    @PostMapping("/updateCatalogueByImageIds")
    @HttpApiMethod(apiKey = "admin.resources.updateCatalogueByImageIds")
    public Result updateCatalogueByImageIds(MainVo vo, String imageIds, Integer catalogueId)
    {
        try
        {
            adminResourcesService.updateCatalogueByImageIds(vo, imageIds, catalogueId);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("批量下载")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "imgIds", value = "图片id集", dataType = "string", paramType = "form"),
    })
    @RequestMapping("/downForZip")
    @HttpApiMethod(apiKey = "admin.resources.downForZip")
    public Result downForZip(MainVo vo, HttpServletResponse response, String imgIds)
    {
        try
        {
            adminResourcesService.downForZip(vo, response, imgIds);
            return Result.exportFile();
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("批量删除")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "imgIds", value = "图片id集", dataType = "string", paramType = "form"),
    })
    @PostMapping("/del")
    @HttpApiMethod(apiKey = "admin.resources.del")
    public Result del(MainVo vo, String imgIds)
    {
        try
        {
            adminResourcesService.del(vo, imgIds);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

}
