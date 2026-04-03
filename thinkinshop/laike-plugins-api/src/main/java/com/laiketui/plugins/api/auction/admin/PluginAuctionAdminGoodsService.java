package com.laiketui.plugins.api.auction.admin;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;

import java.util.Map;

/**
 * 竞拍商品接口
 *
 * @author Trick
 * @date 2022/7/11 16:33
 */
public interface PluginAuctionAdminGoodsService
{


    /**
     * 获取还未参与的商品
     *
     * @param vo        -
     * @param brandId   -
     * @param classId   -
     * @param key       -
     * @param sessionId -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022/7/11 16:45
     */
    Map<String, Object> getProductList(MainVo vo, Integer brandId, Integer classId, String key, String sessionId) throws LaiKeAPIException;

    /**
     * 获取竞拍商品集合
     *
     * @param vo      -
     * @param classId -
     * @param key     -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2022/7/4 17:42
     */
    Map<String, Object> getAuctionGoodsList(MainVo vo, Integer classId, String key) throws LaiKeAPIException;


    /**
     * 获取已经参与竞拍的商品列表
     *
     * @param vo     -
     * @param key    -
     * @param status -
     * @param name   -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022/7/5 16:21
     */
    Map<String, Object> getGoodsList(MainVo vo, String key, Integer status, String name) throws LaiKeAPIException;


    /**
     * 商品显示开关
     *
     * @param vo -
     * @param id -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022/7/1 16:18
     */
    void sessionSwitch(MainVo vo, String id) throws LaiKeAPIException;

    /**
     * 删除竞拍商品
     *
     * @param vo   -
     * @param acId -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022/7/21 15:08
     */
    void delGoods(MainVo vo, Integer acId) throws LaiKeAPIException;

    /**
     * 出价详情列表
     *
     * @param vo   -
     * @param acId -
     * @param key  -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022/7/21 15:29
     */
    Map<String, Object> bidList(MainVo vo, Integer acId, String key) throws LaiKeAPIException;

    /**
     * 删除普通商品判断
     *
     * @param vo
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> delPro(MainVo vo, String goodsIds) throws LaiKeAPIException;
}
