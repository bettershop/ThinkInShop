package com.laiketui.domain.authority;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * 通用 权限绑定
 *
 * @author Trick
 * @date 2021/12/12 11:51
 */
@Table(name = "lkt_user_authority")
public class UserAuthorityModel implements Serializable
{

    /**
     * 用户id
     */
    @Transient
    public static final Integer USER_ID  = 1;
    /**
     * 管理员id
     */
    @Transient
    public static final Integer ADMIN_ID = 2;

    /**
     * id
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;

    /**
     * 账户id(例如 user_id,admin_id)
     */
    private String main_id;

    /**
     * 类型 1=userId 2=admin_id
     */
    private Integer type;

    /**
     * 角色id
     */
    @Column(name = "role_id")
    private String role_id;

    /**
     * 操作者id
     */
    @Column(name = "create_id")
    private String create_id;

    /**
     * 创建时间
     */
    private Date add_date;

    /**
     * 修改时间
     */
    private Date update_date;


    public String getCreate_id()
    {
        return create_id;
    }

    public void setCreate_id(String create_id)
    {
        this.create_id = create_id;
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
     * 获取账户id(例如 user_id,admin_id)
     *
     * @return main_id - 账户id(例如 user_id,admin_id)
     */
    public String getMain_id()
    {
        return main_id;
    }

    /**
     * 设置账户id(例如 user_id,admin_id)
     *
     * @param main_id 账户id(例如 user_id,admin_id)
     */
    public void setMain_id(String main_id)
    {
        this.main_id = main_id == null ? null : main_id.trim();
    }

    public Integer getType()
    {
        return type;
    }

    public void setType(Integer type)
    {
        this.type = type;
    }

    public String getRole_id()
    {
        return role_id;
    }

    public void setRole_id(String role_id)
    {
        this.role_id = role_id;
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
}