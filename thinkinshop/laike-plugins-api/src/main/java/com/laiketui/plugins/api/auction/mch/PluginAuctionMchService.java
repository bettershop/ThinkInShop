package com.laiketui.plugins.api.auction.mch;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.core.utils.tool.DateUtil;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.auction.AddSpecialVo;

import java.util.Date;
import java.util.Map;

/**
 * 竞拍店铺相关
 *
 * @author Trick
 * @date 2022/7/12 15:25
 */
public interface PluginAuctionMchService
{
    /**
     * 竞拍详情-店铺主页数据
     *
     * @param vo        -
     * @param specialId - 专场id
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022/7/12 15:29
     */
    Map<String, Object> index(MainVo vo, String specialId) throws LaiKeAPIException;

    /**
     * 获取专场所有场次
     *
     * @param vo        -
     * @param specialId -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022/7/13 18:19
     */
    Map<String, Object> getSessionList(MainVo vo, String specialId) throws LaiKeAPIException;

    /**
     * 获取场次下所有商品信息
     *
     * @param vo        -
     * @param sessionId - 场次id
     * @param sortType  - 价格排序类型 1=降序 2=升序
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022/7/13 17:27
     */
    Map<String, Object> getSessionGoodsList(MainVo vo, String sessionId, int sortType) throws LaiKeAPIException;

    /**
     * 获取我的专场
     *
     * @param vo        -
     * @param status    - 1=未开始 2=进行中 3=已结束 默认全部
     * @param specialId - 专场id
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022/7/12 15:29
     */
    Map<String, Object> getMySpecialList(MainVo vo, Integer status) throws LaiKeAPIException;

    /**
     * 获取我的专场-明细
     *
     * @param vo        -
     * @param specialId -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022/7/18 17:30
     */
    Map<String, Object> getMySpecialDetailList(MainVo vo, String specialId) throws LaiKeAPIException;

    /**
     * 获取商品列表
     *
     * @param vo        -
     * @param name      -
     * @param specialId - 专场id
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022/7/18 16:12
     */
    Map<String, Object> getGoodsList(MainVo vo, String name, String specialId) throws LaiKeAPIException;

    /**
     * 添加/编辑我的专场
     *
     * @param vo      -
     * @param attrIds - 商品规格id集[{'attrId':1,'startingAmt':1.1,'markUpAmt':2.2}..]
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022/7/12 15:31
     */
    void addMySpecialList(AddSpecialVo vo, String attrIds) throws LaiKeAPIException;

    public static void main(String[] args)
    {
        Date validateDate = DateUtil.dateFormateToDate("2022-12-29 15:46:09", GloabConst.TimePattern.YMDHMS);
        System.out.println(!DateUtil.dateCompare(validateDate, DateUtil.dateFormateToDate("2022-12-29 15:53:38", GloabConst.TimePattern.YMDHMS)));
    }

    /**
     * 编辑店铺专场回显
     *
     * @param vo        -
     * @param specialId -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022/8/9 17:10
     */
    Map<String, Object> mySpecialDetail(MainVo vo, String specialId) throws LaiKeAPIException;

    /**
     * 删除专场
     *
     * @param vo -
     * @param id -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022/7/1 15:51
     */
    void delSpecial(MainVo vo, String id) throws LaiKeAPIException;

    /**
     * 获取我的拍品列表
     *
     * @param vo     -
     * @param status - 1=待发布 2=待参与 3=待开始 4=竞拍中 5=已结束
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022/7/12 15:33
     */
    Map<String, Object> getMyAuctionGoodsList(MainVo vo, Integer status) throws LaiKeAPIException;

    /**
     * 活动申请列表
     *
     * @param vo        -
     * @param status    - 1=未报名 2=已报名 3=已结束
     * @param specialId - 专场id
     * @param shopId    - 店铺id
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022/7/12 15:46
     */
    Map<String, Object> getAuctionSignUpList(MainVo vo, Integer status, String specialId,Integer shopId) throws LaiKeAPIException;

    /**
     * 查看拍品
     *
     * @param vo        -
     * @param specialId -
     * @param isMy      - 是否查看我参与的竞拍商品列表
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022/7/19 17:43
     */
    Map<String, Object> specialGoodsList(MainVo vo, String specialId, boolean isMy) throws LaiKeAPIException;

    /**
     * 确认报名
     *
     * @param vo        -
     * @param specialId -
     * @param attrs     -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022/7/27 10:44
     */
    void sign(MainVo vo, String specialId, String attrs) throws LaiKeAPIException;

    /**
     * 删除竞拍商品
     *
     * @param vo   -
     * @param acId -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022/7/21 15:08
     */
    void delGoods(MainVo vo, Integer acId) throws LaiKeAPIException;
}
