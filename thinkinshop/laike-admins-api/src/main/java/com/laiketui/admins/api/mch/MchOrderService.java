package com.laiketui.admins.api.mch;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.main.RefundVo;
import com.laiketui.domain.vo.order.*;
import com.laiketui.domain.vo.pc.MchPcReturnOrderVo;
import com.laiketui.domain.vo.query.GetCommentsDetailInfoVo;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * 订单管理
 *
 * @author Trick
 * @date 2021/6/2 17:26
 */
public interface MchOrderService
{

    /**
     * 订单列表
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/6/2 17:27
     */
    Map<String, Object> index(AdminOrderListVo vo, HttpServletResponse response) throws LaiKeAPIException;

    /**
     * 商家自配
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/6/2 17:27
     */
    Map<String, Object> selfSend(MainVo vo, String phone, String courier_name, String sNo, String orderListId);

    /**
     * 订单发货
     *
     * @param vo             -
     * @param orderDetailIds -
     * @throws LaiKeAPIException-
     * @author Administrator
     * @date 2021/8/2 14:42
     */
    void deliverySave(MainVo vo, String orderDetailIds) throws LaiKeAPIException;

    /**
     * 商家端极速退款
     * @param vo
     * @return
     */
    Map<String, Object> quickRefund(RefundVo vo);

    /**
     * 线下支付凭证审核
     * @param sNo
     * @param reasonForRejection
     * @param reviewStatus
     */
    void offlineReview(MainVo vo,String sNo, String reasonForRejection, Integer reviewStatus) throws LaiKeAPIException;


    interface SelfLiftingType
    {
        Integer ALL             = 0;
        /**
         * 实物订单
         */
        Integer MATERIAL_OBJECT = 1;
        /**
         * 自提订单
         */
        Integer SELF_MENTION    = 2;
        /**
         * 虚拟订单
         */
        Integer FICTITIOUS      = 3;
        /**
         * 活动订单
         */
        Integer ACTIVITY        = 4;
        /**
         * 积分订单
         */
        Integer INTEGRAL        = 7;
        /**
         * 秒杀订单
         */
        Integer SECKILL         = 8;
    }


    /**
     * 退款列表
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/6/3 16:20
     */
    Map<String, Object> returnList(MchPcReturnOrderVo vo) throws LaiKeAPIException;

    interface OrderStatusEnum
    {
        /**
         * 待审核
         */
        Integer TO_BE_REVIEWED   = 0;
        /**
         * 退款中
         */
        Integer IN_REFUND        = 1;
        /**
         * 退款成功
         */
        Integer REFUND_SUCCESS   = 2;
        /**
         * 退款失败
         */
        Integer REFUND_FAILED    = 3;
        /**
         * 换货中
         */
        Integer IN_EXCHANGE      = 4;
        /**
         * 换货成功
         */
        Integer EXCHANGE_SUCCESS = 5;
        /**
         * 换货失败
         */
        Integer EXCHANGE_FAILED  = 6;
    }

    /**
     * 订单打印
     *
     * @param orderVo -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/12/9 14:10
     */
    List<Map<String, Object>> orderPrint(AdminOrderVo orderVo) throws LaiKeAPIException;

    /**
     * 订单统计
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/12/9 9:49
     */
    Map<String, Object> orderCount(MainVo vo) throws LaiKeAPIException;

    /**
     * 批量发货
     *
     * @param vo    -
     * @param image -
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/6/3 17:19
     */
    boolean batchDelivery(MainVo vo, List<MultipartFile> image) throws LaiKeAPIException;


    /**
     * 发货列表
     *
     * @param vo        -
     * @param fileName  -
     * @param status    -
     * @param startDate -
     * @param endDate   -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/12/8 20:45
     */
    Map<String, Object> deliveryList(MainVo vo, String fileName, Integer status, String startDate, String endDate, HttpServletResponse response) throws LaiKeAPIException;

    /**
     * 删除发货记录
     *
     * @param id -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/12/8 21:04
     */
    void delDelivery(MainVo vo, String id) throws LaiKeAPIException;

    /**
     * 订单发货列表
     *
     * @param vo      -
     * @param orderNo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/6/3 17:19
     */
    Map<String, Object> deliverList(MainVo vo, String orderNo) throws LaiKeAPIException;


    /**
     * 订单发货
     *
     * @param vo          -
     * @param sNo         -
     * @param expressId   -
     * @param courierNum  -
     * @param orderListId -
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/6/3 17:49
     */
    Map<String, Object> deliver(MainVo vo, String sNo, Integer expressId,
                                String courierNum, String orderListId) throws LaiKeAPIException;


    /**
     * 订单详情
     *
     * @param vo  -
     * @param sNo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/6/3 18:14
     */
    Map<String, Object> orderDetails(MainVo vo, String sNo) throws LaiKeAPIException;


    /**
     * 编辑订单详情
     *
     * @param orderVo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/6/4 14:42
     */
    Map<String, Object> editeOrderInfo(OrderModifyVo orderVo) throws LaiKeAPIException;

