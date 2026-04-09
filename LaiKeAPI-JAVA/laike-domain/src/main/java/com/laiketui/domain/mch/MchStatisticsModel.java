package com.laiketui.domain.mch;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author zhuqingyu
 * @create 2024/8/23
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "lkt_mch_statistics")
public class MchStatisticsModel implements Serializable
{

    /**
     * 主键
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;

    /**
     * 商城id
     */
    private Integer store_id;

    /**
     * 店铺id
     */
    private Integer mch_id;

    /**
     * 待发货订单
     */
    private Integer pending_shipment;

    /**
     * 待付款订单
     */
    private Integer obligation;

    /**
     * 退款订单
     */
    private Integer refund_order;

    /**
     * 待审核订单
     */
    private Integer audit_order;

    /**
     * 待审核商品
     */
    private Integer audit_pro;

    /**
     * 库存不足商品
     */
    private Integer ckbz_pro;

    /**
     * 待结算订单
     */
    private Integer djs_order;

    /**
     * 待收货订单
     */
    private Integer dsh_order;

    /**
     * 上架商品
     */
    private Integer sj_pro;

    /**
     * 下架商品
     */
    private Integer xj_pro;

    /**
     * 商品分类
     */
    private Integer pro_class;

    /**
     * 商品品牌
     */
    private Integer pro_brand;

    /**
     * 销售商品sku
     */
    private Integer sale_pro_sku;

    /**
     * 商品sku数量
     */
    private Integer pro_sku;

    /**
     * 总客单数量
     */
    private Integer customer_num;

    /**
     * 关注的客户
     */
    private Integer attention_user_num;

    /**
     * 访问的客户
     */
    private Integer access_user_num;

    /**
     * 新增下单客户
     */
    private Integer new_pay_user;

    /**
     * 待结算金额
     */
    private BigDecimal djs_money;

    /**
     * 已提现金额
     */
    private BigDecimal ytx_money;

    /**
     * 退款金额
     */
    private BigDecimal return_money;

    /**
     * 添加时间
     */
    private Date add_date;

    /**
     * 回收站 0.显示 1.系统回收 2用户回收 3店铺回收
     */
    private Integer recycle;
}
