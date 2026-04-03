package com.laiketui.domain.auction;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 竞拍商品活动表
 *
 * @author Trick
 * @date 2022/7/5 19:51
 */
@Table(name = "lkt_auction_product")
public class AuctionProductModel implements Serializable
{

    /**
     * 竞拍活动状态 0=待拍卖 1=拍卖中 2=已拍卖 3=已流拍
     */
    public interface GoodsStatus
    {
        /**
         * 待拍卖
         */
        Integer STATUS_SOLD_WAIT        = 0;
        /**
         * 拍卖中
         */
        Integer STATUS_SOLD_IN_PROGRESS = 1;
        /**
         * 已拍卖
         */
        Integer STATUS_SOLD             = 2;
        /**
         * 已流拍
         */
        Integer STATUS_UNSOLD           = 3;
    }

    public interface Status
    {
        /**
         * 未开始
         */
        int NOT_START = 0;
        /**
         * 进行中
         */
        int ONGOING   = 1;
        /**
         * 结束
         */
        int END       = 2;
        /**
         * 流拍
         */
        int LOSE      = 3;
    }

    //流拍类型  1未付款流拍  2未达到人数流拍
    public interface passedStatus
    {
        /**
         * 1未付款流拍
         */
        Integer STATUS_1 = 1;
        /**
         * 2未达到人数流拍
         */
        Integer STATUS_2 = 2;
    }

    /**
     * id
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * 场次id
     */
    private String session_id;

    /**
     * 1=已拍卖 2=已流拍
     */
    private Integer status;

    /**
     * 商品id
     */
    private Integer goods_id;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 商品规格id
     */
    private Integer attr_id;

    /**
     * 商品价格
     */
    private BigDecimal goods_price;

    /**
     * 当前出价
     */
    private BigDecimal price;

    /**
     * 加价幅度
     */
    private BigDecimal mark_up_amt;

    /**
     * 起拍价
     */
    private BigDecimal starting_amt;

    /**
     * 最终得主
     */
    private String user_id;

    /**
     * 关联订单
     */
    @Column(name = "sNo")
    private String sNo;

    /**
     * 是否显示
     */
    private Integer is_show;

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
     * 流拍类型  1未付款流拍  2未达到人数流拍
     */
    private Integer passed_status;


    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }

    public String getsNo()
    {
        return sNo;
    }

    public void setsNo(String sNo)
    {
        this.sNo = sNo;
    }

    /**
     * 获取id
     *
     * @return id - id
     */
    public Integer getId()
    {
        return id;
    }

    /**
     * 设置id
     *
     * @param id id
     */
    public void setId(Integer id)
    {
        this.id = id;
    }


    /**
     * 获取起拍价
     *
     * @return starting_amt - 起拍价
     */
    public BigDecimal getStarting_amt()
    {
        return starting_amt;
    }

    /**
     * 设置起拍价
     *
     * @param starting_amt 起拍价
     */
    public void setStarting_amt(BigDecimal starting_amt)
    {
        this.starting_amt = starting_amt;
    }

    /**
     * 获取加价幅度
     *
     * @return mark_up_amt - 加价幅度
     */
    public BigDecimal getMark_up_amt()
    {
        return mark_up_amt;
    }

    /**
     * 设置加价幅度
     *
     * @param mark_up_amt 加价幅度
     */
    public void setMark_up_amt(BigDecimal mark_up_amt)
    {
        this.mark_up_amt = mark_up_amt;
    }

    /**
     * 当前出价
     */
    public BigDecimal getPrice()
    {
        return price;
    }

    /**
     * 当前出价
     */
    public void setPrice(BigDecimal price)
    {
        this.price = price;
    }

    /**
     * 商品价格
     */
    public BigDecimal getGoods_price()
    {
        return goods_price;
    }

    /**
     * 商品价格
     */
    public void setGoods_price(BigDecimal goods_price)
    {
        this.goods_price = goods_price;
    }

    /**
     * 最终得主
     */
    public String getUser_id()
    {
        return user_id;
    }

    /**
     * 最终得主
     */
    public void setUser_id(String user_id)
    {
        this.user_id = user_id;
    }

    /**
     * 获取场次id
     *
     * @return session_id - 场次id
     */
    public String getSession_id()
    {
        return session_id;
    }

    /**
     * 设置场次id
     *
     * @param session_id 场次id
     */
    public void setSession_id(String session_id)
    {
        this.session_id = session_id == null ? null : session_id.trim();
    }

    /**
     * 获取1=已拍卖 2=已流拍
     *
     * @return status - 1=已拍卖 2=已流拍
     */
    public Integer getStatus()
    {
        return status;
    }

    /**
     * 设置1=已拍卖 2=已流拍
     *
     * @param status 1=已拍卖 2=已流拍
     */
    public void setStatus(Integer status)
    {
        this.status = status;
    }

    /**
     * 获取商品id
     *
     * @return goods_id - 商品id
     */
    public Integer getGoods_id()
    {
        return goods_id;
    }

    /**
     * 设置商品id
     *
     * @param goods_id 商品id
     */
    public void setGoods_id(Integer goods_id)
    {
        this.goods_id = goods_id;
    }

    /**
     * 获取商品规格id
     *
     * @return attr_id - 商品规格id
     */
    public Integer getAttr_id()
    {
        return attr_id;
    }

    /**
     * 设置商品规格id
     *
     * @param attr_id 商品规格id
     */
    public void setAttr_id(Integer attr_id)
    {
        this.attr_id = attr_id;
    }

    /**
     * 获取是否显示
     *
     * @return is_show - 是否显示
     */
    public Integer getIs_show()
    {
        return is_show;
    }

    /**
     * 设置是否显示
     *
     * @param is_show 是否显示
     */
    public void setIs_show(Integer is_show)
    {
        this.is_show = is_show;
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

    public Integer getPassed_status()
    {
        return passed_status;
    }

    public void setPassed_status(Integer passed_status)
    {
        this.passed_status = passed_status;
    }
}