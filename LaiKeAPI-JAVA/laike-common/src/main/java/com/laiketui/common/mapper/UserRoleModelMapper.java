package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.authority.UserRoleModel;

import java.util.List;
import java.util.Map;


/**
 * 通用 菜单角色表
 * lkt_user_role
 *
 * @author Trick
 * @date 2021/12/12 11:54
 */
public interface UserRoleModelMapper extends BaseMapper<UserRoleModel>
{

    @Override
    List<Map<String, Object>> selectDynamic(Map<String, Object> map) throws LaiKeAPIException;

    @Override
    int countDynamic(Map<String, Object> map) throws LaiKeAPIException;
}