package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.authority.MenuModel;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

/**
 * 通用 菜单角色表
 *
 * @author Trick
 * @date 2021/12/12 14:49
 */
public interface MenuModelMapper extends BaseMapper<MenuModel>
{

    @Override
    List<Map<String, Object>> selectDynamic(Map<String, Object> map) throws LaiKeAPIException;

    @Override
    int countDynamic(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 获取菜单列表
     *
     * @param map -
     * @return List
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/12/12 16:23
     */
    List<Map<String, Object>> getMenuList(Map<String, Object> map) throws LaiKeAPIException;

    int countMenuList(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 获取当前角色所有菜单id
     *
     * @param storeId -
     * @param roleId  -
     * @param type    -
     * @return List
     * @author Trick
     * @date 2021/12/27 14:09
     */
    @Select("select d.id FROM lkt_user_role a left JOIN lkt_user_authority b ON b.role_id=a.id RIGHT JOIN lkt_authority_mapping c ON c.role_id = a.id " +
            " RIGHT JOIN lkt_menu d ON d.id = c.menu_id " +
            " WHERE a.id=#{roleId} AND d.type = #{type} AND d.is_display = 0 " +
            " ORDER BY d.sort DESC ")
    List<String> getRoleMenuIdList(Integer storeId, String roleId, int type);

    /**
     * 获取指定商城角色id
     *
     * @param storeId -
     * @return int
     * @author Trick
     * @date 2022/5/18 17:09
     */
    @Select("select c.id from lkt_customer a inner join lkt_admin b on a.admin_id=b.id inner join lkt_role c on b.role=c.id where a.id=#{storeId}")
    int getStoreRole(int storeId);

    @Select("select ifnull(max(sort),0) from lkt_menu where type=#{type} and level=#{level} and sid=#{sid}")
    int maxSort(int type, int level, String sid);

    @Update("update lkt_menu a,lkt_menu b set a.sort=b.sort,b.sort = a.sort where a.id=#{id} and b.id=#{id1} ")
    int move(String id, String id1);
}