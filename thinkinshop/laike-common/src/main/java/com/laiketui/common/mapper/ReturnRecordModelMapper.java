package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.order.ReturnRecordModel;
import org.apache.ibatis.annotations.Select;

import java.util.List;


/**
 * 售后信息记录表
 *
 * @author Trick
 * @date 2020/12/4 11:52
 */
public interface ReturnRecordModelMapper extends BaseMapper<ReturnRecordModel>
{

    /**
     * 获取售后信息
     *
     * @param storeId  -
     * @param refundId - 售后id
     * @return ReturnRecordModel
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2023/2/24 10:06
     */
    @Select("select * from lkt_return_record where store_id = #{storeId} and p_id = #{refundId} order by re_time desc,r_type desc")
    List<ReturnRecordModel> getReturnRecord(int storeId, int refundId) throws LaiKeAPIException;
}