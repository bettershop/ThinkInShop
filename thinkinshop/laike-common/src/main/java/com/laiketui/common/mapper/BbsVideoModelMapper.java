package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.plugin.bbs.BbsVideoModel;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

/**
 * @Author: liuao
 * @Date: 2025-10-10-11:14
 * @Description:
 */
public interface BbsVideoModelMapper extends BaseMapper<BbsVideoModel>
{
    @Update("update lkt_bbs_video set response_msg = #{result} where file_id = #{filedId}")
    void setMsg(String filedId, String result);

    @Override
    List<Map<String, Object>> selectDynamic(Map<String, Object> map) throws LaiKeAPIException;
}
