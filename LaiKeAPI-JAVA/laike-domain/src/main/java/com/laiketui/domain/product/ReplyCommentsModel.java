package com.laiketui.domain.product;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

/**
 * 评论回复表
 *
 * @author Trick
 * @date 2023/3/6 13:57
 */
@Table(name = "lkt_reply_comments")
public class ReplyCommentsModel implements Serializable
{

    /**
     * 评论类型
     */
    public interface Type
    {
        /**
         * 用户评论
         */
        int USER = 0;
        /**
         * 商家评论
         */
        int MCH  = 1;
    }

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
     * 评论ID
     */
    private String cid;

    /**
     * 用户id
     */
    private String uid;

    /**
     * 评价内容
     */
    private String content;

    private Date add_time;

    /**
     * 是否是商家回复 0=用户评论 1=商家评论
     */
    private Integer type;

    /**
     * 回复的对象
     */
    private String to_uid;

    /**
     * 上级id
     */
    private Integer sid;

    public String getTo_uid()
    {
        return to_uid;
    }

    public void setTo_uid(String to_uid)
    {
        this.to_uid = to_uid;
    }

    public Integer getSid()
    {
        return sid;
    }

    public void setSid(Integer sid)
    {
        this.sid = sid;
    }

    public Integer getType()
    {
        return type;
    }

    public void setType(Integer type)
    {
        this.type = type;
    }

    /**
     * 获取id
     *
     * @return id - id
     */
    public Integer getId()
    {
        return id;
    }

    /**
     * 设置id
     *
     * @param id id
     */
    public void setId(Integer id)
    {
        this.id = id;
    }

    /**
     * 获取商城id
     *
     * @return store_id - 商城id
     */
    public Integer getStore_id()
    {
        return store_id;
    }

    /**
     * 设置商城id
     *
     * @param store_id 商城id
     */
    public void setStore_id(Integer store_id)
    {
        this.store_id = store_id;
    }

    /**
     * 获取评论ID
     *
     * @return cid - 评论ID
     */
    public String getCid()
    {
        return cid;
    }

    /**
     * 设置评论ID
     *
     * @param cid 评论ID
     */
    public void setCid(String cid)
    {
        this.cid = cid == null ? null : cid.trim();
    }

    /**
     * 获取用户id
     *
     * @return uid - 用户id
     */
    public String getUid()
    {
        return uid;
    }

    /**
     * 设置用户id
     *
     * @param uid 用户id
     */
    public void setUid(String uid)
    {
        this.uid = uid == null ? null : uid.trim();
    }

    /**
     * 获取评价内容
     *
     * @return content - 评价内容
     */
    public String getContent()
    {
        return content;
    }

    /**
     * 设置评价内容
     *
     * @param content 评价内容
     */
    public void setContent(String content)
    {
        this.content = content == null ? null : content.trim();
    }

    /**
     * @return add_time
     */
    public Date getAdd_time()
    {
        return add_time;
    }

    /**
     * @param add_time
     */
    public void setAdd_time(Date add_time)
    {
        this.add_time = add_time;
    }
}