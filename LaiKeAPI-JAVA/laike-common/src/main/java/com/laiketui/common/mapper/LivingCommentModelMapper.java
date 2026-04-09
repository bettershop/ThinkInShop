package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.domain.living.LivingCommentModel;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * @author zhuqingyu
 * @create 2024/6/21
 */
public interface LivingCommentModelMapper extends BaseMapper<LivingCommentModel>
{

    @Select("select a.comment,a.comment_type,b.user_name from lkt_living_comment a left join lkt_user b on a.user_id = b.user_id where a.store_id=#{storeId} and  a.living_id = #{roomId} and a.recycle=0")
    List<Map<String, Object>> selectCommentMap(Integer storeId, Integer roomId);
}
