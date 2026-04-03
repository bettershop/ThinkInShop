package com.laiketui.admins.mch.services.plugin.order;

import com.laiketui.admins.api.mch.plugin.order.MchIntegralOrderService;
import com.laiketui.common.api.PublicOrderService;
import com.laiketui.common.api.order.OrderDubboService;
import com.laiketui.common.utils.EasyPoiExcelUtil;
import com.laiketui.common.utils.tool.cache.RedisDataTool;
import com.laiketui.core.cache.RedisUtil;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.DictionaryConst;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.utils.tool.DataUtils;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.OrderVo;
import com.laiketui.domain.vo.Tool.ExcelParamVo;
import com.laiketui.domain.vo.order.AdminOrderListVo;
import com.laiketui.domain.vo.order.OrderSettlementVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 积分订单
 *
 * @author Trick
 * @date 2021/10/20 15:01
 */
@Service("pcIntegralOrderServiceImpl")
public class MchIntegralOrderServiceImpl implements MchIntegralOrderService
{
    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    @Override
    public Map<String, Object> index(AdminOrderListVo adminOrderVo, HttpServletResponse response) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            resultMap = publicOrderService.pcMchOrderIndex(adminOrderVo);
            if (adminOrderVo.getExportType().equals(1))
            {
                exportOrderData(DataUtils.cast(resultMap.get("list")), response);
                return null;
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("秒杀订单首页 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "index");
        }
        return resultMap;
    }

    //导出订单列表
    private void exportOrderData(List<Map<String, Object>> list, HttpServletResponse response) throws LaiKeAPIException
    {
        try
        {
            //表头
            String[] headerList = new String[]{"订单编号", "创单时间", "店铺名称", "产品名称", "规格", "数量", "小计", "订单总计", "数量", "订单状态", "订单类型", "会员ID", "联系人"
                    , "电话", "地址", "支付方式", "物流单号", "运费"};
            //对应字段
            String[] kayList = new String[]{"orderno", "createDate", "shopName", "goodsName", "attrStr", "needNum", "goodsPrice", "orderPrice", "goodsNum", "status", "otype", "userId", "userName"
                    , "mobile", "addressInfo", "payName", "courier_num", "freight"};
            ExcelParamVo vo = new ExcelParamVo();
            vo.setTitle("订单列表");
            vo.setHeaderList(headerList);
            vo.setValueList(kayList);
            vo.setList(list);
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
            logger.error("导出订单列表数据 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "exportOrderData");
        }
    }


    @Autowired
    @Qualifier("integralOrderHandler")
    private OrderDubboService orderDubboService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void closeOrder(MainVo vo, Integer id) throws LaiKeAPIException
    {
        try
        {
            //关闭订单
            OrderVo orderVo = new OrderVo();
            orderVo.setOrderId(id);
            orderVo.setStoreId(vo.getStoreId());
            orderDubboService.cancleOrder(orderVo);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("关闭订单 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "index");
        }
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delOrder(MainVo vo, String id) throws LaiKeAPIException
    {
        try
        {
            //关闭订单
            OrderVo orderVo = new OrderVo();
            orderVo.setOrdervalue(id);
            orderVo.setStoreId(vo.getStoreId());
            orderDubboService.delOrder(orderVo);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("删除订单 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "index");
        }
    }

    @Override
    public Map<String, Object> orderSettlement(OrderSettlementVo vo, HttpServletResponse response) throws LaiKeAPIException
    {
        Map<String, Object> resultMap;
        try
        {
            RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            vo.setOrderType(DictionaryConst.OrdersType.ORDERS_HEADER_IN);
            resultMap = publicOrderService.getSettlementOrderList(vo);
            if (vo.getExportType() == 1)
            {
                exportOrderData(DataUtils.cast(resultMap.get("list")), response);
                return null;
            }
            return resultMap;
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("订单结算列表 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "index");
        }
    }

    @Autowired
    private PublicOrderService publicOrderService;

    @Autowired
    private RedisUtil redisUtil;
}

