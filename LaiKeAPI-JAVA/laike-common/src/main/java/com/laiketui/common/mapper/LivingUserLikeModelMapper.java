package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.domain.living.LivingUserLikeModel;

import java.util.List;
import java.util.Map;

/**
 * @author zhuqingyu
 * @create 2024/6/25
 */
public interface LivingUserLikeModelMapper extends BaseMapper<LivingUserLikeModel>
{

    List<Map<String, Object>> queryMyLike(String user_id);
}
