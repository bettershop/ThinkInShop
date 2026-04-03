package com.laiketui.domain.vo.user;

import com.laiketui.domain.vo.MainVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * 添加用户参数
 *
 * @author Trick
 * @date 2021/1/8 10:54
 */
@ApiModel(description = "添加用户参数")
public class AddUserVo extends MainVo
{

    @ApiModelProperty(value = "头像", name = "headerUrl")
    private String  headerUrl;
    @ApiModelProperty(value = "会员名称", name = "userName")
    private String  userName;
    @ApiModelProperty(value = "会员等级", name = "grade")
    private Integer grade;
    @ApiModelProperty(value = "会员账号", name = "zhanghao")
    private String  zhanghao;
    @ApiModelProperty(value = "会员密码", name = "mima")
    private String  mima;
    @ApiModelProperty(value = "手机号", name = "phone")
    private String  phone;
    @ApiModelProperty(value = "来源", name = "source")
    private Integer source;

    @ApiModelProperty(value = "生日", name = "phone")
    private String  birthday;
    @ApiModelProperty(value = "性别 0:未知 1:男 2:女", name = "source")
    private Integer sex;
    @ApiModelProperty(value = "区号")
    private String cpc;

    @ApiModelProperty(value = "国家代码")
    private Integer country_num;

    @ApiModelProperty(value = "邮箱")
    private String e_mail;

    public String getCpc()
    {
        return cpc;
    }

    public void setCpc(String cpc)
    {
        this.cpc = cpc;
    }

    public Integer getCountry_num()
    {
        return country_num;
    }

    public void setCountry_num(Integer country_num)
    {
        this.country_num = country_num;
    }

    public String getE_mail()
    {
        return e_mail;
    }

    public void setE_mail(String e_mail)
    {
        this.e_mail = e_mail;
    }

    public String getHeaderUrl()
    {
        return headerUrl;
    }

    public void setHeaderUrl(String headerUrl)
    {
        this.headerUrl = headerUrl;
    }

    public String getUserName()
    {
        return userName;
    }

    public void setUserName(String userName)
    {
        this.userName = userName;
    }

    public Integer getGrade()
    {
        return grade;
    }

    public void setGrade(Integer grade)
    {
        this.grade = grade;
    }

    public String getZhanghao()
    {
        return zhanghao;
    }

    public void setZhanghao(String zhanghao)
    {
        this.zhanghao = zhanghao;
    }

    public String getMima()
    {
        return mima;
    }

    public void setMima(String mima)
    {
        this.mima = mima;
    }

    public String getPhone()
    {
        return phone;
    }

    public void setPhone(String phone)
    {
        this.phone = phone;
    }

    public Integer getSource()
    {
        return source;
    }

    public void setSource(Integer source)
    {
        this.source = source;
    }


    public String getBirthday()
    {
        return birthday;
    }

    public void setBirthday(String birthday)
    {
        this.birthday = birthday;
    }

    public Integer getSex()
    {
        return sex;
    }

    public void setSex(Integer sex)
    {
        this.sex = sex;
    }
}
