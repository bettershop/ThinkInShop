package com.laiketui.admins.admin.controller.mch;

import com.laiketui.admins.api.admin.mch.AdminMchAuthorityService;
import com.laiketui.core.domain.Result;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.admin.menu.AddMenuMainVo;
import com.laiketui.domain.vo.mch.pc.AddRoleMainVo;
import com.laiketui.domain.vo.user.UserRegisterVo;
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
 * @Author: sunH_
 * @Date: Create in 17:21 2023/7/3
 */
@Api(tags = "后台-pc店铺权限")
@RestController
@RequestMapping("/admin/pc/mchAuthority")
public class AdminMchAuthorityController
{

    @Autowired
    private AdminMchAuthorityService adminMchAuthorityService;

    @ApiOperation("获取菜单列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "name", value = "菜单名称/id", dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "id", value = "菜单id", dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "sid", value = "上级id", dataType = "string", paramType = "form"),
    })
    @PostMapping("/getMenuList")
    @HttpApiMethod(apiKey = "admin.pc.mchAuthority.getMenuList")
    public Result getMenuList(MainVo vo, String name, String id, String sid)
    {
        try
        {
            return Result.success(adminMchAuthorityService.getMenuList(vo, name, id, sid));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("添加菜单")
    @PostMapping("/addMenu")
    @HttpApiMethod(apiKey = "admin.pc.mchAuthority.addMenu")
    public Result addMenu(AddMenuMainVo vo)
    {
        try
        {
            adminMchAuthorityService.addMenu(vo);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("删除菜单")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "菜单id", dataType = "string", paramType = "form"),
    })
    @PostMapping("/delMenu")
    @HttpApiMethod(apiKey = "admin.pc.mchAuthority.delMenu")
    public Result delMenu(MainVo vo, String id)
    {
        try
        {
            adminMchAuthorityService.delMenu(vo, id);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("获取不同角色所有授权菜单-树形结构[前端请懒加载]")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "sid", value = "菜单上级id", dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "roleId", value = "角色id", dataType = "string", paramType = "form"),
    })
    @PostMapping("/getUserAuthorityTree")
    @HttpApiMethod(apiKey = "admin.pc.mchAuthority.getUserAuthorityTree")
    public Result getUserAuthorityTree(MainVo vo, String sid, String roleId)
    {
        try
        {
            return Result.success(adminMchAuthorityService.getUserAuthorityTree(vo, sid, roleId));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("角色列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "roleId", value = "角色id", dataType = "string", paramType = "form"),
    })
    @PostMapping("/roleList")
    @HttpApiMethod(apiKey = "admin.pc.mchAuthority.roleList")
    public Result roleList(MainVo vo, String roleId, Integer mchId)
    {
        try
        {
            return Result.success(adminMchAuthorityService.roleList(vo, roleId, mchId));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("添加角色")
    @PostMapping("/addRole")
    @HttpApiMethod(apiKey = "admin.pc.mchAuthority.addRole")
    public Result addRole(AddRoleMainVo vo)
    {
        try
        {
            adminMchAuthorityService.addRole(vo);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("删除角色")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "角色id", dataType = "string", paramType = "form"),
    })
    @PostMapping("/delRole")
    @HttpApiMethod(apiKey = "admin.pc.mchAuthority.delRole")
    public Result delRole(MainVo vo, String id)
    {
        try
        {
            adminMchAuthorityService.delRole(vo, id);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("添加pc店铺管理员账号")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "roleId", value = "角色id", dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "isUpdate", value = "是否添加", dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "id", value = "权限id", dataType = "string", paramType = "form"),
    })
    @PostMapping("/insertUser")
    @HttpApiMethod(apiKey = "admin.pc.mchAuthority.insertUser")
    public Result insertUser(UserRegisterVo vo, String roleId, Integer isUpdate, String id)
    {
        try
        {
            return Result.success(adminMchAuthorityService.insertUser(vo, roleId, isUpdate, id));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("获取管理员列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userId", value = "用户id", dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "roleId", value = "角色id", dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "id", value = "查询关键字", dataType = "string", paramType = "form"),
    })
    @PostMapping("/getAdminList")
    @HttpApiMethod(apiKey = "admin.pc.mchAuthority.getAdminList")
    public Result getAdminList(MainVo vo, String roleId, String id, Integer mchId)
    {
        try
        {
            return Result.success(adminMchAuthorityService.getAdminList(vo, roleId, id, mchId));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("解除管理员权限")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "角色用户映射id", dataType = "string", paramType = "form"),
    })
    @PostMapping("/delBindUserAuthorityTree")
    @HttpApiMethod(apiKey = "admin.pc.mchAuthority.delBindUserAuthorityTree")
    public Result delBindUserAuthorityTree(MainVo vo, String id)
    {
        try
        {
            adminMchAuthorityService.delBindUserAuthorityTree(vo, id);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


}
