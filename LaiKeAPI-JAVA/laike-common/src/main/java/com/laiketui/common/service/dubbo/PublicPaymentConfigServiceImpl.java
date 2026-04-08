package com.laiketui.common.service.dubbo;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import com.google.common.collect.Maps;
import com.laiketui.common.api.PublicPaymentConfigService;
import com.laiketui.common.mapper.*;
import com.laiketui.common.utils.PayMappingUtils;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.DictionaryConst;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.utils.tool.DataUtils;
import com.laiketui.core.utils.tool.SerializePhpUtils;
import com.laiketui.core.utils.tool.StringUtils;
import com.laiketui.domain.order.OrderDataModel;
import com.laiketui.domain.order.OrderModel;
import com.laiketui.domain.payment.PaymentModel;
import com.laiketui.domain.presell.PreSellRecordModel;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 支付配置信息
 *
 * @author wangxian
 */
@Service
public class PublicPaymentConfigServiceImpl implements PublicPaymentConfigService
{

    private final Logger logger = LoggerFactory.getLogger(PublicPaymentConfigServiceImpl.class);

    @Autowired
    private PaymentConfigModelMapper paymentConfigModelMapper;

    @Autowired
    private PaymentModelMapper paymentModelMapper;

    @Autowired
    private OrderModelMapper orderModelMapper;

    @Autowired
    private OrderDataModelMapper orderDataModelMapper;


    @Override
    public String getPaymentConfig(int storeId, String payType)
    {
        try
        {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("payType", payType);
            String configStr = paymentConfigModelMapper.getPaymentConfigInfo(storeId, payType);
            configStr = configStr.replaceAll("%2B", "\\+");
            jsonObject.put("payConfig", configStr);
            return jsonObject.toJSONString();
        }
        catch (Exception e)
        {
            logger.error("获取商城的支付信息失败:", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_, "", "");
        }
    }

    @Autowired
    private PreSellRecordModelMapper preSellRecordModelMapper;

