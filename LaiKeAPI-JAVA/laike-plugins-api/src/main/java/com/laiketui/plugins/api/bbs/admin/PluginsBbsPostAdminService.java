package com.laiketui.plugins.api.bbs.admin;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.plugin.bbs.BbsPostVo;

import java.util.Map;

/**
 * @Author: liuao
 * @Date: 2025-09-30-10:18
 * @Description:
 */
public interface PluginsBbsPostAdminService
{
    /**
     * 文章列表
     * @param vo
     * @return
     */
    Map<String,Object> list(BbsPostVo vo) throws LaiKeAPIException;

    /**
     * 删除文章
     * @param vo
     * @param id
     * @throws LaiKeAPIException
     */
    void del(MainVo vo, String id) throws LaiKeAPIException;


    /**
     * 查看文章详情
     * @param vo
     * @param post_id
     * @return
     */
    Map<String,Object> view(MainVo vo, Long post_id) throws LaiKeAPIException;

    /**
     * 审核文章
     * @param vo
     * @param id
     * @param status
     */
    void reviewPost(MainVo vo, Long id, Integer status,String refuse_text) throws LaiKeAPIException;

    /**
     * 设置推荐
     * @param vo
     * @param id
     * @throws LaiKeAPIException
     */
    void setRecommend(MainVo vo, Long id) throws LaiKeAPIException;
}
