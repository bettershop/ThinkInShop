package com.laiketui.admins.admin.services.admindivideacc;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.google.common.collect.Maps;
import com.laiketui.admins.api.admin.admindivideacc.AdminDivideAccountsService;
import com.laiketui.common.mapper.*;
import com.laiketui.common.utils.EasyPoiExcelUtil;
import com.laiketui.common.utils.tool.cache.RedisDataTool;
import com.laiketui.common.utils.weixin.WXPaySignatureCertificateUtil;
import com.laiketui.common.utils.weixin.WxV3PayConfig;
import com.laiketui.common.utils.weixin.XmlUtil;
import com.laiketui.core.cache.RedisUtil;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.DictionaryConst;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.core.utils.tool.DataUtils;
import com.laiketui.core.utils.tool.DateUtil;
import com.laiketui.core.utils.tool.StringUtils;
import com.laiketui.domain.config.ConfigModel;
import com.laiketui.domain.divideAccount.MchDistributionModel;
import com.laiketui.domain.mch.AdminModel;
import com.laiketui.domain.mch.MchModel;
import com.laiketui.domain.user.User;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.Tool.ExcelParamVo;
import com.laiketui.domain.vo.divideAccount.DivideAccountInfoVo;
import com.laiketui.domain.vo.divideAccount.DivideAccountVo;
import com.wechat.pay.contrib.apache.httpclient.WechatPayHttpClientBuilder;
import com.wechat.pay.contrib.apache.httpclient.util.PemUtil;
import github.wxpay.sdk.WXPay;
import github.wxpay.sdk.WXPayConstants;
import github.wxpay.sdk.WXPayUtil;
import org.apache.commons.collections.MapUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.security.PrivateKey;
import java.security.Signature;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author: sunH_
 * @Date: Create in 11:44 2023/9/14
 */
@Service
public class AdminDivideAccountServiceImpl implements AdminDivideAccountsService
{

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private MchModelMapper mchModelMapper;

    @Autowired
    private MchDistributionModelMapper mchDistributionModelMapper;

    @Autowired
    private DictionaryListModelMapper dictionaryListModelMapper;

    @Autowired
    private PaymentConfigModelMapper paymentConfigModelMapper;

    @Autowired
    private MchDistributionRecordModelMapper mchDistributionRecordModelMapper;

    @Autowired
    private ReturnOrderModelMapper returnOrderModelMapper;

    @Autowired
    private ConfigModelMapper configModelMapper;

    @Override
    public Map<String, Object> divideAccountInfo(MainVo vo, Integer mchId) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            AdminModel user     = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            MchModel   mchModel = mchModelMapper.selectByPrimaryKey(mchId);
            if (mchModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_MBBCZ, "店铺信息不存在", "divideAccountInfo");
            }
