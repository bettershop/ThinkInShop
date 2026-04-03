package com.laiketui.domain.plugins;

import java.util.List;

/**
 * @description:
 * @author: wx
 * @date: Created in 2019/10/25 10:03
 * @version: 1.0
 * @modified By:
 */
public interface PluginMgr
{

    /**
     * @param vo
     * @return
     * @description 安装
     * @author wx
     * @date 2019/10/25 10:04
     */
    void install(PluginsVO vo) throws Exception;

    /**
     * @param vo
     * @return
     * @description 卸载
     * @author wx
     * @date 2019/10/25 10:04
     */
    void uninstall(PluginsVO vo) throws Exception;

    /**
     * @return
     * @description 启用
     * @author wx
     * @date 2019/10/25 10:06
     */
    void open(List<PluginsVO> vos) throws Exception;

    /**
     * @return
     * @description 关闭
     * @author wx
     * @date 2019/10/25 10:06
     */
    void close(List<PluginsVO> vos) throws Exception;

}
