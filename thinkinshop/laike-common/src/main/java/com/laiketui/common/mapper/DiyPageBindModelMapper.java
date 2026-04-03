package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.plugin.DiyPageBindModel;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * @Author: liuao
 * @Date: 2025-06-16-14:53
 * @Description:
 */
public interface DiyPageBindModelMapper extends BaseMapper<DiyPageBindModel> {

    /**
     * 获取当前主题绑定的页面列表
     * @param id
     * @return
     */
    List<Map<String, Object>> getBindPageListByDiyId(@Param("id") Integer id) throws LaiKeAPIException;

    /**
     * 判断页面是否绑定了主题
     * @param id
     * @return
     */
    int countByPageId(Integer id,String name);

    /**
     * 获取页面绑定的主题列表
     * @param map
     * @return
     */
    List<Map<String,Object>> getBindDiyList(Map<String,Object> map);

    @Select("select count(1) from lkt_diy_page_bind where diy_page_id = #{pageId}")
    int countByDiyId(Integer pageId);

    /**
     * 判断是否已绑定
     * @param pageId
     * @param diyId
     */
    @Select("select count(1) from lkt_diy_page_bind where diy_page_id = #{pageId} and diy_id = #{diyId} and link_key = #{link_key} and unit = #{unit}  ")
    int checkCount(Integer diyId, Integer pageId,String link_key,String unit);

    @Select("select diy_page_id from lkt_diy_page_bind where diy_id = #{id} group by diy_page_id ")
    List<Integer> getPageIdByDiyId(int id);

    @Delete("delete from lkt_diy_page_bind where diy_id = #{id}")
    void deleteByDiyId(int id);

    @Select("select diy_id from lkt_diy_page_bind where diy_page_id = #{id} group by diy_id")
    List<Integer> getDiyIdByPageId(Integer id);

    @Select("select * from lkt_diy_page_bind where diy_id = #{diyId} and diy_page_id = #{pageId}")
    List<DiyPageBindModel> getBindUnitByDiyIdAndPageId(Integer diyId, Integer pageId);

    @Select("select id from lkt_diy_page_bind where diy_id = #{diyId} and diy_page_id = #{id} and link_key = #{linkKey} and unit = #{unit}")
    Integer getBindId(Integer diyId, Integer id, String linkKey, String unit);

    @Select("select count(1) from lkt_diy_page_bind where diy_id = #{diyId} and diy_page_id = #{diy_page_id}")
    int getBindData(Integer diyId, Integer diy_page_id);

    @Select("select id from lkt_diy_page_bind where  diy_id = #{diyId} and link_key = #{link}")
    List<Integer> getBindByLinkAndDiyId(Integer diyId, String link);

}
