package com.laiketui.plugins.api.bbs.admin;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.plugin.bbs.BbsCommentVo;

import java.util.Map;

/**
 * @Author: liuao
 * @Date: 2025-10-16-14:36
 * @Description:
 */
public interface PluginsBbsCommentAdminService
{
    /**
     * 获取评论内容
     * @param vo
     * @return
     */
    Map<String,Object> list(BbsCommentVo vo) throws LaiKeAPIException;

    /**
     * 获取评论详情
     * @param vo
     * @param id
     * @return
     */
    Map<String,Object> getById(MainVo vo, Long id) throws LaiKeAPIException;

    /**
     * 批量删除
     * @param vo
     * @param ids
     */
    void del(MainVo vo, String ids) throws LaiKeAPIException;
}
