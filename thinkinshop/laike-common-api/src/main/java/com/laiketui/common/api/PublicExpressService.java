package com.laiketui.common.api;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.config.ExpressModel;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.config.AddExpressSubtableVo;
import com.laiketui.domain.vo.config.GetExpressSubtableListVo;
import com.laiketui.domain.vo.order.GetExpressDeliveryListVo;

import java.util.List;
import java.util.Map;

/**
 * 物流公司信息服务接口
 *
 * @author wangxian
 */
public interface PublicExpressService
{

    /**
     * 获取所有物流公司信息
     *
     * @return
     */
    List<ExpressModel> getExpressInfo() throws LaiKeAPIException;

    /**
     * 获取所有物流公司信息
     *
     * @return
     */
    Map<String, Object> getExpressInfoByMchId(MainVo vo, Integer mchId) throws LaiKeAPIException;

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
     * 获取发货记录列表
     *
     * @param vo
     * @throws LaiKeAPIException
     */
    Map<String, Object> getExpressDeliveryList(GetExpressDeliveryListVo vo) throws LaiKeAPIException;

    /**
     * 判断店铺是否有电子面单配置
     *
     * @param vo
     * @param MchId
     * @throws LaiKeAPIException
     */
    boolean getMchHaveExpressSubtableByMchId(MainVo vo, Integer MchId) throws LaiKeAPIException;

    /**
     * 获取物流公司列表--电子面单
     *
     * @param vo
     * @param sNo
     * @throws LaiKeAPIException
     */
    Map<String, Object> getExpressInfoBySNo(MainVo vo, String sNo) throws LaiKeAPIException;

    /**
     * 获取物流公司列表--电子面单无需订单号用于隐藏已经配置面单的物流
     *
     * @param vo
     * @param needFilter
     * @param id
     * @throws LaiKeAPIException
     */
    Map<String, Object> getExpressInfo(MainVo vo, Integer mchId, boolean needFilter, Integer id) throws LaiKeAPIException;

    /**
     * 查看发货记录商品
     *
     * @param vo
     * @param id   发货记录id
     * @param name 商品名称
     * @throws LaiKeAPIException
     */
    Map<String, Object> getGoodsByExpressDeliveryId(MainVo vo, Integer id, String name) throws LaiKeAPIException;

    /**
     * 小程序发货信息录入
     *
     * @param vo
     * @param sNo
     * @return
     * @throws LaiKeAPIException
     */
    boolean setWxAppUploadShippingInfo(MainVo vo, String sNo) throws LaiKeAPIException;

    /**
     * 同步微信发货信息录入的物流公司信息
     *
     * @return
     * @throws LaiKeAPIException
     */
    void synchronizationWxDelivery() throws LaiKeAPIException;

    /**
     * 同步微信发货订单状态
     *
     * @return
     * @throws LaiKeAPIException
     */
    void synchronizationWxOrderStatus(Integer storeId) throws LaiKeAPIException;
}
