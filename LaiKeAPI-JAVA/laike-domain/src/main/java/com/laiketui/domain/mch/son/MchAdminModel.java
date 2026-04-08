package com.laiketui.domain.mch.son;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

@Table(name = "lkt_mch_admin")
public class MchAdminModel implements Serializable
{
    /**
     * id
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * 商城ID
     */
    private Integer store_id;

    /**
     * 店铺ID
     */
    private Integer mch_id;

    /**
     * 店铺门店ID
     */
    private Integer mch_store_id;

    /**
     * IP
     */
    private String ip;

    /**
     * 授权id
     */
    private String access_id;

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
     * 回收站 0:不回收 1:回收
     */
    private Integer recycle;

    /**
     * 最后一次登录时间
     */
    private Date last_time;

    public Integer getId()
    {
        return id;
    }

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

    public Integer getMch_id()
    {
        return mch_id;
    }

    public void setMch_id(Integer mch_id)
    {
        this.mch_id = mch_id;
    }

    public Integer getMch_store_id()
    {
        return mch_store_id;
    }

    public void setMch_store_id(Integer mch_store_id)
    {
        this.mch_store_id = mch_store_id;
    }

    public String getIp()
    {
        return ip;
    }

    public void setIp(String ip)
    {
        this.ip = ip;
    }

    public String getAccess_id()
    {
        return access_id;
    }

    public void setAccess_id(String access_id)
    {
        this.access_id = access_id;
    }

    public String getAccount_number()
    {
        return account_number;
    }

    public void setAccount_number(String account_number)
    {
        this.account_number = account_number;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public Date getAdd_date()
    {
        return add_date;
    }

    public void setAdd_date(Date add_date)
    {
        this.add_date = add_date;
    }

    public Integer getRecycle()
    {
        return recycle;
    }

    public void setRecycle(Integer recycle)
    {
        this.recycle = recycle;
    }

    public Date getLast_time()
    {
        return last_time;
    }

    public void setLast_time(Date last_time)
    {
        this.last_time = last_time;
    }
}
