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
public class QueryLiveFollowVo extends MainVo
{
    @ApiModelProperty(value = "用户id/账号/昵称", name = "keyWord")
    private String keyWord;

    @ApiModelProperty(value = "开始日期", name = "startDate")
    private String startDate;

    @ApiModelProperty(value = "结束日期", name = "endDate")
    private String endDate;

    @ApiModelProperty(value = "直播间ID", name = "roomId")
    private String roomId;

    private String start_time;

    private String end_time;

}
