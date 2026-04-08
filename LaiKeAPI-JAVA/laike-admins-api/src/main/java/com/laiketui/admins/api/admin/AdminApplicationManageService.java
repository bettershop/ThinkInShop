package com.laiketui.admins.api.admin;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.app.AppConfigInfoVo;

import java.util.Map;

/**
 * app管理
 *
 * @author Trick
 * @date 2021/1/22 9:40
 */
public interface AdminApplicationManageService
{


    /**
     * 获取app版本配置信息
     *
     * @param storeId -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/22 9:41
     */
    Map<String, Object> getVersionConfigInfo(int storeId) throws LaiKeAPIException;


    /**
     * 添加/编辑版本配置信息
     *
     * @param vo -
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/22 9:49
     */
    boolean addVersionConfigInfo(AppConfigInfoVo vo) throws LaiKeAPIException;

}
