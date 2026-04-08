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
 * @create 2024/6/25
 */
@Table(name = "lkt_living_user_like")
@Data
@AllArgsConstructor
@NoArgsConstructor

public class LivingUserLikeModel implements Serializable
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
     * 点赞数
     */
    private Integer dzs_num;

    /**
     * 直播间id
     */
    private Integer living_id;

    /**
     * 添加时间
     */
    private Date add_time;

    /**
     * '回收站 0.显示 1.系统回收 2用户回收 3店铺回收'
     */
    private Integer recycle;

}
