package com.laiketui.common.service.dubbo.order;

import com.alibaba.fastjson2.*;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Maps;
import com.laiketui.common.annotation.HandlerOrderType;
import com.laiketui.common.api.*;
import com.laiketui.common.api.order.OrderDubboService;
import com.laiketui.common.api.plugin.member.PubliceMemberService;
import com.laiketui.common.mapper.*;
import com.laiketui.common.utils.CpcUtils;
import com.laiketui.common.utils.tool.ImgUploadUtils;
import com.laiketui.common.utils.tool.cache.RedisDataTool;
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
import com.laiketui.domain.distribution.FreightModel;
import com.laiketui.domain.living.LivingCommissionModel;
import com.laiketui.domain.living.LivingProductModel;
import com.laiketui.domain.living.LivingRoomModel;
import com.laiketui.domain.log.RecordModel;
import com.laiketui.domain.mch.*;
import com.laiketui.domain.message.MessageLoggingModal;
import com.laiketui.domain.order.*;
import com.laiketui.domain.product.ProductListModel;
import com.laiketui.domain.product.StockModel;
import com.laiketui.domain.supplier.SupplierModel;
import com.laiketui.domain.supplier.SupplierOrderFrightModel;
import com.laiketui.domain.user.User;
import com.laiketui.domain.user.UserAddress;
import com.laiketui.domain.vo.OrderVo;
import com.laiketui.domain.vo.main.RefundVo;
import com.laiketui.domain.vo.order.*;
import com.laiketui.root.gateway.util.I18nUtils;
import org.apache.commons.codec.CharEncoding;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;
import tk.mybatis.mapper.entity.Example;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLDecoder;
import java.util.*;

import static com.laiketui.core.lktconst.DictionaryConst.OrdersStatus.ORDERS_R_STATUS_CLOSE;
import static com.laiketui.core.lktconst.DictionaryConst.OrdersStatus.ORDERS_R_STATUS_DISPATCHED;
import static com.laiketui.core.lktconst.ErrorCode.BizErrorCode.*;
import static com.laiketui.domain.order.OrderModel.*;

/**
 * @author zhuqingyu
 * @create 2024/6/6
 * <p>
 * 直播支付接口
 */
@HandlerOrderType(type = DictionaryConst.OrdersType.ORDERS_HEADER_ZB)
@Service
public class LivingOrderServiceImpl implements OrderDubboService
{
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private PubliceService           publiceService;
    @Autowired
    private RedisUtil                redisUtil;
    @Autowired
    private UserMapper               userMapper;
    @Autowired
    private PaymentConfigModelMapper paymentConfigModelMapper;
    @Autowired
    private PublicAddressService     commonAddressService;
    @Autowired
    private PublicOrderService       publicOrderService;
    @Autowired
    private PublicMchService         publicMchService;
    @Autowired
    private PublicCouponService      publicCouponService;
    @Autowired
    private PublicSubtractionService publicSubtractionService;
    @Autowired
    private MchBrowseModelMapper     mchBrowseModelMapper;
    @Autowired
    private OrderDetailsModelMapper  orderDetailsModelMapper;
    @Autowired
    private OrderModelMapper         orderModelMapper;
    @Autowired
    private CartModelMapper          cartModelMapper;
    @Autowired
    private BuyAgainModalMapper      buyAgainModalMapper;
    @Autowired
    private ConfiGureModelMapper     confiGureModelMapper;
    @Autowired
    private ProductListModelMapper   productListModelMapper;
    @Autowired
    private StockModelMapper         stockModelMapper;
    @Autowired
    private UserFirstModalMapper     userFirstModalMapper;
    @Autowired
    private ReturnOrderModelMapper   returnOrderModelMapper;

    @Autowired
    private ReturnRecordModelMapper returnRecordModelMapper;

    @Autowired
    private RecordModelMapper recordModelMapper;

    @Autowired
    private OrderConfigModalMapper orderConfigModalMapper;

    @Autowired
    private ServiceAddressModelMapper serviceAddressModelMapper;

    @Autowired
    private MchModelMapper mchModelMapper;

    @Autowired
    private ExpressModelMapper expressModelMapper;

    @Autowired
    private AdminModelMapper adminModelMapper;

    @Autowired
    private ReturnGoodsModelMapper returnGoodsModelMapper;

    @Autowired
    private PublicRefundService publicRefundService;

    @Autowired
    private PublicStockService publicStockService;

    @Autowired
    private MessageLoggingModalMapper messageLoggingModalMapper;

    @Autowired
    private CustomerModelMapper customerModelMapper;

    @Autowired
    private CouponOrderModalMapper couponOrderModalMapper;

    @Autowired
    private PubliceMemberService publiceMemberService;
    @Autowired
    private FreightModelMapper   freightModelMapper;

    @Autowired
    private AdminCgModelMapper adminCgModelMapper;

    @Autowired
    private SupplierOrderMapper supplierOrderMapper;

    @Autowired
    private SupplierOrderFrightModelMapper supplierOrderFrightModelMapper;

    @Autowired
    private SupplierModelMapper supplierModelMapper;

    @Autowired
    private CouponModalMapper couponModalMapper;

    @Autowired
    private LivingCommissionModelMapper livingCommissionModelMapper;

    @Autowired
    private PublicPaymentConfigService publicPaymentConfigService;

    @Override
    @Transactional
    public Map<String, Object> settlement(OrderVo vo) throws LaiKeAPIException
    {
        try
        {
            logger.debug("订单信息：{}", JSON.toJSONString(vo));
            // 获取用户信息 直接从redis 里面取 余额、收货信息
            User user;
            if (vo.getUser() != null)
            {
                user = vo.getUser();
            }
            else
            {
                user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
                user = userMapper.selectByPrimaryKey(user.getId());
            }
            String userId = user.getUser_id();

            String   paypswd          = user.getPassword();
            int      wrongtimes       = user.getLogin_num() == null ? 0 : user.getLogin_num();
            Date     verificationtime = new Date();
            Calendar calendar         = Calendar.getInstance();
            calendar.setTime(verificationtime);
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            verificationtime = calendar.getTime();
            Date now = new Date();
            //是否可以使用余额支付 默认false 表示不可以
            boolean enterless = false;
            if (wrongtimes == GloabConst.LktConfig.LOGIN_AGAIN_MAX)
            {
                if (!now.before(verificationtime))
                {
                    user.setLogin_num(0);
                    userMapper.updateByPrimaryKey(user);
                    enterless = true;
                }
            }
            else
            {
                enterless = true;
            }

            //是否有支付密码标记 0无;1有
            int passwordStatus = DictionaryConst.WhetherMaven.WHETHER_OK;
            if (StringUtils.isEmpty(paypswd))
            {
                passwordStatus = DictionaryConst.WhetherMaven.WHETHER_NO;
            }

            //各个支付的开启、关闭状态
            //各个支付的开启、关闭状态
            Map payInfo = new HashMap();
            int storeId = vo.getStoreId();
            payInfo = publicPaymentConfigService.getPaymentInfos(storeId);
            Map map = new HashMap();
            map.put("store_id", storeId);

            //获取用户的默认收货地址
            map.put("user_id", user.getUser_id());
            if (vo.getAddressId() != null && vo.getAddressId() != 0 && vo.getAddressId() != -1)
            {
                map.put("address_id", vo.getAddressId());
            }

            UserAddress userAddress = commonAddressService.findAddress(vo.getStoreId(), user.getUser_id(), vo.getAddressId());
            // 收货地址状态
            int addemt = userAddress == null ? 1 : 0;

            // 获取产品类型
            // [{"pid":"979"},{"cid":"5648"},{"num":1},{"sec_id":"6"}--秒杀id,{}] 运费、商品总价、
            //  cart_id: 3010,3009
            List<Map<String, Object>> productList = null;
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

            List<Map<String, Object>> productsListMap = publiceService.productsList(productList, vo.getCarts(), vo.getBuyType(), DictionaryConst.OrdersType.ORDERS_HEADER_ZB);
            if (CollectionUtils.isEmpty(productsListMap))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_KCBZ, "库存不足2", "settlement");
            }
            //订单中有供应商商品则不支持自提
            //不计算最优优惠卷
            boolean isZiti = true;
            for (Map<String, Object> productsList : productsListMap)
            {
                Integer          pid              = MapUtils.getInteger(productsList, "pid");
                ProductListModel productListModel = productListModelMapper.selectByPrimaryKey(pid);
                if (productListModel != null && StringUtils.isNotEmpty(productListModel.getGongyingshang()))
                {
                    isZiti = false;
                    vo.setCanshu(true);
                }
            }
            //按照店铺归类的商品、运费、商品总价等信息
            Map<String, Object> productsInfo = publiceService.settlementProductsInfo(productsListMap, vo.getStoreId(), vo.getProductType());
            //运费信息
            Map<String, List<Map<String, Object>>> productsFreight = (Map<String, List<Map<String, Object>>>) productsInfo.get("products_freight");
            //计算会员的产品价格和订单产品总价
            List<Map<String, Object>> products = (List<Map<String, Object>>) productsInfo.get("products");
            //计算会员优惠价格
            MemberPriceVo memberPriceVo = new MemberPriceVo();
            memberPriceVo.setUserId(userId);
            memberPriceVo.setStoreId(vo.getStoreId());
            memberPriceVo.setMchProductList(products);
            Map<String, Object> memberProductsInfo = publiceService.getMemberPrice(memberPriceVo, vo.getVipSource());
            //订单产品总计
            BigDecimal orderProductsTotal = DataUtils.getBigDecimalVal(memberProductsInfo, "products_total");
            //拿出商品信息
            productsListMap = (List<Map<String, Object>>) memberProductsInfo.get("products");
            BigDecimal gradeRate = DataUtils.getBigDecimalVal(memberProductsInfo, "grade_rate");

            //计算店铺运费、总订单运费
            BigDecimal yunfei = BigDecimal.ZERO;
            productsInfo = publicOrderService.getFreight(productsFreight, productsListMap, userAddress, vo.getStoreId(), vo.getProductType());
            products = (List<Map<String, Object>>) productsInfo.get("products");
            //是否是自提
            if (vo.getShopAddressId() != null && vo.getShopAddressId() != 0)
            {
                for (Map<String, Object> mchProductsInfo : products)
                {
                    //自提免运费
                    mchProductsInfo.put("freight_price", BigDecimal.ZERO);
                    BigDecimal productTotal = DataUtils.getBigDecimalVal(mchProductsInfo, "product_total");
                    BigDecimal freightPrice = DataUtils.getBigDecimalVal(mchProductsInfo, "freight_price");
                    mchProductsInfo.put("product_total", productTotal.subtract(freightPrice));
                }
            }
            else
            {
                yunfei = DataUtils.getBigDecimalVal(productsInfo, "yunfei");
            }

            //门点自提信息
            Map<String, Object> mdztInfo = new HashMap<>();
            //门店自提结算
            int shopStatus = 0;
            //店铺信息
            MchStoreModel mchStoreModel = null;
            if (products.size() == 1)
            {
                int mchId = (int) products.get(0).get("shop_id");
                mdztInfo = publicMchService.settlement(vo.getStoreId(), mchId, "", vo.getShopAddressId() == null ? 0 : vo.getShopAddressId(), vo.getStoreType());
                shopStatus = (int) mdztInfo.get("shop_status");
                mchStoreModel = (MchStoreModel) mdztInfo.get("mch_store_info");
                mchStoreModel.setAddress((String) mdztInfo.get("address"));
            }


            //平台优惠券数组 // 平台优惠数组
            List<Map<String, Object>> discountList = new ArrayList<>();
            // 满减id
            int subtractionId = 0;
            // 优惠ID数组
            List<Integer> couponIdList = new ArrayList<>();

            //优惠券id拼接字符串
            String resultCouponId = "";

            String orgCouponStr = vo.getCouponId();

            String[] platformDiscountIdList = vo.getCouponId().split(SplitUtils.DH);
            if (platformDiscountIdList.length == 2)
            {
                platformDiscountIdList = Arrays.copyOf(platformDiscountIdList, platformDiscountIdList.length + 1);
                platformDiscountIdList[2] = GloabConst.ManaValue.NODISCOUNT;
            }
            // 最后一调数据的键名
            int platformDiscountTypePos = platformDiscountIdList.length - 1;
            // 平台优惠的健名
            int platformDiscountPos = platformDiscountIdList.length - 2;
            // 优惠类型 三种 ：coupon，substration，nodiscount：第一次进来的时候为 0｜'' 1，12,0,nodiscount
            String discountType = platformDiscountIdList[platformDiscountTypePos];
            //String coupon:platformDiscountIdList
            for (int i = 0; i < platformDiscountIdList.length - 1; i++)
            {
                if (OrderVo.SUBTRACTION.equals(discountType))
                {
                    if (i == platformDiscountPos)
                    {
                        subtractionId = Integer.parseInt(platformDiscountIdList[i]);
                    }
                }
                couponIdList.add(Integer.valueOf(platformDiscountIdList[i]));
            }
            //优惠券id字符串只用来传参
            String couponIds = Joiner.on(SplitUtils.DH).join(couponIdList);

            // 满减--插件
            int autojian = 0;
            // 满减标记
            int isSubtraction = 0;
            // 减掉的金额
            BigDecimal reduceMoney = DataUtils.ZERO_BIGDECIMAL;
            // 满减名
            String reduceName = "";
            // 优惠券金额
            BigDecimal couponMoney = DataUtils.ZERO_BIGDECIMAL;
            // 优惠券名
            String couponName = "";
            //
            boolean couponStatus = DataUtils.FALSE;
            //会员订单不计算最优优惠卷
            if (vo.getVipSource().equals(DictionaryConst.WhetherMaven.WHETHER_OK))
            {
                vo.setCanshu(true);
            }
            // 获取店铺的优惠券
            Integer is_self_driver = vo.getIs_self_delivery();
            if (is_self_driver == null)
            {
                is_self_driver = 0;
            }
            Map<String, Object> mchCouponsMap = publicCouponService.settlementStoreCoupons(vo.getStoreId(), userId, products, couponIds, vo.getCanshu(), is_self_driver);
            resultCouponId = (String) mchCouponsMap.get("coupon_id");
            // 店铺优惠之和
            BigDecimal mchPreferentialAmount = DataUtils.getBigDecimalVal(mchCouponsMap, "preferential_amount");

            //平台优惠券使用
//            Map<String, Object> paltCouponsMap = publicCouponService.settlementPlaformCoupons(vo.getStoreId(), userId, products, couponIds, vo.getCanshu());
            //未选择优惠卷会重新拼接优惠卷
//            if (StringUtils.isNotEmpty(MapUtils.getString(paltCouponsMap, "couponIds"))) {
//                couponIds = MapUtils.getString(paltCouponsMap, "couponIds");
//                platformDiscountIdList = couponIds.split(SplitUtils.DH);
//                // 最后一调数据的键名
//                platformDiscountTypePos = platformDiscountIdList.length - 1;
//                // 平台优惠的健名
//                platformDiscountPos = platformDiscountIdList.length - 2;
//            }
//            // 优惠类型 三种 ：coupon，substration，nodiscount：第一次进来的时候为 0｜'' 1，12,0,nodiscount
//            discountType = platformDiscountIdList[platformDiscountTypePos];
//            products = (List<Map<String, Object>>) paltCouponsMap.get("products");
//            List<Map<String, Object>> couponList = (List<Map<String, Object>>) paltCouponsMap.get("list");
//            if (couponList.size() > 0) {
//                discountList = couponList;
//            }

            //满减 是否打开了满减插件，打开了则进行满减,选择了满减则校验当前满减券是否满足条件
//            Map<String, Object> autoSubtractionsMap = publicSubtractionService.autoSubtraction(vo.getStoreId(), products, subtractionId);
//            if (autoSubtractionsMap.containsKey("is_subtraction")) {
//                isSubtraction = DataUtils.getIntegerVal(autoSubtractionsMap, "is_subtraction");
//                if (isSubtraction == 1) {
//                    reduceMoney = DataUtils.getBigDecimalVal(autoSubtractionsMap, "reduce_money").setScale(2, BigDecimal.ROUND_DOWN);
//                    reduceName = DataUtils.getStringVal(autoSubtractionsMap, "reduce_name_array");
//                    products = (List<Map<String, Object>>) autoSubtractionsMap.get("products");
//                    //可用的满减id集合
//                    List<Map<String, Object>> autoSubtractionsList = (List<Map<String, Object>>) autoSubtractionsMap.get("subtraction_list");
//                    //把所有可用的满减放到平台优惠券id集里面
//                    discountList.addAll(autoSubtractionsList);
//                }
//            }

            // 如果选择了使用优惠券则 需要添加一条不使用
            Map<String, Object> noConpouSelectItem = new HashMap<>();
            noConpouSelectItem.put("coupon_id", "0");
            noConpouSelectItem.put("money", BigDecimal.ZERO);
            noConpouSelectItem.put("coupon_name", I18nUtils.getRawMessage("coupon.use.none"));
            noConpouSelectItem.put("discount_type", GloabConst.ManaValue.NODISCOUNT);
            if (StringUtils.isEmpty(discountType) || "0".equals(discountType) || "no_discount".equals(discountType))
            {
                noConpouSelectItem.put("coupon_status", true);
            }
            else
            {
                boolean isChecked = discountList.size() < 1;
                noConpouSelectItem.put("coupon_status", isChecked);
            }
            discountList.add(noConpouSelectItem);

            int discount = 1;
            // 平台优惠类型的数量
            int platCouponNum1 = 0;
            if (!CollectionUtils.isEmpty(discountList) && discountList.size() > 0)
            {
                platCouponNum1 = discountList.size() - 1;
            }

