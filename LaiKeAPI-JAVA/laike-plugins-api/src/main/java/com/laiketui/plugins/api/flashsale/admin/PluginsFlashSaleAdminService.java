package com.laiketui.plugins.api.flashsale.admin;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.flashsale.AddConfigVo;

import java.util.Map;

public interface PluginsFlashSaleAdminService
{

    /**
     * 获取插件配置
     *
     * @param vo
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> getFlashSaleConfig(MainVo vo) throws LaiKeAPIException;

    /**
     * 保存修改插件配置
     *
     * @param vo
     * @throws LaiKeAPIException
     */
    void addFlashSaleConfig(AddConfigVo vo) throws LaiKeAPIException;
}
