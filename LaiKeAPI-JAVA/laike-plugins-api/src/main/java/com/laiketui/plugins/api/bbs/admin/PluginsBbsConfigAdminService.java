package com.laiketui.plugins.api.bbs.admin;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.plugin.bbs.BbsConfigVo;

import java.util.Map;

/**
 * @Author: liuao
 * @Date: 2025-09-29-10:09
 * @Description:
 */
public interface PluginsBbsConfigAdminService
{

    /**
     * 获取种草配置
     * @param vo
     * @return
     * @throws LaiKeAPIException
     */
    Map<String,Object> getConfig(MainVo vo) throws LaiKeAPIException;

    /**
     * 编辑种草配置
     * @param vo
     */
    void setConfig(BbsConfigVo vo) throws LaiKeAPIException ;

    /**
     * 获取模板，区域字典数据
     * @param vo
     * @return
     * @throws LaiKeAPIException
     */
    Map<String,Object> getDictData(MainVo vo) throws LaiKeAPIException;
}
