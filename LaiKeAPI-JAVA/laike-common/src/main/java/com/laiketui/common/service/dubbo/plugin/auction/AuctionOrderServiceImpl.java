package com.laiketui.common.service.dubbo.plugin.auction;

import com.alibaba.fastjson2.JSON;
import com.laiketui.common.annotation.HandlerOrderType;
import com.laiketui.common.api.order.OrderDubboService;
import com.laiketui.common.utils.HttpApiUtils;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.DictionaryConst;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.domain.vo.OrderVo;
import com.laiketui.domain.vo.order.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * 竞拍订单流程
 *
 * @author Trick
 * @date 2022/7/25 12:03
 */
@HandlerOrderType(type = DictionaryConst.OrdersType.ORDERS_HEADER_JP)
@Service("auctionOrderDubboService")
public class AuctionOrderServiceImpl implements OrderDubboService
{
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 插件远程调用地址
     *
     * @author Trick
     * @date 2022/7/27 15:20
     */
    interface ApiKey
    {
        /**
         * 结算
         */
        String SETTLEMENT_API  = "plugin.auction.settlement";
        /**
         * 下单
         */
        String PLACE_ORDER_API = "plugin.auction.placeOrder";
    }

    @Autowired
    private HttpApiUtils httpApiUtils;

    @Override
    public Map<String, Object> settlement(OrderVo vo)
    {
        Map<String, Object> resultMap;
        try
        {
            Map<String, Object> paramMap = new HashMap<>(1);
            paramMap.put("vo", vo);
            /**
             *   {
             *       'vo':{
             *
             *       },
             *       'userId':'user_12',
             *       'paramMap':{
             *
             *       }
             *   }
             */
            resultMap = httpApiUtils.executeApi(ApiKey.SETTLEMENT_API, JSON.toJSONString(paramMap));
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("竞拍-结算 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "settlement");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> payment(OrderVo vo)
    {
        Map<String, Object> resultMap;
        try
        {
            Map<String, Object> paramApiMap = JSON.parseObject(JSON.toJSONString(vo));
            Map<String, Object> paramMap    = new HashMap<>(1);
            paramMap.put("vo", vo);
            resultMap = httpApiUtils.executeHttpApi(ApiKey.PLACE_ORDER_API, paramApiMap, MediaType.MULTIPART_FORM_DATA_VALUE);
        }
        catch (LaiKeAPIException e)
        {
            logger.error("竞拍下单 异常", e);
            throw e;
        }
        catch (Exception e)
        {
            logger.error("竞拍下单 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "payment");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> updateOrderRemark(OrderVo vo)
    {
        return null;
    }

    @Override
    public Map<String, Object> splitOrder(OrderVo vo)
    {
        return null;
    }

    @Override
    public Map<String, Object> returnData(ApplyReturnDataVo vo, MultipartFile file) throws LaiKeAPIException
    {
        return null;
    }

    @Override
    public Map<String, Object> orderList(OrderVo vo)
    {
        return null;
    }

    @Override
    public Map<String, Object> remindDelivery(OrderVo vo)
    {
        return null;
    }

    @Override
    public Map<String, Object> cancleOrder(OrderVo vo)
    {
        return null;
    }

    @Override
    public Map<String, Object> loadMore(OrderVo vo)
    {
        return null;
    }

    @Override
    public Map<String, Object> delOrder(OrderVo vo)
    {
        return null;
    }

    @Override
    public Map<String, Object> buyAgain(BuyAgainVo vo)
    {
        return null;
    }

    @Override
    public Map<String, Object> orderSearch(OrderVo vo)
    {
        return null;
    }

    @Override
    public Map<String, Object> delCart(OrderVo vo)
    {
        return null;
    }

    @Override
    public Map<String, Object> getPaymentConf(OrderVo vo)
    {
        return null;
    }

    @Override
    public Map<String, Object> orderDetails(OrderVo vo)
    {
        return null;
    }

    @Override
    public Map<String, Object> showLogistics(OrderVo vo)
    {
        return null;
    }

    @Override
    public Map<String, Object> cancleApply(int storeId, int id)
    {
        return null;
    }

    @Override
    public Map<String, Object> returnOrderList(OrderVo vo)
    {
        return null;
    }

    @Override
    public Map<String, Object> okOrder(OrderVo orderVo)
    {
        return null;
    }

    @Override
    public Map<String, Object> returnMethod(OrderVo vo)
    {
        return null;
    }

    @Override
    public Map<String, Object> seeSend(int stroeId, int productId)
    {
        return null;
    }

    @Override
    public Map<String, Object> backSend(ReturnGoodsVo returnGoodsVo)
    {
        return null;
    }

    @Override
    public Map<String, Object> seeExtractionCode(OrderVo vo)
    {
        return null;
    }

    @Override
    public Map<String, Object> returndetails(RefundDetailsVo refundDetailsVo)
    {
        return null;
    }

    @Override
    public Map<String, Object> confirmReceipt(ReturnConfirmReceiptVo vo)
    {
        return null;
    }

    @Override
    public Map<String, Object> cancelApplication(CancleAfterSaleApplyVo params)
    {
        return null;
    }

    @Override
    public Map<String, Object> getPayment(OrderVo vo)
    {
        return null;
    }
}
