package com.laiketui.domain.vo.mch;

import com.laiketui.domain.vo.MainVo;
import io.swagger.annotations.ApiModel;


/**
 * 发货参数
 *
 * @author Trick
 * @date 2020/11/26 16:36
 */
@ApiModel(description = "发货参数")
public class FrontDeliveryVo extends MainVo
{

    /**
     * 用户id
     */
    private String userId;

    /**
     * 订单明细id数组
     */
    private String orderDetailsId;

    /**
     * 快递单号
     */
    private String courierNum;

    /**
     * 订单号
     */
    private String sNo;

    /**
     * 微信id
     */
    private String wxid;

    /**
     * 配送员姓名
     */
    private String courier_name;

    /**
     * 配送员电话
     */
    private String phone;

    public String getCourier_name()
    {
        return courier_name;
    }

    public void setCourier_name(String courier_name)
    {
        this.courier_name = courier_name;
    }

    public String getPhone()
    {
        return phone;
    }

    public void setPhone(String phone)
    {
        this.phone = phone;
    }

    /**
     * 快递公司id
     */
    private Integer expressId;

    public String getUserId()
    {
        return userId;
    }

    public void setUserId(String userId)
    {
        this.userId = userId;
    }

    public String getOrderDetailsId()
    {
        return orderDetailsId;
    }

    public void setOrderDetailsId(String orderDetailsId)
    {
        this.orderDetailsId = orderDetailsId;
    }

    public String getsNo()
    {
        return sNo;
    }

    public void setsNo(String sNo)
    {
        this.sNo = sNo;
    }

    public String getWxid()
    {
        return wxid;
    }

    public void setWxid(String wxid)
    {
        this.wxid = wxid;
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
}
