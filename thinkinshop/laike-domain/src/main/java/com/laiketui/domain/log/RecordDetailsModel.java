package com.laiketui.domain.log;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 操作记录详情
 */
@Table(name = "lkt_record_details")
public class RecordDetailsModel implements Serializable
{

    /**
     * 1 余额充值(用户)  2余额充值(平台)  3活动佣金  4订单退款 5店铺保证金提取 6 平台扣款  7 订单支付  8 参与活动  9充值会员  10 店铺保证金支付  11余额提现  12活动退款
     */
    public interface type
    {
        //1 余额充值(用户)
        Integer USER_BALANCE_RECHARGE     = 1;
        //2余额充值(平台)
        Integer STORE_BALANCE_RECHARGE    = 2;
        //3活动佣金
        Integer EVENT_COMMISSION          = 3;
        //订单退款
        Integer ORDER_REFUND              = 4;
        // 5店铺保证金提取
        Integer MCH_DEPOSIT_WITHDRAWAL    = 5;
        //平台扣款
        Integer PLATFORM_DEDUCTION        = 6;
        //7 订单支付
        Integer ORDER_PAYMENT             = 7;
        //8 参与活动
        Integer PARTICIPATE_IN_ACTIVITIES = 8;
        //9充值会员
        Integer RECHARGE_MEMBERSHIP       = 9;
        //10 店铺保证金支付
        Integer STORE_DEPOSIT_PAYMENT     = 10;
        //11余额提现
        Integer WITHDRAWAL_OF_BALANCE     = 11;
        //12活动退款
        Integer EVENT_REFUND              = 12;
        //13会员续费
        Integer MEMBERSHIP_RENEW          = 13;
        //42直播佣金
        Integer Living_COMMISSION         = 42;
    }

    /**
     * 1 收入类型  2支出类型
     */
    public interface moneyType
    {
        Integer INCOME = 1;

        Integer EXPENDITURE = 2;
    }

    /**
     * 收入/支出 名称   1用户充值 2平台充值 3 拼团活动开团佣金 4普通订单 5竞拍活动 6店铺保证金 7余额扣款 8预售定金缴纳  9会员开通  10 普通订单(代客下单) 11拼团订单
     * 12竟拍订单 13预售订单 14分稍订单  15秒杀订单 16积分兑换订单 17限时折扣订单 18竞拍保证金缴纳 19虚拟订单 20直播订单
     */
    public interface moneyTypeName
    {
        // 1用户充值
        Integer USER_BALANCE_RECHARGE  = 1;
        //2平台充值
        Integer STORE_BALANCE_RECHARGE = 2;
        //3 拼团活动开团佣金
        Integer EVENT_COMMISSION       = 3;
        //普通订单
        Integer ORDER_REFUND           = 4;
        //5竞拍活动
        Integer AUCTION_ACTIVITIES     = 5;
        //6店铺保证金
        Integer MCH_DEPOSIT            = 6;
        //7余额扣款
        Integer BALANCE_DEDUCTION      = 7;
        //预售定金缴纳
        Integer PAYMENT_DEPOSIT        = 8;
        //会员开通
        Integer MEMBER_ACTIVATION      = 9;
        //代客下单
        Integer ORDER_HELP_ORDER       = 10;
        //拼团订单
        Integer ORDER_PT_ORDER         = 11;
        //12竟拍订单
        Integer ORDER_JP_ORDER         = 12;
        //预售订单
        Integer ORDER_YS_ORDER         = 13;
        //分稍订单
        Integer ORDER_FX_ORDER         = 14;
        //秒杀订单
        Integer ORDER_MS_ORDER         = 15;
        //积分兑换订单
        Integer ORDER_IN_ORDER         = 16;
        //17限时折扣订单
        Integer ORDER_FS_ORDER         = 17;
        //18竞拍保证金缴纳
        Integer ORDER_AUCTION_ORDER    = 18;

        //19虚拟订单
        Integer ORDER_VI_ORDER = 19;

