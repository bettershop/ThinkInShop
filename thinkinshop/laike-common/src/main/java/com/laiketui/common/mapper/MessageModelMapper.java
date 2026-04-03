package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.dictionary.MessageModel;

import java.util.List;
import java.util.Map;


/**
 * 短信模板
 *
 * @author Trick
 * @date 2021/1/18 14:49
 */
public interface MessageModelMapper extends BaseMapper<MessageModel>
{


    /**
     * 获取模板信息
     *
     * @param map -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/18 14:50
     */
    @Override
    List<Map<String, Object>> selectDynamic(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 获取模板信息-统计
     *
     * @param map -
     * @return int
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/18 14:50
     */
    @Override
    int countDynamic(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 判断类型是否存在
     * @param category
     * @param storeId
     * @param id
     * @return
     */
    int count(Integer category, Integer storeId, Integer id,int international) throws LaiKeAPIException;
}