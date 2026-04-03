package com.laiketui.admins.api.admin.saas;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.admin.menu.AddMenuVo;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

/**
 * 权限管理
 *
 * @author Trick
 * @date 2021/1/28 16:16
 */
public interface AdminRoleManagerService
{
    /**
     * 获取菜单列表
     *
     * @param name -
     * @param id   -
     * @param vo   -
     * @param sid  -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/28 9:21
     */
    Map<String, Object> getMenuInfo(MainVo vo, String name, Integer id, Integer sid) throws LaiKeAPIException;


    /**
     * 获取菜单级别列表
     *
     * @param vo     -
     * @param sid    -
     * @param id     -
     * @param name   -
     * @param type   -类型 0.后台管理 1.小程序 2.app 3.微信公众号 4.PC 5.生活号 6.报表 7.支付宝小程序
     * @param isCore -是否是核心
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/28 17:12
     */
    Map<String, Object> getMenuLeveInfo(MainVo vo, String name, Integer id, Integer sid, Integer type, Integer isCore) throws LaiKeAPIException;


    /**
     * 添加/编辑菜单
     *
     * @param vo -
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/29 11:42
     */
    void addMenuInfo(AddMenuVo vo) throws LaiKeAPIException;

    /**
     * 菜单顺序移动
     *
     * @param vo     -
     * @param id     -
     * @param moveId - 被换位的id
     * @param type   -1=置顶 2=上移下移
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/6/9 19:05
     */
    void moveMenuSort(MainVo vo, int id, Integer moveId, Integer type) throws LaiKeAPIException;

    /**
     * 删除菜单
     *
     * @param vo     -
     * @param menuId -
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/29 15:04
     */
    void delMenu(MainVo vo, int menuId) throws LaiKeAPIException;


    /**
     * 绑定/解绑角色
     *
     * @param roleId   -
     * @param adminIds -
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/29 17:07
     */
    boolean bindRole(MainVo vo, int roleId, List<Integer> adminIds) throws LaiKeAPIException;

    /**
     * 根据角色获取菜单
     *
     * @param vo     -
     * @param roleId -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/8/13 10:45
     */
    Map<String, Object> getRoleMenu(MainVo vo, int roleId) throws LaiKeAPIException;

    /**
     * 获取权限菜单---路由结构 - 只获取一级
     *
     * @param vo  -
     * @param sid -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/9/23 14:25
     */
    Map<String, Object> getAsyncRoutesByRoutes(MainVo vo, Integer sid) throws LaiKeAPIException;

    /**
     * 获取权限菜单---路由结构
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/9/23 14:25
     */
    Map<String, Object> getAsyncRoutesByRoutes(MainVo vo) throws LaiKeAPIException;

    /**
     * 获取角色所有子菜单的权限按钮
     *
     * @param vo
     * @param menuId
     * @return
     * @throws LaiKeAPIException
     * @author sunH_
     * @date 2021/11/23 10:32
     */
    List<Map<String, Object>> getButton(MainVo vo, BigInteger menuId) throws LaiKeAPIException;

    /**
     * 获取角色菜单tab页面权限
     *
     * @param vo
     * @param menuId
     * @return
     * @throws LaiKeAPIException
     * @author gp
     * @date 2023/10/25
     */
    Map<String, Object> getTab(MainVo vo, Integer menuId) throws LaiKeAPIException;
}
