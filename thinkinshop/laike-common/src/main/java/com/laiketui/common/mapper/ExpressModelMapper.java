package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.config.ExpressModel;

import java.util.List;
import java.util.Map;

/**
 * 快递公司物流
 *
 * @author Trick
 * @date 2021/7/6 17:09
 */
public interface ExpressModelMapper extends BaseMapper<ExpressModel>
{

    @Override
    List<Map<String, Object>> selectDynamic(Map<String, Object> map) throws LaiKeAPIException;

    @Override
    int countDynamic(Map<String, Object> map) throws LaiKeAPIException;
}