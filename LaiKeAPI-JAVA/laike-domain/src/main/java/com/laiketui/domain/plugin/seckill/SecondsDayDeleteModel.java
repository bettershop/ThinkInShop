package com.laiketui.domain.plugin.seckill;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

/**
 * @author Trick
 */
@Table(name = "lkt_seconds_day_delete")
public class SecondsDayDeleteModel implements Serializable
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * 秒杀活动id
     */
    private Integer activity_id;

    /**
     * 删除哪一天
     */
    private String day;

    /**
     * 删除时间
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
     * 获取秒杀活动id
     *
     * @return activity_id - 秒杀活动id
     */
    public Integer getActivity_id()
    {
        return activity_id;
    }

    /**
     * 设置秒杀活动id
     *
     * @param activity_id 秒杀活动id
     */
    public void setActivity_id(Integer activity_id)
    {
        this.activity_id = activity_id;
    }

    /**
     * 获取删除哪一天
     *
     * @return day - 删除哪一天
     */
    public String getDay()
    {
        return day;
    }

    /**
     * 设置删除哪一天
     *
     * @param day 删除哪一天
     */
    public void setDay(String day)
    {
        this.day = day == null ? null : day.trim();
    }

    /**
     * 获取删除时间
     *
     * @return add_time - 删除时间
     */
    public Date getAdd_time()
    {
        return add_time;
    }

    /**
     * 设置删除时间
     *
     * @param add_time 删除时间
     */
    public void setAdd_time(Date add_time)
    {
        this.add_time = add_time;
    }
}