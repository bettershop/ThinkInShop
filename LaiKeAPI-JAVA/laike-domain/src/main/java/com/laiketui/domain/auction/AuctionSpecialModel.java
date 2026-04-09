package com.laiketui.domain.auction;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 竞拍专场
 *
 * @author Trick
 * @date 2022/7/1 16:55
 */
@Table(name = "lkt_auction_special")
public class AuctionSpecialModel implements Serializable
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
     * 专场类型 1=店铺专场 2=普通专场 3=报名专场
     */
    public interface SpecialType
    {
        /**
         * 店铺专场
         */
        int TYPE_MCH  = 1;
        /**
         * 普通专场
         */
        int TYPE_PT   = 2;
        /**
         * 报名专场
         */
        int TYPE_SIGN = 3;
    }

    /**
     * 起拍价单位设置
     */
    public interface SpecialUnit
    {
        /**
         * 固定金额
         */
        int UNIT_FIXED      = 1;
        /**
         * 百分比
         */
        int UNIT_PERCENTAGE = 2;
    }

    /**
     * id
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;

    /**
     * 商城id
     */
    private Integer store_id;

    /**
     * 店铺id
     */
    private Integer mch_id;

    /**
     * 专场名称
     */
    private String name;

    /**
     * 专场图片
     */
    private String img;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 佣金
     */
    private BigDecimal commission;

    /**
     * 保证金金额
     */
    private BigDecimal promise_amt;

    /**
     * 围观人数
     */
    private Integer look_count;

    /**
     * 开始时间
     */
    private Date start_date;

    /**
     * 结束时间
     */
    private Date end_date;

    /**
     * 报名截至时间
     */
    private Date sign_end_date;

    /**
     * 1=未开始 2=进行中 3=已结束
     */
    private Integer status;

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
     * 专场预告
     */
    private String content;

    /**
     * 专场类型 1=店铺专场 2=普通专场 3=报名专场
     */
    private Integer type;
    /**
     * 是否结算 0=否 1=是
     */
    private Integer is_settlement;
    /**
     * 是否显示 0=否 1=是
     */
    private Integer is_show;


    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }

    public Integer getIs_show()
    {
        return is_show;
    }

    public void setIs_show(Integer is_show)
    {
        this.is_show = is_show;
    }

    /**
     * 是否结算 0=否 1=是
     */
    public Integer getIs_settlement()
    {
        return is_settlement;
    }

    /**
     * 是否结算 0=否 1=是
     */
    public void setIs_settlement(Integer is_settlement)
    {
        this.is_settlement = is_settlement;
    }

    /**
     * 专场类型 1=店铺专场 2=普通专场 3=报名专场
     */
    public Integer getType()
    {
        return type;
    }

    /**
     * 专场类型 1=店铺专场 2=普通专场 3=报名专场
     */
    public void setType(Integer type)
    {
        this.type = type;
    }

    /**
     * 起拍价默认单位 1=固定金额 2=价格百分比
     */
    private Integer unit;

    /**
     * 起拍价默认单位 1=固定金额 2=价格百分比
     */
    public Integer getUnit()
    {
        return unit;
    }

    /**
     * 起拍价默认单位 1=固定金额 2=价格百分比
     */
    public void setUnit(Integer unit)
    {
        this.unit = unit;
    }

    /**
     * 起拍价默认值
     */
    private BigDecimal number;

    /**
     * 起拍价默认值
     */
    public BigDecimal getNumber()
    {
        return number;
    }

    /**
     * 起拍价默认值
     */
    public void setNumber(BigDecimal number)
    {
        this.number = number;
    }

    /**
     * 默认加价金额
     */
    private BigDecimal mark_up_amt;

    public BigDecimal getMark_up_amt()
    {
        return mark_up_amt;
    }

    public void setMark_up_amt(BigDecimal mark_up_amt)
    {
        this.mark_up_amt = mark_up_amt;
    }

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
     * 获取商城id
     *
     * @return store_id - 商城id
     */
    public Integer getStore_id()
    {
        return store_id;
    }

    /**
     * 设置商城id
     *
     * @param store_id 商城id
     */
    public void setStore_id(Integer store_id)
    {
        this.store_id = store_id;
    }

    /**
     * 获取店铺id
     *
     * @return mch_id - 店铺id
     */
    public Integer getMch_id()
    {
        return mch_id;
    }

    /**
     * 设置店铺id
     *
     * @param mch_id 店铺id
     */
    public void setMch_id(Integer mch_id)
    {
        this.mch_id = mch_id;
    }

    /**
     * 获取专场名称
     *
     * @return name - 专场名称
     */
    public String getName()
    {
        return name;
    }

    /**
     * 设置专场名称
     *
     * @param name 专场名称
     */
    public void setName(String name)
    {
        this.name = name == null ? null : name.trim();
    }

    /**
     * 获取专场图片
     *
     * @return img - 专场图片
     */
    public String getImg()
    {
        return img;
    }

    /**
     * 设置专场图片
     *
     * @param img 专场图片
     */
    public void setImg(String img)
    {
        this.img = img == null ? null : img.trim();
    }

    /**
     * 获取佣金
     *
     * @return commission - 佣金
     */
    public BigDecimal getCommission()
    {
        return commission;
    }

    /**
     * 设置佣金
     *
     * @param commission 佣金
     */
    public void setCommission(BigDecimal commission)
    {
        this.commission = commission;
    }

    /**
     * 获取保证金金额
     *
     * @return promise_amt - 保证金金额
     */
    public BigDecimal getPromise_amt()
    {
        return promise_amt;
    }

    /**
     * 设置保证金金额
     *
     * @param promise_amt 保证金金额
     */
    public void setPromise_amt(BigDecimal promise_amt)
    {
        this.promise_amt = promise_amt;
    }

    /**
     * 获取围观人数
     *
     * @return look_count - 围观人数
     */
    public Integer getLook_count()
    {
        return look_count;
    }

    /**
     * 设置围观人数
     *
     * @param look_count 围观人数
     */
    public void setLook_count(Integer look_count)
    {
        this.look_count = look_count;
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
     * 获取报名截至时间
     *
     * @return sign_end_date - 报名截至时间
     */
    public Date getSign_end_date()
    {
        return sign_end_date;
    }

    /**
     * 设置报名截至时间
     *
     * @param sign_end_date 报名截至时间
     */
    public void setSign_end_date(Date sign_end_date)
    {
        this.sign_end_date = sign_end_date;
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

    /**
     * 获取专场预告
     *
     * @return content - 专场预告
     */
    public String getContent()
    {
        return content;
    }

    /**
     * 设置专场预告
     *
     * @param content 专场预告
     */
    public void setContent(String content)
    {
        this.content = content == null ? null : content.trim();
    }
}