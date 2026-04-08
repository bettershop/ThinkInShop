package com.laiketui.domain.vo.mch;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.laiketui.core.annotation.ParamsMapping;
import com.laiketui.domain.vo.MainVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 上传商品参数
 *
 * @author Trick
 * @date 2020/11/17 14:48
 */
@ApiModel(description = "商品上传参数")
public class UploadMerchandiseVo extends MainVo
{

    @ApiModelProperty(value = "店铺id", name = "shopId")
    @ParamsMapping("shop_id")
    private int     shopId;
    @ApiModelProperty(value = "商品id(修改时传)", name = "pId")
    @ParamsMapping("p_id")
    private Integer pId;
    @ApiModelProperty(value = "语种id", name = "lang_pid")
    private Integer lang_pid;
    @ApiModelProperty(value = "商品标题", name = "productTitle")
    @ParamsMapping("product_title")
    private String  productTitle;
    @ApiModelProperty(value = "小标题", name = "subtitle")
    private String  subtitle;
    @ApiModelProperty(value = "条形码", name = "scan")
    private String  scan;
    @ApiModelProperty(value = "产品类别", name = "productClassId")
    @ParamsMapping("product_class_id")
    private String  productClassId;
    @ApiModelProperty(value = "品牌", name = "brandId")
    @ParamsMapping("brand_id")
    private String  brandId;
    @ApiModelProperty(value = "关键词", name = "keyword")
    private String  keyword;
    @ApiModelProperty(value = "重量", name = "weight")
    private String  weight;
    @ApiModelProperty(value = "产品图片 多个用,分割", name = "showImg")
    private String  showImg;
    /**
     * cbj=1,yj=998,sj=188,kucun=9999,unit=%E7%AE%B1,stockWarn=99
     */
    @ApiModelProperty(value = "初始值", name = "initial")
    private String  initial;
    /**
     * [{"attr_group_name":"颜色","attr_list":[{"attr_name":"黄色"},{"attr_name":"黑色"}]}]
     * 【没用了】
     */
    @ApiModelProperty(value = "属性/规格", name = "attrGroup")
    @ParamsMapping("attr_group")
    @Deprecated
    private String  attrGroup;
    @ApiModelProperty(value = "属性详细信息", name = "attrArr")
    @ParamsMapping("attr_arr")
    private String  attrArr;
    @ApiModelProperty(value = "运费", name = "freightId", required = true)
    @ParamsMapping("freight_id")
    private Integer freightId;
    @ApiModelProperty(value = "显示位置", name = "displayPosition")
    @ParamsMapping("display_position")
    private String  displayPosition;
    @ApiModelProperty(value = "显示标签集", name = "sType")
    @ParamsMapping("s_type")
    private String  sType;
    /**
     * 支持活动 已经没用了,默认就是正价。
     */
    @ApiModelProperty(value = "支持活动", name = "active")
    @Deprecated
    private Integer active = 1;
    @ApiModelProperty(value = "关联的分销层级id", name = "distributorId")
    @ParamsMapping("distributor_id")
    private Integer distributorId;
    @ApiModelProperty(value = "产品内容", name = "content")
    private String  content;
    @ApiModelProperty(value = "虚拟销量", name = "volume")
    private Integer volume;
    /**
     * [{"tagType":"p","value":"详情","style":"padding:10px;font-size:14px;"}]
     */
    @ApiModelProperty(value = "产品数组内容 (设置前端店铺商品详情插件)", name = "richList")
    private String  richList;
    /**
     * 商品审核状态
     */
    @ApiModelProperty(value = "商品审核状态 1.待审核，2.审核通过，3.审核不通过，4.暂不审核", name = "mchStatus")
    @ParamsMapping("mch_status")
    private Integer mchStatus;
    @ApiModelProperty(value = "单位", name = "unit")
    private String  unit;
    @ApiModelProperty(value = "库存预警", name = "stockWarn")
    private int     stockWarn;
    @ApiModelProperty(value = "产品封面图", name = "coverMap")
    @ParamsMapping("cover_map")
    private String  coverMap;

    @ApiModelProperty(value = "序号", name = "sort")
    private Integer sort;
    @ApiModelProperty(value = "店铺序号", name = "mchSort")
    private Integer mchSort;
    @ApiModelProperty(value = "商品参数", name = "proParam")
    private String  proParam;
    @ApiModelProperty(value = "商品介绍", name = "proIntroduce")
    private String  proIntroduce;
    @ApiModelProperty(value = "收货形式 1.邮寄 2.自提", name = "receivingForm")
    private String  receivingForm;
    @ApiModelProperty(value = "视频", name = "video")
    private String  video;
    @ApiModelProperty(value = "商品视频", name = "proVideo")
    private String  proVideo;


