package com.laiketui.domain.vo.order;

import com.laiketui.core.annotation.ParamsMapping;
import com.laiketui.domain.vo.MainVo;
import io.swagger.annotations.ApiModel;

/**
 * 售后详情
 *
 * @author wangxain
 */
@ApiModel(description = "售后详情")
public class RefundDetailsVo extends MainVo
{

    /**
     * 售后id
     */
    private int     id;
    /**
     * 订单详情id
     */
    @ParamsMapping("order_details_id")
    private Integer orderDetailId;
    /**
     * 订单商品id
     */
    private int     pid;


    public Integer getOrderDetailId()
    {
        return orderDetailId;
    }

    public void setOrderDetailId(Integer orderDetailId)
    {
        this.orderDetailId = orderDetailId;
    }

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public int getPid()
    {
        return pid;
    }

    public void setPid(int pid)
    {
        this.pid = pid;
    }
}
