package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.domain.plugin.bbs.BbsCommentLikeModel;
import org.apache.ibatis.annotations.Update;

import java.util.Map;

/**
 * @Author: liuao
 * @Date: 2025-10-13-16:09
 * @Description:
 */
public interface BbsCommentLikeModelMapper extends BaseMapper<BbsCommentLikeModel>
{
    @Update("update lkt_bbs_post_comment set like_num = like_num + #{like_num} where id =#{id}")
    void updateLikeNum(Long id, int like_num);

    @Override
    int delDynamic(Map<String, Object> map);
}
