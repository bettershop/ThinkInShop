package com.laiketui.apps.api.user;


import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.user.User;
import com.laiketui.domain.user.UserAddress;

import java.util.List;

/**
 * @description: 用户信息服务接口
 * @author: wx
 * @date: Created in 2019/10/19 11:00
 * @version: 1.0
 * @modified By:
 */
public interface AppsUserService
{

    /**
     * @return
     * @description 获取用户信息
     * @author wx
     * @date 2019/10/19 11:51
     */
    User getUser(User user) throws LaiKeAPIException;

    /**
     * @return
     * @description 获取用户地址
     * @author wx
     * @date 2019/10/19 11:51
     */
    UserAddress getUserAddress(UserAddress userAddress) throws LaiKeAPIException;

    /**
     * 新增用户
     *
     * @param user
     * @throws LaiKeAPIException
     */
    void insertUser(User user) throws LaiKeAPIException;

    /**
     * 删除用户
     *
     * @param user
     * @throws LaiKeAPIException
     */
    void deleteUser(User user) throws LaiKeAPIException;

    /**
     * 修改用户信息
     *
     * @throws LaiKeAPIException
     */
    void uploadUser(User user) throws LaiKeAPIException;

    List<User> getUsers() throws LaiKeAPIException;

}
