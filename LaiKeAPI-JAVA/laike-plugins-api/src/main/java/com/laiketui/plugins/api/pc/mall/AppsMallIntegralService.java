package com.laiketui.plugins.api.pc.mall;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;

import java.util.Map;

/**
 * 积分接口
 *
 * @author Trick
 * @date 2022/2/24 14:30
 */
public interface AppsMallIntegralService
{
    /**
     * 我的积分列表
     *
     * @param vo   -
     * @param type - 1 获取积分明细 2 使用明细 else 全部明细
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2022/2/24 14:31
     */
    Map<String, Object> getSignInfo(MainVo vo, Integer type) throws LaiKeAPIException;

    boolean deleteSign(MainVo vo, String ids);
}
