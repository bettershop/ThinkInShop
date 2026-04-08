package com.laiketui.plugins.api.bbs.app;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;

import java.util.Map;

/**
 * @Author: liuao
 * @Date: 2025-10-10-9:58
 * @Description:
 */
public interface PluginsBbsCategoryAppService
{
    /**
     * 获取分类列表
     * @param vo
     * @return
     */
    Map<String,Object> list(MainVo vo) throws LaiKeAPIException;
}
