package com.laiketui.domain.distribution;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;


/**
 * 分销商品表
 *
 * @author Trick
 * @date 2021/2/7 17:18
 */
@Table(name = "lkt_distribution_goods")
public class DistributionGoodsModel implements Serializable
{
    /**
     * 等级规则
     */
    @Transient
    public static final int DISTRIBUTION_RULE_LEVEL  = 1;
    /**
     * 自定义
     */
    @Transient
    public static final int DISTRIBUTION_RULE_CUSTOM = 2;

    /**
     * 商品分佣规则
     */
    public interface DistributionRuleKey
    {
        /**
         * 直推分销比例发放模式 0.百分比 1.固定金额
         */
        String DIRECT_M_TYPE    = "direct_m_type";
        String DIRECT_MODE_TYPE = "direct_mode_type";
        /**
         * 直推分佣金额
         */
        String DIRECT_M     = "direct_m";
        /**
         * 间推分销比例发放模式 0.百分比 1.固定金额
         */
        @Deprecated
        String INDIRECT_M_TYPE = "indirect_m_type";
        String INDIRECT_MODE_TYPE = "indirect_mode_type";
        /**
         * 同【INDIRECT_M_TYPE】
         * 间推分销比例发放模式 0.百分比 1.固定金额
         */
        String INDIRECT_M_TYPE1 = "indirectMType";
        /**
         * 间推分佣金额
         */
        String INDIRECT_M       = "indirect_m";


    }

    public interface DistributionRuleValue
    {
        /**
         * 直推分销比例发放模式 0.百分比
         */
        String DIRECT_M_TYPE_PERCENTAGE = "0";
        /**
         * 直推分销比例发放模式 1.固定金额
         */
        String DIRECT_M_TYPE_FIXED      = "1";

        /**
         * 分佣规则设置 1等级
         */
        Integer DISTRIBUTION_RULE_LEVEL = 1;
        /**
         * 分佣规则设置 2自定义
         */
        Integer DISTRIBUTION_RULE_DIY   = 2;
    }

    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * 商城ID
     */
    private Integer store_id;

    /**
     * 商品ID
     */
    private Integer p_id;

    /**
     * 属性ID
     */
    private Integer s_id;

    /**
     * pv值
     */
    private BigDecimal pv;

    /**
     * 晋升等级
     */
    private Integer uplevel;

    /**
     * 分销规则类型 1等级规则 2自定义
     */
    private Integer distribution_rule;

    /**
     * 自定义规则设置
     */
    private String rules_set;

    /**
     * 添加时间
     */
    private Date add_time;

    /**
     * 回收站 0.不回收 1.回收
     */
    private Integer recycle;

    /**
     * 获取主键ID
     *
     * @return id - 主键ID
     */
    public Integer getId()
    {
        return id;
    }

    /**
     * 设置主键ID
     *
     * @param id 主键ID
     */
    public void setId(Integer id)
    {
        this.id = id;
    }

    /**
     * 获取商城ID
     *
     * @return store_id - 商城ID
     */
    public Integer getStore_id()
    {
        return store_id;
    }

    /**
     * 设置商城ID
     *
     * @param store_id 商城ID
     */
    public void setStore_id(Integer store_id)
    {
        this.store_id = store_id;
    }

    /**
     * 获取商品ID
     *
     * @return p_id - 商品ID
     */
    public Integer getP_id()
    {
        return p_id;
    }

    /**
     * 设置商品ID
     *
     * @param p_id 商品ID
     */
    public void setP_id(Integer p_id)
    {
        this.p_id = p_id;
    }

    /**
     * 获取属性ID
     *
     * @return s_id - 属性ID
     */
    public Integer getS_id()
    {
        return s_id;
    }

    /**
     * 设置属性ID
     *
     * @param s_id 属性ID
     */
    public void setS_id(Integer s_id)
    {
        this.s_id = s_id;
    }

    /**
     * 获取pv值
     *
     * @return pv - pv值
     */
    public BigDecimal getPv()
    {
        return pv;
    }

    /**
     * 设置pv值
     *
     * @param pv pv值
     */
    public void setPv(BigDecimal pv)
    {
        this.pv = pv;
    }

    /**
     * 获取晋升等级
     *
     * @return uplevel - 晋升等级
     */
    public Integer getUplevel()
    {
        return uplevel;
    }

    /**
     * 设置晋升等级
     *
     * @param uplevel 晋升等级
     */
    public void setUplevel(Integer uplevel)
    {
        this.uplevel = uplevel;
    }

    /**
     * 获取分销规则类型 1等级规则 2自定义
     *
     * @return distribution_rule - 分销规则类型 1等级规则 2自定义
     */
    public Integer getDistribution_rule()
    {
        return distribution_rule;
    }

    /**
     * 设置分销规则类型 1等级规则 2自定义
     *
     * @param distribution_rule 分销规则类型 1等级规则 2自定义
     */
    public void setDistribution_rule(Integer distribution_rule)
    {
        this.distribution_rule = distribution_rule;
    }

    /**
     * 获取自定义规则设置
     *
     * @return rules_set - 自定义规则设置
     */
    public String getRules_set()
    {
        return rules_set;
    }

    /**
     * 设置自定义规则设置
     *
     * @param rules_set 自定义规则设置
     */
    public void setRules_set(String rules_set)
    {
        this.rules_set = rules_set == null ? null : rules_set.trim();
    }

    /**
     * 获取添加时间
     *
     * @return add_time - 添加时间
     */
    public Date getAdd_time()
    {
        return add_time;
    }

    /**
     * 设置添加时间
     *
     * @param add_time 添加时间
     */
    public void setAdd_time(Date add_time)
    {
        this.add_time = add_time;
    }

    /**
     * 获取回收站 0.不回收 1.回收
     *
     * @return recycle - 回收站 0.不回收 1.回收
     */
    public Integer getRecycle()
    {
        return recycle;
    }

    /**
     * 设置回收站 0.不回收 1.回收
     *
     * @param recycle 回收站 0.不回收 1.回收
     */
    public void setRecycle(Integer recycle)
    {
        this.recycle = recycle;
    }
}