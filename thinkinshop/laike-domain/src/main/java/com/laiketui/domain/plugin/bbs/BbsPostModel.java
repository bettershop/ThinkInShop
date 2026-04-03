package com.laiketui.domain.plugin.bbs;

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
 * @Date: 2025-09-28-14:19
 * @Description: 文章表
 */

@Data
@Table(name = "lkt_bbs_post")
public class BbsPostModel implements Serializable
{
    @ApiModelProperty(value = "id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ApiModelProperty("商城id")
    private Integer store_id;

    @ApiModelProperty("种草官id")
    private Long forum_id;

    @ApiModelProperty("商品id，多个逗号分隔")
    private String pro_ids;

    @ApiModelProperty("用户id")
    private String user_id;

    @ApiModelProperty("云点播视频id")
    private String video_id;

    @ApiModelProperty("分类id")
    private String category_id;

    @ApiModelProperty("标题")
    private String title;

    @ApiModelProperty("内容")
    private String content;

    @ApiModelProperty("图片")
    private String images;

    @ApiModelProperty("封面图")
    private String cover_img;

    @ApiModelProperty("类型 0：图片 1：视频 2：图文")
    private Integer type;

    @ApiModelProperty("视频")
    private String videos;

    @ApiModelProperty("纯文字")
    private String text;

    @ApiModelProperty("评论数量")
    private Integer comment_num;

    @ApiModelProperty("浏览数")
    private Integer browse_num;

    @ApiModelProperty("点赞数")
    private Integer like_num;

    @ApiModelProperty("收藏数")
    private Integer collect_num;

    @ApiModelProperty("转发数")
    private Integer forward_num;

    @ApiModelProperty("关注数")
    private Integer follow_num;

    @ApiModelProperty("拒绝理由")
    private String refuse_text;

    @ApiModelProperty("状态 1待审核 2审核通过 3审核未通过")
    private Integer status;

    @ApiModelProperty("是否为推荐 0.不是推荐 1.推荐")
    private Integer is_home_recommend;

    @ApiModelProperty("是否隐藏 0.不隐藏 1.隐藏")
    private Integer is_hide;

    @ApiModelProperty("是否删除 0：未删除 1：删除")
    private Integer recycle;

    @ApiModelProperty("添加时间")
    private Date create_time;

    @ApiModelProperty("修改时间")
    private Date update_time;


    @AllArgsConstructor
    @Getter
    public enum Type
    {
        /**
         * 图片
         */
        IMG(0),

        /**
         * 视频
         */
        VIDEO(1),

        /**
         * 图文
         */
        IMG_TEXT(2);

        private final Integer type;

    }
}
