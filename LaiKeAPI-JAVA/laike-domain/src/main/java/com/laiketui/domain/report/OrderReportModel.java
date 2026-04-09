package com.laiketui.domain.report;

import javax.persistence.Column;
import javax.persistence.Table;
import java.io.Serializable;

@Table(name = "lkt_order_report")
public class OrderReportModel implements Serializable
{


    /**
     * 统计指标
     */
    @Column(name = "type")
    private int type;

    /**
     * 数量
     */
    @Column(name = "num")
    private Integer num;

    /**
     * 统计数据
     */
    @Column(name = "data")
    private String data;

    @Column(name = "store_id")
    private Integer storeId;


    /**
     * 获取统计指标
     *
     * @return type - 统计指标
     */
    public int getType()
    {
        return type;
    }

    /**
     * 设置统计指标
     *
     * @param type 统计指标
     */
    public void setType(int type)
    {
        this.type = type;
    }

    /**
     * 获取数量
     *
     * @return num - 数量
     */
    public int getNum()
    {
        return num;
    }

    /**
     * 设置数量
     *
     * @param num 数量
     */
    public void setNum(int num)
    {
        this.num = num;
    }

    /**
     * 获取统计数据
     *
     * @return data - 统计数据
     */
    public String getData()
    {
        return data;
    }

    /**
     * 设置统计数据
     *
     * @param data 统计数据
     */
    public void setData(String data)
    {
        this.data = data == null ? null : data.trim();
    }

    public Integer getStoreId()
    {
        return storeId;
    }

    public void setStoreId(Integer storeId)
    {
        this.storeId = storeId;
    }
}