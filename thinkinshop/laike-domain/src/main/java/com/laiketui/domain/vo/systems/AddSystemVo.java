package com.laiketui.domain.vo.systems;

import com.laiketui.core.annotation.ParamsMapping;
import com.laiketui.domain.vo.MainVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * 添加/修改系统基础配置
 *
 * @author Trick
 * @date 2021/1/19 9:16
 */
@ApiModel(description = "添加/修改系统基础配置参数")
public class AddSystemVo extends MainVo
{

    @ApiModelProperty(value = "isRegister", name = "名称 1.注册 2.免注册")
    private Integer isRegister;
    @ApiModelProperty(value = "logoUrl", name = "公司logo")
    private String  logoUrl;
    @ApiModelProperty(value = "wxHeader", name = "微信默认头像")
    private String  wxHeader;
    @ApiModelProperty(value = "pageDomain", name = "h5页面地址")
    private String  pageDomain;
    @ApiModelProperty(value = "messageSaveDay", name = "前端消息保留天数,为空则不删除")
    private Integer messageSaveDay;
    @ApiModelProperty(value = "appLoginValid", name = "移动端登录有效期,小时")
    private Integer appLoginValid;
    @ApiModelProperty(value = "serverClient", name = "客服脚本")
    private String  serverClient;
    @ApiModelProperty(value = "tencentKey", name = "客服")
    private String  tencentKey;
    @ApiModelProperty(value = "unipush", name = "是否开启unipush推送 0.不推送 1.推送")
    private Integer unipush;
    @ApiModelProperty(value = "pushAppkey", name = "推送Appkey")
    private String  pushAppkey;
    @ApiModelProperty(value = "pushAppid", name = "推送Appid")
    private String  pushAppid;
    @ApiModelProperty(value = "pushMasterEcret", name = "推送秘钥")
    private String  pushMasterEcret;
    @ApiModelProperty(value = "isExpress", name = "是否开启快递100 0.不开启 1.开启")
    private Integer isExpress;
    @ApiModelProperty(value = "expressAddress", name = "查询接口地址")
    private String  expressAddress;
    @ApiModelProperty(value = "express_secret", name = "secret在企业管理后台获取")
    private String  express_secret;
    @ApiModelProperty(value = "express_tempId", name = "主单模板")
    private String  express_tempId;
    @ApiModelProperty(value = "expressNumber", name = "用户编号")
    private String  expressNumber;
    @ApiModelProperty(value = "expressKey", name = "接口调用key")
    @ParamsMapping({"authorization", "expressKey"})
    private String  expressKey;
    @ApiModelProperty(value = "isKicking", name = "是否登录踢人  0.不开启 1.开启")
    private Integer isKicking;
    @ApiModelProperty(value = "hideYourWallet", name = "是否隐藏钱包 0.不隐藏 1.隐藏")
    private Integer hideYourWallet;
    @ApiModelProperty(value = "watermarkName", name = "水印名称")
    private String  watermarkName;
    @ApiModelProperty(value = "watermarkUrl", name = "水印网址")
    private String  watermarkUrl;
    @ApiModelProperty(value = "pcMchPath", name = "商城pc店铺默认地址")
    private String  pcMchPath;
    @ApiModelProperty(value = "pcMchPath", name = "搜索配置是否开启")
    private Integer isOpen;
    @ApiModelProperty(value = "pcMchPath", name = "搜索配置关键词上限")
    private Integer limitNum;
    @ApiModelProperty(value = "pcMchPath", name = "搜索配置关键词,多个用','分隔")
    private String  keyword;
    //订单打印配置
    @ApiModelProperty(value = "printName", name = "打印名称")
    private String  printName;

    @ApiModelProperty(value = "printUrl", name = "打印url")
    private String printUrl;


    @ApiModelProperty(value = "sheng", name = "省")
    private String sheng;


    @ApiModelProperty(value = "shi", name = "shi")
    private String shi;


    @ApiModelProperty(value = "xian", name = "xian")
    private String xian;


    @ApiModelProperty(value = "address", name = "详细地址")
    private String address;


    @ApiModelProperty(value = "phone", name = "打印电话")
    private String phone;

    @ApiModelProperty(value = "isAccounts", name = "是否分账")
    private Integer isAccounts;

    @ApiModelProperty(value = "accountsSet", name = "分账设置账号")
    private String accountsSet;

    @ApiModelProperty("是否开启快递100云打印 0：关闭 1：开启")
    private Integer is_open_cloud;

    @ApiModelProperty("打印机设备码")
    private String siid;

    @ApiModelProperty("云打印回调地址")
    private String cloud_notify;

    @ApiModelProperty("17 track token")
    private String track_secret;

    public String getTrack_secret() {
        return track_secret;
    }

    public void setTrack_secret(String track_secret) {
        this.track_secret = track_secret;
    }

    public Integer getIs_open_cloud() {
        return is_open_cloud;
    }

    public void setIs_open_cloud(Integer is_open_cloud) {
        this.is_open_cloud = is_open_cloud;
    }

    public String getSiid() {
        return siid;
    }

    public void setSiid(String siid) {
        this.siid = siid;
    }

    public String getCloud_notify() {
        return cloud_notify;
    }

    public void setCloud_notify(String cloud_notify) {
        this.cloud_notify = cloud_notify;
    }

    public Integer getIsAccounts()
    {
        return isAccounts;
    }

    public void setIsAccounts(Integer isAccounts)
    {
        this.isAccounts = isAccounts;
    }

