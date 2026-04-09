package com.laiketui.domain.group;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

/**
 * 拼团活动表
 *
 * @author Trick
 * @date 2023/3/20 16:16
 */
@Table(name = "lkt_group_activity")
public class GroupActivityModel implements Serializable
{

    //region 状态类型

    /**
     * 状态类型 0=未开始 1=进行中 2=已结束
     */
    public enum Status
    {
        /**
         * 未开始
         */
        GROUP_GOODS_STATUS_NO_START(0, "未开始"),
        /**
         * 活动中
         */
        GROUP_GOODS_STATUS_UNDER_WAY(1, "进行中"),
        /**
         * 已结束
         */
        GROUP_GOODS_STATUS_END(2, "已结束");

        String  value;
        Integer key;

        public String getValue()
        {
            return value;
        }

        public Integer getKey()
        {
            return key;
        }

        Status(Integer type, String name)
        {
            key = type;
            value = name;
        }

        public static String getNameByType(Integer type)
        {
            //获取所有枚举集合
            Status[] enums = values();
            for (Status enumType : enums)
            {
                if (enumType.getKey().equals(type))
                {
                    return enumType.getValue();
                }
            }
            return null;
        }
    }
    //endregion

    /**
     * 活动id
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;

    /**
     * 活动名称
     */
    private String name;

    /**
     * 商城id
     */
    private Integer store_id;

    /**
     * 店铺id
     */
    private Integer mch_id;

    /**
     * 拼团状态 0=未开始 1=拼团中 2=已结束
     */
    private Integer status;

    /**
     * 开始时间
     */
    private Date start_date;

    /**
     * 结束时间
     */
    private Date    end_date;
    /**
     * 是否自定义
     */
    private Integer isCustom;

    /**
     * 团长是否可参团
     */
    private Integer team_limit;

    /**
     * 同一商品参团限制开关
     */
    private Integer goods_limit;

    /**
     * 是否显示
     */
    private Integer is_show;

    /**
     * 添加时间
     */
    private Date add_date;

    /**
     * 删除标识
     */
    private Integer recycle;

    /**
     * 团长参团规则json {1:{canDiscount:8,openDiscount:75},2:{canDiscount:7,openDiscount:55},...}
     */
    private String team_rule;

    /**
     * 获取活动id
     *
     * @return id - 活动id
     */
    public String getId()
    {
        return id;
    }

    /**
     * 设置活动id
     *
     * @param id 活动id
     */
    public void setId(String id)
    {
        this.id = id == null ? null : id.trim();
    }

    public Integer getIsCustom()
    {
        return isCustom;
    }

    public void setIsCustom(Integer isCustom)
    {
        this.isCustom = isCustom;
    }

    /**
     * 获取活动名称
     *
     * @return name - 活动名称
     */
    public String getName()
    {
        return name;
    }

    /**
     * 设置活动名称
     *
     * @param name 活动名称
     */
    public void setName(String name)
    {
        this.name = name == null ? null : name.trim();
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
     * 获取拼团状态 0=未开始 1=拼团中 2=已结束
     *
     * @return status - 拼团状态 0=未开始 1=拼团中 2=已结束
     */
    public Integer getStatus()
    {
        return status;
    }

    /**
     * 设置拼团状态 0=未开始 1=拼团中 2=已结束
     *
     * @param status 拼团状态 0=未开始 1=拼团中 2=已结束
     */
    public void setStatus(Integer status)
    {
        this.status = status;
    }

    /**
     * 获取开始时间
     *
     * @return start_date - 开始时间
     */
    public Date getStart_date()
    {
        return start_date;
    }

    /**
     * 设置开始时间
     *
     * @param start_date 开始时间
     */
    public void setStart_date(Date start_date)
    {
        this.start_date = start_date;
    }

    /**
     * 获取结束时间
     *
     * @return end_date - 结束时间
     */
    public Date getEnd_date()
    {
        return end_date;
    }

    /**
     * 设置结束时间
     *
     * @param end_date 结束时间
     */
    public void setEnd_date(Date end_date)
    {
        this.end_date = end_date;
    }

    /**
     * 获取团长是否可参团
     *
     * @return team_limit - 团长是否可参团
     */
    public Integer getTeam_limit()
    {
        return team_limit;
    }

    /**
     * 设置团长是否可参团
     *
     * @param team_limit 团长是否可参团
     */
    public void setTeam_limit(Integer team_limit)
    {
        this.team_limit = team_limit;
    }

    /**
     * 获取同一商品参团限制开关
     *
     * @return goods_limit - 同一商品参团限制开关
     */
    public Integer getGoods_limit()
    {
        return goods_limit;
    }

    /**
     * 设置同一商品参团限制开关
     *
     * @param goods_limit 同一商品参团限制开关
     */
    public void setGoods_limit(Integer goods_limit)
    {
        this.goods_limit = goods_limit;
    }

    /**
     * 获取是否显示
     *
     * @return is_show - 是否显示
     */
    public Integer getIs_show()
    {
        return is_show;
    }

    /**
     * 设置是否显示
     *
     * @param is_show 是否显示
     */
    public void setIs_show(Integer is_show)
    {
        this.is_show = is_show;
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
     * 获取删除标识
     *
     * @return recycle - 删除标识
     */
    public Integer getRecycle()
    {
        return recycle;
    }

    /**
     * 设置删除标识
     *
     * @param recycle 删除标识
     */
    public void setRecycle(Integer recycle)
    {
        this.recycle = recycle;
    }

    /**
     * 获取团长参团规则json {1:{canDiscount:8,openDiscount:75},2:{canDiscount:7,openDiscount:55},...}
     *
     * @return team_rule - 团长参团规则json {1:{canDiscount:8,openDiscount:75},2:{canDiscount:7,openDiscount:55},...}
     */
    public String getTeam_rule()
    {
        return team_rule;
    }

    /**
     * 设置团长参团规则json {1:{canDiscount:8,openDiscount:75},2:{canDiscount:7,openDiscount:55},...}
     *
     * @param team_rule 团长参团规则json {1:{canDiscount:8,openDiscount:75},2:{canDiscount:7,openDiscount:55},...}
     */
    public void setTeam_rule(String team_rule)
    {
        this.team_rule = team_rule == null ? null : team_rule.trim();
    }
}