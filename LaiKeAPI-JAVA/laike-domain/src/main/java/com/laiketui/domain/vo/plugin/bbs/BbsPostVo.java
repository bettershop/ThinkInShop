package com.laiketui.domain.vo.plugin.bbs;

import com.laiketui.domain.vo.MainVo;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * @Author: liuao
 * @Date: 2025-09-30-10:23
 * @Description:
 */
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class BbsPostVo extends MainVo
{

    @ApiModelProperty("文章名称")
    private String name;

    @ApiModelProperty("文章标题")
    private String title;

    @ApiModelProperty("状态 1待审核  2审核通过 3审核未通过")
    private Integer status;

    @ApiModelProperty("分类id")
    private String category_id;

    @ApiModelProperty("话题名称")
    private List<String> label_name;

    @ApiModelProperty("类型 0：图片 1：视频 2：图文")
    private Integer type;

    @ApiModelProperty("内容")
    private String content;

    @ApiModelProperty("纯文字")
    private String text;

    @ApiModelProperty("图片")
    private String images;

    @ApiModelProperty("视频id")
    private String fileId;

    @ApiModelProperty("商品id列表")
    private String pro_ids;

    @ApiModelProperty("话题id")
    private Long label_id;

    @ApiModelProperty("长文章封面图")
    private String cover_img;

    @ApiModelProperty("用户id")
    private String user_id;

    @ApiModelProperty("文章id")
    private Long id;

    @ApiModelProperty("视频")
    private String videos;
}

