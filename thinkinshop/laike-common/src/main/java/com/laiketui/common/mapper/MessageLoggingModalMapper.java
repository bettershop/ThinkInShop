package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.message.MessageLoggingModal;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

/**
 * 功能：管理后台首页消息弹窗
 *
 * @author wangxian
 */
public interface MessageLoggingModalMapper extends BaseMapper<MessageLoggingModal>
{

    /**
     * 更新弹窗消息表的记录状态
     *
     * @param storeId
     * @param mchId
     * @param parameter
     * @param type
     * @return
     */
    @Update("update lkt_message_logging set is_popup = 1 , read_or_not = 1  where store_id = #{storeId} and mch_id = #{mchId} and parameter = #{parameter} and type in ( ${type} ) ")
    int updateMessLogInfo(int storeId, int mchId, String parameter, String type);


    /**
     * 标记已弹窗
     *
     * @param ids
     * @return
     */
    @Update("<script>" +
            " update lkt_message_logging set is_popup = 1 where " +
            " <if test='array != null '> " +
            "   <foreach collection=\"array\" item=\"id\" separator=\",\" open=\"id in(\" close=\")\"> " +
            "        #{id,jdbcType=INTEGER}" +
            "   </foreach> " +
            "</if> " +
            "</script>")
    int updateMessLogPopup(String[] ids);

    @Update("<script>" +
            "update lkt_message_logging set read_or_not = 1  where store_id = #{storeId} " +
            "<if test='mchId != null '> " +
            " and mch_id = #{mchId} " +
            "</if> " +
            "and type = #{type} " +
            "</script>")
    int noticeRead(int storeId, Integer mchId, String type);

    @Override
    List<Map<String, Object>> selectDynamic(Map<String, Object> map) throws LaiKeAPIException;

    @Override
    int countDynamic(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 商品审核同步h5店铺端和pc店铺端的消息通知
     *
     * @return
     */
    @Update("update lkt_message_logging set read_or_not = 1,is_popup=1  where store_id = #{storeId} and mch_id = #{mchId} and content = #{content} and type = #{type}")
    void readH5OrPcMch(int storeId, Integer mchId, String content, Integer type);

    List<Map<String, Object>> selectByTypes(Map<String, Object> paramMap);
}