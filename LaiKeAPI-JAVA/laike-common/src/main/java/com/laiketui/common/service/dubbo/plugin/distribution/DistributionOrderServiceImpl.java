package com.laiketui.common.service.dubbo.plugin.distribution;

import com.alibaba.fastjson2.*;
import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import com.laiketui.common.annotation.HandlerOrderType;
import com.laiketui.common.api.*;
import com.laiketui.common.api.distribution.PubliceDistributionService;
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
import com.laiketui.core.utils.tool.SerializePhpUtils;
import com.laiketui.core.utils.tool.StringUtils;
import com.laiketui.domain.config.ConfiGureModel;
import com.laiketui.domain.distribution.*;
import com.laiketui.domain.mch.CartModel;
import com.laiketui.domain.mch.DistributionGradeModel;
import com.laiketui.domain.mch.MchBrowseModel;
import com.laiketui.domain.mch.MchModel;
import com.laiketui.domain.message.MessageLoggingModal;
import com.laiketui.domain.order.BuyAgainModal;
import com.laiketui.domain.order.OrderDetailsModel;
import com.laiketui.domain.order.OrderModel;
import com.laiketui.domain.product.ProductListModel;
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
import tk.mybatis.mapper.entity.Example;

import java.math.BigDecimal;
import java.net.URLDecoder;
import java.util.*;

/**
 * 分销订单
 *
 * @author wangxian
 * @date 2021/4/13 14:46
 */
@HandlerOrderType(type = DictionaryConst.OrdersType.ORDERS_HEADER_FX)
@Service("distributionOrderService")
public class DistributionOrderServiceImpl implements OrderDubboService
{

    private final Logger                     logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private       PubliceDistributionService publiceDistributionService;
    @Autowired
    private       PubliceService             publiceService;
    @Autowired
    private       RedisUtil                  redisUtil;
    @Autowired
    private       UserMapper                 userMapper;
    @Autowired
    private       PaymentConfigModelMapper   paymentConfigModelMapper;
    @Autowired
    private       PublicAddressService       commonAddressService;
    @Autowired
    private       PublicOrderService         publicOrderService;
    @Autowired
    private       MchBrowseModelMapper       mchBrowseModelMapper;
    @Autowired
    private       OrderDetailsModelMapper    orderDetailsModelMapper;
    @Autowired
    private       OrderModelMapper           orderModelMapper;
    @Autowired
    private       CartModelMapper            cartModelMapper;
    @Autowired
    private       BuyAgainModalMapper        buyAgainModalMapper;
    @Autowired
    private       ConfiGureModelMapper       confiGureModelMapper;
    @Autowired
    private       ProductListModelMapper     productListModelMapper;
    @Autowired
    private       StockModelMapper           stockModelMapper;

    @Autowired
    private PublicStockService publicStockService;

    @Autowired
    private DistributionGradeModelMapper distributionGradeModelMapper;

    @Autowired
    private DistributionGoodsModelMapper distributionGoodsModelMapper;

    @Autowired
    private DistributionConfigModelMapper distributionConfigModelMapper;

    @Autowired
    private UserDistributionModelMapper userDistributionModelMapper;

    @Autowired
    private DistributionRecordModelMapper distributionRecordModelMapper;

    @Autowired
    private UserBaseMapper userBaseMapper;

    @Autowired
    private ReturnOrderModelMapper returnOrderModelMapper;

    @Autowired
    private MchModelMapper mchModelMapper;

    @Autowired
    private PublicRefundService publicRefundService;

    @Autowired
    private MessageLoggingModalMapper messageLoggingModalMapper;

    @Autowired
    private FreightModelMapper freightModelMapper;

    @Autowired
    private PublicAddressService publicAddressService;

    @Autowired
    private PublicPaymentConfigService publicPaymentConfigService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> settlement(OrderVo vo)
    {
        try
        {
            // 获取用户信息 直接从redis 里面取 余额、收货信息
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            //刷新缓存
            user = userBaseMapper.selectByPrimaryKey(user.getId());
            RedisDataTool.refreshRedisUserCache(vo.getAccessId(), user, redisUtil);
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

            //获取用户的默认收货地址
            UserAddress userAddress = publicAddressService.findAddress(vo.getStoreId(), user.getUser_id(), vo.getAddressId());
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
            int     pid   = 0;
            int     cid   = 0;
            Integer secId = null;
            for (Map<String, Object> tempMap : productList)
            {
                if (tempMap.containsKey("pid"))
                {
                    pid = Integer.parseInt(tempMap.get("pid").toString());
                }
                if (tempMap.containsKey("cid"))
                {
                    cid = Integer.parseInt(tempMap.get("cid").toString());
                }
            }
            //
            List<Map<String, Object>> productsListMap = publiceService.productsList(productList, vo.getCarts(), vo.getBuyType(), DictionaryConst.OrdersType.ORDERS_HEADER_GM);
            if (CollectionUtils.isEmpty(productsListMap))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_KCBZ, "库存不足", "settlement");
            }
            //按照店铺归类的商品、运费、商品总价等信息
            Map<String, Object> productsInfo = publiceService.settlementProductsInfo(productsListMap, vo.getStoreId(), vo.getProductType());
            //运费信息
            Map<String, List<Map<String, Object>>> productsFreight = (Map<String, List<Map<String, Object>>>) productsInfo.get("products_freight");
            //计算会员的产品价格和订单产品总价
            List<Map<String, Object>> products = (List<Map<String, Object>>) productsInfo.get("products");
            //商品总价
            BigDecimal productsTotal = new BigDecimal(MapUtils.getString(productsInfo, "products_total"));
            //分销商品
            int isDistribution = 1;
            //计算店铺运费、总订单运费
            BigDecimal yunfei = BigDecimal.ZERO;
            productsInfo = publicOrderService.getFreight(productsFreight, products, userAddress, vo.getStoreId(), vo.getProductType());
            products = (List<Map<String, Object>>) productsInfo.get("products");
            yunfei = DataUtils.getBigDecimalVal(productsInfo, "yunfei");
            List shop_list = new ArrayList();
            //门店自提结算（分销无自提计算）
            int shopStatus = 0;
            // 优惠ID数组
            List<Integer> couponIdList = new ArrayList();
            //优惠券id拼接字符串
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
            String  couponName   = "";
            boolean couponStatus = DataUtils.FALSE;
            // 用户分销等级折扣
            BigDecimal discount = new BigDecimal(100);
            //折扣金额
            BigDecimal discountMoney = BigDecimal.ZERO;
            BigDecimal totalDiscount = BigDecimal.ZERO;
            BigDecimal total         = productsTotal;

            //获取分销商品的折扣设置
            DistributionGoodsModel distributionGoodsModel = new DistributionGoodsModel();
            distributionGoodsModel.setP_id(pid);
            distributionGoodsModel.setStore_id(vo.getStoreId());
            distributionGoodsModel.setS_id(cid);
            distributionGoodsModel = distributionGoodsModelMapper.selectOne(distributionGoodsModel);

