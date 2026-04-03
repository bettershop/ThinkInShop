package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.domain.authority.UserAuthorityModel;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 用户权限映射表
 *
 * @author Trick
 * @date 2021/12/12 16:07
 */
public interface UserAuthorityModelMapper extends BaseMapper<UserAuthorityModel>
{


    int countAdminList(Map<String, Object> map);

    List<Map<String, Object>> getAdminList(Map<String, Object> map);

    /**
     * 根据用户id获取权限店铺id
     *
     * @param userId -
     * @return String
     * @author Trick
     * @date 2022/1/5 16:21
     */
    @Select("select b.main_id mch_id  FROM lkt_user_authority a,lkt_user_role b " +
            " where a.main_id=#{userId} and a.type=1 and a.role_id=b.id ")
    String getMchIdByUserId(String userId);


    /**
     * 根据用户id获取权限id
     *
     * @param userId -
     * @return String
     * @author Trick
     * @date 2022/1/5 16:21
     */
    @Select("select b.id roleId  FROM lkt_user_authority a,lkt_user_role b " +
            " where a.main_id=#{userId} and a.type=1 and a.role_id=b.id ")
    String getRoleIdByUserId(String userId);

}