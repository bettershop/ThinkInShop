package com.laiketui.plugins.api.group.vo;

import com.laiketui.domain.vo.MainVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 参团记录列表参数
 *
 * @author Trick
 * @date 2023/3/22 10:39
 */
@Data
@ApiModel(description = "参团记录列表参数")
public class PluginsGroupJoinRecordVo extends MainVo
{
    @ApiModelProperty(value = "主键", name = "id")
    private Integer id;
    @ApiModelProperty(value = "拼团状态 0=拼团中 1=拼团成功 2=拼团失败", name = "status")
    private Integer status;
    @ApiModelProperty(value = "用户名称", name = "userName")
    private String  userName;
    @ApiModelProperty(value = "团长名称", name = "teamName")
    private String  teamName;
    @ApiModelProperty(value = "商品编号/商品名称", name = "key")
    private String  key;
    @ApiModelProperty(value = "团队数量", name = "teamNum")
    private Integer teamNum;

}
