package com.laiketui.domain.user;

import com.laiketui.core.annotation.ParamsMapping;
import io.swagger.annotations.ApiModelProperty;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 提现申请表
 *
 * @author Trick
 * @date 2021/1/27 15:40
 */
@Table(name = "lkt_withdraw")
public class WithdrawModel implements Serializable
{


    /**
     * 审核中
     */
    @Transient
    public static final Integer EXAME_WAIT_STATUS     = 0;
    /**
     * 审核通过
     */
    @Transient
    public static final Integer EXAME_PASS_STATUS     = 1;
    /**
     * 拒绝
     */
    @Transient
    public static final Integer EXAME_NOT_PASS_STATUS = 2;

    /**
     * 提现类型 1银行卡  2微信余额
     */
    public interface WITHDRAW_STATUS
    {
        //1银行卡
        Integer YHK = 1;
        //微信余额
        Integer WX  = 2;
        //贝宝余额
        Integer PAYPAL = 3;
        //stripe余额
        Integer STRIPE = 4;
    }

    /**
     * 提现到微信余额提现状态：1进行中 2已完成 3提现失败
     */
    public interface WX_STATUS
    {
        //1进行中
        Integer WAIT_PAY = 1;
        //2已完成
        Integer SUCCESS  = 2;
        //3提现失败
        Integer FAIL     = 3;
    }

    /**
     * 提现到微信余额提现微信处理状态
     */
    public interface BATCH_STATUS
    {
        //待付款确认。需要付款出资商户在商家助手小程序或服务商助手小程序进行付款确认
        String WAIT_PAY   = "WAIT_PAY";
        //已受理。批次已受理成功，若发起批量转账的30分钟后，转账批次单仍处于该状态，可能原因是商户账户余额不足等。
        // 商户可查询账户资金流水，若该笔转账批次单的扣款已经发生，则表示批次已经进入转账中，请再次查单确认
        String ACCEPTED   = "ACCEPTED";
        //转账中。已开始处理批次内的转账明细单
        String PROCESSING = "PROCESSING";
        //已完成。批次内的所有转账明细单都已处理完成
        String FINISHED   = "FINISHED";
        //已关闭。可查询具体的批次关闭原因确认
        String CLOSED     = "CLOSED";
    }

    /**
     * id
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * 商城id
     */
    private Integer store_id;

    /**
     * 用户id
     */
    private String user_id;

    /**
     * 名称
     */
    private String name;

    /**
     * 微信id
     */
    private String wx_id;

    /**
     * 手机
     */
    private String mobile;

    /**
     * 银行卡id
     */
    @Column(name = "Bank_id")
    private Integer bank_id;

    /**
     * 提现金额
     */
    private BigDecimal money;

    /**
     * 剩余金额
     */
    private BigDecimal z_money;

    /**
     * 手续费
     */
    private BigDecimal s_charge;

    /**
     * 状态 0：审核中 1：审核通过 2：拒绝
     */
    private String status;

    /**
     * 申请时间
     */
    private Date add_date;

    /**
     * 是否是店铺提现
     */
    private Integer is_mch;

    /**
     * 提现通过订单号
     */
    private String txsno;

    private Date examine_date;

    /**
     * 是否回收 0.未回收 1.回收
     */
    private String recovery;

    /**
     * 拒绝原因
     */
    private String refuse;

    /**
     * 提现类型 1银行卡  2微信余额
     */
    private Integer withdrawStatus;

    /**
     * 提现到微信余额提现状态：1进行中 2已完成 3提现失败
     */
    private Integer wxStatus;

    /**
     * 用户openid
     */
    private String wxOpenId;

    /**
     * 微信提现到零钱订单号
     */
    private String wxSon;

    /**
     * 用户微信名称
     */
    private String wxName;

    /**
     * 用户微信余额提现真实姓名
     */
    private String realname;

    /**
     * 贝宝提现邮箱
     */
    private String email;

    /**
     * stripe账户id
     */
    @ApiModelProperty(value = "Stripe账户ID", name = "stripeAccountId")
    @ParamsMapping({"stripe_account_id", "stripeAccountId", "account_id"})
    @Column(name = "stripe_account_id")
    private String stripeAccountId;

    public String getStripeAccountId()
    {
        return stripeAccountId;
    }

