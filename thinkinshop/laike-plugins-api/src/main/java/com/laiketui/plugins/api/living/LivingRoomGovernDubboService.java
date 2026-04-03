package com.laiketui.plugins.api.living;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.living.*;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * @author zhuqingyu
 * @create 2024/6/3
 * <p>
 * 直播间管理
 */
public interface LivingRoomGovernDubboService
{
    /**
     * 直播间管理-直播场次
     *
     * @param vo
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> getIndex(GovernIndexVo vo) throws LaiKeAPIException;

    /**
     * 直播间管理-查询商品
     *
     * @param vo
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> queryLivingPro(QueryLiveProVo vo) throws LaiKeAPIException;

    /**
     * 直播间管理-查询订单
     *
     * @param vo
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> queryLivingOrder(QueryLiveOrderVo vo) throws LaiKeAPIException;

    /**
     * 直播间管理-查询关注列表
     *
     * @param vo
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> queryLivingFollow(QueryLiveFollowVo vo) throws LaiKeAPIException;

    /**
     * 添加直播
     *
     * @param vo
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> addLivingRoom(AddLivingRoomVo vo) throws LaiKeAPIException;

    /**
     * 修改直播状态
     *
     * @param vo
     * @param roomId
     * @return
     * @throws LaiKeAPIException
     */
    void updateLivingStatus(MainVo vo, Integer roomId) throws LaiKeAPIException;

    /**
     * 删除直播
     *
     * @param vo
     * @param roomId
     * @return
     * @throws LaiKeAPIException
     */
    void deleteLivingRoom(MainVo vo, String roomId) throws LaiKeAPIException;

    /**
     * 主播中心-查询
     *
     * @param vo
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> selectLivingUser(MainVo vo) throws LaiKeAPIException;

    /**
     * 查询直播间管理
     *
     * @param vo
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> queryGovernIndex(QueryLiveFollowVo vo) throws LaiKeAPIException;

    /**
     * 根据id查询直播间信息
     *
     * @param vo
     * @param id 直播id
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> queryLivingById(MainVo vo, String id) throws LaiKeAPIException;

    /**
     * 回复或者评论
     *
     * @param vo
     * @return
     * @throws LaiKeAPIException
     */
    void replyOrComment(LivingCommentVo vo) throws LaiKeAPIException;

    /**
     * 结束直播
     *
     * @param vo
     * @param roomId 直播id
     * @throws LaiKeAPIException
     */
    void cancelLiving(MainVo vo, Integer roomId) throws LaiKeAPIException;

    /**
     * 根据直播间ID查询评论
     *
     * @param vo
     * @param roomId 直播间ID
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> queryCommentByRoomId(MainVo vo, Integer roomId) throws LaiKeAPIException;

    /**
     * 查询粉丝
     *
     * @param vo
     * @param keyWord
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> myFans(MainVo vo, String keyWord) throws LaiKeAPIException;

    /**
     * 关注/取消关注
     *
     * @param vo
     * @param user_id
     * @throws LaiKeAPIException
     */
    void followAnchor(MainVo vo, String user_id) throws LaiKeAPIException;

    /**
     * 根据直播间id查询直播数据
     *
     * @param vo
     * @param roomId
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> queryDataByRoomId(MainVo vo, String roomId) throws LaiKeAPIException;

    /**
     * 根据直播间ID查询订单信息
     *
     * @param vo
     * @param keyWord
     * @param status
     * @param roomId
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> queryLivingDataDetails(MainVo vo, String keyWord, String status, String roomId) throws LaiKeAPIException;

    /**
     * 查询我点赞过的直播
     *
     * @param vo
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> queryMyLikeLiving(MainVo vo) throws LaiKeAPIException;

    /**
     * 管理后台-查看观众
     *
     * @param vo
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> adminQueryAudience(QueryLiveFollowVo vo) throws LaiKeAPIException;

    /**
     * 查询转出记录
     *
     * @param vo
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> queryRecord(QueryLiveFollowVo vo, HttpServletResponse response) throws LaiKeAPIException;

    /**
     * 修改排序
     *
     * @param vo
     * @return
     * @throws LaiKeAPIException
     */
    void updateRoomSort(MainVo vo, Integer roomId, Integer sort) throws LaiKeAPIException;
}
