package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.plugin.bbs.BbsConfigModel;

import java.util.List;
import java.util.Map;

/**
 * @Author: liuao
 * @Date: 2025-09-29-9:56
 * @Description:
 */
public interface BbsConfigModelMapper extends BaseMapper<BbsConfigModel>
{
    @Override
    List<Map<String, Object>> selectDynamic(Map<String, Object> map) throws LaiKeAPIException;
}
