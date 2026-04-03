package com.laiketui.domain.vo.mch.pc;

import com.laiketui.domain.vo.MainVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * 添加角色
 *
 * @author Trick
 * @date 2021/12/17 15:44
 */
@ApiModel(description = "添加角色")
public class AddRoleMainVo extends MainVo
{

    @ApiModelProperty(value = "角色id", name = "id")
    private String id;
    @ApiModelProperty(value = "店铺id", name = "mchId")
    private String mchId;

    @ApiModelProperty(value = "角色名称", name = "name")
    private String name;
    @ApiModelProperty(value = "角色描述", name = "describe")
    private String describe;

    @ApiModelProperty(value = "权限id集 ','隔开", name = "roleIdTree")
    private String roleIdTree;

    public String getMchId()
    {
        return mchId;
    }

    public void setMchId(String mchId)
    {
        this.mchId = mchId;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getDescribe()
    {
        return describe;
    }

    public void setDescribe(String describe)
    {
        this.describe = describe;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getRoleIdTree()
    {
        return roleIdTree;
    }

    public void setRoleIdTree(String roleIdTree)
    {
        this.roleIdTree = roleIdTree;
    }
}
