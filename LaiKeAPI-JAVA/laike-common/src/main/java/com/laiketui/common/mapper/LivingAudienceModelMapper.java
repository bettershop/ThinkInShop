package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.living.LivingAudienceModel;

import java.util.List;
import java.util.Map;

/**
 * @author zhuqingyu
 * @create 2024/6/26
 */
public interface LivingAudienceModelMapper extends BaseMapper<LivingAudienceModel>
{
    /**
     * 根据直播间id查询用户信息
     *
     * @param roomId
     * @return
     * @throws LaiKeAPIException
     */
    List<Map<String, Object>> queryLivingUserByRoomId(Integer roomId) throws LaiKeAPIException;

    /**
     * 管理平台-查询直播间关注信息
     *
     * @param map
     * @return
     * @throws LaiKeAPIException
     */
    List<Map<String, Object>> adminQueryAudience(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 管理平台-查询直播间关注信息-计数
     *
     * @param map
     * @return
     * @throws LaiKeAPIException
     */
    List<Map<String, Object>> countQueryAudience(Map<String, Object> map) throws LaiKeAPIException;

}
