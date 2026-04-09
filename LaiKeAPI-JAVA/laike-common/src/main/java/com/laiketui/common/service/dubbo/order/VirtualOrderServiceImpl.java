package com.laiketui.common.service.dubbo.order;

import com.alibaba.fastjson2.*;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Maps;
import com.laiketui.common.annotation.HandlerOrderType;
import com.laiketui.common.api.*;
import com.laiketui.common.api.order.OrderDubboService;
import com.laiketui.common.mapper.*;
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
import com.laiketui.domain.coupon.CouponModal;
import com.laiketui.domain.log.RecordModel;
import com.laiketui.domain.mch.*;
import com.laiketui.domain.message.MessageLoggingModal;
import com.laiketui.domain.order.*;
import com.laiketui.domain.product.ProductListModel;
import com.laiketui.domain.product.StockModel;
import com.laiketui.domain.supplier.SupplierModel;
import com.laiketui.domain.user.User;
import com.laiketui.domain.user.UserAddress;
import com.laiketui.domain.virtual.WriteRecordModel;
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

import static com.laiketui.core.lktconst.DictionaryConst.OrdersStatus.ORDERS_R_STATUS_DISPATCHED;
import static com.laiketui.core.lktconst.ErrorCode.BizErrorCode.*;
import static com.laiketui.domain.order.OrderModel.*;


/**
 * 虚拟订单流程
 *
 * @author gp
 * 2023-08-14
 */
@HandlerOrderType(type = DictionaryConst.OrdersType.ORDERS_HEADER_VI)
@Service
public class VirtualOrderServiceImpl implements OrderDubboService
{

    private final Logger logger = LoggerFactory.getLogger(VirtualOrderServiceImpl.class);

    @Autowired
    private PubliceService            publiceService;
    @Autowired
    private RedisUtil                 redisUtil;
    @Autowired
    private UserMapper                userMapper;
    @Autowired
    private PaymentConfigModelMapper  paymentConfigModelMapper;
    @Autowired
    private PublicAddressService      commonAddressService;
    @Autowired
    private UserBaseMapper            userBaseMapper;
    @Autowired
    private PublicOrderService        publicOrderService;
    @Autowired
    private MchBrowseModelMapper      mchBrowseModelMapper;
    @Autowired
    private OrderDetailsModelMapper   orderDetailsModelMapper;
    @Autowired
    private OrderModelMapper          orderModelMapper;
    @Autowired
    private ConfiGureModelMapper      confiGureModelMapper;
    @Autowired
    private ProductListModelMapper    productListModelMapper;
    @Autowired
    private MchModelMapper            mchModelMapper;
    @Autowired
    private PublicStockService        publicStockService;
    @Autowired
    private MessageLoggingModalMapper messageLoggingModalMapper;
    @Autowired
    private PublicMchService          publicMchService;
    @Autowired
    private CouponModalMapper         couponModalMapper;
    @Autowired
    private PublicCouponService       publicCouponService;
    @Autowired
    private PublicSubtractionService  publicSubtractionService;
    @Autowired
    private FreightModelMapper        freightModelMapper;
    @Autowired
    private CartModelMapper           cartModelMapper;
    @Autowired
    private BuyAgainModalMapper       buyAgainModalMapper;
    @Autowired
    private UserFirstModalMapper      userFirstModalMapper;
    @Autowired
    private CouponOrderModalMapper    couponOrderModalMapper;
    @Autowired
    private MchStoreWriteModelMapper  mchStoreWriteModelMapper;
    @Autowired
    private MchStoreModelMapper       mchStoreModelMapper;
    @Autowired
    private RecordModelMapper         recordModelMapper;
    @Autowired
    private StockModelMapper          stockModelMapper;
    @Autowired
    private OrderConfigModalMapper    orderConfigModalMapper;
    @Autowired
    private ReturnOrderModelMapper    returnOrderModelMapper;
    @Autowired
    private ReturnGoodsModelMapper    returnGoodsModelMapper;
    @Autowired
    private ServiceAddressModelMapper serviceAddressModelMapper;
    @Autowired
    private PublicRefundService       publicRefundService;
    @Autowired
    private CustomerModelMapper       customerModelMapper;
    @Autowired
    private SupplierModelMapper       supplierModelMapper;
    @Autowired
    private WriteRecordModelMapper    writeRecordModelMapper;
    @Autowired
    private ReturnRecordModelMapper   returnRecordModelMapper;
    @Autowired
    private IntegralConfigModelMapper integralConfigModelMapper;

    @Autowired
    private PublicPaymentConfigService publicPaymentConfigService;

