package com.laiketui.common.api.admin;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.role.CoreMenuModel;

import java.util.List;
import java.util.Map;

/**
 * 关于菜单、权限
 *
 * @author Trick
 * @date 2021/1/29 9:42
 */
public interface PublicRoleService
{


    /**
     * 更具菜单id获取树形结构
     *
     * @param roleId - 可选 角色id,用于选中
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/29 9:44
     */
    List<Map<String, Object>> getMenuTreeList(Integer roleId) throws LaiKeAPIException;


    /**
     * 更具权限id获取树形结构
     *
     * @param roleId  - 可选 前端传的角色id,用于选中
     * @param storeId - 商城id
     * @param adminId - 当前登录账号
     * @param isPt    - 是否平台
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021-06-11 15:56:42
     */
    List<Map<String, Object>> getRoleTreeList(int storeId, Integer roleId, Integer adminId, boolean isPt,String lang_code) throws LaiKeAPIException;


    /**
     * 获取当前权限id获取上级菜单信息
     *
     * @param fatherId -
     * @param list     -
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/29 11:22
     */
    void getRoleFatherById(int fatherId, List<CoreMenuModel> list) throws LaiKeAPIException;

    /**
     * 获取所有商户
     *
     * @param roleId -
     * @param name   -
     * @param isBind - 是否绑定
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/29 16:38
     */
    Map<String, Object> getBindListInfo(int roleId, String name, int isBind) throws LaiKeAPIException;


    /**
     * 验证商户是否绑定角色
     *
     * @param adminIds -
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/29 17:11
     */
    boolean verificationBind(List<Integer> adminIds) throws LaiKeAPIException;

    /**
     * 删除权限菜单[暂未实现]
     * 该方法会自动递归删除子集权限
     *
     * @param menuIds -
     * @param level   - 菜单级别
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022/12/8 11:17
     */
    void delMenuId(List<Integer> menuIds, int level) throws LaiKeAPIException;
}
