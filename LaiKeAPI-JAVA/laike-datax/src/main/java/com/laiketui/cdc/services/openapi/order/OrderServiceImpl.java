package com.laiketui.cdc.services.openapi.order;

import com.laiketui.cdc.openapi.order.OrderService;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.order.*;
import com.laiketui.domain.vo.virtual.WriteRecordVo;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

@Service
public class OrderServiceImpl   {

    public Map<String, Object> index(AdminOrderListVo adminOrderVo, HttpServletResponse response) throws LaiKeAPIException {
        return null;
    }

    
    public Map<String, Object> orderCount(MainVo vo) throws LaiKeAPIException {
        return null;
    }

    
    public Map<String, Object> kuaidishow(MainVo vo, String orderno) throws LaiKeAPIException {
        return null;
    }

    
    public Map<String, Object> close(AdminOrderVo orderVo) throws LaiKeAPIException {
        return null;
    }

    
    public Map<String, Object> del(MainVo vo, String orders) throws LaiKeAPIException {
        return null;
    }

    
    public List<Map<String, Object>> orderPrint(AdminOrderVo orderVo) throws LaiKeAPIException {
        return null;
    }

    
    public Map<String, Object> searchExpress(String express) throws LaiKeAPIException {
        return null;
    }

    
    public Map<String, Object> deliveryView(AdminDeliveryVo adminDeliveryVo) throws LaiKeAPIException {
        return null;
    }

    
    public void deliverySave(MainVo vo, Integer exId, String exNo, String orderDetailIds) throws LaiKeAPIException {

    }

    
    public void adminDeliveryForStoreSelf(MainVo vo, String phone, String courier_name, String sNo) throws LaiKeAPIException {

    }

    
    public Map<String, Object> editOrderView(OrderModifyVo orderVo) throws LaiKeAPIException {
        return null;
    }

    
    public void saveEditOrder(EditOrderVo orderVo) throws LaiKeAPIException {

    }

    
    public Map<String, Object> helpOrder(HelpOrderVo vo) throws LaiKeAPIException {
        return null;
    }

    
    public Map<String, Object> valetOrderSettlement(HelpOrderVo vo) throws LaiKeAPIException {
        return null;
    }

    
    public Map<String, Object> orderDetailsInfo(AdminOrderDetailVo orderVo) {
        return null;
    }

    
    public boolean batchDelivery(MainVo vo, List<MultipartFile> image) throws LaiKeAPIException {
        return false;
    }

    
    public Map<String, Object> deliveryList(MainVo vo, String fileName, Integer status, String startDate, String endDate, HttpServletResponse response) throws LaiKeAPIException {
        return null;
    }

    
    public void delDelivery(MainVo vo, String id) throws LaiKeAPIException {

    }

    
    public Map<String, Object> verificationExtractionCode(MainVo vo, Integer orderId, String extractionCode, Integer mch_store_id, Integer pid) throws LaiKeAPIException {
        return null;
    }

    
    public Map<String, Object> ShippingRecords(GetExpressDeliveryListVo vo) throws LaiKeAPIException {
        return null;
    }

    
    public Map<String, Object> expressGetPro(MainVo vo, Integer id, String name) {
        return null;
    }

    
    public void CancelElectronicWaybill(MainVo vo, Integer id) throws LaiKeAPIException {

    }

    
    public void FaceSheetSend(MainVo vo, Integer exId, String orderDetailIds) throws LaiKeAPIException {

    }

    
    public Map<String, Object> getMchStore(MainVo vo, Integer mchId, Integer pid, String sNo) {
        return null;
    }

    
    public Map<String, Object> getWriteRecord(WriteRecordVo vo) {
        return null;
    }

    
    public Map<String, Object> testExtractionCode(MainVo vo, Integer orderId, String extractionCode, Integer mch_store_id) {
        return null;
    }
}
