package com.laiketui.common.api.plugin;


import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.user.User;
import com.laiketui.domain.user.UserAddress;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.OrderVo;
import com.laiketui.domain.vo.pay.PaymentVo;
import com.laiketui.domain.vo.plugin.auction.PromiseOrderVo;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 公共竞拍方法 接口
 *
 * @author Trick
 * @date 2022/7/1 18:00
 */
public interface PublicAuctionService
{
    /**
     * 获取专场活动状态
     *
     * @param id - 专场id
     * @return int
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2022/7/1 17:49
     */
    int getStatus(String id) throws LaiKeAPIException;

    /**
     * 获取专场活动状态
     *
     * @param status -
     * @return String
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022/7/4 12:01
     */
    String getStatusName(int status) throws LaiKeAPIException;


    /**
     * 获取场次活动状态
     *
     * @param id -
     * @return int
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022/7/4 15:51
     */
    int getStatusBySession(String id) throws LaiKeAPIException;

    /**
     * 获取场次活动状态
     *
     * @param status -
     * @return String
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022/7/4 12:01
     */
    String getStatusNameBySession(int status) throws LaiKeAPIException;


    /**
     * 获取商品状态
     *
     * @param status -
     * @return String
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022/7/4 12:01
     */
    String getGoodsStatusName(int status) throws LaiKeAPIException;

    /**
     * 获取专场/场次下所有商品
     *
     * @param vo        -
     * @param specialId - 专场id
     * @param sessionId - 场次id
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022/7/13 14:16
     */
    Map<String, Object> getSessionGoods(MainVo vo, String specialId, String sessionId) throws LaiKeAPIException;

    /**
     * 获取场次商品信息
     *
     * @param vo              -
     * @param specialId       - 专场id
     * @param sessionId       - 场次id
     * @param sessionParmaMap - 条件
     * @return vo
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022/8/10 11:20
     */
    Map<String, Object> getSessionGoods(MainVo vo, String specialId, String sessionId, Map<String, Object> sessionParmaMap) throws LaiKeAPIException;

    /**
     * 当前竞拍商品是否可以出价
     *
     * @param auctionGoodsId -
     * @return boolean
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022/7/13 20:34
     */
    boolean isOutAmt(int auctionGoodsId) throws LaiKeAPIException;


    /**
     * 删除专场
     *
     * @param vo   -
     * @param id   -
     * @param user -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022/7/1 15:51
     */
    void delSpecial(MainVo vo, String id, User user) throws LaiKeAPIException;

    /**
     * 添加商品到场次中
     *
     * @param storeId       - 商城id
     * @param attrJson      -[{'attrId':1,'startingAmt':1.1,'markUpAmt':2.2}..]
     * @param specialId     - 专场id
     * @param sessionId     - 场次id
     * @param isDelOldGoods - 是是编辑,编辑会把之前的商品都删掉
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022/7/26 17:39
     */
    void addGoodsBySessionId(int storeId, String attrJson, String specialId, String sessionId, boolean isDelOldGoods) throws LaiKeAPIException;


    /**
     * 添加商品到场次中 【店铺报名】
     *
     * @param storeId       - 商城id
     * @param attrJson      -[{'attrId':1,'startingAmt':1.1,'markUpAmt':2.2}..]
     * @param specialId     - 专场id
     * @param sessionId     - 场次id
     * @param isDelOldGoods - 是是编辑,编辑会把之前的商品都删掉
     * @param mchId         - 店铺id
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022/7/26 17:39
     */
    void addGoodsBySessionId(int storeId, String attrJson, String specialId, String sessionId, boolean isDelOldGoods, Integer mchId) throws LaiKeAPIException;

    /**
     * 竞拍保证金支付回调
     *
     * @param vo      -
     * @param orderNo -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022/7/18 12:01
     */
    void paymentPromise(PromiseOrderVo vo, String orderNo) throws LaiKeAPIException;



    /**
     * 获取专场预告页面
     * @param specialId
     * @return
     * @throws LaiKeAPIException
     */
    Map<String,Object> getSoonInfo(String specialId,User user) throws LaiKeAPIException;


    /**
     * 设置专场提醒
     * @param specialId
     * @param user
     * @throws LaiKeAPIException
     */
    void setRemind(String specialId,User user) throws LaiKeAPIException;

