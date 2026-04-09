package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.plugin.bbs.BbsCategoryModel;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * @Author: liuao
 * @Date: 2025-09-29-10:01
 * @Description:
 */
public interface BbsCategoryModelMapper extends BaseMapper<BbsCategoryModel>
{
    @Override
    int countDynamic(Map<String, Object> map) throws LaiKeAPIException;


    @Override
    List<Map<String,Object>> selectDynamic(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 判断分类是否存在
     * @param id
     * @param name
     * @param level
     * @return
     */
    int checkCount(@Param("id") Long id, @Param("name") String name,@Param("level") Integer level);

    @Select("select IFNULL(MAX(sort_order) + 1,0) from lkt_bbs_category where store_id=#{storeId}")
    int getClassMaxSort(int storeId);

    @Select("select id from lkt_bbs_category where sid = #{id} and recycle = 0")
    List<Long> getCidBySid(Long id);

    /**
     * 删除贴吧分类
     * @param ids
     */
    void del(@Param("ids") List<Long> ids);
}
