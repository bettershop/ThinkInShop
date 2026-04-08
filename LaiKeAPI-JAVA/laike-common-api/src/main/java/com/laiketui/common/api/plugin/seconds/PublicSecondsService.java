package com.laiketui.common.api.plugin.seconds;

import com.laiketui.core.exception.LaiKeAPIException;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 公共秒杀
 *
 * @author Trick
 * @date 2021/4/9 16:25
 */
public interface PublicSecondsService
{


    /**
     * 秒杀结算
     *
     * @param storeId   -
     * @param goodsInfo - 秒杀商品信息
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/4/9 16:29
     */
    Map<String, Object> settlement(int storeId, List<Map<String, Object>> goodsInfo) throws LaiKeAPIException;

    /**
     * 获取秒杀状态
     *
     * @param secProId  - 秒杀商品id
     * @param startDate -
     * @param endDate   -
     * @return String
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/10/21 9:38
     */
    String getSecondsStatusName(int secProId, Date startDate, Date endDate) throws LaiKeAPIException;

    /**
     * 获取秒杀状态值
     *
     * @param storeId   -
     * @param secProId  -
     * @param startDate -
     * @param stockNum  - 秒杀库存
     * @param endDate   -
     * @param isUpdate  - 是否更新状态
     * @return int
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/10/21 11:19
     */
    int getSecondsStatus(int storeId, Integer mchId, int secProId, int stockNum, Date startDate, Date endDate, boolean isUpdate) throws LaiKeAPIException;

    /**
     * 获取秒杀状态值
     *
     * @param storeId   -
     * @param secProId  -
     * @param startDate -
     * @param stockNum  - 秒杀库存
     * @param endDate   -
     * @param isUpdate  - 是否更新状态
     * @return int
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/10/21 11:19
     */
    int getSecondsStatus(int storeId, int secProId, int stockNum, Date startDate, Date endDate, boolean isUpdate) throws LaiKeAPIException;

    int getSecondsStatus(int storeId, Integer mchId, int secProId, int stockNum, Date startDate, Date endDate) throws LaiKeAPIException;
}
