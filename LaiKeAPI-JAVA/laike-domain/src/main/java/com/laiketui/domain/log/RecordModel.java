package com.laiketui.domain.log;

import com.laiketui.domain.Page;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * 操作记录表
 *
 * @author Trick
 * @date 2022/11/28 16:27
 */
@Table(name = "lkt_record")
public class RecordModel implements Serializable
{
    /**
     * id
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Page page;

    /**
     * 退款 请使用 RecordModel.RecordType
     */
    @Transient
    @Deprecated
    public static final int RECORDTYPE_RETURNAMT = 5;
    /**
     * 余额消费 请使用 RecordModel.RecordType
     */
    @Transient
    @Deprecated
    public static final int BUYING_MEMBERS       = 4;


    /**
     * 支出
     */
    @Transient
    public static final List<Integer> TYPE_EXPENDITURE = Arrays.asList(RecordType.APPLICATION_FOR_WITHDRAWAL, RecordType.BALANCE_CONSUMPTION, RecordType.SYSTEM_DEDUCTION, RecordType.BALANCE_TO_FRIEND, RecordType.PAY_AUCTION_DEPOSIT,
            RecordType.PAY_MCH_DEPOSIT);
    /**
     * 收入
     */
    @Transient
    public static final List<Integer> TYPE_INCOME      = Arrays.asList(RecordModel.RecordType.RECHARGE, RecordModel.RecordType.REFUND, RecordModel.RecordType.RED_ENVELOPE_WITHDRAWAL, RecordModel.RecordType.TRANSFER_IN_BALANCE, RecordModel.RecordType.SYSTEM_RECHARGE, RecordType.REFUND_AUCTION_DEPOSIT
            , RecordType.REFUND_MCH_DEPOSIT);


    /**
     * 充值明细
     */
    @Transient
    public static final List<Integer> TYPE_RECHARGE = Arrays.asList(RecordType.SYSTEM_RECHARGE, RecordType.RECHARGE);

    /**
     * 提现明细
     */
    @Transient
    public static final List<Integer> TYPE_CASH_WITHDRAWAL = Arrays.asList(RecordType.APPLICATION_FOR_WITHDRAWAL, RecordType.WITHDRAWAL_FAILED, RecordType.WITHDRAWAL_SUCCEED);

    /**
     * 商城id
     */
    private Integer store_id;

    /**
     * 用户id
     */
    private String user_id;

    /**
     * 操作金额
     */
    private BigDecimal money = new BigDecimal("0");

    /**
     * 原有金额
     */
    private BigDecimal oldmoney = new BigDecimal("0");

    /**
     * 添加时间
     */
    private Date add_date;

    /**
     * 事件
     */
    private String event;

    /**
     * 类型 0:登录/退出 1:充值 2:申请提现 3:分享 4:余额消费 5:退款 6:红包提现 7:佣金 8:管理佣金 9:待定 10:消费金 11:系统扣款
     * 12:给好友转余额 13:转入余额 14:系统充值 15:系统充积分 16:系统充消费金 17:系统扣积分 18:系统扣消费金 19:消费金解封 20:抽奖中奖
     * 21:  提现成功 22:提现失败 23.取消订单  24分享获取红包 26 交竞拍押金 27 退竞拍押金 28 售后（仅退款） 29 售后（退货退款）30 会员返现
     */
    private Integer type;

