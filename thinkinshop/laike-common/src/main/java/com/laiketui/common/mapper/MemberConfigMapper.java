package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.domain.plugin.member.MemberConfig;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

public interface MemberConfigMapper extends BaseMapper<MemberConfig>
{

    @Select("SELECT COUNT(1) FROM lkt_member_config WHERE store_id = #{storeId}")
    Integer countStoreId(int storeId);

    @Select("SELECT * FROM lkt_member_config WHERE store_id = #{storeId}")
    Map<String, Object> getConfig(int storeId);
}