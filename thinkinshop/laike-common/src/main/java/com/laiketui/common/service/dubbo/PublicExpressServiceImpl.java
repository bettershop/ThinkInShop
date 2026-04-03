package com.laiketui.common.service.dubbo;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import com.laiketui.common.api.PublicExpressService;
import com.laiketui.common.api.PubliceService;
import com.laiketui.common.mapper.*;
import com.laiketui.common.utils.tool.data.GoodsDataUtils;
import com.laiketui.core.cache.RedisUtil;
import com.laiketui.core.common.SplitUtils;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.DictionaryConst;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.core.utils.okhttp.HttpUtils;
import com.laiketui.core.utils.tool.DataUtils;
import com.laiketui.core.utils.tool.DateUtil;
import com.laiketui.core.utils.tool.StringUtils;
import com.laiketui.domain.config.ExpressModel;
import com.laiketui.domain.config.ExpressSubtableModel;
import com.laiketui.domain.log.AdminRecordModel;
import com.laiketui.domain.mch.MchModel;
import com.laiketui.domain.order.ExpressDeliveryModel;
import com.laiketui.domain.order.OrderModel;
import com.laiketui.domain.user.User;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.config.AddExpressSubtableVo;
import com.laiketui.domain.vo.config.GetExpressSubtableListVo;
import com.laiketui.domain.vo.order.GetExpressDeliveryListVo;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLDecoder;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 物流公司信息服务接口
 * return
 *
 * @author wangxian
 */
@Service("publicExpressService")
public class PublicExpressServiceImpl implements PublicExpressService
{

    private final Logger logger = LoggerFactory.getLogger(PublicExpressServiceImpl.class);

    @Override
    public List<ExpressModel> getExpressInfo() throws LaiKeAPIException
    {
        try
        {
            List<ExpressModel> cacheExpress = (List<ExpressModel>) redisUtil.get(GloabConst.RedisHeaderKey.EXPRESS_ALL);
            if (cacheExpress != null)
            {
                return cacheExpress;
            }
            return expressModelMapper.selectAll();
        }
        catch (Exception e)
        {
            logger.error("获取物流公司信息失败", e);
        }

        return null;
    }

    @Override
    public Map<String, Object> getExpressInfoByMchId(MainVo vo, Integer mchId) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("mchId", mchId);
            parmaMap.put("is_open", DictionaryConst.WhetherMaven.WHETHER_OK);
            parmaMap.put("storeId", vo.getStoreId());
            List<Map<String, Object>> list = expressModelMapper.selectDynamic(parmaMap);

