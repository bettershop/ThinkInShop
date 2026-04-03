package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.config.PluginsModel;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;


/**
 * 插件表sql映射
 *
 * @author Trick
 * @date 2020/10/10 9:32
 */
public interface PluginsModelMapper extends BaseMapper<PluginsModel>
{


    /**
     * 获取插件信息
     *
     * @param code - 接口code
     * @return int
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/10/10 9:58
     */

    PluginsModel getPluginInfo(String code, int storeId) throws LaiKeAPIException;

    /**
     * 获取所有安装成功并且未卸载的所有插件
     *
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/10/13 9:10
     */
    //@Select("select * from lkt_plugins where flag=0 and store_id = #{storeId} GROUP BY plugin_code")
    List<PluginsModel> getPluginsAll(@Param("store_id") int storeId) throws LaiKeAPIException;

    @Select("select plugin_code from lkt_plugins where flag = 0 and store_id = #{storeId} GROUP BY plugin_code")
    List<String> getPluginsCodeAll(int storeId) throws LaiKeAPIException;

    @Select("select id, store_id, plugin_name, plugin_code, plugin_img, optime, status, flag, jump_address, opuser, content, menu_id as menuId, is_mch_plugin as isMchPlugin, plugin_sort " +
            "from lkt_plugins where store_id = #{storeId} and flag = 0 order by plugin_sort desc, id asc")
    List<PluginsModel> selectPluginsByStoreIdOrderBySort(int storeId);

    @Select("<script>" +
            "select id, store_id, plugin_name, plugin_code, plugin_sort " +
            "from lkt_plugins " +
            "where store_id = #{storeId} and plugin_code in " +
            "<foreach collection='codes' item='code' open='(' separator=',' close=')'>#{code}</foreach>" +
            " and flag = 0" +
            "</script>")
    List<PluginsModel> selectByStoreIdAndCodes(@Param("storeId") int storeId, @Param("codes") List<String> codes);

    /**
     * 修改插件是否 卸载状态0未卸载1已卸载
     *
     * @param storeId
     * @param pluginCode
     * @param flag
     * @return
     */
    @Update("update lkt_plugins set flag = #{flag} where  store_id = #{storeId} and plugin_code = #{pluginCode}")
    Integer updateFlagByPluginCodeAndStoreId(Integer storeId, String pluginCode, Integer flag);
}