package com.laiketui.plugins.api.pc.mall;


import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;

import java.util.Map;

/**
 * 优惠卷模块
 *
 * @author Trick
 * @date 2021/6/24 15:28
 */
public interface AppsMallCouponService
{

    /**
     * 小程序优惠卷
     *
     * @param vo      -
     * @param type
     * @param mchName
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/6/16 16:52
     */
    Map<String, Object> index(MainVo vo, Integer type, String mchName) throws LaiKeAPIException;

    /**
     * 领取优惠卷
     *
     * @param vo -
     * @param id -
     * @return int
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021-06-24 16:02:08
     */
    Map<String, Object> receive(MainVo vo, int id) throws LaiKeAPIException;


    /**
     * 获取我的优惠卷
     *
     * @param vo   -
     * @param type - 优惠卷类型 0=去兑换/去使用 1=已使用 2=已过期
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/7 11:42
     */
    Map<String, Object> myCoupon(MainVo vo, int type) throws LaiKeAPIException;


    /**
     * 获取商品可用优惠券活动
     *
     * @param vo      -
     * @param goodsId -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021-06-24 16:20:14
     */
    Map<String, Object> proCoupon(MainVo vo, int goodsId) throws LaiKeAPIException;


    /**
     * 获取店铺商品信息
     *
     * @param vo    -
     * @param mchId -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021-06-24 16:31:39
     */
    Map<String, Object> mchCoupon(MainVo vo, int mchId) throws LaiKeAPIException;

}
