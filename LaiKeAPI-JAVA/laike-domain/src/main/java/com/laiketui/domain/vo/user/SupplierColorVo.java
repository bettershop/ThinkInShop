package com.laiketui.domain.vo.user;

import org.apache.commons.lang3.StringUtils;

public class SupplierColorVo
{
    private Integer id;       // 供应商ID
    private String  color;    // 商城样式颜色（如red、blue）

    // Getter和Setter方法
    public Integer getId()
    {
        return id;
    }

    public void setId(Integer id)
    {
        this.id = id;
    }

    public String getColor()
    {
        return color;
    }

    public void setColor(String color)
    {
        this.color = color;
    }

    // 验证参数合法性
    public boolean validate()
    {
        return id != null && id > 0 && !StringUtils.isEmpty(color);
    }
}
