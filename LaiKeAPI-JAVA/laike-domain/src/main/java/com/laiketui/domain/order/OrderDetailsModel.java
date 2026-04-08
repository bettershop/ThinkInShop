package com.laiketui.domain.order;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Table(name = "lkt_order_details")
public class OrderDetailsModel implements Serializable
{


    /**
     * 未结算
     */
    @Transient
    public final static Integer SETTLEMENT_TYPE_UNSETTLED = 0;

    /**
     * 已结算
     */
    @Transient
    public final static Integer SETTLEMENT_TYPE_SETTLED = 1;

    /**
     * 显示
     */
    @Transient
    public final static int SHOW = 1;

    /**
     * 删除
     */
    @Transient
    public final static int DELETE = 2;

    /**
     * id
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Transient
    private String imgUrl;

    @Transient
    private String mchName;

    @Transient
    private String logo;

    /**
     * 商城id
     */
    private Integer store_id;

    /**
     * 用户id
     */
    private String user_id;

    /**
     * 产品id
     */
    private Integer p_id;

    /**
     * 是否加购商品 0：否 1：是
     */
    private Integer is_addp;


    /**
     * 实际支付金额
     */
    @Column(name = "actual_total")
    private BigDecimal actualTotal;

    /**
     * 积分支付比例抵扣
     */
    @Column(name = "score_deduction")
    private Integer scoreDeduction;

    /**
     * 产品名称
     */
    private String p_name;

    /**
     * 物流信息
     */
    @Column(name = "logistics")
    private String logistics;

    /**
     * 产品价格
     */
    private BigDecimal p_price;

    /**
     * 数量
     */
    private Integer num;

    /**
     * 单位
     */
    private String unit;

    /**
     * 订单号
     */
    @Column(name = "r_sNo")
    private String r_sNo;

    /**
     * 添加时间
     */
    private Date add_time;

    /**
     * 发货时间
     */
    private Date deliver_time;

    /**
     * 到货时间
     */
    private Date arrive_time;

    /**
     * 状态 0:未付款 1:未发货 2:待收货 5:已完成  7:订单关闭
     */
    private Integer r_status;

    /**
     * 退货原因
     */
    private String content;

    /**
     * app退款原因
     */
    private String reason;

    /**
     * 退款类型 1:退货退款  2:退款 3:售后
     */
    private Integer re_type;
    /**
     * 商家自配信息
     */
    @Column(name = "store_self_delivery")
    private String  store_self_delivery;

    /**
     * 用户申请退款金额
     */
    private BigDecimal re_apply_money;

    /**
     * 退款金额
     */
    private BigDecimal re_money;

    /**
     * 实际退款金额
     */
    private BigDecimal real_money;

    /**
     * 申请退款时间
     */
    private Date re_time;

    /**
     * 上传凭证
     */
    private String re_photo;

    /**
     * 审核时间
     */
    private Date audit_time;

    /**
     * 拒绝原因
     */
    private String r_content;

    /**
     * 类型 100:不在退货退款状态0:审核中 1:同意并让用户寄回 2:拒绝(退货退款) 3:用户已快递 4:收到寄回商品,同意并退款 5：拒绝并退回商品 8:拒绝(退款) 9:同意并退款 10:拒绝(售后)11:同意并且寄回商品 12售后结束
     */
    private Integer r_type;

    /**
     * 快递公司id ,号分隔
     */
    private String express_id;

    /**
     * 快递单号  ,号分隔
     */
    private String courier_num;

    /**
     * 发货类型 1.手动发货 2.自动发货
     */
    private Integer express_type;

    /**
     * 运费
     */
    private BigDecimal freight;

    /**
     * 配置名称
     */
    private String size;

    /**
     * 规格属性id
     */
    private String sid;

    /**
     * 发货单打印 0.否 1.是
     */
    private Integer invoice;

    /**
     * 快递单打印 0.否 1.是
     */
    private Integer express;

    /**
     * 换货次数
     */
    private Integer exchange_num;

    /**
     * 优惠券id
     */
    private String coupon_id;

    /**
     * 优惠后的金额
     */
    private BigDecimal after_discount;

