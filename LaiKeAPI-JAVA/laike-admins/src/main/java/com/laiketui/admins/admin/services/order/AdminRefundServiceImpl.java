package com.laiketui.admins.admin.services.order;

import com.laiketui.admins.api.admin.order.AdminRefundService;
import com.laiketui.common.api.PublicOrderService;
import com.laiketui.common.api.PublicRefundService;
import com.laiketui.common.api.PubliceService;
import com.laiketui.common.mapper.*;
import com.laiketui.common.utils.EasyPoiExcelUtil;
import com.laiketui.common.utils.tool.cache.RedisDataTool;
import com.laiketui.core.cache.RedisUtil;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.DictionaryConst;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.utils.tool.DataUtils;
import com.laiketui.core.utils.tool.StringUtils;
import com.laiketui.domain.log.AdminRecordModel;
import com.laiketui.domain.mch.AdminModel;
import com.laiketui.domain.order.OrderModel;
import com.laiketui.domain.order.ReturnOrderModel;
import com.laiketui.domain.product.ProductListModel;
import com.laiketui.domain.vo.Tool.ExcelParamVo;
import com.laiketui.domain.vo.main.RefundVo;
import com.laiketui.domain.vo.order.RefundQueryVo;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 退货管理
 *
 * @author Trick
 * @date 2021/1/5 15:54
 */
@Service
public class AdminRefundServiceImpl implements AdminRefundService
{
    private final Logger logger = LoggerFactory.getLogger(AdminRefundServiceImpl.class);

    @Autowired
    private PubliceService publiceService;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private OrderDetailsModelMapper orderDetailsModelMapper;

    @Autowired
    private PublicOrderService publicOrderService;

    @Autowired
    private RecordModelMapper recordModelMapper;

    @Autowired
    private ReturnRecordModelMapper returnRecordModelMapper;

    @Autowired
    private MessageLoggingModalMapper messageLoggingModalMapper;

