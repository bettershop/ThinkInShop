package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.mch.RoleModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * 权限
 *
 * @author Trick
 * @date 2021/1/14 9:37
 */
public interface RoleModelMapper extends BaseMapper<RoleModel>
{


    /**
     * 获取权限列表
     *
     * @param map -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/14 9:38
     */
    List<Map<String, Object>> selectRoleInfo(Map<String, Object> map) throws LaiKeAPIException;


    /**
     * 获取权限列表-统计
     *
     * @param map -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/14 9:38
     */
    int countRoleInfo(Map<String, Object> map) throws LaiKeAPIException;

    List<RoleModel> selectByIds(ArrayList<String> strings);
}