package com.laiketui.domain.vo.config;

import com.laiketui.domain.vo.MainVo;
import io.swagger.annotations.ApiModelProperty;

/**
 * 获取快递公司子表列表
 */
public class GetExpressSubtableListVo extends MainVo
{

    @ApiModelProperty(name = "id", value = "ID")
    private Integer id;

    @ApiModelProperty(name = "mch_id", value = "店铺ID")
    private Integer mch_id;

    @ApiModelProperty(name = "name", value = "物流公司/编码")
    private String name;

    public Integer getId()
    {
        return id;
    }

    public void setId(Integer id)
    {
        this.id = id;
    }

    public Integer getMch_id()
    {
        return mch_id;
    }

    public void setMch_id(Integer mch_id)
    {
        this.mch_id = mch_id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }
}
