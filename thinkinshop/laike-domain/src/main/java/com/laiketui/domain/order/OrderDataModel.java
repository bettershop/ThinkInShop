package com.laiketui.domain.order;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;


/**
 * 订单临时表
 *
 * @author Trick
 * @date 2020/12/23 17:02
 */
@Table(name = "lkt_order_data")
public class OrderDataModel implements Serializable
{

    /**
     * 支付状态
     */
    public interface PayStatus
    {
        /**
         * 未支付
         */
        Integer NOT_PAY = 0;
        /**
         * 已支付
         */
        Integer PAYMENT = 1;

    }


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * 微信订单号
     */
    @Column(name = "`trade_no`")
    private String trade_no;

    @Column(name = "`data`")
    private String data;

    /**
     * 订单类型
     */
    @Column(name = "order_type")
    private String order_type;

    /**
     * 支付方式
     */
    @Column(name = "pay_type")
    private String pay_type;

    /**
     * 创建时间
     */
    @Column(name = "`addtime`")
    private Date addtime;

    /**
     * 支付状态 0未支付 1已支付
     */
    @Column(name = "`status`")
    private Integer status;


    /**
     * 货币编码：支付和退款用
     */
    private String currency_code;

    /**
     * 货币符号
     */
    private String currency_symbol;

    /**
     * 汇率：支付和退款用、界面展示计算用
     */
    private BigDecimal exchange_rate;

    /**
     * 来源
     */
    @Column(name = "`source`")
    private Integer source;

    public String getCurrency_code()
    {
        return currency_code;
    }

    public void setCurrency_code(String currency_code)
    {
        this.currency_code = currency_code;
    }

    public String getCurrency_symbol()
    {
        return currency_symbol;
    }

    public void setCurrency_symbol(String currency_symbol)
    {
        this.currency_symbol = currency_symbol;
    }

    public BigDecimal getExchange_rate()
    {
        return exchange_rate;
    }

    public void setExchange_rate(BigDecimal exchange_rate)
    {
        this.exchange_rate = exchange_rate;
    }

    public Integer getSource()
    {
        return source;
    }

    public void setSource(Integer source)
    {
        this.source = source;
    }

    public String getOrder_type()
    {
        return order_type;
    }

    public void setOrder_type(String order_type)
    {
        this.order_type = order_type;
    }

    public String getPay_type()
    {
        return pay_type;
    }

    public void setPay_type(String pay_type)
    {
        this.pay_type = pay_type;
    }

    /**
     * @return id
     */
    public Integer getId()
    {
        return id;
    }

    /**
     * @param id
     */
    public void setId(Integer id)
    {
        this.id = id;
    }

    /**
     * 获取微信订单号
     *
     * @return trade_no - 微信订单号
     */
    public String getTrade_no()
    {
        return trade_no;
    }

    /**
     * 设置微信订单号
     *
     * @param trade_no 微信订单号
     */
    public void setTrade_no(String trade_no)
    {
        this.trade_no = trade_no == null ? null : trade_no.trim();
    }

    /**
     * @return data
     */
    public String getData()
    {
        return data;
    }

    /**
     * @param data
     */
    public void setData(String data)
    {
        this.data = data == null ? null : data.trim();
    }

    /**
     * 获取创建时间
     *
     * @return addtime - 创建时间
     */
    public Date getAddtime()
    {
        return addtime;
    }

    /**
     * 设置创建时间
     *
     * @param addtime 创建时间
     */
    public void setAddtime(Date addtime)
    {
        this.addtime = addtime;
    }

    /**
     * 获取支付状态 0未支付 1已支付
     *
     * @return status - 支付状态 0未支付 1已支付
     */
    public Integer getStatus()
    {
        return status;
    }

    /**
     * 设置支付状态 0未支付 1已支付
     *
     * @param status 支付状态 0未支付 1已支付
     */
    public void setStatus(Integer status)
    {
        this.status = status;
    }
}
