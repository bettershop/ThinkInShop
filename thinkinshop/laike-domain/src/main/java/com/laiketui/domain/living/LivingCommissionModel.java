package com.laiketui.domain.living;

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
 * @create 2024/5/28
 * 佣金表
 */
@Table(name = "lkt_living_commission")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LivingCommissionModel implements Serializable
{

    /**
     * 未结算
     */
    public final static int COMMISSION_STATUS_UNSETTLED = 101;

    /**
     * 已结算
     */
    public final static int     COMMISSION_STATUS_SETTLED = 100;
    /**
     * id
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private             Integer id;

    /**
     * 商城id
     */
    private Integer store_id;

    /**
     * 用户id
     */
    private String user_id;

    /**
     * 直播间id
     */
    private Integer living_id;

    /**
     * 订单号
     */
    private String s_no;

    /**
     * 佣金
     */
    private BigDecimal commission;

    /**
     * 结算状态（101未发放，100已发放）
     */
    private Integer status;

    /**
     * 添加时间
     */
    private Date add_time;

    /**
     * '回收站 0.显示 1.系统回收 2用户回收 3店铺回收'
     */
    private Integer recycle;
}
