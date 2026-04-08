package com.laiketui.domain.product;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * @author 草稿表
 * @version 1.0
 * @description: liuao
 * @date 2025/1/16 13:56
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "lkt_drafts")
public class DraftsModel implements Serializable
{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * 店铺id
     */
    @Column(name = "mch_id")
    private Integer mch_id;

    /**
     * 供应商id
     */
    @Column(name = "supplier_id")
    private Integer supplier_id;

    /**
     * 商城id
     */
    @Column(name = "store_id")
    private Integer store_id;

    /**
     * 草稿内容 json
     */
    @Column(name = "text")
    private String text;

    /**
     * 添加时间
     */
    @Column(name = "add_time")
    private Date add_time;
}