            resultMap.put("list", list);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("物流公司列表 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "index");
        }
        return resultMap;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addAndUpdateExpressSubtable(AddExpressSubtableVo vo) throws LaiKeAPIException
    {
        try
        {
            Integer express_id = vo.getExpress_id();
            if (StringUtils.isEmpty(express_id))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误");
            }
            ExpressModel expressModel = expressModelMapper.selectByPrimaryKey(express_id);
            if (expressModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLBCZ, "物流不存在");
            }
            if (StringUtils.isEmpty(vo.getMch_id()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误");
            }
            if (StringUtils.isEmpty(vo.getPartnerId()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_PARTHER, "partnerId不能为空");
            }
            ExpressSubtableModel expressSubtableOdl = null;
            if (!StringUtils.isEmpty(vo.getId()))
            {
                expressSubtableOdl = expressSubtableModelMapper.selectByPrimaryKey(vo.getId());
                if (expressSubtableOdl == null)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLBCZ, "物流不存在");
                }
            }
            if (!(expressSubtableOdl != null && expressSubtableOdl.getExpressId().equals(express_id)) &&
                    expressSubtableModelMapper.countExpressSubtableByExpressId(vo.getStoreId(), express_id, vo.getMch_id()) > 0)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_GWLGSYPZDZMD, "该物流公司已配置电子面单");
            }
            int                  lab                  = 0;
            ExpressSubtableModel expressSubtableModel = new ExpressSubtableModel();
            expressSubtableModel.setStoreId(vo.getStoreId());
            expressSubtableModel.setMchId(vo.getMch_id());
            expressSubtableModel.setExpressId(express_id);
            expressSubtableModel.setPartnerId(vo.getPartnerId());
            expressSubtableModel.setPartnerKey(vo.getPartnerKey());
            expressSubtableModel.setPartnerSecret(vo.getPartnerSecret());
            expressSubtableModel.setPartnerName(vo.getPartnerName());
            expressSubtableModel.setExpressNet(vo.getNet());
            expressSubtableModel.setExpressCode(vo.getCode());
            expressSubtableModel.setCheckMan(vo.getCheckMan());
            expressSubtableModel.setTemp_id(vo.getTemp_id());
            if (expressSubtableOdl != null)
            {
                expressSubtableModel.setId(expressSubtableOdl.getId());
                lab = expressSubtableModelMapper.updateByPrimaryKeySelective(expressSubtableModel);

                publiceService.addAdminRecord(vo.getStoreId(), "编辑了面单ID：" + vo.getId() + " 的信息", AdminRecordModel.Type.UPDATE, vo.getAccessId());
            }
            else
            {
                expressSubtableModel.setAddTime(new Date());
                lab = expressSubtableModelMapper.insertSelective(expressSubtableModel);
                publiceService.addAdminRecord(vo.getStoreId(), "添加了面单ID：" + expressSubtableModel.getId(), AdminRecordModel.Type.ADD, vo.getAccessId());
            }
            if (lab <= 0)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CZSB, "操作失败");
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("添加修改快递公司子表 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "addAndUpdateExpressSubtable");
        }
    }


    @Override
    public Map<String, Object> getExpressSubtableList(GetExpressSubtableListVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            if (StringUtils.isEmpty(vo.getMch_id()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误");
            }
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("mchId", vo.getMch_id());
            parmaMap.put("storeId", vo.getStoreId());
            parmaMap.put("name", vo.getName());
            parmaMap.put("pageStart", vo.getPageNo());
            parmaMap.put("pageEnd", vo.getPageSize());
            int                       total = expressSubtableModelMapper.countDynamic(parmaMap);
            List<Map<String, Object>> list  = new ArrayList<>();
            if (total > 0)
            {
                list = expressSubtableModelMapper.selectDynamic(parmaMap);
                list.forEach(map ->
                {
                    map.put("add_time", DateUtil.dateFormate(MapUtils.getString(map, "add_time"), GloabConst.TimePattern.YMDHMS));
                });
            }
            resultMap.put("total", total);
            resultMap.put("list", list);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取快递公司子表列表 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getExpressSubtableList");
        }
        return resultMap;
    }


    @Override
    public Map<String, Object> getExpressSubtableById(MainVo vo, Integer id) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            if (StringUtils.isEmpty(id))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误");
            }
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("id", id);
            int                       total = expressSubtableModelMapper.countDynamic(parmaMap);
            List<Map<String, Object>> list;
            if (total > 0)
            {
                list = expressSubtableModelMapper.selectDynamic(parmaMap);
                list.forEach(map ->
                {
                    map.put("add_time", DateUtil.dateFormate(MapUtils.getString(map, "add_time"), GloabConst.TimePattern.YMDHMS));
                });
            }
            else
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLBCZ, "物流不存在");
            }
            resultMap.put("list", list.get(0));
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取快递公司子表列表 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getExpressSubtableList");
        }
        return resultMap;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delExpressSubtableById(MainVo vo, Integer id) throws LaiKeAPIException
    {
        try
        {
            if (StringUtils.isEmpty(id))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误");
            }
            ExpressSubtableModel expressSubtableModel = expressSubtableModelMapper.selectByPrimaryKey(id);
            if (expressSubtableModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLBCZ, "物流不存在");
            }
            expressSubtableModel.setRecovery(DictionaryConst.WhetherMaven.WHETHER_OK);
            if (expressSubtableModelMapper.updateByPrimaryKeySelective(expressSubtableModel) <= 0)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CZSB, "操作失败");
            }
            publiceService.addAdminRecord(vo.getStoreId(), "删除了面单ID：" + id + " 的信息", AdminRecordModel.Type.DEL, vo.getAccessId());
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取快递公司子表列表 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getExpressSubtableList");
        }
    }

    @Override
    public Map<String, Object> getExpressDeliveryList(GetExpressDeliveryListVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", vo.getStoreId());
            if (StringUtils.isNotEmpty(vo.getExpressName()))
            {
                parmaMap.put("expressName", vo.getExpressName());
            }
            if (StringUtils.isNotEmpty(vo.getsNo()))
            {
                parmaMap.put("sNo", vo.getsNo());
            }
            if (StringUtils.isNotEmpty(vo.getSearch()))
            {
                parmaMap.put("search", vo.getSearch());
            }
            if (StringUtils.isNotEmpty(vo.getMch_name()))
            {
                parmaMap.put("mch_name", vo.getMch_name());
            }
            if (StringUtils.isNotEmpty(vo.getStatus()))
            {
                parmaMap.put("status", vo.getStatus());
            }
            if (StringUtils.isNotEmpty(vo.getStartDate()))
            {
                parmaMap.put("startDate", vo.getStartDate());
            }
            if (StringUtils.isNotEmpty(vo.getEndDate()))
            {
                parmaMap.put("endDate", vo.getEndDate());
            }
            if (StringUtils.isNotEmpty(vo.getMch_id()))
            {
                parmaMap.put("mch_id", vo.getMch_id());
            }
            parmaMap.put("pageStart", vo.getPageNo());
            parmaMap.put("pageEnd", vo.getPageSize());
            parmaMap.put("subtable_id", "subtable_id");
            parmaMap.put("ByCourierNum", "ByCourierNum");
            int                       total = expressDeliveryModelMapper.countDynamic(parmaMap);
            List<Map<String, Object>> list  = new ArrayList<>();
            if (total > 0)
            {
                list = expressDeliveryModelMapper.selectDynamic(parmaMap);
                list.forEach(map ->
                {
                    map.put("deliver_time", DateUtil.dateFormate(MapUtils.getString(map, "deliver_time"), GloabConst.TimePattern.YMDHMS));
                });
            }
            resultMap.put("total", total);
            resultMap.put("list", list);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取发货记录列表 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getExpressDeliveryList");
        }
        return resultMap;
    }

    @Override
    public boolean getMchHaveExpressSubtableByMchId(MainVo vo, Integer mchId) throws LaiKeAPIException
    {
        try
        {
            if (StringUtils.isEmpty(mchId))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误");
            }
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", vo.getStoreId());
            parmaMap.put("mchId", mchId);
            if (expressSubtableModelMapper.countDynamic(parmaMap) > 0)
            {
                return true;
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("判断店铺是否有电子面单配置 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getMchHaveExpressSubtableByMchId");
        }
        return false;
    }

    @Override
    public Map<String, Object> getExpressInfoBySNo(MainVo vo, String sNo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            if (StringUtils.isEmpty(sNo))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误");
            }
            //获取订单信息
            OrderModel orderModel = new OrderModel();
            orderModel.setsNo(sNo);
            orderModel.setStore_id(vo.getStoreId());
            orderModel = orderModelMapper.selectOne(orderModel);
            if (orderModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DDBCZ, "订单不存在");
            }
            String mchId = StringUtils.trim(orderModel.getMch_id(), SplitUtils.DH);
            if (StringUtils.isEmpty(mchId))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SJCW, "数据错误");
            }
            int                       total    = 0;
            List<Map<String, Object>> list     = new ArrayList<>();
            Map<String, Object>       parmaMap = new HashMap<>(16);
            parmaMap.put("is_open", DictionaryConst.WhetherMaven.WHETHER_OK);
            parmaMap.put("sort_sort", DataUtils.Sort.DESC.toString());
            total = expressModelMapper.countDynamic(parmaMap);
            if (total > 0)
            {
                list = expressModelMapper.selectDynamic(parmaMap);
            }
            parmaMap = new HashMap<>(16);
            parmaMap.put("storeId", vo.getStoreId());
            parmaMap.put("mchId", mchId);
            for (Map<String, Object> map : list)
            {
                Integer id = MapUtils.getInteger(map, "id");
                parmaMap.put("express_id", id);
                if (expressSubtableModelMapper.countExpressByexpressSubtable(parmaMap) > 0)
                {
                    map.put("logistics_type", true);
                }
                else
                {
                    map.put("logistics_type", false);
                }
            }
            resultMap.put("total", total);
            resultMap.put("list", list);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取物流公司列表 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getExpressInfoBySNo");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> getExpressInfo(MainVo vo, Integer mchId, boolean needFilter, Integer eid) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            if (StringUtils.isEmpty(mchId))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SJCW, "数据错误");
            }
            int                       total    = 0;
            List<Map<String, Object>> list     = new ArrayList<>();
            Map<String, Object>       parmaMap = new HashMap<>(16);
            parmaMap.put("is_open", DictionaryConst.WhetherMaven.WHETHER_OK);
            parmaMap.put("sort_sort", DataUtils.Sort.DESC.toString());
            total = expressModelMapper.countDynamic(parmaMap);
            if (total > 0)
            {
                list = expressModelMapper.selectDynamic(parmaMap);
            }
            parmaMap = new HashMap<>(16);
            parmaMap.put("storeId", vo.getStoreId());
            parmaMap.put("mchId", mchId);
            for (Map<String, Object> map : list)
            {
                Integer id = MapUtils.getInteger(map, "id");
                parmaMap.put("express_id", id);
                if (expressSubtableModelMapper.countExpressByexpressSubtable(parmaMap) > 0)
                {
                    map.put("logistics_type", true);
                }
                else
                {
                    map.put("logistics_type", false);
                }
            }
            if (needFilter)
            {
                //eid为当前编辑的物流公司id
                if (eid != null)
                {
                    list = list.stream().filter(map -> (!MapUtils.getBoolean(map, "logistics_type") || DataUtils.getIntegerVal(map, "id").equals(eid))).collect(Collectors.toList());
                }
                else
                {
                    list = list.stream().filter(map -> !MapUtils.getBoolean(map, "logistics_type")).collect(Collectors.toList());
                }
            }
            resultMap.put("total", total);
            resultMap.put("list", list);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取物流公司列表 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getExpressInfoBySNo");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> getGoodsByExpressDeliveryId(MainVo vo, Integer id, String name) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            if (StringUtils.isEmpty(id))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误");
            }
            ExpressDeliveryModel expressDeliveryModel = expressDeliveryModelMapper.selectByPrimaryKey(id);
            if (expressDeliveryModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLBCZ, "物流不存在");
            }

            int                       total    = 0;
            List<Map<String, Object>> list     = new ArrayList<>();
            Map<String, Object>       parmaMap = new HashMap<>(16);
            if (StringUtils.isNotEmpty(name))
            {
                parmaMap.put("name", name);
            }
            parmaMap.put("store_id", vo.getStoreId());
            parmaMap.put("expressId", expressDeliveryModel.getExpressId());
            parmaMap.put("courierNum", expressDeliveryModel.getCourierNum());
            parmaMap.put("pageStart", vo.getPageNo());
            parmaMap.put("pageEnd", vo.getPageSize());
            total = expressDeliveryModelMapper.countGoodsByExpressDeliveryId(parmaMap);
            if (total > 0)
            {
                list = expressDeliveryModelMapper.getGoodsByExpressDeliveryId(parmaMap);
                list.forEach(map ->
                {
                    map.put("img", publiceService.getImgPath(MapUtils.getString(map, "img"), vo.getStoreId()));
                    //规格字符串
                    map.put("attribute", GoodsDataUtils.getProductSkuValue(MapUtils.getString(map, "attribute")));
                });
            }

            resultMap.put("total", total);
            resultMap.put("list", list);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("查看发货记录商品 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getGoodsByExpressDeliveryId");
        }
        return resultMap;
    }

    @Override
    public boolean setWxAppUploadShippingInfo(MainVo vo, String sNo) throws LaiKeAPIException
    {
        try
        {
            if (StringUtils.isEmpty(sNo))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误", "setWxAppUploadShippingInfo");
            }
            OrderModel orderModel = new OrderModel();
            orderModel.setStore_id(vo.getStoreId());
            orderModel.setsNo(sNo);
            orderModel = orderModelMapper.selectOne(orderModel);
            //获取用户消息
            User user = new User();
            user.setStore_id(vo.getStoreId());
            user.setUser_id(orderModel.getUser_id());
            user = userMapper.selectOne(user);
            //获取微信小程序支付配置
            String paymentJson = paymentConfigModelMapper.getPaymentConfigInfo(vo.getStoreId(), DictionaryConst.OrderPayType.ORDERPAYTYPE_MINI_WECHAT);
            paymentJson = URLDecoder.decode(paymentJson, GloabConst.Chartset.UTF_8);
            logger.info("小程序支付配置信息：" + paymentJson);
            JSONObject payJson = JSONObject.parseObject(paymentJson);
            String     mchID   = payJson.getString("mch_id");
            logger.info("mchID:{}", mchID);
            String appid = payJson.getString("appid");
            logger.info("appid:{}", appid);
            String Real_sno = null;
            //是否拆单，拆单使用的是同一个微信订单号 进行分拆发货
            //判断子订单是否全部发货完成/或者未发货的在售后中
            OrderModel sonOrderModel = new OrderModel();
            sonOrderModel.setStore_id(vo.getStoreId());
            sonOrderModel.setReal_sno(orderModel.getReal_sno());
            List<OrderModel> sonOrderModelList = orderModelMapper.select(sonOrderModel);
            for (OrderModel model : sonOrderModelList)
            {
                //未发货
                if (model.getStatus() <= DictionaryConst.OrdersStatus.ORDERS_R_STATUS_CONSIGNMENT)
                {
                    return true;
                }
            }
            Real_sno = orderModel.getReal_sno();

            //修改订单的微信订单状态为以发货
            orderModelMapper.UpdateWxOrderStatusByRealSno(vo.getStoreId(), orderModel.getReal_sno(), 2);
            HashMap<String, Object> map = new HashMap<>();
            map.put("store_id", vo.getStoreId());
            if (Real_sno != null)
            {
                map.put("Real_sno", Real_sno);
            }
            else
            {
                map.put("sNo", sNo);
            }
            List<Map<String, Object>> orderDetailBypSnoOrsNo = orderDetailsModelMapper.getOrderDetailBypSnoOrsNo(map);
            //获取微信token
            String                  accessToken = publiceService.getWeiXinToken(vo.getStoreId());
            String                  url         = String.format(GloabConst.WeiXinUrl.UPLOAD_SHIPPING_INFO, accessToken);
            HashMap<String, Object> parmaMap    = new HashMap<>();
            Map<String, Object>     resultMapTemp;
            //获取全部物流信息
            List<Object>            resultList = new ArrayList<>(16);
            HashMap<String, Object> order_key  = new HashMap<>();
            //订单单号类型，用于确认需要上传详情的订单。枚举值1，使用下单商户号和商户侧单号；枚举值2，使用微信支付单号
            order_key.put("order_number_type", 1);
            //mchid:支付下单商户的商户号，由微信支付生成并下发。
            //out_trade_no 商户系统内部订单号，只能是数字、大小写字母`_-*`且在同一个商户号下唯一
            order_key.put("mchid", mchID);
            order_key.put("out_trade_no", orderModel.getReal_sno());
//            order_key.put("transaction_id", "4200002225202405178855537598");
            parmaMap.put("order_key", order_key);
            //logistics_type:物流模式，发货方式枚举值：1、实体物流配送采用快递公司进行实体物流配送形式 2、同城配送 3、虚拟商品，虚拟商品，例如话费充值，点卡等，无实体配送形式 4、用户自提
            //线下自提
            if (orderModel.getSelf_lifting().equals(OrderModel.SELF_LIFTING_PICKED_UP))
            {
                parmaMap.put("logistics_type", 4);
                parmaMap.put("delivery_mode", "UNIFIED_DELIVERY");
                resultMapTemp = new HashMap<>(16);
                StringBuilder goodsListTemp = new StringBuilder();
                for (Map<String, Object> orderDetails : orderDetailBypSnoOrsNo)
                {
                    goodsListTemp.append(MapUtils.getString(orderDetails, "p_name")).append("*").append(MapUtils.getInteger(orderDetails, "num")).append(",");
                }
                resultMapTemp.put("item_desc", goodsListTemp);
                resultList.add(resultMapTemp);
            }
            else
            {
                parmaMap.put("logistics_type", 1);
                ExpressDeliveryModel expressDeliveryModel = null;
                //物流去重,如果多个商品统一发货则只显示一个物流动态
                Set<String> wuLiuIds = new HashSet<>();
                //商品信息快递单号分组
                Map<String, Map<String, Object>> goodsGroupMap = new HashMap<>(16);
                //商品分组
                Map<String, StringBuilder> goodsGroupList = new HashMap<>(16);
                for (Map<String, Object> orderDetails : orderDetailBypSnoOrsNo)
                {
                    String  courier_num = MapUtils.getString(orderDetails, "courier_num");
                    String  express_id  = MapUtils.getString(orderDetails, "express_id");
                    Integer id          = MapUtils.getInteger(orderDetails, "id");
                    String  p_name      = MapUtils.getString(orderDetails, "p_name");
                    Integer num         = MapUtils.getInteger(orderDetails, "num");
                    resultMapTemp = new HashMap<>(16);
                    if (courier_num == null || express_id == null)
                    {
                        continue;
                    }
                    //快递公司id
                    List<String> expressIds = Arrays.asList(express_id.split(","));
                    //快递单号
                    List<String> courierNums = Arrays.asList(courier_num.split(","));
                    for (int i = 0; i < expressIds.size(); i++)
                    {
                        String goods = "";
                        resultMapTemp = new HashMap<>();
                        String expressId  = expressIds.get(i);
                        String courierNum = courierNums.get(i);
                        if (StringUtils.isEmpty(courierNum))
                        {
                            //剔除没有物流单号的商品
                            continue;
                        }
                        //快递公司代号
                        String kdCode   = "";
                        String wuLiuKey = expressId + courierNum;
                        //查询发货记录
                        expressDeliveryModel = new ExpressDeliveryModel();
                        expressDeliveryModel.setOrderDetailsId(id);
                        expressDeliveryModel.setCourierNum(courierNum);
                        expressDeliveryModel.setExpressId(Integer.valueOf(expressId));
                        expressDeliveryModel = expressDeliveryModelMapper.selectOne(expressDeliveryModel);

                        //获取商品信息
                        goods = p_name + "*" + expressDeliveryModel.getNum() + ",";
                        if (wuLiuIds.contains(wuLiuKey))
                        {
                            //获取同单物流信息
                            resultMapTemp = goodsGroupMap.get(courierNum);
                            StringBuilder goodsListTemp = goodsGroupList.get(courierNum);
                            goodsListTemp.append(goods);
                            resultMapTemp.put("item_desc", goodsListTemp);
                            continue;
                        }
                        //分组
                        if (goodsGroupMap.containsKey(courierNum))
                        {
                            //获取同单物流信息
                            resultMapTemp = goodsGroupMap.get(courierNum);
                            StringBuilder goodsListTemp = goodsGroupList.get(courierNum);
                            resultMapTemp.put("item_desc", goodsListTemp);
                        }
                        else
                        {
                            //商品信息根据物流单分组
                            StringBuilder goodsListTemp = new StringBuilder();
                            if (goodsGroupList.containsKey(courierNum))
                            {
                                goodsListTemp.append(goodsGroupList.get(courierNum));
                            }
                            else
                            {
                                goodsListTemp.append(goods);
                                goodsGroupList.put(courierNum, goodsListTemp);
                            }
                            ExpressModel expressModel = expressModelMapper.selectByPrimaryKey(expressId);
                            //构造数据结构
                            //商品信息，例如：微信红包抱枕*1个，限120个字以内
                            resultMapTemp.put("item_desc", goodsListTemp);
                            //物流单号，物流快递发货时必填，示例值: 323244567777 字符字节限制: [1, 128]
                            resultMapTemp.put("tracking_no", courierNum);
                            //物流公司编码，快递公司ID，参见「查询物流公司编码列表」，物流快递发货时必填， 示例值: DHL 字符字节限制: [1, 128]
                            resultMapTemp.put("express_company", expressModel.getWx_delivery_id());
                            HashMap<String, Object> contact = new HashMap<>();
                            String   mchId    = StringUtils.trim(orderModel.getMch_id(), SplitUtils.DH);
                            MchModel mchModel = mchModelMapper.selectByPrimaryKey(mchId);
                            if (orderModel.getMobile() != null)
                            {
                                //receiver_contact:收件人联系方式，收件人联系方式为，采用掩码传输，最后4位数字不能打掩码 示例值: `189****1234, 021-****1234, ****1234, 0**2-***1234, 0**2-******23-10, ****123-8008` 值限制: 0 ≤ value ≤ 1024
                                contact.put("receiver_contact", orderModel.getMobile().replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2"));
                            }
                            else
                            {
                                //寄件人联系方式，寄件人联系方式，采用掩码传输，最后4位数字不能打掩码 示例值: `189****1234, 021-****1234, ****1234, 0**2-***1234, 0**2-******23-10, ****123-8008` 值限制: 0 ≤ value ≤ 1024
                                // 店铺ID
                                String tel = mchModel.getTel();
                                if (tel != null)
                                {
                                    contact.put("consignor_contact", tel.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2"));
                                }
                                else
                                {
                                    contact.put("consignor_contact", "156****1234");
                                }
                            }
                            resultMapTemp.put("cpc", mchModel.getCpc());
                            //联系方式，当发货的物流公司为顺丰时，联系方式为必填，收件人或寄件人联系方式二选一
                            resultMapTemp.put("contact", contact);

                            goodsGroupMap.put(courierNum, resultMapTemp);
                        }
                        wuLiuIds.add(wuLiuKey);
                        resultList.add(resultMapTemp);
                    }
                }
                //delivery_mode:发货模式，发货模式枚举值：1、UNIFIED_DELIVERY（统一发货）2、SPLIT_DELIVERY（分拆发货） 示例值: UNIFIED_DELIVERY
                //is_all_delivered:分拆发货模式时必填，用于标识分拆发货模式下是否已全部发货完成，只有全部发货完成的情况下才会向用户推送发货完成通知。示例值: true/false
                if (resultList.size() <= 1)
                {
                    //一个发货记录  UNIFIED_DELIVERY（统一发货）
                    parmaMap.put("delivery_mode", "UNIFIED_DELIVERY");
                }
                else
                {
                    //多个发货记录  SPLIT_DELIVERY（分拆发货）
                    parmaMap.put("delivery_mode", "SPLIT_DELIVERY");
                    parmaMap.put("is_all_delivered", true);
                }
            }
            //	shipping_list : 物流信息列表，发货物流单列表，支持统一发货（单个物流单）和分拆发货（多个物流单）两种模式，多重性: [1, 10]
            if (resultList.size() > 10)
            {
                resultList = resultList.subList(0, 10);
            }
            parmaMap.put("shipping_list", resultList);
            //	upload_time	string上传时间，用于标识请求的先后顺序 示例值: `2022-12-15T13:29:35.120+08:00` RFC 3339格式时间
            // 获取当前时间的ZonedDateTime实例
            ZonedDateTime now = ZonedDateTime.now();
            // 使用DateTimeFormatter格式化为RFC 3339格式
            DateTimeFormatter formatter   = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
            String            rfc3339Time = now.format(formatter);
            parmaMap.put("upload_time", rfc3339Time);
            //payer	支付者，支付者信息
            HashMap<String, Object> payer = new HashMap<>();
            //用户标识，用户在小程序appid下的唯一标识。 下单前需获取到用户的Openid 示例值: oUpF8uMuAJO_M2pxb1Q9zNjWeS6o 字符字节限制: [1, 128]
            payer.put("openid", user.getWx_id());
            parmaMap.put("payer", payer);
            logger.info("发货信息录入接口:{}", JSON.toJSONString(parmaMap));
            String resultJson = HttpUtils.post(url, JSON.toJSONString(parmaMap));
            Map<String, Object> parma = JSON.parseObject(resultJson, new TypeReference<Map<String, Object>>()
            {
            });
            if (!MapUtils.getString(parma, "errmsg").equals("ok"))
            {
                logger.error("发货信息录入接口微信返回错误:" + resultJson);
                //删除失效的token，下次发货获取新的有效token
                redisUtil.del(GloabConst.RedisHeaderKey.WEIXIN_ACCESS_TOKEN + appid);
                //throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "setWxAppUploadShippingInfo");
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("发货信息录入接口 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "setWxAppUploadShippingInfo");
        }
        return true;
    }


    @Override
    public void synchronizationWxDelivery() throws LaiKeAPIException
    {
        try
        {
            // 获取默认商城id
            Integer defaultStoreId = customerModelMapper.getDefaultStoreId();
            //获取微信token
            String accessToken = publiceService.getWeiXinToken(defaultStoreId);
            if (accessToken == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QWSXTSZ, "请完善系统设置", "synchronizationWxDelivery");
            }
            String url        = String.format(GloabConst.WeiXinUrl.GET_DELIVERY_LIST, accessToken);
            String resultJson = HttpUtils.post(url);
            Map<String, Object> parma = JSON.parseObject(resultJson, new TypeReference<Map<String, Object>>()
            {
            });
            List<Map<String, Object>> list = JSON.parseObject(parma.get("delivery_list").toString(), new TypeReference<List<Map<String, Object>>>()
            {
            });
            //构造数据
            Map<String, String> collect = list.stream()
                    .collect(Collectors.toMap(
                            map -> MapUtils.getString(map, "delivery_name"),
                            map -> MapUtils.getString(map, "delivery_id"),
                            (existingValue, newValue) -> existingValue  // 保留现有值，不覆盖
                    ));
            ExpressModel expressModel = new ExpressModel();
            expressModel.setRecycle(DictionaryConst.WhetherMaven.WHETHER_NO);
            List<ExpressModel> expressModelList = expressModelMapper.select(expressModel);
            for (ExpressModel model : expressModelList)
            {
                String kuaidi_name    = model.getKuaidi_name();
                String wx_delivery_id = null;
                if (collect.containsKey(kuaidi_name))
                {
                    wx_delivery_id = MapUtils.getString(collect, kuaidi_name, "");
                }
                if (wx_delivery_id == null)
                {
                    //截取名称前两位重新查询
                    if (kuaidi_name.length() > 2)
                    {
                        kuaidi_name = kuaidi_name.substring(0, 2);
                    }
                }
                for (Map<String, Object> map : list)
                {
                    if (MapUtils.getString(map, "delivery_name").contains(kuaidi_name))
                    {
                        wx_delivery_id = (MapUtils.getString(map, "delivery_id", ""));
                    }
                }
                if (wx_delivery_id == null)
                {
                    wx_delivery_id = model.getType();
                }
                model.setWx_delivery_id(wx_delivery_id);
                expressModelMapper.updateByPrimaryKeySelective(model);
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("同步微信发货信息录入的物流公司信息 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "synchronizationWxDelivery");
        }
    }

    @Override
    public void synchronizationWxOrderStatus(Integer storeId) throws LaiKeAPIException
    {
        try
        {
            //获取微信token
            String accessToken = publiceService.getWeiXinToken(storeId);
            if (accessToken == null)
            {
                return;
            }
            //获取微信小程序支付配置
            String paymentJson = paymentConfigModelMapper.getPaymentConfigInfo(storeId, DictionaryConst.OrderPayType.ORDERPAYTYPE_MINI_WECHAT);
            if (paymentJson == null)
            {
                return;
            }
            paymentJson = URLDecoder.decode(paymentJson, GloabConst.Chartset.UTF_8);
            logger.info("小程序支付配置信息：" + paymentJson);
            JSONObject payJson = JSONObject.parseObject(paymentJson);
            String     mchID   = payJson.getString("mch_id");
            if (StringUtils.isEmpty(mchID))
            {
                return;
            }
            String                    url                             = String.format(GloabConst.WeiXinUrl.GET_ORDER_STATUS, accessToken);
            List<Map<String, Object>> realSnoByStatusAndWxOrderStatus = orderModelMapper.getRealSnoByStatusAndWxOrderStatus(storeId);
            Map<String, Object>       parma                           = null;
            Map<String, Object>       map                             = new HashMap<>();
            map.put("merchant_id", mchID);
            for (Map<String, Object> realSnoMap : realSnoByStatusAndWxOrderStatus)
            {
                String realSno = MapUtils.getString(realSnoMap, "real_sno");
                map.put("merchant_trade_no", realSno);
                String resultJson = HttpUtils.post(url, JSON.toJSONString(map));
                parma = JSON.parseObject(resultJson, new TypeReference<Map<String, Object>>()
                {
                });
                if (MapUtils.getInteger(parma, "errcode") != 0)
                {
                    logger.error("同步微信发货订单状态微信返回错误:" + resultJson);
                    continue;
                }
                logger.info("商户订单号：" + realSno);
                logger.info("同步微信发货订单状态微信返回:" + resultJson);
                parma = (Map<String, Object>) parma.get("order");
                Integer order_state = MapUtils.getInteger(parma, "order_state");
                //订单状态枚举：(1) 待发货；(2) 已发货；(3) 确认收货；(4) 交易完成；(5) 已退款。
                if (order_state >= 3)
                {
                    //修改微信订单状态
                    orderModelMapper.UpdateWxOrderStatusByRealSno(storeId, realSno, order_state);
                }
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("同步微信发货订单状态 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "synchronizationWxOrderStatus");
        }
    }

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private ExpressModelMapper expressModelMapper;

    @Autowired
    private ExpressSubtableModelMapper expressSubtableModelMapper;

    @Autowired
    private ExpressDeliveryModelMapper expressDeliveryModelMapper;

    @Autowired
    private OrderModelMapper orderModelMapper;

    @Autowired
    private PubliceService publiceService;

    @Autowired
    private OrderDetailsModelMapper orderDetailsModelMapper;

    @Autowired
    private MchModelMapper mchModelMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PaymentModelMapper paymentModelMapper;

    @Autowired
    private PaymentConfigModelMapper paymentConfigModelMapper;

    @Autowired
    private CustomerModelMapper customerModelMapper;

    @Autowired
    private ReturnOrderModelMapper returnOrderModelMapper;
}

