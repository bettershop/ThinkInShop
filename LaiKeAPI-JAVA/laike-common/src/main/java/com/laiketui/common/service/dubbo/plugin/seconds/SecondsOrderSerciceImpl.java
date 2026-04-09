package com.laiketui.common.service.dubbo.plugin.seconds;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.TypeReference;
import com.google.common.collect.Maps;
import com.laiketui.common.annotation.HandlerOrderType;
import com.laiketui.common.api.*;
import com.laiketui.common.api.order.OrderDubboService;
import com.laiketui.common.api.plugin.seconds.PublicSecondsService;
import com.laiketui.common.mapper.*;
import com.laiketui.common.process.OrderShowValueProcess.OrderProcessor;
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
import com.laiketui.core.utils.tool.DoubleFormatUtil;
import com.laiketui.core.utils.tool.StringUtils;
import com.laiketui.domain.config.ConfiGureModel;
import com.laiketui.domain.distribution.FreightModel;
import com.laiketui.domain.mch.MchBrowseModel;
import com.laiketui.domain.mch.MchModel;
import com.laiketui.domain.mch.MchStoreModel;
import com.laiketui.domain.message.MessageLoggingModal;
import com.laiketui.domain.order.OrderDetailsModel;
import com.laiketui.domain.order.OrderModel;
import com.laiketui.domain.plugin.seckill.SecondsRecordModel;
import com.laiketui.domain.product.ProductListModel;
import com.laiketui.domain.seckill.SecondsActivityModel;
import com.laiketui.domain.seckill.SecondsConfigModel;
import com.laiketui.domain.seckill.SecondsProModel;
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
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
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
 * 秒杀订单流程
 *
 * @author Trick
 * @date 2021/4/12 11:05
 */
@HandlerOrderType(type = DictionaryConst.OrdersType.ORDERS_HEADER_MS)
@Service("SecOrderDubboService")
public class SecondsOrderSerciceImpl implements OrderDubboService
{
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private PublicAddressService commonAddressService;

    @Autowired
    private OrderDetailsModelMapper orderDetailsModelMapper;

    @Autowired
    private PublicOrderService   publicOrderService;
    @Autowired
    private PublicMchService     publicMchService;
    @Autowired
    private MchBrowseModelMapper mchBrowseModelMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserBaseMapper userBaseMapper;

    @Autowired
    private PaymentConfigModelMapper paymentConfigModelMapper;

    @Autowired
    private SecondsProModelMapper secondsProModelMapper;

    @Autowired
    private ProductListModelMapper productListModelMapper;

    @Autowired
    private OrderModelMapper orderModelMapper;

    @Autowired
    private MchModelMapper mchModelMapper;

    @Autowired
    private PubliceService publiceService;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired(required = false)
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private SecondsConfigModelMapper secondsConfigModelMapper;

    @Autowired
    private SecondsRecordModelMapper secondsRecordModelMapper;

    @Autowired
    private FreightModelMapper freightModelMapper;

    @Autowired
    private PublicSecondsService publicSecondsService;

    @Autowired
    private PublicMemberService publicMemberService;

    @Autowired
    private PublicStockService publicStockService;

    @Autowired
    private SecondsActivityModelMapper secondsActivityModelMapper;

    @Autowired
    private ConfiGureModelMapper confiGureModelMapper;

    @Autowired
    private ReturnOrderModelMapper returnOrderModelMapper;

    @Autowired
    private PublicRefundService publicRefundService;

    @Autowired
    private MessageLoggingModalMapper messageLoggingModalMapper;

    @Autowired
    private CustomerModelMapper customerModelMapper;

    @Autowired
    private OrderProcessor orderProcessor;

    @Autowired
    private PublicPaymentConfigService publicPaymentConfigService;

