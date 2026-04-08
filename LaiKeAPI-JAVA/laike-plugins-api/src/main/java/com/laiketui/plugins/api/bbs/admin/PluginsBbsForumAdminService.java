package com.laiketui.plugins.api.bbs.admin;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.plugin.bbs.BbsForumVo;

import java.util.Map;

/**
 * @Author: liuao
 * @Date: 2025-09-29-10:54
 * @Description:
 */
public interface PluginsBbsForumAdminService
{
    /**
     * 种草官列表
     * @param vo
     * @return
     */
    Map<String,Object> list(BbsForumVo vo) throws LaiKeAPIException;

    /**
     * 取消/设置种草官审核
     * @param vo
     * @param id
     */
    void underReview(MainVo vo, Long id) throws LaiKeAPIException;


    /**
     * 获取种草官审核列表
     * @param vo
     * @param keywords
     * @param status
     */
    Map<String,Object> forumReviewList(MainVo vo, String keywords, Integer status) throws LaiKeAPIException;

    /**
     * 获取种草官详情
     * @param vo
     * @param id
     * @return
     */
    Map<String,Object> view(MainVo vo, Long id) throws LaiKeAPIException;

    /**
     * 删除种草官
     * @param vo
     * @param id
     */
    void del(MainVo vo, Long id) throws LaiKeAPIException;

    /**
     * 审核种草官
     * @param vo
     * @param id
     * @param status
     * @param refuseText
     * @throws LaiKeAPIException
     */
    void forumReview(MainVo vo, Long id, Integer status, String refuseText) throws LaiKeAPIException;
}
