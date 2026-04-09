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
 * @Date: 2025-10-27-10:50
 * @Description:
 */
@Table(name = "lkt_bbs_template_config")
@ApiModel(description = "长文章图文模板")
@Data
public class BbsTemplateConfigModel implements Serializable
{

    @ApiModelProperty(value = "id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ApiModelProperty("商城id")
    private Integer store_id;

    @ApiModelProperty("模板名称")
    private String name;

    @ApiModelProperty("模板图标")
    private String icon;

    @ApiModelProperty("模板主图")
    private String img;

    @ApiModelProperty("是否启用 0：未启用 1：启用")
    private Integer status;

    @ApiModelProperty("是否删除 0：未删除 1：删除")
    private Integer recycle;

    @ApiModelProperty("添加时间")
    private Date create_time;
}
