package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.plugin.bbs.BbsTemplateConfigModel;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @Author: liuao
 * @Date: 2025-10-27-10:53
 * @Description:
 */
public interface BbsTemplateConfigModelMapper extends BaseMapper<BbsTemplateConfigModel>
{
    @Override
    List<Map<String, Object>> selectDynamic(Map<String, Object> map) throws LaiKeAPIException;

    @Override
    int countDynamic(Map<String, Object> map) throws LaiKeAPIException;

    int checkCount(@Param("name") String name, @Param("id") Long id);

    void del(@Param("idList") List<String> idList);
}
