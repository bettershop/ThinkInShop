package com.laiketui.comps.order.controlller;

import com.alibaba.fastjson2.JSONObject;
import com.laiketui.common.api.PublicOrderService;
import com.laiketui.common.api.order.OrderDubboService;
import com.laiketui.common.process.HandlerOrderContext;
import com.laiketui.core.annotation.ApiLimit;
import com.laiketui.core.annotation.Idempotency;
import com.laiketui.core.annotation.ParamsMapping;
import com.laiketui.core.cache.RedisUtil;
import com.laiketui.core.domain.Result;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.DictionaryConst;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.utils.tool.StringUtils;
import com.laiketui.domain.order.OrderModel;
import com.laiketui.domain.user.User;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.OrderVo;
import com.laiketui.domain.vo.order.*;
import com.laiketui.root.annotation.HttpApiMethod;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import tk.mybatis.mapper.code.ORDER;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author wangxian
 */
@RestController
@RequestMapping("/comps/order")
public class CompsOrderController
{

    private final Logger logger = LoggerFactory.getLogger(CompsOrderController.class);

    @Autowired
    private HandlerOrderContext handlerOrderContext;

    @Autowired
    private PublicOrderService publicOrderService;

    @Autowired
    private RedisUtil redisUtil;

    @RequestMapping("/orderlist")
    @HttpApiMethod(urlMapping = {"app.order.index"})
    public Result orderlist(OrderVo vo)
    {
        try
        {
            OrderDubboService orderService = null;
            if (StringUtils.isNotEmpty(vo.getOrderList()))
            {
                JSONObject jsonObject = JSONObject.parseObject(vo.getOrderList());
                String     sNo        = jsonObject.getString("sNo");
                if (redisUtil.hasKey("lock:order:" + sNo))
                {
                    String token = redisUtil.get("lock:order:" + sNo).toString();
                    if (token.equals(vo.getAccessId()))
                    {
                        Integer status = publicOrderService.getStatusByOrderNo(sNo);
                        if (status == DictionaryConst.OrdersStatus.ORDERS_R_STATUS_UNPAID)
                        {
                            redisUtil.del("lock:order:" + sNo);
                        }
                    }
                }
            }
            if (Objects.isNull(vo.getType()))
            {
                orderService = handlerOrderContext.getOrderService(DictionaryConst.OrdersType.ORDERS_HEADER_GM);
            }
            else
            {
                orderService = handlerOrderContext.getOrderService(vo.getType());
            }
            Map<String, Object> resultMap = orderService.orderList(vo);
            return Result.success(resultMap);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @RequestMapping("/settlement")
    @HttpApiMethod(urlMapping = {"app.order.Settlement", "plugin.integral.App.integralAxios", "app.groupbuy.payfor"})
    public Result settlement(OrderVo vo)
    {
        try
        {
            OrderDubboService   orderService = handlerOrderContext.getOrderService(vo.getType());
            Map<String, Object> resultMap    = orderService.settlement(vo);
            return Result.success(resultMap);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @RequestMapping("/payment")
    @HttpApiMethod(urlMapping = {"app.order.payment", "plugin.integral.App.payment", "app.groupbuy.createOrder"})
    @Idempotency
    @ApiLimit(limit = 1,time = 1)
//    @PreventOverSell
    public Result payment(OrderVo vo)
    {
        try
        {
            OrderDubboService   orderService = handlerOrderContext.getOrderService(vo.getType());
            Map<String, Object> resultMap    = orderService.payment(vo);
            return Result.success(resultMap);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @RequestMapping("/splitOrder")
    @HttpApiMethod(urlMapping = "app.order.leave_Settlement")
    @Idempotency
    public Result splitOrder(String sNo, Integer storeId, String userId, User user)
    {
        try
        {
            /*Map<String, String> orderInfoMap = JSON.parseObject(vo.getOrderList(), new TypeReference<Map<String, String>>() {
            });*/
            OrderModel          orderModel = publicOrderService.getOrderInfoById(storeId, null, sNo);
            Map<String, Object> resultMap  = new HashMap<>(16);
            if (orderModel != null)
            {
                OrderVo vo = new OrderVo();
                vo.setsNo(sNo);
                vo.setStoreId(storeId);
                vo.setUserId(userId);
                vo.setUser(user);
                OrderDubboService orderService = handlerOrderContext.getOrderService(orderModel.getOtype());
                resultMap = orderService.splitOrder(vo);

            }
            return Result.success(resultMap);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("加载更多订单")
    @PostMapping("/loadMore")
    public Result loadMore(OrderVo vo)
    {
        try
        {
            OrderDubboService   orderService = handlerOrderContext.getOrderService(DictionaryConst.OrdersType.ORDERS_HEADER_GM);
            Map<String, Object> resultMap    = orderService.loadMore(vo);
            return Result.success(resultMap);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("订单详情")
    @PostMapping("/orderDetails")
    @HttpApiMethod(urlMapping = {"app.order.order_details"})
    //@PreventOrderResubmit(keyExpr = "#vo.orderId + ',' + #vo.pay")
    public Result orderDetails(OrderVo vo)
    {
        try
        {
            OrderDubboService orderService = null;
            if (Objects.isNull(vo.getType()))
            {
                orderService = handlerOrderContext.getOrderService(DictionaryConst.OrdersType.ORDERS_HEADER_GM);
            }
            else
            {
                orderService = handlerOrderContext.getOrderService(vo.getType());
            }
            Map<String, Object> resultMap = orderService.orderDetails(vo);
            return Result.success(resultMap);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("删除订单")
    @PostMapping("/delOrder")
    @HttpApiMethod(urlMapping = "app.order.del_order")
    @Idempotency
    public Result delOrder(OrderVo vo)
    {
        try
        {
            OrderDubboService   orderService = handlerOrderContext.getOrderService(DictionaryConst.OrdersType.ORDERS_HEADER_GM);
            Map<String, Object> resultMap    = orderService.delOrder(vo);
            return Result.success(resultMap);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("再次购买")
    @PostMapping("/buyAgain")
    @HttpApiMethod(urlMapping = "app.order.buy_again")
    @Idempotency
    public Result buyAgain(BuyAgainVo vo)
    {
        try
        {
            OrderDubboService   orderService = handlerOrderContext.getOrderService(DictionaryConst.OrdersType.ORDERS_HEADER_GM);
            Map<String, Object> resultMap    = orderService.buyAgain(vo);
            return Result.success(resultMap);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("订单搜索")
    @PostMapping("/orderSearch")
    @HttpApiMethod(urlMapping = "app.order.search")
    public Result orderSearch(OrderVo vo)
    {
        try
        {
            OrderDubboService   orderService = handlerOrderContext.getOrderService(DictionaryConst.OrdersType.ORDERS_HEADER_GM);
            Map<String, Object> resultMap    = orderService.orderSearch(vo);
            return Result.success(resultMap);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("删除购物车")
    @PostMapping("/delCart")
    @HttpApiMethod(urlMapping = "app.order.del_cart")
    @Idempotency
    public Result delCart(OrderVo vo)
    {
        try
        {
            OrderDubboService   orderService = handlerOrderContext.getOrderService(DictionaryConst.OrdersType.ORDERS_HEADER_GM);
            Map<String, Object> resultMap    = orderService.delCart(vo);
            return Result.success(resultMap);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("获取支付方式")
    @PostMapping("/getPaymentConf")
    public Result getPaymentConf(OrderVo vo)
    {
        try
        {
            OrderDubboService   orderService = handlerOrderContext.getOrderService(DictionaryConst.OrdersType.ORDERS_HEADER_GM);
            Map<String, Object> resultMap    = orderService.getPaymentConf(vo);
            return Result.success(resultMap);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("物流信息展示")
    @PostMapping("/showLogistics")
    @HttpApiMethod(urlMapping = "app.order.logistics")
    public Result showLogistics(OrderVo vo)
    {
        try
        {
            OrderDubboService   orderService = handlerOrderContext.getOrderService(DictionaryConst.OrdersType.ORDERS_HEADER_GM);
            Map<String, Object> resultMap    = orderService.showLogistics(vo);
            return Result.success(resultMap);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("取消售后申请")
    @PostMapping("/cancleApply")
    @Idempotency
    public Result cancleApply(OrderVo vo, int id)
    {
        try
        {
            OrderDubboService   orderService = handlerOrderContext.getOrderService(DictionaryConst.OrdersType.ORDERS_HEADER_GM);
            Map<String, Object> resultMap    = orderService.cancleApply(vo.getStoreId(), id);
            return Result.success(resultMap);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("申请售后")
    @PostMapping("/ReturnData")
    @HttpApiMethod(urlMapping = "app.order.ReturnData")
//    @PreventDuplicateSubmit
    public Result returnData(ApplyReturnDataVo vo, MultipartFile image)
    {
        try
        {
            OrderDubboService orderService = null;
            if (Objects.isNull(vo.getoType()))
            {
                orderService = handlerOrderContext.getOrderService(DictionaryConst.OrdersType.ORDERS_HEADER_GM);
            }
            else
            {
                if
                (
                Objects.equals(DictionaryConst.OrdersType.ORDERS_HEADER_MS, vo.getoType())
                        || Objects.equals(DictionaryConst.OrdersType.ORDERS_HEADER_FS, vo.getoType())
                        || Objects.equals(DictionaryConst.OrdersType.ORDERS_HEADER_PS, vo.getoType())
                        || Objects.equals(DictionaryConst.OrdersType.ORDERS_HEADER_PT, vo.getoType())
                )
                {
                    vo.setoType(DictionaryConst.OrdersType.ORDERS_HEADER_GM);
                }
                orderService = handlerOrderContext.getOrderService(vo.getoType());
            }
            return Result.success(orderService.returnData(vo, image));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("售后订单列表")
    @PostMapping("/returnOrderList")
    @HttpApiMethod(urlMapping = "app.order.ReturnDataList")
    public Result returnOrderList(OrderVo vo)
    {
        try
        {
            OrderDubboService orderService = null;
            if (!Objects.isNull(vo.getType()))
            {
                orderService = handlerOrderContext.getOrderService(vo.getType());
            }
            else
            {
                orderService = handlerOrderContext.getOrderService(DictionaryConst.OrdersType.ORDERS_HEADER_GM);
            }
            Map<String, Object> resultMap = orderService.returnOrderList(vo);
            return Result.success(resultMap);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("确认收货")
    @PostMapping("/okOrder")
    @HttpApiMethod(urlMapping = "app.order.ok_Order")
    @Idempotency
    public Result okOrder(OrderVo vo)
    {
        try
        {
            OrderModel orderModel = publicOrderService.getOrderInfoById(vo.getStoreId(), vo.getOrderId(), null);
            vo.setsNo(orderModel.getsNo());
            OrderDubboService   orderService = handlerOrderContext.getOrderService(DictionaryConst.OrdersType.ORDERS_HEADER_GM);
            Map<String, Object> resultMap    = orderService.okOrder(vo);
            return Result.success(resultMap);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("点击退款按钮跳转界面")
    @PostMapping("/returnMethod")
    @HttpApiMethod(urlMapping = "app.order.return_method")
    public Result returnMethod(OrderVo vo)
    {
        try
        {
            OrderDubboService orderService = null;
            if (!Objects.isNull(vo.getType()) && vo.getType().equals(DictionaryConst.OrdersType.ORDERS_HEADER_VI))
            {
                orderService = handlerOrderContext.getOrderService(vo.getType());
            }
            else
            {
                orderService = handlerOrderContext.getOrderService(DictionaryConst.OrdersType.ORDERS_HEADER_GM);
            }
            Map<String, Object> resultMap = orderService.returnMethod(vo);
            return Result.success(resultMap);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("获取收货地址和快递信息")
    @PostMapping("/seeSend")
    @HttpApiMethod(urlMapping = "app.order.see_send")
    public Result seeSend(@ParamsMapping("store_id") int storeId, @ParamsMapping("pid") int productId)
    {
        try
        {
            OrderDubboService   orderService = handlerOrderContext.getOrderService(DictionaryConst.OrdersType.ORDERS_HEADER_GM);
            Map<String, Object> resultMap    = orderService.seeSend(storeId, productId);
            return Result.success(resultMap);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("保存快递基本信息")
    @PostMapping("/backSend")
    @HttpApiMethod(urlMapping = "app.order.back_send")
    @Idempotency
    public Result backSend(ReturnGoodsVo returnGoodsVo)
    {
        try
        {
            OrderDubboService   orderService = handlerOrderContext.getOrderService(DictionaryConst.OrdersType.ORDERS_HEADER_GM);
            Map<String, Object> resultMap    = orderService.backSend(returnGoodsVo);
            return Result.success(resultMap);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("查看提货码")
    @PostMapping("/seeExtractionCode")
    @HttpApiMethod(urlMapping = "app.order.see_extraction_code")
    public Result seeExtractionCode(OrderVo vo)
    {
        try
        {
            OrderDubboService orderService = null;
            if (!Objects.isNull(vo.getType()))
            {
                orderService = handlerOrderContext.getOrderService(vo.getType());
            }
            else
            {
                orderService = handlerOrderContext.getOrderService(DictionaryConst.OrdersType.ORDERS_HEADER_GM);
            }
            Map<String, Object> resultMap = orderService.seeExtractionCode(vo);
            return Result.success(resultMap);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("售后订单详情")
    @PostMapping("/returndetails")
    @HttpApiMethod(urlMapping = "app.order.Returndetail")
    public Result returndetails(RefundDetailsVo vo)
    {
        try
        {
            OrderDubboService   orderService = handlerOrderContext.getOrderService(DictionaryConst.OrdersType.ORDERS_HEADER_GM);
            Map<String, Object> resultMap    = orderService.returndetails(vo);
            return Result.success(resultMap);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("售后确认收货")
    @PostMapping("/confirmReceipt")
    @HttpApiMethod(urlMapping = "app.UserReturn.confirm_receipt")
    @Idempotency
    public Result confirmReceipt(ReturnConfirmReceiptVo vo)
    {
        try
        {
            OrderDubboService   orderService = handlerOrderContext.getOrderService(DictionaryConst.OrdersType.ORDERS_HEADER_GM);
            Map<String, Object> resultMap    = orderService.confirmReceipt(vo);
            return Result.success(resultMap);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("取消订单")
    @PostMapping("/cancleOrder")
    @HttpApiMethod(urlMapping = "app.order.removeOrder")
    @Idempotency
    public Result cancleOrder(OrderVo vo)
    {
        try
        {
            OrderModel orderModel = publicOrderService.getOrderInfoById(vo.getStoreId(), vo.getOrderId(), null);
            vo.setsNo(orderModel.getsNo());
            //分销走普通订单流程
            if (DictionaryConst.OrdersType.ORDERS_HEADER_FX.equalsIgnoreCase(orderModel.getOtype()) || DictionaryConst.OrdersType.ORDERS_HEADER_VI.equalsIgnoreCase(orderModel.getOtype()))
            {
                orderModel.setOtype(DictionaryConst.OrdersType.ORDERS_HEADER_GM);
            }
            OrderDubboService   orderService = handlerOrderContext.getOrderService(orderModel.getOtype());
            Map<String, Object> resultMap    = orderService.cancleOrder(vo);
            return Result.success(resultMap);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("提醒发货")
    @PostMapping("/remindDelivery")
    @HttpApiMethod(urlMapping = "app.order.delivery_delivery")
    @Idempotency
    public Result remindDelivery(OrderVo vo)
    {
        try
        {
            OrderDubboService   orderService = handlerOrderContext.getOrderService(DictionaryConst.OrdersType.ORDERS_HEADER_GM);
            Map<String, Object> resultMap    = orderService.remindDelivery(vo);
            return new Result((String) resultMap.get("code"), (String) resultMap.get("message"), new ArrayList<>());
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("撤销审核")
    @PostMapping("/cancelApplication")
    @HttpApiMethod(urlMapping = "app.order.Cancellation_of_application")
    @Idempotency
    public Result cancelApplication(CancleAfterSaleApplyVo vo)
    {

        try
        {
            OrderDubboService   orderService = handlerOrderContext.getOrderService(DictionaryConst.OrdersType.ORDERS_HEADER_GM);
            Map<String, Object> resultMap    = orderService.cancelApplication(vo);
            return new Result((String) resultMap.get("code"), (String) resultMap.get("message"), new ArrayList<>());
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("进入订单界面获取可用支付方式")
    @PostMapping("/getPayment")
    @HttpApiMethod(urlMapping = {"app.Order.getPayment"})
    public Result getPayment(OrderVo vo)
    {
        try
        {
            OrderDubboService   orderService = handlerOrderContext.getOrderService(DictionaryConst.OrdersType.ORDERS_HEADER_GM);
            Map<String, Object> resultMap    = orderService.getPayment(vo);
            return Result.success(resultMap);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("判断订单是否在进行支付")
    @PostMapping("/checkPay")
    @HttpApiMethod(apiKey = "app.order.checkIsPay")
    public Result checkIsPay(String sNo)
    {
        try
        {
            if (redisUtil.hasKey("lock:order:" + sNo))
            {
                String value = redisUtil.get("lock:order:" + sNo).toString();
                if (Objects.equals(value,"payWait"))
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DDZZCLZ, "订单正在处理中");
                }
            }
            return Result.success();
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("快递100电子面单发货回调接口")
    @PostMapping("/kuaidi100CouldNotify")
    public void kuaidi100CouldNotify(HttpServletRequest request, HttpServletResponse response)
    {
        try
        {
            publicOrderService.kuaidi100CouldNotify(request);
            String message = "{\"result\": \"true\", \"returnCode\": \"200\",\"message\": \"提交成功\"}";
            sendSuccess(response,message);
        }
        catch (Exception e)
        {
            logger.error("快递100电子面单发货回调失败:{}", e.getMessage());
            String message = "{\"result\": \"false\", \"returnCode\": \"500\",\"message\": \"提交失败\"}";
            sendFailure(response,message);
        }
    }

    @ApiOperation("17track快递回调")
    @PostMapping("/trackNotify")
    public void trackNotify(HttpServletRequest request, HttpServletResponse response)
    {
        try
        {
            publicOrderService.trackNotify(request);
            String message = "{\"code\": \"200\"}";
            sendSuccess(response,message);
        }
        catch (Exception e)
        {
            logger.error("17track回调失败:{}", e.getMessage());
            String message = "{\"code\": \"500\"}";
            sendFailure(response,message);
        }
    }


    @ApiOperation("线下支付上传凭证")
    @PostMapping("/upload_credentials")
    @HttpApiMethod(apiKey = "app.order.upload_credentials")
    public Result uploadCredentials(OrderVo vo)
    {
        try
        {
            publicOrderService.uploadCredentials(vo);
            return Result.success();
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    /**
     * @param response
     */
    private void sendFailure(HttpServletResponse response,String failureMsg)
    {
        // 设置响应状态码为非200的值，比如500，表示失败
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        // 返回微信要求的失败响应XML格式数据
        writeResponseData(response, failureMsg);
    }

    private void sendSuccess(HttpServletResponse response,String message)
    {
        // 设置响应状态码为200，表示成功
        response.setStatus(HttpServletResponse.SC_OK);
        writeResponseData(response, message);
    }

    /**
     * @param response
     * @param msg
     */
    private void writeResponseData(HttpServletResponse response, String msg)
    {
        PrintWriter writer = null;
        try
        {
            response.setContentType("application/json;charset=UTF-8");
            writer = response.getWriter();
            if (StringUtils.isNotEmpty(msg))
            {
                writer.write(msg);
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (writer != null)
            {
                writer.close();
            }
        }
    }
}
