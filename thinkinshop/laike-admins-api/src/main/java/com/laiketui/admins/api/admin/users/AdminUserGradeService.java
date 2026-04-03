package com.laiketui.admins.api.admin.users;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.user.AddUserGradeVo;
import com.laiketui.domain.vo.user.AddUserRuleVo;

import java.util.Map;

/**
 * 用户等级管理
 *
 * @author Trick
 * @date 2021/1/8 11:26
 */
public interface AdminUserGradeService
{


    /**
     * 获取等级列表
     *
     * @param vo  -
     * @param gid - 可选
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/8 11:27
     */
    Map<String, Object> getUserGradeInfo(MainVo vo, Integer gid) throws LaiKeAPIException;


    /**
     * 保存用户等级
     *
     * @param vo -
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/8 14:40
     */
    void addUserGrade(AddUserGradeVo vo) throws LaiKeAPIException;


    /**
     * 获取赠送商品
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/8 17:29
     */
    Map<String, Object> getGiveGoodsList(MainVo vo) throws LaiKeAPIException;


    /**
     * 删除用户等级
     *
     * @param vo -
     * @param id -
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/8 17:34
     */
    boolean delUserGrade(MainVo vo, int id) throws LaiKeAPIException;


    /**
     * 获取用户配置信息
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/8 17:55
     */
    Map<String, Object> getUserConfigInfo(MainVo vo) throws LaiKeAPIException;


    /**
     * 增加/修改会员规则
     *
     * @param vo -
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/8 18:05
     */
    void addUserRule(AddUserRuleVo vo) throws LaiKeAPIException;
}
