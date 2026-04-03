package com.laiketui.admins.api.admin.systems;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;

import java.util.Map;

/**
 * 搜索配置
 *
 * @author Trick
 * @date 2021/1/19 15:16
 */
public interface AdminSearchService
{


    /**
     * 获取搜索配置信息
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/19 15:21
     */
    Map<String, Object> getSearchConfigIndex(MainVo vo) throws LaiKeAPIException;


    /**
     * 添加/编辑搜索配置
     *
     * @param vo       -
     * @param isOpen   -
     * @param limitNum -
     * @param keyword  -
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/19 15:21
     */
    boolean addSearchConfig(MainVo vo, int isOpen, Integer limitNum, String keyword) throws LaiKeAPIException;
}
