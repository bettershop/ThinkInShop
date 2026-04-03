package com.laiketui.domain.mch;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Table(name = "lkt_promise_sh")
public class PromiseShModel implements Serializable
{
    /**
     * id
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * 店铺id
     */
    private Integer mch_id;

    /**
     * 保证金金额
     */
    private BigDecimal promise_amt;

    /**
     * 保证金审核状态 1=通过 2=拒绝 3=审核中
     */
    private Integer status;

    /**
     * 支付方式
     */
    private String pay_type;

    /**
     * 是否通过
     */
    private Integer is_pass;

    /**
     * 添加时间
     */
    private Date add_date;

    /**
     * 拒绝时间
     */
    private Date   refused_date;
    /**
     * 拒绝原因
     */
    private String refused_why;

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
     * 获取店铺id
     *
     * @return mch_id - 店铺id
     */
    public Integer getMch_id()
    {
        return mch_id;
    }

    /**
     * 设置店铺id
     *
     * @param mch_id 店铺id
     */
    public void setMch_id(Integer mch_id)
    {
        this.mch_id = mch_id;
    }

    /**
     * 获取保证金金额
     *
     * @return promise_amt - 保证金金额
     */
    public BigDecimal getPromise_amt()
    {
        return promise_amt;
    }

    /**
     * 设置保证金金额
     *
     * @param promise_amt 保证金金额
     */
    public void setPromise_amt(BigDecimal promise_amt)
    {
        this.promise_amt = promise_amt;
    }

    /**
     * 获取保证金审核状态 1=通过 2=拒绝 3=审核中
     *
     * @return status - 保证金审核状态 1=通过 2=拒绝 3=审核中
     */
    public Integer getStatus()
    {
        return status;
    }

    /**
     * 设置保证金审核状态 1=通过 2=拒绝 3=审核中
     *
     * @param status 保证金审核状态 1=通过 2=拒绝 3=审核中
     */
    public void setStatus(Integer status)
    {
        this.status = status;
    }

    /**
     * 获取支付方式
     *
     * @return pay_type - 支付方式
     */
    public String getPay_type()
    {
        return pay_type;
    }

    /**
     * 设置支付方式
     *
     * @param pay_type 支付方式
     */
    public void setPay_type(String pay_type)
    {
        this.pay_type = pay_type == null ? null : pay_type.trim();
    }

    /**
     * 获取是否通过
     *
     * @return is_pass - 是否通过
     */
    public Integer getIs_pass()
    {
        return is_pass;
    }

    /**
     * 设置是否通过
     *
     * @param is_pass 是否通过
     */
    public void setIs_pass(Integer is_pass)
    {
        this.is_pass = is_pass;
    }

    /**
     * 获取拒绝时间
     *
     * @return refused_date - 拒绝时间
     */
    public Date getRefused_date()
    {
        return refused_date;
    }

    /**
     * 设置拒绝时间
     *
     * @param refused_date 拒绝时间
     */
    public void setRefused_date(Date refused_date)
    {
        this.refused_date = refused_date;
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
     * 获取拒绝原因
     *
     * @return refused_why - 拒绝原因
     */
    public String getRefused_why()
    {
        return refused_why;
    }

    /**
     * 设置拒绝原因
     *
     * @param refused_why 拒绝原因
     */
    public void setRefused_why(String refused_why)
    {
        this.refused_why = refused_why == null ? null : refused_why.trim();
    }
}