    /**
     * 预售商品参数
     */
    @ApiModelProperty(value = "预售类型   1.定金模式   2.订货模式", name = "sellType")
    private Integer    sellType;
    @ApiModelProperty(value = "定金金额", name = "deposit")
    private BigDecimal deposit;
    @ApiModelProperty(value = "支付定金类型   1.默认    2.自定义", name = "payType")
    private Integer    payType;
    @ApiModelProperty(value = "定金支付开始时间", name = "startTime")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date       startTime;
    @ApiModelProperty(value = "定金支付结束时间", name = "endTime")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date       endTime;
    @ApiModelProperty(value = "尾款支付日期", name = "balancePayTime")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date       balancePayTime;
    @ApiModelProperty(value = "预售数量", name = "sellNum")
    private Integer    sellNum;
    @ApiModelProperty(value = "截止天数", name = "endDay")
    private Integer    endDay;
    @ApiModelProperty(value = "发货时间(天)", name = "deliveryTime")
    private Integer    deliveryTime;

    @ApiModelProperty(value = "第几行【批量上传】", hidden = true, name = "x")
    private Integer x;
    @ApiModelProperty(value = "商品类型 0.实物商品 1.虚拟商品", hidden = true, name = "commodityType")
    @ParamsMapping("commodity_type")
    private Integer commodityType = 0;
    @ApiModelProperty(value = "核销设置 1.线下核销 2.无需核销", hidden = true, name = "WriteOffSettings")
    @ParamsMapping("write_off_settings")
    private Integer writeOffSettings;
    @ApiModelProperty(value = "核销门店id  0全部门店,  1,2,3使用逗号分割", hidden = true, name = "WriteOffMchIds")
    @ParamsMapping("write_off_mch_ids")
    private String  writeOffMchIds;
    @ApiModelProperty(value = "预约时间设置 1.无需预约下单 2.需要预约下单", hidden = true, name = "isAppointment")
    @ParamsMapping("is_appointment")
    private Integer isAppointment;
    @ApiModelProperty("草稿箱id")
    private Integer draftsId;

    @ApiModelProperty("语种编码")
    private String lang_code;

    @ApiModelProperty(value = "移动店铺端编辑供应商商品用的")
    private Integer unitType;


    public Integer getUnitType()
    {
        return unitType;
    }

    public void setUnitType(Integer unitType)
    {
        this.unitType = unitType;
    }

    public Integer getLang_pid()
    {
        return lang_pid;
    }

    public void setLang_pid(Integer lang_pid)
    {
        this.lang_pid = lang_pid;
    }

    public String getLang_code()
    {
        return lang_code;
    }

    public void setLang_code(String lang_code)
    {
        this.lang_code = lang_code;
    }


    public Integer getDraftsId()
    {
        return draftsId;
    }

    public void setDraftsId(Integer draftsId)
    {
        this.draftsId = draftsId;
    }

    public Integer getMchSort()
    {
        return mchSort;
    }

    public void setMchSort(Integer mchSort)
    {
        this.mchSort = mchSort;
    }

    public Integer getX()
    {
        return x;
    }

    public void setX(Integer x)
    {
        this.x = x;
    }

    public String getVideo()
    {
        return video;
    }

    public String getProVideo()
    {
        return proVideo;
    }

    public void setProVideo(String proVideo)
    {
        this.proVideo = proVideo;
    }

    public void setVideo(String video)
    {
        this.video = video;
    }

    public String getReceivingForm()
    {
        return receivingForm;
    }

    public void setReceivingForm(String receivingForm)
    {
        this.receivingForm = receivingForm;
    }

    public Integer getSellType()
    {
        return sellType;
    }

    public void setSellType(Integer sellType)
    {
        this.sellType = sellType;
    }

    public BigDecimal getDeposit()
    {
        return deposit;
    }

    public void setDeposit(BigDecimal deposit)
    {
        this.deposit = deposit;
    }

    public Date getStartTime()
    {
        return startTime;
    }

    public void setStartTime(Date startTime)
    {
        this.startTime = startTime;
    }

    public Date getEndTime()
    {
        return endTime;
    }

    public void setEndTime(Date endTime)
    {
        this.endTime = endTime;
    }

    public Date getBalancePayTime()
    {
        return balancePayTime;
    }

    public void setBalancePayTime(Date balancePayTime)
    {
        this.balancePayTime = balancePayTime;
    }

    public Integer getSellNum()
    {
        return sellNum;
    }

    public void setSellNum(Integer sellNum)
    {
        this.sellNum = sellNum;
    }

    public Integer getEndDay()
    {
        return endDay;
    }

    public void setEndDay(Integer endDay)
    {
        this.endDay = endDay;
    }

    public Integer getDeliveryTime()
    {
        return deliveryTime;
    }

