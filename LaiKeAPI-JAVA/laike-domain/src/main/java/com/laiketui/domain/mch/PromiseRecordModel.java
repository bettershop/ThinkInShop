package com.laiketui.domain.mch;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author zhuqingyu
 * @create 2024/4/28
 */
@Table(name = "lkt_promise_record")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PromiseRecordModel implements Serializable
{
    /**
     * id
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * 店铺id
     */
    private Integer store_id;

    /**
     * 店铺id
     */
    private Integer mch_id;

    /**
     * 保证金金额
     */
    private BigDecimal money;

    /**
     * 保证金状态 1=缴纳 2=退还
     */
    private Integer type;


    /**
     * 审核状态 0=审核中 1=通过 2=拒绝
     */
    private String status;

    /**
     * 添加时间
     */
    private Date add_date;

    /**
     * 备注
     */
    private String remarks;

    /**
     * 审核记录表ID
     */
    private String promise_sh_id;


    /**
     * 类型 1:缴纳，2:退还
     */
    public interface RecordType
    {
        /**
         * 缴纳店铺保证金记录
         */
        Integer PAY_MCH_MARGIN    = 1;
        /**
         * 退还店铺保证金记录
         */
        Integer REFUND_MCH_MARGIN = 2;
    }

}
