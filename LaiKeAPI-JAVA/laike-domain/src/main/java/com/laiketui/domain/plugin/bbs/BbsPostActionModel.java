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
import java.util.Date;

/**
 * @Author: liuao
 * @Date: 2025-09-28-16:17
 * @Description:
 */
@Data
@Table(name = "lkt_bbs_post_action")
@ApiModel(description = "互动表")
public class BbsPostActionModel
{

    @ApiModelProperty(value = "id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ApiModelProperty("帖子id")
    private Long post_id;

    @ApiModelProperty("圈主id")
    private Long forum_id;

    @ApiModelProperty("用户id")
    private String user_id;

    @ApiModelProperty("被关注的user_id")
    private String be_user_id;

    @ApiModelProperty("类型 1.点赞 2.收藏 3.转发 4.关注")
    private Integer action_type;

    @ApiModelProperty("是否删除 0：未删除 1：删除")
    private Integer recycle;

    @ApiModelProperty("添加时间")
    private Date create_time;

    @AllArgsConstructor
    @Getter
    public enum ActionType
    {
        /**
         * 点赞
         */
        LIKE(1),

        /**
         * 收藏
         */
        COLLECT(2),

        /**
         * 转发
         */
        SHARE(3),

        /**
         * 关注
         */
        FOLLOW(4);

        private final Integer type;

    }
}
