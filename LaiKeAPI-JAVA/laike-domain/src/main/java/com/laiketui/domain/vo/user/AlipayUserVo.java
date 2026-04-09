package com.laiketui.domain.vo.user;

import com.laiketui.domain.vo.MainVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * @Author: sunH_
 * @Date: Create in 15:52 2022/7/19
 */
@ApiModel(description = "支付宝用户")
public class AlipayUserVo extends MainVo
{

    @ApiModelProperty(value = "用户昵称", name = "nickName")
    private String nickName;

    @ApiModelProperty(value = "用户头像", name = "headImgUrl")
    private String headImgUrl;

    @ApiModelProperty(value = "性别", name = "sex")
    private Integer sex;

    @ApiModelProperty(value = "支付宝id", name = "zfb_id")
    private String zfb_id;

    @ApiModelProperty(value = "推荐人id", name = "pid")
    private String pid;

    @ApiModelProperty(value = "来源 1.小程序 2.app 3.支付宝小程序 4.头条小程序 5.百度小程序 6.pc端 7.H5", name = "source")
    private Integer source;

    public String getNickName()
    {
        return nickName;
    }

    public void setNickName(String nickName)
    {
        this.nickName = nickName;
    }

    public String getHeadImgUrl()
    {
        return headImgUrl;
    }

    public void setHeadImgUrl(String headImgUrl)
    {
        this.headImgUrl = headImgUrl;
    }

    public Integer getSex()
    {
        return sex;
    }

    public void setSex(Integer sex)
    {
        this.sex = sex;
    }

    public String getZfb_id()
    {
        return zfb_id;
    }

    public void setZfb_id(String zfb_id)
    {
        this.zfb_id = zfb_id;
    }

    public String getPid()
    {
        return pid;
    }

    public void setPid(String pid)
    {
        this.pid = pid;
    }

    public Integer getSource()
    {
        return source;
    }

    public void setSource(Integer source)
    {
        this.source = source;
    }
}
