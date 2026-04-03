package com.laiketui.domain.presell;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author sunH_
 */
@Table(name = "lkt_pre_sell_record")
public class PreSellRecordModel implements Serializable
{


    /**
     * 定金
     */
    @Transient
    public static final Integer DEPOSIT = 0;
    /**
     * 尾款
     */
    @Transient
    public static final Integer BALANCE = 1;

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
     * 用户id
     */
    private String user_id;

    /**
     * 商品id
     */
    private Integer product_id;

    /**
     * 规格id
     */
    private Integer attr_id;

    /**
     * 当前付款金额
     */
    private BigDecimal price;

    /**
     * 定金金额
     */
    private BigDecimal deposit;

    /**
     * 尾款金额
     */
    private BigDecimal balance;

    /**
     * 数量
     */
    private Integer num;

    /**
     * 预售订单号
     */
    @Column(name = "sNo")
    private String sNo;

    /**
     * 支付类型 0.定金 1.尾款
     */
    private Integer pay_type;

    /**
     * 是否支付 0.未支付 1.已支付
     */
    private Integer is_pay;

    /**
     * 是否退款成功  0.否  1.是
     */
    private Integer is_refund = 0;

    /**
     * 是否删除 1是 0否
     */
    private Integer is_delete = 0;

    /**
     * 添加日期/定金支付时间
     */
    private Date add_time;

    /**
     * 尾款支付时间
     */
    private Date pay_balance_time;

    private String order_info;

    private String order_details_info;

    /**
     * 支付订单号
     */
    private String real_sno;

    /**
     * 支付方式
     */
    private String pay;

    /**
     * stripe订单id
     */
    private String stripe_id;

    /**
     * stripe支付意图id
     */
    private String stripe_payment_intent;

    public String getStripe_payment_intent()
    {
        return stripe_payment_intent;
    }

    public void setStripe_payment_intent(String stripe_payment_intent)
    {
        this.stripe_payment_intent = stripe_payment_intent;
    }

    public String getPaypal_id()
    {
        return paypal_id;
    }

    public void setPaypal_id(String paypal_id)
    {
        this.paypal_id = paypal_id;
    }

    /**
     * 贝宝id
     */
    private String paypal_id;


    public String getStripe_id()
    {
        return stripe_id;
    }

    public void setStripe_id(String stripe_id)
    {
        this.stripe_id = stripe_id;
    }

    public String getPay()
    {
        return pay;
    }

    public void setPay(String pay)
    {
        this.pay = pay;
    }

    public String getReal_sno()
    {
        return real_sno;
    }

    public void setReal_sno(String real_sno)
    {
        this.real_sno = real_sno;
    }

    public String getOrder_info()
    {
        return order_info;
    }

    public void setOrder_info(String order_info)
    {
        this.order_info = order_info;
    }

    public String getOrder_details_info()
    {
        return order_details_info;
    }

    public void setOrder_details_info(String order_details_info)
    {
        this.order_details_info = order_details_info;
    }

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
     * 获取用户id
     *
     * @return user_id - 用户id
     */
    public String getUser_id()
    {
        return user_id;
    }

    /**
     * 设置用户id
     *
     * @param user_id 用户id
     */
    public void setUser_id(String user_id)
    {
        this.user_id = user_id == null ? null : user_id.trim();
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
     * 获取价格
     *
     * @return price - 价格
     */
    public BigDecimal getPrice()
    {
        return price;
    }

    /**
     * 设置价格
     *
     * @param price 价格
     */
    public void setPrice(BigDecimal price)
    {
        this.price = price;
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

    /**
     * 获取尾款金额
     *
     * @return balance - 尾款金额
     */
    public BigDecimal getBalance()
    {
        return balance;
    }

    /**
     * 设置尾款金额
     *
     * @param balance 尾款金额
     */
    public void setBalance(BigDecimal balance)
    {
        this.balance = balance;
    }

    /**
     * 获取数量
     *
     * @return num - 数量
     */
    public Integer getNum()
    {
        return num;
    }

    /**
     * 设置数量
     *
     * @param num 数量
     */
    public void setNum(Integer num)
    {
        this.num = num;
    }

    /**
     * 获取预售订单号
     *
     * @return sNo - 预售订单号
     */
    public String getsNo()
    {
        return sNo;
    }

    /**
     * 设置预售订单号
     *
     * @param sNo 预售订单号
     */
    public void setsNo(String sNo)
    {
        this.sNo = sNo == null ? null : sNo.trim();
    }

    public Integer getPay_type()
    {
        return pay_type;
    }

    public void setPay_type(Integer pay_type)
    {
        this.pay_type = pay_type;
    }

    public Integer getIs_pay()
    {
        return is_pay;
    }

    public void setIs_pay(Integer is_pay)
    {
        this.is_pay = is_pay;
    }

    public Integer getIs_refund()
    {
        return is_refund;
    }

    public void setIs_refund(Integer is_refund)
    {
        this.is_refund = is_refund;
    }

    /**
     * 获取是否删除 1是 0否
     *
     * @return is_delete - 是否删除 1是 0否
     */
    public Integer getIs_delete()
    {
        return is_delete;
    }

    /**
     * 设置是否删除 1是 0否
     *
     * @param is_delete 是否删除 1是 0否
     */
    public void setIs_delete(Integer is_delete)
    {
        this.is_delete = is_delete;
    }

    /**
     * 获取添加日期/定金支付时间
     *
     * @return add_time - 添加日期
     */
    public Date getAdd_time()
    {
        return add_time;
    }

    /**
     * 设置添加日期/定金支付时间
     *
     * @param add_time 添加日期
     */
    public void setAdd_time(Date add_time)
    {
        this.add_time = add_time;
    }

    public Date getPay_balance_time()
    {
        return pay_balance_time;
    }

    public void setPay_balance_time(Date pay_balance_time)
    {
        this.pay_balance_time = pay_balance_time;
    }

    @Override
    public String toString()
    {
        return "PreSellRecordModel{" +
                "id=" + id +
                ", store_id=" + store_id +
                ", user_id='" + user_id + '\'' +
                ", product_id=" + product_id +
                ", attr_id=" + attr_id +
                ", price=" + price +
                ", deposit=" + deposit +
                ", balance=" + balance +
                ", num=" + num +
                ", sNo='" + sNo + '\'' +
                ", pay_type=" + pay_type +
                ", is_pay=" + is_pay +
                ", is_refund=" + is_refund +
                ", is_delete=" + is_delete +
                ", add_time=" + add_time +
                ", pay_balance_time=" + pay_balance_time +
                ", order_info='" + order_info + '\'' +
                ", order_details_info='" + order_details_info + '\'' +
                ", real_sno='" + real_sno + '\'' +
                ", pay='" + pay + '\'' +
                ", paypal_id='" + paypal_id + '\'' +
                '}';
    }
}
