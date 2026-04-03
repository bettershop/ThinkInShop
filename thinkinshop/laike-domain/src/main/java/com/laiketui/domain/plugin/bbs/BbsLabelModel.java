package com.laiketui.domain.plugin.bbs;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

/**
 * @Author: liuao
 * @Date: 2025-09-28-14:28
 * @Description:
 */
@Data
@Table(name = "lkt_bbs_label")
@ApiModel(description = "标签表")
public class BbsLabelModel implements Serializable
{

    @ApiModelProperty(value = "id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ApiModelProperty(value = "商城id")
    private Integer store_id;

    @ApiModelProperty(value = "标签名")
    private String name;

    @ApiModelProperty(value = "用户id")
    private String user_id;

    @ApiModelProperty(value = "是否删除 0.未删除 1.删除")
    private Integer recycle;

    @ApiModelProperty(value = "创建时间")
    private Date create_time;

    @ApiModelProperty(value = "修改时间")
    private Date update_time;
}
