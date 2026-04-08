package com.laiketui.domain.plugin.seckill;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Table(name = "lkt_pt_seconds_pro")
public class PtSecondsProModel implements Serializable
{
    /**
     * id自增
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * 店铺id
     */
    private Integer store_id;

    /**
     * 商品id
     */
    private Integer pro_id;

    /**
     * 规格id
     */
    private Integer attr_id;

    /**
     * 秒杀价格
     */
    private BigDecimal seconds_price;

    /**
     * 秒杀库存
     */
    private Integer num;

    /**
     * 最大数量
     */
    private Integer max_num;

    /**
     * 限购数量
     */
    private Integer buy_num;

    /**
     * 活动id
     */
    private Integer activity_id;

    /**
     * 开始时间
     */
    private Date starttime;

    /**
     * 结束时间
     */
    private Date endtime;

    /**
     * 状态 1未开始 2进行中 3结束
     */
    private Integer status;

    /**
     * 时段id
     */
    private Integer time_id;

    /**
     * 平台活动id
     */
    private Integer platform_activities_id;

    /**
     * 店铺id
     */
    private Integer mch_id;

    /**
     * 审核状态
     */
    private Integer audit_status;

    /**
     * 审核失败原因
     */
    private String reason;

    /**
     * 添加日期
     */
    private Date add_time;

    /**
     * 是否显示 1显示 0不显示
     */
    private Integer is_show;

    /**
     * 是否删除 1 是 0 否
     */
    private Integer is_delete;

    /**
     * 是否重置数量
     */
    private Integer resetting;

    /**
     * 是否免邮(1 是 0 否)默认0
     */
    private Integer free_freight;

    /**
     * 获取id自增
     *
     * @return id - id自增
     */
    public Integer getId()
    {
        return id;
    }

    /**
     * 设置id自增
     *
     * @param id id自增
     */
    public void setId(Integer id)
    {
        this.id = id;
    }

    /**
     * 获取店铺id
     *
     * @return store_id - 店铺id
     */
    public Integer getStore_id()
    {
        return store_id;
    }

    /**
     * 设置店铺id
     *
     * @param store_id 店铺id
     */
    public void setStore_id(Integer store_id)
    {
        this.store_id = store_id;
    }

    /**
     * 获取商品id
     *
     * @return pro_id - 商品id
     */
    public Integer getPro_id()
    {
        return pro_id;
    }

    /**
     * 设置商品id
     *
     * @param pro_id 商品id
     */
    public void setPro_id(Integer pro_id)
    {
        this.pro_id = pro_id;
    }

    /**
     * 获取规格id
     *
     * @return attr_id - 规格id
     */
    public Integer getAttr_id()
    {
        return attr_id;
    }

    /**
     * 设置规格id
     *
     * @param attr_id 规格id
     */
    public void setAttr_id(Integer attr_id)
    {
        this.attr_id = attr_id;
    }

    /**
     * 获取秒杀价格
     *
     * @return seconds_price - 秒杀价格
     */
    public BigDecimal getSeconds_price()
    {
        return seconds_price;
    }

    /**
     * 设置秒杀价格
     *
     * @param seconds_price 秒杀价格
     */
    public void setSeconds_price(BigDecimal seconds_price)
    {
        this.seconds_price = seconds_price;
    }

    /**
     * 获取秒杀库存
     *
     * @return num - 秒杀库存
     */
    public Integer getNum()
    {
        return num;
    }

    /**
     * 设置秒杀库存
     *
     * @param num 秒杀库存
     */
    public void setNum(Integer num)
    {
        this.num = num;
    }

    /**
     * 获取最大数量
     *
     * @return max_num - 最大数量
     */
    public Integer getMax_num()
    {
        return max_num;
    }

    /**
     * 设置最大数量
     *
     * @param max_num 最大数量
     */
    public void setMax_num(Integer max_num)
    {
        this.max_num = max_num;
    }

    /**
     * 获取限购数量
     *
     * @return buy_num - 限购数量
     */
    public Integer getBuy_num()
    {
        return buy_num;
    }

    /**
     * 设置限购数量
     *
     * @param buy_num 限购数量
     */
    public void setBuy_num(Integer buy_num)
    {
        this.buy_num = buy_num;
    }

    /**
     * 获取活动id
     *
     * @return activity_id - 活动id
     */
    public Integer getActivity_id()
    {
        return activity_id;
    }

