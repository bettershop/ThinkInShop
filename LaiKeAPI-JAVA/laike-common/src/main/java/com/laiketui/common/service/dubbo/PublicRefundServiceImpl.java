package com.laiketui.common.service.dubbo;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import com.google.common.base.Strings;
import com.laiketui.common.api.*;
import com.laiketui.common.api.order.PublicIntegralService;
import com.laiketui.common.api.pay.PublicPaymentService;
import com.laiketui.common.mapper.*;
import com.laiketui.common.service.dubbo.third.PublicPaypalServiceImpl;
import com.laiketui.common.utils.CpcUtils;
import com.laiketui.common.utils.tool.data.OrderDataUtils;
import com.laiketui.core.cache.RedisUtil;
import com.laiketui.core.common.SplitUtils;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.DictionaryConst;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.core.utils.tool.DataUtils;
import com.laiketui.core.utils.tool.DateUtil;
import com.laiketui.core.utils.tool.SerializePhpUtils;
import com.laiketui.core.utils.tool.StringUtils;
import com.laiketui.domain.config.ConfiGureModel;
import com.laiketui.domain.config.ExpressModel;
import com.laiketui.domain.coupon.CouponModal;
import com.laiketui.domain.home.SystemMessageModel;
import com.laiketui.domain.living.LivingCommissionModel;
import com.laiketui.domain.living.LivingProductModel;
import com.laiketui.domain.living.LivingRoomModel;
import com.laiketui.domain.log.MchAccountLogModel;
import com.laiketui.domain.log.RecordModel;
import com.laiketui.domain.log.SignRecordModel;
import com.laiketui.domain.mch.*;
import com.laiketui.domain.message.MessageLoggingModal;
import com.laiketui.domain.order.*;
import com.laiketui.domain.presell.PreSellGoodsModel;
import com.laiketui.domain.presell.PreSellRecordModel;
import com.laiketui.domain.product.ProductListModel;
import com.laiketui.domain.product.StockModel;
import com.laiketui.domain.seckill.SecondsActivityModel;
import com.laiketui.domain.supplier.SupplierAccountLogModel;
import com.laiketui.domain.supplier.SupplierModel;
import com.laiketui.domain.supplier.SupplierOrderFrightModel;
import com.laiketui.domain.vo.main.RefundVo;
import com.laiketui.domain.vo.order.RefundResultView;
import com.laiketui.root.gateway.util.I18nUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

import static com.laiketui.core.lktconst.DictionaryConst.OrdersStatus.ORDERS_R_STATUS_COMPLETE;
import static com.laiketui.core.lktconst.DictionaryConst.OrdersStatus.ORDERS_R_STATUS_CONSIGNMENT;


/**
 * 公共售后实现
 *
 * @author Trick
 * @date 2020/12/2 12:05
 */
@Service
public class PublicRefundServiceImpl implements PublicRefundService
{

    private final Logger logger = LoggerFactory.getLogger(PublicRefundServiceImpl.class);

    @Autowired
    private PublicOrderService publicOrderService;


    @Autowired
    private MessageLoggingModalMapper messageLoggingModalMapper;

    @Autowired
    private MchStoreWriteModelMapper mchStoreWriteModelMapper;

    @Autowired
    private GroupOpenModelMapper groupOpenModelMapper;

    @Autowired
    private FlashsaleAddGoodsModelMapper flashsaleAddGoodsModelMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private LivingCommissionModelMapper livingCommissionModelMapper;

    @Autowired
    private PublicMchService publicMchService;

    @Autowired
    private PublicPaypalServiceImpl publicPaypalService;

    @Autowired
    private RecordModelMapper recordModelMapper;


