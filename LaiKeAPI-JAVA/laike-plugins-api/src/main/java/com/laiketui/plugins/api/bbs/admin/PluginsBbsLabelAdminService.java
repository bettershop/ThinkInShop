package com.laiketui.plugins.api.bbs.admin;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;

import java.util.Map;

/**
 * @Author: liuao
 * @Date: 2025-10-09-10:44
 * @Description:
 */
public interface PluginsBbsLabelAdminService
{

    /**
     * 话题列表
     * @param vo
     * @param name
     * @return
     */
    Map<String,Object> list(MainVo vo, String name) throws LaiKeAPIException;

    /**
     * 编辑/添加话题
     * @param vo
     * @param name
     * @param id
     * @throws LaiKeAPIException
     */
    void edit(MainVo vo, String name, Long id) throws LaiKeAPIException;

    /**
     * 删除话题
     * @param vo
     * @param ids
     * @throws LaiKeAPIException
     */
    void del(MainVo vo, String ids)  throws LaiKeAPIException;
}
