package com.laiketui.domain.user;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * 第三方用户id映射
 *
 * @author Trick
 * @date 2023/1/13 18:22
 */
@Table(name = "lkt_third_mapping")
public class ThirdMappingModel implements Serializable
{

    public interface Type
    {
        int weiXin = 0;
        int zfb    = 1;
    }

    /**
     * id
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;

    /**
     * 商城id
     */
    private Integer store_id;

    /**
     * 第三方用户主键id
     */
    private String union_id;

    /**
     * 第三方用户应用id
     */
    private String open_id;

    /**
     * 系统用户id
     */
    private String user_id;

    /**
     * 0=微信 1=支付宝
     */
    private Integer type;

    /**
     * 获取id
     *
     * @return id - id
     */
    public String getId()
    {
        return id;
    }

    /**
     * 设置id
     *
     * @param id id
     */
    public void setId(String id)
    {
        this.id = id == null ? null : id.trim();
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
     * 获取第三方用户主键id
     *
     * @return union_id - 第三方用户主键id
     */
    public String getUnion_id()
    {
        return union_id;
    }

    /**
     * 设置第三方用户主键id
     *
     * @param union_id 第三方用户主键id
     */
    public void setUnion_id(String union_id)
    {
        this.union_id = union_id == null ? null : union_id.trim();
    }

    /**
     * 获取第三方用户应用id
     *
     * @return open_id - 第三方用户应用id
     */
    public String getOpen_id()
    {
        return open_id;
    }

    /**
     * 设置第三方用户应用id
     *
     * @param open_id 第三方用户应用id
     */
    public void setOpen_id(String open_id)
    {
        this.open_id = open_id == null ? null : open_id.trim();
    }

    /**
     * 获取系统用户id
     *
     * @return user_id - 系统用户id
     */
    public String getUser_id()
    {
        return user_id;
    }

    /**
     * 设置系统用户id
     *
     * @param user_id 系统用户id
     */
    public void setUser_id(String user_id)
    {
        this.user_id = user_id == null ? null : user_id.trim();
    }

    /**
     * 获取0=微信 1=支付宝
     *
     * @return type - 0=微信 1=支付宝
     */
    public Integer getType()
    {
        return type;
    }

    /**
     * 设置0=微信 1=支付宝
     *
     * @param type 0=微信 1=支付宝
     */
    public void setType(Integer type)
    {
        this.type = type;
    }
}