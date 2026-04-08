package com.laiketui.domain.vo.mch;

import com.laiketui.core.annotation.ParamsMapping;
import com.laiketui.domain.vo.MainVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * 添加我的门店
 *
 * @author Trick
 * @date 2020/11/30 15:15
 */
@ApiModel(description = "添加/修改我的店铺参数")
public class AddStoreVo extends MainVo
{

    @ApiModelProperty(value = "id", name = "id")
    private Integer id;
    @ApiModelProperty(value = "店铺id", name = "shop_id")
    @ParamsMapping("shop_id")
    private Integer shopId;
    @ApiModelProperty(value = "店铺名称", name = "name")
    private String  name;
    @ApiModelProperty(value = "联系电话", name = "mobile")
    private String  mobile;
    @ApiModelProperty(value = "营业时间", name = "business_hours")
    @ParamsMapping("business_hours")
    private String  businessHours;
    @ApiModelProperty(value = "省市区", name = "city_all")
    @ParamsMapping("city_all")
    private String  cityAll;
    @ApiModelProperty(value = "详细地址", name = "address")
    private String  address;
    @ApiModelProperty(value = "是否默认", name = "is_default")
    @ParamsMapping("is_default")
    private Integer isDefault = 0;

    @ApiModelProperty(value = "区号", name = "cpc")
    private String cpc;

    @ApiModelProperty("州")
    private String province;

    @ApiModelProperty("城市")
    private String city;

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCpc()
    {
        return cpc;
    }

    public void setCpc(String cpc)
    {
        this.cpc = cpc;
    }

    public class write_time
    {
        @ApiModelProperty(value = "核销日期段", name = "write_date")
        @ParamsMapping("write_date")
        private String write_date;

        @ApiModelProperty(value = "核销时间段", name = "write_time")
        @ParamsMapping("write_time")
        private String  write_time;
        @ApiModelProperty(value = "核销次数", name = "write_off_num")
        @ParamsMapping("write_off_num")
        private Integer write_off_num = 0;

        public String getWrite_date()
        {
            return write_date;
        }

        public void setWrite_date(String write_date)
        {
            this.write_date = write_date;
        }

        public String getWrite_time()
        {
            return write_time;
        }

        public void setWrite_time(String write_time)
        {
            this.write_time = write_time;
        }

        public Integer getWrite_off_num()
        {
            return write_off_num;
        }

        public void setWrite_off_num(Integer write_off_num)
        {
            this.write_off_num = write_off_num;
        }
    }


    List<write_time> writeList = new ArrayList<>();

    public List<write_time> getWriteList()
    {
        return writeList;
    }

    public void setWriteList(List<write_time> writeList)
    {
        this.writeList = writeList;
    }

    public Integer getShopId()
    {
        return shopId;
    }

    public void setShopId(Integer shopId)
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

    public String getMobile()
    {
        return mobile;
    }

    public void setMobile(String mobile)
    {
        this.mobile = mobile;
    }

    public String getBusinessHours()
    {
        return businessHours;
    }

    public void setBusinessHours(String businessHours)
    {
        this.businessHours = businessHours;
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

    public Integer getIsDefault()
    {
        return isDefault;
    }

    public void setIsDefault(Integer isDefault)
    {
        this.isDefault = isDefault;
    }

    public Integer getId()
    {
        return id;
    }

    public void setId(Integer id)
    {
        this.id = id;
    }
}
