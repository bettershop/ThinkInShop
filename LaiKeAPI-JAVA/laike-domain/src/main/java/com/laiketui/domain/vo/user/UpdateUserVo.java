package com.laiketui.domain.vo.user;

import com.laiketui.core.annotation.ParamsMapping;
import com.laiketui.domain.vo.MainVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.math.BigDecimal;

/**
 * 修改用户资料参数
 *
 * @author Trick
 * @date 2021/1/7 16:09
 */
@ApiModel(description = "修改用户资料参数")
public class UpdateUserVo extends MainVo
{

    @ApiModelProperty(value = "userId", name = "用户id")
    private String     userId;
    @ApiModelProperty(value = "uname", name = "用户名称")
    private String     uname;
    @ApiModelProperty(value = "grade", name = "用户等级")
    private Integer    grade;
    @ApiModelProperty(value = "grade", name = "开通类型 1包月2包年3包季")
    private Integer    gradeType;
    @ApiModelProperty(value = "phone", name = "手机号")
    private String     phone;
    @ApiModelProperty(value = "pwd", name = "密码")
    private String     pwd;
    @ApiModelProperty(value = "paypwd", name = "支付密码")
    private String     paypwd;
    @ApiModelProperty(value = "money", name = "账户余额")
    private BigDecimal money;
    @ApiModelProperty(value = "jifen", name = "积分余额")
    private Integer    jifen;
    @ApiModelProperty(value = "birthday", name = "生日")
    private String     birthday;
    @ApiModelProperty(value = "sex", name = "性别1=男2=女")
    private Integer    sex;
    @ApiModelProperty(value = "头像", name = "headerUrl")
    private String     headerUrl;
    @ApiModelProperty(value = "cpc", name = "区号")
    private String cpc;
    @ApiModelProperty(value = "邮箱")
    @ParamsMapping(value = "e_mail")
    private String email;

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    public String getCpc()
    {
        return cpc;
    }

    public void setCpc(String cpc)
    {
        this.cpc = cpc;
    }

    public String getHeaderUrl()
    {
        return headerUrl;
    }

    public void setHeaderUrl(String headerUrl)
    {
        this.headerUrl = headerUrl;
    }

    public Integer getSex()
    {
        return sex;
    }

    public void setSex(Integer sex)
    {
        this.sex = sex;
    }

    public Integer getGradeType()
    {
        return gradeType;
    }

    public void setGradeType(Integer gradeType)
    {
        this.gradeType = gradeType;
    }

    public String getUserId()
    {
        return userId;
    }

    public void setUserId(String userId)
    {
        this.userId = userId;
    }

    public String getUname()
    {
        return uname;
    }

    public void setUname(String uname)
    {
        this.uname = uname;
    }

    public Integer getGrade()
    {
        return grade;
    }

    public void setGrade(Integer grade)
    {
        this.grade = grade;
    }

    public String getPhone()
    {
        return phone;
    }

    public void setPhone(String phone)
    {
        this.phone = phone;
    }

    public String getPwd()
    {
        return pwd;
    }

    public void setPwd(String pwd)
    {
        this.pwd = pwd;
    }

    public String getPaypwd()
    {
        return paypwd;
    }

    public void setPaypwd(String paypwd)
    {
        this.paypwd = paypwd;
    }

    public BigDecimal getMoney()
    {
        return money;
    }

    public void setMoney(BigDecimal money)
    {
        this.money = money;
    }

    public Integer getJifen()
    {
        return jifen;
    }

    public void setJifen(Integer jifen)
    {
        this.jifen = jifen;
    }

    public String getBirthday()
    {
        return birthday;
    }

    public void setBirthday(String birthday)
    {
        this.birthday = birthday;
    }
}
