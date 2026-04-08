package com.laiketui.domain.vo.plugin.bbs;

import com.laiketui.domain.vo.MainVo;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @Author: liuao
 * @Date: 2025-10-16-14:40
 * @Description:
 */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BbsCommentVo extends MainVo
{
    @ApiModelProperty("笔记id/笔记标题")
    private String post_key;

    @ApiModelProperty("用户id/用户名")
    private String user_key;

    @ApiModelProperty("评论内容")
    private String content;
}
