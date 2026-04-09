package com.laiketui.domain.dictionary;

import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

/**
 * 楼层管理
 *
 * @author Trick
 * @date 2023/2/15 10:53
 */
@Table(name = "lkt_block_home")
public class BlockHomeModel implements Serializable
{

    public static final Integer ENABLE_Ok = 1;

    public static final Integer ENABLE_NO = 2;

    /**
     * 主键id
     */
    @Id
    private String id;

    /**
     * 楼层名称
     */
    private String block_name;

    /**
     * 关键字
     */
    private String keyword;

    /**
     * 添加时间
     */
    private Date add_date;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 回收标志
     */
    private Integer recycle;

    /**
     * 商城id
     */
    private Integer store_id;

    /**
     * 楼层是否开启显示 1显示  2不显示
     */
    private Integer enableOrNot;

    /**
     * 图片
     */
    private String img;

    public String getImg()
    {
        return img;
    }

    public void setImg(String img)
    {
        this.img = img;
    }

    /**
     * 获取主键id
     *
     * @return id - 主键id
     */
    public String getId()
    {
        return id;
    }

    /**
     * 设置主键id
     *
     * @param id 主键id
     */
    public void setId(String id)
    {
        this.id = id;
    }

    /**
     * 获取楼层名称
     *
     * @return block_name - 楼层名称
     */
    public String getBlock_name()
    {
        return block_name;
    }

    /**
     * 设置楼层名称
     *
     * @param block_name 楼层名称
     */
    public void setBlock_name(String block_name)
    {
        this.block_name = block_name == null ? null : block_name.trim();
    }

    /**
     * 获取关键字
     *
     * @return keyword - 关键字
     */
    public String getKeyword()
    {
        return keyword;
    }

    /**
     * 设置关键字
     *
     * @param keyword 关键字
     */
    public void setKeyword(String keyword)
    {
        this.keyword = keyword == null ? null : keyword.trim();
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
     * 获取排序
     *
     * @return sort - 排序
     */
    public Integer getSort()
    {
        return sort;
    }

    /**
     * 设置排序
     *
     * @param sort 排序
     */
    public void setSort(Integer sort)
    {
        this.sort = sort;
    }

    /**
     * 获取回收标志
     *
     * @return recycle - 回收标志
     */
    public Integer getRecycle()
    {
        return recycle;
    }

    /**
     * 设置回收标志
     *
     * @param recycle 回收标志
     */
    public void setRecycle(Integer recycle)
    {
        this.recycle = recycle;
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

    public Integer getEnableOrNot()
    {
        return enableOrNot;
    }

    public void setEnableOrNot(Integer enableOrNot)
    {
        this.enableOrNot = enableOrNot;
    }
}
