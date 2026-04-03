package com.laiketui.comps.invoice.services;


import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.laiketui.common.api.PubliceService;
import com.laiketui.common.mapper.*;
import com.laiketui.common.utils.tool.cache.RedisDataTool;
import com.laiketui.comps.api.invoice.CompsInvoiceInfoService;
import com.laiketui.core.cache.RedisUtil;
import com.laiketui.core.common.SplitUtils;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.DictionaryConst;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.core.utils.tool.DataUtils;
import com.laiketui.core.utils.tool.DateUtil;
import com.laiketui.core.utils.tool.StringUtils;
import com.laiketui.domain.invoice.InvoiceHeaderModel;
import com.laiketui.domain.invoice.InvoiceInfoModel;
import com.laiketui.domain.mch.MchModel;
import com.laiketui.domain.order.OrderDetailsModel;
import com.laiketui.domain.order.OrderModel;
import com.laiketui.domain.user.User;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.invoice.InvoiceInfoVo;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

/**
 * 会员制实现
 *
 * @author Trick
 * @date 2020/12/21 17:49
 */
@Service
public class CompsInvoiceInfoServiceImpl implements CompsInvoiceInfoService
{
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private InvoiceHeaderModelMapper invoiceHeaderModelMapper;

    @Autowired
    private InvoiceInfoModelMapper invoiceInfoModelMapper;

    @Autowired
    private OrderModelMapper orderModelMapper;

    @Autowired
    private UserBaseMapper userBaseMapper;

    @Autowired
    private MchModelMapper mchModelMapper;

    @Autowired
    private PubliceService publiceService;

