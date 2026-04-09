package com.laiketui.plugins.api.group.vo;

import com.laiketui.domain.vo.MainVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 开团记录
 *
 * @author Trick
 * @date 2020/11/4 17:27
 */
@Data
@ApiModel(description = "开团记录")
public class PluginsGroupTeamVo extends MainVo
{
    @ApiModelProperty(value = "主键", name = "id")
    private Integer id;
    @ApiModelProperty(value = "活动id", name = "activityId")
    private String  activityId;
    @ApiModelProperty(value = "开团id", name = "openId")
    private String  openId;
    @ApiModelProperty(value = "拼团状态 0=拼团中 1=拼团成功 2=拼团失败", name = "status")
    private Integer status;
    @ApiModelProperty(value = "团长名称", name = "teamName")
    private String  teamName;
    @ApiModelProperty(value = "商品编号/商品名称", name = "key")
    private String  key;

}
