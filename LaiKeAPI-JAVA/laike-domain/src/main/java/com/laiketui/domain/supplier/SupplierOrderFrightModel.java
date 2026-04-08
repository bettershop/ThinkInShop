package com.laiketui.domain.supplier;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Table(name = "lkt_supplier_order_fright")
public class SupplierOrderFrightModel implements Serializable
{
    /**
     * id
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * 商城id
     */
    private Integer store_id;

    /**
     * 订单号
     */
    @Column(name = "sno")
    private String sNo;

    /**
     * 总运费
     */
    private BigDecimal total_fright;

    /**
     * 供应商id
     */
    private String supplier_id;

    /**
     * 是否结算 0.否 1.是
     */
    private Integer is_settlement;

    /**
     * 创建时间
     */
    private Date add_date;

    /**
     * 商品id
     */
    private Integer detail_id;

    /**
     * 运费
     */
    private BigDecimal freight;

    public Integer getDetail_id()
    {
        return detail_id;
    }

    public void setDetail_id(Integer detail_id)
    {
        this.detail_id = detail_id;
    }

    public BigDecimal getFreight()
    {
        return freight;
    }

    public void setFreight(BigDecimal freight)
    {
        this.freight = freight;
    }

    /**
     * 获取id
     *
     * @return id - id
     */
    public Integer getId()
    {
        return id;
    }

    /**
     * 设置id
     *
     * @param id id
     */
    public void setId(Integer id)
    {
        this.id = id;
    }

    /**
     * 获取商城id
     *
     * @return store_id - 商城id
     */
    public Integer getStore_id()
    {
        return store_id;
    }

    /**
     * 设置商城id
     *
     * @param store_id 商城id
     */
    public void setStore_id(Integer store_id)
    {
        this.store_id = store_id;
    }

    /**
     * 获取订单号
     *
     * @return sNo - 订单号
     */
    public String getsNo()
    {
        return sNo;
    }

    /**
     * 设置订单号
     *
     * @param sNo 订单号
     */
    public void setsNo(String sNo)
    {
        this.sNo = sNo == null ? null : sNo.trim();
    }

    /**
     * 获取总运费
     *
     * @return total_fright - 总运费
     */
    public BigDecimal getTotal_fright()
    {
        return total_fright;
    }

    /**
     * 设置总运费
     *
     * @param total_fright 总运费
     */
    public void setTotal_fright(BigDecimal total_fright)
    {
        this.total_fright = total_fright;
    }

    /**
     * 获取供应商id
     *
     * @return supplier_id - 供应商id
     */
    public String getSupplier_id()
    {
        return supplier_id;
    }

    /**
     * 设置供应商id
     *
     * @param supplier_id 供应商id
     */
    public void setSupplier_id(String supplier_id)
    {
        this.supplier_id = supplier_id == null ? null : supplier_id.trim();
    }

    public Integer getIs_settlement()
    {
        return is_settlement;
    }

    public void setIs_settlement(Integer is_settlement)
    {
        this.is_settlement = is_settlement;
    }

    /**
     * 获取创建时间
     *
     * @return add_date - 创建时间
     */
    public Date getAdd_date()
    {
        return add_date;
    }

    /**
     * 设置创建时间
     *
     * @param add_date 创建时间
     */
    public void setAdd_date(Date add_date)
    {
        this.add_date = add_date;
    }
}