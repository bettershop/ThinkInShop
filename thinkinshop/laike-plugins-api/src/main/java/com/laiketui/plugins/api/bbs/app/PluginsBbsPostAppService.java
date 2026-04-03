package com.laiketui.plugins.api.bbs.app;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.plugin.bbs.BbsPostVo;

import java.util.Map;

/**
 * @Author: liuao
 * @Date: 2025-10-10-9:14
 * @Description:
 */
public interface PluginsBbsPostAppService
{
    /**
     * 文章列表
     * @param vo
     * @param type
     * @return
     */
    Map<String,Object> list(MainVo vo, Integer type,String category_id,Long post_id,String key_word,Integer is_detail,Integer status) throws LaiKeAPIException;

    /**
     * 发布笔记
     * @param vo
     */
    void add(BbsPostVo vo) throws LaiKeAPIException;

    /**
     * 获取商品列表
     * @param vo
     * @param name
     * @return
     */
    Map<String,Object> getProList(MainVo vo, String name,Integer cid) throws LaiKeAPIException;

    /**
     * 获取商品分类
     * @param vo
     * @return
     */
    Map<String,Object> getClassList(MainVo vo) throws LaiKeAPIException;

    /**
     * 点赞/取消点赞文章
     * @param vo
     * @param id
     */
    void likePost(MainVo vo, Long id) throws LaiKeAPIException;

    /**
     * 文章详情
     * @param vo
     * @param id
     * @return
     */
    Map<String,Object> details(MainVo vo, Long id) throws LaiKeAPIException;

    /**
     * 收藏/取消收藏文章
     * @param vo
     * @param id
     */
    void collectPost(MainVo vo, Long id) throws LaiKeAPIException;

    /**
     * 关注/取消关注种草官
     * @param vo
     * @param id
     * @throws LaiKeAPIException
     */
    void follow(MainVo vo, Long id) throws LaiKeAPIException;

    /**
     * 获取评论列表
     * @param vo
     * @param id
     * @return
     */
    Map<String,Object> getCommentList(MainVo vo, Long id) throws LaiKeAPIException;

    /**
     * 添加评论
     * @param vo
     * @param id
     * @param content
     * @throws LaiKeAPIException
     */
    Map<String,Object> commentPost(MainVo vo, Long id, String content,Long parentId,Long top_id) throws LaiKeAPIException;

    /**
     * 点赞/取消点赞评论
     * @param vo
     * @param id
     * @throws LaiKeAPIException
     */
    void commentLike(MainVo vo, Long id) throws  LaiKeAPIException;

    /**
     * 添加视频
     * @param vo
     * @param fileId
     */
    Map<String,Object> addVideo(MainVo vo, String fileId) throws LaiKeAPIException;

    /**
     * 获取下级评论
     * @param vo
     * @param id
     * @return
     */
    Map<String,Object> getChildComment(MainVo vo, Long id) throws LaiKeAPIException;

    /**
     * 分享文章
     * @param vo
     * @param id
     */
    void sharePost(MainVo vo, Long id) throws LaiKeAPIException;

    /**
     * 删除文章
     * @param vo
     * @param id
     * @throws LaiKeAPIException
     */
    void del(MainVo vo, String id) throws LaiKeAPIException;

    /**
     * 编辑文章
     * @param vo
     * @throws LaiKeAPIException
     */
    void edit(BbsPostVo vo) throws LaiKeAPIException;

    /**
     * 获取频道
     * @param vo
     * @return
     * @throws LaiKeAPIException
     */
    Map<String,Object> getChannel(MainVo vo) throws LaiKeAPIException;
}
