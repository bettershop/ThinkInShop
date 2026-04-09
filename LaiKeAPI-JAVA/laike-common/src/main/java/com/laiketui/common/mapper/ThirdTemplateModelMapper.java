package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.weixin.ThirdTemplateModel;

import java.util.List;
import java.util.Map;


/**
 * 小程序模板
 *
 * @author Trick
 * @date 2021/1/20 10:43
 */
public interface ThirdTemplateModelMapper extends BaseMapper<ThirdTemplateModel>
{


    /**
     * 动态sql
     *
     * @param map -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/7 11:23
     */
    @Override
    List<Map<String, Object>> selectDynamic(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 动态sql
     *
     * @param map -
     * @return int
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/7 11:23
     */
    @Override
    int countDynamic(Map<String, Object> map) throws LaiKeAPIException;

}