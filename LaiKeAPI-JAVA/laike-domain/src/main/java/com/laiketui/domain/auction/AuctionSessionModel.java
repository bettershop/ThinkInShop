package com.laiketui.domain.auction;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

/**
 * 竞拍场次表
 *
 * @author Trick
 * @date 2022/7/1 18:52
 */
@Table(name = "lkt_auction_session")
public class AuctionSessionModel implements Serializable
{

    /**
     * 竞拍活动状态 1=未开始 2=进行中 3=已结束
     */
    public interface AuctionStatus
    {
        /**
         * 未开始
         */
        Integer STATUS_NOT_STARTED = 1;
        /**
         * 进行中
         */
        Integer STATUS_STARTED     = 2;
        /**
         * 已结束
         */
        Integer STATUS_END_STARTED = 3;
    }

    /**
     * id
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;

    /**
     * 专场id
     */
    private String special_id;

    /**
     * 场次名称
     */
    private String name;

    /**
     * 开始时间
     */
    private Date start_date;

    /**
     * 结束时间
     */
    private Date end_date;

    /**
     * 1=未开始 2=进行中 3=已结束
     */
    private Integer status;

    /**
     * 是否显示
     */
    private Integer is_show;

    /**
     * 回收标识
     */
    private Integer recovery;

    /**
     * 添加时间
     */
    private Date add_date;

    private Date update_date;

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
     * 获取专场id
     *
     * @return special_id - 专场id
     */
    public String getSpecial_id()
    {
        return special_id;
    }

    /**
     * 设置专场id
     *
     * @param special_id 专场id
     */
    public void setSpecial_id(String special_id)
    {
        this.special_id = special_id == null ? null : special_id.trim();
    }

    /**
     * 获取场次名称
     *
     * @return name - 场次名称
     */
    public String getName()
    {
        return name;
    }

    /**
     * 设置场次名称
     *
     * @param name 场次名称
     */
    public void setName(String name)
    {
        this.name = name == null ? null : name.trim();
    }

    /**
     * 获取开始时间
     *
     * @return start_date - 开始时间
     */
    public Date getStart_date()
    {
        return start_date;
    }

    /**
     * 设置开始时间
     *
     * @param start_date 开始时间
     */
    public void setStart_date(Date start_date)
    {
        this.start_date = start_date;
    }

    /**
     * 获取结束时间
     *
     * @return end_date - 结束时间
     */
    public Date getEnd_date()
    {
        return end_date;
    }

    /**
     * 设置结束时间
     *
     * @param end_date 结束时间
     */
    public void setEnd_date(Date end_date)
    {
        this.end_date = end_date;
    }

    /**
     * 获取1=未开始 2=进行中 3=已结束
     *
     * @return status - 1=未开始 2=进行中 3=已结束
     */
    public Integer getStatus()
    {
        return status;
    }

    /**
     * 设置1=未开始 2=进行中 3=已结束
     *
     * @param status 1=未开始 2=进行中 3=已结束
     */
    public void setStatus(Integer status)
    {
        this.status = status;
    }

    /**
     * 获取是否显示
     *
     * @return is_show - 是否显示
     */
    public Integer getIs_show()
    {
        return is_show;
    }

    /**
     * 设置是否显示
     *
     * @param is_show 是否显示
     */
    public void setIs_show(Integer is_show)
    {
        this.is_show = is_show;
    }

    /**
     * 获取回收标识
     *
     * @return recovery - 回收标识
     */
    public Integer getRecovery()
    {
        return recovery;
    }

    /**
     * 设置回收标识
     *
     * @param recovery 回收标识
     */
    public void setRecovery(Integer recovery)
    {
        this.recovery = recovery;
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
     * @return update_date
     */
    public Date getUpdate_date()
    {
        return update_date;
    }

    /**
     * @param update_date
     */
    public void setUpdate_date(Date update_date)
    {
        this.update_date = update_date;
    }
}