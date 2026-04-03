package com.laiketui.comps.api.im;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.onlinemessage.AddOnlineMessageVo;

import java.util.Map;

public interface CompsOnlineMessageService
{
    /**
     * 查询用户与店铺在线消息
     *
     * @param vo
     * @param userId
     * @param mchId
     * @param type
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> getMessageList(MainVo vo, String userId, String mchId, Integer type) throws LaiKeAPIException;

    /**
     * 添加在线消息
     *
     * @param addOnlineMessageVo
     * @throws LaiKeAPIException
     */
    void addMessage(AddOnlineMessageVo addOnlineMessageVo) throws LaiKeAPIException;

    /**
     * 查询店铺会话的用户列表
     *
     * @param vo
     * @param mchId
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> mchUserList(MainVo vo, String mchId, String userName) throws LaiKeAPIException;

    /**
     * 查询用户会话的店铺列表
     *
     * @param vo-
     * @param userId -
     * @return
     * @throws LaiKeAPIException 2023-10-30
     *                           gp
     */
    Map<String, Object> userMchList(MainVo vo, String userId, String mchId, String mchName) throws LaiKeAPIException;
}
