package com.laiketui.plugins.api.group.vo;

import com.laiketui.domain.vo.MainVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 添加插件订单配置参数
 *
 * @author Trick
 * @date 2023/3/17 15:04
 */
@Data
@ApiModel(description = "添加插件订单配置参数")
public class PluginsGroupAddOrderConfigVo extends MainVo
{

    @ApiModelProperty(name = "autoTheGoods", value = "自动收货时间 /天")
    private Integer autoTheGoods;
    @ApiModelProperty(name = "orderFailure", value = "订单失效时间 /小时")
    private Integer orderFailure;
    @ApiModelProperty(name = "orderAfter", value = "订单售后时间 /天")
    private Integer orderAfter;
    @ApiModelProperty(name = "autoGoodCommentDay", value = "自动评价设置 /天")
    private Integer autoGoodCommentDay;
    @ApiModelProperty(name = "autoCommentContent", value = "自动评价内容")
    private String  autoCommentContent;
    @ApiModelProperty(name = "content", value = "规则")
    private String  content;
    @ApiModelProperty(name = "isOpen", value = "是否开启拼团插件 插件状态 0关闭 1开启")
    private Integer isOpen;

}
