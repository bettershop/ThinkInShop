package com.laiketui.domain.vo.config;

import com.laiketui.domain.vo.MainVo;
import io.swagger.annotations.ApiModelProperty;

/**
 * 店铺添加修改快递公司子表参数
 */
public class AddExpressSubtableVo extends MainVo
{

    @ApiModelProperty(name = "id", value = "ID")
    private Integer id;

    @ApiModelProperty(name = "mch_id", value = "店铺ID")
    private Integer mch_id;

    @ApiModelProperty(name = "express_id", value = "主表ID")
    private Integer express_id;

    @ApiModelProperty(name = "partnerId", value = "电子面单客户账户或月结账号")
    private String partnerId;

    @ApiModelProperty(name = "partnerKey", value = "电子面单密码")
    private String partnerKey;

    @ApiModelProperty(name = "partnerSecret", value = "电子面单密钥")
    private String partnerSecret;

    @ApiModelProperty(name = "partnerName", value = "电子面单客户账户名称")
    private String partnerName;

    @ApiModelProperty(name = "net", value = "收件网点名称")
    private String net;

    @ApiModelProperty(name = "code", value = "电子面单承载编号")
    private String code;

    @ApiModelProperty(name = "checkMan", value = "电子面单承载快递员名")
    private String checkMan;

    @ApiModelProperty(value = "模板id")
    private String temp_id;

    public String getTemp_id() {
        return temp_id;
    }

    public void setTemp_id(String temp_id) {
        this.temp_id = temp_id;
    }

    public Integer getId()
    {
        return id;
    }

    public void setId(Integer id)
    {
        this.id = id;
    }

    public Integer getMch_id()
    {
        return mch_id;
    }

    public void setMch_id(Integer mch_id)
    {
        this.mch_id = mch_id;
    }

    public Integer getExpress_id()
    {
        return express_id;
    }

    public void setExpress_id(Integer express_id)
    {
        this.express_id = express_id;
    }

    public String getPartnerId()
    {
        return partnerId;
    }

    public void setPartnerId(String partnerId)
    {
        this.partnerId = partnerId;
    }

    public String getPartnerKey()
    {
        return partnerKey;
    }

    public void setPartnerKey(String partnerKey)
    {
        this.partnerKey = partnerKey;
    }

    public String getPartnerSecret()
    {
        return partnerSecret;
    }

    public void setPartnerSecret(String partnerSecret)
    {
        this.partnerSecret = partnerSecret;
    }

    public String getPartnerName()
    {
        return partnerName;
    }

    public void setPartnerName(String partnerName)
    {
        this.partnerName = partnerName;
    }

    public String getNet()
    {
        return net;
    }

    public void setNet(String net)
    {
        this.net = net;
    }

    public String getCode()
    {
        return code;
    }

    public void setCode(String code)
    {
        this.code = code;
    }

    public String getCheckMan()
    {
        return checkMan;
    }

    public void setCheckMan(String checkMan)
    {
        this.checkMan = checkMan;
    }
}
