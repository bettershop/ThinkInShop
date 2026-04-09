package com.laiketui.admins.api.mch.authority;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.mch.pc.AddRoleMainVo;
import com.laiketui.domain.vo.user.UserRegisterVo;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * pc店铺权限
 *
 * @author Trick
 * @date 2021/12/12 11:58
 */
public interface MchPcRoleService
{
    /**
     * 角色列表
     *
     * @param vo     -
     * @param roleId -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/12/12 12:00
     */
    Map<String, Object> roleList(MainVo vo, String roleId) throws LaiKeAPIException;

    /**
     * 添加角色
     *
     * @param vo -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/12/17 15:40
     */
    void addRole(AddRoleMainVo vo) throws LaiKeAPIException;

    /**
     * 删除角色
     *
     * @param vo -
     * @param id -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/12/17 15:43
     */
    void delRole(MainVo vo, String id) throws LaiKeAPIException;

    /**
     * 获取用户所有的菜单权限-树形结构[前端请懒加载]
     *
     * @param vo     -
     * @param sid    - 菜单上级id
     * @param roleId - 角色id
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/12/12 12:05
     */
    Map<String, Object> getUserAuthorityTree(MainVo vo, String sid, String roleId) throws LaiKeAPIException;

    /**
     * 为用户绑定角色
     *
     * @param vo     -
     * @param userId -
     * @param roleId -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/12/12 12:06
     */
    void bindUserAuthorityTree(MainVo vo, String userId, String roleId) throws LaiKeAPIException;

    /**
     * 解除绑定
     *
     * @param vo -
     * @param id -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/12/27 19:42
     */
    void delBindUserAuthorityTree(MainVo vo, String id) throws LaiKeAPIException;

    /**
     * 获取用户列表
     *
     * @param vo     -
     * @param key    -
     * @param roleId -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/12/18 15:39
     */
    Map<String, Object> getUserList(MainVo vo, String key, String roleId) throws LaiKeAPIException;


    /**
     * 管理员日志
     *
     * @param vo        -
     * @param zhangHao  -
     * @param startDate -
     * @param endDate   -
     * @param response  -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/12/21 11:02
     */
    Map<String, Object> getRecord(MainVo vo, String zhangHao, String startDate, String endDate, HttpServletResponse response, String logOperationType, String logAccountType) throws LaiKeAPIException;

    /**
     * 删除管理员日志
     *
     * @param vo  -
     * @param ids -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/12/21 17:13
     */
    void delRecord(MainVo vo, String ids) throws LaiKeAPIException;

    /**
     * 获取管理员列表
     *
     * @param vo     -
     * @param roleId -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/12/21 17:53
     */
    Map<String, Object> getAdminList(MainVo vo, String roleId, String id) throws LaiKeAPIException;

    /**
     * 新增用户
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/9/23 9:48
     */
    Map<String, Object> insertUser(UserRegisterVo vo, String roleId, Integer isUpdate, String id, String token) throws LaiKeAPIException;

    /**
     * 验证手机号/账号是否已被注册
     *
     * @param stroeId  -
     * @param zhanghao -
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/9/23 9:49
     */
    boolean isRegister(int stroeId, String zhanghao) throws LaiKeAPIException;
}
