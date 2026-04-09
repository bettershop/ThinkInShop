package com.laiketui.plugins.api.auction.admin;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.auction.AddSessionVo;

import java.util.Map;

/**
 * 后台-竞拍场次接口
 *
 * @author Trick
 * @date 2022/7/1 15:21
 */
public interface PluginAuctionAdminSessionService
{

    /**
     * 获取竞拍场次列表
     *
     * @param vo        -
     * @param key       - id/场次名称
     * @param status    - 1=未开始 2=进行中 3=已结束
     * @param specialId -
     * @param startDate -
     * @param endDate   -
     * @param id        -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022/7/1 15:23
     */
    Map<String, Object> getSessionList(MainVo vo, String key, Integer status, String startDate, String endDate, String id, String specialId) throws LaiKeAPIException;

    /**
     * 添加竞拍场次
     *
     * @param vo -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022/7/1 15:36
     */
    void addSession(AddSessionVo vo) throws LaiKeAPIException;


    /**
     * 删除竞拍场次
     *
     * @param vo -
     * @param id -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022/7/1 15:36
     */
    void delSession(MainVo vo, String id) throws LaiKeAPIException;

    /**
     * 场次开关
     *
     * @param vo -
     * @param id -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022/7/1 16:18
     */
    void sessionSwitch(MainVo vo, String id) throws LaiKeAPIException;
}