            //优惠金额
            BigDecimal preferentialAmount = DataUtils.ZERO_BIGDECIMAL;
            //会员等级金额
            BigDecimal gradeRateAmount = DataUtils.ZERO_BIGDECIMAL;
            //实际付款金额
            BigDecimal payTotal;
            //共优惠金额
            BigDecimal totalDiscount = BigDecimal.ZERO;
            if (!StringUtils.isEmpty(vo.getGradeLevel()) && vo.getGradeLevel() != 0)
            {
                //优惠金额=商品价格
                totalDiscount = orderProductsTotal;
                // 是商品兑换券
                orderProductsTotal = DataUtils.ZERO_BIGDECIMAL;
                // 实际付款金额 = 运费
                payTotal = yunfei;
            }
            else
            {

                if (OrderVo.SUBTRACTION.equals(discountType))
                {
                    // 平台优惠金额
                    preferentialAmount = reduceMoney;
                }
                else if (OrderVo.COUPON.equals(discountType))
                {
                    for (Map<String, Object> couponInfo : discountList)
                    {
                        //为false的时候使用了优惠券
                        boolean couponStatusTmp = DataUtils.getBooleanVal(couponInfo, "coupon_status", false);
                        if (couponStatusTmp)
                        {
                            // 平台优惠金额
                            preferentialAmount = DataUtils.getBigDecimalVal(couponInfo, "money");
                            //1免邮 2满减 3折扣 4会员赠送
                            int activityType = MapUtils.getIntValue(couponInfo, "activity_type");
                            //使用平台免邮卷
                            if (activityType == 1 && vo.getGradeLevel() == 0)
                            {
                                yunfei = BigDecimal.ZERO;
                                for (Map<String, Object> mchProduct : products)
                                {
                                    mchProduct.put("freight_price", BigDecimal.ZERO);
                                    List<Map<String, Object>> onlyProducts = (List<Map<String, Object>>) mchProduct.get("list");
                                    for (Map<String, Object> product : onlyProducts)
                                    {
                                        product.put("freight_price", BigDecimal.ZERO);
                                    }
                                    //将店铺免邮卷全部排除 2023-09-01 gp
                                    List<Map<String, Object>> mchCouponList = JSON.parseObject(JSON.toJSONString(mchProduct.get("coupon_list")), new TypeReference<List<Map<String, Object>>>()
                                    {
                                    });
                                    ArrayList<Object>         objects       = new ArrayList<>();
                                    mchCouponList.forEach(map1 ->
                                    {
                                        if (!CouponModal.COUPON_TYPE_MY.equals(MapUtils.getString(map1, "activityType")))
                                        {
                                            objects.add(map1);
                                        }
                                    });
                                    mchProduct.put("coupon_list", objects);
                                }
                            }
                        }
                    }
                }

                //重新计算总运费
                yunfei = BigDecimal.ZERO;
                for (Map<String, Object> mchProductsInfo : products)
                {
                    gradeRateAmount = gradeRateAmount.add(DataUtils.getBigDecimalVal(mchProductsInfo, "grade_rate_amount"));
                    //重新计算总运费
                    yunfei = yunfei.add(new BigDecimal(MapUtils.getString(mchProductsInfo, "freight_price")));
                }
                // 商品总价-店铺优惠之和-平台优惠+总运费
                BigDecimal youHui = mchPreferentialAmount.add(preferentialAmount);
                if (vo.getVipSource().equals(DictionaryConst.WhetherMaven.WHETHER_OK))
                {
                    //会员商品计算了会员优惠--支付金额减去会员优惠金额
                    youHui = youHui.add(gradeRateAmount);
                }
                //如果优惠金额大于支付金额则金额为0+运费
                if (youHui.compareTo(orderProductsTotal) > 0)
                {
                    payTotal = BigDecimal.ZERO.add(yunfei);
                    //优惠金额大于支付金额--》优惠金额=商品价格
                    totalDiscount = orderProductsTotal;
                }
                else
                {
                    payTotal = orderProductsTotal.subtract(youHui).add(yunfei);
                    //优惠金额
                    totalDiscount = mchPreferentialAmount.add(preferentialAmount).add(gradeRateAmount);
                }
            }
/*            //最少支付金额0.01
            if (new BigDecimal("0.01").compareTo(payTotal) >= 0) {
                payTotal = new BigDecimal("0.01");
            }
            if (new BigDecimal("0.01").compareTo(orderProductsTotal) >= 0) {
                orderProductsTotal = new BigDecimal("0.01");
            }*/
/*            //禅道48455 可以支付0元
            if (BigDecimal.ZERO.compareTo(payTotal) >= 0) {
                payTotal = BigDecimal.ZERO;
            }
            if (BigDecimal.ZERO.compareTo(orderProductsTotal) >= 0) {
                orderProductsTotal = BigDecimal.ZERO;
            }*/
            //52195不管使用哪一种第三方支付方式，最终的支付金额都可以按0.01进行支付处理。余额支付，倒是可以按0处理；
            if (BigDecimal.ZERO.compareTo(payTotal) >= 0)
            {
                if (StringUtils.isNotEmpty(vo.getPayType()) &&
                        vo.getPayType().equals(DictionaryConst.OrderPayType.ORDERPAYTYPE_WALLET_PAY))
                {
                    payTotal = BigDecimal.ZERO;
                }
                else
                {
                    payTotal = new BigDecimal("0.01");
                }
            }
            if (BigDecimal.ZERO.compareTo(orderProductsTotal) >= 0)
            {
                if (vo.getPayType().equals(DictionaryConst.OrderPayType.ORDERPAYTYPE_WALLET_PAY))
                {
                    orderProductsTotal = BigDecimal.ZERO;
                }
                else
                {
                    orderProductsTotal = new BigDecimal("0.01");
                }
            }
            //5.返回数据
            if (vo.getCanshu())
            {
                couponIds = resultCouponId + ",0";
            }
            Map<String, Object> resultMap = new HashMap<>();
            //支付密码错误一天超过5此不允许再使用余额支付
            resultMap.put("enterless", enterless);
            if (DictionaryConst.OrdersType.ORDERS_HEADER_ZB.equals(vo.getType()))
            {
                shopStatus = 0;
            }
            // 自提标记 1为自提
            resultMap.put("shop_status", shopStatus);
            // 门店自提信息
            resultMap.put("shop_list", mchStoreModel);
            // 支付方式信息状态
            resultMap.putAll(payInfo);
            // 商品列表 里面包含了店铺优惠券列表
            resultMap.put("products", products);
            // 是否分销
            resultMap.put("is_distribution", vo.getIsDistribution());
            // 密码状态
            resultMap.put("password_status", passwordStatus);
            // 商品总价
            resultMap.put("products_total", orderProductsTotal);
            // 用户余额
            resultMap.put("user_money", user.getMoney());
            // 实际支付金额
            resultMap.put("total", payTotal);

            resultMap.put("freight", yunfei);
            // 用户地址
            resultMap.put("address", userAddress == null ? new UserAddress() : userAddress);
            // 是否有收货地址
            resultMap.put("addemt", addemt);
            // 优惠券id
            resultMap.put("coupon_id", orgCouponStr);
            // 平台优惠类型的数量
            resultMap.put("coupon_num", platCouponNum1);
            // 平台优惠
            resultMap.put("preferential_amount", preferentialAmount);
            // 平台优惠券列表
            resultMap.put("coupon_list", discountList);
            // 是否满减
            resultMap.put("is_subtraction", isSubtraction);
            // 店铺优惠
            resultMap.put("mch_preferential_amount", mchPreferentialAmount);
            // 总折扣 会员优惠总金额
            resultMap.put("total_discount", totalDiscount);
            // 会员等级金额
            resultMap.put("grade_rate_amount", gradeRateAmount.setScale(2, BigDecimal.ROUND_HALF_DOWN).toString());
            // 会员等级折扣
            resultMap.put("grade_rate", gradeRate);
            // 满减名   TODO（可删掉）
            resultMap.put("reduce_name", reduceName);
            // 满减优惠 TODO（可删掉）
            resultMap.put("reduce_money", reduceMoney);
            // TODO  这以下几个key 是直接使用的声明时候的值
            // 优惠金额
            resultMap.put("coupon_money", couponMoney);
            //  优惠券状态
            resultMap.put("coupon_status", couponStatus);
            //  优惠 固定写成1了 ？
            resultMap.put("discount", discount);
            //  状态 固定写成1了 ？
            resultMap.put("status", "1");
            // 优惠文字
            resultMap.put("coupon_name", couponName);
            // 是否有供应商商品
            resultMap.put("is_supplier_pro", isZiti);
            return resultMap;
        }
        catch (LaiKeAPIException e)
        {
            logger.error("结算订单自定义 异常", e);
            throw e;
        }
        catch (Exception e)
        {
            logger.error("结算订单 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "settlement");
        }

    }

    @Autowired
    private LivingRoomModelMapper livingRoomModelMapper;

    @Autowired
    private LivingProductModelMapper livingProductModelMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> payment(OrderVo vo)
    {
        logger.debug("下单信息：{}", JSON.toJSONString(vo));
        Map<String, Object> resultMap = new HashMap<>();
        try
        {
            // 1.数据准备
            // 获取用户信息 直接从redis 里面取 余额、收货信息
            User user;
            if (vo.getUser() != null)
            {
                user = vo.getUser();
            }
            else
            {
                user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil);
            }
            //更新用户信息
            user = userMapper.selectByPrimaryKey(user.getId());
            int storeId = vo.getStoreId();
            // 用户id
            String userId = user.getUser_id();
            // 商品总价
            BigDecimal productsTotal = BigDecimal.ZERO;
            //  商品数组--------['pid'=>66,'cid'=>88]
            List<Map<String, Object>> products = new ArrayList<>();
            // 用户使用积分
            int allow = vo.getAllow();
            // 门店地址id
            if (vo.getShopAddressId() == null)
            {
                vo.setShopAddressId(0);
            }
            int shopAddressId = vo.getShopAddressId();
            // 订单备注
            String    remarksJson   = vo.getRemarks();
            JSONArray remarkJsonarr = null;
            if (!StringUtils.isEmpty(remarksJson))
            {
                remarkJsonarr = JSON.parseArray(remarksJson);
            }

            // 提交状态 1是再次购买 空是正常提交
            // 支付类型
            String payType = vo.getPayType();
            logger.info("订单支付类型：{}", payType);
            // 订单总价
            BigDecimal total;
            // 3.区分购物车结算和立即购买---列出选购商品
            List<Map<String, Object>> productList = null;
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
            //先判断直播商品卖完没有

            products = publiceService.productsList(productList, vo.getCarts(), vo.getBuyType(), DictionaryConst.OrdersType.ORDERS_HEADER_ZB);
            if (CollectionUtils.isEmpty(products))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_KCBZ, "库存不足1", "payment");
            }
            // 4.查询默认地址
            Map<String, Object> map = new HashMap<>();
            map.put("store_id", vo.getStoreId());

            //获取用户的默认收货地址
            map.put("user_id", user.getUser_id());
            map.put("address_id", vo.getAddressId());
            UserAddress userAddress = commonAddressService.findAddress(map);
            //禅道 50066  自提订单没有收货地址时自提允许自己填
            if (Objects.isNull(userAddress.getId()) && shopAddressId == 0)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QXZSHDZ, "请填写至少一个收货地址", "payment");
            }
            // 存储收货信息
            String mobile    = userAddress.getTel();
            String name      = userAddress.getName();
            String sheng     = userAddress.getSheng();
            String shi       = userAddress.getCity();
            String xian      = userAddress.getQuyu();
            String addressXq = userAddress.getAddress();

            // 5.列出商品数组-计算总价和优惠，拿商品运费ID
            Map<String, Object> productsInfo = publiceService.settlementProductsInfo(products, vo.getStoreId(), vo.getProductType());
            //运费信息
            Map<String, List<Map<String, Object>>> productsFreight = (Map<String, List<Map<String, Object>>>) productsInfo.get("products_freight");
            //计算会员的产品价格和订单产品总价
            products = (List<Map<String, Object>>) productsInfo.get("products");
            //计算会员优惠价格
            MemberPriceVo memberPriceVo = new MemberPriceVo();
            memberPriceVo.setUserId(userId);
            memberPriceVo.setStoreId(vo.getStoreId());
            memberPriceVo.setMchProductList(products);
            Map<String, Object> memberProductsInfo = publiceService.getMemberPrice(memberPriceVo, vo.getVipSource());
            //拿出商品信息
            products = (List<Map<String, Object>>) memberProductsInfo.get("products");
            BigDecimal gradeRate = DataUtils.getBigDecimalVal(memberProductsInfo, "grade_rate");
            productsTotal = DataUtils.getBigDecimalVal(memberProductsInfo, "products_total");

            //计算店铺运费、总订单运费
            BigDecimal yunfei = BigDecimal.ZERO;
            productsInfo = publicOrderService.getFreight(productsFreight, products, userAddress, vo.getStoreId(), vo.getProductType());
            products = (List<Map<String, Object>>) productsInfo.get("products");
            //
            if (vo.getShopAddressId() != 0)
            {
                yunfei = BigDecimal.ZERO;
                for (Map<String, Object> mchProductsInfo : products)
                {
                    mchProductsInfo.put("freight_price", BigDecimal.ZERO);
                    BigDecimal productTotal = DataUtils.getBigDecimalVal(mchProductsInfo, "product_total");
                    BigDecimal freightPrice = DataUtils.getBigDecimalVal(mchProductsInfo, "freight_price");
                    mchProductsInfo.put("product_total", productTotal.subtract(freightPrice));
                }
            }
            else
            {
                yunfei = DataUtils.getBigDecimalVal(productsInfo, "yunfei");
            }

            // 定义初始化数据
            int totalNum = 0;
            int discount = 1;
            // 直播
            String otype             = DictionaryConst.OrdersType.ORDERS_HEADER_ZB;
            int    orderStatus;
            int    shopStatus        = 0;
            String extractionCode    = "";
            String extractionCodeImg = "";

            // 赠品ID
            int giveId = 0;
            // 满减ID
            int subtractionId = 0;
            // 满减优惠金额
            BigDecimal reduceMoney = BigDecimal.ZERO;
            // 满减名称
            String reduceNameArray = "";
            // 优惠券优惠金额
            BigDecimal couponMoney = BigDecimal.ZERO;
            //如果为0元订单，则订单状态为 1-已发货
            if (vo.getGradeLevel() != 0 && (DataUtils.equalBigDecimalZero(yunfei)))
            {
                orderStatus = 1;
            }
            else
            {
                //待付款
                orderStatus = 0;
            }

            //自提二维码生成、运费处理
            if (shopAddressId != 0)
            {
                int                 mchId   = DataUtils.getIntegerVal(products.get(0), "shop_id");
                Map<String, Object> shopMap = publicMchService.settlement(vo.getStoreId(), mchId, "payment", shopAddressId, vo.getStoreType());
                sheng = DataUtils.getStringVal(shopMap, "sheng");
                shi = DataUtils.getStringVal(shopMap, "shi");
                xian = DataUtils.getStringVal(shopMap, "xian");
                addressXq = DataUtils.getStringVal(shopMap, "address");
                shopStatus = DataUtils.getIntegerVal(shopMap, "shop_status");
                extractionCode = DataUtils.getStringVal(shopMap, "extraction_code");
                extractionCodeImg = DataUtils.getStringVal(shopMap, "extraction_code_img");
                yunfei = BigDecimal.ZERO;
                mobile = vo.getFullPhone();
                name = vo.getFullName();
            }

            // 生成订单号
            String sNo = publicOrderService.createOrderNo(vo.getType());
            // 生成支付订单号
            String        realSno   = publicOrderService.createOrderNo(vo.getType());
            StringBuilder mchId     = new StringBuilder();
            List<Integer> mchIdList = new ArrayList<>();

            /**
             * 店铺订单备注
             */
            Map<String, String> mchRemarks = new HashMap<>(16);
            // 是否有备注标记
            boolean      remarksStatus = false;
            List<String> couponIdList  = new ArrayList<>();
            //优惠券id拼接字符串
            String[] platformDiscountIdList = vo.getCouponId().split(SplitUtils.DH);
            if (platformDiscountIdList.length == 2)
            {
                platformDiscountIdList = Arrays.copyOf(platformDiscountIdList, platformDiscountIdList.length + 1);
                platformDiscountIdList[2] = GloabConst.ManaValue.NODISCOUNT;
            }
            // 最后一调数据的键名
            int platformDiscountTypePos = platformDiscountIdList.length - 1;
            // 平台优惠的健名
            int platformDiscountPos = platformDiscountIdList.length - 2;
            // 优惠类型 三种 ：coupon，substration，nodiscount：第一次进来的时候为 0｜'' 1，12,0,nodiscount
            String discountType = platformDiscountIdList[platformDiscountTypePos];
            //是否使用优惠劵
            boolean isCoupon = false;
            for (int i = 0; i < platformDiscountIdList.length - 1; i++)
            {
                String couponId = platformDiscountIdList[i];
                if (OrderVo.SUBTRACTION.equals(discountType))
                {
                    if (i == platformDiscountPos)
                    {
                        subtractionId = Integer.parseInt(couponId);
                        couponIdList.add("0");
                        break;
                    }
                }
                if (!couponId.equals("0"))
                {
                    isCoupon = true;
                }
                couponIdList.add(couponId);
            }
            //优惠券id字符串只用来传参
            String couponIds = Joiner.on(SplitUtils.DH).join(couponIdList);
            //判断下单使用的优惠劵是否已经使用
            if (couponIdList.size() > 0 && isCoupon)
            {
                map = new HashMap<>();
                map.put("store_id", storeId);
                map.put("user_id", userId);
                map.put("idList", couponIdList);
                if (couponModalMapper.countCouponIsUser(map) > 0)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YHJYSYONG, "优惠劵已使用请重新下单", "payment");
                }
            }
            // 获取店铺的优惠券
            Integer is_self_driver = vo.getIs_self_delivery();
            if (is_self_driver == null)
            {
                is_self_driver = 0;
            }
            Map<String, Object> mchCouponsMap = publicCouponService.settlementStoreCoupons(vo.getStoreId(), userId, products, couponIds, vo.getCanshu(), is_self_driver);
            couponIds = DataUtils.getStringVal(mchCouponsMap, "coupon_id");
            products = (List<Map<String, Object>>) mchCouponsMap.get("products");
            // 店铺优惠之和
            BigDecimal mchPreferentialAmount = DataUtils.getBigDecimalVal(mchCouponsMap, "preferential_amount");
            // 店铺优惠券金额
            couponMoney = mchPreferentialAmount;
            //会员等级金额
            BigDecimal gradeRateAmount     = DataUtils.ZERO_BIGDECIMAL;
            BigDecimal preferential_amount = BigDecimal.ZERO;
            int        is_subtraction;
            if (vo.getGradeLevel() != 0)
            {
                //会员特惠赠品
                productsTotal = BigDecimal.ZERO;
                total = yunfei;
                otype = DictionaryConst.OrdersType.ORDERS_HEADER_VIP_GIVE;
            }
            else
            {
                if (OrderVo.SUBTRACTION.equals(discountType))
                {
                    Map<String, Object> autoSubtractionsMap = publicSubtractionService.autoSubtraction(vo.getStoreId(), products, subtractionId);
                    if (autoSubtractionsMap.containsKey("is_subtraction"))
                    {
                        is_subtraction = (int) autoSubtractionsMap.get("is_subtraction");
                        if (is_subtraction == 1)
                        {
                            //
                            products = (List<Map<String, Object>>) autoSubtractionsMap.get("products");
                            // 赠品ID
                            giveId = DataUtils.getIntegerVal(autoSubtractionsMap, "give_id");
                            // 满减ID
                            subtractionId = DataUtils.getIntegerVal(autoSubtractionsMap, "subtraction_id");
                            // 满减金额
                            reduceMoney = DataUtils.getBigDecimalVal(autoSubtractionsMap, "reduce_money");
                            //
                            reduceNameArray = DataUtils.getStringVal(autoSubtractionsMap, "reduce_name_array");
                        }
                    }
                    // 平台优惠金额
                    preferential_amount = reduceMoney;
                }
                else if (OrderVo.COUPON.equals(discountType))
                {
                    Map<String, Object> paltCouponsMap = publicCouponService.settlementPlaformCoupons(vo.getStoreId(), userId, products, couponIds, vo.getCanshu());
                    products = (List<Map<String, Object>>) paltCouponsMap.get("products");
                    List<Map<String, Object>> couponList = (List<Map<String, Object>>) paltCouponsMap.get("list");
                    for (Map<String, Object> platformCoupon : couponList)
                    {
                        boolean coupon_status = MapUtils.getBooleanValue(platformCoupon, "coupon_status");
                        if (coupon_status)
                        {
                            BigDecimal money = DataUtils.getBigDecimalVal(platformCoupon, "money");
                            // 平台优惠金额
                            preferential_amount = money;
                            couponMoney = couponMoney.add(money);
                            int activity_type = DataUtils.getIntegerVal(platformCoupon, "activity_type");
                            if (activity_type == 1 && vo.getGradeLevel() == 0)
                            {
                                yunfei = BigDecimal.ZERO;
                                for (Map<String, Object> mchProduct : products)
                                {
                                    mchProduct.put("freight_price", BigDecimal.ZERO);
                                    List<Map<String, Object>> onlyProducts = (List<Map<String, Object>>) mchProduct.get("list");
                                    for (Map<String, Object> product : onlyProducts)
                                    {
                                        product.put("freight_price", BigDecimal.ZERO);
                                    }
                                }
                            }
                        }
                    }
                }
                //重新计算总运费
                yunfei = BigDecimal.ZERO;
                for (Map<String, Object> mchProductsInfo : products)
                {
                    //重新计算总运费
                    yunfei = yunfei.add(new BigDecimal(MapUtils.getString(mchProductsInfo, "freight_price")));
                    gradeRateAmount = gradeRateAmount.add(DataUtils.getBigDecimalVal(mchProductsInfo, "grade_rate_amount"));
                }
                // 商品总价-店铺优惠之和-平台优惠+总运费
                BigDecimal youHui = mchPreferentialAmount.add(preferential_amount);
                if (StringUtils.isNotEmpty(vo.getVipSource()) && vo.getVipSource().equals(DictionaryConst.WhetherMaven.WHETHER_OK))
                {
                    //会员商品计算了会员优惠--支付金额减去会员优惠金额
                    youHui = youHui.add(gradeRateAmount);
                }
                //如果优惠金额大于支付金额则金额为0+运费
                if (youHui.compareTo(productsTotal) > 0)
                {
                    total = BigDecimal.ZERO.add(yunfei);
                }
                else
                {
                    total = productsTotal.subtract(youHui).add(yunfei);
                }
            }
            int pos = 0;
            //会员优惠总价
            BigDecimal preferential = BigDecimal.ZERO;
            for (Map<String, Object> mchProduct : products)
            {
                String shopId = String.valueOf(mchProduct.get("shop_id"));
                mchId.append(shopId).append(SplitUtils.DH);
                if (remarkJsonarr != null)
                {
                    String tmpDesc = remarkJsonarr.getString(pos++);
                    if (!StringUtils.isEmpty(tmpDesc))
                    {
                        mchRemarks.put(shopId, tmpDesc);
                        remarksStatus = true;
                    }
                }
                preferential = preferential.add(new BigDecimal(MapUtils.getString(mchProduct, "grade_rate_amount")));
                mchIdList.add(Integer.parseInt(shopId));
                //如果是多店铺，添加一条购买记录
                MchBrowseModel mchBrowseModel = new MchBrowseModel();
                mchBrowseModel.setMch_id(shopId);
                mchBrowseModel.setStore_id(storeId);
                mchBrowseModel.setUser_id(userId);
                mchBrowseModel.setEvent("购买了商品");
                mchBrowseModel.setAdd_time(new Date());
                mchBrowseModelMapper.insertSelective(mchBrowseModel);
                List<Map<String, Object>> onlyProducts = (List<Map<String, Object>>) mchProduct.get("list");
                for (Map<String, Object> product : onlyProducts)
                {
                    int pid = DataUtils.getIntegerVal(product, "pid");
                    //根据id获取运费信息
                    //根据id查询商品表  新增不配送区域判断
                    ProductListModel productListModel = productListModelMapper.selectByPrimaryKey(pid);
                    //自提订单不需要判断地址
                    if (vo.getShopAddressId() == 0)
                    {
                        FreightModel freightModel = freightModelMapper.selectByPrimaryKey(productListModel.getFreight());
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
                                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DZCCPSFW, "地址超出配送范围", "payment");
                            }
                        }
                    }
                    int        cid           = DataUtils.getIntegerVal(product, "cid");
                    int        num           = DataUtils.getIntegerVal(product, "num");
                    BigDecimal price         = DataUtils.getBigDecimalVal(product, "price");
                    String     product_title = DataUtils.getStringVal(product, "product_title");
                    String     unit          = DataUtils.getStringVal(product, "unit");
                    String     size          = DataUtils.getStringVal(product, "size");
                    String     coupon_id     = DataUtils.getStringVal(product, "coupon_id");
                    //如果没有优惠则为支付金额
                    BigDecimal amountAfterDiscountTmp = DataUtils.getBigDecimalVal(product, "amount_after_discount", total);

                    BigDecimal freightPrice = DataUtils.getBigDecimalVal(product, "freight_price", BigDecimal.ZERO);
                    // 循环插入订单附表 ，添加不同的订单详情
                    freightPrice = vo.getShopAddressId() != 0 ? BigDecimal.ZERO : freightPrice;

                    OrderDetailsModel orderDetailsModel = new OrderDetailsModel();
                    orderDetailsModel.setStore_id(storeId);
                    orderDetailsModel.setUser_id(userId);
                    orderDetailsModel.setP_id(pid);
                    orderDetailsModel.setP_name(product_title);
                    orderDetailsModel.setP_price(price);
                    orderDetailsModel.setNum(num);
                    orderDetailsModel.setUnit(unit);
                    orderDetailsModel.setR_sNo(sNo);
                    orderDetailsModel.setAdd_time(new Date());
                    orderDetailsModel.setR_status(orderStatus);
                    orderDetailsModel.setSize(size);
                    orderDetailsModel.setSid(cid + "");
                    orderDetailsModel.setFreight(freightPrice);
                    orderDetailsModel.setSettlement_type(0);
                    orderDetailsModel.setCoupon_id(coupon_id);
                    orderDetailsModel.setRecycle(0);
                    orderDetailsModel.setMch_id(Integer.parseInt(shopId));
                    orderDetailsModel.setAfter_discount(amountAfterDiscountTmp);
                    //如果是供应商商品则保存此时的总供货价
                    ConfiGureModel confiGureModel = confiGureModelMapper.selectByPrimaryKey(cid);
                    if (confiGureModel.getSupplier_superior() != null)
                    {
                        confiGureModel = confiGureModelMapper.selectByPrimaryKey(confiGureModel.getSupplier_superior());
                        orderDetailsModel.setSupplier_settlement(confiGureModel.getYprice().multiply(new BigDecimal(num)));
                    }

                    //是直播的商品，需要记录直播信息
                    LivingRoomModel livingRoomModel = livingRoomModelMapper.selectByPrimaryKey(vo.getRoomId());

                    orderDetailsModel.setAnchor_id(livingRoomModel.getUser_id());
                    orderDetailsModel.setLiving_room_id(vo.getRoomId());
                    BigDecimal commissionRate = new BigDecimal(confiGureModel.getCommission()).divide(new BigDecimal(100), 2, RoundingMode.HALF_UP);
                    BigDecimal commission     = amountAfterDiscountTmp.multiply(commissionRate);
                    orderDetailsModel.setCommission(commission);

                    //佣金表加入未发放状态的佣金记录
                    LivingCommissionModel livingCommissionModel = new LivingCommissionModel();
                    livingCommissionModel.setLiving_id(livingRoomModel.getId());
                    livingCommissionModel.setCommission(commission);
                    livingCommissionModel.setStatus(LivingCommissionModel.COMMISSION_STATUS_UNSETTLED);
                    livingCommissionModel.setStore_id(vo.getStoreId());
                    livingCommissionModel.setS_no(sNo);
                    livingCommissionModel.setRecycle(0);
                    livingCommissionModel.setUser_id(livingRoomModel.getUser_id());
                    livingCommissionModel.setAdd_time(new Date());

                    int i = livingCommissionModelMapper.insertSelective(livingCommissionModel);
                    if (i < 0)
                    {
                        throw new LaiKeAPIException(ERROR_CODE_WLYC, "网络异常", "payment");
                    }


                    //减少直播可卖数
                    if (!livingRoomModel.getLiving_status().equals(LivingRoomModel.STATUS_LIVING_STREAMING))
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_BSZZZBZT, "主播已下播，无法购买", "payment");
                    }
                    LivingProductModel livingProductModel = new LivingProductModel();
                    livingProductModel.setLiving_id(livingRoomModel.getId());
                    livingProductModel.setConfig_id(cid);
                    livingProductModel.setRecycle(0);
                    livingProductModel = livingProductModelMapper.selectOne(livingProductModel);

                    int xl_num = livingProductModel.getXl_num() + num;
                    livingProductModel.setXl_num(xl_num);

                    livingProductModelMapper.updateByPrimaryKeySelective(livingProductModel);

                    int beres = orderDetailsModelMapper.insertSelective(orderDetailsModel);
                    // 如果添加失败
                    if (beres < 1)
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_XDSBQSHZS, "下单失败,请稍后再试", "payment");
                    }

                    totalNum += num;
                    if (!StringUtils.isEmpty(vo.getCarts()) && vo.getBuyType() != 1)
                    {
                        Example          cartModelExample = new Example(CartModel.class);
                        Example.Criteria criteria         = cartModelExample.createCriteria();
                        criteria.andEqualTo("store_id", storeId);
                        criteria.andEqualTo("goods_id", pid);
                        criteria.andEqualTo("user_id", userId);
                        criteria.andEqualTo("size_id", cid);

                        // 删除对应购物车内容
                        int res_del = cartModelMapper.deleteByExample(cartModelExample);

                    }
                    else if (!StringUtils.isEmpty(vo.getCarts()) && vo.getBuyType() == 1)
                    {
                        Example          buyAgainModalExample = new Example(BuyAgainModal.class);
                        Example.Criteria criteria             = buyAgainModalExample.createCriteria();
                        criteria.andEqualTo("store_id", storeId);
                        criteria.andEqualTo("goods_id", pid);
                        criteria.andEqualTo("user_id", userId);
                        criteria.andEqualTo("size_id", cid);
                        // 删除对应再次购买购物车内容
                        int res_del = buyAgainModalMapper.deleteByExample(buyAgainModalExample);
                        if (res_del < 1)
                        {
                            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_XDSBQSHZS, "下单失败,请稍后再试", "payment");
                        }
                    }
