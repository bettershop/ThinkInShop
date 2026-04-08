package com.laiketui.common.service.dubbo.plugin.integral;

import com.alibaba.fastjson2.*;
import com.google.common.collect.Maps;
import com.laiketui.common.annotation.HandlerOrderType;
import com.laiketui.common.api.*;
import com.laiketui.common.api.order.OrderDubboService;
import com.laiketui.common.mapper.*;
import com.laiketui.common.process.OrderShowValueProcess.OrderProcessor;
import com.laiketui.common.utils.CurrencyUtils;
import com.laiketui.common.utils.tool.ImgUploadUtils;
import com.laiketui.common.utils.tool.cache.RedisDataTool;
import com.laiketui.common.utils.tool.data.GoodsDataUtils;
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
import com.laiketui.domain.config.IntegralConfigModel;
import com.laiketui.domain.distribution.FreightModel;
import com.laiketui.domain.log.RecordModel;
import com.laiketui.domain.mch.IntegralGoodsModel;
import com.laiketui.domain.mch.MchBrowseModel;
import com.laiketui.domain.mch.MchModel;
import com.laiketui.domain.mch.ServiceAddressModel;
import com.laiketui.domain.message.MessageLoggingModal;
import com.laiketui.domain.order.OrderDetailsModel;
import com.laiketui.domain.order.OrderModel;
import com.laiketui.domain.order.ReturnOrderModel;
import com.laiketui.domain.order.ReturnRecordModel;
import com.laiketui.domain.product.ProductListModel;
import com.laiketui.domain.supplier.SupplierModel;
import com.laiketui.domain.user.User;
import com.laiketui.domain.user.UserAddress;
import com.laiketui.domain.virtual.WriteRecordModel;
import com.laiketui.domain.vo.OrderVo;
import com.laiketui.domain.vo.goods.AddStockVo;
import com.laiketui.domain.vo.main.RefundVo;
import com.laiketui.domain.vo.order.*;
import org.apache.commons.codec.CharEncoding;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.net.URLDecoder;
import java.util.*;

import static com.laiketui.core.lktconst.DictionaryConst.OrdersStatus.ORDERS_R_STATUS_COMPLETE;
import static com.laiketui.core.lktconst.ErrorCode.BizErrorCode.*;

/**
 * 积分订单流程
 *
 * @author Trick
 * @date 2021/4/14 14:19
 */
@HandlerOrderType(type = DictionaryConst.OrdersType.ORDERS_HEADER_IN)
@Service("integralOrderHandler")
public class IntegralOrderServiceImpl implements OrderDubboService
{
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private IntegralGoodsModelMapper integralGoodsModelMapper;

    @Autowired
    private UserBaseMapper userBaseMapper;

    @Autowired
    private MchModelMapper mchModelMapper;

    @Autowired
    private PubliceService publiceService;

    @Autowired
    private PublicAddressService publicAddressService;

    @Autowired
    private PublicOrderService publicOrderService;

    @Autowired
    private ConfiGureModelMapper confiGureModelMapper;

    @Autowired
    private ProductListModelMapper productListModelMapper;

    @Autowired
    private OrderModelMapper orderModelMapper;

    @Autowired
    private OrderDetailsModelMapper orderDetailsModelMapper;

    @Autowired
    private MchBrowseModelMapper mchBrowseModelMapper;

    @Autowired
    private PublicStockService publicStockService;

    @Autowired
    private IntegralConfigModelMapper integralConfigModelMapper;

    @Autowired
    private SupplierModelMapper supplierModelMapper;

    @Autowired
    private PaymentConfigModelMapper paymentConfigModelMapper;

    @Autowired
    private FreightModelMapper freightModelMapper;

    @Autowired
    private SignRecordModelMapper signRecordModelMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ReturnGoodsModelMapper returnGoodsModelMapper;

    @Autowired
    private ServiceAddressModelMapper serviceAddressModelMapper;

    @Autowired
    private MessageLoggingModalMapper messageLoggingModalMapper;

    @Autowired
    private CustomerModelMapper customerModelMapper;

    @Autowired
    private ReturnOrderModelMapper returnOrderModelMapper;

    @Autowired
    private OrderProcessor orderProcessor;

    @Autowired
    private PublicRefundService publicRefundService;

    @Autowired
    private WriteRecordModelMapper writeRecordModelMapper;

    @Autowired
    private ReturnRecordModelMapper returnRecordModelMapper;

    @Autowired
    private RecordModelMapper recordModelMapper;

    @Autowired
    private PublicPaymentConfigService publicPaymentConfigService;

    @Override
    public Map<String, Object> settlement(OrderVo vo)
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            user = userMapper.selectByPrimaryKey(user.getId());
            //总价
            BigDecimal money = new BigDecimal("0");
            //是否能支付
            boolean isPay = true;
            //是否设置支付密码
            int pwdStatus = DictionaryConst.WhetherMaven.WHETHER_OK;
            if (user.getLogin_num().equals(GloabConst.LktConfig.LOGIN_AGAIN_MAX))
            {
                isPay = false;
            }
            if (StringUtils.isEmpty(user.getPassword()))
            {
                pwdStatus = DictionaryConst.WhetherMaven.WHETHER_NO;
            }
            List<Map<String, Object>> productList;
            try
            {
                productList = JSON.parseObject(vo.getProductsInfo(), new TypeReference<List<Map<String, Object>>>()
                {
                });
            }
            catch (JSONException j)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSGSBZQ, vo.getProductsInfo() + ">参数格式不正确", "immediatelyCart");
            }
            if (productList == null || productList.size() == 0)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSBZQ, "参数不正确");
            }
            //获取商品规格信息
            int attrId = 0;
            //所需数量
            int needNum = 0;
            //商品id
            int pid = 0;
            for (Map<String, Object> attrMap : productList)
            {
                if (attrMap.containsKey("cid"))
                {
                    attrId = MapUtils.getIntValue(attrMap, "cid");
                }
                else if (attrMap.containsKey("num"))
                {
                    needNum = MapUtils.getIntValue(attrMap, "num");
                }
                else if (attrMap.containsKey("pid"))
                {
                    pid = MapUtils.getIntValue(attrMap, "pid");
                }
            }
            //获取用户地址
            UserAddress userAddress = publicAddressService.findAddress(vo.getStoreId(), user.getUser_id(), vo.getAddressId());
            //获取商品信息
            if (pid == 0)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "商品pid不存在");
            }
            Map<String, Object> goodsDetailInfo = integralGoodsModelMapper.getGoodsDetailInfo(pid, attrId);
            if (goodsDetailInfo == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SPBCZ, "商品不存在");
            }
            if (MapUtils.getInteger(goodsDetailInfo, "num") < needNum)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_KCBZ, "库存不足");
            }
            //修正积分商品id
            vo.setMainId(MapUtils.getInteger(goodsDetailInfo, "id"));
            //获取店铺信息
            Map<String, Object> shopInfo = new HashMap<>(16);
            Integer             mchId    = StringUtils.stringParseInt(goodsDetailInfo.get("mch_id"));
            if (mchId != null)
            {
                MchModel mchModel = mchModelMapper.selectByPrimaryKey(mchId);
                if (mchModel != null)
                {
                    shopInfo.putAll(publiceService.commodityInformation(vo.getStoreId(), mchId,null));
                    shopInfo.put("mch_id", mchId);
                    shopInfo.put("shop_name", mchModel.getName());
                    shopInfo.put("shop_logo", publiceService.getImgPath(mchModel.getLogo(), vo.getStoreId()));
                }
            }
            goodsDetailInfo.putAll(shopInfo);

            ConfiGureModel confiGureModel = new ConfiGureModel();
            confiGureModel.setId(attrId);
            confiGureModel.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS.toString());
            confiGureModel = confiGureModelMapper.selectOne(confiGureModel);
            if (confiGureModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SPGGBCZ, "商品规格不存在");
            }
            goodsDetailInfo.put("imgurl", publiceService.getImgPath(confiGureModel.getImg(), vo.getStoreId()));
            goodsDetailInfo.put("size", GoodsDataUtils.getProductSkuValue(confiGureModel.getAttribute()));
            //获取所需价格
            BigDecimal goodsPrice = new BigDecimal(MapUtils.getString(goodsDetailInfo, "money"));
            //所需积分
            BigDecimal integral = new BigDecimal(MapUtils.getString(goodsDetailInfo, "integral")).multiply(new BigDecimal(needNum));

            BigDecimal zl = new BigDecimal(MapUtils.getString(goodsDetailInfo, "weight"));

            //计算价格
            BigDecimal freightPrice = this.getFreight(vo.getStoreId(), null, needNum, StringUtils.stringParseInt(goodsDetailInfo.get("freight").toString()), userAddress, zl);
            money = money.add(goodsPrice).multiply(BigDecimal.valueOf(needNum));
            goodsDetailInfo.put("num", needNum);


            //各个支付的开启、关闭状态
            Map payInfo = new HashMap();
            int storeId = vo.getStoreId();
            payInfo = publicPaymentConfigService.getPaymentInfos(storeId);
            Map map = new HashMap();
            map.put("store_id", storeId);

            resultMap.putAll(payInfo);

            resultMap.put("money", money);
            resultMap.put("total", money.add(freightPrice));
            resultMap.put("integral", integral);
            resultMap.put("freight", freightPrice);
            resultMap.put("enterless", isPay);
            resultMap.put("address", userAddress);
            resultMap.put("products", goodsDetailInfo);
            resultMap.put("user_money", user.getMoney());
            resultMap.put("user_score", user.getScore());
            resultMap.put("password_status", pwdStatus);
            resultMap.put("addemt", userAddress != null ? 1 : 0);
        }
        catch (LaiKeAPIException l)
        {
            logger.error("积分兑换-结算 异常", l);
            throw l;
        }
        catch (Exception e)
        {
            logger.error("积分兑换-结算 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "settlement");
        }
        return resultMap;
    }


    /**
     * 计算秒杀运费
     *
     * @param storeId     -
     * @param mchId       - 店铺id
     * @param num         - 购买的数量
     * @param freightId   - 运费模板id
     * @param userAddress - 用户收货地址
     * @return BigDecimal
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/10/21 15:06
     */
    private BigDecimal getFreight(int storeId, Integer mchId, Integer num, Integer freightId,
                                  UserAddress userAddress, BigDecimal weightNum) throws LaiKeAPIException
    {
        //运费
        BigDecimal freightPrice = BigDecimal.ZERO;
        try
        {
            if (mchId == null)
            {
                //获取自营店id
                mchId = customerModelMapper.getStoreMchId(storeId);
            }
            //是否开启了包邮设置
            int packageSettings = DictionaryConst.WhetherMaven.WHETHER_NO;
            //同件
            int                 samePiece           = 0;
            IntegralConfigModel integralConfigModel = new IntegralConfigModel();
            integralConfigModel.setStore_id(storeId);
            integralConfigModel.setMch_id(mchId);
            integralConfigModel = integralConfigModelMapper.selectOne(integralConfigModel);
            if (integralConfigModel != null)
            {
                packageSettings = integralConfigModel.getPackage_settings();
                if (packageSettings == DictionaryConst.WhetherMaven.WHETHER_OK)
                {
                    samePiece = integralConfigModel.getSame_piece();
                }
            }
            //是否免邮
            boolean orderFreight = false;
            //是否开启包邮设置
            if (packageSettings == DictionaryConst.WhetherMaven.WHETHER_OK)
            {
                //是否满足同件免邮规则
                orderFreight = samePiece <= num;
            }
            if (orderFreight)
            {
                //满足免邮条件
                return freightPrice;
            }
            else
            {
                FreightModel freightModel = new FreightModel();
                if (freightId != null)
                {
                    //根据运费模版id
                    freightModel.setId(freightId);
                }
                else
                {
                    //如果没有默认模板则获取默认模板
                    freightModel.setStore_id(storeId);
                    freightModel.setMch_id(mchId);
                    freightModel.setIs_default(DictionaryConst.DefaultMaven.DEFAULT_OK);
                }
                //运费信息
                freightModel = freightModelMapper.selectOne(freightModel);
                if (freightModel != null)
                {
                    //获取运费
                    BigDecimal goodsYunFei = publicOrderService.getFreight(freightModel.getId(), userAddress, num, weightNum);
                    //计算总运费
                    freightPrice = freightPrice.add(goodsYunFei);
                }
            }

        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("计算秒杀运费 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SPXXBWZ, "商品信息不完整", "getFreight");
        }
        return freightPrice;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> payment(OrderVo vo)
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            //刷新缓存
            user = userBaseMapper.selectByPrimaryKey(user.getId());
            RedisDataTool.refreshRedisUserCache(vo.getAccessId(), user, redisUtil);

            List<Map<String, Object>> productList;
            try
            {
                productList = JSON.parseObject(vo.getProductsInfo(), new TypeReference<List<Map<String, Object>>>()
                {
                });
            }
            catch (JSONException j)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSGSBZQ, vo.getProductsInfo() + ">参数格式不正确", "payment");
            }
            if (vo.getMainId() == null || productList == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误");
            }
            //商品id
            int goodsId = 0;
            //获取商品规格信息
            int attrId = 0;
            //所需数量
            int needNum = 0;
            for (Map<String, Object> attrMap : productList)
            {
                if (attrMap.containsKey("pid"))
                {
                    goodsId = MapUtils.getIntValue(attrMap, "pid");
                }
                else if (attrMap.containsKey("cid"))
                {
                    attrId = MapUtils.getIntValue(attrMap, "cid");
                }
                else if (attrMap.containsKey("num"))
                {
                    needNum = MapUtils.getIntValue(attrMap, "num");
                }
            }

            //获取积分商品信息
            IntegralGoodsModel integralGoodsOld = new IntegralGoodsModel();
            integralGoodsOld.setStore_id(vo.getStoreId());
            integralGoodsOld.setAttr_id(attrId);
            integralGoodsOld.setGoods_id(goodsId);
            integralGoodsOld.setIs_delete(DictionaryConst.ProductRecycle.NOT_STATUS);
            integralGoodsOld = integralGoodsModelMapper.selectOne(integralGoodsOld);
            if (integralGoodsOld == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_JFSPBCZ, "积分商品不存在");
            }
            //修正积分商品id 不能存积分商品表的积分 退款直接报错
            vo.setMainId(goodsId);
            //获取商品信息
            ProductListModel productListOld = new ProductListModel();
            productListOld.setId(goodsId);
            productListOld.setStore_id(vo.getStoreId());
            productListOld.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS.toString());
            productListOld = productListModelMapper.selectOne(productListOld);
            if (productListOld == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SPBCZ, "商品不存在");
            }
            BigDecimal zl = new BigDecimal(productListOld.getWeight());

            //获取用户地址
            UserAddress userAddress  = publicAddressService.findAddress(vo.getStoreId(), user.getUser_id(), vo.getAddressId());
            BigDecimal  freightPrice = this.getFreight(vo.getStoreId(), null, needNum, Integer.parseInt(productListOld.getFreight()), userAddress, zl);
            // 订单备注
            Map<String, String> mchRemarks = new HashMap<>();
            if (!StringUtils.isEmpty(vo.getRemarks()))
            {
                mchRemarks.put(productListOld.getMch_id().toString(), vo.getRemarks());
            }

            FreightModel freightModel = freightModelMapper.selectByPrimaryKey(productListOld.getFreight());
            if (freightModel != null)
            {
                //用户地址 省-市-区对应运费模板里的格式
                String address = userAddress.getSheng() + "-" + userAddress.getCity() + "-" + userAddress.getQuyu();
                //不配送区域参数列表
                String    bpsRule = URLDecoder.decode(freightModel.getNo_delivery(), CharEncoding.UTF_8);
                JSONArray objects = JSONArray.parseArray(bpsRule);
                if (objects.contains(address))
                {
                    logger.debug("地址超出配送范围");
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DZCCPSFW, "地址超出配送范围", "getNo_delivery");
                }
            }
            //商品名称
            String goodsName = productListOld.getProduct_title();
            //所需金额(原价)
            BigDecimal needMoney = integralGoodsOld.getMoney();
            //所需积分
            BigDecimal needScore = new BigDecimal(integralGoodsOld.getIntegral()).multiply(new BigDecimal(needNum));
            //获取规格信息
            ConfiGureModel attrOld = confiGureModelMapper.selectByPrimaryKey(attrId);
            //规格处理
            String attribute = GoodsDataUtils.getProductSkuValue(attrOld.getAttribute());

            //获取用户信息
            user = userBaseMapper.selectByPrimaryKey(user.getId());
            if (vo.getPayType().equals(DictionaryConst.OrderPayType.ORDERPAYTYPE_WALLET_PAY) &&
                    user.getMoney().compareTo(needMoney.multiply(new BigDecimal(needNum))) < 0)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YEBZ, "余额不足");
            }
            else if (user.getScore() < needScore.intValue())
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_JFBZ, "积分不足");
            }
            else if (integralGoodsOld.getNum() - needNum < 0)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_KCBZ, "库存不足");
            }
            //添加一条购买记录
            MchBrowseModel mchBrowseModel = new MchBrowseModel();
            mchBrowseModel.setMch_id(productListOld.getMch_id().toString());
            mchBrowseModel.setStore_id(vo.getStoreId());
            mchBrowseModel.setUser_id(user.getUser_id());
            mchBrowseModel.setEvent("购买了积分商品");
            mchBrowseModel.setAdd_time(new Date());
            mchBrowseModelMapper.insertSelective(mchBrowseModel);
            //添加积分商品以扣减库存-无需重复出库
            //出库
