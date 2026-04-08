package com.laiketui.plugins.api.auction.mch;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.auction.AddSpecialVo;
import com.laiketui.domain.vo.order.CommentsInfoVo;
import com.laiketui.domain.vo.order.UpdateCommentsInfoVo;
import com.laiketui.domain.vo.query.GetCommentsDetailInfoVo;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * 竞拍店铺后台-专场相关
 *
 * @author Trick
 * @date 2022/8/1 18:52
 */
public interface PluginAuctionMchSpecialService
{

    /**
     * 获取店铺报名专场列表(店铺参与的报名专场)
     *
     * @param vo        -
     * @param key       -
     * @param status    -
     * @param startDate -
     * @param endDate   -
     * @param id        -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022/8/1 19:03
     */
    Map<String, Object> getSpecialList(MainVo vo, String key, Integer status, String startDate, String endDate, String id) throws LaiKeAPIException;


    /**
     * 获取店铺专场列表
     *
     * @param vo        -
     * @param key       -
     * @param status    -
     * @param startDate -
     * @param endDate   -
     * @param id        -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022/8/1 19:03
     */
    Map<String, Object> index(MainVo vo, String key, Integer status, String startDate, String endDate, String id) throws LaiKeAPIException;


    /**
     * 获取店铺专场编辑回显
     *
     * @param vo        -
     * @param specialId -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022/8/1 19:03
     */
    Map<String, Object> specialDetail(MainVo vo, String specialId) throws LaiKeAPIException;

    /**
     * 取消/删除专场
     *
     * @param vo -
     * @param id -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022/8/1 20:02
     */
    void cancelSpecial(MainVo vo, String id) throws LaiKeAPIException;

    /**
     * 查看拍品
     *
     * @param vo        -
     * @param specialId - 专场id
     * @param name      -
     * @param type      -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022/8/1 20:46
     */
    Map<String, Object> lookAuctionGoods(MainVo vo, String specialId, String name, Integer type) throws LaiKeAPIException;

    /**
     * 其它拍品
     *
     * @param vo        -
     * @param specialId - 专场id
     * @param key       -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022/8/1 20:46
     */
    Map<String, Object> lookAuctionOtherGoods(MainVo vo, String specialId, String key) throws LaiKeAPIException;

    /**
     * 移除竞拍商品
     *
     * @param vo -
     * @param id -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022/8/1 20:02
     */
    void removeAcGoods(MainVo vo, Integer id) throws LaiKeAPIException;

    /**
     * 添加/编辑我的专场
     *
     * @param vo      -
     * @param attrIds - 商品规格id集[{'attrId':1,'startingAmt':1.1,'markUpAmt':2.2}..]
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022-08-02 11:35:56
     */
    void addMySpecialList(AddSpecialVo vo, String attrIds) throws LaiKeAPIException;

    /**
     * 出价详情列表
     *
     * @param vo   -
     * @param acId -
     * @param key  -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022/8/2 11:49
     */
    Map<String, Object> bidList(MainVo vo, Integer acId, String key) throws LaiKeAPIException;


    /**
     * 获取还未参与的商品
     *
     * @param vo        -
     * @param brandId   -
     * @param classId   -
     * @param key       -
     * @param sessionId -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022-08-02 14:52:55
     */
    Map<String, Object> getProductList(MainVo vo, Integer brandId, Integer classId, String key, String sessionId) throws LaiKeAPIException;


    /**
     * 专场是否显示
     *
     * @param vo -
     * @param id -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022-08-03 10:09:40
     */
    void switchSpecial(MainVo vo, String id) throws LaiKeAPIException;


    /**
     * 商品是否显示
     *
     * @param vo -
     * @param id -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022-08-03 10:09:40
     */
    void switchGoods(MainVo vo, String id) throws LaiKeAPIException;

    /**
     * 获取保证金列表
     *
     * @param vo          -
     * @param key         -
     * @param specialName -
     * @param type        -
     * @param startDate   -
     * @param endDate     -
     * @param specialId   - 专场id
     * @return Map
     * @throws LaiKeAPIException-
     */
    Map<String, Object> getPromiseList(MainVo vo, String key, String specialName, Integer type, String startDate, String endDate, String specialId) throws LaiKeAPIException;

    /**
     * 获取评论列表
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/6 16:07
     */
    Map<String, Object> getCommentsInfo(CommentsInfoVo vo, HttpServletResponse response) throws LaiKeAPIException;

    /**
     * 获取评论详细信息
     *
     * @param vo  -
     * @param cid - 评论id
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/6 17:23
     */
    Map<String, Object> getCommentsDetailInfoById(GetCommentsDetailInfoVo vo, int cid) throws LaiKeAPIException;

    /**
     * 修改评论信息
     *
     * @param vo -
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/6 18:07
     */
    void updateCommentsDetailInfoById(UpdateCommentsInfoVo vo) throws LaiKeAPIException;

    /**
     * 删除评论信息
     *
     * @param vo
     * @param id
     * @throws LaiKeAPIException
     */
    void delComments(MainVo vo, String id, int type) throws LaiKeAPIException;

    /**
     * 回复
     *
     * @param vo          -
     * @param commentId   -
     * @param commentText -
     * @return boolean
     * @throws LaiKeAPIException -
     */
    boolean replyComments(MainVo vo, int commentId, String commentText) throws LaiKeAPIException;

}
