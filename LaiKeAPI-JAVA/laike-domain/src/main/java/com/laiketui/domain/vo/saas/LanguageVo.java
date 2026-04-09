package com.laiketui.domain.vo.saas;


import com.laiketui.domain.vo.MainVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@ApiModel(description = "语种")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LanguageVo extends MainVo implements Serializable
{

    @ApiModelProperty(value = "语种id", name = "id")
    private Integer id;

    /**
     * 语言名称
     */
    @ApiModelProperty(value = "语种名字", name = "lang_name")
    private String lang_name;

    /**
     * 语言编码
     */
    @ApiModelProperty(value = "语种编码", name = "lang_code")
    private String lang_code;

    /**
     * 展示位置
     */
    @ApiModelProperty(value = "展示编码", name = "show_num")
    private Integer show_num;

    /**
     * 是否回收 1 不回收 2 回收
     */
    @ApiModelProperty(value = "是否回收 1 不回收 2 回收", name = "recycle")
    private int recycle;

    /**
     * 是否显示 1显示 2 不显示
     */
    @ApiModelProperty(value = "是否显示 1显示 2 不显示", name = "show")
    private int is_show = 1;

}