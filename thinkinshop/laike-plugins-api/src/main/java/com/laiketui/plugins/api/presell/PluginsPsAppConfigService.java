package com.laiketui.plugins.api.presell;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.plugin.presell.PreSellConfigVO;

import java.util.Map;

/**
 * 预售插件配置
 *
 * @author sunH_
 * @date 2021/12/20 15:00
 */
public interface PluginsPsAppConfigService
{

    /**
     * 获取预售配置信息
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author sunH_
     */
    Map<String, Object> getSecConfigInfo(MainVo vo) throws LaiKeAPIException;

    /**
     * 添加/创建预售配置信息
     *
     * @param vo
     * @throws LaiKeAPIException
     * @author sunH_
     */
    void update(PreSellConfigVO vo) throws LaiKeAPIException;

}