    @Override
    @Transactional
    public Map<String, Object> settlement(OrderVo vo) throws LaiKeAPIException
    {
        try
        {
            Map<String, Object> resultMap = new TreeMap<>();
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

            List<Map<String, Object>> productsListMap = publiceService.productsList(productList, vo.getCarts(), vo.getBuyType(), DictionaryConst.OrdersType.ORDERS_HEADER_VI);
            if (CollectionUtils.isEmpty(productsListMap))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_KCBZ, "库存不足2", "settlement");
            }
            Integer write_off_settings = 2;
            //处理是否需要线下核销，在下单页面是否需要选择门店
            for (Map<String, Object> map1 : productsListMap)
            {
                if (map1.containsKey("write_off_settings") && DataUtils.getStringVal(map1, "write_off_settings").equals(ProductListModel.WRITE_OFF_SETTINGS.offline.toString()))
                {
                    //需要线下核销
                    write_off_settings = 1;
                }
            }
            //按照店铺归类的商品、运费、商品总价等信息
            Map<String, Object> productsInfo = publiceService.settlementProductsInfoVI(productsListMap, vo.getStoreId(), vo.getProductType());
            //productsInfo.get("is_appointment");
            int address_status = MapUtils.getIntValue(productsInfo, "address_status");
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
            //无运费计算
            //productsInfo = publicOrderService.getFreight(productsFreight, productsListMap, userAddress, vo.getStoreId(), vo.getProductType());
            products = (List<Map<String, Object>>) productsInfo.get("products");
            //免运费
            for (Map<String, Object> mchProductsInfo : products)
            {
                //自提免运费
                mchProductsInfo.put("freight_price", BigDecimal.ZERO);
                BigDecimal productTotal = DataUtils.getBigDecimalVal(mchProductsInfo, "product_total");
                BigDecimal freightPrice = DataUtils.getBigDecimalVal(mchProductsInfo, "freight_price");
                mchProductsInfo.put("product_total", productTotal.subtract(freightPrice));

                //线下核销不可加入购物车，所以点击再次购买只会有一条数据
                if (write_off_settings == 1)
                {
                    List<Map<String, Object>> prodList = (List<Map<String, Object>>) mchProductsInfo.get("list");
                    Map<String, Object>       prodMap     = prodList.get(0);
                    Integer                   pid = MapUtils.getInteger(prodMap, "pid");
                    resultMap.put("productId",pid);
                }
            }
            Integer shopAddressId = vo.getShopAddressId();
            if (shopAddressId == null || shopAddressId == 0)
            {
                shopAddressId = 0;
            }
            else
            {
//                if (StringUtils.isEmpty(vo.getMchStoreWrite())) {
//                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERRCODE_QXZYYSJ, "请选择预约时间");
//                }
//                MchStoreWriteModel mchStoreWriteModel = mchStoreWriteModelMapper.selectByPrimaryKey(vo.getMchStoreWrite());
//                if (mchStoreWriteModel.getWrite_off_num() != 0 && mchStoreWriteModel.getWrite_off_num() <= mchStoreWriteModel.getOff_num()){
//                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERRCODE_DQYYSJYRM, "当前预约时间以约满,请重新选择");
//                }
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
                mdztInfo = publicMchService.settlement(vo.getStoreId(), mchId, "", shopAddressId, vo.getStoreType());
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
            Map<String, Object> paltCouponsMap = publicCouponService.settlementPlaformCoupons(vo.getStoreId(), userId, products, couponIds, vo.getCanshu());
            //未选择优惠卷会重新拼接优惠卷
            if (StringUtils.isNotEmpty(MapUtils.getString(paltCouponsMap, "couponIds")))
            {
                couponIds = MapUtils.getString(paltCouponsMap, "couponIds");
                platformDiscountIdList = couponIds.split(SplitUtils.DH);
                // 最后一调数据的键名
                platformDiscountTypePos = platformDiscountIdList.length - 1;
                // 平台优惠的健名
                platformDiscountPos = platformDiscountIdList.length - 2;
            }
            // 优惠类型 三种 ：coupon，substration，nodiscount：第一次进来的时候为 0｜'' 1，12,0,nodiscount
            discountType = platformDiscountIdList[platformDiscountTypePos];
            products = (List<Map<String, Object>>) paltCouponsMap.get("products");
            List<Map<String, Object>> couponList = (List<Map<String, Object>>) paltCouponsMap.get("list");
            if (couponList.size() > 0)
            {
                discountList = couponList;
            }

            //满减 是否打开了满减插件，打开了则进行满减,选择了满减则校验当前满减券是否满足条件
            Map<String, Object> autoSubtractionsMap = publicSubtractionService.autoSubtraction(vo.getStoreId(), products, subtractionId);
            if (autoSubtractionsMap.containsKey("is_subtraction"))
            {
                isSubtraction = DataUtils.getIntegerVal(autoSubtractionsMap, "is_subtraction");
                if (isSubtraction == 1)
                {
                    reduceMoney = DataUtils.getBigDecimalVal(autoSubtractionsMap, "reduce_money").setScale(2, BigDecimal.ROUND_DOWN);
                    reduceName = DataUtils.getStringVal(autoSubtractionsMap, "reduce_name_array");
                    products = (List<Map<String, Object>>) autoSubtractionsMap.get("products");
                    //可用的满减id集合
                    List<Map<String, Object>> autoSubtractionsList = (List<Map<String, Object>>) autoSubtractionsMap.get("subtraction_list");
                    //把所有可用的满减放到平台优惠券id集里面
                    discountList.addAll(autoSubtractionsList);
                }
            }

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
                                    ArrayList<Object> objects = new ArrayList<>();
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
            //需要线下核销的商品的门店显示
            if (write_off_settings.equals(ProductListModel.WRITE_OFF_SETTINGS.offline))
            {
                Integer pid = null;
                if (Objects.nonNull(MapUtils.getInteger(resultMap, "productId")))
                {
                    pid = MapUtils.getInteger(resultMap, "productId");
                }
                else
                {
                    if (productList.get(0).containsKey("pid"))
                    {
                        pid = Integer.parseInt(productList.get(0).get("pid").toString());

                    }
                    else
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误");
                    }
                }
                ProductListModel productListModel = productListModelMapper.selectProduct(pid);

                Map<String, Object> paraMap = new HashMap<>();
                paraMap.put("store_id", productListModel.getStore_id());
                paraMap.put("mch_id", productListModel.getMch_id());
                //禅道 252
                paraMap.put("lkt_mch_store_mch_id", productListModel.getMch_id());
                //先使用默认的一个门店，该门店选择适用商品设置中的第一个门店，如果为0，则使用默认的门店，当前端选择后更改门店
                if (vo.getShopAddressId() == null || vo.getShopAddressId() == 0)
                {
                    if (productListModel.getWrite_off_mch_ids().equals("0"))
                    {
                        paraMap.put("is_default", 1);
                    }
                    else
                    {
                        String[] split = productListModel.getWrite_off_mch_ids().split(SplitUtils.DH);
                        paraMap.put("mch_store_id", split[0]);
                    }
                }
                else
                {
                    paraMap.put("mch_store_id", vo.getShopAddressId());
                }
                paraMap.put("recycle", 0);
                List<Map<String, Object>> writeStore = new ArrayList<>();
                //当首次进入settlement页面时，随机选择一家核销门店且展示它的核销时间
                writeStore = mchStoreWriteModelMapper.selectDynamic(paraMap);
                //55984 商品所有的可核销门店都可选择，没有可预约时间的也可以选择，返回空时间列表
                if (writeStore == null || writeStore.size() < 1)
                {
                    if (paraMap.containsKey("mch_store_id"))
                    {
                        paraMap.put("id", MapUtils.getIntValue(paraMap, "mch_store_id"));
                    }
                    paraMap.put("limit", 1);
                    writeStore = mchStoreModelMapper.selectDynamic(paraMap);
                }
                //给日子排序
                Map<String, List<Map<String, Object>>> date = new TreeMap<>(new Comparator<String>()
                {
                    @Override
                    public int compare(String o1, String o2)
                    {
                        try
                        {
                            Date date1 = DateUtil.dateFormateToDate(o1, GloabConst.TimePattern.YMD);
                            Date date2 = DateUtil.dateFormateToDate(o2, GloabConst.TimePattern.YMD);
                            return date1.compareTo(date2);
                        }
                        catch (Exception e)
                        {
                            throw new IllegalArgumentException("Invalid date format", e);
                        }
                    }
                });
                //Map<String, List<Map<String, Object>>> HashMap = new TreeMap<>();
                List<String> ids = new ArrayList<>();

                for (Map<String, Object> originalMap : writeStore)
                {
                    String id = MapUtils.getString(originalMap, "id");
                    if (ids != null && ids.size() > 0 && !ids.contains(id))
                    {
                        Map<String, List<Map<String, Object>>> TempMap = new TreeMap<>();
                        date = TempMap;
                    }
                    ids.add(MapUtils.getString(originalMap, "id"));
                    // 提取原始数据
                    String start_time    = MapUtils.getString(originalMap, "start_time");
                    String end_time      = MapUtils.getString(originalMap, "end_time");
                    int    write_off_num = MapUtils.getInteger(originalMap, "write_off_num", 0); // 假设默认为0
                    String w_id          = MapUtils.getString(originalMap, "w_id");

                    // 格式化时间
                    String start_time_ymd = DateUtil.dateFormate(start_time, GloabConst.TimePattern.YMD);
                    String end_time_ymd   = DateUtil.dateFormate(end_time, GloabConst.TimePattern.YMD);
                    String start_time_hm  = DateUtil.dateFormate(start_time, GloabConst.TimePattern.HM);
                    String end_time_hm    = DateUtil.dateFormate(end_time, GloabConst.TimePattern.HM);

                    // 构造时间范围
                    String time_range = start_time_hm + SplitUtils.BL + end_time_hm;

                    // 获取日期范围内的所有日期
                    List<String> intervalDate = DateUtil.getIntervalDate(start_time_ymd, end_time_ymd);
                    // 遍历每个日期，并将formattedMap添加到对应的列表中

                    List<Map<String, Object>> list1;
                    int                       i = 0;
                    for (String s : intervalDate)
                    {
                        Map<String, Object> formattedMap = new TreeMap<>();
                        formattedMap.put("time_range", time_range);
                        //只用于时间段排序
                        formattedMap.put("sort", start_time_hm);
                        //只用于筛除过期的时间段
                        formattedMap.put("endTime", end_time_hm);
                        formattedMap.put("w_id", w_id);
                        int writeStatus = 0;
                        //已预约次数
                        String off_num = MapUtils.getString(originalMap, "off_num");//5,4
                        //已预约核销次数
                        int      ture_off_num = -1;
                        String[] split        = off_num.split(",");
                        ture_off_num = Integer.parseInt(split[i]);
                        /*if (vo.getMchStoreWriteTime() != null && vo.getMchStoreWriteTime().equals(intervalDate.get(i))) {
                            break;
                        }*/

                        //为1则是可预约
                        if (write_off_num == 0 || (MapUtils.getIntValue(originalMap, "write_off_num") > ture_off_num))
                        {
                            writeStatus = 1;
                        }
                        // 创建一个新的map来存储格式化后的数据和核销次数

                        formattedMap.put("write_status", writeStatus);
                        i++;
                        //s = DateUtil.dateFormate(s, GloabConst.TimePattern.MD2);
                        if (date.containsKey(s))
                        {
                            //list存放日期的所有时刻及其状态
                            list1 = date.get(s);
                            //当formattedMap中的endTime结束时间大于当前时间才加入集合中，时间等于今天需要特殊处理
                            if (s.equals(DateUtil.dateFormate(new Date(), GloabConst.TimePattern.YMD)))
                            {
                                if (DateUtil.dateCompare(end_time_hm, DateUtil.dateFormate(new Date(), GloabConst.TimePattern.HM), GloabConst.TimePattern.HM))
                                {
                                    list1.add(formattedMap);
                                }
                            }
                            else
                            {
                                list1.add(formattedMap);
                            }
                        }
                        else
                        {
                            list1 = new ArrayList<>();
                            if (!DateUtil.dateCompare2(s, DateUtil.dateFormate(new Date(), GloabConst.TimePattern.YMD)))
                            {
                                continue;
                            }
                            //如果日期等于今天则需要特殊处理
                            if (s.equals(DateUtil.dateFormate(new Date(), GloabConst.TimePattern.YMD)))
                            {
                                //日期为今天且技术时刻大于此刻才加入
                                if (DateUtil.dateCompare(end_time_hm, DateUtil.dateFormate(new Date(), GloabConst.TimePattern.HM), GloabConst.TimePattern.HM))
                                {
                                    list1.add(formattedMap);
                                }
                            }
                            else
                            {
                                list1.add(formattedMap);
                            }
                            date.put(s, list1);
                        }

                    }
                    originalMap.put("date", date);

                }
                Set<String> keys = date.keySet();
                for (String key : keys)
                {
                    Collections.sort(date.get(key), new Comparator<Map<String, Object>>()
                    {
                        @Override
                        public int compare(Map<String, Object> o1, Map<String, Object> o2)
                        {
                            return MapUtils.getInteger(o1, "sort").compareTo(MapUtils.getInteger(o2, "sort"));
                        }
                    });
                }
                if (writeStore != null && writeStore.size() > 0)
                {
                    Set<String> seenIds = new HashSet<>();
                    writeStore.removeIf(x -> !seenIds.add(x.get("id").toString()));

                    Object o = writeStore.get(0).get("date");
                    if (o instanceof Map)
                    {
                        Map<String, List<Map<String, Object>>> m         = (Map<String, List<Map<String, Object>>>) o;
                        List<Map<String, Object>>              valueList = new ArrayList<Map<String, Object>>();
                        Iterator<String>                       iter      = m.keySet().iterator();
                        while (iter.hasNext())
                        {
                            String              key     = iter.next();
                            Object              value   = (Object) m.get(key);
                            Map<String, Object> mapList = new TreeMap<String, Object>();
                            mapList.put("key", key);
                            mapList.put("value", value);
                            valueList.add(mapList);
                        }
                        writeStore.get(0).get("date");
                    }
                }
                resultMap.put("mchStoreList", writeStore);
            }
            //支付密码错误一天超过5此不允许再使用余额支付
            resultMap.put("enterless", enterless);
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
            //是否显示核销门店
            resultMap.put("address_status", address_status);

