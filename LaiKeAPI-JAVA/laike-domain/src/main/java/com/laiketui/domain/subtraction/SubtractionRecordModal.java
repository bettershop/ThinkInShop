package com.laiketui.domain.subtraction;

import javax.persistence.*;
import java.util.Date;

@Table(name = "lkt_subtraction_record")
public class SubtractionRecordModal
{
    /**
     * ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * 满减活动ID
     */
    private Integer h_id;

    /**
     * 商品ID(目前没有用了)
     */
    @Transient
    private Integer p_id;

    /**
     * 订单号
     */
    @Column(name = "sNo")
    private String sNo;

    /**
     * 用户id
     */
    private String user_id;

    /**
     * 活动内容
     */
    private String content;

    /**
     * 添加时间
     */
    private Date add_date;

    public Integer getP_id()
    {
        return p_id;
    }

    public void setP_id(Integer p_id)
    {
        this.p_id = p_id;
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
     * 获取满减活动ID
     *
     * @return h_id - 满减活动ID
     */
    public Integer getH_id()
    {
        return h_id;
    }

    /**
     * 设置满减活动ID
     *
     * @param h_id 满减活动ID
     */
    public void setH_id(Integer h_id)
    {
        this.h_id = h_id;
    }

    /**
     * 获取订单号
     *
     * @return sNo - 订单号
     */

    public String getsNo()
    {
        return sNo;
    }

    /**
     * 设置订单号
     *
     * @param sNo 订单号
     */
    public void setsNo(String sNo)
    {
        this.sNo = sNo == null ? null : sNo.trim();
    }

    /**
     * 获取用户id
     *
     * @return user_id - 用户id
     */
    public String getUser_id()
    {
        return user_id;
    }

    /**
     * 设置用户id
     *
     * @param user_id 用户id
     */
    public void setUser_id(String user_id)
    {
        this.user_id = user_id == null ? null : user_id.trim();
    }

    /**
     * 获取活动内容
     *
     * @return content - 活动内容
     */
    public String getContent()
    {
        return content;
    }

    /**
     * 设置活动内容
     *
     * @param content 活动内容
     */
    public void setContent(String content)
    {
        this.content = content == null ? null : content.trim();
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