    @Override
    public String getPaymentConfigByPayNo(String payNo)
    {
        try
        {
            JSONObject jsonObject = new JSONObject();
            logger.info("根据吊起支付订单号:{}", payNo);
            //根据调起支付所用订单号获取主订单号(未支付先拆单，子订单调起支付所用调起支付所用订单号和总订单相同)
            String sNo = orderModelMapper.getOrderByRealSno(payNo);
            logger.info("根据吊起支付订单号获取主订单号:{}", sNo);
            //根据调起支付所用订单号获取订单号，先拆单后支付
            if (StringUtils.isEmpty(sNo))
            {
                sNo = orderModelMapper.getOrdersNoByRealSno(payNo);
                logger.info("先拆单后支付:{}", sNo);
            }
            OrderModel orderModel = new OrderModel();
            //sNo == null  插件订单
            if (StringUtils.isNotEmpty(sNo))
            {
                orderModel.setsNo(sNo);
                orderModel = orderModelMapper.selectOne(orderModel);
            }
            else
            {
                orderModel.setReal_sno(payNo);
                orderModel = orderModelMapper.selectOne(orderModel);
            }
            if (orderModel == null)
            {
                logger.error("预售订单特殊处理");
                //预售订单特殊处理
                if (payNo.contains(DictionaryConst.OrdersType.ORDERS_HEADER_PS))
                {
                    PreSellRecordModel preSellRecordModel = new PreSellRecordModel();
                    preSellRecordModel.setReal_sno(payNo);
                    preSellRecordModel = preSellRecordModelMapper.selectOne(preSellRecordModel);
                    logger.error("preSellRecordModel为：{}", preSellRecordModel.toString());
                    if (preSellRecordModel != null)
                    {
                        orderModel = new OrderModel();
                        orderModel.setsNo(preSellRecordModel.getReal_sno());
                        orderModel.setReal_sno(preSellRecordModel.getReal_sno());
                        orderModel.setStore_id(preSellRecordModel.getStore_id());
                        orderModel.setPay(preSellRecordModel.getPay());
                        orderModel.setZ_price(preSellRecordModel.getPrice());
                    }
                    else
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_HQZFPZXXSB, "获取支付配置信息失败", "getPaymentConfigByPayNo");
                    }
                    //拼团订单特殊处理
                }
                else if (payNo.contains(DictionaryConst.OrdersType.ORDERS_HEADER_PT))
                {
                    OrderDataModel orderDataModel = new OrderDataModel();
                    orderDataModel.setTrade_no(payNo);
                    orderDataModel = orderDataModelMapper.selectOne(orderDataModel);
                    if (orderDataModel != null)
                    {
                        orderModel = new OrderModel();
                        Map<String, Object> goodsInfo = JSON.parseObject(orderDataModel.getData(), new TypeReference<Map<String, Object>>()
                        {
                        });
                        orderModel.setsNo(orderDataModel.getTrade_no());
                        orderModel.setReal_sno(orderDataModel.getTrade_no());
                        orderModel.setZ_price(new BigDecimal(MapUtils.getString(goodsInfo, "z_price")));
                        orderModel.setStore_id(MapUtils.getInteger(goodsInfo, "store_id"));
                        orderModel.setPay(goodsInfo.get("pay").toString());
                        jsonObject.put("userId", MapUtils.getString(goodsInfo, "userId"));
                        jsonObject.put("map", MapUtils.getString(goodsInfo, "map"));
                        jsonObject.put("storeType", MapUtils.getString(goodsInfo, "storeType"));
                        jsonObject.put("real_sno", orderDataModel.getTrade_no());
                    }
                    else
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_HQZFPZXXSB, "获取支付配置信息失败", "getPaymentConfigByPayNo");
                    }
                }
                else if (payNo.contains(DictionaryConst.OrdersType.ORDERS_HEADER_JB))
                {
                    OrderDataModel orderDataModel = new OrderDataModel();
                    orderDataModel.setTrade_no(payNo);
                    orderDataModel = orderDataModelMapper.selectOne(orderDataModel);
                    if (orderDataModel != null)
                    {
                        orderModel = new OrderModel();
                        Map<String, Object> goodsInfo = JSON.parseObject(orderDataModel.getData(), new TypeReference<Map<String, Object>>()
                        {
                        });
                        orderModel.setsNo(orderDataModel.getTrade_no());
                        orderModel.setReal_sno(orderDataModel.getTrade_no());
                        orderModel.setZ_price(new BigDecimal(MapUtils.getString(goodsInfo, "paymentAmt")));
                        orderModel.setStore_id(MapUtils.getInteger(goodsInfo, "storeId"));
                        orderModel.setPay(goodsInfo.get("pay").toString());
                    }
                    else
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_HQZFPZXXSB, "获取支付配置信息失败", "getPaymentConfigByPayNo");
                    }
                }
                else
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DDBCZ, "获取支付配置信息失败", "getPaymentConfigByPayNo");
                }
            }
            else
            {

                if (payNo.contains(DictionaryConst.OrdersType.ORDERS_HEADER_PT))
                {
                    OrderDataModel orderDataModel = new OrderDataModel();
                    orderDataModel.setTrade_no(payNo);
                    orderDataModel = orderDataModelMapper.selectOne(orderDataModel);
                    if (orderDataModel != null)
                    {
                        orderModel = new OrderModel();
                        Map<String, Object> goodsInfo = JSON.parseObject(orderDataModel.getData(), new TypeReference<Map<String, Object>>()
                        {
                        });
                        orderModel.setsNo(orderDataModel.getTrade_no());
                        orderModel.setReal_sno(orderDataModel.getTrade_no());
                        orderModel.setZ_price(new BigDecimal(MapUtils.getString(goodsInfo, "z_price")));
                        orderModel.setStore_id(MapUtils.getInteger(goodsInfo, "store_id"));
                        orderModel.setPay(goodsInfo.get("pay").toString());
                        jsonObject.put("userId", MapUtils.getString(goodsInfo, "userId"));
                        jsonObject.put("map", MapUtils.getString(goodsInfo, "map"));
                        jsonObject.put("storeType", MapUtils.getString(goodsInfo, "storeType"));
                        jsonObject.put("real_sno", orderDataModel.getTrade_no());
                    }
                    else
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_HQZFPZXXSB, "获取支付配置信息失败", "getPaymentConfigByPayNo");
                    }
                }

            }
            int    storeId = orderModel.getStore_id();
            String payType = orderModel.getPay();
            jsonObject.put("payType", payType);
            jsonObject.put("storeId", storeId);
            String configStr = paymentConfigModelMapper.getPaymentConfigInfo(storeId, payType);
