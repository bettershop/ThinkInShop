package com.laiketui.domain.vo.systems;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.laiketui.domain.vo.MainVo;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

/**
 * @author 16640
 * @version 1.0
 * @description: liuao
 * @date 2025/1/7 9:20
 */
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class SensitiveVo extends MainVo {

    @ApiModelProperty("关键字")
    private String keyWord;

    @ApiModelProperty("id")
    private Integer id;

    @ApiModelProperty("敏感词")
    private String word;

    @ApiModelProperty("添加时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date add_time;

    @ApiModelProperty("id列表")
    private String ids;
}