    @Override
    public Map<String, Object> settlement(OrderVo vo)
    {
        Map<String, Object> resultMap = new HashMap<>();
        try
        {
            // 获取用户信息 直接从redis 里面取 余额、收货信息
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            user = userBaseMapper.selectByPrimaryKey(user.getId());
            int storeId = vo.getStoreId();
            //获取商城自营店id
            Integer zyMchId = customerModelMapper.getStoreMchId(storeId);
            //商品金额
            BigDecimal goodsPrice = BigDecimal.ZERO;
            //实际付款金额，最小也要0.01
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
            if (wrongtimes == 5)
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
            int passwordStatus = 1;
            if (StringUtils.isEmpty(paypswd))
            {
                passwordStatus = 0;
            }

            //各个支付的开启、关闭状态
            Map payInfo = new HashMap();
            payInfo = publicPaymentConfigService.getPaymentInfos(storeId);
            Map map = new HashMap();
            map.put("store_id", storeId);

            //获取用户的默认收货地址
            map.put("user_id", user.getUser_id());
            if (vo.getAddressId() != null && vo.getAddressId() != 0 && vo.getAddressId() != -1)
            {
                map.put("address_id", vo.getAddressId());
            }

            UserAddress userAddress = commonAddressService.findAddress(storeId, user.getUser_id(), vo.getAddressId());
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
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.PARAMATER_ERROR, vo.getProductsInfo() + ">参数格式不正确", "immediatelyCart");
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
            //
            List<Map<String, Object>> productsListMap = publiceService.productsList(productList, vo.getCarts(), vo.getBuyType(), DictionaryConst.OrdersType.ORDERS_HEADER_MS);
            if (CollectionUtils.isEmpty(productsListMap))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.STOCK_INSUFFICIENT, "库存不足2", "settlement");
            }
            //按照店铺归类的商品、运费、商品总价等信息
            Map<String, Object> productsInfo = publiceService.settlementProductsInfo(productsListMap, storeId, vo.getProductType());
            //运费信息
            Map<String, List<Map<String, Object>>> productsFreight = DataUtils.cast(productsInfo.get("products_freight"));
            if (productsFreight == null)
            {
                throw new LaiKeAPIException(ORDER_DATA_ERROR, "运费信息错误");
            }
            //拿出商品信息
            productsListMap = DataUtils.cast(productsInfo.get("products"));
            if (productsListMap == null)
            {
                throw new LaiKeAPIException(ORDER_DATA_ERROR, "商品信息错误");
            }
            for (String mchId : productsFreight.keySet())
            {
                productsListMap.get(0).put("shop_id", Integer.parseInt(mchId));
                productsListMap.get(0).put("product_total", new BigDecimal(productsInfo.get("products_total") + "").doubleValue());
            }
            //计算店铺运费、总订单运费
            BigDecimal yunfei = BigDecimal.ZERO;
            productsInfo = publicOrderService.getFreight(productsFreight, productsListMap, userAddress, storeId, vo.getProductType());
            List<Map<String, Object>> products = DataUtils.cast(productsInfo.get("products"));
            if (products == null)
            {
                throw new LaiKeAPIException(ORDER_DATA_ERROR, "商品信息错误");
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
            //获取会员折扣
            BigDecimal gradeRate = BigDecimal.valueOf(publicMemberService.getMemberGradeRate(DictionaryConst.OrdersType.ORDERS_HEADER_MS, user.getUser_id(), storeId));
            //会员优惠价格
            BigDecimal subtract = goodsPrice.subtract(goodsPrice.multiply(gradeRate));
            //计算订单金额
            payTotal = payTotal.add(goodsPrice.multiply(gradeRate)).add(yunfei);
            //实际付款金额，最小也要0.01
            if (payTotal.compareTo(new BigDecimal("0.01")) < 0)
            {
                payTotal = new BigDecimal("0.01");
            }
            //门点自提信息
            Map<String, Object> mdztInfo;
            //门店自提结算
            int shopStatus = 0;
            //店铺信息
            MchStoreModel mchStoreModel = null;
            int           mchId         = (int) products.get(0).get("shop_id");
            if (products.size() == 1)
            {
                if (vo.getShopAddressId() != null)
                {
                    mdztInfo = publicMchService.settlement(storeId, mchId, "", vo.getShopAddressId(), vo.getStoreType());
                    shopStatus = (int) mdztInfo.get("shop_status");
                    mchStoreModel = (MchStoreModel) mdztInfo.get("mch_store_info");
                }
            }
            SecondsActivityModel secondsActivityModel = secondsActivityModelMapper.selectByPrimaryKey(vo.getMainId());
            if (secondsActivityModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_MSSPBCZ, "秒杀商品不存在");
            }
            //获取秒杀限购 默认不限购
            int                buyNum             = 0;
            SecondsConfigModel secondsConfigModel = new SecondsConfigModel();
            secondsConfigModel.setStore_id(storeId);
            secondsConfigModel.setMch_id(zyMchId);
            secondsConfigModel = secondsConfigModelMapper.selectOne(secondsConfigModel);
            if (secondsConfigModel != null && secondsConfigModel.getBuy_num() > 0)
            {
                buyNum = secondsConfigModel.getBuy_num();
            }
            if (buyNum > 0)
            {
                //已购买数量
                int secNum = secondsProModelMapper.getUserSecNum(secondsActivityModel.getId(), user.getUser_id());
                //限购-已购-需购买数量
                if (buyNum - secNum - needNum < 0)
                {
                    //超过限购数量
                    //throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DDMSSPSX, "超过该商品秒杀限购数量上限，每人限购%s");
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.NO_LICENSE_DOMAIN, String.format("超过该商品秒杀限购数量上限,每人限购%s件", buyNum));
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
            // 商品总价
            resultMap.put("vipPrice", goodsPrice.multiply(gradeRate));
            // 用户余额
            resultMap.put("user_money", user.getMoney());
            // 实际支付金额
            resultMap.put("total", payTotal);
            resultMap.put("freight", yunfei);
            // 用户地址
            resultMap.put("address", userAddress == null ? new UserAddress() : userAddress);
            // 是否有收货地址
            resultMap.put("addemt", addemt);
            // 平台优惠
            resultMap.put("preferential_amount", 0);
            // 会员等级金额
            resultMap.put("grade_rate_amount", 0);
            // 会员等级折扣
            resultMap.put("grade_rate", gradeRate);
            // 会员优惠价格
            resultMap.put("grade_money", subtract);
            //秒杀活动结束时间
            resultMap.put("remainingTime", secondsActivityModel.getEndtime().getTime());
            return resultMap;
        }
        catch (LaiKeAPIException e)
        {
            logger.debug("秒杀订单结算 自定义异常", e);
            throw e;
        }
        catch (Exception e)
        {
            logger.debug("秒杀订单结算 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "settlement");
        }

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> payment(OrderVo vo)
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        String              userId    = "";
        // Redis 预扣库存是否成功，失败/异常时用于回滚缓存库存
        boolean redisStockDeducted = false;
        int     rollbackSecId      = 0;
        int     rollbackAttrId     = 0;
        int     rollbackStockNum   = 0;
        try
        {
            // 1.数据准备
            // 获取用户信息 直接从redis 里面取 余额、收货信息
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            //刷新缓存
            user = userBaseMapper.selectByPrimaryKey(user.getId());
            // 用户id
            userId = user.getUser_id();
            RedisDataTool.refreshRedisUserCache(vo.getAccessId(), user, redisUtil);
            int storeId = vo.getStoreId();
            //获取商城自营店id
            Integer zyMchId = customerModelMapper.getStoreMchId(storeId);
            // 商品总价
            BigDecimal productsTotal;
            //  商品数组--------['pid'=>66,'cid'=>88]
            List<Map<String, Object>> products;
            // 用户使用积分
            int allow = vo.getAllow();
            // 门店地址id
            int shopAddressId = vo.getShopAddressId();
            //秒杀商品id
            if (vo.getMainId() == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.PARAMATER_ERROR, "参数错误");
            }
            //防止用户重复排队的标识位: 清理时间-->1.生成订单 2.超时 3.程序异常
            long increment = redisUtil.incr(GloabConst.RedisHeaderKey.SECKILL_ORDER_USER_KEY + user.getUser_id() + vo.getMainId(), 1);
            //设置过期时间
            redisUtil.expire(GloabConst.RedisHeaderKey.SECKILL_ORDER_USER_KEY + user.getUser_id() + vo.getMainId(), 5);
            if (increment > 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CFXD, "重复下单");
            }
            // 提交状态 1是再次购买 空是正常提交
            // 支付类型
            String payType = vo.getPayType();
            // 订单总价
            BigDecimal total = BigDecimal.ZERO;
            // 3.区分购物车结算和立即购买---列出选购商品
            List<Map<String, Object>> productList;
            try
            {
                productList = JSON.parseObject(vo.getProductsInfo(), new TypeReference<List<Map<String, Object>>>()
                {
                });
            }
            catch (JSONException j)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.PARAMATER_ERROR, vo.getProductsInfo() + ">参数格式不正确", "payment");
            }
            products = publiceService.productsList(productList, vo.getCarts(), vo.getBuyType(), DictionaryConst.OrdersType.ORDERS_HEADER_MS);
            if (StringUtils.isEmpty(products))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.STOCK_INSUFFICIENT, "库存不足1", "payment");
            }


            // 4.查询默认地址
            Map<String, Object> map = new HashMap<>();
            map.put("store_id", vo.getStoreId());

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

            // 5.列出商品数组-计算总价和优惠，拿商品运费ID
            Map<String, Object> productsInfo = publiceService.settlementProductsInfo(products, vo.getStoreId(), DictionaryConst.OrdersType.ORDERS_HEADER_MS);
            //运费信息
            Map<String, List<Map<String, Object>>> productsFreight = DataUtils.cast(productsInfo.get("products_freight"));
            if (productsFreight == null)
            {
                logger.debug("订单结算运费数据出错");
                throw new LaiKeAPIException(DATA_ERROR, "数据错误");
            }
            //计算会员的产品价格和订单产品总价
            products = DataUtils.cast(productsInfo.get("products"));
            if (products == null)
            {
                logger.debug("订单商品数据出错");
                throw new LaiKeAPIException(DATA_ERROR, "数据错误");
            }
            //计算会员优惠价格
            Map<String, Object> memberProductsInfo = publiceService.getMemberPrice(products, userId, vo.getStoreId());
            //拿出商品信息
            products = DataUtils.cast(memberProductsInfo.get("products"));
            if (products == null)
            {
                logger.debug("计算会员优惠价格数据出错");
                throw new LaiKeAPIException(DATA_ERROR, "数据错误");
            }
            //商品基本信息
            List<Map<String, Object>> goodsInfo = DataUtils.cast(products.get(0).get("list"));
            BigDecimal                gradeRate = DataUtils.getBigDecimalVal(memberProductsInfo, "grade_rate");
            //商品价格
            productsTotal = DataUtils.getBigDecimalVal(memberProductsInfo, "products_total");
            //秒杀价格
            BigDecimal orderPrice = DataUtils.getBigDecimalVal(goodsInfo.get(0), "secPrice");

            //计算店铺运费、总订单运费
            BigDecimal yunfei = BigDecimal.ZERO;
            productsInfo = publicOrderService.getFreight(productsFreight, products, userAddress, vo.getStoreId(), vo.getProductType());
