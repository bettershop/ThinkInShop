package com.laiketui.apps.api.app;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;

import java.util.Map;

/**
 * 充值
 *
 * @author Trick
 * @date 2020/11/4 16:22
 */
public interface AppsCstrRechargeService
{

    /**
     * 充值界面
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/4 16:21
     */
    Map<String, Object> index(MainVo vo) throws LaiKeAPIException;

}
