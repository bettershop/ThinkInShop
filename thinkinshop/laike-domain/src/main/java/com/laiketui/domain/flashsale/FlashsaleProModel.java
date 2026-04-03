package com.laiketui.domain.flashsale;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

/**
 * 限时折扣商品表
 */
@Table(name = "lkt_flashsale_pro")
public class FlashsaleProModel implements Serializable
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
     * 插件活动id
     */
    private Integer activityId;

    /**
     * 商品ID
     */
    private Integer goodsId;

    /**
     * 规格id
     */
    private Integer attrId;

    /**
     * 活动库存
     */
    private Integer num;

    /**
     * 最大数量
     */
    private Integer maxNum;

    /**
     * 添加日期
     */
    private Date addTime;

    /**
     * 是否删除 1 是 0 否
     */
    private Integer isDelete;

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

    public Integer getActivityId()
    {
        return activityId;
    }

    public void setActivityId(Integer activityId)
    {
        this.activityId = activityId;
    }

    public Integer getGoodsId()
    {
        return goodsId;
    }

    public void setGoodsId(Integer goodsId)
    {
        this.goodsId = goodsId;
    }

    public Integer getAttrId()
    {
        return attrId;
    }

    public void setAttrId(Integer attrId)
    {
        this.attrId = attrId;
    }

    public Integer getNum()
    {
        return num;
    }

    public void setNum(Integer num)
    {
        this.num = num;
    }

    public Integer getMaxNum()
    {
        return maxNum;
    }

    public void setMaxNum(Integer maxNum)
    {
        this.maxNum = maxNum;
    }

    public Date getAddTime()
    {
        return addTime;
    }

    public void setAddTime(Date addTime)
    {
        this.addTime = addTime;
    }

    public Integer getIsDelete()
    {
        return isDelete;
    }

    public void setIsDelete(Integer isDelete)
    {
        this.isDelete = isDelete;
    }
}
