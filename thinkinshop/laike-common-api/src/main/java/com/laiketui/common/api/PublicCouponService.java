package com.laiketui.common.api;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.coupon.CouponConfigModel;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.PageModel;
import com.laiketui.domain.vo.coupon.AddCouponActivityVo;
import com.laiketui.domain.vo.coupon.CouponParmaVo;
import com.laiketui.domain.vo.coupon.SeeCouponVo;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 优惠券通用类
 * 【暂时只支持商城配置优惠券】
 *
 * @author wangxian
 */
public interface PublicCouponService
{

    /**
     * 优惠卷首页
     * 【php>couponAction.test()】
     *
     * @param storeId -
     * @return Integer
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/10/30 14:58
     */
    Integer index(int storeId) throws LaiKeAPIException;

    /**
     * 获取店铺的优惠券
     *
     * @param storeId
     * @param userId
     * @param products
     * @param couponIds
     * @param canshu
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> settlementStoreCoupons(int storeId, String userId, List<Map<String, Object>> products, String couponIds, boolean canshu, int is_self_delivery) throws LaiKeAPIException;


    /**
     * 获取平台的优惠券
     *
     * @param storeId   - 商城id
     * @param userId    - 下单人
     * @param products  - 商品信息
     * @param couponIds - 使用的优惠券id,【店铺券,平台券】
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022/12/28 11:28
     */
    Map<String, Object> settlementPlaformCoupons(int storeId, String userId, List<Map<String, Object>> products, String couponIds, boolean canshu) throws LaiKeAPIException;

    /**
     * 获取用户优惠卷
     * 【php coupon.mycoupon】
     *
     * @param storeId -
     * @param userId  -
     * @param type    - 优惠卷类型 0=去丢换 1=已使用 2=已过期
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/7 11:54
     */
    Map<String, Object> mycoupon(int storeId, String userId, Integer type) throws LaiKeAPIException;

    /**
     * PC商城-领券中心
     * 【php coupon.pc_coupon】
     *
     * @param storeId -
     * @param userId  -
     * @param type    -
     * @return List
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/6/17 16:37
     */
    List<Map<String, Object>> pcCoupon(int storeId, String userId, Integer type) throws LaiKeAPIException;

    /**
     * 更新优惠券
     *
     * @param storeId
     * @param userId
     * @param couponIds
     * @param type
     * @return
     */
    int updateCoupons(int storeId, String userId, String couponIds, int type);

    /**
     * 关联订单和优惠券
     *
     * @param storeId
     * @param userId
     * @param couponIds
     * @param sNo
     * @param type
     * @return
     */
    int couponWithOrder(int storeId, String userId, String couponIds, String sNo, String type);


    /**
     * 领取优惠卷
     * 【php coupon.receive】
     *
     * @param storeId -
     * @param userId  -
     * @param id      - 活动id
     * @return String
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/8 9:18
     */
    String receive(int storeId, String userId, int id) throws LaiKeAPIException;


    /**
     * 赠卷会员列表
     * 【php coupon.get_user】
     *
     * @param storeId   -
     * @param hid       -
     * @param grade     -
     * @param name      -
     * @param pageModel -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/8 9:18
     */
    Map<String, Object> getUser(int storeId, int hid, Integer grade, String name, PageModel pageModel) throws LaiKeAPIException;

    /**
     * 拆单
     *
     * @param storeId
     * @param userId
     * @param sNo
     * @return
     */
    List<Map<String, Object>> splitOrder(int storeId, String userId, String sNo) throws LaiKeAPIException;


    /**
     * 虚拟商品拆单
     *
     * @param storeId
     * @param userId
     * @param sNo
     * @return
     */
    List<Map<String, Object>> splitOrderForVI(int storeId, String userId, String sNo) throws LaiKeAPIException;

    /**
     * 移动端-领券中心
     * 【php coupon.mobile_terminal_coupon_center】
     *
     * @param storeId   -
     * @param userId    -
     * @param type      - 类型 1=非店铺 other 默认店铺
     * @param pageModel -
     * @param mchName
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/7 17:50
     */
    Map<String, Object> mobileTerminalCouponCenter(int storeId, String userId, Integer type, PageModel pageModel, String mchName) throws LaiKeAPIException;


    /**
     * 移动端获取商品可用优惠券活动
     * 【php coupon.pro_coupon】
     *
     * @param storeId -
     * @param userId  -
     * @param goodsId -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/8 11:09
     */
    List<Map<String, Object>> proCoupon(int storeId, String userId, int goodsId) throws LaiKeAPIException;


