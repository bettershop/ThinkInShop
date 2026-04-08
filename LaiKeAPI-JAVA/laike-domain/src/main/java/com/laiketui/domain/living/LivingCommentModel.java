package com.laiketui.domain.living;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

/**
 * @author zhuqingyu
 * @create 2024/5/28
 * 直播间评论
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "lkt_living_comment")
public class LivingCommentModel implements Serializable
{
    /**
     * id
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * 商城id
     */
    private Integer store_id;

    /**
     * 用户id
     */
    private String user_id;

    /**
     * 直播间id
     */
    private String living_id;

    /**
     * 评论
     */
    private String comment;

    /**
     * 上级id
     */
    private Integer p_id;

    /**
     * 添加时间
     */
    private Date add_time;

    /**
     * 评论类型 1:评论，2:关注，3:点赞
     */
    private Integer commentType;

    /**
     * '回收站 0.显示 1.系统回收 2用户回收 3店铺回收'
     */
    private Integer recycle;


}
