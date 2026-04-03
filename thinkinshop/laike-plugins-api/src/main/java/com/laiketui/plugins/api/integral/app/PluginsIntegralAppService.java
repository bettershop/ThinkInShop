package com.laiketui.plugins.api.integral.app;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.OrderVo;

import java.util.Map;

/**
 * 积分商城
 *
 * @author Trick
 * @date 2021/4/14 10:09
 */
public interface PluginsIntegralAppService
{


    /**
     * 我的积分列表
     *
     * @param vo   -
     * @param type - 1 获取积分明细 2 使用明细 else 全部明细
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/2/8 17:21
     */
    Map<String, Object> getSignInfo(MainVo vo, Integer type) throws LaiKeAPIException;

    Map<String, Object> getSignInfo(MainVo vo, Integer type, Integer specifyType,
                                    String signTime, String sNo) throws LaiKeAPIException;

    /**
     * 获取积分使用说明
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/11/9 14:17
     */
    Map<String, Object> instructions(MainVo vo) throws LaiKeAPIException;


    /**
     * 积分商城首页
     *
     * @param vo        -
     * @param goodsName -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/4/14 10:11
     */
    Map<String, Object> index(MainVo vo, String goodsName, Integer mchId) throws LaiKeAPIException;


    /**
     * 获取积分规则
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/4/14 10:40
     */
    Map<String, Object> rule(MainVo vo) throws LaiKeAPIException;


    /**
     * 获取商品明细
     *
     * @param vo -
     * @param id -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/4/14 10:47
     */
    Map<String, Object> goodsdetail(MainVo vo, int id) throws LaiKeAPIException;

    /**
     * 我的兑换
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/11/2 11:07
     */
    Map<String, Object> integralOrder(OrderVo vo) throws LaiKeAPIException;

    boolean deleteSign(MainVo vo, String ids);
}
