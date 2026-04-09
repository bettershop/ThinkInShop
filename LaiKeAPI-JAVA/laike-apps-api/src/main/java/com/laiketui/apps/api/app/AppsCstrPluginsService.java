package com.laiketui.apps.api.app;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.plugin.PluginsVo;

import java.util.Map;

/**
 * 插件信息
 *
 * @author sunH_
 * @date 2022/01/11 14:10
 */
public interface AppsCstrPluginsService
{

    /**
     * 获取插件信息
     *
     * @param vo
     * @param pluginCode
     * @return
     * @throws LaiKeAPIException
     * @author sunH_
     * @date 2022/01/11 14:10
     */
    Map<String, Object> getPluginInfo(MainVo vo, String pluginCode) throws LaiKeAPIException;

    /**
     * 修改插件统用信息
     *
     * @param vo
     * @throws LaiKeAPIException
     * @author sunH_
     * @date 2022/01/11 14:10
     */
    void addOrUpdate(PluginsVo vo) throws LaiKeAPIException;

    /**
     * 获取所有插件-开关
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022/8/9 11:53
     */
    Map<String, Object> getPluginAll(MainVo vo) throws LaiKeAPIException;

}
