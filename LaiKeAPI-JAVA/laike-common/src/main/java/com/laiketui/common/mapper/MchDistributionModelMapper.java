package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.divideAccount.MchDistributionModel;
import org.apache.ibatis.annotations.Delete;

import java.util.List;
import java.util.Map;

public interface MchDistributionModelMapper extends BaseMapper<MchDistributionModel>
{

    @Override
    List<Map<String, Object>> selectDynamic(Map<String, Object> map) throws LaiKeAPIException;

    @Override
    int countDynamic(Map<String, Object> map) throws LaiKeAPIException;

    @Delete("delete from lkt_mch_distribution where mch_id = #{mchId}")
    void delByMchId(Integer mchId) throws LaiKeAPIException;

}