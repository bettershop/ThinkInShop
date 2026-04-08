package com.laiketui.domain.backUp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * 数据备份配置表
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "lkt_backup_config")
public class BackUpConfigModel
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
     * 是否开启自动备份,0关闭，1开启
     */
    private Integer is_open;

    /**
     * 执行周期
     */
    private String execute_cycle;

    /**
     * 保存路径
     */
    private String url;

    /**
     * 添加时间
     */
    private Date add_time;

    /**
     * 条件
     */
    private Integer query_data;

    /**
     * 回收站 0.显示 1.系统回收 2用户回收 3店铺回收
     */
    private Integer recycle;
}
