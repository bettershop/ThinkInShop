package com.laiketui.domain.divideAccount;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Table(name = "lkt_mch_distribution_record")
public class MchDistributionRecordModel implements Serializable
{

    /**
     * 记录类型
     */
    public interface Type
    {
        /**
         * 分账
         */
        int DIVIDEACCOUNTS = 1;
        /**
         * 回退
         */
        int REFUND         = 2;
    }

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
     * 子商户id
     */
    private String sub_mch_id;

    /**
     * 分账接收方账号
     */
    private String account;

    /**
     * 订单号
     */
    private String order_no;

    /**
     * 微信订单号
     */
    private String wx_order_no;

    /**
     * 分账单号
     */
    private String out_order_no;

    /**
     * 分账总金额
     */
    private BigDecimal total_amount;

    /**
     * 分账金额
     */
    private BigDecimal amount;

    /**
     * 分账比例
     */
    private BigDecimal proportion;

    /**
     * 记录类型 1.分账 2.回退
     */
    private Integer r_type;

    /**
     * 添加时间
     */
    private Date add_date;

    /**
     * 是否是平台 0.不是 1.是
     */
    private Integer is_platform_account;

    public Integer getIs_platform_account()
    {
        return is_platform_account;
    }

    public void setIs_platform_account(Integer is_platform_account)
    {
        this.is_platform_account = is_platform_account;
    }

    public String getOrder_no()
    {
        return order_no;
    }

    public void setOrder_no(String order_no)
    {
        this.order_no = order_no;
    }

    public String getWx_order_no()
    {
        return wx_order_no;
    }

    public void setWx_order_no(String wx_order_no)
    {
        this.wx_order_no = wx_order_no;
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
     * 获取子商户id
     *
     * @return sub_mch_id - 子商户id
     */
    public String getSub_mch_id()
    {
        return sub_mch_id;
    }

    /**
     * 设置子商户id
     *
     * @param sub_mch_id 子商户id
     */
    public void setSub_mch_id(String sub_mch_id)
    {
        this.sub_mch_id = sub_mch_id == null ? null : sub_mch_id.trim();
    }

    /**
     * 获取分账接收方账号
     *
     * @return account - 分账接收方账号
     */
    public String getAccount()
    {
        return account;
    }

    /**
     * 设置分账接收方账号
     *
     * @param account 分账接收方账号
     */
    public void setAccount(String account)
    {
        this.account = account == null ? null : account.trim();
    }

    /**
     * 获取分账单号
     *
     * @return out_order_no - 分账单号
     */
    public String getOut_order_no()
    {
        return out_order_no;
    }

    /**
     * 设置分账单号
     *
     * @param out_order_no 分账单号
     */
    public void setOut_order_no(String out_order_no)
    {
        this.out_order_no = out_order_no == null ? null : out_order_no.trim();
    }

    /**
     * 获取分账总金额
     *
     * @return total_amount - 分账总金额
     */
    public BigDecimal getTotal_amount()
    {
        return total_amount;
    }

    /**
     * 设置分账总金额
     *
     * @param total_amount 分账总金额
     */
    public void setTotal_amount(BigDecimal total_amount)
    {
        this.total_amount = total_amount;
    }

    /**
     * 获取分账金额
     *
     * @return amount - 分账金额
     */
    public BigDecimal getAmount()
    {
        return amount;
    }

    /**
     * 设置分账金额
     *
     * @param amount 分账金额
     */
    public void setAmount(BigDecimal amount)
    {
        this.amount = amount;
    }

    /**
     * 获取分账比例
     *
     * @return proportion - 分账比例
     */
    public BigDecimal getProportion()
    {
        return proportion;
    }

    /**
     * 设置分账比例
     *
     * @param proportion 分账比例
     */
    public void setProportion(BigDecimal proportion)
    {
        this.proportion = proportion;
    }

    /**
     * 获取记录类型 1.分账 2.回退
     *
     * @return r_type - 记录类型 1.分账 2.回退
     */
    public Integer getR_type()
    {
        return r_type;
    }

    /**
     * 设置记录类型 1.分账 2.回退
     *
     * @param r_type 记录类型 1.分账 2.回退
     */
    public void setR_type(Integer r_type)
    {
        this.r_type = r_type;
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
}