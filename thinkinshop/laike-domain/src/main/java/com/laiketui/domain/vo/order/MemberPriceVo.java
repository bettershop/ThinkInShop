package com.laiketui.domain.vo.order;

import com.laiketui.domain.vo.MainVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;
import java.util.Map;

/**
 * 计算会员折扣参数
 *
 * @author Trick
 * @date 2022/2/10 10:58
 */
@ApiModel(description = "计算会员折扣参数")
public class MemberPriceVo extends MainVo
{

    /**
     * 每个店铺下的商品信息
     */
    @ApiModelProperty(value = "购买的商品信息集(包括多店铺)", name = "productList")
    private List<Map<String, Object>> mchProductList;

    @ApiModelProperty(value = "购买者userid", name = "userId")
    private String userId;

    public List<Map<String, Object>> getMchProductList()
    {
        return mchProductList;
    }

    public void setMchProductList(List<Map<String, Object>> mchProductList)
    {
        this.mchProductList = mchProductList;
    }

    public String getUserId()
    {
        return userId;
    }

    public void setUserId(String userId)
    {
        this.userId = userId;
    }
}
