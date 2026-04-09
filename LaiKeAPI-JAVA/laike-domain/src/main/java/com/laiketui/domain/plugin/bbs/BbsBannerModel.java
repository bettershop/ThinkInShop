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
 * @Date: 2025-09-28-14:30
 * @Description:
 */
@Data
@ApiModel(description = "轮播图表")
@Table(name = "lkt_bbs_banner")
public class BbsBannerModel implements Serializable
{
    @ApiModelProperty(value = "id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ApiModelProperty(value = "商城ID")
    private Integer store_id;

    @ApiModelProperty(value = "轮播图")
    private String image;

    @ApiModelProperty(value = "链接")
    private String url;

    @ApiModelProperty(value = "排序")
    private Integer sort_order;

    @ApiModelProperty(value = "是否删除 0.未删除 1.删除")
    private Integer recycle;

    @ApiModelProperty(value = "创建时间")
    private Date create_time;
}
