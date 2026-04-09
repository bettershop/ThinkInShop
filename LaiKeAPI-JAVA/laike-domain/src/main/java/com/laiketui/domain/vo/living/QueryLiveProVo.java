package com.laiketui.domain.vo.living;

import com.laiketui.core.annotation.ParamsMapping;
import com.laiketui.domain.vo.MainVo;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author zhuqingyu
 * @create 2024/5/29
 * <p>
 * 直播场次-查询商品
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class QueryLiveProVo extends MainVo
{
    @ApiModelProperty(value = "分类id", name = "cid")
    @ParamsMapping({"cid", "product_class"})
    private Integer cid;

    @ApiModelProperty(value = "品牌id", name = "brandId")
    @ParamsMapping({"brandId", "brand_id"})
    private Integer brandId;

    @ApiModelProperty(value = "商品名称/编号", name = "productTitle")
    @ParamsMapping({"productTitle", "product_title"})
    private String productTitle;

    @ApiModelProperty(value = "店铺名称", name = "mchName")
    @ParamsMapping({"mchName", "mch_name"})
    private String mchName;

    @ApiModelProperty(value = "直播间ID", name = "roomId")
    @ParamsMapping({"roomId", "room_id", "living_id"})
    private String roomId;

    @ApiModelProperty(value = "店铺id", name = "mch_id")
    @ParamsMapping({"mch_id", "mchId"})
    private String mch_id;

    @ApiModelProperty(value = "搜索关键字", name = "key")
    private String key;

    /**
     * 因为和php获取参数不一样，所以加个字段辨别一下
     */
    private Boolean status;
}
