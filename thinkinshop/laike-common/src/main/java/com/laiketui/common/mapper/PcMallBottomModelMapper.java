package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.config.PcMallBottomModel;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

public interface PcMallBottomModelMapper extends BaseMapper<PcMallBottomModel>
{

    /**
     * 获取轮播图信息
     *
     * @param map -
     * @return List
     * @throws LaiKeAPIException -
     * @author gp
     * @date 2023-08-24
     */
    List<Map<String, Object>> selectList(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 获取最新序号
     *
     * @param storeId -
     * @return int
     * @throws LaiKeAPIException -
     * @author gp
     * @date 2023-08-24
     */
    @Select("select if(MAX(sort) IS NULL, 0, MAX(sort))+1 as sort from lkt_banner where store_id = #{storeId}")
    int getPcMaxSort(int storeId) throws LaiKeAPIException;

    /**
     * 获取轮播图信息 - 统计
     *
     * @param map -
     * @return List
     * @throws LaiKeAPIException -
     */
    @Override
    int countDynamic(Map<String, Object> map) throws LaiKeAPIException;
}
