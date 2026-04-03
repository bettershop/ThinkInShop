package com.laiketui.domain.message;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

/**
 * 消息弹窗模型类
 *
 * @author wangxian
 */
@Table(name = "lkt_message_logging")
public class MessageLoggingModal implements Serializable
{

    //管理后台新增跳转路径  禅道48521
    public interface AdminUrl
    {
        /**
         * 店铺保证金提现审核
         */
        String MCH_SECURITY_DEPOSIT_URL = "/plug_ins/stores/bondExamine";

        /**
         * 用户提现审核
         */
        String USER_WITHDRAWAL_URL = "/members/withdrawalManage/withdrawalExamineList";

        /**
         * 店铺提现审核
         */
        String STORE_WITHDRAWAL_URL = "/plug_ins/stores/withdrawalAudit";

        /**
         * 供应商提现审核
         */
        String SUPPLIER_WITHDRAWAL_URL = "/plug_ins/supplier/withdrawalAudit";

        /**
         * 用户分销提现审核
         */
        String USER_DISTRIBUTION_WITHDRAWAL_URL = "/plug_ins/distribution/withdrawalRecord";
    }

    //Pc店铺新增跳转路径  禅道48521
    public interface PcMchUrl
    {
        /**
         * Pc店铺商品审核通过
         */
        String COMMODITY_APPROVAL_URL = "/goods/goodslist/physicalgoods";

        /**
         * Pc店铺商品审核拒绝
         */
        String COMMODITY_REJECTION_URL = "/goods/auditGoods";

        /**
         * pc店铺提现审核消息通知(您的店铺提现申请提交成功，正在等待管理员审核！ + 审核通过 + 审核失败)
         */
        String WITHDRAWAL_APPLICATION_SUCCESSFULLY_SUBMITTED = "/moneyManagement/withdrawalDetails/detailsList";

        /**
         * pc店铺用户提交竞拍保证金提醒
         */
        String AUCTION_DEPOSIT_REMINDER = "/plug_ins/auction/payRecord";
    }

    /**
     * 消息类型 1.订单(待发货) 2.订单(售后) 3.订单(提醒发货) 4.订单(订单关闭) 5.订单(新订单) 6.订单(收货)
     * 7.商品(审核) 8.商品(下架) 9.商品(补货) 10.商品(新商品上架) 11.商品(分类) 12.商品(品牌)
     */
    public interface Type
    {
        /**
         * 订单(待发货)
         */
        Integer TYPE_ORDER_WAIT_SEND              = 1;
        /**
         * 订单(售后)
         */
        Integer TYPE_ORDER_RETURN                 = 2;
        /**
         * 订单(提醒发货)
         */
        Integer TYPE_ORDER_REMIND_SEND            = 3;
        /**
         * 订单(订单关闭)
         */
        Integer TYPE_ORDER_CLOSE                  = 4;
        /**
         * 订单(新订单)
         */
        Integer TYPE_ORDER_NEW                    = 5;
        /**
         * 订单(收货)
         */
        Integer TYPE_ORDER_OK_GOODS               = 6;
        /**
         * 商品(审核)
         */
        Integer TYPE_GOODS_GOODS_EXAMINE          = 7;
        /**
         * 商品(下架)
         */
        Integer TYPE_GOODS_OFF_THE_SHELF          = 8;
        /**
         * 商品(补货)
         */
        Integer TYPE_GOODS_REPLENISHMENT          = 9;
        /**
         * 商品(上架)
         */
        Integer TYPE_GOODS_ON_THE_SHELF           = 10;
        /**
         * 商品(分类)
         */
        Integer TYPE_GOODS_CLASS                  = 11;
        /**
         * 商品(品牌)
         */
        Integer TYPE_GOODS_BRAND                  = 12;
        /**
         * 账单(提现)
         */
        Integer TYPE_WITHDRAW                     = 13;
        /**
         * 供应商商品(违规下架)
         */
        Integer TYPE_SUPPLIER_GOODS_OFF_THE_SHELF = 14;
        /**
         * 商品(删除)
         */
        Integer TYPE_GOODS_DEL                    = 15;
        /**
         * h5店铺保证金审核消息通知
         */
        Integer TYPE_H5MCH_BAIL                   = 16;
        /**
         * h5店铺商品审核消息通知
         */
        Integer TYPE_H5MCH_COMMODITY_APPROVAL     = 17;
        /**
         * pc店铺商品审核消息通知
         */
        Integer TYPE_MCH_COMMODITY_APPROVAL       = 18;
        /**
         * 管理后台用户提现审核
         */
        Integer TYPE_USER_WITHDRAWAL              = 19;
        /**
         * 管理后台店铺提现审核  -- 保证金 + 余额提现
         */
        Integer TYPE_STORE_WITHDRAWAL             = 20;

        /**
         * 管理后台供应商提现审核
         */
        Integer TYPE_SUPPLIER_WITHDRAWAL = 21;

        /**
         * h5店铺用户提交竞拍保证金提醒
         */
        Integer TYPE_H5MCH_AUCTION_DEPOSIT_REMINDER = 22;