    @Override
    public Map<String, Object> getRefundList(RefundQueryVo vo, HttpServletResponse response) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            AdminModel adminModel = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            if ((vo.getId() != null && vo.getId() > 0) || (vo.getOrderId() != null && vo.getOrderId() > 0))
            {
                if (vo.getOrderId() != null)
                {
                    ReturnOrderModel returnOrderModel = returnOrderModelMapper.getReturnOrderInfoByOrderId(vo.getStoreId(), vo.getOrderId());
                    if (returnOrderModel != null)
                    {
                        vo.setId(returnOrderModel.getId());
                    }
                }
                resultMap = publicRefundService.refundPageById(vo.getStoreId(), vo.getId());
                //后台查看按钮权限
                boolean selectBtn = publiceService.jurisdiction(vo.getStoreId(), adminModel, "index.php?module=return&action=View");
                //退款审核流程所有按钮权限
                boolean returnBtn = publiceService.jurisdiction(vo.getStoreId(), adminModel, "index.php?module=return&action=Examine");
                resultMap.put("selectBtn", selectBtn);
                resultMap.put("returnBtn", returnBtn);
            }
            else
            {
                Map<String, Object> returnParmaMap = new HashMap<>(16);
                returnParmaMap.put("store_id", vo.getStoreId());
                if (!StringUtils.isEmpty(vo.getOrderno()))
                {
                    returnParmaMap.put("likeOrderno", vo.getOrderno());
                }
                if (vo.getStatus() != null)
                {
                    //1=退款中,2=退款成功,3=退款失败,4=换货中,5=换货成功,6=换货失败,7=待审核
                    returnParmaMap.put("return_type", vo.getStatus());
                }
                if (!StringUtils.isEmpty(vo.getStartDate()))
                {
                    returnParmaMap.put("startDate", vo.getStartDate());
                    if (!StringUtils.isEmpty(vo.getEndDate()))
                    {
                        returnParmaMap.put("endDate", vo.getEndDate());
                    }
                }
                //订单类型
                if (StringUtils.isNotEmpty(vo.getOrderType()))
                {
                    returnParmaMap.put("orderType", vo.getOrderType());
                }
                if (vo.getoId() != null)
                {
                    returnParmaMap.put("orderId", vo.getoId());
                }

                if (vo.getReType() != null)
                {
                    returnParmaMap.put("reType", vo.getReType());
                }
                if (vo.getMchName() != null)
                {
                    returnParmaMap.put("mchName", vo.getMchName());
                }
                returnParmaMap.put("reTimeSort", DataUtils.Sort.DESC.toString());
                returnParmaMap.put("reTypeSort", DataUtils.Sort.ASC.toString());
                returnParmaMap.put("pageNo", vo.getPageNo());
                returnParmaMap.put("pageSize", vo.getPageSize());
                ArrayList<String> orderNotList = new ArrayList<>();
                orderNotList.add(DictionaryConst.OrdersType.ORDERS_HEADER_ZB);
                returnParmaMap.put("orderTypeNotList", orderNotList);
                List<Map<String, Object>> returnOrderMapList = returnOrderModelMapper.getReturnOrderByGoodsInfo(returnParmaMap);
                for (Map<String, Object> map : returnOrderMapList)
                {
                    String  returnType;
                    Integer reType   = MapUtils.getInteger(map, "re_type");
                    Integer refundId = MapUtils.getIntValue(map, "id");
                    Double  actual_total = MapUtils.getDouble(map, "actual_total");
              /*      if (Objects.nonNull(actual_total))
                    {
                        BigDecimal actualTotal = BigDecimal.valueOf(actual_total);
                        BigDecimal re_money = BigDecimal.valueOf(MapUtils.getDouble(map, "re_money"));
                        if (actualTotal.compareTo(BigDecimal.ZERO) > 0)
                        {
                            re_money = re_money.subtract(actualTotal).setScale(2,BigDecimal.ROUND_HALF_UP);
                            map.put("re_money",re_money);
                        }
                    }*/
                    switch (reType)
                    {
                        case 2:
                            returnType = "仅退款";
                            break;
                        case 1:
                            returnType = "退货退款";
                            break;
                        default:
                            returnType = "换货";
                            break;
                    }
                    //虚拟商品需要核销按照核销次数退款
                    if (map.containsKey("after_discount") && map.containsKey("after_write_off_num") && map.containsKey("r_write_off_num") && DataUtils.getIntegerVal(map, "write_off_settings").equals(ProductListModel.WRITE_OFF_SETTINGS.offline))
                    {
                        BigDecimal re_money = DataUtils.getBigDecimalVal(map, "after_discount").divide(DataUtils.getBigDecimalVal(map, "r_write_off_num").add(DataUtils.getBigDecimalVal(map, "after_write_off_num")), 5, BigDecimal.ROUND_HALF_UP).multiply(DataUtils.getBigDecimalVal(map, "r_write_off_num"))
                                .setScale(2, BigDecimal.ROUND_HALF_UP);
                        map.put("re_money", re_money);
                    }
                    map.put("returnTypeName", returnType);
                    //region 是否可以审核
                    boolean isExamine;
                    map.put("isExamine", isExamine = publicRefundService.isExamine(vo.getStoreId(), refundId));
                    //endregion

                    //region 是否显示人工受理按钮 商品回寄之后被拒绝
                    boolean isManExamine = false;
                    if (!isExamine)
                    {
                        isManExamine = publicRefundService.isMainExamine(vo.getStoreId(), refundId);
                    }
                    map.put("isManExamine", isManExamine);
                    //endregion

                    //实退金额
                    String     realAmtName;
                    BigDecimal realAmt = new BigDecimal(MapUtils.getString(map, "real_money"));
                    if (realAmt.compareTo(BigDecimal.ZERO) == 0 && !DictionaryConst.ReturnRecordReType.RETURNORDERSTATUS_GOODS_REBACK.equals(reType))
                    {
                        realAmtName = "未退款";
                    }
                    else if (DictionaryConst.ReturnRecordReType.RETURNORDERSTATUS_GOODS_REBACK.equals(reType))
                    {
                        realAmtName = "-";
                    }
                    else
                    {
                        realAmtName = realAmt.toString();
                    }
                    map.put("realAmtName", realAmtName);

                    //货币处理
                    String currencyCode = MapUtils.getString(map, "currency_code", "CNY");
                    map.put("currency_code", currencyCode);

                    String currencySymbol = MapUtils.getString(map, "currency_symbol", "￥");
                    map.put("currency_symbol", currencySymbol);

                    BigDecimal exchageRate = new BigDecimal(MapUtils.getDouble(map, "exchange_rate", 1.0));
                    map.put("exchange_rate", exchageRate);

                }
                int total = returnOrderModelMapper.getReturnOrderByGoodsCount(returnParmaMap);

                if (vo.getExportType() == 1)
                {
                    exportOrderData(returnOrderMapList, response);
                    return null;
                }

                resultMap.put("total", total);
                resultMap.put("list", returnOrderMapList);
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取售后列表异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常");
        }
        return resultMap;
    }


    /**
     * 导出售后列表
     *
     * @param goodsList -
     * @param response  -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/7/29 10:22
     */
    private void exportOrderData(List<Map<String, Object>> goodsList, HttpServletResponse response) throws LaiKeAPIException
    {
        try
        {
            //表头
            String[] headerList = new String[]{"用户id", "商品名称", "商品价格", "数量", "订单号", "实退金额", "申请时间", "类型", "状态"};
            //对应字段
            String[]     kayList = new String[]{"user_id", "p_name", "p_price", "num", "sNo", "realAmtName", "re_time", "returnTypeName", "prompt"};
            ExcelParamVo vo      = new ExcelParamVo();
            vo.setTitle("售后列表");
            vo.setHeaderList(headerList);
            vo.setValueList(kayList);
            vo.setList(goodsList);
            vo.setResponse(response);
            vo.setNeedNo(true);
            EasyPoiExcelUtil.excelExport(vo);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("导出订单结算 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "exportOrderData");
        }
    }

    @Autowired
    private IntegralConfigModelMapper integralConfigModelMapper;
    @Autowired
    private SignRecordModelMapper     signRecordModelMapper;
    @Autowired
    private UserBaseMapper            userBaseMapper;
    @Autowired
    private OrderModelMapper          orderModelMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean examine(RefundVo vo) throws LaiKeAPIException
    {
        try
        {
            logger.info("进入退款审核方法");
            AdminModel       adminModel       = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            ReturnOrderModel returnOrderModel = returnOrderModelMapper.selectByPrimaryKey(vo.getId());
            OrderModel       orderModel       = new OrderModel();
            orderModel.setsNo(returnOrderModel.getsNo());
            orderModel = orderModelMapper.selectOne(orderModel);
            if (vo.getType().equals(DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_AGREE_REBACK)
                    || vo.getType().equals(DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_RECEIVED_REBAKGOODS)
                    || vo.getType().equals(DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_AGREE_REBACK_AMT)
                    || vo.getType().equals(DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_AGREE_REBACK_GOODS))
            {
                publiceService.addAdminRecord(vo.getStoreId(), "通过了订单ID：" + orderModel.getsNo() + " 的售后申请", AdminRecordModel.Type.PASS_OR_REFUSE, vo.getAccessId());
            }
            else
            {
                publiceService.addAdminRecord(vo.getStoreId(), "拒绝了订单ID：" + orderModel.getsNo() + " 的售后申请", AdminRecordModel.Type.PASS_OR_REFUSE, vo.getAccessId());
            }
            return publicRefundService.refund(vo);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("售后", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String,Object> quickRefund(RefundVo vo)
    {
        return publicRefundService.adminQuickRefund(vo);
    }


    @Autowired
    private ReturnOrderModelMapper returnOrderModelMapper;

    @Autowired
    private PublicRefundService publicRefundService;
}

