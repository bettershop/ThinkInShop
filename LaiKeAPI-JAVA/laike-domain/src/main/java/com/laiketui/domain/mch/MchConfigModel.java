package com.laiketui.domain.mch;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;

@Table(name = "lkt_mch_config")
public class MchConfigModel implements Serializable
{
    /**
     * 不限制
     */
    @Transient
    public static final Integer NO_LIMIT            = 0;
    /**
     * 指定时间
     */
    @Transient
    public static final Integer SPECIFY_TIME        = 1;
    /**
     * 指定时间段
     */
    @Transient
    public static final Integer SPECIFY_TIME_PERIOD = 2;

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
     * 店铺默认logo
     */
    private String logo;

    /**
     * 结算方式
     */
    private String settlement;

    /**
     * 最低提现金额
     */
    private BigDecimal min_charge;

    /**
     * 最大提现金额
     */
    private BigDecimal max_charge;

    /**
     * 手续费
     */
    private BigDecimal service_charge;

    /**
     * 提现说明
     */
    private String illustrate;

    /**
     * 入驻协议
     */
    private String agreement;

    /**
     * 删除设置
     */
    private Integer delete_settings;

    /**
     * 是否开启插件
     */
    private Integer is_display;

    /**
     * 保证金开关
     */
    private Integer promise_switch;

    /**
     * 保证金金额
     */
    private BigDecimal promise_amt;

    /**
     * 保证金说明
     */
    private String promise_text;

    /**
     * 包邮设置 0.未开启 1.开启
     */
    private Integer package_settings;

    /**
     * 同件
     */
    private Integer same_piece;

    /**
     * 同单
     */
    private Integer same_order;

    /**
     * 同单
     */
    private Integer mch_id;

    /**
     * 店铺新增宣传图
     */
    private String  poster_img;
    /**
     * 店铺头像
     */
    private String  head_img;
    /**
     * 店铺自动审核天数
     */
    private Integer auto_examine;

    /**
     * 自动注销时间(月)
     */
    private Integer auto_log_off;

    /**
     * 提现时间开关 0.不限制 1.指定日期 2.指定时间段
     */
    private Integer withdrawal_time_open;

    /**
     * 指定时间(时间段:15-20)
     */
    private String withdrawal_time;

    public Integer getAuto_log_off()
    {
        return auto_log_off;
    }

    public void setAuto_log_off(Integer auto_log_off)
    {
        this.auto_log_off = auto_log_off;
    }

    public Integer getWithdrawal_time_open()
    {
        return withdrawal_time_open;
    }

    public void setWithdrawal_time_open(Integer withdrawal_time_open)
    {
        this.withdrawal_time_open = withdrawal_time_open;
    }

    public String getWithdrawal_time()
    {
        return withdrawal_time;
    }

    public void setWithdrawal_time(String withdrawal_time)
    {
        this.withdrawal_time = withdrawal_time;
    }

    public Integer getAuto_examine()
    {
        return auto_examine;
    }

    public void setAuto_examine(Integer auto_examine)
    {
        this.auto_examine = auto_examine;
    }

    public String getPoster_img()
    {
        return poster_img;
    }

    public void setPoster_img(String poster_img)
    {
        this.poster_img = poster_img;
    }

    public String getHead_img()
    {
        return head_img;
    }

    public void setHead_img(String head_img)
    {
        this.head_img = head_img;
    }

    public Integer getPackage_settings()
    {
        return package_settings;
    }

    public void setPackage_settings(Integer package_settings)
    {
        this.package_settings = package_settings;
    }

    public Integer getSame_piece()
    {
        return same_piece;
    }

    public void setSame_piece(Integer same_piece)
    {
        this.same_piece = same_piece;
    }

    public Integer getSame_order()
    {
        return same_order;
    }

    public void setSame_order(Integer same_order)
    {
        this.same_order = same_order;
    }

    public Integer getMch_id()
    {
        return mch_id;
    }

    public void setMch_id(Integer mch_id)
    {
        this.mch_id = mch_id;
    }

    public String getPromise_text()
    {
        return promise_text;
    }

    public void setPromise_text(String promise_text)
    {
        this.promise_text = promise_text;
    }

