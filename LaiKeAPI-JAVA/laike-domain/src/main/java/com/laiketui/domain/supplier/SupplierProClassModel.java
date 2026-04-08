package com.laiketui.domain.supplier;

import com.laiketui.domain.vo.PageModel;

import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.io.Serializable;
import java.util.Date;

@Table(name = "lkt_supplier_pro_class")
public class SupplierProClassModel implements Serializable
{
    @Transient
    private PageModel pageModel;

    public PageModel getPageModel()
    {
        return pageModel;
    }

    public void setPageModel(PageModel pageModel)
    {
        this.pageModel = pageModel;
    }

    /**
     * 分类id
     */
    @Id
    private Integer cid;

    /**
     * 上级id
     */
    private Integer sid;

    /**
     * 分类名称
     */
    private String pname;

    /**
     * 英文名称
     */
    private String english_name;

    /**
     * 分类图片
     */
    private String img;

    /**
     * 小图标
     */
    private String bg;

    /**
     * 级别
     */
    private Integer level;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 商城id
     */
    private Integer store_id;

    /**
     * 添加时间
     */
    private Date add_date;

    /**
     * 回收站 0.不回收 1.回收
     */
    private Integer recycle;

    /**
     * 是否显示 0.不显示 1.显示
     */
    private Integer is_display;

    /**
     * 供应商id
     */
    private Integer supplier_id;

    /**
     * 审核状态 0.待审核 1.审核通过 2.不通过
     */
    private Integer examine;

    /**
     * 备注
     */
    private String remark;

    /**
     * 获取分类id
     *
     * @return cid - 分类id
     */
    public Integer getCid()
    {
        return cid;
    }

    /**
     * 设置分类id
     *
     * @param cid 分类id
     */
    public void setCid(Integer cid)
    {
        this.cid = cid;
    }

    /**
     * 获取上级id
     *
     * @return sid - 上级id
     */
    public Integer getSid()
    {
        return sid;
    }

    /**
     * 设置上级id
     *
     * @param sid 上级id
     */
    public void setSid(Integer sid)
    {
        this.sid = sid;
    }

    /**
     * 获取分类名称
     *
     * @return pname - 分类名称
     */
    public String getPname()
    {
        return pname;
    }

    /**
     * 设置分类名称
     *
     * @param pname 分类名称
     */
    public void setPname(String pname)
    {
        this.pname = pname == null ? null : pname.trim();
    }

    /**
     * 获取英文名称
     *
     * @return english_name - 英文名称
     */
    public String getEnglish_name()
    {
        return english_name;
    }

    /**
     * 设置英文名称
     *
     * @param english_name 英文名称
     */
    public void setEnglish_name(String english_name)
    {
        this.english_name = english_name == null ? null : english_name.trim();
    }

    /**
     * 获取分类图片
     *
     * @return img - 分类图片
     */
    public String getImg()
    {
        return img;
    }

    /**
     * 设置分类图片
     *
     * @param img 分类图片
     */
    public void setImg(String img)
    {
        this.img = img == null ? null : img.trim();
    }

    /**
     * 获取小图标
     *
     * @return bg - 小图标
     */
    public String getBg()
    {
        return bg;
    }

    /**
     * 设置小图标
     *
     * @param bg 小图标
     */
    public void setBg(String bg)
    {
        this.bg = bg == null ? null : bg.trim();
    }

    /**
     * 获取级别
     *
     * @return level - 级别
     */
    public Integer getLevel()
    {
        return level;
    }

    /**
     * 设置级别
     *
     * @param level 级别
     */
    public void setLevel(Integer level)
    {
        this.level = level;
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
     * 获取回收站 0.不回收 1.回收
     *
     * @return recycle - 回收站 0.不回收 1.回收
     */
    public Integer getRecycle()
    {
        return recycle;
    }

    /**
     * 设置回收站 0.不回收 1.回收
     *
     * @param recycle 回收站 0.不回收 1.回收
     */
    public void setRecycle(Integer recycle)
    {
        this.recycle = recycle;
    }

    /**
     * 获取是否显示 0.不显示 1.显示
     *
     * @return is_display - 是否显示 0.不显示 1.显示
     */
    public Integer getIs_display()
    {
        return is_display;
    }

    /**
     * 设置是否显示 0.不显示 1.显示
     *
     * @param is_display 是否显示 0.不显示 1.显示
     */
    public void setIs_display(Integer is_display)
    {
        this.is_display = is_display;
    }

    public Integer getSupplier_id()
    {
        return supplier_id;
    }

    public void setSupplier_id(Integer supplier_id)
    {
        this.supplier_id = supplier_id;
    }

    /**
     * 获取审核状态 0.待审核 1.审核通过 2.不通过
     *
     * @return examine - 审核状态 0.待审核 1.审核通过 2.不通过
     */
    public Integer getExamine()
    {
        return examine;
    }

    /**
     * 设置审核状态 0.待审核 1.审核通过 2.不通过
     *
     * @param examine 审核状态 0.待审核 1.审核通过 2.不通过
     */
    public void setExamine(Integer examine)
    {
        this.examine = examine;
    }

    /**
     * 获取备注
     *
     * @return remark - 备注
     */
    public String getRemark()
    {
        return remark;
    }

    /**
     * 设置备注
     *
     * @param remark 备注
     */
    public void setRemark(String remark)
    {
        this.remark = remark == null ? null : remark.trim();
    }
}