//            publicStockService.outStockNum(vo.getStoreId(), productListOld.getId(), attrId, needNum);
            //生成订单号
            String orderNo = publicOrderService.createOrderNo(DictionaryConst.OrdersType.ORDERS_HEADER_IN);
            // 生成支付订单号
            String realSno = publicOrderService.createOrderNo(DictionaryConst.OrdersType.ORDERS_HEADER_IN);
            //生成订单明细
            OrderDetailsModel orderDetailsModel = new OrderDetailsModel();
            orderDetailsModel.setStore_id(vo.getStoreId());
            orderDetailsModel.setUser_id(user.getUser_id());
            //积分商品id
            orderDetailsModel.setP_id(vo.getMainId());
            orderDetailsModel.setP_name(goodsName);
            orderDetailsModel.setP_price(needMoney);
            orderDetailsModel.setNum(needNum);
            orderDetailsModel.setUnit(attrOld.getUnit());
            orderDetailsModel.setR_sNo(orderNo);
            orderDetailsModel.setAdd_time(new Date());
            orderDetailsModel.setR_status(DictionaryConst.OrdersStatus.ORDERS_R_STATUS_UNPAID);
            orderDetailsModel.setSize(attribute);
            orderDetailsModel.setSid(attrId + "");
            orderDetailsModel.setFreight(freightPrice);
            orderDetailsModel.setSettlement_type(OrderDetailsModel.SETTLEMENT_TYPE_UNSETTLED);
            orderDetailsModel.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS);
            orderDetailsModel.setMch_id(productListOld.getMch_id());
            //消费积分记录在这里
            //orderDetailsModel.setAfter_discount(new BigDecimal(integralGoodsOld.getIntegral()));
            //3862 【JAVA开发环境】积分：移动端--积分订单缺少售后功能

            //after_discount 优惠后的金额  actual_total 实际支付金额
            // 1. 获取 BigDecimal 类型的金额
            BigDecimal money = integralGoodsOld.getMoney();
            // 2. 将 int 转换为 BigDecimal
            BigDecimal needNumBig = BigDecimal.valueOf(needNum);
            // 3. 执行乘法运算（BigDecimal * BigDecimal）
            BigDecimal totalBig = money.multiply(needNumBig);
            totalBig = totalBig.add(freightPrice);
            orderDetailsModel.setActualTotal(totalBig);
            orderDetailsModel.setAfter_discount(totalBig);

            //score_deduction 积分支付抵扣
            int scoreDeduction = integralGoodsOld.getIntegral() * needNum;
            orderDetailsModel.setScoreDeduction(scoreDeduction);
            //p_integral 产品积分（单个产品对应的积分，允许为NULL）
            orderDetailsModel.setP_integral(integralGoodsOld.getIntegral());

            int row = orderDetailsModelMapper.insertSelective(orderDetailsModel);
            if (row < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_XDSBQSHZS, "下单失败,请稍后再试");
            }
            //获取订单配置
            Map<String, Object> configMap = publicOrderService.getOrderConfig(vo.getStoreId(), productListOld.getMch_id(), DictionaryConst.OrdersType.ORDERS_HEADER_IN);
            //订单失效  - 秒
            Integer orderFailureDay = 3600;
            if (StringUtils.isNotEmpty(configMap))
            {
                //未付款订单保留时间
                orderFailureDay = MapUtils.getInteger(configMap, "orderFailureDay");
                if (orderFailureDay == null || orderFailureDay < 0)
                {
                    orderFailureDay = 3600;
                }
            }
            OrderModel orderModel = new OrderModel();
            orderModel.setStore_id(vo.getStoreId());
            orderModel.setUser_id(user.getUser_id());
            orderModel.setName(userAddress.getName());
            orderModel.setMobile(userAddress.getTel());
            orderModel.setNum(needNum);
            orderModel.setZ_price(needMoney.multiply(new BigDecimal(needNum)).add(freightPrice));
            orderModel.setOld_total(orderModel.getZ_price());
            orderModel.setsNo(orderNo);
            orderModel.setReal_sno(realSno);
            orderModel.setRemark(vo.getRemarks());
            orderModel.setSheng(userAddress.getSheng());
            orderModel.setShi(userAddress.getCity());
            orderModel.setXian(userAddress.getQuyu());
            orderModel.setAddress(userAddress.getAddress());
            orderModel.setSpz_price(needScore);
            orderModel.setStatus(DictionaryConst.OrdersStatus.ORDERS_R_STATUS_UNPAID);
            orderModel.setSource(vo.getStoreType());
            orderModel.setOtype(DictionaryConst.OrdersType.ORDERS_HEADER_IN);
            orderModel.setMch_id(String.format("%s%s%s", SplitUtils.DH, productListOld.getMch_id(), SplitUtils.DH));
            orderModel.setP_sNo("");
            orderModel.setRemarks(JSON.toJSONString(mchRemarks));
            orderModel.setZ_freight(freightPrice);
            orderModel.setOld_freight(freightPrice);
            orderModel.setAllow(needScore.intValue());
            orderModel.setSubtraction_id(0);
            orderModel.setAdd_time(new Date());
            orderModel.setOrderFailureTime(orderFailureDay);
            orderModel.setScoreDeduction(integralGoodsOld.getIntegral());
            //货币信息[用户支付的货币信息]前提是商城默认币种不设置以后不允许修改
            orderModel.setCurrency_code(vo.getCurrency_code());
            orderModel.setCurrency_symbol(vo.getCurrency_symbol());
            orderModel.setExchange_rate(vo.getExchange_rate());
            row = orderModelMapper.insertSelective(orderModel);
            if (row < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_XDSBQSHZS, "下单失败,请稍后再试");
            }
            //扣减库存
            row = integralGoodsModelMapper.addStockNum(integralGoodsOld.getId(), -needNum);
            if (row < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_JFSPKCBZ, "积分商品库存不足");
            }

            MessageLoggingModal messageLoggingSave = new MessageLoggingModal();
            messageLoggingSave.setMch_id(productListOld.getMch_id());
            messageLoggingSave.setStore_id(vo.getStoreId());
            messageLoggingSave.setType(MessageLoggingModal.Type.TYPE_ORDER_NEW);
            messageLoggingSave.setTo_url(OrderDataUtils.getOrderRoute(DictionaryConst.OrdersType.ORDERS_HEADER_IN, 0));
            messageLoggingSave.setParameter(orderModel.getId() + "");
            messageLoggingSave.setContent(String.format("您来新订单了，订单为%s，请及时处理！", orderModel.getsNo()));
            messageLoggingSave.setAdd_date(new Date());
            messageLoggingModalMapper.insertSelective(messageLoggingSave);

            resultMap.put("sNo", orderNo);
            resultMap.put("total", orderModel.getZ_price());
            resultMap.put("total_score", orderModel.getAllow());
            resultMap.put("order_id", orderModel.getId());
            //下单时间
            resultMap.put("orderTime", DateUtil.dateFormate(orderModel.getAdd_time(), GloabConst.TimePattern.YMDHMS));
        }
        catch (LaiKeAPIException e)
        {
            logger.error("积分商城兑换下单 异常", e);
            throw e;
        }
        catch (Exception e)
        {
            logger.error("积分商城兑换下单 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "payment");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> updateOrderRemark(OrderVo vo)
    {
        return null;
    }

    @Override
    public Map<String, Object> splitOrder(OrderVo vo)
    {
        return null;
    }

    @Override
    public Map<String, Object> returnData(ApplyReturnDataVo vo, MultipartFile file) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            //商品图片集
            List<String> imgUlrs = new ArrayList<>();
            if (file != null)
            {
                if (vo.getUploadNum() == 0)
                {
                    //清空缓存
                    redisUtil.del(GloabConst.RedisHeaderKey.RETURN_UPLOAD_KEY + vo.getOrderDetailsId());
                }
                //获取之前的图片
                Object obj = redisUtil.get(GloabConst.RedisHeaderKey.RETURN_UPLOAD_KEY + vo.getOrderDetailsId());
                if (obj != null)
                {
                    imgUlrs = DataUtils.cast(obj);
                    if (imgUlrs == null)
                    {
                        imgUlrs = new ArrayList<>();
                    }
                }
                List<MultipartFile> files = new ArrayList<>();
                files.add(file);
                //默认使用oos
                String uploadType = GloabConst.UploadConfigConst.IMG_UPLOAD_OSS;
                //获取店铺信息
                /*ConfigModel configModel = new ConfigModel();
                configModel.setStore_id(vo.getStoreId());
                configModel = configModelMapper.selectOne(configModel);
                if (configModel != null) {
                    uploadType = configModel.getUpserver();
                }*/
                //图片上传
                List<String> urls = publiceService.uploadImage(files, uploadType, vo.getStoreType(), vo.getStoreId());
                if (urls.size() != files.size())
                {
                    logger.info(String.format("图片上传失败 需上传:%s 实际上传:%s", files.size(), imgUlrs.size()));
                    throw new LaiKeAPIException(ERROR_CODE_TPSCSB_001, "图片上传失败", "returnData");
                }
                imgUlrs.add(ImgUploadUtils.getUrlImgByName(urls.get(0), true));
                redisUtil.set(GloabConst.RedisHeaderKey.RETURN_UPLOAD_KEY + vo.getOrderDetailsId(), imgUlrs, 10);
                if (vo.getUploadMaxNum() != vo.getUploadNum() + 1)
                {
                    return resultMap;
                }
            }
            //清空缓存
            redisUtil.del(GloabConst.RedisHeaderKey.RETURN_UPLOAD_KEY + vo.getOrderDetailsId());

            resultMap = this.initReturnData(vo, StringUtils.stringImplode(new ArrayList<>(imgUlrs), SplitUtils.DH, false), GloabConst.RedisHeaderKey.LOGIN_ACCESS_TOKEN);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("申请售后 异常", e);
            throw new LaiKeAPIException(ERROR_CODE_WLYC, "网络异常", "returnData");
        }
        return resultMap;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> returnData(ApplyReturnDataVo vo, String imgUrl, String tokenKey) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User         user       = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, tokenKey, true);
            List<String> goodsNames = new ArrayList<>();
            //支持批量售后
            List<String> detailIds = DataUtils.convertToList(vo.getOrderDetailsId().split(SplitUtils.DH));
            if (detailIds == null)
            {
                throw new LaiKeAPIException(ERROR_CODE_CSCW, "参数错误");
            }
            resultMap.put("RefundType", "");
            //获取订单信息
            OrderModel orderModel = null;
            //是否整单退款 整单则直接退运费  默认false 为非整单退款
            boolean isWholeOrderAfterSaler = false;

            for (String did : detailIds)
            {
                int detailId = Integer.parseInt(did);

                //获取订单商品信息
                OrderDetailsModel orderDetailsModel = new OrderDetailsModel();
                orderDetailsModel.setStore_id(vo.getStoreId());
                orderDetailsModel.setUser_id(user.getUser_id());
                orderDetailsModel.setId(detailId);
                orderDetailsModel = orderDetailsModelMapper.selectOne(orderDetailsModel);

                if (orderDetailsModel == null)
                {
                    throw new LaiKeAPIException(ERROR_CODE_DDXXBCZ, "订单信息不存在", "returnData");
                }
                //获取订单主表信息
                if (orderModel == null)
                {
                    orderModel = new OrderModel();
                    String sNo = orderDetailsModel.getR_sNo();
                    orderModel.setsNo(sNo);
                    orderModel = orderModelMapper.selectOne(orderModel);
                    if (orderModel == null)
                    {
                        throw new LaiKeAPIException(ERROR_CODE_DDXXBCZ, "订单信息不存在", "returnData");
                    }
                    OrderDetailsModel orderDetailsTmp = new OrderDetailsModel();
                    orderDetailsTmp.setR_sNo(sNo);
                    List<OrderDetailsModel> orderTotalDetailsList = orderDetailsModelMapper.select(orderDetailsTmp);
                    isWholeOrderAfterSaler = orderTotalDetailsList.size() == detailIds.size();
                }
                if (StringUtils.isNotEmpty(orderDetailsModel.getArrive_time()))
                {
                    String mchId = StringUtils.trim(orderModel.getMch_id(), SplitUtils.DH);
                    //判断订单是否已过售后期
                    Map<String, Object> configMap = publicOrderService.getOrderConfig(vo.getStoreId(), Integer.parseInt(mchId), orderModel.getOtype().toUpperCase());
                    if (configMap == null)
                    {
                        logger.info("【{}】插件-未开启/未配置 ", orderModel.getOtype());
                        continue;
                    }
                    //售后日期 单位【秒】
                    int orderAfterSec = MapUtils.getInteger(configMap, "orderAfter");
                    //计算最终售后期
                    Date arriveDate = DateUtil.getAddDateBySecond(orderDetailsModel.getArrive_time(), orderAfterSec);
                    logger.info("【{}】订单最终受后期为:{} ", orderModel.getOtype(), DateUtil.dateFormate(arriveDate, GloabConst.TimePattern.YMDHMS));
                    if (DateUtil.dateCompare(new Date(), arriveDate))
                    {
                        logger.info("{}订单已过受后期", orderModel.getsNo());
                        throw new LaiKeAPIException(ERROR_CODE_YGSHQ, "已过售后期", "returnData");
                    }
                }
                //判断当前明细是否已经在售后中
                if (returnOrderModelMapper.orderDetailReturnIsNotEnd(vo.getStoreId(), orderModel.getsNo(), detailId) > 0)
                {
                    logger.info("订单{} 明细id{} 正在售后中,无法申请售后!", orderModel.getsNo(), detailId);
                    throw new LaiKeAPIException(ERROR_CODE_LBYSPZSHZ, "列表有商品再售后中", "returnData");
                }
                //是否达到售后上限 --非急速退款
                Map<String, Boolean> returnInfo   = publicRefundService.afterSaleButtonShow(orderDetailsModel.getStore_id(), orderModel.getOtype(), orderDetailsModel);
                boolean              refundAmtBtn = MapUtils.getBooleanValue(returnInfo, "refundAmtBtn");
                boolean              refund       = MapUtils.getBooleanValue(returnInfo, "refund");


                goodsNames.add(orderDetailsModel.getP_name());
                //添加售后订单信息
                ReturnOrderModel returnOrderModel = new ReturnOrderModel();
                returnOrderModel.setStore_id(vo.getStoreId());
                returnOrderModel.setUser_id(user.getUser_id());
                returnOrderModel.setsNo(orderDetailsModel.getR_sNo());
                returnOrderModel.setP_id(orderDetailsModel.getId());
                returnOrderModel.setPid(orderDetailsModel.getP_id());
                //退款类型 1:退货退款  2:退款 3:换货
                returnOrderModel.setRe_type(vo.getType());
                String content = vo.getExplain();
                if (!StringUtils.isEmpty(content))
                {
                    content = URLDecoder.decode(content, CharEncoding.UTF_8);
                }
                else
                {
                    content = "";
                }
                returnOrderModel.setContent(content);
                returnOrderModel.setR_type(DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_EXAMEWAIT_STATUS);
                returnOrderModel.setSid(Integer.parseInt(orderDetailsModel.getSid()));
                //验证金额
                if (vo.getRefundApplyMoney().compareTo(orderModel.getZ_price()) > 0)
                {
                    throw new LaiKeAPIException(ERROR_CODE_JEBNDYDQDDZJE, "金额不能大于当前订单总金额", "returnData");
                }

                //获取退款金额
                BigDecimal refundPrice = publicOrderService.getOrderPrice(detailId, vo.getStoreId());
                //如果子订单和申请售后的订单数量一致 则为订单整体售后


                if (isWholeOrderAfterSaler)
                {
                    refundPrice = refundPrice.add(orderDetailsModel.getFreight());
                }
                if (Objects.nonNull(orderDetailsModel.getActualTotal()) && orderDetailsModel.getActualTotal().compareTo(BigDecimal.ZERO) > 0)
                {
                    refundPrice = refundPrice.subtract(orderDetailsModel.getActualTotal());
                }
                returnOrderModel.setRe_money(refundPrice);
                BigDecimal reApplyMoney = vo.getRefundApplyMoney();
                if (reApplyMoney.compareTo(BigDecimal.ZERO) > 0 && reApplyMoney.compareTo(returnOrderModel.getRe_money()) < 0)
                {
                    returnOrderModel.setRe_apply_money(reApplyMoney);
                }
                else
                {
                    returnOrderModel.setRe_apply_money(returnOrderModel.getRe_money());
                }

                //记录凭证
                String phpImgUrlSerialize = "";
                if (StringUtils.isNotEmpty(imgUrl))
                {
                    String[] imgUrls = imgUrl.split(SplitUtils.DH);
                    for (int i = 0; i < imgUrls.length; i++)
                    {
                        imgUrls[i] = ImgUploadUtils.getUrlImgByName(imgUrls[i], true);
                    }
                    phpImgUrlSerialize = SerializePhpUtils.JavaSerializeByPhp(imgUrls);
                }
                returnOrderModel.setRe_photo(phpImgUrlSerialize);
                returnOrderModel.setRe_time(new Date());
                //虚拟商品记录退款次数
                if (orderModel.getOtype().equals(DictionaryConst.OrdersType.ORDERS_HEADER_VI) && orderDetailsModel.getWrite_off_num() != null)
                {
                    returnOrderModel.setR_write_off_num(orderDetailsModel.getWrite_off_num());
                    //往核销记录表中添加退款信息
                    WriteRecordModel writeRecordModel = new WriteRecordModel();
                    writeRecordModel.setStatus(WriteRecordModel.status.refund);
                    writeRecordModel.setP_id(orderDetailsModel.getId());
                    writeRecordModel.setS_no(orderDetailsModel.getR_sNo());
                    writeRecordModelMapper.insertSelective(writeRecordModel);
                    //扣除所剩的可核销次数
                    orderDetailsModel.setWrite_off_num(0);
                    orderDetailsModelMapper.updateByPrimaryKey(orderDetailsModel);
                }

                int count = returnOrderModelMapper.insertSelective(returnOrderModel);
                if (count < 1)
                {
                    throw new LaiKeAPIException(ERROR_CODE_SCSHDDSB, "生成售后订单失败", "returnData");
                }
                //记录本次售后情况
                ReturnRecordModel returnRecordModel = new ReturnRecordModel();
                returnRecordModel.setUser_id(user.getUser_id());
                returnRecordModel.setStore_id(vo.getStoreId());
                returnRecordModel.setRe_type(vo.getType());
                returnRecordModel.setR_type(DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_EXAMEWAIT_STATUS);
                returnRecordModel.setsNo(orderDetailsModel.getR_sNo());
                returnRecordModel.setP_id(returnOrderModel.getId());
                returnRecordModel.setMoney(vo.getRefundApplyMoney());
                returnRecordModel.setRe_photo(phpImgUrlSerialize);
                returnRecordModel.setProduct_id(orderDetailsModel.getP_id());
                returnRecordModel.setAttr_id(Integer.parseInt(orderDetailsModel.getSid()));
                returnRecordModel.setRe_time(new Date());
                count = returnRecordModelMapper.insertSelective(returnRecordModel);
                if (count < 1)
                {
                    throw new LaiKeAPIException(ERROR_CODE_WLGZSHJLSB, "网络故障,售后记录失败", "returnData");
                }
                //记录订单改变之前的状态
                RecordModel recordModel = new RecordModel();
                recordModel.setStore_id(vo.getStoreId());
                recordModel.setUser_id(user.getUser_id());
                Map<String, Object> tempMap = new HashMap<>(16);
                tempMap.put("r_sNo", orderDetailsModel.getR_sNo());
                tempMap.put("r_status", orderDetailsModel.getR_status());
                tempMap.put("order_details_id", orderDetailsModel.getId());
                recordModel.setEvent(JSON.toJSONString(tempMap));
                count = recordModelMapper.insertSelective(recordModel);
                if (count < 1)
                {
                    logger.info(String.format("订单%s,操作记录失败", orderDetailsModel.getR_sNo()));
                }
                //如果详单商品未发货可以进行极速退款
                //判断详单是否已经发货
                //自提订单待收货也能急速退款
                if ((orderModel.getStatus().equals(DictionaryConst.OrdersStatus.ORDERS_R_STATUS_CONSIGNMENT) &&
                        orderModel.getOtype().equals(DictionaryConst.OrdersType.ORDERS_HEADER_IN)) || (orderModel.getStatus().equals(DictionaryConst.OrdersStatus.ORDERS_R_STATUS_DISPATCHED) && orderModel.getSelf_lifting() == 1))
                {
                    //商品全部都未发货-开始极速退款流程
                    RefundVo refundVo = new RefundVo();
                    refundVo.setStoreId(vo.getStoreId());
                    refundVo.setId(returnOrderModel.getId());
                    refundVo.setPrice(refundPrice);
                    refundVo.setSNo(orderDetailsModel.getR_sNo());
                    refundVo.setType(DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_QUICK_REFUND);
                    refundVo.setOrderDetailId(orderDetailsModel.getId());
                    refundVo.setReType(vo.getType());
                    publicRefundService.quickRefund(refundVo);
                    resultMap.put("RefundType", DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_QUICK_REFUND);

                    //极速退款删除原来的订单提醒记录
                    MessageLoggingModal messageLoggingSave = new MessageLoggingModal();
                    messageLoggingSave.setParameter(String.valueOf(orderModel.getId()));
                    List<MessageLoggingModal> messageLoggingModalList = messageLoggingModalMapper.select(messageLoggingSave);
                    for (MessageLoggingModal messageLoggingModal : messageLoggingModalList)
                    {
                        messageLoggingModal.setRead_or_not(1);
                        messageLoggingModal.setIs_popup(1);
                        messageLoggingModalMapper.updateByPrimaryKeySelective(messageLoggingModal);
                    }
                }
                else
                {
                    if (!refundAmtBtn && !refund)
                    {
                        logger.error("订单详情id{} 已达到申请限制", detailId);
                        throw new LaiKeAPIException(ERROR_CODE_YDDSHSQXZ, "已达到售后申请限制");
                    }
                }
            }
            if (orderModel == null)
            {
                throw new LaiKeAPIException(ERROR_CODE_DDXXBCZ, "订单信息不存在", "returnData");
            }

            //通知后台消息
            MessageLoggingModal messageLoggingSave = new MessageLoggingModal();
            messageLoggingSave.setStore_id(vo.getStoreId());
            String[] mchId = StringUtils.trim(orderModel.getMch_id(), SplitUtils.DH).split(SplitUtils.DH);
            messageLoggingSave.setMch_id(Integer.parseInt(mchId[0]));
            messageLoggingSave.setType(MessageLoggingModal.Type.TYPE_ORDER_RETURN);
            messageLoggingSave.setTo_url(OrderDataUtils.getOrderRoute(orderModel.getOtype(), 1));
            messageLoggingSave.setParameter(orderModel.getId() + "");
            messageLoggingSave.setContent(String.format("订单 %s已申请退款（或者退货退款或者换货），请前往退货列表中及时处理！", orderModel.getsNo()));
            messageLoggingSave.setAdd_date(new Date());
            messageLoggingModalMapper.insertSelective(messageLoggingSave);

            resultMap.put("product_title", goodsNames);
            resultMap.put("sNo", orderModel.getsNo());

            String     orderCurrencyCode   = orderModel.getCurrency_code();
            String     orderCurrencySymbol = orderModel.getCurrency_symbol();
            BigDecimal orderExchangeRate   = orderModel.getExchange_rate();
            resultMap.put("currency_code", StringUtils.isNotEmpty(orderCurrencyCode) ? orderCurrencyCode : "￥");
            resultMap.put("currency_symbol", StringUtils.isNotEmpty(orderCurrencySymbol) ? orderCurrencySymbol : "￥");
            resultMap.put("exchange_rate", orderExchangeRate == null ? BigDecimal.ONE : orderExchangeRate);

            resultMap.put("time", DateUtil.dateFormate(new Date(), GloabConst.TimePattern.YMDHMS));
            resultMap.put("refund_amount", vo.getRefundApplyMoney());
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("申请售后 异常", e);
            throw new LaiKeAPIException(ERROR_CODE_WLYC, "网络异常", "returnData");
        }
        return resultMap;
    }



    private Map<String,Object> initReturnData(ApplyReturnDataVo vo, String imgUrl, String tokenKey)
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User         user       = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, tokenKey, true);
            List<String> goodsNames = new ArrayList<>();
            //支持批量售后
            List<String> detailIds = DataUtils.convertToList(vo.getOrderDetailsId().split(SplitUtils.DH));
            if (detailIds == null)
            {
                throw new LaiKeAPIException(ERROR_CODE_CSCW, "参数错误");
            }
            resultMap.put("RefundType", "");
            //获取订单信息
            OrderModel orderModel = null;
            //是否整单退款 整单则直接退运费  默认false 为非整单退款
            boolean isWholeOrderAfterSaler = false;

            for (String did : detailIds)
            {
                int detailId = Integer.parseInt(did);

                //获取订单商品信息
                OrderDetailsModel orderDetailsModel = new OrderDetailsModel();
                orderDetailsModel.setStore_id(vo.getStoreId());
                orderDetailsModel.setUser_id(user.getUser_id());
                orderDetailsModel.setId(detailId);
                orderDetailsModel = orderDetailsModelMapper.selectOne(orderDetailsModel);

                if (orderDetailsModel == null)
                {
                    throw new LaiKeAPIException(ERROR_CODE_DDXXBCZ, "订单信息不存在", "returnData");
                }
                //获取订单主表信息
                if (orderModel == null)
                {
                    orderModel = new OrderModel();
                    String sNo = orderDetailsModel.getR_sNo();
                    orderModel.setsNo(sNo);
                    orderModel = orderModelMapper.selectOne(orderModel);
                    if (orderModel == null)
                    {
                        throw new LaiKeAPIException(ERROR_CODE_DDXXBCZ, "订单信息不存在", "returnData");
                    }
                    OrderDetailsModel orderDetailsTmp = new OrderDetailsModel();
                    orderDetailsTmp.setR_sNo(sNo);
                    List<OrderDetailsModel> orderTotalDetailsList = orderDetailsModelMapper.select(orderDetailsTmp);
                    isWholeOrderAfterSaler = orderTotalDetailsList.size() == detailIds.size();
                }
                if (StringUtils.isNotEmpty(orderDetailsModel.getArrive_time()))
                {
                    String mchId = StringUtils.trim(orderModel.getMch_id(), SplitUtils.DH);
                    //判断订单是否已过售后期
                    Map<String, Object> configMap = publicOrderService.getOrderConfig(vo.getStoreId(), Integer.parseInt(mchId), orderModel.getOtype().toUpperCase());
                    if (configMap == null)
                    {
                        logger.info("【{}】插件-未开启/未配置 ", orderModel.getOtype());
                        continue;
                    }
                    //售后日期 单位【秒】
                    int orderAfterSec = MapUtils.getInteger(configMap, "orderAfter");
                    //计算最终售后期
                    Date arriveDate = DateUtil.getAddDateBySecond(orderDetailsModel.getArrive_time(), orderAfterSec);
                    logger.info("【{}】订单最终受后期为:{} ", orderModel.getOtype(), DateUtil.dateFormate(arriveDate, GloabConst.TimePattern.YMDHMS));
                    if (DateUtil.dateCompare(new Date(), arriveDate))
                    {
                        logger.info("{}订单已过受后期", orderModel.getsNo());
                        throw new LaiKeAPIException(ERROR_CODE_YGSHQ, "已过售后期", "returnData");
                    }
                }
                //判断当前明细是否已经在售后中
                if (returnOrderModelMapper.orderDetailReturnIsNotEnd(vo.getStoreId(), orderModel.getsNo(), detailId) > 0)
                {
                    logger.info("订单{} 明细id{} 正在售后中,无法申请售后!", orderModel.getsNo(), detailId);
                    throw new LaiKeAPIException(ERROR_CODE_LBYSPZSHZ, "列表有商品再售后中", "returnData");
                }
                //是否达到售后上限 --非急速退款
                Map<String, Boolean> returnInfo   = publicRefundService.afterSaleButtonShow(orderDetailsModel.getStore_id(), orderModel.getOtype(), orderDetailsModel);
                boolean              refundAmtBtn = MapUtils.getBooleanValue(returnInfo, "refundAmtBtn");
                boolean              refund       = MapUtils.getBooleanValue(returnInfo, "refund");


                goodsNames.add(orderDetailsModel.getP_name());
                //添加售后订单信息
                ReturnOrderModel returnOrderModel = new ReturnOrderModel();
                returnOrderModel.setStore_id(vo.getStoreId());
                returnOrderModel.setUser_id(user.getUser_id());
                returnOrderModel.setsNo(orderDetailsModel.getR_sNo());
                returnOrderModel.setP_id(orderDetailsModel.getId());
                returnOrderModel.setPid(orderDetailsModel.getP_id());
                //退款类型 1:退货退款  2:退款 3:换货
                returnOrderModel.setRe_type(vo.getType());
                String content = vo.getExplain();
                if (!StringUtils.isEmpty(content))
                {
                    content = URLDecoder.decode(content, CharEncoding.UTF_8);
                }
                else
                {
                    content = "";
                }
                returnOrderModel.setContent(content);
                returnOrderModel.setR_type(DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_EXAMEWAIT_STATUS);
                returnOrderModel.setSid(Integer.parseInt(orderDetailsModel.getSid()));
                //验证金额
                if (vo.getRefundApplyMoney().compareTo(orderModel.getZ_price()) > 0)
                {
                    throw new LaiKeAPIException(ERROR_CODE_JEBNDYDQDDZJE, "金额不能大于当前订单总金额", "returnData");
                }

                //获取退款金额
//                BigDecimal refundPrice = publicOrderService.getOrderPrice(detailId, vo.getStoreId());
                BigDecimal refundPrice = orderDetailsModel.getAfter_discount();
                //如果子订单和申请售后的订单数量一致 则为订单整体售后


//                if (isWholeOrderAfterSaler)
//                {
//                    refundPrice = refundPrice.add(orderDetailsModel.getFreight());
//                }
//                if (Objects.nonNull(orderDetailsModel.getActualTotal()) && orderDetailsModel.getActualTotal().compareTo(BigDecimal.ZERO) > 0)
//                {
//                    refundPrice = refundPrice.subtract(orderDetailsModel.getActualTotal());
//                }
                returnOrderModel.setRe_money(refundPrice);
                BigDecimal reApplyMoney = vo.getRefundApplyMoney();
                if (reApplyMoney.compareTo(BigDecimal.ZERO) > 0 && reApplyMoney.compareTo(returnOrderModel.getRe_money()) < 0)
                {
                    returnOrderModel.setRe_apply_money(reApplyMoney);
                }
                else
                {
                    returnOrderModel.setRe_apply_money(returnOrderModel.getRe_money());
                }

                //记录凭证
                String phpImgUrlSerialize = "";
                if (StringUtils.isNotEmpty(imgUrl))
                {
                    String[] imgUrls = imgUrl.split(SplitUtils.DH);
                    for (int i = 0; i < imgUrls.length; i++)
                    {
                        imgUrls[i] = ImgUploadUtils.getUrlImgByName(imgUrls[i], true);
                    }
                    phpImgUrlSerialize = SerializePhpUtils.JavaSerializeByPhp(imgUrls);
                }
                returnOrderModel.setRe_photo(phpImgUrlSerialize);
                returnOrderModel.setRe_time(new Date());
                //虚拟商品记录退款次数
                if (orderModel.getOtype().equals(DictionaryConst.OrdersType.ORDERS_HEADER_VI) && orderDetailsModel.getWrite_off_num() != null)
                {
                    returnOrderModel.setR_write_off_num(orderDetailsModel.getWrite_off_num());
                    //往核销记录表中添加退款信息
                    WriteRecordModel writeRecordModel = new WriteRecordModel();
                    writeRecordModel.setStatus(WriteRecordModel.status.refund);
                    writeRecordModel.setP_id(orderDetailsModel.getId());
                    writeRecordModel.setS_no(orderDetailsModel.getR_sNo());
                    writeRecordModelMapper.insertSelective(writeRecordModel);
                    //扣除所剩的可核销次数
                    orderDetailsModel.setWrite_off_num(0);
                    orderDetailsModelMapper.updateByPrimaryKey(orderDetailsModel);
                }

                int count = returnOrderModelMapper.insertSelective(returnOrderModel);
                if (count < 1)
                {
                    throw new LaiKeAPIException(ERROR_CODE_SCSHDDSB, "生成售后订单失败", "returnData");
                }
                //记录本次售后情况
                ReturnRecordModel returnRecordModel = new ReturnRecordModel();
                returnRecordModel.setUser_id(user.getUser_id());
                returnRecordModel.setStore_id(vo.getStoreId());
                returnRecordModel.setRe_type(vo.getType());
                returnRecordModel.setR_type(DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_EXAMEWAIT_STATUS);
                returnRecordModel.setsNo(orderDetailsModel.getR_sNo());
                returnRecordModel.setP_id(returnOrderModel.getId());
                returnRecordModel.setMoney(vo.getRefundApplyMoney());
                returnRecordModel.setRe_photo(phpImgUrlSerialize);
                returnRecordModel.setProduct_id(orderDetailsModel.getP_id());
                returnRecordModel.setAttr_id(Integer.parseInt(orderDetailsModel.getSid()));
                returnRecordModel.setRe_time(new Date());
                count = returnRecordModelMapper.insertSelective(returnRecordModel);
                if (count < 1)
                {
                    throw new LaiKeAPIException(ERROR_CODE_WLGZSHJLSB, "网络故障,售后记录失败", "returnData");
                }
                //记录订单改变之前的状态
                RecordModel recordModel = new RecordModel();
                recordModel.setStore_id(vo.getStoreId());
                recordModel.setUser_id(user.getUser_id());
                Map<String, Object> tempMap = new HashMap<>(16);
                tempMap.put("r_sNo", orderDetailsModel.getR_sNo());
                tempMap.put("r_status", orderDetailsModel.getR_status());
                tempMap.put("order_details_id", orderDetailsModel.getId());
                recordModel.setEvent(JSON.toJSONString(tempMap));
                count = recordModelMapper.insertSelective(recordModel);
                if (count < 1)
                {
                    logger.info(String.format("订单%s,操作记录失败", orderDetailsModel.getR_sNo()));
                }
                //如果详单商品未发货可以进行极速退款
                //判断详单是否已经发货
                //自提订单待收货也能急速退款
                if ((orderModel.getStatus().equals(DictionaryConst.OrdersStatus.ORDERS_R_STATUS_CONSIGNMENT) &&
                        orderModel.getOtype().equals(DictionaryConst.OrdersType.ORDERS_HEADER_IN)) || (orderModel.getStatus().equals(DictionaryConst.OrdersStatus.ORDERS_R_STATUS_DISPATCHED) && orderModel.getSelf_lifting() == 1))
                {
                    //商品全部都未发货-开始极速退款流程
                    RefundVo refundVo = new RefundVo();
                    refundVo.setStoreId(vo.getStoreId());
                    refundVo.setId(returnOrderModel.getId());
                    refundVo.setPrice(refundPrice);
                    refundVo.setSNo(orderDetailsModel.getR_sNo());
                    refundVo.setType(DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_QUICK_REFUND);
                    refundVo.setOrderDetailId(orderDetailsModel.getId());
                    refundVo.setReType(vo.getType());
                    publicRefundService.quickRefund(refundVo);
                    resultMap.put("RefundType", DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_QUICK_REFUND);

                    //极速退款删除原来的订单提醒记录
                    MessageLoggingModal messageLoggingSave = new MessageLoggingModal();
                    messageLoggingSave.setParameter(String.valueOf(orderModel.getId()));
                    List<MessageLoggingModal> messageLoggingModalList = messageLoggingModalMapper.select(messageLoggingSave);
                    for (MessageLoggingModal messageLoggingModal : messageLoggingModalList)
                    {
                        messageLoggingModal.setRead_or_not(1);
                        messageLoggingModal.setIs_popup(1);
                        messageLoggingModalMapper.updateByPrimaryKeySelective(messageLoggingModal);
                    }
                }
                else
                {
                    if (!refundAmtBtn && !refund)
                    {
                        logger.error("订单详情id{} 已达到申请限制", detailId);
                        throw new LaiKeAPIException(ERROR_CODE_YDDSHSQXZ, "已达到售后申请限制");
                    }
                }
            }
            if (orderModel == null)
            {
                throw new LaiKeAPIException(ERROR_CODE_DDXXBCZ, "订单信息不存在", "returnData");
            }

            //通知后台消息
            MessageLoggingModal messageLoggingSave = new MessageLoggingModal();
            messageLoggingSave.setStore_id(vo.getStoreId());
            String[] mchId = StringUtils.trim(orderModel.getMch_id(), SplitUtils.DH).split(SplitUtils.DH);
            messageLoggingSave.setMch_id(Integer.parseInt(mchId[0]));
            messageLoggingSave.setType(MessageLoggingModal.Type.TYPE_ORDER_RETURN);
            messageLoggingSave.setTo_url(OrderDataUtils.getOrderRoute(orderModel.getOtype(), 1));
            messageLoggingSave.setParameter(orderModel.getId() + "");
            messageLoggingSave.setContent(String.format("订单 %s已申请退款（或者退货退款或者换货），请前往退货列表中及时处理！", orderModel.getsNo()));
            messageLoggingSave.setAdd_date(new Date());
            messageLoggingModalMapper.insertSelective(messageLoggingSave);

            resultMap.put("product_title", goodsNames);
            resultMap.put("sNo", orderModel.getsNo());

            String     orderCurrencyCode   = orderModel.getCurrency_code();
            String     orderCurrencySymbol = orderModel.getCurrency_symbol();
            BigDecimal orderExchangeRate   = orderModel.getExchange_rate();
            resultMap.put("currency_code", StringUtils.isNotEmpty(orderCurrencyCode) ? orderCurrencyCode : "￥");
            resultMap.put("currency_symbol", StringUtils.isNotEmpty(orderCurrencySymbol) ? orderCurrencySymbol : "￥");
            resultMap.put("exchange_rate", orderExchangeRate == null ? BigDecimal.ONE : orderExchangeRate);

            resultMap.put("time", DateUtil.dateFormate(new Date(), GloabConst.TimePattern.YMDHMS));
            resultMap.put("refund_amount", vo.getRefundApplyMoney());
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("申请售后 异常", e);
            throw new LaiKeAPIException(ERROR_CODE_WLYC, "网络异常", "returnData");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> orderList(OrderVo vo)
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);

            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("storeId", vo.getStoreId());
            parmaMap.put("userId", user.getUser_id());
            parmaMap.put("orderType", vo.getQueryOrderType());
            parmaMap.put("start", vo.getPageNo());
            if (vo.getMchId() != null && user.getMchId() != null)
            {
                if (user.getMchId().equals(vo.getMchId()))
                {
                    parmaMap.put("mchId", user.getMchId());
                }
            }
            parmaMap.put("keyWord", vo.getKeyword());
            parmaMap.put("pageSize", vo.getPageSize());
            parmaMap.put("otype", DictionaryConst.OrdersType.ORDERS_HEADER_IN);
            //禅道49609 -》用户、商家、平台删除自己的订单
            parmaMap.put("userRecycle", OrderModel.SHOW);
            List<Map<String, Object>> orderList = orderModelMapper.getUserCenterOrderList(parmaMap);
            for (Map<String, Object> orderInfo : orderList)
            {
                //可开发票过期提醒
                boolean isInvoice = false;
                //是否超时
                boolean invoiceTimeout = false;
                //总运费
                BigDecimal totalYunFei = BigDecimal.ZERO;
                //商品总数量
                BigDecimal totalNeedNum = BigDecimal.ZERO;
                //订单号
                String orderno = orderInfo.get("sNo").toString();

                //订单状态
                Integer status = MapUtils.getInteger(orderInfo, "status");
                //订单类型
                String oType = MapUtils.getString(orderInfo, "otype");
                Integer selfLifting = MapUtils.getInteger(orderInfo, "self_lifting");
                //店铺id
                int mchId = 0;
                // 店铺 名称
                String mchName = "";
                // 店铺logo
                String logoUrl = "";
                // 店铺头像
                String headImg = "";

                //店铺信息处理
                String mchIdStr = orderInfo.get("mch_id") + "";
                if (!StringUtils.isEmpty(mchIdStr))
                {
                    mchId = Integer.parseInt(StringUtils.trim(mchIdStr, SplitUtils.DH));
                    //获取店铺信息
                    MchModel mchModel = mchModelMapper.selectByPrimaryKey(mchId);
                    mchName = mchModel.getName();
                    logoUrl = publiceService.getImgPath(mchModel.getLogo(), vo.getStoreId());
                    headImg = publiceService.getImgPath(mchModel.getHead_img(), vo.getStoreId());
                        //到货时间
                        Date arriveTime = DateUtil.dateFormateToDate(MapUtils.getString(orderInfo, "arrive_time"), GloabConst.TimePattern.YMDHMS);
                        //结算状态
                        Integer settlementStatus = MapUtils.getInteger(orderInfo, "settlement_status");
                        if (!Objects.isNull(arriveTime) && settlementStatus.equals(DictionaryConst.WhetherMaven.WHETHER_OK) && status.equals(ORDERS_R_STATUS_COMPLETE) && mchModel.getIs_invoice().equals(DictionaryConst.WhetherMaven.WHETHER_OK))
                        {
                            isInvoice = true;
                            if (DateUtil.dateCompare(new Date(), DateUtil.getAddDate(arriveTime, 20)))
                            {
                                invoiceTimeout = true;
                            }
                        }
                }
                orderInfo.put("shop_id", mchId);
                orderInfo.put("shop_name", mchName);
                orderInfo.put("shop_logo", logoUrl);
                orderInfo.put("headImg", headImg);

                //获取订单明细
                OrderDetailsModel orderDetailsModel = new OrderDetailsModel();
                orderDetailsModel.setStore_id(vo.getStoreId());
                orderDetailsModel.setR_sNo(orderno);
                List<OrderDetailsModel> detailList = orderDetailsModelMapper.select(orderDetailsModel);
                List<Map<String, Object>> orderDetailsModelList = JSON.parseObject(JSON.toJSONString(detailList), new TypeReference<List<Map<String, Object>>>()
                {
                });
                int commentsType = 2;
                boolean comment  = false;
                //是否展示物流
                boolean haveExpress = false;
                for (Map<String, Object> orderDetails : orderDetailsModelList)
                {
                    haveExpress = false;
                    Integer deliverNum = MapUtils.getInteger(orderDetails, "deliverNum");

                    if (deliverNum != null && deliverNum > 0 && !"2".equals(DataUtils.getStringVal(orderInfo, "self_lifting")))
                    {
                        haveExpress = true;
                    }
                    //统计运费
                    totalYunFei = totalYunFei.add(new BigDecimal(orderDetails.get("freight").toString()));
                    totalNeedNum = totalNeedNum.add(new BigDecimal(orderDetails.get("num").toString()));
                    int            attrId         = Integer.parseInt(orderDetails.get("sid").toString());
                    ConfiGureModel confiGureModel = confiGureModelMapper.selectByPrimaryKey(attrId);
                    if (confiGureModel == null)
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SJCW, "数据错误");
                    }
                    int goodsId = confiGureModel.getPid();
                    orderDetails.put("pro_id", goodsId);
                    orderDetails.put("imgurl", publiceService.getImgPath(confiGureModel.getImg(), vo.getStoreId()));
                    int orderCommentType = publicOrderService.orderCommentType(vo.getStoreId(), user.getUser_id(),
                            orderno, MapUtils.getIntValue(orderDetails, "id"), MapUtils.getIntValue(orderDetails, "sid"), MapUtils.getIntValue(orderDetails, "r_status"));
                    if (orderCommentType == 1)
                    {
                        //多商品有一个商品未评价显示立即评价
                        commentsType = 1;
                        comment = true;
                    }else if (orderCommentType == 2)
                    {
                        comment = true;
                    }
                    orderDetails.put("comments_type", orderCommentType);
                }
                if (!comment)
                {
                    commentsType = 3;
                }
                String sNo = MapUtils.getString(orderInfo, "sNo");
                Map<String,Object> resMap = new HashMap<>();
                int unFinishShouHouOrderNum = returnOrderModelMapper.orderReturnIsNotEnd(vo.getStoreId(), sNo);
                OrderModel orderModel = new OrderModel();
                orderModel.setStore_id(vo.getStoreId());
                orderModel.setsNo(sNo);
                orderModel = orderModelMapper.selectOne(orderModel);
                orderModel = orderModelMapper.selectOne(orderModel);
                Map<String, Boolean> buttonShow = publicRefundService.afterSaleButtonShow(vo.getStoreId(), orderModel.getOtype(), detailList,
                        orderModel, unFinishShouHouOrderNum);
                Boolean refund = MapUtils.getBoolean(buttonShow, "refund");
                Boolean  refundShowBtn = MapUtils.getBoolean(buttonShow, "refundShowBtn");
                Map<String,Object> paramMap = new HashMap<>();
                paramMap.put("status",status);
                paramMap.put("self_lifting",selfLifting);
                paramMap.put("orderCommentType",commentsType);
                paramMap.put("haveExpress",haveExpress);
                paramMap.put("invoiceTimeout",invoiceTimeout);
                paramMap.put("isInvoice",isInvoice);
                paramMap.put("unFinishShouHouOrderNum",unFinishShouHouOrderNum);
                paramMap.put("count",detailList.size());
                paramMap.put("refundShowBtn",refundShowBtn);
                paramMap.put("refund",refund);
                //纯积分商品价格是0 不能开发票
                // 安全地获取并转换数值
                BigDecimal zPriceBD   = (BigDecimal) orderInfo.getOrDefault("z_price", BigDecimal.ZERO);
                BigDecimal zFreightBD = (BigDecimal) orderInfo.getOrDefault("z_freight", BigDecimal.ZERO);
                // 计算价格（确保不为负数，如业务允许负数则移除 Math.max）
