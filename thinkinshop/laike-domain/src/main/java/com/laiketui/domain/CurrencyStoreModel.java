package com.laiketui.domain;

import javax.persistence.Table;
import javax.persistence.Transient;
import java.math.BigDecimal;
import java.util.Date;

@Table(name = "lkt_currency_store")
public class CurrencyStoreModel
{

    /**
     * 是否默认币种
     */
    public interface DefaultCurrency
    {
        //是
        int YES = 1;
        //否
        int NO  = 0;
    }

    /**
     * 是否默认币种
     */
    public interface ShowOptions
    {
        //展示
        int YES = 1;
        //不展示
        int NO  = 0;
    }

    /**
     * 是否回收
     */
    public interface RecycleOptions
    {
        //回收
        int YES = 1;
        //不回收
        int NO  = 0;
    }


    /**
     * 商城id
     */
    private Integer store_id;

    /**
     * 货币id
     */
    private Integer currency_id;

    /**
     * 货币符号
     */
    @Transient
    private String currency_symbol;

    /**
     * 是否展示 0 不展示 1展示
     */
    private Integer is_show;

    /**
     * 是否商城基础货币【结算货币】默认 0 否 1 是
     */
    private Integer default_currency;

    /**
     * 基础货币汇率
     */
    private BigDecimal exchange_rate;

    /**
     * 是否删除 0 未删除 1已删除
     */
    private Integer recycle;

    private Date update_time;

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
     * 获取货币id
     *
     * @return currency_id - 货币id
     */
    public Integer getCurrency_id()
    {
        return currency_id;
    }

    /**
     * 设置货币id
     *
     * @param currency_id 货币id
     */
    public void setCurrency_id(Integer currency_id)
    {
        this.currency_id = currency_id;
    }

    /**
     * 获取货币符号
     *
     * @return currency_symbol - 货币符号
     */
    public String getCurrency_symbol()
    {
        return currency_symbol;
    }

    /**
     * 设置货币符号
     *
     * @param currency_symbol 货币符号
     */
    public void setCurrency_symbol(String currency_symbol)
    {
        this.currency_symbol = currency_symbol;
    }

    /**
     * 获取是否展示 0 不展示 1展示
     *
     * @return is_show - 是否展示 0 不展示 1展示
     */
    public Integer getIs_show()
    {
        return is_show;
    }

    /**
     * 设置是否展示 0 不展示 1展示
     *
     * @param is_show 是否展示 0 不展示 1展示
     */
    public void setIs_show(Integer is_show)
    {
        this.is_show = is_show;
    }

    /**
     * 获取是否商城基础货币【结算货币】默认 0 否 1 是
     *
     * @return default_currency - 是否商城基础货币【结算货币】默认 0 否 1 是
     */
    public Integer getDefault_currency()
    {
        return default_currency;
    }

    /**
     * 设置是否商城基础货币【结算货币】默认 0 否 1 是
     *
     * @param default_currency 是否商城基础货币【结算货币】默认 0 否 1 是
     */
    public void setDefault_currency(Integer default_currency)
    {
        this.default_currency = default_currency;
    }

    /**
     * 获取基础货币汇率
     *
     * @return exchange_rate - 基础货币汇率
     */
    public BigDecimal getExchange_rate()
    {
        return exchange_rate;
    }

    /**
     * 设置基础货币汇率
     *
     * @param exchange_rate 基础货币汇率
     */
    public void setExchange_rate(BigDecimal exchange_rate)
    {
        this.exchange_rate = exchange_rate;
    }

    /**
     * 获取是否删除 0 未删除 1已删除
     *
     * @return recycle - 是否删除 0 未删除 1已删除
     */
    public Integer getRecycle()
    {
        return recycle;
    }

    /**
     * 设置是否删除 0 未删除 1已删除
     *
     * @param recycle 是否删除 0 未删除 1已删除
     */
    public void setRecycle(Integer recycle)
    {
        this.recycle = recycle;
    }

    /**
     * @return update_time
     */
    public Date getUpdate_time()
    {
        return update_time;
    }

    /**
     * @param update_time
     */
    public void setUpdate_time(Date update_time)
    {
        this.update_time = update_time;
    }
}
