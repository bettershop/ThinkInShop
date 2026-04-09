package com.laiketui.common.api.plugin.substration;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.user.User;
import com.laiketui.domain.vo.MainVo;

import java.util.Map;

/**
 * 公共满减
 *
 * @author Trick
 * @date 2021/4/16 9:53
 */
public interface PubliceSubstrationService
{


    /**
     * 获取满减商品列表
     *
     * @param vo   -
     * @param user -
     * @param id   -满减id
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/4/16 10:33
     */
    Map<String, Object> getSubstrationGoodsList(MainVo vo, User user, int id) throws LaiKeAPIException;

}
