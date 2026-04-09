package com.laiketui.common.api;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.mch.MchConfigModel;
import com.laiketui.domain.mch.MchModel;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.PageModel;
import com.laiketui.domain.vo.mch.AddFreihtVo;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 关于店铺公共类
 *
 * @author Trick
 * @date 2020/11/13 9:11
 */
public interface PublicMchService
{


    /**
     * 验证店铺是否存在
     *
     * @param storeId -
     * @param userId  -
     * @param shopId  -
     * @return MchModel
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/13 9:14
     */
    MchModel verificationMchExits(int storeId, String userId, Integer shopId) throws LaiKeAPIException;

    /**
     * 验证店铺是否存在
     *
     * @param storeId -
     * @param userId  -
     * @param shopId  -
     * @return MchModel
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/13 9:14
     */
    @Deprecated
    MchModel verificationMchExis(int storeId, String userId, Integer shopId) throws LaiKeAPIException;

    /**
     * 自提结算
     *
     * @param storeId       商城id
     * @param shopId        店铺id    
     * @param res
     * @param shopAddressId 门店地址
     * @return
     */
    Map<String, Object> settlement(int storeId, Integer shopId, String res, int shopAddressId, int storeType) throws LaiKeAPIException;


    /**
     * 确认收货予店铺结算
     * 【php mch.parameter】
     * 【自提不会有跨店订单,所以店铺id只会有一个】
     *
     * @param storeId      -
     * @param shopId       - 店铺id
     * @param orderno      - 订单号
     * @param paymentMoney - 订单金额
     * @param allow        - 积分
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/1 11:10
     */
    void clientConfirmReceipt(int storeId, int shopId, String orderno, BigDecimal paymentMoney, BigDecimal allow) throws LaiKeAPIException;


    /**
     * 运费列表
     * 【php freight.freight_list】
     *
     * @param storeId -
     * @param mchId   -
     * @param name    -
     * @param isUse   - 是否只显示被使用的运费信息
     * @param page    -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/1 14:51
     */
    Map<String, Object> freightList(int storeId, int mchId, String name, Integer isUse, PageModel page) throws LaiKeAPIException;

    /**
     * 运费列表
     * 【php freight.freight_list】
     *
     * @param vo    -
     * @param mchId -
     * @param name  -
     * @param isUse - 是否只显示被使用的运费信息
     * @param page  -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2025/12/1 14:51
     */
    Map<String, Object> freightList(MainVo vo, int mchId, String name, Integer isUse, PageModel page) throws LaiKeAPIException;


    /**
     * 添加/修改运费
     * 【php freight.freight_add】
     *
     * @param vo -
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/1 16:15
     */
    boolean freightAdd(AddFreihtVo vo) throws LaiKeAPIException;


    /**
     * 编辑运费显示页面
     * 【php freight.freight_modify_show】
     *
     * @param storeId -
     * @param mchId   -
     * @param id      -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/2 9:10
     */
    Map<String, Object> freightModifyShow(int storeId, int mchId, int id) throws LaiKeAPIException;

    Map<String, Object> freightModifyShow(MainVo vo, int mchId, int id) throws LaiKeAPIException;


    /**
     * 删除运费
     * 【php freight.freight_del】
     *
     * @param storeId -
     * @param ids     -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2020/12/1 17:25
     */
    void freightDel(int storeId, String ids,Integer mchId) throws LaiKeAPIException;


    /**
     * 修改默认运费
     * 【php freight.set_default】
     *
     * @param vo -
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/1 16:15
     */
    void setDefault(AddFreihtVo vo) throws LaiKeAPIException;


    /**
     * 生成提取码
     * 【php mch.extraction_code】
     *
     * @return String
     * @throws LaiKeAPIException -
     * @author wangxian
     * @date 2020/12/14 10:49
     */
    String extractionCode() throws LaiKeAPIException;

    /**
     * 创建二维码
     * 【 php mch.extraction_code_img】
     *
     * @param extractionCode
     * @return
     * @throws LaiKeAPIException
     */
    String createQRCodeImg(String extractionCode, int storeId, int store_type) throws LaiKeAPIException;


    /**
     * 注销商户
     *
     * @param storeId -
     * @param mchId   -
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/26 15:19
     */
    boolean cancellationShop(int storeId, int mchId) throws LaiKeAPIException;


    /**
     * 是否缴纳保证金
     *
     * @param vo     -
     * @param userId -
     * @return boolean
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/10/26 10:49
     */
    boolean isPromisePay(MainVo vo, String userId) throws LaiKeAPIException;

    /**
     * 支付保证金
     *
     * @param vo      -
     * @param payType - 支付方式 wallet_pay=钱包支付...
     * @param pwd     -
     * @param userId  -
     * @param orderNo - 临时订单号
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/10/26 10:47
     */
    Map<String, Object> paymentPromise(MainVo vo, String payType, String pwd, String userId, String orderNo) throws LaiKeAPIException;

    /**
     * 店铺设置
     *
     * @param storeId
     * @param mchId
     * @return
     * @throws LaiKeAPIException
     */
    MchConfigModel getMchConfig(Integer storeId, Integer mchId) throws LaiKeAPIException;

    /**
     * 判断店铺是否已打烊
     *
     * @param mchId
     * @return
     * @throws LaiKeAPIException
     */
    String mchIsOpen(Integer mchId) throws LaiKeAPIException;

    /**
     * 是否需要缴纳保证金
     *
     * @param vo
     * @param userId
     * @return
     * @throws LaiKeAPIException
     */
    boolean judgeMchPromise(MainVo vo, String userId) throws LaiKeAPIException;
}
