package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.domain.user.ThirdMappingModel;
import org.apache.ibatis.annotations.Select;

/**
 * 第三方用户映射
 *
 * @author Trick
 * @date 2023/1/13 18:21
 */
public interface ThirdMappingModelMapper extends BaseMapper<ThirdMappingModel>
{


    @Select("select a.user_id from lkt_third_mapping a inner join lkt_user b on a.user_id=b.user_id " +
            "where a.store_id=#{storeId} and a.union_id=#{unionId} and a.type=#{type} limit 1")
    String getUserIdByUnionId(int storeId, String unionId, int type);

}