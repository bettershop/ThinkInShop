package com.laiketui.plugins.api.group.view.h5;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 拼团详情 视图
 *
 * @author Trick
 * @date 2023-03-31 14:52:29
 */
@Data
public class PluginsGroupAppGroupInfoView
{
    @ApiModelProperty(value = "开团id", name = "openId")
    private String                       openId;
    @ApiModelProperty(value = "活动id", name = "acId")
    private String                       acId;
    @ApiModelProperty(value = "活动状态 0=未开始 1=进行中 2=已结束", name = "status")
    private Integer                      status;
    @ApiModelProperty(value = "商品信息", name = "goodsInfo")
    private PluginsGroupAppGoodsInfoView goodsInfo;
    @ApiModelProperty(value = "活动距离开始/结束时间(毫秒) 如果是未开始则为负数 ", name = "timingNum")
    private Long                         timingNum;
    @ApiModelProperty(value = "团队人数", name = "teamNum")
    private Integer                      teamNum;
    @ApiModelProperty(value = "当前人是否参团", name = "isUserJoin")
    private Boolean                      isUserJoin = false;
    @ApiModelProperty(value = "还差多少人成团", name = "diffNum")
    private Integer                      diffNum;
    @ApiModelProperty(value = "是否可以参团", name = "isJoin")
    private Boolean                      isJoin     = false;
    @ApiModelProperty(value = "是否是团长", name = "isTeamMain")
    private Boolean                      isTeamMain = false;
    @ApiModelProperty(value = "拼团状态 0=拼团中 1=拼团成功 2=拼团失败", name = "rule")
    private Integer                      teamStatus;
    @ApiModelProperty(value = "按钮类型 1=立即参团 2=邀请好友参团 3=更多拼团", name = "type")
    private Integer                      type;
    @ApiModelProperty(value = "进度 1=点击开团或参团 2=邀请好友参团 3=达到拼团人数 4=组团成功等待发货", name = "progress")
    private Integer                      progress;
    @ApiModelProperty(value = "订单id-拼团成功且当前登陆人是团员才有值", name = "orderId")
    private Integer                      orderId;
    @ApiModelProperty(value = "0进行中（还未成团，在时间内）1拼团成功2拼团失败3拼团失效", name = "isEnd")
    private Integer                      isEnd;
    @ApiModelProperty(value = "参与人员", name = "joinUserList")
    private List<Map<String, Object>>    joinUserList;

}
