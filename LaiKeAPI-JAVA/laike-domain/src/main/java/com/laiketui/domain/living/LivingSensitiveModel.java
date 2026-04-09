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
@Table(name = "lkt_living_sensitive")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LivingSensitiveModel implements Serializable
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
    private String word;


    /**
     * 添加时间
     */
    private Date add_time;


    /**
     * '回收站 0.显示 1.回收 '
     */
    private Integer recycle;
}
