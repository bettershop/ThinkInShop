package com.laiketui.common.api.distribution;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.user.User;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.PageModel;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 分销
 *
 * @author Trick
 * @date 2021/2/19 16:58
 */
public interface PubliceDistributionService
{
    /**
     * 分销统一升一级
     * 【Commission.uplevel】
     *
     * @param storeId -
     * @param upId    - 升级id
     * @param orderNo - 可选 用于判断是否是礼包商品
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/2/19 13:52
     */
    void uplevel(int storeId, String upId, String orderNo) throws LaiKeAPIException;

    /**
     * 礼包升级
     * 【Commission.straight_up】
     *
     * @param storeId -
     * @param userId  - 用户id
     * @param level   - 需要升到的等级
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/4 13:52
     */
    void straightUp(int storeId, String userId, int level) throws LaiKeAPIException;


    /**
     * 创建分销等级和会员信息
     * 【php Commission.create_level】
     *
     * @param storeId  -
     * @param userId   -
     * @param level    -
     * @param fatherId -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/2/19 17:44
     */
    void createLevel(int storeId, String userId, int level, String fatherId) throws LaiKeAPIException;


    /**
     * 佣金发放
     * 【php Commission.putcomm】
     *
     * @param storeId     -
     * @param orderno     -订单号
     * @param achievement -推广业绩
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/2/20 9:13
     */
    void putcomm(int storeId, String orderno, BigDecimal achievement) throws LaiKeAPIException;

    /**
     * 佣金结算
     * 【根据后台分销设置 佣金结算方式来】
     *
     * @param storeId -
     * @param userId  -
     * @param orderno -
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/5/25 17:45
     */
    void commSettlement(int storeId, String userId, String orderno) throws LaiKeAPIException;

    /**
     * 获取分销商品
     *
     * @param storeId   -
     * @param user      -
     * @param pageModel -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/2/23 15:04
     */
    Map<String, Object> getGoodsInfo(int storeId, User user, PageModel pageModel) throws LaiKeAPIException;

    /**
     * 获取用户分销商品价格
     *
     * @param storeId    -
     * @param userId     -
     * @param goodsPrice -
     * @return BigDecimal
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022/1/24 15:57
     */
    BigDecimal getGoodsPrice(int storeId, String userId, BigDecimal goodsPrice) throws LaiKeAPIException;

    /**
     * 获取用户分销折扣值
     *
     * @param storeId -
     * @param userId  -
     * @return BigDecimal
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022/1/24 17:25
     */
    BigDecimal getGoodsDiscount(int storeId, String userId) throws LaiKeAPIException;

    /**
     * 获取分润基值
     *
     * @param storeId    -
     * @param goodsPrice - 商品售价
     * @param costPrice  - 商品成本价
     * @param orderPrice - 订单成交价
     * @param num        - 数量
     * @param pv         - pv值
     * @return BigDecimal
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022/9/8 15:07
     */
    BigDecimal getProfit(int storeId, BigDecimal goodsPrice, BigDecimal costPrice, BigDecimal orderPrice, BigDecimal num, BigDecimal pv) throws LaiKeAPIException;

    /**
     * 获取分销中心商品
     * @param vo
     * @param productTitle
     * @param sortKey
     * @param queue
     * @param cid
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> getGoodsList(MainVo vo, String productTitle, String sortKey, int queue, Integer cid,User user) throws LaiKeAPIException;

}
