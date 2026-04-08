package com.laiketui.common.api;

import com.laiketui.core.exception.LaiKeAPIException;

import java.util.List;
import java.util.Map;


/**
 * 满减通用类
 *
 * @author wangxian
 */
public interface PublicSubtractionService
{

    /**
     * 自动满减
     *
     * @param store_id
     * @param products
     * @param subtractionId
     * @return
     */
    Map<String, Object> autoSubtraction(int store_id, List<Map<String, Object>> products, int subtractionId) throws LaiKeAPIException;

    /**
     * 获取满减信息
     *
     * @param storeId
     * @param subtractionId
     * @param products
     * @return
     */
    Map<String, Object> subtractionList(int storeId, int subtractionId, List<Map<String, Object>> products) throws LaiKeAPIException;

    /**
     * 添加满减记录
     *
     * @param storeId
     * @param userId
     * @param sNo
     * @param giveId
     * @return
     */
    int subtractionRecord(int storeId, String userId, String sNo, int giveId);


}