//            configStr = URLEncoder.encode(configStr, GloabConst.Chartset.UTF_8);
            jsonObject.put("payConfig", configStr);

            logger.error("getPaymentConfigByPayNo->jsonObject:" + jsonObject.toJSONString());
            return jsonObject.toJSONString();
        }
        catch (Exception e)
        {
            logger.error("获取商城的支付信息失败:", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_HQZFPZXXSB, "获取支付配置信息失败", "getPaymentConfigByPayNo");
        }
    }


    @Override
    public String getPaymentConfigBySNo(String sNo)
    {
        try
        {
            JSONObject jsonObject = new JSONObject();
//            logger.info("根据吊起支付订单号:{}", payNo);
            logger.info("根据吊起主订单号:{}", sNo);
            //根据调起支付所用订单号获取主订单号(未支付先拆单，子订单调起支付所用调起支付所用订单号和总订单相同)
            String payNo = orderModelMapper.getOrderBySno(sNo);
            logger.info("根据吊起主订单号获取支付订单号:{}", payNo);
            //根据调起支付所用订单号获取订单号，先拆单后支付
            if (StringUtils.isEmpty(sNo))
            {
                sNo = orderModelMapper.getOrdersNoByRealSno(payNo);
                logger.info("先拆单后支付:{}", sNo);
            }
            OrderModel orderModel = new OrderModel();
            //sNo == null  插件订单
            if (StringUtils.isNotEmpty(sNo))
            {
                orderModel.setsNo(sNo);
                orderModel = orderModelMapper.selectOne(orderModel);
            }
            else
            {
                orderModel.setReal_sno(payNo);
                orderModel = orderModelMapper.selectOne(orderModel);
            }
            if (orderModel == null)
            {
                //预售订单特殊处理
                if (payNo.contains(DictionaryConst.OrdersType.ORDERS_HEADER_PS))
                {
                    PreSellRecordModel preSellRecordModel = new PreSellRecordModel();
                    preSellRecordModel.setReal_sno(payNo);
                    preSellRecordModel = preSellRecordModelMapper.selectOne(preSellRecordModel);
                    if (preSellRecordModel != null)
                    {
                        orderModel = new OrderModel();
                        orderModel.setsNo(preSellRecordModel.getReal_sno());
                        orderModel.setReal_sno(preSellRecordModel.getReal_sno());
                        orderModel.setStore_id(preSellRecordModel.getStore_id());
                        orderModel.setPay(preSellRecordModel.getPay());
                        orderModel.setZ_price(preSellRecordModel.getPrice());
                    }
                    else
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_HQZFPZXXSB, "获取支付配置信息失败", "getPaymentConfigByPayNo");
                    }
                    //拼团订单特殊处理
                }
                else if (payNo.contains(DictionaryConst.OrdersType.ORDERS_HEADER_PT))
                {
                    OrderDataModel orderDataModel = new OrderDataModel();
                    orderDataModel.setTrade_no(payNo);
                    orderDataModel = orderDataModelMapper.selectOne(orderDataModel);
                    if (orderDataModel != null)
                    {
                        orderModel = new OrderModel();
                        Map<String, Object> goodsInfo = JSON.parseObject(orderDataModel.getData(), new TypeReference<Map<String, Object>>()
                        {
                        });
                        orderModel.setsNo(orderDataModel.getTrade_no());
                        orderModel.setReal_sno(orderDataModel.getTrade_no());
                        orderModel.setZ_price(new BigDecimal(MapUtils.getString(goodsInfo, "z_price")));
                        orderModel.setStore_id(MapUtils.getInteger(goodsInfo, "store_id"));
                        orderModel.setPay(goodsInfo.get("pay").toString());
                        jsonObject.put("userId", MapUtils.getString(goodsInfo, "userId"));
                        jsonObject.put("map", MapUtils.getString(goodsInfo, "map"));
                        jsonObject.put("storeType", MapUtils.getString(goodsInfo, "storeType"));
                        jsonObject.put("real_sno", orderDataModel.getTrade_no());
                    }
                    else
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_HQZFPZXXSB, "获取支付配置信息失败", "getPaymentConfigByPayNo");
                    }
                }
                else if (payNo.contains(DictionaryConst.OrdersType.ORDERS_HEADER_JB))
                {
                    OrderDataModel orderDataModel = new OrderDataModel();
                    orderDataModel.setTrade_no(payNo);
                    orderDataModel = orderDataModelMapper.selectOne(orderDataModel);
                    if (orderDataModel != null)
                    {
                        orderModel = new OrderModel();
                        Map<String, Object> goodsInfo = JSON.parseObject(orderDataModel.getData(), new TypeReference<Map<String, Object>>()
                        {
                        });
                        orderModel.setsNo(orderDataModel.getTrade_no());
                        orderModel.setReal_sno(orderDataModel.getTrade_no());
                        orderModel.setZ_price(new BigDecimal(MapUtils.getString(goodsInfo, "paymentAmt")));
                        orderModel.setStore_id(MapUtils.getInteger(goodsInfo, "storeId"));
                        orderModel.setPay(goodsInfo.get("pay").toString());
                    }
                    else
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_HQZFPZXXSB, "获取支付配置信息失败", "getPaymentConfigByPayNo");
                    }
                }
                else
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_HQZFPZXXSB, "获取支付配置信息失败", "getPaymentConfigByPayNo");
                }
            }
            else
            {

                if (payNo.contains(DictionaryConst.OrdersType.ORDERS_HEADER_PT))
                {
                    OrderDataModel orderDataModel = new OrderDataModel();
                    orderDataModel.setTrade_no(payNo);
                    orderDataModel = orderDataModelMapper.selectOne(orderDataModel);
                    if (orderDataModel != null)
                    {
                        orderModel = new OrderModel();
                        Map<String, Object> goodsInfo = JSON.parseObject(orderDataModel.getData(), new TypeReference<Map<String, Object>>()
                        {
                        });
                        orderModel.setsNo(orderDataModel.getTrade_no());
                        orderModel.setReal_sno(orderDataModel.getTrade_no());
                        orderModel.setZ_price(new BigDecimal(MapUtils.getString(goodsInfo, "z_price")));
                        orderModel.setStore_id(MapUtils.getInteger(goodsInfo, "store_id"));
                        orderModel.setPay(goodsInfo.get("pay").toString());
                        jsonObject.put("userId", MapUtils.getString(goodsInfo, "userId"));
                        jsonObject.put("map", MapUtils.getString(goodsInfo, "map"));
                        jsonObject.put("storeType", MapUtils.getString(goodsInfo, "storeType"));
                        jsonObject.put("real_sno", orderDataModel.getTrade_no());
                    }
                    else
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_HQZFPZXXSB, "获取支付配置信息失败", "getPaymentConfigByPayNo");
                    }
                }

            }
            int    storeId = orderModel.getStore_id();
            String payType = orderModel.getPay();
            jsonObject.put("payType", payType);
            jsonObject.put("storeId", storeId);
            String configStr = paymentConfigModelMapper.getPaymentConfigInfo(storeId, payType);
