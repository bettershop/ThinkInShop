package com.laiketui.domain.mch;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

@Table(name = "lkt_mch_store_write")
public class MchStoreWriteModel implements Serializable
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
     * 开始时间
     */
    private Date start_time;

    /**
     * 结束时间
     */
    private Date end_time;

    /**
     * 虚拟商品可核销次数 0无限制
     */
    private Integer write_off_num;

    /**
     * 已预约核销次数
     */
    private String off_num;

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
     * 添加时间
     */
    private Date add_time;

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

    public Date getStart_time()
    {
        return start_time;
    }

    public void setStart_time(Date start_time)
    {
        this.start_time = start_time;
    }

    public Date getEnd_time()
    {
        return end_time;
    }

    public void setEnd_time(Date end_time)
    {
        this.end_time = end_time;
    }

    public Integer getWrite_off_num()
    {
        return write_off_num;
    }

    public void setWrite_off_num(Integer write_off_num)
    {
        this.write_off_num = write_off_num;
    }

    public Date getAdd_time()
    {
        return add_time;
    }

    public void setAdd_time(Date add_time)
    {
        this.add_time = add_time;
    }

    public String getOff_num()
    {
        return off_num;
    }

    public void setOff_num(String off_num)
    {
        this.off_num = off_num;
    }
}
