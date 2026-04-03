package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.domain.living.LivingSensitiveModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zhuqingyu
 * @create 2024/5/28
 * <p>
 * 直播配置表
 */
public interface LivingSensitiveModelMapper extends BaseMapper<LivingSensitiveModel>
{
    List<Map<String, Object>> selectSensitive(HashMap<String, Object> params);

    int selectTotal(HashMap<String, Object> params);
}
