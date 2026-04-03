package com.laiketui.domain.vo.mch;

import com.laiketui.core.annotation.ParamsMapping;
import com.laiketui.domain.vo.MainVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * 添加我的门店
 *
 * @author Trick
 * @date 2020/11/30 15:15
 */
@ApiModel(description = "添加/修改我的店铺参数")
public class EditAppointVo extends MainVo
{

    @ApiModelProperty(value = "id", name = "id")
    private Integer id;
    @ApiModelProperty(value = "店铺id", name = "shop_id")
    @ParamsMapping("shop_id")
    private Integer shopId;

    @ApiModelProperty(value = "是否默认", name = "is_default")
    @ParamsMapping("is_default")
    private Integer isDefault     = 0;
    @ApiModelProperty(value = "核销日期", name = "write_date")
    @ParamsMapping("write_date")
    private String  write_date;
    @ApiModelProperty(value = "核销时间段", name = "write_time")
    @ParamsMapping("write_time")
    private String  write_time;
    @ApiModelProperty(value = "核销次数", name = "write_off_num")
    @ParamsMapping("write_off_num")
    private Integer write_off_num = 0;


    public Integer getWrite_off_num()
    {
        return write_off_num;
    }

    public void setWrite_off_num(Integer write_off_num)
    {
        this.write_off_num = write_off_num;
    }

    public Integer getId()
    {
        return id;
    }

    public void setId(Integer id)
    {
        this.id = id;
    }

    public Integer getShopId()
    {
        return shopId;
    }

    public void setShopId(Integer shopId)
    {
        this.shopId = shopId;
    }

    public Integer getIsDefault()
    {
        return isDefault;
    }

    public void setIsDefault(Integer isDefault)
    {
        this.isDefault = isDefault;
    }

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
}
