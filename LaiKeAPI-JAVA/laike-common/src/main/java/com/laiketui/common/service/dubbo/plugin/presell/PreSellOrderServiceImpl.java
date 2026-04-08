package com.laiketui.common.service.dubbo.plugin.presell;

import com.alibaba.fastjson2.*;
import com.google.common.collect.Maps;
import com.laiketui.common.annotation.HandlerOrderType;
import com.laiketui.common.api.*;
import com.laiketui.common.api.order.OrderDubboService;
import com.laiketui.common.mapper.*;
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
import com.laiketui.core.utils.tool.StringUtils;
import com.laiketui.domain.config.ConfiGureModel;
import com.laiketui.domain.distribution.FreightModel;
import com.laiketui.domain.log.RecordModel;
import com.laiketui.domain.mch.MchBrowseModel;
import com.laiketui.domain.mch.MchModel;
import com.laiketui.domain.mch.MchStoreModel;
import com.laiketui.domain.message.MessageLoggingModal;
import com.laiketui.domain.order.OrderDetailsModel;
import com.laiketui.domain.order.OrderModel;
import com.laiketui.domain.presell.PreSellConfigModel;
import com.laiketui.domain.presell.PreSellGoodsModel;
import com.laiketui.domain.presell.PreSellRecordModel;
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
import java.net.URLDecoder;
import java.util.*;

import static com.laiketui.domain.order.OrderModel.*;


/**
 * 预售订单流程
 *
 * @author sunH_
 * @date 2021/12/21 15:00
 */
