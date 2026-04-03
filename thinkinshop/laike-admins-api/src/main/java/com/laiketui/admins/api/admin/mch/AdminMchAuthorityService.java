package com.laiketui.admins.api.admin.mch;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.admin.menu.AddMenuMainVo;
import com.laiketui.domain.vo.mch.pc.AddRoleMainVo;
import com.laiketui.domain.vo.user.UserRegisterVo;

import java.util.Map;

/**
 * @Author: sunH_
 * @Date: Create in 16:45 2023/7/3
 */
public interface AdminMchAuthorityService
{
    /**
     * 获取菜单列表
     *
     * @param vo   -
     * @param sid  -
     * @param id   -
     * @param name -
     * @return Map
     * @throws LaiKeAPIException -
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

    /**
     * 获取用户所有的菜单权限-树形结构[前端请懒加载]
     *
     * @param vo     -
     * @param sid    - 菜单上级id
     * @param roleId - 角色id
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/12/12 12:05
     */
    Map<String, Object> getUserAuthorityTree(MainVo vo, String sid, String roleId) throws LaiKeAPIException;

    /**
     * 角色列表
     *
     * @param vo     -
     * @param roleId -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/12/12 12:00
     */
    Map<String, Object> roleList(MainVo vo, String roleId, Integer mchId) throws LaiKeAPIException;

    /**
     * 添加角色
     *
     * @param vo -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/12/17 15:40
     */
    void addRole(AddRoleMainVo vo) throws LaiKeAPIException;

    /**
     * 删除角色
     *
     * @param vo -
     * @param id -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/12/17 15:43
     */
    void delRole(MainVo vo, String id) throws LaiKeAPIException;

    /**
     * 添加店铺管理员账号
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/9/23 9:48
     */
    Map<String, Object> insertUser(UserRegisterVo vo, String roleId, Integer isUpdate, String id) throws LaiKeAPIException;

    /**
     * 验证手机号/账号是否已被注册
     *
     * @param stroeId  -
     * @param zhanghao -
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/9/23 9:49
     */
    boolean isRegister(int stroeId, String zhanghao) throws LaiKeAPIException;

    /**
     * 获取管理员列表
     *
     * @param vo     -
     * @param roleId -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/12/21 17:53
     */
    Map<String, Object> getAdminList(MainVo vo, String roleId, String id, Integer mchId) throws LaiKeAPIException;

    /**
     * 解除管理员权限
     *
     * @param vo -
     * @param id -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/12/27 19:42
     */
    void delBindUserAuthorityTree(MainVo vo, String id) throws LaiKeAPIException;
}
