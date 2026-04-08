package com.laiketui.domain.seckill;

import javax.persistence.*;
import java.io.Serializable;

/**
 * 秒杀配置表
 *
 * @author Trick
 * @date 2023/3/7 16:24
 */
@Table(name = "lkt_seconds_config")
public class SecondsConfigModel implements Serializable
{
    /**
     * 主键自增
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * 店铺id
     */
    private Integer store_id;

    /**
     * 是否开启 1 是 0 否
     */
    private Integer is_open;

    /**
     * 秒杀商品默认限购数量
     */
    private Integer buy_num;

    /**
     * 秒杀活动提醒 （单位：秒） [支持多个 2023-03-07 14:37:39]
     */
    private String remind;

    /**
     * 轮播图
     */
    private String imageurl;

    /**
     * 规则
     */
    private String  rule;
    /**
     * PC端秒杀广告图设置
     */
    @Column(name = "pcAdImg")
    private String  pcAdImg;
    /**
     * 是否开启预告
     */
    private Integer is_herald;

    /**
     * 预告时间
     */
    @Column(name = "heraldTime")
    private Integer heraldTime;

    /**
     * 订单失效 (单位 秒)
     */
    private Integer order_failure;

    /**
     * 订单售后时间 (单位秒) 默认7天
     */
    private Integer order_after;
    /**
     * 自动收货时间 (单位秒) 默认7天
     */
    private Integer auto_the_goods;
    /**
     * 提醒发货限制 间隔(单位 秒) 默认1小时
     */
    private Integer deliver_remind;
    /**
     * 多件包邮设置 0.未开启 1.开启
     */
    private Integer package_settings;
    /**
     * 同件
     */
    private Integer same_piece;
    /**
     * 自动评价设置几后自动好评
     */
    private Integer auto_good_comment_day;
    /**
     * 自动评价设置几后自动好评内容
     */
    private String  auto_good_comment_content;

    public String getAuto_good_comment_content()
    {
        return auto_good_comment_content;
    }

    public void setAuto_good_comment_content(String auto_good_comment_content)
    {
        this.auto_good_comment_content = auto_good_comment_content;
    }

    /**
     * 店铺id
     */
    private Integer mch_id;

    public Integer getMch_id()
    {
        return mch_id;
    }

    public void setMch_id(Integer mch_id)
    {
        this.mch_id = mch_id;
    }

    public Integer getOrder_failure()
    {
        return order_failure;
    }

    public void setOrder_failure(Integer order_failure)
    {
        this.order_failure = order_failure;
    }

    public Integer getOrder_after()
    {
        return order_after;
    }

    public void setOrder_after(Integer order_after)
    {
        this.order_after = order_after;
    }

    public Integer getAuto_the_goods()
    {
        return auto_the_goods;
    }

    public void setAuto_the_goods(Integer auto_the_goods)
    {
        this.auto_the_goods = auto_the_goods;
    }

    public Integer getDeliver_remind()
    {
        return deliver_remind;
    }

    public void setDeliver_remind(Integer deliver_remind)
    {
        this.deliver_remind = deliver_remind;
    }

    public Integer getPackage_settings()
    {
        return package_settings;
    }

    public void setPackage_settings(Integer package_settings)
    {
        this.package_settings = package_settings;
    }

    public Integer getSame_piece()
    {
        return same_piece;
    }

    public void setSame_piece(Integer same_piece)
    {
        this.same_piece = same_piece;
    }

    public Integer getAuto_good_comment_day()
    {
        return auto_good_comment_day;
    }

    public void setAuto_good_comment_day(Integer auto_good_comment_day)
    {
        this.auto_good_comment_day = auto_good_comment_day;
    }

    public Integer getIs_herald()
    {
        return is_herald;
    }

    public void setIs_herald(Integer is_herald)
    {
        this.is_herald = is_herald;
    }

    public Integer getHeraldTime()
    {
        return heraldTime;
    }

    public void setHeraldTime(Integer heraldTime)
    {
        this.heraldTime = heraldTime;
    }

    public String getPcAdImg()
    {
        return pcAdImg;
    }

    public void setPcAdImg(String pcAdImg)
    {
        this.pcAdImg = pcAdImg;
    }

    /**
     * 获取主键自增
     *
     * @return id - 主键自增
     */
    public Integer getId()
    {
        return id;
    }

    /**
     * 设置主键自增
     *
     * @param id 主键自增
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
     * 获取是否开启 1 是 0 否
     *
     * @return is_open - 是否开启 1 是 0 否
     */
    public Integer getIs_open()
    {
        return is_open;
    }

    /**
     * 设置是否开启 1 是 0 否
     *
     * @param is_open 是否开启 1 是 0 否
     */
    public void setIs_open(Integer is_open)
    {
        this.is_open = is_open;
    }

    /**
     * 获取秒杀商品默认限购数量
     *
     * @return buy_num - 秒杀商品默认限购数量
     */
    public Integer getBuy_num()
    {
        return buy_num;
    }

    /**
     * 设置秒杀商品默认限购数量
     *
     * @param buy_num 秒杀商品默认限购数量
     */
    public void setBuy_num(Integer buy_num)
    {
        this.buy_num = buy_num;
    }

    /**
     * 获取秒杀活动提醒 （单位：秒）
     *
     * @return remind - 秒杀活动提醒 （单位：秒）
     */
    public String getRemind()
    {
        return remind;
    }

    /**
     * 设置秒杀活动提醒 （单位：秒）
     *
     * @param remind 秒杀活动提醒 （单位：秒）
     */
    public void setRemind(String remind)
    {
        this.remind = remind;
    }

    public String getImageurl()
    {
        return imageurl;
    }

    public void setImageurl(String imageurl)
    {
        this.imageurl = imageurl;
    }

    public String getRule()
    {
        return rule;
    }

    public void setRule(String rule)
    {
        this.rule = rule;
    }
}