//            configStr = URLEncoder.encode(configStr, GloabConst.Chartset.UTF_8);
            jsonObject.put("payConfig", configStr);

            logger.error("getPaymentConfigByPayNo->jsonObject:" + jsonObject.toJSONString());
            return jsonObject.toJSONString();
        }
        catch (Exception e)
        {
            logger.error("获取商城的支付信息失败:", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_HQZFPZXXSB, "获取支付配置信息失败", "getPaymentConfigByPayNo");
        }
    }

    @Override
    public String getPaymentConfigByMemberPayNo(String payNo)
    {
        try
        {
            OrderDataModel orderModel = new OrderDataModel();
            orderModel.setTrade_no(payNo);
            orderModel = orderDataModelMapper.selectOne(orderModel);
            if (orderModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_HQZFPZXXSB, "获取支付配置信息失败", "getPaymentConfigByPayNo");
            }
            String data = orderModel.getData();
            logger.info("data:{}", data);
            Map map = null;
            if (payNo.contains(DictionaryConst.OrdersType.ORDERS_HEADER_MCH_PROMISE) || payNo.contains(DictionaryConst.OrdersType.ORDERS_HEADER_DJ))
            {
                map = JSON.parseObject(data, Map.class);
            }
            else
            {
                map = SerializePhpUtils.getUnserializeObj(data, Map.class);
            }
            logger.info("map:{}", map);
            Integer storeId = MapUtils.getInteger(map, "storeId");
            if (storeId == null)
            {
                storeId = MapUtils.getInteger(map, "store_id");
            }
            String payType = MapUtils.getString(map, "pay");
            if (payType == null)
            {
                payType = MapUtils.getString(map, "payType");
            }
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("payType", payType);
            jsonObject.put("storeId", storeId);
            String paymentConfigInfo = paymentConfigModelMapper.getPaymentConfigInfo(storeId, payType);
            logger.info("paymentConfigInfo:{}", paymentConfigInfo);
            jsonObject.put("payConfig", paymentConfigInfo);
            return jsonObject.toJSONString();
        }
        catch (Exception e)
        {
            logger.error("获取商城的支付信息失败:", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_HQZFPZXXSB, "获取支付配置信息失败", "getPaymentConfigByPayNo");
        }
    }


    @Override
    public List<PaymentModel> getPayment()
    {
        try
        {
            return paymentModelMapper.selectAll();
        }
        catch (Exception e)
        {
            logger.error("获取平台支付信息失败", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_HQPTZFXXSB, "获取平台支付信息失败", "getPayment");
        }
    }

    @Override
    public Map<String, String> getPaymentMap()
    {
        Map<String, String> retMap = Maps.newConcurrentMap();
        try
        {
            List<PaymentModel> paymentModels = paymentModelMapper.selectAll();
            for (PaymentModel paymentModel : paymentModels)
            {
                retMap.put(paymentModel.getClass_name(), paymentModel.getName());
            }
            return retMap;
        }
        catch (Exception e)
        {
            logger.error("获取平台支付信息失败", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_HQPTZFXXSB, "获取平台支付信息失败", "getPayment");
        }
    }

    @Override
    public Map<String, Object> getPaymentInfos(int storeId)
    {
        Map<String, Object> retMap = Maps.newConcurrentMap();
        try
        {
            Map<String, Object> map = new HashMap<>();
            map.put("store_id", storeId);
            //支付相关配置的信息
            List<Map<String, Object>> paymentStatus  = paymentConfigModelMapper.getPaymentConfigDynamic(map);
            Map<String, Integer>      payment        = new HashMap<>(16);
            Map<String, String>       defaultpayment = new HashMap<>(16);
            for (Map<String, Object> paymentConf : paymentStatus)
            {
                String payName = DataUtils.getStringVal(paymentConf, "class_name");
                if (StringUtils.isEmpty(payName))
                {
                    continue;
                }

                int    isOpen       = DataUtils.getIntegerVal(paymentConf, "statusSwitch", 0);
                String payClassName = DataUtils.getStringVal(paymentConf, "class_name");

                payment.put(payClassName, isOpen);

                int isdefaultpay = DataUtils.getIntegerVal(paymentConf, "isdefaultpay", 2);
                if (isdefaultpay == 1)
                {
                    defaultpayment.put("defaultpayName", PayMappingUtils.getPayName(payClassName));

                }
            }

            retMap.put("defaultpayment", defaultpayment);
            retMap.put("payment", payment);

            return retMap;
        }
        catch (Exception e)
        {
            logger.error("获取平台支付信息失败", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_HQPTZFXXSB, "获取平台支付信息失败", "getPayment");
        }
    }

    @Override
    public List<String> getV3config(String wechatPaySerial)
    {
      return paymentConfigModelMapper.getV3config(wechatPaySerial);
    }
}
