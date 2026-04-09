package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.payment.PaymentModel;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;


/**
 * 支付方式
 *
 * @author Trick
 * @date 2020/12/2 16:56
 */
public interface PaymentModelMapper extends BaseMapper<PaymentModel>
{


    /**
     * 获取支付配置信息
     *
     * @param map -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/2 16:59
     */
    List<Map<String, Object>> getPaymentConfigInfoDynamic(Map<String, Object> map) throws LaiKeAPIException;


    /**
     * 获取所有的支付状态
     *
     * @param storeId -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/22 18:32
     */
    @Select("select c.status,p.class_name from lkt_payment_config c left join lkt_payment p on c.pid=p.id where c.store_id=#{storeId}")
    List<Map<String, Object>> getPaymentStatusAll(int storeId) throws LaiKeAPIException;


    List<Map<String, Object>> getPaymentConfigInfo(Map<String, Object> map) throws LaiKeAPIException;

    int countPaymentConfigInfo(Map<String, Object> map) throws LaiKeAPIException;

}