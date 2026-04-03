package com.laiketui.domain.vo.order;

import com.laiketui.core.annotation.ParamsMapping;
import com.laiketui.domain.vo.MainVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.servlet.http.HttpServletResponse;

/**
 * 订单列表参数-商户后台管理
 *
 * @author Trick
 * @date 2021/7/19 14:35
 */
@ApiModel(description = "订单查询列表参数")
public class AdminOrderListVo extends MainVo
{

    @ApiModelProperty(value = "订单id", name = "id")
    private Integer id;
    @ApiModelProperty(value = "订单号", name = "orderNo")
    private String  orderNo;

    @ApiModelProperty(value = "关键字", name = "keyWord")
    private String  keyWord;
    @ApiModelProperty(value = "店铺名称", name = "mchName")
    private String  mchName;
    @ApiModelProperty(value = "订单状态", name = "status")
    private Integer status;
    @ApiModelProperty(value = "特殊订单分类 1=实物订单 2=自提订单(GM) 3=虚拟订单 4=活动订单 ,7=积分,8=秒杀, 9=,10=分销订单 11自提订单(GM + MS) 12:直播订单", name = "selfLifting", hidden = true)
    private Integer selfLifting;
    @ApiModelProperty(value = "插件订单是否为自提订单 0=实物订单 1=自提订单 ,", name = "selfLifting", hidden = true)
    private Integer pluginSelfLifting;
    @ApiModelProperty(value = "下单类型", name = "operationType")
    private Integer operationType;
    @ApiModelProperty(value = "开始日期", name = "startDate")
    private String  startDate;
    @ApiModelProperty(value = "结束日期", name = "endDate")
    private String  endDate;
    @ApiModelProperty(value = "店铺id", name = "mchId", hidden = true)
    private Integer mchId;
    @ApiModelProperty(value = "供应商id", name = "supplierId")
    private Integer supplierId;
    @ApiModelProperty(value = "操作者", name = "operator")
    private String  operator;
    @ApiModelProperty(value = "门店管理员id", name = "operator")
    private Integer mchAdminId;
    @ApiModelProperty(value = "门店id", name = "operator")
    private Integer MchStoreId;
    @ApiModelProperty("来源 1：app")
    private Integer source;
    @ApiModelProperty(value = "查询类型 默认全部 return=退款,payment=未付款,send=待发货, Receive=待收货", name = "orderType")
    @ParamsMapping("order_type")
    private String  orderType;
    @ApiModelProperty(value = "用户id", name = "userId")
    @ParamsMapping({"userId", "user_id"})
    private String userId;
    @ApiModelProperty(value = "售后类型", name = "reType")
    @ParamsMapping({"reType", "re_type", "retype"})
    private Integer reType;

    @ApiModelProperty(value = "订单状态", name = "rType")
    @ParamsMapping({"rType", "r_type", "rtype"})
    private Integer rType;

    public Integer getReType() {
        return reType;
    }

    public void setReType(Integer reType) {
        this.reType = reType;
    }

    public Integer getrType() {
        return rType;
    }

    public void setrType(Integer rType) {
        this.rType = rType;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getOrderType()
    {
        return orderType;
    }

    public void setOrderType(String orderType)
    {
        this.orderType = orderType;
    }

    public String getOperator()
    {
        return operator;
    }

    public void setOperator(String operator)
    {
        this.operator = operator;
    }

    public Integer getMchId()
    {
        return mchId;
    }

    public void setMchId(Integer mchId)
    {
        this.mchId = mchId;
    }

    @ApiModelProperty(value = "http", name = "http", hidden = true)
    private HttpServletResponse httpServletResponse;

    public HttpServletResponse getHttpServletResponse()
    {
        return httpServletResponse;
    }

    public void setHttpServletResponse(HttpServletResponse httpServletResponse)
    {
        this.httpServletResponse = httpServletResponse;
    }

    public Integer getId()
    {
        return id;
    }

    public void setId(Integer id)
    {
        this.id = id;
    }

    public String getOrderNo()
    {
        return orderNo;
    }

    public void setOrderNo(String orderNo)
    {
        this.orderNo = orderNo;
    }

    public String getKeyWord()
    {
        return keyWord;
    }

    public void setKeyWord(String keyWord)
    {
        this.keyWord = keyWord;
    }

    public String getMchName()
    {
        return mchName;
    }

    public void setMchName(String mchName)
    {
        this.mchName = mchName;
    }

    public Integer getStatus()
    {
        return status;
    }

    public void setStatus(Integer status)
    {
        this.status = status;
    }

    public Integer getSelfLifting()
    {
        return selfLifting;
    }

    public void setSelfLifting(Integer selfLifting)
    {
        this.selfLifting = selfLifting;
    }

    public Integer getOperationType()
    {
        return operationType;
    }

    public void setOperationType(Integer operationType)
    {
        this.operationType = operationType;
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

    public Integer getSupplierId()
    {
        return supplierId;
    }

    public void setSupplierId(Integer supplierId)
    {
        this.supplierId = supplierId;
    }

    public Integer getMchAdminId()
    {
        return mchAdminId;
    }

    public void setMchAdminId(Integer mchAdminId)
    {
        this.mchAdminId = mchAdminId;
    }

    public Integer getMchStoreId()
    {
        return MchStoreId;
    }

    public void setMchStoreId(Integer mchStoreId)
    {
        MchStoreId = mchStoreId;
    }

    public Integer getPluginSelfLifting()
    {
        return pluginSelfLifting;
    }

    public void setPluginSelfLifting(Integer pluginSelfLifting)
    {
        this.pluginSelfLifting = pluginSelfLifting;
    }

    public Integer getSource()
    {
        return source;
    }

    public void setSource(Integer source)
    {
        this.source = source;
    }
}
