package com.laiketui.admins.admin.controller;

import com.laiketui.admins.api.admin.role.AdminRoleService;
import com.laiketui.core.domain.Result;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.domain.mch.RoleModel;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.admin.role.AddAdminVo;
import com.laiketui.domain.vo.admin.role.LoggerAdminVo;
import com.laiketui.domain.vo.role.AddRoleVo;
import com.laiketui.root.annotation.HttpApiMethod;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * 权限管理
 *
 * @author Trick
 * @date 2021/1/13 12:14
 */
@Api(tags = "后台-权限管理")
@RestController
@RequestMapping("/admin/role")
public class AdminRoleManagerController
{

    @Autowired
    private AdminRoleService adminRoleService;

    @ApiOperation("获取管理员列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "管理员id", dataType = "int", paramType = "form")
    })
    @PostMapping("/getAdminInfo")
    @HttpApiMethod(apiKey = "admin.role.getAdminInfo")
    public Result getAdminInfo(MainVo vo, Integer id)
    {
        try
        {
            return Result.success(adminRoleService.getAdminInfo(vo, id));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("获取管理员角色信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "管理员id", dataType = "int", paramType = "form")
    })
    @PostMapping("/getRoleListInfo")
    @HttpApiMethod(apiKey = "admin.role.getRoleListInfo")
    public Result getRoleListInfo(MainVo vo, Integer id)
    {
        try
        {
            return Result.success(adminRoleService.getRoleListInfo(vo, RoleModel.STATUS_ROLE, id));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("添加/修改管理员信息")
    @PostMapping("/addAdminInfo")
    @HttpApiMethod(apiKey = "admin.role.addAdminInfo")
    public Result addAdminInfo(AddAdminVo vo)
    {
        try
        {
            adminRoleService.addAdminInfo(vo);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("删除管理员信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "管理员id", required = true, dataType = "int", paramType = "form")
    })
    @PostMapping("/delAdminInfo")
    @HttpApiMethod(apiKey = "admin.role.delAdminInfo")
    public Result delAdminInfo(MainVo vo, int id)
    {
        try
        {
            adminRoleService.delAdminInfo(vo, id);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("禁用/启用管理员")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "管理员id", required = true, dataType = "int", paramType = "form")
    })
    @PostMapping("/stopAdmin")
    @HttpApiMethod(apiKey = "admin.role.stopAdmin")
    public Result stopAdmin(MainVo vo, int id)
    {
        try
        {
            adminRoleService.stopAdmin(vo, id);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("获取管理员日志列表")
    @PostMapping("/getAdminLoggerInfo")
    @HttpApiMethod(apiKey = "admin.role.getAdminLoggerInfo")
    public Result getAdminLoggerInfo(LoggerAdminVo vo, HttpServletResponse response)
    {
        try
        {
            Map<String, Object> ret = adminRoleService.getAdminLoggerInfo(vo, response);
            return ret == null ? Result.exportFile() : Result.success(ret);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("批量删除管理员日志")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "ids", value = "管理员id集", required = true, dataType = "string", paramType = "form")
    })
    @PostMapping("/delAdminLogger")
    @HttpApiMethod(apiKey = "admin.role.delAdminLogger")
    public Result delAdminLogger(MainVo vo, String ids)
    {
        try
        {
            return Result.success(adminRoleService.delAdminLogger(vo, ids));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    /**
     * 平台权限管理角色列表-添加/修改角色
     */
    @ApiOperation("获取权限列表【平台权限控制】")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "权限id", dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "lang_code", value = "语种", dataType = "String", paramType = "form")
    })
    @PostMapping("/getPlatformUserRoleInfo")
    @HttpApiMethod(apiKey = "admin.role.platform.getUserRoleInfo")
    public Result getPlatformUserRoleInfo(MainVo vo, Integer id,String lang_code)
    {
        try
        {
            return Result.success(adminRoleService.getUserRoleInfo(vo, id, true,lang_code));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    /**
     * 添加商户绑定角色
     * 角色管理绑定权限
     */
    @ApiOperation("获取权限列表【商城权限控制】")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "权限id", dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "lang_code", value = "语种", dataType = "String", paramType = "form")
    })
    @PostMapping("/getUserRoleInfo")
    @HttpApiMethod(apiKey = "admin.role.getUserRoleInfo")
    public Result getUserRoleInfo(MainVo vo, Integer id,String lang_code)
    {
        try
        {
            return Result.success(adminRoleService.getUserRoleInfo(vo, id, false,lang_code));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("获取权限下拉")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "权限id", dataType = "int", paramType = "form")
    })
    @PostMapping("/getUserRoles")
    @HttpApiMethod(apiKey = "admin.role.getUserRoles")
    public Result getUserRoles(MainVo vo, Integer id)
    {
        try
        {
            return Result.success(adminRoleService.getUserRoles(vo, id));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("获取权限菜单列表")
    @PostMapping("/getUserRoleMenuInfo")
    @HttpApiMethod(apiKey = "admin.role.getUserRoleMenuInfo")
    public Result getUserRoleMenuInfo(MainVo vo)
    {
        try
        {
            return Result.success(adminRoleService.getUserRoleMenuInfo(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("添加/修改角色")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "权限id", dataType = "int", paramType = "form")
    })
    @PostMapping("/addUserRoleMenu")
    @HttpApiMethod(apiKey = "admin.role.addUserRoleMenu")
    public Result addUserRoleMenu(AddRoleVo vo, Integer id)
    {
        try
        {
            vo.setStatus(RoleModel.STATUS_ROLE);
            adminRoleService.addUserRoleMenu(vo, id);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("删除角色")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "权限id", dataType = "int", paramType = "form")
    })
    @PostMapping("/delUserRoleMenu")
    @HttpApiMethod(apiKey = "admin.role.delUserRoleMenu")
    public Result delUserRoleMenu(AddRoleVo vo, Integer id)
    {
        try
        {
            adminRoleService.delUserRoleMenu(vo, id);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }
}
