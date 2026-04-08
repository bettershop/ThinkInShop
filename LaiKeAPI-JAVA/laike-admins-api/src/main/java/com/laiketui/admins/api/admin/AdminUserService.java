package com.laiketui.admins.api.admin;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.admin.UpdateAdminVo;
import com.laiketui.domain.vo.user.AdminColorVo;
import com.laiketui.domain.vo.user.AdminLoginVo;

import java.util.Map;

/**
 * 商城后台登录
 *
 * @author Trick
 * @date 2021/1/26 11:30
 */
public interface AdminUserService
{
    /**
     * 后台登录
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/6/15 15:55
     */
    Map<String, Object> login(AdminLoginVo vo) throws LaiKeAPIException;

    /**
     * 退出登录
     *
     * @param vo -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022/1/6 16:54
     */
    void logout(MainVo vo) throws LaiKeAPIException;

    /**
     * 赋予系统管理员商城
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/8/17 16:27
     */
    Map<String, Object> setUserAdmin(MainVo vo) throws LaiKeAPIException;

    /**
     * 修改当前管理员基本信息
     *
     * @param vo -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/9/6 11:38
     */
    void updateAdminInfo(UpdateAdminVo vo) throws LaiKeAPIException;

    /**
     * 设置管理用户 系统语言
     *
     * @param vo -
     * @throws LaiKeAPIException
     * @date 2023/4/7 11:04
     */
    void updateLanguageByUser(MainVo vo) throws LaiKeAPIException;

    /**
     * 获取验证码
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author gp
     * @date 2023-09-21
     */
    Map<String, Object> getCode(MainVo vo) throws LaiKeAPIException;

    /**
     * 标记公告以读
     *
     * @param vo
     * @param tell_id 公告id
     */
    void markToRead(MainVo vo, Integer tell_id) throws LaiKeAPIException;

    /**
     * 获取平台维护公告
     *
     * @param vo
     * @return
     */
    Map<String, Object> getUserTell(MainVo vo);

    void updateAdminColor(AdminColorVo vo);
}
