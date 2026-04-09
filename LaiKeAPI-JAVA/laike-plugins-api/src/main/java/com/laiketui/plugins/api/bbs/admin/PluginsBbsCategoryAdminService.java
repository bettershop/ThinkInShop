package com.laiketui.plugins.api.bbs.admin;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.plugin.bbs.BbsCategoryVo;

import java.util.Map;

/**
 * @Author: liuao
 * @Date: 2025-09-29-13:54
 * @Description:
 */
public interface PluginsBbsCategoryAdminService
{

    /**
     * 频道列表
     * @param vo
     * @return
     * @throws LaiKeAPIException
     */
    Map<String,Object> list(BbsCategoryVo vo) throws LaiKeAPIException;

    /**
     * 设置/取消热门
     * @param vo
     * @param id
     */
    void setPopular(MainVo vo, Long id) throws LaiKeAPIException;

    /**
     * 添加/编辑频道
     * @param vo
     */
    void saveCategory(BbsCategoryVo vo) throws LaiKeAPIException;

    /**
     * 获取频道
     * @param vo
     * @param id
     * @return
     */
    Map<String,Object> getCategory(MainVo vo, Long id) throws LaiKeAPIException;

    /**
     * 置顶频道
     * @param vo
     * @param id
     */
    void topCategory(MainVo vo, Long id) throws LaiKeAPIException;

    /**
     * 删除频道
     * @param vo
     * @param id
     */
    void delCategory(MainVo vo, Long id) throws LaiKeAPIException;

    /**
     * 获取当前分类所有上级
     * @param vo
     * @param classId
     * @return
     */
    Map<String,Object> getClassLevelTopAllInfo(MainVo vo, Long classId);

}

