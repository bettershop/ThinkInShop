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
 * 数据备份记录表
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "lkt_backup_record")
public class BackUpRecordModel
{

    /**
     * 备份中
     */
    public final static int BACKUP_RECORD_STATUS_AFOOT     = 0;
    /**
     * 已完成
     */
    public final static int BACKUP_RECORD_STATUS_COMPLETED = 1;
    /**
     * 备份失败
     */
    public final static int BACKUP_RECORD_STATUS_FAIL      = 2;


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
     * 文件名称
     */
    private String file_name;

    /**
     * 文件大小
     */
    private String file_size;

    /**
     * 备份类型
     */
    private String file_type;

    /**
     * 文件路径
     */
    private String file_url;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 添加时间
     */
    private Date add_time;

    /**
     * 回收站 0.显示 1.系统回收 2用户回收 3店铺回收
     */
    private Integer recycle;
}
