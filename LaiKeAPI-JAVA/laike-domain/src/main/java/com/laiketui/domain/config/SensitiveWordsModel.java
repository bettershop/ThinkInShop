package com.laiketui.domain.config;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * @author 16640
 * @version 1.0
 * @description: liuao
 * @date 2025/1/7 9:25
 */
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Table(name = "lkt_sensitive_words")
public class SensitiveWordsModel implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ApiModelProperty("敏感词")
    private String word;

    @Column(name = "add_time")
    @ApiModelProperty("新增时间")
    private Date addTime;

    @Column(name = "store_id")
    @ApiModelProperty("商城id")
    private Integer storeId;

    @ApiModelProperty("是否删除 0.未删除 1.已删除")
    private Integer recycle;
}
