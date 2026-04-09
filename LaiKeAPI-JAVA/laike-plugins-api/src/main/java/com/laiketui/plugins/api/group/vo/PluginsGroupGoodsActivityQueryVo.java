package com.laiketui.plugins.api.group.vo;

import com.laiketui.domain.vo.MainVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 获取拼团活动列表 查询参数
 *
 * @author Trick
 * @date 2020/11/4 17:27
 */
@Data
@ApiModel(description = "获取拼团活动列表 查询参数")
public class PluginsGroupGoodsActivityQueryVo extends MainVo
{
    @ApiModelProperty(value = "主键", name = "id")
    private Integer id;
    @ApiModelProperty(value = "拼团状态 1.未开始 2.活动中 3.已结束", name = "status")
    private Integer status;
    @ApiModelProperty(value = "活动名称", name = "name")
    private String  name;

}
