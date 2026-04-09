package com.laiketui.plugins.api.group.vo;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.laiketui.core.utils.tool.StringUtils;
import com.laiketui.domain.vo.MainVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.apache.commons.collections.MapUtils;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 添加拼团活动参数
 *
 * @author Trick
 * @date 2020/11/4 17:27
 */
@ApiModel(description = "添加拼团活动参数")
public class PluginsGroupGroupSaveActivityVo extends MainVo
{
    @ApiModelProperty(value = "主键", name = "id")
    private String                           id;
    @ApiModelProperty(value = "店铺id 默认自营店", name = "mchId", hidden = true)
    private Integer                          mchId;
    @ApiModelProperty(value = "活动名称", name = "name")
    private String                           name;
    @ApiModelProperty(value = "团长参团设置 0=不限制 1=限制", name = "teamSwitch")
    private Integer                          teamSwitch;
    @ApiModelProperty(value = "同一商品参团限制 0=不限制 1=限制", name = "againSwitch")
    private Integer                          againSwitch;
    @ApiModelProperty(value = "是否显示 0=不显示 1=显示", name = "showSwitch")
    private Integer                          showSwitch;
    @ApiModelProperty(value = "商品id集", name = "goodsIds")
    private String                           goodsIds;
    @ApiModelProperty(value = "拼团优惠规则 ", name = "groupRule")
    private Map<Integer, PluginsGroupRuleVo> groupRule;
    @ApiModelProperty(value = "活动开始时间", name = "startDate")
    private String                           startDate;
    @ApiModelProperty(value = "活动结束时间", name = "endDate")
    private String                           endDate;
    @ApiModelProperty(value = "是否自定义 0不是 1是", name = "isCustom")
    private Integer                          isCustom;

    public Integer getIsCustom()
    {
        return isCustom;
    }

    public void setIsCustom(Integer isCustom)
    {
        this.isCustom = isCustom;
    }

    public Map<Integer, PluginsGroupRuleVo> getGroupRule()
    {
        return groupRule;
    }

    public void setGroupRuleVo(Map<Integer, PluginsGroupRuleVo> groupRuleVo)
    {
        this.groupRule = groupRuleVo;
    }

    public void setGroupRule(String groupRule)
    {
        if (StringUtils.isNotEmpty(groupRule))
        {
            //[{"num":"1","canDiscount":"2","openDiscount":"3"},{"num":"4","canDiscount":"5","openDiscount":"6"}]
            List<Map<String, Object>> list = JSON.parseObject(groupRule, new TypeReference<List<Map<String, Object>>>()
            {
            });
            Map<Integer, PluginsGroupRuleVo> dataMap = new HashMap<>(16);
            for (Map<String, Object> map : list)
            {
                PluginsGroupRuleVo groupRuleVo = new PluginsGroupRuleVo();
                groupRuleVo.setCanDiscount(new BigDecimal(MapUtils.getString(map, "canDiscount")));
                groupRuleVo.setOpenDiscount(new BigDecimal(MapUtils.getString(map, "openDiscount")));
                dataMap.put(MapUtils.getIntValue(map, "num"), groupRuleVo);
            }
            //{1:{canDiscount:8,openDiscount:75},2:{canDiscount:7,openDiscount:55},...}
            this.groupRule = dataMap;
        }
    }

    public Integer getTeamSwitch()
    {
        return teamSwitch;
    }

    public void setTeamSwitch(Integer teamSwitch)
    {
        this.teamSwitch = teamSwitch;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public Integer getMchId()
    {
        return mchId;
    }

    public void setMchId(Integer mchId)
    {
        this.mchId = mchId;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public Integer getAgainSwitch()
    {
        return againSwitch;
    }

    public void setAgainSwitch(Integer againSwitch)
    {
        this.againSwitch = againSwitch;
    }

    public Integer getShowSwitch()
    {
        return showSwitch;
    }

    public void setShowSwitch(Integer showSwitch)
    {
        this.showSwitch = showSwitch;
    }

    public String getGoodsIds()
    {
        return goodsIds;
    }

    public void setGoodsIds(String goodsIds)
    {
        this.goodsIds = goodsIds;
    }

    public String getStartDate()
    {
        return startDate;
    }

    public void setStartDate(String startDate)
    {
        this.startDate = startDate;
    }

    public String getEndDate()
    {
        return endDate;
    }

    public void setEndDate(String endDate)
    {
        this.endDate = endDate;
    }
}
