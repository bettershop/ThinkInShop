package com.laiketui.domain.vo.living;

import com.laiketui.core.annotation.ParamsMapping;
import com.laiketui.domain.vo.MainVo;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author zhuqingyu
 * @create 2024/5/29
 * <p>
 * 新增/编辑直播配置入参
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddLivingConfigVo extends MainVo
{
    @ApiModelProperty(name = "isOpen", value = "是否开启插件 0-不开启 1-开启")
    @ParamsMapping("is_open")
    private Integer isOpen;

    @ApiModelProperty(name = "agreeContent", value = "直播协议")
    private String agreeContent;

    @ApiModelProperty(name = "agreeTitle", value = "协议标题")
    private String agreeTitle;

    @ApiModelProperty(name = "pushUrl", value = "推流url")
    private String pushUrl;

    @ApiModelProperty(name = "playUrl", value = "播放url")
    private String playUrl;

    @ApiModelProperty(name = "license_url", value = "url")
    private String license_url;

    @ApiModelProperty(name = "license_key", value = "key")
    private String license_key;

    @ApiModelProperty(name = "mch_is_open", value = "店铺是否开启插件 0-不开启 1-开启")
    @ParamsMapping("mch_is_open")
    private Integer mchIsOpen;
}
