package com.laiketui.apps.api.mch;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;

import java.util.Map;

/**
 * 店铺保证金
 *
 * @author Trick
 * @date 2021/10/25 14:16
 */
public interface AppsMchPromiseAdminService
{
    /**
     * 保证金记录列表
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/10/25 14:41
     */
    Map<String, Object> index(MainVo vo, String keyName) throws LaiKeAPIException;

    /**
     * 保证金记录列表
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/10/25 14:41
     */
    Map<String, Object> PromisePriceIndex(MainVo vo, String mchName, String status, String startTime, String endTime) throws LaiKeAPIException;
}
