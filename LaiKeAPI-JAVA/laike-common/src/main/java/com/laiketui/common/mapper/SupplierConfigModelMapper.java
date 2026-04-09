package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.supplier.SupplierConfigModel;
import org.apache.ibatis.annotations.Select;

public interface SupplierConfigModelMapper extends BaseMapper<SupplierConfigModel>
{

    @Select("SELECT * FROM lkt_supplier_config WHERE store_id = #{storeId}")
    SupplierConfigModel getConfig(int storeId) throws LaiKeAPIException;
}