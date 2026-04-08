package com.laiketui.domain.vo.config;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * pc商城页面显示配置参数
 *
 * @author gp
 * @date 2023-08-25
 */
@ApiModel(description = "插件配置参数")
public class AddPcConfigVo
{
    @ApiModelProperty(name = "mallIcon", value = "浏览器标签图标")
    private String mallIcon;
    @ApiModelProperty(name = "mallName", value = "浏览器标签名称")
    private String mallName;
    @ApiModelProperty(name = "mallLogo", value = "登录页配置logo")
    private String mallLogo;
    @ApiModelProperty(name = "shortcutMenu2", value = "登录页配置 快捷菜单")
    private String shortcutMenu2;
    @ApiModelProperty(name = "copyright", value = "登录页配置 版权信息")
    private String copyright;
    @ApiModelProperty(name = "archival", value = "登录页配置 备案信息")
    private String archival;
    @ApiModelProperty(name = "authority", value = "登录页配置 官方网站")
    private String authority;
    @ApiModelProperty(name = "list", value = "登录页配置 底部连接")
    private String list;
    @ApiModelProperty(name = "welcomeTerm", value = "首页配置 欢迎语")
    private String welcomeTerm;
    @ApiModelProperty(name = "shortcutMenu3", value = "首页配置 快捷菜单")
    private String shortcutMenu3;
    @ApiModelProperty(name = "APPUrl", value = "浮窗设置 下载APP二维码")
    private String APPUrl;
    @ApiModelProperty(name = "APPExplain", value = "浮窗设置 APP说明")
    private String APPExplain;
    @ApiModelProperty(name = "H5Url", value = "商品分享设置 分享h5二维码")
    private String H5Url;
    @ApiModelProperty(name = "textExplain", value = "商品分享设置 文字说明")
    private String textExplain;

    public String getMallIcon()
    {
        return mallIcon;
    }

    public void setMallIcon(String mallIcon)
    {
        this.mallIcon = mallIcon;
    }

    public String getMallName()
    {
        return mallName;
    }

    public void setMallName(String mallName)
    {
        this.mallName = mallName;
    }

    public String getMallLogo()
    {
        return mallLogo;
    }

    public void setMallLogo(String mallLogo)
    {
        this.mallLogo = mallLogo;
    }

    public String getShortcutMenu2()
    {
        return shortcutMenu2;
    }

    public void setShortcutMenu2(String shortcutMenu2)
    {
        this.shortcutMenu2 = shortcutMenu2;
    }

    public String getCopyright()
    {
        return copyright;
    }

    public void setCopyright(String copyright)
    {
        this.copyright = copyright;
    }

    public String getArchival()
    {
        return archival;
    }

    public void setArchival(String archival)
    {
        this.archival = archival;
    }

    public String getAuthority()
    {
        return authority;
    }

    public void setAuthority(String authority)
    {
        this.authority = authority;
    }

    public String getList()
    {
        return list;
    }

    public void setList(String list)
    {
        this.list = list;
    }

    public String getWelcomeTerm()
    {
        return welcomeTerm;
    }

    public void setWelcomeTerm(String welcomeTerm)
    {
        this.welcomeTerm = welcomeTerm;
    }

    public String getShortcutMenu3()
    {
        return shortcutMenu3;
    }

    public void setShortcutMenu3(String shortcutMenu3)
    {
        this.shortcutMenu3 = shortcutMenu3;
    }

    public String getAPPUrl()
    {
        return APPUrl;
    }

    public void setAPPUrl(String APPUrl)
    {
        this.APPUrl = APPUrl;
    }

    public String getAPPExplain()
    {
        return APPExplain;
    }

    public void setAPPExplain(String APPExplain)
    {
        this.APPExplain = APPExplain;
    }

    public String getH5Url()
    {
        return H5Url;
    }

    public void setH5Url(String h5Url)
    {
        H5Url = h5Url;
    }

    public String getTextExplain()
    {
        return textExplain;
    }

    public void setTextExplain(String textExplain)
    {
        this.textExplain = textExplain;
    }
}
