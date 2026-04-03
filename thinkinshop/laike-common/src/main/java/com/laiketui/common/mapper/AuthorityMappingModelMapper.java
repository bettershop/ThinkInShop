package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.authority.AuthorityMappingModel;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 菜单权限映射表
 *
 * @author Trick
 * @date 2021/12/18 15:05
 */
public interface AuthorityMappingModelMapper extends BaseMapper<AuthorityMappingModel>
{

    @Select("select b.* from lkt_authority_mapping a RIGHT JOIN lkt_menu b on a.menu_id = b.id  " +
            "and `level` = #{level} " +
            "where a.role_id=#{roleId}")
    List<Map<String, Object>> getRoleMenu(int level, String roleId);


    @Override
    int saveList(Map<String, Object> map) throws LaiKeAPIException;
}