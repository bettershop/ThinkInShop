package com.laiketui.plugins.api.group.h5;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.plugins.api.group.vo.PluginsGroupGoodsVo;

import java.util.Map;

/**
 * 移动端拼团商品相关接口
 *
 * @author Trick
 * @date 2023/3/27 10:53
 */
public interface PluginsGroupAppGoodsService
{


    /**
     * 获取拼团商品列表
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2023/3/27 11:16
     */
    Map<String, Object> index(PluginsGroupGoodsVo vo) throws LaiKeAPIException;

    /**
     * 获取拼团规则
     *
     * @param vo    -
     * @param mchId -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2023/3/29 19:48
     */
    Map<String, Object> rule(MainVo vo, Integer mchId) throws LaiKeAPIException;

    /**
     * 商品信息
     *
     * @param vo      -
     * @param acId    - 活动
     * @param goodsId - 活动
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2023/3/30 10:04
     */
    Map<String, Object> goodsInfo(MainVo vo, String acId, int goodsId) throws LaiKeAPIException;

    /**
     * 规格弹窗
     *
     * @param vo     -
     * @param openId -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2023/4/10 20:55
     */
    Map<String, Object> djAttribute(MainVo vo, String openId) throws LaiKeAPIException;

    /**
     * 拼团列表
     *
     * @param vo     -
     * @param acId   -
     * @param goodId -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2023/4/4 11:23
     */
    Map<String, Object> groupList(MainVo vo, String acId, Integer goodId) throws LaiKeAPIException;

    /**
     * 拼团详情
     *
     * @param vo     -
     * @param openId -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2023/3/31 14:50
     */
    Map<String, Object> groupInfo(MainVo vo, String openId) throws LaiKeAPIException;
}
