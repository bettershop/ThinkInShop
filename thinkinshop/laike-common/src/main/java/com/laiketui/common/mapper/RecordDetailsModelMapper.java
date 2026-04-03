package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.log.RecordDetailsModel;

import java.util.List;
import java.util.Map;

public interface RecordDetailsModelMapper extends BaseMapper<RecordDetailsModel>
{

    /**
     * 获取用户操作记录
     *
     * @param map -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/11 16:03
     */
    List<Map<String, Object>> getUserWalletRecordInfo(Map<String, Object> map) throws LaiKeAPIException;
}