    /**
     * 设置活动id
     *
     * @param activity_id 活动id
     */
    public void setActivity_id(Integer activity_id)
    {
        this.activity_id = activity_id;
    }

    /**
     * 获取开始时间
     *
     * @return starttime - 开始时间
     */
    public Date getStarttime()
    {
        return starttime;
    }

    /**
     * 设置开始时间
     *
     * @param starttime 开始时间
     */
    public void setStarttime(Date starttime)
    {
        this.starttime = starttime;
    }

    /**
     * 获取结束时间
     *
     * @return endtime - 结束时间
     */
    public Date getEndtime()
    {
        return endtime;
    }

    /**
     * 设置结束时间
     *
     * @param endtime 结束时间
     */
    public void setEndtime(Date endtime)
    {
        this.endtime = endtime;
    }

    /**
     * 获取状态 1未开始 2进行中 3结束
     *
     * @return status - 状态 1未开始 2进行中 3结束
     */
    public Integer getStatus()
    {
        return status;
    }

    /**
     * 设置状态 1未开始 2进行中 3结束
     *
     * @param status 状态 1未开始 2进行中 3结束
     */
    public void setStatus(Integer status)
    {
        this.status = status;
    }

    /**
     * 获取时段id
     *
     * @return time_id - 时段id
     */
    public Integer getTime_id()
    {
        return time_id;
    }

    /**
     * 设置时段id
     *
     * @param time_id 时段id
     */
    public void setTime_id(Integer time_id)
    {
        this.time_id = time_id;
    }

    /**
     * 获取平台活动id
     *
     * @return platform_activities_id - 平台活动id
     */
    public Integer getPlatform_activities_id()
    {
        return platform_activities_id;
    }

    /**
     * 设置平台活动id
     *
     * @param platform_activities_id 平台活动id
     */
    public void setPlatform_activities_id(Integer platform_activities_id)
    {
        this.platform_activities_id = platform_activities_id;
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
     * 获取审核状态
     *
     * @return audit_status - 审核状态
     */
    public Integer getAudit_status()
    {
        return audit_status;
    }

    /**
     * 设置审核状态
     *
     * @param audit_status 审核状态
     */
    public void setAudit_status(Integer audit_status)
    {
        this.audit_status = audit_status;
    }

    /**
     * 获取审核失败原因
     *
     * @return reason - 审核失败原因
     */
    public String getReason()
    {
        return reason;
    }

    /**
     * 设置审核失败原因
     *
     * @param reason 审核失败原因
     */
    public void setReason(String reason)
    {
        this.reason = reason == null ? null : reason.trim();
    }

    /**
     * 获取添加日期
     *
     * @return add_time - 添加日期
     */
    public Date getAdd_time()
    {
        return add_time;
    }

    /**
     * 设置添加日期
     *
     * @param add_time 添加日期
     */
    public void setAdd_time(Date add_time)
    {
        this.add_time = add_time;
    }

    /**
     * 获取是否显示 1显示 0不显示
     *
     * @return is_show - 是否显示 1显示 0不显示
     */
    public Integer getIs_show()
    {
        return is_show;
    }

    /**
     * 设置是否显示 1显示 0不显示
     *
     * @param is_show 是否显示 1显示 0不显示
     */
    public void setIs_show(Integer is_show)
    {
        this.is_show = is_show;
    }

    /**
     * 获取是否删除 1 是 0 否
     *
     * @return is_delete - 是否删除 1 是 0 否
     */
    public Integer getIs_delete()
    {
        return is_delete;
    }

    /**
     * 设置是否删除 1 是 0 否
     *
     * @param is_delete 是否删除 1 是 0 否
     */
    public void setIs_delete(Integer is_delete)
    {
        this.is_delete = is_delete;
    }

    /**
     * 获取是否重置数量
     *
     * @return resetting - 是否重置数量
     */
    public Integer getResetting()
    {
        return resetting;
    }

    /**
     * 设置是否重置数量
     *
     * @param resetting 是否重置数量
     */
    public void setResetting(Integer resetting)
    {
        this.resetting = resetting;
    }

    /**
     * 获取是否免邮(1 是 0 否)默认0
     *
     * @return free_freight - 是否免邮(1 是 0 否)默认0
     */
    public Integer getFree_freight()
    {
        return free_freight;
    }

    /**
     * 设置是否免邮(1 是 0 否)默认0
     *
     * @param free_freight 是否免邮(1 是 0 否)默认0
     */
    public void setFree_freight(Integer free_freight)
    {
        this.free_freight = free_freight;
    }
}