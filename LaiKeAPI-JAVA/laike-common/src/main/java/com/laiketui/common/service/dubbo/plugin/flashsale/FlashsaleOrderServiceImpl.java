package com.laiketui.common.service.dubbo.plugin.flashsale;

import com.alibaba.fastjson2.*;
import com.google.common.collect.Maps;
import com.laiketui.common.annotation.HandlerOrderType;
import com.laiketui.common.api.*;
import com.laiketui.common.api.order.OrderDubboService;
import com.laiketui.common.api.plugin.member.PubliceMemberService;
import com.laiketui.common.mapper.*;
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
import com.laiketui.core.utils.tool.StringUtils;
import com.laiketui.domain.config.ConfiGureModel;
import com.laiketui.domain.distribution.FreightModel;
import com.laiketui.domain.flashsale.FlashsaleActivityModel;
import com.laiketui.domain.flashsale.FlashsaleAddGoodsModel;
import com.laiketui.domain.flashsale.FlashsaleRecordModel;
import com.laiketui.domain.mch.MchBrowseModel;
import com.laiketui.domain.mch.MchModel;
import com.laiketui.domain.message.MessageLoggingModal;
import com.laiketui.domain.order.OrderDetailsModel;
import com.laiketui.domain.order.OrderModel;
import com.laiketui.domain.product.ProductListModel;
import com.laiketui.domain.product.StockModel;
import com.laiketui.domain.user.User;
import com.laiketui.domain.user.UserAddress;
import com.laiketui.domain.vo.OrderVo;
import com.laiketui.domain.vo.goods.AddStockVo;
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
import java.math.RoundingMode;
import java.net.URLDecoder;
import java.util.*;

import static com.laiketui.domain.order.OrderModel.ORDER_CLOSE;

/**
 * 限时折扣订单流程
 *
 * @author gp
 * @date 2024/01/17
 */
@HandlerOrderType(type = DictionaryConst.OrdersType.ORDERS_HEADER_FS)
@Service
public class FlashsaleOrderServiceImpl implements OrderDubboService {
    private final Logger logger = LoggerFactory.getLogger(FlashsaleOrderServiceImpl.class);

    @Autowired
    private PubliceService publiceService;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private PaymentConfigModelMapper paymentConfigModelMapper;
    @Autowired
    private PublicAddressService commonAddressService;
    @Autowired
    private PublicOrderService publicOrderService;
    @Autowired
    private PublicMchService publicMchService;
    @Autowired
    private PublicCouponService publicCouponService;
    @Autowired
    private PublicSubtractionService publicSubtractionService;
    @Autowired
    private MchBrowseModelMapper mchBrowseModelMapper;
    @Autowired
    private OrderDetailsModelMapper orderDetailsModelMapper;
    @Autowired
    private OrderModelMapper orderModelMapper;
    @Autowired
    private CartModelMapper cartModelMapper;
    @Autowired
    private BuyAgainModalMapper buyAgainModalMapper;
    @Autowired
    private ConfiGureModelMapper confiGureModelMapper;
    @Autowired
    private ProductListModelMapper productListModelMapper;
    @Autowired
    private StockModelMapper stockModelMapper;
    @Autowired
    private UserFirstModalMapper userFirstModalMapper;
    @Autowired
    private ReturnOrderModelMapper returnOrderModelMapper;

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
    private FreightModelMapper freightModelMapper;

    @Autowired
    private AdminCgModelMapper adminCgModelMapper;

    @Autowired
    private FlashsaleActivityModelMapper flashsaleActivityModelMapper;

    @Autowired
    private FlashsaleAddGoodsModelMapper flashsaleAddGoodsModelMapper;

    @Autowired
    private FlashsaleRecordModelMapper flashsaleRecordModelMapper;

    @Autowired
    private PublicPaymentConfigService publicPaymentConfigService;