    /**
     * PC端-店铺获取商品可用优惠券活动
     * 【php coupon.mch_coupon】
     *
     * @param storeId -
     * @param userId  -
     * @param mchId   -
     * @param type    - 1=立即领取 other 领取
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/8 17:06
     */
    List<Map<String, Object>> mchCoupon(int storeId, String userId, int mchId, int type) throws LaiKeAPIException;


    /**
     * 店铺优惠卷
     * 【php coupon.store_coupons】
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/8 17:48
     */
    Map<String, Object> storeCoupons(CouponParmaVo vo) throws LaiKeAPIException;


    /**
     * 添加店铺优惠卷页面数据
     * 【php coupon.add_store_coupons_page】
     *
     * @param storeId -
     * @param userId  -
     * @param mchId   -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/9 11:13
     */
    Map<String, Object> addStoreCouponsPage(int storeId, String userId, int mchId) throws LaiKeAPIException;


    /**
     * 发行优惠卷
     * 【php coupon.add_store_coupons】
     *
     * @param vo     -
     * @param userId -
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/9 13:56
     */
    boolean addStoreCoupons(AddCouponActivityVo vo, String userId) throws LaiKeAPIException;


    /**
     * 根据id获取优惠卷信息
     * 【php coupon.modify_store_coupons_page】
     *
     * @param storeId -
     * @param id      -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/10 9:22
     */
    Map<String, Object> getCouponsInfoById(int storeId, int id) throws LaiKeAPIException;


    /**
     * 店铺查看优惠卷领取信息
     * 【php coupon.see_coupon】
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/10 11:03
     */
    Map<String, Object> seeCoupon(SeeCouponVo vo) throws LaiKeAPIException;

    Map<String, Object> seeCoupon(int storeId, int id, Integer mchId, Integer status, Integer type, String sNo, String name, String keyWord, String pageTo, PageModel pageModel) throws LaiKeAPIException;

    Map<String, Object> seeCoupon(int storeId, int id, Integer mchId, Integer status, String sNo, String name, String pageTo, PageModel pageModel) throws LaiKeAPIException;


    /**
     * 删除店铺优惠券活动
     * 【php coupon.del_mch_coupon】
     *
     * @param storeId -
     * @param id      -
     * @param mchId   -
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/10 14:30
     */
    boolean delMchCoupon(int storeId, int id, Integer mchId) throws LaiKeAPIException;

    /**
     * 批量删除优惠券活动
     *
     * @param storeId -
     * @param ids     -
     * @param mchId   -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2023/1/17 11:15
     */
    void delBatchMchCoupon(int storeId, List<String> ids, Integer mchId) throws LaiKeAPIException;

    /**
     * 批量删除优惠券-[我的优惠券]
     *
     * @param storeId -
     * @param ids     -
     * @param userId  -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2023/1/17 11:15
     */
    void delBatchMyCoupon(int storeId, List<String> ids, String userId) throws LaiKeAPIException;

    /**
     * 获取店铺所有商品类别
     * 【php coupon.mch_fenlei】
     *
     * @param storeId -
     * @param mchId   -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/10 15:59
     */
    List<Map<String, Object>> mchFenlei(int storeId, Integer mchId) throws LaiKeAPIException;


    /**
     * 获取店铺商品信息
     * 【php coupon.mchProduct】
     *
     * @param storeId   -
     * @param mchId     -
     * @param name      - 商品名称
     * @param pageModel -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/10 17:23
     */
    List<Map<String, Object>> mchProduct(MainVo vo, int mchId, String name) throws LaiKeAPIException;


    /**
     * 赠送优惠卷
     * 【php coupon.gift_user】
     *
     * @param storeId    -
     * @param userIdList -
     * @param hid        -
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/23 15:56, 2021-01-25 17:24:27
     */
    boolean giveCoupons(int storeId, List<String> userIdList, int hid) throws LaiKeAPIException;

    /**
     * 查询用户可用会员优惠券
     *
     * @param storeId
     * @param userId
     * @return
     */
    List<Map<String, Object>> getUserCoupon(int storeId, String userId, BigDecimal amount) throws LaiKeAPIException;

    /**
     * 获取优惠券配置信息
     *
     * @param storeId -
     * @param mchId   -
     * @return CouponConfigModel
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022/12/14 10:59
     */
    CouponConfigModel getCouponConfigInfo(int storeId, int mchId) throws LaiKeAPIException;

}
