package com.laiketui.admins.api.admin.systems;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.systems.AddMessageListVo;
import com.laiketui.domain.vo.systems.AddMessageVo;

import java.util.Map;

/**
 * 短信管理
 *
 * @author Trick
 * @date 2021/1/15 15:17
 */
public interface AdminSmsService
{


    /**
     * 获取短信列表
     *
     * @param vo -
     * @param id -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/15 15:18
     */
    Map<String, Object> getSmsInfo(MainVo vo, Integer id) throws LaiKeAPIException;


    /**
     * 添加/修改 短信列表
     *
     * @param vo -
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/18 10:56
     */
    boolean addMessageList(AddMessageListVo vo) throws LaiKeAPIException;


    /**
     * 删除自定义模板配置
     *
     * @param vo -
     * @param id -
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/18 14:27
     */
    boolean delMessageList(MainVo vo, int id) throws LaiKeAPIException;


    /**
     * 获取短信模板类型信息
     *
     * @param vo -
     * @param id -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/18 14:43
     */
    Map<String, Object> getSmsTemplateInfo(MainVo vo, Integer id,Integer international) throws LaiKeAPIException;


    /**
     * 添加/编辑短信模板
     *
     * @param vo    -
     * @param phone - 接收手机号
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/18 15:07
     */
    void addMessage(AddMessageVo vo, String phone,String cpc) throws LaiKeAPIException;


    /**
     * 删除短信模板
     *
     * @param vo -
     * @param id -
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/18 17:22
     */
    void delMessage(MainVo vo, int id) throws LaiKeAPIException;


    /**
     * 获取商城短信配置项
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/18 17:28
     */
    Map<String, Object> getTemplateConfigInfo(MainVo vo) throws LaiKeAPIException;


    /**
     * 添加/编辑模板配置项
     *
     * @param vo       -
     * @param key      -
     * @param secret   -
     * @param signName -
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/18 17:29
     */
    boolean addTemplateConfig(MainVo vo, String key, String secret, String signName) throws LaiKeAPIException;
}