    public Integer getIs_display()
    {
        return is_display;
    }

    public void setIs_display(Integer is_display)
    {
        this.is_display = is_display;
    }

    public Integer getPromise_switch()
    {
        return promise_switch;
    }

    public void setPromise_switch(Integer promise_switch)
    {
        this.promise_switch = promise_switch;
    }

    public BigDecimal getPromise_amt()
    {
        return promise_amt;
    }

    public void setPromise_amt(BigDecimal promise_amt)
    {
        this.promise_amt = promise_amt;
    }

    /**
     * 商品设置  1.上传商品 2.
     * <p>
     * 自选商品
     */
    private String commodity_setup;

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
     * 获取店铺默认logo
     *
     * @return logo - 店铺默认logo
     */
    public String getLogo()
    {
        return logo;
    }

    /**
     * 设置店铺默认logo
     *
     * @param logo 店铺默认logo
     */
    public void setLogo(String logo)
    {
        this.logo = logo == null ? null : logo.trim();
    }

    /**
     * 获取结算方式
     *
     * @return settlement - 结算方式
     */
    public String getSettlement()
    {
        return settlement;
    }

    /**
     * 设置结算方式
     *
     * @param settlement 结算方式
     */
    public void setSettlement(String settlement)
    {
        this.settlement = settlement == null ? null : settlement.trim();
    }

    /**
     * 获取最低提现金额
     *
     * @return min_charge - 最低提现金额
     */
    public BigDecimal getMin_charge()
    {
        return min_charge;
    }

    /**
     * 设置最低提现金额
     *
     * @param min_charge 最低提现金额
     */
    public void setMin_charge(BigDecimal min_charge)
    {
        this.min_charge = min_charge;
    }

    /**
     * 获取最大提现金额
     *
     * @return max_charge - 最大提现金额
     */
    public BigDecimal getMax_charge()
    {
        return max_charge;
    }

    /**
     * 设置最大提现金额
     *
     * @param max_charge 最大提现金额
     */
    public void setMax_charge(BigDecimal max_charge)
    {
        this.max_charge = max_charge;
    }

    /**
     * 获取手续费
     *
     * @return service_charge - 手续费
     */
    public BigDecimal getService_charge()
    {
        return service_charge;
    }

    /**
     * 设置手续费
     *
     * @param service_charge 手续费
     */
    public void setService_charge(BigDecimal service_charge)
    {
        this.service_charge = service_charge;
    }

    /**
     * 获取提现说明
     *
     * @return illustrate - 提现说明
     */
    public String getIllustrate()
    {
        return illustrate;
    }

    /**
     * 设置提现说明
     *
     * @param illustrate 提现说明
     */
    public void setIllustrate(String illustrate)
    {
        this.illustrate = illustrate == null ? null : illustrate.trim();
    }

    /**
     * 获取入驻协议
     *
     * @return agreement - 入驻协议
     */
    public String getAgreement()
    {
        return agreement;
    }

    /**
     * 设置入驻协议
     *
     * @param agreement 入驻协议
     */
    public void setAgreement(String agreement)
    {
        this.agreement = agreement == null ? null : agreement.trim();
    }

    /**
     * 获取删除设置
     *
     * @return delete_settings - 删除设置
     */
    public Integer getDelete_settings()
    {
        return delete_settings;
    }

    /**
     * 设置删除设置
     *
     * @param delete_settings 删除设置
     */
    public void setDelete_settings(Integer delete_settings)
    {
        this.delete_settings = delete_settings;
    }

    /**
     * 获取商品设置  1.上传商品 2.
     * <p>
     * 自选商品
     *
     * @return commodity_setup - 商品设置  1.上传商品 2.
     * <p>
     * 自选商品
     */
    public String getCommodity_setup()
    {
        return commodity_setup;
    }

    /**
     * 设置商品设置  1.上传商品 2.
     * <p>
     * 自选商品
     *
     * @param commodity_setup 商品设置  1.上传商品 2.
     *                        <p>
     *                        自选商品
     */
    public void setCommodity_setup(String commodity_setup)
    {
        this.commodity_setup = commodity_setup == null ? null : commodity_setup.trim();
    }
}