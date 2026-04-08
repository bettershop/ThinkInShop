package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.domain.product.CommentsImgModel;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 评论图片记录表
 *
 * @author Trick
 * @date 2021/10/21 17:47
 */
public interface CommentsImgModelMapper extends BaseMapper<CommentsImgModel>
{

    //获取评论图片
    @Select("select comments_url from lkt_comments_img where comments_id=#{commentsId}")
    List<String> getCommentsImages(int commentsId);

    //获取评论图片
    @Select("select comments_url from lkt_comments_img where comments_id=#{commentsId} and type=#{type} ")
    List<String> getCommentsImagesByType(int commentsId, int type);
}