    @Override
    @Transactional
    public Map<String, Object> settlement(OrderVo vo) throws LaiKeAPIException {
        try {
            logger.debug("订单信息：{}", JSON.toJSONString(vo));
            // 获取用户信息 直接从redis 里面取 余额、收货信息
            User user;
            if (vo.getUser() != null) {
                user = vo.getUser();
            } else {
                user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
                user = userMapper.selectByPrimaryKey(user.getId());
            }
            String userId = user.getUser_id();

            String paypswd = user.getPassword();
            int wrongtimes = user.getLogin_num() == null ? 0 : user.getLogin_num();
            Date verificationtime = new Date();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(verificationtime);
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            verificationtime = calendar.getTime();
            Date now = new Date();
            //是否可以使用余额支付 默认false 表示不可以
            boolean enterless = false;
            if (wrongtimes == GloabConst.LktConfig.LOGIN_AGAIN_MAX) {
                if (!now.before(verificationtime)) {
                    user.setLogin_num(0);
                    userMapper.updateByPrimaryKey(user);
                    enterless = true;
                }
            } else {
                enterless = true;
            }

            //是否有支付密码标记 0无;1有
            int passwordStatus = DictionaryConst.WhetherMaven.WHETHER_OK;
            if (StringUtils.isEmpty(paypswd)) {
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
            if (vo.getAddressId() != null && vo.getAddressId() != 0 && vo.getAddressId() != -1) {
                map.put("address_id", vo.getAddressId());
            }

            UserAddress userAddress = commonAddressService.findAddress(vo.getStoreId(), user.getUser_id(), vo.getAddressId());
            // 收货地址状态
            int addemt = userAddress == null ? 1 : 0;

            // 获取产品类型
            // [{"pid":"979"},{"cid":"5648"},{"num":1},{"sec_id":"6"}--秒杀id,{}] 运费、商品总价、
            //  cart_id: 3010,3009
            List<Map<String, Object>> productList = null;
            try {
                productList = JSON.parseObject(vo.getProductsInfo(), new TypeReference<List<Map<String, Object>>>() {
                });
            } catch (JSONException j) {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSGSBZQ, vo.getProductsInfo() + ">参数格式不正确", "immediatelyCart");
            }
            List<Map<String, Object>> productsListMap = publiceService.productsList(productList, vo.getCarts(), vo.getBuyType(), DictionaryConst.OrdersType.ORDERS_HEADER_GM);
            if (CollectionUtils.isEmpty(productsListMap)) {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_KCBZ, "库存不足2", "settlement");
            }

            //订单中有供应商商品则不支持自提
            //不计算最优优惠卷
            boolean isZiti = true;
            for (Map<String, Object> productsList : productsListMap) {
                Integer pid = MapUtils.getInteger(productsList, "pid");
                ProductListModel productListModel = productListModelMapper.selectByPrimaryKey(pid);
                if (productListModel != null && StringUtils.isNotEmpty(productListModel.getGongyingshang())) {
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
            yunfei = DataUtils.getBigDecimalVal(productsInfo, "yunfei");
            //获取限时折扣
            FlashsaleActivityModel flashsaleActivityModel = flashsaleActivityModelMapper.selectByPrimaryKey(vo.getMainId());
            BigDecimal discount = BigDecimal.ZERO;
            discount = flashsaleActivityModel.getDiscount();
            if (flashsaleActivityModel == null) {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SPBCZ, "商品不存在");
            }
            //修改商品价格
            for (Map<String, Object> productMap : products) {
                List<Map<String, Object>> list = (List<Map<String, Object>>) productMap.get("list");
                Map<String, Object> objectMap = list.get(0);
                String price1 = MapUtils.getString(objectMap, "price");
                BigDecimal price = new BigDecimal(price1)
                        .multiply(discount.divide(new BigDecimal(10))).setScale(2, BigDecimal.ROUND_HALF_UP);
                objectMap.put("price", price.toString());
                if (BigDecimal.ZERO.compareTo(orderProductsTotal) >= 0) {
                    orderProductsTotal = BigDecimal.ZERO;
                } else {
                    orderProductsTotal = price.multiply(new BigDecimal(MapUtils.getString(objectMap, "num")).setScale(2, BigDecimal.ROUND_HALF_UP));
                }
            }
            Map<String, Object> resultMap = new HashMap<>();
            // 自提标记 1为自提
            resultMap.put("shop_status", 0);
            //支付密码错误一天超过5此不允许再使用余额支付
            resultMap.put("enterless", enterless);
            // 支付方式信息状态
            resultMap.putAll(payInfo);
            // 商品列表 里面包含了店铺优惠券列表
            resultMap.put("products", products);
            // 是否分销
            resultMap.put("is_distribution", vo.getIsDistribution());
            // 密码状态
            resultMap.put("password_status", passwordStatus);
            //加购价格
            BigDecimal addGoodsPrice =  BigDecimal.ZERO;
            if (StringUtils.isNotEmpty(vo.getAdd_good())){
                addGoodsPrice = calcAddGoodsPrice(vo.getAdd_good());
            }
            // 商品总价 = 商品价格+加购价格
            resultMap.put("products_total", orderProductsTotal.add(addGoodsPrice));
            // 用户余额
            resultMap.put("user_money", user.getMoney());
            // 实际支付金额
            resultMap.put("total", orderProductsTotal.add(yunfei).add(addGoodsPrice));
            resultMap.put("freight", yunfei);
            // 用户地址
            resultMap.put("address", userAddress == null ? new UserAddress() : userAddress);
            // 是否有收货地址
            resultMap.put("addemt", addemt);
            // 平台优惠
            resultMap.put("preferential_amount", 0);
            // 会员等级折扣
            resultMap.put("grade_rate", gradeRate);
            //  优惠
            resultMap.put("discount", discount);
            //  状态 固定写成1了 ？
            resultMap.put("status", "1");
            //截止时间秒级时间戳
            resultMap.put("remainingTime", flashsaleActivityModel.getEndtime().getTime());
            return resultMap;
        } catch (LaiKeAPIException e) {
            logger.error("结算订单自定义 异常", e);
            throw e;
        } catch (Exception e) {
            logger.error("结算订单 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "settlement");
        }

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> payment(OrderVo vo) {
        logger.debug("下单信息：{}", JSON.toJSONString(vo));
        Map<String, Object> resultMap = new HashMap<>();
        try {
            // 1.数据准备
            // 获取用户信息 直接从redis 里面取 余额、收货信息
            User user;
            if (vo.getUser() != null) {
                user = vo.getUser();
            } else {
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
            if (vo.getShopAddressId() == null) {
                vo.setShopAddressId(0);
            }
            int shopAddressId = vo.getShopAddressId();
            // 订单备注
            String remarksJson = vo.getRemarks();
            JSONArray remarkJsonarr = null;
            if (!StringUtils.isEmpty(remarksJson)) {
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
            try {
                productList = JSON.parseObject(vo.getProductsInfo(), new TypeReference<List<Map<String, Object>>>() {
                });
            } catch (JSONException j) {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSGSBZQ, vo.getProductsInfo() + ">参数格式不正确", "payment");
            }
            products = publiceService.productsList(productList, vo.getCarts(), vo.getBuyType(), DictionaryConst.OrdersType.ORDERS_HEADER_GM);
            if (CollectionUtils.isEmpty(products)) {
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
            if (Objects.isNull(userAddress.getId()) && shopAddressId == 0) {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QXZSHDZ, "请填写至少一个收货地址", "payment");
            }
            // 存储收货信息
            String mobile = userAddress.getTel();
            String name = userAddress.getName();
            String sheng = userAddress.getSheng();
            String shi = userAddress.getCity();
            String xian = userAddress.getQuyu();
            String addressXq = userAddress.getAddress();
            //获取限时折扣值
            FlashsaleActivityModel flashsaleActivityModel = flashsaleActivityModelMapper.selectByPrimaryKey(vo.getMainId());
            if (flashsaleActivityModel == null) {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SPBCZ, "商品不存在", "payment");
            }
            if (DateUtil.dateCompare(new Date(), flashsaleActivityModel.getEndtime())) {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_XSZKSJYJS, "限时折扣时间已结束", "payment");
            }
            BigDecimal flashsaleDiscount = flashsaleActivityModel.getDiscount();
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


            //计算店铺运费、总订单运费
            BigDecimal yunfei = BigDecimal.ZERO;
            productsInfo = publicOrderService.getFreight(productsFreight, products, userAddress, vo.getStoreId(), vo.getProductType());
            products = (List<Map<String, Object>>) productsInfo.get("products");
            //
            if (vo.getShopAddressId() != 0) {
                yunfei = BigDecimal.ZERO;
                for (Map<String, Object> mchProductsInfo : products) {
                    mchProductsInfo.put("freight_price", BigDecimal.ZERO);
                    BigDecimal productTotal = DataUtils.getBigDecimalVal(mchProductsInfo, "product_total");
                    BigDecimal freightPrice = DataUtils.getBigDecimalVal(mchProductsInfo, "freight_price");
                    mchProductsInfo.put("product_total", productTotal.subtract(freightPrice));
                }
            } else {
                yunfei = DataUtils.getBigDecimalVal(productsInfo, "yunfei");
            }

            // 定义初始化数据
            int totalNum = 0;
            int discount = 1;
            // 自提信息店铺
            String otype = DictionaryConst.OrdersType.ORDERS_HEADER_FS;
            int orderStatus;
            int shopStatus = 0;
            String extractionCode = "";
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
            if (vo.getGradeLevel() != 0 && (DataUtils.equalBigDecimalZero(yunfei))) {
                orderStatus = 1;
            } else {
                //待付款
                orderStatus = 0;
            }

            // 生成订单号
            String sNo = publicOrderService.createOrderNo(vo.getType());
            // 生成支付订单号
            String realSno = publicOrderService.createOrderNo(vo.getType());
            StringBuilder mchId = new StringBuilder();
            List<Integer> mchIdList = new ArrayList<>();
            /**
             * 店铺订单备注
             */
            Map<String, String> mchRemarks = new HashMap<>(16);
            // 是否有备注标记
            boolean remarksStatus = false;
            total = yunfei;
            int pos = 0;
            //会员优惠总价
            BigDecimal preferential = BigDecimal.ZERO;
            FlashsaleRecordModel flashsaleRecordModel;
            for (Map<String, Object> mchProduct : products) {
                String shopId = String.valueOf(mchProduct.get("shop_id"));
                mchId.append(shopId).append(SplitUtils.DH);
                if (remarkJsonarr != null) {
                    String tmpDesc = remarkJsonarr.getString(pos++);
                    if (!StringUtils.isEmpty(tmpDesc)) {
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
                for (Map<String, Object> product : onlyProducts) {
                    int pid = DataUtils.getIntegerVal(product, "pid");
                    //根据id获取运费信息
                    //根据id查询商品表  新增不配送区域判断
                    ProductListModel productListModel = productListModelMapper.selectByPrimaryKey(pid);
                    //自提订单不需要判断地址
                    if (vo.getShopAddressId() == 0) {
                        FreightModel freightModel = freightModelMapper.selectByPrimaryKey(productListModel.getFreight());
                        if (freightModel != null) {
                            //用户地址 省-市-区对应运费模板里的格式
                            String address = userAddress.getSheng() + "-" + userAddress.getCity() + "-" + userAddress.getQuyu();
                            //不配送区域参数列表
                            String bpsRule = URLDecoder.decode(freightModel.getNo_delivery(), CharEncoding.UTF_8);
                            JSONArray objects = JSONArray.parseArray(bpsRule);
                            if (objects.contains(address)) {
                                logger.debug("地址超出配送范围");
                                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DZCCPSFW, "地址超出配送范围", "payment");
                            }
                        }
                    }
                    int cid = DataUtils.getIntegerVal(product, "cid");
                    int num = DataUtils.getIntegerVal(product, "num");
                    BigDecimal price = DataUtils.getBigDecimalVal(product, "price");
                    String product_title = DataUtils.getStringVal(product, "product_title");
                    String unit = DataUtils.getStringVal(product, "unit");
                    String size = DataUtils.getStringVal(product, "size");
                    String coupon_id = DataUtils.getStringVal(product, "coupon_id");
                    if (flashsaleDiscount == null || flashsaleDiscount.compareTo(BigDecimal.ZERO) <= 0) {
                        flashsaleDiscount = BigDecimal.TEN;
                    }
                    //商品总价
                    productsTotal = productsTotal.add(price.multiply(new BigDecimal(num)).setScale(2, BigDecimal.ROUND_HALF_UP));
                    price = price.multiply(flashsaleDiscount.divide(BigDecimal.TEN)).setScale(2, BigDecimal.ROUND_HALF_UP);
                    //如果没有优惠则为支付金额
                    BigDecimal amountAfterDiscountTmp = price.multiply(new BigDecimal(num)).setScale(2, BigDecimal.ROUND_HALF_UP);
                    total = total.add(amountAfterDiscountTmp);
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
                    int beres = orderDetailsModelMapper.insertSelective(orderDetailsModel);
                    // 如果添加失败
                    if (beres < 1) {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_XDSBQSHZS, "下单失败,请稍后再试", "payment");
                    }
                    totalNum += num;
                    AddStockVo addStockVo = new AddStockVo();
                    addStockVo.setStoreId(vo.getStoreId());
                    addStockVo.setId(cid);
                    addStockVo.setPid(pid);
                    addStockVo.setAddNum(-num);
                    addStockVo.setText(userId + "生成订单所需" + num);
                    addStockVo.setMchId(Integer.valueOf(shopId));
                    publicStockService.addGoodsStock(addStockVo, null);
                    flashsaleRecordModel = new FlashsaleRecordModel();
                    flashsaleRecordModel.setStoreId(storeId);
                    flashsaleRecordModel.setUserId(userId);
                    flashsaleRecordModel.setActivityId(vo.getMainId());
                    flashsaleRecordModel.setProId(pid);
                    flashsaleRecordModel.setAttrId(cid);
                    flashsaleRecordModel.setPrice(price);
                    flashsaleRecordModel.setSecId(null);
                    flashsaleRecordModel.setNum(num);
                    flashsaleRecordModel.setsNo(sNo);
                    flashsaleRecordModel.setIsDelete(DictionaryConst.WhetherMaven.WHETHER_NO);
                    flashsaleRecordModel.setAddTime(new Date());
                    beres = flashsaleRecordModelMapper.insertSelective(flashsaleRecordModel);
                    if (beres < 1) {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_XDSBQSHZS, "下单失败,请稍后再试", "payment");
                    }
                }
            }
            //加购商品处理
            if (vo.getAdd_good() != null && !vo.getAdd_good().equals("undefined")) {
                List<Map<String, Object>> addGoodsList = convertToList(vo.getAdd_good());
                for (Map<String, Object> addGoodsMap : addGoodsList) {
                    int id = MapUtils.getIntValue(addGoodsMap, "id");
                    int num = MapUtils.getIntValue(addGoodsMap, "num");

                    FlashsaleAddGoodsModel flashsaleAddGoodsModel = flashsaleAddGoodsModelMapper.selectByPrimaryKey(id);
                    if (Objects.isNull(flashsaleAddGoodsModel)){
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SPBCZ, "商品不存在", "settlement");
                    }
                    ProductListModel productListModel = productListModelMapper.selectByPrimaryKey(flashsaleAddGoodsModel.getGoodsId());
                    if (productListModel == null) {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SPBCZ, "商品不存在", "payment");
                    }
                    ConfiGureModel confiGureModel = confiGureModelMapper.selectByPrimaryKey(flashsaleAddGoodsModel.getAttrId());

                    Integer pId = productListModel.getId();
                    Integer cId = confiGureModel.getId();
                    BigDecimal price = flashsaleAddGoodsModel.getPrice();
                    BigDecimal oldPrice = BigDecimal.valueOf(MapUtils.getDouble(addGoodsMap, "price"));

                    if (oldPrice.compareTo(price) != 0)
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_JGSPBD,"抱歉，获取商品数据失效，请重新下单！","payment");
                    }
                    OrderDetailsModel orderDetailsModel = new OrderDetailsModel();
                    orderDetailsModel.setStore_id(storeId);
                    orderDetailsModel.setUser_id(userId);
                    orderDetailsModel.setP_id(pId);
                    orderDetailsModel.setP_name(productListModel.getProduct_title());
                    orderDetailsModel.setP_price(price);
                    orderDetailsModel.setNum(num);
                    orderDetailsModel.setIs_addp(DictionaryConst.WhetherMaven.WHETHER_OK);
                    orderDetailsModel.setUnit(confiGureModel.getUnit());
                    orderDetailsModel.setR_sNo(sNo);
                    orderDetailsModel.setAdd_time(new Date());
                    orderDetailsModel.setR_status(orderStatus);
                    orderDetailsModel.setSize(GoodsDataUtils.getProductSkuValue(confiGureModel.getAttribute()));
                    orderDetailsModel.setSid(cId + "");
                    orderDetailsModel.setFreight(BigDecimal.ZERO);
                    orderDetailsModel.setSettlement_type(0);
                    orderDetailsModel.setCoupon_id("");
                    orderDetailsModel.setRecycle(0);
                    orderDetailsModel.setAfter_discount(price);
                    int beres = orderDetailsModelMapper.insertSelective(orderDetailsModel);
                    // 如果添加失败
                    if (beres < 1) {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_XDSBQSHZS, "下单失败,请稍后再试", "payment");
                    }
                    BigDecimal addGoodsPrice = price.multiply(new BigDecimal(num));
                    productsTotal = productsTotal.add(addGoodsPrice);
                    total = total.add(addGoodsPrice);
                    totalNum += num;

                    AddStockVo addStockVo = new AddStockVo();
                    addStockVo.setStoreId(vo.getStoreId());
                    addStockVo.setId(cId);
                    addStockVo.setPid(pId);
                    addStockVo.setAddNum(-num);
                    addStockVo.setText(userId + "生成订单所需" + num);
                    addStockVo.setMchId(productListModel.getMch_id());
                    publicStockService.addGoodsStock(addStockVo, null);

                    flashsaleRecordModel = new FlashsaleRecordModel();
                    flashsaleRecordModel.setStoreId(storeId);
                    flashsaleRecordModel.setUserId(userId);
                    flashsaleRecordModel.setActivityId(null);
                    flashsaleRecordModel.setProId(pId);
                    flashsaleRecordModel.setAttrId(cId);
                    flashsaleRecordModel.setPrice(addGoodsPrice);
                    flashsaleRecordModel.setNum(num);
                    flashsaleRecordModel.setSecId(flashsaleAddGoodsModel.getId());
                    flashsaleRecordModel.setsNo(sNo);
                    flashsaleRecordModel.setIsDelete(DictionaryConst.WhetherMaven.WHETHER_NO);
                    flashsaleRecordModel.setAddTime(new Date());
                    beres = flashsaleRecordModelMapper.insertSelective(flashsaleRecordModel);
                    if (beres < 1) {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_XDSBQSHZS, "下单失败,请稍后再试", "payment");
                    }
                }
     /*           FlashsaleAddGoodsModel flashsaleAddGoodsModel = flashsaleAddGoodsModelMapper.selectByPrimaryKey(vo.getAdd_good());
                if (flashsaleAddGoodsModel == null) {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SPBCZ, "商品不存在", "payment");
                }
                //查询商品信息
                ProductListModel productListModel = productListModelMapper.selectByPrimaryKey(flashsaleAddGoodsModel.getGoodsId());
                if (productListModel == null) {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SPBCZ, "商品不存在", "payment");
                }
                //查询规格信息
                ConfiGureModel confiGureModel = confiGureModelMapper.selectByPrimaryKey(flashsaleAddGoodsModel.getAttrId());
                Integer pId = productListModel.getId();
                Integer cId = confiGureModel.getId();
                BigDecimal price = flashsaleAddGoodsModel.getPrice();
                OrderDetailsModel orderDetailsModel = new OrderDetailsModel();
                orderDetailsModel.setStore_id(storeId);
                orderDetailsModel.setUser_id(userId);
                orderDetailsModel.setP_id(pId);
                orderDetailsModel.setP_name(productListModel.getProduct_title());
                orderDetailsModel.setP_price(price);
                orderDetailsModel.setNum(1);
                orderDetailsModel.setUnit(confiGureModel.getUnit());
                orderDetailsModel.setR_sNo(sNo);
                orderDetailsModel.setAdd_time(new Date());
                orderDetailsModel.setR_status(orderStatus);
                orderDetailsModel.setSize(GoodsDataUtils.getProductSkuValue(confiGureModel.getAttribute()));
                orderDetailsModel.setSid(cId + "");
                orderDetailsModel.setFreight(BigDecimal.ZERO);
                orderDetailsModel.setSettlement_type(0);
                orderDetailsModel.setCoupon_id("");
                orderDetailsModel.setRecycle(0);
                orderDetailsModel.setAfter_discount(price);
                int beres = orderDetailsModelMapper.insertSelective(orderDetailsModel);
                // 如果添加失败
                if (beres < 1) {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_XDSBQSHZS, "下单失败,请稍后再试", "payment");
                }
                productsTotal = productsTotal.add(price);
                total = total.add(price);
                totalNum += 1;
                AddStockVo addStockVo = new AddStockVo();
                addStockVo.setStoreId(vo.getStoreId());
                addStockVo.setId(cId);
                addStockVo.setPid(pId);
                addStockVo.setAddNum(-1);
                addStockVo.setText(userId + "生成订单所需" + 1);
                addStockVo.setMchId(productListModel.getMch_id());
                publicStockService.addGoodsStock(addStockVo, null);

                flashsaleRecordModel = new FlashsaleRecordModel();
                flashsaleRecordModel.setStoreId(storeId);
                flashsaleRecordModel.setUserId(userId);
                flashsaleRecordModel.setActivityId(null);
                flashsaleRecordModel.setProId(pId);
                flashsaleRecordModel.setAttrId(cId);
                flashsaleRecordModel.setPrice(price);
                flashsaleRecordModel.setNum(1);
                flashsaleRecordModel.setSecId(flashsaleAddGoodsModel.getId());
                flashsaleRecordModel.setsNo(sNo);
                flashsaleRecordModel.setIsDelete(DictionaryConst.WhetherMaven.WHETHER_NO);
                flashsaleRecordModel.setAddTime(new Date());
                beres = flashsaleRecordModelMapper.insertSelective(flashsaleRecordModel);
                if (beres < 1) {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_XDSBQSHZS, "下单失败,请稍后再试", "payment");
                }*/
            }
            String mainOrderRemarks = "";
            if (remarksStatus) {
                mainOrderRemarks = JSON.toJSONString(mchRemarks);
            }
            //最少支付金额0.01  禅道48455 可以支付0元
            if (BigDecimal.ZERO.compareTo(total) >= 0) {
                total = BigDecimal.ZERO;
            }
            //获取订单配置
            Map<String, Object> configMap = publicOrderService.getOrderConfig(storeId, 0, otype);
            //订单失效  - 秒
            Integer orderFailureDay = 3600;
            if (StringUtils.isNotEmpty(configMap)) {
                //未付款订单保留时间
                orderFailureDay = MapUtils.getInteger(configMap, "orderFailureDay");
                if (orderFailureDay == null || orderFailureDay < 0) {
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
            orderModel.setCoupon_id("");
            orderModel.setSubtraction_id(subtractionId);
            orderModel.setConsumer_money(allow);
            orderModel.setCoupon_activity_name(reduceNameArray);
            orderModel.setSpz_price(productsTotal);
            orderModel.setStatus(orderStatus);
            orderModel.setReduce_price(reduceMoney);
            orderModel.setSource(vo.getStoreType());
            orderModel.setOtype(otype);
            orderModel.setMch_id(mchId.toString());
            orderModel.setP_sNo("");
            orderModel.setBargain_id(0);
            orderModel.setComm_discount(new BigDecimal(discount));
            orderModel.setRemarks(mainOrderRemarks);
            orderModel.setReal_sno(realSno);
            orderModel.setSelf_lifting(shopStatus);
            orderModel.setExtraction_code(extractionCode);
            orderModel.setExtraction_code_img(extractionCodeImg);
            orderModel.setGrade_rate(gradeRate);
            orderModel.setGrade_fan(preferential);
            orderModel.setZ_freight(yunfei);
            orderModel.setOld_freight(yunfei);
            orderModel.setCoupon_price(couponMoney);
            orderModel.setPreferential_amount(BigDecimal.ZERO);
            orderModel.setSingle_store(shopAddressId);
            orderModel.setReadd(OrderModel.READD_UNREAD);
            orderModel.setZhekou(BigDecimal.ZERO);
            orderModel.setRecycle(0);
            orderModel.setPick_up_store(0);
            orderModel.setOrderFailureTime(orderFailureDay);
            orderModelMapper.insertSelective(orderModel);
            int r_o = orderModel.getId();
            if (r_o >= 0) {
                MessageLoggingModal messageLoggingSave = new MessageLoggingModal();
                messageLoggingSave.setStore_id(orderModel.getStore_id());
                messageLoggingSave.setMch_id(Integer.parseInt(StringUtils.trim(orderModel.getMch_id(), SplitUtils.DH)));
                messageLoggingSave.setType(MessageLoggingModal.Type.TYPE_ORDER_NEW);
                messageLoggingSave.setTo_url(OrderDataUtils.getOrderRoute(orderModel.getOtype(), 0));
                messageLoggingSave.setParameter(orderModel.getId() + "");
                messageLoggingSave.setContent(String.format("您来新订单了，订单为%s，请及时处理", orderModel.getsNo()));
                messageLoggingSave.setAdd_date(new Date());
                messageLoggingModalMapper.insertSelective(messageLoggingSave);
                //订单号
                resultMap.put("sNo", sNo);
                //订单总支付金额
                resultMap.put("total", total);
                //订单id
                resultMap.put("order_id", r_o);
                //下单时间
                resultMap.put("orderTime", DateUtil.dateFormate(orderModel.getAdd_time(), GloabConst.TimePattern.YMDHMS));
                return resultMap;
            } else {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_XDSBQSHZS, "下单失败,请稍后再试", "payment");
            }
        } catch (LaiKeAPIException l) {
            logger.error("下单 自定义异常", l);
            throw l;
        } catch (Exception e) {
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
    public Map<String, Object> updateOrderRemark(OrderVo vo) {
        return null;
    }

    /**
     * 拆分订单
     *
     * @param vo
     * @return
     */
    @Override
    public Map<String, Object> splitOrder(OrderVo vo) {
        return null;
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
    public Map<String, Object> returnData(ApplyReturnDataVo vo, MultipartFile file) throws LaiKeAPIException {
        return null;
    }

    /**
     * 订单列表
     *
     * @param vo
     * @return
     */
    @Override
    public Map<String, Object> orderList(OrderVo vo) {
        User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
        return publicOrderService.orderList(vo, user);
    }

    /**
     * 提醒发货
     *
     * @param vo
     * @return
     */
    @Override
    public Map<String, Object> remindDelivery(OrderVo vo) {
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
    public Map<String, Object> cancleOrder(OrderVo vo) {
        Map<String, Object> retMap = Maps.newHashMap();
        try {
            int storeId = vo.getStoreId();
            // 订单id
            int orderId = vo.getOrderId();
            // 根据微信id,查询用户id
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            String userId = user.getUser_id();
            // 根据订单id,查询订单列表(订单号)
            OrderModel order = new OrderModel();
            order.setStore_id(storeId);
            order.setUser_id(userId);
            order.setId(orderId);
            order = orderModelMapper.selectOne(order);
            if (order != null) {
                // 订单号
                String sNo = order.getsNo();
                if (!order.getStatus().equals(DictionaryConst.OrdersStatus.ORDERS_R_STATUS_UNPAID)) {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.OPERATION_FAILED, "订单不在待付款状态");
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
                params.put("cancel_method", DictionaryConst.CancelMethod.SELF);
                int row = orderModelMapper.updateOrderInfo(params);
                if (row < 1) {
                    logger.info(sNo + "修改订单状态失败:");
                }

                int row1 = orderDetailsModelMapper.updateOrderDetailsStatusAndSettlementStatus(storeId, sNo, ORDER_CLOSE, OrderDetailsModel.SETTLEMENT_TYPE_SETTLED);
                logger.info(sNo + "订单关闭:");
                if (row1 < 1) {
                    logger.info("sNo 修改订单状态失败！");
                }
                if (row1 >= 0 && row >= 0) {
                    OrderDetailsModel orderDetailsModel = new OrderDetailsModel();
                    orderDetailsModel.setStore_id(storeId);
                    orderDetailsModel.setR_sNo(sNo);
                    List<OrderDetailsModel> orderDetailsModels = orderDetailsModelMapper.select(orderDetailsModel);
                    for (OrderDetailsModel orderDetailsInfo : orderDetailsModels) {
                        int pid = orderDetailsInfo.getP_id();
                        int goodsNum = orderDetailsInfo.getNum();
                        String attributeId = orderDetailsInfo.getSid();
                        ConfiGureModel confiGureModel = new ConfiGureModel();
                        confiGureModel.setId(Integer.valueOf(attributeId));
                        confiGureModel.setPid(pid);

                        confiGureModel = confiGureModelMapper.selectOne(confiGureModel);
                        int totalNum = confiGureModel.getNum();

                        row = productListModelMapper.addGoodsStockNum(pid, goodsNum);
                        if (row < 1) {
                            logger.info("修改商品库存失败！");
                        }
                        confiGureModel.setNum(goodsNum);
                        confiGureModel.setTotal_num(0);
                        row = confiGureModelMapper.addGoodsAttrStockNum(confiGureModel);
                        if (row < 1) {
                            logger.info("修改商品属性库存失败！");
                        }

                        String content = userId + "取消订单，返还" + goodsNum;
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
                } else {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YWYC, "业务异常", "cancleOrder");
                }
            }
            return retMap;
        } catch (LaiKeAPIException l) {
            logger.error("取消订单 异常", l);
            throw l;
        } catch (Exception e) {
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
    public Map<String, Object> loadMore(OrderVo vo) {
        return null;
    }

    /**
     * 删除订单
     *
     * @param vo
     * @return
     */
    @Override
    public Map<String, Object> delOrder(OrderVo vo) {
        return null;
    }

    /**
     * 再次购买
     *
     * @param vo
     * @return
     */
    @Override
    public Map<String, Object> buyAgain(BuyAgainVo vo) {
        return null;
    }

    /**
     * 搜索
     *
     * @param vo
     * @return
     */
    @Override
    public Map<String, Object> orderSearch(OrderVo vo) {
        return null;
    }

    /**
     * 删除购物车
     *
     * @param vo
     * @return
     */
    @Override
    public Map<String, Object> delCart(OrderVo vo) {
        return null;
    }

    /**
     * 获取支付方式
     *
     * @param vo
     * @return
     */
    @Override
    public Map<String, Object> getPaymentConf(OrderVo vo) {
        return null;
    }

    /**
     * 订单详情 移动端个人中心
     *
     * @param vo
     * @return
     */
    @Override
    public Map<String, Object> orderDetails(OrderVo vo) {
        return null;
    }

    /**
     * 查看物流
     *
     * @param vo
     * @return
     */
    @Override
    public Map<String, Object> showLogistics(OrderVo vo) {
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
    public Map<String, Object> cancleApply(int storeId, int id) {
        return null;
    }

    /**
     * 退货信息
     *
     * @param vo
     * @return
     */
    @Override
    public Map<String, Object> returnOrderList(OrderVo vo) {
        Map<String, Object> resultMap = new HashMap<>(16);
        List<Map<String, Object>> product = new ArrayList<>();
        try {
            int storeId = vo.getStoreId();
            String keyword = vo.getKeyword();
            User user;
            if (vo.getUser() != null) {
                user = vo.getUser();
            } else {
                user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            }
            String userId = user.getUser_id();

            Map<String, Object> params = new HashMap<>(16);
            params.put("keyword", keyword);
            params.put("userId", userId);
            params.put("storeId", storeId);
            if (vo.getOrderId() != 0) {
                params.put("returnOrderId", vo.getOrderId());
            }
            if (!vo.getType().equals(DictionaryConst.OrdersType.ORDERS_HEADER_GM)) {
                params.put("type", vo.getType());
            } else {
                ArrayList<String> typeList = new ArrayList<>();
                typeList.add(vo.getType());
                typeList.add(DictionaryConst.OrdersType.ORDERS_HEADER_PT);
                params.put("typeList", typeList);
            }

            params.put("start", vo.getPageNo());
            params.put("pageSize", vo.getPageSize());
            int total = returnOrderModelMapper.getReturnOrderListCount(params);
            List<Map<String, Object>> list = returnOrderModelMapper.getReturnOrderList(params);
            if (!CollectionUtils.isEmpty(list)) {
                Map<String, Object> arr;
                for (Map<String, Object> returnOrderInfo : list) {
                    Integer id = MapUtils.getInteger(returnOrderInfo, "id");
                    //订单明细id
                    Integer orderDetailId = MapUtils.getInteger(returnOrderInfo, "detailId");
                    Integer p_id = MapUtils.getInteger(returnOrderInfo, "goodsId");
                    if (p_id == null) {
                        logger.debug("订单商品不存在 订单号{}", MapUtils.getString(returnOrderInfo, "sNo"));
                        continue;
                    }
                    //规格图
                    String attrImg = MapUtils.getString(returnOrderInfo, "attrImg");

                    returnOrderInfo.put("pid", p_id);
                    returnOrderInfo.put("audit_time", DateUtil.dateFormate(MapUtils.getString(returnOrderInfo, "audit_time"), GloabConst.TimePattern.YMDHMS));
                    returnOrderInfo.put("re_time", DateUtil.dateFormate(MapUtils.getString(returnOrderInfo, "re_time"), GloabConst.TimePattern.YMDHMS));
                    String r_sNo = DataUtils.getStringVal(returnOrderInfo, "sNo");
                    OrderModel orderModel = new OrderModel();
                    orderModel.setStore_id(storeId);
                    orderModel.setsNo(r_sNo);
                    orderModel = orderModelMapper.selectOne(orderModel);
                    if (orderModel == null) {
                        logger.warn("订单号不存在：{}", r_sNo);
                        continue;
                    }
                    int orderId = orderModel.getId();
                    arr = returnOrderInfo;
                    // 根据产品id,查询产
                    ProductListModel productListModel = new ProductListModel();
                    productListModel.setId(p_id);
                    productListModel = productListModelMapper.selectOne(productListModel);
                    String url = "";
                    Integer mchid = 0;
                    if (productListModel != null) {
                        // 拼图片路径
                        url = publiceService.getImgPath(attrImg, storeId);
                        mchid = productListModel.getMch_id();
                    }

                    arr.put("shop_id", 0);
                    arr.put("shop_name", "");
                    arr.put("shop_logo", "");
                    arr.put("headImg", "");
                    if (mchid != null && mchid != 0) {
                        MchModel mchModel = new MchModel();
                        mchModel.setId(mchid);
                        mchModel = mchModelMapper.selectOne(mchModel);
                        if (mchModel != null) {
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
            } else {
                resultMap.put("list", new ArrayList<>());
            }
            resultMap.put("total", total);
            resultMap.put("message", "操作成功");
            resultMap.put("code", 200);
        } catch (LaiKeAPIException l) {
            logger.error("获取售后列表 异常:", l);
            throw l;
        } catch (Exception e) {
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
    public Map<String, Object> okOrder(OrderVo orderVo) {
        return null;
    }

    /**
     * 订单点击退货后，进入的页面 return_method
     *
     * @param vo
     * @return
     */
    @Override
    public Map<String, Object> returnMethod(OrderVo vo) {
        return null;
    }

    /**
     * 获取收货地址和快递信息
     *
     * @param stroeId
     * @param productId
     * @return
     */
    @Override
    public Map<String, Object> seeSend(int stroeId, int productId) {
        return null;
    }

    /**
     * 快递回寄信息
     *
     * @param returnGoodsVo
     * @return
     */
    @Override
    public Map<String, Object> backSend(ReturnGoodsVo returnGoodsVo) {
        return null;
    }

    /**
     * 查看提货码
     *
     * @param vo
     * @return
     */
    @Override
    public Map<String, Object> seeExtractionCode(OrderVo vo) {
        return null;
    }

    /**
     * 售后订单详情
     *
     * @param refundDetailsVo
     * @return
     */
    @Override
    public Map<String, Object> returndetails(RefundDetailsVo refundDetailsVo) {
        return null;
    }

    /**
     * 售后确认收货
     *
     * @param vo@return
     */
    @Override
    public Map<String, Object> confirmReceipt(ReturnConfirmReceiptVo vo) {
        return null;
    }

    /**
     * 撤销售后申请
     *
     * @param params
     * @return
     */
    @Override
    public Map<String, Object> cancelApplication(CancleAfterSaleApplyVo params) {
        return null;
    }

    @Override
    public Map<String, Object> getPayment(OrderVo vo) {
        return null;
    }

    /**
     * 加购商品转换
     *
     * @return
     */
    private BigDecimal calcAddGoodsPrice(String addGoods){
        Double price = 0.0d;
        try {
            List<Map<String, Object>> productList = convertToList(addGoods);
            if (!productList.isEmpty()){
                for (Map<String, Object> map : productList) {
                    int id = MapUtils.getIntValue(map, "id");
                    int num = MapUtils.getIntValue(map, "num");
                    FlashsaleAddGoodsModel flashsaleAddGoodsModel = flashsaleAddGoodsModelMapper.selectByPrimaryKey(id);
                    if (Objects.isNull(flashsaleAddGoodsModel)){
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SPBCZ, "商品不存在", "settlement");
                    }
                    //加购价格
                    BigDecimal goodsPrice = flashsaleAddGoodsModel.getPrice();
                    price += goodsPrice.multiply(new BigDecimal(num)).doubleValue();
                }
            }
        } catch (Exception e) {
           throw new LaiKeAPIException("计算加购商品价格失败");
        }
        return BigDecimal.valueOf(price).setScale(2, RoundingMode.HALF_UP);
    }
    
    private List<Map<String,Object>> convertToList(String addGoods){
        List<Map<String, Object>> productList = new ArrayList<>();
        try {
            productList = JSON.parseObject(addGoods, new TypeReference<List<Map<String, Object>>>() {});
        }catch (JSONException e){
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSGSBZQ,addGoods + ">参数格式不正确", "immediatelyCart");
        }
        return productList;
    }
}