    @Override
    public boolean refund(RefundVo vo) throws LaiKeAPIException
    {
        int id      = vo.getId();
        int storeId = vo.getStoreId();
        logger.info("是否走了定时任务：：：：{}",vo.getIsTask());
        try
        {
            //是否退还优惠券
            boolean refundCoupon = true;
            //店铺联系方式
            String mchPhone = "";
            //是否短信配置 ---> 禅道47551 测试说没有发送短信给商家的功能
            boolean isSendNotice = false;
            //售后记录
            ReturnRecordModel returnRecordSave = new ReturnRecordModel();
            returnRecordSave.setStore_id(vo.getStoreId());
            int count = 0;

            //获取电商平台管理员信息
            CustomerModel customerModel = new CustomerModel();
            customerModel.setId(vo.getStoreId());
            customerModel.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS);
            customerModel = customerModelMapper.selectOne(customerModel);
            if (customerModel != null)
            {
                mchPhone = customerModel.getMobile();
            }
            Integer orderDetailId = null;
            //获取退款订单详情
            List<Map<String, Object>> orderInfoList      = getReturnOrderInfo(vo);
            Map<String, Object>       returnOrderInfoMap = orderInfoList.get(0);
            //详情状态
            int rStatus = MapUtils.getInteger(returnOrderInfoMap, "r_status");
            //支付方式
            String payType = returnOrderInfoMap.get("pay").toString();
            //售后id
            int refundId = MapUtils.getIntValue(returnOrderInfoMap, "refundId");
            // 0:待结算——1:已结算
            int settlementType = MapUtils.getIntValue(returnOrderInfoMap, "settlement_type");
            //订单是否已经结算标识
            boolean isSettlement = settlementType != 0;
            if (isSettlement)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DDYJJS, "订单已经结算");
            }

            //剩余核销次数
            Integer afterWriteOffNum = MapUtils.getInteger(returnOrderInfoMap, "after_write_off_num");

            //店铺id
            int mchId = StringUtils.stringParseInt(StringUtils.trim(returnOrderInfoMap.get("mch_id").toString(), SplitUtils.DH));
            //当不是后台操作时
            if (vo.getMchId() != 0)
            {
                //验证店铺
                if (vo.getMchId() != mchId)
                {
                    logger.info("订单所属店铺不一致 参数:" + vo.getMchId() + ">>>" + mchId);
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_MDBHBZQ, "门店编号不正确", "refund");
                }
            }
            //获取店铺信息
            MchModel mchModel = new MchModel();
            mchModel.setStore_id(vo.getStoreId());
            mchModel.setId(mchId);
            mchModel = mchModelMapper.selectOne(mchModel);
            if (mchModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DPSJYC, "店铺数据异常", "refund");
            }

            //商品id
            int goodsId = StringUtils.stringParseInt(returnOrderInfoMap.get("goodsId").toString());

            //订单区号
            String cpc = MapUtils.getString(returnOrderInfoMap, "cpc", "86");

            //插件id
            int p_id = StringUtils.stringParseInt(returnOrderInfoMap.get("p_id").toString());
            //是否为供应商商品
            boolean          isSupplierPro = false;
            ProductListModel proModel      = null;
            //订单id
            String orderno = returnOrderInfoMap.get("sNo").toString();

           /* if (!orderno.contains(DictionaryConst.OrdersType.ORDERS_HEADER_PT))
            {

            }
            else
            {
                //拼团订单详情表p_id字段存储的不是商品id 是开团id 所以只在这个地方做特殊处理 需要根据开团id 查询出商品id 在查商品信息
                GroupOpenModel groupOpenModel = new GroupOpenModel();
                groupOpenModel.setId(goodsId + "");
                groupOpenModel = groupOpenModelMapper.selectOne(groupOpenModel);
                goodsId = groupOpenModel.getGoods_id();
                proModel = productListModelMapper.selectByPrimaryKey(goodsId);
            }*/
            proModel = productListModelMapper.selectByPrimaryKey(goodsId);
            if (proModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SPBCZ, "商品不存在");
            }
            //订单详情id
            int detailId = orderDetailId = StringUtils.stringParseInt(returnOrderInfoMap.get("detialId").toString());
            //订单金额
            BigDecimal orderPrice = new BigDecimal(returnOrderInfoMap.get("z_price").toString());
            //订单原始支付金额
            BigDecimal old_total = new BigDecimal(returnOrderInfoMap.get("old_total").toString());
            //单个商品的实际支付金额
            BigDecimal goodsPayPrice = getOrderPrice(vo.getStoreId(), detailId);
            //售后类型
            int reType = MapUtils.getIntValue(returnOrderInfoMap, "re_type");
            //订单类型
            String otype = MapUtils.getString(returnOrderInfoMap, "otype", "");
            //订单id
            int orderId = StringUtils.stringParseInt(returnOrderInfoMap.get("oId").toString());
            //非换货情况： 仅退款 退货退款
            if (!DictionaryConst.ReturnRecordReType.RETURNORDERSTATUS_GOODS_REBACK.equals(reType))
            {
                //退款金额限制
                if (orderPrice.compareTo(vo.getPrice()) < 0)
                {
                    logger.info("订单详情id{}退款失败,金额错误 退款金额{} 实际订单支付金额{}", detailId, vo.getPrice(), orderPrice);
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SRDJEYWQZXSR, "输入的金额有误,请重新输入");
                }
                else if (otype.equals(DictionaryConst.OrdersType.ORDERS_HEADER_VI) && DataUtils.getBigDecimalVal(returnOrderInfoMap, "re_money").compareTo(vo.getPrice()) < 0)
                {
                    logger.info("订单详情id{}退款失败,金额错误 退款金额{} 实际虚拟商品应退金额{}", detailId, vo.getPrice(), DataUtils.getBigDecimalVal(returnOrderInfoMap, "re_money"));
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_TKJEYGXYYTJE, "退款金额应该小于应退金额");
                }
            }
            OrderModel orderModel = new OrderModel();
            //订单读取状态
            int read = StringUtils.stringParseInt(returnOrderInfoMap.get("readd") + "");
            if (read == 0)
            {
                orderModel = new OrderModel();
                orderModel.setId(orderId);
                orderModel.setReadd(OrderModel.READ);
                count = orderModelMapper.updateByPrimaryKeySelective(orderModel);
                if (count < 1)
                {
                    logger.info("修改订单已读状态失败 参数:" + JSON.toJSONString(orderModel));
                }
            }

            //买家userid
            String clientUserId = returnOrderInfoMap.get("user_id").toString();
            returnRecordSave.setUser_id(clientUserId);
            //买家名称
            String clientUserName = returnOrderInfoMap.get("name").toString();
            //买家手机号
            String clientUserPhon = returnOrderInfoMap.get("mobile").toString();

            if (StringUtils.isNotEmpty(proModel.getGongyingshang()) && !Objects.isNull(proModel.getSupplier_superior()))
            {
                isSupplierPro = true;
                redisUtil.set(GloabConst.RedisHeaderKey.REFUND_ORDER_KEY + storeId + orderno, isSupplierPro);
            }
            Integer livingRoomId = MapUtils.getInteger(returnOrderInfoMap, "living_room_id");
            //属性id
            int attributeId = StringUtils.stringParseInt(returnOrderInfoMap.get("attributeId").toString());

            System.out.println("支付方式来了：" + payType);
            //订单商品使用的优惠卷id
            String couponId = returnOrderInfoMap.get("detailCouponId") + "";
            //满减活动id
            int subtractionId = StringUtils.stringParseInt(returnOrderInfoMap.get("subtraction_id") + "");
            //订单商品运费
            BigDecimal freight = new BigDecimal(returnOrderInfoMap.get("freight").toString());
            //支付流水号
            String realOrderno = MapUtils.getString(returnOrderInfoMap, "real_sno", "");
            //父订单id
            String psNo = MapUtils.getString(returnOrderInfoMap, "p_sNo");
            //调起支付支付金额
            BigDecimal upPaymentPrice = old_total;
            // 订单拆单过
            if (StringUtils.isNotEmpty(psNo))
            {
                //根据调起支付所用订单号获取主订单号(未支付先拆单，子订单调起支付所用调起支付所用订单号和总订单相同)
                String sNo = orderModelMapper.getOrderByRealSno(realOrderno);
                logger.info("根据吊起支付订单号获取主订单号:{}", sNo);
                if (StringUtils.isNotEmpty(sNo))
                {
                    orderModel = new OrderModel();
                    orderModel.setsNo(sNo);
                    orderModel = orderModelMapper.selectOne(orderModel);
                    upPaymentPrice = orderModel.getOld_total();
                }
            }
            //是否已到货标识 只有确认收货后钱才会到店铺余额,过了售后则到可提现金额
            boolean isArruve = OrderModel.ORDER_FINISH == rStatus;
            //订单商品总价
            BigDecimal orderDetailPrice = new BigDecimal(returnOrderInfoMap.get("p_price").toString());
            //商品数量
            BigDecimal goodsNeedNum = new BigDecimal(returnOrderInfoMap.get("num").toString());
            orderDetailPrice = orderDetailPrice.multiply(goodsNeedNum);
            logger.info("订单号：{},订单详情id:{},商品总价：{},商品数量：{},运费：{}", psNo, orderDetailId, orderDetailPrice, goodsNeedNum, freight);

            //供应商端除外其他端操作供应商售后订单只做店铺是否同意处理
            if (supplierReturnOrderMchHandle(vo, goodsId, orderId, orderno))
            {
                return true;
            }

            //虚拟商品退货回滚日期可预约次数
            refundCoupon = viReturnOrderHandle(refundCoupon, returnOrderInfoMap);


            //实际退款金额
            BigDecimal realMoney  = vo.getPrice();
            logger.info("===同意退款===");
            //获取实际支付金额
            logger.info("同意订单详情id{}退款,订单商品付款金额{},实际退款金额{}", detailId, goodsPayPrice, vo.getPrice());
            // 组合支付不要了
            if (DictionaryConst.OrderPayType.ORDERPAYTYPE_TT_ALIPAY.equals(payType))
            {
                payType = DictionaryConst.OrderPayType.ORDERPAYTYPE_ALIPAY;
            }
            else if (DictionaryConst.OrderPayType.ORDERPAYTYPE_BAIDU_PAY.equals(payType))
            {
                payType = DictionaryConst.OrderPayType.ORDERPAYTYPE_WALLET_PAY;
            }

            logger.info("退款信息payType，{}", payType);
            MchAccountLogModel mchAccountLogModel = new MchAccountLogModel();
            mchAccountLogModel.setStore_id(vo.getStoreId());
            mchAccountLogModel.setMch_id(mchModel.getId().toString());
            mchAccountLogModel.setRemake(orderno);
            mchAccountLogModel.setAddtime(new Date());
            mchAccountLogModel.setId(null);

            //实际退款金额
            realMoney = vo.getPrice();
            //最多退款金额
            BigDecimal maxTuiMoney = goodsPayPrice.add(freight);

            //获取加购商品的金额
            List<OrderDetailsModel> addGoodsList = orderDetailsModelMapper.getAddGoodsList(orderno);
            if (CollectionUtils.isNotEmpty(addGoodsList))
            {
                double total = addGoodsList.stream()
                        .mapToDouble(order -> order.getP_price().multiply(new BigDecimal(order.getNum())).doubleValue())
                        .sum();
                maxTuiMoney = maxTuiMoney.add(new BigDecimal(total));
            }

            //最多退款金额大于店铺退款金额的时候结束退款操作
            if (maxTuiMoney.compareTo(vo.getPrice()) < 0)
            {
                logger.info("订单详情id{}退款失败,金额错误 退款金额{} 实际订单支付金额{}", detailId, vo.getPrice(), maxTuiMoney);
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SRDJEYWQZXSR, "输入的金额有误,请重新输入");
            }

            // 添加一条退款记录---少退的情况 是否少退,如果少退则增加店铺收入-立即结算到可提现金额
            // 剩余未退款金额 = 该订单详情最大退款金额 - 退款金额
            BigDecimal remainMoney = maxTuiMoney.subtract(vo.getPrice());
            // 1、用户未收货：普通、供应商商品已付款待收货；2、虚拟商品待核销
            boolean orderHadPayButNotReceiptFlag = rStatus == OrderModel.ORDER_PAYED || rStatus == OrderModel.ORDER_UNRECEIVE || (DictionaryConst.OrdersType.ORDERS_HEADER_VI.equals(otype) && rStatus == DictionaryConst.OrdersStatus.ORDERS_R_STATUS_TOBEVERIFIED);
            // 1、已收货：普通商品店铺已入账店铺余额，且供应商商品未入账；
            boolean orderhadReceiptFlag = (rStatus == OrderModel.ORDER_FINISH);


            String     tuiTitle   = "退货/退款成功!";
            String     tuiContext = "您的退货/退款申请已通过!";
            //订单售后流程
            if (vo.getType().equals(DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_AGREE_REBACK)
                    || vo.getType().equals(DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_USER_DELIVERED)
                    || vo.getType().equals(DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_RECEIVED_REBAKGOODS)
                    || vo.getType().equals(DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_QUICK_SUPPLIER_AGRESS)
                    || vo.getType().equals(DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_AGREE_REBACK_AMT)
                    //人审核流程
                    || vo.getType().equals(DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_REFUSE_ARTIFICIAL_OK)
            )
            {
                if (vo.getType().equals(DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_REFUSE_ARTIFICIAL_OK) && !this.isMainExamine(vo.getStoreId(), refundId))
                {
                    logger.debug("售后id:{},不能人工受理!", refundId);
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ILLEGAL_INVASION, "非法入侵");
                }
                logger.info("=====退款开始=====");
                if (DictionaryConst.ReturnRecordReType.RETURNORDERSTATUS_GOODS_REBACK.equals(reType) && vo.getType().equals(DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_AGREE_REBACK))
                {
                    tuiTitle = "换货";
                    tuiContext = "您的换货申请已通过!";
                }

                vo.setMchId(mchId);
                vo.setMchModel(mchModel);
                vo.setIsSupplierPro(isSupplierPro);
                vo.setProductListModel(proModel);
                vo.setSNo(orderno);
                vo.setRStatus(rStatus);
                vo.setRealMoney(realMoney);
                vo.setMchAccountLogModel(mchAccountLogModel);
                vo.setMaxTuiMoney(maxTuiMoney);
                vo.setRemainMoney(remainMoney);
                vo.setOrderHadPayButNotReceiptFlag(orderHadPayButNotReceiptFlag);
                vo.setOrderhadReceiptFlag(orderhadReceiptFlag);
                vo.setOrderDetailId(orderDetailId);
                vo.setIsSettlement(isSettlement);
                vo.setLivingRoomId(livingRoomId);
                vo.setGoodsId(goodsId);
                vo.setOType(otype);
                vo.setAttributeId(attributeId);
                vo.setGoodsNeedNum(goodsNeedNum);
                vo.setClientUserId(clientUserId);
                vo.setAfterWriteOffNum(afterWriteOffNum);
                vo.setRefundCoupon(refundCoupon);
                vo.setOrderPrice(orderPrice);
                vo.setOrderId(orderId);
                vo.setCouponId(couponId);
                vo.setPsNo(psNo);
                vo.setMchPhone(mchPhone);
                vo.setIsSendNotice(isSendNotice);
                vo.setPayType(payType);
                vo.setRealOrderNo(realOrderno);
                vo.setUpPaymentPrice(upPaymentPrice);
                vo.setRefundId(refundId);
                vo.setFreight(freight);
                vo.setOrderDetailPrice(orderDetailPrice);
                vo.setP_id(p_id);
                //同意退款
                if (vo.getType().equals(DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_RECEIVED_REBAKGOODS)
                        || vo.getType().equals(DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_AGREE_REBACK_AMT)
                        //人工受理
                        || vo.getType().equals(DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_REFUSE_ARTIFICIAL_OK))
                {
                    //公共退款业务逻辑处理
                    initRefund(vo);
                }
            }
            else if (DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_AGREE_REBACK_GOODS.equals(vo.getType()))
            {
                tuiTitle = String.format("【%s】订单发货啦！", vo.getSNo());
                tuiContext = "您购买的商品已经在赶去见您的路上啦！";
                //添加一条快递数据
                ReturnGoodsModel returnGoodsModel = new ReturnGoodsModel();
                returnGoodsModel.setStore_id(vo.getStoreId());
                returnGoodsModel.setName(clientUserName);
                returnGoodsModel.setTel(clientUserPhon);

                if (StringUtils.isEmpty(vo.getCourierNum()))
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_KDDHBNWK, "快递单号不能为空");
                }
                //查询快递公司信息
                ExpressModel expressModel = new ExpressModel();

                if (StringUtils.isEmpty(vo.getExpressId()))
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_KDGSBNWK, "快递公司不能为空");
                }

                expressModel.setId(vo.getExpressId());
                expressModel.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS);
                expressModel = expressModelMapper.selectOne(expressModel);
                if (expressModel == null)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLGSBCZ, "物流公司不存在");
                }

                if (!CpcUtils.isHome(cpc))
                {
                    publicOrderService.trackDelivery(storeId,vo.getCourierNum());
                }

                returnGoodsModel.setExpress(expressModel.getKuaidi_name());
                returnGoodsModel.setExpress_num(vo.getCourierNum());
                returnGoodsModel.setUser_id(clientUserId);
                returnGoodsModel.setOid(detailId + "");
                returnGoodsModel.setRe_id(vo.getId());
                returnGoodsModel.setAdd_data(new Date());
                count = returnGoodsModelMapper.insertSelective(returnGoodsModel);
                if (count < 1)
                {
                    logger.info("新增售后记录失败 参数:" + JSON.toJSONString(returnGoodsModel));
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLGZTKSB, "网络故障,退款失败", "refund");
                }
                //修改明细物流信息
                OrderDetailsModel updateOrderDetail = new OrderDetailsModel();
                updateOrderDetail.setId(detailId);
                updateOrderDetail.setExpress_id(expressModel.getId().toString());
                updateOrderDetail.setCourier_num(returnGoodsModel.getExpress_num());
                count = orderDetailsModelMapper.updateByPrimaryKeySelective(updateOrderDetail);
                if (count < 1)
                {
                    logger.info("修改订单物流信息失败 参数:" + JSON.toJSONString(updateOrderDetail));
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLGZTKSB, "网络故障,退款失败", "refund");
                }
                updateOrderDetail = orderDetailsModelMapper.selectByPrimaryKey(detailId);
                //物流记录表
                ExpressDeliveryModel expressDeliveryModel = new ExpressDeliveryModel();
                expressDeliveryModel.setStore_id(vo.getStoreId());
                expressDeliveryModel.setsNo(updateOrderDetail.getR_sNo());
                expressDeliveryModel.setExpressId(expressModel.getId());
                expressDeliveryModel.setCourierNum(returnGoodsModel.getExpress_num());
                expressDeliveryModel.setOrderDetailsId(updateOrderDetail.getId());
                expressDeliveryModel.setNum(updateOrderDetail.getNum());
                expressDeliveryModel.setDeliverTime(new Date());
                expressDeliveryModelMapper.insertSelective(expressDeliveryModel);
                //记录
                returnRecordSave.setCourier_num(vo.getCourierNum());
                returnRecordSave.setExpress_id(vo.getExpressId());
            }
            else if (vo.getType().equals(DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_QUICK_SUPPLIER_AGRESS))
            {
                tuiTitle = "退货退款";
                tuiContext = "您的退货退款申请已通过!";
            }
            else
            {
                String text = vo.getText() + "";
                if (StringUtils.isEmpty(text.trim()))
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_LYBNWK, "理由不能为空", "refund");
                }
                //0:审核中 2:拒绝(退货退款) 3:用户已快递 5：拒绝并退回商品 8:拒绝(退款)10:拒绝(售后)
                tuiTitle = "退货/退款失败!";
                tuiContext = String.format("您申请的退货/退款被拒绝!原因：%s", vo.getText());
                //记录
                returnRecordSave.setContent(vo.getText());
                //商家拒绝退款，退还可核销次数
                if (MapUtils.getString(returnOrderInfoMap, "otype").equals(DictionaryConst.OrdersType.ORDERS_HEADER_VI) && returnOrderInfoMap.containsKey("r_write_off_num"))
                {
                    OrderDetailsModel orderDetailsModel = orderDetailsModelMapper.selectByPrimaryKey(detailId);
                    orderDetailsModel.setWrite_off_num(DataUtils.getIntegerVal(returnOrderInfoMap, "r_write_off_num"));
                    orderDetailsModelMapper.updateByPrimaryKey(orderDetailsModel);
                }
                //如果供应商拒绝退款，设置供应商结算金额为0
                if (StringUtils.isNotEmpty(vo.getOperator()) && vo.getOperator().equals("supplier"))
                {
                    OrderDetailsModel orderDetailsModel = orderDetailsModelMapper.selectByPrimaryKey(detailId);
                    orderDetailsModel.setSupplier_settlement(BigDecimal.ZERO);
                    orderDetailsModelMapper.updateByPrimaryKey(orderDetailsModel);
                }
            }
            if (!vo.getIsTask())
            {
                extractRefundRecord(vo, realMoney, returnRecordSave, clientUserId, reType, orderno, refundId, goodsId, attributeId, tuiTitle, tuiContext, orderDetailId);
            }
            return true;
        }
        catch (ClassCastException c)
        {
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSLXBZQ, "参数类型不正确", "refund");
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("售后 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "refund");
        }
        finally
        {
            //删除重复操作标识
            redisUtil.del(GloabConst.RedisHeaderKey.REFUND_ORDER_KEY + storeId + "_" + id);
        }
    }

    private void extractRefundRecord(RefundVo vo, BigDecimal realMoney, ReturnRecordModel returnRecordSave, String clientUserId, int reType, String orderno, int refundId, int goodsId, int attributeId, String tuiTitle, String tuiContext, Integer orderDetailId)
    {
        int count;
        //修改售后状态
        ReturnOrderModel updateReturnOrder = new ReturnOrderModel();
        updateReturnOrder.setId(vo.getId());
        updateReturnOrder.setR_type(vo.getType());
        updateReturnOrder.setR_content(vo.getText());
        if (realMoney != null && realMoney.compareTo(BigDecimal.ZERO) > 0)
        {
            //实际退款金额
            updateReturnOrder.setReal_money(realMoney);
        }
        updateReturnOrder.setAudit_time(new Date());

        count = returnOrderModelMapper.updateByPrimaryKeySelective(updateReturnOrder);
        if (count < 1)
        {
            logger.info("订单售后状态失败 参数:" + JSON.toJSONString(updateReturnOrder));
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLGZTKSB, "网络故障,退款失败", "refund");
        }
        //重新生成一条售后记录 每次操作都增加一条售后记录 add by trick 2023-02-24 10:29:02
        returnRecordSave.setUser_id(clientUserId);
        returnRecordSave.setStore_id(vo.getStoreId());
        returnRecordSave.setRe_type(reType);
        returnRecordSave.setR_type(vo.getType());
        returnRecordSave.setsNo(orderno);
        returnRecordSave.setP_id(refundId);
        returnRecordSave.setMoney(realMoney);
        returnRecordSave.setProduct_id(goodsId);
        returnRecordSave.setAttr_id(attributeId);
        returnRecordSave.setRe_time(new Date());
        count = returnRecordModelMapper.insertSelective(returnRecordSave);
        if (count < 1)
        {
            logger.info("添加售后信息失败 参数:" + JSON.toJSONString(returnRecordSave));
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLGZSHJLSB, "网络故障,售后记录失败", "returnData");
        }
        //TODO 【微信推送暂时不做】 发送微信小程序推送退款信息
        //站内推送退款信息
        //站内推送消息
        sendInnerMessage(vo, clientUserId, tuiTitle, tuiContext);
        //最后再判断一下订单是否可以售后
        ReturnOrderModel returnOrderModelNew = returnOrderModelMapper.getReturnNewInfoByDetailId(orderDetailId);
        if (returnOrderModelNew == null)
        {
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SHDDYCX, "售后订单已撤销", "refund");
        }
    }

    /**
     * 获取退款订单详情
     *
     * @param vo
     * @return
     */
    private List<Map<String, Object>> getReturnOrderInfo(RefundVo vo)
    {
        int                 id       = vo.getId();
        int                 storeId  = vo.getStoreId();
        Map<String, Object> parmaMap = new HashMap<>(16);
        parmaMap.put("store_id", vo.getStoreId());
        //防止连续点击造成多次退款 -并发处理
        if (redisUtil.incr(GloabConst.RedisHeaderKey.REFUND_ORDER_KEY + storeId + "_" + id, 1) > 1)
        {
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CFCZ, "重复操作", "refund");
        }
        //判断是否设置售后地址
        ServiceAddressModel serviceAddressModel = new ServiceAddressModel();
        serviceAddressModel.setStore_id(vo.getStoreId());
        serviceAddressModel.setUid("admin");
        serviceAddressModel.setIs_default(DictionaryConst.DefaultMaven.DEFAULT_OK);
        serviceAddressModel.setType(DictionaryConst.ServiceAddressType.ServiceAddressType_AfterSale);
        int count = serviceAddressModelMapper.selectCount(serviceAddressModel);
        if (count < 0)
        {
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QSZSHDZ, "请设置售后地址", "refund");
        }

        //进入订单详情把未读状态改成已读状态，已读状态的状态不变
        parmaMap.put("id", vo.getId());
        List<Map<String, Object>> orderInfoList = returnOrderModelMapper.getReturnOrderJoinOrderListDynamic(parmaMap);
        if (CollectionUtils.isEmpty(orderInfoList))
        {
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SHDDBCZ, "售后订单不存在", "refund");
        }

        //详情状态
        int rStatus = MapUtils.getInteger(orderInfoList.get(0), "r_status");

        //防止店铺和平台同时进行退款，退多比款
        if (rStatus == DictionaryConst.OrdersStatus.ORDERS_R_STATUS_CLOSE)
        {
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DDYGB,"订单已关闭，请勿重复提交");
        }
        return orderInfoList;
    }

    private void initRefund (RefundVo vo) {

        /**
         * 走定时任务只需要执行系统的数据回滚，不需要走退款
         * 不走定时任务的钱包支付，需要全部都执行回滚
         */
        try {

            //店铺id
            int mchId = vo.getMchId();
            //店铺信息
            MchModel mchModel = vo.getMchModel();
            //是否已结算
            Boolean isSettlement = vo.getIsSettlement();
            //是否供应商商品
            Boolean isSupplierPro = vo.getIsSupplierPro();
            //商品信息
            ProductListModel proModel = vo.getProductListModel();
            //订单编号
            String sNo = vo.getSNo();
            //订单详情状态
            Integer rStatus = vo.getRStatus();
            //实际退款金额
            BigDecimal realMoney = vo.getRealMoney();
            //商家售后明细
            MchAccountLogModel mchAccountLogModel = vo.getMchAccountLogModel();
            //最大退款金额
            BigDecimal maxTuiMoney = vo.getMaxTuiMoney();
            //剩余退款金额
            BigDecimal remainMoney = vo.getRemainMoney();
            //是否待收货
            Boolean orderHadPayButNotReceiptFlag = vo.getOrderHadPayButNotReceiptFlag();
            //是否已收货
            Boolean orderhadReceiptFlag = vo.getOrderhadReceiptFlag();
            //订单详情id
            Integer detailId = vo.getOrderDetailId();
            //直播间id
            Integer livingRoomId = vo.getLivingRoomId();
            //商品id
            Integer goodsId = vo.getGoodsId();
            //订单类型
            String oType = vo.getOType();
            //属性id
            Integer attributeId = vo.getAttributeId();
            //商品数量
            BigDecimal goodsNeedNum = vo.getGoodsNeedNum();
            //用户id
            String clientUserId = vo.getClientUserId();
            //退还核销次数
            Integer afterWriteOffNum = vo.getAfterWriteOffNum();
            //是否退还优惠券
            Boolean refundCoupon = vo.getRefundCoupon();
            //订单金额
            BigDecimal orderPrice = vo.getOrderPrice();
            //订单id
            Integer orderId = vo.getOrderId();
            //优惠券id
            String couponId = vo.getCouponId();
            //父订单号
            String psNo = vo.getPsNo();
            //店铺电话
            String mchPhone = vo.getMchPhone();
            //是否短信通知商家
            Boolean isSendNotice = vo.getIsSendNotice();
            //支付类型
            String payType = vo.getPayType();
            //支付流水号
            String realOrderNo = vo.getRealOrderNo();
            //调起支付金额
            BigDecimal upPaymentPrice = vo.getUpPaymentPrice();
            //售后id
            Integer refundId = vo.getRefundId();
            //运费
            BigDecimal freight = vo.getFreight();
            //订单详情金额
            BigDecimal orderDetailPrice = vo.getOrderDetailPrice();


            //实时进行第三方支付退款，只走申请退款接口，不走系统业务逻辑，业务逻辑走定时任务查询退款是否成功，成功才进行回滚
            if (!vo.getIsTask() && !DictionaryConst.OrderPayType.ORDERPAYTYPE_WALLET_PAY.equals(payType))
            {
                logger.info("走定时任务进行业务操作");
                //**********************************************************************
                //**********************************************************************
                //七、用户退款：钱包、微信、支付宝、贝宝
                //**********************************************************************
                //**********************************************************************
                //退款流程：退款金额原路返回
                returnOrderRefundAmountBackHandle(vo, mchPhone, isSendNotice, detailId, orderId, clientUserId, payType, realOrderNo, upPaymentPrice, refundId);
                return;
            }

            //**********************************************************************
            //**********************************************************************
            // 一 、 店铺、供应商 账户结算 ：
            // 说明：只做详单的结算，不做主单的结算 这时候的金额只在账户余额增减，可提现金额不做增减。
            //**********************************************************************
            //**********************************************************************

            //供应商商品售后 仅退款需结算给供应商当前订单的供货价及运费 退货退款需结算给供应商当前订单的运费
            //刷新店铺信息
            isSettlement = returnOrderMoneySettlement(vo, mchId, mchModel, isSupplierPro, proModel, sNo, rStatus, realMoney,
                    mchAccountLogModel, maxTuiMoney, remainMoney, orderHadPayButNotReceiptFlag, orderhadReceiptFlag, detailId);

            //**********************************************************************
            //**********************************************************************
            // 二 、 库存
            //**********************************************************************
            //**********************************************************************

            returnOrderGoodStockHandle(vo, livingRoomId, goodsId, isSupplierPro, proModel, oType,
                    attributeId, goodsNeedNum, clientUserId);

            //**********************************************************************
            //**********************************************************************
            //四、判断是否为预售商品，修改预售订单表的数据
            //**********************************************************************
            //**********************************************************************
            presellOrderHandle(sNo);

            //**********************************************************************
            //**********************************************************************
            //五、直播订单处理 佣金删除
            //**********************************************************************
            //**********************************************************************
            //如果订单类型为直播并且最终同意售后，包括仅退款和退货退款同意
            livingOrderHandle(vo, oType, sNo);

            //**********************************************************************
            //**********************************************************************
            // 五、订单详情处理
            //**********************************************************************
            //**********************************************************************

            //修改订单状态为关闭
            returnOrderDetailsHandle(afterWriteOffNum, isSettlement, detailId, oType, sNo);

            //**********************************************************************
            //**********************************************************************
            //六、如果订单商品使用了优惠卷
            //**********************************************************************
            //**********************************************************************
            returnOrderCouponHandler(vo, refundCoupon, orderPrice, orderId, sNo, clientUserId, couponId, psNo);

            int count = orderModelMapper.updateOrderPrice(vo.getStoreId(), sNo, vo.getPrice(), freight, orderDetailPrice);
            if (count < 1)
            {
                logger.error("退款订单价格扣减失败 订单号:{} 扣减订单总金额:{} 扣减运费:{} 扣减订单总商品金额:{}", sNo, vo.getPrice(), freight, orderDetailPrice);
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLGZTKSB, "网络故障,退款失败", "refund");
            }

            if (!vo.getIsTask() && payType.equals(DictionaryConst.OrderPayType.ORDERPAYTYPE_WALLET_PAY))
            {
                //**********************************************************************
                //**********************************************************************
                //七、用户退款：钱包、微信、支付宝、贝宝
                //**********************************************************************
                //**********************************************************************
                //退款流程：退款金额原路返回
                returnOrderRefundAmountBackHandle(vo, mchPhone, isSendNotice, detailId, orderId, clientUserId, payType, realOrderNo, upPaymentPrice, refundId);

                //统一订单状态 测试普通订单 纯积分付款退款 退回积分
                publicOrderService.updateOrderState(vo.getStoreId(), sNo, vo.getReType());

            }

        } catch (LaiKeAPIException e)
        {
            throw e;
        }
    }

    private void presellOrderHandle(String orderno)
    {
        List<Map<String, Object>> list = preSellRecordModelMapper.selectListBySNo(orderno);
        logger.error("预售退款的list:" + list.toString());
        if (!Objects.isNull(list) && !list.isEmpty())
        {

            preSellRecordModelMapper.updateIsRefundBySNo(orderno, 1);
        }
    }

    private void livingOrderHandle(RefundVo vo, String otype, String orderno)
    {
        if ((DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_RECEIVED_REBAKGOODS.equals(vo.getType()) ||
                DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_AGREE_REBACK_AMT.equals(vo.getType()) ||
                DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_REFUSE_ARTIFICIAL_OK.equals(vo.getType())) &&
                DictionaryConst.OrdersType.ORDERS_HEADER_ZB.equals(otype))
        {
            OrderDetailsModel orderDetailsModel = new OrderDetailsModel();
            orderDetailsModel.setR_sNo(orderno);
            orderDetailsModel.setRecycle(0);
            orderDetailsModel.setStore_id(vo.getStoreId());
            orderDetailsModel = orderDetailsModelMapper.selectOne(orderDetailsModel);
            orderDetailsModel.setCommission(new BigDecimal(0));
            orderDetailsModelMapper.updateByPrimaryKeySelective(orderDetailsModel);
            //删除掉佣金表的记录
            LivingCommissionModel livingCommissionModel = new LivingCommissionModel();
            livingCommissionModel.setS_no(orderno);
            livingCommissionModel.setRecycle(0);
            livingCommissionModel = livingCommissionModelMapper.selectOne(livingCommissionModel);
            livingCommissionModel.setRecycle(1);
            livingCommissionModelMapper.updateByPrimaryKeySelective(livingCommissionModel);
        }
    }

    /**
     * 退款订单详情处理
     *
     * @param afterWriteOffNum
     * @param isSettlement     是否结算
     * @param detailId         订单详情id
     * @param otype            订单类型
     */
    private void returnOrderDetailsHandle(Integer afterWriteOffNum, boolean isSettlement, int detailId, String otype, String orderNo) throws LaiKeAPIException
    {
        try
        {
            List<OrderDetailsModel> orderDetailsModelList = new ArrayList<>();
            //获取加购商品信息
            OrderDetailsModel addOrderDetail = new OrderDetailsModel();
            addOrderDetail.setIs_addp(1);
            addOrderDetail.setR_sNo(orderNo);
            List<OrderDetailsModel> addOrderDetailList = orderDetailsModelMapper.select(addOrderDetail);
            if (!addOrderDetailList.isEmpty())
            {
                orderDetailsModelList.addAll(addOrderDetailList);
            }

            OrderDetailsModel detailsModel = orderDetailsModelMapper.selectByPrimaryKey(detailId);
            orderDetailsModelList.add(detailsModel);
            //退款列表
            for (OrderDetailsModel orderDetailsModel : orderDetailsModelList)
            {
                int count;
                if (isSettlement)
                {
                    orderDetailsModel.setSettlement_type(OrderDetailsModel.SETTLEMENT_TYPE_SETTLED);
                }
                if (DictionaryConst.OrdersType.ORDERS_HEADER_VI.equals(otype))
                {
                    if (Objects.nonNull(afterWriteOffNum))
                    {
                        //虚拟商品退款的时候需要记录一下到货时间
                        orderDetailsModel.setArrive_time(new Date());
                        if (afterWriteOffNum > 0)
                        {
                            orderDetailsModel.setR_status(ORDERS_R_STATUS_COMPLETE);
                        }
                        else
                        {
                            orderDetailsModel.setR_status(DictionaryConst.OrdersStatus.ORDERS_R_STATUS_CLOSE);
                        }
                    }
                }
                else
                {
                    orderDetailsModel.setR_status(DictionaryConst.OrdersStatus.ORDERS_R_STATUS_CLOSE);
                }
                orderDetailsModel.setAudit_time(new Date());
                count = orderDetailsModelMapper.updateByPrimaryKeySelective(orderDetailsModel);
                if (count < 1)
                {
                    logger.info("关闭订单失败 参数:" + JSON.toJSONString(orderDetailsModel));
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLGZTKSB, "网络故障,退款失败", "refund");
                }
            }
        }
        catch (LaiKeAPIException e)
        {
            throw e;
        }
    }


    /**
     * 退款订单金额结算
     *
     * @param vo                           退款vo实体
     * @param mchId                        店铺id
     * @param mchModel                     店铺信息
     * @param isSupplierPro                是否供应商商品
     * @param proModel                     商品
     * @param orderno                      订单号
     * @param rStatus                      订单状态
     * @param realMoney                    实际退款金额
     * @param mchAccountLogModel           店铺账户明细对象
     * @param maxTuiMoney                  最多退款金额
     * @param remainMoney                  退款剩余金额
     * @param orderHadPayButNotReceiptFlag 待收货
     * @param orderhadReceiptFlag          已收货
     * @return
     */
    private boolean returnOrderMoneySettlement(RefundVo vo, int mchId, MchModel mchModel, boolean isSupplierPro, ProductListModel proModel, String orderno, int rStatus, BigDecimal realMoney, MchAccountLogModel mchAccountLogModel, BigDecimal maxTuiMoney, BigDecimal remainMoney, boolean orderHadPayButNotReceiptFlag, boolean orderhadReceiptFlag, Integer detailId) throws LaiKeAPIException
    {
        //**********************************************************************
        //此详单剩余未退款金额=remainMoney
        // 店铺订单：
        //1:代收货：结算给店铺余额。
        //a: 大于0  account_money = account_money + remainMoney
        // 1:已收货：结算给店铺余额。
        //a: 大于0  account_money = account_money - realMoney
        // 供应商订单：
        //1:代收货：结算给店铺余额。
        //a: 大于0  supplier_banlance = supplier_banlance + remainMoney
        // 1:已收货：结算给店铺余额。
        //a: 大于0  account_money = account_money - 此单所有入账金额
        //a: 大于0  supplier_banlance = supplier_banlance + remainMoney
        //**********************************************************************
        try
        {
            int        count;
            BigDecimal mchAccountMoney;
            boolean    isSettlement;
            mchModel = mchModelMapper.selectByPrimaryKey(mchModel.getId());
            //店铺账户余额
            mchAccountMoney = mchModel.getAccount_money();
            boolean flag = false;
            if (isSupplierPro)
            {
                //标记该商品已经结算
                isSettlement = true;
                //供应商结算金额
                BigDecimal supplierSettlementMoney = BigDecimal.ZERO;
                // 根据供应商ID，查询供应商信息
                SupplierModel supplierModel = supplierModelMapper.selectByPrimaryKey(proModel.getGongyingshang());
                //供应商余额
                BigDecimal surplusAccountMoney = BigDecimal.ZERO;
                if (Objects.isNull(supplierModel))
                {
                    logger.error("供应商信息为空。");
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_GYSXXBCZ, "供应商信息不存在");
                }

                // 供应商余额
                surplusAccountMoney = supplierModel.getSurplus_balance();

                if (orderhadReceiptFlag)
                {
                    if (vo.getPrice().compareTo(mchAccountMoney) > 0)
                    {
                        logger.error("退款金额:{}大于店铺金额:{}", vo.getPrice(), mchAccountMoney);
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TKSBDPYEBZ, "退款失败店铺余额不足");
                    }
                    // 供应商余额入账金额 = 剩余未退款金额
                    supplierSettlementMoney = remainMoney;

                    // 已经收货（因为已经确认收货，钱已经到店铺余额）所以 结算 店铺余额 = 店铺余额 - 退款金额
                    if (mchAccountMoney.compareTo(maxTuiMoney) < 0)
                    {
                        logger.error("退款金额:{}大于店铺金额:{}", maxTuiMoney, mchAccountMoney);
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TKSBDPYEBZ, "退款失败店铺余额不足");
                    }

                    mchAccountMoney = mchAccountMoney.subtract(maxTuiMoney);

                    //减少店铺余额
                    count = mchModelMapper.settlementMchAccountMoneyAfterReceipt(vo.getStoreId(), mchId, maxTuiMoney);
                    if (count < 1)
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TKSB, "退款失败");
                    }
                    mchAccountLogModel.setPrice(realMoney);
                    mchAccountLogModel.setAccount_money(mchAccountMoney);
                    mchAccountLogModel.setType(DictionaryConst.MchAccountLogType.MCHACCOUNTLOG_TYPE_REFUND);
                    mchAccountLogModel.setStatus(DictionaryConst.MchAccountLogStatus.MCHACCOUNTLOG_STATUS_EXPENDITURE);
                    //供应商退款、店铺出账记录拆成退款和供应商两条记录
                    flag = true;
                }
                else
                {
                    // 还未收货（因为还未收货，钱还未到店铺余额）
                    supplierSettlementMoney = remainMoney; // 供应商余额入账金额 = 剩余未退款金额
                }

                //退款这里直接给供应商结算 订单同一状态修改的那个方法那边不再结算供应商余额
                if (supplierSettlementMoney.compareTo(BigDecimal.ZERO) > 0)
                {
                    //增加供应商余额
                    count = supplierModelMapper.addPrice(supplierModel.getId().intValue(), supplierSettlementMoney);
                    if (count < 1)
                    {
                        logger.error("供应商余额修改失败！参数::{}", supplierModel.getId());
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TKSBWLGZ, "退款失败,网络故障");
                    }

                    //退款减去供应商结算金额，这个地方不能减运费，如果只退部分运费的话，供应商结算金额小于运费的话，结算金额会成负数 bug:3272
                    OrderDetailsModel orderDetailsModel = new OrderDetailsModel();
                    orderDetailsModel.setId(detailId);
                    orderDetailsModel = orderDetailsModelMapper.selectByPrimaryKey(orderDetailsModel);
                    if (Objects.nonNull(orderDetailsModel))
                    {
                        //orderDetailsModel.setSupplier_settlement(supplierSettlementMoney.subtract(freight));
                        orderDetailsModel.setSupplier_settlement(supplierSettlementMoney);
                        orderDetailsModelMapper.updateByPrimaryKeySelective(orderDetailsModel);
                    }

                    //生成余额明细
                    count = supplierAccountLogModelMapper.saveAccountLog(vo.getStoreId(), Integer.parseInt(proModel.getGongyingshang()), supplierSettlementMoney, supplierModel.getSurplus_balance(), SupplierAccountLogModel.ENTRY, SupplierAccountLogModel.ORDER, new Date(), orderno, "退款成功，退还剩余未退款金额给供应商");
                    if (count < 1)
                    {
                        logger.error("供应商添加资金记录失败！参数:{}", supplierModel.getId());
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TKSBWLGZ, "退款失败,网络故障");
                    }
                }
            }
            else
            {
                //标记该商品已经结算
                isSettlement = true;
                //刷新店铺信息
                mchModel = mchModelMapper.selectByPrimaryKey(mchModel.getId());
                if (orderHadPayButNotReceiptFlag)
                {
                    //待收货场景退款
                    //详单退款有剩余金额 ：店铺金额 = 店铺金额 + 详单剩余退款金额
                    if (remainMoney.compareTo(BigDecimal.ZERO) > 0)
                    {
                        mchAccountMoney = mchAccountMoney.add(remainMoney);
                        mchAccountLogModel.setPrice(remainMoney);
                        mchAccountLogModel.setAccount_money(mchAccountMoney);
                        mchAccountLogModel.setType(DictionaryConst.MchAccountLogType.MCHACCOUNTLOG_TYPE_REFUND);
                        mchAccountLogModel.setStatus(DictionaryConst.MchAccountLogStatus.MCHACCOUNTLOG_STATUS_INCOME);

                        //增加店铺余额
                        count = mchModelMapper.settlementMchAccountMoneyBeReceipt(vo.getStoreId(), mchId, remainMoney);
                        if (count < 1)
                        {
                            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TKSB, "退款失败");
                        }
                    }
                }
                else if (orderhadReceiptFlag)
                {
                    // 用户已收货 详单支付金额事先已经到到店铺余额账户 ， 此时需要从店铺余额中扣减实际退款金额
                    logger.info("退款增加店铺收入(少退的情况) 结算到店铺id:{} 可提现金额:{}", mchId, remainMoney);
                    // 已经收货（因为已经确认收货，钱已经到店铺余额）
                    if (vo.getPrice().compareTo(mchAccountMoney) > 0)
                    {
                        logger.error("退款金额:{}大于店铺金额:{}", vo.getPrice(), mchAccountMoney);
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TKSBDPYEBZ, "");
                    }
                    if (rStatus == OrderModel.ORDER_FINISH)
                    {
                        mchAccountMoney = mchAccountMoney.subtract(realMoney);

                        mchAccountLogModel.setPrice(realMoney);
                        mchAccountLogModel.setAccount_money(mchAccountMoney);
                        mchAccountLogModel.setType(DictionaryConst.MchAccountLogType.MCHACCOUNTLOG_TYPE_REFUND);
                        mchAccountLogModel.setStatus(DictionaryConst.MchAccountLogStatus.MCHACCOUNTLOG_STATUS_EXPENDITURE);

                        //减少店铺余额
                        count = mchModelMapper.settlementMchAccountMoneyAfterReceipt(vo.getStoreId(), mchId, realMoney);
                        if (count < 1)
                        {
                            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TKSB, "退款失败");
                        }
                        /*logger.debug("开始退还冻结积分");
                        User       clientUser = userBaseMapper.selectByUserId(vo.getStoreId(), clientUserId);
                        Integer    frozenScore = signRecordModelMapper.getScoreNumByType(orderno, SignRecordModel.ScoreType.INTEGRAL_FROZEN);
                        AddScoreVo addScoreVo = new AddScoreVo();
                        addScoreVo.setType(SignRecordModel.ScoreType.INTEGRAL_REFUND_RETURN);
                        addScoreVo.setOrderNo(orderno);
                        addScoreVo.setUserId(clientUserId);
                        addScoreVo.setStoreId(vo.getStoreId());
                        addScoreVo.setScoreOld(clientUser.getScore());
                        addScoreVo.setScore(frozenScore);
                        addScoreVo.setEvent("退还冻结积分");
                        publicIntegralService.addScore(addScoreVo);*/
                        //退还冻结积分
                        //signRecordModelMapper.updateForZenScore(orderno, SignRecordModel.ScoreType.INTEGRAL_FROZEN);

                        //禅道3415 保留冻结积分记录 新增售后退还积分记录
                        SignRecordModel signRecordModel = new SignRecordModel();
                        signRecordModel.setsNo(orderno);
                        signRecordModel.setType(SignRecordModel.ScoreType.INTEGRAL_FROZEN);
                        signRecordModel = signRecordModelMapper.selectOne(signRecordModel);
                        if (Objects.nonNull(signRecordModel))
                        {
                            //禅道4127 已退还的冻结积分，设置解冻时间为null
                            signRecordModel.setFrozen_time(null);
                            signRecordModelMapper.updateByPrimaryKey(signRecordModel);
                            signRecordModel.setId(null);
                            signRecordModel.setSign_time(new Date());
                            signRecordModel.setType(SignRecordModel.ScoreType.INTEGRAL_FROZEN_RETURN);
                            signRecordModel.setRecord("冻结积分退回");
                            signRecordModelMapper.insert(signRecordModel);
                        }
                        else
                        {
                            logger.error("冻结积分记录signRecordModel为null！！！");
                        }

                    }
                }
            }

            if (Objects.nonNull(mchAccountLogModel.getAccount_money()) && mchAccountLogModel.getAccount_money().compareTo(BigDecimal.ZERO) >= 0)
            {
                count = mchAccountLogModelMapper.insertSelective(mchAccountLogModel);
                if (flag)
                {
                    mchAccountLogModel.setId(null);
                    if (maxTuiMoney.subtract(realMoney).compareTo(BigDecimal.ZERO) > 0)
                    {
                        mchAccountLogModel.setPrice(maxTuiMoney.subtract(realMoney));
                        mchAccountLogModel.setAccount_money(mchAccountMoney);
                        mchAccountLogModel.setType(DictionaryConst.MchAccountLogType.MCHACCOUNTLOG_TYPE_SUPPLIER);
                        mchAccountLogModel.setStatus(DictionaryConst.MchAccountLogStatus.MCHACCOUNTLOG_STATUS_EXPENDITURE);
                        count = mchAccountLogModelMapper.insertSelective(mchAccountLogModel);
                    }
                }
                if (count < 1)
                {
                    logger.info("退款记录失败:" + JSON.toJSONString(mchAccountLogModel));
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TKSBWLGZ, "退款失败,网络故障");
                }
            }
            return isSettlement;
        }
        catch (LaiKeAPIException e)
        {
            throw e;
        }
    }

    /**
     * 退款金额原路返回
     *
     * @param vo
     * @param mchPhone       店铺电话
     * @param isSendNotice   是否发送消息
     * @param detailId       订单详情id
     * @param orderId        订单id
     * @param clientUserId   用户id
     * @param payType        支付方式
     * @param realOrderno    支付吊起订单号
     * @param upPaymentPrice 支付金额
     * @throws LaiKeAPIException
     */
    private void returnOrderRefundAmountBackHandle(RefundVo vo, String mchPhone, boolean isSendNotice, int detailId, int orderId,
                                                   String clientUserId, String payType, String realOrderno, BigDecimal upPaymentPrice, Integer refundId) throws LaiKeAPIException
    {
        try
        {
            switch (payType)
            {
                case DictionaryConst.OrderPayType.ORDERPAYTYPE_WALLET_PAY:
                    //钱包支付全额退款
                    publicMemberService.returnUserMoney(vo.getStoreId(), clientUserId, vo.getPrice(), detailId, false);
                    break;
                case DictionaryConst.OrderPayType.ORDERPAYTYPE_ALIPAY:
                case DictionaryConst.OrderPayType.ORDERPAYTYPE_ALIPAY_TMP:
                case DictionaryConst.OrderPayType.ORDERPAYTYPE_PC_ALIPAY:
                case DictionaryConst.OrderPayType.ORDERPAYTYPE_ALIPAY_MINIPAY:
                case DictionaryConst.OrderPayType.ORDERPAYTYPE_ALIPAY_WAP:
                    //发起退款
                    Map<String, String> map = publicAlipayServiceImpl.refundOrder(vo.getStoreId(), orderId, payType, realOrderno, vo.getPrice(), upPaymentPrice);
                    String code = map.get("code");
                    String aliOutRequestNo = MapUtils.getString(map, "ali_out_request_no");
                    //阿里退款单号赋值
                    if (StringUtils.isNotEmpty(aliOutRequestNo))
                    {
                        returnOrderModelMapper.setALiOutRequestNo(refundId,aliOutRequestNo);
                    }
                    if (DictionaryConst.AliApiCode.ALIPAY_ACQ_SELLER_BALANCE_NOT_ENOUGH.equals(code))
                    {
                        //短信通知商家
                        if (isSendNotice)
                        {
                            //获取短信列表
                            Map<String, Object> messageInfo = messageListModelMapper.getMessageListInfoByType(vo.getStoreId(), GloabConst.VcodeCategory.TYPE_NOTICE, GloabConst.VcodeCategory.PAY_REFUND_ORDER);
                            String              content;
                            String              parmaJson;
                            Map<String, String> smsParmaMap = null;
                            if (messageInfo != null)
                            {
                                isSendNotice = true;
                                content = messageInfo.get("content").toString();
                                parmaJson = JSON.toJSONString(SerializePhpUtils.getUnserializeObj(content, Map.class));
                                smsParmaMap = JSON.parseObject(parmaJson, new TypeReference<Map<String, String>>()
                                {
                                });
                            }
                            else
                            {
                                logger.error("商城【{}】未配置通知短信服务!", vo.getStoreId());
                            }
                            publiceService.sendSms(vo.getStoreId(), mchPhone, GloabConst.VcodeCategory.TYPE_NOTICE, vo.getType(), smsParmaMap);
                        }
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TKSB, "退款失败", "refund");
                    }
                    break;
                case DictionaryConst.OrderPayType.ORDERPAYTYPE_APP_WECHAT:
                case DictionaryConst.OrderPayType.ORDERPAYTYPE_MINI_WECHAT:
                case DictionaryConst.OrderPayType.ORDERPAYTYPE_PC_WECHAT:
                case DictionaryConst.OrderPayType.ORDERPAYTYPE_H5_WECHAT:
                case DictionaryConst.OrderPayType.ORDERPAYTYPE_JSAPI_WECHAT:
                    //微信退款
                    logger.info("退款参数-{}-,", JSON.toJSONString(vo));
                    logger.info("退款参数orderId-{},", orderId);
                    logger.info("退款参数payType-{}-,", payType);
                    logger.info("退款参数realOrderno-{},", realOrderno);
                    map = publicWechatServiceImpl.refundOrder(vo.getStoreId(), orderId, payType, realOrderno, vo.getPrice(), upPaymentPrice);
                    //Map<String, Object> dividendRefundMap = publicWechatServiceImpl.dividendRefund(orderno);
                    //logger.info("#########微信分账回退######## {}", JSONObject.toJSONString(dividendRefundMap));
                    logger.info("#########退款返回##### {}", JSONObject.toJSONString(map));
                    break;
                //贝宝退款
                case DictionaryConst.OrderPayType.PAYPAL_PAY:
                    logger.info("退款参数-{}-,", JSON.toJSONString(vo));
                    logger.info("退款参数orderId-{},", orderId);
                    logger.info("退款参数payType-{}-,", payType);
                    logger.info("退款参数realOrderno-{},", realOrderno);
                    map = publicPaypalService.refundOrder(vo.getStoreId(), vo.getSNo(), orderId, payType, realOrderno, vo.getPrice(), Boolean.FALSE, upPaymentPrice, refundId);
                    logger.info("#########退款返回##### {}", JSONObject.toJSONString(map));
                    break;
                //stripe退款
                case DictionaryConst.OrderPayType.STRIPE_PAY:
                    logger.error("退款参数-{}-,", JSON.toJSONString(vo));
                    logger.info("退款参数orderId-{},", orderId);
                    logger.info("退款参数payType-{}-,", payType);
                    logger.info("退款参数realOrderno-{},", realOrderno);
                    map = publicStripeServiceImpl.refundOrder(vo.getStoreId(), vo.getSNo(), orderId, payType, realOrderno, vo.getPrice(), Boolean.FALSE, upPaymentPrice, refundId);
                    logger.error("#########退款返回##### {}", JSONObject.toJSONString(map));
                    break;
                case DictionaryConst.OrderPayType.OFFLINE_PAY:
                    break;
                default:
                    break;
            }
        }
        catch (LaiKeAPIException e)
        {
            throw e;
        }
    }

    /**
     * 库存处理
     *
     * @param vo
     * @param livingRoomId
     * @param goodsId
     * @param isSupplierPro
     * @param proModel
     * @param otype
     * @param attributeId
     * @param goodsNeedNum
     */
    private void returnOrderGoodStockHandle(RefundVo vo, Integer livingRoomId, int goodsId, boolean isSupplierPro, ProductListModel proModel, String otype, int attributeId, BigDecimal goodsNeedNum, String userId) throws LaiKeAPIException
    {
        //只有退货退款需要退库存
        //优化仅退款也需要回滚商品库存 禅道#35100
        //蝉道-56522， 56520问题，因为以前是未发货的时候有仅退款的操作，加了极速退款之后没有这个操作了，需要调整
        //禅道 115 虚拟商品不做库存处理
        try
        {
            int count;
            if ((DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_RECEIVED_REBAKGOODS.equals(vo.getType()) ||
                    DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_AGREE_REBACK_AMT.equals(vo.getType()) ||
                    DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_REFUSE_ARTIFICIAL_OK.equals(vo.getType()))
                    && !DictionaryConst.OrdersType.ORDERS_HEADER_VI.equals(otype))
            {
                //获取商品库存信息
                ConfiGureModel confiGureModel = new ConfiGureModel();
                confiGureModel.setId(attributeId);
                confiGureModel = confiGureModelMapper.selectOne(confiGureModel);
                if (confiGureModel == null)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SPKCBCZ, "商品库存不存在", "refund");
                }
                //供应商商品处理
                Integer cid    = confiGureModel.getId();
                Integer goodId = goodsId;
                if (isSupplierPro)
                {
                    //子供应商商品也需要回滚
                    count = productListModelMapper.updateProductListVolume(goodsNeedNum.negate().intValue(), vo.getStoreId(), goodId);
                    if (count < 1)
                    {
                        logger.info("销量扣减失败 商品id:" + goodId + " 销量扣减数量:" + goodsNeedNum);
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLGZTKSB, "网络故障,退款失败", "refund");
                    }
                    count = confiGureModelMapper.reduceGoodsStockNum(goodsNeedNum.negate().intValue(), cid);
                    if (count < 1)
                    {
                        logger.info("回滚属性库存失败 商品id:" + goodId + " 退货数量:" + goodsNeedNum);
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLGZTKSB, "网络故障,退款失败", "refund");
                    }
                    count = productListModelMapper.addGoodsStockNum(goodId, goodsNeedNum.intValue());
                    if (count < 1)
                    {
                        logger.info("商品库存回滚失败 商品id:" + goodId + " 销量扣减数量:" + goodsNeedNum);
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLGZTKSB, "网络故障,退款失败", "refund");
                    }
                    confiGureModel = confiGureModelMapper.selectByPrimaryKey(confiGureModel.getSupplier_superior());
                    cid = confiGureModel.getId();
                    goodId = proModel.getSupplier_superior();
                    logger.info("正在处理供应商商品id" + goodId + "----->规格id" + cid + "回滚库存、扣减销量、增加总库存操作");
                }
                //预售商品需要回滚所有规格库存
                if (DictionaryConst.WhetherMaven.WHETHER_OK == proModel.getIs_presell())
                {
                    count = confiGureModelMapper.reduceProAllAttrNum(goodsNeedNum.negate().intValue(), goodsId);
                    if (count < 1)
                    {
                        logger.info("回滚属性库存失败 商品id:" + goodId + " 退货数量:" + goodsNeedNum);
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLGZTKSB, "网络故障,退款失败", "refund");
                    }
                    //直播商品需要先判断商品所在的直播是否还在直播，如果是还在直播，需要先还回给直播间商品
                }
                else if (DictionaryConst.OrdersType.ORDERS_HEADER_ZB.equals(otype))
                {
                    //先获取订单状态
                    LivingRoomModel livingRoomModel = livingRoomModelMapper.selectByPrimaryKey(livingRoomId);
                    //如果极速退款时直播没有结束，则返回给直播的商品，如果直播已经结束
                    LivingProductModel livingProductModel = new LivingProductModel();
                    livingProductModel.setLiving_id(livingRoomModel.getId());
                    livingProductModel.setConfig_id(cid);
                    livingProductModel.setRecycle(0);
                    livingProductModel = livingProductModelMapper.selectOne(livingProductModel);
                    livingProductModel.setXl_num(livingProductModel.getXl_num() + goodsNeedNum.negate().intValue());
                    //直播没有结束
                    if (livingRoomModel != null && livingRoomModel.getLiving_status().equals(LivingRoomModel.STATUS_LIVING_STREAMING))
                    {
                        livingProductModelMapper.updateByPrimaryKeySelective(livingProductModel);
                    }
                    else
                    {
                        //直播结束，回滚库存
                        count = confiGureModelMapper.reduceGoodsStockNum(goodsNeedNum.negate().intValue(), cid);
                        if (count < 1)
                        {
                            logger.info("回滚属性库存失败 商品id:" + goodId + " 退货数量:" + goodsNeedNum);
                            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLGZTKSB, "网络故障,退款失败", "refund");
                        }
                        // 增加商品库存
                        count = productListModelMapper.addGoodsStockNum(goodId, goodsNeedNum.intValue());
                        if (count < 1)
                        {
                            logger.info("商品库存回滚失败 商品id:" + goodId + " 销量扣减数量:" + goodsNeedNum);
                            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLGZTKSB, "网络故障,退款失败", "refund");
                        }
                        livingProductModelMapper.updateByPrimaryKeySelective(livingProductModel);
                    }
                }
                //积分订单退款增加库存
                else if (DictionaryConst.OrdersType.ORDERS_HEADER_IN.equals(otype))
                {
                    count = integralGoodsModelMapper.addGoodsStockNum(goodsNeedNum.intValue(),cid,goodId,vo.getStoreId());
                    if (count < 1)
                    {
                        logger.info("商品库存回滚失败 商品id:" + goodId);
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLGZTKSB, "网络故障,退款失败", "refund");
                    }
                }
                //秒杀订单库存增加,现在秒杀订单暂时只能换货和极速退款，所以这里先注释掉
               /* else if(DictionaryConst.OrdersType.ORDERS_HEADER_MS.equals(otype))
                {
                    SecondsActivityModel secondsActivityModel = new SecondsActivityModel();
                    secondsActivityModel.setId(vo.getP_id());
                    secondsActivityModel.setStore_id(vo.getStoreId());
                    secondsActivityModel.setIs_delete(DictionaryConst.ProductRecycle.NOT_STATUS);
                    secondsActivityModel = secondsActivityModelMapper.selectOne(secondsActivityModel);
                    if (Objects.nonNull(secondsActivityModel))
                    {
                        //更新秒杀活动商品库存
                        count = secondsActivityModelMapper.addStockNum(secondsActivityModel.getId(), goodsNeedNum.intValue());
                        if (count < 1)
                        {
                            logger.info("商品库存回滚失败 商品id:" + secondsActivityModel.getGoodsId());
                            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLGZTKSB, "网络故障,退款失败", "refund");
                        }
                        String goodsKey = GloabConst.RedisHeaderKey.SECKILL_GOODS_KEY + secondsActivityModel.getLabel_id();
                        //更新秒杀缓存库存信息
                        Map<String, Object> secGoodsMap = (Map<String, Object>) redisUtil.hget(goodsKey, vo.getP_id().toString());
                        if (MapUtils.isNotEmpty(secGoodsMap))
                        {
                            secGoodsMap.put("secStockNum", MapUtils.getInteger(secGoodsMap, "secStockNum") + goodsNeedNum.intValue());
                            redisUtil.hset(goodsKey, vo.getP_id().toString(), secGoodsMap);
                        }
                    }
                }*/
                else
                {
                    //回滚库存
                    count = confiGureModelMapper.reduceGoodsStockNum(goodsNeedNum.negate().intValue(), cid);
                    if (count < 1)
                    {
                        logger.info("回滚属性库存失败 商品id:" + goodId + " 退货数量:" + goodsNeedNum);
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLGZTKSB, "网络故障,退款失败", "refund");
                    }
                }
                //直播的不走减销量
                if (!DictionaryConst.OrdersType.ORDERS_HEADER_ZB.equals(otype))
                {
                    //减销量
                    count = productListModelMapper.updateProductListVolume(goodsNeedNum.negate().intValue(), vo.getStoreId(), goodId);
                    if (count < 1)
                    {
                        logger.error("销量扣减失败 商品id:" + goodId + " 销量扣减数量:" + goodsNeedNum);
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLGZTKSB, "网络故障,退款失败", "refund");
                    }

                    // 增加商品库存
                    count = productListModelMapper.addGoodsStockNum(goodId, goodsNeedNum.intValue());
                    if (count < 1)
                    {
                        logger.error("商品库存回滚失败 商品id:" + goodId + " 销量扣减数量:" + goodsNeedNum);
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLGZTKSB, "网络故障,退款失败", "refund");
                    }
                }
                //退款成功 增加商品总库存
                String     text       = userId + "退款成功,增加商品库存" + goodsNeedNum;
                StockModel stockModel = new StockModel();
                stockModel.setStore_id(vo.getStoreId());
                stockModel.setProduct_id(goodId);
                stockModel.setAttribute_id(cid);
                stockModel.setTotal_num(confiGureModel.getTotal_num());
                stockModel.setFlowing_num(goodsNeedNum.intValue());
                stockModel.setType(StockModel.StockType.STOCKTYPE_WAREHOUSING);
                stockModel.setContent(text);
                stockModel.setAdd_date(new Date());
                count = stockModelMapper.insertSelective(stockModel);
                if (count < 1)
                {
                    logger.error("添加商品库存失败 参数:" + JSON.toJSONString(stockModel));
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLGZTKSB, "网络故障,退款失败", "refund");
                }
            }
        }
        catch (LaiKeAPIException e)
        {
            throw e;
        }
    }

    /**
     * 优惠券处理
     *
     * @param vo
     * @param refundCoupon
     * @param orderPrice
     * @param orderId
     * @param orderno
     * @param clientUserId
     * @param couponId
     * @param psNo
     */
    private void returnOrderCouponHandler(RefundVo vo, boolean refundCoupon, BigDecimal orderPrice, int orderId, String orderno, String clientUserId, String couponId, String psNo) throws LaiKeAPIException
    {
        try
        {
            if (!StringUtils.isEmpty(couponId) && refundCoupon)
            {
                if (!("0,0".equals(couponId) || "0".equals(couponId)))
                {
                    // 当订单详情使用了优惠券
                    // 订单详情使用的优惠券ID字符串 转数组
                    String[] couponList = couponId.split(SplitUtils.DH);
                    for (int i = 0; i < couponList.length; i++)
                    {
                        String tmpCouponId = couponList[i];
                        if (!"0".equals(tmpCouponId) && null != tmpCouponId)
                        {
                            // 使用了优惠券
                            if (i == 0 || (i == 1 && Strings.isNullOrEmpty(psNo)))
                            {
                                // 使用了店铺优惠券 或 (使用了平台优惠券 并且 不是跨店铺订单)
                                // 根据商城ID、订单号、店铺优惠券ID，查询不是这个订单详情的数据
                                List<Map<String, Object>> otherOrders = orderDetailsModelMapper.getOrderDetailsUseTheCoupon(vo.getStoreId(), orderno, tmpCouponId, orderId);
                                boolean                   flag        = false;
                                //所有详单的优惠和金额为0
                                boolean allAfterDiscountIsZero = true;
                                if (otherOrders != null && otherOrders.size() > 0)
                                {
                                    // 存在(该订单里，还有其它详情使用了这张店铺优惠券)
                                    // 该订单里，有多少详情使用了这张店铺优惠券
                                    int size = otherOrders.size();
                                    // 该订单里，使用了这张店铺优惠券,并退款或退货退款成功的数量
                                    int returnNum = 0;
                                    for (Map<String, Object> otherOrderDetail : otherOrders)
                                    {
                                        int orderStatus = MapUtils.getIntValue(otherOrderDetail, "r_status");
                                        if (orderStatus == DictionaryConst.OrdersStatus.ORDERS_R_STATUS_CLOSE)
                                        {
                                            returnNum++;
                                        }
                                        //优惠后金额不为0
                                        if (BigDecimal.ZERO.compareTo(new BigDecimal(MapUtils.getString(otherOrderDetail, "after_discount"))) != 0)
                                        {
                                            allAfterDiscountIsZero = false;
                                        }
                                    }
                                    if (returnNum == size)
                                    {
                                        flag = true;
                                    }
                                }
                                else
                                {
                                    flag = true;
                                }
                                if (flag)
                                {
                                    // 该订单，使用了这张店铺优惠券的订单商品都退款或退款退款成功
                                    int row = publicCouponService.couponWithOrder(vo.getStoreId(), clientUserId, tmpCouponId, orderno, "update");
                                    if (row == 0)
                                    {
                                        //回滚删除已经创建的订单
                                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLGZTKSB, "网络故障,退款失败", "refund");
                                    }
                                    // =2
                                    // 退回优惠券
                                    row = publicCouponService.updateCoupons(vo.getStoreId(), clientUserId, tmpCouponId, CouponModal.COUPON_TYPE_NOT_USED);
                                    if (row == 0)
                                    {
                                        //回滚删除已经创建的订单
                                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLGZTKSB, "网络故障,退款失败", "refund");
                                    }
                                    //使用了平台优惠卷，优惠金额大于总价。当订单所有商品退款成功时须将支付的0.01元退还 2023-09-12 gp
                                    if (i == 1 && allAfterDiscountIsZero && orderPrice.compareTo(new BigDecimal("0.01")) == 0)
                                    {
                                        vo.setPrice(vo.getPrice().add(orderPrice));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            logger.error("returnOrderCouponHandler>>" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLGZTKSB, "网络故障,退款失败", "returnOrderCouponHandler");
        }
    }

    /**
     * 虚拟商品售后处理
     *
     * @param refundCoupon
     * @param returnOrderInfoMap
     * @return
     */
    private boolean viReturnOrderHandle(boolean refundCoupon, Map<String, Object> returnOrderInfoMap) throws LaiKeAPIException
    {
        try
        {
            if (DataUtils.getStringVal(returnOrderInfoMap, "otype").equals(DictionaryConst.OrdersType.ORDERS_HEADER_VI) && returnOrderInfoMap.containsKey("write_time") && returnOrderInfoMap.containsKey("write_time_id"))
            {
                if (returnOrderInfoMap.containsKey("after_write_off_num") && DataUtils.getIntegerVal(returnOrderInfoMap, "after_write_off_num") > 0)
                {
                    refundCoupon = false;
                }
                //需要预约的商品
                //预约的时间   2024-07-10 05:00-06:00
                String   writeTime = DataUtils.getStringVal(returnOrderInfoMap, "write_time");
                String[] s         = writeTime.split(" ", 2);
                writeTime = s[0];
                Integer            writeTimeId        = DataUtils.getIntegerVal(returnOrderInfoMap, "write_time_id");
                MchStoreWriteModel mchStoreWriteModel = mchStoreWriteModelMapper.selectByPrimaryKey(writeTimeId);
                String             off_num            = mchStoreWriteModel.getOff_num();
                Integer            write_off_num      = mchStoreWriteModel.getWrite_off_num();
                // 格式化时间
                String start_time_ymd = DateUtil.dateFormate(mchStoreWriteModel.getStart_time(), GloabConst.TimePattern.YMD);
                String end_time_ymd   = DateUtil.dateFormate(mchStoreWriteModel.getEnd_time(), GloabConst.TimePattern.YMD);
                // 获取日期范围内的所有日期
                List<String> intervalDate = DateUtil.getIntervalDate(start_time_ymd, end_time_ymd);
                for (int i = 0; i < intervalDate.size(); i++)
                {
                    if (intervalDate.get(i).equals(writeTime))
                    {
                        String[] split = off_num.split(SplitUtils.DH);
                        //如果为无限预约次数，则无需回滚
                        if (write_off_num != null && write_off_num == 0)
                        {
                            break;
                        }
                        //将对应的已预约次数减掉1
                        split[i] = String.valueOf(Integer.parseInt(split[i]) - 1);
                        StringBuilder sb = new StringBuilder();
                        for (int j = 0; j < split.length; j++)
                        {
                            sb.append(split[j]);
                            if (j < split.length - 1)
                            {
                                sb.append(","); // 只在不是最后一个元素时添加逗号
                            }
                        }
                        String modified = sb.toString();
                        mchStoreWriteModel.setOff_num(modified);
                    }
                }
                mchStoreWriteModelMapper.updateByPrimaryKey(mchStoreWriteModel);
            }
            return refundCoupon;
        }
        catch (Exception e)
        {
            logger.error("viReturnOrderHandle>>" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLGZTKSB, "网络故障,退款失败", "viReturnOrderHandle");
        }
    }

    /**
     * 供应商订单售后店铺处理
     *
     * @param vo
     * @param goodsId
     * @param orderId
     * @param orderno
     * @return
     */
    private boolean supplierReturnOrderMchHandle(RefundVo vo, int goodsId, int orderId, String orderno) throws LaiKeAPIException
    {
        try
        {
            if (StringUtils.isEmpty(vo.getOperator()))
            {
                ProductListModel productListModel = productListModelMapper.selectByPrimaryKey(goodsId);
                if (productListModel != null
                        && StringUtils.isNotEmpty(productListModel.getGongyingshang())
                        && DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_AGREE_REBACK.equals(vo.getType()))
                {
                    //修改售后状态
                    ReturnOrderModel updateReturnOrder = new ReturnOrderModel();
                    updateReturnOrder.setId(vo.getId());
                    updateReturnOrder.setIs_agree(DictionaryConst.WhetherMaven.WHETHER_OK);
                    returnOrderModelMapper.updateByPrimaryKeySelective(updateReturnOrder);
                    MessageLoggingModal messageLoggingSave = new MessageLoggingModal();
                    messageLoggingSave.setStore_id(vo.getStoreId());
                    messageLoggingSave.setType(MessageLoggingModal.Type.TYPE_ORDER_RETURN);
                    messageLoggingSave.setTo_url(OrderDataUtils.getOrderRoute(orderno, 0));
                    messageLoggingSave.setParameter(orderId + "");
                    messageLoggingSave.setContent(String.format("订单 %s已申请退款（或退货退款、或换货），请前往退货列表中及时处理！", orderno));
                    messageLoggingSave.setAdd_date(new Date());
                    messageLoggingSave.setSupplier_id(Integer.valueOf(productListModel.getGongyingshang()));
                    messageLoggingModalMapper.insertSelective(messageLoggingSave);
                    if (vo.getType().equals(DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_RECEIVED_REBAKGOODS) ||
                            DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_AGREE_REBACK_GOODS.equals(vo.getType()))
                    {
                        return true;
                    }
                }
            }
            return false;
        }
        catch (Exception e)
        {
            logger.error("supplierReturnOrderMchHandle>>" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLGZTKSB, "网络故障,退款失败", "refund");
        }
    }

    /**
     * 站内信息
     *
     * @param vo
     * @param clientUserId
     */
    private void sendInnerMessage(RefundVo vo, String clientUserId, String tuiTitle, String tuiContext)
    {
        try
        {
            SystemMessageModel systemMessageSave = new SystemMessageModel();
            systemMessageSave.setStore_id(vo.getStoreId());
            systemMessageSave.setSenderid("admin");
            systemMessageSave.setRecipientid(clientUserId);
            systemMessageSave.setTitle(tuiTitle);
            systemMessageSave.setContent(tuiContext);
            systemMessageSave.setType(SystemMessageModel.ReadType.UNREAD);
            systemMessageSave.setTitle(tuiTitle);
            systemMessageSave.setContent(tuiContext);
            systemMessageSave.setTime(new Date());
            systemMessageModelMapper.insertSelective(systemMessageSave);
        }
        catch (Exception e)
        {
            logger.error("sendInnerMessage>>" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLGZTKSB, "网络故障,退款失败", "sendInnerMessage");
        }
    }

    @Override
    public boolean quickRefund(RefundVo vo) throws LaiKeAPIException
    {
        int id      = vo.getId();
        int storeId = vo.getStoreId();
        try
        {
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", vo.getStoreId());
            //店铺联系方式
            String mchPhone = "";
            //是否短信配置 ---> 禅道47551 测试说没有发送短信给商家的功能
            boolean isSendNotice = false;
            //防止连续点击造成多次退款 -并发处理
            if (redisUtil.incr(GloabConst.RedisHeaderKey.REFUND_ORDER_KEY + storeId + "_" + id, 1) > 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CFCZ, "重复操作", "refund");
            }
            //售后记录
            ReturnRecordModel returnRecordSave = new ReturnRecordModel();
            returnRecordSave.setStore_id(vo.getStoreId());
            int count = 0;
            //获取电商平台管理员信息
            CustomerModel customerModel = new CustomerModel();
            customerModel.setId(vo.getStoreId());
            customerModel.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS);
            customerModel = customerModelMapper.selectOne(customerModel);
            if (customerModel != null)
            {
                mchPhone = customerModel.getMobile();
            }
            Integer orderDetailId = null;
            //进入订单详情把未读状态改成已读状态，已读状态的状态不变
            parmaMap.put("id", vo.getOrderDetailId());
            List<Map<String, Object>> orderInfoList = returnOrderModelMapper.getReturnOrderQuickRefund(parmaMap);
            if (orderInfoList != null && orderInfoList.size() > 0)
            {
                Map<String, Object> returnOrderInfoMap = orderInfoList.get(0);
                //订单读取状态
                int read = StringUtils.stringParseInt(returnOrderInfoMap.get("readd") + "");
                //订单id
                int orderId = StringUtils.stringParseInt(returnOrderInfoMap.get("oId").toString());

                //插件id
                Integer p_id = MapUtils.getInteger(returnOrderInfoMap, "goodsId");

                //订单id
                String orderno = returnOrderInfoMap.get("sNo").toString();
                //订单详情id
                int detailId = orderDetailId = StringUtils.stringParseInt(returnOrderInfoMap.get("detialId").toString());
                //订单金额
                BigDecimal orderPrice = new BigDecimal(returnOrderInfoMap.get("z_price").toString());
                //订单原始支付金额
                BigDecimal old_total = new BigDecimal(returnOrderInfoMap.get("old_total").toString());
                //单个商品的实际支付金额
                BigDecimal goodsPayPrice = getOrderPrice(vo.getStoreId(), detailId);
                //买家userid
                String clientUserId = returnOrderInfoMap.get("user_id").toString();
                returnRecordSave.setUser_id(clientUserId);
                //属性id
                int attributeId = StringUtils.stringParseInt(returnOrderInfoMap.get("attributeId").toString());
                ConfiGureModel confiGureModel = new ConfiGureModel();
                confiGureModel.setId(attributeId);
                confiGureModel = confiGureModelMapper.selectOne(confiGureModel);
                if (confiGureModel == null)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SPKCBCZ, "商品库存不存在", "refund");
                }

                //StringUtils.stringParseInt(returnOrderInfoMap.get("goodsId").toString());
                //由于订单详情表的p_id,在插件订单的情况下，存的不是商品id，而是对应的插件活动id，所以急速退款会导致，拿不到对应的商品id，现在统一拿属性表对应的商品id
                int goodsId = confiGureModel.getPid();

                //是否为供应商商品
                boolean          isSupplierPro = false;
                ProductListModel proModel      = productListModelMapper.selectByPrimaryKey(goodsId);
                if (StringUtils.isNotEmpty(proModel.getGongyingshang()) && !Objects.isNull(proModel.getSupplier_superior()))
                {
                    isSupplierPro = true;
                }

                //支付方式
                String payType = returnOrderInfoMap.get("pay").toString();
                //订单商品使用的优惠卷id
                String couponId = returnOrderInfoMap.get("detailCouponId") + "";
                //订单商品运费
                BigDecimal freight = new BigDecimal(returnOrderInfoMap.get("freight").toString());
                //支付流水号
                String realOrderno = MapUtils.getString(returnOrderInfoMap, "real_sno", "");
                //父订单id
                String psNo = MapUtils.getString(returnOrderInfoMap, "p_sNo");
                //订单类型
                String oType = "";
                //调起支付支付金额
                BigDecimal upPaymentPrice = old_total;
                OrderModel orderModel     = new OrderModel();
                orderModel.setsNo(orderno);
                orderModel = orderModelMapper.selectOne(orderModel);
                oType = orderModel.getOtype();
                // 订单拆单过
                if (StringUtils.isNotEmpty(psNo))
                {
                    //根据调起支付所用订单号获取主订单号(未支付先拆单，子订单调起支付所用调起支付所用订单号和总订单相同)
                    String sNo = orderModelMapper.getOrderByRealSno(realOrderno);
                    logger.info("根据吊起支付订单号获取主订单号:{}", sNo);
                    if (StringUtils.isNotEmpty(sNo))
                    {
                        orderModel = new OrderModel();
                        orderModel.setsNo(sNo);
                        orderModel = orderModelMapper.selectOne(orderModel);
                        upPaymentPrice = orderModel.getOld_total();
                    }
                }

                //订单商品总价
                BigDecimal orderDetailPrice = new BigDecimal(returnOrderInfoMap.get("p_price").toString());
                //商品数量
                BigDecimal goodsNeedNum = new BigDecimal(returnOrderInfoMap.get("num").toString());
                orderDetailPrice = orderDetailPrice.multiply(goodsNeedNum);

                if (read == 0)
                {
                    orderModel = new OrderModel();
                    orderModel.setId(orderId);
                    orderModel.setReadd(OrderModel.READ);
                    count = orderModelMapper.updateByPrimaryKeySelective(orderModel);
                    if (count < 1)
                    {
                        logger.info("修改订单已读状态失败 参数:" + JSON.toJSONString(orderModel));
                    }
                }
                //站内推送消息
                String             tuiTitle          = "退货/退款成功!";
                String             tuiContext        = "您的退货/退款申请已通过!";
                SystemMessageModel systemMessageSave = new SystemMessageModel();
                systemMessageSave.setStore_id(vo.getStoreId());
                systemMessageSave.setSenderid("admin");
                systemMessageSave.setRecipientid(clientUserId);
                systemMessageSave.setTitle(tuiTitle);
                systemMessageSave.setContent(tuiContext);
                systemMessageSave.setType(SystemMessageModel.ReadType.UNREAD);

                //实际退款金额
                BigDecimal realMoney = vo.getPrice();
                //订单售后流程
                logger.info("===同意退款===");
                //获取实际支付金额
                logger.info("同意订单详情id{}退款,订单商品付款金额{},实际退款金额{}", detailId, goodsPayPrice, vo.getPrice());
                // 组合支付不要了
                if (DictionaryConst.OrderPayType.ORDERPAYTYPE_TT_ALIPAY.equals(payType))
                {
                    payType = DictionaryConst.OrderPayType.ORDERPAYTYPE_ALIPAY;
                }
                else if (DictionaryConst.OrderPayType.ORDERPAYTYPE_BAIDU_PAY.equals(payType))
                {
                    payType = DictionaryConst.OrderPayType.ORDERPAYTYPE_WALLET_PAY;
                }
                //获取短信列表
                Map<String, Object> messageInfo = messageListModelMapper.getMessageListInfoByType(vo.getStoreId(), GloabConst.VcodeCategory.TYPE_NOTICE, GloabConst.VcodeCategory.PAY_REFUND_ORDER);
                String              content;
                String              parmaJson;
                Map<String, String> smsParmaMap = null;
                if (messageInfo != null)
                {
                    isSendNotice = true;
                    content = messageInfo.get("content").toString();
                    parmaJson = JSON.toJSONString(SerializePhpUtils.getUnserializeObj(content, Map.class));
                    smsParmaMap = JSON.parseObject(parmaJson, new TypeReference<Map<String, String>>()
                    {
                    });
                }
                else
                {
                    logger.error("商城【{}】未配置通知短信服务!", vo.getStoreId());
                }
                logger.info("退款信息payType，{}", payType);
                //获取商品库存信息

                //供应商商品处理
                Integer cid    = confiGureModel.getId();
                Integer goodId = goodsId;
                if (isSupplierPro)
                {
                    //极速退款供应商结算金额处理
                    OrderDetailsModel orderDetailsModel = orderDetailsModelMapper.selectByPrimaryKey(detailId);
                    orderDetailsModel.setSupplier_settlement(BigDecimal.ZERO);
                    count = orderDetailsModelMapper.updateByPrimaryKeySelective(orderDetailsModel);
                    if (count < 1)
                    {
                        logger.info("供应商结算金额归零 参数:" + JSON.toJSONString(orderDetailsModel));
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLGZTKSB, "供应商结算金额异常,退款失败", "refund");
                    }
                    //子供应商商品也需要回滚
                    count = productListModelMapper.updateProductListVolume(goodsNeedNum.negate().intValue(), vo.getStoreId(), goodId);
                    if (count < 1)
                    {
                        logger.info("销量扣减失败 商品id:" + goodId + " 销量扣减数量:" + goodsNeedNum);
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLGZTKSB, "网络故障,退款失败", "refund");
                    }
                    count = confiGureModelMapper.reduceGoodsStockNum(goodsNeedNum.negate().intValue(), cid);
                    if (count < 1)
                    {
                        logger.info("回滚属性库存失败 商品id:" + goodId + " 退货数量:" + goodsNeedNum);
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLGZTKSB, "网络故障,退款失败", "refund");
                    }
                    count = productListModelMapper.addGoodsStockNum(goodId, goodsNeedNum.intValue());
                    if (count < 1)
                    {
                        logger.info("商品库存回滚失败 商品id:" + goodId + " 销量扣减数量:" + goodsNeedNum);
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLGZTKSB, "网络故障,退款失败", "refund");
                    }
                    confiGureModel = confiGureModelMapper.selectByPrimaryKey(confiGureModel.getSupplier_superior());
                    cid = confiGureModel.getId();
                    goodId = proModel.getSupplier_superior();
                    logger.info("正在处理供应商商品id" + goodId + "----->规格id" + cid + "回滚库存、扣减销量、增加总库存操作");
                }

                //预售商品需要回滚所有规格库存
                if (DictionaryConst.WhetherMaven.WHETHER_OK == proModel.getIs_presell())
                {
                    count = confiGureModelMapper.reduceProAllAttrNum(goodsNeedNum.negate().intValue(), goodsId);
                    if (count < 1)
                    {
                        logger.info("回滚属性库存失败 商品id:" + goodId + " 退货数量:" + goodsNeedNum);
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLGZTKSB, "网络故障,退款失败", "refund");
                    }
                    PreSellGoodsModel preSellGoodsModel = new PreSellGoodsModel();
                    preSellGoodsModel.setProduct_id(goodId);
                    preSellGoodsModel.setIs_delete(0);
                    preSellGoodsModel = preSellGoodsMapper.selectOne(preSellGoodsModel);

                    //250718 预售商品目前没有库存设置
