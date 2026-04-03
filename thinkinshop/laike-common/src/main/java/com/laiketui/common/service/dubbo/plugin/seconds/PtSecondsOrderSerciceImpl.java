package com.laiketui.common.service.dubbo.plugin.seconds;

import com.alibaba.fastjson2.*;
import com.laiketui.common.annotation.HandlerOrderType;
import com.laiketui.common.api.*;
import com.laiketui.common.api.order.OrderDubboService;
import com.laiketui.common.mapper.*;
import com.laiketui.common.utils.tool.cache.RedisDataTool;
import com.laiketui.core.cache.RedisUtil;
import com.laiketui.core.common.SplitUtils;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.DictionaryConst;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.utils.tool.DataUtils;
import com.laiketui.core.utils.tool.SerializePhpUtils;
import com.laiketui.core.utils.tool.StringUtils;
import com.laiketui.domain.mch.MchBrowseModel;
import com.laiketui.domain.mch.MchModel;
import com.laiketui.domain.mch.MchStoreModel;
import com.laiketui.domain.order.OrderDetailsModel;
import com.laiketui.domain.order.OrderModel;
import com.laiketui.domain.plugin.seckill.PtSecondsProModel;
import com.laiketui.domain.product.ProductListModel;
import com.laiketui.domain.user.User;
import com.laiketui.domain.user.UserAddress;
import com.laiketui.domain.vo.OrderVo;
import com.laiketui.domain.vo.order.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.*;

import static com.laiketui.core.lktconst.ErrorCode.BizErrorCode.*;

/**
 * 平台-秒杀
 *
 * @author Trick
 * @date 2021/4/28 14:12
 */
