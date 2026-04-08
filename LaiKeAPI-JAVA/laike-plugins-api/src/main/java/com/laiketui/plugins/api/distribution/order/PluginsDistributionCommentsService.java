package com.laiketui.plugins.api.distribution.order;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.order.CommentsDetailInfoVo;
import com.laiketui.domain.vo.order.CommentsInfoVo;
import com.laiketui.domain.vo.order.UpdateCommentsInfoVo;
import com.laiketui.domain.vo.query.GetCommentsDetailInfoVo;

import java.util.Map;

/**
 * 评论管理
 *
 * @author Trick
 * @date 2021/1/6 15:57
 */
public interface PluginsDistributionCommentsService
{


    /**
     * 获取评论列表
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/6 16:07
     */
    Map<String, Object> getCommentsInfo(CommentsInfoVo vo) throws LaiKeAPIException;


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
    Map<String, Object> getCommentsDetailInfoById(CommentsDetailInfoVo vo, int cid) throws LaiKeAPIException;

    /**
     * 获取评论详情回复列表
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2023/3/6 14:44
     */
    Map<String, Object> getCommentReplyList(GetCommentsDetailInfoVo vo) throws LaiKeAPIException;

    /**
     * 删除评论回复
     *
     * @param vo -
     * @param id -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2023/3/7 15:49
     */
    void delCommentReply(MainVo vo, int id) throws LaiKeAPIException;

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
     * 回复
     *
     * @param vo          -
     * @param commentId   -
     * @param commentText -
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/7 9:30
     */
    boolean replyComments(MainVo vo, int commentId, String commentText) throws LaiKeAPIException;


    /**
     * 删除评论
     *
     * @param vo        -
     * @param commentId -
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/7 9:52
     */
    boolean delComments(MainVo vo, int commentId) throws LaiKeAPIException;
}
