package com.laiketui.domain.living;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * @author zhuqingyu
 * @create 2024/5/28
 * 直播间
 */
@Table(name = "lkt_living_room")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LivingRoomModel implements Serializable
{

    /**
     * 显示
     */
    @Transient
    public final static int RECYCLE_SHOW   = 0;
    /**
     * 系统回收
     */
    @Transient
    public final static int RECYCLE_SYSTEM = 1;
    /**
     * 用户回收
     */
    @Transient
    public final static int RECYCLE_USER   = 2;
    /**
     * 店铺回收
     */
    @Transient
    public final static int RECYCLE_MCH    = 3;

    /**
     * 直播的状态，0：预约中，1：直播中，2：已结束，3：已取消
     */

    /**
     * 预约中
     */
    public final static int     STATUS_LIVING_RESERVATION = 0;
    /**
     * 直播中
     */
    public final static int     STATUS_LIVING_STREAMING   = 1;
    /**
     * 已结束
     */
    public final static int     STATUS_LIVING_END         = 2;
    /**
     * 已取消
     */
    public final static int     STATUS_LIVING_CANCELED    = 3;
    /**
     * id
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private             Integer id;

    /**
     * 商城id
     */
    private Integer store_id;

    /**
     * 直播的标题
     */
    private String living_theme;

    /**
     * 直播简介
     */
    private String living_description;

    /**
     * 直播开始时间
     */
    private Date start_time;

    /**
     * 直播时长 分钟
     */
    private String living_time;

    /**
     * 直播结束时间
     */
    private Date end_time;


    /**
     * 直播的状态，0：预约中，1：直播中，2：已结束，3：已过期，4：已取消
     */
    private Integer living_status;

    /**
     * 直播间封面图地址
     */
    private String living_img;

    /**
     * 添加时间
     */
    private Date add_time;

    /**
     * 主播id
     */
    private String user_id;

    /**
     * 直播间观看人数
     */
    private Integer living_num;

    /**
     * 直播间评论数
     */
    private Integer living_review_num;

    /**
     * 直播间推流地址
     */
    private String push_url;

    /**
     * 播放地址
     */
    private String play_url;

    /**
     * 点赞数
     */
    private Integer like_num;

    /**
     * '回收站 0.显示 1.系统回收 2用户回收 3店铺回收'
     */
    private Integer recycle;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 观看粉丝数
     */
    private Integer living_fans_num;

    /**
     * 新增粉丝数
     */
    private Integer fans_num;
}
