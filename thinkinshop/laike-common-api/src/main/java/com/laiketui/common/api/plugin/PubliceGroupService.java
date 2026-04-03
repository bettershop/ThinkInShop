package com.laiketui.common.api.plugin;

import com.laiketui.core.exception.LaiKeAPIException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 公共拼团
 *
 * @author Trick
 * @date 2021/2/22 15:31
 */
public interface PubliceGroupService
{


    /**
     * 获取正在拼团得商品
     * 【php indexAction.go_group】
     *
     * @param storeId   -
     * @param pageStart -
     * @param pageEnd   -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/2/22 15:33
     */
    List<Map<String, Object>> getGroupGoodsInfo(int storeId, int pageStart, int pageEnd) throws LaiKeAPIException;


    /**
     * 获取拼团价格
     *
     * @param storeId       -
     * @param groupLevelStr - 拼团等级参数 a:2:{i:2;s:5:"70~60";i:4;s:5:"65~55";}
     * @param minPrice      - 商品最低价格
     * @param openNum       - 开团人数 - 0表示获取最小团的折扣比例
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/3/31 16:17
     */
    Map<String, BigDecimal> getGroupDiscountPrice(int storeId, String groupLevelStr, BigDecimal minPrice, int openNum) throws LaiKeAPIException;


    /**
     * 拼团规则校验
     *
     * @param storeId -
     * @param userId  -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/3/30 14:11
     */
    Map<String, Boolean> validataGroup(int storeId, String userId) throws LaiKeAPIException;

    /**
     * 拼团规则校验
     *
     * @param storeId    -
     * @param userId     -
     * @param isPlatForm -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021-04-26 15:46:33
     */
    Map<String, Boolean> validataGroup(int storeId, String userId, boolean isPlatForm) throws LaiKeAPIException;


    /**
     * 回退商品库存
     *
     * @param storeId    -
     * @param orderno    -
     * @param activityNo - 活动编号
     * @param text       - 操作记录文字
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/3/31 18:15
     */
    boolean reBackGoodsNum(int storeId, String orderno, int activityNo, String text) throws LaiKeAPIException;


    /**
     * 关闭未支付的订单
     *
     * @param storeId -
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/4/6 14:56
     */
    void closeUnPaidOrder(int storeId) throws LaiKeAPIException;


    /**
     * 拼团任务
     * 【php go_group.dotask】
     *
     * @param storeId -
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/4/6 16:54
     */
    void ptTask(int storeId) throws LaiKeAPIException;
}
