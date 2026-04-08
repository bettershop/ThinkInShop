package com.laiketui.plugins.api.auction.app;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 我的竞拍
 *
 * @author Trick
 * @date 2022/7/12 16:16
 */
public interface PluginAuctionAppUserService
{
    /**
     * 我的竞拍
     *
     * @param vo     -
     * @param status - 1=已收藏 2=竞拍中 3=已拍下 4=未得标
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2022/7/12 15:29
     */
    Map<String, Object> getMyAuctionList(MainVo vo, Integer status) throws LaiKeAPIException;

    /**
     * 我的保证金
     *
     * @param vo     -
     * @param status - 1=已缴纳 2=已退还 3=已扣除
     * @param name   -专场名称
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2022/7/12 15:29
     */
    Map<String, Object> getMyPromiseList(MainVo vo, Integer status, String name) throws LaiKeAPIException;

    /**
     * 收藏竞拍商品
     *
     * @param vo   -
     * @param acId -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022/7/14 14:28
     */
    void collection(MainVo vo, Integer acId) throws LaiKeAPIException;


    /**
     * 保证金确认页面数据
     *
     * @param vo        -
     * @param specialId -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022/7/14 15:09
     */
    Map<String, Object> payPromisePage(MainVo vo, String specialId) throws LaiKeAPIException;

    /**
     * 支付保证金下单
     *
     * @param vo        -
     * @param specialId -专场id
     * @param payType   -支付类型
     * @param pwd       - 密码
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022/7/12 17:01
     */
    Map<String, Object> payPromise(MainVo vo, String specialId, String payType) throws LaiKeAPIException;

    /**
     * 出价
     *
     * @param vo        -
     * @param acGoodsId - 竞拍商品id
     * @param price     - 出价金额
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022/7/12 17:03
     */
    void offerAmt(MainVo vo, Integer acGoodsId, BigDecimal price) throws LaiKeAPIException;

    /**
     * 设置提醒
     *
     * @param vo        -
     * @param specialId -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022/8/3 15:41
     */
    void setRemind(MainVo vo, String specialId) throws LaiKeAPIException;

}
