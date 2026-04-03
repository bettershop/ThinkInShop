package com.laiketui.domain.mch.son;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Table(name = "lkt_mch_admin_record")
public class MchAdminRecordModel implements Serializable
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
     * 管理员ID
     */
    private Integer administrators_id;

    /**
     * 订单号
     */
    @Column(name = "sNo")
    private String sNo;

    /**
     * 添加时间
     */
    private Date add_date;

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

    public Integer getAdministrators_id()
    {
        return administrators_id;
    }

    public void setAdministrators_id(Integer administrators_id)
    {
        this.administrators_id = administrators_id;
    }

    public String getsNo()
    {
        return sNo;
    }

    public void setsNo(String sNo)
    {
        this.sNo = sNo;
    }

    public Date getAdd_date()
    {
        return add_date;
    }

    public void setAdd_date(Date add_date)
    {
        this.add_date = add_date;
    }
}
