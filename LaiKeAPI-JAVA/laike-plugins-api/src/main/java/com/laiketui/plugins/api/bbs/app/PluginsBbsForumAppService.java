package com.laiketui.plugins.api.bbs.app;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.plugin.bbs.BbsForumVo;

import java.util.Map;

/**
 * @Author: liuao
 * @Date: 2025-10-13-10:53
 * @Description:
 */
public interface PluginsBbsForumAppService
{
    /**
     *
     * @param vo
     * @return
     */
    Map<String,Object> details(MainVo vo,Integer type,String user_id) throws LaiKeAPIException;

    /**
     * 获取关注列表
     * @param vo
     * @return
     */
    Map<String,Object> getFocusList(MainVo vo,Integer type) throws LaiKeAPIException;

    /**
     * 编辑/添加种草官
     * @param vo
     * @throws LaiKeAPIException
     */
    void addOrUpdate(BbsForumVo vo) throws LaiKeAPIException;
}
