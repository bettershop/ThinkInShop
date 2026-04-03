package com.laiketui.apps.user.services;

import com.laiketui.apps.api.user.AppsUserService;
import com.laiketui.common.mapper.UserMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.user.User;
import com.laiketui.domain.user.UserAddress;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @description:
 * @author: wx
 * @date: Created in 2019/10/29 11:01
 * @version:
 * @modified By:
 */
@Service
public class AppsUserServiceImpl implements AppsUserService
{

    @Autowired
    private UserMapper userMapper;

    @Override
    public User getUser(User user) throws LaiKeAPIException
    {
        User user1 = userMapper.selectByPrimaryKey(user.getId());
        return user1;
    }

    @Override
    public UserAddress getUserAddress(UserAddress userAddress) throws LaiKeAPIException
    {
        return null;
    }

    @Override
    public void insertUser(User user) throws LaiKeAPIException
    {

        userMapper.insert(user);
    }

    @Override
    public void deleteUser(User user) throws LaiKeAPIException
    {
        userMapper.delete(user);
    }

    @Override
    public void uploadUser(User user) throws LaiKeAPIException
    {
        userMapper.updateByPrimaryKey(user);
    }


    @Override
    public List<User> getUsers() throws LaiKeAPIException
    {
        return userMapper.selectAll();
    }


}
