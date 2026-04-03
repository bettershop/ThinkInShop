package com.laiketui.domain.authority;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * 通用 菜单角色表
 *
 * @author Trick
 * @date 2021/12/12 11:52
 */
@Table(name = "lkt_user_role")
public class UserRoleModel implements Serializable
{
    /**
     * id
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;

    /**
     * 门店类型
     */
    @Transient
    public static final Integer STORE_ID = 1;
    /**
     * 店铺类型
     */
    @Transient
    public static final Integer SHOP_ID  = 2;
    /**
     * 商城类型
     */
    @Transient
    public static final Integer MALL_ID  = 3;

    /**
     * 角色名称
     */
    private String name;

    /**
     * 归类主键id(例如 门店id/店铺id/商城id)
     */
    @Column(name = "main_id")
    private String main_id;


    /**
     * 创建时间
     */
    private Date add_date;

    /**
     * 修改时间
     */
    private Date update_date;

    /**
     * 角色说明
     */
    @Column(name = "`text`")
    private String text;

    /**
     * 1=门店id 2=店铺id 3=商城id
     */
    private Integer type;

    public Integer getType()
    {
        return type;
    }

    public void setType(Integer type)
    {
        this.type = type;
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
     * 获取角色名称
     *
     * @return name - 角色名称
     */
    public String getName()
    {
        return name;
    }

    /**
     * 设置角色名称
     *
     * @param name 角色名称
     */
    public void setName(String name)
    {
        this.name = name == null ? null : name.trim();
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

    /**
     * 获取角色说明
     *
     * @return text - 角色说明
     */
    public String getText()
    {
        return text;
    }

    /**
     * 设置角色说明
     *
     * @param text 角色说明
     */
    public void setText(String text)
    {
        this.text = text == null ? null : text.trim();
    }


    public String getMain_id()
    {
        return main_id;
    }

    public void setMain_id(String main_id)
    {
        this.main_id = main_id;
    }
}