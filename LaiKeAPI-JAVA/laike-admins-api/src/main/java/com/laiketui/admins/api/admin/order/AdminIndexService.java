package com.laiketui.admins.api.admin.order;


import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.order.*;
import com.laiketui.domain.vo.virtual.WriteRecordVo;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * 订单列表
 *
 * @author wangxian
 */
public interface AdminIndexService
{

    /**
     * 后台订单列表
     * 【php orderslist>Index.order_index】
     *
     * @param adminOrderVo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author wangxian
     * @date 2021/7/19 14:50
     * @since Trick v1.0
     */
    Map<String, Object> index(AdminOrderListVo adminOrderVo, HttpServletResponse response) throws LaiKeAPIException;

    /**
     * 订单统计 - 普通订单
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/7/19 17:24
     */
    Map<String, Object> orderCount(MainVo vo) throws LaiKeAPIException;


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
     * 关闭订单
     *
     * @param orderVo
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> close(AdminOrderVo orderVo) throws LaiKeAPIException;

    /**
     * 删除订单
     *
     * @param vo     -
     * @param orders -
     * @return Map
     * @throws LaiKeAPIException-
     * @author wangxian
     * @date 2021/7/21 10:54
     * @since Trick v1.0
     */
    Map<String, Object> del(MainVo vo, String orders) throws LaiKeAPIException;

    /**
     * 订单打印
     *
     * @param orderVo
     * @return
     * @throws LaiKeAPIException
     */
    List<Map<String, Object>> orderPrint(AdminOrderVo orderVo) throws LaiKeAPIException;

    /**
     * 搜索快递公司 express
     *
     * @return
     */
    Map<String, Object> searchExpress(String express) throws LaiKeAPIException;

    /**
     * 进入发货界面
     *
     * @param adminDeliveryVo
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> deliveryView(AdminDeliveryVo adminDeliveryVo) throws LaiKeAPIException;

    /**
     * 订单发货
     *
     * @param vo             -
     * @param shipDataVo -
     * @throws LaiKeAPIException-
     * @author Administrator
     * @date 2021/8/2 14:42
     */
    void deliverySave(MainVo vo, ShipDataVo shipDataVo) throws LaiKeAPIException;

    /**
     * 发货
     * 【php DeliveryHelper.frontDelivery】
     *
     * @param vo             -
     * @param exId           -
     * @param exNo           -
     * @param orderDetailIds -
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/26 16:43
     */
    void adminDeliveryForStoreSelf(MainVo vo, String phone, String courier_name, String sNo) throws LaiKeAPIException;

    /**
     * 编辑订单界面
     *
     * @param orderVo
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> editOrderView(OrderModifyVo orderVo) throws LaiKeAPIException;

    /**
     * 保存编辑订单
     *
     * @param orderVo -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/8/2 10:21
     */
    void saveEditOrder(EditOrderVo orderVo) throws LaiKeAPIException;

    /**
     * 代客下单
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/8/2 17:36
     */
    Map<String, Object> helpOrder(HelpOrderVo vo) throws LaiKeAPIException;

    /**
     * 代客下单-结算
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/9/28 13:13
     */
    Map<String, Object> valetOrderSettlement(HelpOrderVo vo) throws LaiKeAPIException;

    /**
     * 平台订单详情
     *
     * @param orderVo
     * @return
     */
    Map<String, Object> orderDetailsInfo(AdminOrderDetailVo orderVo);

    /**
     * 批量发货
     *
     * @param vo    -
     * @param image -
     * @return boolean
     * @throws LaiKeAPIException -
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
     */
    Map<String, Object> deliveryList(MainVo vo, String fileName, Integer status, String startDate, String endDate, HttpServletResponse response) throws LaiKeAPIException;


    /**
     * 删除批量发货记录
     *
     * @param id -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/12/8 21:04
     */
    void delDelivery(MainVo vo, String id) throws LaiKeAPIException;


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
    Map<String, Object> verificationExtractionCode(MainVo vo, Integer orderId, String extractionCode, Integer mch_store_id, Integer pid) throws LaiKeAPIException;

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
    Map<String, Object> expressGetPro(MainVo vo, Integer id, String name);

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
     * 查询可核销门店
     *
     * @param vo
     * @param mchId
     * @return
     */
    Map<String, Object> getMchStore(MainVo vo, Integer mchId, Integer pid, String sNo);

    /**
     * 查询核销记录
     *
     * @param vo
     * @return
     */
    Map<String, Object> getWriteRecord(WriteRecordVo vo);

    /**
     * 检验核销码是否正确
     *
     * @param vo
     * @return
     */
    Map<String, Object> testExtractionCode(MainVo vo, Integer orderId, String extractionCode, Integer mch_store_id);

    /**
     * 发货
     * @param vo
     * @param shippingData
     */
    void delivery(MainVo vo, ShipDataVo shippingData);


    /**
     * 批量修改电子面单状态
     * @param vo
     * @param ids
     * @throws LaiKeAPIException
     */
    void updateExpressDeliveryStatus(MainVo vo, String ids) throws LaiKeAPIException;

    /**
     * 电子面单复打
     * @param vo
     * @param ids
     */
    void overridePrint(MainVo vo, String ids) throws LaiKeAPIException;
}