@HandlerOrderType(type = com.laiketui.core.lktconst.DictionaryConst.OrdersType.PTHD_ORDER_PM)
@Service("ptSecOrderDubboService")
public class PtSecondsOrderSerciceImpl implements OrderDubboService
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
    private PaymentConfigModelMapper paymentConfigModelMapper;

    @Autowired
    private PtSecondsProModelMapper secondsProModelMapper;

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

    @Autowired
    private PublicPaymentConfigService publicPaymentConfigService;

    @Override
    public Map<String, Object> settlement(OrderVo vo)
    {
        try
        {
            // 获取用户信息 直接从redis 里面取 余额、收货信息
            User   user   = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            String userId = user.getUser_id();

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

            UserAddress userAddress = commonAddressService.findAddress(map);
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

            //
            List<Map<String, Object>> productsListMap = publiceService.productsList(productList, vo.getCarts(), vo.getBuyType(), DictionaryConst.OrdersType.ORDERS_HEADER_GM);
            if (CollectionUtils.isEmpty(productsListMap))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.STOCK_INSUFFICIENT, "库存不足2", "settlement");
            }
            //按照店铺归类的商品、运费、商品总价等信息
            Map<String, Object> productsInfo = publiceService.settlementProductsInfo(productsListMap, vo.getStoreId(), vo.getProductType());
            //运费信息
            Map<String, List<Map<String, Object>>> productsFreight = (Map<String, List<Map<String, Object>>>) productsInfo.get("products_freight");
            //计算会员的产品价格和订单产品总价
            List<Map<String, Object>> products = (List<Map<String, Object>>) productsInfo.get("products");
            //计算会员优惠价格
            Map<String, Object> memberProductsInfo = publiceService.getMemberPrice(products, userId, vo.getStoreId());
            //订单产品总计
            BigDecimal orderProductsTotal = DataUtils.getBigDecimalVal(memberProductsInfo, "products_total");
            //拿出商品信息
            productsListMap = (List<Map<String, Object>>) memberProductsInfo.get("products");
            BigDecimal gradeRate = DataUtils.getBigDecimalVal(memberProductsInfo, "grade_rate");

            //计算店铺运费、总订单运费
            BigDecimal yunfei = BigDecimal.ZERO;
            productsInfo = publicOrderService.getFreight(productsFreight, productsListMap, userAddress, vo.getStoreId(), vo.getProductType());
            products = (List<Map<String, Object>>) productsInfo.get("products");
            //
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

            //优惠金额
            BigDecimal preferentialAmount = DataUtils.ZERO_BIGDECIMAL;
            //会员等级金额
            BigDecimal gradeRateAmount = DataUtils.ZERO_BIGDECIMAL;
            //实际付款金额
            BigDecimal payTotal = new BigDecimal("0");
            if (!StringUtils.isEmpty(vo.getGradeLevel()) && vo.getGradeLevel() != 0)
            {
                // 是商品兑换券
                orderProductsTotal = DataUtils.ZERO_BIGDECIMAL;
                // 实际付款金额 = 运费
                payTotal = yunfei;
            }
            else
            {
                for (Map<String, Object> mchProductsInfo : products)
                {
                    gradeRateAmount = gradeRateAmount.add(DataUtils.getBigDecimalVal(mchProductsInfo, "grade_rate_amount"));
                }
            }
            //计算订单金额
            payTotal = payTotal.add(orderProductsTotal);

            Map<String, Object> resultMap = new HashMap<>();
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
            BigDecimal goodsPrice = orderProductsTotal.add(gradeRateAmount);
            resultMap.put("products_total", goodsPrice);
            // 用户余额
            resultMap.put("user_money", user.getMoney());
            // 实际支付金额
            resultMap.put("total", payTotal);
            resultMap.put("freight", yunfei);
            // 用户地址
            resultMap.put("address", userAddress);
            // 是否有收货地址
            resultMap.put("addemt", addemt);
            // 平台优惠
            resultMap.put("preferential_amount", preferentialAmount);
            // 会员等级金额
            resultMap.put("grade_rate_amount", gradeRateAmount);
            // 会员等级折扣
            resultMap.put("grade_rate", gradeRate);
            return resultMap;
        }
        catch (LaiKeAPIException e)
        {
            e.printStackTrace();
            throw e;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.BUSY_NETWORK, "网络异常", "settlement");
        }

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> payment(OrderVo vo)
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        // 1.数据准备
        // 获取用户信息 直接从redis 里面取 余额、收货信息
        User user    = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil);
        int  storeId = vo.getStoreId();
        // 用户id
        String userId = user.getUser_id();
        // 商品总价
        BigDecimal productsTotal;
        //  商品数组--------['pid'=>66,'cid'=>88]
        List<Map<String, Object>> products;
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
        // 订单总价
        BigDecimal total;
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
        products = publiceService.productsList(productList, vo.getCarts(), vo.getBuyType(), DictionaryConst.OrdersType.PTHD_ORDER_PM);
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
        Map<String, Object> productsInfo = publiceService.settlementProductsInfo(products, vo.getStoreId(), DictionaryConst.OrdersType.PTHD_ORDER_PM);
        //运费信息
        Map<String, List<Map<String, Object>>> productsFreight = (Map<String, List<Map<String, Object>>>) productsInfo.get("products_freight");
        //计算会员的产品价格和订单产品总价
        products = (List<Map<String, Object>>) productsInfo.get("products");
        //计算会员优惠价格
        Map<String, Object> memberProductsInfo = publiceService.getMemberPrice(products, userId, vo.getStoreId());
        //拿出商品信息
        products = (List<Map<String, Object>>) memberProductsInfo.get("products");
        BigDecimal gradeRate = DataUtils.getBigDecimalVal(memberProductsInfo, "grade_rate");
        productsTotal = DataUtils.getBigDecimalVal(memberProductsInfo, "products_total");

        //计算店铺运费、总订单运费
        BigDecimal yunfei = BigDecimal.ZERO;
        productsInfo = publicOrderService.getFreight(productsFreight, products, userAddress, vo.getStoreId(), DictionaryConst.OrdersType.PTHD_ORDER_PM);
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
            //自提免邮
            yunfei = BigDecimal.ZERO;
        }

        // 生成订单号
        String sNo = publicOrderService.createOrderNo(vo.getType());
        // 生成支付订单号
        String realSno = publicOrderService.createOrderNo(vo.getType());
        String mchId   = "";

        Map<String, String> mchRemarks = new HashMap<>();
        // 是否有备注标记
        boolean    remarksStatus       = false;
        BigDecimal preferential_amount = BigDecimal.ZERO;
        total = yunfei;
        int pos = 0;
        for (Map<String, Object> mchProduct : products)
        {
            String shopId = String.valueOf(mchProduct.get("shop_id"));
            mchId = mchId + shopId + SplitUtils.DH;
            if (remarkJsonarr != null)
            {
                //处理订单备注
                String tmpDesc = remarkJsonarr.getString(pos++);
                if (!StringUtils.isEmpty(tmpDesc))
                {
                    mchRemarks.put(shopId, tmpDesc);
                    remarksStatus = true;
                }
            }
            //添加一条购买记录
            MchBrowseModel mchBrowseModel = new MchBrowseModel();
            mchBrowseModel.setMch_id(mchId);
            mchBrowseModel.setStore_id(storeId);
            mchBrowseModel.setUser_id(userId);
            mchBrowseModel.setEvent("购买了商品");
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
                int    cid          = DataUtils.getIntegerVal(product, "cid");
                int    num          = DataUtils.getIntegerVal(product, "num");
                String productTitle = DataUtils.getStringVal(product, "product_title");
                String unit         = DataUtils.getStringVal(product, "unit");
                String size         = DataUtils.getStringVal(product, "size");
                //如果没有优惠则为支付
                BigDecimal amountAfterDiscountTmp = DataUtils.getBigDecimalVal(product, "amount_after_discount", total);

                BigDecimal freightPrice = DataUtils.getBigDecimalVal(product, "freight_price");
                // 循环插入订单附表 ，添加不同的订单详情
                freightPrice = vo.getShopAddressId() != 0 ? BigDecimal.ZERO : freightPrice;

                OrderDetailsModel orderDetailsModel = new OrderDetailsModel();
                orderDetailsModel.setStore_id(storeId);
                orderDetailsModel.setUser_id(userId);
                orderDetailsModel.setP_id(pid);
                orderDetailsModel.setP_name(productTitle);
                orderDetailsModel.setP_price(productsTotal);
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
                orderDetailsModel.setMch_id(Integer.parseInt(shopId));
                orderDetailsModel.setAfter_discount(amountAfterDiscountTmp);

                int beres = orderDetailsModelMapper.insertSelective(orderDetailsModel);
                // 如果添加失败
                if (beres < 1)
                {
                    throw new LaiKeAPIException(ORDER_FAILED_TRY_AGAIN_LATER, "下单失败,请稍后再试", "payment");
                }
                totalNum += num;
            }
        }
        //库存处理
        PtSecondsProModel secondsProModel = secondsProModelMapper.selectByPrimaryKey(vo.getMainId());
        if (secondsProModel == null)
        {
            throw new LaiKeAPIException(PLUGIN_SECONDS_GOODS_NOT_EXIST, "秒杀商品不存在");
        }
        //是否超过最大数量
        if (totalNum > secondsProModel.getNum())
        {
            throw new LaiKeAPIException(PLUGIN_SECONDS_STOCK_OUT, "库存不足");
        }
        //扣减库存
        int row = secondsProModelMapper.addStockNum(secondsProModel.getId(), -totalNum);
        if (row < 1)
        {
            throw new LaiKeAPIException(ORDER_FAILED_TRY_AGAIN_LATER, "下单失败,请稍后再试", "payment");
        }

        //加上运费
        total = total.add(productsTotal);

        //订单备注
        String mainOrderRemarks = "";
        if (remarksStatus)
        {
            mainOrderRemarks = SerializePhpUtils.JavaSerializeByPhp(mchRemarks);
        }

        mchId = com.laiketui.core.utils.tool.StringUtils.rtrim(mchId, SplitUtils.DH);
        mchId = SplitUtils.DH + mchId + SplitUtils.DH;

        OrderModel orderModel = new OrderModel();
        orderModel.setStore_id(storeId);
        orderModel.setUser_id(userId);
        orderModel.setName(name);
        orderModel.setMobile(mobile);
        orderModel.setNum(totalNum);
        orderModel.setZ_price(total);
        orderModel.setsNo(sNo);
        orderModel.setSheng(sheng);
        orderModel.setShi(shi);
        orderModel.setXian(xian);
        orderModel.setAddress(addressXq);
        orderModel.setRemark(" ");
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
        orderModel.setOtype(DictionaryConst.OrdersType.PTHD_ORDER_PM);
        orderModel.setMch_id(mchId);
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
        orderModel.setPreferential_amount(preferential_amount);
        orderModel.setSingle_store(shopAddressId);
        orderModel.setReadd(OrderModel.READD_UNREAD);
        orderModel.setZhekou(BigDecimal.ZERO);
        orderModel.setRecycle(0);
        orderModel.setPick_up_store(0);
        orderModelMapper.insertSelective(orderModel);
        int orderId = orderModel.getId();
        if (orderId >= 0)
        {
            //订单号
            resultMap.put("sNo", sNo);
            //订单总支付金额
            resultMap.put("total", total);
            //订单id
            resultMap.put("order_id", orderId);
            return resultMap;
        }
        else
        {
            throw new LaiKeAPIException(ORDER_FAILED_TRY_AGAIN_LATER, "下单失败,请稍后再试", "payment");
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
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);

            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("storeId", vo.getStoreId());
            parmaMap.put("userId", user.getUser_id());
            parmaMap.put("orderType", vo.getOrderType());
            parmaMap.put("status", "notClose");
            parmaMap.put("pageStart", vo.getPageNo());
            parmaMap.put("pageEnd", vo.getPageSize());
            parmaMap.put("otype", DictionaryConst.OrdersType.PTHD_ORDER_PM);
            List<Map<String, Object>> orderList = orderModelMapper.selectDynamic(parmaMap);
            for (Map<String, Object> orderInfo : orderList)
            {
                //总运费
                BigDecimal totalYunFei = new BigDecimal("0");
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

                //店铺信息处理
                String mchIdStr = orderInfo.get("mch_id") + "";
                if (!StringUtils.isEmpty(mchIdStr))
                {
                    mchId = Integer.parseInt(StringUtils.trim(mchIdStr, SplitUtils.DH));
                    //获取店铺信息
                    MchModel mchModel = mchModelMapper.selectByPrimaryKey(mchId);
                    mchName = mchModel.getName();
                    logoUrl = publiceService.getImgPath(mchModel.getLogo(), vo.getStoreId());
                }
                orderInfo.put("shop_id", mchId);
                orderInfo.put("shop_name", mchName);
                orderInfo.put("shop_logo", logoUrl);

                //获取订单明细
                OrderDetailsModel orderDetailsModel = new OrderDetailsModel();
                orderDetailsModel.setStore_id(vo.getStoreId());
                orderDetailsModel.setR_sNo(orderno);
                List<Map<String, Object>> orderDetailsModelList = orderDetailsModelMapper.getOrderDetailNotClose(vo.getStoreId(), orderno);
                for (Map<String, Object> orderDetails : orderDetailsModelList)
                {
                    //统计运费
                    totalYunFei = totalYunFei.add(new BigDecimal(orderDetails.get("freight").toString()));
                    totalNeedNum = totalNeedNum.add(new BigDecimal(orderDetails.get("num").toString()));
                    int goodsId = Integer.parseInt(orderDetails.get("p_id").toString());
                    //获取商品信息
                    ProductListModel productListModel = productListModelMapper.selectByPrimaryKey(goodsId);
                    orderDetails.put("pro_id", goodsId);
                    orderDetails.put("imgurl", publiceService.getImgPath(productListModel.getImgurl(), vo.getStoreId()));
                }
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
            e.printStackTrace();
            logger.error("秒杀订单列表 异常" + e.getMessage());
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
        return null;
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
