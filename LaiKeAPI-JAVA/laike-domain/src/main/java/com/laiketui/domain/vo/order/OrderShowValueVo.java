package com.laiketui.domain.vo.order;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author 订单按钮显示
 * @version 1.0
 * @description: liuao
 * @date 2025/1/21 9:31
 */
@Data
public class OrderShowValueVo
{
    /**
     * 商品显示按钮
     */
    @JsonProperty("get_order_details_button")
    private List<Map<String,Object>> goodsShowValueList;

    /**
     * 订单详情底部显示按钮
     */
    @JsonProperty("get_button_list")
    private List<Map<String,Object>> bottomShowVlueList;
}
