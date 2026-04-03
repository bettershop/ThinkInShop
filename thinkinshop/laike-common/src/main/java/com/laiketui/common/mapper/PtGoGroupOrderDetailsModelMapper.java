package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.plugin.group.PtGoGroupOrderDetailsModel;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

/**
 * @author Trick
 */
public interface PtGoGroupOrderDetailsModelMapper extends BaseMapper<PtGoGroupOrderDetailsModel>
{

    @Override
    List<Map<String, Object>> selectDynamic(Map<String, Object> map) throws LaiKeAPIException;

    @Override
    int countDynamic(Map<String, Object> map) throws LaiKeAPIException;

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
    @Update("update lkt_pt_go_group_order_details set r_status=#{status} where r_sNo=#{orderno} and store_id=#{storeId}")
    Integer updateStatus(int storeId, String orderno, int status) throws LaiKeAPIException;

}