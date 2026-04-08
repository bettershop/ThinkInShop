package com.laiketui.domain.vo.goods;

import com.laiketui.domain.vo.MainVo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

/**
 * @version 1.0
 * @description: liuao
 * @date 2025/1/16 15:51
 */
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class DraftsVo extends MainVo
{
    private Integer id;

    /**
     * 店铺id
     */
    private Integer mchId;

    /**
     * 供应商id
     */
    private Integer supplierId;

    /**
     * 草稿内容 json
     */
    private String text;

    /**
     * 添加时间
     */
    private Date addTime;
}
