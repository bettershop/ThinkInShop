package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.weixin.ThirdMiniInfoModel;

import java.util.List;
import java.util.Map;

/**
 * 授权小程序信息表
 *
 * @author Trick
 * @date 2021/1/21 18:30
 */
public interface ThirdMiniInfoModelMapper extends BaseMapper<ThirdMiniInfoModel>
{

    /**
     * 动态sql
     *
     * @param map -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021-02-03 17:28:33
     */
    @Override
    List<Map<String, Object>> selectDynamic(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 动态sql-统计
     *
     * @param map -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021-02-03 17:28:38
     */
    @Override
    int countDynamic(Map<String, Object> map) throws LaiKeAPIException;
}