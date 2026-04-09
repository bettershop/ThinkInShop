package com.laiketui.apps.api.user;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.goods.ProductShareVo;
import com.laiketui.domain.vo.user.GradeOrderVo;

import java.util.Map;

/**
 * 会员制接口
 *
 * @author Trick
 * @date 2020/12/21 17:35
 */
public interface AppsUserRechargeService
{


    /**
     * 获取我的详细数据
     * 【php rechargeService.index】
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/21 17:37
     */
    Map<String, Object> index(MainVo vo) throws LaiKeAPIException;


    /**
     * 会员等级列表展示
     * 【php rechargeAction.grade】
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2020/12/22 18:12
     */
    Map<String, Object> grade(MainVo vo) throws LaiKeAPIException;


    /**
     * 会员等级订单
     * 【php rechargeAction.gradeOrder】
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/23 12:02
     */
    Map<String, Object> gradeOrder(GradeOrderVo vo) throws LaiKeAPIException;


    /**
     * 会员升价渲染等级接口
     * 【php rechargeAction.upgrade】
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/23 19:03
     */
    Map<String, Object> upgrade(MainVo vo) throws LaiKeAPIException;


    /**
     * 会员等级中心
     * 【php rechargeAction.grade_center】
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/24 9:44
     */
    Map<String, Object> gradeCenter(MainVo vo) throws LaiKeAPIException;


    /**
     * 获取更多特惠商品
     * 【php rechargeAction.get_more】
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/24 11:10
     */
    Map<String, Object> getMore(MainVo vo) throws LaiKeAPIException;


    /**
     * 关闭自动续费
     * 【php rechargeAction.close】
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/24 14:17
     */
    Map<String, Object> closeAutomaticRenewal(MainVo vo) throws LaiKeAPIException;


    /**
     * 会员制分享-已移除
     * 【php rechargeAction.share】
     *
     * @param vo        -
     * @param storeType -
     * @param httpUrl   - 当前服务器url地址
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/24 14:37
     */
    @Deprecated
    Map<String, Object> share(MainVo vo, Integer storeType, String httpUrl) throws LaiKeAPIException;


    /**
     * 制作商品分享带参数二维码
     * 【php rechargeAction.product_share】
     *
     * @param vo      -
     * @param httpUrl - 当前路径
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/25 9:25
     */
    Map<String, Object> productShare(ProductShareVo vo, String httpUrl) throws LaiKeAPIException;
}
