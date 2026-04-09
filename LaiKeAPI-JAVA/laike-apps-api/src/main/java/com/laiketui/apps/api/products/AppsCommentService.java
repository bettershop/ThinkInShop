package com.laiketui.apps.api.products;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;

import java.util.Map;

/**
 * 评论
 *
 * @author Trick
 * @date 2023/3/6 11:03
 */
public interface AppsCommentService
{

    /**
     * 获取商品评论列表
     *
     * @param vo      -
     * @param goodsId -
     * @param type    - 类型 (0=全部,1=好评,2=中评,3=差评,4=有图)
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2023-03-06 11:06:24 -以优化
     */
    Map<String, Object> getComment(MainVo vo, int goodsId, int type) throws LaiKeAPIException;

    /**
     * 获取评论详情
     *
     * @param vo        -
     * @param commentId -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2023/3/6 14:25
     */
    Map<String, Object> getCommentDetail(MainVo vo, int commentId) throws LaiKeAPIException;

    /**
     * 获取评论详情回复列表
     *
     * @param vo        -
     * @param commentId -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2023/3/6 14:44
     */
    Map<String, Object> getCommentReplyList(MainVo vo, int commentId) throws LaiKeAPIException;


    /**
     * 发表回复
     *
     * @param vo        -
     * @param commentId -
     * @param sid       - @人的userId
     * @param text      - 发表内容
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2023/3/6 14:44
     */
    void sendComment(MainVo vo, int commentId, Integer sid, String text) throws LaiKeAPIException;


}
