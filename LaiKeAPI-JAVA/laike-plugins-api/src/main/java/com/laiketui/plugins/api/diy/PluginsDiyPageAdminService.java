package com.laiketui.plugins.api.diy;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.diy.DiyPageModel;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.diy.DiyPageVo;

import java.util.Map;

public interface PluginsDiyPageAdminService
{

    /**
     * 获取diy模页面列表
     *
     * @param vo        -
     * @return Map
     * @throws LaiKeAPIException -
     * @author vvx
     * @date 2022/04/28 16:00
     */
    Map<String, Object> getDiyPageList(MainVo vo,String name,Integer status) throws LaiKeAPIException;

    /**
     * 修改/编辑diy模板套装
     *
     * @param vo -
     * @throws LaiKeAPIException-
     * @author vvx
     * @date 2022/04/28 16:00
     */
    Map<String,Object> addOrUpdateDiyPage(DiyPageVo vo) throws LaiKeAPIException;



    /**
     * 删除绑定关系
     *
     * @param vo -
     * @param diyId -页面id
     * @param pageId -主题id
     * @throws LaiKeAPIException-
     * @author vvx
     * @date 2022/04/28 16:00
     */
    void delBindDiyPage(MainVo vo, Integer diyId,Integer pageId) throws LaiKeAPIException;

    /**
     * 获取diy单页信息
     *
     * @param vo -
     * @param id -
     * @throws LaiKeAPIException-
     * @author vvx
     * @date 2022/04/28 16:00
     */
    Map<String, Object> getDiyPageById(MainVo vo, int id) throws LaiKeAPIException;

    /**
     * 查看关联主题
     * @param vo
     * @param id
     * @return
     */
    Map<String,Object> getDiyPageBindList(MainVo vo, Integer id, String name);

    /**
     * 删除页面
     * @param vo
     * @param id
     */
    void delDiyPage(MainVo vo, Integer id);


    /**
     * 获取页面json
     * @param diyId
     * @param link
     * @return
     */
    DiyPageModel getPageJson(MainVo vo, Integer diyId, String link) throws LaiKeAPIException;
}
