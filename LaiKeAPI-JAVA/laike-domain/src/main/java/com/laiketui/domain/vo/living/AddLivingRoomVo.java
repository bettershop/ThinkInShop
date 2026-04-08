package com.laiketui.domain.vo.living;

import com.laiketui.domain.vo.MainVo;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author zhuqingyu
 * @create 2024/6/17
 * <p>
 * 添加直播
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddLivingRoomVo extends MainVo
{
    @ApiModelProperty(name = "img", value = "直播封面")
    private String img;

    @ApiModelProperty(name = "title", value = "直播标题")
    private String title;

    @ApiModelProperty(name = "startTime", value = "开始时间")
    private String startTime;

    @ApiModelProperty(name = "anchor_id", value = "主播id")
    private String anchor_id;

    @ApiModelProperty(name = "push_url", value = "推流地址")
    private String push_url;

    @ApiModelProperty(name = "play_url", value = "播放地址")
    private String play_url;
}