        /**
         * h5店铺提现审核消息通知(您的店铺提现申请提交成功，正在等待管理员审核！ + 审核通过 + 审核失败)
         */
        Integer TYPE_H5MCH_WITHDRAWAL_APPLICATION_SUCCESSFULLY_SUBMITTED = 23;
        /**
         * pc店铺提现审核消息通知(您的店铺提现申请提交成功，正在等待管理员审核！ + 审核通过 + 审核失败)
         */
        Integer TYPE_MCH_WITHDRAWAL_APPLICATION_SUCCESSFULLY_SUBMITTED   = 24;
        /**
         * pc店铺保证金审核消息通知  pc店铺用户提交竞拍保证金提醒
         */
        Integer TYPE_PCMCH_BAIL                                          = 25;
    }

    /**
     * ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * 商城id
     */
    private Integer store_id;

    /**
     * 店铺id
     */
    private Integer mch_id;

    /**
     * 消息类型 1.订单(待发货) 2.订单(售后) 3.订单(提醒发货) 4.订单(订单关闭) 5.订单(新订单) 6.订单(收货) 7.商品(审核) 8.商品(下架) 9.商品(补货) 10.商品(新商品上架) 11.商品(分类) 12.商品(品牌)
     */
    private Integer type;

    /**
     * 参数
     */
    private String parameter;

    /**
     * 内容
     */
    private String content;

    /**
     * 是否弹窗 0.未弹 1.已弹
     */
    private Integer is_popup;

    /**
     * 是否已读 0.未读 1.已读
     */
    private Integer read_or_not;

    /**
     * 添加时间
     */
    private Date add_date;

    /**
     * 前端路由地址 add by trick 2023-03-13 11:37:45
     */
    private String to_url;

    /**
     * 供应商id
     */
    private Integer supplier_id;

    public Integer getSupplier_id()
    {
        return supplier_id;
    }

    public void setSupplier_id(Integer supplier_id)
    {
        this.supplier_id = supplier_id;
    }

    public String getTo_url()
    {
        return to_url;
    }

    public void setTo_url(String to_url)
    {
        this.to_url = to_url;
    }

    /**
     * 获取ID
     *
     * @return id - ID
     */
    public Integer getId()
    {
        return id;
    }

    /**
     * 设置ID
     *
     * @param id ID
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
     * 获取消息类型 1.订单(待发货) 2.订单(售后) 3.订单(提醒发货) 4.订单(订单关闭) 5.订单(新订单) 6.订单(收货) 7.商品(审核) 8.商品(下架) 9.商品(补货) 10.商品(新商品上架) 11.商品(分类) 12.商品(品牌)
     *
     * @return type - 消息类型 1.订单(待发货) 2.订单(售后) 3.订单(提醒发货) 4.订单(订单关闭) 5.订单(新订单) 6.订单(收货) 7.商品(审核) 8.商品(下架) 9.商品(补货) 10.商品(新商品上架) 11.商品(分类) 12.商品(品牌)
     */
    public Integer getType()
    {
        return type;
    }

    /**
     * 设置消息类型 1.订单(待发货) 2.订单(售后) 3.订单(提醒发货) 4.订单(订单关闭) 5.订单(新订单) 6.订单(收货) 7.商品(审核) 8.商品(下架) 9.商品(补货) 10.商品(新商品上架) 11.商品(分类) 12.商品(品牌)
     *
     * @param type 消息类型 1.订单(待发货) 2.订单(售后) 3.订单(提醒发货) 4.订单(订单关闭) 5.订单(新订单) 6.订单(收货) 7.商品(审核) 8.商品(下架) 9.商品(补货) 10.商品(新商品上架) 11.商品(分类) 12.商品(品牌)
     */
    public void setType(Integer type)
    {
        this.type = type;
    }

    /**
     * 获取参数
     *
     * @return parameter - 参数
     */
    public String getParameter()
    {
        return parameter;
    }

    /**
     * 设置参数
     *
     * @param parameter 参数
     */
    public void setParameter(String parameter)
    {
        this.parameter = parameter == null ? null : parameter.trim();
    }

    /**
     * 获取内容
     *
     * @return content - 内容
     */
    public String getContent()
    {
        return content;
    }

    /**
     * 设置内容
     *
     * @param content 内容
     */
    public void setContent(String content)
    {
        this.content = content == null ? null : content.trim();
    }

    /**
     * 获取是否弹窗 0.未弹 1.已弹
     *
     * @return is_popup - 是否弹窗 0.未弹 1.已弹
     */
    public Integer getIs_popup()
    {
        return is_popup;
    }

    /**
     * 设置是否弹窗 0.未弹 1.已弹
     *
     * @param is_popup 是否弹窗 0.未弹 1.已弹
     */
    public void setIs_popup(Integer is_popup)
    {
        this.is_popup = is_popup;
    }

    /**
     * 获取是否已读 0.未读 1.已读
     *
     * @return read_or_not - 是否已读 0.未读 1.已读
     */
    public Integer getRead_or_not()
    {
        return read_or_not;
    }

    /**
     * 设置是否已读 0.未读 1.已读
     *
     * @param read_or_not 是否已读 0.未读 1.已读
     */
    public void setRead_or_not(Integer read_or_not)
    {
        this.read_or_not = read_or_not;
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