package com.laiketui.domain.diy;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

@Table(name = "lkt_diy_project")
public class DiyProjectModel implements Serializable
{
    /**
     * 项目id
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * 商城id
     */
    private Integer store_id;

    /**
     * 管理员id
     */
    private Integer admin_id;

    /**
     * 项目名称
     */
    private String pro_title;

    /**
     * 项目logo
     */
    private String logo;

    /**
     * 封面
     */
    private String cover;

    /**
     * 状态 1启用 2不启用
     */
    private Integer is_show;

    /**
     * 删除  1 是 2 否
     */
    private Integer is_del;

    /**
     * 店铺id
     */
    private Integer mch_id;

    /**
     * 供应商id
     */
    private Integer sp_id;

    /**
     * 添加时间
     */
    private Date add_date;

    /**
     * 修改时间
     */
    private Date update_date;

    /**
     * 获取项目id
     *
     * @return id - 项目id
     */
    public Integer getId()
    {
        return id;
    }

    /**
     * 设置项目id
     *
     * @param id 项目id
     */
    public void setId(Integer id)
    {
        this.id = id;
    }

    /**
     * 获取商城id
     *
     * @return store_id - 商城id
     */
    public Integer getStore_id()
    {
        return store_id;
    }

    /**
     * 设置商城id
     *
     * @param store_id 商城id
     */
    public void setStore_id(Integer store_id)
    {
        this.store_id = store_id;
    }

    /**
     * 获取管理员id
     *
     * @return admin_id - 管理员id
     */
    public Integer getAdmin_id()
    {
        return admin_id;
    }

    /**
     * 设置管理员id
     *
     * @param admin_id 管理员id
     */
    public void setAdmin_id(Integer admin_id)
    {
        this.admin_id = admin_id;
    }

    /**
     * 获取项目名称
     *
     * @return pro_title - 项目名称
     */
    public String getPro_title()
    {
        return pro_title;
    }

    /**
     * 设置项目名称
     *
     * @param pro_title 项目名称
     */
    public void setPro_title(String pro_title)
    {
        this.pro_title = pro_title == null ? null : pro_title.trim();
    }

    /**
     * 获取项目logo
     *
     * @return logo - 项目logo
     */
    public String getLogo()
    {
        return logo;
    }

    /**
     * 设置项目logo
     *
     * @param logo 项目logo
     */
    public void setLogo(String logo)
    {
        this.logo = logo == null ? null : logo.trim();
    }

    /**
     * 获取封面
     *
     * @return cover - 封面
     */
    public String getCover()
    {
        return cover;
    }

    /**
     * 设置封面
     *
     * @param cover 封面
     */
    public void setCover(String cover)
    {
        this.cover = cover == null ? null : cover.trim();
    }

    /**
     * 获取状态 1启用 2不启用
     *
     * @return is_show - 状态 1启用 2不启用
     */
    public Integer getIs_show()
    {
        return is_show;
    }

    /**
     * 设置状态 1启用 2不启用
     *
     * @param is_show 状态 1启用 2不启用
     */
    public void setIs_show(Integer is_show)
    {
        this.is_show = is_show;
    }

    /**
     * 获取删除  1 是 2 否
     *
     * @return is_del - 删除  1 是 2 否
     */
    public Integer getIs_del()
    {
        return is_del;
    }

    /**
     * 设置删除  1 是 2 否
     *
     * @param is_del 删除  1 是 2 否
     */
    public void setIs_del(Integer is_del)
    {
        this.is_del = is_del;
    }

    /**
     * 获取店铺id
     *
     * @return mch_id - 店铺id
     */
    public Integer getMch_id()
    {
        return mch_id;
    }

    /**
     * 设置店铺id
     *
     * @param mch_id 店铺id
     */
    public void setMch_id(Integer mch_id)
    {
        this.mch_id = mch_id;
    }

    /**
     * 获取供应商id
     *
     * @return sp_id - 供应商id
     */
    public Integer getSp_id()
    {
        return sp_id;
    }

    /**
     * 设置供应商id
     *
     * @param sp_id 供应商id
     */
    public void setSp_id(Integer sp_id)
    {
        this.sp_id = sp_id;
    }

    /**
     * 获取添加时间
     *
     * @return add_date - 添加时间
     */
    public Date getAdd_date()
    {
        return add_date;
    }

    /**
     * 设置添加时间
     *
     * @param add_date 添加时间
     */
    public void setAdd_date(Date add_date)
    {
        this.add_date = add_date;
    }

    /**
     * 获取修改时间
     *
     * @return update_date - 修改时间
     */
    public Date getUpdate_date()
    {
        return update_date;
    }

    /**
     * 设置修改时间
     *
     * @param update_date 修改时间
     */
    public void setUpdate_date(Date update_date)
    {
        this.update_date = update_date;
    }
}