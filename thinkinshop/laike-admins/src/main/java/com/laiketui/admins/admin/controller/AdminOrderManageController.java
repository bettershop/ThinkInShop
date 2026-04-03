package com.laiketui.admins.admin.controller;

import com.alibaba.fastjson2.JSON;
import com.laiketui.admins.api.admin.order.AdminCommentsService;
import com.laiketui.admins.api.admin.order.AdminIndexService;
import com.laiketui.admins.api.admin.order.AdminRefundService;
import com.laiketui.core.domain.Result;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.core.lktconst.gwconst.LaiKeGWConst;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.main.RefundVo;
import com.laiketui.domain.vo.order.*;
import com.laiketui.domain.vo.query.GetCommentsDetailInfoVo;
import com.laiketui.domain.vo.virtual.WriteRecordVo;
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
 * @date 2021/1/5 15:52
 */
@Api(tags = "后台-订单管理")
@RestController
@RequestMapping("/admin/order")
public class AdminOrderManageController
{

    @Autowired
    private AdminRefundService adminRefundService;

    @Autowired
    private AdminCommentsService adminCommentsService;

    @Autowired
    private AdminIndexService adminIndexService;

    @ApiOperation("获取售后列表")
    @PostMapping("/getRefundList")
    @HttpApiMethod(apiKey = "admin.order.getRefundList")
    public Result getRefundList(RefundQueryVo vo, HttpServletResponse response)
    {
        try
        {
            //vo.setOrderType(DictionaryConst.OrdersType.ORDERS_HEADER_GM);
            Map<String, Object> ret = adminRefundService.getRefundList(vo, response);
            return ret == null ? Result.exportFile() : Result.success(ret);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("售后 通过/拒绝")
    @PostMapping("/examine")
    @HttpApiMethod(apiKey = "admin.order.examine")
    public Result examine(RefundVo vo)
    {
        try
        {
            return Result.success(adminRefundService.examine(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("获取评论列表")
    @PostMapping("/getCommentsInfo")
    @HttpApiMethod(apiKey = "admin.order.getCommentsInfo")
    public Result getCommentsInfo(CommentsInfoVo vo)
    {
        try
        {
            return Result.success(adminCommentsService.getCommentsInfo(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("获取评论明细")
    @PostMapping("/getCommentsDetailInfoById")
    @HttpApiMethod(apiKey = "admin.order.getCommentsDetailInfoById")
    public Result getCommentsDetailInfoById(CommentsDetailInfoVo vo, int cid)
    {
        try
        {
            return Result.success(adminCommentsService.getCommentsDetailInfoById(vo, cid));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("获取评论详情回复列表")
    @PostMapping("/getCommentReplyList")
    @HttpApiMethod(apiKey = "admin.order.getCommentReplyList")
    public Result getCommentReplyList(GetCommentsDetailInfoVo vo)
    {
        try
        {
            return Result.success(adminCommentsService.getCommentReplyList(vo));
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
    @HttpApiMethod(apiKey = "admin.order.delCommentReply")
    public Result delCommentReply(MainVo vo, int id)
    {
        try
        {
            adminCommentsService.delCommentReply(vo, id);
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
    @HttpApiMethod(apiKey = "admin.order.replyComments")
    public Result replyComments(MainVo vo, int commentId, String commentText)
    {
        try
        {
            return Result.success(adminCommentsService.replyComments(vo, commentId, commentText));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("修改评论")
    @PostMapping("/updateCommentsDetailInfoById")
    @HttpApiMethod(apiKey = "admin.order.updateCommentsDetailInfoById")
    public Result updateCommentsDetailInfoById(UpdateCommentsInfoVo vo)
    {
        try
        {
            adminCommentsService.updateCommentsDetailInfoById(vo);
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
    @HttpApiMethod(apiKey = "admin.order.delComments")
    public Result delComments(MainVo vo, int commentId)
    {
        try
        {
            return Result.success(adminCommentsService.delComments(vo, commentId));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("平台订单详情")
    @PostMapping("/orderDetailsInfo")
    @HttpApiMethod(apiKey = "admin.order.orderDetailsInfo")
    public Result orderDetailsInfo(AdminOrderDetailVo vo)
    {
        try
        {
            return Result.success(adminIndexService.orderDetailsInfo(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("查询核销门店")
    @PostMapping("/getMch_store")
    @HttpApiMethod(apiKey = "admin.order.getMch_store")
    public Result getMchStore(MainVo vo, Integer mchId, Integer pid, String sNo)
    {
        try
        {
            return Result.success(adminIndexService.getMchStore(vo, mchId, pid, sNo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("查询核销记录")
    @PostMapping("/getWrite_record")
    @HttpApiMethod(apiKey = "admin.order.getWrite_record")
    public Result getWriteRecord(WriteRecordVo vo)
    {
        try
        {
            return Result.success(adminIndexService.getWriteRecord(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("后台保存编辑订单信息")
    @PostMapping("/saveEditOrder")
    @HttpApiMethod(apiKey = "admin.order.saveEditOrder")
    public Result saveEditOrder(EditOrderVo vo)
    {
        try
        {
            adminIndexService.saveEditOrder(vo);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("跳转编辑订单界面")
    @PostMapping("/editOrderView")
    @HttpApiMethod(apiKey = "admin.order.editOrderView")
    public Result editOrderView(OrderModifyVo vo)
    {
        try
        {
            return Result.success(adminIndexService.editOrderView(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    /*@ApiOperation("发货提交")
    @PostMapping("/deliverySave")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "exId", value = "快递公司", required = true, dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "exNo", value = "快递单号", required = true, dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "orderDetailIds", value = "订单明细id集", required = true, dataType = "string", paramType = "form"),
    })
    @HttpApiMethod(apiKey = "admin.order.deliverySave")
    public Result deliverySave(MainVo vo,String orderDetailIds) {
        try {
            adminIndexService.deliverySave(vo, orderDetailIds);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        } catch (LaiKeAPIException e) {
            return Result.error(e.getCode(), e.getMessage());
        }
    }*/

    @ApiOperation("新发货提交，后台端发货统一接口")
    @PostMapping("/deliverySave")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "exId", value = "快递公司", required = true, dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "exNo", value = "快递单号", required = true, dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "orderDetailIds", value = "订单明细id集", required = true, dataType = "string", paramType = "form"),
    })
    @HttpApiMethod(apiKey = "admin.order.UnifiedShipment")
    public Result deliverySave(MainVo vo, String list)
    {
        try
        {
            ShipDataVo shippingData = JSON.parseObject(list, ShipDataVo.class);
            adminIndexService.delivery(vo, shippingData);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("商家自配发货提交")
    @PostMapping("/deliverySaveForStoreSelf")
    @HttpApiMethod(apiKey = "admin.order.deliverySaveForStoreSelf")
    public Result deliverySave(MainVo vo, String phone, String courier_name, String sNo)
    {
        try
        {
            adminIndexService.adminDeliveryForStoreSelf(vo, phone, courier_name, sNo);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("发货界面")
    @PostMapping("/deliveryView")
    @HttpApiMethod(apiKey = "admin.order.deliveryView")
    public Result deliveryView(AdminDeliveryVo adminDeliveryVo)
    {
        try
        {
            return Result.success(adminIndexService.deliveryView(adminDeliveryVo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("代客下单-结算")
    @PostMapping("/valetOrderSettlement")
    @HttpApiMethod(apiKey = "admin.valetOrder.Settlement")
    public Result valetOrderSettlement(HelpOrderVo vo)
    {
        try
        {
            return Result.success(adminIndexService.valetOrderSettlement(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("代客下单")
    @PostMapping("/helpOrder")
    @HttpApiMethod(apiKey = "admin.order.helpOrder")
    public Result helpOrder(HelpOrderVo vo)
    {
        try
        {
            return Result.success(adminIndexService.helpOrder(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("搜索物流公司")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "express", value = "物流名称", required = true, dataType = "string", paramType = "form")
    })
    @PostMapping("/searchExpress")
    @HttpApiMethod(apiKey = "admin.order.searchExpress")
    public Result searchExpress(String express)
    {
        try
        {
            return Result.success(adminIndexService.searchExpress(express));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("订单打印")
    @PostMapping("/orderPrint")
    @HttpApiMethod(apiKey = "admin.order.orderPrint")
    public Result orderPrint(AdminOrderVo orderVo)
    {
        try
        {
            return Result.success(adminIndexService.orderPrint(orderVo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("删除订单")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "orders", value = "订单号集", required = true, dataType = "string", paramType = "form")
    })
    @PostMapping("/del")
    @HttpApiMethod(apiKey = "admin.order.del")
    public Result del(MainVo vo, String orders)
    {
        try
        {
            return Result.success(adminIndexService.del(vo, orders));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("关闭订单")
    @PostMapping("/close")
    @HttpApiMethod(apiKey = "admin.order.close")
    public Result close(AdminOrderVo orderVo)
    {
        try
        {
            return Result.success(adminIndexService.close(orderVo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("订单列表")
    @PostMapping("/index")
    @HttpApiMethod(apiKey = "admin.order.index")
    public Result index(AdminOrderListVo vo, HttpServletResponse response)
    {
        try
        {
            Map<String, Object> ret = adminIndexService.index(vo, response);
            return ret == null ? Result.exportFile() : Result.success(ret);
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
    @HttpApiMethod(apiKey = "admin.order.kuaidishow")
    public Result kuaidishow(MainVo vo, String orderno)
    {
        try
        {
            return Result.success(adminIndexService.kuaidishow(vo, orderno));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("订单统计")
    @PostMapping("/orderCount")
    @HttpApiMethod(apiKey = "admin.order.orderCount")
    public Result orderCount(MainVo vo)
    {
        try
        {
            return Result.success(adminIndexService.orderCount(vo));
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
    @HttpApiMethod(apiKey = "admin.order.batchDelivery")
    public Result batchDelivery(MainVo vo, List<MultipartFile> image)
    {
        try
        {
            return Result.success(adminIndexService.batchDelivery(vo, image));
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
    @HttpApiMethod(urlMapping = {"admin.order.deliveryList"})
    public Result deliveryList(MainVo vo, String fileName, Integer status, String startDate, String endDate, HttpServletResponse response)
    {
        try
        {
            return Result.success(adminIndexService.deliveryList(vo, fileName, status, startDate, endDate, response));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("删除批量发货记录")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "id", dataType = "string", paramType = "form")
    })
    @PostMapping("/delDelivery")
    @HttpApiMethod(apiKey = "admin.order.delDelivery")
    public Result delDelivery(MainVo vo, String id)
    {
        try
        {
            adminIndexService.delDelivery(vo, id);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
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
    @HttpApiMethod(apiKey = "admin.Order.VerificationExtractionCode")
    public Result verificationExtractionCode(MainVo vo, Integer orderId, String extractionCode, Integer mch_store_id, Integer pid)
    {
        try
        {
            return Result.success(adminIndexService.verificationExtractionCode(vo, orderId, extractionCode, mch_store_id, pid));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("检验验证码")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "orderId", value = "订单id", dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "extractionCode", value = "提货码", required = true, dataType = "string", paramType = "form")
    })
    @PostMapping("/testExtractionCode")
    @HttpApiMethod(apiKey = "admin.Order.testExtractionCode")
    public Result testExtractionCode(MainVo vo, Integer orderId, String extractionCode, Integer mch_store_id)
    {
        try
        {
            return Result.success(adminIndexService.testExtractionCode(vo, orderId, extractionCode, mch_store_id));
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
    @HttpApiMethod(apiKey = "admin.order.ShippingRecords")
    public Result ShippingRecords(GetExpressDeliveryListVo vo)
    {
        try
        {
            return Result.success(adminIndexService.ShippingRecords(vo));
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
    @HttpApiMethod(apiKey = "admin.order.getPro")
    public Result expressGetPro(MainVo vo, Integer id, String name)
    {
        try
        {
            return Result.success(adminIndexService.expressGetPro(vo, id, name));
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
    @HttpApiMethod(apiKey = "admin.order.CancelElectronicWaybill")
    public Result CancelElectronicWaybill(MainVo vo, Integer id)
    {
        try
        {
            String code = LaiKeGWConst.GW_SUCCESS;
            String msg = adminIndexService.CancelElectronicWaybill(vo, id);
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
    @HttpApiMethod(apiKey = "admin.order.FaceSheetSend")
    public Result FaceSheetSend(MainVo vo, Integer exId, String orderDetailIds)
    {
        try
        {
            adminIndexService.FaceSheetSend(vo, exId, orderDetailIds);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("极速退款")
    @PostMapping("/quickRefund")
    @HttpApiMethod(apiKey = "admin.order.CancellationOfOrder")
    public Result quickRefund(RefundVo vo)
    {
        try
        {
            Map<String,Object> res = adminRefundService.quickRefund(vo);
            return Result.success(res);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("电子面单状态修改")
    @PostMapping("/updateExpressDeliveryStatus")
    @HttpApiMethod(apiKey = "admin.order.updateExpressDeliveryStatus")
    public Result updateExpressDeliveryStatus(MainVo vo,String ids)
    {
        try
        {
            adminIndexService.updateExpressDeliveryStatus(vo, ids);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("电子面单复打")
    @PostMapping("/overridePrint")
    @HttpApiMethod(apiKey = "admin.order.overridePrint")
    public Result overridePrint(MainVo vo,String ids)
    {
        try
        {
            adminIndexService.overridePrint(vo, ids);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }
}