    /**
     * 订单结算标识 0未结算 1已结算
     */
    private Integer settlement_type;

    /**
     * 回收站 0.显示 1.回收
     */
    private Integer recycle;

    /**
     * 代课下单手动优惠金额
     */
    @Column(name = "manual_offer")
    private BigDecimal manual_offer;

    /**
     * 总供货价
     */
    private BigDecimal supplier_settlement;

    /**
     * 用户删除 1.显示 2.删除
     */
    private Integer userRecycle;

    /**
     * 商户删除 1.显示 2.删除
     */
    private Integer mchRecycle;

    /**
     * 商户id
     */
    @Column(name = "mch_id")
    private Integer mch_id;

    /**
     * 商城删除 1.显示 2.删除
     */
    private Integer storeRecycle;

    /**
     * 商品发货数量
     */
    private Integer deliverNum;

    /**
     * 直播间ID
     */
    private String living_room_id;

    /**
     * 主播id
     */
    private String anchor_id;

    /**
     * 佣金
     */
    private BigDecimal commission;
    /**
     * 平台优惠金额
     */
    private BigDecimal platform_coupon_price;
    /**
     * 店铺优惠金额
     */
    private BigDecimal store_coupon_price;
    /**
     * 虚拟商品待核销次数
     */
    private Integer    write_off_num;
    /**
     * 虚拟商品已核销次数
     */
    private Integer    after_write_off_num;
    /**
     * 预约门店id
     */
    private Integer    mch_store_write_id;
    /**
     * 虚拟商品预约时间
     */
    private String     write_time;
    /**
     * 虚拟商品预约时间段id
     */
    private Integer    write_time_id;

    /**
     * 产品积分
     */
    private Integer p_integral;

    public String getLogistics() {
        return logistics;
    }

    public void setLogistics(String logistics) {
        this.logistics = logistics;
    }

    public Integer getP_integral()
    {
        return p_integral;
    }

    public void setP_integral(Integer p_integral)
    {
        this.p_integral = p_integral;
    }

    public BigDecimal getActualTotal()
    {
        return actualTotal;
    }

    public void setActualTotal(BigDecimal actualTotal)
    {
        this.actualTotal = actualTotal;
    }

    public Integer getScoreDeduction()
    {
        return scoreDeduction;
    }

    public void setScoreDeduction(Integer scoreDeduction)
    {
        this.scoreDeduction = scoreDeduction;
    }

    public String getMchName()
    {
        return mchName;
    }

    public void setMchName(String mchName)
    {
        this.mchName = mchName;
    }

    public String getLogo()
    {
        return logo;
    }

    public void setLogo(String logo)
    {
        this.logo = logo;
    }

    public String getImgUrl()
    {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl)
    {
        this.imgUrl = imgUrl;
    }

    public Integer getWrite_time_id()
    {
        return write_time_id;
    }

    public BigDecimal getPlatform_coupon_price()
    {
        return platform_coupon_price;
    }

    public void setPlatform_coupon_price(BigDecimal platform_coupon_price)
    {
        this.platform_coupon_price = platform_coupon_price;
    }

    public Integer getMch_id()
    {
        return mch_id;
    }

    public void setMch_id(Integer mch_id)
    {
        this.mch_id = mch_id;
    }

    public BigDecimal getStore_coupon_price()
    {
        return store_coupon_price;
    }

    public void setStore_coupon_price(BigDecimal store_coupon_price)
    {
        this.store_coupon_price = store_coupon_price;
    }

    public void setWrite_time_id(Integer write_time_id)
    {
        this.write_time_id = write_time_id;
    }

    public Integer getWrite_off_num()
    {
        return write_off_num;
    }

    public void setWrite_off_num(Integer write_off_num)
    {
        this.write_off_num = write_off_num;
    }

    public Integer getAfter_write_off_num()
    {
        return after_write_off_num;
    }

    public void setAfter_write_off_num(Integer after_write_off_num)
    {
        this.after_write_off_num = after_write_off_num;
    }

    public Integer getMch_store_write_id()
    {
        return mch_store_write_id;
    }

    public void setMch_store_write_id(Integer mch_store_write_id)
    {
        this.mch_store_write_id = mch_store_write_id;
    }

