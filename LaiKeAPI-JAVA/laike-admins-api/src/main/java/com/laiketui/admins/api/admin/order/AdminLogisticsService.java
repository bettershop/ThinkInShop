package com.laiketui.admins.api.admin.order;


import com.laiketui.core.exception.LaiKeAPIException;

import java.util.Map;

/**
 * 订单物流
 *
 * @author wangxian
 */
public interface AdminLogisticsService
{

    /**
     * 物流信息
     *
     * @param orderNo
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> getLogistics(String orderNo) throws LaiKeAPIException;

}
