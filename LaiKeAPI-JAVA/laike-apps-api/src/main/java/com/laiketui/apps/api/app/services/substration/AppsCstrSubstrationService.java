package com.laiketui.apps.api.app.services.substration;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;

import java.util.Map;

/**
 * 满减
 *
 * @author Trick
 * @date 2021/4/16 14:27
 */
public interface AppsCstrSubstrationService
{


    /**
     * 获取满减商品列表
     *
     * @param vo -
     * @param id -满减id
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/4/16 10:33
     */
    Map<String, Object> getSubstrationGoodsList(MainVo vo, int id) throws LaiKeAPIException;

}