    public String getWrite_time()
    {
        return write_time;
    }

    public void setWrite_time(String write_time)
    {
        this.write_time = write_time;
    }

    public String getLiving_room_id()
    {
        return living_room_id;
    }

    public void setLiving_room_id(String living_room_id)
    {
        this.living_room_id = living_room_id;
    }

    public String getAnchor_id()
    {
        return anchor_id;
    }

    public void setAnchor_id(String anchor_id)
    {
        this.anchor_id = anchor_id;
    }

    public BigDecimal getCommission()
    {
        return commission;
    }

    public void setCommission(BigDecimal commission)
    {
        this.commission = commission;
    }

    public Integer getDeliverNum()
    {
        return deliverNum;
    }

    public void setDeliverNum(Integer deliverNum)
    {
        this.deliverNum = deliverNum;
    }

    public BigDecimal getSupplier_settlement()
    {
        return supplier_settlement;
    }

    public void setSupplier_settlement(BigDecimal supplier_settlement)
    {
        this.supplier_settlement = supplier_settlement;
    }
    public Integer getIs_addp() {
        return is_addp;
    }

    public void setIs_addp(Integer is_addp) {
        this.is_addp = is_addp;
    }
    public BigDecimal getManual_offer()
    {
        return manual_offer;
    }

    public void setManual_offer(BigDecimal manual_offer)
    {
        this.manual_offer = manual_offer;
    }

    /**
     * 获取id
     *
     * @return id - id
     */
    public Integer getId()
    {
        return id;
    }

    /**
     * 设置id
     *
     * @param id id
     */
    public void setId(Integer id)
    {
        this.id = id;
    }

    /**
     * 获取商城id
     *
     * @return store_id - 商城id
     */
    public Integer getStore_id()
    {
        return store_id;
    }

    /**
     * 设置商城id
     *
     * @param store_id 商城id
     */
    public void setStore_id(Integer store_id)
    {
        this.store_id = store_id;
    }

    /**
     * 获取用户id
     *
     * @return user_id - 用户id
     */
    public String getUser_id()
    {
        return user_id;
    }

    /**
     * 设置用户id
     *
     * @param user_id 用户id
     */
    public void setUser_id(String user_id)
    {
        this.user_id = user_id == null ? null : user_id.trim();
    }

    /**
     * 获取产品id
     * 【如果是插件 则是插件对应的商品id 如果不是插件则是商品id
     * 获取商品信息尽量用 sid】
     *
     * @return p_id - 产品id
     */
    public Integer getP_id()
    {
        return p_id;
    }

    /**
     * 设置产品id
     *
     * @param p_id 产品id
     */
    public void setP_id(Integer p_id)
    {
        this.p_id = p_id;
    }

    /**
     * 获取产品名称
     *
     * @return p_name - 产品名称
     */
    public String getP_name()
    {
        return p_name;
    }

    /**
     * 设置产品名称
     *
     * @param p_name 产品名称
     */
    public void setP_name(String p_name)
    {
        this.p_name = p_name == null ? null : p_name.trim();
    }

    /**
     * 获取产品价格
     *
     * @return p_price - 产品价格
     */
    public BigDecimal getP_price()
    {
        return p_price;
    }

    /**
     * 设置产品价格
     *
     * @param p_price 产品价格
     */
    public void setP_price(BigDecimal p_price)
    {
        this.p_price = p_price;
    }

    /**
     * 获取数量
     *
     * @return num - 数量
     */
    public Integer getNum()
    {
        return num;
    }

    /**
     * 设置数量
     *
     * @param num 数量
     */
    public void setNum(Integer num)
    {
        this.num = num;
    }

    /**
     * 获取单位
     *
     * @return unit - 单位
     */
    public String getUnit()
    {
        return unit;
    }

    /**
     * 设置单位
     *
     * @param unit 单位
     */
    public void setUnit(String unit)
    {
        this.unit = unit == null ? null : unit.trim();
    }

    /**
     * 获取订单号
     *
     * @return r_sNo - 订单号
     */
    public String getR_sNo()
    {
        return r_sNo;
    }