    public void setDeliveryTime(Integer deliveryTime)
    {
        this.deliveryTime = deliveryTime;
    }

    public Integer getPayType()
    {
        return payType;
    }

    public void setPayType(Integer payType)
    {
        this.payType = payType;
    }

    public Integer getSort()
    {
        return sort;
    }

    public void setSort(Integer sort)
    {
        this.sort = sort;
    }

    public Integer getVolume()
    {
        return volume;
    }

    public void setVolume(Integer volume)
    {
        this.volume = volume;
    }

    public int getShopId()
    {
        return shopId;
    }

    public void setShopId(int shopId)
    {
        this.shopId = shopId;
    }

    public String getProductTitle()
    {
        return productTitle;
    }

    public void setProductTitle(String productTitle)
    {
        this.productTitle = productTitle;
    }

    public String getSubtitle()
    {
        return subtitle;
    }

    public void setSubtitle(String subtitle)
    {
        this.subtitle = subtitle;
    }

    public String getScan()
    {
        return scan;
    }

    public void setScan(String scan)
    {
        this.scan = scan;
    }

    public String getProductClassId()
    {
        return productClassId;
    }

    public void setProductClassId(String productClassId)
    {
        this.productClassId = productClassId;
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

    public String getWeight()
    {
        return weight;
    }

    public void setWeight(String weight)
    {
        this.weight = weight;
    }

    public String getShowImg()
    {
        return showImg;
    }

    public void setShowImg(String showImg)
    {
        this.showImg = showImg;
    }

    public String getInitial()
    {
        return initial;
    }

    public void setInitial(String initial)
    {
        this.initial = initial;
    }

    public String getAttrGroup()
    {
        return attrGroup;
    }

    public void setAttrGroup(String attrGroup)
    {
        this.attrGroup = attrGroup;
    }

    public String getAttrArr()
    {
        return attrArr;
    }

    public void setAttrArr(String attrArr)
    {
        this.attrArr = attrArr;
    }

    public Integer getFreightId()
    {
        return freightId;
    }

    public void setFreightId(Integer freightId)
    {
        this.freightId = freightId;
    }

    public String getDisplayPosition()
    {
        return displayPosition;
    }

    public void setDisplayPosition(String displayPosition)
    {
        this.displayPosition = displayPosition;
    }

    public String getsType()
    {
        return sType;
    }

    public void setsType(String sType)
    {
        this.sType = sType;
    }

    public Integer getActive()
    {
        return active;
    }

    public void setActive(Integer active)
    {
        this.active = active;
    }

    public Integer getDistributorId()
    {
        return distributorId;
    }

    public void setDistributorId(Integer distributorId)
    {
        this.distributorId = distributorId;
    }

    public String getContent()
    {
        return content;
    }

    public void setContent(String content)
    {
        this.content = content;
    }

    public String getRichList()
    {
        return richList;
    }

    public void setRichList(String richList)
    {
        this.richList = richList;
    }

    public Integer getMchStatus()
    {
        return mchStatus;
    }

    public void setMchStatus(Integer mchStatus)
    {
        this.mchStatus = mchStatus;
    }

    public String getUnit()
    {
        return unit;
    }

    public void setUnit(String unit)
    {
        this.unit = unit;
    }

    public int getStockWarn()
    {
        return stockWarn;
    }

    public void setStockWarn(int stockWarn)
    {
        this.stockWarn = stockWarn;
    }

    public String getCoverMap()
    {
        return coverMap;
    }

    public void setCoverMap(String coverMap)
    {
        this.coverMap = coverMap;
    }

    public Integer getpId()
    {
        return pId;
    }

    public void setpId(Integer pId)
    {
        this.pId = pId;
    }

    public String getProParam()
    {
        return proParam;
    }

    public void setProParam(String proParam)
    {
        this.proParam = proParam;
    }

    public String getProIntroduce()
    {
        return proIntroduce;
    }

    public void setProIntroduce(String proIntroduce)
    {
        this.proIntroduce = proIntroduce;
    }

    public Integer getCommodityType()
    {
        return commodityType;
    }

    public void setCommodityType(Integer commodityType)
    {
        this.commodityType = commodityType;
    }

    public Integer getWriteOffSettings()
    {
        return writeOffSettings;
    }

    public void setWriteOffSettings(Integer writeOffSettings)
    {
        this.writeOffSettings = writeOffSettings;
    }

    public String getWriteOffMchIds()
    {
        return writeOffMchIds;
    }

    public void setWriteOffMchIds(String writeOffMchIds)
    {
        this.writeOffMchIds = writeOffMchIds;
    }

    public Integer getIsAppointment()
    {
        return isAppointment;
    }

    public void setIsAppointment(Integer isAppointment)
    {
        this.isAppointment = isAppointment;
    }
}