//            resultMap.put("sub_mch_id", mchModel.getSub_mch_id());
//            resultMap.put("sub_app_id", mchModel.getSub_app_id());
            ConfigModel configModel = new ConfigModel();
            configModel.setStore_id(vo.getStoreId());
            configModel = configModelMapper.selectOne(configModel);
            if (configModel != null)
            {
                resultMap.put("accounts_set", configModel.getAccountsSet());
            }
            else
            {
                resultMap.put("accounts_set", "");
            }
            //分账账户
            Map<String, Object> pramMap = new HashMap<>(16);
            pramMap.put("mchId", mchModel.getId());
            List<Map<String, Object>> list = mchDistributionModelMapper.selectDynamic(pramMap);
            for (Map<String, Object> map : list)
            {
                map.put("add_date", DateUtil.dateFormate(MapUtils.getString(map, "add_date"), GloabConst.TimePattern.YMDHMS));
                String dType     = MapUtils.getString(map, "d_type");
                String dTypeDesc = dictionaryListModelMapper.getDictionaryText("分账接收方类型", dType);
                map.put("type", dType);
                map.put("typeDesc", dTypeDesc);
                String relationship     = MapUtils.getString(map, "relationship");
                String relationshipDesc = dictionaryListModelMapper.getDictionaryText("分账接收方关系", relationship);
                map.put("relationshipDesc", relationshipDesc);
            }
            resultMap.put("list", list);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取店铺分账信息 异常 ", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "divideAccountInfo");
        }
        return resultMap;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveDivideAccount(DivideAccountVo vo) throws LaiKeAPIException
    {
        try
        {
            AdminModel user = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);

            //根据store_id查询商城的mch_id和appid
            String paymentJson = URLDecoder.decode(paymentConfigModelMapper.getPaymentConfigInfo(vo.getStoreId(), DictionaryConst.OrderPayType.ORDERPAYTYPE_MINI_WECHAT), GloabConst.Chartset.UTF_8);

            JSONObject payJson = JSONObject.parseObject(paymentJson);
            String     mchId   = payJson.getString("mch_id");
            logger.debug("mchID:{}", mchId);
            String appId = payJson.getString("appid");
            logger.debug("appID:{}", appId);
            String keyPem = payJson.getString("key_pem");
            logger.debug("key_pem:{}", keyPem);
            String mch_key = payJson.getString("mch_key");
            logger.debug("mch_key:{}", mch_key);
            String sign_type = "HMAC-SHA256";
            if (Objects.isNull(vo.getMchId()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "请传入店铺唯一标识", "saveDivideAccount");
            }
//            if (StringUtils.isEmpty(vo.getSubMchId())) {
//                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "请输入子商户号", "saveDivideAccount");
//            }
//            if (StringUtils.isEmpty(vo.getSubAppId())) {
//                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "请输入子商户应用id", "saveDivideAccount");
//            }
            MchModel mchModel = mchModelMapper.selectByPrimaryKey(vo.getMchId());
            if (mchModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_MBBCZ, "店铺信息不存在", "divideAccountInfo");
            }
