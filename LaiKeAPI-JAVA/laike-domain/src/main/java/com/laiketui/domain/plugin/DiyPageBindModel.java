package com.laiketui.domain.plugin;

import lombok.Data;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

/**
 * @Author: liuao
 * @Date: 2025-06-16-14:52
 * @Description:
 */
@Table(name = "lkt_diy_page_bind")
@Data
public class DiyPageBindModel implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * diy_id
     */
    private Integer diy_id;

    /**
     * 页面id
     */
    private Integer diy_page_id;


    /**
     * diy数据使用链接对应的json key，逗号拼接
     */
    private String link_key;

    /**
     * 子组件value
     */
    private String unit;

    /**
     * 绑定时间
     */
    private Date bind_time;
}
