package com.laiketui.plugins.api.group.view.h5;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 评论 视图
 *
 * @author Trick
 * @date 2023/3/30 10:52
 */
@Data
public class PluginsGroupAppCommentInfoView
{
    @ApiModelProperty(value = "评论id", name = "cid")
    private Integer cid;
    @ApiModelProperty(value = "用户名称", name = "userName")
    private String  userName;
    @ApiModelProperty(value = "用户头像", name = "userHadedImg")
    private String  userHadedImg;
    @ApiModelProperty(value = "评论内容", name = "commentContext")
    private String  commentContext;


}
