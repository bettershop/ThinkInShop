package com.laiketui.domain.auction;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 竞拍保证金
 *
 * @author Trick
 * @date 2022/7/21 11:37
 */
@Table(name = "lkt_auction_promise")
public class AuctionPromiseModel implements Serializable
{
    /**
     * 主键
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * 用户id
     */
    private String user_id;

    /**
     * 保证金额
     */
    private BigDecimal promise;

    /**
     * 添加时间
     */
    private Date add_time;

    /**
     * 保证金扣除时间或者是退回时间
     */
    private Date back_time;

    /**
     * 专场id
     */
    private String special_id;

    /**
     * 订单编号
     */
    private String trade_no;

    /**
     * 是否成功支付，0失败，1成功
     */
    private Integer is_pay;

    /**
     * 是否退款成功  0-成功 1-失败
     */
    private Integer is_back;

    /**
     * 商城id
     */
    private Integer store_id;

    /**
     * 是否扣除，0未扣除 1扣除
     */
    private Integer is_deduction;

    /**
     * 支付方式
     */
    private String type;

    /**
     * 是否符合退款标准  0：不符合 1：符合
     */
    private Integer allow_back;

    /**
     * 收货地址ID
     */
    private Integer address_id;

    /**
     * 来源 1.小程序 2.app 3.支付宝小程序 4.头条小程序 5.百度小程序 6.pc端 7.H5
     */
    private Integer source;

    /**
     * 回收标识
     */
    private Integer recovery;

    public Integer getRecovery()
    {
        return recovery;
    }

    public void setRecovery(Integer recovery)
    {
        this.recovery = recovery;
    }

    public Integer getIs_deduction()
    {
        return is_deduction;
    }

    public void setIs_deduction(Integer is_deduction)
    {
        this.is_deduction = is_deduction;
    }

    /**
     * 获取主键
     *
     * @return id - 主键
     */
    public Integer getId()
    {
        return id;
    }

    /**
     * 设置主键
     *
     * @param id 主键
     */
    public void setId(Integer id)
    {
        this.id = id;
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

    public Date getBack_time()
    {
        return back_time;
    }

    public void setBack_time(Date back_time)
    {
        this.back_time = back_time;
    }

    /**
     * 获取保证金额
     *
     * @return promise - 保证金额
     */
    public BigDecimal getPromise()
    {
        return promise;
    }

    /**
     * 设置保证金额
     *
     * @param promise 保证金额
     */
    public void setPromise(BigDecimal promise)
    {
        this.promise = promise;
    }

    /**
     * 获取添加时间
     *
     * @return add_time - 添加时间
     */
    public Date getAdd_time()
    {
        return add_time;
    }

    /**
     * 设置添加时间
     *
     * @param add_time 添加时间
     */
    public void setAdd_time(Date add_time)
    {
        this.add_time = add_time;
    }

    /**
     * 专场id
     *
     * @return special_id - 竞拍商品id
     */
    public String getSpecial_id()
    {
        return special_id;
    }

    /**
     * 专场id
     *
     * @param special_id 竞拍商品id
     */
    public void setSpecial_id(String special_id)
    {
        this.special_id = special_id == null ? null : special_id.trim();
    }

    /**
     * 获取订单编号
     *
     * @return trade_no - 订单编号
     */
    public String getTrade_no()
    {
        return trade_no;
    }

    /**
     * 设置订单编号
     *
     * @param trade_no 订单编号
     */
    public void setTrade_no(String trade_no)
    {
        this.trade_no = trade_no == null ? null : trade_no.trim();
    }

    /**
     * 获取是否成功支付，0失败，1成功
     *
     * @return is_pay - 是否成功支付，0失败，1成功
     */
    public Integer getIs_pay()
    {
        return is_pay;
    }

    /**
     * 设置是否成功支付，0失败，1成功
     *
     * @param is_pay 是否成功支付，0失败，1成功
     */
    public void setIs_pay(Integer is_pay)
    {
        this.is_pay = is_pay;
    }

    /**
     * 获取是否退款成功  0-成功 1-失败
     *
     * @return is_back - 是否退款成功  0-成功 1-失败
     */
    public Integer getIs_back()
    {
        return is_back;
    }

    /**
     * 设置是否退款成功  0-成功 1-失败
     *
     * @param is_back 是否退款成功  0-成功 1-失败
     */
    public void setIs_back(Integer is_back)
    {
        this.is_back = is_back;
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
     * 获取支付方式
     *
     * @return type - 支付方式
     */
    public String getType()
    {
        return type;
    }

    /**
     * 设置支付方式
     *
     * @param type 支付方式
     */
    public void setType(String type)
    {
        this.type = type == null ? null : type.trim();
    }

    /**
     * 获取是否符合退款标准  0：不符合 1：符合
     *
     * @return allow_back - 是否符合退款标准  0：不符合 1：符合
     */
    public Integer getAllow_back()
    {
        return allow_back;
    }

    /**
     * 设置是否符合退款标准  0：不符合 1：符合
     *
     * @param allow_back 是否符合退款标准  0：不符合 1：符合
     */
    public void setAllow_back(Integer allow_back)
    {
        this.allow_back = allow_back;
    }

    /**
     * 获取收货地址ID
     *
     * @return address_id - 收货地址ID
     */
    public Integer getAddress_id()
    {
        return address_id;
    }

    /**
     * 设置收货地址ID
     *
     * @param address_id 收货地址ID
     */
    public void setAddress_id(Integer address_id)
    {
        this.address_id = address_id;
    }

    /**
     * 获取来源 1.小程序 2.app 3.支付宝小程序 4.头条小程序 5.百度小程序 6.pc端 7.H5
     *
     * @return source - 来源 1.小程序 2.app 3.支付宝小程序 4.头条小程序 5.百度小程序 6.pc端 7.H5
     */
    public Integer getSource()
    {
        return source;
    }

    /**
     * 设置来源 1.小程序 2.app 3.支付宝小程序 4.头条小程序 5.百度小程序 6.pc端 7.H5
     *
     * @param source 来源 1.小程序 2.app 3.支付宝小程序 4.头条小程序 5.百度小程序 6.pc端 7.H5
     */
    public void setSource(Integer source)
    {
        this.source = source;
    }
}