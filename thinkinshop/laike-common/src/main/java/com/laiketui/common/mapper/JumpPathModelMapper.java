package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.config.JumpPathModel;

import java.util.List;
import java.util.Map;

/**
 * 跳转路径
 *
 * @author Trick
 * @date 2021/6/30 14:10
 */
public interface JumpPathModelMapper extends BaseMapper<JumpPathModel>
{


    @Override
    List<Map<String, Object>> selectDynamic(Map<String, Object> map) throws LaiKeAPIException;

    @Override
    int countDynamic(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 获取店铺列表
     * @param parmaMap
     * @return
     */
    List<Map<String, Object>> getMchList(Map<String, Object> parmaMap);

}