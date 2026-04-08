package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.supplier.SupplierProModel;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

/**
 * @author sunH_
 */
public interface SupplierProModelMapper extends BaseMapper<SupplierProModel>
{

    int countCondition(Map<String, Object> map) throws LaiKeAPIException;

    List<Map<String, Object>> selectCondition(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 获取商城商品最新顺序号
     *
     * @param storeId -
     * @return int
     * @throws LaiKeAPIException -
     * @author sunH_
     * @date 2022/09/27 13:50
     */
    @Select("select COALESCE(MAX(sort),0) + 1 as sort from lkt_supplier_pro where store_id = #{storeId} ")
    int getGoodsMaxSort(int storeId) throws LaiKeAPIException;

    /**
     * 获取供应商商品销量排序
     *
     * @param storeId -
     * @return int
     * @throws LaiKeAPIException -
     * @author sunH_
     * @date 2022/09/27 13:50
     */
    @Select("SELECT id,imgurl,product_title,real_volume as volume FROM lkt_product_list WHERE store_id = #{storeId} AND recycle = 0 AND mch_id = 0 AND gongyingshang = #{supplierId} AND status != 4 ORDER BY volume DESC")
    List<Map<String, Object>> orderByVolume(int storeId, String supplierId) throws LaiKeAPIException;

    /**
     * 增加供应商商品总库存
     *
     * @param pid -
     * @param num -
     * @return int
     * @throws LaiKeAPIException -
     */
    @Update("update lkt_supplier_pro set num = num + #{num} where id = #{pid}")
    int addGoodsStockNum(@Param("pid") int pid, @Param("num") int num) throws LaiKeAPIException;
}