package com.laiketui.domain.vo.systems;

import com.laiketui.domain.vo.MainVo;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * @author 16640
 * @version 1.0
 * @description: liuao
 * @date 2025/1/6 14:00
 */
@AllArgsConstructor
@Setter
@Getter
public class FrontConfigVo extends MainVo {

    @ApiModelProperty("前端消息保留天数")
    private Integer messageSaveDay;

    @ApiModelProperty("移动端登录有效期")
    private Integer login_validity;

    @ApiModelProperty("水印名称")
    private String watermarkName;

    @ApiModelProperty("水印网址")
    private String watermarkUrl;

    @ApiModelProperty("登录页logo")
    private String logon_logo;

    @ApiModelProperty("版权信息")
    private String copyright_information;

    @ApiModelProperty("备案信息")
    private String record_information;

    @ApiModelProperty("登录页友情链接 json数组")
    private String link_to_landing_page;

    @ApiModelProperty("默认头像设置")
    private String admin_default_portrait;

    @ApiModelProperty(value = "h_Address", name = "h5页面地址")
    private String h_Address;

    @ApiModelProperty(value = "default_lang_code", name = "商城默认语种")
    private String default_lang_code;

    @ApiModelProperty(value = "移动端logo")
    private String app_logo;

    @ApiModelProperty(value = "浏览器icon图标")
    private String html_icon;

    @ApiModelProperty(value = "商城名称")
    private String store_name;

    @ApiModelProperty("商城后台logo")
    private String store_logo;

    @ApiModelProperty("是否开启分账 0：否 1：是")
    private Integer is_accounts;

    @ApiModelProperty("分账账号")
    private String accounts_set;
}
