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
 * @Date: 2025-09-28-15:24
 * @Description:
 */
@Data
@ApiModel(description = "贴吧转让申请表")
@Table(name = "lkt_bbs_forum_transfer")
public class BbsForumTransferModel implements Serializable
{
    @ApiModelProperty(value = "id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ApiModelProperty(value = "吧主ID")
    private Long forum_id;

    @ApiModelProperty(value = "转让人")
    private String from_user_id;

    @ApiModelProperty(value = "接手人")
    private String to_user_id;

    @ApiModelProperty(value = "转让原因")
    private String reason_for_transfer;

    @ApiModelProperty(value = "状态 1.待审核  2.审核通过 3.审核未通过")
    private Integer status;

    @ApiModelProperty(value = "拒绝理由")
    private String refuse_text;

    @ApiModelProperty(value = "是否删除 0.未删除 1.删除")
    private Integer recycle;

    @ApiModelProperty(value = "创建时间")
    private Date create_time;
}
