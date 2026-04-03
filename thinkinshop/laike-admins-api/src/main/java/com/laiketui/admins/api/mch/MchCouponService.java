package com.laiketui.admins.api.mch;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.coupon.AddCouponActivityVo;
import com.laiketui.domain.vo.coupon.CouponParmaVo;
import com.laiketui.domain.vo.coupon.CouponUserVo;
import com.laiketui.domain.vo.goods.GoodsConfigureVo;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * 优惠卷管理
 *
 * @author Trick
 * @date 2021/6/9 15:33
 */
public interface MchCouponService
{

    /**
     * 店铺优惠卷列表
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/6/9 15:39
     */
    Map<String, Object> index(CouponParmaVo vo) throws LaiKeAPIException;

    /**
     * 添加优惠卷页面
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/6/10 10:10
     */
    Map<String, Object> addPage(MainVo vo) throws LaiKeAPIException;

    /**
     * 添加优惠卷
     *
     * @param vo -
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/6/10 10:13
     */
    void add(AddCouponActivityVo vo) throws LaiKeAPIException;

    /**
     * 编辑优惠卷
     *
     * @param vo -
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/6/10 10:13
     */
    void modify(AddCouponActivityVo vo) throws LaiKeAPIException;


    /**
     * 加载编辑页面数据接口
     *
     * @param vo
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> modifyPage(CouponParmaVo vo) throws LaiKeAPIException;


    /**
     * 查看优惠卷
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/6/10 10:13
     */
    Map<String, Object> seeCoupon(CouponParmaVo vo) throws LaiKeAPIException;


    /**
     * 删除优惠卷活动
     *
     * @param vo -
     * @param id -
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/6/10 10:13
     */
    void del(MainVo vo, int id) throws LaiKeAPIException;

    /**
     * 批量删除
     *
     * @param vo  -
     * @param ids -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2023/1/17 11:27
     */
    void batchDel(MainVo vo, String ids) throws LaiKeAPIException;


    /**
     * 店铺分类
     *
     * @param vo -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/6/10 10:13
     */
    Map<String, Object> fenlei(MainVo vo) throws LaiKeAPIException;


    /**
     * 获取店铺商品信息
     *
     * @param vo   -
     * @param name -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/6/10 10:13
     */
    Map<String, Object> mchProduct(MainVo vo, String name) throws LaiKeAPIException;


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
     * @param vo
     * @param userIds
     * @param hid
     * @throws LaiKeAPIException
     */
    void receiveUserCoupon(MainVo vo, String userIds, int hid) throws LaiKeAPIException;


    /**
     * 获取优惠卷领取记录
     *
     * @param vo
     * @param hid
     * @param status
     * @param type
     * @param keyWord
     * @param response
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> seeCouponLogger(MainVo vo, int hid, Integer status, Integer type, String keyWord, HttpServletResponse response) throws LaiKeAPIException;


    /**
     * 查看优惠卷赠送记录
     *
     * @param vo
     * @param hid
     * @param state
     * @param type
     * @param keyWord
     * @param response
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> seeGiveCouponLogger(MainVo vo, int hid, Integer state, Integer type, String keyWord, HttpServletResponse response) throws LaiKeAPIException;

    /**
     * 活动是否显示-开关
     *
     * @param vo
     * @param hid
     * @return
     * @throws LaiKeAPIException
     */
    boolean activityisDisplay(MainVo vo, int hid) throws LaiKeAPIException;

    /**
     * 开启店铺主页领卷入口-开关
     *
     * @param vo
     * @param mchId
     * @return
     */
    Boolean isOpenCoupon(MainVo vo, Integer mchId) throws LaiKeAPIException;

    /**
     * 获取指定商品列表
     *
     * @param vo
     * @return Map
     * @throws LaiKeAPIException -
     */
    Map<String, Object> getSpecifiedGoodsInfo(GoodsConfigureVo vo, String id);
}