//
//                    AddStockVo addStockVo = new AddStockVo();
//                    addStockVo.setStoreId(vo.getStoreId());
//                    addStockVo.setId(cid);
//                    addStockVo.setPid(pid);
//                    addStockVo.setAddNum(-num);
//                    addStockVo.setText(userId + "生成订单所需" + num);
//                    addStockVo.setMchId(Integer.valueOf(shopId));
//                    publicStockService.addGoodsStock(addStockVo, null);
                }
            }

            String mainOrderRemarks = "";
            if (remarksStatus)
            {
                mainOrderRemarks = JSON.toJSONString(mchRemarks);
            }
            //最少支付金额0.01  禅道48455 可以支付0元
            if (BigDecimal.ZERO.compareTo(total) >= 0)
            {
                total = BigDecimal.ZERO;
            }
            //52195不管使用哪一种第三方支付方式，最终的支付金额都可以按0.01进行支付处理。余额支付，倒是可以按0处理；
            if (BigDecimal.ZERO.compareTo(total) >= 0)
            {
                if (StringUtils.isNotEmpty(vo.getPayType()) &&
                        vo.getPayType().equals(DictionaryConst.OrderPayType.ORDERPAYTYPE_WALLET_PAY))
                {
                    total = BigDecimal.ZERO;
                }
                else
                {
                    total = new BigDecimal("0.01");
                }
            }
            //获取订单配置
            Map<String, Object> configMap = publicOrderService.getOrderConfig(storeId, null, otype);
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

            mchId = new StringBuilder(StringUtils.rtrim(mchId.toString(), SplitUtils.DH));
            mchId = new StringBuilder(SplitUtils.DH + mchId + SplitUtils.DH);

            OrderModel orderModel = new OrderModel();
            orderModel.setStore_id(storeId);
            orderModel.setUser_id(userId);
            orderModel.setName(name);
            orderModel.setMobile(mobile);
            orderModel.setNum(totalNum);
            orderModel.setOld_total(total);
            orderModel.setZ_price(total);
            orderModel.setsNo(sNo);
            orderModel.setSheng(sheng);
            orderModel.setShi(shi);
            orderModel.setXian(xian);
            orderModel.setAddress(addressXq);
            orderModel.setRemark(vo.getRemarks());
            orderModel.setPay(payType);
            orderModel.setAdd_time(new Date());
            orderModel.setCoupon_id(couponIds);
            orderModel.setSubtraction_id(subtractionId);
            orderModel.setConsumer_money(allow);
            orderModel.setCoupon_activity_name(reduceNameArray);
            orderModel.setSpz_price(productsTotal);
            orderModel.setStatus(orderStatus);
            orderModel.setReduce_price(reduceMoney);
            orderModel.setSource(vo.getStoreType());
            orderModel.setOtype(otype);
            orderModel.setMch_id(mchId.toString());