            if (distributionGoodsModel == null)
            {
                logger.error("分销订单结算 异常 分销商品id={} 不存在", pid);
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "settlement");
            }

            //1等级规则 2自定义
            int distributionDiyRuletype = distributionGoodsModel.getDistribution_rule();
            //当前分销身份信息
            DistributionGradeModel distributionGrade = distributionGradeModelMapper.getUserDistribution(user);
            boolean isShowDiscount = false;
            if (distributionGrade != null)
            {
                if (DistributionGoodsModel.DistributionRuleValue.DISTRIBUTION_RULE_LEVEL == distributionDiyRuletype)
                {
                    String              sets     = distributionGrade.getSets();
                    Map<String, Object> gradeMap = SerializePhpUtils.getDistributionGradeBySets(sets);

                    if (gradeMap.containsKey("zhekou") && MapUtils.getInteger(gradeMap, "zhekou") == 1)
                    {
                        isShowDiscount = true;
                        discount = distributionGrade.getDiscount();
                        logger.info("分销等级->{}对应的分销折扣->{}", distributionGrade.getId(), discount);
                    }
                }
                else
                {
                    //当前用户身份分销等级id
                    Integer                   gid           = null;
                    List<Map<String, Object>> rulesSetArray = SerializePhpUtils.getUnserializeToList(distributionGoodsModel.getRules_set());
                    if (rulesSetArray == null || rulesSetArray.isEmpty())
                    {
                        logger.error("分销订单结算异常分销商品未设置自定义分佣规则 {} 不存在", pid);
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_FXSPWSZDIYRULE, "网络异常", "settlement");
                    }
                    gid = distributionGrade.getId();
                    for (int i = 0; i < rulesSetArray.size(); i++)
                    {
                        Map setsMap = (Map) rulesSetArray.get(i);
                        int tempGid = MapUtils.getInteger(setsMap, "id");
                        if (tempGid == gid)
                        {
                            Double gradeDiydisCount = MapUtils.getDouble(setsMap, "diy_discount");
                            discount = BigDecimal.valueOf(gradeDiydisCount);
                            logger.info("商品->{}对应的自定义分销折扣->{}", pid, gradeDiydisCount);
                            break;
                        }
                    }
                }
            }


            if (discount.compareTo(BigDecimal.ZERO) > 0)
            {
                // 商品总价 * 分销折扣 + 运费   实际付款金额
                total = productsTotal.multiply(discount).multiply(new BigDecimal("0.01")).setScale(2, BigDecimal.ROUND_HALF_UP);
                //总优惠金额
                totalDiscount = productsTotal.subtract(productsTotal.multiply(discount).multiply(new BigDecimal("0.01"))).setScale(2, BigDecimal.ROUND_HALF_UP);
                discountMoney = totalDiscount;
            }
            total = total.add(yunfei).setScale(2, BigDecimal.ROUND_HALF_UP);
            Map<String, Object> resultMap = new HashMap<>(16);
            //支付密码错误一天超过5此不允许再使用余额支付
            resultMap.put("is_distribution", isDistribution);
            resultMap.put("enterless", enterless);
            resultMap.put("isShowDiscount",isShowDiscount);
            // 自提标记 1为自提
            resultMap.put("shop_status", shopStatus);
            // 门店自提信息
            resultMap.put("shop_list", new ArrayList<>());
            // 支付方式信息状态
            resultMap.putAll(payInfo);
            // 商品列表
            resultMap.put("products", products);
            // 密码状态
            resultMap.put("password_status", passwordStatus);
            // 商品总价
            resultMap.put("products_total", productsTotal);
            // 用户余额
            resultMap.put("user_money", user.getMoney());
            // 实际支付金额
            resultMap.put("total", total);
            resultMap.put("freight", yunfei);
            // 用户地址
            resultMap.put("address", userAddress);
            // 是否有收货地址
            resultMap.put("addemt", addemt);
            // 优惠券id
            resultMap.put("coupon_id", 0);
            // 平台优惠类型的数量
            resultMap.put("coupon_num", 0);
            // 是否满减
            resultMap.put("is_subtraction", isSubtraction);
            // 优惠券列表
            resultMap.put("coupon_list", new ArrayList<>());
            // 店铺优惠
            resultMap.put("mch_preferential_amount", 0);
            // 平台优惠
            resultMap.put("preferential_amount", 0);
            // 总折扣 会员优惠总金额
            resultMap.put("total_discount", totalDiscount);
            // 会员等级金额
            resultMap.put("grade_rate_amount", 0);
            // 会员等级折扣
            resultMap.put("grade_rate", 1);
            resultMap.put("reduce_name", reduceName);
            resultMap.put("reduce_money", reduceMoney);
            // 优惠金额
            resultMap.put("coupon_money", couponMoney);
            //  优惠券状态
            resultMap.put("coupon_status", couponStatus);
            //  折扣值
            resultMap.put("discount", discount);
            //保留两位小数  禅道  50655
            //优惠金额  禅道41345  不显示购物折扣
            resultMap.put("discountMoney", false);
            //  状态 固定写成1了 ？
            resultMap.put("status", "1");
            // 优惠文字
            resultMap.put("coupon_name", couponName);
            return resultMap;
        }
        catch (LaiKeAPIException e)
        {
            logger.error("分销订单结算 错误", e);
            throw e;
        }
        catch (Exception e)
        {
            logger.error("分销订单结算 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "settlement");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> payment(OrderVo vo)
    {
        Map<String, Object> resultMap = new HashMap<>();
        try
        {
            // 1.数据准备
            // 获取用户信息 直接从redis 里面取 余额、收货信息
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil);
            //刷新缓存
            user = userBaseMapper.selectByPrimaryKey(user.getId());
            RedisDataTool.refreshRedisUserCache(vo.getAccessId(), user, redisUtil);
            int storeId = vo.getStoreId();
            // 用户id
            String userId = user.getUser_id();
            // 商品总价(折后)
            BigDecimal productsTotal = BigDecimal.ZERO;
            // 商品售价
            BigDecimal goodsPrice = BigDecimal.ZERO;
            //  商品数组--------['pid'=>66,'cid'=>88]
            List<Map<String, Object>> products = new ArrayList<>();
            // 用户使用积分
            int allow = vo.getAllow();
            // 门店地址id
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
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSGSBZQ, vo.getProductsInfo() + ">参数格式不正确", "distribution payment");
            }
            int     gpid  = 0;
            int     gcid  = 0;
            Integer secId = null;
            for (Map<String, Object> tempMap : productList)
            {
                if (tempMap.containsKey("pid"))
                {
                    gpid = Integer.parseInt(tempMap.get("pid").toString());
                }
                if (tempMap.containsKey("cid"))
                {
                    gcid = Integer.parseInt(tempMap.get("cid").toString());
                }
            }

            products = publiceService.productsList(productList, vo.getCarts(), vo.getBuyType(), DictionaryConst.OrdersType.ORDERS_HEADER_GM);
            if (CollectionUtils.isEmpty(products))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_KCBZ, "库存不足", "distribution payment");
            }
            // 4.查询默认地址
            UserAddress userAddress = publicAddressService.findAddress(vo.getStoreId(), user.getUser_id(), vo.getAddressId());
            // 存储收货信息
            String           mobile    = userAddress.getTel();
            String           name      = userAddress.getName();
            String           sheng     = userAddress.getSheng();
            String           shi       = userAddress.getCity();
            String           xian      = userAddress.getQuyu();
            String           addressXq = userAddress.getAddress();
            ProductListModel productListOld;
            for (Map<String, Object> product : products)
            {
                //获取商品信息
                productListOld = new ProductListModel();
                productListOld.setId(MapUtils.getIntValue(product, "pid"));
                productListOld.setStore_id(vo.getStoreId());
                productListOld.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS.toString());
                productListOld = productListModelMapper.selectOne(productListOld);
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
                        logger.info("地址超出配送范围");
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DZCCPSFW, "地址超出配送范围", "getNo_delivery");
                    }
                }
            }

            // 5.列出商品数组-计算总价和优惠，拿商品运费ID
            Map<String, Object> productsInfo = publiceService.settlementProductsInfo(products, vo.getStoreId(), vo.getProductType());
            //运费信息
            Map<String, List<Map<String, Object>>> productsFreight = (Map<String, List<Map<String, Object>>>) productsInfo.get("products_freight");
            //计算会员的产品价格和订单产品总价
            products = (List<Map<String, Object>>) productsInfo.get("products");
            goodsPrice = productsTotal = new BigDecimal(MapUtils.getString(productsInfo, "products_total"));
            //计算店铺运费、总订单运费
            BigDecimal yunfei = BigDecimal.ZERO;
            productsInfo = publicOrderService.getFreight(productsFreight, products, userAddress, vo.getStoreId(), vo.getProductType());
            products = (List<Map<String, Object>>) productsInfo.get("products");
            //
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
            // 自提信息店铺
            String otype = DictionaryConst.OrdersType.ORDERS_HEADER_FX;
            //订单状态 默认待付款
            int    orderStatus       = OrderModel.ORDER_UNPAY;
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
            Integer grade_l = vo.getGradeLevel();
            if (grade_l != null && grade_l != 0 && (DataUtils.equalBigDecimalZero(yunfei)))
            {
                orderStatus = OrderModel.ORDER_PAYED;
            }
            BigDecimal preferential_amount = BigDecimal.ZERO;
            // 用户分销等级折扣
            BigDecimal discount      = BigDecimal.valueOf(100);
            BigDecimal totalDiscount = BigDecimal.ZERO;
            BigDecimal total;
            //当前分销人折扣金额
            BigDecimal discountMoney = BigDecimal.ZERO;

            //获取分销商品的折扣设置
            DistributionGoodsModel distributionGoodsModel = new DistributionGoodsModel();
            distributionGoodsModel.setP_id(gpid);
            distributionGoodsModel.setStore_id(vo.getStoreId());
            distributionGoodsModel.setS_id(gcid);
            distributionGoodsModel = distributionGoodsModelMapper.selectOne(distributionGoodsModel);

            if (distributionGoodsModel == null)
            {
                logger.error("分销订单结算 异常 分销商品id={} 不存在", gpid);
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "payment");
            }

            //1等级规则 2自定义
            int distributionDiyRuletype = distributionGoodsModel.getDistribution_rule();
            //当前分销身份信息
            DistributionGradeModel distributionGrade = distributionGradeModelMapper.getUserDistribution(user);
            if (Objects.nonNull(distributionGrade))
            {
                if (DistributionGoodsModel.DistributionRuleValue.DISTRIBUTION_RULE_LEVEL == distributionDiyRuletype)
                {
                    String              sets     = distributionGrade.getSets();
                    Map<String, Object> gradeMap = SerializePhpUtils.getDistributionGradeBySets(sets);
                    // == 1 ?
                    if (gradeMap.containsKey("zhekou") && MapUtils.getInteger(gradeMap, "zhekou") == 1)
                    {
                        discount = distributionGrade.getDiscount();
                        logger.info("分销等级->{}对应的分销折扣->{}", distributionGrade.getId(), discount);
                    }

                }
                else
                {
                    Integer gid = distributionGrade.getId();
                    List<Map<String, Object>> rulesSetArray = SerializePhpUtils.getUnserializeToList(distributionGoodsModel.getRules_set());
                    if (rulesSetArray == null || rulesSetArray.isEmpty())
                    {
                        logger.error("分销订单结算异常分销商品未设置自定义分佣规则 {} 不存在", gpid);
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_FXSPWSZDIYRULE, "网络异常", "settlement");
                    }

                    for (int i = 0; i < rulesSetArray.size(); i++)
                    {
                        Map setsMap = (Map) rulesSetArray.get(i);
                        int tempGid = MapUtils.getInteger(setsMap, "id");
                        if (tempGid == gid)
                        {
                            Double gradeDiydisCount = MapUtils.getDouble(setsMap, "diy_discount");
                            discount = BigDecimal.valueOf(gradeDiydisCount);
                            logger.info("商品->{}对应的自定义分销折扣->{}", gpid, gradeDiydisCount);
                            break;
                        }
                    }
                }
            }

            // 生成订单号
            String sNo = publicOrderService.createOrderNo(vo.getType());
            // 生成支付订单号
            String realSno = publicOrderService.createOrderNo(vo.getType());
            String mchId   = "";


            if (grade_l == null)
            {
                productsTotal = BigDecimal.ZERO;
                total = yunfei;
            }
            else
            {
                total = productsTotal.multiply(discount).multiply(new BigDecimal("0.01")).setScale(2, BigDecimal.ROUND_HALF_UP); // 商品总价 * 分销折扣 - 自动满减 + 运费 - 优惠券金额     实际付款金额
                //折扣金额
                discountMoney = productsTotal.subtract(total).setScale(2, BigDecimal.ROUND_HALF_UP);
                //插件不允许使用优惠券的情况下可以使用此字段
                preferential_amount = couponMoney = preferential_amount.add(discountMoney);
                if (total.compareTo(BigDecimal.ZERO) == 0)
                {
                    total = new BigDecimal("0.01");
                }
                total = total.add(yunfei);
            }
            /**
             * 店铺订单备注
             */
            Map<String, String> mchRemarks     = new HashMap<>();
            int                 is_subtraction = 1;
            int                 pos            = 0;
            for (Map<String, Object> mchProduct : products)
            {
                String shopId = String.valueOf(mchProduct.get("shop_id"));
                mchId = mchId + shopId + SplitUtils.DH;
                if (remarkJsonarr != null)
                {
                    String tmpDesc = remarkJsonarr.getString(pos++);
                    if (!StringUtils.isEmpty(tmpDesc))
                    {
                        mchRemarks.put(shopId, tmpDesc);
                    }
                }
                //如果是多店铺，添加一条购买记录
                MchBrowseModel mchBrowseModel = new MchBrowseModel();
                mchBrowseModel.setMch_id(mchId);
                mchBrowseModel.setStore_id(storeId);
                mchBrowseModel.setUser_id(userId);
                mchBrowseModel.setEvent("购买了商品");
                mchBrowseModel.setAdd_time(new Date());
                mchBrowseModelMapper.insertSelective(mchBrowseModel);
                List<Map<String, Object>> onlyProducts = (List<Map<String, Object>>) mchProduct.get("list");
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
                    BigDecimal freightPrice           = DataUtils.getBigDecimalVal(product, "freight_price");
                    // 循环插入订单附表 ，添加不同的订单详情
                    freightPrice = vo.getShopAddressId() != 0 ? BigDecimal.ZERO : freightPrice;
                    //特殊处理减去运费
                    if (amountAfterDiscountTmp.equals(total))
                    {
                        amountAfterDiscountTmp = amountAfterDiscountTmp.subtract(freightPrice);
                    }
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
                    orderDetailsModel.setSettlement_type(OrderDetailsModel.SETTLEMENT_TYPE_UNSETTLED);
                    orderDetailsModel.setCoupon_id(coupon_id);
                    orderDetailsModel.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS);
                    orderDetailsModel.setAfter_discount(amountAfterDiscountTmp);
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

                    AddStockVo addStockVo = new AddStockVo();
                    addStockVo.setStoreId(vo.getStoreId());
                    addStockVo.setPid(pid);
                    addStockVo.setId(cid);
                    addStockVo.setAddNum(-num);
                    addStockVo.setText(userId + "生成订单所需" + num);
                    publicStockService.addGoodsStock(addStockVo, user.getZhanghao());
                }
            }
            mchId = StringUtils.rtrim(mchId, SplitUtils.DH);
            //获取订单配置
            Map<String, Object> configMap = publicOrderService.getOrderConfig(storeId, Integer.valueOf(mchId), otype);
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
            orderModel.setOld_total(total);
            orderModel.setsNo(sNo);
            orderModel.setSheng(sheng);
            orderModel.setShi(shi);
            orderModel.setXian(xian);
            orderModel.setAddress(addressXq);
            orderModel.setRemark(" ");
            orderModel.setPay(payType);
            orderModel.setAdd_time(new Date());
            orderModel.setCoupon_id("");
            orderModel.setSubtraction_id(subtractionId);
            orderModel.setConsumer_money(allow);
            orderModel.setCoupon_activity_name(reduceNameArray);
            orderModel.setSpz_price(productsTotal);
            orderModel.setStatus(orderStatus);
            orderModel.setReduce_price(reduceMoney);
            //禅道 41345
            orderModel.setCoupon_price(BigDecimal.ZERO);
            orderModel.setSource(vo.getStoreType());
            orderModel.setOtype(otype);
            orderModel.setMch_id(mchId);
            orderModel.setP_sNo("");
            orderModel.setBargain_id(0);
            orderModel.setComm_discount(discountMoney);
            orderModel.setRemarks(JSON.toJSONString(mchRemarks));
            orderModel.setReal_sno(realSno);
            orderModel.setSelf_lifting(shopStatus);
            orderModel.setExtraction_code(extractionCode);
            orderModel.setExtraction_code_img(extractionCodeImg);
            orderModel.setZ_freight(yunfei);
            orderModel.setOld_freight(yunfei);
            //禅道 41345
            orderModel.setPreferential_amount(BigDecimal.ZERO);
            orderModel.setSingle_store(shopAddressId);
            orderModel.setReadd(OrderModel.READD_UNREAD);
            orderModel.setZhekou(discount);
            orderModel.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS);
            orderModel.setPick_up_store(0);
            orderModel.setOrderFailureTime(orderFailureDay);
            orderModel.setMchRecycle(OrderModel.DELETE);
            //货币信息[用户支付的货币信息]前提是商城默认币种不设置以后不允许修改
            orderModel.setCurrency_code(vo.getCurrency_code());
            orderModel.setCurrency_symbol(vo.getCurrency_symbol());
            orderModel.setExchange_rate(vo.getExchange_rate());
            orderModelMapper.insertSelective(orderModel);
            int rowId = orderModel.getId();
            mchId = com.laiketui.core.utils.tool.StringUtils.trim(mchId, SplitUtils.DH);
            MessageLoggingModal messageLoggingSave = new MessageLoggingModal();
            messageLoggingSave.setMch_id(Integer.valueOf(mchId));
            messageLoggingSave.setStore_id(vo.getStoreId());
            messageLoggingSave.setType(MessageLoggingModal.Type.TYPE_ORDER_NEW);
            messageLoggingSave.setTo_url(OrderDataUtils.getOrderRoute(otype, 0));
            messageLoggingSave.setParameter(orderModel.getId() + "");
            messageLoggingSave.setContent(String.format("您来新订单了，订单为%s，请及时处理！", orderModel.getsNo()));
            messageLoggingSave.setAdd_date(new Date());
            messageLoggingModalMapper.insertSelective(messageLoggingSave);
            if (rowId >= 0)
            {
                //计算分佣金
                vo.setsNo(sNo);
                boolean flag = orderDistribution(vo, vo.getFatherId(), userId, goodsPrice, total, new BigDecimal(totalNum));
                //订单号
                resultMap.put("sNo", sNo);
                //订单总支付金额
                resultMap.put("total", total);
                //订单id
                resultMap.put("order_id", rowId);
                //下单时间
                resultMap.put("orderTime", DateUtil.dateFormate(orderModel.getAdd_time(), GloabConst.TimePattern.YMDHMS));
                return resultMap;
            }
            else
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_XDSBQSHZS, "下单失败,请稍后再试", "payment");
            }
        }
        catch (LaiKeAPIException e)
        {
            logger.error("分销下单失败 错误", e);
            throw e;
        }
        catch (Exception e)
        {
            logger.error("分销下单失败 异常", e);
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
            User         user          = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            List<String> orderTypeList = new ArrayList<>();
            orderTypeList.add(DictionaryConst.OrdersType.ORDERS_HEADER_FX);
            vo.setOrderTypeList(orderTypeList);
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
            params.put("type", vo.getType());
            params.put("start", vo.getPageNo());
            params.put("pageSize", vo.getPageSize());
            int                       total = returnOrderModelMapper.getReturnOrderListCount(params);
            List<Map<String, Object>> list  = returnOrderModelMapper.getReturnOrderList(params);
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
                        logger.info("订单商品不存在 订单号{}", MapUtils.getString(returnOrderInfo, "sNo"));
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
                    String returnStatus = publicRefundService.getRefundStatus(vo.getStoreId(), r_sNo, orderDetailId);
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

    /**
     * 计算分销订单佣金
     *
     * @param vo            -
     * @param pid           - 推荐人
     * @param userId        - 购买者
     * @param goodsPrice    - 商品总价
     * @param baseDisAmount - 订单总价
     * @param num           - 数量
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2022/1/27 11:06
     */
    public boolean orderDistribution(OrderVo vo, String pid, String userId, BigDecimal goodsPrice, BigDecimal baseDisAmount, BigDecimal num) throws LaiKeAPIException
    {
        try
        {
            // $dis_0->商品id  $dis_1->订单号
            //分销商品信息
            Map<String, Object> map = Maps.newHashMap();
            map.put("store_id", vo.getStoreId());
            map.put("sno", vo.getsNo());
            String           productsInfo = vo.getProductsInfo();
            List<JSONObject> proJsonArray = JSON.parseArray(productsInfo, JSONObject.class);
            int goodId = 0;
            for (JSONObject projson : proJsonArray)
            {
                if (projson.containsKey("pid"))
                {
                    map.put("pro_id", projson.get("pid"));
                    goodId = projson.getInteger("pid");
                    break;
                }
            }

            Map<String, Object> distributionGoodInfosMap = distributionGoodsModelMapper.getDistributionGoodInfos(map);
            if (distributionGoodInfosMap == null || distributionGoodInfosMap.size() == 0)
            {
                logger.error("分销订单结算异常分销商品 {} 不存在", goodId);
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_FXSPBCZ, "分销商品不存在", "orderDistribution");
            }
            //商品绑定等级
            int uplevel = MapUtils.getIntValue(distributionGoodInfosMap, "uplevel");
            //商品分佣规则
            int distributionRule = MapUtils.getInteger(distributionGoodInfosMap, "distribution_rule");
            //获取分销设置
            DistributionConfigModel distributionConfigModel = new DistributionConfigModel();
            distributionConfigModel.setStore_id(vo.getStoreId());
            distributionConfigModel = distributionConfigModelMapper.selectOne(distributionConfigModel);
            //反序列号
            Map<String, Object> config = SerializePhpUtils.getDistributionConfigBySets(distributionConfigModel.getSets());
            //分销层级 目前只能两级
            int disLevel = MapUtils.getIntValue(config, DistributionConfigModel.SetsKey.C_CENGJI);

            if (disLevel < 1)
            {
                logger.error("分销配置{}未设置层级 {} ", JSON.toJSONString(config), disLevel);
                return true;
            }

            // 默认是按照订单总价走 分销基值
            BigDecimal costPrice = new BigDecimal(MapUtils.getString(distributionGoodInfosMap, "costprice"));
            BigDecimal pv        = new BigDecimal(MapUtils.getString(distributionGoodInfosMap, "pv"));

            // 商品分销规则
            List<Map<String, Object>> goodsDisDiyRules = null;
            if (distributionRule == DistributionGoodsModel.DISTRIBUTION_RULE_CUSTOM)
            {
                if (!distributionGoodInfosMap.containsKey("rules_set"))
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_FXSPWSZDIYRULE, "分销商品未设置自定义分销规则", "orderDistribution");
                }
                /////新增开始
                goodsDisDiyRules = SerializePhpUtils.getUnserializeToList(MapUtils.getString(distributionGoodInfosMap, "rules_set"));
                if (CollectionUtils.isEmpty(goodsDisDiyRules))
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_FXSPWSZDIYRULE, "分销商品未设置自定义分销规则", "orderDistribution");
                }
            }

            //获取分润基值
            baseDisAmount = publiceDistributionService.getProfit(vo.getStoreId(), goodsPrice, costPrice, baseDisAmount, num, pv);

            if (BigDecimal.ZERO.compareTo(baseDisAmount) < 0)
            {
                //百分比
                BigDecimal precent1 = new BigDecimal("0.01");

                //是否内购 2是 1否
                int internalPurchaseFlag = MapUtils.getIntValue(config, DistributionConfigModel.SetsKey.C_NEIGOU);

                //分销等级相应佣金计算 所有等级相应应得奖励
                // 一、分两条线计算：
                // 1、佣金 （直推、间推） 说明：两级
                // 内购开启的情况购买者自己拿直推，上级拿间推
                // 内购未开启的情况购买者上级拿直推，上级推荐者拿间推
                // 计算佣金的时候要分情况，
                //      a：如果商品设置的是使用自定义规则则需要使用商品设置的分佣规则；同级和极差还是使用等级的设置；
                //      b：如果商品设置的是使用分销等级分佣规则，那么就使用分销等级的分佣规则；同级和极差也使用分销等级的奖励规则
                // 2、奖励（同级、极差） 说明：两级
                // 同级：购买者的分销等级和直接上级的分销等级相同，则购买者的直接上级可以获取同级奖励； Q:同级奖励到什么时候结束呢        A:【第2层】
                // 级差：购买者的分销等级小于直接上级的分销等级相同，则购买者的直接上级可以获取极差级奖励；Q:级差奖励到什么时候结束呢      A:【第2层】

                // 二、根据购买者的上级推荐id 去获取上级的分销等级从而 获取对应的分佣和奖励

                // 默认 level+0 =>  comm1 :0, comm2:0, sibling(平级、同级）: 0, different: 0
                // 钻石 level+钻石id =>  comm1 :1, comm2:2, sibling(平级、同级）: 3, different: 2
                // 铂金 level+铂金id =>  comm1 :1, comm2:2, sibling(平级、同级）: 3, different: 2
                // 黄金 level+黄金id =>  comm1 :1, comm2:2, sibling(平级、同级）: 3, different: 2
                // 白银 level+白银id =>  comm1 :1, comm2:2, sibling(平级、同级）: 3, different: 2

                Map<String, Map<String, Object>> allLevelConfigInfoMap = Maps.newHashMap();

                //默认级别的配置界面没有配置 代码中写死了
                Map<String, Object>              defaultLevelInfo      = Maps.newHashMap();
                allLevelConfigInfoMap.put("level0", defaultLevelInfo);
                //默认等级级差奖
                defaultLevelInfo.put("different", 0);
                //默认等级平级奖
                defaultLevelInfo.put("sibling", 0);
                //默认直推
                defaultLevelInfo.put("comm1", 0);
                //默认间推
                defaultLevelInfo.put("comm2", 0);

                //获取所有等级
                DistributionGradeModel distributionGradeModel = new DistributionGradeModel();
                distributionGradeModel.setStore_id(vo.getStoreId());
                List<DistributionGradeModel> distributionGradeModels = distributionGradeModelMapper.select(distributionGradeModel);
                //单个商品分销规则
                Map<String, Object> goodsDisDiyRuleInfoMap = null;
                int                 pos                    = 0;
                for (DistributionGradeModel distributionGrade : distributionGradeModels)
                {
                    Map<String, Object> tmpLevelInfo = Maps.newHashMap();
                    int                 id           = distributionGrade.getId();
                    //等级配置
                    Map<String, Object> disGradeConfig = SerializePhpUtils.getDistributionGradeBySets(distributionGrade.getSets());
                    int                 different_type = MapUtils.getIntValue(disGradeConfig, "different_type");
                    int                 sibling_type   = MapUtils.getIntValue(disGradeConfig, "sibling_type");
                    //差级
                    BigDecimal different = new BigDecimal(MapUtils.getString(disGradeConfig, "different"));
                    //平即
                    BigDecimal sibling = new BigDecimal(MapUtils.getString(disGradeConfig, "sibling"));
                    //加上数量计算
                    disGradeConfig.put("needNum", num);
                    //级差计算
                    if (different_type == Integer.valueOf(DistributionGoodsModel.DistributionRuleValue.DIRECT_M_TYPE_FIXED))
                    {
                        //佣金计算单位为元时 //计算级差
                        tmpLevelInfo.put("different", different);
                    }
                    else
                    {
                        //佣金计算单位为百分比%时//计算级差
                        tmpLevelInfo.put("different", different.multiply(baseDisAmount).multiply(precent1));
                    }
                    //平级计算
                    if (sibling_type == Integer.valueOf(DistributionGoodsModel.DistributionRuleValue.DIRECT_M_TYPE_FIXED))
                    {
                        //佣金计算单位为元时 计算平级
                        tmpLevelInfo.put("sibling", sibling);
                    }
                    else
                    {
                        //佣金计算单位为百分比%时 //计算平级
                        tmpLevelInfo.put("sibling", sibling.multiply(baseDisAmount).multiply(precent1));
                    }

                    //分佣计算
                    // 1=分销等级规则
                    // 2=自定义规则
                    if (distributionRule == DistributionGoodsModel.DISTRIBUTION_RULE_LEVEL)
                    {
                        //等级规则
                        settingDisConfigs(baseDisAmount, precent1, tmpLevelInfo, disGradeConfig);
                    }
                    else
                    {
                        //自定义规则
                            goodsDisDiyRuleInfoMap = goodsDisDiyRules.get(pos++);
                            goodsDisDiyRuleInfoMap.put("needNum",num);
                            settingDisConfigs(baseDisAmount, precent1, tmpLevelInfo, goodsDisDiyRuleInfoMap);
                    }

                    allLevelConfigInfoMap.put("level" + id, tmpLevelInfo);
                }

                //查询推荐人
                Map<String, Object> parentUserInfo = userDistributionModelMapper.getReferencesinfo(vo.getStoreId(), userId);
                logger.info("当前用户推荐人信息：{}", JSON.toJSONString(parentUserInfo));

                if (parentUserInfo != null && parentUserInfo.size() > 0)
                {
                    //永久关系的上级
                    //lkt_user_distribution 表中的分销商上级
                    String mainPid = MapUtils.getString(parentUserInfo, "pid");
                    //如果设置注册绑定永久关系则扫别人的推荐商品则推荐人不能改变,否则可以变成别人
                    //lkt_user表中的 注册上级
                    String referee = MapUtils.getString(parentUserInfo, "Referee");

                    if (StringUtils.isEmpty(mainPid))
                    {
                        logger.info("当前用户{},没有永久关系,临时关系为:{},推荐购买人为{}", userId, referee, pid);
                        //判断当前关系确定方式,如果是注册时确定永久关系,则推荐人为最上级(推荐人必须是分销商,不是则为平台)
                        if (StringUtils.isNotEmpty(pid) && DistributionConfigModel.RelationshipType.PLUGIN_CLOSE.equals(distributionConfigModel.getRelationship()))
                        {
                            //消费绑定关系
                            referee = pid;
                            logger.info("[当前开启消费时确定关系]当前用户没有永久关系,推荐购买人为上级id:{}", pid);
                        }

                        //判断购买者的上级用户是否是分销商
                        UserDistributionModel userDistributionModel = new UserDistributionModel();
                        userDistributionModel.setStore_id(vo.getStoreId());
                        userDistributionModel.setUser_id(referee);
                        userDistributionModel = userDistributionModelMapper.selectOne(userDistributionModel);
                        if (userDistributionModel == null || userDistributionModel.getLevel() == 0)
                        {
                            logger.info("当前推荐人{}非分销商 关系绑定到平台", referee);
                            //用户推荐人不存在时，推荐人默认为第一个分销商
                            referee = userDistributionModelMapper.getTheTopLevelUser(vo.getStoreId());
                        }
                        //创建分销商和上级关系
                        logger.info("当前推荐人{}非分销商 创建分销商和上级关系上级->{}", userId, referee);
                        //TODO 创建为分销商 这个地方有些客户的需要定制修改 publiceDistributionService.createLevel 这个方法
                        publiceDistributionService.createLevel(vo.getStoreId(), userId, 0, referee);
                    }
                    else
                    {
                        logger.info("当前用户{}有永久关系,上级为{}", userId, mainPid);
                        referee = mainPid;
                    }

                    parentUserInfo.put("Referee", referee);
                    //会员分销等级 $dis_9
                    int userDisLevel = MapUtils.getIntValue(parentUserInfo, "level");

                    /*
                     * 标记当前分佣了几级 最多两级
                     */
                    int commissionDistributionLevel = 1;
                    //存储被拿佣金等级 $takenCommissionLevel = array();
                    Map<Object, Object> takenCommissionLevel = Maps.newHashMap();
                    //已经获取的团队业绩奖金 $dis_21 = 0;
                    BigDecimal dis_21 = BigDecimal.ZERO;
                    // 已被获取的级差奖金额
                    BigDecimal obtainedDifferentialBonusAmount = BigDecimal.ZERO;
                    if ((referee + "").equalsIgnoreCase(userId))
                    {
                        //当购买人id等于推荐人id时推荐人id为空 顶级uplevel=0的用户才进这个判断
                        referee = "";
                    }
                    //判断内购 开启内购 内购只会有直推+间推 自己购物拿直推金,上级拿间推金
                    UserDistributionModel userDistributionMe = new UserDistributionModel();
                    //查询自己等级
                    userDistributionMe.setUser_id(userId);
                    userDistributionMe.setStore_id(vo.getStoreId());
                    userDistributionMe = userDistributionModelMapper.selectOne(userDistributionMe);
                    //购买人等级  如果分销等级为空 那么分销等级为商品绑定的分销等级
                    Integer level = userDistributionMe.getLevel();
                    level = level == null ? 0 : level;
                    //是否内购 2是 1否
                    if (internalPurchaseFlag == 2)
                    {
                        //用户为分销商时应获得的直推佣金
                        BigDecimal internalPurchaseCommAmount = new BigDecimal(MapUtils.getString(allLevelConfigInfoMap.get("level" + level), "comm1"));

                        if (internalPurchaseCommAmount.compareTo(BigDecimal.ZERO) > 0)
                        {
                            DistributionRecordModel distributionRecordModel = new DistributionRecordModel();
                            distributionRecordModel.setStore_id(vo.getStoreId());
                            distributionRecordModel.setUser_id(userId);
                            distributionRecordModel.setFrom_id(userId);
                            distributionRecordModel.setMoney(internalPurchaseCommAmount);
                            distributionRecordModel.setsNo(vo.getsNo());
                            distributionRecordModel.setLevel(commissionDistributionLevel);
                            distributionRecordModel.setType(DistributionRecordModel.Type.SHIFT_TO);
                            distributionRecordModel.setUserLevel(level);
                            distributionRecordModel.setGenre(DistributionRecordModel.CommissionType.DIRECT_PUSH);
                            String content = "用户" + userId + "获得" + internalPurchaseCommAmount.setScale(2, BigDecimal.ROUND_HALF_UP) + "内购佣金";
                            distributionRecordModel.setEvent(content);
                            distributionRecordModel.setAdd_date(new Date());
                            int row = distributionRecordModelMapper.insertSelective(distributionRecordModel);
                            distributionRecordModel.setId(null);
                            if (row < 1)
                            {
                                //todo 记录日志
                                logger.info(content + "【失败】");
                                return false;
                            }
                        }

                        //内购标记只需要计算上级,上上级不计算
                        commissionDistributionLevel = 2;
                    }

                    //购买人id
                    String fromUserId = userId;
                    //当推荐人存在时;这里只比较购买者上级以上的
                    if (StringUtils.isNotEmpty(referee))
                    {
                        //循环查询上级并计算佣金
                        while (commissionDistributionLevel <= disLevel)
                        {
                            //查询用户推荐人，分销商等级
                            UserDistributionModel userDistributionModel = userDistributionModelMapper.getParentUserInfo(vo.getStoreId(), referee);
                            if (userDistributionModel == null)
                            {
                                logger.info("【分销】 用户推荐人{}不是分销商,不进行佣金计算", referee);
                                break;
                            }

                            //上级分销商等级 一般都不会为0，为0的只有两种情况：1>顶级的分销层级为0 但是代数为0；2>最低级的分销商级别为0但代数至少是1
                            int superiorDistributorLevel = userDistributionModel.getLevel() == null ? 0 : userDistributionModel.getLevel();

                            // 如果上级没有分销等级，下级买等级规则商品则不计算佣金;
                            // 禅道53554上级没有等级都不给分佣（只有一种情况，顶级分销商没有等级）
                            if (superiorDistributorLevel > 0)
                            {
                                //不存在的等级不计算 ?? 计算当前用户等级对应的还是当前等级对应的 佣金和奖励
                                boolean flag = allLevelConfigInfoMap.containsKey("level" + superiorDistributorLevel);
                                if (!flag)
                                {
                                    //log
                                    logger.info("用户{}不存在的等级{}", userDistributionModel.getUser_id(), superiorDistributorLevel);
                                    break;
                                }
                                // 用户应得层级佣金  comm1直推/comm2间推金额
                                BigDecimal userEarnedTierCommissionAmount = new BigDecimal(MapUtils.getString(allLevelConfigInfoMap.get("level" + superiorDistributorLevel), "comm" + commissionDistributionLevel));
                                //当佣金大于零时插入佣金记录
                                if (userEarnedTierCommissionAmount.compareTo(BigDecimal.ZERO) > 0)
                                {
                                    String recordEventContent = "用户" + referee + "获得" + userEarnedTierCommissionAmount.setScale(2, BigDecimal.ROUND_HALF_UP) + "佣金";
                                    DistributionRecordModel distributionRecordModel = new DistributionRecordModel();
                                    distributionRecordModel.setStore_id(vo.getStoreId());
                                    distributionRecordModel.setUser_id(referee);
                                    distributionRecordModel.setFrom_id(fromUserId);
                                    distributionRecordModel.setMoney(userEarnedTierCommissionAmount);
                                    distributionRecordModel.setsNo(vo.getsNo());
                                    distributionRecordModel.setLevel(commissionDistributionLevel);
                                    distributionRecordModel.setUserLevel(superiorDistributorLevel);
                                    distributionRecordModel.setEvent(recordEventContent);
                                    distributionRecordModel.setType(DistributionRecordModel.Type.SHIFT_TO);
                                    distributionRecordModel.setAdd_date(new Date());

                                    if (DistributionRecordModel.CommissionType.DIRECT_PUSH.equals(commissionDistributionLevel))
                                    {
                                        distributionRecordModel.setGenre(DistributionRecordModel.CommissionType.DIRECT_PUSH);
                                    }
                                    else if (DistributionRecordModel.CommissionType.INTERPULSION.equals(commissionDistributionLevel))
                                    {
                                        distributionRecordModel.setGenre(DistributionRecordModel.CommissionType.INTERPULSION);
                                    }

                                    int row = distributionRecordModelMapper.insertSelective(distributionRecordModel);
                                    if (row < 1)
                                    {
                                        logger.error("用户" + referee + "获得插入佣金记录 {} 失败", JSON.toJSONString(distributionRecordModel));
                                        return false;
                                    }
                                }
                            }

                            //禅道50820 50783  50785
                            //平级奖[同级奖]计算 (当前购买人等级和上级一样则拿)【平级和差级只有商品的分佣使用的是分销等级规则才计算，目前自定义规则里面没有界面设置平级和级差奖励】
                            if (distributionRule == DistributionGoodsModel.DISTRIBUTION_RULE_LEVEL)
                            {
                                String fatherId = userDistributionModel.getPid();
                                //当前用户等级
                                UserDistributionModel userDistributionFather = userDistributionModelMapper.getParentUserInfo(vo.getStoreId(), userId);
                                //同级是当前分销商和它的上级比
                                if (userDistributionFather != null && userDistributionFather.getLevel() == superiorDistributorLevel)
                                {
                                    logger.info("{}获得平级奖等级{},下级id{}等级{}", referee, superiorDistributorLevel, userId, userDistributionFather.getLevel());
                                    BigDecimal siblingCommAmount = new BigDecimal(MapUtils.getString(allLevelConfigInfoMap.get("level" + superiorDistributorLevel), "sibling"));
                                    //当佣金大于零时插入佣金记录
                                    if (siblingCommAmount.compareTo(BigDecimal.ZERO) > 0)
                                    {
                                        String                  recordEventContent      = "用户" + referee + "获得" + siblingCommAmount.setScale(2, BigDecimal.ROUND_HALF_UP) + "佣金";
                                        DistributionRecordModel distributionRecordModel = new DistributionRecordModel();
                                        distributionRecordModel.setStore_id(vo.getStoreId());
                                        distributionRecordModel.setUser_id(referee);
                                        distributionRecordModel.setFrom_id(fromUserId);
                                        distributionRecordModel.setMoney(siblingCommAmount);
                                        distributionRecordModel.setsNo(vo.getsNo());
                                        distributionRecordModel.setLevel(commissionDistributionLevel);
                                        distributionRecordModel.setEvent(recordEventContent);
                                        distributionRecordModel.setType(DistributionRecordModel.Type.SHIFT_TO);
                                        distributionRecordModel.setAdd_date(new Date());
                                        distributionRecordModel.setGenre(DistributionRecordModel.CommissionType.SIDEWAYS);
                                        distributionRecordModel.setUserLevel(superiorDistributorLevel);
                                        int row = distributionRecordModelMapper.insertSelective(distributionRecordModel);
                                        if (row < 1)
                                        {
                                            logger.error("用户" + referee + "获得插入佣金记录 {} 失败", JSON.toJSONString(distributionRecordModel));
                                            return false;
                                        }
                                    }
                                    takenCommissionLevel.put(superiorDistributorLevel, 1);
                                }
                                else
                                {
                                    //当级差奖还没有被拿的时候
                                    //计算级差奖  等级相应级差奖-已被领取级差奖
                                    BigDecimal diffAmount          = new BigDecimal(MapUtils.getString(allLevelConfigInfoMap.get("level" + superiorDistributorLevel), "different"));
                                    BigDecimal levelDiffCommAmount = diffAmount.subtract(obtainedDifferentialBonusAmount);
                                    //层级佣金+级差奖，当获取的级差奖小于或等于0时，级差奖为0
                                    levelDiffCommAmount = levelDiffCommAmount.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : levelDiffCommAmount;
                                    //级差是购买者跟上级比较一次，然后上级再和上上级比较; 如果当前比较的人的等级>被比较的人则无法获取到级差
                                    if (userDistributionFather != null)
                                    {
                                        //上级一定要比下级大才有级差奖
                                        if (superiorDistributorLevel <= userDistributionFather.getLevel())
                                        {
                                            logger.info("{}无法获得级差奖,原因:当前等级为:{},比较等级为:{},本来应得级差金额为:{}", referee, superiorDistributorLevel, userDistributionFather.getLevel(), levelDiffCommAmount);
                                            levelDiffCommAmount = BigDecimal.ZERO;
                                        }
                                    }
                                    //当佣金大于零时插入佣金记录
                                    if (levelDiffCommAmount.compareTo(BigDecimal.ZERO) > 0)
                                    {
                                        String                  recordEventContent      = "用户" + referee + "获得" + levelDiffCommAmount.setScale(2, BigDecimal.ROUND_HALF_UP) + "佣金";
                                        DistributionRecordModel distributionRecordModel = new DistributionRecordModel();
                                        distributionRecordModel.setStore_id(vo.getStoreId());
                                        distributionRecordModel.setUser_id(referee);
                                        distributionRecordModel.setFrom_id(fromUserId);
                                        distributionRecordModel.setMoney(levelDiffCommAmount);
                                        distributionRecordModel.setsNo(vo.getsNo());
                                        distributionRecordModel.setLevel(commissionDistributionLevel);
                                        distributionRecordModel.setEvent(recordEventContent);
                                        distributionRecordModel.setType(DistributionRecordModel.Type.SHIFT_TO);
                                        distributionRecordModel.setAdd_date(new Date());
                                        distributionRecordModel.setGenre(DistributionRecordModel.CommissionType.DIFFERENTIAL);
                                        distributionRecordModel.setUserLevel(superiorDistributorLevel);
                                        int row = distributionRecordModelMapper.insertSelective(distributionRecordModel);
                                        if (row < 1)
                                        {
                                            logger.error("用户" + referee + "获得插入佣金记录 {} 失败", JSON.toJSONString(distributionRecordModel));
                                            return false;
                                        }
                                    }
                                    //当可获取级差奖大于已领取级差奖金时更新已领取奖金
                                    obtainedDifferentialBonusAmount = diffAmount.compareTo(obtainedDifferentialBonusAmount) > 0 ? diffAmount : obtainedDifferentialBonusAmount;
                                    //存储此等级到数组，val=1 表示此等级级差奖已被领取
                                    takenCommissionLevel.put(superiorDistributorLevel, 0);
                                }
                            }
                            if (!StringUtils.isEmpty(userDistributionModel.getPid()))
                            {
                                //当用户推荐人存在时  继续循环
                                referee = userDistributionModel.getPid();
                                //userId使用当前用户id
                                userId = userDistributionModel.getUser_id();
                                commissionDistributionLevel++;
                            }
                            else
                            {
                                // 当用户推荐人不存在时，结束循环
                                break;
                            }
                        }
                    }
                }
            }
            return true;
        }
        catch (LaiKeAPIException e)
        {
            logger.error("订单" + vo.getsNo() + "计算分佣失败。", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_XDSBJSFYSB, "下单失败,计算分佣失败", "orderDistribution");
        }
    }

    /**
     * 计算直推间推分佣
     * 等级规则 : config=lkt_distribution_grade.sets
     * 自定义规则 : config=lkt_distribution_goods.rules_set
     *
     * @param baseDisAmount 分润基值
     * @param precent1      百分比
     * @param tmpLevelInfo  直推间推值
     * @param config        商品/等级 规则
     */
    private void settingDisConfigs(BigDecimal baseDisAmount, BigDecimal precent1, Map<String, Object> tmpLevelInfo, Map<String, Object> config)
    {
        try
        {
            //a:4:{i:0;a:7:{s:9:"gradeName";s:6:"白银";s:16:
            // "direct_mode_type";i:1;s:18:
            // "indirect_mode_type";i:0;s:12:
            // "diy_discount";s:3:"0.9";s:2:
            // "id";s:2:"19";s:8:
            // "direct_m";s:1:"2";s:10:
            // "indirect_m";s:1:"1";}i:1;a:7:{s:9:"gradeName";s:6:"黄金";s:16:"direct_mode_type";i:1;s:18:"indirect_mode_type";i:0;s:12:"diy_discount";s:3:"0.6";s:2:"id";s:2:"20";s:8:"direct_m";s:1:"3";s:10:"indirect_m";r:9;}i:2;a:7:{s:9:"gradeName";s:6:"钻石";s:16:"direct_mode_type";i:1;s:18:"indirect_mode_type";i:1;s:12:"diy_discount";r:14;s:2:"id";s:2:"21";s:8:"direct_m";s:1:"4";s:10:"indirect_m";r:8;}i:3;a:7:{s:9:"gradeName";s:6:"铂金";s:16:"direct_mode_type";i:0;s:18:"indirect_mode_type";i:1;s:12:"diy_discount";r:14;s:2:"id";s:2:"35";s:8:"direct_m";s:1:"6";s:10:"indirect_m";r:16;}}
            //direct_m_type 为等级直推单位 0.百分比 1.固定金额
            int direct_m_type = MapUtils.getIntValue(config, DistributionGoodsModel.DistributionRuleKey.DIRECT_MODE_TYPE);
            //indirect_m_type  间推分销比例发放模式 0.百分比 1.固定金额
            int indirect_m_type = MapUtils.getIntValue(config, DistributionGoodsModel.DistributionRuleKey.INDIRECT_MODE_TYPE);

//            if (config.containsKey(DistributionGoodsModel.DistributionRuleKey.INDIRECT_M_TYPE1))
//            {
//                indirect_m_type = MapUtils.getIntValue(config, DistributionGoodsModel.DistributionRuleKey.INDIRECT_M_TYPE1);
//            }

            //商品数量
            BigDecimal needNum = new BigDecimal(MapUtils.getString(config, "needNum"));
            //直推返佣金额
            String     directM  = MapUtils.getString(config, DistributionGoodsModel.DistributionRuleKey.DIRECT_M);
            BigDecimal direct_m = BigDecimal.ZERO;
            if (StringUtils.isNotEmpty(directM))
            {
                direct_m = new BigDecimal(directM);
            }

            //间推返佣金额
            String     indirectM  = MapUtils.getString(config, DistributionGoodsModel.DistributionRuleKey.INDIRECT_M);
            BigDecimal indirect_m = BigDecimal.ZERO;
            if (StringUtils.isNotEmpty(indirectM))
            {
                indirect_m = new BigDecimal(indirectM);
            }

            if (direct_m_type == Integer.valueOf(DistributionGoodsModel.DistributionRuleValue.DIRECT_M_TYPE_FIXED))
            {
                //佣金计算单位为元时 直推分销奖
                tmpLevelInfo.put("comm1", direct_m.multiply(needNum));
            }
            else
            {
                //佣金计算单位为百分比%时 直推分销奖
                tmpLevelInfo.put("comm1", direct_m.multiply(baseDisAmount).multiply(precent1));
            }

            if (indirect_m_type == 1)
            {
                //佣金计算单位为元时 间推分销奖
                tmpLevelInfo.put("comm2", indirect_m.multiply(needNum));
            }
            else
            {
                //佣金计算单位为百分比%时 间推分销奖
                tmpLevelInfo.put("comm2", indirect_m.multiply(baseDisAmount).multiply(precent1));
            }
        }
        catch (Exception e)
        {
            logger.error("直推、间推计算失败 :", e);
        }
    }

    /**
     * 计算直推间推金额
     * 等级规则 : config=lkt_distribution_grade.sets
     * 自定义规则 : config=lkt_distribution_goods.rules_set
     *
     * @param baseDisAmount 分润基值
     * @param precent1      百分比
     * @param tmpLevelInfo  直推间推值
     * @param config        商品/等级 规则
     */
