package com.laiketui.domain.vo.mch;

import com.laiketui.core.annotation.ParamsMapping;
import com.laiketui.domain.vo.MainVo;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class AddAppointVo extends MainVo
{

    @ApiModelProperty(value = "id", name = "id")
    private Integer id;
    @ApiModelProperty(value = "商城id", name = "store_id")
    @ParamsMapping("store_id")
    private Integer store_id;
    @ApiModelProperty(value = "店铺id", name = "mch_id")
    @ParamsMapping("mch_id")
    private Integer mch_id;

    @ApiModelProperty(value = "门店id", name = "mch_store_id")
    @ParamsMapping("mch_store_id")
    private Integer mch_store_id  = 0;
    @ApiModelProperty(value = "核销日期", name = "write_date")
    @ParamsMapping("write_date")
    private String  write_date;
    @ApiModelProperty(value = "核销时间段", name = "write_time")
    @ParamsMapping("write_time")
    private String  write_time;
    @ApiModelProperty(value = "核销次数", name = "write_off_num")
    @ParamsMapping("write_off_num")
    private Integer write_off_num = 0;
}
