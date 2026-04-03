package com.laiketui.domain.vo.mch;

import com.laiketui.core.annotation.ParamsMapping;
import com.laiketui.domain.vo.MainVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * 添加运费
 *
 * @author Trick
 * @date 2020/12/1 16:10
 */
@ApiModel(description = "添加运费")
public class AddFreihtVo extends MainVo
{

    @ApiModelProperty(value = "运费id", name = "id")
    @ParamsMapping("id")
    private Integer fid;

    @ApiModelProperty(value = "店铺id", name = "shop_id")
    @ParamsMapping("shop_id")
    private Integer shopId;
    @ApiModelProperty(value = "规则名称", name = "name")
    private String  name;
    @ApiModelProperty(value = "类型", name = "type")
    private Integer type;
    @ApiModelProperty(value = "是否默认", name = "is_default")
    @ParamsMapping("is_default")
    private Integer isDefault;
    @ApiModelProperty(value = "运费规则", name = "hidden_freight")
    @ParamsMapping("hidden_freight")
    private String  hiddenFreight;
    @ApiModelProperty(value = "是否包邮设置", name = "is_package_settings")
    @ParamsMapping("is_package_settings")
    private Integer isPackageSettings;
    @ApiModelProperty(value = "包邮设置", name = "package_settings")
    @ParamsMapping("package_settings")
    private String  packageSettings;
    @ApiModelProperty(value = "是否不配送", name = "is_no_delivery")
    @ParamsMapping("is_no_delivery")
    private Integer isNoDelivery;
    @ApiModelProperty(value = "不配送区域", name = "no_delivery")
    @ParamsMapping("no_delivery")
    private String  noDelivery;
    @ApiModelProperty(value = "供应商id", name = "supplierId")
    private Integer supplierId;

    @ApiModelProperty(value = "默认运费规则", name = "default_freight")
    @ParamsMapping("default_freight")
    private String  defaultFreight;

    @ApiModelProperty(value = "不配送地区id", name = "threeIdsList")
    @ParamsMapping("threeIdsList")
    private String  threeIdsList;

    public String getThreeIdsList()
    {
        return threeIdsList;
    }

    public void setThreeIdsList(String threeIdsList)
    {
        this.threeIdsList = threeIdsList;
    }

    public void setIsPackageSettings(Integer isPackageSettings)
    {
        this.isPackageSettings = isPackageSettings;
    }

    public void setPackageSettings(String packageSettings)
    {
        this.packageSettings = packageSettings;
    }

    public void setIsNoDelivery(Integer isNoDelivery)
    {
        this.isNoDelivery = isNoDelivery;
    }

    public void setNoDelivery(String noDelivery)
    {
        this.noDelivery = noDelivery;
    }

    public Integer getIsPackageSettings()
    {
        return isPackageSettings;
    }

    public String getPackageSettings()
    {
        return packageSettings;
    }

    public Integer getIsNoDelivery()
    {
        return isNoDelivery;
    }

    public String getNoDelivery()
    {
        return noDelivery;
    }

    public Integer getFid()
    {
        return fid;
    }

    public void setFid(Integer fid)
    {
        this.fid = fid;
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

    public Integer getType()
    {
        return type;
    }

    public void setType(Integer type)
    {
        this.type = type;
    }

    public Integer getIsDefault()
    {
        return isDefault;
    }

    public void setIsDefault(Integer isDefault)
    {
        this.isDefault = isDefault;
    }

    public String getHiddenFreight()
    {
        return hiddenFreight;
    }

    public void setHiddenFreight(String hiddenFreight)
    {
        this.hiddenFreight = hiddenFreight;
    }

    public Integer getSupplierId()
    {
        return supplierId;
    }

    public void setSupplierId(Integer supplierId)
    {
        this.supplierId = supplierId;
    }

    public String getDefaultFreight()
    {
        return defaultFreight;
    }

    public void setDefaultFreight(String defaultFreight)
    {
        this.defaultFreight = defaultFreight;
    }
}
