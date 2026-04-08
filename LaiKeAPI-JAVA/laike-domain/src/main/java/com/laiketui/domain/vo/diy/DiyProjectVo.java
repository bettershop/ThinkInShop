package com.laiketui.domain.vo.diy;


import com.laiketui.domain.vo.MainVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "添加/编辑diy模板工程")
public class DiyProjectVo extends MainVo
{

    /**
     * 项目id
     */
    @ApiModelProperty(name = "id", value = "项目id")
    private Integer id;

    /**
     * 管理员id
     */
    @ApiModelProperty(name = "admin_id", value = "管理员id")
    private Integer admin_id;

    /**
     * 项目名称
     */
    @ApiModelProperty(name = "pro_title", value = "项目名称")
    private String pro_title;

    /**
     * 项目logo
     */
    @ApiModelProperty(name = "id", value = "项目id")
    private String logo;

    /**
     * 封面
     */
    @ApiModelProperty(name = "cover", value = "封面")
    private String cover;

    /**
     * 状态 1启用 2不启用
     */
    @ApiModelProperty(name = "is_show", value = "状态 1启用 2不启用")
    private Integer is_show;

    /**
     * 删除  1 是 2 否
     */
    @ApiModelProperty(name = "is_del", value = "删除 1 是 2 否")
    private Integer is_del;

    /**
     * 店铺id
     */
    @ApiModelProperty(name = "mch_id", value = "店铺id")
    private Integer mch_id;

    /**
     * 供应商id
     */
    @ApiModelProperty(name = "sp_id", value = "供应商id")
    private Integer sp_id;

    public Integer getId()
    {
        return id;
    }

    public void setId(Integer id)
    {
        this.id = id;
    }

    public Integer getAdmin_id()
    {
        return admin_id;
    }

    public void setAdmin_id(Integer admin_id)
    {
        this.admin_id = admin_id;
    }

    public String getPro_title()
    {
        return pro_title;
    }

    public void setPro_title(String pro_title)
    {
        this.pro_title = pro_title;
    }

    public String getLogo()
    {
        return logo;
    }

    public void setLogo(String logo)
    {
        this.logo = logo;
    }

    public String getCover()
    {
        return cover;
    }

    public void setCover(String cover)
    {
        this.cover = cover;
    }

    public Integer getIs_show()
    {
        return is_show;
    }

    public void setIs_show(Integer is_show)
    {
        this.is_show = is_show;
    }

    public Integer getIs_del()
    {
        return is_del;
    }

    public void setIs_del(Integer is_del)
    {
        this.is_del = is_del;
    }

    public Integer getMch_id()
    {
        return mch_id;
    }

    public void setMch_id(Integer mch_id)
    {
        this.mch_id = mch_id;
    }

    public Integer getSp_id()
    {
        return sp_id;
    }

    public void setSp_id(Integer sp_id)
    {
        this.sp_id = sp_id;
    }
}
