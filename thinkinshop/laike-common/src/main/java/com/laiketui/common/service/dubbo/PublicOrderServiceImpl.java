package com.laiketui.common.service.dubbo;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import com.alipay.api.AlipayClient;
import com.alipay.api.AlipayConfig;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradeFastpayRefundQueryModel;
import com.alipay.api.request.AlipayTradeFastpayRefundQueryRequest;
import com.alipay.api.response.AlipayTradeFastpayRefundQueryResponse;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.kuaidi100.sdk.api.LabelCancel;
import com.kuaidi100.sdk.api.LabelV2;
import com.kuaidi100.sdk.contant.ApiInfoConstant;
import com.kuaidi100.sdk.contant.PrintType;
import com.kuaidi100.sdk.core.IBaseClient;
import com.kuaidi100.sdk.pojo.HttpResult;
import com.kuaidi100.sdk.request.LabelCancelParam;
import com.kuaidi100.sdk.request.ManInfo;
import com.kuaidi100.sdk.request.PrintReq;
import com.kuaidi100.sdk.request.labelV2.OrderReq;
import com.laiketui.common.api.*;
import com.laiketui.common.api.admin.PublicAdminService;
import com.laiketui.common.api.distribution.PubliceDistributionService;
import com.laiketui.common.api.order.PublicIntegralService;
import com.laiketui.common.api.pay.PublicPaymentService;
import com.laiketui.common.api.plugin.PublicAuctionService;
import com.laiketui.common.api.plugin.member.PubliceMemberService;
import com.laiketui.common.api.plugin.payment.PaymentService;
import com.laiketui.common.consts.OrderShowValueEnum;
import com.laiketui.common.mapper.*;
import com.laiketui.common.process.OrderShowValueProcess.OrderProcessor;
import com.laiketui.common.utils.CpcUtils;
import com.laiketui.common.utils.HttpApiUtils;
import com.laiketui.common.utils.UrlParamUtils;
import com.laiketui.common.utils.tool.MD5Util;
import com.laiketui.common.utils.tool.cache.RedisDataTool;
import com.laiketui.common.utils.tool.data.GoodsDataUtils;
import com.laiketui.common.utils.tool.data.OrderDataUtils;
import com.laiketui.common.utils.weixin.AppletUtil;
import com.laiketui.core.cache.RedisUtil;
import com.laiketui.core.common.SplitUtils;
import com.laiketui.core.config.alipay.AlipayConfigInfo;
import com.laiketui.core.config.wechatpay.WechatConfigInfo;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.DictionaryConst;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.core.utils.okhttp.HttpUtils;
import com.laiketui.core.utils.tool.*;
import com.laiketui.domain.auction.AuctionPromiseModel;
import com.laiketui.domain.config.*;
import com.laiketui.domain.coupon.CouponActivityModel;
import com.laiketui.domain.coupon.CouponModal;
import com.laiketui.domain.distribution.DistributionConfigModel;
import com.laiketui.domain.distribution.DistributionGoodsModel;
import com.laiketui.domain.distribution.FreightModel;
import com.laiketui.domain.distribution.UserDistributionModel;
import com.laiketui.domain.flashsale.FlashsaleConfigModel;
import com.laiketui.domain.group.GroupOpenModel;
import com.laiketui.domain.home.SystemMessageModel;
import com.laiketui.domain.log.AdminRecordModel;
import com.laiketui.domain.log.RecordDetailsModel;
import com.laiketui.domain.log.RecordModel;
import com.laiketui.domain.log.SignRecordModel;
import com.laiketui.domain.mch.*;
import com.laiketui.domain.message.MessageLoggingModal;
import com.laiketui.domain.message.TemplateData;
import com.laiketui.domain.order.*;
import com.laiketui.domain.payment.PaymentModel;
import com.laiketui.domain.plugin.group.PtGoGroupOrderModel;
import com.laiketui.domain.presell.PreSellConfigModel;
import com.laiketui.domain.presell.PreSellGoodsModel;
import com.laiketui.domain.presell.PreSellRecordModel;
import com.laiketui.domain.product.CommentsModel;
import com.laiketui.domain.product.ProductListModel;
import com.laiketui.domain.seckill.SecondsConfigModel;
import com.laiketui.domain.supplier.SupplierModel;
import com.laiketui.domain.supplier.SupplierOrderFrightModel;
import com.laiketui.domain.user.User;
import com.laiketui.domain.user.UserAddress;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.OrderVo;
import com.laiketui.domain.vo.config.AddPluginOrderConfigVo;
import com.laiketui.domain.vo.freight.DefaultFreightVO;
import com.laiketui.domain.vo.freight.FreightRuleVO;
import com.laiketui.domain.vo.main.RefundVo;
import com.laiketui.domain.vo.mch.FrontDeliveryVo;
import com.laiketui.domain.vo.mch.MchOrderDetailVo;
import com.laiketui.domain.vo.mch.MchOrderIndexVo;
import com.laiketui.domain.vo.order.*;
import com.laiketui.domain.vo.pay.PayVo;
import com.laiketui.domain.vo.pay.PaymentVo;
import com.laiketui.domain.vo.pc.MchPcOrderIndexVo;
import com.laiketui.domain.vo.plugin.auction.PromiseOrderVo;
import com.laiketui.domain.vo.plugin.integral.AddScoreVo;
import com.laiketui.domain.vo.plugin.member.MemberOrderVo;
import com.laiketui.root.license.Md5Util;
import com.paypal.core.PayPalEnvironment;
import com.paypal.core.PayPalHttpClient;
import com.paypal.http.HttpResponse;
import com.paypal.payments.Refund;
import com.paypal.payments.RefundsGetRequest;
import github.wxpay.sdk.WXPay;
import github.wxpay.sdk.WXPayConstants;
import github.wxpay.sdk.WXPayUtil;
import github.wxpay.sdk.WXPayXmlUtil;
import org.apache.commons.codec.CharEncoding;
import org.apache.commons.collections.MapUtils;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import static com.laiketui.common.process.OrderShowValueProcess.OrderProcessor.addActions;
import static com.laiketui.core.lktconst.DictionaryConst.OrdersStatus.*;
import static com.laiketui.core.lktconst.ErrorCode.BizErrorCode.*;
import static com.laiketui.domain.order.OrderModel.*;


/**
 * 订单通用服务类
 *
 * @author wangxian
 */
@Service
public class PublicOrderServiceImpl implements PublicOrderService
{

    private final Logger logger = LoggerFactory.getLogger(PublicOrderServiceImpl.class);

    @Autowired
    private HttpApiUtils httpApiUtils;

    @Autowired
    private MchConfigModelMapper mchConfigModelMapper;

    @Autowired
    private CustomerModelMapper customerModelMapper;

    @Autowired
    private SupplierModelMapper supplierModelMapper;
    @Autowired
    private AdminModelMapper    adminModelMapper;

    @Autowired
    private SupplierOrderFrightModelMapper supplierOrderFrightModelMapper;

    @Autowired
    private UserDistributionModelMapper userDistributionModelMapper;

    @Autowired
    private ExpressDeliveryModelMapper expressDeliveryModelMapper;

    @Autowired
    private ExpressSubtableModelMapper   expressSubtableModelMapper;
    @Autowired
    private StoreSelfDeliveryModelMapper storeSelfDeliveryModelMapper;

    @Autowired
    private PublicExpressService     publicExpressService;
    @Autowired
    private MchStoreWriteModelMapper mchStoreWriteModelMapper;
    @Autowired
    private CouponOrderModalMapper   couponOrderModalMapper;

    @Autowired
    private CurrencyStoreModelMapper currencyStoreModelMapper;

    @Override
    public Map<String, Object> getFreight(Map<String, List<Map<String, Object>>> freightMap,
                                          List<Map<String, Object>> products, UserAddress userAddress, int storeId, String productType) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>();
        try
        {
            //每个商品的运费
            Map<Integer, BigDecimal> freightGoodsMap = new HashMap<>();

            //获取商城自营店id
            Integer zyMchId    = customerModelMapper.getStoreMchId(storeId);
            double  yunfei     = 0.0;
            int     productNum = 0;
            //新增重量
            BigDecimal weightNum     = BigDecimal.ZERO;
            int        productNewNum = 0;
            //是否开启了包邮设置
            int packageSettings = 0;
            //
            boolean orderYunfei = false;
            //同件
            int samePiece = 0;
            //同单
            Integer sameOrder = null;
            //店铺订单包邮设置
            Map<String, Object> mchBY = new HashMap<>();
            //店铺同订单是否符合包邮集合
            Map<Integer, Boolean> isBY = new HashMap<>();
            //同店铺的订单数量
            int mchNum = 0;
            int goodId = 0;
            if (DictionaryConst.OrdersType.ORDERS_HEADER_PS.equals(productType))
            {
                PreSellConfigModel preSellConfigModel = new PreSellConfigModel();
                preSellConfigModel.setStore_id(storeId);
                preSellConfigModel = preSellConfigModelMapper.selectOne(preSellConfigModel);
                if (!Objects.isNull(preSellConfigModel))
                {
                    packageSettings = preSellConfigModel.getPackage_settings();
                    // 同件
                    samePiece = preSellConfigModel.getSame_piece();
                }
            }
            else if (DictionaryConst.OrdersType.ORDERS_HEADER_MS.equals(productType))
            {
                SecondsConfigModel secondsConfigModel = new SecondsConfigModel();
                secondsConfigModel.setStore_id(storeId);
                secondsConfigModel.setMch_id(zyMchId);
                secondsConfigModel = secondsConfigModelMapper.selectOne(secondsConfigModel);
                if (secondsConfigModel != null)
                {
                    packageSettings = secondsConfigModel.getPackage_settings();
                    // 同件
                    samePiece = secondsConfigModel.getSame_piece();
                    if (packageSettings == 1)
                    {
                        //开始计算秒杀同件包邮设置
                        Set<String> keys     = freightMap.keySet();
                        boolean     piecePro = false;//同件商品包邮是否达到条件
                        for (String key : keys)
                        {
                            List<Map<String, Object>> productFreights = new ArrayList<>(freightMap.get(key));
                            for (Map<String, Object> productFreight : productFreights)
                            {
                                int num = MapUtils.getInteger(productFreight, "num");
                                //同件包邮条件判断
                                if (num >= samePiece)
                                {
                                    productFreight.put("order_yunfei", true);
                                }
                                for (Map<String, Object> product : products)
                                {
                                    product.put("freight", 0);
                                }
                            }
                        }
                    }
                }
            }
            else
            {
                //首先计算平台包邮设置是否达到条件(平台包邮设置不需要拆分店铺)
                OrderConfigModal orderConfigModal = new OrderConfigModal();
                orderConfigModal.setStore_id(storeId);
                orderConfigModal = orderConfigModalMapper.selectOne(orderConfigModal);
                if (orderConfigModal != null && orderConfigModal.getPackage_settings().equals(DictionaryConst.WhetherMaven.WHETHER_OK))
                {
                    packageSettings = orderConfigModal.getPackage_settings();
                    // 同件
                    samePiece = orderConfigModal.getSame_piece();
                    // 同单
                    sameOrder = orderConfigModal.getSame_order();
                    //开始计算平台同件同单包邮设置
                    Set<String>           keys        = freightMap.keySet();
                    int                   orderProNum = 0;//订单商品总数
                    boolean               piecePro    = false;//同件商品包邮是否达到条件
                    Map<Integer, Integer> proNumMap   = new HashMap<>(16);//单个商品总数
                    for (String key : keys)
                    {
                        List<Map<String, Object>> productFreights = new ArrayList<>(freightMap.get(key));
                        for (Map<String, Object> productFreight : productFreights)
                        {
                            int goodsId = MapUtils.getInteger(productFreight, "pid");
                            int num     = MapUtils.getInteger(productFreight, "num");
                            orderProNum += num;
                            //同件包邮 同件商品不同规格也算为一件商品
                            if (proNumMap.containsKey(goodsId))
                            {
                                num += proNumMap.get(goodsId);
                            }
                            proNumMap.put(goodsId, num);
                            //同件包邮条件判断
                            if (num >= samePiece)
                            {
                                piecePro = true;
                            }
                        }
                    }
                    //如果平台包邮设置都没达到条件则划分店铺再进行同件同单包邮
                    if (orderProNum >= sameOrder || piecePro)
                    {
                        //满足免邮条件全部包邮
                        for (String key : keys)
                        {
                            List<Map<String, Object>> productFreights = freightMap.get(key);
                            for (Map<String, Object> productFreight : productFreights)
                            {
                                productFreight.put("order_yunfei", true);
                            }
                        }
                        for (Map<String, Object> product : products)
                        {
                            product.put("freight", 0);
                        }
                    }
                    else
                    {
                        for (Map<String, Object> productInfo : products)
                        {
                            String         mchId          = MapUtils.getString(productInfo, "shop_id");
                            MchConfigModel mchConfigModel = new MchConfigModel();
                            mchConfigModel.setStore_id(storeId);
                            mchConfigModel.setMch_id(Integer.valueOf(mchId));
                            mchConfigModel = mchConfigModelMapper.selectOne(mchConfigModel);
                            //判断条件清零
                            orderProNum = 0;//订单商品总数
                            piecePro = false;//同件商品包邮是否达到条件
                            proNumMap = new HashMap<>(16);//单个商品总数
                            if (!Objects.isNull(mchConfigModel) && mchConfigModel.getPackage_settings() != 0)
                            {
                                packageSettings = mchConfigModel.getPackage_settings();
                                // 同件
                                samePiece = mchConfigModel.getSame_piece();
                                // 同单
                                sameOrder = mchConfigModel.getSame_order();
                                List<Map<String, Object>> list = new ArrayList<>(freightMap.get(mchId));
                                for (Map<String, Object> productFreight : list)
                                {
                                    int goodsId = MapUtils.getInteger(productFreight, "pid");
                                    int num     = MapUtils.getInteger(productFreight, "num");
                                    orderProNum += num;
                                    //同件包邮 同件商品不同规格也算为一件商品
                                    if (proNumMap.containsKey(goodsId))
                                    {
                                        num += proNumMap.get(goodsId);
                                    }
                                    proNumMap.put(goodsId, num);
                                    //同件包邮条件判断
                                    if (num >= samePiece)
                                    {
                                        piecePro = true;
                                    }
                                    productFreight.put("order_yunfei", false);
                                }
                                //达到店铺包邮条件则该店铺所有商品都包邮
                                if (orderProNum >= sameOrder || piecePro)
                                {
                                    for (Map<String, Object> productFreight : list)
                                    {
                                        productFreight.put("order_yunfei", true);
                                    }
                                    productInfo.put("freight", 0);
                                }
                            }
                        }
                    }
                }
                else
                {
                    //平台包邮策略未开启直接进行店铺包邮策略处理
                    for (Map<String, Object> productInfo : products)
                    {
                        String         mchId          = MapUtils.getString(productInfo, "shop_id");
                        MchConfigModel mchConfigModel = new MchConfigModel();
                        mchConfigModel.setStore_id(storeId);
                        mchConfigModel.setMch_id(Integer.valueOf(mchId));
                        mchConfigModel = mchConfigModelMapper.selectOne(mchConfigModel);
                        //同件同单判断条件
                        int                   orderProNum = 0;//订单商品总数
                        boolean               piecePro    = false;//同件商品包邮是否达到条件
                        Map<Integer, Integer> proNumMap   = new HashMap<>(16);//单个商品总数
                        if (!Objects.isNull(mchConfigModel) && mchConfigModel.getPackage_settings() != 0)
                        {
                            packageSettings = mchConfigModel.getPackage_settings();
                            // 同件
                            samePiece = mchConfigModel.getSame_piece();
                            // 同单
                            sameOrder = mchConfigModel.getSame_order();
                            List<Map<String, Object>> list = new ArrayList<>(freightMap.get(mchId));
                            for (Map<String, Object> productFreight : list)
                            {
                                int goodsId = MapUtils.getInteger(productFreight, "pid");
                                int num     = MapUtils.getInteger(productFreight, "num");
                                orderProNum += num;
                                //同件包邮 同件商品不同规格也算为一件商品
                                if (proNumMap.containsKey(goodsId))
                                {
                                    num += proNumMap.get(goodsId);
                                }
                                proNumMap.put(goodsId, num);
                                //同件包邮条件判断
                                if (num >= samePiece)
                                {
                                    piecePro = true;
                                }
                                productFreight.put("order_yunfei", false);
                            }
                            //达到店铺包邮条件则该店铺所有商品都包邮
                            if (orderProNum >= sameOrder || piecePro)
                            {
                                for (Map<String, Object> productFreight : list)
                                {
                                    productFreight.put("order_yunfei", true);
                                }
                                productInfo.put("freight", 0);
                            }
                        }
                    }
                }
            }
            //各个店铺下商品所需的运费
            Map<String, Object> freightIdMap = new HashMap<>();
            Set<String>         keys         = freightMap.keySet();
            for (String key : keys)
            {
                //运费信息
                resultMap.put("freight_id", freightIdMap);
                List<Map<String, Object>> productFreights = freightMap.get(key);
                //同一个商品不同规格数据计算运费的首件续件(首重续重)按同一个商品算（重量不需要累加因为在计算运费中会和数量相乘）
                Map<Integer, Object> proNumWeight = new HashMap<>(16);
                for (int i = 0; i < productFreights.size(); i++)
                {
                    Map<String, Object> productFreight = productFreights.get(i);
                    Map<String, Object> numWeight      = new HashMap<>(16);
                    Integer             num            = MapUtils.getInteger(productFreight, "num");
                    String              weight1        = MapUtils.getString(productFreight, "weight");
                    BigDecimal          weight         = new BigDecimal(StringUtils.isNotEmpty(weight1) ? weight1 : "0.00");
                    numWeight.put("num", num);
                    numWeight.put("weight", weight);
                    //商品id
                    Integer goodsId = MapUtils.getInteger(productFreight, "pid");
                    if (proNumWeight.containsKey(goodsId))
                    {
                        Map     map    = (Map) proNumWeight.get(goodsId);
                        Integer oldNum = MapUtils.getInteger(map, "num");
//                        BigDecimal oldWeight = new BigDecimal(MapUtils.getString(map, "weight"));
                        map.put("num", oldNum + num);
//                        map.put("weight", oldWeight.add(weight));
                        continue;
                    }
                    proNumWeight.put(goodsId, numWeight);
                }
                Map<Integer, Integer> newFreightMap = new HashMap();
                for (int i = 0; i < productFreights.size(); i++)
                {
                    Map<String, Object> productFreight = productFreights.get(i);
                    //获取运费模版id
                    Integer freightIdObj = MapUtils.getInteger(productFreight, "freight_id");
                    //单件运费
                    boolean singlProductYunfei = false;
                    if (productFreight.containsKey(key))
                    {
                        singlProductYunfei = (boolean) productFreight.get(key);
                    }
                    else
                    {
                        if (!Objects.isNull(productFreight.get("order_yunfei")))
                        {
                            singlProductYunfei = (boolean) productFreight.get("order_yunfei");
                        }
                    }
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
                        //单个商品免运费
                        if (singlProductYunfei)
                        {
                            yunfei += 0;
                            List<Double> mchProductFreigths = new ArrayList<>();
                            if (freightIdMap.containsKey(key))
                            {
                                mchProductFreigths = DataUtils.cast(freightIdMap.get(key));
                                if (mchProductFreigths == null)
                                {
                                    mchProductFreigths = new ArrayList<>();
                                }
                            }
                            mchProductFreigths.add(0.0);
                            freightIdMap.put(key, mchProductFreigths);
                        }
                        else
                        {
                            //获取运费
                            BigDecimal          goodsYunFei      = BigDecimal.ZERO;
                            Map<String, Object> productFreightYF = productFreights.get(i);
                            //商品id
                            //获取运费模版id
                            Integer      freightIdObj1   = MapUtils.getInteger(productFreightYF, "freight_id");
                            FreightModel newFreightModel = new FreightModel();
                            if (freightIdObj1 != null)
                            {
                                BigDecimal freightId = BigDecimal.valueOf(freightIdObj1);
                                newFreightModel.setId(freightId.intValue());
                            }
                            Integer goodsFreightId = MapUtils.getInteger(productFreightYF, "pid");
                            if (proNumWeight.containsKey(goodsFreightId))
                            {
                                if (newFreightMap.containsKey(goodsFreightId))
                                {
                                    continue;
                                }
                                Map        map       = (Map) proNumWeight.get(goodsFreightId);
                                Integer    oldNum    = MapUtils.getInteger(map, "num");
                                BigDecimal oldWeight = new BigDecimal(MapUtils.getString(map, "weight"));
                                productFreightYF.put("num", oldNum);
//                                productFreightYF.put("weight", oldWeight);
                                newFreightMap.put(goodsFreightId, 1);
                            }
                            productNewNum = MapUtils.getInteger(productFreightYF, "num");
                            //新增重量
                            if (StringUtils.isNotEmpty(MapUtils.getInteger(productFreightYF, "weight")))
                            {
                                weightNum = new BigDecimal(MapUtils.getString(productFreightYF, "weight")).setScale(0, BigDecimal.ROUND_UP);
                            }
                            goodsYunFei = this.getFreight(newFreightModel.getId(), userAddress, productNewNum, weightNum);
                            //计算总运费
                            yunfei += goodsYunFei.doubleValue();

                            List<Double> mchProductFreigths = new ArrayList<>();
                            mchProductFreigths.add(goodsYunFei.doubleValue());
                            freightIdMap.put(key, mchProductFreigths);
                            //商品运费信息
                            freightGoodsMap.put(goodsFreightId, goodsYunFei);
                        }
                    }
                }
            }

            resultMap.put("yunfei", yunfei);
            resultMap.put("freight_ids", resultMap.get("freight_id"));

            if (DictionaryConst.OrdersType.ORDERS_HEADER_JP.equals(productType))
            {
                for (Map<String, Object> product : products)
                {
                    product.put("freight_price", yunfei);
                }
            }
            else
            {
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
                    int           pos        = 0;
                    List<Integer> goodIdList = new ArrayList<>();
                    for (Map<String, Object> productInfo : onlyProductsInfo)
                    {
                        BigDecimal freight_price = BigDecimal.ZERO;
                        products_num = products_num.add(new BigDecimal(productInfo.get("num") + ""));
                        //商品id
                        Integer goodsId = MapUtils.getInteger(productInfo, "pid");
                        if (goodIdList.contains(goodsId))
                        {
                            continue;
                        }
                        goodIdList.add(goodsId);
                        Set<Integer>             keySet = freightGoodsMap.keySet();
                        Map<Integer, BigDecimal> map    = new HashMap<>();
                        BigDecimal               ss     = BigDecimal.ZERO;
                        // 遍历keySet，并输出key的值
                        for (Integer key : keySet)
                        {
                            if (goodsId.compareTo(key) == 0)
                            {
                                ss = ss.add(freightGoodsMap.get(goodsId));
                                map.put(shop_id, ss);
                            }
                        }
                        if (map.containsKey(shop_id))
                        {
                            freight_price = map.get(shop_id);
                            freight_price_total = freight_price_total.add(freight_price);
                        }
                        productInfo.put("freight_price", freight_price);
                    }
                    mchProduct.put("freight_price", freight_price_total);
                    mchProduct.put("products_num", products_num);
                    Double mchProductTotal = (Double) mchProduct.get("product_total");
                    mchProduct.put("product_total", DoubleFormatUtil.format(mchProductTotal));
                }
            }
            resultMap.put("products", products);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("运费计算失败 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YFJSSB, "运费计算失败", "getFreight");
        }
        return resultMap;
    }

    @Override
    public BigDecimal getFreight(Integer goodsAddressId, UserAddress userAddress) throws LaiKeAPIException
    {
        BigDecimal yunfei = BigDecimal.ZERO;
        try
        {
            if (goodsAddressId == null)
            {
                return yunfei;
            }
            //获取商品运费模板信息
            FreightModel freightModel = freightModelMapper.selectByPrimaryKey(goodsAddressId);
            if (freightModel != null && userAddress != null)
            {
                //用户地址 省-市-区对应运费模板里的格式
                String address = userAddress.getSheng() + "-" + userAddress.getCity() + "-" + userAddress.getQuyu();
                if (StringUtils.isNotEmpty(freightModel.getNo_delivery()))
                {
                    //不配送区域参数列表
                    String    bpsRule = URLDecoder.decode(freightModel.getNo_delivery(), CharEncoding.UTF_8);
                    JSONArray objects = JSONArray.parseArray(bpsRule);
                    if (objects.contains(address))
                    {
                        logger.debug("地址超出配送范围");
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DZCCPSFW, "地址超出配送范围", "getNo_delivery");
                    }
                }
                //满足条件使用指定运费规则运费 没达到条件则计算默认运费
                if (StringUtils.isNotEmpty(freightModel.getFreight()))
                {
                    List<FreightRuleVO> freightRuleVOS = JSON.parseArray(freightModel.getFreight(), FreightRuleVO.class);
                    for (FreightRuleVO freightRuleVO : freightRuleVOS)
                    {
                        List<String> stringList     = Arrays.asList(freightRuleVO.getName().split(SplitUtils.DH));
                        List<String> freightAddress = new ArrayList<>(stringList);
                        if (freightAddress.contains(address))
                        {
                            yunfei = new BigDecimal(freightRuleVO.getFreight());
                            return yunfei;
                        }
                    }
                }
                //计算默认运费规则
                DefaultFreightVO defaultFreightVO = JSONObject.parseObject(freightModel.getDefault_freight(), DefaultFreightVO.class);
                yunfei = new BigDecimal(defaultFreightVO.getNum2());
            }
        }
        catch (LaiKeAPIException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            logger.debug("运费计算 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YFJSSB, "运费计算失败", "getFreight");
        }
        return yunfei;
    }

    @Override
    public BigDecimal getFreight(Integer goodsAddressId, UserAddress userAddress, int productNum, BigDecimal weightNum) throws LaiKeAPIException
    {
        BigDecimal yunfei         = BigDecimal.ZERO;
        BigDecimal weightNumCount = weightNum.multiply(new BigDecimal(productNum));
        try
        {
            if (goodsAddressId == null)
            {
                return yunfei;
            }
            //获取商品运费模板信息
            FreightModel freightModel = freightModelMapper.selectByPrimaryKey(goodsAddressId);
            if (freightModel != null && userAddress != null)
            {
                //新运费规则：有指定运费规则并且用户地址在指定运费规则地区中就按指定运费计算 反之计算默认运费规则
                String address = userAddress.getSheng() + "-" + userAddress.getCity() + "-" + userAddress.getQuyu();
                if (StringUtils.isNotEmpty(freightModel.getFreight()))
                {
                    List<String>        freightAddress = new ArrayList<>();
                    List<FreightRuleVO> freightRuleVOS = JSON.parseArray(freightModel.getFreight(), FreightRuleVO.class);
                    freightRuleVOS.stream().forEach(freightRuleVO ->
                    {
                        List<String> stringList = Arrays.asList(freightRuleVO.getName().split(SplitUtils.DH));
                        freightAddress.addAll(stringList);
                    });
                    if (freightAddress.contains(address))
                    {
                        //计算指定运费规则
                        for (FreightRuleVO freightRuleVO : freightRuleVOS)
                        {
                            if (Objects.isNull(freightRuleVO))
                            {
                                continue;
                            }
                            List<String> addressList = Arrays.asList(freightRuleVO.getName().split(SplitUtils.DH));
                            //获取运费首件件数
                            int SjJs = Integer.parseInt(freightRuleVO.getOne());
                            //获取运费续件件数
                            int XjJs = Integer.parseInt(freightRuleVO.getTwo());
                            //获取首件重量
                            BigDecimal SjWeightYuFei = new BigDecimal(freightRuleVO.getOne());
                            //获取续件重量
                            BigDecimal XjWeightYuFei = new BigDecimal(freightRuleVO.getTwo());
                            //获取首件件数运费
                            BigDecimal SjYuFei = new BigDecimal(freightRuleVO.getFreight());
                            //获取续件件数运费
                            BigDecimal XjYuFei = new BigDecimal(freightRuleVO.getTfreight());
                            //不包邮
                            if (freightModel.getIs_package_settings() == 0)
                            {
                                //类型 件
                                if (freightModel.getType() == 0)
                                {
                                    if (addressList.contains(address) && productNum <= SjJs)
                                    {
                                        //                            yunfei = new BigDecimal(yufeiMap.get("one").toString());
                                        yunfei = SjYuFei;
                                        break;
                                    }
                                    else if (addressList.contains(address) && productNum <= XjJs)
                                    {
                                        yunfei = XjYuFei.add(SjYuFei);
                                        break;
                                    }
                                    else if (addressList.contains(address) && productNum > XjJs)
                                    {
                                        //商品-首件  <= 续件       首件运费+续件运费
                                        if (productNum - SjJs <= XjJs)
                                        {
                                            yunfei = XjYuFei.add(SjYuFei);
                                            break;
                                        }
                                        //商品-首件  > 续件    (商品重量-首件重量)/续件重量   进一取整    首件运费+进一取整*续件运费
                                        if (productNum - SjJs > XjJs)
                                        {
                                            BigDecimal JS = (new BigDecimal(productNum).subtract(new BigDecimal(freightRuleVO.getOne()))).divide(new BigDecimal(freightRuleVO.getTwo()), 0, BigDecimal.ROUND_UP);
                                            yunfei = SjYuFei.add(JS.multiply(XjYuFei));
                                            break;
                                        }
                                    }
                                    else
                                    {
                                        if (addressList.contains(address))
                                        {
                                            yunfei = SjYuFei;
                                            break;
                                        }
                                    }
                                }
                                else if (freightModel.getType() == 1)
                                {//重量
                                    if (addressList.contains(address) && (weightNumCount.compareTo(SjWeightYuFei) != 1))
                                    {
                                        //                            yunfei = new BigDecimal(yufeiMap.get("one").toString());
                                        yunfei = SjYuFei;
                                        break;
                                    }
                                    else if (addressList.contains(address) && (weightNumCount.compareTo(XjWeightYuFei) != 1 && weightNumCount.compareTo(SjWeightYuFei) != -1))
                                    {
                                        yunfei = XjYuFei.add(SjYuFei);
                                        break;
                                    }
                                    else if (addressList.contains(address) && weightNumCount.compareTo(XjWeightYuFei) == 1)
                                    {
                                        //商品-首件  <= 续件       首件运费+续件运费
                                        if ((weightNumCount.subtract(SjWeightYuFei)).compareTo(XjWeightYuFei) != 1)
                                        {
                                            yunfei = XjYuFei.add(SjYuFei);
                                            break;
                                        }
                                        //商品-首件  > 续件    (商品重量-首件重量)/续件重量   进一取整    首件运费+进一取整*续件运费
                                        if ((weightNumCount.subtract(SjWeightYuFei)).compareTo(XjWeightYuFei) == 1)
                                        {
                                            BigDecimal JYQZ = (weightNumCount.subtract(new BigDecimal(freightRuleVO.getOne()))).divide(new BigDecimal(freightRuleVO.getTwo()), 0, BigDecimal.ROUND_UP);
                                            yunfei = SjYuFei.add(JYQZ.multiply(XjYuFei));
                                            break;
                                        }
                                    }
                                    else
                                    {
                                        if (addressList.contains(address))
                                        {
                                            yunfei = SjYuFei;
                                            break;
                                        }
                                    }
                                }
                                else
                                {
                                    if (addressList.contains(address))
                                    {
                                        yunfei = SjYuFei;
                                        break;
                                    }
                                }
                            }
                            else
                            {
                                //包邮
                                //类型 件
                                if (freightModel.getType() == 0)
                                {
                                    if (addressList.contains(address) && productNum <= SjJs && StringUtils.isNotEmpty(freightModel.getPackage_settings()) && productNum < Integer.parseInt(freightModel.getPackage_settings()))
                                    {
                                        //                            yunfei = new BigDecimal(yufeiMap.get("one").toString());
                                        yunfei = SjYuFei;
                                        break;
                                    }
                                    else if (addressList.contains(address) && productNum <= XjJs && StringUtils.isNotEmpty(freightModel.getPackage_settings()) && productNum < Integer.parseInt(freightModel.getPackage_settings()))
                                    {
                                        yunfei = XjYuFei.add(SjYuFei);
                                        break;
                                    }
                                    else if (addressList.contains(address) && productNum > XjJs && StringUtils.isNotEmpty(freightModel.getPackage_settings()) && productNum < Integer.parseInt(freightModel.getPackage_settings()))
                                    {
                                        //商品-首件  <= 续件       首件运费+续件运费
                                        if (productNum - SjJs <= XjJs)
                                        {
                                            yunfei = XjYuFei.add(SjYuFei);
                                            break;
                                        }
                                        //商品-首件  > 续件    (商品重量-首件重量)/续件重量   进一取整    首件运费+进一取整*续件运费
                                        if (productNum - SjJs > XjJs)
                                        {
                                            BigDecimal JS = (new BigDecimal(productNum).subtract(new BigDecimal(freightRuleVO.getOne()))).divide(new BigDecimal(freightRuleVO.getTwo()), 0, BigDecimal.ROUND_UP);
                                            yunfei = SjYuFei.add(JS.multiply(XjYuFei));
                                            break;
                                        }
                                    }
                                    else if (addressList.contains(address) && StringUtils.isNotEmpty(freightModel.getPackage_settings()) && productNum >= Integer.parseInt(freightModel.getPackage_settings()))
                                    {
                                        yunfei = new BigDecimal("0.0");
                                        break;
                                    }
                                    else
                                    {
                                        if (addressList.contains(address) && StringUtils.isNotEmpty(freightModel.getPackage_settings()) && productNum >= Integer.parseInt(freightModel.getPackage_settings()))
                                        {
                                            yunfei = new BigDecimal("0.0");
                                            break;
                                        }
                                        else
                                        {
                                            if (addressList.contains(address))
                                            {
                                                yunfei = SjYuFei;
                                                break;
                                            }
                                        }
                                    }
                                }
                                else if (freightModel.getType() == 1)
                                {//重量
                                    if (addressList.contains(address) && (weightNumCount.compareTo(SjWeightYuFei) != 1) && StringUtils.isNotEmpty(freightModel.getPackage_settings()) && productNum < Integer.parseInt(freightModel.getPackage_settings()))
                                    {
                                        //                            yunfei = new BigDecimal(yufeiMap.get("one").toString());
                                        yunfei = SjYuFei;
                                        break;
                                    }
                                    else if (addressList.contains(address) && (weightNumCount.compareTo(XjWeightYuFei) != 1 && weightNumCount.compareTo(SjWeightYuFei) != -1) && StringUtils.isNotEmpty(freightModel.getPackage_settings()) && productNum < Integer.parseInt(freightModel.getPackage_settings()))
                                    {
                                        yunfei = XjYuFei.add(SjYuFei);
                                        break;
                                    }
                                    else if (addressList.contains(address) && weightNumCount.compareTo(XjWeightYuFei) == 1 && StringUtils.isNotEmpty(freightModel.getPackage_settings()) && productNum < Integer.parseInt(freightModel.getPackage_settings()))
                                    {
                                        //商品-首件  <= 续件       首件运费+续件运费
                                        if ((weightNumCount.subtract(SjWeightYuFei)).compareTo(XjWeightYuFei) != 1)
                                        {
                                            yunfei = XjYuFei.add(SjYuFei);
                                            break;
                                        }
                                        //商品-首件  > 续件    (商品重量-首件重量)/续件重量   进一取整    首件运费+进一取整*续件运费
                                        if ((weightNumCount.subtract(SjWeightYuFei)).compareTo(XjWeightYuFei) == 1)
                                        {
                                            BigDecimal JYQZ = (weightNumCount.subtract(new BigDecimal(freightRuleVO.getOne()))).divide(new BigDecimal(freightRuleVO.getTwo()), 0, BigDecimal.ROUND_UP);
                                            yunfei = SjYuFei.add(JYQZ.multiply(XjYuFei));
                                            break;
                                        }
                                    }
                                    else if (addressList.contains(address) && StringUtils.isNotEmpty(freightModel.getPackage_settings()) && productNum >= Integer.parseInt(freightModel.getPackage_settings()))
                                    {
                                        yunfei = new BigDecimal("0.0");
                                        break;
                                    }
                                    else
                                    {
                                        if (addressList.contains(address))
                                        {
                                            yunfei = SjYuFei;
                                            break;
                                        }
                                    }
                                }
                                else
                                {
                                    if (addressList.contains(address) && StringUtils.isNotEmpty(freightModel.getPackage_settings()) && productNum >= Integer.parseInt(freightModel.getPackage_settings()))
                                    {
                                        yunfei = new BigDecimal("0.0");
                                        break;
                                    }
                                    else
                                    {
                                        if (addressList.contains(address))
                                        {
                                            yunfei = SjYuFei;
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                        return yunfei;
                    }
                }

                //计算默认运费规则
                DefaultFreightVO defaultFreightVO = JSONObject.parseObject(freightModel.getDefault_freight(), DefaultFreightVO.class);
                //获取运费首件件数
                int SjJs = Integer.parseInt(defaultFreightVO.getNum1());
                //获取运费续件件数
                int XjJs = Integer.parseInt(defaultFreightVO.getNum3());
                //获取首件重量
                BigDecimal SjWeightYuFei = new BigDecimal(defaultFreightVO.getNum1());
                //获取续件重量
                BigDecimal XjWeightYuFei = new BigDecimal(defaultFreightVO.getNum3());
                //获取首件件数运费
                BigDecimal SjYuFei = new BigDecimal(defaultFreightVO.getNum2());
                //获取续件件数运费
                BigDecimal XjYuFei = new BigDecimal(defaultFreightVO.getNum4());
                //不包邮
                if (freightModel.getIs_package_settings() == 0)
                {
                    //类型 件
                    if (freightModel.getType() == 0)
                    {
                        if (productNum <= SjJs)
                        {
                            yunfei = SjYuFei;
                        }
                        else if (productNum <= XjJs)
                        {
                            yunfei = XjYuFei.add(SjYuFei);
                        }
                        else if (productNum > XjJs)
                        {
                            //商品-首件  <= 续件       首件运费+续件运费
                            if (productNum - SjJs <= XjJs)
                            {
                                yunfei = XjYuFei.add(SjYuFei);
                            }
                            //商品-首件  > 续件    (商品重量-首件重量)/续件重量   进一取整    首件运费+进一取整*续件运费
                            if (productNum - SjJs > XjJs)
                            {
                                BigDecimal JS = (new BigDecimal(productNum).subtract(new BigDecimal(defaultFreightVO.getNum1()))).divide(new BigDecimal(defaultFreightVO.getNum3()), 0, BigDecimal.ROUND_UP);
                                yunfei = SjYuFei.add(JS.multiply(XjYuFei));
                            }
                        }
                        else
                        {
                            yunfei = SjYuFei;
                        }
                    }
                    else if (freightModel.getType() == 1)
                    {//重量
                        if ((weightNumCount.compareTo(SjWeightYuFei) != 1))
                        {
                            //                            yunfei = new BigDecimal(yufeiMap.get("one").toString());
                            yunfei = SjYuFei;
                        }
                        else if ((weightNumCount.compareTo(XjWeightYuFei) != 1 && weightNumCount.compareTo(SjWeightYuFei) != -1))
                        {
                            yunfei = XjYuFei.add(SjYuFei);
                        }
                        else if (weightNumCount.compareTo(XjWeightYuFei) == 1)
                        {
                            //商品-首件  <= 续件       首件运费+续件运费
                            if ((weightNumCount.subtract(SjWeightYuFei)).compareTo(XjWeightYuFei) != 1)
                            {
                                yunfei = XjYuFei.add(SjYuFei);
                            }
                            //商品-首件  > 续件    (商品重量-首件重量)/续件重量   进一取整    首件运费+进一取整*续件运费
                            if ((weightNumCount.subtract(SjWeightYuFei)).compareTo(XjWeightYuFei) == 1)
                            {
                                BigDecimal JYQZ = (weightNumCount.subtract(new BigDecimal(defaultFreightVO.getNum1()))).divide(new BigDecimal(defaultFreightVO.getNum3()), 0, BigDecimal.ROUND_UP);
                                yunfei = SjYuFei.add(JYQZ.multiply(XjYuFei));
                            }
                        }
                        else
                        {
                            yunfei = SjYuFei;
                        }
                    }
                    else
                    {
                        yunfei = SjYuFei;
                    }
                }
                else
                {//包邮
                    //类型 件
                    if (freightModel.getType() == 0)
                    {
                        if (productNum <= SjJs && StringUtils.isNotEmpty(freightModel.getPackage_settings()) && productNum < Integer.parseInt(freightModel.getPackage_settings()))
                        {
                            yunfei = SjYuFei;
                        }
                        else if (productNum <= XjJs && StringUtils.isNotEmpty(freightModel.getPackage_settings()) && productNum < Integer.parseInt(freightModel.getPackage_settings()))
                        {
                            yunfei = XjYuFei.add(SjYuFei);
                        }
                        else if (productNum > XjJs && StringUtils.isNotEmpty(freightModel.getPackage_settings()) && productNum < Integer.parseInt(freightModel.getPackage_settings()))
                        {
                            //商品-首件  <= 续件       首件运费+续件运费
                            if (productNum - SjJs <= XjJs)
                            {
                                yunfei = XjYuFei.add(SjYuFei);
                            }
                            //商品-首件  > 续件    (商品重量-首件重量)/续件重量   进一取整    首件运费+进一取整*续件运费
                            if (productNum - SjJs > XjJs)
                            {
                                BigDecimal JS = (new BigDecimal(productNum).subtract(new BigDecimal(defaultFreightVO.getNum1()))).divide(new BigDecimal(defaultFreightVO.getNum3()), 0, BigDecimal.ROUND_UP);
                                yunfei = SjYuFei.add(JS.multiply(XjYuFei));
                            }
                        }
                        else if (StringUtils.isNotEmpty(freightModel.getPackage_settings()) && productNum >= Integer.parseInt(freightModel.getPackage_settings()))
                        {
                            yunfei = new BigDecimal("0.0");
                        }
                        else
                        {
                            if (StringUtils.isNotEmpty(freightModel.getPackage_settings()) && productNum >= Integer.parseInt(freightModel.getPackage_settings()))
                            {
                                yunfei = new BigDecimal("0.0");
                            }
                            else
                            {
                                yunfei = SjYuFei;
                            }
                        }
                    }
                    else if (freightModel.getType() == 1)
                    {//重量
                        if ((weightNumCount.compareTo(SjWeightYuFei) != 1) && StringUtils.isNotEmpty(freightModel.getPackage_settings()) && productNum < Integer.parseInt(freightModel.getPackage_settings()))
                        {
                            yunfei = SjYuFei;
                        }
                        else if ((weightNumCount.compareTo(XjWeightYuFei) != 1 && weightNumCount.compareTo(SjWeightYuFei) != -1) && StringUtils.isNotEmpty(freightModel.getPackage_settings()) && productNum < Integer.parseInt(freightModel.getPackage_settings()))
                        {
                            yunfei = XjYuFei.add(SjYuFei);
                        }
                        else if (weightNumCount.compareTo(XjWeightYuFei) == 1 && StringUtils.isNotEmpty(freightModel.getPackage_settings()) && productNum < Integer.parseInt(freightModel.getPackage_settings()))
                        {
                            //商品-首件  <= 续件       首件运费+续件运费
                            if ((weightNumCount.subtract(SjWeightYuFei)).compareTo(XjWeightYuFei) != 1)
                            {
                                yunfei = XjYuFei.add(SjYuFei);
                            }
                            //商品-首件  > 续件    (商品重量-首件重量)/续件重量   进一取整    首件运费+进一取整*续件运费
                            if ((weightNumCount.subtract(SjWeightYuFei)).compareTo(XjWeightYuFei) == 1)
                            {
                                BigDecimal JYQZ = (weightNumCount.subtract(new BigDecimal(defaultFreightVO.getNum1()))).divide(new BigDecimal(defaultFreightVO.getNum3()), 0, BigDecimal.ROUND_UP);
                                yunfei = SjYuFei.add(JYQZ.multiply(XjYuFei));
                            }
                        }
                        else if (StringUtils.isNotEmpty(freightModel.getPackage_settings()) && productNum >= Integer.parseInt(freightModel.getPackage_settings()))
                        {
                            yunfei = new BigDecimal("0.0");
                        }
                        else
                        {
                            yunfei = SjYuFei;
                        }
                    }
                    else
                    {
                        if (StringUtils.isNotEmpty(freightModel.getPackage_settings()) && productNum >= Integer.parseInt(freightModel.getPackage_settings()))
                        {
                            yunfei = new BigDecimal("0.0");
                        }
                        else
                        {
                            yunfei = SjYuFei;
                        }
                    }
                }
            }
        }
        catch (LaiKeAPIException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            logger.debug("运费计算 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YFJSSB, "运费计算失败", "getFreight");
        }
        return yunfei;
    }

    @Override
    public String createOrderNo(String orderType) throws LaiKeAPIException
    {
        try
        {

            if (StringUtils.isEmpty(orderType))
            {
                orderType = DictionaryConst.OrdersType.ORDERS_HEADER_GM;
            }
            StringBuilder sNo = new StringBuilder();
            // 纳秒级时间戳
            long timestamp = Instant.now().toEpochMilli() * 1000_000 + System.nanoTime() % 1000_000;
            // 线程安全的随机数生成器
            ThreadLocalRandom random = ThreadLocalRandom.current();

            sNo.append(orderType)
                    .append(timestamp)
                    .append(random.nextInt(10))
                    .append(random.nextInt(10))
                    .append(random.nextInt(10))
                    .append(random.nextInt(10))
                    .append(random.nextInt(10))
                    .append(random.nextInt(10));

            // 检查唯一性
            OrderModel orderModel = new OrderModel();
            orderModel.setsNo(sNo.toString());
            orderModel = orderModelMapper.selectOne(orderModel);

            if (orderModel == null) {
                return sNo.toString();
            } else {
                // 如果重复，重新生成（避免递归）
                return createOrderNo(orderType);
            }
            //分布式唯一ID生成器：Snowflake算法或UUID，避免依赖数据库查询。
            //缓存机制：将生成的订单号缓存到Redis等内存数据库，减少数据库查询。
            //批量生成：预生成一批订单号，减少实时生成的压力。
//            String uniqueId = UuidCreator.getTimeOrdered().toString().replace("-", "");
//            return orderType + uniqueId;
        }
        catch (Exception e)
        {
            logger.error("创建订单号异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CJDDHSB, "创建订单号失败", "createOrderNo");
        }
    }

    @Override
    public Map<String, Object> bMchOrderIndex(MchPcOrderIndexVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            //查询参数列表
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", vo.getStoreId());
            parmaMap.put("mch_id", vo.getShopId());
            parmaMap.put("group_sNo", "group_sNo");
            parmaMap.put("add_time_sort", DataUtils.Sort.DESC.toString());
            parmaMap.put("pageStart", vo.getPageNo());
            parmaMap.put("pageEnd", vo.getPageSize());
            if (StringUtils.isNotEmpty(vo.getOrderno()))
            {
                parmaMap.put("order_like", vo.getOrderno());
            }
            if (StringUtils.isNotEmpty(vo.getStartDate()))
            {
                parmaMap.put("startDate", vo.getStartDate());
            }
            if (StringUtils.isNotEmpty(vo.getEndDate()))
            {
                parmaMap.put("endDate", vo.getEndDate());
            }
            if (StringUtils.isNotEmpty(vo.getOrderType()))
            {
                parmaMap.put("otype", vo.getOrderType().toUpperCase());
            }
            //运输方式
            if (StringUtils.isNotEmpty(vo.getDeliverType()))
            {
                parmaMap.put("self_lifting", vo.getDeliverType());
            }
            if (vo.getOrderStauts() != null)
            {
                switch (vo.getOrderStauts())
                {
                    case ORDERS_R_STATUS_UNPAID:
                        parmaMap.put("status", ORDERS_R_STATUS_UNPAID);
                        break;
                    case ORDERS_R_STATUS_CONSIGNMENT:
                        parmaMap.put("status", ORDERS_R_STATUS_CONSIGNMENT);
                        break;
                    case ORDERS_R_STATUS_DISPATCHED:
                        parmaMap.put("status", ORDERS_R_STATUS_DISPATCHED);
                        break;
                    case ORDERS_R_STATUS_COMPLETE:
                        parmaMap.put("status", ORDERS_R_STATUS_COMPLETE);
                        break;
                    case ORDERS_R_STATUS_CLOSE:
                        parmaMap.put("status", ORDERS_R_STATUS_CLOSE);
                        break;
                }
            }

            List<Map<String, Object>> orderList = orderModelMapper.selectbMchOrderIndex(parmaMap);
            int                       total     = orderModelMapper.countbMchOrderIndex(parmaMap);
            for (Map<String, Object> map : orderList)
            {
                Map<String, Object> resultOrderMap = new HashMap<>(16);
                int                 orderId        = MapUtils.getIntValue(map, "id");
                String              orderno        = MapUtils.getString(map, "sNo");
                int                 orderStatus    = MapUtils.getIntValue(map, "status");
                String              otype          = MapUtils.getString(map, "otype");
                //支付方式
                String payType = map.get("payName") + "";
                if (StringUtils.isEmpty(payType))
                {
                    payType = "钱包";
                }
                //计算订单成本价
                List<Map<String, Object>> priceInfoList = orderDetailsModelMapper.getbMchOrderIndexAmt(orderno, vo.getStoreId());
                BigDecimal                costPrice     = new BigDecimal("0");
                for (Map<String, Object> priceInfo : priceInfoList)
                {
                    costPrice = costPrice.add(new BigDecimal(priceInfo.get("num") + "").multiply(new BigDecimal(priceInfo.get("costprice") + "")));
                }
                resultOrderMap.put("orderno", orderno);
                StringBuilder address = new StringBuilder(map.get("sheng").toString());
                address.append(map.get("shi").toString()).append(map.get("xian").toString()).append(map.get("address").toString());
                resultOrderMap.put("address", address);
                resultOrderMap.put("status", orderStatus);
                resultOrderMap.put("pay", payType);

                //订单明细
                List<Map<String, Object>> orderDetailList = orderDetailsModelMapper.getbMchOrderIndexDetail(orderno);
                //当前订单物流集
                List<String> wuliyList = new ArrayList<>();
                //统计当前订单总运费
                BigDecimal yunFei = BigDecimal.ZERO;
                //订单状态
                Set<Integer> orderStatusList = new HashSet<>();
                for (Map<String, Object> detail : orderDetailList)
                {
                    BigDecimal yf = new BigDecimal(MapUtils.getString(detail, "freight"));
                    yunFei = yunFei.add(yf);
                    //物流id
                    Integer expressId = MapUtils.getInteger(detail, "express_id");
                    if (expressId != null)
                    {
                        ExpressModel expressModel = new ExpressModel();
                        expressModel.setId(expressId);
                        expressModel = expressModelMapper.selectOne(expressModel);
                        if (expressModel != null)
                        {
                            String wuliu = String.format("%s(%s)", expressModel.getId(), expressModel.getKuaidi_name());
                            wuliyList.add(wuliu);
                        }
                    }
                    String imgUrl = MapUtils.getString(detail, "imgurl");
                    detail.put("imgUrl", publiceService.getImgPath(imgUrl, vo.getStoreId()));
                    //如果明细状态都是 7 则代修改订单为相同状态
                    int status = MapUtils.getIntValue(detail, "r_status");
                    if (ORDERS_R_STATUS_CLOSE == status)
                    {
                        orderStatusList.add(status);
                    }
                }
                //是否处于同一状态
                if (orderStatusList.size() == 1)
                {
                    //状态是否未发生变化
                    if (!orderStatusList.contains(orderStatus) && !orderStatusList.isEmpty())
                    {
                        OrderModel orderUpdate = new OrderModel();
                        orderUpdate.setId(orderId);
                        orderUpdate.setStatus(orderStatus = orderStatusList.iterator().next());
                        int row = orderModelMapper.updateByPrimaryKeySelective(orderUpdate);
                        if (row < 1)
                        {
                            logger.debug("{} 订单和明细状态不一致,修改失败", orderno);
                        }
                    }
                }
                switch (orderStatus)
                {
                    case ORDERS_R_STATUS_UNPAID:
                        resultOrderMap.put("status", "待付款");
                        break;
                    case ORDERS_R_STATUS_CONSIGNMENT:
                        resultOrderMap.put("status", "待发货");
                        if (DictionaryConst.OrdersType.ORDERS_HEADER_PT.equalsIgnoreCase(otype))
                        {
                            resultOrderMap.put("status", "拼团成功");
                        }
                        break;
                    case ORDERS_R_STATUS_DISPATCHED:
                        resultOrderMap.put("status", "待付款");
                        if (DictionaryConst.OrdersType.ORDERS_HEADER_PT.equalsIgnoreCase(otype))
                        {
                            resultOrderMap.put("status", "拼团成功");
                        }
                        break;
                    case ORDERS_R_STATUS_COMPLETE:
                        resultOrderMap.put("status", "订单完成");
                        if (DictionaryConst.OrdersType.ORDERS_HEADER_PT.equalsIgnoreCase(otype))
                        {
                            resultOrderMap.put("status", "拼团成功");
                        }
                        break;
                    case ORDERS_R_STATUS_CLOSE:
                        resultOrderMap.put("status", "订单关闭");
                        if (DictionaryConst.OrdersType.ORDERS_HEADER_PT.equalsIgnoreCase(otype))
                        {
                            resultOrderMap.put("status", "拼团成功");
                        }
                        break;
                    default:
                        break;
                }
                resultOrderMap.put("createDate", MapUtils.getString(map, "add_time"));
                resultOrderMap.put("freight", yunFei);
                resultOrderMap.put("courier_num", wuliyList);
                resultOrderMap.put("products", orderDetailList);

                map.clear();
                map.putAll(resultOrderMap);
            }

            resultMap.put("list", orderList);
            resultMap.put("total", total);
        }
        catch (Exception e)
        {
            logger.error("订单列表（pc店铺端） 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "aMchOrderIndex");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> pcMchOrderIndex(AdminOrderListVo vo) throws LaiKeAPIException
    {

        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", vo.getStoreId());
//            parmaMap.put("user_id", vo.getUserId());
            //条件查询参数
            if (vo.getReType() != null) {
                parmaMap.put("reType", vo.getReType());
            }
            if (vo.getrType() != null) {
                parmaMap.put("rType", vo.getrType());
            }
            if (vo.getOrderType() != null) {
                parmaMap.put("isReturn", vo.getOrderType());
            }
            if (StringUtils.isNotEmpty(vo.getOrderNo()))
            {
                parmaMap.put("sNo", vo.getOrderNo());
            }

            parmaMap.put("operation_type", vo.getOperationType());
            if (StringUtils.isNotEmpty(vo.getKeyWord())) {
                parmaMap.put("sNo", StringUtils.getKey(vo.getKeyWord()));
            }
            //56374 平台统计所有的订单，不填入mch_id查询所有店铺信息
            if (vo.getMchId() != null && !GloabConst.LktConfig.LKT_CONFIG_TYPE_PT.toString().equals(vo.getOperator()))
            {
                parmaMap.put("mch_Id", vo.getMchId());
            }
            //特殊订单
            if (vo.getSelfLifting() != null)
            {
                Integer      selfLifting = 0;
                String       orderType   = null;
                List<String> list        = new ArrayList<>();
                //特殊订单分类 1=实物订单 2=自提订单(GM) 3=虚拟订单 4=活动订单 ,7=积分,8=秒杀, 9=,10=分销订单 11自提订单(GM + MS) 12:直播订单
                switch (vo.getSelfLifting())
                {
                    case 0:
                        list.add(DictionaryConst.OrdersType.ORDERS_HEADER_GM);
                        //list.add(DictionaryConst.OrdersType.ORDERS_HEADER_VI);
                        parmaMap.put("orderTypeList", list);
                        //平台不显示商家自配的订单
                        if (GloabConst.LktConfig.LKT_CONFIG_TYPE_PT.toString().equals(vo.getOperator()))
                        {
                            List<String> selfLiftingNotInList = new ArrayList<>();
                            selfLiftingNotInList.add("2");
                            parmaMap.put("selfLiftingNotInList", selfLiftingNotInList);
                        }
                        selfLifting = null;
                        break;
                    case 1:
                        //实物订单
                        list.add(DictionaryConst.OrdersType.ORDERS_HEADER_GM);
                        parmaMap.put("orderTypeList", list);
                        break;
                    case 3:
                        orderType = DictionaryConst.OrdersType.ORDERS_HEADER_VI;
                        selfLifting = 3;
                        break;
                    case 4:
                        list.add(DictionaryConst.OrdersType.ORDERS_HEADER_GM);
                        list.add(DictionaryConst.OrdersType.ORDERS_HEADER_VI);
                        parmaMap.put("orderTypeList_not", list);
                        break;
                    case 5:
                        selfLifting = 2;
                        break;
                    case 2:
                        //pc核销端的订单显示
                        if (GloabConst.LktConfig.LKT_CONFIG_TYPE_MCH_WRITE.toString().equals(vo.getOperator()))
                        {
                            list.add(DictionaryConst.OrdersType.ORDERS_HEADER_GM);
                            list.add(DictionaryConst.OrdersType.ORDERS_HEADER_MS);
                        }
                        else
                        {
                            list.add(DictionaryConst.OrdersType.ORDERS_HEADER_GM);
                        }
                        parmaMap.put("orderTypeList", list);
                        //1为自提订单
                        selfLifting = 1;
                        break;
                    case 6:
                        orderType = DictionaryConst.OrdersType.ORDERS_HEADER_JP;
                        break;
                    case 7:
                        orderType = DictionaryConst.OrdersType.ORDERS_HEADER_IN;
                        break;
                    case 8:
                        orderType = DictionaryConst.OrdersType.ORDERS_HEADER_MS;
                        if (StringUtils.isNotEmpty(vo.getPluginSelfLifting()))
                        {
                            selfLifting = vo.getPluginSelfLifting();
                        }
                        else
                        {
                            selfLifting = null;
                        }
                        break;
                    case 9:
                        //只显示拼团成功的订单
                        orderType = DictionaryConst.OrdersType.ORDERS_HEADER_PT;
                        parmaMap.put("groupStatus", GroupOpenModel.Status.GROUP_GOODS_STATUS_SUCCESS.getKey());
                        parmaMap.put("noStatus", String.valueOf(DictionaryConst.OrdersStatus.ORDERS_R_STATUS_UNPAID));
                        break;
                    case 10:
                        orderType = DictionaryConst.OrdersType.ORDERS_HEADER_FX;
                        break;
                    case 11:
                        //限时折扣
                        orderType = DictionaryConst.OrdersType.ORDERS_HEADER_FS;
                        break;
                    case 20:
                        //拼团特殊处理-只显示拼团成功的订单
                        List<Integer> groupStatus = new ArrayList<>();
                        //拼团  -》禅道48417 增加拼团失败订单
                        groupStatus.add(GroupOpenModel.Status.GROUP_GOODS_STATUS_SUCCESS.getKey());
                        groupStatus.add(GroupOpenModel.Status.GROUP_GOODS_STATUS_FAIL.getKey());
                        parmaMap.put("groupStatusList", groupStatus);
                        //拼团创建不显示待付款订单  禅道45216
                        parmaMap.put("noStatus", String.valueOf(DictionaryConst.OrdersStatus.ORDERS_R_STATUS_UNPAID));
                        break;
                    case 21:
                        //h5拼团店铺特殊处理-只显示拼团失败的订单
                        orderType = DictionaryConst.OrdersType.ORDERS_HEADER_PT;
                        parmaMap.put("groupStatus", GroupOpenModel.Status.GROUP_GOODS_STATUS_FAIL.getKey());
                        parmaMap.put("noStatus", String.valueOf(DictionaryConst.OrdersStatus.ORDERS_R_STATUS_UNPAID));
                        break;
                    case 12:
                        orderType = DictionaryConst.OrdersType.ORDERS_HEADER_ZB;
                        break;
                    case 13:
                        //供应商
                        orderType = DictionaryConst.OrdersType.ORDERS_HEADER_GM;
                        parmaMap.put("supplierOrder", "supplierOrder");
                        break;
                }
                if (list.size() == 0)
                {
                    parmaMap.put("orderType", orderType);
                }
                if (selfLifting != null)
                {
                    parmaMap.put("self_lifting", selfLifting);
                    if (orderType != null && orderType.equals(DictionaryConst.OrdersType.ORDERS_HEADER_VI))
                    {
                        //虚拟商品，selfLifting包含3，4
                        parmaMap.remove("self_lifting");
                    }
                }
            }

            if (!Objects.isNull(vo.getSupplierId()))
            {
                parmaMap.put("supplierId", vo.getSupplierId());
            }
            parmaMap.put("mch_name", vo.getMchName());
            parmaMap.put("ostatus", vo.getStatus());

            parmaMap.put("startdate", vo.getStartDate());
            parmaMap.put("enddate", vo.getEndDate());
            parmaMap.put("start", vo.getPageNo());
            parmaMap.put("pagesize", vo.getPageSize());
            parmaMap.put("group_sNo", "group_sNo");
            parmaMap.put("add_time_sort", DataUtils.Sort.DESC.toString());
            if (vo.getId() != null)
            {
                parmaMap.put("orderId", vo.getId());
            }
            //todo-start-plugin-meger 禅道49609 -》用户、商家、平台删除自己的订单
            if (vo.getStoreType() == GloabConst.StoreType.STORE_TYPE_PC_ADMIN)
            {
                parmaMap.put("storeRecycle", OrderModel.SHOW);
            }
            else if (vo.getStoreType() == GloabConst.StoreType.STORE_TYPE_PC_MCH
                    || vo.getStoreType() == GloabConst.StoreType.STORE_TYPE_APP
                    || vo.getStoreType() == GloabConst.StoreType.STORE_TYPE_H5
                    || vo.getStoreType() == GloabConst.StoreType.STORE_TYPE_WX_MP)
            {
                parmaMap.put("mchRecycle", OrderModel.SHOW);
            }
            //todo-end
            //三个后台管理系统的订单按订单状态顺序排序，再此处直接拼sql语句，虚拟商品需要特殊处理排序
            /*
            if (vo.getStoreType() == GloabConst.StoreType.STORE_TYPE_PC_ADMIN || vo.getStoreType() == GloabConst.StoreType.STORE_TYPE_PC_MCH)
            {
                //虚拟订单
                if (vo.getSelfLifting() == 3)
                {
                    parmaMap.put("order_status_sort", "0,8,5,7");
                }
                else
                {
                    parmaMap.put("order_status_sort", "0,1,2,5,7");
                }
            }*/
            //管理平台订单剔除待付款订单中包含供应商商品的订单
            if (GloabConst.LktConfig.LKT_CONFIG_TYPE_PT.toString().equals(vo.getOperator()))
            {
                parmaMap.put("eliminateContainSupplier", "eliminateContainSupplier");
                //禅道49609 -》用户、商家、平台删除自己的订单
                parmaMap.put("storeRecycle", OrderModel.SHOW);
            }
            else if (GloabConst.LktConfig.LKT_CONFIG_TYPE_PC.toString().equals(vo.getOperator()))
            {
                parmaMap.put("mchRecycle", OrderModel.SHOW);
            }
            //门店查看订单处理
            if (StringUtils.isNotEmpty(vo.getMchAdminId()))
            {
                if (StringUtils.isNotEmpty(vo.getStatus()) && vo.getStatus().equals(ORDERS_R_STATUS_COMPLETE))
                {
                    parmaMap.put("mchAdminId", vo.getMchAdminId());
                }
                else
                {
                    //以核销订单筛选核销人
                    parmaMap.put("mchAdminId2", vo.getMchAdminId());
                }
            }
            if (StringUtils.isNotEmpty(vo.getMchStoreId()))
            {
                parmaMap.put("mchStoreId", vo.getMchStoreId());
            }
            List<Map<String, Object>> resultList = new ArrayList<>();
            if (vo.getMchId() != null && GloabConst.LktConfig.LKT_CONFIG_TYPE_PC.toString().equals(vo.getStoreType()))
            {
                parmaMap.put("mch_pro", vo.getMchId());
            }
            int                       total     = orderModelMapper.countAdminOrderList(parmaMap);
            List<Map<String, Object>> orderList = new ArrayList<>();
            if (total > 0)
            {
//                logger.error("目标sql来了为y！！！！！！！！！");
                orderList = orderModelMapper.adminOrderList(parmaMap);

                for (Map<String, Object> map : orderList) {

                    if (Objects.nonNull(vo.getSource()) && Objects.equals(vo.getSource(), 1)) {
                        map.put("source", vo.getSource());
                    }
                    String orderNo = MapUtils.getString(map, "sNo");

                    BigDecimal exchangeRate = new BigDecimal(MapUtils.getDoubleValue(map, "exchange_rate", 1));
                    logger.info("订单号：{},汇率：{}", orderNo);

                    //订单详情id
                    int detailId = MapUtils.getIntValue(map, "detailId");
                    //订单商品个数  禅道56651  将商品数量修改为该店铺详单
                    int goodsNum = MapUtils.getIntValue(map, "goodsNum");
                    if (StringUtils.isNotEmpty(map.get("d_num"))) {
                        goodsNum = DataUtils.getIntegerVal(map, "d_num");
                    }

                    //商品数量
                    int needNum = MapUtils.getIntValue(map, "needNum");
                    //商品id
                    int pid = MapUtils.getIntValue(map, "p_id");
                    //规格id
                    int sid = MapUtils.getIntValue(map, "sid");
                    //运费
                    String freight = MapUtils.getString(map, "freight");
                    //订单状态
                    int status = MapUtils.getIntValue(map, "status");
                    //供应商id
                    Integer supplierId = MapUtils.getInteger(map, "gongyingshang");
                    String otype = MapUtils.getString(map, "otype");
                    //拼团的old_freight 是null，直接拿z_freight计算汇率会报错
                    if (otype.equals(DictionaryConst.OrdersType.ORDERS_HEADER_PT)) {
                        map.put("old_freight", freight);
                    } else {
                        map.put("z_freight", MapUtils.getString(map, "old_freight"));
                    }
                    if (StringUtils.isNotEmpty(supplierId))
                    {
                        SupplierOrderFrightModel supplierOrderFrightModel = new SupplierOrderFrightModel();
                        supplierOrderFrightModel.setsNo(orderNo);
                        List<SupplierOrderFrightModel> select = supplierOrderFrightModelMapper.select(supplierOrderFrightModel);
                        //总运费
                        BigDecimal zFreight = BigDecimal.ZERO;
                        for (SupplierOrderFrightModel model : select)
                        {
                            zFreight = zFreight.add(model.getFreight());
                        }
                        BigDecimal tmpZFreight = zFreight;
                        map.put("freight", tmpZFreight);
                        map.put("z_freight", tmpZFreight);
                        freight = MapUtils.getString(map, "freight");
                        if (vo.getSupplierId() != null)
                        {
                            BigDecimal supplierSettlement = new BigDecimal(MapUtils.getString(map, "supplier_settlement"));
                            BigDecimal orderPrice         = supplierSettlement.add(zFreight);
                            map.put("z_price", orderPrice);
                        }
                    }

                    //导出需要导出明细,如果导出明细则会导致数量与前台不一致,这里先查询是否有多条明细,如果当前订单有多条明细则把明细放入集合
                    if (vo.getExportType().equals(1))
                    {
                        Map<String, Object> orderParamMap = new HashMap<>(16);
                        orderParamMap.put("orderNo1", orderNo);
                        orderParamMap.put("group_detail_id", "group_detail_id");
                        OrderDetailsModel orderDetailsModel = new OrderDetailsModel();
                        orderDetailsModel.setR_sNo(orderNo);
                        int count = orderDetailsModelMapper.selectCount(orderDetailsModel);

                        if (count > 1)
                        {
                            List<Map<String, Object>> orderDetailList = orderModelMapper.adminOrderList(orderParamMap);
                            for (Map<String, Object> detailMap : orderDetailList)
                            {
                                if (Objects.nonNull(vo.getSource()) && Objects.equals(vo.getSource(), 1))
                                {
                                    detailMap.put("source", vo.getSource());
                                }
                                detailMap.put("goodsNum", goodsNum);
                                //详单发货状态修改
                                if ((MapUtils.getIntValue(detailMap, "r_status") < ORDERS_R_STATUS_DISPATCHED
                                        && StringUtils.isNotEmpty(MapUtils.getString(detailMap, "courier_num")))
                                        || MapUtils.getIntValue(detailMap, "r_status") == ORDERS_R_STATUS_DISPATCHED)
                                {
                                    detailMap.put("status", ORDERS_R_STATUS_DISPATCHED);
                                }
                                else if (MapUtils.getIntValue(detailMap, "r_status") == ORDERS_R_STATUS_CLOSE)
                                {
                                    detailMap.put("status", ORDERS_R_STATUS_CLOSE);
                                }
                                pcMchOrderIndex(detailMap, vo.getStoreId(), resultList);
                            }
                            continue;
                        }
                    }
                    map.put("operator", vo.getOperator());
                    //是否有电子面单发货
                    if (expressDeliveryModelMapper.countBysNo(vo.getStoreId(), orderNo) > 0)
                    {
                        map.put("logistics_type", true);
                    }
                    else
                    {
                        map.put("logistics_type", false);
                    }
                    //虚拟订单，自营店只能核销自己的订单
                    if (GloabConst.LktConfig.LKT_CONFIG_TYPE_PT.toString().equals(vo.getOperator()) && !parmaMap.containsKey("self_lifting"))
                    {
                        //查找出不同商城的自营店
                        Integer storeMchId = customerModelMapper.getStoreMchId(vo.getStoreId());
                        map.put("admin_store", storeMchId);
                    }
                    //导出end
                    pcMchOrderIndex(map, vo.getStoreId(), resultList);
                }
            }
            // 当请求售后订单列表时，过滤掉没有售后信息的订单
//            if ("return".equals(vo.getOrderType()) && !resultList.isEmpty())
//            {
//                Iterator<Map<String, Object>> iterator      = resultList.iterator();
//                int                           filteredCount = 0; // 记录过滤掉的数量
//
//                while (iterator.hasNext())
//                {
//                    Map<String, Object> order = iterator.next();
//                    if (!order.containsKey("re_type"))
//                    {
//                        iterator.remove(); // 移除不包含re_type的订单
//                        filteredCount++;
//                    }
//                }
//
//                // 更新总数量
//                total = Math.max(0, total - filteredCount);
//                logger.info("过滤售后订单：原总数={}, 过滤后={}", total + filteredCount, total);
//            }

            resultMap.put("total", total);
            resultMap.put("list", resultList);
        }
        catch (Exception e)
        {
            logger.error("订单列表 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "pcMchOrderIndex");
        }
        return resultMap;
    }

    //订单列表辅助方法
    private void pcMchOrderIndex(Map<String, Object> map, int storeId, List<Map<String, Object>> resultList)
    {
        try
        {
            Map<String, Object> res = new HashMap<>(16);
            res.put("id", MapUtils.getString(map, "id"));
            String cpc = MapUtils.getString(map, "cpc");
            String shop_cpc = MapUtils.getString(map, "shop_cpc");
            res.put("orderDetailIds", MapUtils.getString(map, "detailId"));
            res.put("haveReturnOrder", DictionaryConst.WhetherMaven.WHETHER_NO);
            //判断有无售后未结束
            int unFinishShouHouOrderNum = returnOrderModelMapper.orderDetailReturnIsNotEnd(storeId, MapUtils.getString(map, "sNo"), MapUtils.getIntValue(map, "detailId"));
            //有售后未结束不能发货 currency_symbol
            //有售后未结束不能发货 currency_symbole
            if (unFinishShouHouOrderNum > 0)
            {
                res.put("haveReturnOrder", DictionaryConst.WhetherMaven.WHETHER_OK);
            }

            //价格
            Integer pid = MapUtils.getInteger(map, "supplier_superior");
            if (Objects.nonNull(pid))
            {
                ConfiGureModel supplierSuperior = confiGureModelMapper.getProductMinPriceAndMaxYprice(pid);
                if (Objects.nonNull(supplierSuperior) && Objects.nonNull(supplierSuperior.getPrice()))
                {
                    res.put("supplierGoodsPrice", supplierSuperior.getYprice());
                }
            }
            res.put("detailId", MapUtils.getString(map, "detailId"));
            String sNo = MapUtils.getString(map, "sNo");
            res.put("review_status",MapUtils.getInteger(map,"review_status"));
            res.put("voucher",MapUtils.getString(map,"voucher"));
            res.put("sNo",MapUtils.getString(map,"sNo"));

            //返回订单售后情况 lkt_return_order
            ReturnOrderModel returnOrderModel1 = returnOrderModelMapper.getReturnNewInfoBySno(sNo);

            if (returnOrderModel1 != null)
            {
                //类型 100:不在退货退款状态0:审核中 1:同意并让用户寄回 2:拒绝(退货退款) 3:用户已快递 4:收到寄回商品,同意并退款 5：拒绝并退回商品 8:拒绝(退款) 9:同意并退款 10:拒绝(售后)
                // 11:同意并且寄回商品 12售后结束 13最终状态-人工审核成功  14 人工审核失败(暂时没有这种状态) 15 订单未发货-系统自动急速退款 16 收到回寄商品,确定不影响二次销售
                res.put("r_type", returnOrderModel1.getR_type());
                //退款类型 1:退货退款  2:退款 3:换货
                res.put("re_type", returnOrderModel1.getRe_type());
                //退款原因
                res.put("content", returnOrderModel1.getContent());
                //实际退款金额
                res.put("real_money", returnOrderModel1.getReal_money());
                //退款金额
                res.put("re_money", returnOrderModel1.getRe_money());
                //退款id
                res.put("re_id", returnOrderModel1.getId());
                //申请退款时间
                res.put("re_time", returnOrderModel1.getRe_time());
                // 补充prompt状态描述逻辑
                int    rType  = returnOrderModel1.getR_type();
                int    reType = returnOrderModel1.getRe_type();
                String prompt;

                if ((rType == 1 || rType == 3 || rType == 16) && (reType == 1 || reType == 2))
                {
                    prompt = "退款中";
                }
                else if (rType == 4 || rType == 9 || rType == 15 || rType == 13)
                {
                    prompt = "退款成功";
                }
                else if ((rType == 2 || rType == 5 || rType == 8 || rType == 10) && reType != 3)
                {
                    prompt = "退款失败";
                }
                else if ((rType == 1 || rType == 3 || rType == 11) && reType == 3)
                {
                    prompt = "换货中";
                }
                else if (rType == 12)
                {
                    prompt = "换货成功";
                }
                else if ((rType == 5 || rType == 10) && reType == 3)
                {
                    prompt = "换货失败";
                }
                else
                {
                    prompt = "待审核";
                }

                res.put("prompt", prompt);
            }

            if (returnOrderModel1 != null)
            {
                //region 是否可以审核
                boolean isExamine;
                res.put("isExamine", isExamine = publicRefundService.isExamine(storeId, returnOrderModel1.getId()));
                //endregion

                //region 是否显示人工受理按钮 商品回寄之后被拒绝
                boolean isManExamine = false;
                if (!isExamine)
                {
                    isManExamine = publicRefundService.isMainExamine(storeId, returnOrderModel1.getId());
                }
                res.put("isManExamine", isManExamine);
            }

            //返回订单相关信息 lkt_order
            OrderModel orderModel = orderModelMapper.selectBySno(sNo);
            //订单状态
            res.put("order_status", orderModel.getStatus());

            BigDecimal exchangeRate = new BigDecimal(MapUtils.getDoubleValue(map, "exchange_rate", 1));
            logger.info("pcMchOrderIndex:订单号：{},汇率：{}", sNo);

            res.put("orderno", sNo);
            res.put("mchOrderNo", MapUtils.getString(map, "real_sno"));
            res.put("createDate", DateUtil.dateFormate(MapUtils.getString(map, "add_time"), GloabConst.TimePattern.YMDHMS));
            res.put("mchName", MapUtils.getString(map, "shopName"));
            res.put("goodsName", MapUtils.getString(map, "product_title"));
            //商品图片
            String imgUrl = publiceService.getImgPath(MapUtils.getString(map, "img"), storeId);
            res.put("goodsImgUrl", imgUrl);
            //处理规格
            String attrStr = GoodsDataUtils.getProductSkuValue(MapUtils.getString(map, "attribute"));
            res.put("attrStr", attrStr);

            res.put("needNum", MapUtils.getString(map, "needNum"));
            res.put("num", MapUtils.getString(map, "num"));
            //todo-plugin res.put("goodsPrice", MapUtils.getString(map, "goodsAmt"));
            res.put("goodsPrice", new BigDecimal(MapUtils.getString(map, "goodsPrice")));
            //拼团显示商品原售价
            if (map.containsKey("source") && MapUtils.getIntValue(map, "source") == 1)
            {
                double goodsAmt = MapUtils.getDoubleValue(map, "goodsAmt");
                res.replace("goodsPrice", new BigDecimal(goodsAmt));
            }

            res.put("currency_symbol", MapUtils.getString(map, "currency_symbol"));
            res.put("currency_code", MapUtils.getString(map, "currency_code"));
            res.put("exchange_rate", MapUtils.getString(map, "exchange_rate"));
            res.put("orderPrice", new BigDecimal(MapUtils.getString(map, "z_price")));
            res.put("old_total", new BigDecimal(MapUtils.getString(map, "old_total")));
            res.put("write_off_settings", MapUtils.getString(map, "write_off_settings"));
            //todo-plugin
            res.put("detailGoodsPrice", new BigDecimal(MapUtils.getString(map, "detail_goods_price", "0")));
            //虚拟商品特有参数
            if (Objects.equals(MapUtils.getInteger(map, "commodity_type"), ProductListModel.COMMODITY_TYPE.virtual))
            {
                res.put("write_off_settings", MapUtils.getString(map, "write_off_settings"));
                res.put("is_appointment", MapUtils.getString(map, "is_appointment"));
                Integer mchId = MapUtils.getInteger(map, "mchId");
                if (map.containsKey("admin_store") && !Strings.isNullOrEmpty(DataUtils.getStringVal(map, "admin_store")) && MapUtils.getInteger(map,"admin_store") == mchId)
                {
                    res.put("showHX", 1);
                }
            }
            //下单类型1用户下单2店铺下单3平台下单
            res.put("operation_type", MapUtils.getString(map, "operation_type"));
            switch (MapUtils.getString(map, "operation_type"))
            {
                case "1":
                    res.put("operationTypeName", "用户下单");
                    break;
                case "2":
                    res.put("operationTypeName", "店铺下单");
                    break;
                case "3":
                    res.put("operationTypeName", "平台下单");
                    break;
                default:
                    res.put("operationTypeName", "未知类型");
            }
            //订单状态
            res.put("orderStatus", MapUtils.getIntValue(map, "status"));
            //订单状态
            res.put("status", OrderDataUtils.getOrderStatus(MapUtils.getIntValue(map, "status")));
            //回显订单退款金额
            if (MapUtils.getIntValue(map, "status") == ORDERS_R_STATUS_CLOSE)
            {
                //获取退款信息
                ReturnOrderModel returnOrderModel = returnOrderModelMapper.getReturnNewInfoBySno(sNo);
                if (Objects.nonNull(returnOrderModel))
                {
                    res.put("return_money", returnOrderModel.getReal_money());
                    res.put("return_date", DateUtil.dateFormate(returnOrderModel.getAudit_time(), GloabConst.TimePattern.YMDHMS));
                }
                else
                {
                    res.put("return_money", MapUtils.getDoubleValue(map, "old_total"));
                }
            }
            //订单类型
            res.put("otype", OrderDataUtils.getOrderType(MapUtils.getString(map, "otype")));
            //订单商品数量
            res.put("goodsNum", MapUtils.getString(map, "goodsNum"));
            //优惠后的金额
            res.put("after_discount", new BigDecimal(MapUtils.getString(map, "after_discount")));
            //积分
            res.put("allow", String.format("%.0f", new BigDecimal(MapUtils.getString(map, "after_discount"))));
            //支付积分
            res.put("orderAllow", String.format("%.0f", new BigDecimal(MapUtils.getString(map, "allow"))));
            //禅道4465 管理后台退款成功的订单需要显示积分和金额都为零
//            4585 【JAVA开发环境】积分商城：管理后台--退款成功的，订单总价要么是100积分+￥100，要么是0积分+￥0
//            Integer rType = (Integer) res.get("r_type");
//            if (rType != null && (rType == 4 || rType == 9 || rType == 13 || rType == 15))
//            {
//                res.put("orderAllow", 0);
//            }
            //支付方式
            String payType = MapUtils.getString(map, "pay");
            res.put("pay", payType);
            if (StringUtils.isNotEmpty(payType))
            {
                PaymentModel paymentModel = new PaymentModel();
                paymentModel.setClass_name(payType);
                paymentModel = paymentModelMapper.selectOne(paymentModel);
                if (paymentModel != null)
                {
                    payType = paymentModel.getName();
                }
                //线下支付处理
                if (DictionaryConst.OrderPayType.OFFLINE_PAY.equals(payType))
                {
                    payType = "线下支付";
                }
            }
            if (StringUtils.isNotEmpty(DataUtils.getStringVal(map, "store_self_delivery")))
            {
                StoreSelfDeliveryModel store_self_delivery = storeSelfDeliveryModelMapper.selectByPrimaryKey(DataUtils.getStringVal(map, "store_self_delivery"));
                res.put("delivery_time", DateUtil.dateFormate(store_self_delivery.getDelivery_time(), GloabConst.TimePattern.YMD));
                res.put("delivery_period", store_self_delivery.getDelivery_period());
            }
            res.put("payName", payType);
            res.put("userId", MapUtils.getString(map, "user_id"));
            res.put("userName", MapUtils.getString(map, "name"));
            //分销订单userName格式和php不一致，返回两个
            res.put("user_name", MapUtils.getString(map, "name"));
            //获取物流信息
            StringBuffer              wuliuStr    = new StringBuffer(SplitUtils.DH);
            StringBuffer              courier_num = new StringBuffer(SplitUtils.DH);
            List<Map<String, Object>> mapList     = orderDetailsModelMapper.selectStoreOrderDetails(storeId, MapUtils.getString(map, "sNo"));

            //去重
            Map<String, Object> wuLiuMap = new HashMap<>(16);
            if (mapList.size() > 0)
            {
                mapList.forEach(expressStr ->
                {
                    res.put("p_integral", expressStr.get("p_integral"));
                    res.put("score_deduction", expressStr.get("score_deduction"));
                    if (StringUtils.isNotEmpty(MapUtils.getString(expressStr, "express_id")))
                    {
                        String[] exIds        = MapUtils.getString(expressStr, "express_id").split(SplitUtils.DH);
                        String[] courier_nums = MapUtils.getString(expressStr, "courier_num").split(SplitUtils.DH);
                        for (int i = 0; i < exIds.length; i++)
                        {
                            ExpressModel expressModel = expressModelMapper.selectByPrimaryKey(exIds[i]);
                            //物流单号
                            String courierNo = courier_nums[i];
                            if (expressModel != null && !wuLiuMap.containsKey(courierNo))
                            {
                                String logistics = String.format("%s(%s)", courierNo, expressModel.getKuaidi_name());
                                wuLiuMap.put(courierNo, expressModel.getKuaidi_name());
                                wuliuStr.append(logistics).append(SplitUtils.DH);
                                courier_num.append(courierNo).append(SplitUtils.DH);
                            }
                        }
                    }
                });
            }
            res.put("expressStr", wuliuStr);
            res.put("expressList", StringUtils.trim(wuliuStr.toString(), SplitUtils.DH).split(SplitUtils.DH));
            res.put("mobile", MapUtils.getString(map, "mobile"));
            String addressInfo = MapUtils.getString(map, "address");
            res.put("address", addressInfo);
            String sheng = MapUtils.getString(map, "sheng"), shi = MapUtils.getString(map, "shi"), xian = MapUtils.getString(map, "xian");
            //如果shop_cpc不为空，表示是自提订单
            if (StringUtils.isNotEmpty(shop_cpc))
            {
                cpc = shop_cpc;
            }
            res.put("addressInfo", CpcUtils.coverAddress(cpc,sheng,shi,xian,addressInfo));
            res.put("courier_num", StringUtils.trim(courier_num.toString(), SplitUtils.DH));
            res.put("freight", new BigDecimal(MapUtils.getString(map, "z_freight")));
            res.put("detailFreight", new BigDecimal(MapUtils.getString(map, "freight")));
            res.put("shopName", MapUtils.getString(map, "shopName"));
            res.put("self_lifting", MapUtils.getString(map, "self_lifting"));
            //运输方式
            switch (MapUtils.getString(map, "self_lifting"))
            {
                case "0":
                    res.put("selfLiftingName", "快递");
                    break;
                case "1":
                    res.put("selfLiftingName", "自提");
                    break;
                case "2":
                    res.put("selfLiftingName", "同城配送");
                    break;
                default:
                    res.put("selfLiftingName", "未知类型");
            }
            //res.put("selfLiftingName", MapUtils.getIntValue(map, "self_lifting") == 0 ? "快递" : "自提");
            //供应商名称
            res.put("supplierName", MapUtils.getString(map, "supplierName"));
            //供应商名称
            res.put("is_lssued", MapUtils.getString(map, "is_lssued"));

            //操作人
            res.put("operator", MapUtils.getString(map, "operator"));
            res.put("logistics_type", map.get("logistics_type"));

            //todo-plugins-s
            res.put("goodsId", MapUtils.getInteger(map, "goodsId"));
            res.put("old_total", new BigDecimal(MapUtils.getString(map, "old_total")));
            res.put("old_freight", new BigDecimal(MapUtils.getString(map, "old_freight")));
            //todo-plugins-e

            //虚拟商品特殊处理，去除运费和物流单号
            if (MapUtils.getString(map, "otype").equals(DictionaryConst.OrdersType.ORDERS_HEADER_VI))
            {
                res.remove("expressStr");
                res.remove("expressList");
                res.remove("freight");
            }
            resultList.add(res);
        }
        catch (Exception e)
        {
            logger.error("订单列表 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "pcMchOrderIndex");
        }
    }

    @Override
    public Map<String, Object> aMchOrderIndex(MchOrderIndexVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            int paymentNum = 0;
            int sendNum    = 0;
            int retyrnNum  = 0;

            //订单明细 一对多
            List<Map<String, Object>> orderDetailList = new ArrayList<>();
            //用于暂存订单主表信息 订单分组
            Map<String, Map<String, Object>> orderMap = new HashMap<>(16);

            //查询参数列表
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", vo.getStoreId());
            //parmaMap.put("order_mch_id", vo.getShopId());
            parmaMap.put("mch_id", vo.getShopId());
            if (StringUtils.isNotEmpty(vo.getOrderHeadrType()))
            {
                parmaMap.put("orderType", vo.getOrderHeadrType());
            }
            if (StringUtils.isNotEmpty(vo.getOrderHeadrType()))
            {
                parmaMap.put("orderStauts", vo.getOrderStauts());
            }
            if (StringUtils.isNotEmpty(vo.getIsSupplierPro()))
            {
                parmaMap.put("supplierPro", "supplierPro");
                parmaMap.remove("mch_id");
                parmaMap.put("supplierMchId",vo.getShopId());
            }
            if (StringUtils.isNotEmpty(vo.getIsSupplier()))
            {
                parmaMap.put("isSupplierPro", "isSupplierPro");
            }
            parmaMap.put("recycle1", DictionaryConst.ProductRecycle.NOT_STATUS);

            //统计当前店铺未付款数量
            Map<String, Object> unpaidParmaMap = new HashMap<>(16);
            unpaidParmaMap.putAll(parmaMap);
            List<String> statusList = new ArrayList<>();
            statusList.add(String.valueOf(DictionaryConst.OrdersStatus.ORDERS_R_STATUS_UNPAID));
            unpaidParmaMap.put("statusList", statusList);
            //统计当前店铺 未发货并且是配送 或者 待收货并且是自提 的数量
            Map<String, Object> notFinishedParmaMap = new HashMap<>(16);
            notFinishedParmaMap.putAll(parmaMap);
            notFinishedParmaMap.put("statusType", 3);
            //统计当前店铺售后订单 审核中、同意并让用户寄回、用户已快递 的数量
            Map<String, Object> returnOrderParmaMap = new HashMap<>(16);
            returnOrderParmaMap.putAll(parmaMap);
            List<String> rTypeList = new ArrayList<>();
            rTypeList.add(DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_EXAMEWAIT_STATUS.toString());
            rTypeList.add(DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_AGREE_REBACK.toString());
            rTypeList.add(DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_USER_DELIVERED.toString());
            returnOrderParmaMap.put("rTypeList", rTypeList);

            List<String> orderTypes = new ArrayList<>();

            if (StringUtils.isEmpty(vo.getOrderHeadrType()))
            {
                orderTypes.add(DictionaryConst.OrdersType.ORDERS_HEADER_GM);
                notFinishedParmaMap.put("orderType", DictionaryConst.OrdersType.ORDERS_HEADER_GM);
                unpaidParmaMap.put("orderType", DictionaryConst.OrdersType.ORDERS_HEADER_GM);
            }
            else
            {
                orderTypes.add(vo.getOrderHeadrType());
                notFinishedParmaMap.put("orderType", (vo.getOrderHeadrType()));
                unpaidParmaMap.put("orderType", (vo.getOrderHeadrType()));
            }
            returnOrderParmaMap.put("orderTypes", orderTypes);

            if (StringUtils.isNotEmpty(vo.getKeyword()))
            {
                unpaidParmaMap.put("likeOrderno", vo.getKeyword());
                notFinishedParmaMap.put("likeOrderno", vo.getKeyword());
                returnOrderParmaMap.put("likeOrderno", vo.getKeyword());
                parmaMap.put("likeOrderno", vo.getKeyword());
            }
            paymentNum = orderModelMapper.countOrdersNumDynamic(unpaidParmaMap);
            sendNum = orderModelMapper.countOrdersNumDynamic(notFinishedParmaMap);
            retyrnNum = returnOrderModelMapper.countRturnOrderNumDynamic(returnOrderParmaMap);

            //是否有商品已经发货，显示发货按钮
            boolean haveExpress = false;
            //判断是否为售后类型
            if (!"return".equals(vo.getOrderType()))
            {
                if ("payment".equals(vo.getOrderType()))
                {
                    //未付款
                    statusList = new ArrayList<>();
                    statusList.add(String.valueOf(DictionaryConst.OrdersStatus.ORDERS_R_STATUS_UNPAID));
                    parmaMap.put("statusList", statusList);
                }
                else if ("send".equals(vo.getOrderType()))
                {
                    //未发货，统计快递发货以及商家自配的订单
                    parmaMap.put("statusType", 3);
                }
                else if ("receipt".equals(vo.getOrderType()) && vo.getOrderHeadrType().equals(DictionaryConst.OrdersType.ORDERS_HEADER_VI))
                {
                    //待审核
                    parmaMap.put("statusType", 8);
                }
                //平台活动id
                if (vo.getPlatformActivitiesId() != null)
                {
                    parmaMap.put("platform_activities_id", vo.getPlatformActivitiesId());
                }

                //根据用户id和前台参数,查询订单表 (id、订单号、订单价格、添加时间、订单状态、优惠券id)
                List<String> recycleList = new ArrayList<>();
                recycleList.add(String.valueOf(OrderModel.RECYCLE_SHOW));
                recycleList.add(String.valueOf(OrderModel.RECYCLE_USER));
                parmaMap.put("store_id", vo.getStoreId());
                if (!StringUtils.isEmpty(vo.getKeyword()))
                {
                    parmaMap.put("likeOrderno", vo.getKeyword());
                }
                parmaMap.put("mchRecycle", OrderModel.SHOW);
                parmaMap.put("recycleList", recycleList);
                parmaMap.put("pageNo", vo.getPageNo());
                parmaMap.put("pageSize", vo.getPageSize());
                if (StringUtils.isEmpty(vo.getOrderHeadrType()))
                {
                    parmaMap.put("orderType", DictionaryConst.OrdersType.ORDERS_HEADER_GM);
                }
                else
                {
                    parmaMap.put("orderType", vo.getOrderHeadrType());
                }
                parmaMap.put("group_sNo", "group_sNo");
                List<Map<String, Object>> orderMapList = orderModelMapper.getOrdersNumDynamic(parmaMap);
                OrderDetailsModel         orderDetailsInfo;
                List<OrderDetailsModel>   orderDetailsModelsList;
                for (Map<String, Object> map : orderMapList)
                {
                    //主键id
                    int oid = Integer.parseInt(map.get("id").toString());
                    //订单号
                    String orderno = map.get("sNo").toString();
                    // 根据订单号,查询订单详情
                    orderDetailsInfo = new OrderDetailsModel();
                    orderDetailsInfo.setStore_id(vo.getStoreId());
                    orderDetailsInfo.setR_sNo(orderno);
                    orderDetailsModelsList = orderDetailsModelMapper.select(orderDetailsInfo);
                    for (OrderDetailsModel orderDetails : orderDetailsModelsList)
                    {
                        if (orderDetails.getDeliverNum() != null && orderDetails.getDeliverNum() > 0 && !"2".equals(DataUtils.getStringVal(map, "self_lifting")))
                        {
                            //2 为商家自配
                            haveExpress = true;
                        }
                        else
                        {
                            haveExpress = false;
                        }
                    }
                    if (orderDetailsModelsList.get(0).getWrite_time() != null)
                    {
                        if (!orderDetailsModelsList.get(0).getWrite_time().equals(""))
                        {
                            //是否预约，预约了为1，没预约为2
                            map.put("show_store_name", 1);
                        }
                    }
                    else
                    {
                        map.put("show_store_name", 2);
                    }

                    map.put("haveExpress", haveExpress);
                    //订单类型
                    String orderHeader = map.get("otype").toString();
                    //订单总状态
                    int status = Integer.parseInt(map.get("status").toString());
                    //优惠满减金额
                    BigDecimal reducePrice = new BigDecimal(map.get("reduce_price").toString());
                    //优惠卷金额
                    BigDecimal couponPrice = new BigDecimal(map.get("coupon_price").toString());
                    //下单日期
                    String orderDate = map.get("add_time").toString();
                    orderDate = DateUtil.dateFormate(orderDate, GloabConst.TimePattern.YMD2);
                    map.put("time", orderDate);
                    //店铺id集
                    String       mchListStr = map.get("mch_id").toString();
                    List<String> mchList    = Arrays.asList(StringUtils.trim(mchListStr, ",").split(","));
                    //订单价格
                    BigDecimal orderAmt = new BigDecimal(map.get("spz_price").toString());
                    //售后状态 1=售后未结束 2=全部在售后且未结束
                    int saleType = 0;

                    map.put("sale_type", 0);
                    //判断有无订单售后未结束
                    Map<String, Object> returnNotFinishedParamMap = new HashMap<>(16);
                    returnNotFinishedParamMap.put("store_id", vo.getStoreId());
                    returnNotFinishedParamMap.put("sNo", orderno);
                    //rType=2 订单是否全在售后且未结束
                    returnNotFinishedParamMap.put("rType", 2);
                    int count = returnOrderModelMapper.countRturnOrderNumDynamic(returnNotFinishedParamMap);
                    if (count > 0)
                    {
                        saleType = 1;
                    }
                    //判断订单是否全在售后且未结束  rType=1 未完成售后的状态
                    returnNotFinishedParamMap.put("rType", 1);
                    int count1 = returnOrderModelMapper.countRturnOrderNumDynamic(returnNotFinishedParamMap);
                    if (count1 == count)
                    {
                        saleType = 2;
                    }
                    map.put("sale_type", saleType);

                    //跨店订单订单标识  跨店订单 并且 未完成的
                    boolean orderStatus = mchList.size() <= 1 || status != 0;

                    //获取该订单的详情信息
                    Map<String, Object> orderDetailParmaMap = new HashMap<>(16);
                    orderDetailParmaMap.put("store_id", vo.getStoreId());
                    orderDetailParmaMap.put("orderno", orderno);
                    orderDetailParmaMap.put("mch_id", vo.getShopId());
                    List<Map<String, Object>> orderGoodsInfoList = orderDetailsModelMapper.getOrderDetailByGoodsInfo(orderDetailParmaMap);

                    //订单明细状态如果全部都是一个状态，则修改主表状态
                    Set<Integer> rStatusList = new TreeSet<>();
                    //商品总价
                    BigDecimal goodsAmt = BigDecimal.ZERO;
                    //运费总价
                    BigDecimal freightAmt = BigDecimal.ZERO;
                    for (Map<String, Object> orderGoods : orderGoodsInfoList)
                    {
                        //订单详情id
                        Integer orderDetailsId = MapUtils.getInteger(orderGoods, "id");
                        //是否已经退款成功
                        if (returnOrderModelMapper.orderReturnSuccessNum(vo.getStoreId(), orderno, orderDetailsId) > 0)
                        {
                            orderGoods.put("isReturn", true);
                        }
                        else
                        {
                            orderGoods.put("isReturn", false);
                        }
                        int rStatus = Integer.parseInt(orderGoods.get("r_status").toString());
                        //订单详情状态
                        rStatusList.add(rStatus);
                        //图片处理
                        String imgUrl = orderGoods.get("img") + "";
                        if (StringUtils.isEmpty(imgUrl))
                        {
                            imgUrl = orderGoods.get("imgurl") + "";
                        }
                        imgUrl = publiceService.getImgPath(imgUrl, vo.getStoreId());
                        orderGoods.put("imgurl", imgUrl);

                        //供应商名称
                        Integer       gysId         = MapUtils.getInteger(orderGoods, "gys");
                        SupplierModel supplierModel = new SupplierModel();
                        supplierModel.setId(gysId);
                        if (Objects.nonNull(gysId))
                        {
                            SupplierModel model = supplierModelMapper.selectOne(supplierModel);
                            orderGoods.put("supplierName", model.getSupplier_name());
                        }

                        //处理跨店订单 且未支付
                        if (!orderStatus)
                        {
                            BigDecimal currentGoodsAmt = new BigDecimal(0);
                            //当前商品价格
                            currentGoodsAmt = new BigDecimal(orderGoods.get("p_price").toString());
                            //当前商品数量
                            BigDecimal currentGoodsNum = new BigDecimal(orderGoods.get("num").toString());
                            //当前商品运费价格
                            BigDecimal currentFreightAmt = new BigDecimal(orderGoods.get("freight").toString());

                            goodsAmt = goodsAmt.add(currentGoodsAmt).multiply(currentGoodsNum);
                            freightAmt = freightAmt.add(currentFreightAmt);
                        }
                        else
                        {
                            Integer isDistribution = MapUtils.getInteger(orderMap, "is_distribution");
                            if (isDistribution == null)
                            {
                                isDistribution = 0;
                            }
                            orderGoods.put("otype", orderHeader);
                            //是否是分销订单
                            if (isDistribution == 1)
                            {
                                orderGoods.put("otype", DictionaryConst.OrdersType.ORDERS_HEADER_FX);
                            }
                        }
                    }

                    if (!orderStatus)
                    {
                        // 该店铺商品总价 除以 整个订单商品总价 乘以 优惠的满减金额
                        BigDecimal reduceSumPrice = goodsAmt.divide(orderAmt, 2, BigDecimal.ROUND_HALF_UP).multiply(reducePrice);
                        // 该店铺商品总价 除以 整个订单商品总价 乘以 优惠的优惠券金额
                        BigDecimal couponSumPrice = goodsAmt.divide(orderAmt, 2, BigDecimal.ROUND_HALF_UP).multiply(couponPrice);
                        map.put("reduce_price", reduceSumPrice);
                        map.put("coupon_price", couponSumPrice);

                        //计算会员特惠 折扣*(商品总价格-满减价格-优惠卷价格)+运费价格
                        BigDecimal gradeRate = new BigDecimal(publicMemberService.getMemberGradeRate(orderHeader, vo.getUserId(), vo.getStoreId()) + "");
                        //商品最后价格 (商品总价格-满减价格-优惠卷价格)
                        BigDecimal goodsPrice = new BigDecimal(goodsAmt.toString());
                        goodsPrice = goodsPrice.subtract(reduceSumPrice).subtract(couponPrice);
                        //会员特惠价格 折扣*(商品总价格-满减价格-优惠卷价格)+运费价格
                        gradeRate = gradeRate.multiply(goodsPrice).add(freightAmt);

                        map.put("z_price", gradeRate);
                    }
                    map.put("list", orderGoodsInfoList);
                    map.put("order_status", orderStatus);
                    //全部是一个状态,修改主表状态
                    if (rStatusList.size() == 1 && !rStatusList.isEmpty())
                    {
                        //订单关闭则不处理
                        int uStatus = rStatusList.iterator().next();
                        if (uStatus != DictionaryConst.OrdersStatus.ORDERS_R_STATUS_CLOSE)
                        {
                            //明细订单状态和总状态一样则不处理
                            if (uStatus != status)
                            {
                                OrderModel orderModel = new OrderModel();
                                orderModel.setId(oid);
                                orderModel.setStatus(uStatus);
                                count = orderModelMapper.updateByPrimaryKeySelective(orderModel);
                                if (count < 1)
                                {
                                    logger.info("订单状态修改失败 参数:" + JSON.toJSONString(orderModel));
                                }
                                if (uStatus == ORDERS_R_STATUS_COMPLETE)
                                {
                                    status = ORDERS_R_STATUS_COMPLETE;
                                }
                            }
                        }
                    }
                    map.put("status",status);
                    //是否有电子面单发货
                    if (expressDeliveryModelMapper.countBysNo(vo.getStoreId(), orderno) > 0)
                    {
                        map.put("logistics_type", true);
                    }
                    else
                    {
                        map.put("logistics_type", false);
                    }
                    //一对多 res节点
                    orderDetailList.add(map);
                }
            }
            else
            {
                //售后流程
                Map<String, Object> returnParmaMap = new HashMap<>(16);
                returnParmaMap.put("store_id", vo.getStoreId());
                returnParmaMap.put("mch_id", vo.getShopId());
                if (!StringUtils.isEmpty(vo.getKeyword()))
                {
                    returnParmaMap.put("likeOrderno", vo.getKeyword());
                }
                returnParmaMap.put("reTimeSort", DataUtils.Sort.DESC.toString());
                returnParmaMap.put("reTypeSort", DataUtils.Sort.ASC.toString());
                returnParmaMap.put("newReturn", "newReturn");
                //如果类型为直播
                if (StringUtils.isNotEmpty(vo.getOrderHeadrType()) && vo.getOrderHeadrType().equals(DictionaryConst.OrdersType.ORDERS_HEADER_ZB))
                {
                    returnParmaMap.put("orderType", DictionaryConst.OrdersType.ORDERS_HEADER_ZB);
                }
                else if (StringUtils.isNotEmpty(vo.getOrderHeadrType()) && vo.getOrderHeadrType().equals(DictionaryConst.OrdersType.ORDERS_HEADER_VI))
                {
                    returnParmaMap.put("orderType", DictionaryConst.OrdersType.ORDERS_HEADER_VI);
                }
                else if (StringUtils.isNotEmpty(vo.getOrderHeadrType()) && vo.getOrderHeadrType().equals(DictionaryConst.OrdersType.ORDERS_HEADER_FS))
                {
                    returnParmaMap.put("orderType", DictionaryConst.OrdersType.ORDERS_HEADER_FS);
                }
                else
                {
                    //55549 退货只显示实物和虚拟订单
                    returnParmaMap.put("orderType", DictionaryConst.OrdersType.ORDERS_HEADER_GM);
                }
                returnParmaMap.put("pageNo", vo.getPageNo());
                returnParmaMap.put("pageSize", vo.getPageSize());
                List<Map<String, Object>> returnOrderMapList = returnOrderModelMapper.getReturnOrderByGoodsInfo(returnParmaMap);

                for (Map<String, Object> map : returnOrderMapList)
                {
                    map.put("pid", MapUtils.getInteger(map, "goodsId"));
                    //订单号
                    String orderno = map.get("sNo").toString();
                    //退款类型
                    int reType = Integer.parseInt(map.get("re_type").toString());
                    //文字说明
                    String typeText;

                    //图片处理
                    String imgUrl = map.get("img") + "";
                    if (StringUtils.isEmpty(imgUrl))
                    {
                        imgUrl = map.get("imgurl") + "";
                    }
                    imgUrl = publiceService.getImgPath(imgUrl, vo.getStoreId());
                    map.put("imgurl", imgUrl);

                    switch (reType)
                    {
                        case ReturnOrderModel.RE_TYPE_RETURN_REFUND:
                            typeText = "退货退款";
                            break;
                        case ReturnOrderModel.RE_TYPE_REFUND:
                            typeText = "仅退款";
                            break;
                        default:
                            typeText = "换货";
                            break;
                    }
                    map.put("type", typeText);

                    Map<String, Object> orderInfo;
                    //获取订单主表信息
                    Map<String, Object> orderParmaMap = new HashMap<>(16);
                    orderParmaMap.put("store_id", vo.getStoreId());
                    orderParmaMap.put("orderno", orderno);
                    List<Map<String, Object>> orderMapList = orderModelMapper.getOrdersNumDynamic(orderParmaMap);
                    if (orderMapList != null && orderMapList.size() > 0)
                    {
                        orderInfo = orderMapList.get(0);
                        int isDistribution = Integer.parseInt(map.get("is_distribution").toString());
                        map.put("otype", orderInfo.get("otype"));
                        if (isDistribution == 1)
                        {
                            map.put("otype", DictionaryConst.OrdersType.ORDERS_HEADER_FX);
                        }
                        orderInfo.put("time", DateUtil.dateFormate(MapUtils.getString(map, "re_time"), GloabConst.TimePattern.YMD));
                        orderInfo.put("list", map);
                    }
                    else
                    {
                        logger.debug("【订单数据发生错误,订单号:{} 该订单不存在】", orderno);
                        continue;
                    }
                    //一对多 res节点
                    orderDetailList.add(orderInfo);
                }
            }
            //构造数据结构
            List<Map<String, Object>> data = new ArrayList<>();
            //暂存订单信息 时间分组
            Map<String, List<Map<String, Object>>> groupDateMap = new HashMap<>(16);
            for (Map<String, Object> map : orderDetailList)
            {
                String time = map.get("time").toString();
                //同一个时段合并
                List<Map<String, Object>> tempMap = new ArrayList<>();
                if (groupDateMap.containsKey(time))
                {
                    tempMap = groupDateMap.get(time);
                }
                tempMap.add(map);
                groupDateMap.put(time, tempMap);
            }
            for (String time : groupDateMap.keySet())
            {
                Map<String, Object> tempData = new HashMap<>(16);
                tempData.put("time", time);
                tempData.put("res", groupDateMap.get(time));
                data.add(tempData);
            }

            resultMap.put("list", DataUtils.mapSort(data, "time", DataUtils.Sort.DESC));
            resultMap.put("payment_num", paymentNum);
            resultMap.put("send_num", sendNum);
            resultMap.put("return_num", retyrnNum);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("订单列表（移动店铺端） 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "aMchOrderIndex");
        }
        return resultMap;
    }

    /**
     * 订单拆单（新）
     *
     * @param storeId -
     * @param sNo     -
     * @param tradeNo - 第三方支付订单号
     * @param userId  -
     * @param payType - 支付方式
     * @return
     * @throws LaiKeAPIException
     */
    @Override
    public Map<String, Object> orderPayment(int storeId, String sNo, String tradeNo, String userId, String payType,String oType) throws LaiKeAPIException
    {
        try
        {
            int row;
            //在支付后进行拆单：调用拆单接口，返回父订单与子订单列表
            Map<String, Object> paramMap = new LinkedHashMap<>(16);
            User                user     = new User();
            user.setUser_id(userId);
            user = userMapper.selectOne(user);
            paramMap.put("sNo", sNo);
            paramMap.put("storeId", storeId);
            paramMap.put("userId", userId);
            paramMap.put("user", user);
            Map<String, Object> paramApiMap = JSON.parseObject(JSON.toJSONString(paramMap));
            Map<String, Object> resultMap   = httpApiUtils.executeHttpApi("app.order.leave_Settlement", paramApiMap, MediaType.MULTIPART_FORM_DATA_VALUE);
            if (DataUtils.getBooleanVal(resultMap, "flag") != true)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_, "拆单错误", "splitOrder");
            }
            /*OrderModel orderModel = new OrderModel();
            orderModel.setStore_id(storeId);
            orderModel.setsNo(sNo);
            orderModel.setUser_id(userId);
            orderModel = orderModelMapper.selectOne(orderModel);*/
            OrderModel orderModel = JSON.parseObject(JSON.toJSONString(resultMap.get("order")), new TypeReference<OrderModel>()
            {
            });
            if (orderModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误", "pay");
            }
            resultMap.put("p_sNo", orderModel.getsNo());
            //resultMap中的order为父订单，list中为拆分后的多个子订单
            List<OrderModel> orderList = JSON.parseObject(JSON.toJSONString(resultMap.get("list")), new TypeReference<List<OrderModel>>()
            {
            });
            //List<OrderModel> orderList= (List<OrderModel>) resultMap.get("list");
            BigDecimal       zPrice         = orderModel.getZ_price();
            int              selfLifting    = orderModel.getSelf_lifting();
            List<OrderModel> orderModelList = new ArrayList<>();
            //订单是否多店铺已经拆单
            OrderModel order = new OrderModel();
            order.setStore_id(storeId);
            order.setUser_id(userId);
            order.setP_sNo(sNo);
            //orderModelList = orderModelMapper.select(order);
            //BeanUtils.copyProperties(orderList, orderModelList);
            //单店铺供应商商品和普通商品同时下单-父订单为普通商品订单
            /*if (orderModel.getStatus().equals(ORDERS_R_STATUS_UNPAID) && !orderModel.getOtype().equals(DictionaryConst.OrdersType.ORDERS_HEADER_VI)) {
                String mchId = StringUtils.trim(orderModel.getMch_id(), SplitUtils.DH);
                // 店铺id字符串
                List<String> shopIds = Splitter.on(SplitUtils.DH).splitToList(mchId);
                if (shopIds.size() == 1) {
                    orderModelList.add(orderModel);
                } else {
                    //不为单店铺?
                    logger.error("订单号:" + sNo);
                    logger.error("单店铺供应商商品和普通商品同时下单-父订单为普通商品订单");
                    logger.error("父订单订单状态为待付款订单");
                    throw new LaiKeAPIException(API_OPERATION_FAILED, "支付失败", "pay");
                }
            }*/
            /*if (StringUtils.isEmpty(orderModelList) || orderModelList.size() <= 0){
                orderModelList.add(orderModel);
            }*/
            if (orderList.isEmpty())
            {
                throw new LaiKeAPIException(API_OPERATION_FAILED, "支付失败", "pay");
            }
            Map<String, Object> conditionMap = Maps.newHashMap();
            for (OrderModel model : orderList)
            {
                sNo = model.getsNo();
                //支付完成后统一更新订单主表状态与支付信息
                conditionMap.put("payTime", new Date());
                conditionMap.put("storeId", model.getStore_id());
                conditionMap.put("sNo", sNo);
                conditionMap.put("pay", payType);
                if (!StringUtils.isEmpty(tradeNo))
                {
                    conditionMap.put("tradeNo", tradeNo);
                }
                if (model.getOtype().equals(DictionaryConst.OrdersType.ORDERS_HEADER_VI))
                {
                    selfLifting = model.getSelf_lifting();
                }

                // 默认待发货状态（普通配送）
                int status = DictionaryConst.OrdersStatus.ORDERS_R_STATUS_CONSIGNMENT;
                //用于更新orderDetail表
                Map<String, Object> param = new HashMap<>();
                if (selfLifting == 1)
                {
                    //自提订单：付款后直接进入待收货（等待核销/取货）
                    status = DictionaryConst.OrdersStatus.ORDERS_R_STATUS_DISPATCHED;
                }
                else if (selfLifting == 4)
                {
                    //无需核销的虚拟商品：付款后直接完成，并设置到货时间用于结算
                    status = DictionaryConst.OrdersStatus.ORDERS_R_STATUS_COMPLETE;
                    param.put("arrive_time", new Date());
                }
                else if (selfLifting == 3)
                {
                    //到店核销/预约类：付款后进入待核销
                    status = ORDERS_R_STATUS_TOBEVERIFIED;
                }

                conditionMap.put("status", status);
                row = orderModelMapper.wallectPayUpdateOrder(conditionMap);
                if (row < 1)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_ZFSB, "支付失败", "pay");
                }
                param.put("storeId", storeId);
                param.put("orderno", sNo);
                param.put("r_status", status);
                //同步更新订单明细状态，保持与主表一致
                row = orderDetailsModelMapper.updateByOrdernoDynamic(param);
                if (row < 1)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_ZFSB, "支付失败", "pay");
                }
            }
            //无需核销订单记录待提现金额
            if (selfLifting == 4)
            {
                String mchId = StringUtils.trim(orderModel.getMch_id(), SplitUtils.DH);
                // 店铺id字符串
                List<String> shopIds = Splitter.on(SplitUtils.DH).splitToList(mchId);
                if (shopIds.size() == 1)
                {
                    //确认收货到店铺账上的金额不能是订单金额  一个订单多个商品,一个退款(少退),其余的确认收货,少退的部分还在订单金额里面,但实际上这笔钱已经结算到店铺可提现金额里面了
                    publicMchService.clientConfirmReceipt(storeId, Integer.parseInt(shopIds.get(0)), sNo, orderModel.getZ_price(), BigDecimal.ZERO);
                }
            }
            return resultMap;
        }
        catch (Exception e)
        {
            logger.error("钱包支付 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QBZFSB, "钱包支付失败", "pay");
        }
    }

    @Override
    public BigDecimal orderPaymentForOther(int storeId, String sNo, String tradeNo, String userId, String payType) throws LaiKeAPIException
    {
        try
        {
            int        row;
            OrderModel orderModel = new OrderModel();
            orderModel.setStore_id(storeId);
            orderModel.setsNo(sNo);
            orderModel.setUser_id(userId);
            orderModel = orderModelMapper.selectOne(orderModel);
            if (orderModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误", "pay");
            }
            BigDecimal       zPrice      = orderModel.getZ_price();
            int              selfLifting = orderModel.getSelf_lifting();
            List<OrderModel> orderModelList;
            //订单是否多店铺已经拆单
            OrderModel order = new OrderModel();
            order.setStore_id(storeId);
            order.setUser_id(userId);
            order.setP_sNo(sNo);
            orderModelList = orderModelMapper.select(order);

            //单店铺供应商商品和普通商品同时下单-父订单为普通商品订单
            if (orderModel.getStatus().equals(ORDERS_R_STATUS_UNPAID) && !orderModel.getOtype().equals(DictionaryConst.OrdersType.ORDERS_HEADER_VI))
            {
                String mchId = StringUtils.trim(orderModel.getMch_id(), SplitUtils.DH);
                // 店铺id字符串
                List<String> shopIds = Splitter.on(SplitUtils.DH).splitToList(mchId);
                if (shopIds.size() == 1)
                {
                    orderModelList.add(orderModel);
                }
                else
                {
                    //不为单店铺?
                    logger.error("订单号:" + sNo);
                    logger.error("单店铺供应商商品和普通商品同时下单-父订单为普通商品订单");
                    logger.error("父订单订单状态为待付款订单");
                    throw new LaiKeAPIException(API_OPERATION_FAILED, "支付失败", "pay");
                }
            }
            if (StringUtils.isEmpty(orderModelList) || orderModelList.size() <= 0)
            {
                orderModelList.add(orderModel);
            }
            Map<String, Object> conditionMap = Maps.newHashMap();
            for (OrderModel model : orderModelList)
            {
                sNo = model.getsNo();
                //支付完成后统一更新订单主表状态与支付信息
                conditionMap.put("payTime", new Date());
                conditionMap.put("storeId", model.getStore_id());
                conditionMap.put("sNo", sNo);
                conditionMap.put("pay", payType);
                if (!StringUtils.isEmpty(tradeNo))
                {
                    conditionMap.put("tradeNo", tradeNo);
                }
                if (model.getOtype().equals(DictionaryConst.OrdersType.ORDERS_HEADER_VI))
                {
                    selfLifting = model.getSelf_lifting();
                }

                // 默认待发货状态（普通配送）
                int status = DictionaryConst.OrdersStatus.ORDERS_R_STATUS_CONSIGNMENT;
                //用于更新orderDetail表
                Map<String, Object> param = new HashMap<>();
                if (selfLifting == 1)
                {
                    //自提订单：付款后直接进入待收货（等待核销/取货）
                    status = DictionaryConst.OrdersStatus.ORDERS_R_STATUS_DISPATCHED;
                }
                else if (selfLifting == 4)
                {
                    //无需核销的虚拟商品：付款后直接完成，并设置到货时间用于结算
                    status = DictionaryConst.OrdersStatus.ORDERS_R_STATUS_COMPLETE;
                    param.put("arrive_time", new Date());
                }
                else if (selfLifting == 3)
                {
                    //到店核销/预约类：付款后进入待核销
                    status = ORDERS_R_STATUS_TOBEVERIFIED;
                }

                conditionMap.put("status", status);
                row = orderModelMapper.wallectPayUpdateOrder(conditionMap);
                if (row < 1)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_ZFSB, "支付失败", "pay");
                }
                param.put("storeId", storeId);
                param.put("orderno", sNo);
                param.put("r_status", status);
                //同步更新订单明细状态，保持与主表一致
                row = orderDetailsModelMapper.updateByOrdernoDynamic(param);
                if (row < 1)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_ZFSB, "支付失败", "pay");
                }
            }
            //无需核销订单记录待提现金额
            if (selfLifting == 4)
            {
                String mchId = StringUtils.trim(orderModel.getMch_id(), SplitUtils.DH);
                // 店铺id字符串
                List<String> shopIds = Splitter.on(SplitUtils.DH).splitToList(mchId);
                if (shopIds.size() == 1)
                {
                    //确认收货到店铺账上的金额不能是订单金额  一个订单多个商品,一个退款(少退),其余的确认收货,少退的部分还在订单金额里面,但实际上这笔钱已经结算到店铺可提现金额里面了
                    publicMchService.clientConfirmReceipt(storeId, Integer.parseInt(shopIds.get(0)), sNo, orderModel.getZ_price(), BigDecimal.ZERO);
                }
            }
            return zPrice;
        }
        catch (Exception e)
        {
            logger.error("钱包支付 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QBZFSB, "钱包支付失败", "pay");
        }
    }

    @Override
    @Transactional
    public void reBackOrder(PaymentVo vo) throws LaiKeAPIException
    {
        //删除拆单多出来的主单信息
        String     orderList  = vo.getOrder_list();
        JSONObject jsonObject = JSONObject.parseObject(orderList);
        // 订单sNo
        String sNo;
        if (!org.springframework.util.StringUtils.isEmpty(vo.getsNo()))
        {
            sNo = vo.getsNo();
        }
        else
        {
            sNo = jsonObject.getString("sNo");
        }

        User user;
        if (GloabConst.StoreType.STORE_TYPE_PC_MALL == vo.getStoreType())
        {
            logger.debug("pc商城支付失败将订单信息回退");
            user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_PC_SHOP_TOKEN, true);
        }
        else
        {
            user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
        }

        //竞拍保证金下单回滚,只有钱包支付才走http内部调用方式
        if (StringUtils.isNotEmpty(vo.getParameter()))
        {

            Map<String, Object> paramMap = JSON.parseObject(vo.getParameter(), new TypeReference<Map<String, Object>>()
            {
            });
            String apiUrl = MapUtils.getString(paramMap, "laikeApi");

            if (apiUrl.equals("plugin.auction.payPromise") && vo.getPayType().equals("wallet_pay"))
            {
                String specialId = jsonObject.getString("specialId");
                AuctionPromiseModel auctionPromiseOld = new AuctionPromiseModel();
                auctionPromiseOld.setStore_id(vo.getStoreId());
                auctionPromiseOld.setUser_id(user.getUser_id());
                auctionPromiseOld.setSpecial_id(specialId);
                auctionPromiseOld.setTrade_no(vo.getsNo());
                auctionPromiseOld = auctionPromiseModelMapper.selectOne(auctionPromiseOld);
                if (Objects.nonNull(auctionPromiseOld))
                {
                    auctionPromiseModelMapper.deleteByPrimaryKey(auctionPromiseOld.getId());
                }
            }
        }



        if (StringUtils.isNotEmpty(sNo) && (sNo.contains(DictionaryConst.OrdersType.ORDERS_HEADER_VI) || sNo.contains(DictionaryConst.OrdersType.ORDERS_HEADER_GM)))
        {
            //回退主订单信息
            Map conditionMap = Maps.newHashMap();
            conditionMap.put("status", ORDERS_R_STATUS_UNPAID);
            conditionMap.put("recycle", DictionaryConst.ProductRecycle.NOT_STATUS);
            conditionMap.put("orderno", sNo);
            conditionMap.put("user_id", user.getUser_id());
            int row = orderModelMapper.updateByOrdernoDynamic(conditionMap);
            if (row < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CZSB, "操作失败", "splitOrder");
            }
            OrderModel newOrderModel = new OrderModel();
            newOrderModel.setP_sNo(sNo);
            newOrderModel.setUser_id(user.getUser_id());
            List<OrderModel> newOrderModelList = orderModelMapper.select(newOrderModel);
            if (newOrderModelList.size() > 1)
            {
                List<String> sNos = newOrderModelList.stream().map(x -> x.getsNo()).collect(Collectors.toList());
                //将详单的信息改回来
                orderDetailsModelMapper.reBack(sNo, sNos);
                //删除多出俩的主表信息
                conditionMap.clear();
                conditionMap.put("status", DictionaryConst.OrdersStatus.ORDERS_R_STATUS_CLOSE);
                conditionMap.put("recycle", DictionaryConst.ProductRecycle.RECOVERY);
                conditionMap.put("ordernoList", sNos);
                conditionMap.put("user_id", user.getUser_id());
                int i = orderModelMapper.updateByOrdernoDynamic(conditionMap);
                if (i < 1)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CZSB, "操作失败", "splitOrder");
                }
                couponOrderModalMapper.reBackCouponOrder(newOrderModelList.get(0).getStore_id(), sNo, user.getUser_id());
            }
        }

    }

    @Override
    public void frontDelivery(FrontDeliveryVo vo) throws LaiKeAPIException
    {
        try
        {
            int storeId = vo.getStoreId();
            int count = 0;
            if (StringUtils.isEmpty(vo.getsNo()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DDHWK, "订单号为空");
            }
            //获取订单信息
            OrderModel orderModel = new OrderModel();
            orderModel.setsNo(vo.getsNo());
            orderModel = orderModelMapper.selectOne(orderModel);
            //快递公司id
            if (orderModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DDBCZ, "订单不存在", "frontDelivery");
            }
            if (orderModel.getSelf_lifting() == 2)
            {
                //商家自配
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERRCODE_QTXPSXX, "请填写配送信息", "frontDelivery");
            }
            if (vo.getExpressId() == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QXZKDGS, "请选择快递公司", "frontDelivery");
            }

            String cpc = orderModel.getCpc();
            boolean isHome = Objects.equals(cpc,"86") || Objects.equals(cpc,"852");

            if (isHome)
            {
                ExpressModel expressModel = new ExpressModel();
                expressModel.setId(vo.getExpressId());
                count = expressModelMapper.selectCount(expressModel);
                if (count < 1)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLGSBZQ, "物流公司id不正确", "frontDelivery");
                }
            }

            //快递单号
            if (!StringUtils.isEmpty(vo.getCourierNum()))
            {
                if (vo.getCourierNum().length() >= 10 && vo.getCourierNum().length() <= 20)
                {
                    int exnum = orderDetailsModelMapper.getDeliverNumByExIdAndExNo(vo.getExpressId().toString(), vo.getCourierNum());
                    if (exnum > 0)
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_FHSKDDHYCZ, "发货时,快递单号已存在", "frontDelivery");
                    }
                }
                else
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_FHSKDDHSRCW, "发货时,快递单号输入错误", "frontDelivery");
                }
            }
            else
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_FHSWTXKDDH, "发货时,未填写快递单号", "frontDelivery");
            }
            String exId = vo.getExpressId().toString();
            String exNo = vo.getCourierNum();
            if (StringUtils.isEmpty(vo.getOrderDetailsId()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误");
            }
            List<Map<String, Object>> orderListIds = JSON.parseObject(vo.getOrderDetailsId(), new TypeReference<List<Map<String, Object>>>()
            {
            });
            OrderDetailsModel orderDetailsOld;
            //统计当前订单下有多少件商品
            int orderGoodsNum = orderModel.getNum();
            //统计当前已发货数量
            int deliverNum = orderDetailsModelMapper.getDeliverNumBySNo(storeId, vo.getsNo());
            //统计已经售后完成的数量
            int returnNum = orderDetailsModelMapper.getOrderDetailsNum(String.valueOf(ORDERS_R_STATUS_CLOSE), vo.getsNo());
            //如果为限时折扣订单需要已发货数量加上已经售后的订单数量
            if (DictionaryConst.OrdersType.ORDERS_HEADER_FS.equals(orderModel.getOtype()))
            {
                deliverNum = deliverNum + returnNum;
            }
            //统计这次发货数量
            int nowDeliverNum = 0;
            //发货记录
            ExpressDeliveryModel expressDeliveryModel;
            for (Map<String, Object> orderListId : orderListIds)
            {
                int detailId = MapUtils.getIntValue(orderListId, "detailId");
                int num      = MapUtils.getIntValue(orderListId, "num");
                if (num == 0)
                {
                    logger.debug("发货失败,发货商品数量错误 详情id{}", detailId);
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QSRSPSL, "请输入商品数量");
                }
                //售后中的订单不能发货
                if (returnOrderModelMapper.orderDetailReturnIsNotEnd(storeId, orderModel.getsNo(), detailId) > 0)
                {
                    logger.debug("发货失败,订单正在售后中 详情id{}", detailId);
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_LBYSPZSHZ, "发货失败,订单正在售后中");
                }
                //订单非待发货状态
                if (orderDetailsModelMapper.getDetailsNumById(storeId, detailId) > 0)
                {
                    logger.debug("发货失败,订单状态非待发货 详情id{}", detailId);
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YJFH, "该订单已发货");
                }
                orderDetailsOld = orderDetailsModelMapper.selectByPrimaryKey(detailId);
                String oExId = orderDetailsOld.getExpress_id();
                if (StringUtils.isNotEmpty(oExId))
                {
                    oExId = oExId + "," + exId;
                }
                else
                {
                    oExId = exId;
                }
                String oExNo = orderDetailsOld.getCourier_num();
                if (StringUtils.isNotEmpty(oExNo))
                {
                    oExNo = oExNo + "," + exNo;
                }
                else
                {
                    oExNo = exNo;
                }
                int DeliverNum = 0;
                if (orderDetailsOld.getDeliverNum() != null)
                {
                    DeliverNum = orderDetailsOld.getDeliverNum();
                }
                if (DeliverNum + num > orderDetailsOld.getNum())
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SJCW, "数据错误");
                }
                nowDeliverNum += num;
                //修改订单明细 修改明细订单状态为待收货
                OrderDetailsModel orderDetailsModel = new OrderDetailsModel();
                orderDetailsModel.setId(detailId);
                if (DeliverNum + num == orderDetailsOld.getNum())
                {
                    orderDetailsModel.setR_status(ORDERS_R_STATUS_DISPATCHED);
                }
                if (!isHome)
                {
                    this.trackDelivery(vo.getStoreId(),vo.getCourierNum());
                }

                orderDetailsModel.setExpress_id(oExId);
                orderDetailsModel.setCourier_num(oExNo);
                orderDetailsModel.setDeliverNum(DeliverNum + num);
                orderDetailsModel.setDeliver_time(new Date());
                int row = orderDetailsModelMapper.updateByPrimaryKeySelective(orderDetailsModel);
                if (row < 1)
                {
                    logger.debug("订单{} 发货失败,明细id{}", vo.getsNo(), detailId);
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_FHSB, "发货失败");
                }
                expressDeliveryModel = new ExpressDeliveryModel();
                expressDeliveryModel.setStore_id(storeId);
                expressDeliveryModel.setsNo(orderDetailsModel.getR_sNo());
                expressDeliveryModel.setExpressId(Integer.valueOf(exId));
                expressDeliveryModel.setCourierNum(exNo);
                expressDeliveryModel.setOrderDetailsId(orderDetailsModel.getId());
                expressDeliveryModel.setNum(num);
                expressDeliveryModel.setDeliverTime(new Date());
                row = expressDeliveryModelMapper.insertSelective(expressDeliveryModel);
                if (row < 1)
                {
                    logger.debug("订单{} 发货失败,明细id{}", vo.getsNo(), detailId);
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_FHSB, "发货失败");
                }
            }
            //是否都已经完成了发货,完成了发货则修改订单全部状态为待收货
            if (orderGoodsNum < deliverNum + nowDeliverNum)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SJCW, "数据错误");
            }
            //是否已经全部发货
            //修改订单主表状态 只有明细全部已发货才修改
            if (orderGoodsNum == deliverNum + nowDeliverNum)
            {
                OrderModel updateOrder = new OrderModel();
                updateOrder.setId(orderModel.getId());
                updateOrder.setStatus(DictionaryConst.OrdersStatus.ORDERS_R_STATUS_DISPATCHED);
                count = orderModelMapper.updateByPrimaryKeySelective(updateOrder);
                if (count < 1)
                {
                    logger.info("订单发货状态修改失败 参数:" + JSON.toJSONString(updateOrder));
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "frontDelivery");
                }
                //订单来源小程序并且使用小程序支付的话 全部发货完调用微信发货同步信息
                if (orderModel.getSource().toString().equals(DictionaryConst.StoreSource.LKT_LY_001)
                        && orderModel.getPay().equals(DictionaryConst.OrderPayType.ORDERPAYTYPE_MINI_WECHAT))
                {
                    publicExpressService.setWxAppUploadShippingInfo(vo, orderModel.getsNo());
                }
            }
            //站内推送发货信息
            publicAdminService.systemMessageSend(vo, SystemMessageModel.ReadType.UNREAD, "系统消息", "您购买的商品已经在赶去见您的路上啦!", orderModel.getUser_id());

            //TODO 【微信推送】暂时不做
            //$pusher->pushMessage($user_id, $db, $msg_title, $msg_content, $store_id, $user_id);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("发货 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "frontDelivery");
        }
    }

    @Override
    public Map<String, Object> okOrder(int storeId, String accessId, String orderno, Integer rType)
    {
        Map<String, Object> ret = Maps.newHashMap();
        try
        {
            MainVo vo = new MainVo();
            vo.setStoreId(storeId);
            vo.setAccessId(accessId);
            ret = this.okOrder(vo, GloabConst.RedisHeaderKey.LOGIN_ACCESS_TOKEN, orderno, rType);
        }
        catch (LaiKeAPIException l)
        {
            logger.error("确认收货 失败", l);
            throw l;
        }
        catch (Exception e)
        {
            logger.error("确认收货 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YWYC, "业务异常", "okOrder");
        }

        return ret;
    }

    @Override
    public Map<String, Object> okOrder(MainVo vo, String tokenKey, String orderNo, Integer rType) throws LaiKeAPIException
    {
        Map<String, Object> ret = new HashMap<>(16);
        try
        {
            // 根据授权id,查询用户id
            if (StringUtils.isEmpty(vo.getAccessId()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误");
            }
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, tokenKey, true);
            // 获取信息
            OrderModel orderModel = new OrderModel();
            orderModel.setsNo(orderNo);
            orderModel = orderModelMapper.selectOne(orderModel);
            if (orderModel == null)
            {
                logger.error("订单不存在！");
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QRSHSB, "确认收货失败", "okOrder");
            }
            //自提订单确认售后不能走这里
            if (orderModel.getSelf_lifting().equals(1))
            {
                logger.error("自提订单不能点击确认收货,只能走校验码！");
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QRSHSB, "确认收货失败", "okOrder");
            }

            // 售后商品回寄收货
            if (rType != null && rType.equals(DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_AGREE_REBACK_GOODS))
            {
                //查询订单信息
                ret.put("code", 200);
                int row = orderDetailsModelMapper.updateOkOrderDetails(vo.getStoreId(), orderModel.getId());
                if (row < 1)
                {
                    logger.error("确认收货失败！");
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QRSHSB, "确认收货失败", "okOrder");
                }
                else
                {
                    return ret;
                }
            }
            String userId = user.getUser_id();
            String sNo    = orderModel.getsNo();
            //积分
            Integer allow = orderModel.getAllow();
            allow = allow == null ? 0 : allow;
            //订单金额
            BigDecimal zPrice = orderModel.getZ_price();
            //最终可到店铺账上的金额
            BigDecimal        realMoney    = BigDecimal.ZERO;
            OrderDetailsModel orderDetails = new OrderDetailsModel();
            orderDetails.setStore_id(vo.getStoreId());
            orderDetails.setUser_id(userId);
            orderDetails.setR_sNo(orderModel.getsNo());
            List<OrderDetailsModel> orderDetailsModels = orderDetailsModelMapper.select(orderDetails);
            if (CollectionUtils.isEmpty(orderDetailsModels))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误", "okOrder");
            }

            Map<String, Object> param = new HashMap<>(16);
            for (OrderDetailsModel orderDetailsModel : orderDetailsModels)
            {
                int     orderDetailsId = orderDetailsModel.getId();
                Integer rStatus        = orderDetailsModel.getR_status();
                // 待收货
                if (rStatus == ORDER_UNRECEIVE)
                {
                    //计算实际可到账的金额 如果是积分,优惠金额则是积分
                    BigDecimal afterDiscount = orderDetailsModel.getAfter_discount();
                    if (orderModel.getOtype().equals(DictionaryConst.OrdersType.ORDERS_HEADER_IN))
                    {
                        //使用积分商品价格
                        afterDiscount = orderDetailsModel.getP_price();
                    }
                    realMoney = realMoney.add(afterDiscount).add(orderDetailsModel.getFreight());
                    param.put("r_status", ORDER_FINISH);
                    param.put("arrive_time", new Date());
                    param.put("storeId", vo.getStoreId());
                    param.put("detailId", orderDetailsId);
                    param.put("orderno", sNo);
                    int row = orderDetailsModelMapper.updateByOrdernoDynamic(param);
                    if (row < 0)
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QRSHSB, "确认收货失败", "okOrder");
                    }
                }
            }
            //确认收货 普通商品赠送积分
            if (DictionaryConst.OrdersType.ORDERS_HEADER_GM.equals(orderModel.getOtype()))
            {
                //如果订单之前已经获得了一笔积分则不再获取
                SignRecordModel signRecordCount = new SignRecordModel();
                signRecordCount.setStore_id(vo.getStoreId());
                signRecordCount.setUser_id(userId);
                signRecordCount.setType(DictionaryConst.IntegralType.SHOP_INTEGRAL);
                signRecordCount.setsNo(sNo);
                if (signRecordModelMapper.selectCount(signRecordCount) == 0)
                {
                    publicMemberService.memberSettlement(vo.getStoreId(), userId, sNo, zPrice, IntegralConfigModel.GiveStatus.RECEIVING);
                    //刷新缓存
                    RedisDataTool.refreshRedisUserCache(vo.getAccessId(), user, tokenKey, redisUtil);
                }
                else
                {
                    logger.debug("订单{}已经获得过一笔【会员购物积分】积分,此次不获嘚积分.", sNo);
                }
            }

            //订单完成
            int row = orderModelMapper.updateOrderStatus(vo.getStoreId(), sNo, userId, ORDER_FINISH);
            if (row < 0)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QRSHSB, "确认收货失败", "okOrder");
            }
            //分销结算
            if (DictionaryConst.OrdersType.ORDERS_HEADER_FX.equalsIgnoreCase(orderModel.getOtype()))
            {
                if (publiceService.frontPlugin(vo.getStoreId(), DictionaryConst.Plugin.DISTRIBUTION, null))
                {
                    //获取分销配置信息
                    DistributionConfigModel distributionConfigModel = new DistributionConfigModel();
                    distributionConfigModel.setStore_id(vo.getStoreId());
                    distributionConfigModel = distributionConfigModelMapper.selectOne(distributionConfigModel);
                    if (distributionConfigModel != null)
                    {
                        Map<String, Object> cpayMap = SerializePhpUtils.getDistributionConfigBySets(distributionConfigModel.getSets());
                        int                 type    = MapUtils.getIntValue(cpayMap, DistributionConfigModel.SetsKey.C_PAY);
                        if (type == DistributionConfigModel.SettementType.SETTEMENTTYPE_RECEIVING)
                        {
                            publiceDistributionService.commSettlement(vo.getStoreId(), userId, orderNo);
                        }
                    }
                    else
                    {
                        logger.debug("{}商城分销设置未配置", vo.getStoreId());
                    }
                }
            }

            // 已收货
            int mchId = Integer.parseInt(StringUtils.trim(orderModel.getMch_id(), SplitUtils.DH));

            //通知后台消息
            MessageLoggingModal messageLoggingSave = new MessageLoggingModal();
            messageLoggingSave.setStore_id(vo.getStoreId());
            messageLoggingSave.setMch_id(mchId);
            messageLoggingSave.setType(MessageLoggingModal.Type.TYPE_ORDER_OK_GOODS);
            messageLoggingSave.setTo_url(com.laiketui.common.utils.tool.data.OrderDataUtils.getOrderRoute(orderModel.getOtype(), 0));
            messageLoggingSave.setParameter(orderModel.getId() + "");
            messageLoggingSave.setContent(String.format("订单%s，用户已确定收货！", orderModel.getsNo()));
            messageLoggingSave.setAdd_date(new Date());
            messageLoggingModalMapper.insertSelective(messageLoggingSave);

            //确认收货到店铺账上的金额不能是订单金额  一个订单多个商品,一个退款(少退),其余的确认收货,少退的部分还在订单金额里面,但实际上这笔钱已经结算到店铺可提现金额里面了
            publicMchService.clientConfirmReceipt(vo.getStoreId(), mchId, sNo, realMoney, new BigDecimal(allow));

            //小程序端确认收货自动更改微信订单状态
            if (StringUtils.isNotEmpty(orderModel.getPay()) && StringUtils.isNotEmpty(orderModel.getWx_order_status()))
            {
                if (orderModel.getPay().equals(DictionaryConst.OrderPayType.ORDERPAYTYPE_MINI_WECHAT) && orderModel.getWx_order_status() == 2)
                {
                    //小程序支付，前端调用小程序收货组件，自动变更微信订单状态
                    orderModelMapper.UpdateWxOrderStatusByRealSno(vo.getStoreId(), orderModel.getReal_sno(), 3);
                }
            }
            ret.put("code", 200);
        }
        catch (LaiKeAPIException l)
        {
            logger.error("确认收货 失败", l);
            throw l;
        }
        catch (Exception e)
        {
            logger.error("确认收货 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YWYC, "业务异常", "okOrder");
        }
        return ret;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void pluginsAdminDelivery(MainVo vo, Integer exId, String exNo, String orderDetailIds) throws LaiKeAPIException
    {
        try
        {
            int row;
            //订单明细集
            List<String> orderListIds = Arrays.asList(orderDetailIds.split(","));
            //获取订单信息
            int storeId = vo.getStoreId();
            int len     = orderListIds.size();
            //是全部已经发货,如果详单都被发货,则同意修改订单状态为待收货
            boolean batchSend = false;
            //获取当前订单号
            String            orderNo;
            OrderDetailsModel orderDetailsOld = new OrderDetailsModel();
            orderDetailsOld.setStore_id(vo.getStoreId());
            orderDetailsOld.setId(Integer.parseInt(orderListIds.get(0)));
            orderDetailsOld = orderDetailsModelMapper.selectOne(orderDetailsOld);
            if (orderDetailsOld == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_XDBCZ, "详单不存在");
            }
            orderNo = orderDetailsOld.getR_sNo();
            //查询订单信息
            OrderModel orderModel = new OrderModel();
            orderModel.setStore_id(vo.getStoreId());
            orderModel.setsNo(orderNo);
            orderModel = orderModelMapper.selectOne(orderModel);
            OrderDetailsModel orderDetailsCount = new OrderDetailsModel();
            orderDetailsCount.setStore_id(vo.getStoreId());
            orderDetailsCount.setR_sNo(orderNo);
            //统计当前订单下有多少商品
            int orderGoodsNum = orderDetailsModelMapper.selectCount(orderDetailsCount);
            //统计当前发货次数
            orderDetailsCount.setR_status(DictionaryConst.OrdersStatus.ORDERS_R_STATUS_DISPATCHED);
            int curSendPos = orderDetailsModelMapper.selectCount(orderDetailsCount);
            //是否已经全部发货
            if (orderGoodsNum == len + curSendPos)
            {
                //批量发货
                batchSend = true;
            }
            ExpressDeliveryModel expressDeliveryModel;
            //订单明细发货
            for (String id : orderListIds)
            {
                //修改订单明细 修改明细订单状态为待收货
                OrderDetailsModel orderDetailsModel = new OrderDetailsModel();
                orderDetailsModel.setId(Integer.parseInt(id));
                orderDetailsModel.setR_status(DictionaryConst.OrdersStatus.ORDERS_R_STATUS_DISPATCHED);
                orderDetailsModel.setExpress_id(exId + "");
                orderDetailsModel.setCourier_num(exNo);
                orderDetailsModel.setDeliver_time(new Date());
                row = orderDetailsModelMapper.updateByPrimaryKeySelective(orderDetailsModel);
                if (row < 1)
                {
                    logger.debug("订单{} 发货失败,明细id{}", orderNo, id);
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_FHSB, "发货失败");
                }
                orderDetailsModel = orderDetailsModelMapper.selectByPrimaryKey(orderDetailsModel.getId());
                expressDeliveryModel = new ExpressDeliveryModel();
                expressDeliveryModel.setStore_id(storeId);
                expressDeliveryModel.setsNo(orderDetailsModel.getR_sNo());
                expressDeliveryModel.setExpressId(exId);
                expressDeliveryModel.setCourierNum(exNo);
                expressDeliveryModel.setOrderDetailsId(orderDetailsModel.getId());
                expressDeliveryModel.setNum(orderDetailsModel.getNum());
                expressDeliveryModel.setDeliverTime(new Date());
                row = expressDeliveryModelMapper.insertSelective(expressDeliveryModel);

                if (vo.getStoreType() == 8 || vo.getStoreType() == 7)
                {
                    publiceService.addAdminRecord(vo.getStoreId(), "将订单ID：" + orderDetailsModel.getR_sNo() + " 进行了发货", AdminRecordModel.Type.UPDATE, vo.getAccessId());
                }
            }
            //是否都已经完成了发货,完成了发货则修改订单全部状态为待收货
            if (batchSend)
            {
                row = orderModelMapper.updateOrderStatusByOrderNo(vo.getStoreId(), DictionaryConst.OrdersStatus.ORDERS_R_STATUS_DISPATCHED, orderNo);
                if (row < 1)
                {
                    logger.debug("订单{} 发货失败,状态修改失败", orderNo);
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_FHSB, "发货失败");
                }
                //订单来源小程序并且使用小程序支付的话 全部发货完调用微信发货同步信息
                if (orderModel.getSource().toString().equals(DictionaryConst.StoreSource.LKT_LY_001)
                        && orderModel.getPay().equals(DictionaryConst.OrderPayType.ORDERPAYTYPE_MINI_WECHAT))
                {
                    publicExpressService.setWxAppUploadShippingInfo(vo, orderNo);
                }
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("发货异常： ", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "adminDelivery");
        }
    }

    @Override
    public void adminDelivery(MainVo vo, Integer exId, String exNo, String orderDetailIds) throws LaiKeAPIException
    {
        try
        {
            AdminModel adminModel = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            int        row;
            //订单明细集
            List<Map<String, Object>> orderListIds = JSON.parseObject(orderDetailIds, new TypeReference<List<Map<String, Object>>>()
            {
            });
            //获取订单信息
            int storeId = vo.getStoreId();
            int len     = orderListIds.size();
            //是全部已经发货,如果详单都被发货,则同意修改订单状态为待收货
            boolean batchSend = false;
            //获取当前订单号
            String            orderNo;
            OrderDetailsModel orderDetailsOld = new OrderDetailsModel();
            orderDetailsOld.setStore_id(vo.getStoreId());
            orderDetailsOld.setId(MapUtils.getInteger(orderListIds.get(0), "detailId"));
            orderDetailsOld = orderDetailsModelMapper.selectOne(orderDetailsOld);
            if (orderDetailsOld == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_XDBCZ, "详单不存在");
            }
            orderNo = orderDetailsOld.getR_sNo();
            //查询订单信息
            OrderModel orderModel = new OrderModel();
            orderModel.setStore_id(vo.getStoreId());
            orderModel.setsNo(orderNo);
            orderModel = orderModelMapper.selectOne(orderModel);

            //统计当前订单下有多少件商品
            int orderGoodsNum = orderModel.getNum();
            //统计当前已发货数量
            int deliverNum = orderDetailsModelMapper.getDeliverNumBySNo(vo.getStoreId(), orderNo);
            //统计这次发货数量
            int nowDeliverNum = 0;
            //发货记录
            ExpressDeliveryModel expressDeliveryModel;

            //判断是否是国内发货
            boolean isHome = Objects.equals(orderModel.getCpc(),"86") || Objects.equals(orderModel.getCpc(),"852");

            //订单明细发货
            for (Map<String, Object> orderListId : orderListIds)
            {
                int id  = MapUtils.getIntValue(orderListId, "detailId");
                int num = MapUtils.getIntValue(orderListId, "num");
                if (num == 0)
                {
                    logger.debug("发货失败,发货商品数量错误 orderId{}", id);
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QSRSPSL, "请输入商品数量");
                }
                //是否存在售后中商品
                if (returnOrderModelMapper.orderDetailReturnIsNotEnd(vo.getStoreId(), orderNo, id) > 0)
                {
                    logger.error("后台发货 orderId:{},发货失败:商品正在售后中或已关闭!", id);
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SPZTFSBH_QSXLB, "商品状态发生变化,请刷新列表!");
                }
                orderDetailsOld = orderDetailsModelMapper.selectByPrimaryKey(id);
                String oExId = orderDetailsOld.getExpress_id();
                if (StringUtils.isNotEmpty(oExId))
                {
                    oExId = oExId + "," + exId;
                }
                else
                {
                    oExId = exId.toString();
                }
                String oExNo = orderDetailsOld.getCourier_num();
                if (StringUtils.isNotEmpty(oExNo))
                {
                    oExNo = oExNo + "," + exNo;
                }
                else
                {
                    oExNo = exNo;
                }
                int DeliverNum = 0;
                if (orderDetailsOld.getDeliverNum() != null)
                {
                    DeliverNum = orderDetailsOld.getDeliverNum();
                }
                if (DeliverNum + num > orderDetailsOld.getNum())
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SJCW, "数据错误");
                }
                nowDeliverNum += num;
                //修改订单明细 修改明细订单状态为待收货
                OrderDetailsModel orderDetailsModel = new OrderDetailsModel();
                orderDetailsModel.setId(id);
                if (DeliverNum + num == orderDetailsOld.getNum())
                {
                    orderDetailsModel.setR_status(ORDERS_R_STATUS_DISPATCHED);
                }

                if (!isHome)
                {
                    this.trackDelivery(vo.getStoreId(),exNo);
                }

                orderDetailsModel.setExpress_id(oExId);
                orderDetailsModel.setCourier_num(oExNo);
                orderDetailsModel.setDeliverNum(DeliverNum + num);
                orderDetailsModel.setDeliver_time(new Date());
                row = orderDetailsModelMapper.updateByPrimaryKeySelective(orderDetailsModel);
                if (row < 1)
                {
                    logger.debug("订单{} 发货失败,明细id{}", orderNo, id);
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_FHSB, "发货失败");
                }
                expressDeliveryModel = new ExpressDeliveryModel();
                expressDeliveryModel.setStore_id(storeId);
                expressDeliveryModel.setsNo(orderNo);
                expressDeliveryModel.setExpressId(exId);
                expressDeliveryModel.setCourierNum(exNo);
                expressDeliveryModel.setOrderDetailsId(orderDetailsModel.getId());
                expressDeliveryModel.setNum(num);
                expressDeliveryModel.setDeliverTime(new Date());
                row = expressDeliveryModelMapper.insertSelective(expressDeliveryModel);
                if (row < 1)
                {
                    logger.debug("订单{} 发货失败,明细id{}", orderNo, id);
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_FHSB, "发货失败");
                }
            }
            //是否都已经完成了发货,完成了发货则修改订单全部状态为待收货
            if (orderGoodsNum < deliverNum + nowDeliverNum)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SJCW, "数据错误");
            }
            //是否已经全部发货
            if (orderGoodsNum == deliverNum + nowDeliverNum)
            {
                //批量发货
                batchSend = true;
            }
            if (batchSend)
            {
                row = orderModelMapper.updateOrderStatusByOrderNo(vo.getStoreId(), ORDERS_R_STATUS_DISPATCHED, orderNo);
                if (row < 1)
                {
                    logger.debug("订单{} 发货失败,状态修改失败", orderNo);
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_FHSB, "发货失败");
                }
                //订单来源小程序并且使用小程序支付的话 全部发货完调用微信发货同步信息
                if (orderModel.getSource().toString().equals(DictionaryConst.StoreSource.LKT_LY_001)
                        && orderModel.getPay().equals(DictionaryConst.OrderPayType.ORDERPAYTYPE_MINI_WECHAT))
                {
                    publicExpressService.setWxAppUploadShippingInfo(vo, orderNo);
                }
            }

            AdminRecordModel adminRecordModel = new AdminRecordModel();
            adminRecordModel.setStore_id(storeId);
            adminRecordModel.setAdmin_name(adminModel.getName());
            adminRecordModel.setEvent("操作订单号为 " + orderNo + " 的订单发货 ");
            publiceService.addAdminRecord(storeId, "将订单ID：" + orderNo + " 进行了发货", AdminRecordModel.Type.UPDATE, vo.getAccessId());
            //站内推送发货信息
            publicAdminService.systemMessageSend(vo, SystemMessageModel.ReadType.UNREAD, "系统消息", "您购买的商品已经在赶去见您的路上啦!", orderDetailsOld.getUser_id());
            //发送模板消息
            NoticeModel noticeModel = new NoticeModel();
            noticeModel.setStore_id(storeId);
            noticeModel = noticeModelMapper.selectOne(noticeModel);
            if (Objects.isNull(noticeModel))
            {
                logger.debug("该商城id{}暂无微信推送模板", storeId);
            }
            //当前用户信息
            User userEntity = new User();
            userEntity.setUser_id(orderDetailsOld.getUser_id());
            User user = userBaseMapper.selectOne(userEntity);
            logger.error("userId: {}", user.getUser_id());
            if (StringUtils.isNotEmpty(user.getWx_id()))
            {
                //发起人openid
                String openId = user.getWx_id();
                logger.error("openId: {}", openId);
                //获取token
                String accessToken = publiceService.getWeiXinToken(storeId);
                logger.error("accessToken: {}", accessToken);
                //快递公司信息
                ExpressModel expressModel = expressModelMapper.selectByPrimaryKey(exId);
                if (Objects.isNull(expressModel))
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_MBBCZ, "快递信息不存在");
                }
                //发送通知
                if (noticeModel != null)
                {
                    String              thing6 = "请随时关注订单进度";
                    Map<String, Object> map    = new HashMap<>(16);
                    map.put("thing6", new TemplateData(thing6));
                    map.put("character_string1", new TemplateData(orderNo));
                    map.put("thing2", new TemplateData(orderDetailsOld.getP_name()));
                    map.put("phrase3", new TemplateData(expressModel.getKuaidi_name()));
                    map.put("character_string4", new TemplateData(exNo));
                    String response = AppletUtil.sendMessage(accessToken, openId, noticeModel.getDelivery(), map);
                    logger.error("=================微信消息推送返回值：{}", response);
                }
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("发货异常： ", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "adminDelivery");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void electronicSheetDelivery(MainVo vo, Integer exId, String orderDetailIds) throws LaiKeAPIException
    {
        try
        {
            int row;
//            if (vo != null){
//                //后面记得删除 todo
//                //无法调试 禅道52796 【JAVA开发环境】电子面单（管理后台，移动端）：提示：请输入正确的电子面单数据
//                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QPZJQDDZMDSJ, "请配置正确的电子面单数据", "adminDelivery");
//            }
            //订单明细集
            List<Map<String, Object>> orderListIds = JSON.parseObject(orderDetailIds, new TypeReference<List<Map<String, Object>>>()
            {
            });
            //获取订单信息
            int storeId = vo.getStoreId();
            int len     = orderListIds.size();
            //是全部已经发货,如果详单都被发货,则同意修改订单状态为待收货
            boolean batchSend = false;
            //获取当前订单号
            String            orderNo;
            //打印方式
            PrintType printType = PrintType.IMAGE;
            OrderDetailsModel orderDetailsOld = new OrderDetailsModel();
            orderDetailsOld.setStore_id(vo.getStoreId());
            orderDetailsOld.setId(MapUtils.getInteger(orderListIds.get(0), "detailId"));
            orderDetailsOld = orderDetailsModelMapper.selectOne(orderDetailsOld);
            if (orderDetailsOld == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_XDBCZ, "详单不存在");
            }
            orderNo = orderDetailsOld.getR_sNo();
            //查询订单信息
            OrderModel orderModel = new OrderModel();
            orderModel.setStore_id(vo.getStoreId());
            orderModel.setsNo(orderNo);
            orderModel = orderModelMapper.selectOne(orderModel);

            //系统配置
            ConfigModel configModel = new ConfigModel();
            configModel.setStore_id(vo.getStoreId());
            configModel = configModelMapper.selectOne(configModel);

            //是否开启云打印
            boolean is_open_cloud = configModel.getIs_open_cloud() == 1;

            //接口调用key
            String expressKey = configModel.getExpress_key();
            //secret在企业管理后台获取
            String expressSecret = configModel.getExpress_secret();
            //主单模板
            String expressTempId = configModel.getExpress_tempId();
            if (StringUtils.isEmpty(expressKey))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_KD100WPZ, "快递100未配置请联系管理员");
            }

            if (StringUtils.isEmpty(expressSecret))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_PTDZMDWPZ, "商城电子面单未配置请联系管理员");
            }

            if (StringUtils.isEmpty(expressTempId))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_PTDZMDWPZMBID, "商城电子面单未配置模版ID");
            }
            //获取快递公司信息
            ExpressModel expressModel = expressModelMapper.selectByPrimaryKey(exId);
            if (expressModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SJCW, "数据错误");
            }
            //店铺id
            String mchId = StringUtils.trim(orderModel.getMch_id(), SplitUtils.DH);
            //获取店铺的对应物流电子面单配置
            ExpressSubtableModel expressSubtableModel = new ExpressSubtableModel();
            expressSubtableModel.setStoreId(vo.getStoreId());
            expressSubtableModel.setMchId(Integer.parseInt(mchId));
            expressSubtableModel.setExpressId(exId);
            expressSubtableModel.setRecovery(DictionaryConst.WhetherMaven.WHETHER_NO);
            expressSubtableModel = expressSubtableModelMapper.selectOne(expressSubtableModel);
            if (expressSubtableModel == null || StringUtils.isEmpty(expressSubtableModel.getPartnerId()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SJCW, "数据错误");
            }
            //统计当前订单下有多少件商品
            int orderGoodsNum = orderModel.getNum();
            //统计当前已发货数量
            int deliverNum = orderDetailsModelMapper.getDeliverNumBySNo(vo.getStoreId(), orderNo);
            //统计这次发货数量
            int nowDeliverNum = 0;

            for (Map<String, Object> orderListId : orderListIds)
            {
                int id  = MapUtils.getIntValue(orderListId, "detailId");
                int num = MapUtils.getIntValue(orderListId, "num");
                if (num == 0)
                {
                    logger.debug("发货失败,发货商品数量错误 orderId{}", id);
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QSRSPSL, "请输入商品数量");
                }
                //是否存在售后中商品
                if (returnOrderModelMapper.orderDetailReturnIsNotEnd(vo.getStoreId(), orderNo, id) > 0)
                {
                    logger.error("后台发货 orderId:{},发货失败:商品正在售后中或已关闭!", id);
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SPZTFSBH_QSXLB, "商品状态发生变化,请刷新列表!");
                }
                int DeliverNum = 0;
                if (orderDetailsOld.getDeliverNum() != null)
                {
                    DeliverNum = orderDetailsOld.getDeliverNum();
                }
                if (DeliverNum + num > orderDetailsOld.getNum())
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SJCW, "数据错误");
                }
            }
            //电子面单发货
            //收件人信息
            ManInfo recManInfo = new ManInfo();
            recManInfo.setName(orderModel.getName());
            recManInfo.setMobile(orderModel.getMobile());
            recManInfo.setPrintAddr(orderModel.getSheng() + orderModel.getShi() + orderModel.getXian() + orderModel.getAddress());

            //寄件人信息
            ServiceAddressModel serviceAddressModel = new ServiceAddressModel();
            serviceAddressModel.setStore_id(storeId);
            serviceAddressModel.setUid("admin");
            serviceAddressModel.setType(ServiceAddressModel.TYPE_DELIVER_GOODS);
            serviceAddressModel.setIs_default(DictionaryConst.DefaultMaven.DEFAULT_OK);
            serviceAddressModel = serviceAddressModelMapper.selectOne(serviceAddressModel);
            if (Objects.isNull(serviceAddressModel))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QXTJSHDZ, "售后地址不存在");
            }

            ManInfo sendManInfo = new ManInfo();
            sendManInfo.setName(serviceAddressModel.getName());
            sendManInfo.setMobile(serviceAddressModel.getTel());
            String dizhi = serviceAddressModel.getSheng() + serviceAddressModel.getShi() + serviceAddressModel.getXian() + serviceAddressModel.getAddress();
            sendManInfo.setPrintAddr(dizhi);

            OrderReq orderReq = new OrderReq();
            //电子面单客户账户或月结账号
            orderReq.setPartnerId(expressSubtableModel.getPartnerId());
            orderReq.setKuaidicom(expressModel.getType());
            orderReq.setCount(1);
            //主单模板
            orderReq.setTempId(expressSubtableModel.getTemp_id());
            orderReq.setSendMan(sendManInfo);
            orderReq.setRecMan(recManInfo);
            //电子面单打印时号码脱敏
            orderReq.setNeedDesensitization(true);
            orderReq.setPartnerSecret(expressSubtableModel.getPartnerSecret());
            orderReq.setPartnerKey(expressSubtableModel.getPartnerKey());
/*            //子单号
            orderReq.setNeedBack("1");
            //回单号
            orderReq.setNeedChild("1");*/

            //判断是否开启了快递100云打印
            if (is_open_cloud)
            {
                orderReq.setSiid(configModel.getSiid());
                printType = PrintType.CLOUD;
                orderReq.setCallBackUrl(configModel.getCloud_notify());
            }
            //物品名称
            orderReq.setCargo(orderDetailsOld.getP_name());
            orderReq.setPrintType(printType);
            if (expressModel.getType().equals("jitu"))
            {
                orderReq.setWeight(1);
            }
            String param = new Gson().toJson(orderReq);
            String t     = System.currentTimeMillis() + "";

            PrintReq printReq = new PrintReq();
            printReq.setT(t);
            printReq.setKey(expressKey);
            //32位大写，签名，用于验证身份，按MD5 (param +t+key+ secret)的顺序进行MD5加密，不需要加上“+”号
            //org.apache.commons.codec.digest.DigestUtils.md5Hex(msg).toUpperCase();
            printReq.setSign(MD5Util.MD5Is32(param + t + expressKey + expressSecret, null).toUpperCase());
            printReq.setMethod(ApiInfoConstant.ORDER);
            printReq.setParam(param);
            logger.error("电子面单请求参数{}", printReq);

            IBaseClient baseClient = new LabelV2();
            HttpResult  execute    = baseClient.execute(printReq);
            logger.error("电子面单返回值{}", execute.toString());
            Map<String, Object> bodyMap = JSON.parseObject(execute.getBody(), new TypeReference<Map<String, Object>>()
            {
            });
            String message = MapUtils.getString(bodyMap, "message");
            if (!MapUtils.getString(bodyMap, "code").equals("200"))
            {
                logger.error("电子面单返错误!");
                throw new LaiKeAPIException(ErrorCode.SysErrorCode.ALL_CODE,message, "adminDelivery");
            }
            Map<String, Object> dataMap = (Map<String, Object>) bodyMap.get("data");
            //订单号
            String exNo = MapUtils.getString(dataMap, "kuaidinum");
            //task_id
            String task_id = MapUtils.getString(dataMap, "taskId");

            //快递公司订单号
            String kdComOrderNum = MapUtils.getString(dataMap, "kdComOrderNum");
            //面单短链
            String label = MapUtils.getString(dataMap, "label");
            //发货记录
            ExpressDeliveryModel expressDeliveryModel;
            //订单明细发货
            for (Map<String, Object> orderListId : orderListIds)
            {
                int id  = MapUtils.getIntValue(orderListId, "detailId");
                int num = MapUtils.getIntValue(orderListId, "num");
                orderDetailsOld = orderDetailsModelMapper.selectByPrimaryKey(id);
                String oExId = orderDetailsOld.getExpress_id();
                if (StringUtils.isNotEmpty(oExId))
                {
                    oExId = oExId + "," + exId;
                }
                else
                {
                    oExId = exId.toString();
                }
                String oExNo = orderDetailsOld.getCourier_num();
                if (StringUtils.isNotEmpty(oExNo))
                {
                    oExNo = oExNo + "," + exNo;
                }
                else
                {
                    oExNo = exNo;
                }
                int DeliverNum = 0;
                if (orderDetailsOld.getDeliverNum() != null)
                {
                    DeliverNum = orderDetailsOld.getDeliverNum();
                }
                nowDeliverNum += num;
                //修改订单明细 修改明细订单状态为待收货
                OrderDetailsModel orderDetailsModel = new OrderDetailsModel();
                orderDetailsModel.setId(id);
                if (DeliverNum + num == orderDetailsOld.getNum())
                {
                    orderDetailsModel.setR_status(ORDERS_R_STATUS_DISPATCHED);
                }
                orderDetailsModel.setExpress_id(oExId);
                orderDetailsModel.setCourier_num(oExNo);
                orderDetailsModel.setDeliverNum(DeliverNum + num);
                orderDetailsModel.setDeliver_time(new Date());
                row = orderDetailsModelMapper.updateByPrimaryKeySelective(orderDetailsModel);
                if (row < 1)
                {
                    logger.debug("订单{} 发货失败,明细id{}", orderNo, id);
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_FHSB, "发货失败");
                }
                expressDeliveryModel = new ExpressDeliveryModel();
                expressDeliveryModel.setStore_id(storeId);
                expressDeliveryModel.setsNo(orderNo);
                expressDeliveryModel.setExpressId(exId);
                expressDeliveryModel.setTask_id(task_id);
                expressDeliveryModel.setCourierNum(exNo);
                expressDeliveryModel.setOrderDetailsId(orderDetailsModel.getId());
                expressDeliveryModel.setNum(num);

                if (StringUtils.isNotEmpty(label) && !label.startsWith("https") && label.indexOf("http:") != -1)
                {
                    label = label.replace("http:", "https:");
                }

                expressDeliveryModel.setLabel(label);
                expressDeliveryModel.setKdComOrderNum(kdComOrderNum);
                expressDeliveryModel.setSubtableId(expressSubtableModel.getId());
                expressDeliveryModel.setDeliverTime(new Date());
                row = expressDeliveryModelMapper.insertSelective(expressDeliveryModel);
                if (row < 1)
                {
                    logger.debug("订单{} 发货失败,明细id{}", orderNo, id);
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_FHSB, "发货失败");
                }
            }
            //是否都已经完成了发货,完成了发货则修改订单全部状态为待收货
            if (orderGoodsNum < deliverNum + nowDeliverNum)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SJCW, "数据错误");
            }
            //是否已经全部发货
            if (orderGoodsNum == deliverNum + nowDeliverNum)
            {
                //批量发货
                batchSend = true;
            }
            if (batchSend)
            {
                row = orderModelMapper.updateOrderStatusByOrderNo(vo.getStoreId(), ORDERS_R_STATUS_DISPATCHED, orderNo);
                if (row < 1)
                {
                    logger.debug("订单{} 发货失败,状态修改失败", orderNo);
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_FHSB, "发货失败");
                }
                //订单来源小程序并且使用小程序支付的话 全部发货完调用微信发货同步信息
                if (orderModel.getSource().toString().equals(DictionaryConst.StoreSource.LKT_LY_001)
                        && orderModel.getPay().equals(DictionaryConst.OrderPayType.ORDERPAYTYPE_MINI_WECHAT))
                {
                    publicExpressService.setWxAppUploadShippingInfo(vo, orderNo);
                }
            }
            //站内推送发货信息
            publicAdminService.systemMessageSend(vo, SystemMessageModel.ReadType.UNREAD, "系统消息", "您购买的商品已经在赶去见您的路上啦!", orderDetailsOld.getUser_id());
            //发送模板消息
            NoticeModel noticeModel = new NoticeModel();
            noticeModel.setStore_id(storeId);
            noticeModel = noticeModelMapper.selectOne(noticeModel);
            if (Objects.isNull(noticeModel))
            {
                logger.debug("该商城id{}暂无微信推送模板", storeId);
            }
            //当前用户信息
            User userEntity = new User();
            userEntity.setUser_id(orderDetailsOld.getUser_id());
            User user = userBaseMapper.selectOne(userEntity);
            logger.error("userId: {}", user.getUser_id());
            if (StringUtils.isNotEmpty(user.getWx_id()))
            {
                //发起人openid
                String openId = user.getWx_id();
                logger.error("openId: {}", openId);
                //获取token
                String accessToken = publiceService.getWeiXinToken(storeId);
                logger.error("accessToken: {}", accessToken);
                //发送通知
                if (noticeModel != null)
                {
                    String              thing6 = "请随时关注订单进度";
                    Map<String, Object> map    = new HashMap<>(16);
                    map.put("thing6", new TemplateData(thing6));
                    map.put("character_string1", new TemplateData(orderNo));
                    map.put("thing2", new TemplateData(orderDetailsOld.getP_name()));
                    map.put("phrase3", new TemplateData(expressModel.getKuaidi_name()));
                    map.put("character_string4", new TemplateData(exNo));
                    String response = AppletUtil.sendMessage(accessToken, openId, noticeModel.getDelivery(), map);
                    logger.error("=================微信消息推送返回值：{}", response);
                }
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("电子面单发货异常： ", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "adminDelivery");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String cancelElectronicSheetDelivery(MainVo vo, Integer expressDeliveryId) throws LaiKeAPIException
    {
        String msg = "操作成功";
        try
        {
            int row;
            if (StringUtils.isEmpty(expressDeliveryId))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误", "cancelElectronicSheetDelivery");
            }
            //系统配置
            ConfigModel configModel = new ConfigModel();
            configModel.setStore_id(vo.getStoreId());
            configModel = configModelMapper.selectOne(configModel);
            //接口调用key
            String expressKey = configModel.getExpress_key();
            //secret在企业管理后台获取
            String expressSecret = configModel.getExpress_secret();
            //主单模板
            String expressTempId = configModel.getExpress_tempId();
            if (StringUtils.isEmpty(expressKey))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_KD100WPZ, "快递100未配置请联系管理员");
            }
            if (StringUtils.isEmpty(expressSecret))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_PTDZMDWPZ, "商城电子面单未配置请联系管理员");
            }
            if (StringUtils.isEmpty(expressTempId))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_PTDZMDWPZMBID, "商城电子面单未配置模版ID");
            }
            ExpressDeliveryModel expressDeliveryModel = expressDeliveryModelMapper.selectByPrimaryKey(expressDeliveryId);
            if (expressDeliveryModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLBCZ, "物流不存在", "cancelElectronicSheetDelivery");
            }
            //快递单号
            String courierNum = expressDeliveryModel.getCourierNum();
            //快递公司id
            Integer expressId = expressDeliveryModel.getExpressId();
            //快递公司订单号
            String kdComOrderNum = expressDeliveryModel.getKdComOrderNum();
            //快递公司子表ID
            Integer subtableId = expressDeliveryModel.getSubtableId();
            //订单号
            String       sNo          = expressDeliveryModel.getsNo();
            ExpressModel expressModel = expressModelMapper.selectByPrimaryKey(expressId);
            if (expressModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SJCW, "数据错误");
            }
            expressDeliveryModel = new ExpressDeliveryModel();
            expressDeliveryModel.setStore_id(vo.getStoreId());
            expressDeliveryModel.setCourierNum(courierNum);
            expressDeliveryModel.setExpressId(expressId);
            List<ExpressDeliveryModel> list = expressDeliveryModelMapper.select(expressDeliveryModel);
            OrderDetailsModel          orderDetailsModel;
            //快递公司id
            List<String> expressIds;
            //快递单号
            List<String>  courierNums;
            StringBuilder detailExpressId;
            StringBuilder detailCourierNum;
            //开始回滚订单发货数量
            for (ExpressDeliveryModel deliveryModel : list)
            {
                detailExpressId = new StringBuilder();
                detailCourierNum = new StringBuilder();
                //发货数量
                Integer num = deliveryModel.getNum();
                //详单id
                Integer orderDetailsId = deliveryModel.getOrderDetailsId();
                //修改订单明细 修改明细订单状态为待发货
                orderDetailsModel = orderDetailsModelMapper.selectByPrimaryKey(orderDetailsId);
                expressIds = new ArrayList<>(Arrays.asList(expressId.toString().split(SplitUtils.DH)));
                courierNums = new ArrayList<>(Arrays.asList(orderDetailsModel.getCourier_num().split(SplitUtils.DH)));
                expressIds.remove(expressId.toString());
                courierNums.remove(courierNum);
                for (String id : expressIds)
                {
                    detailExpressId.append(id).append(SplitUtils.DH);
                }
                for (String s : courierNums)
                {
                    detailCourierNum.append(s).append(SplitUtils.DH);
                }
                orderDetailsModel.setId(orderDetailsId);
                orderDetailsModel.setR_status(ORDERS_R_STATUS_CONSIGNMENT);
                orderDetailsModel.setExpress_id(StringUtils.trim(detailExpressId.toString(), SplitUtils.DH));
                orderDetailsModel.setCourier_num(StringUtils.trim(detailCourierNum.toString(), SplitUtils.DH));
                orderDetailsModel.setDeliverNum(orderDetailsModel.getDeliverNum() - num);
                orderDetailsModel.setDeliver_time(null);
                row = orderDetailsModelMapper.updateByPrimaryKeySelective(orderDetailsModel);
                if (row < 1)
                {
                    logger.debug("订单{} 取消电子面单发货失败,明细id{}", sNo, orderDetailsId);
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_FHSB, "发货失败");
                }
            }
            //修改订单状态
            row = orderModelMapper.updateOrderStatusByOrderNo(vo.getStoreId(), ORDERS_R_STATUS_CONSIGNMENT, sNo);
            if (row < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_FHSB, "发货失败");
            }
            ExpressSubtableModel expressSubtableModel = expressSubtableModelMapper.selectByPrimaryKey(subtableId);
            if (expressSubtableModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SJCW, "数据错误");
            }


            boolean cancellation = isCancellation(expressModel.getType());
            if (!cancellation)
            {
                msg = "该快递公司暂不支持取消电子面单，请重新选择快递公司发货";
                return msg;
            }

            //取消面单
            LabelCancelParam labelCancelParam = new LabelCancelParam();
            //labelCancelParam.setPartnerId(expressSubtableModel.getPartnerId());
            labelCancelParam.setKuaidicom(expressModel.getType());
            labelCancelParam.setKuaidinum(courierNum);
            //快递公司订单号(对应下单时返回的kdComOrderNum，如果没有可以不传，否则必传)
            //labelCancelParam.setOrderId(kdComOrderNum);

            //labelCancelParam.setReason("暂时不寄了");
            String param = new Gson().toJson(labelCancelParam);
            String t     = System.currentTimeMillis() + "";

            PrintReq printReq = new PrintReq();
            printReq.setT(t);
            printReq.setKey(expressKey);
            printReq.setMethod(ApiInfoConstant.CANCEL_METHOD);
            printReq.setSign(MD5Util.MD5Is32(param + t + expressKey + expressSecret, null).toUpperCase());
            printReq.setParam(param);

            IBaseClient baseClient = new LabelCancel();
            HttpResult  execute    = baseClient.execute(printReq);
            logger.info("取消电子面单发货返回值" + execute.toString());
            Map<String, Object> bodyMap = JSON.parseObject(execute.getBody(), new TypeReference<Map<String, Object>>()
            {
            });
            String message = MapUtils.getString(bodyMap, "message");
            if (!MapUtils.getString(bodyMap, "returnCode").equals("200"))
            {
                logger.error("取消电子面单发货错误!");
                throw new LaiKeAPIException(ErrorCode.SysErrorCode.ALL_CODE, message, "adminDelivery");
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("取消电子面单发货：", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "adminDelivery");
        }
        return msg;
    }

    @Override
    public Map<String, Object> storeOrderDetails(int storeId, String ordernno) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            //订单基本信息
            Map<String, Object> orderInfoMap = new HashMap<>(16);
            //收货人信息
            Map<String, Object> receivingInfoMap = new HashMap<>(16);
            //商品信息
            List<Map<String, Object>> goodsList = new ArrayList<>();
            //下单人折扣
            BigDecimal orderUserGrate = BigDecimal.ONE;

            //获取订单信息
            OrderModel orderModel = new OrderModel();
            orderModel.setStore_id(storeId);
            orderModel.setsNo(ordernno);
            orderModel = orderModelMapper.selectOne(orderModel);
            if (orderModel == null)
            {
                throw new LaiKeAPIException(DATA_NOT_EXIST, "订单不存在");
            }
            if (orderModel.getGrade_rate().compareTo(BigDecimal.ZERO) > 0)
            {
                orderUserGrate = orderModel.getGrade_rate();
            }
            //店铺优惠金额
            BigDecimal mchDisAmt = orderModel.getCoupon_price();
            if (orderModel.getSubtraction_id() != null)
            {
                if (orderModel.getSubtraction_id() != null && orderModel.getSubtraction_id() == 0)
                {
                    BigDecimal dicPirce = orderModel.getPreferential_amount();
                    //优惠卷金额+优惠金额
                    mchDisAmt = mchDisAmt.add(dicPirce);
                }
            }
            orderInfoMap.put("mch_discount", mchDisAmt);
            orderInfoMap.put("preferential_amount", orderModel.getPreferential_amount());

            int shopId = StringUtils.stringParseInt(orderModel.getMch_id().split(SplitUtils.DH)[1]);
            //获取订单设置
            OrderConfigModal orderConfigModal = new OrderConfigModal();
            orderConfigModal.setStore_id(storeId);
            orderConfigModal = orderConfigModalMapper.selectOne(orderConfigModal);
            //获取提现发货时间间隔 默认七天
            Date remindSendDate = DateUtil.getAddDate(new Date(), 7);
            if (orderConfigModal != null && orderConfigModal.getRemind() != null && orderConfigModal.getRemind() > 0)
            {
                BigDecimal remmindDay = new BigDecimal(orderConfigModal.getRemind()).divide(new BigDecimal("24"), 2, BigDecimal.ROUND_HALF_UP);
                long       remndHour  = orderConfigModal.getRemind() % 24;
                remndHour = DateUtil.dateConversion(remndHour, DateUtil.TimeType.TIME);
                remindSendDate = DateUtil.getAddDate(new Date(), remmindDay.intValue());
                remindSendDate = DateUtil.getAddDateBySecond(remindSendDate, Integer.parseInt(Long.toString(remndHour)));
            }
            //进入订单详情把未读状态改成已读状态，已读状态的状态不变
            MessageLoggingModal messageLoggingUpdate = new MessageLoggingModal();
            messageLoggingUpdate.setIs_popup(1);
            messageLoggingUpdate.setRead_or_not(1);
            if (orderModel.getReadd() == 0)
            {
                //订单未读处理
                OrderModel orderUpdate = new OrderModel();
                orderUpdate.setId(orderModel.getId());
                orderUpdate.setReadd(READ);
                if (orderModel.getDelivery_status() == 1)
                {
                    orderUpdate.setRemind(remindSendDate);
                }
                int row = orderModelMapper.updateByPrimaryKeySelective(orderUpdate);
                if (row < 1)
                {
                    logger.debug("{} 修改已读状态失败", ordernno);
                }
                //后台已读订单处理
                publiceService.messageUpdate(storeId, shopId, MessageLoggingModal.Type.TYPE_ORDER_WAIT_SEND, ordernno, messageLoggingUpdate);
                publiceService.messageUpdate(storeId, shopId, MessageLoggingModal.Type.TYPE_ORDER_REMIND_SEND, ordernno, messageLoggingUpdate);
                publiceService.messageUpdate(storeId, shopId, MessageLoggingModal.Type.TYPE_ORDER_NEW, ordernno, messageLoggingUpdate);
            }
            if (ORDERS_R_STATUS_COMPLETE == orderModel.getStatus())
            {
                publiceService.messageUpdate(storeId, shopId, MessageLoggingModal.Type.TYPE_ORDER_OK_GOODS, ordernno, messageLoggingUpdate);
            }
            else if (ORDERS_R_STATUS_CLOSE == orderModel.getStatus())
            {
                publiceService.messageUpdate(storeId, shopId, MessageLoggingModal.Type.TYPE_ORDER_CLOSE, ordernno, messageLoggingUpdate);
            }
            orderInfoMap.put("orderno", orderModel.getsNo());
            //获取订单状态
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("name", "订单状态");
            parmaMap.put("value", orderModel.getStatus());
            List<Map<String, Object>> orderStatusMap = dictionaryNameModelMapper.selectDynamic(parmaMap);
            if (orderStatusMap != null)
            {
                orderInfoMap.put("status", orderStatusMap.get(0).get("text"));
            }
            orderInfoMap.put("z_freight", orderModel.getZ_freight());
            orderInfoMap.put("source", orderModel.getSource());
            //支付方式
            String       payType      = "未知";
            PaymentModel paymentModel = new PaymentModel();
            paymentModel.setClass_name(orderModel.getPay());
            paymentModel = paymentModelMapper.selectOne(paymentModel);
            if (paymentModel != null)
            {
                payType = paymentModel.getName();
            }
            orderInfoMap.put("payType", payType);
            orderInfoMap.put("orderDate", DateUtil.dateFormate(orderModel.getAdd_time(), GloabConst.TimePattern.YMDHMS));
            orderInfoMap.put("payDate", DateUtil.dateFormate(orderModel.getPay_time(), GloabConst.TimePattern.YMDHMS));

            //发货时间
            String sendDate = null;
            //运费
            BigDecimal yunfei = BigDecimal.ZERO;
            //快递单号
            List<Map<String, Object>> kuaidiList = new ArrayList<>();
            //会员优惠金额
            BigDecimal youhuiVipPrice;
            //商品总价
            BigDecimal goodsPrice = BigDecimal.ZERO;

            //优惠方式
            String discountType = "";
            if (orderModel.getSubtraction_id() != null && orderModel.getSubtraction_id() > 0)
            {
                discountType = "满减";
            }
            else if (StringUtils.isNotEmpty(orderModel.getCoupon_id()))
            {
                orderModel.setCoupon_id(StringUtils.trim(orderModel.getCoupon_id(), SplitUtils.DH));
                String[] couponIds = orderModel.getCoupon_id().split(SplitUtils.DH);
                if (!"0".equals(couponIds[0]))
                {
                    discountType = "优惠券";
                }
            }

            //获取明细信息
            List<Map<String, Object>> orderDetailList = orderDetailsModelMapper.selectStoreOrderDetails(storeId, ordernno);
            for (Map<String, Object> map : orderDetailList)
            {
                Map<String, Object> goodsMap = new HashMap<>(16);
                if (StringUtils.isNotEmpty(sendDate) && map.containsKey("deliver_time"))
                {
                    sendDate = map.get("deliver_time").toString();
                }
                //获取快递信息
                if (map.containsKey("express_id"))
                {
                    String       courierNo    = map.get("courier_num") + "";
                    int          exId         = StringUtils.stringParseInt(map.get("express_id"));
                    ExpressModel expressModel = new ExpressModel();
                    expressModel.setId(exId);
                    expressModel = expressModelMapper.selectOne(expressModel);
                    if (expressModel != null)
                    {
                        Map<String, Object> kuaidiMap = new HashMap<>(16);
                        kuaidiMap.put("kuaidi_name", expressModel.getKuaidi_name());
                        kuaidiMap.put("kuaidi_no", courierNo);
                        kuaidiList.add(kuaidiMap);
                    }
                }
                //图片处理
                String goodsImgUrl = MapUtils.getString(map, "img");
                goodsImgUrl = publiceService.getImgPath(goodsImgUrl, storeId);

                BigDecimal yf       = new BigDecimal(MapUtils.getString(map, "freight"));
                BigDecimal needNum  = new BigDecimal(map.get("num").toString());
                BigDecimal price    = new BigDecimal(map.get("p_price").toString());
                BigDecimal orderAmt = price.multiply(needNum);
                goodsPrice = goodsPrice.add(orderAmt);
                yunfei = yunfei.add(yf);

                goodsMap.put("goodsName", map.get("p_name"));
                goodsMap.put("goodsId", orderModel.getPid());
                goodsMap.put("goodsImgUrl", goodsImgUrl);
                goodsMap.put("attrId", map.get("sid"));
                goodsMap.put("size", map.get("size"));
                goodsMap.put("commission", map.get("commission"));
                goodsMap.put("num", needNum);
                goodsMap.put("price", price);
                //小计
                goodsMap.put("subtotal", orderAmt);

                goodsList.add(goodsMap);
            }
            orderInfoMap.put("sendDate", sendDate);
            orderInfoMap.put("kuaidiList", kuaidiList);
            orderInfoMap.put("arriveDate", orderModel.getArrive_time());
            orderInfoMap.put("userid", orderModel.getUser_id());
            User user = new User();
            user.setUser_id(orderModel.getUser_id());
            user = userBaseMapper.selectOne(user);
            String userName = "";
            if (user != null)
            {
                userName = user.getUser_name();
            }
            orderInfoMap.put("userName", userName);
            orderInfoMap.put("remarks", orderModel.getRemarks());
            //收货人信息
            receivingInfoMap.put("name", orderModel.getName());
            receivingInfoMap.put("tel", orderModel.getMobile());
            receivingInfoMap.put("address", orderModel.getName());
            receivingInfoMap.put("sheng", orderModel.getSheng());
            receivingInfoMap.put("shi", orderModel.getShi());
            receivingInfoMap.put("xian", orderModel.getXian());
            receivingInfoMap.put("remark", orderModel.getRemark());
            //商品总价
            resultMap.put("goodsPrice", goodsPrice);
            //订单金额
            resultMap.put("z_price", orderModel.getZ_price());
            //运费
            resultMap.put("freightPrice", yunfei);
            //会员折扣价
            youhuiVipPrice = goodsPrice.subtract(goodsPrice.multiply(orderUserGrate));
            resultMap.put("youhuiVipPrice", youhuiVipPrice);

            resultMap.put("status", orderModel.getStatus());
            //优惠方式
            resultMap.put("discount_type", discountType);
            resultMap.put("pay_price", orderModel.getZ_price());
            //订单信息
            resultMap.put("orderInfo", orderInfoMap);
            //收货人信息
            resultMap.put("receivingInfo", receivingInfoMap);
            //商品信息
            resultMap.put("goodsList", goodsList);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("订单详情(后台、PC店铺) 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "storeOrderDetails");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> mchOrderDetails(MchOrderDetailVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", vo.getStoreId());
            parmaMap.put("orderno", vo.getsNo());
            parmaMap.put("mch_id", vo.getShopId());
            parmaMap.put("mch_pro", "mch_pro");
            if (StringUtils.isNotEmpty(vo.isSupplierPro()) && !vo.isSupplierPro())
            {
                parmaMap.put("supplierPro", "supplierPro");
            }
            //订单优惠后总价
            BigDecimal orderReduceAmt    = BigDecimal.ZERO;
            BigDecimal platform_discount = BigDecimal.ZERO;
            BigDecimal store_discount    = BigDecimal.ZERO;
            //会员折扣优惠
            BigDecimal                gradeRateAmount = BigDecimal.ZERO;
            List<Map<String, Object>> orderDetailList = orderModelMapper.getOrdersNumDynamic(parmaMap);
            //是否显示查看物流
            //是否有商品已经发货，显示发货按钮
            boolean haveExpress = false;
            if (orderDetailList != null && orderDetailList.size() > 0)
            {
                for (Map<String, Object> order : orderDetailList)
                {

                    boolean isLogistics = false;
                    order.put("add_time", DateUtil.dateFormate(order.get("add_time") + "", GloabConst.TimePattern.YMDHMS));
                    //主键id
                    int id = Integer.parseInt(order.get("id").toString());
                    //店铺id
                    String mchIdListStr = order.get("mch_id").toString();
                    //订单号
                    String orderno = order.get("sNo").toString();
                    //订单金额
                    BigDecimal orderAmt = new BigDecimal(order.get("z_price").toString());
                    //商品总价
                    BigDecimal goodsAmt = BigDecimal.ZERO;
                    //满减金额
                    BigDecimal reduceAmt = new BigDecimal(order.get("reduce_price").toString());
                    //折扣，此处grade_rate存储的是10，应该除以10换成正确折扣
                    BigDecimal gradeRate = new BigDecimal(order.get("grade_rate").toString()).divide(new BigDecimal("10"), 2, BigDecimal.ROUND_HALF_UP);
                    //优惠卷金额
                    BigDecimal couponAmt = new BigDecimal(order.get("coupon_price").toString());
                    //订单状态
                    int status = Integer.parseInt(order.get("status").toString());
                    //店铺id
                    String[] mchIdList = StringUtils.trim(mchIdListStr, SplitUtils.DH).split(SplitUtils.DH);
                    //订单取消方式
                    Integer cancel_method = 0;
                    //满减活动ID
                    Integer subtractionId = StringUtils.stringParseInt(order.get("coupon_id") + "");
                    //会员的等级折扣
                    BigDecimal greadeRate = new BigDecimal(order.get("grade_rate").toString());
                    //优惠卷金额  --平台优惠 + 店铺优惠
                    BigDecimal mchCouponPrice = new BigDecimal(order.get("coupon_price").toString());
                    //是否自提
                    int selfLifting = StringUtils.stringParseInt(order.get("self_lifting").toString());
                    //是否是是拆单后的订单
                    String pSno = "";
                    if (order.containsKey("p_sNo"))
                    {
                        pSno = DataUtils.getStringVal(order, "p_sNo");
                    }

                    //平台优惠金额
                    BigDecimal preferentialAmount = new BigDecimal(order.get("preferential_amount").toString());
                    //现已为店铺优惠
                    mchCouponPrice = mchCouponPrice.subtract(preferentialAmount);
                    //优惠券
                    String couponId = MapUtils.getString(order, "coupon_id", "0");
                    //平台优惠券金额
                    BigDecimal preferential_amount = BigDecimal.ZERO;
                    // 商品售价
                    BigDecimal orderDetailsPPrice = DataUtils.getBigDecimalVal(order, "p_price");
                    // 商品数量
                    BigDecimal pNum = DataUtils.getBigDecimalVal(order, "num");
                    // 优惠后的金额
                    BigDecimal afterDiscount = DataUtils.getBigDecimalVal(order, "Dafter_discount");
                    // 单cid下平台优惠
                    BigDecimal platform_coupon_price = DataUtils.getBigDecimalVal(order, "platform_coupon_price");
                    // 单cid下店铺优惠
                    BigDecimal store_coupon_price = DataUtils.getBigDecimalVal(order, "store_coupon_price");
                    platform_discount = platform_discount.add(platform_coupon_price);
                    store_discount = store_discount.add(store_coupon_price);
                    String discountType = "";
                    //类型：优惠券coupon、满减subtraction 0,12 或者  12,21 最后一个逗号后面的数字就是平台优惠券id
                    String platCouponId = null;
                    if (!Objects.isNull(couponId))
                    {
                        String[] couponsId = StringUtils.trim(couponId, SplitUtils.DH).split(SplitUtils.DH);
                        platCouponId = couponsId[couponsId.length - 1];
                    }
                    else
                    {
                        platCouponId = "0";
                    }
                    if (!"0".equals(platCouponId))
                    {
                        //平台优惠券优惠类型
                        discountType = "优惠券";
                        //平台优惠券和店铺优惠券合在一块了
                        couponAmt = couponAmt.subtract(preferentialAmount);
                    }
                    //多店铺优惠劵拆单--》优惠金额特殊处理
                    boolean isCouponHandle = false;
                    if (StringUtils.isNotEmpty(pSno) && (BigDecimal.ZERO.compareTo(preferentialAmount) < 0 || BigDecimal.ZERO.compareTo(couponAmt) < 0))
                    {
                        isCouponHandle = true;
                        //这里是订单明细优惠
                        preferential_amount = preferential_amount.add(orderDetailsPPrice.multiply(pNum).subtract(afterDiscount));
//                    coupon_price = BigDecimal.ZERO;
                    }
                    if (isCouponHandle)
                    {
                        //拆单后总优惠金额 - 该店铺优惠金额
                        preferentialAmount = preferential_amount.subtract(couponAmt);
                    }


                    int subtraction = DataUtils.getIntegerVal(order, "subtraction_id", 0);
                    if (subtraction != 0)
                    {
                        discountType = "满减";
                    }
                    order.put("discount_type", discountType);
                    //订单备注处理 getUnserializeObj
                    String remarks = order.get("remarks") + "";
                    if (StringUtils.isNotEmpty(remarks))
                    {
                        // 订单备注
                        Map<String, String> remarkMap = JSON.parseObject(remarks, new TypeReference<Map<String, String>>()
                        {
                        });
                        if (!remarkMap.isEmpty())
                        {
                            for (String key : remarkMap.keySet())
                            {
                                remarks = remarkMap.get(key);
                            }
                        }
                        else
                        {
                            remarks = "";
                        }
                    }
                    else
                    {
                        remarks = "";
                    }
                    //判断有无订单售后未结束
                    int saleType = 0;
                    parmaMap.clear();
                    parmaMap.put("store_id", vo.getStoreId());
                    parmaMap.put("sNo", vo.getsNo());
                    parmaMap.put("rType", 1);
                    int count = returnOrderModelMapper.countRturnOrderNumDynamic(parmaMap);
                    if (count > 0)
                    {
                        saleType = 1;
                    }
                    //判断订单是否全在售后且未结束
                    parmaMap.clear();
                    parmaMap.put("store_id", vo.getStoreId());
                    parmaMap.put("orderno", vo.getsNo());
                    parmaMap.put("notStatus", DictionaryConst.OrdersStatus.ORDERS_R_STATUS_CLOSE);
                    int returnOrderNum = orderDetailsModelMapper.countOrderDetailDynamic(parmaMap);
                    if (returnOrderNum == count)
                    {
                        saleType = 2;
                    }


                    //是否未读,未读则标记已读状态
                    OrderModel orderModel = new OrderModel();
                    orderModel.setId(id);
                    orderModel.setReadd(OrderModel.READ);
                    count = orderModelMapper.updateByPrimaryKeySelective(orderModel);
                    if (count < 1)
                    {
                        logger.info("订单标记已读失败 参数" + JSON.toJSONString(orderModel));
                    }
                    OrderModel orderModel1 = orderModelMapper.selectByPrimaryKey(id);
                    if (orderModel1.getStatus() == ORDERS_R_STATUS_CLOSE)
                    {
                        cancel_method = orderModel1.getCancel_method();
                    }
                    //是否为自提 则联系信息显示店铺信息
                    //禅道 46348 使用用户信息 2023-09-18
/*                    if (selfLifting == 1) {
                        //获取店铺信息
                        MchStoreModel mchStoreModel = new MchStoreModel();
                        mchStoreModel.setStore_id(vo.getStoreId());
                        mchStoreModel.setMch_id(vo.getShopId());
                        mchStoreModel.setIs_default(DictionaryConst.DefaultMaven.DEFAULT_OK);
                        mchStoreModel = mchStoreModelMapper.selectOne(mchStoreModel);
                        if (mchStoreModel != null) {
                            order.put("name", mchStoreModel.getName());
                            order.put("mobile", mchStoreModel.getMobile());
                            order.put("address", mchStoreModel.getAddress());
                        }
                    }*/
                    //获取下单人名称
                    String clientUserName = "";
                    String clientUserId   = order.get("user_id") + "";
                    User   user           = new User();
                    user.setUser_id(clientUserId);
                    user = userBaseMapper.selectOne(user);
                    if (user != null)
                    {
                        clientUserName = user.getUser_name();
                    }
                    order.put("user_name", clientUserName);
                    //商品价格处理
                    parmaMap.clear();
                    parmaMap.put("store_id", vo.getStoreId());
                    parmaMap.put("orderno", orderno);
                    parmaMap.put("mch_id", vo.getShopId());
                    List<Map<String, Object>> orderDetailInfoList = orderDetailsModelMapper.getOrderDetailByGoodsInfo(parmaMap);
                    //运费总价
                    BigDecimal freightAmt = BigDecimal.ZERO;

                    //商品总价
                    for (Map<String, Object> orderGoods : orderDetailInfoList)
                    {
                        boolean          showReturn = false;
                        ReturnOrderModel model      = canShowReturn(MapUtils.getInteger(orderGoods, "goodsId"), orderno);
                        if (Objects.nonNull(model))
                        {
                            showReturn = true;
                        }
                        if (showReturn)
                        {
                            orderGoods.put("returnId", model.getId());
                        }
                        orderGoods.put("showReturn", showReturn);
                        orderGoods.put("self_lifting",selfLifting);

                        //图片处理
                        String imgUrl = orderGoods.get("img") + "";
                        if (StringUtils.isEmpty(imgUrl))
                        {
                            imgUrl = orderGoods.get("imgurl") + "";
                        }
                        imgUrl = publiceService.getImgPath(imgUrl, vo.getStoreId());
                        orderGoods.put("pic", imgUrl);
                        //发货时间-只要有一个发了货就又查看物流按钮
                        String deliverTime = MapUtils.getString(orderGoods, "deliver_time");
                        if (!isLogistics && StringUtils.isNotEmpty(deliverTime))
                        {
                            isLogistics = true;
                        }
                        //当前商品价格
                        BigDecimal currentGoodsAmt = new BigDecimal(orderGoods.get("p_price").toString());
                        //当前商品数量
                        BigDecimal currentGoodsNum = new BigDecimal(orderGoods.get("num").toString());
                        //当前商品运费价格
                        BigDecimal currentFreightAmt = new BigDecimal(orderGoods.get("freight").toString());
                        if (StringUtils.isNotEmpty(MapUtils.getString(orderGoods, "express_id"))
                                && StringUtils.isNotEmpty(MapUtils.getString(orderGoods, "courier_num")))
                        {
                            haveExpress = true;
                        }
                        goodsAmt = goodsAmt.add(currentGoodsAmt.multiply(currentGoodsNum));
                        freightAmt = freightAmt.add(currentFreightAmt);
                        //处理订单详情中是否显示适用核销门店
                        if (orderGoods.containsKey("write_off_settings") && DataUtils.getStringVal(orderGoods, "write_off_settings").equals(ProductListModel.WRITE_OFF_SETTINGS.offline.toString()))
                        {
                            resultMap.put("show_write_store", 1);
                            //mchId,storeId
                            int i = 0;
                            if (DataUtils.getStringVal(orderGoods, "write_off_mch_ids").equals("0"))
                            {
                                Map<String, Object> Map = new HashMap<>();
                                Map.put("store_id", vo.getStoreId());
                                Map.put("mch_id", vo.getShopId());
                                i = mchStoreModelMapper.countDynamic(Map);
                            }
                            else
                            {
                                String[] split = DataUtils.getStringVal(orderGoods, "write_off_mch_ids").split(SplitUtils.DH);
                                i += split.length;
                            }
                            resultMap.put("write_store_num", i);
                        }
                        //处理预约信息
                        if (orderGoods.containsKey("write_off_settings") && DataUtils.getStringVal(orderGoods, "write_off_settings").equals(ProductListModel.WRITE_OFF_SETTINGS.offline.toString()) &&
                                orderGoods.containsKey("is_appointment") && DataUtils.getStringVal(orderGoods, "is_appointment").equals(ProductListModel.IS_APPOINTMENT.isOpin.toString()))
                        {
                            Map<String, Object> appointment   = new HashMap<>();
                            MchStoreModel       mchStoreModel = mchStoreModelMapper.selectByPrimaryKey(DataUtils.getIntegerVal(orderGoods, "mch_store_write_id"));
                            if (mchStoreModel != null)
                            {
                                appointment.put("time", DataUtils.getStringVal(orderGoods, "write_time"));
                                appointment.put("name", mchStoreModel.getName());
                                appointment.put("address", mchStoreModel.getSheng() + mchStoreModel.getShi() + mchStoreModel.getXian() + " " + mchStoreModel.getAddress());
                                order.put("appointment", appointment);
                                order.put("show_appointment", 1);
                            }
                        }
                        //虚拟商品特殊处理，添加可核销次数
                        if (orderGoods.containsKey("commodity_type") && DataUtils.getStringVal(orderGoods, "commodity_type").equals(ProductListModel.COMMODITY_TYPE.virtual.toString()))
                        {
                            parmaMap.clear();
                            parmaMap.put("p_id", DataUtils.getStringVal(orderGoods, "goodsId"));
                            parmaMap.put("r_sNo", orderno);
                            List<OrderDetailsModel> orderDetailsModels = orderDetailsModelMapper.selectByPidAndSno(parmaMap);
                            Integer                 write_off_num      = orderDetailsModels.get(0).getWrite_off_num();
                            orderGoods.put("write_off_num", write_off_num);
                            //待核销和核销完表示
                            //是否核销过,没核销过的为1，核销过为2 ，当有退款记录的时候也需要为2
                            ReturnOrderModel returnOrderModel = new ReturnOrderModel();
                            returnOrderModel.setsNo(vo.getsNo());
                            returnOrderModel.setP_id(orderDetailsModels.get(0).getId());
                            List<ReturnOrderModel> select = returnOrderModelMapper.select(returnOrderModel);
                            if (orderDetailsModels.get(0).getAfter_write_off_num() != 0 || select.size() > 0)
                            {
                                orderGoods.put("is_write", 2);
                            }
                            else
                            {
                                orderGoods.put("is_write", 1);
                            }
                            //为1时为待使用，为2时为已用完
                            orderGoods.put("status", orderDetailsModels.get(0).getWrite_off_num() != 0 ? 1 : 2);
                        }
                        //商家自配处理
                        if (selfLifting == 2 && StringUtils.isNotEmpty(DataUtils.getStringVal(order, "store_self_delivery")))
                        {
                            HashMap<String, Object> storeSelfInfo          = new HashMap<>();
                            StoreSelfDeliveryModel  storeSelfDeliveryModel = storeSelfDeliveryModelMapper.selectByPrimaryKey(DataUtils.getStringVal(order, "store_self_delivery"));
                            storeSelfInfo.put("delivery_time", DateUtil.dateFormate(storeSelfDeliveryModel.getDelivery_time(), GloabConst.TimePattern.YMD));
                            storeSelfInfo.put("delivery_period", storeSelfDeliveryModel.getDelivery_period());
                            storeSelfInfo.put("phone", storeSelfDeliveryModel.getPhone());
                            storeSelfInfo.put("courier_name", storeSelfDeliveryModel.getCourier_name());
                            order.put("storeSelfInfo", storeSelfInfo);
                        }
                        //发货方信息
                        ProductListModel proModel = productListModelMapper.selectByPrimaryKey(DataUtils.getStringVal(orderGoods, "goodsId"));
                        if (StringUtils.isNotEmpty(proModel.getGongyingshang()))
                        {
                            HashMap<String, Object> data          = new HashMap<>();
                            SupplierModel           supplierModel = supplierModelMapper.selectByPrimaryKey(proModel.getGongyingshang());
                            data.put("supplierName", supplierModel.getContacts());
                            data.put("supplierPhone", supplierModel.getContact_phone());
                            data.put("supplierAddress", supplierModel.getProvince() + supplierModel.getCity() + supplierModel.getArea() + supplierModel.getAddress());
                            data.put("orderTypeName", "供应商");
                            resultMap.put("supplierInfo", data);
                        }
                    }
                    //跨店铺未付款订单标识
                    boolean flag = (mchIdList.length > 1 && status == 0);
                    order.put("order_status", !flag);

                    BigDecimal reduceSumPrice = BigDecimal.ZERO;
                    BigDecimal couponSumPrice = BigDecimal.ZERO;
                    if (orderAmt.doubleValue() > 0)
                    {
                        // 该店铺商品总价 除以 整个订单商品总价 乘以 优惠的满减金额
                        reduceSumPrice = goodsAmt.divide(orderAmt, 2, BigDecimal.ROUND_HALF_UP).multiply(reduceAmt);
                        // 该店铺商品总价 除以 整个订单商品总价 乘以 优惠的优惠券金额
                        couponSumPrice = goodsAmt.divide(orderAmt, 5, BigDecimal.ROUND_HALF_UP).multiply(couponAmt).setScale(2, BigDecimal.ROUND_HALF_UP);

                        //计算会员特惠 折扣*(商品总价格-满减价格-优惠卷价格)+运费价格
                        //商品最后价格 (商品总价格-满减价格-优惠卷价格)
                        BigDecimal goodsPrice = new BigDecimal(goodsAmt.toString());
                        goodsPrice = goodsPrice.subtract(reduceSumPrice).subtract(couponSumPrice);
                        //订单总价，订单总价已经减去满减和优惠卷
                        orderReduceAmt = orderReduceAmt.add(goodsPrice);
                        //会员特惠价格 折扣*(商品总价格-满减价格-优惠卷价格)+运费价格
                        //gradeRate = gradeRate.multiply(goodsPrice).add(freightAmt);
                        //会员优惠价格应该不需要添加运费，上面为老代码
                        gradeRateAmount = gradeRate.multiply(goodsPrice);
                    }
                    order.put("list", orderDetailInfoList);
                    order.put("haveExpress", haveExpress);
                    order.put("reduce_money", reduceSumPrice);
                    order.put("coupon_money", couponSumPrice);
                    //订单总价
                    order.put("z_price", orderAmt);
                    order.put("spz_price", goodsAmt);
                    order.put("z_freight", freightAmt);
                    //会员优惠的折扣率，应该在0-1的区间中
                    order.put("grade_rate", gradeRate);
                    //店铺优惠
                    order.put("coupon_price", mchCouponPrice);
                    order.put("cancel_method", cancel_method);

                    //该店铺的平台优惠金额
                    order.put("preferential_amount", preferentialAmount);
                    if (orderno.contains(DictionaryConst.OrdersType.ORDERS_HEADER_VI) || orderno.contains(DictionaryConst.OrdersType.ORDERS_HEADER_GM))
                    {
                        order.put("preferential_amount", platform_discount);
                        order.put("coupon_price", store_discount);
                        //新拆单逻辑的订单总价为减去优惠的商品价格的总和
                        if (goodsAmt.subtract(platform_discount).subtract(store_discount).compareTo(BigDecimal.ZERO) < 0)
                        {
                            order.put("old_total", BigDecimal.ZERO.add(freightAmt));
                        }
                        else
                        {
                            order.put("old_total", goodsAmt.subtract(platform_discount).subtract(store_discount).add(freightAmt));
                        }
                    }
                    //会员优惠金额
                    order.put("grade_rate_amount", gradeRateAmount);
                    order.put("remarks", remarks);
                    order.put("sale_type", saleType);
                    order.put("isLogistics", isLogistics);
                    resultMap.put("list", order);
                }
            }
            else
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DDBCZ, "订单不存在", "mchOrderDetails");
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取订单明细 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "mchOrderDetails");
        }
        return resultMap;
    }

    @Override
    public void updateOrderState(int storeId, String orderno,Integer type) throws LaiKeAPIException
    {
        try
        {
            //获取主表状态
            OrderModel orderModel = new OrderModel();
            orderModel.setsNo(orderno);
            orderModel = orderModelMapper.selectOne(orderModel);
            Integer    returnScore = 0;
            BigDecimal returnMoney = BigDecimal.ZERO;
            if (orderModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DDBCZ, "订单不存在", "updateOrderState");
            }
            //获取订单明细
            OrderDetailsModel orderDetailsModel = new OrderDetailsModel();
            orderDetailsModel.setStore_id(storeId);
            orderDetailsModel.setR_sNo(orderno);
            List<OrderDetailsModel> orderDetailsModelList = orderDetailsModelMapper.select(orderDetailsModel);
            if (orderDetailsModelList == null || orderDetailsModelList.size() <= 0)
            {
                logger.error("订单是否多店铺已经拆单");
                orderDetailsModelList = new ArrayList<>();
                //订单是否多店铺已经拆单
                OrderModel order = new OrderModel();
                order.setStore_id(storeId);
                order.setP_sNo(orderno);
                List<OrderModel> orderModelList = orderModelMapper.select(order);
                for (OrderModel model : orderModelList)
                {
                    orderDetailsModel.setR_sNo(model.getsNo());
                    orderDetailsModelList.addAll(orderDetailsModelMapper.select(orderDetailsModel));
                }
            }
            //总单中各详单的全部状态
            List<Integer> orderStateList = new ArrayList<>();
            for (OrderDetailsModel orderDetails : orderDetailsModelList)
            {
                if (Objects.nonNull(orderDetails.getScoreDeduction()))
                {
                    returnScore += orderDetails.getScoreDeduction();
                }
                if (Objects.nonNull(orderDetails.getActualTotal()))
                {
                    returnMoney = returnMoney.add(orderDetails.getActualTotal());
                }
                orderStateList.add(orderDetails.getR_status());
            }
            if (Objects.nonNull(type) && type == 2)
            {
                returnMoney = BigDecimal.ZERO;
            }
            Integer[] orderStateSortList = new Integer[orderStateList.size()];
            orderStateList.toArray(orderStateSortList);
            Arrays.sort(orderStateSortList);
            int rStatus = orderStateSortList[0];
            //判断订单商品是否都是同一个状态 最小状态为最终状态
            if (orderModel.getStatus().equals(rStatus))
            {
                logger.debug("订单明细状态和订单总状态都处于统一状态,无需修改");
            }
            else if (orderModel.getOtype().equals(DictionaryConst.OrdersType.ORDERS_HEADER_VI) && orderStateList.contains(ORDERS_R_STATUS_TOBEVERIFIED))
            {
                //虚拟商品特殊处理，此时为待核销状态，值为8
                //如果详单中还有为待核销状态的就不改变总单的状态
                logger.debug("订单明细状态中还有待核销的订单,无需修改");
            }
            else if (orderModel.getOtype().equals(DictionaryConst.OrdersType.ORDERS_HEADER_VI) && orderStateList.contains(ORDERS_R_STATUS_COMPLETE))
            {
                //修订主表状态
                OrderModel updateorder = new OrderModel();
                updateorder.setId(orderModel.getId());
                updateorder.setStatus(ORDERS_R_STATUS_COMPLETE);
                //如果都是关闭状态,则标记订单已经结算
                updateorder.setSettlement_status(OrderDetailsModel.SETTLEMENT_TYPE_SETTLED);
                //结算时间
                updateorder.setArrive_time(new Date());
                int count = orderModelMapper.updateByPrimaryKeySelective(updateorder);
                if (count < 1)
                {
                    logger.error("修订订单主表状态失败 参数:" + JSON.toJSONString(updateorder));
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLGZTKSB, "网络故障,退款失败", "updateOrderState");
                }
                else
                {
                    logger.debug("修订订单主表状态成功 参数:" + JSON.toJSONString(updateorder));
                }
            }
            else
            {
                //修订主表状态
                OrderModel updateOrder = new OrderModel();
                updateOrder.setId(orderModel.getId());
                updateOrder.setStatus(rStatus);

                int count = orderModelMapper.updateByPrimaryKeySelective(updateOrder);
                if (count < 1)
                {
                    logger.error("修订订单主表状态失败 参数:" + JSON.toJSONString(updateOrder));
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLGZTKSB, "网络故障,退款失败", "updateOrderState");
                }
                else
                {
                    logger.debug("修订订单主表状态成功 参数:" + JSON.toJSONString(updateOrder));
                }

                //如果都是关闭状态,则标记订单已经结算 todo

                if (rStatus == ORDERS_R_STATUS_CLOSE)
                {
                    //用完删除
                    String     returnSupplierFlagKey = GloabConst.RedisHeaderKey.REFUND_ORDER_KEY + storeId + orderno;
                    OrderModel beSettlementOrderInfo = orderModelMapper.selectOne(updateOrder);
                    //获取订单剩余金额,整个订单退款后，剩余的金额
                    BigDecimal zMoney          = beSettlementOrderInfo.getZ_price();
                    Object     isSupplier      = redisUtil.get(returnSupplierFlagKey);
                    boolean    isSupplierOrder = false;
                    if (!Objects.isNull(isSupplier))
                    {
                        isSupplierOrder = (boolean) isSupplier;
                    }
                    //极速退款，如果使用了积分抵扣，不扣除店铺余额
                    if (returnMoney.compareTo(BigDecimal.ZERO) > 0)
                    {
                        zMoney = zMoney.add(returnMoney);
                    }
                    if (zMoney.compareTo(BigDecimal.ZERO) > 0 && !isSupplierOrder)
                    {
                        redisUtil.del(returnSupplierFlagKey);
                        String mchId = StringUtils.trim(beSettlementOrderInfo.getMch_id(), SplitUtils.DH);
                        if (StringUtils.isEmpty(mchId))
                        {
                            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SJCW, "数据错误");
                        }
                        count = mchModelMapper.settlementMch(storeId, Integer.parseInt(mchId), zMoney);
                        if (count < 0)
                        {
                            logger.error("修订订单主表状态失败 参数:" + JSON.toJSONString(updateOrder));
                            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLGZTKSB, "网络故障,退款失败", "updateOrderState");
                        }
                    }
                    //更新订单结算状态
                    beSettlementOrderInfo.setSettlement_status(OrderDetailsModel.SETTLEMENT_TYPE_SETTLED);
                    //结算时间
                    beSettlementOrderInfo.setArrive_time(new Date());
                    //退款导致交易关闭
                    beSettlementOrderInfo.setCancel_method(DictionaryConst.CancelMethod.REFUND);
                    count = orderModelMapper.updateByPrimaryKeySelective(beSettlementOrderInfo);
                    if (count < 0)
                    {
                        logger.error("修订订单主表状态失败 参数:" + JSON.toJSONString(beSettlementOrderInfo));
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLGZTKSB, "网络故障,退款失败", "updateOrderState");
                    }
                    //返还积分
                    if (returnScore > 0)
                    {
                        logger.info("积分组合支付退款流程***************************");
                        User user = new User();
                        user.setUser_id(beSettlementOrderInfo.getUser_id());
                        user = userMapper.selectOne(user);
                        if (Objects.isNull(user))
                        {
                            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YHBCZ, "用户不存在");
                        }
                        userMapper.updateUserScore(beSettlementOrderInfo.getScoreDeduction(), storeId, user.getUser_id());
                        AddScoreVo addScoreVo = new AddScoreVo();
                        addScoreVo.setType(SignRecordModel.ScoreType.INTEGRAL_REFUND_RETURN);
                        addScoreVo.setOrderNo(orderno);
                        addScoreVo.setUserId(user.getUser_id());
                        addScoreVo.setStoreId(storeId);
                        addScoreVo.setScoreOld(user.getScore());
                        addScoreVo.setScore(returnScore);
                        addScoreVo.setEvent("退还积分");
                        publicIntegralService.addScore(addScoreVo);
                    }
                }
            }
        }
        catch (Exception e)
        {
            logger.error("修改订单状态 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "updateOrderState");
        }
    }

    @Override
    public Map<String, Object> orderList(OrderVo orderVo, User user) throws LaiKeAPIException
    {
        try
        {
            Map retMap = Maps.newHashMap();
            int storeId = orderVo.getStoreId();

            String keyword = orderVo.getKeyword();
            if (StringUtils.isEmpty(keyword))
            {
                keyword = orderVo.getOrdervalue();
            }
            String queryType = orderVo.getQueryOrderType();
            //虚拟商品特殊处理,将待收货改成待核销
            if (orderVo.getType().equals(DictionaryConst.OrdersType.ORDERS_HEADER_VI) && queryType.equals("receipt"))
            {
                queryType = "write";
            }
            //订单失效时间
            int     orderFailure = orderVo.getOrderFailure();
            String  company      = orderVo.getCompany();
            String  userId       = user.getUser_id();
            Integer mchIdMain    = customerModelMapper.getStoreMchId(storeId);
            //是否为总店
            boolean isMain = mchIdMain.equals(user.getMchId());
            //是否为分销商 1=true 0=false
            int isDistribution = 0;
            //获取会员分销信息
            UserDistributionModel userDistributionModel = new UserDistributionModel();
            userDistributionModel.setStore_id(user.getStore_id());
            userDistributionModel.setUser_id(user.getUser_id());
            userDistributionModel = userDistributionModelMapper.selectOne1(userDistributionModel);
            if (userDistributionModel != null)
            {
                if (isMain || userDistributionModel.getLevel() > 0)
                {
                    isDistribution = 1;
                }
            }
            //订单类型
            List<String> orderTypeList = orderVo.getOrderTypeList();
            // 订单状态 0待付款 1已付款/待发货 2待收货[3待评价] 5待评价
            int[] orderStatus = {ORDERS_R_STATUS_UNPAID, ORDERS_R_STATUS_CONSIGNMENT, ORDERS_R_STATUS_DISPATCHED, 3};
            // 角标 小红点
            Map<String, Object> resOrderMap = new HashMap<>();
            Map<String, Object> param       = new HashMap<>();
            param.put("storeId", storeId);
            param.put("userId", userId);
            for (int status : orderStatus)
            {
                param.put("status", status);
                switch (status)
                {
                    case 3:
                        //待评价
                        Map<String, Object> parmaMap1 = new HashMap<>(16);
                        parmaMap1.put("store_id", orderVo.getStoreId());
                        parmaMap1.put("user_id", user.getUser_id());
                        parmaMap1.put("typeList", orderTypeList);
                        //待评价
                        int orderNum = orderModelMapper.countNotCommentNum(parmaMap1);
                        resOrderMap.put("5", orderNum);
                        break;
                    case 4:
                        //退款售后
                        List<Integer> typeList = new ArrayList<>();
                        typeList.add(DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_EXAMEWAIT_STATUS);
                        typeList.add(DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_AGREE_REBACK);
                        typeList.add(DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_USER_DELIVERED);
                        Map<String, Object> parmaMap = new HashMap<>(16);
                        parmaMap.put("store_id", orderVo.getStoreId());
                        parmaMap.put("user_id", user.getUser_id());
                        parmaMap.put("r_typeList", typeList);
                        parmaMap.put("orderTypes", orderTypeList);
                        int number = returnOrderModelMapper.countRturnOrderNumDynamic(parmaMap);
                        resOrderMap.put("th", number);
                        break;
                    default:
                        //更具状态获取订单数量
                        Map<String, Object> parmaMap2 = new HashMap<>(16);
                        parmaMap2.put("store_id", orderVo.getStoreId());
                        parmaMap2.put("status", status);
                        parmaMap2.put("userId", user.getUser_id());
                        parmaMap2.put("orderTypeList", orderTypeList);
                        orderNum = orderModelMapper.countDynamic(parmaMap2);
                        resOrderMap.put(status + "", orderNum);
                        break;
                }
            }
            param.remove("status");
            param.put("keyWord", keyword);
            param.put("orderType", queryType);
            if (StringUtils.isNotEmpty(orderVo.getOrderType()))
            {
                param.put("otype", orderVo.getOrderType());
            }
            else if (StringUtils.isNotEmpty(keyword))
            {
                //展示所有订单
                param.put("otype", null);
            }
            else
            {
                //我的订单只显示普通订单
                param.put("otypeList", orderTypeList);
            }
            if (StringUtils.isNotEmpty(orderVo.getCondition()))
            {
                param.put("preSellCondition", orderVo.getCondition());
            }
            //不需要展示的商品类型
            if (orderVo.getoTypeNotIn() != null)
            {
                param.put("oTypeNotIn", DictionaryConst.OrdersType.ORDERS_HEADER_VI);
            }
            //禅道49609 -》用户、商家、平台删除自己的订单
            param.put("userRecycle", OrderModel.SHOW);
            param.put("start", orderVo.getPageNo());
            param.put("pageSize", orderVo.getPageSize());
            int orderListCount = orderModelMapper.getUserCenterOrderListCount(param);

            //如果没有订单记录则跳过其他的方法
            if (orderListCount == 0)
            {
                retMap = Maps.newHashMap();
                retMap.put("order", new ArrayList<>());
                retMap.put("order_failure", orderFailure);
                retMap.put("company", company);
                retMap.put("order_num", orderListCount);
                retMap.put("res_order", resOrderMap);
                retMap.put("isDistribution", isDistribution);
                return retMap;
            }
            //是否有商品已经发货，显示查看物流按钮
            boolean                   haveExpress = false;
            List<Map<String, Object>> dbOrderList = orderModelMapper.getUserCenterOrderList(param);
            List<Map<String, Object>> orderList   = new ArrayList<>();
            if (!CollectionUtils.isEmpty(dbOrderList))
            {
                for (Map<String, Object> orderInfo : dbOrderList)
                {
                    haveExpress = false;
                    //可开发票过期提醒
                    boolean isInvoice = false;
                    //是否超时
                    boolean invoiceTimeout = false;
                    //订单id
                    Integer orderId = MapUtils.getInteger(orderInfo, "id");
                    // 订单号
                    String sNo = DataUtils.getStringVal(orderInfo, "sNo");
                    //支付方式
                    String pay_type = DataUtils.getStringVal(orderInfo, "pay_type");
                    // 店主ID
                    String mchId = DataUtils.getStringVal(orderInfo, "mch_id");
                    // 是否自提
                    Integer selfLifting = MapUtils.getInteger(orderInfo, "self_lifting");
                    //是否为会员订单
                    Integer vipSource = MapUtils.getInteger(orderInfo, "vip_source");
                    orderInfo.put("vipGoods",Objects.nonNull(vipSource) && vipSource == 1);
                    //订单类型
                    String oType = MapUtils.getString(orderInfo, "otype");

                    //凭证审核状态
                    Integer review_status = MapUtils.getInteger(orderInfo, "review_status");
                    //订单状态
                    Integer status = MapUtils.getInteger(orderInfo, "status");
                    //到货时间
                    Date arriveTime = DateUtil.dateFormateToDate(MapUtils.getString(orderInfo, "arrive_time"), GloabConst.TimePattern.YMDHMS);
                    //付款时间
                    Date payTime = DateUtil.dateFormateToDate(MapUtils.getString(orderInfo, "pay_time"), GloabConst.TimePattern.YMDHMS);
                    //结算状态
                    Integer settlementStatus = MapUtils.getInteger(orderInfo, "settlement_status");
                    //店铺信息
                    String   mchid    = StringUtils.trim(mchId, SplitUtils.DH);
                    MchModel mchModel = mchModelMapper.getMchInfo(mchid, storeId);
                    if (!Objects.isNull(arriveTime) && settlementStatus.equals(DictionaryConst.WhetherMaven.WHETHER_OK) && status.equals(ORDERS_R_STATUS_COMPLETE) && mchModel.getIs_invoice().equals(DictionaryConst.WhetherMaven.WHETHER_OK))
                    {
                        isInvoice = true;
                        if (DateUtil.dateCompare(new Date(), DateUtil.getAddDate(arriveTime, 20)))
                        {
                            invoiceTimeout = true;
                        }
                    }
                    //虚拟商品
                    if (oType.equals(DictionaryConst.OrdersType.ORDERS_HEADER_VI))
                    {
                        Integer write_off_settings = MapUtils.getInteger(orderInfo, "write_off_settings");
                        if (write_off_settings != null && write_off_settings.equals(ProductListModel.WRITE_OFF_SETTINGS.offline))
                        {
                            //需要线下核销
                            orderInfo.put("write_off_settings", 1);
                        }
                        else
                        {
                            orderInfo.put("write_off_settings", 2);
                        }
                        //0:其他正常关闭   1：待付款关闭订单
                        Integer isDfkGbi = 0;
                        if (payTime == null && status == ORDERS_R_STATUS_CLOSE)
                        {
                            isDfkGbi = 1;
                        }
                        orderInfo.put("isDfkGbi", isDfkGbi);
                    }
                    //发票信息
                    orderInfo.put("isInvoice", isInvoice);
                    orderInfo.put("invoiceTimeout", invoiceTimeout);
                    BigDecimal subtract = new BigDecimal(MapUtils.getString(orderInfo, "z_price")).subtract(new BigDecimal(MapUtils.getString(orderInfo, "z_freight")));
                    orderInfo.put("invoicePrice", subtract);
                    //判断是否为多店铺订单hgindex
                    String[] mchIdsArr = mchId.split(SplitUtils.DH);
                    if (mchIdsArr.length > 2)
                    {
                        //是多店铺订单
                        orderInfo.put("ismch", true);
                    }
                    else
                    {
                        //不是多店铺订单
                        orderInfo.put("ismch", false);
                    }
                    orderInfo.put("shop_id", 0);
                    orderInfo.put("shop_name", "");
                    orderInfo.put("shop_logo", "");
                    if (StringUtils.isNotEmpty(mchId))
                    {
                        if (mchModel != null)
                        {
                            orderInfo.put("shop_id", mchModel.getId());
                            orderInfo.put("shop_name", mchModel.getName());
                            orderInfo.put("shop_logo", publiceService.getImgPath(mchModel.getLogo(), storeId));
                            orderInfo.put("shopHead_img", publiceService.getImgPath(mchModel.getHead_img(), storeId));
                            orderInfo.put("shop_is_lock", mchModel.getIs_lock());
                            //获取订单配置信息
                            Map<String, Object> configMap = publicOrderService.getOrderConfig(storeId, mchModel.getId(), oType.toUpperCase());
                            if (MapUtils.isNotEmpty(configMap))
                            {
                                orderFailure = MapUtils.getInteger(configMap, "orderFailureDay");
                            }
                        }
                    }
                    // 根据订单号,查询订单详情
                    OrderDetailsModel orderDetailsModel = new OrderDetailsModel();
                    orderDetailsModel.setStore_id(storeId);
                    orderDetailsModel.setR_sNo(sNo);
                    List<OrderDetailsModel> orderDetailsModelList = orderDetailsModelMapper.select(orderDetailsModel);
                    orderInfo.put("list", orderDetailsModelList);
                    BigDecimal          totalFreight = BigDecimal.ZERO;
                    Map<String, Object> detailsInfo  = null;
                    JSONObject          jsonObject   = null;
                    JSONArray           jsonArray    = new JSONArray();
                    for (OrderDetailsModel orderDetails : orderDetailsModelList)
                    {
                        //商家自配的订单不需要展示物流信息
                        if (orderDetails.getDeliverNum() != null && orderDetails.getDeliverNum() > 0 && !"2".equals(DataUtils.getStringVal(orderInfo, "self_lifting")))
                        {
                            haveExpress = true;
                        }
                        jsonObject = JSONObject.from(orderDetails);
                        detailsInfo = new HashMap<>();
                        BigDecimal freight = orderDetails.getFreight();
                        totalFreight = BigDecimal.valueOf(DoubleFormatUtil.format(totalFreight.doubleValue() + freight.doubleValue()));
                        Integer re_type = orderDetails.getR_type();
                        if (re_type != null && re_type > 0)
                        {
                            detailsInfo.put("r_status", orderDetails.getR_status());
                            detailsInfo.put("p_id", orderDetails.getP_id());
                            detailsInfo.put("order_id", orderDetails.getId());
                            detailsInfo.put("id", orderDetails.getId());
                            detailsInfo.put("isreturn", 1);
                            int r_type = orderDetails.getR_type();
                            if (r_type == 0)
                            {
                                detailsInfo.put("prompt", "审核中");
                                detailsInfo.put("buyer", "");
                                detailsInfo.put("return_state", "");
                            }
                            else if (r_type == 1)
                            {
                                ServiceAddressModel serviceAddressModel = new ServiceAddressModel();
                                serviceAddressModel.setStore_id(storeId);
                                serviceAddressModel.setUid("admin");
                                serviceAddressModel.setType(ServiceAddressModel.TYPE_DELIVER_GOODS);
                                serviceAddressModel.setIs_default(DictionaryConst.DefaultMaven.DEFAULT_OK);
                                serviceAddressModel = serviceAddressModelMapper.selectOne(serviceAddressModel);
                                Map $buyer = new HashMap();
                                $buyer.put("tel", serviceAddressModel.getTel());
                                $buyer.put("name", serviceAddressModel.getName());
                                $buyer.put("address_xq", serviceAddressModel.getAddress_xq());
                                detailsInfo.put("prompt", "审核通过");
                                detailsInfo.put("buyer", $buyer);
                                detailsInfo.put("return_state", "");
                            }
                            else if (r_type == 2)
                            {
                                detailsInfo.put("prompt", "审核拒绝");
                                detailsInfo.put("buyer", "");
                                detailsInfo.put("return_state", "");
                            }
                            else if (r_type == 3)
                            {
                                detailsInfo.put("prompt", "审核通过");
                                detailsInfo.put("buyer", "");
                                detailsInfo.put("return_state", "");
                                if (re_type == 3)
                                {
                                    detailsInfo.put("prompt", "商品审核中");
                                }
                            }
                            else if (r_type == 4)
                            {
                                detailsInfo.put("prompt", "审核通过");
                                detailsInfo.put("buyer", "");
                                detailsInfo.put("return_state", "退货退款");
                            }
                            else if (r_type == 5)
                            {
                                detailsInfo.put("prompt", "审核拒绝");
                                detailsInfo.put("buyer", "");
                                detailsInfo.put("return_state", "");
                            }
                            else if (r_type == 8)
                            {
                                detailsInfo.put("prompt", "审核拒绝");
                                detailsInfo.put("buyer", "");
                                detailsInfo.put("return_state", "");
                            }
                            else if (r_type == 9)
                            {
                                detailsInfo.put("prompt", "审核通过");
                                detailsInfo.put("buyer", "");
                                detailsInfo.put("return_state", "");
                            }
                            else if (r_type == 11)
                            {
                                detailsInfo.put("prompt", "退换中");
                                detailsInfo.put("buyer", "");
                                detailsInfo.put("return_state", "");
                            }
                            else if (r_type == 12)
                            {
                                detailsInfo.put("prompt", "售后完成");
                                detailsInfo.put("buyer", "");
                                detailsInfo.put("return_state", "");
                            }
                            jsonObject.put("return", detailsInfo);
                            jsonArray.add(jsonObject);
                        }
                    }
                    List<OrderDetailsModel> tmpList = (List<OrderDetailsModel>) orderInfo.get("list");
                    if (tmpList.size() > 0)
                    {
                        orderInfo.put("r_type", tmpList.get(0).getR_type());
                    }
                    else
                    {
                        orderInfo.put("r_type", "100");
                    }
                    orderInfo.put("z_freight", totalFreight);
                    int orderDetailNum = orderDetailsModelMapper.getOrderDetailNum(storeId, sNo);
                    orderInfo.put("sum", orderDetailNum);
                    int    allow    = 0;
                    Object allowObj = orderInfo.get("allow");
                    if (allowObj != null)
                    {
                        allow = (int) orderInfo.get("allow");
                    }
                    List<JSONObject> templist = new ArrayList<>();
                    for (OrderDetailsModel orderDetailsModel1 : tmpList)
                    {
                        jsonObject = JSONObject.from(orderDetailsModel1);
                        jsonObject.put("integral", DoubleFormatUtil.format(allow / orderDetailNum));
                        templist.add(jsonObject);
                    }
                    orderInfo.put("list", templist);
                    List<Map<String, Object>> product = new ArrayList<>();
                    //用于pc商城评价按钮判断
                    int                 commentsType        = 2;
                    boolean             comment             = false;
                    boolean             canBuy              = true;
                    Map<String, Object> orderDetailValueMap = new HashMap<>();
                    if (!CollectionUtils.isEmpty(templist))
                    {
                        boolean userCanAfter = true;
                        for (JSONObject orderDetailsJsonObj : templist)
                        {
                            //详情id
                            int                 detailId   = MapUtils.getIntValue(orderDetailsJsonObj, "id");
                            Integer             pId        = MapUtils.getInteger(orderDetailsJsonObj, "p_id");
                            Map<String, Object> configMap  = getOrderConfig(storeId, MapUtils.getInteger(orderDetailsJsonObj, "mch_id"), oType);
                            int                 orderAfter = DateUtil.dateConversion(7, DateUtil.TimeType.DAY);
                            if (configMap != null)
                            {
                                if (StringUtils.isEmpty(orderFailure))
                                {
                                    orderFailure = MapUtils.getIntValue(configMap, "orderFailureDay");
                                }
                                orderFailure = orderFailure / 60 / 60;
                                orderAfter = MapUtils.getIntValue(configMap, "orderAfter");
                            }
                            // 产品规格id
                            int sid      = orderDetailsJsonObj.getInteger("sid");
                            int r_status = orderDetailsJsonObj.getInteger("r_status");
                            Date arrive_time = orderDetailsJsonObj.getDate("arrive_time");
                            Integer mch_id = MapUtils.getInteger(orderDetailsJsonObj, "mch_id");
                            //评价按钮
                            int orderCommentType = this.orderCommentType(storeId, userId, sNo, detailId, sid, r_status);
                            if (arriveTime != null)
                            {
                                //售后截至日期 按秒算
                                Date afterDate = DateUtil.getAddDateBySecond(new Date(), orderAfter);
                                logger.info("售后日期转换***********************{}", DateUtil.dateFormate(afterDate, GloabConst.TimePattern.YMDHMS));
                                if (new Date().compareTo(afterDate) >= 0)
                                {
                                    userCanAfter = false;
                                }
                            }
                            //是否已经退款成功
                            Integer orderReturnSuccessNum = returnOrderModelMapper.orderReturnSuccessNum(storeId, sNo, detailId);
                            if (orderReturnSuccessNum > 0)
                            {
                                orderDetailsJsonObj.put("isReturn", true);
                            }
                            else
                            {
                                orderDetailsJsonObj.put("isReturn", false);
                            }
                            //多商品有一个商品未评价显示立即评价
                            if (orderCommentType == 1)
                            {
                                commentsType = 1;
                                comment = true;
                            }
                            else if (orderCommentType == 2)
                            {
                                comment = true;
                            }
                            orderDetailsJsonObj.put("comments_type", orderCommentType);
                            boolean ismch = (boolean) orderInfo.get("ismch");
                            if (ismch)
                            {
                                MchModel mchModel1 = mchModelMapper.getMchInfoByPid(sid);
                                if (mchModel1 != null)
                                {
                                    orderDetailsJsonObj.put("shop_id", mchModel1.getId());
                                    orderDetailsJsonObj.put("shop_name", mchModel1.getName());
                                    orderDetailsJsonObj.put("shop_logo", publiceService.getImgPath(mchModel1.getLogo(), storeId));
                                }
                            }
                            ConfiGureModel confiGureModel = new ConfiGureModel();
                            confiGureModel.setId(sid);
                            confiGureModel = confiGureModelMapper.selectOne(confiGureModel);
                            if (r_status == 0)
                            {
                                //如果是代付款订单 查询规格价格是否改变
                                if (confiGureModel != null)
                                {
                                    //当前规格设置价格
                                    BigDecimal currentPrice = confiGureModel.getPrice();
                                    BigDecimal orderPrice   = orderDetailsJsonObj.getBigDecimal("p_price");
                                    if (currentPrice.equals(orderPrice))
                                    {
                                        canBuy = false;
                                    }
                                }
                            }
                            String size = orderDetailsJsonObj.getString("size");
                            if (StringUtils.isNotEmpty(size))
                            {
                                orderDetailsJsonObj.put("size", com.laiketui.core.utils.tool.StringUtils.ltrim(size, SplitUtils.FH));
                            }
                            // 属性id
                            orderDetailsJsonObj.put("attribute_id", orderDetailsJsonObj.get("sid"));
                            detailsInfo = orderDetailsJsonObj;
                            // 获取商品规格图片
                            String imgUrl = null;
                            if (confiGureModel != null)
                            {
                                imgUrl = confiGureModel.getImg();
                            }
                            detailsInfo.put("imgurl", publiceService.getImgPath(imgUrl, storeId));
                            Integer orderDetailReturnIsNotEnd = returnOrderModelMapper.orderDetailReturnIsNotEnd(storeId, sNo, detailId);
                            try
                            {
                                publicOrderService.orderAfterSaleExpire(storeId, mch_id, oType, arrive_time);
                            }
                            catch (LaiKeAPIException l)
                            {
                                userCanAfter = false;
                            }
                            Integer goodsNum = returnOrderModelMapper.getOrderReturnGoodsNum(storeId, detailId, sNo);

                            ReturnOrderModel returnOrderInfo = returnOrderModelMapper.getReturnOrderInfo(storeId, detailId, sNo);

                            Map<String, Object> orderDetailShowValueList  = orderDetailShowValue(oType, r_status, orderCommentType, userCanAfter, settlementStatus, orderDetailReturnIsNotEnd, selfLifting, orderReturnSuccessNum,status);
                            if (goodsNum >= 2 || (Objects.nonNull(returnOrderInfo) && (returnOrderInfo.getR_type() == 4 || returnOrderInfo.getR_type() == 9 || returnOrderInfo.getR_type() == 13)) || oType.equals(DictionaryConst.OrdersType.ORDERS_HEADER_FS))
                            {
                                orderDetailShowValueList.remove(OrderShowValueEnum.SQSH.getName());
                            }
                            redisUtil.del("get_order_details_button_" + detailId);
                            redisUtil.set("get_order_details_button_" + detailId, orderDetailShowValueList, 60 * 5);
                            orderDetailValueMap.putAll(orderDetailShowValueList);
                            if (oType.equals(DictionaryConst.OrdersType.ORDERS_HEADER_FS)) {
                                orderDetailShowValueList.remove(OrderShowValueEnum.CKSH.getName());
                                orderDetailShowValueList.remove(OrderShowValueEnum.SQSH.getName());
                            }
                            detailsInfo.put("get_order_details_button", orderDetailShowValueList);

                            product.add(detailsInfo);
                            // 订单详情状态
                            int orderDetailsStatus = orderDetailsJsonObj.getInteger("r_status");
                            // 状态 0:未付款 1:未发货 2:待收货 5:已完成  7:订单关闭
                            int               otherStatusOrderNum  = 0;
                            int               closeOrderNum        = 0;
                            int               allOrderNum          = 0;
                            OrderDetailsModel orderDetailsModelTmp = new OrderDetailsModel();
                            orderDetailsModelTmp.setStore_id(storeId);
                            orderDetailsModelTmp.setR_sNo(sNo);
                            orderDetailsModelTmp.setR_status(orderDetailsStatus);
                            if (orderDetailsStatus != 7)
                            {
                                //其他状态
                                otherStatusOrderNum = orderDetailsModelMapper.selectCount(orderDetailsModelTmp);
                            }
                            else
                            {
                                //已经关闭
                                closeOrderNum = orderDetailsModelMapper.selectCount(orderDetailsModelTmp);
                            }
                            orderDetailsModelTmp.setR_status(null);
                            //所有
                            allOrderNum = orderDetailsModelMapper.selectCount(orderDetailsModelTmp);

                            // 如果订单下面的商品都处在同一状态,那就改订单状态为已完成
                            if ((otherStatusOrderNum + closeOrderNum) == allOrderNum)
                            {
                                //如果订单数量相等 则修改父订单状态
                                orderModelMapper.updateOrderStatus(storeId, sNo, userId, orderDetailsStatus);
                                if (orderDetailsStatus > 0)
                                {
                                    orderInfo.put("status", orderDetailsStatus);
                                }
                            }

                        }
                        //所有商品都已经评价了
                        if (!comment)
                        {
                            commentsType = 3;
                        }
                        orderInfo.put("isOrderDel", this.isOrderDelButtonShow(orderId));
                        orderInfo.put("list", product);
                        orderInfo.put("can_buy", canBuy);
                        orderInfo.put("add_time", DateUtil.dateFormate(MapUtils.getString(orderInfo, "add_time"), GloabConst.TimePattern.YMDHMS));
                        orderInfo.put("comments_type", commentsType);
                    }
                    orderInfo.put("sale_type", 0);
                    int unFinishShouHouOrderNum = returnOrderModelMapper.orderReturnIsNotEnd(storeId, sNo);
                    //判断有无订单售后未结束
                    if (unFinishShouHouOrderNum > 0)
                    {
                        orderInfo.put("sale_type", 1);
                    }
                    //预售订单信息
                    PreSellRecordModel preSellRecordModel = new PreSellRecordModel();
                    preSellRecordModel.setsNo(sNo);
                    Integer payTarget = null;
                    List<PreSellRecordModel> preSellRecordModelList = preSellRecordModelMapper.select(preSellRecordModel);
                    if (!CollectionUtils.isEmpty(preSellRecordModelList))
                    {
                        for (PreSellRecordModel sellRecordModel : preSellRecordModelList)
                        {
                            //支付类型 0.定金 1.尾款
                            if (sellRecordModel.getIs_pay() == 0)
                            {
                                Integer payType = sellRecordModel.getPay_type();

                                if (Objects.isNull(payType))
                                {
                                    payTarget = 3;
                                    break;
                                }else
                                {
                                    payTarget = payType == 0 ? 1 : 2;
                                }
                            }
                        }
                        orderInfo.put("payTarget",payTarget);
                        preSellRecordModel = preSellRecordModelList.get(0);
                        PreSellGoodsModel preSellGoodsModel = new PreSellGoodsModel();
                        preSellGoodsModel.setProduct_id(preSellRecordModel.getProduct_id());
                        preSellGoodsModel = preSellGoodsMapper.selectOne(preSellGoodsModel);
                        orderInfo.put("sellType", preSellGoodsModel.getSell_type());

                        if (preSellGoodsModel.getSell_type().equals(PreSellGoodsModel.DEPOSIT_PATTERN))
                        {
                            orderInfo.put("canPay", false);
                            orderInfo.put("deposit", preSellRecordModel.getDeposit());
                            orderInfo.put("balance", preSellRecordModel.getBalance());
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTime(preSellGoodsModel.getBalance_pay_time());
                            calendar.set(Calendar.HOUR_OF_DAY, 0);
                            calendar.set(Calendar.MINUTE, 0);
                            calendar.set(Calendar.SECOND, 0);
                            Date startTime = calendar.getTime();
                            calendar.add(Calendar.DAY_OF_MONTH, 1);
                            calendar.add(Calendar.SECOND, -1);
                            Date endTime = calendar.getTime();
                            orderInfo.put("startTime", DateUtil.dateFormate(startTime, GloabConst.TimePattern.YMDHMS));
                            orderInfo.put("endTime", DateUtil.dateFormate(endTime, GloabConst.TimePattern.YMDHMS));
                            if (DateUtil.dateCompare(new Date(), startTime) && DateUtil.dateCompare(endTime, new Date()))
                            {
                                orderInfo.put("canPay", true);
                            }
                        }
                        else
                        {
                            orderInfo.put("endTime", DateUtil.dateFormate(preSellGoodsModel.getDeadline(), GloabConst.TimePattern.YMDHMS));
                        }
                        orderInfo.put("deliveryTime", preSellGoodsModel.getDelivery_time());
                    }
                    orderInfo.put("haveExpress", haveExpress);
                    //订单状态按钮显示
                    Map<String, Object> initShowValue    = new HashMap<>();
                    Integer             sellType         = MapUtils.getInteger(orderInfo, "sellType");
                    Boolean             canPay           = MapUtils.getBoolean(orderInfo, "canPay");
                    Integer             writeOffSettings = MapUtils.getInteger(orderInfo, "write_off_settings");
                    OrderModel          orderModel       = new OrderModel();
                    orderModel.setStore_id(storeId);
                    orderModel.setsNo(sNo);
                    int count = orderDetailsModelList.size();
                    orderModel = orderModelMapper.selectOne(orderModel);
                    // 总价
                    BigDecimal zPrice = orderModel.getZ_price();
                    // 订单总价
                    BigDecimal old_total = orderModel.getOld_total();
                    //总运费
                    BigDecimal orderFreight = orderModel.getZ_freight();
                    // 计算发票金额（总价 - 运费）
                    BigDecimal invoicePriceBD = zPrice.subtract(orderFreight);
                    // 如果需要int类型，进行转换（四舍五入并确保非负）
                    int                  invoicePrice  = Math.max(0, invoicePriceBD.setScale(0, RoundingMode.HALF_UP).intValue());
                    Map<String, Boolean> buttonShow    = publicRefundService.afterSaleButtonShow(storeId, orderModel.getOtype(), orderDetailsModelList, orderModel, unFinishShouHouOrderNum);
                    Boolean              refund        = MapUtils.getBoolean(buttonShow, "refund");
                    Boolean              refundShowBtn = MapUtils.getBoolean(buttonShow, "refundShowBtn");
                    initShowValue = orderShowValue(unFinishShouHouOrderNum, status, haveExpress, commentsType, selfLifting, oType, sellType, invoiceTimeout,
                            refund, refundShowBtn, isInvoice, canPay, count, writeOffSettings, invoicePrice,pay_type,review_status);
                    String keys = String.format(GloabConst.RedisHeaderKey.ORDER_SHOW_VALUE_KEY, sNo);
                    int cksh = 0;
                    for (Map.Entry<String, Object> entry : orderDetailValueMap.entrySet())
                    {
                        String key   = entry.getKey();
                        Object value = entry.getValue();
                        if (key.contains(OrderShowValueEnum.SQSH.getName()) && Objects.equals(OrderShowValueEnum.SQSH.getShowValue(), value))
                        {
                            initShowValue.put(key, value);
                        }else if (key.contains(OrderShowValueEnum.CKSH.getName()) && Objects.equals(OrderShowValueEnum.CKSH.getShowValue(), value))
                        {
                            cksh++;
                        }
                    }
                    if ((!orderDetailValueMap.isEmpty() && cksh == orderDetailValueMap.size()) || initShowValue.containsKey(OrderShowValueEnum.JSTK.getName()))
                    {
                        initShowValue.remove(OrderShowValueEnum.SQSH.getName());
                    }
                    orderInfo.put("get_button_list", initShowValue);
                    redisUtil.del(keys);
                    redisUtil.set(keys, initShowValue, 5 * 60);
                    orderList.add(orderInfo);
                }
            }


            retMap.put("order", orderList);
            retMap.put("order_failure", orderFailure);
            retMap.put("company", company);
            retMap.put("order_num", orderListCount);
            retMap.put("res_order", resOrderMap);
            retMap.put("isDistribution", isDistribution);
            //存缓存
//            redisUtil.set("orderTest"+storeId , retMap);
            return retMap;
        }
        catch (LaiKeAPIException e)
        {
            logger.error("订单列表获取失败 ", e);
            throw e;
        }
        catch (Exception e)
        {
            logger.error("订单列表获取 异常 ", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DDLBHQSB, "订单列表获取失败", "orderList");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deliverySave(MainVo vo, Integer exId, String exNo, String orderDetailIds) throws LaiKeAPIException
    {
        try
        {
            // 请选择快递公司
            if (exId == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QXZKDGS, "请选择快递公司");
            }
            if (org.apache.commons.lang3.StringUtils.isEmpty(exNo))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QSRKDDH, "请输入快递单号");
            }
            OrderDetailsModel orderDetailsModel = new OrderDetailsModel();
            orderDetailsModel.setExpress_id(exId.toString());
            orderDetailsModel.setCourier_num(exNo);
            int count = orderDetailsModelMapper.selectCount(orderDetailsModel);
            if (count > 0)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_KDDHYCZ, "快递单号已存在");
            }
            publicOrderService.pluginsAdminDelivery(vo, exId, exNo, orderDetailIds);
        }
        catch (LaiKeAPIException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            logger.error("订单发货 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "orderPrint");
        }

    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveEditOrder(EditOrderVo orderVo) throws LaiKeAPIException
    {
        try
        {
            int row;
            publiceService.getRedisUserCache(orderVo);
            if (org.apache.commons.lang3.StringUtils.isEmpty(orderVo.getOrderNo()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DDBJSB, "订单编辑失败");
            }
            //获取订单信息
            OrderModel orderModel = new OrderModel();
            orderModel.setStore_id(orderVo.getStoreId());
            orderModel.setsNo(orderVo.getOrderNo());
            orderModel = orderModelMapper.selectOne(orderModel);
            if (orderModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DDBCZ, "订单不存在");
            }
            if (DictionaryConst.OrdersStatus.ORDERS_R_STATUS_CONSIGNMENT != orderModel.getStatus()
                    && DictionaryConst.OrdersStatus.ORDERS_R_STATUS_UNPAID != orderModel.getStatus())
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DDYFHBNJXXG, "订单已发货,不能进行修改");
            }
            //是否是跨店铺
            String[] mchList   = StringUtils.trim(orderModel.getMch_id(), com.laiketui.core.common.SplitUtils.DH).split(com.laiketui.core.common.SplitUtils.DH);
            boolean  isManyMch = mchList.length > 1;

            OrderModel orderUpdate = new OrderModel();
            orderUpdate.setId(orderModel.getId());
            orderUpdate.setMobile(orderVo.getTel());
            orderUpdate.setSheng(orderVo.getShen());
            orderUpdate.setShi(orderVo.getShi());
            orderUpdate.setXian(orderVo.getXian());
            orderUpdate.setName(orderVo.getUserName());
            orderUpdate.setAddress(orderVo.getAddress());
            if (!isManyMch && StringUtils.isNotEmpty(orderVo.getRemarks()))
            {
                Map<String, String> remarksMap = new HashMap<>(1);
                remarksMap.put(mchList[0], orderVo.getRemarks());
                orderUpdate.setRemarks(JSON.toJSONString(remarksMap));
            }
            //修改订单信息
            if (DictionaryConst.OrdersStatus.ORDERS_R_STATUS_UNPAID == orderModel.getStatus())
            {
                //订单状态（只能修改成待发货）
                if (DictionaryConst.OrdersStatus.ORDERS_R_STATUS_CONSIGNMENT == orderVo.getOrderStatus())
                {
                    orderUpdate.setStatus(DictionaryConst.OrdersStatus.ORDERS_R_STATUS_CONSIGNMENT);
                    //修改订单明细状态
                    row = orderDetailsModelMapper.updateOrderDetailsStatus(orderVo.getStoreId(), orderVo.getOrderNo(), orderUpdate.getStatus());
                    if (row < 1)
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_XGSB, "修改失败");
                    }
                }
                //未付款可以修改订单金额、订单状态（只能修改成待发货）
                if (org.apache.commons.lang3.StringUtils.isNotEmpty(orderVo.getOrderAmt()))
                {
                    orderUpdate.setZ_price(new BigDecimal(orderVo.getOrderAmt()));
                    //修改订单金额后需要记录差额
                    BigDecimal        priceDiff         = orderModel.getZ_price().subtract(orderUpdate.getZ_price());
                    OrderDetailsModel orderDetailsModel = new OrderDetailsModel();
                    orderDetailsModel.setStore_id(orderVo.getStoreId());
                    orderDetailsModel.setR_sNo(orderVo.getOrderNo());
                    List<OrderDetailsModel> orderDetailsModelList = orderDetailsModelMapper.select(orderDetailsModel);
                    for (OrderDetailsModel orderDetails : orderDetailsModelList)
                    {
                        //支付金额=优惠金额+运费
                        BigDecimal payMoney = orderDetails.getAfter_discount().add(orderDetails.getFreight());
                        //该单均摊差额 (优惠后金额+运费)/订单总金额*订单差额
                        BigDecimal updatePrice = payMoney.divide(orderModel.getZ_price(), 12, BigDecimal.ROUND_DOWN).multiply(priceDiff);
                        //重新计算优惠后金额 优惠后金额 - (优惠后金额/(优惠后金额+运费))*该单均摊金额
                        BigDecimal moneyTemp     = orderDetails.getAfter_discount().divide(payMoney, 12, BigDecimal.ROUND_DOWN).multiply(updatePrice);
                        BigDecimal afterDiscount = orderDetails.getAfter_discount().subtract(moneyTemp).setScale(2, BigDecimal.ROUND_HALF_UP);
                        //重新计算运费金额 运费金额 - (运费金额/(优惠后金额+运费))*该单均摊金额
                        BigDecimal yunFeiPrice = orderDetails.getFreight().subtract(orderDetails.getFreight().divide(payMoney, 12, BigDecimal.ROUND_DOWN).multiply(updatePrice)).setScale(2, BigDecimal.ROUND_HALF_UP);

                        //修改订单明细金额
                        OrderDetailsModel orderDetailsUpdate = new OrderDetailsModel();
                        orderDetailsUpdate.setId(orderDetails.getId());
                        orderDetailsUpdate.setFreight(yunFeiPrice);
                        orderDetailsUpdate.setAfter_discount(afterDiscount);
                        row = orderDetailsModelMapper.updateByPrimaryKeySelective(orderDetailsUpdate);
                        if (row < 1)
                        {
                            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DDBJSB, "订单编辑失败");
                        }
                    }
                }
            }
            //修改订单基本信息
            row = orderModelMapper.updateByPrimaryKeySelective(orderUpdate);
            if (row < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_XGSB, "修改失败");
            }

            publiceService.addAdminRecord(orderVo.getStoreId(), "修改了订单ID：" + orderVo.getOrderNo() + " 的信息", AdminRecordModel.Type.UPDATE, orderVo.getAccessId());
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("订单编辑失败", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DDBJSB, "订单编辑失败", "saveEditOrder");
        }
    }


    @Override
    public Map<String, Object> deliveryView(com.laiketui.domain.vo.order.AdminDeliveryVo adminDeliveryVo) throws LaiKeAPIException
    {
        Map<String, Object> retMap = Maps.newHashMap();
        try
        {
            int                       storeId    = adminDeliveryVo.getStoreId();
            String                    sNo        = adminDeliveryVo.getsNo();
            List<Map<String, Object>> ordersList = orderModelMapper.getDeleiveryOrders(storeId, sNo);
            // todo 未知变量
            int                       put             = 1;
            List<Map<String, Object>> returnGoodsList = new ArrayList<>();
            for (Map<String, Object> orderInfo : ordersList)
            {
                orderInfo.put("imgurl", publiceService.getImgPath(MapUtils.getString(orderInfo, "imgurl"), storeId));
                int rstatus = MapUtils.getIntValue(orderInfo, "r_status");
                if (rstatus == 1)
                {
                    put = 0;
                }

                returnGoodsList.add(orderInfo);
            }
            retMap.put("express", searchExpress(null));
            retMap.put("id", sNo);
            retMap.put("put", put);
            retMap.put("goods", returnGoodsList);
            retMap.put("count", returnGoodsList.size());
            return retMap;
        }
        catch (LaiKeAPIException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            logger.error("获取发货信息失败", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "deliveryView");
        }
    }

    @Override
    public Map<String, Object> searchExpress(String express) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            Map<String, Object> paramMap = new HashMap<>(16);
            paramMap.put("is_open", 1);
            paramMap.put("sort_sort", DataUtils.Sort.DESC.toString());
            if (org.apache.commons.lang3.StringUtils.isNotEmpty(express))
            {
                paramMap.put("kuaidi_name", express);
            }

            int                       total            = expressModelMapper.countDynamic(paramMap);
            List<Map<String, Object>> expressModelList = new ArrayList<>();
            if (total > 0)
            {
                expressModelList = expressModelMapper.selectDynamic(paramMap);
            }
            resultMap.put("total", total);
            resultMap.put("list", expressModelList);
            return resultMap;
        }
        catch (LaiKeAPIException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            logger.error("获取快递信息失败：", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "searchExpress");
        }
    }


    @Override
    public Map<String, Object> orderPcList(AdminOrderVo vo) throws LaiKeAPIException
    {
        Map<String, Object> retMap = Maps.newHashMap();
        try
        {
            int    store_id  = vo.getStoreId();
            String adminName = vo.getAdminName();
            // 订单号
            String sNo = vo.getsNo();
            // 品牌
            if (StringUtils.isNotEmpty(vo.getxOrder()))
            {
                vo.setStartdate(DateUtil.dateFormate(new Date(), GloabConst.TimePattern.YMD) + "00:00:00");
                vo.setEnddate(DateUtil.dateFormate(new Date(), GloabConst.TimePattern.YMD) + "23:59:59");
            }

            if ("1".equals(vo.getNewsStatus()))
            {
                /**已付款*/
                vo.setStatus(OrderModel.ORDER_PAYED + "");
            }

            if (StringUtils.isNotEmpty(vo.getsNo()))
            {
                vo.setsNo("%" + vo.getsNo() + "%");
            }
            Map<String, Object>       paramMap   = JSONObject.parseObject(JSONObject.toJSONString(vo));
            int                       total      = orderModelMapper.getAdminOrderCount(paramMap);
            List<Map<String, Object>> returnList = new ArrayList<>();
            if (total > 0)
            {
                //支付方式
                List<PaymentModel>  paymentModelList = paymentModelMapper.selectAll();
                Map<String, Object> payTypeShowName  = Maps.newHashMap();
                for (PaymentModel paymentModel : paymentModelList)
                {
                    payTypeShowName.put(paymentModel.getClass_name(), paymentModel.getName());
                }
                AdminRecordModel adminRecordModel = new AdminRecordModel();
                adminRecordModel.setAdmin_name(adminName);
                adminRecordModel.setStore_id(store_id);
                adminRecordModel.setType(AdminRecordModel.Type.EXCEL_OUT);
                if ("This_page".equals(vo.getPageto()))
                {
                    adminRecordModel.setEvent("导出订单第" + vo.getPageNo() + "的信息 ");
                }
                else if ("whole".equals(vo.getPageto()))
                {
                    adminRecordModel.setEvent("导出订单全部信息");
                }
                else if ("inquiry".equals(vo.getPageto()))
                {
                    adminRecordModel.setEvent("导出订单全部信息");
                }
                paramMap.put("start", vo.getPageNo());
                paramMap.put("pagesize", vo.getPageSize());
                publiceService.addAdminRecord(store_id, adminName, adminRecordModel.getEvent(), AdminRecordModel.Type.EXCEL_OUT, AdminRecordModel.Source.PC_PLATFORM);
                List<Map<String, Object>> orderList = orderModelMapper.adminOrderList(paramMap);

                if (!CollectionUtils.isEmpty(orderList))
                {
                    String type = "1,3,5,";
                    for (Map<String, Object> orderInfo : returnList)
                    {

                        int shopId    = MapUtils.getIntValue(orderInfo, "shop_id");
                        int statusInt = MapUtils.getIntValue(orderInfo, "status");
                        if (StringUtils.isNotEmpty(sNo))
                        {
                            if (statusInt == ORDERS_R_STATUS_COMPLETE)
                            {
                                type += "6,";
                            }
                            if (statusInt == ORDERS_R_STATUS_CLOSE)
                            {
                                type += "4,";
                            }
                            messageLoggingModalMapper.updateMessLogInfo(store_id, shopId, vo.getsNo(), type);
                        }

                        int      freight = 0;
                        String   mchIds  = MapUtils.getString(orderInfo, "mch_id");
                        String[] mchArrs = mchIds.split(SplitUtils.DH);
                        if (statusInt == ORDERS_R_STATUS_UNPAID)
                        {
                            orderInfo.put("t_mch_id", "");
                        }
                        else
                        {
                            orderInfo.put("t_mch_id", mchArrs[1]);
                        }
                        int mchLen = mchArrs.length;
                        if (mchLen > 3)
                        {
                            orderInfo.put("is_mch", 1);
                        }
                        else
                        {
                            orderInfo.put("is_mch", 0);
                        }
                        //地址
                        String sheng   = MapUtils.getString(orderInfo, "sheng");
                        String shi     = MapUtils.getString(orderInfo, "shi");
                        String xian    = MapUtils.getString(orderInfo, "xian");
                        String address = MapUtils.getString(orderInfo, "address");
                        orderInfo.put("address", sheng + shi + xian + address);
                        orderInfo.put("statu", statusInt);
                        String pay = MapUtils.getString(orderInfo, "pay");
                        orderInfo.put("statu", payTypeShowName.getOrDefault(pay, "钱包"));
                        String                    userId           = MapUtils.getString(orderInfo, "user_id");
                        List<Map<String, Object>> orderProductList = orderModelMapper.adminOrderListProductInfo(paramMap);
                        // 快递单号
                        List<String> courierNumList = new ArrayList<>();
                        String       mchname        = "";
                        if (!CollectionUtils.isEmpty(orderProductList))
                        {
                            for (Map<String, Object> vd : orderProductList)
                            {
                                mchname = MapUtils.getString(orderInfo, "mchname");
                                freight += MapUtils.getDouble(orderInfo, "freight");
                                String imgurl = MapUtils.getString(orderInfo, "imgurl");
                                vd.put("imgurl", publiceService.getImgPath(imgurl, store_id));
                                int experId = MapUtils.getIntValue(orderInfo, "express_id");
                                if (experId != 0)
                                {
                                    ExpressModel expressModel = new ExpressModel();
                                    expressModel.setId(experId);
                                    expressModel = expressModelMapper.selectOne(expressModel);
                                    // 快递单号
                                    courierNumList.add(MapUtils.getString(orderInfo, "courier_num") + "(" + expressModel.getKuaidi_name() + ")");
                                }
                                // 订单详情状态
                                int               orderDetailsStatus   = MapUtils.getIntValue(orderInfo, "r_status");
                                int               otherStatusOrderNum  = 0;
                                int               closeOrderNum        = 0;
                                int               allOrderNum          = 0;
                                OrderDetailsModel orderDetailsModelTmp = new OrderDetailsModel();
                                orderDetailsModelTmp.setStore_id(store_id);
                                orderDetailsModelTmp.setR_sNo(sNo);
                                orderDetailsModelTmp.setR_status(orderDetailsStatus);
                                if (orderDetailsStatus != OrderModel.ORDER_CLOSE)
                                {
                                    //其他状态
                                    otherStatusOrderNum = orderDetailsModelMapper.selectCount(orderDetailsModelTmp);
                                }
                                else
                                {
                                    //已经关闭
                                    closeOrderNum = orderDetailsModelMapper.selectCount(orderDetailsModelTmp);
                                }
                                orderDetailsModelTmp.setR_status(null);
                                //所有
                                allOrderNum = orderDetailsModelMapper.selectCount(orderDetailsModelTmp);

                                // 如果订单下面的商品都处在同一状态,那就改订单状态为已完成
                                if ((otherStatusOrderNum + closeOrderNum) == allOrderNum)
                                {
                                    //如果订单数量相等 则修改父订单状态
                                    orderModelMapper.updateOrderStatus(store_id, sNo, userId, orderDetailsStatus);
                                }
                            }

                            Map courier_num_map = Maps.newHashMap();
                            for (String couriernum1 : courierNumList)
                            {
                                if (!courier_num_map.containsKey(couriernum1))
                                {
                                    courier_num_map.put(couriernum1, couriernum1);
                                }
                            }
                            orderInfo.put("courier_num", courier_num_map);
                            orderInfo.put("products", orderProductList);
                            orderInfo.put("mchname", mchname);
                            getOrderStatus(orderInfo, statusInt);
                            orderInfo.put("freight", freight);
                            returnList.add(orderInfo);
                        }

                    }
                }
            }

            //todo 没有用
            retMap.put("brand_str", "");
            retMap.put("ordtype", "");
            retMap.put("list", returnList);
            retMap.put("total", total);
            retMap.put("source_str", publiceService.getDictionaryList("来源"));
            retMap.put("class", publiceService.getDictionaryList("订单状态"));
            return retMap;
        }
        catch (Exception e)
        {
            logger.error("订单列表获取 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DDLBHQSB, "订单列表获取失败", "orderPcList");
        }
    }

    /**
     * 后台获取订单状态
     *
     * @param orderInfo
     * @param statusInt
     */
    private void getOrderStatus(Map<String, Object> orderInfo, int statusInt)
    {
        switch (statusInt)
        {
            case ORDERS_R_STATUS_UNPAID:
                orderInfo.put("status", "待付款");
                orderInfo.put("bgcolor", "#f5b1aa");
                break;
            case ORDERS_R_STATUS_CONSIGNMENT:
                orderInfo.put("status", "待发货");
                orderInfo.put("bgcolor", "#f09199");
                break;
            case ORDERS_R_STATUS_DISPATCHED:
                orderInfo.put("status", "待收货");
                orderInfo.put("bgcolor", "#f19072");
                break;
            case ORDERS_R_STATUS_COMPLETE:
                orderInfo.put("status", "订单完成");
                orderInfo.put("bgcolor", "#f7b977");
                break;
            case ORDERS_R_STATUS_CLOSE:
                orderInfo.put("status", "订单关闭");
                orderInfo.put("bgcolor", "#ffbd8b");
                break;
            case ORDERS_R_STATUS_TOBEVERIFIED:
                orderInfo.put("status", "待核销");
                orderInfo.put("bgcolor", "#ffbd8b");
                break;
        }
    }

    @Override
    public Map<String, Object> getLogistics(String orderNo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            List<Object> resultList = new ArrayList<>(16);
            //物流信息集
            List<Object> dataList = new ArrayList<>();
            //商品信息快递单号分组
            Map<String, Map<String, Object>> goodsGroupMap = new HashMap<>(16);
            //快递查询付费接口开关
            boolean isOpen = false;
            //是否全部发货多包裹
            boolean isPackage = false;
            //获取订单信息
            OrderModel orderModel = new OrderModel();
            orderModel.setsNo(orderNo);
            orderModel = orderModelMapper.selectOne(orderModel);
            if (orderModel != null)
            {
                //收货人号码 ->顺丰速运、顺丰快运必填
                String mobile = orderModel.getMobile();
                //物流去重,如果多个商品统一发货则只显示一个物流动态
                Set<String> wuLiuIds = new HashSet<>();
                //商品分组
                Map<String, List<Map<String, Object>>> goodsGroupList = new HashMap<>(16);

                //获取配置信息
                ConfigModel configModel = new ConfigModel();
                configModel.setStore_id(orderModel.getStore_id());
                configModel = configModelMapper.selectOne(configModel);
                if (configModel != null && StringUtils.isNotEmpty(configModel.getExpress_address()))
                {
                    if (configModel.getIs_express() == DictionaryConst.WhetherMaven.WHETHER_OK)
                    {
                        isOpen = true;
                    }
                }
                //发货记录
                ExpressDeliveryModel expressDeliveryModel;
                OrderDetailsModel    orderDetailsModel = new OrderDetailsModel();
                orderDetailsModel.setR_sNo(orderNo);
                List<OrderDetailsModel> orderDetailsModelList = orderDetailsModelMapper.select(orderDetailsModel);
                for (OrderDetailsModel orderDetails : orderDetailsModelList)
                {
                    Map<String, Object> resultMapTemp = new HashMap<>(16);
                    if (orderDetails.getExpress_id() == null || orderDetails.getCourier_num() == null)
                    {
                        continue;
                    }
                    //快递公司id
                    List<String> expressIds = Arrays.asList(orderDetails.getExpress_id().split(","));
                    //快递单号
                    List<String> courierNums = Arrays.asList(orderDetails.getCourier_num().split(","));
                    for (int i = 0; i < expressIds.size(); i++)
                    {
                        Map<String, Object> goodsMap = new HashMap<>(16);
                        resultMapTemp = new HashMap<>();
                        String expressId  = expressIds.get(i);
                        String courierNum = courierNums.get(i);
                        if (StringUtils.isEmpty(courierNum))
                        {
                            //剔除没有物流单号的商品
                            continue;
                        }
                        //快递名称
                        String kdName = "";
                        //快递公司代号
                        String kdCode   = "";
                        String wuLiuKey = expressId + courierNum;
                        //查询发货记录
                        expressDeliveryModel = new ExpressDeliveryModel();
                        expressDeliveryModel.setOrderDetailsId(orderDetails.getId());
                        expressDeliveryModel.setCourierNum(courierNum);
                        expressDeliveryModel.setExpressId(Integer.valueOf(expressId));
                        expressDeliveryModel = expressDeliveryModelMapper.selectOne(expressDeliveryModel);
                        //发货数量
                        int num = 0;
                        if (expressDeliveryModel == null)
                        {
                            num = orderDetails.getNum();
                        }
                        else
                        {
                            num = expressDeliveryModel.getNum();
                            //未全部发货，多包裹
                            if (num < orderDetails.getNum())
                            {
                                isPackage = true;
                            }
                        }
                        //获取商品信息
                        ConfiGureModel confiGureModel = confiGureModelMapper.selectByPrimaryKey(orderDetails.getSid());
                        goodsMap.put("img", publiceService.getImgPath(confiGureModel.getImg(), orderDetails.getStore_id()));
                        goodsMap.put("num", num);
                        if (wuLiuIds.contains(wuLiuKey))
                        {
                            //获取同单物流信息
                            resultMapTemp = goodsGroupMap.get(courierNum);
                            List<Map<String, Object>> goodsListTemp = goodsGroupList.get(courierNum);
                            goodsListTemp.add(goodsMap);
                            resultMapTemp.put("pro_list", goodsListTemp);
                            continue;
                        }

                        //获取快递公司代码
                        ExpressModel expressModel = expressModelMapper.selectByPrimaryKey(expressId);
                        if (expressModel == null)
                        {
                            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLGSBCZ, "物流公司不存在");
                        }
                        kdName = expressModel.getKuaidi_name();
                        kdCode = expressModel.getType();
                        String url;
                        String resultJson = null;
                        String cpc = orderModel.getCpc();
                        boolean home = CpcUtils.isHome(cpc);
                        if (isOpen)
                        {
                            url = configModel.getExpress_address();
                            Map<String, Object> parma = new HashMap<>(16);
                            parma.put("com", kdCode);
                            parma.put("num", courierNum);
                            parma.put("phone", mobile);
                            parma.put("from", "");
                            parma.put("to", "");
                            parma.put("resultv2", "1");
                            //请求参数
                            String param = JSON.toJSONString(parma);
                            //签名
                            String sign = Md5Util.MD5endoce(param + configModel.getExpress_key() + configModel.getExpress_number());
                            if (sign == null)
                            {
                                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SJCW, "数据错误");
                            }
                            url += "?param=" + param + "&sign=" + sign.toUpperCase() + "&customer=" + configModel.getExpress_number();

                            resultJson = HttpUtils.post(url);
                        }
                        else
                        {
                            if (home)
                            {
                                url = String.format(GloabConst.OtherUrl.API_KUAIDI100_URL, kdCode, courierNum);
                                resultJson = HttpUtils.get(url);
                            }
                            else
                            {
                                String logistics = orderDetails.getLogistics();
                                if (StringUtils.isNotEmpty(logistics))
                                {
                                    dataList = UrlParamUtils.splitJsonArraysBySeparator(logistics);
                                    dataList = UrlParamUtils.convertLogisticsFields(dataList);
                                }
                            }
                        }
                        if (home)
                        {
                            JSONObject obj = JSONObject.parseObject(resultJson);
                            if (obj != null && obj.containsKey("data"))
                            {
                                dataList = DataUtils.cast(obj.get("data"));
                            }
                        }

                        if (CollectionUtils.isEmpty(dataList))
                        {
                            dataList = new ArrayList<>();
                        }
                        goodsMap.put("img", publiceService.getImgPath(confiGureModel.getImg(), orderDetails.getStore_id()));
                        goodsMap.put("num", num);
                        //分组
                        if (goodsGroupMap.containsKey(courierNum))
                        {
                            //获取同单物流信息
                            resultMapTemp = goodsGroupMap.get(courierNum);
                            List<Map<String, Object>> goodsListTemp = goodsGroupList.get(courierNum);
                            resultMapTemp.put("pro_list", goodsListTemp);
                        }
                        else
                        {
                            //商品信息根据物流单分组
                            List<Map<String, Object>> goodsListTemp = new ArrayList<>();
                            if (goodsGroupList.containsKey(courierNum))
                            {
                                goodsListTemp.addAll(goodsGroupList.get(courierNum));
                            }
                            else
                            {
                                goodsListTemp.add(goodsMap);
                                goodsGroupList.put(courierNum, goodsListTemp);
                            }
                            //构造数据结构
                            resultMapTemp.put("list", dataList);
                            resultMapTemp.put("pro_list", goodsListTemp);
                            resultMapTemp.put("kuaidi_name", kdName);
                            resultMapTemp.put("courier_num", courierNum);
                            resultMapTemp.put("type", kdCode);
                            //是否为售后物流 1=订单物流 2=售后物流
                            resultMapTemp.put("shop_type", 1);
                            goodsGroupMap.put(courierNum, resultMapTemp);
                        }
                        wuLiuIds.add(wuLiuKey);
                        resultList.add(resultMapTemp);
                    }
                }
            }
            else if (StringUtils.isInteger(orderNo))
            {
                //售后物流
                ReturnOrderModel returnOrderModel = returnOrderModelMapper.selectByPrimaryKey(orderNo);
                if (returnOrderModel != null)
                {
                    ReturnGoodsModel returnGoodsModel = new ReturnGoodsModel();
                    returnGoodsModel.setStore_id(returnOrderModel.getStore_id());
                    returnGoodsModel.setOid(String.valueOf(returnOrderModel.getP_id()));
                    List<ReturnGoodsModel> list = returnGoodsModelMapper.select(returnGoodsModel);
                    int                    i    = 0;
                    for (ReturnGoodsModel returnGoods : list)
                    {
                        Map<String, Object> resultMapTemp;
                        //快递公司id
                        String express = returnGoods.getExpress();
                        //快递单号
                        String courierNum = returnGoods.getExpress_num();
                        //快递名称
                        String kdName = "";
                        //快递公司代号
                        String kdCode = "";
                        //获取订单信息
                        orderModel = new OrderModel();
                        orderModel.setsNo(returnOrderModel.getsNo());
                        orderModel = orderModelMapper.selectOne(orderModel);

                        String cpc = orderModel.getCpc();
                        boolean home = CpcUtils.isHome(cpc);
                        //收货人号码 ->顺丰速运、顺丰快运必填
                        String mobile = orderModel.getMobile();
                        //获取配置信息
                        ConfigModel configModel = new ConfigModel();
                        configModel.setStore_id(orderModel.getStore_id());
                        configModel = configModelMapper.selectOne(configModel);
                        if (configModel != null && StringUtils.isNotEmpty(configModel.getExpress_address()))
                        {
                            if (configModel.getIs_express() == DictionaryConst.WhetherMaven.WHETHER_OK)
                            {
                                isOpen = true;
                            }
                        }
                        //获取订单明细
                        OrderDetailsModel orderDetailsModel = new OrderDetailsModel();
//                        orderDetailsModel.setR_sNo(orderNo);
                        orderDetailsModel.setId(Integer.parseInt(returnGoods.getOid()));
                        OrderDetailsModel orderDetails = orderDetailsModelMapper.selectOne(orderDetailsModel);
                        //获取商品信息
                        ConfiGureModel confiGureModel = confiGureModelMapper.selectByPrimaryKey(orderDetails.getSid());
                        //获取快递公司代码
                        if (express != null)
                        {
                            ExpressModel expressModel = new ExpressModel();
                            expressModel.setKuaidi_name(express);
                            expressModel.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS);
                            expressModel = expressModelMapper.selectOne(expressModel);
                            kdName = expressModel.getKuaidi_name();
                            kdCode = expressModel.getType();
                            String url;
                            String resultJson;
                            if (isOpen)
                            {
                                url = configModel.getExpress_address();
                                Map<String, Object> parma = new HashMap<>(16);
                                parma.put("com", kdCode);
                                parma.put("num", courierNum);
                                parma.put("phone", mobile);
                                parma.put("from", "");
                                parma.put("to", "");
                                parma.put("resultv2", "1");
                                //请求参数
                                String param = JSON.toJSONString(parma);
                                //签名
                                String sign = Md5Util.MD5endoce(param + configModel.getExpress_key() + configModel.getExpress_number());
                                if (sign == null)
                                {
                                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SJCW, "数据错误");
                                }
                                url += "?param=" + param + "&sign=" + sign.toUpperCase() + "&customer=" + configModel.getExpress_number();
                                resultJson = HttpUtils.post(url);
                            }
                            else
                            {
                                if (home)
                                {
                                    url = String.format(GloabConst.OtherUrl.API_KUAIDI100_URL, kdCode, courierNum);
                                    resultJson = HttpUtils.get(url);
                                    JSONObject obj = JSONObject.parseObject(resultJson);
                                    if (obj.containsKey("data"))
                                    {
                                        dataList = DataUtils.cast(obj.get("data"));
                                    }
                                    else
                                    {
                                        dataList = new ArrayList<>();
                                    }
                                }
                                else
                                {
                                    dataList = UrlParamUtils.splitJsonArraysBySeparator(returnGoods.getLogistics());
                                    dataList = UrlParamUtils.convertLogisticsFields(dataList);
                                }
                            }
                        }
                        //分组
                        if (goodsGroupMap.containsKey(courierNum))
                        {
                            resultMapTemp = goodsGroupMap.get(courierNum);
                        }
                        else
                        {
                            //构造数据结构
                            Map<String, Object> tempMap = new HashMap<>(16);
                            tempMap.put("list", dataList);
                            Map<String, Object> goodsMap = new HashMap<>(16);
                            //商品信息
                            List<Map<String, Object>> goodsInfo = new ArrayList<>();
                            goodsMap.put("img", publiceService.getImgPath(confiGureModel.getImg(), orderDetails.getStore_id()));
                            goodsMap.put("num", orderDetails.getNum());
                            goodsInfo.add(goodsMap);

                            tempMap.put("pro_list", goodsInfo);
                            tempMap.put("kuaidi_name", kdName);
                            tempMap.put("courier_num", courierNum);
                            tempMap.put("type", kdCode);
                            //是否为售后物流 1=订单物流 2=商家回寄物流  3=买家回寄物流
                            if (list.size() > 1)
                            {
                                if (i == 0)
                                {
                                    tempMap.put("shop_type", 3);
                                }
                                else
                                {
                                    tempMap.put("shop_type", 2);
                                }
                            }
                            else
                            {
                                tempMap.put("shop_type", 3);
                            }
                            resultMapTemp = tempMap;
                            goodsGroupMap.put(courierNum, resultMapTemp);
                        }
                        i++;
                        resultList.add(resultMapTemp);
                    }
                }
            }
            else
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DDBCZ, "订单不存在", "logistics");
            }

            resultMap.put("list", resultList);
            resultMap.put("isPackage", isPackage);
            return resultMap;
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取物流信息 异常:", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_HQWLXXYC, "获取物流信息 异常", "getLogistics");
        }
    }


    @Override
    public Map<String, Object> ucOrderDetails(OrderVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap  = new HashMap<>(16);
        JSONObject          jsonObject = new JSONObject();
        // 订单中商品信息
        List<Map> productList = new ArrayList<>();
        // 没有批量退货按钮
        int batchDel = 1;
        try
        {
            int     storeId = vo.getStoreId();
            Integer orderId = vo.getOrderId();
            User    user;
            if (vo.getUser() != null)
            {
                user = vo.getUser();
            }
            else
            {
                user = RedisDataTool.getLktUser(vo.getAccessId(), redisUtil);
            }
            //获取用户最新数据
            user = userBaseMapper.selectByPrimaryKey(user.getId());

            //各个支付的开启、关闭状态
            Map payInfo = new HashMap();
            payInfo = publicPaymentConfigService.getPaymentInfos(storeId);

            // 根据微信id,查询用户id
            String userId = user.getUser_id();
            // 支付密码错误次数
            int loginNum = user.getLogin_num() == null ? 0 : user.getLogin_num();
            // 支付密码验证时间
            Date verificationTime = user.getVerification_time();
            if (verificationTime != null)
            {
                verificationTime = DateUtil.getAddDate(verificationTime, 1);
            }

            String user_password  = user.getPassword();
            int    passwordStatus = DictionaryConst.WhetherMaven.WHETHER_NO;
            if (!org.springframework.util.StringUtils.isEmpty(user_password))
            {
                passwordStatus = DictionaryConst.WhetherMaven.WHETHER_OK;
            }

            Date time = new Date();
            // 是否可以输入密码
            boolean enterless = true;
            if (loginNum == GloabConst.LktConfig.LOGIN_AGAIN_MAX)
            {
                if (verificationTime != null && time.after(verificationTime))
                {
                    enterless = false;
                }
                else
                {
                    userMapper.updateUserLoginnum(storeId, userId);
                    enterless = true;
                }
            }

            OrderModel orderModel = new OrderModel();
            orderModel.setStore_id(storeId);
            orderModel.setId(orderId);
            orderModel = orderModelMapper.selectOne(orderModel);
            if (orderModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误", "orderDetails");
            }
            int mchId = Integer.parseInt(StringUtils.trim(orderModel.getMch_id(), SplitUtils.DH).split(SplitUtils.DH)[0]);
            //获取插件订单配置
            Map<String, Object> configMap = getOrderConfig(storeId, mchId, orderModel.getOtype());
            //订单失效  - 秒
            Integer orderFailure = orderModel.getOrderFailureTime();
            // 订单过期删除时间单位
            String company = "hour";
            //订单售后时间 按秒算
            int orderAfter = DateUtil.dateConversion(7, DateUtil.TimeType.DAY);
            if (configMap != null)
            {
                if (StringUtils.isEmpty(orderFailure))
                {
                    orderFailure = MapUtils.getIntValue(configMap, "orderFailureDay");
                }
                orderFailure = orderFailure / 60 / 60;
                if (!configMap.containsKey("order_failure"))
                {
                    orderFailure = 2;
                }
                orderAfter = MapUtils.getIntValue(configMap, "orderAfter");
            }
            // 订单号
            String sNo = orderModel.getsNo();
            //是否为会员订单
            boolean vipGoods = Objects.nonNull(orderModel.getVip_source()) && orderModel.getVip_source() == 1;
            //调起支付订单号
            String real_sno = orderModel.getReal_sno();
            //支付类型
            String pay_type = orderModel.getPay();
            // 总价
            BigDecimal zPrice = orderModel.getZ_price();
            // 订单总价
            BigDecimal old_total = orderModel.getOld_total();
            //总运费
            BigDecimal orderFreight = orderModel.getZ_freight();
            // 计算发票金额（总价 - 运费）
            BigDecimal invoicePriceBD = zPrice.subtract(orderFreight);
            // 如果需要int类型，进行转换（四舍五入并确保非负）
            int invoicePrice = Math.max(0, invoicePriceBD.setScale(0, RoundingMode.HALF_UP).intValue());
            //订单状态
            int status = orderModel.getStatus();
            // 余额抵扣
            BigDecimal offset_balance = orderModel.getOffset_balance();
            offset_balance = offset_balance == null ? BigDecimal.ZERO : offset_balance;
            if (DictionaryConst.OrdersType.ORDERS_HEADER_GM.equals(orderModel.getOtype())
                    && zPrice.compareTo(offset_balance) != 0 && status != 0)
            {
                zPrice = zPrice.add(offset_balance);
            }
            //订单备注处理
            String remarks      = "";
            String orderRemarks = orderModel.getRemarks();
            if (StringUtils.isNotEmpty(orderRemarks))
            {
                // 订单备注
                Map<String, String> remarkMap = JSON.parseObject(orderRemarks, new TypeReference<Map<String, String>>()
                {
                });
                for (String key : remarkMap.keySet())
                {
                    remarks = remarkMap.get(key);
                }
            }


            /**
             * 收到取消订单
             */
            int hand_del = 0;

            // 订单时间
            Date   addTime   = orderModel.getAdd_time();
            String dbRemarks = orderModel.getRemarks();
            Date   payTime   = orderModel.getPay_time();
            //0:其他正常关闭   1：待付款关闭订单
            Integer isDfkGbi = 0;
            if (payTime == null && orderModel.getStatus() == ORDERS_R_STATUS_CLOSE)
            {
                isDfkGbi = 1;
            }
            //联系人
            String name = orderModel.getName();
            //联系手机号
            String mobile1 = orderModel.getMobile();
            if (orderModel.getSelf_lifting().equals(OrderModel.SELF_LIFTING_DISTRIBUTION) && StringUtils.isNotEmpty(mobile1))
            {
                //隐藏操作
                mobile1 = mobile1.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2");
            }

            String sheng = orderModel.getSheng();
            String shi   = orderModel.getShi();
            String xian  = orderModel.getXian();
            //联系地址
            String address = orderModel.getAddress();

            //订单类型
            String otype = orderModel.getOtype();



            //自动满减名称
            String couponActivityName = orderModel.getCoupon_activity_name();
            //满减金额
            BigDecimal reducePrice = Objects.nonNull(orderModel.getReduce_price()) ? orderModel.getReduce_price() : BigDecimal.ZERO;
            if (DataUtils.equalBigDecimalZero(reducePrice))
            {
                couponActivityName = "￥" + reducePrice;
            }
            // 优惠券ID
            String couponId = orderModel.getCoupon_id();
            // 订单取消方式
            Integer cancel_method = 0;
            // 优惠券金额
            BigDecimal couponPrice = Objects.nonNull(orderModel.getCoupon_price()) ? orderModel.getCoupon_price() : BigDecimal.ZERO;
            // 会员等级折扣
            BigDecimal gradeRate = orderModel.getGrade_rate();

            if (gradeRate == null)
            {
                gradeRate = BigDecimal.ONE;
            }

            //提醒状态
            Integer deliveryStatus = orderModel.getDelivery_status();
            // 是否自提
            Integer selfLifting = orderModel.getSelf_lifting();
            // 自提门店
            Integer singleStore = orderModel.getSingle_store();
            //平台优惠金额
            BigDecimal preferentialAmount = orderModel.getPreferential_amount();
            //下单类型
            Integer operationType = orderModel.getOperation_type();
            //代客下单手动优惠金额
            BigDecimal manualOffer = orderModel.getManual_offer();
            //是否是是拆单后的订单
            String pSno = orderModel.getP_sNo();
            //多店铺优惠劵拆单--》优惠金额特殊处理
            boolean isCouponHandle = false;
            // 平台优惠券金额
            BigDecimal preferential_amount = BigDecimal.ZERO;
            //分销折扣金额
            BigDecimal comm_amount = orderModel.getComm_discount();
            //
            String couponName = "";

            //主播ID
            String              anchor_id      = "";
            CouponActivityModel couponActivity = null;
            if (!StringUtils.isEmpty(couponId))
            {
                orderModel.setCoupon_id(couponId = StringUtils.trim(orderModel.getCoupon_id(), SplitUtils.DH));
                String[] cpids = couponId.split(SplitUtils.DH);
                for (String cpid : cpids)
                {
                    if (!"0".equals(cpid))
                    {
                        couponActivity = couponActivityModelMapper.getOneCouponActivity(storeId, Integer.parseInt(cpid));
                        break;
                    }
                }
            }

            if (couponActivity != null)
            {
                if (couponActivity.getActivity_type() == 2)
                {
                    couponName = "(" + couponActivity.getDiscount().multiply(BigDecimal.TEN) + "折)";
                }
            }
            BigDecimal userMoney = BigDecimal.ZERO;
            BigDecimal userScore = BigDecimal.ZERO;
            if (status == 0 || status == 7)
            {
                // 未付款
                //用户余额
                userMoney = user.getMoney();
                //用户积分 --积分订单判断（建议积分订单单独抽取）
                userScore = new BigDecimal(user.getScore());
            }

            BigDecimal productTotal = BigDecimal.ZERO;
            BigDecimal zFreight     = BigDecimal.ZERO;
            String     discountType = "";
            //类型：优惠券coupon、满减subtraction 0,12 或者  12,21 最后一个逗号后面的数字就是平台优惠券id
            String platCouponId = null;
            if (!Objects.isNull(couponId))
            {
                String[] couponsId = StringUtils.trim(couponId, SplitUtils.DH).split(SplitUtils.DH);
                platCouponId = couponsId[couponsId.length - 1];
            }
            else
            {
                platCouponId = "0";
            }


            if (!"0".equals(platCouponId))
            {
                //平台优惠券优惠类型
                discountType = "优惠券";
                //平台优惠券和店铺优惠券合在一块了
                couponPrice = couponPrice.subtract(preferentialAmount);
            }
            int subtractionId = orderModel.getSubtraction_id() == null ? 0 : orderModel.getSubtraction_id();
            if (subtractionId != 0)
            {
                discountType = "满减";
            }
            // 会员优惠金额
            BigDecimal gradeRateAmount = BigDecimal.ZERO;
            // 根据订单号,查询订单详情
            OrderDetailsModel orderDetailsInfo = new OrderDetailsModel();
            orderDetailsInfo.setStore_id(storeId);
            orderDetailsInfo.setR_sNo(sNo);
            List<OrderDetailsModel> orderDetailsModelsList = orderDetailsModelMapper.select(orderDetailsInfo);
            if (orderDetailsModelsList == null || orderDetailsModelsList.size() <= 0)
            {
                logger.error("订单是否多店铺已经拆单");
                orderDetailsModelsList = new ArrayList<>();
                //订单是否多店铺已经拆单
                OrderModel order = new OrderModel();
                order.setStore_id(storeId);
                order.setP_sNo(sNo);
                List<OrderModel> orderModelList = orderModelMapper.select(order);
                for (OrderModel model : orderModelList)
                {
                    orderDetailsInfo.setR_sNo(model.getsNo());
                    orderDetailsModelsList.addAll(orderDetailsModelMapper.select(orderDetailsInfo));
                }
            }
            boolean userCanAfter = true;
            String  mchName      = "";
            //可开发票过期提醒
            boolean isInvoice = false;
            //发票超时
            boolean invoiceTimeout = false;
            //pc商城是否显示批量售后按钮
            boolean allRefund = false;
            boolean refundShowBtn = false;
            //是否有商品已经发货，显示发货按钮
            boolean haveExpress = false;
            //多订单评论按钮
            List<Integer> orderCommentTypeList = new ArrayList<>();
            int commentsType = 2;
            boolean comment = false;
            Map<String, Object> orderDetailValueMap = new HashMap<>();
            //直播间ID
            String living_id = "";
            if (!CollectionUtils.isEmpty(orderDetailsModelsList))
            {
                // 是否有批量申请
                List<Integer> batch = new ArrayList<Integer>();
                //订单详情正在售后中的订单数量
                Integer orderDetailsNum = 0;
                for (OrderDetailsModel orderDetails : orderDetailsModelsList)
                {
                    if (orderDetails.getDeliverNum() != null && orderDetails.getDeliverNum() > 0 && orderModel.getSelf_lifting() != SELF_LIFTING_STORE_SELF)
                    {
                        //2为商家自配的订单不展示物流信息
                        haveExpress = true;
                    }
                    //订单详情id
                    Integer detailId = orderDetails.getId();
                    //订单状态
                    Integer detailStatus = orderDetails.getR_status();
                    // 商品售价
                    BigDecimal orderDetailsPPrice = orderDetails.getP_price();
                    // 商品数量
                    BigDecimal pNum = BigDecimal.valueOf(orderDetails.getNum());
                    // 优惠后的金额
                    BigDecimal afterDiscount = orderDetails.getAfter_discount();
                    // 总折扣
                    gradeRateAmount = gradeRateAmount.add(orderDetailsPPrice.subtract(orderDetailsPPrice.multiply(gradeRate)).multiply(pNum)).setScale(2, BigDecimal.ROUND_HALF_UP);
                    if (StringUtils.isNotEmpty(pSno) && (BigDecimal.ZERO.compareTo(preferentialAmount) < 0 || BigDecimal.ZERO.compareTo(couponPrice) < 0))
                    {
                        isCouponHandle = true;
                        //这里是订单明细优惠
                        preferential_amount = preferential_amount.add(orderDetailsPPrice.multiply(pNum).subtract(afterDiscount));
//                    coupon_price = BigDecimal.ZERO;
                    }
                    // 属性id
                    String         sid            = orderDetails.getSid();
                    ConfiGureModel confiGureModel = confiGureModelMapper.selectByPrimaryKey(sid);
                    if (confiGureModel == null)
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SPSXBCZ, "商品属性不存在");
                    }
                    //插件商品id
                    int pluginId = orderDetails.getP_id();
                    //直播间id
                    living_id = orderDetails.getLiving_room_id();
                    // 产品id
                    int p_id = confiGureModel.getPid();
                    orderDetails.setP_id(p_id);
                    // 数据结构转换
                    jsonObject = JSONObject.parseObject(JSON.toJSONString(orderDetails));
                    //评价按钮
                    int orderCommentType = this.orderCommentType(storeId, userId, sNo, detailId, confiGureModel.getId(), orderDetails.getR_status());
                    orderCommentTypeList.add(orderCommentType);
                    //有一个商品已经评价了
                    jsonObject.put("comments_type", orderCommentType);
                    if (orderCommentType == 1)
                    {
                        commentsType = 1;
                        comment = true;
                    }
                    else if (orderCommentType == 2)
                    {
                        comment = true;
                    }

                    //根据产品id 查询店铺id
                    ProductListModel productListModel = new ProductListModel();
                    productListModel.setId(p_id);
                    productListModel.setStore_id(storeId);
                    // 如果为虚拟商品
                    if (orderModel.getOtype().equals(DictionaryConst.OrdersType.ORDERS_HEADER_VI))
                    {
                        productListModel.setCommodity_type(ProductListModel.COMMODITY_TYPE.virtual);
                    }
                    productListModel = productListModelMapper.selectOne(productListModel);
                    if (productListModel != null)
                    {
                        int      shop_id  = productListModel.getMch_id();
                        MchModel mchModel = new MchModel();
                        mchModel.setId(shop_id);
                        mchModel.setRecovery(DictionaryConst.ProductRecycle.NOT_STATUS);
                        mchModel = mchModelMapper.selectOne(mchModel);
                        if (mchModel != null)
                        {
                            jsonObject.put("shop_id", shop_id);
                            jsonObject.put("shop_name", mchModel.getName());
                            jsonObject.put("shop_logo", publiceService.getImgPath(mchModel.getLogo(), storeId));
                            jsonObject.put("shop_head", publiceService.getImgPath(mchModel.getHead_img(), storeId));
                        }
                        else
                        {
                            jsonObject.put("shop_name", "");
                            jsonObject.put("shop_logo", "");
                        }
                        jsonObject.put("shop_id", shop_id);
                        //虚拟商品的是否需要线下核销、是否需要预约时间
                        jsonObject.put("write_off_settings", productListModel.getWrite_off_settings());
                        jsonObject.put("is_appointment", productListModel.getIs_appointment());

                        //处理订单详情中是否显示适用核销门店
                        if (productListModel.getWrite_off_settings() != null && productListModel.getWrite_off_settings().equals(ProductListModel.WRITE_OFF_SETTINGS.offline))
                        {
                            resultMap.put("show_write_store", 1);
                            //mchId,storeId
                            int i = 0;
                            if (productListModel.getWrite_off_mch_ids().equals("0"))
                            {
                                Map<String, Object> Map = new HashMap<>();
                                Map.put("store_id", storeId);
                                Map.put("mch_id", mchId);
                                i = mchStoreModelMapper.countDynamic(Map);
                            }
                            else
                            {
                                String[] split = productListModel.getWrite_off_mch_ids().split(SplitUtils.DH);
                                i += split.length;
                            }
                            resultMap.put("write_store_num", i);
                        }
                        //处理预约信息
                        if (productListModel.getWrite_off_settings() != null && productListModel.getWrite_off_settings().equals(ProductListModel.WRITE_OFF_SETTINGS.offline) &&
                                productListModel.getIs_appointment() != null && productListModel.getIs_appointment().equals(ProductListModel.IS_APPOINTMENT.isOpin))
                        {
                            Map<String, Object> appointment = new HashMap<>();
                            appointment.put("time", orderDetails.getWrite_time());
                            MchStoreModel mchStoreModel = mchStoreModelMapper.selectByPrimaryKey(orderDetails.getMch_store_write_id());
                            if (mchStoreModel != null)
                            {
                                appointment.put("name", mchStoreModel.getName());
                                appointment.put("address", mchStoreModel.getSheng() + mchStoreModel.getShi() + mchStoreModel.getXian() + " " + mchStoreModel.getAddress());
                            }
                            //为1展示预约信息
                            resultMap.put("show_appointment", 1);
                            resultMap.put("appointment", appointment);
                        }
                    }
                    //商品单价
                    orderDetailsPPrice = orderDetails.getP_price();
                    // 数量
                    BigDecimal num = BigDecimal.valueOf(orderDetails.getNum());
                    // 运费
                    BigDecimal freight = orderDetails.getFreight();
                    // 商品总价
                    productTotal = productTotal.add(orderDetailsPPrice.multiply(num));
                    //如果为竞拍商品，重新计算总价
                    // 运费总价
                    zFreight = zFreight.add(freight);
                    //结算时间
                    Date arriveTime = orderModel.getArrive_time();
                    //结算状态
                    Integer settlementStatus = orderModel.getSettlement_status();
                    //如果售后期设置的是0天，结算时间则默认当前时间
                    if(orderAfter == 0 && status == ORDERS_R_STATUS_COMPLETE && Objects.isNull(arriveTime) && settlementStatus == 0)
                    {
                        arriveTime = new Date();
                    }
                    //是否可以售后
                    if (arriveTime != null)
                    {
                        //售后截至日期 按秒算
                        Date afterDate = DateUtil.getAddDateBySecond(new Date(), orderAfter);
                        logger.info("售后日期转换***********************{}", DateUtil.dateFormate(afterDate, GloabConst.TimePattern.YMDHMS));
                        if (new Date().compareTo(afterDate) >= 0)
                        {
                            userCanAfter = false;
                        }
                    }
                    //售后标识 判断申请售后按钮的显示
//                    if (ORDERS_R_STATUS_CLOSE != orderModel.getStatus()) {
                    Map<String, Boolean> buttonShow = publicRefundService.afterSaleButtonShow(storeId, orderModel.getOtype(), orderDetails);
                    refundShowBtn = MapUtils.getBoolean(buttonShow, "refundShowBtn");
                    jsonObject.putAll(buttonShow);
                    //pc商城显示批量售后按钮
                    if (jsonObject.get("refund") != null)
                    {
                        if ((Boolean) jsonObject.get("refund"))
                        {
                            allRefund = true;
                        }
                    }
//                    }
                    Date date = DateUtil.getAddDate(new Date(), -7);
                    if (arriveTime != null)
                    {
                        if (arriveTime.before(date))
                        {
                            // 到货时间少于7天
                            jsonObject.put("info", 1);
                        }
                        else
                        {
                            // 已经到货
                            jsonObject.put("info", 0);
                        }
                    }
                    else
                    {
                        jsonObject.put("info", 0);
                    }
                    //店铺信息
                    MchModel mchModel = mchModelMapper.getMchInfo(String.valueOf(mchId), storeId);
                    if (!Objects.isNull(arriveTime) && settlementStatus.equals(DictionaryConst.WhetherMaven.WHETHER_OK) && status == ORDERS_R_STATUS_COMPLETE && mchModel.getIs_invoice().equals(DictionaryConst.WhetherMaven.WHETHER_OK))
                    {
                        isInvoice = true;
                        if (DateUtil.dateCompare(new Date(), DateUtil.getAddDate(arriveTime, 20)))
                        {
                            invoiceTimeout = true;
                        }
                    }
                    // 根据产品id,查询产品列表 (产品规格图片)
                    String              url        = publiceService.getImgPath(confiGureModel.getImg(), storeId);
                    Map<String, Object> productMap = jsonObject;
                    productMap.put("imgurl", url);
                    productMap.put("sid", sid);
                    productMap.put("is_distribution", productListModel == null ? 0 : productListModel.getIs_distribution());
                    //不是普通订单则用插件id
                    if (!DictionaryConst.OrdersType.ORDERS_HEADER_GM.equals(orderModel.getOtype()))
                    {
                        //如果是分销则转换id
                        if (DictionaryConst.OrdersType.ORDERS_HEADER_FX.equalsIgnoreCase(orderModel.getOtype()))
                        {
                            DistributionGoodsModel distributionGoodsModel = new DistributionGoodsModel();
                            distributionGoodsModel.setStore_id(vo.getStoreId());
                            distributionGoodsModel.setP_id(orderDetails.getP_id());
                            distributionGoodsModel.setS_id(Integer.parseInt(orderDetails.getSid()));
                            distributionGoodsModel.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS);
                            distributionGoodsModel = distributionGoodsModelMapper.selectOne(distributionGoodsModel);
                            if (distributionGoodsModel != null)
                            {
                                pluginId = distributionGoodsModel.getId();
                            }
                        }
                        productMap.put("pluginId", pluginId);
                    }
                    Integer orderDetailReturnIsNotEnd = returnOrderModelMapper.orderDetailReturnIsNotEnd(storeId, sNo, orderDetails.getId());

                    Integer orderReturnSuccessNum = returnOrderModelMapper.orderReturnSuccessNum(storeId, sNo, orderDetails.getId());
                    if (orderReturnSuccessNum > 0)
                    {
                        orderDetailsNum++;
                    }
                    Integer goodsNum = returnOrderModelMapper.getOrderReturnGoodsNum(storeId, detailId, sNo);

                    ReturnOrderModel returnOrderInfo = returnOrderModelMapper.getReturnOrderInfo(storeId, detailId, sNo);
                    if (Objects.isNull(vo.getIsMch()))
                    {
                        Map<String, Object> orderDetailShowValueList = orderDetailShowValue(otype, detailStatus, orderCommentType, userCanAfter, settlementStatus, orderDetailReturnIsNotEnd, selfLifting, orderReturnSuccessNum,status);
                        if (goodsNum >= 2 || (Objects.nonNull(returnOrderInfo) && (returnOrderInfo.getR_type() == 4 || returnOrderInfo.getR_type() == 9 || returnOrderInfo.getR_type() == 13)) || otype.equals(DictionaryConst.OrdersType.ORDERS_HEADER_FS))
                        {
                            orderDetailShowValueList.remove(OrderShowValueEnum.SQSH.getName());
                        }
                        orderDetailValueMap.putAll(orderDetailShowValueList);
                        if (otype.equals(DictionaryConst.OrdersType.ORDERS_HEADER_FS)) {
                            orderDetailShowValueList.remove(OrderShowValueEnum.CKSH.getName());
                            orderDetailShowValueList.remove(OrderShowValueEnum.SQSH.getName());
                        }
                        productMap.put("get_order_details_button", orderDetailShowValueList);
                    }
                    productList.add(productMap);
                    //统一订单状态
                    this.updateOrderState(vo.getStoreId(), sNo,null);
                    if (orderDetailReturnIsNotEnd > 0)
                    {
                        orderDetailsNum++;
                    }
                    //订单明细是否已经退款成功


                    //如果订单状态是直播的订单
                    if (DictionaryConst.OrdersType.ORDERS_HEADER_ZB.equals(orderModel.getOtype()))
                    {
                        anchor_id = orderDetails.getAnchor_id();
                    }
                }
                if (!comment)
                {
                    commentsType = 3;
                }
                resultMap.put("sale_type", 0);
                int unFinishShouHouOrderNum = returnOrderModelMapper.orderReturnIsNotEnd(storeId, sNo);
                //判断有无订单售后未结束
                if (unFinishShouHouOrderNum > 0)
                {
                    resultMap.put("sale_type", 1);
                }
                //所有订单详情都在售后中/订单明细已经退款成功，不显示批量售后按钮
                if (orderDetailsNum == orderDetailsModelsList.size())
                {
                    resultMap.put("sale_type", 2);
                }
                for (Integer val : batch)
                {
                    if (val == 0)
                    {
                        // 有批量退货按钮
                        batchDel = 0;
                        break;
                    }
                }
            }
            else
            {
                batchDel = 0;
            }
            //更新最新订单状态
            orderModel = orderModelMapper.selectByPrimaryKey(orderModel.getId());
            if (orderModel != null)
            {
                status = orderModel.getStatus();
                cancel_method = orderModel.getCancel_method();
            }
            Integer saleType = MapUtils.getInteger(resultMap, "sale_type");
            //将orderModel转为Map对象 好对里面字段进行修改及添加
            Map<String, Object> orderModelMap = JSON.parseObject(JSONObject.toJSONString(orderModel), new TypeReference<Map<String, Object>>()
            {
            });
            //判断订单是否被删除
            if (orderModel.getUserRecycle() == 2)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERRCODE_DDYBSC, "订单已被删除，无法查看详情", "orderModel");
            }
            String gradeFan = MapUtils.getString(orderModelMap, "grade_fan");
            if (Objects.nonNull(gradeFan))
            {
                orderModelMap.put("grade_fan", new BigDecimal(gradeFan).setScale(2, BigDecimal.ROUND_HALF_DOWN).toString());
            }

            //平台/店铺优惠金额特殊处理
            if (isCouponHandle)
            {
                //拆单后总优惠金额 - 该店铺优惠金额
                preferentialAmount = preferential_amount.subtract(couponPrice);
            }
            //插件使用
            boolean userCanOpen  = true;
            boolean userCanCan   = true;
            boolean userCanMs    = true;
            boolean userCanBuyMs = true;
            boolean isagainCan   = false;
            boolean isagainOpen  = false;
            boolean isinpt       = false;

            Map<String, Object> sellInfo = new HashMap<>(16);
            //预售订单信息
            PreSellRecordModel preSellRecordModel = new PreSellRecordModel();
            preSellRecordModel.setsNo(sNo);
            List<PreSellRecordModel> select = preSellRecordModelMapper.select(preSellRecordModel);
            if (select != null && select.size() > 0)
            {
                preSellRecordModel = select.get(0);
                PreSellGoodsModel preSellGoodsModel = new PreSellGoodsModel();
                preSellGoodsModel.setProduct_id(preSellRecordModel.getProduct_id());
                preSellGoodsModel = preSellGoodsMapper.selectOne(preSellGoodsModel);
                sellInfo.put("sellType", preSellGoodsModel.getSell_type());
                if (preSellGoodsModel.getSell_type() == 1)
                {
                    sellInfo.put("canPay", false);
                    sellInfo.put("deposit", preSellRecordModel.getDeposit());
                    sellInfo.put("balance", preSellRecordModel.getBalance());
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(preSellGoodsModel.getBalance_pay_time());
                    calendar.set(Calendar.HOUR_OF_DAY, 0);
                    calendar.set(Calendar.MINUTE, 0);
                    calendar.set(Calendar.SECOND, 0);
                    Date startTime = calendar.getTime();
                    calendar.add(Calendar.DAY_OF_MONTH, 1);
                    calendar.add(Calendar.SECOND, -1);
                    Date endTime = calendar.getTime();
                    sellInfo.put("startTime", DateUtil.dateFormate(startTime, GloabConst.TimePattern.YMDHMS));
                    sellInfo.put("endTime", DateUtil.dateFormate(endTime, GloabConst.TimePattern.YMDHMS));
                    if (DateUtil.dateCompare(new Date(), startTime) && DateUtil.dateCompare(endTime, new Date()))
                    {
                        sellInfo.put("canPay", true);
                    }
                    sellInfo.put("total", preSellRecordModel.getBalance().add(zFreight));
                }
                else
                {
                    sellInfo.put("endTime", DateUtil.dateFormate(preSellGoodsModel.getDeadline(), GloabConst.TimePattern.YMDHMS));
                    sellInfo.put("total", productTotal.add(zFreight));
                }
                sellInfo.put("deliveryTime", preSellGoodsModel.getDelivery_time());
            }
            //订单详情底部按钮显示
            Map<String, Object> resMap = new HashMap<>();
            Boolean             canPay = MapUtils.getBoolean(sellInfo, "canPay");
            String              keys   = String.format(GloabConst.RedisHeaderKey.ORDER_SHOW_VALUE_KEY, sNo);
            Integer             sellType = MapUtils.getInteger(sellInfo, "sellType");
            Integer             writeOffSettings = MapUtils.getInteger(jsonObject, "write_off_settings");
            //列表按钮
            if (Objects.isNull(vo.getIsMch()))
            {
                int unFinishShouHouOrderNum = returnOrderModelMapper.orderReturnIsNotEnd(storeId, sNo);
                Map<String, Boolean> buttonShow   = publicRefundService.afterSaleButtonShow(storeId, orderModel.getOtype(), orderDetailsModelsList, orderModel, unFinishShouHouOrderNum);
                Boolean              refund        = MapUtils.getBoolean(buttonShow, "refund");
                Boolean              refundShow = MapUtils.getBoolean(buttonShow, "refundShowBtn");
                int count = orderDetailsModelsList.size();
                Map<String, Object> initShowValue = orderShowValue(unFinishShouHouOrderNum, status, haveExpress, commentsType, selfLifting, otype, sellType, invoiceTimeout, refund, refundShow, isInvoice, canPay, count, writeOffSettings, invoicePrice,pay_type,orderModel.getReview_status());
                int cksh = 0;

                for (Map.Entry<String, Object> entry : orderDetailValueMap.entrySet())
                {
                    String key   = entry.getKey();
                    Object value = entry.getValue();
                    if (key.contains(OrderShowValueEnum.SQSH.getName()) && Objects.equals(OrderShowValueEnum.SQSH.getShowValue(), value))
                    {
                        initShowValue.put(key, value);
                    }else if (key.contains(OrderShowValueEnum.CKSH.getName()) && Objects.equals(OrderShowValueEnum.CKSH.getShowValue(), value))
                    {
                        cksh++;
                    }
                }
                if ((!orderDetailValueMap.isEmpty() && cksh == orderDetailValueMap.size()) || initShowValue.containsKey(OrderShowValueEnum.JSTK.getName()))
                {
                    initShowValue.remove(OrderShowValueEnum.SQSH.getName());
                }
                if (otype.equals(DictionaryConst.OrdersType.ORDERS_HEADER_FS) && cksh > 0) {
                    initShowValue.put(OrderShowValueEnum.CKSH.getName(), OrderShowValueEnum.CKSH.getShowValue());
                }
                resultMap.put("get_button_list", initShowValue);
            }

            //平台待客下单，优惠金额为平台优惠
            if (ORDER_TYPE_PT == operationType)
            {
                couponPrice = BigDecimal.ZERO;
                preferentialAmount = manualOffer;
                discountType = "平台优惠";
            }
            Map<String, Object> retMapTmp = new HashMap<>(16);
            if (DictionaryConst.OrdersType.ORDERS_HEADER_ZB.equals(orderModel.getOtype()))
            {
                Map<String, Object> livingMap = livingProductModelMapper.selectLivingById(living_id);
                retMapTmp.put("anchor_name", livingMap.get("user_name"));
                retMapTmp.put("live_img", livingMap.get("live_img"));
            }
            //商家自配处理
            if (selfLifting == 2 && orderDetailsModelsList.get(0).getStore_self_delivery() != null)
            {
                HashMap<String, Object> storeSelfInfo          = new HashMap<>();
                StoreSelfDeliveryModel  storeSelfDeliveryModel = storeSelfDeliveryModelMapper.selectByPrimaryKey(orderDetailsModelsList.get(0).getStore_self_delivery());
                storeSelfInfo.put("delivery_time", DateUtil.dateFormate(storeSelfDeliveryModel.getDelivery_time(), GloabConst.TimePattern.YMD));
                storeSelfInfo.put("delivery_period", storeSelfDeliveryModel.getDelivery_period());
                storeSelfInfo.put("phone", storeSelfDeliveryModel.getPhone());
                storeSelfInfo.put("courier_name", storeSelfDeliveryModel.getCourier_name());
                retMapTmp.put("storeSelfInfo", storeSelfInfo);
            }


            //发票金额
            retMapTmp.put("invoicePrice", zPrice.subtract(orderFreight));
            retMapTmp.put("reason_for_rejection",orderModel.getReason_for_rejection());
            retMapTmp.put("isInvoice", isInvoice);
            retMapTmp.put("invoiceTimeout", invoiceTimeout);
            retMapTmp.put("status", status);
            retMapTmp.putAll(payInfo);
            retMapTmp.put("hand_del", hand_del);
            retMapTmp.put("user_can_Open", userCanOpen);
            retMapTmp.put("user_can_after", userCanAfter);
            retMapTmp.put("user_can_ms", userCanMs);
            retMapTmp.put("user_can_buy_ms", userCanBuyMs);
            retMapTmp.put("user_can_can", userCanCan);
            retMapTmp.put("isagain_can", isagainCan);
            retMapTmp.put("isagain_open", isagainOpen);
            retMapTmp.put("isinpt", isinpt);
            retMapTmp.put("otype", otype);
            retMapTmp.put("remarks", remarks);
            retMapTmp.put("id", orderId);
            retMapTmp.put("sNo", sNo);
            retMapTmp.put("vipGoods",vipGoods);
            retMapTmp.put("message", sNo);
            retMapTmp.put("z_price", zPrice);
            retMapTmp.put("real_sno", real_sno);
            retMapTmp.put("pay_type", pay_type);
            retMapTmp.put("old_total", old_total);
            retMapTmp.put("product_total", productTotal);
            retMapTmp.put("cancel_method", cancel_method);
            //原运费
            retMapTmp.put("old_freight", StringUtils.isEmpty(orderModel.getOld_freight()) ? orderFreight : orderModel.getOld_freight());

            BigDecimal exchangeRate = orderModel.getExchange_rate();
            exchangeRate = Objects.isNull(exchangeRate) ? BigDecimal.ONE : exchangeRate;
            String currencySymbol = orderModel.getCurrency_symbol();
            String currencyCode   = orderModel.getCurrency_code();
            //币种和符号为null，获取商城默认币种
            if (StringUtils.isEmpty(currencySymbol) || StringUtils.isEmpty(currencyCode))
            {
                Map defaultCurrency = currencyStoreModelMapper.getDefaultCurrency(orderModel.getStore_id());
                currencyCode = Objects.isNull(currencyCode) ? (String) defaultCurrency.get("currency_code") : currencyCode;
                currencySymbol = Objects.isNull(currencySymbol) ? (String) defaultCurrency.get("currency_symbol") : currencySymbol;
            }

            retMapTmp.put("currency_code", currencyCode);
            retMapTmp.put("currency_symbol", currencySymbol);
            retMapTmp.put("exchange_rate", exchangeRate);

            retMapTmp.put("mch_name", mchName);
            retMapTmp.put("z_freight", orderFreight);
            retMapTmp.put("name", name);
            retMapTmp.put("mobile", mobile1);
            retMapTmp.put("address", address);
            retMapTmp.put("add_time", DateUtil.dateFormate(addTime, GloabConst.TimePattern.YMDHMS));
            retMapTmp.put("isDfkGbi", isDfkGbi);//是否待付款关闭
            retMapTmp.put("r_status", status);
            retMapTmp.put("list", productList);
            retMapTmp.put("user_money", userMoney);
            retMapTmp.put("user_score", userScore);
            retMapTmp.put("order_failure", orderFailure);
            retMapTmp.put("company", company);
            retMapTmp.put("batch_del", batchDel);
            retMapTmp.put("coupon_activity_name", couponActivityName);
            retMapTmp.put("coupon_price", couponPrice);
            retMapTmp.put("coupon_name", couponName);
            retMapTmp.put("enterless", enterless);
            retMapTmp.put("offset_balance", offset_balance);
            retMapTmp.put("omsg", orderModelMap);
            retMapTmp.put("password_status", passwordStatus);
            retMapTmp.put("comm_discount", "");
//            retMapTmp.put("grade_rate", gradeRate);
            retMapTmp.put("delivery_status", deliveryStatus);
            retMapTmp.put("self_lifting", selfLifting);
//            retMapTmp.put("grade_rate_amount", gradeRateAmount);
            retMapTmp.put("discount_type", discountType);
            retMapTmp.put("preferential_amount", preferentialAmount);
            retMapTmp.put("gstatus", null);
            retMapTmp.put("pttype", null);
            retMapTmp.put("logistics", null);
            retMapTmp.put("pro_id", null);
            retMapTmp.put("order_no", null);
            retMapTmp.put("p_sNo", null);
            retMapTmp.put("allow", orderModel.getAllow());
            retMapTmp.put("is_end", null);
            retMapTmp.put("subtraction_list", null);
            retMapTmp.put("comm_amount", comm_amount);
            retMapTmp.put("haveExpress", haveExpress);
            resultMap.put("sellInfo", sellInfo);
            resultMap.put("allRefund", allRefund);
            //订单是否可以删除标识
            retMapTmp.put("isOrderDelBtn", this.isOrderDelButtonShow(orderModel.getId()));
            retMapTmp.put("wx_order_status", orderModel.getWx_order_status());
            resultMap.putAll(retMapTmp);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("订单详情 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "orderDetails");
        }
        return resultMap;
    }

    @Override
    public BigDecimal getOrderPrice(int orderDetailId, int storeId) throws LaiKeAPIException
    {
        try
        {
            //判断单个商品退款是否有使用优惠
            Map<String, Object> returnInfo = orderModelMapper.getReturnOrderInfo(storeId, orderDetailId);
            //订单号
            String sNo = MapUtils.getString(returnInfo, "sNo");

            //计算汇率
//            String exchangeRate = MapUtils.getString(returnInfo, "exchange_rate");
//            if (StringUtils.isNotEmpty(exchangeRate)){
//                //如果有汇率则转换成原价
//                BigDecimal rate = new BigDecimal(exchangeRate);
//                returnInfo.put("after_discount", new BigDecimal(MapUtils.getString(returnInfo, "after_discount"))
//                        .multiply(rate).setScale(2, RoundingMode.HALF_UP));
//            }

//            Integer express_id = DataUtils.getIntegerVal(returnInfo, "express_id", 0);
            //商品优惠后金额即实际支付金额
            BigDecimal after_discount = DataUtils.getBigDecimalVal(returnInfo, "after_discount");
            //计算实际支付金额
            BigDecimal price;
//            判断是否发货
//            if (freight.compareTo(BigDecimal.ZERO) > 0 && express_id != null && express_id != 0) {
//                //计算实际支付金额
//                price = after_discount;
//            } else {
            //计算实际支付金额
            //2023/08/03优化  运费会在售后审核中决定退不退  所有申请售后只会计算除去运费的价格
            if (sNo.contains(DictionaryConst.OrdersType.ORDERS_HEADER_GM) || sNo.contains(DictionaryConst.OrdersType.ORDERS_HEADER_ZB))
            {
                price = after_discount;
            }
            else if (sNo.contains(DictionaryConst.OrdersType.ORDERS_HEADER_VI))
            {
                ProductListModel productListModel = productListModelMapper.selectByPrimaryKey(DataUtils.getIntegerVal(returnInfo, "p_id"));
                //无需核销不按次数计算
                if (productListModel.getWrite_off_settings().equals(ProductListModel.WRITE_OFF_SETTINGS.dispenseWith))
                {
                    price = after_discount;
                }
                else
                {
                    //虚拟商品按可核销次数退款
                    Integer write_off_num       = DataUtils.getIntegerVal(returnInfo, "write_off_num");
                    Integer after_write_off_num = DataUtils.getIntegerVal(returnInfo, "after_write_off_num");
                    price = after_discount.divide(new BigDecimal(write_off_num + after_write_off_num), 5, RoundingMode.HALF_UP).multiply(new BigDecimal(write_off_num)).setScale(2, RoundingMode.HALF_UP);
                }
            }
            else
            {
                //在申请入口方法处理运费这里不处理
                price = after_discount;
            }
            return price;
        }
        catch (Exception e)
        {
            logger.error("获取订单信息异常:", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_HQDDXXYC, "获取订单信息异常", "getOrderPrice");
        }
    }

    @Override
    public Map payBackUpOrder(OrderModel orderModel) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = Maps.newHashMap();
        try
        {
            /** 订单号 */
            String sNo = orderModel.getsNo();
            logger.info("支付回调更新订单{}开始", sNo);
            logger.info("订单信息：{}", JSON.toJSONString(orderModel));
            //1.数据准备
            /** 用户id */
            String userId = orderModel.getUser_id();
            /** 订单金额 */
            BigDecimal zPrice = orderModel.getZ_price();
            /** 微信支付单号 */
            String tradeNo = orderModel.getTrade_no();
            /** 支付类型 */
            String pay = orderModel.getPay();
            /** 商城id */
            int storeId = orderModel.getStore_id();
            //第三方唯一id
            String transactionId = orderModel.getTransaction_id();
            //分账状态
            Integer dividend_status = orderModel.getDividend_status();
            //业务流程
            if (orderModel.getsNo().contains(DictionaryConst.OrdersType.ORDERS_HEADER_PT))
            {
                //拼团订单支付流程
                PayVo payVo = new PayVo();
                payVo.setStoreId(storeId);
                payVo.setPayType(pay);
                payVo.setOrderno(sNo);
                payVo.setUserId(userId);
                payVo.setTradeNo(tradeNo);
                groupPayService.payment(payVo);
            }
            else if (orderModel.getsNo().contains(DictionaryConst.OrdersType.PTHD_ORDER_PP))
            {
                //平台拼团订单支付流程
                PayVo payVo = new PayVo();
                payVo.setStoreId(storeId);
                payVo.setPayType(pay);
                payVo.setOrderno(sNo);
                payVo.setUserId(userId);
                payVo.setTradeNo(tradeNo);
                ptGroupPayService.payment(payVo);
            }
            else if (orderModel.getsNo().contains(DictionaryConst.OrdersType.ORDERS_HEADER_FX))
            {
                //修改订单状态
                orderPaymentForOther(orderModel.getStore_id(), orderModel.getsNo(), tradeNo, userId, pay);
                //分销支付流程
                PayVo payVo = new PayVo();
                payVo.setStoreId(storeId);
                payVo.setPayType(pay);
                payVo.setOrderno(sNo);
                payVo.setUserId(userId);
                payVo.setTradeNo(tradeNo);
                distributionPayService.payment(payVo);
            }
            else
            {
                //如果是积分订单,则扣除积分
                if (DictionaryConst.OrdersType.ORDERS_HEADER_IN.equalsIgnoreCase(orderModel.getOtype()))
                {
                    User user = new User();
                    user.setUser_id(userId);
                    user = userBaseMapper.selectOne(user);
                    //获取积分
                    OrderDetailsModel orderDetailsModel = new OrderDetailsModel();
                    orderDetailsModel.setR_sNo(orderModel.getsNo());
                    orderDetailsModel = orderDetailsModelMapper.selectOne(orderDetailsModel);
//                    publicPaymentService.integralPay(user.getUser_id(), orderDetailsModel.getAfter_discount().multiply(new BigDecimal(orderDetailsModel.getNum())), sNo, orderModel.getZ_price(), user.getAccess_id());
                    publicPaymentService.integralPay(user.getUser_id(), new BigDecimal(orderDetailsModel.getScoreDeduction()).multiply(new BigDecimal(orderDetailsModel.getNum())), sNo, orderModel.getZ_price(), user.getAccess_id());
                }
                //普通订单流程
                if (DictionaryConst.OrdersType.ORDERS_HEADER_GM.equalsIgnoreCase(orderModel.getOtype()) || DictionaryConst.OrdersType.ORDERS_HEADER_VI.equalsIgnoreCase(orderModel.getOtype()))
                {
                    resultMap = orderPayment(orderModel.getStore_id(), orderModel.getsNo(), tradeNo, userId, pay,orderModel.getOtype());
                }
                else
                {
                    orderPaymentForOther(orderModel.getStore_id(), orderModel.getsNo(), tradeNo, userId, pay);
                }
            }
            //会员生日特权  赠送积分
            if (DictionaryConst.OrdersType.ORDERS_HEADER_GM.equals(orderModel.getOtype()) ||
                    DictionaryConst.OrdersType.ORDERS_HEADER_VI.equalsIgnoreCase(orderModel.getOtype()))
            {
                publiceMemberService.doublePoints(userId, storeId, zPrice, sNo);
            }
            List<OrderModel> orderModelList;
            //订单是否多店铺已经拆单
            OrderModel order = new OrderModel();
            order.setStore_id(storeId);
            order.setUser_id(userId);
            order.setP_sNo(sNo);
            orderModelList = orderModelMapper.select(order);
            if (DictionaryConst.OrdersType.ORDERS_HEADER_GM.equalsIgnoreCase(orderModel.getOtype()) || DictionaryConst.OrdersType.ORDERS_HEADER_VI.equalsIgnoreCase(orderModel.getOtype()))
            {
                orderModelList = JSON.parseObject(JSON.toJSONString(resultMap.get("list")), new TypeReference<List<OrderModel>>()
                {
                });
                orderModel = JSON.parseObject(JSON.toJSONString(resultMap.get("order")), new TypeReference<OrderModel>()
                {
                });
            }
            //单店铺供应商商品和普通商品同时下单-父订单为普通商品订单
            /*if (orderModel.getStatus().equals(ORDERS_R_STATUS_UNPAID)&&!orderModel.getOtype().equals(DictionaryConst.OrdersType.ORDERS_HEADER_VI)){
                String mchId = StringUtils.trim(orderModel.getMch_id(), SplitUtils.DH);
                // 店铺id字符串
                List<String> shopIds = Splitter.on(SplitUtils.DH).splitToList(mchId);
                if (shopIds.size() == 1){
                    orderModelList.add(orderModel);
                }else {
                    //不为单店铺?
                    logger.error("订单号:" + sNo);
                    logger.error("单店铺供应商商品和普通商品同时下单-父订单为普通商品订单");
                    logger.error("父订单订单状态为待付款订单");
                    throw new LaiKeAPIException(API_OPERATION_FAILED, "支付失败", "pay");
                }
            }*/

            if (StringUtils.isEmpty(orderModelList) || orderModelList.size() <= 0)
            {
                orderModelList.add(orderModel);
            }

            // 自提订单在支付成功后需要同步微信发货信息（只同步一次）
            String  wxSyncSno     = null;
            Integer wxSyncStoreId = null;
            for (OrderModel Order : orderModelList)
            {
                //是否为供应商订单
                int supplierOrder = 0;
                storeId = Order.getStore_id();
                sNo = Order.getsNo();
                if (wxSyncSno == null
                        && DictionaryConst.OrderPayType.ORDERPAYTYPE_MINI_WECHAT.equals(pay)
                        && Order.getSelf_lifting() != null
                        && Order.getSelf_lifting() == OrderModel.SELF_LIFTING_PICKED_UP)
                {
                    wxSyncSno = sNo;
                    wxSyncStoreId = storeId;
                }
                // 支付完成 增加商品销量
                List<Map<String, Object>> orderProductsInfoList = orderModelMapper.getOrderProductInfo(storeId, sNo);
                if (!CollectionUtils.isEmpty(orderProductsInfoList))
                {
                    for (Map<String, Object> orderProductsInfoMap : orderProductsInfoList)
                    {
                        Integer pid              = DataUtils.getIntegerVal(orderProductsInfoMap, "goodsId");
                        Integer num              = DataUtils.getIntegerVal(orderProductsInfoMap, "num");
                        Integer supplierSuperior = MapUtils.getInteger(orderProductsInfoMap, "supplier_superior");
                        int     row              = 0;
                        if (sNo.contains(DictionaryConst.OrdersType.ORDERS_HEADER_PS))
                        {
                            PreSellRecordModel preSellRecordModel = new PreSellRecordModel();
                            preSellRecordModel.setsNo(sNo);
                            List<PreSellRecordModel> select = preSellRecordModelMapper.select(preSellRecordModel);
                            if (select.size() == 1 && Objects.isNull(select.get(0).getPay_type()))
                            {
                                row = productListModelMapper.updateProductListVolume(num, storeId, pid);
                            }
                        }
                        else
                        {
                            row = productListModelMapper.updateProductListVolume(num, storeId, pid);
                            //如果为供应商商品同时增加供应商商品销量
                            if (!Objects.isNull(supplierSuperior))
                            {
                                row = productListModelMapper.updateProductListVolume(num, storeId, supplierSuperior);
                                //供应商订单提醒
                                supplierOrder = 2;
                            }
                        }
                        if (row < 1)
                        {
                            logger.error("订单{}支付成功,商品id【{}】销量增加【{}】失败", sNo, pid, num);
                        }
                        else
                        {
                            logger.error("订单{}支付成功,商品id【{}】销量增加【{}】成功", sNo, pid, num);
                        }
                        //如果是小程序支付，修改订单中的transaction_id和dividend_status
                        if (DictionaryConst.OrderPayType.ORDERPAYTYPE_MINI_WECHAT.equals(orderModel.getPay()))
                        {
                            orderModelMapper.updateIdAndStatusBySno(sNo, transactionId, dividend_status);
                            logger.error("getDelivery_status:{}", dividend_status);
                        }
                    }
                }
                //虚拟商品无需发货
                if (Order.getMch_id() != null && !DictionaryConst.OrdersType.ORDERS_HEADER_VI.equals(Order.getOtype()))
                {
                    //通知后台消息
                    MessageLoggingModal messageLoggingSave = new MessageLoggingModal();
                    messageLoggingSave.setStore_id(Order.getStore_id());
                    String[] mchId = StringUtils.trim(Order.getMch_id(), SplitUtils.DH).split(SplitUtils.DH);
                    messageLoggingSave.setMch_id(Integer.parseInt(mchId[0]));
                    messageLoggingSave.setType(MessageLoggingModal.Type.TYPE_ORDER_WAIT_SEND);
                    messageLoggingSave.setTo_url(com.laiketui.common.utils.tool.data.OrderDataUtils.getOrderRoute(Order.getOtype(), supplierOrder));
                    messageLoggingSave.setParameter(Order.getId() + "");
                    messageLoggingSave.setContent(String.format("订单%s已支付成功，正等待发货中，请及时发货！", Order.getsNo()));
                    messageLoggingSave.setAdd_date(new Date());
                    messageLoggingModalMapper.insertSelective(messageLoggingSave);
                }
            }
            if (wxSyncSno != null && wxSyncStoreId != null)
            {
                // 仅对自提且小程序支付的订单同步微信发货信息
                MainVo wxVo = new MainVo();
                wxVo.setStoreId(wxSyncStoreId);
                publicExpressService.setWxAppUploadShippingInfo(wxVo, wxSyncSno);
            }
//            if (DictionaryConst.OrdersType.ORDERS_HEADER_GM.equals(orderModel.getOtype())) {
//                publicMemberService.memberSettlement(storeId, userId, sNo, zPrice, IntegralConfigModel.GiveStatus.PAYMENT);
//            }
            User user = new User();
            user.setUser_id(userId);
            user = userBaseMapper.selectOne(user);
            //刷新缓存
            RedisDataTool.refreshRedisUserCache(user, redisUtil);

            //站内推送退款信息
            SystemMessageModel systemMessageSave = new SystemMessageModel();
            systemMessageSave.setType(SystemMessageModel.ReadType.UNREAD);
            systemMessageSave.setSenderid("admin");
            systemMessageSave.setStore_id(user.getStore_id());
            systemMessageSave.setRecipientid(user.getUser_id());
            systemMessageSave.setTitle("系统消息");
            systemMessageSave.setContent("您的宝贝马上就会发货啦!");
            systemMessageSave.setTime(new Date());
            systemMessageModelMapper.insertSelective(systemMessageSave);


            //TODO 短信发送
            // generate_code($db, $mobile, 1, 8, array("sNo" => $order_id));
            resultMap.put("code", 200);
            resultMap.put("message", "success");
            logger.info("支付回调更新订单完成");
        }
        catch (LaiKeAPIException l)
        {
            logger.error("支付回调出问题 自定义异常", l);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_ZFHDCWT, "支付回调出问题", "payBackUpOrder");
        }
        catch (Exception e)
        {
            logger.error("支付回调出问题 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_ZFHDCWT, "支付回调出问题", "payBackUpOrder");
        }
        return resultMap;
    }

    @Override
    public Map payBackUpOrderMember(OrderDataModel orderDataModel) throws LaiKeAPIException
    {
        Map resultMap = Maps.newHashMap();
        try
        {
            if (OrderDataModel.PayStatus.NOT_PAY.equals(orderDataModel.getStatus()))
            {
                /** 订单号 */
                String sNo = orderDataModel.getTrade_no();
                logger.info("支付回调更新订单{}开始", sNo);
                logger.info("订单信息：{}", JSON.toJSONString(orderDataModel));
                String data = orderDataModel.getData();
                Map    map  = SerializePhpUtils.getUnserializeObj(data, Map.class);
                //1.数据准备
                /** 用户id */
                String userId = DataUtils.getStringVal(map, "user_id");
                /** 订单金额 */
                BigDecimal zPrice = DataUtils.getBigDecimalVal(map, "total");
                /** 商城id */
                int storeId = DataUtils.getIntegerVal(map, "store_id");
                int row     = orderDataModelMapper.updateStatus(orderDataModel.getId(), 1);
                logger.info("更新数据条数：{}", row);

                User user = new User();
                user.setUser_id(userId);
                user = userBaseMapper.selectOne(user);
                userMapper.updateUserMoney(zPrice, storeId, userId);

                user = userBaseMapper.selectByPrimaryKey(user.getId());
                //添加充值记录详情
                RecordDetailsModel recordDetailsModel = new RecordDetailsModel();
                recordDetailsModel.setStore_id(storeId);
                recordDetailsModel.setMoney(zPrice);
                recordDetailsModel.setUserMoney(user.getMoney());
                recordDetailsModel.setMoneyType(RecordDetailsModel.moneyType.INCOME);
                recordDetailsModel.setMoneyTypeName(RecordDetailsModel.moneyTypeName.USER_BALANCE_RECHARGE);
                recordDetailsModel.setType(RecordDetailsModel.type.USER_BALANCE_RECHARGE);
                recordDetailsModel.setRecordTime(new Date());
                recordDetailsModel.setTypeName(orderDataModel.getPay_type());
                recordDetailsModel.setAddTime(new Date());
                String remarks = DataUtils.getStringVal(map, "remarks");
                if (StringUtils.isNotEmpty(remarks))
                {
                    recordDetailsModel.setRecordNotes(remarks);
                }

                Map<String, Object> userCurrecyMap  = currencyStoreModelMapper.getCurrencyInfo(storeId, user.getPreferred_currency());
                Map<String, Object> storeCurrecyMap = currencyStoreModelMapper.getDefaultCurrency(storeId);
                recordDetailsModel.setCurrency_code(DataUtils.getStringVal(userCurrecyMap, "currency_code", DataUtils.getStringVal(storeCurrecyMap, "currency_code")));
                recordDetailsModel.setCurrency_symbol(DataUtils.getStringVal(userCurrecyMap, "currency_symbol", DataUtils.getStringVal(storeCurrecyMap, "currency_symbol")));
                recordDetailsModel.setExchange_rate(DataUtils.getBigDecimalVal(userCurrecyMap, "exchange_rate", DataUtils.getBigDecimalVal(storeCurrecyMap, "exchange_rate")));


                recordDetailsModelMapper.insert(recordDetailsModel);

                RecordModel recordModel = new RecordModel();
                recordModel.setStore_id(storeId);
                recordModel.setUser_id(user.getUser_id());
                recordModel.setMoney(zPrice);
                recordModel.setOldmoney(user.getMoney());
                recordModel.setEvent(String.format("充值%s元", zPrice));
                recordModel.setType(RecordModel.RecordType.RECHARGE);
                recordModel.setAdd_date(new Date());
                recordModel.setDetails_id(recordDetailsModel.getId());
                recordModelMapper.insertSelective(recordModel);

                //TODO 短信发送
                // generate_code($db, $mobile, 1, 8, array("sNo" => $order_id));
                resultMap.put("code", 200);
                resultMap.put("message", "success");
                logger.info("支付回调更新订单完成");
            }
            else
            {
                logger.info("此订单已更新处理成功");
            }
        }
        catch (Exception e)
        {
            logger.error("支付回调出问题 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_ZFHDCWT, "支付回调出问题", "payBackUpOrder");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> payBackForMember(OrderDataModel orderDataModel) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            if (OrderDataModel.PayStatus.NOT_PAY.equals(orderDataModel.getStatus()))
            {
                String sNo = orderDataModel.getTrade_no();
                logger.info("支付回调更新订单{}开始", sNo);
                logger.info("订单信息：{}", JSON.toJSONString(orderDataModel));
                String        data          = orderDataModel.getData();
                MemberOrderVo memberOrderVo = JSON.parseObject(data, MemberOrderVo.class);
                Integer       storeId       = memberOrderVo.getStoreId();
                int           row           = orderDataModelMapper.updateStatus(orderDataModel.getId(), 1);
                logger.info("更新数据条数：{}", row);
                //更新用户信息
                User user = new User();
                user.setUser_id(memberOrderVo.getUserId());
                user = userBaseMapper.selectOne(user);
                SystemMessageModel systemMessageSave = new SystemMessageModel();
                //消息通知
                if (user.getGrade().equals(User.MEMBER))
                {
                    //站内推送发货信息
                    systemMessageSave.setType(SystemMessageModel.ReadType.UNREAD);
                    systemMessageSave.setSenderid("admin");
                    systemMessageSave.setStore_id(user.getStore_id());
                    systemMessageSave.setRecipientid(user.getUser_id());
                    systemMessageSave.setTitle("系统消息");
                    systemMessageSave.setContent("会员续费成功，您的会员权益已恢复！");
                    systemMessageSave.setTime(new Date());
                    systemMessageModelMapper.insertSelective(systemMessageSave);
                }
                else
                {
                    //站内推送退款信息
                    systemMessageSave.setType(SystemMessageModel.ReadType.UNREAD);
                    systemMessageSave.setSenderid("admin");
                    systemMessageSave.setStore_id(user.getStore_id());
                    systemMessageSave.setRecipientid(user.getUser_id());
                    systemMessageSave.setTitle("系统消息");
                    systemMessageSave.setContent("会员开通成功，您可以开始享受会员权益了！");
                    systemMessageSave.setTime(new Date());
                    systemMessageModelMapper.insertSelective(systemMessageSave);
                }
                //开通会员赠送积分
                publiceMemberService.sendPoints(user, memberOrderVo.getMemberType(), storeId, orderDataModel);
                if (!user.getGrade().equals(User.MEMBER))
                {
                    user.setGrade_add(new Date());
                }
                user.setGrade(User.MEMBER);
                user.setGrade_m(String.valueOf(memberOrderVo.getMemberType()));
                user.setGrade_end(DateUtil.dateFormateToDate(memberOrderVo.getEndTime(), GloabConst.TimePattern.YMDHMS));
                user.setIs_out(String.valueOf(DictionaryConst.WhetherMaven.WHETHER_NO));
                user.setIs_box(String.valueOf(DictionaryConst.WhetherMaven.WHETHER_OK));
                userBaseMapper.updateByPrimaryKeySelective(user);

                //更新优惠券
                if (StringUtils.isNotEmpty(memberOrderVo.getCouponId()))
                {
                    CouponModal couponModal = couponModalMapper.selectByPrimaryKey(memberOrderVo.getCouponId());
                    if (!Objects.isNull(couponModal))
                    {
                        couponModal.setType(CouponModal.COUPON_TYPE_USED);
                        couponModalMapper.updateByPrimaryKeySelective(couponModal);
                    }
                    int add = publicCouponService.couponWithOrder(storeId, memberOrderVo.getUserId(), memberOrderVo.getCouponId(), sNo, "add");
                    if (add == 0)
                    {
                        //回滚删除已经创建的订单
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TJYHQGLDDSJSB, "添加优惠券关联订单数据失败", "payBackForMember");
                    }
                }
                //TODO 短信发送
                resultMap.put("code", 200);
                resultMap.put("message", "success");
                logger.info("支付回调更新订单完成");
            }
            else
            {
                logger.info("此订单已更新处理成功");
            }
        }
        catch (Exception e)
        {
            logger.error("支付回调出问题 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_ZFHDCWT, "支付回调出问题", "payBackUpOrder");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> payBackUpOrderMchPromise(OrderDataModel orderDataModel) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = Maps.newHashMap();
        try
        {
            String sNo = orderDataModel.getTrade_no();
            logger.info("【店铺保证金】支付回调更新订单{}开始", sNo);
            logger.info("订单信息：{}", JSON.toJSONString(orderDataModel));
            String              data = orderDataModel.getData();
            Map<String, Object> map  = JSON.parseObject(data, Map.class);
            logger.info("map{}", map);
//            Map<String, Object> map = DataUtils.cast(SerializePhpUtils.getUnserializeObj(data, Map.class));
            if (map == null)
            {
                logger.error("订单信息数据出错");
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_ZFHDCWT, "支付回调出问题", "payBackUpOrderMchPromise");
            }
            int row = orderDataModelMapper.updateStatus(orderDataModel.getId(), OrderDataModel.PayStatus.PAYMENT);
            if (row < 1)
            {
                logger.error("支付失败 修改临时订单状态失败");
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_ZFHDCWT, "支付回调出问题", "payBackUpOrderMchPromise");
            }
            //用户id
            String userId = DataUtils.getStringVal(map, "user_id");
            //商城id
            int    storeId = DataUtils.getIntegerVal(map, "storeId");
            MainVo vo      = new MainVo();
            vo.setStoreId(storeId);

            User user = new User();
            user.setUser_id(userId);
            user = userBaseMapper.selectOne(user);
            vo.setAccessId(user.getAccess_id());
            //支付保证金
            publicMchService.paymentPromise(vo, orderDataModel.getOrder_type(), null, user.getUser_id(), sNo);
        }
        catch (Exception e)
        {
            logger.error("支付回调出问题 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_ZFHDCWT, "支付回调出问题", "payBackUpOrder");
        }
        return resultMap;
    }


    @Override
    public Map<String, Object> payBackUpOrderAuctionPromise(OrderDataModel orderDataModel) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = Maps.newHashMap();
        try
        {
            String sNo = orderDataModel.getTrade_no();
            logger.info("【竞拍保证金】支付回调更新订单{}开始", sNo);
            logger.info("订单信息：{}", JSON.toJSONString(orderDataModel));
            PromiseOrderVo promiseOrderVo = JSON.parseObject(orderDataModel.getData(), PromiseOrderVo.class);
            logger.info("map{}", orderDataModel.getData());
            int row = orderDataModelMapper.updateStatus(orderDataModel.getId(), OrderDataModel.PayStatus.PAYMENT);
            if (row < 1)
            {
                logger.error("支付失败 修改临时订单状态失败");
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_ZFHDCWT, "支付回调出问题", "payBackUpOrderMchPromise");
            }
            if (com.laiketui.core.lktconst.DictionaryConst.OrderPayType.ORDERPAYTYPE_WALLET_PAY.equals(promiseOrderVo.getPay()))
            {
                String token = redisUtil.get(com.laiketui.core.lktconst.GloabConst.RedisHeaderKey.LOGIN_ACCESS_FLAG + promiseOrderVo.getUser_id()) + "";
                publicPaymentService.walletPay(promiseOrderVo.getUser_id(), promiseOrderVo.getPaymentAmt(), token, sNo);
            }
            //支付保证金
            publicAuctionService.paymentPromise(promiseOrderVo, sNo);
            Map<String, Object> mchInfoBySpecialId = auctionProductModelMapper.getMchInfoBySpecialId(promiseOrderVo.getSpecialId());
            //h5店铺消息推送
            MessageLoggingModal messageLoggingSave = new MessageLoggingModal();
            messageLoggingSave.setMch_id(MapUtils.getInteger(mchInfoBySpecialId, "mchId"));
            messageLoggingSave.setStore_id(promiseOrderVo.getStoreId());
            messageLoggingSave.setType(MessageLoggingModal.Type.TYPE_H5MCH_AUCTION_DEPOSIT_REMINDER);
            messageLoggingSave.setTo_url("");
            messageLoggingSave.setParameter("用户已缴纳竟拍保证金");
            messageLoggingSave.setContent(String.format("ID为%s的用户已缴纳竟拍专场保证金", promiseOrderVo.getUser_id()));
            messageLoggingSave.setAdd_date(new Date());
            messageLoggingModalMapper.insertSelective(messageLoggingSave);
            //PC店铺消息推送
            messageLoggingSave = new MessageLoggingModal();
            messageLoggingSave.setMch_id(MapUtils.getInteger(mchInfoBySpecialId, "mchId"));
            messageLoggingSave.setStore_id(promiseOrderVo.getStoreId());
            messageLoggingSave.setType(MessageLoggingModal.Type.TYPE_PCMCH_BAIL);
            messageLoggingSave.setTo_url(MessageLoggingModal.PcMchUrl.AUCTION_DEPOSIT_REMINDER);
            messageLoggingSave.setParameter(promiseOrderVo.getSpecialId() + "");
            messageLoggingSave.setContent(String.format("ID为%s的用户已缴纳竟拍专场保证金", promiseOrderVo.getUser_id()));
            messageLoggingSave.setAdd_date(new Date());
            messageLoggingModalMapper.insertSelective(messageLoggingSave);
        }
        catch (Exception e)
        {
            logger.error("支付回调出问题 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_ZFHDCWT, "支付回调出问题", "payBackUpOrderAuctionPromise");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> payBackForPreSell(PaymentVo vo, OrderModel orderModel) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = Maps.newHashMap();
        try
        {
            PreSellRecordModel preSellRecordModel = new PreSellRecordModel();
            PreSellGoodsModel  preSellGoodsModel  = new PreSellGoodsModel();
            if (vo.getPayTarget() == 1)
            {
                preSellRecordModel.setsNo(orderModel.getsNo());
                preSellRecordModel.setPay_type(PreSellRecordModel.DEPOSIT);
                preSellRecordModel = preSellRecordModelMapper.selectOne(preSellRecordModel);
                if (preSellRecordModel == null)
                {
                    throw new LaiKeAPIException(API_OPERATION_FAILED, "请先支付定金", "pay");
                }
                preSellGoodsModel.setProduct_id(preSellRecordModel.getProduct_id());
                preSellGoodsModel = preSellGoodsMapper.selectOne(preSellGoodsModel);
//                // 生成订单号
//                String sNo = createOrderNo(DictionaryConst.OrdersType.ORDERS_HEADER_PS);
//                // 生成支付订单号
//                String realSno = createOrderNo(DictionaryConst.OrdersType.ORDERS_HEADER_PS);
                preSellRecordModel.setPrice(new BigDecimal(vo.getPayment_money()));
                preSellRecordModel.setIs_pay(DictionaryConst.WhetherMaven.WHETHER_OK);
                preSellRecordModel.setAdd_time(new Date());
                preSellRecordModel.setPay(vo.getPayType());
                OrderModel        orderInfo         = JSON.parseObject(preSellRecordModel.getOrder_info(), OrderModel.class);
                OrderDetailsModel orderDetailsModel = JSON.parseObject(preSellRecordModel.getOrder_details_info(), OrderDetailsModel.class);
                orderInfo.setPay(vo.getPayType());
                orderModelMapper.insertSelective(orderInfo);
                orderDetailsModelMapper.insertSelective(orderDetailsModel);
                // 支付完成 增加商品销量
                int row = productListModelMapper.updateProductListVolume(preSellRecordModel.getNum(), vo.getStoreId(), preSellRecordModel.getProduct_id());
                if (row < 1)
                {
                    logger.error("订单{}支付成功,商品id【{}】销量增加【{}】失败", orderInfo.getsNo(), preSellRecordModel.getProduct_id(), preSellRecordModel.getNum());
                }
                else
                {
                    logger.error("订单{}支付成功,商品id【{}】销量增加【{}】成功", orderInfo.getsNo(), preSellRecordModel.getProduct_id(), preSellRecordModel.getNum());
                }
                //新增未支付尾款的预售订单信息
                PreSellRecordModel balance = new PreSellRecordModel();
                BeanUtils.copyProperties(preSellRecordModel, balance);
                balance.setId(null);
                balance.setPrice(BigDecimal.ZERO);
                balance.setPay_type(PreSellRecordModel.BALANCE);
                balance.setIs_pay(DictionaryConst.WhetherMaven.WHETHER_NO);
                balance.setAdd_time(new Date());
                balance.setReal_sno(publicOrderService.createOrderNo(DictionaryConst.OrdersType.ORDERS_HEADER_PS));
                balance.setPay(null);
                balance.setOrder_info(null);
                balance.setOrder_details_info(null);
                preSellRecordModelMapper.insertSelective(balance);
                logger.error("预售记录表插入操作触发  7770！！！");
                //订金模式站内通知  订金模式站内通知放在支付回调逻辑里  因为订金模式订单没付款订金是不产生待付款订单
                MessageLoggingModal messageLoggingSave = new MessageLoggingModal();
                messageLoggingSave.setMch_id(Integer.valueOf(StringUtils.trim(orderInfo.getMch_id(), SplitUtils.DH)));
                messageLoggingSave.setStore_id(vo.getStoreId());
                messageLoggingSave.setType(MessageLoggingModal.Type.TYPE_ORDER_NEW);
                messageLoggingSave.setTo_url(com.laiketui.common.utils.tool.data.OrderDataUtils.getOrderRoute(DictionaryConst.OrdersType.ORDERS_HEADER_PS, 0));
                messageLoggingSave.setParameter(orderInfo.getId() + "");
                messageLoggingSave.setContent(String.format("您来新订单了，订单为%s，请及时处理！", orderModel.getsNo()));
                messageLoggingSave.setAdd_date(new Date());
                messageLoggingModalMapper.insertSelective(messageLoggingSave);
            }
            else if (vo.getPayTarget() == 2)
            {
                preSellRecordModel.setsNo(orderModel.getsNo());
                preSellRecordModel.setPay_type(PreSellRecordModel.BALANCE);
                preSellRecordModel.setIs_pay(DictionaryConst.WhetherMaven.WHETHER_NO);
                preSellRecordModel = preSellRecordModelMapper.selectOne(preSellRecordModel);
                preSellRecordModel.setPrice(new BigDecimal(vo.getPayment_money()));
                preSellRecordModel.setIs_pay(DictionaryConst.WhetherMaven.WHETHER_OK);
                preSellRecordModel.setPay_balance_time(new Date());
                preSellRecordModel.setAdd_time(new Date());
                preSellRecordModel.setPay(vo.getPayType());
                resultMap = payBackUpOrder(orderModel);
            }
            else
            {
                preSellRecordModel.setsNo(orderModel.getsNo());
                preSellRecordModel = preSellRecordModelMapper.selectOne(preSellRecordModel);
                preSellRecordModel.setPrice(preSellRecordModel.getPrice().add(new BigDecimal(vo.getPayment_money())));
                preSellRecordModel.setIs_pay(DictionaryConst.WhetherMaven.WHETHER_OK);
                preSellRecordModel.setPay(vo.getPayType());
                resultMap = payBackUpOrder(orderModel);
                //当订货预售商品剩余数量达到零时，商品自动下架
                preSellGoodsModel.setProduct_id(preSellRecordModel.getProduct_id());
                preSellGoodsModel = preSellGoodsMapper.selectOne(preSellGoodsModel);
                if (preSellGoodsModel.getSurplus_num() == 0)
                {
                    ProductListModel productListModel = productListModelMapper.selectByPrimaryKey(preSellGoodsModel.getProduct_id());
                    productListModel.setStatus(String.valueOf(DictionaryConst.GoodsStatus.OFFLINE_GROUNDING));
                    productListModelMapper.updateByPrimaryKey(productListModel);
                }
            }
            preSellGoodsMapper.updateByPrimaryKey(preSellGoodsModel);
            preSellRecordModelMapper.updateByPrimaryKey(preSellRecordModel);
        }
        catch (Exception e)
        {
            logger.error("预售支付回调 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YSZFHDYC, "预售支付回调异常", "payBackForPreSell");
        }
        return resultMap;
    }

    @Override
    public OrderModel getOrderInfo(String orderNo) throws LaiKeAPIException
    {
        OrderModel orderModel;
        try
        {
            orderModel = getOrderInfo(orderNo, null, null);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取订单信息 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_HQDDXXYC, "获取订单信息 异常", "getOrderInfo");
        }
        return orderModel;
    }

    @Override
    public OrderModel getOrderInfo(String orderNo, PaymentVo paymentVo, String userId) throws LaiKeAPIException
    {
        OrderModel orderModel = new OrderModel();
        try
        {
            //获取订单信息
            if (orderNo.contains(DictionaryConst.OrdersType.ORDERS_HEADER_PT))
            {
                OrderDataModel orderDataModel = new OrderDataModel();
                orderDataModel.setTrade_no(orderNo);
                orderDataModel = orderDataModelMapper.selectOne(orderDataModel);
                Map<String, Object> goodsInfo = JSON.parseObject(orderDataModel.getData(), new TypeReference<Map<String, Object>>()
                {
                });
                orderModel.setsNo(orderDataModel.getTrade_no());
                orderModel.setReal_sno(orderDataModel.getTrade_no());
                orderModel.setZ_price(new BigDecimal(goodsInfo.get("z_price").toString()));
                orderModel.setStore_id((Integer) goodsInfo.get("store_id"));
            }
            else if (orderNo.contains(DictionaryConst.OrdersType.PTHD_ORDER_PP))
            {
                PtGoGroupOrderModel ptGoGroupOrderModel = new PtGoGroupOrderModel();
                ptGoGroupOrderModel.setsNo(orderNo);
//                ptGoGroupOrderModel = ptGoGroupOrderModelMapper.selectOne(ptGoGroupOrderModel);
                orderModel = JSON.parseObject(JSON.toJSONString(ptGoGroupOrderModel), OrderModel.class);
            }
            else if (orderNo.contains(DictionaryConst.OrdersType.ORDERS_HEADER_KJ))
            {
                orderModel.setReal_sno(orderNo);
                orderModel = orderModelMapper.selectOne(orderModel);
            }
            else if (orderNo.contains(DictionaryConst.OrdersType.ORDERS_HEADER_DJ))
            {
                OrderDataModel orderDataModel = new OrderDataModel();
                orderDataModel.setTrade_no(orderNo);
                OrderDataModel orderDataModel1 = orderDataModelMapper.selectOne(orderDataModel);
                String         data            = orderDataModel1.getData();
                MemberOrderVo  memberOrderVo   = JSONObject.parseObject(data, MemberOrderVo.class);
                orderModel.setStore_id(memberOrderVo.getStoreId());
                orderModel.setsNo(orderNo);
                orderModel.setReal_sno(orderNo);
                orderModel.setZ_price(memberOrderVo.getAmount());
            }
            else if (orderNo.contains(DictionaryConst.OrdersType.ORDERS_HEADER_CZ))
            {
                orderModel.setStore_id(paymentVo.getStoreId());
                orderModel.setsNo(orderNo);
                orderModel.setReal_sno(orderNo);
                orderModel.setZ_price(paymentVo.getTotal());

                //用于第三方支付
                orderModel.setCurrency_code(paymentVo.getCurrency_code());
                orderModel.setCurrency_symbol(paymentVo.getCurrency_symbol());
                orderModel.setExchange_rate(paymentVo.getExchange_rate());

                Map<String, Object> dataMap = new HashMap<>(16);
                dataMap.put("order_id", orderNo);
                dataMap.put("user_id", userId);
                dataMap.put("trade_no", orderNo);
                dataMap.put("pay", paymentVo.getPayType());
                dataMap.put("total", paymentVo.getTotal());
                dataMap.put("store_id", paymentVo.getStoreId());
                dataMap.put("remarks", paymentVo.getRemarks());

                //非余额支付 添加一条临时订单
                OrderDataModel orderDataModel = new OrderDataModel();
                orderDataModel.setTrade_no(orderNo);
                orderDataModel.setData(SerializePhpUtils.JavaSerializeByPhp(dataMap));
                orderDataModel.setOrder_type(DictionaryConst.OrdersType.ORDERS_HEADER_CZ);
                orderDataModel.setPay_type(paymentVo.getPayType());
                orderDataModel.setAddtime(new Date());
                orderDataModel.setSource(paymentVo.getStoreType());
                orderDataModel.setStatus(OrderDataModel.PayStatus.NOT_PAY);
                orderDataModel.setCurrency_code(paymentVo.getCurrency_code());
                orderDataModel.setCurrency_symbol(paymentVo.getCurrency_symbol());
                orderDataModel.setExchange_rate(paymentVo.getExchange_rate());
                orderDataModelMapper.insert(orderDataModel);
            }
            else if (orderNo.contains(DictionaryConst.OrdersType.ORDERS_HEADER_MCH_PROMISE))
            {
                OrderDataModel orderDataModel = new OrderDataModel();
                orderDataModel.setTrade_no(orderNo);
                orderDataModel = orderDataModelMapper.selectOne(orderDataModel);
                if (orderDataModel == null)
                {
                    orderModel = null;
                }
                else
                {
                    Map<String, Object> map = JSON.parseObject(orderDataModel.getData(), new TypeReference<Map<String, Object>>()
                    {
                    });
                    orderModel.setReal_sno(orderNo);
                    orderModel.setsNo(orderNo);
                    orderModel.setStatus(orderDataModel.getStatus());
                    orderModel.setAdd_time(orderDataModel.getAddtime());
                    orderModel.setOtype(orderDataModel.getOrder_type());
                    orderModel.setMch_id(MapUtils.getString(map, "mchId"));
                    orderModel.setStore_id(MapUtils.getInteger(map, "storeId"));
                    orderModel.setZ_price(new BigDecimal(MapUtils.getString(map, "paymentAmt")));
                }
            }
            else if (orderNo.contains(DictionaryConst.OrdersType.ORDERS_HEADER_PS))
            {
                PreSellRecordModel preSellRecordModel = new PreSellRecordModel();
                logger.info("进入预售订单-----------------------，支付方式：：：{}",paymentVo.getPayTarget());
                if (paymentVo.getPayTarget() == 1)
                {
                    preSellRecordModel.setsNo(orderNo);
                    preSellRecordModel.setPay_type(PreSellRecordModel.DEPOSIT);
                    preSellRecordModel = preSellRecordModelMapper.selectOne(preSellRecordModel);
                    preSellRecordModel.setPay(paymentVo.getPayType());
                    preSellRecordModelMapper.updateByPrimaryKeySelective(preSellRecordModel);
                    orderModel = JSON.parseObject(preSellRecordModel.getOrder_info(), OrderModel.class);
                    orderModel.setZ_price(preSellRecordModel.getDeposit());
                }
                else if (paymentVo.getPayTarget() == 2)
                {
                    preSellRecordModel = new PreSellRecordModel();
                    preSellRecordModel.setsNo(orderNo);
                    preSellRecordModel.setPay_type(PreSellRecordModel.BALANCE);
                    preSellRecordModel = preSellRecordModelMapper.selectOne(preSellRecordModel);
                    preSellRecordModel.setPay(paymentVo.getPayType());
                    preSellRecordModelMapper.updateByPrimaryKeySelective(preSellRecordModel);
                    orderModel.setsNo(orderNo);
                    orderModel = orderModelMapper.selectOne(orderModel);
//                    //定金付尾款，应该重新生成支付订单号
//                    String realSno = publicOrderService.createOrderNo(DictionaryConst.OrdersType.ORDERS_HEADER_PS);
//                    orderModel.setReal_sno(realSno);
                    orderModel.setReal_sno(preSellRecordModel.getReal_sno());
                    orderModel.setZ_price(orderModel.getZ_price().subtract(preSellRecordModel.getDeposit()));
                }
                else
                {
                    orderModel.setsNo(orderNo);
                    orderModel = orderModelMapper.selectOne(orderModel);
                }

                logger.info("预售下单数据：：：：{}",JSON.toJSONString(preSellRecordModel));
                logger.info("实际支付金额：：：：：：{}",orderModel.getZ_price());
            }
            else if (orderNo.contains(DictionaryConst.OrdersType.ORDERS_HEADER_JB))
            {
                OrderDataModel orderDataModel = new OrderDataModel();
                orderDataModel.setTrade_no(orderNo);
                orderDataModel = orderDataModelMapper.selectOne(orderDataModel);
                if (orderDataModel == null)
                {
                    orderModel = null;
                }
                else
                {
                    PromiseOrderVo promiseOrderVo = JSON.parseObject(orderDataModel.getData(), PromiseOrderVo.class);
                    orderModel.setReal_sno(orderNo);
                    orderModel.setsNo(orderNo);
                    orderModel.setStatus(orderDataModel.getStatus());
                    orderModel.setAdd_time(orderDataModel.getAddtime());
                    orderModel.setOtype(orderDataModel.getOrder_type());
                    orderModel.setStore_id(promiseOrderVo.getStoreId());
                    orderModel.setZ_price(promiseOrderVo.getPaymentAmt());
                }
            }
            else if (orderNo.contains(DictionaryConst.OrdersType.ORDERS_HEADER_IN))
            {
                orderModel.setsNo(orderNo);
                orderModel = orderModelMapper.selectOne(orderModel);
                orderModel.setReal_sno(publicOrderService.createOrderNo(DictionaryConst.OrdersType.ORDERS_HEADER_IN));
                orderModelMapper.updateByPrimaryKeySelective(orderModel);
            }
            else if (orderNo.contains(DictionaryConst.OrdersType.ORDERS_HEADER_ZB))
            {
                orderModel.setsNo(orderNo);
                orderModel = orderModelMapper.selectOne(orderModel);
                orderModel.setReal_sno(publicOrderService.createOrderNo(DictionaryConst.OrdersType.ORDERS_HEADER_ZB));
                orderModelMapper.updateByPrimaryKeySelective(orderModel);
            }
            else
            {
                if (paymentVo != null)
                {
                    //是否有内部api参数,如果有则调用内部实现 add by trick 2022-08-10 17:05:01
                    Map<String, Object> paramMap = JSON.parseObject(paymentVo.getParameter(), new TypeReference<Map<String, Object>>()
                    {
                    });
                    String laikeApiUrl = MapUtils.getString(paramMap, "laikeApi");
                    if (StringUtils.isNotEmpty(laikeApiUrl))
                    {
                        logger.debug("正在【下单】{} 执行接口:{}", orderNo, laikeApiUrl);
                        Map<String, Object> paramMap1 = new HashMap<>(1);
                        paramMap1.put("vo", JSON.toJSONString(paymentVo));
                        Map<String, Object> resultMap = httpApiUtils.executeApi(laikeApiUrl, JSON.toJSONString(paramMap1));
                        orderNo = MapUtils.getString(resultMap, "sNo");
                    }
                }
                orderModel.setsNo(orderNo);
                orderModel = orderModelMapper.selectOne(orderModel);
                //如果没找到则找临时表
                if (orderModel == null)
                {
                    OrderDataModel orderDataOld = new OrderDataModel();
                    orderDataOld.setTrade_no(orderNo);
                    orderDataOld.setOrder_type(paymentVo.getPayType());
                    orderDataOld = orderDataModelMapper.selectOne(orderDataOld);
                    if (orderDataOld != null)
                    {
                        Map<String, Object> map = JSON.parseObject(orderDataOld.getData(), new TypeReference<Map<String, Object>>()
                        {
                        });
                        //构造订单数据
                        orderModel = new OrderModel();
                        orderModel.setReal_sno(orderDataOld.getTrade_no());
                        orderModel.setAdd_time(orderDataOld.getAddtime());
                        orderModel.setOtype(orderDataOld.getOrder_type());
                        orderModel.setZ_price(new BigDecimal(MapUtils.getString(map, "paymentAmt")));
                    }
                }
                else
                {
                    //待支付订单支付宝支付外部订单号重复问题处理
                    orderModel.setReal_sno(publicOrderService.createOrderNo(DictionaryConst.OrdersType.ORDERS_HEADER_GM));
                    orderModelMapper.updateByPrimaryKeySelective(orderModel);
                    List<OrderModel> orderModelList = null;
                    //订单是否多店铺未支付已经拆单
                    //供应商拆单
                    OrderModel order = new OrderModel();
                    order.setP_sNo(orderModel.getsNo());
                    orderModelList = orderModelMapper.select(order);
                    if (orderModelList != null)
                    {
                        for (OrderModel model : orderModelList)
                        {
                            model.setReal_sno(orderModel.getReal_sno());
                            orderModelMapper.updateByPrimaryKeySelective(model);
                        }
                    }
                }
            }
            if (orderModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DDBCZ, "订单不存在", "pay");
            }
            //判断是否为orderModel中的数据,用于处理payType为空的情况
            OrderModel orderEntity = new OrderModel();
            if (paymentVo != null && paymentVo.getStoreId() != 0)
            {
                orderEntity.setStore_id(paymentVo.getStoreId());
            }
            else
            {
                orderEntity.setStore_id(orderModel.getStore_id());
            }
            orderEntity.setsNo(orderNo);
            orderEntity = orderModelMapper.selectOne(orderModel);
            if (!Objects.isNull(orderEntity) && orderEntity.getId() != null)
            {
                if (paymentVo != null && StringUtils.isNotEmpty(paymentVo.getPayType()))
                {
                    orderEntity.setPay(paymentVo.getPayType());
                }
                else
                {
                    orderEntity.setPay(orderModel.getPay());
                }
                orderModelMapper.updateByPrimaryKeySelective(orderEntity);
            }
        }
        catch (Exception e)
        {
            logger.error("支付-获取订单信息异常：", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_ZFHQDDXXSB, "支付-获取订单信息失败", "getOrderInfo");
        }
        return orderModel;
    }

    @Override
    public void modifyOrder(EditOrderVo orderVo) throws LaiKeAPIException
    {
        try
        {
            int row;
            //获取订单信息
            OrderModel orderModel = new OrderModel();
            orderModel.setStore_id(orderVo.getStoreId());
            orderModel.setsNo(orderVo.getOrderNo());
            orderModel = orderModelMapper.selectOne(orderModel);
            if (orderModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DDBCZ, "订单不存在");
            }
            if (DictionaryConst.OrdersStatus.ORDERS_R_STATUS_CONSIGNMENT != orderModel.getStatus()
                    && DictionaryConst.OrdersStatus.ORDERS_R_STATUS_UNPAID != orderModel.getStatus())
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DDBJSB, "订单编辑失败");
            }
            //是否是跨店铺
            String[] mchList   = StringUtils.trim(orderModel.getMch_id(), SplitUtils.DH).split(SplitUtils.DH);
            boolean  isManyMch = mchList.length > 1;

            OrderModel orderUpdate = new OrderModel();
            orderUpdate.setId(orderModel.getId());
            orderUpdate.setMobile(orderVo.getTel());
            orderUpdate.setSheng(orderVo.getShen());
            orderUpdate.setShi(orderVo.getShi());
            orderUpdate.setXian(orderVo.getXian());
            orderUpdate.setName(orderVo.getUserName());
            orderUpdate.setAddress(orderVo.getAddress());
            //多店铺不能修改订单备注
            if (!isManyMch && StringUtils.isNotEmpty(orderVo.getRemarks()))
            {
                Map<String, String> remarkMap = new HashMap<>(16);
                remarkMap.put(mchList[0], orderVo.getRemarks());
                orderUpdate.setRemarks(JSON.toJSONString(remarkMap));
            }
            //修改订单信息
            if (!isManyMch || DictionaryConst.OrdersStatus.ORDERS_R_STATUS_UNPAID == orderModel.getStatus())
            {
                //订单状态（只能修改成待发货）
                if (DictionaryConst.OrdersStatus.ORDERS_R_STATUS_CONSIGNMENT == orderVo.getOrderStatus())
                {
                    orderUpdate.setStatus(DictionaryConst.OrdersStatus.ORDERS_R_STATUS_CONSIGNMENT);
                    //支付方式默认钱包
                    orderUpdate.setPay(DictionaryConst.OrderPayType.ORDERPAYTYPE_WALLET_PAY);
                    orderUpdate.setPay_time(new Date());
                    //修改订单明细状态
                    row = orderDetailsModelMapper.updateOrderDetailsStatus(orderVo.getStoreId(), orderVo.getOrderNo(), orderUpdate.getStatus());
                    if (row < 1)
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DDBJSB, "订单编辑失败");
                    }
                }
                //未付款可以修改订单金额、订单状态（只能修改成待发货）
                if (StringUtils.isNotEmpty(orderVo.getOrderAmt()) && DictionaryConst.OrdersStatus.ORDERS_R_STATUS_UNPAID == orderModel.getStatus())
                {
                    orderUpdate.setZ_price(new BigDecimal(orderVo.getOrderAmt()));
                    //修改订单金额后需要记录差额
                    BigDecimal        priceDiff         = orderModel.getZ_price().subtract(orderUpdate.getZ_price());
                    OrderDetailsModel orderDetailsModel = new OrderDetailsModel();
                    orderDetailsModel.setStore_id(orderVo.getStoreId());
                    orderDetailsModel.setR_sNo(orderVo.getOrderNo());
                    List<OrderDetailsModel> orderDetailsModelList = orderDetailsModelMapper.select(orderDetailsModel);
                    for (OrderDetailsModel orderDetails : orderDetailsModelList)
                    {
                        //支付金额=优惠金额+运费
                        BigDecimal payMoney = orderDetails.getAfter_discount().add(orderDetails.getFreight());
                        //该单均摊差额 (优惠后金额+运费)/订单总金额*订单差额
                        BigDecimal updatePrice = payMoney.divide(orderModel.getZ_price(), 12, BigDecimal.ROUND_DOWN).multiply(priceDiff);
                        //重新计算优惠后金额 优惠后金额 - (优惠后金额/(优惠后金额+运费))*该单均摊金额
                        BigDecimal moneyTemp     = orderDetails.getAfter_discount().divide(payMoney, 12, BigDecimal.ROUND_DOWN).multiply(updatePrice);
                        BigDecimal afterDiscount = orderDetails.getAfter_discount().subtract(moneyTemp).setScale(2, BigDecimal.ROUND_HALF_UP);
                        //重新计算运费金额 运费金额 - (运费金额/(优惠后金额+运费))*该单均摊金额
                        BigDecimal yunFeiPrice = orderDetails.getFreight().subtract(orderDetails.getFreight().divide(payMoney, 12, BigDecimal.ROUND_DOWN).multiply(updatePrice)).setScale(2, BigDecimal.ROUND_HALF_UP);

                        //修改订单明细金额
                        OrderDetailsModel orderDetailsUpdate = new OrderDetailsModel();
                        orderDetailsUpdate.setId(orderDetails.getId());
                        orderDetailsUpdate.setFreight(yunFeiPrice);
                        orderDetailsUpdate.setAfter_discount(afterDiscount);
                        row = orderDetailsModelMapper.updateByPrimaryKeySelective(orderDetailsUpdate);
                        if (row < 1)
                        {
                            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DDBJSB, "订单编辑失败");
                        }
                    }
                }
            }
            if (orderModel.getSelf_lifting() == 2 && orderVo.getDeliveryTime() != null && orderVo.getDeliveryPeriod() != null)
            {
                //商家自配的修改时间
                OrderDetailsModel orderDetailsModel = new OrderDetailsModel();
                orderDetailsModel.setStore_id(orderVo.getStoreId());
                orderDetailsModel.setR_sNo(orderVo.getOrderNo());
                List<OrderDetailsModel> orderDetailsModelList = orderDetailsModelMapper.select(orderDetailsModel);
                for (OrderDetailsModel orderDetails : orderDetailsModelList)
                {
                    if (orderDetails.getStore_self_delivery() != null)
                    {
                        StoreSelfDeliveryModel storeSelfDeliveryModel = storeSelfDeliveryModelMapper.selectByPrimaryKey(orderDetails.getStore_self_delivery());
                        storeSelfDeliveryModel.setDelivery_time(DateUtil.dateFormateToDate(orderVo.getDeliveryTime(), GloabConst.TimePattern.YMD));
                        storeSelfDeliveryModel.setDelivery_period(orderVo.getDeliveryPeriod());
                        storeSelfDeliveryModelMapper.updateByPrimaryKeySelective(storeSelfDeliveryModel);
                    }
                }
            }
            //运费更新信息
            if (!orderModel.getSheng().equals(orderVo.getShen()) || !orderModel.getShi().equals(orderVo.getShi()) || !orderModel.getXian().equals(orderVo.getXian()))
            {
                //订单总运费
                BigDecimal freight = BigDecimal.ZERO;
                //运费计算参数
                Map<String, Object> proMap            = new HashMap<>(16);
                OrderDetailsModel   orderDetailsModel = new OrderDetailsModel();
                orderDetailsModel.setStore_id(orderVo.getStoreId());
                orderDetailsModel.setR_sNo(orderVo.getOrderNo());
                List<OrderDetailsModel> orderDetailsModelList = orderDetailsModelMapper.select(orderDetailsModel);
                for (OrderDetailsModel orderDetails : orderDetailsModelList)
                {
                    if (proMap.containsKey(orderDetails.getP_id().toString()))
                    {
                        proMap.put(orderDetails.getP_id().toString(), MapUtils.getInteger(proMap, orderDetails.getP_id().toString()) + orderDetails.getNum());
                    }
                    else
                    {
                        proMap.put(orderDetails.getP_id().toString(), orderDetails.getNum());
                    }
                    orderDetails.setFreight(BigDecimal.ZERO);
                    orderDetailsModelMapper.updateByPrimaryKeySelective(orderDetails);
                }
                UserAddress userAddress = new UserAddress();
                userAddress.setSheng(orderVo.getShen());
                userAddress.setCity(orderVo.getShi());
                userAddress.setQuyu(orderVo.getXian());
                //更新订单详情运费
                for (String key : proMap.keySet())
                {
                    ProductListModel  productListModel = productListModelMapper.selectByPrimaryKey(key);
                    Integer           proNum           = MapUtils.getInteger(proMap, key);
                    BigDecimal        freightPrice     = this.getFreight(Integer.valueOf(productListModel.getFreight()), userAddress, proNum, new BigDecimal(productListModel.getWeight()));
                    OrderDetailsModel proOrderDetail   = new OrderDetailsModel();
                    proOrderDetail.setStore_id(orderVo.getStoreId());
                    proOrderDetail.setR_sNo(orderVo.getOrderNo());
                    proOrderDetail.setP_id(Integer.valueOf(key));
                    proOrderDetail = orderDetailsModelMapper.select(proOrderDetail).get(0);
                    proOrderDetail.setFreight(freightPrice);
                    orderDetailsModelMapper.updateByPrimaryKeySelective(proOrderDetail);
                    freight = freight.add(freightPrice);
                }
                //修改订单总运费
                orderUpdate.setZ_price(orderModel.getZ_price().subtract(orderModel.getZ_freight()).add(freight));
                orderUpdate.setZ_freight(freight);
            }
            //修改订单基本信息
            row = orderModelMapper.updateByPrimaryKeySelective(orderUpdate);
            if (row < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DDBJSB, "订单编辑失败");
            }

        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("编辑订单失败：", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_BJDDSB, "编辑订单失败", "saveEditOrder");
        }
    }

    @Override
    public Map<String, Object> orderPcDetails(AdminOrderDetailVo adminOrderDetailVo) throws LaiKeAPIException
    {
        Map<String, Object> retMap = Maps.newConcurrentMap();
        try
        {
            int     store_id  = adminOrderDetailVo.getStoreId();
            String  sNo       = adminOrderDetailVo.getId();
            String  orderType = adminOrderDetailVo.getOrderType();
            String  type      = adminOrderDetailVo.getType();
            boolean update_s  = StringUtils.isEmpty(type);

            OrderConfigModal orderConfigModal = new OrderConfigModal();
            orderConfigModal.setStore_id(store_id);
            orderConfigModal = orderConfigModalMapper.selectOne(orderConfigModal);

            Date remind_time;
            if (orderConfigModal != null)
            {
                int remind = orderConfigModal.getRemind();
                if (remind == 0)
                {
                    remind_time = DateUtil.getAddDate(new Date(), 7);
                }
                else
                {
                    int remind_day  = remind / 24;
                    int remind_hour = remind % 24;
                    remind_time = DateUtil.getAddDate(new Date(), remind_day);
                    remind_time = DateUtil.getAddDateBySecond(remind_time, remind_hour * 3600);
                }
            }
            else
            {
                remind_time = DateUtil.getAddDate(new Date(), 7);
            }

            //是否是多店铺订单
            boolean isManyMch = false;
            /*-----------进入订单详情把未读状态改成已读状态，已读状态的状态不变-------*/
            OrderModel orderModel = new OrderModel();
            orderModel.setStore_id(store_id);
            orderModel.setsNo(sNo);
            orderModel = orderModelMapper.selectOne(orderModel);
            // 订单总价
            BigDecimal pay_price = BigDecimal.ZERO;
            //订单总价 不会变的
            BigDecimal old_total = orderModel.getOld_total();
            //原运费 不会变的
            BigDecimal old_freight = orderModel.getOld_freight();
            Map        params      = Maps.newConcurrentMap();
            if (orderModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DDBCZ, "订单不存在");
            }
            if (orderModel.getMch_id().split(SplitUtils.DH).length > 2)
            {
                isManyMch = true;
            }

            BigDecimal exchangeRate   = orderModel.getExchange_rate();
            exchangeRate = Objects.isNull(exchangeRate) ? BigDecimal.ONE : exchangeRate;
            String     currencySymbol = orderModel.getCurrency_symbol();
            String     currencyCode   = orderModel.getCurrency_code();
            //币种和符号为null，获取商城默认币种
            if (StringUtils.isEmpty(currencySymbol) || StringUtils.isEmpty(currencyCode))
            {
                Map defaultCurrency = currencyStoreModelMapper.getDefaultCurrency(store_id);
                currencyCode = Objects.isNull(currencyCode) ? (String) defaultCurrency.get("currency_code") : currencyCode;
                currencySymbol = Objects.isNull(currencySymbol) ? (String) defaultCurrency.get("currency_symbol") : currencySymbol;
            }
            logger.info("订单号：{},汇率：{}", sNo, exchangeRate);

            pay_price = orderModel.getZ_price();
            String  shopId = StringUtils.trim(orderModel.getMch_id(), SplitUtils.DH);
            Integer readd  = orderModel.getReadd();
            if (readd == 0)
            {
                params.put("readd", 1);
                params.put("store_id", store_id);
                params.put("orderno", sNo);
                int delivery_status = orderModel.getDelivery_status();
                if (delivery_status == 1)
                {
                    params.put("remind", remind_time);
                }
                int row = orderModelMapper.updateByOrdernoDynamic(params);

                if (row < 1)
                {
                    logger.error("修改订单失败");
                }

                String order_status = "1,3,5";
                //可能存在多店铺情况
                String[] mchIds = shopId.split(SplitUtils.DH);
                for (String mchId : mchIds)
                {
                    messageLoggingModalMapper.updateMessLogInfo(store_id, Integer.parseInt(mchId), sNo, order_status);
                }
            }
            int status = orderModel.getStatus();
            if (status == ORDERS_R_STATUS_COMPLETE && shopId.length() == 1)
            {
                messageLoggingModalMapper.updateMessLogInfo(store_id, Integer.parseInt(shopId), sNo, MessageLoggingModal.Type.TYPE_ORDER_OK_GOODS.toString());
            }
            if (status == ORDERS_R_STATUS_CLOSE && shopId.length() == 1)
            {
                messageLoggingModalMapper.updateMessLogInfo(store_id, Integer.parseInt(shopId), sNo, MessageLoggingModal.Type.TYPE_ORDER_CLOSE.toString());
            }
            /*--------------------------------------------------------------------------*/
            //支付方式
            Map<String, String>       paymentModelMap       = publicPaymentConfigService.getPaymentMap();
            List<Map<String, Object>> adminOrderDetailsList = orderDetailsModelMapper.getAdminOrderDetailsInfo(sNo, store_id);
            int                       num                   = adminOrderDetailsList.size();
            if (orderModel.getOtype().equals(DictionaryConst.OrdersType.ORDERS_HEADER_PT))
            {
                for (Map<String, Object> map : adminOrderDetailsList)
                {
                    int teamNum = groupOpenRecordModelMapper.getTeamNumBysNO(sNo);
                    map.put("teamNum",teamNum);
                }
            }
            Map<String, Object>       data                  = new HashMap<>(16);
            // 满减金额
            BigDecimal reduce_price = BigDecimal.ZERO;
            // 优惠券金额
            BigDecimal coupon_price = BigDecimal.ZERO;
            // 平台优惠券金额
            BigDecimal preferential_amount = orderModel.getPreferential_amount();
            // 平台优惠券金额 (用来赋值的）
            BigDecimal preferentiaNewlAmount = BigDecimal.ZERO;
            //分销折扣金额
            BigDecimal comm_amount = BigDecimal.ZERO;
            //多店铺优惠劵拆单--》优惠金额特殊处理
            boolean isCouponHandle = false;
            // 积分
            int        allow  = 0;
            BigDecimal yunfei = BigDecimal.ZERO;
            //
            List<String> courier_num_arr = new ArrayList<>();
            //快递字符串信息 xxx(xxx),...
            String        kdStrFormat = "%s(%s);";
            StringBuilder kdStr       = new StringBuilder();
            //快递公司名称
            StringBuilder express_name = new StringBuilder();
            // 优惠金额
            BigDecimal yh_money = BigDecimal.ZERO;
            // 优惠类型
            String discount_type = "";
            // 优惠券ID
            Map<String, Object> orderDetailsData = adminOrderDetailsList.get(0);
            //分销折扣金额
            comm_amount = DataUtils.getBigDecimalVal(orderDetailsData, "comm_discount", BigDecimal.ZERO);
            // 优惠券
            String coupon_id = DataUtils.getStringVal(orderDetailsData, "coupon_id", "0");
            // 满减活动ID
            Integer subtraction_id = DataUtils.getIntegerVal(orderDetailsData, "subtraction_id", 0);
            // 会员等级折扣
            BigDecimal grade_rate = DataUtils.getBigDecimalVal(orderDetailsData, "grade_rate", BigDecimal.ZERO);
            // 会员优惠金额
            BigDecimal grade_rate_amount = DataUtils.getBigDecimalVal(orderDetailsData, "grade_fan", BigDecimal.ZERO);
            // 商品总价
            BigDecimal spz_price = DataUtils.getBigDecimalVal(orderDetailsData, "spz_price", BigDecimal.ZERO);
            // 总运费
            BigDecimal z_freight = BigDecimal.ZERO;
            // 订单类型
            String otype = DataUtils.getStringVal(orderDetailsData, "otype", "0");
            //是否自提
            Integer selfLifting = DataUtils.getIntegerVal(orderDetailsData, "self_lifting", 0);
            //$zifuchuan = trim(strrchr($coupon_id, ','),',');
            int zifuchuan = com.laiketui.core.utils.tool.StringUtils.trim(coupon_id, SplitUtils.DH).indexOf(SplitUtils.DH);
            //实物商品和虚拟商品的平台优惠
            BigDecimal platform_coupon_price = BigDecimal.ZERO;
            //实物商品和虚拟商品的店铺优惠
            BigDecimal store_coupon_price = BigDecimal.ZERO;

            if (subtraction_id != 0)
            {
                discount_type = "满减";
            }
            else if (zifuchuan != 0)
            {
                discount_type = "优惠券";
            }
            //订单总运费
            BigDecimal totalFright = BigDecimal.ZERO;
            //订单商品总价小计
            BigDecimal goodsPrice = new BigDecimal("0");
//            List<String> express_ids = new ArrayList<String>();
            courier_num_arr = new ArrayList<>();
            data.put("fh", 0);
            if (Objects.nonNull(MapUtils.getInteger(orderDetailsData,"self_lifting")))
            {
                data.put("self_lifting", MapUtils.getInteger(orderDetailsData, "self_lifting"));
            }
            int        orderDetailsStatus = -1;
            BigDecimal totalSupplyPrice   = BigDecimal.ZERO;
            //是否计算了商品运费
            List<Integer> goodFreight = new ArrayList<>();
            //订单发货记录
            ExpressDeliveryModel expressDeliveryModel;
            retMap.put("supplier_id", 0);
            //虚拟和实物商品只显示该店铺下的订单
            if ((otype.equals(DictionaryConst.OrdersType.ORDERS_HEADER_GM) || otype.equals(DictionaryConst.OrdersType.ORDERS_HEADER_VI)) && adminOrderDetailVo.getMchId() != null && adminOrderDetailsList.get(0).containsKey("d_mch_id"))
            {
                adminOrderDetailsList = adminOrderDetailsList.stream().filter(x -> x.containsKey("d_mch_id") && DataUtils.getIntegerVal(x, "d_mch_id").equals(adminOrderDetailVo.getMchId())).collect(Collectors.toList());
            }
            for (Map<String, Object> orderDetailsMap : adminOrderDetailsList)
            {
                if (MapUtils.getString(orderDetailsMap, "otype").equals(DictionaryConst.OrdersType.ORDERS_HEADER_ZB))
                {
                    //主播信息，只有直播的订单才显示主播信息
                    retMap.put("anchor_name", orderDetailsMap.get("anchor_name"));
                    retMap.put("anchor_url", orderDetailsMap.get("anchor_url"));
                }

                if (StringUtils.isNotEmpty(DataUtils.getStringVal(orderDetailsMap, "store_self_delivery")) && orderModel.getSelf_lifting() == 2)
                {
                    //商家自配处理
                    HashMap<String, Object> storeSelfInfo          = new HashMap<>();
                    StoreSelfDeliveryModel  storeSelfDeliveryModel = storeSelfDeliveryModelMapper.selectByPrimaryKey(DataUtils.getStringVal(orderDetailsMap, "store_self_delivery"));
                    storeSelfInfo.put("delivery_time", DateUtil.dateFormate(storeSelfDeliveryModel.getDelivery_time(), GloabConst.TimePattern.YMD));
                    storeSelfInfo.put("delivery_period", storeSelfDeliveryModel.getDelivery_period());
                    storeSelfInfo.put("phone", storeSelfDeliveryModel.getPhone());
                    storeSelfInfo.put("courier_name", storeSelfDeliveryModel.getCourier_name());
                    retMap.put("storeSelfInfo", storeSelfInfo);
                }
                //订单号
                String orderNo = MapUtils.getString(orderDetailsMap, "sNo");
                //快递单号
                String courierNum = MapUtils.getString(orderDetailsMap, "courier_num");
                //订单详情id
                int orderDetailId = MapUtils.getIntValue(orderDetailsMap, "id");
                platform_coupon_price = platform_coupon_price.add(DataUtils.getBigDecimalVal(orderDetailsMap, "platform_coupon_price"));
                store_coupon_price = store_coupon_price.add(DataUtils.getBigDecimalVal(orderDetailsMap, "store_coupon_price"));
                //商品id
                int                      pId                      = MapUtils.getIntValue(orderDetailsMap, "p_id");
                SupplierOrderFrightModel supplierOrderFrightModel = new SupplierOrderFrightModel();
                supplierOrderFrightModel.setsNo(orderNo);
                supplierOrderFrightModel.setDetail_id(pId);
                supplierOrderFrightModel = supplierOrderFrightModelMapper.selectOne(supplierOrderFrightModel);
                if (!Objects.isNull(supplierOrderFrightModel) && !goodFreight.contains(pId))
                {
                    totalFright = totalFright.add(supplierOrderFrightModel.getFreight());
                    goodFreight.add(pId);
                }
                //商品规格id
                int sid = MapUtils.getIntValue(orderDetailsMap, "sid");
                //商品图片
                String goodsImgUrl = MapUtils.getString(orderDetailsMap, "img");
                //订单详情总供货价
                String supplierSettlement = MapUtils.getString(orderDetailsMap, "supplier_settlement");
                if (StringUtils.isNotEmpty(supplierSettlement))
                {
                    totalSupplyPrice = totalSupplyPrice.add(new BigDecimal(supplierSettlement));
                }
                goodsImgUrl = publiceService.getImgPath(goodsImgUrl, store_id);
                orderDetailsMap.put("pic", goodsImgUrl);
                // 联系人
                data.put("user_name", MapUtils.getString(orderDetailsMap, "user_name"));
                // 快递单号
                data.put("courierNum", courierNum);
                // 联系人
                data.put("name", DataUtils.getStringVal(orderDetailsMap, "name", ""));
                String              remarks    = DataUtils.getStringVal(orderDetailsMap, "remarks", "");
                Map<String, String> remarksMap = new HashMap<>(16);
                //备注
                if (StringUtils.isNotEmpty(remarks))
                {
                    // 订单备注
                    remarksMap = JSON.parseObject(remarks, new TypeReference<Map<String, String>>()
                    {
                    });
                }
                StringBuilder remarksStr = new StringBuilder(SplitUtils.DH);
                for (String str : remarksMap.keySet())
                {
                    remarksStr.append(remarksMap.get(str)).append(SplitUtils.DH);
                }
                data.put("remarks", StringUtils.trim(remarksStr.toString(), SplitUtils.DH));
                //运费取详单中的运费进行累加计算
                z_freight = z_freight.add(DataUtils.getBigDecimalVal(orderDetailsMap, "freight"));

                //判断是否显示预约信息
                if (orderDetailsMap.containsKey("mch_store_write_id") && DataUtils.getIntegerVal(orderDetailsMap, "mch_store_write_id") != null)
                {
                    data.put("show_write_time", 1);
                    HashMap<String, Object> write_time_info = new HashMap<>();
                    MchStoreModel           mch_store_write = mchStoreModelMapper.selectByPrimaryKey(DataUtils.getIntegerVal(orderDetailsMap, "mch_store_write_id"));
                    if (orderDetailsMap.containsKey("write_time") && DataUtils.getStringVal(orderDetailsMap, "write_time") != null)
                    {
                        write_time_info.put("time", DataUtils.getStringVal(orderDetailsMap, "write_time"));
                        write_time_info.put("mch_store", mch_store_write.getName());
                        write_time_info.put("address", mch_store_write.getSheng() + mch_store_write.getShi() + mch_store_write.getXian() + mch_store_write.getAddress());
                    }
                    data.put("write_time_info", write_time_info);
                }
                else
                {
                    data.put("show_write_time", 0);
                }
                //订单类型
                data.put("orderTypeName", OrderDataUtils.getOrderType(MapUtils.getString(orderDetailsMap, "otype")));
                //支付时间
                String addTime = DateUtil.dateFormate(MapUtils.getString(orderDetailsMap, "add_time"), GloabConst.TimePattern.YMDHMS);
                String payTime = MapUtils.getString(orderDetailsMap, "pay_time");
                orderDetailsMap.put("add_time", addTime);
                if (StringUtils.isEmpty(payTime))
                {
                    payTime = "";
                }
                else
                {
                    payTime = DateUtil.dateFormate(payTime, GloabConst.TimePattern.YMDHMS);
                }
                //售后信息
                Map<String, Object> returnInfo = new HashMap<>(16);
                //订单售后状态
                String returnOrderStatus = "";
                String statusName;
                //处于售后状态时不显示核销按钮标志
                int show_write = 0;
                //查询详单是否有售后正在进行中
                ReturnOrderModel returnOrderModel = returnOrderModelMapper.getReturnOrderInfo(store_id, orderDetailId, orderNo);
                if (returnOrderModel != null)
                {
                    //处于售后状态时不显示核销按钮标志
                    show_write = 0;

                    Integer r_type  = returnOrderModel.getR_type();
                    Integer re_type = returnOrderModel.getRe_type();
                    //前端国际化状态返回数字特殊处理
                    /*WHEN (d.r_type = 1 || d.r_type = 3)
                AND d.re_type = 1 THEN
                '退款中'
                when d.r_type = 4 || d.r_type = 9 then '退款成功'
                when (d.r_type = 2 || d.r_type = 5 || d.r_type = 8) and d.re_type != 3 then '退款失败'
                when (d.r_type = 1 || d.r_type = 3 || d.r_type = 11) and d.re_type = 3 then '换货中'
                when d.r_type = 12 then '换货成功'
                when (d.r_type = 5 || d.r_type = 10) and d.re_type = 3 then '换货失败'
                when d.r_type = 13 then '退款成功'
                ELSE '审核中'*/
                    if ((r_type.equals(DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_AGREE_REBACK)
                            || r_type.equals(DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_USER_DELIVERED))
                            && re_type.equals(ReturnOrderModel.RE_TYPE_RETURN_REFUND))
                    {
                        returnOrderStatus = "1";
                        statusName = "退款中";
                    }
                    else if (r_type.equals(DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_RECEIVED_REBAKGOODS)
                            || r_type.equals(DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_AGREE_REBACK_AMT)
                            || r_type.equals(DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_REFUSE_ARTIFICIAL_OK)
                            || r_type.equals(DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_QUICK_REFUND))
                    {
                        returnOrderStatus = "2";  //退款成功
                        statusName = "退款成功";
                    }
                    else if ((r_type.equals(DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_REFUSE_REBACKGOODS_AMT)
                            || r_type.equals(DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_REFUSE_REBACKGOODS)
                            || r_type.equals(DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_REFUSE_AMT))
                            && !re_type.equals(ReturnOrderModel.RE_TYPE_EXCHANGE))
                    {
                        returnOrderStatus = "3";  //退款失败
                        statusName = "退款失败";
                    }
                    else if ((r_type.equals(DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_AGREE_REBACK)
                            || r_type.equals(DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_USER_DELIVERED)
                            || r_type.equals(DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_AGREE_REBACK_GOODS))
                            && re_type.equals(ReturnOrderModel.RE_TYPE_EXCHANGE))
                    {
                        returnOrderStatus = "4";  //换货中
                        statusName = "换货中";
                    }
                    else if (r_type.equals(DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_AFTER_SALE_END))
                    {
                        returnOrderStatus = "5";  //换货成功
                        statusName = "换货成功";
                    }
                    else if ((r_type.equals(DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_REFUSE_REBACKGOODS)
                            || r_type.equals(DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_REFUSE_AFTER_SALE))
                            && re_type.equals(ReturnOrderModel.RE_TYPE_EXCHANGE))
                    {
                        returnOrderStatus = "6";  //换货失败
                        statusName = "换货失败";
                    }
                    else
                    {
                        returnOrderStatus = "7";  //审核中
                        statusName = "审核中";
                    }
                    returnInfo.put("id", returnOrderModel.getId());
                    returnInfo.put("statusName", statusName);
                    returnInfo.put("name", statusName);
                    //不知道这判断用来干啥的，导致供应商端无法查看售后状态  禅道53777
/*                    if (returnOrderModel.getIs_agree().equals(DictionaryConst.WhetherMaven.WHETHER_NO) && adminOrderDetailVo.getSupplierId() != null) {
                        returnInfo.put("statusName", "");
                    }*/
                    orderDetailsMap.put("returnInfo", returnInfo);
                    if (MapUtils.getString(orderDetailsMap, "otype").equals(DictionaryConst.OrdersType.ORDERS_HEADER_VI) && returnOrderModel.getR_write_off_num() != null)
                    {
                        //处理退款核销次数
                        retMap.put("r_write_off_num", returnOrderModel.getR_write_off_num());
                        orderDetailsMap.put("r_write_off_num", returnOrderModel.getR_write_off_num());
                    }
                }
                else
                {
                    orderDetailsMap.put("returnInfo", "");
                }
                orderDetailsMap.put("show_write", show_write);
                orderDetailsMap.put("returnOrderStatus", returnOrderStatus);
                //店铺信息
                String   mchId     = StringUtils.trim(MapUtils.getString(orderDetailsMap, "mch_id"), SplitUtils.DH);
                String[] mchIdList = mchId.split(SplitUtils.DH);
                for (String id : mchIdList)
                {
                    MchModel mchModel = new MchModel();
                    mchModel = mchModelMapper.getMchInfo(id, store_id);
                    if (!Objects.isNull(mchModel))
                    {
                        orderDetailsMap.put("mchName", mchModel.getName());
                    }
                }

                data.put("pay_time", payTime);
                //来源
                data.put("source", MapUtils.getString(orderDetailsMap, "source"));
                //订单号
                data.put("sNo", MapUtils.getString(orderDetailsMap, "sNo"));
                // 联系电话
                data.put("oid", MapUtils.getString(orderDetailsMap, "oid"));
                // 联系电话
                data.put("mobile", MapUtils.getString(orderDetailsMap, "mobile"));
                //获取订单状态
                int orderStatus = MapUtils.getIntValue(orderDetailsMap, "status");
                orderDetailsMap.put("status", orderStatus);
                orderDetailsMap.put("statusName", OrderDataUtils.getOrderStatus(orderStatus));
                if (orderStatus == DictionaryConst.OrdersStatus.ORDERS_R_STATUS_TOBEVERIFIED)
                {
                    show_write = 1;
                }
                //是否显示核销按钮标志
                orderDetailsMap.put("show_write", show_write);
                //todo
                String grade_rate_tmp = DataUtils.getStringVal(orderDetailsMap, "grade_rate", "");
                if ("1.00".equalsIgnoreCase(grade_rate_tmp) || "0.00".equalsIgnoreCase(grade_rate_tmp) ||
                        "0".equalsIgnoreCase(grade_rate_tmp) || "1".equalsIgnoreCase(grade_rate_tmp))
                {
                    data.put("grade_rate2", 1);
                    data.put("grade_rate", "-");
                }
                else
                {
                    data.put("grade_rate2", grade_rate_tmp);
                    BigDecimal grade = new BigDecimal(grade_rate_tmp).multiply(new BigDecimal("10"));
                    data.put("grade_rate", grade + "折");
                }

                BigDecimal pnum    = DataUtils.getBigDecimalVal(orderDetailsMap, "num", BigDecimal.ZERO);
                BigDecimal p_price = DataUtils.getBigDecimalVal(orderDetailsMap, "p_price", BigDecimal.ONE);
                goodsPrice = goodsPrice.add(pnum.multiply(p_price));
                //优惠金额
                yh_money = yh_money.add(pnum.multiply(p_price)).subtract(pnum.multiply(p_price).multiply(new BigDecimal(grade_rate_tmp)));

                String sheng   = DataUtils.getStringVal(orderDetailsMap, "sheng", "");
                String shi     = DataUtils.getStringVal(orderDetailsMap, "shi", "");
                String xian    = DataUtils.getStringVal(orderDetailsMap, "xian", "");
                String address = DataUtils.getStringVal(orderDetailsMap, "address", "");

                BigDecimal zheKou = new BigDecimal("0");
                if (orderModel.getGrade_rate().compareTo(BigDecimal.ZERO) >= 0)
                {
                    zheKou = orderModel.getGrade_rate().multiply(new BigDecimal("10"));
                }
                data.put("grade_rate", zheKou);
                // 详细地址
                data.put("address", new StringBuilder().append(sheng).append(shi).append(xian).append(address));
                data.put("sheng", sheng);
                data.put("shi", shi);
                data.put("xian", xian);
                data.put("r_address", address);
                if (MapUtils.getString(orderDetailsMap, "otype").equals(DictionaryConst.OrdersType.ORDERS_HEADER_IN))
                {
                    //800 积分订单的总价格需要修改
                    orderDetailsMap.put("spz_price", goodsPrice);
//                    if (status == ORDERS_R_STATUS_CLOSE) {
//                        orderDetailsMap.put("after_discount", BigDecimal.ZERO);
//                    }
                }
                //发货方信息
                ProductListModel proModel = productListModelMapper.selectByPrimaryKey(pId);
                if (StringUtils.isNotEmpty(proModel.getGongyingshang()))
                {
                    SupplierModel supplierModel = supplierModelMapper.selectByPrimaryKey(proModel.getGongyingshang());
                    data.put("supplierName", supplierModel.getSupplier_name());
                    data.put("supplierPhone", supplierModel.getContact_phone());
                    data.put("supplierAddress", supplierModel.getProvince() + supplierModel.getCity() + supplierModel.getArea() + supplierModel.getAddress());
                    data.put("orderTypeName", "供应商");
                    Map<String, Object> goodsInfo  = DataUtils.cast(SerializePhpUtils.getUnserializeObj(proModel.getInitial(), Map.class));
                    orderDetailsMap.put("supplier_settlement",MapUtils.getDouble(goodsInfo,"yj"));
                }
                // 添加时间
                data.put("add_time", DateUtil.dateFormate(MapUtils.getString(orderDetailsMap, "add_time"), GloabConst.TimePattern.YMDHMS));
                data.put("z_price", DataUtils.getBigDecimalVal(orderDetailsMap, "z_price", BigDecimal.ZERO));
                data.put("user_id", DataUtils.getStringVal(orderDetailsMap, "user_id", ""));
                // 发货时间
                String deliverTime = "";
                if (orderDetailsMap.containsKey("deliver_time"))
                {
                    deliverTime = MapUtils.getString(orderDetailsMap, "deliver_time");
                    if (StringUtils.isNotEmpty(deliverTime))
                    {
                        deliverTime = DateUtil.dateFormate(MapUtils.getString(orderDetailsMap, "deliver_time"), GloabConst.TimePattern.YMDHMS);
                    }
                }
                data.put("deliver_time", deliverTime);
                // 到货时间
                String arriveTime = "";
                if (orderDetailsMap.containsKey("arrive_time"))
                {
                    arriveTime = MapUtils.getString(orderDetailsMap, "arrive_time");
                }
                data.put("arrive_time", arriveTime);

                orderDetailsStatus = DataUtils.getIntegerVal(orderDetailsMap, "status", -1);
                // 订单详情状态
                data.put("r_status", orderDetailsStatus);
                data.put("status01", orderDetailsStatus);
                data.put("gstatus", orderDetailsStatus);
                // 订单类型
                data.put("otype", DataUtils.getStringVal(orderDetailsMap, "otype", ""));
                // 退货原因
                String contentReturn = "";
                if (orderDetailsMap.containsKey("content"))
                {
                    contentReturn = MapUtils.getString(orderDetailsMap, "content");
                }
                data.put("content", contentReturn);
                // 快递公司id
                String express_id = DataUtils.getStringVal(orderDetailsMap, "express_id", "");
                //
                data.put("express_id", express_id);
                String[] express_ids  = express_id.split(SplitUtils.DH);
                String[] courier_nums = DataUtils.getStringVal(orderDetailsMap, "courier_num", "").split(SplitUtils.DH);
                if (!StringUtils.isEmpty(express_id))
                {
                    //查询订单发货记录
                    for (int i = 0; i < express_ids.length; i++)
                    {
                        express_id = express_ids[i];
                        String                    courier_num  = courier_nums[i];
                        ExpressModel              expressModel = expressModelMapper.selectByPrimaryKey(express_id);
                        String                    kuaidi_name  = expressModel.getKuaidi_name();
                        List<Map<String, String>> cnsList      = new ArrayList<>();
                        if (!courier_num_arr.contains(courier_num))
                        {
                            Map<String, String> tmpData = Maps.newConcurrentMap();
                            tmpData.put("num", DataUtils.getStringVal(orderDetailsMap, "num", ""));
                            tmpData.put("kuaidi_name", kuaidi_name);
                            cnsList.add(tmpData);
                            data.put("courier_num", cnsList);
                            courier_num_arr.add(courier_num);
                            kdStr.append(String.format(kdStrFormat, courier_num, kuaidi_name));
                            // 快递公司名称
                            express_name.append(kuaidi_name).append(SplitUtils.DH);
                        }
                    }
                }


                if (StringUtils.isNotEmpty(courier_nums))
                {
                    data.put("fh", 1);
                }
                //单号集合
                data.put("courierList", courier_num_arr);

                // 满减金额
                reduce_price = DataUtils.getBigDecimalVal(orderDetailsMap, "reduce_price", BigDecimal.ZERO);
                // 优惠券金额 优惠金额记录的是 店铺优惠卷+平台优惠卷金额【这里是订单总优惠】
                coupon_price = DataUtils.getBigDecimalVal(orderDetailsMap, "coupon_price", BigDecimal.ZERO);
                //是否是是拆单后的订单
                String pSno = MapUtils.getString(orderDetailsMap, "p_sNo");
                // 平台优惠券金额
                BigDecimal currentPreferentialAmount = DataUtils.getBigDecimalVal(orderDetailsMap, "preferential_amount", BigDecimal.ZERO);
                if (currentPreferentialAmount.compareTo(BigDecimal.ZERO) > 0)
                {
                    //计算店铺优惠金额
                    coupon_price = coupon_price.subtract(currentPreferentialAmount);
                }
                //禅道[32766] 如果是拆单并且使用了优惠则只显示平台优惠,商品金额-平摊后的支付金额=优惠金额
                //重新计算优惠，矫正优惠金额 -> 47765
                /**
                 * 2025-6-17；禅道4151：
                 * 这里会有问题，preferential_amount 如果默认0的话，实际拆单了，但是不会走这个判断，会导致走下面的else
                 * ，如果有多个订单，第二次走if里面会在平台优惠基础上继续添加优惠金额，导致优惠金额错误，所以在外面重新定义一个平台优惠金额，专门用来赋值，移动端也是这么做的
                 */
                if (StringUtils.isNotEmpty(pSno) && (BigDecimal.ZERO.compareTo(preferential_amount) < 0 || BigDecimal.ZERO.compareTo(coupon_price) < 0))
                {
                    isCouponHandle = true;
                    BigDecimal currentDiscountAmount = DataUtils.getBigDecimalVal(orderDetailsMap, "after_discount", BigDecimal.ZERO);
                    //这里是订单明细优惠
                    preferentiaNewlAmount = preferentiaNewlAmount.add(p_price.multiply(pnum).subtract(currentDiscountAmount));
//                    coupon_price = BigDecimal.ZERO;
                }
                /*else
                {
                    preferential_amount = currentPreferentialAmount;
                }*/
                // 积分
                allow = DataUtils.getIntegerVal(orderDetailsMap, "allow", 0);
                String pay     = DataUtils.getStringVal(orderDetailsMap, "pay", "");
                String paytype = "";
                if (paymentModelMap.containsKey(pay))
                {
                    paytype = paymentModelMap.get(pay);
                }
                else if (DictionaryConst.OrderPayType.OFFLINE_PAY.equals(pay))
                {
                    //线下支付特殊处理
                    paytype = "线下支付";
                }
                // 支付方式
                data.put("pay", pay);
                data.put("paytype", paytype);
                data.put("lottery_status", 7);
                data.put("id", sNo);
                // 微信支付交易号
                data.put("trade_no", DataUtils.getStringVal(orderDetailsMap, "trade_no", ""));
                BigDecimal freight = DataUtils.getBigDecimalVal(orderDetailsMap, "", BigDecimal.ZERO);
                yunfei = yunfei.add(freight);
                data.put("paytype", paytype);
                // 根据产品id,查询产品主图
                ProductListModel productListModel = new ProductListModel();
                productListModel.setId(DataUtils.getIntegerVal(orderDetailsMap, "p_id", 0));
                //如果是其它插件订单则获取商品id
                if (!DictionaryConst.OrdersType.ORDERS_HEADER_GM.equals(orderModel.getOtype()))
                {
                    int            attrId         = MapUtils.getIntValue(orderDetailsMap, "sid");
                    ConfiGureModel confiGureModel = confiGureModelMapper.selectByPrimaryKey(attrId);
                    if (confiGureModel == null)
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SPSXBCZ, "商品属性不存在");
                    }
                    productListModel.setId(confiGureModel.getPid());
                    //积分商品使用积分商品库存替换商品库存
                    if (otype.equals(DictionaryConst.OrdersType.ORDERS_HEADER_IN))
                    {
                        orderDetailsMap.put("total_num", integralGoodsModelMapper.selectAttrNumByAttrId(store_id, attrId));
                    }
                }
                //区分实物商品和虚拟商品
                if (DictionaryConst.OrdersType.ORDERS_HEADER_VI.equals(orderModel.getOtype()))
                {
                    productListModel.setCommodity_type(1);
                }
                productListModel = productListModelMapper.selectOne(productListModel);
                if (StringUtils.isNotEmpty(productListModel.getImgurl()))
                {
                    orderDetailsMap.put("pic", publiceService.getImgPath(productListModel.getImgurl(), store_id));
                }
                if (StringUtils.isNotEmpty(productListModel.getGongyingshang()))
                {
                    retMap.put("supplier_id", productListModel.getGongyingshang());
                }
                //供应商端单价处理
                if (adminOrderDetailVo.getSupplierId() != null)
                {
                    ConfiGureModel confiGureModel       = confiGureModelMapper.selectByPrimaryKey(sid);
                    ConfiGureModel supplierProConfigure = confiGureModelMapper.selectByPrimaryKey(confiGureModel.getSupplier_superior());
                    orderDetailsMap.put("p_price", supplierProConfigure.getYprice());
                }
                else
                {
                    //商品单价 p_price GoodsPrice 是num * p_price
                    orderDetailsMap.put("p_price", p_price);
                }

            }
            yh_money = yh_money.add(coupon_price).add(reduce_price);
            // 运费
            data.put("yunfei", yunfei);
            data.put("express_name", express_name.toString());

            getOrderStatus(data, orderDetailsStatus);
            if (adminOrderDetailVo.getSupplierId() != null)
            {
                pay_price = totalSupplyPrice.add(totalFright);
            }
            //平台/店铺优惠金额特殊处理
            if (isCouponHandle)
            {
                //拆单后总优惠金额 - 该店铺优惠金额
                preferential_amount = preferentiaNewlAmount.subtract(coupon_price);
            }
            //订单状态
            List<String> orderStatusCnName = ImmutableList.of("待付款", "待发货", "待收货", "订单完成", "订单关闭");
            //不同类型订单的优惠券处理与总价的处理
            if (otype.equals(DictionaryConst.OrdersType.ORDERS_HEADER_GM) || otype.equals(DictionaryConst.OrdersType.ORDERS_HEADER_VI))
            {
                retMap.put("preferential_amount", preferential_amount);
                retMap.put("coupon_price", coupon_price);
                old_total = goodsPrice.subtract(platform_coupon_price).subtract(store_coupon_price).subtract(grade_rate_amount);
                if (old_total.compareTo(BigDecimal.ZERO) < 0)
                {
                    old_total = BigDecimal.ZERO;
                }
                //防止大额优惠券将运费的钱也减掉
                old_total = old_total.add(z_freight);
                retMap.put("old_total", (old_total));
            }
            else
            {
                retMap.put("coupon_price", coupon_price);
                retMap.put("preferential_amount", preferential_amount);
                retMap.put("old_total", old_total);
            }
            retMap.put("sdata", orderStatusCnName);
//            retMap.put("yh_money", yh_money);
            //满减赠品
            retMap.put("zp_res", new ArrayList<>());
            retMap.put("update_s", update_s);
            retMap.put("data", data);
            retMap.put("detail", adminOrderDetailsList);
            retMap.put("reduce_price", reduce_price);
            retMap.put("comm_amount", comm_amount);
            retMap.put("grade_rate_amount", grade_rate_amount);
            retMap.put("allow", allow);
            retMap.put("num", num);
            retMap.put("discount_type", discount_type);
            retMap.put("pay_price", pay_price);
            //retMap.put("z_freight", old_freight != null && old_freight.compareTo(BigDecimal.ZERO) > 0 ? old_freight : z_freight);
            retMap.put("z_freight", z_freight);
            retMap.put("grade_rate", grade_rate);
            //商品总价小计
            retMap.put("spz_price", goodsPrice);
            //快递
            retMap.put("expressStr", StringUtils.trim(kdStr.toString(), SplitUtils.DH));
            //订单备注
            String remark = "";
            remark = orderModel.getRemark() == null ? "" : orderModel.getRemark();
            retMap.put("remark", remark);
            //是否多店铺订单
            retMap.put("isManyMch", isManyMch);
            //操作者
            retMap.put("operator", adminOrderDetailVo.getOperationName());
            //退款状态
            retMap.put("returnStatus", publicRefundService.getRefundStatus(store_id, sNo));
            //订单总供货价
            retMap.put("totalSupplyPrice", totalSupplyPrice);
            //订单总运费
            retMap.put("totalSupplierFright", totalFright);
            //是否自提
            retMap.put("selfLifting", selfLifting);

            //订单支付时使用的币种
            retMap.put("currency_code", currencyCode);
            //订单支付时币种符号
            retMap.put("currency_symbol", currencySymbol);
            //订单支付时币种对应基础货币的汇率
            retMap.put("exchange_rate", exchangeRate);
        }
        catch (Exception e)
        {
            logger.error("获取订单详情 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_HQDDXQSB, "获取订单详情失败", "orderPcDetails");
        }
        return retMap;
    }

    @Override
    public Map<String, Object> getPaymentConfig(int storeId) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", storeId);
            List<Map<String, Object>> resultPayConfigInfoMap = paymentConfigModelMapper.getPaymentConfigDynamic(parmaMap);
            for (Map<String, Object> map : resultPayConfigInfoMap)
            {
                String key = map.get("class_name").toString();
                resultMap.put(key, map.get("status").toString());
            }
            return resultMap;
        }
        catch (Exception e)
        {
            logger.error("获取支付配置信息 异常: ", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_HQZFPZXX, "获取支付配置信息");
        }
    }

    @Override
    public Map<String, Object> orderDetailsInfo(AdminOrderDetailVo orderVo)
    {
        try
        {
            String sNo = orderVo.getsNo();
            orderVo.setId(sNo);
            orderVo.setsNo(sNo);
            orderVo.setOrderType("see");
            return publicOrderService.orderPcDetails(orderVo);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("订单详情异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.BUSY_NETWORK, "订单编辑失败", "orderDetailsInfo");
        }
    }


    @Override
    public Map<String, Object> editOrderView(OrderModifyVo orderVo) throws LaiKeAPIException
    {
        Map<String, Object> retMap = Maps.newConcurrentMap();
        try
        {
            // 订单id sno
            String             sNo                = orderVo.getsNo();
            AdminOrderDetailVo adminOrderDetailVo = new AdminOrderDetailVo();
            adminOrderDetailVo.setId(sNo);
            adminOrderDetailVo.setsNo(sNo);
            adminOrderDetailVo.setOrderType("modify");
            adminOrderDetailVo.setType(orderVo.getType());
            adminOrderDetailVo.setStoreId(orderVo.getStoreId());
            Map<String, Object> adminOrderDetailsMap = publicOrderService.orderPcDetails(adminOrderDetailVo);
            List<ExpressModel>  expressModels        = publicExpressService.getExpressInfo();
            adminOrderDetailsMap.put("express", expressModels);
            retMap.putAll(adminOrderDetailsMap);
        }
        catch (LaiKeAPIException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            logger.error("订单编辑失败", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.BUSY_NETWORK, "订单详情获取失败", "editOrderView");
        }
        return retMap;
    }

    @Override
    public Map<String, Object> kuaidishow(MainVo vo, String orderNo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            //publiceService.getRedisUserCache(vo);
            resultMap = publicOrderService.getLogistics(orderNo);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取订单物流信息 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.BUSY_NETWORK, "网络异常", "kuaidishow");
        }
        return resultMap;
    }


    @Override
    public Map<String, Object> orderList(OrderVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User user = publiceService.getRedisUserCache(vo);

            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("storeId", vo.getStoreId());
            parmaMap.put("userId", user.getUser_id());
            parmaMap.put("orderType", vo.getQueryOrderType());
            parmaMap.put("status", "notClose");
            parmaMap.put("pageStart", vo.getPageNo());
            parmaMap.put("pageEnd", vo.getPageSize());
            parmaMap.put("otype", DictionaryConst.OrdersType.ORDERS_HEADER_IN);
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
                    mchId = Integer.parseInt(StringUtils.trim(mchIdStr, com.laiketui.core.common.SplitUtils.DH));
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
                    int            attrId         = Integer.parseInt(orderDetails.get("sid").toString());
                    ConfiGureModel confiGureModel = confiGureModelMapper.selectByPrimaryKey(attrId);
                    if (confiGureModel == null)
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SJCW, "数据错误");
                    }
                    int goodsId = confiGureModel.getPid();
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
            logger.error("积分订单列表 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "orderList");
        }
        return resultMap;
    }


    @Override
    public OrderModel getOrderInfoById(int storeId, Integer id, String orderno) throws LaiKeAPIException
    {
        OrderModel orderModel = new OrderModel();
        try
        {
            if (id != null)
            {
                orderModel = orderModelMapper.selectByPrimaryKey(id);
            }
            else
            {
                orderModel.setStore_id(storeId);
                orderModel.setsNo(orderno);
                orderModel = orderModelMapper.selectOne(orderModel);
            }
            return orderModel;
        }
        catch (Exception e)
        {
            logger.error("获取订单信息 异常: ", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常");
        }
    }

    @Override
    public OrderModel getOrderInfoByDetailId(int storeId, int did) throws LaiKeAPIException
    {
        OrderModel orderModel = new OrderModel();
        try
        {
            OrderDetailsModel orderDetailsModel = orderDetailsModelMapper.selectByPrimaryKey(did);
            if (orderDetailsModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DDSJYC, "订单数据异常");
            }
            orderModel.setStore_id(storeId);
            orderModel.setsNo(orderDetailsModel.getR_sNo());
            orderModel = orderModelMapper.selectOne(orderModel);

            return orderModel;
        }
        catch (Exception e)
        {
            logger.error("获取订单信息 异常:", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常");
        }
    }

    @Override
    public Map<String, Object> getSettlementOrderList(OrderSettlementVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            int                 storeId  = vo.getStoreId();
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", vo.getStoreId());
            if (vo.getMchId() != null)
            {
                parmaMap.put("mchId", vo.getMchId());
            }
            //结算订单只显示已完成、订单关闭的订单 供应商订单付了款就能显示
            List<Integer> statusList = new ArrayList<>();
            statusList.add(ORDERS_R_STATUS_COMPLETE);
            statusList.add(ORDERS_R_STATUS_CLOSE);
            if (vo.getSupplierId() != null)
            {
                parmaMap.put("supplierId", vo.getSupplierId());
                statusList.add(ORDERS_R_STATUS_CONSIGNMENT);
                statusList.add(ORDERS_R_STATUS_DISPATCHED);
            }
            if (StringUtils.isNotEmpty(vo.getSupplierOrder()))
            {
                parmaMap.put("supplierOrder", vo.getSupplierOrder());
                statusList.add(ORDERS_R_STATUS_CONSIGNMENT);
                statusList.add(ORDERS_R_STATUS_DISPATCHED);
            }
            parmaMap.put("statusList", statusList);
            parmaMap.put("add_time_sort", DataUtils.Sort.DESC.toString());
            parmaMap.put("start", vo.getPageNo());
            parmaMap.put("pagesize", vo.getPageSize());
            //是否结算
            if (vo.getStatus() != null)
            {
                if (vo.getStatus() == 1)
                {
                    //已结算
                    parmaMap.put("settlement_status", OrderDetailsModel.SETTLEMENT_TYPE_SETTLED);
                }
                else
                {
                    //未结算
                    parmaMap.put("settlement_status", OrderDetailsModel.SETTLEMENT_TYPE_UNSETTLED);
                }
            }
            parmaMap.put("orderId", vo.getId());
            parmaMap.put("mch_id", vo.getId());
            if (StringUtils.isNotEmpty(vo.getSearch()))
            {
                parmaMap.put("search", vo.getSearch());
            }
            if (StringUtils.isNotEmpty(vo.getStartDate()))
            {
                parmaMap.put("startdate", vo.getStartDate() + " 00:00:00");
            }
            if (StringUtils.isNotEmpty(vo.getEndDate()))
            {
                parmaMap.put("enddate", vo.getEndDate() + " 00:00:00");
            }
            if (StringUtils.isNotEmpty(vo.getMchName()))
            {
                parmaMap.put("mch_name", vo.getMchName());
            }
            if (StringUtils.isNotEmpty(vo.getMchSupplierName()))
            {
                parmaMap.put("mchSupplierName", vo.getMchSupplierName());
            }
            //订单类型
            if (StringUtils.isNotEmpty(vo.getOrderType()))
            {
                if (DictionaryConst.OrdersType.ORDERS_HEADER_GM.equals(vo.getOrderType()))
                {
                    List<String> orderTypeList = new ArrayList<>();
                    orderTypeList.add(DictionaryConst.OrdersType.ORDERS_HEADER_GM);
                    //同时显示虚拟订单
                    orderTypeList.add(DictionaryConst.OrdersType.ORDERS_HEADER_VI);
                    parmaMap.put("orderTypeList", orderTypeList);
                }
                else
                {
                    parmaMap.put("orderType", vo.getOrderType());
                }
            }
            parmaMap.put("group_sNo", "group_sNo");
            //不显示未付款订单  禅道：44779
            parmaMap.put("orderStatus", "orderStatus");
            //228 已结算列表中不展示未支付的订单
            parmaMap.put("noPay", "noPay");
            int                       total        = orderModelMapper.countAdminOrderList(parmaMap);
            List<Map<String, Object>> orderList    = new ArrayList<>();
            List<Map<String, Object>> AllorderList = new ArrayList<>();
            Map<String, Object>       Summary      = new HashMap<>();
            //计算结算金额总和
            BigDecimal allSettlementPrice = BigDecimal.ZERO;
            //计算佣金
            BigDecimal allCommission = BigDecimal.ZERO;
            //计算退还佣金
            BigDecimal returnCommission = BigDecimal.ZERO;
            //计算退单金额
            BigDecimal returnMoney = BigDecimal.ZERO;
            //待结算金额
            BigDecimal totalSettlementPrice = BigDecimal.ZERO;
            if (total > 0)
            {
                orderList = orderModelMapper.adminOrderList(parmaMap);
                parmaMap.remove("start");
                parmaMap.remove("pagesize");
                if (StringUtils.isNotEmpty(vo.getStartDate()))
                {
                    parmaMap.put("startdate", vo.getStartDate() + " 00:00:00");
                }
                if (StringUtils.isNotEmpty(vo.getEndDate()))
                {
                    parmaMap.put("enddate", vo.getEndDate() + " 00:00:00");
                }
                AllorderList = orderModelMapper.adminOrderList(parmaMap);
                for (Map<String, Object> map : orderList)
                {
                    String  orderType   = MapUtils.getString(map, "otype");
                    String  orderNo     = MapUtils.getString(map, "sNo");
                    Integer detailId    = MapUtils.getInteger(map, "detailId");
                    Integer p_id        = MapUtils.getInteger(map, "p_id");

                    //获取供应商订单运费
                    Integer supplierId = MapUtils.getInteger(map, "gongyingshang");
                    if (StringUtils.isNotEmpty(supplierId)) {
                        SupplierOrderFrightModel supplierOrderFrightModel = new SupplierOrderFrightModel();
                        supplierOrderFrightModel.setsNo(orderNo);
                        List<SupplierOrderFrightModel> select = supplierOrderFrightModelMapper.select(supplierOrderFrightModel);
                        //总运费
                        BigDecimal zFreight = BigDecimal.ZERO;
                        for (SupplierOrderFrightModel model : select) {
                            zFreight = zFreight.add(model.getFreight());
                        }
                        map.put("freight", zFreight);
                    }
                    String  couponPrice = map.get("coupon_price") + "";
                    if (StringUtils.isEmpty(couponPrice))
                    {
                        couponPrice = "0";
                    }
                    //是否结算标识settlement_status
                    Integer isSettlement = MapUtils.getInteger(map, "settlement_status");
                    int     status       = MapUtils.getIntValue(map, "status");
                    //佣金
                    BigDecimal commission = BigDecimal.ZERO;
                    //退还佣金
                    BigDecimal rcommission = BigDecimal.ZERO;
                    //如果是分销订单则佣金计算
                    if (DictionaryConst.OrdersType.ORDERS_HEADER_FX.equalsIgnoreCase(orderType))
                    {
                        //类型 1:转入(收入) 2:提现 3:个人进获奖8:充值积分
                        commission = distributionRecordModelMapper.sumEstimateAmtByType(1, orderNo);
                        rcommission = distributionRecordModelMapper.sumEstimateAmtByType(9, orderNo);
                    }
                    if (DictionaryConst.OrdersType.ORDERS_HEADER_ZB.equalsIgnoreCase(orderType))
                    {
                        commission = new BigDecimal(map.get("liveCommission").toString()).setScale(2, RoundingMode.HALF_UP);
                    }
                    //退款金额
                    BigDecimal returnAmt = returnOrderModelMapper.getReturnAmtByOrderForSettlement(orderNo);
                    map.put("commission", commission);
                    map.put("r_commission", rcommission);
                    map.put("return_money", returnAmt);

                    //优惠金额(该金额是总优惠券金额 平台+店铺)
                    BigDecimal mchDisAmt = new BigDecimal(couponPrice);
                    //平台优惠金额
                    BigDecimal dicPrice = new BigDecimal(map.get("preferential_amount").toString());
                    if (BigDecimal.ZERO.compareTo(dicPrice) < 0)
                    {
                        //如果用了平台优惠券 则店铺优惠券 = 总优惠券金额-平台优惠金额
                        mchDisAmt = mchDisAmt.subtract(dicPrice);
                    }
                    map.put("mch_discount", mchDisAmt);
                    map.put("preferential_amount", dicPrice);

                    BigDecimal settlementPrice = BigDecimal.ZERO;
                    String     statusName      = "待结算";
                    map.put("arrive_time", DateUtil.dateFormate(MapUtils.getString(map, "arrive_time"), GloabConst.TimePattern.YMDHMS));
                    if (OrderDetailsModel.SETTLEMENT_TYPE_SETTLED.equals(isSettlement))
                    {
                        //结算金额 订单金额就是结算金额
                        statusName = "已结算";
                    } else {
                        map.put("arrive_time", null);
                    }
                    // bug:3274
                    settlementPrice = calcSettlementPrice(map, status, storeId, orderNo);
                    if (Objects.nonNull(MapUtils.getDouble(map,"settlement")))
                    {
                        map.put("supplier_settlement",MapUtils.getDouble(map,"settlement"));
                    }

                    map.put("settlementPrice", settlementPrice);
                    map.put("status_name", statusName);
                    map.put("add_time", DateUtil.dateFormate(MapUtils.getString(map, "add_time"), GloabConst.TimePattern.YMDHMS));

                    //金额字段保留两位小数
                    if (StringUtils.isNotEmpty(MapUtils.getString(map, "z_price")))
                    {
                        map.put("z_price", String.valueOf(new BigDecimal(MapUtils.getString(map, "z_price")).setScale(2, RoundingMode.HALF_DOWN).toString()));
                    }
                    if (StringUtils.isNotEmpty(MapUtils.getString(map, "preferential_amount")))
                    {
                        map.put("preferential_amount", String.valueOf(new BigDecimal(MapUtils.getString(map, "preferential_amount")).setScale(2, RoundingMode.HALF_DOWN)));
                    }
                    if (StringUtils.isNotEmpty(MapUtils.getString(map, "mch_discount")))
                    {
                        map.put("mch_discount", String.valueOf(new BigDecimal(MapUtils.getString(map, "mch_discount")).setScale(2, RoundingMode.HALF_DOWN)));
                    }
                    if (StringUtils.isNotEmpty(MapUtils.getString(map, "z_freight")))
                    {
                        map.put("z_freight", String.valueOf(new BigDecimal(MapUtils.getString(map, "z_freight")).setScale(2, RoundingMode.HALF_DOWN)));
                    }
                    if (StringUtils.isNotEmpty(MapUtils.getString(map, "commission")))
                    {
                        map.put("commission", String.valueOf(new BigDecimal(MapUtils.getString(map, "commission")).setScale(2, RoundingMode.HALF_DOWN)));
                    }
                    if (StringUtils.isNotEmpty(MapUtils.getString(map, "r_commission")))
                    {
                        map.put("r_commission", String.valueOf(new BigDecimal(MapUtils.getString(map, "r_commission")).setScale(2, RoundingMode.HALF_DOWN)));
                    }
                    if (StringUtils.isNotEmpty(MapUtils.getString(map, "return_money")))
                    {
                        map.put("return_money", String.valueOf(new BigDecimal(MapUtils.getString(map, "return_money")).setScale(2, RoundingMode.HALF_DOWN)));
                        map.put("reduce_price", String.valueOf(new BigDecimal(MapUtils.getString(map, "return_money")).setScale(2, RoundingMode.HALF_DOWN)));
                    }
                    if (StringUtils.isNotEmpty(MapUtils.getString(map, "settlementPrice")))
                    {
                        map.put("settlementPrice", String.valueOf(new BigDecimal(MapUtils.getString(map, "settlementPrice")).setScale(2, RoundingMode.HALF_DOWN)));
                    }
                }

                for (Map<String, Object> map : AllorderList)
                {
                    int     status       = MapUtils.getIntValue(map, "status");
                    String orderNo   = MapUtils.getString(map, "sNo");
                    String orderType = MapUtils.getString(map, "otype");
                    //退单金额
                    returnMoney = returnMoney.add(returnOrderModelMapper.getReturnAmtByOrderForSettlement(orderNo));
                    if (DictionaryConst.OrdersType.ORDERS_HEADER_FX.equalsIgnoreCase(orderType))
                    {
                        //类型 1:转入(收入) 2:提现 3:个人进获奖8:充值积分
                        allCommission = allCommission.add(distributionRecordModelMapper.sumEstimateAmtByType(1, orderNo));
                        returnCommission = returnCommission.add(distributionRecordModelMapper.sumEstimateAmtByType(9, orderNo));
                    }
                    if (DictionaryConst.OrdersType.ORDERS_HEADER_ZB.equalsIgnoreCase(orderType))
                    {
                        allCommission = allCommission.add(new BigDecimal(map.get("liveCommission").toString()).setScale(2, RoundingMode.HALF_UP));
                    }
                    BigDecimal settlementPrice = calcSettlementPrice(map, status, storeId, orderNo);
                    totalSettlementPrice = totalSettlementPrice.add(settlementPrice);
                }
                Summary.put("commission", allCommission);
                Summary.put("allSettlementPrice", totalSettlementPrice);
                Summary.put("returnCommission", returnCommission);
                Summary.put("returnMoney", returnMoney);
                Summary.put("total", AllorderList.size());
            }
            resultMap.put("list", orderList);
            resultMap.put("allSettlementPrice", totalSettlementPrice);
            resultMap.put("summary", Summary);
            resultMap.put("total", total);
        }
        catch (Exception e)
        {
            logger.error("获取订单信息 异常:", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> getOrderConfig(int storeId, String oType) throws LaiKeAPIException
    {
        try
        {
            return getOrderConfig(storeId, null, oType);
        }
        catch (Exception e)
        {
            logger.error("根据订单类型获取订单配置 异常:", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常");
        }
    }


    @Override
    public Map<String, Object> getOrderConfig(int storeId, Integer mchId, String oType) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            return getOrderConfig(storeId, mchId, oType, true);
        }
        catch (Exception e)
        {
            logger.error("根据订单类型获取订单配置 异常:", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常");
        }
    }

    @Override
    public Map<String, Object> getOrderConfig(int storeId, Integer mchId, String oType, boolean isOpenConfig) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            //获取自营店id
            Integer storeMchId = customerModelMapper.getStoreMchId(storeId);
            //自动收货时间
            int autoTeGood = DateUtil.dateConversion(7, DateUtil.TimeType.DAY);
            //是否需要结算,如果是false则不走结算
            boolean isSettlement = true;
            //订单失效
            int orderFailureDay = DateUtil.dateConversion(1, DateUtil.TimeType.DAY);
            //自动评价内容
            String autoCommentContent = "评价方未及时评价，系统自动默认好评";
            //发货提醒限制
            int remind = DateUtil.dateConversion(1, DateUtil.TimeType.TIME);
            //定时好评
            int commentDay = DateUtil.dateConversion(7, DateUtil.TimeType.DAY);
            //订单售后时间
            int                 orderAfter = DateUtil.dateConversion(7, DateUtil.TimeType.DAY);
            OrderConfigModal orderConfigModal = new OrderConfigModal();
            orderConfigModal.setStore_id(storeId);
            orderConfigModal = orderConfigModalMapper.selectOne(orderConfigModal);
            if (Objects.nonNull(orderConfigModal) && Objects.nonNull(orderConfigModal.getOrder_failure()))
            {
                resultMap.put("order_failure",orderConfigModal.getOrder_failure());
            }
            Map<String, Object> paramMap   = new HashMap<>(1);
            switch (oType.toUpperCase())
            {
                //暂定虚拟商品和普通商品一样
                case DictionaryConst.OrdersType.ORDERS_HEADER_VI:
                    //获取订单配置信息 普通订单设置单位是天 插件都是秒
                case DictionaryConst.OrdersType.ORDERS_HEADER_GM:
                case DictionaryConst.OrdersType.ORDERS_HEADER_FX:
                    // TODO
                case DictionaryConst.OrdersType.ORDERS_HEADER_ZB:
                    if (orderConfigModal != null)
                    {
                        autoTeGood = DateUtil.dateConversion(orderConfigModal.getAuto_the_goods(), DateUtil.TimeType.DAY);
                        orderFailureDay = DateUtil.dateConversion(orderConfigModal.getOrder_failure(), DateUtil.TimeType.TIME);
                        remind = DateUtil.dateConversion(orderConfigModal.getRemind(), DateUtil.TimeType.TIME);
                        commentDay = DateUtil.dateConversion(orderConfigModal.getAuto_good_comment_day(), DateUtil.TimeType.DAY);
                        orderAfter = DateUtil.dateConversion(orderConfigModal.getOrder_after(), DateUtil.TimeType.DAY);
                        if (StringUtils.isNotEmpty(orderConfigModal.getAuto_good_comment_content()))
                        {
                            autoCommentContent = orderConfigModal.getAuto_good_comment_content();
                        }
                    }
                    break;
                case DictionaryConst.OrdersType.ORDERS_HEADER_MS:
                    SecondsConfigModel secondsConfigModel = new SecondsConfigModel();
                    secondsConfigModel.setStore_id(storeId);
                    secondsConfigModel.setMch_id(storeMchId);
                    secondsConfigModel = secondsConfigModelMapper.selectOne(secondsConfigModel);
                    if (secondsConfigModel == null || secondsConfigModel.getIs_open() == DictionaryConst.WhetherMaven.WHETHER_NO)
                    {
                        logger.debug("【秒杀】插件未开启/未配置");
                        return null;
                    }
                    autoTeGood = secondsConfigModel.getAuto_the_goods();
                    orderFailureDay = secondsConfigModel.getOrder_failure();
                    remind = secondsConfigModel.getDeliver_remind();
                    commentDay = secondsConfigModel.getAuto_good_comment_day();
                    //秒杀售后默认15天...
                    orderAfter = secondsConfigModel.getOrder_after();
                    autoCommentContent = secondsConfigModel.getAuto_good_comment_content();
                    break;
                case DictionaryConst.OrdersType.ORDERS_HEADER_IN:
                    IntegralConfigModel integralConfigModel = new IntegralConfigModel();
                    integralConfigModel.setStore_id(storeId);
                    integralConfigModel.setMch_id(storeMchId);
                    integralConfigModel = integralConfigModelMapper.selectOne(integralConfigModel);
                    if (integralConfigModel == null || integralConfigModel.getStatus() == DictionaryConst.WhetherMaven.WHETHER_NO)
                    {
                        logger.debug("【积分】插件未开启/未配置");
                        return null;
                    }
                    autoTeGood = integralConfigModel.getAuto_the_goods();
                    orderFailureDay = integralConfigModel.getOrder_failure();
                    remind = integralConfigModel.getDeliver_remind();
                    commentDay = integralConfigModel.getAuto_good_comment_day();
                    orderAfter = integralConfigModel.getOrder_after();
                    if (StringUtils.isNotEmpty(integralConfigModel.getAuto_good_comment_content()))
                    {
                        autoCommentContent = integralConfigModel.getAuto_good_comment_content();
                    }
                    break;
                case DictionaryConst.OrdersType.ORDERS_HEADER_PS:
                    PreSellConfigModel preSellConfigModel = new PreSellConfigModel();
                    preSellConfigModel.setStore_id(storeId);
                    preSellConfigModel = preSellConfigModelMapper.selectOne(preSellConfigModel);
                    if (Objects.isNull(preSellConfigModel) || preSellConfigModel.getIs_open() == DictionaryConst.WhetherMaven.WHETHER_NO)
                    {
                        logger.debug("【预售】插件未开启/未配置");
                        return null;
                    }
                    autoTeGood = preSellConfigModel.getAuto_the_goods();
                    orderFailureDay = preSellConfigModel.getOrder_failure();
                    remind = preSellConfigModel.getDeliver_remind();
                    commentDay = preSellConfigModel.getAuto_good_comment_day();
                    orderAfter = preSellConfigModel.getOrder_after();
                    if (StringUtils.isNotEmpty(preSellConfigModel.getAuto_good_comment_content()))
                    {
                        autoCommentContent = preSellConfigModel.getAuto_good_comment_content();
                        logger.info("【预售】插件自动回复内容为{}", autoCommentContent);
                    }
                    break;
                case DictionaryConst.OrdersType.ORDERS_HEADER_JP:
                    paramMap.put("storeId", storeId);
                    try
                    {
                        resultMap = httpApiUtils.executeHttpApi("plugin.auction.getPluginConfig", paramMap, MediaType.MULTIPART_FORM_DATA_VALUE);
                        if (!isOpenConfig)
                        {
                            int isOpen = MapUtils.getIntValue(resultMap, "isOpen");
                            if (DictionaryConst.WhetherMaven.WHETHER_NO == isOpen)
                            {
                                logger.debug("【竞拍】插件未开启/未配置");
                                return null;
                            }
                        }
                        orderFailureDay = MapUtils.getInteger(resultMap, "orderFailureDay");
                        remind = MapUtils.getInteger(resultMap, "remind");
                        //竞拍有佣金结算,这里不结算
                        isSettlement = false;
                    }
                    catch (LaiKeAPIException i)
                    {
                        logger.debug("【竞拍】获取配置异常", i);
                        return null;
                    }
                    break;
                case DictionaryConst.OrdersType.ORDERS_HEADER_PT:
                    try
                    {
                        paramMap.put("storeId", storeId);
                        resultMap = httpApiUtils.executeHttpApi("plugin.group.http.getOrderConfig", paramMap, MediaType.MULTIPART_FORM_DATA_VALUE);
                        if (!isOpenConfig)
                        {
                            int isOpen = MapUtils.getIntValue(resultMap, "isOpen");
                            if (DictionaryConst.WhetherMaven.WHETHER_NO == isOpen)
                            {
                                logger.debug("【拼团】插件未开启/未配置");
                                return null;
                            }
                        }
                        orderAfter = DateUtil.dateConversion(MapUtils.getInteger(resultMap, "orderAfter"), DateUtil.TimeType.DAY);
                        autoTeGood = MapUtils.getInteger(resultMap, "autoTeGood");
                        commentDay = MapUtils.getInteger(resultMap, "commentDay");
                        autoCommentContent = MapUtils.getString(resultMap, "autoCommentContent");
                        resultMap.put("content", MapUtils.getString(resultMap, "ruleContent"));
                    }
                    catch (LaiKeAPIException i)
                    {
                        logger.debug("【拼团】获取配置异常", i);
                        return null;
                    }
                    break;
                case DictionaryConst.OrdersType.ORDERS_HEADER_FS:
                    OrderConfigModal orderConfigModal1 = new OrderConfigModal();
                    orderConfigModal1.setStore_id(storeId);
                    orderConfigModal1 = orderConfigModalMapper.selectOne(orderConfigModal1);

                    FlashsaleConfigModel flashsaleConfigModel = new FlashsaleConfigModel();
                    flashsaleConfigModel.setStoreId(storeId);
                    flashsaleConfigModel.setMchId(0);
                    flashsaleConfigModel = flashsaleConfigModelMapper.selectOne(flashsaleConfigModel);
                    if (flashsaleConfigModel == null)
                    {
                        logger.debug("【限时折扣】插件未开启/未配置");
                        break;
                    }
                    if (orderConfigModal1 != null)
                    {
                        orderFailureDay = DateUtil.dateConversion(orderConfigModal1.getOrder_failure(), DateUtil.TimeType.TIME);
                        remind = DateUtil.dateConversion(orderConfigModal1.getRemind(), DateUtil.TimeType.TIME);
                    }
                    autoTeGood = flashsaleConfigModel.getAutoTheGoods();
                    commentDay = flashsaleConfigModel.getAutoGoodCommentDay();
                    //秒杀售后默认15天...
                    orderAfter = flashsaleConfigModel.getOrderAfter();
                    if (StringUtils.isNotEmpty(flashsaleConfigModel.getAutoGoodCommentContent()))
                    {
                        autoCommentContent = flashsaleConfigModel.getAutoGoodCommentContent();
                    }
                    break;
                default:
                    return null;
            }

            resultMap.put("isSettlement", isSettlement);
            resultMap.put("remind", remind);
            resultMap.put("commentDay", commentDay);
            resultMap.put("autoTeGood", autoTeGood);
            resultMap.put("orderAfter", orderAfter);
            resultMap.put("orderFailureDay", orderFailureDay);
            resultMap.put("autoCommentContent", autoCommentContent);
        }
        catch (Exception e)
        {
            logger.error("根据订单类型获取订单配置 异常:", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常");
        }
        return resultMap;
    }

    @Override
    public void addOrderConfig(AddPluginOrderConfigVo vo) throws LaiKeAPIException
    {
        try
        {
            if (StringUtils.isEmpty(vo.getPluginCode()))
            {
                vo.setPluginCode("");
            }
            Map<String, Object> params = new HashMap<>(16);
            switch (vo.getPluginCode().toLowerCase())
            {
                case DictionaryConst.Plugin.GOGROUP:
                    params.put("params", JSON.toJSONString(vo));
                    try
                    {
                        httpApiUtils.executeApi("plugin.group.http.addOrderConfig", JSON.toJSONString(params));
                    }
                    catch (LaiKeAPIException i)
                    {
                        logger.debug("【拼团】获取配置异常", i);
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "操作失败");
                    }
                    break;
                default:
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "插件不存在");
            }
        }
        catch (Exception e)
        {
            logger.error("添加插件配置 异常:", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常");
        }
    }

    @Override
    public void orderAfterSaleExpire(int storeId, Integer mchId, String oType, Date arriveTime) throws LaiKeAPIException
    {
        try
        {
            if (StringUtils.isEmpty(oType))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误");
            }
            if (arriveTime == null)
            {
                return;
            }
            //判断订单是否已过售后期
            Map<String, Object> configMap = publicOrderService.getOrderConfig(storeId, mchId, oType.toUpperCase());
            if (configMap == null)
            {
                logger.debug("【{}】插件-未开启/未配置 ", oType);
                return;
            }
            //售后日期 单位【秒】
            int orderAfterSec = MapUtils.getInteger(configMap, "orderAfter");
            //计算最终售后期
            Date arriveDate = DateUtil.getAddDateBySecond(arriveTime, orderAfterSec);
            logger.debug("【{}】订单最终受后期为:{} ", oType, DateUtil.dateFormate(arriveDate, GloabConst.TimePattern.YMDHMS));
            if (DateUtil.dateCompare(new Date(), arriveDate))
            {
                logger.debug("订单已过受后期");
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YGSHQ, "已过售后期", "returnData");
            }
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
    }

    @Override
    public int orderCommentType(int storeId, String userId, String orderNo, int detailId, int sid, int orderStatus) throws LaiKeAPIException
    {
        int type = 0;
        try
        {
            //判断订单评论状态
            CommentsModel commentsModel = new CommentsModel();
            commentsModel.setStore_id(storeId);
            commentsModel.setAttribute_id(sid);
            commentsModel.setUid(userId);
            commentsModel.setOid(orderNo);
            commentsModel = commentsModelMapper.selectOne(commentsModel);
            if (commentsModel != null)
            {
                //待追评
                type = 2;
                if (!org.springframework.util.StringUtils.isEmpty(commentsModel.getReview()))
                {
                    //评论完成
                    type = 3;
                }
            }
            else
            {
                if (orderStatus == ORDERS_R_STATUS_COMPLETE)
                {
                    //待评价
                    type = 1;
                }
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("订单是否可以评价 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "orderCommentType");
        }
        return type;
    }

    //获取本月第一天
    public Date getMonthFirstDate(Date date)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);          // 设置当前日历实例的时间
        calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH));   // 设置日历实例的月取值
        calendar.set(Calendar.DATE, 1);        // 设置日历实例的日取值
        return calendar.getTime();      // 得到日历实例表示的当前时间
    }


    @Override
    public List<Map<String, Object>> getReportData(int storeid) throws LaiKeAPIException
    {
        List<Map<String, Object>> resultList = new ArrayList<>();
        try
        {
            Date         currentDate = DateUtil.dateFormateToDate(new Date(), GloabConst.TimePattern.YMD);
            List<String> dateList    = new ArrayList<>();
            //当前天
            dateList.add(DateUtil.dateFormate(currentDate, GloabConst.TimePattern.YMD));
            //减一天
            dateList.add(DateUtil.dateFormate(DateUtil.getAddDate(currentDate, -1), GloabConst.TimePattern.YMD));
            //当前月
            dateList.add(DateUtil.dateFormate(currentDate, GloabConst.TimePattern.YM));
            //获取本月第一天
//            dateList.add(DateUtil.dateFormate(getMonthFirstDate(new Date()), GloabConst.TimePattern.YMD));

            //订单数
            Map<String, Object> detailMap = new HashMap<>();
            //订单金额
            Map<String, Object> moneyMap = new HashMap<>();
            //退款金额
            Map<String, Object> returnMap       = new HashMap<>();
            Integer             yesterdayNum    = 0;
            Integer             todayNum        = 0;
            BigDecimal          yesterdayMoney  = BigDecimal.ZERO;
            BigDecimal          todaydayMoney   = BigDecimal.ZERO;
            BigDecimal          yesterdayReturn = BigDecimal.ZERO;
            BigDecimal          todaydayReturn  = BigDecimal.ZERO;
            for (int i = 0; i < dateList.size(); i++)
            {
                switch (i)
                {
                    case 0:
                        todayNum = orderModelMapper.countByDate(storeid, dateList.get(i));
                        todaydayMoney = orderModelMapper.sumByDate(storeid, dateList.get(i));
                        todaydayReturn = returnOrderModelMapper.sumByDate(storeid, dateList.get(i));
                        detailMap.put("today_num", todayNum == null ? 0 : todayNum);
                        moneyMap.put("today_money", todaydayMoney == null ? 0 : todaydayMoney);
                        returnMap.put("today_return", todaydayReturn == null ? 0 : todaydayReturn);
                        break;
                    case 1:
                        yesterdayNum = orderModelMapper.countByDate(storeid, dateList.get(i));
                        yesterdayMoney = orderModelMapper.sumByDate(storeid, dateList.get(i));
                        yesterdayReturn = returnOrderModelMapper.sumByDate(storeid, dateList.get(i));
                        detailMap.put("yesterday_num", yesterdayNum == null ? 0 : yesterdayNum);
                        moneyMap.put("yesterday_money", yesterdayMoney == null ? 0 : yesterdayMoney);
                        returnMap.put("yesterday_return", yesterdayReturn == null ? 0 : yesterdayReturn);
                        break;
                    case 2:
                        detailMap.put("month_num", orderModelMapper.countByModel(storeid, dateList.get(i)) == null ? 0 : orderModelMapper.countByModel(storeid, dateList.get(i)));
                        moneyMap.put("month_money", orderModelMapper.sumByModel(storeid, dateList.get(i)) == null ? BigDecimal.ZERO : orderModelMapper.sumByModel(storeid, dateList.get(i)));
                        returnMap.put("month_return", returnOrderModelMapper.sumByModel(storeid, dateList.get(i)) == null ? BigDecimal.ZERO : returnOrderModelMapper.sumByModel(storeid, dateList.get(i)));
                        break;


                }
            }
//            [{"yesterday_num":45,"today_num":12,"add_rate":"18%","month_num":330,"all_num":383},{"add_money_rate":"-34%","month_money":44208.59,"today_money":725.60,"yesterday_money":2819.40,"all_money":89116.25},{"all_return":8021056.59,"yesterday_return":0.0,"month_return":10882.0,"today_return":0,"all_return_rate":"12%"}]
            NumberFormat numberFormat = NumberFormat.getInstance();
            numberFormat.setMaximumFractionDigits(2);
            detailMap.put("all_num", orderModelMapper.countAllOrder(storeid));
            if ((todayNum - yesterdayNum) == 0)
            {
                detailMap.put("add_rate", "0%");
            }
            else
            {
                detailMap.put("add_rate", yesterdayNum == 0 ? "100%" : numberFormat.format(Math.abs((float) todayNum - yesterdayNum) / (float) yesterdayNum * 100) + "%");

            }
            detailMap.put("flag", todayNum - yesterdayNum >= 0 ? "up" : "down");

            moneyMap.put("all_money", orderModelMapper.AllMoeny(storeid));
            if (todaydayMoney.subtract(yesterdayMoney).compareTo(BigDecimal.ZERO) == 0)
            {
                moneyMap.put("add_money_rate", "0%");
            }
            else
            {
                moneyMap.put("add_money_rate", yesterdayMoney.compareTo(BigDecimal.ZERO) == 0 ? "100%" : numberFormat.format(Math.abs(todaydayMoney.subtract(yesterdayMoney).floatValue()) / yesterdayMoney.floatValue() * 100) + "%");

            }
            moneyMap.put("flag", todaydayMoney.compareTo(yesterdayMoney) >= 0 ? "up" : "down");

            returnMap.put("all_return", returnOrderModelMapper.sumAll(storeid));
            if (todaydayReturn.subtract(yesterdayReturn).compareTo(BigDecimal.ZERO) == 0)
            {
                returnMap.put("all_return_rate", "0%");
            }
            else
            {
                returnMap.put("all_return_rate", yesterdayReturn.compareTo(BigDecimal.ZERO) == 0 ? "100%" : numberFormat.format(Math.abs(todaydayReturn.subtract(yesterdayReturn).floatValue()) / yesterdayReturn.floatValue() * 100) + "%");
            }
            returnMap.put("flag", todaydayReturn.compareTo(yesterdayReturn) >= 0 ? "up" : "down");

            resultList.add(detailMap);
            resultList.add(moneyMap);
            resultList.add(returnMap);
        }
        catch (Exception e)
        {
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_, "统计异常！");
        }
        return resultList;
    }

    public Map<String, Object> getOrderData(int storeid) throws LaiKeAPIException
    {
        try
        {
            Date       currentDate = DateUtil.dateFormateToDate(new Date(), GloabConst.TimePattern.YMD);
            List<Date> dateList    = new ArrayList<>();
            dateList.add(DateUtil.getAddDate(currentDate, -7));
            dateList.add(DateUtil.getAddDateByMonth(currentDate, -1));
            dateList.add(DateUtil.getAddDateByYear(currentDate, -1));
            Map<String, Object> detailMap = new HashMap<>();

            for (int i = 0; i < dateList.size(); i++)
            {
                List<List> dataList = new ArrayList<>();
                if (i < 2)
                {
                    List<Date>    days   = DateUtil.createDays(dateList.get(i));
                    List<String>  daystr = new ArrayList<>();
                    List<Integer> nums   = new ArrayList<>();
                    for (Date day : days)
                    {
                        String paramday = DateUtil.dateFormate(day, GloabConst.TimePattern.YMD);
                        daystr.add(paramday);
                        Integer orderNumByDate = orderModelMapper.getOrderNumByDate(storeid, paramday);
                        nums.add(orderNumByDate == null ? 0 : orderNumByDate);
                    }
                    dataList.add(daystr);
                    dataList.add(nums);

                }
                else
                {
                    List<Date>    days   = DateUtil.createMonths(dateList.get(i));
                    List<Integer> nums   = new ArrayList<>();
                    List<String>  daystr = new ArrayList<>();
                    for (Date day : days)
                    {
                        String paramday = DateUtil.dateFormate(day, GloabConst.TimePattern.YM);
                        daystr.add(paramday);
                        Integer orderNumByDate = orderModelMapper.getOrderNumByMonth(storeid, paramday);
                        nums.add(orderNumByDate == null ? 0 : orderNumByDate);
                    }
                    dataList.add(daystr);
                    dataList.add(nums);
                }
                switch (i)
                {
                    case 0:
                        detailMap.put("week", dataList);
                        break;
                    case 1:
                        detailMap.put("month", dataList);
                        break;
                    case 2:
                        detailMap.put("year", dataList);
                        break;
                }
            }
            return detailMap;
        }
        catch (Exception e)
        {
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_, "网络异常", "getOrderData");
        }

    }

    public Map<String, Object> getRefundOrderData(int storeid) throws LaiKeAPIException
    {
        try
        {
            Date       currentDate = DateUtil.dateFormateToDate(new Date(), GloabConst.TimePattern.YMD);
            List<Date> dateList    = new ArrayList<>();
            dateList.add(DateUtil.getAddDate(currentDate, -7));
            dateList.add(DateUtil.getAddDateByMonth(currentDate, -1));
            dateList.add(DateUtil.getAddDateByYear(currentDate, -1));
            Map<String, Object> detailMap = new HashMap<>();

            for (int i = 0; i < dateList.size(); i++)
            {
                List<List> dataList = new ArrayList<>();
                if (i < 2)
                {
                    List<Date>    days       = DateUtil.createDays(dateList.get(i));
                    List<String>  daystr     = new ArrayList<>();
                    List<Integer> nums       = new ArrayList<>();
                    List<Integer> refund_num = new ArrayList<>();
                    for (Date day : days)
                    {
                        String paramday = DateUtil.dateFormate(day, GloabConst.TimePattern.YMD);
                        daystr.add(paramday);
                        Integer orderNumByDate = orderModelMapper.getOrderNumByDate(storeid, paramday);
                        Integer refundNum      = returnOrderModelMapper.countBydays(storeid, paramday);
                        refund_num.add(refundNum == null ? 0 : refundNum);
                        nums.add(orderNumByDate == null ? 0 : orderNumByDate);
                    }
                    dataList.add(daystr);
                    dataList.add(nums);
                    dataList.add(refund_num);

                }
                else
                {
                    List<Date>    days       = DateUtil.createMonths(dateList.get(i));
                    List<Integer> nums       = new ArrayList<>();
                    List<String>  daystr     = new ArrayList<>();
                    List<Integer> refund_num = new ArrayList<>();
                    for (Date day : days)
                    {
                        String paramday = DateUtil.dateFormate(day, GloabConst.TimePattern.YM);
                        daystr.add(paramday);
                        Integer orderNumByDate = orderModelMapper.getOrderNumByMonth(storeid, paramday);
                        Integer refundNum      = returnOrderModelMapper.countByMonth(storeid, paramday);
                        refund_num.add(refundNum == null ? 0 : refundNum);
                        nums.add(orderNumByDate == null ? 0 : orderNumByDate);
                    }
                    dataList.add(daystr);
                    dataList.add(nums);
                    dataList.add(refund_num);
                }

                switch (i)
                {
                    case 0:
                        detailMap.put("week", dataList);
                        break;
                    case 1:
                        detailMap.put("month", dataList);
                        break;
                    case 2:
                        detailMap.put("year", dataList);
                        break;
                }
            }
            return detailMap;
        }
        catch (Exception e)
        {
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_, "网络异常", "getRefundOrderData");
        }


    }

    public Map<String, Object> getOrderDataByStatus(int storeid, int status) throws LaiKeAPIException
    {
        try
        {
            Map<String, Object> resultMap = new HashMap<>();
            Map<String, Object> dayMap    = new HashMap<>();
            Map<String, Object> weekMap   = new HashMap<>();
            NumberFormat        dF        = NumberFormat.getInstance();
            dF.setMaximumFractionDigits(0);
            List<String> dataList = new ArrayList<>();
            //当前时间
            String currentDate = DateUtil.dateFormate(new Date(), GloabConst.TimePattern.YMD);
            //当前时间减一天
            String yesterdayDate = DateUtil.dateFormate(DateUtil.getAddDate(new Date(), -1), GloabConst.TimePattern.YMD);
            //上周
            String preDate = DateUtil.dateFormate(DateUtil.getAddDate(new Date(), -6), GloabConst.TimePattern.YMD);
            //减14天
            String weekDate = DateUtil.dateFormate(DateUtil.getAddDate(new Date(), -13), GloabConst.TimePattern.YMD);
            //上一月
            String monthDate = DateUtil.dateFormate(DateUtil.getAddMonth(new Date(), -1), GloabConst.TimePattern.YMD);
            //减一月+减一星期
            String premonthDate = DateUtil.dateFormate(DateUtil.getAddDate(DateUtil.getAddMonth(new Date(), -1), -6), GloabConst.TimePattern.YMD);
            //当日数量
            Integer todayAmount;
            //昨天
            Integer yesterdayAmount;
            //上周的今天
            Integer preAmount;
            //本周
            Integer prePeriodAmount;
            //上周
            Integer weekPeriodAmount;
            //上个月的当前星期
            Integer monthPeriodAmount;

            Integer substractValue;
            Integer substractValue1;
            Integer substractValue3;
            Integer substractValue4;
            if (status == 4)
            {
                todayAmount = returnOrderModelMapper.getOrderNumByPeriod(storeid, currentDate, currentDate);
                yesterdayAmount = returnOrderModelMapper.getOrderNumByPeriod(storeid, yesterdayDate, yesterdayDate);
                preAmount = returnOrderModelMapper.getOrderNumByPeriod(storeid, preDate, preDate);

                substractValue = todayAmount - yesterdayAmount;
                substractValue1 = todayAmount - preAmount;

                prePeriodAmount = returnOrderModelMapper.getOrderNumByPeriod(storeid, preDate, currentDate);
                weekPeriodAmount = returnOrderModelMapper.getOrderNumByPeriod(storeid, weekDate, preDate);
                monthPeriodAmount = returnOrderModelMapper.getOrderNumByPeriod(storeid, premonthDate, monthDate);

                substractValue3 = prePeriodAmount - weekPeriodAmount;
                substractValue4 = prePeriodAmount - monthPeriodAmount;
            }
            else
            {
                todayAmount = orderModelMapper.getOrderNumByPeriod(storeid, status, currentDate, currentDate);
                yesterdayAmount = orderModelMapper.getOrderNumByPeriod(storeid, status, yesterdayDate, yesterdayDate);
                preAmount = orderModelMapper.getOrderNumByPeriod(storeid, status, preDate, preDate);
                substractValue = todayAmount - yesterdayAmount;
                substractValue1 = todayAmount - preAmount;

                prePeriodAmount = orderModelMapper.getOrderNumByPeriod(storeid, status, preDate, currentDate);
                weekPeriodAmount = orderModelMapper.getOrderNumByPeriod(storeid, status, weekDate, preDate);
                monthPeriodAmount = orderModelMapper.getOrderNumByPeriod(storeid, status, premonthDate, monthDate);

                substractValue3 = prePeriodAmount - weekPeriodAmount;
                substractValue4 = prePeriodAmount - monthPeriodAmount;
            }
            dayMap.put("num", todayAmount);
            dayMap.put("hbflag", substractValue >= 0 ? "up" : "down");
            if (substractValue == 0)
            {
                dayMap.put("hbrate", "0%");
            }
            else
            {
                dayMap.put("hbrate", yesterdayAmount == 0 ? "100%" : dF.format((Math.abs(substractValue.floatValue()) / yesterdayAmount.floatValue() * 100)) + "%");
            }
            if (substractValue1 == 0)
            {
                dayMap.put("tbrate", "0%");
            }
            else
            {
                dayMap.put("tbrate", preAmount == 0 ? "100%" : dF.format(Math.abs(substractValue1.floatValue()) / preAmount.floatValue() * 100) + "%");
            }
            dayMap.put("tbflag", substractValue1 >= 0 ? "up" : "down");
            dataList.add(JSON.toJSONString(dayMap));
            weekMap.put("num", prePeriodAmount);
            weekMap.put("hbflag", substractValue3 >= 0 ? "up" : "down");
            if (substractValue3 == 0)
            {
                weekMap.put("hbrate", "0");
            }
            else
            {
                weekMap.put("hbrate", weekPeriodAmount == 0 ? "100%" : dF.format((Math.abs(substractValue3.floatValue()) / weekPeriodAmount.floatValue() * 100)) + "%");

            }
            if (substractValue4 == 0)
            {
                weekMap.put("tbrate", "0%");
            }
            else
            {
                weekMap.put("tbrate", monthPeriodAmount == 0 ? "100%" : dF.format(Math.abs(substractValue4.floatValue()) / monthPeriodAmount.floatValue() * 100) + "%");

            }
            weekMap.put("tbflag", substractValue4 >= 0 ? "up" : "down");
            dataList.add(JSON.toJSONString(weekMap));
            resultMap.put("data", dataList);
            switch (status)
            {
                case 0:
                    resultMap.put("name", "待付款");
                    break;
                case 1:
                    resultMap.put("name", "待发货");
                    break;
                case 2:
                    resultMap.put("name", "待收获");
                    break;
                case 3:
                    resultMap.put("name", "待评价");
                case 4:
                    resultMap.put("name", "待处理");
            }
            return resultMap;
        }
        catch (Exception e)
        {
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_, "网络异常", "getOrderDataByStatus");
        }
    }

    @Override
    public boolean isOrderDelButtonShow(int orderId) throws LaiKeAPIException
    {
        boolean isShow = false;
        try
        {
            OrderModel orderOld = orderModelMapper.selectByPrimaryKey(orderId);
            if (orderOld == null)
            {
                return false;
            }
            if (ORDERS_R_STATUS_CLOSE == orderOld.getStatus())
            {
                isShow = true;
            }
            else if (orderOld.getSelf_lifting() == 0)
            {
                //自提订单不能删除 是否可以删除订单逻辑
                if (ORDERS_R_STATUS_COMPLETE == orderOld.getStatus() && DictionaryConst.WhetherMaven.WHETHER_OK == orderOld.getSettlement_status())
                {
                    isShow = true;
                }
            }
        }
        catch (Exception e)
        {
            logger.error("订单是否可以删除按钮显示逻辑 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_, "网络异常", "isOrderDelButtonShow");
        }
        return isShow;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean allDelOrder(int storeId, String sNo) throws LaiKeAPIException
    {
        try
        {
            int allDelOrder = orderModelMapper.getAllDelOrder(storeId, sNo);
            //用户、商家、平台是否都已经删除订单
            if (allDelOrder == 1)
            {
                //回收订单
                int i1 = orderModelMapper.delOrder(storeId, sNo);
                int i2 = orderDetailsModelMapper.delOrderDetails(storeId, sNo);
                if (i1 <= 0 && i2 <= 0)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YWYC, "业务异常", "delOrder");
                }
                logger.debug("用户、商家、平台都已经删除订单,回收订单号：{}", sNo);
            }
            else
            {
                return false;
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
        return true;
    }

    @Override
    public void selfSend(FrontDeliveryVo vo) throws LaiKeAPIException
    {
        try
        {
            int storeId = vo.getStoreId();
            if (StringUtils.isEmpty(vo.getsNo()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DDHWK, "订单号为空");
            }

            //获取订单信息
            OrderModel orderModel = new OrderModel();
            orderModel.setsNo(vo.getsNo());
            orderModel = orderModelMapper.selectOne(orderModel);
            if (orderModel != null)
            {
                OrderDetailsModel orderDetailsModel1 = new OrderDetailsModel();
                orderDetailsModel1.setR_sNo(vo.getsNo());
                List<OrderDetailsModel> orderListIds = orderDetailsModelMapper.select(orderDetailsModel1);
                OrderDetailsModel       orderDetailsOld;
                //统计当前订单下有多少件商品
                int orderGoodsNum = orderModel.getNum();
                //统计当前已发货数量
                int deliverNum = orderDetailsModelMapper.getDeliverNumBySNo(storeId, vo.getsNo());
                //统计这次发货数量
                int nowDeliverNum = 0;
                for (OrderDetailsModel orderDetail : orderListIds)
                {
                    int detailId = orderDetail.getId();
                    int num      = orderDetail.getNum();
                    if (num == 0)
                    {
                        logger.debug("发货失败,发货商品数量错误 详情id{}", detailId);
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QSRSPSL, "请输入商品数量");
                    }
                    //售后中的订单不能发货
                    if (returnOrderModelMapper.orderDetailReturnIsNotEnd(storeId, orderModel.getsNo(), detailId) > 0)
                    {
                        logger.debug("发货失败,订单正在售后中 详情id{}", detailId);
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_LBYSPZSHZ, "发货失败,订单正在售后中");
                    }
                    //订单非待发货状态
                    if (orderDetailsModelMapper.getDetailsNumById(storeId, detailId) > 0)
                    {
                        logger.debug("发货失败,订单状态非待发货 详情id{}", detailId);
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ORDER_DELIVERYED, "发货失败,订单状态非待发货");
                    }
                    orderDetailsOld = orderDetailsModelMapper.selectByPrimaryKey(detailId);
                    int DeliverNum = 0;
                    if (orderDetailsOld.getDeliverNum() != null)
                    {
                        DeliverNum = orderDetailsOld.getDeliverNum();
                    }
                    if (DeliverNum + num > orderDetailsOld.getNum())
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SJCW, "数据错误");
                    }
                    nowDeliverNum += num;
                    if (vo.getCourier_name() != null && vo.getPhone() != null && orderDetail.getStore_self_delivery() != null)
                    {
                        StoreSelfDeliveryModel storeSelfDeliveryModel = new StoreSelfDeliveryModel();
                        storeSelfDeliveryModel.setCourier_name(vo.getCourier_name());
                        storeSelfDeliveryModel.setPhone(vo.getPhone());
                        storeSelfDeliveryModel.setId(Integer.parseInt(orderDetail.getStore_self_delivery()));
                        storeSelfDeliveryModelMapper.updateByPrimaryKeySelective(storeSelfDeliveryModel);
                    }
                    //修改订单明细 修改明细订单状态为待收货
                    OrderDetailsModel orderDetailsModel = new OrderDetailsModel();
                    orderDetailsModel.setId(detailId);
                    if (DeliverNum + num == orderDetailsOld.getNum())
                    {
                        orderDetailsModel.setR_status(ORDERS_R_STATUS_DISPATCHED);
                    }
                    orderDetailsModel.setDeliverNum(DeliverNum + num);
                    orderDetailsModel.setDeliver_time(new Date());
                    int row = orderDetailsModelMapper.updateByPrimaryKeySelective(orderDetailsModel);
                    if (row < 1)
                    {
                        logger.debug("订单{} 发货失败,明细id{}", vo.getsNo(), detailId);
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_FHSB, "发货失败");
                    }
                }
                //是否都已经完成了发货,完成了发货则修改订单全部状态为待收货
                if (orderGoodsNum < deliverNum + nowDeliverNum)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SJCW, "数据错误");
                }
                //是否已经全部发货
                //修改订单主表状态 只有明细全部已发货才修改
                if (orderGoodsNum == deliverNum + nowDeliverNum)
                {
                    OrderModel updateOrder = new OrderModel();
                    updateOrder.setId(orderModel.getId());
                    updateOrder.setStatus(DictionaryConst.OrdersStatus.ORDERS_R_STATUS_DISPATCHED);
                    int count = orderModelMapper.updateByPrimaryKeySelective(updateOrder);
                    if (count < 1)
                    {
                        logger.info("订单发货状态修改失败 参数:" + JSON.toJSONString(updateOrder));
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "frontDelivery");
                    }
                    //订单来源小程序并且使用小程序支付的话 全部发货完调用微信发货同步信息
                    if (orderModel.getSource().toString().equals(DictionaryConst.StoreSource.LKT_LY_001)
                            && orderModel.getPay().equals(DictionaryConst.OrderPayType.ORDERPAYTYPE_MINI_WECHAT))
                    {
                        publicExpressService.setWxAppUploadShippingInfo(vo, orderModel.getsNo());
                    }
                    //如果全部发货，清除消息通知
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
                //站内推送发货信息
                publicAdminService.systemMessageSend(vo, SystemMessageModel.ReadType.UNREAD, "系统消息", "您购买的商品已经在赶去见您的路上啦!", orderModel.getUser_id());

                //TODO 【微信推送】暂时不做
                //$pusher->pushMessage($user_id, $db, $msg_title, $msg_content, $store_id, $user_id);
            }
            else
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DDBCZ, "订单不存在", "frontDelivery");
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("发货 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "frontDelivery");
        }
    }

    @Override
    public Map<String, Object> customerOrderIndex(OrderVo vo)
    {
        {
            List<Map<String, Object>> orderList = new ArrayList<>();
            Map<String, Object>       resMap    = new HashMap<>();
            Map<String, Object>       paramMap  = new HashMap<>();
            try
            {
                int  storeId = vo.getStoreId();
                User user    = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
                if (user == null)
                {
                    throw new LaiKeAPIException(ERROR_CODE_YHBCZ, "用户不存在");
                }
                List<Integer> statusList = new ArrayList<>();
                statusList.add(DictionaryConst.OrdersStatus.ORDERS_R_STATUS_COMPLETE);
                statusList.add(DictionaryConst.OrdersStatus.ORDERS_R_STATUS_UNPAID);
                statusList.add(DictionaryConst.OrdersStatus.ORDERS_R_STATUS_CLOSE);
                statusList.add(DictionaryConst.OrdersStatus.ORDERS_R_STATUS_TOBEVERIFIED);
                paramMap.put("storeId", storeId);
                paramMap.put("otype", DictionaryConst.OrdersType.ORDERS_HEADER_GM);
                paramMap.put("userId", user.getUser_id());
                paramMap.put("mch_id", vo.getMchId());
                paramMap.put("statusNotList", statusList);
                paramMap.put("start", vo.getPageNo());
                paramMap.put("pageSize", vo.getPageSize());
                int orderCount = orderModelMapper.getUserCenterOrderListCount(paramMap);
                if (orderCount > 0)
                {
                    orderList = orderModelMapper.getUserCenterOrderList(paramMap);
                    if (!CollectionUtils.isEmpty(orderList))
                    {
                        for (Map<String, Object> orderInfo : orderList)
                        {
                            // 订单号
                            String sNo = DataUtils.getStringVal(orderInfo, "sNo");
                            //订单状态
                            Integer status      = MapUtils.getInteger(orderInfo, "status");
                            Integer selfLifting = MapUtils.getInteger(orderInfo, "self_lifting");
                            //是否显示申请售后
                            boolean userCanAfter            = false;
                            int     unFinishShouHouOrderNum = returnOrderModelMapper.getOrderReturnIsNotEnd(storeId, sNo);
                            if (status == DictionaryConst.OrdersStatus.ORDERS_R_STATUS_DISPATCHED && unFinishShouHouOrderNum == 0 && selfLifting == 0)
                            {
                                userCanAfter = true;
                            }
                            orderInfo.put("userCanAfter", userCanAfter);
                            OrderDetailsModel orderDetailsModel = new OrderDetailsModel();
                            orderDetailsModel.setStore_id(storeId);
                            orderDetailsModel.setR_sNo(sNo);
                            List<OrderDetailsModel> orderDetailsModelList = orderDetailsModelMapper.select(orderDetailsModel);
                            for (OrderDetailsModel detailsModel : orderDetailsModelList)
                            {
                                Integer        sid            = Integer.valueOf(detailsModel.getSid());
                                ConfiGureModel confiGureModel = confiGureModelMapper.selectByPrimaryKey(sid);
                                if (Objects.nonNull(confiGureModel))
                                {
                                    if (StringUtils.isNotEmpty(confiGureModel.getImg()))
                                    {
                                        detailsModel.setImgUrl(publiceService.getImgPath(confiGureModel.getImg(), storeId));
                                    }
                                }
                            }
                            orderInfo.put("list", orderDetailsModelList);
                        }
                    }
                }
                resMap.put("list", orderList);
                resMap.put("total", orderCount);
            }
            catch (LaiKeAPIException l)
            {
                logger.error("客服页面获取订单列表异常", l);
            }
            catch (Exception e)
            {
                logger.error("客服页面获取订单列表异常", e);
                throw new LaiKeAPIException(ERROR_CODE_WLYC, "网络异常", "customerOrderIndex");
            }
            return resMap;
        }
    }

    @Override
    public String getsNoByOrderId(Integer orderId)
    {
        return orderModelMapper.getsNoByOrderId(orderId);
    }

    @Override
    public Integer getStatusByOrderNo(String sNo)
    {
        return orderModelMapper.getStatusByOrderNo(sNo);
    }

    @Override
    public void thirdPayOrderReturn(List<ReturnOrderModel> returnOrderModelList) throws LaiKeAPIException
    {
        try
        {
            for (ReturnOrderModel returnOrderModel : returnOrderModelList)
            {

                String sNo = returnOrderModel.getsNo();

                logger.info("订单号：{}正在执行退款业务回滚",sNo);

                Integer storeId = returnOrderModel.getStore_id();

                Map<String,Object> map = orderDetailsModelMapper.getDetailInfoById(returnOrderModel.getsNo(),returnOrderModel.getP_id(),storeId);
                //商户订单号
                String realSno = MapUtils.getString(map, "real_sno");
                //支付方式
                String pay = MapUtils.getString(map, "pay");
                //订单详情状态
                Integer rStatus = MapUtils.getInteger(map, "r_status");
                if (MapUtils.isEmpty(map) || pay.equals(DictionaryConst.OrderPayType.ORDERPAYTYPE_WALLET_PAY) || rStatus == ORDERS_R_STATUS_CLOSE)continue;

                //是否退款成功
                boolean flag = false;

                //支付配置
                String configStr = paymentConfigModelMapper.getPaymentConfigInfo(storeId, pay);
                if (StringUtils.isEmpty(configStr))continue;
                JSONObject jsonObject = JSONObject.parseObject(configStr);
                switch (pay) {
                    //支付宝查询退款
                    case DictionaryConst.OrderPayType.ORDERPAYTYPE_ALIPAY:
                    case DictionaryConst.OrderPayType.ORDERPAYTYPE_ALIPAY_TMP:
                    case DictionaryConst.OrderPayType.ORDERPAYTYPE_PC_ALIPAY:
                    case DictionaryConst.OrderPayType.ORDERPAYTYPE_ALIPAY_MINIPAY:
                    case DictionaryConst.OrderPayType.ORDERPAYTYPE_ALIPAY_WAP:
                        //支付宝公共参数配置
                        AlipayConfig alipayConfig = AlipayConfigInfo.getAlipayConfig(configStr);
                        //初始化sdk
                        AlipayClient alipayClient = new DefaultAlipayClient(alipayConfig);
                        //构造请求参数调用接口
                        AlipayTradeFastpayRefundQueryRequest request = new AlipayTradeFastpayRefundQueryRequest();
                        AlipayTradeFastpayRefundQueryModel model = new AlipayTradeFastpayRefundQueryModel();
                        model.setOutTradeNo(realSno);
                        //退款请求号（退款时生成的，查询退款状态时，商户单号和退款单号必须同时带，不然查不到退款状态）
                        if (StringUtils.isNotEmpty(returnOrderModel.getAli_out_request_no()))
                        {
                            model.setOutRequestNo(returnOrderModel.getAli_out_request_no());
                        }
                        request.setBizModel(model);
                        try
                        {
                            AlipayTradeFastpayRefundQueryResponse response = alipayClient.execute(request);
                            logger.info("支付宝查询退款返回结果：：：：{}",JSON.toJSONString(response));
                            //当接口返回的refund_status值为REFUND_SUCCESS时表示退款成功，否则表示退款没有执行成功。
                            if (response.getRefundStatus().equals("REFUND_SUCCESS"))
                            {
                                flag = true;
                            }
                        }catch (Exception e)
                        {
                            logger.error("支付宝查询退款失败：：：：{}",e.getMessage());
                        }

                        break;
                        //微信查询退款
                    case DictionaryConst.OrderPayType.ORDERPAYTYPE_APP_WECHAT:
                    case DictionaryConst.OrderPayType.ORDERPAYTYPE_MINI_WECHAT:
                    case DictionaryConst.OrderPayType.ORDERPAYTYPE_PC_WECHAT:
                    case DictionaryConst.OrderPayType.ORDERPAYTYPE_H5_WECHAT:
                    case DictionaryConst.OrderPayType.ORDERPAYTYPE_JSAPI_WECHAT:
                        logger.info("进入微信查询退款");
                        Map<String,String> paramMap = new HashMap<>();
                        String appid = jsonObject.getString("appid");
                        String mch_id = jsonObject.getString("mch_id");
                        //随机生成32位字符串
                        String nonce_str = WXPayUtil.generateNonceStr();
                        //api密钥
                        String key = jsonObject.getString("mch_key");
                        paramMap.put("appid",appid);
                        paramMap.put("mch_id",mch_id);
                        paramMap.put("nonce_str",nonce_str);
                        paramMap.put("out_trade_no",realSno);
                        //生成签名
                        String sign = WXPayUtil.generateSignature(paramMap, key, WXPayConstants.SignType.MD5);
                        paramMap.put("sign",sign);

                        WechatConfigInfo config = new WechatConfigInfo(appid,mch_id,key);
                        WXPay wxPay = new WXPay(config);
                        /**
                         * return_code返回SUCCESS只能代表接口请求成功，退款结果得根据退款状态判断
                         * refund_status_$n,下标是动态的，使用starts-with函数匹配所有以"refund_status_"开头的元素
                         */
                        try
                        {
                            //发起调用（不需要证书），返回格式为xml
                            String responseXml = wxPay.requestWithoutCert(WXPayConstants.QUERY_RETURN_URL_SUFFIX, paramMap, 6 * 1000, 8 * 1000);
                            DocumentBuilder builder = WXPayXmlUtil.newDocumentBuilder();
                            Document doc = builder.parse(new InputSource(new StringReader(responseXml)));
                            // 2. 使用XPath表达式
                            XPath xpath = XPathFactory.newInstance().newXPath();
                            XPathExpression expr = xpath.compile("//*[starts-with(local-name(), 'refund_status_')]");
                            // 3. 执行查询并处理结果
                            NodeList nodeList = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
                            if (nodeList != null && nodeList.getLength() > 0)
                            {
                                for(int i = 0; i < nodeList.getLength(); i++) {
                                    Node node = nodeList.item(i);
                                    String fieldName = node.getNodeName();
                                    String value = node.getTextContent();
                                    logger.info("fieldName:::{}>>>> value:::::{}",fieldName,value);
                                    if (Objects.equals(value,"SUCCESS"))
                                    {
                                        flag = true;
                                        break;
                                    }
                                }
                            }
                        }
                        catch (Exception e)
                        {
                            logger.error("微信返回结果解析异常{}",e.getMessage());
                        }
                        break;
                        //贝宝查询退款
                    case DictionaryConst.OrderPayType.PAYPAL_PAY:
                        String clientId = jsonObject.getString("client_id");
                        String clientSecret = jsonObject.getString("client_secret");
                        //配置环境（沙盒/生产）
                        PayPalEnvironment payPalEnvironment = new PayPalEnvironment.Sandbox(
                                clientId,
                                clientSecret
                        );
                        PayPalHttpClient client = new PayPalHttpClient(payPalEnvironment);
                        try
                        {
                            String refundId = returnOrderModel.getPay_pal_return_id();
                            RefundsGetRequest refundsGetRequest = new RefundsGetRequest(refundId);
                            HttpResponse<Refund> refundHttpResponse = client.execute(refundsGetRequest);
                            Refund result = refundHttpResponse.result();
                            logger.info("贝宝订单退款查询：：：：{}",JSON.toJSONString(result));
                            String status = result.status();
                            if (status.equals("COMPLETED"))
                            {
                                flag = true;
                            }
                        }catch (Exception e)
                        {
                            logger.error("贝宝订单查询退款失败：：：{}",e.getMessage());
                        }
                        break;
                    default:
                        break;
                }

                //查询退款失败，跳过继续下一个
                if (!flag)continue;

                logger.info("是否继续执行退款：{}",flag);
                RefundVo vo = new RefundVo();
                //订单详情id
                vo.setId(returnOrderModel.getId());
                vo.setStoreId(storeId);
                vo.setPrice(returnOrderModel.getReal_money());
                vo.setType(DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_AGREE_REBACK_AMT);
                vo.setIsTask(true);
                vo.setSNo(returnOrderModel.getsNo());
                vo.setText("订单退款");
                publicRefundService.refund(vo);
            }
        }
        catch (LaiKeAPIException l)
        {
            logger.error("第三方支付订单退款异常:{}", l.getMessage());
            throw l;
        }
        catch (Exception e)
        {
            throw new LaiKeAPIException(ERROR_CODE_WLYC, "网络异常",e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getPayConfig(String type, int storeId) throws LaiKeAPIException
    {
        Map<String, Object> result = new HashMap<>();
        try
        {
            String  paymentJson = paymentConfigModelMapper.getPaymentConfigInfo(storeId, type);
            paymentJson = URLDecoder.decode(paymentJson, GloabConst.Chartset.UTF_8);
            result.put("data", paymentJson);
        }
        catch (LaiKeAPIException l)
        {
            logger.error("获取支付信息失败:{}", l.getMessage());
            throw l;
        }
        catch (Exception e)
        {
            throw new LaiKeAPIException(ERROR_CODE_WLYC, "网络异常",e.getMessage());
        }
        return result;
    }



    @Override
    @Transactional(rollbackFor = Exception.class)
    public void kuaidi100CouldNotify(HttpServletRequest request) throws LaiKeAPIException
    {
        try
        {
            String body = getRequestBody(request);
            logger.info("快递100电子面单回调信息：{}",body);

            //解码
            String decodeUrlParam = UrlParamUtils.decodeUrlParam(body);
            //获取请求参数
            Map<String,String> paramMap = UrlParamUtils.parseUrlParams(decodeUrlParam);

            JSONObject param = JSONObject.parseObject(paramMap.get("param"));
            logger.info("回调请求param:{}",param);

            //任务id
            String taskId = paramMap.get("taskId");
            logger.info("回调请求taskId:{}",taskId);


            String code = param.getString("status");

            boolean is_success = Objects.equals("200",code);
            logger.info("is_success{}",is_success);


            ExpressDeliveryModel expressDeliveryModel = new ExpressDeliveryModel();
            expressDeliveryModel.setTask_id(taskId);
            expressDeliveryModel = expressDeliveryModelMapper.selectOne(expressDeliveryModel);
            if (Objects.isNull(expressDeliveryModel))
            {
                logger.error("查无此任务快递信息：{}",taskId);
                return;
            }
            //是否打印 0.未打印 1.已打印 2.打印失败
            int status = is_success ? 1 : 2;
            expressDeliveryModelMapper.updateStatusById(status,expressDeliveryModel.getId());

            //打印失败，调接口发起复打
            /*if (!is_success)
            {
                overridePrint(configModel,taskId);
            }*/

        }
        catch (LaiKeAPIException l)
        {
            logger.error("快递100电子面单回调失败", l);
        }
        catch (Exception e)
        {
            logger.error("快递100电子面单回调失败", e);
            throw new LaiKeAPIException(ERROR_CODE_WLYC, "网络异常", "kuaidi100CouldNotify");
        }

    }


    @Override
    public void overridePrint(ConfigModel configModel,ExpressDeliveryModel model) throws LaiKeAPIException
    {
        String key = configModel.getExpress_key();
        String t = System.currentTimeMillis() + "";

        Map<String,Object> map = new HashMap<>();
        map.put("taskId",model.getTask_id());
        map.put("siid",configModel.getSiid());
        String param = new Gson().toJson(map);

        String sign = MD5Util.MD5Is32(param + t + key + configModel.getExpress_secret(), null).toUpperCase();

        PrintReq printReq = new PrintReq();
        printReq.setT(t);
        printReq.setKey(key);
        printReq.setMethod(ApiInfoConstant.CLOUD_PRINT_OLD_METHOD);
        printReq.setSign(sign);
        printReq.setParam(param);

        try
        {
            IBaseClient baseClient = new LabelV2();
            HttpResult  execute    = baseClient.execute(printReq);
            logger.error("电子面单复打返回值：{}", execute.toString());
            Map<String, Object> bodyMap = com.alibaba.fastjson.JSON.parseObject(execute.getBody(), new com.alibaba.fastjson.TypeReference<Map<String, Object>>()
            {
            });
            String message = MapUtils.getString(bodyMap, "message");
            if (!MapUtils.getString(bodyMap, "code").equals("200"))
            {
                logger.error("电子面单复打异常：{}",message);
                throw new LaiKeAPIException(ErrorCode.SysErrorCode.ALL_CODE,message, "overridePrint");
            }
            expressDeliveryModelMapper.updateStatusById(1,model.getId());
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("电子面单复打异常： ", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "overridePrint");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void uploadCredentials(OrderVo vo) throws LaiKeAPIException
    {
        try
        {
            RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            OrderModel orderModel = new OrderModel();
            orderModel.setsNo(vo.getsNo());
            orderModel = orderModelMapper.selectOne(orderModel);
            if (Objects.isNull(orderModel))
            {
                throw new LaiKeAPIException(ERROR_CODE_DDBCZ,"订单不存在");
            }
            orderModel.setVoucher(vo.getVoucher());
            orderModel.setReview_status(1);
            orderModelMapper.updateByPrimaryKeySelective(orderModel);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("上传凭证异常： ", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "uploadCredentials");
        }
    }

    @Override
    public Boolean trackDelivery(Integer storeId, String courierNum)
    {
        try
        {
            ConfigModel configModel = new ConfigModel();
            configModel.setStore_id(storeId);
            configModel = configModelMapper.selectOne(configModel);
            if (Objects.isNull(configModel.getTrack_secret()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TRACKMYWPZ, "密钥为空");
            }
            HttpPost httpPost = new HttpPost("https://api.17track.net/track/v2.4/register");
            httpPost.addHeader("17token", configModel.getTrack_secret());

            List<Map<String, Object>> list = new ArrayList<>();
            Map<String, Object> map = new HashMap<>();
            map.put("number", courierNum);
            list.add(map);

            StringEntity entity = new StringEntity(JSONObject.toJSONString(list), StandardCharsets.UTF_8);
            entity.setContentType("application/json; charset=utf-8");
            httpPost.setEntity(entity);

            String body = HttpUtils.post(httpPost);
            logger.info("body:{}", body);
            JSONObject jsonObject = JSONObject.parseObject(body);

            Integer code = jsonObject.getInteger("code");
            if (code != 0)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TRACKFHSB, "发货失败");
            }
            JSONObject data = jsonObject.getJSONObject("data");

            //拒绝接受请求的单号信息集合
            JSONArray rejected = data.getJSONArray("rejected");
            if (Objects.nonNull(rejected))
            {
                for (Object o : rejected)
                {
                    JSONObject jsonRejected = (JSONObject) o;
                    JSONObject error = jsonRejected.getJSONObject("error");
                    String message = error.getString("message");
                    Integer errorCode = error.getInteger("code");
                    //返回错误码和错误信息，方便检查
                    String msg = "error:" + errorCode + "," + "message:" + message;
                    throw new LaiKeAPIException(ErrorCode.SysErrorCode.ALL_CODE, msg);
                }
            }
        }
        catch (Exception e)
        {
            logger.error("17track发货调用失败： ", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "trackDelivery");
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void trackNotify(HttpServletRequest request) throws LaiKeAPIException
    {
        try
        {
            String body = getRequestBody(request);

            String sign = request.getHeader("sign");
            logger.info("17 track sign :{}",sign);

            //解码
            body = UrlParamUtils.decodeUrlParam(body);
            logger.info("17 track body:{}",body);
            JSONObject jsonObject = JSONObject.parseObject(body);
            String event = jsonObject.getString("event");
            //推送事件：TRACKING_UPDATED：更新 TRACKING_STOPPED：停止
            if (event.equals("TRACKING_UPDATED"))
            {
                JSONObject data = jsonObject.getJSONObject("data");
                //物流单号
                String number = data.getString("number");

                ExpressDeliveryModel expressDeliveryModel = new ExpressDeliveryModel();
                expressDeliveryModel.setCourierNum(number);
                expressDeliveryModel = expressDeliveryModelMapper.selectOne(expressDeliveryModel);
                if (Objects.isNull(expressDeliveryModel))
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLBCZ,"物流不存在");
                }
                JSONObject trackInfo = data.getJSONObject("track_info");
                JSONObject tracking = trackInfo.getJSONObject("tracking");
                JSONArray providers = tracking.getJSONArray("providers");

                //拿第一条
                JSONObject delivery = providers.getJSONObject(0);
                JSONArray events = delivery.getJSONArray("events");
                String deliveryInfo = events.toJSONString();


                List<OrderDetailsModel> orderDetailsModels = orderDetailsModelMapper.likeCourierNum(number);
                if (!CollectionUtils.isEmpty(orderDetailsModels))
                {
                    for (OrderDetailsModel orderDetailsModel : orderDetailsModels)
                    {
                        //如果有多件商品信息物流，用逗号隔开
                        deliveryInfo = concatStepByStep(orderDetailsModel.getLogistics(), deliveryInfo);
                        OrderDetailsModel update = new OrderDetailsModel();
                        update.setId(orderDetailsModel.getId());
                        update.setLogistics(deliveryInfo);
                        orderDetailsModelMapper.updateByPrimaryKeySelective(update);
                    }
                }
                //查看是否是售后物流
                else
                {
                    ReturnGoodsModel returnGoodsModel = new ReturnGoodsModel();
                    returnGoodsModel.setExpress_num(number);
                    returnGoodsModel = returnGoodsModelMapper.selectOne(returnGoodsModel);
                    if (Objects.nonNull(returnGoodsModel))
                    {
                        returnGoodsModel.setLogistics(deliveryInfo);
                        returnGoodsModelMapper.updateByPrimaryKeySelective(returnGoodsModel);
                    }
                }
            }
        }
        catch (LaiKeAPIException l)
        {
            logger.error("17track回调失败", l);
        }
        catch (Exception e)
        {
            logger.error("17track回调失败", e);
            throw new LaiKeAPIException(ERROR_CODE_WLYC, "网络异常", "trackNotify");
        }
    }

    /**
     * 是否显示查看退款按钮
     *
     * @param pid
     * @return
     */
    private ReturnOrderModel canShowReturn(Integer pid, String orderNo)
    {
        ReturnOrderModel returnOrderModel = new ReturnOrderModel();
        returnOrderModel.setPid(pid);
        returnOrderModel.setsNo(orderNo);
        return returnOrderModelMapper.selectOne(returnOrderModel);
    }

    /**
     * 字符串拼接，逗号分割
     * @param currentStr
     * @param appendStr
     * @return
     */
    private String concatStepByStep(String currentStr, String appendStr)
    {
        String safeAppendStr = appendStr == null ? "" : appendStr;
        if (currentStr == null || currentStr.isEmpty())
        {
            return safeAppendStr;
        }
        return currentStr + "," + safeAppendStr;
    }

    /**
     * 订单详情商品按钮
     *
     * @param orderDetailStatus         订单详情
     * @param orderCommentType          评论按钮
     * @param userCanAfter              能否售后 true 能 false 不能
     * @param settlementStatus          结算状态
     * @param orderDetailReturnIsNotEnd 是否有售后
     * @return
     */
    private Map<String, Object> orderDetailShowValue(String oType, Integer orderDetailStatus, Integer orderCommentType, Boolean userCanAfter, Integer settlementStatus, Integer orderDetailReturnIsNotEnd, Integer self_lifting, Integer orderReturnSuccessNum,Integer status)
    {
        Map<String, Object> detailMap = new HashMap<>();
        switch (orderDetailStatus)
        {
            /**
             * 物流订单：申请售后
             * 商家配送订单：申请售后
             */
            case ORDERS_R_STATUS_DISPATCHED:
                if (userCanAfter && self_lifting != 1 && (oType.equals(DictionaryConst.OrdersType.ORDERS_HEADER_GM) || oType.equals(DictionaryConst.OrdersType.ORDERS_HEADER_FS) || oType.equals(DictionaryConst.OrdersType.ORDERS_HEADER_FX)))
                {
                    if (orderDetailReturnIsNotEnd == null || orderDetailReturnIsNotEnd == 0)
                    {
                        if (!Objects.equals(ORDERS_R_STATUS_CONSIGNMENT,status))
                        {
                            addActions(detailMap, OrderShowValueEnum.SQSH);
                        }
                    }
                    else
                    {
                        addActions(detailMap, OrderShowValueEnum.CKSH);
                    }
                }
                break;
            //已完成
            case ORDERS_R_STATUS_COMPLETE:
                /**
                 * 物流订单：：申请售后/查看售后、立即评价/追加评价
                 * 自提订单：立即评价/追加评价
                 * 商家配送订单：立即评价/追加评价
                 */
                if (orderCommentType == 2)
                {
                    addActions(detailMap, OrderShowValueEnum.ZJPJ);
                }
                if (settlementStatus != 1)
                {
                    if (oType.equals(DictionaryConst.OrdersType.ORDERS_HEADER_IN) || oType.equals(DictionaryConst.OrdersType.ORDERS_HEADER_JP))break;
                    if ((orderDetailReturnIsNotEnd != null && orderDetailReturnIsNotEnd > 0 || orderReturnSuccessNum > 0))
                    {
                        addActions(detailMap, OrderShowValueEnum.CKSH);
                    }
                    if (userCanAfter)
                    {
                        if (self_lifting == 3 || self_lifting == 1)break;
                        if (orderDetailReturnIsNotEnd == null || orderDetailReturnIsNotEnd == 0)
                        {
                            addActions(detailMap, OrderShowValueEnum.SQSH);
                        }
                    }
                }
                break;
            case ORDERS_R_STATUS_CLOSE:
                if ((orderReturnSuccessNum != null && orderReturnSuccessNum > 0) || orderDetailReturnIsNotEnd > 0)
                {
                    addActions(detailMap, OrderShowValueEnum.CKSH);
                }
        }
        return detailMap;
    }

    private Map<String, Object> orderShowValue(Integer unFinishShouHouOrderNum, Integer status, Boolean haveExpress, Integer orderCommentType,
                                               Integer self_lifting, String oType, Integer sellType, Boolean invoiceTimeout, Boolean refund,
                                               Boolean refundShowBtn, Boolean isInvoice, Boolean canPay, Integer count, Integer writeOffSettings,
                                               Integer invoicePrice,String pay_type,Integer review_status)
    {
        Map<String, Object> resMap   = new HashMap<>();
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("unFinishShouHouOrderNum", unFinishShouHouOrderNum);
        paramMap.put("status", status);
        paramMap.put("haveExpress", haveExpress);
        paramMap.put("orderCommentType", orderCommentType);
        paramMap.put("self_lifting", self_lifting);
        paramMap.put("sellType", sellType);
        paramMap.put("refundShowBtn", refundShowBtn);
        paramMap.put("refund", refund);
        paramMap.put("invoiceTimeout", invoiceTimeout);
        paramMap.put("isInvoice", isInvoice);
        paramMap.put("canPay", canPay);
        paramMap.put("count", count);
        paramMap.put("writeOffSettings", writeOffSettings);
        paramMap.put("payType",pay_type);
        paramMap.put("review_status",review_status);
        if (invoicePrice != null)
        {
            paramMap.put("invoicePrice", invoicePrice);
        }
        //订单类型策略
        orderProcessor.processOrder(oType, resMap, paramMap);
        return resMap;
    }


    /**
     * 计算结算金额
     * @param map
     * @param status
     * @param storeId
     * @param orderNo
     * @return
     */
    private BigDecimal calcSettlementPrice(Map<String,Object> map,Integer status,Integer storeId,String orderNo)
    {
        BigDecimal settlementPrice =new BigDecimal(MapUtils.getString(map, "z_price"));
        BigDecimal oldTotal = new BigDecimal(MapUtils.getString(map, "old_total"));

        //供应商端结算列表字段处理
        String supplier_settlement = MapUtils.getString(map, "supplier_settlement");
        if (StringUtils.isNotEmpty(supplier_settlement))
        {
            map.put("settlement",new BigDecimal(supplier_settlement));
            settlementPrice = new BigDecimal(supplier_settlement);
            //售后通过 ->退货退款通过不计算供货价
            if (status == ORDERS_R_STATUS_CLOSE)
            {
                if (returnOrderModelMapper.getOrderGoodsRefundSuccessful(storeId, orderNo) > 0)
                {
                    settlementPrice = BigDecimal.ZERO;
                }
            }
            BigDecimal z_price         = BigDecimal.valueOf(MapUtils.getDouble(map, "z_price"));
            SupplierOrderFrightModel supplierOrderFrightModel = new SupplierOrderFrightModel();
            supplierOrderFrightModel.setsNo(orderNo);
            List<SupplierOrderFrightModel> select = supplierOrderFrightModelMapper.select(supplierOrderFrightModel);
            if (!Objects.isNull(select) && !select.isEmpty())
            {
                BigDecimal fright = BigDecimal.ZERO;
                for (SupplierOrderFrightModel model : select)
                {
                    //707 运费显示总的，不用z_fright
                    fright = fright.add(model.getFreight());
                }
                //极速退款不显示运费
                if (settlementPrice.compareTo(BigDecimal.ZERO) == 0 && status == ORDERS_R_STATUS_CLOSE)
                {
                    settlementPrice = BigDecimal.ZERO;
                }
                else
                {
                    settlementPrice = settlementPrice.add(fright);
                }
                map.put("supplier_settlement", settlementPrice);
            }

            BigDecimal mchSettlementPrice = z_price.subtract(settlementPrice).setScale(2, RoundingMode.HALF_DOWN);
            if (z_price.compareTo(oldTotal) < 0)
            {
                mchSettlementPrice = BigDecimal.ZERO;
            }
            map.put("mchSettlementPrice", mchSettlementPrice);
            if (DataUtils.getStringVal(map, "otype").equals(DictionaryConst.OrdersType.ORDERS_HEADER_VI) && Strings.isNullOrEmpty(DataUtils.getStringVal(map, "pay_time")) && status == ORDERS_R_STATUS_CLOSE && DataUtils.getIntegerVal(map, "settlement_status").equals(OrderDetailsModel.SETTLEMENT_TYPE_SETTLED))
            {
                settlementPrice = BigDecimal.ZERO;
            }
            if (DataUtils.getIntegerVal(map, "settlement_status").equals(OrderDetailsModel.SETTLEMENT_TYPE_UNSETTLED))
            {
                settlementPrice = DataUtils.getBigDecimalVal(map, "z_price");
            }
        }
        return settlementPrice;
    }

    //判断快递公司是否支持取消面单
    private boolean isCancellation(String type)
    {
        boolean flag = true;

        Map<String,Object> param = new HashMap<>();
        param.put("name","电子面单取消支持快递公司");
        param.put("status",1);
        param.put("lang_code","zh_CN");
        List<Map<String, Object>> dictionary = dictionaryListModelMapper.getDictionaryByName(param);
        if (org.apache.commons.collections.CollectionUtils.isNotEmpty(dictionary))
        {
            List<String> valueList = dictionary.stream()
                    .map(map -> (String) map.get("value"))
                    .collect(Collectors.toList());

            flag = valueList.contains(type);
        }
        return flag;
    }




    private String getRequestBody(HttpServletRequest request) throws Exception
    {
        try
        {
            BufferedReader br = new BufferedReader(new InputStreamReader(request.getInputStream(), "UTF-8"));
            String         line;
            StringBuilder  sb = new StringBuilder();
            while ((line = br.readLine()) != null)
            {
                sb.append(line);
            }
            return sb.toString();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw e;
        }
    }

    @Autowired
    private DictionaryListModelMapper dictionaryListModelMapper;

    @Autowired
    private OrderProcessor                orderProcessor;
    @Autowired
    private DistributionRecordModelMapper distributionRecordModelMapper;

    @Autowired
    private ReturnOrderModelMapper returnOrderModelMapper;

    @Autowired
    private PublicPaymentConfigService publicPaymentConfigService;

    @Autowired
    private ExpressModelMapper expressModelMapper;

    @Autowired
    private MessageLoggingModalMapper messageLoggingModalMapper;

    @Autowired
    private FreightModelMapper freightModelMapper;

    @Autowired
    private AdminCgModelMapper adminCgModelMapper;

    @Autowired
    private OrderDataModelMapper orderDataModelMapper;

    @Autowired
    private OrderConfigModalMapper orderConfigModalMapper;

    @Autowired
    private OrderModelMapper orderModelMapper;

    @Autowired
    private OrderDetailsModelMapper orderDetailsModelMapper;

    @Autowired
    private PubliceService publiceService;

    @Autowired
    private PublicMemberService publicMemberService;

    @Autowired
    private MchStoreModelMapper mchStoreModelMapper;

    @Autowired
    private MchModelMapper mchModelMapper;

    @Autowired
    private UserBaseMapper userBaseMapper;

    @Autowired
    private ServiceAddressModelMapper serviceAddressModelMapper;

    @Autowired
    private AuctionProductModelMapper auctionProductModelMapper;

    @Autowired
    private CommentsModelMapper commentsModelMapper;

    @Autowired
    private ConfiGureModelMapper confiGureModelMapper;

    @Autowired
    private ProductListModelMapper productListModelMapper;

    @Autowired
    private ReturnGoodsModelMapper returnGoodsModelMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RecordModelMapper recordModelMapper;

    @Autowired
    private PaymentConfigModelMapper paymentConfigModelMapper;

    @Autowired
    private PaymentModelMapper paymentModelMapper;

    @Autowired
    private PublicMchService publicMchService;

    @Autowired
    private CouponActivityModelMapper couponActivityModelMapper;

    @Autowired
    private PublicAdminRecordService publicAdminRecordService;

    @Autowired
    private PaymentService groupPayService;

    @Autowired
    private PaymentService ptGroupPayService;

    @Autowired
    private PaymentService distributionPayService;

    @Autowired
    private DistributionConfigModelMapper distributionConfigModelMapper;

    @Autowired
    private DistributionGoodsModelMapper distributionGoodsModelMapper;

    @Autowired
    private PubliceDistributionService publiceDistributionService;


    @Autowired
    private DictionaryNameModelMapper dictionaryNameModelMapper;

    @Autowired
    private PublicRefundService publicRefundService;

    @Autowired
    private SystemMessageModelMapper systemMessageModelMapper;

    @Autowired
    private ConfigModelMapper configModelMapper;

    @Autowired
    private IntegralGoodsModelMapper integralGoodsModelMapper;

    @Autowired
    private IntegralConfigModelMapper integralConfigModelMapper;

    @Autowired
    private SecondsConfigModelMapper secondsConfigModelMapper;

    @Autowired
    private PublicIntegralService publicIntegralService;

    @Autowired
    @Qualifier("publicWechatServiceImpl")
    private PublicPaymentService publicPaymentService;

    @Autowired
    private PreSellConfigModelMapper preSellConfigModelMapper;

    @Autowired
    private PreSellRecordModelMapper preSellRecordModelMapper;

    @Autowired
    private PreSellGoodsMapper preSellGoodsMapper;

    @Autowired
    private PublicOrderService publicOrderService;

    @Autowired
    private PublicAdminService publicAdminService;

    @Autowired
    private SignRecordModelMapper signRecordModelMapper;

    @Autowired
    private PubliceMemberService publiceMemberService;

    @Autowired
    private PublicAuctionService publicAuctionService;

    @Autowired
    private CouponModalMapper couponModalMapper;

    @Autowired
    private PublicCouponService publicCouponService;

    @Autowired
    private NoticeModelMapper noticeModelMapper;

    @Autowired
    private RecordDetailsModelMapper recordDetailsModelMapper;

    @Autowired
    private FlashsaleConfigModelMapper flashsaleConfigModelMapper;

    @Autowired
    private LivingProductModelMapper livingProductModelMapper;

    @Autowired
    private GroupOpenRecordModelMapper groupOpenRecordModelMapper;

    @Autowired
    private AuctionPromiseModelMapper auctionPromiseModelMapper;
}
