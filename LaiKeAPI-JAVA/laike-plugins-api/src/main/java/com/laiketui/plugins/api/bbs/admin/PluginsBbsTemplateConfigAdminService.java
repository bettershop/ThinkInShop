package com.laiketui.plugins.api.bbs.admin;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.plugin.bbs.BbsTemplateVo;

import java.util.Map;

/**
 * @Author: liuao
 * @Date: 2025-10-27-10:57
 * @Description:
 */
public interface PluginsBbsTemplateConfigAdminService
{
    /**
     * 模板列表
     * @param vo
     * @return
     */
    Map<String,Object> list(MainVo vo) throws LaiKeAPIException;

    /**
     * 添加/编辑模板
     * @param vo
     * @throws LaiKeAPIException
     */
    void addOrUpdate(BbsTemplateVo vo) throws LaiKeAPIException;

    /**
     * 删除模板
     * @param vo
     * @param ids
     * @throws LaiKeAPIException
     */
    void del(MainVo vo, String ids) throws LaiKeAPIException;
}
