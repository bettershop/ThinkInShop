package com.laiketui.domain.vo.virtual;

import com.laiketui.domain.vo.MainVo;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class WriteRecordVo extends MainVo
{

    /**
     * 订单号
     */
    @ApiModelProperty(value = "订单号", name = "sNo")
    String sNo;


}
