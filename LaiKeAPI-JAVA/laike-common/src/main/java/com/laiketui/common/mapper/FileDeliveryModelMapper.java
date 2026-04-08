package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.file.FileDeliveryModel;

import java.util.List;
import java.util.Map;

/**
 * 批量操作记录
 *
 * @author Trick
 * @date 2021/12/8 20:51
 */
public interface FileDeliveryModelMapper extends BaseMapper<FileDeliveryModel>
{

    @Override
    List<Map<String, Object>> selectDynamic(Map<String, Object> map) throws LaiKeAPIException;

    @Override
    int countDynamic(Map<String, Object> map) throws LaiKeAPIException;
}