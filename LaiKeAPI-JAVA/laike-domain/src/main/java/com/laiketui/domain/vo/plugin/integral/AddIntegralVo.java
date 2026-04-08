package com.laiketui.domain.vo.plugin.integral;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.laiketui.domain.vo.MainVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.math.BigDecimal;
import java.util.List;

/**
 * 添加/编辑积分商品
 *
 * @author Trick
 * @date 2021/5/12 11:38
 */
@ApiModel(description = "添加/编辑积分商品")
public class AddIntegralVo extends MainVo
{
    @ApiModelProperty(name = "id", value = "积分商品id")
    private Integer             id;
    @ApiModelProperty(name = "goodsid", value = "商品id")
    private Integer             goodsid;
    @ApiModelProperty(name = "attrId", value = "规格id")
    private Integer             attrId;
    @ApiModelProperty(name = "stockNum", value = "库存数量")
    private Integer             stockNum;
    @ApiModelProperty(name = "maxStockNum", value = "最大库存数量")
    private Integer             maxStockNum;
    @ApiModelProperty(name = "addNum", value = "增发库存数量")
    private Integer             addNum;
    @ApiModelProperty(name = "integral", value = "所需积分")
    private BigDecimal          integral;
    @ApiModelProperty(name = "price", value = "现金")
    private BigDecimal          money = new BigDecimal(0);
    @ApiModelProperty(name = "addIntegralVoList", value = "批量添加积分商品")
    private List<AddIntegralVo> addIntegralVoList;

    public List<AddIntegralVo> getAddIntegralVoList()
    {
        return addIntegralVoList;
    }

    public void setAddIntegralVoList(String addIntegralVoList)
    {
        if (addIntegralVoList != null && !"".equals(addIntegralVoList))
        {
            //[{"goodsid":4976,"attrId":1977,"stockNum":10,"integral":10,"money":12},{"goodsid":4845,"attrId":1777,"stockNum":10,"integral":10,"money":12}]
            List<AddIntegralVo> list = JSON.parseObject(addIntegralVoList, new TypeReference<List<AddIntegralVo>>()
            {
            });
            this.addIntegralVoList = list;
        }
    }

    public Integer getId()
    {
        return id;
    }

    public Integer getStockNum()
    {
        return stockNum;
    }

    public void setStockNum(Integer stockNum)
    {
        this.stockNum = stockNum;
    }

    public Integer getMaxStockNum()
    {
        return maxStockNum;
    }

    public void setMaxStockNum(Integer maxStockNum)
    {
        this.maxStockNum = maxStockNum;
    }

    public void setId(Integer id)
    {
        this.id = id;
    }

    public Integer getGoodsid()
    {
        return goodsid;
    }

    public void setGoodsid(Integer goodsid)
    {
        this.goodsid = goodsid;
    }

    public Integer getAttrId()
    {
        return attrId;
    }

    public void setAttrId(Integer attrId)
    {
        this.attrId = attrId;
    }

    public BigDecimal getIntegral()
    {
        return integral;
    }

    public void setIntegral(BigDecimal integral)
    {
        this.integral = integral;
    }

    public BigDecimal getMoney()
    {
        return money;
    }

    public void setMoney(BigDecimal money)
    {
        this.money = money;
    }

    public Integer getAddNum()
    {
        return addNum;
    }

    public void setAddNum(Integer addNum)
    {
        this.addNum = addNum;
    }
}
