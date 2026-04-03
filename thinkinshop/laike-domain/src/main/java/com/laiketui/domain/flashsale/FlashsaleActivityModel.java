package com.laiketui.domain.flashsale;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 限时折扣活动表
 */
@Table(name = "lkt_flashsale_activity")
public class FlashsaleActivityModel implements Serializable
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
     * 商品id
     */
    private Integer goodsId;

    /**
     * 活动id
     */
    private String labelId;

    /**
     * 折扣价格
     */
    private BigDecimal flashsalePrice;

    /**
     * 价格单位 0=百分比 1=固定值
     */
    private Integer priceType;

    /**
     * 折扣
     */
    private BigDecimal discount;

    /**
     * 购买上限
     */
    private Integer buylimit;

    /**
     * 活动库存
     */
    private Integer num;

    /**
     * 最大数量
     */
    private Integer maxNum;

    /**
     * 活动名称
     */
    private String name;

    /**
     * 活动状态 1 未开始 2 进行中 3结束
     */
    private Integer status;

    /**
     * 活动类型
     */
    private Integer type;

    /**
     * 开始时间
     */
    private Date starttime;

    /**
     * 结束时间
     */
    private Date endtime;

    /**
     * 是否显示 1 是 0 否
     */
    private Integer isshow;

    /**
     * 是否删除 1 是 0 否
     */
    private Integer isDelete;

    /**
     * 创建时间
     */
    private Date createDate;

    /**
     * 修改时间
     */
    private Date updateDate;

    /**
     * 是否通知 0=未通知 1=已通知
     */
    private Integer isNotice;

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

    public Integer getGoodsId()
    {
        return goodsId;
    }

    public void setGoodsId(Integer goodsId)
    {
        this.goodsId = goodsId;
    }

    public String getLabelId()
    {
        return labelId;
    }

    public void setLabelId(String labelId)
    {
        this.labelId = labelId;
    }

    public BigDecimal getFlashsalePrice()
    {
        return flashsalePrice;
    }

    public void setFlashsalePrice(BigDecimal flashsalePrice)
    {
        this.flashsalePrice = flashsalePrice;
    }

    public Integer getPriceType()
    {
        return priceType;
    }

    public void setPriceType(Integer priceType)
    {
        this.priceType = priceType;
    }

    public BigDecimal getDiscount()
    {
        return discount;
    }

    public void setDiscount(BigDecimal discount)
    {
        this.discount = discount;
    }

    public Integer getBuylimit()
    {
        return buylimit;
    }

    public void setBuylimit(Integer buylimit)
    {
        this.buylimit = buylimit;
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

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public Integer getStatus()
    {
        return status;
    }

    public void setStatus(Integer status)
    {
        this.status = status;
    }

    public Integer getType()
    {
        return type;
    }

    public void setType(Integer type)
    {
        this.type = type;
    }

    public Date getStarttime()
    {
        return starttime;
    }

    public void setStarttime(Date starttime)
    {
        this.starttime = starttime;
    }

    public Date getEndtime()
    {
        return endtime;
    }

    public void setEndtime(Date endtime)
    {
        this.endtime = endtime;
    }

    public Integer getIsshow()
    {
        return isshow;
    }

    public void setIsshow(Integer isshow)
    {
        this.isshow = isshow;
    }

    public Integer getIsDelete()
    {
        return isDelete;
    }

    public void setIsDelete(Integer isDelete)
    {
        this.isDelete = isDelete;
    }

    public Date getCreateDate()
    {
        return createDate;
    }

    public void setCreateDate(Date createDate)
    {
        this.createDate = createDate;
    }

    public Date getUpdateDate()
    {
        return updateDate;
    }

    public void setUpdateDate(Date updateDate)
    {
        this.updateDate = updateDate;
    }

    public Integer getIsNotice()
    {
        return isNotice;
    }

    public void setIsNotice(Integer isNotice)
    {
        this.isNotice = isNotice;
    }
}
