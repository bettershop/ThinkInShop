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
@Table(name = "lkt_mch_buy_power")
public class MchBuyPowerModel implements Serializable
{

    /**
     * 主键
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;

    /**
     * 店铺id
     */
    private Integer mch_id;

    /**
     * 商城id
     */
    private Integer store_id;

    /**
     * 用户id
     */
    private String user_id;

    /**
     * 金额
     */
    private BigDecimal money;

    /**
     * 添加时间
     */
    private Date add_date;

    /**
     * 回收站 0.显示 1.系统回收 2用户回收 3店铺回收
     */
    private Integer recycle;
}
