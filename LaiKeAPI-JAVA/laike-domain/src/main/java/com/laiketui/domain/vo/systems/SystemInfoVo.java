package com.laiketui.domain.vo.systems;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author 16640
 * @version 1.0
 * @description: liuao
 * @date 2025/1/6 14:51
 */
@Data
public class SystemInfoVo {

    private Integer messageSaveDay;

    private String logon_logo;

    private Integer appLoginValid;

    private String copyright_information;

    private String admin_default_portrait;

    private String record_information;

    private String link_to_landing_page;

    private String watermark_url;

    private String watermark_name;

    @JsonProperty("express_address")
    private String expressAddress;

    @JsonProperty("express_key")
    private String expressKey;

    private String express_secret;

    private String express_tempId;

    @JsonProperty("express_number")
    private String expressNumber;

    @JsonProperty("print_name")
    private String printName;

    @JsonProperty("print_url")
    private String printUrl;

    private String sheng;

    private String shi;

    private String xian;

    private String address;

    private String phone;

    //h5地址
    private String h5_domain;

    /**
     * 商城默认语种
     */
    private String default_lang_code;

    /**
     * 移动端logo
     */
    private String app_logo;

    /**
     *商城logo
     */
    private String store_logo;

    /**
     * 浏览器icon图标
     */
    private String html_icon;

    /**
     * 商城名称
     */
    private String store_name;

    @ApiModelProperty("是否开启快递100云打印 0：关闭 1：开启")
    private Integer is_open_cloud;

    @ApiModelProperty("打印机设备码")
    private String siid;

    @ApiModelProperty("云打印回调地址")
    private String cloud_notify;

    @ApiModelProperty("17 track token")
    private String track_secret;

    @ApiModelProperty("是否开启分账 0：否 1：是")
    private Integer is_accounts;

    @ApiModelProperty("分账账号")
    private String accounts_set;
}