//                int invoicePrice = Math.max(0, zPriceBD.subtract(zFreightBD).intValueExact());
                // 修改后：使用四舍五入处理小数部分
                int invoicePrice = Math.max(0, zPriceBD.subtract(zFreightBD)
                        .setScale(2, BigDecimal.ROUND_HALF_UP) // 四舍五入取整
                        .intValue()); // 转换为int
                paramMap.put("invoicePrice", invoicePrice);
                orderProcessor.processOrder(oType,resMap,paramMap);
                String keys = String.format(GloabConst.RedisHeaderKey.ORDER_SHOW_VALUE_KEY, sNo);
                redisUtil.del(keys);
                redisUtil.set(keys,resMap,5 * 60);
                orderInfo.put("get_button_list",resMap);
                orderInfo.put("refund", true);
                orderInfo.put("list", orderDetailsModelList);
                orderInfo.put("z_freight", totalYunFei);
                orderInfo.put("sum", totalNeedNum);
                orderInfo.put("invoicePrice", invoicePrice);
            }
            resultMap.put("order", orderList);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("积分订单列表 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "orderList");
        }
        return resultMap;
    }


    @Override
    public Map<String, Object> remindDelivery(OrderVo vo)
    {
        return null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> cancleOrder(OrderVo vo)
    {
        try
        {
            OrderModel orderOld = new OrderModel();
            orderOld.setId(vo.getOrderId());
            orderOld.setStore_id(vo.getStoreId());
            orderOld.setOtype(DictionaryConst.OrdersType.ORDERS_HEADER_IN);
            orderOld.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS);
            orderOld = orderModelMapper.selectOne(orderOld);
            if (orderOld == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DDBCZ, "订单不存在");
            }
            if (!orderOld.getStatus().equals(DictionaryConst.OrdersStatus.ORDERS_R_STATUS_UNPAID))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DDBZDFKZT, "订单不在待付款状态");
            }
            //关闭订单
            OrderModel orderUpdate = new OrderModel();
            orderUpdate.setId(orderOld.getId());
            orderUpdate.setStatus(DictionaryConst.OrdersStatus.ORDERS_R_STATUS_CLOSE);
            int row = orderModelMapper.updateByPrimaryKeySelective(orderUpdate);
            if (row < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DDGBSB, "订单关闭失败");
            }
            //关闭明细
            row = orderDetailsModelMapper.closeOrder(orderOld.getsNo());
            if (row < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DDGBSB, "订单关闭失败");
            }
            //获取订单明细
            OrderDetailsModel orderDetailsModel = new OrderDetailsModel();
            orderDetailsModel.setStore_id(vo.getStoreId());
            orderDetailsModel.setR_sNo(orderOld.getsNo());
            orderDetailsModel.setP_id(vo.getMainId());
            orderDetailsModel = orderDetailsModelMapper.selectOne(orderDetailsModel);
            if (orderDetailsModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DDSJCW, "订单数据错误");
            }
            //获取插件商品
            IntegralGoodsModel integralGoodsModel = integralGoodsModelMapper.selectByPrimaryKey(orderDetailsModel.getP_id());
            if (integralGoodsModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SPBCZ, "商品不存在");
            }
            //回滚库存
            integralGoodsModelMapper.releaseStockNum(orderOld.getsNo(), integralGoodsModel.getId());

            //回滚商品库存
            AddStockVo addStockVo = new AddStockVo();
            addStockVo.setStoreId(vo.getStoreId());
            addStockVo.setId(Integer.parseInt(orderDetailsModel.getSid()));
            addStockVo.setPid(integralGoodsModel.getGoods_id());
            addStockVo.setAddNum(orderDetailsModel.getNum());
            addStockVo.setText(orderOld.getUser_id() + "取消订单,返还" + orderDetailsModel.getNum());
            publicStockService.addGoodsStock(addStockVo, orderOld.getUser_id());
            //扣减商品销量
