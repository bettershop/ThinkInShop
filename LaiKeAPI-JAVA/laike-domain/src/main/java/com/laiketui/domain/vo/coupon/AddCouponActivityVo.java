package com.laiketui.domain.vo.coupon;

import com.laiketui.core.annotation.ParamsMapping;
import com.laiketui.domain.vo.MainVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.math.BigDecimal;

/**
 * 添加/编辑优惠卷参数
 *
 * @author Trick
 * @date 2020/12/9 12:14
 */
@ApiModel(description = "添加/编辑优惠卷参数")
public class AddCouponActivityVo extends MainVo
{

    @ApiModelProperty(value = "优惠卷id", name = "id")
    private Integer id;

    @ApiModelProperty(value = "店铺id", name = "mch_id")
    @ParamsMapping("mch_id")
    private int mchId;

    @ApiModelProperty(value = "活动类型  1=免邮 2=满减 3=折扣 4=会员赠送", name = "activity_type")
    @ParamsMapping("activity_type")
    private int activityType;

    @ApiModelProperty(value = "活动名称", name = "name")
    private String     name;
    @ApiModelProperty(value = "发行数量", name = "circulation")
    private Integer    circulation;
    @ApiModelProperty(value = "优惠卷面值", name = "money")
    private BigDecimal money;
    @ApiModelProperty(value = "折扣值", name = "discount")
    private BigDecimal discount;
    @ApiModelProperty(value = "满减值", name = "z_money")
    @ParamsMapping("z_money")
    private BigDecimal zmoney;
    @ApiModelProperty(value = "优惠卷使用范围 1：全部商品 2:指定商品 3：指定分类", name = "type")
    private int        type;
    @ApiModelProperty(value = "商品id集", name = "menu_list")
    @ParamsMapping("menu_list")
    private String     menuList;
    @ApiModelProperty(value = "商品类别集", name = "class_list")
    @ParamsMapping("class_list")
    private String     classList;
    @ApiModelProperty(value = "活动开始时间", name = "start_time")
    @ParamsMapping("start_time")
    private String     startTime;
    @ApiModelProperty(value = "活动结束时间", name = "end_time")
    @ParamsMapping("end_time")
    private String     endTime;
    @ApiModelProperty(value = "有效时间(设置多久(天)后失效)", name = "day")
    private Integer    day;
    @ApiModelProperty(value = "使用说明", name = "Instructions")
    private String     instructions;
    @ApiModelProperty(value = "领取张数限制", name = "limitCount")
    private Integer    limitCount;
    @ApiModelProperty(value = "发行单位 0=商城 1=自营店 2=店铺", name = "issueUnit")
    private Integer    issueUnit;
    @ApiModelProperty(value = "领取方式 0=手动领取 1=自动发放", name = "receiveType")
    private Integer    receiveType;
    @ApiModelProperty(value = "优惠券图片", name = "image")
    private String     cover_map;

    public String getCover_map()
    {
        return cover_map;
    }

    public void setCover_map(String cover_map)
    {
        this.cover_map = cover_map;
    }

    public Integer getId()
    {
        return id;
    }

    public void setId(Integer id)
    {
        this.id = id;
    }

    public int getMchId()
    {
        return mchId;
    }

    public void setMchId(int mchId)
    {
        this.mchId = mchId;
    }

    public int getActivityType()
    {
        return activityType;
    }

    public void setActivityType(int activityType)
    {
        this.activityType = activityType;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public Integer getCirculation()
    {
        return circulation;
    }

    public void setCirculation(Integer circulation)
    {
        this.circulation = circulation;
        if (circulation == null || circulation == 0)
        {
            this.circulation = 9999999;
        }
    }

    public BigDecimal getMoney()
    {
        return money;
    }

    public void setMoney(BigDecimal money)
    {
        this.money = money;
    }

    public BigDecimal getDiscount()
    {
        return discount;
    }

    public void setDiscount(BigDecimal discount)
    {
        this.discount = discount;
    }

    public BigDecimal getZmoney()
    {
        return zmoney;
    }

    public void setZmoney(BigDecimal zmoney)
    {
        this.zmoney = zmoney;
    }

    public int getType()
    {
        return type;
    }

    public void setType(int type)
    {
        this.type = type;
    }

    public String getMenuList()
    {
        return menuList;
    }

    public void setMenuList(String menuList)
    {
        this.menuList = menuList;
    }

    public String getClassList()
    {
        return classList;
    }

    public void setClassList(String classList)
    {
        this.classList = classList;
    }

    public String getStartTime()
    {
        return startTime;
    }

    public void setStartTime(String startTime)
    {
        this.startTime = startTime;
    }

    public String getEndTime()
    {
        return endTime;
    }

    public void setEndTime(String endTime)
    {
        this.endTime = endTime;
    }

    public Integer getDay()
    {
        return day;
    }

    public void setDay(Integer day)
    {
        this.day = day;
    }

    public String getInstructions()
    {
        return instructions;
    }

    public void setInstructions(String instructions)
    {
        this.instructions = instructions;
    }

    public Integer getLimitCount()
    {
        return limitCount;
    }

    public void setLimitCount(Integer limitCount)
    {
        this.limitCount = limitCount;
    }

    public Integer getIssueUnit()
    {
        return issueUnit;
    }

    public void setIssueUnit(Integer issueUnit)
    {
        this.issueUnit = issueUnit;
    }

    public Integer getReceiveType()
    {
        return receiveType;
    }

    public void setReceiveType(Integer receiveType)
    {
        this.receiveType = receiveType;
    }
}
