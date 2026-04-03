package com.laiketui.domain.seckill;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 秒杀活动表
 *
 * @author Trick
 */
@Table(name = "lkt_seconds_activity")
public class SecondsActivityModel implements Serializable
{


    /**
     * 显示活动
     */
    public static final Integer IS_SHOW_OK = 1;

    /**
     * 不显示活动
     */
    public static final Integer IS_SHOW_NO = 0;

    /**
     * 秒杀状态
     * DictionaryConst.Seckill
     */
    public interface SecondsStatus
    {
        /**
         * 未开始
         */
        int SECKILL_STATUS_NOT_START = 1;
        /**
         * 进行中
         */
        int SECKILL_STATUS_START     = 2;
        /**
         * 秒杀结束
         */
        int SECKILL_STATUS_END       = 3;
        /**
         * 秒杀预告
         */
        int SECKILL_STATUS_HERALD    = 4;
        /**
         * 已抢光
         */
        int SECKILL_STATUS_TOOT_ALL  = 5;
    }

    /**
     * id自增
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * 商城id
     */
    private Integer store_id;

    /**
     * 秒杀标签id
     */
    private String label_id;

    /**
     * 秒杀价格
     */
    private BigDecimal seconds_price;

    /**
     * 秒杀价格单位
     */
    private Integer price_type;


    /**
     * 秒杀库存
     */
    private Integer num;

    /**
     * 规格id
     */
    @Column(name = "attr_id")
    private Integer attrId;

    /**
     * 最大数量
     */
    private Integer max_num;

    /**
     * 商品id
     */
    @Column(name = "goodsId")
    private Integer goodsId;

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
     * 活动开始时间
     */
    private Date starttime;

    /**
     * 活动结束时间
     */
    private Date endtime;

    /**
     * 是否显示 1 是 0 否
     */
    private Integer isshow;

    /**
     * 是否删除 1 是 0 否
     */
    private Integer is_delete;

    /**
     * 创建时间
     */
    private Date create_date;

    /**
     * 修改时间
     */
    private Date update_date;


    /**
     * 是否通知
     */
    @Column(name = "isNotice")
    private Integer isNotice;

    /**
     * 删除方式 1手动删除 2定时任务删除 默认为0
     */
    private Integer delete_method;

    /**
     * 是否推送过消息
     */
    private String remind_json;

    public Integer getAttrId()
    {
        return attrId;
    }

    public void setAttrId(Integer attrId)
    {
        this.attrId = attrId;
    }

    public Integer getDelete_method()
    {
        return delete_method;
    }

    public void setDelete_method(Integer delete_method)
    {
        this.delete_method = delete_method;
    }

    public String getRemind_json()
    {
        return remind_json;
    }

    public void setRemind_json(String remind_json)
    {
        this.remind_json = remind_json;
    }

    public Integer getIsNotice()
    {
        return isNotice;
    }

    public void setIsNotice(Integer isNotice)
    {
        this.isNotice = isNotice;
    }

    public Integer getId()
    {
        return id;
    }

    public void setId(Integer id)
    {
        this.id = id;
    }

    public Integer getStore_id()
    {
        return store_id;
    }

    public void setStore_id(Integer store_id)
    {
        this.store_id = store_id;
    }

    public String getLabel_id()
    {
        return label_id;
    }

    public void setLabel_id(String label_id)
    {
        this.label_id = label_id;
    }

    public BigDecimal getSeconds_price()
    {
        return seconds_price;
    }

    public void setSeconds_price(BigDecimal seconds_price)
    {
        this.seconds_price = seconds_price;
    }

    public Integer getPrice_type()
    {
        return price_type;
    }

    public void setPrice_type(Integer price_type)
    {
        this.price_type = price_type;
    }

    public Integer getNum()
    {
        return num;
    }

    public void setNum(Integer num)
    {
        this.num = num;
    }

    public Integer getMax_num()
    {
        return max_num;
    }

    public void setMax_num(Integer max_num)
    {
        this.max_num = max_num;
    }

    public Integer getGoodsId()
    {
        return goodsId;
    }

    public void setGoodsId(Integer goodsId)
    {
        this.goodsId = goodsId;
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

    public Integer getIs_delete()
    {
        return is_delete;
    }

    public void setIs_delete(Integer is_delete)
    {
        this.is_delete = is_delete;
    }

    public Date getCreate_date()
    {
        return create_date;
    }

    public void setCreate_date(Date create_date)
    {
        this.create_date = create_date;
    }

    public Date getUpdate_date()
    {
        return update_date;
    }

    public void setUpdate_date(Date update_date)
    {
        this.update_date = update_date;
    }
}