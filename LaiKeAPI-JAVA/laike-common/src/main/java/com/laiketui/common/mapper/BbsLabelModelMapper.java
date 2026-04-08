package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.plugin.bbs.BbsLabelModel;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * @Author: liuao
 * @Date: 2025-09-29-10:02
 * @Description:
 */
public interface BbsLabelModelMapper extends BaseMapper<BbsLabelModel>
{
    @Override
    int countDynamic(Map<String, Object> map) throws LaiKeAPIException;


    @Override
    List<Map<String, Object>> selectDynamic(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 判断名称是否存在
     * @param name
     * @param id
     * @return
     */
    int checkCount(@Param("name") String name, @Param("id") Long id);

    /**
     * 批量删除话题
     * @param idList
     */
    void del(@Param("idList") List<String> idList);

    @Select("select * from lkt_bbs_label where name = #{name} and store_id = #{store_id} and recycle = 0")
    BbsLabelModel getByName(String name,Integer store_id);

    /**
     * 根据文章id获取话题列表
     * @param postId
     * @return
     */
    List<Map<String, Object>> getListByPostId(@Param("post_id") Long postId);

}