    /**
     * 设置订单号
     *
     * @param r_sNo 订单号
     */
    public void setR_sNo(String r_sNo)
    {
        this.r_sNo = r_sNo == null ? null : r_sNo.trim();
    }

    /**
     * 获取添加时间
     *
     * @return add_time - 添加时间
     */
    public Date getAdd_time()
    {
        return add_time;
    }

    /**
     * 设置添加时间
     *
     * @param add_time 添加时间
     */
    public void setAdd_time(Date add_time)
    {
        this.add_time = add_time;
    }

    /**
     * 获取发货时间
     *
     * @return deliver_time - 发货时间
     */
    public Date getDeliver_time()
    {
        return deliver_time;
    }

    /**
     * 设置发货时间
     *
     * @param deliver_time 发货时间
     */
    public void setDeliver_time(Date deliver_time)
    {
        this.deliver_time = deliver_time;
    }

    /**
     * 获取到货时间
     *
     * @return arrive_time - 到货时间
     */
    public Date getArrive_time()
    {
        return arrive_time;
    }

    /**
     * 设置到货时间
     *
     * @param arrive_time 到货时间
     */
    public void setArrive_time(Date arrive_time)
    {
        this.arrive_time = arrive_time;
    }

    /**
     * 获取状态 0:未付款 1:未发货 2:待收货 5:已完成  7:订单关闭
     *
     * @return r_status - 状态 0:未付款 1:未发货 2:待收货 5:已完成  7:订单关闭
     */
    public Integer getR_status()
    {
        return r_status;
    }

    /**
     * 设置状态 0:未付款 1:未发货 2:待收货 5:已完成  7:订单关闭
     *
     * @param r_status 状态 0:未付款 1:未发货 2:待收货 5:已完成  7:订单关闭
     */
    public void setR_status(Integer r_status)
    {
        this.r_status = r_status;
    }

    /**
     * 获取退货原因
     *
     * @return content - 退货原因
     */
    public String getContent()
    {
        return content;
    }

    /**
     * 设置退货原因
     *
     * @param content 退货原因
     */
    public void setContent(String content)
    {
        this.content = content == null ? null : content.trim();
    }

    /**
     * 获取app退款原因
     *
     * @return reason - app退款原因
     */
    public String getReason()
    {
        return reason;
    }

    /**
     * 设置app退款原因
     *
     * @param reason app退款原因
     */
    public void setReason(String reason)
    {
        this.reason = reason == null ? null : reason.trim();
    }

    /**
     * 获取退款类型 1:退货退款  2:退款 3:售后
     *
     * @return re_type - 退款类型 1:退货退款  2:退款 3:售后
     */
    public Integer getRe_type()
    {
        return re_type;
    }

    /**
     * 设置退款类型 1:退货退款  2:退款 3:售后
     *
     * @param re_type 退款类型 1:退货退款  2:退款 3:售后
     */
    public void setRe_type(Integer re_type)
    {
        this.re_type = re_type;
    }

    /**
     * 获取用户申请退款金额
     *
     * @return re_apply_money - 用户申请退款金额
     */
    public BigDecimal getRe_apply_money()
    {
        return re_apply_money;
    }

    /**
     * 设置用户申请退款金额
     *
     * @param re_apply_money 用户申请退款金额
     */
    public void setRe_apply_money(BigDecimal re_apply_money)
    {
        this.re_apply_money = re_apply_money;
    }

    /**
     * 获取退款金额
     *
     * @return re_money - 退款金额
     */
    public BigDecimal getRe_money()
    {
        return re_money;
    }

    /**
     * 设置退款金额
     *
     * @param re_money 退款金额
     */
    public void setRe_money(BigDecimal re_money)
    {
        this.re_money = re_money;
    }

    /**
     * 获取实际退款金额
     *
     * @return real_money - 实际退款金额
     */
    public BigDecimal getReal_money()
    {
        return real_money;
    }

    /**
     * 设置实际退款金额
     *
     * @param real_money 实际退款金额
     */
    public void setReal_money(BigDecimal real_money)
    {
        this.real_money = real_money;
    }

    /**
     * 获取申请退款时间
     *
     * @return re_time - 申请退款时间
     */
    public Date getRe_time()
    {
        return re_time;
    }

