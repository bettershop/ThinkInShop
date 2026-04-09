package com.laiketui.domain.vo.goods;

import com.laiketui.domain.vo.MainVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * 增加库存
 *
 * @author Trick
 * @date 2021/1/4 14:18
 */
@ApiModel(description = "增加库存")
public class AddStockVo extends MainVo
{
    @ApiModelProperty(value = "属性id", name = "attrId")
    private int id;
    @ApiModelProperty(value = "商品id", name = "pid")
    private int pid;

    @ApiModelProperty(value = "商品ids", name = "pids")
    private String ids;

    @ApiModelProperty(value = "增加库存数量", name = "add_num")
    private int addNum;

    @ApiModelProperty(value = "文本内容", name = "text")
    private String text;

    @ApiModelProperty(value = "是否修改总库存", name = "text")
//    private boolean isUpStockTotal = false;
    private boolean isUpStockTotal = true;

    @ApiModelProperty(value = "店铺id(供应商使用)", name = "mchId")
    private Integer mchId;

    public boolean isUpStockTotal()
    {
        return isUpStockTotal;
    }

    public void setUpStockTotal(boolean upStockTotal)
    {
        isUpStockTotal = upStockTotal;
    }

    public String getText()
    {
        return text;
    }

    public void setText(String text)
    {
        this.text = text;
    }

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public int getPid()
    {
        return pid;
    }

    public void setPid(int pid)
    {
        this.pid = pid;
    }

    public int getAddNum()
    {
        return addNum;
    }

    public void setAddNum(int addNum)
    {
        this.addNum = addNum;
    }

    public Integer getMchId()
    {
        return mchId;
    }

    public void setMchId(Integer mchId)
    {
        this.mchId = mchId;
    }

    public String getIds()
    {
        return ids;
    }

    public void setIds(String ids)
    {
        this.ids = ids;
    }
}
