package com.laiketui.apps.api.app;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;

import java.util.Map;

/**
 * 直播
 *
 * @author Trick
 * @date 2020/12/16 16:23
 */
public interface AppsCstrLiveBroadcastService
{


    /**
     * 获取直播列表
     * 【php liveBroadcast.getLiveList】
     *
     * @param vo     -
     * @param roomId - 房间id
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/16 16:28
     */
    Map<String, Object> getLiveBroadcastList(MainVo vo, String roomId) throws LaiKeAPIException;

}
