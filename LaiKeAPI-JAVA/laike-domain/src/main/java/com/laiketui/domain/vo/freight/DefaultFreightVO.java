package com.laiketui.domain.vo.freight;

import io.swagger.annotations.ApiModelProperty;

/**
 * @Author: sunH_
 * @Date: Create in 16:58 2022/12/1
 */
public class DefaultFreightVO
{

    @ApiModelProperty(name = "num1", value = "件数/克数")
    private String num1;

    @ApiModelProperty(name = "num2", value = "默认费用")
    private String num2;

    @ApiModelProperty(name = "num3", value = "续件续重")
    private String num3;

    @ApiModelProperty(name = "num4", value = "续费")
    private String num4;

    public String getNum1()
    {
        return num1;
    }

    public void setNum1(String num1)
    {
        this.num1 = num1;
    }

    public String getNum2()
    {
        return num2;
    }

    public void setNum2(String num2)
    {
        this.num2 = num2;
    }

    public String getNum3()
    {
        return num3;
    }

    public void setNum3(String num3)
    {
        this.num3 = num3;
    }

    public String getNum4()
    {
        return num4;
    }

    public void setNum4(String num4)
    {
        this.num4 = num4;
    }
}