//            productListModelMapper.updateProductListVolume(-orderDetailsModel.getNum(), vo.getStoreId(), integralGoodsModel.getGoods_id());

        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("秒杀关闭订单 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "cancleOrder");
        }
        return null;
    }

    @Override
    public Map<String, Object> loadMore(OrderVo vo)
    {
        return null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> delOrder(OrderVo vo)
    {
        try
        {
            if (StringUtils.isEmpty(vo.getOrdervalue()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误");
            }
            String[] orderIds = vo.getOrdervalue().split(SplitUtils.DH);
            for (String orderId : orderIds)
            {
                OrderModel orderOld = new OrderModel();
                orderOld.setId(Integer.parseInt(orderId));
                orderOld.setStore_id(vo.getStoreId());
                orderOld.setOtype(DictionaryConst.OrdersType.ORDERS_HEADER_IN);
                orderOld.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS);
                orderOld = orderModelMapper.selectOne(orderOld);
                if (orderOld == null)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DDBCZ, "订单不存在");
                }
                logger.debug("正在删除订单{}", orderOld.getsNo());
                //删除订单
                OrderModel orderUpdate = new OrderModel();
                orderUpdate.setId(orderOld.getId());
                orderUpdate.setRecycle(DictionaryConst.ProductRecycle.RECOVERY);
                int row = orderModelMapper.updateByPrimaryKeySelective(orderUpdate);
                if (row < 1)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DDGBSB, "订单关闭失败");
                }
                Map<String, Object> parmaMap = new HashMap<>(16);
                parmaMap.put("store_id", vo.getStoreId());
                parmaMap.put("orderno", orderOld.getsNo());
                parmaMap.put("recycle", DictionaryConst.ProductRecycle.NOT_STATUS);

                List<Map<String, Object>> orderDetailsModelList = orderDetailsModelMapper.getOrderDetailByGoodsInfo(parmaMap);
                for (Map<String, Object> detail : orderDetailsModelList)
                {
                    Integer status  = MapUtils.getInteger(detail, "r_status");
                    int     id      = MapUtils.getIntValue(detail, "id");
                    int     attrId  = MapUtils.getIntValue(detail, "sid");
                    int     goodsId = MapUtils.getIntValue(detail, "goodsId");
                    int     pid     = MapUtils.getIntValue(detail, "commodityId");
                    int     num     = MapUtils.getIntValue(detail, "num");
                    //删除明细
                    row = orderDetailsModelMapper.delOrderDetails1(vo.getStoreId(), DictionaryConst.ProductRecycle.RECOVERY, orderOld.getsNo());
                    if (row < 1)
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DDGBSB, "订单关闭失败");
                    }
                    if (status.equals(DictionaryConst.OrdersStatus.ORDERS_R_STATUS_COMPLETE) || status.equals(DictionaryConst.OrdersStatus.ORDERS_R_STATUS_CLOSE))
                    {
                        logger.debug("订单明细id{} 状态{} 删除订单不回滚库存", id, status);
                        continue;
                    }
                    //回滚【积分】库存
                    integralGoodsModelMapper.releaseStockNum(orderOld.getsNo(), pid);
