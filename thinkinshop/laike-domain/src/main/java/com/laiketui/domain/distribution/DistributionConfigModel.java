package com.laiketui.domain.distribution;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * 分销配置
 *
 * @author Trick
 */
@Table(name = "lkt_distribution_config")
public class DistributionConfigModel implements Serializable
{

    /**
     * 按照商品利润计算
     */
    public static final int BY_SPLR = 1;
    /**
     * 按照商品售价计算
     */
    public static final int BY_SPSJ = 2;
    /**
     * 按照设置的PV值计算
     */
    public static final int BY_PV   = 3;

    /**
     * 结算方式
     **/
    public interface SettementType
    {
        /**
         * 付款后
         **/
        int SETTEMENTTYPE_PAYMENT   = 1;
        /**
         * 收货后
         **/
        int SETTEMENTTYPE_RECEIVING = 2;
    }

    /**
     * 插件开关
     */
    public interface PluginSwitch
    {
        /**
         * 开启
         */
        Integer PLUGIN_OPEN  = 1;
        /**
         * 关闭
         */
        Integer PLUGIN_CLOSE = 0;
    }

    /**
     * 关系确定方式 0注册确定 1消费确定
     */
    public interface RelationshipType
    {
        /**
         * 注册确定
         */
        Integer PLUGIN_OPEN  = 0;
        /**
         * 消费确定
         */
        Integer PLUGIN_CLOSE = 1;
    }

    /**
     * 分销规则
     * a:6:{s:8:"c_cengji";s:1:"1";s:9:"c_uplevel";s:1:"1";s:8:"c_neigou";s:1:"1";s:5:"c_pay";s:1:"2";s:10:"c_yjjisuan";s:1:"0";s:7:"content";s:0:"";}
     * c_cengji = 分销层级（0.不开启分销机制  1.一级分销 2.二级分销  )
     * c_uplevel = 等级晋升设置（1.满足任意一项升级，2.满足所有项升级）
     * c_neigou = 分销内购（1.关闭  2.开启）
     * c_pay = 规则结算方式（1.付款后  2.完成后
     * c_yjjisuan = 佣金计算方式（0.默认佣金计算方式订单成交价 1利润 2规格售价 3PV值）
     * content = 分销规则
     */
    public interface SetsKey
    {
        /**
         * 分销层级
         */
        String C_CENGJI   = "c_cengji";
        /**
         * 等级晋升设置
         */
        String C_UPLEVEL  = "c_uplevel";
        /**
         * 分销内购
         */
        String C_NEIGOU   = "c_neigou";
        /**
         * 规则结算方式
         */
        String C_PAY      = "c_pay";
        /**
         * 佣金计算方式
         */
        String C_YJJISUAN = "c_yjjisuan";
        /**
         * 分销规则
         */
        String CONTENT    = "content";

        /**
         * c_cengji = 分销层级（0.不开启分销机制  1.一级分销 2.二级分销  )
         * c_uplevel = 等级晋升设置（1.满足任意一项升级，2.满足所有项升级）
         * c_neigou = 分销内购（1.关闭  2.开启）
         * c_pay = 规则结算方式（1.付款后  2.完成后
         * c_yjjisuan = 佣金计算方式（0.默认佣金计算方式订单成交价 1利润 2规格售价 3PV值）
         */
        public interface SetsValue
        {
            /**
             * 不开启分销机制
             */
            Integer C_CENGJI_OFF = 0;
            /**
             * 一级分销
             */
            Integer C_CENGJI_ONE = 1;
            /**
             * 二级分销
             */
            Integer C_CENGJI_TWO = 2;

            /**
             * 满足任意一项升级
             */
            Integer C_UPLEVEL_ONE = 1;
            /**
             * 满足所有项升级
             */
            Integer C_UPLEVEL_ALL = 2;

            /**
             * 分销内购-关闭
             */
            Integer C_NEIGOU_CLOSE = 1;
            /**
             * 分销内购-开启
             */
            Integer C_NEIGOU_OPEN  = 2;

            /**
             * 规则结算方式-付款后
             */
            Integer C_PAY_PAYMENT = 1;
            /**
             * 规则结算方式-完成后
             */
            Integer C_PAY_SUCCESS = 2;

            /**
             * 佣金计算方式-默认佣金计算方式订单成交价
             */
            Integer C_YJJISUAN_DEFAULT    = 0;
            /**
             * 规则结算方式-利润
             */
            Integer C_YJJISUAN_PROFIT     = 1;
            /**
             * 规则结算方式-规格售价
             */
            Integer C_YJJISUAN_ATTR_PRICE = 2;
            /**
             * 规则结算方式-PV值
             */
            Integer C_YJJISUAN_PV         = 3;

        }


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

    private String sets;

    /**
     * 插件状态 0关闭 1开启
     */
    private Integer status;

    /**
     * 是否开启加盟广告 0关闭 1开启
     */
    private Integer advertising;

    /**
     * 广告地址
     */
    private String ad_image;

    /**
     * 关系确定方式 0注册确定 1消费确定
     */
    private Integer relationship;

    public Integer getRelationship()
    {
        return relationship;
    }

    public void setRelationship(Integer relationship)
    {
        this.relationship = relationship;
    }

    public String getAd_image()
    {
        return ad_image;
    }

    public void setAd_image(String ad_image)
    {
        this.ad_image = ad_image;
    }

    public Integer getAdvertising()
    {
        return advertising;
    }

    public void setAdvertising(Integer advertising)
    {
        this.advertising = advertising;
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
     * a:6:{s:8:"c_cengji";s:1:"1";s:9:"c_uplevel";s:1:"1";s:8:"c_neigou";s:1:"1";s:5:"c_pay";s:1:"2";s:10:"c_yjjisuan";s:1:"0";s:7:"content";s:0:"";}
     * c_cengji = 分销层级（0.不开启分销机制  1.一级分销 2.二级分销  )
     * c_uplevel = 等级晋升设置（1.满足任意一项升级，2.满足所有项升级）
     * c_neigou = 分销内购（1.关闭  2.开启）
     * c_pay = 规则结算方式（1.付款后  2.完成后
     * c_yjjisuan = 佣金计算方式（0.默认佣金计算方式订单成交价 1利润 2规格售价 3PV值）
     * content = 分销规则
     *
     * @return sets
     */
    public String getSets()
    {
        return sets;
    }

    /**
     * @param sets
     */
    public void setSets(String sets)
    {
        this.sets = sets == null ? null : sets.trim();
    }

    /**
     * 获取插件状态 0关闭 1开启
     *
     * @return status - 插件状态 0关闭 1开启
     */
    public Integer getStatus()
    {
        return status;
    }

    /**
     * 设置插件状态 0关闭 1开启
     *
     * @param status 插件状态 0关闭 1开启
     */
    public void setStatus(Integer status)
    {
        this.status = status;
    }
}