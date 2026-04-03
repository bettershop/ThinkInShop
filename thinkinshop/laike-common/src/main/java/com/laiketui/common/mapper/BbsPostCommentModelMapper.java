package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.plugin.bbs.BbsPostCommentModel;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @Author: liuao
 * @Date: 2025-10-09-14:42
 * @Description:
 */
public interface BbsPostCommentModelMapper extends BaseMapper<BbsPostCommentModel>
{

    @Override
    int countDynamic(Map<String, Object> map) throws LaiKeAPIException;


    /**
     * 获取评论数据
     * @param map
     * @return
     */
    List<BbsPostCommentModel> findTopLevelComments(Map<String, Object> map);

    @Override
    List<Map<String, Object>> selectDynamic(Map<String, Object> map) throws LaiKeAPIException;

    List<BbsPostCommentModel> findByParentId(@Param("parentId") Long parentId ,@Param("pageNo") int pageNo,@Param("pageSize") int pageSize,@Param("like_user_id") String like_user_id);


    @Override
    int delDynamic(Map<String, Object> map);


    Map<String,Object> getParentUserName(@Param("id") Long parentId);
}
