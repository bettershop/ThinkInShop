package com.laiketui.plugins.api.member;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.plugin.member.MemberUserVo;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * 管理平台会员管理
 *
 * @Author: sunH_
 * @Date: Create in 16:00 2022/6/29
 */
public interface PluginMemberAdminService
{

    /**
     * 会员列表
     *
     * @param vo
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> getMember(MemberUserVo vo, HttpServletResponse response) throws LaiKeAPIException;

    /**
     * 会员信息
     *
     * @param vo
     * @param userId
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> getMemberInfo(MainVo vo, String userId) throws LaiKeAPIException;

    /**
     * 非会员列表
     *
     * @param vo
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> getUser(MainVo vo, String userId, String userName, String phone) throws LaiKeAPIException;

    /**
     * 添加会员
     *
     * @param vo
     * @param userIds  用户id 多选用逗号隔开
     * @param method   开通方式 1-月卡 2-季卡 3-年卡
     * @param overTime 到期时间
     * @throws LaiKeAPIException
     */
    void addMember(MainVo vo, String userIds, Integer method, String overTime) throws LaiKeAPIException;

    /**
     * 编辑会员信息
     *
     * @param vo
     * @param overTime
     * @throws LaiKeAPIException
     */
    void updateMember(MainVo vo, String userId, String overTime) throws LaiKeAPIException;

    /**
     * 删除会员
     *
     * @param vo
     * @param userIds 用户id 多选用逗号隔开
     * @throws LaiKeAPIException
     */
    void delMember(MainVo vo, String userIds) throws LaiKeAPIException;

}
