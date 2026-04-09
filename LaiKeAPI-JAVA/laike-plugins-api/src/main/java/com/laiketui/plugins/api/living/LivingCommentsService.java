package com.laiketui.plugins.api.living;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.order.CommentsDetailInfoVo;
import com.laiketui.domain.vo.order.CommentsInfoVo;
import com.laiketui.domain.vo.order.UpdateCommentsInfoVo;
import com.laiketui.domain.vo.query.GetCommentsDetailInfoVo;

import java.util.Map;

/**
 * @author zhuqingyu
 * @create 2024/6/11
 */
public interface LivingCommentsService
{
    /**
     * 获取评论列表
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     */
    Map<String, Object> getCommentsInfo(CommentsInfoVo vo) throws LaiKeAPIException;

    /**
     * 获取评论详细信息
     *
     * @param vo  -
     * @param cid - 评论id
     * @return Map
     * @throws LaiKeAPIException -
     */
    Map<String, Object> getCommentsDetailInfoById(CommentsDetailInfoVo vo, int cid) throws LaiKeAPIException;

    /**
     * 获取评论详情回复列表
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException-
     */
    Map<String, Object> getCommentReplyList(GetCommentsDetailInfoVo vo) throws LaiKeAPIException;

    /**
     * 删除评论回复
     *
     * @param vo -
     * @param id -
     * @throws LaiKeAPIException-
     */
    void delCommentReply(MainVo vo, int id) throws LaiKeAPIException;

    /**
     * 修改评论信息
     *
     * @param vo -
     * @throws LaiKeAPIException -
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
     */
    boolean replyComments(MainVo vo, int commentId, String commentText) throws LaiKeAPIException;


    /**
     * 管理后台-回复
     *
     * @param vo          -
     * @param commentId   -
     * @param commentText -
     * @return boolean
     * @throws LaiKeAPIException -
     */
    boolean replyAdminComments(MainVo vo, int commentId, String commentText) throws LaiKeAPIException;


    /**
     * 删除评论
     *
     * @param vo        -
     * @param commentId -
     * @return boolean
     * @throws LaiKeAPIException -
     */
    boolean delComments(MainVo vo, int commentId) throws LaiKeAPIException;
}
