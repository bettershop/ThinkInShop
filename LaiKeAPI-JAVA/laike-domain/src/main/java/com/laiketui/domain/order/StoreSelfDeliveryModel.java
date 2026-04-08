package com.laiketui.domain.order;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

@Table(name = "lkt_self_delivery_info")
public class StoreSelfDeliveryModel implements Serializable
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
    private Date delivery_time;

    /**
     * 订单号
     */
    private String delivery_period;

    /**
     * 快递单号
     */
    private String phone;


    private String courier_name;

    public Integer getId()
    {
        return id;
    }

    public void setId(Integer id)
    {
        this.id = id;
    }

    public Date getDelivery_time()
    {
        return delivery_time;
    }

    public void setDelivery_time(Date delivery_time)
    {
        this.delivery_time = delivery_time;
    }

    public String getDelivery_period()
    {
        return delivery_period;
    }

    public void setDelivery_period(String delivery_period)
    {
        this.delivery_period = delivery_period;
    }

    public String getPhone()
    {
        return phone;
    }

    public void setPhone(String phone)
    {
        this.phone = phone;
    }

    public String getCourier_name()
    {
        return courier_name;
    }

    public void setCourier_name(String courier_name)
    {
        this.courier_name = courier_name;
    }
}
