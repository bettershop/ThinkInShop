package com.laiketui.apps.api.app.services;

import java.util.Map;

/**
 * 虚拟商品接口
 *
 * @author Trick
 * @date 2020/12/17 11:23
 */
public interface AppsCstrVirtualService
{

    /**
     * 订单详情中查询核销记录
     *
     * @param id
     * @return
     */
    Map<String, Object> getWriteRecord(Integer id);


}
