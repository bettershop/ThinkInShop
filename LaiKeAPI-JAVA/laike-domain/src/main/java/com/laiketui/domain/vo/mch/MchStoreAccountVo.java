package com.laiketui.domain.vo.mch;

import com.laiketui.domain.vo.MainVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * @Author: sunH_
 * @Date: Create in 18:19 2023/2/21
 */
@ApiModel(description = "添加/编辑门店账户")
public class MchStoreAccountVo extends MainVo
{

    @ApiModelProperty(value = "id", name = "id")
    private Integer id;
    @ApiModelProperty(value = "门店id", name = "mchStoreId")
    private Integer mchStoreId;
    @ApiModelProperty(value = "账号", name = "accountNumber")
    private String  accountNumber;
    @ApiModelProperty(value = "密码", name = "passWord")
    private String  passWord;

    public Integer getId()
    {
        return id;
    }

    public void setId(Integer id)
    {
        this.id = id;
    }

    public Integer getMchStoreId()
    {
        return mchStoreId;
    }

    public void setMchStoreId(Integer mchStoreId)
    {
        this.mchStoreId = mchStoreId;
    }

    public String getAccountNumber()
    {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber)
    {
        this.accountNumber = accountNumber;
    }

    public String getPassWord()
    {
        return passWord;
    }

    public void setPassWord(String passWord)
    {
        this.passWord = passWord;
    }
}
