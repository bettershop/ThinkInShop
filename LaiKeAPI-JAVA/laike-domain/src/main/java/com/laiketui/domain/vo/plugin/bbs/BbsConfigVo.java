package com.laiketui.domain.vo.plugin.bbs;

import com.laiketui.domain.vo.MainVo;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

/**
 * @Author: liuao
 * @Date: 2025-09-29-10:36
 * @Description:
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class BbsConfigVo extends MainVo
{
    @ApiModelProperty(value = "开关 0.关闭 1.开启")
    private Integer is_status;

    @ApiModelProperty(value = "密钥")
    private String secret_key;

    @ApiModelProperty(value = "密钥id")
    private String secret_id;

    @ApiModelProperty(value = "appid")
    private String appid;

    @ApiModelProperty(value = "防盗链 key")
    private String security_key;

    @ApiModelProperty(value = "区域储存位置")
    private String region;

    @ApiModelProperty(value = "cdn域名播放地址")
    private String cdn_url;

    @ApiModelProperty(value = "过期时间 单位：小时")
    private Integer expire_time;

    @ApiModelProperty(value = "是否开启cdn 0：关闭 1：开启")
    private Integer is_cdn;

    @ApiModelProperty(value = "须知")
    private String notice;

    @ApiModelProperty(value = "视频转码模板id")
    private Integer definition_template_id;

    @ApiModelProperty(value = "视频采样截图模板id")
    private Integer sample_template_id;

    @ApiModelProperty(value = "回调地址")
    private String notify_url;

    @ApiModelProperty(value = "播放器许可证url")
    private String license_url;

}
