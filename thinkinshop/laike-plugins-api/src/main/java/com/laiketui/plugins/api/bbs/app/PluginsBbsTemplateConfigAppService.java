package com.laiketui.plugins.api.bbs.app;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;

import java.util.Map;

/**
 * @Author: liuao
 * @Date: 2025-10-27-11:27
 * @Description:
 */
public interface PluginsBbsTemplateConfigAppService
{
    /**
     * 获取模板
     * @param vo
     * @return
     */
    Map<String,Object> list(MainVo vo) throws LaiKeAPIException;
}
