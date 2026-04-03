package com.laiketui.common.api.plugin.sgin;

import com.laiketui.core.exception.LaiKeAPIException;

import java.util.List;
import java.util.Map;

/**
 * 签到公共
 *
 * @author Trick
 * @date 2021/4/7 14:17
 */
public interface PublicSginService
{

    /**
     * 获取用户签到数据
     * 【php sgin.index】
     *
     * @param storeId -
     * @param userId  -
     * @param year    -
     * @param month   -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/4/7 14:22
     */
    Map<String, Object> index(int storeId, String userId, int year, int month) throws LaiKeAPIException;

    /**
     * 删除过期积分
     * 【php sgin.index】
     *
     * @param storeId -
     * @param ids     -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/4/7 14:22
     */
    boolean deleteSign(int storeId, List<String> ids) throws LaiKeAPIException;

}