        //20直播订单
        Integer ORDER_ZB_ORDER = 20;

        //21会员续费
        Integer MEMBER_RENEW      = 21;

        Integer PAYMENT_FINAL =23;

        //42直播佣金
        Integer Living_COMMISSION = 42;

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
     * 操作金额
     */
    private BigDecimal money;

    /**
     * 账户余额
     */
    private BigDecimal userMoney;

    /**
     * 账单类型
     */
    private Integer type;
    /**
     * 1 收入类型  2支出类型
     */
    private Integer moneyType;

    /**
     * 收入/支出 名称
     */
    private Integer moneyTypeName;
    /**
     * 到账时间/提现时间/支出时间
     */
    private Date    recordTime;
    /**
     * 备注
     */
    private String  recordNotes;

    /**
     * 充值方式/退款类型/活动类型
     */
    private String typeName;

    /**
     * 订单号
     */
    private String sNo;

    /**
     * 商品名字/活动名称
     */
    private String titleName;

    /**
     * 活动编号
     */
    private String activityCode;

    /**
     * 店铺名称
     */
    private String mchName;

    /**
     * 提现手续费
     */
    private String withdrawalFees;

    /**
     * 提现方式
     */
    private String withdrawalMethod;
    /**
     * 添加时间
     */
    private Date   addTime;

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

    public Integer getId()
    {
        return id;
    }

    public void setId(Integer id)
    {
        this.id = id;
    }

    public Integer getStore_id()
    {
        return store_id;
    }

    public void setStore_id(Integer store_id)
    {
        this.store_id = store_id;
    }

    public BigDecimal getMoney()
    {
        return money;
    }

    public void setMoney(BigDecimal money)
    {
        this.money = money;
    }

    public BigDecimal getUserMoney()
    {
        return userMoney;
    }

    public void setUserMoney(BigDecimal userMoney)
    {
        this.userMoney = userMoney;
    }

    public Integer getType()
    {
        return type;
    }

    public void setType(Integer type)
    {
        this.type = type;
    }

    public Integer getMoneyType()
    {
        return moneyType;
    }

    public void setMoneyType(Integer moneyType)
    {
        this.moneyType = moneyType;
    }

    public Date getRecordTime()
    {
        return recordTime;
    }

    public void setRecordTime(Date recordTime)
    {
        this.recordTime = recordTime;
    }

    public String getRecordNotes()
    {
        return recordNotes;
    }

    public void setRecordNotes(String recordNotes)
    {
        this.recordNotes = recordNotes;
    }

    public String getTypeName()
    {
        return typeName;
    }

    public void setTypeName(String typeName)
    {
        this.typeName = typeName;
    }

    public String getsNo()
    {
        return sNo;
    }

    public void setsNo(String sNo)
    {
        this.sNo = sNo;
    }

    public String getTitleName()
    {
        return titleName;
    }

    public void setTitleName(String titleName)
    {
        this.titleName = titleName;
    }

    public String getActivityCode()
    {
        return activityCode;
    }

    public void setActivityCode(String activityCode)
    {
        this.activityCode = activityCode;
    }

    public String getMchName()
    {
        return mchName;
    }

    public void setMchName(String mchName)
    {
        this.mchName = mchName;
    }

    public String getWithdrawalFees()
    {
        return withdrawalFees;
    }

    public void setWithdrawalFees(String withdrawalFees)
    {
        this.withdrawalFees = withdrawalFees;
    }

    public String getWithdrawalMethod()
    {
        return withdrawalMethod;
    }

    public void setWithdrawalMethod(String withdrawalMethod)
    {
        this.withdrawalMethod = withdrawalMethod;
    }

    public Date getAddTime()
    {
        return addTime;
    }

    public void setAddTime(Date addTime)
    {
        this.addTime = addTime;
    }

    public Integer getMoneyTypeName()
    {
        return moneyTypeName;
    }

    public void setMoneyTypeName(Integer moneyTypeName)
    {
        this.moneyTypeName = moneyTypeName;
    }
}
