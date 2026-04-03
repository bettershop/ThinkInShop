package com.laiketui.domain.plugin.bbs;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

/**
 * @Author: liuao
 * @Date: 2025-09-28-14:35
 * @Description:
 */
@Data
@ApiModel(description = "种草官表")
@Table(name = "lkt_bbs_forum")
public class BbsForumModel implements Serializable
{
    @ApiModelProperty(value = "id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ApiModelProperty(value = "商城ID")
    private Integer store_id;

    @ApiModelProperty(value = "种草官名称")
    private String name;

    @ApiModelProperty(value = "简介")
    private String description;

    @ApiModelProperty(value = "封面")
    private String cover_img;

    @ApiModelProperty(value = "头像")
    private String head_img;

    @ApiModelProperty(value = "分类")
    private String category_id;

    @ApiModelProperty(value = "用户ID")
    private String user_id;

    @ApiModelProperty(value = "是否需要审核 0.不需要 1.需要")
    private Integer need_platform_review;

    @ApiModelProperty(value = "状态 1.待审核  2.审核通过 3.审核未通过")
    private Integer status;

    @ApiModelProperty(value = "拒绝理由")
    private String refuse_text;

    @ApiModelProperty(value = "点赞数")
    private Integer like_num;

    @ApiModelProperty(value = "收藏数")
    private Integer collect_num;

    @ApiModelProperty(value = "发布文章数")
    private Integer post_num;

    @ApiModelProperty(value = "转发数")
    private Integer forward_num;

    @ApiModelProperty(value = "关注数")
    private Integer follow_num;

    @ApiModelProperty(value = "是否删除 0.未删除 1.删除")
    private Integer recycle;

    @ApiModelProperty(value = "创建时间")
    private Date create_time;

    @ApiModelProperty(value = "修改时间")
    private Date update_time;


    @AllArgsConstructor
    @Getter
    public enum status
    {
        //审核未通过
        WITH(1),

        //审核通过
        PASS(2),

        //审核未通过
        NOT_PASS(3);

        private final Integer value;
    }

}
