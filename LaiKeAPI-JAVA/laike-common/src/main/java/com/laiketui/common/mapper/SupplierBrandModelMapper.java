package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.supplier.SupplierBrandModel;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

public interface SupplierBrandModelMapper extends BaseMapper<SupplierBrandModel>
{

    int countCondition(Map<String, Object> map) throws LaiKeAPIException;

    List<Map<String, Object>> selectCondition(Map<String, Object> map) throws LaiKeAPIException;

    //解除品牌与某个分类的关系-【调用此方法必须立即调用 removeClassBrandClean】
    @Update("update lkt_supplier_brand a set a.categories=REPLACE(categories,CONCAT(#{classId},','), '') where categories like CONCAT('%,',#{classId},',%') ")
    int removeClassBrand(Integer classId);

    //清理垃圾数据
    @Update("update lkt_supplier_brand a set a.categories='' where a.categories = ',' ")
    int removeClassBrandClean();

    //品牌分类是空的则绑定到默认分类下面
    @Update("update lkt_supplier_brand a set a.categories=CONCAT(',',(select cid from lkt_product_class x where x.store_id=a.store_id and x.is_default=1 and x.recycle=0),',') where a.categories='' and a.store_id=#{storeId} ")
    int bindDefaultClass(int storeId);
}