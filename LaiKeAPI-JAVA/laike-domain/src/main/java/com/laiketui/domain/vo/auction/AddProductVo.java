package com.laiketui.domain.vo.auction;


import com.laiketui.domain.vo.MainVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * 添加商品到竞拍活动 参数
 *
 * @author Trick
 * @date 2022/7/1 15:30
 */
@ApiModel(description = "添加/编辑 商品到竞拍活动")
public class AddProductVo extends MainVo
{
    @ApiModelProperty(name = "attrList", value = "商品规格id集")
    private String attrList;

    public String getAttrList()
    {
        return attrList;
    }

    public void setAttrList(String attrList)
    {
        this.attrList = attrList;
    }
}
