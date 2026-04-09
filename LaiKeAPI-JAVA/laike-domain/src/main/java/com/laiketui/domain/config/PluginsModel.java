package com.laiketui.domain.config;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

/**
 * @author sunH_
 */
@Table(name = "lkt_plugins")
public class PluginsModel implements Serializable
{

    /**
     * 插件安装状态
     */
    public interface PluginStatus
    {
        /**
         * 失败
         */
        Integer STATUS_FAIL    = 0;
        /**
         * 成功
         */
        Integer STATUS_SUCCESS = 1;
    }

    /**
     * 插件卸载状态
     */
    public interface PluginUninstall
    {
        /**
         * 未卸载
         */
        Integer UNINSTALL_NOT = 0;
        /**
         * 已卸载
         */
        Integer UNINSTALL_OK  = 1;
    }

    /**
     * 主键
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * 商城id
     */
    private Integer store_id;

    /**
     * 插件名称
     */
    private String plugin_name;

    /**
     * 插件编码
     */
    private String plugin_code;

    /**
     * 插件图标
     */
    private String plugin_img;

    /**
     * 安装时间
     */
    private Date optime;

    /**
     * 安装状态0失败1成功
     */
    private Integer status;

    /**
     * 卸载状态0未卸载1已卸载
     */
    private Integer flag;

    /**
     * 插件跳转地址
     */
    private String jump_address;

    /**
     * 操作人用户名
     */
    private String opuser;

    /**
     * 插件描述
     */
    private String content;

    /**
     * 插件对应通用菜单表id(用于控制pc店铺插件菜单是否显示)
     */
    private String menuId;

    /**
     * 插件是否支持pc店铺 1支持 2不支持
     */
    private Integer isMchPlugin;

    /**
     * 插件排序，数值越大越靠前
     */
    private Integer plugin_sort;

    public Integer getPlugin_sort() {
        return plugin_sort;
    }

    public void setPlugin_sort(Integer plugin_sort) {
        this.plugin_sort = plugin_sort;
    }

    public String getContent()
    {
        return content;
    }

    public void setContent(String content)
    {
        this.content = content;
    }

    /**
     * 获取主键
     *
     * @return id - 主键
     */
    public Integer getId()
    {
        return id;
    }

    /**
     * 设置主键
     *
     * @param id 主键
     */
    public void setId(Integer id)
    {
        this.id = id;
    }

    public Integer getStore_id()
    {
        return store_id;
    }

    public void setStore_id(Integer store_id)
    {
        this.store_id = store_id;
    }

    /**
     * 获取插件名称
     *
     * @return plugin_name - 插件名称
     */
    public String getPlugin_name()
    {
        return plugin_name;
    }

    /**
     * 设置插件名称
     *
     * @param plugin_name 插件名称
     */
    public void setPlugin_name(String plugin_name)
    {
        this.plugin_name = plugin_name == null ? null : plugin_name.trim();
    }

    /**
     * 获取插件编码
     *
     * @return plugin_code - 插件编码
     */
    public String getPlugin_code()
    {
        return plugin_code;
    }

    /**
     * 设置插件编码
     *
     * @param plugin_code 插件编码
     */
    public void setPlugin_code(String plugin_code)
    {
        this.plugin_code = plugin_code == null ? null : plugin_code.trim();
    }

    /**
     * 获取插件图标
     *
     * @return plugin_img - 插件图标
     */
    public String getPlugin_img()
    {
        return plugin_img;
    }

    /**
     * 设置插件图标
     *
     * @param plugin_img 插件图标
     */
    public void setPlugin_img(String plugin_img)
    {
        this.plugin_img = plugin_img == null ? null : plugin_img.trim();
    }

    /**
     * 获取安装时间
     *
     * @return optime - 安装时间
     */
    public Date getOptime()
    {
        return optime;
    }

    /**
     * 设置安装时间
     *
     * @param optime 安装时间
     */
    public void setOptime(Date optime)
    {
        this.optime = optime;
    }

    /**
     * 获取安装状态0失败1成功
     *
     * @return status - 安装状态0失败1成功
     */
    public Integer getStatus()
    {
        return status;
    }

    /**
     * 设置安装状态0失败1成功
     *
     * @param status 安装状态0失败1成功
     */
    public void setStatus(Integer status)
    {
        this.status = status;
    }

    /**
     * 获取卸载状态0未卸载1已卸载
     *
     * @return flag - 卸载状态0未卸载1已卸载
     */
    public Integer getFlag()
    {
        return flag;
    }

    /**
     * 设置卸载状态0未卸载1已卸载
     *
     * @param flag 卸载状态0未卸载1已卸载
     */
    public void setFlag(Integer flag)
    {
        this.flag = flag;
    }

    public String getJump_address()
    {
        return jump_address;
    }

    public void setJump_address(String jump_address)
    {
        this.jump_address = jump_address;
    }

    /**
     * 获取操作人用户名
     *
     * @return opuser - 操作人用户名
     */
    public String getOpuser()
    {
        return opuser;
    }

    /**
     * 设置操作人用户名
     *
     * @param opuser 操作人用户名
     */
    public void setOpuser(String opuser)
    {
        this.opuser = opuser == null ? null : opuser.trim();
    }

    public String getMenuId()
    {
        return menuId;
    }

    public void setMenuId(String menuId)
    {
        this.menuId = menuId;
    }

    public Integer getIsMchPlugin()
    {
        return isMchPlugin;
    }

    public void setIsMchPlugin(Integer isMchPlugin)
    {
        this.isMchPlugin = isMchPlugin;
    }
}