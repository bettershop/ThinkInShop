package com.laiketui.common.api.cascade;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.user.UserGradeModel;
import com.laiketui.domain.vo.MainVo;

import java.util.List;
import java.util.Map;

/**
 * 级联查询接口
 *
 * @author Trick
 * @date 2021/1/7 15:17
 */
public interface PublicCascadeService
{


    /**
     * 获取会员等级列表
     *
     * @param storeId -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/7 15:18
     */
    List<UserGradeModel> getGradeList(int storeId) throws LaiKeAPIException;


    /**
     * 获取来源列表
     *
     * @param storeId -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/7 15:31
     */
    Map<String, Object> getSourceList(int storeId) throws LaiKeAPIException;


    /**
     * 短信类别级联
     *
     * @param name -
     * @param vo   -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/15 16:01
     */
    Map<String, Object> getSmsTypeList(MainVo vo, String name) throws LaiKeAPIException;

    /**
     * 获取短信模板
     *
     * @param type -
     * @param id   -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/8/9 10:55
     */
    Map<String, Object> getSmsTemplateList(MainVo vo, Integer type, Integer id) throws LaiKeAPIException;

    interface SmsType
    {
        Integer VERIFICATION = 0;
        Integer NOTIFICATION = 1;
    }


    /**
     * 省市级联-更具名称
     *
     * @param groupName - 省市县名称
     * @param groupId   - 上级id
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/5/27 11:47
     */
    List<Map<String, Object>> getJoinCityCounty(String groupName, int groupId) throws LaiKeAPIException;
}