//    private void settingDisConfigs(BigDecimal baseDisAmount, BigDecimal precent1, Map<String, Object> tmpLevelInfo, Map<String, Object> config)
//    {
//        try
//        {
//            //direct_m_type 为等级直推单位 0.百分比 1.固定金额
//            int direct_m_type = MapUtils.getIntValue(config, DistributionGoodsModel.DistributionRuleKey.DIRECT_M_TYP);
//            //indirect_m_type  间推分销比例发放模式 0.百分比 1.固定金额
//            int indirect_m_type = MapUtils.getIntValue(config, DistributionGoodsModel.DistributionRuleKey.INDIRECT_M_TYPE);
//            if (config.containsKey(DistributionGoodsModel.DistributionRuleKey.INDIRECT_M_TYPE1))
//            {
//                indirect_m_type = MapUtils.getIntValue(config, DistributionGoodsModel.DistributionRuleKey.INDIRECT_M_TYPE1);
//            }
//            //商品数量
//            BigDecimal needNum = new BigDecimal(MapUtils.getString(config, "needNum"));
//            //直推返佣金额
//            String     directM  = MapUtils.getString(config, DistributionGoodsModel.DistributionRuleKey.DIRECT_M);
//            BigDecimal direct_m = BigDecimal.ZERO;
//            if (StringUtils.isNotEmpty(directM))
//            {
//                direct_m = new BigDecimal(directM);
//            }
//
//            //间推返佣金额
//            String     indirectM  = MapUtils.getString(config, DistributionGoodsModel.DistributionRuleKey.INDIRECT_M);
//            BigDecimal indirect_m = BigDecimal.ZERO;
//            if (StringUtils.isNotEmpty(indirectM))
//            {
//                indirect_m = new BigDecimal(indirectM);
//            }
//
//            if (direct_m_type == 1)
//            {
//                //佣金计算单位为元时 直推分销奖
//                tmpLevelInfo.put("comm1", direct_m.multiply(needNum));
//            }
//            else
//            {
//                //佣金计算单位为百分比%时 直推分销奖
//                tmpLevelInfo.put("comm1", direct_m.multiply(baseDisAmount).multiply(precent1));
//            }
//            if (indirect_m_type == 1)
//            {
//                //佣金计算单位为元时 间推分销奖
//                tmpLevelInfo.put("comm2", indirect_m.multiply(needNum));
//            }
//            else
//            {
//                //佣金计算单位为百分比%时 间推分销奖
//                tmpLevelInfo.put("comm2", indirect_m.multiply(baseDisAmount).multiply(precent1));
//            }
//        }
//        catch (Exception e)
//        {
//            logger.error("直推、间推计算失败 :", e);
//        }
//    }

    @Override
    public Map<String, Object> getPayment(OrderVo vo)
    {
        return null;
    }
}

