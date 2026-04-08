package com.laiketui.domain.vo;

import com.laiketui.core.annotation.ParamsMapping;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 主要参数
 *
 * @author Trick
 * @date 2020/10/28 15:29
 */
@ApiModel(description = "基类")
@Data
public class MainVo extends PageModel implements Serializable
{

    @ApiModelProperty(value = "商城id", name = "storeId", example = "1")
    @ParamsMapping({"store_id"})
    private int storeId = 0;

    @ApiModelProperty(value = "限制数量")
    private Integer limit_num;

    /**
     * 系统使用者所在国家 系统使用者所在国家 一般有用户注册信息表中获取 或者系统默认内置国家：中国:156
     */
    @ApiModelProperty(value = "所属国家", name = "country_num", example = "中国：156、美国：840 系统使用者所在国家")
    private Integer country;

    /**
     * 系统使用者所选语种 一般有用户表、管理员表中用户在使用系统时候所选的语种 或者系统默认内置语种：中文简体zh_CN
     */
    @ApiModelProperty(value = "语言", name = "language", example = "中文简体：zh_CN、中文繁体 zh_TW、英文：en_US 说明：系统使用者所选语种")
    private String language;

    /**
     * 系统使用者在维护系统数据时某个功能中数据的所属语种 数据管理功能用
     */
    @ApiModelProperty(value = "语言", name = "lang_code", example = "中文简体：zh_CN、中文繁体 zh_TW、英文：en_US 说明：系统使用者在维护系统数据时某个功能中数据的所属语种 ")
    private String lang_code;

    /**
     * 系统使用者在维护系统数据时某个功能中数据的所属国家 数据管理功能用
     */
    @ApiModelProperty(value = "所属国家", name = "country_num", example = "中国：156、美国：840 说明：系统使用者在维护系统数据时某个功能中数据的所属国家")
    private Integer country_num;

    @ApiModelProperty(value = "token", name = "accessId", example = "1234")
    @ParamsMapping({"TOKEN", "access_id"})
    private String accessId;

    @ApiModelProperty(value = "商城类型", name = "storeType", example = "1")
    @ParamsMapping({"store_type", "mch_type"})
    private int storeType = 1;

    @ApiModelProperty(value = "导出 1=导出数据", name = "exportType")
    private Integer exportType = 0;

    @ApiModelProperty(value = "区号 默认86中国大陆", name = "cpc")
    private String cpc;


    @ApiModelProperty(value = "为1的时候 sql改成查询为空的品牌", name = "brandnotset")
    private int brandnotset;


    @ApiModelProperty(value = "只为1的时候 sql改成查询为空的分类", name = "classnotset")
    private int classnotset;

    private int isAuction = 0;

}
