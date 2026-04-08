package com.laiketui.domain.vo.living;

import com.laiketui.domain.vo.MainVo;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author zhuqingyu
 * @create 2024/6/18
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class QueryLivingDataVo extends MainVo
{
    @ApiModelProperty(name = "shop_id", value = "店铺id")
    int shop_id;

    @ApiModelProperty(name = "startTime", value = "开始时间")
    String startTime;

    @ApiModelProperty(name = "endTime", value = "结束时间")
    String endTime;

    @ApiModelProperty(name = "keyWord", value = "直播协议")
    String keyWord;

    @ApiModelProperty(name = "agreeContent", value = "直播协议")
    String status;

}
