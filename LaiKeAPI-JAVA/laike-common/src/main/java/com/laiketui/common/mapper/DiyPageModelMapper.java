package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.diy.DiyPageModel;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

public interface DiyPageModelMapper extends BaseMapper<DiyPageModel>
{

    /**
     * 获取diy模页面列表
     *
     * @param map
     * @return Map
     * @throws LaiKeAPIException -
     * @author vvx
     * @date 2022/04/28 16:00
     */
    List<Map<String, Object>> getDiyPageList(Map<String,Object> map) throws LaiKeAPIException;

    /**
     * 动态sql-统计
     *
     * @param map -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/20 10:56
     */
    @Override
    int countDynamic(Map<String, Object> map) throws LaiKeAPIException;

    int checkCountName(@Param("pageName") String pageName, @Param("id") Integer id);

    DiyPageModel getOneByLink(@Param("link") String link, @Param("store_id") Integer storeId);


    @Update("update lkt_diy_page set recycle = 1,status = 0 where id =#{id}")
    void remove(Integer id);
}