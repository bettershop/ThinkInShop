package com.laiketui.admins.api.mch.plugin;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.plugin.group.AddGroup;
import com.laiketui.domain.vo.plugin.group.AddGroupConfigVo;
import com.laiketui.domain.vo.plugin.group.QueryOpenGroupVo;

import java.util.Map;

/**
 * 拼团后台管理
 *
 * @author Trick
 * @date 2021/5/8 11:46
 */
public interface MchGroupService
{

    /**
     * 拼团管理首页
     *
     * @param vo        -
     * @param goodsName - 商品名称
     * @param status    - 拼团状态
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/5/8 11:48
     */
    Map<String, Object> index(MainVo vo, String goodsName, Integer status) throws LaiKeAPIException;


    /**
     * 获取开团记录列表
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/5/8 14:51
     */
    Map<String, Object> openGroupList(QueryOpenGroupVo vo) throws LaiKeAPIException;


    /**
     * 获取参团记录列表
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/5/8 14:51
     */
    Map<String, Object> canGroupList(QueryOpenGroupVo vo) throws LaiKeAPIException;


    /**
     * 拼团明细
     *
     * @param vo     -
     * @param ptcode -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/5/8 18:09
     */
    Map<String, Object> groupDetailList(MainVo vo, String ptcode) throws LaiKeAPIException;


    /**
     * 根据id获取拼团活动信息
     *
     * @param vo         -
     * @param goodsId    -
     * @param activityNo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/5/10 9:55
     */
    Map<String, Object> getGroupById(MainVo vo, Integer goodsId, Integer activityNo) throws LaiKeAPIException;

    /**
     * 添加/编辑拼团活动
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/5/10 9:29
     */
    Map<String, Object> addGroup(AddGroup vo) throws LaiKeAPIException;


    /**
     * 复制拼团活动
     *
     * @param vo         -
     * @param activityNo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/5/10 14:54
     */
    Map<String, Object> copyGroup(MainVo vo, int activityNo) throws LaiKeAPIException;


    /**
     * 拼团活动开始/结束
     *
     * @param vo         -
     * @param activityNo -
     * @param type       - 开始/结束拼团活动
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/5/10 16:02
     */
    Map<String, Object> switchGroup(MainVo vo, int activityNo, int type) throws LaiKeAPIException;


    /**
     * 拼团活动是否显示开始/结束
     *
     * @param vo         -
     * @param activityNo -
     * @param type       - 开始/结束拼团活动
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/5/10 16:02
     */
    Map<String, Object> switchGroupShow(MainVo vo, int activityNo, int type) throws LaiKeAPIException;


    /**
     * 删除拼团活动
     *
     * @param vo          -
     * @param activityNos -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/5/10 17:30
     */
    Map<String, Object> delGroup(MainVo vo, String activityNos) throws LaiKeAPIException;


    /**
     * 获取拼团配置信息
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/5/11 9:19
     */
    Map<String, Object> getGroupConfig(MainVo vo) throws LaiKeAPIException;


    /**
     * 添加/编辑配置信息
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/5/11 9:30
     */
    Map<String, Object> addGroupConfig(AddGroupConfigVo vo) throws LaiKeAPIException;
}
