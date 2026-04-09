package com.laiketui.common.aop;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.laiketui.core.cache.RedisUtil;
import com.laiketui.core.common.SplitUtils;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.DictionaryConst;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.utils.tool.StringUtils;
import com.laiketui.domain.datax.RocketMqConst;
import com.laiketui.domain.mch.CartModel;
import com.laiketui.domain.vo.OrderVo;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * 主要用于控制商品超卖
 */
@Aspect
@Component
@Order(2)
public class PreventOverSellAop
{

    public static final String REQUEST_KEY = "Request-ID:";

    public static final String LOCK_PRE = "PreventOverSell-LOCK:";

    @Autowired
    private RedissonClient redissonClient;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Pointcut("@annotation(com.laiketui.core.annotation.PreventOverSell)")
    public void preventOverSell()
    {
    }

    @Autowired
    private RedisUtil redisUtil;

    @Around("preventOverSell()")
    public Object aroundAdvice(ProceedingJoinPoint joinPoint) throws Throwable
    {
        RLock         lock          = null;
        String        apiName       = null;
        String        apiMethodName = null;
        StringBuilder parameterName = null;
        try
        {
            Signature       signature       = joinPoint.getSignature();
            MethodSignature methodSignature = (MethodSignature) signature;
            Method          method          = methodSignature.getMethod();
            apiMethodName = method.getName();
            //不同功能的方法不要同名
            lock = redissonClient.getLock(LOCK_PRE + apiMethodName);
            if (lock.tryLock())
            {
                // 获取方法参数
                Object[] args = joinPoint.getArgs();
                if ("payment".equals(apiMethodName))
                {
                    OrderVo vo = (OrderVo) args[0];
                    //商城ID
                    int storeId = vo.getStoreId();
                    //端类型
                    int storetype = vo.getStoreType();
                    //非购物车商品信息 [{"pid":"979"},{"cid":"5648"},{"num":1}]
                    String productInfos = vo.getProductsInfo();
                    //购物车id 1,2
                    String carts = vo.getCarts();
                    logger.info("下单参数：{}", JSON.toJSONString(vo));
                    String orderType = vo.getType();
                    if (StringUtils.isEmpty(orderType))
                    {
                        orderType = DictionaryConst.OrdersType.ORDERS_HEADER_GM;
                    }
                    String payType = vo.getPayType();
                    //如果是余额下单检查余额是否足够
                    if (DictionaryConst.OrderPayType.ORDERPAYTYPE_WALLET_PAY.equalsIgnoreCase(payType))
                    {
                        String        userKey               = RocketMqConst.SellOverRedisKeys.USER_MONEY_KEY;
                        RAtomicDouble userMoneyAtomicDouble = null;
                        userKey = userKey + storeId + SplitUtils.UNDERLIEN + vo.getAccessId();
                        userMoneyAtomicDouble = redissonClient.getAtomicDouble(userKey);
                        if (userMoneyAtomicDouble.get() <= 0)
                        {
                            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YEBZ, "余额不足");
                        }
                    }

                    logger.info("下单参数：{}", JSON.toJSONString(vo));
                    String productStockKey  = RocketMqConst.SellOverRedisKeys.PRODUCT_STOCK_KEY;
                    String productStatusKey = RocketMqConst.SellOverRedisKeys.PRODUCT_STATUS_KEY;

                    RAtomicLong proStock  = null;
                    RAtomicLong proStatus = null;
                    if (DictionaryConst.OrdersType.ORDERS_HEADER_MS.equals(orderType))
                    {
                        logger.info("秒杀放行");
                        //是否已购买过
                        //库存是否足够
                        return joinPoint.proceed();
                    }
                    else if (DictionaryConst.OrdersType.ORDERS_HEADER_GM.equals(orderType))
                    {
                        //购物车
                        if (StringUtils.isNotEmpty(carts))
                        {
                            String[] cartids = carts.split(SplitUtils.DH);
                            if (cartids != null && cartids.length > 0)
                            {
                                for (String cartid : cartids)
                                {
                                    String cartsKey = RocketMqConst.SellOverRedisKeys.SHOP_CART_KEY;
                                    cartsKey = cartsKey + storeId + SplitUtils.UNDERLIEN + cartid;
                                    RBucket<CartModel> bucket    = redissonClient.getBucket(cartsKey);
                                    CartModel          cartModel = bucket.get();
                                    if (cartModel != null)
                                    {
                                        int pid = cartModel.getGoods_id();
                                        int num = cartModel.getGoods_num();

                                        String tail = storeId + SplitUtils.UNDERLIEN + pid;
                                        productStatusKey = productStatusKey + tail;
                                        //已下架
                                        proStatus = redissonClient.getAtomicLong(productStatusKey);
                                        if (proStatus.get() == DictionaryConst.GoodsStatus.OFFLINE_GROUNDING)
                                        {
                                            logger.info("商品id：{}已下架", pid);
                                            throw new LaiKeAPIException(ErrorCode.BizErrorCode.OFF_SHELF, "商品已下架");
                                        }

                                        productStockKey = productStockKey + tail;
                                        //库存是否满足本次购买
                                        proStock = redissonClient.getAtomicLong(productStockKey);
                                        if (proStock.get() < num)
                                        {
                                            logger.info("商品id：{} 库存不足", pid);
                                            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_KCBZ, "库存不足");
                                        }
                                    }
                                }
                            }
                        }
                        else if (StringUtils.isNotEmpty(productInfos))
                        {
                            //非购物车
                            JSONArray productInfoArray = JSON.parseArray(productInfos);

                            JSONObject productInfoPid = productInfoArray.getJSONObject(0);
                            int        pid            = productInfoPid.getIntValue("pid");
                            JSONObject productInfoNum = productInfoArray.getJSONObject(2);
                            int        num            = productInfoNum.getIntValue("num");

                            String tail = storeId + SplitUtils.UNDERLIEN + pid;
                            productStatusKey = productStatusKey + tail;
                            //已下架
                            proStatus = redissonClient.getAtomicLong(productStatusKey);
                            if (proStatus.get() == DictionaryConst.GoodsStatus.OFFLINE_GROUNDING)
                            {
                                logger.info("商品id：{}已下架", pid);
                                throw new LaiKeAPIException(ErrorCode.BizErrorCode.OFF_SHELF, "商品已下架");
                            }
                            productStockKey = productStockKey + tail;
                            //库存是否满足本次购买
                            proStock = redissonClient.getAtomicLong(productStockKey);
                            if (proStock.get() < num)
                            {
                                logger.info("商品id：{} 库存不足", pid);
                                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_KCBZ, "库存不足");
                            }
                        }
                    }
                }
                // 继续执行原始方法
                return joinPoint.proceed();
            }
            else
            {
                //客户端提示的是 请稍后再试
                logger.error("未获取到业务锁，请下次再试.");
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QSHZS, "请稍后再试");
            }
        }
        finally
        {
            if (lock != null && lock.isHeldByCurrentThread())
            {
                lock.unlock();
            }
        }
    }
}