package com.laiketui.plugins.api.coupon;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.coupon.AddCouponActivityVo;
import com.laiketui.domain.vo.coupon.CouponParmaVo;

import java.util.List;
import java.util.Map;

/**
 * 优惠卷接口
 *
 * @author Trick
 * @date 2020/12/7 9:49
 */
public interface PluginCouponService
{


    /**
     * 获取我的优惠卷
     * 【php coupon.mycoupon】
     *
     * @param vo   -
     * @param type -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/7 11:42
     */
    Map<String, Object> mycoupon(MainVo vo, int type) throws LaiKeAPIException;


    /**
     * 店铺优惠券使用记录
     *
     * @param vo   -
     * @param acId - 优惠券活动id
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2023/1/18 10:59
     */
    Map<String, Object> mchUseRecord(MainVo vo, Integer acId) throws LaiKeAPIException;

    /**
     * 获取小程序优惠券活动
     * 【php coupon.index】
     *
     * @param vo      -
     * @param type    - 1=非店铺 other 默认店铺
     * @param mchName
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/7 16:39
     */
    Map<String, Object> index(MainVo vo, int type, String mchName) throws LaiKeAPIException;


    /**
     * 领取优惠卷
     * 【php coupon.index】
     *
     * @param vo -
     * @param id -
     * @return int
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/8 9:08
     */
    Map<String, Object> receive(MainVo vo, int id) throws LaiKeAPIException;


    /**
     * 获取商品可用优惠券活动
     * 【php coupon.pro_coupon】
     *
     * @param vo      -
     * @param goodsId -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/8 14:56
     */
    Map<String, Object> proCoupon(MainVo vo, int goodsId) throws LaiKeAPIException;


    /**
     * 获取店铺商品信息
     * 【php coupon.mch_coupon】
     *
     * @param vo    -
     * @param mchId -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/8 17:30
     */
    Map<String, Object> mchCoupon(MainVo vo, int mchId) throws LaiKeAPIException;


    /**
     * 店铺优惠卷列表
     * 【php coupon.mch_index】
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/8 17:42
     */
    Map<String, Object> mchIndex(CouponParmaVo vo) throws LaiKeAPIException;


    /**
     * 添加优惠卷页面数据
     * 【php coupon.add_page】
     *
     * @param vo    -
     * @param mchId -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/9 11:54
     */
    Map<String, Object> addPage(MainVo vo, int mchId) throws LaiKeAPIException;


    /**
     * 添加优惠卷
     * 【php coupon.add】
     *
     * @param vo -
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/9 13:46
     */
    boolean add(AddCouponActivityVo vo) throws LaiKeAPIException;


    /**
     * 编辑优惠卷
     * 【php coupon.add】
     *
     * @param vo -
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/9 18:49
     */
    void modify(AddCouponActivityVo vo) throws LaiKeAPIException;


    /**
     * 加载编辑页面数据接口
     * 【php coupon.modify_page】
     *
     * @param vo    -
     * @param mchId -
     * @param id    -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/10 9:19
     */
    Map<String, Object> modifyPage(MainVo vo, int mchId, int id) throws LaiKeAPIException;


    /**
     * 查看优惠卷
     * 【php coupon.see_coupon】
     *
     * @param vo     -
     * @param id     -
     * @param mchId  -
     * @param status -
     * @param sNo    -
     * @param name   -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/10 10:55
     */
    Map<String, Object> seeCoupon(MainVo vo, int id, int mchId, Integer status, String sNo, String name) throws LaiKeAPIException;


    /**
     * 删除优惠卷活动
     * 【php coupon.del】
     *
     * @param vo    -
     * @param id    -
     * @param mchId -
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/10 14:28
     */
    boolean del(MainVo vo, int id, int mchId) throws LaiKeAPIException;

    /**
     * 批量删除我的优惠卷
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
     * 【php coupon.fenlei】
     *
     * @param vo    -
     * @param mchId -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/10 15:39
     */
    List<Map<String, Object>> fenlei(MainVo vo, int mchId) throws LaiKeAPIException;


    /**
     * 获取店铺商品信息
     * 【php coupon.mch_product】
     *
     * @param vo    -
     * @param mchId -
     * @param name  -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/10 17:22
     */
    List<Map<String, Object>> mchProduct(MainVo vo, int mchId, String name) throws LaiKeAPIException;
}
