package com.laiketui.domain.authority;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

/**
 * 通用 菜单权限映射表
 *
 * @author Trick
 * @date 2021/12/12 11:55
 */
@Table(name = "lkt_authority_mapping")
public class AuthorityMappingModel implements Serializable
{
    /**
     * id
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;

    /**
     * 角色id
     */
    private String role_id;

    /**
     * 菜单权限id
     */
    private String menu_id;

    /**
     * 创建时间
     */
    private Date add_date;

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
     * 获取角色id
     *
     * @return role_id - 角色id
     */
    public String getRole_id()
    {
        return role_id;
    }

    /**
     * 设置角色id
     *
     * @param role_id 角色id
     */
    public void setRole_id(String role_id)
    {
        this.role_id = role_id == null ? null : role_id.trim();
    }

    /**
     * 获取菜单权限id
     *
     * @return menu_id - 菜单权限id
     */
    public String getMenu_id()
    {
        return menu_id;
    }

    /**
     * 设置菜单权限id
     *
     * @param menu_id 菜单权限id
     */
    public void setMenu_id(String menu_id)
    {
        this.menu_id = menu_id == null ? null : menu_id.trim();
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