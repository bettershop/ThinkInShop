package com.laiketui.apps.api.app.services;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;

import java.util.List;
import java.util.Map;

/**
 * 购物车接口
 *
 * @author Trick
 * @date 2020/10/20 9:50
 */
public interface AppsCstrCartService
{


    /**
     * 进入购物车页面
     *
     * @param storeId  -
     * @param language -
     * @param accessId -
     * @param page     -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/10/20 9:52
     */
    Map<String, Object> index(MainVo vo, Integer commodityType) throws LaiKeAPIException;


    /**
     * 修改购物车
     *
     * @param vo    -
     * @param goods - 购物车信息 {"num":2,"cart_id":3297}
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/10/21 9:13
     */
    boolean upCart(MainVo vo, String goods) throws LaiKeAPIException;


    /**
     * 清空购物车
     *
     * @param vo -
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/10/21 13:43
     */
    boolean delAllCart(MainVo vo) throws LaiKeAPIException;


    /**
     * 修改购物车商品属性
     *
     * @param vo          -
     * @param cartId      -
     * @param attributeId -
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/10/21 14:30
     */
    boolean modifyAttribute(MainVo vo, Integer cartId, Integer attributeId) throws LaiKeAPIException;


    /**
     * 修改购物车商品属性
     *
     * @param vo     -
     * @param cartId -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/10/21 17:56
     */
    List<Map<String, Object>> djAttribute(MainVo vo, int cartId) throws LaiKeAPIException;


    /**
     * 删除购物车
     *
     * @param vo      -
     * @param cartIds -
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/10/22 12:06
     */
    boolean delcart(MainVo vo, String cartIds) throws LaiKeAPIException;


}
