package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.mch.MchStoreWriteModel;

import java.util.List;
import java.util.Map;

public interface MchStoreWriteModelMapper extends BaseMapper<MchStoreWriteModel>
{

    @Override
    List<Map<String, Object>> selectDynamic(Map<String, Object> map) throws LaiKeAPIException;

    @Override
    int countDynamic(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 查询核销门店
     *
     * @param parmaMap
     * @return
     */
    List<Map<String, Object>> selectMchStore(Map<String, Object> parmaMap);

}
