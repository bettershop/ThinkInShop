package com.laiketui.plugins.api.diy;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.diy.DiyVo;

import java.util.Map;

/**
 * @Author: sunH_
 * @Date: Create in 12:24 2023/10/11
 */
public interface PluginsDiyAdminService
{


    /**
     * 获取diy模板首页列表
     *
     * @param vo -
     * @param id -
     * @return Map
     * @throws LaiKeAPIException -
     * @author sunH
     * @date 2022/04/28 16:00
     */
    Map<String, Object> getDiyList(MainVo vo, Integer id,Integer theme_type,String theme_type_code) throws LaiKeAPIException;

    /**
     * 修改/编辑diy模板
     *
     * @param vo -
     * @throws LaiKeAPIException-
     * @author sunH
     * @date 2022/04/28 16:00
     */
    Map<String,Object> addOrUpdateDiy(DiyVo vo) throws LaiKeAPIException;


    /**
     * 设置diy模板
     *
     * @param vo -
     * @param id -
     * @throws LaiKeAPIException-
     * @author sunH
     * @date 2022/04/28 16:00
     */
    void diyStatus(MainVo vo, Integer id) throws LaiKeAPIException;


    /**
     * 删除diy模板
     *
     * @param vo -
     * @param id -
     * @throws LaiKeAPIException-
     * @author sunH
     * @date 2022/04/28 16:00
     */
    void delDiy(MainVo vo, int id) throws LaiKeAPIException;

    /**
     * 获取当前主题绑定的页面列表
     * @param vo
     * @return
     */
    Map<String, Object> getBindPageList(MainVo vo, Integer id) throws LaiKeAPIException;


    /**
     * 模板管理，获取插件状态
     * @param vo
     * @return
     * @throws LaiKeAPIException
     */
    Map<String,Object> getPluginStatus(MainVo vo) throws LaiKeAPIException;
}
