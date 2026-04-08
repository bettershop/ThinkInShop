package com.laiketui.domain.vo.living;

import com.laiketui.domain.vo.MainVo;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author zhuqingyu
 * @create 2024/6/21
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LivingCommentVo extends MainVo
{
    @ApiModelProperty(value = "直播间ID", name = "roomId")
    private String roomId;

    @ApiModelProperty(value = "评论", name = "comment")
    private String comment;

    @ApiModelProperty(value = "回复ID", name = "pId")
    private Integer pId;

    @ApiModelProperty(value = "评论类型 1:评论，2:关注，3:点赞", name = "commentType")
    private Integer commentType;
}
