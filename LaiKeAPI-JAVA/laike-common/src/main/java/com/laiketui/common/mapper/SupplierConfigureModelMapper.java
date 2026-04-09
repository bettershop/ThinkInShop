package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.supplier.SupplierConfigureModel;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface SupplierConfigureModelMapper extends BaseMapper<SupplierConfigureModel>
{

    /**
     * 获取某产品库存数量
     *
     * @param pid -
     * @return BigDecimal
     * @throws LaiKeAPIException -
     * @author sunH
     * @date 2022/9/15 14:09
     */
    @Select("select ifnull(sum(num),0) as num from lkt_supplier_configure where pid = #{pid} and recycle = 0")
    Integer countConfigGureNum(int pid) throws LaiKeAPIException;

    /**
     * 获取商品最小出售价格、最大原价格
     *
     * @param pid -
     * @return Map
     * @throws LaiKeAPIException -
     */
    @Select("SELECT ifnull(min(price),0) price, ifnull(max(yprice),0) yprice,img,unit FROM lkt_supplier_configure WHERE pid = #{pid} and recycle=0")
    SupplierConfigureModel getProductMinPriceAndMaxYprice(int pid) throws LaiKeAPIException;

    @Select("SELECT ifnull(min(price),0) price, ifnull(max(price),0) yprice,img,unit FROM lkt_supplier_configure WHERE pid = #{pid} and recycle=0")
    SupplierConfigureModel getProductMinPriceAndMaxPrice(int pid) throws LaiKeAPIException;

    /**
     * 回收除了当前的属性
     *
     * @param pid     -
     * @param pidList -
     * @return int
     * @throws LaiKeAPIException -
     */
    @Update("<script>" +
            "update lkt_supplier_configure set recycle = 1 where " +
            " pid=#{pid} " +
            " <if test='pidList != null '> " +
            "   <foreach collection=\"pidList\" item=\"id\" separator=\",\" open=\"and id not in(\" close=\")\"> " +
            "        #{id,jdbcType=INTEGER}" +
            "   </foreach> " +
            "</if> " +
            "</script>")
    int delAppointConfiGureInfo(int pid, List<Integer> pidList) throws LaiKeAPIException;

    /**
     * 增/减商品规格库存
     *
     * @param confiGureModel -
     * @return int
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/19 15:40
     */
    @Update("update lkt_supplier_configure set total_num = total_num + #{total_num},num = num + #{num} where pid = #{pid} and id = #{id}")
    int addGoodsAttrStockNum(SupplierConfigureModel confiGureModel) throws LaiKeAPIException;
}