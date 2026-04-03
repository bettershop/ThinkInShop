package com.laiketui.domain.flashsale;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 限时折扣记录表
 */
@Table(name = "lkt_flashsale_record")
public class FlashsaleRecordModel implements Serializable
{

    /**
     * 主键自增
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * 商城id
     */
    private Integer storeId;

    /**
     * 用户id
     */
    private String userId;

    /**
     * 活动id
     */
    private Integer activityId;

    /**
     * 时段id
     */
    private Integer timeId;

    /**
     * 商品id
     */
    private Integer proId;

    /**
     * 规格id
     */
    private Integer attrId;

    /**
     * 限时折扣id
     */
    private Integer secId;

    /**
     * 价格
     */
    private BigDecimal price;

    /**
     * 数量
     */
    private Integer num;

    /**
     * 限时折扣订单
     */
    @Column(name = "sno")
    private String sNo;

    /**
     * 是否删除 1是 0否
     */
    private Integer isDelete;

    private Date addTime;

    public Integer getId()
    {
        return id;
    }

    public void setId(Integer id)
    {
        this.id = id;
    }

    public Integer getStoreId()
    {
        return storeId;
    }

    public void setStoreId(Integer storeId)
    {
        this.storeId = storeId;
    }

    public String getUserId()
    {
        return userId;
    }

    public void setUserId(String userId)
    {
        this.userId = userId;
    }

    public Integer getActivityId()
    {
        return activityId;
    }

    public void setActivityId(Integer activityId)
    {
        this.activityId = activityId;
    }

    public Integer getTimeId()
    {
        return timeId;
    }

    public void setTimeId(Integer timeId)
    {
        this.timeId = timeId;
    }

    public Integer getProId()
    {
        return proId;
    }

    public void setProId(Integer proId)
    {
        this.proId = proId;
    }

    public Integer getAttrId()
    {
        return attrId;
    }

    public void setAttrId(Integer attrId)
    {
        this.attrId = attrId;
    }

    public Integer getSecId()
    {
        return secId;
    }

    public void setSecId(Integer secId)
    {
        this.secId = secId;
    }

    public BigDecimal getPrice()
    {
        return price;
    }

    public void setPrice(BigDecimal price)
    {
        this.price = price;
    }

    public Integer getNum()
    {
        return num;
    }

    public void setNum(Integer num)
    {
        this.num = num;
    }

    public String getsNo()
    {
        return sNo;
    }

    public void setsNo(String sNo)
    {
        this.sNo = sNo;
    }

    public Integer getIsDelete()
    {
        return isDelete;
    }

    public void setIsDelete(Integer isDelete)
    {
        this.isDelete = isDelete;
    }

    public Date getAddTime()
    {
        return addTime;
    }

    public void setAddTime(Date addTime)
    {
        this.addTime = addTime;
    }
}