    /**
     * 类型 0:登录/退出 1:充值 2:申请提现 3:分享 4:余额消费 5:退款 6:红包提现 7:佣金 8:管理佣金 9:待定 10:消费金 11:系统扣款
     * 12:给好友转余额 13:转入余额 14:系统充值 15:系统充积分 16:系统充消费金 17:系统扣积分 18:系统扣消费金 19:消费金解封 20:抽奖中奖
     * 21:  提现成功 22:提现失败 23.取消订单  24分享获取红包 26 交竞拍押金 27 退竞拍押金 28 售后（仅退款） 29 售后（退货退款）30 会员返现
     */
    public interface RecordType
    {
        /**
         * 0:登录/退出
         */
        Integer LOGIN_OR_OUT               = 0;
        /**
         * 充值
         */
        Integer RECHARGE                   = 1;
        /**
         * 申请提现
         */
        Integer APPLICATION_FOR_WITHDRAWAL = 2;
        /**
         * 分享
         */
        @Deprecated
        Integer SHARE = 3;
        /**
         * 余额消费
         */
        Integer BALANCE_CONSUMPTION           = 4;
        /**
         * 退款
         */
        Integer REFUND                        = 5;
        /**
         * 红包提现
         */
        Integer RED_ENVELOPE_WITHDRAWAL       = 6;
        /**
         * 佣金
         */
        Integer COMMISSION                    = 7;
        /**
         * 管理佣金
         */
        Integer MANAGEMENT_COMMISSION         = 8;
        /**
         * 待定
         */
        Integer UNDETERMINED                  = 9;
        /**
         * 消费金
         */
        Integer CONSUMPTION_FUND              = 10;
        /**
         * 系统扣款
         */
        Integer SYSTEM_DEDUCTION              = 11;
        /**
         * 给好友转余额
         */
        Integer BALANCE_TO_FRIEND             = 12;
        /**
         * 转入余额
         */
        Integer TRANSFER_IN_BALANCE           = 13;
        /**
         * 系统充值
         */
        Integer SYSTEM_RECHARGE               = 14;
        /**
         * 系统充积分
         */
        Integer SYSTEM_RECHARGE_INTEGRAL      = 15;
        /**
         * 系统充消费金
         */
        Integer SYSTEM_CONSUMPTION_FUND       = 16;
        /**
         * 系统扣积分
         */
        Integer SYSTEM_BUCKLE_INTEGRAL        = 17;
        /**
         * 系统扣消费金
         */
        Integer SYSTEM_BUCKLE_CONSUMPTION     = 18;
        /**
         * 消费金解封
         */
        Integer UNSEAL_CONSUMPTION_FUND       = 19;
        /**
         * 抽奖中奖
         */
        Integer LUCKY_DRAW                    = 20;
        /**
         * 提现成功
         */
        Integer WITHDRAWAL_SUCCEED            = 21;
        /**
         * 提现失败
         */
        Integer WITHDRAWAL_FAILED             = 22;
        /**
         * 取消订单
         */
        Integer CANCEL_ORDER                  = 23;
        /**
         * 分享获取红包
         */
        Integer SHARE_GET_RED_ENVELOPES       = 24;
        /**
         * 交竞拍押金
         */
        Integer PAY_AUCTION_DEPOSIT           = 26;
        /**
         * 退竞拍押金
         */
        Integer REFUND_AUCTION_DEPOSIT        = 27;
        /**
         * 售后（仅退款）
         */
        Integer ONLY_REFUND                   = 28;
        /**
         * 售后（退货退款）
         */
        Integer RETURN_REFUND                 = 29;
        /**
         * 会员返现
         */
        Integer MEMBER_CASH_BACK              = 30;
        /**
         * 线上支付
         */
        Integer BALANCE_THIRDHAND_CONSUMPTION = 36;
        /**
         * 第三方退款
         */
        Integer MEMBER_THIRDHAND_REFUND       = 37;
        /**
         * 缴纳店铺押金
         */
        Integer PAY_MCH_DEPOSIT               = 38;
        /**
         * 退店铺押金
         */
        Integer REFUND_MCH_DEPOSIT            = 39;
        /**
         * 缴纳店铺保证金记录
         */
        Integer PAY_MCH_BOND                  = 40;
        /**
         * 退还店铺保证金记录
         */
        Integer REFUND_MCH_BOND               = 41;

        /**
         * 直播佣金转入
         */
        Integer LIVING_COMMISSION = 42;
    }

    /**
     * 是否是店铺提现 0.不是店铺提现 1.是店铺提现
     */
    private Integer is_mch;

    /**
     * 外键id
     */
    @Column(name = "main_id")
    private String main_id;

    /**
     * 记录详情id
     */
    private Integer details_id;

    public String getMain_id()
    {
        return main_id;
    }

    public void setMain_id(String main_id)
    {
        this.main_id = main_id;
    }

    public Integer getDetails_id()
    {
        return details_id;
    }

