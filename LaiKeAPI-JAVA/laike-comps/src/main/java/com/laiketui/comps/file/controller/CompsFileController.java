package com.laiketui.comps.file.controller;

import com.laiketui.comps.api.file.CompsFileService;
import com.laiketui.core.annotation.ParamsMapping;
import com.laiketui.core.domain.Result;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.files.FilesVo;
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

import javax.servlet.http.HttpServletRequest;


/**
 * 图片上传列表
 *
 * @author Trick
 * @date 2021/7/7 18:16
 */
@Api(tags = "文件上传")
@RestController
@RequestMapping("/comps/resources/file")
public class CompsFileController
{
    @Autowired
    private CompsFileService compsFileService;

    @ApiOperation("文件列表")
    @PostMapping("/index")
    @HttpApiMethod(apiKey = "resources.file.index")
    public Result index(FilesVo vo, HttpServletRequest request)
    {
        try
        {
            vo.setRequest(request);
            return Result.success(compsFileService.index(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("获取文件分组")
    @PostMapping("/groupList")
    @HttpApiMethod(apiKey = "resources.file.groupList")
    public Result groupList(MainVo vo)
    {
        try
        {
            return Result.success(compsFileService.groupList(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("创建/修改自定义目录")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "catalogueName", value = "目录名称", dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "id", value = "分组id", dataType = "int", paramType = "form")
    })
    @PostMapping("/createCatalogue")
    @HttpApiMethod(apiKey = "resources.file.createCatalogue")
    public Result createCatalogue(MainVo vo, String catalogueName, Integer id)
    {
        try
        {
            compsFileService.createCatalogue(vo, catalogueName, id);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("文件上传")
    @PostMapping("/uploadFiles")
    @HttpApiMethod(apiKey = "resources.file.uploadFiles")
    public Result uploadFiles(@RequestParam(value = "file",required = false) MultipartFile[] image, UploadFileVo vo)
    {
        try
        {
            vo.setImage(image);
            return Result.success(compsFileService.uploadImage(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("删除目录")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "目录id", dataType = "int", paramType = "form")
    })
    @PostMapping("/delCatalogue")
    @HttpApiMethod(apiKey = "resources.file.delCatalogue")
    public Result delCatalogue(MainVo vo, int id)
    {
        try
        {
            compsFileService.delCatalogue(vo, id);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
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
    @HttpApiMethod(apiKey = "resources.file.delFile")
    public Result delFile(MainVo vo, @ParamsMapping("id") String ids)
    {
        try
        {
            compsFileService.delFile(vo, ids);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("下载外链图片到oss上")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "imgUrl", value = "图片路径", dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "text", value = "图片说明", dataType = "string", paramType = "form"),
    })
    @PostMapping("/uploadUrlFiles")
    @HttpApiMethod(apiKey = "resources.file.uploadUrlFiles")
    public Result uploadUrlFiles(MainVo vo, String imgUrl, String text, Integer mchId)
    {
        try
        {
            compsFileService.uploadUrlFiles(vo, imgUrl, text, mchId);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

}
