package com.laiketui.domain.vo.plugin;

import com.laiketui.domain.vo.MainVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * 添加/编辑插件通用信息
 *
 * @author sunH_
 * @date 2022/01/1 14:36
 */
@ApiModel(description = "添加/编辑插件通用信息")
public class PluginsVo extends MainVo
{
    @ApiModelProperty(name = "id", value = "插件通用信息id")
    private Integer id;
    @ApiModelProperty(name = "plugin_name", value = "插件名称")
    private String  plugin_name;
    @ApiModelProperty(name = "plugin_code", value = "插件编码")
    private String  plugin_code;
    @ApiModelProperty(name = "plugin_img", value = "插件图标")
    private String  plugin_img;
    @ApiModelProperty(name = "jump_address", value = "插件跳转地址")
    private String  jump_address;
    @ApiModelProperty(name = "status", value = "安装状态 0失败 1成功")
    private Integer status;
    @ApiModelProperty(name = "flag", value = "卸载状态 0未卸载 1已卸载")
    private Integer flag;

    public Integer getId()
    {
        return id;
    }

    public void setId(Integer id)
    {
        this.id = id;
    }

    public String getPlugin_name()
    {
        return plugin_name;
    }

    public void setPlugin_name(String plugin_name)
    {
        this.plugin_name = plugin_name;
    }

    public String getPlugin_code()
    {
        return plugin_code;
    }

    public void setPlugin_code(String plugin_code)
    {
        this.plugin_code = plugin_code;
    }

    public String getPlugin_img()
    {
        return plugin_img;
    }

    public void setPlugin_img(String plugin_img)
    {
        this.plugin_img = plugin_img;
    }

    public String getJump_address()
    {
        return jump_address;
    }

    public void setJump_address(String jump_address)
    {
        this.jump_address = jump_address;
    }

    public Integer getStatus()
    {
        return status;
    }

    public void setStatus(Integer status)
    {
        this.status = status;
    }

    public Integer getFlag()
    {
        return flag;
    }

    public void setFlag(Integer flag)
    {
        this.flag = flag;
    }
}
