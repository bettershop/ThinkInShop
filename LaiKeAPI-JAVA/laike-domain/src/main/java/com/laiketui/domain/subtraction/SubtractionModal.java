package com.laiketui.domain.subtraction;

import javax.persistence.*;
import java.util.Date;

@Table(name = "lkt_subtraction")
public class SubtractionModal
{

    /**
     * 1.阶梯满减 2.循环满减 3.满赠 4.满件折扣
     */
    @Transient
    public static final Integer SUBTRACTION_JT = 1;
    /**
     * 1.阶梯满减 2.循环满减 3.满赠 4.满件折扣
     */
    @Transient
    public static final Integer SUBTRACTION_XH = 2;
    /**
     * 1.阶梯满减 2.循环满减 3.满赠 4.满件折扣
     */
    @Transient
    public static final Integer SUBTRACTION_MZ = 3;
    /**
     * 1.阶梯满减 2.循环满减 3.满赠 4.满件折扣
     */
    @Transient
    public static final Integer SUBTRACTION_ZK = 4;

    /**
     * 1.指定分类 2.全场满减 3.指定品牌
     */
    @Transient
    public static final String SUBTRACTION_RANG_ZDFL = "1";

    /**
     * 1.指定分类 2.全场满减 3.指定品牌
     */
    @Transient
    public static final String SUBTRACTION_RANG_QCMJ = "2";
    /**
     * 1.指定分类 2.全场满减 3.指定品牌
     */
    @Transient
    public static final String SUBTRACTION_RANG_ZDPP = "3";

    /**
     * 活动状态 1.未开始 2.开启 3.关闭 4.已结束
     */
    @Transient
    public static final Integer NOT_START = 1;

    /**
     * 活动状态 1.未开始 2.开启 3.关闭 4.已结束
     */
    @Transient
    public static final Integer STARTING = 2;

    /**
     * 活动状态 1.未开始 2.开启 3.关闭 4.已结束
     */
    @Transient
    public static final Integer CLOSE = 3;

    /**
     * 活动状态 1.未开始 2.开启 3.关闭 4.已结束
     */
    @Transient
    public static final Integer END = 4;


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
     * 店主id
     */
    private Integer mch_id;

    /**
     * 活动标题
     */
    private String title;

    /**
     * 活动名称
     */
    private String name;

    /**
     * 满减应用范围
     */
    private String subtraction_range;

    /**
     * 满减范围参数
     */
    private String subtraction_parameter;

    /**
     * 满减类型 1.阶梯满减 2.循环满减 3.满赠 4.满件折扣
     */
    private Integer subtraction_type;

    /**
     * 活动开始时间
     */
    private Date starttime;

    /**
     * 活动结束时间
     */
    private Date endtime;

    /**
     * 满减图片显示位置
     */
    private String position_zfc;

    /**
     * 满减图片
     */
    private String image;

    /**
     * 活动状态 1.未开始 2.开启 3.关闭 4.已结束
     */
    private Integer status;

    /**
     * 添加时间
     */
    private Date add_date;

    /**
     * 满减
     */
    private String subtraction;

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
     * 获取店主id
     *
     * @return mch_id - 店主id
     */
    public Integer getMch_id()
    {
        return mch_id;
    }

    /**
     * 设置店主id
     *
     * @param mch_id 店主id
     */
    public void setMch_id(Integer mch_id)
    {
        this.mch_id = mch_id;
    }

    /**
     * 获取活动标题
     *
     * @return title - 活动标题
     */
    public String getTitle()
    {
        return title;
    }

    /**
     * 设置活动标题
     *
     * @param title 活动标题
     */
    public void setTitle(String title)
    {
        this.title = title == null ? null : title.trim();
    }

    /**
     * 获取活动名称
     *
     * @return name - 活动名称
     */
    public String getName()
    {
        return name;
    }

    /**
     * 设置活动名称
     *
     * @param name 活动名称
     */
    public void setName(String name)
    {
        this.name = name == null ? null : name.trim();
    }

    /**
     * 获取满减应用范围
     *
     * @return subtraction_range - 满减应用范围
     */
    public String getSubtraction_range()
    {
        return subtraction_range;
    }

    /**
     * 设置满减应用范围
     *
     * @param subtraction_range 满减应用范围
     */
    public void setSubtraction_range(String subtraction_range)
    {
        this.subtraction_range = subtraction_range == null ? null : subtraction_range.trim();
    }

    /**
     * 获取满减范围参数
     *
     * @return subtraction_parameter - 满减范围参数
     */
    public String getSubtraction_parameter()
    {
        return subtraction_parameter;
    }

    /**
     * 设置满减范围参数
     *
     * @param subtraction_parameter 满减范围参数
     */
    public void setSubtraction_parameter(String subtraction_parameter)
    {
        this.subtraction_parameter = subtraction_parameter == null ? null : subtraction_parameter.trim();
    }

    /**
     * 获取满减类型 1.阶梯满减 2.循环满减 3.满赠 4.满件折扣
     *
     * @return subtraction_type - 满减类型 1.阶梯满减 2.循环满减 3.满赠 4.满件折扣
     */
    public Integer getSubtraction_type()
    {
        return subtraction_type;
    }

    /**
     * 设置满减类型 1.阶梯满减 2.循环满减 3.满赠 4.满件折扣
     *
     * @param subtraction_type 满减类型 1.阶梯满减 2.循环满减 3.满赠 4.满件折扣
     */
    public void setSubtraction_type(Integer subtraction_type)
    {
        this.subtraction_type = subtraction_type;
    }

    /**
     * 获取活动开始时间
     *
     * @return starttime - 活动开始时间
     */
    public Date getStarttime()
    {
        return starttime;
    }

    /**
     * 设置活动开始时间
     *
     * @param starttime 活动开始时间
     */
    public void setStarttime(Date starttime)
    {
        this.starttime = starttime;
    }

    /**
     * 获取活动结束时间
     *
     * @return endtime - 活动结束时间
     */
    public Date getEndtime()
    {
        return endtime;
    }

    /**
     * 设置活动结束时间
     *
     * @param endtime 活动结束时间
     */
    public void setEndtime(Date endtime)
    {
        this.endtime = endtime;
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
     * 获取满减图片
     *
     * @return image - 满减图片
     */
    public String getImage()
    {
        return image;
    }

    /**
     * 设置满减图片
     *
     * @param image 满减图片
     */
    public void setImage(String image)
    {
        this.image = image == null ? null : image.trim();
    }

    /**
     * 获取活动状态 1.未开始 2.开启 3.关闭 4.已结束
     *
     * @return status - 活动状态 1.未开始 2.开启 3.关闭 4.已结束
     */
    public Integer getStatus()
    {
        return status;
    }

    /**
     * 设置活动状态 1.未开始 2.开启 3.关闭 4.已结束
     *
     * @param status 活动状态 1.未开始 2.开启 3.关闭 4.已结束
     */
    public void setStatus(Integer status)
    {
        this.status = status;
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
     * 获取满减
     *
     * @return subtraction - 满减
     */
    public String getSubtraction()
    {
        return subtraction;
    }

    /**
     * 设置满减
     *
     * @param subtraction 满减
     */
    public void setSubtraction(String subtraction)
    {
        this.subtraction = subtraction == null ? null : subtraction.trim();
    }
}