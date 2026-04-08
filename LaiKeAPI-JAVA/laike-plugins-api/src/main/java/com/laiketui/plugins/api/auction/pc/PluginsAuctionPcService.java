package com.laiketui.plugins.api.auction.pc;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.auction.AuctionSearchVo;
import com.laiketui.domain.vo.user.CollectionVo;

import java.math.BigDecimal;
import java.util.Map;

/**
 * @Author: liuao
 * @Date: 2025-10-30-10:50
 * @Description:
 */
public interface PluginsAuctionPcService
{
    /**
     * 拍品首页搜索
     * @param vo
     * @return
     */
    Map<String,Object> search(AuctionSearchVo vo) throws LaiKeAPIException;

    /**
     * 获取竞拍专场列表
     * @param vo
     * @return
     */
    Map<String,Object> getSpecialList(AuctionSearchVo vo) throws LaiKeAPIException;

    /**
     * 获取预告页面
     * @param vo
     * @param id
     * @return
     */
    Map<String,Object> getSoonInfo(MainVo vo, String id) throws LaiKeAPIException;

    /**
     * 设置专场提醒
     * @param vo
     * @param specialId
     * @throws LaiKeAPIException
     */
    void setRemind(MainVo vo, String specialId) throws LaiKeAPIException;

    /**
     * 获取场次+商品列表
     * @param vo
     * @return
     * @throws LaiKeAPIException
     */
    Map<String,Object> getSessionList(AuctionSearchVo vo) throws  LaiKeAPIException;

    /**
     * 获取竞拍商品详情
     * @param vo
     * @param acid
     * @return
     */
    Map<String,Object> getGoodsDetails(MainVo vo, Integer acid) throws LaiKeAPIException;

    /**
     * 获取商品出价记录
     * @param vo
     * @param acid
     * @return
     */
    Map<String,Object> getGoodsOutAmtRecord(MainVo vo, Integer acid) throws LaiKeAPIException;

    /**
     *
     * @param vo
     * @param specialId
     * @throws LaiKeAPIException
     */
    void lookSpecial(MainVo vo, String specialId) throws LaiKeAPIException;

    /**
     * 竞拍保证金确认页面
     * @param vo
     * @param specialId
     * @return
     */
    Map<String,Object> payPromisePage(MainVo vo, String specialId) throws LaiKeAPIException;

    /**
     * 保证金下单
     * @param vo
     * @param specialId
     * @param payType
     * @return
     */
    Map<String,Object> payPromise(MainVo vo, String specialId, String payType) throws LaiKeAPIException;

    /**
     * 获取热门竞拍专场列表
     * @param vo
     * @return
     * @throws LaiKeAPIException
     */
    Map<String,Object> getHotSpecialList(AuctionSearchVo vo) throws LaiKeAPIException;

    /**
     * 获取热门竞拍商品列表
     * @param vo
     * @return
     */
    Map<String,Object> getHotAuctionProList(AuctionSearchVo vo) throws LaiKeAPIException;

    /**
     * 获取商品分类
     * @param vo
     * @return
     * @throws LaiKeAPIException
     */
    Map<String,Object> getClassList(MainVo vo,Integer mchId) throws LaiKeAPIException;

    /**
     * 获取店铺专场列表
     * @param vo
     * @return
     * @throws LaiKeAPIException
     */
    Map<String,Object> getMchSpecialList(AuctionSearchVo vo) throws LaiKeAPIException;

    /**
     * 获取店铺分类
     * @param vo
     * @return
     * @throws LaiKeAPIException
     */
    Map<String,Object> getMchClass(MainVo vo) throws LaiKeAPIException;

    /**
     * 获取特色商家
     * @param vo
     * @return
     * @throws LaiKeAPIException
     */
    Map<String,Object> getMchList(MainVo vo,Integer classId,String name) throws LaiKeAPIException;

    /**
     * 获取竞拍规则
     * @param vo
     * @return
     */
    Map<String,Object> getRule(MainVo vo) throws LaiKeAPIException;

    /**
     * 收藏竞拍商品
     * @param vo
     * @param acId
     * @throws LaiKeAPIException
     */
    void collection(MainVo vo, Integer acId) throws  LaiKeAPIException;

    /**
     * 获取店铺相关推荐拍品
     * @param vo
     * @param mchId
     * @return
     */
    Map<String,Object> getRelatedGoods(MainVo vo, Integer mchId) throws LaiKeAPIException;

    /**
     * 竞拍出价
     * @param vo
     * @param acGoodsId
     * @param price
     * @throws LaiKeAPIException
     */
    void offerAmt(MainVo vo, Integer acGoodsId, BigDecimal price) throws LaiKeAPIException;

    /**
     * 竞拍专场-详情
     * @param vo
     * @param specialId
     * @return
     */
    Map<String,Object> specialDetail(MainVo vo, String specialId) throws LaiKeAPIException;

    /**
     * 获取专场所有场次
     * @param vo
     * @param specialId
     * @return
     */
    Map<String,Object> getAllSessionList(MainVo vo, String specialId) throws LaiKeAPIException;


    /**
     * 获取场次下所有商品
     * @param vo
     * @param sessionId
     * @param sortType
     * @return
     * @throws LaiKeAPIException
     */
    Map<String,Object> getSessionGoodsList(MainVo vo, String sessionId, Integer sortType) throws LaiKeAPIException;

    /**
     * 获取店铺信息
     * @param vo
     * @param mchId
     * @return
     * @throws LaiKeAPIException
     */
    Map<String,Object> getShopInfo(MainVo vo, Integer mchId) throws LaiKeAPIException;

    /**
     * 收藏店铺
     * @param vo
     * @param mchId
     * @throws LaiKeAPIException
     */
    void toggleShopCollect(MainVo vo, Integer mchId) throws LaiKeAPIException;


    /**
     * 获取我的收藏
     * @param vo
     * @return
     * @throws LaiKeAPIException
     */
    Map<String,Object> getCollection(CollectionVo vo) throws LaiKeAPIException;

    /**
     * 取消竞拍收藏
     * @param collectionIds
     */
    void removeCollection(MainVo vo,String collectionIds) throws LaiKeAPIException;

}
