package com.laiketui.plugins.api.pc.mall;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;

import java.util.List;
import java.util.Map;

/**
 * 购物车
 *
 * @author Trick
 * @date 2021/6/21 9:54
 */
public interface AppsMallCartService
{


    /**
     * 进入购物车页面
     *
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021-06-21 09:54:49
     */
    Map<String, Object> index(MainVo vo) throws LaiKeAPIException;


    /**
     * 修改购物车
     *
     * @param vo   -
     * @param cart - 购物车信息 [{"num":2,"cart_id":3297},...]
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021-06-21 11:00:57
     */
    void upCart(MainVo vo, String cart) throws LaiKeAPIException;


    /**
     * 批量删除购物车商品
     *
     * @param vo      -
     * @param cartIds -
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021-06-21 11:19:40
     */
    void delCart(MainVo vo, String cartIds) throws LaiKeAPIException;

    /**
     * 移动到我的收藏
     *
     * @param vo     -
     * @param cartId -
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021-06-21 11:19:40
     */
    void toCollection(MainVo vo, Integer cartId) throws LaiKeAPIException;


    /**
     * 修改购物车商品属性
     *
     * @param vo     -
     * @param cartId -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021-06-21 14:03:37
     */
    List<Map<String, Object>> djAttribute(MainVo vo, int cartId) throws LaiKeAPIException;


    /**
     * 修改购物车商品属性
     *
     * @param vo          -
     * @param cartId      -
     * @param attributeId -
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021-06-21 14:14:45
     */
    void modifyAttribute(MainVo vo, Integer cartId, Integer attributeId) throws LaiKeAPIException;
}
