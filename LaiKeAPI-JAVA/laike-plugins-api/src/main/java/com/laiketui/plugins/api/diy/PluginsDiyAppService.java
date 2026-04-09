package com.laiketui.plugins.api.diy;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;

import java.util.Map;

/**
 * @Author: sunH_
 * @Date: Create in 12:23 2023/10/11
 */
public interface PluginsDiyAppService
{

    /**
     * 首页接口
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2022/5/9 11:24
     */
    Map<String, Object> index(MainVo vo) throws LaiKeAPIException;

    /**
     * 根据页面id获取页面数据
     * @param id
     * @return
     */
    Map<String,Object> getPageInfoById(Integer id);
}
