package com.laiketui.admins.mch.controller.authority;

import com.laiketui.admins.api.mch.authority.MchPcRoleService;
import com.laiketui.core.domain.Result;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.domain.vo.MainVo;
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

import javax.servlet.http.HttpServletResponse;

/**
 * 权限
 *
 * @author Trick
 * @date 2021/12/12 11:56
 */
@Api(tags = "pc店铺权限管理")
@RestController
@RequestMapping("/pc/mch/authority/")
public class MchRoleController
{

    @Autowired
    private MchPcRoleService mchPcRoleService;

    @ApiOperation("角色列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "roleId", value = "角色id", dataType = "string", paramType = "form"),
    })
    @PostMapping("/roleList")
    @HttpApiMethod(apiKey = "mch.Mch.Authority.RoleList")
    public Result roleList(MainVo vo, String roleId)
    {
        try
        {
            return Result.success(mchPcRoleService.roleList(vo, roleId));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("添加角色")
    @PostMapping("/addRole")
    @HttpApiMethod(apiKey = "mch.Mch.Authority.AddRole")
    public Result addRole(AddRoleMainVo vo)
    {
        try
        {
            mchPcRoleService.addRole(vo);
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
    @HttpApiMethod(apiKey = "mch.Mch.Authority.DelRole")
    public Result delRole(MainVo vo, String id)
    {
        try
        {
            mchPcRoleService.delRole(vo, id);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("获取所有菜单-树形结构[前端请懒加载]")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "sid", value = "菜单上级id", dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "roleId", value = "角色id", dataType = "string", paramType = "form"),
    })
    @PostMapping("/getUserAuthorityTree")
    @HttpApiMethod(apiKey = "mch.Mch.Authority.GetUserAuthorityTree")
    public Result getUserAuthorityTree(MainVo vo, String sid, String roleId)
    {
        try
        {
            return Result.success(mchPcRoleService.getUserAuthorityTree(vo, sid, roleId));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("获取用户列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "key", value = "查询关键字", dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "roleId", value = "角色id", dataType = "string", paramType = "form"),
    })
    @PostMapping("/getUserList")
    @HttpApiMethod(apiKey = "pc.mch.authority.getUserList")
    public Result getUserList(MainVo vo, String key, String roleId)
    {
        try
        {
            return Result.success(mchPcRoleService.getUserList(vo, key, roleId));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("为用户绑定角色")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userId", value = "用户id", dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "roleId", value = "角色id", dataType = "string", paramType = "form"),
    })
    @PostMapping("/bindUserAuthorityTree")
    @HttpApiMethod(apiKey = "pc.mch.authority.bindUserAuthorityTree")
    public Result bindUserAuthorityTree(MainVo vo, String userId, String roleId)
    {
        try
        {
            mchPcRoleService.bindUserAuthorityTree(vo, userId, roleId);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("解除角色绑定")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "角色用户映射id", dataType = "string", paramType = "form"),
    })
    @PostMapping("/delBindUserAuthorityTree")
    @HttpApiMethod(apiKey = "mch.Mch.Authority.DelBindUserAuthorityTree")
    public Result delBindUserAuthorityTree(MainVo vo, String id)
    {
        try
        {
            mchPcRoleService.delBindUserAuthorityTree(vo, id);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("管理员列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userId", value = "用户id", dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "roleId", value = "角色id", dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "id", value = "查询关键字", dataType = "string", paramType = "form"),
    })
    @PostMapping("/getAdminList")
    @HttpApiMethod(apiKey = "mch.Mch.Authority.GetAdminList")
    public Result getAdminList(MainVo vo, String roleId, String id)
    {
        try
        {
            return Result.success(mchPcRoleService.getAdminList(vo, roleId, id));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("管理员日志")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "zhangHao", value = "账号", dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "startDate", value = "开始时间", dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "endDate", value = "结束时间", dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "logAccountType", value = "日志账号类型", dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "logOperationType", value = "日志操作类型", dataType = "string", paramType = "form"),
    })
    @PostMapping("/getRecord")
    @HttpApiMethod(apiKey = "mch.Mch.Authority.GetRecord")
    public Result getRecord(MainVo vo, String zhangHao, String startDate, String endDate, HttpServletResponse response, String logOperationType, String logAccountType)
    {
        try
        {
            return Result.success(mchPcRoleService.getRecord(vo, zhangHao, startDate, endDate, response, logOperationType, logAccountType));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("删除管理员日志")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "ids", value = "id集", dataType = "string", paramType = "form"),
    })
    @PostMapping("/delRecord")
    @HttpApiMethod(apiKey = "mch.Mch.Authority.DelRecord")
    public Result delRecord(MainVo vo, String ids)
    {
        try
        {
            mchPcRoleService.delRecord(vo, ids);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("添加管理员")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "roleId", value = "角色id", dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "isUpdate", value = "是否添加", dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "id", value = "权限id", dataType = "string", paramType = "form"),
    })
    @PostMapping("/insertUser")
    @HttpApiMethod(apiKey = "mch.Mch.Authority.InsertUser")
    public Result insertUser(UserRegisterVo vo, String roleId, Integer isUpdate, String id)
    {
        try
        {
            return Result.success(mchPcRoleService.insertUser(vo, roleId, isUpdate, id, vo.getAccessId()));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }
}