//                    preSellGoodsModel.setSurplus_num(preSellGoodsModel.getSurplus_num() + goodsNeedNum.intValue());
                    preSellGoodsMapper.updateByPrimaryKeySelective(preSellGoodsModel);
                }
                else if (DictionaryConst.OrdersType.ORDERS_HEADER_ZB.equals(oType))
                {
                    //先获取订单状态
                    LivingRoomModel livingRoomModel = livingRoomModelMapper.selectByPrimaryKey(returnOrderInfoMap.get("living_room_id"));
                    //如果退款时直播没有结束，则返回给直播的商品，如果直播已经结束
                    LivingProductModel livingProductModel = new LivingProductModel();
                    livingProductModel.setLiving_id(livingRoomModel.getId());
                    livingProductModel.setConfig_id(cid);
                    livingProductModel.setRecycle(0);
                    livingProductModel = livingProductModelMapper.selectOne(livingProductModel);
                    livingProductModel.setXl_num(livingProductModel.getXl_num() + goodsNeedNum.negate().intValue());
                    //直播没有结束
                    if (livingRoomModel != null && livingRoomModel.getLiving_status().equals(LivingRoomModel.STATUS_LIVING_STREAMING))
                    {
                        livingProductModelMapper.updateByPrimaryKeySelective(livingProductModel);
                    }
                    else
                    {
                        //直播结束，回滚库存
                        count = confiGureModelMapper.reduceGoodsStockNum(goodsNeedNum.negate().intValue(), cid);
                        if (count < 1)
                        {
                            logger.info("回滚属性库存失败 商品id:" + goodId + " 退货数量:" + goodsNeedNum);
                            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLGZTKSB, "网络故障,退款失败", "refund");
                        }
                        // 增加商品库存
                        count = productListModelMapper.addGoodsStockNum(goodId, goodsNeedNum.intValue());
                        if (count < 1)
                        {
                            logger.info("商品库存回滚失败 商品id:" + goodId + " 销量扣减数量:" + goodsNeedNum);
                            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLGZTKSB, "网络故障,退款失败", "refund");
                        }
                        livingProductModelMapper.updateByPrimaryKeySelective(livingProductModel);
                    }
                }
                //积分订单退款增加库存
                else if (DictionaryConst.OrdersType.ORDERS_HEADER_IN.equals(oType))
                {
                    count = integralGoodsModelMapper.addGoodsStockNum(goodsNeedNum.intValue(),cid,goodId,storeId);
                    if (count < 1)
                    {
                        logger.info("商品库存回滚失败 商品id:" + goodId);
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLGZTKSB, "网络故障,退款失败", "refund");
                    }
                }
                //秒杀订单库存增加
                else if(DictionaryConst.OrdersType.ORDERS_HEADER_MS.equals(oType))
                {
                    SecondsActivityModel secondsActivityModel = new SecondsActivityModel();
                    secondsActivityModel.setId(p_id);
                    secondsActivityModel.setStore_id(vo.getStoreId());
                    secondsActivityModel.setIs_delete(DictionaryConst.ProductRecycle.NOT_STATUS);
                    secondsActivityModel = secondsActivityModelMapper.selectOne(secondsActivityModel);
                    if (Objects.nonNull(secondsActivityModel))
                    {
                        //更新秒杀活动商品库存
                        count = secondsActivityModelMapper.addStockNum(secondsActivityModel.getId(), goodsNeedNum.intValue());
                        if (count < 1)
                        {
                            logger.info("商品库存回滚失败 商品id:" + secondsActivityModel.getGoodsId());
                            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLGZTKSB, "网络故障,退款失败", "refund");
                        }
                        String goodsKey = GloabConst.RedisHeaderKey.SECKILL_GOODS_KEY + secondsActivityModel.getLabel_id();
                        //更新秒杀缓存库存信息
                        Map<String, Object> secGoodsMap = (Map<String, Object>) redisUtil.hget(goodsKey, p_id.toString());
                        if (MapUtils.isNotEmpty(secGoodsMap))
                        {
                            secGoodsMap.put("secStockNum", MapUtils.getInteger(secGoodsMap, "secStockNum") + goodsNeedNum.intValue());
                            redisUtil.hset(goodsKey, p_id.toString(), secGoodsMap);
                        }
                    }
                }
                else
                {
                    //回滚库存
                    count = confiGureModelMapper.reduceGoodsStockNum(goodsNeedNum.negate().intValue(), cid);
                    if (count < 1)
                    {
                        logger.info("回滚属性库存失败 商品id:" + goodId + " 退货数量:" + goodsNeedNum);
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLGZTKSB, "网络故障,退款失败", "refund");
                    }
                }
                //直播的订单不走这个减销量
                if (!DictionaryConst.OrdersType.ORDERS_HEADER_ZB.equals(oType))
                {
                    //减销量
                    count = productListModelMapper.updateProductListVolume(goodsNeedNum.negate().intValue(), vo.getStoreId(), goodId);
                    if (count < 1)
                    {
                        logger.info("销量扣减失败 商品id:" + goodId + " 销量扣减数量:" + goodsNeedNum);
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLGZTKSB, "网络故障,退款失败", "refund");
                    }
                    // 增加商品库存
                    count = productListModelMapper.addGoodsStockNum(goodId, goodsNeedNum.intValue());
                    if (count < 1)
                    {
                        logger.info("商品库存回滚失败 商品id:" + goodId + " 销量扣减数量:" + goodsNeedNum);
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLGZTKSB, "网络故障,退款失败", "refund");
                    }
                }
                    //退款成功 增加商品总库存
                    String     text       = "退款成功,增加商品库存" + goodsNeedNum;
                    StockModel stockModel = new StockModel();
                    stockModel.setStore_id(vo.getStoreId());
                    stockModel.setProduct_id(goodId);
                    stockModel.setAttribute_id(cid);
                    stockModel.setTotal_num(confiGureModel.getTotal_num());
                    stockModel.setFlowing_num(goodsNeedNum.intValue());
                    stockModel.setType(StockModel.StockType.STOCKTYPE_WAREHOUSING);
                    stockModel.setContent(text);
                    stockModel.setAdd_date(new Date());
                    count = stockModelMapper.insertSelective(stockModel);
                    if (count < 1)
                    {
                        logger.info("添加商品库存失败 参数:" + JSON.toJSONString(stockModel));
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLGZTKSB, "网络故障,退款失败", "refund");
                    }
                //修改订单状态为关闭
                OrderDetailsModel orderDetailsModel = new OrderDetailsModel();
                orderDetailsModel.setId(detailId);
                orderDetailsModel.setR_status(DictionaryConst.OrdersStatus.ORDERS_R_STATUS_CLOSE);
                orderDetailsModel.setCommission(new BigDecimal(0));
                orderDetailsModel.setAudit_time(new Date());
                count = orderDetailsModelMapper.updateByPrimaryKeySelective(orderDetailsModel);
                //如果是直播商品，需要删除佣金信息
                if (DictionaryConst.OrdersType.ORDERS_HEADER_ZB.equals(oType))
                {
                    LivingCommissionModel livingCommissionModel = new LivingCommissionModel();
                    livingCommissionModel.setS_no(orderno);
                    livingCommissionModel.setRecycle(0);
                    livingCommissionModel = livingCommissionModelMapper.selectOne(livingCommissionModel);
                    livingCommissionModel.setRecycle(1);
                    livingCommissionModelMapper.updateByPrimaryKeySelective(livingCommissionModel);
                }
                if (count < 1)
                {
                    logger.info("关闭订单失败 参数:" + JSON.toJSONString(orderDetailsModel));
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLGZTKSB, "网络故障,退款失败", "refund");
                }

                //如果订单商品使用了优惠卷
                //**********************************************************************
                //**********************************************************************
                //三、如果订单商品使用了优惠卷
                //**********************************************************************
                //**********************************************************************
                //是否退还优惠券
                boolean refundCoupon = true;
                returnOrderCouponHandler(vo, refundCoupon, orderPrice, orderId, orderno, clientUserId, couponId, psNo);

                //判断是否为预售商品，修改预售订单表的数据
                PreSellRecordModel preSellRecordModel = new PreSellRecordModel();
                preSellRecordModel.setsNo(orderno);
                List<PreSellRecordModel> select = preSellRecordModelMapper.select(preSellRecordModel);
                if (!Objects.isNull(select) && select.size() > 0)
                {
                    for (PreSellRecordModel model : select)
                    {
                        model.setIs_refund(1);
                        preSellRecordModelMapper.updateByPrimaryKeySelective(model);
                    }
                }
                //如果是退款详单是该订单下同商品不同规格最后一个商品则会退给用户运费 反之则不退
                //是否是该订单下最后一个同商品不同规格还未结算和关闭的的商品 用于判断是否退给用户运费
                List<OrderDetailsModel> orderDetailsForPro = orderDetailsModelMapper.getSameProDetailNotCloseAndSettlement(vo.getStoreId(), orderno, goodsId);
                if (orderDetailsForPro.size() == 1)
                {
                    //供应商商品运费逻辑处理 如果为订单中最后一个同商品不同规格的数据进行退款 则供应商运费数据标记已结算
                    if (isSupplierPro)
                    {
                        SupplierOrderFrightModel supplierOrderFrightModel = new SupplierOrderFrightModel();
                        supplierOrderFrightModel.setStore_id(vo.getStoreId());
                        supplierOrderFrightModel.setsNo(orderno);
                        supplierOrderFrightModel.setDetail_id(goodsId);
                        supplierOrderFrightModel = supplierOrderFrightModelMapper.selectOne(supplierOrderFrightModel);
                        if (supplierOrderFrightModel != null)
                        {
                            vo.setPrice(vo.getPrice().add(supplierOrderFrightModel.getFreight()));
                            realMoney = realMoney.add(supplierOrderFrightModel.getFreight());
                            supplierOrderFrightModel.setFreight(BigDecimal.ZERO);
                            supplierOrderFrightModel.setIs_settlement(DictionaryConst.WhetherMaven.WHETHER_OK);
                            supplierOrderFrightModelMapper.updateByPrimaryKeySelective(supplierOrderFrightModel);
                        }
                    }
                    else
                    {
                        vo.setPrice(vo.getPrice().add(freight));
                        realMoney = realMoney.add(freight);
                    }
                }
                else
                {
                    //同商品不同规格的详单只会有一个详单会有运费 所以需要判断如果退款的是有运费的详单则会将运费保存在另外一个没运费的详单中去(用于结算 供应商订单除外因为供应商订单有单独的运费信息表)
                    if ((freight != null && freight.compareTo(BigDecimal.ZERO) > 0) && !isSupplierPro)
                    {
                        OrderDetailsModel sameProOrderDetail = orderDetailsModelMapper.getSameProOrderDetail(vo.getStoreId(), orderno, goodsId, detailId);
                        if (sameProOrderDetail != null)
                        {
                            sameProOrderDetail.setFreight(freight);
                            orderDetailsModelMapper.updateByPrimaryKeySelective(sameProOrderDetail);
                            OrderDetailsModel currentOrderDetail = orderDetailsModelMapper.selectByPrimaryKey(detailId);
                            currentOrderDetail.setFreight(BigDecimal.ZERO);
                            orderDetailsModelMapper.updateByPrimaryKeySelective(currentOrderDetail);
                        }
                    }
                }

                count = orderModelMapper.updateOrderPrice(vo.getStoreId(), orderno, vo.getPrice(), freight, orderDetailPrice);
                if (count < 1)
                {
                    logger.info(String.format("退款订单价格扣减失败 订单号:%s 扣减订单总金额:%s 扣减运费:%s 扣减订单总商品金额:%s", orderno, vo.getPrice(), freight, orderDetailPrice));
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLGZTKSB, "网络故障,退款失败", "refund");
                }

                //退款流程 原路返回
                returnOrderRefundAmountBackHandle(vo, mchPhone, isSendNotice, detailId, orderId, clientUserId, payType, realOrderno, upPaymentPrice, null);

                //统一订单状态
                publicOrderService.updateOrderState(vo.getStoreId(), orderno, vo.getReType());

                //修改售后状态
                ReturnOrderModel updateReturnOrder = new ReturnOrderModel();
                updateReturnOrder.setId(vo.getId());
                updateReturnOrder.setR_type(vo.getType());
                updateReturnOrder.setR_content(vo.getText());
                if (realMoney.compareTo(BigDecimal.ZERO) > 0)
                {
                    //实际退款金额
                    updateReturnOrder.setReal_money(realMoney);
                }
                updateReturnOrder.setAudit_time(new Date());
                count = returnOrderModelMapper.updateByPrimaryKeySelective(updateReturnOrder);
                if (count < 1)
                {
                    logger.info("订单售后状态失败 参数:" + JSON.toJSONString(updateReturnOrder));
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLGZTKSB, "网络故障,退款失败", "refund");
                }
                //重新生成一条售后记录 每次操作都增加一条售后记录 add by trick 2023-02-24 10:29:02
                returnRecordSave.setUser_id(clientUserId);
                returnRecordSave.setStore_id(vo.getStoreId());
                returnRecordSave.setRe_type(vo.getReType());
                returnRecordSave.setR_type(vo.getType());
                returnRecordSave.setsNo(orderno);
                returnRecordSave.setP_id(vo.getId());
                returnRecordSave.setMoney(realMoney);
                returnRecordSave.setProduct_id(goodsId);
                returnRecordSave.setAttr_id(attributeId);
                returnRecordSave.setRe_time(new Date());
                count = returnRecordModelMapper.insertSelective(returnRecordSave);
                if (count < 1)
                {
                    logger.info("添加售后信息失败 参数:" + JSON.toJSONString(returnRecordSave));
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLGZSHJLSB, "网络故障,售后记录失败", "returnData");
                }
                //TODO 【微信推送暂时不做】 发送微信小程序推送退款信息
                //站内推送退款信息
                systemMessageSave.setTitle(tuiTitle);
                systemMessageSave.setContent(tuiContext);
                systemMessageSave.setTime(new Date());
                systemMessageModelMapper.insertSelective(systemMessageSave);
            }
            else
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SHDDBCZ, "售后订单不存在", "refund");
            }
            //最后再判断一下订单是否可以售后
            ReturnOrderModel returnOrderModelNew = returnOrderModelMapper.getReturnNewInfoByDetailId(orderDetailId);
            if (returnOrderModelNew == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SHDDYCX, "售后订单已撤销", "refund");
            }
            return true;
        }
        catch (ClassCastException c)
        {
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSLXBZQ, "参数类型不正确", "refund");
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("售后 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "refund");
        }
        finally
        {
            //删除重复操作标识
            redisUtil.del(GloabConst.RedisHeaderKey.REFUND_ORDER_KEY + storeId + "_" + id);
        }
    }

    @Override
    public Map<String, Object> adminQuickRefund(RefundVo vo)
    {
        {
            Map<String, Object> res        = new HashMap<>();
            OrderModel          orderModel = new OrderModel();
            List<String>        goodsNames = new ArrayList<>();
            BigDecimal          total      = BigDecimal.ZERO;
            try
            {
                String sNo = vo.getSNo();
                orderModel.setsNo(sNo);
                orderModel = orderModelMapper.selectOne(orderModel);
                if (Objects.isNull(orderModel))
                {
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DDXXBCZ, "订单信息不存在", "quickRefund");
                    }
                }

                Integer storeId = orderModel.getStore_id();
                //针对普通订单未发货状态进行极速退款
                if (orderModel.getStatus().equals(DictionaryConst.OrdersStatus.ORDERS_R_STATUS_CONSIGNMENT) && orderModel.getOtype().equals(DictionaryConst.OrdersType.ORDERS_HEADER_GM))
                {
                    String        userId             = orderModel.getUser_id();
                    List<Integer> orderDetailsIdList = orderDetailsModelMapper.getOrderDetailsIdByNO(sNo);
                    if (CollectionUtils.isEmpty(orderDetailsIdList))
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DDXQHQSB, "订单详情获取失败", "quickRefund");
                    }
                    for (Integer orderDetailId : orderDetailsIdList)
                    {
                        OrderDetailsModel orderDetailsModel = new OrderDetailsModel();
                        orderDetailsModel.setStore_id(storeId);
                        orderDetailsModel.setUser_id(userId);
                        orderDetailsModel.setId(orderDetailId);
                        orderDetailsModel = orderDetailsModelMapper.selectOne(orderDetailsModel);
                        if (Objects.isNull(orderDetailsModel))
                        {
                            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DDXXBCZ, "订单信息不存在", "quickRefund");
                        }
                        if (vo.getMchId() != 0)
                        {
                            if (vo.getMchId() != orderDetailsModel.getMch_id())
                            {
                                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_MDBHBZQ, "门店编号不正确", "refund");
                            }
                        }
                        goodsNames.add(orderDetailsModel.getP_name());
                        //添加售后订单信息
                        ReturnOrderModel returnOrderModel = new ReturnOrderModel();
                        returnOrderModel.setStore_id(storeId);
                        returnOrderModel.setUser_id(userId);
                        returnOrderModel.setsNo(orderDetailsModel.getR_sNo());
                        returnOrderModel.setP_id(orderDetailsModel.getId());
                        returnOrderModel.setPid(orderDetailsModel.getP_id());
                        returnOrderModel.setRe_type(2);
                        returnOrderModel.setContent("极速退款");
                        returnOrderModel.setR_type(DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_QUICK_REFUND);
                        returnOrderModel.setSid(Integer.parseInt(orderDetailsModel.getSid()));
                        //获取可以退款的金额
                        BigDecimal refundPrice = publicOrderService.getOrderPrice(orderDetailId, storeId);
                        total = total.add(refundPrice);
                        refundPrice = refundPrice.add(orderDetailsModel.getFreight());
                        returnOrderModel.setRe_money(refundPrice);
                        returnOrderModel.setRe_apply_money(refundPrice);
                        returnOrderModel.setRe_photo("");
                        returnOrderModel.setRe_time(new Date());
                        int count = returnOrderModelMapper.insertSelective(returnOrderModel);
                        if (count < 1)
                        {
                            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SCSHDDSB, "生成售后订单失败", "quickRefund");
                        }

                        ReturnRecordModel returnRecordModel = new ReturnRecordModel();
                        returnRecordModel.setUser_id(userId);
                        returnRecordModel.setStore_id(storeId);
                        returnRecordModel.setRe_type(2);
                        returnRecordModel.setR_type(DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_EXAMEWAIT_STATUS);
                        returnRecordModel.setsNo(orderDetailsModel.getR_sNo());
                        returnRecordModel.setP_id(returnOrderModel.getId());
                        returnRecordModel.setMoney(refundPrice);
                        returnRecordModel.setRe_photo("");
                        returnRecordModel.setProduct_id(orderDetailsModel.getP_id());
                        returnRecordModel.setAttr_id(Integer.parseInt(orderDetailsModel.getSid()));
                        returnRecordModel.setRe_time(new Date());
                        count = returnRecordModelMapper.insertSelective(returnRecordModel);
                        if (count < 1)
                        {
                            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLGZSHJLSB, "网络故障,售后记录失败", "returnData");
                        }

                        //操作记录表
                        RecordModel recordModel = new RecordModel();
                        recordModel.setStore_id(storeId);
                        recordModel.setUser_id(userId);
                        Map<String, Object> tempMap = new HashMap<>(16);
                        tempMap.put("r_sNo", orderDetailsModel.getR_sNo());
                        tempMap.put("r_status", orderDetailsModel.getR_status());
                        tempMap.put("order_details_id", orderDetailsModel.getId());
                        recordModel.setEvent(JSON.toJSONString(tempMap));
                        count = recordModelMapper.insertSelective(recordModel);
                        if (count < 1)
                        {
                            logger.error("订单{},操作记录失败", orderDetailsModel.getR_sNo());
                        }

                        RefundVo refundVo = new RefundVo();
                        refundVo.setStoreId(storeId);
                        refundVo.setId(returnOrderModel.getId());
                        refundVo.setPrice(refundPrice);
                        refundVo.setSNo(orderDetailsModel.getR_sNo());
                        refundVo.setType(DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_QUICK_REFUND);
                        refundVo.setOrderDetailId(orderDetailsModel.getId());
                        refundVo.setReType(2);
                        //极速退款
                        this.quickRefund(refundVo);

                        //极速退款删除原来的订单提醒记录
                        MessageLoggingModal messageLoggingSave = new MessageLoggingModal();
                        messageLoggingSave.setParameter(String.valueOf(orderModel.getId()));
                        List<MessageLoggingModal> messageLoggingModalList = messageLoggingModalMapper.select(messageLoggingSave);
                        if (CollectionUtils.isNotEmpty(messageLoggingModalList))
                        {
                            for (MessageLoggingModal messageLoggingModal : messageLoggingModalList)
                            {
                                messageLoggingModal.setRead_or_not(1);
                                messageLoggingModal.setIs_popup(1);
                                messageLoggingModalMapper.updateByPrimaryKeySelective(messageLoggingModal);
                            }
                        }
                    }
                }
                res.put("RefundType", DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_QUICK_REFUND);
                res.put("product_title", goodsNames);
                res.put("sNo", sNo);
                res.put("time", DateUtil.dateFormate(new Date(), GloabConst.TimePattern.YMDHMS));
                res.put("refund_amount", total);
                res.put("type", 2);
            }
            catch (LaiKeAPIException l)
            {
                throw l;
            }
            catch (Exception e)
            {
                logger.error("极速退款", e);
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常");
            }
            return res;
        }
    }

    @Override
    public Map<String, Object> refundPageById(int storeId, int id) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            //获取售后订单细信息
            Map<String, Object> refundInfoMap = returnOrderModelMapper.getReturnOrderMap(storeId, id, null);

            if (refundInfoMap != null && !refundInfoMap.isEmpty())
            {
                //商品id
                int pid = Integer.parseInt(refundInfoMap.get("goodsId").toString());

                //返回退款积分
                String sNo = MapUtils.getString(refundInfoMap, "sNo");
                if (StringUtils.isEmpty(sNo))
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DDXXBCZ, "订单号不能为空", "refundPageById");
                }
                else
                {
                    OrderModel orderModel = orderModelMapper.selectBySno(sNo);
                    Integer    allow      = orderModel.getAllow();
                    refundInfoMap.put("allow", allow);
                }

                //判断是否为供应商订单
                Integer gysId = MapUtils.getInteger(refundInfoMap, "gys");
                if (Objects.nonNull(gysId))
                {
                    refundInfoMap.put("supplierPrice", MapUtils.getDoubleValue(refundInfoMap, "yprice"));
                }

                refundInfoMap.put("pid", pid);
                //订单详情id
                int orderDetailId = Integer.parseInt(refundInfoMap.get("p_id").toString());
                //售后处理状态
                int rtype  = Integer.parseInt(refundInfoMap.get("rtype").toString());
                int reType = MapUtils.getIntValue(refundInfoMap, "re_type");
                //订单号
                String orderno = refundInfoMap.get("sNo").toString();

                //加购商品列表
                List<Map<String, Object>> addGoodList = getAddGoodList(orderno, storeId);
                if (CollectionUtils.isNotEmpty(addGoodList))
                {
                    resultMap.put("addGoodsList", addGoodList);
                }

                String sheng   = DataUtils.getStringVal(refundInfoMap, "sheng", "");
                String shi     = DataUtils.getStringVal(refundInfoMap, "shi", "");
                String xian    = DataUtils.getStringVal(refundInfoMap, "xian", "");
                String address = DataUtils.getStringVal(refundInfoMap, "address", "");
                refundInfoMap.put("address", new StringBuilder().append(sheng).append(shi).append(xian).append(address));


                //获取商品信息
                ProductListModel productListModel = new ProductListModel();
                productListModel.setId(pid);
                //如果为虚拟商品则修改查询条件
                // todo sjw
                if (refundInfoMap.containsKey("commodity_type") && DataUtils.getStringVal(refundInfoMap, "commodity_type").equals(ProductListModel.COMMODITY_TYPE.virtual.toString()))
                {
                    productListModel.setCommodity_type(ProductListModel.COMMODITY_TYPE.virtual);
                }
                productListModel = productListModelMapper.selectOne(productListModel);
                if (productListModel == null)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SPBCZ, "商品不存在", "refundPageById");
                }
                String imgUrl = publiceService.getImgPath(productListModel.getImgurl(), storeId);
                refundInfoMap.put("imgurl", imgUrl);

                //商品金额
                BigDecimal goodsPrice = new BigDecimal(refundInfoMap.get("p_price").toString());
                //实付金额
                BigDecimal paidPrice = new BigDecimal(refundInfoMap.get("after_discount").toString());
                //数量
                BigDecimal needNum = new BigDecimal(refundInfoMap.get("num").toString());
                //判断商品类型，0.实物商品 1.虚拟商品 2.虚拟商品无需核销 3.虚拟商品需要核销
                int type = 0;
                if (productListModel.getWrite_off_settings() != null)
                {
                    if (productListModel.getWrite_off_settings() == 1)
                    {
                        //线下核销
                        type = ProductListModel.COMMODITY_TYPE.virtual_need_write;
                    }
                    else
                    {
                        type = ProductListModel.COMMODITY_TYPE.virtual_not_need_write;
                    }
                }
                //运费
                BigDecimal yunfei = new BigDecimal(refundInfoMap.get("freight").toString());
                //小计
                BigDecimal subtotal = goodsPrice.multiply(needNum).add(yunfei);
                //合计
                BigDecimal total = paidPrice.add(yunfei);
                if (CollectionUtils.isNotEmpty(addGoodList))
                {
                    double addTotal = addGoodList.stream()
                            .mapToDouble(map -> MapUtils.getDoubleValue(map, "z_price"))
                            .sum();
                    //合计加上加购商品的总计
                    total = total.add(new BigDecimal(addTotal));
                }
                //优惠金额 ==小计 - 合计
                BigDecimal discountAmount = subtotal.subtract(total);
                refundInfoMap.put("z_price", subtotal);
                refundInfoMap.put("total", total);
                refundInfoMap.put("discountAmount", discountAmount);
                if (orderno.contains(DictionaryConst.OrdersType.ORDERS_HEADER_IN))
                {
                    refundInfoMap.put("discountAmount", BigDecimal.ZERO);
                }
                //获取售后凭证图
                List<String>         rePhotoImgUrlList = new ArrayList<>();
                String               rePhoto           = String.valueOf(refundInfoMap.get("re_photo"));
                Map<Integer, String> rePhotoMap        = DataUtils.cast(SerializePhpUtils.getUnserializeObj(rePhoto, Map.class));
                if (rePhotoMap != null)
                {
                    for (Integer key : rePhotoMap.keySet())
                    {
                        String photoImgUrl = rePhotoMap.get(key);
                        photoImgUrl = publiceService.getImgPath(photoImgUrl, storeId);
                        rePhotoImgUrlList.add(photoImgUrl);
                    }
                }

                //region 显示回寄物流信息
                List<ReturnGoodsModel> returnGoodsModelList = null;
                ReturnGoodsModel       returnGoodsModel     = new ReturnGoodsModel();
                returnGoodsModel.setStore_id(storeId);
                returnGoodsModel.setOid(String.valueOf(orderDetailId));
                returnGoodsModel.setRe_id(id);
                returnGoodsModelList = returnGoodsModelMapper.select(returnGoodsModel);
                //endregion

                //标记已读
                orderModelMapper.updateIsReadd(storeId, orderno, OrderModel.READ);

                //查询售后记录
                List<RefundResultView>  refundResultViewList = new ArrayList<>();
                List<ReturnRecordModel> returnRecordModels   = returnRecordModelMapper.getReturnRecord(storeId, id);
                for (ReturnRecordModel recordModel : returnRecordModels)
                {
                    //视图参数
                    RefundResultView refundResultView = new RefundResultView();
                    //审核结果
                    String examineResultStr = "";
                    //拒绝原因
                    String refuseText = "";
                    int    rTypeOld   = recordModel.getR_type();
                    int    reTypeOld  = recordModel.getRe_type();
                    if (rTypeOld == 0)
                    {
                        examineResultStr = "审核中";
                        refundResultView.setApplyTime(DateUtil.dateFormate(recordModel.getRe_time(), GloabConst.TimePattern.YMDHMS));
                        recordModel.setRe_time(null);
                    }
                    else if (rTypeOld == DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_RECEIVED_REBAKGOODS
                            || rTypeOld == DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_AGREE_REBACK_AMT
                            || rTypeOld == DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_REFUSE_ARTIFICIAL_OK)
                    {
                        examineResultStr = "退款成功";
                        refundResultView.setReturnMoney(recordModel.getMoney());
                    }
                    else if (rTypeOld == DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_QUICK_REFUND)
                    {
                        //(系统自动审核)
                        examineResultStr = "退款成功";
                        refundResultView.setReturnMoney(recordModel.getMoney());
                    }
                    else
                    {
                        if (rTypeOld == DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_REFUSE_REBACKGOODS_AMT
                                || rTypeOld == DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_REFUSE_AFTER_SALE
                                || rTypeOld == DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_REFUSE_REBACKGOODS
                                || rTypeOld == DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_REFUSE_AMT)
                        {
                            examineResultStr = "审核拒绝";
                            refuseText = recordModel.getContent();
                        }
                        else if (rTypeOld == DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_USER_DELIVERED)
                        {
                            examineResultStr = "商品审核中";
                            ExpressModel expressModel = expressModelMapper.selectByPrimaryKey(recordModel.getExpress_id());
                            String       str          = "-";
                            if (expressModel != null)
                            {
                                str = expressModel.getKuaidi_name();
                            }
                            refundResultView.setReBackNo(String.format("%s(%s)", recordModel.getCourier_num(), str));
                            refundResultView.setReBackTime(DateUtil.dateFormate(recordModel.getRe_time(), GloabConst.TimePattern.YMDHMS));
                            recordModel.setRe_time(null);
                        }
                        else if (rTypeOld == DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_AGREE_REBACK
                                || rTypeOld == DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_QUICK_SUPPLIER_AGRESS)
                        {
                            examineResultStr = "审核通过";
                        }
                        else if (rTypeOld == DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_AGREE_REBACK_GOODS)
                        {
                            examineResultStr = "商品审核通过";
                            ExpressModel expressModel = expressModelMapper.selectByPrimaryKey(recordModel.getExpress_id());
                            String       str          = "-";
                            if (expressModel != null)
                            {
                                str = expressModel.getKuaidi_name();
                            }
                            refundResultView.setReturnBackNo(String.format("%s(%s)", recordModel.getCourier_num(), str));
                            refundResultView.setReBackTime(DateUtil.dateFormate(recordModel.getRe_time(), GloabConst.TimePattern.YMDHMS));
                        }
                    }
                    if (recordModel.getRe_time() != null)
                    {
                        refundResultView.setExamineResultTime(DateUtil.dateFormate(recordModel.getRe_time(), GloabConst.TimePattern.YMDHMS));
                    }
                    refundResultView.setExamineResult(examineResultStr);
                    refundResultView.setRefuseText(refuseText);

                    if (StringUtils.isNotEmpty(examineResultStr))
                    {
                        refundResultViewList.add(refundResultView);
                    }
                }

                //用户退货记录信息
                resultMap.put("rdata", returnGoodsModelList);
                //售后记录
                resultMap.put("examineInfo", refundResultViewList);
                //售后记录信息
                resultMap.put("record", returnRecordModels);
                //售后订单细信息
                resultMap.put("list", refundInfoMap);
                //售后凭证信息
                resultMap.put("imgs", rePhotoImgUrlList);
                resultMap.put("type", type);

                resultMap.put("currency_symbol", MapUtils.getString(refundInfoMap, "currency_symbol"));
                resultMap.put("exchange_rate", MapUtils.getDouble(refundInfoMap, "exchange_rate"));


            }
        }
        catch (Exception e)
        {
            logger.error("根据id查询售后信息 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getBrandInfo");
        }
        return resultMap;
    }

    @Override
    public String getRefundStatus(int storeId, int detailId) throws LaiKeAPIException
    {
        String statusName;
        try
        {
            if (detailId == 0)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_XQBNWK, "详情id不能为空");
            }
            //查询售后情况
            ReturnOrderModel returnOrderModel = returnOrderModelMapper.getReturnNewInfoByDetailId(detailId);
            statusName = getRefundStatusByName(returnOrderModel);
        }
        catch (Exception e)
        {
            logger.error("获取售后状态【订单商品】 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getRefundStatus");
        }
        return statusName;
    }

    @Override
    public String getRefundStatus(int storeId, Integer recId) throws LaiKeAPIException
    {
        try
        {
            if (recId == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误");
            }
            ReturnOrderModel returnOrderModel = returnOrderModelMapper.selectByPrimaryKey(recId);
            return getRefundStatusByName(returnOrderModel);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
    }

    @Override
    public String getRefundStatus(int storeId, String orderNo) throws LaiKeAPIException
    {
        try
        {
            return getRefundStatus(storeId, orderNo, null);
        }
        catch (Exception e)
        {
            logger.error("获取售后状态【订单】 异常" + e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getRefundStatus");
        }
    }

    @Override
    public String getRefundStatus(int storeId, String orderNo, Integer orderDetailId) throws LaiKeAPIException
    {
        String statusName;
        try
        {
            if (StringUtils.isEmpty(orderNo))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DDHBNWK, "订单号不能为空");
            }
            //查询售后情况
            ReturnOrderModel returnOrderModel;
            if (orderDetailId == null)
            {
                returnOrderModel = returnOrderModelMapper.getReturnNewInfoBySno(orderNo);
            }
            else
            {
                returnOrderModel = returnOrderModelMapper.getReturnNewInfoByDetailId(orderDetailId);
            }
            statusName = getRefundStatusByName(returnOrderModel);
        }
        catch (Exception e)
        {
            logger.error("获取售后状态【订单】 异常" + e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getRefundStatus");
        }
        return statusName;
    }

    private String getRefundStatusByName(ReturnOrderModel returnOrderModel)
    {
        String statusName = "";
        if (returnOrderModel != null)
        {
            int rType  = returnOrderModel.getR_type();
            int reType = returnOrderModel.getRe_type();

            //退款转态
            if (reType == DictionaryConst.ReturnRecordReType.RETURNORDERTYPE_REFUSE_AMT
                    && rType == DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_EXAMEWAIT_STATUS)
            {
                statusName = I18nUtils.getRawMessage("refund.status.reviewing");
            }
            else if (rType == DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_RECEIVED_REBAKGOODS
                    || rType == DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_AGREE_REBACK_AMT
                    || rType == DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_REFUSE_ARTIFICIAL_OK
                    || rType == DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_QUICK_REFUND)
            {
                statusName = I18nUtils.getRawMessage("refund.status.success");
            }
            else if (reType != DictionaryConst.ReturnRecordReType.RETURNORDERSTATUS_GOODS_REBACK
                    && (rType == DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_REFUSE_REBACKGOODS_AMT || rType == DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_REFUSE_REBACKGOODS || rType == DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_REFUSE_AMT || rType == DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_REFUSE_AFTER_SALE))
            {
                statusName = I18nUtils.getRawMessage("refund.status.fail");
            }
            else if (reType == DictionaryConst.ReturnRecordReType.RETURNORDERSTATUS_GOODS_REBACK
                    && (rType == DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_AGREE_REBACK || rType == DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_USER_DELIVERED || rType == DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_AGREE_REBACK_GOODS))
            {
                statusName = I18nUtils.getRawMessage("refund.status.exchangeing");
            }
            else if (rType == DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_AFTER_SALE_END)
            {
                statusName = I18nUtils.getRawMessage("refund.status.exchange_success");
            }
            else if (reType == DictionaryConst.ReturnRecordReType.RETURNORDERSTATUS_GOODS_REBACK
                    && (rType == DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_REFUSE_REBACKGOODS || rType == DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_REFUSE_AFTER_SALE))
            {
                statusName = I18nUtils.getRawMessage("refund.status.exchange_fail");
            }
            else if (reType == DictionaryConst.ReturnRecordReType.RETURNORDERSTATUS_REFUSE_REBACKGOODS_AMT && rType == DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_AGREE_REBACK)
            {
                statusName = I18nUtils.getRawMessage("refund.status.refunding");
            }
            else
            {
                statusName = I18nUtils.getRawMessage("refund.status.reviewing");
            }
        }
        return statusName;
    }

    @Override
    public int getOrderRefundType(int rType, int reType) throws LaiKeAPIException
    {
        //0=可以人工审核 1=待审核 2=退款中 3=退款成功 4=退款失败(仅退款) 5=退换中 6=换货成功 7=换货失败 8=退款失败(退货退款) 9=人工审核失败 10=人工审核成功
        int refundType = 0;
        //退款转态
        if (rType == 0)
        {
            refundType = 1;
        }
        else if (reType == DictionaryConst.ReturnRecordReType.RETURNORDERTYPE_REFUSE_AMT
                && (rType == DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_AGREE_REBACK || rType == DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_USER_DELIVERED))
        {
            refundType = 2;
        }
        else if (rType == DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_RECEIVED_REBAKGOODS || rType == DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_AGREE_REBACK_AMT)
        {
            refundType = 3;
        }
        else if (reType != DictionaryConst.ReturnRecordReType.RETURNORDERSTATUS_GOODS_REBACK
                && (rType == DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_REFUSE_REBACKGOODS_AMT || rType == DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_REFUSE_REBACKGOODS
                || rType == DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_REFUSE_AMT
                || rType == DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_REFUSE_ARTIFICIAL_OK))
        {
            //退款失败
            refundType = 4;
            if (reType == DictionaryConst.ReturnRecordReType.RETURNORDERSTATUS_REFUSE_REBACKGOODS_AMT)
            {
                refundType = 8;
            }
            //是否可以人工审核 只有用户回寄给商城之后才能人工审核
            if (rType == DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_REFUSE_REBACKGOODS_AMT)
            {
                refundType = 0;
            }
            //是否人工审核
            if (rType == DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_REFUSE_ARTIFICIAL_OK)
            {
                refundType = 10;
            }
        }
        else if (reType == DictionaryConst.ReturnRecordReType.RETURNORDERSTATUS_GOODS_REBACK
                && (rType == DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_AGREE_REBACK || rType == DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_USER_DELIVERED || rType == DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_AGREE_REBACK_GOODS))
        {
            refundType = 5;
        }
        else if (rType == DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_AFTER_SALE_END)
        {
            refundType = 6;
        }
        else if (reType == DictionaryConst.ReturnRecordReType.RETURNORDERSTATUS_GOODS_REBACK
                && (rType == DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_REFUSE_REBACKGOODS || rType == DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_REFUSE_AFTER_SALE))
        {
            refundType = 7;
            //是否人工审核
            if (rType == DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_REFUSE_ARTIFICIAL_OK)
            {
                refundType = 10;
            }
        }
        return refundType;
    }

    /**
     * 1、source 1  type = 1 false
     * 2、source 1 type 16 true && 审核页面只显示退款
     */
    @Override
    public boolean isExamine(int storeId, Integer refundId) throws LaiKeAPIException
    {
        boolean          isExamine      = false;
        ReturnOrderModel returnOrderOld = returnOrderModelMapper.selectByPrimaryKey(refundId);
        if (returnOrderOld == null)
        {
            return false;
        }
        if (DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_EXAMEWAIT_STATUS.equals(returnOrderOld.getR_type())
                || DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_USER_DELIVERED.equals(returnOrderOld.getR_type())
                || DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_QUICK_SUPPLIER_AGRESS.equals(returnOrderOld.getR_type())
        )
        {
            isExamine = true;
        }

        return isExamine;
    }

    @Override
    public boolean isMainExamine(int storeId, Integer refundId) throws LaiKeAPIException
    {
        boolean          isManExamine   = false;
        ReturnOrderModel returnOrderOld = returnOrderModelMapper.selectByPrimaryKey(refundId);
        if (returnOrderOld == null)
        {
            return false;
        }
        //商品回寄后被拒绝
        if (DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_REFUSE_REBACKGOODS.equals(returnOrderOld.getR_type()))
        {
            isManExamine = true;
        }
        return isManExamine;
    }

    /**
     * 获取订单实际支付金额
     * 【php tool.get_order_price】
     *
     * @param id - 订单明细id
     * @return BigDecimal
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/2 15:33
     */
    private BigDecimal getOrderPrice(int storeId, int id) throws LaiKeAPIException
    {
        try
        {
            BigDecimal          orderPrice;
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", storeId);
            parmaMap.put("detailId", id);
            //获取商品信息
            List<Map<String, Object>> orderGoodsInfoList = orderModelMapper.getOrderInfoLeftDetailDynamic(parmaMap);
            if (orderGoodsInfoList != null && orderGoodsInfoList.size() > 0)
            {
                Map<String, Object> map = orderGoodsInfoList.get(0);
                //快递id
                int expressId = StringUtils.stringParseInt(map.get("express_id") + "");
                //运费
                BigDecimal freight = new BigDecimal(map.get("freight").toString());
                //订单号
                String sNo = map.get("sNo").toString();
                //商品id
                Integer pid = MapUtils.getInteger(map, "p_id");
                //判断是否存在供应商商品运费 有则替换
                SupplierOrderFrightModel supplierOrderFrightModel = new SupplierOrderFrightModel();
                supplierOrderFrightModel.setStore_id(storeId);
                supplierOrderFrightModel.setsNo(sNo);
                supplierOrderFrightModel.setDetail_id(pid);
                supplierOrderFrightModel = supplierOrderFrightModelMapper.selectOne(supplierOrderFrightModel);
                if (supplierOrderFrightModel != null)
                {
                    freight = supplierOrderFrightModel.getTotal_fright();
                }
                //商品优惠后的实际金额
                BigDecimal afterDiscount = BigDecimal.ZERO;
                if (map.get("after_discount") != null)
                {
                    afterDiscount = new BigDecimal(map.get("after_discount").toString());
                }

                //判断是否发货
                if (freight.doubleValue() > 0 && expressId > 0)
                {
                    //发了货不退运费
                    orderPrice = afterDiscount;
                }
                else
                {
                    //计算实际支付金额
                    if (sNo.contains(DictionaryConst.OrdersType.ORDERS_HEADER_GM))
                    {
                        orderPrice = afterDiscount;
                    }
                    else
                    {
                        orderPrice = afterDiscount.add(freight);
                    }
                }

                return orderPrice;
            }
            else
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WZDDDXX, "未找到订单信息", "getOrderPrice");
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取订单实际支付金额 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getOrderPrice");
        }
    }

    @Override
    public Map<String, Boolean> afterSaleButtonShow(int storeId, String oType, OrderDetailsModel orderDetailsModel) throws LaiKeAPIException
    {
        Map<String, Boolean> resultMap = new HashMap<>(16);
        try
        {
            OrderModel orderModel = new OrderModel();
            orderModel.setsNo(orderDetailsModel.getR_sNo());
            orderModel = orderModelMapper.selectOne(orderModel);
            if (orderModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DDBCZ, "订单不存在");
            }
            //申请售后
            boolean refund = true;
            //仅退款 只有代发货才有仅退款
            boolean refundAmt = orderDetailsModel.getR_status() > DictionaryConst.OrdersStatus.ORDERS_R_STATUS_UNPAID;
            //换货 未发货没有换货
            boolean refundGoods = orderDetailsModel.getR_status() > DictionaryConst.OrdersStatus.ORDERS_R_STATUS_CONSIGNMENT;
            //换货 订单两次换货成功后不能再换货  --禅道47399
            if (returnOrderModelMapper.getOrderReturnGoodsNum(storeId, orderDetailsModel.getId(), orderDetailsModel.getR_sNo()) >= 2)
            {
                refundGoods = false;
            }
            //退货退款 未发货没有退货退款
            boolean refundGoodsAmt = orderDetailsModel.getR_status() > DictionaryConst.OrdersStatus.ORDERS_R_STATUS_CONSIGNMENT;
            //查看售后按钮
            boolean refundShowBtn = false;
            //申请退款按钮 如果订单完成了则显示 售后按钮 否则显示退款按钮 俩按钮只有文字上的区别;插件只显示申请售后按钮
            boolean isRefundAmtBtn = (DictionaryConst.OrdersType.ORDERS_HEADER_GM.equalsIgnoreCase(oType) || DictionaryConst.OrdersType.ORDERS_HEADER_PT.equalsIgnoreCase(oType) || DictionaryConst.OrdersType.ORDERS_HEADER_IN.equalsIgnoreCase(oType)) && orderDetailsModel.getR_status() > DictionaryConst.OrdersStatus.ORDERS_R_STATUS_UNPAID && ORDERS_R_STATUS_COMPLETE != orderDetailsModel.getR_status();
            int     unFinishShouHouOrderNum = 0;
            //获取售后信息
            ReturnOrderModel returnOrderModel = returnOrderModelMapper.getReturnOrderInfo(storeId, orderDetailsModel.getId(), orderDetailsModel.getR_sNo());
            out:
            if (returnOrderModel != null)
            {
                int row;
                /*
                //超过售后次数 禅道:37024
                row= returnOrderModelMapper.orderRefundCount(storeId, orderDetailsModel.getR_sNo(), orderDetailsModel.getId());
                if (row >= GloabConst.LktConfig.RETURN_ORDER_LIMIT) {
                    refund = refundAmt = refundGoods = refundGoodsAmt = isRefundAmtBtn = false;
                    break out;
                }*/
                //判断有无售后未结束
                unFinishShouHouOrderNum = returnOrderModelMapper.orderDetailReturnIsNotEnd(storeId, orderDetailsModel.getR_sNo(), orderDetailsModel.getId());
                //订单每个商品可以申请n次售后,成功才计算  申请换货成功后不能退款！
                if (unFinishShouHouOrderNum > 0)
                {
                    //有售后未结束不能申请售后
                    refund = refundAmt = refundGoods = isRefundAmtBtn = refundGoodsAmt = false;
                    //显示查看售后按钮
                    refundShowBtn = true;
                }
                else
                {
                    //判断是否申请过换货,申请换货成功后不能退款
                    ReturnOrderModel returnOrderCount = new ReturnOrderModel();
                    returnOrderCount.setStore_id(storeId);
                    returnOrderCount.setsNo(orderDetailsModel.getR_sNo());
                    returnOrderCount.setR_type(DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_AFTER_SALE_END);
                    returnOrderCount.setRe_type(DictionaryConst.ReturnRecordReType.RETURNORDERSTATUS_GOODS_REBACK);
                    row = returnOrderModelMapper.selectCount(returnOrderCount);
                    if (row > 0)
                    {
                        //有成功记录 换货可以换两次,如果只成功了一次则再次显示申请售后按钮
                        refundAmt = false;
                        refundGoodsAmt = false;
                        //超过换货次数
                        if (row >= GloabConst.LktConfig.RETURN_ORDER_LIMIT)
                        {
                            refund = false;
                            //超过换货次数不显示查看售后按钮
                            refundShowBtn = false;
                            isRefundAmtBtn = false;
                        }
                    }
                }
            }
            //退款是否有成功记录
            if (DictionaryConst.OrdersType.ORDERS_HEADER_GM.equalsIgnoreCase(oType) || DictionaryConst.OrdersType.ORDERS_HEADER_PT.equalsIgnoreCase(oType) || DictionaryConst.OrdersType.ORDERS_HEADER_PS.equalsIgnoreCase(oType) || DictionaryConst.OrdersType.ORDERS_HEADER_FS.equalsIgnoreCase(oType) || DictionaryConst.OrdersType.ORDERS_HEADER_ZB.equalsIgnoreCase(oType) || DictionaryConst.OrdersType.ORDERS_HEADER_IN.equalsIgnoreCase(oType))
            {
                //退款是否有成功记录
                int row = returnOrderModelMapper.orderReturnSuccessNum(storeId, orderDetailsModel.getR_sNo(), orderDetailsModel.getId());
                if (row > 0)
                {
                    refund = refundAmt = refundGoods = refundGoodsAmt = isRefundAmtBtn = false;
                    //52463 【JAVA开发环境】订单（管理后台）：已经退款的订单，订单总价应该不能改变，可以在订单详情中加退款的标识
                    refundShowBtn = true;
                }
                else
                {
                    //失败了则显示申请售后
                    refund = true;
                }
            }
            else
            {
                //其它插件只能申请换货
                refundAmt = false;
                refundGoodsAmt = false;
            }
            //积分订单、人工审核没有售后   250616积分订单有售后了
//            if (DictionaryConst.OrdersType.ORDERS_HEADER_IN.equalsIgnoreCase(oType))
//            {
//                refund = refundAmt = refundGoods = refundGoodsAmt = refundShowBtn = false;
//            }
            //竞拍订单没有售后
            if (DictionaryConst.OrdersType.ORDERS_HEADER_JP.equalsIgnoreCase(oType))
            {
                refund = refundAmt = refundGoods = refundGoodsAmt = isRefundAmtBtn = refundShowBtn = false;
            }
            //虚拟订单只有仅退款，虚拟订单特殊处理
            if (DictionaryConst.OrdersType.ORDERS_HEADER_VI.equalsIgnoreCase(oType))
            {
                refundAmt = true;
                refundShowBtn = false;
                refundGoods = refundGoodsAmt;

                //退款是否有成功记录
                int row = returnOrderModelMapper.orderReturnSuccessNum(storeId, orderDetailsModel.getR_sNo(), orderDetailsModel.getId());
                if (row > 0)
                {
                    refund = refundAmt = refundGoods = refundGoodsAmt = isRefundAmtBtn = false;
                    //52463 【JAVA开发环境】订单（管理后台）：已经退款的订单，订单总价应该不能改变，可以在订单详情中加退款的标识
                    refundShowBtn = true;
                }
                else
                {
                    //失败了则显示申请售后
                    refund = true;
                }
                if (!orderModel.getStatus().equals(DictionaryConst.OrdersStatus.ORDERS_R_STATUS_CLOSE))
                {
                    isRefundAmtBtn = refund = true;
                }
                if (orderModel.getStatus().equals(DictionaryConst.OrdersStatus.ORDERS_R_STATUS_UNPAID))
                {
                    isRefundAmtBtn = refund = false;
                }
                if (unFinishShouHouOrderNum > 0)
                {
                    //有售后未结束不能申请售后
                    refund = refundAmt = refundGoods = isRefundAmtBtn = refundGoodsAmt = false;
                    //显示查看售后按钮
                    refundShowBtn = true;
                }

            }
            //订单是否已经结算,已经结算了没有售后
            if (orderDetailsModel.getSettlement_type().equals(DictionaryConst.WhetherMaven.WHETHER_OK))
            {
                refund = refundAmt = refundGoods = refundGoodsAmt = isRefundAmtBtn = false;
            }
            //订单已完成,判断是否已过了售后期,过了受后期不显示售后按钮
            if (ORDERS_R_STATUS_COMPLETE == orderDetailsModel.getR_status())
            {
                String mchId = StringUtils.trim(orderModel.getMch_id(), SplitUtils.DH);
                try
                {
                    publicOrderService.orderAfterSaleExpire(storeId, Integer.parseInt(mchId), oType, orderDetailsModel.getArrive_time());
                }
                catch (LaiKeAPIException l)
                {
                    refund = refundAmt = refundGoods = refundGoodsAmt = isRefundAmtBtn = false;
                }
            }
            //代发货订单有极速退款不显示售后按钮 禅道 53624
            if (ORDERS_R_STATUS_CONSIGNMENT == orderModel.getStatus()
                    && (DictionaryConst.OrdersType.ORDERS_HEADER_GM.equalsIgnoreCase(oType) || DictionaryConst.OrdersType.ORDERS_HEADER_ZB.equalsIgnoreCase(oType)
                    || DictionaryConst.OrdersType.ORDERS_HEADER_IN.equalsIgnoreCase(oType)))
            {
                refund = refundAmt = refundGoods = refundGoodsAmt = isRefundAmtBtn = false;
            }
            if (refund)
            {
                refund = refundAmt || refundGoods || refundGoodsAmt || isRefundAmtBtn;
            }
            //自提订单没有搜后
            if (orderModel.getSelf_lifting() == 1)
            {
                refund = refundAmt = refundGoods = refundGoodsAmt = isRefundAmtBtn = refundShowBtn = false;
            }

            //有售后未结束不能申请售后
            resultMap.put("refund", refund);
            resultMap.put("refundAmt", refundAmt);
            resultMap.put("refundGoods", refundGoods);
            resultMap.put("refundAmtBtn", isRefundAmtBtn);
            resultMap.put("refundShowBtn", refundShowBtn);
            resultMap.put("refundGoodsAmt", refundGoodsAmt);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("售后按钮是否能显示 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "afterSaleButtonShow");
        }
        return resultMap;
    }

    @Override
    public Map<String, Boolean> afterSaleButtonShow(Integer storeId, String oType, List<OrderDetailsModel> orderDetailsModelList, OrderModel orderModel, Integer unFinishShouHouOrderNum)
    {
        Map<String, Boolean> res           = new HashMap<>();
        boolean              refund        = true;
        boolean              refundShowBtn = true;
        for (OrderDetailsModel orderDetailsModel : orderDetailsModelList)
        {
            Map<String, Boolean> refundButtonIsShowMap = refundButtonIsShow(orderDetailsModel, storeId, oType, unFinishShouHouOrderNum, orderModel);
            Boolean              currentRefund         = MapUtils.getBoolean(refundButtonIsShowMap, "refund");
            Boolean              currentRefundShowBtn  = MapUtils.getBoolean(refundButtonIsShowMap, "refundShowBtn");
            if (!currentRefund && refund)
            {
                refund = false;
            }
            if (!currentRefundShowBtn && refundShowBtn)
            {
                refundShowBtn = false;
            }
        }
        res.put("refund", refund);
        res.put("refundShowBtn", refundShowBtn);
        return res;
    }

    private Map<String, Boolean> refundButtonIsShow(OrderDetailsModel orderDetailsModel, Integer storeId, String oType, Integer unFinishShouHouOrderNum, OrderModel orderModel)
    {
        Map<String, Boolean> res = new HashMap<>();
        //申请售后
        boolean refund = true;
        //仅退款 只有代发货才有仅退款
        boolean refundAmt = orderDetailsModel.getR_status() > DictionaryConst.OrdersStatus.ORDERS_R_STATUS_UNPAID;
        //换货 未发货没有换货
        boolean refundGoods = orderDetailsModel.getR_status() > DictionaryConst.OrdersStatus.ORDERS_R_STATUS_CONSIGNMENT;
        //换货 订单两次换货成功后不能再换货  --禅道47399
        if (returnOrderModelMapper.getOrderReturnGoodsNum(storeId, orderDetailsModel.getId(), orderDetailsModel.getR_sNo()) >= 2)
        {
            refundGoods = false;
        }
        //查看售后按钮
        boolean refundShowBtn = false;
        //退货退款 未发货没有退货退款
        boolean refundGoodsAmt = orderDetailsModel.getR_status() > DictionaryConst.OrdersStatus.ORDERS_R_STATUS_CONSIGNMENT;
        //申请退款按钮 如果订单完成了则显示 售后按钮 否则显示退款按钮 俩按钮只有文字上的区别;插件只显示申请售后按钮
        boolean isRefundAmtBtn = (DictionaryConst.OrdersType.ORDERS_HEADER_GM.equalsIgnoreCase(oType)
                || DictionaryConst.OrdersType.ORDERS_HEADER_PT.equalsIgnoreCase(oType)
                || DictionaryConst.OrdersType.ORDERS_HEADER_IN.equalsIgnoreCase(oType))
                && orderDetailsModel.getR_status() > DictionaryConst.OrdersStatus.ORDERS_R_STATUS_UNPAID
                && ORDERS_R_STATUS_COMPLETE != orderDetailsModel.getR_status();
        //获取售后信息
        ReturnOrderModel returnOrderModel = returnOrderModelMapper.getReturnOrderInfo(storeId, orderDetailsModel.getId(), orderDetailsModel.getR_sNo());
        if (Objects.nonNull(returnOrderModel))
        {
            int row;
            //订单每个商品可以申请n次售后,成功才计算  申请换货成功后不能退款！
            if (unFinishShouHouOrderNum > 0 || returnOrderModel.getR_type() == 4 || returnOrderModel.getR_type() == 9 || returnOrderModel.getR_type() == 13)
            {
                //有售后未结束不能申请售后
                refund = refundAmt = refundGoods = isRefundAmtBtn = refundGoodsAmt = false;
                //显示查看售后按钮
                refundShowBtn = true;
            }
            else
            {
                //判断是否申请过换货,申请换货成功后不能退款
                ReturnOrderModel returnOrderCount = new ReturnOrderModel();
                returnOrderCount.setStore_id(storeId);
                returnOrderCount.setsNo(orderDetailsModel.getR_sNo());
                returnOrderCount.setR_type(DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_AFTER_SALE_END);
                returnOrderCount.setRe_type(DictionaryConst.ReturnRecordReType.RETURNORDERSTATUS_GOODS_REBACK);
                row = returnOrderModelMapper.selectCount(returnOrderCount);
                if (row > 0)
                {
                    //有成功记录 换货可以换两次,如果只成功了一次则再次显示申请售后按钮
                    refundAmt = false;
                    refundGoodsAmt = false;
                    //超过换货次数
                    if (row >= GloabConst.LktConfig.RETURN_ORDER_LIMIT)
                    {
                        refund = false;
                        //超过换货次数不显示查看售后按钮
                        isRefundAmtBtn = false;
                        refundShowBtn = false;
                    }
                }
            }
        }
        //退款是否有成功记录
        if (DictionaryConst.OrdersType.ORDERS_HEADER_GM.equalsIgnoreCase(oType)
                || DictionaryConst.OrdersType.ORDERS_HEADER_PT.equalsIgnoreCase(oType)
                || DictionaryConst.OrdersType.ORDERS_HEADER_PS.equalsIgnoreCase(oType)
                || DictionaryConst.OrdersType.ORDERS_HEADER_FS.equalsIgnoreCase(oType)
                || DictionaryConst.OrdersType.ORDERS_HEADER_ZB.equalsIgnoreCase(oType)
                || DictionaryConst.OrdersType.ORDERS_HEADER_IN.equalsIgnoreCase(oType))
        {
            //退款是否有成功记录
            int row = returnOrderModelMapper.orderReturnSuccessNum(storeId, orderDetailsModel.getR_sNo(), orderDetailsModel.getId());
            //失败了则显示申请售后
            if (row > 0)
            {
                refund = refundAmt = refundGoods = refundGoodsAmt = isRefundAmtBtn = false;
                //52463 【JAVA开发环境】订单（管理后台）：已经退款的订单，订单总价应该不能改变，可以在订单详情中加退款的标识
                refundShowBtn = true;
            }

        }
//        //积分订单、人工审核没有售后
//        if (DictionaryConst.OrdersType.ORDERS_HEADER_IN.equalsIgnoreCase(oType))
//        {
//            refund = false;
//        }
        //竞拍订单没有售后
        if (DictionaryConst.OrdersType.ORDERS_HEADER_JP.equalsIgnoreCase(oType))
        {
            refund = refundAmt = refundGoods = refundGoodsAmt = isRefundAmtBtn = refundShowBtn = false;
        }
        //订单是否已经结算,已经结算了没有售后
        if (orderDetailsModel.getSettlement_type().equals(DictionaryConst.WhetherMaven.WHETHER_OK))
        {
            refund = refundAmt = refundGoods = refundGoodsAmt = isRefundAmtBtn = refundShowBtn = false;
        }
        //订单已完成,判断是否已过了售后期,过了受后期不显示售后按钮
        if (ORDERS_R_STATUS_COMPLETE == orderDetailsModel.getR_status())
        {
            String mchId = StringUtils.trim(orderModel.getMch_id(), SplitUtils.DH);
            try
            {
                publicOrderService.orderAfterSaleExpire(storeId, Integer.parseInt(mchId), oType, orderDetailsModel.getArrive_time());
            }
            catch (LaiKeAPIException l)
            {
                refund = refundAmt = refundGoods = refundGoodsAmt = isRefundAmtBtn = false;
            }
        }
        //代发货订单有极速退款不显示售后按钮 禅道 53624
        if (ORDERS_R_STATUS_CONSIGNMENT == orderModel.getStatus()
                && (DictionaryConst.OrdersType.ORDERS_HEADER_GM.equalsIgnoreCase(oType) || DictionaryConst.OrdersType.ORDERS_HEADER_ZB.equalsIgnoreCase(oType)))
        {
            refund = refundAmt = refundGoods = refundGoodsAmt = isRefundAmtBtn = false;
        }
        if (refund)
        {
            refund = refundAmt || refundGoods || refundGoodsAmt || isRefundAmtBtn;
        }
        res.put("refund", refund);
        res.put("refundShowBtn", refundShowBtn);
        return res;
    }

    //获取加购商品信息
    private List<Map<String, Object>> getAddGoodList(String orderNo, Integer storeId)
    {
        List<Map<String, Object>> addGoodsList = new ArrayList<>();

        List<OrderDetailsModel> orderDetailsModelList = orderDetailsModelMapper.getAddGoodsList(orderNo);
        for (OrderDetailsModel detailsModel : orderDetailsModelList)
        {
            Map<String, Object> map = new HashMap<>();
            //金额
            BigDecimal goodPrice = detailsModel.getP_price();
            //数量
            Integer num     = detailsModel.getNum();
            String  imgUrl  = detailsModel.getImgUrl();
            String  imgPath = publiceService.getImgPath(imgUrl, storeId);
            map.put("imgurl", imgPath);
            map.put("goodsId", detailsModel.getP_id());
            map.put("p_name", detailsModel.getP_name());
            map.put("size", detailsModel.getSize());
            map.put("num", num);
            map.put("z_price", goodPrice.multiply(new BigDecimal(num)));
            addGoodsList.add(map);
        }
        return addGoodsList;
    }

    @Autowired
    private ServiceAddressModelMapper serviceAddressModelMapper;

    @Autowired
    private CustomerModelMapper customerModelMapper;

    @Autowired
    private OrderModelMapper orderModelMapper;

    @Autowired
    private MchModelMapper mchModelMapper;

    @Autowired
    private ReturnOrderModelMapper returnOrderModelMapper;

    @Autowired
    private PublicMemberService publicMemberService;

    @Autowired
    @Qualifier("publicAlipayServiceImpl")
    private PublicPaymentService publicAlipayServiceImpl;

    @Autowired
    @Qualifier("publicWechatServiceImpl")
    private PublicPaymentService publicWechatServiceImpl;

    @Autowired
    @Qualifier("publicStripeServiceImpl")
    private PublicPaymentService publicStripeServiceImpl;

    @Autowired
    private PubliceService publiceService;

    @Autowired
    private MessageListModelMapper messageListModelMapper;

    @Autowired
    private MchAccountLogModelMapper mchAccountLogModelMapper;

    @Autowired
    private ConfiGureModelMapper confiGureModelMapper;

    @Autowired
    private ProductListModelMapper productListModelMapper;

    @Autowired
    private StockModelMapper stockModelMapper;

    @Autowired
    private OrderDetailsModelMapper orderDetailsModelMapper;

    @Autowired
    private ReturnGoodsModelMapper returnGoodsModelMapper;

    @Autowired
    private ExpressModelMapper expressModelMapper;

    @Autowired
    private ReturnRecordModelMapper returnRecordModelMapper;

    @Autowired
    private SystemMessageModelMapper systemMessageModelMapper;

    @Autowired
    private PublicCouponService publicCouponService;

    @Autowired
    private PreSellRecordModelMapper preSellRecordModelMapper;

    @Autowired
    private SignRecordModelMapper signRecordModelMapper;

    @Autowired
    private UserBaseMapper userBaseMapper;

    @Autowired
    private PublicIntegralService publicIntegralService;

    @Autowired
    private SupplierOrderFrightModelMapper supplierOrderFrightModelMapper;

    @Autowired
    private SupplierModelMapper supplierModelMapper;

    @Autowired
    private SupplierAccountLogModelMapper supplierAccountLogModelMapper;

    @Autowired
    private ExpressDeliveryModelMapper expressDeliveryModelMapper;

    @Autowired
    private LivingRoomModelMapper livingRoomModelMapper;

    @Autowired
    private LivingProductModelMapper livingProductModelMapper;

    @Autowired
    private PreSellGoodsMapper preSellGoodsMapper;

    @Autowired
    private IntegralGoodsModelMapper integralGoodsModelMapper;

    @Autowired
    private SecondsActivityModelMapper secondsActivityModelMapper;

}
