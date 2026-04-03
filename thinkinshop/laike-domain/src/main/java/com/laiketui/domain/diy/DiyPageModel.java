package com.laiketui.domain.diy;

import lombok.Data;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

@Table(name = "lkt_diy_page")
@Data
public class DiyPageModel implements Serializable
{
    /**
     * 页面id
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * 商城id
     */
    private Integer store_id;

    /**
     * 页面名称
     */
    private String page_name;

    /**
     * 页面类型 1：系统页面 2：自定义页面
     */
    private Integer type;

    /**
     * 页面对应的key
     */
    private String page_key;

    /**
     * 链接
     */
    private String link;

    /**
     * 状态 0:不启用 1:启用
     */
    private Integer status;

    /**
     * 是否回收 0：显示 1：回收
     */
    private Integer recycle;

    /**
     * 添加时间
     */
    private Date create_time;

    /**
     * 更新时间
     */
    private Date update_time;

    /**
     * 创建人
     */
    private String create_by;

    /**
     * 页面内容
     */
    private String page_context;

    /**
     * 图片
     */
    private String image;
}