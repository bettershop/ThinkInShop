package com.laiketui.domain.vo.order;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;

import java.util.List;

// 主实体类
public class ShipDataVo
{

    /**
     * 物流公司id
     */
    @JSONField(name = "expressid")
    private Integer expressId;

    /**
     * 物流号
     */
    @JSONField(name = "courierNum")
    private String courierNumber;

    /**
     * 发货类型 1普通发货 2电子面单 3商家配送
     */
    private Integer type;

    /**
     * 配送员信息
     */
    private PsyInfo psyInfo;

    /**
     * 订单信息
     */
    private List<OrderDetail> orderList;

    // Getters and Setters
    public Integer getExpressId()
    {
        return expressId;
    }

    public void setExpressId(Integer expressId)
    {
        this.expressId = expressId;
    }

    public String getCourierNumber()
    {
        return courierNumber;
    }

    public void setCourierNumber(String courierNumber)
    {
        this.courierNumber = courierNumber;
    }

    public Integer getType()
    {
        return type;
    }

    public void setType(Integer type)
    {
        this.type = type;
    }

    public PsyInfo getPsyInfo()
    {
        return psyInfo;
    }

    public void setPsyInfo(PsyInfo psyInfo)
    {
        this.psyInfo = psyInfo;
    }

    public List<OrderDetail> getOrderList()
    {
        return orderList;
    }

    public void setOrderList(List<OrderDetail> orderList)
    {
        this.orderList = orderList;
    }

    // 订单详情类
    @Data
    public static class OrderDetail
    {
        private Integer detailId;
        private Integer num;
    }

    public class PsyInfo
    {
        private String name;
        private String tel;

        // Getters and Setters
        public String getName()
        {
            return name;
        }

        public void setName(String name)
        {
            this.name = name;
        }

        public String getTel()
        {
            return tel;
        }

        public void setTel(String tel)
        {
            this.tel = tel;
        }
    }
}