    /**
     * 设置申请退款时间
     *
     * @param re_time 申请退款时间
     */
    public void setRe_time(Date re_time)
    {
        this.re_time = re_time;
    }

    /**
     * 获取上传凭证
     *
     * @return re_photo - 上传凭证
     */
    public String getRe_photo()
    {
        return re_photo;
    }

    /**
     * 设置上传凭证
     *
     * @param re_photo 上传凭证
     */
    public void setRe_photo(String re_photo)
    {
        this.re_photo = re_photo == null ? null : re_photo.trim();
    }

    /**
     * 获取审核时间
     *
     * @return audit_time - 审核时间
     */
    public Date getAudit_time()
    {
        return audit_time;
    }

    /**
     * 设置审核时间
     *
     * @param audit_time 审核时间
     */
    public void setAudit_time(Date audit_time)
    {
        this.audit_time = audit_time;
    }

    /**
     * 获取拒绝原因
     *
     * @return r_content - 拒绝原因
     */
    public String getR_content()
    {
        return r_content;
    }

    /**
     * 设置拒绝原因
     *
     * @param r_content 拒绝原因
     */
    public void setR_content(String r_content)
    {
        this.r_content = r_content == null ? null : r_content.trim();
    }

    /**
     * 获取类型 100:不在退货退款状态0:审核中 1:同意并让用户寄回 2:拒绝(退货退款) 3:用户已快递 4:收到寄回商品,同意并退款 5：拒绝并退回商品 8:拒绝(退款) 9:同意并退款 10:拒绝(售后)11:同意并且寄回商品 12售后结束
     *
     * @return r_type - 类型 100:不在退货退款状态0:审核中 1:同意并让用户寄回 2:拒绝(退货退款) 3:用户已快递 4:收到寄回商品,同意并退款 5：拒绝并退回商品 8:拒绝(退款) 9:同意并退款 10:拒绝(售后)11:同意并且寄回商品 12售后结束
     */
    public Integer getR_type()
    {
        return r_type;
    }

    /**
     * 设置类型 100:不在退货退款状态0:审核中 1:同意并让用户寄回 2:拒绝(退货退款) 3:用户已快递 4:收到寄回商品,同意并退款 5：拒绝并退回商品 8:拒绝(退款) 9:同意并退款 10:拒绝(售后)11:同意并且寄回商品 12售后结束
     *
     * @param r_type 类型 100:不在退货退款状态0:审核中 1:同意并让用户寄回 2:拒绝(退货退款) 3:用户已快递 4:收到寄回商品,同意并退款 5：拒绝并退回商品 8:拒绝(退款) 9:同意并退款 10:拒绝(售后)11:同意并且寄回商品 12售后结束
     */
    public void setR_type(Integer r_type)
    {
        this.r_type = r_type;
    }

    public String getExpress_id()
    {
        return express_id;
    }

    public void setExpress_id(String express_id)
    {
        this.express_id = express_id;
    }

    public String getStore_self_delivery()
    {
        return store_self_delivery;
    }

    public void setStore_self_delivery(String store_self_delivery)
    {
        this.store_self_delivery = store_self_delivery;
    }

    /**
     * 获取快递单号
     *
     * @return courier_num - 快递单号
     */
    public String getCourier_num()
    {
        return courier_num;
    }

    /**
     * 设置快递单号
     *
     * @param courier_num 快递单号
     */
    public void setCourier_num(String courier_num)
    {
        this.courier_num = courier_num == null ? null : courier_num.trim();
    }

    /**
     * 获取发货类型 1.手动发货 2.自动发货
     *
     * @return express_type - 发货类型 1.手动发货 2.自动发货
     */
    public Integer getExpress_type()
    {
        return express_type;
    }

    /**
     * 设置发货类型 1.手动发货 2.自动发货
     *
     * @param express_type 发货类型 1.手动发货 2.自动发货
     */
    public void setExpress_type(Integer express_type)
    {
        this.express_type = express_type;
    }

    /**
     * 获取运费
     *
     * @return freight - 运费
     */
    public BigDecimal getFreight()
    {
        return freight;
    }