    public void setDetails_id(Integer details_id)
    {
        this.details_id = details_id;
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
     * 获取操作金额
     *
     * @return money - 操作金额
     */
    public BigDecimal getMoney()
    {
        return money;
    }

    /**
     * 设置操作金额
     *
     * @param money 操作金额
     */
    public void setMoney(BigDecimal money)
    {
        this.money = money;
    }

    /**
     * 获取原有金额
     *
     * @return oldmoney - 原有金额
     */
    public BigDecimal getOldmoney()
    {
        return oldmoney;
    }

    /**
     * 设置原有金额
     *
     * @param oldmoney 原有金额
     */
    public void setOldmoney(BigDecimal oldmoney)
    {
        this.oldmoney = oldmoney;
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
     * 获取事件
     *
     * @return event - 事件
     */
    public String getEvent()
    {
        return event;
    }

    /**
     * 设置事件
     *
     * @param event 事件
     */
    public void setEvent(String event)
    {
        this.event = event == null ? null : event.trim();
    }

    /**
     * 获取类型 0:登录/退出 1:充值 2:申请提现 3:分享 4:余额消费 5:退款 6:红包提现 7:佣金 8:管理佣金 9:待定 10:消费金 11:系统扣款   12:给好友转余额 13:转入余额 14:系统充值 15:系统充积分 16:系统充消费金 17:系统扣积分 18:系统扣消费金 19:消费金解封 20:抽奖中奖 21:  提现成功 22:提现失败 23.取消订单  24分享获取红包 26 交竞拍押金 27 退竞拍押金 28 售后（仅退款） 29 售后（退货退款）30 会员返现
     * 31 = 积分消费
     *
     * @return type - 类型 0:登录/退出 1:充值 2:申请提现 3:分享 4:余额消费 5:退款 6:红包提现 7:佣金 8:管理佣金 9:待定 10:消费金 11:系统扣款   12:给好友转余额 13:转入余额 14:系统充值 15:系统充积分 16:系统充消费金 17:系统扣积分 18:系统扣消费金 19:消费金解封 20:抽奖中奖 21:  提现成功 22:提现失败 23.取消订单  24分享获取红包 26 交竞拍押金 27 退竞拍押金 28 售后（仅退款） 29 售后（退货退款）30 会员返现
     */
    public Integer getType()
    {
        return type;
    }

    /**
     * 设置类型 0:登录/退出 1:充值 2:申请提现 3:分享 4:余额消费 5:退款 6:红包提现 7:佣金 8:管理佣金 9:待定 10:消费金 11:系统扣款   12:给好友转余额 13:转入余额 14:系统充值 15:系统充积分 16:系统充消费金 17:系统扣积分 18:系统扣消费金 19:消费金解封 20:抽奖中奖 21:  提现成功 22:提现失败 23.取消订单  24分享获取红包 26 交竞拍押金 27 退竞拍押金 28 售后（仅退款） 29 售后（退货退款）30 会员返现
     * 31 = 积分消费
     *
     * @param type 类型 0:登录/退出 1:充值 2:申请提现 3:分享 4:余额消费 5:退款 6:红包提现 7:佣金 8:管理佣金 9:待定 10:消费金 11:系统扣款
     *             12:给好友转余额 13:转入余额 14:系统充值 15:系统充积分 16:系统充消费金 17:系统扣积分 18:系统扣消费金 19:消费金解封
     *             20:抽奖中奖 21:  提现成功 22:提现失败 23.取消订单  24分享获取红包 26 交竞拍押金 27 退竞拍押金 28 售后（仅退款） 29 售后（退货退款）30 会员返现
     */
    public void setType(Integer type)
    {
        this.type = type;
    }

    /**
     * 获取是否是店铺提现 0.不是店铺提现 1.是店铺提现
     *
     * @return is_mch - 是否是店铺提现 0.不是店铺提现 1.是店铺提现
     */
    public Integer getIs_mch()
    {
        return is_mch;
    }

    /**
     * 设置是否是店铺提现 0.不是店铺提现 1.是店铺提现
     *
     * @param is_mch 是否是店铺提现 0.不是店铺提现 1.是店铺提现
     */
    public void setIs_mch(Integer is_mch)
    {
        this.is_mch = is_mch;
    }

    public Page getPage()
    {
        return page;
    }

    public void setPage(Page page)
    {
        this.page = page;
    }

    public RecordModel(Integer store_id, String user_id, BigDecimal money, BigDecimal oldmoney, Date add_date, String event, Integer type)
    {
        this.store_id = store_id;
        this.user_id = user_id;
        this.money = money;
        this.oldmoney = oldmoney;
        this.add_date = add_date;
        this.event = event;
        this.type = type;
    }

    public RecordModel(Integer store_id, String user_id, BigDecimal money, BigDecimal oldmoney, Date add_date, String event, Integer type, Integer detailsId)
    {
        this.store_id = store_id;
        this.user_id = user_id;
        this.money = money;
        this.oldmoney = oldmoney;
        this.add_date = add_date;
        this.event = event;
        this.type = type;
        this.details_id = detailsId;
    }

    public RecordModel()
    {
    }

}