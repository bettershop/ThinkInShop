package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.plugin.group.PtGoGroupOrderModel;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

/**
 * @author Trick
 */
public interface PtGoGroupOrderModelMapper extends BaseMapper<PtGoGroupOrderModel>
{

    /**
     * 修改订单状态
     *
     * @param storeId -
     * @param orderno -
     * @param status  -
     * @return Integer
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/4/1 11:19
     */
    @Update("update lkt_pt_go_group_order set `status`=#{status} where sno=#{orderno} and store_id=#{storeId}")
    Integer updateStatus(int storeId, String orderno, int status) throws LaiKeAPIException;

    /**
     * 获取拼团订单列表
     *
     * @param map -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/3/24 9:43
     */
    List<Map<String, Object>> getGrouporder(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 当前用户成功拼团数量
     *
     * @param storeId -
     * @param userId  -
     * @param ptCode  -
     * @return Integer
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/3/26 18:44
     */
    @Select("select count(id) as count from lkt_pt_go_group_order where ptcode = #{ptCode} and user_id = #{userId} " +
            "and status NOT IN (0,6,7,8) and ptcode != '' and store_id = #{storeId}")
    Integer countGroupPayOrderNum(int storeId, String userId, String ptCode) throws LaiKeAPIException;

    /**
     * 统计
     *
     * @param map -
     * @return Integer
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/3/25 10:17
     */
    Integer countGrouporder(Map<String, Object> map) throws LaiKeAPIException;

    Integer countGrouporderList(Map<String, Object> map) throws LaiKeAPIException;

}