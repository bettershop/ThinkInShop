package com.laiketui.common.api.pay;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.mch.PromiseShModel;
import com.laiketui.domain.vo.pay.PaymentVo;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 支付接口
 *
 * @author Trick
 * @date 2020/12/3 15:10
 */
public interface PublicPaymentService
{

    /**
     * 支付成功回调地址
     */
    String RETURNURL = "http://trick.free.idcfengye.com/mch/alipayCallBack";
    /**
     * 支付成功异步回调地址
     */
    String NOTIFYURL = "http://trick.free.idcfengye.com/mch/alipayCallBack";

    Map<String, String> refundOrder(int storeId, String sNo, Integer id, String className, String tradeNo, BigDecimal refundAmt, boolean isTempOrder, BigDecimal orderPrice,Integer refundId) throws LaiKeAPIException;

    Map<String, Object> refund(Map params) throws LaiKeAPIException;

    /**
     * 二维码支付
     *
     * @param appid      -
     * @param privateKey -
     * @param publicKey  -
     * @param orderno    - 订单号
     * @param orderAmt   - 订单金额
     * @return String -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2020/12/3 15:13
     */
    String qrCodeOrder(String appid, String privateKey, String publicKey, String orderno, BigDecimal orderAmt) throws LaiKeAPIException;


    /**
     * 退款
     *
     * @param storeId   -
     * @param id        - 订单id
     * @param className - 支付方式
     * @param treadeNo  -  交易流水号
     * @param refundAmt - 退款金额
     * @return Map<String, String>
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2020/12/3 15:31
     */
    Map<String, String> refundOrder(int storeId, Integer id, String className, String treadeNo, BigDecimal refundAmt, BigDecimal orderPrice) throws LaiKeAPIException;

    /**
     * 退款
     *
     * @param storeId   -
     * @param id        - 订单id
     * @param className - 支付方式
     * @param treadeNo  -  交易流水号
     * @param refundAmt - 退款金额
     * @return Map<String, String>
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2020/12/3 15:31
     */
    Map<String, String> refundOrder(int storeId, Integer id, String className, String treadeNo, BigDecimal refundAmt) throws LaiKeAPIException;


    /**
     * 退款
     *
     * @param storeId        -
     * @param promiseShModel - 保证金审核
     * @param className      - 支付方式
     * @param treadeNo       -  交易流水号
     * @param refundAmt      - 退款金额
     * @return Map<String, String>
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2020/12/3 15:31
     */
    Map<String, String> refundOrder(int storeId, PromiseShModel promiseShModel, String className, String treadeNo, BigDecimal refundAmt) throws LaiKeAPIException;

    /**
     * 退款
     *
     * @param storeId    -
     * @param id         - 保证金审核id
     * @param isPass     - 是否通过
     * @param refusedWhy - 拒绝原因
     * @param className  - 支付方式
     * @param treadeNo   -  交易流水号
     * @param refundAmt  - 退款金额
     * @return Map<String, String>
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2020/12/3 15:31
     */
    Map<String, String> refundOrder(int storeId, int id, int isPass, String refusedWhy, String className, String treadeNo, BigDecimal refundAmt) throws LaiKeAPIException;

    /**
     * 退款
     *
     * @param storeId     -
     * @param id          - 订单id
     * @param className   - 支付方式
     * @param tradeNo     -  交易流水号
     * @param refundAmt   - 退款金额
     * @param isTempOrder - 是否从临时表中取
     * @return Map<String, String>
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2020/12/3 15:31
     */
    default Map<String, String> refundOrder(int storeId, Integer id, String className, String tradeNo, BigDecimal refundAmt, boolean isTempOrder, BigDecimal orderPrice) throws LaiKeAPIException
    {
        return null;
    }

    /**
     * 退款
     *
     * @param storeId     -
     * @param id          - 订单id
     * @param className   - 支付方式
     * @param tradeNo     -  交易流水号
     * @param refundAmt   - 退款金额
     * @param isTempOrder - 是否从临时表中取
     * @return Map<String, String>
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2020/12/3 15:31
     */
    default Map<String, String> refundOrder(int storeId, Integer id, String className, String tradeNo, BigDecimal refundAmt, boolean isTempOrder) throws LaiKeAPIException
    {
        return null;
    }

