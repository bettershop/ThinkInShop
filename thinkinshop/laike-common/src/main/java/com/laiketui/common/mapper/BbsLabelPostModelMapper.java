package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.domain.plugin.bbs.BbsLabelPostModel;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Author: liuao
 * @Date: 2025-09-29-10:03
 * @Description:
 */
public interface BbsLabelPostModelMapper extends BaseMapper <BbsLabelPostModel>
{
    void delByLabelIds(@Param("ids") List<String> ids);


    List<Long> getPostIdByStoreId(@Param("store_id") int storeId,@Param("label_id") Long label_id,@Param("label_name") String label_name);

    void add(@Param("labelIdList") List<Long> labelIdList, @Param("post_id") Long id,@Param("store_id") Integer storeId);

    @Delete("delete from lkt_bbs_label_post where post_id = #{postId}")
    void delByPostId(Long postId);
}
