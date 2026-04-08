package com.laiketui.apps.api.app.auction;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 竞拍
 *
 * @author Trick
 * @date 2021/4/22 10:02
 */
public interface AppsCstrAuctionService
{


    /**
     * 竞拍列表
     *
     * @param vo   -
     * @param type -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/4/22 10:03
     */
    Map<String, Object> index(MainVo vo, String type) throws LaiKeAPIException;


    /**
     * 获取竞拍商品详情
     *
     * @param vo   -
     * @param id   -
     * @param isfx -true-从分享链接进入  false-不是从分享链接进入
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/4/22 18:24
     */
    Map<String, Object> auctionGoodsDetail(MainVo vo, int id, String isfx) throws LaiKeAPIException;


    /**
     * 定时获取竞拍页面热数据
     *
     * @param vo -
     * @param id -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/4/23 10:21
     */
    Map<String, Object> timeRequest(MainVo vo, int id) throws LaiKeAPIException;


    /**
     * 交押金/出价
     *
     * @param vo        -
     * @param id        -
     * @param password  -
     * @param payType   -
     * @param addressId -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/4/23 11:23
     */
    Map<String, Object> goPay(MainVo vo, int id, String password, String payType, Integer addressId) throws LaiKeAPIException;


    /**
     * 出价
     *
     * @param vo    -
     * @param id    -
     * @param price -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/4/23 15:24
     */
    Map<String, Object> bid(MainVo vo, int id, BigDecimal price) throws LaiKeAPIException;


    /**
     * 竞拍规则
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/4/23 17:23
     */
    Map<String, Object> rule(MainVo vo) throws LaiKeAPIException;


    /**
     * 出价记录
     *
     * @param vo -
     * @param id -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/4/23 17:27
     */
    Map<String, Object> record(MainVo vo, int id) throws LaiKeAPIException;


    /**
     * 开始竞拍商品
     *
     * @param vo -
     * @param id -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/4/24 9:04
     */
    Map<String, Object> startAution(MainVo vo, int id) throws LaiKeAPIException;
}
