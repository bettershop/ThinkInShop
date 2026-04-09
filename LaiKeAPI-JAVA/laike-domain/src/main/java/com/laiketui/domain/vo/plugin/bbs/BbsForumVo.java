package com.laiketui.domain.vo.plugin.bbs;

import com.laiketui.domain.vo.MainVo;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @Author: liuao
 * @Date: 2025-09-29-10:57
 * @Description:
 */
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class BbsForumVo extends MainVo
{
    @ApiModelProperty("关键字（贴吧名称/用户id）")
    private String keywords;

    @ApiModelProperty("分类id")
    private Integer cid;

    @ApiModelProperty("是否需要吧主审核 0.不需要 1.需要")
    private Integer review;

    @ApiModelProperty("简介")
    private String description;

    @ApiModelProperty("种草官名称")
    private String name;

    @ApiModelProperty("用户头像")
    private String headimgurl;
}
