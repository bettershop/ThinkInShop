package com.laiketui.domain.vo.living;

import com.laiketui.domain.vo.MainVo;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author zhuqingyu
 * @create 2024/5/29
 * <p>
 * 直播间管理-主页
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GovernIndexVo extends MainVo
{

    @ApiModelProperty(name = "title", value = "场次ID/直播标题")
    private String title;

    @ApiModelProperty(name = "status", value = "直播状态")
    private String status;

    @ApiModelProperty(name = "userId", value = "用户Id")
    private String userId;

    @ApiModelProperty(name = "startTime", value = "开始时间")
    private String startDate;

    @ApiModelProperty(name = "endTime", value = "结束时间")
    private String endDate;
}