    /**
     * 保存编辑订单
     *
     * @param orderVo -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021-12-09 15:32:08
     */
    void saveEditOrder(EditOrderVo orderVo) throws LaiKeAPIException;

    /**
     * 删除订单
     *
     * @param vo       -
     * @param ordernos -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/6/4 15:01
     */
    Map<String, Object> delOrder(MainVo vo, List<String> ordernos) throws LaiKeAPIException;

    /**
     * 获取快递公司列表
     *
     * @param vo      -
     * @param express -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/12/9 14:46
     */
    Map<String, Object> searchExpress(MainVo vo, String express) throws LaiKeAPIException;

    /**
     * 获取订单物流信息
     *
     * @param vo      -
     * @param orderno -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/6/4 15:29
     */
    Map<String, Object> kuaidishow(MainVo vo, String orderno) throws LaiKeAPIException;

    /**
     * 验证码提货（自提）获取其商品详情
     *
     * @param vo
     * @param orderId
     * @param extractionCode
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> getGoodsInfoByExtractionCode(MainVo vo, Integer orderId, String extractionCode) throws LaiKeAPIException;

    /**
     * 验证码提货
     *
     * @param vo             -
     * @param orderId        -
     * @param extractionCode -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021-12-09 16:32:08
     */
    Map<String, Object> verificationExtractionCode(MainVo vo, Integer orderId, String extractionCode) throws LaiKeAPIException;

    /**
     * 售后订单详情
     *
     * @param vo      -
     * @param id      -
     * @param orderNo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/6/4 16:03
     */
    Map<String, Object> refundPageById(MainVo vo, Integer id, String orderNo) throws LaiKeAPIException;


    /**
     * 售后审核
     *
     * @param vo -
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/6/9 11:02
     */
    void examine(RefundVo vo) throws LaiKeAPIException;

    /**
     * 获取评论列表
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author sunH
     * @date 2023/9/11 16:07
     */
    Map<String, Object> getCommentsInfo(CommentsInfoVo vo) throws LaiKeAPIException;

    /**
     * 获取评论详细信息
     *
     * @param vo  -
     * @param cid - 评论id
     * @return Map
     * @throws LaiKeAPIException -
     * @author sunH
     * @date 2023/9/11 16:07
     */
    Map<String, Object> getCommentsDetailInfoById(CommentsDetailInfoVo vo, int cid) throws LaiKeAPIException;

    /**
     * 获取评论详情回复列表
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException-
     * @author sunH
     * @date 2023/9/11 16:07
     */
    Map<String, Object> getCommentReplyList(GetCommentsDetailInfoVo vo) throws LaiKeAPIException;

    /**
     * 删除评论回复
     *
     * @param vo -
     * @param id -
     * @throws LaiKeAPIException-
     * @author sunH
     * @date 2023/9/11 16:07
     */
    void delCommentReply(MainVo vo, int id) throws LaiKeAPIException;

    /**
     * 修改评论信息
     *
     * @param vo -
     * @throws LaiKeAPIException -
     * @author sunH
     * @date 2023/9/11 16:07
     */
    void updateCommentsDetailInfoById(UpdateCommentsInfoVo vo) throws LaiKeAPIException;


    /**
     * 回复
     *
     * @param vo          -
     * @param commentId   -
     * @param commentText -
     * @return boolean
     * @throws LaiKeAPIException -
     * @author sunH
     * @date 2023/9/11 16:07
     */
    boolean replyComments(MainVo vo, int commentId, String commentText) throws LaiKeAPIException;


    /**
     * 删除评论
     *
     * @param vo        -
     * @param commentId -
     * @return boolean
     * @throws LaiKeAPIException -
     * @author sunH
     * @date 2023/9/11 16:07
     */
    boolean delComments(MainVo vo, int commentId) throws LaiKeAPIException;

    /**
     * 获取订单发货列表
     *
     * @param vo
     * @return
     */
    Map<String, Object> ShippingRecords(GetExpressDeliveryListVo vo) throws LaiKeAPIException;

    /**
     * 查看发货记录商品
     *
     * @param vo
     * @param id
     * @param name
     * @return
     */
    Map<String, Object> expressGetPro(MainVo vo, Integer id, String name) throws LaiKeAPIException;

    /**
     * 取消电子面单发货
     *
     * @param vo
     * @param id 发货记录id
     */
    String CancelElectronicWaybill(MainVo vo, Integer id) throws LaiKeAPIException;

    /**
     * 电子面单发货
     *
     * @param vo
     * @param exId
     * @param orderDetailIds
     */
    void FaceSheetSend(MainVo vo, Integer exId, String orderDetailIds) throws LaiKeAPIException;


    /**
     * 分账记录
     *
     * @param vo
     * @param mchId
     * @param condition
     * @param startDate
     * @param endDate
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> divideRecord(MainVo vo, Integer mchId, String condition, String startDate, String endDate, HttpServletResponse response) throws LaiKeAPIException;
}
