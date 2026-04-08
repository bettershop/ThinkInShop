package com.laiketui.admins.mch.services.plugin.order;

import com.laiketui.admins.api.mch.plugin.order.MchRefundService;
import com.laiketui.common.api.PublicRefundService;
import com.laiketui.common.api.PubliceService;
import com.laiketui.common.mapper.ReturnOrderModelMapper;
import com.laiketui.common.utils.EasyPoiExcelUtil;
import com.laiketui.common.utils.tool.cache.RedisDataTool;
import com.laiketui.core.cache.RedisUtil;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.DictionaryConst;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.utils.tool.DataUtils;
import com.laiketui.core.utils.tool.StringUtils;
import com.laiketui.domain.mch.AdminModel;
import com.laiketui.domain.order.ReturnOrderModel;
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
public class MchRefundServiceImpl implements MchRefundService
{
    private final Logger logger = LoggerFactory.getLogger(MchRefundServiceImpl.class);

    @Autowired
    private PubliceService publiceService;

    @Autowired
    private RedisUtil redisUtil;

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
                    returnParmaMap.put("orderno", vo.getOrderno());
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
                if (vo.getOrderId() != null)
                {
                    returnParmaMap.put("orderId", vo.getOrderId());
                }
                returnParmaMap.put("reTimeSort", DataUtils.Sort.DESC.toString());
                returnParmaMap.put("reTypeSort", DataUtils.Sort.ASC.toString());
                returnParmaMap.put("pageNo", vo.getPageNo());
                returnParmaMap.put("pageSize", vo.getPageSize());
                List<Map<String, Object>> returnOrderMapList = returnOrderModelMapper.getReturnOrderByGoodsInfo(returnParmaMap);
                for (Map<String, Object> map : returnOrderMapList)
                {
                    String  returnType;
                    Integer reType = MapUtils.getInteger(map, "re_type");
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
                    map.put("returnTypeName", returnType);
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
            String[] headerList = new String[]{"用户id", "产品名称", "产品价格", "数量", "订单号", "实退金额", "发布时间", "类型", "状态"};
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean examine(RefundVo vo) throws LaiKeAPIException
    {
        try
        {
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


    @Autowired
    private ReturnOrderModelMapper returnOrderModelMapper;

    @Autowired
    private PublicRefundService publicRefundService;
}

