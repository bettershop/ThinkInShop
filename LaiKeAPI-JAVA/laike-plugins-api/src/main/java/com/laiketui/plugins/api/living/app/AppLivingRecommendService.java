package com.laiketui.plugins.api.living.app;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;

import java.util.Map;

public interface AppLivingRecommendService
{

    /**
     * 查询推荐/关注
     *
     * @param vo
     * @param type     类型
     * @param sortType 排序类型
     * @param sort     排序
     * @return
     */
    Map<String, Object> index(MainVo vo, Integer type, String sortType, String sort);

    /**
     * 关注/取消关注
     *
     * @param vo
     * @param user_id
     * @throws LaiKeAPIException
     */
    void followAnchor(MainVo vo, String user_id, Integer roomId) throws LaiKeAPIException;


    /**
     * 查询关注主播
     *
     * @param vo
     * @return
     */
    Map<String, Object> queryFollowAnchor(MainVo vo, String userName);

    /**
     * 主播中心
     *
     * @param vo
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> selectLivingUser(MainVo vo, String liveUser) throws LaiKeAPIException;


    /**
     * 点赞
     *
     * @param vo
     * @param roomId
     * @param dz_num
     * @throws LaiKeAPIException
     */
    void spotLike(MainVo vo, Integer roomId, Integer dz_num) throws LaiKeAPIException;

    /**
     * 添加浏览记录
     *
     * @param vo
     * @param roomId
     * @throws LaiKeAPIException
     */
    Map<String, Object> addLivingBrowse(MainVo vo, Integer roomId, Integer audienceId) throws LaiKeAPIException;

}