//            mchModel.setSub_mch_id(vo.getSubMchId());
//            mchModel.setSub_app_id(vo.getSubAppId());
//            mchModelMapper.updateByPrimaryKeySelective(mchModel);
            //清理原有的分账信息 调用微信的删除分账接收方的接口
            //查询出这个商户下的所有分账账户信息
            MchDistributionModel distributionModel = new MchDistributionModel();
            distributionModel.setMch_id(mchModel.getId());
            List<MchDistributionModel> list = mchDistributionModelMapper.select(distributionModel);
            //如果查询出来没有数据就不需要调用微信的删除分账接收方的接口
            if (list.size() > 0)
            {
                for (MchDistributionModel mchDistributionModel : list)
                {
                    //随机生成32位数的字符串
                    String        nonce_str = WXPayUtil.generateNonceStr();
                    StringBuilder receiver  = new StringBuilder();

                    receiver.append("{");
                    receiver.append("\"type\": \"").append(mchDistributionModel.getD_type()).append("\",");
                    receiver.append("\"account\": \"").append(mchDistributionModel.getAccount()).append("\"");
                    receiver.append("}");

                    //获取签名
                    Map<String, String> queryMap = Maps.newHashMap();
                    queryMap.put("appid", appId);
                    queryMap.put("mch_id", mchId);
                    queryMap.put("nonce_str", nonce_str);
                    queryMap.put("receiver", receiver.toString());
                    queryMap.put("sign_type", sign_type);
                    String querySign = WXPayUtil.generateSignature(queryMap, mch_key, WXPayConstants.SignType.HMACSHA256);
                    logger.debug("querySign:{}", querySign);
                    StringBuilder sb = new StringBuilder();
                    sb.append("<xml>");
                    sb.append("<mch_id>").append(mchId).append("</mch_id>");
                    sb.append("<appid>").append(mchId).append("</appid>");
                    sb.append("<nonce_str>").append(nonce_str).append("</nonce_str>");
                    sb.append("<sign_type>").append(sign_type).append("</sign_type>");
                    sb.append("<receiver>");
                    sb.append(receiver);
                    sb.append("</receiver>");
                    sb.append("</xml>");

                    WXPay               wxPay         = new WXPay();
                    String              request       = wxPay.requestWithoutCert(WXPayConstants.PAY_PROFITSHARINGREMOVERECEIVER, nonce_str, sb.toString());
                    Map<String, String> resultBodyMap = XmlUtil.xmlToMap(request);
                    if (!MapUtils.getString(resultBodyMap, "return_code").equals("SUCCESS"))
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SCSB, "调用微信删除接收方接口失败", "saveDivideAccount");
                    }
                }
            }
            mchDistributionModelMapper.delByMchId(mchModel.getId());
            //需要保存的的数据
            List<MchDistributionModel> mchDistributionModelList = new ArrayList<>();
            //分账信息
            BigDecimal max   = new BigDecimal(100);
            BigDecimal total = BigDecimal.ZERO;
            if (StringUtils.isNotEmpty(vo.getDivideAccountInfo()))
            {
                List<DivideAccountInfoVo> divideAccountInfoVos = JSONArray.parseArray(vo.getDivideAccountInfo(), DivideAccountInfoVo.class);
                for (DivideAccountInfoVo divideAccountInfoVo : divideAccountInfoVos)
                {
                    if (StringUtils.isEmpty(divideAccountInfoVo.getType()) ||
                            StringUtils.isEmpty(divideAccountInfoVo.getAccount()) ||
                            StringUtils.isEmpty(divideAccountInfoVo.getRelationship()) ||
                            Objects.isNull(divideAccountInfoVo.getProportion())
                    )
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "请完善分账信息", "divideAccountInfo");
                    }
                    MchDistributionModel mchDistributionModel = new MchDistributionModel();
                    mchDistributionModel.setMch_id(mchModel.getId());
                    mchDistributionModel.setD_type(divideAccountInfoVo.getType());
                    mchDistributionModel.setAccount(divideAccountInfoVo.getAccount());
                    mchDistributionModel.setRelationship(divideAccountInfoVo.getRelationship());
                    mchDistributionModel.setProportion(divideAccountInfoVo.getProportion());
                    mchDistributionModel.setAdd_date(new Date());
                    if (divideAccountInfoVo.getType().equals(MchDistributionModel.Type.MERCHANT_ID))
                    {
                        if (StringUtils.isEmpty(divideAccountInfoVo.getName()))
                        {
                            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERRCODE_ZTWMERCHANTIDSNAMEBT, "状态为MERCHANT_ID时name必填", "divideAccountInfo");
                        }
                    }
                    mchDistributionModel.setName(divideAccountInfoVo.getName());
                    mchDistributionModelList.add(mchDistributionModel);
                    total = total.add(divideAccountInfoVo.getProportion());
                }
            }
            if (total.compareTo(max) > 0)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "分账信息比例总和不得超过百分百", "divideAccountInfo");
            }
            //调用第三方接口补全分账信息
            //拼接字符串,循环调用
            for (MchDistributionModel mchDistributionModel : mchDistributionModelList)
            {
                //随机生成32位数的字符串
                String        nonce_str = WXPayUtil.generateNonceStr();
                StringBuilder receiver  = new StringBuilder();

                //拼接receiver
                receiver.append("{");
                receiver.append("\"type\": \"").append(mchDistributionModel.getD_type()).append("\",");
                receiver.append("\"account\": \"").append(mchDistributionModel.getAccount()).append("\",");
                if (mchDistributionModel.getD_type().equals(MchDistributionModel.Type.MERCHANT_ID))
                {
                    receiver.append("\"name\": \"").append(mchDistributionModel.getName()).append("\",");
                }
                else
                {
                    receiver.append("\"name\": \"").append("").append("\",");
                }
                receiver.append("\"relation_type\": \"").append(mchDistributionModel.getRelationship()).append("\"");
                receiver.append("}");

                //获取签名
                Map<String, String> queryMap = Maps.newHashMap();
                queryMap.put("appid", appId);
                queryMap.put("mch_id", mchId);
                queryMap.put("nonce_str", nonce_str);
                queryMap.put("receiver", receiver.toString());
                queryMap.put("sign_type", sign_type);
                String querySign = WXPayUtil.generateSignature(queryMap, mch_key, WXPayConstants.SignType.HMACSHA256);
                logger.debug("querySign:{}", querySign);
                StringBuilder sb = new StringBuilder();
                sb.append("<xml>");
                sb.append("<mch_id>").append(mchId).append("</mch_id>");
                sb.append("<appid>").append(mchId).append("</appid>");
                sb.append("<nonce_str>").append(nonce_str).append("</nonce_str>");
                sb.append("<sign_type>").append(sign_type).append("</sign_type>");
                sb.append("<receiver>");
                sb.append(receiver);
                sb.append("</receiver>");
                sb.append("</xml>");

                WXPay               wxPay         = new WXPay();
                String              request       = wxPay.requestWithoutCert(WXPayConstants.PAY_PROFITSHARINGADDRECEIVER, nonce_str, sb.toString());
                Map<String, String> resultBodyMap = XmlUtil.xmlToMap(request);
                if (!MapUtils.getString(resultBodyMap, "return_code").equals("SUCCESS"))
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TJSB, "调用微信添加接收方接口失败", "saveDivideAccount");
                }
            }
            if (mchDistributionModelList.size() > 0)
            {
                mchDistributionModelMapper.insertList(mchDistributionModelList);
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("保存店铺分账信息 异常 ", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "saveDivideAccount");
        }
    }

    @Override
    public Map<String, Object> applyBilling(MainVo vo, String date) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
