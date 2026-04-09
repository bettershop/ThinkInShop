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
 * 直播间产品
 */
@Table(name = "lkt_living_product")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LivingProductModel implements Serializable
{
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
     * 直播间id
     */
    private Integer living_id;

    /**
     * 商品ID
     */
    private Integer pro_id;

    /**
     * 规格ID
     */
    private Integer config_id;

    /**
     * 可卖数量
     */
    private Integer num;

    /**
     * 可卖数量
     */
    private Integer xl_num;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 添加时间
     */
    private Date add_time;

    /**
     * 是否在讲解（每个直播间只能有一个讲解的产品，默认0不讲解）
     */
    private Integer represent;

    /**
     * '回收站 0.显示 1.系统回收 2用户回收 3店铺回收'
     */
    private Integer recycle;

    /**
     * 序号
     */
    private Integer sort_num;

    /**
     * 直播价格
     */
    private BigDecimal live_price;
}