    /**
     * 余额支付
     *
     * @param userId      -
     * @param money       -
     * @param token       -
     * @param tokenKey    - 登录标识头key
     * @param recordType  - 记录类型,默认余额消费 请参考:RecordModel.RecordType add by trick 2022-12-15 11:48:46
     * @param recordEvent - 记录内容，默认余额消费信息
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/10/26 11:57
     */
    default void walletPay(String userId, BigDecimal money, String token, String tokenKey, int recordType, String recordEvent, String sNo) throws LaiKeAPIException
    {
    }

    /**
     * 余额支付
     *
     * @param userId      -
     * @param money       -
     * @param token       -
     * @param tokenKey    - 登录标识头key
     * @param recordType  - 记录类型,默认余额消费 请参考:RecordModel.RecordType add by trick 2022-12-15 11:48:46
     * @param recordEvent - 记录内容，默认余额消费信息
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/10/26 11:57
     */
    default void walletPay(String userId, BigDecimal money, String token, String tokenKey, int recordType, String recordEvent) throws LaiKeAPIException
    {
    }

    default void walletPay(String userId, BigDecimal money, String token, String tokenKey, String sNo) throws LaiKeAPIException
    {
    }

    default void walletPay(String userId, BigDecimal money, String token, String sNo) throws LaiKeAPIException
    {
    }

    /**
     * 积分支付
     *
     * @param userId     -
     * @param money      -
     * @param orderNo    -
     * @param orderPrice -
     * @param token      -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/11/2 11:46
     */
    default void integralPay(String userId, BigDecimal money, String orderNo, BigDecimal orderPrice, String token) throws LaiKeAPIException
    {

    }

    /**
     * 钱包退款
     *
     * @param userId -
     * @param money  -
     * @param token  -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/10/26 14:24
     */
    default void walletReturnPay(String userId, BigDecimal money, String token) throws LaiKeAPIException
    {
    }

    /**
     * 钱包退款
     *
     * @param userId -
     * @param money  -
     * @param token  -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/10/26 14:24
     */
    default void walletReturnPay(String userId, PromiseShModel promiseShModel, BigDecimal money, String token) throws LaiKeAPIException
    {
    }

    /**
     * 钱包退款
     *
     * @param userId -
     * @param money  -
     * @param token  -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/10/26 14:24
     */
    default void walletReturnPay(String userId, PromiseShModel promiseShModel, BigDecimal money, String token, Integer mchId) throws LaiKeAPIException
    {
    }

    /**
     * 微信请求分账
     *
     * @param orderNo       微信支付订单号
     * @param transactionId 微信生成的订单号
     * @return
     * @throws LaiKeAPIException
     */
    default Map<String, Object> divideAccount(String orderNo, String transactionId, StringBuilder loggerStr) throws LaiKeAPIException
    {
        return null;
    }

    /**
     * 微信请求分账-新的微信请求分账
     *
     * @param orderNo       微信支付订单号
     * @param transactionId 微信生成的订单号
     * @return
     * @throws LaiKeAPIException
     */
    default Map<String, Object> newDivideAccount(String orderNo, String transactionId, StringBuilder loggerStr) throws LaiKeAPIException
    {
        return null;
    }

    /**
     * 微信查询分账结果
     *
     * @param paymentVo
     * @param orderNo
     * @return
     * @throws LaiKeAPIException
     */
    default Map<String, Object> ledgerResults(PaymentVo paymentVo, String orderNo) throws LaiKeAPIException
    {
        return null;
    }

//    /**
//     * 查询剩余待分金额
//     * @param keyPem
//     * @param mchId
//     * @param mchSerialNo
//     * @param transactionId
//     * @return
//     * @throws LaiKeAPIException
//     */
//    default Map<String, Object> queryRemainingAmount(String keyPem, String mchId, String mchSerialNo, String transactionId) throws LaiKeAPIException {
//        return null;
//    }

    /**
     * 微信分账回退
     *
     * @param orderNo
     * @return
     * @throws LaiKeAPIException
     */
    default Map<String, Object> dividendRefund(String orderNo) throws LaiKeAPIException
    {
        return null;
    }

}
