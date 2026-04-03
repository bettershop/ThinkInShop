package com.laiketui.admins.mch.controller.authority;

import com.laiketui.admins.api.mch.authority.MchMenuService;
import com.laiketui.common.api.PublicSortService;
import com.laiketui.core.domain.Result;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.admin.menu.AddMenuMainVo;
import com.laiketui.root.annotation.HttpApiMethod;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 权限菜单
 *
 * @author Trick
 * @date 2021/12/19 10:29
 */
@Api(tags = "pc店铺权限菜单管理")
@RestController
@RequestMapping("/pc/mch/menu")
public class MchMenuController
{

    @Autowired
    private MchMenuService mchMenuService;

    @Autowired
    @Qualifier("menuServiceImpl")
    private PublicSortService publicSortService;

    @ApiOperation("获取菜单列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "name", value = "菜单名称/id", dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "id", value = "菜单id", dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "sid", value = "上级id", dataType = "string", paramType = "form"),
    })
    @PostMapping("/getMenuList")
    @HttpApiMethod(apiKey = "mch.Mch.Menu.GetMenuList")
    public Result getMenuList(MainVo vo, String name, String id, String sid)
    {
        try
        {
            return Result.success(mchMenuService.getMenuList(vo, name, id, sid));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("添加菜单")
    @PostMapping("/addMenu")
    @HttpApiMethod(apiKey = "mch.Mch.Menu.AddMenu")
    public Result addMenu(AddMenuMainVo vo)
    {
        try
        {
            mchMenuService.addMenu(vo);
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
    @HttpApiMethod(apiKey = "mch.Mch.Menu.DelMenu")
    public Result delMenu(MainVo vo, String id)
    {
        try
        {
            mchMenuService.delMenu(vo, id);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("上下移动")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "moveId", value = "菜单id 换位1", dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "moveId1", value = "菜单id 换位2", dataType = "string", paramType = "form"),
    })
    @PostMapping("/sortMove")
    @HttpApiMethod(apiKey = "mch.Mch.Menu.SortMove")
    public Result sortMove(MainVo vo, String moveId, String moveId1)
    {
        try
        {
            publicSortService.sortMove(vo, moveId, moveId1);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("置顶")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "菜单id", dataType = "string", paramType = "form"),
    })
    @PostMapping("/top")
    @HttpApiMethod(apiKey = "mch.Mch.Menu.Top")
    public Result top(MainVo vo, String id)
    {
        try
        {
            publicSortService.top(vo, id);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }
}
