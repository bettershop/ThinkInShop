package com.laiketui.domain.vo.mch;

import com.laiketui.core.annotation.ParamsMapping;
import com.laiketui.domain.vo.MainVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * 添加/编辑添加店铺
 *
 * @author Trick
 * @date 2021/1/26 14:08
 */
@ApiModel(description = "编辑店铺信息")
public class AddMchVo extends MainVo
{

    @ApiModelProperty(value = "店铺Id", name = "id")
    private Integer id;

    @ApiModelProperty(value = "直播房间号", name = "roomid")
    private Integer roomid;
    @ApiModelProperty(value = "店铺信息", name = "mchInfo")
    private String  mchInfo;
    @ApiModelProperty(value = "店铺名称", name = "mchName")
    private String  mchName;
    @ApiModelProperty(value = "店铺经营范围", name = "confines")
    private String  confines;
    @ApiModelProperty(value = "真实姓名", name = "realName")
    private String  realName;
    @ApiModelProperty(value = "身份证号", name = "idNumber")
    private String  idNumber;
    @ApiModelProperty(value = "联系电话", name = "tel")
    private String  tel;
    @ApiModelProperty(value = "省", name = "shen")
    private String  shen;
    @ApiModelProperty(value = "市", name = "shi")
    private String  shi;
    @ApiModelProperty(value = "县", name = "xian")
    private String  xian;
    @ApiModelProperty(value = "详细地址", name = "address")
    private String  address;
    @ApiModelProperty(value = "店铺性质：0.个人 1.企业", name = "nature")
    private String  nature;
    @ApiModelProperty(value = "营业状态", name = "0=未营业 1=营业 2=打样")
    private Integer isOpen;
    @ApiModelProperty(value = "营业时间", name = "business_hours")
    @ParamsMapping("businessHours")
    private String  businessHours;
    @ApiModelProperty(value = "店铺logo", name = "logo")
    private String  logo;
    @ApiModelProperty(value = "是否支持开票 0.否 1.是", name = "isInvoice")
    private Integer isInvoice        = 0;
    @ApiModelProperty(value = "是否支持商家自配 0.否 1.是", name = "is_self_delivery")
    private Integer is_self_delivery = 0;
    @ApiModelProperty(value = "分类id", name = "cid")
    private Integer cid;
    @ApiModelProperty(value = "营业执照(身份证正反面)", name = "businessLicense")
    private String  license;

    @ApiModelProperty(value = "宣传图", name = "posterImg")
    private String posterImg;

    @ApiModelProperty(value = "头像", name = "headImg")
    private String headImg;

    @ApiModelProperty(value = "区号", name = "cpc")
    private String cpc;

    @ApiModelProperty(value = "货币", name = "preferred_currency")
    private int preferred_currency;

    public int getPreferred_currency()
    {
        return preferred_currency;
    }

    public void setPreferred_currency(int preferred_currency)
    {
        this.preferred_currency = preferred_currency;
    }

    public String getCpc()
    {
        return cpc;
    }

    public void setCpc(String cpc)
    {
        this.cpc = cpc;
    }

    public String getLicense()
    {
        return license;
    }

    public void setLicense(String license)
    {
        this.license = license;
    }

    public Integer getIs_self_delivery()
    {
        return is_self_delivery;
    }

    public void setIs_self_delivery(Integer is_self_delivery)
    {
        this.is_self_delivery = is_self_delivery;
    }

    public String getRealName()
    {
        return realName;
    }

    public void setRealName(String realName)
    {
        this.realName = realName;
    }

    public String getIdNumber()
    {
        return idNumber;
    }

    public void setIdNumber(String idNumber)
    {
        this.idNumber = idNumber;
    }

    public String getPosterImg()
    {
        return posterImg;
    }

    public void setPosterImg(String posterImg)
    {
        this.posterImg = posterImg;
    }

    public String getHeadImg()
    {
        return headImg;
    }

    public void setHeadImg(String headImg)
    {
        this.headImg = headImg;
    }

    public String getMchName()
    {
        return mchName;
    }

    public void setMchName(String mchName)
    {
        this.mchName = mchName;
    }

    public String getLogo()
    {
        return logo;
    }

    public void setLogo(String logo)
    {
        this.logo = logo;
    }

    public Integer getIsOpen()
    {
        return isOpen;
    }

    public void setIsOpen(Integer isOpen)
    {
        this.isOpen = isOpen;
    }

    public String getAddress()
    {
        return address;
    }

    public void setAddress(String address)
    {
        this.address = address;
    }

    public Integer getId()
    {
        return id;
    }

    public void setId(Integer id)
    {
        this.id = id;
    }

    public Integer getRoomid()
    {
        return roomid;
    }

    public void setRoomid(Integer roomid)
    {
        this.roomid = roomid;
    }

    public String getMchInfo()
    {
        return mchInfo;
    }

    public void setMchInfo(String mchInfo)
    {
        this.mchInfo = mchInfo;
    }

    public String getConfines()
    {
        return confines;
    }

    public void setConfines(String confines)
    {
        this.confines = confines;
    }

    public String getTel()
    {
        return tel;
    }

    public void setTel(String tel)
    {
        this.tel = tel;
    }

    public String getShen()
    {
        return shen;
    }

    public void setShen(String shen)
    {
        this.shen = shen;
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

    public String getNature()
    {
        return nature;
    }

    public void setNature(String nature)
    {
        this.nature = nature;
    }

    public Integer getIsInvoice()
    {
        return isInvoice;
    }

    public void setIsInvoice(Integer isInvoice)
    {
        this.isInvoice = isInvoice;
    }

    public Integer getCid()
    {
        return cid;
    }

    public void setCid(Integer cid)
    {
        this.cid = cid;
    }

    public String getBusinessHours()
    {
        return businessHours;
    }

    public void setBusinessHours(String businessHours)
    {
        this.businessHours = businessHours;
    }
}
