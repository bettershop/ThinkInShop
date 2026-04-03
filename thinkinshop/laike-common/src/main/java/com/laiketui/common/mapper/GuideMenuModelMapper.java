package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.domain.role.GuideMenuModel;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 功能导览菜单
 *
 * @author Trick
 * @date 2021/10/21 16:44
 */
public interface GuideMenuModelMapper extends BaseMapper<GuideMenuModel>
{

    @Select("SELECT IFNULL(MAX(guide_sort), 0) AS max_sort FROM lkt_guide_menu WHERE store_id = #{storeId} and role_id =#{roleId} ")
    int maxSort(int storeId, int roleId);

    //删除权限功能导览菜单
    @Delete(" delete a.* from lkt_guide_menu a,lkt_core_menu b where  a.menu_id=b.id and a.menu_id!=#{menuPtId} and b.s_id!=#{menuPtId} and b.type!=0 " +
            " and a.role_id=#{roleId} ")
    int deleteGuidMenu(int menuPtId, int roleId);

    /**
     * 批量删除导览菜单（角色 + 指定菜单ID集合）
     * @param roleId   角色ID
     * @param menuIds  要删除的菜单ID集合（当前语种下的菜单ID）
     * @return 删除的行数
     */
    int deleteGuideByRoleAndMenuIds(@Param("roleId") Integer roleId,
                                    @Param("menuIds") List<Integer> menuIds);

    void batchInsert(@Param("guideMenuList") List<GuideMenuModel> guideMenuList);
}