    public String getAccountsSet()
    {
        return accountsSet;
    }

    public void setAccountsSet(String accountsSet)
    {
        this.accountsSet = accountsSet;
    }

    public String getPcMchPath()
    {
        return pcMchPath;
    }

    public void setPcMchPath(String pcMchPath)
    {
        this.pcMchPath = pcMchPath;
    }

    public String getWatermarkName()
    {
        return watermarkName;
    }

    public void setWatermarkName(String watermarkName)
    {
        this.watermarkName = watermarkName;
    }

    public String getWatermarkUrl()
    {
        return watermarkUrl;
    }

    public void setWatermarkUrl(String watermarkUrl)
    {
        this.watermarkUrl = watermarkUrl;
    }

    public String getTencentKey()
    {
        return tencentKey;
    }

    public void setTencentKey(String tencentKey)
    {
        this.tencentKey = tencentKey;
    }

    public Integer getIsRegister()
    {
        return isRegister;
    }

    public void setIsRegister(Integer isRegister)
    {
        this.isRegister = isRegister;
    }

    public String getLogoUrl()
    {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl)
    {
        this.logoUrl = logoUrl;
    }

    public String getWxHeader()
    {
        return wxHeader;
    }

    public void setWxHeader(String wxHeader)
    {
        this.wxHeader = wxHeader;
    }

    public String getPageDomain()
    {
        return pageDomain;
    }

    public void setPageDomain(String pageDomain)
    {
        this.pageDomain = pageDomain;
    }

    public Integer getMessageSaveDay()
    {
        return messageSaveDay;
    }

    public void setMessageSaveDay(Integer messageSaveDay)
    {
        this.messageSaveDay = messageSaveDay;
    }

    public Integer getAppLoginValid()
    {
        return appLoginValid;
    }

    public void setAppLoginValid(Integer appLoginValid)
    {
        this.appLoginValid = appLoginValid;
    }

    public String getServerClient()
    {
        return serverClient;
    }

    public void setServerClient(String serverClient)
    {
        this.serverClient = serverClient;
    }

    public Integer getUnipush()
    {
        return unipush;
    }

    public void setUnipush(Integer unipush)
    {
        this.unipush = unipush;
    }

    public String getPushAppkey()
    {
        return pushAppkey;
    }

    public void setPushAppkey(String pushAppkey)
    {
        this.pushAppkey = pushAppkey;
    }

    public String getPushAppid()
    {
        return pushAppid;
    }

    public void setPushAppid(String pushAppid)
    {
        this.pushAppid = pushAppid;
    }

    public String getPushMasterEcret()
    {
        return pushMasterEcret;
    }

    public void setPushMasterEcret(String pushMasterEcret)
    {
        this.pushMasterEcret = pushMasterEcret;
    }

    public Integer getIsExpress()
    {
        return isExpress;
    }

    public void setIsExpress(Integer isExpress)
    {
        this.isExpress = isExpress;
    }

    public String getExpressAddress()
    {
        return expressAddress;
    }

    public void setExpressAddress(String expressAddress)
    {
        this.expressAddress = expressAddress;
    }

    public String getExpressNumber()
    {
        return expressNumber;
    }

    public void setExpressNumber(String expressNumber)
    {
        this.expressNumber = expressNumber;
    }

    public String getExpressKey()
    {
        return expressKey;
    }

    public void setExpressKey(String expressKey)
    {
        this.expressKey = expressKey;
    }

    public Integer getIsKicking()
    {
        return isKicking;
    }

    public void setIsKicking(Integer isKicking)
    {
        this.isKicking = isKicking;
    }

    public Integer getHideYourWallet()
    {
        return hideYourWallet;
    }

    public void setHideYourWallet(Integer hideYourWallet)
    {
        this.hideYourWallet = hideYourWallet;
    }

    public Integer getIsOpen()
    {
        return isOpen;
    }

    public void setIsOpen(Integer isOpen)
    {
        this.isOpen = isOpen;
    }

    public Integer getLimitNum()
    {
        return limitNum;
    }

    public void setLimitNum(Integer limitNum)
    {
        this.limitNum = limitNum;
    }

    public String getKeyword()
    {
        return keyword;
    }

    public void setKeyword(String keyword)
    {
        this.keyword = keyword;
    }

    public String getPrintName()
    {
        return printName;
    }

    public void setPrintName(String printName)
    {
        this.printName = printName;
    }

    public String getPrintUrl()
    {
        return printUrl;
    }

    public void setPrintUrl(String printUrl)
    {
        this.printUrl = printUrl;
    }

    public String getSheng()
    {
        return sheng;
    }

    public void setSheng(String sheng)
    {
        this.sheng = sheng;
    }

    public String getShi()
    {
        return shi;
    }

    public void setShi(String shi)
    {
        this.shi = shi;
    }

    public String getXian()
    {
        return xian;
    }

    public void setXian(String xian)
    {
        this.xian = xian;
    }

    public String getAddress()
    {
        return address;
    }

    public void setAddress(String address)
    {
        this.address = address;
    }

    public String getPhone()
    {
        return phone;
    }

    public void setPhone(String phone)
    {
        this.phone = phone;
    }

    public String getExpress_secret()
    {
        return express_secret;
    }

    public void setExpress_secret(String express_secret)
    {
        this.express_secret = express_secret;
    }

    public String getExpress_tempId()
    {
        return express_tempId;
    }

    public void setExpress_tempId(String express_tempId)
    {
        this.express_tempId = express_tempId;
    }
}
