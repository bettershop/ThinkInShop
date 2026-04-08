package com.laiketui.domain.mch;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

@Table(name = "lkt_mch_store_account")
public class MchStoreAccountModel implements Serializable
{
    /**
     * ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * 商城id
     */
    private Integer store_id;

    /**
     * 店铺ID
     */
    private Integer mch_id;

    /**
     * 门店id
     */
    private Integer mch_store_id;

    /**
     * 账号
     */
    private String account_number;

    /**
     * 密码
     */
    private String password;

    /**
     * 添加时间
     */
    private Date add_date;

    /**
     * 上次登录时间
     */
    private Date last_login;

    /**
     * 是否回收
     */
    private Integer recycle;

    public Integer getRecycle()
    {
        return recycle;
    }

    public void setRecycle(Integer recycle)
    {
        this.recycle = recycle;
    }

    /**
     * 获取ID
     *
     * @return id - ID
     */
    public Integer getId()
    {
        return id;
    }

    /**
     * 设置ID
     *
     * @param id ID
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
     * 获取店铺ID
     *
     * @return mch_id - 店铺ID
     */
    public Integer getMch_id()
    {
        return mch_id;
    }

    /**
     * 设置店铺ID
     *
     * @param mch_id 店铺ID
     */
    public void setMch_id(Integer mch_id)
    {
        this.mch_id = mch_id;
    }

    /**
     * 获取门店id
     *
     * @return mch_store_id - 门店id
     */
    public Integer getMch_store_id()
    {
        return mch_store_id;
    }

    /**
     * 设置门店id
     *
     * @param mch_store_id 门店id
     */
    public void setMch_store_id(Integer mch_store_id)
    {
        this.mch_store_id = mch_store_id;
    }

    /**
     * 获取账号
     *
     * @return account_number - 账号
     */
    public String getAccount_number()
    {
        return account_number;
    }

    /**
     * 设置账号
     *
     * @param account_number 账号
     */
    public void setAccount_number(String account_number)
    {
        this.account_number = account_number == null ? null : account_number.trim();
    }

    /**
     * 获取密码
     *
     * @return password - 密码
     */
    public String getPassword()
    {
        return password;
    }

    /**
     * 设置密码
     *
     * @param password 密码
     */
    public void setPassword(String password)
    {
        this.password = password == null ? null : password.trim();
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
     * 获取上次登录时间
     *
     * @return last_login - 上次登录时间
     */
    public Date getLast_login()
    {
        return last_login;
    }

    /**
     * 设置上次登录时间
     *
     * @param last_login 上次登录时间
     */
    public void setLast_login(Date last_login)
    {
        this.last_login = last_login;
    }
}