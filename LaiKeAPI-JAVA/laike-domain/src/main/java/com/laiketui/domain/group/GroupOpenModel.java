package com.laiketui.domain.group;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 开团表
 *
 * @author Trick
 * @date 2023/3/20 16:16
 */
@Table(name = "lkt_group_open")
public class GroupOpenModel implements Serializable
{

    //region 状态类型 0=拼团中 1=拼团成功 2=拼团失败

    /**
     * 状态类型 0=拼团中 1=拼团成功 2=拼团失败
     */
    public enum Status
    {
        /**
         * 拼团中
         */
        GROUP_GOODS_STATUS_ING(0, "拼团中"),
        /**
         * 拼团成功
         */
        GROUP_GOODS_STATUS_SUCCESS(1, "拼团成功"),
        /**
         * 拼团失败
         */
        GROUP_GOODS_STATUS_FAIL(2, "拼团失败");

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
            GroupOpenModel.Status[] enums = values();
            for (GroupOpenModel.Status enumType : enums)
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
     * 拼团记录表id
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;

    /**
     * 团长user_id
     */
    private String userId;

    /**
     * 活动id
     */
    private String activity_id;

    /**
     * 团队人数
     */
    private Integer team_num;

    /**
     * 团长佣金
     */
    private BigDecimal team_commission;

    /**
     * 商品id
     */
    private Integer goods_id;

    /**
     * 规格id
     */
    private Integer attrId;

    /**
     * 商品拼团状态 0=拼团中 1=拼团成功 2=拼团失败
     */
    private Integer status;

    /**
     * 平台成功后的订单号
     */
    private String sno;

    /**
     * 添加时间
     */
    private Date add_date;

    /**
     * 拼团结束时间
     */
    private Date end_date;

    /**
     * 修改时间
     */
    private Date update_date;

    /**
     * 删除标识
     */
    private Integer recycle;

    /**
     * 是否已结算
     */
    private Integer is_settlement;

    public Integer getIs_settlement()
    {
        return is_settlement;
    }

    public void setIs_settlement(Integer is_settlement)
    {
        this.is_settlement = is_settlement;
    }

    public Date getEnd_date()
    {
        return end_date;
    }

    public void setEnd_date(Date end_date)
    {
        this.end_date = end_date;
    }

    public Integer getTeam_num()
    {
        return team_num;
    }

    public void setTeam_num(Integer team_num)
    {
        this.team_num = team_num;
    }

    public BigDecimal getTeam_commission()
    {
        return team_commission;
    }

    public void setTeam_commission(BigDecimal team_commission)
    {
        this.team_commission = team_commission;
    }

    public String getUserId()
    {
        return userId;
    }

    public void setUserId(String userId)
    {
        this.userId = userId;
    }

    public Integer getAttrId()
    {
        return attrId;
    }

    public void setAttrId(Integer attrId)
    {
        this.attrId = attrId;
    }

    /**
     * 获取拼团记录表id
     *
     * @return id - 拼团记录表id
     */
    public String getId()
    {
        return id;
    }

    /**
     * 设置拼团记录表id
     *
     * @param id 拼团记录表id
     */
    public void setId(String id)
    {
        this.id = id == null ? null : id.trim();
    }

    /**
     * 获取活动id
     *
     * @return activity_id - 活动id
     */
    public String getActivity_id()
    {
        return activity_id;
    }

    /**
     * 设置活动id
     *
     * @param activity_id 活动id
     */
    public void setActivity_id(String activity_id)
    {
        this.activity_id = activity_id == null ? null : activity_id.trim();
    }

    /**
     * 获取商品id
     *
     * @return goods_id - 商品id
     */
    public Integer getGoods_id()
    {
        return goods_id;
    }

    /**
     * 设置商品id
     *
     * @param goods_id 商品id
     */
    public void setGoods_id(Integer goods_id)
    {
        this.goods_id = goods_id;
    }

    /**
     * 获取商品拼团状态 0=拼团中 1=拼团成功 2=拼团失败
     *
     * @return status - 商品拼团状态 0=拼团中 1=拼团成功 2=拼团失败
     */
    public Integer getStatus()
    {
        return status;
    }

    /**
     * 设置商品拼团状态 0=拼团中 1=拼团成功 2=拼团失败
     *
     * @param status 商品拼团状态 0=拼团中 1=拼团成功 2=拼团失败
     */
    public void setStatus(Integer status)
    {
        this.status = status;
    }

    /**
     * 获取平台成功后的订单号
     *
     * @return sno - 平台成功后的订单号
     */
    public String getSno()
    {
        return sno;
    }

    /**
     * 设置平台成功后的订单号
     *
     * @param sno 平台成功后的订单号
     */
    public void setSno(String sno)
    {
        this.sno = sno == null ? null : sno.trim();
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
     * 获取修改时间
     *
     * @return update_date - 修改时间
     */
    public Date getUpdate_date()
    {
        return update_date;
    }

    /**
     * 设置修改时间
     *
     * @param update_date 修改时间
     */
    public void setUpdate_date(Date update_date)
    {
        this.update_date = update_date;
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
}