package com.laiketui.domain.plugin;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * 插件活动商品表
 *
 * @author Trick
 * @date 2021/11/16 19:18
 */
@Table(name = "lkt_plugin_activity")
public class PluginActivityModel implements Serializable
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;

    /**
     * 商品id
     */
    @Column(name = "goodsId")
    private Integer goodsId;

    /**
     * 插件code
     */
    private String plugin_code;

    /**
     * 开始时间
     */
    private Date starttime;

    /**
     * 结束时间
     */
    private Date endtime;

    /**
     * 状态 1未开始 2进行中 3结束
     */
    private Integer status;

    /**
     * 创建时间
     */
    private Date create_date;

    /**
     * 修改时间
     */
    private Date update_date;

    /**
     * 是否删除标识
     */
    private Integer is_delete;

    /**
     * @return id
     */
    public String getId()
    {
        return id;
    }

    /**
     * @param id
     */
    public void setId(String id)
    {
        this.id = id == null ? null : id.trim();
    }

    /**
     * 获取商品id
     *
     * @return goodsId - 商品id
     */
    public Integer getGoodsId()
    {
        return goodsId;
    }

    /**
     * 设置商品id
     *
     * @param goodsId 商品id
     */
    public void setGoodsId(Integer goodsId)
    {
        this.goodsId = goodsId;
    }

    /**
     * 获取插件code
     *
     * @return plugin_code - 插件code
     */
    public String getPlugin_code()
    {
        return plugin_code;
    }

    /**
     * 设置插件code
     *
     * @param plugin_code 插件code
     */
    public void setPlugin_code(String plugin_code)
    {
        this.plugin_code = plugin_code == null ? null : plugin_code.trim();
    }

    /**
     * 获取开始时间
     *
     * @return starttime - 开始时间
     */
    public Date getStarttime()
    {
        return starttime;
    }

    /**
     * 设置开始时间
     *
     * @param starttime 开始时间
     */
    public void setStarttime(Date starttime)
    {
        this.starttime = starttime;
    }

    /**
     * 获取结束时间
     *
     * @return endtime - 结束时间
     */
    public Date getEndtime()
    {
        return endtime;
    }

    /**
     * 设置结束时间
     *
     * @param endtime 结束时间
     */
    public void setEndtime(Date endtime)
    {
        this.endtime = endtime;
    }

    /**
     * 获取状态 1未开始 2进行中 3结束
     *
     * @return status - 状态 1未开始 2进行中 3结束
     */
    public Integer getStatus()
    {
        return status;
    }

    /**
     * 设置状态 1未开始 2进行中 3结束
     *
     * @param status 状态 1未开始 2进行中 3结束
     */
    public void setStatus(Integer status)
    {
        this.status = status;
    }

    /**
     * 获取创建时间
     *
     * @return create_date - 创建时间
     */
    public Date getCreate_date()
    {
        return create_date;
    }

    /**
     * 设置创建时间
     *
     * @param create_date 创建时间
     */
    public void setCreate_date(Date create_date)
    {
        this.create_date = create_date;
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
     * 获取是否删除标识
     *
     * @return is_delete - 是否删除标识
     */
    public Integer getIs_delete()
    {
        return is_delete;
    }

    /**
     * 设置是否删除标识
     *
     * @param is_delete 是否删除标识
     */
    public void setIs_delete(Integer is_delete)
    {
        this.is_delete = is_delete;
    }
}