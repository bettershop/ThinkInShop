package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.home.UiNavigationBarModel;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

/**
 * ui导航栏
 *
 * @author Trick
 * @date 2021/2/23 10:56
 */
public interface UiNavigationBarModelMapper extends BaseMapper<UiNavigationBarModel>
{


    /**
     * 获取ui导航栏
     *
     * @param storeId -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/2/23 10:58
     */
    @Select("SELECT name,url,image,isshow,is_login  FROM `lkt_ui_navigation_bar` where store_id = #{storeId} order by sort desc")
    List<Map<String, Object>> getNavigationInfo(int storeId) throws LaiKeAPIException;

    @Select("select IFNULL(max(sort),0)+1 from lkt_ui_navigation_bar where  store_id = #{storeId}")
    int getMaxSort(int storeId);


    @Update("update lkt_ui_navigation_bar a,lkt_ui_navigation_bar b set a.sort=b.sort,b.sort = a.sort where a.id=#{id} and b.id=#{id1} ")
    int move(int id, int id1);

    void insertBatch(@Param("saveBatchList") List<UiNavigationBarModel> saveBatchList, @Param("storeId") Integer storeId);

}