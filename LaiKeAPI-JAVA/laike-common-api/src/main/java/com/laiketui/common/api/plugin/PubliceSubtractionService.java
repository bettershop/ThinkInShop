package com.laiketui.common.api.plugin;

import com.laiketui.core.exception.LaiKeAPIException;

import java.util.List;
import java.util.Map;

/**
 * 公共满减
 *
 * @author Trick
 * @date 2021/2/23 14:29
 */
public interface PubliceSubtractionService
{


    /**
     * 获取满减移动端图片
     * 【php subtraction.test】
     *
     * @param storeId -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/2/23 14:32
     */
    String getAppImage(int storeId) throws LaiKeAPIException;

    /**
     * 获取满减活动图片
     * 【php subtraction.get_subtraction_image】
     *
     * @param storeId -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/2/23 14:32
     */
    List<Map<String, Object>> getSubtractionImage(int storeId) throws LaiKeAPIException;
}
