package com.laiketui.domain.authority;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * 通用 菜单权限映射表 PC店铺菜单
 *
 * @author Trick
 * @date 2021/12/12 11:49
 */
@Table(name = "lkt_menu")
public class MenuModel implements Serializable
{
    /**
     * id
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;

    /**
     * 归类主键id(例如 门店id/店铺id/商城id)
     */
    @Column(name = "main_id")
    private String main_id;

    /**
     * 类型 1=门店id 2=店铺id 3=商城id
     */
    private Integer type;

    /**
     * 上级id
     */
    private String sid;

    /**
     * 菜单名称
     */
    private String name;

    /**
     * 菜单等级
     */
    private Integer level;

    /**
     * 默认图标
     */
    private String logo;

    /**
     * 选中图标
     */
    private String checked_logo;

    /**
     * 页面所在路径文件夹 [vue-动态路由里的 path]
     */
    private String path;

    /**
     * 页面路径 [vue-动态路由里的 component]
     */
    private String url;

    /**
     * 是否禁用 1=是 0=否
     */
    private Integer is_display;

    /**
     * 菜单说明
     */
    @Column(name = "`text`")
    private String text;

    /**
     * 排序序号
     */
    private Integer sort;

    /**
     * 创建时间
     */
    private Date add_date;

    /**
     * 修改时间
     */
    private Date update_date;

    /**
     * 语种
     */
    private String lang_code;

    /**
     * 国家编码
     */
    private Integer country_num;

    public String getLang_code()
    {
        return lang_code;
    }

    public void setLang_code(String lang_code)
    {
        this.lang_code = lang_code;
    }

    public Integer getCountry_num()
    {
        return country_num;
    }

    public void setCountry_num(Integer country_num)
    {
        this.country_num = country_num;
    }

    public String getLogo()
    {
        return logo;
    }

    public void setLogo(String logo)
    {
        this.logo = logo;
    }

    public String getChecked_logo()
    {
        return checked_logo;
    }

    public void setChecked_logo(String checked_logo)
    {
        this.checked_logo = checked_logo;
    }

    public Integer getSort()
    {
        return sort;
    }

    public void setSort(Integer sort)
    {
        this.sort = sort;
    }

    public Date getUpdate_date()
    {
        return update_date;
    }

    public void setUpdate_date(Date update_date)
    {
        this.update_date = update_date;
    }

    public Integer getType()
    {
        return type;
    }

    public void setType(Integer type)
    {
        this.type = type;
    }

    public String getText()
    {
        return text;
    }

    public void setText(String text)
    {
        this.text = text;
    }

    /**
     * 获取id
     *
     * @return id - id
     */
    public String getId()
    {
        return id;
    }

    /**
     * 设置id
     *
     * @param id id
     */
    public void setId(String id)
    {
        this.id = id == null ? null : id.trim();
    }

    /**
     * 获取归类主键id(例如 门店id/店铺id/商城id)
     *
     * @return main_id - 归类主键id(例如 门店id/店铺id/商城id)
     */
    public String getMain_id()
    {
        return main_id;
    }

    /**
     * 设置归类主键id(例如 门店id/店铺id/商城id)
     *
     * @param main_id 归类主键id(例如 门店id/店铺id/商城id)
     */
    public void setMain_id(String main_id)
    {
        this.main_id = main_id;
    }

    /**
     * 获取上级id
     *
     * @return sid - 上级id
     */
    public String getSid()
    {
        return sid;
    }

    /**
     * 设置上级id
     *
     * @param sid 上级id
     */
    public void setSid(String sid)
    {
        this.sid = sid == null ? null : sid.trim();
    }

    /**
     * 获取菜单名称
     *
     * @return name - 菜单名称
     */
    public String getName()
    {
        return name;
    }

    /**
     * 设置菜单名称
     *
     * @param name 菜单名称
     */
    public void setName(String name)
    {
        this.name = name == null ? null : name.trim();
    }

    /**
     * 获取菜单等级
     *
     * @return level - 菜单等级
     */
    public Integer getLevel()
    {
        return level;
    }

    /**
     * 设置菜单等级
     *
     * @param level 菜单等级
     */
    public void setLevel(Integer level)
    {
        this.level = level;
    }

    /**
     * 获取页面所在路径文件夹 [vue-动态路由里的 path]
     *
     * @return path - 页面所在路径文件夹 [vue-动态路由里的 path]
     */
    public String getPath()
    {
        return path;
    }

    /**
     * 设置页面所在路径文件夹 [vue-动态路由里的 path]
     *
     * @param path 页面所在路径文件夹 [vue-动态路由里的 path]
     */
    public void setPath(String path)
    {
        this.path = path == null ? null : path.trim();
    }

    /**
     * 获取页面路径 [vue-动态路由里的 component]
     *
     * @return url - 页面路径 [vue-动态路由里的 component]
     */
    public String getUrl()
    {
        return url;
    }

    /**
     * 设置页面路径 [vue-动态路由里的 component]
     *
     * @param url 页面路径 [vue-动态路由里的 component]
     */
    public void setUrl(String url)
    {
        this.url = url == null ? null : url.trim();
    }

    /**
     * 获取是否禁用 1=是 0=否
     *
     * @return is_display - 是否禁用 1=是 0=否
     */
    public Integer getIs_display()
    {
        return is_display;
    }

    /**
     * 设置是否禁用 1=是 0=否
     *
     * @param is_display 是否禁用 1=是 0=否
     */
    public void setIs_display(Integer is_display)
    {
        this.is_display = is_display;
    }

    /**
     * 获取创建时间
     *
     * @return add_date - 创建时间
     */
    public Date getAdd_date()
    {
        return add_date;
    }

    /**
     * 设置创建时间
     *
     * @param add_date 创建时间
     */
    public void setAdd_date(Date add_date)
    {
        this.add_date = add_date;
    }
}