//            AdminModel user = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            //获取服务商商户id和服务商应用id
            String miniWechatConfig  = paymentConfigModelMapper.getPaymentConfigInfo(vo.getStoreId(), "mini_wechat");
            if (StringUtils.isEmpty(miniWechatConfig))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_XCXWPZ,"小程序未配置");
            }
            String paymentJson = URLDecoder.decode(miniWechatConfig, GloabConst.Chartset.UTF_8);
            logger.info("mini_wechat支付配置信息：" + paymentJson);
            JSONObject payJson = JSONObject.parseObject(paymentJson);
            String     mchId   = payJson.getString("mch_id");
            logger.info("mchID:{}", mchId);
            String appId = payJson.getString("appid");
            logger.info("appID:{}", appId);
            String keyPem = payJson.getString("key_pem");
            logger.info("key_pem:{}", keyPem);
            CloseableHttpClient httpClient = WXPaySignatureCertificateUtil.checkSign(keyPem, mchId, WxV3PayConfig.mchSerialNo);
            date = date + " 00:00:00";
            logger.info("正在申请{}的账单", DateUtil.dateFormate(date, GloabConst.TimePattern.YMD));
            String     url        = String.format("https://api.mch.weixin.qq.com/v3/profitsharing/bills?bill_date=%s", DateUtil.dateFormate(date, GloabConst.TimePattern.YMD));
            URIBuilder uriBuilder = new URIBuilder(url);
            HttpGet    httpGet    = new HttpGet(uriBuilder.build());
            httpGet.addHeader("Accept", "application/json");
            httpGet.addHeader("Content-type", "application/json; charset=utf-8");
            CloseableHttpResponse httpResponse = httpClient.execute(httpGet);
            HttpEntity            entity       = httpResponse.getEntity();
            if (entity != null)
            {
                System.out.println(entity.getContent());
                String jsonResult = EntityUtils.toString(entity);
                System.out.println(jsonResult);
                Map map = JSONObject.parseObject(jsonResult, Map.class);
                if (map.containsKey("code"))
                {
                    throw new LaiKeAPIException(map.get("code").toString(), map.get("message").toString(), "applyBilling");
                }
                resultMap = map;
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("申请分账账单 异常 ", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "applyBilling");
        }
        return resultMap;
    }

    @Override
    public String downloadBilling(MainVo vo, String url, HttpServletResponse httpServletResponse) throws LaiKeAPIException
    {
        try
        {
//            AdminModel user = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            //获取服务商商户id和服务商应用id
            String paymentJson = URLDecoder.decode(paymentConfigModelMapper.getPaymentConfigInfo(vo.getStoreId(), "mini_wechat"), GloabConst.Chartset.UTF_8);
            logger.info("mini_wechat支付配置信息：" + paymentJson);
            JSONObject payJson = JSONObject.parseObject(paymentJson);
            String     mchId   = payJson.getString("mch_id");
            logger.info("mchID:{}", mchId);
            String appId = payJson.getString("appid");
            logger.info("appID:{}", appId);
            String keyPem = payJson.getString("key_pem");
            logger.info("key_pem:{}", keyPem);
//            String certPath = "C:/Users/Administrator/Desktop/WeChatPay/sfs/apiclient_cert.p12";
//            String certPath = payJson.getString("cert_p12");
//            logger.info("certPath:{}", certPath);
            String timestamp = System.currentTimeMillis() + "";
            String nonceStr  = UUID.randomUUID().toString().replace("-", "");
            String billSign  = this.createBillSign(nonceStr, timestamp, url, mchId, WxV3PayConfig.mchSerialNo, keyPem);
            url = url + "&mchid=" + mchId + "&nonce_str=" + nonceStr + "&signature=" + billSign + "&timestamp=" + timestamp + "&serial_no=" + WxV3PayConfig.mchSerialNo;
            PrivateKey merchantPrivateKey = PemUtil.loadPrivateKey(
                    new ByteArrayInputStream(keyPem.getBytes("utf-8")));
//            X509Certificate wechatpayCertificate = PemUtil.loadCertificate(
//                    new ByteArrayInputStream(certPath.getBytes("utf-8")));
//            ArrayList<X509Certificate> listCertificates = new ArrayList<>();
//            listCertificates.add(wechatpayCertificate);
            CloseableHttpClient httpClient = WechatPayHttpClientBuilder.create()
                    .withMerchant(mchId, WxV3PayConfig.mchSerialNo, merchantPrivateKey)
//                    .withWechatpay(listCertificates)
                    .withValidator(response -> true)
                    .build();
//            CloseableHttpClient httpClient = WXPaySignatureCertificateUtil.checkSign(keyPem, mchId, WxV3PayConfig.mchSerialNo);
            URIBuilder uriBuilder = new URIBuilder(url);
            HttpGet    httpGet    = new HttpGet(uriBuilder.build());
            httpGet.addHeader("Accept", "application/json");
            httpGet.addHeader("Content-type", "application/json; charset=utf-8");

            CloseableHttpResponse httpResponse = httpClient.execute(httpGet);
            HttpEntity            entity       = httpResponse.getEntity();
            System.out.println(entity);
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode == 200)
            {
                String   collect = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent())).lines().collect(Collectors.joining(""));
                String   s       = EntityUtils.toString(httpResponse.getEntity());
                String[] rows    = s.split("\n");
                //表头
                String[] keywords = rows[0].split(",");
                //对应字段
                String[]     kayList = new String[]{"time", "initiate_mch_id", "sub_mch_id", "order_no", "out_trade_no", "detail_no", "refund_no", "order_price", "account", "price", "type", "status", "desc", "remark"};
                List<String> strings = Arrays.stream(rows).collect(Collectors.toList());
                List<String> datas   = strings.subList(1, (strings.size() - 2));
                List<Map<String, Object>> maps = datas.stream().map(i ->
                {
                    Map<String, Object> map   = new HashMap<>();
                    String[]            split = i.split(",");
                    map.put("time", split[0]);
                    map.put("initiate_mch_id", split[1]);
                    map.put("sub_mch_id", split[2]);
                    map.put("order_no", split[3]);
                    map.put("out_trade_no", split[4]);
                    map.put("detail_no", split[5]);
                    map.put("refund_no", split[6]);
                    map.put("order_price", split[7]);
                    map.put("account", split[8]);
                    map.put("price", split[9]);
                    map.put("type", split[10]);
                    map.put("status", split[11]);
                    map.put("desc", split[12]);
                    map.put("remark", split[13]);
                    return map;
                }).collect(Collectors.toList());
                vo.setExportType(1);
                ExcelParamVo excelParamVo = new ExcelParamVo();
                excelParamVo.setTitle("分账列表列表");
                excelParamVo.setHeaderList(keywords);
                excelParamVo.setValueList(kayList);
                excelParamVo.setList(maps);
                excelParamVo.setResponse(httpServletResponse);
                EasyPoiExcelUtil.excelExport(excelParamVo);
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("下载分账账单 异常 ", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "applyBilling");
        }
        return null;
    }

    @Override
    public Map<String, Object> divideRecord(MainVo vo, Integer mchId, String condition, String startDate, String endDate, HttpServletResponse response) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            //查询服务商id
            //查询服务商id
            String mini_wechat = paymentConfigModelMapper.getPaymentConfigInfo(vo.getStoreId(), "mini_wechat");
            if (StringUtils.isEmpty(mini_wechat))
            {
                resultMap.put("total", 0);
                List<Map<String, Object>> list = new ArrayList<>();
                resultMap.put("list", list);
                if (vo.getExportType().equals(1))
                {
                    exportRecordReportData(list, response);
                    return null;
                }
                return resultMap;
            }
            String     paymentJson  = URLDecoder.decode(mini_wechat, GloabConst.Chartset.UTF_8);
            JSONObject payJson      = JSONObject.parseObject(paymentJson);
            String     serviceMchId = payJson.getString("mch_id");
            logger.info("mchID:{}", mchId);
            if (mchId != null)
            {
                User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);
            }
            else
            {
                AdminModel user = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            }
            Map<String, Object> paramMap = new HashMap<>(16);
            paramMap.put("storeId", vo.getStoreId());
            paramMap.put("add_date_sort", DataUtils.Sort.ASC.toString());
            paramMap.put("pageStart", vo.getPageNo());
            paramMap.put("pageEnd", vo.getPageSize());
            if (!Objects.isNull(mchId))
            {
                paramMap.put("mchId", mchId);
                paramMap.put("serviceMchId", serviceMchId);
            }
            if (StringUtils.isNotEmpty(condition))
            {
                paramMap.put("condition", condition);
            }
            if (StringUtils.isNotEmpty(startDate))
            {
                paramMap.put("startDate", startDate);
            }
            if (StringUtils.isNotEmpty(endDate))
            {
                paramMap.put("endDate", endDate);
            }
            List<Map<String, Object>> list = new ArrayList<>();
            int                       i    = mchDistributionRecordModelMapper.countDynamic(paramMap);
            if (i > 0)
            {
                list = mchDistributionRecordModelMapper.selectDynamic(paramMap);
                for (Map<String, Object> map : list)
                {
                    map.put("add_date", DateUtil.dateFormate(MapUtils.getString(map, "add_date"), GloabConst.TimePattern.YMDHMS));
                    String     orderNo          = MapUtils.getString(map, "order_no");
                    BigDecimal returnAmtByOrder = returnOrderModelMapper.getReturnAmtByOrder(orderNo);
                    map.put("refund_price", returnAmtByOrder);
                }
            }
            resultMap.put("total", i);
            resultMap.put("list", list);
            if (vo.getExportType().equals(1))
            {
                exportRecordReportData(list, response);
                return null;
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("分账记录 异常 ", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "divideRecord");
        }
        return resultMap;
    }

    public String createBillSign(String nonceStr, String timestamp, String download, String mchId, String mchSerialNo, String privateKey) throws Exception
    {

        String token      = download.split("token=")[1];
        String plain_text = mchId + "\n" + nonceStr + "\n" + mchSerialNo + "\n" + timestamp + "\n" + token;
        PrivateKey privateKey1 = PemUtil.loadPrivateKey(
                new ByteArrayInputStream(privateKey.getBytes("utf-8")));
        Signature sign = Signature.getInstance("SHA256withRSA");
        sign.initSign(privateKey1);
        sign.update(plain_text.getBytes("utf-8"));

        return Base64.getEncoder().encodeToString(sign.sign());

    }

    private void exportRecordReportData(List<Map<String, Object>> list, HttpServletResponse response) throws LaiKeAPIException
    {
        try
        {
            //表头
            String[] headerList = new String[]{"订单编号", "订单金额", "退单金额", "分账对象", "分账金额", "运费", "店铺名称", "处理时间"};
            //对应字段
            String[]     kayList = new String[]{"order_no", "z_price", "refund_price", "account", "amount", "z_freight", "name", "add_date"};
            ExcelParamVo vo      = new ExcelParamVo();
            vo.setTitle("分账记录报表列表");
            vo.setHeaderList(headerList);
            vo.setValueList(kayList);
            vo.setList(list);
            vo.setResponse(response);
            EasyPoiExcelUtil.excelExport(vo);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("导出商品数据 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "exportGoodsData");
        }
    }
}
