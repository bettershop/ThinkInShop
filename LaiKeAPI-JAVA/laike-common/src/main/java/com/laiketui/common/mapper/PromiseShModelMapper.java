package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.mch.PromiseShModel;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

public interface PromiseShModelMapper extends BaseMapper<PromiseShModel>
{

    /**
     * 获取保证金审核多表动态sql
     *
     * @param map -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/10/30 11:43
     */
    List<Map<String, Object>> getPromiseShInfoDynamic(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 获取保证金审核返回h5动态sql
     *
     * @param map -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/10/30 11:43
     */
    List<Map<String, Object>> getPromiseShRefusedWhyDynamic(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 获取保证金审核返回最新一条状态动态sql
     *
     * @param map -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/10/30 11:43
     */
    List<Map<String, Object>> getPromiseShWhyDynamic(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 获取保证金审核多表动态sql
     *
     * @param map -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/10/30 11:43
     */
    int countPromiseShInfoDynamic(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 获取保证金退还审核第一条
     *
     * @param mchId
     * @return
     * @throws LaiKeAPIException
     */
    @Select("select * from lkt_promise_sh where mch_id = #{mchId} order by add_date desc limit 1")
    Map<String, Object> getFirst(int mchId) throws LaiKeAPIException;

}