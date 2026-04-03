package com.laiketui.domain.vo.mch;

import com.laiketui.core.annotation.ParamsMapping;
import com.laiketui.domain.vo.MainVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.web.multipart.MultipartFile;

/**
 * 设置店铺参数
 *
 * @author Trick
 * @date 2020/11/24 9:06
 */
@ApiModel(description = "基类")
public class SetShopVo extends MainVo
{

    @ApiModelProperty(value = "店铺id", name = "shop_id")
    @ParamsMapping("shop_id")
    private int    shopId;
    @ApiModelProperty(value = "店铺名称", name = "name")
    private String name;
    @ApiModelProperty(value = "店铺信息", name = "shop_information")
    @ParamsMapping("shop_information")
    private String shopInformation;
    @ApiModelProperty(value = "是否营业：0.未营业 1.营业中 2.打样", name = "is_open")
    @ParamsMapping("is_open")
    private String isOpen;
    @ApiModelProperty(value = "营业时间", name = "business_hours")
    @ParamsMapping("business_hours")
    private String businessHours;
    @ApiModelProperty(value = "真实姓名", name = "realname")
    private String realname;
    @ApiModelProperty(value = "身份证号", name = "ID_number")
    @ParamsMapping("ID_number")
    private String idNumber;
    @ApiModelProperty(value = "联系电话", name = "tel")
    private String tel;
    @ApiModelProperty(value = "省市县", name = "city_all")
    @ParamsMapping("city_all")
    private String cityAll;
    @ApiModelProperty(value = "联系地址", name = "address")
    private String address;
    @ApiModelProperty(value = "经营范围", name = "shop_range")
    @ParamsMapping("shop_range")
    private String shopRange;

    @ApiModelProperty(value = "店铺logo", name = "file")
    @ParamsMapping({"image", "file"})
    private MultipartFile file;
    @ApiModelProperty(value = "是否支持开票 0.否 1.是", name = "isInvoice")
    private Integer       isInvoice;
    @ApiModelProperty(value = "店铺id", name = "cid")
    private Integer       cid;
    @ApiModelProperty(value = "宣传图", name = "posterImg")
    private String        posterImg;

    @ApiModelProperty(value = "头像", name = "headImg")
    private String  headImg;
    @ApiModelProperty(value = "是否支持商家自配 0.否 1.是", name = "is_self_delivery")
    private Integer is_self_delivery;

    @ApiModelProperty(value = "区号", name = "cpc")
    private String cpc;

    public String getCpc()
    {
        return cpc;
    }

    public void setCpc(String cpc)
    {
        this.cpc = cpc;
    }

    public String getHeadImg()
    {
        return headImg;
    }

    public void setHeadImg(String headImg)
    {
        this.headImg = headImg;
    }

    public String getPosterImg()
    {
        return posterImg;
    }

    public void setPosterImg(String posterImg)
    {
        this.posterImg = posterImg;
    }

    public Integer getIs_self_delivery()
    {
        return is_self_delivery;
    }

    public void setIs_self_delivery(Integer is_self_delivery)
    {
        this.is_self_delivery = is_self_delivery;
    }

    public int getShopId()
    {
        return shopId;
    }

    public void setShopId(int shopId)
    {
        this.shopId = shopId;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getShopInformation()
    {
        return shopInformation;
    }

    public void setShopInformation(String shopInformation)
    {
        this.shopInformation = shopInformation;
    }

    public String getIsOpen()
    {
        return isOpen;
    }

    public void setIsOpen(String isOpen)
    {
        this.isOpen = isOpen;
    }

    public String getRealname()
    {
        return realname;
    }

    public void setRealname(String realname)
    {
        this.realname = realname;
    }

    public String getIdNumber()
    {
        return idNumber;
    }

    public void setIdNumber(String idNumber)
    {
        this.idNumber = idNumber;
    }

    public String getTel()
    {
        return tel;
    }

    public void setTel(String tel)
    {
        this.tel = tel;
    }

    public String getCityAll()
    {
        return cityAll;
    }

    public void setCityAll(String cityAll)
    {
        this.cityAll = cityAll;
    }

    public String getAddress()
    {
        return address;
    }

    public void setAddress(String address)
    {
        this.address = address;
    }

    public String getShopRange()
    {
        return shopRange;
    }

    public void setShopRange(String shopRange)
    {
        this.shopRange = shopRange;
    }

    public MultipartFile getFile()
    {
        return file;
    }

    public void setFile(MultipartFile file)
    {
        this.file = file;
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
