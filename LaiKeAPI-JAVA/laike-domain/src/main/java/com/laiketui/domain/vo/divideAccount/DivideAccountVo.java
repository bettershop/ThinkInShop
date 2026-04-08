package com.laiketui.domain.vo.divideAccount;

import com.laiketui.domain.vo.MainVo;
import io.swagger.annotations.ApiModelProperty;

/**
 * @Author: sunH_
 * @Date: Create in 16:33 2023/8/8
 */
public class DivideAccountVo extends MainVo
{

    @ApiModelProperty(name = "mchId", value = "店铺id")
    private Integer mchId;
    @ApiModelProperty(name = "subMchId", value = "子商户号")
    private String  subMchId;
    @ApiModelProperty(name = "subAppId", value = "子商户应用号")
    private String  subAppId;
    @ApiModelProperty(name = "divideAccountInfo", value = "分账信息")
    private String  divideAccountInfo;

    public Integer getMchId()
    {
        return mchId;
    }

    public void setMchId(Integer mchId)
    {
        this.mchId = mchId;
    }

    public String getSubMchId()
    {
        return subMchId;
    }

    public void setSubMchId(String subMchId)
    {
        this.subMchId = subMchId;
    }

    public String getSubAppId()
    {
        return subAppId;
    }

    public void setSubAppId(String subAppId)
    {
        this.subAppId = subAppId;
    }

    public String getDivideAccountInfo()
    {
        return divideAccountInfo;
    }

    public void setDivideAccountInfo(String divideAccountInfo)
    {
        this.divideAccountInfo = divideAccountInfo;
    }
}
