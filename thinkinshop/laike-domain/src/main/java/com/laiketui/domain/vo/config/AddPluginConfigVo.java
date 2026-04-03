package com.laiketui.domain.vo.config;

import com.laiketui.domain.vo.MainVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * 插件配置参数
 *
 * @author Trick
 * @date 2023/3/17 15:04
 */
@ApiModel(description = "插件配置参数")
public class AddPluginConfigVo extends MainVo
{

    @ApiModelProperty(name = "id", value = "插件id")
    private Integer id;
    @ApiModelProperty(name = "content", value = "插件介绍")
    private String  content;
    @ApiModelProperty(name = "pluginSwitch", value = "插件开关 1=开 0=关")
    private Integer pluginSwitch;

    public Integer getId()
    {
        return id;
    }

    public void setId(Integer id)
    {
        this.id = id;
    }

    public String getContent()
    {
        return content;
    }

    public void setContent(String content)
    {
        this.content = content;
    }

    public Integer getPluginSwitch()
    {
        return pluginSwitch;
    }

    public void setPluginSwitch(Integer pluginSwitch)
    {
        this.pluginSwitch = pluginSwitch;
    }
}
