package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.plugin.bbs.BbsForumModel;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

/**
 * @Author: liuao
 * @Date: 2025-09-29-10:01
 * @Description:
 */
public interface BbsForumModelMapper extends BaseMapper<BbsForumModel>
{

    @Override
    int countDynamic(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 贴吧列表
     * @param map 查询参数
     * @return
     */
    List<Map<String, Object>> list(Map<String, Object> map);

    /**
     *
     * @param id
     * @return
     */
    int getCountByCategoryId(@Param("id") Long id);

    @Update("update lkt_bbs_forum set recycle = 1 where id = #{id}")
    void del(Long id);

    void updateNum(@Param("user_id") String userId,@Param("num") int num,@Param("type") Integer type,@Param("id") Long id);

    @Select("SELECT description, name,like_num,post_num,id,collect_num,head_img FROM lkt_bbs_forum where user_id = #{user_id} and recycle = 0")
    BbsForumModel details(String user_id);
}
