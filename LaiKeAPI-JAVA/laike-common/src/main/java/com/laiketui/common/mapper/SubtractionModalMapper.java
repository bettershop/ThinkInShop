package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.subtraction.SubtractionModal;

import java.util.List;
import java.util.Map;

/**
 * 满减
 *
 * @author Trick
 */
public interface SubtractionModalMapper extends BaseMapper<SubtractionModal>
{

    @Override
    List<Map<String, Object>> selectDynamic(Map<String, Object> map) throws LaiKeAPIException;

    @Override
    int countDynamic(Map<String, Object> map) throws LaiKeAPIException;
}