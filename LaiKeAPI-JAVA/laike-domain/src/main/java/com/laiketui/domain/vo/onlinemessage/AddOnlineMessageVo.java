package com.laiketui.domain.vo.onlinemessage;

import com.laiketui.domain.vo.MainVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Date;
import java.util.List;

/**
 * @author Trick
 */
@ApiModel(description = "发送在线消息")
public class AddOnlineMessageVo extends MainVo
{

    @ApiModelProperty("发送id")
    private String send_id;


    public String getSend_id()
    {
        return send_id;
    }

    public void setSend_id(String send_id)
    {
        this.send_id = send_id;
    }

    public String getReceive_id()
    {
        return receive_id;
    }

    public void setReceive_id(String receive_id)
    {
        this.receive_id = receive_id;
    }

    public String getContent()
    {
        return content;
    }

    public void setContent(String content)
    {
        this.content = content;
    }

    public Integer getIs_mch_send()
    {
        return is_mch_send;
    }

    public void setIs_mch_send(Integer is_mch_send)
    {
        this.is_mch_send = is_mch_send;
    }

    public Date getAdd_date()
    {
        return add_date;
    }

    public void setAdd_date(Date add_date)
    {
        this.add_date = add_date;
    }

    public Integer getIs_read()
    {
        return is_read;
    }

    public void setIs_read(Integer is_read)
    {
        this.is_read = is_read;
    }

    /**
     * 接收信息id
     */
    private String  receive_id;
    /**
     * 类容
     */
    private String  content;
    /**
     * 是否店铺发送 0否 1 是 店铺发送 id 为店铺id
     */
    private Integer is_mch_send;

    public List<String> getImg_list()
    {
        return img_list;
    }

    public void setImg_list(List<String> img_list)
    {
        this.img_list = img_list;
    }

    private Date    add_date;
    /**
     * 是否以读
     */
    private Integer is_read;

    private List<String> img_list;

    @ApiModelProperty("订单id")
    private Integer orderId;

    @ApiModelProperty("商品id")
    private Integer pId;

    public Integer getpId()
    {
        return pId;
    }

    public void setpId(Integer pId)
    {
        this.pId = pId;
    }

    public Integer getOrderId()
    {
        return orderId;
    }

    public void setOrderId(Integer orderId)
    {
        this.orderId = orderId;
    }
}
