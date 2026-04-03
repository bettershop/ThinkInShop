package com.laiketui.apps.api.app.services;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;

import java.util.Map;

/**
 * 我的足迹/记录
 *
 * @author Trick
 * @date 2020/10/30 15:48
 */
public interface AppsCstrFootprintService
{


    /**
     * 获取我的历史记录
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/10/30 15:53
     */
    Map<String, Object> index(MainVo vo) throws LaiKeAPIException;


    /**
     * 删除我的历史记录
     *
     * @param vo          -
     * @param footprintId -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/10/30 15:53
     */
    boolean alldel(MainVo vo, int footprintId) throws LaiKeAPIException;
}