    public void setStripeAccountId(String stripeAccountId)
    {
        this.stripeAccountId = stripeAccountId;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
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
     * 获取名称
     *
     * @return name - 名称
     */
    public String getName()
    {
        return name;
    }

    /**
     * 设置名称
     *
     * @param name 名称
     */
    public void setName(String name)
    {
        this.name = name == null ? null : name.trim();
    }

    /**
     * 获取微信id
     *
     * @return wx_id - 微信id
     */
    public String getWx_id()
    {
        return wx_id;
    }

    /**
     * 设置微信id
     *
     * @param wx_id 微信id
     */
    public void setWx_id(String wx_id)
    {
        this.wx_id = wx_id == null ? null : wx_id.trim();
    }

    /**
     * 获取手机
     *
     * @return mobile - 手机
     */
    public String getMobile()
    {
        return mobile;
    }

    /**
     * 设置手机
     *
     * @param mobile 手机
     */
    public void setMobile(String mobile)
    {
        this.mobile = mobile == null ? null : mobile.trim();
    }

    /**
     * 获取银行卡id
     *
     * @return Bank_id - 银行卡id
     */
    public Integer getBank_id()
    {
        return bank_id;
    }

    /**
     * 设置银行卡id
     *
     * @param bank_id 银行卡id
     */
    public void setBank_id(Integer bank_id)
    {
        this.bank_id = bank_id;
    }

    /**
     * 获取提现金额
     *
     * @return money - 提现金额
     */
    public BigDecimal getMoney()
    {
        return money;
    }

    /**
     * 设置提现金额
     *
     * @param money 提现金额
     */
    public void setMoney(BigDecimal money)
    {
        this.money = money;
    }

    /**
     * 获取剩余金额
     *
     * @return z_money - 剩余金额
     */
    public BigDecimal getZ_money()
    {
        return z_money;
    }

    /**
     * 设置剩余金额
     *
     * @param z_money 剩余金额
     */
    public void setZ_money(BigDecimal z_money)
    {
        this.z_money = z_money;
    }

    /**
     * 获取手续费
     *
     * @return s_charge - 手续费
     */
    public BigDecimal getS_charge()
    {
        return s_charge;
    }

    /**
     * 设置手续费
     *
     * @param s_charge 手续费
     */
    public void setS_charge(BigDecimal s_charge)
    {
        this.s_charge = s_charge;
    }

    /**
     * 获取状态 0：审核中 1：审核通过 2：拒绝
     *
     * @return status - 状态 0：审核中 1：审核通过 2：拒绝
     */
    public String getStatus()
    {
        return status;
    }

    /**
     * 设置状态 0：审核中 1：审核通过 2：拒绝
     *
     * @param status 状态 0：审核中 1：审核通过 2：拒绝
     */
    public void setStatus(String status)
    {
        this.status = status;
    }

    /**
     * 获取申请时间
     *
     * @return add_date - 申请时间
     */
    public Date getAdd_date()
    {
        return add_date;
    }

    /**
     * 设置申请时间
     *
     * @param add_date 申请时间
     */
    public void setAdd_date(Date add_date)
    {
        this.add_date = add_date;
    }

    /**
     * 获取是否是店铺提现
     *
     * @return is_mch - 是否是店铺提现
     */
    public Integer getIs_mch()
    {
        return is_mch;
    }

    /**
     * 设置是否是店铺提现
     *
     * @param is_mch 是否是店铺提现
     */
    public void setIs_mch(Integer is_mch)
    {
        this.is_mch = is_mch;
    }

    /**
     * 获取提现通过订单号
     *
     * @return txsno - 提现通过订单号
     */
    public String getTxsno()
    {
        return txsno;
    }

    /**
     * 设置提现通过订单号
     *
     * @param txsno 提现通过订单号
     */
    public void setTxsno(String txsno)
    {
        this.txsno = txsno == null ? null : txsno.trim();
    }

    /**
     * @return examine_date
     */
    public Date getExamine_date()
    {
        return examine_date;
    }

    /**
     * @param examine_date
     */
    public void setExamine_date(Date examine_date)
    {
        this.examine_date = examine_date;
    }

    /**
     * 获取是否回收 0.未回收 1.回收
     *
     * @return recovery - 是否回收 0.未回收 1.回收
     */
    public String getRecovery()
    {
        return recovery;
    }

    /**
     * 设置是否回收 0.未回收 1.回收
     *
     * @param recovery 是否回收 0.未回收 1.回收
     */
    public void setRecovery(String recovery)
    {
        this.recovery = recovery;
    }

    /**
     * 获取拒绝原因
     *
     * @return refuse - 拒绝原因
     */
    public String getRefuse()
    {
        return refuse;
    }

    /**
     * 设置拒绝原因
     *
     * @param refuse 拒绝原因
     */
    public void setRefuse(String refuse)
    {
        this.refuse = refuse == null ? null : refuse.trim();
    }

    public Integer getWithdrawStatus()
    {
        return withdrawStatus;
    }

    public void setWithdrawStatus(Integer withdrawStatus)
    {
        this.withdrawStatus = withdrawStatus;
    }

    public Integer getWxStatus()
    {
        return wxStatus;
    }

    public void setWxStatus(Integer wxStatus)
    {
        this.wxStatus = wxStatus;
    }

    public String getWxOpenId()
    {
        return wxOpenId;
    }

    public void setWxOpenId(String wxOpenId)
    {
        this.wxOpenId = wxOpenId;
    }

    public String getWxSon()
    {
        return wxSon;
    }

    public void setWxSon(String wxSon)
    {
        this.wxSon = wxSon;
    }

    public String getWxName()
    {
        return wxName;
    }

    public void setWxName(String wxName)
    {
        this.wxName = wxName;
    }

    public String getRealname()
    {
        return realname;
    }

    public void setRealname(String realname)
    {
        this.realname = realname;
    }
}
