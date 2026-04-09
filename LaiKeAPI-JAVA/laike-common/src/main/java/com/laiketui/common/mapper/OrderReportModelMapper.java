package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.domain.report.OrderReportModel;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Select;

public interface OrderReportModelMapper extends BaseMapper<OrderReportModel>
{

    @Delete("delete from lkt_order_report where  store_id=#{storeid} and type=#{type}")
    void deleteByType(int storeid, int type);

    @Select("select type,num,data,store_id from lkt_order_report where store_id = #{store_id} and type = #{type}")
    int getOne(int store_id, int type);

}