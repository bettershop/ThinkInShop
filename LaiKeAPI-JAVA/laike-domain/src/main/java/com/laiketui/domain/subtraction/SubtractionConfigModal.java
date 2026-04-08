package com.laiketui.domain.subtraction;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.util.Date;

@Table(name = "lkt_subtraction_config")
public class SubtractionConfigModal
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
     * 是否开启满减 0.否 1.是
     */
    private Integer is_subtraction;

    /**
     * 满减应用范围
     */
    private String range_zfc;

    /**
     * 满赠商品
     */
    private String pro_id;

    /**
     * 满减图片显示位置
     */
    private String position_zfc;

    /**
     * 满减包邮设置 0.否 1.是
     */
    private Integer is_shipping;

    /**
     * 单笔满多少包邮
     */
    private BigDecimal z_money;

    /**
     * 不参与包邮地区
     */
    private String address_id;

    /**
     * 添加时间
     */
    private Date add_date;

    /**
     * 移动端图片
     */
    private String mobile_terminal_image;

    /**
     * pc端图片
     */
    private String pc_side_picture;

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
     * 获取是否开启满减 0.否 1.是
     *
     * @return is_subtraction - 是否开启满减 0.否 1.是
     */
    public Integer getIs_subtraction()
    {
        return is_subtraction;
    }

    /**
     * 设置是否开启满减 0.否 1.是
     *
     * @param is_subtraction 是否开启满减 0.否 1.是
     */
    public void setIs_subtraction(Integer is_subtraction)
    {
        this.is_subtraction = is_subtraction;
    }

    /**
     * 获取满减应用范围
     *
     * @return range_zfc - 满减应用范围
     */
    public String getRange_zfc()
    {
        return range_zfc;
    }

    /**
     * 设置满减应用范围
     *
     * @param range_zfc 满减应用范围
     */
    public void setRange_zfc(String range_zfc)
    {
        this.range_zfc = range_zfc == null ? null : range_zfc.trim();
    }

    /**
     * 获取满赠商品
     *
     * @return pro_id - 满赠商品
     */
    public String getPro_id()
    {
        return pro_id;
    }

    /**
     * 设置满赠商品
     *
     * @param pro_id 满赠商品
     */
    public void setPro_id(String pro_id)
    {
        this.pro_id = pro_id == null ? null : pro_id.trim();
    }

    /**
     * 获取满减图片显示位置
     *
     * @return position_zfc - 满减图片显示位置
     */
    public String getPosition_zfc()
    {
        return position_zfc;
    }

    /**
     * 设置满减图片显示位置
     *
     * @param position_zfc 满减图片显示位置
     */
    public void setPosition_zfc(String position_zfc)
    {
        this.position_zfc = position_zfc == null ? null : position_zfc.trim();
    }

    /**
     * 获取满减包邮设置 0.否 1.是
     *
     * @return is_shipping - 满减包邮设置 0.否 1.是
     */
    public Integer getIs_shipping()
    {
        return is_shipping;
    }

    /**
     * 设置满减包邮设置 0.否 1.是
     *
     * @param is_shipping 满减包邮设置 0.否 1.是
     */
    public void setIs_shipping(Integer is_shipping)
    {
        this.is_shipping = is_shipping;
    }

    /**
     * 获取单笔满多少包邮
     *
     * @return z_money - 单笔满多少包邮
     */
    public BigDecimal getZ_money()
    {
        return z_money;
    }

    /**
     * 设置单笔满多少包邮
     *
     * @param z_money 单笔满多少包邮
     */
    public void setZ_money(BigDecimal z_money)
    {
        this.z_money = z_money;
    }

    /**
     * 获取不参与包邮地区
     *
     * @return address_id - 不参与包邮地区
     */
    public String getAddress_id()
    {
        return address_id;
    }

    /**
     * 设置不参与包邮地区
     *
     * @param address_id 不参与包邮地区
     */
    public void setAddress_id(String address_id)
    {
        this.address_id = address_id == null ? null : address_id.trim();
    }

    /**
     * 获取添加时间
     *
     * @return add_date - 添加时间
     */
    public Date getAdd_date()
    {
        return add_date;
    }

    /**
     * 设置添加时间
     *
     * @param add_date 添加时间
     */
    public void setAdd_date(Date add_date)
    {
        this.add_date = add_date;
    }

    /**
     * 获取移动端图片
     *
     * @return mobile_terminal_image - 移动端图片
     */
    public String getMobile_terminal_image()
    {
        return mobile_terminal_image;
    }

    /**
     * 设置移动端图片
     *
     * @param mobile_terminal_image 移动端图片
     */
    public void setMobile_terminal_image(String mobile_terminal_image)
    {
        this.mobile_terminal_image = mobile_terminal_image == null ? null : mobile_terminal_image.trim();
    }

    /**
     * 获取pc端图片
     *
     * @return pc_side_picture - pc端图片
     */
    public String getPc_side_picture()
    {
        return pc_side_picture;
    }

    /**
     * 设置pc端图片
     *
     * @param pc_side_picture pc端图片
     */
    public void setPc_side_picture(String pc_side_picture)
    {
        this.pc_side_picture = pc_side_picture == null ? null : pc_side_picture.trim();
    }
}