@HandlerOrderType(type = DictionaryConst.OrdersType.ORDERS_HEADER_PS)
@Service("PreSellOrderDubboService")
public class PreSellOrderServiceImpl implements OrderDubboService
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
    private PreSellGoodsMapper preSellGoodsMapper;

    @Autowired
    private PreSellRecordModelMapper preSellRecordModelMapper;

    @Autowired
    private PreSellConfigModelMapper preSellConfigModelMapper;

    @Autowired
    private FreightModelMapper freightModelMapper;

    @Autowired
    private AdminCgModelMapper adminCgModelMapper;

    @Autowired
    private PublicPaymentConfigService publicPaymentConfigService;

    @Autowired
    private MessageLoggingModalMapper messageLoggingModalMapper;

    @Override
    public Map<String, Object> settlement(OrderVo vo)
    {
        Map<String, Object> resultMap = new HashMap<>();
        try
        {
            // 获取用户信息 直接从redis 里面取 余额、收货信息
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            //商品金额
            BigDecimal goodsPrice = BigDecimal.ZERO;
            //实际付款金额
            BigDecimal payTotal = BigDecimal.ZERO;

            String   paypswd          = user.getPassword();
            int      wrongtimes       = user.getLogin_num();
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

            //
            List<Map<String, Object>> productsListMap = publiceService.productsList(productList, vo.getCarts(), vo.getBuyType(), DictionaryConst.OrdersType.ORDERS_HEADER_MS);
            if (CollectionUtils.isEmpty(productsListMap))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_KCBZ, "库存不足2", "settlement");
            }
            //按照店铺归类的商品、运费、商品总价等信息
            Map<String, Object> productsInfo = publiceService.settlementProductsInfo(productsListMap, vo.getStoreId(), vo.getProductType());
            //运费信息
            Map<String, List<Map<String, Object>>> productsFreight = DataUtils.cast(productsInfo.get("products_freight"));
            if (productsFreight == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YFXXCW, "运费信息错误");
            }
            //拿出商品信息
            productsListMap = DataUtils.cast(productsInfo.get("products"));
            if (productsListMap == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SPXXCW, "商品信息错误");
            }
            for (String mchId : productsFreight.keySet())
            {
                productsListMap.get(0).put("shop_id", Integer.parseInt(mchId));
                productsListMap.get(0).put("product_total", new BigDecimal(productsInfo.get("products_total") + "").doubleValue());
            }
            //计算店铺运费、总订单运费
            BigDecimal yunfei = BigDecimal.ZERO;
            productsInfo = publicOrderService.getFreight(productsFreight, productsListMap, userAddress, vo.getStoreId(), vo.getProductType());
            List<Map<String, Object>> products = DataUtils.cast(productsInfo.get("products"));
            if (products == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SPXXCW, "商品信息错误");
            }
            goodsPrice = new BigDecimal(products.get(0).get("product_total") + "");
            //运费
            if (vo.getShopAddressId() != null && vo.getShopAddressId() != 0)
            {
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
            //计算订单金额
            payTotal = payTotal.add(yunfei);
            //门点自提信息
            Map<String, Object> mdztInfo;
            //门店自提结算
            int shopStatus = 0;
            //店铺信息
            MchStoreModel mchStoreModel = null;
            if (products.size() == 1)
            {
                int mchId = (int) products.get(0).get("shop_id");
                if (vo.getShopAddressId() != null)
                {
                    mdztInfo = publicMchService.settlement(vo.getStoreId(), mchId, "", vo.getShopAddressId(), vo.getStoreType());
                    shopStatus = (int) mdztInfo.get("shop_status");
                    mchStoreModel = (MchStoreModel) mdztInfo.get("mch_store_info");
                }
            }

            //支付密码错误一天超过5此不允许再使用余额支付
            resultMap.put("enterless", enterless);
            // 自提标记 1为自提
            resultMap.put("shop_status", shopStatus);
            // 门店自提信息
            resultMap.put("shop_list", mchStoreModel);
            // 支付方式信息状态
            resultMap.putAll(payInfo);
            // 商品列表
            resultMap.put("products", products);
            // 是否分销
            resultMap.put("is_distribution", vo.getIsDistribution());
            // 密码状态
            resultMap.put("password_status", passwordStatus);
            // 商品总价
            resultMap.put("products_total", goodsPrice);
            // 用户余额
            resultMap.put("user_money", user.getMoney());
            // 实际支付金额(判断商品是否为预售商品)
            Map<String, Object>       sellMap           = new HashMap<>(16);
            List<Map<String, Object>> list              = (List<Map<String, Object>>) products.get(0).get("list");
            Map<String, Object>       sellGood          = list.get(0);
            int                       pid               = Integer.parseInt(sellGood.get("pid").toString());
            int                       num               = Integer.parseInt(sellGood.get("num").toString());
            PreSellGoodsModel         preSellGoodsModel = new PreSellGoodsModel();
            preSellGoodsModel.setProduct_id(pid);
            preSellGoodsModel = preSellGoodsMapper.selectOne(preSellGoodsModel);
            if (!Objects.isNull(preSellGoodsModel))
            {
                PreSellConfigModel preSellConfigModel = new PreSellConfigModel();
                preSellConfigModel.setStore_id(vo.getStoreId());
                preSellConfigModel = preSellConfigModelMapper.selectOne(preSellConfigModel);
                if (!Objects.isNull(preSellConfigModel))
                {
                    sellMap.put("depositDesc", preSellConfigModel.getDeposit_desc());
                    sellMap.put("balanceDesc", preSellConfigModel.getBalance_desc());
                }
                sellMap.put("sellType", preSellGoodsModel.getSell_type());
                if (preSellGoodsModel.getSell_type().equals(1))
                {
                    BigDecimal deposit = preSellGoodsModel.getDeposit().multiply(new BigDecimal(num));
                    sellMap.put("deposit", deposit);
                    sellMap.put("balance", goodsPrice.subtract(deposit).add(yunfei));
                    resultMap.put("total", deposit);
                }
                else
                {
                    resultMap.put("total", goodsPrice.add(yunfei));
                }
            }
            resultMap.put("sellMap", sellMap);
            resultMap.put("freight", yunfei);
            // 用户地址
            resultMap.put("address", userAddress);
            // 是否有收货地址
            resultMap.put("addemt", addemt);
            // 平台优惠
            resultMap.put("preferential_amount", 0);
            // 会员等级金额
            resultMap.put("grade_rate_amount", 0);
            return resultMap;
        }
        catch (LaiKeAPIException e)
        {
            logger.debug("预售商品结算异常", e);
            throw e;
        }
        catch (Exception e)
        {
            logger.debug("预售商品结算异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "settlement");
        }

    }

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
            /**
             * 店铺订单备注
             */
            Map<String, String> mchRemarks = new HashMap<>();
            // 是否有备注标记
            boolean                   remarksStatus = false;
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
            products = publiceService.productsList(productList, vo.getCarts(), vo.getBuyType(), DictionaryConst.OrdersType.ORDERS_HEADER_PS);
            if (StringUtils.isEmpty(products))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_KCBZ, "库存不足1", "payment");
            }

            // 提交状态 1是再次购买 空是正常提交
            // 支付类型
            String payType = vo.getPayType();
            logger.info("订单支付类型：{}", payType);
            // 订单总价
            BigDecimal total;
            // 4.查询默认地址
            Map<String, Object> map = new HashMap<>();
            map.put("store_id", vo.getStoreId());
            //支付时间
            Date payDateTime = null;

            //获取用户的默认收货地址
            map.put("user_id", user.getUser_id());
            map.put("address_id", vo.getAddressId());
            UserAddress userAddress = commonAddressService.findAddress(map);
            // 存储收货信息
            String mobile    = userAddress.getTel();
            String name      = userAddress.getName();
            String sheng     = userAddress.getSheng();
            String shi       = userAddress.getCity();
            String xian      = userAddress.getQuyu();
            String addressXq = userAddress.getAddress();

            //列出商品数组-计算总价和优惠，拿商品运费ID
            Map<String, Object> productsInfo = publiceService.settlementProductsInfo(products, vo.getStoreId(), DictionaryConst.OrdersType.ORDERS_HEADER_PS);
            //运费信息
            Map<String, List<Map<String, Object>>> productsFreight = DataUtils.cast(productsInfo.get("products_freight"));
            if (productsFreight == null)
            {
                logger.debug("订单结算运费数据出错");
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SJCW, "数据错误");
            }
            //计算会员的产品价格和订单产品总价
            products = DataUtils.cast(productsInfo.get("products"));
            if (products == null)
            {
                logger.debug("订单商品数据出错");
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SJCW, "数据错误");
            }
            //计算会员优惠价格
            MemberPriceVo memberPriceVo = new MemberPriceVo();
            memberPriceVo.setUserId(userId);
            memberPriceVo.setStoreId(vo.getStoreId());
            memberPriceVo.setMchProductList(products);
            Map<String, Object> memberProductsInfo = publiceService.getMemberPrice(memberPriceVo, vo.getVipSource());
            //拿出商品信息
            products = DataUtils.cast(memberProductsInfo.get("products"));
            if (products == null)
            {
                logger.debug("计算会员优惠价格数据出错");
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SJCW, "数据错误");
            }
            BigDecimal gradeRate = DataUtils.getBigDecimalVal(memberProductsInfo, "grade_rate");
            productsTotal = DataUtils.getBigDecimalVal(memberProductsInfo, "products_total");

            //计算店铺运费、总订单运费
            BigDecimal yunfei = BigDecimal.ZERO;
            productsInfo = publicOrderService.getFreight(productsFreight, products, userAddress, vo.getStoreId(), vo.getProductType());
            products = DataUtils.cast(productsInfo.get("products"));
            if (products == null)
            {
                logger.debug("计算店铺运费、总订单运费 数据出错");
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SJCW, "数据错误");
            }
            if (vo.getShopAddressId() != 0)
            {
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
            // 自提信息店铺
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
            //店铺id
            int mch_id = DataUtils.getIntegerVal(products.get(0), "shop_id");
            //自提二维码生成、运费处理
            if (shopAddressId != 0)
            {
                Map<String, Object> shopMap = publicMchService.settlement(vo.getStoreId(), mch_id, "payment", shopAddressId, vo.getStoreType());
                sheng = DataUtils.getStringVal(shopMap, "sheng");
                shi = DataUtils.getStringVal(shopMap, "shi");
                xian = DataUtils.getStringVal(shopMap, "xian");
                addressXq = DataUtils.getStringVal(shopMap, "address");
                shopStatus = DataUtils.getIntegerVal(shopMap, "shop_status");
                extractionCode = DataUtils.getStringVal(shopMap, "extraction_code");
                extractionCodeImg = DataUtils.getStringVal(shopMap, "extraction_code_img");
                yunfei = BigDecimal.ZERO;
            }

            // 生成订单号
            String sNo = publicOrderService.createOrderNo(vo.getType());
            // 生成支付订单号
            String        realSno             = publicOrderService.createOrderNo(vo.getType());
            StringBuilder mchId               = new StringBuilder();
            BigDecimal    preferential_amount = BigDecimal.ZERO;
            // 商品总价
            total = productsTotal.add(yunfei);
            int pos = 0;
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
                //如果是多店铺，添加一条购买记录
                MchBrowseModel mchBrowseModel = new MchBrowseModel();
                mchBrowseModel.setMch_id(shopId);
                mchBrowseModel.setStore_id(storeId);
                mchBrowseModel.setUser_id(userId);
                mchBrowseModel.setEvent("购买了预售商品");
                mchBrowseModel.setAdd_time(new Date());
                mchBrowseModelMapper.insertSelective(mchBrowseModel);
                List<Map<String, Object>> onlyProducts = DataUtils.cast(mchProduct.get("list"));
                if (onlyProducts == null)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SJYC, "数据异常");
                }
                for (Map<String, Object> product : onlyProducts)
                {
                    int pid = DataUtils.getIntegerVal(product, "pid");
                    //根据id获取运费信息
                    //根据id查询商品表  新增不配送区域判断
                    ProductListModel proFreight   = productListModelMapper.selectByPrimaryKey(pid);
                    FreightModel     freightModel = freightModelMapper.selectByPrimaryKey(proFreight.getFreight());
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
                    int        cid          = DataUtils.getIntegerVal(product, "cid");
                    int        num          = DataUtils.getIntegerVal(product, "num");
                    BigDecimal price        = DataUtils.getBigDecimalVal(product, "price");
                    String     productTitle = DataUtils.getStringVal(product, "product_title");
                    String     unit         = DataUtils.getStringVal(product, "unit");
                    String     size         = DataUtils.getStringVal(product, "size");
                    //如果没有优惠则为支付金额
                    BigDecimal amountAfterDiscountTmp = DataUtils.getBigDecimalVal(product, "amount_after_discount");
                    BigDecimal freightPrice           = DataUtils.getBigDecimalVal(product, "freight_price");
                    // 循环插入订单附表 ，添加不同的订单详情
                    freightPrice = vo.getShopAddressId() != 0 ? BigDecimal.ZERO : freightPrice;
                    PreSellGoodsModel preSellGoodsModel = new PreSellGoodsModel();
                    preSellGoodsModel.setProduct_id(pid);
                    preSellGoodsModel = preSellGoodsMapper.selectOne(preSellGoodsModel);
                    ProductListModel productListModel = new ProductListModel();
                    productListModel.setId(pid);
                    productListModel = productListModelMapper.selectOne(productListModel);
                    if (!Objects.isNull(preSellGoodsModel))
                    {
                        if (Integer.parseInt(productListModel.getStatus()) != DictionaryConst.GoodsStatus.NEW_GROUNDING)
                        {
                            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_GSPZWSJ, "该商品暂未上架");
                        }
                        if (!Objects.isNull(vo.getPayTarget()) && vo.getPayTarget() == 1)
                        {
                            if (preSellGoodsModel.getSell_type() != 1)
                            {
                                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_GSPBZCDJMS, "该商品不支持定金模式");
                            }
                            if (!Objects.isNull(preSellGoodsModel.getDeposit_end_time()) && preSellGoodsModel.getDeposit_end_time().before(new Date()))
                            {
                                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_GSPYGZFDJYXQ, "该商品已过支付定金有效期");
                            }
                        }
                        if (!Objects.isNull(vo.getPayTarget()) && vo.getPayTarget() == 2)
                        {
                            if (!preSellGoodsModel.getSell_type().equals(PreSellGoodsModel.DEPOSIT_PATTERN))
                            {
                                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_GSPBZCDJMS, "该商品不支持定金模式");
                            }
                            if (!Objects.isNull(preSellGoodsModel.getBalance_pay_time()))
                            {
                                Calendar calendar = Calendar.getInstance();
                                calendar.setTime(preSellGoodsModel.getBalance_pay_time());
                                calendar.set(Calendar.HOUR_OF_DAY, 0);
                                calendar.set(Calendar.MINUTE, 0);
                                calendar.set(Calendar.SECOND, 0);
                                Date start = calendar.getTime();
                                calendar.add(Calendar.DAY_OF_MONTH, 1);
                                calendar.add(Calendar.SECOND, -1);
                                Date end = calendar.getTime();
                                if (DateUtil.dateCompare(start, new Date()))
                                {
                                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_GSPHWDZFWKSJ, "该商品还未到支付尾款时间");
                                }
                                if (DateUtil.dateCompare(new Date(), end))
                                {
                                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_GSPYGWKZFSJ, "该商品已过尾款支付时间");
                                }
                            }
                        }
                        if (!Objects.isNull(vo.getPayTarget()) && vo.getPayTarget() == 3)
                        {
                            if (!preSellGoodsModel.getSell_type().equals(PreSellGoodsModel.ORDER_PATTERN))
                            {
                                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_GSPBZCDHMS, "该商品不支持订货模式");
                            }
                            if (!Objects.isNull(preSellGoodsModel.getDeadline()) && preSellGoodsModel.getDeadline().before(new Date()))
                            {
                                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_GSPYJZYS, "该商品已截止预售");
                            }
                            if (preSellGoodsModel.getSell_num() < num)
                            {
                                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_GSPSYYSKC, "该商品剩余预售库存：" + preSellGoodsModel.getSell_num());
                            }
                            if (preSellGoodsModel.getSell_num() == 0)
                            {
                                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_GSPYSK, "该商品已售空");
                            }
                        }
                        totalNum += num;
                        String mainOrderRemarks = "";
                        if (remarksStatus)
                        {
                            mainOrderRemarks = JSON.toJSONString(mchRemarks);
                        }
                        //创建订单详情
                        OrderDetailsModel orderDetailsModel = new OrderDetailsModel();
                        orderDetailsModel.setStore_id(storeId);
                        orderDetailsModel.setUser_id(userId);
                        orderDetailsModel.setP_id(pid);
                        orderDetailsModel.setP_name(productTitle);
                        orderDetailsModel.setP_price(price);
                        orderDetailsModel.setNum(num);
                        orderDetailsModel.setUnit(unit);
                        orderDetailsModel.setR_sNo(sNo);
                        orderDetailsModel.setAdd_time(new Date());
                        orderDetailsModel.setR_status(OrderModel.ORDER_UNPAY);
                        orderDetailsModel.setSize(size);
                        orderDetailsModel.setSid(cid + "");
                        orderDetailsModel.setFreight(freightPrice);
                        orderDetailsModel.setSettlement_type(0);
                        orderDetailsModel.setRecycle(0);
                        orderDetailsModel.setAfter_discount(amountAfterDiscountTmp);
                        orderDetailsModel.setExchange_num(0);
                        orderDetailsModel.setInvoice(0);
                        orderDetailsModel.setExpress(0);
                        orderDetailsModel.setCoupon_id(String.valueOf(0));
                        orderDetailsModel.setMch_id(Integer.parseInt(shopId));
                        orderDetailsModel.setManual_offer(new BigDecimal("0.00"));
                        //创建订单信息
                        mchId = new StringBuilder(new StringBuilder(StringUtils.rtrim(mchId.toString(), SplitUtils.DH)));
                        mchId = new StringBuilder(new StringBuilder(SplitUtils.DH + mchId + SplitUtils.DH));
                        //获取订单配置
                        Map<String, Object> configMap = publicOrderService.getOrderConfig(vo.getStoreId(), null, DictionaryConst.OrdersType.ORDERS_HEADER_PS);
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
                        orderModel.setStore_id(storeId);
                        orderModel.setUser_id(userId);
                        orderModel.setName(name);
                        orderModel.setMobile(mobile);
                        orderModel.setNum(num);
                        orderModel.setZ_price(total);
                        orderModel.setOld_total(orderModel.getZ_price());
                        orderModel.setsNo(sNo);
                        orderModel.setSheng(sheng);
                        orderModel.setShi(shi);
                        orderModel.setXian(xian);
                        orderModel.setAddress(addressXq);
                        orderModel.setRemark(vo.getRemarks());
                        orderModel.setPay(payType);
                        orderModel.setAdd_time(payDateTime = new Date());
                        orderModel.setSubtraction_id(subtractionId);
                        orderModel.setConsumer_money(allow);
                        orderModel.setCoupon_activity_name(reduceNameArray);
                        orderModel.setSpz_price(productsTotal);
                        orderModel.setStatus(OrderModel.ORDER_UNPAY);
                        orderModel.setReduce_price(reduceMoney);
                        orderModel.setSource(vo.getStoreType());
                        orderModel.setOtype(DictionaryConst.OrdersType.ORDERS_HEADER_PS);
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
                        orderModel.setZ_freight(yunfei);
                        orderModel.setCoupon_price(couponMoney);
                        orderModel.setPreferential_amount(preferential_amount);
                        orderModel.setSingle_store(shopAddressId);
                        orderModel.setReadd(OrderModel.READD_UNREAD);
                        orderModel.setZhekou(BigDecimal.ZERO);
                        orderModel.setRecycle(0);
                        orderModel.setPick_up_store(0);
                        orderModel.setOperation_type(1);
                        orderModel.setOffset_balance(new BigDecimal("0.00"));
                        orderModel.setDelivery_status(0);
                        orderModel.setCoupon_id(String.valueOf(0));
                        orderModel.setIs_anonymous(0);
                        orderModel.setRed_packet(String.valueOf(0));
                        orderModel.setAllow(0);
                        orderModel.setIs_put(0);
                        orderModel.setManual_offer(new BigDecimal("0.00"));
                        orderModel.setSettlement_status(0);
                        orderModel.setOrderFailureTime(orderFailureDay);
                        PreSellRecordModel preSellRecordModel = new PreSellRecordModel();
                        preSellRecordModel.setStore_id(storeId);
                        preSellRecordModel.setUser_id(userId);
                        preSellRecordModel.setProduct_id(pid);
                        preSellRecordModel.setAttr_id(cid);
                        preSellRecordModel.setPrice(new BigDecimal(0));
                        preSellRecordModel.setNum(num);
                        preSellRecordModel.setsNo(sNo);
                        preSellRecordModel.setReal_sno(realSno);
                        preSellRecordModel.setAdd_time(new Date());
                        if (preSellGoodsModel.getSell_type().equals(2))
                        {
                            //订货模式订单信息
                            preSellRecordModelMapper.insertSelective(preSellRecordModel);
                            logger.error("预售记录表插入操作触发  804！！！");
                            orderDetailsModelMapper.insertSelective(orderDetailsModel);
                            orderModelMapper.insertSelective(orderModel);
                            //计算支付金额
                            total = (productsTotal.add(yunfei));
                            //订货模式站内通知  订金模式站内通知放在支付回调逻辑里  因为订金模式订单没付款订金是不产生待付款订单
                            MessageLoggingModal messageLoggingSave = new MessageLoggingModal();
                            messageLoggingSave.setMch_id(Integer.valueOf(shopId));
                            messageLoggingSave.setStore_id(vo.getStoreId());
                            messageLoggingSave.setType(MessageLoggingModal.Type.TYPE_ORDER_NEW);
                            messageLoggingSave.setTo_url(OrderDataUtils.getOrderRoute(DictionaryConst.OrdersType.ORDERS_HEADER_PS, 0));
                            messageLoggingSave.setParameter(orderModel.getId() + "");
                            messageLoggingSave.setContent(String.format("您来新订单了，订单为%s，请及时处理！", orderModel.getsNo()));
                            messageLoggingSave.setAdd_date(new Date());
                            messageLoggingModalMapper.insertSelective(messageLoggingSave);
                        }
                        else
                        {
                            //定金模式订单信息
                            preSellRecordModel.setDeposit(preSellGoodsModel.getDeposit().multiply(new BigDecimal(num)));
                            preSellRecordModel.setBalance(productsTotal.subtract(preSellRecordModel.getDeposit()));
                            preSellRecordModel.setPay_type(PreSellRecordModel.DEPOSIT);
                            preSellRecordModel.setIs_pay(DictionaryConst.WhetherMaven.WHETHER_NO);
                            preSellRecordModel.setOrder_details_info(JSON.toJSONString(orderDetailsModel));
                            preSellRecordModel.setOrder_info(JSON.toJSONString(orderModel));
                            preSellRecordModelMapper.insertSelective(preSellRecordModel);
                            logger.error("预售记录表插入操作触发  830！！！");
                            logger.error("生成了订单号：" + sNo);

                            //计算支付金额
                            if (vo.getPayTarget() == 1)
                            {
                                total = preSellRecordModel.getDeposit();
                            }
                            else if (vo.getPayTarget() == 2)
                            {
                                total = preSellRecordModel.getBalance().add(yunfei);
                            }
                        }
                        //库存-1
                        int res_del1 = productListModelMapper.reduceGoodsStockNum(pid, num);
                        if (res_del1 < 1)
                        {
                            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_XDSBQSHZS, "下单失败,请稍后再试", "payment");
                        }
                        //预售商品所有规格共用一个库存
                        int res_del2 = confiGureModelMapper.reduceProAllAttrNum(num, pid);
                        if (res_del2 < 1)
                        {
                            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_XDSBQSHZS, "下单失败,请稍后再试", "payment");
                        }
                        //订货模式预售商品预售数量-1
                        if (preSellGoodsModel.getSell_type() == 2)
                        {
                            preSellGoodsModel.setSurplus_num(preSellGoodsModel.getSurplus_num() - num);
                            preSellGoodsMapper.updateByPrimaryKey(preSellGoodsModel);
                        }
                        ConfiGureModel confiGureModel = new ConfiGureModel();
                        confiGureModel.setId(cid);
                        confiGureModel.setPid(pid);
                        confiGureModel = confiGureModelMapper.selectOne(confiGureModel);
                        StockModel stockModel = new StockModel();
                        stockModel.setStore_id(storeId);
                        stockModel.setProduct_id(pid);
                        stockModel.setAttribute_id(cid);
                        stockModel.setTotal_num(confiGureModel.getNum());
                        stockModel.setFlowing_num(num);
                        stockModel.setType(StockModel.StockType.AGREEMENTTYPE_WAREHOUSING_OUT);
                        stockModel.setUser_id(userId);
                        stockModel.setAdd_date(new Date());
                        stockModel.setContent(userId + "生成订单所需" + num);
                        stockModelMapper.insertSelective(stockModel);

                    }
                }
            }
            //订单号
            resultMap.put("sNo", sNo);
            //订单总支付金额
            resultMap.put("total", total);
            //订单支付时间
            resultMap.put("orderTime", DateUtil.dateFormate(payDateTime, GloabConst.TimePattern.YMDHMS));
            return resultMap;
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("下单 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_XDSBQSHZS, "下单失败,请稍后再试", "payment");
        }
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
        return null;
    }

    @Override
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
    public Map<String, Object> remindDelivery(OrderVo vo)
    {
        return null;
    }

    @Override
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
                String     couponId      = order.getCoupon_id();
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

                Map params = Maps.newHashMap();
                params.put("status", ORDER_CLOSE);
                params.put("cancleOrder", "yes");
                params.put("storeId", storeId);
                params.put("userId", userId);
                params.put("id", orderId);
                int row = orderModelMapper.updateOrderInfo(params);
                if (row < 1)
                {
                    logger.info(sNo + "修改订单状态失败:");
                }

                int row1 = orderDetailsModelMapper.updateOrderDetailsStatus(storeId, sNo, ORDER_CLOSE);
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
                        row = confiGureModelMapper.addGoodsPreSellAttrStockNum(confiGureModel);
                        if (row < 1)
                        {
                            logger.info("修改商品属性库存失败！");
                        }
                        ConfiGureModel allConfiGureModel = new ConfiGureModel();
                        allConfiGureModel.setPid(pid);
                        List<ConfiGureModel> list = confiGureModelMapper.select(allConfiGureModel);
                        for (ConfiGureModel gureModel : list)
                        {
                            String     content    = userId + "取消订单，返还" + goodsNum;
                            StockModel stockModel = new StockModel();
                            stockModel.setStore_id(storeId);
                            stockModel.setProduct_id(pid);
                            stockModel.setAttribute_id(gureModel.getId());
                            stockModel.setTotal_num(totalNum);
                            stockModel.setFlowing_num(goodsNum);
                            stockModel.setType(StockModel.StockType.STOCKTYPE_WAREHOUSING);
                            stockModel.setUser_id(userId);
                            stockModel.setAdd_date(new Date());
                            stockModel.setContent(content);
                            stockModelMapper.insert(stockModel);
                        }
                        PreSellGoodsModel preSellGoodsModel = new PreSellGoodsModel();
                        preSellGoodsModel.setProduct_id(pid);
                        PreSellGoodsModel preSellGoods = preSellGoodsMapper.selectOne(preSellGoodsModel);
                        if (!Objects.isNull(preSellGoods))
                        {
                            if (preSellGoods.getSell_type().equals(PreSellGoodsModel.ORDER_PATTERN))
                            {
                                preSellGoods.setSurplus_num(preSellGoods.getSurplus_num() + goodsNum);
                                preSellGoodsMapper.updateByPrimaryKey(preSellGoods);
                            }
                        }
                    }
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
    public Map<String, Object> loadMore(OrderVo vo)
    {
        return null;
    }

    @Override
    public Map<String, Object> delOrder(OrderVo vo)
    {
        Map retMap = Maps.newHashMap();
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
                String sNo  = order.getsNo();
                int    row1 = orderDetailsModelMapper.delOrderDetails(storeId, sNo);
                int    row2 = orderModelMapper.delOrder(storeId, sNo);
                if (row1 >= 0 && row2 >= 0)
                {
                    Map<String, Object> parmaMap = new HashMap<>(16);
                    parmaMap.put("store_id", vo.getStoreId());
                    parmaMap.put("orderno", sNo);
                    parmaMap.put("recycle", DictionaryConst.ProductRecycle.NOT_STATUS);
                    //回滚库存
                    List<Map<String, Object>> orderDetailsModelList = orderDetailsModelMapper.getOrderDetailByGoodsInfo(parmaMap);
                    for (Map<String, Object> detail : orderDetailsModelList)
                    {
                        Integer status  = MapUtils.getInteger(detail, "r_status");
                        int     id      = MapUtils.getIntValue(detail, "id");
                        int     attrId  = MapUtils.getIntValue(detail, "sid");
                        int     goodsId = MapUtils.getIntValue(detail, "goodsId");
                        int     num     = MapUtils.getIntValue(detail, "num");
                        Integer integer = returnOrderModelMapper.orderDetailReturnIsNotEnd(vo.getStoreId(), sNo, id);
                        if (integer != null && integer > 0)
                        {
                            throw new LaiKeAPIException(ErrorCode.BizErrorCode.OPERATION_FAILED, "当前订单正在售后处理,不可关闭");
                        }
                        if (status.equals(DictionaryConst.OrdersStatus.ORDERS_R_STATUS_COMPLETE) || status.equals(DictionaryConst.OrdersStatus.ORDERS_R_STATUS_CLOSE))
                        {
                            logger.debug("订单明细id{} 状态{} 删除订单不回滚库存", id, status);
                            continue;
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
                        productListModelMapper.updateProductListVolume(-num, vo.getStoreId(), goodsId);
                    }

                    retMap.put("code", 200);
                    retMap.put("message", "操作成功");
                }
                else
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YWYC, "业务异常", "delOrder");
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
            params.put("type", vo.getType());
            params.put("start", vo.getPageNo());
            params.put("pageSize", vo.getPageSize());
            List<Map<String, Object>> list = returnOrderModelMapper.getReturnOrderList(params);
            if (!CollectionUtils.isEmpty(list))
            {
                Map<String, Object> arr;
                for (Map<String, Object> returnOrderInfo : list)
                {
                    //订单明细id
                    Integer orderDetailId = MapUtils.getInteger(returnOrderInfo, "detailId");
                    Integer p_id          = MapUtils.getInteger(returnOrderInfo, "goodsId");
                    if (p_id == null)
                    {
                        logger.debug("订单商品不存在 订单号{}", MapUtils.getString(returnOrderInfo, "sNo"));
                        continue;
                    }
                    returnOrderInfo.put("pid", p_id);
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
                        mchid = productListModel.getMch_id();
                    }
                    OrderDetailsModel orderDetailsModel = orderDetailsModelMapper.selectByPrimaryKey(orderDetailId);
                    if (orderDetailsModel != null)
                    {
                        ConfiGureModel confiGureModel = confiGureModelMapper.selectByPrimaryKey(orderDetailsModel.getSid());
                        if (confiGureModel != null)
                        {
                            url = publiceService.getImgPath(confiGureModel.getImg(), storeId);
                        }
                    }
                    else
                    {
                        if (productListModel != null)
                        {
                            url = publiceService.getImgPath(productListModel.getImgurl(), storeId);
                            mchid = productListModel.getMch_id();
                        }
                    }
                    arr.put("shop_id", 0);
                    arr.put("shop_name", "");
                    arr.put("shop_logo", "");
                    arr.put("head_img", "");
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
                            String imgPath = publiceService.getImgPath(mchModel.getHead_img(), storeId);
                            arr.put("head_img", imgPath);
                            arr.put("headImg", imgPath);
                        }
                    }
                    arr.put("order_id", orderId);
                    arr.put("imgurl", url);
                    //售后id
                    Integer id           = MapUtils.getInteger(returnOrderInfo, "id");
                    String  returnStatus = publicRefundService.getRefundStatus(vo.getStoreId(), id);
                    arr.put("prompt", returnStatus);
                    product.add(arr);
                }
                resultMap.put("list", product);
            }
            else
            {
                resultMap.put("list", new ArrayList<>());
            }
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
    public Map<String, Object> okOrder(OrderVo orderVo)
    {
        return null;
    }

    @Override
    public Map<String, Object> returnMethod(OrderVo vo)
    {
        return null;
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
    public Map<String, Object> returndetails(RefundDetailsVo refundDetailsVo)
    {
        return null;
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
}

