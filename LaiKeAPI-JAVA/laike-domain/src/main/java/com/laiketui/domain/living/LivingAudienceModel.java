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
 * @create 2024/6/26
 * <p>
 * 直播间观众
 */
@Table(name = "lkt_living_audience")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LivingAudienceModel implements Serializable
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
     * 开始时间
     */
    private Date start_time;

    /**
     * 结束时间
     */
    private Date end_time;

    /**
     * 添加时间
     */
    private Date add_time;

    /**
     * '回收站 0.显示 1.系统回收 2用户回收 3店铺回收'
     */
    private Integer recycle;

    /**
     * 时长
     */
    private Long sc;
}
