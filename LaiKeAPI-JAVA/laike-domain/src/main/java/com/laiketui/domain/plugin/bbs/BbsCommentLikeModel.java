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
 * @Date: 2025-10-13-16:05
 * @Description:
 */
@Data
@Table(name = "lkt_bbs_comment_like")
@ApiModel(description = "评论点赞表")
public class BbsCommentLikeModel implements Serializable
{
    @ApiModelProperty(value = "id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ApiModelProperty("评论id")
    private Long comment_id;

    @ApiModelProperty("用户id")
    private String user_id;

    @ApiModelProperty("文章id")
    private Long post_id;
}
