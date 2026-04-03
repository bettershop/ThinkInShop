package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.role.CoreMenuModel;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;


/**
 * 核心菜单
 *
 * @author Trick
 * @date 2021/1/14 11:39
 */
public interface CoreMenuModelMapper extends BaseMapper<CoreMenuModel>
{


    /**
     * 获取某级菜单
     *
     * @param sid -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/14 11:40
     */

    List<Map<String, Object>> getCoreMenuInfoBySid(@Param("sid") int sid, @Param("lang_code") String lang_code) throws LaiKeAPIException;

    /**
     * 获取某级菜单下tab页面id
     *
     * @param sid -
     * @return List
     * @throws LaiKeAPIException -
     * @author gp
     * @date 2023-10-23
     */
    @Select("select id from lkt_core_menu where s_id = #{sid} and recycle = 0 and is_tab = 1")
    List<Integer> getCoreMenuTabIdBySid(int sid) throws LaiKeAPIException;

    /**
     * 批量获取存在 tab 子菜单的父级菜单id
     *
     * @param ids - 父级菜单id集合
     * @return List
     * @throws LaiKeAPIException -
     */
    List<Integer> getMenuIdsWithTabChildren(@Param("ids") List<Integer> ids) throws LaiKeAPIException;

    /**
     * 获取系统菜单
     *
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/14 15:44
     */
    List<Integer> getSystemMenuList(String lang_code);

    /**
     * 获取核心菜单信息
     *
     * @param map -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/28 16:26
     */
    @Override
    List<Map<String, Object>> selectDynamic(Map<String, Object> map) throws LaiKeAPIException;


    /**
     * 获取功能导览
     *
     * @param map -
     * @return List -
     * @throws LaiKeAPIException -
     */
    List<Map<String, Object>> getFunctionOverview(Map<String, Object> map);


    List<Integer> getRoleMenuIds(Map<String, Object> map) throws LaiKeAPIException;

    //获取角色拥有的权限信息
    List<Map<String, Object>> getRoleMenuInfos(Map<String, Object> map) throws LaiKeAPIException;

    int countFunctionOverview(Map<String, Object> map);

    /**
     * 获取核心菜单信息 - 统计
     *
     * @param map -
     * @return int
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/28 16:26
     */
    @Override
    int countDynamic(Map<String, Object> map) throws LaiKeAPIException;


    /**
     * 获取最大的序号
     *
     * @param id -
     * @return int
     * @author Trick
     * @date 2021/6/10 9:26
     */
    @Select("select IFNULL(MAX(sort), 0) AS max_sort from lkt_core_menu where level=#{level}")
    int maxSort(int level);

    //获取平台菜单
    @Select("select * from lkt_core_menu where module='Platform' and type=0 and level=1")
    List<CoreMenuModel> getSystemMenu();

    //获取当前级别菜单最新序号
    @Select("select IFNULL(MAX(sort), 0) AS max_sort from lkt_core_menu where level=#{level} and s_id=#{sid} and type=#{type}")
    int maxSortByLevel(int level, int sid, int type);

    //删除菜单
    @Delete("delete a,b,c from lkt_core_menu a left join lkt_role_menu b on a.id=b.menu_id left join lkt_guide_menu c on a.id=c.menu_id where a.id=#{menuId}")
    int delCoreMenu(int menuId);

    /**
     * 序号换位
     *
     * @param id     -
     * @param moveId -
     * @return int
     * @author Trick
     * @date 2021/6/10 9:38
     */
    @Update("update lkt_core_menu a,lkt_core_menu b set a.sort= b.sort,b.sort=a.sort where a.id=#{id} and b.id=#{moveId}")
    int moveSort(int id, int moveId);

    /**
     * 获取角色菜单以下所有的权限按钮
     *
     * @param roleId
     * @param meunId
     * @return
     * @author sunH_
     * @date 2021/11/23 10:38
     */
    @Select("SELECT DISTINCT c.* FROM lkt_core_menu c LEFT JOIN lkt_role_menu r ON c.id = r.menu_id WHERE r.role_id = #{roleId} AND c.s_id = #{meunId} AND c.is_button = 1 AND c.recycle = 0")
    List<Map<String, Object>> getButton(Integer roleId, BigInteger meunId);

    /**
     * 获取角色菜单tab页面权限
     *
     * @param roleId -
     * @param meunId -
     * @return
     * @author gp
     * @date 2023/10/25
     */
    @Select("SELECT DISTINCT c.* FROM lkt_core_menu c LEFT JOIN lkt_role_menu r ON c.id = r.menu_id WHERE r.role_id = #{roleId} AND c.s_id = #{meunId} AND  c.recycle = 0 and c.is_tab = 1")
    List<Map<String, Object>> getTab(Integer roleId, Integer meunId);

    @Select("SELECT id FROM `lkt_core_menu` WHERE s_id  in (SELECT id FROM lkt_core_menu WHERE module = 'plug_ins')")
    List<Integer> getAllPlugMenuId();

    /**
     * 根据给定的权限菜单id 查询菜单信息
     * @param permissions
     * @return
     */
    List<CoreMenuModel> selectByIds(List<Integer> permissions);

    /**
     * 查询指定语种的所有菜单 ID
     * @param langCode
     * @return
     */
    List<Integer> getMenuIdsByLangCode(String langCode);

    /**
     * 查询角色当前所有菜单
     * @param roleId
     * @param language
     * @return
     */
    List<Map<String, Object>> selectAllRoleMenus(Integer roleId, String language);
}
