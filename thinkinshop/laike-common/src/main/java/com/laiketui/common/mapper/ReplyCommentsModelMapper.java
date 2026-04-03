package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.product.ReplyCommentsModel;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 评论回复表
 *
 * @author Trick
 * @date 2023/3/6 14:03
 */
public interface ReplyCommentsModelMapper extends BaseMapper<ReplyCommentsModel>
{

    //获取商家回复
    @Select("select a.content from lkt_reply_comments a inner join lkt_mch b on a.uid=b.user_id inner join lkt_mch mch on mch.id=#{mchId} and mch.user_id=a.uid where a.cid=#{commentId} and a.type = 1 order by a.add_time limit 1")
    String getMchReplyInfo(int commentId, int mchId);

    @Override
    List<Map<String, Object>> selectDynamic(Map<String, Object> map) throws LaiKeAPIException;

    @Override
    int countDynamic(Map<String, Object> map) throws LaiKeAPIException;

    //回复数量
    @Select("select count(1) from lkt_reply_comments where cid=#{cid} and type=0 and sid is null ")
    int countReplyNum(int cid);
}