    @Override
    public Map<String, Object> getToInvoiced(MainVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            List<Map<String, Object>> list     = new ArrayList<>();
            User                      user     = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            Map<String, Object>       paramMap = new HashMap<>(16);
            paramMap.put("store_id", vo.getStoreId());
            paramMap.put("pageStart", vo.getPageNo());
            paramMap.put("pageEnd", vo.getPageSize());
            paramMap.put("add_time_sort", DataUtils.Sort.DESC.toString());
            paramMap.put("recycle", DictionaryConst.ProductRecycle.NOT_STATUS);
            paramMap.put("userId", user.getUser_id());
            paramMap.put("status", DictionaryConst.OrdersStatus.ORDERS_R_STATUS_COMPLETE);
            paramMap.put("settlement_status", OrderDetailsModel.SETTLEMENT_TYPE_SETTLED);
            paramMap.put("notZero", "notZero");
            int count = orderModelMapper.countDynamic(paramMap);
            int notCount = 0;
            if (count > 0)
            {
                List<Map<String, Object>> mapList = orderModelMapper.selectDynamic(paramMap);
                for (Map<String, Object> map : mapList)
                {
                    Map<String, Object> returnMap = new HashMap<>(16);
                    returnMap.put("order_id", MapUtils.getInteger(map, "id"));
                    returnMap.put("store_id", MapUtils.getInteger(map, "store_id"));
                    returnMap.put("user_id", MapUtils.getString(map, "user_id"));
                    BigDecimal subtract = new BigDecimal(MapUtils.getString(map, "z_price")).subtract(new BigDecimal(MapUtils.getString(map, "z_freight")));
                    returnMap.put("z_price", subtract);
                    returnMap.put("old_total", MapUtils.getString(map, "old_total"));
                    returnMap.put("sNo", MapUtils.getString(map, "sNo"));
                    returnMap.put("add_time", DateUtil.dateFormate(MapUtils.getString(map, "add_time"), GloabConst.TimePattern.YMDHMS));
                    returnMap.put("mch_id", MapUtils.getString(map, "mch_id"));
                    returnMap.put("z_freight", MapUtils.getString(map, "z_freight"));
                    returnMap.put("settlement_status", MapUtils.getInteger(map, "settlement_status"));
                    //店铺名称
                    String   mchIds   = MapUtils.getString(map, "mch_id");
                    String   mchId    = StringUtils.trim(mchIds, SplitUtils.DH);
                    MchModel mchModel = mchModelMapper.getMchInfo(mchId, vo.getStoreId());
                    returnMap.put("shop_id", mchModel.getId());
                    returnMap.put("shop_name", mchModel.getName());
                    returnMap.put("shop_logo", publiceService.getImgPath(mchModel.getLogo(), vo.getStoreId()));
                    //商家未开放发票功能及已申请过的订单不显示
                    InvoiceInfoModel invoiceInfoModel = new InvoiceInfoModel();
                    invoiceInfoModel.setsNo(MapUtils.getString(map, "sNo"));
                    invoiceInfoModel.setStore_id(vo.getStoreId());
                    invoiceInfoModel.setRecovery(DictionaryConst.ProductRecycle.NOT_STATUS);
                    invoiceInfoModel = invoiceInfoModelMapper.selectOne(invoiceInfoModel);
                    Date arriveTime = DateUtil.dateFormateToDate(MapUtils.getString(map, "arrive_time"), GloabConst.TimePattern.YMDHMS);
                    if (mchModel.getIs_invoice().equals(DictionaryConst.WhetherMaven.WHETHER_OK) && invoiceInfoModel == null && !DateUtil.dateCompare(new Date(), DateUtil.getAddDate(arriveTime, 20)))
                    {
                        if (subtract.compareTo(BigDecimal.ZERO) != 0)
                        {
                            list.add(returnMap);
                        }
                        else
                        {
                            notCount++;
                        }

                    }
                }
            }
            count -= notCount;
            resultMap.put("total", count);
            resultMap.put("list", list);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("查询可以开票的订单 异常 ", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getToInvoiced");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> getInvoiceInfo(MainVo vo, Integer status) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            List<Map<String, Object>> list     = new ArrayList<>();
            User                      user     = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            Map<String, Object>       paramMap = new HashMap<>(16);
            paramMap.put("storeId", vo.getStoreId());
            paramMap.put("page", vo.getPageNo());
            paramMap.put("pageSize", vo.getPageSize());
            paramMap.put("addTimeSort", DataUtils.Sort.DESC.toString());
            paramMap.put("userId", user.getUser_id());
            paramMap.put("invoiceStatus", status);
            Integer integer = invoiceInfoModelMapper.countList(paramMap);
            if (integer != null && integer > 0)
            {
                list = invoiceInfoModelMapper.getList(paramMap);
                for (Map<String, Object> map : list)
                {
                    boolean invoiceTimeout = false;
                    String  sNo            = MapUtils.getString(map, "s_no");
                    String  file           = MapUtils.getString(map, "file");
                    if (StringUtils.isNotEmpty(file))
                    {
                        map.put("file", publiceService.getImgPath(file, vo.getStoreId()));
                    }
                    String fileTime = MapUtils.getString(map, "file_time");
                    if (StringUtils.isNotEmpty(fileTime))
                    {
                        map.put("file_time", DateUtil.dateFormate(MapUtils.getString(map, "file_time"), GloabConst.TimePattern.YMDHMS));
                    }
                    //店铺名称
                    String   mchIds   = MapUtils.getString(map, "mch_id");
                    String   mchId    = StringUtils.trim(mchIds, SplitUtils.DH);
                    MchModel mchModel = mchModelMapper.getMchInfo(mchId, vo.getStoreId());
                    map.put("shop_id", mchModel.getId());
                    map.put("shop_name", mchModel.getName());
                    map.put("shop_logo", publiceService.getImgPath(mchModel.getLogo(), vo.getStoreId()));
                    //订单id
                    OrderModel orderModel = new OrderModel();
                    orderModel.setStore_id(vo.getStoreId());
                    orderModel.setsNo(sNo);
                    orderModel = orderModelMapper.selectOne(orderModel);
                    if (orderModel == null)
                    {
                        logger.info("订单编号{}信息不存在", sNo);
                        continue;
                    }
                    map.put("add_time", DateUtil.dateFormate(orderModel.getAdd_time(), GloabConst.TimePattern.YMDHMS));
                    map.put("order_id", orderModel.getId());
                    //发票抬头信息
                    String             invoiceHeader      = MapUtils.getString(map, "invoice_header");
                    InvoiceHeaderModel invoiceHeaderModel = JSONObject.parseObject(invoiceHeader, InvoiceHeaderModel.class);
                    map.put("headId", invoiceHeaderModel.getId());
                    //
                    if (DateUtil.dateCompare(new Date(), DateUtil.getAddDate(orderModel.getArrive_time(), 20)))
                    {
                        invoiceTimeout = true;
                    }
                    map.put("invoiceTimeout", invoiceTimeout);
                }
            }
            resultMap.put("total", integer);
            resultMap.put("list", list);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("查询用户发票列表 异常 ", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getInvoiceInfo");
        }
        return resultMap;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void applyInvoicing(InvoiceInfoVo vo) throws LaiKeAPIException
    {
        try
        {
            if (vo.getHeadId() == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "发票抬头id不得为空", "applyInvoicing");
            }
            if (vo.getType() == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "请选择抬头类型", "applyInvoicing");
            }
            if (StringUtils.isEmpty(vo.getCompanyName()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "名称不得为空", "applyInvoicing");
            }
            if (vo.getAmount() == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "请输入发票总金额", "applyInvoicing");
            }
            if (StringUtils.isEmpty(vo.getsNo()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "请输入需要开票的订单号", "applyInvoicing");
            }
            if (StringUtils.isEmpty(vo.getEmail()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "请输入电子邮箱以备通知", "applyInvoicing");
            }
            OrderModel orderModel = new OrderModel();
            orderModel.setStore_id(vo.getStoreId());
            orderModel.setsNo(vo.getsNo());
            orderModel = orderModelMapper.selectOne(orderModel);
            if (Objects.isNull(orderModel.getId()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DDBCZ, "订单不存在", "applyInvoicing");
            }
            String   mchId    = StringUtils.trim(orderModel.getMch_id(), SplitUtils.DH);
            MchModel mchModel = mchModelMapper.getMchInfo(mchId, vo.getStoreId());
            if (mchModel.getIs_invoice().equals(DictionaryConst.WhetherMaven.WHETHER_NO))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DPZBZCFPGN, "店铺暂不支持发票功能", "applyInvoicing");
            }
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);

            InvoiceInfoModel invoiceInfoModel = new InvoiceInfoModel();
            invoiceInfoModel.setStore_id(vo.getStoreId());
            invoiceInfoModel.setUser_id(user.getUser_id());
            invoiceInfoModel.setMch_id(mchModel.getId());
            invoiceInfoModel.setType(vo.getType());
            invoiceInfoModel.setCompany_name(vo.getCompanyName());
            invoiceInfoModel.setCompany_tax_number(vo.getCompanyTaxNumber());
            invoiceInfoModel.setsNo(vo.getsNo());
            invoiceInfoModel.setInvoice_amount(vo.getAmount());
            invoiceInfoModel.setEmail(vo.getEmail());
            invoiceInfoModel.setInvoice_status(InvoiceInfoModel.IN_APPLICATION);
            invoiceInfoModel.setRecovery(DictionaryConst.ProductRecycle.NOT_STATUS);
            invoiceInfoModel.setAdd_time(new Date());
            //发票抬头快照信息
            InvoiceHeaderModel invoiceHeaderModel = invoiceHeaderModelMapper.selectByPrimaryKey(vo.getHeadId());
            if (Objects.isNull(invoiceHeaderModel))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_MBBCZ, "发票抬头信息不存在", "applyInvoicing");
            }
            JSONObject jsonObject = JSONObject.from(invoiceHeaderModel);
            invoiceInfoModel.setInvoice_header(jsonObject.toJSONString());
            if (vo.getId() == null)
            {
                InvoiceInfoModel oldInvoiceInfo = new InvoiceInfoModel();
                oldInvoiceInfo.setStore_id(vo.getStoreId());
                oldInvoiceInfo.setUser_id(user.getUser_id());
                oldInvoiceInfo.setsNo(vo.getsNo());
                oldInvoiceInfo.setRecovery(DictionaryConst.ProductRecycle.NOT_STATUS);
                oldInvoiceInfo = invoiceInfoModelMapper.selectOne(oldInvoiceInfo);
                if (oldInvoiceInfo != null && oldInvoiceInfo.getId() != null)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_GDDYSQKP, "该订单已申请开票", "applyInvoicing");
                }
                invoiceInfoModelMapper.insertSelective(invoiceInfoModel);
            }
            else
            {
                invoiceInfoModel.setId(vo.getId());
                invoiceInfoModelMapper.updateByPrimaryKeySelective(invoiceInfoModel);
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("申请开票 异常 ", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "applyInvoicing");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void revoke(MainVo vo, Integer id) throws LaiKeAPIException
    {
        try
        {
            InvoiceInfoModel invoiceInfoModel = invoiceInfoModelMapper.selectByPrimaryKey(id);
            if (invoiceInfoModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_MBBCZ, "目标发票记录信息不存在", "revoke");
            }
            invoiceInfoModel.setInvoice_status(InvoiceInfoModel.RESCINDED);
            invoiceInfoModelMapper.updateByPrimaryKeySelective(invoiceInfoModel);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("撤销 异常 ", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "revoke");
        }
    }

    @Override
    public Map<String, Object> getDetails(MainVo vo, Integer id) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            List<Map<String, Object>> list     = new ArrayList<>();
            User                      user     = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            Map<String, Object>       paramMap = new HashMap<>(16);
            paramMap.put("storeId", vo.getStoreId());
            paramMap.put("userId", user.getUser_id());
            paramMap.put("id", id);
            Integer integer = invoiceInfoModelMapper.countList(paramMap);
            if (integer != null && integer > 0)
            {
                list = invoiceInfoModelMapper.getList(paramMap);
                Map<String, Object> map = list.get(0);
                map.put("add_time", DateUtil.dateFormate(MapUtils.getString(map, "add_time"), GloabConst.TimePattern.YMDHMS));
                String file = MapUtils.getString(map, "file");
                if (StringUtils.isNotEmpty(file))
                {
                    map.put("file", publiceService.getImgPath(file, vo.getStoreId()));
                }
                String fileTime = MapUtils.getString(map, "file_time");
                if (StringUtils.isNotEmpty(fileTime))
                {
                    map.put("file_time", DateUtil.dateFormate(MapUtils.getString(map, "file_time"), GloabConst.TimePattern.YMDHMS));
                }
                resultMap = map;
            }
            else
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_MBBCZ, "目标发票记录信息不存在", "invoiceInfo");
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("查询用户发票详情 异常 ", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "invoiceInfo");
        }
        return resultMap;
    }
}