//                    //回滚商品库存
//                    AddStockVo addStockVo = new AddStockVo();
//                    addStockVo.setStoreId(vo.getStoreId());
//                    addStockVo.setId(attrId);
//                    addStockVo.setPid(goodsId);
//                    addStockVo.setAddNum(num);
//                    addStockVo.setText("后台删除订单,返还" + num);
//                    publicStockService.addGoodsStock(addStockVo, "admin");
                    //扣减商品销量
                    productListModelMapper.updateProductListVolume(-num, vo.getStoreId(), goodsId);
                }

            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("积分删除订单 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "delOrder");
        }
        return null;
    }

    @Override
    public Map<String, Object> buyAgain(BuyAgainVo vo)
    {
        return null;
    }

    @Override
    public Map<String, Object> orderSearch(OrderVo vo)
    {
        return null;
    }

    @Override
    public Map<String, Object> delCart(OrderVo vo)
    {
        return null;
    }

    @Override
    public Map<String, Object> getPaymentConf(OrderVo vo)
    {
        return null;
    }

    @Override
    public Map<String, Object> orderDetails(OrderVo vo)
    {
        return null;
    }

    @Override
    public Map<String, Object> showLogistics(OrderVo vo)
    {
        return null;
    }

    @Override
    public Map<String, Object> cancleApply(int storeId, int id)
    {
        return null;
    }

    @Override
    public Map<String, Object> returnOrderList(OrderVo vo)
    {
        Map<String, Object>       resultMap = new HashMap<>(16);
        List<Map<String, Object>> product   = new ArrayList<>();
        try
        {
            int    storeId = vo.getStoreId();
            String keyword = vo.getKeyword();
            User   user;
            if (vo.getUser() != null)
            {
                user = vo.getUser();
            }
            else
            {
                user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            }
            String userId = user.getUser_id();

            Map<String, Object> params = new HashMap<>(16);
            params.put("keyword", keyword);
            params.put("userId", userId);
            params.put("storeId", storeId);
            if (vo.getOrderId() != 0)
            {
                params.put("returnOrderId", vo.getOrderId());
            }
            if (!vo.getType().equals(DictionaryConst.OrdersType.ORDERS_HEADER_IN))
            {
                params.put("type", vo.getType());
            }
            else
            {
                ArrayList<String> typeList = new ArrayList<>();
                typeList.add(vo.getType());
                typeList.add(DictionaryConst.OrdersType.ORDERS_HEADER_PT);
                if (vo.getoTypeNotIn() == null || (vo.getoTypeNotIn() != null && !vo.getoTypeNotIn().equals(DictionaryConst.OrdersType.ORDERS_HEADER_VI)))
                {
                    typeList.add(DictionaryConst.OrdersType.ORDERS_HEADER_VI);
                }
                params.put("typeList", typeList);
            }

            params.put("start", vo.getPageNo());
            params.put("pageSize", vo.getPageSize());
            int                       total = returnOrderModelMapper.getReturnOrderListCount(params);
            List<Map<String, Object>> list  = returnOrderModelMapper.getReturnOrderList(params);
            if (!CollectionUtils.isEmpty(list))
            {
                Map<String, Object> arr;
                for (Map<String, Object> returnOrderInfo : list)
                {
                    Integer id = MapUtils.getInteger(returnOrderInfo, "id");
                    //订单明细id
                    Integer orderDetailId = MapUtils.getInteger(returnOrderInfo, "detailId");
                    Integer p_id          = MapUtils.getInteger(returnOrderInfo, "goodsId");
                    if (p_id == null)
                    {
                        logger.debug("订单商品不存在 订单号{}", MapUtils.getString(returnOrderInfo, "sNo"));
                        continue;
                    }
                    //规格图
                    String attrImg = MapUtils.getString(returnOrderInfo, "attrImg");

                    returnOrderInfo.put("pid", p_id);
                    returnOrderInfo.put("audit_time", DateUtil.dateFormate(MapUtils.getString(returnOrderInfo, "audit_time"), GloabConst.TimePattern.YMDHMS));
                    returnOrderInfo.put("re_time", DateUtil.dateFormate(MapUtils.getString(returnOrderInfo, "re_time"), GloabConst.TimePattern.YMDHMS));
                    String     r_sNo      = DataUtils.getStringVal(returnOrderInfo, "sNo");
                    OrderModel orderModel = new OrderModel();
                    orderModel.setStore_id(storeId);
                    orderModel.setsNo(r_sNo);
                    orderModel = orderModelMapper.selectOne(orderModel);
                    if (orderModel == null)
                    {
                        logger.warn("订单号不存在：{}", r_sNo);
                        continue;
                    }
                    int orderId = orderModel.getId();
                    if (orderModel.getAllow() != null)
                    {
                        Integer allow = orderModel.getAllow();
                        returnOrderInfo.put("allow", allow);
                    }

                    arr = returnOrderInfo;
                    // 根据产品id,查询产
                    ProductListModel productListModel = new ProductListModel();
                    productListModel.setId(p_id);
                    productListModel = productListModelMapper.selectByPrimaryKey(productListModel);
                    String  url   = "";
                    Integer mchid = 0;
                    if (productListModel != null)
                    {
                        // 拼图片路径
                        url = publiceService.getImgPath(attrImg, storeId);
                        mchid = productListModel.getMch_id();
                    }

                    arr.put("shop_id", 0);
                    arr.put("shop_name", "");
                    arr.put("shop_logo", "");
                    arr.put("headImg", "");

                    //订单支付货币信息
                    String     orderCurrencyCode   = orderModel.getCurrency_code();
                    String     orderCurrencySymbol = orderModel.getCurrency_symbol();
                    BigDecimal orderExchangeRate   = orderModel.getExchange_rate();
                    arr.put("currency_code", StringUtils.isNotEmpty(orderCurrencyCode) ? orderCurrencyCode : "￥");
                    arr.put("currency_symbol", StringUtils.isNotEmpty(orderCurrencySymbol) ? orderCurrencySymbol : "￥");
                    arr.put("exchange_rate", orderExchangeRate == null ? BigDecimal.ONE : orderExchangeRate);

                    if (mchid != null && mchid != 0)
                    {
                        MchModel mchModel = new MchModel();
                        mchModel.setId(mchid);
                        mchModel = mchModelMapper.selectOne(mchModel);
                        if (mchModel != null)
                        {
                            arr.put("shop_id", mchModel.getId());
                            arr.put("shop_name", mchModel.getName());
                            arr.put("shop_logo", publiceService.getImgPath(mchModel.getLogo(), storeId));
                            arr.put("headImg", publiceService.getImgPath(mchModel.getHead_img(), storeId));
                        }
                    }
                    arr.put("order_id", orderId);
                    arr.put("imgurl", url);
                    String returnStatus = publicRefundService.getRefundStatus(vo.getStoreId(), id);
                    arr.put("prompt", returnStatus);
                    product.add(arr);
                }
                resultMap.put("list", product);
            }
            else
            {
                resultMap.put("list", new ArrayList<>());
            }
            resultMap.put("total", total);
            resultMap.put("message", "操作成功");
            resultMap.put("code", 200);
        }
        catch (LaiKeAPIException l)
        {
            logger.error("获取售后列表 异常:", l);
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取售后列表 异常:", e);
            throw new LaiKeAPIException(ERROR_CODE_YWYC, "业务异常", "returnOrderList");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> okOrder(OrderVo orderVo)
    {
        return null;
    }

    //    @Override
//    public Map<String, Object> returnMethod(OrderVo vo)
//    {
//        return null;
//    }
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> returnMethod(OrderVo vo)
    {
        Map<String, Object> data = Maps.newHashMap();
        logger.info("售后信息：{}", JSON.toJSONString(vo));
        try
        {
            // 商城id
            int storeId = vo.getStoreId();
            // 订单详情id
            String orderDetailsId = vo.getOrderDetailsId();
            // 根据微信id,查询用户id
            User user;
            if (vo.getUser() != null)
            {
                user = vo.getUser();
            }
            else
            {
                user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil);
            }
            if (StringUtils.isEmpty(orderDetailsId))
            {
                throw new LaiKeAPIException(PARAMATER_ERROR, "参数错误");
            }
            OrderModel orderModel = null;

            BigDecimal                refundPrice     = BigDecimal.ZERO;
            List<Map<String, Object>> list            = new ArrayList<>();
            String[]                  orderDetaisdIds = StringUtils.trim(orderDetailsId, SplitUtils.DH).split(SplitUtils.DH);
            Map<Integer, Object>      rStatus         = new HashMap<>();
            boolean                   status          = false;
            //售后按钮- 批量售后只显示同一个能申请的类型按钮
            boolean refund = true, refundAmt = true, refundGoodsAmt = true, refundGoods = true;

            // 同一订单下面多个自订单
            String sNo = "";

            //是否整单退款 整单则直接退运费  默认false 为非整单退款
            boolean isWholeOrderAfterSaler = false;

            for (String orderDetailsIdStr : orderDetaisdIds)
            {
                int did = Integer.parseInt(orderDetailsIdStr);

                OrderDetailsModel orderDetails = new OrderDetailsModel();
                orderDetails.setStore_id(storeId);
                orderDetails.setUser_id(user.getUser_id());
                orderDetails.setId(did);
                orderDetails = orderDetailsModelMapper.selectOne(orderDetails);
                if (orderDetails != null)
                {
                    sNo = orderDetails.getR_sNo();
                    if (orderModel == null)
                    {
                        orderModel = new OrderModel();
                        orderModel.setStore_id(storeId);
                        orderModel.setsNo(sNo);
                        orderModel = orderModelMapper.selectOne(orderModel);

                        if (orderModel == null)
                        {
                            logger.error("订单：{},不存在", sNo);
                            continue;
                        }

                        if (!isWholeOrderAfterSaler)
                        {
                            OrderDetailsModel orderDetailsTmp = new OrderDetailsModel();
                            orderDetailsTmp.setR_sNo(sNo);
                            List<OrderDetailsModel> orderTotalDetailsList = orderDetailsModelMapper.select(orderDetailsTmp);
                            //如果子订单和申请售后的订单数量一致 则为订单整体售后
                            isWholeOrderAfterSaler = orderTotalDetailsList.size() == orderDetaisdIds.length;
                        }


                    }

                    JSONObject jsonObject = JSONObject.parseObject(JSON.toJSONString(orderDetails));
                    rStatus.put(orderDetails.getR_status(), orderDetails.getR_status());
                    String attributeId = orderDetails.getSid();
                    int    sid         = 0;
                    if (!StringUtils.isEmpty(attributeId))
                    {
                        sid = Integer.parseInt(attributeId);
                    }
                    //是否再售后中,售后中不显示商品
                    if (returnOrderModelMapper.orderDetailReturnIsNotEnd(vo.getStoreId(), sNo, did) > 0)
                    {
                        logger.debug("订单{} 明细id{} 正在售后中,无法申请售后!", sNo, did);
                        continue;
                    }
                    //售后按钮逻辑
                    Map<String, Boolean> btnMap = publicRefundService.afterSaleButtonShow(storeId, orderModel.getOtype(), orderDetails);
                    boolean currentRefund = MapUtils.getBooleanValue(btnMap, "refund"), currentRefundAmt = MapUtils.getBooleanValue(btnMap, "refundAmt"),
                            currentRefundGoodsAmt = MapUtils.getBooleanValue(btnMap, "refundGoodsAmt"), currentRefundGoods = MapUtils.getBooleanValue(btnMap, "refundGoods");
                    if (!currentRefund && refund)
                    {
                        refund = false;
                    }
                    if (!currentRefundAmt && refundAmt)
                    {
                        refundAmt = false;
                    }
                    if (!currentRefundGoodsAmt && refundGoodsAmt)
                    {
                        refundGoodsAmt = false;
                    }
                    if (!currentRefundGoods && refundGoods)
                    {
                        refundGoods = false;
                    }
                    //售后按钮逻辑
                    jsonObject.putAll(btnMap);

                    refundPrice = refundPrice.add(publicOrderService.getOrderPrice(did, storeId));
                    logger.error("refundPrice第一次为y:{}", refundPrice);
                    if (isWholeOrderAfterSaler)
                    {
                        refundPrice = refundPrice.add(orderDetails.getFreight());
                        logger.error("refundPrice第二次为y:{}", refundPrice);
                    }
                    if (Objects.nonNull(orderDetails.getActualTotal()) && orderDetails.getActualTotal().compareTo(BigDecimal.ZERO) > 0)
                    {
                        refundPrice = refundPrice.subtract(orderDetails.getActualTotal());
                        logger.error("refundPrice第三次为y:{}", refundPrice);
//                        4279 【JAVA开发环境】会员：移动端--使用积分抵扣后，极速退款，退款金额显示0，实际是1块
//                        if (orderModel.getZ_price().compareTo(BigDecimal.ZERO) == 0)
//                        {
//                            refundPrice = BigDecimal.ZERO;
//                        }
                    }

                    if (sNo.contains(DictionaryConst.OrdersType.ORDERS_HEADER_IN))
                    {
                        refundPrice = refundPrice.add(publicOrderService.getOrderPrice(did, storeId));
                    }

                    //货币信息处理
                    if (orderModel != null)
                    {
                        String     orderCurrencyCode   = orderModel.getCurrency_code();
                        String     orderCurrencySymbol = orderModel.getCurrency_symbol();
                        BigDecimal orderExchangeRate   = orderModel.getExchange_rate();
                        jsonObject.put("currency_code", orderCurrencyCode);
                        jsonObject.put("currency_symbol", orderCurrencySymbol);
                        jsonObject.put("exchange_rate", orderExchangeRate);
                    }

                    ConfiGureModel confiGureModel = confiGureModelMapper.selectByPrimaryKey(sid);
                    if (confiGureModel != null)
                    {
                        String imgPath = publiceService.getImgPath(confiGureModel.getImg(), storeId);
                        jsonObject.put("image", imgPath);
                        jsonObject.put("imgurl", imgPath);
                        list.add(jsonObject);
                    }


                }
            }

            Integer selfLifting;
            String  orderType = null;
            if (orderModel != null)
            {
                selfLifting = orderModel.getSelf_lifting();
                orderType = orderModel.getOtype();
                String     orderCurrencyCode   = orderModel.getCurrency_code();
                String     orderCurrencySymbol = orderModel.getCurrency_symbol();
                BigDecimal orderExchangeRate   = orderModel.getExchange_rate();
                data.put("currency_code", orderCurrencyCode);
                data.put("currency_symbol", orderCurrencySymbol);
                data.put("exchange_rate", orderExchangeRate);
            }
            else
            {
                selfLifting = 0;

                //TODO 默认无订单处理待完善
                data.put("currency_code", "CNY");
                data.put("currency_symbol", "￥");
                data.put("exchange_rate", 1);
                //TODO 默认无订单处理待完善
            }

            for (Integer rstatustmp : rStatus.keySet())
            {
                if (rstatustmp == 2)
                {
                    status = true;
                    break;
                }
            }

            data.put("refund", refund);
            data.put("refundAmt", refundAmt);
            data.put("refundGoodsAmt", refundGoodsAmt);
            data.put("refundGoods", refundGoods);

            data.put("orderType", orderType);
            data.put("refund_price", refundPrice);
            data.put("self_lifting", selfLifting);
            data.put("list", list);
            data.put("status", status);
            return data;
        }
        catch (Exception e)
        {
            logger.error("获取售后信息 异常:", e);
            throw new LaiKeAPIException(ERROR_CODE_YWYC, "业务异常", "returnMethod");
        }
    }

    @Override
    public Map<String, Object> seeSend(int stroeId, int productId)
    {
        return null;
    }

    @Override
    public Map<String, Object> backSend(ReturnGoodsVo returnGoodsVo)
    {
        return null;
    }

    @Override
    public Map<String, Object> seeExtractionCode(OrderVo vo)
    {
        return null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> returndetails(RefundDetailsVo refundDetailsVo)
    {
        Map<String, Object>    resultMap       = Maps.newHashMap();
        List<ReturnOrderModel> returnOrderList = new ArrayList<>();
        try
        {
            // 订单/详情id
            int id      = refundDetailsVo.getId();
            int pid     = refundDetailsVo.getPid();
            int storeId = refundDetailsVo.getStoreId();

            //查询详情订单信息
            Map<String, Object> returnOrder;
            if (refundDetailsVo.getOrderDetailId() != null)
            {
                returnOrder = returnOrderModelMapper.getReturnOrderMap(storeId, null, refundDetailsVo.getOrderDetailId());
            }
            else
            {
                returnOrder = returnOrderModelMapper.getReturnOrderMap(storeId, id, null);
            }
            if (returnOrder == null)
            {
                throw new LaiKeAPIException(ERROR_CODE_SHXXBCZ, "售后信息不存在", "returnData");
            }


            String     sNo        = MapUtils.getString(returnOrder, "sNo");
            OrderModel orderModel = new OrderModel();
            orderModel.setsNo(sNo);
            orderModel = orderModelMapper.selectOne(orderModel);

            String     orderCurrencyCode   = orderModel.getCurrency_code();
            String     orderCurrencySymbol = orderModel.getCurrency_symbol();
            BigDecimal orderExchangeRate   = orderModel.getExchange_rate();
            resultMap.put("currency_code", StringUtils.isNotEmpty(orderCurrencyCode) ? orderCurrencyCode : "￥");
            resultMap.put("currency_symbol", StringUtils.isNotEmpty(orderCurrencySymbol) ? orderCurrencySymbol : "￥");
            resultMap.put("exchange_rate", orderExchangeRate == null ? BigDecimal.ONE : orderExchangeRate);


            //售后id
            int returnId = MapUtils.getIntValue(returnOrder, "id");
            //订单详情ID
            int details_id = DataUtils.getIntegerVal(returnOrder, "p_id");
            //商品id
            pid = MapUtils.getIntValue(returnOrder, "goodsId");
            //商品信息
            Map<String, Object> goodsInfo = new HashMap<>(16);
            goodsInfo.put("id", pid);
            goodsInfo.put("size", MapUtils.getString(returnOrder, "size"));
            goodsInfo.put("goodsName", MapUtils.getString(returnOrder, "p_name"));
            goodsInfo.put("price", CurrencyUtils.calculatePrice(MapUtils.getDouble(returnOrder, "p_price"), orderExchangeRate));
            goodsInfo.put("img", publiceService.getImgPath(MapUtils.getString(returnOrder, "img"), storeId));
            goodsInfo.put("mchName", MapUtils.getString(returnOrder, "mchName"));
            goodsInfo.put("mchLogo", publiceService.getImgPath(MapUtils.getString(returnOrder, "logo"), storeId));
            goodsInfo.put("headimg", publiceService.getImgPath(MapUtils.getString(returnOrder, "head_img"), storeId));
            goodsInfo.put("num", MapUtils.getInteger(returnOrder, "num"));
            goodsInfo.put("mchId", MapUtils.getInteger(returnOrder, "mchId"));

            //存入积分单价
            OrderDetailsModel orderDetailsTmp = new OrderDetailsModel();
            orderDetailsTmp.setR_sNo(sNo);
            orderDetailsTmp.setStore_id(storeId);
            OrderDetailsModel orderDetailsModel = orderDetailsModelMapper.selectOne(orderDetailsTmp);
            goodsInfo.put("p_integral", orderDetailsModel.getP_integral());

            Map<String, Object> send_info   = new HashMap<>(16);
            Map<String, Object> return_info = new HashMap<>(16);
            //查询买家回寄信息
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", refundDetailsVo.getStoreId());
            parmaMap.put("oid", details_id);
            parmaMap.put("re_id", returnId);
            parmaMap.put("pageStart", 0);
            parmaMap.put("pageEnd", 2);
            parmaMap.put("add_data_sort", DataUtils.Sort.DESC.toString());
            List<Map<String, Object>> returnGoodsMap = returnGoodsModelMapper.selectDynamic(parmaMap);

            if (returnGoodsMap != null && returnGoodsMap.size() > 0)
            {
                if (returnGoodsMap.size() > 1)
                {
                    //查询卖家退换信息
                    return_info = returnGoodsMap.get(0);
                    send_info = returnGoodsMap.get(1);
                }
                else
                {
                    send_info = returnGoodsMap.get(0);
                }
            }

            Map<String, Object> info = Maps.newHashMap();
            //退款信息
            //申请时间
            info.put("re_time", DateUtil.dateFormate(MapUtils.getString(returnOrder, "re_time"), GloabConst.TimePattern.YMDHMS));
            //退款金额
            BigDecimal p_price = DataUtils.getBigDecimalVal(returnOrder, "real_money", BigDecimal.ZERO);
            info.put("p_price", p_price);
            if (DataUtils.equalBigDecimalZero(p_price))
            {
                info.put("p_price", DataUtils.getBigDecimalVal(returnOrder, "re_apply_money", BigDecimal.ZERO));
            }

            //拒绝原因
            info.put("r_content", returnOrder.get("r_content"));
            //售后类型1
            info.put("re_type", returnOrder.get("re_type"));
            //售后商品名称
            info.put("p_name", returnOrder.get("p_name"));
            //售后订单
            info.put("r_sNo", sNo);

            //订单类型和积分
            resultMap.put("oType", orderModel.getOtype());
            resultMap.put("allow", orderModel.getAllow());

            //加购商品信息
            List<Map<String, Object>> addGoodList = getAddGoodList(sNo, storeId);
            if (org.apache.commons.collections.CollectionUtils.isNotEmpty(addGoodList))
            {
                Iterator<Map<String, Object>> iterator = addGoodList.iterator();
                while (iterator.hasNext())
                {
                    Map<String, Object> goodsMap = iterator.next();
                    Integer             goodsId  = MapUtils.getInteger(goodsMap, "goodsId");
                    if (pid == goodsId)
                    {
                        iterator.remove();
                    }
                }
                resultMap.put("addGoodsList", addGoodList);
            }

            //售后类型
            info.put("type", returnOrder.get("r_type"));
            //退货原因
            info.put("content", returnOrder.get("content"));
            //凭证
            String       re_photo   = DataUtils.getStringVal(returnOrder, "re_photo", "");
            List<String> imagesList = new ArrayList<>();
            if (!StringUtils.isEmpty(re_photo))
            {
                Map<Integer, String> photosMap = DataUtils.cast(SerializePhpUtils.getUnserializeObj(re_photo, Map.class));
                if (photosMap != null)
                {
                    for (Integer key : photosMap.keySet())
                    {
                        String img = photosMap.get(key);
                        // 获取图片路径
                        imagesList.add(publiceService.getImgPath(img, storeId));
                    }
                }
            }
            info.put("re_photo", imagesList.size() == 0 ? null : imagesList);
            //如果为有核销次数的虚拟还需要返回待核销次数
            if (returnOrder.containsKey("r_write_off_num") && returnOrder.containsKey("write_off_settings") && DataUtils.getIntegerVal(returnOrder, "write_off_settings") == 1)
            {
                info.put("r_write_off_num", MapUtils.getIntValue(returnOrder, "r_write_off_num"));
            }


            String address = "";
            String name    = "";
            String phone   = "";
            String cpc     = "86";

            //r0
            ProductListModel productListModel = productListModelMapper.selectProductByIdAndStoreId(pid, storeId);
            if (productListModel != null)
            {
                Integer zyMchId = customerModelMapper.getStoreMchId(refundDetailsVo.getStoreId());
                // 获取商城自营店 如果是自营店就获取平台售后地址，非自营店则获取店铺地址
                if (productListModel.getMch_id().equals(zyMchId))
                {
                    ServiceAddressModel serviceAddressModel = new ServiceAddressModel();
                    serviceAddressModel.setStore_id(storeId);
                    serviceAddressModel.setUid("admin");
                    // 0不是 1默认
                    serviceAddressModel.setIs_default(DictionaryConst.DefaultMaven.DEFAULT_OK);
                    // 1发货地址 2售后地址
                    serviceAddressModel.setType(ServiceAddressModel.TYPE_RETURN_GOODS);
                    serviceAddressModel = serviceAddressModelMapper.selectOne(serviceAddressModel);
                    address = serviceAddressModel.getAddress_xq();
                    name = serviceAddressModel.getName();
                    phone = serviceAddressModel.getTel();
                    cpc = serviceAddressModel.getCpc();
                }
                else
                {
                    MchModel mchModel = mchModelMapper.selectByPrimaryKey(productListModel.getMch_id());
                    if (mchModel != null)
                    {
                        address = mchModel.getSheng() + mchModel.getShi() + mchModel.getXian() + mchModel.getAddress();
                        name = mchModel.getRealname();
                        phone = mchModel.getTel();
                        cpc = mchModel.getCpc();
                    }
                }
                //如果是供应商商品则获取供应商地址
                if (StringUtils.isNotEmpty(productListModel.getGongyingshang()))
                {
                    SupplierModel supplierModel = supplierModelMapper.selectByPrimaryKey(productListModel.getGongyingshang());
                    if (supplierModel != null)
                    {
                        address = supplierModel.getProvince() + supplierModel.getCity() + supplierModel.getArea() + supplierModel.getAddress();
                        name = supplierModel.getContacts();
                        phone = supplierModel.getContact_phone();
                    }
                }

                //查询售后记录
                ReturnOrderModel returnOrderModel = new ReturnOrderModel();
                returnOrderModel.setP_id(pid);
                returnOrderModel.setStore_id(storeId);
                List<ReturnOrderModel> returnOrderModelList = returnOrderModelMapper.select(returnOrderModel);

                if (!CollectionUtils.isEmpty(returnOrderModelList))
                {
                    int pos = 0;
                    for (ReturnOrderModel returnOrderTmp : returnOrderModelList)
                    {
                        if (pos < returnOrderModelList.size() - 1)
                        {
                            returnOrderList.add(returnOrderTmp);
                        }
                    }
                }
            }
            else
            {
                logger.info("获取商品信息失败，{}", pid);
                throw new LaiKeAPIException(ERROR_CODE_YWYC, "业务异常", "returndetails");
            }
            Map<String, Object> store_info = Maps.newHashMap();
            store_info.put("address", address);
            store_info.put("name", name);
            store_info.put("phone", phone);
            store_info.put("cpc", cpc);
            resultMap.put("code", 200);
            resultMap.put("message", "操作成功");
            resultMap.put("info", info);
            resultMap.put("store_info", store_info);
            resultMap.put("record", returnOrderList);
            resultMap.put("send_info", send_info);
            resultMap.put("return_info", return_info);
            resultMap.put("goodsInfo", goodsInfo);
        }
        catch (LaiKeAPIException e)
        {
            logger.error(" 获取售后订单详情失败：", e);
            throw e;
        }
        catch (Exception e)
        {
            logger.error(" 获取售后订单详情失败：", e);
            throw new LaiKeAPIException(ERROR_CODE_YWYC, "业务异常", "returndetails");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> confirmReceipt(ReturnConfirmReceiptVo vo)
    {
        return null;
    }

    @Override
    public Map<String, Object> cancelApplication(CancleAfterSaleApplyVo params)
    {
        return null;
    }

    @Override
    public Map<String, Object> getPayment(OrderVo vo)
    {
        return null;
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
            map.put("mchId", detailsModel.getMch_id());
            map.put("mchLogo", publiceService.getImgPath(detailsModel.getLogo(), storeId));
            map.put("mchName", detailsModel.getMchName());

            addGoodsList.add(map);
        }
        return addGoodsList;
    }
}

