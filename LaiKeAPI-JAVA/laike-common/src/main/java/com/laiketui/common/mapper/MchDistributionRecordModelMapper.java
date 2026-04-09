package com.laiketui.common.mapper;


import com.laiketui.core.db.BaseMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.divideAccount.MchDistributionRecordModel;

import java.util.List;
import java.util.Map;

public interface MchDistributionRecordModelMapper extends BaseMapper<MchDistributionRecordModel>
{

    @Override
    List<Map<String, Object>> selectDynamic(Map<String, Object> map) throws LaiKeAPIException;

    @Override
    int countDynamic(Map<String, Object> map) throws LaiKeAPIException;

}