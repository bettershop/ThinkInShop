package com.laiketui.domain.systems;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

@Table(name = "lkt_system_tell_user")
public class SystemTellUserModel implements Serializable
{

    public interface IS_SUPPLIER
    {
        int ok = 1;
        int no = 0;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * 商城ID
     */
    private Integer store_id;

    /**
     * 公告id
     */
    private Integer tell_id;

    /**
     * 标记以读用户id
     */
    private String read_id;

    /**
     * 标记以读用户所在端 1微信小程序 , 2 h5, 11 app, 7 pc店铺, 6 pc商城 8 管理后台 9 PC门店核销, 10H5门店核销
     */
    private Integer store_type;

    /**
     * 是否为供应商  0不是  1是
     */
    private Integer isSupplier;

    /**
     * 添加时间
     */
    private Date add_time;

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
     * 获取公告id
     *
     * @return tell_id - 公告id
     */
    public Integer getTell_id()
    {
        return tell_id;
    }

    /**
     * 设置公告id
     *
     * @param tell_id 公告id
     */
    public void setTell_id(Integer tell_id)
    {
        this.tell_id = tell_id;
    }

    public String getRead_id()
    {
        return read_id;
    }

    public void setRead_id(String read_id)
    {
        this.read_id = read_id;
    }

    public Integer getStore_type()
    {
        return store_type;
    }

    public void setStore_type(Integer store_type)
    {
        this.store_type = store_type;
    }

    /**
     * 获取添加时间
     *
     * @return add_time - 添加时间
     */
    public Date getAdd_time()
    {
        return add_time;
    }

    /**
     * 设置添加时间
     *
     * @param add_time 添加时间
     */
    public void setAdd_time(Date add_time)
    {
        this.add_time = add_time;
    }

    public Integer getIsSupplier()
    {
        return isSupplier;
    }

    public void setIsSupplier(Integer isSupplier)
    {
        this.isSupplier = isSupplier;
    }
}