//            orderModel.setP_sNo("");
            orderModel.setBargain_id(0);
            orderModel.setComm_discount(new BigDecimal(discount));
            orderModel.setRemarks(mainOrderRemarks);
            orderModel.setReal_sno(realSno);
            orderModel.setSelf_lifting(shopStatus);
            orderModel.setExtraction_code(extractionCode);
            orderModel.setExtraction_code_img(extractionCodeImg);
            orderModel.setGrade_rate(gradeRate);
            orderModel.setGrade_fan(preferential);
            orderModel.setOld_freight(yunfei);
            orderModel.setZ_freight(yunfei);
            orderModel.setCoupon_price(couponMoney);
            orderModel.setPreferential_amount(preferential_amount);
            orderModel.setSingle_store(shopAddressId);
            orderModel.setReadd(OrderModel.READD_UNREAD);
            orderModel.setZhekou(BigDecimal.ZERO);
            orderModel.setRecycle(0);
            orderModel.setPick_up_store(0);
            orderModel.setOrderFailureTime(orderFailureDay);
            orderModelMapper.insertSelective(orderModel);
            int r_o = orderModel.getId();
            if (r_o >= 0)
            {
                if (giveId != 0 && vo.getGradeLevel() == null)
                {
                    int row = productListModelMapper.reduceGoodsStockNum(giveId, 1);
                    if (row < 1)
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_XDSBQSHZS, "下单失败,请稍后再试", "payment");
                    }

                    row = confiGureModelMapper.reduceGiveGoodsStockNum(giveId);
                    if (row < 1)
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_XDSBQSHZS, "下单失败,请稍后再试", "payment");
                    }
                }
                //如果为会员等级赠送商品，修改兑换券状态
                if (vo.getGradeLevel() != 0 && vo.getGradeLevel() != null)
                {
                    int res_1 = userFirstModalMapper.updateUserGiveRecord(sNo, storeId, userId, vo.getGradeLevel());
                    if (res_1 < 0)
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_XDSBGXDHQSYZTSB, "下单失败,更新兑换券使用状态失败", "payment");
                    }
                }
                if (OrderVo.SUBTRACTION.equals(discountType))
                {
                    // 满减--插件
                    publicSubtractionService.subtractionRecord(storeId, userId, sNo, giveId);
                }

                if (!StringUtils.isEmpty(couponIds))
                {
                    int updateResultFlag = publicCouponService.updateCoupons(storeId, userId, couponIds, CouponModal.COUPON_TYPE_USED);
                    if (updateResultFlag == 0)
                    {
                        //回滚删除已经创建的订单
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_XDSBGXDHQSYZTSB, "下单失败,更新兑换券使用状态失败", "payment");
                    }
                    updateResultFlag = publicCouponService.couponWithOrder(storeId, userId, couponIds, sNo, "add");
                    if (updateResultFlag == 0)
                    {
                        //回滚删除已经创建的订单
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TJYHQGLDDSJSB, "添加优惠券关联订单数据失败", "payment");
                    }
                }
                //通知后台消息

                vo.setsNo(sNo);
                vo.setUser(user);
                //订单拆单
                this.splitOrder(vo);
                //订单号
                resultMap.put("sNo", sNo);
                //订单总支付金额
                resultMap.put("total", total);
                //订单id
                resultMap.put("order_id", r_o);
                //下单时间
                resultMap.put("orderTime", DateUtil.dateFormate(orderModel.getAdd_time(), GloabConst.TimePattern.YMDHMS));
                return resultMap;
            }
            else
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_XDSBQSHZS, "下单失败,请稍后再试", "payment");
            }
        }
        catch (LaiKeAPIException l)
        {
            logger.error("下单 自定义异常", l);
            throw l;
        }
        catch (Exception e)
        {
            logger.error("下单 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_XDSBQSHZS, "下单失败,请稍后再试", "payment");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> updateOrderRemark(OrderVo vo)
    {
        try
        {
            int row = orderModelMapper.updateOrderRemark(vo.getRemarks(), vo.getsNo(), vo.getStoreId());
            logger.info("更新结果：" + row);
        }
        catch (Exception e)
        {
            logger.error(e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CZSB, "操作失败", "updateOrderRemark");
        }
        return null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> splitOrder(OrderVo vo)
    {
        Map<String, Object> resultMap = new HashMap<>();
        try
        {
            String sNo;
            // 订单信息
            if (StringUtils.isNotEmpty(vo.getOrderList()))
            {
                String orderList = vo.getOrderList();
                //orderList 字符串转数组
                Map<String, String> orderInfoMap = JSON.parseObject(orderList, Map.class);
                sNo = orderInfoMap.get("sNo");
            }
            else if (StringUtils.isNotEmpty(vo.getsNo()))
            {
                sNo = vo.getsNo();
            }
            else
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误");
            }

            User user;
            if (vo.getUser() != null)
            {
                user = vo.getUser();
            }
            else
            {
                user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            }
            int storeId = vo.getStoreId();
            // 用户id
            String     userId     = user.getUser_id();
            OrderModel orderModel = new OrderModel();
            orderModel.setStore_id(storeId);
            orderModel.setsNo(sNo);
            orderModel.setUser_id(userId);
            orderModel = orderModelMapper.selectOne(orderModel);
            // 判断订单是否存在、有效
            if (orderModel == null)
            {
                // 数据异常，返回错误提示
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DDBCZ, sNo + "订单不存在", "payment");
            }
            //只有普通订单才需要拆单
            if (!orderModel.getOtype().equals(DictionaryConst.OrdersType.ORDERS_HEADER_GM))
            {
                //操作成功
                resultMap.put("flag", true);
                //跳过拆分
                return resultMap;
            }

            //获取订单数据
            BigDecimal oldTotal = orderModel.getZ_price();
            // 优惠券ID
            String couponId = orderModel.getCoupon_id();
            // 满减ID
            Integer subtractionId = orderModel.getSubtraction_id();
            // 查询出的满减金额
            BigDecimal reducePrice = orderModel.getReduce_price();
            // 会员折扣 )
            BigDecimal gradeRate = orderModel.getGrade_rate();
            // 商品总价
            BigDecimal zSpzPrice = orderModel.getSpz_price();
            // 订单状态
            Integer status = orderModel.getStatus();
            //每个规格优惠价格处理
            Map<Integer, BigDecimal> attrAvgMap = new HashMap<>(16);
            //拆备注
            Map<String, String> remarks      = new HashMap<>(16);
            String              orderRemarks = orderModel.getRemarks();
            if (!StringUtils.isEmpty(orderRemarks))
            {
                remarks = JSON.parseObject(orderModel.getRemarks(), new TypeReference<Map<String, String>>()
                {
                });
            }

//            if (status == ORDERS_R_STATUS_UNPAID) {
//                // 如果未支付完成则不拆分订单
//                //操作成功
//                resultMap.put("flag", true);
//                resultMap.put("message", "未支付完成则不拆分订单");
//                return resultMap;
//            }

            // 店铺ID字符串
            String mchId = orderModel.getMch_id();
            orderModel.setId(null);

            String type = sNo.substring(0, 2);//获取订单号前两位字母（类型）
            // mch_id 和 shop_id 相同
            mchId = StringUtils.trim(mchId, SplitUtils.DH);
            // 店铺id字符串
            List<String> shopIds = Splitter.on(SplitUtils.DH).splitToList(mchId);

            //当为多家店铺时拆单
            if (!CollectionUtils.isEmpty(shopIds) && shopIds.size() > 1)
            {
                List<Map<String, Object>> couponList = null;
                //是否使用了优惠券标识
                boolean isCoupon = false;
                if (StringUtils.isNotEmpty(couponId))
                {
                    String[] couponIdList = couponId.split(SplitUtils.DH);
                    for (String couponIdTemp : couponIdList)
                    {
                        if (!"0".equals(couponIdTemp))
                        {
                            isCoupon = true;
                            break;
                        }
                    }
                }
                if (isCoupon)
                {
                    //优惠券拆单
                    logger.debug("{}进入优惠券拆单 优惠券字符串{}", sNo, couponId);
                    couponList = publicCouponService.splitOrder(storeId, userId, sNo);
                }
                //需处理拆单的供应商订单
                Map<Integer, List<Map<String, Object>>> supplierOrder = new HashMap<>(16);

                List<OrderModel> splitOrders = new ArrayList<>();
                int              size        = shopIds.size();
                for (String shopId : shopIds)
                {
                    //订单总价
                    BigDecimal orderPriceTotal = BigDecimal.ZERO;
                    OrderModel orderModelTmp   = new OrderModel();
                    BeanUtils.copyProperties(orderModel, orderModelTmp);
                    // 生成订单号
                    String sNoTmp = publicOrderService.createOrderNo(type);
                    orderModelTmp.setMch_id(SplitUtils.DH + shopId + SplitUtils.DH);
                    orderModelTmp.setsNo(sNoTmp);
                    orderModelTmp.setP_sNo(sNo);
                    //查询单个商品的价格，运费，数量
                    List<Map<String, Object>> orderDetailsList = orderModelMapper.getOrderDetails(storeId, sNo, Integer.parseInt(shopId));
                    //查询到数据
                    BigDecimal orderNum = BigDecimal.ZERO;
                    if (!CollectionUtils.isEmpty(orderDetailsList))
                    {
                        int orderDetailsListSize = orderDetailsList.size();
                        //商品总价
                        BigDecimal spzPrice     = BigDecimal.ZERO;
                        BigDecimal totalFreight = BigDecimal.ZERO;
                        //供应商订单集合
                        List<Map<String, Object>> supplierOrderList = new ArrayList<>();
                        for (Map<String, Object> orderDetailsMap : orderDetailsList)
                        {
                            Integer order_details_id = DataUtils.getIntegerVal(orderDetailsMap, "id");
                            //供应商商品处理
                            Integer          p_id             = DataUtils.getIntegerVal(orderDetailsMap, "p_id");
                            ProductListModel productListModel = productListModelMapper.selectByPrimaryKey(p_id);
                            if (StringUtils.isNotEmpty(productListModel.getGongyingshang()))
                            {
                                supplierOrderList.add(orderDetailsMap);
                                supplierOrder.put(Integer.valueOf(shopId), supplierOrderList);
                                orderDetailsListSize--;
                                continue;
                            }
                            //规格实际支付价格
                            BigDecimal payPrice     = new BigDecimal(MapUtils.getString(orderDetailsMap, "after_discount"));
                            BigDecimal num          = DataUtils.getBigDecimalVal(orderDetailsMap, "num");
                            BigDecimal productPrice = DataUtils.getBigDecimalVal(orderDetailsMap, "p_price", BigDecimal.ZERO);
                            BigDecimal freight      = DataUtils.getBigDecimalVal(orderDetailsMap, "freight");
                            orderNum = orderNum.add(num);
                            //商品总价
                            BigDecimal price = productPrice.multiply(num);
                            spzPrice = spzPrice.add(price);
                            //总运费
                            totalFreight = totalFreight.add(freight);
                            //订单总价
                            orderPriceTotal = orderPriceTotal.add(payPrice);
                            //规格价格记录
                            attrAvgMap.put(order_details_id, price);
                            int row = orderDetailsModelMapper.updateOrderDetailsParentOrderNo(storeId, sNoTmp, order_details_id);
                            if (row < 1)
                            {
                                logger.error("修改订单号失败！");
                                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CZSB, "操作失败", "splitOrder");
                            }
                        }
                        //全部为供应商商品
                        if (orderDetailsListSize <= 0)
                        {
                            size--;
                        }
                        orderModelTmp.setSpz_price(spzPrice);
//                        orderModelTmp.setPay_time(new Date());
                        orderModelTmp.setSubtraction_id(0);
                        orderModelTmp.setReduce_price(new BigDecimal(0));
                        orderModelTmp.setCoupon_id("0");
                        orderModelTmp.setCoupon_price(new BigDecimal(0));
                        orderModelTmp.setGrade_rate(gradeRate);
                        orderModelTmp.setPreferential_amount(new BigDecimal(0));
                        //临时变量
                        BigDecimal spzPriceTmp = spzPrice;
                        if (isCoupon)
                        {
                            // 参与优惠券 查看php 中 couponlist的数据结构
                            if (!CollectionUtils.isEmpty(couponList) && couponList.size() > 0)
                            {
                                for (Map<String, Object> couponInfoMap : couponList)
                                {
                                    String mchIdTmp = DataUtils.getStringVal(couponInfoMap, "mchId");
                                    if (shopId.equals(mchIdTmp))
                                    {
                                        //获取店铺优惠券 如果没有则获取平台优惠券 2.如果有使用店铺优惠券则看是否使用了平台优惠券，使用了则用‘,’隔开
                                        String couponIdTmp = MapUtils.getString(couponInfoMap, "couponId");
                                        if ("0".equals(couponIdTmp))
                                        {
                                            couponIdTmp = MapUtils.getString(couponInfoMap, "platformCouponId");
                                        }
                                        else
                                        {
                                            //获取平台优惠券拼接店铺优惠券 [店铺优惠券,平台优惠券]
                                            String ptCouponId = DataUtils.getStringVal(couponInfoMap, "platformCouponId");
                                            if (!"0".equals(ptCouponId))
                                            {
                                                couponIdTmp += SplitUtils.DH + ptCouponId;
                                            }
                                        }
                                        orderModelTmp.setCoupon_id(couponIdTmp);
                                        spzPriceTmp = spzPrice.subtract(orderModelTmp.getCoupon_price());
                                        //平台优惠金额
                                        BigDecimal preferentialAmount = DataUtils.getBigDecimalVal(couponInfoMap, "preferential_amount");
                                        //店铺优惠金额
                                        BigDecimal mahCouponAmount = DataUtils.getBigDecimalVal(couponInfoMap, "mch_coupon_amount");
                                        orderModelTmp.setPreferential_amount(preferentialAmount);
                                        orderModelTmp.setCoupon_price(preferentialAmount.add(mahCouponAmount));
                                        int row = publicCouponService.couponWithOrder(storeId, userId, couponIdTmp, sNoTmp, "add");
                                        if (row <= 0)
                                        {
                                            logger.error("添加优惠券关联订单数据失败！");
                                            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TJYHQGLDDSJSB, "添加优惠券关联订单数据失败", "splitOrder");
                                        }

                                    }
                                }
                            }
                        }

                        //修改当前店铺每个商品优惠金额
                        /*Map<Integer, BigDecimal> resultAvgMap = DataAlgorithmTool.orderPriceAverage()
                        for (Map<String, Object> orderDetailsMap : orderDetailsList) {
                            Integer orderDetailsId = DataUtils.getIntegerVal(orderDetailsMap, "id");
                            BigDecimal num = DataUtils.getBigDecimalVal(orderDetailsMap, "num");
                            BigDecimal productPrice = DataUtils.getBigDecimalVal(orderDetailsMap, "p_price", BigDecimal.ZERO);
                            //计算平台优惠后的商品价格 如果有多个商品 则每个商品平摊优惠金额
                            BigDecimal youHuiPrice = DataAlgorithmTool.orderPriceAverage(spzPrice.multiply(gradeRate).multiply(num), productPrice.multiply(gradeRate).multiply(num), orderModelTmp.getCoupon_price());
                            OrderDetailsModel orderDetailsUpdate = new OrderDetailsModel();
                            orderDetailsUpdate.setId(orderDetailsId);
                            orderDetailsUpdate.setAfter_discount(productPrice.multiply(num).subtract(youHuiPrice));
                            int row = orderDetailsModelMapper.updateByPrimaryKeySelective(orderDetailsUpdate);
                            if (row < 1) {
                                logger.error("拆单修改订单明细失败！");
                                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CZSB, "操作失败", "splitOrder");
                            }
                            orderPriceTotal = orderPriceTotal.add(orderDetailsUpdate.getAfter_discount());
                        }*/
                        if (subtractionId != null && subtractionId != 0)
                        {
                            // 参与满减
                            orderModelTmp.setSubtraction_id(subtractionId);
                            // 该店铺商品总价 除以 整个订单商品总价 乘以 优惠的满减金额
                            BigDecimal val = spzPrice.divide(zSpzPrice, 2, BigDecimal.ROUND_HALF_UP).multiply(reducePrice);
                            orderModelTmp.setReduce_price(val);
                            val = orderModelTmp.getPreferential_amount().add(orderModelTmp.getReduce_price());
                            orderModelTmp.setPreferential_amount(val);
                            orderPriceTotal = spzPrice.subtract(orderModelTmp.getReduce_price());
                        }
                        //原订单支付金额为0.01，拆单后两个订单的金额也为0.001
                        if (oldTotal.compareTo(new BigDecimal("0.01")) == 0)
                        {
                            if (orderPriceTotal.compareTo(BigDecimal.ZERO) == 0)
                            {
                                orderPriceTotal = new BigDecimal("0.01");
                            }
                        }
                        BigDecimal val = orderPriceTotal.add(totalFreight);
                        orderModelTmp.setZ_price(val);
                        orderModelTmp.setZ_freight(totalFreight);
                        orderModelTmp.setOld_freight(totalFreight);
                        orderModelTmp.setStatus(status);
                        orderModelTmp.setNum(orderNum.intValue());
                        orderModelTmp.setRemarks("");
                        orderModelTmp.setOld_total(val);
                        orderModelTmp.setReal_sno(orderModel.getReal_sno());
                        //备注处理
                        if (!remarks.isEmpty())
                        {
                            String              remarksStr = remarks.get(shopId);
                            Map<String, String> remarksMap = new HashMap<>(1);
                            if (StringUtils.isNotEmpty(remarksStr))
                            {
                                remarksMap.put(shopId, remarksStr);
                                orderModelTmp.setRemarks(JSON.toJSONString(remarksMap));
                            }
                        }
                        if (orderModelTmp.getNum() > 0)
                        {
                            splitOrders.add(orderModelTmp);
                        }
//                        if (splitOrders.size() > 0 && splitOrders.size() == size) {
                        if (splitOrders.size() > 0 && splitOrders.size() == size)
                        {
                            orderModelMapper.insertList(splitOrders);
                            logger.info("保存拆单信息！");
                            //删掉未拆单之前的订单
                            MessageLoggingModal messageLoggingDel = new MessageLoggingModal();
                            messageLoggingDel.setStore_id(storeId);
                            messageLoggingDel.setParameter(orderModel.getId() + "");
                            messageLoggingModalMapper.delete(messageLoggingDel);
                            splitOrders.forEach(order ->
                            {
                                MessageLoggingModal messageLoggingSave = new MessageLoggingModal();
                                messageLoggingSave.setStore_id(order.getStore_id());
                                messageLoggingSave.setMch_id(Integer.parseInt(StringUtils.trim(order.getMch_id(), SplitUtils.DH)));
                                messageLoggingSave.setType(MessageLoggingModal.Type.TYPE_ORDER_NEW);
                                messageLoggingSave.setTo_url(OrderDataUtils.getOrderRoute(order.getOtype(), 0));
                                messageLoggingSave.setParameter(order.getId() + "");
                                messageLoggingSave.setContent(String.format("您来新订单了，订单为%s，请及时处理", order.getsNo()));
                                messageLoggingSave.setAdd_date(new Date());
                                messageLoggingModalMapper.insertSelective(messageLoggingSave);
                            });
                        }

                        int row = orderDetailsModelMapper.updateOrderDetailsStatus(storeId, sNoTmp, status);
                        if (row < 0)
                        {
                            logger.error("订单拆分失败！");
                            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CZSB, "操作失败", "splitOrder");
                        }
                    }
                    else
                    {
                        logger.error("没有查询到订单信息！");
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CZSB, "操作失败", "splitOrder");
                    }
                }
                //拆单后关闭并逻辑删除原主订单
                Map conditionMap = Maps.newHashMap();
                conditionMap.put("status", ORDERS_R_STATUS_CLOSE);
                conditionMap.put("recycle", DictionaryConst.ProductRecycle.RECOVERY);
                conditionMap.put("orderno", sNo);
                int row = orderModelMapper.updateByOrdernoDynamic(conditionMap);
                if (row < 1)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CZSB, "操作失败", "splitOrder");
                }
                //拆单后删除原优惠券订单关联数据
                couponOrderModalMapper.delCouponOrder(storeId, sNo);
                //供应商拆单(多店铺拆单后)
                if (supplierOrder.size() > 0)
                {
                    this.splitSupplierOrder(vo.getStoreId(), supplierOrder, sNo, status);
                }
            }
            else
            {
                OrderDetailsModel orderDetailsModel = new OrderDetailsModel();
                orderDetailsModel.setStore_id(storeId);
                orderDetailsModel.setUser_id(userId);
                orderDetailsModel.setR_sNo(sNo);
                List<OrderDetailsModel> orderDetailsList = orderDetailsModelMapper.select(orderDetailsModel);
                //是否有供应商拆单
                boolean isSplitSupplier = false;
                for (OrderDetailsModel detailsModel : orderDetailsList)
                {
                    //供应商商品处理
                    Integer          p_id             = detailsModel.getP_id();
                    ProductListModel productListModel = productListModelMapper.selectByPrimaryKey(p_id);
                    if (StringUtils.isNotEmpty(productListModel.getGongyingshang()))
                    {
                        //供应商拆单(同店铺未拆单)
                        isSplitSupplier = this.splitSupplierOrder(vo.getStoreId(), sNo);
                    }
                }
                //将普通商品拆出来
                if (isSplitSupplier)
                {
                    String     sNoTmp   = publicOrderService.createOrderNo(DictionaryConst.OrdersType.ORDERS_HEADER_GM);
                    OrderModel orderOld = new OrderModel();
                    orderOld.setStore_id(storeId);
                    orderOld.setsNo(sNo);
                    orderOld = orderModelMapper.selectOne(orderOld);
                    OrderModel orderModelTmp = new OrderModel();
                    BeanUtils.copyProperties(orderOld, orderModelTmp);
                    orderModelTmp.setId(null);
                    orderModelTmp.setsNo(sNoTmp);
                    orderModelTmp.setP_sNo(orderOld.getsNo());
                    orderModelTmp.setZ_price(orderOld.getZ_price());
                    orderModelTmp.setZ_freight(orderOld.getZ_freight());
                    orderModelTmp.setOld_freight(orderOld.getZ_freight());
                    orderModelTmp.setSpz_price(orderOld.getSpz_price());
                    orderModelTmp.setNum(orderOld.getNum());
                    orderModelTmp.setReal_sno(orderOld.getReal_sno());
                    orderModelTmp.setOld_total(orderOld.getZ_price());
                    orderModelTmp.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS);
                    orderModelMapper.insertSelective(orderModelTmp);
                    //关联新订单
                    int row = orderDetailsModelMapper.updateOrderDetailsBysNo(storeId, orderModelTmp.getsNo(), sNo);
                    if (row < 1)
                    {
                        logger.error("修改订单号失败！");
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CZSB, "操作失败");
                    }
                    //拆单后关闭并逻辑删除原主订单
                    orderOld.setStatus(ORDERS_R_STATUS_CLOSE);
                    orderOld.setRecycle(DictionaryConst.ProductRecycle.RECOVERY);
                    orderOld.setZ_freight(orderOld.getOld_freight());
                    orderOld.setZ_price(orderOld.getOld_total());
                    orderOld.setSpz_price(orderOld.getOld_total());
                    row = orderModelMapper.updateByPrimaryKeySelective(orderOld);
                    if (row < 1)
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CZSB, "操作失败", "splitOrder");
                    }
                }
            }
            //TODO 订单成功后的 推送 和 消息记录
            //$this->orderMessage($sNo, $store_id, $user_id, $db, $shop_id,$Identification);
        }
        catch (Exception e)
        {
            logger.error("拆单出现 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_, e.getMessage(), "splitOrder");
        }
        //操作成功
        resultMap.put("flag", true);
        return resultMap;
    }

    @Override
    @Transactional
    public Map<String, Object> orderList(OrderVo vo)
    {
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            return publicOrderService.orderList(vo, user);
        }
        catch (LaiKeAPIException e)
        {
            logger.error("订单列表获取失败 异常", e);
            throw e;
        }
        catch (Exception e)
        {
            logger.error("订单列表获取失败 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DDLBHQSB, "订单列表获取失败", "orderList");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> remindDelivery(OrderVo vo)
    {
        Map<String, Object> retMap = Maps.newHashMap();
        try
        {
            int storeId = vo.getStoreId();
            // 订单id
            int  orderId = vo.getOrderId();
            User user    = null;
            if (vo.getUser() != null)
            {
                user = vo.getUser();
            }
            else
            {
                user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil);
            }
            String     userId = user.getUser_id();
            OrderModel order  = new OrderModel();
            order.setStore_id(storeId);
            order.setUser_id(userId);
            order.setId(orderId);
            order = orderModelMapper.selectOne(order);
            if (order != null)
            {
                Integer             mchId     = Integer.parseInt(StringUtils.trim(order.getMch_id(), SplitUtils.DH).split(SplitUtils.DH)[0]);
                Map<String, Object> configMap = publicOrderService.getOrderConfig(storeId, mchId, order.getOtype().toUpperCase());
                if (configMap != null && !configMap.isEmpty())
                {
                    //间隔时间 - 秒
                    int  remind = MapUtils.getInteger(configMap, "remind");
                    Date remindNextDate;
                    if (remind > 0)
                    {
                        //计算下次间隔时间
                        remindNextDate = DateUtil.getAddDateBySecond(new Date(), remind);
                    }
                    else
                    {
                        //否则只能提醒一次 下次提醒时间+10年
                        remindNextDate = DateUtil.getAddMonth(new Date(), 12 * 10);
                        logger.debug("订单{} 【提醒发货】无次数限制,默认只提醒一次", order.getsNo());
                    }
                    orderModelMapper.updateDeliveryRemind(storeId, userId, orderId, remindNextDate);
                }
                OrderDetailsModel orderDetailsModel = new OrderDetailsModel();
                orderDetailsModel.setStore_id(order.getStore_id());
                orderDetailsModel.setR_sNo(order.getsNo());
                orderDetailsModel = orderDetailsModelMapper.select(orderDetailsModel).get(0);
                ProductListModel productListModel = productListModelMapper.selectByPrimaryKey(orderDetailsModel.getP_id());
                //通知后台消息
                MessageLoggingModal messageLoggingSave = new MessageLoggingModal();
                messageLoggingSave.setStore_id(storeId);
                if (productListModel != null && StringUtils.isNotEmpty(productListModel.getGongyingshang()) && order.getIs_lssued().equals(DictionaryConst.WhetherMaven.WHETHER_OK))
                {
                    messageLoggingSave.setSupplier_id(Integer.valueOf(productListModel.getGongyingshang()));
                }
                else
                {
                    messageLoggingSave.setMch_id(mchId);
                }
                messageLoggingSave.setType(MessageLoggingModal.Type.TYPE_ORDER_REMIND_SEND);
                messageLoggingSave.setTo_url(OrderDataUtils.getOrderRoute(order.getOtype(), 0));
                messageLoggingSave.setParameter(order.getId() + "");
                messageLoggingSave.setContent(String.format("订单%s的用户已经迫不及待想要收到宝贝了，请前往订单列表发货！", order.getsNo()));
                messageLoggingSave.setAdd_date(new Date());
                messageLoggingModalMapper.insertSelective(messageLoggingSave);

                retMap.put("message", "操作成功");
                retMap.put("code", "200");
            }
            else
            {
                retMap.put("message", "已经提醒过了,请稍后再试！");
                retMap.put("code", ORDER_REMIND_ALREADY);
            }
        }
        catch (Exception e)
        {
            logger.error("提醒发货失败 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TXFHSB, "提醒发货失败", "remindDelivery");
        }
        return retMap;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> cancleOrder(OrderVo vo)
    {
        Map<String, Object> retMap = Maps.newHashMap();
        try
        {
            int storeId = vo.getStoreId();
            // 订单id
            int orderId = vo.getOrderId();
            // 根据微信id,查询用户id
            User user = null;
            if (vo.getUser() != null)
            {
                user = vo.getUser();
            }
            else
            {
                user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil);
            }
            String     userId = user.getUser_id();
            BigDecimal money  = user.getMoney();
            // 根据订单id,查询订单列表(订单号)
            OrderModel order = new OrderModel();
            order.setStore_id(storeId);
            order.setUser_id(userId);
            order.setId(orderId);
            order = orderModelMapper.selectOne(order);
            if (order != null)
            {
                // 订单号
                String sNo = order.getsNo();
                // 订单状态
                int status = order.getStatus();
                // 订单总价
                BigDecimal zPrice = order.getZ_price();
                // 优惠券id
                String couponId = order.getCoupon_id();
                //抵扣金额
                BigDecimal offsetBalance = order.getOffset_balance();
                String     otype         = order.getOtype();
                if (DictionaryConst.OrdersType.ORDERS_HEADER_VIP_GIVE.equals(otype))
                {
                    userFirstModalMapper.cancleUserFirstRecord(sNo, storeId);
                }
                StringBuilder event = new StringBuilder();
                event.append(userId).append("取消订单号为").append(sNo).append("的订单");

                RecordModel recordModel = new RecordModel();
                recordModel.setStore_id(storeId);
                recordModel.setUser_id(userId);
                recordModel.setMoney(zPrice);
                recordModel.setOldmoney(money);
                recordModel.setAdd_date(new Date());
                recordModel.setEvent(event.toString());
                recordModel.setType(RecordModel.RecordType.CANCEL_ORDER);
                switch (status)
                {
                    case ORDER_PAYED:
                        userMapper.updateUserMoney(zPrice, storeId, userId);
                        recordModelMapper.insert(recordModel);
                        break;
                    case ORDER_UNRECEIVE:
                        //已经发货不支持取消订单
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YJFH, "已经发货", "cancleOrder");
                    default:
                        if (offsetBalance.doubleValue() > 0)
                        {
                            //修改用户余额
                            userMapper.updateUserMoney(offsetBalance, storeId, userId);
                            //添加日志
                            event.append(userId).append("退款").append(offsetBalance).append("元余额");
                            recordModel.setEvent(event.toString());
                            recordModelMapper.insert(recordModel);
                        }
                        break;
                }
                //禅道52271 取消订单订单状态修改为已结算
                Map params = Maps.newHashMap();
                params.put("status", ORDER_CLOSE);
                params.put("cancleOrder", GloabConst.ManaValue.MANA_VALUE_YES);
                params.put("settlement_status", OrderDetailsModel.SETTLEMENT_TYPE_SETTLED);
                params.put("settlement_time", new Date());
                params.put("storeId", storeId);
                params.put("userId", userId);
                params.put("id", orderId);
                int row = orderModelMapper.updateOrderInfo(params);
                if (row < 1)
                {
                    logger.info(sNo + "修改订单状态失败:");
                }

                int row1 = orderDetailsModelMapper.updateOrderDetailsStatusAndSettlementStatus(storeId, sNo, ORDER_CLOSE, OrderDetailsModel.SETTLEMENT_TYPE_SETTLED);
                logger.info(sNo + "订单关闭:");
                if (row1 < 1)
                {
                    logger.info("sNo 修改订单状态失败！");
                }
                if (row1 >= 0 && row >= 0)
                {
                    if (org.apache.commons.lang3.StringUtils.isNotEmpty(couponId)
                            && !"0".equals(couponId))
                    {
                        row = publicCouponService.updateCoupons(storeId, userId, couponId, 0);
                        if (row == 0)
                        {
                            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YWYC, "业务异常", "cancleOrder");
                        }
                        logger.info("会员" + userId + "取消订单号为" + sNo + "时,修改优惠券ID为" + couponId + "的状态！");
                        row = publicCouponService.couponWithOrder(storeId, userId, couponId, sNo, "update");
                        if (row == 0)
                        {
                            //回滚删除已经创建的订单
                            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TJYHQGLDDSJSB, "添加优惠券关联订单数据失败", "cancleOrder");
                        }
                    }
                    OrderDetailsModel orderDetailsModel = new OrderDetailsModel();
                    orderDetailsModel.setStore_id(storeId);
                    orderDetailsModel.setR_sNo(sNo);
                    List<OrderDetailsModel> orderDetailsModels = orderDetailsModelMapper.select(orderDetailsModel);
                    for (OrderDetailsModel orderDetailsInfo : orderDetailsModels)
                    {
                        int    pid         = orderDetailsInfo.getP_id();
                        int    goodsNum    = orderDetailsInfo.getNum();
                        String attributeId = orderDetailsInfo.getSid();
                        //直播的订单需要先判断直播的状态，如果该订单正在直播，需要退还到直播商品的库存
                        LivingRoomModel livingRoomModel = livingRoomModelMapper.selectByPrimaryKey(orderDetailsInfo.getLiving_room_id());
                        if (livingRoomModel != null && livingRoomModel.getLiving_status().equals(LivingRoomModel.STATUS_LIVING_STREAMING))
                        {
                            LivingProductModel livingProductModel = new LivingProductModel();
                            livingProductModel.setLiving_id(livingRoomModel.getId());
                            livingProductModel.setConfig_id(Integer.valueOf(orderDetailsInfo.getSid()));
                            livingProductModel.setRecycle(0);
                            livingProductModel = livingProductModelMapper.selectOne(livingProductModel);
                            livingProductModel.setXl_num(livingProductModel.getXl_num() - goodsNum);
                            livingProductModelMapper.updateByPrimaryKeySelective(livingProductModel);
                        }
                        else
                        {
                            //供应商库存处理
                            ProductListModel productListModel = productListModelMapper.selectByPrimaryKey(pid);
                            if (productListModel.getSupplier_superior() != null)
                            {
                                ProductListModel supplierPro = productListModelMapper.selectByPrimaryKey(productListModel.getSupplier_superior());
                                pid = supplierPro.getId();
                                ConfiGureModel confiGureModel = confiGureModelMapper.selectByPrimaryKey(attributeId);
                                confiGureModel = confiGureModelMapper.selectByPrimaryKey(confiGureModel.getSupplier_superior());
                                attributeId = confiGureModel.getId().toString();
                            }
                            ConfiGureModel confiGureModel = new ConfiGureModel();
                            confiGureModel.setId(Integer.valueOf(attributeId));
                            confiGureModel.setPid(pid);

                            confiGureModel = confiGureModelMapper.selectOne(confiGureModel);
                            int totalNum = confiGureModel.getNum();

                            row = productListModelMapper.addGoodsStockNum(pid, goodsNum);
                            if (row < 1)
                            {
                                logger.info("修改商品库存失败！");
                            }
                            confiGureModel.setNum(goodsNum);
                            confiGureModel.setTotal_num(0);
                            row = confiGureModelMapper.addGoodsAttrStockNum(confiGureModel);
                            if (row < 1)
                            {
                                logger.info("修改商品属性库存失败！");
                            }

                            String     content    = userId + "取消订单，返还" + goodsNum;
                            StockModel stockModel = new StockModel();
                            stockModel.setStore_id(storeId);
                            stockModel.setProduct_id(pid);
                            stockModel.setAttribute_id(Integer.valueOf(attributeId));
                            stockModel.setTotal_num(totalNum);
                            stockModel.setFlowing_num(goodsNum);
                            stockModel.setType(0);
                            stockModel.setUser_id(userId);
                            stockModel.setAdd_date(new Date());
                            stockModel.setContent(content);
                            stockModelMapper.insert(stockModel);
                        }
                    }

                    if (DictionaryConst.OrdersType.ORDERS_HEADER_ZB.equals(otype))
                    {
                        LivingCommissionModel livingCommissionModel = new LivingCommissionModel();
                        livingCommissionModel.setS_no(sNo);
                        livingCommissionModel.setRecycle(0);
                        livingCommissionModel = livingCommissionModelMapper.selectOne(livingCommissionModel);
                        if (livingCommissionModel != null)
                        {
                            livingCommissionModel.setRecycle(1);
                            livingCommissionModelMapper.updateByPrimaryKeySelective(livingCommissionModel);
                        }


                    }
                    //通知后台消息
                    MessageLoggingModal messageLoggingSave = new MessageLoggingModal();
                    messageLoggingSave.setStore_id(storeId);
                    String[] mchId = StringUtils.trim(order.getMch_id(), SplitUtils.DH).split(SplitUtils.DH);
                    messageLoggingSave.setMch_id(Integer.parseInt(mchId[0]));
                    messageLoggingSave.setType(MessageLoggingModal.Type.TYPE_ORDER_CLOSE);
                    messageLoggingSave.setTo_url(OrderDataUtils.getOrderRoute(order.getOtype(), 0));
                    messageLoggingSave.setParameter(order.getId() + "");
                    messageLoggingSave.setContent(String.format("用户还需再考虑考虑，关闭了%s订单！", order.getsNo()));
                    messageLoggingSave.setAdd_date(new Date());
                    messageLoggingModalMapper.insertSelective(messageLoggingSave);
                    retMap.put("op_result", "操作成功");
                }
                else
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YWYC, "业务异常", "cancleOrder");
                }
            }
            return retMap;
        }
        catch (LaiKeAPIException l)
        {
            logger.error("取消订单 异常", l);
            throw l;
        }
        catch (Exception e)
        {
            logger.error("取消订单 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_, e.getMessage(), "cancleOrder");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> loadMore(OrderVo vo)
    {
        try
        {
            int    storeId  = vo.getStoreId();
            String accessId = vo.getAccessId();
            String keyword  = vo.getOrdervalue();
            // 页数
            int page = vo.getPageNo();
            //订单类型
            String           orderType        = vo.getOrderType();
            OrderConfigModal orderConfigModal = new OrderConfigModal();
            orderConfigModal.setStore_id(storeId);
            orderConfigModal = orderConfigModalMapper.selectOne(orderConfigModal);
            //订单失效天数
            int orderFailure = 2;
            // 订单过期删除时间
            int    orderOverdue = 0;
            String company      = GloabConst.TimeUnit.UNIT_DAY;
            String unit         = GloabConst.TimeUnit.UNIT_DAY;
            if (orderConfigModal != null)
            {
                orderFailure = orderConfigModal.getOrder_failure();
                orderOverdue = orderConfigModal.getOrder_overdue();
                if ("天".equals(orderConfigModal.getCompany()))
                {
                    company = GloabConst.TimeUnit.UNIT_DAY;
                }
                else
                {
                    company = "hour";
                }
                if ("天".equals(orderConfigModal.getUnit()))
                {
                    unit = GloabConst.TimeUnit.UNIT_DAY;
                }
                else
                {
                    unit = "hour";
                }
            }

            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil);
            // 根据微信id,查询用户id
            Map<String, Object> data = publicOrderService.orderList(vo, user);
            return data;
        }
        catch (Exception e)
        {
            logger.error("订单列表获取 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DDLBHQSB, "订单列表获取失败", "orderList");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Map<String, Object> delOrder(OrderVo vo)
    {
        Map<String, Object> retMap = Maps.newHashMap();
        try
        {
            int storeId = vo.getStoreId();
            // 订单id
            int orderId = vo.getOrderId();
            // 根据微信id,查询用户id
            User user = null;
            if (vo.getUser() != null)
            {
                user = vo.getUser();
            }
            else
            {
                user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil);
            }
            String userId = user.getUser_id();
            // 根据订单id,查询订单列表(订单号)
            OrderModel order = new OrderModel();
            order.setStore_id(storeId);
            order.setUser_id(userId);
            order.setId(orderId);
            order = orderModelMapper.selectOne(order);
            if (order != null)
            {
                // 订单号
                String sNo = order.getsNo();
                //用户删除订单
                int row1 = orderDetailsModelMapper.userDelOrderDetails(storeId, sNo);
                int row2 = orderModelMapper.userDelOrder(storeId, sNo);
                if (row1 >= 0 && row2 >= 0)
                {
                    //判断用户、商家、平台是否都已经删除订单
                    publicOrderService.allDelOrder(storeId, sNo);
                    Map<String, Object> parmaMap = new HashMap<>(16);
                    parmaMap.put("store_id", vo.getStoreId());
                    parmaMap.put("orderno", sNo);
                    List<Map<String, Object>> orderDetailsModelList = orderDetailsModelMapper.getOrderDetailByGoodsInfo(parmaMap);
                    for (Map<String, Object> detail : orderDetailsModelList)
                    {
                        int     id      = MapUtils.getIntValue(detail, "id");
                        Integer integer = returnOrderModelMapper.orderDetailReturnIsNotEnd(vo.getStoreId(), sNo, id);
                        if (integer != null && integer > 0)
                        {
                            throw new LaiKeAPIException(ErrorCode.BizErrorCode.OPERATION_FAILED, "当前订单正在售后处理,不可关闭");
                        }
                        //禅道49609 -》用户、商家、平台删除自己的订单  删除订单不回滚库存  产品：刘蔷
/*                        if (status.equals(DictionaryConst.OrdersStatus.ORDERS_R_STATUS_COMPLETE) || status.equals(DictionaryConst.OrdersStatus.ORDERS_R_STATUS_CLOSE)) {
                            logger.debug("订单明细id{} 状态{} 删除订单不回滚库存", id, status);
                        }
                        //回滚商品库存
                        AddStockVo addStockVo = new AddStockVo();
                        addStockVo.setStoreId(vo.getStoreId());
                        addStockVo.setId(attrId);
                        addStockVo.setPid(goodsId);
                        addStockVo.setAddNum(num);
                        addStockVo.setText("后台删除订单,返还" + num);
                        publicStockService.addGoodsStock(addStockVo, "admin");
                        //扣减商品销量
                        productListModelMapper.updateProductListVolume(-num, vo.getStoreId(), goodsId);*/
                    }
                    retMap.put("code", 200);
                    retMap.put("message", "操作成功");
                }
                else
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SCSB, "删除失败", "delOrder");
                }
            }
            else
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YWYC, "业务异常", "delOrder");
            }
        }
        catch (Exception e)
        {
            logger.error("业务异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YWYC, "业务异常", "delOrder");
        }
        return retMap;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> buyAgain(BuyAgainVo vo)
    {
        Map retMap = new HashMap();
        try
        {
            int storeId = vo.getStoreId();
            // 订单id
            int        orderId = vo.getId();
            OrderModel order   = new OrderModel();
            order.setStore_id(storeId);
            order.setId(orderId);
            order = orderModelMapper.selectOne(order);
            if (order != null)
            {
                String            userId            = order.getUser_id();
                String            sNo               = order.getsNo();
                OrderDetailsModel orderDetailsModel = new OrderDetailsModel();
                orderDetailsModel.setR_sNo(sNo);
                orderDetailsModel.setStore_id(storeId);
                List<OrderDetailsModel> orderDetailsModelList = orderDetailsModelMapper.select(orderDetailsModel);
                // 可以添加购物车
                int cartType = 0;
                for (OrderDetailsModel orderDetails : orderDetailsModelList)
                {
                    Integer num = productListModelMapper.getProductNum(storeId, orderDetails.getP_id(), Integer.parseInt(orderDetails.getSid()));
                    num = num == null ? 0 : num;
                    if (num > 0)
                    {
                        if (num >= orderDetails.getNum())
                        {
                            // 可以添加购物车
                            cartType = 1;
                        }
                        else
                        {
                            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_KCBCZ_001, "库存不充足", "buyAgain");
                        }
                    }
                    else
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SPYSQHWSJWFGM, "商品已售罄或未上架无法购买", "buyAgain");
                    }
                }
                List<Integer> cartId = new ArrayList<>();
                if (cartType == 1)
                {
                    for (OrderDetailsModel orderDetails : orderDetailsModelList)
                    {
                        BuyAgainModal buyAgainModal = new BuyAgainModal();
                        buyAgainModal.setStore_id(storeId);
                        buyAgainModal.setUser_id(userId);
                        buyAgainModal.setGoods_id(orderDetails.getP_id());
                        buyAgainModal.setGoods_num(orderDetails.getNum());
                        buyAgainModal.setCreate_time(new Date());
                        buyAgainModal.setSize_id(orderDetails.getSid());
                        buyAgainModalMapper.insert(buyAgainModal);
                        // 得到添加数据的id
                        int id = buyAgainModal.getId();
                        if (id < 1)
                        {
                            logger.info("添加购物车失败！");
                        }
                        cartId.add(id);
                    }
                }
                retMap.put("code", "200");
                retMap.put("cart_id", Joiner.on(SplitUtils.DH).join(cartId));
                retMap.put("message", "操作成功");
            }
            else
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YWYC, "业务异常", "buyAgain");
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("业务异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YWYC, "业务异常", "buyAgain");
        }
        return retMap;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> orderSearch(OrderVo vo)
    {
        Map resultMap = Maps.newHashMap();
        try
        {
            int storeId = vo.getStoreId();
            // 订单号
            String sNo = vo.getsNo();
            // 根据微信id,查询用户id
            User              user              = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil);
            OrderDetailsModel orderDetailsModel = new OrderDetailsModel();
            orderDetailsModel.setStore_id(storeId);
            //TODO
            orderDetailsModel.setR_status(4);
            orderDetailsModel.setR_sNo(sNo);
            orderDetailsModel = orderDetailsModelMapper.selectOne(orderDetailsModel);
            JSONObject dataJson = new JSONObject();
            if (orderDetailsModel != null)
            {
                // 商品id
                int pid = orderDetailsModel.getP_id();
                // 类型
                int r_type = orderDetailsModel.getR_type();
                dataJson = JSONObject.parseObject(JSON.toJSONString(orderDetailsModel));
                OrderModel order = new OrderModel();
                order.setStore_id(storeId);
                order.setsNo(sNo);
                order.setUser_id(user.getUser_id());
                order = orderModelMapper.selectOne(order);
                int order_id = order.getId();

                // 根据产品id,查询产品列表 (产品图片)
                ProductListModel productListModel = new ProductListModel();
                productListModel.setStore_id(storeId);
                productListModel.setId(pid);
                productListModel = productListModelMapper.selectOne(productListModel);
                String url = "";
                if (productListModel != null)
                {
                    // 拼图片路径
                    url = publiceService.getImgPath(productListModel.getImgurl(), productListModel.getStore_id());
                }
                dataJson.put("order_id", order_id);
                dataJson.put("imgurl", url);
                if (r_type == 0)
                {
                    dataJson.put("prompt", "审核中");
                    dataJson.put("buyer", "");
                    dataJson.put("return_state", "");
                }
                else if (r_type == 1)
                {
                    ServiceAddressModel serviceAddressModel = new ServiceAddressModel();
                    serviceAddressModel.setStore_id(storeId);
                    serviceAddressModel.setUid("admin");
                    serviceAddressModel.setIs_default(DictionaryConst.DefaultMaven.DEFAULT_OK);
                    serviceAddressModel.setType(ServiceAddressModel.TYPE_RETURN_GOODS);
                    serviceAddressModel = serviceAddressModelMapper.selectOne(serviceAddressModel);
                    Map buyer = Maps.newHashMap();
                    buyer.put("tel", serviceAddressModel.getTel());
                    buyer.put("name", serviceAddressModel.getName());
                    buyer.put("address_xq", serviceAddressModel.getAddress_xq());
                    dataJson.put("prompt", "审核通过");
                    dataJson.put("buyer", buyer);
                    dataJson.put("return_state", "");
                }
                else if (r_type == 2)
                {
                    dataJson.put("prompt", "拒绝");
                    dataJson.put("buyer", "");
                    dataJson.put("return_state", "");
                }
                else if (r_type == 3)
                {
                    dataJson.put("prompt", "审核通过");
                    dataJson.put("buyer", "");
                    dataJson.put("return_state", "");
                }
                else if (r_type == 4)
                {
                    dataJson.put("prompt", "退货完成");
                    dataJson.put("buyer", "");
                    dataJson.put("return_state", "退货退款");
                }
                else if (r_type == 5)
                {
                    dataJson.put("prompt", "退货失败");
                    dataJson.put("buyer", "");
                    dataJson.put("return_state", "退货退款");
                }
                resultMap.put("code", 200);
                resultMap.put("data", dataJson);
                resultMap.put("message", "操作成功");
            }
            else
            {
                resultMap.put("code", 200);
                resultMap.put("data", null);
                resultMap.put("message", "操作成功");
            }
        }
        catch (Exception e)
        {
            logger.error("业务异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YWYC, "业务异常", "orderSearch");
        }
        return resultMap;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> delCart(OrderVo vo)
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            int store_id = vo.getStoreId();
            // 订单id
            String cartIds = vo.getCarts();
            User   user    = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil);
            if (StringUtils.isEmpty(cartIds))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YWYC, "业务异常", "delCart");
            }

            String[] cartIdArrays = cartIds.split(SplitUtils.DH);
            for (String cartId : cartIdArrays)
            {
                CartModel cartModel = new CartModel();
                cartModel.setStore_id(store_id);
                cartModel.setId(Integer.valueOf(cartId));
                cartModel.setUser_id(user.getUser_id());
                List<CartModel> cartModels = cartModelMapper.select(cartModel);
                if (!CollectionUtils.isEmpty(cartModels))
                {
                    int row = cartModelMapper.delete(cartModel);
                    if (row < 0)
                    {
                        logger.error("删除购物车失败");
                        resultMap.put("code", ABNORMAL_BIZ);
                        resultMap.put("message", "操作失败");
                    }
                }
            }
            resultMap.put("code", 200);
            resultMap.put("message", "操作成功");
        }
        catch (Exception e)
        {
            logger.error("业务异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YWYC, "业务异常", "delCart");
        }
        return resultMap;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> getPaymentConf(OrderVo vo)
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            int store_id = vo.getStoreId();
            // 接收参数 // 链接
            String url = vo.getUrl();
            // 支付类名称
            String type = vo.getPayClassName();
            // 返回参数
            Map<String, Object> tmpMap = new HashMap<>(16);
            tmpMap.put("config", "");
            tmpMap.put("url", url);
            if (!StringUtils.isEmpty(type))
            {
                String config = paymentConfigModelMapper.getPaymentConfigInfo(store_id, type);
                if (!StringUtils.isEmpty(config))
                {
                    config = config.replaceAll("%2B", "\\+");
                    tmpMap.put("config", JSON.parse(config));
                }
            }
            resultMap.put("data", tmpMap);
            resultMap.put("code", 200);
            resultMap.put("message", "操作成功");
        }
        catch (Exception e)
        {
            logger.error("业务异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YWYC, "业务异常", "orderSearch");
        }
        return resultMap;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> orderDetails(OrderVo vo)
    {
        Map<String, Object> resultMap;
        try
        {
            resultMap = publicOrderService.ucOrderDetails(vo);
        }
        catch (LaiKeAPIException e)
        {
            logger.error("订单详情 异常", e);
            throw e;
        }
        catch (Exception e)
        {
            logger.error("订单详情异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_FFRQ, "非法入侵", "orderDetails");
        }
        return resultMap;
    }

    /**
     * @param vo
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> showLogistics(OrderVo vo)
    {
        Map<String, Object> map;
        try
        {
            map = publicOrderService.getLogistics(vo.getsNo());
        }
        catch (Exception e)
        {
            logger.error("物流获取异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_HQWLXXYC, "获取物流信息 异常", "getLogistics");
        }
        return map;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> cancleApply(int storeId, int id)
    {
        Map map = new HashMap();
        try
        {
            ReturnOrderModel returnOrderModel = new ReturnOrderModel();
            returnOrderModel.setId(id);
            returnOrderModel.setStore_id(storeId);
            int row = returnOrderModelMapper.delete(returnOrderModel);
            if (row > 0)
            {
                map.put("code", 200);
            }
            else
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YWYC, "业务异常", "cancleApply");
            }
        }
        catch (Exception e)
        {
            logger.error("业务异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YWYC, "业务异常", "cancleApply");
        }
        return map;
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
            if (!vo.getType().equals(DictionaryConst.OrdersType.ORDERS_HEADER_GM))
            {
                params.put("type", vo.getType());
            }
            else
            {
                ArrayList<String> typeList = new ArrayList<>();
                typeList.add(vo.getType());
                typeList.add(DictionaryConst.OrdersType.ORDERS_HEADER_PT);
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
                    arr = returnOrderInfo;
                    // 根据产品id,查询产
                    ProductListModel productListModel = new ProductListModel();
                    productListModel.setId(p_id);
                    productListModel = productListModelMapper.selectOne(productListModel);
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
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YWYC, "业务异常", "returnOrderList");
        }
        return resultMap;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> okOrder(OrderVo orderVo)
    {
        try
        {
            return publicOrderService.okOrder(orderVo.getStoreId(), orderVo.getAccessId(), orderVo.getsNo(), orderVo.getRtype());
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("确认收货 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YWYC, "业务异常", "okOrder");
        }

    }

    @Transactional(rollbackFor = Exception.class)
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
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TPSCSB_001, "图片上传失败", "returnData");
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
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "returnData");
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
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误");
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
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DDXXBCZ, "订单信息不存在", "returnData");
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
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DDXXBCZ, "订单信息不存在", "returnData");
                    }

                    if (!isWholeOrderAfterSaler)
                    {
                        OrderDetailsModel orderDetailsTmp = new OrderDetailsModel();
                        orderDetailsTmp.setR_sNo(sNo);
                        List<OrderDetailsModel> orderTotalDetailsList = orderDetailsModelMapper.select(orderDetailsTmp);
                        //如果子订单和申请售后的订单数量一致 则为订单整体售后
                        isWholeOrderAfterSaler = orderTotalDetailsList.size() == detailIds.size();
                    }

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
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YGSHQ, "已过售后期", "returnData");
                    }
                }
                //判断当前明细是否已经在售后中
                if (returnOrderModelMapper.orderDetailReturnIsNotEnd(vo.getStoreId(), orderModel.getsNo(), detailId) > 0)
                {
                    logger.info("订单{} 明细id{} 正在售后中,无法申请售后!", orderModel.getsNo(), detailId);
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_LBYSPZSHZ, "列表有商品再售后中", "returnData");
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
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_JEBNDYDQDDZJE, "金额不能大于当前订单总金额", "returnData");
                }

                //获取退款金额
                BigDecimal refundPrice = publicOrderService.getOrderPrice(detailId, vo.getStoreId());
                returnOrderModel.setRe_money(refundPrice);
                if (isWholeOrderAfterSaler)
                {
                    returnOrderModel.setRe_money(refundPrice.add(orderDetailsModel.getFreight()));
                }
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

                int count = returnOrderModelMapper.insertSelective(returnOrderModel);
                if (count < 1)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SCSHDDSB, "生成售后订单失败", "returnData");
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
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLGZSHJLSB, "网络故障,售后记录失败", "returnData");
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
                //这里只处理直播商品
                //如果详单商品未发货可以进行极速退款
                //判断详单是否已经发货
                if (orderModel.getStatus().equals(DictionaryConst.OrdersStatus.ORDERS_R_STATUS_CONSIGNMENT) &&
                        orderModel.getOtype().equals(DictionaryConst.OrdersType.ORDERS_HEADER_ZB))
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

                    System.out.println("走了极速退款");
                }
                else
                {
                    if (!refundAmtBtn && !refund)
                    {
                        logger.debug("订单详情id{} 已达到申请限制", detailId);
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YDDSHSQXZ, "已达到售后申请限制");
                    }
                }
            }
            if (orderModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DDXXBCZ, "订单信息不存在", "returnData");
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
            resultMap.put("time", DateUtil.dateFormate(new Date(), GloabConst.TimePattern.YMDHMS));
            resultMap.put("refund_amount", vo.getRefundApplyMoney());
            resultMap.put("type", vo.getType());
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("申请售后 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "returnData");
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
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误");
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
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DDXXBCZ, "订单信息不存在", "returnData");
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
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DDXXBCZ, "订单信息不存在", "returnData");
                    }

                    if (!isWholeOrderAfterSaler)
                    {
                        OrderDetailsModel orderDetailsTmp = new OrderDetailsModel();
                        orderDetailsTmp.setR_sNo(sNo);
                        List<OrderDetailsModel> orderTotalDetailsList = orderDetailsModelMapper.select(orderDetailsTmp);
                        //如果子订单和申请售后的订单数量一致 则为订单整体售后
                        isWholeOrderAfterSaler = orderTotalDetailsList.size() == detailIds.size();
                    }

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
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YGSHQ, "已过售后期", "returnData");
                    }
                }
                //判断当前明细是否已经在售后中
                if (returnOrderModelMapper.orderDetailReturnIsNotEnd(vo.getStoreId(), orderModel.getsNo(), detailId) > 0)
                {
                    logger.info("订单{} 明细id{} 正在售后中,无法申请售后!", orderModel.getsNo(), detailId);
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_LBYSPZSHZ, "列表有商品再售后中", "returnData");
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
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_JEBNDYDQDDZJE, "金额不能大于当前订单总金额", "returnData");
                }

                //获取退款金额
                BigDecimal refundPrice = publicOrderService.getOrderPrice(detailId, vo.getStoreId());
                returnOrderModel.setRe_money(refundPrice);
                if (isWholeOrderAfterSaler)
                {
                    refundPrice = refundPrice.add(orderDetailsModel.getFreight());
                    returnOrderModel.setRe_money(refundPrice);
                }
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

                int count = returnOrderModelMapper.insertSelective(returnOrderModel);
                if (count < 1)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SCSHDDSB, "生成售后订单失败", "returnData");
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
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLGZSHJLSB, "网络故障,售后记录失败", "returnData");
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
                //这里只处理直播商品
                //如果详单商品未发货可以进行极速退款
                //判断详单是否已经发货
                if (orderModel.getStatus().equals(DictionaryConst.OrdersStatus.ORDERS_R_STATUS_CONSIGNMENT) &&
                        orderModel.getOtype().equals(DictionaryConst.OrdersType.ORDERS_HEADER_ZB))
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

                    System.out.println("走了极速退款");
                }
                else
                {
                    if (!refundAmtBtn && !refund)
                    {
                        logger.debug("订单详情id{} 已达到申请限制", detailId);
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YDDSHSQXZ, "已达到售后申请限制");
                    }
                }
            }
            if (orderModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DDXXBCZ, "订单信息不存在", "returnData");
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
            resultMap.put("time", DateUtil.dateFormate(new Date(), GloabConst.TimePattern.YMDHMS));
            resultMap.put("refund_amount", vo.getRefundApplyMoney());
            resultMap.put("type", vo.getType());
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("申请售后 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "returnData");
        }
        return resultMap;
    }

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
                    Map<String, Boolean> btnMap        = publicRefundService.afterSaleButtonShow(storeId, orderModel.getOtype(), orderDetails);
                    boolean              currentRefund = MapUtils.getBooleanValue(btnMap, "refund"), currentRefundAmt = MapUtils.getBooleanValue(btnMap, "refundAmt"), currentRefundGoodsAmt = MapUtils.getBooleanValue(btnMap, "refundGoodsAmt"), currentRefundGoods = MapUtils.getBooleanValue(btnMap, "refundGoods");
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
                    if (isWholeOrderAfterSaler)
                    {
                        refundPrice = refundPrice.add(orderDetails.getFreight());
                    }

                    ConfiGureModel confiGureModel = confiGureModelMapper.selectByPrimaryKey(sid);
                    if (confiGureModel != null)
                    {
                        String imgPath = publiceService.getImgPath(confiGureModel.getImg(), storeId);
                        jsonObject.put("image",imgPath);
                        jsonObject.put("imgurl",imgPath);
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
            }
            else
            {
                selfLifting = 0;
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
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YWYC, "业务异常", "returnMethod");
        }
    }


    @Override
    public Map<String, Object> seeSend(int storeId, int productId)
    {
        Map<String, Object> returnMap = new HashMap<>(16);
        try
        {
            String           address          = "";
            String           name             = "";
            String           phone            = "";
            ProductListModel productListModel = new ProductListModel();
            productListModel.setStore_id(storeId);
            productListModel.setId(productId);
            // 根据商品id，查询商品信息
            productListModel = productListModelMapper.selectOne(productListModel);

            if (productListModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YWYC, "业务异常", "seeSend");
            }

            // 店铺ID
            int mchId = productListModel.getMch_id();
            // 根据店铺ID，查询管理员信息
            AdminModel adminModel = new AdminModel();
            adminModel.setShop_id(mchId);
            adminModel.setStore_id(storeId);
            List<AdminModel> adminModels = adminModelMapper.select(adminModel);
            if (adminModels != null && adminModels.size() > 0)
            {
                ServiceAddressModel serviceAddressModel = new ServiceAddressModel();
                serviceAddressModel.setStore_id(storeId);
                serviceAddressModel.setUid("admin");
                serviceAddressModel.setIs_default(DictionaryConst.DefaultMaven.DEFAULT_OK);
                serviceAddressModel.setType(ServiceAddressModel.TYPE_RETURN_GOODS);
                serviceAddressModel = serviceAddressModelMapper.selectOne(serviceAddressModel);
                if (serviceAddressModel != null)
                {
                    address = serviceAddressModel.getAddress_xq();
                    name = serviceAddressModel.getName();
                    phone = serviceAddressModel.getTel();
                }
            }
            else
            {
                MchModel mchModel = new MchModel();
                mchModel.setStore_id(storeId);
                mchModel.setId(mchId);
                mchModel = mchModelMapper.selectOne(mchModel);
                if (mchModel != null)
                {
                    address = mchModel.getSheng() + mchModel.getShi() + mchModel.getXian() + mchModel.getAddress();
                    name = mchModel.getName();
                    phone = mchModel.getTel();
                }
            }
            if (StringUtils.isNotEmpty(productListModel.getGongyingshang()))
            {
                SupplierModel supplierModel = supplierModelMapper.selectByPrimaryKey(productListModel.getGongyingshang());
                address = supplierModel.getProvince() + supplierModel.getCity() + supplierModel.getArea() + supplierModel.getAddress();
                name = supplierModel.getContacts();
                phone = supplierModel.getContact_phone();
            }

            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("sort_sort", DataUtils.Sort.DESC.toString());
            parmaMap.put("recycle", DictionaryConst.ProductRecycle.NOT_STATUS);
            parmaMap.put("is_open", 1);
            List<Map<String, Object>> list = expressModelMapper.selectDynamic(parmaMap);

            returnMap.put("address", address);
            returnMap.put("name", name);
            returnMap.put("phone", phone);
            returnMap.put("express", list);
            returnMap.put("message", "操作成功");
            return returnMap;
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取售后地址和快递信息异常 ", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "returnData");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> backSend(ReturnGoodsVo returnGoodsVo)
    {
        Map<String, Object> returnMap = new HashMap<>(16);
        try
        {
            User user = returnGoodsVo.getUser();
            if (user == null)
            {
                user = RedisDataTool.getRedisUserCache(returnGoodsVo.getAccessId(), redisUtil);
            }
            // 根据微信id,查询用户id
            String userId = user.getUser_id();

            ReturnOrderModel returnOrderModel = new ReturnOrderModel();
            returnOrderModel.setStore_id(returnGoodsVo.getStoreId());
            returnOrderModel.setId(returnGoodsVo.getId());
            returnOrderModel = returnOrderModelMapper.selectOne(returnOrderModel);
            if (returnOrderModel == null)
            {
                throw new LaiKeAPIException(DATA_NOT_EXIST, "售后信息不存在");
            }
            Integer oid = returnOrderModel.getP_id();

            ReturnGoodsModel returnGoodsModel = new ReturnGoodsModel();
            returnGoodsModel.setStore_id(returnGoodsVo.getStoreId());
            returnGoodsModel.setName(returnGoodsVo.getLxr());
            returnGoodsModel.setTel(returnGoodsVo.getLxdh());
            returnGoodsModel.setExpress(returnGoodsVo.getKdname());
            returnGoodsModel.setExpress_num(returnGoodsVo.getKdcode());
            returnGoodsModel.setUser_id(userId);
            returnGoodsModel.setUid(userId);
            returnGoodsModel.setOid(oid + "");
            returnGoodsModel.setAdd_data(new Date());
            returnGoodsModel.setRe_id(returnGoodsVo.getId());
            int row = returnGoodsModelMapper.insert(returnGoodsModel);
            if (row < 1)
            {
                logger.error("添加回退物品信息失败.");
            }
            ExpressModel expressModel = new ExpressModel();
            if (returnGoodsVo.getKdId() != null)
            {
                expressModel.setId(returnGoodsVo.getKdId());
            }
            else
            {
                expressModel.setKuaidi_name(returnGoodsVo.getKdname());
            }
            expressModel = expressModelMapper.selectOne(expressModel);

            String sNo = returnOrderModel.getsNo();
            OrderModel orderModel = new OrderModel();
            orderModel.setsNo(sNo);
            orderModel = orderModelMapper.selectOne(orderModel);
            if (Objects.isNull(orderModel))
            {
                throw new LaiKeAPIException(ERROR_CODE_DDBCZ,"订单不存在");
            }

            //如果订单cpc不是国内，则调用17track发货
            if (!CpcUtils.isHome(orderModel.getCpc()))
            {
                publicOrderService.trackDelivery(returnGoodsVo.getStoreId(),returnGoodsVo.getKdcode());
            }


            //重新生成一条售后记录 每次操作都增加一条售后记录 add by trick 2023-02-24 10:29:02
            ReturnRecordModel returnRecordSave = new ReturnRecordModel();
            returnRecordSave.setUser_id(user.getUser_id());
            returnRecordSave.setStore_id(returnGoodsVo.getStoreId());
            returnRecordSave.setRe_type(returnOrderModel.getRe_type());
            returnRecordSave.setR_type(DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_USER_DELIVERED);
            returnRecordSave.setsNo(returnOrderModel.getsNo());
            returnRecordSave.setP_id(returnOrderModel.getId());
            returnRecordSave.setMoney(BigDecimal.ZERO);
            returnRecordSave.setProduct_id(returnOrderModel.getPid());
            returnRecordSave.setAttr_id(returnOrderModel.getSid());
            returnRecordSave.setExpress_id(expressModel.getId());
            returnRecordSave.setCourier_num(returnGoodsModel.getExpress_num());
            returnRecordSave.setRe_time(new Date());
            row = returnRecordModelMapper.insertSelective(returnRecordSave);
            if (row < 1)
            {
                logger.info("添加售后信息失败 参数:" + JSON.toJSONString(returnRecordSave));
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLGZSHJLSB, "网络故障,售后记录失败", "returnData");
            }
            row = returnOrderModelMapper.updateReturnOrder(returnGoodsVo.getStoreId(), returnGoodsVo.getId(), DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_USER_DELIVERED);
            if (row < 1)
            {
                throw new LaiKeAPIException(OPERATION_FAILED, "操作失败", "returnData");
            }
        }
        catch (LaiKeAPIException l)
        {
            logger.error("获取售后地址和快递信息错误 :", l);
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取售后地址和快递信息异常 :", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "returnData");
        }
        returnMap.put("code", 200);
        returnMap.put("message", "操作成功");
        return returnMap;
    }

    /**
     * 查看提货码
     *
     * @param vo
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> seeExtractionCode(OrderVo vo)
    {
        Map resultMap = Maps.newHashMap();
        try
        {
            int storeId = vo.getStoreId();
            // 订单id
            int orderId = vo.getOrderId();
            // 根据微信id,查询用户id
            User user = vo.getUser();
            if (user == null)
            {
                user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil);
            }
            String userId = user.getUser_id();
            Map    map    = new HashMap();
            map.put("store_id", storeId);
            map.put("id", orderId);
            List<Map<String, Object>> orderInfoList = orderModelMapper.getOrderInfoLeftDetailDynamic(map);
            if (CollectionUtils.isEmpty(orderInfoList))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YWYC, "业务异常", "seeExtractionCode");
            }
            Map<String, Object> orderInfo = orderInfoList.get(0);
            // 提货码
            String   extractionCode    = DataUtils.getStringVal(orderInfo, "extraction_code");
            String   extractionCodeImg = null;
            String[] rew               = extractionCode.split(SplitUtils.DH);
            // 订单状态
            int status = DataUtils.getIntegerVal(orderInfo, "status");
            // 总价
            BigDecimal zPrice = DataUtils.getBigDecimalVal(orderInfo, "z_price");
            // 订单号
            String sNo = DataUtils.getStringVal(orderInfo, "sNo");
            // 待收货
            if (status == ORDERS_R_STATUS_DISPATCHED)
            {
                String extraction_code1 = extractionCode;
                if (rew.length != 3)
                {
                    // 店铺
                    extraction_code1 = publicMchService.extractionCode();
                    String[] extraction_code2 = extraction_code1.split(SplitUtils.DH);
                    extractionCode = extraction_code2[0];
                    extractionCodeImg = publicMchService.createQRCodeImg(extraction_code1, storeId, vo.getStoreType());
                }
                else
                {
                    long time = System.currentTimeMillis() / 1000;
                    if (Long.parseLong(rew[2]) <= time)
                    {
                        // 提货码有效时间 小于等于 当前时间
                        extraction_code1 = publicMchService.extractionCode();
                        String[] extraction_code2 = extraction_code1.split(SplitUtils.DH);
                        extractionCode = extraction_code2[0];
                        extractionCodeImg = publicMchService.createQRCodeImg(extraction_code1, storeId, vo.getStoreType());
                    }
                    else
                    {
                        // 提货码
                        extractionCode = rew[0];
                        // 提现码二维码
                        extractionCodeImg = DataUtils.getStringVal(orderInfo, "extraction_code_img");
                    }
                }
                OrderModel updateOrder = new OrderModel();
                updateOrder.setId(orderId);
                updateOrder.setExtraction_code(extraction_code1);
                updateOrder.setExtraction_code_img(extractionCodeImg);
                int count = orderModelMapper.updateByPrimaryKeySelective(updateOrder);
            }
            else
            {
                // 提货码
                extractionCode = rew[0];
                // 提现码二维码
                extractionCodeImg = DataUtils.getStringVal(orderInfo, "extraction_code_img");
            }
            int num = 0;
            // 商品ID
            int productId = DataUtils.getIntegerVal(orderInfo, "p_id");
            map.clear();
            map.put("store_id", storeId);
            map.put("user_id", userId);
            map.put("id", orderId);
            List<Map<String, Object>> orderCodesInfo = orderModelMapper.seeExtractionCode(map);
            List<Map<String, Object>> products       = new ArrayList<>();
            for (int i = 0; i < orderCodesInfo.size(); i++)
            {
                Map<String, Object> productMap = orderCodesInfo.get(i);
                // 商品ID
                int proId = MapUtils.getInteger(productMap, "pid");
                // 商品规格id
                int productSid = MapUtils.getInteger(productMap, "sid");
                Map params     = new HashMap();
                params.put("store_id", storeId);
                params.put("sid", productSid);
                params.put("p_id", proId);
                List<Map<String, Object>> productInfoList = productListModelMapper.getGoodsTitleAndImg(params);
                if (CollectionUtils.isEmpty(productInfoList))
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YWYC, "业务异常", "seeExtractionCode");
                }
                Map<String, Object> productInfoMap = productInfoList.get(0);
                // 商品名称
                String product_title = DataUtils.getStringVal(productInfoMap, "product_title");
                // 拼图片路径
                String img = publiceService.getImgPath(DataUtils.getStringVal(productInfoMap, "img"), storeId);
                num = num + MapUtils.getInteger(productMap, "num");
                Map productRetMap = new HashMap();
                productRetMap.put("p_id", productId);
                productRetMap.put("product_title", product_title);
                productRetMap.put("p_price", DataUtils.getBigDecimalVal(productMap, "p_price", BigDecimal.ZERO));
                productRetMap.put("num", MapUtils.getInteger(productMap, "num"));
                productRetMap.put("sid", MapUtils.getInteger(productMap, "sid"));
                productRetMap.put("size", DataUtils.getStringVal(productMap, "size"));
                productRetMap.put("img", img);
                products.add(productRetMap);
            }
            Map data = Maps.newHashMap();
            data.put("status", status);
            data.put("extraction_code", extractionCode);
            data.put("extraction_code_img", extractionCodeImg);
            data.put("por_list", products);
            data.put("z_price", zPrice);
            data.put("sNo", sNo);
            data.put("num", num);

            return data;
        }
        catch (Exception e)
        {
            logger.error("查看提货码异常:", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YWYC, "业务异常", "seeExtractionCode");
        }
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
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SHXXBCZ, "售后信息不存在", "returnData");
            }
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
            goodsInfo.put("price", MapUtils.getString(returnOrder, "p_price"));
            goodsInfo.put("img", publiceService.getImgPath(MapUtils.getString(returnOrder, "img"), storeId));
            goodsInfo.put("mchName", MapUtils.getString(returnOrder, "mchName"));
            goodsInfo.put("mchLogo", publiceService.getImgPath(MapUtils.getString(returnOrder, "logo"), storeId));
            goodsInfo.put("headimg", publiceService.getImgPath(MapUtils.getString(returnOrder, "head_img"), storeId));
            goodsInfo.put("num", MapUtils.getInteger(returnOrder, "num"));
            goodsInfo.put("mchId", MapUtils.getInteger(returnOrder, "mchId"));

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
            info.put("r_sNo", returnOrder.get("sNo"));
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

            // 根据商品id，查询商品信息
            ProductListModel productListModel = new ProductListModel();
            productListModel.setStore_id(storeId);
            productListModel.setId(pid);

            String address = "";
            String name    = "";
            String phone   = "";

            //r0
            productListModel = productListModelMapper.selectOne(productListModel);
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
                }
                else
                {
                    MchModel mchModel = mchModelMapper.selectByPrimaryKey(productListModel.getMch_id());
                    if (mchModel != null)
                    {
                        address = mchModel.getSheng() + mchModel.getShi() + mchModel.getXian() + mchModel.getAddress();
                        name = mchModel.getRealname();
                        phone = mchModel.getTel();
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
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YWYC, "业务异常", "returndetails");
            }
            Map<String, Object> store_info = Maps.newHashMap();
            store_info.put("address", address);
            store_info.put("name", name);
            store_info.put("phone", phone);
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
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YWYC, "业务异常", "returndetails");
        }
        return resultMap;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> confirmReceipt(ReturnConfirmReceiptVo vo)
    {
        Map resultMap = Maps.newHashMap();
        try
        {
            // 商城ID
            int storeId = vo.getStoreId();
            //售后订单ID
            int    id        = vo.getId();
            String access_id = vo.getAccessId();
            User   user      = null;
            if (GloabConst.StoreType.STORE_TYPE_PC_MALL == vo.getStoreType())
            {
                user = RedisDataTool.getRedisUserCache(access_id, redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_PC_SHOP_TOKEN, true);
            }
            else
            {
                user = RedisDataTool.getRedisUserCache(access_id, redisUtil);
            }
            String           userId           = user.getUser_id();
            ReturnOrderModel returnOrderModel = new ReturnOrderModel();
            returnOrderModel.setStore_id(storeId);
            returnOrderModel.setId(id);
            returnOrderModel = returnOrderModelMapper.selectOne(returnOrderModel);
            int exchange_num = 0;
            int oid          = 0;
            if (returnOrderModel != null)
            {
                //订单详情ID
                oid = returnOrderModel.getP_id();
                //订单号
//                String sNo = returnOrderModel.getsNo();
                OrderDetailsModel orderDetailsModel1 = new OrderDetailsModel();
                orderDetailsModel1.setStore_id(storeId);
                orderDetailsModel1.setUser_id(userId);
                orderDetailsModel1.setId(oid);
//                orderDetailsModel1.setR_sNo(sNo);
                orderDetailsModel1 = orderDetailsModelMapper.selectOne(orderDetailsModel1);
                if (orderDetailsModel1 != null)
                {
                    exchange_num = orderDetailsModel1.getExchange_num() + 1;
                }
            }
            else
            {
                logger.error("售后确认收货异常");
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SHQRSHYC, "售后确认收货异常", "confirmReceipt");
            }

            int row = returnOrderModelMapper.updateReturnOrder(storeId, id, DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_AFTER_SALE_END);
            if (row < 0)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SHQRSHYC, "售后确认收货异常", "confirmReceipt");
            }

            row = orderDetailsModelMapper.confirmReceiptOrderDetaisl(exchange_num, storeId, userId, oid);
            if (row < 0)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SHQRSHYC, "售后确认收货异常", "confirmReceipt");
            }
            else
            {
                resultMap.put("code", 200);
                resultMap.put("message", "操作成功");
            }
        }
        catch (Exception e)
        {
            logger.error("售后确认收货异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SHQRSHYC, "售后确认收货异常", "confirmReceipt");
        }

        return resultMap;
    }

    /**
     * 撤销申请
     *
     * @param vo
     * @return
     */
    @Override
    public Map<String, Object> cancelApplication(CancleAfterSaleApplyVo vo)
    {
        Map<String, Object> resultMap = Maps.newHashMap();
        try
        {
            RedisDataTool.isLogin(vo.getAccessId(), redisUtil);
            int id = vo.getId();
            //判断是否可以撤销
            ReturnOrderModel returnOrderModel = returnOrderModelMapper.selectByPrimaryKey(id);
            if (returnOrderModel == null)
            {
                throw new LaiKeAPIException(DATA_NOT_EXIST, "数据不存在");
            }
            if (!returnOrderModel.getR_type().equals(DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_EXAMEWAIT_STATUS))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_ZTFSBH, "状态发生变化");
            }
            returnOrderModelMapper.deleteByPrimaryKey(id);
            //操作成功
            resultMap.put("code", "200");
            resultMap.put("message", "撤销成功");
            return resultMap;
        }
        catch (LaiKeAPIException l)
        {
            logger.error("售后确认收货自定义异常", l);
            throw l;
        }
        catch (Exception e)
        {
            logger.error("售后确认收货异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CXSHSB, "撤销审核失败", "cancelApplication");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean splitSupplierOrder(int storeId, String orderNo) throws LaiKeAPIException
    {
        try
        {
            boolean    SupplierOrder = true;
            OrderModel orderOld      = new OrderModel();
            orderOld.setStore_id(storeId);
            orderOld.setsNo(orderNo);
            orderOld = orderModelMapper.selectOne(orderOld);
            if (orderOld == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CZSB, "操作失败", "splitSupplierOrder");
            }
            //支付总金额
            BigDecimal oldTotal = orderOld.getZ_price();
            //单个商品不做拆单
            OrderDetailsModel orderDetailsCount = new OrderDetailsModel();
            orderDetailsCount.setStore_id(storeId);
            orderDetailsCount.setR_sNo(orderNo);

            if (orderDetailsModelMapper.selectCount(orderDetailsCount) <= 1)
            {
                //单商品订单更新old_total n
                orderOld.setOld_total(oldTotal);
                orderModelMapper.updateByPrimaryKeySelective(orderOld);
                logger.debug("单个商品不做拆单");
                //进行供应商订单运费保存(用于店铺包邮但是供应商不包邮情况)
                this.supplierOrderFreight(orderOld.getStore_id(), orderOld.getsNo());
                return false;
            }
            List<Map<String, Object>> supplierOrderList = supplierOrderMapper.getSupplierSplitOrderList(storeId, orderNo);

            Map<Integer, OrderModel> supplierOrderMap = new HashMap<>(16);
            //供应商商品总数量(用于扣除父订单总数量中属于供应商商品的数量)
            int supplierProNum = 0;
            //开始拆单-分单
            if (supplierOrderList.size() > 0)
            {
                for (Map<String, Object> map : supplierOrderList)
                {
                    OrderModel orderModelTmp = new OrderModel();
                    //供应商商品id
                    Integer p_id = DataUtils.getIntegerVal(map, "pid");
                    //优惠后价格
                    BigDecimal payPrice = new BigDecimal(MapUtils.getString(map, "after_discount"));
                    //商品数量
                    BigDecimal num = DataUtils.getBigDecimalVal(map, "num");
                    supplierProNum += Integer.parseInt(String.valueOf(num));
                    //商品价格
                    BigDecimal productPrice = DataUtils.getBigDecimalVal(map, "p_price", BigDecimal.ZERO);
                    //商品运费
                    BigDecimal freight = DataUtils.getBigDecimalVal(map, "freight");
                    //当前规格供应商id
                    Integer supplierId = MapUtils.getInteger(map, "supplierId");
                    //当前订单明细id
                    Integer orderDetailsId = MapUtils.getInteger(map, "detailId");
                    // 生成订单号-每个供应商的商品分一个订单
                    if (!supplierOrderMap.containsKey(supplierId))
                    {
                        String sNoTmp = publicOrderService.createOrderNo(DictionaryConst.OrdersType.ORDERS_HEADER_GM);
                        BeanUtils.copyProperties(orderOld, orderModelTmp);
                        orderModelTmp.setId(null);
                        orderModelTmp.setsNo(sNoTmp);
                        orderModelTmp.setP_sNo(orderNo);
                        orderModelTmp.setZ_price(payPrice.add(freight));
                        orderModelTmp.setZ_freight(freight);
                        orderModelTmp.setOld_freight(freight);
                        orderModelTmp.setSpz_price(num.multiply(productPrice));
                        orderModelTmp.setNum(num.intValue());
                        orderModelTmp.setReal_sno(orderOld.getReal_sno());
                        orderModelTmp.setOld_total(payPrice.add(freight));
                        orderModelTmp.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS);
                        supplierOrderMap.put(supplierId, orderModelTmp);
                    }
                    else
                    {
                        orderModelTmp = supplierOrderMap.get(supplierId);
                        orderModelTmp.setZ_price(orderModelTmp.getZ_price().add(payPrice.add(freight)));
                        orderModelTmp.setSpz_price(orderModelTmp.getSpz_price().add(num.multiply(productPrice)));
                        orderModelTmp.setZ_freight(orderModelTmp.getZ_freight().add(freight));
                        orderModelTmp.setNum(orderModelTmp.getNum() + num.intValue());
                        orderModelTmp.setReal_sno(orderOld.getReal_sno());
                        orderModelTmp.setOld_total(orderModelTmp.getZ_price());
                        supplierOrderMap.put(supplierId, orderModelTmp);
                    }
                    //关联新订单
                    int row = orderDetailsModelMapper.updateOrderDetailsParentOrderNo(storeId, orderModelTmp.getsNo(), orderDetailsId);
                    if (row < 1)
                    {
                        logger.error("修改订单号失败！");
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CZSB, "操作失败");
                    }
                }
                //保存拆单后订单信息
                for (Integer supplierId : supplierOrderMap.keySet())
                {
                    logger.info("正在保存{}店铺的{}供应商订单信息", StringUtils.trim(orderOld.getMch_id(), SplitUtils.DH), supplierId);
                    OrderModel orderModel = supplierOrderMap.get(supplierId);
                    orderModelMapper.insertSelective(orderModel);
                    //更新主订单信息必要金额信息 --可以不扣减原订单金额 gp 2024-04-17 禅道53776    不行造成了其他问题55089
                    orderOld.setZ_price(orderOld.getZ_price().subtract(orderModel.getZ_price()));
                    orderOld.setSpz_price(orderOld.getSpz_price().subtract(orderModel.getSpz_price()));
                    orderOld.setZ_freight(orderOld.getZ_freight().subtract(orderModel.getZ_freight()));
                    orderModelMapper.updateByPrimaryKeySelective(orderOld);
                    //进行供应商订单运费保存(用于店铺包邮但是供应商不包邮情况)
                    this.supplierOrderFreight(orderModel.getStore_id(), orderModel.getsNo());
                }
                //更新父订单数量(如果父订单商品数量为0就直接关闭父订单)
                if (supplierProNum > 0)
                {
                    orderOld.setNum(orderOld.getNum() - supplierProNum);
                    if (orderOld.getNum() == 0)
                    {
                        orderOld.setStatus(ORDERS_R_STATUS_CLOSE);
                        SupplierOrder = false;
                    }
                    orderModelMapper.updateByPrimaryKeySelective(orderOld);
                }
                logger.info("保存供应商拆单信息成功！");
            }
            return SupplierOrder;
        }
        catch (LaiKeAPIException l)
        {
            logger.error("供应商单店铺拆单 自定义异常", l);
            throw l;
        }
        catch (Exception e)
        {
            logger.error("供应商单店铺拆单 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_, e.getMessage(), "splitSupplierOrder");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void splitSupplierOrder(int storeId, Map<Integer, List<Map<String, Object>>> mapList, String sNo, Integer status) throws LaiKeAPIException
    {
        try
        {
            //父订单信息
            OrderModel orderOld = new OrderModel();
            orderOld.setStore_id(storeId);
            orderOld.setsNo(sNo);
            orderOld = orderModelMapper.selectOne(orderOld);

            if (orderOld == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CZSB, "操作失败", "splitSupplierOrder");
            }
            BigDecimal oldTotal = orderOld.getZ_price();
            //拆备注
            Map<String, String> remarks      = new HashMap<>(16);
            String              orderRemarks = orderOld.getRemarks();
            if (!StringUtils.isEmpty(orderRemarks))
            {
                remarks = JSON.parseObject(orderRemarks, new TypeReference<Map<String, String>>()
                {
                });
            }
            for (Integer mchId : mapList.keySet())
            {
                List<OrderModel>          splitOrders       = new ArrayList<>();
                Map<Integer, OrderModel>  supplierOrderMap  = new HashMap<>(16);
                List<Map<String, Object>> supplierOrderList = mapList.get(mchId);
                for (Map<String, Object> supplierOrder : supplierOrderList)
                {
                    OrderModel orderModelTmp = new OrderModel();
                    //订单详情id
                    Integer order_details_id = DataUtils.getIntegerVal(supplierOrder, "id");
                    //供应商商品id
                    Integer          p_id             = DataUtils.getIntegerVal(supplierOrder, "p_id");
                    ProductListModel productListModel = productListModelMapper.selectByPrimaryKey(p_id);
                    //优惠后价格
                    BigDecimal payPrice = new BigDecimal(MapUtils.getString(supplierOrder, "after_discount"));
                    //商品数量
                    BigDecimal num = DataUtils.getBigDecimalVal(supplierOrder, "num");
                    //商品价格
                    BigDecimal productPrice = DataUtils.getBigDecimalVal(supplierOrder, "p_price", BigDecimal.ZERO);
                    //商品运费
                    BigDecimal freight = DataUtils.getBigDecimalVal(supplierOrder, "freight");
                    if (!supplierOrderMap.containsKey(Integer.valueOf(productListModel.getGongyingshang())))
                    {
                        String sNoTmp = publicOrderService.createOrderNo(DictionaryConst.OrdersType.ORDERS_HEADER_GM);
                        BeanUtils.copyProperties(orderOld, orderModelTmp);
                        orderModelTmp.setId(null);
                        orderModelTmp.setsNo(sNoTmp);
                        orderModelTmp.setP_sNo(sNo);
                        orderModelTmp.setMch_id(SplitUtils.DH + mchId + SplitUtils.DH);
                        orderModelTmp.setZ_price(payPrice.add(freight));
                        orderModelTmp.setOld_total(payPrice.add(freight));
                        orderModelTmp.setReal_sno(orderOld.getReal_sno());
                        orderModelTmp.setZ_freight(freight);
                        orderModelTmp.setOld_freight(freight);
                        orderModelTmp.setSpz_price(num.multiply(productPrice));
                        orderModelTmp.setNum(num.intValue());
                        orderModelTmp.setStatus(status);
                        orderModelTmp.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS);
                        //备注处理
                        if (!remarks.isEmpty())
                        {
                            String              remarksStr = remarks.get(mchId.toString());
                            Map<String, String> remarksMap = new HashMap<>(1);
                            if (StringUtils.isNotEmpty(remarksStr))
                            {
                                remarksMap.put(mchId.toString(), remarksStr);
                                orderModelTmp.setRemarks(JSON.toJSONString(remarksMap));
                            }
                        }
                        supplierOrderMap.put(Integer.valueOf(productListModel.getGongyingshang()), orderModelTmp);
                    }
                    else
                    {
                        orderModelTmp = supplierOrderMap.get(Integer.valueOf(productListModel.getGongyingshang()));
                        orderModelTmp.setZ_price(orderModelTmp.getZ_price().add(payPrice));
                        orderModelTmp.setSpz_price(orderModelTmp.getSpz_price().add(num.multiply(productPrice)));
                        orderModelTmp.setNum(orderModelTmp.getNum() + num.intValue());
                        orderModelTmp.setOld_total(orderModelTmp.getZ_price().add(payPrice));
                        orderModelTmp.setReal_sno(orderOld.getReal_sno());
                        supplierOrderMap.put(Integer.valueOf(productListModel.getGongyingshang()), orderModelTmp);
                    }
                    //关联新订单
                    int row = orderDetailsModelMapper.updateOrderDetailsParentOrderNo(storeId, orderModelTmp.getsNo(), order_details_id);
                    if (row < 1)
                    {
                        logger.error("修改订单号失败！");
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CZSB, "操作失败");
                    }
                }
                //保存拆单后订单信息
                for (Integer supplierId : supplierOrderMap.keySet())
                {
                    logger.info("正在保存{}店铺的{}供应商订单信息", mchId, supplierId);
                    OrderModel orderModel = supplierOrderMap.get(supplierId);
                    orderModelMapper.insertSelective(orderModel);
                    //更新主订单信息必要金额信息
                    orderOld.setZ_price(orderOld.getZ_price().subtract(orderModel.getZ_price()));
                    orderOld.setSpz_price(orderOld.getSpz_price().subtract(orderModel.getSpz_price()));
                    orderOld.setZ_freight(orderOld.getZ_freight().subtract(orderModel.getZ_freight()));
                    orderOld.setOld_total(oldTotal);
                    orderModelMapper.updateByPrimaryKeySelective(orderOld);
                    //进行供应商订单运费保存(用于店铺包邮但是供应商不包邮情况)
                    this.supplierOrderFreight(orderModel.getStore_id(), orderModel.getsNo());
                }
            }
        }
        catch (LaiKeAPIException l)
        {
            logger.error("供应商多店铺拆单 自定义异常", l);
            throw l;
        }
        catch (Exception e)
        {
            logger.error("供应商多店铺拆单 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_, e.getMessage(), "splitSupplierOrder");
        }
    }

    public void supplierOrderFreight(int storeId, String sNo) throws LaiKeAPIException
    {
        try
        {
            //供应商id
            String supplierId = "";
            //查询用户下单地址用于计算运费
            OrderModel orderModel = new OrderModel();
            orderModel.setStore_id(storeId);
            orderModel.setsNo(sNo);
            orderModel = orderModelMapper.selectOne(orderModel);
            UserAddress userAddress = new UserAddress();
            userAddress.setSheng(orderModel.getSheng());
            userAddress.setCity(orderModel.getShi());
            userAddress.setQuyu(orderModel.getXian());
            userAddress.setAddress(orderModel.getAddress());
            //总运费
            BigDecimal totalFreight = BigDecimal.ZERO;
            //计算订单运费Map
            Map<Integer, Map<String, Object>> getFreightMap   = new HashMap<>(16);
            List<Map<String, Object>>         orderDetailList = orderDetailsModelMapper.getOrderDetailNotClose(storeId, sNo);
            for (Map<String, Object> orderDetail : orderDetailList)
            {
                //详情id
                Integer id = MapUtils.getInteger(orderDetail, "id");
                //商品id
                Integer pid = MapUtils.getInteger(orderDetail, "p_id");
                //订单商品数量
                Integer          num              = MapUtils.getInteger(orderDetail, "num");
                ProductListModel productListModel = productListModelMapper.selectByPrimaryKey(pid);
                //商品单个重量
                BigDecimal weight = new BigDecimal(productListModel.getWeight());
                //商品运费模板id
                String freight = productListModel.getFreight();
                //商品所属供应商(因为是拆单之后所以订单里的商品只会属于一个供应商)
                supplierId = productListModel.getGongyingshang();
                if (getFreightMap.containsKey(pid))
                {
                    Map<String, Object> freightMap = getFreightMap.get(pid);
                    freightMap.put("num", MapUtils.getInteger(freightMap, "num") + num);
                }
                else
                {
                    Map<String, Object> map = new HashMap<>(16);
                    map.put("detailId", id);
                    map.put("num", num);
                    map.put("weight", weight);
                    map.put("freight", freight);
                    getFreightMap.put(pid, map);
                }
            }
            List<SupplierOrderFrightModel> supplierOrderFrightModelList = new ArrayList<>();
            for (Integer pid : getFreightMap.keySet())
            {
                Map<String, Object> map        = getFreightMap.get(pid);
                Integer             detailId   = MapUtils.getInteger(map, "detailId");
                Integer             num        = MapUtils.getInteger(map, "num");
                BigDecimal          weight     = new BigDecimal(MapUtils.getString(map, "weight"));
                Integer             freight    = MapUtils.getInteger(map, "freight");
                BigDecimal          proFreight = publicOrderService.getFreight(freight, userAddress, num, weight);
                totalFreight = totalFreight.add(proFreight);
                //添加供应商订单运费数据准备
                SupplierOrderFrightModel supplierOrderFrightModel = new SupplierOrderFrightModel();
                supplierOrderFrightModel.setStore_id(storeId);
                supplierOrderFrightModel.setsNo(sNo);
                supplierOrderFrightModel.setDetail_id(pid);
                supplierOrderFrightModel.setFreight(proFreight);
                supplierOrderFrightModel.setSupplier_id(supplierId);
                supplierOrderFrightModel.setIs_settlement(DictionaryConst.WhetherMaven.WHETHER_NO);
                supplierOrderFrightModel.setAdd_date(new Date());
                supplierOrderFrightModelList.add(supplierOrderFrightModel);
            }
            if (supplierOrderFrightModelList.size() > 0)
            {
                for (SupplierOrderFrightModel supplierOrderFrightModel : supplierOrderFrightModelList)
                {
                    supplierOrderFrightModel.setTotal_fright(totalFreight);
                }
                supplierOrderFrightModelMapper.insertList(supplierOrderFrightModelList);
            }
        }
        catch (LaiKeAPIException l)
        {
            logger.error("保存供应商订单运费信息 异常", l);
            throw l;
        }
        catch (Exception e)
        {
            logger.error("保存供应商订单运费信息 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_, e.getMessage(), "supplierOrderFreight");
        }
    }

    @Override
    public Map<String, Object> getPayment(OrderVo vo) throws LaiKeAPIException
    {
        Map<String, Object> payInfo = new HashMap<>(16);
        try
        {
            //各个支付的开启、关闭状态
            int storeId = vo.getStoreId();
            payInfo = publicPaymentConfigService.getPaymentInfos(storeId);
        }
        catch (LaiKeAPIException l)
        {
            logger.error("进入订单界面获取可用支付方式 异常", l);
            throw l;
        }
        catch (Exception e)
        {
            logger.error("进入订单界面获取可用支付方式 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_, e.getMessage(), "getPayment");
        }
        return (Map<String, Object>) payInfo.get("payment");
    }
}
