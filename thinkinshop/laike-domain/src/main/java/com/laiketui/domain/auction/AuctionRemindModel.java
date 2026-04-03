package com.laiketui.domain.auction;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

@Table(name = "lkt_auction_remind")
public class AuctionRemindModel implements Serializable
{
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
     * 提醒人
     */
    private String user_id;

    /**
     * 是否已提醒
     */
    private Integer is_remind;

    /**
     * 添加时间
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
     * 获取提醒人
     *
     * @return user_id - 提醒人
     */
    public String getUser_id()
    {
        return user_id;
    }

    /**
     * 设置提醒人
     *
     * @param user_id 提醒人
     */
    public void setUser_id(String user_id)
    {
        this.user_id = user_id == null ? null : user_id.trim();
    }

    /**
     * 获取是否已提醒
     *
     * @return is_remind - 是否已提醒
     */
    public Integer getIs_remind()
    {
        return is_remind;
    }

    /**
     * 设置是否已提醒
     *
     * @param is_remind 是否已提醒
     */
    public void setIs_remind(Integer is_remind)
    {
        this.is_remind = is_remind;
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
}