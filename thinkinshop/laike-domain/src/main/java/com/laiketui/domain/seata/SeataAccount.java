package com.laiketui.domain.seata;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Table(name = "t_user")
public class SeataAccount implements Serializable
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String user_name;

    private String name;

    private String sex;

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
     * @return user_name
     */
    public String getUser_name()
    {
        return user_name;
    }

    /**
     * @param user_name
     */
    public void setUser_name(String user_name)
    {
        this.user_name = user_name == null ? null : user_name.trim();
    }

    /**
     * @return name
     */
    public String getName()
    {
        return name;
    }

    /**
     * @param name
     */
    public void setName(String name)
    {
        this.name = name == null ? null : name.trim();
    }

    /**
     * @return sex
     */
    public String getSex()
    {
        return sex;
    }

    /**
     * @param sex
     */
    public void setSex(String sex)
    {
        this.sex = sex == null ? null : sex.trim();
    }
}