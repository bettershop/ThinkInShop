package com.laiketui.domain.plugin.bbs;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * @Author: liuao
 * @Date: 2025-10-10-10:52
 * @Description:
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "lkt_bbs_video")
public class BbsVideoModel implements Serializable
{

    @ApiModelProperty("id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ApiModelProperty("商城id")
    private Integer store_id;

    @ApiModelProperty("视频id")
    private String file_id;

    @ApiModelProperty("播放地址")
    private String url;

    @ApiModelProperty("封面图")
    private String cover_img;

    @ApiModelProperty("状态 0：准备上传 1：上传完成 2：处理完成")
    private Integer status;

    @ApiModelProperty("响应数据")
    private String response_msg;
}
