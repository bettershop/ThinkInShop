package com.laiketui.plugins.api.diy;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.diy.DiyProjectVo;

import java.util.Map;

public interface PluginsDiyProjectAdminService
{

    /**
     * 获取diy模板套装列表
     *
     * @param vo -
     * @param id -
     * @return Map
     * @throws LaiKeAPIException -
     * @author vvx
     * @date 2022/04/28 16:00
     */
    Map<String, Object> getDiyProjectList(MainVo vo, Integer id) throws LaiKeAPIException;

    /**
     * 修改/编辑diy模板套装
     *
     * @param vo -
     * @throws LaiKeAPIException-
     * @author vvx
     * @date 2022/04/28 16:00
     */
    void addOrUpdateDiyProject(DiyProjectVo vo) throws LaiKeAPIException;


    /**
     * 设置diy模板套装状态
     *
     * @param vo -
     * @param id -
     * @throws LaiKeAPIException-
     * @author vvx
     * @date 2022/04/28 16:00
     */
    void diyProjectStatus(MainVo vo, Integer id) throws LaiKeAPIException;


    /**
     * 删除diy模板套装
     *
     * @param vo -
     * @param id -
     * @throws LaiKeAPIException-
     * @author vvx
     * @date 2022/04/28 16:00
     */
    void delDiyProject(MainVo vo, int id) throws LaiKeAPIException;


    /**
     * 获取diy模板套装
     *
     * @param vo -
     * @param id -
     * @throws LaiKeAPIException-
     * @author vvx
     * @date 2022/04/28 16:00
     */
    Map<String, Object> getDiyProjectById(MainVo vo, int id) throws LaiKeAPIException;


}
