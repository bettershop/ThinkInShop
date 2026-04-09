package com.laiketui.apps.api.mch;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.config.AddExpressSubtableVo;
import com.laiketui.domain.vo.config.GetExpressSubtableListVo;
import com.laiketui.domain.vo.order.GetExpressDeliveryListVo;

import java.util.Map;

/**
 * 物流管理
 */
public interface AppsMchExpressService
{

    /**
     * 添加修改快递公司子表
     *
     * @param vo
     * @throws LaiKeAPIException
     */
    void addAndUpdateExpressSubtable(AddExpressSubtableVo vo) throws LaiKeAPIException;

    /**
     * 获取快递公司子表列表
     *
     * @param vo
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> getExpressSubtableList(GetExpressSubtableListVo vo) throws LaiKeAPIException;

    /**
     * 获取快递公司子表详情
     *
     * @param vo
     * @param id
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> getExpressSubtableById(MainVo vo, Integer id) throws LaiKeAPIException;

    /**
     * 删除快递公司子表详情
     *
     * @param vo
     * @param id
     * @return
     * @throws LaiKeAPIException
     */
    void delExpressSubtableById(MainVo vo, Integer id) throws LaiKeAPIException;

    /**
     * 获取所有物流公司信息
     *
     * @return
     */
    Map<String, Object> getExpressInfo(MainVo vo, Integer id) throws LaiKeAPIException;

    /**
     * 获取发货物流公司信息
     *
     * @param vo
     * @param sNo
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> GetLogistics(MainVo vo, String sNo);

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
}
