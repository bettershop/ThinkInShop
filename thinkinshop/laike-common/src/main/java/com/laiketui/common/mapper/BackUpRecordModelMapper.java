package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.domain.backUp.BackUpRecordModel;

import java.util.List;
import java.util.Map;

/**
 * @author zhuqingyu
 * @create 2024/8/19
 */
public interface BackUpRecordModelMapper extends BaseMapper<BackUpRecordModel>
{
    List<Map<String, Object>> recordList(Map<String, Object> map);

    int countRecord(Map<String, Object> map);
}
