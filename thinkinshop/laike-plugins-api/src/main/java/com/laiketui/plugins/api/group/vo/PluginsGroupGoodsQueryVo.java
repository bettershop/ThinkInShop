package com.laiketui.plugins.api.group.vo;

import com.laiketui.domain.vo.MainVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 未参加拼团的商品列表
 *
 * @author Trick
 * @date 2020/11/4 17:27
 */
@Data
@ApiModel(description = "未参加拼团的商品列表")
public class PluginsGroupGoodsQueryVo extends MainVo
{
    @ApiModelProperty(value = "主键", name = "id")
    private Integer id;
    @ApiModelProperty(value = "拼团活动id", name = "activityId")
    private String  activityId;
    @ApiModelProperty(value = "商品分类", name = "classId")
    private Integer classId;
    @ApiModelProperty(value = "商品品牌", name = "goodsBrandId")
    private Integer goodsBrandId;
    @ApiModelProperty(value = "商品名称", name = "goodsName")
    private String  goodsName;
    @ApiModelProperty(value = "商品id", name = "goodsName")
    private String  goodsId;
    @ApiModelProperty(value = "h5店铺请求类型   1返回请求商品列表有goodsId就只返回goodsId的商品列表/否则反空数组  2返回排除goodsId的商品列表（goodsId为空反所有）", name = "requestType")
    private Integer requestType;
}
