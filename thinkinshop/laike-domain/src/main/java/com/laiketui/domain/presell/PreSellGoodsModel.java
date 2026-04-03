package com.laiketui.domain.presell;

import com.laiketui.domain.product.ProductListModel;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author sunH_
 */
@Table(name = "lkt_pre_sell_goods")
public class PreSellGoodsModel implements Serializable
{
    /**
     * 唯一标识
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * 商品id
     */
    private Integer product_id;

    /**
     * 商品名称
     */
    private String product_title;

    /**
     * 预售类型   1.定金模式   2.订货模式
     */
    private Integer sell_type;

    /**
     * 定金模式
     */
    @Transient
    public static final Integer DEPOSIT_PATTERN = 1;
    /**
     * 订货模式
     */
    @Transient
    public static final Integer ORDER_PATTERN   = 2;

    /**
     * 定金金额
     */
    private BigDecimal deposit;

    /**
     * 支付定金类型   1.默认    2.自定义
     */
    private Integer pay_type;

    /**
     * 默认
     */
    @Transient
    public static final Integer DEFAULT = 1;
    /**
     * 自定义
     */
    @Transient
    public static final Integer CUSTOM  = 2;


    /**
     * 定金支付开始时间
     */
    private Date deposit_start_time;

    /**
     * 定金支付结束时间
     */
    private Date deposit_end_time;

    /**
     * 尾款支付日期
     */
    private Date balance_pay_time;

    /**
     * 预售数量
     */
    private Integer sell_num;

    /**
     * 剩余数量
     */
    private Integer surplus_num;

    /**
     * 截止天数
     */
    private Integer end_day;

    /**
     * 截止时间
     */
    private Date deadline;

    /**
     * 发货时间(天)
     */
    private Integer delivery_time;

    /**
     * 是否上架过
     */
    private Integer is_on_shelf;

    /**
     * 是否显示  0.否 1.是
     */
    private Integer is_display;

    /**
     * 是否删除 0.否 1.是
     */
    private Integer is_delete;

    @OneToOne(targetEntity = ProductListModel.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", referencedColumnName = "id", insertable = false, updatable = false)
    private ProductListModel productListModel;

    public ProductListModel getProductListModel()
    {
        return productListModel;
    }

    public void setProductListModel(ProductListModel productListModel)
    {
        this.productListModel = productListModel;
    }

    /**
     * 获取唯一标识
     *
     * @return id - 唯一标识
     */
    public Integer getId()
    {
        return id;
    }

    /**
     * 设置唯一标识
     *
     * @param id 唯一标识
     */
    public void setId(Integer id)
    {
        this.id = id;
    }

    /**
     * 获取商品id
     *
     * @return product_id - 商品id
     */
    public Integer getProduct_id()
    {
        return product_id;
    }

    /**
     * 设置商品id
     *
     * @param product_id 商品id
     */
    public void setProduct_id(Integer product_id)
    {
        this.product_id = product_id;
    }

    public String getProduct_title()
    {
        return product_title;
    }

    public void setProduct_title(String product_title)
    {
        this.product_title = product_title;
    }

    /**
     * 获取预售类型   1.定金模式   2.订货模式
     *
     * @return sell_type - 预售类型   1.定金模式   2.订货模式
     */
    public Integer getSell_type()
    {
        return sell_type;
    }

    /**
     * 设置预售类型   1.定金模式   2.订货模式
     *
     * @param sell_type 预售类型   1.定金模式   2.订货模式
     */
    public void setSell_type(Integer sell_type)
    {
        this.sell_type = sell_type;
    }

    /**
     * 获取定金金额
     *
     * @return deposit - 定金金额
     */
    public BigDecimal getDeposit()
    {
        return deposit;
    }

    /**
     * 设置定金金额
     *
     * @param deposit 定金金额
     */
    public void setDeposit(BigDecimal deposit)
    {
        this.deposit = deposit;
    }

    public Integer getPay_type()
    {
        return pay_type;
    }

    public void setPay_type(Integer pay_type)
    {
        this.pay_type = pay_type;
    }

    /**
     * 获取预售数量
     *
     * @return sell_num - 预售数量
     */
    public Integer getSell_num()
    {
        return sell_num;
    }

    /**
     * 设置预售数量
     *
     * @param sell_num 预售数量
     */
    public void setSell_num(Integer sell_num)
    {
        this.sell_num = sell_num;
    }

    public Integer getSurplus_num()
    {
        return surplus_num;
    }

    public void setSurplus_num(Integer surplus_num)
    {
        this.surplus_num = surplus_num;
    }

    /**
     * 获取截止天数
     *
     * @return end_day - 截止天数
     */
    public Integer getEnd_day()
    {
        return end_day;
    }

    /**
     * 设置截止天数
     *
     * @param end_day 截止天数
     */
    public void setEnd_day(Integer end_day)
    {
        this.end_day = end_day;
    }

    public Date getDeposit_start_time()
    {
        return deposit_start_time;
    }

    public void setDeposit_start_time(Date deposit_start_time)
    {
        this.deposit_start_time = deposit_start_time;
    }

    public Date getDeposit_end_time()
    {
        return deposit_end_time;
    }

    public void setDeposit_end_time(Date deposit_end_time)
    {
        this.deposit_end_time = deposit_end_time;
    }

    public Date getBalance_pay_time()
    {
        return balance_pay_time;
    }

    public void setBalance_pay_time(Date balance_pay_time)
    {
        this.balance_pay_time = balance_pay_time;
    }

    public Date getDeadline()
    {
        return deadline;
    }

    public void setDeadline(Date deadline)
    {
        this.deadline = deadline;
    }

    public Integer getIs_on_shelf()
    {
        return is_on_shelf;
    }

    public void setIs_on_shelf(Integer is_on_shelf)
    {
        this.is_on_shelf = is_on_shelf;
    }

    /**
     * 获取发货时间(天)
     *
     * @return delivery_time - 发货时间(天)
     */
    public Integer getDelivery_time()
    {
        return delivery_time;
    }

    /**
     * 设置发货时间(天)
     *
     * @param delivery_time 发货时间(天)
     */
    public void setDelivery_time(Integer delivery_time)
    {
        this.delivery_time = delivery_time;
    }

    public Integer getIs_display()
    {
        return is_display;
    }

    public void setIs_display(Integer is_display)
    {
        this.is_display = is_display;
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