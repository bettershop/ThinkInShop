package com.laiketui.domain.vo.user;

import com.laiketui.core.annotation.ParamsMapping;
import com.laiketui.domain.vo.MainVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * 搜索参数
 *
 * @author Trick
 * @date 2021/6/18 11:01
 */
@ApiModel(description = "搜索参数")
public class SearchVo extends MainVo
{
    @ApiModelProperty(value = "主目录Id", name = "cid")
    private Integer cid;
    @ApiModelProperty(value = "分配目录id集", name = "class_id")
    @ParamsMapping("class_id")
    private String  classId;
    @ApiModelProperty(value = "品牌id集", name = "brandId")
    @ParamsMapping("brand_id")
    private String  brandId;
    @ApiModelProperty(value = "关键字", name = "keyword")
    private String  keyword;
    @ApiModelProperty(value = "金额", name = "amount")
    private String  amount;
    @ApiModelProperty(value = "1=降序 0=升序", name = "sort")
    private Integer sort;
    @ApiModelProperty(value = "排序类型(price 或 volume)", name = "sort_type")
    @ParamsMapping("sort_type")
    private String  sortType;
    @ApiModelProperty(value = "搜索类型 1=商品 2=店铺", name = "search_type")
    @ParamsMapping("search_type")
    private Integer searchType;
    @ApiModelProperty(value = "品牌首字母", name = "brandHead")
    private String  brandHead;
    @ApiModelProperty(value = "分类首字母", name = "classHead")
    private String  classHead;

    public String getClassHead()
    {
        return classHead;
    }

    public void setClassHead(String classHead)
    {
        this.classHead = classHead;
    }

    public String getBrandHead()
    {
        return brandHead;
    }

    public void setBrandHead(String brandHead)
    {
        this.brandHead = brandHead;
    }

    public Integer getSearchType()
    {
        return searchType;
    }

    public void setSearchType(Integer searchType)
    {
        this.searchType = searchType;
    }

    public Integer getCid()
    {
        return cid;
    }

    public void setCid(Integer cid)
    {
        this.cid = cid;
    }

    public String getClassId()
    {
        return classId;
    }

    public void setClassId(String classId)
    {
        this.classId = classId;
    }

    public String getBrandId()
    {
        return brandId;
    }

    public void setBrandId(String brandId)
    {
        this.brandId = brandId;
    }

    public String getKeyword()
    {
        return keyword;
    }

    public void setKeyword(String keyword)
    {
        this.keyword = keyword;
    }

    public String getAmount()
    {
        return amount;
    }

    public void setAmount(String amount)
    {
        this.amount = amount;
    }

    public Integer getSort()
    {
        return sort;
    }

    public void setSort(Integer sort)
    {
        if (sort == null)
        {
            sort = 0;
        }
        this.sort = sort;
    }

    public String getSortType()
    {
        return sortType;
    }

    public void setSortType(String sortType)
    {
        this.sortType = sortType;
    }
}
