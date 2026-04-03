package com.laiketui.common.service.dubbo.order;

import com.laiketui.common.api.PublicOrderService;
import com.laiketui.common.api.PubliceService;
import com.laiketui.common.api.order.OrderSettlementService;
import com.laiketui.common.mapper.OrderDetailsModelMapper;
import com.laiketui.common.mapper.OrderModelMapper;
import com.laiketui.common.utils.EasyPoiExcelUtil;
import com.laiketui.core.cache.RedisUtil;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.DictionaryConst;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.core.utils.tool.DataUtils;
import com.laiketui.domain.log.AdminRecordModel;
import com.laiketui.domain.order.OrderModel;
import com.laiketui.domain.user.User;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.Tool.ExcelParamVo;
import com.laiketui.domain.vo.order.AdminOrderDetailVo;
import com.laiketui.domain.vo.order.OrderSettlementVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 订单结算
 *
 * @author Trick
 * @date 2021/7/7 11:31
 */
@Service
public class OrderSettlementServiceImpl implements OrderSettlementService
{

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private OrderModelMapper orderModelMapper;

    @Autowired
    private PublicOrderService publicOrderService;

    @Autowired
    private OrderDetailsModelMapper orderDetailsModelMapper;

    @Autowired
    private PubliceService publiceService;

    @Override
    public Map<String, Object> index(OrderSettlementVo vo, HttpServletResponse response) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            //RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            //todo-plugin-meger
            User user = publiceService.getRedisUserCache(vo);
            //管理平台需要显示所有店铺的结算列表
            if (vo.getStoreType() != GloabConst.StoreType.STORE_TYPE_PC_ADMIN)
            {
                vo.setMchId(user.getMchId());
            }
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

    /**
     * 导出订单结算
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
            String[] headerList = new String[]{"结算单号", "结算金额", "佣金", "退还佣金", "订单编号", "订单金额", "店铺名称", "退单金额"
                    , "结算状态", "结算时间","运费","店铺优惠","平台优惠","订单生成时间"};
            //对应字段
            String[] kayList = new String[]{"id", "settlementPrice", "commission", "r_commission", "sNo", "z_price", "shopName", "return_money"
                    , "status_name", "arrive_time","z_freight","mch_discount","preferential_amount","add_time"};
            //对应字段
            ExcelParamVo vo = new ExcelParamVo();
            vo.setTitle("订单结算列表");
            vo.setHeaderList(headerList);
            vo.setValueList(kayList);
            vo.setList(goodsList);
            vo.setResponse(response);
            vo.setNeedNo(false);
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
    public Map<String, Object> detail(MainVo vo, String orderNo) throws LaiKeAPIException
    {
        try
        {
            //RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            User               user               = publiceService.getRedisUserCache(vo);
            AdminOrderDetailVo adminOrderDetailVo = new AdminOrderDetailVo();
            adminOrderDetailVo.setStoreId(vo.getStoreId());
            adminOrderDetailVo.setId(orderNo);
            adminOrderDetailVo.setOrderType("see");
            return publicOrderService.orderPcDetails(adminOrderDetailVo);
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void del(MainVo vo, int id) throws LaiKeAPIException
    {
        try
        {
            int        row;
            User       user       = publiceService.getRedisUserCache(vo);
            OrderModel orderModel = orderModelMapper.selectByPrimaryKey(id);
            if (orderModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DDBCZ, "订单不存在");
            }
            OrderModel orderUpdate = new OrderModel();
            orderUpdate.setId(id);
            orderUpdate.setRecycle(DictionaryConst.ProductRecycle.RECOVERY);
            row = orderModelMapper.updateByPrimaryKeySelective(orderUpdate);
            if (row < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SCSB, "删除失败");
            }
            row = orderDetailsModelMapper.delOrderDetails1(vo.getStoreId(), DictionaryConst.ProductRecycle.RECOVERY, orderModel.getsNo());
            if (row < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SCSB, "删除失败");
            }
            //记录日志
            publiceService.addAdminRecord(vo.getStoreId(), "删除了订单ID：" + orderModel.getsNo() + "的结算信息", AdminRecordModel.Type.DEL, vo.getAccessId());
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("删除结算订单 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "del");
        }
    }

    @Override
    public Map<String, Object> pluginIndex(OrderSettlementVo vo, HttpServletResponse response) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User user = publiceService.getRedisUserCache(vo);
            vo.setMchId(user.getMchId());
            resultMap = publicOrderService.getSettlementOrderList(vo);
            if (vo.getExportType() == 1)
            {
                pluginExportOrderData(DataUtils.cast(resultMap.get("list")), response);
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

    @Override
    public Map<String, Object> pluginDetail(MainVo vo, String orderNo) throws LaiKeAPIException
    {
        try
        {
            publiceService.getRedisUserCache(vo);
            AdminOrderDetailVo adminOrderDetailVo = new AdminOrderDetailVo();
            adminOrderDetailVo.setStoreId(vo.getStoreId());
            adminOrderDetailVo.setId(orderNo);
            adminOrderDetailVo.setOrderType("see");
            return publicOrderService.orderPcDetails(adminOrderDetailVo);
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

    @Override
    public void pluginDel(MainVo vo, int id) throws LaiKeAPIException
    {
        try
        {
            int row;
            publiceService.getRedisUserCache(vo);
            OrderModel orderModel = orderModelMapper.selectByPrimaryKey(id);
            if (orderModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DDBCZ, "订单不存在");
            }
            OrderModel orderUpdate = new OrderModel();
            orderUpdate.setId(id);
            orderUpdate.setRecycle(DictionaryConst.ProductRecycle.RECOVERY);
            row = orderModelMapper.updateByPrimaryKeySelective(orderUpdate);
            if (row < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SCSB, "删除失败");
            }
            row = orderDetailsModelMapper.delOrderDetails1(vo.getStoreId(), DictionaryConst.ProductRecycle.RECOVERY, orderModel.getsNo());
            if (row < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SCSB, "删除失败");
            }
            publiceService.addAdminRecord(vo.getStoreId(), "删除了订单ID：" + orderModel.getsNo() + " 的结算信息", AdminRecordModel.Type.DEL, vo.getAccessId());
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("删除结算订单 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "del");
        }
    }

    /**
     * 导出订单结算
     *
     * @param goodsList -
     * @param response  -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/7/29 10:22
     */
    private void pluginExportOrderData(List<Map<String, Object>> goodsList, HttpServletResponse response) throws LaiKeAPIException
    {
        try
        {
            //表头
            String[] headerList = new String[]{"结算单号", "结算金额", "订单编号", "订单金额", "店铺名称", "结算状态", "结算时间", "运费", "订单生成时间"};
            //对应字段
            String[] kayList = new String[]{"id", "r_commission", "sNo", "z_price", "shopName", "status_name", "arrive_time", "z_freight", "add_time"};
            //对应字段
            ExcelParamVo vo = new ExcelParamVo();
            vo.setTitle("订单结算列表");
            vo.setHeaderList(headerList);
            vo.setValueList(kayList);
            vo.setList(goodsList);
            vo.setResponse(response);
            vo.setNeedNo(false);
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
}

