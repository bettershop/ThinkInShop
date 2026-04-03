package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.plugin.activitie.PlatformActivitiesModel;

import java.util.List;
import java.util.Map;

/**
 * @author Trick
 */
public interface PlatformActivitiesModelMapper extends BaseMapper<PlatformActivitiesModel>
{

    @Override
    List<Map<String, Object>> selectDynamic(Map<String, Object> map) throws LaiKeAPIException;

    @Override
    int countDynamic(Map<String, Object> map) throws LaiKeAPIException;

}