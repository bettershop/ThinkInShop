package com.laiketui.admins.api.mch.authority;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.admin.menu.AddMenuMainVo;

import java.util.Map;

/**
 * 权限菜单接口
 *
 * @author Trick
 * @date 2021/12/19 10:30
 */
public interface MchMenuService
{

    /**
     * 获取菜单列表
     *
     * @param vo   -
     * @param sid  -
     * @param id   -
     * @param name -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/12/19 10:33
     */
    Map<String, Object> getMenuList(MainVo vo, String name, String id, String sid) throws LaiKeAPIException;

    /**
     * 添加菜单
     *
     * @param vo -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/12/19 10:35
     */
    void addMenu(AddMenuMainVo vo) throws LaiKeAPIException;

    /**
     * 删除菜单
     *
     * @param vo -
     * @param id -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/12/19 10:36
     */
    void delMenu(MainVo vo, String id) throws LaiKeAPIException;


}
