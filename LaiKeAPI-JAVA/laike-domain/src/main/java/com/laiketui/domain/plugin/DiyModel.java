package com.laiketui.domain.plugin;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * diy
 *
 * @author Trick
 * @date 2021/6/30 13:23
 */
@Table(name = "lkt_diy")
public class DiyModel implements Serializable
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * 版本号
     */
    private String version;

    /**
     * 页面名称
     */
    private String name;

    /**
     * 页面数据
     */
    private String value;

    /**
     * 主题类型字段code
     */
    private String theme_dict_code;

    /**
     * 主题类型
     */
    private Integer theme_type;

    /**
     * 备注
     */
    private String remark;

    /**
     * 添加时间
     */
    private Long add_time;

    /**
     * 更新时间
     */
    private Long update_time;

    /**
     * 是否使用
     */
    private Integer status;

    /**
     * 页面类型
     */
    private Integer type;

    /**
     * 是否删除
     */
    private Integer is_del;

    /**
     * 商城ID
     */
    private Integer store_id;

    /**
     * 店铺id
     */
    private Integer mch_id;

    /**
     * 封面图
     */
    private String cover;


    /**
     * 导航
     */
    private String tab_bar;

    /**
     * 导航配置信息
     */
    private String tabber_info;

    /**
     * 语言
     */
    private String lang_code;

    public String getLang_code() {
        return lang_code;
    }

    public void setLang_code(String lang_code) {
        this.lang_code = lang_code;
    }

    public String getTab_bar() {
        return tab_bar;
    }

    public void setTab_bar(String tab_bar) {
        this.tab_bar = tab_bar;
    }

    public String getTabber_info() {
        return tabber_info;
    }

    public void setTabber_info(String tabber_info) {
        this.tabber_info = tabber_info;
    }

    public Integer getMch_id() {
        return mch_id;
    }

    public void setMch_id(Integer mch_id) {
        this.mch_id = mch_id;
    }

    public Integer getTheme_type() {
        return theme_type;
    }

    public void setTheme_type(Integer theme_type) {
        this.theme_type = theme_type;
    }

    public String getTheme_dict_code() {
        return theme_dict_code;
    }

    public void setTheme_dict_code(String theme_dict_code) {
        this.theme_dict_code = theme_dict_code;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    /**
     * @return id
     */
    public Integer getId()
    {
        return id;
    }

    /**
     * @param id
     */
    public void setId(Integer id)
    {
        this.id = id;
    }

    /**
     * 获取版本号
     *
     * @return version - 版本号
     */
    public String getVersion()
    {
        return version;
    }

    /**
     * 设置版本号
     *
     * @param version 版本号
     */
    public void setVersion(String version)
    {
        this.version = version == null ? null : version.trim();
    }

    /**
     * 获取页面名称
     *
     * @return name - 页面名称
     */
    public String getName()
    {
        return name;
    }

    /**
     * 设置页面名称
     *
     * @param name 页面名称
     */
    public void setName(String name)
    {
        this.name = name == null ? null : name.trim();
    }

    /**
     * 获取页面数据
     *
     * @return value - 页面数据
     */
    public String getValue()
    {
        return value;
    }

    /**
     * 设置页面数据
     *
     * @param value 页面数据
     */
    public void setValue(String value)
    {
        this.value = value == null ? null : value.trim();
    }

    /**
     * 获取添加时间
     *
     * @return add_time - 添加时间
     */
    public Long getAdd_time()
    {
        return add_time;
    }

    /**
     * 设置添加时间
     *
     * @param add_time 添加时间
     */
    public void setAdd_time(Long add_time)
    {
        this.add_time = add_time;
    }

    /**
     * 获取更新时间
     *
     * @return update_time - 更新时间
     */
    public Long getUpdate_time()
    {
        return update_time;
    }

    /**
     * 设置更新时间
     *
     * @param update_time 更新时间
     */
    public void setUpdate_time(Long update_time)
    {
        this.update_time = update_time;
    }

    /**
     * 获取是否使用
     *
     * @return status - 是否使用
     */
    public Integer getStatus()
    {
        return status;
    }

    /**
     * 设置是否使用
     *
     * @param status 是否使用
     */
    public void setStatus(Integer status)
    {
        this.status = status;
    }

    /**
     * 获取页面类型
     *
     * @return type - 页面类型
     */
    public Integer getType()
    {
        return type;
    }

    /**
     * 设置页面类型
     *
     * @param type 页面类型
     */
    public void setType(Integer type)
    {
        this.type = type;
    }

    /**
     * 获取是否删除
     *
     * @return is_del - 是否删除
     */
    public Integer getIs_del()
    {
        return is_del;
    }

    /**
     * 设置是否删除
     *
     * @param is_del 是否删除
     */
    public void setIs_del(Integer is_del)
    {
        this.is_del = is_del;
    }

    /**
     * 获取商城ID
     *
     * @return store_id - 商城ID
     */
    public Integer getStore_id()
    {
        return store_id;
    }

    /**
     * 设置商城ID
     *
     * @param store_id 商城ID
     */
    public void setStore_id(Integer store_id)
    {
        this.store_id = store_id;
    }

    /**
     * 获取封面图
     *
     * @return cover - 封面图
     */
    public String getCover()
    {
        return cover;
    }

    /**
     * 设置封面图
     *
     * @param cover 封面图
     */
    public void setCover(String cover)
    {
        this.cover = cover == null ? null : cover.trim();
    }
}