package com.laiketui.domain.onlinemessage;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

/**
 * @author Trick
 */
@Table(name = "lkt_online_message")
public class OnlineMessageModel implements Serializable
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private             Integer id;
    /**
     * 用户发送信息
     */
    public static final Integer SEND_TYPE_USER = 0;
    /**
     * 店铺发送信息
     */
    public static final Integer SEND_TYPE_MCH  = 1;

    private Integer store_id;

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

    private String send_id;

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


    private String receive_id;

    private String content;

    private Integer is_mch_send;

    private Date add_date;

    private Integer is_read;
}
