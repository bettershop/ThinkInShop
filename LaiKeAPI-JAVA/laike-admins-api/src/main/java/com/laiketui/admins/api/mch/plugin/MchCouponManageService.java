package com.laiketui.admins.api.mch.plugin;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.coupon.AddCouponActivityVo;
import com.laiketui.domain.vo.coupon.AddCouponConfigVo;
import com.laiketui.domain.vo.coupon.CouponParmaVo;
import com.laiketui.domain.vo.coupon.CouponUserVo;

import java.util.Map;

/**
 * 插件管理
 *
 * @author Trick
 * @date 2021/1/22 14:38
 */
public interface MchCouponManageService
{


    /**
     * 获取商城优惠卷配置信息
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/22 15:43
     */
    Map<String, Object> getCouponConfigInfo(MainVo vo) throws LaiKeAPIException;


    /**
     * 添加/编辑优惠卷配置
     *
     * @param vo -
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/22 15:58
     */
    boolean addCouponConfig(AddCouponConfigVo vo) throws LaiKeAPIException;

    /**
     * 获取优惠卷信息
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/22 15:45
     */
    Map<String, Object> getCouponCardInfo(CouponParmaVo vo) throws LaiKeAPIException;


    /**
     * 获取赠卷会员列表
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/22 15:43
     */
    Map<String, Object> getGiveUserInfo(CouponUserVo vo) throws LaiKeAPIException;


    /**
     * 赠卷
     *
     * @param vo      -
     * @param userIds -
     * @param hid     -
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/25 14:59
     */
    boolean receiveUserCoupon(MainVo vo, String userIds, int hid) throws LaiKeAPIException;


    /**
     * 活动是否显示-开关
     *
     * @param vo  -
     * @param hid -
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/25 15:35
     */
    boolean activityisDisplay(MainVo vo, int hid) throws LaiKeAPIException;


    /**
     * 删除优惠卷活动
     *
     * @param vo  -
     * @param hid -
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/25 16:06
     */
    boolean delCoupon(MainVo vo, int hid) throws LaiKeAPIException;


    /**
     * 查看优惠卷领取记录
     *
     * @param vo     -
     * @param hid    -
     * @param status -
     * @param sNo    -
     * @param name   -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/25 16:19
     */
    Map<String, Object> seeCouponLogger(MainVo vo, int hid, Integer status, String sNo, String name) throws LaiKeAPIException;


    /**
     * 查看优惠卷赠送记录
     *
     * @param vo   -
     * @param hid  -
     * @param type -
     * @param sNo  -
     * @param name -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/25 17:39
     */
    Map<String, Object> seeGiveCouponLogger(MainVo vo, int hid, Integer type, String sNo, String name) throws LaiKeAPIException;


    /**
     * 获取指定商品列表
     *
     * @param vo        -
     * @param goodsName -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/26 10:10
     */
    Map<String, Object> getAssignGoods(MainVo vo, String goodsName) throws LaiKeAPIException;


    /**
     * 获取指定商品分类
     *
     * @param vo    -
     * @param mchId -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/26 11:00
     */
    Map<String, Object> getAssignGoodsClass(MainVo vo) throws LaiKeAPIException;


    /**
     * 添加优惠卷
     *
     * @param vo -
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/26 11:10
     */
    boolean addCoupon(AddCouponActivityVo vo) throws LaiKeAPIException;
}
