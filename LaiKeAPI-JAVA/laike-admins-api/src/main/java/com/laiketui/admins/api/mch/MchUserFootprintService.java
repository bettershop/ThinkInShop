package com.laiketui.admins.api.mch;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;

import java.util.Map;

/**
 * 客户管理
 *
 * @author Trick
 * @date 2021/5/26 17:19
 */
public interface MchUserFootprintService
{


    /**
     * 客户管理列表
     *
     * @param vo        -
     * @param phone     -
     * @param startDate -
     * @param endDate   -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/5/26 17:22
     */
    Map<String, Object> index(MainVo vo, String phone, String startDate, String endDate) throws LaiKeAPIException;


    /**
     * 获取用户今日足迹明细
     *
     * @param vo     -
     * @param userId -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/5/27 10:53
     */
    Map<String, Object> userPug(MainVo vo, String userId) throws LaiKeAPIException;


    /**
     * 删除足迹
     *
     * @param vo     -
     * @param userId -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/5/27 11:07
     */
    Map<String, Object> del(MainVo vo, String userId) throws LaiKeAPIException;

}
