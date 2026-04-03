package com.laiketui.domain.virtual;

import lombok.Data;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

@Data
@Table(name = "lkt_write_record")
public class WriteRecordModel implements Serializable
{
    /**
     * ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * 核销门店id
     */
    private Integer write_store_id;

    /**
     * 核销时间
     */
    private Date write_time;

    /**
     * 核销码
     */
    private String write_code;

    /**
     * 订单号
     */
    private String  s_no;
    /**
     * 订单详情id
     */
    private Integer p_id;
    /**
     * 核销状态，1为已核销完，2为退款状态中，3为还有核销次数，4为退款成功
     */
    private Integer status;

    public interface status
    {
        //已核销
        Integer isWrite = 1;

        Integer refund = 2;

        Integer continueWrite = 3;

        //退款成功
        Integer finishWrite = 4;

        //退款失败
        Integer returnFail = 5;

    }
}
