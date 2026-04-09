package com.laiketui.admins.api.admin.plugin;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.config.AddPluginConfigVo;
import com.laiketui.domain.vo.config.AddPluginOrderConfigVo;

import java.util.Map;

/**
 * 插件管理
 *
 * @author Trick
 * @date 2023/3/17 10:18
 */
public interface AdminPluginManageService
{

    /**
     * 获取插件列表
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2023/3/17 10:21
     */
    Map<String, Object> index(MainVo vo) throws LaiKeAPIException;

    /**
     * 插件开关
     *
     * @param vo -
     * @param id -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2023/3/17 13:53
     */
    void pluginSwitch(MainVo vo, int id) throws LaiKeAPIException;

    /**
     * 插件配置
     *
     * @param vo -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2023/3/17 15:32
     */
    void addPlugin(AddPluginConfigVo vo) throws LaiKeAPIException;

    /**
     * 添加/编辑 插件订单设置
     *
     * @param vo -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2023/3/17 15:24
     */
    void addPluginConfig(AddPluginOrderConfigVo vo) throws LaiKeAPIException;

    /**
     * 获取订单设置
     *
     * @param vo         -
     * @param pluginCode -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2023/4/4 18:05
     */
    Map<String, Object> getOrderConfig(MainVo vo, String pluginCode) throws LaiKeAPIException;

    /**
     * 保存插件排序
     *
     * @param vo         -
     * @param pluginId   插件ID
     * @param pluginSort 排序值
     * @throws LaiKeAPIException-
     * @author 影子
     * @date 2026/3/9
     */
    void saveSort(MainVo vo, int pluginId, int pluginSort) throws LaiKeAPIException;
}
