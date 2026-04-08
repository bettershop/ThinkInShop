package com.laiketui.admins.mch.controller;

import com.laiketui.admins.api.mch.MchOrderService;
import com.laiketui.core.domain.Result;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.core.lktconst.gwconst.LaiKeGWConst;
import com.laiketui.core.utils.tool.DataUtils;
import com.laiketui.core.utils.tool.StringUtils;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.main.RefundVo;
import com.laiketui.domain.vo.order.*;
import com.laiketui.domain.vo.pc.MchPcOrderIndexVo;
import com.laiketui.domain.vo.pc.MchPcReturnOrderVo;
import com.laiketui.domain.vo.query.GetCommentsDetailInfoVo;
import com.laiketui.root.annotation.HttpApiMethod;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 订单管理
 *
 * @author Trick
 * @date 2021/6/2 17:25
 */
@Api(tags = "订单管理")
@RestController
@RequestMapping("/admin/mch/order/")
public class MchOrderController
{

    @Autowired
    private MchOrderService mchOrderService;

    @ApiOperation("获取订单列表")
    @PostMapping("/index")
    @HttpApiMethod(apiKey = "mch.Mch.Order.Index")
    public Result index(AdminOrderListVo vo, HttpServletResponse response)
    {
        try
        {
            return Result.success(mchOrderService.index(vo, response));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("退款列表")
    @PostMapping("/returnList")
    @HttpApiMethod(urlMapping = {"mch.Mch.Order.ReturnList", "mch.Mch.Order.GetRefundList"})
    public Result returnList(MchPcReturnOrderVo vo, HttpServletResponse response)
    {
        try
        {
            vo.setResponse(response);
            return Result.success(mchOrderService.returnList(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("订单打印")
    @PostMapping("/orderPrint")
    @HttpApiMethod(apiKey = "mch.Mch.Order.OrderPrint")
    public Result orderPrint(AdminOrderVo orderVo)
    {
        try
        {
            return Result.success(mchOrderService.orderPrint(orderVo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("订单统计")
    @PostMapping("/orderCount")
    @HttpApiMethod(apiKey = "mch.Mch.Order.OrderCount")
    public Result orderCount(MchPcOrderIndexVo vo)
    {
        try
        {
            return Result.success(mchOrderService.orderCount(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("批量发货")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "files", value = "文件集", required = true, dataType = "file", paramType = "form")
    })
    @PostMapping("/batchDelivery")
    @HttpApiMethod(apiKey = "mch.Mch.Order.BatchDelivery")
    public Result batchDelivery(MainVo vo, List<MultipartFile> image)
    {
        try
        {
            return Result.success(mchOrderService.batchDelivery(vo, image));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("批量记录列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "orderno", value = "订单号", dataType = "string", paramType = "form"),
    })
    @PostMapping("/deliverList")
    @HttpApiMethod(apiKey = "mch.Mch.Order.DeliverList")
    public Result deliverList(MainVo vo, String orderno)
    {
        try
        {
            return Result.success(mchOrderService.deliverList(vo, orderno));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("获取代发货记录列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "fileName", value = "文件名称", dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "status", value = "状态 1=发货成功 0=发货失败", dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "startDate", value = "开始时间", dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "endDate", value = "结束时间", dataType = "string", paramType = "form"),
    })
    @PostMapping("/deliveryList")
    @HttpApiMethod(urlMapping = {"mch.Mch.Order.DeliveryList", "admin.mch.order.deliveryView"})
    public Result deliveryList(MainVo vo, String fileName, Integer status, String startDate, String endDate, HttpServletResponse response)
    {
        try
        {
            return Result.success(mchOrderService.deliveryList(vo, fileName, status, startDate, endDate, response));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("删除发货记录")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "id", dataType = "string", paramType = "form")
    })
    @PostMapping("/delDelivery")
    @HttpApiMethod(apiKey = "mch.Mch.Order.DelDelivery")
    public Result delDelivery(MainVo vo, String id)
    {
        try
        {
            mchOrderService.delDelivery(vo, id);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("发货列表显示")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "expressName", value = "快递名称", dataType = "string", paramType = "form"),
    })
    @PostMapping("/searchExpress")
    @HttpApiMethod(apiKey = "mch.Mch.Order.SearchExpress")
    public Result searchExpress(MainVo vo, String expressName)
    {
        try
        {
            return Result.success(mchOrderService.searchExpress(vo, expressName));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("发货")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "sNo", value = "订单号", required = true, dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "expressId", value = "快递公司id", required = true, dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "courierNum", value = "快递单号", required = true, dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "orderListId", value = "订单明细id集", required = true, dataType = "string", paramType = "form")
    })
    @PostMapping("/deliver")
    @HttpApiMethod(apiKey = "mch.Mch.Order.Deliver")
    public Result deliver(MainVo vo, String sNo, Integer expressId,
                          String courierNum, String orderListId, String phone, String courier_name)
    {
        try
        {
            if (StringUtils.isNotEmpty(phone))
            {
                return Result.success(mchOrderService.selfSend(vo, phone, courier_name, sNo, orderListId));
            }
            else
            {
                return Result.success(mchOrderService.deliver(vo, sNo, expressId, courierNum, orderListId));
            }
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("订单详情")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "sNo", value = "订单号", required = true, dataType = "string", paramType = "form")
    })
    @PostMapping("/orderDetails")
    @HttpApiMethod(apiKey = "admin.mch.order.orderDetails")
    public Result orderDetails(MainVo vo, String sNo)
    {
        try
        {
            return Result.success(mchOrderService.orderDetails(vo, sNo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("订单编辑回显")
    @PostMapping("/editeOrderInfo")
    @HttpApiMethod(apiKey = "mch.Mch.Order.EditeOrderInfo")
    public Result editeOrderInfo(OrderModifyVo orderVo)
    {
        try
        {
            return Result.success(mchOrderService.editeOrderInfo(orderVo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("后台保存编辑订单信息")
    @PostMapping("/saveEditOrder")
    @HttpApiMethod(apiKey = "mch.Mch.Order.SaveEditOrder")
    public Result saveEditOrder(EditOrderVo vo)
    {
        try
        {
            mchOrderService.saveEditOrder(vo);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("批量删除订单")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "ordernos", value = "订单号", required = true, dataType = "string", paramType = "form")
    })
    @PostMapping("/delOrder")
    @HttpApiMethod(apiKey = "mch.Mch.Order.DelOrder")
    public Result delOrder(MainVo vo, String[] ordernos)
    {
        try
        {
            return Result.success(mchOrderService.delOrder(vo, DataUtils.convertToList(ordernos)));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("获取订单物流信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "orderno", value = "订单号", required = true, dataType = "string", paramType = "form")
    })
    @PostMapping("/kuaidishow")
    @HttpApiMethod(apiKey = "mch.Mch.Order.Kuaidishow")
    public Result kuaidishow(MainVo vo, String orderno)
    {
        try
        {
            return Result.success(mchOrderService.kuaidishow(vo, orderno));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("验证码提货（自提）获取其商品详情")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "orderId", value = "订单id", dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "extractionCode", value = "提货码", required = true, dataType = "string", paramType = "form")
    })
    @PostMapping("/getGoodsInfoByExtractionCode")
    @HttpApiMethod(apiKey = "mch.Mch.Order.getGoodsInfoByExtractionCode")
    public Result getGoodsInfoByExtractionCode(MainVo vo, Integer orderId, String extractionCode)
    {
        try
        {
            return Result.success(mchOrderService.getGoodsInfoByExtractionCode(vo, orderId, extractionCode));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("验证码提货（自提）")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "orderId", value = "订单id", dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "extractionCode", value = "提货码", required = true, dataType = "string", paramType = "form")
    })
    @PostMapping("/verificationExtractionCode")
    @HttpApiMethod(apiKey = "mch.Mch.Order.VerificationExtractionCode")
    public Result verificationExtractionCode(MainVo vo, Integer orderId, String extractionCode)
    {
        try
        {
            return Result.success(mchOrderService.verificationExtractionCode(vo, orderId, extractionCode));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("售后订单详情")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "售后id", dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "orderNo", value = "订单号", dataType = "string", paramType = "form"),
    })
    @PostMapping("/refundPageById")
    @HttpApiMethod(apiKey = "mch.Mch.Order.RefundPageById")
    public Result refundPageById(MainVo vo, Integer id, String orderNo)
    {
        try
        {
            return Result.success(mchOrderService.refundPageById(vo, id, orderNo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("售后审核")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "售后id", required = true, dataType = "int", paramType = "form")
    })
    @PostMapping("/examine")
    @HttpApiMethod(apiKey = "mch.Mch.Order.Examine")
    public Result examine(RefundVo vo)
    {
        try
        {
            mchOrderService.examine(vo);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("获取评论列表")
    @PostMapping("/getCommentsInfo")
    @HttpApiMethod(apiKey = "mch.Mch.Order.getCommentsInfo")
    public Result getCommentsInfo(CommentsInfoVo vo)
    {
        try
        {
            return Result.success(mchOrderService.getCommentsInfo(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("获取评论明细")
    @PostMapping("/getCommentsDetailInfoById")
    @HttpApiMethod(apiKey = "mch.Mch.Order.getCommentsDetailInfoById")
    public Result getCommentsDetailInfoById(CommentsDetailInfoVo vo, int cid)
    {
        try
        {
            return Result.success(mchOrderService.getCommentsDetailInfoById(vo, cid));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("获取评论详情回复列表")
    @PostMapping("/getCommentReplyList")
    @HttpApiMethod(apiKey = "admin.mch.order.getCommentReplyList")
    public Result getCommentReplyList(GetCommentsDetailInfoVo vo)
    {
        try
        {
            return Result.success(mchOrderService.getCommentReplyList(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("删除评论详情回复列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "回复列表", required = true, dataType = "int", paramType = "form"),
    })
    @PostMapping("/delCommentReply")
    @HttpApiMethod(apiKey = "mch.Mch.Order.delCommentReply")
    public Result delCommentReply(MainVo vo, int id)
    {
        try
        {
            mchOrderService.delCommentReply(vo, id);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("回复评论")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "commentId", value = "评论id", dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "commentText", value = "评论内容", dataType = "string", paramType = "form")
    })
    @PostMapping("/replyComments")
    @HttpApiMethod(apiKey = "mch.Mch.Order.replyComments")
    public Result replyComments(MainVo vo, int commentId, String commentText)
    {
        try
        {
            return Result.success(mchOrderService.replyComments(vo, commentId, commentText));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("修改评论")
    @PostMapping("/updateCommentsDetailInfoById")
    @HttpApiMethod(apiKey = "mch.Mch.Order.updateCommentsDetailInfoById")
    public Result updateCommentsDetailInfoById(UpdateCommentsInfoVo vo)
    {
        try
        {
            mchOrderService.updateCommentsDetailInfoById(vo);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("删除评论")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "commentId", value = "评论id", required = true, dataType = "int", paramType = "form")
    })
    @PostMapping("/delComments")
    @HttpApiMethod(apiKey = "mch.Mch.Order.delComments")
    public Result delComments(MainVo vo, int commentId)
    {
        try
        {
            return Result.success(mchOrderService.delComments(vo, commentId));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("获取订单发货列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "sNo", value = "订单号", dataType = "String", paramType = "form")
    })
    @PostMapping("/ShippingRecords")
    @HttpApiMethod(apiKey = "mch.Mch.Order.ShippingRecords")
    public Result ShippingRecords(GetExpressDeliveryListVo vo)
    {
        try
        {
            return Result.success(mchOrderService.ShippingRecords(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("查看发货记录商品")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "发货记录id", dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "name", value = "商品名称", dataType = "string", paramType = "form")
    })
    @PostMapping("/expressGetPro")
    @HttpApiMethod(apiKey = "mch.Mch.Order.getPro")
    public Result expressGetPro(MainVo vo, Integer id, String name)
    {
        try
        {
            return Result.success(mchOrderService.expressGetPro(vo, id, name));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("取消电子面单发货")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "发货记录id", dataType = "int", paramType = "form")
    })
    @PostMapping("/CancelElectronicWaybill")
    @HttpApiMethod(apiKey = "mch.Mch.order.CancelElectronicWaybill")
    public Result CancelElectronicWaybill(MainVo vo, Integer id)
    {
        try
        {
            String code = LaiKeGWConst.GW_SUCCESS;
            String msg = mchOrderService.CancelElectronicWaybill(vo, id);
            if (!Objects.equals(msg,"操作成功"))
            {
                code = "0000000";
            }
            return Result.success(code,msg);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("电子面单发货")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "exId", value = "快递公司", required = true, dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "orderDetailIds", value = "订单明细id集", required = true, dataType = "string", paramType = "form")
    })
    @PostMapping("/FaceSheetSend")
    @HttpApiMethod(apiKey = "mch.Mch.order.FaceSheetSend")
    public Result FaceSheetSend(MainVo vo, Integer exId, String orderDetailIds)
    {
        try
        {
            mchOrderService.FaceSheetSend(vo, exId, orderDetailIds);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("新发货提交，后台端发货统一接口")
    @PostMapping("/deliverySave")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "exId", value = "快递公司", required = true, dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "exNo", value = "快递单号", required = true, dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "orderDetailIds", value = "订单明细id集", required = true, dataType = "string", paramType = "form"),
    })
    @HttpApiMethod(apiKey = "mch.Mch.Order.UnifiedShipment")
    public Result deliverySave(MainVo vo, String list)
    {
        try
        {
            mchOrderService.deliverySave(vo, list);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("分账记录")
    @PostMapping("/divideRecord")
    @HttpApiMethod(urlMapping = "mch.Mch.DivideAccount.divideRecord")
    public Result divideRecord(MainVo vo, Integer mchId, String condition, String startDate, String endDate, HttpServletResponse response)
    {
        try
        {
            return Result.success(mchOrderService.divideRecord(vo, mchId, condition, startDate, endDate, response));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("极速退款")
    @PostMapping("/quickRefund")
    @HttpApiMethod(apiKey = "mch.Mch.order.CancellationOfOrder")
    public Result quickRefund(RefundVo vo)
    {
        try
        {
            Map<String,Object> res = mchOrderService.quickRefund(vo);
            return Result.success(res);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("凭证审核")
    @PostMapping("/offlineReview")
    @HttpApiMethod(apiKey = "mch.Mch.order.offlineReview")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "sNo", value = "订单编号", required = true, dataType = "String", paramType = "form"),
            @ApiImplicitParam(name = "reason_for_rejection", value = "拒绝原因",  dataType = "String", paramType = "form"),
            @ApiImplicitParam(name = "review_status", value = "审核状态 2:通过 3：拒绝", required = true, dataType = "Integer", paramType = "form"),
    })
    public Result offlineReview(MainVo vo,String sNo,String reason_for_rejection,Integer review_status)
    {
        try
        {
            mchOrderService.offlineReview(vo,sNo,reason_for_rejection,review_status);
            return Result.success();
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }
}
