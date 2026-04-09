package com.laiketui.domain.vo.pay;

import com.laiketui.core.annotation.ParamsMapping;
import com.laiketui.domain.vo.MainVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.math.BigDecimal;

/**
 * 支付
 *
 * @author wangxain
 */
@ApiModel(description = "支付")
public class PaymentVo extends MainVo
{

    @ParamsMapping("type")
    private String     payType;
    /**
     * 支付金额
     */
    private String     payment_money;
    /**
     * 地址id
     */
    private String     address_id;
    /**
     * 备注
     */
    private String     remarks;
    /**
     * 订单列表
     */
    private String     order_list;
    /**
     * 等级
     */
    @ParamsMapping("grade_l")
    private int        grade;
    /**
     * 订单ID
     */
    private int   order_id;
    /**
     * 订单号
     */
    @ParamsMapping("order_no")
    private String     sNo;
    /**
     * 吊起第三方支付使用订单号
     */
    private String     real_sno;
    /**
     * 参数 这个参数用于内部api传参调用-
     */
    private String     parameter;
    /**
     * 支付类型
     */
    private String     type;
    /**
     * JSAPI code
     */
    private String     code;
    /**
     * 支付标题 商品名
     */
    private String     title;
    /**
     * jsapi 支付刷新界面
     */
    private String     url;
    /**
     * 充值金额
     */
    private BigDecimal total;

    /**
     * 支付密码
     */
    private String password;

    /**
     * 支付目标
     */
    @ApiModelProperty(value = "支付目标 1.定金  2.尾款  3.全款", name = "payTarget")
    private Integer payTarget;

    /**
     * 支付宝code(支付宝小程序用)
     */
    @ApiModelProperty(value = "支付宝code(支付宝小程序用)", name = "alimp_authcode")
    private String alimp_authcode;
    /**
     * 支付用户id
     */
    private String userId;

    /**
     * 支付用户openid
     */
    private String openid;

    /**
     * 货币编码：支付和退款用
     */
    private String currency_code;

    /**
     * 货币符号
     */
    private String currency_symbol;

    /**
     * 汇率：支付和退款用、界面展示计算用
     */
    private BigDecimal exchange_rate;

    public String getCurrency_code()
    {
        return currency_code;
    }

    public void setCurrency_code(String currency_code)
    {
        this.currency_code = currency_code;
    }

    public String getCurrency_symbol()
    {
        return currency_symbol;
    }

    public void setCurrency_symbol(String currency_symbol)
    {
        this.currency_symbol = currency_symbol;
    }

    public BigDecimal getExchange_rate()
    {
        return exchange_rate;
    }

    public void setExchange_rate(BigDecimal exchange_rate)
    {
        this.exchange_rate = exchange_rate;
    }

    public String getUserId()
    {
        return userId;
    }

    public void setUserId(String userId)
    {
        this.userId = userId;
    }

    public Integer getPayTarget()
    {
        return payTarget;
    }

    public void setPayTarget(Integer payTarget)
    {
        this.payTarget = payTarget;
    }

    public BigDecimal getTotal()
    {
        return total;
    }

    public void setTotal(BigDecimal total)
    {
        this.total = total;
    }

    public String getUrl()
    {
        return url;
    }

    public void setUrl(String url)
    {
        this.url = url;
    }

    public String getCode()
    {
        return code;
    }

    public void setCode(String code)
    {
        this.code = code;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public int getOrder_id()
    {
        return order_id;
    }

    public void setOrder_id(int order_id)
    {
        this.order_id = order_id;
    }

    public String getsNo()
    {
        return sNo;
    }

    public void setsNo(String sNo)
    {
        this.sNo = sNo;
    }

    public String getParameter()
    {
        return parameter;
    }

    public void setParameter(String parameter)
    {
        this.parameter = parameter;
    }

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public int getGrade()
    {
        return grade;
    }

    public void setGrade(int grade)
    {
        this.grade = grade;
    }

    public String getPayType()
    {
        return payType;
    }

    public void setPayType(String payType)
    {
        this.payType = payType;
    }

    public String getPayment_money()
    {
        return payment_money;
    }

    public void setPayment_money(String payment_money)
    {
        this.payment_money = payment_money;
    }

    public String getAddress_id()
    {
        return address_id;
    }

    public void setAddress_id(String address_id)
    {
        this.address_id = address_id;
    }

    public String getRemarks()
    {
        return remarks;
    }

    public void setRemarks(String remarks)
    {
        this.remarks = remarks;
    }

    public String getOrder_list()
    {
        return order_list;
    }

    public void setOrder_list(String order_list)
    {
        this.order_list = order_list;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public String getAlimp_authcode()
    {
        return alimp_authcode;
    }

    public void setAlimp_authcode(String alimp_authcode)
    {
        this.alimp_authcode = alimp_authcode;
    }

    public String getReal_sno()
    {
        return real_sno;
    }

    public void setReal_sno(String real_sno)
    {
        this.real_sno = real_sno;
    }

    public String getOpenid()
    {
        return openid;
    }

    public void setOpenid(String openid)
    {
        this.openid = openid;
    }

    @Override
    public String toString()
    {
        return "PaymentVo{" +
                "payType='" + payType + '\'' +
                ", payment_money='" + payment_money + '\'' +
                ", address_id='" + address_id + '\'' +
                ", remarks='" + remarks + '\'' +
                ", order_list='" + order_list + '\'' +
                ", grade=" + grade +
                ", order_id=" + order_id +
                ", sNo='" + sNo + '\'' +
                ", real_sno='" + real_sno + '\'' +
                ", parameter='" + parameter + '\'' +
                ", type='" + type + '\'' +
                ", code='" + code + '\'' +
                ", title='" + title + '\'' +
                ", url='" + url + '\'' +
                ", total=" + total +
                ", password='" + password + '\'' +
                ", payTarget=" + payTarget +
                ", alimp_authcode='" + alimp_authcode + '\'' +
                ", userId='" + userId + '\'' +
                ", openid='" + openid + '\'' +
                ", currency_code='" + currency_code + '\'' +
                ", currency_symbol='" + currency_symbol + '\'' +
                ", exchange_rate=" + exchange_rate +
                '}';
    }
}
