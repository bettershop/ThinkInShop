package com.laiketui.plugins.api.seckill;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;

import java.util.Map;

/**
 * 秒杀
 *
 * @author Trick
 * @date 2021/4/8 14:55
 */
public interface PluginsSecAppService
{

    /**
     * 秒杀首页数据
     * 【php seckill.seckillhome】
     *
     * @param vo        -
     * @param id        - 标签id
     * @param startDate - 开始时间
     * @param endDate   - 结束时间
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/4/8 15:17
     * - update 2021-10-21 10:01:38
     */
    @Deprecated
    Map<String, Object> index(MainVo vo, Integer id, String startDate, String endDate) throws LaiKeAPIException;

    default Map<String, Object> index(MainVo vo, String id, String startDate, String endDate) throws LaiKeAPIException
    {
        return null;
    }

    /**
     * 获取秒杀商品列表(只获取商品)
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022/9/5 17:53
     */
    default Map<String, Object> goodsList(MainVo vo) throws LaiKeAPIException
    {
        return null;
    }

    /**
     * 获取秒杀标签
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/10/22 15:00
     */
    default Map<String, Object> secondsLabelList(MainVo vo, Integer mchId) throws LaiKeAPIException
    {
        return null;
    }


    /**
     * 获取秒杀规则
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/10/21 14:53
     */
    default Map<String, Object> getSecondsRule(MainVo vo) throws LaiKeAPIException
    {
        return null;
    }


    /**
     * 秒杀订单列表
     * 【php seckill.seckillorder】
     *
     * @param vo        -
     * @param orderType -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/4/9 9:55
     */
    Map<String, Object> seckillOrder(MainVo vo, String orderType) throws LaiKeAPIException;

}