//            productsInfo = this.getFreight(productsFreight, products, userAddress, vo.getStoreId(), vo.getMainId());
            products = DataUtils.cast(productsInfo.get("products"));
            if (products == null)
            {
                logger.debug("计算店铺运费、总订单运费 数据出错");
                throw new LaiKeAPIException(DATA_ERROR, "数据错误");
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
                yunfei = DataUtils.getBigDecimalVal(productsInfo, "yunfei", BigDecimal.ZERO);
            }

            // 定义初始化数据
            int totalNum = 0;
            int discount = 1;
            // 自提信息店铺
            int    shopStatus        = 0;
            String extractionCode    = "";
            String extractionCodeImg = "";

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
            //处理订单备注
            String remarks = "";
            if (StringUtils.isNotEmpty(vo.getRemarks()))
            {
                Map<String, String> mchRemarks = new HashMap<>(16);
                mchRemarks.put(mch_id + "", vo.getRemarks());
                remarks = JSON.toJSONString(mchRemarks);
            }
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
                //自提免邮
                yunfei = BigDecimal.ZERO;
                mobile = vo.getFullPhone();
                name = vo.getFullName();
            }

            // 生成订单号
            String sNo = publicOrderService.createOrderNo(vo.getType());
            // 生成支付订单号
            String realSno = publicOrderService.createOrderNo(vo.getType());
            String mchId   = "";
            //秒杀插件只会有一个商品
            int attrId = 0;
            //获取秒杀商品信息
            SecondsActivityModel secondsActivityModel = new SecondsActivityModel();
            secondsActivityModel.setId(vo.getMainId());
            secondsActivityModel.setStore_id(vo.getStoreId());
            secondsActivityModel.setIs_delete(DictionaryConst.ProductRecycle.NOT_STATUS);
            secondsActivityModel = secondsActivityModelMapper.selectOne(secondsActivityModel);
            if (secondsActivityModel == null)
            {
                throw new LaiKeAPIException(PLUGIN_SECONDS_GOODS_NOT_EXIST, "秒杀活动不存在");
            }
            //自提订单不需要判断地址
            if (vo.getShopAddressId() == 0)
            {
                int pid = secondsActivityModel.getGoodsId();
                //根据id获取运费信息
                //根据id查询商品表  新增不配送区域判断
                ProductListModel productListModel = productListModelMapper.selectByPrimaryKey(pid);
                FreightModel     freightModel     = freightModelMapper.selectByPrimaryKey(productListModel.getFreight());
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
            //秒杀配置信息
            SecondsConfigModel secondsConfigModel = new SecondsConfigModel();
            secondsConfigModel.setStore_id(vo.getStoreId());
            secondsConfigModel.setMch_id(zyMchId);
            secondsConfigModel = secondsConfigModelMapper.selectOne(secondsConfigModel);
            BigDecimal preferential_amount = BigDecimal.ZERO;

            // 先预计算本次下单的秒杀总数量，避免后续并发扣减时再动态统计
            for (Map<String, Object> mchProduct : products)
            {
                List<Map<String, Object>> onlyProducts = DataUtils.cast(mchProduct.get("list"));
                if (onlyProducts == null)
                {
                    throw new LaiKeAPIException(DATA_ERROR, "数据异常");
                }
                for (Map<String, Object> product : onlyProducts)
                {
                    int cid = DataUtils.getIntegerVal(product, "cid");
                    int num = DataUtils.getIntegerVal(product, "num");
                    if (num <= 0)
                    {
                        throw new LaiKeAPIException(PARAMATER_ERROR, "秒杀数量异常");
                    }
                    totalNum += num;
                    if (attrId == 0)
                    {
                        attrId = cid;
                    }
                    else if (attrId != cid)
                    {
                        throw new LaiKeAPIException(PARAMATER_ERROR, "秒杀订单暂不支持多规格同时下单");
                    }
                }
            }
            if (totalNum <= 0 || attrId <= 0)
            {
                throw new LaiKeAPIException(PARAMATER_ERROR, "秒杀数量或规格异常");
            }

            SecondsActivityModel activityModel = secondsActivityModelMapper.selectByPrimaryKey(vo.getMainId());
            if (activityModel == null)
            {
                throw new LaiKeAPIException(PLUGIN_SECONDS_GOODS_NOT_EXIST, "秒杀商品不存在");
            }
            //获取秒杀限购 默认不限购
            int buyNum = 0;
            if (secondsConfigModel != null && secondsConfigModel.getBuy_num() > 0)
            {
                buyNum = secondsConfigModel.getBuy_num();
            }
            if (buyNum > 0)
            {
                //已购买数量
                int secNum = secondsProModelMapper.getUserSecNum(secondsActivityModel.getId(), user.getUser_id());
                //限购-已购-需购买数量
                if (buyNum - secNum - totalNum < 0)
                {
                    //超过限购数量
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DDMSSPSX,
                            "超过该商品秒杀限购数量上限,可秒杀数量:" + (buyNum - secNum) + "件");
                }
            }
            // 快速失败校验（最终是否超卖以数据库原子扣减为准）
            if (totalNum > activityModel.getNum())
            {
                throw new LaiKeAPIException(PLUGIN_SECONDS_STOCK_OUT, "库存不足");
            }
            //刷新数据
            secondsActivityModel = secondsActivityModelMapper.selectByPrimaryKey(secondsActivityModel.getId());
            if (secondsActivityModel == null)
            {
                throw new LaiKeAPIException(PLUGIN_SECONDS_GOODS_NOT_EXIST, "秒杀活动不存在");
            }
            int status = publicSecondsService.getSecondsStatus(
                    vo.getStoreId(),
                    secondsActivityModel.getId(),
                    activityModel.getNum(),
                    secondsActivityModel.getStarttime(),
                    secondsActivityModel.getEndtime(),
                    true
            );
            if (status != SecondsProModel.SecondsStatus.SECKILL_STATUS_START)
            {
                switch (status)
                {
                    case SecondsProModel.SecondsStatus.SECKILL_STATUS_NOT_START:
                        throw new LaiKeAPIException(PARAMATER_ERROR, "活动未开始");
                    case SecondsProModel.SecondsStatus.SECKILL_STATUS_END:
                        throw new LaiKeAPIException(PARAMATER_ERROR, "活动已经结束");
                    default:
                        throw new LaiKeAPIException(PARAMATER_ERROR, "活动马上开始,请稍后再来");
                }
            }

            // 1) Redis 原子预扣（用于高并发快速失败），缓存缺失时退化到DB原子扣减
            Long redisDeductRet = tryDeductSeckillStockFromRedis(vo.getMainId(), attrId, totalNum);
            if (redisDeductRet != null && redisDeductRet == -1L)
            {
                throw new LaiKeAPIException(PLUGIN_SECONDS_STOCK_OUT, "秒杀库存不足");
            }
            if (redisDeductRet != null && redisDeductRet >= 0L)
            {
                redisStockDeducted = true;
                rollbackSecId = vo.getMainId();
                rollbackAttrId = attrId;
                rollbackStockNum = totalNum;
            }

            // 2) DB 原子扣减（最终防超卖保障）
            int row = secondsActivityModelMapper.deductStockNum(secondsActivityModel.getId(), totalNum);
            if (row < 1)
            {
                if (redisStockDeducted)
                {
                    rollbackSeckillStockToRedis(rollbackSecId, rollbackAttrId, rollbackStockNum);
                    redisStockDeducted = false;
                }
                logger.error("秒杀商品总库存扣减失败 -{}", totalNum);
                throw new LaiKeAPIException(PLUGIN_SECONDS_STOCK_OUT, "秒杀库存不足");
            }
            // 更新秒杀活动缓存库存（展示用途，失败不影响主流程）
            refreshSeckillGoodsCacheStock(secondsActivityModel.getLabel_id(), vo.getMainId(), totalNum);

            for (Map<String, Object> mchProduct : products)
            {
                String shopId = String.valueOf(mchProduct.get("shop_id"));
                mchId = mchId + shopId + SplitUtils.DH;
                //添加一条购买记录
                MchBrowseModel mchBrowseModel = new MchBrowseModel();
                mchBrowseModel.setMch_id(mchId);
                mchBrowseModel.setStore_id(storeId);
                mchBrowseModel.setUser_id(userId);
                mchBrowseModel.setEvent("购买了秒杀商品");
                mchBrowseModel.setAdd_time(new Date());
                mchBrowseModelMapper.insertSelective(mchBrowseModel);
                List<Map<String, Object>> onlyProducts = DataUtils.cast(mchProduct.get("list"));
                if (onlyProducts == null)
                {
                    throw new LaiKeAPIException(DATA_ERROR, "数据异常");
                }

                for (Map<String, Object> product : onlyProducts)
                {
                    int    pid          = DataUtils.getIntegerVal(product, "pid");
                    int    cid          = attrId = DataUtils.getIntegerVal(product, "cid");
                    int    num          = DataUtils.getIntegerVal(product, "num");
                    String productTitle = DataUtils.getStringVal(product, "product_title");
                    String unit         = DataUtils.getStringVal(product, "unit");
                    String size         = DataUtils.getStringVal(product, "size");

                    BigDecimal freightPrice = DataUtils.getBigDecimalVal(product, "freight_price");
                    // 循环插入订单附表 ，添加不同的订单详情
                    freightPrice = vo.getShopAddressId() != 0 ? BigDecimal.ZERO : freightPrice;

                    OrderDetailsModel orderDetailsModel = new OrderDetailsModel();
                    orderDetailsModel.setStore_id(storeId);
                    orderDetailsModel.setUser_id(userId);
                    //秒杀商品id
                    orderDetailsModel.setP_id(vo.getMainId());
                    orderDetailsModel.setP_name(productTitle);
                    orderDetailsModel.setP_price(orderPrice);
                    orderDetailsModel.setNum(num);
                    orderDetailsModel.setUnit(unit);
                    orderDetailsModel.setR_sNo(sNo);
                    orderDetailsModel.setAdd_time(new Date());
                    orderDetailsModel.setR_status(OrderModel.ORDER_UNPAY);
                    orderDetailsModel.setSize(size);
                    orderDetailsModel.setMch_id(Integer.parseInt(shopId));
                    orderDetailsModel.setSid(cid + "");
                    orderDetailsModel.setFreight(freightPrice);
                    orderDetailsModel.setSettlement_type(0);
                    orderDetailsModel.setRecycle(0);
                    //秒杀在添加的时候已经出库
//                    publicStockService.outStockNum(vo.getStoreId(), pid, cid, num);
                    //秒杀记录
                    SecondsRecordModel secondsRecordSave = new SecondsRecordModel();
                    secondsRecordSave.setStore_id(vo.getStoreId());
                    secondsRecordSave.setUser_id(userId);
                    secondsRecordSave.setSec_id(vo.getMainId());
                    secondsRecordSave.setPrice(freightPrice.add(orderPrice));
                    secondsRecordSave.setNum(num);
                    secondsRecordSave.setIs_delete(0);
                    secondsRecordSave.setPro_id(pid);
                    secondsRecordSave.setAttr_id(cid);
                    secondsRecordSave.setsNo(sNo);
                    secondsRecordSave.setActivity_id(secondsActivityModel.getId());
                    secondsRecordSave.setTime_id(0);
                    secondsRecordSave.setAdd_time(new Date());
                    int beres = secondsRecordModelMapper.insertSelective(secondsRecordSave);
                    if (beres < 1)
                    {
                        throw new LaiKeAPIException(ORDER_FAILED_TRY_AGAIN_LATER, "下单失败,请稍后再试", "payment");
                    }
                    //计算订单总价
                    orderDetailsModel.setAfter_discount(total = total.add(orderPrice.multiply(new BigDecimal(num))));

                    beres = orderDetailsModelMapper.insertSelective(orderDetailsModel);
                    // 如果添加失败
                    if (beres < 1)
                    {
                        throw new LaiKeAPIException(ORDER_FAILED_TRY_AGAIN_LATER, "下单失败,请稍后再试", "payment");
                    }
                }
            }
            total = total.add(yunfei);

            mchId = com.laiketui.core.utils.tool.StringUtils.rtrim(mchId, SplitUtils.DH);
            //获取订单配置
            Map<String, Object> configMap = publicOrderService.getOrderConfig(vo.getStoreId(), zyMchId, DictionaryConst.OrdersType.ORDERS_HEADER_MS);
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
            mchId = SplitUtils.DH + mchId + SplitUtils.DH;

            OrderModel orderModel = new OrderModel();
            orderModel.setStore_id(storeId);
            orderModel.setUser_id(userId);
            orderModel.setName(name);
            orderModel.setMobile(mobile);
            orderModel.setNum(totalNum);
            orderModel.setZ_price(total);
            orderModel.setOld_total(orderModel.getZ_price());
            orderModel.setsNo(sNo);
            orderModel.setSheng(sheng);
            orderModel.setShi(shi);
            orderModel.setXian(xian);
            orderModel.setAddress(addressXq);
            orderModel.setPay(payType);
            orderModel.setAdd_time(new Date());
            orderModel.setSubtraction_id(subtractionId);
            orderModel.setConsumer_money(allow);
            orderModel.setCoupon_activity_name(reduceNameArray);
            orderModel.setSpz_price(productsTotal);
            orderModel.setStatus(OrderModel.ORDER_UNPAY);
            orderModel.setReduce_price(reduceMoney);
            orderModel.setCoupon_price(couponMoney);
            orderModel.setSource(vo.getStoreType());
            orderModel.setOtype(DictionaryConst.OrdersType.ORDERS_HEADER_MS);
            orderModel.setMch_id(mchId);
            orderModel.setP_sNo("");
            orderModel.setBargain_id(0);
            orderModel.setComm_discount(new BigDecimal(discount));
            orderModel.setRemarks(remarks);
            orderModel.setReal_sno(realSno);
            orderModel.setSelf_lifting(shopStatus);
            orderModel.setExtraction_code(extractionCode);
            orderModel.setExtraction_code_img(extractionCodeImg);
            orderModel.setGrade_rate(gradeRate);
            orderModel.setZ_freight(yunfei);
            orderModel.setOld_freight(yunfei);
            orderModel.setPreferential_amount(preferential_amount);
            orderModel.setSingle_store(shopAddressId);
            orderModel.setReadd(OrderModel.READD_UNREAD);
            orderModel.setZhekou(BigDecimal.ZERO);
            orderModel.setRecycle(0);
            orderModel.setPick_up_store(0);
            orderModel.setOrderFailureTime(orderFailureDay);
            //货币信息[用户支付的货币信息]前提是商城默认币种不设置以后不允许修改
            orderModel.setCurrency_code(vo.getCurrency_code());
            orderModel.setCurrency_symbol(vo.getCurrency_symbol());
            orderModel.setExchange_rate(vo.getExchange_rate());
            orderModelMapper.insertSelective(orderModel);
            mchId = com.laiketui.core.utils.tool.StringUtils.trim(mchId, SplitUtils.DH);
            MessageLoggingModal messageLoggingSave = new MessageLoggingModal();
            messageLoggingSave.setMch_id(Integer.valueOf(mchId));
            messageLoggingSave.setStore_id(vo.getStoreId());
            messageLoggingSave.setType(MessageLoggingModal.Type.TYPE_ORDER_NEW);
            messageLoggingSave.setTo_url(OrderDataUtils.getOrderRoute(DictionaryConst.OrdersType.ORDERS_HEADER_MS, 0));
            messageLoggingSave.setParameter(orderModel.getId() + "");
            messageLoggingSave.setContent(String.format("您来新订单了，订单为%s，请及时处理！", orderModel.getsNo()));
            messageLoggingSave.setAdd_date(new Date());
            messageLoggingModalMapper.insertSelective(messageLoggingSave);
            int orderId = orderModel.getId();
            if (orderId >= 0)
            {
                //订单号
                resultMap.put("sNo", sNo);
                //订单总支付金额
                resultMap.put("total", total);
                //订单id
                resultMap.put("order_id", orderId);
                //下单时间
                resultMap.put("orderTime", DateUtil.dateFormate(orderModel.getAdd_time(), GloabConst.TimePattern.YMDHMS));
                return resultMap;
            }
            else
            {
                throw new LaiKeAPIException(ORDER_FAILED_TRY_AGAIN_LATER, "下单失败,请稍后再试", "payment");
            }
        }
        catch (LaiKeAPIException e)
        {
            if (redisStockDeducted)
            {
                rollbackSeckillStockToRedis(rollbackSecId, rollbackAttrId, rollbackStockNum);
            }
            logger.error("秒杀下单 异常", e);
            throw e;
        }
        catch (Exception e)
        {
            if (redisStockDeducted)
            {
                rollbackSeckillStockToRedis(rollbackSecId, rollbackAttrId, rollbackStockNum);
            }
            logger.error("秒杀下单 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.BUSY_NETWORK, "下单失败", "payment");
        }
        finally
        {
            //释放锁
            redisUtil.del(GloabConst.RedisHeaderKey.SECKILL_ORDER_USER_KEY + userId + vo.getMainId());
        }
    }

    /**
     * 秒杀库存 Redis 原子预扣减
     * <p>
     * 返回值：
     * <br>-1: 库存不足
     * <br>-2: 参数非法
     * <br>-3: 缓存不存在（降级走DB原子扣减）
     * <br>>=0: 扣减成功后的剩余库存
     */
    private Long tryDeductSeckillStockFromRedis(Integer secId, Integer attrId, int needNum)
    {
        if (secId == null || attrId == null || needNum <= 0)
        {
            return -2L;
        }
        if (stringRedisTemplate == null)
        {
            return null;
        }
        String goodsAttrNumKey = GloabConst.RedisHeaderKey.SECKILL_GOODS_ATTR_NUM_KEY + secId;
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setResultType(Long.class);
        script.setScriptText(
                "local key = KEYS[1]\n" +
                        "local field = ARGV[1]\n" +
                        "local need = tonumber(ARGV[2])\n" +
                        "if not need or need <= 0 then return -2 end\n" +
                        "if redis.call('HEXISTS', key, field) == 0 then return -3 end\n" +
                        "local stock = tonumber(redis.call('HGET', key, field))\n" +
                        "if not stock then return -3 end\n" +
                        "if stock < need then return -1 end\n" +
                        "return redis.call('HINCRBY', key, field, 0 - need)"
        );
        try
        {
            return stringRedisTemplate.execute(
                    script,
                    Collections.singletonList(goodsAttrNumKey),
                    String.valueOf(attrId),
                    String.valueOf(needNum)
            );
        }
        catch (Exception e)
        {
            logger.warn("秒杀Redis库存预扣减异常，降级为DB原子扣减。secId={}, attrId={}, needNum={}, error={}",
                    secId, attrId, needNum, e.getMessage());
            return null;
        }
    }

    /**
     * 秒杀库存 Redis 回滚
     */
    private void rollbackSeckillStockToRedis(int secId, int attrId, int rollbackNum)
    {
        if (secId <= 0 || attrId <= 0 || rollbackNum <= 0)
        {
            return;
        }
        try
        {
            String goodsAttrNumKey = GloabConst.RedisHeaderKey.SECKILL_GOODS_ATTR_NUM_KEY + secId;
            redisUtil.hincr(goodsAttrNumKey, String.valueOf(attrId), rollbackNum);
        }
        catch (Exception e)
        {
            logger.error("秒杀Redis库存回滚失败，secId={}, attrId={}, rollbackNum={}", secId, attrId, rollbackNum, e);
        }
    }

    /**
     * 更新秒杀商品缓存库存（仅用于前端展示，失败不影响主流程）
     */
    private void refreshSeckillGoodsCacheStock(String labelId, Integer secGoodsId, int deductNum)
    {
        if (StringUtils.isEmpty(labelId) || secGoodsId == null || deductNum <= 0)
        {
            return;
        }
        try
        {
            String goodsKey = GloabConst.RedisHeaderKey.SECKILL_GOODS_KEY + labelId;
            Object cacheObj = redisUtil.hget(goodsKey, secGoodsId.toString());
            if (!(cacheObj instanceof Map))
            {
                return;
            }
            Map<String, Object> secGoodsMap = (Map<String, Object>) cacheObj;
            BigDecimal oldStock = DataUtils.getBigDecimalVal(secGoodsMap, "secStockNum", BigDecimal.ZERO);
            BigDecimal newStock = oldStock.subtract(BigDecimal.valueOf(deductNum));
            if (newStock.compareTo(BigDecimal.ZERO) < 0)
            {
                newStock = BigDecimal.ZERO;
            }
            secGoodsMap.put("secStockNum", newStock);
            redisUtil.hset(goodsKey, secGoodsId.toString(), secGoodsMap);
        }
        catch (Exception e)
        {
            logger.warn("刷新秒杀展示库存失败，labelId={}, secGoodsId={}, deductNum={}", labelId, secGoodsId, deductNum);
        }
    }

    /**
     * 计算秒杀运费
     *
     * @param freightMap  - 店铺运费信息
     * @param products    - 商品信息
     * @param userAddress - 用户收货地址
     * @param storeId     -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/10/21 15:06
     */
    private Map<String, Object> getFreight(Map<String, List<Map<String, Object>>> freightMap, List<Map<String, Object>> products,
                                           UserAddress userAddress, int storeId, int secProId) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            //运费
            BigDecimal yunfei = BigDecimal.ZERO;
            //商品限购数量 默认不限购
            int buyNum = 0;
            //是否开启了包邮设置
            int packageSettings = 0;
            //同件
            int                samePiece          = 0;
            SecondsConfigModel secondsConfigModel = new SecondsConfigModel();
            secondsConfigModel.setStore_id(storeId);
            secondsConfigModel.setMch_id(MapUtils.getInteger(products.get(0), "shop_id"));
            secondsConfigModel = secondsConfigModelMapper.selectOne(secondsConfigModel);
            if (secondsConfigModel != null)
            {
                packageSettings = secondsConfigModel.getPackage_settings();
                if (packageSettings == 1)
                {
                    samePiece = secondsConfigModel.getSame_piece();
                    buyNum = secondsConfigModel.getBuy_num();
                }
            }
            //是否超过限购数量
            int secNum = secondsProModelMapper.getUserSecNum(secProId, userAddress.getUid());
            if (buyNum > 0 && secNum > buyNum)
            {
                throw new LaiKeAPIException(PARAMATER_ERROR, "下单失败,超过限购数量!");
            }
            //店铺id
            Set<String> keys = freightMap.keySet();
            //是否免邮
            boolean orderYunfei = false;
            //是否开启包邮设置
            for (String key : keys)
            {
                List<Map<String, Object>> productFreights = freightMap.get(key);
                for (Map<String, Object> productFreight : productFreights)
                {
                    if (packageSettings == 1)
                    {
                        //是否满足同件免邮规则
                        int num = MapUtils.getInteger(productFreight, "num");
                        //是否需要运费
                        orderYunfei = samePiece <= num;
                        //秒杀限购数量
                        if (buyNum > 0 && (num + secNum) > buyNum)
                        {
                            throw new LaiKeAPIException(PARAMATER_ERROR, "下单失败,超过限购数量!");
                        }
                    }
                    productFreight.put("order_yunfei", orderYunfei);
                }
            }
            if (orderYunfei)
            {
                //满足免邮条件
                for (Map<String, Object> product : products)
                {
                    product.put("freight", 0.0);
                }
            }
            else
            {
                //各个店铺下商品所需的运费
                Map<String, Object> freightIdMap = new HashMap<>(16);
                for (String key : keys)
                {
                    //运费信息
                    resultMap.put("freight_id", freightIdMap);
                    List<Map<String, Object>> productFreights = freightMap.get(key);
                    for (Map<String, Object> productFreight : productFreights)
                    {
                        //获取运费模版id
                        Integer      freightIdObj = MapUtils.getInteger(productFreight, "freight_id");
                        FreightModel freightModel = new FreightModel();
                        if (freightIdObj != null)
                        {
                            BigDecimal freightId = BigDecimal.valueOf(freightIdObj);
                            freightModel.setId(freightId.intValue());
                        }
                        else
                        {
                            freightModel.setMch_id(Integer.parseInt(key));
                            freightModel.setStore_id(storeId);
                            freightModel.setIs_default(1);
                        }
                        freightModel = freightModelMapper.selectOne(freightModel);
                        if (freightModel != null)
                        {
                            //获取运费
                            BigDecimal goodsYunFei = publicOrderService.getFreight(freightModel.getId(), userAddress);
                            //计算总运费
                            yunfei = yunfei.add(goodsYunFei);
                            List<Double> mchProductFreigths = new ArrayList<>();
                            mchProductFreigths.add(goodsYunFei.doubleValue());
                            freightIdMap.put(key, mchProductFreigths);
                        }
                    }
                }
            }
            resultMap.put("yunfei", yunfei);
            resultMap.put("freight_ids", resultMap.get("freight_id"));

            Map<String, List<Double>> freight_ids = DataUtils.cast(resultMap.get("freight_ids"));
            //计算各个店铺下商品的运费
            for (Map<String, Object> mchProduct : products)
            {
                BigDecimal freight_price_total = BigDecimal.ZERO;
                BigDecimal products_num        = BigDecimal.ZERO;
                int        shop_id             = (int) mchProduct.get("shop_id");

                List<Map<String, Object>> onlyProductsInfo = DataUtils.cast(mchProduct.get("list"));
                if (onlyProductsInfo == null)
                {
                    onlyProductsInfo = new ArrayList<>();
                }
                //纯商品信息
                int pos = 0;
                for (Map<String, Object> productInfo : onlyProductsInfo)
                {
                    BigDecimal freight_price = BigDecimal.ZERO;
                    products_num = products_num.add(new BigDecimal(productInfo.get("num") + ""));
                    if (freight_ids != null)
                    {
                        List<Double> shopGoodsYunFeiList = freight_ids.get(shop_id + "");
                        logger.debug("计算订单运费 当前店铺{},当前店铺商品集运费{}", shop_id, JSON.toJSONString(shopGoodsYunFeiList));
                        if (shopGoodsYunFeiList != null && shopGoodsYunFeiList.size() > pos)
                        {
                            freight_price = freight_price.add(new BigDecimal(shopGoodsYunFeiList.get(pos++).toString()));
                            freight_price_total = freight_price_total.add(freight_price);
                        }
                    }
                    productInfo.put("freight_price", freight_price);
                }
                mchProduct.put("freight_price", DoubleFormatUtil.format(freight_price_total.doubleValue()));
                mchProduct.put("products_num", products_num);
                Double mchProductTotal = (Double) mchProduct.get("product_total");
                mchProduct.put("product_total", DoubleFormatUtil.format(mchProductTotal));
            }
            resultMap.put("products", products);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("计算秒杀运费 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.BUSY_NETWORK, "商品信息不完整", "getFreight");
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
        return null;
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
            parmaMap.put("keyWord", vo.getKeyword());
            parmaMap.put("orderType", vo.getQueryOrderType());
            parmaMap.put("start", vo.getPageNo());
            parmaMap.put("pageSize", vo.getPageSize());
            parmaMap.put("otype", DictionaryConst.OrdersType.ORDERS_HEADER_MS);
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
                BigDecimal totalYunFei = new BigDecimal("0");
                //订单状态
                Integer status = MapUtils.getInteger(orderInfo, "status");
                //订单类型
                String oType = MapUtils.getString(orderInfo, "otype");
                Integer selfLifting = MapUtils.getInteger(orderInfo, "self_lifting");
                //商品总数量
                BigDecimal totalNeedNum = new BigDecimal("0");
                //订单号
                String orderno = orderInfo.get("sNo").toString();
                //店铺id
                int mchId = 0;
                // 店铺 名称
                String mchName = "";
                // 店铺logo
                String logoUrl = "";
                //店铺头像
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
                    if (mchModel.getHead_img() != null)
                    {
                        headImg = publiceService.getImgPath(mchModel.getHead_img(), vo.getStoreId());
                    }
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
                        throw new LaiKeAPIException(DATA_NOT_EXIST, "商品不存在");
                    }
                    //获取商品信息
                    ProductListModel productListModel = productListModelMapper.selectByPrimaryKey(confiGureModel.getPid());
                    if (Objects.nonNull(productListModel))
                    {
                        orderDetails.put("pro_id", productListModel.getId());
                    }
                    orderDetails.put("imgurl", publiceService.getImgPath(confiGureModel.getImg(), vo.getStoreId()));
                    //评价按钮
                    int orderCommentType = publicOrderService.orderCommentType(vo.getStoreId(), MapUtils.getString(orderDetails, "user_id"), MapUtils.getString(orderDetails, "r_sNo"), MapUtils.getInteger(orderDetails, "id"), MapUtils.getInteger(orderDetails, "sid"), MapUtils.getIntValue(orderDetails, "r_status"));
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
                //所有商品都已经评价了
                if (!comment)
                {
                    commentsType = 3;
                }
                String sNo = MapUtils.getString(orderInfo, "sNo");
                int unFinishShouHouOrderNum = returnOrderModelMapper.orderReturnIsNotEnd(vo.getStoreId(), sNo);
                OrderModel orderModel = new OrderModel();
                orderModel.setStore_id(vo.getStoreId());
                orderModel.setsNo(sNo);
                orderModel = orderModelMapper.selectOne(orderModel);
                orderModel = orderModelMapper.selectOne(orderModel);
                Map<String, Boolean> buttonShow  = publicRefundService.afterSaleButtonShow(vo.getStoreId(), orderModel.getOtype(), detailList, orderModel, unFinishShouHouOrderNum);
                Boolean refund = MapUtils.getBoolean(buttonShow, "refund");
                Boolean  refundShowBtn = MapUtils.getBoolean(buttonShow, "refundShowBtn");
                Map<String,Object> resMap = new HashMap<>();
                Map<String,Object> paramMap = new HashMap<>();
                paramMap.put("status",status);
                paramMap.put("self_lifting",selfLifting);
                paramMap.put("invoiceTimeout",invoiceTimeout);
                paramMap.put("isInvoice",isInvoice);
                paramMap.put("orderCommentType",commentsType);
                paramMap.put("unFinishShouHouOrderNum",unFinishShouHouOrderNum);
                paramMap.put("haveExpress",haveExpress);
                paramMap.put("refundShowBtn",refundShowBtn);
                paramMap.put("refund",refund);
                paramMap.put("count",detailList.size());
                orderProcessor.processOrder(oType,resMap,paramMap);
                String keys = String.format(GloabConst.RedisHeaderKey.ORDER_SHOW_VALUE_KEY, sNo);
                redisUtil.del(keys);
                redisUtil.set(keys,resMap,5 * 60);
                orderInfo.put("get_button_list",resMap);
                orderInfo.put("refund", true);
                orderInfo.put("list", orderDetailsModelList);
                orderInfo.put("z_freight", totalYunFei);
                orderInfo.put("sum", totalNeedNum);

            }
            resultMap.put("order", orderList);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("秒杀订单列表 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.BUSY_NETWORK, "网络异常", "seckillOrder");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> remindDelivery(OrderVo vo)
    {
        return null;
    }

    @Override
    public Map<String, Object> cancleOrder(OrderVo vo)
    {
        try
        {
            OrderModel orderOld = new OrderModel();
            orderOld.setId(vo.getOrderId());
            orderOld.setStore_id(vo.getStoreId());
            orderOld.setOtype(DictionaryConst.OrdersType.ORDERS_HEADER_MS);
            orderOld.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS);
            orderOld = orderModelMapper.selectOne(orderOld);
            if (orderOld == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.DATA_NOT_EXIST, "订单不存在");
            }
            if (!orderOld.getStatus().equals(DictionaryConst.OrdersStatus.ORDERS_R_STATUS_UNPAID))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.OPERATION_FAILED, "订单不在待付款状态");
            }
            //秒杀记录
            SecondsRecordModel secondsRecordModel = new SecondsRecordModel();
            secondsRecordModel.setStore_id(vo.getStoreId());
            secondsRecordModel.setsNo(orderOld.getsNo());
            secondsRecordModel.setIs_delete(0);
            secondsRecordModel = secondsRecordModelMapper.selectOne(secondsRecordModel);
            if (secondsRecordModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.DATA_NOT_EXIST, "秒杀订单数据异常");
            }
            //关闭订单
            OrderModel orderUpdate = new OrderModel();
            orderUpdate.setId(orderOld.getId());
            orderUpdate.setStatus(DictionaryConst.OrdersStatus.ORDERS_R_STATUS_CLOSE);
            orderUpdate.setCancel_method(DictionaryConst.CancelMethod.SELF);
            int row = orderModelMapper.updateByPrimaryKeySelective(orderUpdate);
            if (row < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.OPERATION_FAILED, "订单关闭失败");
            }
            //关闭明细
            row = orderDetailsModelMapper.closeOrder(orderOld.getsNo());
            if (row < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.OPERATION_FAILED, "订单关闭失败");
            }
            //回滚库存
            secondsRecordModelMapper.releaseStockNum(orderOld.getsNo());
            //扣减商品销量
            if (DictionaryConst.OrdersStatus.ORDERS_R_STATUS_CONSIGNMENT <= orderOld.getStatus())
            {
                productListModelMapper.updateProductListVolume(-secondsRecordModel.getNum(), vo.getStoreId(), secondsRecordModel.getPro_id());
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("秒杀关闭订单 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.BUSY_NETWORK, "网络异常", "cancleOrder");
        }
        return null;
    }

    @Override
    public Map<String, Object> loadMore(OrderVo vo)
    {
        return null;
    }

    @Override
    public Map<String, Object> delOrder(OrderVo vo)
    {
        try
        {
            if (StringUtils.isEmpty(vo.getOrdervalue()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.PARAMATER_ERROR, "参数错误");
            }
            String[] orderIds = vo.getOrdervalue().split(SplitUtils.DH);
            for (String orderId : orderIds)
            {
                OrderModel orderOld = new OrderModel();
                orderOld.setId(Integer.parseInt(orderId));
                orderOld.setStore_id(vo.getStoreId());
                orderOld.setOtype(DictionaryConst.OrdersType.ORDERS_HEADER_MS);
                orderOld.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS);
                orderOld = orderModelMapper.selectOne(orderOld);
                if (orderOld == null)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.DATA_NOT_EXIST, "订单不存在");
                }
                logger.debug("正在删除订单{}", orderOld.getsNo());
                //删除订单
                OrderModel orderUpdate = new OrderModel();
                orderUpdate.setId(orderOld.getId());
                orderUpdate.setRecycle(DictionaryConst.ProductRecycle.RECOVERY);
                int row = orderModelMapper.updateByPrimaryKeySelective(orderUpdate);
                if (row < 1)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.OPERATION_FAILED, "订单关闭失败");
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
                    Integer integer = returnOrderModelMapper.orderDetailReturnIsNotEnd(vo.getStoreId(), orderOld.getsNo(), id);
                    if (integer != null && integer > 0)
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.OPERATION_FAILED, "当前订单正在售后处理,不可关闭");
                    }
                    //删除明细
                    row = orderDetailsModelMapper.delOrderDetails1(vo.getStoreId(), DictionaryConst.ProductRecycle.RECOVERY, orderOld.getsNo());
                    if (row < 1)
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.OPERATION_FAILED, "订单关闭失败");
                    }
                    if (status.equals(DictionaryConst.OrdersStatus.ORDERS_R_STATUS_COMPLETE) || status.equals(DictionaryConst.OrdersStatus.ORDERS_R_STATUS_CLOSE))
                    {
                        logger.debug("订单明细id{} 状态{} 删除订单不回滚库存", id, status);
                        continue;
                    }
                    //回滚【秒杀】库存
                    secondsRecordModelMapper.releaseStockNum(orderOld.getsNo());

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
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("秒杀删除订单 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.BUSY_NETWORK, "网络异常", "delOrder");
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
            params.put("start", vo.getPageNo());
            params.put("pageSize", vo.getPageSize());
            params.put("type", vo.getType());
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
                            if (mchModel.getHead_img() != null)
                            {
                                arr.put("headImg", publiceService.getImgPath(mchModel.getHead_img(), storeId));
                            }
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
