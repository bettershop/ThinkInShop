package com.laiketui.domain.file;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * 批量发货记录表
 *
 * @author Trick
 * @date 2021/12/7 17:33
 */
@Table(name = "lkt_file_delivery")
public class FileDeliveryModel implements Serializable
{
    /**
     * id
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;

    /**
     * 文件名称
     */
    private String name;

    /**
     * 标签副标题
     */
    private String title;

    /**
     * 文件上传路径
     */
    private String upload_path;

    /**
     * 文件状态 1=发货成功 0=发货失败
     */
    private Integer status;

    /**
     * 失败原因
     */
    @Column(name = "`text`")
    private String text;

    /**
     * 操作店铺id
     */
    private Integer mch_id;

    /**
     * 发货时间
     */
    private Date add_date;

    private Date    update_date;
    /**
     * 类型： 0批量导入商品  1批量发货  2.导入用户
     */
    private Integer type;
    /**
     * 发货订单数量/导入商品数
     */
    private Integer orderNum;

    public Integer getOrderNum()
    {
        return orderNum;
    }

    public void setOrderNum(Integer orderNum)
    {
        this.orderNum = orderNum;
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
     * 获取文件名称
     *
     * @return name - 文件名称
     */
    public String getName()
    {
        return name;
    }

    /**
     * 设置文件名称
     *
     * @param name 文件名称
     */
    public void setName(String name)
    {
        this.name = name == null ? null : name.trim();
    }

    /**
     * 获取标签副标题
     *
     * @return title - 标签副标题
     */
    public String getTitle()
    {
        return title;
    }

    /**
     * 设置标签副标题
     *
     * @param title 标签副标题
     */
    public void setTitle(String title)
    {
        this.title = title == null ? null : title.trim();
    }

    /**
     * 获取文件上传路径
     *
     * @return upload_path - 文件上传路径
     */
    public String getUpload_path()
    {
        return upload_path;
    }

    /**
     * 设置文件上传路径
     *
     * @param upload_path 文件上传路径
     */
    public void setUpload_path(String upload_path)
    {
        this.upload_path = upload_path == null ? null : upload_path.trim();
    }

    /**
     * 获取文件状态 1=发货成功 0=发货失败
     *
     * @return status - 文件状态 1=发货成功 0=发货失败
     */
    public Integer getStatus()
    {
        return status;
    }

    /**
     * 设置文件状态 1=发货成功 0=发货失败
     *
     * @param status 文件状态 1=发货成功 0=发货失败
     */
    public void setStatus(Integer status)
    {
        this.status = status;
    }

    /**
     * 获取失败原因
     *
     * @return text - 失败原因
     */
    public String getText()
    {
        return text;
    }

    /**
     * 设置失败原因
     *
     * @param text 失败原因
     */
    public void setText(String text)
    {
        this.text = text;
    }

    public Integer getMch_id()
    {
        return mch_id;
    }

    public void setMch_id(Integer mch_id)
    {
        this.mch_id = mch_id;
    }

    /**
     * 获取发货时间
     *
     * @return add_date - 发货时间
     */
    public Date getAdd_date()
    {
        return add_date;
    }

    /**
     * 设置发货时间
     *
     * @param add_date 发货时间
     */
    public void setAdd_date(Date add_date)
    {
        this.add_date = add_date;
    }

    /**
     * @return update_date
     */
    public Date getUpdate_date()
    {
        return update_date;
    }

    /**
     * @param update_date
     */
    public void setUpdate_date(Date update_date)
    {
        this.update_date = update_date;
    }
}