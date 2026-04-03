package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.role.RoleMenuModel;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;


/**
 * 权限菜单
 *
 * @author Trick
 * @date 2021/1/14 15:47
 */
public interface RoleMenuModelMapper extends BaseMapper<RoleMenuModel>
{


    /**
     * 根据角色获取权限列表
     *
     * @param roleId -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/14 15:44
     */

    @Select("select menu_id from lkt_role_menu where role_id = #{id}")
    List<Integer> getUserRoleMenuInfoToId(int roleId) throws LaiKeAPIException;


    /**
     * 获取商城普通管理员权限【只适用于商城普通管理员】
     * (如果账号是平台普通管理员,自身的权限以商城管理员拥有的权限为基础做筛选)
     *
     * @param roleId    -
     * @param storeRole - 商城权限
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/14 15:44
     */
    @Select("select menu_id from lkt_role_menu a where a.role_id = #{roleId} and EXISTS (select x.menu_id from lkt_role_menu x where x.role_id = #{storeRole} and a.menu_id=x.menu_id ) ")
    List<Integer> getStoreRoleMenuInfo(int roleId, int storeRole);

    /**
     * 根据角色和url判断是否有权限
     *
     * @param roleId -
     * @param url    -
     * @author Trick
     * @date 2021/8/3 15:37
     */
    @Select("select count(1) from lkt_role_menu as a left join lkt_core_menu as b on a.menu_id = b.id " +
            "where a.role_id in (#{roleId}) and b.recycle = 0 and b.url = #{url}")
    int countButtonRole(String roleId, String url);

    /**
     * 根据权限删除对应菜单
     *
     * @param roleId   -
     * @param menuPtId -  平台一级菜单id
     * @return int
     * @author Trick
     * @date 2021/10/28 14:09
     */
    @Delete("delete a.* from lkt_role_menu a,lkt_core_menu b where  a.menu_id=b.id and a.menu_id!=#{menuPtId} and b.s_id!=#{menuPtId} and b.type!=0 " +
            "and a.role_id=#{roleId}")
    int deleteMenu(int roleId, int menuPtId);

    void deleteByRoleId(Integer roleId);

    void batchInsertIgnore(@Param("roleMenuList") List<RoleMenuModel> roleMenuList);

    /**
     * 根据角色ID 和 菜单ID集合 批量删除关联
     * @param roleId   角色ID
     * @param menuIds  菜单ID集合
     * @return 删除行数
     */
    int deleteByRoleAndMenuIds(@Param("roleId") Integer roleId,
                               @Param("menuIds") List<Integer> menuIds);
}