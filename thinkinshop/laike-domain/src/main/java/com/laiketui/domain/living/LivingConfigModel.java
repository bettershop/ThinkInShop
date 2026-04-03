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
 * 直播配置
 */
@Table(name = "lkt_living_config")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LivingConfigModel implements Serializable
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
     * 直播间推流地址
     */
    private String push_url;

    /**
     * 播放地址
     */
    private String play_url;

    /**
     * 获取是否开启插件 0-不开启 1-开启
     */
    private Integer is_open;

    /**
     * 店铺端入口设置 0-不开启 1-开启
     */
    private Integer mch_is_open;

    /**
     * 协议内容
     */
    private String agree_content;
    /**
     * 协议标题
     */
    private String agree_title;

    /**
     * 添加时间
     */
    private Date add_time;

    /**
     * 腾讯云url
     */
    private String license_url;

    /**
     * 腾讯云key
     */
    private String license_key;

    /**
     * '回收站 0.显示 1.系统回收 2用户回收 3店铺回收'
     */
    private Integer recycle;
}
