package com.laiketui.domain.vo.admin.menu;

import com.laiketui.domain.vo.MainVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * 添加/编辑菜单
 *
 * @author Trick
 * @date 2021/1/29 11:38
 */
@ApiModel(description = "添加/编辑菜单")
public class AddMenuMainVo extends MainVo
{
    @ApiModelProperty(value = "菜单id", name = "id")
    private String id;

    @ApiModelProperty(value = "菜单名称", name = "menuName")
    private String  menuName;
    @ApiModelProperty(value = "默认图标", name = "defaultLogo")
    private String  defaultLogo;
    @ApiModelProperty(value = "选中图标", name = "checkedLogo")
    private String  checkedLogo;
    @ApiModelProperty(value = "菜单等级", name = "level")
    private Integer level;
    @ApiModelProperty(value = "菜单分类 ", name = "type", hidden = true)
    private Integer type;
    @ApiModelProperty(value = "上级菜单", name = "fatherMenuId")
    private String  fatherMenuId = "0";
    @ApiModelProperty(value = "文件夹path 页面所在路径文件夹 [vue-动态路由里的 path]", name = "path")
    private String  path;
    @ApiModelProperty(value = "菜单地址 页面路径 [vue-动态路由里的 component]", name = "menuUrl")
    private String  menuUrl;
    @ApiModelProperty(value = "是否禁用 1=是 0=否", name = "isDisplay")
    private Integer isDisplay    = 0;
    @ApiModelProperty(value = "菜单说明", name = "text")
    private String  text;

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getMenuName()
    {
        return menuName;
    }

    public void setMenuName(String menuName)
    {
        this.menuName = menuName;
    }

    public String getDefaultLogo()
    {
        return defaultLogo;
    }

    public void setDefaultLogo(String defaultLogo)
    {
        this.defaultLogo = defaultLogo;
    }

    public String getCheckedLogo()
    {
        return checkedLogo;
    }

    public void setCheckedLogo(String checkedLogo)
    {
        this.checkedLogo = checkedLogo;
    }

    public Integer getLevel()
    {
        return level;
    }

    public void setLevel(Integer level)
    {
        this.level = level;
    }

    public Integer getType()
    {
        return type;
    }

    public void setType(Integer type)
    {
        this.type = type;
    }

    public String getFatherMenuId()
    {
        return fatherMenuId;
    }

    public void setFatherMenuId(String fatherMenuId)
    {
        this.fatherMenuId = fatherMenuId;
    }

    public String getPath()
    {
        return path;
    }

    public void setPath(String path)
    {
        this.path = path;
    }

    public String getMenuUrl()
    {
        return menuUrl;
    }

    public void setMenuUrl(String menuUrl)
    {
        this.menuUrl = menuUrl;
    }

    public Integer getIsDisplay()
    {
        return isDisplay;
    }

    public void setIsDisplay(Integer isDisplay)
    {
        this.isDisplay = isDisplay;
    }

    public String getText()
    {
        return text;
    }

    public void setText(String text)
    {
        this.text = text;
    }
}
