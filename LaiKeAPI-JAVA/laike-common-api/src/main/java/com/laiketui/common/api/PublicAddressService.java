package com.laiketui.common.api;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.user.UserAddress;

import java.util.List;
import java.util.Map;


/**
 * 通用用户收货地址
 *
 * @author wangxian
 */
public interface PublicAddressService
{

    /**
     * 获取用户地址
     *
     * @return
     */
    UserAddress findAddress(Map<String, Object> map);


    /**
     * 获取用户地址
     * 【php Tool.find_address】
     *
     * @param storeId -
     * @param uid     -
     * @param id      - 地址id -可选
     * @return UserAddress
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/3/26 17:46
     */
    UserAddress findAddress(int storeId, String uid, Integer id) throws LaiKeAPIException;

    /**
     * 根据 省市县名称获取地址id
     *
     * @param storeId     -
     * @param addressName - [省、市、县]
     * @return List
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022/3/3 10:16
     */
    List<Integer> findAddressIdByName(int storeId, List<String> addressName) throws LaiKeAPIException;
}
