package com.laiketui.domain.seckill;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

/**
 * 秒杀商品 - 规格
 *
 * @author Trick
 * @date 2021/10/21 9:58
 */
@Table(name = "lkt_seconds_pro")
public class SecondsProModel implements Serializable
{

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
     * 店铺id
     */
    private Integer store_id;

    /**
     * 规格id
     */
    private Integer attr_id;


    /**
     * 秒杀库存
     */
    private Integer num;

    /**
     * 最大数量
     */
    private Integer max_num;

    /**
     * 活动id
     */
    private Integer activity_id;

    /**
     * 添加日期
     */
    private Date add_time;

    /**
     * 是否删除 1 是 0 否
     */
    private Integer is_delete;

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

    public Integer getAttr_id()
    {
        return attr_id;
    }

    public void setAttr_id(Integer attr_id)
    {
        this.attr_id = attr_id;
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

    public Integer getActivity_id()
    {
        return activity_id;
    }

    public void setActivity_id(Integer activity_id)
    {
        this.activity_id = activity_id;
    }

    public Date getAdd_time()
    {
        return add_time;
    }

    public void setAdd_time(Date add_time)
    {
        this.add_time = add_time;
    }

    public Integer getIs_delete()
    {
        return is_delete;
    }

    public void setIs_delete(Integer is_delete)
    {
        this.is_delete = is_delete;
    }
}