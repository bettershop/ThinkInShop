package com.laiketui.common.consts;


import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum OrderShowValueEnum
{

    /** 取消订单*/
    QXDD(1,0,"qxdd"),

    /** 删除订单*/
    SCDD(1,0,"scdd"),

    /**立即付款*/
    LJFK(1,0,"ljfk"),

    /**查看物流*/
    CKWL(1,0,"ckwl"),

    /**提醒发货*/
    TXFH(1,0,"txfh"),

    /**极速退款*/
    JSTK(1,0,"jstk"),

    /**确认收货*/
    QRSH(1,0,"qrsh"),

    /**核销码*/
    HXM(1,0,"hxm"),

    /**立即评价*/
    LJPJ(1,0,"ljpj"),

    /**追加评价*/
    ZJPJ(1,0,"zjpj"),

    /**查看售后*/
    CKSH(1,0,"cksh"),

    /**申请售后*/
    SQSH(1,0,"sqsh"),

    /**提取码*/
    TQM(1,0,"tqm"),

    /**查看提取码*/
    CKTQM(1,0,"cktqm"),

    /**再次购买*/
    ZCGM(1,0,"zcgm"),

    /**申请开票*/
    SQKP(1,0,"sqkp"),

    /**上传凭证*/
    SCPZ(1,0,"scpz"),


    /**凭证审核中*/
    PZSHZ(1,0,"pzshz");

    private final Integer showValue;

    private final Integer noShowValue;

    private final String name;
}
