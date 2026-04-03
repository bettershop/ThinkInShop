package com.laiketui.domain.vo.user;

import org.apache.commons.lang3.StringUtils;

/**
 * 管理员颜色设置VO
 * 用于存储管理员名称和商城样式颜色
 * 主要用于前端展示和样式配置
 *
 * @Author Trick
 * @Date 2021/6/15 15:55
 */
public class AdminColorVo
{
    private String name;        // 管理员名称
    private String color;      // 商城样式颜色（如red、blue）

    // Getter和Setter方法
    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getColor()
    {
        return color;
    }

    public void setColor(String color)
    {
        this.color = color;
    }

    // 验证方法
    public boolean validate()
    {
        return !StringUtils.isEmpty(name) && !StringUtils.isEmpty(color);
    }
}
