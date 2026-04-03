package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.config.ExpressSubtableModel;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

public interface ExpressSubtableModelMapper extends BaseMapper<ExpressSubtableModel>
{
    @Override
    List<Map<String, Object>> selectDynamic(Map<String, Object> map) throws LaiKeAPIException;

    @Override
    int countDynamic(Map<String, Object> map) throws LaiKeAPIException;


    List<Map<String, Object>> selectExpressByexpressSubtable(Map<String, Object> map) throws LaiKeAPIException;


    int countExpressByexpressSubtable(Map<String, Object> map) throws LaiKeAPIException;

    @Select("select count(*) from  lkt_express_subtable where store_id = #{storeId} and express_id = #{expressId} and mch_id = #{mchId} and recovery = 0")
    int countExpressSubtableByExpressId(Integer storeId, Integer expressId, Integer mchId) throws LaiKeAPIException;
}
