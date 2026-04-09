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
@Table(name = "lkt_flashsale_label")
public class FlashsaleLabelModel implements Serializable
{
    public interface STATUS
    {
        Integer WKS = 1;

        Integer JXZ = 2;

        Integer YJS = 3;
    }

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
     * 活动名称
     */
    private String name;

    /**
     * 活动状态 1 未开始 2 进行中 3结束
     */
    private Integer status;

    /**
     * 折扣
     */
    private BigDecimal discount;

    /**
     * 购买上限
     */
    private Integer buylimit;

    /**
     * 更新来源
     */
    private Integer update_source;

    /**
     *
     */
    private Integer isShow;

    /**
     * 开始时间
     */
    private Date starttime;

    /**
     * 结束时间
     */
    private Date endtime;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 活动说明
     */
    private String content;

    /**
     * 添加时间
     */
    private Date addDate;

    /**
     * 修改时间
     */
    private Date updateDate;

    /**
     * 是否回收 0 1
     */
    private Integer recovery;

    /**
     * 店铺id
     */
    private Integer mchId;

    public Integer getUpdate_source()
    {
        return update_source;
    }

    public void setUpdate_source(Integer update_source)
    {
        this.update_source = update_source;
    }

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

    public Integer getIsShow()
    {
        return isShow;
    }

    public void setIsShow(Integer isShow)
    {
        this.isShow = isShow;
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

    public Integer getSort()
    {
        return sort;
    }

    public void setSort(Integer sort)
    {
        this.sort = sort;
    }

    public String getContent()
    {
        return content;
    }

    public void setContent(String content)
    {
        this.content = content;
    }

    public Date getAddDate()
    {
        return addDate;
    }

    public void setAddDate(Date addDate)
    {
        this.addDate = addDate;
    }

    public Date getUpdateDate()
    {
        return updateDate;
    }

    public void setUpdateDate(Date updateDate)
    {
        this.updateDate = updateDate;
    }

    public Integer getRecovery()
    {
        return recovery;
    }

    public void setRecovery(Integer recovery)
    {
        this.recovery = recovery;
    }

    public Integer getMchId()
    {
        return mchId;
    }

    public void setMchId(Integer mchId)
    {
        this.mchId = mchId;
    }
}
