package com.laiketui.plugins.api.bbs.app;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;

import java.util.Map;

/**
 * @Author: liuao
 * @Date: 2025-10-10-9:50
 * @Description: 
 */public interface PluginsBbsLabelAppService
{
    /**
     * 获取话题
     * @param vo
     * @return
     */
    Map<String,Object> index(MainVo vo,String name)  throws LaiKeAPIException;
}