            resultMap.put("freight", yunfei);
            // 用户地址
            //resultMap.put("address", userAddress == null ? new UserAddress() : userAddress);
            // 是否有收货地址
            //resultMap.put("addemt", addemt);
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
            //暂定虚拟商品不是供应商商品
            resultMap.put("is_supplier_pro", true);
            resultMap.put("scoreDeductionValue",false);

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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> payment(OrderVo vo) throws LaiKeAPIException
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
            int storeId    = vo.getStoreId();
            int ProductNum = 0;
            // 用户id
            String userId = user.getUser_id();
            // 商品总价
            BigDecimal productsTotal = BigDecimal.ZERO;
            //  商品数组--------['pid'=>66,'cid'=>88]
            List<Map<String, Object>> products = new ArrayList<>();
            // 用户使用积分
            int allow = vo.getAllow();
            // 门店地址id
            Integer shopAddressId = vo.getShopAddressId();
            if (shopAddressId == null || shopAddressId == 0)
            {
                shopAddressId = 0;
                //当没有选择预约门店，则将商品可适用的核销门店加入
            }
            else
            {
                //判断预约时间是否已经约满了
                if (StringUtils.isEmpty(vo.getMchStoreWrite()) || StringUtils.isEmpty(vo.getShopAddressId()))
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERRCODE_QXZYYSJ, "请选择预约时间");
                }
                MchStoreWriteModel mchStoreWriteModel = mchStoreWriteModelMapper.selectByPrimaryKey(vo.getMchStoreWrite());
                if (mchStoreWriteModel == null)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSGSBZQ, vo.getsNo() + "预约参数不正确", "payment");
                }
                //todo sjw 更改
                /*if (mchStoreWriteModel.getWrite_off_num() != 0 && mchStoreWriteModel.getWrite_off_num() <= mchStoreWriteModel.getOff_num()){
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERRCODE_DQYYSJYRM, "当前预约时间以约满,请重新选择");
                }*/
            }
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
            products = publiceService.productsList(productList, vo.getCarts(), vo.getBuyType(), DictionaryConst.OrdersType.ORDERS_HEADER_VI);
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
            // 存储收货信息
            String mobile    = "";
            String name      = "";
            String sheng     = "";
            String shi       = "";
            String xian      = "";
            String addressXq = "";
            if (userAddress != null)
            {
                mobile = userAddress.getTel();
                name = userAddress.getName();
                sheng = userAddress.getSheng();
                shi = userAddress.getCity();
                xian = userAddress.getQuyu();
                addressXq = userAddress.getAddress();
            }
            //提前记录是否需要线下核销以及是否需要预约时间
            Integer write_off_settings = ProductListModel.WRITE_OFF_SETTINGS.offline;
            Integer isAppointment      = ProductListModel.IS_APPOINTMENT.noOpin;
            logger.info("商品信息：：：：：：{}",products);
            for (Map<String, Object> product : products)
            {
                if (product.containsKey("write_off_settings"))
                {
                    if (MapUtils.getInteger(product, "write_off_settings").equals(ProductListModel.WRITE_OFF_SETTINGS.dispenseWith))
                    {
                        write_off_settings = ProductListModel.WRITE_OFF_SETTINGS.dispenseWith;
                    }
                }
                if (product.containsKey("is_appointment"))
                {
                    if (MapUtils.getInteger(product, "is_appointment").equals(ProductListModel.IS_APPOINTMENT.isOpin))
                    {
                        isAppointment = ProductListModel.IS_APPOINTMENT.isOpin;
                        //如果有未支付且为预约订单则不能再次下单
                        HashMap<String, Object> param = new HashMap<>();
                        param.put("user_id", user.getUser_id());
                        param.put("r_status", DictionaryConst.OrdersStatus.ORDERS_R_STATUS_UNPAID);
                        List<OrderDetailsModel> orderDetailsModelList = orderDetailsModelMapper.selectOrder(param);
                    /*if (orderDetailsModelList.size()>0) {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_YWZFDXYYDD, "还有未支付的需预约的虚拟订单，请先支付或取消", "payment");
                    }*/
                    }
                }
            }
            // 5.列出商品数组-计算总价和优惠，拿商品运费ID
            Map<String, Object> productsInfo = publiceService.settlementProductsInfoVI(products, vo.getStoreId(), vo.getProductType());
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
//            productsInfo = publicOrderService.getFreight(productsFreight, products, userAddress, vo.getStoreId(), vo.getProductType());
            products = (List<Map<String, Object>>) productsInfo.get("products");

            yunfei = BigDecimal.ZERO;
            for (Map<String, Object> mchProductsInfo : products)
            {
                mchProductsInfo.put("freight_price", BigDecimal.ZERO);
                BigDecimal productTotal = DataUtils.getBigDecimalVal(mchProductsInfo, "product_total");
                BigDecimal freightPrice = DataUtils.getBigDecimalVal(mchProductsInfo, "freight_price");
                mchProductsInfo.put("product_total", productTotal.subtract(freightPrice));
                //线下核销不可加入购物车，所以点击再次购买只会有一条数据
                if (write_off_settings == 1)
                {
                    List<Map<String, Object>> prodList = (List<Map<String, Object>>) mchProductsInfo.get("list");
                    Map<String, Object>       prodMap     = prodList.get(0);
                    Integer                   pid = MapUtils.getInteger(prodMap, "pid");
                    resultMap.put("productId",pid);
                }
            }

            // 定义初始化数据
            int totalNum = 0;
            int discount = 1;
            // 自提信息店铺
            String otype             = DictionaryConst.OrdersType.ORDERS_HEADER_VI;
            int    orderStatus;
            int    shopStatus        = 1;
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
            //
            Integer Self_lifting = 3;
            if (write_off_settings == ProductListModel.WRITE_OFF_SETTINGS.dispenseWith)
            {
                //如果为虚拟商品无需核销订单则直接为已完成
                Self_lifting = 4;
            }
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

            //核销二维码生成、运费处理
            int                 mch_id  = DataUtils.getIntegerVal(products.get(0), "shop_id");
            Map<String, Object> shopMap = publicMchService.settlement(vo.getStoreId(), mch_id, "payment", shopAddressId, vo.getStoreType());
            sheng = DataUtils.getStringVal(shopMap, "sheng");
            shi = DataUtils.getStringVal(shopMap, "shi");
            xian = DataUtils.getStringVal(shopMap, "xian");
            addressXq = DataUtils.getStringVal(shopMap, "address");
            shopStatus = DataUtils.getIntegerVal(shopMap, "shop_status");
            extractionCode = DataUtils.getStringVal(shopMap, "extraction_code");
            extractionCodeImg = DataUtils.getStringVal(shopMap, "extraction_code_img");
            yunfei = BigDecimal.ZERO;
            mobile = user.getMobile();
            name = user.getUser_name();


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
            BigDecimal gradeRateAmount = DataUtils.ZERO_BIGDECIMAL;
            // 平台优惠金额
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
                ProductNum = onlyProducts.size();
                for (Map<String, Object> product : onlyProducts)
                {
                    int        pid           = DataUtils.getIntegerVal(product, "pid");
                    int        cid           = DataUtils.getIntegerVal(product, "cid");
                    int        num           = DataUtils.getIntegerVal(product, "num");
                    BigDecimal price         = DataUtils.getBigDecimalVal(product, "price");
                    String     product_title = DataUtils.getStringVal(product, "product_title");
                    String     unit          = DataUtils.getStringVal(product, "unit");
                    String     size          = DataUtils.getStringVal(product, "size");
                    String     coupon_id     = DataUtils.getStringVal(product, "coupon_id");
                    //如果没有优惠则为支付金额
                    BigDecimal amountAfterDiscountTmp = DataUtils.getBigDecimalVal(product, "amount_after_discount", total);

                    BigDecimal     freightPrice   = BigDecimal.ZERO;
                    ConfiGureModel confiGureModel = confiGureModelMapper.selectByPrimaryKey(cid);


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
                    orderDetailsModel.setMch_id(DataUtils.getIntegerVal(product, "mch_id"));
                    orderDetailsModel.setAfter_discount(amountAfterDiscountTmp);
                    orderDetailsModel.setStore_coupon_price(DataUtils.getBigDecimalVal(product, "shop_discount", BigDecimal.ZERO));
                    orderDetailsModel.setPlatform_coupon_price(DataUtils.getBigDecimalVal(product, "platform_discount", BigDecimal.ZERO));
                    //设置可核销次数、已核销次数、预约门店、预约时间
                    Integer write_off_num = Objects.nonNull(confiGureModel.getWrite_off_num()) ? confiGureModel.getWrite_off_num() : 0;
                    orderDetailsModel.setAfter_write_off_num(0);
                    if (write_off_settings == 2)
                    {
                        write_off_num = 0;
                    }
                    orderDetailsModel.setWrite_off_num(write_off_num);
                    if (isAppointment == 2)
                    {
                        //需要线下核销且预约
                        if (vo.getMchStoreWrite() == null || vo.getMchStoreWriteTime() == null)
                        {
                            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERRCODE_QXZYYSJ, "请选择预约时间", "payment");
                        }
                        MchStoreWriteModel mchStoreWriteModel = mchStoreWriteModelMapper.selectByPrimaryKey(vo.getMchStoreWrite());
                        if (mchStoreWriteModel == null)
                        {
                            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERRCODE_QXZYYSJ, "请选择预约时间", "payment");
                        }
                        // 格式化时间
                        String start_time_hm = DateUtil.dateFormate(mchStoreWriteModel.getStart_time(), GloabConst.TimePattern.HM);
                        String end_time_hm   = DateUtil.dateFormate(mchStoreWriteModel.getEnd_time(), GloabConst.TimePattern.HM);
                        //设置预约门店
                        orderDetailsModel.setMch_store_write_id(vo.getShopAddressId());
                        //2024-06-26 09:00-10:00
                        orderDetailsModel.setWrite_time(vo.getMchStoreWriteTime() + " " + start_time_hm + SplitUtils.HG + end_time_hm);
                        orderDetailsModel.setWrite_time_id(vo.getMchStoreWrite());
                        //虚拟商品需要线下核销且需要预约的商品扣除预约的天数
                        //需要预约的商品
                        //预约的时间   2024-07-10 05:00-06:00
                        String   writeTime = vo.getMchStoreWriteTime() + " " + start_time_hm + SplitUtils.HG + end_time_hm;
                        String[] s         = writeTime.split(" ", 2);
                        writeTime = s[0];
                        String  off_num       = mchStoreWriteModel.getOff_num();
                        write_off_num = mchStoreWriteModel.getWrite_off_num();
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
                                if (write_off_num != null && write_off_num != 0 && write_off_num.equals(Integer.parseInt(split[i])))
                                {
                                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERRCODE_DQYYSJYRM, "当前预约时间以约满,请重新选择", "pay");
                                }
                                split[i] = String.valueOf(Integer.parseInt(split[i]) + 1);
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

                    /*AddStockVo addStockVo = new AddStockVo();
                    addStockVo.setStoreId(vo.getStoreId());
                    addStockVo.setId(cid);
                    addStockVo.setPid(pid);
                    addStockVo.setAddNum(-num);
                    addStockVo.setText(userId + "生成订单所需" + num);
                    addStockVo.setMchId(Integer.valueOf(shopId));
                    publicStockService.addGoodsStock(addStockVo, null);*/
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
            Map<String, Object> configMap = publicOrderService.getOrderConfig(storeId, null, DictionaryConst.OrdersType.ORDERS_HEADER_GM);
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
            //虚拟商品需要核销的为3，无需核销的为4
            orderModel.setSelf_lifting(Self_lifting);
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
            orderModel.setPick_up_store(vo.getShopAddressId());
            orderModel.setOrderFailureTime(orderFailureDay);
            orderModel.setStore_write_time(vo.getMchStoreWriteTime());

            //货币信息[用户支付的货币信息]前提是商城默认币种不设置以后不允许修改
            orderModel.setCurrency_code(vo.getCurrency_code());
            orderModel.setCurrency_symbol(vo.getCurrency_symbol());
            orderModel.setExchange_rate(vo.getExchange_rate());

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
                String[] mchList = StringUtils.trim(mchId.toString(), SplitUtils.DH).split(SplitUtils.DH);
                for (String mchid : mchList)
                {
                    MessageLoggingModal messageLoggingSave = new MessageLoggingModal();
                    messageLoggingSave.setStore_id(storeId);
                    messageLoggingSave.setMch_id(Integer.valueOf(mchid));
                    messageLoggingSave.setType(MessageLoggingModal.Type.TYPE_ORDER_NEW);
                    messageLoggingSave.setTo_url(OrderDataUtils.getOrderRoute(vo.getType(), 0));
                    messageLoggingSave.setParameter(String.valueOf(r_o));
                    messageLoggingSave.setContent(String.format("您来新订单了，订单为%s，请及时处理", sNo));
                    messageLoggingSave.setAdd_date(new Date());
                    messageLoggingModalMapper.insertSelective(messageLoggingSave);
                }

                vo.setsNo(sNo);
                vo.setUser(user);

                // 店铺ID字符串
                String mch_ID = orderModel.getMch_id();
                // mch_id 和 shop_id 相同
                mch_ID = StringUtils.trim(mch_ID, SplitUtils.DH);
                // 店铺id字符串
                List<String> shopIds = Splitter.on(SplitUtils.DH).splitToList(mch_ID);
                //订单拆单，1件商品不拆
                /*if (ProductNum>1||shopIds.size()>1){
                    this.splitOrder(vo);
                }*/
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

    /**
     * 更新订单备注
     *
     * @param vo
     * @return
     */
    @Override
    public Map<String, Object> updateOrderRemark(OrderVo vo)
    {
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
            String     userId     = vo.getUserId();
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

            String type = sNo.substring(0, 2);//获取订单号前两位字母（类型）
            // mch_id 和 shop_id 相同
            mchId = StringUtils.trim(mchId, SplitUtils.DH);
            // 店铺id字符串
            List<String> shopIds = Splitter.on(SplitUtils.DH).splitToList(mchId);
            //单店铺如果所有商品的核销方式都一样则不需要拆单 -- 无需拆单的情况
            if (!CollectionUtils.isEmpty(shopIds) && shopIds.size() == 1)
            {
                //查询单个商品的价格，运费，数量
                List<Map<String, Object>> orderDetailsList = orderModelMapper.getOrderDetails(storeId, sNo, Integer.parseInt(shopIds.get(0)));
                //核销方式等于第一个的商品的核销方式，后面所有的商品如果有一个核销方式不同则需要拆单，否则则不拆
                Integer write_off_settings = DataUtils.getIntegerVal(orderDetailsList.get(0), "write_off_settings");
                //需要拆单标识
                boolean needSpilt = false;
                for (Map<String, Object> orderDetailMap : orderDetailsList)
                {
                    Integer tempWriteOffSettings = DataUtils.getIntegerVal(orderDetailMap, "write_off_settings");
                    if (!tempWriteOffSettings.equals(write_off_settings))
                    {
                        needSpilt = true;
                    }
                }
                //无需拆单则直接返回原订单不拆单
                if (!needSpilt)
                {
                    resultMap.put("order", orderModel);
                    List<OrderModel> orderModelslist = new ArrayList<>();
                    orderModelslist.add(orderModel);
                    resultMap.put("list", orderModelslist);
                    resultMap.put("flag", true);
                    return resultMap;
                }
            }
            //用于在拆单赋值时给orderModelTmp的id赋值null用于插入，orderModelTmp为splitOrder中的临时变量
            orderModel.setId(null);
            //每家店铺都按商品拆单--需要拆单的情况
            if (!CollectionUtils.isEmpty(shopIds))
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
                    couponList = publicCouponService.splitOrderForVI(storeId, userId, sNo);
                }

                List<OrderModel> splitOrders = new ArrayList<>();
                for (String shopId : shopIds)
                {

                    //查询到数据
                    BigDecimal orderNum = BigDecimal.ZERO;
                    //查询单个商品的价格，运费，数量
                    List<Map<String, Object>> orderDetailsList = orderModelMapper.getOrderDetails(storeId, sNo, Integer.parseInt(shopId));

                    if (!CollectionUtils.isEmpty(orderDetailsList))
                    {

                        //每个商品生成一个订单，也就是每个detail生成一个订单
                        for (Map<String, Object> orderDetailsMap : orderDetailsList)
                        {
                            //订单总价
                            BigDecimal orderPriceTotal = BigDecimal.ZERO;
                            //商品总价
                            BigDecimal spzPrice      = BigDecimal.ZERO;
                            BigDecimal totalFreight  = BigDecimal.ZERO;
                            OrderModel orderModelTmp = new OrderModel();
                            BeanUtils.copyProperties(orderModel, orderModelTmp);
                            // 生成订单号
                            String sNoTmp = publicOrderService.createOrderNo(type);
                            orderModelTmp.setMch_id(SplitUtils.DH + shopId + SplitUtils.DH);
                            orderModelTmp.setsNo(sNoTmp);
                            orderModelTmp.setP_sNo(sNo);
                            //重新生成核销码
                            String extractionCode    = publicMchService.extractionCode();
                            String extractionCodeImg = publicMchService.createQRCodeImg(extractionCode, storeId, vo.getStoreType());
                            orderModelTmp.setExtraction_code(extractionCode);
                            orderModelTmp.setExtraction_code_img(extractionCodeImg);

                            Integer order_details_id = DataUtils.getIntegerVal(orderDetailsMap, "id");
                            if (MapUtils.getIntValue(orderDetailsMap, "write_off_settings") == ProductListModel.WRITE_OFF_SETTINGS.offline)
                            {
                                orderModelTmp.setSelf_lifting(3);
                            }
                            else
                            {
                                orderModelTmp.setSelf_lifting(4);
                            }

                            //规格实际支付价格
                            BigDecimal payPrice     = new BigDecimal(MapUtils.getString(orderDetailsMap, "after_discount"));
                            Integer    num          = DataUtils.getIntegerVal(orderDetailsMap, "num");
                            BigDecimal productPrice = DataUtils.getBigDecimalVal(orderDetailsMap, "p_price", BigDecimal.ZERO);
                            BigDecimal freight      = DataUtils.getBigDecimalVal(orderDetailsMap, "freight");
                            //orderNum = orderNum.add(num);
                            //商品总价
                            BigDecimal price = productPrice.multiply(new BigDecimal(num.toString()));
                            spzPrice = spzPrice.add(price);
                            //总运费
                            totalFreight = totalFreight.add(freight);
                            //订单总价
                            orderPriceTotal = orderPriceTotal.add(payPrice);
                            int row = orderDetailsModelMapper.updateOrderDetailsParentOrderNo(storeId, sNoTmp, order_details_id);
                            if (row < 1)
                            {
                                logger.error("修改订单号失败！");
                                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CZSB, "操作失败", "splitOrder");
                            }
                            //特殊处理
                            orderModelTmp.setSpz_price(price);
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
                                        //判断优惠券是否是该家店铺的
                                        if (shopId.equals(mchIdTmp))
                                        {
                                            //获取店铺优惠券的id 如果没有则获取平台优惠券 2.如果有使用店铺优惠券则看是否使用了平台优惠券，使用了则用‘,’隔开
                                            String couponIdTmp = MapUtils.getString(couponInfoMap, "couponId");
                                            //如果没有使用店铺优惠券，则将平台的优惠券赋值
                                            if ("0".equals(couponIdTmp))
                                            {
                                                couponIdTmp = MapUtils.getString(couponInfoMap, "platformCouponId");
                                            }
                                            else
                                            {
                                                //获取平台优惠券拼接店铺优惠券 [店铺优惠券,平台优惠券]
                                                String ptCouponId = DataUtils.getStringVal(couponInfoMap, "platformCouponId");
                                                //查看是否使用了平台的优惠券，如果使用了就用，隔开  [店铺优惠券,平台优惠券]
                                                if (!"0".equals(ptCouponId))
                                                {
                                                    couponIdTmp += SplitUtils.DH + ptCouponId;
                                                }
                                            }
                                            orderModelTmp.setCoupon_id(couponIdTmp);
                                            spzPriceTmp = spzPrice.subtract(orderModelTmp.getCoupon_price());
                                            //平台优惠金额
                                            BigDecimal preferentialAmount = DataUtils.getBigDecimalVal(couponInfoMap, "platformPreferentialAmount");
                                            //当前规格平台优惠金额   当前商品价格/订单总价*平台优惠金额
                                            preferentialAmount = DataUtils.getBigDecimalVal(orderDetailsMap, "p_price").divide(new BigDecimal(orderModel.getSpz_price().toString()), 2, BigDecimal.ROUND_HALF_UP).multiply(preferentialAmount).setScale(2, RoundingMode.HALF_UP);
                                            orderModelTmp.setPreferential_amount(preferentialAmount);
                                            BigDecimal mahCouponAmount = DataUtils.getBigDecimalVal(couponInfoMap, "mch_coupon_amount");
                                            //当前规格店铺优惠金额   当前商品价格/店铺小计*店铺优惠金额
                                            if (DataUtils.getBigDecimalVal(couponInfoMap, "origin_price").compareTo(BigDecimal.ZERO) == 0)
                                            {
                                                couponInfoMap.put("origin_price", new BigDecimal("1"));
                                            }
                                            mahCouponAmount = DataUtils.getBigDecimalVal(orderDetailsMap, "p_price").divide(DataUtils.getBigDecimalVal(couponInfoMap, "origin_price"), 2, BigDecimal.ROUND_HALF_UP).multiply(mahCouponAmount).setScale(2, RoundingMode.HALF_UP);
                                            //总优惠金额
                                            orderModelTmp.setCoupon_price(preferentialAmount.add(mahCouponAmount));
                                            row = publicCouponService.couponWithOrder(storeId, userId, couponIdTmp, sNoTmp, "add");
                                            if (row <= 0)
                                            {
                                                logger.error("添加优惠券关联订单数据失败！");
                                                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TJYHQGLDDSJSB, "添加优惠券关联订单数据失败", "splitOrder");
                                            }

                                        }
                                    }
                                }
                            }

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
                            orderModelTmp.setNum(num);
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

                        }


                        /*int row = orderDetailsModelMapper.updateOrderDetailsStatus(storeId, sNoTmp, status);
                        if (row < 0) {
                            logger.error("订单拆分失败！");
                            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CZSB, "操作失败", "splitOrder");
                        }*/
                    }
                    else
                    {
                        logger.error("没有查询到订单信息！");
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CZSB, "操作失败", "splitOrder");
                    }
                }
                OrderModel orderModel1 = orderModelMapper.selectBySno(orderModel.getsNo());
                resultMap.put("order", orderModel1);
                if (splitOrders.size() > 0)
                {
                    orderModelMapper.insertList(splitOrders);
                    resultMap.put("list", splitOrders);
                    logger.info("保存拆单信息！");
                    //删掉未拆单之前的订单
                    MessageLoggingModal messageLoggingDel = new MessageLoggingModal();
                    messageLoggingDel.setStore_id(storeId);
                    messageLoggingDel.setParameter(orderModel1.getId() + "");
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
                //拆单后关闭并逻辑删除原主订单
                Map conditionMap = Maps.newHashMap();
                conditionMap.put("status", DictionaryConst.OrdersStatus.ORDERS_R_STATUS_CLOSE);
                conditionMap.put("recycle", DictionaryConst.ProductRecycle.RECOVERY);
                conditionMap.put("orderno", sNo);
                int row = orderModelMapper.updateByOrdernoDynamic(conditionMap);
                if (row < 1)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CZSB, "操作失败", "splitOrder");
                }
                //拆单后删除原优惠券订单关联数据
                couponOrderModalMapper.delCouponOrder(storeId, sNo);
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

    /**
     * 申请售后
     * 【php order.ReturnData】
     *
     * @param vo   -
     * @param file -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/4 15:21
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
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
                //优化 35523 极速退款 --先只处理GM订单
                //如果详单商品未发货可以进行极速退款
                //判断详单是否已经发货
                if (orderModel.getStatus().equals(DictionaryConst.OrdersStatus.ORDERS_R_STATUS_CONSIGNMENT) &&
                        orderModel.getOtype().equals(DictionaryConst.OrdersType.ORDERS_HEADER_GM))
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
                        logger.debug("订单详情id{} 已达到申请限制", detailId);
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YDDSHSQXZ, "已达到售后申请限制");
                    }
                }
                resultMap.put("write_off_num", orderDetailsModel.getWrite_off_num());
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

    /**
     * 虚拟商品退货
     *
     * @param vo       -
     * @param imgUrl   - 图片url地址
     * @param tokenKey - 登录模块key
     * @return
     * @throws LaiKeAPIException
     */
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
                //优化 35523 极速退款 --先只处理GM订单
                //如果详单商品未发货可以进行极速退款
                //判断详单是否已经发货
                if (orderModel.getStatus().equals(DictionaryConst.OrdersStatus.ORDERS_R_STATUS_CONSIGNMENT) &&
                        orderModel.getOtype().equals(DictionaryConst.OrdersType.ORDERS_HEADER_GM))
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
                        logger.debug("订单详情id{} 已达到申请限制", detailId);
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YDDSHSQXZ, "已达到售后申请限制");
                    }
                }
                resultMap.put("write_off_num", orderDetailsModel.getWrite_off_num());
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

    /**
     * 订单列表
     *
     * @param vo
     * @return
     */
    @Override
    public Map<String, Object> orderList(OrderVo vo)
    {
        try
        {
            User         user          = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            List<String> orderTypeList = new ArrayList<>();
            orderTypeList.add(DictionaryConst.OrdersType.ORDERS_HEADER_VI);
            vo.setOrderTypeList(orderTypeList);
            vo.setOrderType(DictionaryConst.OrdersType.ORDERS_HEADER_VI);
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

    /**
     * 提醒发货
     *
     * @param vo
     * @return
     */
    @Override
    public Map<String, Object> remindDelivery(OrderVo vo)
    {
        return null;
    }

    /**
     * 取消订单
     *
     * @param vo
     * @return
     */
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

                        /*AddStockVo addStockVo = new AddStockVo();
                        addStockVo.setStoreId(vo.getStoreId());
                        addStockVo.setId(Integer.valueOf(attributeId));
                        addStockVo.setPid(pid);
                        addStockVo.setAddNum(goodsNum);
                        addStockVo.setText(userId + "取消订单，返还" + goodsNum);
                        addStockVo.setMchId(Integer.valueOf(vo.getMchId()));
                        publicStockService.addGoodsStock(addStockVo, userId);*/

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

    /**
     * 加载更多订单
     *
     * @param vo
     * @return
     */
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

    /**
     * 删除订单
     *
     * @param vo
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
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

    /**
     * 再次购买
     *
     * @param vo
     * @return
     */
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

    /**
     * 搜索
     *
     * @param vo
     * @return
     */
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

    /**
     * 删除购物车
     *
     * @param vo
     * @return
     */
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

    /**
     * 获取支付方式
     *
     * @param vo
     * @return
     */
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

    /**
     * 订单详情 移动端个人中心
     *
     * @param vo
     * @return
     */
    @Override
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
     * 查看物流
     *
     * @param vo
     * @return
     */
    @Override
    public Map<String, Object> showLogistics(OrderVo vo)
    {
        return null;
    }

    /**
     * 撤销申请
     *
     * @param storeId
     * @param id
     * @return
     */
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

    /**
     * 退货信息
     *
     * @param vo
     * @return
     */
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
            /*if (!vo.getType().equals(DictionaryConst.OrdersType.ORDERS_HEADER_GM)) {
                params.put("type", vo.getType());
            } else {
                ArrayList<String> typeList = new ArrayList<>();
                typeList.add(vo.getType());
                typeList.add(DictionaryConst.OrdersType.ORDERS_HEADER_PT);
                params.put("typeList", typeList);
            }*/
            params.put("type", DictionaryConst.OrdersType.ORDERS_HEADER_VI);
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

    /**
     * 订单详情/订单列表-确认收货
     *
     * @param orderVo
     * @return
     */
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

    /**
     * 订单点击退货后，进入的页面 return_method
     *
     * @param vo
     * @return
     */
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
                    /*//查看商品核销了几次，已经核销的次数不给退  总价 除以 未核销+已核销 乘以 未核销  应退款保留两位小数
                    Integer write_off_num=orderDetails.getWrite_off_num();
                    Integer after_write_off_num=orderDetails.getAfter_write_off_num();
                    refundPrice=refundPrice.divide(new BigDecimal(write_off_num+after_write_off_num),2, RoundingMode.HALF_UP).multiply(new BigDecimal(write_off_num)).setScale(2, RoundingMode.HALF_UP);*/
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

            //data.put("refund", refund);
            //虚拟商品只能仅退款
            data.put("refundAmt", true);
            //data.put("refundGoodsAmt", refundGoodsAmt);
            //data.put("refundGoods", refundGoods);

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

    /**
     * 获取收货地址和快递信息
     *
     * @param stroeId
     * @param productId
     * @return
     */
    @Override
    public Map<String, Object> seeSend(int stroeId, int productId)
    {
        return null;
    }

    /**
     * 快递回寄信息
     *
     * @param returnGoodsVo
     * @return
     */
    @Override
    public Map<String, Object> backSend(ReturnGoodsVo returnGoodsVo)
    {
        return null;
    }

    /**
     * 虚拟商品查看核销码
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
            if (status == ORDERS_R_STATUS_DISPATCHED || status == DictionaryConst.OrdersStatus.ORDERS_R_STATUS_TOBEVERIFIED)
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
                //如果二维码图片没有带？，则加上固定?a=2
                if (!extractionCodeImg.contains("?"))
                {
                    extractionCodeImg = extractionCodeImg + "?a=2";
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
                //虚拟商品查询待核销次数
                Map<String, Object> paramsToOrderDetail = new HashMap<>();
                paramsToOrderDetail.put("p_id", productId);
                paramsToOrderDetail.put("r_sNo", sNo);
                List<OrderDetailsModel> orderDetails      = orderDetailsModelMapper.selectByPidAndSno(paramsToOrderDetail);
                OrderDetailsModel       orderDetailsModel = orderDetails.get(0);


                Map productRetMap = new HashMap();
                productRetMap.put("p_id", productId);
                productRetMap.put("product_title", DataUtils.getStringVal(productMap, "p_name"));
                productRetMap.put("p_price", DataUtils.getBigDecimalVal(productMap, "p_price", BigDecimal.ZERO));
                productRetMap.put("num", MapUtils.getInteger(productMap, "num"));
                productRetMap.put("sid", MapUtils.getInteger(productMap, "sid"));
                productRetMap.put("size", DataUtils.getStringVal(productMap, "size"));
                productRetMap.put("img", img);
                //待核销次数
                productRetMap.put("write_off_num", MapUtils.getInteger(productMap, "write_off_num"));
                //是否核销过,没核销过的为1，核销过为2 ，当有退款记录的时候也需要为2
                ReturnOrderModel returnOrderModel = new ReturnOrderModel();
                returnOrderModel.setsNo(sNo);
                returnOrderModel.setP_id(orderDetailsModel.getId());
                List<ReturnOrderModel> select = returnOrderModelMapper.select(returnOrderModel);
                if (MapUtils.getInteger(productMap, "after_write_off_num") != 0 || select.size() > 0)
                {
                    productRetMap.put("is_write", 2);
                }
                else
                {
                    productRetMap.put("is_write", 1);
                }

                //为1时为待使用，为2时为已用完 ，当有退款行为时也显示为核销完
                productRetMap.put("status", MapUtils.getInteger(productMap, "write_off_num") != 0 ? 1 : 2);
                /*if (select.size()>0){
                    productRetMap.put("status",2);
                }*/
                //订单详情id
                productRetMap.put("dId", MapUtils.getInteger(productMap, "dId"));
                products.add(productRetMap);
            }
            Map data = Maps.newHashMap();
            data.put("status", status);
            data.put("extraction_code", extractionCode);
            data.put("extraction_code_img", ImgUploadUtils.getUrlPure(extractionCodeImg, true));
            data.put("por_list", products);
            data.put("z_price", zPrice);

            data.put("currency_symbol", DataUtils.getStringVal(orderInfo, "currency_symbol"));
            data.put("exchange_rate", DataUtils.getBigDecimalVal(orderInfo, "exchange_rate"));

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

    /**
     * 售后订单详情
     *
     * @param refundDetailsVo
     * @return
     */
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
            goodsInfo.put("status", 1);

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
            info.put("r_write_off_num", DataUtils.getIntegerVal(returnOrder, "r_write_off_num"));
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
            //虚拟商品退款核销次数
            info.put("r_write_off_num", returnOrder.get("r_write_off_num"));

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
            //查询所有商品
            productListModel.setCommodity_type(ProductListModel.COMMODITY_TYPE.virtual);

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

    /**
     * 售后确认收货
     *
     * @param vo@return
     */
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
     * 撤销售后申请
     *
     * @param params
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
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

    /**
     * 进入订单界面获取可用支付方式
     *
     * @param vo
     * @return
     */
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
