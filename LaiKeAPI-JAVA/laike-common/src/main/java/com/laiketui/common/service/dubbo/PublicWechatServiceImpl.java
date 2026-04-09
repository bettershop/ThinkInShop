package com.laiketui.common.service.dubbo;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import com.google.common.collect.Maps;
import com.laiketui.common.api.PubliceService;
import com.laiketui.common.api.order.PublicIntegralService;
import com.laiketui.common.api.pay.PublicPaymentService;
import com.laiketui.common.mapper.*;
import com.laiketui.common.utils.tool.cache.RedisDataTool;
import com.laiketui.common.utils.weixin.AppletUtil;
import com.laiketui.common.utils.weixin.WXPaySignatureCertificateUtil;
import com.laiketui.common.utils.weixin.WXPayV3Constants;
import com.laiketui.common.utils.weixin.WxV3PayConfig;
import com.laiketui.core.cache.RedisUtil;
import com.laiketui.core.common.SplitUtils;
import com.laiketui.core.config.wechatpay.WechatConfigInfo;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.DictionaryConst;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.core.utils.tool.DataUtils;
import com.laiketui.core.utils.tool.DateUtil;
import com.laiketui.core.utils.tool.StringUtils;
import com.laiketui.domain.divideAccount.MchDistributionModel;
import com.laiketui.domain.divideAccount.MchDistributionRecordModel;
import com.laiketui.domain.log.RecordDetailsModel;
import com.laiketui.domain.log.RecordModel;
import com.laiketui.domain.log.SignRecordModel;
import com.laiketui.domain.mch.MchModel;
import com.laiketui.domain.mch.MchPromiseModel;
import com.laiketui.domain.mch.PromiseShModel;
import com.laiketui.domain.message.TemplateData;
import com.laiketui.domain.order.NoticeModel;
import com.laiketui.domain.order.OrderDataModel;
import com.laiketui.domain.order.OrderDetailsModel;
import com.laiketui.domain.order.OrderModel;
import com.laiketui.domain.presell.PreSellRecordModel;
import com.laiketui.domain.user.User;
import com.laiketui.domain.vo.pay.PaymentVo;
import com.laiketui.domain.vo.plugin.integral.AddScoreVo;
import github.wxpay.sdk.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static com.laiketui.core.lktconst.ErrorCode.BizErrorCode.API_OPERATION_FAILED;

@Service("publicWechatServiceImpl")
public class PublicWechatServiceImpl implements PublicPaymentService
{

    private final Logger logger = LoggerFactory.getLogger(PublicWechatServiceImpl.class);

    @Autowired
    private PaymentConfigModelMapper paymentConfigModelMapper;

    @Autowired
    private OrderModelMapper orderModelMapper;

    @Autowired
    private OrderDataModelMapper orderDataModelMapper;

    @Autowired
    private NoticeModelMapper noticeModelMapper;

    @Autowired
    private PubliceService publiceService;

    @Autowired
    private MchPromiseModelMapper mchPromiseModelMapper;

    @Autowired
    private MchModelMapper mchModelMapper;

    @Autowired
    private PromiseShModelMapper promiseShModelMapper;

    @Autowired
    private OrderDetailsModelMapper orderDetailsModelMapper;

    @Autowired
    private MchDistributionModelMapper mchDistributionModelMapper;

    @Autowired
    private ConfiGureModelMapper confiGureModelMapper;

    @Autowired
    private MchDistributionRecordModelMapper mchDistributionRecordModelMapper;

    /**
     * 获取微信支付配置
     *
     * @param paymentVo
     * @return
     * @throws Exception
     */
    private WechatConfigInfo getWechatConfigInfo(PaymentVo paymentVo) throws Exception
    {
        //微信app支付 默认app支付
        String className   = paymentVo.getPayType();
        int    storeId     = paymentVo.getStoreId();
        String paymentJson = paymentConfigModelMapper.getPaymentConfigInfo(storeId, className);
        paymentJson = URLDecoder.decode(paymentJson, GloabConst.Chartset.UTF_8);
        //paymentJson = paymentJson.replaceAll("%2B", "\\+");
        logger.info(className + "支付配置信息：" + paymentJson);
        JSONObject payJson = JSONObject.parseObject(paymentJson);
        String     appID   = payJson.getString("appid");
        logger.info("appID:{}", appID);
        String mchID = payJson.getString("mch_id");
        logger.info("mchID:{}", mchID);
        String key = payJson.getString("mch_key");
        logger.info("key:{}", key);
        String certPath = payJson.getString("sslcert_path");
        logger.info("certPath:{}", certPath);
        String notifyUrl = payJson.getString("notify_url");
        logger.info("notifyUrl:{}", notifyUrl);
        String appSecreKey = payJson.getString("appsecret");
        logger.info("appSecreKey:{}", appSecreKey);
        String serial_no =  payJson.getString("serial_no");
        logger.info("serial_no:{}", serial_no);
        String APIv3_key =  payJson.getString("APIv3_key");
        String key_pem =  payJson.getString("key_pem");
        logger.info("key_pem:{}", key_pem);
        String cert_p12 = payJson.getString("cert_p12");
        logger.info("cert_p12:{}", cert_p12);
        return new WechatConfigInfo(appID, mchID, key, cert_p12 == null ? "/var/pay/apiclient_cert.p12" : cert_p12, notifyUrl, appSecreKey,StringUtils.isEmpty(serial_no) ? "" : serial_no,StringUtils.isEmpty(APIv3_key) ? "" : APIv3_key,key_pem);
    }


    @Override
    public Map<String, String> refundOrder(int storeId, String sNo, Integer id, String className, String tradeNo, BigDecimal refundAmt, boolean isTempOrder, BigDecimal orderPrice,Integer refundId) throws LaiKeAPIException
    {
        return null;
    }

    //y标记
    @Override
    public Map<String, Object> refund(Map params) throws LaiKeAPIException
    {
        return null;
    }

    @Override
    public String qrCodeOrder(String appid, String privateKey, String publicKey, String orderno, BigDecimal orderAmt) throws LaiKeAPIException
    {
        return null;
    }

    @Override
    public Map<String, String> refundOrder(int storeId, Integer id, String className, String tradeNo, BigDecimal refundAmt, boolean isTempOrder, BigDecimal orderPrice) throws LaiKeAPIException
    {
        Map<String, String> resultMap = new HashMap<>(16);
        try
        {
            String proName = "";
            String thing6  = "卖家已同意退款将原路退回到您的账户";
            logger.info(">>微信退款开始>>");
            //获取商户支付配置信息
            PaymentVo paymentVo = new PaymentVo();
            paymentVo.setPayType(className);
            paymentVo.setStoreId(storeId);
            WechatConfigInfo    config = getWechatConfigInfo(paymentVo);
            String apIv3Key = config.getAPIv3_key();
            WXPay               wxpay  = new WXPay(config);
            Map<String, String> params = new HashMap<>(16);
            params.put("appid", config.getAppID());
            params.put("mch_id", config.getMchID());
            //商户订单号
            params.put("out_trade_no", tradeNo);
            //商户退款单号
            String out_refund_no = tradeNo.concat(String.valueOf(System.currentTimeMillis()));
            params.put("out_refund_no", out_refund_no);
            //订单信息
            OrderModel orderModel = new OrderModel();
            if (!isTempOrder)
            {
                orderModel.setId(id);
                orderModel = orderModelMapper.selectOne(orderModel);
                OrderDetailsModel orderDetailsModel = new OrderDetailsModel();
                if (orderModel == null)
                {
                    orderModel = new OrderModel();
                    orderModel.setReal_sno(tradeNo);
                    orderModel.setOtype(tradeNo.substring(0, 2));
                    logger.info("支付订单号：" + tradeNo);
                    orderModel = orderModelMapper.selectOne(orderModel);
                }
                orderDetailsModel.setStore_id(storeId);
                orderDetailsModel.setR_sNo(orderModel.getsNo());
                List<OrderDetailsModel> select = orderDetailsModelMapper.select(orderDetailsModel);
                if (select.size() > 0)
                {
                    orderDetailsModel = select.get(0);
                    proName = orderDetailsModel.getP_name();
                    if (select.size() > 1)
                    {
                        proName = proName + "等商品";
                    }
                }
            }
            else
            {
                OrderDataModel orderDataOld = orderDataModelMapper.selectByPrimaryKey(id);
                if (orderDataOld == null)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SJCW, "数据错误");
                }
                Map<String, Object> orderInfoData = JSON.parseObject(orderDataOld.getData(), new TypeReference<Map<String, Object>>()
                {
                });
                orderModel.setUser_id(MapUtils.getString(orderInfoData, "user_id"));
            }
            BigDecimal yb           = new BigDecimal("100");

