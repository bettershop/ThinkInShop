package com.laiketui.domain.plugin.bbs;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * @Author: liuao
 * @Date: 2025-09-28-16:20
 * @Description:
 */
@Data
@ApiModel(description = "标签文章关联表")
@Table(name = "lkt_bbs_label_post")
public class BbsLabelPostModel implements Serializable
{
    @ApiModelProperty(value = "id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ApiModelProperty("商城id")
    private Integer store_id;

    @ApiModelProperty("标签id")
    private Long label_id;

    @ApiModelProperty("文章id")
    private Long post_id;


}
