package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.onlinemessage.OnlineMessageModel;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

public interface OnlineMessageModelMapper extends BaseMapper<OnlineMessageModel>
{
    /**
     * 查询用户与店铺在线消息
     *
     * @param storeId
     * @param userId
     * @return
     * @throws LaiKeAPIException
     */
    List<Map<String, Object>> getMessageListByParams(@Param("storeId") int storeId, @Param("userId") String userId, @Param("mchId") String mchId, @Param("idList") List<Integer> idList) throws LaiKeAPIException;

    /**
     * 查询店铺会话的用户列表
     *
     * @param mchId
     * @return
     * @throws LaiKeAPIException
     */
    List<Map<String, Object>> mchUserList(@Param("storeId") int storeId, @Param("mchId") String mchId, @Param("userId") String userId, @Param("userName") String userName) throws LaiKeAPIException;

    /**
     * 查询用户会话的店铺列表
     *
     * @param mchId
     * @return
     * @throws LaiKeAPIException
     */
    List<Map<String, Object>> userMchList(@Param("storeId") int storeId, @Param("mchId") String mchId, @Param("userId") String userId, @Param("mchName") String mchName) throws LaiKeAPIException;

    /**
     * 查询用户会话的店铺列表
     *
     * @param mchId
     * @return
     * @throws LaiKeAPIException
     */
    List<Map<String, Object>> getUserMchList(@Param("storeId") int storeId, @Param("mchId") String mchId, @Param("userId") String userId) throws LaiKeAPIException;

    /**
     * 设置消息已读
     *
     * @param storeId
     * @param sendId
     * @param receiveId
     * @return
     */
    @Update("update lkt_online_message set is_read = 1 where send_id = #{sendId} and receive_id = #{receiveId} and is_read = 0 ")
    int updateIsRead(int storeId, String sendId, String receiveId);

    /**
     * 获取用户列表，店铺最近的一条聊天记录
     *
     * @param storeId
     * @param sendId
     * @param receiveId
     * @return第一条
     */
    @Select("select content,add_date from lkt_online_message where " +
            "(send_id = #{sendId} and receive_id = #{receiveId}) " +
            "or (send_id = #{receiveId} and receive_id = #{sendId}) " +
            "ORDER BY add_date DESC LIMIT 1 ")
    Map<String, Object> getUserMchArticleOneMessage(int storeId, String sendId, String receiveId);

    /**
     * 判断用户是否有未读消息
     *
     * @param storeId
     * @param UserId
     * @return
     */
    @Select("Select count(*) from lkt_online_message where  store_id = #{storeId} and receive_id = #{UserId} and is_read = 0 ")
    int countUserMessageNotRead(int storeId, String UserId);

    /**
     * 判断店铺是否有未读消息
     *
     * @param storeId
     * @param mchId
     * @return
     */
    @Select("Select count(*) from lkt_online_message where  store_id = #{storeId} and receive_id = #{mchId} and is_read = 0 ")
    int countMchMessageNotRead(int storeId, String mchId);
}