            int total_fee = orderPrice.multiply(yb).intValue();
            int refund_fee = refundAmt.multiply(yb).intValue();

            String     orderAmount  = String.valueOf(orderPrice.multiply(yb).intValue());
            String     refundAmount = String.valueOf(refundAmt.multiply(yb).intValue());
            params.put("total_fee", orderAmount);
            //退款金额
            params.put("refund_fee", refundAmount);

            if (StringUtils.isEmpty(apIv3Key))
            {
                logger.info("v2版本退款");
                Map<String, String> resultMap1 = wxpay.refund(params);
                logger.info("退款信息，{}", JSONObject.toJSONString(resultMap1));

                //微信请求退款失败
                if (!"SUCCESS".equals(MapUtils.getString(resultMap1, "result_code")) || !"SUCCESS".equals(MapUtils.getString(resultMap1, "return_code")))
                {
                    logger.info("退款错误信息，{}", MapUtils.getString(resultMap1, "err_code_des"));
                    if (StringUtils.isNotEmpty(MapUtils.getString(resultMap1,"err_code_des")) && !Objects.equals(MapUtils.getString(resultMap1,"err_code_des"),"订单已全额退款"))
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CZSB, MapUtils.getString(resultMap1, "err_code_des"), "refundOrder");
                    }
                }
            }
            else
            {
                logger.info("v3版本退款");
                WXPaySignatureCertificateUtil.wxV3Refund(out_refund_no,tradeNo,total_fee,refund_fee,config);
            }
            resultMap.put("code", GloabConst.ManaValue.MANA_VALUE_SUCCESS);
            //发送模板消息
            NoticeModel noticeModel = new NoticeModel();
            noticeModel.setStore_id(paymentVo.getStoreId());
            noticeModel = noticeModelMapper.selectOne(noticeModel);
            if (Objects.isNull(noticeModel))
            {
                logger.debug("该商城id{}暂无微信推送模板", paymentVo.getStoreId());
            }
            //当前用户信息
            User userEntity = new User();
            userEntity.setUser_id(orderModel.getUser_id());
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
                    Map<String, Object> map = new HashMap<>(16);
                    map.put("amount2", new TemplateData(String.valueOf(refundAmt)));
                    map.put("date3", new TemplateData(DateUtil.dateFormate(new Date(), GloabConst.TimePattern.YMDHMS)));
                    map.put("character_string4", new TemplateData(orderModel.getsNo()));
                    map.put("thing5", new TemplateData(proName));
                    map.put("thing6", new TemplateData(thing6));
                    String response = AppletUtil.sendMessage(accessToken, openId, noticeModel.getRefund_res(), map);
                    logger.error("=================微信消息推送返回值：{}", response);
                }
            }
            return resultMap;
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("微信退款 异常:", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", e.getMessage());
        }
    }

    @Override
    public Map<String, String> refundOrder(int storeId, PromiseShModel promiseShModel, String className, String tradeNo, BigDecimal refundAmt) throws LaiKeAPIException
    {
        Map<String, String> resultMap = new HashMap<>(16);
        try
        {
            String proName = "";
            String thing6  = "卖家已同意退款将原路退回到您的账户";
            logger.info(">>微信退款开始>>");
            //获取商户支付配置信息
            PaymentVo paymentVo = new PaymentVo();
            paymentVo.setPayType(className);
            paymentVo.setStoreId(storeId);
            WechatConfigInfo    config = getWechatConfigInfo(paymentVo);

            //判断是否启用微信支付v3版本
            String apIv3Key = config.getAPIv3_key();

            Map<String, String> params = new HashMap<>(16);
            params.put("appid", config.getAppID());
            params.put("mch_id", config.getMchID());
            //商户订单号
            params.put("out_trade_no", tradeNo);
            String out_refund_no = tradeNo.concat(String.valueOf(System.currentTimeMillis()));
            //商户退款单号
            params.put("out_refund_no", out_refund_no);
            //修改表
            promiseShModelMapper.updateByPrimaryKeySelective(promiseShModel);
            PromiseShModel promiseShTKModel = promiseShModelMapper.selectByPrimaryKey(promiseShModel.getId());
            //根据审核表获取保证金表
            MchPromiseModel mchPromiseOld = new MchPromiseModel();
            mchPromiseOld.setStatus(MchPromiseModel.PromiseConstant.STATUS_PAY);
            mchPromiseOld.setMch_id(promiseShTKModel.getMch_id());
            mchPromiseOld = mchPromiseModelMapper.selectOne(mchPromiseOld);
            //修改保证金表
            MchPromiseModel mchPromiseUpdate = new MchPromiseModel();
            mchPromiseUpdate.setStatus(MchPromiseModel.PromiseConstant.STATUS_RETURN_PAY);
            mchPromiseUpdate.setIs_return_pay(1);
            mchPromiseUpdate.setId(mchPromiseOld.getId());
            mchPromiseUpdate.setUpdate_date(new Date());
            int row = mchPromiseModelMapper.updateByPrimaryKeySelective(mchPromiseUpdate);
            if (row < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TKSB, "退款失败");
            }
            BigDecimal yb           = new BigDecimal("100");
            int total_fee = refundAmt.multiply(yb).intValue();
            String     refundAmount = String.valueOf(refundAmt.multiply(yb).intValue());
            params.put("total_fee", refundAmount);
            //退款金额
            params.put("refund_fee", refundAmount);
            logger.info("微信退款申请参数{}", params);

            if (StringUtils.isEmpty(apIv3Key))
            {
                WXPay wxpay  = new WXPay(config);
                Map<String, String> resultMap1 = wxpay.refund(params);
                logger.info("#########退款信息#########start####");
                logger.info("退款信息，{}", JSONObject.toJSONString(resultMap1));
                logger.info("#########退款信息#########end######");
                //微信请求退款失败
                if (!"SUCCESS".equals(MapUtils.getString(resultMap1, "result_code")) || !"SUCCESS".equals(MapUtils.getString(resultMap1, "return_code")))
                {
                    logger.info("退款错误信息，{}", MapUtils.getString(resultMap1, "err_code_des"));
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CZSB, MapUtils.getString(resultMap1, "err_code_des"), "refundOrder");
                }
            }
            else
            {
                WXPaySignatureCertificateUtil.wxV3Refund(out_refund_no,tradeNo,total_fee,total_fee,config);
            }
            resultMap.put("code", GloabConst.ManaValue.MANA_VALUE_SUCCESS);
            //发送模板消息
            NoticeModel noticeModel = new NoticeModel();
            noticeModel.setStore_id(paymentVo.getStoreId());
            noticeModel = noticeModelMapper.selectOne(noticeModel);
            if (Objects.isNull(noticeModel))
            {
                logger.debug("该商城id{}暂无微信推送模板", paymentVo.getStoreId());
            }
            //当前用户信息
            User userEntity = new User();
            userEntity.setUser_id(mchModelMapper.selectByPrimaryKey(promiseShTKModel.getMch_id()).getUser_id());
            User user = userBaseMapper.selectOne(userEntity);
            proName = "保证金";
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
                    Map<String, Object> map = new HashMap<>(16);
                    map.put("amount2", new TemplateData(String.valueOf(refundAmt)));
                    map.put("date3", new TemplateData(DateUtil.dateFormate(new Date(), GloabConst.TimePattern.YMDHMS)));
                    map.put("character_string4", new TemplateData(mchPromiseOld.getOrderNo()));
                    map.put("thing5", new TemplateData(proName));
                    map.put("thing6", new TemplateData(thing6));
                    String response = AppletUtil.sendMessage(accessToken, openId, noticeModel.getRefund_res(), map);
                    logger.error("=================微信消息推送返回值：{}", response);
                }
            }
            return resultMap;
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("微信退款 异常:", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "returnAlipay");
        }
    }

    @Override
    public Map<String, String> refundOrder(int storeId, int id, int isPass, String refusedWhy, String className, String treadeNo, BigDecimal refundAmt) throws LaiKeAPIException
    {
        try
        {
            return refundOrder(storeId, id, isPass, refusedWhy, className, treadeNo, refundAmt);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("支付宝退款 异常:", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "returnAlipay");
        }
    }

    @Override
    public Map<String, String> refundOrder(int storeId, Integer id, String className, String treadeNo, BigDecimal refundAmt) throws LaiKeAPIException
    {
        try
        {
            return refundOrder(storeId, id, className, treadeNo, refundAmt, false);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("微信退款 异常:", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "returnAlipay");
        }
    }


    @Override
    public Map<String, String> refundOrder(int storeId, Integer id, String className, String treadeNo, BigDecimal refundAmt, BigDecimal orderPrice) throws LaiKeAPIException
    {
        try
        {
            return refundOrder(storeId, id, className, treadeNo, refundAmt, false, orderPrice);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("微信退款 异常:", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "returnAlipay");
        }
    }

    @Override
    public void walletPay(String userId, BigDecimal money, String token, String tokenKey, int recordType, String recordEvent) throws LaiKeAPIException
    {
        try
        {
            if (StringUtils.isEmpty(userId))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YHWK, "用户id为空");
            }
            User user = new User();
            user.setUser_id(userId);
            user = userBaseMapper.selectOne(user);
            if (user == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YHBCZ, "用户不存在");
            }
            //清空密码尝试次数
            User updateUser = new User();
            updateUser.setId(user.getId());
            updateUser.setLogin_num(0);
            userBaseMapper.updateByPrimaryKeySelective(updateUser);
            // 用户余额
            BigDecimal userMoney = user.getMoney();
            // 用户余额 大于 余额抵扣金额
            if (userMoney.compareTo(money) >= 0)
            {
                // 根据微信id,修改用户余额
                int row = userMapper.walletPayUpdateUserAccount(money, user.getStore_id(), userId);
                if (row < 0)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_ZFSB, "支付失败", "pay");
                }

                RecordModel recordModel = new RecordModel();
                recordModel.setStore_id(user.getStore_id());
                recordModel.setUser_id(userId);
                recordModel.setMoney(money);
                recordModel.setOldmoney(userMoney);
                recordModel.setEvent(recordEvent);
                recordModel.setType(recordType);
                recordModel.setAdd_date(new Date());
                recordModel.setIs_mch(DictionaryConst.WhetherMaven.WHETHER_NO);
                recordModelMapper.insertSelective(recordModel);

                //刷新缓存
                User userCache = userBaseMapper.selectByPrimaryKey(user.getId());
                RedisDataTool.refreshRedisUserCache(userCache, redisUtil);
            }
            else
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YEBZ, "余额不足", "pay");
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("余额支付 异常:", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "walletPay");
        }
    }

    @Override
    public void walletPay(String userId, BigDecimal money, String token, String sNo) throws LaiKeAPIException
    {
        walletPay(userId, money, token, GloabConst.RedisHeaderKey.LOGIN_ACCESS_TOKEN, sNo);
    }

    @Override
    public void walletPay(String userId, BigDecimal money, String token, String tokenKey, String sNo) throws LaiKeAPIException
    {
        try
        {
            this.walletPay(userId, money, token, tokenKey, RecordModel.RecordType.BALANCE_CONSUMPTION, userId + "使用了" + money.toString() + "元余额", sNo);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("余额支付 异常:", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "walletPay");
        }
    }

    @Override
    public void walletPay(String userId, BigDecimal money, String token, String tokenKey, int recordType, String recordEvent, String sNo) throws LaiKeAPIException
    {
        try
        {
            if (StringUtils.isEmpty(userId))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YHWK, "用户id为空");
            }
            User user = new User();
            user.setUser_id(userId);
            user = userBaseMapper.selectOne(user);
            if (user == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YHBCZ, "用户不存在");
            }
            //清空密码尝试次数
            User updateUser = new User();
            updateUser.setId(user.getId());
            updateUser.setLogin_num(0);
            userBaseMapper.updateByPrimaryKeySelective(updateUser);
            // 用户余额
            BigDecimal userMoney = user.getMoney();
            // 用户余额 大于 余额抵扣金额
            if (userMoney.compareTo(money) >= 0)
            {
                // 根据微信id,修改用户余额
                int row = userMapper.walletPayUpdateUserAccount(money, user.getStore_id(), userId);
                if (row < 0)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_ZFSB, "支付失败", "pay");
                }
                user = userBaseMapper.selectByPrimaryKey(user.getId());
                //添加充值记录详情
                RecordDetailsModel recordDetailsModel = new RecordDetailsModel();
                recordDetailsModel.setStore_id(user.getStore_id());
                recordDetailsModel.setMoney(money);
                recordDetailsModel.setUserMoney(user.getMoney());
                recordDetailsModel.setRecordTime(new Date());
                recordDetailsModel.setAddTime(new Date());
                if (sNo.contains(DictionaryConst.OrdersType.ORDERS_HEADER_DJ))
                {
                    recordDetailsModel.setMoneyType(RecordDetailsModel.moneyType.EXPENDITURE);
                    OrderDataModel orderDataModel = new OrderDataModel();
                    orderDataModel.setTrade_no(sNo);
                    orderDataModel = orderDataModelMapper.selectOne(orderDataModel);
                    //判断用户是否为续费
                    if (orderDataModel != null)
                    {
                        Map<String, Object> map = JSON.parseObject(orderDataModel.getData(), new TypeReference<Map<String, Object>>()
                        {
                        });
                        if (map.containsKey("is_renew") && DataUtils.getIntegerVal(map, "is_renew").equals(1))
                        {
                            recordDetailsModel.setMoneyTypeName(RecordDetailsModel.moneyTypeName.MEMBER_RENEW);
                            recordDetailsModel.setType(RecordDetailsModel.type.MEMBERSHIP_RENEW);
                        }
                        else
                        {
                            recordDetailsModel.setMoneyTypeName(RecordDetailsModel.moneyTypeName.MEMBER_ACTIVATION);
                            recordDetailsModel.setType(RecordDetailsModel.type.RECHARGE_MEMBERSHIP);
                        }
                    }
                    recordDetailsModel.setsNo(sNo);
                }
                else if (sNo.contains(DictionaryConst.OrdersType.ORDERS_HEADER_PS))
                {
                    PreSellRecordModel preSellRecordModel = new PreSellRecordModel();
                    preSellRecordModel.setsNo(sNo);
                    int payTarget = 3;
                    preSellRecordModel.setIs_pay(1);
                    //preSellRecordModel.setPay_type(PreSellRecordModel.DEPOSIT);
                    List<PreSellRecordModel> preSellRecordModelList = preSellRecordModelMapper.select(preSellRecordModel);
                    if (CollectionUtils.isNotEmpty(preSellRecordModelList))
                    {
                        for (PreSellRecordModel sellRecordModel : preSellRecordModelList)
                        {
                            //支付类型 0.定金 1.尾款
                            Integer payType = sellRecordModel.getPay_type();
                            if(Objects.isNull(payType))break;
                            payTarget = payType == 0 ? 1 : 2;
                        }
                    }
                    if (payTarget == 1)
                    {
                        recordDetailsModel.setMoneyType(RecordDetailsModel.moneyType.EXPENDITURE);
                        recordDetailsModel.setMoneyTypeName(RecordDetailsModel.moneyTypeName.PAYMENT_DEPOSIT);
                        recordDetailsModel.setType(RecordDetailsModel.type.PARTICIPATE_IN_ACTIVITIES);
                        recordDetailsModel.setTypeName("预售活动");
                        recordDetailsModel.setsNo(sNo);
                    }else if (payTarget == 2)
                    {
                        recordDetailsModel.setMoneyType(RecordDetailsModel.moneyType.EXPENDITURE);
                        recordDetailsModel.setMoneyTypeName(RecordDetailsModel.moneyTypeName.PAYMENT_FINAL);
                        recordDetailsModel.setType(RecordDetailsModel.type.PARTICIPATE_IN_ACTIVITIES);
                        recordDetailsModel.setTypeName("预售活动");
                        recordDetailsModel.setsNo(sNo);
                    }
                    else
                    {
                        recordDetailsModel.setMoneyType(RecordDetailsModel.moneyType.EXPENDITURE);
                        recordDetailsModel.setMoneyTypeName(RecordDetailsModel.moneyTypeName.ORDER_YS_ORDER);
                        recordDetailsModel.setType(RecordDetailsModel.type.ORDER_PAYMENT);
                        recordDetailsModel.setsNo(sNo);
                    }
                }
                else if (sNo.contains(DictionaryConst.OrdersType.ORDERS_HEADER_MCH_PROMISE))
                {
                    recordDetailsModel.setMoneyType(RecordDetailsModel.moneyType.EXPENDITURE);
                    recordDetailsModel.setMoneyTypeName(RecordDetailsModel.moneyTypeName.MCH_DEPOSIT);
                    recordDetailsModel.setType(RecordDetailsModel.type.STORE_DEPOSIT_PAYMENT);
                    recordDetailsModel.setsNo(sNo);
                }
                else if (sNo.contains(DictionaryConst.OrdersType.ORDERS_HEADER_JB))
                {
                    recordDetailsModel.setMoneyType(RecordDetailsModel.moneyType.EXPENDITURE);
                    recordDetailsModel.setMoneyTypeName(RecordDetailsModel.moneyTypeName.ORDER_AUCTION_ORDER);
                    recordDetailsModel.setType(RecordDetailsModel.type.PARTICIPATE_IN_ACTIVITIES);
                    recordDetailsModel.setTypeName("竞拍活动");
                    recordDetailsModel.setsNo(sNo);
                }
                else
                {
                    if (sNo.contains(DictionaryConst.OrdersType.ORDERS_HEADER_PT) || sNo.contains(DictionaryConst.OrdersType.ORDERS_HEADER_KT))
                    {
                        recordDetailsModel.setMoneyTypeName(RecordDetailsModel.moneyTypeName.ORDER_PT_ORDER);
                    }
                    else if (sNo.contains(DictionaryConst.OrdersType.ORDERS_HEADER_FX))
                    {
                        recordDetailsModel.setMoneyTypeName(RecordDetailsModel.moneyTypeName.ORDER_FX_ORDER);
                    }
                    else if (sNo.contains(DictionaryConst.OrdersType.ORDERS_HEADER_MS))
                    {
                        recordDetailsModel.setMoneyTypeName(RecordDetailsModel.moneyTypeName.ORDER_MS_ORDER);
                    }
                    else if (sNo.contains(DictionaryConst.OrdersType.ORDERS_HEADER_FS))
                    {
                        recordDetailsModel.setMoneyTypeName(RecordDetailsModel.moneyTypeName.ORDER_FS_ORDER);
                    }
                    else if (sNo.contains(DictionaryConst.OrdersType.ORDERS_HEADER_VI))
                    {
                        recordDetailsModel.setMoneyTypeName(RecordDetailsModel.moneyTypeName.ORDER_VI_ORDER);
                    }
                    else if (sNo.contains(DictionaryConst.OrdersType.ORDERS_HEADER_ZB))
                    {
                        recordDetailsModel.setMoneyTypeName(RecordDetailsModel.moneyTypeName.ORDER_ZB_ORDER);
                    }
                    //4174 【JAVA开发环境】积分：移动端--积分订单下单成功后，在我的余额--账单明细里支出类型应该是积分订单
                    else if (sNo.contains(DictionaryConst.OrdersType.ORDERS_HEADER_IN))
                    {
                        recordDetailsModel.setMoneyTypeName(RecordDetailsModel.moneyTypeName.ORDER_IN_ORDER);
                    }
                    else
                    {
                        recordDetailsModel.setMoneyTypeName(RecordDetailsModel.moneyTypeName.ORDER_REFUND);
                    }
                    recordDetailsModel.setMoneyType(RecordDetailsModel.moneyType.EXPENDITURE);
                    recordDetailsModel.setType(RecordDetailsModel.type.ORDER_PAYMENT);
                    recordDetailsModel.setsNo(sNo);
                }

                Map<String, Object> userCurrecyMap  = currencyStoreModelMapper.getCurrencyInfo(user.getStore_id(), user.getPreferred_currency());
                Map<String, Object> storeCurrecyMap = currencyStoreModelMapper.getDefaultCurrency(user.getStore_id());
                recordDetailsModel.setCurrency_code(DataUtils.getStringVal(userCurrecyMap, "currency_code", DataUtils.getStringVal(storeCurrecyMap, "currency_code")));
                recordDetailsModel.setCurrency_symbol(DataUtils.getStringVal(userCurrecyMap, "currency_symbol", DataUtils.getStringVal(storeCurrecyMap, "currency_symbol")));
                recordDetailsModel.setExchange_rate(DataUtils.getBigDecimalVal(userCurrecyMap, "exchange_rate", DataUtils.getBigDecimalVal(storeCurrecyMap, "exchange_rate")));


                recordDetailsModelMapper.insertSelective(recordDetailsModel);
                RecordModel recordModel = new RecordModel();
                recordModel.setStore_id(user.getStore_id());
                recordModel.setUser_id(userId);
                recordModel.setMoney(money);
                recordModel.setOldmoney(userMoney);
                recordModel.setEvent(recordEvent);
                recordModel.setType(recordType);
                recordModel.setAdd_date(new Date());
                recordModel.setIs_mch(DictionaryConst.WhetherMaven.WHETHER_NO);
                recordModel.setDetails_id(recordDetailsModel.getId());
                recordModelMapper.insertSelective(recordModel);

                //刷新缓存
                User userCache = userBaseMapper.selectByPrimaryKey(user.getId());
                RedisDataTool.refreshRedisUserCache(userCache, redisUtil);
            }
            else
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YEBZ, "余额不足", "pay");
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("余额支付 异常:", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "walletPay");
        }
    }

    @Override
    public void integralPay(String userId, BigDecimal money, String orderNo, BigDecimal orderPrice, String token) throws LaiKeAPIException
    {
        try
        {
            if (StringUtils.isEmpty(userId))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YHWK, "用户id为空");
            }
            User user = new User();
            user.setUser_id(userId);
            user = userBaseMapper.selectOne(user);
            if (user == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YHBCZ, "用户不存在");
            }
            // 用户余额
            BigDecimal userMoney = new BigDecimal(user.getScore());
            // 用户积分余额 大于 积分余额抵扣金额
            if (userMoney.compareTo(money) >= 0)
            {
                int  row;
                Date sysDate = new Date();
                //结算扣除用户过期积分
                //todo 使用了的积分也要扣除掉？
                row = signRecordModelMapper.settlementIntegralByUserId(user.getStore_id(), user.getUser_id(), sysDate);
                logger.debug("扣除用户过期积分 执行结果:{}", row);
                //失效积分
                row = signRecordModelMapper.userInvalidScoreByUserId(user.getUser_id(), sysDate);
                logger.debug("失效积分 执行结果:{}", row);
                // 根据微信id,修改用户余额
                row = userMapper.scorePayUpdateUserAccount(money, user.getStore_id(), userId);
                if (row < 0)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_JFZFSB, "积分支付失败", "pay");
                }
                String     event   = userId + "使用了" + money.toString() + "积分";
                AddScoreVo scoreVo = new AddScoreVo();
                scoreVo.setStoreId(user.getStore_id());
                scoreVo.setUserId(user.getUser_id());
                scoreVo.setScore(money.intValue());
                scoreVo.setScoreOld(user.getScore());
                scoreVo.setEvent(event);
                scoreVo.setType(SignRecordModel.ScoreType.CONSUMPTION);
                scoreVo.setOrderNo(orderNo);
                publicIntegralService.addScore(scoreVo);

                //刷新缓存
                User userCache = userBaseMapper.selectByPrimaryKey(user.getId());
                RedisDataTool.refreshRedisUserCache(token, userCache, redisUtil);
            }
            else
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_JFBZ, "积分不足", "integralPay");
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("积分支付 异常:", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "integralPay");
        }
    }

    @Override
    public void walletReturnPay(String userId, BigDecimal money, String token) throws LaiKeAPIException
    {
        try
        {
            if (StringUtils.isEmpty(userId))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YHWK, "用户id为空");
            }
            User user = new User();
            user.setUser_id(userId);
            user = userBaseMapper.selectOne(user);
            if (user == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YHBCZ, "用户不存在");
            }
            // 退还
            int row = userMapper.updateUserMoney(money, user.getStore_id(), userId);
            //判断是否添加成功
            if (row > 0)
            {
                logger.info("余额退款成功");
            }
            else
            {
                logger.info("余额退款失败");
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TKSB, "退款失败");
            }
            // 添加买家退款日志

            String      event       = userId + "退款" + money + "元余额";
            RecordModel recordModel = new RecordModel();
            recordModel.setStore_id(user.getStore_id());
            recordModel.setUser_id(userId);
            recordModel.setOldmoney(money);
            recordModel.setMoney(money);
            recordModel.setEvent(event);
            recordModel.setType(RecordModel.RECORDTYPE_RETURNAMT);
            row = recordModelMapper.insertSelective(recordModel);

            if (row > 0)
            {
                logger.info("新增记录成功");
            }
            else
            {
                logger.info("新增记录失败");
            }
            //刷新缓存
            RedisDataTool.refreshRedisUserCache(userBaseMapper.selectByPrimaryKey(user.getId()), redisUtil);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("钱包退款 异常:", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "walletReturnPay");
        }
    }


    @Override
    public void walletReturnPay(String userId, PromiseShModel promiseShModel, BigDecimal money, String token) throws LaiKeAPIException
    {
        try
        {
            if (StringUtils.isEmpty(userId))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YHWK, "用户id为空");
            }
            User user = new User();
            user.setUser_id(userId);
            user = userBaseMapper.selectOne(user);
            if (user == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YHBCZ, "用户不存在");
            }
            // 退还
            int row = userMapper.updateUserMoney(money, user.getStore_id(), userId);
            //判断是否添加成功
            if (row > 0)
            {
                logger.info("余额退款成功");
            }
            else
            {
                logger.info("余额退款失败");
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TKSB, "退款失败");
            }
            // 添加买家退款日志

            String      event       = userId + "退款" + money + "元余额";
            RecordModel recordModel = new RecordModel();
            recordModel.setStore_id(user.getStore_id());
            recordModel.setUser_id(userId);
            recordModel.setOldmoney(money);
            recordModel.setMoney(money);
            recordModel.setEvent(event);
            recordModel.setType(RecordModel.RECORDTYPE_RETURNAMT);
            row = recordModelMapper.insertSelective(recordModel);

            if (row > 0)
            {
                logger.info("新增记录成功");
            }
            else
            {
                logger.info("新增记录失败");
            }
            //修改表
            promiseShModelMapper.updateByPrimaryKeySelective(promiseShModel);
            PromiseShModel promiseShTKModel = promiseShModelMapper.selectByPrimaryKey(promiseShModel.getId());
            //根据审核表获取保证金表
            MchPromiseModel mchPromiseOld = new MchPromiseModel();
            mchPromiseOld.setStatus(MchPromiseModel.PromiseConstant.STATUS_PAY);
            mchPromiseOld.setMch_id(promiseShTKModel.getMch_id());
            mchPromiseOld = mchPromiseModelMapper.selectOne(mchPromiseOld);
            //修改保证金表
            MchPromiseModel mchPromiseUpdate = new MchPromiseModel();
            mchPromiseUpdate.setStatus(MchPromiseModel.PromiseConstant.STATUS_RETURN_PAY);
            mchPromiseUpdate.setIs_return_pay(1);
            mchPromiseUpdate.setId(mchPromiseOld.getId());
            mchPromiseUpdate.setUpdate_date(new Date());
            int row1 = mchPromiseModelMapper.updateByPrimaryKeySelective(mchPromiseUpdate);
            if (row1 < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TKSB, "退款失败");
            }
            //刷新缓存
            RedisDataTool.refreshRedisUserCache(userBaseMapper.selectByPrimaryKey(user.getId()), redisUtil);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("钱包退款 异常:", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "walletReturnPay");
        }
    }

    @Override
    public void walletReturnPay(String userId, PromiseShModel promiseShModel, BigDecimal money, String token, Integer mchId) throws LaiKeAPIException
    {
        try
        {
            if (StringUtils.isEmpty(userId))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YHWK, "用户id为空");
            }
            User user = new User();
            user.setUser_id(userId);
            user = userBaseMapper.selectOne(user);
            if (user == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YHBCZ, "用户不存在");
            }
            // 退还
            int row = userMapper.updateUserMoney(money, user.getStore_id(), userId);
            //判断是否添加成功
            if (row > 0)
            {
                logger.info("余额退款成功");
            }
            else
            {
                logger.info("余额退款失败");
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TKSB, "退款失败");
            }


            //添加充值记录详情
            RecordDetailsModel recordDetailsModel = new RecordDetailsModel();
            recordDetailsModel.setStore_id(user.getStore_id());
            recordDetailsModel.setMoney(money);
            recordDetailsModel.setUserMoney(user.getMoney());
            recordDetailsModel.setMoneyType(RecordDetailsModel.moneyType.INCOME);
            recordDetailsModel.setMoneyTypeName(RecordDetailsModel.moneyTypeName.MCH_DEPOSIT);
            recordDetailsModel.setType(RecordDetailsModel.type.MCH_DEPOSIT_WITHDRAWAL);
            recordDetailsModel.setRecordTime(new Date());
            recordDetailsModel.setAddTime(new Date());

            Map<String, Object> userCurrecyMap  = currencyStoreModelMapper.getCurrencyInfo(user.getStore_id(), user.getPreferred_currency());
            Map<String, Object> storeCurrecyMap = currencyStoreModelMapper.getDefaultCurrency(user.getStore_id());
            recordDetailsModel.setCurrency_code(DataUtils.getStringVal(userCurrecyMap, "currency_code", DataUtils.getStringVal(storeCurrecyMap, "currency_code")));
            recordDetailsModel.setCurrency_symbol(DataUtils.getStringVal(userCurrecyMap, "currency_symbol", DataUtils.getStringVal(storeCurrecyMap, "currency_symbol")));
            recordDetailsModel.setExchange_rate(DataUtils.getBigDecimalVal(userCurrecyMap, "exchange_rate", DataUtils.getBigDecimalVal(storeCurrecyMap, "exchange_rate")));


            row = recordDetailsModelMapper.insert(recordDetailsModel);

            //增加店铺保证金退还记录
            RecordModel recordModel = new RecordModel();
            recordModel.setStore_id(user.getStore_id());
            recordModel.setUser_id(userId);
            recordModel.setMoney(money);
            recordModel.setOldmoney(user.getMoney());
            recordModel.setEvent("退还店铺保证金");
            recordModel.setType(RecordModel.RecordType.REFUND_MCH_BOND);
            recordModel.setAdd_date(new Date());
            recordModel.setIs_mch(DictionaryConst.WhetherMaven.WHETHER_NO);
            recordModel.setDetails_id(recordDetailsModel.getId());
            recordModel.setMain_id(mchId.toString());
            row = recordModelMapper.insertSelective(recordModel);
            if (row > 0)
            {
                logger.info("新增记录成功");
            }
            else
            {
                logger.info("新增记录失败");
            }
            //修改表
            promiseShModelMapper.updateByPrimaryKeySelective(promiseShModel);
            PromiseShModel promiseShTKModel = promiseShModelMapper.selectByPrimaryKey(promiseShModel.getId());
            //根据审核表获取保证金表
            MchPromiseModel mchPromiseOld = new MchPromiseModel();
            mchPromiseOld.setStatus(MchPromiseModel.PromiseConstant.STATUS_PAY);
            mchPromiseOld.setMch_id(promiseShTKModel.getMch_id());
            mchPromiseOld = mchPromiseModelMapper.selectOne(mchPromiseOld);
            //修改保证金表
            MchPromiseModel mchPromiseUpdate = new MchPromiseModel();
            mchPromiseUpdate.setStatus(MchPromiseModel.PromiseConstant.STATUS_RETURN_PAY);
            mchPromiseUpdate.setIs_return_pay(1);
            mchPromiseUpdate.setId(mchPromiseOld.getId());
            mchPromiseUpdate.setUpdate_date(new Date());
            int row1 = mchPromiseModelMapper.updateByPrimaryKeySelective(mchPromiseUpdate);
            if (row1 < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TKSB, "退款失败");
            }
            //刷新缓存
            RedisDataTool.refreshRedisUserCache(userBaseMapper.selectByPrimaryKey(user.getId()), redisUtil);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("钱包退款 异常:", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "walletReturnPay");
        }
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> divideAccount(String orderNo, String transactionId, StringBuilder loggerStr) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            logger.debug("######################微信分账开始#####################");
            //查询订单信息
            OrderModel orderModel = new OrderModel();
            orderModel.setReal_sno(orderNo);
            orderModel = orderModelMapper.selectOne(orderModel);
            if (orderModel == null)
            {
                throw new LaiKeAPIException(API_OPERATION_FAILED, "微信分账失败:订单信息不存在", "divideAccount");
            }
            logger.debug("分账订单信息::::::::::::::::::::{}", JSONObject.toJSONString(orderModel));
            //获取订单店铺信息
            String   orderMchId = StringUtils.trim(orderModel.getMch_id(), SplitUtils.DH);
            MchModel mchModel   = mchModelMapper.selectByPrimaryKey(orderMchId);
            //获取订单店铺所有分账账户信息
            MchDistributionModel mchDistributionQuery = new MchDistributionModel();
            mchDistributionQuery.setMch_id(mchModel.getId());
            List<MchDistributionModel> mchDistributionModelList = mchDistributionModelMapper.select(mchDistributionQuery);
            //获取服务商商户id和服务商应用id
            String paymentJson = URLDecoder.decode(paymentConfigModelMapper.getPaymentConfigInfo(orderModel.getStore_id(), orderModel.getPay()), GloabConst.Chartset.UTF_8);
            logger.debug(orderModel.getPay() + "支付配置信息：" + paymentJson);
            JSONObject payJson = JSONObject.parseObject(paymentJson);
            String     mchId   = payJson.getString("mch_id");
            logger.debug("mchID:{}", mchId);
            String appId = payJson.getString("appid");
            logger.debug("appID:{}", appId);
            String keyPem = payJson.getString("key_pem");
            logger.debug("key_pem:{}", keyPem);
            String divideAccount = payJson.getString("divide_account");
            logger.debug("divide_account:{}", divideAccount);
            // 统一参数封装
            Map<String, Object> params = new HashMap<>(8);
            //微信支付分配的子商户号，即分账的出资商户号。
            params.put("sub_mchid", mchModel.getSub_mch_id());
            //微信分配的服务商appid
            params.put("appid", appId);
            //微信支付订单号，成功支付后获取,实际项目中：支付回调时需要更新到order表中
            params.put("transaction_id", transactionId);
            // 商户系统内部的分账单号，在商户系统内部唯一
            int    outOrderNo       = new java.security.SecureRandom().nextInt(9999999);
            long   l                = System.currentTimeMillis();
            String divideAccountsNo = "DA" + l + outOrderNo;
            params.put("out_trade_no", divideAccountsNo);
            /**
             * 是否完成分账(是否解冻剩余未分资金)
             * 1、如果为true，该笔订单剩余未分账的金额会解冻回分账方商户；
             * 2、如果为false，该笔订单剩余未分账的金额不会解冻回分账方商户，可以对该笔订单再次进行分账。
             */
            params.put("unfreeze_unsplit", false);
            //请求分账前需确认订单中可分配分账金额有多少
            Map<String, Object> remainingAmountMap = queryRemainingAmount(keyPem, mchId, WxV3PayConfig.mchSerialNo, transactionId);
            logger.debug("订单中可分配分账金额信息：{}", JSONObject.toJSONString(remainingAmountMap));
            // TODO 分账计算方法：(服务商 = 售价 - 成本价    分账账户 = (订单总金额 - 服务商获得的钱) * 分账账户比例)
            //设置分账接收方列表，可以分给多个商户或者个人
            List<Map<String, Object>> receivers = new ArrayList<>();
            //服务商分账信息
            BigDecimal        serviceAmount     = BigDecimal.ZERO;
            OrderDetailsModel orderDetailsQuery = new OrderDetailsModel();
            orderDetailsQuery.setR_sNo(orderModel.getsNo());
            orderDetailsQuery.setR_status(DictionaryConst.OrdersStatus.ORDERS_R_STATUS_COMPLETE);
            List<OrderDetailsModel> orderDetailsModels = orderDetailsModelMapper.select(orderDetailsQuery);
            for (OrderDetailsModel orderDetailsModel : orderDetailsModels)
            {
//                ConfiGureModel regionalConfigure = confiGureModelMapper.getRegionalConfigure(orderDetailsModel.getP_id(), orderModel.getRegion_id());
//                logger.info("商品id:{}  成本价：{}  售价：{}", regionalConfigure.getPid(), regionalConfigure.getCostprice(), regionalConfigure.getPrice());
//                serviceAmount = serviceAmount.add(regionalConfigure.getPrice().subtract(regionalConfigure.getCostprice()).multiply(new BigDecimal(orderDetailsModel.getNum())));
            }
            logger.debug("固定分账账户{}  分账金额{}", divideAccount, serviceAmount);
            if (serviceAmount.compareTo(BigDecimal.ZERO) > 0)
            {
                Map<String, Object> serviceMap = new HashMap<>(4);
                serviceMap.put("type", "MERCHANT_ID");
                serviceMap.put("account", divideAccount);
                serviceMap.put("amount", serviceAmount.multiply(new BigDecimal(100)).intValue());
                serviceMap.put("description", "订单" + orderModel.getsNo() + "分账");
                receivers.add(serviceMap);
            }
            //分账账户分账信息
            BigDecimal receiverPrice = orderModel.getZ_price().subtract(serviceAmount);
            logger.debug("剩余分账金额{}", receiverPrice);
            if (receiverPrice.compareTo(BigDecimal.ZERO) > 0)
            {
                for (MchDistributionModel mchDistributionModel : mchDistributionModelList)
                {
                    Map<String, Object> receiversMap = new HashMap<>(4);
                    receiversMap.put("type", mchDistributionModel.getD_type());
                    receiversMap.put("account", mchDistributionModel.getAccount());
                    // 分账金额，单位为分，只能为整数 因此与比例相乘无需再÷百分号
                    BigDecimal multiply = receiverPrice.multiply(mchDistributionModel.getProportion());
                    logger.info("分账账户{}获得分账金额:{}分", mchDistributionModel.getAccount(), multiply);
                    if (multiply.setScale(0, BigDecimal.ROUND_HALF_DOWN).intValue() <= 0)
                    {
                        logger.info("四舍五入取整后分账金额:{} 不参与分账", multiply.setScale(0, BigDecimal.ROUND_HALF_DOWN).intValue());
                        continue;
                    }
                    receiversMap.put("amount", multiply.setScale(0, BigDecimal.ROUND_HALF_DOWN).intValue());
                    // 分账描述
                    receiversMap.put("description", "订单" + orderModel.getsNo() + "分账");
                    receivers.add(receiversMap);
                }
            }
            params.put("receivers", receivers);
            String paramsStr = JSON.toJSONString(params);
            logger.debug("请求参数 ===> {}", paramsStr);
            loggerStr.append(String.format("请求参数 ===> {%s}", paramsStr));
            CloseableHttpClient httpClient = WXPaySignatureCertificateUtil.checkSign(keyPem, mchId, WxV3PayConfig.mchSerialNo);
            HttpPost            httpPost   = new HttpPost(WXPayV3Constants.REQUEST_FOR_LEDGER_ALLOCATION);
            StringEntity        entity     = new StringEntity(paramsStr, "utf-8");
            entity.setContentType("application/json");
            httpPost.setEntity(entity);
            httpPost.setHeader("Accept", "application/json");
            CloseableHttpResponse response = httpClient.execute(httpPost);
            //获取返回状态
            int    statusCode = response.getStatusLine().getStatusCode();
            String result     = EntityUtils.toString(response.getEntity(), "UTF-8");
            if (statusCode == 200)
            {
                //创建分账记录
                JSONObject jsonObject = JSONObject.parseObject(result);
                //分帐单号
                String divideAccountNo = jsonObject.getString("out_order_no");
                //分账账户信息列表
                String    receiverList = jsonObject.getString("receivers");
                List<Map> maps         = JSONArray.parseArray(receiverList, Map.class);
                for (Map receiverMap : maps)
                {
                    //分账账户商户id
                    String account = MapUtils.getString(receiverMap, "account");
                    //分账金额
                    String                     amount                     = MapUtils.getString(receiverMap, "amount");
                    MchDistributionRecordModel mchDistributionRecordModel = new MchDistributionRecordModel();
                    mchDistributionRecordModel.setOrder_no(orderModel.getsNo());
                    mchDistributionRecordModel.setWx_order_no(transactionId);
                    mchDistributionRecordModel.setMch_id(mchModel.getId());
                    mchDistributionRecordModel.setSub_mch_id(mchModel.getSub_mch_id());
                    mchDistributionRecordModel.setAccount(account);
                    mchDistributionRecordModel.setOut_order_no(divideAccountNo);
                    mchDistributionRecordModel.setTotal_amount(BigDecimal.ZERO);
                    mchDistributionRecordModel.setAmount(new BigDecimal(amount).divide(new BigDecimal(100)));
                    mchDistributionRecordModel.setR_type(MchDistributionRecordModel.Type.DIVIDEACCOUNTS);
                    mchDistributionRecordModel.setAdd_date(new Date());
                    mchDistributionRecordModelMapper.insertSelective(mchDistributionRecordModel);
                }
            }
            // 执行结果
            logger.debug("分账响应参数：{}", result);
            resultMap.put("resultJsonString", result);
            logger.debug("######################微信分账结束#####################");
        }
        catch (Exception e)
        {
            logger.error("微信分账失败", e);
            throw new LaiKeAPIException(API_OPERATION_FAILED, "微信分账失败", "divideAccount");
        }
        return resultMap;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> newDivideAccount(String orderNo, String transactionId, StringBuilder loggerStr) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            logger.debug("######################微信分账开始#####################");
            //查询订单信息
            OrderModel orderModel = new OrderModel();
            orderModel.setReal_sno(orderNo);
            orderModel = orderModelMapper.selectOne(orderModel);
            if (orderModel == null)
            {
                throw new LaiKeAPIException(API_OPERATION_FAILED, "微信分账失败:订单信息不存在", "divideAccount");
            }
            logger.debug("分账订单信息::::::::::::::::::::{}", JSONObject.toJSONString(orderModel));
            //获取订单店铺信息
            String   orderMchId = StringUtils.trim(orderModel.getMch_id(), SplitUtils.DH);
            MchModel mchModel   = mchModelMapper.selectByPrimaryKey(orderMchId);
            //获取订单店铺所有分账账户信息
            MchDistributionModel mchDistributionQuery = new MchDistributionModel();
            mchDistributionQuery.setMch_id(mchModel.getId());
            List<MchDistributionModel> mchDistributionModelList = mchDistributionModelMapper.select(mchDistributionQuery);
            //获取服务商商户id和服务商应用id
            String paymentJson = URLDecoder.decode(paymentConfigModelMapper.getPaymentConfigInfo(orderModel.getStore_id(), orderModel.getPay()), GloabConst.Chartset.UTF_8);
            logger.debug(orderModel.getPay() + "支付配置信息：" + paymentJson);
            JSONObject payJson = JSONObject.parseObject(paymentJson);
            String     mchId   = payJson.getString("mch_id");
            logger.debug("mchID:{}", mchId);
            String appId = payJson.getString("appid");
            logger.debug("appID:{}", appId);
            String keyPem = payJson.getString("key_pem");
            logger.debug("key_pem:{}", keyPem);
            String mch_key = payJson.getString("mch_key");
            logger.debug("mch_key:{}", mch_key);
            String divideAccount = payJson.getString("divide_account");
            logger.debug("divide_account:{}", divideAccount);
            String out_order_no = orderModel.getReal_sno();
            logger.debug("out_order_no:{}", out_order_no);
            String transaction_id = orderModel.getTransaction_id();
            logger.debug("transaction_id:{}", transaction_id);
            String nonce_str = WXPayUtil.generateNonceStr();

            String sign_type = "HMAC-SHA256";
            //分账账户分账信息
            BigDecimal receiverPrice = orderModel.getZ_price();
            logger.debug("分账金额{}", receiverPrice);
            //拼接xml参数
            StringBuilder sb        = new StringBuilder();
            StringBuilder receivers = new StringBuilder();
            sb.append("<xml>");
            sb.append("<appid>").append(appId).append("</appid>");
            sb.append("<mch_id>").append(mchId).append("</mch_id>");
            sb.append("<nonce_str>").append(nonce_str).append("</nonce_str>");
            sb.append("<out_order_no>").append(out_order_no).append("</out_order_no>");

            //          拼接receivers部分
            sb.append("<receivers>");
            receivers.append("[");
            if (receiverPrice.compareTo(BigDecimal.ZERO) > 0)
            {
                for (int i = 0; i < mchDistributionModelList.size(); i++)
                {
                    MchDistributionModel mchDistributionModel = mchDistributionModelList.get(i);
                    receivers.append("{");
                    //分账类型
                    receivers.append("\"type\": \"").append(mchDistributionModel.getD_type()).append("\",");
                    receivers.append("\"account\": \"").append(mchDistributionModel.getAccount()).append("\",");
                    BigDecimal proportion = mchDistributionModel.getProportion();
                    BigDecimal multiply   = receiverPrice.multiply(proportion);
                    logger.info("分账账户{}获得分账金额:{}分", mchDistributionModel.getAccount(), multiply);
                    //分账金额，单位为分，只能为整数，不能超过原订单支付金额及最大分账比例金额
                    receivers.append("\"amount\": ").append(multiply.setScale(0, BigDecimal.ROUND_HALF_DOWN)).append(",");
                    if (mchDistributionModel.getD_type().equals(MchDistributionModel.Type.MERCHANT_ID))
                    {
                        receivers.append("\"name\": \"").append(mchDistributionModel.getName()).append("\",");
                    }
                    else
                    {
                        receivers.append("\"name\": \"").append("").append("\",");
                    }
                    // 分账描述
                    receivers.append("\"description\": \"").append("订单" + orderModel.getsNo() + "分账").append("\"");
                    receivers.append("}");
                    if (i < mchDistributionModelList.size() - 1)
                    {
                        receivers.append(",");
                    }
                    receivers.append("");
                }
            }
            receivers.append("]");
            //将receivers加入到xml中
            sb.append(receivers);
            sb.append("</receivers>");

            sb.append("<sign_type>").append(sign_type).append("</sign_type>");
            sb.append("<transaction_id>").append(transactionId).append("</transaction_id>");
            //获取签名
            Map<String, String> queryMap = Maps.newHashMap();
            queryMap.put("appid", appId);
            queryMap.put("mch_id", mchId);
            queryMap.put("nonce_str", nonce_str);
            queryMap.put("out_order_no", orderModel.getReal_sno());
            queryMap.put("receivers", receivers.toString());
            queryMap.put("sign_type", sign_type);
            queryMap.put("transaction_id", transactionId);
            String querySign = WXPayUtil.generateSignature(queryMap, mch_key, WXPayConstants.SignType.HMACSHA256);
            logger.debug("querySign:{}", querySign);

            sb.append("<sign>").append(querySign).append("</sign>");

            sb.append("</xml>");
            logger.info("请求参数 === {}", sb);
            //双向认证请求
            PaymentVo paymentVo = new PaymentVo();
            paymentVo.setStoreId(orderModel.getStore_id());
            paymentVo.setPayType(orderModel.getPay());
            WechatConfigInfo config = this.getWechatConfigInfo(paymentVo);
            WXPay            wxPay  = new WXPay(config);
            //请求微信分账
            String request = wxPay.requestWithCert(WXPayConstants.SECAPI_PAY_MULTIPROFITSHARING, nonce_str, sb.toString(), 6000, 8000);
            //解析微信分账的结果集，返回为xml格式
            DocumentBuilderFactory factory  = DocumentBuilderFactory.newInstance();
            DocumentBuilder        builder  = factory.newDocumentBuilder();
            Document               document = builder.parse(new ByteArrayInputStream(request.getBytes(StandardCharsets.UTF_8)));
            // 获取根元素
            Element root       = document.getDocumentElement();
            String  returnCode = "";
            // 获取return_code元素
            NodeList returnCodeNodes = root.getElementsByTagName("return_code");
            if (returnCodeNodes.getLength() > 0)
            {
                Node returnCodeNode = returnCodeNodes.item(0);
                // 提取CDATA部分
                if (returnCodeNode.getFirstChild().getNodeType() == Node.CDATA_SECTION_NODE)
                {
                    returnCode = returnCodeNode.getFirstChild().getNodeValue();
                }
            }
            //获取返回状态
            if ("SUCCESS".equals(returnCode))
            {
                //创建分账记录
                //分帐单号
                String divideAccountNo = out_order_no;
                //分账账户信息列表
                //剩余金额
                BigDecimal syMoney = receiverPrice;
                for (MchDistributionModel mchDistributionModel : mchDistributionModelList)
                {
                    //分账金额
                    BigDecimal proportion = mchDistributionModel.getProportion();
                    BigDecimal multiply   = receiverPrice.multiply(proportion).setScale(0, BigDecimal.ROUND_HALF_DOWN);
                    syMoney = syMoney.subtract(multiply.divide(new BigDecimal(100)));
                    MchDistributionRecordModel mchDistributionRecordModel = new MchDistributionRecordModel();
                    mchDistributionRecordModel.setOrder_no(orderModel.getsNo());
                    mchDistributionRecordModel.setWx_order_no(transactionId);
                    mchDistributionRecordModel.setMch_id(mchModel.getId());
                    mchDistributionRecordModel.setAccount(mchDistributionModel.getAccount());
                    mchDistributionRecordModel.setOut_order_no(divideAccountNo);
                    mchDistributionRecordModel.setTotal_amount(receiverPrice);
                    mchDistributionRecordModel.setAmount(multiply.divide(new BigDecimal(100)));
                    mchDistributionRecordModel.setR_type(MchDistributionRecordModel.Type.DIVIDEACCOUNTS);
                    mchDistributionRecordModel.setAdd_date(new Date());
                    //0不是平台
                    mchDistributionRecordModel.setIs_platform_account(0);
                    mchDistributionRecordModelMapper.insertSelective(mchDistributionRecordModel);

                }
                //剩下的金额都是平台的
                MchDistributionRecordModel mchDistributionRecordModel = new MchDistributionRecordModel();
                mchDistributionRecordModel.setOrder_no(orderModel.getsNo());
                mchDistributionRecordModel.setWx_order_no(transactionId);
                mchDistributionRecordModel.setMch_id(mchModel.getId());
                mchDistributionRecordModel.setAccount(mchId);
                mchDistributionRecordModel.setOut_order_no(divideAccountNo);
                mchDistributionRecordModel.setTotal_amount(receiverPrice);
                mchDistributionRecordModel.setAmount(syMoney);
                mchDistributionRecordModel.setR_type(MchDistributionRecordModel.Type.DIVIDEACCOUNTS);
                mchDistributionRecordModel.setAdd_date(new Date());
                //1是平台
                mchDistributionRecordModel.setIs_platform_account(1);
                mchDistributionRecordModelMapper.insertSelective(mchDistributionRecordModel);
            }
            //执行结果
            logger.debug("分账响应结果：{}", request);
            resultMap.put("resultJsonString", returnCode);
            resultMap.put("result", request);
            logger.debug("######################微信分账结束#####################");
        }
        catch (Exception e)
        {
            logger.error("微信分账失败", e);
            throw new LaiKeAPIException(API_OPERATION_FAILED, "微信分账失败", "divideAccount");
        }
        return resultMap;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> dividendRefund(String orderNo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            logger.info("######################微信分账回退开始#####################");
            //查询订单信息
            OrderModel orderModel = new OrderModel();
            orderModel.setsNo(orderNo);
            orderModel = orderModelMapper.selectOne(orderModel);
            if (orderModel == null)
            {
                throw new LaiKeAPIException(API_OPERATION_FAILED, "微信分账回退失败:订单信息不存在", "divideAccount");
            }
            //获取服务商商户id和服务商应用id
            String paymentJson = URLDecoder.decode(paymentConfigModelMapper.getPaymentConfigInfo(orderModel.getStore_id(), orderModel.getPay()), GloabConst.Chartset.UTF_8);
            logger.info(orderModel.getPay() + "支付配置信息：" + paymentJson);
            JSONObject payJson = JSONObject.parseObject(paymentJson);
            String     mchId   = payJson.getString("mch_id");
            logger.info("mchID:{}", mchId);
            String appId = payJson.getString("appid");
            logger.info("appID:{}", appId);
            String keyPem = payJson.getString("key_pem");
            logger.info("key_pem:{}", keyPem);
            //获取分账记录
            MchDistributionRecordModel mchDistributionRecordModel = new MchDistributionRecordModel();
            mchDistributionRecordModel.setOrder_no(orderModel.getsNo());
            mchDistributionRecordModel.setR_type(MchDistributionRecordModel.Type.DIVIDEACCOUNTS);
            List<MchDistributionRecordModel> mchDistributionRecordModels = mchDistributionRecordModelMapper.select(mchDistributionRecordModel);
            for (MchDistributionRecordModel model : mchDistributionRecordModels)
            {
                // 统一参数封装
                Map<String, Object> params = new HashMap<>(8);
                //子商户号
                params.put("sub_mchid", model.getSub_mch_id());
                //商户分账单号
                params.put("out_order_no", model.getOut_order_no());
                //商户回退单号
                int    outOrderNo       = new java.security.SecureRandom().nextInt(9999999);
                long   time             = System.currentTimeMillis();
                String divideAccountsNo = "RF" + time + outOrderNo;
                params.put("out_return_no", divideAccountsNo);
                //回退商户号
                params.put("return_mchid", model.getAccount());
                //回退金额
                params.put("amount", model.getAmount().multiply(new BigDecimal(100)));
                //回退描述
                params.put("description", "订单" + orderNo + "退款成功");
                String jsonString = JSONObject.toJSONString(params);
                logger.info("分账回退请求参数：{}", jsonString);
                CloseableHttpClient httpClient = WXPaySignatureCertificateUtil.checkSign(keyPem, mchId, WxV3PayConfig.mchSerialNo);
                HttpPost            httpPost   = new HttpPost(WXPayV3Constants.REQUEST_FOR_DISTRIBUTION_AND_REFUND);
                StringEntity        entity     = new StringEntity(jsonString, "utf-8");
                entity.setContentType("application/json");
                httpPost.setEntity(entity);
                httpPost.setHeader("Accept", "application/json");
                CloseableHttpResponse response = httpClient.execute(httpPost);
                //获取返回状态
                int    statusCode = response.getStatusLine().getStatusCode();
                String result     = EntityUtils.toString(response.getEntity(), "UTF-8");
                // 执行结果
                logger.info("微信分账回退响应参数：{}", result);
                if (statusCode != 200)
                {
                    JSONObject jsonObject = JSONObject.parseObject(result);
                    throw new LaiKeAPIException(API_OPERATION_FAILED, jsonObject.get("message").toString(), "divideAccount");
                }
                else
                {
                    JSONObject jsonObject = JSONObject.parseObject(result);
                    model.setId(null);
                    model.setOut_order_no(divideAccountsNo);
                    model.setR_type(MchDistributionRecordModel.Type.REFUND);
                    model.setAdd_date(new Date());
                    mchDistributionRecordModelMapper.insertSelective(model);
                }
            }
            logger.info("######################微信分账回退结束#####################");
        }
        catch (Exception e)
        {
            logger.error("微信分账回退失败", e);
            throw new LaiKeAPIException(API_OPERATION_FAILED, "微信分账回退失败", "dividendRefund");
        }
        return resultMap;
    }

    private Map<String, Object> queryRemainingAmount(String keyPem, String mchId, String mchSerialNo, String transactionId) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            CloseableHttpClient httpClient = WXPaySignatureCertificateUtil.checkSign(keyPem, mchId, mchSerialNo);
            String              url        = String.format("https://api.mch.weixin.qq.com/v3/profitsharing/transactions/%s/amounts", transactionId);
            URIBuilder          uriBuilder = new URIBuilder(url);
            HttpGet             httpGet    = new HttpGet(uriBuilder.build());
            httpGet.addHeader("Accept", "application/json");
            httpGet.addHeader("Content-type", "application/json; charset=utf-8");
            CloseableHttpResponse httpResponse = httpClient.execute(httpGet);
            HttpEntity            entity       = httpResponse.getEntity();
            System.out.println(entity.getContent());
            if (entity != null)
            {
                String json_result = EntityUtils.toString(entity);
                System.out.println(json_result);
                JSONObject jsonObject = JSONObject.parseObject(json_result);
                resultMap.put("transaction_id", jsonObject.getString("transaction_id"));
                resultMap.put("unsplit_amount", jsonObject.getIntValue("unsplit_amount"));
            }
            else
            {
                throw new LaiKeAPIException(API_OPERATION_FAILED, "查询剩余待分金额失败", "queryRemainingAmount");
            }
        }
        catch (Exception e)
        {
            logger.error("查询剩余待分金额", e);
            throw new LaiKeAPIException(API_OPERATION_FAILED, "查询剩余待分金额失败", "queryRemainingAmount");
        }
        return resultMap;
    }

    // 随机生成16位字符串
    String getRandomStr()
    {
        String        base   = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random        random = new Random();
        StringBuilder sb     = new StringBuilder();
        for (int i = 0; i < 16; i++)
        {
            int number = random.nextInt(base.length());
            sb.append(base.charAt(number));
        }
        return sb.toString();
    }

    @Autowired
    private CurrencyStoreModelMapper currencyStoreModelMapper;

    @Autowired
    private PaymentModelMapper paymentModelMapper;

    @Autowired
    private PublicIntegralService publicIntegralService;

    @Autowired
    private IntegralConfigModelMapper integralConfigModelMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserBaseMapper userBaseMapper;

    @Autowired
    private RecordModelMapper recordModelMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private SignRecordModelMapper signRecordModelMapper;

    @Autowired
    private RecordDetailsModelMapper recordDetailsModelMapper;

    @Autowired
    private PreSellRecordModelMapper preSellRecordModelMapper;

    private WXPayRequest wxPayRequest;

    private WXPayConfig wxPayConfig;
}