    /**
     * 获取竞拍商品详情
     * @param user
     * @param acid
     * @return
     * @throws LaiKeAPIException
     */
    Map<String,Object> getGoodsDetail(User user,Integer acid,Integer store_id) throws LaiKeAPIException;

    /**
     * 获取竞拍商品出价记录
     * @param vo
     * @param acid
     * @return
     * @throws LaiKeAPIException
     */
    Map<String,Object> getGoodsOutAmtRecord(MainVo vo, Integer acid) throws LaiKeAPIException;

    /**
     * 围观专场
     * @param vo
     * @param specialId
     * @throws LaiKeAPIException
     */
    void lookSpecial(MainVo vo, String specialId) throws LaiKeAPIException;


    /**
     * 获取保证金页面
     * @param store_id
     * @param specialId
     * @param user
     * @return
     * @throws LaiKeAPIException
     */
    Map<String,Object> payPromisePage(Integer store_id, String specialId,User user,Integer type) throws LaiKeAPIException;

    /**
     * 保证金下单
     * @param store_id
     * @param specialId
     * @param user
     * @return
     * @throws LaiKeAPIException
     */
    Map<String,Object> payPromise(Integer store_id, String specialId,User user,String payType) throws LaiKeAPIException;

    /**
     * 获取店铺专场列表
     * @param vo
     * @return
     * @throws LaiKeAPIException
     */
    Map<String,Object> getMchSpecialList(MainVo vo,User user,Map<String,Object> paramMap,Integer status,String sortType,String sort) throws LaiKeAPIException;

    /**
     * 获取场次下的所有商品
     * @param vo
     * @param sessionId
     * @return
     * @throws LaiKeAPIException
     */
    Map<String,Object> getSessionGoodsList(MainVo vo, String sessionId) throws LaiKeAPIException;

    /**
     * 获取主图
     * @param map
     * @param store_id
     * @param specialId
     * @throws LaiKeAPIException
     */
    void getMainImg(Map<String,Object> map,Integer store_id,String specialId) throws LaiKeAPIException;

    List<Map<String,Object>> sortByGoodsTotal(List<Map<String,Object>> list,String sort,String Sort_criteria) throws  LaiKeAPIException;

    /**
     * 收藏竞拍商品
     * @param vo
     * @param acId
     * @param user
     * @throws LaiKeAPIException
     */
    void collection(MainVo vo, Integer acId,User user) throws LaiKeAPIException;


    /**
     * 竞拍出价
     * @param vo
     * @param acGoodsId
     * @param price
     * @param user
     * @throws LaiKeAPIException
     */
    void offerAmt(MainVo vo, Integer acGoodsId, BigDecimal price,User user) throws LaiKeAPIException;

    /**
     * 竞拍专场-详情
     * @param vo
     * @param specialId
     * @return
     * @throws LaiKeAPIException
     */
    Map<String,Object> specialDetail(MainVo vo, String specialId) throws LaiKeAPIException;

    /**
     * 获取场次下所有商品列表
     * @param vo
     * @param sessionId
     * @param sortType
     * @return
     * @throws LaiKeAPIException
     */
    Map<String,Object> getSessionGoodsList(MainVo vo, String sessionId, Integer sortType) throws LaiKeAPIException;


    /**
     * 竞拍处理活动
     * @param storeId
     * @return
     * @throws LaiKeAPIException
     */
    Map<String,Object> execute(Integer storeId) throws LaiKeAPIException;

    /**
     * 保证金退款
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022/8/15 9:53
     */
    Map<String, Object> backPromise(PaymentVo vo) throws LaiKeAPIException;

    /**
     * 下单
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022/7/27 14:43
     */
    Map<String, Object> placeOrder(OrderVo vo) throws LaiKeAPIException;

    /**
     * 计算运费
     * @param storeId
     * @param mchId
     * @param num
     * @param freightId
     * @param userAddress
     * @param weight
     * @return
     * @throws LaiKeAPIException
     */
    BigDecimal getFreight(int storeId, Integer mchId, Integer num, Integer freightId,
                                  UserAddress userAddress, BigDecimal weight) throws LaiKeAPIException;

}