package com.laiketui.domain.plugin.bbs;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @Author: liuao
 * @Date: 2025-10-09-14:37
 * @Description:
 */
@Table(name = "lkt_bbs_post_comment")
@Data
@ApiModel(description = "评论表")
public class BbsPostCommentModel implements Serializable
{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ApiModelProperty("文章id")
    private Long post_id;

    @ApiModelProperty("用户id")
    private String user_id;

    @ApiModelProperty("点赞数")
    private Integer like_num;

    @ApiModelProperty("内容")
    private String content;

    @ApiModelProperty("上级id")
    private Long parent_id;

    @ApiModelProperty("一级评论id")
    private Long top_id;

    @ApiModelProperty("是否删除 0未删除 1删除")
    private Integer recycle;

    @ApiModelProperty("创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private Date create_time;

    @ApiModelProperty("回复数")
    private Integer reply_num;

    @Transient
    private String user_name;

    @Transient
    private String headimgurl;

    @Transient
    private Integer has_sub_comments;

    @Transient
    private Integer is_like;

    @Transient
    private String parent_user_name;

    @Transient
    private String parent_user_id;



    @Transient
    private List<BbsPostCommentModel> replies = new ArrayList<>(); // 回复列表
}
