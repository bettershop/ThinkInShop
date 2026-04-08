package com.laiketui.domain.vo.living;

import com.laiketui.domain.vo.MainVo;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author zhuqingyu
 * @create 2024/6/3
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class QueryLiveOrderVo extends MainVo
{
    @ApiModelProperty(value = "关键字", name = "keyWord")
    private String keyWord;

    @ApiModelProperty(value = "店铺名称", name = "mchName")
    private String mchName;

    @ApiModelProperty(value = "订单状态", name = "status")
    private Integer status;

    @ApiModelProperty(value = "开始日期", name = "startDate")
    private String startDate;

    @ApiModelProperty(value = "结束日期", name = "endDate")
    private String endDate;

    @ApiModelProperty(value = "直播间ID", name = "roomId")
    private String roomId;

    @ApiModelProperty(value = "主播Id", name = "userId")
    private String userId;
}