    /**
     * 设置运费
     *
     * @param freight 运费
     */
    public void setFreight(BigDecimal freight)
    {
        this.freight = freight;
    }

    /**
     * 获取配置名称
     *
     * @return size - 配置名称
     */
    public String getSize()
    {
        return size;
    }

    /**
     * 设置配置名称
     *
     * @param size 配置名称
     */
    public void setSize(String size)
    {
        this.size = size == null ? null : size.trim();
    }

    /**
     * 获取规格属性id
     *
     * @return sid - 规格属性id
     */
    public String getSid()
    {
        return sid;
    }

    /**
     * 设置规格属性id
     *
     * @param sid 规格属性id
     */
    public void setSid(String sid)
    {
        this.sid = sid == null ? null : sid.trim();
    }

    /**
     * 获取发货单打印 0.否 1.是
     *
     * @return invoice - 发货单打印 0.否 1.是
     */
    public Integer getInvoice()
    {
        return invoice;
    }

    /**
     * 设置发货单打印 0.否 1.是
     *
     * @param invoice 发货单打印 0.否 1.是
     */
    public void setInvoice(Integer invoice)
    {
        this.invoice = invoice;
    }

    /**
     * 获取快递单打印 0.否 1.是
     *
     * @return express - 快递单打印 0.否 1.是
     */
    public Integer getExpress()
    {
        return express;
    }

    /**
     * 设置快递单打印 0.否 1.是
     *
     * @param express 快递单打印 0.否 1.是
     */
    public void setExpress(Integer express)
    {
        this.express = express;
    }

    /**
     * 获取换货次数
     *
     * @return exchange_num - 换货次数
     */
    public Integer getExchange_num()
    {
        return exchange_num;
    }

    /**
     * 设置换货次数
     *
     * @param exchange_num 换货次数
     */
    public void setExchange_num(Integer exchange_num)
    {
        this.exchange_num = exchange_num;
    }

    /**
     * 获取优惠券id
     *
     * @return coupon_id - 优惠券id
     */
    public String getCoupon_id()
    {
        return coupon_id;
    }

    /**
     * 设置优惠券id
     *
     * @param coupon_id 优惠券id
     */
    public void setCoupon_id(String coupon_id)
    {
        this.coupon_id = coupon_id == null ? null : coupon_id.trim();
    }

    /**
     * 获取优惠后的金额
     *
     * @return after_discount - 优惠后的金额
     */
    public BigDecimal getAfter_discount()
    {
        return after_discount;
    }

    /**
     * 设置优惠后的金额
     *
     * @param after_discount 优惠后的金额
     */
    public void setAfter_discount(BigDecimal after_discount)
    {
        this.after_discount = after_discount;
    }

    /**
     * 获取订单结算标识 0未结算 1已结算
     *
     * @return settlement_type - 订单结算标识 0未结算 1已结算
     */
    public Integer getSettlement_type()
    {
        return settlement_type;
    }

    /**
     * 设置订单结算标识 0未结算 1已结算
     *
     * @param settlement_type 订单结算标识 0未结算 1已结算
     */
    public void setSettlement_type(Integer settlement_type)
    {
        this.settlement_type = settlement_type;
    }

    /**
     * 获取回收站 0.显示 1.回收
     *
     * @return recycle - 回收站 0.显示 1.回收
     */
    public Integer getRecycle()
    {
        return recycle;
    }

    /**
     * 设置回收站 0.显示 1.回收
     *
     * @param recycle 回收站 0.显示 1.回收
     */
    public void setRecycle(Integer recycle)
    {
        this.recycle = recycle;
    }

    public Integer getUserRecycle()
    {
        return userRecycle;
    }

    public void setUserRecycle(Integer userRecycle)
    {
        this.userRecycle = userRecycle;
    }

    public Integer getMchRecycle()
    {
        return mchRecycle;
    }

    public void setMchRecycle(Integer mchRecycle)
    {
        this.mchRecycle = mchRecycle;
    }

    public Integer getStoreRecycle()
    {
        return storeRecycle;
    }

    public void setStoreRecycle(Integer storeRecycle)
    {
        this.storeRecycle = storeRecycle;
    }
}
