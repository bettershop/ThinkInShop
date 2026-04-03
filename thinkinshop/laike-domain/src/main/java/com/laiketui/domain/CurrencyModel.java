package com.laiketui.domain;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Table(name = "lkt_currency")
public class CurrencyModel
{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * ISO货币代码(如USD)
     */
    private String currency_code;

    /**
     * 货币名称
     */
    private String currency_name;

    /**
     * 是否展示 0 不展示 1展示
     */
    private Integer is_show;

    /**
     * 货币符号($)
     */
    private String currency_symbol;


    /**
     * 是否删除 0 未删除 1已删除
     */
    private Integer recycle;

    private Date update_time;

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
     * 获取ISO货币代码(如USD)
     *
     * @return currency_code - ISO货币代码(如USD)
     */
    public String getCurrency_code()
    {
        return currency_code;
    }

    /**
     * 设置ISO货币代码(如USD)
     *
     * @param currency_code ISO货币代码(如USD)
     */
    public void setCurrency_code(String currency_code)
    {
        this.currency_code = currency_code == null ? null : currency_code.trim();
    }

    /**
     * 获取货币名称
     *
     * @return currency_name - 货币名称
     */
    public String getCurrency_name()
    {
        return currency_name;
    }

    /**
     * 设置货币名称
     *
     * @param currency_name 货币名称
     */
    public void setCurrency_name(String currency_name)
    {
        this.currency_name = currency_name == null ? null : currency_name.trim();
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
     * 获取货币符号($)
     *
     * @return currency_symbol - 货币符号($)
     */
    public String getCurrency_symbol()
    {
        return currency_symbol;
    }

    /**
     * 设置货币符号($)
     *
     * @param currency_symbol 货币符号($)
     */
    public void setCurrency_symbol(String currency_symbol)
    {
        this.currency_symbol = currency_symbol == null ? null : currency_symbol.trim();
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