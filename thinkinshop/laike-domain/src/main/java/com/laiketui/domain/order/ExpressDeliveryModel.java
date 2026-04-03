package com.laiketui.domain.order;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Table(name = "lkt_express_delivery")
public class ExpressDeliveryModel implements Serializable
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
     * 订单号
     */
    @Column(name = "sno")
    private String sNo;

    /**
     * 订单详情ID
     */
    private Integer orderDetailsId;

    /**
     * 快递公司ID
     */
    private Integer expressId;

    /**
     * 快递单号
     */
    private String courierNum;

    /**
     * 发货数量
     */
    private Integer num;

    /**
     * 发货时间
     */
    private Date deliverTime;

    /**
     * 快递100任务id
     */
    private String task_id;

    /**
     * 子单号
     */
    @Column(name = "childNum")
    private String childNum;

    /**
     * 回单号
     */
    @Column(name = "returnNum")
    private String returnNum;

    /**
     * 面单短链
     */
    private String label;

    /**
     * 快递公司订单号
     */
    @Column(name = "kdComOrderNum")
    private String kdComOrderNum;

    /**
     * 是否打印 0.未打印 1.已打印
     */
    private Integer isStatus;

    /**
     * 快递公司子表ID
     */
    private Integer subtableId;

    public String getTask_id() {
        return task_id;
    }

    public void setTask_id(String task_id) {
        this.task_id = task_id;
    }

    public Integer getId()
    {
        return id;
    }

    public void setId(Integer id)
    {
        this.id = id;
    }

    public Integer getStore_id()
    {
        return store_id;
    }

    public void setStore_id(Integer store_id)
    {
        this.store_id = store_id;
    }

    public String getsNo()
    {
        return sNo;
    }

    public void setsNo(String sNo)
    {
        this.sNo = sNo;
    }

    public Integer getOrderDetailsId()
    {
        return orderDetailsId;
    }

    public void setOrderDetailsId(Integer orderDetailsId)
    {
        this.orderDetailsId = orderDetailsId;
    }

    public Integer getExpressId()
    {
        return expressId;
    }

    public void setExpressId(Integer expressId)
    {
        this.expressId = expressId;
    }

    public String getCourierNum()
    {
        return courierNum;
    }

    public void setCourierNum(String courierNum)
    {
        this.courierNum = courierNum;
    }

    public Integer getNum()
    {
        return num;
    }

    public void setNum(Integer num)
    {
        this.num = num;
    }

    public Date getDeliverTime()
    {
        return deliverTime;
    }

    public void setDeliverTime(Date deliverTime)
    {
        this.deliverTime = deliverTime;
    }

    public String getChildNum()
    {
        return childNum;
    }

    public void setChildNum(String childNum)
    {
        this.childNum = childNum;
    }

    public String getReturnNum()
    {
        return returnNum;
    }

    public void setReturnNum(String returnNum)
    {
        this.returnNum = returnNum;
    }

    public String getLabel()
    {
        return label;
    }

    public void setLabel(String label)
    {
        this.label = label;
    }

    public String getKdComOrderNum()
    {
        return kdComOrderNum;
    }

    public void setKdComOrderNum(String kdComOrderNum)
    {
        this.kdComOrderNum = kdComOrderNum;
    }

    public Integer getIsStatus()
    {
        return isStatus;
    }

    public void setIsStatus(Integer isStatus)
    {
        this.isStatus = isStatus;
    }

    public Integer getSubtableId()
    {
        return subtableId;
    }

    public void setSubtableId(Integer subtableId)
    {
        this.subtableId = subtableId;
    }
}
