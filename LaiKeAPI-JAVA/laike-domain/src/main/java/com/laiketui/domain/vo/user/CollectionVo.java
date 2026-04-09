package com.laiketui.domain.vo.user;

import com.laiketui.domain.vo.MainVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * 收藏参数
 *
 * @author Trick
 * @date 2021/6/18 11:01
 */
@ApiModel(description = "收藏参数")
public class CollectionVo extends MainVo
{
    @ApiModelProperty(value = "类型", name = "type")
    Integer type;
    @ApiModelProperty(value = "店铺或者是商品名称", name = "mchNameOrProName")
    String  mchNameOrProName;
    @ApiModelProperty(value = "商品分页", name = "proPageSize")
    Integer proPageSize = 15;
    @ApiModelProperty(value = "商品页量", name = "proPageNum")
    Integer proPageNum  = 1;
    @ApiModelProperty(value = "热销还是新品，热销产品0、新品1", name = "rx")
    String  rx;

    public String getRx()
    {
        return rx;
    }

    public void setRx(String rx)
    {
        this.rx = rx;
    }

    public Integer getType()
    {
        return type;
    }

    public void setType(Integer type)
    {
        this.type = type;
    }

    public String getMchNameOrProName()
    {
        return mchNameOrProName;
    }

    public void setMchNameOrProName(String mchNameOrProName)
    {
        this.mchNameOrProName = mchNameOrProName;
    }

    public Integer getProPageSize()
    {
        return proPageSize;
    }

    public void setProPageSize(Integer proPageSize)
    {
        this.proPageSize = proPageSize;
    }

    public Integer getProPageNum()
    {
        return proPageNum;
    }

    public void setProPageNum(Integer proPageNum)
    {
        this.proPageNum = proPageNum;
    }
}
