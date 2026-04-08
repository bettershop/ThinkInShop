package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.mch.MchStoreAccountModel;

import java.util.List;
import java.util.Map;

/**
 * @author sunH_
 */
public interface MchStoreAccountModelMapper extends BaseMapper<MchStoreAccountModel>
{

    int countList(Map<String, Object> map) throws LaiKeAPIException;

    List<Map<String, Object>> selectList(Map<String, Object> map) throws LaiKeAPIException;
}