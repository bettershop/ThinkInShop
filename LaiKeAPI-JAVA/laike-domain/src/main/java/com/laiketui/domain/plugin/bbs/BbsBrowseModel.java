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
 * @Date: 2025-09-28-16:11
 * @Description:
 */
@Data
@Table(name = "lkt_bbs_browse")
@ApiModel(description = "浏览表")
public class BbsBrowseModel implements Serializable
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ApiModelProperty("文章id")
    private Long post_id;

    @ApiModelProperty("用户id")
    private String user_id;

    @ApiModelProperty("是否删除 0：未删除 1：删除")
    private Integer recycle;

    @ApiModelProperty("创建时间")
    private Date create_time;
}
