package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.product.ProductClassModel;
import com.laiketui.domain.supplier.SupplierProClassModel;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

public interface SupplierProClassModelMapper extends BaseMapper<SupplierProClassModel>
{

    int countCondition(Map<String, Object> map) throws LaiKeAPIException;

    List<Map<String, Object>> selectCondition(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 单表动态统计
     *
     * @param supplierProClassModel -
     * @return int
     * @throws LaiKeAPIException -
     */
    int getGoodsClassCount(SupplierProClassModel supplierProClassModel) throws LaiKeAPIException;

    /**
     * 获取类目信息
     *
     * @param supplierProClassModel -
     * @return List
     * @throws LaiKeAPIException -
     */
    List<ProductClassModel> getProductClassLevel(SupplierProClassModel supplierProClassModel) throws LaiKeAPIException;

    /**
     * 获取最新序号
     *
     * @param storeId -
     * @return int
     * @throws LaiKeAPIException -
     */
    @Select("select COALESCE(MAX(sort),0) + 1 from lkt_supplier_pro_class where store_id=#{storeId}")
    int getGoodsClassMaxSort(int storeId) throws LaiKeAPIException;

    @Update("update lkt_supplier_pro_class set recycle=1 where sid=#{cid} and recycle = 0")
    int delClassBySid(int cid);
}